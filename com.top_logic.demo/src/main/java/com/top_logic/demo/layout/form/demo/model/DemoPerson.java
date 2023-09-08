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

	public String getGivenName() {
		return givenName;
	}

	public String getGender() {
		return gender;
	}

	public String getBirthdate() {
		return birthdate;
	}

	public String getContact() {
		return contact;
	}

	public String getSurname() {
		return surname;
	}

	public String getTitle() {
		return title;
	}
	
	public String getResponsability() {
		return responsability;
	}
	
	public String getMarital() {
		return marital;
	}
	
}
