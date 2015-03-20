package io.github.nbdd0121.compiler.ir.type;


public class VoidType extends Type {

	public static final VoidType VOID = new VoidType();
	
	private VoidType() {

	}

	public String toString() {
		return "void";
	}

	@Override
	public int getByteSize() {
		throw new RuntimeException("Cannot query size of void type");
	}

}
