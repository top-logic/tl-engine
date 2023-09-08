/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.tree.model.TLTreeNodeResourceProvider;

/**
 * {@link TreeContentRenderer} that can be configured during construction.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurableTreeContentRenderer extends AbstractTreeContentRenderer {

	/**
	 * Configuration options for {@link ConfigurableTreeContentRenderer}.
	 */
	public interface Config<I extends ConfigurableTreeContentRenderer> extends PolymorphicConfiguration<I> {

		/** @see #getResourceProvider() */
		String RESOURCE_PROVIDER = "resourceProvider";

		/** @see #getTreeImageProvider() */
		String TREE_IMAGE_PROVIDER = "treeImageProvider";

		/**
		 * The {@link ResourceProvider} to take the display properties of node objects from.
		 */
		@Name(RESOURCE_PROVIDER)
		@InstanceFormat
		@InstanceDefault(TLTreeNodeResourceProvider.class)
		@ImplementationClassDefault(TLTreeNodeResourceProvider.class)
		ResourceProvider getResourceProvider();

		/**
		 * The {@link TreeImageProvider} providing images for rendering the tree structure.
		 */
		@Name(TREE_IMAGE_PROVIDER)
		@InstanceFormat
		@InstanceDefault(DefaultTreeImageProvider.class)
		TreeImageProvider getTreeImageProvider();

	}

	private final TreeImageProvider treeImages;
	private final ResourceProvider resourceProvider;

	public ConfigurableTreeContentRenderer() {
		this(DefaultTreeImageProvider.INSTANCE);
	}
	
	public ConfigurableTreeContentRenderer(TreeImageProvider treeImages) {
		this(treeImages, MetaResourceProvider.INSTANCE);
	}
	
	public ConfigurableTreeContentRenderer(TreeImageProvider treeImages, ResourceProvider resourceProvider) {
		this(resourceProvider, treeImages);
	}
	
	/**
	 * Creates a {@link ConfigurableTreeContentRenderer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfigurableTreeContentRenderer(InstantiationContext context, Config<?> config) {
		this(config.getResourceProvider(), config.getTreeImageProvider());
	}

	public ConfigurableTreeContentRenderer(ResourceProvider resourceProvider, TreeImageProvider treeImages) {
		this.treeImages = treeImages;
		this.resourceProvider = resourceProvider;
	}
	
	@Override
	public ResourceProvider getResourceProvider() {
		return resourceProvider;
	}

	@Override
	protected TreeImageProvider getTreeImages() {
		return treeImages;
	}

}
