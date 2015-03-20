package io.github.nbdd0121.compiler.ir.function;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.type.Type;

import java.util.ArrayList;
import java.util.List;

public class Block extends Value {

	String label;
	Function function;
	ArrayList<Instruction> instructions = new ArrayList<>();

	public Block(Function func) {
		function = func;
		label = String.valueOf(function.getBlocks().size());
	}

	public void emit(Instruction ins) {
		instructions.add(ins);
	}

	@Override
	public Type getType() {
		throw new RuntimeException("block cannot be typed");
	}

	public String getLabel() {
		return label;
	}

	public List<Instruction> getInstructions() {
		return instructions;
	}

	public String toString() {
		return "." + label;
	}

	public void setLabel(String string) {
		label = string;
	}

	public String toCodeString(String ident) {
		StringBuilder sb = new StringBuilder();
		if (label.length() != 0)
			sb.append(ident).append(".").append(label).append(":\n");
		for (Instruction ins : instructions) {
			sb.append(ident + "  ").append(ins).append("\n");
		}
		return sb.toString();
	}

	public Instruction getLastInstruction() {
		if (instructions.isEmpty())
			return null;
		return instructions.get(instructions.size() - 1);
	}

	public boolean isLabelled() {
		return label.length() != 0;
	}

}
