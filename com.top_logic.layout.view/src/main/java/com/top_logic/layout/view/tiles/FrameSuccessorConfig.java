/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.config.annotation.Abstract;

/**
 * A frame that may follow the frame of the enclosing {@link FrameRouteConfig &lt;frame&gt;}
 * declaration, that is, be pushed onto it: a nested {@link FrameRouteConfig &lt;frame&gt;}
 * declaration, or a {@link FrameRefConfig &lt;frame-ref&gt;} naming one declared elsewhere in the
 * stack.
 */
@Abstract
public interface FrameSuccessorConfig extends FrameContentConfig {
	// Common type of the successors of a frame declaration.
}
