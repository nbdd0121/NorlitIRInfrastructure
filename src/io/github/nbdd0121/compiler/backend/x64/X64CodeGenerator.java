package io.github.nbdd0121.compiler.backend.x64;

import static io.github.nbdd0121.compiler.backend.x64.X64Location.R10;
import static io.github.nbdd0121.compiler.backend.x64.X64Location.R11;
import static io.github.nbdd0121.compiler.backend.x64.X64Location.R8;
import static io.github.nbdd0121.compiler.backend.x64.X64Location.R9;
import static io.github.nbdd0121.compiler.backend.x64.X64Location.RAX;
import static io.github.nbdd0121.compiler.backend.x64.X64Location.RBP;
import static io.github.nbdd0121.compiler.backend.x64.X64Location.RCX;
import static io.github.nbdd0121.compiler.backend.x64.X64Location.RDX;
import static io.github.nbdd0121.compiler.backend.x64.X64Location.RSP;
import static io.github.nbdd0121.compiler.backend.x64.X64Location.newImm;
import io.github.nbdd0121.compiler.backend.CodeGenerator;
import io.github.nbdd0121.compiler.ir.Declaration;
import io.github.nbdd0121.compiler.ir.Global;
import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Module;
import io.github.nbdd0121.compiler.ir.Value;
import io.github.nbdd0121.compiler.ir.analysis.LifetimeAnalysis;
import io.github.nbdd0121.compiler.ir.annotation.LiveVariableAnnotation;
import io.github.nbdd0121.compiler.ir.constant.IntegerConstant;
import io.github.nbdd0121.compiler.ir.decl.VariableDeclaration;
import io.github.nbdd0121.compiler.ir.decl.VariableDefinition;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;
import io.github.nbdd0121.compiler.ir.function.Local;
import io.github.nbdd0121.compiler.ir.type.PointerType;
import io.github.nbdd0121.compiler.ir.type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.nwgjb.commons.StringUtil;
import com.nwgjb.containers.IntContainer;

public class X64CodeGenerator extends CodeGenerator {

	static X64Location[] volatileRegisters = { RAX, RCX, RDX, R8, R9, R10, R11 };

	/*
	 * A cache of current processing block's LiveVariableAnnotation. This helps
	 * to track whether the lifetime of a local variable ends
	 */
	LiveVariableAnnotation liveVariableAnnotation;
	/*
	 * Track of local & block-internal variable's lifetime. Zero indicates the
	 * end of variable's life time, and its occupied register or memory will be
	 * freed
	 */
	HashMap<Local, IntContainer> variableRefCount = new HashMap<>();

	/*
	 * Track of a variable's location. It is either a register location or
	 * memory location. If a variable has both a register location and a memory
	 * location, the register location is stored.
	 */
	HashMap<Local, X64Location> variableLocation = new HashMap<>();

	/*
	 * Track of a variable's memory location. This is useful for spilling and
	 * cross-block lifetime variables.
	 */
	HashMap<Local, X64Location> memoryLocation = new HashMap<>();

	/*
	 * Track what's stored in the register.
	 */
	HashMap<X64Location, Value> registerValue = new HashMap<>();

	LinkedList<X64Location> freeMemoryLocations = new LinkedList<>();

	/*
	 * Track whether a register is pegged. If a register is pegged, its content
	 * cannot be spilled out. This is useful when a operation requires more than
	 * 1 register.
	 */
	HashSet<X64Location> pegged = new HashSet<>();

	List<X64Instruction> assembly = new ArrayList<>();

	int localVarSize;

	/**
	 * Indicates the variable is used once.
	 * 
	 * @param var
	 *            The variable
	 */
	private void decreaseReferenceCount(Value val) {
		/*
		 * Lifetime reference count works only for local variables
		 */
		if (!(val instanceof Local)) {
			return;
		}
		Local var = (Local) val;
		/*
		 * For export or external local variables, we need to preserve the value
		 * in memory
		 */
		if (!liveVariableAnnotation.internal.contains(var)) {
			return;
		}
		IntContainer container = variableRefCount.get(var);
		/*
		 * Initialize the reference count according to the result of lifetime
		 * analysis
		 */
		if (container == null) {
			container = new IntContainer();
			container.value = LifetimeAnalysis.getAnnotation(var).internalRef;
			variableRefCount.put(var, container);
		}
		container.value--;
		/*
		 * If lifetime ended, we can recycle the resource owned by the variable
		 */
		if (container.value <= 0) {
			X64Location loc = variableLocation.get(var);
			if (loc.isRegister())
				registerValue.remove(loc);
			if (memoryLocation.containsKey(var)) {
				freeMemoryLocations.add(memoryLocation.remove(var));
			}
			variableLocation.remove(var);
			memoryLocation.remove(var);
			// System.out.println("; Lifetime of " + var + " ended");
		}
	}

