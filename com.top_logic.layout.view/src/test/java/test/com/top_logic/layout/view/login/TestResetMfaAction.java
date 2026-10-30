/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.login;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.login.I18NConstants;
import com.top_logic.layout.view.login.ResetMfaAction;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests {@link ResetMfaAction}, the administrative action removing the stored second-factor secret
 * of an account while keeping what the account is required to use.
 *
 * <p>
 * The account is a {@link Person} without a knowledge base behind it: its knowledge base records the
 * transactions begun and committed, and the account records the attribute updates, so that what the
 * action writes, and in which transaction, is what the test inspects.
 * </p>
 */
public class TestResetMfaAction extends TestCase {

	/** The steps the action took, in order. */
	private final List<String> _log = new ArrayList<>();

	/** The number of messages shown to the user. */
	private int _shown;

	/**
	 * Tests that an input that is no account is refused with the reason.
	 */
	public void testRefusesNonAccount() {
		try {
			newAction().execute(context(), "no account");
			fail("The action did not refuse.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.MFA_NO_ACCOUNT, ex.getErrorKey());
		}
		assertEquals("Nothing is shown for a refused input.", 0, _shown);
	}

	/**
	 * Tests that the secret is cleared in a committed transaction, the requirement left untouched,
	 * and the user told.
	 */
	public void testClearsSecretKeepsRequirement() {
		Account account = new Account();

		Object result = newAction().execute(context(), account);

		assertSame("The action hands its input on.", account, result);
		assertEquals("The secret is cleared, and nothing else, inside a committed transaction.",
			Arrays.asList("begin", "update " + Person.MFA_SECRET_ATTR + "=null", "commit"), _log);
		assertEquals("The user is told that the second factor was removed.", 1, _shown);
	}

	/** The production action, with nothing replaced. */
	private static ResetMfaAction newAction() {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestResetMfaAction.class);
		return new ResetMfaAction(context, TypedConfiguration.newConfigItem(ResetMfaAction.Config.class));
	}

	/** A context counting the messages it shows. */
	private ReactContext context() {
		return new DefaultViewContext(new DefaultReactContext("", "test", null, new ReactWindowRegistry("test")))
			.withErrorSink(new ErrorSink() {
				@Override
				public void showError(HTMLFragment content) {
					_shown++;
				}

				@Override
				public void showWarning(HTMLFragment content) {
					_shown++;
				}

				@Override
				public void showInfo(HTMLFragment content) {
					_shown++;
				}
			});
	}

	/**
	 * An account logging the updates made to it into {@link TestResetMfaAction#_log}, and refusing
	 * a change of its requirement.
	 */
	private class Account extends Person {

		Account() {
			super(null);
		}

		@Override
		public String getName() {
			return "test";
		}

		@Override
		public void tUpdateByName(String partName, Object value) {
			_log.add("update " + partName + "=" + value);
		}

		@Override
		public void setMFARequirement(MfaRequirement value) {
			throw new AssertionError("The requirement is kept.");
		}

		@Override
		public KnowledgeBase tKnowledgeBase() {
			return (KnowledgeBase) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { KnowledgeBase.class }, (proxy, method, args) -> {
					if (!method.getName().equals("beginTransaction")) {
						throw new UnsupportedOperationException(method.getName());
					}
					_log.add("begin");
					return transaction();
				});
		}

		private Transaction transaction() {
			return (Transaction) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { Transaction.class }, (proxy, method, args) -> {
					switch (method.getName()) {
						case "commit":
							_log.add("commit");
							return null;
						case "close":
						case "rollback":
							// A no-op after the commit.
							return null;
						default:
							throw new UnsupportedOperationException(method.getName());
					}
				});
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module and the resources the shown message is
	 * resolved from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestResetMfaAction.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
