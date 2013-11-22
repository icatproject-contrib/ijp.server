package org.icatproject.ijp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.icatproject.ijp.server.Pbs;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPbs {

	private static Pbs pbs;

	@Test
	public void testStates() throws Exception {
		Map<String, String> states = pbs.getStates();
		assertTrue(states.size() > 0);
		assertEquals("down", states.get("sig-04.esc.rl.ac.uk"));
		assertEquals("job-exclusive", states.get("sig-05.esc.rl.ac.uk"));
		assertEquals("free", states.get("sig-06.esc.rl.ac.uk"));
	}

	@Test
	public void testOffline() throws Exception {
		pbs.setOffline("sig-04.esc.rl.ac.uk");
	}

	@Test
	public void testOnline() throws Exception {
		pbs.setOnline("rclsfserv004.rc-harwell.ac.uk");
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		pbs = new Pbs();
	}
}