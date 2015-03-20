package io.github.nbdd0121.compiler.ir.type;

import java.util.HashMap;

public class IntegerType extends Type {

	static IntegerType[] arrCache = new IntegerType[128];
	static HashMap<Integer, IntegerType> cache = new HashMap<>();
	public static final IntegerType I1 = new IntegerType(1);
	public static final IntegerType I8 = new IntegerType(8);
	public static final IntegerType I16 = new IntegerType(16);
	public static final IntegerType I32 = new IntegerType(32);
	public static final IntegerType I64 = new IntegerType(64);

	int bits;

	private IntegerType(int bits) {
		this.bits = bits;
	}

	public static IntegerType get(int bits) {
		if (bits < arrCache.length) {
			if (arrCache[bits] == null) {
				arrCache[bits] = new IntegerType(bits);
			}
			return arrCache[bits];
		} else {
			if (cache.containsKey(bits)) {
				return cache.get(bits);
			} else {
				IntegerType ret = new IntegerType(bits);
				cache.put(bits, ret);
				return ret;
			}
		}
	}

	public int getByteSize() {
		return (bits + 7) / 8;
	}

	public String toString() {
		return "i" + bits;
	}

}
