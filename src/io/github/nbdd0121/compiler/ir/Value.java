package io.github.nbdd0121.compiler.ir;

import io.github.nbdd0121.compiler.ir.annotation.Annotation;
import io.github.nbdd0121.compiler.ir.type.Type;

import java.util.ArrayList;

public abstract class Value {

	ArrayList<Annotation> annotations = null;

	public abstract Type getType();

	public void addAnnotation(Annotation anno) {
		if (annotations == null)
			annotations = new ArrayList<>();
		annotations.add(anno);
	}

	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(Class<T> clazz) {
		if (annotations == null)
			return null;
		for (Annotation a : annotations) {
			if (clazz.isInstance(a)) {
				return (T) a;
			}
		}
		return null;
	}

}
