/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;

import com.top_logic.basic.AliasDefinition;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.AliasSource;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.io.FileUtilities;

/**
 * Test for the {@link AliasDefinition descriptions} of configured aliases and the values stored in
 * the application ({@link AliasManager#storeValue(String, String)}).
 *
 * <p>
 * Environment values are given as system properties, since environment variables cannot be set
 * from a test.
 * </p>
 */
public class TestAliasDefinitions extends BasicTestCase {

	private static final String SET_PROPERTY = "test_alias_definitions_set";

	private static final String UNSET_PROPERTY = "test_alias_definitions_unset";

	private static final String EXPLICIT = "%EXPLICIT%";

	private static final String DEFAULTED = "%DEFAULTED%";

	private static final String NO_DEFAULT = "%NO_DEFAULT%";

	private static final String IMPLICIT = "%Implicit-Var.1%";

	private static final String IMPLICIT_PROPERTY = "tl_implicit_var_1";

	private static final String PLAIN = "%PLAIN%";

	private static final String EXPLICIT_ONLY = "%EXPLICIT_ONLY%";

	private static final String EXPLICIT_ONLY_IMPLICIT_PROPERTY = "tl_explicit_only";

	private static final String UNKNOWN = "%UNKNOWN_ALIAS%";

	private static final List<String> SYSTEM_PROPERTIES =
		Arrays.asList(SET_PROPERTY, IMPLICIT_PROPERTY, EXPLICIT_ONLY_IMPLICIT_PROPERTY);

	private File _folder;

	private File _file;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		for (String property : SYSTEM_PROPERTIES) {
			assertNull("System property '" + property + "' already set.", System.getProperty(property));
		}
		assertNull(System.getProperty(UNSET_PROPERTY));

