/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.tools.model.TidDiagnostics.TypeState;
import com.top_logic.tl.ai.core.tools.model.TidDiagnostics.Types;

/**
 * Which kind of wrong identifier a model passed, and what it is told to do about it.
 *
 * <p>
 * Every case here was one sentence before - "Invalid TID format. Expected 'TypeName:id', got: …" -
 * for a uuid, for a spoken record code, for a printed object and for a word that is none of those.
 * The classification is what turns a retry loop into one corrected call, so each class is pinned
 * with the remedy it names, not only with the fact that the message changed.
 * </p>
 *
 * <p>
 * Nothing here needs a knowledge base: what the type repository would answer comes through
 * {@link Types}, which is the seam that exists for exactly this.
 * </p>
 *
 * @see TestTidResolverDiagnostics For the same classification reached through a real resolution.
 */
public class TestTidDiagnostics extends TestCase {

	/**
	 * Building the configured hints of a test resolves the configuration types through the
	 * {@code TypeIndex}, which needs the module system.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestTidDiagnostics.class)));
	}

	/** A type repository that knows one concrete type and one abstract one. */
	private static final Types TYPES = typeName -> switch (typeName) {
		case "GenericObject" -> TypeState.INSTANTIABLE;
		case "Item" -> TypeState.ABSTRACT;
		default -> TypeState.UNKNOWN;
	};

	/** A uuid is the id of something else the model read; it is told where the id it wants is. */
	public void testAUuid() {
		String message = TidDiagnostics.explain("8f1c2d3e-4b5a-6c7d-8e9f-0a1b2c3d4e5f", TYPES);

		assertSays("is a uuid", message);
		assertSays("8f1c2d3e-4b5a-6c7d-8e9f-0a1b2c3d4e5f", message);
		assertSays("tool answer", message);
	}

	/** Upper case hex is a uuid too. */
	public void testAUuidInUpperCase() {
		assertSays("is a uuid", TidDiagnostics.explain("8F1C2D3E-4B5A-6C7D-8E9F-0A1B2C3D4E5F", TYPES));
	}

	/** Something merely uuid-shaped in parts is not called a uuid. */
	public void testNotQuiteAUuid() {
		assertSays("is not an object id", TidDiagnostics.explain("8f1c2d3e-4b5a-6c7d", TYPES));
	}

	/** The {@code toString} of an object, pasted out of a log line. */
	public void testAPrintedObject() {
		String message = TidDiagnostics.explain("Person(Karl Meier)", TYPES);

		assertSays("is a printed object", message);
		assertSays("Person(Karl Meier)", message);
	}

	/** A printed object whose closing bracket did not survive the copy is still one. */
	public void testAnUnterminatedPrintedObject() {
		assertSays("is a printed object", TidDiagnostics.explain("Person(Karl", TYPES));
	}

	/** A word that is no identifier of any kind gets the form spelled out. */
	public void testSomethingElseEntirely() {
		String message = TidDiagnostics.explain("foo", TYPES);

		assertSays("'foo' is not an object id", message);
		assertSays("'TypeName:id'", message);
	}

	/** The type in front of the colon is named as the thing that does not exist. */
	public void testAnUnknownType() {
		String message = TidDiagnostics.explain("Invoice:5371", TYPES);

		assertSays("No object type is called 'Invoice'", message);
		assertFalse("A type that does not exist cannot have a missing object: " + message,
			message.contains("not found"));
	}

	/** A type that exists but stores nothing says so, rather than blaming the id. */
	public void testAnAbstractType() {
		String message = TidDiagnostics.explain("Item:5371", TYPES);

		assertSays("'Item' is abstract", message);
	}

	/** An id the engine's parser rejects is named as the wrong half of the pair. */
	public void testAnIdThatIsNoId() {
		String message = TidDiagnostics.explain("GenericObject:no-such-object", TYPES);

		assertSays("The part behind the colon", message);
		assertSays("not an id", message);
	}

	/** So is a missing one. */
	public void testAnEmptyId() {
		assertSays("The part behind the colon", TidDiagnostics.explain("GenericObject:", TYPES));
	}

	/** And a missing type name is named too. */
	public void testAnEmptyType() {
		assertSays("no type in front of the colon", TidDiagnostics.explain(":5371", TYPES));
	}

	/** Type and id are both plausible: then the object is simply not there for this caller. */
	public void testAWellFormedIdThatNamesNothing() {
		String message = TidDiagnostics.explain("GenericObject:5371", TYPES);

		assertSays("Object 'GenericObject:5371' not found", message);
		assertSays("you may not read it", message);
	}

	/** The branch and revision suffixes of an object key do not make the id unreadable. */
	public void testAQualifiedId() {
		assertSays("Object 'GenericObject:5371@42' not found",
			TidDiagnostics.explain("GenericObject:5371@42", TYPES));
	}

