/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.inspector.model.InspectorModel;
import com.top_logic.layout.inspector.model.InspectorTreeBuilder;
import com.top_logic.layout.inspector.model.InspectorTreeTableModel;
import com.top_logic.layout.table.model.EmptyTableConfigurationProvider;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.tree.TreeTableComponent;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;
import com.top_logic.layout.tree.model.TreeBuilder;

/**
 * Component displaying the inspected tree.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class InspectorTreeComponent extends TreeTableComponent {

	/**
	 * Configuration of {@link InspectorTreeComponent}.
	 */
	public interface Config extends TreeTableComponent.Config {

		@Override
		@InstanceDefault(EmptyTableConfigurationProvider.class)
		TableConfigurationProvider getAdditionalConfiguration();

	}

	/**
	 * Creates a {@link InspectorTreeComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InspectorTreeComponent(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof InspectorModel;
	}

	@Override
	protected <N extends AbstractTreeTableNode<N>> AbstractTreeTableModel<?> createTreeModel(
			TableConfiguration tableConfig, List<String> columns) {
		return new InspectorTreeTableModel(getInspectorTreeBuilder(), getInspectorModel(), columns, tableConfig);
	}

	private InspectorModel getInspectorModel() {
		return (InspectorModel) this.getModel();
	}

	private <N extends AbstractTreeTableNode<N>> InspectorTreeBuilder getInspectorTreeBuilder() {
		TreeBuilder<N> untypedTreeBuilder = this.getTreeBuilder();

		return (InspectorTreeBuilder) untypedTreeBuilder;
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean shouldCheckMissingTypeConfiguration() {
		/* This component might display any TLObject. It is therefore registered for all events.
		 * This check is therefore pointless here. */
		return false;
	}

}
