/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.provider.ProxyResourceProvider;
import com.top_logic.layout.tree.model.TLTreeNodeResourceProvider;

/**
 * {@link Renderer} for the name column showing a link icon for linked content.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebFolderNameRenderer extends ResourceRenderer<ResourceRenderer.Config<WebFolderNameRenderer>> {

	public static class WebFolderNameResourceProvider extends ProxyResourceProvider {

		@Override
		public String getLink(DisplayContext context, Object anObject) {
			if (anObject instanceof WebFolder) {
				// No link for folders, because the goto is executed internally.
				return null;
			}
			if (anObject instanceof Document || anObject instanceof DocumentVersion) {
				// No link for content.
				return null;
			}
			return super.getLink(context, anObject);
		}

	}

	/**
	 * Singleton {@link WebFolderNameRenderer} instance.
	 */
	public static final WebFolderNameRenderer INSTANCE;
	static {
		TLTreeNodeResourceProvider resources = TLTreeNodeResourceProvider.newTLTreeNodeResourceProvider(new WebFolderNameResourceProvider());
		INSTANCE = TypedConfigUtil.createInstance(createConfig(WebFolderNameRenderer.class, resources));
	}

	/**
	 * Creates a new {@link WebFolderNameRenderer}.
	 */
	public WebFolderNameRenderer(InstantiationContext context, Config<WebFolderNameRenderer> config) {
		super(context, config);
	}

    @Override
	protected void writeImage(DisplayContext aContext, TagWriter out, Object aValue, ThemeImage anImage)
			throws IOException {
		FolderNode node = (FolderNode) aValue;
		if (node.isLink()) {
			out.beginBeginTag(SPAN);
			out.writeAttribute(STYLE_ATTR, "position:relative;");
            out.endBeginTag();
            {
            	super.writeImage(aContext, out, node, anImage);
            	
				XMLTag icon = Icons.SMALL_LINK.toIcon();
				icon.beginBeginTag(aContext, out);
            	out.writeAttribute(ALT_ATTR, "");

				icon.endEmptyTag(aContext, out);
            }
            out.endTag(SPAN);
        } else {
        	super.writeImage(aContext, out, node, anImage);
        }
    }
}