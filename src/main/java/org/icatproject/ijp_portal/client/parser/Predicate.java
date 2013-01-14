package org.icatproject.ijp_portal.client.parser;

import java.util.Map;

public class Predicate {

	// Predicate ::= True | False | ( ArithExpression COMPOP ArithExpression )

	private Boolean torf;
	private ArithExpression left;
	private Token compop;
	private ArithExpression right;

	public Predicate(Input input, Map<String, Object> m) throws ParserException {
		Token t = input.peek(0);
		if (t.getType().equals(Token.Type.BOOLEAN_LITERAL)) {
			torf = t.getValue().equals("True");
			input.consume();
		} else {
			left = new ArithExpression(input, m);
			compop = input.consume(Token.Type.COMPOP);
			right = new ArithExpression(input, m);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (torf != null) {
			if (torf) {
				sb.append("True");
			} else {
				sb.append("False");
			}
		} else {
			sb.append(left);
			sb.append(" " + compop + " ");
			sb.append(right);
		}
		return sb.toString();
	}

	public boolean evaluate() throws ParserException {
		if (torf != null) {
			return torf;
		}
		
		Object leftVal = left == null? null:left.evaluate();
		Object rightVal = right == null? null :right.evaluate();
	
		if (compop.getValue().equals("==")) {
			if (leftVal == null) {
				return rightVal == null;
			}
			if (rightVal == null) {
				return leftVal == null;
			}
			return leftVal.equals(rightVal);
		}
		if (compop.getValue().equals("!=")) {
			if (leftVal == null) {
				return rightVal != null;
			}
			if (rightVal == null) {
				return leftVal != null;
			}
			return !leftVal.equals(rightVal);
		}
		if (left == null || right == null) {
			throw new ParserException("null may only be compared with '==' and '!=' operators");
		}

		if (leftVal instanceof Long && rightVal instanceof Long) {
			if (compop.getValue().equals(">=")) {
				return (Long) leftVal >= (Long) rightVal;
			}
			if (compop.getValue().equals(">")) {
				return (Long) leftVal > (Long) rightVal;
			}
			if (compop.getValue().equals("<")) {
				return (Long) leftVal < (Long) rightVal;
			}
			if (compop.getValue().equals("<=")) {
				return (Long) leftVal <= (Long) rightVal;
			}
		} else {
			double dleft = 0;
			double dright = 0;
			try {
				dleft = leftVal instanceof Double ? (Double)leftVal : ((Long)leftVal).doubleValue();
				dright = rightVal instanceof Double ? (Double)rightVal : ((Long)rightVal).doubleValue();
			} catch (ClassCastException e) {
				throw new ParserException("Only numbers can be compared with '>, >=, < and <='");
			}
			if (compop.getValue().equals(">=")) {
				return dleft >= dright;
			}
			if (compop.getValue().equals(">")) {
				return dleft > dright;
			}
			if (compop.getValue().equals("<")) {
				return dleft < dright;
			}
			if (compop.getValue().equals("<=")) {
				return dleft <= dright;
			}
		}
		throw new ParserException("This cannot happen");
	}
}
