import java.io.*;
import java.util.*;

public class Simulator {

	private static int[] context;
	private static int context_pc = 0;
	private static int[] memory;
	private static int limit_sp = 0;
	private static final int MEMORY_SIZE_WORDS = 30720; //28672;

	public static boolean check(List<String> program) {
		for (String lin : program) {
			if (lin.contains("****")) {
				return true;
			}
		}
		return false;
	}

	public static int load(List<String> program) {
		context = new int[16];
		memory = new int[MEMORY_SIZE_WORDS];
		int lines = 0;
		try {
			for (String lin : program) {
				String[] flds = lin.trim().split("\\s+");
				if (flds.length >= 2) {
					int data = Integer.parseInt(flds[1], 16);
					memory[lines] = data;
					lines++;
				}
			}
			System.out.printf("[program (code + data): %d bytes]\n", lines * 2);
			
			context_pc = 0;
			limit_sp = (lines * 2) + 2;
			context[14] = MEMORY_SIZE_WORDS * 2;
			
			System.out.printf("[memory size: %d]\n", MEMORY_SIZE_WORDS * 2);
			
			return 0;
		} catch (Exception e) {
			System.out.println("[error loading object code - simulator failed]");
			
			return -1;
		}
	}
	
	public static int[] getcontext() {
		return context;
	}
	public static int[] getmemory() {
		return memory;
	}
	
	public static int getpc() {
		return context_pc;
	}
	
	public static int getinst() {
		return memory[context_pc >> 1];
	}
	
	public static int getinstdata() {
		return memory[(context_pc >> 1) + 1];
	}
	
	private static int io(int address, int val) {
		String input = "";
		Scanner scan = new Scanner(System.in);
		char data1, data2;
		int retval = 0;
		
		switch (address) {
			case 0xf000:	// write int (with line feed)
				System.out.printf("%d\n", val); break;
			case 0xf002:	// write int
				System.out.printf("%d", val); break;
			case 0xf004:	// write char (with line feed)
				System.out.printf("%c\n", (char)(val & 0xff)); break;
			case 0xf006:	// write char
				System.out.printf("%c", (char)(val & 0xff)); break;
			case 0xf008:	// write string (with line feed)
				val >>= 1;
				while (true) {
					data1 = (char)(memory[val] >> 8);
					data2 = (char)(memory[val] & 0xff);
					System.out.printf("%c%c", data1, data2); 
					if (data1 == 0 || data2 == 0) break;
					val++;
				}
				System.out.printf("\n"); 
				break;
			case 0xf00a:	// write string
				val >>= 1;
				while (true) {
					data1 = (char)(memory[val] >> 8);
					data2 = (char)(memory[val] & 0xff);
					System.out.printf("%c%c", data1, data2); 
					if (data1 == 0 || data2 == 0) break;
					val++;
				}
				break;
			case 0xf00c:	// write hex (with linefeed)
				System.out.printf("%04x\n", val & 0xffff); break;
			case 0xf00e:	// write hex
				System.out.printf("%04x", val & 0xffff); break;
			case 0xf010:	// read int
				System.out.printf("(int)? ");
				input = scan.nextLine();
				retval = Integer.decode(input);
				break;
			case 0xf014:	// read char
				System.out.printf("(char)? ");
				input = scan.nextLine();
				retval = Integer.parseInt(String.valueOf(input.charAt(0)));
				break;
			case 0xf018:	// read string
				System.out.printf("(string)? ");
				val >>= 1;
				input = scan.nextLine() + "\0\0";
				int i = 0;
				while (true) {
					data1 = (char)(input.charAt(i));
					data2 = (char)(input.charAt(i + 1));
					memory[val] = (data1 << 8) | data2;
					if (data1 == 0 || data2 == 0) break;
					val++;
					i += 2;
				}
				break;
			case 0xf01c:	// read hex
				System.out.printf("(hex)? ");
				input = scan.nextLine();
				retval = Integer.parseInt(input, 16);
				break;
			default:
				System.out.printf("[error - invalid IO port (%04x)]\n", address);
		}
		
		return retval;
	}

