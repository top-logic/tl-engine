/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table.tree.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.element.layout.structured.StructuredElementTreeModel;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.compare.CompareAlgorithmHolder;
import com.top_logic.layout.compare.RevisionCompareComponent;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.structure.EmptyLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.EmptyTableConfigurationProvider;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.table.tree.compare.CompareTreeTableComponent;
import com.top_logic.layout.table.tree.compare.TreeCompareConfiguration;
import com.top_logic.layout.tree.renderer.TreeCellRenderer;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;

/**
 * Invisible layout component, that will be used as model adapter of
 * {@link RevisionCompareComponent} to {@link CompareTreeTableComponent}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class AdapterRevisionCompareComponent extends LayoutComponent implements Selectable, CompareAlgorithmHolder {

	/**
	 * Configuration for {@link AdapterRevisionCompareComponent}.
	 */
	public interface Config extends LayoutComponent.Config, Selectable.SelectableConfig {

		/** Attribute name of {@link #getObjectTypes()} */
		public static final String OBJECT_TYPES_ATTRIBUTE = "objectTypes";

		/** Attribute name of {@link #getTableConfigurationProvider()} */
		public static final String TABLE_CONFIGURATION_PROVIDER_ATTRIBUTE = "tableConfigurationProvider";

		@Override
		@ItemDefault(EmptyLayoutControlProvider.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		/**
		 * string representation of object types, that shall be displayed in compare table.
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(OBJECT_TYPES_ATTRIBUTE)
		List<String> getObjectTypes();

		/**
		 * {@link TableConfigurationProvider}, that will be applied to
		 *         {@link TableConfiguration}s of comparison base and comparison change.
		 */
		@InstanceFormat
		@InstanceDefault(EmptyTableConfigurationProvider.class)
		@Name(TABLE_CONFIGURATION_PROVIDER_ATTRIBUTE)
		TableConfigurationProvider getTableConfigurationProvider();
	}

	private CompareAlgorithm _compareAlgorithm;

	/**
	 * Create a new {@link AdapterRevisionCompareComponent}.
	 */
	public AdapterRevisionCompareComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public void setCompareAlgorithm(CompareAlgorithm algorithm) {
		_compareAlgorithm = algorithm;
		setSelected(getCompareModel(getModel()));
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		setSelected(getCompareModel(getModel()));
	}

	private Object getCompareModel(Object model) {
		if (model == null || getCompareAlgorithm() == null) {
			return null;
		}

		Object firstTreeRoot = getCompareAlgorithm().getCompareObject(this, model);
		if (firstTreeRoot == null) {
			return null;
		}

		TreeCompareConfiguration treeCompareConfiguration = new TreeCompareConfiguration();
		treeCompareConfiguration.setFirstTreeRoot(firstTreeRoot);
		treeCompareConfiguration.setFirstTree(new StructuredElementTreeModel((StructuredElement) firstTreeRoot));
		treeCompareConfiguration.setSecondTreeRoot(model);
		treeCompareConfiguration.setSecondTree(new StructuredElementTreeModel((StructuredElement) model));
		treeCompareConfiguration.setFirstTableConfigurationProvider(getTableConfigurationProvider(firstTreeRoot));
		treeCompareConfiguration.setSecondTableConfigurationProvider(getTableConfigurationProvider(model));
		return treeCompareConfiguration;
	}

	private TableConfigurationProvider getTableConfigurationProvider(Object model) {
		List<TableConfigurationProvider> providers = new ArrayList<>();

		addGenericConfigurations(providers, model);
		providers.add(new NoDefaultColumnAdaption() {
			
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				ColumnConfiguration selfColumn = table.declareColumn("name");
				com.top_logic.layout.tree.renderer.TreeCellRenderer.Config treeCellRendererConfig =
					TypedConfiguration.newConfigItem(TreeCellRenderer.Config.class);
				treeCellRendererConfig.setImplementationClass(TreeCellRenderer.class);

				CellRenderer treeCellRenderer =
						SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(treeCellRendererConfig);
				selfColumn.setCellRenderer(treeCellRenderer);
				table.setDefaultColumns(Collections.singletonList(selfColumn.getName()));
			}
		});
		providers.add(((Config) getConfig()).getTableConfigurationProvider());
		providers.add(GenericTableConfigurationProvider.showDefaultColumns());
		return TableConfigurationFactory.combine(providers);
	}

	private void addGenericConfigurations(List<TableConfigurationProvider> providers, Object model) {
		List<String> objectTypes = ((Config) getConfig()).getObjectTypes();
		if (objectTypes.isEmpty()) {
			providers.add(GenericTableConfigurationProvider
				.getTableConfigurationProvider((TLClass) ((StructuredElement) model).tType()));
		} else {
			Set<TLClass> classes = new HashSet<>();
			for (String objectType : objectTypes) {
				classes.add(resolveMetaElement(objectType));
			}
			providers.add(new GenericTableConfigurationProvider(classes));
		}
	}

	private TLClass resolveMetaElement(String metaElementName) {
		return (TLClass) TLModelUtil.findType(metaElementName);
	}

	@Override
	public CompareAlgorithm getCompareAlgorithm() {
		return _compareAlgorithm;
	}

}
