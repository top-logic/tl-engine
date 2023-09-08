/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.messagebox.SimpleSelectDialog;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link MultipleSettingsCommand} loading the setting formerly stored under a given name.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LoadNamedSetting extends MultipleSettingsCommand {

	private final Consumer<ConfigKey> _configLoader;

	private ResPrefix _selectDialogPrefix = I18NConstants.SELECT_NAMED_TABLE_CONFIGURATION;

	/**
	 * Creates a new {@link LoadNamedSetting}.
	 */
	public LoadNamedSetting(ConfigKey configKey, Consumer<ConfigKey> configLoader) {
		super(configKey);
		_configLoader = configLoader;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		Map<String, List<String>> storedConfigKeys = storedConfigKeys();
		List<String> keys;
		List<String> names;
		if (storedConfigKeys == null) {
			keys = new ArrayList<>();
			names = new ArrayList<>();
		} else {
			keys = storedConfigKeys.get(KEYS_KEY);
			names = storedConfigKeys.get(NAMES_KEY);
		}

		SimpleSelectDialog<String> selectDialog = new SimpleSelectDialog<>(_selectDialogPrefix, keys);
		selectDialog.setLabels(key -> names.get(keys.indexOf(key)));
		selectDialog.setSelectionHandler(this::handleSelectionChanged);
		return selectDialog.open(context);
	}

	private HandlerResult handleSelectionChanged(@SuppressWarnings("unused") DisplayContext context, String configKey) {
		_configLoader.accept(ConfigKey.named(configKey));
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Sets the prefix for the select dialog.
	 */
	public void setSelectDialogPrefix(ResPrefix selectDialogPrefix) {
		_selectDialogPrefix = selectDialogPrefix;

	}

}
