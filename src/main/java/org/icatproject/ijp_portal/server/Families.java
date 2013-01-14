package org.icatproject.ijp_portal.server;

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
		@XmlAttribute
		protected Integer count;
		@XmlAttribute
		protected String create;
		@XmlAttribute
		protected String destroy;

		public List<Families.Family.Allowed> getAllowed() {
			if (allowed == null) {
				allowed = new ArrayList<Families.Family.Allowed>();
			}
			return this.allowed;
		}

		public String getName() {
			return name;
		}

		public Integer getCount() {
			return count;
		}

		public static class Allowed {

			@XmlAttribute(required = true)
			protected String pattern;

			public String getPattern() {
				return pattern;
			}

		}

		public String getDestroy() {
			return destroy;
		}

		public String getCreate() {
			return create;
		}

		public Pattern getRE() {
			StringBuilder sb = new StringBuilder();
			if (getAllowed().isEmpty()) {
				return null;
			}
			for (Families.Family.Allowed ffa : getAllowed()) {
				if (sb.length() != 0)sb.append("|");
				sb.append("(" + ffa.pattern + ")");
			}
			return Pattern.compile(sb.toString());
		}

	}

	public String getPuppet() {
		StringBuilder sb = new StringBuilder();
		sb.append("class usergen {").append('\n');
		for (Family f : getFamily()) {
			String create = f.getCreate();
			for (int i = 0; i < f.getCount(); i++) {
				String user = f.getName() + i;
				sb.append(create.replace("$user", user)).append('\n');
			}
		}
		sb.append("}").append('\n');
		return sb.toString();
	}

}
