/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlRenderer;

/**
 * Creates client side views of {@link ListControl}s. It must render the Control
 * of the following form
 * 
 * <pre>
 * 	&lt;controlTag id=&quot;controlID&quot;&gt;
 * 		...
 * 		&lt;contentTag id=&quot;contentID&quot;&gt;
 * 			&lt;renderItems /&gt;
 * 		&lt;contentTag&gt;
 * 		...
 * 	&lt;/controlTag&gt;
 * </pre>
 * 
 * 
 * where &quot;controlID&quot; must be {@link ListControl#getID()},
 * &quot;contentID&quot; must be {@link ListControl#getContentID()}, and
 * &quot;renderItems&quot; must be the things written by
 * {@link ListRenderer#renderItems(DisplayContext, TagWriter, ListControl, int, int)}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ListRenderer extends ControlRenderer<ListControl> {

	/**
	 * Convenience method for rendering the contiguous sequence of list items
	 * from the given start index (inclusively) to the given stop index
	 * (exclusively).
	 * 
	 * <br/>
	 * 
	 * {@link ListControl} will use this method to write updates.
	 */
	public void renderItems(DisplayContext context, TagWriter out, ListControl view, int startIndex, int stopIndex) throws IOException;

}
