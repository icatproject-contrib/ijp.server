package org.icatproject.ijp.shared.xmlmodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class MultipleOptions {
    @XmlValue
    private boolean multiple = false;

    @XmlAttribute
    private boolean forceSingleJob = false;

    public MultipleOptions() {
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isForceSingleJob() {
        return forceSingleJob;
    }

    public void setForceSingleJob(boolean forceSingleJob) {
        this.forceSingleJob = forceSingleJob;
    }
}
