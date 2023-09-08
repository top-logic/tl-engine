/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import java.io.IOException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbContentRenderer;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbControl;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class BreadcrumbComponentTreeContentRenderer extends BreadcrumbContentRenderer {
    
	/**
	 * Called by the {@link TypedConfiguration} for creating a
	 * {@link BreadcrumbComponentTreeContentRenderer}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public BreadcrumbComponentTreeContentRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void writeNodeText(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		if(currentNode instanceof LayoutComponent) {
			writeComponentTitle(out, (LayoutComponent) currentNode);
			
		}
		else if(currentNode instanceof DefaultMutableTLTreeNode) {
			DefaultMutableTLTreeNode theNode = (DefaultMutableTLTreeNode) currentNode;
			Object theUObject = theNode.getBusinessObject();
			if(theUObject instanceof LayoutComponent) {
				writeComponentTitle(out, (LayoutComponent) theUObject);
			}
			else {
				super.writeNodeText(context, out, breadcrumb, currentNode);
			}
		}
		else {
			super.writeNodeText(context, out, breadcrumb, currentNode);
		}
	}

	private void writeComponentTitle(TagWriter out, LayoutComponent component) {
		String nodeText = Resources.getInstance().getString(component.getTitleKey());
		out.writeText(nodeText);
	}

}
