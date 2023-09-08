/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.Iterator;

/**
 * {@link OptionModel} implementation delegating calls to another {@link OptionModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class OptionModelProxy<T> implements OptionModel<T> {

	@Override
	public Iterator<T> iterator() {
		return delegate().iterator();
	}

	@Override
	public Object getBaseModel() {
		return delegate().getBaseModel();
	}

	@Override
	public int getOptionCount() {
		return delegate().getOptionCount();
	}

	@Override
	public boolean addOptionListener(OptionModelListener listener) {
		return delegate().addOptionListener(listener);
	}

	@Override
	public boolean removeOptionListener(OptionModelListener listener) {
		return delegate().removeOptionListener(listener);
	}

	/**
	 * The {@link OptionModel} to delegate methods to. Repeated calls of this method delivers the
	 * same {@link OptionModel} instance.
	 * 
	 * @return Must not be <code>null</code>.
	 */
	public abstract OptionModel<T> delegate();

}

