package org.icatproject.ijp.client.parser;

import java.util.List;
import java.util.Map;

public class ExpressionEvaluator {

	public static boolean isTrue(String string, Map<String, Object> m) throws ParserException {
		try {
			List<Token> tokens = Tokenizer.getTokens(string);
			Input input = new Input(tokens);
			LogicalExpression exp = new LogicalExpression(input, m);
			if (input.peek(0) != null) {
				throw new ParserException("Trailing material after valid expression");
			}
//			System.out.println(exp);
			return exp.evaluate();
		} catch (LexerException e) {
			throw new ParserException("LexerException " + e.getMessage());
		}

	}
}
