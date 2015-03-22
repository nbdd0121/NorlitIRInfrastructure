package io.github.nbdd0121.compiler.ir.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.nwgjb.commons.StringUtil;

public class FunctionType extends Type {

	Type ret;
	List<Type> param = new ArrayList<>();
	boolean vaarg;

	public FunctionType(boolean vaarg, Type ret, Type... param) {
		this.ret = ret;
		this.vaarg = vaarg;
		this.param = Arrays.asList(param);
	}

	public FunctionType(Type ret, Type... param) {
		this(false, ret, param);
	}

	@Override
	public String toString() {
		if (!vaarg)
			return ret + "(" + StringUtil.join(param, ", ") + ")";
		else if (param.isEmpty()) {
			return ret + "(...)";
		} else {
			return ret + "(" + StringUtil.join(param, ", ") + ", ...)";
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (!(obj instanceof FunctionType)) {
			return false;
		}
		FunctionType func = (FunctionType) obj;
		return vaarg == func.vaarg && ret.equals(func.ret)
				&& param.equals(func.param);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ret, param, vaarg);
	}

	@Override
	public int getByteSize() {
		throw new RuntimeException("Cannot query size of function type");
	}

	public Type getReturnType() {
		return ret;
	}

	public List<Type> getParameters() {
		return param;
	}

}
