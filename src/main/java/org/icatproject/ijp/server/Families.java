package org.icatproject.ijp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "families")
public class Families {

	@XmlElement(required = true)
	protected List<Families.Family> family;
	@XmlAttribute(name = "default")
	protected String _default;

	public List<Families.Family> getFamily() {
		if (family == null) {
			family = new ArrayList<Families.Family>();
		}
		return this.family;
	}

	public String getDefault() {
		return _default;
	}

	public static class Family {
		@XmlElement(required = true)
		protected List<Families.Family.Allowed> allowed;
		@XmlAttribute
		protected String name;

		public List<Families.Family.Allowed> getAllowed() {
			if (allowed == null) {
				allowed = new ArrayList<Families.Family.Allowed>();
			}
			return this.allowed;
		}

		public String getName() {
			return name;
		}

		public static class Allowed {

			@XmlAttribute(required = true)
			protected String pattern;

			public String getPattern() {
				return pattern;
			}

		}

		public Pattern getRE() {
			StringBuilder sb = new StringBuilder();
			if (getAllowed().isEmpty()) {
				return null;
			}
			for (Families.Family.Allowed ffa : getAllowed()) {
				if (sb.length() != 0)
					sb.append("|");
				sb.append("(" + ffa.pattern + ")");
			}
			return Pattern.compile(sb.toString());
		}

	}

}