	private static boolean cycle() { //List<String> output
		int pc = context_pc;
		context[0] = 0x0000;

		// fetch an instruction from memory
		int instruction = memory[pc >> 1];

		// predecode the instruction (extract opcode fields)
		// op1 op2 I ra   rb  
		// ccc cccci aaaa bbbb
		int op1 = (instruction & 0xe000) >> 13;
		int op2 = (instruction & 0x1e00) >> 9;
		int imm = (instruction & 0x0100) >> 8;
		int ra = (instruction & 0x00f0) >> 4;
		int rb = (instruction & 0x000f);

		// operand select / sign extension
		int srctgt, src1, src2, src1u, src2u, immed = 0;

		srctgt = (short) context[ra];
		
		if (imm == 0) {
			src1 = (short) context[ra];
			src2 = (short) context[rb];
			src1u = context[ra];
			src2u = context[rb];
		} else {
			immed = memory[(pc >> 1) + 1];
			src1 = (short) context[rb];
			src2 = (short) immed;
			src1u = context[rb];
			src2u = immed;
		}

		int pc_before_branch = pc;

		// decode and execute
		switch (op1) {
			case 0:
				if (op2 == 0) context[ra] = src1 & src2;
				else if (op2 == 2) context[ra] = src1 | src2;
				else if (op2 == 3) context[ra] = src1 ^ src2;
				else if (op2 == 4) context[ra] = src1 + src2;
				else if (op2 == 5) context[ra] = src1 - src2;
				else { System.out.println("[error - invalid logic or arithmetic instruction]"); return false; }
				break;
			case 5:
				if (op2 == 0) context[ra] = src1 < src2 ? 1 : 0;
				else if (op2 == 1) context[ra] = src1 >= src2 ? 1 : 0;
				else if (op2 == 4) context[ra] = src1u < src2u ? 1 : 0;
				else if (op2 == 5) context[ra] = src1u >= src2u ? 1 : 0;
				else if (op2 == 8) context[ra] = src1 == src2 ? 1 : 0;
				else if (op2 == 9) context[ra] = src1 == src2 ? 1 : 0;
				else { System.out.println("[error - invalid comparison instruction]"); return false; }
				break;
			case 1:
				if (op2 == 8) context[ra] = src1 << (src2 & 0xf);
				else if (op2 == 10) context[ra] = src1u >>> (src2 & 0xf);
				else if (op2 == 11) context[ra] = src1 >> (src2 & 0xf);
				else { System.out.println("[error - invalid shift instruction]"); return false; }
				break;
			case 2:
				int addr = (imm == 0) ? src2u : (src1u + src2u);
				switch(op2) {
					case 0:
						if (addr >= 0xf000) context[ra] = io(addr, srctgt & 0xffff);
						else context[ra] = memory[addr >> 1];
						break;
					case 2:
					case 3:
						int word = memory[addr >> 1];
						int aByte = ((addr & 1) != 0) ? (word & 0xff) : (word >> 8);
						if (op2 == 2 && (aByte > 0x7f)) {
							context[ra] = (aByte - 0x100) & 0xffff;
						} else {
							context[ra] = aByte;
						}
						break;
					case 4:
						if (addr >= 0xf000) io(addr, srctgt);
						else memory[addr >> 1] = srctgt & 0xffff;
						break;
					case 6:
						if ((addr & 1) != 0) memory[addr >> 1] = (memory[addr >> 1] & 0xff00) | (srctgt & 0xff);
						else memory[addr >> 1] = (memory[addr >> 1] & 0x00ff) | ((srctgt & 0xff) << 8);
						break;
					default: System.out.println("[error - invalid data transfer instruction]"); return false;
				}
				break;
			case 4:
				src1 = (short) context[ra];
				int branch_target;
				if (imm == 1) {
					src2 = (short) context[rb];
					branch_target = immed;
				} else {
					src2 = 0;
					branch_target = context[rb];
				}

				boolean condition = false;
				if      (op2 == 0 && src1 < src2) condition = true;
				else if (op2 == 1 && src1 >= src2) condition = true;
				else if (op2 == 4 && (src1 & 0xffff) < (src2 & 0xffff)) condition = true;
				else if (op2 == 5 && (src1 & 0xffff) >= (src2 & 0xffff)) condition = true;
				else if (op2 == 8 && src1 == src2) condition = true;
				else if (op2 == 9 && src1 != src2) condition = true;
				
				if (condition) {
					pc = branch_target;
				}
				break;
			case 7:
				System.out.println("[halt]");
				return false;
			default:
				System.out.println("[error - invalid instruction]");
				return false;
		}

		// update the program counter
		if (pc == pc_before_branch)
			pc += (imm == 0) ? 2 : 4;
		context_pc = pc;
		
		// fix the stored word to the matching hardware size
		context[ra] &= 0xffff;

		return true;
	}

	public static void run() {
		long cycles = 0;
		while (true) {
			if (!cycle())
				break;
				
			cycles++;
			if (context[14] < limit_sp) {
				System.out.println("[error - stack overflow detected]");
				break;
			}
		}
		System.out.printf("%d cycles\n", cycles + 1);
	}
	
	public static boolean step() {
		boolean val;
		
		val = cycle();
				
		if (context[14] < limit_sp) {
			System.out.println("[error - stack overflow detected]");
			return false;
		}
		
		return val;
	}
}

