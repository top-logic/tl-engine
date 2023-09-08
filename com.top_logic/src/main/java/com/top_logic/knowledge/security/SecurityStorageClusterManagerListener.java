/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.security;

import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManagerListener;
import com.top_logic.util.Utils;

/**
 * Updates the {@link SecurityStorage#PROPERTY_AUTOUPDATE} and
 * {@link SecurityStorage#PROPERTY_REBUILDING} in the SecurityStorage when they change their value
 * in the {@link ClusterManager}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class SecurityStorageClusterManagerListener implements ClusterManagerListener {

	private final SecurityStorage _storage;

	SecurityStorageClusterManagerListener(SecurityStorage storage) {
		_storage = storage;
	}

	@Override
	public void clusterPropertyChangeConfirmed(String propertyName, Object currentValue) {
		if (SecurityStorage.PROPERTY_REBUILDING.equals(propertyName)) {
			_storage.rebuildFlag = (Boolean) currentValue;
		}
	}

	@Override
	public void clusterPropertyChanged(String propertyName, Object oldValue, Object newValue, boolean thisNode) {
		if (SecurityStorage.PROPERTY_AUTOUPDATE.equals(propertyName)) {
			_storage.autoUpdate = Utils.getbooleanValue(newValue);
		}
		if (SecurityStorage.PROPERTY_REBUILDING.equals(propertyName)) {
			_storage.rebuildFlag = Utils.equals(oldValue, newValue) ? (Boolean) newValue : null;
		}
	}

}
