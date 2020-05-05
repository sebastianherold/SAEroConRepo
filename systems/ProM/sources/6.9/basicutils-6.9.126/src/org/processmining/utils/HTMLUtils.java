package org.processmining.utils;

/**
 * @deprecated Use HTMLUtils from org.processmining.basicutils.utils
 */
@Deprecated
public class HTMLUtils {

	public static String encode(String s) {
		return s.replaceAll("&", "&amp;").replaceAll("<", "&lt").replaceAll(">", "&gt;");
	}
}
