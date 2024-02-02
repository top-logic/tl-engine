/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.tree.component.StructureModelBuilder;
import com.top_logic.layout.tree.component.TreeBuilderTreeView;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredType;

/**
 * {@link GridBuilder} that retrieves its model objects from a
 * {@link StructureModelBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructureGridBuilder<R> extends AbstractTreeGridBuilder<R> {

	/**
	 * Configuration of a {@link StructureGridBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends AbstractTreeGridBuilder.Config {

		/**
		 * Configuration of the actual {@link StructureModelBuilder}, that creates the tree.
		 */
		@Mandatory
		@Name(AbstractTreeGridBuilder.Config.BUILDER_ATTRIBUTE)
		PolymorphicConfiguration<StructureModelBuilder<Object>> getBuilder();

	}

	final StructureModelBuilder<Object> _treeBuilder;
	
	/**
	 * Creates a new {@link StructureGridBuilder} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link StructureGridBuilder}.
	 */
	public StructureGridBuilder(InstantiationContext context, Config config) {
		super(context, config);
		_treeBuilder = context.getInstance(config.getBuilder());
	}

	@Override
	public ModelBuilder unwrap() {
		return _treeBuilder;
	}

	/**
	 * Creates a {@link StructureGridBuilder}.
	 * 
	 * @param treeBuilder
	 *        The {@link TreeBuilder} that provides model objects.
	 */
	public StructureGridBuilder(StructureModelBuilder<Object> treeBuilder) {
		_treeBuilder = treeBuilder;
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		return _treeBuilder.getModel(businessModel, aComponent);
	}
	
	@Override
	protected boolean canExpandAll() {
		return _treeBuilder.canExpandAll();
	}
	
	@Override
	protected Collection<? extends Object> getParents(LayoutComponent component, Object rowModel) {
		return Collections.singletonList(_treeBuilder.getParent(rowModel));
	}

	@Override
	protected Iterable<?> getChildren(final LayoutComponent component, final Object rowModel) {
		return new Iterable<>() {
			@Override
			public Iterator<Object> iterator() {
				// Interface TreeView is to abstract.
				@SuppressWarnings("unchecked")
				Iterator<Object> result = (Iterator<Object>) _treeBuilder.getChildIterator(component, rowModel);
				return result;
			}
		};
	}
	
	@Override
	protected boolean isLeaf(LayoutComponent component, Object childObject) {
		return _treeBuilder.isLeaf(component, childObject);
	}

	@Override
	protected Collection<? extends Object> getNodesToUpdate(LayoutComponent component, Object object) {
		return Collections.emptySet();
	}

	@Override
	protected Set<TLStructuredType> getTypesToObserve() {
		return Collections.emptySet();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return _treeBuilder.supportsModel(aModel, aComponent);
	}

	@Override
	public boolean supportsRow(GridComponent grid, Object row) {
		return _treeBuilder.supportsNode(grid, row);
	}

	@Override
	protected TreeView<Object> asTreeView(GridComponent grid) {
		return new TreeBuilderTreeView<>(grid, _treeBuilder);
	}

	@Override
	public Object retrieveModelFromRow(GridComponent grid, Object row) {
		return getRootForNode(row);
	}

	private Object getRootForNode(Object nodeModel) {
		Object result = nodeModel;
		while (true) {
			Object parent = _treeBuilder.getParent(result);
			if (parent == null) {
				return result;
			}
			
			result = parent;
		}
	}

}
