/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * {@link AbstractProtectedValueMapping} that maps the values with a given mapping for the blocked
 * value and one for the unwrapped values.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ProtectedValueMapping extends AbstractProtectedValueMapping<Object> {

	private final Mapping<Object, ?> _dispatch;

	private final Mapping<Object, ?> _blockedMapping;

	/**
	 * Creates a new {@link ProtectedValueMapping}.
	 * 
	 * <p>
	 * When a {@link ProtectedValue} is not allowed to see it is returned.
	 * </p>
	 * 
	 * @param dispatch
	 *        {@link Mapping} used to map unwrapped values.
	 */
	public ProtectedValueMapping(Mapping<Object, ?> dispatch, BoundCommandGroup requiredRight) {
		this(dispatch, Mappings.identity(), requiredRight);
	}

	/**
	 * Creates a new {@link ProtectedValueMapping}.
	 * 
	 * @param dispatch
	 *        {@link Mapping} used to map unwrapped values.
	 * @param blockedMapping
	 *        {@link Mapping} used to map {@link ProtectedValue} which the user must not see.
	 */
	public ProtectedValueMapping(Mapping<Object, ?> dispatch, Mapping<Object, ?> blockedMapping,
			BoundCommandGroup requiredRight) {
		super(requiredRight);
		_dispatch = dispatch;
		_blockedMapping = blockedMapping;
	}

	@Override
	protected Object blockedValue(ProtectedValue value) {
		return _blockedMapping.map(value);
	}

	@Override
	protected Object handleUnprotected(Object input) {
		return _dispatch.map(input);
	}

}

