/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.normalize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.model.annotate.AnnotationContainer;
import com.top_logic.model.annotate.TLValueNormalizer;
import com.top_logic.model.annotate.util.ValueNormalizer;
import com.top_logic.model.annotate.util.ValueNormalizerChain;
import com.top_logic.model.normalize.CollapseWhitespaceNormalizer;
import com.top_logic.model.normalize.EmptyToNullNormalizer;
import com.top_logic.model.normalize.LowerCaseNormalizer;
import com.top_logic.model.normalize.TrimNormalizer;
import com.top_logic.model.normalize.UpperCaseNormalizer;

/**
 * Test case for the built-in {@link ValueNormalizer}s and their combination in a
 * {@link TLValueNormalizer} annotation.
 */
@SuppressWarnings("javadoc")
public class TestBuiltinNormalizers extends TestCase {

	private static final Object NON_TEXT = Long.valueOf(42);

	public void testTrim() {
		ValueNormalizer trim = normalizer(TrimNormalizer.Config.class);

		assertEquals("a  b", trim.normalize(null, null, " \t a  b \n"));
		assertNormalizesElements(trim, List.of(" a", "b "), List.of("a", "b"));
		assertKeepsNonText(trim);
	}

	public void testCollapseWhitespace() {
		ValueNormalizer collapse = normalizer(CollapseWhitespaceNormalizer.Config.class);

		assertEquals("Hello World !", collapse.normalize(null, null, "  Hello \t\n  World   ! "));
		assertEquals("", collapse.normalize(null, null, "   "));
		assertNormalizesElements(collapse, List.of(" a  b", "c"), List.of("a b", "c"));
		assertKeepsNonText(collapse);
	}

	public void testLowerCase() {
		ValueNormalizer lowerCase = normalizer(LowerCaseNormalizer.Config.class);

		assertEquals(" title ", lowerCase.normalize(null, null, " TiTLE "));
		assertNormalizesElements(lowerCase, List.of("A", "b"), List.of("a", "b"));
		assertKeepsNonText(lowerCase);
	}

	public void testLowerCaseIsIndependentOfLanguage() {
		ValueNormalizer lowerCase = normalizer(LowerCaseNormalizer.Config.class);

		assertEquals("Not the Turkish dotless i.", "title", lowerCase.normalize(null, null, "TITLE"));
	}

	public void testUpperCase() {
		ValueNormalizer upperCase = normalizer(UpperCaseNormalizer.Config.class);

		assertEquals(" TITLE ", upperCase.normalize(null, null, " tItle "));
		assertNormalizesElements(upperCase, List.of("a", "B"), List.of("A", "B"));
		assertKeepsNonText(upperCase);
	}

	public void testEmptyToNull() {
		ValueNormalizer emptyToNull = normalizer(EmptyToNullNormalizer.Config.class);

		assertNull(emptyToNull.normalize(null, null, ""));
		assertEquals("A blank text is not empty.", " ", emptyToNull.normalize(null, null, " "));
		assertEquals("a", emptyToNull.normalize(null, null, "a"));
		assertNormalizesElements(emptyToNull, List.of("a", ""), Arrays.asList("a", null));
		assertKeepsNonText(emptyToNull);
	}

	public void testSetStaysSet() {
		ValueNormalizer lowerCase = normalizer(LowerCaseNormalizer.Config.class);

		Object result = lowerCase.normalize(null, null, new LinkedHashSet<>(List.of("B", "A", "b")));

		assertTrue(result instanceof Set);
		assertEquals("Equal normalized elements are merged, the order is kept.", List.of("b", "a"),
			new ArrayList<>((Set<?>) result));
	}

	public void testChainAppliesInOrder() {
		ValueNormalizer trimLowerCase = new ValueNormalizerChain(
			List.of(normalizer(TrimNormalizer.Config.class), normalizer(LowerCaseNormalizer.Config.class)));
		assertEquals("abc", trimLowerCase.normalize(null, null, "  ABC "));

		ValueNormalizer trimEmptyToNull = new ValueNormalizerChain(
			List.of(normalizer(TrimNormalizer.Config.class), normalizer(EmptyToNullNormalizer.Config.class)));
		assertNull(trimEmptyToNull.normalize(null, null, "   "));

		ValueNormalizer emptyToNullTrim = new ValueNormalizerChain(
			List.of(normalizer(EmptyToNullNormalizer.Config.class), normalizer(TrimNormalizer.Config.class)));
		assertEquals("The order matters.", "", emptyToNullTrim.normalize(null, null, "   "));
	}

	public void testAnnotatedChain() {
		TLValueNormalizer annotation = TypedConfiguration.newConfigItem(TLValueNormalizer.class);
		annotation.getNormalizers().add(TypedConfiguration.newConfigItem(TrimNormalizer.Config.class));
		annotation.getNormalizers().add(TypedConfiguration.newConfigItem(EmptyToNullNormalizer.Config.class));

		AnnotationContainer annotations = AnnotationContainer.EMPTY.with(annotation);
		assertTrue(ValueNormalizer.isAnnotated(annotations));
		ValueNormalizer normalizer = ValueNormalizer.annotated(annotations);

		assertTrue(normalizer instanceof ValueNormalizerChain);
		assertNull(normalizer.normalize(null, null, "   "));
		assertEquals("a", normalizer.normalize(null, null, " a "));
	}

	public void testAnnotatedSingle() {
		TLValueNormalizer annotation = TypedConfiguration.newConfigItem(TLValueNormalizer.class);
		annotation.getNormalizers().add(TypedConfiguration.newConfigItem(UpperCaseNormalizer.Config.class));

		ValueNormalizer normalizer = ValueNormalizer.annotated(AnnotationContainer.EMPTY.with(annotation));

		assertTrue(normalizer instanceof UpperCaseNormalizer);
	}

	public void testNotAnnotated() {
		assertFalse(ValueNormalizer.isAnnotated(AnnotationContainer.EMPTY));
		assertNull(ValueNormalizer.annotated(AnnotationContainer.EMPTY));

		AnnotationContainer emptyAnnotation =
			AnnotationContainer.EMPTY.with(TypedConfiguration.newConfigItem(TLValueNormalizer.class));
		assertFalse(ValueNormalizer.isAnnotated(emptyAnnotation));
		assertNull(ValueNormalizer.annotated(emptyAnnotation));
	}

	private static void assertKeepsNonText(ValueNormalizer normalizer) {
		assertNull(normalizer.normalize(null, null, null));
		assertSame(NON_TEXT, normalizer.normalize(null, null, NON_TEXT));
	}

	private static void assertNormalizesElements(ValueNormalizer normalizer, List<String> values,
			List<String> expected) {
		Object result = normalizer.normalize(null, null, values);
		assertTrue(result instanceof List);
		assertEquals(expected, result);
	}

	@SuppressWarnings("rawtypes")
	private static ValueNormalizer normalizer(Class<? extends PolymorphicConfiguration> configType) {
		PolymorphicConfiguration<?> config = TypedConfiguration.newConfigItem(configType);
		return (ValueNormalizer) TypedConfigUtil.createInstance(config);
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestBuiltinNormalizers.class, TypeIndex.Module.INSTANCE));
	}

}
