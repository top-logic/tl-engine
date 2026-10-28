/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.git;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.credential.AbstractSecretStore;
import com.top_logic.basic.credential.I18NConstants;
import com.top_logic.basic.credential.WritableSecretStore;
import com.top_logic.basic.util.ResKey;

/**
 * {@link WritableSecretStore} of the tests, keeping the written secrets in memory, shared by all
 * instances.
 */
public class MemorySecretStore extends AbstractSecretStore<MemorySecretStore.Config> implements WritableSecretStore {

	/**
	 * Configuration of {@link MemorySecretStore}.
	 */
	public interface Config extends AbstractSecretStore.Config<MemorySecretStore> {
		// No further settings.
	}

	private static final Map<String, String> SECRETS = new ConcurrentHashMap<>();

	/**
	 * Creates a {@link MemorySecretStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public MemorySecretStore(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected String lookupKey(String key) {
		return SECRETS.get(key);
	}

	@Override
	protected ResKey missing(String key) {
		return I18NConstants.ERROR_SECRET_UNDEFINED__NAME.fill(key);
	}

	@Override
	public void store(String name, String secret) {
		SECRETS.put(key(name), secret);
	}

	@Override
	public boolean remove(String name) {
		return SECRETS.remove(key(name)) != null;
	}

	/**
	 * The names of the secrets written to the stores of this class.
	 */
	public static Set<String> names() {
		return Set.copyOf(SECRETS.keySet());
	}

}
