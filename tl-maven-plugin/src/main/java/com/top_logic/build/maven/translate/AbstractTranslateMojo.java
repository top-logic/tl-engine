/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.translate;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;

import com.top_logic.tools.resources.translate.deepl.DeepLTranslator;

/**
 * Base class for tools using the DeepL API.
 */
public abstract class AbstractTranslateMojo extends AbstractMojo {

	/**
	 * The server ID from which to retrieve the API key (from the <code>passphrase</code> setting).
	 * 
	 * <p>
	 * The API key may also be configured directly using {@link #apiKey}.
	 * </p>
	 */
	@Parameter(defaultValue = "deepl", property = "tl.deepl.serverId")
	protected String serverId;

	/**
	 * The base URL of the DeepL API.
	 */
	@Parameter(defaultValue = "https://api.deepl.com/v2", property = "tl.deepl.apiHost")
	protected String apiHost;

	/**
	 * The API key to authenticate translation requests.
	 * 
	 * <p>
	 * Alternatively, the API key con be configured as <code>passphrase</code> for the server
	 * setting with the ID given in {@link #serverId}.
	 * </p>
	 */
	@Parameter(property = "tl.deepl.apiKey")
	protected String apiKey;

	/**
	 * The current user system settings for use in Maven.
	 */
	@Parameter(defaultValue = "${settings}", readonly = true)
	protected Settings settings;

	@Inject
	private SettingsDecrypter settingsDecrypter;

	/**
	 * The translation API.
	 */
	protected DeepLTranslator _translator;

	/**
	 * Initializes {@link #_translator}
	 */
	protected final void initTranslator() {
		DeepLTranslator.Builder builder = new DeepLTranslator.Builder();
		initTranslator(builder);
		_translator = builder.build();
	}

	/**
	 * Hook for subclasses to enhance the given builder.
	 */
	protected void initTranslator(DeepLTranslator.Builder builder) {
		builder.setApiHost(apiHost);
		builder.setApiKey(getApiKey());
	}

	/**
	 * The API key to access the translation service.
	 * 
	 * <p>
	 * The key is taken from the Maven server configuration with the {@link #serverId server ID}
	 * passed as parameter.
	 * </p>
	 */
	protected final String getApiKey() {
		if (isEmpty(apiKey)) {
			Server server = settings.getServer(serverId);
			if (server == null) {
				return null;
			}
	
			SettingsDecryptionResult serverDecrypted =
				settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(server));
	
			return serverDecrypted.getServer().getPassphrase();
		}
		return apiKey;
	}

	/**
	 * Check if a String is empty, i.e. null or <code>""</code>.
	 *
	 * @return true, if the String is null or <code>""</code>.
	 */
	public static final boolean isEmpty(CharSequence aString) {
		return ((aString == null) || aString.length() == 0);
	}

	/**
	 * Checks, whether two strings are both empty or one equal to the other.
	 *
	 * @return <code>true</code>, if both strings are empty, or one is equal to the other.
	 */
	public static boolean equalsEmpty(String s1, String s2) {
		if (isEmpty(s1)) {
			return isEmpty(s2);
		} else {
			return s1.equals(s2);
		}
	}

}
