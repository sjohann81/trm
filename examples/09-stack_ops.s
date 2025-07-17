main
; push 12345 and 2345 to stack
	or a0,zr,12345
	sub sp,sp,2
	stw a0,sp
	or a0,zr,2345
	sub sp,sp,2
	stw a0,sp
; call func1 (2 parameters on the stack)
	or lr,zr,ret1
	beq zr,zr,func1
ret1
; pop and print return value
	ldw a0,sp
	add sp,sp,2
	stw a0,zr,0xf000
; push 999, 888 and 777 to stack
	or a0,zr,999
	or a1,zr,888
	or a2,zr,777
	sub sp,sp,6
	stw a2,sp,0
	stw a1,sp,2
	stw a0,sp,4
; call func2 (3 parameters on the stack, count on a0)
	add a0,zr,3
	or lr,zr,ret2
	beq zr,zr,func2
ret2
; pop and print return value
	ldw a0,sp
	add sp,sp,2
	stw a0,zr,0xf000
	hlt

func1
	xor a1,a1
; pop and accummulate values
	ldw a2,sp
	add sp,sp,2
	add a1,a2
	ldw a2,sp
	add sp,sp,2
	add a1,a2
; push result to stack
	sub sp,sp,2
	stw a1,sp
	beq zr,lr

func2
	xor a1,a1
loopfunc2
; pop and accummulate values
	beq a0,zr,endfunc2
	ldw a2,sp
	add sp,sp,2
	add a1,a2
	sub a0,a0,1
	beq zr,zr,loopfunc2
endfunc2
; push result to stack
	sub sp,sp,2
	stw a1,sp
	beq zr,lr
