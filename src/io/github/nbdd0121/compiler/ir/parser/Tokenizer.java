package io.github.nbdd0121.compiler.ir.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;

public class Tokenizer {

	PushbackReader reader;

	public Tokenizer(Reader reader) {
		this.reader = new PushbackReader(reader);
	}

	public Tokenizer(String str) {
		this(new StringReader(str));
	}

	public Tokenizer(InputStream stream) {
		this(new InputStreamReader(stream));
	}

	public static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	public static boolean isLetter(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	public static boolean isIdentifierStart(char c) {
		return isLetter(c) || c == '.';
	}

	public static boolean isIdentifierPart(char c) {
		return isIdentifierStart(c) || isDigit(c);
	}

	private String readIdentifier() throws IOException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			char character = (char) reader.read();
			if (!isIdentifierPart(character)) {
				reader.unread(character);
				break;
			}
			sb.append(character);
		}
		return sb.toString();
	}

	public Token next() {
		try {
			while (true) {
				char character = (char) reader.read();
				if (character == 0xFFFF) {
					return new Token("EOF");
				}

				switch (character) {
					case ' ':
					case '\t':
					case '\r':
						continue;
					case '\n':
						return new Token("LineBreak");
					case ';': {
						char c;
						while (true) {
							c = (char) reader.read();
							if (c == '\r' || c == '\n' || c == 0xFFFF) {
								reader.unread(c);
								break;
							}
						}
						continue;
					}
					case '-': {
						character = (char) reader.read();
						if (isDigit(character)) {
							return new Token("Number", "-" + character
									+ readIdentifier());
						} else {
							reader.unread(character);
							return new Token("-");
						}
					}
					case '*':
					case '=':
					case '[':
					case ']':
					case '(':
					case ')':
					case ',':
					case '+':
					case ':':
					case '{':
					case '}':
						return new Token(String.valueOf(character));
					case '%':
						return new Token("Local", readIdentifier());
					case '@':
						return new Token("Global", readIdentifier());
					case '.':
						return new Token("Label", readIdentifier());
					case '"': {
						StringBuilder sb = new StringBuilder();
						while (true) {
							character = (char) reader.read();
							switch (character) {
								case '\r':
								case '\n':
								case 0xFFFF:
									throw new ParsingException(
											"String not enclosed");
								case '"':
									return new Token("String", sb.toString());
							}
							sb.append(character);
						}
					}
				}
				if (isDigit(character)) {
					return new Token("Number", character + readIdentifier());
				} else if (isIdentifierStart(character)) {
					String id = character + readIdentifier();
					if (id.charAt(0) == 'i' && isDigit(id.charAt(1))) {
						return new Token("IntType", id.substring(1));
					}
					return new Token(id);
				}
				throw new RuntimeException(character + " is not valid");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
