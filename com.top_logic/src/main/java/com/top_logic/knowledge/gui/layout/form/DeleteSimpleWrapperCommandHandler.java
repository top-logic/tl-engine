/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.form;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.form.component.AbstractDeleteCommandHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * The DeleteSimpleWrapperCommandHandler just deletes a Wrapper.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class DeleteSimpleWrapperCommandHandler extends AbstractDeleteCommandHandler {

    public static final String COMMAND = "DeleteSimpleWrapper";

    public DeleteSimpleWrapperCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public void deleteObject(LayoutComponent component, Object model, Map<String, Object> arguments) {
		checkTLObject(model);
		((TLObject) model).tDelete();
	}

	@Override
	protected void deleteObjects(LayoutComponent component, Iterable<?> elements, Map<String, Object> arguments) {
		elements.forEach(this::checkTLObject);
		@SuppressWarnings("unchecked")
		Iterable<? extends TLObject> items = (Iterable<? extends TLObject>) elements;
		KBUtils.deleteAll(items);
    }

	private void checkTLObject(Object model) {
		if (!(model instanceof TLObject)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_BUSINESS_OBJECT__ELEMENT.fill(model));
		}
	}

}
