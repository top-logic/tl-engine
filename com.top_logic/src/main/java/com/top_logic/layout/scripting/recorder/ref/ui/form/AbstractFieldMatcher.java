/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.form.FormMember;

/**
 * Base class for configured {@link Filter}s matching {@link FormMember}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFieldMatcher<C extends PolymorphicConfiguration<?>> implements FieldMatcher,
		ConfiguredInstance<C> {

	private final C _config;

	/**
	 * Creates a {@link AbstractFieldMatcher}.
	 */
	public AbstractFieldMatcher(C config) {
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	public String toString() {
		return _config.toString();
	}

}