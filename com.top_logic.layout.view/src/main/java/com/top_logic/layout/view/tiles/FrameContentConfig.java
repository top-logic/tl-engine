/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.view.UIElement;

/**
 * An entry inside a {@link FrameRouteConfig &lt;frame&gt;} declaration: a
 * {@link FrameParamConfig &lt;param&gt;} of the frame, or a {@link FrameSuccessorConfig frame that
 * may follow it}.
 */
@Abstract
public interface FrameContentConfig extends ConfigurationItem {

	/** Configuration name for {@link #getId()}. */
	String ID = UIElement.Config.ID;

	/**
	 * The name an overlay of the view addresses this entry by.
	 *
	 * <p>
	 * The entries of a {@link FrameRouteConfig &lt;frame&gt;} declaration and the declarations of a
	 * {@link TileStackElement &lt;tile-stack&gt;} are keyed by it; an entry without id is
	 * anonymous. A {@link FrameRouteConfig &lt;frame&gt;} declaration always has one, since it is
	 * pushed by its id.
	 * </p>
	 *
	 * <p>
	 * Unlike the id of an element, the id is no global key of the view: it is unique within its
	 * stack only, since the views displayed in a stack push their frames by these ids, and two
	 * stacks of one view displaying the same views declare frames of the same ids.
	 * </p>
	 *
	 * @see UIElement.Config#getId()
	 */
	@Name(ID)
	String getId();
}
