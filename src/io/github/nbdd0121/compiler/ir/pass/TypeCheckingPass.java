package io.github.nbdd0121.compiler.ir.pass;

import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.type.FunctionType;
import io.github.nbdd0121.compiler.ir.type.IntegerType;
import io.github.nbdd0121.compiler.ir.type.PointerType;
import io.github.nbdd0121.compiler.ir.type.Type;
import io.github.nbdd0121.compiler.ir.type.VoidType;

import java.util.List;

public class TypeCheckingPass extends BlockLocalPass {

	public void checkType(Type expected, Type refer) {
		if (!expected.equals(refer)) {
			throw new RuntimeException("Expected type " + expected
					+ " instead of " + refer);
		}
	}

	public void checkPointerType(PointerType ptr, Type refer) {
		if (!ptr.getRefer().equals(refer)) {
			throw new RuntimeException("Type " + ptr + " cannot refer to "
					+ refer);
		}
	}

	public void checkPointerType(Type ptr, Type refer) {
		if (!ptr.isPointer()) {
			throw new RuntimeException("Type " + ptr + " is not a pointer");
		}
		checkPointerType((PointerType) ptr, refer);
	}

	public void checkPointer(Type ptr) {
		if (!(ptr instanceof PointerType)) {
			throw new RuntimeException("Type " + ptr + " is not a pointer");
		}
	}

	public void checkInteger(Type ptr) {
		if (!(ptr instanceof IntegerType)) {
			throw new RuntimeException("Type " + ptr + " is not a integer");
		}
	}

	public void checkAlloca(Instruction ins) {
		if (ins.dest == null) {
			throw new RuntimeException("Alloca must have a target operand");
		}
		if (ins.op.length != 1 && ins.op.length != 2) {
			throw new RuntimeException("Alloca takes 1 or 2 arguments");
		}
		if (!(ins.op[0] instanceof Type)) {
			throw new RuntimeException(
					"The first argument of alloca must be Type");
		}
		checkPointerType(ins.dest.getType(), (Type) ins.op[0]);
		if (ins.op.length == 2)
			checkInteger(ins.op[1].getType());
	}

	public void checkStore(Instruction ins) {
		if (ins.dest != null) {
			throw new RuntimeException(
					"Target operand is not allowed in store operation");
		}
		if (ins.op.length != 2) {
			throw new RuntimeException(
					"Exactly 2 arguments are required by store operation");
		}
		checkPointerType(ins.op[0].getType(), ins.op[1].getType());
	}

	public void checkLoad(Instruction ins) {
		if (ins.dest == null) {
			throw new RuntimeException(
					"Target operand is required in load operation");
		}
		if (ins.op.length != 1) {
			throw new RuntimeException(
					"Exactly 1 argument is required by load operation");
		}
		checkPointerType(ins.op[0].getType(), ins.dest.getType());
	}

	public void checkJmp(Instruction ins) {
		if (ins.dest != null) {
			throw new RuntimeException(
					"Target operand is not allowed in jmp operation");
		}
		if (ins.op.length != 1) {
			throw new RuntimeException(
					"Exactly 1 argument is required by jmp operation");
		}
		if (!(ins.op[0] instanceof Block)) {
			throw new RuntimeException("First argument of jmp must be a label");
		}
	}

	public void checkCondJmp(Instruction ins) {
		if (ins.dest != null) {
			throw new RuntimeException(
					"Target operand is not allowed in conditional jmp operation");
		}
		if (ins.op.length != 3) {
			throw new RuntimeException(
					"Exactly 3 arguments are required by conditional jmp operation");
		}
		// TODO TBD whether restrict type of ins.op[0]
		if (!(ins.op[1] instanceof Block) || !(ins.op[2] instanceof Block)) {
			throw new RuntimeException(
					"Second and third argument of conditional jmp must be a label");
		}
	}

	public void checkCall(Instruction ins) {
		if (ins.op.length == 0) {
			throw new RuntimeException(
					"At least 1 argument is required by call operation");
		}
		Type type = ins.op[0].getType();
		if (!(type instanceof PointerType)) {
			throw new RuntimeException(
					"Cannot call on non-function pointer type");
		}
		Type ptr = ((PointerType) type).getRefer();
		if (!(ptr instanceof FunctionType)) {
			throw new RuntimeException(
					"Cannot call on non-function pointer type");
		}

		FunctionType func = (FunctionType) ptr;

		if (ins.dest != null) {
			if (func.getReturnType() instanceof VoidType) {
				throw new RuntimeException(
						"Invalid use of return value of void-return function");
			}
			checkType(func.getReturnType(), ins.dest.getType());
		}

		List<Type> param = func.getParameters();
		if (param.size() != ins.op.length - 1) {
			throw new RuntimeException("Argument count mismatch. " + func
					+ " expect " + param.size() + " arguments but passed "
					+ (ins.op.length - 1) + " arguments");
		}
		for (int i = 0; i < ins.op.length - 1; i++) {
			checkType(param.get(i), ins.op[i + 1].getType());
		}
	}

	public void checkIndex(Instruction ins) {
		if (ins.dest == null) {
			throw new RuntimeException(
					"Target operand is required in index operation");
		}
		if (ins.op.length != 2) {
			throw new RuntimeException(
					"Exactly 2 arguments are required by index operation");
		}
		checkPointer(ins.op[0].getType());
		checkInteger(ins.op[1].getType());
	}

	private void checkReturn(Instruction ins) {
		if (ins.dest != null) {
			throw new RuntimeException(
					"Target operand is not allowed in return operation");
		}
		FunctionType type = function.getType();
		Type ret = type.getReturnType();
		if (ret instanceof VoidType) {
			if (ins.op.length != 0) {
				throw new RuntimeException(
						"Exactly 0 arguments are required by return operation in void function");
			}
		} else {
			if (ins.op.length != 1) {
				throw new RuntimeException(
						"Exactly 1 argument is required by return operation in non-void function");
			}
			checkType(ret, ins.op[0].getType());
		}
	}

	private void checkArithmetic(Instruction ins) {
		if (ins.dest == null) {
			throw new RuntimeException(
					"Target operand is required in arithmetic operations");
		}
		if (ins.op.length != 2) {
			throw new RuntimeException(
					"Exactly 2 arguments are required by arithmetic operations");
		}
		Type targetType = ins.dest.getType();
		checkInteger(targetType);
		checkType(targetType, ins.op[0].getType());
		checkType(targetType, ins.op[1].getType());
	}

	public boolean pass(Block block) {
		for (Instruction ins : block.getInstructions()) {
			switch (ins.type) {
				case ALLOCA:
					checkAlloca(ins);
					break;
				case STORE:
					checkStore(ins);
					break;
				case LOAD:
					checkLoad(ins);
					break;
				case JMP:
					checkJmp(ins);
					break;
				case COND_JMP:
					checkCondJmp(ins);
					break;
				case CALL:
					checkCall(ins);
					break;
				case RETURN:
					checkReturn(ins);
					break;
				case ADD:
				case SUB:
					checkArithmetic(ins);
					// TODO Could be implemented after getelementptr
					break;
				case INDEX:
					checkIndex(ins);
					break;
				default:
					// break;
					throw new UnsupportedOperationException(ins.toString());
			}
		}
		return false;
	}

}
