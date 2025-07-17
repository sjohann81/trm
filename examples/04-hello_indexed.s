main
	add a0,zr,0
loop
	ldb v0,a0,text		; print string
	stw v0,zr,0xf006
	beq v0,zr,end
	add a0,a0,1
	beq zr,zr,loop
end
	add v0,zr,10		; print a line feed char
	stw v0,zr,0xf006
	add v0,zr,text		; print string service
	stw v0,zr,0xf008
	hlt

text
	"hello world!"