	/**
	 * Allocate a memory location for a variable, or reuse if previously
	 * allocated
	 * 
	 * @param var
	 * @return The RBP relative memory location of a local variable
	 */
	private X64Location getMemoryLocation(Local var) {
		if (!memoryLocation.containsKey(var)) {
			int size = var.getType().getByteSize();
			if (freeMemoryLocations.isEmpty()) {
				localVarSize += 8; // Make variable aligned
				X64Location loc = X64Location.getMemoryLocation("[rbp - "
						+ localVarSize + "]", size);
				memoryLocation.put(var, loc);
				return loc;
			} else {
				X64Location loc = freeMemoryLocations.removeLast()
						.getSubLocation(size);
				memoryLocation.put(var, loc);
				return loc;
			}
		} else {
			return memoryLocation.get(var);
		}
	}

	/**
	 * Bind a register with a local variable
	 * 
	 * @param l
	 *            Location
	 * @param reg
	 *            Register
	 */
	private void bindRegister(Local l, X64Location reg) {
		variableLocation.put(l, reg);
		registerValue.put(reg, l);
	}

	/**
	 * Find a spare register without spilling any content to memory
	 * 
	 * @return Finded register, or null if cannot find
	 */
	public X64Location allocRegisterNoSpill() {
		Value l;
		for (X64Location loc : volatileRegisters) {
			l = registerValue.get(loc);
			if (l == null) {
				return loc;
			}
		}
		return null;
	}

	/**
	 * Allocate a register. If no register is available, register variables will
	 * be spill to memory
	 * 
	 * @return Finded register
	 */
	public X64Location allocRegister() {
		X64Location reg = allocRegisterNoSpill();
		if (reg != null) {
			return reg;
		}
		for (X64Location loc : volatileRegisters) {
			if (!pegged.contains(loc)) {
				spillRegister(loc);
				return loc;
			}
		}
		throw new RuntimeException("run out of registers");
	}

	/**
	 * Spill a register to memory
	 * 
	 * @param reg
	 *            Register to spill
	 */
	public void spillRegister(X64Location reg) {
		Value spilled = registerValue.get(reg);
		if (spilled != null) {
			// System.out.println("; " + reg + " containing " + spilled
			// + " is spilled");
			if (spilled instanceof Local) {
				/*
				 * If the variable is the first time spill to memory, then we
				 * need to spill it to memory. However, this strategy will cause
				 * problems in loop TODO if sth need spill, spill it
				 * immediatedly after allocation
				 */
				if (memoryLocation.get(spilled) == null) {
					moveVariable((Local) spilled,
							getMemoryLocation((Local) spilled));
				} else {
					bindVariable((Local) spilled,
							getMemoryLocation((Local) spilled));
				}
			} else {
				registerValue.put(reg, null);
			}
		}
	}

	/**
	 * Move a variable from current location to destination
	 * 
	 * @param l
	 *            The value to move
	 * @param to
	 *            Destination
	 */
	public void moveVariable(Value l, X64Location to) {
		X64Location from = getLocation(l);
		if (from.isRegister()) {
			/* Old register will not longer be used for the variable */
			registerValue.put(from, null);
		}
		if (to.isRegister()) {
			/* New register will hold the variable */
			registerValue.put(to, l);
		}
		if (l instanceof Local)
			variableLocation.put((Local) l, to);
		mov(to, from);
	}

	/**
	 * Bind a variable from current location to destination without moving
	 * anything
	 * 
	 * @param l
	 *            The value to move
	 * @param to
	 *            Destination
	 */
	public void bindVariable(Value l, X64Location to) {
		X64Location from = getLocation(l);
		if (from.isRegister()) {
			/* Old register will not longer be used for the variable */
			registerValue.put(from, null);
		}
		if (to.isRegister()) {
			/* New register will hold the variable */
			registerValue.put(to, l);
		}
		if (l instanceof Local)
			variableLocation.put((Local) l, to);
	}

