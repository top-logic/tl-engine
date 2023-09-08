/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree.compare;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.form.decorator.CompareInfo;
import com.top_logic.layout.form.decorator.CompareService;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.model.EmptyTableConfigurationProvider;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.table.tree.TreeTableComponent;
import com.top_logic.layout.table.tree.TreeTableData;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.IndexedTreeTableModel;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.layout.CommandRegistry;

/**
 * {@link TreeTableComponent}, that shows differences between tree based component models, using the
 * {@link CompareService}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class CompareTreeTableComponent extends TreeTableComponent {

	/**
	 * Configuration of {@link CompareTreeTableComponent}
	 */
	public interface Config extends TreeTableComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Property name of {@link #getTreeCompareConfigurationProvider()}
		 */
		public static final String TREE_COMPARE_CONFIGURATION_PROVIDER_PROPERTY = "treeCompareConfigurationProvider";

		/**
		 * Property name of {@link #getDefaultCompareMode()}}
		 */
		public static final String DEFAULT_COMPARE_MODE_PROPERTY = "defaultCompareMode";

		@ItemDefault(DefaultTreeTableBuilder.class)
		@Override
		PolymorphicConfiguration<TreeBuilder<DefaultTreeTableNode>> getTreeBuilder();

		/**
		 * {@link TreeCompareConfigurationProvider}, that is basically used to create the tree table
		 *         model of the compare view.
		 */
		@InstanceFormat
		@InstanceDefault(PlainTreeCompareConfigurationProvider.class)
		@Name(TREE_COMPARE_CONFIGURATION_PROVIDER_PROPERTY)
		TreeCompareConfigurationProvider getTreeCompareConfigurationProvider();

		/**
		 * {@link CompareMode}, which shall be initially used to display comparison result.
		 */
		@Name(DEFAULT_COMPARE_MODE_PROPERTY)
		CompareMode getDefaultCompareMode();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			TreeTableComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(ShowNextChangeCommand.COMMAND_NAME);
			registry.registerButton(ShowPreviousChangeCommand.COMMAND_NAME);
			registry.registerButton(OverlayCompareCommand.COMMAND_NAME);
			registry.registerButton(SideBySideCompareCommand.COMMAND_NAME);
		}

	}

	private TreeCompareConfigurationProvider _treeCompareModelProvider;
	private CompareMode _compareMode;


	/**
	 * Create a new {@link CompareTreeTableComponent}.
	 */
	public CompareTreeTableComponent(InstantiationContext context, Config attributes)
			throws ConfigurationException {
		super(context, attributes);
		_treeCompareModelProvider = attributes.getTreeCompareConfigurationProvider();
		_compareMode = attributes.getDefaultCompareMode();
	}

	/**
	 * Sets the table selection to next displayed comparison difference.
	 */
	public void selectNextChange() {
		AbstractTreeTableNode<?> selectionBefore = getSelectedNode();
		CompareService.selectNextChangeAfter(getTableData(), selectionBefore);
		if (selectionBefore == getSelectedNode()) {
			CompareService.showNoNextChangeMessage();
		}
	}

	private AbstractTreeTableNode<?> getSelectedNode() {
		return CollectionUtil.getSingleValueFromCollection(getSelectedNodes());
	}

	/**
	 * Sets the table selection to previous displayed comparison difference.
	 */
	public void selectPreviousChange() {
		AbstractTreeTableNode<?> selectionBefore = getSelectedNode();
		CompareService.selectPreviousChangeBefore(getTableData(), selectionBefore);
		if (selectionBefore == getSelectedNode()) {
			CompareService.showNoPreviousChangeMessage();
		}
	}

	/** @see #getCompareMode() */
	public void setCompareMode(CompareMode compareMode) {
		_compareMode = compareMode;
		getTableViewModel().resetToConfiguration();
		invalidate();
	}

	/**
	 * {@link CompareMode} to display comparison results.
	 */
	public CompareMode getCompareMode() {
		return _compareMode;
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		boolean supportsObject = super.supportsInternalModel(object);
		if (object == null) {
			return supportsObject;
		}
		for (Class<?> type : _treeCompareModelProvider.getSupportedModelTypes()) {
			if (type.isAssignableFrom(object.getClass())) {
				return supportsObject;
			}
		}
		return false;
	}

	@Override
	protected ITableRenderer getTableRenderer() {
		return DefaultTableRenderer.newInstance();
	}

	@Override
	protected AbstractTreeTableModel<?> createTreeModel() {
		TreeCompareConfiguration treeCompareConfig =
			_treeCompareModelProvider.getTreeCompareConfiguration(getModel(), this);
		AbstractTreeTableModel<?> compareTreeTableModel;
		if (treeCompareConfig != null) {
			CompareService<CompareInfo> compareService = new CompareService<>(null, null);
			if (_compareMode == CompareMode.OVERLAY) {
				compareTreeTableModel =
					compareService.getCompareTreeTableModel(treeCompareConfig);
			} else {
				compareTreeTableModel =
					compareService.getSideBySideCompareTreeTableModel(treeCompareConfig);
			}
		} else {
			compareTreeTableModel =
				new IndexedTreeTableModel<DefaultTreeTableNode>(getTreeBuilder(), "", Collections.<String> emptyList(),
				TableConfigurationFactory.build(EmptyTableConfigurationProvider.INSTANCE));
		}
		return compareTreeTableModel;

	}

	@Override
	protected void adjustTreeTableData(TreeTableData treeTableData) {
		if (getModel() != null) {
			CompareService.selectNextChange(treeTableData, null);
		}
	}

}
