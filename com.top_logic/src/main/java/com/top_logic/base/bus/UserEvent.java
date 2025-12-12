/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.bus;

import java.util.Date;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * The {@link UserEvent} to make changes of Users available.
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
