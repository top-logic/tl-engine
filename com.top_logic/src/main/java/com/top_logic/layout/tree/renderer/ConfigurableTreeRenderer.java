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
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeRenderer;

/**
 * Configurable {@link TreeRenderer} that can be configured during construction.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurableTreeRenderer extends TreeRenderer {

	/**
	 * Configuration options for {@link ConfigurableTreeRenderer}.
	 */
	public interface Config<I extends ConfigurableTreeRenderer> extends PolymorphicConfiguration<I> {

		/** @see #getContentTag() */
		String CONTENT_TAG = "contentTag";

		/** @see #getNodeTag() */
		String NODE_TAG = "nodeTag";

		/** @see #getTreeContentRenderer() */
		String TREE_CONTENT_RENDERER = "treeContentRenderer";

		/**
		 * The HTML tag to use for the whole tree.
		 */
		@Name(CONTENT_TAG)
		@StringDefault(DefaultTreeRenderer.CONTROL_TAG)
		String getContentTag();

		/**
		 * The HTML tag to use for tree nodes.
		 */
		@Name(NODE_TAG)
		@StringDefault(DefaultTreeRenderer.NODE_TAG)
		String getNodeTag();

		/**
		 * The {@link TreeContentRenderer} responsible for rendering node contents.
		 */
		@Name(TREE_CONTENT_RENDERER)
		@InstanceFormat
		@InstanceDefault(ConfigurableTreeContentRenderer.class)
		@ImplementationClassDefault(ConfigurableTreeContentRenderer.class)
		TreeContentRenderer getTreeContentRenderer();

	}

	private final String controlTag;
	private final String nodeTag;
	private final TreeContentRenderer contentRenderer;
	
	/**
	 * Creates a {@link ConfigurableTreeRenderer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfigurableTreeRenderer(InstantiationContext context, Config<?> config) {
		this(config.getContentTag(), config.getNodeTag(), config.getTreeContentRenderer());
	}

	public ConfigurableTreeRenderer(String contentTag, String nodeTag, TreeContentRenderer contentRenderer) {
		this.controlTag = contentTag;
		this.nodeTag = nodeTag;
		this.contentRenderer = contentRenderer;
	}

	@Override
	protected final String getControlTag(TreeControl control) {
		return this.controlTag;
	}

	@Override
	protected String getNodeTag() {
		return nodeTag;
	}

	@Override
	public TreeContentRenderer getTreeContentRenderer() {
		return contentRenderer;
	}
	
}
