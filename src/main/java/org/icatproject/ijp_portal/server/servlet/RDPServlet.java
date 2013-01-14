package org.icatproject.ijp_portal.server.servlet;

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

		// The same file name 'temp.rdp' is reused so try hard to avoid caching
		resp.setHeader("Cache-control", "no-cache, no-store");
		resp.setHeader("Pragma", "no-cache");
		resp.setHeader("Expires", "0");

		resp.setHeader("Content-disposition", "attachment;filename=LSF_remote_session.rdp");
		resp.setContentType("application/x-rdp");

		String hostName = req.getParameter("hostName");
		String accountName = req.getParameter("accountName");

		PrintWriter out = resp.getWriter();
		out.println("username:s:" + accountName);
		out.println("full address:s:" + hostName);
		out.close();

	}
}
