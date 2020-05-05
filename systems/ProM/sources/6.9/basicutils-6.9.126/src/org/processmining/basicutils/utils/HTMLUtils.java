package org.processmining.basicutils.utils;

public class HTMLUtils {

	public static String encode(String s) {
		return s.replaceAll("&", "&amp;").replaceAll("<", "&lt").replaceAll(">", "&gt;");
	}
}
