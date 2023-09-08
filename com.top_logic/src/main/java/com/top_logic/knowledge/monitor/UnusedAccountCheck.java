/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.monitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.db.DBUtil;
import com.top_logic.util.db.ResultExtractor;

/**
 * Helper class checking for (and deleting) unused accounts.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UnusedAccountCheck extends AbstractConfiguredInstance<UnusedAccountCheck.Config> {

	/**
	 * Typed configuration interface definition for {@link UnusedAccountCheck}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<UnusedAccountCheck> {

		/**
		 * Config attribute for the amount of days after which users will be notified about unused
		 * accounts.
		 */
		String CONF_NOTIFY_DAY_COUNT = "notifyDayCount";

		/** Config attribute for the amount of days after which unused accounts will get deleted. */
		String CONF_DELETE_DAY_COUNT = "deleteDayCount";

		/** Config attribute for the user names to exclude from automatic deleting. */
		String CONF_EXCLUDE_UIDS = "excludeUIDs";

		/**
		 * The amount of days after which users will be notified about unused accounts.
		 */
		@IntDefault(0)
		@Name(CONF_NOTIFY_DAY_COUNT)
		int getNotifyDayCount();

		/**
		 * The amount of days after which unused accounts will get deleted.
		 */
		@IntDefault(0)
		@Name(CONF_DELETE_DAY_COUNT)
		int getDeleteDayCount();

		/**
		 * The user names to exclude from automatic deleting.
		 */
		@Name(CONF_EXCLUDE_UIDS)
		@Label("Exclude user IDs")
		@Format(CommaSeparatedStringSet.class)
		Set<String> getExcludeUIDs();

	}

	private final PersonManager _personManager;

	private final KnowledgeBase _kb;

	/**
	 * Create a {@link UnusedAccountCheck}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public UnusedAccountCheck(InstantiationContext context, Config config) {
		super(context, config);
		_personManager = PersonManager.getManager();
		_kb = PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * Checks each account, informs users about not used accounts for a specific amount of days and
	 * deletes unused accounts
	 * 
	 * @param checkOnly
	 *        if <code>true</code>, no notification or deletion will be done, but only the check
	 * 
	 * @return three int values as follow:<br/>
	 *         [0]: the amount of users which have logged in again since last notification<br/>
	 *         [1]: the amount of users which were informed about not used accounts<br/>
	 *         [2]: the amount of deleted accounts
	 */
	public int[/* 3 */] checkUnusedAccounts(boolean checkOnly) {
		int resetCount = 0, notifiedCount = 0, deletedCount = 0;
		Date now = new Date();
		Map<String, Date> latestLogins = getLatestLogins();
		int notifyDayCount = getConfig().getNotifyDayCount();
		int deleteDayCount = getConfig().getDeleteDayCount();
		Set<String> ignoreAccounts = getConfig().getExcludeUIDs();
		List<Person> persons = _personManager.getAllAlivePersons();
		for (Person person : persons) {
			String username = person.getName();
			if (ignoreAccounts.contains(username))
				continue;

			Date lastLogin = latestLogins.get(person.getName());
			if (lastLogin == null)
				lastLogin = person.getCreated();
			if (lastLogin == null)
				continue; // ignore never created user

			int dayDiff = DateUtil.dayDiff(lastLogin, now, DateUtil.EXCLUDE_FIRST_DATE);
			boolean notified = person.getBooleanValue(Person.ATTR_UNUSED_NOTIFIED);
			boolean notifyEnabled = notifyDayCount > 0, deleteEnabled = deleteDayCount > 0;

			if (notifyEnabled && dayDiff <= notifyDayCount) {
				if (notified) {
					if (!checkOnly) {
						person.setBoolean(Person.ATTR_UNUSED_NOTIFIED, false);
					}
					resetCount++;
				}
			} else if (!deleteEnabled || dayDiff <= deleteDayCount) {
				if (notifyEnabled && !notified) {
					if (!checkOnly) {
						notifyUnused(person, dayDiff);
						person.setBoolean(Person.ATTR_UNUSED_NOTIFIED, true);
					}
					notifiedCount++;
				}
			} else if (deleteEnabled) {
				if (!checkOnly) {
					deleteUnusedAccount(person, dayDiff);
				}
				deletedCount++;
			}
		}
		return ArrayUtil.createIntArray(resetCount, notifiedCount, deletedCount);
	}

	/**
	 * Gets a map containing all users with their last login date.
	 */
	private Map<String, Date> getLatestLogins() {
		try {
			return latestLoginFromDatabase();
		} catch (Exception e) {
			Logger.error("Unable to query last logins for users.", e, UserMonitor.class);
			return Collections.emptyMap();
		}
	}

	private Map<String, Date> latestLoginFromDatabase() throws SQLException {
		DBHelper dialect = KBUtils.getConnectionPool(_kb).getSQLDialect();
		MOClassImpl moClass = (MOClassImpl) _kb.getMORepository().getMetaObject(UserSession.OBJECT_NAME);
		MOAttributeImpl nameAttr = (MOAttributeImpl) moClass.getAttribute(UserSession.NAME_ATTRIBUTE);
		MOAttributeImpl loginAttr = (MOAttributeImpl) moClass.getAttribute(UserSession.LOGIN);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		sb.append(dialect.columnRef(nameAttr.getDBName()));
		sb.append(", MAX(");
		sb.append(dialect.columnRef(loginAttr.getDBName()));
		sb.append(") FROM ");
		sb.append(dialect.tableRef(moClass.getDBName()));
		sb.append(" GROUP BY ");
		sb.append(dialect.columnRef(nameAttr.getDBName()));
		// IGNORE FindBugs(SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING): Dynamic
		// SQL construction is necessary for DBMS abstraction. No user-input is passed to the
		// statement source.
		return DBUtil.executeQuery(sb.toString(), new ResultExtractor<Map<String, Date>>() {
			@Override
			public Map<String, Date> extractResult(ResultSet result) throws SQLException {
				Map<String, Date> resultMap = new HashMap<>();
				while (result.next()) {
					resultMap.put(result.getString(1), dialect.getDate(result, 2));
				}
				return resultMap;
			}
		});
	}

	/**
	 * Informs the given user because of no usage of his account. Here, only a log message is
	 * created. Subclasses may extend this behavior.
	 * 
	 * @param person
	 *        the person to inform
	 * @param dayDiff
	 *        the amount of days the user hasn't logged in for
	 */
	protected void notifyUnused(Person person, int dayDiff) {
		StringBuilder msg = new StringBuilder();
		msg.append("User '");
		msg.append(person.getName());
		msg.append("' has not logged in for ");
		msg.append(dayDiff);
		msg.append(" days. The account will get deleted after no usage of ");
		msg.append(getConfig().getDeleteDayCount());
		msg.append(" days.");
		Logger.info(msg.toString(), UserMonitor.class);
	}

	/**
	 * Deletes the given account because of no usage.
	 * 
	 * @param person
	 *        the account to delete
	 * @param dayDiff
	 *        the amount of days the user hasn't logged in for
	 */
	protected void deleteUnusedAccount(Person person, int dayDiff) {
		StringBuilder msg = new StringBuilder();
		msg.append("User '");
		msg.append(person.getName());
		msg.append("' has not logged in for ");
		msg.append(dayDiff);
		msg.append(" days. The account gets now deleted because it isn't used.");
		Logger.info(msg.toString(), UserMonitor.class);
		_personManager.deleteUser(person);
	}

}

