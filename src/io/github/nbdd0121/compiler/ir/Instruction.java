package io.github.nbdd0121.compiler.ir;

import io.github.nbdd0121.compiler.ir.function.Local;
import io.github.nbdd0121.compiler.ir.type.PointerType;
import io.github.nbdd0121.compiler.ir.type.Type;

import com.nwgjb.commons.StringUtil;

public class Instruction {

	public static enum Opcode {
		/* Pointer Arithmetic */
		INDEX("index"),

		NOP("nop"), ALLOCA("alloca"), LOAD("load"), STORE("store"),
		/* Assignment */
		ASSIGN,
		/* Arithmetic */
		ADD("+"), SUB("-"), MUL("*"), DIV("/"), MOD("%"),
		/* Comparision */
		LT("<"), LTEQ("<="), GT(">"), GTEQ(">="), EQ("=="), INEQ("!="),
		/* Bitwise/Logical Operation */
		AND("&"), OR("|"), XOR("^"), NEG("-"),
		/* Function call */
		CALL("call"), RETURN("return"),
		/* Jump */
		JMP("jmp"), COND_JMP("jmp");

		String desc;

		private Opcode() {

		}

		private Opcode(String str) {
			desc = str;
		}

		public String toString() {
			return desc;
		}

	}

	public Opcode type;
	public Local dest;
	public Value op[];

	public Instruction(Opcode type, Local dest, Value... op) {
		this.type = type;
		this.dest = dest;
		this.op = op;
	}

	public String toString() {
		switch (type) {
			case ALLOCA:
			case LOAD:
			case STORE:
			case INDEX:
			case NOP:
			case COND_JMP:
			case JMP:
			case RETURN:
			case CALL:
				if (dest != null)
					return dest + " = " + type + " "
							+ StringUtil.join(op, ", ");
				else
					return type + " " + StringUtil.join(op, ", ");
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case MOD:
			case LT:
			case LTEQ:
			case GT:
			case GTEQ:
			case EQ:
			case INEQ:
			case AND:
			case OR:
			case XOR:
				return dest + " = " + op[0] + " " + type + " " + op[1];
			case NEG:
				return dest + " = " + type + op[0];
			case ASSIGN:
				return dest + " = " + op[0];
			default:
				return super.toString();
		}
	}

	public static Instruction assign(Local dest, Value op1) {
		if (dest == null) {
			throw new RuntimeException(
					"NorlitIR: assignment must have a target operand");
		}
		if (!dest.getType().equals(op1.getType())) {
			throw new RuntimeException("NorlitIR: assignment from "
					+ op1.getType() + " to " + dest.getType() + " is not valid");
		}
		return new Instruction(Opcode.ASSIGN, dest, op1);
	}

	public static Instruction add(Local dest, Value op1, Value op2) {
		return new Instruction(Opcode.ADD, dest, op1, op2);
	}

	public static Instruction sub(Local dest, Value op1, Value op2) {
		return new Instruction(Opcode.SUB, dest, op1, op2);
	}

	public static Instruction alloca(Local dest, Type size) {
		if (dest == null) {
			throw new RuntimeException(
					"NorlitIR: alloca must have a target operand");
		}
		return new Instruction(Opcode.ALLOCA, dest, size);
	}

	public static Instruction alloca(Local dest, Type size, Value val) {
		if (dest == null) {
			throw new RuntimeException(
					"NorlitIR: alloca must have a target operand");
		}
		return new Instruction(Opcode.ALLOCA, dest, size, val);
	}

	public static Instruction load(Local dest, Value ptr) {
		return new Instruction(Opcode.LOAD, dest, ptr);
	}

	public static Instruction store(Value dest, Value value) {
		Type destType = dest.getType();
		if (!(destType instanceof PointerType)) {
			throw new RuntimeException("NorlitIR: Cannot store to non-pointer");
		}
		if (!((PointerType) destType).getRefer().equals(value.getType())) {
			throw new RuntimeException("NorlitIR: Cannot store "
					+ value.getType() + " to " + destType);
		}
		return new Instruction(Opcode.STORE, null, dest, value);
	}

	public static Instruction index(Local ret, Value val, Value idx) {
		return new Instruction(Opcode.INDEX, ret, val, idx);
	}

	public static Instruction call(Local ret, Value op1, Value... ops) {
		Value[] o = new Value[ops.length + 1];
		o[0] = op1;
		System.arraycopy(ops, 0, o, 1, ops.length);
		return new Instruction(Opcode.CALL, ret, o);
	}

	public static Instruction voidCall(Value op1, Value... ops) {
		return call(null, op1, ops);
	}

	public static Instruction jmp() {
		return new Instruction(Opcode.JMP, null, (Value) null);
	}

	public static Instruction condJmp(Value predicate) {
		return new Instruction(Opcode.COND_JMP, null, predicate, null, null);
	}

	public static Instruction nop() {
		return new Instruction(Opcode.NOP, null);
	}

	public static Instruction return_(Value value) {
		return new Instruction(Opcode.RETURN, null, value);
	}

	public static Instruction return_() {
		return new Instruction(Opcode.RETURN, null);
	}

	public boolean isBranchInstruction() {
		switch (type) {
			case JMP:
			case COND_JMP:
				return true;
			default:
				return false;
		}
	}

}
