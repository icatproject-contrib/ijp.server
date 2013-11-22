package org.icatproject.ijp.client.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Term {

	// Term ::= Factor ( "*"|"/"|"%" Factor ) *

	private List<Factor> factors = new ArrayList<Factor>();
	private List<Token> ops = new ArrayList<Token>();

	private enum Mode {
		LONG, DOUBLE
	}

	public Term(Input input, Map<String, Object> m) throws ParserException {
		factors.add(new Factor(input, m));
		Token t = null;
		while ((t = input.peek(0)) != null) {
			if (t.getType() == Token.Type.TIMESMULT) {
				ops.add(input.consume());
				factors.add(new Factor(input, m));
			} else {
				return;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.factors.get(0));
		for (int i = 1; i < this.factors.size(); i++) {
			sb.append(" " + this.ops.get(i - 1) + " ");
			sb.append(this.factors.get(i));
		}
		return sb.toString();
	}

	public Object evaluate() throws ParserException {
		
		Object value = factors.get(0).evaluate();
		if (factors.size() == 1) {
			return value;
		}
		
		long ltotal = 0;
		double dtotal = 0;
		Mode mode = null;
		if (value instanceof Long) {
			ltotal = (Long) value;
			mode = Mode.LONG;
		} else if (value instanceof Double) {
			dtotal = (Double) value;
			mode = Mode.DOUBLE;
		} else {
			throw new ParserException("Unexpected term for * / or % operations " + value + " of type " + value.getClass());
		}
		for (int i = 1; i < factors.size(); i++) {
			value = factors.get(i).evaluate();
			String op = this.ops.get(i - 1).getValue();
			if (mode == Mode.LONG) {
				if (value instanceof Long) {
					if (op.equals("*")) {
						ltotal *= (Long) value;
					} else if (op.equals("/")) {
						ltotal /= (Long) value;
					} else {
						ltotal %= (Long) value;
					}
				} else if (value instanceof Double) {
					if (op.equals("*")) {
						dtotal = ltotal * (Double) value;
					} else if (op.equals("/")) {
						dtotal = ltotal / (Double) value;
					} else {
						dtotal = ltotal % (Double) value;
					}
					mode = Mode.DOUBLE;
				} else {
					throw new ParserException("Unexpected term for addition or subtraction: "
							+ value);
				}
			} else {
				if (value instanceof Long || value instanceof Double) {
					if (op.equals("*")) {
						dtotal *= (Double) value;
					} else if (op.equals("/")) {
						dtotal /= (Double) value;
					} else {
						dtotal %= (Double) value;
					}
				} else {
					throw new ParserException("Unexpected term for addition or subtraction: "
							+ value);
				}
			}

		}
		return mode == Mode.LONG ? ltotal : dtotal;
	}

}
