main
	add v0,zr,0
	add v1,zr,24
rep
	bge v0,v1,endrep
	add a0,v0,0
	
	add lr,zr,ret1
	beq zr,zr,fibonacci
ret1
	stw a0,zr,0xf000
	add v0,v0,1
	beq zr,zr,rep
endrep
	hlt


; int fibonacci(int i)
; {
;	if (i == 0)
;		return 0
; 	if (i == 1)
; 		return 1;
; 		
; 	return fibonacci(i - 1) + fibonacci(i - 2);
; }

fibonacci
; push v0, v1 and lr to the stack
	sub sp,sp,6
	stw v0,sp,0
	stw v1,sp,2
	stw lr,sp,4
; if (a0 == 0) goto endfib1
	beq a0,zr,endfib
; if (a0 == 1) goto endfib1
	add v0,zr,1
	beq a0,v0,endfib
; call fibonacci (i - 1)
	sub a0,a0,1
	add v1,a0,0
	add lr,zr,retfib1
	beq zr,zr,fibonacci
retfib1
; v0 = fibonacci(i - 1)
	add v0,a0,0
; call fibonacci(i - 2)
	sub a0,v1,1
	add lr,zr,retfib2
	beq zr,zr,fibonacci
retfib2
; a0 = fibonacci(i - 1) + fibonacci(i - 2) 
	add v0,a0
	add a0,v0,0
endfib
; pop v0, v1 and lr from the stack
	ldw v0,sp,0
	ldw v1,sp,2
	ldw lr,sp,4
	add sp,sp,6
; return
	beq zr,lr

