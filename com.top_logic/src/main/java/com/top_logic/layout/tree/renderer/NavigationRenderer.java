/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbContentRenderer;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbControl;
import com.top_logic.layout.tree.model.ComponentTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.NoImageResourceProvider;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.util.Resources;

/**
 * The class {@link NavigationRenderer} renders the nodes of the tree of some
 * {@link ComponentTreeModel}.
 * 
 * @see ComponentTreeModel
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NavigationRenderer extends BreadcrumbContentRenderer {

	/** {@link ConfigurationItem} for the {@link NavigationRenderer}. */
	public interface Config extends BreadcrumbContentRenderer.Config {

		@BooleanDefault(false)
		@Override
		boolean isRootVisible();

		@ImplementationClassDefault(NoImageResourceProvider.class)
		@Override
		PolymorphicConfiguration<ResourceProvider> getResourceProvider();

	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link NavigationRenderer}.
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
	public NavigationRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Expects that the given node is a {@link TLTreeNode} whose
	 * {@link TLTreeNode#getBusinessObject()} is a {@link Card}.
	 */
	@Override
	protected void writeNodeText(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		if (currentNode.equals(breadcrumb.getTree().getRoot()) && getRootLabel() != null) {
			out.writeText(Resources.getInstance().getString(getRootLabel()));
		} else {
			final TLTreeNode<?> node = (TLTreeNode<?>) currentNode;
			final Card userObject = (Card) node.getBusinessObject();
			userObject.writeCardInfo(context, out);
		}
	}

}
