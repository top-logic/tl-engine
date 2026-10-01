/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.migration;

import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.knowledge.service.migration.processors.MigrateDocumentContentProcessor;
import com.top_logic.knowledge.service.migration.processors.RepositoryContentReader;
import com.top_logic.mail.proxy.MailDataSourceAdaptor;
import com.top_logic.mail.proxy.MailReceiver;
import com.top_logic.mail.proxy.MailReceiverService;

/**
 * {@link RepositoryContentReader} reading the content of mail attachments from the mail server.
 *
 * <p>
 * The path of a document is the data source name of the attachment in the mail data source
 * without protocol, e.g. <code>INBOX?&lt;mail ID&gt;&amp;&lt;attachment number&gt;</code>. The
 * mail server is accessed with the configuration of the {@link MailReceiverService}, independent of
 * the running service. If receiving mails is not activated, or the server cannot be reached, no
 * content is delivered.
 * </p>
 *
 * @see MigrateDocumentContentProcessor
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MailContentReader extends AbstractConfiguredInstance<MailContentReader.Config<?>>
		implements RepositoryContentReader {

	/**
	 * Configuration options of {@link MailContentReader}.
	 */
	public interface Config<I extends MailContentReader> extends PolymorphicConfiguration<I> {
		// No additional options.
	}

	private MailDataSourceAdaptor _mails;

	private boolean _connected;

	/**
	 * Creates a {@link MailContentReader} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MailContentReader(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public boolean isVersioned() {
		return false;
	}

	@Override
	public BinaryData read(String path, int version) throws IOException {
		MailDataSourceAdaptor mails = mails();
		if (mails == null) {
			return null;
		}
		InputStream content;
		try {
			content = mails.getEntry(path);
		} catch (DatabaseAccessException ex) {
			Logger.warn("Cannot read mail content '" + path + "': " + ex.getMessage(), ex, MailContentReader.class);
			return null;
		}
		if (content == null) {
			return null;
		}
		try (InputStream in = content) {
			return BinaryDataFactory.createFileBasedBinaryData(in);
		}
	}

	/**
	 * Access to the mail server, <code>null</code> if the mail server cannot be accessed.
	 */
	private MailDataSourceAdaptor mails() {
		if (!_connected) {
			_connected = true;
			_mails = connect();
		}
		return _mails;
	}

	private static MailDataSourceAdaptor connect() {
		MailReceiverService.Config serverConfig;
		try {
			serverConfig = (MailReceiverService.Config) ApplicationConfig.getInstance()
				.getServiceConfiguration(MailReceiverService.class);
		} catch (ConfigurationException ex) {
			Logger.warn("No mail server configured, the content of mail attachments cannot be read.", ex,
				MailContentReader.class);
			return null;
		}
		if (!serverConfig.isActivated()) {
			Logger.warn("Receiving mails is not activated, the content of mail attachments cannot be read.",
				MailContentReader.class);
			return null;
		}
		MailReceiver receiver = new MailReceiver(serverConfig);
		try {
			if (!receiver.login()) {
				Logger.warn("Login to the mail server failed, the content of mail attachments cannot be read.",
					MailContentReader.class);
				return null;
			}
		} catch (RuntimeException ex) {
			Logger.warn("Login to the mail server failed, the content of mail attachments cannot be read.", ex,
				MailContentReader.class);
			return null;
		}
		return new MailDataSourceAdaptor(receiver);
	}

}
