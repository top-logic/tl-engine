/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.login;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.security.auth.pac4j.config.AnonymousClientConfigurator;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.login.I18NConstants;
import com.top_logic.security.auth.pac4j.login.Pac4jLoginMethodProvider;

/**
 * Tests the button label {@link Pac4jLoginMethodProvider#buttonLabel(ResKey)} derives from the
 * {@link ClientConfigurator.Config#getLabel() configured label} of a client.
 */
@SuppressWarnings("javadoc")
public class TestPac4jLoginMethodProvider extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	/** A label given as plain text. */
	private static final String PLAIN_LABEL = "Login with ACME";

	public interface Config extends ConfigurationItem {

		String CLIENT = "client";

		@Name(CLIENT)
		ClientConfigurator.Config<?> getClient();

	}

	/** A client without label gets the generic label. */
	public void testMissingLabel() throws Exception {
		assertEquals(I18NConstants.LOGIN_WITH_SSO, buttonLabel(""));
	}

	/** A label from an environment variable that is not set gets the generic label. */
	public void testEmptyLabel() throws Exception {
		assertEquals(I18NConstants.LOGIN_WITH_SSO, buttonLabel("<label><en></en></label>"));
	}

	/** A label with only empty translations gets the generic label. */
	public void testEmptyTranslations() throws Exception {
		assertEquals(I18NConstants.LOGIN_WITH_SSO, buttonLabel("<label><en></en><de></de></label>"));
	}

	/** A plain text label is shown as it is, in every language. */
	public void testPlainLabel() throws Exception {
		ResKey label = buttonLabel("<label><en>" + PLAIN_LABEL + "</en></label>");
		assertTrue(label.isLiteral());
		ResKey.LiteralKey literal = (ResKey.LiteralKey) label;
		assertEquals(PLAIN_LABEL, literal.getTranslationWithoutFallbacks(Locale.ENGLISH));
		assertEquals(Collections.singleton(PLAIN_LABEL), new HashSet<>(literal.getTranslations().values()));
	}

	/** A label referencing a resource key is kept. */
	public void testResourceKeyLabel() throws Exception {
		ResKey key = com.top_logic.security.auth.pac4j.config.I18NConstants.INVALID_FORMAT__DETAILS.fill("x");
		assertEquals(key, Pac4jLoginMethodProvider.buttonLabel(key));
		assertFalse(Pac4jLoginMethodProvider.isEmptyLabel(key));
	}

	/** Labels without text. */
	public void testIsEmptyLabel() {
		assertTrue(Pac4jLoginMethodProvider.isEmptyLabel(null));
		assertTrue(Pac4jLoginMethodProvider.isEmptyLabel(ResKey.NONE));
		assertTrue(Pac4jLoginMethodProvider.isEmptyLabel(ResKey.text("")));
		assertFalse(Pac4jLoginMethodProvider.isEmptyLabel(ResKey.text(PLAIN_LABEL)));
	}

	private ResKey buttonLabel(String labelElement) throws Exception {
		Config config = read("<" + ROOT_TAG + "><" + Config.CLIENT + " class='"
			+ AnonymousClientConfigurator.class.getName() + "' name='test-client'>"
			+ labelElement
			+ "</" + Config.CLIENT + "></" + ROOT_TAG + ">");
		return Pac4jLoginMethodProvider.buttonLabel(config.getClient().getLabel());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestPac4jLoginMethodProvider.class);
	}

}
