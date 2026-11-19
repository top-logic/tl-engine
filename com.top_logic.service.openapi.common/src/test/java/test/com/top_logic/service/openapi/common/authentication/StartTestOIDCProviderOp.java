/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.common.authentication;

import java.util.List;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.credential.EnvironmentSecretStore;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;

/**
 * Script action starting a {@link TestOIDCProvider} in the JVM of the application and publishing
 * its access data as system properties.
 *
 * <p>
 * The action registers the {@link Config#getClients() configured clients} at the provider and sets
 * the system property {@link Config#getIssuerProperty()} to the {@link TestOIDCProvider#getIssuerUrl()
 * issuer URL} and, for each client, the system properties {@link Client#getIdProperty()} and
 * {@link Client#getSecretProperty()} to its ID and secret. Afterwards the aliases are reloaded, so
 * that aliases referring to these properties with <code>${env:...}</code> deliver the published
 * values. A secret referenced by name is looked up in the system properties by the
 * {@link EnvironmentSecretStore}.
 * </p>
 *
 * <p>
 * The provider lives as long as the JVM. A further execution of the action reuses the running
 * provider.
 * </p>
 */
public class StartTestOIDCProviderOp extends AbstractApplicationActionOp<StartTestOIDCProviderOp.Config> {

	/**
	 * Configuration options for {@link StartTestOIDCProviderOp}.
	 */
	public interface Config extends ApplicationAction {

		/**
		 * Configuration name of {@link #getIssuerProperty()}.
		 */
		String ISSUER_PROPERTY = "issuer-property";

		/**
		 * Configuration name of {@link #getClients()}.
		 */
		String CLIENTS = "clients";

		/**
		 * Name of the system property receiving the issuer URL of the provider.
		 */
		@Name(ISSUER_PROPERTY)
		@Mandatory
		String getIssuerProperty();

		/**
		 * The clients to register at the provider.
		 */
		@Name(CLIENTS)
		@EntryTag(Client.CLIENT)
		List<Client> getClients();

	}

	/**
	 * A client registered at the {@link TestOIDCProvider}.
	 */
	public interface Client extends ConfigurationItem {

		/**
		 * Tag name of a {@link Client} in {@link Config#getClients()}.
		 */
		String CLIENT = "client";

		/**
		 * Configuration name of {@link #getId()}.
		 */
		String ID = "id";

		/**
		 * Configuration name of {@link #getSecret()}.
		 */
		String SECRET = "secret";

		/**
		 * Configuration name of {@link #getAccount()}.
		 */
		String ACCOUNT = "account";

		/**
		 * Configuration name of {@link #getIdProperty()}.
		 */
		String ID_PROPERTY = "id-property";

		/**
		 * Configuration name of {@link #getSecretProperty()}.
		 */
		String SECRET_PROPERTY = "secret-property";

		/**
		 * The ID the client authenticates with.
		 */
		@Name(ID)
		@Mandatory
		String getId();

		/**
		 * The secret the client authenticates with.
		 */
		@Name(SECRET)
		@Mandatory
		String getSecret();

		/**
		 * Name of the account reported by the introspection of tokens issued to the client.
		 */
		@Name(ACCOUNT)
		@Nullable
		String getAccount();

		/**
		 * Name of the system property receiving {@link #getId()}.
		 */
		@Name(ID_PROPERTY)
		@Mandatory
		String getIdProperty();

		/**
		 * Name of the system property receiving {@link #getSecret()}.
		 */
		@Name(SECRET_PROPERTY)
		@Mandatory
		String getSecretProperty();

	}

	private static TestOIDCProvider _provider;

	/**
	 * Creates a {@link StartTestOIDCProviderOp} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public StartTestOIDCProviderOp(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		TestOIDCProvider provider = provider();
		for (Client client : getConfig().getClients()) {
			provider.addClient(client.getId(), client.getSecret(), client.getAccount());
			System.setProperty(client.getIdProperty(), client.getId());
			System.setProperty(client.getSecretProperty(), client.getSecret());
		}
		System.setProperty(getConfig().getIssuerProperty(), provider.getIssuerUrl());
		AliasManager.getInstance().reload();
		return argument;
	}

	private static synchronized TestOIDCProvider provider() throws Exception {
		if (_provider == null || !_provider.isRunning()) {
			TestOIDCProvider provider = new TestOIDCProvider();
			provider.start();
			_provider = provider;
		}
		return _provider;
	}

}
