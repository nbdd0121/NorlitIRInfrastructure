package io.github.nbdd0121.compiler.ir.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nwgjb.commons.StringUtil;

public class StructType extends AggregateType {

	List<Type> items = new ArrayList<Type>();

	public StructType(Type... types) {
		items = Arrays.asList(types);
	}

	public String toString() {
		return "{" + StringUtil.join(items, ", ") + "}";
	}

	@Override
	public int getByteSize() {
		int size = 0;
		for (Type t : items) {
			size += t.getByteSize();
		}
		return size;
	}

}
