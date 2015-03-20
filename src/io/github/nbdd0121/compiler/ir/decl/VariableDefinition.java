package io.github.nbdd0121.compiler.ir.decl;

import io.github.nbdd0121.compiler.ir.Declaration;
import io.github.nbdd0121.compiler.ir.Global;
import io.github.nbdd0121.compiler.ir.constant.Constant;

public class VariableDefinition extends Declaration {

	Global variable;
	Constant value;

	public VariableDefinition(Global var, Constant val) {
		variable = var;
		value = val;
	}

	public String toString() {
		return variable + " = " + value;
	}

	public Constant getValue() {
		return value;
	}

	public Global getVariable() {
		return variable;
	}

}
