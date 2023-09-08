/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.element.structured.wrap.SubtreeLoader;
import com.top_logic.layout.tree.component.AbstractTreeModelBuilder;
import com.top_logic.layout.tree.component.StructureModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModule;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadOperation;

/**
 * {@link StructureModelBuilder} that provides a filtered {@link StructuredElement}
 * tree.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultStructuredElementTreeModelBuilder extends AbstractTreeModelBuilder<StructuredElement>
		implements ConfiguredInstance<DefaultStructuredElementTreeModelBuilder.Config> {

	/**
	 * Configuration options for {@link DefaultStructuredElementTreeModelBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<ModelBuilder> {

		/**
		 * @see #getTreeFilters()
		 */
		String TREE_FILTERS_ATTRIBUTE = "treeFilters";

		/**
		 * @see #getStructureName()
		 */
		public static final String STRUCTURE_NAME_ATTRIBUTE = "structureName";

		/**
		 * Name of the structure to display (starting with the
		 * {@link TLModule#DEFAULT_SINGLETON_NAME} singleton).
		 */
		@Name(STRUCTURE_NAME_ATTRIBUTE)
		String getStructureName();

		/**
		 * List of and-concatenated {@link Filter}s that select the nodes to display.
		 */
		@Name(TREE_FILTERS_ATTRIBUTE)
		@DefaultContainer
		List<PolymorphicConfiguration<Filter<? super StructuredElement>>> getTreeFilters();

	}
	
	private final Filter<? super StructuredElement> _treeFilter;

	private Config _config;

	/**
	 * Creates a {@link DefaultStructuredElementTreeModelBuilder} from configuration.
	 */
	public DefaultStructuredElementTreeModelBuilder(InstantiationContext context, Config config) {
		_config = config;
		_treeFilter = concat(context, FilterFactory.trueFilter(), config.getTreeFilters());
	}
	
	private static Filter<? super StructuredElement> concat(InstantiationContext context,
			Filter<? super StructuredElement> start,
			List<PolymorphicConfiguration<Filter<? super StructuredElement>>> treeFilters) {
		Filter<? super StructuredElement> result = start;
		for (PolymorphicConfiguration<Filter<? super StructuredElement>> filter : treeFilters) {
			result = FilterFactory.and(start, context.getInstance(filter));
		}
		return result;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel == null) {
			return getRoot(aComponent);
		}
		assert supportsModel(businessModel, aComponent) : "component has model which is not supported.";
		return businessModel;
	}

	private StructuredElement getRoot(LayoutComponent contextComponent) {
		String structure = getConfig().getStructureName();
		if (structure.isEmpty()) {
			StringBuilder noStructure = new StringBuilder();
			noStructure.append("No structure configured. Can not create an initial model for component '");
			noStructure.append(contextComponent.getName());
			noStructure.append("'");
			throw new IllegalStateException(noStructure.toString());
		}
		return StructuredElementFactory.getInstanceForStructure(structure).getRoot();
	}

	@Override
	public Collection<? extends StructuredElement> getParents(LayoutComponent contextComponent, StructuredElement node) {
		StructuredElement parent = node.getParent();
		if (parent == null) {
			// node is root
			return Collections.emptyList();
		}
		return Collections.singletonList(parent);
	}

	@Override
	public Iterator<? extends StructuredElement> getChildIterator(StructuredElement node) {
		return node.getChildren(_treeFilter).iterator();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent component) {
		if (!getConfig().getStructureName().isEmpty() && aModel == null) {
			// A structure name is given, therefore a root model can be retrieved.
			StructuredElement root = getRoot(component);

			// The view is enabled, if the filter accepts the root element.
			return _treeFilter.accept(root);
		}
		return supportsNode(component, aModel);
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return (node instanceof StructuredElement) && _treeFilter.accept((StructuredElement) node);
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, StructuredElement node) {
		return node.getRoot();
	}

	@Override
	public PreloadOperation loadForExpansion() {
		return new PreloadOperation() {

			@Override
			public void prepare(PreloadContext context, Collection<?> baseObjects) {
				SubtreeLoader subtreeLoader = new SubtreeLoader(context);
				for (Object baseObject : baseObjects) {
					subtreeLoader.loadTree((StructuredElement) baseObject, 2);
				}
			}
		};
	}

}