	/**
	 * Ensure a register is stored in target location
	 * 
	 * @param l
	 *            Value to store in reg
	 * @param reg
	 *            Target register
	 */
	public void ensureRegister(Value l, X64Location reg) {
		/* Variable that reg holds */
		Value var = registerValue.get(reg);

		/* If fortunately reg holds l, we do nothing */
		if (var == l) {
			return;
		}

		/* If the register is not occupied, we simply move the variable */
		if (var == null) {
			moveVariable(l, reg);
			return;
		}

		/* Spill the register, so now it's unoccupied */
		spillRegister(reg);
		moveVariable(l, reg);
	}

	/**
	 * Peg a register, so it will not be moved
	 * 
	 * @param reg
	 *            Register to peg
	 */
	public void pegRegister(X64Location reg) {
		if (reg.isRegister())
			pegged.add(reg);
	}

	/**
	 * Unpeg a register
	 * 
	 * @param reg
	 *            Register to peg
	 */
	public void unpegRegister(X64Location reg) {
		pegged.remove(reg);
	}

	/**
	 * Get a location that can be directly addressed via [] syntax.
	 * 
	 * @param v
	 *            Value to address
	 * @return Location
	 */
	public X64Location getAddressableLocation(Value v) {
		if (v instanceof Local) {
			Local l = (Local) v;
			X64Location loc = variableLocation.get(l);
			if (loc != null) {
				if (loc.isRegister()) {
					return loc;
				}
				X64Location dest = allocRegister();
				moveVariable(l, dest);
				return dest;
			}
			loc = allocRegister();
			variableLocation.put(l, loc);
			registerValue.put(loc, l);
			// System.out.println("; " + loc + " is allocated to " + l);
			return loc;
		} else if (v instanceof IntegerConstant) {
			return X64Location.newImm("" + ((IntegerConstant) v).longValue());
		} else {
			throw new RuntimeException("");
		}
	}

	/**
	 * Get a location of given value. Similar to getAddressableLocation, but
	 * this time the location can be in memory
	 * 
	 * @param v
	 *            Value
	 * @return Location
	 */
	public X64Location getLocation(Value v) {
		if (v instanceof Local) {
			Local l = (Local) v;
			X64Location loc = variableLocation.get(l);
			if (loc != null) {
				return loc;
			}
			loc = allocRegister();
			variableLocation.put(l, loc);
			registerValue.put(loc, l);
			// System.out.println("; " + loc + " is allocated to " + l);
			return loc;
		} else if (v instanceof IntegerConstant) {
			return X64Location.newImm("" + ((IntegerConstant) v).longValue());
		} else {
			throw new RuntimeException("");
		}
	}

	public void generate(Module module) {
		for (Declaration decl : module.getDeclarations()) {
			if (decl instanceof Function) {
				generate((Function) decl);
			} else if (decl instanceof VariableDeclaration) {
				System.out.println("[extern " + decl.getVariable().name + "]");
			} else if (decl instanceof VariableDefinition) {
				throw new UnsupportedOperationException(decl.toString());
			} else {
				throw new AssertionError();
			}
		}
	}

	private void generate(Function func) {
		LifetimeAnalysis.annotate(func);

		List<Local> param = func.getParameters();
		if (param.size() > 0) {
			bindRegister(param.get(0), RCX);
		}
		if (param.size() > 1) {
			bindRegister(param.get(1), RDX);
		}
		if (param.size() > 2) {
			bindRegister(param.get(2), R8);
		}
		if (param.size() > 3) {
			bindRegister(param.get(3), R9);
		}
		if (param.size() > 4) {
			throw new UnsupportedOperationException();
		}

		List<Block> blocks = func.getBlocks();
		for (int i = 0; i < blocks.size() - 1; i++) {
			generate(blocks.get(i), blocks.get(i + 1));
		}
		generate(blocks.get(blocks.size() - 1), null);

		String name = func.getVariable().getName();
		System.out.println("[global " + name + "]");
		System.out.println(name + ":");
		System.out.println("push rbp");
		System.out.println("mov rbp, rsp");
		System.out.println("sub rsp, " + ((localVarSize + 15) & ~15));
		System.out.println(StringUtil.join(assembly, "\n"));

		localVarSize = 0;
		assembly.clear();
	}

