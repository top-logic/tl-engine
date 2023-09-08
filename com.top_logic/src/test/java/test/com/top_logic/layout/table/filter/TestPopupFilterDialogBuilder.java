/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.Optional;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.Icons;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.filter.PopupFilterDialogBuilder;
import com.top_logic.layout.table.filter.TableFilterModel;

/**
 * {@link TestCase} for {@link TableFilterModel}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestPopupFilterDialogBuilder extends BasicTestCase {

	public void testInversionOptionCreated() throws Exception {
		assertInversionFieldCreated(true);
	}

	public void testNoInversionOptionCreated() throws Exception {
		assertInversionFieldCreated(false);
	}

	private void assertInversionFieldCreated(boolean showInversionOption) {
		TableFilterModel filterModel = createTableFilterModel(showInversionOption);
		PopupFilterDialogBuilder filterDialogBuilder = createFilterDialogBuilder();
		FormContext filterContext = new FormContext("testTable", ResPrefix.GLOBAL);
		filterDialogBuilder.createFilterDialogContent(filterModel, DummyDisplayContext.newInstance(), filterContext,
			Optional.empty());
		assertEquals(showInversionOption,
			filterContext.getFirstMemberRecursively(PopupFilterDialogBuilder.INVERT_FIELD) != null);
	}


	private TableFilterModel createTableFilterModel(boolean showInversionOption) {
		TableFilterModel filterModel =
			new TableFilterModel(Collections.singletonList(new DummyConfiguredFilter()),
				true, showInversionOption);
		return filterModel;
	}

	private PopupFilterDialogBuilder createFilterDialogBuilder() {
		return new PopupFilterDialogBuilder(
			new DefaultPopupDialogModel(
				new DefaultLayoutData(ThemeFactory.getTheme().getValue(Icons.FILTER_DIALOG_WIDTH),
					DisplayUnit.PIXEL, 100, 0, DisplayUnit.PIXEL, 100, Scrolling.AUTO)));
	}

	public static Test suite() {
		return (TLTestSetup.createTLTestSetup(new TestSuite(TestPopupFilterDialogBuilder.class)));
	}
}
