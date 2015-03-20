package io.github.nbdd0121.compiler.ir.optimze;

import io.github.nbdd0121.compiler.ir.analysis.ControlFlowAnalysis;
import io.github.nbdd0121.compiler.ir.annotation.ControlFlowAnnotation;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;
import io.github.nbdd0121.compiler.ir.pass.FunctionPass;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Remove dead code where control flow will never reaches.
 * 
 * @author Gary
 */
public class BlockGarbageCollection extends FunctionPass {

	@Override
	public boolean pass(Function function) {
		ControlFlowAnalysis.annotate(function);
		HashSet<Block> referencedBlock = new HashSet<>();
		LinkedList<Block> dealingList = new LinkedList<>();
		dealingList.add(function.getBlocks().get(0));
		while (!dealingList.isEmpty()) {
			Block b = dealingList.removeFirst();
			referencedBlock.add(b);
			ControlFlowAnnotation anno = ControlFlowAnalysis.getAnnotation(b);
			for (Block n : anno.successor) {
				if (!referencedBlock.contains(n)) {
					dealingList.add(n);
				}
			}
		}
		function.getBlocks().removeIf(b -> !referencedBlock.contains(b));
		return false;
	}

}
