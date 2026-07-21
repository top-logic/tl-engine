/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.operation;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.operation.ApplicationModeService;
import com.top_logic.base.operation.ApplicationModeService.Config;
import com.top_logic.base.operation.OperationMode;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.core.workspace.Environment;

/**
 * Test for {@link ApplicationModeService}.
 *
 * @author <a href="mailto:jonathan.huesing@top-logic.com">Jonathan Hüsing</a>
 */
@SuppressWarnings("javadoc")
public class TestApplicationModeService extends BasicTestCase {

	public void testExplicitConfigWins() {
		assertSame(OperationMode.TEST, newService(OperationMode.TEST).getMode());
		assertSame(OperationMode.PRODUCTION, newService(OperationMode.PRODUCTION).getMode());
		assertSame(OperationMode.DEVELOPMENT, newService(OperationMode.DEVELOPMENT).getMode());
	}

	public void testDerivationUnderTest() {
		// An automated test install is detected via the test container.
		assertSame(OperationMode.TEST, ApplicationModeService.deriveMode(true));
	}

	public void testDerivationDefaultsToProduction() {
		// Production is the zero-configuration default. DEVELOPMENT is never derived; it is only
		// reached via an explicit configured mode (see testExplicitConfigWins).
		assertSame(OperationMode.PRODUCTION, ApplicationModeService.deriveMode(false));
	}

	/**
	 * {@link Environment#isDeployed()} and {@link ApplicationModeService} must key off the same
	 * variable value. {@link Environment} lives below the core module and cannot reference
	 * {@link OperationMode}, so it duplicates the development marker as a string constant; this test
	 * guards the two against drifting apart.
	 */
	public void testDevelopmentMarkerAgreesWithEnvironment() {
		assertEquals(OperationMode.DEVELOPMENT.getExternalName(), Environment.OPERATION_MODE_DEVELOPMENT);
		assertEquals("tl_operation_mode", Environment.OPERATION_MODE);
	}

	public void testMaintenanceInactiveWhenManagerAbsent() {
		// The MaintenanceWindowManager module is not started in this minimal setup, so the service
		// must null-safely report maintenance as inactive.
		assertFalse(newService(OperationMode.PRODUCTION).isMaintenanceActive());
	}

	public void testExternalNames() {
		assertEquals("development", OperationMode.DEVELOPMENT.getExternalName());
		assertEquals("test", OperationMode.TEST.getExternalName());
		assertEquals("production", OperationMode.PRODUCTION.getExternalName());
	}

	/**
	 * The module boots from the shipped configuration (with the {@code %OPERATION_MODE%} alias
	 * resolving to empty, hence falling through to the derivation) and yields a usable instance.
	 */
	public void testModuleBootsAndDerives() {
		ApplicationModeService service = ApplicationModeService.getInstance();
		assertNotNull("Service module must be started.", service);
		assertNotNull("An effective mode must always be derived.", service.getMode());
		// The manager module is not started in this setup, so maintenance must be reported inactive.
		assertFalse(service.isMaintenanceActive());
	}

	private ApplicationModeService newService(OperationMode mode) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setImplementationClass(ApplicationModeService.class);
		config.setMode(mode);
		return TypedConfigUtil.createInstance(config);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestApplicationModeService}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(
			ServiceTestSetup.createSetup(TestApplicationModeService.class, ApplicationModeService.Module.INSTANCE));
	}
}
