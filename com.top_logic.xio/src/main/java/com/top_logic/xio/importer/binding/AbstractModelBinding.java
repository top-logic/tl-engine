/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.Location;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.xio.importer.handlers.ImportPart;

/**
 * Base class for {@link ModelBinding} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractModelBinding implements ModelBinding {

	final TLModel _model;

	private Map<String, Object> _objects = new HashMap<>();

	/**
	 * Creates a {@link AbstractModelBinding}.
	 *
	 * @param model
	 *        The context model.
	 */
	public AbstractModelBinding(TLModel model) {
		_model = model;
	}

	@Override
	public void setReference(ImportPart handler, Object obj, String name, Object value) {
		setValue(obj, name, value);
	}

	@Override
	public void setProperty(ImportPart handler, Object obj, String name, Object value) {
		setValue(obj, name, value);
	}

	@Override
	public Object resolveObject(ImportPart handler, Location location, String id) {
		return _objects.get(id);
	}

	@Override
	public void assignId(Object value, String id) {
		if (id != null) {
			Object clash = _objects.put(id, value);
			if (clash != null && clash != value) {
				Logger.warn("Inconsistent assignment to ID '" + id + "': " + clash + ", vs. " + value,
					ApplicationModelBinding.class);
			}
		}
	}

	@Override
	public Object createObject(ImportPart handler, Location location, String modelType, String id) {
		try {
			TLClass type = (TLClass) resolveType(modelType);
			TLObject result = createObject(type);
			assignId(result, id);
			return result;
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * Instantiates the given {@link TLClass}.
	 * 
	 * @param type
	 *        The type to instantiate.
	 * @return The newly created object of the given type.
	 */
	protected abstract TLObject createObject(TLClass type);

	@Override
	public void addValue(ImportPart handler, Object obj, String part, Object element) {
		TLObject self = (TLObject) obj;
		TLStructuredTypePart referencePart = self.tType().getPartOrFail(part);
		self.tAdd(referencePart, element);
	}

	@Override
	public boolean isInstanceOf(Object obj, String type) {
		try {
			return TLModelUtil.isCompatibleInstance(resolveType(type), (TLObject) obj);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * The context model of the import.
	 */
	public final TLModel getModel() {
		return _model;
	}

	/**
	 * Resolves the {@link TLType} with the given name from the {@link #getModel()}.
	 *
	 * @param typeName
	 *        The name of the type to resolve.
	 * @return The resolved type.
	 * @throws ConfigurationException
	 *         If the given name cannot be resolved to a type.
	 */
	protected TLType resolveType(String typeName) throws ConfigurationException {
		return TLModelUtil.findType(_model, typeName);
	}

	/**
	 * Actually sets the property with the given name of the given object to the given value.
	 *
	 * @param obj
	 *        The object whose property to set.
	 * @param partName
	 *        The name of the property to assign.
	 * @param value
	 *        The new value of the property.
	 */
	protected void setValue(Object obj, String partName, Object value) {
		TLObject self = (TLObject) obj;
		TLStructuredTypePart part = self.tType().getPartOrFail(partName);
		Object oldValue = self.tValue(part);
		if (oldValue != null
			&& (!(oldValue instanceof Collection<?>) || !((Collection<?>) oldValue).isEmpty())) {
			// Assuming that null and empty are potential default values.

			Logger.warn("Overriding value of part '" + part + "' with value '" + value + "', old value was '"
				+ oldValue + "'.", ApplicationModelBinding.class);
		}
		self.tUpdate(part, value);
	}

}
