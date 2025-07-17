main
	ldw v2,zr,var_b
; v1 = &var_a; v1 = *v1; v1 += v2; print v1
	add v1,zr,var_a
	ldw v1,v1
	add v1,v2
	stw v1,zr,0xf000
; v1 = &ptr_a; v1 = **v1; v1 += v2; print v1
	add v1,zr,ptr_a
	ldw v1,v1
	ldw v1,v1
	add v1,v2
	stw v1,zr,0xf000
	
	hlt

ptr_a
	var_a
var_a
	123
var_b
	456
