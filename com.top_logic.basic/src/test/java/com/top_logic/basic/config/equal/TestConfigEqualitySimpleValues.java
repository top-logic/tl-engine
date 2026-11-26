/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.equal;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.RegExpValueProvider;

/**
 * Test of {@link ConfigEquality} for simple property values that have no value equality of their
 * own and are created anew each time they are read.
 */
@SuppressWarnings("javadoc")
public class TestConfigEqualitySimpleValues extends AbstractTypedConfigurationTestCase {

	/**
	 * Value without value equality: {@link #equals(Object)} is the identity of {@link Object}.
	 */
	public static final class Token {

		private final String _name;

		public Token(String name) {
			_name = name;
		}

		public String getName() {
			return _name;
		}

	}

	/**
	 * Format that creates a new {@link Token} each time a text is read.
	 */
	public static final class TokenFormat extends AbstractConfigurationValueProvider<Token> {

		public static final TokenFormat INSTANCE = new TokenFormat();

		private TokenFormat() {
			super(Token.class);
		}

		@Override
		protected Token getValueNonEmpty(String propertyName, CharSequence propertyValue) {
			return new Token(propertyValue.toString());
		}

		@Override
		protected String getSpecificationNonNull(Token configValue) {
			return configValue.getName();
		}

	}

	/**
	 * Format that reads {@link Token}s but cannot write them.
	 */
	public static final class UnwritableTokenFormat extends AbstractConfigurationValueProvider<Token> {

		public static final UnwritableTokenFormat INSTANCE = new UnwritableTokenFormat();

		private UnwritableTokenFormat() {
			super(Token.class);
		}

		@Override
		protected Token getValueNonEmpty(String propertyName, CharSequence propertyValue) {
			return new Token(propertyValue.toString());
		}

		@Override
		protected String getSpecificationNonNull(Token configValue) {
			throw new IllegalArgumentException("Cannot write token: " + configValue.getName());
		}

	}

	public interface TokenConfig extends ConfigurationItem {

		String TOKEN = "token";

		@Name(TOKEN)
		@Format(TokenFormat.class)
		Token getToken();

	}

	public interface UnwritableTokenConfig extends ConfigurationItem {

		String TOKEN = "token";

		@Name(TOKEN)
		@Format(UnwritableTokenFormat.class)
		Token getToken();

		void setToken(Token value);

	}

	public interface PatternConfig extends ConfigurationItem {

		String PATTERN = "pattern";

		@Name(PATTERN)
		@Format(RegExpValueProvider.class)
		Pattern getPattern();

	}

	public void testSameTextIsEqual() throws ConfigurationException {
		TokenConfig left = token("a");
		TokenConfig right = token("a");

		assertNotSame(left.getToken(), right.getToken());
		assertFalse(left.getToken().equals(right.getToken()));
		assertConfigEquals(left, right);
	}

	public void testDifferentTextIsNotEqual() throws ConfigurationException {
		assertConfigNotEquals(token("a"), token("b"));
	}

	public void testUnsetValueIsNotEqual() throws ConfigurationException {
		TokenConfig unset = TypedConfiguration.newConfigItem(TokenConfig.class);

		assertConfigNotEquals(token("a"), unset);
		assertConfigNotEquals(unset, token("a"));
		assertConfigEquals(unset, TypedConfiguration.newConfigItem(TokenConfig.class));
	}

	/**
	 * Two {@link Pattern}s are equal if they have the same pattern.
	 *
	 * <p>
	 * The flags of the patterns are not checked, because {@link RegExpValueProvider} writes the
	 * pattern without its flags.
	 * </p>
	 */
	public void testPattern() throws ConfigurationException {
		PatternConfig left = pattern("a+b");
		PatternConfig right = pattern("a+b");

		assertNotSame(left.getPattern(), right.getPattern());
		assertConfigEquals(left, right);
		assertConfigNotEquals(left, pattern("a*b"));
	}

	public void testUnwritableValueKeepsEquals() throws ConfigurationException {
		UnwritableTokenConfig left = unwritableToken("a");
		UnwritableTokenConfig right = unwritableToken("a");

		// Different tokens that cannot be written are not equal.
		assertFalse(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, right));
		ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(left);
		ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(right);

		// The same token is equal.
		right.setToken(left.getToken());
		assertConfigEquals(left, right);
	}

	private static void assertConfigEquals(ConfigurationItem left, ConfigurationItem right) {
		assertTrue(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, right));
		assertTrue(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(right, left));
		assertEquals(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(left),
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(right));
	}

	private static void assertConfigNotEquals(ConfigurationItem left, ConfigurationItem right) {
		assertFalse(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, right));
		assertFalse(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(right, left));
	}

	private static TokenConfig token(String text) throws ConfigurationException {
		return TypedConfiguration.newConfigItem(TokenConfig.class,
			Collections.singletonMap(TokenConfig.TOKEN, text).entrySet());
	}

	private static UnwritableTokenConfig unwritableToken(String text) throws ConfigurationException {
		return TypedConfiguration.newConfigItem(UnwritableTokenConfig.class,
			Collections.singletonMap(UnwritableTokenConfig.TOKEN, text).entrySet());
	}

	private static PatternConfig pattern(String text) throws ConfigurationException {
		return TypedConfiguration.newConfigItem(PatternConfig.class,
			Collections.singletonMap(PatternConfig.PATTERN, text).entrySet());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("config", TypedConfiguration.getConfigurationDescriptor(TokenConfig.class));
	}

	/**
	 * A cumulative {@link Test} for all tests in {@link TestConfigEqualitySimpleValues}.
	 */
	public static Test suite() {
		return suite(TestConfigEqualitySimpleValues.class);
	}

}
