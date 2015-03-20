package io.github.nbdd0121.compiler.backend.x64;

import io.github.nbdd0121.compiler.backend.Backend;
import io.github.nbdd0121.compiler.backend.CodeGenerator;
import io.github.nbdd0121.compiler.backend.Model;

public class X64Backend extends Backend {

	private static X64Backend instance = new X64Backend();

	private X64Backend() {

	}

	public Model getModel() {
		return X64Model.getInstance();
	}

	public Class<? extends CodeGenerator> getCodeGeneratorFactory() {
		return X64CodeGenerator.class;
	}

	public static X64Backend getInstance() {
		return instance;
	}

}
