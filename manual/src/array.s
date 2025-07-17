main
	add v0,zr,0					; v0 = 0 (i)
	ldw v1,zr,size				; v1 = size
loop
	bge v0,v1,endloop			; if i >= size, break
	lsl v2,v0,1					; v2 = i * 2
	ldw v3,v2,vet				; v3 = vet[i]
	sub v3,v3,5					; v3 -= 5
	stw v3,v2,vet				; vet[i] = v3
	stw v0,zr,0xf000			; print v0 (index)
	stw v3,zr,0xf000			; print v4 (value)
	add v0,v0,1					; i++
	beq zr,zr,loop				; goto loop
endloop
	hlt
    
vet
	0 1 2 3 4 5 6 7 8 9
size
	10
