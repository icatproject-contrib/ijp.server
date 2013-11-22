package org.icatproject.ijp.client.parser;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.icatproject.ijp.client.parser.ExpressionEvaluator;
import org.icatproject.ijp.client.parser.ParserException;
import org.junit.Test;

public class TestSearch {

	@Test
	public void testGood() throws Exception {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("age", 25);

		assertEquals(Boolean.TRUE, ExpressionEvaluator.isTrue("age > 21  && True", m));

		m.put("age", 19);
		assertEquals(Boolean.FALSE, ExpressionEvaluator.isTrue("age > 21  && True", m));

		m.put("float", 19.0);
		m.put("double", 29.0D);
		m.put("integer", 39);
		m.put("long", 49L);

		assertEquals(Boolean.TRUE,
				ExpressionEvaluator.isTrue("float + double + integer + long > 135", m));
		assertEquals(Boolean.TRUE,
				ExpressionEvaluator.isTrue("integer + double + float + long < 137", m));
		assertEquals(Boolean.TRUE,
				ExpressionEvaluator.isTrue("integer + double + float + long == 136.0", m));

		m.put("nullvalue", null);
		assertEquals(Boolean.TRUE, ExpressionEvaluator.isTrue("nullvalue == null", m));
		assertEquals(Boolean.TRUE, ExpressionEvaluator.isTrue("double != null", m));

		m.put("stringvalue", "aardvark");
		m.put("nullStringvalue", null);

		assertEquals(Boolean.TRUE, ExpressionEvaluator.isTrue("nullStringvalue == null", m));
		assertEquals(Boolean.TRUE, ExpressionEvaluator.isTrue("stringvalue != null", m));
		assertEquals(Boolean.FALSE, ExpressionEvaluator.isTrue("nullStringvalue != null", m));
		assertEquals(Boolean.FALSE, ExpressionEvaluator.isTrue("stringvalue == null", m));
	}

	@Test(expected = ParserException.class)
	public void testBad1() throws Exception {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("age", 25);
		ExpressionEvaluator.isTrue("ag > 21", m);
	}

	@Test(expected = ParserException.class)
	public void testBad2() throws Exception {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("age", "25");
		ExpressionEvaluator.isTrue("age > 21", m);
	}

	@Test(expected = ParserException.class)
	public void testBad3() throws Exception {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("age", 25);
		ExpressionEvaluator.isTrue("age > 21   junk", m);
	}

}