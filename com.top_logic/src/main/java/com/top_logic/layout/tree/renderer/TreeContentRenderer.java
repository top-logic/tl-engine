/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Interface for rendering single nodes in the contents of a {@link TreeControl}
 * 
 * <p>
 * This class is a class instead of a Java interface to provide type-safe
 * adaption to the generic {@link Renderer} interface.
 * </p>
 * 
 * @see ConfigurableTreeRenderer for a {@link TreeRenderer} that can have a
 *      {@link TreeContentRenderer} plug-in.
 * 
 * @see TreeRenderer for specifying the visual apperance of a complete
 *      {@link TreeControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TreeContentRenderer implements Renderer<NodeContext>, HTMLConstants {
	
	/**
	 * Returns the {@link ResourceProvider} of this {@link TreeContentRenderer}.
	 */
	public abstract ResourceProvider getResourceProvider();

	/**
	 * Final adapter method to provide a type-safe interface
	 * {@link #writeNodeContent(DisplayContext, TagWriter, NodeContext)} for
	 * {@link TreeContentRenderer}s but still adhere to the {@link Renderer}
	 * interface.
	 * 
	 * @see #writeNodeContent(DisplayContext, TagWriter, NodeContext)
	 */
	@Override
	public final void write(DisplayContext context, TagWriter out, NodeContext value) throws IOException {
		this.writeNodeContent(context, out, value);
	}
	
	/**
	 * Renders the contents of the HTML element respresenting the given node
	 * object.
	 */
	public abstract void writeNodeContent(DisplayContext context, TagWriter writer, NodeContext nodeContext) throws IOException;

}
