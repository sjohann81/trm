main
	add v0,zr,0					; v0 = 0 (i)
	ldw v1,zr,size				; v1 = size
loop
	bge v0,v1,endloop			; if i >= size, break
	ldb v2,v0,bytes				; v1 = bytes[i]
	lbu v3,v0,bytes				; v2 = bytes[i] (unsigned)
	stw v2,zr,0xf000			; print v1
	stw v3,zr,0xf000			; print v2
	add v0,v0,1					; i++
	beq zr,zr,loop				; goto loop
endloop
	hlt

bytes
	$-65 $66 $-67 $68 $-69 $5
size
	6
