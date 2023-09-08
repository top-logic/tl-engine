/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.cluster;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManagerException;
import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link ClusterManager} that changes its state to
 * {@link com.top_logic.base.cluster.ClusterManager.NodeState#STARTUP} directly after start up of
 * the module.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ClusterManagerForTest extends ClusterManager {

	/**
	 * Creates a new {@link ClusterManagerForTest}.
	 */
	public ClusterManagerForTest(InstantiationContext context, Config config) throws ClusterManagerException {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		setNodeState(ClusterManager.NodeState.STARTUP);
	}

}

