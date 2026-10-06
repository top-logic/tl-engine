/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

abstract class CopyOperationImpl extends CopyOperation implements CopyFilter, CopyConstructor {

	final TLFactory _factory = ModelService.getInstance().getFactory();

	private TLReference _contextRef;

	private TLObject _context;

	CopyFilter _filter = this;

	private CopyConstructor _constructor = this;

	private Boolean _transientCopy;

	private Boolean _useSecurity;

	private boolean _skipTransactionDefaults;

	/**
	 * Whether a part of a copy has a default computed in the creating transaction, indexed by the
	 * part of the copy's type.
	 */
	private final Map<TLStructuredTypePart, Boolean> _computedInTransaction = new HashMap<>();

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
	public CopyOperation withSecurity(Boolean useSecurity) {
		_useSecurity = useSecurity;
		return this;
	}

	@Override
	public CopyOperation skipTransactionDefaults(boolean skip) {
		_skipTransactionDefaults = skip;
		return this;
	}

	/**
	 * Whether the given part is copied to the given copy.
	 *
	 * <p>
	 * A part is not copied, if the copy keeps the default computed in its creating transaction,
	 * see {@link #skipTransactionDefaults(boolean)}.
	 * </p>
	 *
	 * @param copy
	 *        The copy that defines the given part.
	 * @param part
	 *        The part of the original's type.
	 */
	final boolean isCopied(TLObject copy, TLStructuredTypePart part) {
		return isCopied(copy.tTransient(), resolve(copy, part));
	}

	/**
	 * Whether a value is copied to the given part of a copy.
	 *
	 * @param transientCopy
	 *        Whether the copy is transient.
	 * @param targetPart
	 *        The part of the copy's type.
	 */
	private boolean isCopied(boolean transientCopy, TLStructuredTypePart targetPart) {
		if (!_skipTransactionDefaults || transientCopy) {
			return true;
		}
		return !_computedInTransaction.computeIfAbsent(targetPart, CopyOperationImpl::isComputedInTransaction);
	}

	private static boolean isComputedInTransaction(TLStructuredTypePart part) {
		DefaultProvider defaultProvider = DisplayAnnotations.getDefaultProvider(part);
		return defaultProvider != null && defaultProvider.isComputedInTransaction();
	}

	/**
	 * Whether read access to copied attributes is checked (see {@link #withSecurity(Boolean)}).
	 */
	protected final boolean useSecurity() {
		return Boolean.TRUE.equals(_useSecurity);
	}

