#!/usr/bin/python3

import sys, string, re

codes = {
	"and":0x0000, "or":0x0400, "xor":0x0600, "add":0x0800,
	"sub":0x0a00, "lsl":0x3000, "lsr":0x3400, "asr":0x3600,
	"ldw":0x4000, "ldb":0x4400, "lbu":0x4600, "stw":0x4800,
	"stb":0x4c00, "blt":0x8000, "bge":0x8200, "bbl":0x8800,
	"bae":0x8a00, "beq":0x9000, "bne":0x9200, "tlt":0xa000,
	"tge":0xa200, "tbl":0xa800, "tae":0xaa00, "teq":0xb000,
	"tne":0xb200, "hlt":0xf800
}

lookup = {
	"r0":0, "r1":1, "r2":2, "r3":3,
	"r4":4, "r5":5, "r6":6, "r7":7,
	"r8":8, "r9":9, "r10":10, "r11":11,
	"r12":12, "r13":13, "r14":14, "r15":15,
	
	"zr":0, "a0":1, "a1":2, "a2":3,
	"a3":4, "v0":5, "v1":6, "v2":7,
	"v3":8, "v4":9, "v5":10, "v6":11,
	"v7":12, "v8":13, "fp": 13, "sp":14,
	"lr":15
}

def is_number(s):
	try:
		int(s)
		return True
	except ValueError:
		return False

def tohex(n):
	return "%s" % ("0000%x" % (n & 0xffff))[-4:]

def getval(s) :
	"return numeric value of a symbol or number"
	if not s : return 0							# empty symbol - zero
	a = lookup.get(s)							# get value or None if not in lookup
	if a == None : return int(s, 0)						# just a number (prefix can be 0x.. 0o.. 0b..)
	else : return a

def pass1(program) :
	"process pseudo operations"
	i = 0
	for lin in program :
		flds = str.split(lin)
		if flds :
			if flds[0] == ";" :
				program[i] = '\n'
			if flds[0] == "nop" :
				program[i] = "\tand	r0,r0\n"
			if flds[0] == "hlt" :
				program[i] = "\thlt	r0,r0\n"
		i += 1

def pass2(program) :
	"determine addresses for labels and add to the lookup dictionary"
	global lookup
	pc = 0
	for lin in program :
		flds = str.split(lin)
		if not flds : continue						# just an empty line
		if lin[0] > ' ' :
			symb = flds[0]						# a symbol - save its address in lookup
			lookup[symb] = pc
		else :
			if codes.get(flds[0]) == None :
				flds2 = ' '.join(flds)
				if flds2 :
					if flds2[0] == '"' and flds2[-1] == '"' :
						flds2 = lin
						flds2 = flds2[1:-1]
						flds2 = flds2.replace("\\t", chr(0x09))
						flds2 = flds2.replace("\\n", chr(0x0a))
						flds2 = flds2.replace("\\r", chr(0x0d))
						while (flds2[0] != '"') :
							flds2 = flds2[1:]
						flds2 = flds2[1:-1] + '\0'
						while (len(flds2) % 2) != 0 :
							flds2 = flds2 + '\0'
						pc = pc + len(flds2)
					elif flds2[0] == '[' and flds2[-1] == ']' :
						flds2 = lin
						flds2 = int(flds2[2:-2], 0)
						if (flds2 % 2) != 0 :
							flds2 = flds2 + 1
						pc = pc + flds2
					else:
						if flds2[0] == '$' :
							for f in flds :
								pc = pc + 1
								
							if (len(flds) % 2) != 0 :
								pc = pc + 1
						else :
							for f in flds :
								pc = pc + 2
			else :
				parts = ''.join(flds[1:])
				parts = str.split(parts, ";");					# strip comments on code lines
				parts = parts[0]
				parts = str.split(parts, ",")					# break opcode fields
				if len(parts) == 2 :
					pc = pc + 2
				if len(parts) == 3 :
					pc = pc + 4

