/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.Date;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnFilterFieldFactory;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Test for the {@link ColumnFilterFieldFactory}: a bound of a column is entered as a form field of
 * the column's type, a value shown as a cell of the column shows it.
 */
public class TestColumnFilterFieldFactory extends AbstractModelAccessTest {

	private static final String NUMBER = "number";

	private static final String DAY = "day";

	private static final String PROJECTS = "projects";

	private static final String UNKNOWN = "unknown";

	/** The cells are read by a user who may read every value. */
	@Override
	protected Person initialUser() {
		return _root;
	}

	private ColumnFilterFieldFactory factory() {
		return new ColumnFilterFieldFactory(Map.of(
			NUMBER, ColumnType.of(TLModelPartRef.ref("tl.core:Integer").resolveType(), false),
			DAY, ColumnType.of(TLModelPartRef.ref("tl.core:Date").resolveType(), false),
			PROJECTS, ColumnType.of(type(PROJECT), true),
			UNKNOWN, ColumnType.UNRESOLVED));
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/** A number is entered in a number field, a date with a date picker. */
	public void testValueInputs() {
		ColumnFilterFieldFactory factory = factory();
		ReactControl number = factory.valueInput(context(), NUMBER, new AbstractFieldModel(Long.valueOf(3)));
		assertTrue("Got: " + number, number instanceof ReactNumberInputControl);
		ReactControl day = factory.valueInput(context(), DAY, new AbstractFieldModel(new Date()));
		assertTrue("Got: " + day, day instanceof ReactDatePickerControl);
	}

	/**
	 * An object is displayed as a cell shows it; a point in time, a truth value and an unknown column
	 * by label.
	 */
	public void testValueDisplays() {
		ColumnFilterFieldFactory factory = factory();
		assertNotNull(factory.valueDisplay(context(), PROJECTS, _project));
		assertNull("A day is shown by its label.", factory.valueDisplay(context(), DAY, new Date()));
		assertNull("A truth value is shown by its label, not as a second checkbox.",
			factory.valueDisplay(context(), PROJECTS, Boolean.TRUE));
		assertNull(factory.valueDisplay(context(), UNKNOWN, "x"));
		assertNull(factory.valueInput(context(), UNKNOWN, new AbstractFieldModel("x")));
		assertNull("A column the factory does not know.", factory.valueDisplay(context(), "other", "x"));
	}

	/**
	 * The test suite, additionally starting the services creating the controls.
	 */
	public static Test suite() {
		return suiteWith(TestColumnFilterFieldFactory.class, TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE,
			FieldControlService.Module.INSTANCE, ColumnProviderService.Module.INSTANCE);
	}

}
