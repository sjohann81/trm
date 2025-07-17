# TRM - Tiny RISC Machine

## What is this?

TRM is a basic CPU with reduced features. This is the implementation of a 16-bit instruction set architecture based on RISC principles with a focus on teaching computer architecture. It consists of a simple toolchain composed by an assembler and simulator / debugger (virtual CPU). There are example programs available and a complete user manual (in Portuguese). The following instructions are implemented:

* Logic / Arithmetic (and, or, xor, add, sub)
* Comparison (tlt, tge, tbl, tae, teq, tne)
* Shift (lsl, lsr, asr)
* Data transfer (ldw, ldb, lbu, stw, stb)
* Control transfer (blt, bge, bbl, bae, beq, bne)


## Machine Registers

The architecture has 16 general purpose registers. Register usage conventions are presented in the table. Calling conventions are presented in the user manual.

| Register	| Name		| Function							|
| ------------- | ------------- | ------------------------------------------------------------- |
| r0		| zr		| constant zero							|
| r1		| a0		| arguments, return values / scratch regs - not preserved	|
| r2		| a1		|								|
| r3		| a2		|								|
| r4		| a3		|								|
| r5		| v0		| local variables - preserved					|
| r6		| v1		|								|
| r7		| v2		|								|
| r8		| v3		|								|
| r9		| v4		|								|
| r10		| v5		|								|
| r11		| v6		|								|
| r12		| v7		|								|
| r13		| v8/fp		| frame pointer - preserved					|
| r14		| sp		| stack pointer - preserved					|
| r15		| lr		| link register - preserved					|


## Instruction format:

Instructions can be two or four bytes long. According to the addressing mode, instructions can have only register arguments or a combination of registers and a literal value. The following format is used by all instructions. Instructions that have a literal value encoded in the instruction stream, following the first instruction word. 'op1' and 'op2' fields are opcode values, the 'I' field tells if the next word encodes a 16 bit immediate value (literal), 'ra' encodes the A register and 'rb' the B register.

```
op1 op2 I ra   rb  
ccc cccci aaaa bbbb
```

# Instruction set

The instruction set is very compact, being only 25 instructions (excluding 'hlt' which is a pseudo operation). In practice, a subset of just 14 instructions is used for most class examples.

| Instruction	| Opcode	| Description			| Regular format		| Immediate format		| Other info		|
| ------------- | ------------- | ----------------------------- | ----------------------------- | ----------------------------- | --------------------- |
| and		| 000 0000	| logical and			| ra = ra and rb		| ra = rb and imm		|			|
| or		| 000 0010	| logical or			| ra = ra or rb			| ra = rb or imm		|			|
| xor		| 000 0011	| logical exclusive or		| ra = ra xor rb		| ra = rb xor imm		|			|
| add		| 000 0100	| arithmetic add		| ra = ra + rb			| ra = rb + imm			|			|
| sub		| 000 0101	| arithmetic subtract		| ra = ra - rb			| ra = rb - imm			|			|
| lsl		| 001 1000	| shift left			| ra = ra << rb			| ra = rb << imm		|			|
| lsr		| 001 1010	| shift right			| ra = ra >>> rb		| ra = rb >>> imm		|			|
| asr		| 001 1011	| arithmetic shift right	| ra = ra >> rb			| ra = rb >> imm		|			|
| ldw		| 010 0000	| load word			| ra = mem[rb]			| ra = mem[rb + imm]		| word access		|
| ldb		| 010 0010	| load byte			| ra = mem[rb]			| ra = mem[rb + imm]		| byte access		|
| lbu		| 010 0011	| load byte unsigned		| ra = mem[rb]			| ra = mem[rb + imm]		| byte access		|
| stw		| 010 0100	| store word			| mem[rb] = ra			| mem[rb + imm] = ra		| word access		|
| stb		| 010 0110	| store byte			| mem[rb] = ra			| mem[rb + imm] = ra		| byte access		|
| blt		| 100 0000	| branch if less than		| pc = ra < 0 ? rb : pc + 2	| pc = ra < rb ? imm : pc + 2	|			|
| bge		| 100 0001	| branch if greater or equal	| pc = ra >= 0 ? rb : pc + 2	| pc = ra >= rb ? imm : pc + 2	|			|
| bbl		| 100 0100	| branch if below		| pc = ra < 0 ? rb : pc + 2	| pc = ra < rb ? imm : pc + 2	| unsigned		|
| bae		| 100 0101	| branch if above or equal	| pc = ra >= 0 ? rb : pc + 2	| pc = ra >= rb ? imm : pc + 2	| unsigned		|
| beq		| 100 1000	| branch if equal		| pc = ra == 0 ? rb : pc + 2	| pc = ra == rb ? imm : pc + 2	|			|
| bne		| 100 1001	| branch if not equal		| pc = ra != 0 ? rb : pc + 2	| pc = ra != rb ? imm : pc + 2	|			|
| tlt		| 101 0000	| test if less than		| ra = ra < rb ? 1 : 0		| ra = rb < imm ? 1 : 0		|			|
| tge		| 101 0001	| test if greater or equal	| ra = ra >= rb ? 1 : 0		| ra = rb >= imm ? 1 : 0	|			|
| tbl		| 101 0100	| test if below			| ra = ra < rb ? 1 : 0		| ra = rb < imm ? 1 : 0		| unsigned		|
| tae		| 101 0101	| test if above or equal	| ra = ra >= rb ? 1 : 0		| ra = rb >= imm ? 1 : 0	| unsigned		|
| teq		| 101 1000	| test if equal			| ra = ra == rb ? 1 : 0		| ra = rb == imm ? 1 : 0	|			|
| tne		| 101 1001	| test if not equal		| ra = ra == rb ? 1 : 0		| ra = rb == imm ? 1 : 0	|			|
| hlt		| 111 1100	| halt program			| 				| 				| only on simulation	|


## Addressing modes

- register: two register operands
- register and immediate: two register operands and one immediate operand
- register indirect (base register): memory location in a register
- register indirect and displacement (base + displacement): memory location in a register + immediate

## Toolchain

There are two versions of the toolchain, being one in the Python programming language and the other in Java. The Python version consists of two programs: an assembler (assemble.py) and a simulator (simulator.py) and both programs read and write to stdin and from stdout. The Java version is more complete, as is composed by an assembler and a simulator with a debugger. To run an example, just launch the programs:

```
$ python3 python/assemble.py example/01.add.s > 01-add.txt
$ python3 python/simulator.py 01-add.txt
```

Or use the full featured Java simulator (use the flags 'r' or 'd' for a complete run or debugging):

```
$ java -jar java/Run.jar r example/01-add.s
```

## Simulator services

* 0xf000 write int (with line feed)
* 0xf002 write int
* 0xf004 write char (with line feed)
* 0xf006 write char
* 0xf008 write string (with line feedd)
* 0xf00a write string
* 0xf00c write hex (with line feed)
* 0xf00e write hex
* 0xf010 read int
* 0xf014 read char
* 0xf018 read string
* 0xf01c read hex
