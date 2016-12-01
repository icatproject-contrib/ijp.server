package org.icatproject.ijp.server.manager;

import org.icatproject.ijp.shared.InternalException;
import org.icatproject.ijp.shared.xmlmodel.JobType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class XmlFileManagerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testJobTypeModel_multipleTrue_multipleTrue() throws IOException, InternalException {
        File input = folder.newFile("test.xml");

        try(PrintWriter writer = new PrintWriter(input)) {
            writer.write("<jobType>\n" +
                    "    <multiple>true</multiple>\n" +
                    "</jobType>");
        };


        XmlFileManager fileManager = new XmlFileManager();

        JobType jt = fileManager.getJobType(input);

        assertThat(jt.getMultiple().isMultiple(), equalTo(true));
    }

    @Test
    public void testJobTypeModel_multipleFalse_multipleFalse() throws IOException, InternalException {
        File input = folder.newFile("test.xml");

        try(PrintWriter writer = new PrintWriter(input)) {
            writer.write("<jobType>\n" +
                    "    <multiple>false</multiple>\n" +
                    "</jobType>");
        };


        XmlFileManager fileManager = new XmlFileManager();

        JobType jt = fileManager.getJobType(input);

        assertThat(jt.getMultiple().isMultiple(), equalTo(false));
    }

    @Test
    public void testJobTypeModel_noMultipleEntry_multipleFalse() throws IOException, InternalException {
        File input = folder.newFile("test.xml");

        try(PrintWriter writer = new PrintWriter(input)) {
            writer.write("<jobType>\n" +
                    "</jobType>");
        };


        XmlFileManager fileManager = new XmlFileManager();

        JobType jt = fileManager.getJobType(input);

        assertThat(jt.getMultiple().isMultiple(), equalTo(false));
    }

    @Test
    public void testJobTypeModel_forceSingleJob_forceSingleJobIsTrue() throws IOException, InternalException {
        File input = folder.newFile("test.xml");

        try(PrintWriter writer = new PrintWriter(input)) {
            writer.write("<jobType>\n" +
                    "    <multiple forceSingleJob=\"true\">true</multiple>\n" +
                    "</jobType>");
        };


        XmlFileManager fileManager = new XmlFileManager();

        JobType jt = fileManager.getJobType(input);

        assertThat(jt.getMultiple().isForceSingleJob(), equalTo(true));
    }

    @Test
    public void testJobTypeModel_noForceSingleJobAttribute_forceSingleJobIsFalse() throws IOException,
            InternalException {
        File input = folder.newFile("test.xml");

        try(PrintWriter writer = new PrintWriter(input)) {
            writer.write("<jobType>\n" +
                    "    <multiple>true</multiple>\n" +
                    "</jobType>");
        };


        XmlFileManager fileManager = new XmlFileManager();

        JobType jt = fileManager.getJobType(input);

        assertThat(jt.getMultiple().isForceSingleJob(), equalTo(false));
    }

    @Test
    public void testJobTypeModel_forceSingleJobAttributeIsFalse_forceSingleJobIsFalse() throws IOException,
            InternalException {
        File input = folder.newFile("test.xml");

        try(PrintWriter writer = new PrintWriter(input)) {
            writer.write("<jobType>\n" +
                    "    <multiple forceSingleJob=\"false\">true</multiple>\n" +
                    "</jobType>");
        };


        XmlFileManager fileManager = new XmlFileManager();

        JobType jt = fileManager.getJobType(input);

        assertThat(jt.getMultiple().isForceSingleJob(), equalTo(false));
    }
}
