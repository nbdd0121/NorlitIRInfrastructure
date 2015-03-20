package io.github.nbdd0121.compiler.ir.optimze;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;
import io.github.nbdd0121.compiler.ir.function.Local;
import io.github.nbdd0121.compiler.ir.pass.FunctionPass;

import java.util.HashMap;

public class LocalVarRenaming extends FunctionPass {
	
	public boolean pass(Function function) {
		HashMap<String, Local> locals = function.getLocalMap();
		locals.clear();
		for (Block b : function.getBlocks()) {
			for (Instruction i : b.getInstructions()) {
				if (i.dest != null) {
					if (Character.isDigit(i.dest.name.charAt(0))) {
						String newName = String.valueOf(locals.size());
						if (!i.dest.name.equals(newName)) {
							System.out.println("Renaming %" + i.dest.name
									+ " to %" + newName);
							i.dest.name = newName;
						}
					}
					locals.put(i.dest.name, i.dest);
				}
			}
		}
		return false;
	}

}
