package org.icatproject.ijp.shared;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PortalUtils {

	public enum ParameterValueType {
		STRING, NUMERIC, DATE_AND_TIME
	}

	public enum ParameterLevelType {
		DATASET, DATASET_PARAMETER
	}

	public enum OutputType {
		STANDARD_OUTPUT("Standard Output"), ERROR_OUTPUT("Error Output");
		private final String name;

		private OutputType(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	public enum MultiJobTypes {
		MULTIPLE_DATASETS_ONE_JOB, ONE_DATASET_PER_JOB
	}

	public static final int MAX_RESULTS = 200;

	// define the max and min "integer" values that a double can represent exactly
	// see: http://mindprod.com/applet/converter.html
	public static final Double TWO_TO_POWER_53_DOUBLE = 9007199254740992D;
	public static final Double MINUS_TWO_TO_POWER_53_DOUBLE = -9007199254740992D;

	public static HashMap<ParameterValueType, List<String>> PARAM_OPERATOR_MAPPINGS = initialiseParamOperatorMappings();
	public static HashMap<String, String> JOB_STATUS_MAPPINGS = initialiseJobStatusMappings();

	private static HashMap<ParameterValueType, List<String>> initialiseParamOperatorMappings() {
		HashMap<ParameterValueType, List<String>> paramOperatorMappings = new HashMap<ParameterValueType, List<String>>();
		paramOperatorMappings.put(ParameterValueType.STRING, Arrays.asList("=", "!=", "LIKE"));
		paramOperatorMappings.put(ParameterValueType.NUMERIC,
				Arrays.asList("=", "!=", "<", "<=", ">", ">=", "BETWEEN"));
		paramOperatorMappings.put(ParameterValueType.DATE_AND_TIME, Arrays.asList("BETWEEN"));
		return paramOperatorMappings;
	}

	/**
	 * Set up a mappings for status strings as defined in the qstat section of the Torque
	 * Administrator's Guide
	 */
	private static HashMap<String, String> initialiseJobStatusMappings() {
		HashMap<String, String> jobStatusMappings = new HashMap<String, String>();
		jobStatusMappings.put("C", "COMPLETED");
		jobStatusMappings.put("E", "EXITING");
		jobStatusMappings.put("H", "HELD");
		jobStatusMappings.put("Q", "QUEUED");
		jobStatusMappings.put("R", "RUNNING");
		jobStatusMappings.put("T", "BEING MOVED");
		jobStatusMappings.put("W", "WAITING");
		jobStatusMappings.put("S", "SUSPENDED");
		jobStatusMappings.put("", "UNKNOWN");
		return jobStatusMappings;
	}

	public static String createStringFromList(List<String> stringList, String separator) {
		if ( stringList.size() == 0 ) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for ( String stringFromList : stringList ) {
			sb.append(stringFromList);
			sb.append(separator);
		}
		return sb.substring(0, sb.length()-separator.length());
	}

	public static String removeBackspacesFromString(String inString) {
		char backspaceChar = new Character('\b').charValue();
		StringBuilder sb = new StringBuilder(inString.length());
		for (int i = 0; i < inString.length(); i++) {
			char nextChar = inString.charAt(i);
			if (nextChar == backspaceChar) {
				sb.deleteCharAt(sb.length() - 1);
			} else {
				sb.append(nextChar);
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		// System.out.println( DatasetType.LSF_DATASET.toString() );
		// System.out.println( DatasetType.LSF_PROJECT.toString() );
		// String jobId = "19.rclsfserv009.rc-harwell.ac.uk";
		// System.out.println( "[" + jobId.substring(0,jobId.indexOf(".")) + "]" );
		// String query =
		// "Dataset.id [type.name = '${datasetType}'] <-> DatasetParameter [(type.name = 'users' AND stringValue IN (${stringValues}))";
		// System.out.println("query=[" + query + "]");
		// query = query.replace("${datasetType}", "MyString");
		// System.out.println("query=[" + query + "]");

		// Long.MAX_VALUE=[9223372036854775807]
		BigDecimal maxLongBigDecimal = new BigDecimal(Long.MAX_VALUE);
		Double myDouble = 9007199254740992D;
		// max and min integer values that a double can represent exactly
		// see: http://mindprod.com/applet/converter.html
		Double twoToPower53Double = 9007199254740992D;
		Double minusTwoToPower53Double = -9007199254740992D;
		BigDecimal myDoubleBigDecimal = new BigDecimal(myDouble);
		BigDecimal twoToPower53BigDecimal = new BigDecimal(twoToPower53Double);
		BigDecimal minusTwoToPower53BigDecimal = new BigDecimal(minusTwoToPower53Double);
		System.out.println("twoToPower53BigDecimal =[" + twoToPower53BigDecimal + "]");
		System.out.println("minusTwoToPower53BigDecimal =[" + minusTwoToPower53BigDecimal + "]");

		int compareMaxInt = myDoubleBigDecimal.compareTo(twoToPower53BigDecimal);
		int compareMinInt = myDoubleBigDecimal.compareTo(minusTwoToPower53BigDecimal);
		// if the number is less than or equal to the maximum safe "integer" value and
		// the number is greater than or equal to the minimum safe "integer" value
		System.out.println("compareMaxInt =[" + compareMaxInt + "]");
		System.out.println("compareMinInt =[" + compareMinInt + "]");

		String displayString = null;
		if ((compareMaxInt == -1 || compareMaxInt == 0)
				&& (compareMinInt == 0 || compareMinInt == 1)) {
			try {
				BigInteger bigInt = myDoubleBigDecimal.toBigIntegerExact();
				displayString = bigInt.toString();
			} catch (ArithmeticException e) {
				// myDoubleBigDecimal has a non-zero fractional part
				// use a string representation of this BigDecimal
				// using engineering notation if an exponent is needed.
				displayString = myDoubleBigDecimal.toEngineeringString();
			}
		} else {
			System.out.println("Double is outside of the safe range: [" + myDoubleBigDecimal + "]");
			displayString = myDoubleBigDecimal.toEngineeringString();
		}
		System.out.println("displayString =[" + displayString + "]");

		// Double myDouble = 12345.56D;
		// Double myDouble = 12345D;
		// Double doubleValueOfLong = Double.valueOf(((Long)Long.MAX_VALUE).doubleValue());
		// System.out.println("myDouble          =[" + myDouble + "]");
		// System.out.println("doubleValueOfLong =[" + doubleValueOfLong + "]");
		// int compareInt = myDouble.compareTo(doubleValueOfLong);
		// System.out.println("compareInt        =[" + compareInt + "]");

		// System.out.println("maxLongBigDecimal  =[" + maxLongBigDecimal + "]");
		// System.out.println("myDoubleBigDecimal =[" + myDoubleBigDecimal + "]");
		// int compareInt = myDoubleBigDecimal.compareTo(maxLongBigDecimal);
		// System.out.println("compareInt         =[" + compareInt + "]");

		// if ( doubleValueOfDouble <= doubleValueOfLong ) {
		// System.out.println( "myDouble <= Long.MAX_VALUE");
		// Double remainderDouble = myDouble % 1;
		// System.out.println("remainderDouble=[" + remainderDouble + "]");
		// if ( remainderDouble == 0.0D ) {
		// System.out.println("remainderDouble is zero");
		// } else {
		// System.out.println("remainderDouble is NOT zero");
		// }
		// } else {
		// System.out.println( "myDouble > Long.MAX_VALUE");
		// }
	}

}
