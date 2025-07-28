import java.io.*;
import java.util.*;

public class Simulator {

	private static int[] context;
	private static int context_pc = 0;
	private static int[] memory;
	private static int lsaddr = -1;
	private static int limit_sp = 0;
	private static final int MEMORY_SIZE_WORDS = 30720; //28672;
	private static UserOutput out;
	private static UserInput in;

	Simulator(UserInput in, UserOutput out) {
		this.in = in;
		this.out = out;
	}

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
			out.putString(String.format("[program (code + data): %d bytes]\n", lines * 2));
			
			if (lines == 0) {
				out.putString(String.format("[no object code - simulator failed]\n"));
				
				return -1;
			}
			
			context_pc = 0;
			limit_sp = (lines * 2) + 2;
			context[14] = MEMORY_SIZE_WORDS * 2;
			
			out.putString(String.format("[memory size: %d]\n", MEMORY_SIZE_WORDS * 2));
			
			return 0;
		} catch (Exception e) {
			out.putString(String.format("[error loading object code - simulator failed]\n"));
			
			return -1;
		}
	}
	
	public static int[] getcontext() {
		return context;
	}
	public static int[] getmemory() {
		return memory;
	}
	
	public static int getlastlsaddr() {
		return lsaddr;
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
		char data1, data2;
		int retval = 0;
		
		switch (address) {
			case 0xf000:	// write int (with line feed)
				out.putString(String.format("%d\n", val)); break;
			case 0xf002:	// write int
				out.putString(String.format("%d", val)); break;
			case 0xf004:	// write char (with line feed)
				out.putString(String.format("%c\n", (char)(val & 0xff))); break;
			case 0xf006:	// write char
				out.putString(String.format("%c", (char)(val & 0xff))); break;
			case 0xf008:	// write string (with line feed)
				val >>= 1;
				while (true) {
					data1 = (char)(memory[val] >> 8);
					data2 = (char)(memory[val] & 0xff);
					out.putString(String.format("%c%c", data1, data2)); 
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
					out.putString(String.format("%c%c", data1, data2)); 
					if (data1 == 0 || data2 == 0) break;
					val++;
				}
				break;
			case 0xf00c:	// write hex (with linefeed)
				out.putString(String.format("%04x\n", val & 0xffff)); break;
			case 0xf00e:	// write hex
				out.putString(String.format("%04x", val & 0xffff)); break;
			case 0xf010:	// read int
				out.putString(String.format("[(int)?] "));
				retval = in.getInt();
				break;
			case 0xf014:	// read char
				out.putString(String.format("[(char)?] "));
				retval = in.getChar();
				break;
			case 0xf018:	// read string
				out.putString(String.format("[(string)?] "));
				in.getString(memory, val);
				break;
			case 0xf01c:	// read hex
				out.putString(String.format("[(hex)?] "));
				retval = in.getHex();
				break;
			default:
				out.putString(String.format("[error - invalid IO port (%04x)]\n", address));
		}
		
		return retval;
	}

	private static boolean cycle() {
		int pc = context_pc;
		context[0] = 0x0000;
		lsaddr = -1;

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
				else { out.putString(String.format("[error - invalid logic or arithmetic instruction]\n")); return false; }
				break;
			case 5:
				if (op2 == 0) context[ra] = src1 < src2 ? 1 : 0;
				else if (op2 == 1) context[ra] = src1 >= src2 ? 1 : 0;
				else if (op2 == 4) context[ra] = src1u < src2u ? 1 : 0;
				else if (op2 == 5) context[ra] = src1u >= src2u ? 1 : 0;
				else if (op2 == 8) context[ra] = src1 == src2 ? 1 : 0;
				else if (op2 == 9) context[ra] = src1 == src2 ? 1 : 0;
				else { out.putString(String.format("[error - invalid comparison instruction]\n")); return false; }
				break;
			case 1:
				if (op2 == 8) context[ra] = src1 << (src2 & 0xf);
				else if (op2 == 10) context[ra] = src1u >>> (src2 & 0xf);
				else if (op2 == 11) context[ra] = src1 >> (src2 & 0xf);
				else { out.putString(String.format("[error - invalid shift instruction]\n")); return false; }
				break;
			case 2:
				int addr = (imm == 0) ? src2u : (src1u + src2u);
				lsaddr = addr;
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
						lsaddr |= 0x10000;
						if (addr >= 0xf000) io(addr, srctgt);
						else memory[addr >> 1] = srctgt & 0xffff;
						break;
					case 6:
						lsaddr |= 0x10000;
						if ((addr & 1) != 0) memory[addr >> 1] = (memory[addr >> 1] & 0xff00) | (srctgt & 0xff);
						else memory[addr >> 1] = (memory[addr >> 1] & 0x00ff) | ((srctgt & 0xff) << 8);
						break;
					default: out.putString(String.format("[error - invalid data transfer instruction]\n"));
					 return false;
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
				out.putString(String.format("[halt]\n"));
				return false;
			default:
				out.putString(String.format("[error - invalid instruction]\n"));
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
				out.putString(String.format("[error - stack overflow detected]\n"));
				break;
			}
		}
		out.putString(String.format("%d cycles\n", cycles + 1));
	}
	
	public static boolean step() {
		boolean val;
		
		val = cycle();
				
		if (context[14] < limit_sp) {
			out.putString(String.format("[error - stack overflow detected]\n"));
			return false;
		}
		
		return val;
	}
}

