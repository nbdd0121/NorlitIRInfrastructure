package io.github.nbdd0121.compiler.backend;

import io.github.nbdd0121.compiler.backend.x64.X64CodeGenerator;
import io.github.nbdd0121.compiler.ir.Module;

public abstract class CodeGenerator {

	static Class<? extends CodeGenerator> factory = X64CodeGenerator.class;

	public abstract void generate(Module mod);

	public static void setFactory(Class<? extends CodeGenerator> clazz) {
		factory = clazz;
	}

	public static CodeGenerator newInstance() {
		try {
			return factory.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
