/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo.model;

/**
 * Demo class that represents a person.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoPerson {
    String title;
    String surname;
    String givenName;
    String gender;
    String birthdate;
    String contact;
    String responsability;
    String marital;
    String comment;
    
	public DemoPerson(String title, String surname, String givenName, String responsability, String marital, String gender, 
	                  String birthdate, String contact, String comment) {
		this.title = title;
		this.surname = surname;
		this.givenName = givenName;
		this.gender = gender;
		this.birthdate = birthdate;
		this.contact = contact;
		this.responsability = responsability;
		this.marital = marital;
        this.comment = comment;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    return "title:" + title
	         + " surname:"     + surname
	         + " givenName:"   + givenName
	         + " gender:"      + gender
	         + " birthdate:"   + birthdate
	         + " contact:"     + contact
	         + " responsability:" + responsability
	         + " marital:"     + marital
	         + " comment:"     + comment;
	}

	/** The given name of this person. */
	public String getGivenName() {
		return givenName;
	}

	/** The gender of this person. */
	public String getGender() {
		return gender;
	}

	/** The birth date of this person. */
	public String getBirthdate() {
		return birthdate;
	}

	/** The contact information of this person. */
	public String getContact() {
		return contact;
	}

	/** The surname of this person. */
	public String getSurname() {
		return surname;
	}

	/** The academic title of this person. */
	public String getTitle() {
		return title;
	}
	
	/** The responsibility of this person. */
	public String getResponsability() {
		return responsability;
	}
	
	/** The marital status of this person. */
	public String getMarital() {
		return marital;
	}
	
}
