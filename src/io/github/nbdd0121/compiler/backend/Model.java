package io.github.nbdd0121.compiler.backend;

import io.github.nbdd0121.compiler.backend.x64.X64Model;

public abstract class Model {

	public static final Model ABSTRACT = new Model() {
		@Override
		public int getMachineWordSize() {
			throw new RuntimeException(
					"Abstract model do not have certain machine word size");
		}
	};

	static Model model = new X64Model();

	public abstract int getMachineWordSize();

	public static Model getModel() {
		return model;
	}

	public static void setModel(Model m) {
		model = m;
	}

}
