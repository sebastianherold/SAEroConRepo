package org.processmining.contexts.uitopia.model;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.uitopia.api.model.Author;

public class ProMResourceTypeInformation {

	private final static Map<Class<?>, ResourceTypeInfo> typeInfo = new HashMap<Class<?>, ResourceTypeInfo>();
	private static ProMResourceTypeInformation instance;

	private ProMResourceTypeInformation() {
	}

	public final static ProMResourceTypeInformation getInstance() {
		if (instance == null) {
			instance = new ProMResourceTypeInformation();
		}
		return instance;
	}

	public ResourceTypeInfo getInfoFor(Class<?> type) {
		return typeInfo.get(type);
	}
	
	public void setInfoFor(Class<?> type, String typename, String affiliation, String email, String author, String website,
			String icon) {
		typeInfo.put(type, new ResourceTypeInfo(typename, affiliation, email, author, website, icon));
	}
}

class ResourceTypeInfo implements Author {

	public String affiliation;
	public String email;
	public String author;
	public String website;
	public String icon;
	public String typename;

	public ResourceTypeInfo(String typename, String affiliation, String email, String author, String website,
			String icon) {
		this.typename = typename;
		this.affiliation = affiliation;
		this.email = email;
		this.author = author;
		this.website = website;
		this.icon = icon;
	}

	public String getTypeName() {
		return typename;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return author;
	}

	public URI getWebsite() {
		URI uri = null;
		try {
			uri = new URL(website).toURI();
		} catch (Exception e2) {
		}
		return uri;
	}

	public String getIcon() {
		return icon;
	}

}
