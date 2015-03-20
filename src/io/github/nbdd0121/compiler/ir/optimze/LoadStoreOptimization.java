package io.github.nbdd0121.compiler.ir.optimze;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.analysis.AliasAnalysis;
import io.github.nbdd0121.compiler.ir.annotation.AliasAnnotation.Location;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;
import io.github.nbdd0121.compiler.ir.function.Local;
import io.github.nbdd0121.compiler.ir.pass.BlockLocalPass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class LoadStoreOptimization extends BlockLocalPass {

	HashMap<Value, Value> varLoadLookup = new HashMap<>();
	HashMap<Location, Value> memoryInfo = new HashMap<>();
	HashMap<Location, Integer> storeInfo = new HashMap<>();

	public Location getLocation(Value val) {
		if (val instanceof Local) {
			return AliasAnalysis.getAnnotation((Local) val).location;
		} else {
			throw new UnsupportedOperationException(val.toString());
		}
	}

	public Value loadFrom(Value from, Value dest) {
		if (varLoadLookup.containsKey(from)) {
			return varLoadLookup.get(from);
		}
		Location loc = getLocation(from);
		if (memoryInfo.containsKey(loc)) {
			return memoryInfo.get(loc);
		} else {
			if (loc.isDetermined())
				memoryInfo.put(loc, dest);
			varLoadLookup.put(from, dest);
			return null;
		}
	}

	public void invalidate(Location loc) {
		varLoadLookup.clear();
		HashSet<Location> alias = new HashSet<>();
		for (Location l : memoryInfo.keySet()) {
			if (l.isInterfereWith(loc)) {
				alias.add(l);
			}
		}
		for (Location l : alias) {
			memoryInfo.remove(l);
		}
	}

	public void storeTo(Location loc, Value dest) {
		invalidate(loc);
		memoryInfo.put(loc, dest);
	}

	public boolean pass(Block block) {
		boolean ret = false;
		List<Instruction> insts = block.getInstructions();
		for (int i = 0; i < insts.size(); i++) {
			Instruction ins = insts.get(i);
			switch (ins.type) {
				case LOAD: {
					Value replace = loadFrom(ins.op[0], ins.dest);
					if (replace != null) {
						insts.set(i, Instruction.assign(ins.dest, replace));
						System.out.println("Optimize load " + ins.op[0]
								+ " to " + replace);
						ret = true;
					}
					break;
				}
				case STORE:
					Location loc = getLocation(ins.op[0]);
					storeTo(loc, ins.op[1]);
					varLoadLookup.put(ins.op[0], ins.op[1]);
					if (storeInfo.containsKey(loc)) {
						int index = storeInfo.get(loc);
						System.out.println("Optimize out store operation "
								+ index + " to " + ins.op[0]);
						insts.set(index, Instruction.nop());
						ret = true;
					}
					storeInfo.put(loc, i);
					break;
				case CALL:
					memoryInfo.clear();
					varLoadLookup.clear();
					storeInfo.clear();
					break;
				default:
					break;
			}
		}
		memoryInfo.clear();
		storeInfo.clear();
		varLoadLookup.clear();
		return ret;
	}

	public boolean pass(Function function) {
		AliasAnalysis.annotate(function);
		return super.pass(function);
	}
}
