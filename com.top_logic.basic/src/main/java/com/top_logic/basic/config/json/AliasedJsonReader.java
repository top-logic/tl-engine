/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.io.IOException;

import com.top_logic.basic.shared.io.R;
import com.top_logic.basic.vars.IVariableExpander;
import com.top_logic.basic.vars.VariableExpander;
import com.top_logic.common.json.gstream.JsonReader;

/**
 * {@link JsonReader} expanding {@link #nextString()} and {@link #nextName()} using a
 * {@link VariableExpander}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AliasedJsonReader extends JsonReader {

	private IVariableExpander _expander;

	/**
	 * Creates a new {@link AliasedJsonReader}.
	 */
	public AliasedJsonReader(R in, IVariableExpander expander) {
		super(in);
		_expander = expander;
	}

	@Override
	public String nextName() throws IOException {
		return _expander.expand(super.nextName());
	}

	@Override
	public String nextString() throws IOException {
		return _expander.expand(super.nextString());
	}

}

