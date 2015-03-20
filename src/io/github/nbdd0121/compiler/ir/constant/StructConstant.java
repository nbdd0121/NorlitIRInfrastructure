package io.github.nbdd0121.compiler.ir.constant;

import io.github.nbdd0121.compiler.ir.type.StructType;
import io.github.nbdd0121.compiler.ir.type.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nwgjb.commons.StringUtil;

public class StructConstant extends Constant {

	StructType type;
	List<Constant> items = new ArrayList<Constant>();

	public StructConstant(StructType type, Constant... types) {
		this.type = type;
		items = Arrays.asList(types);
	}

	public String toString() {
		return type + " {" + StringUtil.join(items, ", ") + "}";
	}
	
	@Override
	public Type getType(){
		return type;
	}

}
