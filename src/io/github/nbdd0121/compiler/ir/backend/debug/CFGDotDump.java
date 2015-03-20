package io.github.nbdd0121.compiler.ir.backend.debug;

import io.github.nbdd0121.compiler.backend.CodeGenerator;
import io.github.nbdd0121.compiler.ir.Declaration;
import io.github.nbdd0121.compiler.ir.Instruction;
import io.github.nbdd0121.compiler.ir.Module;
import io.github.nbdd0121.compiler.ir.function.Block;
import io.github.nbdd0121.compiler.ir.function.Function;

import java.util.List;

import com.nwgjb.commons.StringEscaper;

public class CFGDotDump extends CodeGenerator {

	StringBuilder builder;

	private void generate(Block b, Block nextBlock) {
		builder.append("  \"")
				.append(b)
				.append("\" [shape=record,label=\"{")
				.append(StringEscaper.escape(b.toCodeString("")).replace("\\n",
						"\\l"));
		Instruction last = b.getLastInstruction();
		if (last == null) {
			builder.append("}\"]\n");
			if (nextBlock != null)
				builder.append("  \"").append(b).append("\" -> \"")
						.append(nextBlock).append("\"\n");
		} else {
			switch (last.type) {
				case JMP:
					builder.append("}\"]\n  \"").append(b).append("\" -> \"")
							.append(last.op[0]).append("\"\n");
					break;
				case COND_JMP:
					builder.append("|{<T>T|<F>F}}\"]\n  \"").append(b)
							.append("\":T -> \"").append(last.op[1])
							.append("\"\n  \"").append(b).append("\":F -> \"")
							.append(last.op[2]).append("\"\n");
					break;
				case RETURN:
					builder.append("}\"]\n");
					break;
				default:
					builder.append("}\"]\n");
					if (nextBlock != null)
						builder.append("  \"").append(b).append("\" -> \"")
								.append(nextBlock).append("\"\n");
					break;
			}
		}
	}

	private String dump(Function func) {
		builder = new StringBuilder();
		builder.append("digraph \"")
				.append(StringEscaper.escape(func.getVariable().getName()))
				.append("\" {\n");

		List<Block> blocks = func.getBlocks();
		for (int i = 0; i < blocks.size(); i++) {
			generate(blocks.get(i),
					i == blocks.size() - 1 ? null : blocks.get(i + 1));
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public void generate(Module mod) {
		for (Declaration d : mod.getDeclarations()) {
			if (d instanceof Function) {
				System.out.println(dump((Function) d));
			}
		}
	}
}
