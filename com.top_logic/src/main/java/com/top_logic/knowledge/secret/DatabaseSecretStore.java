/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.secret;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.credential.AbstractSecretStore;
import com.top_logic.basic.credential.ChainSecretStore;
import com.top_logic.basic.credential.SecretStore;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.credential.WritableSecretStore;
import com.top_logic.basic.encryption.AuthenticatedEncryption;
import com.top_logic.basic.encryption.InvalidCipherTextException;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;

/**
 * {@link WritableSecretStore} keeping the secrets encrypted in the database of the application.
 *
 * <p>
 * Each secret is a row of the table {@link #TABLE} holding the name of the secret and its cipher
 * text, the secret encrypted with {@link AuthenticatedEncryption} (AES in GCM mode). The secrets
 * are therefore shared by all nodes of a cluster and backed up with the database. The table is
 * not versioned: a replaced or removed secret leaves no cipher text behind. Its rows are no model
 * objects and cannot be reached through the model, TL-Script or the generic UI.
 * </p>
 *
 * <p>
 * The encryption key is never stored in the database. It is itself a named secret (see
 * {@link Config#getKeySecret()}), the Base64 encoding of an AES key of 128, 192 or 256 bits,
 * delivered by the other stores of the installation, e.g. created with
 * <code>openssl rand -base64 32</code>. The store never holds its own key, and while it fetches
 * its key, it holds no secret at all. A secret that does not authenticate with the key - the
 * cipher text was modified, was written with another key, or was copied from the row of another
 * secret - is reported as error naming the secret.
 * </p>
 *
 * <p>
 * The store is placed last in the {@link ChainSecretStore} of the {@link SecretStoreService},
 * behind the read-only stores delivering its key and the secrets needed before the knowledge base
 * is started (e.g. the database password). While the knowledge base is not started, the store
 * holds no secrets.
 * </p>
 *
 * <pre>
 * &lt;config service-class="com.top_logic.basic.credential.SecretStoreService"&gt;
 *   &lt;instance class="com.top_logic.basic.credential.SecretStoreService"&gt;
 *     &lt;store class="com.top_logic.basic.credential.ChainSecretStore"&gt;
 *       &lt;stores&gt;
 *         &lt;env-store/&gt;
 *         &lt;database-store key-secret="tl_secret_store_key"/&gt;
 *       &lt;/stores&gt;
 *     &lt;/store&gt;
 *   &lt;/instance&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * <p>
 * Each write runs in a transaction of its own. A secret is written and read under its name with
 * the {@link Config#getPrefix() prefix} of the store.
 * </p>
 *
 * @see SecretStoreService#getWritableStore()
 */
