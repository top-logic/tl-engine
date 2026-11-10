/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.base.accesscontrol.ExternalLoginListener;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.ScriptAbort;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.ExternalLoginListenerByExpression;
import com.top_logic.model.search.providers.ExternalLoginListenerByExpression.Config;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of {@link ExternalLoginListenerByExpression}.
 *
 * <p>
 * The test bodies run with security and without a logged in user, as a login does.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestExternalLoginListenerByExpression extends AbstractSearchExpressionTest {

	private static final String CLIENT = "test-idp";

	private static final String FIRST_NAME = "firstName";

	private static final String LAST_NAME = "lastName";

	private Person _account;

	@Override
	protected boolean checksSecurity() {
		return true;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_account = TestPerson.createPerson("externalLoginScriptUser");
			_account.tUpdateByName(FIRST_NAME, "Initial");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_account.tDelete();
			tx.commit();
		}
		_account = null;
		super.tearDown();
	}

	public void testOnCreateSetsFirstNameFromClaim() throws ConfigurationException {
		ExternalLoginListenerByExpression listener = listener(
			"account -> claims -> client -> $account.set(`tl.accounts:Person#firstName`, "
				+ "regex(\"^[^,]*, *\").regexReplace($claims[\"given_name\"], \"\"))",
			null);

		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			listener.onCreate(_account, identity(Map.of("given_name", "Haumacher, Bernhard")));
			// The listener has no on-login script.
			listener.onLogin(_account, identity(Map.of("given_name", "Haumacher, Bernhard")));
			tx.commit();
		}

		assertEquals("Bernhard", _account.tValueByName(FIRST_NAME));
	}

	public void testOnLoginSeesClientAndNestedClaims() throws ConfigurationException {
		ExternalLoginListenerByExpression listener = listener(null,
			"account -> claims -> client -> $account.set(`tl.accounts:Person#lastName`, "
				+ "$client + \"/\" + $claims[\"org\"][\"unit\"] + \"/\" + $claims[\"groups\"].size())");

		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			// The listener has no on-create script.
			listener.onCreate(_account, identity(Map.of()));
			assertNull(_account.tValueByName(LAST_NAME));

			listener.onLogin(_account,
				identity(Map.of("org", Map.of("unit", "R&D"), "groups", List.of("/admins", "/developers"))));
			tx.commit();
		}

		assertEquals(CLIENT + "/R&D/2", _account.tValueByName(LAST_NAME));
	}

	public void testThrowRefusesLoginWithReason() throws ConfigurationException {
		ExternalLoginListenerByExpression listener = listener(null,
			"account -> claims -> client -> { $account.set(`tl.accounts:Person#firstName`, \"Changed\"); "
				+ "throw(\"No department.\"); }");

		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			listener.onLogin(_account, identity(Map.of()));
			fail("Script must refuse the login.");
		} catch (LoginDeniedException ex) {
			assertEquals(ResKey.text("No department."), ex.getReason());
			assertTrue(ex.getCause() instanceof ScriptAbort);
		}

		assertEquals("Rollback restores the account.", "Initial", _account.tValueByName(FIRST_NAME));
	}

	public void testScriptErrorRefusesLogin() throws ConfigurationException {
		ExternalLoginListenerByExpression listener = listener(null,
			"account -> claims -> client -> { $account.set(`tl.accounts:Person#firstName`, \"Changed\"); "
				+ "$account.set(`tl.accounts:Person#noSuchAttribute`, 1); }");

		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			listener.onLogin(_account, identity(Map.of()));
			fail("Script must fail.");
		} catch (RuntimeException ex) {
			assertFalse("Only an explicit throw() gives a reason.", ex instanceof LoginDeniedException);
		}

		assertEquals("Rollback restores the account.", "Initial", _account.tValueByName(FIRST_NAME));
	}

	/**
	 * Without disabled security, a script of the login could not modify the account, since no user
	 * is logged in.
	 */
	public void testSecuredScriptCannotModifyAccount() throws Exception {
		QueryExecutor secured =
			QueryExecutor.compile(parse("account -> $account.set(`tl.accounts:Person#firstName`, \"Changed\")"));
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			secured.execute(_account);
			fail("Write access without user must be denied.");
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	private static ExternalIdentity identity(Map<String, ?> claims) {
		return new ExternalIdentity(CLIENT, "subject-1", "externalLoginScriptUser", claims);
	}

	/**
	 * Reads a listener from its configuration in a list of listeners, as an
	 * {@link ExternalLoginListener} is configured at an external login.
	 */
	private static ExternalLoginListenerByExpression listener(String onCreate, String onLogin)
			throws ConfigurationException {
		StringBuilder xml = new StringBuilder();
		xml.append("<config><listeners><").append(Config.TAG_NAME).append('>');
		appendScript(xml, Config.ON_CREATE, onCreate);
		appendScript(xml, Config.ON_LOGIN, onLogin);
		xml.append("</").append(Config.TAG_NAME).append("></listeners></config>");

		BufferingProtocol log = new BufferingProtocol();
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		ListenersConfig config = (ListenersConfig) new ConfigurationReader(context,
			Collections.singletonMap("config", TypedConfiguration.getConfigurationDescriptor(ListenersConfig.class)))
				.setSource(CharacterContents.newContent(xml.toString()))
				.read();
		List<ExternalLoginListener> listeners = TypedConfiguration.getInstanceList(context, config.getListeners());
		log.checkErrors();
		assertEquals(1, listeners.size());
		return (ExternalLoginListenerByExpression) listeners.get(0);
	}

	private static void appendScript(StringBuilder xml, String property, String script) {
		if (script != null) {
			xml.append('<').append(property).append("><![CDATA[").append(script).append("]]></")
				.append(property).append('>');
		}
	}

	/**
	 * Configuration with a list of {@link ExternalLoginListener}s, as an external login has it.
	 */
	public interface ListenersConfig extends ConfigurationItem {

		/**
		 * The configured listeners.
		 */
		List<PolymorphicConfiguration<? extends ExternalLoginListener>> getListeners();

	}

	public static Test suite() {
		return suite(TestExternalLoginListenerByExpression.class);
	}

}
