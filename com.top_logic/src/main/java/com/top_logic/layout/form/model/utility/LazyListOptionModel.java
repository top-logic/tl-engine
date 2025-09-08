/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.List;
import java.util.function.Supplier;

/**
 * {@link ListOptionModel} whose list is dynamically fetched by a given {@link Supplier} at each
 * call.
 * 
 * @see DefaultListOptionModel List option model with constant options
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LazyListOptionModel<T> implements ListOptionModel<T> {

	private final Supplier<? extends List<? extends T>> _options;

	private List<? extends T> _cachedBaseModel;

	/**
	 * Creates a new {@link LazyListOptionModel}.
	 * 
	 * @param options
	 *        {@link Supplier} delivering the actual options.
	 * 
	 */
	public LazyListOptionModel(Supplier<? extends List<? extends T>> options) {
		_options = options;
	}

	@Override
	public List<? extends T> getBaseModel() {
		if (_cachedBaseModel == null) {
			_cachedBaseModel = _options.get();
			if (_cachedBaseModel == null) {
				throw new NullPointerException("Option supplier delivers null options: " + _options);
			}
		}
		return _cachedBaseModel;
	}

	@Override
	public void resetBaseModel() {
		_cachedBaseModel = null;
	}

	@Override
	public boolean addOptionListener(OptionModelListener listener) {
		return false;
	}

	@Override
	public boolean removeOptionListener(OptionModelListener listener) {
		return false;
	}


}

