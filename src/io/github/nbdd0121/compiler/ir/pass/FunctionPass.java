package io.github.nbdd0121.compiler.ir.pass;

import io.github.nbdd0121.compiler.ir.Declaration;
import io.github.nbdd0121.compiler.ir.Module;
import io.github.nbdd0121.compiler.ir.function.Function;

public abstract class FunctionPass {

	public abstract boolean pass(Function b);

	public boolean pass(Module mod) {
		boolean ret = false;
		for (Declaration b : mod.getDeclarations()) {
			if (b instanceof Function)
				ret |= pass((Function) b);
		}
		return ret;
	}

}
