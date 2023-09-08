/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime;

import java.util.Collection;
import java.util.List;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;

/**
 * Base class for {@link ActionContext} implementations.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractActionContext implements ActionContext {

	private DisplayContext _displayContext;

	/** Creates a new {@link AbstractActionContext}. */
	public AbstractActionContext(DisplayContext displayContext) {
		_displayContext = displayContext;
	}

	@Override
	public DisplayContext getDisplayContext() {
		return _displayContext;
	}

	/**
	 * @see #getDisplayContext()
	 */
	public void setDisplayContext(DisplayContext displayContext) {
		_displayContext = displayContext;
	}

	@Override
	public Object resolve(ModelName value) {
		return ModelResolver.locateModel(this, value);
	}

	@Override
	public Object resolve(ModelName value, Object valueContext) {
		return ModelResolver.locateModel(this, valueContext, value);
	}

	@Override
	public <T> List<T> resolveCollection(Class<T> exprectedType, Collection<? extends ModelName> values) {
		return ModelResolver.locateModels(this, exprectedType, values);
	}

	@Override
	public <T> List<T> resolveCollection(Class<T> exprectedType, Collection<? extends ModelName> values,
			Object valueContext) {
		return ModelResolver.locateModels(this, exprectedType, valueContext, values);
	}

}