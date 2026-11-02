/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.secret;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import javax.crypto.KeyGenerator;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.ChainSecretStore;
import com.top_logic.basic.credential.EnvironmentSecretStore;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.credential.WritableSecretStore;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.secret.DatabaseSecretStore;
import com.top_logic.knowledge.secret.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test case for {@link DatabaseSecretStore}.
 */
@SuppressWarnings("javadoc")
public class TestDatabaseSecretStore extends BasicTestCase {

	/** Name of the secret holding the key, delivered as system property by the environment store. */
	private static final String KEY_SECRET = "test_database_secret_store_key";

	private static final String SECRET = "test_database_secret_store_secret";

	private static final String OTHER = "test_database_secret_store_other";

	private static final String VALUE = "plain-token-4711";

	private static final String OTHER_VALUE = "other-token-0815";

	private static final String PREFIX = "pfx_";

	private static final InstantiationContext CONTEXT = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty(KEY_SECRET, newKey());
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(KEY_SECRET);
		KnowledgeBase kb = kb();
		try (Transaction tx = kb.beginTransaction()) {
			for (KnowledgeItem row : rows()) {
				row.delete();
			}
			tx.commit();
		}
		super.tearDown();
	}

	/**
	 * A written secret is found through the lookup path of the {@link SecretStoreService}, by the
	 * writable store of its chain.
	 */
	public void testLookupThroughService() {
		SecretStoreService service = newService(null);
		WritableSecretStore writable = service.getWritableStore();
		assertNotNull("Chain with a database store has no writable store.", writable);

		assertNull(service.lookup(SECRET));
		writable.store(SECRET, VALUE);
		assertEquals(VALUE, service.lookup(SECRET));
		assertEquals(VALUE, service.getSecret(SECRET));
		assertNull(service.lookup(OTHER));
	}

	/**
	 * The writable store of a chain with prefix writes the secret under the prefixed name, where
	 * the chain looks for it.
	 */
	public void testChainPrefix() {
		SecretStoreService service = newService(PREFIX);
		service.getWritableStore().store(SECRET, VALUE);

		assertEquals(VALUE, service.getSecret(SECRET));
		assertEquals(VALUE, newStore().lookup(PREFIX + SECRET));
		assertNull(newStore().lookup(SECRET));
	}

	/**
	 * A chain without writable store has none, and neither has the service.
	 */
	public void testNoWritableStore() {
		ChainSecretStore.Config chain = TypedConfiguration.newConfigItem(ChainSecretStore.Config.class);
		chain.getStores().add(TypedConfiguration.newConfigItem(EnvironmentSecretStore.Config.class));
		SecretStoreService.Config config = TypedConfiguration.newConfigItem(SecretStoreService.Config.class);
		config.setStore(chain);
		assertNull(new SecretStoreService(CONTEXT, config).getWritableStore());
	}

	/**
	 * Storing a secret again replaces it; removing it makes it absent.
	 */
	public void testReplaceAndRemove() {
		DatabaseSecretStore store = newStore();
		store.store(SECRET, VALUE);
		store.store(OTHER, OTHER_VALUE);
		store.store(SECRET, OTHER_VALUE);
		assertEquals(OTHER_VALUE, store.getSecret(SECRET));
		assertEquals(1, rows(SECRET).size());

		assertTrue(store.remove(SECRET));
		assertNull(store.lookup(SECRET));
		assertTrue(rows(SECRET).isEmpty());
		assertFalse(store.remove(SECRET));
		assertEquals(OTHER_VALUE, store.getSecret(OTHER));

		I18NRuntimeException ex = failure(() -> store.getSecret(SECRET));
		assertEquals(I18NConstants.ERROR_DATABASE_SECRET_UNDEFINED__NAME.fill(SECRET), ex.getErrorKey());
	}

	/**
	 * A secret is read by another store instance, i.e. it lives in the database only.
	 */
	public void testSurvivesFreshInstance() {
		newStore().store(SECRET, VALUE);
		assertEquals(VALUE, newStore().getSecret(SECRET));
	}

	/**
	 * The stored row holds neither the plain secret nor the key.
	 */
	public void testPlainValueNotStored() {
		newStore().store(SECRET, VALUE);
		List<KnowledgeItem> rows = rows(SECRET);
		assertEquals(1, rows.size());
		KnowledgeItem row = rows.get(0);
		String encodedKey = System.getProperty(KEY_SECRET);
		for (MOAttribute attribute : ((MOStructure) row.tTable()).getAttributes()) {
			Object value = row.getAttributeValue(attribute.getName());
			String text = String.valueOf(value);
			assertFalse("Plain secret stored in " + attribute.getName(), text.contains(VALUE));
			assertFalse("Key stored in " + attribute.getName(), text.contains(encodedKey));
		}
		String cipherText = (String) row.getAttributeValue(DatabaseSecretStore.CIPHER_TEXT_ATTR);
		assertFalse(new String(Base64.getDecoder().decode(cipherText)).contains(VALUE));
	}

	/**
	 * A modified cipher text is rejected with an error naming the secret but not containing it.
	 */
	public void testTamperedCipherTextRejected() {
		DatabaseSecretStore store = newStore();
		store.store(SECRET, VALUE);

		KnowledgeItem row = rows(SECRET).get(0);
		byte[] cipherText = Base64.getDecoder().decode((String) row.getAttributeValue(DatabaseSecretStore.CIPHER_TEXT_ATTR));
		cipherText[cipherText.length / 2] ^= 1;
		setCipherText(row, Base64.getEncoder().encodeToString(cipherText));

		assertNotAuthentic(store, SECRET);

		setCipherText(row, "no cipher text");
		assertNotAuthentic(store, SECRET);
	}

	/**
	 * The cipher text of one secret copied to the row of another is rejected.
	 */
	public void testSwappedCipherTextRejected() {
		DatabaseSecretStore store = newStore();
		store.store(SECRET, VALUE);
		store.store(OTHER, OTHER_VALUE);

		String otherCipherText =
			(String) rows(OTHER).get(0).getAttributeValue(DatabaseSecretStore.CIPHER_TEXT_ATTR);
		setCipherText(rows(SECRET).get(0), otherCipherText);

		assertNotAuthentic(store, SECRET);
		assertEquals(OTHER_VALUE, store.getSecret(OTHER));
	}

	/**
	 * A secret written with another key is rejected.
	 */
	public void testWrongKeyRejected() throws Exception {
		DatabaseSecretStore store = newStore();
		store.store(SECRET, VALUE);

		System.setProperty(KEY_SECRET, newKey());
		assertNotAuthentic(store, SECRET);
	}

	/**
	 * A missing key is reported naming the key secret, on writing and on reading.
	 */
	public void testMissingKey() {
		DatabaseSecretStore store = newStore();
		store.store(SECRET, VALUE);

		System.clearProperty(KEY_SECRET);
		ResKey expected = I18NConstants.ERROR_DATABASE_SECRET_KEY_MISSING__KEY.fill(KEY_SECRET);
		assertEquals(expected, failure(() -> store.store(OTHER, OTHER_VALUE)).getErrorKey());
		assertEquals(expected, failure(() -> store.lookup(SECRET)).getErrorKey());
		assertTrue(rows(OTHER).isEmpty());
	}

	/**
	 * A key that is no Base64 encoded AES key is reported naming the key secret.
	 */
	public void testInvalidKey() {
		System.setProperty(KEY_SECRET, Base64.getEncoder().encodeToString(new byte[7]));
		ResKey expected = I18NConstants.ERROR_DATABASE_SECRET_KEY_INVALID__KEY.fill(KEY_SECRET);
		assertEquals(expected, failure(() -> newStore().store(SECRET, VALUE)).getErrorKey());

		System.setProperty(KEY_SECRET, "not base64 !");
		assertEquals(expected, failure(() -> newStore().store(SECRET, VALUE)).getErrorKey());
	}

	/**
	 * The store neither holds nor accepts its own key, and rejects an empty secret.
	 */
	public void testOwnKeyAndEmptySecret() {
		DatabaseSecretStore store = newStore();
		assertNull(store.lookup(KEY_SECRET));
		assertEquals(I18NConstants.ERROR_DATABASE_SECRET_IS_KEY__NAME.fill(KEY_SECRET),
			failure(() -> store.store(KEY_SECRET, VALUE)).getErrorKey());
		assertEquals(I18NConstants.ERROR_DATABASE_SECRET_EMPTY__NAME.fill(SECRET),
			failure(() -> store.store(SECRET, "")).getErrorKey());
		assertTrue(rows().isEmpty());
	}

	private void assertNotAuthentic(DatabaseSecretStore store, String name) {
		I18NRuntimeException ex = failure(() -> store.lookup(name));
		assertEquals(I18NConstants.ERROR_DATABASE_SECRET_NOT_AUTHENTIC__NAME.fill(name), ex.getErrorKey());
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			String message = cause.getMessage();
			assertTrue("Secret in message: " + message,
				message == null || !(message.contains(VALUE) || message.contains(OTHER_VALUE)));
		}
	}

	private static I18NRuntimeException failure(Runnable action) {
		try {
			action.run();
		} catch (I18NRuntimeException ex) {
			return ex;
		}
		fail("Expected failure.");
		return null;
	}

	private static void setCipherText(KnowledgeItem row, String cipherText) {
		try (Transaction tx = kb().beginTransaction()) {
			row.setAttributeValue(DatabaseSecretStore.CIPHER_TEXT_ATTR, cipherText);
			tx.commit();
		}
	}

	private static SecretStoreService newService(String prefix) {
		ChainSecretStore.Config chain = TypedConfiguration.newConfigItem(ChainSecretStore.Config.class);
		chain.setPrefix(prefix);
		chain.getStores().add(TypedConfiguration.newConfigItem(EnvironmentSecretStore.Config.class));
		chain.getStores().add(storeConfig());
		SecretStoreService.Config config = TypedConfiguration.newConfigItem(SecretStoreService.Config.class);
		config.setStore(chain);
		return new SecretStoreService(CONTEXT, config);
	}

	private static DatabaseSecretStore newStore() {
		return (DatabaseSecretStore) CONTEXT.getInstance(storeConfig());
	}

	private static DatabaseSecretStore.Config storeConfig() {
		DatabaseSecretStore.Config config = TypedConfiguration.newConfigItem(DatabaseSecretStore.Config.class);
		config.setKeySecret(KEY_SECRET);
		return config;
	}

	private static List<KnowledgeItem> rows(String name) {
		List<KnowledgeItem> result = new ArrayList<>();
		Iterator<KnowledgeItem> it =
			kb().getObjectsByAttribute(DatabaseSecretStore.TABLE, DatabaseSecretStore.NAME_ATTR, name);
		it.forEachRemaining(result::add);
		return result;
	}

	private static List<KnowledgeItem> rows() {
		return new ArrayList<>(kb().getAllKnowledgeObjects(DatabaseSecretStore.TABLE));
	}

	private static String newKey() throws NoSuchAlgorithmException {
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(256);
		return Base64.getEncoder().encodeToString(generator.generateKey().getEncoded());
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(TestDatabaseSecretStore.class);
	}

}