		_folder = createNamedTestFile("aliasDefinitions");
		FileUtilities.deleteR(_folder);
		FileUtilities.enforceDirectory(_folder);
		_file = new File(_folder, AliasManager.STORED_VALUES_FILE);
	}

	@Override
	protected void tearDown() throws Exception {
		for (String property : SYSTEM_PROPERTIES) {
			System.clearProperty(property);
		}
		_folder.setWritable(true);
		FileUtilities.deleteR(_folder);
		super.tearDown();
	}

	/**
	 * The environment names of a definition with <code>${env:...}</code> references are recorded in
	 * definition order, the first one set determines the source, and a stored value is ignored.
	 */
	public void testExplicitEnvironmentWinsOverStored() throws IOException {
		System.setProperty(SET_PROPERTY, "set");
		writeStored(EXPLICIT, "stored");

		AliasManager aliases = create(_file);
		AliasDefinition definition = aliases.getDefinition(EXPLICIT);

		assertEquals(EXPLICIT, definition.getName());
		assertEquals(explicitDefinition(), definition.getDefinition());
		assertEquals(Arrays.asList(UNSET_PROPERTY, SET_PROPERTY), definition.getEnvironmentNames());
		assertFalse(definition.isImplicitEnvironmentName());
		assertEquals("a-b", definition.getDefaultValue());
		assertEquals(AliasSource.SYSTEM_PROPERTY, definition.getSource());
		assertEquals(SET_PROPERTY, definition.getSourceName());
		assertEquals("a-set", definition.getValue());
		assertEquals("a-set", definition.getEffectiveValue());
		assertEquals("a-set", aliases.getAlias(EXPLICIT));
		assertEquals("stored", definition.getStoredValue());
		assertFalse("A stored value is not used while an environment name is set.", definition.isChangePending());
	}

	/**
	 * A stored value replaces the whole definition, when no environment name of the alias is set.
	 */
	public void testStoredWinsOverDefinition() throws IOException {
		writeStored(EXPLICIT, "stored");
		writeStored(DEFAULTED, "stored %PLAIN%");

		AliasManager aliases = create(_file);

		AliasDefinition explicit = aliases.getDefinition(EXPLICIT);
		assertEquals(AliasSource.STORED, explicit.getSource());
		assertNull(explicit.getSourceName());
		assertEquals("stored", explicit.getValue());
		assertEquals("a-b", explicit.getDefaultValue());
		assertFalse(explicit.isChangePending());

		AliasDefinition defaulted = aliases.getDefinition("DEFAULTED");
		assertEquals(AliasSource.STORED, defaulted.getSource());
		assertEquals("stored %PLAIN%", defaulted.getValue());
		assertEquals("Stored value references other aliases.", "stored plain", defaulted.getEffectiveValue());
	}

	/**
	 * Without environment and stored value, the defaults of the definition apply.
	 */
	public void testDefinition() {
		AliasManager aliases = create(_file);

		AliasDefinition defaulted = aliases.getDefinition(DEFAULTED);
		assertEquals(AliasSource.DEFINITION, defaulted.getSource());
		assertEquals("default", defaulted.getValue());
		assertEquals("default", defaulted.getDefaultValue());
		assertNull(defaulted.getStoredValue());
		assertFalse(defaulted.isChangePending());

		AliasDefinition noDefault = aliases.getDefinition(NO_DEFAULT);
		assertEquals(AliasSource.DEFINITION, noDefault.getSource());
		assertEquals("x--y", noDefault.getValue());

		AliasDefinition plain = aliases.getDefinition(PLAIN);
		assertEquals(AliasSource.DEFINITION, plain.getSource());
		assertTrue(plain.isImplicitEnvironmentName());
		assertEquals(Collections.singletonList("tl_plain"), plain.getEnvironmentNames());
		assertEquals("plain", plain.getValue());
	}

	/**
	 * An alias without <code>${env:...}</code> reference is set as a whole by its implicit
	 * environment name.
	 */
	public void testImplicitEnvironmentName() {
		assertEquals("tl_storage_path", AliasManager.implicitEnvironmentName("%STORAGE_PATH%"));
		assertEquals("tl_ldap_url", AliasManager.implicitEnvironmentName("LDAP_URL"));
		assertEquals(IMPLICIT_PROPERTY, AliasManager.implicitEnvironmentName(IMPLICIT));

		System.setProperty(IMPLICIT_PROPERTY, "implicit");

		AliasManager aliases = create(_file);
		AliasDefinition definition = aliases.getDefinition(IMPLICIT);
		assertTrue(definition.isImplicitEnvironmentName());
		assertEquals(Collections.singletonList(IMPLICIT_PROPERTY), definition.getEnvironmentNames());
		assertEquals("implicit %PLAIN%", definition.getDefaultValue());
		assertEquals(AliasSource.SYSTEM_PROPERTY, definition.getSource());
		assertEquals(IMPLICIT_PROPERTY, definition.getSourceName());
		assertEquals("implicit", definition.getValue());
		assertEquals("implicit", aliases.getAlias(IMPLICIT));
	}

	/**
	 * The implicit environment name does not apply to an alias with <code>${env:...}</code>
	 * reference.
	 */
	public void testNoImplicitNameForExplicitDefinition() {
		System.setProperty(EXPLICIT_ONLY_IMPLICIT_PROPERTY, "implicit");

		AliasManager aliases = create(_file);
		AliasDefinition definition = aliases.getDefinition(EXPLICIT_ONLY);
		assertFalse(definition.isImplicitEnvironmentName());
		assertEquals(Collections.singletonList(UNSET_PROPERTY), definition.getEnvironmentNames());
		assertEquals(AliasSource.DEFINITION, definition.getSource());
		assertEquals("only", definition.getValue());
	}

	/**
	 * Programmatic aliases and unknown names have no description and cannot be stored.
	 */
	public void testProgrammaticAndUnknownRefused() throws IOException {
		AliasManager aliases = create(_file);

		assertNull(aliases.getDefinition(AliasManager.APP_ROOT));
		assertEquals("Configured value of a programmatic alias is resolved.", "/configured-root",
			aliases.getAlias(AliasManager.APP_ROOT));
		assertNull(aliases.getDefinition(UNKNOWN));
		assertNull(aliases.getDefinition("%IN%VALID%"));
		for (AliasDefinition definition : aliases.getDefinitions()) {
			assertFalse(AliasManager.APP_ROOT.equals(definition.getName()));
		}

		assertStoreRefused(aliases, AliasManager.APP_ROOT);
		assertStoreRefused(aliases, UNKNOWN);
		assertFalse(_file.exists());
	}

	/**
	 * A stored value is written to the file, reported as pending until the aliases are resolved
	 * again, and removed again.
	 */
	public void testStoreAndRemove() throws IOException {
		AliasManager aliases = create(_file);
		assertTrue(aliases.canStoreValues());

		String value = "new value ü";
		aliases.storeValue("DEFAULTED", value);

		String content = new String(Files.readAllBytes(_file.toPath()), StandardCharsets.UTF_8);
		assertTrue("Value not stored in UTF-8: " + content, content.contains(value));

		AliasDefinition pending = aliases.getDefinition(DEFAULTED);
		assertEquals(value, pending.getStoredValue());
		assertEquals("Running configuration unchanged.", AliasSource.DEFINITION, pending.getSource());
		assertEquals("default", pending.getValue());
		assertEquals("default", aliases.getAlias(DEFAULTED));
		assertTrue(pending.isChangePending());

		aliases.reload();
		AliasDefinition stored = aliases.getDefinition(DEFAULTED);
		assertEquals(AliasSource.STORED, stored.getSource());
		assertEquals(value, stored.getValue());
		assertFalse(stored.isChangePending());

		aliases.storeValue(EXPLICIT, "other");
		aliases.removeValue(DEFAULTED);
		AliasDefinition removed = aliases.getDefinition(DEFAULTED);
		assertNull(removed.getStoredValue());
		assertEquals(AliasSource.STORED, removed.getSource());
		assertTrue(removed.isChangePending());
		assertEquals("other", aliases.getDefinition(EXPLICIT).getStoredValue());

		// Removing an absent value is a no-op.
		aliases.removeValue(UNKNOWN);

		aliases.reload();
		assertEquals(AliasSource.DEFINITION, aliases.getDefinition(DEFAULTED).getSource());
		assertEquals(AliasSource.STORED, aliases.getDefinition(EXPLICIT).getSource());

		File[] files = _folder.listFiles();
		assertEquals("Temporary file left over: " + Arrays.toString(files), 1, files.length);
	}

	/**
	 * A missing autoconf folder is created when its parent is writable.
	 */
	public void testMissingFolderCreated() {
		File folder = new File(_folder, "autoconf");
		File file = new File(folder, AliasManager.STORED_VALUES_FILE);
		AliasManager aliases = create(file);
		assertTrue(aliases.canStoreValues());

		aliases.storeValue(PLAIN, "value");
		assertTrue(file.isFile());
		assertEquals("value", aliases.getDefinition(PLAIN).getStoredValue());

		File missingParent = new File(new File(_folder, "missing"), "autoconf");
		assertFalse(create(new File(missingParent, AliasManager.STORED_VALUES_FILE)).canStoreValues());
	}

	/**
	 * Values cannot be stored to a read-only folder.
	 */
	public void testReadOnlyFolder() throws IOException {
		writeStored(DEFAULTED, "stored");
		assertTrue(_folder.setWritable(false));
		if (Files.isWritable(_folder.toPath())) {
			// Running with privileges that ignore file permissions.
			return;
		}

		AliasManager aliases = create(_file);
		assertFalse(aliases.canStoreValues());
		assertEquals("Stored values are read from a read-only folder.", AliasSource.STORED,
			aliases.getDefinition(DEFAULTED).getSource());
		assertStoreRefused(aliases, PLAIN);
		try {
			aliases.removeValue(DEFAULTED);
			fail("Removing from a read-only folder must fail.");
		} catch (I18NRuntimeException ex) {
			// Expected.
		}
		assertEquals("stored", aliases.getDefinition(DEFAULTED).getStoredValue());
	}

	/**
	 * An {@link AliasManager} without file for stored values cannot store.
	 */
	public void testNoStoredValuesFile() {
		AliasManager aliases = create(null);
		assertFalse(aliases.canStoreValues());
		assertNull(aliases.getDefinition(DEFAULTED).getStoredValue());
		assertStoreRefused(aliases, PLAIN);
	}

	private static void assertStoreRefused(AliasManager aliases, String name) {
		try {
			aliases.storeValue(name, "value");
			fail("Storing a value for '" + name + "' must fail.");
		} catch (I18NRuntimeException ex) {
			// Expected.
		}
	}

	private void writeStored(String name, String value) throws IOException {
		Properties values = new Properties();
		if (_file.exists()) {
			try (Reader reader = Files.newBufferedReader(_file.toPath(), StandardCharsets.UTF_8)) {
				values.load(reader);
			}
		}
		values.setProperty(name, value);
		try (OutputStream out = Files.newOutputStream(_file.toPath());
				Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
			values.store(writer, null);
		}
	}

	private static String explicitDefinition() {
		return "${env:" + UNSET_PROPERTY + ":a}-${env:" + SET_PROPERTY + ":b}";
	}

	private static AliasManager create(File file) {
		Map<String, String> definitions = new LinkedHashMap<>();
		definitions.put(EXPLICIT, explicitDefinition());
		definitions.put(DEFAULTED, "${env:" + UNSET_PROPERTY + ":default}");
		definitions.put(NO_DEFAULT, "x-${env:" + UNSET_PROPERTY + "}-y");
		definitions.put(IMPLICIT, "implicit %PLAIN%");
		definitions.put(PLAIN, "plain");
		definitions.put(EXPLICIT_ONLY, "${env:" + UNSET_PROPERTY + ":only}");
		definitions.put(AliasManager.APP_ROOT, "/configured-root");

		Map<String, String> programmatic = new HashMap<>();
		programmatic.put(AliasManager.APP_ROOT, "/root");

		TestingAliasManager result = new TestingAliasManager(definitions, file);
		result.setBaseAliases(programmatic);
		result.reload();
		return result;
	}

	/**
	 * {@link AliasManager} with given definitions and file for stored values.
	 */
	private static final class TestingAliasManager extends AliasManager {

		private final Map<String, String> _definitions;

		private final File _file;

		TestingAliasManager(Map<String, String> definitions, File file) {
			_definitions = definitions;
			_file = file;
		}

		@Override
		public boolean usesXMLProperties() {
			return false;
		}

		@Override
		protected Map<String, String> resolveConfiguredAliases() {
			return resolveDefinitions(_definitions);
		}

		@Override
		public File getStoredValuesFile() {
			return _file;
		}

	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestAliasDefinitions.class);
	}

}
