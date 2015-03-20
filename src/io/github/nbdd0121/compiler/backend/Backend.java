package io.github.nbdd0121.compiler.backend;

public abstract class Backend {

	private static Backend backend;

	public abstract Model getModel();

	public abstract Class<? extends CodeGenerator> getCodeGeneratorFactory();

	public static void setBackend(Backend b) {
		backend = b;
		Model.setModel(b.getModel());
		CodeGenerator.setFactory(b.getCodeGeneratorFactory());
	}

	public static Backend getBackend() {
		return backend;
	}

}
