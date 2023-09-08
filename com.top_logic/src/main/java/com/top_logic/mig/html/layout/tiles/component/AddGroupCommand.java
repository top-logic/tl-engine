/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import static com.top_logic.layout.DisplayDimension.*;

import java.util.Map;
import java.util.function.Function;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.messagebox.CreateConfigurationDialog;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileGroup;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * {@link AbstractCommandHandler} that creates a new empty {@link TileGroup} and adds it to the
 * calling component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AddGroupCommand extends AbstractCommandHandler {

	/** Id of the {@link AddGroupCommand} to configure in {@link CommandHandlerFactory}. */
	public static final String COMMAND_ID = "addTileGroup";

	/**
	 * Creates a new {@link AddGroupCommand}.
	 */
	public AddGroupCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		Function<? super StaticPreviewConfiguration, HandlerResult> okHandle =
			item -> {
				TileGroup newGroup = TypedConfiguration.newConfigItem(TileGroup.class);
				newGroup.setLabel(ResKey.text(item.getLabel()));
				newGroup.setPreview(StaticPreview.newStaticPreview(item.getImage(), ResKey.text(item.getDescription())));
				((TileContainerComponent) aComponent).addNewTile(newGroup);
				return HandlerResult.DEFAULT_RESULT;
			};
		DisplayDimension width = dim(300, DisplayUnit.PIXEL);
		DisplayDimension height = dim(230, DisplayUnit.PIXEL);
		return new CreateConfigurationDialog<>(StaticPreviewConfiguration.class, okHandle,
			I18NConstants.ADD_LAYOUT_GROUP.key("title"),
			width, height).open(aContext);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), HasCompositeTileLayout.INSTANCE);
	}

}

