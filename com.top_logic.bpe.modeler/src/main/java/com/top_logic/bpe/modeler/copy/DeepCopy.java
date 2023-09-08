/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.util.model.ModelService;

/**
 * Algorithm creating a deep copy of an object graph along its composite edges.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeepCopy {

	/**
	 * Utility method for cloning the given {@link TLObject} and all it's containments.
	 *
	 * @param orig
	 *        The original object.
	 * @return The deep clone.
	 */
	public static <T extends TLObject> T copyDeep(T orig) {
		TLFactory factory = ModelService.getInstance().getFactory();
		return copyDeep(factory, orig);
	}

	/**
	 * Utility method for cloning the given {@link TLObject} and all it's containments.
	 *
	 * @param factory
	 *        The {@link TLFactory} to use for instantiating copies.
	 * @param orig
	 *        The original object.
	 * @return The deep clone.
	 */
	public static <T extends TLObject> T copyDeep(TLFactory factory, T orig) {
		TLObject copy = new DeepCopy(factory).copy(orig);
		@SuppressWarnings("unchecked")
		T result = (T) copy;
		return result;
	}

	private Map<TLObject, TLObject> _copyByOrig = new HashMap<>();

	private TLFactory _factory;

	/**
	 * Creates a {@link DeepCopy}.
	 */
	public DeepCopy(TLFactory factory) {
		_factory = factory;
	}

	/**
	 * Creates a deep copy of the given object.
	 * 
	 * <p>
	 * If you call this method multiple times on the same instance, you are able to clone an
	 * interconnected forest of objects with only a single copy for each object.
	 * </p>
	 *
	 * @param orig
	 *        The object to copy along with all its containments.
	 * @return The copied object (graph).
	 */
	public TLObject copy(TLObject orig) {
		TLObject copy = copyStructure(null, orig);
		copyValues(orig, copy);
		return copy;
	}

	private void copyValues(TLObject orig, TLObject copy) {
		TLClass type = type(orig);
		for (TLStructuredTypePart part : type.getAllParts()) {
			Object value = orig.tValue(part);
			if (isComposite(part)) {
				// Note: The composite ends are considered in structural copy regardless if they are
				// "backwards" references. Therefore, do not continue on a derived composite part.

				// Descend to containments.
				if (value instanceof Collection<?>) {
					Collection<?> collection = (Collection<?>) value;
					for (Object element : collection) {
						copyValues((TLObject) element, _copyByOrig.get(element));
					}
				} else if (value != null) {
					copyValues((TLObject) value, _copyByOrig.get(value));
				}
			} else {
				if (part.isDerived()) {
					continue;
				}

				copy.tUpdate(part, copyValue(value));
			}
		}
	}

	private Object copyValue(Object value) {
		if (value instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) value;
			List<Object> result = new ArrayList<>(collection.size());
			for (Object element : collection) {
				result.add(copyValue(element));
			}
			return result;
		} else if (value instanceof TLObject) {
			TLObject copy = _copyByOrig.get(value);
			if (copy == null) {
				// Not part of the copy scope.
				return value;
			}
			return copy;
		} else {
			return value;
		}
	}

	private TLObject copyStructure(TLObject context, TLObject orig) {
		if (orig == null) {
			return null;
		}
		TLClass type = type(orig);
		TLObject copy = createNew(context, type);
		_copyByOrig.put(orig, copy);
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (!isComposite(part)) {
				continue;
			}
			Object value = orig.tValue(part);
			Object valueCopy;
			if (value instanceof Collection<?>) {
				List<Object> collectionCopy = new ArrayList<>();
				for (Object element : (Collection<?>) value) {
					TLObject elementCopy = copyStructure(orig, (TLObject) element);
					collectionCopy.add(elementCopy);
				}
				valueCopy = collectionCopy;
			} else {
				valueCopy = copyStructure(orig, (TLObject) value);
			}

			if (!part.isDerived()) {
				// Note: Reverse references could be flagged as "composite" and must not be updated.
				// Only collect the objects belonging to the structure and fill the forwards
				// aggregate references.
				copy.tUpdate(part, valueCopy);
			}
		}

		return copy;
	}

	private static boolean isComposite(TLStructuredTypePart part) {
		if (part.getModelKind() != ModelKind.REFERENCE) {
			return false;
		}

		boolean composite = ((TLReference) part).getEnd().isComposite();
		if (!composite) {
			return false;
		}
		return true;
	}

	private TLObject createNew(TLObject context, TLClass type) {
		return _factory.createObject(type, context, null);
	}

	private TLClass type(TLObject orig) {
		return (TLClass) orig.tType();
	}

}
