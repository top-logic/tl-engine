/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external.meta;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.contact.external.ExternalContact;
import com.top_logic.contact.external.ExternalContacts;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.element.meta.kbbased.storage.AbstractStorage;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AbstractStorage} for single {@link ExternalContact} references.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExternalContactSingletonStorage<C extends ExternalContactSingletonStorage.Config<?>>
		extends AbstractStorage<C> {

	/**
	 * Configuration options for {@link ExternalContactSingletonStorage}.
	 */
	@TagName("external-contact-singleton")
	public interface Config<I extends ExternalContactSingletonStorage<?>> extends AbstractStorage.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link ExternalContactSingletonStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExternalContactSingletonStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public final void init(TLStructuredTypePart attribute) {
		// Singleton.
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		try {
			Set theContacts =
				ExternalContacts.getCurrentContacts(KBUtils.getWrappedObjectName(object),
					KBUtils.getWrappedObjectName(attribute));
			Iterator theContactIt = theContacts.iterator();
			if (theContactIt.hasNext()) {
				return theContactIt.next();
			}
			else {
				return null;
			}
		} catch (SQLException e) {
			throw new AttributeException("Access failed.", e);
		}
	}

	@Override
	public void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		Set theValues = new HashSet();
		if (aValue != null) {
			theValues.add(aValue);
		}

		try {
			TLID objectId = KBUtils.getWrappedObjectName(object);
			TLID attributeId = KBUtils.getWrappedObjectName(attribute);
			ExternalContacts.updateContacts(objectId, attributeId, new Date(), theValues);
		} catch (SQLException e) {
			throw new AttributeException("Access failed.", e);
		}
	}

	@Override
	protected void checkSetValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object aValue) throws TopLogicException {
		// Remark: type should be checked be sub classes
		if (aMetaAttributed != null) {
			// Check value type
			if (aValue != null && !(aValue instanceof ExternalContact)) {
				throw new IllegalArgumentException("Value must be an ExternalContact for " + attribute);
			}

			// Check attribute definition
			AttributeUtil.checkHasAttribute(aMetaAttributed, attribute);
		}
	}

}
