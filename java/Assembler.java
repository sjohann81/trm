import java.io.*;
import java.util.*;

public class Assembler {

	private static final Map<String, Integer> codes = new HashMap<>();
	private static final Map<Integer, String> codes_inv = new HashMap<>();
	private static final Map<String, Integer> lookup = new HashMap<>();
	private static final Map<String, Integer> symtbl = new TreeMap<>();

	static {
		initialize_maps();
	}

	private static void initialize_maps() {
		codes.put("and", 0x0000); codes.put("or", 0x0400); codes.put("xor", 0x0600);
		codes.put("add", 0x0800); codes.put("sub", 0x0a00); codes.put("lsl", 0x3000);
		codes.put("lsr", 0x3400); codes.put("asr", 0x3600); codes.put("ldw", 0x4000);
		codes.put("ldb", 0x4400); codes.put("lbu", 0x4600); codes.put("stw", 0x4800);
		codes.put("stb", 0x4c00); codes.put("blt", 0x8000); codes.put("bge", 0x8200);
		codes.put("bbl", 0x8800); codes.put("bae", 0x8a00); codes.put("beq", 0x9000);
		codes.put("bne", 0x9200); codes.put("tlt", 0xa000); codes.put("tge", 0xa200);
		codes.put("tbl", 0xa800); codes.put("tae", 0xaa00); codes.put("teq", 0xb000);
		codes.put("tne", 0xb200); codes.put("hlt", 0xf800);

		for (Map.Entry<String, Integer> entry : codes.entrySet()) {
			codes_inv.put(entry.getValue(), entry.getKey());
		}

		lookup.put("r0", 0); lookup.put("r1", 1); lookup.put("r2", 2); lookup.put("r3", 3);
		lookup.put("r4", 4); lookup.put("r5", 5); lookup.put("r6", 6); lookup.put("r7", 7);
		lookup.put("r8", 8); lookup.put("r9", 9); lookup.put("r10", 10); lookup.put("r11", 11);
		lookup.put("r12", 12); lookup.put("r13", 13); lookup.put("r14", 14); lookup.put("r15", 15);

		lookup.put("zr", 0); lookup.put("a0", 1); lookup.put("a1", 2); lookup.put("a2", 3);
		lookup.put("a3", 4); lookup.put("v0", 5); lookup.put("v1", 6); lookup.put("v2", 7);
		lookup.put("v3", 8); lookup.put("v4", 9); lookup.put("v5", 10); lookup.put("v6", 11);
		lookup.put("v7", 12); lookup.put("v8", 13); lookup.put("fp", 13); lookup.put("sp", 14);
		lookup.put("lr", 15);
	}
	
	public String getcode(int key) {
		return codes_inv.get(key);
	}
	
	public List<String> getsymbols() {
		List <String> symbols = new ArrayList<>();
		
		for (Map.Entry<String, Integer> entry : symtbl.entrySet()) {
			symbols.add(String.format("%04x", entry.getValue()) + " " + entry.getKey());
		}
						
		return symbols;
	}

	private static String tohex(int n) {
		return String.format("%04x", n & 0xffff);
	}

	private static int getval(String s) {
		if (s == null || s.isEmpty()) return 0;
		if (lookup.containsKey(s)) {
			return lookup.get(s);
		}
		try {
			return Integer.decode(s);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("invalid symbol: " + s);
		}
	}

	public static void pass1(List<String> program) {
		for (int i = 0; i < program.size(); i++) {
			String line = program.get(i).trim();
			if (line.isEmpty() || line.startsWith(";")) {
				program.set(i, "");
				continue;
			}
			String[] fields = line.split("\\s+");
			if (fields.length > 0) {
				if (fields[0].equals("nop")) {
					program.set(i, "\tand\tr0,r0");
				} else if (fields[0].equals("hlt")) {
					program.set(i, "\thlt\tr0,r0");
				}
			}
		}
	}
	
	public static void pass2(List<String> program) {
		int pc = 0;
		for (String line : program) {
			String trimmed_line = line.trim();
			if (trimmed_line.isEmpty()) continue;

			String instruction_part = line;
			if (!Character.isWhitespace(line.charAt(0))) {
				String[] fields = trimmed_line.split("\\s+", 2);
				lookup.put(fields[0], pc);
				symtbl.put(fields[0], pc);
				instruction_part = (fields.length > 1) ? fields[1] : "";
			}

			instruction_part = instruction_part.trim();
			if (instruction_part.isEmpty()) continue;

			String[] fields = instruction_part.split("\\s+");
			
			if (codes.containsKey(fields[0])) {
				String all_operands = String.join("", Arrays.copyOfRange(fields, 1, fields.length));
				String[] parts_comment = all_operands.split(";");
				String[] parts = parts_comment[0].split(",");
				pc += (parts.length == 3) ? 4 : 2;
			} else {
				int quote_start = instruction_part.indexOf('"');
				int quote_end = instruction_part.lastIndexOf('"');
				int bracket_start = instruction_part.indexOf('[');
				int bracket_end = instruction_part.lastIndexOf(']');

				if (quote_start != -1 && quote_end > quote_start) {
					String content = instruction_part.substring(quote_start + 1, quote_end);
					content = content.replace("\\t", "\t");
					content = content.replace("\\n", "\n");
					content = content.replace("\\r", "\r");
					int len = content.length() + 1;
					if (len % 2 != 0)
						len++;
					pc += len;
				} else if (bracket_start != -1 && bracket_end > bracket_start) {
					String content = instruction_part.substring(bracket_start + 1, bracket_end);
					int len = Integer.decode(content);
					if (len % 2 != 0)
						len++;
					pc += len;
				} else {
					int dollar_start = instruction_part.indexOf('$');
					
					if (dollar_start != -1) {
						pc += fields.length;
						if (fields.length % 2 != 0) pc++;
					} else {
						pc += fields.length * 2;
					}
				}
			}
		}
	}

