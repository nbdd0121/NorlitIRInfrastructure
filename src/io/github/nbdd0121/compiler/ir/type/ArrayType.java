package io.github.nbdd0121.compiler.ir.type;

import java.util.Objects;

public class ArrayType extends AggregateType {

	Type refer;
	int dimension;

	public ArrayType(Type refer, int dim) {
		this.refer = refer;
		this.dimension = dim;
	}

	@Override
	public String toString() {
		return "[" + dimension + " x " + refer + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof ArrayType) {
			ArrayType t = (ArrayType) o;
			return t.refer.equals(refer) && t.dimension == dimension;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(refer, dimension);
	}

	@Override
	public int getByteSize() {
		return refer.getByteSize() * dimension;
	}

	public Type getElementType() {
		return refer;
	}

	public int getDimension() {
		return dimension;
	}

}
