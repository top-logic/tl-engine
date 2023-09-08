/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.view.component.property.BooleanProperty;
import com.top_logic.reporting.view.component.property.DateProperty;
import com.top_logic.reporting.view.component.property.FilterProperty;
import com.top_logic.reporting.view.component.property.IntRangeProperty;
import com.top_logic.util.Utils;

/**
 * {@link FilterVO} extension that is able to filter elements.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public abstract class FilteringFilterVO<T> extends FilterVO {

	private Map<String, FilterProperty> properties;

	/**
	 * Creates a new {@link FilteringFilterVO}.
	 */
	public FilteringFilterVO(Object aModel) {
		super(aModel);
		properties = new HashMap<>();
	}

	/**
	 * Filter elements.
	 * 
	 * @param anElement
	 *        an element
	 * @param aHolder
	 *        the element's holder
	 * @return true if the element passes the checks
	 */
	public abstract boolean acceptElement(T anElement, Object aHolder);

	/**
	 * Filter holders.
	 * 
	 * @param aHolder
	 *        the holder
	 * @return true if the holder passes the checks
	 */
	public abstract boolean acceptHolder(Object aHolder);

	public Collection<FilterProperty> getProperties() {
		return properties.values();
	}

	protected void addProperty(FilterProperty aProperty) {
		properties.put(aProperty.getName(), aProperty);
	}

	public FilterProperty getProperty(String aName) {
		return properties.get(aName);
	}

	/**
	 * Gets the values map.
	 */
	public Map<String, FilterProperty> getAllProperties() {
		return properties;
	}

	/**
	 * Sets the {@link FilterProperty}s for the {@link FilteringFilterVO}.
	 * 
	 * @param props
	 *        The {@link Collection} of {@link FilterProperty}s.
	 */
	public void setProperties(Collection<FilterProperty> props) {
		for (FilterProperty property : props) {
			properties.put(property.getName(), property);
		}
	}

	/**
	 * Method to get {@link FilterProperty}s of type Integer (e.g. {@link IntRangeProperty}) by name
	 */
	public int getInteger(String propertyName) {
		FilterProperty filterProperty = properties.get(propertyName);
		return Utils.getintValue(filterProperty != null ? filterProperty.getValue() : null);
	}

	/**
	 * Method to get {@link FilterProperty}s of type Boolean (e.g. {@link BooleanProperty}) by name
	 */
	public boolean getBoolean(String propertyName) {
		FilterProperty filterProperty = properties.get(propertyName);
		return Utils.getbooleanValue(filterProperty != null ? filterProperty.getValue() : null);
	}

	/** Method to get {@link FilterProperty}s of type Date (e.g. {@link DateProperty}) by name */
	public Date getDate(String propertyName) {
		FilterProperty filterProperty = properties.get(propertyName);
		return Utils.getDateValue(filterProperty != null ? filterProperty.getValue() : null);
	}

	/** Method to get {@link FilterProperty}s by name */
	public Object getValue(String propertyName) {
		FilterProperty filterProperty = properties.get(propertyName);
		return filterProperty != null ? filterProperty.getValue() : null;
	}

}
