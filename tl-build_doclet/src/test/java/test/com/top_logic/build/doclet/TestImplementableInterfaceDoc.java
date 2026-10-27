/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.doclet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.spi.ToolProvider;

import junit.framework.TestCase;

import com.top_logic.build.doclet.TLDoclet;

/**
 * Test for the export of the method documentation of interfaces that a TL-Script can implement in
 * a configuration property.
 *
 * <p>
 * Runs the {@link TLDoclet} on the fixtures in <code>src/test/fixtures/scriptable</code>.
 * </p>
 */
public class TestImplementableInterfaceDoc extends TestCase {

	private static final String FIXTURES = "src/test/fixtures/scriptable";

	private static final String PACKAGE = "test.scriptable";

	private Properties _messages;

	private String _output;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		File outDir = new File("target/test-implementable-interface-doc");
		outDir.mkdirs();
		File messagesFile = new File(outDir, "messages_en.properties");
		Files.deleteIfExists(messagesFile.toPath());

		String cp = System.getProperty("java.class.path");
		String[] docArgs = {
			"-sourcepath", FIXTURES,
			"-docletpath", cp,
			"-doclet", TLDoclet.class.getName(),
			"-d", outDir.getPath(),
			"-targetMessages", messagesFile.getPath(),
			PACKAGE,
		};

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try (PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
			int result = ToolProvider.findFirst("javadoc").get().run(out, out, docArgs);
			_output = buffer.toString(StandardCharsets.UTF_8);
			assertEquals(_output, 0, result);
		}

		_messages = new Properties();
		try (InputStream in = Files.newInputStream(messagesFile.toPath())) {
			_messages.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		} catch (IOException ex) {
			fail("No messages written: " + _output);
		}
	}

	/**
	 * An interface bound directly by a property is exported with its implementable methods.
	 */
	public void testDirectBinding() {
		String key = PACKAGE + ".Provider.create";
		assertEquals("Create", _messages.getProperty(key));
		assertEquals("Creates a value for the given context, see <code>#isTransient()</code>.",
			_messages.getProperty(key + ".tooltip"));
		assertEquals("The context of the new value.", _messages.getProperty(key + ".param.context.tooltip"));
		assertEquals("The number of values.", _messages.getProperty(key + ".param.count.tooltip"));
		assertEquals("The created value.", _messages.getProperty(key + ".return"));

		// Default methods can be implemented, static methods not.
		assertEquals("Whether the value is not stored.",
			_messages.getProperty(PACKAGE + ".Provider.isTransient.return"));
		assertNull(_messages.getProperty(PACKAGE + ".Provider.none"));
	}

	/**
	 * Methods of a super interface in the run are keyed with the super interface.
	 */
	public void testSuperInterface() {
		String key = PACKAGE + ".BaseProvider.init";
		assertEquals("Initializes the provider.", _messages.getProperty(key + ".tooltip"));
		assertEquals("The name of the provider.", _messages.getProperty(key + ".param.name.tooltip"));
		assertNull(_messages.getProperty(PACKAGE + ".Provider.init"));
	}

	/**
	 * An interface bound by a sub-interface of the polymorphic configuration, or used as element
	 * type of a collection or map is exported.
	 */
	public void testIndirectBinding() {
		assertEquals("The result of Callback.", _messages.getProperty(PACKAGE + ".Callback.runCallback.return"));
		assertEquals("The result of Listed.", _messages.getProperty(PACKAGE + ".Listed.runListed.return"));
		assertEquals("The result of Mapped.", _messages.getProperty(PACKAGE + ".Mapped.runMapped.return"));
	}

	/**
	 * Interfaces with type parameters and interfaces not referenced from a configuration property
	 * are not exported.
	 */
	public void testNotExported() {
		assertNull(_messages.getProperty(PACKAGE + ".Generic.runGeneric"));
		assertNull(_messages.getProperty(PACKAGE + ".Unused.runUnused"));
	}

	/**
	 * Missing documentation of an implementable method is not reported.
	 */
	public void testNoWarnings() {
		assertFalse(_output, _output.contains("Missing description"));
		assertFalse(_output, _output.contains("Invalid configuration reference"));
		assertEquals("Undocumented", _messages.getProperty(PACKAGE + ".Provider.undocumented"));
	}

}
