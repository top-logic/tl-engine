/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.bus;

import java.util.Date;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * The {@link UserEvent} to make changes of users available.
 * 
 * @param passiveUser
 *        The user affected by this event.
 * @param user
 *        The user triggering this event.
 * @param sessionID
 *        Session ID of the passive user.
 * @param machine
 *        Client IP address of the session.
 * @param type
 *        Type of this event.
 * @param date
 *        Date when this event occurs.
 */
public record UserEvent(Person passiveUser, Person user, String sessionID, String machine, EventType type, Date date) {

	/**
	 * Type of the {@link UserEvent}.
	 */
	public enum EventType {
		/** User has logged in. */
		LOGGED_IN,
		/** User has logged out. */
		LOGGED_OUT;
	}

	/**
	 * Constructor creates a new {@link UserEvent} with {@link UserEvent#date()} now.
	 */
	public UserEvent(Person passiveUser, Person user, String sessionID, String machine, EventType type) {
		this(passiveUser, user, sessionID, machine, type, new Date());
	}

}
