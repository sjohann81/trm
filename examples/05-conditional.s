main
	add v0,zr,65
	ldw v1,zr,val1
	ldw v2,zr,val2
	; v2 < v1?
	blt v2,v1,end
	; v1 < v2?
	blt v1,v2,less_than
	beq zr,zr,end
less_than
	stw v0,zr,0xf006
	add v0,v0,1
	ldw v1,zr,val2
	ldw v2,zr,val1
	; v1 < v2?
	blt v1,v2,end
	; v1 >= v2?
	bge v1,v2,gre_equal1
	beq zr,zr,end
gre_equal1
	stw v0,zr,0xf006
	add v0,v0,1
	; v2 >= v2?
	bge v2,v1,end
	; v2 >= v2?
	bge v2,v2,gre_equal2
	beq zr,zr,end
gre_equal2
	stw v0,zr,0xf006
	add v0,v0,1
	ldw v1,zr,val1
	ldw v2,zr,val2
	; v1 != v1?
	bne v1,v1,end
	; v1 != v2?
	bne v1,v2,not_equal
	beq zr,zr,end
not_equal
	stw v0,zr,0xf006
	add v0,v0,1
	; v1 == v2?
	beq v1,v2,end
	; v1 == v1?
	beq v1,v1,equal
	beq zr,zr,end
equal
	stw v0,zr,0xf006
	add v0,v0,1

comparisons
	ldw v1,zr,val1
	ldw v2,zr,val2
	tlt v1,v2
	beq v1,zr,end
	stw v0,zr,0xf006
	add v0,v0,1

	ldw v1,zr,val1
	ldw v2,zr,val2
	tge v1,v2
	bne v1,zr,end
	stw v0,zr,0xf006
	add v0,v0,1

	ldw v1,zr,val1
	ldw v2,zr,val2
	tbl v1,v2
	bne v1,zr,end
	stw v0,zr,0xf006
	add v0,v0,1

	ldw v1,zr,val1
	ldw v2,zr,val2
	teq v1,v2
	bne v1,zr,end
	stw v0,zr,0xf006
	add v0,v0,1
end
	hlt

val1
	-12345
val2
	555
