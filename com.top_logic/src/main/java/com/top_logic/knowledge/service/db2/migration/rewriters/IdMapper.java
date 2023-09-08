/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import com.top_logic.basic.TLID;

/**
 * Mapping from source {@link TLID} to target {@link TLID}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface IdMapper {

	/**
	 * Returns the target {@link TLID} for the given source {@link TLID}.
	 * 
	 * @param dumpId
	 *        The source {@link TLID} to get target id for.
	 * 
	 * @return May be <code>null</code>, in case there is no target {@link TLID} for the given
	 *         source {@link TLID}.
	 */
	TLID mapId(TLID dumpId);

	/**
	 * Same as {@link #mapId(TLID)} but creates a new {@link TLID} for an unknown source
	 * {@link TLID}. Later calls to {@link #mapId(TLID)} of {@link #mapOrCreateId(TLID)} will return
	 * the same target {@link TLID}.
	 */
	TLID mapOrCreateId(TLID dumpId);

}
