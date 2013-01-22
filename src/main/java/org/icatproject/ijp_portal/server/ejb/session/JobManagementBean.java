package org.icatproject.ijp_portal.server.ejb.session;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.icatproject.ICAT;
import org.icatproject.IcatException_Exception;
import org.icatproject.ijp_portal.server.Families;
import org.icatproject.ijp_portal.server.Icat;
import org.icatproject.ijp_portal.server.Qstat;
import org.icatproject.ijp_portal.server.ejb.entity.Job;
import org.icatproject.ijp_portal.server.manager.XmlFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rl.esc.catutils.ShellCommand;
import org.icatproject.ijp_portal.shared.Constants;
import org.icatproject.ijp_portal.shared.ForbiddenException;
import org.icatproject.ijp_portal.shared.InternalException;
import org.icatproject.ijp_portal.shared.ParameterException;
import org.icatproject.ijp_portal.shared.PortalUtils.OutputType;
import org.icatproject.ijp_portal.shared.SessionException;
import org.icatproject.ijp_portal.shared.xmlmodel.JobType;
import org.icatproject.ijp_portal.shared.xmlmodel.JobTypeMappings;

/**
 * Session Bean implementation to manage job status
 */
@Stateless
public class JobManagementBean {

	private ICAT icat;

	private class LocalFamily {

		private int count;
		private Pattern re;

		public LocalFamily(int count, Pattern re) {
			this.count = count;
			this.re = re;
		}

	}

	private String defaultFamily;
	private Map<String, LocalFamily> families = new HashMap<String, LocalFamily>();
	private Unmarshaller qstatUnmarshaller;
	JobTypeMappings jobTypeMappings;