	public void mov(X64Location dest, X64Location src) {
		if (dest == src) {
			return;
		}
		if (dest.isMemory() && src.isMemory()) {
			mov(X64Location.RAX, src);
			mov(dest, X64Location.RAX);
			return;
		}
		if (dest.getSize() == src.getSize() || src.isImmediate()) {
			assembly.add(new X64Instruction("mov", dest, src));
		} else {
			if (dest.isRegister() && src.isMemory()
					&& src.getSize() < dest.getSize()) {
				assembly.add(new X64Instruction("movzx", dest, src));
				return;
			} else if (dest.isMemory() && src.isRegister()
					&& src.getSize() > dest.getSize()) {
				assembly.add(new X64Instruction("mov", dest, src
						.getSubLocation(dest.getSize())));
				return;
			}
			throw new RuntimeException("Unsupported operation moving from "
					+ src + " to " + dest);
		}
	}

	private void spillAllRegisters() {
		for (X64Location loc : volatileRegisters) {
			spillRegister(loc);
		}
	}

	private void generateCall(Instruction ins) {
		/*
		 * This code generator conforms to Microsoft X64 Calling convention.
		 * Arguments are passed by rcx, rdx, r8 and r9
		 */
		if (ins.op.length > 1) {
			ensureRegister(ins.op[1], RCX);
			pegRegister(RCX);
		}
		if (ins.op.length > 2) {
			ensureRegister(ins.op[2], RDX);
			pegRegister(RDX);
		}
		if (ins.op.length > 3) {
			ensureRegister(ins.op[3], R8);
			pegRegister(R8);
		}
		if (ins.op.length > 4) {
			ensureRegister(ins.op[4], R9);
			pegRegister(R9);
		}
		if (ins.op.length > 5) {
			throw new UnsupportedOperationException();
		}

		/*
		 * Clean variables and store volatile registers
		 */
		decreaseReferenceCount(ins.op);
		spillAllRegisters();

		unpegRegister(RCX);
		unpegRegister(RDX);
		unpegRegister(R8);
		unpegRegister(R9);

		/* Shadow space */
		assembly.add(new X64Instruction("sub", RSP, newImm("32")));
		String funcName = ((Global) ins.op[0]).name;
		assembly.add(new X64Instruction("call", newImm(funcName)));
		assembly.add(new X64Instruction("add", RSP, newImm("32")));
	}

	private void decreaseReferenceCount(Value... values) {
		for (Value v : values) {
			decreaseReferenceCount(v);
		}
	}

