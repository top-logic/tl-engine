/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.layout.form.model.SelectField;

/**
 * {@link OptionModel} wrapping a {@link List} of options for a {@link SelectField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ListOptionModel<T> extends OptionModel<T> {

	@Override
	public List<? extends T> getBaseModel();

	@Override
	default int getOptionCount() {
		return getBaseModel().size();
	}

	@Override
	default Iterator<T> iterator() {
		return Collections.<T> unmodifiableList(getBaseModel()).iterator();
	}

}
