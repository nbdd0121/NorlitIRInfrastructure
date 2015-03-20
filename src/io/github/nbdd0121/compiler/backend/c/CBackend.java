package io.github.nbdd0121.compiler.backend.c;

import io.github.nbdd0121.compiler.backend.Backend;
import io.github.nbdd0121.compiler.backend.CodeGenerator;
import io.github.nbdd0121.compiler.backend.Model;

public class CBackend extends Backend {

	private static CBackend instance = new CBackend();

	private CBackend() {

	}

	public Model getModel() {
		return Model.ABSTRACT;
	}

	public Class<? extends CodeGenerator> getCodeGeneratorFactory() {
		return CCodeGenerator.class;
	}

	public static CBackend getInstance() {
		return instance;
	}

}
