package org.icatproject.ijp_portal.server.ejb.session;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.icatproject.ijp_portal.server.ejb.session.JobManagementBean;
import org.junit.Test;

public class TestJobManagementBean {
	
	private static String sq = "\"'\"";

	@Test
	public void testSimple() throws Exception {
		assertEquals("'a' 'b' '1'", JobManagementBean.escaped(Arrays.asList("a", "b", "1")));
	}
	
	@Test
	public void testOneQuote() throws Exception {
		assertEquals("'a' 'o'" + sq + "'brien' '1'", JobManagementBean.escaped(Arrays.asList("a", "o'brien", "1")));
	}
	
	@Test
	public void testOneQuoteTwice() throws Exception {
		assertEquals("'a' 'o'" + sq + "'brie'" + sq + "'n' '1'", JobManagementBean.escaped(Arrays.asList("a", "o'brie'n", "1")));
	}
	
	@Test
	public void testDoubleQuote() throws Exception {
		assertEquals("'a' 'o'" + sq + sq + "'brien' '1'", JobManagementBean.escaped(Arrays.asList("a", "o''brien", "1")));
	}
	
	@Test
	public void testJustThreeQuotes() throws Exception {
		assertEquals("'a' " + sq + sq + sq + " '1'", JobManagementBean.escaped(Arrays.asList("a", "'''", "1")));
	}
}