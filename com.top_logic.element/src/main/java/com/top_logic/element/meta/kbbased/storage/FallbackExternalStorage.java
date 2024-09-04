/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ExternalStorage} that will lookup local values first.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FallbackExternalStorage<C extends FallbackExternalStorage.Config<?>> extends ExternalStorage<C> {

	/**
	 * Configuration options for {@link FallbackExternalStorage}.
	 */
	@TagName("external-fallback-storage")
	public interface Config<I extends FallbackExternalStorage<?>> extends ExternalStorage.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link FallbackExternalStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FallbackExternalStorage(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute)
			throws AttributeException {
		Object theValue = object.tGetData(attribute.getName());
		if (theValue == null) {
			theValue = super.getAttributeValue(object, attribute);
		}
		return theValue;
	}

	@Override
	public void internalSetAttributeValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		aMetaAttributed.tSetData(attribute.getName(), aValue);
		AttributeOperations.touch(aMetaAttributed, attribute);
	}
}

