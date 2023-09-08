/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.external.meta;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.contact.external.ExternalContact;
import com.top_logic.contact.external.ExternalContacts;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.kbbased.storage.CollectionStorage;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CollectionStorage} for {@link ExternalContact} references.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExternalContactStorage<C extends ExternalContactStorage.Config<?>> extends CollectionStorage<C> {

	/**
	 * Configuration options for {@link ExternalContactStorage}.
	 */
	@TagName("external-contact")
	public interface Config<I extends ExternalContactStorage<?>> extends CollectionStorage.Config<I> {
		// Pure marker interface.
	}


	/**
	 * Creates a {@link ExternalContactStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExternalContactStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		return internalGet(object, attribute);
	}

	@Override
	public void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValues)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		internalSet(object, attribute, new HashSet<>((Collection<?>) aValues));
	}

	private Set internalGet(TLObject object, TLStructuredTypePart attribute) {
		try {
			return ExternalContacts.getCurrentContacts(KBUtils.getWrappedObjectName(object),
				KBUtils.getWrappedObjectName(attribute));
		} catch (SQLException e) {
			throw new AttributeException("Access failed.", e);
		}
	}

	private void internalSet(TLObject object, TLStructuredTypePart attribute, Set newContacts) {
		try {
			TLID objectId = KBUtils.getWrappedObjectName(object);
			TLID attributeId = KBUtils.getWrappedObjectName(attribute);
			ExternalContacts.updateContacts(objectId, attributeId, new Date(), newContacts);
		} catch (SQLException e) {
			throw new AttributeException("Access failed.", e);
		}
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		Set values = internalGet(object, attribute);
		values.add(aValue);
		internalSet(object, attribute, values);
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		Set values = internalGet(object, attribute);
		values.remove(aValue);
		internalSet(object, attribute, values);
	}

	@Override
	protected void checkAddValue(TLObject object, TLStructuredTypePart attribute, Object value) throws TopLogicException {
		// Assume check by updating component
	}

	@Override
	protected void checkRemoveValue(TLObject object, TLStructuredTypePart attribute, Object value) throws TopLogicException {
		// Assume check by updating component
	}

	@Override
	protected void checkSetValues(TLObject object, TLStructuredTypePart attribute, Collection value) throws TopLogicException {
		// Assume check by updating component
	}

}
