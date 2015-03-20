package io.github.nbdd0121.compiler.ir.constant;

import static io.github.nbdd0121.compiler.ir.type.IntegerType.I32;
import io.github.nbdd0121.compiler.ir.type.IntegerType;
import io.github.nbdd0121.compiler.ir.type.Type;

import java.math.BigInteger;

public class IntegerConstant extends Constant {

	public static final IntegerConstant TRUE = new IntegerConstant(
			IntegerType.I1, 1);
	public static final IntegerConstant FALSE = new IntegerConstant(
			IntegerType.I1, 0);

	IntegerType type;
	BigInteger value;

	public IntegerConstant(IntegerType type, int value) {
		this.type = type;
		this.value = BigInteger.valueOf(value);
	}

	public IntegerConstant(IntegerType type, BigInteger value) {
		this.type = type;
		this.value = value;
	}
	
	public String toString() {
		return type + " " + value;
	}

	public BigInteger getValue() {
		return value;
	}

	public long longValue() {
		return value.longValue();
	}

	@Override
	public Type getType() {
		return type;
	}

	public static IntegerConstant i32(int val) {
		return new IntegerConstant(I32, val);
	}

}
