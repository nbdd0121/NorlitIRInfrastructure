package io.github.nbdd0121.compiler.ir.constant;

import io.github.nbdd0121.compiler.ir.type.FloatingType;
import io.github.nbdd0121.compiler.ir.type.Type;

import java.math.BigDecimal;

public class FloatingConstant extends Constant {
	
	FloatingType type;
	BigDecimal value;

	public FloatingConstant(FloatingType type, double value) {
		this.type = type;
		this.value = BigDecimal.valueOf(value);
	}

	public String toString() {
		return type + " " + value;
	}
	
	@Override
	public Type getType(){
		return type;
	}

}
