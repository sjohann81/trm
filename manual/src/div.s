; function: divide two numbers
; dependencies: udiv
; arguments: a0 (numerator) and a1 (denominator)
; result: a0 (quotient) and a1 (modulus)

div
	xor a2,a2
	bge a0,zr,skipdiv01
	xor a0,a0,-1
	add a0,a0,1
	add a2,zr,1
skipdiv01
	bge a1,zr,skipdiv02
	xor a1,a1,-1
	add a1,a1,1
	xor a2,a2,1
skipdiv02
	sub sp,sp,4
	stw a2,sp,0
	stw lr,sp,2
	add lr,zr,retudiv01
	beq zr,zr,udiv
retudiv01
	ldw lr,sp,2
	ldw a2,sp,0
	add sp,sp,4
	beq a2,zr,skipdiv03
	xor a0,a0,-1
	add a0,a0,1
skipdiv03
	beq zr,lr


; function: unsigned divide two numbers
; arguments: a0 (numerator) and a1 (denominator)
; result: a0 (quotient) and a1 (modulus)

udiv
	xor a2,a2
	add a3,zr,1
	bne a1,zr,repudiv01
	xor a0,a0
	beq zr,lr
repudiv01
	blt a1,zr,repudiv02
	lsl a1,a1,1
	lsl a3,a3,1
	beq zr,zr,repudiv01
repudiv02
	beq a3,zr,rependudiv02
	bbl a0,a1,skipudiv01
	sub a0,a1
	add a2,a3
skipudiv01
	lsr a1,a1,1
	lsr a3,a3,1
	beq zr,zr,repudiv02
rependudiv02
	add a1,a0,0
	add a0,a2,0
	beq zr,lr
