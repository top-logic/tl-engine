/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.converters;

import com.google.inject.Inject;

import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;
import com.top_logic.knowledge.service.db2.migration.rewriters.IdMapper;

/**
 * {@link Mapping} that updates a {@link String} ID to a target {@link TLID}.
 * 
 * @see IdListMapping
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IdMapping implements Mapping<String, TLID> {

	private IdMapper _idMapper;

	/**
	 * Initialises the {@link IdMapper} for this {@link IdMapping}.
	 */
	@Inject
	public void initIdMapper(IdMapper idMapper) {
		_idMapper = idMapper;
	}

	@Override
	public TLID map(String input) {
		if (StringServices.isEmpty(input)) {
			return null;
		}

		TLID dumpId = StringID.fromExternalForm(input);
		return _idMapper.mapId(dumpId);
	}

}

