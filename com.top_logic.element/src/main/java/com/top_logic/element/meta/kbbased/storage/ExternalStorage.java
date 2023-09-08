/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.element.config.annotation.QueryParameterFormat;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.CustomAttributeValueLocator;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link AtomicStorage} that retrieves values from an external system.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExternalStorage<C extends ExternalStorage.Config<?>> extends AtomicStorage<C> {

	/**
	 * Configuration optionis for {@link ExternalStorage}.
	 */
	@TagName("external-singleton-storage")
	public interface Config<I extends ExternalStorage<?>> extends AtomicStorage.Config<I> {
		/** @see #getQuery() */
		String QUERY_PROPERTY = "query";

		/** @see #getParameters() */
		String PARAMETERS_PROPERTY = "parameters";

		/** @see #getQueryLocator() */
		String LOCATOR_PROPERTY = "locator";

		/** @see #getResultLocator() */
		String RESULT_PROPERTY = "result";

		/**
		 * The query string that retrieves the result from the external data source.
		 * 
		 * @see #getQueryLocator()
		 */
		@Name(QUERY_PROPERTY)
		String getQuery();

		/**
		 * The value providers for the {@link #getQuery()} parameters.
		 */
		@Format(QueryParameterFormat.class)
		@Name(PARAMETERS_PROPERTY)
		Map<String, AttributeValueLocator> getParameters();

		/**
		 * Optional algorithm that creates a dynamic {@link #getQuery()}.
		 */
		@Name(LOCATOR_PROPERTY)
		AttributeValueLocator getQueryLocator();

		/**
		 * The algorithm that derives the final values from the retrieved external data.
		 */
		@Name(RESULT_PROPERTY)
		AttributeValueLocator getResultLocator();
	}

	private static final String PARAMS_SEP_QUERY = "%";

	private final Map<String, AttributeValueLocator> _argumentLocators;

	private final AttributeValueLocator _queryTemplateLocator;

	private final AttributeValueLocator _resultLocator;

	/**
	 * Creates a {@link ExternalStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExternalStorage(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);

		if (StringServices.isEmpty(config.getQuery()) && (config.getQueryLocator() == null)) {
			throw new ConfigurationException("At least one of 'locator' or 'query' must be defined.");
		}

		AttributeValueLocator queryTemplateLocator = config.getQueryLocator();
		if (queryTemplateLocator == null) {
			queryTemplateLocator = new ConstantLocator(config.getQuery());
		}
		Map<String, AttributeValueLocator> argumentLocators = config.getParameters();
		AttributeValueLocator resultLocator = config.getResultLocator();

		_queryTemplateLocator = queryTemplateLocator;
		_argumentLocators = argumentLocators;
		_resultLocator = nonNull(resultLocator);
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	private AttributeValueLocator nonNull(AttributeValueLocator locator) {
		if (locator == null) {
			return SelfLocator.INSTANCE;
		}
		return locator;
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		String query = computeQuery(object);
		if (query == null) {
			return null;
		}

		{
			DataAccessProxy theDAP = new DataAccessProxy(query);
			DataObject theDO = theDAP.getObjectEntry();
			return computeResult(theDO);
		}
	}

	protected final Object computeResult(DataObject resultDO) {
		if (resultDO == null) {
			return null;
		}
		return _resultLocator.locateAttributeValue(resultDO);
	}

	/**
	 * Get the DAP query string for the current attribute on the given attributed
	 * 
	 * @param object
	 *        the attributed object. Must not be <code>null</code>
	 * @return the DAP query string
	 * @throws AttributeException
	 *         if getting the attribute value oder the DAP query fails
	 */
	protected String computeQuery(TLObject object) throws AttributeException {
		return fillArguments(computeQueryTemplate(object), computeArguments(object));
	}

	/**
	 * Get the basic query string (without param replacements)
	 * 
	 * @param object
	 *        the attributed
	 * @return the basic query string
	 */
	protected String computeQueryTemplate(TLObject object) {
		return (String) _queryTemplateLocator.locateAttributeValue(object);
	}

	/**
	 * Replace parameter wildcards with values
	 * 
	 * @param template
	 *        the query with wildcards
	 * @param arguments
	 *        the values as (parameter name, parameter value)
	 * @return the query. <code>null</code> if the query is <code>null</code> or empty or one of the
	 *         params is missing
	 * @throws IllegalArgumentException
	 *         if a param is not a String
	 */
	protected String fillArguments(String template, Map<String, Object> arguments) {
		if (StringServices.isEmpty(template)) {
			return null;
		}

		String theQuery = "";
		StringTokenizer theTok = new StringTokenizer(template, PARAMS_SEP_QUERY);
		boolean isOdd = true;
		while (theTok.hasMoreTokens()) {
			String theToken = theTok.nextToken();
			if (isOdd) {
				theQuery += theToken;
			}
			else {
				Object theValue = arguments.get(theToken);
				if (theValue == null) {
					return null;
				}

				if (!(theValue instanceof String)) {
					// TODO KBU use formatters for other value types or something like that...
					throw new IllegalArgumentException("Attribute value for " + theToken + " is not a String: "
						+ theValue);
				}

				String theStrVal = (String) theValue;
				if (StringServices.isEmpty(theStrVal)) {
					return null;
				}

				theQuery += theStrVal;
			}

			isOdd = !isOdd;
		}

		return theQuery;
	}

	/**
	 * Get the values for parameters in a (attribute name, attribute value formatted as a string)
	 * Map
	 * 
	 * @param object
	 *        the attributed object
	 * @return the values as defined above
	 * @throws AttributeException
	 *         if getting a value fails
	 */
	protected Map<String, Object> computeArguments(TLObject object) throws AttributeException {

		if (_argumentLocators != null && !_argumentLocators.isEmpty()) {
			Map<String, Object> arguments = new HashMap<>();
			for (Entry<String, AttributeValueLocator> argumentSpec : _argumentLocators.entrySet()) {
				String argument = argumentSpec.getKey();
				AttributeValueLocator argumentLocator = argumentSpec.getValue();
				arguments.put(argument, argumentLocator.locateAttributeValue(object));
			}
			return arguments;
		} else {
			return Collections.emptyMap();
		}
	}

	@Override
	public void internalSetAttributeValue(TLObject aMetaAttributed, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException,
			AttributeException {
		throw new AttributeException("setAttributeValue not supported");
	}

	@Override
	public Object getUpdateValue(AttributeUpdate update) throws NoSuchAttributeException,
			IllegalArgumentException, AttributeException {
		throw new AttributeException("getUpdateValue not supported");
	}

	@Override
	public void checkUpdate(AttributeUpdate update) {
		// Ignore
		return;
	}

	@Override
	public void update(AttributeUpdate update)
			throws AttributeException {
		// Ignore.
	}

	static final class SelfLocator extends CustomAttributeValueLocator {

		/**
		 * Singleton {@link ExternalStorage.SelfLocator} instance.
		 */
		public static final SelfLocator INSTANCE = new SelfLocator();

		private SelfLocator() {
			// Singleton constructor.
		}

		@Override
		public Object locateAttributeValue(Object anObject) {
			return anObject;
		}

	}

	private final class ConstantLocator extends CustomAttributeValueLocator {

		private final Object _value;

		public ConstantLocator(Object value) {
			_value = value;
		}

		@Override
		public Object locateAttributeValue(Object anObject) {
			return _value;
		}

	}

}
