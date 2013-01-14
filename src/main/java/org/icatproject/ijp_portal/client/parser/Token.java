package org.icatproject.ijp_portal.client.parser;

public class Token {

	enum Type {
		STRING, NAME, OPENPAREN, CLOSEPAREN, COMPOP, INTEGER, AND, OR, NOT, REAL, BOOLEAN_LITERAL, PLUMIN, TIMESMULT, NULL;

		public String toString() {
			return Tokenizer.getTypeToPrint(this);
		}
	};

	private Type type;

	private String value;

	public String getValue() {
		return value;
	}

	Token(Type type, String value) {
		this.type = type;
		this.value = value;
	}

	public Token(Type type, char value) {
		this.type = type;
		this.value = Character.toString(value);
	}

	public Type getType() {
		return type;
	}

	public String toString() {
		return this.value;
	}

}