def assemble(flds) :
	"assemble instruction to machine code"
	opval = codes.get(flds[0])
	symb = lookup.get(flds[0])
	if symb != None :
		return symb
	else :
		if opval == None : return int(flds[0], 0)			# just a number (prefix can be 0x.. 0o.. 0b..)
		parts = ''.join(flds[1:])
		parts = str.split(parts, ";");					# strip comments on code lines
		parts = parts[0]
		parts = str.split(parts, ",")					# break opcode fields
		if len(parts) == 2 :
			parts = [0, parts[0], parts[1]]
			return (opval | (getval(parts[1]) << 4) | (getval(parts[2]) & 0xf))
		if len(parts) == 3 :
			parts = [0, parts[0], parts[1], parts[2]]
			return (((opval | 0x0100 |
				(getval(parts[1]) << 4) | 
				(getval(parts[2]) & 0xf)) << 16) |
				getval(parts[3]) & 0xffff)

def pass3(program) :
	"translate assembly code and symbols to machine code"
	
	codes_inv = {v: k for k, v in codes.items()}
	
	args = sys.argv[1:]
	if args :
		args = args[0]
	else :
		args = ''

	pc = 0
	line = 0

	for lin in program :
		line = line + 1
		flds = str.split(lin)
		if lin[0] > ' ' : flds = flds[1:]			# drop symbol if there is one
		if not flds : continue
		try :
			flds2 = ' '.join(flds)
			if flds2[0] == '"' and flds2[-1] == '"' :
				flds2 = lin
				flds2 = flds2[1:-1]
				flds2 = flds2.replace("\\t", chr(0x09))
				flds2 = flds2.replace("\\n", chr(0x0a))
				flds2 = flds2.replace("\\r", chr(0x0d))
				while (flds2[0] != '"') :
					flds2 = flds2[1:]
				flds2 = flds2[1:-1] + '\0'
				while (len(flds2) % 2) != 0 :
					flds2 = flds2 + '\0'
				flds3 = ''
				while True :
					flds3 += (str((int(ord(flds2[0])) << 8) |
						int(ord(flds2[1])))) + ' '
					flds2 = flds2[2:]
					if flds2 == '' : break
				flds3 = str.split(flds3)
				instruction = assemble(flds3)
				print ("%04x %s" % (pc, tohex(instruction)))
				pc = pc + 2
				flds3 = flds3[1:]
				for f in flds3 :
					instruction = assemble(flds3)
					print ("%04x %s" % (pc, tohex(instruction)))
					pc = pc + 2
					flds3 = flds3[1:]
				flds = ''
			elif flds2[0] == '[' and flds2[-1] == ']' :
				flds2 = lin
				flds2 = int(flds2[2:-2], 0)
				if (flds2 % 2) != 0 :
					flds2 = flds2 + 1
				for i in range(0, flds2, 2) :
					print ("%04x 0000" % pc)
					pc = pc + 2
			else :
				if codes.get(flds[0]) == None :
					flds2 = ' '.join(flds)
					if (flds2[0] == '$') :
						if (len(flds) % 2) != 0 :
							flds.append('$0')
							
						for f in flds :
							if (len(flds) % 2) == 0 : 
								data = ((int(flds[0][1:], 0) & 0xff) << 8) | (int(flds[1][1:], 0) & 0xff)
								print ("%04x %s" % (pc, tohex(data)))
								pc = pc + 2
								flds = flds[1:]
							else :
								flds = flds[1:]
								continue
					else :
						for f in flds :
							data = assemble(flds)
							print ("%04x %s" % (pc, tohex(data)))
							pc = pc + 2
							flds = flds[1:]
				else :
					instruction = assemble(flds)
					if instruction < 65536 :	# instruction without immediate field
						print ("%04x %s    (%s r%d,r%d)" % (pc, tohex(instruction),
							codes_inv[instruction & 0xfe00],
							(instruction & 0xf0) >> 4,
							(instruction & 0xf)))
						pc = pc + 2
					else :				# instruction with immediate field
						inst = instruction >> 16
						imm = instruction & 0xffff
						print ("%04x %s    (%s r%d,r%d,%d)" % (pc, tohex(inst),
							codes_inv[inst & 0xfe00],
							(inst & 0xf0) >> 4,
							(inst & 0xf), imm))
						pc = pc + 2
						print ("%04x %s" % (pc, tohex(imm)))
						pc = pc + 2
						
		except :
			print ("**** ????    line %d --> %s" % (line, lin.replace(chr(0x0a), ' ')))
			return -1
	return 0

def main() :
	sys.stdin.reconfigure(errors = 'ignore')
	program = sys.stdin.readlines()
	pass1(program)
	pass2(program)
	pass3(program)

if __name__ == "__main__" : main()
