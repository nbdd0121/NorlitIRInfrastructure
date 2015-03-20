package io.github.nbdd0121.compiler.ir.type;

import io.github.nbdd0121.compiler.ir.Value;

public abstract class Type extends Value {

	public static final Type TYPE = new Type(){
		@Override
		public int getByteSize() {
			throw new RuntimeException("Cannot query size of a type");
		}
	};

	protected Type() {

	}

	@Override
	public Type getType() {
		return TYPE;
	}

	public abstract int getByteSize();

	public boolean isPointer() {
		return this instanceof PointerType;
	}

}
