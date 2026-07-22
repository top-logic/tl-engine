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

import com.top_logic.base.operation.OperationMode;
import com.top_logic.base.operation.OperationModeService;
import com.top_logic.basic.core.workspace.Environment;
import com.top_logic.tool.execution.IDEOnly;

/**
 * Test for {@link OperationModeService}.
 *
 * @author <a href="mailto:jonathan.huesing@top-logic.com">Jonathan Hüsing</a>
 */
@SuppressWarnings("javadoc")
public class TestOperationModeService extends BasicTestCase {

	/**
	 * The module boots from the shipped configuration (with the {@code %OPERATION_MODE%} alias
	 * resolving to empty, hence falling through to the derivation), resolves the effective mode
	 * <em>once at startup</em> and returns that cached value from {@link OperationModeService#getMode()}.
	 */
	public void testResolvesModeAtStartup() {
		OperationModeService service = OperationModeService.getInstance();
		assertNotNull("Service module must be started.", service);
		OperationMode mode = service.getMode();
		assertNotNull("An effective mode must be resolved at startup, not lazily on first access.", mode);
		// The value is precomputed, so repeated reads are stable and return the same instance.
		assertSame("The effective mode is computed once and cached.", mode, service.getMode());
	}

	/**
	 * IDE-only commands (layout export, model extraction) must be reachable on a developer
	 * workstation <em>and</em> under an automated test container, and hidden only in a real
	 * production install. Guards the regression where these commands became hidden during scripted
	 * tests because the run was classified as deployed.
	 */
	public void testIDEOnlyVisibleOutsideProduction() {
		assertTrue("IDE tooling must be available on a developer workstation.",
			IDEOnly.isVisibleIn(OperationMode.DEVELOPMENT));
		assertTrue("IDE tooling must remain available under an automated test container "
			+ "so scripted tests can exercise it.", IDEOnly.isVisibleIn(OperationMode.TEST));
		assertFalse("IDE tooling must be hidden in a real production install.",
			IDEOnly.isVisibleIn(OperationMode.PRODUCTION));
	}

	public void testExternalNames() {
		assertEquals("development", OperationMode.DEVELOPMENT.getExternalName());
		assertEquals("test", OperationMode.TEST.getExternalName());
		assertEquals("production", OperationMode.PRODUCTION.getExternalName());
	}

	/**
	 * {@link Environment#isDeployed()} and {@link OperationModeService} must key off the same
	 * variable value. {@link Environment} lives below the core module and cannot reference
	 * {@link OperationMode}, so it duplicates the development marker as a string constant; this test
	 * guards the two against drifting apart.
	 */
	public void testDevelopmentMarkerAgreesWithEnvironment() {
		assertEquals(OperationMode.DEVELOPMENT.getExternalName(), Environment.OPERATION_MODE_DEVELOPMENT);
		assertEquals("tl_operation_mode", Environment.OPERATION_MODE);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestOperationModeService}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(
			ServiceTestSetup.createSetup(TestOperationModeService.class, OperationModeService.Module.INSTANCE));
	}
}
