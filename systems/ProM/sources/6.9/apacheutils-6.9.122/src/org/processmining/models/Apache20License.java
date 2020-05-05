package org.processmining.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.processmining.framework.util.HTMLToString;

public class Apache20License implements HTMLToString {

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buffer = new StringBuffer();
		URL url;
		InputStream is = null;
		BufferedReader br;
		String line;
		String start = "visible }</style>";
		String end = "h1 id=\"apply\">APPENDIX";

		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		try {
			url = new URL("http://www.apache.org/licenses/LICENSE-2.0.html");
			is = url.openStream(); // throws an IOException
			br = new BufferedReader(new InputStreamReader(is));
			boolean copy = false;

			while ((line = br.readLine()) != null) {
				if (line.indexOf(start) > 0) {
					copy = true;
					line = line.substring(line.indexOf(start) + start.length());
				}
				if (line.indexOf(end) > 0) {
					line = line.substring(0, line.indexOf(end) - 1);
					if (copy) {
						copy = false;
						buffer.append(line);
						buffer.append("\n");
					}
				}
				if (copy) {
					buffer.append(line);
					buffer.append("\n");
				}
			}
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
				// nothing to see here
			}
		}
		if (includeHTMLTags) {
			buffer.append("</html>");
		}

		return buffer.toString();
	}

}
