/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.tiles;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.LabelBasedPreview;
import com.top_logic.model.TLObject;

/**
 * {@link LabelBasedPreview} whose parts (label, description, and actual preview) are configired
 * separately.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class ModelBasedTilePreview extends LabelBasedPreview<ModelBasedTilePreview.Config> {

	/**
	 * Typed configuration interface definition for {@link ModelBasedTilePreview}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@DisplayOrder({
		Config.MODEL,
		Config.LABEL,
		Config.DESCRIPTION,
		Config.CONTENT_PREVIEW,
	})
	public interface Config extends LabelBasedPreview.Config<ModelBasedTilePreview> {
		/** Configuration name for the value of {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for the value of {@link #getDescription()}. */
		String DESCRIPTION = "description";

		/** Configuration name for the value of {@link #getContentPreview()}. */
		String CONTENT_PREVIEW = "content-preview";

		/** Configuration name for the value of {@link #getModel()}. */
		String MODEL = "model";

		/**
		 * Actually unused.
		 */
		@Override
		@Hidden
		ThemeImage getIcon();

		/**
		 * {@link ModelSpec} that is used to compute {@link #getLabel()}, {@link #getDescription()},
		 * and {@link #getContentPreview()} from.
		 * 
		 * <p>
		 * If no model is specified, the model of the tile is used. For example, if the tile
		 * represents a business object, this object is used as the model.
		 * </p>
		 */
		@Name(MODEL)
		ModelSpec getModel();

		/**
		 * Configuration of the label of the preview. When nothing is configured, the name of the
		 * component is used as label.
		 * 
		 * <p>
		 * The label is created with the model returned by {@link #getModel()}.
		 * </p>
		 */
		@Name(LABEL)
		PolymorphicConfiguration<TilePreviewPartProvider.Text> getLabel();

		/**
		 * Configuration of the description of the preview.
		 * 
		 * <p>
		 * The description is created with the model returned by {@link #getModel()}.
		 * </p>
		 * 
		 * @return May be <code>null</code>. In this case no description is rendered.
		 */
		@Name(DESCRIPTION)
		PolymorphicConfiguration<TilePreviewPartProvider.Text> getDescription();

		/**
		 * Configuration of the content preview.
		 * 
		 * <p>
		 * The preview is created with the model returned by {@link #getModel()}.
		 * </p>
		 *
		 * @return May be <code>null</code>. In this case no preview part is rendered.
		 */
		@Name(CONTENT_PREVIEW)
		PolymorphicConfiguration<TilePreviewPartProvider.Content> getContentPreview();

	}

	private TilePreviewPartProvider.Text _label;

	private TilePreviewPartProvider.Text _description;

	private TilePreviewPartProvider.Content _preview;

	private ChannelLinking _model;

	/**
	 * Create a {@link ModelBasedTilePreview}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ModelBasedTilePreview(InstantiationContext context, Config config) {
		super(context, config);
		_model = context.getInstance(config.getModel());
		_label = context.getInstance(config.getLabel());
		_description = context.getInstance(config.getDescription());
		_preview = context.getInstance(config.getContentPreview());
	}

	@Override
	protected HTMLFragment labelContent(ComponentTile tile) {
		if (_label != null) {
			HTMLFragment label = _label.createPreviewPart(model(tile));
			if (label != null) {
				return label;
			} else {
				return Fragments.empty();
			}
		}
		return Fragments.message(tile.getTileComponent().getTitleKey());
	}

	@Override
	protected HTMLFragment descriptionContent(ComponentTile tile) {
		if (_description != null) {
			HTMLFragment description = _description.createPreviewPart(model(tile));
			if (description != null) {
				return description;
			}
		}
		return Fragments.empty();
	}
	
	@Override
	protected HTMLFragment previewContent(ComponentTile tile) {
		if (_preview != null) {
			HTMLFragment description = _preview.createPreviewPart(model(tile));
			if (description != null) {
				return description;
			}
		}
		return super.previewContent(tile);
	}

	private Object model(ComponentTile tile) {
		if (_model == null) {
			Object tileBusinessObject = tile.getBusinessObject();
			return tileBusinessObject instanceof TLObject ? tileBusinessObject : null;
		}
		return ChannelLinking.eval(tile.getTileComponent(), _model);
	}

}

