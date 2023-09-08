/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.template;

import java.io.File;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.util.TestCollector;
import test.com.top_logic.layout.scripting.ApplicationTestSetup;
import test.com.top_logic.layout.scripting.ScriptedTest;
import test.com.top_logic.layout.scripting.ScriptedTest.ScriptedTestParameters;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.scripting.template.excel.ExcelChecker;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelFileConfig.BasicSheetSet;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelSheetConfig;

/**
 * {@link TestCollector} for modules containing scripted tests.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScriptedTestCollector implements TestCollector {

	@Override
	public Test getTestsForFile(File targetFile) {
		if (isExcelFile(targetFile)) {
			BinaryData fileLocation = BinaryDataFactory.createBinaryData(targetFile);
			BasicSheetSet basicSheetSet = BasicSheetSet.FILE_DEFINED;
			List<ExcelSheetConfig> sheetConfigs = Collections.emptyList();
			ExcelChecker excelChecker =
				new ExcelChecker(targetFile.getName(), fileLocation, basicSheetSet, sheetConfigs);

			final ScriptedTest scriptedTest =
				new ScriptedTest(excelChecker, excelChecker.getName(), new ScriptedTestParameters());

			return ApplicationTestSetup.setupApplication(scriptedTest);
		}
		return null;
	}

	private boolean isExcelFile(File targetFile) {
		return targetFile.getName().endsWith(".xlsx");
	}

	@Override
	public void addTestForDirectory(TestSuite suite, File testDir, boolean recursive) {
		// No special tests here.
	}

	@Override
	public void addModuleIndependentTests(TestSuite suite) {
		// No module independent tests here.
	}

}
