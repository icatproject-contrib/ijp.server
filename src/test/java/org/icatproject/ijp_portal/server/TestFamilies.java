package org.icatproject.ijp_portal.server;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.icatproject.ijp_portal.server.Families;
import org.junit.Test;

public class TestFamilies {

	@Test
	public void testGood() throws Exception {

		Unmarshaller um = JAXBContext.newInstance(Families.class).createUnmarshaller();
		Families fams = (Families) um.unmarshal(new FileReader("ijp/families.xml"));

		assertEquals("batch", fams.getDefault());
		System.out.println(fams.getPuppet());

		Map<String,String> res = new HashMap<String, String>();
		for (Families.Family fam : fams.getFamily()) {
			res.put(fam.getName(), fam.getRE() == null?null:fam.getRE().toString());
		}
		assertEquals(null, res.get("batch"));
		assertEquals("(ingester)|(anotherIngester)", res.get("ingest"));

	}
}