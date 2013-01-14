package org.icatproject.ijp_portal.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.icatproject.ijp_portal.server.Qstat;
import org.junit.Test;

public class TestQstat {

	@Test
	public void testGood() throws Exception {

		Unmarshaller um = JAXBContext.newInstance(Qstat.class).createUnmarshaller();
		Qstat qstat = (Qstat) um.unmarshal(new FileReader("src/test/resources/qstat.xml"));
		List<Qstat.Job> jobs = qstat.getJobs();
		assertEquals(2, jobs.size());

		for (Qstat.Job job : jobs) {
			if (job.getJobId().equals("84.cloud068.gridpp.rl.ac.uk")) {
				assertEquals("zgeyhfxalu.sh", job.getBatchFilename());
				assertEquals("C", job.getStatus());
				assertEquals("cloud073.gridpp.rl.ac.uk/0", job.getWorkerNode());
			} else {
				assertEquals("qxiqvwspxh.sh", job.getBatchFilename());
				assertEquals("Q", job.getStatus());
				assertNull(job.getWorkerNode());
			}
		}
	}
}