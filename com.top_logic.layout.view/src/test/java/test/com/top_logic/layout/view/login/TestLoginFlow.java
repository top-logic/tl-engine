/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.login;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.wrap.person.MfaRequirement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.login.ContinueLoginAction;
import com.top_logic.layout.view.login.DialogStep;
import com.top_logic.layout.view.login.I18NConstants;
import com.top_logic.layout.view.login.LoginFlow;
import com.top_logic.layout.view.login.LoginStep;
import com.top_logic.layout.view.login.SecondFactor;
import com.top_logic.layout.view.login.SecondFactorLoginStep;
import com.top_logic.layout.view.login.SecondFactorStep;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests {@link LoginFlow}, the chain of {@link LoginStep}s between the authentication of an account
 * and the start of its session.
 *
 * <p>
 * The steps here record what is asked of them instead of opening dialogs, and the flow records its
 * completion instead of logging the account in.
 * </p>
 */
public class TestLoginFlow extends TestCase {

	private final List<String> _events = new ArrayList<>();

	private final ReactContext _context =
		new DefaultReactContext("", "test", null, new ReactWindowRegistry("test"));

	private final Person _account = new Account("account");

	@Override
	protected void runTest() throws Throwable {
		// A login is bound to the sub-session of its window.
		ThreadContextManager.inContext(ThreadContextManager.getManager().newSubSessionContext(), () -> {
			super.runTest();
			return null;
		});
	}

	/**
	 * Tests that the steps run in their order, that each starts only after the one before has
	 * finished, and that the login completes after the last one only.
	 */
	public void testStepsRunInOrder() {
		Recording first = new Recording("first", true);
		Recording second = new Recording("second", true);
		flow(first, second).begin(_context);

		assertEquals(List.of("start first"), _events);
		assertSame(first, LoginFlow.pending().getCurrentStep());

		LoginFlow.finish(_context, Recording.class, _account);
		assertEquals(List.of("start first", "start second"), _events);

		LoginFlow.finish(_context, Recording.class, _account);
		assertEquals(List.of("start first", "start second", "complete"), _events);
		assertNull("A completed login leaves nothing to finish.", LoginFlow.pending());
	}

	/**
	 * Tests that a step not applying to the account is skipped.
	 */
	public void testNonApplyingStepIsSkipped() {
		flow(new Recording("skipped", false), new Recording("due", true)).begin(_context);

		assertEquals(List.of("start due"), _events);
	}

	/**
	 * Tests that a login without an applying step completes immediately.
	 */
	public void testNoApplyingStepCompletes() {
		flow(new Recording("skipped", false)).begin(_context);

		assertEquals(List.of("complete"), _events);
		assertNull(LoginFlow.pending());
	}

	/**
	 * Tests that a step is finished only by an action of its own kind: an action finishing another
	 * kind of step is refused, cancels the login, and nothing completes.
	 */
	public void testStepOfOtherKindCannotBeFinished() {
		flow(new Proving("proof"), new Recording("after", true)).begin(_context);

		assertRefused(() -> LoginFlow.finish(_context, Recording.class, _account));
		assertEquals(List.of("start proof"), _events);
		assertNull("The refused login is cancelled.", LoginFlow.pending());

		assertRefused(() -> LoginFlow.finish(_context, Proving.class, _account));
		assertEquals("Nothing is left to finish once the login is cancelled.", List.of("start proof"), _events);
	}

	/**
	 * Tests that the general <code>&lt;continue-login/&gt;</code> cannot finish a step that asks the
	 * account for a proof, the second factor among them.
	 */
	public void testContinueCannotSkipProvingStep() {
		flow(secondFactor(), new Recording("after", true)).begin(_context);
		assertEquals(List.of("verify " + METHOD), _events);

		assertRefused(() -> newContinue().execute(_context, null));

		assertEquals("Neither the following step nor the login is reached.", List.of("verify " + METHOD), _events);
		assertNull(LoginFlow.pending());
	}

	/**
	 * Tests that <code>&lt;continue-login/&gt;</code> finishes a {@link DialogStep} and the login
	 * proceeds.
	 */
	public void testContinueFinishesDialogStep() {
		flow(dialogStep(), new Recording("after", true)).begin(_context);
		assertEquals(List.of(), _events);

		newContinue().execute(_context, null);

		assertEquals(List.of("start after"), _events);
	}

	/**
	 * Tests that a step of a login cannot be finished for another account.
	 */
	public void testOtherAccountCannotFinish() {
		flow(new Recording("first", true)).begin(_context);

		assertRefused(() -> LoginFlow.finish(_context, Recording.class, new Account("other")));
		assertEquals(List.of("start first"), _events);
		assertNull(LoginFlow.pending());
	}

	/**
	 * Tests that nothing is finished without a login in progress.
	 */
	public void testNothingToFinishWithoutLogin() {
		assertRefused(() -> newContinue().execute(_context, null));
		assertEquals(List.of(), _events);
	}

