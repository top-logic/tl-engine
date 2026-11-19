/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.ui;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.view.dnd.DropConfig;

/**
 * Configuration of one {@code <drop>} of a {@link FlowDiagramElement}: a drop made on the diagram
 * as a whole, or onto a node of it.
 *
 * <p>
 * The {@link #getTargetChannel() target channel}, the {@link #getTargetExecutability() target
 * executability} rules and the {@link #getRefuseIf() refusal function} get the user object of the
 * node dropped onto for a drop onto a {@link FlowDropTargetMode#NODE node}; a drop on the
 * {@link FlowDropTargetMode#DIAGRAM diagram} has no target. Both get the point the drop is made at,
 * see {@link #getXAs()}.
 * </p>
 */
@TagName(DropConfig.TAG_NAME)
public interface FlowDropConfig extends DropConfig {

	/** Configuration name for {@link #getTarget()}. */
	String TARGET = "target";

	/**
	 * Whether the diagram as a whole or a single node is the target of this drop.
	 */
	@Name(TARGET)
	FlowDropTargetMode getTarget();

}
