package io.github.nbdd0121.compiler.ir.type;

public class IncompleteType extends Type {

	public IncompleteType() {

	}

	public String toString() {
		return "incomplete";
	}

	@Override
	public int getByteSize() {
		throw new RuntimeException("Cannot query size of incomplete type");
	}

}
