main
	add a0,zr,a					; a0 = &a
	add a1,zr,b					; a1 = &b
	add a2,zr,res				; a2 = &res
	add lr,zr,ret1				; lr = &ret1
	beq zr,zr,func1				; func1()
ret1
	ldw a0,zr,res				; a0 = res (not needed!)
	stw a0,zr,0xf000			; print a0
	hlt
	
a
	123
b
	456
res
	0

func1
	ldw a0,a0					; a0 = a
	ldw a1,a1					; a1 = b
	add a0,a1					; a0 = a0 + a1
	stw a0,a2					; res = a0
	beq zr,lr					; return
