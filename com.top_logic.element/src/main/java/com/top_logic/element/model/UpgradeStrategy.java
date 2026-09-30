/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Strategy for automatic upgrade, if the model of a new software version differs form the model
 * baseline of the running application.
 */
public enum UpgradeStrategy implements ExternallyNamed {
	/**
	 * When the model in a new software version differs from the model baseline of the running
	 * application, upgrade the application model with inferred changes from the new software
	 * version.
	 */
	UPGRADE,

	/**
	 * Ignore model changes in the new software version and boot with the current application model.
	 */
	IGNORE,

	/**
	 * Prevent application startup, if the model of the new software version differs from the model
	 * baseline of the running application.
	 */
	PREVENT,;

	@Override
	public String getExternalName() {
		return switch (this) {
			// For legacy compatibility.
			case UPGRADE -> "true";

			// For legacy compatibility.
			case IGNORE -> "false";

			case PREVENT -> "prevent";
		};
	}

}
