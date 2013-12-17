package org.icatproject.ijp.server.rest;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.DELETE;
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
import org.icatproject.ijp.shared.AccountDTO;
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
	public String cancel(@PathParam("jobId") String jobId, @QueryParam("sessionId") String sessionId) {

		checkCredentials(sessionId);
		try {
			return jobManagementBean.cancel(sessionId, jobId);
		} catch (SessionException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		} catch (ForbiddenException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		} catch (InternalException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage() + "\n").build());
		}
	}

	private void checkCredentials(String sessionId) {
		if (sessionId == null) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity("No sessionId was specified\n").build());
		}
	}

	@DELETE
	@Path("delete/{jobId}")
	public String delete(@PathParam("jobId") String jobId, @QueryParam("sessionId") String sessionId) {
		checkCredentials(sessionId);
		try {
			return jobManagementBean.delete(sessionId, jobId);
		} catch (SessionException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		} catch (ForbiddenException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		} catch (InternalException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage() + "\n").build());
		} catch (ParameterException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity(e.getMessage() + "\n").build());
		}

	}

	@GET
	@Path("error/{jobId}")
	public String getError(@PathParam("jobId") String jobId,
			@QueryParam("sessionId") String sessionId) {

		checkCredentials(sessionId);

		try {
			return jobManagementBean.getJobOutput(sessionId, jobId, OutputType.ERROR_OUTPUT);
		} catch (SessionException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		} catch (ForbiddenException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		} catch (InternalException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage() + "\n").build());
		}
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
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity(e.getMessage() + "\n").build());
		}
	}

	@GET
	@Path("output/{jobId}")
	public String getOutput(@PathParam("jobId") String jobId,
			@QueryParam("sessionId") String sessionId) {

		checkCredentials(sessionId);

		try {
			return jobManagementBean.getJobOutput(sessionId, jobId, OutputType.STANDARD_OUTPUT);
		} catch (SessionException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		} catch (ForbiddenException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		} catch (InternalException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getClass() + " reports " + e.getMessage() + "\n").build());
		}
	}

	@GET
	@Path("status")
	public String getStatus(@QueryParam("sessionId") String sessionId) {
		checkCredentials(sessionId);
		try {
			return jobManagementBean.listStatus(sessionId);
		} catch (SessionException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		}
	}

	@GET
	@Path("status/{jobId}")
	public String getStatus(@PathParam("jobId") String jobId,
			@QueryParam("sessionId") String sessionId) {

		checkCredentials(sessionId);
		try {
			return jobManagementBean.getStatus(jobId, sessionId);
		} catch (SessionException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		} catch (ForbiddenException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		}
	}

	@PostConstruct
	void init() {
		try {
			XmlFileManager xmlFileManager = new XmlFileManager();
			jobTypes = xmlFileManager.getJobTypeMappings().getJobTypesMap();
			logger.debug("Initialised JobManagementBean");
		} catch (Exception e) {
			String msg = e.getClass().getName() + " reports " + e.getMessage();
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	@POST
	@Path("submit")
	public String submit(@QueryParam("jobName") String jobName,
			@QueryParam("parameter") List<String> parameters,
			@QueryParam("sessionId") String sessionId) {

		checkCredentials(sessionId);
		try {
			logger.debug("submit: " + jobName + " with parameters " + parameters
					+ " under sessionId " + sessionId);

			if (jobName == null) {
				throw new ParameterException("No jobName was specified");
			}

			JobType jobType = jobTypes.get(jobName);
			if (jobType == null) {
				throw new ParameterException("jobName " + jobName + " not recognised");
			}
			String type = jobType.getType();
			if (type == null) {
				throw new InternalException(
						"XML describing job type does not include the type field");
			}
			if (type.equals("interactive")) {
				AccountDTO accountDTO = jobManagementBean.submitInteractive(sessionId, jobType,
						parameters);
				return "rdesktop -u " + accountDTO.getAccountName() + " -p "
						+ accountDTO.getPassword() + " " + accountDTO.getHostName();
			} else if (type.equals("batch")) {
				return jobManagementBean.submitBatch(sessionId, jobType, parameters);
			} else {
				throw new InternalException("XML describing job '" + jobName
						+ "' has a type field with an invalid value '" + jobType.getType() + "'");
			}
		} catch (InternalException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage() + "\n").build());
		} catch (SessionException e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity(e.getMessage() + "\n").build());
		} catch (ParameterException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity(e.getMessage() + "\n").build());
		}

	}

}