@Label("Secrets in the database")
public class DatabaseSecretStore extends AbstractSecretStore<DatabaseSecretStore.Config>
		implements WritableSecretStore {

	/**
	 * Configuration options for {@link DatabaseSecretStore}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AbstractSecretStore.Config<DatabaseSecretStore> {

		/**
		 * Tag name of this configuration in a list of {@link SecretStore}s.
		 */
		String TAG_NAME = "database-store";

		/**
		 * @see #getKeySecret()
		 */
		String KEY_SECRET = "key-secret";

		/**
		 * Name of the secret holding the encryption key of this store, the Base64 encoding of an
		 * AES key of 128, 192 or 256 bits.
		 *
		 * <p>
		 * The key is fetched from the {@link SecretStoreService} like any named secret, so it is
		 * delivered by the other stores of the installation or by an override of the service.
		 * </p>
		 */
		@Name(KEY_SECRET)
		@StringDefault(DEFAULT_KEY_SECRET)
		String getKeySecret();

		/**
		 * @see #getKeySecret()
		 */
		void setKeySecret(String value);

	}

	/**
	 * Default of {@link Config#getKeySecret()}.
	 */
	public static final String DEFAULT_KEY_SECRET = "tl_secret_store_key";

	/**
	 * Name of the table holding the secrets.
	 */
	public static final String TABLE = "StoredSecret";

	/**
	 * Column of {@link #TABLE} holding the name of a secret (with the prefix of the store).
	 */
	public static final String NAME_ATTR = "name";

	/**
	 * Column of {@link #TABLE} holding the Base64 encoded cipher text of a secret.
	 */
	public static final String CIPHER_TEXT_ATTR = "cipherText";

	/**
	 * Algorithm of the encryption key.
	 */
	private static final String KEY_ALGORITHM = "AES";

	/**
	 * Separator between the name of the secret and the secret in the encrypted plain text, binding
	 * the cipher text to the name.
	 */
	private static final char NAME_SEPARATOR = '\0';

	private static final SecureRandom RANDOM = new SecureRandom();

	private final ThreadLocal<Boolean> _resolvingKey = new ThreadLocal<>();

	/**
	 * Creates a {@link DatabaseSecretStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DatabaseSecretStore(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected String lookupKey(String key) {
		if (!holdsSecrets() || isKeySecret(key)) {
			return null;
		}
		DataObject row = PersistencyLayer.getKnowledgeBase().getObjectByAttribute(TABLE, NAME_ATTR, key);
		if (row == null) {
			return null;
		}
		return decrypt(key, (String) row.getAttributeValue(CIPHER_TEXT_ATTR));
	}

	@Override
	protected ResKey missing(String key) {
		return I18NConstants.ERROR_DATABASE_SECRET_UNDEFINED__NAME.fill(key);
	}

	@Override
	public void store(String name, String secret) {
		String key = key(name);
		if (StringServices.isEmpty(secret)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_DATABASE_SECRET_EMPTY__NAME.fill(key));
		}
		if (isKeySecret(key)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_DATABASE_SECRET_IS_KEY__NAME.fill(key));
		}
		String cipherText = encrypt(key, secret);

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction(I18NConstants.STORED_SECRET__NAME.fill(key));
		try {
			KnowledgeItem row = (KnowledgeItem) kb.getObjectByAttribute(TABLE, NAME_ATTR, key);
			if (row == null) {
				kb.createKnowledgeItem(TABLE,
					Map.<String, Object> of(NAME_ATTR, key, CIPHER_TEXT_ATTR, cipherText).entrySet(),
					KnowledgeItem.class);
			} else {
				row.setAttributeValue(CIPHER_TEXT_ATTR, cipherText);
			}
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	@Override
	public boolean remove(String name) {
		String key = key(name);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction(I18NConstants.REMOVED_SECRET__NAME.fill(key));
		try {
			KnowledgeItem row = (KnowledgeItem) kb.getObjectByAttribute(TABLE, NAME_ATTR, key);
			if (row == null) {
				return false;
			}
			row.delete();
			tx.commit();
			return true;
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Whether this store may be asked for a secret: the knowledge base is started, and this store
	 * is not fetching its own key in the current thread.
	 */
	private boolean holdsSecrets() {
		return PersistencyLayer.Module.INSTANCE.isActive() && _resolvingKey.get() == null;
	}

	private boolean isKeySecret(String key) {
		return key.equals(getConfig().getKeySecret());
	}

	private String encrypt(String key, String secret) {
		byte[] plainText = (key + NAME_SEPARATOR + secret).getBytes(StandardCharsets.UTF_8);
		try {
			return Base64.getEncoder().encodeToString(encryption().encrypt(plainText));
		} finally {
			Arrays.fill(plainText, (byte) 0);
		}
	}

	private String decrypt(String key, String cipherText) {
		byte[] plainText;
		try {
			plainText = encryption().decrypt(Base64.getDecoder().decode(cipherText));
		} catch (InvalidCipherTextException | IllegalArgumentException ex) {
			throw notAuthentic(key);
		}
		try {
			String content = new String(plainText, StandardCharsets.UTF_8);
			int separator = content.indexOf(NAME_SEPARATOR);
			if (separator < 0 || !key.equals(content.substring(0, separator))) {
				throw notAuthentic(key);
			}
			return content.substring(separator + 1);
		} finally {
			Arrays.fill(plainText, (byte) 0);
		}
	}

	private I18NRuntimeException notAuthentic(String key) {
		return new I18NRuntimeException(I18NConstants.ERROR_DATABASE_SECRET_NOT_AUTHENTIC__NAME.fill(key));
	}

	private AuthenticatedEncryption encryption() {
		String keySecret = getConfig().getKeySecret();
		if (StringServices.isEmpty(keySecret)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_DATABASE_SECRET_KEY_NOT_CONFIGURED);
		}
		String encodedKey;
		_resolvingKey.set(Boolean.TRUE);
		try {
			encodedKey = SecretStoreService.lookupSecret(keySecret);
		} finally {
			_resolvingKey.remove();
		}
		if (encodedKey == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_DATABASE_SECRET_KEY_MISSING__KEY.fill(keySecret));
		}
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(encodedKey.trim());
		} catch (IllegalArgumentException ex) {
			throw invalidKey(keySecret);
		}
		try {
			switch (keyBytes.length) {
				case 16:
				case 24:
				case 32:
					return new AuthenticatedEncryption(RANDOM, new SecretKeySpec(keyBytes, KEY_ALGORITHM));
				default:
					throw invalidKey(keySecret);
			}
		} finally {
			Arrays.fill(keyBytes, (byte) 0);
		}
	}

	private I18NRuntimeException invalidKey(String keySecret) {
		return new I18NRuntimeException(I18NConstants.ERROR_DATABASE_SECRET_KEY_INVALID__KEY.fill(keySecret));
	}

}
