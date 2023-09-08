/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.addons.loginmessages.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;

/**
 * A util class for {@link LoginMessage}s.
 *
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public class LoginMessagesUtil {

	/** Name prefix for storing a confirmed {@link LoginMessage} in a {@link PersonalConfiguration}. */
	public static final String PREFIX_CONFIRMED_LOGIN_MESSAGE = "confirmedLoginMessage_";

	/**
	 * A {@link Comparator} to compare {@link LoginMessage}s by name.
	 *
	 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
	 */
	public static class LoginMessageComparatorByNameDescending implements Comparator<LoginMessage> {

		/** The default instance. */
		public static final LoginMessageComparatorByNameDescending INSTANCE = new LoginMessageComparatorByNameDescending();

		@Override
		public int compare(LoginMessage loginMessage1, LoginMessage loginMessage2) {
			return -loginMessage1.getName().compareTo(loginMessage2.getName());
		}
	}

	/**
	 * Provides the list of all {@link LoginMessage}s.
	 */
	public static List<LoginMessage> getLoginMessagesSortedByNameDescending() {
		List<LoginMessage> loginMessages = getLoginMessages();
		Collections.sort(loginMessages, LoginMessageComparatorByNameDescending.INSTANCE);
		return loginMessages;
	}

	/**
	 * Provides the list of all {@link LoginMessage}s.
	 */
	public static List<LoginMessage> getLoginMessages() {
		List<?> loginMessages = LoginMessagesWrapperFactory.getInstance().getAllWrappersFor(LoginMessage.LOGIN_MESSAGE_TYPE);
		return new ArrayList<>(CollectionUtil.dynamicCastView(LoginMessage.class, loginMessages));
	}

	/**
	 * Provides the name for a confirmed {@link LoginMessage} stored in a {@link PersonalConfiguration}.
	 */
	public static String getConfirmName(LoginMessage loginMessage) {
		return LoginMessagesUtil.PREFIX_CONFIRMED_LOGIN_MESSAGE + loginMessage.tId();
	}

	/**
	 * Whether today is between the start date ({@link LoginMessage#getStartDate}) and the end date
	 * ({@link LoginMessage#getEndDate}) of the given {@link LoginMessage}.
	 */
	public static boolean isInTimeInterval(LoginMessage loginMessage) {
		return isAfterStartDate(loginMessage) && isBeforeEndDate(loginMessage);
	}

	/**
	 * Whether today is after the start date ({@link LoginMessage#getStartDate}) of the given {@link LoginMessage}.
	 */
	public static boolean isAfterStartDate(LoginMessage loginMessage) {
		Date startDate = loginMessage.getStartDate();
		return startDate == null || DateUtil.compareDatesByDay(new Date(), startDate) >= 0;
	}

	/**
	 * Whether today is before the end date ({@link LoginMessage#getEndDate}) of the given {@link LoginMessage}.
	 */
	public static boolean isBeforeEndDate(LoginMessage loginMessage) {
		Date endDate = loginMessage.getEndDate();
		return endDate == null || DateUtil.compareDatesByDay(new Date(), endDate) <= 0;
	}

	/**
	 * Whether the confirmation of the given {@link LoginMessage} is expired according to the given {@link PersonalConfiguration}.
	 */
	public static boolean isConfirmExpired(final PersonalConfiguration configuration, LoginMessage loginMessage) {
		Date confirmDate = getConfirmDate(configuration, loginMessage);
		// if there is no confirmation, it must be expired
		if (confirmDate == null) {
			return true;
		}

		Date expirationDate = loginMessage.getConfirmExpirationDate();
		// if confirmation is older than expiration date, it is expired
		if (expirationDate != null && expirationDate.after(confirmDate)) {
			return true;
		}

		// if there is no duration, confirmation lasts forever
		Integer confirmDuration = loginMessage.getConfirmDuration();
		if (confirmDuration == null) {
			return false;
		}

		// if there is a duration, check the difference
		return DateUtil.differenceInDays(new Date(), confirmDate) >= confirmDuration;
	}

	/**
	 * Provides the {@link Date} stored in a {@link PersonalConfiguration} for the given {@link LoginMessage}.
	 */
	public static Date getConfirmDate(final PersonalConfiguration configuration, LoginMessage loginMessage) {
		String confirmName = LoginMessagesUtil.getConfirmName(loginMessage);
		return (Date) configuration.getValue(confirmName);
	}
}
