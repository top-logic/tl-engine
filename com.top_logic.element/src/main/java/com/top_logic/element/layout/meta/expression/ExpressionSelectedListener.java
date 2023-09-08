/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.expression;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.util.Utils;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;

/**
 * {@link ValueListener} for the {@link ExpressionSelectorComponent}: When the selected expression
 * changes, the search/report is loaded and the fields for publishing it are updated.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class ExpressionSelectedListener<W extends Wrapper> implements ValueListener {

	private final ExpressionSelectorComponent<W> _component;

	ExpressionSelectedListener(ExpressionSelectorComponent<W> component) {
		_component = component;
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		Object singleSelection = CollectionUtil.getFirst(newValue);
		Class<W> wrapperClass = _component.getWrapperClass();
		if ((singleSelection != null) && !wrapperClass.isInstance(singleSelection)) {
			error("Field " + field.getLabel() + " contains object of wrong type. Expected type: "
					+ Utils.debug(wrapperClass) + ", actual object: " + singleSelection);
			return;
		}
		W expression = wrapperClass.cast(singleSelection);
		_component.updatePublishFields(expression);
		if (!_component.isActive()) {
			return;
		}
		try {
			_component.loadExpression(expression);
		} catch (ConfigurationException ex) {
			InfoService.showError(I18NConstants.ERROR_LOADING_QUERY, ex.getErrorKey());
		}
	}

	private void error(String message) {
		Logger.error(message, ExpressionSelectedListener.class);
	}

}
