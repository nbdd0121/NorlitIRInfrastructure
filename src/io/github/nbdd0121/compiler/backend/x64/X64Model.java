package io.github.nbdd0121.compiler.backend.x64;

import io.github.nbdd0121.compiler.backend.Model;

public class X64Model extends Model {

	private static X64Model instance = new X64Model();

	static String[] registers = { "rax", "rcx", "rdx", "r8", "r9", "r10", "r11" };

	@Override
	public int getMachineWordSize() {
		return 8;
	}

	public static X64Model getInstance() {
		return instance;
	}

}
