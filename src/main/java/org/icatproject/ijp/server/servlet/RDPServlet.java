package org.icatproject.ijp.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide rdp files.
 */
@SuppressWarnings("serial")
@WebServlet(description = "Return an rdp file", urlPatterns = { "/rdp/*" })
public class RDPServlet extends HttpServlet {

	final static Logger logger = LoggerFactory.getLogger(RDPServlet.class);

	@Override
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);

		logger.info("RDPServlet started up");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {

		// the same file name is reused so try hard to avoid caching
		// NOTE: previously the following were being set
//		resp.setHeader("Cache-control", "no-cache, no-store");
//		resp.setHeader("Pragma", "no-cache");
//		resp.setHeader("Expires", "0");
		// but on IE8 I was getting an error saying
		// "Unable to download <filename> from <server>"
		// "Internet Explorer was unable to open this site. The requested site is"
		// "either unavailable or cannot be found. Please try again later."
		// I found the suggestion of Cache-Control: private at the following URL
		// http://biostall.com/how-to-fix-ie8-error-unable-to-download-x-php-from-yoursite-com
		resp.setHeader("Cache-Control", "private");

		// BR, 2016-07-26: CORS headers added for topcat GUI
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
		resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD");
		
		resp.setHeader("Content-disposition", "attachment;filename=LSF_remote_session.rdp");
		// I couldn't find a definitive answer to whether the mime type should be rdp or x-rdp
		// both seem to work but x-rdp seems to be mentioned more in the searches I did
		resp.setContentType("application/x-rdp");

		String hostName = req.getParameter("hostName");
		String accountName = req.getParameter("accountName");

		PrintWriter out = resp.getWriter();
		out.println("username:s:" + accountName);
		out.println("full address:s:" + hostName);
		out.close();

	}
}
