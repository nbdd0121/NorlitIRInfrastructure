package io.github.nbdd0121.compiler.ir.decl;

import io.github.nbdd0121.compiler.ir.Declaration;
import io.github.nbdd0121.compiler.ir.Global;

public class VariableDeclaration extends Declaration {

	Global variable;

	public VariableDeclaration(Global var) {
		variable = var;
	}

	public String toString() {
		return "declare " + variable;
	}
	
	public Global getVariable(){
		return variable;
	}

}
