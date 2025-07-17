main
	add v0,zr,vet				; v0 = &vet
	add v1,zr,endvet			; v1 = &vet + sizeof(vet)
loop
	bge v0,v1,endloop			; if &vet >= &endvet, break
	ldw v3,v0					; v3 = *vet
	sub v3,v3,5					; v3 -= 5
	stw v3,v0					; *vet = v3
	stw v0,zr,0xf000			; print v0 (address)
	stw v3,zr,0xf000			; print v2 (value)
	add v0,v0,2					; &vet++
	beq zr,zr,loop				; goto loop
endloop
	hlt
    
vet
	0 1 2 3 4 5 6 7 8 9
endvet
