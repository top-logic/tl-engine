/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.dump;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.version.Version;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.migration.KnowledgeBaseDumper;

/**
 * The dump of all data of a {@link KnowledgeBase} as a file: a gzip-compressed XML document written
 * by the {@link KnowledgeBaseDumper}.
 *
 * <p>
 * The file is produced while it is read: {@link BinaryData#deliverTo(OutputStream)} writes the
 * dump directly to the given stream, so a dump of any size is delivered without being buffered
 * in memory or in a temporary file. Its size is therefore unknown in advance.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KBDump {

	/**
	 * Gzip file extension.
	 */
	public static final String GZ_EXT = ".gz";

	/**
	 * Dump file extension.
	 */
	public static final String DUMP_EXT = "-dump.xml";

	/**
	 * The names of the types and tables that a dump leaves out unless told otherwise.
	 *
	 * <p>
	 * The {@link SecurityStorage} is computed from the role rules and is built again after an
	 * import.
	 * </p>
	 */
	public static final Set<String> DEFAULT_EXCLUDE_TYPES = Collections.singleton(SecurityStorage.TABLE_NAME);

	/**
	 * The name of a dump of the given {@link KnowledgeBase} taken now: the name and version of the
	 * application and the last committed revision, e.g. <code>my-app-1.0-r1234-dump.xml.gz</code>.
	 */
	public static String fileName(KnowledgeBase kb) {
		Version version = Version.getApplicationVersion();
		long revision = kb.getHistoryManager().getLastRevision();
		return version.getName() + "-" + version.getVersionString() + "-r" + revision + DUMP_EXT + GZ_EXT;
	}

	/**
	 * The dump of the given {@link KnowledgeBase}, written when the file is read.
	 *
	 * @param kb
	 *        The {@link KnowledgeBase} to dump.
	 * @param excludeTypes
	 *        The names of the types and tables that must not be exported; <code>null</code> for the
	 *        {@link #DEFAULT_EXCLUDE_TYPES}.
	 * @return The file whose content is the dump, named by {@link #fileName(KnowledgeBase)}.
	 */
	public static BinaryData newDump(KnowledgeBase kb, Set<String> excludeTypes) {
		String name = fileName(kb);
		Set<String> ignored = excludeTypes == null ? DEFAULT_EXCLUDE_TYPES : excludeTypes;
		BinaryDataSource source = new BinaryDataSource() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public long getSize() {
				return -1;
			}

			@Override
			public String getContentType() {
				return BinaryData.CONTENT_TYPE_OCTET_STREAM;
			}

			@Override
			public void deliverTo(OutputStream out) throws IOException {
				KnowledgeBaseDumper dumper = new KnowledgeBaseDumper(kb);
				dumper.setIgnoreTypes(ignored);
				try (GZIPOutputStream zipOut = new GZIPOutputStream(out)) {
					dumper.dump(zipOut);
				}
			}
		};
		// The upgrade delivers by writing the dump to the stream of the consumer, a stream to read
		// from is only made on demand.
		return source.toData();
	}

}
