main
	ldw v0,zr,var_a
	ldw v1,zr,var_b
	add v0,v1
	hlt
var_a
	123
var_b
	456
