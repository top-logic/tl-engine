/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.form.component.AbstractDeleteCommandHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;


/**
 * Standard definition of a delete handler for
 * {@link com.top_logic.contact.business.AbstractContact}s.
 * 
 * @author <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ContactDeleteHandler extends AbstractDeleteCommandHandler {

    public ContactDeleteHandler (InstantiationContext context, Config config) {
		super(context, config);
    }

    /** 
     * We can focus on the super type AttributedWrapper. 
     * @see AbstractDeleteCommandHandler#deleteObject(LayoutComponent, java.lang.Object, Map)
     */
    @Override
	protected void deleteObject(LayoutComponent component, Object model, Map<String, Object> arguments) {
		checkAbstractContact(model);
		((AbstractContact) model).tDelete();
	}

	@Override
	protected void deleteObjects(LayoutComponent component, Iterable<?> elements, Map<String, Object> arguments) {
		elements.forEach(this::checkAbstractContact);
		@SuppressWarnings("unchecked")
		Iterable<? extends TLObject> contacts = (Iterable<? extends TLObject>) elements;
		KBUtils.deleteAll(contacts);
	}

	private void checkAbstractContact(Object model) {
		if (!(model instanceof AbstractContact)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_CONTACT__ELEMENT.fill(model));
        }
	}
}
