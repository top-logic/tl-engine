/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * Tests that a {@link RowSetTableControl} tells the client which of its columns require a value
 * while the table is edited: a column of an attribute that every row type requires is marked, a
 * column of an attribute that only some row types require is not.
 */
public class TestRowSetTableRequired extends AbstractModelAccessTest {

	/** Matches the state of the column with the given name, which holds no nested objects. */
	private static Pattern columnState(String name) {
		return Pattern.compile("\\{[^{}]*\"name\":\"" + name + "\"[^{}]*\\}");
	}

	/** The state of the name column of the table in the given form, with the given row types. */
	private String nameColumn(FormControl form, boolean edit, List<String> rowTypes) {
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(form.getReactContext()), form,
			new AttributeRowSetBinding(ITEMS), List.of(), RowEditPolicy.ALL);
		table.setRowTypes(rowTypes.stream().<TLStructuredType> map(AbstractModelAccessTest::type).toList());
		table.init();
		if (edit) {
			assertTrue(form.enterEditMode());
		}
		String json = table.getTableControl().stateAsJSON();
		Matcher name = columnState(NAME).matcher(json);
		assertTrue("The name column is displayed: " + json, name.find());
		return name.group();
	}

	private FormControl itemsForm() {
		TLObject step = newPart(_project, STEPS, STEP);
		newPart(step, ITEMS, ITEM);
		becomeUser(_root);
		ReactContext reactContext =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		return new FormControl(reactContext, step, "no model", NoTokenHandling.INSTANCE);
	}

	/** The mandatory name of an item is marked at the heading of the edited table. */
	public void testMandatoryAttributeIsRequired() {
		String name = nameColumn(itemsForm(), true, List.of(ITEM));
		assertTrue(name, name.contains("\"required\":true"));
	}

	/** A table that is not edited marks no column. */
	public void testNothingRequiredWithoutEditing() {
		String name = nameColumn(itemsForm(), false, List.of(ITEM));
		assertFalse(name, name.contains("\"required\""));
	}

	/** An attribute that only one of the row types requires is not marked at the heading. */
	public void testRequiredByOneRowTypeOnlyIsNotMarked() {
		String name = nameColumn(itemsForm(), true, List.of(STEP, ITEM));
		assertFalse(name, name.contains("\"required\""));
	}

	/** The test suite. */
	public static Test suite() {
		return suiteWith(TestRowSetTableRequired.class, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, FieldControlService.Module.INSTANCE);
	}

}
