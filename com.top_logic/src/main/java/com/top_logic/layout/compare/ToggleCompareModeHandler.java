/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} toggling the compare mode of a {@link RevisionCompareComponent}.
 * 
 * @see RevisionCompareComponent#toggleCompareMode()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ToggleCompareModeHandler extends AbstractCommandHandler {

	/**
	 * Configuration of a {@link ToggleCompareModeHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/** Name for {@link ToggleCompareModeHandler.Config#getDeactivateImage()}. */
		String DEACTIVATE_IMAGE = "deactivateImage";

		/**
		 * Image to display to deactivate compare mode.
		 * 
		 * <p>
		 * If <code>null</code>, {@link #getImage()} is used instead.
		 * </p>
		 */
		@Name(DEACTIVATE_IMAGE)
		ThemeImage getDeactivateImage();

		@Override
		@FormattedDefault(TARGET_NULL)
		ModelSpec getTarget();
	}

	/**
	 * Creates a new {@link ToggleCompareModeHandler}.
	 */
	public ToggleCompareModeHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	private ThemeImage getDeactivateImage() {
		Config config = (Config) getConfig();
		ThemeImage result = config.getDeactivateImage();
		if (result == null) {
			return config.getImage();
		}
		return result;
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {
		boolean compareModeActive = ((RevisionCompareComponent) aComponent).toggleCompareMode();
		CommandModel commandModel = getCommandModel(someArguments);
		if (commandModel != null) {
			adaptCommandModel(aContext, aComponent, commandModel, compareModeActive);
		} else {
			/* This happens in application tests, because the command is executed without
			 * CommandModel. */
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private void adaptCommandModel(DisplayContext context, LayoutComponent component, CommandModel commandModel,
			boolean compareModeActive) {
		ResKey resKey = getResourceKey(component);
		if (compareModeActive) {
			commandModel.setImage(getDeactivateImage());
			commandModel.setLabel(context.getResources().getStringWithDefaultKey(resKey.suffix(".active"), resKey));
		} else {
			commandModel.setImage(getImage(component));
			commandModel.setLabel(context.getResources().getString(resKey));
		}
	}

	@Override
	protected ResKey getDefaultI18NKey() {
		return I18NConstants.TOGGLE_COMPARE_MODE;
	}

}
