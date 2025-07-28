import java.io.*;
import java.util.*;

class OutputDev extends UserOutput {
	public void putInt(int val) {}
	public void putHex(int val) {}
	public void putChar(char val) {}
	public void putString(String val) {
		System.out.print(val);
	}
}

class InputDev extends UserInput {
	private String input = "";
	private Scanner scan = new Scanner(System.in);
	private char data1, data2;
	private int retval;
	
	public int getInt() {
		input = scan.nextLine();
		retval = Integer.decode(input);
		return retval;
	}
	public int getHex() {
		input = scan.nextLine();
		retval = Integer.parseInt(input, 16);
		return retval;
	}
	public char getChar() {
		input = scan.nextLine();
		retval = Integer.parseInt(String.valueOf(input.charAt(0)));
		return (char)(retval & 0xff);
	}
	public void getString(int[] memory, int addr) {
		addr >>= 1;
		input = scan.nextLine() + "\0\0";
		int i = 0;
		while (true) {
			data1 = (char)(input.charAt(i));
			data2 = (char)(input.charAt(i + 1));
			memory[addr] = (data1 << 8) | data2;
			if (data1 == 0 || data2 == 0) break;
			addr++;
			i += 2;
		}
	}
}

public class Trm {
	private static void printexec(Assembler asm, Simulator sim, int inst, int[] context) {
		if ((inst & 0x0100) != 0) {
			System.out.printf("pc: %04x, instruction: %s r%d,r%d,%d\n",
			sim.getpc(), asm.getcode(inst & 0xfe00), (inst & 0x00f0) >> 4,
			(inst & 0x000f), sim.getinstdata());
		} else {
			System.out.printf("pc: %04x, instruction: %s r%d,r%d\n",
			sim.getpc(), asm.getcode(inst & 0xfe00), (inst & 0x00f0) >> 4,
			(inst & 0x000f));
		}
			
		System.out.printf(" r0: [%04x]  r1: [%04x]  r2: [%04x]  r3: [%04x]\n",
		context[0], context[1], context[2], context[3]);
		System.out.printf(" r4: [%04x]  r5: [%04x]  r6: [%04x]  r7: [%04x]\n",
		context[4], context[5], context[6], context[7]);
		System.out.printf(" r8: [%04x]  r9: [%04x] r10: [%04x] r11: [%04x]\n",
		context[8], context[9], context[10], context[11]);
		System.out.printf("r12: [%04x] r13: [%04x] r14: [%04x] r15: [%04x]\n",
		context[12], context[13], context[14], context[15]);
	}
	
	private static void printdump(Simulator sim, int addr, int len) {
		int[] memory = sim.getmemory();
		
		addr = (addr >> 1) & ~0xf;
		len = (len >> 1);
		
		for (int k = addr; k < addr + len + 8 && k < memory.length; k += 8) {
			System.out.printf("%04x  ", k << 1);
			
			for (int l = 0; l < 8; l++) {
				System.out.printf("%02x %02x ",
				memory[k + l] >> 8, memory[k + l] & 0xff);
			}
			System.out.printf("\n");
		}
		
	}
	
	private static void debug(Assembler asm, Simulator sim, List <String> obj_code) {
		boolean go = true;
		int baddr = -1;
		int run = 0;
		int inst;
		int[] context;
		String input = "";
		String input2 = "";
		Scanner scan = new Scanner(System.in);
		int cycles = 0;
						
		while (true) {
			if (sim.getpc() == baddr)
				run = 0;
			
			inst = sim.getinst();
			context = sim.getcontext();
			
			if (run == 0) {
				printexec(asm, sim, inst, context);
				System.out.println("q: quit; r: run; b: breakpoint; t: reset program; ENTER: next;");
				System.out.println("l: program list; s: symbol table; d: mem dump");
			}
			
			if (run == 0)
				input = scan.nextLine();
				
			if (input.equals("q"))
				break;
			
			if (input.equals("r"))
				run = 1;
				
			if (input.equals("b")) {
				System.out.printf("Address: ");
				input = scan.nextLine();
				baddr = Integer.decode(input);
				continue;
			}
			
			if (input.equals("t")) {
				cycles = 0;
				sim.load(obj_code);
				go = true;
				continue;
			}
			
			if (input.equals("l")) {
				System.out.println("Program list:");
				for (String line_out : obj_code) {
					System.out.println(line_out);
				}
				continue;
			}
			
			if (input.equals("s")) {
				List<String> symbols;
				System.out.println("Symbol table:");
				symbols = asm.getsymbols();
				for (String sym : symbols) {
					System.out.println(sym);
				}
				continue;
			}

			if (input.equals("d")) {
				System.out.printf("Address: ");
				input = scan.nextLine();
				int addr = Integer.decode(input);
				System.out.printf("Length: ");
				input2 = scan.nextLine();
				int len = Integer.decode(input2);
				printdump(sim, addr, len);
				continue;
			}
			
			go = sim.step();
			
			if (go) {
				cycles++;
			} else {
				System.out.printf("%d cycles\n", cycles);
				run = 0;
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		List<String> program = new ArrayList<>();
		List<String> obj_code = new ArrayList<>();
		UserOutput out = new OutputDev();
		UserInput in = new InputDev();
		Assembler asm = new Assembler();
		Simulator sim = new Simulator(in, out);
		BufferedReader reader;
		BufferedWriter writer;
		String line;
		
		if (args.length >= 2) {
			reader = new BufferedReader(new FileReader(args[1]));
		} else {
			System.out.println("Syntax: java -jar Trm.jar <mode> <source> <output>");
			System.out.println("<mode>   - a (assemble), s (simulation), r (run), d (debug)");
			System.out.println("<source> - assembly (.s) source code for 'a', 'r' and 'd' modes,");
			System.out.println("           object code (.trm) for 's' mode");
			System.out.println("<output> - object code (.trm) for 'a' mode");
			return;
		}

		if (args[0].equals("a") || args[0].equals("r") || args[0].equals("d")) {
			while ((line = reader.readLine()) != null) {
				program.add(line);
			}
			
			asm.pass1(program);
			asm.pass2(program);
			asm.pass3(program, obj_code);

			if (args.length >= 3) {
				writer = new BufferedWriter(new FileWriter(args[2]));
				for (String line_out : obj_code) {
					writer.write(line_out);
					writer.newLine();
				}
				writer.close();
			} else {
				if (!args[0].equals("d")) {
					for (String line_out : obj_code) {
						System.out.println(line_out);
					}
				}
			}
		} else {
			while ((line = reader.readLine()) != null) {
				obj_code.add(line);
			}
		}
		reader.close();

		if (args[0].equals("s") || args[0].equals("r") || args[0].equals("d")) {
			if (sim.check(obj_code)) {
				System.out.println("[program has errors]");
			} else {
				int val = sim.load(obj_code);
				if (val == 0) {
					if (args[0].equals("d")) {
						debug(asm, sim, obj_code);
					} else {
						sim.run();
					}
				}
			}
		}
	}
}
