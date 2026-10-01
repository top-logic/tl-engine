/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;

/**
 * Application configuration of the document repository, whose content is moved into the content
 * attribute of the documents by the {@link MigrateDocumentContentProcessor}.
 *
 * <p>
 * The configuration is input of the data migration only. An application that stores its document
 * repository in another way (e.g. encrypted) configures the matching reader here, so that the
 * migration script shared by all applications reads the content correctly.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DocumentRepositoryConfig extends ConfigurationItem {

	/** Configuration name of {@link #getReader()}. */
	String READER = "reader";

	/**
	 * Access to the document repository.
	 */
	@Name(READER)
	PolymorphicConfiguration<? extends RepositoryContentReader> getReader();

	/**
	 * @see #getReader()
	 */
	void setReader(PolymorphicConfiguration<? extends RepositoryContentReader> value);

}
