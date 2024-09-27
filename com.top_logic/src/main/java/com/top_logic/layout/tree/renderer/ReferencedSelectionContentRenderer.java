/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link ConfigurableTreeContentRenderer}, that is used in popup select dialogs, to generate
 * {@link HTMLConstants#SPAN}s for node entries.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ReferencedSelectionContentRenderer extends ConfigurableTreeContentRenderer {

	/**
	 * Create a new {@link ReferencedSelectionContentRenderer}.
	 */
	public ReferencedSelectionContentRenderer(TreeImageProvider treeImages, ResourceProvider resourceProvider) {
		super(resourceProvider, treeImages);
	}

	@Override
	protected void renderTextLinkStart(DisplayContext context, TagWriter writer, TreeControl tree,
			Object node, boolean canSelect) throws IOException {
		ResKey tooltip = ResKey.text(getResourceProvider().getTooltip(node));
		writeTextLinkStart(context, writer, SPAN, canSelect, OnClickWriter.NONE, node, tooltip);
	}

	@Override
	protected void renderTextLinkStop(TagWriter writer, boolean canSelect) throws IOException {
		writeTextLinkStop(writer, SPAN, canSelect);
	}

}
