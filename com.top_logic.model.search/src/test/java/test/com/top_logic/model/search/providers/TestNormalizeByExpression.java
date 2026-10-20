/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLValueNormalizer;
import com.top_logic.model.annotate.util.ValueNormalizer;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.providers.NormalizeByExpression;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link NormalizeByExpression} configured in a {@link TLValueNormalizer} annotation.
 */
@SuppressWarnings("javadoc")
public class TestNormalizeByExpression extends AbstractSearchExpressionTest {

	private static final String TYPE = "TestSearchExpression:A";

	private static final String NAME = "name";

	private static final String STR = "str";

	public void testTrim() throws Exception {
		ValueNormalizer normalizer = normalizer("value -> obj -> $value.trim()");
		TLObject a = newA("prefix");

		assertEquals("text", normalizer.normalize(a, part(NAME), "  text \t"));
	}

	public void testAccessObject() throws Exception {
		ValueNormalizer normalizer =
			normalizer("value -> obj -> toString($obj.get(`TestSearchExpression:A#str`), ':', $value.trim())");
		TLObject a = newA("prefix");

		assertEquals("prefix:text", normalizer.normalize(a, part(NAME), " text "));
	}

	public void testWithoutObjectAndAttribute() throws Exception {
		ValueNormalizer normalizer =
			normalizer("value -> obj -> if($obj == null, $value.trim(), 'unexpected')");

		assertEquals("text", normalizer.normalize(null, null, " text "));
	}

	public void testNullResult() throws Exception {
		ValueNormalizer normalizer = normalizer("value -> obj -> null");

		assertNull(normalizer.normalize(null, null, "text"));
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

	private static ValueNormalizer normalizer(String function) throws Exception {
		NormalizeByExpression.Config<?> config =
			TypedConfiguration.newConfigItem(NormalizeByExpression.Config.class);
		TypedConfigUtil.setProperty(config, NormalizeByExpression.Config.FUNCTION, parse(function));

		TLValueNormalizer annotation = TypedConfiguration.newConfigItem(TLValueNormalizer.class);
		annotation.setNormalizer(config);

		return TypedConfigUtil.createInstance(annotation.getNormalizer());
	}

	public static Test suite() {
		return suite(TestNormalizeByExpression.class);
	}

}
