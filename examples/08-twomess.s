main
	add a0,zr,mess1
	add lr,zr,ret1
	beq zr,zr,print
ret1
	add a0,zr,mess2
	add lr,zr,ret2
	beq zr,zr,print
ret2
	hlt
	
print
	ldb a1,a0
	stw a1,zr,0xf006
	add a0,a0,1
	bne a1,zr,print
	beq zr,lr
	
mess1
	"Lorem ipsum dolor\n"
mess2
	"labore et dolore magna aliqua\n"
