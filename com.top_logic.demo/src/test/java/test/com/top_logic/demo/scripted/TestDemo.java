/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

import com.top_logic.basic.ArrayUtil;

/**
 * Scripted tests for the Demo application.
 * <br/>
 * They are used to tests whether the features provided by the framework work correctly.
 * For example tables, trees, fields etc.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestDemo {

	public static Test suite() {
		List<String> testCases = new ArrayList<>();
		Collections.addAll(testCases,
			"00_VisitAllTabs",
			"01_EnsureAllTabsAreVisible",
			"02_CreateNewElements",
			"04_ProgrammaticDialog",
			"05_SortingTable",
			"06_SortingDocumentColumn",
			"07_AddToClipboardHandler",
			"09_CreateComment",
			"10_CreateOrganizations",
			"11_AddPersonToCompany",
			"12_AssertsOnFormFields",
			"13_TestControlsForm",
			"14_CreateObjectWithDerivedAttributes",
			"15_TestEditingFieldsWithInconsistentValues",
			"15a_TestUnicodeCharacters",
			"16_CheckPreventRootDeletion",
			"17_AssertsOnFormFieldsInStructureEditor",
			"20_CreateTableAssertionTestElements",
			"21_AssertsOnValuesInSimpleTables",
			"22_AssertsOnValuesInFrozenTables",
			"23_NumberFields",
			"24_DateTimeField",
			"31_CountryNamingScheme",
			"33_TreeSelectionExpansion",
			"50_GridTestDataCreation",
			"51_GridSimpleInitialSelection",
			"52_GridSimpleEditAndAssert",
			"52a_GridEditMode",
			"52b_DeleteInGridAndEditorViewMode",
			"53_TestGridConcurrent",
			"54_GridSelectionBoxes",
			"55_SearchResultReport",
			"61_LeaveEditModeForNullModel",
			"62_TestSwitchToMaintainanceMode",
			"70_ListMetaAttribute",
			"71_DuplicateFastListName",
			"72_DerivedAttributePathNavigationInHistory",
			"73_ShowCurrentVersion",
			"74_ReverseInlineReference",
			"75_DefaultFloatFormat",
			"80_Regression_FastList",
			"81_Regression_Ticket_9708",
			"82_Regression_Ticket_9708_RawValue",
			"83_Regression_Ticket_10405",
			"84_Regression_Ticket_9812",
			"85_Regression_Ticket_10427",
			"86_Regression_Ticket_9815",
			"87_Regression_Ticket_13026_Setup",
			"88_Regression_Ticket_13026",
			"89_Regression_Ticket_13020",
			"8A_KnowledgeBaseRevert",
			"8B_Regression_Ticket_19065",
			"8C_Regression_Ticket_18728",
			"8D_Regression_Ticket_20622_00",
			"8D_Regression_Ticket_20622_01",
			"8D_Regression_Ticket_20622_02",
			"90_TableFilter",
			"91_TableFilter-Sidebar",
			"92_TreeFilterOptions",
			"93_StatementMonitor",
			"94_ComparableTableFilter",
			"95_TreeTableFiltering_Setup",
			"95_TreeTableFiltering_Test",
			"95_TreeTableFiltering_Teardown",
			"A0_10904_TreeTable",
			"A1_FuzzyGotoActionOp",
			"A2_FuzzyFormMemberNaming",
			"A3_WarningsUponApply",
			"A4_RolesProfileImport",
			"A5_ThirdPartyLibraries",
			"B0_ImageGallery"
			/* Takes long and is only useful for profiling: "FE_Create1000Elements" */
			/* Changes the file 'Messages.properties': "FF_changeI18N", */
		);
		addSidebarLayoutTests(testCases);
		TestSuite theTestSuite =
			XmlScriptedTestUtil.suite(TestDemo.class, testCases.toArray(ArrayUtil.EMPTY_STRING_ARRAY));
		return DemoSetup.createDemoSetup(theTestSuite);
	}

	private static void addSidebarLayoutTests(List<String> testCases) {
		Collections.addAll(testCases,
			"C0_Sidebar-Layout-Setup",
			"00_VisitAllTabs",
			"C0_Sidebar-Layout-Teardown"
		);
	}

}