	private void generate(Block b, Block nextBlock) {
		liveVariableAnnotation = LifetimeAnalysis.getAnnotation(b);
		variableLocation.putAll(memoryLocation);

		if (b.isLabelled())
			assembly.add(new X64Instruction(b + ":"));

		for (Instruction ins : b.getInstructions()) {
			assembly.add(new X64Instruction(""));
			assembly.add(new X64Instruction("; " + ins));
			switch (ins.type) {
				case ALLOCA: {
					/* Decrease rsp by certain amount to allocate */
					Type type = (Type) ins.op[0];
					int size = type.getByteSize();
					if (ins.op.length == 2) {
						if (ins.op[1] instanceof IntegerConstant) {
							size *= ((IntegerConstant) ins.op[1]).longValue();
						} else {
							throw new UnsupportedOperationException(
									"Variable size alloca not supported yet");
						}
					}
					assembly.add(new X64Instruction("sub", RSP,
							newImm(((size + 15) & ~15))));
					mov(getLocation(ins.dest), X64Location.RSP);
					break;
				}
				case STORE: {
					/*
					 * Since a store operation involves with memory, both
					 * operands should be addressable so no mov [mem], [mem]
					 * will happen
					 */
					X64Location dest = getAddressableLocation(ins.op[0]);
					pegRegister(dest);
					X64Location src = getAddressableLocation(ins.op[1]);
					unpegRegister(dest);
					PointerType ptr = (PointerType) ins.op[0].getType();
					mov(X64Location.getMemoryLocation("[" + dest + "]", ptr
							.getRefer().getByteSize()), src);
					decreaseReferenceCount(ins.op);
					break;
				}
				case LOAD: {
					/* Same as store */
					X64Location dest = getAddressableLocation(ins.dest);
					pegRegister(dest);
					X64Location src = getAddressableLocation(ins.op[0]);
					unpegRegister(dest);
					PointerType ptr = (PointerType) ins.op[0].getType();
					mov(dest, X64Location.getMemoryLocation("[" + src + "]",
							ptr.getRefer().getByteSize()));
					decreaseReferenceCount(ins.op[0]);
					break;
				}
				case CALL: {
					generateCall(ins);
					/* Return value at RAX */
					if (ins.dest != null)
						bindRegister(ins.dest, RAX);
					break;
				}
				case ADD: {
					/* First make two operands available */
					X64Location loc1 = getLocation(ins.op[0]);
					pegRegister(loc1);
					X64Location loc2 = getLocation(ins.op[1]);
					unpegRegister(loc2);
					/*
					 * By clean variable, we make possbility bigger that the
					 * allocated register will be same as loc1, so we can
					 * eliminate a mov instruction
					 */
					decreaseReferenceCount(ins.op);
					X64Location value = allocRegister();
					mov(value, loc1);
					assembly.add(new X64Instruction("add", value, loc2));
					bindRegister(ins.dest, value);
					break;
				}
				case SUB: {
					/* Same as add */
					X64Location loc1 = getLocation(ins.op[0]);
					pegRegister(loc1);
					X64Location loc2 = getLocation(ins.op[1]);
					unpegRegister(loc2);
					decreaseReferenceCount(ins.op);
					X64Location value = allocRegister();
					mov(value, loc1);
					assembly.add(new X64Instruction("sub", value, loc2));
					bindRegister(ins.dest, value);
					break;
				}
				case COND_JMP: {
					/* Make the variable available */
					X64Location loc = getLocation(ins.op[0]);
					/* Clean it up */
					decreaseReferenceCount(ins.op[0]);
					/* Spill everything to RAM, since we are leaving the block */
					spillAllRegisters();
					/*
					 * Same to do so, since we did not use any register after
					 * cleaning up
					 */
					loc = loc.getSubLocation(ins.op[0].getType().getByteSize());
					if (loc.isRegister()) {
						assembly.add(new X64Instruction("test", loc, loc));
					} else {
						assembly.add(new X64Instruction("cmp", loc, newImm(0)));
					}
					if (ins.op[1] == nextBlock) {
						assembly.add(new X64Instruction("jz", newImm(ins.op[2]
								.toString())));
					} else if (ins.op[2] == nextBlock) {
						assembly.add(new X64Instruction("jnz", newImm(ins.op[1]
								.toString())));
					} else {
						assembly.add(new X64Instruction("jnz", newImm(ins.op[1]
								.toString())));
						assembly.add(new X64Instruction("jmp", newImm(ins.op[2]
								.toString())));
					}
					break;
				}
				case JMP: {
					if (ins.op[0] != nextBlock) {
						spillAllRegisters();
						assembly.add(new X64Instruction("jmp", newImm(ins.op[0]
								.toString())));
					}
					break;
				}
				case RETURN: {
					if (ins.op.length == 1)
						ensureRegister(ins.op[0], RAX);
					assembly.add(new X64Instruction("mov", RSP, RBP));
					assembly.add(new X64Instruction("pop", RBP));
					assembly.add(new X64Instruction("ret"));
					break;
				}
				case GETELEMENTPTR: {
					PointerType type = (PointerType) ins.op[0].getType();
					if (ins.op.length != 2 || type.getRefer().getByteSize() != 1) {
						throw new UnsupportedOperationException();
					}
					/*
					 * Similar to add, since strong typed indexing is just
					 * pointer arithmetics
					 */
					X64Location loc1 = getLocation(ins.op[0]);
					pegRegister(loc1);
					X64Location loc2 = getLocation(ins.op[1]);
					unpegRegister(loc2);

					decreaseReferenceCount(ins.op);
					X64Location value = allocRegister();
					mov(value, loc1);
					assembly.add(new X64Instruction("add", value, loc2));
					bindRegister(ins.dest, value);
					break;
				}

				default:
					throw new RuntimeException("unknown " + ins);
			}
		}

		spillAllRegisters();

		variableRefCount.clear();
		variableLocation.clear();
		registerValue.clear();
	}
}
