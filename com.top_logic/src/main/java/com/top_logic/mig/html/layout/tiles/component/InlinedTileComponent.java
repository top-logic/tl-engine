/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.component.LayoutContainerBoundChecker;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.SingleLayoutContainer;
import com.top_logic.mig.html.layout.tiles.InlinedTileFactory;
import com.top_logic.mig.html.layout.tiles.TileFactory;
import com.top_logic.mig.html.layout.tiles.TileInfo;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerDelegate;

/**
 * {@link SingleLayoutContainer} that offers tiles for multiple business objects in the "parent
 * tile".
 * 
 * <p>
 * A {@link InlinedTileComponent} is a special component in the tile context for displaying multiple
 * business objects as tiles. No tile is created for the {@link InlinedTileComponent} itself, but
 * the tiles for the business objects are displayed.
 * </p>
 * 
 * @see TileListComponent
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InlinedTileComponent extends SingleLayoutContainer implements Selectable, BoundCheckerDelegate {

	/**
	 * Typed configuration interface definition for {@link InlinedTileComponent}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends SingleLayoutContainer.Config {

		/**
		 * Configuration element for setting a {@link #getModelBuilder()}.
		 */
		String MODEL_BUILDER_ELEMENT = "modelBuilder";

		/**
		 * {@link ModelBuilder} that creates the elements that may be selectable.
		 */
		@Name(MODEL_BUILDER_ELEMENT)
		@Mandatory
		PolymorphicConfiguration<? extends ModelBuilder> getModelBuilder();

		/**
		 * @see #getModelBuilder()
		 */
		void setModelBuilder(PolymorphicConfiguration<? extends ModelBuilder> value);

		@Override
		@ItemDefault(SingleLayoutContainer.ChildControlProvider.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

	}

	/**
	 * Special {@link TileInfo} which uses a {@link TileFactory} creating multiple tiles for a
	 * component.
	 * 
	 * @see InlinedTileFactory
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface InlinedTileInfo extends TileInfo {

		@Override
		@InstanceDefault(InlinedTileFactory.class)
		TileFactory getTileFactory();

	}

	private ModelBuilder _builder;

	private final BoundChecker _boundCheckerDelegate = new LayoutContainerBoundChecker<>(this);

	/**
	 * Create a {@link InlinedTileComponent}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public InlinedTileComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_builder = context.getInstance(config.getModelBuilder());
	}

	/**
	 * This will be delegated to the {@link ModelBuilder#supportsModel(Object, LayoutComponent)}.
	 *
	 * @param anObject
	 *        The object to be checked.
	 * @return If the model builder can process the given object.
	 */
	@Override
	protected boolean supportsInternalModel(Object anObject) {
		if (!super.supportsInternalModel(anObject)) {
			return false;
		}
		return _builder.supportsModel(anObject, this);
	}

	/**
	 * Determines the gui model for this component.
	 */
	public Object getGUIModel() {
		return _builder.getModel(getModel(), this);
	}

	@Override
	public BoundChecker getDelegate() {
		return _boundCheckerDelegate;
	}

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

}
