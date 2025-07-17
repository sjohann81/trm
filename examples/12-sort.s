main
; print, sort and print again vec1
	add a0,zr,vec1
	add a1,zr,vec1end
	add lr,zr,ret1
	beq zr,zr,do_array
ret1
; print, sort and print again vec2
	add a0,zr,vec2
	add a1,zr,vec2end
	add lr,zr,ret2
	beq zr,zr,do_array
ret2
; print, sort and print again vec3
	add a0,zr,vec3
	add a1,zr,vec3end
	add lr,zr,ret3
	beq zr,zr,do_array
ret3
	hlt
	
; do_array (a0: &array, a1: &end_array)
do_array
; push arguments and lr to stack
; arguments a0 and a1 will be reused, and lr is saved because this
; function calls other functions
	sub sp,sp,6
	stw a0,sp,0
	stw a1,sp,2
	stw lr,sp,4

	add lr,zr,retprint
	beq zr,zr,print
retprint
	ldw a0,sp,0
	ldw a1,sp,2
	add lr,zr,retsort
	beq zr,zr,sort
retsort
	ldw a0,sp,0
	ldw a1,sp,2
	add lr,zr,retprint_sorted
	beq zr,zr,print
retprint_sorted
; restore pushed arguments and lr
	ldw a0,sp,0
	ldw a1,sp,2
	ldw lr,sp,4
	add sp,sp,6
	beq zr,lr

; array print (a0: &array, a1: &end_array)
print
printloop
	bge a0,a1,endprint
	ldw a2,a0
	stw a2,zr,0xf000
	add a0,a0,2
	beq zr,zr,printloop
endprint
	add a2,zr,10
	stw a2,zr,0xf006
	beq zr,lr

; array sort (a0: &array, a1: &end_array)
sort
; save v0, v1 and v2, as they will be used in this function and are
; defined as callee saved registers
	sub sp,sp,6
	stw v0,sp,0
	stw v1,sp,2
	stw v2,sp,4
	add a3,a1,0
	sub a1,a1,2
loop_i
	bge a0,a1,loop_iend
	add a2,a0,2
loop_j
	bge a2,a3,loop_jend
	ldw v0,a0
	ldw v1,a2
	blt v0,v1,skip_swap
swap
	add v2,v1,0
	add v1,v0,0
	add v0,v2,0
	stw v0,a0
	stw v1,a2
skip_swap
	add a2,a2,2
	beq zr,zr,loop_j
loop_jend
	add a0,a0,2
	beq zr,zr,loop_i
loop_iend
; restore saved registers
	ldw v0,sp,0
	ldw v1,sp,2
	ldw v2,sp,4
	add sp,sp,6
	beq zr,lr

; array data
vec1
	-4 3 22 -532 1256 53 2 0 52 -8
vec1end
vec2
	53 33 -2443 10002 -141
vec2end
vec3
	53 33 -2443 10002 -141 -4 3 22 -532 1256 53 2 0 52 -8
vec3end
