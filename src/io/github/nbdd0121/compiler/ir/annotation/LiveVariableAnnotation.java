package io.github.nbdd0121.compiler.ir.annotation;

import io.github.nbdd0121.compiler.ir.function.Local;

import java.util.HashSet;

public class LiveVariableAnnotation extends Annotation {

	public HashSet<Local> export = new HashSet<>();
	public HashSet<Local> internal = new HashSet<>();
	
}
