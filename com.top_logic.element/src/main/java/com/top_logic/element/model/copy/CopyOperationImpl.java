/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.model.ModelService;

abstract class CopyOperationImpl extends CopyOperation implements CopyFilter, CopyConstructor {

	final TLFactory _factory = ModelService.getInstance().getFactory();

	private TLReference _contextRef;

	private TLObject _context;

	CopyFilter _filter = this;

	private CopyConstructor _constructor = this;

	private Boolean _transientCopy;

	@Override
	public CopyOperationImpl setFilter(CopyFilter filter) {
		_filter = filter;
		return this;
	}

	@Override
	public CopyOperationImpl setTransient(Boolean transientCopy) {
		_transientCopy = transientCopy;
		return this;
	}

	@Override
	public CopyOperation setConstructor(CopyConstructor constructor) {
		_constructor = constructor;
		return this;
	}

	public final TLObject getContext() {
		return _context;
	}

	@Override
	public TLObject setContext(TLObject object, TLReference contextRef) {
		setContextRef(contextRef);

		TLObject before = _context;
		_context = object;
		return before;
	}

	private TLReference getContextRef() {
		return _contextRef;
	}

	private void setContextRef(TLReference contextRef) {
		_contextRef = contextRef;
	}

	protected abstract Set<Entry<TLObject, TLObject>> localCopies();

	@Override
	public Object copyReference(TLObject orig) {
		return createCopy(orig);
	}

	@Override
	public boolean accept(TLStructuredTypePart part, Object value, TLObject context) {
		return true;
	}

	private Object createCopy(TLObject orig) {
		TLObject existing = resolveCopy(orig);
		if (existing != null) {
			return existing;
		}

		TLReference contextRef = getContextRef();
		TLObject context = getContext();
		TLObject copy = _constructor.allocate(orig, contextRef, context);
		if (copy == null && _constructor != this) {
			// Note: Returning null from a constructor function means to invoke the default
			// constructor. Suppressing a copy must be done in a filter expression.
			copy = allocate(orig, contextRef, context);
		}
		enterCopy(orig, copy);
		return copy;
	}

	@Override
	public TLObject allocate(TLObject orig, TLReference reference, TLObject context) {
		TLStructuredType type = orig.tType();
		if (type.getModelKind() != ModelKind.CLASS) {
			// Not an object, no copy.
			return orig;
		}

		TLStructuredType currentType = WrapperHistoryUtils.getCurrent(type);
		if (currentType == null) {
			// Type does no longer exist, keep historic reference.
			return orig;
		}

		TLClass classType = (TLClass) currentType;
		boolean copyTransient = _transientCopy == null ? orig.tTransient() : _transientCopy;
		if (copyTransient) {
			return TransientObjectFactory.INSTANCE.createObject(classType, context);
		} else {
			return _factory.createObject(classType, context);
		}
	}

	final void copyComposite(TLObject orig, TLReference reference, TLObject copy) {
		// Note: The target object may be of another type than the source object and not
		// define all properties of the source.
		if (!defines(copy, reference)) {
			return;
		}

		Object value = orig.tValue(reference);

		if (!_filter.accept(reference, value, orig)) {
			return;
		}

		Object valueCopy = copyValue(orig, reference, value);
		copy.tUpdate(reference, valueCopy);
	}

	private Object copyValue(TLObject orig, TLReference reference, Object value) {
		if (value instanceof Collection) {
			return copyCollection(orig, reference, (Collection<?>) value);
		} else if (value == null) {
			return null;
		} else {
			return copyObject(orig, reference, (TLObject) value);
		}
	}

	private Object copyCollection(TLObject orig, TLReference reference, Collection<?> collection) {
		Collection<Object> result = allocateCopy(collection);
		for (Object element : collection) {
			result.add(copyObject(orig, reference, (TLObject) element));
		}
		return result;
	}

	private Object copyObject(TLObject orig, TLReference reference, TLObject value) {
		TLReference refBefore = getContextRef();
		TLObject before = setContext(orig, reference);
		try {
			return createCopy(value);
		} finally {
			setContext(before, refBefore);
		}
	}

	static Collection<Object> allocateCopy(Collection<?> value) {
		if (value instanceof Set<?>) {
			return new LinkedHashSet<>();
		}
		return new ArrayList<>();
	}

	static boolean defines(TLObject object, TLStructuredTypePart part) {
		return part.getDefinition() == definition(object, part);
	}

	private static TLStructuredTypePart definition(TLObject object, TLStructuredTypePart part) {
		TLStructuredTypePart resolved = resolve(object, part);
		if (resolved == null) {
			return null;
		}
		return resolved.getDefinition();
	}

	private static TLStructuredTypePart resolve(TLObject object, TLStructuredTypePart part) {
		return object.tType().getPart(part.getName());
	}


}