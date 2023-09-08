/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.DynamicDelegatingCommandModel;
import com.top_logic.layout.form.model.AbstractExecutabilityModel;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Test case for {@link DynamicDelegatingCommandModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDynamicDelegatingCommandModel extends TestCase {

	public void testExecutability() {
		ThreadContextManager.inSystemInteraction(TestDynamicDelegatingCommandModel.class, new InContext() {
			@Override
			public void inContext() {
				doTestExecutability();
			}
		});
	}

	void doTestExecutability() {
		Command command = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				throw new UnsupportedOperationException();
			}
		};

		class TestingAbstractExecutabilityModel extends AbstractExecutabilityModel {
			private ExecutableState _state = ExecutableState.NOT_EXEC_HIDDEN;

			@Override
			protected ExecutableState calculateExecutability() {
				return _state;
			}

			public void setState(ExecutableState state) {
				_state = state;
			}
		}

		TestingAbstractExecutabilityModel executability = new TestingAbstractExecutabilityModel();
		CommandModel model = CommandModelFactory.commandModel(command, executability);

		assertFalse(model.isVisible());
		assertFalse(model.isExecutable());

		executability.setState(ExecutableState.EXECUTABLE);
		executability.updateExecutabilityState();

		assertTrue(model.isVisible());
		assertTrue(model.isExecutable());

		VisibilityListener listener = new VisibilityListener() {
			@Override
			public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
				return Bubble.BUBBLE;
			}
		};
		model.addListener(VisibilityModel.VISIBLE_PROPERTY, listener);

		executability.setState(ExecutableState.NO_EXEC_INVALID);
		executability.updateExecutabilityState();

		assertTrue(model.isVisible());
		assertFalse(model.isExecutable());

		model.removeListener(VisibilityModel.VISIBLE_PROPERTY, listener);

		assertTrue(model.isVisible());
		assertFalse(model.isExecutable());
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(
			ServiceTestSetup.createSetup(
				TestDynamicDelegatingCommandModel.class, ThreadContextManager.Module.INSTANCE));
	}

}
