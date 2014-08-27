package org.icatproject.ijp.server.ejb.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.icatproject.ICAT;
import org.icatproject.IcatException_Exception;
import org.icatproject.ijp.server.Families;
import org.icatproject.ijp.server.Icat;
import org.icatproject.ijp.server.ejb.entity.Job;
import org.icatproject.ijp.server.manager.XmlFileManager;
import org.icatproject.ijp.shared.AccountDTO;
import org.icatproject.ijp.shared.Constants;
import org.icatproject.ijp.shared.ForbiddenException;
import org.icatproject.ijp.shared.InternalException;
import org.icatproject.ijp.shared.ParameterException;
import org.icatproject.ijp.shared.PortalUtils.OutputType;
import org.icatproject.ijp.shared.SessionException;
import org.icatproject.ijp.shared.xmlmodel.JobType;
import org.icatproject.utils.CheckedProperties;
import org.icatproject.utils.ShellCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session Bean implementation to manage job status
 */
@Stateless
public class JobManagementBean {

	public class Estimator implements Runnable {

		private String sessionId;
		private JobType jobType;
		private List<String> parameters;
		private Entry<String, WebTarget> entry;

		private Response response;

		public Estimator(String sessionId, JobType jobType, List<String> parameters,
				Entry<String, WebTarget> entry) {
			this.sessionId = sessionId;
			this.jobType = jobType;
			this.parameters = parameters;
			this.entry = entry;
		}

		@Override
		public void run() {
			WebTarget server = entry.getValue();
			response = server.path("estimate").queryParam("sessionId", sessionId)
					.queryParam("jobType", jobType).queryParam("parameters", parameters)
					.request(MediaType.TEXT_PLAIN).get(Response.class);
		}

