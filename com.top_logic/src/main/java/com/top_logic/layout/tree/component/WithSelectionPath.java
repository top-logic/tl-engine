/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.SelectionChannelSPI;
import com.top_logic.layout.component.IComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ComponentChannel} definition for tree-like {@link IComponent}.
 * 
 * <p>
 * The channel {@value WithSelectionPath#SELECTION_PATH_CHANNEL} contains the path to the currently
 * selected object, resp. a {@link Collection} of paths if this component supports multiple
 * selection.
 * </p>
 * 
 * <p>
 * Note: In addition to implementing this interface, a {@link LayoutComponent} must ensure that
 * {@link LayoutComponent#channels()} contains a channel with name
 * {@link WithSelectionPath#SELECTION_PATH_CHANNEL}.
 * </p>
 * 
 * @see WithSelectionPath#SELECTION_PATH_SPI
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WithSelectionPath extends IComponent {

	/**
	 * Name of the corresponding {@link WithSelectionPath} delivering the components selection path.
	 */
	String SELECTION_PATH_CHANNEL = "selectionPath";

	/**
	 * Default {@link ChannelSPI} for creating a corresponding {@link ComponentChannel}.
	 */
	ChannelSPI SELECTION_PATH_SPI = new SelectionChannelSPI(SELECTION_PATH_CHANNEL, null, Collections.emptySet());

	/**
	 * The current component's selection paths.
	 * 
	 * <p>
	 * If this component supports single selection, then the value is either <code>null</code>
	 * representing that no element is selected or a {@link List} containing the path from the root
	 * node (at position <code>0</code>) to the selected object (at position
	 * <code>size() - 1</code>).
	 * </p>
	 * 
	 * <p>
	 * If this component supports multiple selection, then the value is a {@link Collection} (not
	 * null). The collection contains for each selected element a {@link List} holding the path from
	 * the root node (at position <code>0</code>) to the selected object (at position
	 * <code>size() - 1</code>).
	 * </p>
	 */
	default Object getSelectionPath() {
		return selectionPathChannel().get();
	}

	/**
	 * Sets the component's selection path.
	 * 
	 * @see #getSelectionPath()
	 */
	default boolean setSelectionPath(Object newValue) {
		return selectionPathChannel().set(newValue);
	}

	/**
	 * The {@link ComponentChannel} managing the component's selection path.
	 */
	default ComponentChannel selectionPathChannel() {
		return getChannel(SELECTION_PATH_CHANNEL);
	}

}
