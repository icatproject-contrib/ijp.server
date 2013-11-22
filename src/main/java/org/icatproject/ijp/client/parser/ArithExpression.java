package org.icatproject.ijp.client.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArithExpression {

	// ArithExpression ::= Term ( "+"|"-" Term ) *

	private List<Term> terms = new ArrayList<Term>();
	private List<Token> ops = new ArrayList<Token>();

	private enum Mode {
		LONG, DOUBLE
	}

	public ArithExpression(Input input, Map<String, Object> m) throws ParserException {
		this.terms.add(new Term(input, m));
		Token t = null;
		while ((t = input.peek(0)) != null) {
			if (t.getType() == Token.Type.PLUMIN) {
				ops.add(input.consume());
				this.terms.add(new Term(input, m));
			} else {
				return;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(terms.get(0));
		for (int i = 1; i < terms.size(); i++) {
			sb.append(" " + this.ops.get(i - 1) + " ");
			sb.append(this.terms.get(i));
		}
		return sb.toString();
	}

	public Object evaluate() throws ParserException {
		Object value = terms.get(0).evaluate();
		if (terms.size() == 1) {
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
			throw new ParserException("Unexpected term for addition or subtraction: " + value);
		}
		for (int i = 1; i < terms.size(); i++) {
			value = terms.get(i).evaluate();
			String op = this.ops.get(i - 1).getValue();
			if (mode == Mode.LONG) {
				if (value instanceof Long) {
					if (op.equals("+")) {
						ltotal += (Long) value;
					} else {
						ltotal -= (Long) value;
					}
				} else if (value instanceof Double) {
					if (op.equals("+")) {
						dtotal = ltotal + (Double) value;
					} else {
						dtotal = ltotal - (Double) value;
					}
					mode = Mode.DOUBLE;
				} else {
					throw new ParserException("Unexpected term for addition or subtraction: "
							+ value);
				}
			} else {
				if ( value instanceof Double) {
					double dvalue = (Double) value;
					if (op.equals("+")) {
						dtotal += dvalue;
					} else {
						dtotal -= dvalue;
					}
				} else if (value instanceof Long) {
					double dvalue = ((Long) value).doubleValue();
					if (op.equals("+")) {
						dtotal += dvalue;
					} else {
						dtotal -= dvalue;
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
