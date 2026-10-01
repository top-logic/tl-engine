/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.providers.ModeSelectorByExpression;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link ModeSelectorByExpression}: the mode it computes and the attributes it reports
 * that mode to depend on.
 */
@SuppressWarnings("javadoc")
public class TestModeSelectorByExpression extends AbstractSearchExpressionTest {

	private static final String TYPE = "TestSearchExpression:A";

	private static final String NAME = "name";

	private static final String STR = "str";

	private static final String INT = "int";

	public void testTwoArgumentsTraceDependencies() throws Exception {
		ModeSelector selector = selector(
			"a -> editMode -> if($a.get(`TestSearchExpression:A#str`) == 'locked', 'disabled', 'default')");
		TLObject a = newA("locked");

		assertEquals(FormVisibility.DISABLED, selector.getMode(a, part(NAME), true));
		assertEquals(Set.of(part(STR)), dependencies(selector, a, true));
	}

	public void testTwoArgumentsTraceWithEditMode() throws Exception {
		ModeSelector selector = selector(
			"a -> editMode -> if($editMode, $a.get(`TestSearchExpression:A#str`), $a.get(`TestSearchExpression:A#int`)) != null");
		TLObject a = newA("value");

		assertEquals(Set.of(part(STR)), dependencies(selector, a, true));
		assertEquals(Set.of(part(INT)), dependencies(selector, a, false));
	}

	public void testOneArgument() throws Exception {
		ModeSelector selector =
			selector("a -> if($a.get(`TestSearchExpression:A#str`) == 'locked', 'disabled', 'default')");
		TLObject a = newA("open");

		assertEquals(FormVisibility.DEFAULT, selector.getMode(a, part(NAME), true));
		assertEquals(Set.of(part(STR)), dependencies(selector, a, true));
		assertEquals(Set.of(part(STR)), dependencies(selector, a, false));
	}

	private static Set<TLStructuredTypePart> dependencies(ModeSelector selector, TLObject object, boolean editMode) {
		Set<TLStructuredTypePart> result = new HashSet<>();
		selector.traceDependencies(object, part(NAME), editMode, pointer -> result.add(pointer.attribute()),
			null);
		return result;
	}

	private static TLObject newA(String str) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(type());
		result.tUpdateByName(STR, str);
		return result;
	}

	private static TLStructuredTypePart part(String name) {
		return type().getPart(name);
	}

	private static TLClass type() {
		return (TLClass) TLModelUtil.findType(TYPE);
	}

	private static ModeSelector selector(String function) throws Exception {
		ModeSelectorByExpression.Config<?> config =
			TypedConfiguration.newConfigItem(ModeSelectorByExpression.Config.class);
		TypedConfigUtil.setProperty(config, ModeSelectorByExpression.Config.FUNCTION, parse(function));
		return TypedConfigUtil.createInstance(config);
	}

	public static Test suite() {
		return suite(TestModeSelectorByExpression.class);
	}

}
