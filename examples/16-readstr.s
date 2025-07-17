main
	add v0,zr,buf
	ldw v0,zr,0xf018
	add v1,zr,buf
	stw v1,zr,0xf008
	hlt
buf
	[50]
