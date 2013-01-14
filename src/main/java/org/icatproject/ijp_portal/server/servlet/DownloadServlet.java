package org.icatproject.ijp_portal.server.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rl.esc.catutils.CheckedProperties;
import uk.ac.rl.esc.catutils.CheckedProperties.CheckedPropertyException;
import org.icatproject.ijp_portal.shared.Constants;

/**
 * Provide download of datasets.
 */
@SuppressWarnings("serial")
@WebServlet(description = "Downloads datasets from the IDS", urlPatterns = { "/download/*" })
public class DownloadServlet extends HttpServlet {

	final static Logger logger = LoggerFactory.getLogger(DownloadServlet.class);
	private static final int BUFFER_SIZE = 2048; // set the same as the IDS for now
	private String idsUrlStemString;

	@Override
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);

		try {
			CheckedProperties portalProps = new CheckedProperties();
			portalProps.loadFromFile(Constants.PROPERTIES_FILEPATH);

			if (portalProps.has("javax.net.ssl.trustStore")) {
				System.setProperty("javax.net.ssl.trustStore",
						portalProps.getProperty("javax.net.ssl.trustStore"));
			}
			URL idsUrlStem = portalProps.getURL("ids.url");
			idsUrlStemString = idsUrlStem.toExternalForm();
		} catch (CheckedPropertyException e) {
			// get the error into portal.log and server.log
			String errorMsg = "Error initialising DownloadServlet: " + e.getMessage();
			logger.error(errorMsg);
			throw new ServletException(errorMsg);
		}

		logger.info("DownloadServlet started up");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {

		String sessionId = req.getParameter("sessionId");
		String datasetId = req.getParameter("datasetId");
		String datasetName = req.getParameter("datasetName");

		resp.setHeader("Content-disposition", "attachment;filename=" + datasetName + ".zip");
		resp.setContentType("application/octet-stream");

		OutputStream out = resp.getOutputStream();

		String idsUrlString = idsUrlStemString + "/ids2/Data/getDataset" +
							  "?sessionid=" + sessionId +
							  "&datasetId=" + datasetId;
		URL idsURL = new URL(idsUrlString);
		BufferedInputStream in = new BufferedInputStream(idsURL.openStream());
		int size;
		byte[] buffer = new byte[BUFFER_SIZE];
		while ((size = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
		    out.write(buffer, 0, size);
		}
		in.close();
		out.close();
		
	}
}
