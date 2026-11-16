/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import static test.com.top_logic.layout.view.command.OperationModes.*;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.operation.OperationMode;
import com.top_logic.base.operation.OperationModeService;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.command.DevelopmentOnly;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests for {@link DevelopmentOnly}: a command reserved for development is hidden in a production
 * install and executable in development and test mode.
 */
public class TestDevelopmentOnly extends TestCase {

	/** In production, the command is hidden. */
	public void testHiddenInProduction() {
		inMode(OperationMode.PRODUCTION, () -> {
			ExecutableState state = createRule().isExecutable(null);

			assertTrue(state.isHidden());
			assertFalse(state.isExecutable());
		});
	}

	/** On a developer workstation, the command is executable. */
	public void testExecutableInDevelopment() {
		inMode(OperationMode.DEVELOPMENT, () -> {
			assertTrue(createRule().isExecutable(null).isExecutable());
		});
	}

	/** In a test install, the command is executable, so that a scripted test can exercise it. */
	public void testExecutableInTest() {
		inMode(OperationMode.TEST, () -> {
			assertTrue(createRule().isExecutable(null).isExecutable());
		});
	}

	/** The shared instance decides like a configured rule. */
	public void testSharedInstance() {
		inMode(OperationMode.PRODUCTION, () -> {
			assertTrue(DevelopmentOnly.INSTANCE.isExecutable(null).isHidden());
		});
		inMode(OperationMode.DEVELOPMENT, () -> {
			assertTrue(DevelopmentOnly.INSTANCE.isExecutable(null).isExecutable());
		});
	}

	private static ViewExecutabilityRule createRule() {
		DevelopmentOnly.Config config = TypedConfiguration.newConfigItem(DevelopmentOnly.Config.class);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module the rule's configuration is created with
	 * and the {@link OperationModeService} the rule asks.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDevelopmentOnly.class, TypeIndex.Module.INSTANCE,
				OperationModeService.Module.INSTANCE));
	}
}
