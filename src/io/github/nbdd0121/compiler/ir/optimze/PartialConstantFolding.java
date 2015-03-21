package io.github.nbdd0121.compiler.ir.optimze;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Instruction.Opcode;
import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.constant.Constant;
import io.github.nbdd0121.compiler.ir.constant.IntegerConstant;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Local;
import io.github.nbdd0121.compiler.ir.pass.BlockLocalPass;
import io.github.nbdd0121.compiler.ir.type.IntegerType;

import java.util.HashMap;
import java.util.List;

public class PartialConstantFolding extends BlockLocalPass {

	HashMap<Local, Instruction> declaration = new HashMap<>();

	public boolean partialConstantForm(Instruction ins) {
		if (ins.op[0] instanceof Local && ins.op[1] instanceof Constant) {
			return true;
		} else {
			return false;
		}
	}

	public void normalizeInstruction(Instruction ins) {
		switch (ins.type) {
			case ADD: {
				if (ins.op[0] instanceof Constant && ins.op[1] instanceof Local) {
					Value temp = ins.op[0];
					ins.op[0] = ins.op[1];
					ins.op[1] = temp;
				}
			}
			default:
				break;
		}
	}

	public boolean pass(Block block) {
		boolean ret = false;
		declaration.clear();
		List<Instruction> insts = block.getInstructions();
		for (int i = 0; i < insts.size(); i++) {
			Instruction ins = insts.get(i);
			switch (ins.type) {
				case ADD: {
					normalizeInstruction(ins);
					if (partialConstantForm(ins)) {
						if (declaration.containsKey(ins.op[0])) {
							Instruction instruction = declaration
									.get(ins.op[0]);
							if (instruction.type == Opcode.ADD) {
								System.out
										.print("Partial constant folding from "
												+ ins);
								ins.op[0] = instruction.op[0];
								ins.op[1] = new IntegerConstant(
										(IntegerType) ins.op[1].getType(),
										((IntegerConstant) instruction.op[1])
												.getValue()
												.add(((IntegerConstant) ins.op[1])
														.getValue()));
								System.out.println(" to " + ins);
							}
						}
						declaration.put(ins.dest, ins);
					}
					break;
				}
				case SUB: {
					if (partialConstantForm(ins)) {
						if (declaration.containsKey(ins.op[0])) {
							Instruction instruction = declaration
									.get(ins.op[0]);
							if (instruction.type == Opcode.SUB) {
								System.out
										.print("Partial constant folding from "
												+ ins);
								ins.op[0] = instruction.op[0];
								ins.op[1] = new IntegerConstant(
										(IntegerType) ins.op[1].getType(),
										((IntegerConstant) instruction.op[1])
												.getValue()
												.add(((IntegerConstant) ins.op[1])
														.getValue()));
								System.out.println(" to " + ins);
							}
						}
						declaration.put(ins.dest, ins);
					}
					break;
				}
				case INDEX: {
					if (partialConstantForm(ins)) {
						if (declaration.containsKey(ins.op[0])) {
							Instruction instruction = declaration
									.get(ins.op[0]);
							if (instruction.type == Opcode.INDEX) {
								System.out
										.print("Partial constant folding from "
												+ ins);
								ins.op[0] = instruction.op[0];
								ins.op[1] = new IntegerConstant(
										(IntegerType) ins.op[1].getType(),
										((IntegerConstant) instruction.op[1])
												.getValue()
												.add(((IntegerConstant) ins.op[1])
														.getValue()));
								System.out.println(" to " + ins);
							}
						}
						declaration.put(ins.dest, ins);
					}
					break;
				}
				default:
					break;
			}
		}
		declaration.clear();
		return ret;
	}

}
