main
	add v0,zr,123		; v0 = 123
	add v0,v0,456		; v0 = v0 + 456
	stw v0,zr,0xf000	; print v0
	ldw v1,zr,var_a		; v1 = var_a
	ldw v2,zr,var_b		; v2 = var_b
	add v1,v2		; v1 = v1 + v2
	stw v1,zr,0xf000	; print v1
	hlt			; stop program

var_a
	123
var_b
	456
