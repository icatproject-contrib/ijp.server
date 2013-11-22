package org.icatproject.ijp.client.parser;

import java.util.Map;

public class BooleanFactor {

	private boolean not;
	private Predicate predicate;
	private LogicalExpression logicalExpression;

	// BooleanFactor ::= ("!")? Predicate | ( "(" LogicalExpression ")" )

	public BooleanFactor(Input input, Map<String, Object> m) throws ParserException {
		Token t = input.peek(0);
		if (t.getType() == Token.Type.NOT) {
			input.consume();
			this.not = true;
		}

		t = input.peek(0);
		if (t.getType() == Token.Type.OPENPAREN) {
			input.consume();
			this.logicalExpression = new LogicalExpression(input, m);
			input.consume(Token.Type.CLOSEPAREN);
		} else {
			this.predicate = new Predicate(input,m);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.not) {
			sb.append("! ");
		}
		if (this.predicate != null) {
			sb.append(predicate);
		} else {
			sb.append("(");
			sb.append(logicalExpression);
			sb.append(")");
		}
		return sb.toString();
	}

	public boolean evaluate() throws ParserException {
		boolean result = this.predicate == null? logicalExpression.evaluate() : predicate.evaluate();
		if (not) result = ! result;
		return result;
	}
}
