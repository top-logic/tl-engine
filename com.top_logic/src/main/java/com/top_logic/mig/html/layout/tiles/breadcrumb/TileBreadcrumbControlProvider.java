/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.breadcrumb;

import static com.top_logic.basic.util.Utils.*;
import static java.util.Objects.requireNonNull;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbControl;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbData;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbDataOwner;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbRenderer;
import com.top_logic.layout.tree.breadcrumb.DefaultBreadcrumbRenderer;
import com.top_logic.layout.tree.breadcrumb.GenericBreadcrumbDataOwner;
import com.top_logic.layout.tree.breadcrumb.GenericBreadcrumbDataOwner.AnnotatedModel;
import com.top_logic.layout.tree.breadcrumb.MutableBreadcrumbData;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link ControlProvider} for the {@link BreadcrumbControl}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TileBreadcrumbControlProvider extends AbstractConfiguredInstance<TileBreadcrumbControlProvider.Config>
		implements ControlProvider {

	/** {@link ConfigurationItem} for the {@link TileBreadcrumbControlProvider}. */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<TileBreadcrumbControlProvider> {

		/** The XML tag for this component. */
		String TAG_NAME = "breadcrumb";

		/** Property name of {@link #getRenderer()}. */
		String RENDERER = "renderer";

		/** Property name of {@link #getTreeBuilder()}. */
		String TREE_BUILDER = "treeBuilder";

		/**
		 * The {@link BreadcrumbRenderer} that should render the breadcrumb.
		 */
		@Name(RENDERER)
		@ItemDefault
		@ImplementationClassDefault(DefaultBreadcrumbRenderer.class)
		PolymorphicConfiguration<BreadcrumbRenderer> getRenderer();

		/**
		 * The {@link TreeBuilder} for the {@link TLTreeModel} displayed by the breadcrumb.
		 */
		@Name(TREE_BUILDER)
		@ItemDefault
		@ImplementationClassDefault(TileBreadcrumbTreeModelBuilder.class)
		PolymorphicConfiguration<TileBreadcrumbTreeModelBuilder> getTreeBuilder();

	}

	private final BreadcrumbRenderer _renderer;

	private final TileBreadcrumbTreeModelBuilder _treeBuilder;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link TileBreadcrumbControlProvider}.
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
	public TileBreadcrumbControlProvider(InstantiationContext context, Config config) {
		super(context, config);
		_renderer = requireNonNull(context.getInstance(config.getRenderer()));
		_treeBuilder = requireNonNull(context.getInstance(config.getTreeBuilder()));
	}

	@Override
	public BreadcrumbControl createControl(Object model, String style) {
		checkParameters(model, style);
		return createControl(createData((TileContainerComponent) model));
	}

	private void checkParameters(Object model, String style) {
		if (!(model instanceof TileContainerComponent)) {
			throw new IllegalArgumentException(
				"The model has to be a " + TileContainerComponent.class.getName() + ". Actual value: " + debug(model));
		}
		if (style != null) {
			throw new IllegalArgumentException(
				"There are no diffent styles. The only valid parameter is null. Actual value: " + debug(style));
		}
	}

	private BreadcrumbData createData(TileContainerComponent component) {
		TileBreadcrumbTreeModel treeModel = createTreeModel(component);
		SingleSelectionModel selectionModel = treeModel.getSingleSelectionModel();

		AnnotatedModel algorithm = AnnotatedModel.INSTANCE;
		BreadcrumbDataOwner owner = new GenericBreadcrumbDataOwner(component, algorithm);
		BreadcrumbData data = createData(treeModel, selectionModel, owner);

		algorithm.annotate(component, data);

		return data;
	}

	private BreadcrumbControl createControl(BreadcrumbData data) {
		return new BreadcrumbControl(getRenderer(), data);
	}

	private TileBreadcrumbTreeModel createTreeModel(TileContainerComponent component) {
		return new TileBreadcrumbTreeModel(getTreeBuilder(), component);
	}

	private BreadcrumbData createData(TLTreeModel<?> treeModel, SingleSelectionModel treeSelection,
			BreadcrumbDataOwner owner) {
		return new MutableBreadcrumbData(treeModel, treeSelection, treeSelection, owner);
	}

	private TreeBuilder<TileBreadcrumbTreeNode> getTreeBuilder() {
		return _treeBuilder;
	}

	private final BreadcrumbRenderer getRenderer() {
		return _renderer;
	}

}