	private static long assemble(String[] fields) {
		String op = fields[0];
		Integer op_val = codes.get(op);

		if (op_val == null)
			return getval(op);
		
		String all_operands = String.join("", Arrays.copyOfRange(fields, 1, fields.length));
		String[] parts_comment = all_operands.split(";");
		String[] parts = parts_comment[0].split(",");
		
		if (parts.length == 2) {
			return op_val | (getval(parts[0]) << 4) | (getval(parts[1]) & 0xf);
		} else if (parts.length == 3) {
			long instruction_word = op_val | 0x0100 | (getval(parts[0]) << 4) | (getval(parts[1]) & 0xf);
			return (instruction_word << 16) | (getval(parts[2]) & 0xffff);
		}
		
		throw new IllegalArgumentException("incorrect number of operands: " + parts.length);
	}
	
	public static int pass3(List<String> program, List<String> output) {
		int pc = 0;
		int line_num = 0;

		for (String line : program) {
			line_num++;
			String trimmed_line = line.trim();
			if (trimmed_line.isEmpty()) continue;

			String instruction_part = line;
			if (!Character.isWhitespace(line.charAt(0))) {
				String[] fields = trimmed_line.split("\\s+", 2);
				instruction_part = (fields.length > 1) ? fields[1] : "";
			}
			
			instruction_part = instruction_part.trim();
			if (instruction_part.isEmpty()) continue;
			
			try {
				String[] fields = instruction_part.split("\\s+");

				if (codes.containsKey(fields[0])) {
					long assembled_code = assemble(fields);
					
					if (assembled_code > 0xffff) {
						int inst_word = (int)(assembled_code >>> 16);
						int imm_word = (int)(assembled_code & 0xffff);
						output.add(String.format("%s %s    (%s r%d,r%d,%d)",
								tohex(pc), tohex(inst_word), codes_inv.get(inst_word & 0xfe00),
								(inst_word & 0xf0) >> 4, (inst_word & 0xf), imm_word));
						pc += 2;
						output.add(String.format("%s %s", tohex(pc), tohex(imm_word)));
						pc += 2;
					} else {
						int instruction = (int)assembled_code;
						output.add(String.format("%s %s    (%s r%d,r%d)",
								tohex(pc), tohex(instruction), codes_inv.get(instruction & 0xfe00),
								(instruction & 0xf0) >> 4, (instruction & 0xf)));
						
						pc += 2;
					}
				} else {
					int quote_start = instruction_part.indexOf('"');
					int quote_end = instruction_part.lastIndexOf('"');
					int bracket_start = instruction_part.indexOf('[');
					int bracket_end = instruction_part.lastIndexOf(']');

					if (quote_start != -1 && quote_end > quote_start) {
						String content = instruction_part.substring(quote_start + 1, quote_end);
						content = content.replace("\\t", "\t");
						content = content.replace("\\n", "\n");
						content = content.replace("\\r", "\r");
						content = content + "\0";
						if (content.length() % 2 != 0)
							content = content + "\0";
						for (int i = 0; i < content.length(); i += 2) {
							int word = (content.charAt(i) << 8) | content.charAt(i + 1);
							output.add(String.format("%s %s", tohex(pc), tohex(word)));
							pc += 2;
						}
					} else if (bracket_start != -1 && bracket_end > bracket_start) {
						String content = instruction_part.substring(bracket_start + 1, bracket_end);
						int len = Integer.decode(content);
						if (len % 2 != 0)
							len++;
						for (int i = 0; i < len; i += 2) {
							output.add(String.format("%s 0000", tohex(pc)));
							pc += 2;
						}
					} else {
						int dollar_start = instruction_part.indexOf('$');
					
						if (dollar_start != -1) {
							ArrayList<String> fields_ar = new ArrayList<String>();
							for (String data_field : fields)
								fields_ar.add(data_field);
							if (fields.length % 2 != 0)
								fields_ar.add("$0");
							for (int f = 0; f < fields_ar.size(); f += 2) {
								String data_field1 = fields_ar.get(f).replace("$", "");
								String data_field2 = fields_ar.get(f + 1).replace("$", "");
								long data = (Integer.decode(data_field1) << 8) | Integer.decode(data_field2);
								output.add(String.format("%s %s", tohex(pc), tohex((int)data)));
								pc += 2;
							}
						} else {
							for (String data_field : fields) {
								long data = assemble(new String[]{data_field});
								output.add(String.format("%s %s", tohex(pc), tohex((int)data)));
								pc += 2;
							}
						}
					}
				}
			} catch (Exception e) {
				output.add(String.format("**** ????    assemble failed (%s)", e.getMessage()));
				output.add(String.format("             line %d --> \"%s\"", line_num, line.replace("\n", "")));
				return -1;
			}
		}
		return 0;
	}
}
