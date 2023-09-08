/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.DefaultBundle;
import com.top_logic.basic.util.I18NBundleSPI;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResKeyEncoding;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.util.ResourcesModule.Config;

/**
 * Test case for {@link ResKeyEncoding}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestResKeyEncoding extends TestCase {

	enum X {
		A, B;
	}

	public void testEncodeMessage() {
		assertEncode("abc.def", message());
		assertEncode("abc.def", message((Object[]) null));
		assertEncode("abc.def/sMessage 1", message("Message 1"));
		assertEncode("abc.def/sMessage 1", message(new Object[] { "Message 1" }));
		assertEncode("abc.def/sMessage 1/l123", message("Message 1", Long.valueOf(123)));
	}

	public void testEncodeNull() {
		ResKey key = message(new Object[] { null });
		String encoded = ResKey.encode(key);
		ResKey decoded = ResKey.decode(encoded);
		assertNull(decoded.arguments()[0]);
	}

	public void testEncodeUnknown() {
		ResKey key = message(new NamedConstant("foobar"));
		String encoded = ResKey.encode(key);
		ResKey decoded = ResKey.decode(encoded);
		assertEquals("foobar", decoded.arguments()[0]);
	}

	public void testEncodeLongString() {
		assertEncodeDecode(BasicTestCase.randomString(1024, true, true, true, true));
	}

	public void testEncodeInteger() {
		assertEncodeDecode(42);
	}

	public void testEncodeLong() {
		assertEncodeDecode(42424242424242L);
	}

	public void testEncodeFloat() {
		assertEncodeDecode(42.4242F);
	}

	public void testEncodeDouble() {
		assertEncodeDecode(42.42424242424242D);
	}

	public void testEncodeDate() {
		assertEncodeDecode(DateUtil.createDate(2016, Calendar.FEBRUARY, 29, 23, 59, 59));
	}

	public void testEncodeBoolean() {
		assertEncodeDecode(true);
		assertEncodeDecode(false);
	}

	public void testEncodeEnum() {
		assertEncodeDecode(X.A);
	}

	public void testEncodeKey() {
		assertEncodeDecode(ResKey.forTest("inner.key"));
	}

	public void testEncodeDecode() {
		String arg1 = "$r.set(tl.dev.webhook:BuildRequest#item, $item)";
		String arg2 = "()";
		Map<String, String> arg3 = new LinkedHashMap<>();
		arg3.put("foo", "bar");
		arg3.put("unencoded", "(-> ^/s/i.:\\d+/?.($).().(.))");
		ResKey key = I18NConstants.WITH_ARGS__A1_A2_A3.fill(arg1, arg2, arg3);
		String encoded = ResKey.encode(key);
		ResKey decoded = ResKey.decode(encoded);

		assertNotNull(decoded);
		assertEquals(key.getKey(), decoded.getKey());
		assertEquals(key.arguments().length, decoded.arguments().length);
		assertEquals(key.arguments()[0], decoded.arguments()[0]);
		assertEquals(key.arguments()[1], decoded.arguments()[1]);
		assertEquals(key.arguments()[2].toString(), decoded.arguments()[2]);
	}

	public void testDecodeLegacyString() {
		String value = "/foo//bar/";
		String encoded = "abc.def/s" + value.replace("/", "//");
		ResKey decoded = ResKey.decode(encoded);
		assertEquals(value, decoded.arguments()[0]);
	}

	public void testDecodeLegacyText() {
		String encoded = "abc.def/t";
		ResKey decoded = ResKey.decode(encoded);
		assertNull(decoded.arguments()[0]);
	}

	public void testDecodeEmpty() {
		assertNull(ResKey.decode(""));
		assertNull(ResKey.decode(null));
	}

	private void assertEncodeDecode(Object value) {
		ResKey key = message(value);
		String encoded = ResKey.encode(key);
		ResKey decoded = ResKey.decode(encoded);
		assertEquals(value, decoded.arguments()[0]);
	}

	public void testMessageWithoutArguments() {
		ResKey key = message();
		assertSame(key.plain(), key);
	}

	private ResKey message(Object... arguments) {
		return ResKey.message(ResKey.forTest("abc.def"), arguments);
	}

	private ResKey message(Object argument) {
		return ResKey.message(ResKey.forTest("abc.def"), argument);
	}

	private void assertEncode(String expectedEncoding, ResKey resKey) {
		String encoded = ResKey.encode(resKey);
		assertEquals(expectedEncoding, encoded);

		ResKey decoded = ResKey.decode(encoded);
		assertEquals(resKey.getKey(), decoded.getKey());
		assertTrue(Arrays.equals(resKey.arguments(), decoded.arguments()));
	}

	public void testEncodeLiteralText() {
		assertDecodeEncodedText(null);
		assertDecodeEncodedText("");
		assertDecodeEncodedText("Hello world");
		assertDecodeEncodedText("/^°!\"§$%&/()=?\\´`+~*#'-_.:,'; \t\r\nöäüÖÄÜß€/");
		assertDecodeEncodedText("!Starting with an exclamation mark");
		assertDecodeEncodedText("Ending with an exclamation mark!");
	}

	private void assertDecodeEncodedText(String literalText) {
		ResKey text = ResKey.text(literalText);
		String encodedText = ResKey.encode(text);
		ResKey decodedText = ResKey.decode(encodedText);
		assertEquals(literalText, decodedText.arguments()[0]);
	}

	public void testEncodeLiteralTranslations() {
		assertDecodeEncodedTranslations("");
		assertDecodeEncodedTranslations("Hello world");
		assertDecodeEncodedTranslations("/^°!\"§$%&/()=?\\´`+~*#'-_.:,'; \t\r\nöäüÖÄÜß€/");
		assertDecodeEncodedTranslations("!Starting with an exclamation mark");
		assertDecodeEncodedTranslations("Ending with an exclamation mark!");
	}

	private void assertDecodeEncodedTranslations(String literalText) {
		Builder translations = ResKey.builder();
		String enText = literalText.isEmpty() ? "" : "en:" + literalText;
		translations.add(Locale.ENGLISH, enText);

		String deText = literalText.isEmpty() ? "" : "de:" + literalText;
		translations.add(Locale.GERMAN, deText);
		ResKey text = translations.build();

		String encodedText = ResKey.encode(text);
		ResKey decodedText = ResKey.decode(encodedText);

		assertEquals(deText, decodedText.resolve(bundle(Locale.GERMAN), text));
		assertEquals(enText, decodedText.resolve(bundle(Locale.ENGLISH), text));
	}

	private I18NBundleSPI bundle(Locale locale) {
		try {
			Config config = TypedConfiguration.newConfigItem(Config.class);
			ResourcesModule owner = new ResourcesModule(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
			I18NBundleSPI bundle =
				new DefaultBundle(owner, locale, Collections.emptyMap(), null);
			return bundle;
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(TestResKeyEncoding.class);
	}

}
