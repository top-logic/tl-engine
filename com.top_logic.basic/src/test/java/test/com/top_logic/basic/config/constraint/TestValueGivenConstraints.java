/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.constraint;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.config.constraint.impl.ContainedIn;
import com.top_logic.basic.config.constraint.impl.MandatoryIfGiven;
import com.top_logic.basic.config.constraint.impl.MandatoryIfNoneGiven;
import com.top_logic.basic.config.constraint.impl.NotGivenTogether;

/**
 * Test of the constraints {@link MandatoryIfGiven}, {@link MandatoryIfNoneGiven},
 * {@link NotGivenTogether} and {@link ContainedIn}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestValueGivenConstraints extends BasicTestCase {

	/**
	 * User and password are given together.
	 */
	public interface Credentials extends ConfigurationItem {

		String USER = "user";

		String PASSWORD = "password";

		@Name(USER)
		@Constraint(value = MandatoryIfGiven.class, args = @Ref(PASSWORD))
		String getUser();

		void setUser(String value);

		@Name(PASSWORD)
		@Constraint(value = MandatoryIfGiven.class, args = @Ref(USER))
		String getPassword();

		void setPassword(String value);

	}

	/**
	 * Either a connection string or a host name is given.
	 */
	public interface Connection extends ConfigurationItem {

		String URL = "url";

		String HOST = "host";

		@Name(URL)
		@Constraint(value = NotGivenTogether.class, args = @Ref(HOST))
		String getUrl();

		void setUrl(String value);

		@Name(HOST)
		@Constraint(value = MandatoryIfNoneGiven.class, args = @Ref(URL))
		String getHost();

		void setHost(String value);

	}

	/**
	 * A named entry.
	 */
	public interface Entry extends NamedConfigMandatory {
		// Pure name.
	}

	/**
	 * A default naming one of the entries.
	 */
	public interface Entries extends ConfigurationItem {

		String ENTRIES = "entries";

		String DEFAULT = "default";

		@Name(ENTRIES)
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		Map<String, Entry> getEntries();

		@Name(DEFAULT)
		@Constraint(value = ContainedIn.class, args = @Ref(ENTRIES))
		String getDefault();

		void setDefault(String value);

	}

	public void testMandatoryIfGiven() throws ConfigurationException {
		Credentials config = TypedConfiguration.newConfigItem(Credentials.class);
		assertNoFailure(config);

		config.setUser("");
		config.setPassword("");
		assertNoFailure(config);

		config.setUser("admin");
		assertFailure(config, Credentials.PASSWORD);

		config.setPassword("secret");
		assertNoFailure(config);

		config.setUser(null);
		assertFailure(config, Credentials.USER);
	}

	public void testAlternatives() throws ConfigurationException {
		Connection config = TypedConfiguration.newConfigItem(Connection.class);
		assertFailure(config, Connection.HOST);

		config.setUrl("");
		assertFailure(config, Connection.HOST);

		config.setUrl("jdbc:h2:mem:");
		assertNoFailure(config);

		config.setHost("localhost");
		assertFailure(config, Connection.URL);

		config.setUrl("");
		assertNoFailure(config);
	}

	public void testContainedIn() throws ConfigurationException {
		Entries config = TypedConfiguration.newConfigItem(Entries.class);
		assertNoFailure(config);

		config.setDefault("a");
		assertFailure(config, Entries.DEFAULT);

		Entry entry = TypedConfiguration.newConfigItem(Entry.class);
		entry.setName("a");
		config.getEntries().put("a", entry);
		assertNoFailure(config);

		config.setDefault("b");
		assertFailure(config, Entries.DEFAULT);
	}

	private static void assertNoFailure(ConfigurationItem config) throws ConfigurationException {
		assertEquals(List.of(), failures(config));
	}

	private static void assertFailure(ConfigurationItem config, String property) throws ConfigurationException {
		List<ConstraintFailure> failures = failures(config);
		assertEquals(failures.toString(), 1, failures.size());
		assertEquals(property, failures.get(0).getContextProperty().getPropertyName());
	}

	private static List<ConstraintFailure> failures(ConfigurationItem config) throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(config);
		return checker.getFailures();
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(TestValueGivenConstraints.class);
	}

}
