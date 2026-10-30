/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.passkey.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.security.passkey.SoftwareAuthenticator;
import test.com.top_logic.security.passkey.TestPasskeyService;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ContextDependentRule;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.login.SecondFactor;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PendingRegistration;
import com.top_logic.security.passkey.model.Passkey;
import com.top_logic.security.passkey.view.DeletePasskeyAction;
import com.top_logic.security.passkey.view.I18NConstants;
import com.top_logic.security.passkey.view.PasskeySecondFactor;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link DeletePasskeyAction}: an account required to use a second factor cannot delete
 * the passkey that is its last one, every other passkey is deleted.
 */
public class TestDeletePasskeyAction extends BasicTestCase {

	private static final String LABEL = "Test passkey";

	private final List<Person> _accounts = new ArrayList<>();

	private ReactContext _context;

	private HttpServletRequest _request;

	private PasskeySecondFactor _passkeyMethod;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_request = TestPasskeyService.request("http", TestPasskeyService.HOST, TestPasskeyService.PORT);

		PasskeySecondFactor.Config<?> config = TypedConfiguration.newConfigItem(PasskeySecondFactor.Config.class);
		config.setName("passkey");
		config.update(config.descriptor().getProperty(PasskeySecondFactor.Config.LABEL), ResKey.text("Passkey"));
		_passkeyMethod =
			(PasskeySecondFactor) new DefaultInstantiationContext(TestDeletePasskeyAction.class).getInstance(config);
	}

	@Override
	protected void tearDown() throws Exception {
		for (Person account : _accounts) {
			if (account.isAlive()) {
				TestPerson.deletePersonAndUser(account);
			}
		}
		_accounts.clear();
		super.tearDown();
	}

	/** An account that may log in without a second factor deletes its last passkey. */
	public void testOptionalAccountDeletesLastPasskey() throws Exception {
		Person account = account("deleteOptional", MfaRequirement.OPTIONAL);
		Passkey passkey = register(account);

		assertNull(DeletePasskeyAction.refusal(passkey, List.of(_passkeyMethod)));

		delete(passkey);
		assertFalse(passkey.tValid());
		assertEquals(List.of(), PasskeyService.getInstance().getPasskeys(account));
	}

	/** The last passkey of an account required to use a second factor is not deleted. */
	public void testRequiredAccountKeepsLastPasskey() throws Exception {
		Person account = account("deleteRequired", MfaRequirement.REQUIRED);
		Passkey passkey = register(account);

		ResKey expected = I18NConstants.ERROR_LAST_SECOND_FACTOR__ACCOUNT.fill(account.getName());
		assertEquals(expected, DeletePasskeyAction.refusal(passkey, List.of(_passkeyMethod)));
		assertEquals("A method for passkeys does not count as another second factor.",
			expected, DeletePasskeyAction.refusal(passkey, List.of(_passkeyMethod, factor(false))));

		try {
			delete(passkey);
			fail("The last second factor of a required account must not be deleted.");
		} catch (TopLogicException ex) {
			assertEquals(expected, ex.getErrorKey());
		}
		assertTrue(passkey.tValid());
		assertEquals(List.of(passkey), PasskeyService.getInstance().getPasskeys(account));
	}

	/** A required account keeping another passkey deletes one of them, but not the last one. */
	public void testRequiredAccountWithFurtherPasskey() throws Exception {
		Person account = account("deleteSecond", MfaRequirement.REQUIRED);
		Passkey first = register(account);
		Passkey second = register(account);

		assertNull(DeletePasskeyAction.refusal(first, List.of(_passkeyMethod)));
		assertNull(DeletePasskeyAction.refusal(second, List.of(_passkeyMethod)));

		delete(first);
		assertFalse(first.tValid());
		assertNotNull(DeletePasskeyAction.refusal(second, List.of(_passkeyMethod)));
	}

	/** A required account with another method of the login set up deletes its last passkey. */
	public void testRequiredAccountWithOtherFactor() throws Exception {
		Person account = account("deleteOther", MfaRequirement.REQUIRED);
		Passkey passkey = register(account);

		assertNull(DeletePasskeyAction.refusal(passkey, List.of(_passkeyMethod, factor(true))));
		assertNotNull("A method the account has not set up does not count.",
			DeletePasskeyAction.refusal(passkey, List.of(_passkeyMethod, factor(false))));
	}

	/**
	 * The disabled deletion of the last passkey is decided anew when the account gets a further
	 * passkey, although the selected passkey stays the same.
	 */
	public void testRuleFollowsFurtherPasskey() throws Exception {
		Person account = account("deleteFollows", MfaRequirement.REQUIRED);
		Passkey passkey = register(account);

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb.getMORepository());
		log.checkErrors();
		GlobalModelEventForwarder scope = new GlobalModelEventForwarder(kb, kb.getUpdateChain(), relevance);
		ViewContext viewContext = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")) {
				@Override
				public ModelScope getModelScope() {
					return scope;
				}
			});

		ViewExecutabilityRule rule = newAction().getIntrinsicRule();
		((ContextDependentRule) rule).bind(viewContext);
		int[] revalidations = { 0 };
		Runnable stop = ((ObservableRule) rule).observe(() -> revalidations[0]++);
		try {
			assertNotNull(DeletePasskeyAction.refusal(passkey, List.of(_passkeyMethod)));

			register(account);
			scope.synthesizeModelEvents();

			assertTrue("A further passkey must let the rule decide anew.", revalidations[0] > 0);
			assertNull(DeletePasskeyAction.refusal(passkey, List.of(_passkeyMethod)));
		} finally {
			stop.run();
		}
	}

	private void delete(Passkey passkey) {
		DeletePasskeyAction action = newAction();
		// The access check of the deletion is not under test: the passkey is deleted without it.
		ThreadContext.withoutSecurityChecks(() -> action.execute(_context, passkey));
	}

	private static DeletePasskeyAction newAction() {
		DeletePasskeyAction.Config config = TypedConfiguration.newConfigItem(DeletePasskeyAction.Config.class);
		return (DeletePasskeyAction) new DefaultInstantiationContext(TestDeletePasskeyAction.class).getInstance(config);
	}

	private Passkey register(Person account) throws Exception {
		SoftwareAuthenticator authenticator = new SoftwareAuthenticator(TestPasskeyService.ORIGIN, 0);
		PasskeyService service = PasskeyService.getInstance();
		PendingRegistration registration = service.startRegistration(account, _request);
		return service.finishRegistration(registration, authenticator.create(registration.toOptionsJson()), LABEL);
	}

	private Person account(String name, MfaRequirement requirement) {
		Person account = TestPerson.createPerson(name);
		_accounts.add(account);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Set MFA requirement"))) {
			account.setMFARequirement(requirement);
			tx.commit();
		}
		return account;
	}

	/**
	 * A second factor method that every account has or has not set up.
	 */
	private static SecondFactor factor(boolean enrolled) {
		return new SecondFactor() {
			@Override
			public ResKey getLabel() {
				return ResKey.text("Other factor");
			}

			@Override
			public ThemeImage getIcon() {
				return null;
			}

			@Override
			public boolean isEnrolled(Person account) {
				return enrolled;
			}

			@Override
			public boolean canEnroll(Person account) {
				return true;
			}

			@Override
			public void verify(ReactContext context, Person account) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void enroll(ReactContext context, Person account) {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * The suite, with a knowledge base holding the passkey model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestDeletePasskeyAction.class),
				ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				PasskeyService.Module.INSTANCE));
	}

}
