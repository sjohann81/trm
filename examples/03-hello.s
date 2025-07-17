main
	add a0,zr,text		; a0 = &text
loop
	ldb v0,a0		; v0 = mem[a0]
	stw v0,zr,0xf006	; print char
	beq v0,zr,end		; if v0 == 0, goto end
	add a0,a0,1		; &text++
	beq zr,zr,loop		; goto loop
end
	add v0,zr,10		; print a line feed char
	stw v0,zr,0xf006
	hlt

text	
	"hello world!"
