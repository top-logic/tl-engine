/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.tree.model.MutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link StructureEditComponent} whose abstract methods are implemented by configured plugins.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigurableStructureEditComponent<N extends MutableTLTreeNode<N>> extends StructureEditComponent<N> {

	public interface Config extends StructureEditComponent.Config {

		@Name(TREE_MODEL_BUILDER_ELEMENT)
		PolymorphicConfiguration<? extends ModelBuilder> getTreeModelBuilder();

		@Name(NODE_GROUP_INITIALIZER_ELEMENT)
		@InstanceFormat
		NodeGroupInitializer getNodeGroupInitializer();

	}

	/**
	 * tag name of the inner configuration element which describes the {@link #treeModelBuilder}
	 */
	private static final String TREE_MODEL_BUILDER_ELEMENT = "treeModelBuilder";

	/**
	 * tag name of the inner configuration element which describes the {@link #nodeGroupInitializer}
	 */
	private static final String NODE_GROUP_INITIALIZER_ELEMENT = "nodeGroupInitializer";

	/**
	 * Model builder to create the {@link TLTreeModel} which serves as model for this component,
	 * i.e. {@link ModelBuilder#getModel(Object, LayoutComponent)} must return a non <code>null</code>
	 * {@link TLTreeModel}. must not be <code>null</code>
	 */
	private ModelBuilder treeModelBuilder;

	/**
	 * configured {@link NodeGroupInitializer}
	 */
	private NodeGroupInitializer nodeGroupInitializer;

	/**
	 * Creates a new {@link ConfigurableStructureEditComponent} from the given attributes. Is called
	 * by reflection with attributes created from configuration.
	 */
	public ConfigurableStructureEditComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		this.treeModelBuilder = context.getInstance(atts.getTreeModelBuilder());
		this.nodeGroupInitializer = atts.getNodeGroupInitializer();
	}

	@Override
	protected TLTreeModel<N> createTreeModel() {
		return (TLTreeModel<N>) treeModelBuilder.getModel(getModel(), this);
	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
		super.componentsResolved(context);

		if (treeModelBuilder == null) {
			final String msg =
				"Missing model builder in '" + this + "'. Maybe no '" + TREE_MODEL_BUILDER_ELEMENT + "' configured.";
			Logger.error(msg, ConfigurableStructureEditComponent.class);
			throw new IllegalStateException(msg);
		}
		if (nodeGroupInitializer == null) {
			final String msg =
				"Missing node group initializer builder in '" + this + "'. Maybe no '" + NODE_GROUP_INITIALIZER_ELEMENT
					+ "' configured.";
			Logger.error(msg, ConfigurableStructureEditComponent.class);
			throw new IllegalStateException(msg);
		}
	}

	@Override
	protected NodeGroupInitializer getNodeGroupInitializer() {
		return nodeGroupInitializer;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return treeModelBuilder.supportsModel(anObject, this);
	}

}