		/** returns null in case of error */
		public Integer getTime() {
			String json = null;
			try {
				json = (String) processResponse(response);
				try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
					return jsonReader.readObject().getInt("time");
				}
			} catch (Exception e) {
				logger.error("Bad response from batch service " + json);
				return null;
			}

		}

	}

	private ICAT icat;

	private String defaultFamily;
	private Map<String, Pattern> familyPatterm = new HashMap<>();

	private Map<String, JobType> jobTypes;

	private Client client;

	private Map<String, WebTarget> batchServers = new HashMap<>();

	@PostConstruct
	private void init() {
		try {
			icat = Icat.getIcat();

			Unmarshaller um = JAXBContext.newInstance(Families.class).createUnmarshaller();
			Families fams = (Families) um.unmarshal(new FileReader(Constants.CONFIG_SUBDIR
					+ "/families.xml"));

			defaultFamily = fams.getDefault();
			for (Families.Family fam : fams.getFamily()) {
				familyPatterm.put(fam.getName(), fam.getRE());
			}

			XmlFileManager xmlFileManager = new XmlFileManager();
			jobTypes = xmlFileManager.getJobTypeMappings().getJobTypesMap();

			CheckedProperties props = new CheckedProperties();
			props.loadFromFile(Constants.PROPERTIES_FILEPATH);
			List<String> batchserverUrlstrings = new ArrayList<>(Arrays.asList(props.getString(
					"batchserverUrls").split("\\s+")));
			client = ClientBuilder.newClient();

			for (String batchserverUrlstring : batchserverUrlstrings) {
				batchServers.put(batchserverUrlstring,
						client.target(batchserverUrlstring + "/unixbatch"));
			}
			logger.debug("Initialised JobManagementBean");
		} catch (Exception e) {
			String msg = e.getClass().getName() + " reports " + e.getMessage();
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	@PreDestroy
	private void exit() {
		client.close();
		logger.debug("Closing down JobManagementBean");
	}

	private final static Logger logger = LoggerFactory.getLogger(JobManagementBean.class);

	private static final int maxSeconds = 5;

	@PersistenceContext(unitName = "ijp")
	private EntityManager entityManager;

	public List<Job> getJobsForUser(String sessionId) throws SessionException {
		String username = getUserName(sessionId);
		return entityManager.createNamedQuery(Job.FIND_BY_USERNAME, Job.class)
				.setParameter("username", username).getResultList();
	}

	public String getJobOutput(String sessionId, String jobId, OutputType outputType)
			throws SessionException, ForbiddenException, InternalException {
		Job job = getJob(sessionId, jobId);
		String ext = "." + (outputType == OutputType.STANDARD_OUTPUT ? "o" : "e")
				+ jobId.split("\\.")[0];
		Path path = FileSystems.getDefault().getPath("/home/batch/jobs",
				job.getBatchFilename() + ext);
		boolean delete = false;
		if (!Files.exists(path)) {
			logger.debug("Getting intermediate output for " + jobId);
			ShellCommand sc = new ShellCommand("sudo", "-u", "batch", "ssh", job.getWorkerNode(),
					"sudo", "push_output", job.getBatchUsername(), path.toFile().getName());
			if (sc.isError()) {
				return "Temporary? problem getting output " + sc.getStderr();
			}
			path = FileSystems.getDefault().getPath("/home/batch/jobs",
					job.getBatchFilename() + ext + "_tmp");
			delete = true;
		}
		if (Files.exists(path)) {
			logger.debug("Returning output for " + jobId);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				Files.copy(path, baos);
			} catch (IOException e) {
				throw new InternalException(e.getClass() + " reports " + e.getMessage());
			}
			if (delete) {
				try {
					Files.deleteIfExists(path);
				} catch (IOException e) {
					throw new InternalException("Unable to delete temporary file");
				}
			}
			return baos.toString();

		} else {
			throw new InternalException("No output file available at the moment");
		}
	}

	@Schedule(minute = "*/1", hour = "*")
	public void updateJobsFromQstat() {
		try {

			ShellCommand sc = new ShellCommand("qstat", "-x");
			if (sc.isError()) {
				throw new InternalException("Unable to query jobs via qstat " + sc.getStderr());
			}
			String jobsXml = sc.getStdout().trim();
			if (jobsXml.isEmpty()) {
				/* See if any jobs have completed without being noticed */
				for (Job job : entityManager.createNamedQuery(Job.FIND_INCOMPLETE, Job.class)
						.getResultList()) {
					logger.warn("Updating status of job '" + job.getId() + "' from '"
							+ job.getStatus() + "' to 'C' as not known to qstat");
					job.setStatus("C");
				}
				return;
			}

			// Qstat qstat = (Qstat) qstatUnmarshaller.unmarshal(new StringReader(jobsXml));
			// for (Qstat.Job xjob : qstat.getJobs()) {
			// String id = xjob.getJobId();
			// String status = xjob.getStatus();
			// String wn = xjob.getWorkerNode();
			// String workerNode = wn != null ? wn.split("/")[0] : "";
			// String comment = xjob.getComment() == null ? "" : xjob.getComment();
			//
			// Job job = entityManager.find(Job.class, id);
			// if (job != null) {/* Log updates on portal jobs */
			// if (!job.getStatus().equals(xjob.getStatus())) {
			// logger.debug("Updating status of job '" + id + "' from '" + job.getStatus()
			// + "' to '" + status + "'");
			// job.setStatus(status);
			// }
			// if (!job.getWorkerNode().equals(workerNode)) {
			// logger.debug("Updating worker node of job '" + id + "' from '"
			// + job.getWorkerNode() + "' to '" + workerNode + "'");
			// job.setWorkerNode(workerNode);
			// }
			// String oldComment = job.getComment() == null ? "" : job.getComment();
			// if (!oldComment.equals(comment)) {
			// logger.debug("Updating comment of job '" + id + "' from '" + oldComment
			// + "' to '" + comment + "'");
			// job.setComment(comment);
			// }
			// }
			// }
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			logger.error("Update of db jobs from qstat failed. Class " + e.getClass() + " reports "
					+ e.getMessage() + baos.toString());
		}
	}

	public String submitBatch(String sessionId, JobType jobType, List<String> parameters)
			throws SessionException, InternalException, ForbiddenException, ParameterException {
		String reqFamily = jobType.getFamily();
		String family = reqFamily == null ? defaultFamily : reqFamily;
		Pattern lf = familyPatterm.get(family);
		if (lf == null) {
			throw new ParameterException("Requested family " + reqFamily + " not known");
		}
		String username = getUserName(sessionId);

		if (lf != null && !lf.matcher(username).matches()) {
			throw new ForbiddenException(username + " is not allowed to use family " + family);
		}

		Entry<String, WebTarget> bestEntry;
		if (batchServers.size() == 1) {
			bestEntry = batchServers.entrySet().iterator().next();
		} else {
			Map<Thread, Estimator> threads = new HashMap<>();

			/* Send message to batch workers in parallel */
			for (Entry<String, WebTarget> entry : batchServers.entrySet()) {
				Estimator estimator = new Estimator(sessionId, jobType, parameters, entry);
				Thread thread = new Thread(estimator);
				thread.start();
				threads.put(thread, estimator);
			}
			int m = 0;
			while (true) {
				m++;
				int n = 0;
				int best = 0;
				Estimator bestEstimator = null;
				for (Entry<Thread, Estimator> threadEntry : threads.entrySet()) {
					Thread thread = threadEntry.getKey();
					Estimator estimator = threadEntry.getValue();
					if (!thread.isAlive()) {
						n++;
						Integer bestCandidiate = estimator.getTime();
						if (bestCandidiate != null) {
							if (bestEstimator == null || bestCandidiate < best) {
								bestEstimator = estimator;
								best = bestCandidiate;
							}
						}
					}
				}
				if (n == batchServers.size() || m > maxSeconds) {
					bestEntry = bestEstimator.entry;
					break;
				}
			}
		}

		Response response = bestEntry.getValue().path("submit").queryParam("sessionId", sessionId)
				.queryParam("jobType", jobType).queryParam("parameters", parameters)
				.request(MediaType.TEXT_PLAIN).get(Response.class);
		String json = (String) processResponse(response);
		try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
			return jsonReader.readObject().getString("jobId");
		} catch (JsonException e) {
			throw new InternalException("Bad response from batch service " + json);
		}

	}

	private Object processResponse(Response response) throws InternalException, ForbiddenException,
			ParameterException, SessionException {
		if (response.getStatus() / 200 != 2) {
			String code = null;
			String message = null;
			String result = (String) response.getEntity();
			try (JsonParser parser = Json.createParser(new ByteArrayInputStream(result.getBytes()))) {
				String key = null;
				while (parser.hasNext()) {
					JsonParser.Event event = parser.next();
					if (event == Event.KEY_NAME) {
						key = parser.getString();
					} else if (event == Event.VALUE_STRING || event == Event.VALUE_NUMBER) {
						if (key.equals("code")) {
							code = parser.getString();
						} else if (key.equals("message")) {
							message = parser.getString();
						} else {
							throw new InternalException(key + " is not an expected key in the json");
						}
					}
				}
			}
			if (code == null || message == null) {
				throw new InternalException("Values of code or message not specified in " + result);
			}
			if (code.equals("ForbiddenException")) {
				throw new ForbiddenException(message);
			} else if (code.equals("InternalException")) {
				throw new InternalException(message);
			} else if (code.equals("ParameterException")) {
				throw new ParameterException(message);
			} else if (code.equals("SessionException")) {
				throw new SessionException(message);
			}
		}
		return response.getEntity();
	}

	public AccountDTO submitInteractive(String sessionId, JobType jobType, List<String> parameters)
			throws InternalException {
		return null;
		// Path p = null; TODO
		// try {
		// p = Files.createTempFile(null, null);
		// } catch (IOException e) {
		// throw new InternalException("Unable to create a temporary file: " + e.getMessage());
		// }
		// File interactiveScriptFile = p.toFile();
		// createScript(interactiveScriptFile, parameters, jobType, sessionId);
		// Account account = machineEJB.prepareMachine(sessionId, jobType.getExecutable(),
		// parameters,
		// interactiveScriptFile);
		// return account.getDTO(machineEJB.getPoolPrefix());
	}

	private String getUserName(String sessionId) throws SessionException {
		try {
			return icat.getUserName(sessionId);
		} catch (IcatException_Exception e) {
			throw new SessionException("IcatException " + e.getFaultInfo().getType() + " "
					+ e.getMessage());
		}
	}

	public String listStatus(String sessionId) throws SessionException {
		String username = getUserName(sessionId);
		List<Job> jobs = entityManager.createNamedQuery(Job.FIND_BY_USERNAME, Job.class)
				.setParameter("username", username).getResultList();
		StringBuilder sb = new StringBuilder();
		for (Job job : jobs) {
			sb.append(job.getId() + ", " + job.getStatus() + "\n");
		}
		return sb.toString();

	}

	public String getStatus(String jobId, String sessionId) throws SessionException,
			ForbiddenException {
		Job job = getJob(sessionId, jobId);
		StringBuilder sb = new StringBuilder();
		sb.append("Id:                 " + job.getId() + "\n");
		sb.append("Status:             " + job.getStatus() + "\n");
		sb.append("Comment:            " + job.getComment() + "\n");
		sb.append("Date of submission: " + job.getSubmitDate() + "\n");
		sb.append("Node:               " + job.getWorkerNode() + "\n");
		return sb.toString();
	}

	private Job getJob(String sessionId, String jobId) throws SessionException, ForbiddenException {
		String username = getUserName(sessionId);
		Job job = entityManager.find(Job.class, jobId);
		if (job == null || !job.getUsername().equals(username)) {
			throw new ForbiddenException("Job does not belong to you");
		}
		return job;
	}

	public String delete(String sessionId, String jobId) throws SessionException,
			ForbiddenException, InternalException, ParameterException {
		Job job = getJob(sessionId, jobId);
		if (!job.getStatus().equals("C")) {
			throw new ParameterException(
					"Only completed jobs can be deleted - try cancelling first");
		}
		for (String oe : new String[] { "o", "e" }) {
			String ext = "." + oe + jobId.split("\\.")[0];
			Path path = FileSystems.getDefault().getPath("/home/batch/jobs",
					job.getBatchFilename() + ext);
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				throw new InternalException("Unable to delete " + path.toString());
			}
		}
		entityManager.remove(job);
		return "";
	}

	public String cancel(String sessionId, String jobId) throws SessionException,
			ForbiddenException, InternalException {
		Job job = getJob(sessionId, jobId);
		ShellCommand sc = new ShellCommand("qdel", job.getId());
		if (sc.isError()) {
			throw new InternalException("Unable to cancel job " + sc.getStderr());
		}
		return "";
	}

	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, JobType> entry : jobTypes.entrySet()) {
			if (sb.length() == 0) {
				sb.append("Available job types are:\n");
			} else {
				sb.append("\n");
			}
			JobType jt = entry.getValue();
			sb.append(jt.getName() + " is " + jt.getType());
		}
		return sb.toString();
	}

	public String getHelp(String jobType) throws ParameterException {
		JobType jt = jobTypes.get(jobType);
		if (jt == null) {
			throw new ParameterException("Job type " + jobType + " is not recognised.");
		}
		// TODO this is or the toString method need improvement - probably a special method such as
		// getHelp()
		return jt.toString();
	}
}
