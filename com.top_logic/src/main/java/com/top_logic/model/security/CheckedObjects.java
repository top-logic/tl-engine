/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Collection;
import java.util.function.Function;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * Decides an operation on the checked objects of an access check.
 *
 * <p>
 * The checked objects are the value an expression yields for an access check, e.g. the security
 * object of a privileged TL-Script function. The decision depends only on that value:
 * </p>
 *
 * <ul>
 * <li>a single object: the operation is checked on it;</li>
 * <li>a {@link Collection}: the operation must be allowed on every element;</li>
 * <li><code>null</code> or an empty collection: {@link Outcome#NO_OBJECT no object}.</li>
 * </ul>
 *
 * <p>
 * An element is checked with {@link ModelAccessRights#isAllowed(Person, TLObject, BoundCommandGroup)}.
 * An edit overlay of a form ({@link TLFormObjectBase} that is not {@link TLFormObjectBase#isCreate()
 * create}) is checked as the object it {@link TLFormObjectBase#editedObject(TLObject) edits}. An
 * element that is no {@link TLObject} is {@link Outcome#INVALID invalid}.
 * </p>
 *
 * @see ModelAccessRights
 */
public final class CheckedObjects {

	/**
	 * The kind of a {@link Decision}.
	 */
	public enum Outcome {

		/**
		 * The operation is allowed on every checked object.
		 */
		ALLOWED,

		/**
		 * There is no object to check: the checked value is <code>null</code> or an empty
		 * collection.
		 */
		NO_OBJECT,

		/**
		 * The {@link Decision#getElement() element} can not be checked, e.g. since it is no
		 * {@link TLObject}.
		 */
		INVALID,

		/**
		 * The operation is not allowed on the {@link Decision#getElement() element}.
		 */
		DENIED;

	}

	/**
	 * The result of a check of the checked objects.
	 */
	public static final class Decision {

		/**
		 * The operation is allowed on every checked object.
		 */
		public static final Decision ALLOWED = new Decision(Outcome.ALLOWED, null);

		/**
		 * There is no object to check.
		 */
		public static final Decision NO_OBJECT = new Decision(Outcome.NO_OBJECT, null);

		private final Outcome _outcome;

		private final Object _element;

		private Decision(Outcome outcome, Object element) {
			_outcome = outcome;
			_element = element;
		}

		/**
		 * A {@link Decision} that the given element can not be checked.
		 */
		public static Decision invalid(Object element) {
			return new Decision(Outcome.INVALID, element);
		}

		/**
		 * A {@link Decision} that the operation is not allowed on the given element.
		 */
		public static Decision denied(Object element) {
			return new Decision(Outcome.DENIED, element);
		}

		/**
		 * The kind of this decision.
		 */
		public Outcome getOutcome() {
			return _outcome;
		}

		/**
		 * The element the decision failed on, for {@link Outcome#INVALID} and
		 * {@link Outcome#DENIED}; <code>null</code> otherwise.
		 *
		 * <p>
		 * An {@link Outcome#INVALID invalid} element may itself be <code>null</code>, e.g. a
		 * <code>null</code> element of a collection.
		 * </p>
		 */
		public Object getElement() {
			return _element;
		}

		/**
		 * Whether the operation is allowed on every checked object.
		 */
		public boolean isAllowed() {
			return _outcome == Outcome.ALLOWED;
		}

		@Override
		public String toString() {
			return _element == null ? _outcome.name() : _outcome.name() + "(" + _element + ")";
		}

	}

	private CheckedObjects() {
		// Utility class.
	}

	/**
	 * Decides the given checked value with the given check of a single element.
	 *
	 * @param checkedValue
	 *        A single object, a {@link Collection} of objects, or <code>null</code>.
	 * @param elementCheck
	 *        Decides a single element, either {@link Decision#ALLOWED}, {@link Decision#invalid(Object)}
	 *        or {@link Decision#denied(Object)}.
	 * @return {@link Decision#NO_OBJECT} for <code>null</code> or an empty collection, the first
	 *         decision of an element that is not {@link Decision#isAllowed() allowed}, otherwise
	 *         {@link Decision#ALLOWED}.
	 */
	public static Decision check(Object checkedValue, Function<Object, Decision> elementCheck) {
		if (checkedValue == null) {
			return Decision.NO_OBJECT;
		}
		if (checkedValue instanceof Collection<?> collection) {
			if (collection.isEmpty()) {
				return Decision.NO_OBJECT;
			}
			for (Object element : collection) {
				Decision decision = elementCheck.apply(element);
				if (!decision.isAllowed()) {
					return decision;
				}
			}
			return Decision.ALLOWED;
		}
		return elementCheck.apply(checkedValue);
	}

	/**
	 * Decides whether the given person may perform the given operation on the given checked value.
	 *
	 * @param person
	 *        The person to check.
	 * @param checkedValue
	 *        A single object, a {@link Collection} of objects, or <code>null</code>.
	 * @param operation
	 *        The operation to check.
	 * @see #checkElement(Person, Object, BoundCommandGroup)
	 */
	public static Decision check(Person person, Object checkedValue, BoundCommandGroup operation) {
		return check(checkedValue, element -> checkElement(person, element, operation));
	}

	/**
	 * Decides whether the given person may perform the given operation on the given checked value,
	 * accepting stored objects only.
	 *
	 * <p>
	 * The access check allows everything on an object whose roles are not computed (see
	 * {@link #isStored(TLObject)}). Where the checked value comes from a caller (e.g. computed from
	 * the arguments of a call), such an object would let the caller authorize the operation with an
	 * object of its own making (e.g. a transient object). This check therefore treats such an
	 * element as {@link Outcome#INVALID invalid}.
	 * </p>
	 *
	 * @param person
	 *        The person to check.
	 * @param checkedValue
	 *        A single object, a {@link Collection} of objects, or <code>null</code>.
	 * @param operation
	 *        The operation to check.
	 * @see #checkStoredElement(Person, Object, BoundCommandGroup)
	 */
	public static Decision checkStored(Person person, Object checkedValue, BoundCommandGroup operation) {
		return check(checkedValue, element -> checkStoredElement(person, element, operation));
	}

	/**
	 * Decides whether the given person may perform the given operation on a single element of the
	 * checked objects.
	 *
	 * @param person
	 *        The person to check.
	 * @param element
	 *        The element to check. An edit overlay of a form is checked as the object it edits.
	 * @param operation
	 *        The operation to check.
	 * @return {@link Decision#ALLOWED}, {@link Decision#denied(Object)}, or
	 *         {@link Decision#invalid(Object)} for an element that is no {@link TLObject}.
	 */
	public static Decision checkElement(Person person, Object element, BoundCommandGroup operation) {
		if (!(element instanceof TLObject candidate)) {
			return Decision.invalid(element);
		}
		return decide(person, element, TLFormObjectBase.editedObject(candidate), operation);
	}

	/**
	 * Decides whether the given person may perform the given operation on a single element of the
	 * checked objects, accepting {@link #isStored(TLObject) stored} objects only.
	 *
	 * @param person
	 *        The person to check.
	 * @param element
	 *        The element to check. An edit overlay of a form is checked as the object it edits.
	 * @param operation
	 *        The operation to check.
	 * @return {@link Decision#ALLOWED}, {@link Decision#denied(Object)}, or
	 *         {@link Decision#invalid(Object)} for an element that is no {@link TLObject} or whose
	 *         (edited) object is not {@link #isStored(TLObject) stored}.
	 */
	public static Decision checkStoredElement(Person person, Object element, BoundCommandGroup operation) {
		if (!(element instanceof TLObject candidate)) {
			return Decision.invalid(element);
		}
		TLObject object = TLFormObjectBase.editedObject(candidate);
		if (!isStored(object)) {
			return Decision.invalid(element);
		}
		return decide(person, element, object, operation);
	}

	private static Decision decide(Person person, Object element, TLObject object, BoundCommandGroup operation) {
		if (ModelAccessRights.getInstance().isAllowed(person, object, operation)) {
			return Decision.ALLOWED;
		}
		return Decision.denied(element);
	}

	/**
	 * Whether the given object is a stored object whose roles are computed.
	 *
	 * <p>
	 * The access check allows everything on an object that is no {@link BoundObject}, is
	 * transient, or was created in the current transaction. A create overlay of a form is
	 * transient.
	 * </p>
	 */
	public static boolean isStored(TLObject object) {
		return object instanceof BoundObject && SecurityConfigurationService.isCommitted(object);
	}

}
