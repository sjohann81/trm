main
	add v0,zr,str1				; v0 = &str1
	add v1,zr,str2				; v1 = &str2
loop
	ldb v2,v0					; v2 = *str1
	stb v2,v1					; *str2 = v2
	add v0,v0,1					; &str1++
	add v1,v1,1					; &str2++
	beq v2,zr,endloop			; if v2 == 0, break
	beq zr,zr,loop				; goto loop
endloop

	add v1,zr,str2				; v1 = &str2
loop2
	ldb v2,v1					; v2 = *str2
	stb v2,zr,0xf000			; print v2
	add v1,v1,1					; &str2++
	beq v2,zr,endloop2			; if v2 == 0, break
	beq zr,zr,loop2				; goto loop2
endloop2
	hlt

str1
	"hello world!"
str2
	[30]
