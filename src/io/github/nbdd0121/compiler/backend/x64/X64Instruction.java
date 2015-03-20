package io.github.nbdd0121.compiler.backend.x64;

import com.nwgjb.commons.StringUtil;

public class X64Instruction {

	String opcode;
	X64Location operands[];

	public X64Instruction(String op, X64Location... o) {
		opcode = op;
		operands = o;
	}

	public String toString() {
		return opcode + "\t" + StringUtil.join(operands, ", ");
	}

}
