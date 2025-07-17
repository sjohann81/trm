main
	ldw v0,zr,a					; v0 = a
	ldw v1,zr,b					; v1 = b
	ldw v2,zr,res				; v2 = res
	sub sp,sp,6					; push v2, v1, v0
	stw v2,sp,4
	stw v1,sp,2
	stw v0,sp,0
	add a0,sp,0					; a0 = sp
	add lr,zr,ret1				; lr = &ret1
	beq zr,zr,func1				; func1()
ret1
	ldw v0,sp,0					; pop v0, v1, v2
	ldw v1,sp,2
	ldw v2,sp,4
	add sp,sp,6
	stw v2,zr,res				; res = v2
	stw v2,zr,0xf000
	hlt
	
a
	123
b
	456
res
	0

func1
	ldw a2,a0,0					; a2 = a
	ldw a3,a0,2					; a3 = b
	add a2,a3					; a2 = a2 + a3
	stw a2,a0,4					; res = a2
	beq zr,lr					; return
