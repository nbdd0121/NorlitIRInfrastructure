package io.github.nbdd0121.compiler.ir.analysis;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.annotation.LifetimeAnnotation;
import io.github.nbdd0121.compiler.ir.annotation.LiveVariableAnnotation;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;
import io.github.nbdd0121.compiler.ir.function.Local;

import java.util.HashMap;
import java.util.Map.Entry;

public class LifetimeAnalysis {

	private static HashMap<Local, LiveVariableAnnotation> defInfo = new HashMap<>();

	public static LifetimeAnnotation getAnnotation(Local local) {
		LifetimeAnnotation anno = local.getAnnotation(LifetimeAnnotation.class);
		if (anno == null) {
			anno = new LifetimeAnnotation();
			local.addAnnotation(anno);
		}
		return anno;
	}

	public static LiveVariableAnnotation getAnnotation(Block b) {
		LiveVariableAnnotation anno = b
				.getAnnotation(LiveVariableAnnotation.class);
		if (anno == null) {
			anno = new LiveVariableAnnotation();
			b.addAnnotation(anno);
		}
		return anno;
	}

	private static void scanDef(Block b) {
		for (Instruction ins : b.getInstructions()) {
			if (ins.dest != null) {
				defInfo.put(ins.dest, getAnnotation(b));
			}
		}
	}

	private static void scanUse(Block b) {
		LiveVariableAnnotation thisAnnotation = getAnnotation(b);
		for (Instruction ins : b.getInstructions()) {
			for (Value v : ins.op) {
				if (v instanceof Local) {
					LiveVariableAnnotation blk = defInfo.get(v);
					if (blk != thisAnnotation) {
						blk.export.add((Local) v);
					} else {
						getAnnotation((Local) v).internalRef++;
					}
				}
			}
		}
	}

	public static void annotate(Function func) {
		/* Already annotated */
		if (func.getBlocks().get(0).getAnnotation(LiveVariableAnnotation.class) != null)
			return;
		for (Local b : func.getParameters()) {
			defInfo.put(b, getAnnotation(func.getBlocks().get(0)));
		}
		for (Block b : func.getBlocks()) {
			scanDef(b);
		}
		for (Block b : func.getBlocks()) {
			scanUse(b);
		}
		for (Entry<Local, LiveVariableAnnotation> entry : defInfo.entrySet()) {
			if (!entry.getValue().export.contains(entry.getKey())) {
				entry.getValue().internal.add(entry.getKey());
			}
		}
		defInfo.clear();
	}

}
