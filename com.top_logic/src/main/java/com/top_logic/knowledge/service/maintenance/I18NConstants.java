/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.maintenance;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The operation requires an active maintenance window.
	 */
	public static ResKey ERROR_NO_MAINTENANCE_WINDOW;

	/**
	 * @en The operation requires this node to be the only active node of the cluster, but {0} nodes
	 *     are active.
	 */
	public static ResKey1 ERROR_CLUSTER_NODES_ACTIVE__COUNT;

	/**
	 * @en The state of the cluster cannot be determined.
	 */
	public static ResKey ERROR_CLUSTER_STATE_UNAVAILABLE;

	/**
	 * @en The persistency layer of this application does not store its data in a database and
	 *     cannot be maintained this way.
	 */
	public static ResKey ERROR_UNSUPPORTED_KNOWLEDGE_BASE;

	static {
		initConstants(I18NConstants.class);
	}
}
