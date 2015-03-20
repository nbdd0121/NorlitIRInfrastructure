package io.github.nbdd0121.compiler.ir.pass;

import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;

public abstract class BlockLocalPass extends FunctionPass {

	protected Function function;

	public abstract boolean pass(Block b);

	public boolean pass(Function function) {
		this.function = function;
		boolean ret = false;
		for (Block b : function.getBlocks()) {
			ret |= pass(b);
		}
		return ret;
	}

}
