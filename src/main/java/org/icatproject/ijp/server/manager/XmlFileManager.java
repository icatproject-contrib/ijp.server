package org.icatproject.ijp.server.manager;

import java.io.File;
import java.io.FilenameFilter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.icatproject.ijp.shared.Constants;
import org.icatproject.ijp.shared.InternalException;
import org.icatproject.ijp.shared.xmlmodel.JobDatasetMappings;
import org.icatproject.ijp.shared.xmlmodel.JobDatasetType;
import org.icatproject.ijp.shared.xmlmodel.JobType;
import org.icatproject.ijp.shared.xmlmodel.JobTypeMappings;
import org.icatproject.ijp.shared.xmlmodel.SearchItems;

public class XmlFileManager {

	final static Logger logger = LoggerFactory.getLogger(XmlFileManager.class);

	public SearchItems getDatasetSearchItems() throws InternalException {
		return getSearchItems("dataset_search_items");
	}

	public SearchItems getDatafileSearchItems() throws InternalException {
		return getSearchItems("datafile_search_items");
	}

	private SearchItems getSearchItems(String fileName) throws InternalException {
		SearchItems searchItems = null;
		File xmlFile = new File(Constants.CONFIG_SUBDIR + "/" + fileName + ".xml");
		try {
			JAXBContext context = JAXBContext.newInstance(SearchItems.class);
			Unmarshaller u = context.createUnmarshaller();
			logger.debug("SearchItems read");
			JAXBElement<SearchItems> root = u.unmarshal(new StreamSource(xmlFile),
					SearchItems.class);
			searchItems = root.getValue();
		} catch (JAXBException e) {
			throw new InternalException("Error reading XML definition for SearchItems from file "
					+ xmlFile.getAbsolutePath() + ": " + e.getMessage());
		}
		return searchItems;
	}

	public JobTypeMappings getJobTypeMappings() throws InternalException {
		JobTypeMappings jobTypeMappings = new JobTypeMappings();
		File[] dirListing = new File(Constants.CONFIG_SUBDIR + "/job_types")
			.listFiles(new FilenameFilter() {
			    @Override
			    public boolean accept(File dir, String name) {
			        return name.endsWith(".xml");
			    }
			});
		if( dirListing == null ){
			throw new InternalException("Error listing contents of job_types folder, does it exist?");
		}
		for (File xmlFile : dirListing) {
			JobType jobType = getJobType(xmlFile);
			jobTypeMappings.addJobTypeToMap(jobType);
		}
		return jobTypeMappings;
	}

	private JobType getJobType(File xmlFile) throws InternalException {
		JobType jobType = null;
		try {
			JAXBContext context = JAXBContext.newInstance(JobType.class);
			Unmarshaller u = context.createUnmarshaller();
			JAXBElement<JobType> root = u.unmarshal(new StreamSource(xmlFile), JobType.class);
			jobType = root.getValue();
			logger.debug("JobType " + jobType.getName() + " read.");
		} catch (JAXBException e) {
			throw new InternalException("Error reading XML definition for JobType from file "
					+ xmlFile.getAbsolutePath() + ": " + e.getMessage());
		}
		return jobType;
	}

	JobDatasetMappings getJobDatasetMappings() throws InternalException {
		JobDatasetMappings jobDatasetMappings = new JobDatasetMappings();
		File[] dirListing = new File(Constants.CONFIG_SUBDIR + "/job_dataset_parameters")
				.listFiles(new FilenameFilter() {
				    @Override
				    public boolean accept(File dir, String name) {
				        return name.endsWith(".xml");
				    }
				});
		if( dirListing == null ){
			throw new InternalException("Error listing contents of job_dataset_parameters folder, does it exist?");
		}
		for (File xmlFile : dirListing) {
			JobDatasetType jobDatasetType = getJobDatasetType(xmlFile);
			jobDatasetMappings.addJobDatasetTypeToMap(jobDatasetType);
		}
		return jobDatasetMappings;
	}

	private JobDatasetType getJobDatasetType(File xmlFile) throws InternalException {
		JobDatasetType jobDatasetType = null;
		try {
			JAXBContext context = JAXBContext.newInstance(JobDatasetType.class);
			Unmarshaller u = context.createUnmarshaller();
			logger.debug("JobDatasetType read");
			JAXBElement<JobDatasetType> root = u.unmarshal(new StreamSource(xmlFile),
					JobDatasetType.class);
			jobDatasetType = root.getValue();
		} catch (JAXBException e) {
			throw new InternalException(
					"Error reading XML definition for JobDatasetType from file "
							+ xmlFile.getAbsolutePath() + ": " + e.getMessage());
		}
		return jobDatasetType;
	}

}
