/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.knowledge.service.migration._27517;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.sql.Batch;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLOrder;
import com.top_logic.basic.db.sql.SQLQuery.Parameter;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.db.sql.SQLStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DestinationReference;
import com.top_logic.knowledge.service.db2.SourceReference;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.processors.AddApplicationTypesProcessor;
import com.top_logic.knowledge.service.migration.processors.RemoveApplicationTypesProcessor;
import com.top_logic.model.migration.Util;

/**
 * {@link MigrationProcessor} updating person table as migration for Ticket 27517.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Ticket27517UpdatePersonTable extends AbstractConfiguredInstance<Ticket27517UpdatePersonTable.Config>
		implements MigrationProcessor {

	private static final String PASSWORD_COL_HISTORY = "HISTORY";

	private static final String PASSWORD_COL_PASSWORD = "PASSWORD";

	private static final String PASSWORD_COL_ACCOUNT_ID = "ACCOUNT_ID";

	private static final String PASSWORD_DBNAME = "PASSWORD";

	private static final String OLD_PERSON_COL_PWDHISTORY = "PWDHISTORY";

	private static final String OLD_PERSON_COL_LOCALE = "LOCALE";

	private static final String REPRESENTS_USER_DBNAME = "REPRESENTS_USER";

	private static final String USER_COL_USERROLE = "USERROLE";

	private static final String USER_COL_USERPASSWORD = "USERPASSWORD";

	private static final String USER_COL_CN = "CN";

	private static final String USER_DBNAME = "TL_USER";

	private static final String PERSON_COL_CONTACT_ID = "CONTACT_ID";

	private static final String PERSON_COL_ADMIN = "ADMIN";

	private static final String PERSON_COL_RESTRICTED_USER = "RESTRICTED_USER";

	private static final String PERSON_COL_AUTH_DEVICE_ID = "AUTH_DEVICE_ID";

	private static final String PERSON_COL_UNUSED_NOTIFIED = "UNUSED_NOTIFIED";

	private static final String PERSON_COL_TIMEZONE = "TIMEZONE";

	private static final String PERSON_COL_LANGUAGE = "LANGUAGE";

	private static final String PERSON_COL_COUNTRY = "COUNTRY";

	private static final String PERSON_COL_NAME = "NAME";

	private static final String PERSON_FLEX_ATTR_TIMEZONE = "timezone";

	private static final Pattern TL_ADMIN = Pattern.compile("\\btl\\.admin\\b");

	private static final String PERSON_APPLICATION_TYPE_NAME = "Person";

	private static final String PERSON_DBNAME = "PERSON";

	/**
	 * {@link ConfigurationItem} identifying a row in an application object table.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface ObjectRow extends ConfigurationItem {
		
		/**
		 * Value of the {@link BasicTypes#BRANCH_DB_NAME} column.
		 */
		long getBranch();

		/**
		 * Setter for {@link #getBranch()}.
		 */
		void setBranch(long value);

		/**
		 * Value of the {@link BasicTypes#IDENTIFIER_DB_NAME} column.
		 */
		long getIdentifier();

		/**
		 * Setter for {@link #getIdentifier()}.
		 */
		void setIdentifier(long value);

		/**
		 * Value of the {@link BasicTypes#REV_MAX_DB_NAME} column.
		 */
		long getRevMax();

		/**
		 * Setter for {@link #getRevMax()}.
		 */
		void setRevMax(long value);

		/**
		 * Value of the {@link BasicTypes#REV_MIN_DB_NAME} column.
		 */
		long getRevMin();

		/**
		 * Setter for {@link #getRevMin()}.
		 */
		void setRevMin(long value);

		/**
		 * Value of the {@link BasicTypes#REV_CREATE_DB_NAME} column.
		 */
		long getRevCreate();

		/**
		 * Setter for {@link #getRevCreate()}.
		 */
		void setRevCreate(long value);
	}

	/**
	 * {@link ObjectRow} identifying a
	 * {@link Ticket27517UpdatePersonTable#PERSON_APPLICATION_TYPE_NAME}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface PersonRow extends ObjectRow {

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#PERSON_COL_NAME} column.
		 */
		String getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(String value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#OLD_PERSON_COL_LOCALE} column.
		 */
		String getLocale();

		/**
		 * Setter for {@link #getLocale()}.
		 */
		void setLocale(String value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#PERSON_COL_UNUSED_NOTIFIED} column.
		 */
		Boolean isUnusedNotify();

		/**
		 * Setter for {@link #isUnusedNotify()}.
		 */
		void setUnusedNotify(Boolean value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#PERSON_COL_AUTH_DEVICE_ID} column.
		 */
		String getAuthDeviceID();

		/**
		 * Setter for {@link #getAuthDeviceID()}.
		 */
		void setAuthDeviceID(String value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#PERSON_COL_RESTRICTED_USER} column.
		 */
		Boolean isRestrictedUser();

		/**
		 * Setter for {@link #isRestrictedUser()}.
		 */
		void setRestrictedUser(Boolean value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#PERSON_COL_COUNTRY} column.
		 */
		String getCountry();

		/**
		 * Setter for {@link #getCountry()}.
		 */
		void setCountry(String value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#PERSON_COL_LANGUAGE} column.
		 */
		String getLanguage();

		/**
		 * Setter for {@link #getLanguage()}.
		 */
		void setLanguage(String value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#PERSON_COL_TIMEZONE} column.
		 */
		String getTimeZone();

		/**
		 * Setter for {@link #getTimeZone()}.
		 */
		void setTimeZone(String value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#PERSON_COL_ADMIN} column.
		 */
		Boolean isAdmin();

		/**
		 * Setter for {@link #isAdmin()}.
		 */
		void setAdmin(Boolean value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#PERSON_COL_CONTACT_ID} column.
		 */
		Long getContact();

		/**
		 * Setter for {@link #getContact()}.
		 */
		void setContact(Long value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#USER_COL_USERPASSWORD} column.
		 */
		String getPassword();

		/**
		 * Setter for {@link #getPassword()}.
		 */
		void setPassword(String value);

		/**
		 * Value of the {@link Ticket27517UpdatePersonTable#OLD_PERSON_COL_PWDHISTORY} column.
		 */
		String getPasswordHistory();

		/**
		 * Setter for {@link #getPasswordHistory()}.
		 */
		void setPasswordHistory(String value);

	}

	/**
	 * Typed configuration interface definition for {@link Ticket27517UpdatePersonTable}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<Ticket27517UpdatePersonTable> {

		/**
		 * Configuration name for {@link #getPersonTable()}
		 */
		String PERSON_TABLE = "person-table";

		/**
		 * Definition of the person table.
		 */
		@Name(PERSON_TABLE)
		MetaObjectConfig getPersonTable();
	}

	private final String _defaultUserTimeZone;

	private Util _util;

	/**
	 * Create a {@link Ticket27517UpdatePersonTable}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public Ticket27517UpdatePersonTable(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		String configuredTypeName = config.getPersonTable().getObjectName();
		if (!PERSON_APPLICATION_TYPE_NAME.equals(configuredTypeName)) {
			throw new ConfigurationException(I18NConstants.ERROR_PERSON_TABLE_DEFINITION_EXPECTED__ACTUAL_EXPECTED
				.fill(configuredTypeName, PERSON_APPLICATION_TYPE_NAME), Config.PERSON_TABLE, configuredTypeName);
		}
		_defaultUserTimeZone = createDefaultTimeZone();
	}

	private String createDefaultTimeZone() throws ConfigurationException {
		TimeZones.Config timeZonesConfig = (TimeZones.Config) ApplicationConfig
				.getInstance().getServiceConfiguration(TimeZones.class);
		return Utils.formatTimeZone(timeZonesConfig.getDefaultTimeZone());
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		log.info("Migrate persons.");
		try {
			_util = util(context);

			List<PersonRow> persons = readPersons(connection);
			if (persons.isEmpty()) {
				log.info("No entries in persons table found to migrate.");
				return;
			}
			updateCountyAndLanguage(log, persons);
			updateTimeZones(log, connection, persons);
			updateContacts(log, connection, persons);

			Map<String, PersonRow> currentPersonByName = currentPersonByName(log, persons);
			updatePersonsFromUser(log, connection, currentPersonByName);

			recreatePersonTable(context, log, connection);

			insertNewPersons(log, connection, persons);

			createPasswords(log, connection, currentPersonByName.values());
			
			log.info("Persons migration finished.");
		} catch (SQLException ex) {
			log.error("Failure migrating persons: " + ex.getMessage(), ex);
		}
	}

	private Util util(MigrationContext context) {
		return context.getSQLUtils();
	}

	private void createPasswords(Log log, PooledConnection connection, Collection<PersonRow> persons)
			throws SQLException {
		log.info("Insert passwords.");
		List<String> columns = new ArrayList<>();
		List<SQLExpression> values = new ArrayList<>();
		List<Parameter> parameters = new ArrayList<>();

		columns.add(_util.branchColumnOrNull());
		values.add(_util.branchParamOrNull());
		parameters.add(_util.branchParamDef());

		columns.add(BasicTypes.IDENTIFIER_DB_NAME);
		values.add(parameter(DBType.LONG, "identifier"));
		parameters.add(parameterDef(DBType.LONG, "identifier"));

		columns.add(BasicTypes.REV_MAX_DB_NAME);
		values.add(parameter(DBType.LONG, "revMax"));
		parameters.add(parameterDef(DBType.LONG, "revMax"));

		columns.add(BasicTypes.REV_MIN_DB_NAME);
		values.add(parameter(DBType.LONG, "revMin"));
		parameters.add(parameterDef(DBType.LONG, "revMin"));

		columns.add(BasicTypes.REV_CREATE_DB_NAME);
		values.add(parameter(DBType.LONG, "revCreate"));
		parameters.add(parameterDef(DBType.LONG, "revCreate"));

		columns.add(PASSWORD_COL_ACCOUNT_ID);
		values.add(parameter(DBType.LONG, "account"));
		parameters.add(parameterDef(DBType.LONG, "account"));

		columns.add(PASSWORD_COL_PASSWORD);
		values.add(parameter(DBType.STRING, "password"));
		parameters.add(parameterDef(DBType.STRING, "password"));

		columns.add(PASSWORD_COL_HISTORY);
		values.add(parameter(DBType.STRING, "history"));
		parameters.add(parameterDef(DBType.STRING, "history"));

		columns = Util.listWithoutNull(columns);
		values = Util.listWithoutNull(values);

		CompiledStatement insert =
			query(parameters, insert(table(PASSWORD_DBNAME), columns, values)).toSql(connection.getSQLDialect());
		int maxBatchSize = connection.getSQLDialect().getMaxBatchSize(15);
		try (Batch batch = insert.createBatch(connection)) {
			int added = 0;
			for (PersonRow p : persons) {
				String password = p.getPassword();
				if (password.isEmpty()) {
					// No login possible for account
					continue;
				}
				batch.addBatch(
					p.getBranch(),
					_util.newID(connection).toStorageValue(),
					p.getRevMax(),
					p.getRevMin(),
					p.getRevCreate(),
					p.getIdentifier(),
					password,
					p.getPasswordHistory());
				added++;
				if (added >= maxBatchSize) {
					batch.executeBatch();
					added = 0;
				}
			}
			if (added > 0) {
				batch.executeBatch();
			}
		}
	}

	private void recreatePersonTable(MigrationContext context, Log log, PooledConnection connection) {
		MetaObjectConfig personTable = getConfig().getPersonTable();
		RemoveApplicationTypesProcessor.Config dropProcessor =
			TypedConfiguration.newConfigItem(RemoveApplicationTypesProcessor.Config.class);
		dropProcessor.getApplicationTables().add(
			RemoveApplicationTypesProcessor.ApplicationTable.newTable(PERSON_APPLICATION_TYPE_NAME, PERSON_DBNAME));

		AddApplicationTypesProcessor.Config<?> addProcessor =
			TypedConfiguration.newConfigItem(AddApplicationTypesProcessor.Config.class);
		addProcessor.getSchema().getTypes().put(PERSON_APPLICATION_TYPE_NAME, personTable);

		log.info("Delete persons table.");
		execute(context, log, connection, dropProcessor);
		log.info("Re-create persons table.");
		execute(context, log, connection, addProcessor);
	}

	private void execute(MigrationContext context, Log log, PooledConnection connection,
			PolymorphicConfiguration<? extends MigrationProcessor> dropProcessor) {
		TypedConfigUtil.createInstance(dropProcessor).doMigration(context, log, connection);
	}

	private void insertNewPersons(Log log, PooledConnection connection, List<PersonRow> persons) throws SQLException {
		log.info("Insert new persons.");
		List<String> columns = new ArrayList<>();
		List<SQLExpression> values = new ArrayList<>();
		List<Parameter> parameters = new ArrayList<>();

		columns.add(_util.branchColumnOrNull());
		values.add(_util.branchParamOrNull());
		parameters.add(_util.branchParamDef());

		columns.add(BasicTypes.IDENTIFIER_DB_NAME);
		values.add(parameter(DBType.LONG, "identifier"));
		parameters.add(parameterDef(DBType.LONG, "identifier"));

		columns.add(BasicTypes.REV_MAX_DB_NAME);
		values.add(parameter(DBType.LONG, "revMax"));
		parameters.add(parameterDef(DBType.LONG, "revMax"));

		columns.add(BasicTypes.REV_MIN_DB_NAME);
		values.add(parameter(DBType.LONG, "revMin"));
		parameters.add(parameterDef(DBType.LONG, "revMin"));

		columns.add(BasicTypes.REV_CREATE_DB_NAME);
		values.add(parameter(DBType.LONG, "revCreate"));
		parameters.add(parameterDef(DBType.LONG, "revCreate"));

		columns.add(PERSON_COL_NAME);
		values.add(parameter(DBType.STRING, "name"));
		parameters.add(parameterDef(DBType.STRING, "name"));

		columns.add(PERSON_COL_COUNTRY);
		values.add(parameter(DBType.STRING, "country"));
		parameters.add(parameterDef(DBType.STRING, "country"));

		columns.add(PERSON_COL_LANGUAGE);
		values.add(parameter(DBType.STRING, "language"));
		parameters.add(parameterDef(DBType.STRING, "language"));

		columns.add(PERSON_COL_TIMEZONE);
		values.add(parameter(DBType.STRING, PERSON_FLEX_ATTR_TIMEZONE));
		parameters.add(parameterDef(DBType.STRING, PERSON_FLEX_ATTR_TIMEZONE));

		columns.add(PERSON_COL_UNUSED_NOTIFIED);
		values.add(parameter(DBType.BOOLEAN, "unusedNotified"));
		parameters.add(parameterDef(DBType.BOOLEAN, "unusedNotified"));

		columns.add(PERSON_COL_AUTH_DEVICE_ID);
		values.add(parameter(DBType.STRING, "authDevice"));
		parameters.add(parameterDef(DBType.STRING, "authDevice"));

		columns.add(PERSON_COL_RESTRICTED_USER);
		values.add(parameter(DBType.BOOLEAN, "restricted"));
		parameters.add(parameterDef(DBType.BOOLEAN, "restricted"));

		columns.add(PERSON_COL_ADMIN);
		values.add(parameter(DBType.BOOLEAN, "admin"));
		parameters.add(parameterDef(DBType.BOOLEAN, "admin"));

		columns.add(PERSON_COL_CONTACT_ID);
		values.add(parameter(DBType.LONG, "contact"));
		parameters.add(parameterDef(DBType.LONG, "contact"));

		columns = Util.listWithoutNull(columns);
		values = Util.listWithoutNull(values);

		CompiledStatement insert =
			query(parameters, insert(table(PERSON_DBNAME), columns, values)).toSql(connection.getSQLDialect());
		int maxBatchSize = connection.getSQLDialect().getMaxBatchSize(15);
		try (Batch batch = insert.createBatch(connection)) {
			int added = 0;
			for (PersonRow p : persons) {
				Object contact = p.getContact();
				if (contact == null) {
					// ID column is always mandatory. If no contact is specified, a value must still
					// be given.
					contact = IdentifierUtil.nullIdForMandatoryDatabaseColumns().toStorageValue();
				}
				batch.addBatch(
					p.getBranch(),
					p.getIdentifier(),
					p.getRevMax(),
					p.getRevMin(),
					p.getRevCreate(),
					p.getName(),
					p.getCountry(),
					p.getLanguage(),
					p.getTimeZone(),
					p.isUnusedNotify(),
					p.getAuthDeviceID(),
					p.isRestrictedUser(),
					p.isAdmin(),
					contact);
				added++;
				if (added >= maxBatchSize) {
					batch.executeBatch();
					added = 0;
				}
			}
			if (added > 0) {
				batch.executeBatch();
			}
		}
	}

	private void updatePersonsFromUser(Log log, PooledConnection connection, Map<String, PersonRow> currentPersons)
			throws SQLException {
		log.info("Update from users table.");
		List<SQLColumnDefinition> representsUserColumns = columns(
			columnDef(USER_COL_CN),
			columnDef(USER_COL_USERPASSWORD),
			columnDef(USER_COL_USERROLE));
		SQLSelect representsUser =
			select(representsUserColumns, table(USER_DBNAME));
		try (ResultSet result = execQuery(connection, representsUser)) {
			while (result.next()) {
				String login = result.getString(1);
				String password = result.getString(2);
				String allRoles = result.getString(3);
				PersonRow p = currentPersons.get(login);
				if (p == null) {
					log.info("No current person with login '" + login + "' available. Ignoring user.", Protocol.WARN);
					continue;
				}
				p.setAdmin(isAdmin(allRoles));
				p.setPassword(password);
			}
		}
	}

	private Boolean isAdmin(String allRoles) {
		if (StringServices.isEmpty(allRoles)) {
			return false;
		}
		return TL_ADMIN.matcher(allRoles).find();
	}

	private Map<String, PersonRow> currentPersonByName(Log log, List<PersonRow> persons) {
		Map<String, PersonRow> currentPersons = new HashMap<>();
		for (PersonRow p : persons) {
			if (p.getRevMax() < Revision.CURRENT_REV) {
				continue;
			}
			PersonRow clash = currentPersons.put(p.getName(), p);
			if (clash != null) {
				log.info(
					"Multiple current persons with name '" + p.getName() + "', dropping: ID " + clash.getIdentifier(),
					Log.WARN);
			}
		}
		return currentPersons;
	}

	private void updateContacts(Log log, PooledConnection connection, List<PersonRow> persons) throws SQLException {
		log.info("Update user relation.");
		List<SQLColumnDefinition> representsUserColumns = columns(
			_util.branchColumnDef(),
			columnDef(BasicTypes.IDENTIFIER_DB_NAME),
			columnDef(BasicTypes.REV_MAX_DB_NAME),
			columnDef(BasicTypes.REV_MIN_DB_NAME),
			columnDef(_util.refID(SourceReference.REFERENCE_SOURCE_NAME)),
			columnDef(_util.refID(DestinationReference.REFERENCE_DEST_NAME)));
		SQLSelect representsUser =
			select(representsUserColumns, table(REPRESENTS_USER_DBNAME), SQLBoolean.TRUE,
				newOrder(_util.refID(DestinationReference.REFERENCE_DEST_NAME)));
		int personIndex = 0;
		try (ResultSet result = execQuery(connection, representsUser)) {
			long lastBranch, branch = -1;
			long lastRevMax, revMax = -1;
			long lastRevMin, revMin = -1;
			long lastContactID, contactID = -1;
			long lastPersonID, personID = -1;
			while (result.next()) {
				lastBranch = branch;
				lastRevMax = revMax;
				lastRevMin = revMin;
				lastContactID = contactID;
				lastPersonID = personID;
				branch = result.getLong(1);
				revMax = result.getLong(3);
				revMin = result.getLong(4);
				contactID = result.getLong(5);
				personID = result.getLong(6);
				if (branch == lastBranch && personID == lastPersonID) {
					// contact assignment for the same person.
					assert revMin >= lastRevMin : "Results are ordered by branch, person, revMin.";
					if (revMin <= lastRevMax) {
						/* multiple references to person. Relation is de-facto *not* a to-one
						 * reference! */
						log.info("Multiple assignments to person with ID '" + personID + "' on branch '" + branch
								+ "' found. (" + branch + "," + revMin + "," + revMax + ","
								+ contactID + "->" + personID
								+ ") vs. (" + branch + "," + lastRevMin + "," + lastRevMax + ","
								+ lastContactID + "->" + lastPersonID + "). Ignoring data (" + branch + "," + revMin
								+ "," + revMax + "," + contactID + "->"
								+ personID + ").",
							Protocol.WARN);
						continue;
					}
				}
				while (true) {
					if (personIndex == persons.size()) {
						log.info("No Person with ID '" + personID + "' on branch '" + branch
								+ "' found. Ignoring data (" + branch + "," + revMin + "," + revMax + ","
								+ contactID + "->" + personID
							+ ").", Log.WARN);
						break;
					}
					PersonRow p = persons.get(personIndex);
					if (!sameObject(p, branch, personID)) {
						if (branch < p.getBranch() || personID < p.getIdentifier()) {
							log.info("No Person with ID '" + personID + "' on branch '" + branch
									+ "' found. Ignoring data (" + branch + "," + revMin + "," + revMax + ","
									+ contactID + "->" + personID
								+ ").", Log.WARN);
							break;
						}
						personIndex++;
						continue;
					}
					if (revMin > p.getRevMax()) {
						// change after life-range of person. Try next person range.
						personIndex++;
						continue;
					}
					if (revMin < p.getRevMin()) {
						if (revMax < p.getRevMin()) {
							log.info("No Person alive at revision '" + revMin + "'. Ignoring data (" + branch + ","
									+ revMin + "," + revMax + "," + contactID + "->" + personID
								+ ").", Log.WARN);
							break;
						}
						p.setContact(contactID);
						if (revMax > p.getRevMax()) {
							personIndex++;
							continue;
						} else {
							if (revMax < p.getRevMax()) {
								// p.getRevMin() <= revMax < p.getRevMax()
								PersonRow later = TypedConfiguration.copy(p);
								p.setRevMax(revMax);
								later.setRevMin(revMax + 1);
								// Row not valid for later person
								later.setContact(null);
								persons.add(personIndex + 1, later);
							}
							personIndex++;
							break;
						}
					}
					// p.getRevMin() <= revMin <= p.getRevMax()
					if (revMin > p.getRevMin()) {
						// No row entry for change: Create new one.
						PersonRow later = TypedConfiguration.copy(p);
						p.setRevMax(revMin - 1);
						later.setRevMin(revMin);
						persons.add(personIndex + 1, later);
						// process newly added person
						personIndex++;
						continue;
					}
					// p.getRevMin() == revMin
					p.setContact(contactID);
					if (revMax == p.getRevMax()) {
						personIndex++;
						break;
					}
					if (revMax > p.getRevMax()) {
						personIndex++;
						continue;
					}
					// No row entry for "end of lifetime change": Create new one
					PersonRow later = TypedConfiguration.copy(p);
					later.setContact(null);
					p.setRevMax(revMax);
					later.setRevMin(revMax + 1);
					persons.add(personIndex + 1, later);
					personIndex++;
					break;
				}
			}
		}
	}

	private void updateTimeZones(Log log, PooledConnection connection, List<PersonRow> persons) throws SQLException {
		log.info("Updating timezones.");
		List<SQLColumnDefinition> timezonesColumns = columns(
			_util.branchColumnDef(),
			columnDef(BasicTypes.IDENTIFIER_DB_NAME),
			columnDef(BasicTypes.REV_MAX_DB_NAME),
			columnDef(BasicTypes.REV_MIN_DB_NAME),
			columnDef(AbstractFlexDataManager.VARCHAR_DATA_DBNAME));
		SQLExpression where = and(
			eq(column(AbstractFlexDataManager.TYPE_DBNAME), literalString(PERSON_APPLICATION_TYPE_NAME)),
			eq(column(AbstractFlexDataManager.ATTRIBUTE_DBNAME), literalString(PERSON_FLEX_ATTR_TIMEZONE)));
		SQLSelect timezones =
			select(timezonesColumns, table(AbstractFlexDataManager.FLEX_DATA_DB_NAME), where,
				newOrder(BasicTypes.IDENTIFIER_DB_NAME));
		int personIndex = 0;
		try (ResultSet result = execQuery(connection, timezones)) {
			while (result.next()) {
				long branch = result.getLong(1);
				long personID = result.getLong(2);
				long revMax = result.getLong(3);
				long revMin = result.getLong(4);
				String timezone = result.getString(5);
				while (true) {
					if (personIndex == persons.size()) {
						log.info("No Person with ID '" + personID + "' on branch '" + branch
								+ "' found. Ignoring data (" + branch + "," + revMin + "," + revMax + "," + timezone
							+ ").", Log.WARN);
						break;
					}
					PersonRow p = persons.get(personIndex);
					if (!sameObject(p, branch, personID)) {
						if (branch < p.getBranch() || personID < p.getIdentifier()) {
							log.info("No Person with ID '" + personID + "' on branch '" + branch
									+ "' found. Ignoring data (" + branch + "," + revMin + "," + revMax + "," + timezone
								+ ").", Log.WARN);
							break;
						}
						personIndex++;
						continue;
					}
					if (revMin > p.getRevMax()) {
						// change after life-range of person. Try next person range.
						personIndex++;
						continue;
					}
					if (revMin < p.getRevMin()) {
						if (revMax < p.getRevMin()) {
							log.info("No Person alive at revision '" + revMin + "'. Ignoring data (" + branch + ","
								+ revMin + "," + revMax + "," + timezone + ").", Log.WARN);
							break;
						}
						p.setTimeZone(timezone);
						if (revMax > p.getRevMax()) {
							personIndex++;
							continue;
						} else {
							if (revMax < p.getRevMax()) {
								// p.getRevMin() <= revMax < p.getRevMax()
								PersonRow later = TypedConfiguration.copy(p);
								p.setRevMax(revMax);
								later.setRevMin(revMax + 1);
								// Row not valid for later person
								later.setTimeZone(null);
								persons.add(personIndex + 1, later);
							}
							personIndex++;
							break;
						}
					}
					// p.getRevMin() <= revMin <= p.getRevMax()
					if (revMin > p.getRevMin()) {
						// No row entry for change: Create new one
						PersonRow later = TypedConfiguration.copy(p);
						p.setRevMax(revMin - 1);
						later.setRevMin(revMin);
						persons.add(personIndex + 1, later);
						// process newly added person
						personIndex++;
						continue;
					}
					// p.getRevMin() == revMin
					p.setTimeZone(timezone);
					if (revMax == p.getRevMax()) {
						personIndex++;
						break;
					}
					if (revMax > p.getRevMax()) {
						personIndex++;
						continue;
					}
					// No row entry for "end of lifetime change": Create new one
					PersonRow later = TypedConfiguration.copy(p);
					later.setTimeZone(null);
					p.setRevMax(revMax);
					later.setRevMin(revMax + 1);
					persons.add(personIndex + 1, later);
					personIndex++;
					break;
				}
			}
		}
		// Update timezone that is used by default and that was not stored before.
		persons.stream().filter(p -> p.getTimeZone().isEmpty()).forEach(p -> p.setTimeZone(_defaultUserTimeZone));

		log.info("Remove old timezone data.");

		CompiledStatement statement =
			toStatement(delete(table(AbstractFlexDataManager.FLEX_DATA_DB_NAME), where), connection);
		statement.executeUpdate(connection);
	}

	private List<SQLOrder> newOrder(String idColumn) {
		List<SQLOrder> order;
		String branchColumn = _util.branchColumnOrNull();
		if (branchColumn != null) {
			order = orders(
				order(false, column(branchColumn)),
				order(false, column(idColumn)),
				order(false, column(BasicTypes.REV_MIN_DB_NAME)));
		} else {
			order = orders(
				order(false, column(idColumn)),
				order(false, column(BasicTypes.REV_MIN_DB_NAME)));
		}
		return order;
	}

	private boolean sameObject(PersonRow p, long branch, long identifier) {
		boolean sameObject = (p.getBranch() == branch) && (p.getIdentifier() == identifier);
		return sameObject;
	}

	private void updateCountyAndLanguage(Log log, List<PersonRow> persons) {
		log.info("Update country and language.");
		for (PersonRow p : persons) {
			String locale = p.getLocale();
			if (locale.isEmpty()) {
				continue;
			}
			Locale localeFromString = ResourcesModule.localeFromString(locale);
			p.setLanguage(localeFromString.getLanguage());
			p.setCountry(localeFromString.getCountry());
		}
	}

	private List<PersonRow> readPersons(PooledConnection connection) throws SQLException {
		List<SQLColumnDefinition> personColumns = columns(
			_util.branchColumnDef(),
			columnDef(BasicTypes.IDENTIFIER_DB_NAME),
			columnDef(BasicTypes.REV_MAX_DB_NAME),
			columnDef(BasicTypes.REV_MIN_DB_NAME),
			columnDef(BasicTypes.REV_CREATE_DB_NAME),
			columnDef(PERSON_COL_NAME),
			columnDef(OLD_PERSON_COL_LOCALE),
			columnDef(PERSON_COL_UNUSED_NOTIFIED),
			columnDef(PERSON_COL_AUTH_DEVICE_ID),
			columnDef(PERSON_COL_RESTRICTED_USER),
			columnDef(OLD_PERSON_COL_PWDHISTORY));
		SQLSelect personSelect =
			select(personColumns, table(PERSON_DBNAME), SQLBoolean.TRUE, newOrder(BasicTypes.IDENTIFIER_DB_NAME));
		List<PersonRow> persons = new ArrayList<>();
		try (ResultSet result = execQuery(connection, personSelect)) {
			while (result.next()) {
				PersonRow p = TypedConfiguration.newConfigItem(PersonRow.class);
				p.setBranch(result.getLong(1));
				p.setIdentifier(result.getLong(2));
				p.setRevMax(result.getLong(3));
				p.setRevMin(result.getLong(4));
				p.setRevCreate(result.getLong(5));
				p.setName(result.getString(6));
				p.setLocale(result.getString(7));
				p.setUnusedNotify(result.getBoolean(8));
				p.setAuthDeviceID(result.getString(9));
				p.setRestrictedUser(result.getBoolean(10));
				p.setPasswordHistory(result.getString(11));
				persons.add(p);
			}
		}
		return persons;
	}

	void orderPersons(List<PersonRow> persons) {
		persons.sort((p1, p2) -> {
			int branchCompare = Long.compare(p1.getBranch(), p2.getBranch());
			if (branchCompare != 0) {
				return branchCompare;
			}
			int idCompare = Long.compare(p1.getIdentifier(), p2.getIdentifier());
			if (idCompare != 0) {
				return idCompare;
			}
			return Long.compare(p1.getRevMin(), p2.getRevMin());
		});
	}

	private ResultSet execQuery(PooledConnection connection, SQLSelect personSelect) throws SQLException {
		return toStatement(personSelect, connection).executeQuery(connection);
	}

	private CompiledStatement toStatement(SQLStatement select, PooledConnection connection) throws SQLException {
		return query(select).toSql(connection.getSQLDialect());
	}

}
