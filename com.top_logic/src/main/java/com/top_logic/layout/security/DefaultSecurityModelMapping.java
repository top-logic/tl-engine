/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import com.top_logic.basic.col.DynamicCastMapping;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.NullSaveMapping;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.model.TLObject;

/**
 * Default {@link Mapping} used as {@link TableConfiguration#getModelMapping() model mapping} when
 * nothing is configured.
 * 
 * <p>
 * Expects that the input is already a {@link TLObject}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultSecurityModelMapping implements Mapping<Object, TLObject> {

	/** Singleton {@link DefaultSecurityModelMapping} instance. */
	public static final DefaultSecurityModelMapping INSTANCE = new DefaultSecurityModelMapping();

	private final Mapping<Object, ? extends TLObject> _delegate;

	private DefaultSecurityModelMapping() {
		_delegate = new NullSaveMapping<>(new DynamicCastMapping<>(TLObject.class));
	}

	@Override
	public TLObject map(Object input) {
		return _delegate.map(input);
	}

}

