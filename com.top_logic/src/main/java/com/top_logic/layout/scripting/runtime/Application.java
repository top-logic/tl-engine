/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletException;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * An application unter test.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Application {

	/**
	 * Creates a new {@link ApplicationSession} for the given user with the given time zone and
	 * locale.
	 */
	ApplicationSession login(Person user, TimeZone timeZone, Locale locale) throws IOException, MalformedURLException, ServletException;

	/**
	 * Creates a new {@link ApplicationSession} for the given user, its {@link TimeZone}, and its
	 * {@link Locale}.
	 */
	default ApplicationSession login(Person user) throws IOException, MalformedURLException, ServletException {
		return login(user, user.getTimeZone(), user.getLocale());
	}

	/**
	 * Creates a new {@link ApplicationSession} for the given user with the given time zone and
	 * locale and the main layout loaded from the given (alternative) location.
	 */
	ApplicationSession login(Person user, TimeZone timeZone, Locale locale, String layoutName)
			throws IOException, MalformedURLException, ServletException;

	/**
	 * Creates a new {@link ApplicationSession} for the given user, its {@link TimeZone}, and its
	 * {@link Locale} and the main layout loaded from the given (alternative) location.
	 */
	default ApplicationSession login(Person user, String layoutName)
			throws IOException, MalformedURLException, ServletException {
		return login(user, user.getTimeZone(), user.getLocale(), layoutName);
	}

}
