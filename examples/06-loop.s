main
	add v0,zr,0
	add v1,zr,5
	add v2,zr,65
loop1
	bge v0,v1,endloop1
	stw v2,zr,0xf006
	add v0,v0,1
	beq zr,zr,loop1
endloop1
	add v2,v2,1
loop2
	blt v1,zr,endloop2
	stw v2,zr,0xf006
	sub v1,v1,1
	beq zr,zr,loop2
endloop2
	hlt
