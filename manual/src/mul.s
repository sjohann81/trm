; function: multiply two numbers
; arguments: a0 (multiplicand) and a1 (multiplier)
; result: a0 (product)

mul
	xor a2,a2
repmul01
	beq a1,zr,endmul01
	and r4,a1,1
	beq r4,zr,skipmul01
	add a2,a0
skipmul01
	lsl a0,a0,1
	lsr a1,a1,1
	beq zr,zr,repmul01
endmul01
	add a0,a2,0
	beq zr,lr
