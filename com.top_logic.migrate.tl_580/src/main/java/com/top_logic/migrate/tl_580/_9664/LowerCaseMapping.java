/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580._9664;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.NullSaveMapping;

/**
 * Maps an object to the lower case variant of its {@link #toString()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LowerCaseMapping extends NullSaveMapping<Object, String> {

	/**
	 * Creates a new {@link LowerCaseMapping}.
	 */
	public LowerCaseMapping() {
		super(new Mapping<Object, String>() {

			@Override
			public String map(Object input) {
				return String.valueOf(input).toLowerCase();
			}
		});
	}

}
