
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.user;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Additional account information.
 *
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public interface UserInterface {

    /** The attribute "username", i.e. login name */
	String USER_NAME     = "username";

	/** @see #getTitle() */
	String TITLE = "title";

	/** @see #getName() */
	String NAME = "name";

	/** @see #getFirstName() */
	String FIRST_NAME = "firstname";

	/** @see #getEMail() */
	String EMAIL = "email";

	/** @see #getPhone() */
	String PHONE = "phone";

	/**
	 * Login ID of the user.
	 */
	String getUserName();

    /**
     * the firstname of this user
     */
	String getFirstName();

	/**
	 * @see #getFirstName()
	 */
	void setFirstName(String value);

    /**
	 * The family name the user.
	 */
	String getName();

	/**
	 * @see #getName()
	 */
	void setName(String value);
    
	/**
	 * The e-mail of the user that the system may send mails to, if the user must be notified.
	 */
	String getEMail();

	/**
	 * @see #getEMail()
	 */
	void setEMail(String value);

    /**
     * the title of this user
     */
	String getTitle();

	/**
	 * @see #getTitle()
	 */
	void setTitle(String value);

    /**
     * the internal number of this user
     */
	String getPhone();

	/**
	 * @see #getPhone()
	 */
	void setPhone(String value);

	/**
	 * {@link Comparator} establishing a convenient order for the UI.
	 */
	public static Comparator<? super UserInterface> comparator(Locale l) {
		Collator collator = Collator.getInstance(l);
		return Comparator
			.comparing(UserInterface::getName, collator)
			.thenComparing(UserInterface::getFirstName, collator)
			.thenComparing(UserInterface::getTitle, collator);
	}

}
