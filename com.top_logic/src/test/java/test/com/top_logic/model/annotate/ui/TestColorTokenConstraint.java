/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.annotate.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.gui.DesignTokenKind;
import com.top_logic.gui.DesignTokenService;
import com.top_logic.model.annotate.ui.ColorSpec;
import com.top_logic.model.annotate.ui.ColorTokenOptions;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;

/**
 * Test for the design token check of {@link ColorSpec#getToken()}.
 */
@SuppressWarnings("javadoc")
public class TestColorTokenConstraint extends BasicTestCase {

	/** A color token the {@link Vocabulary} of this test knows. */
	private static final String KNOWN_TOKEN = "support-success";

	/** A token no {@link DesignTokenKind#COLOR} vocabulary of this test knows. */
	private static final String UNKNOWN_TOKEN = "support-nonesuch";

	public void testKnownTokenIsAccepted() throws ConfigurationException {
		assertNoProblem(checkClassifier(KNOWN_TOKEN));
	}

	public void testUnknownTokenIsReported() throws ConfigurationException {
		List<ConstraintFailure> failures = checkClassifier(UNKNOWN_TOKEN);
		assertEquals("The unknown token is reported once: " + failures, 1, failures.size());

		ConstraintFailure failure = failures.get(0);
		assertTrue("An unknown token is a warning, the model still loads.", failure.isWarning());

		List<Object> arguments = Arrays.asList(failure.getConstraintName().arguments());
		assertTrue("The message names the token: " + arguments, arguments.contains(UNKNOWN_TOKEN));
		assertTrue("The message lists the known tokens: " + arguments,
			arguments.contains(String.join(", ", sortedColorTokens())));
	}

	public void testLiteralColorIsAccepted() throws ConfigurationException {
		assertNoProblem(check(classifier("<color value='#04A38D'/>")));
	}

	public void testAbsentTokenIsAccepted() throws ConfigurationException {
		assertNoProblem(check(classifier("<color/>")));
	}

	public void testEmptyTokenIsAccepted() throws ConfigurationException {
		assertNoProblem(check(tokenClassifier("")));
	}

	public void testOptionsAreTheSortedColorTokens() {
		assertEquals("The color tokens of the vocabulary are offered, sorted by name.",
			sortedColorTokens(), new ColorTokenOptions().apply());
	}

	public void testOptionsOfferNoTokenOfAnotherKind() {
		List<String> options = new ColorTokenOptions().apply();
		for (String lengthToken : Vocabulary.LENGTH_TOKENS) {
			assertFalse("A token of another kind is no option: " + lengthToken + " in " + options,
				options.contains(lengthToken));
		}
	}

	public void testTokenOfAnotherKindIsReported() throws ConfigurationException {
		for (String lengthToken : Vocabulary.LENGTH_TOKENS) {
			assertEquals("A token of another kind is no color token: " + lengthToken,
				1, checkClassifier(lengthToken).size());
		}
	}

	/**
	 * Tests for an application declaring no token vocabulary.
	 */
	@SuppressWarnings("javadoc")
	public static class WithoutVocabulary extends BasicTestCase {

		public void testNoVocabularyOffersNoOption() {
			assertTrue("Without a started service, nothing is offered to choose from.",
				new ColorTokenOptions().apply().isEmpty());
		}

		public void testNoVocabularyChecksNothing() throws ConfigurationException {
			assertTrue("Without a started service, no token is known.",
				DesignTokenService.tokenNames(DesignTokenKind.COLOR).isEmpty());

			assertNoProblem(check(tokenClassifier(UNKNOWN_TOKEN)));
		}

	}

	static List<String> sortedColorTokens() {
		List<String> result = new ArrayList<>(Vocabulary.COLOR_TOKENS);
		Collections.sort(result);
		return result;
	}

	static void assertNoProblem(List<ConstraintFailure> failures) {
		assertTrue("No problem expected, but got: " + failures, failures.isEmpty());
	}

	static List<ConstraintFailure> checkClassifier(String token) throws ConfigurationException {
		return check(tokenClassifier(token));
	}

	static ClassifierConfig tokenClassifier(String token) throws ConfigurationException {
		return classifier("<color token='" + token + "'/>");
	}

	static ClassifierConfig classifier(String colorAnnotation) throws ConfigurationException {
		return read(ClassifierConfig.class, "classifier",
			"<classifier name='open'>"
				+ "<annotations>" + colorAnnotation + "</annotations>"
				+ "</classifier>");
	}

	static List<ConstraintFailure> check(ConfigurationItem config) throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(config);
		return checker.getFailures();
	}

	@SuppressWarnings("unchecked")
	static <T extends ConfigurationItem> T read(Class<T> type, String tag, String xml)
			throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors =
			Collections.singletonMap(tag, TypedConfiguration.getConfigurationDescriptor(type));
		Protocol log = new AssertProtocol();
		ConfigurationItem result = new ConfigurationReader(new DefaultInstantiationContext(log), descriptors)
			.setSource(CharacterContents.newContent(xml)).read();
		log.checkErrors();
		return (T) result;
	}

	/**
	 * {@link DesignTokenService} declaring the token vocabulary of this test.
	 */
	public static class Vocabulary extends DesignTokenService {

		/** The {@link DesignTokenKind#COLOR} tokens this vocabulary declares. */
		static final List<String> COLOR_TOKENS = Arrays.asList("support-warning", KNOWN_TOKEN, "support-error");

		/** The {@link DesignTokenKind#LENGTH} tokens this vocabulary declares. */
		static final List<String> LENGTH_TOKENS = Arrays.asList("spacing-m", "radius-s");

		/**
		 * Creates a {@link Vocabulary} from the given configuration.
		 *
		 * @param context
		 *        {@link InstantiationContext} to instantiate sub configurations.
		 * @param config
		 *        Configuration for this {@link Vocabulary}.
		 */
		public Vocabulary(InstantiationContext context, ManagedClass.ServiceConfiguration<?> config) {
			super(context, config);
		}

		@Override
		public Collection<String> getTokenNames(DesignTokenKind kind) {
			switch (kind) {
				case COLOR:
					return COLOR_TOKENS;
				case LENGTH:
					return LENGTH_TOKENS;
				default:
					return Collections.emptySet();
			}
		}

	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestColorTokenConstraint.class.getName());
		suite.addTest(withVocabulary());
		suite.addTest(new TestSuite(WithoutVocabulary.class));
		return TLTestSetup.createTLTestSetup(suite);
	}

	/**
	 * The tests running with the {@link Vocabulary} of this test started.
	 */
	private static Test withVocabulary() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestColorTokenConstraint.class),
			DesignTokenService.Module.INSTANCE);
		String fileName = CustomPropertiesDecorator.createFileName(TestColorTokenConstraint.class);
		return TestUtils.doNotMerge(new CustomPropertiesSetup(test, fileName, true));
	}

}
