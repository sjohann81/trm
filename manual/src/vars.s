main
	ldw v0,zr,var_a				; v0 = var_a
	ldw v1,zr,var_b				; v1 = var_b
	add v0,v1					; v0 = v0 + v1
	stw v0,zr,var_c				; var_c = v0
	stw v0,zr,0xf000			; print v0
	hlt

var_a
	123
var_b
	456
var_c
	0
