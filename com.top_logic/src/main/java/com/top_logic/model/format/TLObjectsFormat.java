/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.format;

import static com.top_logic.basic.col.factory.CollectionFactory.*;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.model.TLObject;

/**
 * {@link ConfigurationValueProvider} that serializes {@link Collection}s of {@link TLObject}s to
 * the configuration.
 * <p>
 * Uses {@link TLObjectFormat} for each entry.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLObjectsFormat extends AbstractConfigurationValueProvider<Collection<TLObject>> {

	/** Separator between two {@link TLObject}s. */
	public static final String SEPARATOR = ";";

	/** The {@link TLObjectsFormat} instance. */
	public static final TLObjectsFormat INSTANCE = new TLObjectsFormat();

	private TLObjectsFormat() {
		super(Collection.class);
	}

	@Override
	protected Collection<TLObject> getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	@Override
	protected Collection<TLObject> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		Set<TLObject> result = set();
		String[] formattedObjects = propertyValue.toString().split(SEPARATOR);
		int i = 0;
		for (String formattedObject : formattedObjects) {
			result.add(read(propertyName + "[" + i + "]", formattedObject));
			i += 1;
		}
		return result;
	}

	private TLObject read(String propertyName, String formattedObject) throws ConfigurationException {
		return TLObjectFormat.INSTANCE.getValue(propertyName, formattedObject);
	}

	@Override
	protected String getSpecificationNonNull(Collection<TLObject> objects) {
		Collection<String> formattedObjects = list();
		for (TLObject object : objects) {
			formattedObjects.add(write(object));
		}
		return StringServices.join(formattedObjects, SEPARATOR);
	}

	private String write(TLObject object) {
		return TLObjectFormat.INSTANCE.getSpecification(object);
	}

	@Override
	public Collection<TLObject> defaultValue() {
		return set();
	}

}
