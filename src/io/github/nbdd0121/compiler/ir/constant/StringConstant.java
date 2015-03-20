package io.github.nbdd0121.compiler.ir.constant;

import com.nwgjb.commons.StringEscaper;

import io.github.nbdd0121.compiler.ir.type.ArrayType;
import io.github.nbdd0121.compiler.ir.type.IntegerType;
import io.github.nbdd0121.compiler.ir.type.Type;

public class StringConstant extends Constant {

	ArrayType type;
	String value;

	public StringConstant(String value) {
		type = new ArrayType(IntegerType.I8, value.getBytes().length+1);
		this.value = value;
	}

	public String toString() {
		return "\"" + StringEscaper.escape(value) + "\"";
	}

	public String stringValue() {
		return value;
	}
	
	@Override
	public Type getType(){
		return type;
	}

}
