/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

import java.util.List;

/**
 * This class is a container for select field options, stored as list.
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class DefaultListOptionModel<T> implements ListOptionModel<T> {

	private List<? extends T> options;
	
	/**
	 * Create a new DefaultListOptionModel
	 *  
	 * @param options - the select field options
	 */
	public DefaultListOptionModel(List<? extends T> options) {
		assert (options != null) : "Options must not be null";
		this.options = options;
	}
	
	@Override
	public List<? extends T> getBaseModel() {
		return options;
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
