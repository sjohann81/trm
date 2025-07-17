#!/usr/bin/python

import sys, string

context = [
	0x0000, 0x0000, 0x0000, 0x0000,		# r0 - r3
	0x0000, 0x0000, 0x0000, 0x0000,		# r4 - r7
	0x0000, 0x0000, 0x0000, 0x0000,		# r8 - r11
	0x0000, 0x0000, 0x0000, 0x0000,		# r12 - r15
]

context_pc = 0x0000
limit_sp = 0x0000

memory = []
terminput = []

def tohex(n):
	return "%s" % ("0000%x" % (n & 0xffff))[-4:]

def check(program) :
	for lin in program :
		flds = str.split(lin)
		for f in flds :
			if f == '****' :
				return 1
	return 0

def load(program) :
	global limit_sp
	
	lines = 0
	# load program into memory
	for lin in program :
		flds = str.split(lin)
		data = int(flds[1], 16)
		memory.append(data)
		lines += 1
	print ("[program (code + data): %d bytes]" % (len(memory) * 2))
	
	# set the stack limit to the end of program section
	limit_sp = (len(memory) * 2) + 2
	# fill the rest of memory with zeroes
	#for i in range(lines, 28672) :
	for i in range(lines, 30720) :
		memory.append(0)
	# set the stack pointer to the last memory position
	context[14] = len(memory) * 2
	print ("[memory size: %d]" % (len(memory) * 2))

def io(addr, val) :
	if addr == 0xf000 : print (val, end = ""), print ("\n", end = "")
	elif addr == 0xf002 : print (val, end = "")
	elif addr == 0xf004 : print (chr(val & 0xff), end = ""), print ("\n", end = "")
	elif addr == 0xf006 : print (chr(val & 0xff), end = "")
	elif addr == 0xf008 :
		while True :
			data1 = chr(memory[val >> 1] >> 8) 
			data2 = chr(memory[val >> 1] & 0xff)
			print (data1, end = ""), print (data2, end = "")
			if (data1 == '\0' or data2 == '\0') : break
			val = val + 2
		print ("")
	elif addr == 0xf00a :
		while True :
			data1 = chr(memory[val >> 1] >> 8) 
			data2 = chr(memory[val >> 1] & 0xff)
			print (data1, end = ""), print (data2, end = "")
			if (data1 == '\0' or data2 == '\0') : break
			val = val + 2
	elif addr == 0xf00c : print ("%04x" % (val))
	elif addr == 0xf00e : print ("%04x" % (val), end = "")
	elif addr == 0xf010 : pass
	elif addr == 0xf014 : pass
	elif addr == 0xf018 : pass
	elif addr == 0xf01c : pass
	else :
		print ("[error - invalid IO port (%04x)]" % addr)
		
	return 0

