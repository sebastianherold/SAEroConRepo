package org.processmining.contexts.uitopia.model;

import java.net.URI;
import java.net.URL;

import org.deckfour.uitopia.api.model.Author;

public class AbstractAuthored {

	public Author getAuthor() {
		// TODO
		return new Author() {

			public String getAffiliation() {
				// TODO Auto-generated method stub
				return "Eindhoven University of Technology";
			}

			public String getEmail() {
				// TODO Auto-generated method stub
				return "foo@bar.com";
			}

			public String getName() {
				// TODO Auto-generated method stub
				return "John Doe";
			}

			public URI getWebsite() {
				// TODO Auto-generated method stub
				try {
					return new URL("http://www.processmining.org").toURI();
				} catch (Exception e) {
					return null;
				}
			}

		};
	}
}
