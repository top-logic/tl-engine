/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPoolBase.DataSourceConfig;
import com.top_logic.basic.sql.AbstractConfiguredConnectionPoolBase.DataSourceConfig.EncryptedDriverOption;
import com.top_logic.basic.sql.DefaultDataSourceFactory;
import com.top_logic.basic.sql.LoggingDataSourceFactory;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.SecretOptionsDataSource;
import com.top_logic.basic.vars.VariableExpander;

/**
 * Tests the database password given as {@link EncryptedDriverOption} with a secret source: the
 * attribute forms, the reference to an environment variable read at each new connection, and the
 * written form of the configuration.
 */
@SuppressWarnings("javadoc")
public class TestSecretDriverOptions extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	/** The system property holding the password in the tests referencing a variable. */
	private static final String VARIABLE = "test_secret_driver_options_password";

	private static final String ALIAS = "%TEST_SECRET_DRIVER_OPTIONS_PASSWD%";

	private static final String USER = "sa";

	private static final String PASSWORD_OPTION = "password";

	private static final String PASSWORD = "first-password";

	private static final String ROTATED_PASSWORD = "second-password";

	private static int _databaseCounter;

	public interface Config extends ConfigurationItem {

		String DATA_SOURCE = "data-source";

		@Name(DATA_SOURCE)
		@ItemDefault
		@ImplementationClassDefault(DefaultDataSourceFactory.class)
		DataSourceConfig getDataSource();

	}

	private String _url;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_url = "jdbc:h2:mem:TestSecretDriverOptions" + (++_databaseCounter) + ";DB_CLOSE_DELAY=-1";
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** A password from an environment variable is read each time a connection is opened. */
	public void testEnvironmentPasswordRotation() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		DataSource dataSource = dataSource(read(config("", environmentPassword())));
		assertTrue(dataSource instanceof SecretOptionsDataSource);

		assertRotation(dataSource);
	}

	/** Through a {@link LoggingDataSourceFactory}, a rotated password is used as well. */
	public void testEnvironmentPasswordRotationLogging() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		DataSource dataSource = dataSource(read(config(
			" class='" + LoggingDataSourceFactory.class.getName() + "'", environmentPassword())));
		assertTrue(dataSource instanceof LoggingDataSourceProxy);

		assertRotation(dataSource);
	}

	/** An undefined variable is reported when a connection is opened, without the password. */
	public void testEnvironmentPasswordUndefined() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		DataSource dataSource = dataSource(read(config("", environmentPassword())));
		try (Connection connection = dataSource.getConnection()) {
			assertConnected(connection);

			System.clearProperty(VARIABLE);
			try (Connection other = dataSource.getConnection()) {
				fail("Connection opened without password.");
			} catch (SQLException ex) {
				assertTrue(ex.getMessage(), ex.getMessage().contains(VARIABLE));
				assertFalse(ex.getMessage(), ex.getMessage().contains(PASSWORD));
			}
			shutdown(connection);
		}
	}

	/** The attribute with prefix <code>unencrypted:</code> keeps working. */
	public void testUnencryptedAttribute() throws Exception {
		assertConnects(read(config("", encryptedOption("unencrypted:" + PASSWORD))));
	}

	/** An encrypted attribute keeps working. */
	public void testEncryptedAttribute() throws Exception {
		assertConnects(read(config("", encryptedOption(ConfigurationEncryption.encrypt(PASSWORD)))));
	}

	/** An alias resolved to an encoded password keeps working. */
	public void testAliasAttribute() throws Exception {
		VariableExpander expander = new VariableExpander();
		AliasManager.putVariable(expander, ALIAS, "unencrypted:" + PASSWORD);
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSecretDriverOptions.class);
		ConfigurationReader reader = new ConfigurationReader(context, getDescriptors());
		reader.setVariableExpander(expander);
		reader.setSource(CharacterContents.newContent(config("", encryptedOption(ALIAS))));
		Config config = (Config) reader.read();
		context.checkErrors();

		assertConnects(config);
	}

	/** An empty encrypted option is passed as empty value, as before. */
	public void testEmptyPassword() throws Exception {
		Config config = read(config("", encryptedOption("")));
		DataSource dataSource = dataSource(config);
		assertFalse("No secret to read.", dataSource instanceof SecretOptionsDataSource);
		try (Connection connection = dataSource.getConnection()) {
			assertConnected(connection);
			shutdown(connection);
		}
	}

	/** A reference to a named secret is written as element without class holding only the name. */
	public void testWriteEnvironmentPassword() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		Config config = read(config("", environmentPassword()));

		String xml = toXML(config);
		assertFalse("Password written: " + xml, xml.contains(PASSWORD));
		assertTrue("Not written in short form: " + xml, xml.contains("<value name=\"" + VARIABLE + "\""));

		Config copy = (Config) fromXML(xml);
		assertTrue(password(copy) instanceof NamedSecretSource.Config);
		assertEquals(xml, toXML(copy));
		assertConnects(copy);
	}

	/** A configured password is written encrypted. */
	public void testWriteLiteralPassword() throws Exception {
		Config config = read(config("", encryptedOption("unencrypted:" + PASSWORD)));

		String xml = toXML(config);
		assertFalse("Password written in plain text: " + xml, xml.contains(PASSWORD));
		assertTrue("Not written as encrypted attribute: " + xml,
			xml.contains(EncryptedDriverOption.VALUE + "=\"" + ConfigurationEncryption.AUTHENTICATED_PREFIX));

		Config copy = (Config) fromXML(xml);
		assertTrue(password(copy) instanceof LiteralSecretSource.Config);
		assertConnects(copy);
	}

	private void assertRotation(DataSource dataSource) throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			assertConnected(connection);
			try (Statement statement = connection.createStatement()) {
				statement.execute("ALTER USER " + USER + " SET PASSWORD '" + ROTATED_PASSWORD + "'");
			}

			try (Connection stale = dataSource.getConnection()) {
				fail("Connection opened with the replaced password.");
			} catch (SQLException ex) {
				// Expected: the variable still holds the replaced password.
			}

			System.setProperty(VARIABLE, ROTATED_PASSWORD);
			try (Connection rotated = dataSource.getConnection()) {
				assertConnected(rotated);
			}
			assertConnected(connection);
			shutdown(connection);
		}
	}

	private void assertConnects(Config config) throws Exception {
		DataSource dataSource = dataSource(config);
		try (Connection connection = dataSource.getConnection()) {
			assertConnected(connection);

			// The database was created with the given password, a different one is refused.
			DataSource sameDatabase = dataSource(read(config("", encryptedOption("unencrypted:other"))));
			try (Connection other = sameDatabase.getConnection()) {
				fail("Connection opened with a wrong password.");
			} catch (SQLException ex) {
				// Expected.
			}
			shutdown(connection);
		}
	}

	private static void assertConnected(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			assertTrue(statement.execute("SELECT 1"));
		}
	}

	/**
	 * Drops the in-memory database, that is kept open between connections (DB_CLOSE_DELAY=-1) so
	 * that each connection checks the password the database was created with.
	 */
	private static void shutdown(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("SHUTDOWN");
		}
	}

	private static PolymorphicConfiguration<? extends SecretSource> password(Config config) {
		return ((EncryptedDriverOption) config.getDataSource().getDriverOptions().get(PASSWORD_OPTION)).getValue();
	}

	private DataSource dataSource(Config config) throws ConfigurationException {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config.getDataSource())
			.createDataSource(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
	}

	private String config(String dataSourceAttributes, String passwordOption) {
		return "<" + ROOT_TAG + "><" + Config.DATA_SOURCE + dataSourceAttributes + ">"
			+ "<driver-class>org.h2.jdbcx.JdbcDataSource</driver-class>"
			+ "<options>"
			+ "<option name='url' value='" + _url + "'/>"
			+ "<option name='user' value='" + USER + "'/>"
			+ passwordOption
			+ "</options>"
			+ "</" + Config.DATA_SOURCE + "></" + ROOT_TAG + ">";
	}

	private static String environmentPassword() {
		return "<encrypted-option name='" + PASSWORD_OPTION + "'><value name='" + VARIABLE + "'/></encrypted-option>";
	}

	private static String encryptedOption(String value) {
		return "<encrypted-option name='" + PASSWORD_OPTION + "' value='" + value + "'/>";
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestSecretDriverOptions.class);
	}

}
