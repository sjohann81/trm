main
	ldw a0,zr,a					; a0 = a
	ldw a1,zr,b					; a1 = b
	add lr,zr,ret1				; lr = &ret1
	beq zr,zr,func1				; func1()
ret1
	stw a0,zr,res				; res = a0
	stw a0,zr,0xf000			; print a0
	hlt
	
a
	123
b
	456
res
	0

func1
	add a0,a1					; a0 = a0 + a1
	beq zr,lr					; return
