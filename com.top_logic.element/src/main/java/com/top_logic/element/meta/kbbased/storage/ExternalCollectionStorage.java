/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.DataObject;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.element.config.annotation.LocatorNameFormat;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ExternalStorage} that retrieves collection values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExternalCollectionStorage<C extends ExternalCollectionStorage.Config<?>> extends ExternalStorage<C> {

	/**
	 * Configuration options for {@link ExternalCollectionStorage}.
	 */
	@TagName("external-collection-storage")
	public interface Config<I extends ExternalCollectionStorage<?>> extends ExternalStorage.Config<I> {
		/** @see #getIteratedLocator() */
		String ITERATED_PROPERTY = "iterated";

		/**
		 * {@link AttributeValueLocator} that provides a collection of parameters for which a query
		 * is issued each.
		 */
		@Format(LocatorNameFormat.class)
		@Name(ITERATED_PROPERTY)
		AttributeValueLocator getIteratedLocator();
	}

	/** Name to be used for iterated param in DAP query Strings. */
	public static final String ITERATED_PARAM = "iterated";

	private final AttributeValueLocator _iterationLocator;

	/**
	 * Creates a {@link ExternalCollectionStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExternalCollectionStorage(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);

		AttributeValueLocator iteratedLocator = config.getIteratedLocator();
		if (iteratedLocator == null) {
			throw new ConfigurationException("Locator 'iterated' must be defined");
		}
		_iterationLocator = iteratedLocator;
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute)
			throws AttributeException {
		// Get the basic query with parameter wildcards
		String theBasicQuery = computeQueryTemplate(object);
		
		Set<Object> result = new HashSet<>();
		try {
			Map<String, Object> arguments = computeArguments(object);
			Collection<String> iteration = computeIteration(object);
			for (String iterationValue : iteration) {
				arguments.put(ITERATED_PARAM, iterationValue);
				String query = fillArguments(theBasicQuery, arguments);
		
				DataAccessProxy theDAP = new DataAccessProxy(query);
				DataObject theDO = theDAP.getObjectEntry();
				Object theInnerRes = computeResult(theDO);
				if (theInnerRes != null) {
					result.add(theInnerRes);
				}
			}
		} catch (Exception ex) {
			throw new AttributeException("Failed to get value from DAP: " + ex.getMessage(), ex);
		}
		
		return result;
	}

	protected final Collection<String> computeIteration(TLObject object) {
		return (Collection) _iterationLocator.locateAttributeValue(object);
	}

}
