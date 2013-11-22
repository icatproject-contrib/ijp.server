package org.icatproject.ijp.client.parser;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

	enum State {
		RESET, NONE, INQUOTES, CLOSEDQUOTES, LT, NAME, INTEGER, REAL, NOT, GT, EQ, AMPERSAND, BAR, MINUS
	}

	public static List<Token> getTokens(String input) throws LexerException {
		List<Token> tokens = new ArrayList<Token>();
		State state = State.NONE;
		int start = 0;
		char ch = ' ';
		for (int i = 0; i < input.length() + 1; i++) {
			if (state == State.RESET) {
				i--;
				state = State.NONE;
			} else if (i < input.length()) {
				ch = input.charAt(i);
			} else {
				ch = 0;
			}
			// System.out.println(state + " " + i + " " + ch);
			if (state == State.NONE) {
				if (ch == ' ' || ch == 0) {
					// Ignore
				} else if (Character.isLetter(ch)) {
					state = State.NAME;
					start = i;
				} else if (Character.isDigit(ch)) {
					state = State.INTEGER;
					start = i;
				} else if (ch == '\'') {
					state = State.INQUOTES;
					start = i;
				} else if (ch == '(') {
					tokens.add(new Token(Token.Type.OPENPAREN, ch));
				} else if (ch == ')') {
					tokens.add(new Token(Token.Type.CLOSEPAREN, ch));
				} else if (ch == '<') {
					state = State.LT;
				} else if (ch == '+') {
					tokens.add(new Token(Token.Type.PLUMIN, ch));
				} else if (ch == '-') {
					state = State.MINUS;
					start = i;
				} else if (ch == '*' || ch == '/' || ch == '%') {
					tokens.add(new Token(Token.Type.TIMESMULT, ch));
				} else if (ch == '=') {
					state = State.EQ;
				} else if (ch == '&') {
					state = State.AMPERSAND;
				} else if (ch == '|') {
					state = State.BAR;
				} else if (ch == '>') {
					state = State.GT;
				} else if (ch == '!') {
					state = State.NOT;
				} else {
					reportError(ch, state, i, input);
				}
			} else if (state == State.INQUOTES) {
				if (ch == '\'') {
					state = State.CLOSEDQUOTES;
				} else if (ch == 0) {
					reportError(ch, state, i, input);
				}
			} else if (state == State.CLOSEDQUOTES) {
				if (ch == '\'') {
					state = State.INQUOTES;
				} else {
					tokens.add(new Token(Token.Type.STRING, input.substring(start + 1, i - 1)
							.replace("''", "'")));
					state = State.RESET;
				}
			} else if (state == State.GT) {
				if (ch == '=') {
					tokens.add(new Token(Token.Type.COMPOP, ">="));
					state = State.NONE;
				} else {
					tokens.add(new Token(Token.Type.COMPOP, ">"));
					state = State.RESET;
				}
			} else if (state == State.LT) {
				if (ch == '=') {
					tokens.add(new Token(Token.Type.COMPOP, "<="));
					state = State.NONE;
				} else {
					tokens.add(new Token(Token.Type.COMPOP, "<"));
					state = State.RESET;
				}
			} else if (state == State.EQ) {
				if (ch == '=') {
					tokens.add(new Token(Token.Type.COMPOP, "=="));
					state = State.NONE;
				} else {
					reportError(ch, state, i, input);
				}
			} else if (state == State.AMPERSAND) {
				if (ch == '&') {
					tokens.add(new Token(Token.Type.AND, "&&"));
					state = State.NONE;
				} else {
					reportError(ch, state, i, input);
				}
			} else if (state == State.BAR) {
				if (ch == '|') {
					tokens.add(new Token(Token.Type.OR, "||"));
					state = State.NONE;
				} else {
					reportError(ch, state, i, input);
				}
			} else if (state == State.NAME) {
				if (!Character.isLetterOrDigit(ch) && ch != '_') {
					String name = input.substring(start, i);
					if (name.equals("True") || name.equals("False")) {
						tokens.add(new Token(Token.Type.BOOLEAN_LITERAL, name));
					} else if (name.equals("null")) {
						tokens.add(new Token(Token.Type.NULL, name));
					} else {
						tokens.add(new Token(Token.Type.NAME, name));
					}
					state = State.RESET;
				}
			} else if (state == State.MINUS) {
				if (Character.isDigit(ch)) {
					state = State.INTEGER;
				} else {
					tokens.add(new Token(Token.Type.PLUMIN, "-"));
					state = State.RESET;
				}
			} else if (state == State.INTEGER) {
				if (ch == 'e' || ch == 'E' || ch == '.') {
					state = State.REAL;
				} else if (!Character.isDigit(ch)) {
					tokens.add(new Token(Token.Type.INTEGER, input.substring(start, i)));
					state = State.RESET;
				}
			} else if (state == State.REAL) {
				if (!Character.isDigit(ch) && ch != 'e' && ch != 'E' && ch != '.' && ch != '+'
						&& ch != '-') {
					Double d = null;
					try {
						d = Double.parseDouble(input.substring(start, i));
					} catch (NumberFormatException e) {
						reportError(ch, state, i, input);
					}
					tokens.add(new Token(Token.Type.REAL, d.toString()));
					state = State.RESET;
				}
			} else if (state == State.NOT) {
				if (ch == '=') {
					tokens.add(new Token(Token.Type.COMPOP, "!="));
					state = State.NONE;
				} else {
					tokens.add(new Token(Token.Type.NOT, "!"));
					state = State.RESET;
				}
			}
		}

		return tokens;
	}

	private static void reportError(char ch, State state, int i, String input)
			throws LexerException {
		int i1 = Math.max(0, i - 4);
		int i2 = Math.min(i + 5, input.length());
		if (ch != 0) {
			throw new LexerException("Unexpected character '" + ch + "' near \""
					+ input.substring(i1, i2) + "\" in state " + state + " for string: " + input);
		} else {
			throw new LexerException("Unexpected end of string in state " + state + " for string: "
					+ input);
		}
	}

	public static String getTypeToPrint(Token.Type type) {
		if (type == Token.Type.COMPOP) {
			return ">, <, !=, =, <>, >=, <=";
		} else if (type == Token.Type.OPENPAREN) {
			return "(";
		} else if (type == Token.Type.CLOSEPAREN) {
			return ")";
		}
		return type.name();
	}

}
