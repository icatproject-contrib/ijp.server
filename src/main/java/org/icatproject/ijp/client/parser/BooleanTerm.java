package org.icatproject.ijp.client.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BooleanTerm {

	// BooleanTerm ::= BooleanFactor ( "&&" BooleanFactor ) *

	private List<BooleanFactor> factors = new ArrayList<BooleanFactor>();

	public BooleanTerm(Input input, Map<String, Object> m) throws ParserException {
		this.factors.add(new BooleanFactor(input, m));
		Token t = null;
		while ((t = input.peek(0)) != null) {
			if (t.getType() == Token.Type.AND) {
				input.consume();
				this.factors.add(new BooleanFactor(input, m));
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
			sb.append(" && ");
			sb.append(this.factors.get(i));
		}
		return sb.toString();
	}

	public boolean evaluate() throws ParserException {
		for (BooleanFactor term : factors) {
			if (!term.evaluate()) {
				return false;
			}
		}
		return true;
	}

}
