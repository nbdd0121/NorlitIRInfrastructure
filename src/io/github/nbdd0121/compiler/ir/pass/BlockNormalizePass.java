package io.github.nbdd0121.compiler.ir.pass;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;

import java.util.List;

/**
 * Normalize block. By normalizing a block, all blocks will end with a branch
 * instruction. No block can penetrate through to its next adjacent block.
 * 
 * @author Gary
 */
public class BlockNormalizePass extends FunctionPass {

	void pass(Block block, Block next) {
		Instruction ins = block.getLastInstruction();
		if (ins != null) {
			switch (ins.type) {
				case COND_JMP:
				case JMP:
				case RETURN:
					return;
				default:
					break;
			}
		}
		ins = Instruction.jmp();
		ins.op[0] = next;
		block.emit(ins);
	}

	public boolean pass(Function function) {
		List<Block> blocks = function.getBlocks();
		for (int i = 0; i < blocks.size(); i++) {
			if (i == blocks.size() - 1) {
				pass(blocks.get(i), null);
			} else {
				pass(blocks.get(i), blocks.get(i + 1));
			}
		}
		return false;
	}

}