	/**
	 * Tests that a new login in the window replaces the one in progress: the steps of the replaced
	 * login can no longer be finished.
	 */
	public void testNewLoginReplacesPending() {
		flow(new Proving("old")).begin(_context);
		flow(new Recording("new", true)).begin(_context);

		assertRefused(() -> LoginFlow.finish(_context, Proving.class, _account));
		assertEquals(List.of("start old", "start new"), _events);
	}

	/**
	 * Tests that the second-factor step runs for an account that has set up a second factor.
	 */
	public void testSecondFactorStepApplies() {
		flow(secondFactor()).begin(_context);

		assertEquals(List.of("verify " + METHOD), _events);
		LoginFlow.finish(_context, SecondFactorLoginStep.class, _account);
		assertEquals(List.of("verify " + METHOD, "complete"), _events);
	}

	/**
	 * Tests the passwordless passkey login: the flow begins with the second factor proven, so the
	 * second-factor step is skipped and the further steps run before the login completes.
	 */
	public void testProvenSecondFactorRunsRemainingSteps() {
		LoginFlow flow = flow(secondFactor(), new Recording("message", true));
		flow.set(SecondFactorLoginStep.PROVEN, Boolean.TRUE);
		flow.begin(_context);

		assertEquals(List.of("start message"), _events);
		LoginFlow.finish(_context, Recording.class, _account);
		assertEquals(List.of("start message", "complete"), _events);
	}

	private static void assertRefused(Runnable action) {
		try {
			action.run();
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.LOGIN_FAILED, ex.getErrorKey());
			return;
		}
		fail("Not refused.");
	}

	private LoginFlow flow(LoginStep... steps) {
		return new LoginFlow(_account, List.of(steps)) {
			@Override
			protected void complete(ReactContext context) {
				_events.add("complete");
			}
		};
	}

	private static ContinueLoginAction newContinue() {
		return new ContinueLoginAction(new DefaultInstantiationContext(TestLoginFlow.class),
			TypedConfiguration.newConfigItem(ContinueLoginAction.Config.class));
	}

	private static final String METHOD = "method";

	/**
	 * A {@link SecondFactorLoginStep} offering one {@link SecondFactor} that every account has set
	 * up, recording its verification.
	 */
	private SecondFactorLoginStep secondFactor() {
		SecondFactorLoginStep.Config<?> config = TypedConfiguration.newConfigItem(SecondFactorLoginStep.Config.class);
		config.setName("second-factor");
		SecondFactor method = new Method();
		return new SecondFactorLoginStep(new DefaultInstantiationContext(TestLoginFlow.class), config) {
			@Override
			protected SecondFactorStep decide(LoginFlow flow) {
				return SecondFactorStep.decide(flow.getAccount(), List.of(method));
			}
		};
	}

	/**
	 * A {@link DialogStep} without condition; without a dialog manager, its dialog opens nothing.
	 */
	private static DialogStep dialogStep() {
		DialogStep.Config<?> config = TypedConfiguration.newConfigItem(DialogStep.Config.class);
		config.setName("dialog");
		config.update(config.descriptor().getProperty(DialogStep.Config.VIEW), "dialog.view.xml");
		return new DialogStep(new DefaultInstantiationContext(TestLoginFlow.class), config);
	}

	/**
	 * A step that applies as it was told to and records being started.
	 */
	private class Recording implements LoginStep {

		private final String _name;

		private final boolean _applies;

		Recording(String name, boolean applies) {
			_name = name;
			_applies = applies;
		}

		@Override
		public boolean appliesTo(LoginFlow flow) {
			return _applies;
		}

		@Override
		public void start(ReactContext context, LoginFlow flow) {
			_events.add("start " + _name);
		}
	}

	/**
	 * A step of a kind of its own, standing for one that has the account prove something.
	 */
	private class Proving implements LoginStep {

		private final String _name;

		Proving(String name) {
			_name = name;
		}

		@Override
		public boolean appliesTo(LoginFlow flow) {
			return true;
		}

		@Override
		public void start(ReactContext context, LoginFlow flow) {
			_events.add("start " + _name);
		}
	}

	/**
	 * A second-factor method every account has set up, recording its verification.
	 */
	private class Method implements SecondFactor {

		@Override
		public com.top_logic.basic.util.ResKey getLabel() {
			return com.top_logic.basic.util.ResKey.text(METHOD);
		}

		@Override
		public ThemeImage getIcon() {
			return null;
		}

		@Override
		public boolean isEnrolled(Person account) {
			return true;
		}

		@Override
		public boolean canEnroll(Person account) {
			return true;
		}

		@Override
		public void verify(ReactContext context, Person account) {
			_events.add("verify " + METHOD);
		}

		@Override
		public void enroll(ReactContext context, Person account) {
			_events.add("enroll " + METHOD);
		}
	}

	/**
	 * An account that answers with its name and needs no knowledge base.
	 */
	private static class Account extends Person {

		private final String _name;

		Account(String name) {
			super(null);
			_name = name;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public MfaRequirement getMFARequirement() {
			return MfaRequirement.OPTIONAL;
		}

		@Override
		public boolean equals(Object other) {
			// Identity: the account has no knowledge item to compare.
			return other == this;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, the thread contexts and the resources of
	 * the reported messages.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestLoginFlow.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
