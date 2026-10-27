/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;

/**
 * An entry inside a {@link FrameRouteConfig &lt;frame&gt;} declaration: a
 * {@link FrameParamConfig &lt;param&gt;} of the frame, or a {@link FrameSuccessorConfig frame that
 * may follow it}.
 */
@Abstract
public interface FrameContentConfig extends ConfigurationItem {
	// Common type of the entries of a frame declaration.
}
