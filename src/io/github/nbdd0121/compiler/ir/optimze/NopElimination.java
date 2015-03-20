package io.github.nbdd0121.compiler.ir.optimze;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Instruction.Opcode;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.pass.BlockLocalPass;

import java.util.List;

public class NopElimination extends BlockLocalPass {
	
	public boolean pass(Block block) {
		List<Instruction> insts = block.getInstructions();
		boolean ret = false;
		for (int i = 0; i < insts.size(); i++) {
			Instruction ins = insts.get(i);
			if (ins.type == Opcode.NOP) {
				insts.remove(i);
				i--;
				ret = true;
			}
		}
		return ret;
	}


}
