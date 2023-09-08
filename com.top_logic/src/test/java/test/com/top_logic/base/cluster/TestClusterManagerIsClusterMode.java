/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.cluster;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.Computation;

/**
 * Tests the methods to determine whether the {@link ClusterManager} is in cluster mode.
 * 
 * This test expects the manager is in cluster mode.
 * 
 * @see TestClusterManagerIsNotClusterMode
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestClusterManagerIsClusterMode extends BasicTestCase {

	/**
	 * Tests the static method {@link ClusterManager#isClusterModeConfigured()}.
	 */
	public void testStatic() {
		assertTrue(ClusterManager.isClusterModeConfigured());
	}

	/**
	 * Tests the instance method {@link ClusterManager#isClusterMode()}.
	 */
	public void testInstance() {
		ModuleUtil.INSTANCE.inModuleContext(ClusterManager.Module.INSTANCE, new Computation<Void>() {

			@Override
			public Void run() {
				assertTrue(ClusterManager.getInstance().isClusterMode());
				return null;
			}
		});
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestClusterManagerIsClusterMode}.
	 */
	public static Test suite() {
		Test innerTest = new TestSuite(TestClusterManagerIsClusterMode.class);
		innerTest = ClusterManagerSetup.setupClusterTables(innerTest);
		innerTest = ServiceTestSetup.startConnectionPool(innerTest);
		String customFile = CustomPropertiesDecorator.createFileName(TestClusterManagerIsClusterMode.class);
		innerTest = new CustomPropertiesSetup(innerTest, customFile, true);
		return TLTestSetup.createTLTestSetup(innerTest);
	}

}
