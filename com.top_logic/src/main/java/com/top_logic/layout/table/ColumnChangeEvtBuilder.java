/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.Collections;
import java.util.List;

/**
 * Builder class to create a default {@link ColumnChangeEvt}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ColumnChangeEvtBuilder implements ColumnChangeEvt {

	private TableModel _source;

	private List<String> _newValue;

	private List<String> _oldValue;

	@Override
	public List<String> oldValue() {
		return _oldValue;
	}

	@Override
	public List<String> newValue() {
		return _newValue;
	}

	@Override
	public TableModel source() {
		return _source;
	}

	/**
	 * Sets the source table model.
	 * 
	 * @see #source()
	 */
	void setSource(TableModel source) {
		_source = source;
	}

	/**
	 * Sets the list of new columns.
	 * 
	 * @see #newValue()
	 */
	void setNewValue(List<String> newValue) {
		_newValue = Collections.unmodifiableList(newValue);
	}

	/**
	 * Sets the list of old columns.
	 * 
	 * @see #oldValue()
	 */
	void setOldValue(List<String> oldValue) {
		_oldValue = Collections.unmodifiableList(oldValue);
	}

	/**
	 * Resolves the {@link ColumnChangeEvt} from this builder.
	 */
	ColumnChangeEvt toChangeEvent() {
		return this;
	}

}
