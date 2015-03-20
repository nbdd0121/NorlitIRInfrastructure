package io.github.nbdd0121.compiler.ir.parser;

import io.github.nbdd0121.compiler.ir.Declaration;
import io.github.nbdd0121.compiler.ir.Global;
import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Instruction.Opcode;
import io.github.nbdd0121.compiler.ir.Module;
import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.constant.Constant;
import io.github.nbdd0121.compiler.ir.constant.IntegerConstant;
import io.github.nbdd0121.compiler.ir.constant.StringConstant;
import io.github.nbdd0121.compiler.ir.decl.VariableDeclaration;
import io.github.nbdd0121.compiler.ir.decl.VariableDefinition;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;
import io.github.nbdd0121.compiler.ir.function.Local;
import io.github.nbdd0121.compiler.ir.type.ArrayType;
import io.github.nbdd0121.compiler.ir.type.FunctionType;
import io.github.nbdd0121.compiler.ir.type.IntegerType;
import io.github.nbdd0121.compiler.ir.type.PointerType;
import io.github.nbdd0121.compiler.ir.type.Type;
import io.github.nbdd0121.compiler.ir.type.VoidType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Parser {

	Tokenizer tokenizer;
	LinkedList<Token> pushbacks = new LinkedList<>();
	HashMap<String, Block> labelSymbolTable = new HashMap<>();

	/* Used by parseInstruction */
	Local dest;
	List<Value> operands = new ArrayList<>();

	Function function;

	Module module = new Module();

	public Parser(Tokenizer tok) {
		tokenizer = tok;
	}

	public Token lookahead() {
		if (!pushbacks.isEmpty()) {
			return pushbacks.peekFirst();
		} else {
			Token t = tokenizer.next();
			pushbacks.addFirst(t);
			return t;
		}
	}

	public Token next() {
		if (!pushbacks.isEmpty()) {
			return pushbacks.removeFirst();
		} else {
			return tokenizer.next();
		}
	}

	public Token expect(String type) {
		Token t = next();
		if (t.is(type)) {
			return t;
		}
		throw new RuntimeException("Expected type " + type
				+ " but encountered " + t.getType());
	}

	public Token consumeIf(String type) {
		Token t = lookahead();
		if (t.is(type)) {
			consume();
			return t;
		}
		return null;
	}

	public Token consumeIf(String type, String value) {
		Token t = lookahead();
		if (t.is(type) && t.getValue().equals(value)) {
			consume();
			return t;
		}
		return null;
	}

	public void consume() {
		next();
	}

	public Type parseType() {
		Token t = lookahead();
		Type type;
		switch (t.getType()) {
			case "void":
				consume();
				type = VoidType.VOID;
				break;
			case "IntType":
				consume();
				type = IntegerType.get(Integer.valueOf(t.getValue()));
				break;
			case "[": {
				consume();
				int dim = Integer.valueOf(expect("Number").getValue());
				expect("x");
				Type arrType = parseType();
				type = new ArrayType(arrType, dim);
				expect("]");
				break;
			}
			default:
				throw new ParsingException("Cannot parse '" + t
						+ "' as a type, instruction or operand");
		}
		outer: while (true) {
			t = lookahead();
			switch (t.getType()) {
				case "*":
					consume();
					type = PointerType.get(type);
					break;
				case "(": {
					consume();
					List<Type> args = new ArrayList<>();
					boolean vaarg = false;
					if (consumeIf(")") == null) {
						if (consumeIf("Label", "..") != null) {
							vaarg = true;
						} else {
							args.add(parseType());
							while (consumeIf(",") != null) {
								if (consumeIf("Label", "..") != null) {
									vaarg = true;
									break;
								}
								args.add(parseType());
							}
						}
						expect(")");
					}
					type = new FunctionType(vaarg, type,
							args.toArray(new Type[args.size()]));
					break;
				}
				default:
					break outer;
			}
		}
		return type;
	}

	public Local parseLocal(Type type) {
		String name = expect("Local").getValue();
		return function.getNamedLocal(type, name);
	}

	public Global parseGlobal(Type type) {
		String name = expect("Global").getValue();
		return module.emitGlobal(type, name);
	}

	public Value parseTypedValue() {
		if (lookahead().is("String")) {
			return new StringConstant(next().getValue());
		} else if (lookahead().is("Label")) {
			return parseLabel();
		}
		Type type = parseType();
		Token nxt = lookahead();
		switch (nxt.getType()) {
			case "Local":
				return parseLocal(type);
			case "Global":
				return parseGlobal(type);
			case "Number": {
				if (type instanceof IntegerType) {
					consume();
					String value = nxt.getValue();
					if (value.charAt(0) == '0') {
						if (value.length() == 1) {
							return new IntegerConstant((IntegerType) type, 0);
						}
						if (value.charAt(1) == 'x' || value.charAt(1) == 'X') {
							return new IntegerConstant((IntegerType) type,
									new BigInteger(value.substring(2), 16));
						}
						if (value.charAt(1) == 'o' || value.charAt(1) == 'O') {
							return new IntegerConstant((IntegerType) type,
									new BigInteger(value.substring(2), 8));
						}
						if (value.charAt(1) == 'b' || value.charAt(1) == 'B') {
							return new IntegerConstant((IntegerType) type,
									new BigInteger(value.substring(2), 2));
						}
						return new IntegerConstant((IntegerType) type,
								new BigInteger(value.substring(1), 8));
					}
					return new IntegerConstant((IntegerType) type,
							new BigInteger(nxt.getValue()));
				}
				throw new RuntimeException("E");
			}
			default:
				return type;
		}
	}

	public Block parseLabel() {
		String lit = expect("Label").getValue();
		if (labelSymbolTable.containsKey(lit)) {
			return labelSymbolTable.get(lit);
		} else {
			Block lbl = new Block(function);
			labelSymbolTable.put(lit, lbl);
			lbl.setLabel(lit);
			return lbl;
		}
	}

	public Instruction parseInstruction() {
		try {
			Type type = parseType();
			dest = parseLocal(type);
			expect("=");
		} catch (ParsingException e) {
			dest = null;
		}

		Value operand1;
		try {
			operand1 = parseTypedValue();
		} catch (ParsingException e) {
			operand1 = null;
		}

		if (operand1 == null) {
			Token opcode = next();

			operands.clear();
			if (consumeIf("LineBreak") == null) {
				operands.add(parseTypedValue());
				while (consumeIf(",") != null) {
					operands.add(parseTypedValue());

				}
				expect("LineBreak");
			}

			Value[] operandArr = operands.toArray(new Value[operands.size()]);

			/* Assembly like instructions */
			switch (opcode.getType()) {
				case "alloca": {
					return new Instruction(Opcode.ALLOCA, dest, operandArr);
				}
				case "store": {
					return new Instruction(Opcode.STORE, dest, operandArr);
				}
				case "load": {
					return new Instruction(Opcode.LOAD, dest, operandArr);
				}
				case "call": {
					return new Instruction(Opcode.CALL, dest, operandArr);
				}
				case "return": {
					return new Instruction(Opcode.RETURN, dest, operandArr);
				}
				case "getelementptr": {
					return new Instruction(Opcode.GETELEMENTPTR, dest,
							operandArr);
				}
				case "jmp": {
					if (operands.size() == 1) {
						return new Instruction(Opcode.JMP, dest, operandArr);
					} else if (operands.size() == 3) {
						return new Instruction(Opcode.COND_JMP, dest,
								operandArr);
					} else {
						throw new RuntimeException(
								"Either 1 or 3 arguments are required by jmp operation");
					}
				}
				default:
					throw new RuntimeException("Unknown instruction " + opcode);
			}
		} else {
			Token opcode = next();
			switch (opcode.getType()) {
				case "+": {
					Value op2 = parseTypedValue();
					expect("LineBreak");
					return Instruction.add(dest, operand1, op2);
				}
				case "-": {
					Value op2 = parseTypedValue();
					expect("LineBreak");
					return Instruction.sub(dest, operand1, op2);
				}
				case "LineBreak":
					return Instruction.assign(dest, operand1);
				default:
					throw new RuntimeException("Unknown instruction " + opcode);
			}
		}
	}

	public Function parseFunction() {
		labelSymbolTable.clear();

		consume();
		Type type = parseType();
		Global var = parseGlobal(type);
		function = new Function(var);
		while (consumeIf(",") != null) {
			function.addParameter(parseLocal(parseType()));
		}
		expect("{");

		while (!lookahead().is("}")) {
			if (lookahead().is("LineBreak")) {
				consume();
				continue;
			}
			if (lookahead().is("Label")) {
				Block lbl = parseLabel();
				expect(":");
				function.addBlock(lbl);
			} else {
				function.emit(parseInstruction());
			}
		}
		expect("}");
		expect("LineBreak");
		return function;
	}

	public Declaration parseDeclaration() {
		switch (lookahead().getType()) {
			case "declare": {
				consume();
				Type type = parseType();
				Global gbl = parseGlobal(type);
				expect("LineBreak");
				return new VariableDeclaration(gbl);
			}
			case "define": {
				return parseFunction();
			}
			default: {
				Type type = parseType();
				Global gbl = parseGlobal(type);
				expect("=");
				Value value = parseTypedValue();
				if (!(value instanceof Constant)) {
					throw new RuntimeException(
							"NorlitIR: Expected constant expression instead of "
									+ value);
				}
				expect("LineBreak");
				return new VariableDefinition(gbl, (Constant) value);
			}
		}
	}

	public void parse() {
		while (!lookahead().is("EOF")) {
			if (consumeIf("LineBreak") != null)
				continue;
			module.emit(parseDeclaration());
		}
	}

	public static Module parse(Tokenizer t) {
		Parser parser = new Parser(t);
		parser.parse();
		return parser.module;
	}

}
