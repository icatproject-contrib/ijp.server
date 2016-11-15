package org.icatproject.ijp.server.ejb.session;

import org.icatproject.ICAT;
import org.icatproject.IcatException_Exception;
import org.icatproject.ids.client.NotFoundException;
import org.icatproject.ijp.server.Families;
import org.icatproject.ijp.server.Icat;
import org.icatproject.ijp.server.ejb.entity.Job;
import org.icatproject.ijp.server.ejb.entity.Job.Status;
import org.icatproject.ijp.server.manager.XmlFileManager;
import org.icatproject.ijp.shared.*;
import org.icatproject.ijp.shared.PortalUtils.OutputType;
import org.icatproject.ijp.shared.xmlmodel.JobOption;
import org.icatproject.ijp.shared.xmlmodel.JobType;
import org.icatproject.utils.CheckedProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Session Bean implementation to manage job status
 */
@Stateless
public class JobManagementBean {

	public class Estimator implements Runnable {

		private String sessionId;
		private String executable;
		private List<String> parameters;
		private Entry<String, WebTarget> entry;

		private Response response;
		private boolean interactive;
		private String family;

		public Estimator(String sessionId, String executable, List<String> parameters, String family,
				boolean interactive, Entry<String, WebTarget> entry) {
			this.sessionId = sessionId;
			this.executable = executable;
			this.parameters = parameters;
			this.family = family;
			this.interactive = interactive;
			this.entry = entry;
		}

		@Override
		public void run() {
			WebTarget server = entry.getValue();
			response = server.path("estimate").queryParam("sessionId", sessionId).queryParam("executable", executable)
					.queryParam("parameters", parameters).queryParam("interactive", interactive)
					.queryParam("family", family).request(MediaType.APPLICATION_JSON).get(Response.class);
		}

