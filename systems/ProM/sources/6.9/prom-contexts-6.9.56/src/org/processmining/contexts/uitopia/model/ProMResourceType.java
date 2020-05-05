package org.processmining.contexts.uitopia.model;

import java.awt.Image;
import java.net.URI;
import java.net.URL;

import org.deckfour.uitopia.api.model.Author;
import org.deckfour.uitopia.api.model.ResourceType;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.processmining.framework.annotations.AuthoredType;
import org.processmining.framework.annotations.Icon;

public class ProMResourceType implements ResourceType {

	private final Class<?> type;
	private Author author;
	private Image icon;
	private String name;
	private final static String DEFAULT_ICON = "resourcetype_model_30x35.png";

	public ProMResourceType(Class<?> aType) {
		type = aType;

		final ResourceTypeInfo typeInfo = ProMResourceTypeInformation.getInstance().getInfoFor(aType);
		if (typeInfo != null) {
			author = typeInfo;
			name = typeInfo.getTypeName();
			icon = ImageLoader.load(typeInfo.getIcon());

		} else {
			if (type.isAnnotationPresent(AuthoredType.class)) {
				author = new Author() {

					public String getAffiliation() {
						return type.getAnnotation(AuthoredType.class).affiliation();
					}

					public String getEmail() {
						return type.getAnnotation(AuthoredType.class).email();
					}

					public String getName() {
						return type.getAnnotation(AuthoredType.class).author();
					}

					public URI getWebsite() {
						URI uri = null;
						try {
							uri = new URL(type.getAnnotation(AuthoredType.class).website()).toURI();
						} catch (Exception e) {
							try {
								uri = new URL("http://www.processmining.org").toURI();
							} catch (Exception e2) {
							}
						}
						return uri;
					}

				};
				name = type.getAnnotation(AuthoredType.class).typeName();
			} else {
				author = new Author() {

					public String getAffiliation() {
						return "Affiliation unknown";
					}

					public String getEmail() {
						return "h.m.w.verbeek@tue.nl";
					}

					public String getName() {
						return "Author unknown";
					}

					public URI getWebsite() {
						URI uri = null;
						try {
							uri = new URL("http://www.processmining.org").toURI();
						} catch (Exception e2) {
						}
						return uri;
					}
				};
				name = type.getSimpleName();
			}

			if (type.isAnnotationPresent(Icon.class)) {
				icon = ImageLoader.load(type.getAnnotation(Icon.class).icon());
			}

			/*
			 * HV: No icon found yet. Use default.
			 */
			if (icon == null) {
				if (type.getName().equals("org.deckfour.xes.model.XLog")) {
					/*
					 * HV: The framework does not know the XLog interface, and
					 * OpenXES does not know the @Icon annotation. Therefore, we
					 * hardcode the link between an XLog and its icon here.
					 */
					icon = ImageLoader.load("resourcetype_log_30x35.png");
				} else {
					icon = ImageLoader.load(DEFAULT_ICON);
				}
			}
		}

	}

	public Author getTypeAuthor() {
		return author;
	}

	public Class<?> getTypeClass() {
		return type;
	}

	public Image getTypeIcon() {
		return icon;
	}

	public String getTypeName() {
		return name;
	}

	public boolean isAssignableFrom(ResourceType type) {
		return getTypeClass().isAssignableFrom(type.getTypeClass());
	}

	public boolean equals(Object o) {
		if (!(o instanceof ResourceType)) {
			return false;
		}
		ResourceType rt = (ResourceType) o;
		return (type.equals(rt.getTypeClass()) && getTypeName().equals(rt.getTypeName()));

	}

	public int hashCode() {
		return type.hashCode() + 37 * getTypeName().hashCode();
	}

}
