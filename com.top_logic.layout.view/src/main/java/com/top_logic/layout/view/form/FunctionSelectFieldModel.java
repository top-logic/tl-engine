/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.top_logic.layout.form.model.SelectFieldModel;

/**
 * A {@link FunctionFieldModel} whose value is chosen from options.
 *
 * <p>
 * The field holds its value as the selection a select control edits, see
 * {@link SelectionValues}: the read function's value is offered as the list of the chosen options,
 * and a selection is handed to the write function as the value it stands for - a single object, or
 * nothing, for a field holding one value, a list for a field holding several.
 * </p>
 */
public class FunctionSelectFieldModel extends FunctionFieldModel implements SelectFieldModel {

	private final boolean _multiple;

	private List<?> _options;

	private List<SelectOptionsListener> _optionsListeners = Collections.emptyList();

	/**
	 * Creates a {@link FunctionSelectFieldModel}.
	 *
	 * @param object
	 *        The object the value is read from and written to.
	 * @param read
	 *        Computes the value the object currently holds.
	 * @param write
	 *        Writes an edited value, receiving the object and the new value.
	 * @param options
	 *        The values to choose from.
	 * @param multiple
	 *        Whether several values are chosen, which the read function delivers and the write
	 *        function receives as a collection.
	 */
	public FunctionSelectFieldModel(Object object, Function<Object, Object> read, BiConsumer<Object, Object> write,
			List<?> options, boolean multiple) {
		super(object, read.andThen(SelectionValues::selection),
			(target, selection) -> write.accept(target, SelectionValues.value(selection, multiple)));
		_options = options == null ? Collections.emptyList() : options;
		_multiple = multiple;
	}

	@Override
	public List<?> getOptions() {
		return _options;
	}

	@Override
	public boolean isMultiple() {
		return _multiple;
	}

	@Override
	public void setOptions(List<?> options) {
		_options = options == null ? Collections.emptyList() : options;
		for (SelectOptionsListener listener : _optionsListeners.toArray(new SelectOptionsListener[0])) {
			listener.onOptionsChanged(this, _options);
		}
	}

	@Override
	public void addOptionsListener(SelectOptionsListener listener) {
		if (_optionsListeners.isEmpty()) {
			_optionsListeners = new ArrayList<>(2);
		}
		_optionsListeners.add(listener);
	}

	@Override
	public void removeOptionsListener(SelectOptionsListener listener) {
		_optionsListeners.remove(listener);
	}

}
