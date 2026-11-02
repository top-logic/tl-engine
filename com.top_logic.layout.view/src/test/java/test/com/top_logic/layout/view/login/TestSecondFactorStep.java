/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.login;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.login.I18NConstants;
import com.top_logic.layout.view.login.SecondFactor;
import com.top_logic.layout.view.login.SecondFactorStep;
import com.top_logic.layout.view.login.SecondFactorStep.Kind;
import com.top_logic.layout.view.login.SecondFactorsConfig;
import com.top_logic.layout.view.login.TotpSecondFactor;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests {@link SecondFactorStep}, the decision of the React login what follows the password of an
 * account: completing the login, confirming the account with a second factor, or setting one up.
 */
public class TestSecondFactorStep extends TestCase {

	/** The configuration of the module, relative to the module directory the test runs in. */
	private static final String MODULE_CONFIG = "src/main/webapp/WEB-INF/conf/tl-layout-view.conf.config.xml";

	/** The tag of an entry of the {@link ApplicationConfig.Config#getConfigs() application configurations}. */
	private static final String CONFIG_TAG = "config";

	/**
	 * The TOTP secret of an account that has the method set up; never decrypted, only its presence
	 * counts.
	 */
	private static final Password SECRET = new Password("not-decrypted");

	/**
	 * Tests that an account with no method set up and no requirement logs in with the password
	 * alone.
	 */
	public void testOptionalWithoutFactorCompletes() {
		Method a = new Method("a", false, true);
		Method b = new Method("b", false, true);

		SecondFactorStep step = SecondFactorStep.decide(new Account(MfaRequirement.OPTIONAL), List.of(a, b));

		assertEquals(Kind.COMPLETE, step.getKind());
		assertEquals(List.of(), step.getMethods());
	}

	/**
	 * Tests that an account whose second factor is disabled and that has none set up logs in with
	 * the password alone, although methods could be set up.
	 */
	public void testDisabledWithoutFactorCompletes() {
		Method a = new Method("a", false, true);

		SecondFactorStep step = SecondFactorStep.decide(new Account(MfaRequirement.DISABLED), List.of(a));

		assertEquals(Kind.COMPLETE, step.getKind());
	}

	/**
	 * Tests that without any configured method a required account cannot log in.
	 */
	public void testRequiredWithoutMethodsIsRefused() {
		assertRefused(new Account(MfaRequirement.REQUIRED), List.of());
	}

	/**
	 * Tests that the only method an account has set up confirms it, and that carrying the step out
	 * opens that method's verification directly.
	 */
	public void testOneEnrolledVerifiesDirectly() {
		Method a = new Method("a", false, true);
		Method b = new Method("b", true, true);
		Account account = new Account(MfaRequirement.OPTIONAL);

		SecondFactorStep step = SecondFactorStep.decide(account, List.of(a, b));

		assertEquals(Kind.VERIFY, step.getKind());
		assertEquals(List.of(b), step.getMethods());

		step.perform(null, account);
		assertEquals(List.of(), a._calls);
		assertEquals(List.of(Method.VERIFY), b._calls);
	}

	/**
	 * Tests that a method set up is asked for even where the account's requirement disables the
	 * second factor: an account that has one keeps being asked for it.
	 */
	public void testEnrolledVerifiesWhateverTheRequirement() {
		Method a = new Method("a", true, false);

		for (MfaRequirement requirement : MfaRequirement.values()) {
			SecondFactorStep step = SecondFactorStep.decide(new Account(requirement), List.of(a));

			assertEquals(requirement.name(), Kind.VERIFY, step.getKind());
			assertEquals(requirement.name(), List.of(a), step.getMethods());
		}
	}

	/**
	 * Tests that several methods set up are all offered, in configuration order, and that methods
	 * not set up are not.
	 */
	public void testSeveralEnrolledAreOffered() {
		Method a = new Method("a", true, true);
		Method b = new Method("b", false, true);
		Method c = new Method("c", true, true);

		SecondFactorStep step = SecondFactorStep.decide(new Account(MfaRequirement.REQUIRED), List.of(a, b, c));

		assertEquals(Kind.VERIFY, step.getKind());
		assertEquals(List.of(a, c), step.getMethods());
	}

	/**
	 * Tests that a required account without a method set up sets up the only one it may, and that
	 * carrying the step out opens its set-up directly.
	 */
	public void testRequiredWithOneEnrollableEnrollsDirectly() {
		Method a = new Method("a", false, false);
		Method b = new Method("b", false, true);
		Account account = new Account(MfaRequirement.REQUIRED);

		SecondFactorStep step = SecondFactorStep.decide(account, List.of(a, b));

		assertEquals(Kind.ENROLL, step.getKind());
		assertEquals(List.of(b), step.getMethods());

		step.perform(null, account);
		assertEquals(List.of(), a._calls);
		assertEquals(List.of(Method.ENROLL), b._calls);
	}

	/**
	 * Tests that a required account without a method set up is offered all methods it may set up.
	 */
	public void testRequiredWithSeveralEnrollableAreOffered() {
		Method a = new Method("a", false, true);
		Method b = new Method("b", false, false);
		Method c = new Method("c", false, true);

		SecondFactorStep step = SecondFactorStep.decide(new Account(MfaRequirement.REQUIRED), List.of(a, b, c));

		assertEquals(Kind.ENROLL, step.getKind());
		assertEquals(List.of(a, c), step.getMethods());
	}

	/**
	 * Tests that a required account that has no method set up and may set up none is refused.
	 */
	public void testRequiredWithoutEnrollableIsRefused() {
		Method a = new Method("a", false, false);

		assertRefused(new Account(MfaRequirement.REQUIRED), List.of(a));
	}

