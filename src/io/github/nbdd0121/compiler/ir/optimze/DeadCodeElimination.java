package io.github.nbdd0121.compiler.ir.optimze;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Instruction.Opcode;
import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;
import io.github.nbdd0121.compiler.ir.function.Local;
import io.github.nbdd0121.compiler.ir.pass.FunctionPass;

import java.util.HashSet;
import java.util.List;

public class DeadCodeElimination extends FunctionPass {

	HashSet<Local> waiting = new HashSet<>();
	HashSet<Local> dealed = new HashSet<>();

	private boolean markRoot(Block block) {
		boolean modified = false;
		for (Instruction i : block.getInstructions()) {
			if (i.dest == null || hasSideEffect(i.type)) {
				for (Value v : i.op) {
					if (v instanceof Local) {
						if (!dealed.contains(v)) {
							waiting.add((Local) v);
							modified = true;
						}
					}
				}
			}
		}
		return modified;
	}

	private boolean incrementMark(Block block) {
		boolean modified = false;
		for (Instruction i : block.getInstructions()) {
			if (i.dest == null)
				continue;
			if (dealed.contains(i.dest) || !waiting.contains(i.dest))
				continue;
			dealed.add(i.dest);
			waiting.remove(i.dest);
			for (Value v : i.op) {
				if (v instanceof Local) {
					if (!dealed.contains(v)) {
						waiting.add((Local) v);
						modified = true;
					}
				}
			}
		}
		return modified;
	}

	private void sweep(Block block) {
		List<Instruction> insts = block.getInstructions();
		for (int i = 0; i < insts.size(); i++) {
			Instruction in = insts.get(i);
			if (in.dest != null && !dealed.contains(in.dest)) {
				insts.set(i, Instruction.nop());
				System.out.println("Optimize out dead code " + in);
			}
		}
	}

	public boolean pass(Function func) {
		boolean modified = false;
		for (Block b : func.getBlocks()) {
			modified |= markRoot(b);
		}
		while (modified) {
			modified = false;
			for (Block b : func.getBlocks()) {
				modified |= incrementMark(b);
			}
		}
		for (Block b : func.getBlocks()) {
			sweep(b);
		}
		return false;
	}

	private boolean hasSideEffect(Opcode type) {
		switch (type) {
			case CALL:
				return true;
			default:
				return false;
		}
	}
}
