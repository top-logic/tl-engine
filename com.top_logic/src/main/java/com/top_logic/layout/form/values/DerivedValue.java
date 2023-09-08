/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.DerivedPropertyAlgorithm;
import com.top_logic.basic.config.Updater;

/**
 * {@link Value} based on a {@link DerivedProperty}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DerivedValue<T> extends ObservableValue<T> implements ConfigurationListener {

	private final ConfigurationItem _model;

	private final DerivedPropertyAlgorithm _algorithm;

	private final Updater _updater;

	private T _value;

	private boolean _attached;

	/**
	 * Creates a {@link DerivedValue}.
	 *
	 * @param model
	 *        The {@link ConfigurationItem} model the given {@link DerivedPropertyAlgorithm}
	 *        operates on.
	 * @param algorithm
	 *        The {@link DerivedPropertyAlgorithm} computing the value.
	 * @param updater
	 *        Callback for listener registration.
	 */
	public DerivedValue(ConfigurationItem model, DerivedPropertyAlgorithm algorithm, Updater updater) {
		_model = model;
		_algorithm = algorithm;
		_updater = updater;
	}

	@Override
	public T get() {
		if (_attached) {
			return _value;
		} else {
			return lookup();
		}
	}

	@Override
	protected void startObserving() {
		super.startObserving();

		update();
		_updater.install(_model, this);
		_attached = true;
	}

	@Override
	protected void stopObserving() {
		_updater.uninstall(_model, this);
		_attached = false;

		super.stopObserving();
	}

	@Override
	public void onChange(ConfigurationChange change) {
		update();
		handleChanged();
	}

	private void update() {
		_value = lookup();
	}

	@SuppressWarnings("unchecked")
	private T lookup() {
		try {
			return (T) _algorithm.apply(_model);
		} catch (RuntimeException ex) {
			Logger.error("Cannot compute derived value for '" + _algorithm.getProperty() + "'.",
				ex, DerivedValue.class);
			return null;
		}
	}

}
