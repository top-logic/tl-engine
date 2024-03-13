/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.cluster;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestFactoryProxy;
import test.com.top_logic.basic.db.schema.SelectiveSchemaTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.service.db2.DropSequenceSetup;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManager.ClusterManagerDBExecutor;

/**
 * Creates the {@link ClusterManager} tables and starts it.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ClusterManagerSetup {

	private static final String CLUSTER_SCHEMA_NAME = "cluster";

	/**
	 * Ensures that the {@link ClusterManager} can be used in inner test.
	 */
	public static Test setupClusterManager(Test innerTest) {
		innerTest = ServiceTestSetup.createSetup(innerTest, ClusterManager.Module.INSTANCE);
		innerTest = setupClusterTables(innerTest);
		return innerTest;
	}

	/**
	 * Ensures that {@link ClusterManager} tables exist.
	 */
	public static Test setupClusterTables(Test innerTest) {
		innerTest = new SelectiveSchemaTestSetup(innerTest, CLUSTER_SCHEMA_NAME);
		innerTest =
			new DropSequenceSetup(innerTest, ClusterManagerDBExecutor.NODE_ID, ClusterManagerDBExecutor.MESSAGE_ID,
				ClusterManagerDBExecutor.REQUEST_ID);
		return innerTest;
	}

	/**
	 * Ensures that the {@link ClusterManager} can be used in tests created by the given
	 * {@link TestFactory}.
	 */
	public static TestFactory setupClusterManager(TestFactory innerFactory) {
		return new TestFactoryProxy(innerFactory) {

			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				return setupClusterManager(super.createSuite(testCase, suiteName));
			}
		};
	}
}

