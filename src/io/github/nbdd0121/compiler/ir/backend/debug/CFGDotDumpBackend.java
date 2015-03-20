package io.github.nbdd0121.compiler.ir.backend.debug;

import io.github.nbdd0121.compiler.backend.Backend;
import io.github.nbdd0121.compiler.backend.CodeGenerator;
import io.github.nbdd0121.compiler.backend.Model;

public class CFGDotDumpBackend extends Backend {

	private static CFGDotDumpBackend instance = new CFGDotDumpBackend();

	private CFGDotDumpBackend() {

	}

	public Model getModel() {
		return Model.ABSTRACT;
	}

	public Class<? extends CodeGenerator> getCodeGeneratorFactory() {
		return CFGDotDump.class;
	}

	public static CFGDotDumpBackend getInstance() {
		return instance;
	}

}