def cycle() :
	global terminput
	global context_pc
	
	pc = context_pc
	context[0] = 0x0000
	
	# fetch an instruction from memory
	instruction = memory[pc >> 1]
	
	# predecode the instruction (extract opcode fields)
	#
	# op1 op2 I ra   rb  
	# ccc cccci aaaa bbbb
	op1 = (instruction & 0xe000) >> 13
	op2 = (instruction & 0x1e00) >> 9
	imm = (instruction & 0x0100) >> 8
	ra  = (instruction & 0x00f0) >> 4
	rb  = (instruction & 0x000f)

	srctgt = 0
	src1 = 0
	src2 = 0
	src1u = 0
	src2u = 0
	immed = 0
	
	# operand select / sign extension
	if context[ra] > 0x7fff : src1 = context[ra] - 0x10000
	else : src1 = context[ra]
	srctgt = src1
	if imm == 0 :
		if context[rb] > 0x7fff : src2 = context[rb] - 0x10000
		else : src2 = context[rb]
		src1u = context[ra]
		src2u = context[rb]
	else :
		immed = memory[(pc >> 1) + 1]
		if context[rb] > 0x7fff : src1 = context[rb] - 0x10000
		else : src1 = context[rb]
		if immed > 0x7fff : src2 = immed - 0x10000
		else : src2 = immed
		src1u = context[rb]
		src2u = immed
	
	# decode and execute
	if op1 == 0 :
		if op2 == 0 : context[ra] = src1 & src2
		elif op2 == 2 : context[ra] = src1 | src2
		elif op2 == 3 : context[ra] = src1 ^ src2
		elif op2 == 4 : context[ra] = src1 + src2
		elif op2 == 5 : context[ra] = src1 - src2
		else :
			print ("[error - invalid logic or arithmetic instruction]")
			return 0
	elif op1 == 5 :
		if op2 == 0 : context[ra] = (0, 1) [src1 < src2]
		elif op2 == 1 : context[ra] = (0, 1) [src1 >= src2]
		elif op2 == 4 : context[ra] = (0, 1) [src1u < src2u]
		elif op2 == 5 : context[ra] = (0, 1) [src1u >= src2u]
		elif op2 == 8 : context[ra] = (0, 1) [src1 == src2]
		elif op2 == 9 : context[ra] = (0, 1) [src1 != src2]
		else :
			print ("[error - invalid comparison instruction]")
			return 0
	elif op1 == 1 :
		if op2 == 8 : context[ra] = src1 << (src2 & 0xf)
		elif op2 == 10 : context[ra] = src1u >> (src2 & 0xf)
		elif op2 == 11 : context[ra] = src1 >> (src2 & 0xf)
		else :
			print ("[error - invalid shift instruction]")
			return 0
	elif op1 == 2 :
		if imm == 0 : addr = src2u
		else : addr = src1u + src2u
		
		if op2 == 0 : 
			if addr >= 0xf000 : context[ra] = io(addr, srctgt & 0xffff)
			else : context[ra] = memory[addr >> 1]
		elif op2 == 2 or op2 == 3 :
			if addr & 1 : byte = memory[addr >> 1] & 0xff
			else : byte = memory[addr >> 1] >> 8
			if byte <= 0x7f or op2 == 3 : context[ra] = byte
			else : context[ra] = (byte - 0x100) & 0xffff
		elif op2 == 4 :
			if addr >= 0xf000 : io(addr, srctgt & 0xffff)
			else : memory[addr >> 1] = srctgt & 0xffff
		elif op2 == 6 :
			if addr & 1 : memory[addr >> 1] = (memory[addr >> 1] & 0xff00) | (srctgt & 0xff)
			else : memory[addr >> 1] = (memory[addr >> 1] & 0x00ff) | ((srctgt & 0xff) << 8)
		else :
			print ("[error - invalid data transfer instruction]")
			return 0
	elif op1 == 4 :
		src1 = srctgt
		if imm == 1 :
			if context[rb] > 0x7fff : src2 = context[rb] - 0x10000
			else : src2 = context[rb]
			srctgt = immed - 4
		else :
			src2 = 0
			srctgt = context[rb] - 2

		if op2 == 0 :
			if src1 < src2 : pc = srctgt
		elif op2 == 1 :
			if src1 >= src2 : pc = srctgt
		elif op2 == 4 :
			if (src1 & 0xffff) < (src2 & 0xffff) : pc = srctgt
		elif op2 == 5 :
			if (src1 & 0xffff) >= (src2 & 0xffff) : pc = srctgt
		elif op2 == 8 :
			if src1 == src2 : pc = srctgt
		elif op2 == 9 :
			if src1 != src2 : pc = srctgt
		else :
			print ("[error - invalid branch instruction]")
			return 0
	elif op1 == 7 :
		print ("[halt]")
		return 0
	else :
		print ("[error - invalid instruction]")
		return 0
			
	# increment the program counter
	if (imm == 0) :
		pc = pc + 2
	else :
		pc = pc + 4
	context_pc = pc
	
	# fix the stored word to the matching hardware size
	context[ra] &= 0xffff
	
	return 1

def run(program) :
	global limit_sp
	
	codes = {
		"and":0x0000, "or":0x0400, "xor":0x0600, "add":0x0800,
		"sub":0x0a00, "lsl":0x3000, "lsr":0x3400, "asr":0x3600,
		"ldw":0x4000, "ldb":0x4400, "lbu":0x4600, "stw":0x4800,
		"stb":0x4c00, "blt":0x8000, "bge":0x8200, "bbl":0x8800,
		"bae":0x8a00, "beq":0x9000, "bne":0x9200, "tlt":0xa000,
		"tge":0xa200, "tbl":0xa800, "tae":0xaa00, "teq":0xb000,
		"tne":0xb200, "hlt":0xf800
	}

	codes_inv = {v: k for k, v in codes.items()}
	
	cycles = 1
	args = sys.argv[1:]
	
	while True : 
		inst = memory[context_pc >> 1]
		last_pc = context_pc

		if args :
			if (args[0] == 'd') :
				try :
					if (inst & 0x0100) :
						print ("pc: %04x, instruction: %s r%d,r%d,%d" % (context_pc, codes_inv[inst & 0xfe00], (inst & 0x00f0) >> 4, (inst & 0x000f), memory[(context_pc >> 1) + 1]))
					else :
						print ("pc: %04x, instruction: %s r%d,r%d" % (context_pc, codes_inv[inst & 0xfe00], (inst & 0x00f0) >> 4, (inst & 0x000f)))
					print (" r0: [%04x]  r1: [%04x]  r2: [%04x]  r3: [%04x]" % (context[0], context[1], context[2], context[3]))
					print (" r4: [%04x]  r5: [%04x]  r6: [%04x]  r7: [%04x]" % (context[4], context[5], context[6], context[7]))
					print (" r8: [%04x]  r9: [%04x] r10: [%04x] r11: [%04x]" % (context[8], context[9], context[10], context[11]))
					print ("r12: [%04x] r13: [%04x] r14: [%04x] r15: [%04x]" % (context[12], context[13], context[14], context[15]))
					a = input()
					if a == 'q' :
						break
				except :
					pass
		
		if not cycle() : break
		cycles += 1
		
		if context[14] < limit_sp :
			print ("[error - stack overflow detected]")
			break;
	print ("%d cycles" % cycles)

def main() :
	sys.stdin.reconfigure(errors = 'ignore')
	program = sys.stdin.readlines()
	if (check(program)) :
		print ("[program has errors]")
	else :
		load(program)
		sys.stdin = open('/dev/tty')
		run(program)

if __name__ == "__main__" : main()
