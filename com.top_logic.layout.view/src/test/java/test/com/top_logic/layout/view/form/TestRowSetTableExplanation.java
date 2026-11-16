/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.LabelDescription;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.util.TLContextManager;

/**
 * Tests the explanation a {@link RowSetTableControl} over an attribute offers: the
 * {@link LabelDescription description} of the attribute, at the title of a framed table and
 * beside a frameless one, as help text in edit mode, and none for an attribute without
 * description.
 */
public class TestRowSetTableExplanation extends AbstractModelAccessTest {

	private ReactContext _reactContext;

	private FormControl _form;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_form = new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
	}

	@Override
	protected void tearDown() throws Exception {
		_form = null;
		_reactContext = null;
		super.tearDown();
	}

	@Override
	protected Person initialUser() {
		return _responsible;
	}

	/**
	 * A framed table over an attribute with description offers it at its title, and as help text
	 * in edit mode only.
	 */
	public void testFramedTable() {
		RowSetTableControl table = table(TASKS, true);
		String description = tasksDescription();
		assertEquals(description, state(table).get(ReactPanelControl.EXPLANATION));
		assertEquals(Boolean.FALSE, state(table).get(ReactPanelControl.EXPLANATION_HELP));
		assertNotNull("The framed table shows the attribute as title.", state(table).get("title"));

		assertTrue(_form.enterEditMode());
		assertEquals(description, state(table).get(ReactPanelControl.EXPLANATION));
		assertEquals(Boolean.TRUE, state(table).get(ReactPanelControl.EXPLANATION_HELP));
	}

	/**
	 * A frameless table over an attribute with description has no title and offers the
	 * description all the same.
	 */
	public void testFramelessTable() {
		RowSetTableControl table = table(TASKS, false);
		assertNull("A frameless table shows no title.", state(table).get("title"));
		assertEquals(tasksDescription(), state(table).get(ReactPanelControl.EXPLANATION));

		assertTrue(_form.enterEditMode());
		assertEquals(Boolean.TRUE, state(table).get(ReactPanelControl.EXPLANATION_HELP));
	}

	/** A table over an attribute without description offers no explanation, not even in edit mode. */
	public void testAttributeWithoutDescription() {
		RowSetTableControl table = table(STEPS, true);
		assertNull(state(table).get(ReactPanelControl.EXPLANATION));

		assertTrue(_form.enterEditMode());
		assertNull(state(table).get(ReactPanelControl.EXPLANATION));
		assertEquals(Boolean.FALSE, state(table).get(ReactPanelControl.EXPLANATION_HELP));
	}

	/** The description of the attribute {@value #TASKS}, as the resources of the test define it. */
	private static String tasksDescription() {
		String result = LabelDescription.description(TLModelI18N.getI18NKey(type(PROJECT).getPart(TASKS)));
		assertNotNull("The test resources describe the attribute " + TASKS + ".", result);
		return result;
	}

	private RowSetTableControl table(String attribute, boolean framed) {
		return TLContextManager.inPersonContext(_responsible, () -> {
			RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(_reactContext), _form,
				new AttributeRowSetBinding(attribute), List.of(), RowEditPolicy.ALL);
			table.setFramed(framed);
			table.init();
			return table;
		});
	}

	/** The state of the given control as the client receives it. */
	private static Map<?, ?> state(RowSetTableControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its data columns and their cells, and the scripting the columns compute with.
	 */
	public static Test suite() {
		return suiteWith(TestRowSetTableExplanation.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
