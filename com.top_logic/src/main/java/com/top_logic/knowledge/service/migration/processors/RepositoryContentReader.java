/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.io.IOException;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Read-only access to the versioned content of a document repository, as stored by the document
 * management of former TopLogic versions.
 *
 * <p>
 * A repository stores each document under a path with a sequence of versions, starting with
 * version <code>1</code>. An implementation knows the storage layout of one kind of repository.
 * </p>
 *
 * @see MigrateDocumentContentProcessor
 * @see FileRepositoryContentReader
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RepositoryContentReader {

	/**
	 * Reads a version of a document.
	 *
	 * @param path
	 *        The path of the document in the repository, path elements are separated by
	 *        <code>/</code>.
	 * @param version
	 *        The version of the document, starting with <code>1</code>.
	 * @return The content of the given version, <code>null</code> if the repository has no such
	 *         document or version. The size of the result is the size of the stored content.
	 * @throws IOException
	 *         If accessing the repository fails.
	 */
	BinaryData read(String path, int version) throws IOException;

}
