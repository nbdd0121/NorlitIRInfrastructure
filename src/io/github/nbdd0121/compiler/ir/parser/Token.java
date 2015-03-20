package io.github.nbdd0121.compiler.ir.parser;

public class Token {

	String type;
	String value;

	public Token(String type, String value) {
		this.type = type;
		this.value = value;
	}

	public Token(String type) {
		this.type = type;
	}

	public String toString() {
		if (value == null)
			return type;
		return type + " " + value;
	}

	public String getType() {
		return type;
	}

	public boolean is(String string) {
		return type.equals(string);
	}

	public String getValue() {
		return value;
	}

}
