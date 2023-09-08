/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.logging.LogUtil;

/**
 * Utilities used for synchronization of different TL systems.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLSyncUtils {

	/**
	 * The start of TL-Sync messages.
	 * <p>
	 * This can be used to quickly identify these messages, for example when analyzing problems.
	 * </p>
	 */
	public static final String MESSAGE_TYPE = "TL-Sync";

	/**
	 * The major version number of TL-Sync messages.
	 * 
	 * @see #MESSAGE_CURRENT_VERSION
	 */
	public static final String MESSAGE_CURRENT_MAJOR_VERSION = "2";

	/**
	 * The version of the TL-Sync message.
	 * <p>
	 * {@link #MESSAGE_LEGACY_VERSION_1 Version 1} was before ticket #27154.
	 * </p>
	 * <p>
	 * Increment the <em>major version</em> with every change of the message protocol that is
	 * <em>not backwards compatible</em>. That means, if both the sender and the receiver have to be
	 * updated to prevent the communication from failing.
	 * </p>
	 * <p>
	 * Increment the <em>minor version</em> with every <em>feature</em> change of the message
	 * protocol that is backwards compatible. That means, if it causes no problem if only the sender
	 * or only the receiver use the newer version of the protocol. They may not profit from the
	 * change until both are updated, but nothing will fail if only one of them is updated.
	 * </p>
	 * <p>
	 * Increment the patch number with every other change of the message protocol. That means, bug
	 * fixes that cause no problem if only the sender or only the receiver use the newer version of
	 * the protocol. They may not profit from the bug fix until both are updated, but nothing will
	 * fail if only one of them is updated.
	 * </p>
	 * <p>
	 * See <a href="https://semver.org/">Semantic versioning</a> for details:
	 * <a href="https://en.wikipedia.org/wiki/Semantic_versioning">Wikipedia</a>
	 * </p>
	 */
	public static final String MESSAGE_CURRENT_VERSION = MESSAGE_CURRENT_MAJOR_VERSION + ".0.0";

	/**
	 * The first version of TL-Sync messages.
	 * <p>
	 * It did not allow detection of missing messages.
	 * </p>
	 */
	public static final String MESSAGE_LEGACY_VERSION_1 = "1.0.0";

	/**
	 * The separator between the {@link #MESSAGE_TYPE}, the {@link #MESSAGE_CURRENT_VERSION}
	 * and potential other fields in the serialized message.
	 */
	public static final char MESSAGE_FIELD_SEPARATOR = ';';

	/** The {@link Charset} for the headers of a TL-Sync message. */
	public static final Charset MESSAGE_HEADER_CHARSET = StandardCharsets.UTF_8;

	/** The {@link LogUtil#withLogMark(String, String, Runnable) log mark} for TL-Sync. */
	public static final String LOG_MARK_TL_SYNC = "in-tl-sync-context";

	/** Common prefix for all properties in the {@link DBProperties} used in TL sync. */
	public static final String DB_PROPERTIES_PREFIX = "TLSync.";

	/**
	 * Common prefix for all properties storing system local informations received by the TL system
	 * with the given identifier.
	 */
	public static String getSourceSystemPrefix(long systemId) {
		return DB_PROPERTIES_PREFIX + "source" + systemId + ".";
	}

	/**
	 * Property storing the last successful processed revision received from the system with the
	 * given identifier.
	 */
	public static String getProcessedRevisionKey(long systemId) {
		return getSourceSystemPrefix(systemId) + "lastProcessedRev";
	}

	/**
	 * Property storing the last successful sent revision and date.
	 */
	public static String getLastSentRevisionAtDateKey() {
		return DB_PROPERTIES_PREFIX + "lastSentRevisionAtDate";
	}

	/**
	 * Property storing the "lock" for the property {@link #getLastSentRevisionAtDateKey()}.
	 */
	public static String getLastSentRevisionAtDateLockKey() {
		return DB_PROPERTIES_PREFIX + "lastSentRevisionAtDate.lock";
	}

	/**
	 * Property in {@link DBProperties} for {@link TLSyncRecord#getLastMessageRevision()}.
	 */
	public static String getLastMessageRevisionKey() {
		return DB_PROPERTIES_PREFIX + "lastSendMessageRevision";
	}

}
