/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.conditional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for {@link PreconditionCommandHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestPreconditionCommandHandler extends TestCase {

	private C2 _command;

	static class C1 extends PreconditionCommandHandler {

		public C1(InstantiationContext context, PreconditionCommandHandler.Config config) {
			super(context, config);
		}

		@Override
		protected CommandStep prepare(LayoutComponent component, final Object model,
				final Map<String, Object> arguments) {
			if (arguments.get("firstPrecondition") != null) {
				return new Success() {
					@Override
					protected HandlerResult doPrepare(DisplayContext context) {
						addTestStep(model, "firstPrepare");
						return super.doPrepare(context);
					}

					@Override
					protected void doExecute(DisplayContext context) {
						if (arguments.containsKey("crash1")) {
							addTestStep(model, "execute1Failed");
							throw new TopLogicException(ResKey.forTest("execute1Failed"));
						}
						addTestStep(model, "firstExecute");
					}

					@Override
					protected HandlerResult doCommit(DisplayContext context) {
						if (arguments.containsKey("fail1")) {
							addTestStep(model, "commit1Failed");
							return HandlerResult.error(ResKey.forTest("commit1Failed"));
						}

						addTestStep(model, "firstCommit");
						return super.doCommit(context);
					}

					@Override
					protected void doFinally(DisplayContext context) {
						addTestStep(model, "firstFinally");
						super.doFinally(context);
					}
				};
			} else {
				return new Failure(ResKey.forTest("firstStepFailed"));
			}
		}

		@SuppressWarnings("unchecked")
		protected void addTestStep(final Object model, String value) {
			Map<String, Object> map = (Map<String, Object>) model;
			Object before = map.get("result");
			map.put("result", (before == null ? "" : before + ",") + value);
		}
	}

	public static class C2 extends C1 {

		public C2(InstantiationContext context, C1.Config config) {
			super(context, config);
		}

		@Override
		protected CommandStep prepare(LayoutComponent component, final Object model, Map<String, Object> arguments) {
			CommandStep superPrepare = super.prepare(component, model, arguments);
			if (superPrepare.getExecutability().isExecutable()) {
				if (arguments.get("secondPrecondition") != null) {
					return superPrepare.and(new Success() {
						@Override
						protected HandlerResult doPrepare(DisplayContext context) {
							addTestStep(model, "secondPrepare");
							return super.doPrepare(context);
						}

						@Override
						protected void doExecute(DisplayContext context) {
							addTestStep(model, "secondExecute");
						}

						@Override
						protected HandlerResult doCommit(DisplayContext context) {
							addTestStep(model, "secondCommit");
							return super.doCommit(context);
						}

						@Override
						protected void doFinally(DisplayContext context) {
							addTestStep(model, "secondFinally");
							super.doFinally(context);
						}
					});
				} else {
					return new Failure(ResKey.forTest("secondStepFailed"));
				}
			} else {
				return superPrepare;
			}
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_command = c2();
	}

	public void testExecutability() {
		assertFalse(_command.isExecutable(null, null, Collections.singletonMap("firstPrecondition", new Object()))
			.isExecutable());
		assertFalse(_command.isExecutable(null, null, Collections.singletonMap("secondPrecondition", new Object()))
			.isExecutable());
	}

	public void testExecution() {
		Map<String, Object> arguments = arguments();

		assertTrue(_command.isExecutable(null, null, arguments).isExecutable());

		HashMap<String, Object> result = new HashMap<>();
		assertTrue(_command.handleCommand(null, null, result, arguments).isSuccess());
		assertEquals(
			"firstPrepare,secondPrepare,firstExecute,secondExecute,secondCommit,firstCommit,secondFinally,firstFinally",
			result.get("result"));
	}

	public void testDynamicFailure() {
		Map<String, Object> arguments = arguments();
		arguments.put("fail1", new Object());

		assertTrue(_command.isExecutable(null, null, arguments).isExecutable());

		HashMap<String, Object> result = new HashMap<>();
		assertFalse(_command.handleCommand(null, null, result, arguments).isSuccess());
		assertEquals(
			"firstPrepare,secondPrepare,firstExecute,secondExecute,secondCommit,commit1Failed,secondFinally,firstFinally",
			result.get("result"));
	}

	public void testCrash() {
		Map<String, Object> arguments = arguments();
		arguments.put("crash1", new Object());

		assertTrue(_command.isExecutable(null, null, arguments).isExecutable());

		HashMap<String, Object> result = new HashMap<>();
		try {
			assertFalse(_command.handleCommand(null, null, result, arguments).isSuccess());
		} catch (TopLogicException ex) {
			assertEquals("execute1Failed", ex.getErrorKey().getKey());
		}
		assertEquals(
			"firstPrepare,secondPrepare,execute1Failed,secondFinally,firstFinally",
			result.get("result"));
	}

	private Map<String, Object> arguments() {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("firstPrecondition", new Object());
		arguments.put("secondPrecondition", new Object());
		return arguments;
	}

	private C2 c2() {
		C2.Config config = TypedConfiguration.newConfigItem(C2.Config.class);
		config.setImplementationClass(C2.class);
		config.update(config.descriptor().getProperty(CommandHandler.Config.ID_PROPERTY), "c2");
		C2 command = (C2) TypedConfigUtil.createInstance(config);
		return command;
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestPreconditionCommandHandler.class);
	}

}
