/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.cluster;

/**
 * An adapter class for the ClusterMessageListener interface.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class ClusterManagerAdapter implements ClusterManagerListener {

    @Override
	public void clusterPropertyChangeConfirmed(String propertyName, Object currentValue) {
    }

    @Override
	public void clusterPropertyChanged(String propertyName, Object oldValue, Object newValue, boolean changedByThisNode) {
    }

}
