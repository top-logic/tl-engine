/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.stream.Location;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.xio.importer.I18NConstants;
import com.top_logic.xio.importer.handlers.ImportPart;

/**
 * Base class for {@link ModelBinding} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractModelBinding implements ModelBinding {

	final TLModel _model;

	private final boolean _usesSecurity;

	private Map<String, Object> _objects = new HashMap<>();

	/**
	 * Creates a {@link AbstractModelBinding}.
	 *
	 * @param model
	 *        The context model.
	 * @param usesSecurity
	 *        See {@link #usesSecurity()}.
	 */
	public AbstractModelBinding(TLModel model, boolean usesSecurity) {
		_model = model;
		_usesSecurity = usesSecurity;
	}

	/**
	 * Whether the expressions of the import definition are evaluated with the current user's access
	 * rights.
	 *
	 * <p>
	 * An import definition is application configuration, not user input, and it maps an external
	 * document onto the model. An import therefore normally runs with <em>definer's rights</em>
	 * ({@code false}): the expressions resolve and write objects regardless of what the importing
	 * user may read or write, and whether that user may import at all is decided by the execution
	 * right of the enclosing command. Evaluating an import with the user's rights instead would
	 * corrupt data rather than protect it - a lookup for an existing object the user must not read
	 * silently yields nothing, so the import creates a duplicate.
	 * </p>
	 *
	 * <p>
	 * The flag is {@code true} only where the import is triggered from a context that is itself
	 * subject to the user's rights and must not become a bypass: the <i>TL-Script</i> function
	 * {@code parseXml} passes the security setting of the calling script here.
	 * </p>
	 */
	public final boolean usesSecurity() {
		return _usesSecurity;
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
	public void getProperty(ImportPart handler, Object obj, String name, Consumer<Object> continuation) {
		TLObject self = (TLObject) obj;
		TLStructuredTypePart part = self.tType().getPartOrFail(name);
		continuation.accept(self.tValue(part));
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

	/**
	 * Adds the given element to the given multiple reference, if it is not yet contained.
	 *
	 * <p>
	 * Linking is idempotent: An object that is imported multiple times (e.g. an object that joins a
	 * duplicate) is linked only once. Membership is checked with
	 * {@link TLObject#tContains(TLStructuredTypePart, Object)}, which does not need to read the
	 * whole reference.
	 * </p>
	 */
	@Override
	public void addValue(ImportPart handler, Object obj, String part, Object element) {
		TLObject self = (TLObject) obj;
		TLStructuredTypePart referencePart = self.tType().getPartOrFail(part);
		if (self.tContains(referencePart, element)) {
			return;
		}
		checkNotContainedElsewhere(self, referencePart, element);
		self.tAdd(referencePart, element);
	}

	@Override
	public boolean isInstanceOf(Object obj, String type) {
		try {
			return TLModelUtil.isCompatibleInstance(resolveType(type), obj);
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
		if (value instanceof Collection<?> collection) {
			for (Object element : collection) {
				checkNotContainedElsewhere(self, part, element);
			}
		} else {
			checkNotContainedElsewhere(self, part, value);
		}
		self.tUpdate(part, value);
	}

	/**
	 * Rejects storing the given value into a composite reference, if the value is already part of
	 * another container.
	 *
	 * <p>
	 * An object can be part of at most one container. Storing an object that is already contained
	 * in another container (or in the same container through another composite reference) into a
	 * composite reference would make it part of two containers. Storing it again into the container
	 * and reference that already holds it is not a conflict, nor is storing it into a composite
	 * reference of the container it was created in (a container known without a reference).
	 * </p>
	 *
	 * @param container
	 *        The object whose part receives the value.
	 * @param part
	 *        The part receiving the value. Nothing is checked, if it is not a composite
	 *        {@link TLReference}.
	 * @param value
	 *        The value to store. Nothing is checked, if it is not a {@link TLObject}.
	 * @throws TopLogicException
	 *         If the value is already part of another container.
	 */
	private static void checkNotContainedElsewhere(TLObject container, TLStructuredTypePart part, Object value) {
		if (!(part instanceof TLReference reference) || !reference.isComposite()) {
			return;
		}
		if (!(value instanceof TLObject element)) {
			return;
		}
		TLObject currentContainer = element.tContainer();
		if (currentContainer == null) {
			return;
		}
		TLReference currentReference = element.tContainerReference();
		boolean otherReference =
			currentReference != null && currentReference.getDefinition() != reference.getDefinition();
		if (currentContainer == container && !otherReference) {
			return;
		}
		throw new TopLogicException(
			I18NConstants.ERROR_OBJECT_ALREADY_CONTAINED__OBJ_CONTAINER_REF_NEW_CONTAINER_NEW_REF.fill(element,
				currentContainer, currentReference, container, reference));
	}

}
