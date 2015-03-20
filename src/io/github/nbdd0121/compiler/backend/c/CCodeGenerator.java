package io.github.nbdd0121.compiler.backend.c;

import io.github.nbdd0121.compiler.backend.CodeGenerator;
import io.github.nbdd0121.compiler.ir.Declaration;
import io.github.nbdd0121.compiler.ir.Global;
import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Module;
import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.constant.IntegerConstant;
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

import java.util.List;

import com.nwgjb.commons.StringUtil;

public class CCodeGenerator extends CodeGenerator {

	int tempCounter = 0;

	private static String mangleName(String name, boolean numLead) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0'
					&& c <= '9' && (numLead || i != 0)) {
				sb.append(c);
			} else if (c == '_') {
				sb.append("_");
			} else {
				sb.append("_").append(Integer.toHexString(c)).append("_");
			}
		}
		return sb.toString();
	}

	private static String mangleLabel(String name) {
		return "label_" + mangleName(name, true);
	}

	private static String mangleLocal(Local local) {
		return "local_" + mangleName(local.getName(), true);
	}

	private static String mangleType(Type t) {
		if (t instanceof IntegerType) {
			return "int" + t.getByteSize() * 8 + "_t";
		} else if (t instanceof FunctionType) {
			FunctionType type = (FunctionType) t;
			if (type.getParameters().size() == 0) {
				return "typeof(" + mangleType(type.getReturnType()) + "(void))";
			} else {
				return "typeof("
						+ mangleType(type.getReturnType())
						+ "("
						+ StringUtil.join(
								type.getParameters().stream()
										.map(CCodeGenerator::mangleType), ", ")
						+ "))";
			}
		} else if (t instanceof VoidType) {
			return "void";
		} else if (t instanceof PointerType) {
			return mangleType(((PointerType) t).getRefer()) + "*";
		} else if (t instanceof ArrayType) {
			ArrayType type = (ArrayType) t;
			return "typeof(" + mangleType(type.getElementType()) + "["
					+ type.getDimension() + "])";
		} else {
			throw new UnsupportedOperationException(t.toString());
		}
	}

	private static String mangleTypedName(Type t, String mangledName) {
		if (t instanceof IntegerType) {
			return "int" + t.getByteSize() * 8 + "_t " + mangledName;
		} else if (t instanceof FunctionType) {
			FunctionType type = (FunctionType) t;
			if (type.getParameters().size() == 0) {
				return mangleType(type.getReturnType()) + " " + mangledName
						+ "(void)";
			} else {
				return mangleType(type.getReturnType())
						+ " "
						+ mangledName
						+ "("
						+ StringUtil.join(
								type.getParameters().stream()
										.map(CCodeGenerator::mangleType), ", ")
						+ ")";
			}
		} else if (t instanceof PointerType) {
			return mangleType(((PointerType) t).getRefer()) + "* "
					+ mangledName;
		} else if (t instanceof ArrayType) {
			ArrayType type = (ArrayType) t;
			return mangleType(type.getElementType()) + " " + mangledName + "["
					+ type.getDimension() + "]";
		} else {
			throw new UnsupportedOperationException(t.toString());
		}
	}

	private String getTemp() {
		return "temp_" + (tempCounter++);
	}

	private String getLocation(Value v) {
		if (v instanceof Local) {
			return mangleLocal((Local) v);
		} else if (v instanceof Global) {
			return mangleName(((Global) v).getName(), false);
		} else if (v instanceof IntegerConstant) {
			return ((IntegerConstant) v).getValue().toString();
		} else {
			throw new UnsupportedOperationException(v.toString());
		}
	}

	private void defineLocal(Local l) {
		System.out.print(mangleTypedName(l.getType(), mangleLocal(l)));
	}

	private void defineGlobal(Global l) {
		System.out.print(mangleTypedName(l.getType(),
				mangleName(l.getName(), false)));
	}

	@Override
	public void generate(Module module) {
		System.out.println("#include <stdint.h>");
		for (Declaration decl : module.getDeclarations()) {
			if (decl instanceof Function) {
				generate((Function) decl);
			} else if (decl instanceof VariableDeclaration) {
				defineGlobal(decl.getVariable());
				System.out.println(";");
			} else if (decl instanceof VariableDefinition) {
				throw new UnsupportedOperationException(decl.toString());
			} else {
				throw new AssertionError();
			}
		}
	}

	private void generate(Function func) {
		Global var = func.getVariable();
		String name = mangleName(var.getName(), false);
		FunctionType type = (FunctionType) func.getVariable().getType();
		System.out.print(mangleType(type.getReturnType()) + " " + name + "(");
		if (type.getParameters().size() == 0) {
			System.out.println("void) {");
		} else {
			List<Local> params = func.getParameters();
			for (int i = 0; i < params.size(); i++) {
				if (i != 0) {
					System.out.print(", ");
				}
				Local l = params.get(i);
				defineLocal(l);
			}
			System.out.println(") {");
		}
		List<Block> blocks = func.getBlocks();
		for (int i = 0; i < blocks.size() - 1; i++) {
			generate(blocks.get(i), blocks.get(i + 1));
		}
		generate(blocks.get(blocks.size() - 1), null);
		System.out.println("}");
	}

	private void generate(Block b, Block nextBlock) {
		if (b.isLabelled())
			System.out.println(mangleLabel(b.getLabel()) + ":;");

		for (Instruction ins : b.getInstructions()) {
			System.out.println("// " + ins);
			switch (ins.type) {
				case ALLOCA: {
					Type type = (Type) ins.op[0];
					String temp = getTemp();
					if (ins.op.length == 2)
						System.out.println(mangleTypedName(type, temp) + "["
								+ getLocation(ins.op[1]) + "];");
					else
						System.out.println(mangleTypedName(type, temp) + ";");
					defineLocal(ins.dest);
					System.out.println(" = &" + temp + ";");
					break;
				}
				case STORE: {
					System.out.println("*(" + getLocation(ins.op[0]) + ") = "
							+ getLocation(ins.op[1]) + ";");
					break;
				}
				case LOAD: {
					defineLocal(ins.dest);
					System.out.println(" = *(" + getLocation(ins.op[0]) + ");");
					break;
				}
				case ADD: {
					defineLocal(ins.dest);
					System.out.println(" = " + getLocation(ins.op[0]) + " + "
							+ getLocation(ins.op[1]) + ";");
					break;
				}
				case SUB: {
					defineLocal(ins.dest);
					System.out.println(" = " + getLocation(ins.op[0]) + " - "
							+ getLocation(ins.op[1]) + ";");
					break;
				}
				case CALL: {
					if (ins.dest != null) {
						defineLocal(ins.dest);
						System.out.print(" = ");
					}
					System.out.print(getLocation(ins.op[0]) + "(");
					for (int i = 1; i < ins.op.length; i++) {
						if (i != 1) {
							System.out.print(", ");
						}
						System.out.print(getLocation(ins.op[i]));
					}
					System.out.print(");\n");
					break;
				}
				case COND_JMP: {
					System.out
							.print("if("
									+ getLocation(ins.op[0])
									+ ") goto "
									+ mangleLabel(((Block) ins.op[1])
											.getLabel())
									+ "; else goto "
									+ mangleLabel(((Block) ins.op[2])
											.getLabel()) + ";");
					break;
				}
				case JMP: {
					System.out
							.print("goto "
									+ mangleLabel(((Block) ins.op[0])
											.getLabel()) + ";");
					break;
				}
				case RETURN: {
					if (ins.op[0] == null)
						System.out.print("return;");
					else
						System.out.print("return " + getLocation(ins.op[0])
								+ ";");
					break;
				}
				case GETELEMENTPTR: {
					if (ins.op.length != 2) {
						throw new UnsupportedOperationException();
					}
					defineLocal(ins.dest);
					System.out.println(" = &" + getLocation(ins.op[0]) + "["
							+ getLocation(ins.op[1]) + "];");
					break;
				}
				default:
					throw new RuntimeException("unknown " + ins);
			}
			System.out.println();
		}
	}
}
