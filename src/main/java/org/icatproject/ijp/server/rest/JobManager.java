package org.icatproject.ijp.server.rest;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.icatproject.ijp.server.ejb.session.JobManagementBean;
import org.icatproject.ijp.server.manager.XmlFileManager;
import org.icatproject.ijp.shared.ForbiddenException;
import org.icatproject.ijp.shared.InternalException;
import org.icatproject.ijp.shared.ParameterException;
import org.icatproject.ijp.shared.PortalUtils.OutputType;
import org.icatproject.ijp.shared.SessionException;
import org.icatproject.ijp.shared.xmlmodel.JobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Path("jm")
public class JobManager {

	private final static Logger logger = LoggerFactory.getLogger(JobManager.class);

	@EJB
	private JobManagementBean jobManagementBean;

	private Map<String, JobType> jobTypes;

	@POST
	@Path("cancel/{jobId}")
	public void cancel(@PathParam("jobId") long id, @FormParam("sessionId") String sessionId) throws SessionException,
			ForbiddenException, InternalException, ParameterException {
		jobManagementBean.cancel(sessionId, id);
	}

	@DELETE
	@Path("delete/{jobId}")
	public void delete(@PathParam("jobId") long id, @QueryParam("sessionId") String sessionId) throws SessionException,
			ForbiddenException, InternalException, ParameterException {
		jobManagementBean.delete(sessionId, id);
	}

	@GET
	@Path("error/{jobId}")
	public String getError(@PathParam("jobId") long id, @QueryParam("sessionId") String sessionId)
			throws SessionException, ForbiddenException, InternalException, ParameterException {
		return jobManagementBean.getJobOutput(sessionId, id, OutputType.ERROR_OUTPUT);
	}

	@GET
	@Path("jobtype")
	public String getJobType() {
		return jobManagementBean.getHelp();
	}

	@GET
	@Path("jobtype/{jobType}")
	public String getJobType(@PathParam("jobType") String jobType) {
		try {
			return jobManagementBean.getHelp(jobType);
		} catch (ParameterException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(e.getMessage() + "\n").build());
		}
	}

	@GET
	@Path("output/{jobId}")
	public String getOutput(@PathParam("jobId") long id, @QueryParam("sessionId") String sessionId)
			throws SessionException, ForbiddenException, InternalException, ParameterException {
		return jobManagementBean.getJobOutput(sessionId, id, OutputType.STANDARD_OUTPUT);
	}

	@GET
	@Path("status")
	public String getStatus(@QueryParam("sessionId") String sessionId) throws SessionException, InternalException,
			ForbiddenException, ParameterException {
		return jobManagementBean.listStatus(sessionId);
	}

	@GET
	@Path("status/{jobId}")
	public String getStatus(@PathParam("jobId") long jobId, @QueryParam("sessionId") String sessionId)
			throws SessionException, ForbiddenException, InternalException, ParameterException {
		return jobManagementBean.getStatus(jobId, sessionId);
	}

	@PostConstruct
	void init() {
		try {
			XmlFileManager xmlFileManager = new XmlFileManager();
			jobTypes = xmlFileManager.getJobTypeMappings().getJobTypesMap();
			logger.info("Initialised JobManager");
		} catch (Exception e) {
			String msg = e.getClass().getName() + " reports " + e.getMessage();
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	@POST
	@Path("submit")
	public String submit(@FormParam("jobName") String jobName, @FormParam("parameter") List<String> parameters,
			@FormParam("sessionId") String sessionId) throws ForbiddenException, ParameterException, InternalException,
			SessionException {

		if (jobName == null) {
			throw new ParameterException("No jobName was specified");
		}

		JobType jobType = jobTypes.get(jobName);
		if (jobType == null) {
			throw new ParameterException("jobName " + jobName + " not recognised");
		}
		String type = jobType.getType();
		if (type == null) {
			throw new InternalException("XML describing job type does not include the type field");
		}
		if (type.equals("interactive")) {
			String json = jobManagementBean.submitInteractive(sessionId, jobType, parameters);
			try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
				JsonObject rdp = jsonReader.readObject().getJsonObject("rdp");
				if (rdp != null) {
					String host = rdp.getString("host");
					String account = rdp.getString("username");
					String password = rdp.getString("password");
					return "rdesktop -u " + account + " -p " + password + " " + host;
				} else {
					throw new InternalException("Bad response from batch service " + json);
				}
			} catch (JsonException e) {
				throw new InternalException("Bad response from batch service " + json);
			}

		} else if (type.equals("batch")) {
			return Long.toString(jobManagementBean.submitBatch(sessionId, jobType, parameters));
		} else {
			throw new InternalException("XML describing job '" + jobName + "' has a type field with an invalid value '"
					+ jobType.getType() + "'");
		}
	}

}