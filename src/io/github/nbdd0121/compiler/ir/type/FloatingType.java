package io.github.nbdd0121.compiler.ir.type;


public class FloatingType extends Type {

	public static final FloatingType FLOAT = new FloatingType(23, 8);
	public static final FloatingType DOUBLE = new FloatingType(52, 11);

	int fracBits;
	int expBits;

	private FloatingType(int fracBits, int expBits) {
		this.fracBits = fracBits;
		this.expBits = expBits;
	}

	public String toString() {
		if (this == FLOAT) {
			return "float";
		} else if (this == DOUBLE) {
			return "double";
		}
		return "f" + (fracBits + expBits + 1);
	}

	@Override
	public int getByteSize() {
		return (fracBits + expBits + 1 + 7) / 8;
	}

}
