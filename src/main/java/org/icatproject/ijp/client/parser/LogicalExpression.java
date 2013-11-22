package org.icatproject.ijp.client.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogicalExpression {

	// LogicalExpression ::= BooleanTerm ( "||" BooleanTerm ) *

	public boolean evaluate() throws ParserException {
		for (BooleanTerm term : booleanTerms) {
			if (term.evaluate()) {
				return true;
			}
		}
		return false;
	}

	private List<BooleanTerm> booleanTerms = new ArrayList<BooleanTerm>();

	public LogicalExpression(Input input, Map<String, Object> m) throws ParserException {
		this.booleanTerms.add(new BooleanTerm(input, m));
		Token t = null;
		while ((t = input.peek(0)) != null) {
			if (t.getType() == Token.Type.OR) {
				input.consume();
				this.booleanTerms.add(new BooleanTerm(input, m));
			} else {
				return;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.booleanTerms.get(0));
		for (int i = 1; i < this.booleanTerms.size(); i++) {
			sb.append(" || ");
			sb.append(this.booleanTerms.get(i));
		}
		return sb.toString();
	}

}
