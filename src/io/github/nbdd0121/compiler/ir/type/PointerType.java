package io.github.nbdd0121.compiler.ir.type;

import io.github.nbdd0121.compiler.backend.Model;

import java.util.HashMap;

public class PointerType extends Type {

	public static final PointerType NULL_TYPE = new PointerType(null);

	static HashMap<Type, PointerType> cache = new HashMap<>();

	Type refer;

	private PointerType(Type refer) {
		this.refer = refer;
	}

	public static PointerType get(Type refer) {
		if (cache.containsKey(refer)) {
			return cache.get(refer);
		} else {
			PointerType ret = new PointerType(refer);
			cache.put(refer, ret);
			return ret;
		}
	}

	public String toString() {
		return refer + "*";
	}

	public Type getRefer() {
		return refer;
	}
	
	@Override
	public int getByteSize(){
		return Model.getModel().getMachineWordSize();
	}
	

}
