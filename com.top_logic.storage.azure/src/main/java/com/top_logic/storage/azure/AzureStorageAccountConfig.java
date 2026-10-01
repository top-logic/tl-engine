/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.azure;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;

/**
 * Settings for accessing an Azure storage account.
 *
 * <p>
 * The account is either given by a connection string, or by its account name and account key with
 * an optional endpoint. Exactly one of both forms is used.
 * </p>
 *
 * @implNote The client for the account is created by {@link AzureStorageAccount}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AzureStorageAccountConfig extends ConfigurationItem {

	/**
	 * Configuration name of {@link #getConnectionString()}.
	 */
	String CONNECTION_STRING = "connection-string";

	/**
	 * Configuration name of {@link #getEndpoint()}.
	 */
	String ENDPOINT = "endpoint";

	/**
	 * Configuration name of {@link #getAccountName()}.
	 */
	String ACCOUNT_NAME = "account-name";

	/**
	 * Configuration name of {@link #getAccountKey()}.
	 */
	String ACCOUNT_KEY = "account-key";

	/**
	 * The connection string of the storage account, as shown in the access keys of the account in
	 * the Azure portal, e.g.
	 * <code>DefaultEndpointsProtocol=https;AccountName=myaccount;AccountKey=...;EndpointSuffix=core.windows.net</code>.
	 *
	 * <p>
	 * The connection string contains the account key, so the value is given encrypted in the
	 * configuration, or as plain text with the prefix <code>unencrypted:</code>. Empty if the
	 * account is given by its account name and account key. A connection string with a shared
	 * access signature instead of an account key grants access as well, but allows no direct
	 * downloads.
	 * </p>
	 */
	@Name(CONNECTION_STRING)
	@Encrypted
	String getConnectionString();

	/**
	 * @see #getConnectionString()
	 */
	void setConnectionString(String value);

	/**
	 * The URL of the blob service of the storage account, e.g.
	 * <code>https://myaccount.blob.core.windows.net</code>.
	 *
	 * <p>
	 * Empty for the default endpoint of an account in the Azure cloud, which is derived from the
	 * account name. Required for other clouds and for the storage emulator Azurite, where the
	 * account name is part of the path (<code>http://127.0.0.1:10000/devstoreaccount1</code>). Only
	 * used together with account name and account key.
	 * </p>
	 */
	@Name(ENDPOINT)
	String getEndpoint();

	/**
	 * @see #getEndpoint()
	 */
	void setEndpoint(String value);

	/**
	 * The name of the storage account.
	 *
	 * <p>
	 * Required, if no connection string is given.
	 * </p>
	 */
	@Name(ACCOUNT_NAME)
	String getAccountName();

	/**
	 * @see #getAccountName()
	 */
	void setAccountName(String value);

	/**
	 * An access key of the storage account.
	 *
	 * <p>
	 * Required, if no connection string is given. The value is given encrypted in the
	 * configuration, or as plain text with the prefix <code>unencrypted:</code>.
	 * </p>
	 */
	@Name(ACCOUNT_KEY)
	@Encrypted
	String getAccountKey();

	/**
	 * @see #getAccountKey()
	 */
	void setAccountKey(String value);

}
