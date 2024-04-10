/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import static com.top_logic.basic.util.ResKey.*;
import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.I18NBundleSPI;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.LangString;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.util.ResourcesModule.Config;

/**
 * Test case for {@link ResKey} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestResKey extends TestCase {

	enum SomeEnum {
		A, B, C
	}

	private BundleForTest _bundle;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		Map<String, String> fallbackMap = new HashMap<>();
		fallbackMap.put("message1.existing", "Message1({0})");
		fallbackMap.put("message1.existing.suffix", "MessageSuffix1({0})");
		fallbackMap.put("message2.existing", "Message2({0}, {1})");
		fallbackMap.put("test.com.top_logic.basic.util.TestResKey.SomeEnum.A", "First choice");
		fallbackMap.put("test.com.top_logic.basic.util.TestResKey.SomeEnum.B", "Second choice");
		fallbackMap.put("test.com.top_logic.basic.util.TestResKey.SomeEnum.C", "Third choice");
		fallbackMap.put("testFallbackNesting.1.1", "Translation of: testFallbackNesting.1.1");
		fallbackMap.put("testFallbackNesting.1.2", "Translation of: testFallbackNesting.1.2");
		fallbackMap.put("testFallbackNesting.1.3", "Translation of: testFallbackNesting.1.3");
		fallbackMap.put("testFallbackNesting.2.2", "Translation of: testFallbackNesting.2.2");
		BundleForTest fallbackBundle = new BundleForTest(fallbackMap, Locale.CHINESE);

		Map<String, String> map = new HashMap<>();
		map.put("foo.bar.existing", "FooBarExisting");
		map.put("foo.bar.deprecated.existing", "FooBarDeprecatedExisting");
		map.put("testFallbackNesting.2.3", "Translation of: testFallbackNesting.2.3");
		map.put("testFallbackNesting.3.3", "Translation of: testFallbackNesting.3.3");
		map.put("testFallbackWithFallbackSuffix.2.suffix.2", "Translation of: testFallbackWithFallbackSuffix.2.suffix.2");
		map.put("testFallbackWithFallbackSuffix.3.suffix.2", "Translation of: testFallbackWithFallbackSuffix.3.suffix.2");
		map.put("testFallbackWithFallbackSuffix.1.suffix.3", "Translation of: testFallbackWithFallbackSuffix.1.suffix.3");
		map.put("testFallbackWithFallbackSuffix.2.suffix.3", "Translation of: testFallbackWithFallbackSuffix.2.suffix.3");
		map.put("testFallbackWithFallbackSuffix.3.suffix.3", "Translation of: testFallbackWithFallbackSuffix.3.suffix.3");
		map.put("testSuffixOfLiteralTextWithFallback.1.suffix.2", "Translation of: testSuffixOfLiteralTextWithFallback.1.suffix.2");
		map.put("testTooltip.keyWithTooltip", "Translation of: testTooltip.keyWithTooltip");
		map.put("testTooltip.keyWithTooltip.tooltip", "Translation of: testTooltip.keyWithTooltip.tooltip");
		map.put("testTooltip.keyWithoutTooltip", "Translation of: testTooltip.keyWithoutTooltip");

		_bundle = new BundleForTest(map, Locale.ENGLISH, fallbackBundle);
	}

	public void testResolve() {
		assertResolve("FooBarExisting",
			ResKey.internalCreate("foo.bar.existing"));
		assertResolve("[foo.bar.missing]",
			ResKey.internalCreate("foo.bar.missing"));

		assertResolve("FooBarExisting",
			ResKey.fallback(ResKey.internalCreate("foo.bar.existing"),
				ResKey.deprecated(ResKey.internalCreate("foo.bar.deprecated.existing"))));
		_bundle.assertNoDeprecations();

		assertResolve("FooBarDeprecatedExisting",
			ResKey.deprecated(ResKey.internalCreate("foo.bar.deprecated.existing")));
		_bundle.assertDeprecations("foo.bar.deprecated.existing");

		assertResolve("[foo.bar.missing]",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"),
				ResKey.deprecated(ResKey.internalCreate("foo.bar.deprecated.missing"))));
		_bundle.assertNoDeprecations();

		assertResolve("FooBarDeprecatedExisting",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"),
				ResKey.fallback(
					ResKey.deprecated(ResKey.internalCreate("foo.bar.deprecated.missing")),
					ResKey.deprecated(ResKey.internalCreate("foo.bar.deprecated.existing")))));
		_bundle.assertDeprecations("foo.bar.deprecated.existing");
		
		assertResolve("FooBarDeprecatedExisting",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"),
				ResKey.deprecated(
					ResKey.fallback(
						ResKey.internalCreate("foo.bar.deprecated.missing"),
						ResKey.internalCreate("foo.bar.deprecated.existing")))));
		_bundle.assertDeprecations("foo.bar.deprecated.existing");

		assertResolve("[deprecated:foo.bar.deprecated.missing]",
			ResKey.deprecated(ResKey.internalCreate("foo.bar.deprecated.missing")));
		_bundle.assertNoDeprecations();

		assertResolve("Literal Text",
			ResKey.text("Literal Text"));

		assertResolve("FooBarExisting",
			ResKey.message(ResKey.internalCreate("foo.bar.existing"), 42, "Literal Text"));
		assertResolve("Message1(42)",
			ResKey.message(ResKey.internalCreate("message1.existing"), 42, "Literal Text"));
		assertResolve("Message1(42)",
			ResKey.fallback(
				ResKey.message(ResKey.internalCreate("message1.existing"), 42, "Literal Text"),
				ResKey.message(ResKey.internalCreate("message2.existing"), 42, "Literal Text")));
		assertResolve("Message1(42)",
			ResKey.message(
				ResKey.fallback(
					ResKey.internalCreate("message1.existing"),
					ResKey.internalCreate("message2.existing")), 42, "Literal Text"));
		assertResolve("Message2(42, Literal Text)",
			ResKey.message(
				ResKey.fallback(
					ResKey.internalCreate("foo.bar.missing"),
					ResKey.internalCreate("message2.existing")), 42, "Literal Text"));
		assertResolve("Message2(42, Literal Text)",
			ResKey.message(
				ResKey.deprecated(
					ResKey.fallback(
						ResKey.internalCreate("foo.bar.missing"),
						ResKey.internalCreate("message2.existing"))), 42, "Literal Text"));
		assertResolve("Some text",
			ResKey.message(
				ResKey.fallback(
					ResKey.internalCreate("foo.bar.missing"),
					ResKey.text("Some text")), 42, "Literal Text"));
		assertResolve("[foo.bar.missing(42, \"Literal Text\")]",
			ResKey.message(
				ResKey.fallback(
					ResKey.internalCreate("foo.bar.missing"),
					ResKey.NONE), 42, "Literal Text"));
		assertResolve("Message2(42, Literal Text)",
			ResKey.message(ResKey.internalCreate("message2.existing"), 42, "Literal Text"));
		assertResolve("Message2(42, Literal Text)",
			ResKey.internalCreate("message2.existing").asResKey1().fill(42).asResKey1().fill("Literal Text"));
		assertResolve("[foo.bar.missing(42, \"Literal Text\")]",
			ResKey.message(ResKey.internalCreate("foo.bar.missing"), 42, "Literal Text"));

		assertResolve("FooBarExisting",
			ResKey.message(ResKey.internalCreate("foo.bar.existing"), "Literal Text"));
		assertResolve("[foo.bar.missing(\"Literal Text\")]",
			ResKey.message(ResKey.internalCreate("foo.bar.missing"), "Literal Text"));

		assertResolve("Literal Text",
			ResKey.message(ResKey.text("Literal Text"), "foo", "bar"));

		assertResolve("FooBarExisting",
			ResKey.fallback(ResKey.internalCreate("foo.bar.existing"), ResKey.internalCreate("foo.bar.missing")));
		assertResolve("FooBarExisting",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"), ResKey.internalCreate("foo.bar.existing")));
		assertResolve("[foo.bar.missing]",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"), ResKey.NONE));
		assertResolve("[foo.bar.missing]",
			ResKey.fallback(ResKey.NONE, ResKey.internalCreate("foo.bar.missing")));
		assertResolve("[deprecated:foo.bar.missing]",
			ResKey.fallback(ResKey.deprecated(ResKey.internalCreate("foo.bar.missing")), ResKey.NONE));
		assertResolve("[deprecated:foo.bar.missing]",
			ResKey.fallback(ResKey.NONE, ResKey.deprecated(ResKey.internalCreate("foo.bar.missing"))));
		assertResolve("Literal Text",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"), ResKey.text("Literal Text")));

		assertResolve("[none(some debug information).suffix]",
			ResKey.none("some debug information", "suffix"));
		assertResolve("[none(some debug information).suffix]",
			ResKey.none("some debug information", null).suffix(".suffix"));
		assertResolve("[none(some debug information).suffix.other]",
			ResKey.none("some debug information", "suffix").suffix(".other"));

		assertResolve("FooBarExisting",
			ResKey.fallback(ResKey.internalCreate("absurd.key"), ResKey.internalCreate("foo.bar")).suffix(".existing"));
	}

	public void testDeprecationSuffix() {
		assertResolve("FooBarDeprecatedExisting",
			ResKey.deprecated(ResKey.internalCreate("foo.bar.deprecated")).suffix("existing"));
		_bundle.assertDeprecations("foo.bar.deprecated.existing");
	}

	public void testNullDefault() {
		assertResolve(null,
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"), ResKey.text(null)));

		_bundle.assertNoUnknown();
	}

	public void testNoneFallback() {
		assertResolve("FooBarExisting",
			ResKey.fallback(ResKey.none("Obj", "prop"), ResKey.internalCreate("foo.bar.existing")));
		assertResolve("[foo.bar.missing]",
			ResKey.fallback(ResKey.none("Obj", "prop"), ResKey.internalCreate("foo.bar.missing")));
		assertResolve("FooBarExisting",
			ResKey.fallback(ResKey.internalCreate("foo.bar.existing"), ResKey.none("Obj", "prop")));
		assertResolve("[foo.bar.missing]",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"), ResKey.none("Obj", "prop")));
		_bundle.assertUnknown(ResKey.internalCreate("foo.bar.missing"));
	}

	public void testNoneNullFallback() {
		assertResolve("[none(Obj).prop]",
			ResKey.fallback(ResKey.none("Obj", "prop"), null));
		assertResolve("[none(Obj).prop]",
			ResKey.fallback(null, ResKey.none("Obj", "prop")));
		_bundle.assertUnknown(ResKey.none("Obj", "prop"));
	}

	public void testSuffixWithArguments() {
		Object[] arguments = { "Argument" };
		assertResolve("MessageSuffix1(Argument)",
			ResKey.message(ResKey.internalCreate("message1.existing"), arguments).suffix("suffix"));
	}

	public void testFallbackNesting_1() {
		ResKey key1 = ResKey.internalCreate("testFallbackNesting.1.1");
		ResKey key2 = ResKey.internalCreate("testFallbackNesting.1.2");
		ResKey key3 = ResKey.internalCreate("testFallbackNesting.1.3");
		assertResolve("Translation of: testFallbackNesting.1.1", key1.fallback(key2).fallback(key3));
		assertResolve("Translation of: testFallbackNesting.1.1", key1.fallback(key2.fallback(key3)));
	}

	public void testFallbackNesting_2() {
		ResKey key1 = ResKey.internalCreate("testFallbackNesting.2.1");
		ResKey key2 = ResKey.internalCreate("testFallbackNesting.2.2");
		ResKey key3 = ResKey.internalCreate("testFallbackNesting.2.3");
		assertResolve("Translation of: testFallbackNesting.2.2", key1.fallback(key2).fallback(key3));
		assertResolve("Translation of: testFallbackNesting.2.2", key1.fallback(key2.fallback(key3)));
	}

	public void testFallbackNesting_3() {
		ResKey key1 = ResKey.internalCreate("testFallbackNesting.3.1");
		ResKey key2 = ResKey.internalCreate("testFallbackNesting.3.2");
		ResKey key3 = ResKey.internalCreate("testFallbackNesting.3.3");
		assertResolve("Translation of: testFallbackNesting.3.3", key1.fallback(key2).fallback(key3));
		assertResolve("Translation of: testFallbackNesting.3.3", key1.fallback(key2.fallback(key3)));
	}

	public void testFallbackWithFallbackSuffix() {
		ResKey key1 = ResKey.internalCreate("testFallbackWithFallbackSuffix.1");
		ResKey key2 = ResKey.internalCreate("testFallbackWithFallbackSuffix.2");
		ResKey key3 = ResKey.internalCreate("testFallbackWithFallbackSuffix.3");
		ResKey innerKeys = key1.fallback(key2).fallback(key3);
		ResKey suffix1Keys = innerKeys.suffix("suffix.1");
		ResKey suffix2Keys = innerKeys.suffix("suffix.2");
		ResKey suffix3Keys = innerKeys.suffix("suffix.3");
		ResKey allKeys = suffix1Keys.fallback(suffix2Keys).fallback(suffix3Keys);
		assertResolve("Translation of: testFallbackWithFallbackSuffix.2.suffix.2", allKeys);
	}

	public void testSuffixOfLiteralTextWithFallback() {
		ResKey key1 = ResKey.internalCreate("testSuffixOfLiteralTextWithFallback.1");
		ResKey key2 = ResKey.text("Translation of: testSuffixOfLiteralTextWithFallback.2");
		ResKey innerKeys = key1.fallback(key2);
		ResKey suffix1Keys = innerKeys.suffix("suffix.1");
		ResKey suffix2Keys = innerKeys.suffix("suffix.2");
		ResKey allKeys = suffix1Keys.fallback(suffix2Keys);
		assertResolve("Translation of: testSuffixOfLiteralTextWithFallback.1.suffix.2", allKeys);
	}

	public void testExpandArguments() {
		assertResolve("Message1(FooBarExisting)",
			ResKey.message(ResKey.internalCreate("message1.existing"), ResKey.internalCreate("foo.bar.existing")));
		assertResolve("Message1(Some literal text)",
			ResKey.message(ResKey.internalCreate("message1.existing"), ResKey.text("Some literal text")));
		assertResolve("Message1(First choice)",
			ResKey.message(ResKey.internalCreate("message1.existing"), SomeEnum.A));
	}

	public void testDebugRepresentation() {
		assertDebug("*[foo.bar.existing]",
			ResKey.internalCreate("foo.bar.existing"));
		assertDebug("-[foo.bar.missing]",
			ResKey.internalCreate("foo.bar.missing"));
		
		assertDebug("-[foo.bar.missing]",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"),
				ResKey.deprecated(ResKey.internalCreate("foo.bar.deprecated.missing"))));
		assertDebug("-[foo.bar.missing]|*[deprecated:foo.bar.deprecated.existing]",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"),
				ResKey.deprecated(ResKey.internalCreate("foo.bar.deprecated.existing"))));

		assertDebug("\"Literal Text\"",
			ResKey.text("Literal Text"));

		assertDebug("*[foo.bar.existing(42, \"Literal Text\")]",
			ResKey.message(ResKey.internalCreate("foo.bar.existing"), 42, "Literal Text"));
		assertDebug("-[foo.bar.missing(42, \"Literal Text\")]",
			ResKey.message(ResKey.internalCreate("foo.bar.missing"), 42, "Literal Text"));

		assertDebug("*[foo.bar.existing(\"Literal Text\")]",
			ResKey.message(ResKey.internalCreate("foo.bar.existing"), "Literal Text"));
		assertDebug("-[foo.bar.missing(\"Literal Text\")]",
			ResKey.message(ResKey.internalCreate("foo.bar.missing"), "Literal Text"));

		assertDebug("*[foo.bar.existing]|-[foo.bar.missing]",
			ResKey.fallback(ResKey.internalCreate("foo.bar.existing"), ResKey.internalCreate("foo.bar.missing")));
		assertDebug("-[foo.bar.missing]|*[foo.bar.existing]",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"), ResKey.internalCreate("foo.bar.existing")));
		assertDebug("-[foo.bar.missing]|\"Literal Text\"",
			ResKey.fallback(ResKey.internalCreate("foo.bar.missing"), ResKey.text("Literal Text")));

		assertDebug("-[none(some debug information).suffix]",
			ResKey.none("some debug information", "suffix"));
	}

	public void testDefaultSuffixes() {
		ResKey key1 = ResKey.internalCreate("key1");
		assertEquals(key1.getKey() + ".tooltip", key1.tooltip().getKey());
	}

	public void testEquals() {
		ResKey key1 = ResKey.internalCreate("key1");
		assertEquals(key1, key1);
		assertEquals(key1, ResKey.internalCreate("key" + "1"));
		assertEquals(key1, ResKey.message(key1));
		assertEquals(key1, ResKey.fallback(key1, null));
		assertEquals(key1, ResKey.fallback(null, key1));
		assertNotEquals(key1, null);
		assertNotEquals(key1, ResKey.internalCreate("key2"));
		assertNotEquals(key1, ResKey.NONE);
		assertNotEquals(key1, ResKey.none("foo", "bar"));
		assertNotEquals(key1, ResKey.message(key1, "foo"));
		assertNotEquals(key1, ResKey.text("key1"));

		// It is questionable, whether the following should hold or not. De-facto, currently it does
		// not hold, because a fallback key has no encoded form and uses the encoded from of its
		// first alternative.
		//
		// assertNotEquals(key1, ResKey.fallback(key1, ResKey.internalCreate("key2")));
	}

	public void testWithoutFallbackLanguage() {
		assertResolve("FooBarExisting", _bundle, ResKey.internalCreate("foo.bar.existing"), false);
		assertResolve("Message1(FooBarExisting)", _bundle,
			ResKey.internalCreate("message1.existing").asResKey1().fill(ResKey.internalCreate("foo.bar.existing")),
			true);
		assertResolve(null, _bundle,
			ResKey.internalCreate("message1.existing").asResKey1().fill(ResKey.internalCreate("foo.bar.existing")),
			false);
	}

	public void testFallbackInArgument() {
		Object[] arguments = { ResKey.fallback(
			ResKey.internalCreate("foo.bar.missing"),
			ResKey.internalCreate("foo.bar.existing")) };
		assertResolve("Message1(FooBarExisting)",
			ResKey.message(ResKey.internalCreate("message1.existing"), arguments));
	}

	public void testFallbackLiteralSuffix() {
		assertResolve("result", fallback(ResKey.text("literal").suffix("invalid"), text("result")));
	}

	public void testLiteralKey() {
		ResKey literal = ResKey.literal(en("foo"), de("bar"));
		assertResolve("foo", literal);
		assertEquals("Resolving a suffix of literal key does not fail.", ResKey.NONE, literal.tooltip());

		ResKey pattern = ResKey.literal(en("foo {0}"), de("bar {0}"));
		ResKey message = ResKey.message(pattern, "!");
		assertResolve("foo !", message);
		assertEquals("Resolving a suffix of message with literal key does not fail.", ResKey.NONE, message.tooltip());
	}

	public void testLiteralKeyFallback() {
		ResKey literal = ResKey.literal(de("bar"));
		assertResolve("bar", literal);
	}

	public void testLiteralKeyLanguageFallback() {
		Locale de = new Locale("de");
		Locale de_CH = new Locale("de", "CH");
		Locale en = new Locale("en");

		ResKey literal = ResKey.literal(
			ResKey.langString(en, "Hello"),
			ResKey.langString(de, "Guten Tag"),
			ResKey.langString(de_CH, "Grüzi"));

		BundleForTest bundle_de =
			new BundleForTest(Collections.emptyMap(), new Locale("de"));
		BundleForTest bundle_en =
			new BundleForTest(Collections.emptyMap(), new Locale("en"));

		BundleForTest bundle_de_DE =
			new BundleForTest(Collections.emptyMap(), new Locale("de", "DE"), bundle_de);
		assertResolve("Guten Tag", bundle_de_DE, literal);
		BundleForTest bundle_de_CH =
			new BundleForTest(Collections.emptyMap(), new Locale("de", "CH"), bundle_de);
		assertResolve("Grüzi", bundle_de_CH, literal);
		BundleForTest bundle_de_AT =
			new BundleForTest(Collections.emptyMap(), new Locale("de", "AT"), bundle_de);
		assertResolve("Guten Tag", bundle_de_AT, literal);
		BundleForTest bundle_en_US =
			new BundleForTest(Collections.emptyMap(), new Locale("en", "US"), bundle_en);
		assertResolve("Hello", bundle_en_US, literal);
	}

	public void testLiteralKeyUnknown() {
		ResKey literal = ResKey.literal(ch("bar"), en("foo"));
		assertEquals("[#(\"foo\"@en, \"bar\"@zh)]", literal.unknown(_bundle));
	}

	public void testTooltip() {
		ResKey key1 = ResKey.internalCreate("testTooltip.keyWithTooltip");
		assertResolve("Translation of: testTooltip.keyWithTooltip", key1);
		assertResolve("Translation of: testTooltip.keyWithTooltip.tooltip", key1.tooltip());
		assertResolve("Translation of: testTooltip.keyWithTooltip.tooltip", key1.tooltipOptional());

		ResKey key2 = ResKey.internalCreate("testTooltip.keyWithoutTooltip");
		assertResolve("Translation of: testTooltip.keyWithoutTooltip", key2);
		assertResolve("[testTooltip.keyWithoutTooltip.tooltip]", key2.tooltip());
		assertResolve(null, key2.tooltipOptional());
	}

	private static LangString en(String value) {
		return langString(Locale.ENGLISH, value);
	}

	private static LangString de(String value) {
		return langString(Locale.GERMAN, value);
	}

	private static LangString ch(String value) {
		return langString(Locale.CHINESE, value);
	}

	private void assertEquals(ResKey expected, ResKey actual) {
		TestCase.assertEquals(expected, actual);
		TestCase.assertEquals(expected.hashCode(), actual.hashCode());
	}

	private void assertDebug(String expected, ResKey resKey) {
		assertEquals(expected, resKey.debug(_bundle));
	}

	private void assertResolve(String expected, ResKey resKey) {
		assertResolve(expected, _bundle, resKey);
	}

	private void assertResolve(String expected, BundleForTest bundle, ResKey resKey) {
		assertResolve(expected, bundle, resKey, true);
	}

	private void assertResolve(String expected, BundleForTest bundle, ResKey resKey, boolean withFallback) {
		assertEquals(expected, resKey.resolve(bundle, resKey, withFallback));

		ResKey encoded = ResKey.decode(ResKey.encode(resKey));
		assertEquals("Decoded key not same.", resKey, encoded);
		assertEquals("Decoded key does not resolve.", expected, encoded.resolve(bundle, resKey, withFallback));
	}

	static final class BundleForTest implements I18NBundleSPI {
		private ResourcesModule _owner;

		private final Map<String, String> _map;
		private Set<ResKey> _unknown = new HashSet<>();
		private Set<String> _deprecations = new HashSet<>();

		private Locale _locale;

		private I18NBundleSPI _fallback;

		public BundleForTest(Map<String, String> map, Locale locale) {
			this(map, locale, null);
		}

		/**
		 * Creates a {@link TestResKey.BundleForTest}.
		 */
		public BundleForTest(Map<String, String> map, Locale locale, I18NBundleSPI fallback) {
			_map = map;
			_locale = locale;
			_fallback = fallback;
		}

		@Override
		public I18NBundleSPI getFallback() {
			return _fallback;
		}

		@Override
		public ResourcesModule owner() {
			if (_owner == null) {
				_owner = mkOwner();
			}
			return _owner;
		}
	
		private static ResourcesModule mkOwner() {
			try {
				Config config = TypedConfiguration.newConfigItem(Config.class);
				config.setSupportedLocales(
					Arrays.asList(
						Locale.ENGLISH.toLanguageTag(),
						Locale.GERMAN.toLanguageTag(),
						Locale.CHINESE.toLanguageTag()));
				config.setFallbackLocale(Locale.CHINESE);
				return new ResourcesModule(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}

		@Override
		public List<Locale> getSupportedLocalesInDisplayOrder() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean existsResource(String key) {
			return _map.containsKey(key);
		}

		@Override
		public String lookup(String key, boolean withFallbackBundle) {
			String value = _map.get(key);
			if (value != null || _map.containsKey(key)) {
				return value;
			}
			if (withFallbackBundle && _fallback != null) {
				return _fallback.lookup(key, withFallbackBundle);
			}
			return value;
		}
	
		@Override
		public Locale getLocale() {
			return _locale;
		}
	
		@Override
		public void handleUnknownKey(ResKey key) {
			_unknown.add(key);
		}

		@Override
		public void handleDeprecatedKey(ResKey deprecatedKey, ResKey origKey) {
			_deprecations.add(deprecatedKey.getKey());
		}
		
		public void assertNoDeprecations() {
			assertDeprecations();
		}
		
		public void assertDeprecations(String... expectedKeys) {
			assertEq(_deprecations, list(expectedKeys));
		}

		private <T> void assertEq(Collection<T> acual, List<T> expected) {
			boolean ok = acual.containsAll(expected);
			if (!ok) {
				HashSet<T> missing = new HashSet<>(expected);
				missing.removeAll(acual);
				fail("Missing: " + missing.toString());
			}
			acual.removeAll(expected);
			BasicTestCase.assertEquals("Unexpected: " + acual.toString(), 0, acual.size());
			acual.clear();
		}

		public void assertNoUnknown() {
			assertUnknown();
		}

		public void assertUnknown(ResKey... expectedKeys) {
			assertEq(_unknown, list(expectedKeys));
		}

		@Override
		public Set<String> getLocalKeys() {
			return _map.keySet();
		}

		@Override
		public String getString(ResKey aKey) {
			return internalGetString(aKey, true);
		}

		@Override
		public String getStringWithoutFallback(ResKey key) {
			return internalGetString(key, false);
		}

		private String internalGetString(ResKey key, boolean withFallback) {
			if (key == null) {
				return StringServices.EMPTY_STRING;
			}
			return key.resolve(this, key, withFallback);
		}

		@Override
		public void invalidate() {
			// Ignore.
		}

	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(TestResKey.class);
	}

}
