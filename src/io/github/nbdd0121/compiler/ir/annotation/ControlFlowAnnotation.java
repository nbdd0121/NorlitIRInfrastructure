package io.github.nbdd0121.compiler.ir.annotation;

import io.github.nbdd0121.compiler.ir.function.Block;

import java.util.HashSet;

public class ControlFlowAnnotation extends Annotation {

	public HashSet<Block> predecessor = new HashSet<>();
	public HashSet<Block> successor = new HashSet<>();
	
}
