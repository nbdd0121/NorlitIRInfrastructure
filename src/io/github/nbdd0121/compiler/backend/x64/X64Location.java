package io.github.nbdd0121.compiler.backend.x64;

public class X64Location {
	public static X64Location RAX = new X64Location(X64Location.Type.REGISTER,
			"rax", 8);
	public static X64Location RCX = new X64Location(X64Location.Type.REGISTER,
			"rcx", 8);
	public static X64Location RDX = new X64Location(X64Location.Type.REGISTER,
			"rdx", 8);
	public static X64Location R8 = new X64Location(X64Location.Type.REGISTER,
			"r8", 8);
	public static X64Location R9 = new X64Location(X64Location.Type.REGISTER,
			"r9", 8);
	public static X64Location R10 = new X64Location(X64Location.Type.REGISTER,
			"r10", 8);
	public static X64Location R11 = new X64Location(X64Location.Type.REGISTER,
			"r11", 8);
	public static X64Location RBP = new X64Location(X64Location.Type.REGISTER,
			"rbp", 8);
	public static X64Location RSP = new X64Location(X64Location.Type.REGISTER,
			"rsp", 8);

	static String[][] regNames = { { "rax", "eax", "ax", "al" },
			{ "rcx", "ecx", "cx", "cl" }, { "rdx", "edx", "dx", "dl" },
			{ "rbx", "ebx", "bx", "bl" }, { "rsi", "esi", "si", "sil" },
			{ "rdi", "edi", "di", "dil" }, { "rbp", "ebp", "bp", "bpl" },
			{ "rsp", "esp", "sp", "spl" }, { "r8", "r8d", "r8w", "r8b" },
			{ "r9", "r9d", "r9w", "r9b" }, { "r10", "r10d", "r10w", "r10b" },
			{ "r11", "r11d", "r11w", "r11b" },
			{ "r12", "r12d", "r12w", "r12b" },
			{ "r13", "r13d", "r13w", "r13b" },
			{ "r14", "r14d", "r14w", "r14b" },
			{ "r15", "r15d", "r15w", "r15b" } };

	private static int getRegisterIndex(String reg) {
		switch (reg) {
			case "rax":
				return 0;
			case "rcx":
				return 1;
			case "rdx":
				return 2;
			case "rbx":
				return 3;
			case "rsi":
				return 4;
			case "rdi":
				return 5;
			case "rbp":
				return 6;
			case "rsp":
				return 7;
			case "r8":
				return 8;
			case "r9":
				return 9;
			case "r10":
				return 10;
			case "r11":
				return 11;
			case "r12":
				return 12;
			case "r13":
				return 13;
			case "r14":
				return 14;
			case "r15":
				return 15;
			default:
				throw new AssertionError("Unexpected register " + reg);
		}
	}

	private static int getSizeIndex(int size) {
		switch (size) {
			case 1:
				return 3;
			case 2:
				return 2;
			case 4:
				return 1;
			case 8:
				return 0;
			default:
				throw new AssertionError("Unexpected size " + size);
		}
	}

	public static String getRegisterString(String reg, int size) {
		return regNames[getRegisterIndex(reg)][getSizeIndex(size)];
	}

	enum Type {
		MEMORY, REGISTER, IMMEDIATE
	}

	Type type;
	String str;
	int size;

	public X64Location(Type type, String str, int size) {
		this.type = type;
		this.str = str;
		this.size = size;
	}

	public static X64Location getMemoryLocation(String str, int size) {
		return new X64Location(Type.MEMORY, str, size);
	}

	public static X64Location newImm(String str) {
		return new X64Location(Type.IMMEDIATE, str, 8);
	}

	public static X64Location newImm(int val) {
		return new X64Location(Type.IMMEDIATE, String.valueOf(val), 8);
	}

	public String toString() {
		if (isMemory()) {
			switch (size) {
				case 1:
					return "byte" + str;
				case 2:
					return "word" + str;
				case 4:
					return "dword" + str;
				case 8:
					return "qword" + str;
				default:
					throw new AssertionError();
			}
		} else if (isRegister()) {
			if (size == 8) {
				return str;
			}
			return getRegisterString(str, size);
		}
		return str;
	}

	public int getSize() {
		return size;
	}

	public boolean isRegister() {
		return type == Type.REGISTER;
	}

	public boolean isMemory() {
		return type == Type.MEMORY;
	}

	public X64Location getSubLocation(int size) {
		if (size == this.size) {
			return this;
		}
		return new X64Location(type, str, size);
	}

	public boolean isImmediate() {
		return type == Type.IMMEDIATE;
	}
}