	/**
	 * Reads the value of the given part from the original, applying a read-access check when
	 * {@link #useSecurity() security is enabled}.
	 *
	 * <p>
	 * This mirrors a TL-Script attribute read ({@code $orig.get(part)}): if the current user must
	 * not read the part on the original, the empty value (<code>null</code>, or an empty collection
	 * for multiple parts) is returned, exactly as {@code get} would. This keeps {@code copy()} a pure
	 * shortcut for {@code new(...).set(part, $orig.get(part))...}.
	 * </p>
	 */
	protected final Object readValue(TLObject orig, TLStructuredTypePart part) {
		if (useSecurity() && !ModelAccessRights.getInstance().isReadAllowed(orig, part)) {
			return TLModelUtil.getEmptyValue(part);
		}
		return orig.tValue(part);
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
			if (useSecurity()
					&& !ModelAccessRights.getInstance().isAllowedCreate(TLContext.currentUser(), classType, context)) {
				// Mirror new(type): allocating a copy requires the CREATE permission on the copied
				// type. Persisting a transient object is the creation of that object, not a copy of
				// it, so it is refused with the create wording.
				ResKey1 message = orig.tTransient() ? I18NConstants.ERROR_PERSIST_PERMISSION_DENIED__TYPE
					: I18NConstants.ERROR_CREATE_PERMISSION_DENIED__TYPE;
				throw new TopLogicException(message.fill(classType));
			}
			if (useSecurity() && orig.tTransient()) {
				checkInitialValues(orig, classType, context);
			}
			return _factory.createObject(classType, context);
		}
	}

	/**
	 * Checks that the current user may set every value the given transient original passes to the
	 * persistent object of the given type created in the given context.
	 *
	 * <p>
	 * Persisting a transient object is the creation of that object with the values of the transient
	 * object as initial values. A value that differs from the initial value the attribute gets on
	 * creation (see {@link #isInitialValue(TLStructuredTypePart, TLObject, Object)}) requires the
	 * right to write the attribute of the object to be created, see
	 * {@link ModelAccessRights#isAllowedInitial(Person, TLClass, TLObject, TLStructuredTypePart, BoundCommandGroup)}.
	 * The check runs before the persistent object is created.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If a value must not be set.
	 */
	private void checkInitialValues(TLObject orig, TLClass type, TLObject context) {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		for (TLStructuredTypePart part : orig.tType().getAllParts()) {
			TLStructuredTypePart targetPart = type.getPart(part.getName());
			if (targetPart == null || targetPart.isDerived()
				|| targetPart.getDefinition() != part.getDefinition()) {
				// Not copied, see copyValues() and copyComposite().
				continue;
			}
			if (!isCopied(false, targetPart)) {
				continue;
			}
			if (rights.isAllowedInitial(user, type, context, targetPart, SimpleBoundCommandGroup.WRITE)) {
				continue;
			}
			Object value = readValue(orig, part);
			if (!_filter.accept(part, value, orig)) {
				continue;
			}
			if (isInitialValue(targetPart, context, value)) {
				continue;
			}
			throw new TopLogicException(
				I18NConstants.ERROR_INITIAL_VALUE_PERMISSION_DENIED__ATTRIBUTE_TYPE.fill(targetPart, type));
		}
	}

	/**
	 * Whether the given value is the initial value the given attribute gets when an object is
	 * created in the given context.
	 *
	 * <p>
	 * The initial value is the value of the attribute's {@link DefaultProvider} for the context, the
	 * value {@link TLFactory#setupDefaultValues(Object, TLObject, com.top_logic.model.TLStructuredType)}
	 * sets. Without a {@link DefaultProvider}, and for a {@link DefaultProvider} that is
	 * {@link DefaultProvider#isComputedInTransaction() computed in the creating transaction}, which a
	 * transient object never receives, it is the empty value: <code>null</code> or an empty
	 * collection. Values are compared by equality; an empty string equals <code>null</code>, the
	 * values of a multiple attribute are compared as list if the attribute is ordered, otherwise as
	 * set.
	 * </p>
	 */
	private static boolean isInitialValue(TLStructuredTypePart part, TLObject context, Object value) {
		DefaultProvider defaultProvider = DisplayAnnotations.getDefaultProvider(part);
		Object initial;
		if (defaultProvider == null || defaultProvider.isComputedInTransaction()) {
			initial = TLModelUtil.getEmptyValue(part);
		} else {
			initial = defaultProvider.createDefault(context, part);
		}
		return sameValue(part, value, initial);
	}

	private static boolean sameValue(TLStructuredTypePart part, Object value, Object other) {
		if (part.isMultiple()) {
			Collection<?> values = asCollection(value);
			Collection<?> others = asCollection(other);
			if (part.isOrdered() || part.isBag()) {
				return new ArrayList<>(values).equals(new ArrayList<>(others));
			}
			return values.size() == others.size() && new HashSet<>(values).equals(new HashSet<>(others));
		}
		Object normalized = normalize(value);
		Object normalizedOther = normalize(other);
		if (normalized instanceof Number number && normalizedOther instanceof Number otherNumber) {
			return number.doubleValue() == otherNumber.doubleValue();
		}
		return Objects.equals(normalized, normalizedOther);
	}

	private static Collection<?> asCollection(Object value) {
		if (value == null) {
			return Collections.emptyList();
		}
		if (value instanceof Collection<?> collection) {
			return collection;
		}
		return Collections.singletonList(value);
	}

	private static Object normalize(Object value) {
		if (value instanceof CharSequence text && text.length() == 0) {
			return null;
		}
		return value;
	}

	final void copyComposite(TLObject orig, TLReference reference, TLObject copy) {
		// Note: The target object may be of another type than the source object and not
		// define all properties of the source.
		if (!defines(copy, reference)) {
			return;
		}
		if (!isCopied(copy, reference)) {
			return;
		}

		Object value = readValue(orig, reference);

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