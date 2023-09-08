/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import static test.com.top_logic.ComponentTestUtils.*;

import java.util.Map;

import test.com.top_logic.ComponentTestUtils;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.ObjectFlag;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelRegistry;
import com.top_logic.layout.form.FormMember;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.SimpleComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * The class {@link CommandHandlerCommandModelTest} is an abstract test for
 * {@link CommandModel} which base on {@link CommandHandler} and therefore must
 * react on the {@link ExecutableState} by
 * {@link CommandDispatcher#resolveExecutableState(CommandHandler, LayoutComponent, Map)}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CommandHandlerCommandModelTest<T extends CommandModel> extends AbstractCommandModelTest<T> {

	protected static final ResKey NOT_EXEC_REASON_KEY = ResKey.forTest("reasonKey");

	public static final class TestingCommand extends AbstractCommandHandler {

		public TestingCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			return null;
		}

		@Override
		public boolean needsConfirm() {
			return true;
		}

		public static AbstractCommandHandler newInstance(String commandId, ExecutabilityRule executabilityRule) {
			TestingCommand result = newInstance(TestingCommand.class, commandId);
			result.setRule(executabilityRule);
			return result;
		}
	}

	protected static enum State {
			visibleExecutable, visibleNotExecutable, notVisible
		}

	protected static boolean VISIBLE = true;
	protected static boolean HIDDEN = false;
	protected static boolean EXECUTABLE = true;
	protected static boolean NOT_EXECUTABLE = false;

	protected SimpleComponent component;
	protected ObjectFlag<State> flag;
	protected AbstractCommandHandler commandHandler;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		flag = new ObjectFlag<>(State.visibleExecutable);
	
		final ExecutabilityRule executabilityRule = new ExecutabilityRule() {
	
			@Override
			public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
				switch (flag.get()) {
					case visibleExecutable:
						return ExecutableState.EXECUTABLE;
					case visibleNotExecutable:
						return ExecutableState.createDisabledState(NOT_EXEC_REASON_KEY);
					case notVisible:
						return ExecutableState.NOT_EXEC_HIDDEN;
					default:
						throw new UnreachableAssertion("Unknown type : " + flag.get());
				}
			}
		};
		commandHandler = TestingCommand.newInstance("id", executabilityRule);
		component = createSimpleComponent();
	}

	private SimpleComponent createSimpleComponent() {
		MainLayout ml = newMainLayout();
		SimpleComponent simpleComponent = ComponentTestUtils.newSimpleComponent("header", "content");
		ml.addComponent(simpleComponent);
		return simpleComponent;
	}
	
	@Override
	protected void tearDown() throws Exception {
		commandHandler = null;
		component = null;
		flag = null;
		super.tearDown();
	}

	protected void updateExecutabilityRule(final State state) {
		flag.set(state);
		CommandModelRegistry.getRegistry().updateCommandModels();
	}

	/**
	 * checks the {@link CommandModel#isVisible() visibility} and
	 * {@link CommandModel#isExecutable() executability} of the
	 * {@link #getCommandModel() command model}.
	 * 
	 * @param visible
	 *        whether the command model should be visible.
	 * @param executable
	 *        whether the command model should be executable.
	 */
	protected void check(boolean visible, boolean executable) {
		if (visible) {
			assertTrue("command model must be visible", getCommandModel().isVisible());
		} else {
			assertFalse("command model must be hidden", getCommandModel().isVisible());
		}
		if (executable) {
			assertTrue("command model must be executable", getCommandModel().isExecutable());
		} else {
			assertFalse("command model must not be executable", getCommandModel().isExecutable());
		}
	}
	
	public void testCorrectI18N() {
		ResKey differentReason = Resources.encodeLiteralText("newReason");
		getCommandModel().setNotExecutable(differentReason);
		assertEquals(differentReason, getCommandModel().getNotExecutableReasonKey());
		
		getCommandModel().setExecutable();
		updateExecutabilityRule(State.visibleNotExecutable);
		assertEquals(NOT_EXEC_REASON_KEY, getCommandModel().getNotExecutableReasonKey());
	}

	public void testUpdateByExecutabilityRule() {
		listener.clear();
		updateExecutabilityRule(State.visibleNotExecutable);
		listener.assertCaptured(CommandModel.EXECUTABLE_PROPERTY, true, false);
		check(VISIBLE, NOT_EXECUTABLE);
		
		listener.clear();
		updateExecutabilityRule(State.notVisible);
		listener.assertCaptured(FormMember.VISIBLE_PROPERTY, true, false);
		check(HIDDEN, NOT_EXECUTABLE);
		
		listener.clear();
		updateExecutabilityRule(State.visibleExecutable);
		listener.assertCaptured(FormMember.VISIBLE_PROPERTY, false, true);
		listener.assertCaptured(CommandModel.EXECUTABLE_PROPERTY, false, true);
		check(VISIBLE, EXECUTABLE);
	}

	public void testStrongestExecutabilityWins() {
		getCommandModel().setNotExecutable(NOT_EXEC_REASON_KEY);
		updateExecutabilityRule(State.visibleExecutable);
		check(VISIBLE, NOT_EXECUTABLE);
		getCommandModel().setExecutable();
		updateExecutabilityRule(State.visibleNotExecutable);
		check(VISIBLE, NOT_EXECUTABLE);
		getCommandModel().setNotExecutable(NOT_EXEC_REASON_KEY);
		updateExecutabilityRule(State.visibleNotExecutable);
		check(VISIBLE, NOT_EXECUTABLE);
	}

	public void testStrongestVisibilityWins() {
		getCommandModel().setVisible(true);
		updateExecutabilityRule(State.visibleExecutable);
		check(VISIBLE, EXECUTABLE);
		getCommandModel().setVisible(false);
		updateExecutabilityRule(State.visibleExecutable);
		check(HIDDEN, NOT_EXECUTABLE);
		updateExecutabilityRule(State.notVisible);
		getCommandModel().setVisible(true);
		check(HIDDEN, NOT_EXECUTABLE);
	}
	
}

