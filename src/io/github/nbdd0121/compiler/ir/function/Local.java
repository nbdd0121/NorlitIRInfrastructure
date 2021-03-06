package io.github.nbdd0121.compiler.ir.function;

import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.type.Type;

public class Local extends Value {

	Type type;
	public String name;

	Local(Type type, String name) {
		this.type = type;
		this.name = name;
	}

	public String toString() {
		return type + " %" + name;
	}

	@Override
	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

}
