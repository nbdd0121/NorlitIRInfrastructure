package io.github.nbdd0121.compiler.ir.constant;

import io.github.nbdd0121.compiler.ir.type.PointerType;
import io.github.nbdd0121.compiler.ir.type.Type;


public class NullConstant extends Constant {

	public static final NullConstant NULL = new NullConstant();
	
	private NullConstant() {
		
	}

	public String toString() {
		return "null";
	}
	
	@Override
	public Type getType(){
		return PointerType.get(null);
	}

}
