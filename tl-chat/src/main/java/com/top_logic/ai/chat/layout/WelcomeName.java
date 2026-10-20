/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import com.top_logic.base.user.UserInterface;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * The name an empty chat greets its user with.
 *
 * <p>
 * The account's contact has a first name of its own, and that is what the greeting uses. It is
 * <em>not</em> the first word of {@link Person#getFullName()}: a full name is title, first name and
 * last name, so for a contact with a title that word is the title - a client whose contact carries
 * the title "Gründerin," was greeted "Hallo Gründerin,." (#597).
 * </p>
 *
 * <p>
 * An account without a contact, or whose contact has no first name, is greeted with its login name,
 * as it always was.
 * </p>
 */
public final class WelcomeName {

	private WelcomeName() {
		// Static helper.
	}

	/**
	 * The name to greet {@code person} with.
	 *
	 * @param person
	 *        The acting account.
	 * @return The contact's first name, or else the login name; empty when there is neither.
	 */
	public static String of(Person person) {
		UserInterface contact = person.getUser();
		return of(contact == null ? null : contact.getFirstName(), person.getName());
	}

	/**
	 * The name to greet with, given the two sources {@link #of(Person)} reads.
	 *
	 * <p>
	 * Punctuation at the end of a name is dropped: the greeting puts its own full stop after the
	 * name, and a name that ends in a comma or a stop of its own would then read "Hallo Anna,." -
	 * such data is typed into a contact form, not checked.
	 * </p>
	 *
	 * @param firstName
	 *        The contact's first name, or <code>null</code> when the account has no contact.
	 * @param loginName
	 *        The account's login name.
	 * @return The cleaned first name when it has any, otherwise the cleaned login name, otherwise
	 *         the empty string.
	 */
	public static String of(String firstName, String loginName) {
		String first = clean(firstName);
		return first.isEmpty() ? clean(loginName) : first;
	}

	private static String clean(String name) {
		if (name == null) {
			return "";
		}
		String result = name.strip();
		int end = result.length();
		while (end > 0 && isTrailingPunctuation(result.charAt(end - 1))) {
			end--;
		}
		return result.substring(0, end).strip();
	}

	private static boolean isTrailingPunctuation(char ch) {
		return ch == ',' || ch == '.' || ch == ';' || ch == ':';
	}

}
