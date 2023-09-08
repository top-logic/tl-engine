/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.cluster.monitor;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.basic.Logger;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorMessage.Status;
import com.top_logic.util.monitor.MonitorResult;

/**
 * {@link MonitorComponent} retrieving status from the {@link ClusterManager}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClusterMonitor implements MonitorComponent {

	@Override
	public String getName() {
		return ClusterManager.class.getSimpleName();
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void checkState(MonitorResult result) {
		Status status = Status.INFO;
		StringBuilder message = new StringBuilder();

		if (!ClusterManager.Module.INSTANCE.isActive()) {
			message.append("Not active");
			status = Status.FATAL;
		} else {
			ClusterManager cm = ClusterManager.getInstance();
			message.append("Node ID: ");
			message.append(cm.getNodeId());

			message.append(", Node state: ");
			message.append(cm.getNodeState());

			try {
				List<Long> activeNodes = cm.getActiveNodes();
				message.append(", Active nodes: ");
				message.append(activeNodes);
			} catch (SQLException ex) {
				message.append(", Error retrieving node state: " + ex.getMessage());
				status = Status.ERROR;
				Logger.error("Error retrieving node states.", ex, ClusterMonitor.class);
			}
		}

		result.addMessage(new MonitorMessage(status, message.toString(), this));
	}

}