	@PostConstruct
	void init() {
		try {
			icat = Icat.getIcat();

			Unmarshaller um = JAXBContext.newInstance(Families.class).createUnmarshaller();
			Families fams = (Families) um.unmarshal(new FileReader(Constants.CONFIG_SUBDIR
					+ "/families.xml"));
			PrintStream p = new PrintStream("/etc/puppet/modules/usergen/manifests/init.pp");
			p.print(fams.getPuppet());
			p.close();
			defaultFamily = fams.getDefault();
			for (Families.Family fam : fams.getFamily()) {
				LocalFamily lf = new LocalFamily(fam.getCount(), fam.getRE());
				families.put(fam.getName(), lf);
			}

			qstatUnmarshaller = JAXBContext.newInstance(Qstat.class).createUnmarshaller();

			XmlFileManager xmlFileManager = new XmlFileManager();
			jobTypeMappings = xmlFileManager.getJobTypeMappings();

			logger.debug("Initialised JobManagementBean");
		} catch (Exception e) {
			String msg = e.getClass().getName() + " reports " + e.getMessage();
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(JobManagementBean.class);
	private final static Random random = new Random();
	private final static String chars = "abcdefghijklmnpqrstuvwxyz";

	@PersistenceContext(unitName = "portal")
	private EntityManager entityManager;

	// TODO - these need moving into the properties file
	private final String INGESTER_DIR = "/usr/local/escience/ingester";
	private final String IDS2_URL = "http://rclsfserv009.rc-harwell.ac.uk:8080";

	@Deprecated
	public Job submitDataset(String sessionId, String username, Long datasetId) {
		System.out.println("Received submit dataset request for dataset: " + datasetId);
		String currentDirName = System.getProperty("user.dir");
		String batchScriptName = System.currentTimeMillis() + ".sh";
		// the batch script needs to be written to disk by the dmf user (running glassfish)
		// before it can be submitted via the qsub command
		File batchScriptFile = new File(Constants.DMF_WORKING_DIR_NAME + "/" + batchScriptName);
		String quincyStdOutFilenameStem = Constants.BATCH_WORKING_DIR_NAME + "/" + batchScriptName
				+ ".quincy_stdout";
		// String triggerInputDir = "/home/dmf/ingester/triggers";
		String wgetUrlStem = IDS2_URL + "/ids2/Data/";
		String wgetUrlMiddleBit = "getDataset?sessionid=" + sessionId + "&datasetId=";
		String wgetUrlEnding = wgetUrlMiddleBit + datasetId;
		String unzippedDatasetDir = "unzipped_dataset";
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(batchScriptFile));
			br.write("#!/bin/bash");
			br.newLine();
			br.write("# Script to process dataset " + datasetId + " for sessionId " + sessionId);
			br.newLine();
			br.write("# Current working directory is " + currentDirName);
			br.newLine();
			br.write("echo `date` - THE START");
			br.newLine();
			// not needed now that java is installed in /usr/bin
			// br.write(". /home/dmf/.setup_java_env");
			// br.newLine();
			br.write(". /usr/local/msmm/setup/setup.sh");
			br.newLine();
			// br.write("export LD_LIBRARY_PATH=\"/usr/local/msmm/lib:${LD_LIBRARY_PATH}\"");
			// br.newLine();
			// br.write("export MSMM_PROJECTS=\"/home/dmf/MSMM_ANALYSED\"");
			// br.newLine();
			br.write("env");
			br.newLine();
			br.write("jobNum=${PBS_JOBID%%.*}");
			br.newLine();
			br.write("quincyOutputFilename=" + quincyStdOutFilenameStem + ".q${jobNum}");
			br.newLine();
			br.write("jobIdDir=" + Constants.BATCH_WORKING_DIR_NAME + "/$PBS_JOBID");
			br.newLine();
			br.write("mkdir -p $jobIdDir");
			br.newLine();
			br.write("cd $jobIdDir");
			br.newLine();
			br.write("echo `date` - Before wget");
			br.newLine();
			// wget output goes to standard error so combine it with standard output and tee it to
			// the quincy output file
			// so that progress can be seen in the portal during wget and unzipping even before
			// quincy starts
			br.write("wget --no-proxy \"" + wgetUrlStem + wgetUrlEnding
					+ "\" 2>&1 | tee ${quincyOutputFilename}");
			br.newLine();
			br.write("echo `date` - After wget and before unzip");
			br.newLine();
			br.write("mkdir " + unzippedDatasetDir);
			br.newLine();
			br.write("unzip \"" + wgetUrlEnding + "\" -d " + unzippedDatasetDir
					+ " | tee -a ${quincyOutputFilename}");
			br.newLine();
			// remove the downloaded file
			br.write("rm \"" + wgetUrlEnding + "\"");
			br.newLine();
			br.write("echo `date` - After unzip");
			br.newLine();
			br.write("# firstRunCfg will be for example ./" + unzippedDatasetDir + "/run.cfg");
			br.newLine();
			// find the first run.cfg file in the unzipped directory
			br.write("firstRunCfg=`find . -name run.cfg | head -1`");
			br.newLine();
			// find the line specifying the RunDir parameter
			br.write("runDirLine=`cat $firstRunCfg | grep RunDir`");
			br.newLine();
			br.write("echo runDirLine=[${runDirLine}]");
			br.newLine();
			// double escaping of the backslash is required here!
			// the 4 backslashes translate to 2 backslashes in the shell script
			// which is one escaped backslash really
			br.write("datasetMainDir=${runDirLine#*[/|\\\\]}");
			br.newLine();
			br.write("echo datasetMainDir=[${datasetMainDir}]");
			br.newLine();
			// remove any new line characters from this variable
			br.write("datasetMainDir=$(echo ${datasetMainDir} | tr -d '\n')");
			br.newLine();
			br.write("datasetMainDir=$(echo ${datasetMainDir} | tr -d '\r')");
			br.newLine();
			// change any backslashes to forward slashes
			// more escaping - what we need is "s|\\|/|g"
			br.write("datasetMainDir=$(echo ${datasetMainDir} | sed 's|\\\\|/|g')");
			br.newLine();
			br.write("echo datasetMainDir=[${datasetMainDir}]");
			br.newLine();
			// recreate the directory structure from the RunDir parameter in the run.cfg
			br.write("mkdir -p ${datasetMainDir}");
			br.newLine();
			// unzip the dataset into the created directory structure
			br.write("mv " + unzippedDatasetDir + "/* ${datasetMainDir}");
			br.newLine();
			// remove the now empty unzipped dataset directory
			br.write("rmdir " + unzippedDatasetDir);
			br.newLine();
			// put the "dataset root marker" required by quincy in the top level directory
			br.write("touch msmm_dataset_root_marker.nodelete");
			br.newLine();
			// re-find the first run.cfg file in the re-created directory structure
			// br.write("newFirstRunCfg=`echo $firstRunCfg | sed \"s/" + unzippedDatasetDir +
			// "/${datasetMainDir}/\"`");
			br.write("newFirstRunCfg=`find ${datasetMainDir} -name run.cfg | head -1`");
			br.newLine();
			br.write("echo newFirstRunCfg=[${newFirstRunCfg}]");
			br.newLine();

			// download and unpack any dependency datasets into the correct subfolder
			br.write("echo `date` - Before dependencies");
			br.newLine();
			br.write("currentDir=${PWD}");
			br.newLine();
			br.write("INGESTER_DIR=" + INGESTER_DIR);
			br.newLine();
			br.write("dependencyTypes=( Bead Bias Dark FlatField Check )");
			br.newLine();
			br.write("for dependencyType in ${dependencyTypes[@]}");
			br.newLine();
			br.write("do");
			br.newLine();
			br.write("  cd \"$INGESTER_DIR\"");
			br.newLine();
			br.write("  depDatasetId=$( ./dependency_dataset_finder.sh " + datasetId
					+ " $dependencyType )");
			br.newLine();
			br.write("  cd \"$currentDir\"");
			br.newLine();
			br.write("  if [ -n \"$depDatasetId\" ]; then");
			br.newLine();
			br.write("    depDirLine=$( cat $newFirstRunCfg | grep ${dependencyType}Image | head -1 )");
			br.newLine();
			// remove up to the first slash
			br.write("    depDir=${depDirLine#*[/|\\\\]}");
			br.newLine();
			// remove after the last slash
			br.write("    depDir=${depDir%[/|\\\\]*}");
			br.newLine();
			// remove any new line characters from this variable
			br.write("    depDir=$(echo ${depDir} | tr -d '\n')");
			br.newLine();
			br.write("    depDir=$(echo ${depDir} | tr -d '\r')");
			br.newLine();
			// change any backslashes to forward slashes
			// more escaping - what we need is "s|\\|/|g"
			br.write("    depDir=$(echo ${depDir} | sed 's|\\\\|/|g')");
			br.newLine();
			br.write("    mkdir -p \"${depDir}\"");
			br.newLine();
			br.write("    wgetUrlEnding=\"" + wgetUrlMiddleBit + "${depDatasetId}\"");
			br.newLine();
			br.write("    wget --no-proxy \"" + wgetUrlStem
					+ "${wgetUrlEnding}\" 2>&1 | tee -a ${quincyOutputFilename}");
			br.newLine();
			br.write("    unzip \"${wgetUrlEnding}\" -d \"${depDir}\" | tee -a ${quincyOutputFilename}");
			br.newLine();
			br.write("    rm \"${wgetUrlEnding}\"");
			br.newLine();
			br.write("  fi");
			br.newLine();
			br.write("done");
			br.newLine();
			br.write("echo `date` - After dependencies");
			br.newLine();

			br.write("echo `date` - Before quincy");
			br.newLine();
			// br.write("/usr/local/msmm/bin/quincy \"$jobIdDir/$newFirstRunCfg\"");
			// TODO - remove the --boojum option - this is required for now to allow multi channel
			// datasets to run without RegistrationFailureErrors until something gets fixed by LSF
			br.write("run_quincy --boojum registration.ignore_registration_residuals_error=true \"$jobIdDir/$newFirstRunCfg\" | tee -a ${quincyOutputFilename}");
			br.newLine();
			br.write("echo `date` - After quincy");
			br.newLine();
			// check if run_quincy ran successfully
			br.write("exitStatusLine=$( grep \"Finished with exit status=0\" ${quincyOutputFilename} )");
			br.newLine();
			// if the grep returns no results (zero length output)
			br.write("if [ -z \"$exitStatusLine\" ]; then");
			br.newLine();
			br.write("  echo \"Problem running quincy. See ${quincyOutputFilename} for more details\"");
			br.newLine();
			// otherwise ingest the created dataset (project) into ICAT
			br.write("else");
			br.newLine();
			// grep the run_quincy output for the line specifying the "ProjectDir"
			// br.write("  projectDirLine=`cat " + quincyStdOutFilename + " | grep ProjectDir=`");
			br.write("  projectDirLine=$( grep ProjectDir= ${quincyOutputFilename} )");
			br.newLine();
			br.write("  echo projectDirLine=[${projectDirLine}]");
			br.newLine();
			// remove everything up to and including the first double quote
			// some double escaping is used here as well with the 3(!) backslashes
			br.write("  lineFromFirstQuote=${projectDirLine#*\\\"}");
			br.newLine();
			br.write("  echo lineFromFirstQuote=[${lineFromFirstQuote}]");
			br.newLine();
			// remove everything from (and including) the last double quote
			br.write("  projectDir=${lineFromFirstQuote%%\\\"*}");
			br.newLine();
			br.write("  echo projectDir=[${projectDir}]");
			br.newLine();
			// make a copy of quincy.log so that when the projectDir is removed we don't lose it
			br.write("  cp -p \"${projectDir}/quincy.log\" " + Constants.BATCH_WORKING_DIR_NAME
					+ "/" + batchScriptName + ".quincy.log");
			br.newLine();
			br.write("  cd " + INGESTER_DIR);
			br.newLine();
			br.write("  echo `date` - Before dataset ingest");
			br.newLine();
			// set the Internal Field Separator variable to just newlines
			// so that an array of output lines can be captured
			br.write("  savedIFS=${IFS}");
			br.newLine();
			br.write("  IFS=$'\n'");
			br.newLine();
			// capture the direct_dataset_ingester.sh output in an array (include stderr)
			br.write("  outputArray=($(./direct_dataset_ingester.sh \"${projectDir}\" 2>&1))");
			br.newLine();
			br.write("  ingesterExitCode=$?");
			br.newLine();
			br.write("  IFS=${savedIFS}");
			br.newLine();
			br.write("  echo `date` - After dataset ingest");
			br.newLine();
			br.write("  if [ $ingesterExitCode -eq 0 ]; then");
			br.newLine();
			// get the ID of the newly ingested dataset from the last line of the output
			br.write("    outputArraySize=${#outputArray[*]}");
			br.newLine();
			br.write("    outputDatasetId=${outputArray[$(( ${outputArraySize}-1 ))]}");
			br.newLine();
			br.write("    echo `date` - Before job creator");
			br.newLine();
			// use the new dataset ID to create a job object linking the input and output datasets
			// with run_quincy
			br.write("    ./job_creator.sh run_quincy 1.0 inputDatasetIds " + datasetId
					+ " outputDatasetIds ${outputDatasetId}");
			br.newLine();
			br.write("    echo `date` - After job creator");
			br.newLine();
			br.write("  else");
			br.newLine();
			br.write("    echo \"Direct Dataset Ingester failed with exit code $ingesterExitCode\". Output was:");
			br.newLine();
			br.write("    for outputLine in \"${outputArray[@]}\"; do");
			br.newLine();
			br.write("      echo $outputLine");
			br.newLine();
			br.write("    done");
			br.newLine();
			br.write("  fi");
			br.newLine();
			// clean up the project created in the MSMM_analysed directory
			br.write("  echo `date` - Before projectDir cleanup");
			br.newLine();
			br.write("  rm -rf \"${projectDir}\"");
			br.newLine();
			br.write("fi");
			br.newLine();
			// clean up the downloaded dataset in the directory named with the job id
			br.write("echo `date` - Before jobIdDir cleanup");
			br.newLine();
			br.write("rm -rf \"${jobIdDir}\"");
			br.newLine();
			br.write("echo `date` - THE END");
			br.newLine();

			br.close();
			batchScriptFile.setExecutable(true);
		} catch (IOException e) {
			System.err.println("Exception creating batch script: " + e.getMessage());
		}

		try {
			// submit the job as the user batch instead of dmf
			// this user has less privileges so this is safer
			ProcessBuilder pb = new ProcessBuilder("sudo", "-u", "batch", "qsub", "-w",
					Constants.BATCH_WORKING_DIR_NAME, batchScriptFile.getAbsolutePath());
			pb.redirectErrorStream(true);
			Process qsubProcess = pb.start();
			InputStream combinedOutput = qsubProcess.getInputStream();
			int returnValue = qsubProcess.waitFor();
			String outputString = "";
			try {
				outputString = new Scanner(combinedOutput).useDelimiter("\\A").next();
			} catch (java.util.NoSuchElementException e) {
				// Do nothing
			}
			combinedOutput.close();
			if (returnValue == 0) {
				System.out.println("qsub command executed successfully:");
				String jobId = outputString.trim();
				System.out.println("jobId=[" + outputString + "]");
				// record the job in the Derby DB
				// Job job = new Job(jobId, "Q", username, new Date());
				// set the variable "workerNode" to an empty string so
				// that it is not null and String.equals can be used later
				// without having to check for null first
				Job job = new Job();
				job.setId(jobId);
				job.setStatus("Q");
				job.setUsername(username);
				job.setSubmitDate(new Date());
				job.setBatchFilename(batchScriptName);
				job.setWorkerNode("");
				entityManager.persist(job);
				// call updateJobsInDatabaseFromQstat to populate the worker node field
				// in the database from the info in qstat
				// updateJobsInDatabaseFromQstat();
				// the output from a successful submission is a job ID string
				return job;
			} else {
				System.err.println("Error executing qsub command:");
				System.out.println(outputString);
			}
		} catch (IOException e) {
			System.err.println("Exception executing qsub commmand: " + e.getMessage());
		} catch (InterruptedException e) {
			System.err.println("Exception waiting for qsub commmand: " + e.getMessage());
		}

		// there has been an error submitting the job - return null
		// return "Error submitting dataset " + datasetId + " for processing";
		return null;
	}

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
			logger.debug("Returning final output for " + jobId);
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
				return;
			}

			Qstat qstat = (Qstat) qstatUnmarshaller.unmarshal(new StringReader(jobsXml));
			for (Qstat.Job xjob : qstat.getJobs()) {
				String id = xjob.getJobId();
				String status = xjob.getStatus();
				String wn = xjob.getWorkerNode();
				String workerNode = wn != null ? wn.split("/")[0] : "";
				String comment = xjob.getComment() == null ? "" : xjob.getComment();

				Job job = entityManager.find(Job.class, id);
				if (job != null) {/* Log updates on portal jobs */
					if (!job.getStatus().equals(xjob.getStatus())) {
						logger.debug("Updating status of job '" + id + "' from '" + job.getStatus()
								+ "' to '" + status + "'");
						job.setStatus(status);
					}
					if (!job.getWorkerNode().equals(workerNode)) {
						logger.debug("Updating worker node of job '" + id + "' from '"
								+ job.getWorkerNode() + "' to '" + workerNode + "'");
						job.setWorkerNode(workerNode);
					}
					String oldComment = job.getComment() == null ? "" : job.getComment();
					if (!oldComment.equals(comment)) {
						logger.debug("Updating comment of job '" + id + "' from '" + oldComment
								+ "' to '" + comment + "'");
						job.setComment(comment);
					}
				}
			}
		} catch (Exception e) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			logger.error("Update of db jobs from qstat failed. Class " + e.getClass() + " reports "
					+ e.getMessage() + baos.toString());
		}
	}

	public String submit(String sessionId, String jobName, String options, String reqFamily)
			throws InternalException, SessionException, ParameterException {

		logger.debug("submit: " + jobName + " with options " + options + " under sessionId "
				+ sessionId + " as " + reqFamily);

		String family = reqFamily == null ? defaultFamily : reqFamily;
		LocalFamily lf = families.get(family);
		if (lf == null) {
			throw new ParameterException("Requested family " + reqFamily + " not known");
		}
		String username = getUserName(sessionId);

		if (lf.re != null && !lf.re.matcher(username).matches()) {
			throw new ParameterException(username + " is not allowed to use family " + family);
		}
		String owner = family + random.nextInt(lf.count);

		JobType jobType = jobTypeMappings.getJobTypesMap().get(jobName);
		if (jobType == null) {
			throw new ParameterException("Requested jobname " + jobName + " not known");
		}

		/*
		 * The batch script needs to be written to disk by the dmf user (running glassfish) before
		 * it can be submitted via the qsub command as a less privileged batch user. First generate
		 * a unique name for it.
		 */
		File batchScriptFile = null;
		do {
			char[] pw = new char[10];
			for (int i = 0; i < pw.length; i++) {
				pw[i] = chars.charAt(random.nextInt(chars.length()));
			}
			String batchScriptName = new String(pw) + ".sh";
			batchScriptFile = new File(Constants.DMF_WORKING_DIR_NAME, batchScriptName);
		} while (batchScriptFile.exists());

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(batchScriptFile));
			bw.write("#!/bin/sh");
			bw.newLine();
			bw.write("echo $(date) - " + jobName + " starting");
			bw.newLine();
			bw.write(jobType.getExecutable() + " " + sessionId + " " + options);
			bw.newLine();
			bw.write("echo $(date) - " + jobName + " ending");
			bw.newLine();
		} catch (IOException e) {
			throw new InternalException("Exception creating batch script: " + e.getMessage());
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// Ignore it
				}
			}
		}
		batchScriptFile.setExecutable(true);

		ShellCommand sc = new ShellCommand("sudo", "-u", owner, "qsub", "-k", "eo",
				batchScriptFile.getAbsolutePath());
		if (sc.isError()) {
			throw new InternalException("Unable to submit job via qsub " + sc.getStderr());
		}
		String jobId = sc.getStdout().trim();

		sc = new ShellCommand("qstat", "-x", jobId);
		if (sc.isError()) {
			throw new InternalException("Unable to query just submitted job via qstat "
					+ sc.getStderr());
		}
		String jobsXml = sc.getStdout().trim();

		Qstat qstat;
		try {
			qstat = (Qstat) qstatUnmarshaller.unmarshal(new StringReader(jobsXml));
		} catch (JAXBException e1) {
			throw new InternalException("Unable to query jobs via qstat " + sc.getStderr());
		}
		for (Qstat.Job xjob : qstat.getJobs()) {
			String id = xjob.getJobId();
			if (id.equals(jobId)) {
				Job job = new Job();
				job.setId(jobId);
				job.setStatus(xjob.getStatus());
				job.setComment(xjob.getComment());
				String wn = xjob.getWorkerNode();
				job.setWorkerNode(wn != null ? wn.split("/")[0] : "");
				job.setBatchUsername(owner);
				job.setUsername(username);
				job.setSubmitDate(new Date());
				job.setBatchFilename(batchScriptFile.getName());
				entityManager.persist(job);
			}
		}
		return jobId;
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
}
