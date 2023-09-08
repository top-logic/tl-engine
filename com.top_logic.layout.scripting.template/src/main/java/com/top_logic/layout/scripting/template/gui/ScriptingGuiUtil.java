/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionReader;
import com.top_logic.layout.scripting.template.excel.ExcelChecker;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelFileConfig.BasicSheetSet;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelSheetConfig;

/**
 * Utilities of the ScriptingGui.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptingGuiUtil {

	/**
	 * Parses the {@link ApplicationAction}s in the given {@link File}.
	 */
	public static ScriptContainer toScriptContainer(File script) {
		ApplicationAction actions = parse(BinaryDataFactory.createBinaryData(script));
		if (script.getName().endsWith(ActionReader.FILE_ENDING)) {
			return ScriptContainer.createPersistable(actions, script);
		} else {
			return ScriptContainer.createTransient(actions);
		}
	}

	/**
	 * Parses the {@link ApplicationAction}s in the given {@link BinaryData}.
	 */
	public static ScriptContainer toScriptContainer(BinaryData script) {
		return ScriptContainer.createTransient(parse(script));
	}

	private static ApplicationAction parse(BinaryData script) {
		if (script.getName().endsWith(ActionReader.FILE_ENDING)) {
			return parseActions(script);
		}
		if (ExcelChecker.supportsFile(script)) {
			return parseExcel(script);
		}
		throw new IllegalArgumentException("Unknown file typ: " + script.getName());
	}

	private static ApplicationAction parseActions(BinaryData binaryData) {
		try {
			return ActionReader.INSTANCE.readAction(binaryData);
		} catch (ConfigurationException ex) {
			String message = "Failed to parse the actions in the given file: " + ex.getMessage();
			throw new RuntimeException(message, ex);
		}
	}

	private static ApplicationAction parseExcel(BinaryData binaryData) {
		List<ExcelSheetConfig> sheetConfigs = Collections.<ExcelSheetConfig> emptyList();
		String excelName = binaryData.getName();
		return new ExcelChecker(excelName, binaryData, BasicSheetSet.FILE_DEFINED, sheetConfigs).get();
	}

}
