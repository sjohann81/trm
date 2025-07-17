main
	add a0,zr,0x1234
	add a1,zr,0x5678
	add a2,zr,0x2345
	add a3,zr,0x6789
	add lr,zr,ret1
	beq zr,zr,add32
ret1
	stw a1,zr,0xf00e	; print hex
	stw a0,zr,0xf00c	; print hex (with line feed)
	hlt

add32
	sub sp,sp,4
	stw v0,sp,0
	stw v1,sp,2
	add v0,a0,0
	add v0,a2
	add v1,v0,0
	tbl v1,a0
	add v1,a1
	add a0,v1,0
	add a0,a3
	add a1,v0,0
	ldw v1,sp,2
	ldw v0,sp,0
	add sp,sp,4
	beq zr,lr
