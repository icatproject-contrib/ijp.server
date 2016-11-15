package org.icatproject.ijp.server.rest;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
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

import org.icatproject.ids.client.NotFoundException;
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

	/**
	 * Cancels a job.
	 * 
	 * @summary cancel
	 * 
	 * @param jobId the id of the job to be cancelled
	 * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * 
	 * @throws SessionException
	 * @throws ForbiddenException
	 * @throws InternalException
	 * @throws ParameterException
	 */
	@POST
	@Path("cancel/{jobId}")
	public void cancel(@PathParam("jobId") long jobId, @FormParam("sessionId") String sessionId) throws SessionException,
			ForbiddenException, InternalException, ParameterException {
		jobManagementBean.cancel(sessionId, jobId);
	}

	/**
	 * Deletes a job, removing it from the IJP's job status list.
	 * 
	 * @summary delete
	 * 
	 * @param jobId id of the job to be deleted
	 * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 *
	 * @throws SessionException
	 * @throws ForbiddenException
	 * @throws InternalException
	 * @throws ParameterException
	 */
	@DELETE
	@Path("delete/{jobId}")
	public void delete(@PathParam("jobId") long jobId, @QueryParam("sessionId") String sessionId) throws SessionException,
			ForbiddenException, InternalException, ParameterException {
		jobManagementBean.delete(sessionId, jobId);
	}

	/**
	 * Returns the error output for the specified job.
	 * 
	 * @summary error
	 * 
	 * @param jobId id of the job whose error output is requested
	 * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @return a JSON object with an 'output' field that contains the error output for the specified job.
	 * @throws SessionException
	 * @throws ForbiddenException
	 * @throws InternalException
	 * @throws ParameterException
	 */
	@GET
	@Path("error/{jobId}")
	public String getError(@PathParam("jobId") long jobId, @QueryParam("sessionId") String sessionId)
			throws SessionException, ForbiddenException, InternalException, ParameterException {
		String output = jobManagementBean.getJobOutput(sessionId, jobId, OutputType.ERROR_OUTPUT);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Json.createGenerator(baos).writeStartObject().write("output", output).writeEnd().close();
		return baos.toString();
	}

	/**
	 * Gets a description of all available job types.
	 * 
	 * @summary jobtype (all)
	 * 
	 * @return JSON array of jobtype descriptions.
	 */
	@GET
	@Path("jobtype")
	public String getJobType() {
		return jobManagementBean.getJobTypeNames();
	}

	/**
	 * Gets a description of the named job type.
	 * 
	 * @summary jobtype (single)
	 * 
	 * @param jobType the name of the job type.
	 * @return JSON object describing the named job type.
	 */
	@GET
	@Path("jobtype/{jobType}")
	public String getJobType(@PathParam("jobType") String jobType) {
		try {
			return jobManagementBean.getJobTypeJson(jobType);
		} catch (ParameterException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(e.getMessage() + "\n").build());
		}
	}

	/**
	 * Returns the (standard) output for the specified job.
	 * 
	 * @summary output
	 * 
	 * @param jobId id of the job whose output is requested
	 * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @return a JSON object with an 'output' field that contains the (standard) output for the specified job.
	 * 
	 * @throws SessionException
	 * @throws ForbiddenException
	 * @throws InternalException
	 * @throws ParameterException
	 */
	@GET
	@Path("output/{jobId}")
	public String getOutput(@PathParam("jobId") long jobId, @QueryParam("sessionId") String sessionId)
			throws SessionException, ForbiddenException, InternalException, ParameterException {
		String output = jobManagementBean.getJobOutput(sessionId, jobId, OutputType.STANDARD_OUTPUT);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Json.createGenerator(baos).writeStartObject().write("output", output).writeEnd().close();
		return baos.toString();
	}

	/**
	 * Gets a status report for all known jobs.
	 * 
	 * @summary status (all jobs)
	 * 
	 * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @return a JSON array of job-status objects.
	 * 
	 * @throws SessionException
	 * @throws InternalException
	 * @throws ForbiddenException
	 * @throws ParameterException
	 */
	@GET
	@Path("status")
	public String getStatus(@QueryParam("sessionId") String sessionId) throws SessionException, InternalException,
			ForbiddenException, ParameterException {
		return jobManagementBean.listStatus(sessionId);
	}

	/**
	 * Gets the status of the identified job.
	 * 
	 * @summary status (single job)
	 * 
	 * @param jobId id of the job
	 * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @return JSON describing the status of the identified job.
	 * 
	 * @throws SessionException
	 * @throws ForbiddenException
	 * @throws InternalException
	 * @throws ParameterException
	 */
	@GET
	@Path("status/{jobId}")
	public String getStatus(@PathParam("jobId") long jobId, @QueryParam("sessionId") String sessionId)
			throws SessionException, ForbiddenException, InternalException, ParameterException {
		return jobManagementBean.getStatus(jobId, sessionId);
	}

	/**
	 * Submits a job of the given job-type name with the given parameters.
	 * 
	 * @summary submit
	 * 
	 * @param jobName the name of the job type to be submitted
	 * @param parameters list of (command-line) parameter strings to be passed to the job.
	 * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @return JSON object containing the jobId, e.g. <code>{"jobId":1234}</code>
	 * 
	 * @throws ForbiddenException
	 * @throws ParameterException
	 * @throws InternalException
	 * @throws SessionException
	 */
	@POST
	@Path("submit")
	public String submit(@FormParam("jobName") String jobName, @FormParam("parameter") List<String> parameters,
			@FormParam("sessionId") String sessionId) throws ForbiddenException, ParameterException, InternalException,
			SessionException {

		if (jobName == null) {
			throw new ParameterException("No jobName was specified");
		}

		return jobManagementBean.submit(sessionId, jobName, parameters);
	}

	/**
	 * Provides the provenance information for the given job.
	 * @param jobId The given job.
	 * @param provenanceId The provenance record ID in ICAT.
	 * @param sessionId a valid session id which takes the form <code>0d9a3706-80d4-4d29-9ff3-4d65d4308a24</code>
	 * @return JSON object containing the jobId and the provenance record, e.g. <code>{"jobId": 1234,
	 * "provenanceId": }</code>
	 */
	@POST
	@Path("job/{jobId}")
	public String setProvenance(@PathParam("jobId") long jobId,
				@FormParam("provenanceId") long provenanceId,
				@FormParam("sessionId") String sessionId)
			throws NotFoundException, ForbiddenException, SessionException {

		return jobManagementBean.saveProvenanceId(sessionId, jobId, provenanceId);
	}

}