	/**
	 * Tests that a method chosen from several opens its verification, respectively its set-up,
	 * depending on the step.
	 */
	public void testStartDispatchesByKind() {
		Method a = new Method("a", true, true);
		Method b = new Method("b", true, true);
		Account account = new Account(MfaRequirement.OPTIONAL);
		SecondFactorStep verify = SecondFactorStep.decide(account, List.of(a, b));
		verify.start(null, account, b);
		assertEquals(List.of(), a._calls);
		assertEquals(List.of(Method.VERIFY), b._calls);

		Method c = new Method("c", false, true);
		Method d = new Method("d", false, true);
		Account required = new Account(MfaRequirement.REQUIRED);
		SecondFactorStep enroll = SecondFactorStep.decide(required, List.of(c, d));
		enroll.start(null, required, c);
		assertEquals(List.of(Method.ENROLL), c._calls);
		assertEquals(List.of(), d._calls);
	}

	/**
	 * Tests that the module's configuration offers the TOTP method and that a TOTP-only application
	 * decides as before: a secret means the code dialog, a required account without one the
	 * enrollment, otherwise the password alone.
	 */
	public void testModuleConfiguresTotp() throws ConfigurationException, IOException {
		SecondFactorsConfig config = TypedConfiguration.parse(CONFIG_TAG,
			SecondFactorsConfig.class, CharacterContents.newContent(moduleSecondFactors()));
		assertEquals(1, config.getMethods().size());

		SecondFactor totp = new DefaultInstantiationContext(TestSecondFactorStep.class)
			.getInstance(config.getMethods().get(0));
		assertTrue(totp instanceof TotpSecondFactor);
		assertNotNull(totp.getLabel());
		assertNotNull(totp.getIcon());

		assertEquals(Kind.VERIFY,
			SecondFactorStep.decide(new Account(MfaRequirement.DISABLED, SECRET), List.of(totp)).getKind());
		assertEquals(Kind.ENROLL,
			SecondFactorStep.decide(new Account(MfaRequirement.REQUIRED, null), List.of(totp)).getKind());
		assertEquals(Kind.COMPLETE,
			SecondFactorStep.decide(new Account(MfaRequirement.OPTIONAL, null), List.of(totp)).getKind());
	}

	/**
	 * The {@link SecondFactorsConfig} entry of the module configuration, as a document of its own.
	 *
	 * <p>
	 * Only this entry is read: the services the configuration file also declares need the
	 * configuration of a whole application.
	 * </p>
	 */
	private static String moduleSecondFactors() throws IOException {
		String file = Files.readString(Path.of(MODULE_CONFIG), StandardCharsets.UTF_8);
		String start = "<" + CONFIG_TAG + " " + ConfigurationSchemaConstants.CONFIG_NS_PREFIX + ":interface=\""
			+ SecondFactorsConfig.class.getName() + "\"";
		int begin = file.indexOf(start);
		assertTrue("The module configures the second-factor methods.", begin >= 0);
		String endTag = "</" + CONFIG_TAG + ">";
		int end = file.indexOf(endTag, begin) + endTag.length();
		String entry = file.substring(begin, end);
		return "<" + CONFIG_TAG + " xmlns:" + ConfigurationSchemaConstants.CONFIG_NS_PREFIX + "=\""
			+ ConfigurationSchemaConstants.CONFIG_NS + "\"" + entry.substring(CONFIG_TAG.length() + 1);
	}

	private static void assertRefused(Account account, List<Method> methods) {
		try {
			SecondFactorStep.decide(account, methods);
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_NO_SECOND_FACTOR__USER.fill(account.getName()), ex.getErrorKey());
			return;
		}
		fail("A required account without a second factor that can be set up must be refused.");
	}

	/**
	 * A {@link SecondFactor} answering with fixed values and recording the steps opened.
	 */
	private static class Method implements SecondFactor {

		/** Recorded for a call of {@link #verify(ReactContext, Person)}. */
		static final String VERIFY = "verify";

		/** Recorded for a call of {@link #enroll(ReactContext, Person)}. */
		static final String ENROLL = "enroll";

		private final String _name;

		private final boolean _enrolled;

		private final boolean _canEnroll;

		final List<String> _calls = new ArrayList<>();

		Method(String name, boolean enrolled, boolean canEnroll) {
			_name = name;
			_enrolled = enrolled;
			_canEnroll = canEnroll;
		}

		@Override
		public ResKey getLabel() {
			return ResKey.text(_name);
		}

		@Override
		public ThemeImage getIcon() {
			return null;
		}

		@Override
		public boolean isEnrolled(Person account) {
			return _enrolled;
		}

		@Override
		public boolean canEnroll(Person account) {
			return _canEnroll;
		}

		@Override
		public void verify(ReactContext context, Person account) {
			_calls.add(VERIFY);
		}

		@Override
		public void enroll(ReactContext context, Person account) {
			_calls.add(ENROLL);
		}

		@Override
		public String toString() {
			return _name;
		}
	}

	/**
	 * An account answering with the requirement it was created with, outside of a knowledge base.
	 */
	private static class Account extends Person {

		private final MfaRequirement _requirement;

		private final Password _secret;

		Account(MfaRequirement requirement) {
			this(requirement, null);
		}

		Account(MfaRequirement requirement, Password secret) {
			super(null);
			_requirement = requirement;
			_secret = secret;
		}

		@Override
		public MfaRequirement getMFARequirement() {
			return _requirement;
		}

		@Override
		public Password getMFASecret() {
			return _secret;
		}

		@Override
		public String getName() {
			return "test";
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module and the resources the reported messages are
	 * resolved from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestSecondFactorStep.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
