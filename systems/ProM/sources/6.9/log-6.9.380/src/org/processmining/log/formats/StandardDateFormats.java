package org.processmining.log.formats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Standard formats for Dates that might be encountered in log data, e.g. in CSV
 * format.
 */
public final class StandardDateFormats {

	@SuppressWarnings("serial")
	private static final Set<SimpleDateFormat> STANDARD_DATE_FORMATS = new LinkedHashSet<SimpleDateFormat>() {
		{
			add(new SimpleDateFormat("yyyy-M-d H:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("yyyy-M-d H:mm:ss.SSSz"));
			add(new SimpleDateFormat("yyyy-M-d H:mm:ss.SSS"));
			add(new SimpleDateFormat("yyyy-M-d H:mm:ssXXX"));
			add(new SimpleDateFormat("yyyy-M-d H:mm:ssz"));
			add(new SimpleDateFormat("yyyy-M-d H:mm:ss"));
			add(new SimpleDateFormat("yyyy-M-d H:mm"));
			add(new SimpleDateFormat("yyyy-M-d'T'H:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("yyyy-M-d'T'H:mm:ss.SSSz"));
			add(new SimpleDateFormat("yyyy-M-d'T'H:mm:ss.SSS"));
			add(new SimpleDateFormat("yyyy-M-d'T'H:mm:ssXXX"));
			add(new SimpleDateFormat("yyyy-M-d'T'H:mm:ssz"));
			add(new SimpleDateFormat("yyyy-M-d'T'H:mm:ss"));
			add(new SimpleDateFormat("yyyy-M-d'T'H:mm"));
			add(new SimpleDateFormat("yyyy-M-d"));

			add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSz"));
			add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
			add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX"));
			add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz"));
			add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
			add(new SimpleDateFormat("yyyy-MM-dd HH:mm"));
			add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz"));
			add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"));
			add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
			add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"));
			add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
			add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"));
			add(new SimpleDateFormat("yyyy-MM-dd"));

			add(new SimpleDateFormat("yyyy/M/d H:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("yyyy/M/d H:mm:ss.SSSz"));
			add(new SimpleDateFormat("yyyy/M/d H:mm:ss.SSS"));
			add(new SimpleDateFormat("yyyy/M/d H:mm:ssXXX"));
			add(new SimpleDateFormat("yyyy/M/d H:mm:ssz"));
			add(new SimpleDateFormat("yyyy/M/d H:mm:ss"));
			add(new SimpleDateFormat("yyyy/M/d H:mm"));
			add(new SimpleDateFormat("yyyy/M/d'T'H:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("yyyy/M/d'T'H:mm:ss.SSSz"));
			add(new SimpleDateFormat("yyyy/M/d'T'H:mm:ss.SSS"));
			add(new SimpleDateFormat("yyyy/M/d'T'H:mm:ssXXX"));
			add(new SimpleDateFormat("yyyy/M/d'T'H:mm:ssz"));
			add(new SimpleDateFormat("yyyy/M/d'T'H:mm:ss"));
			add(new SimpleDateFormat("yyyy/M/d'T'H:mm"));
			add(new SimpleDateFormat("yyyy/M/d"));

			add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSz"));
			add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS"));
			add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ssXXX"));
			add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ssz"));
			add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
			add(new SimpleDateFormat("yyyy/MM/dd HH:mm"));
			add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss.SSSz"));
			add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss.SSS"));
			add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ssXXX"));
			add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ssz"));
			add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss"));
			add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm"));
			add(new SimpleDateFormat("yyyy/MM/dd"));

			add(new SimpleDateFormat("M/d/yyyy H:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("M/d/yyyy H:mm:ss.SSSz"));
			add(new SimpleDateFormat("M/d/yyyy H:mm:ss.SSS"));
			add(new SimpleDateFormat("M/d/yyyy H:mm:ssXXX"));
			add(new SimpleDateFormat("M/d/yyyy H:mm:ssz"));
			add(new SimpleDateFormat("M/d/yyyy H:mm:ss"));
			add(new SimpleDateFormat("M/d/yyyy H:mm"));
			add(new SimpleDateFormat("M/d/yyyy'T'H:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("M/d/yyyy'T'H:mm:ss.SSSz"));
			add(new SimpleDateFormat("M/d/yyyy'T'H:mm:ss.SSS"));
			add(new SimpleDateFormat("M/d/yyyy'T'H:mm:ssXXX"));
			add(new SimpleDateFormat("M/d/yyyy'T'H:mm:ssz"));
			add(new SimpleDateFormat("M/d/yyyy'T'H:mm:ss"));
			add(new SimpleDateFormat("M/d/yyyy'T'H:mm"));
			add(new SimpleDateFormat("M/d/yyyy"));

			add(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSSz"));
			add(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS"));
			add(new SimpleDateFormat("MM/dd/yyyy HH:mm:ssXXX"));
			add(new SimpleDateFormat("MM/dd/yyyy HH:mm:ssz"));
			add(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"));
			add(new SimpleDateFormat("MM/dd/yyyy HH:mm"));
			add(new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ss.SSSz"));
			add(new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ss.SSS"));
			add(new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ssXXX"));
			add(new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ssz"));
			add(new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ss"));
			add(new SimpleDateFormat("MM/dd/yyyy'T'HH:mm"));
			add(new SimpleDateFormat("MM/dd/yyyy"));
			
			add(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSSz"));
			add(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS"));
			add(new SimpleDateFormat("dd/MM/yyyy HH:mm:ssXXX"));
			add(new SimpleDateFormat("dd/MM/yyyy HH:mm:ssz"));
			add(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"));
			add(new SimpleDateFormat("dd/MM/yyyy HH:mm"));
			add(new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss.SSSz"));
			add(new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss.SSS"));
			add(new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ssXXX"));
			add(new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ssz"));
			add(new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss"));
			add(new SimpleDateFormat("dd/MM/yyyy'T'HH:mm"));	

			add(new SimpleDateFormat("yyyy.M.d H:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("yyyy.M.d H:mm:ss.SSSz"));
			add(new SimpleDateFormat("yyyy.M.d H:mm:ss.SSS"));
			add(new SimpleDateFormat("yyyy.M.d H:mm:ssXXX"));
			add(new SimpleDateFormat("yyyy.M.d H:mm:ssz"));
			add(new SimpleDateFormat("yyyy.M.d H:mm:ss"));
			add(new SimpleDateFormat("yyyy.M.d H:mm"));

			add(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSSz"));
			add(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS"));
			add(new SimpleDateFormat("yyyy.MM.dd HH:mm:ssXXX"));
			add(new SimpleDateFormat("yyyy.MM.dd HH:mm:ssz"));
			add(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss"));
			add(new SimpleDateFormat("yyyy.MM.dd HH:mm"));
			
			add(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSXXX"));
			add(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSz"));
			add(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS"));
			add(new SimpleDateFormat("dd.MM.yyyy HH:mm:ssXXX"));
			add(new SimpleDateFormat("dd.MM.yyyy HH:mm:ssz"));
			add(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"));
			add(new SimpleDateFormat("dd.MM.yyyy HH:mm"));

			add(new SimpleDateFormat("d-M-yyyy:H:mm:ss"));
			add(new SimpleDateFormat("EEE, d MMM yyyy H:mm:ss z"));
			add(new SimpleDateFormat("M-d-yyyy H:mm:ss"));
			add(new SimpleDateFormat("M-d-yyyy H:mm"));
			add(new SimpleDateFormat("M-d-yyyy"));
			add(new SimpleDateFormat("d-M-yyyy H:mm:ss"));
			add(new SimpleDateFormat("d-M-yyyy H:mm"));
			add(new SimpleDateFormat("d-M-yyyy"));

			add(new SimpleDateFormat("dd-MM-yyyy:HH:mm:ss"));
			add(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z"));
			add(new SimpleDateFormat("MM-dd-yyyy HH:mm:ss"));
			add(new SimpleDateFormat("MM-dd-yyyy HH:mm"));
			add(new SimpleDateFormat("MM-dd-yyyy"));
			add(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
			add(new SimpleDateFormat("dd-MM-yyyy HH:mm"));
			add(new SimpleDateFormat("dd-MM-yyyy"));
			
			add(new SimpleDateFormat("dd.MM.yyyy HH.mm"));
		}
	};

	static {
		for (DateFormat df : STANDARD_DATE_FORMATS) {
			df.setLenient(false);
		}
	}

	private StandardDateFormats() {
		super();
	}

	/**
	 * @return {@link SimpleDateFormat} for various {@link Date} patterns in
	 *         order from most specific to least specific.
	 */
	public static Iterable<SimpleDateFormat> getStandardDateFormats() {
		return STANDARD_DATE_FORMATS;
	}

}