		/* returns null in case of error */
		public Integer getTime() {
			String json = null;
			try {
				checkResponse(response);
				json = response.readEntity(String.class);
				try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
					return jsonReader.readObject().getInt("time");
				}
			} catch (Exception e) {
				logger.error("Bad response from batch service " + entry.getValue().getUri() + ": " + e.getClass() + " "
						+ e.getMessage() + " " + json);
				return null;
			}

		}

	}

	private ICAT icat;

	private String defaultFamily;
	private Map<String, Pattern> familyPattern = new HashMap<>();

	private Map<String, JobType> jobTypes;

	private Client client;

	private Map<String, WebTarget> batchServers = new HashMap<>();

	private String icatUrl;
	private String idsUrl;

	@PostConstruct
	private void init() {
		try {
			icat = Icat.getIcat();

			Unmarshaller um = JAXBContext.newInstance(Families.class).createUnmarshaller();
			Families fams = (Families) um.unmarshal(new FileReader(Constants.CONFIG_SUBDIR + "/families.xml"));

			defaultFamily = fams.getDefault();
			logger.info("Default family is " + defaultFamily);
			for (Families.Family fam : fams.getFamily()) {
				familyPattern.put(fam.getName(), fam.getRE());
				logger.info("Family " + fam.getName() + " accessible to " + fam.getRE());
			}

			XmlFileManager xmlFileManager = new XmlFileManager();
			jobTypes = xmlFileManager.getJobTypeMappings().getJobTypesMap();

			CheckedProperties props = new CheckedProperties();
			props.loadFromFile(Constants.PROPERTIES_FILEPATH);
			List<String> batchserverUrlstrings = new ArrayList<>(Arrays.asList(props.getString("batchserverUrls")
					.split("\\s+")));
			client = ClientBuilder.newClient();

			for (String batchserverUrlstring : batchserverUrlstrings) {
				batchServers.put(batchserverUrlstring, client.target(batchserverUrlstring + "/batch"));
			}

			icatUrl = props.getString("icat.url");
			idsUrl = props.getString("ids.url");

			logger.info("Initialised JobManagementBean");
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
	
	/**
	 * Reload the JobTypes to pick up any changes.
	 */
	@Schedule(minute = "*/1", hour = "*")
	private void refreshJobTypes(){
		try {
			logger.info("Refreshing JobTypes...");
			XmlFileManager xmlFileManager = new XmlFileManager();
			jobTypes = xmlFileManager.getJobTypeMappings().getJobTypesMap();
		} catch (Exception e) {
			String msg = e.getClass().getName() + " reports " + e.getMessage();
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(JobManagementBean.class);

	private static final int maxSeconds = 5;

	@PersistenceContext(unitName = "ijp")
	private EntityManager entityManager;

	public String getJobOutput(String sessionId, long id, OutputType outputType) throws SessionException,
			ForbiddenException, InternalException, ParameterException {
		logger.debug("getJobOutput for id " + id + " outputType " + outputType + " under sessionId " + sessionId);

		Job job = getJob(sessionId, id);
		WebTarget batch = batchServers.get(job.getBatch());
		Response response = batch.path(outputType == OutputType.STANDARD_OUTPUT ? "output" : "error")
				.path(job.getJobId()).queryParam("sessionId", sessionId).queryParam("icatUrl", icatUrl)
				.request(MediaType.APPLICATION_OCTET_STREAM).get(Response.class);
		checkResponse(response);
		return response.readEntity(String.class);

	}

	private Entry<String, WebTarget> chooseBatch(String sessionId, JobType jobType, List<String> parameters)
			throws ParameterException, SessionException, ForbiddenException, InternalException {
		String reqFamily = jobType.getFamily();
		String family = reqFamily == null ? defaultFamily : reqFamily;
		if (!familyPattern.containsKey(family)) { // Map with null values
			throw new ParameterException("Requested family " + reqFamily + " not known");
		}
		Pattern lf = familyPattern.get(family);
		String username = getUserName(sessionId);
		if (lf != null && !lf.matcher(username).matches()) {
			throw new ForbiddenException(username + " is not allowed to use family " + family);
		}

		if (jobType.isSessionId()) {
			parameters.add("--sessionId=" + sessionId);
		}

		if (jobType.isIcatUrlRequired()) {
			parameters.add("--icatUrl=" + icatUrl);
		}

		if (jobType.isIdsUrlRequired()) {
			parameters.add("--idsUrl=" + idsUrl);
		}

		Entry<String, WebTarget> bestEntry;
		if (batchServers.size() == 1) {
			bestEntry = batchServers.entrySet().iterator().next();
		} else {
			Map<Thread, Estimator> threads = new HashMap<>();

			/* Send message to batch workers in parallel */
			for (Entry<String, WebTarget> entry : batchServers.entrySet()) {
				Estimator estimator = new Estimator(sessionId, jobType.getExecutable(), parameters, family, false,
						entry);
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
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Ignore interrupts
				}
			}
		}

		if (bestEntry == null) {
			throw new InternalException("No batch system responded positively within " + maxSeconds + " seconds");
		}

		return bestEntry;

	}

	public String submitBatch(String sessionId, JobType jobType, List<String> parameters) throws SessionException,
			InternalException, ForbiddenException, ParameterException {
		logger.debug("submitBatch: " + jobType + " with parameters " + parameters + " under sessionId " + sessionId);

		Entry<String, WebTarget> bestEntry = chooseBatch(sessionId, jobType, parameters);

		Form f = new Form().param("sessionId", sessionId).param("icatUrl", icatUrl)
				.param("executable", jobType.getExecutable()).param("interactive", "false");

		String reqFamily = jobType.getFamily();
		String family = reqFamily == null ? defaultFamily : reqFamily;
		if (family != null) {
			f.param("family", family);
		}
		for (String s : parameters) {
			f.param("parameter", s);
		}

		Response response = bestEntry.getValue().path("submit").request(MediaType.APPLICATION_JSON)
				.post(Entity.form(f), Response.class);

		checkResponse(response);
		String json = response.readEntity(String.class);

		try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
			String jobId = jsonReader.readObject().getString("jobId");
			Job job = new Job();
			job.setJobId(jobId);
			job.setBatch(bestEntry.getKey());
			job.setUsername(getUserName(sessionId));
			job.setSubmitDate(new Date());
			job.setJobType(jobType.getName());
			job.setStatus(Status.OTHER);
			entityManager.persist(job);
			
			// construct a JSON string for the jobId (to parallel submitInteractive)
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Json.createGenerator(baos).writeStartObject().write("jobId", job.getId()).writeEnd().close();
			logger.debug("submitBatch: returning JSON: " + baos.toString());
			return baos.toString();
		} catch (JsonException e) {
			throw new InternalException("Bad response from batch service " + json);
		}

	}

	public String submitInteractive(String sessionId, JobType jobType, List<String> parameters)
			throws InternalException, ForbiddenException, ParameterException, SessionException {
		logger.debug("submitInteractive: " + jobType + " with parameters " + parameters + " under sessionId "
				+ sessionId);

		Entry<String, WebTarget> bestEntry = chooseBatch(sessionId, jobType, parameters);

		Form f = new Form().param("sessionId", sessionId).param("icatUrl", icatUrl)
				.param("executable", jobType.getExecutable()).param("interactive", "true");

		String reqFamily = jobType.getFamily();
		String family = reqFamily == null ? defaultFamily : reqFamily;
		if (family != null) {
			f.param("family", family);
		}
		for (String s : parameters) {
			f.param("parameter", s);
		}

		Response response = bestEntry.getValue().path("submit").request(MediaType.APPLICATION_JSON)
				.post(Entity.form(f), Response.class);

		checkResponse(response);

		String s = response.readEntity(String.class);
		logger.debug("submitInteractive: received response: " + s);
		return s;

	}

	/**
	 * Submit a named job with the given parameters.
	 * 
	 * @param sessionId an ICAT sessionId
	 * @param jobName name of the jobtype to run
	 * @param parameters parameter values (string list)
	 * @return JSON: jobId for batch jobs, RDP connection details for interactive jobs.
	 * 
	 * @throws InternalException
	 * @throws ForbiddenException
	 * @throws ParameterException
	 * @throws SessionException
	 */
	public String submit(String sessionId, String jobName, List<String> parameters)
			throws InternalException, ForbiddenException, ParameterException, SessionException {
		JobType jobType = jobTypes.get(jobName);
		if (jobType == null) {
			throw new ParameterException("jobName " + jobName + " not recognised");
		}
		String type = jobType.getType();
		if (type == null) {
			throw new InternalException("XML describing job type does not include the type field");
		}
		if (type.equals("interactive")) {
			return submitInteractive(sessionId, jobType, parameters);
		} else if (type.equals("batch")) {
			return submitBatch(sessionId, jobType, parameters);
		} else {
			throw new InternalException("XML describing job '" + jobName + "' has a type field with an invalid value '"
					+ jobType.getType() + "'");
		}
	}

	public String saveProvenanceId(String sessionId, long jobId, long provenanceId)
			throws NotFoundException, ForbiddenException, SessionException {
		Job job = getJob(sessionId, jobId);
		job.setProvenanceId(provenanceId);
		entityManager.persist(job);
		return "{ \"jobId\": " + job.getId()
				+ ", \"provenanceId\": " + job.getProvenanceId()
				+ "}";
	}

	private void checkResponse(Response response) throws InternalException, ForbiddenException, ParameterException,
			SessionException {
		if (response.getStatus() / 100 != 2) {
			String code = null;
			String message = null;
			String result = response.readEntity(String.class);
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
	}

	private String getUserName(String sessionId) throws SessionException {
		try {
			if (sessionId == null) {
				throw new SessionException("sessionId must not be null");
			}
			return icat.getUserName(sessionId);
		} catch (IcatException_Exception e) {
			throw new SessionException("IcatException " + e.getFaultInfo().getType() + " " + e.getMessage());
		}
	}

	public String listStatus(String sessionId) throws SessionException, InternalException, ForbiddenException,
			ParameterException {
		logger.debug("listStatus under sessionId " + sessionId);

		String username = getUserName(sessionId);
		List<Job> jobs = entityManager.createNamedQuery(Job.FIND_BY_USERNAME, Job.class)
				.setParameter("username", username).getResultList();
		String status;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos).writeStartArray();
		for (Job job : jobs) {
			Status statusEnum = job.getStatus();
			if (statusEnum == Status.COMPLETED) {
				status = "Completed";
			} else if (statusEnum == Status.CANCELLED) {
				status = "Cancelled";
			} else {
				WebTarget batch = batchServers.get(job.getBatch());
				if (batch == null) {
					logger.warn("listStatus: unfinished job " + job.getJobId() + "(status " + job.getStatus()
							+ ") is from batch server not in current configuration: " + job.getBatch());
					status = "Unknown";
					job.setStatus(Status.OTHER);
				} else {
					// Don't let batch server response failures spoil listing of
					// other jobs
					try {
						Response response = batch.path("status").path(job.getJobId())
								.queryParam("sessionId", sessionId).queryParam("icatUrl", icatUrl)
								.request(MediaType.APPLICATION_JSON).get(Response.class);
						checkResponse(response);
						String json = response.readEntity(String.class);
						JsonReader jsonReader = Json.createReader(new StringReader(json));
						status = jsonReader.readObject().getString("status");
						if (status.equals("Completed")) {
							job.setStatus(Status.COMPLETED);
						} else if (status.equals("Cancelled")) {
							job.setStatus(Status.CANCELLED);
						}
					} catch (Exception e) {
						logger.warn("listStatus: exception processing batch server " + job.getBatch()
								+ " response for job " + job.getJobId() + "(status " + job.getStatus() + ") "
								+ e.getClass() + " " + e.getMessage());
						status = "Unknown";
						job.setStatus(Status.OTHER);
					}
				}
			}
			synchronized (dateTimeFormat) {
				gen.writeStartObject()
						.write("jobId", job.getId())
						.write("name", job.getJobType())
						.write("date", dateTimeFormat.format(job.getSubmitDate()))
						.write("status", status)
						.write("job", job.getProvenanceId()).writeEnd();
			}
		}
		gen.writeEnd().close();
		return baos.toString();

	}

	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String getStatus(long id, String sessionId) throws SessionException, ForbiddenException, InternalException,
			ParameterException {
		logger.debug("getStatus for id " + id + " under sessionId " + sessionId);
		Job job = getJob(sessionId, id);
		String status;
		Status statusEnum = job.getStatus();
		if (statusEnum == Status.COMPLETED) {
			status = "Completed";
		} else if (statusEnum == Status.CANCELLED) {
			status = "Cancelled";
		} else {
			WebTarget batch = batchServers.get(job.getBatch());
			Response response = batch.path("status").path(job.getJobId()).queryParam("sessionId", sessionId)
					.queryParam("icatUrl", icatUrl).request(MediaType.APPLICATION_JSON).get(Response.class);
			checkResponse(response);
			String json = response.readEntity(String.class);
			try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
				status = jsonReader.readObject().getString("status");
				if (status.equals("Completed")) {
					job.setStatus(Status.COMPLETED);
				} else if (status.equals("Cancelled")) {
					job.setStatus(Status.CANCELLED);
				}
			} catch (JsonException e) {
				throw new InternalException(e.getClass() + " " + e.getMessage() + " for:" + json);
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		synchronized (dateTimeFormat) {
			Json.createGenerator(baos).writeStartObject().write("jobId", job.getId()).write("name", job.getJobType())
					.write("date", dateTimeFormat.format(job.getSubmitDate())).write("status", status).writeEnd()
					.close();
		}
		return baos.toString();
	}

	private Job getJob(String sessionId, long id) throws SessionException, ForbiddenException {
		String username = getUserName(sessionId);
		Job job = entityManager.find(Job.class, id);
		if (job == null || !job.getUsername().equals(username)) {
			throw new ForbiddenException("Job does not belong to you");
		}
		return job;
	}

	public void delete(String sessionId, long id) throws SessionException, ForbiddenException, InternalException,
			ParameterException {
		logger.debug("d	elete for id " + id + " under sessionId " + sessionId);
		Job job = getJob(sessionId, id);
		WebTarget batch = batchServers.get(job.getBatch());
		Response response = batch.path("delete").path(job.getJobId()).queryParam("sessionId", sessionId)
				.queryParam("icatUrl", icatUrl).request(MediaType.APPLICATION_JSON).delete(Response.class);
		checkResponse(response);
		entityManager.remove(job);
	}

	public void cancel(String sessionId, long id) throws SessionException, ForbiddenException, InternalException,
			ParameterException {
		logger.debug("cancel for id " + id + " under sessionId " + sessionId);
		Job job = getJob(sessionId, id);
		WebTarget batch = batchServers.get(job.getBatch());
		Form f = new Form().param("sessionId", sessionId).param("icatUrl", icatUrl);
		Response response = batch.path("cancel").path(job.getJobId()).request(MediaType.APPLICATION_JSON)
				.post(Entity.form(f), Response.class);
		checkResponse(response);
		job.setStatus(Status.CANCELLED);
	}

	/**
	 * Returns a 'human-friendly' string listing the available job types.
	 * This is used by the REST jobtype/ request (with no arguments).
	 * 
	 * @return
	 */
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

	/**
	 * Returns the String conversion for the named jobType (if it is found).
	 * 
	 * @param jobType
	 * @return String
	 * @throws ParameterException
	 */
	public String getHelp(String jobType) throws ParameterException {
		JobType jt = jobTypes.get(jobType);
		if (jt == null) {
			logger.debug("JMB.getHelp: " + jobType + " not found. Current names: " + getJobTypeNames());
			throw new ParameterException("JMB.getHelp: Job type " + jobType + " is not recognised.");
		}
		// TODO Should add a help/description field to JobTypes.
		return getJobTypeJson(jobType);
	}
	
	/**
	 * Return the list of names of available JobTypes, as a JSON string representing an array of strings.
	 * 
	 * @return String
	 */
	public String getJobTypeNames() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos).writeStartArray();
		for (Entry<String, JobType> entry : jobTypes.entrySet()) {
			gen.write(entry.getValue().getName());
		}
		gen.writeEnd().close();
		return baos.toString();
	}
	
	/**
	 * Generate a JSON representation of the named JobType and return it as a string.
	 * 
	 * @param jobType
	 * @return JSON string
	 * @throws ParameterException if no JobType of that name is found
	 */
	public String getJobTypeJson(String jobType) throws ParameterException {
		JobType jt = jobTypes.get(jobType);
		if (jt == null) {
			logger.debug("JMB.getJobType: " + jobType + " not found. Current names: " + getJobTypeNames());
			throw new ParameterException("JMB.getJobType: Job type " + jobType + " is not recognised.");
		}
		
		// NOTE: it may seem more logical to define the JSON for JobType (and JobOption) in
		// the JobType / JobOptions classes themselves; but these are shared between the server and the (GWT) client,
		// which have mutually incompatible JSON libraries. In preference to introducing a third
		// JSON generator, we use the server-side javax.json here.
		
		// We assume that many of the fields of a JobType (and JobOption) are not null,
		// but not that this does not appear to be required of the (implied) XML schemas.
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JsonGenerator gen = Json.createGenerator(baos).writeStartObject();
			gen.write("name",jt.getName());
			gen.write("executable",jt.getExecutable());
			gen.write("multiple",jt.getMultiple());
			gen.write("type",jt.getType());
			writeIfNotNull(gen,"family",jt.getFamily());
			writeIfNotNull(gen,"acceptsDatasets",jt.isAcceptsDatasets());
			writeIfNotNull(gen,"acceptsDatafiles",jt.isAcceptsDatafiles());
			writeIfNotNull(gen,"sessionIdRequired",jt.isSessionId());
			writeIfNotNull(gen,"icatUrlRequired",jt.isIcatUrlRequired());
			writeIfNotNull(gen,"idsUrlRequired",jt.isIdsUrlRequired());
			gen.writeStartArray("datasetTypes");
			for( String datasetType : jt.getDatasetTypes() ){
				gen.write(datasetType);
			}
			gen.writeEnd(); // of datasetTypes array
			gen.writeStartArray("jobOptions");
			for( JobOption jobOption : jt.getJobOptions() ){
				// Add each JobOption as a map from its name to its content?
				// gen.writeStartObject(jobOption.getName());
				gen.writeStartObject();
					gen.write("name",jobOption.getName());
					writeIfNotNull(gen,"groupName", jobOption.getGroupName());
					gen.write("type",jobOption.getType());
					gen.write("programParameter",jobOption.getProgramParameter());
					gen.writeStartArray("values");
					for( String value : jobOption.getValues() ){
						gen.write(value);
					}
					gen.writeEnd(); // of values array
					writeIfNotNull(gen,"defaultValue",jobOption.getDefaultValue());
					writeIfNotNull(gen,"minValue",jobOption.getMinValue());
					writeIfNotNull(gen,"maxValue",jobOption.getMaxValue());
					writeIfNotNull(gen,"condition",jobOption.getCondition());
					writeIfNotNull(gen,"tip",jobOption.getTip());
				gen.writeEnd();
			}
			gen.writeEnd(); // of jobOptions array
		gen.writeEnd().close();
		
		return baos.toString();
	}
	
	/**
	 * Add an object with the given name and value to a JsonGenerator
	 * only if the Boolean value is not null.
	 * 
	 * @param gen JsonGenerator
	 * @param name
	 * @param value Boolean
	 */
	private void writeIfNotNull(JsonGenerator gen, String name, Boolean value) {
		if( value != null ){
			gen.write(name,value);
		}
	}

	/**
	 * Add an object with the given name and value to a JsonGenerator
	 * only if the String value is not null.
	 * 
	 * @param gen JsonGenerator
	 * @param name
	 * @param value String
	 */
	private void writeIfNotNull(JsonGenerator gen, String name, String value){
		if( value != null ){
			gen.write(name,value);
		}
	}
}
