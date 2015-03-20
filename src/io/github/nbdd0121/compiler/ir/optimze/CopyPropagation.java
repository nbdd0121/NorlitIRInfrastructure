package io.github.nbdd0121.compiler.ir.optimze;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Instruction.Opcode;
import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;
import io.github.nbdd0121.compiler.ir.function.Local;
import io.github.nbdd0121.compiler.ir.pass.FunctionPass;

import java.util.HashMap;
import java.util.List;

public class CopyPropagation extends FunctionPass {

	HashMap<Local, Value> replacementTable = new HashMap<>();

	public boolean optimize(Block block) {
		boolean optimized = false;
		List<Instruction> insts = block.getInstructions();
		for (int i = 0; i < insts.size(); i++) {
			Instruction ins = insts.get(i);
			if (ins.type == Opcode.ASSIGN) {
				insts.set(i, Instruction.nop());
				optimized = true;
				if (replacementTable.containsKey(ins.op[0])) {
					replacementTable.put(ins.dest,
							replacementTable.get(ins.op[0]));
				} else {
					replacementTable.put(ins.dest, ins.op[0]);
				}
			} else {
				for (int j = 0; j < ins.op.length; j++) {
					if (replacementTable.containsKey(ins.op[j])) {
						Value replace = replacementTable.get(ins.op[j]);
						System.out.println("Replace " + ins.op[j] + " to "
								+ replace);
						ins.op[j] = replace;
					}
				}
			}
		}
		return optimized;
	}

	public void bind(Local dest, Value src) {
		replacementTable.put(dest, src);
	}

	public boolean pass(Function function) {
		boolean ret = false;
		for (Block b : function.getBlocks()) {
			ret |= optimize(b);
		}
		replacementTable.clear();
		return ret;
	}

}
