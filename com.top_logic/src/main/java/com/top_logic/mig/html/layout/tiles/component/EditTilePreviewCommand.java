/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import static com.top_logic.layout.DisplayDimension.*;

import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.LabeledConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.messagebox.CreateConfigurationDialog;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.component.StaticPreview.Config;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Command to edit the preview of a given {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EditTilePreviewCommand<T extends TileLayout & LabeledConfiguration & PreviewedTile> extends TileMenuCommand<T> {

	/**
	 * Creates a new {@link EditTilePreviewCommand}.
	 */
	public EditTilePreviewCommand(TileContainerComponent container, T tile) {
		super(container, tile);
		setImage(Icons.EDIT_TILE_PREVIEW_ICON);
		setLabel(Resources.getInstance().getString(I18NConstants.EDIT_TILE_PREVIEW_COMMAND));
	}

	@Override
	protected HandlerResult internalExecute(DisplayContext context) {
		Function<? super LabelConfiguration, HandlerResult> okHandle =
			item -> {
				if (item instanceof StaticPreviewConfiguration) {
					Config preview = StaticPreview.newStaticPreview(((StaticPreviewConfiguration) item).getImage(),
						ResKey.text(((StaticPreviewConfiguration) item).getDescription()));
					_tile.setPreview(preview);
				}
				_tile.setLabel(ResKey.text(item.getLabel()));
				_container.updateDisplayedLayout(((CompositeTile) _container.displayedLayout()).getTiles());
				return HandlerResult.DEFAULT_RESULT;
			};

		DisplayDimension width = dim(300, DisplayUnit.PIXEL);
		DisplayDimension height = dim(230, DisplayUnit.PIXEL);
		PolymorphicConfiguration<? extends TilePreview> previewConf = _tile.getPreview();
		Class<? extends LabelConfiguration> previewConfType;
		if (previewConf == null || previewConf instanceof StaticPreview.Config) {
			previewConfType = StaticPreviewConfiguration.class;
		} else {
			previewConfType = LabelConfiguration.class;
		}
		CreateConfigurationDialog<? extends LabelConfiguration> dialog =
			new CreateConfigurationDialog<>(previewConfType, okHandle,
				I18NConstants.EDIT_TILE_PREVIEW_DIALOG.key("title"), width,
				height);

		LabelConfiguration newPreview = dialog.getModel();
		ConfigurationDescriptor descriptor = newPreview.descriptor();
		newPreview.update(descriptor.getProperty(StaticPreviewConfiguration.LABEL_NAME),
			context.getResources().getString(_tile.getLabel()));

		if (newPreview instanceof StaticPreviewConfiguration && previewConf != null) {
			StaticPreview.Config staticPreview = (StaticPreview.Config) previewConf;
			newPreview.update(descriptor.getProperty(StaticPreviewConfiguration.DESCRIPTION_NAME),
				context.getResources().getString(staticPreview.getDescription()));
			newPreview.update(descriptor.getProperty(StaticPreviewConfiguration.IMAGE_NAME),
				staticPreview.getIcon());
		}
		return dialog.open(context);
	}

}
