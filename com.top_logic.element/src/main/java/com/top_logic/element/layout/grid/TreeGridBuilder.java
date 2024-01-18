/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.tree.component.TreeBuilderTreeView;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredType;

/**
 * {@link GridBuilder} based on a {@link TreeModelBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeGridBuilder<R> extends AbstractTreeGridBuilder<R> {
	
	/**
	 * Configuration of a {@link TreeGridBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends AbstractTreeGridBuilder.Config {

		/**
		 * Configuration of the {@link TreeModelBuilder} that creates the tree.
		 */
		@Mandatory
		@Name(AbstractTreeGridBuilder.Config.BUILDER_ATTRIBUTE)
		PolymorphicConfiguration<TreeModelBuilder<Object>> getBuilder();

	}

	private final TreeModelBuilder<Object> _builder;
	
	/**
	 * Creates a new {@link StructureGridBuilder} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link StructureGridBuilder}.
	 */
	public TreeGridBuilder(InstantiationContext context, Config config) {
		super(context, config);
		_builder = context.getInstance(config.getBuilder());
	}

	/**
	 * Creates a {@link TreeGridBuilder}.
	 * 
	 * @param builder
	 *        The {@link TreeModelBuilder} to retrieve model elements.
	 */
	public TreeGridBuilder(TreeModelBuilder<Object> builder) {
		_builder = builder;
	}

	@Override
	public ModelBuilder unwrap() {
		return _builder;
	}

	@Override
	public boolean supportsRow(GridComponent grid, Object row) {
		return _builder.supportsNode(grid, row);
	}

	@Override
	public Object retrieveModelFromRow(GridComponent grid, Object row) {
		return _builder.retrieveModelFromNode(grid, row);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return _builder.supportsModel(aModel, aComponent);
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		return _builder.getModel(businessModel, aComponent);
	}
	
	@Override
	protected boolean canExpandAll() {
		return _builder.canExpandAll();
	}

	@Override
	protected Collection<? extends Object> getParents(LayoutComponent component, Object rowModel) {
		return _builder.getParents(component, rowModel);
	}

	@Override
	protected Iterable<?> getChildren(final LayoutComponent component, final Object rowModel) {
		final TreeModelBuilder<Object> builder = _builder;
		return new Iterable<>() {
			@Override
			public Iterator<Object> iterator() {
				// TreeView is to abstract.
				@SuppressWarnings("unchecked")
				Iterator<Object> result = (Iterator<Object>) builder.getChildIterator(component, rowModel);
				return result;
			}
		};
	}

	@Override
	protected boolean isLeaf(LayoutComponent component, Object childObject) {
		return _builder.isLeaf(component, childObject);
	}

	@Override
	protected Collection<? extends Object> getNodesToUpdate(LayoutComponent component, Object object) {
		return _builder.getNodesToUpdate(component, object);
	}

	@Override
	protected Set<TLStructuredType> getTypesToObserve() {
		return _builder.getTypesToObserve();
	}

	@Override
	protected TreeView<Object> asTreeView(GridComponent grid) {
		return new TreeBuilderTreeView<>(grid, _builder);
	}

}
