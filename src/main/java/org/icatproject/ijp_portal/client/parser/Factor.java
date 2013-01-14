package org.icatproject.ijp_portal.client.parser;

import java.util.Map;

public class Factor {

	// Factor ::= ("-")? ( Name | Literal | ( "(" ArithExpression ")" )

	private boolean minus;
	private Object value;
	private ArithExpression arithExpression;

	public Factor(Input input, Map<String, Object> m) throws ParserException {
		Token t = input.peek(0);
		if (t.getType() == Token.Type.PLUMIN && t.getValue().equals("-")) {
			input.consume();
			this.minus = true;
		}

		t = input.consume();
		if (t.getType() == Token.Type.OPENPAREN) {
			this.arithExpression = new ArithExpression(input, m);
			input.consume(Token.Type.CLOSEPAREN);
		} else {
			if (t.getType() == Token.Type.NAME) {
				value = m.get(t.getValue());
				if (value == null) {
					if (!m.containsKey(t.getValue())) {
						throw new ParserException(t.getValue()
								+ " has no value supplied in the map");
					}
				}
				if (value instanceof Float) {
					value = new Double((Float) value);
				} else if (value instanceof Integer) {
					value = new Long((Integer) value);
				}
			} else if (t.getType() == Token.Type.STRING) {
				this.value = t.getValue();
			} else if (t.getType() == Token.Type.INTEGER) {
				this.value = Long.parseLong(t.getValue());
			} else if (t.getType() == Token.Type.REAL) {
				this.value = Double.parseDouble(t.getValue());
			} else if (t.getType() == Token.Type.NULL) {
				this.value = null;
			} else {
				throw new ParserException(t.getValue() + " should be a string or numeric");
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (minus) {
			sb.append("- ");
		}
		if (arithExpression != null) {
			sb.append("(");
			sb.append(arithExpression);
			sb.append(")");
		} else if (value != null) {
			sb.append(value);
		} else {
			sb.append("null");
		}
		return sb.toString();
	}

	public Object evaluate() throws ParserException {
		Object result = arithExpression == null ? value : arithExpression.evaluate();
		if (minus) {
			if (result instanceof Long) {
				result = -(Long) result;
			} else if (result instanceof Double) {
				result = -(Double) result;
			} else {
				throw new ParserException("Cannot negate " + result);
			}
		}
		return result;
	}
}
