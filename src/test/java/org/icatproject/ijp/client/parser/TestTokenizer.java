package org.icatproject.ijp.client.parser;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.icatproject.ijp.client.parser.LexerException;
import org.icatproject.ijp.client.parser.Token;
import org.icatproject.ijp.client.parser.Tokenizer;
import org.junit.Test;

public class TestTokenizer {

	@Test
	public void testGood1() throws Exception {
		List<Token> tokens = Tokenizer.getTokens("null investigation facility_user_id == ()");
		String[] tostrings = { "null", "investigation", "facility_user_id", "==", "(", ")" };
		assertEquals(tostrings.length, tokens.size());
		int i = 0;
		for (Token t : tokens) {
			assertEquals(tostrings[i++], t.toString());
		}
		assertEquals(Token.Type.NULL, tokens.get(0).getType());
		assertEquals(Token.Type.NAME, tokens.get(1).getType());
	}

	@Test
	public void testGood2() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("oR != <> > < 17 15. 17.0E-1 && || + - * / % -17 -17.1 -a");
		String[] tostrings = { "oR", "!=", "<", ">", ">", "<", "17", "15.0", "1.7", "&&", "||",
				"+", "-", "*", "/", "%", "-17", "-17.1", "-", "a" };
		assertEquals(tostrings.length, tokens.size());
		int i = 0;
		for (Token t : tokens) {
			assertEquals(tostrings[i++], t.toString());
		}
	}

	@Test
	public void testQuotes() throws Exception {
		List<Token> tokens = Tokenizer
				.getTokens("c=='aaa' && d=='bbb''qqq' && e == ' ' && f == '' || g == ''''''");
		String[] tostrings = { "c", "==", "aaa", "&&", "d", "==", "bbb'qqq", "&&", "e", "==", " ",
				"&&", "f", "==", "", "||", "g", "==", "''" };
		assertEquals(tostrings.length, tokens.size());
		int i = 0;
		for (Token t : tokens) {
			assertEquals(tostrings[i++], t.toString());
		}
	}

	@Test(expected = LexerException.class)
	public void testBad1() throws Exception {
		Tokenizer.getTokens("=");
	}

	@Test(expected = LexerException.class)
	public void testBad2() throws Exception {
		Tokenizer.getTokens("= ");
	}

	@Test(expected = LexerException.class)
	public void testBad3() throws Exception {
		Tokenizer.getTokens("'abcds''qwe ");
	}

}
