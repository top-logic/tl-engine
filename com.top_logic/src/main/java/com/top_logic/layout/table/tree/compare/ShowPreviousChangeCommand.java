/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree.compare;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.tool.boundsec.AbstractCommandHandler;

/**
 * {@link AbstractCommandHandler}, that steps through structure comparison changes, displayed by
 * {@link CompareTreeTableComponent}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ShowPreviousChangeCommand extends ShowChangeCommand {

	/** Default command name for {@link ShowPreviousChangeCommand} */
	public static final String COMMAND_NAME = "showPreviousChange";

	/** Default image for the command opening a side by side compare view. */
	static final String COMMAND_IMAGE_KEY = "/misc/compare/showPreviousChange.png";

	/**
	 * Configuration of {@link SideBySideCompareCommand}.
	 */
	public interface Config extends ShowChangeCommand.Config {

		@Override
		@FormattedDefault(COMMAND_IMAGE_KEY)
		ThemeImage getImage();

	}

	/**
	 * Create a new {@link ShowPreviousChangeCommand}.
	 */
	public ShowPreviousChangeCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void selectFollowingChange(CompareTreeTableComponent compareComponent) {
		compareComponent.selectPreviousChange();
	}

	@Override
	protected ResKey getDefaultI18NKey() {
		return I18NConstants.SHOW_PREVIOUS_CHANGE;
	}
}
