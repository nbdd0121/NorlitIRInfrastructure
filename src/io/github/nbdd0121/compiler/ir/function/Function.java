package io.github.nbdd0121.compiler.ir.function;

import io.github.nbdd0121.compiler.ir.Declaration;
import io.github.nbdd0121.compiler.ir.Global;
import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nwgjb.commons.StringUtil;

public class Function extends Declaration {

	Global variable;

	HashMap<String, Local> locals = new HashMap<>();

	ArrayList<Local> param = new ArrayList<>();

	ArrayList<Block> blocks = new ArrayList<>();
	Block current = new Block(this);
	Block entryBlock = current;
	{
		current.setLabel("");
		blocks.add(current);
	}

	public Function(Global var) {
		this.variable = var;
	}

	public void addParameter(Local local) {
		locals.put(local.name, local);
		param.add(local);
	}

	public void emit(Instruction ins) {
		current.emit(ins);
	}

	public Block emitLabel() {
		Block block = new Block(this);
		addBlock(block);
		return block;
	}

	public Local getTemp(Type t) {
		return getNamedLocal(t, "" + locals.size());
	}

	public Local getNamedLocal(Type type, String name) {
		if (locals.containsKey(name)) {
			Local l = locals.get(name);
			if (type != null && !l.getType().equals(type)) {
				throw new RuntimeException(
						"NorlitIR: Type check failure: previous declaration of "
								+ name + " yields type " + l.getType()
								+ " instead of " + type);
			}
			return l;
		} else {
			Local l = new Local(type, name);
			locals.put(name, l);
			return l;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("define ").append(variable)
				.append(StringUtil.join(param, ", ")).append("{\n");
		for (Block b : blocks) {
			sb.append(b.toCodeString("  "));
		}
		sb.append("}");
		return sb.toString();
	}

	public int getLocalVarCount() {
		return locals.size();
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	public Global getVariable() {
		return variable;
	}

	public List<Local> getParameters() {
		return param;
	}

	public HashMap<String, Local> getLocalMap() {
		return locals;
	}

	public void addBlock(Block lbl) {
//		Instruction last = current.getLastInstruction();
//		if (last == null || !last.isBranchInstruction()) {
//			Instruction jmp = Instruction.jmp();
//			jmp.op[0] = lbl;
//			emit(jmp);
//		}
		current = lbl;
		blocks.add(current);
	}

}
