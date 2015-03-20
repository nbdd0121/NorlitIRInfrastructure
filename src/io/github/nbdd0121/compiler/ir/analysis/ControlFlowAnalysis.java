package io.github.nbdd0121.compiler.ir.analysis;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.annotation.ControlFlowAnnotation;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;

import java.util.List;

public class ControlFlowAnalysis {

	public static ControlFlowAnnotation getAnnotation(Block b) {
		ControlFlowAnnotation anno = b
				.getAnnotation(ControlFlowAnnotation.class);
		if (anno == null) {
			anno = new ControlFlowAnnotation();
			b.addAnnotation(anno);
		}
		return anno;
	}

	private static void addSuccessor(Block block, Block successor) {
		ControlFlowAnnotation ba = getAnnotation(block);
		ControlFlowAnnotation sa = getAnnotation(successor);
		ba.successor.add(successor);
		sa.predecessor.add(block);
	}

	private static void analyzeBlock(Block b, Block through) {
		Instruction last = b.getLastInstruction();
		if (last == null) {
			addSuccessor(b, through);
			return;
		}
		switch (last.type) {
			case RETURN:
				break;
			case JMP:
				addSuccessor(b, (Block) last.op[0]);
				break;
			case COND_JMP:
				addSuccessor(b, (Block) last.op[1]);
				addSuccessor(b, (Block) last.op[2]);
				break;
			default:
				addSuccessor(b, through);
				break;
		}
	}

	public static void annotate(Function func) {
		List<Block> blocks = func.getBlocks();
		for (int i = 0; i < blocks.size() - 1; i++) {
			analyzeBlock(blocks.get(i), blocks.get(i + 1));
		}
		analyzeBlock(blocks.get(blocks.size() - 1), null);
	}

}
