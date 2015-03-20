package io.github.nbdd0121.compiler.ir;

import io.github.nbdd0121.compiler.ir.type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nwgjb.commons.StringUtil;

public class Module {

	ArrayList<Declaration> declarations = new ArrayList<>();
	HashMap<String, Global> globals = new HashMap<>();

	public void emit(Declaration decl) {
		declarations.add(decl);
	}

	public Global emitGlobal(Type type, String name) {
		if (globals.containsKey(name)) {
			Global l = globals.get(name);
			if (type != null && !l.getType().equals(type)) {
				throw new RuntimeException(
						"NorlitIR: Type check failure: previous declaration of "
								+ name + " yields type " + l.getType()
								+ " instead of " + type);
			}
			return l;
		} else {
			Global l = new Global(type, name);
			globals.put(name, l);
			return l;
		}
	}

	public String toString() {
		return StringUtil.join(declarations, "\n");
	}

	public List<Declaration> getDeclarations() {
		return declarations;
	}
}
