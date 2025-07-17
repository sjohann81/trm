main
; print an array of integers
	add v0,zr,array
	add v1,zr,a_limit
loop1
	bge v0,v1,end1
	ldw v2,v0
	stw v2,zr,0xf000
	add v0,v0,2
	beq zr,zr,loop1
end1
; print a line feed
	add v3,zr,10
	stw v3,zr,0xf006
; print an array of bytes
	add v0,zr,bytearray
	add v1,zr,ba_limit
loop2 
	bge v0,v1,end2
	ldb v2,v0
	stw v2,zr,0xf000
	add v0,v0,1
	beq zr,zr,loop2
end2
; print a line feed
	add v3,zr,10
	stw v3,zr,0xf006
; print an array of bytes (unsigned)
	add v0,zr,bytearray
	add v1,zr,ba_limit
loop3
	bge v0,v1,end3
	lbu v2,v0
	stw v2,zr,0xf000
	add v0,v0,1
	beq zr,zr,loop3
end3
	hlt

space
	[50]
s_limit

array
	0 -1 2 -3 4 -5 6 -7 8 -9 12345 -23456
a_limit

bytearray 
	$1 $2 $-42 $12 $-1
ba_limit