	/** Without a type repository, an identifier of the right shape is still answered. */
	public void testWithoutATypeRepository() {
		assertSays("Object 'Invoice:5371' not found", TidDiagnostics.explain("Invoice:5371", Types.NONE));
	}

	/** Nothing at all is a case of its own: there is no string to classify. */
	public void testNothing() {
		assertSays("An object id is required", TidDiagnostics.explain(null, TYPES));
		assertSays("An object id is required", TidDiagnostics.explain("   ", TYPES));
	}

	/** A model that pasted a document gets its own words back, but not all of them. */
	public void testAVeryLongIdentifier() {
		String message = TidDiagnostics.explain("x".repeat(500), TYPES);

		assertTrue("The quoted identifier is cut off: " + message, message.contains("..."));
		assertTrue("A message about one identifier stays short: " + message.length(),
			message.length() < 300);
	}

	/**
	 * The application's hint is the one thing the framework cannot know, and it comes first.
	 */
	public void testAConfiguredHint() {
		try (ObjectIdSettings.Overridden ignored = ObjectIdSettings.override(
			hints(Map.of("[A-Za-z][A-Za-z0-9]*-[0-9]+",
				"is a record code. Search for it with sales__search and use the id from the answer.")))) {

			String message = TidDiagnostics.explain("LEAD-014", Types.NONE);

			assertEquals("'LEAD-014' is a record code. Search for it with sales__search"
				+ " and use the id from the answer.", message);
		}
	}

	/** It is tried before the built-in classes, so an application may rename one of them. */
	public void testAHintWinsOverABuiltInClass() {
		try (ObjectIdSettings.Overridden ignored = ObjectIdSettings.override(
			hints(Map.of("[0-9a-fA-F-]{36}", "is an import key. Look the object up with erp__find.")))) {

			assertSays("is an import key",
				TidDiagnostics.explain("8f1c2d3e-4b5a-6c7d-8e9f-0a1b2c3d4e5f", Types.NONE));
		}
	}

	/** A hint is about the whole identifier, not about a part of it. */
	public void testAHintDoesNotMatchAPart() {
		try (ObjectIdSettings.Overridden ignored = ObjectIdSettings.override(
			hints(Map.of("[A-Z]+-[0-9]+", "is a record code.")))) {

			assertSays("is not an object id", TidDiagnostics.explain("about LEAD-014", Types.NONE));
		}
	}

	/** A hint whose pattern does not compile is ignored, not thrown. */
	public void testABrokenHintIsIgnored() {
		try (ObjectIdSettings.Overridden ignored = ObjectIdSettings.override(
			hints(Map.of("[unclosed", "is a record code.")))) {

			assertSays("is not an object id", TidDiagnostics.explain("foo", Types.NONE));
		}
	}

	/** With no hints configured - the shipped state - the built-in classification is in charge. */
	public void testWithoutHints() {
		assertSays("is not an object id", TidDiagnostics.explain("LEAD-014", Types.NONE));
	}

	/** {@link ModelSecurity} is not consulted here: this class sees strings, never objects. */
	public void testTheDiagnosisNeedsNoPrincipal() {
		assertNotNull(TidDiagnostics.explain("foo", Types.NONE));
	}

	/**
	 * The section as an administrator writes it, in the spelling the shipped configuration
	 * documents.
	 */
	public void testTheConfiguredSectionAsItIsDocumented() throws ConfigurationException {
		ObjectIdConfig section = readSection("""
			<object-ids>
				<hints>
					<hint pattern="[A-Za-z][A-Za-z0-9]*-[0-9]+"
						explanation="is a record code. Search for it with sales__search and use the id from the answer."
					/>
				</hints>
			</object-ids>
			""");

		try (ObjectIdSettings.Overridden ignored = ObjectIdSettings.override(section)) {
			assertSays("is a record code", TidDiagnostics.explain("LEAD-014", Types.NONE));
		}
	}

	private static ObjectIdConfig readSection(String xml) throws ConfigurationException {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(ObjectIdConfig.class);
		ConfigurationReader reader = new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("object-ids", descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		return (ObjectIdConfig) reader.read();
	}

	private static ObjectIdConfig hints(Map<String, String> patternToExplanation) {
		ObjectIdConfig config = TypedConfiguration.newConfigItem(ObjectIdConfig.class);
		for (Map.Entry<String, String> entry : patternToExplanation.entrySet()) {
			ObjectIdConfig.HintConfig hint =
				TypedConfiguration.newConfigItem(ObjectIdConfig.HintConfig.class);
			hint.update(hint.descriptor().getProperty(ObjectIdConfig.HintConfig.PATTERN), entry.getKey());
			hint.update(hint.descriptor().getProperty(ObjectIdConfig.HintConfig.EXPLANATION), entry.getValue());
			config.getHints().add(hint);
		}
		return config;
	}

	private static void assertSays(String expected, String actual) {
		assertTrue("Expected '" + expected + "' in: " + actual, actual.contains(expected));
	}
}
