package io.github.nbdd0121.compiler.ir.analysis;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.annotation.AliasAnnotation;
import io.github.nbdd0121.compiler.ir.annotation.AliasAnnotation.Location;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;
import io.github.nbdd0121.compiler.ir.function.Local;
import io.github.nbdd0121.compiler.ir.type.PointerType;
import io.github.nbdd0121.compiler.ir.type.Type;

public class AliasAnalysis {

	// private static HashMap<Local, > defInfo = new HashMap<>();

	public static AliasAnnotation getAnnotation(Local local) {
		AliasAnnotation anno = local.getAnnotation(AliasAnnotation.class);
		if (anno == null) {
			anno = new AliasAnnotation();
			local.addAnnotation(anno);
		}
		return anno;
	}

	private static Location getLocation(Value val) {
		if (val instanceof Local) {
			return getAnnotation((Local) val).location;
		} else {
			throw new UnsupportedOperationException(val.toString());
		}
	}

	private static void scanDef(Block b) {
		for (Instruction ins : b.getInstructions()) {
			if (ins.dest != null && ins.dest.getType().isPointer()) {
				PointerType type = (PointerType) ins.dest.getType();
				Type refer = type.getRefer();
				switch (ins.type) {
					case ALLOCA: {
						AliasAnnotation anno = getAnnotation(ins.dest);
						anno.location = Location.alloca(refer.getByteSize());
						break;
					}
					case LOAD: {
						AliasAnnotation anno = getAnnotation(ins.dest);
						anno.location = Location.anywhere(refer.getByteSize());
						break;
					}
					case ASSIGN: {
						AliasAnnotation anno = getAnnotation(ins.dest);
						anno.location = getLocation(ins.op[0]);
						break;
					}
					// TODO
					case GETELEMENTPTR: {
						AliasAnnotation anno = getAnnotation(ins.dest);
						anno.location = Location.anywhere(refer.getByteSize());
						break;
					}
					case ADD:
					case SUB:
						throw new AssertionError();
					default:
						throw new UnsupportedOperationException("Analyzing "
								+ ins);
				}
			}
		}
	}

	// private static void scanUse(Block b) {
	// LiveVariableAnnotation thisAnnotation = getAnnotation(b);
	// for (Instruction ins : b.getInstructions()) {
	// for (Value v : ins.op) {
	// if (v instanceof Local) {
	// LiveVariableAnnotation blk = defInfo.get(v);
	// if (blk != thisAnnotation) {
	// blk.export.add((Local) v);
	// } else {
	// getAnnotation((Local) v).internalRef++;
	// }
	// }
	// }
	// }
	// }

	public static void annotate(Function func) {
		ControlFlowAnalysis.annotate(func);
		LifetimeAnalysis.annotate(func);
		for (Local b : func.getParameters()) {
			if (b.getType().isPointer()) {
				AliasAnnotation anno = getAnnotation(b);
				PointerType type = (PointerType) b.getType();
				anno.location = Location
						.anywhere(type.getRefer().getByteSize());
			}
		}
		for (Block b : func.getBlocks()) {
			scanDef(b);
		}
	}

}
