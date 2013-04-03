package org.icatproject.ijp_portal.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.icatproject.ijp_portal.shared.xmlmodel.JobType;
import org.junit.Test;

public class TestJobType {

	@Test
	public void testGood() throws Exception {

		Unmarshaller um = JAXBContext.newInstance(JobType.class).createUnmarshaller();
		JobType job = (JobType) um.unmarshal(new FileReader("ijp/job_types/ingest.xml"));

		assertTrue(job.isSessionId());
		assertEquals("ingest", job.getFamily());

	}
	
	@Test
	public void testGood2() throws Exception {

		Unmarshaller um = JAXBContext.newInstance(JobType.class).createUnmarshaller();
		JobType job = (JobType) um.unmarshal(new FileReader("ijp/job_types/date.xml"));

		assertFalse(job.isSessionId());
		assertNull(job.getFamily());

	}
}