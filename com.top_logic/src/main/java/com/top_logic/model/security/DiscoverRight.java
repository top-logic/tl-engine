/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

import de.haumacher.msgbuf.data.ReflectiveDataObject;

/**
 * The checks of the {@link SimpleBoundCommandGroup#DISCOVER Discover} right on values: restricting a
 * value to the objects a user may discover, and checking the new targets of a reference.
 *
 * <p>
 * TL-Script and both user interfaces use these checks, so that an object source delivers the same
 * objects and a reference accepts the same targets everywhere. Whether a single object is
 * discoverable is decided by {@link ModelAccessRights#isDiscoverAllowed(Person, TLObject)}.
 * </p>
 */
public final class DiscoverRight {

	/**
	 * Per-thread state of {@link #isComputedForUser()}, <code>null</code> outside
	 * {@link #computeForUser(boolean, Supplier)}, which counts as a computation on behalf of the
	 * user.
	 */
	private static final ThreadLocal<Boolean> COMPUTED_FOR_USER = new ThreadLocal<>();

	private DiscoverRight() {
		// Static utility.
	}

	/**
	 * Restricts the given value to the objects the current user may discover.
	 *
	 * @see #filter(Person, Object, Consumer)
	 */
	public static Object filter(Object value) {
		return filter(TLContext.currentUser(), value, null);
	}

	/**
	 * Restricts the given value to the objects the given user may discover.
	 *
	 * <p>
	 * An undiscoverable {@link TLObject} is replaced by <code>null</code>; an undiscoverable element
	 * of a {@link Collection} is dropped, as is an entry of a {@link Map} whose key or value is
	 * undiscoverable. Collections and maps are filtered recursively.
	 * </p>
	 *
	 * <p>
	 * Only <em>business objects</em> are filtered. A {@link TLModelPart model element} (a type, an
	 * attribute, a module, a classifier) is discoverable for every user and therefore kept, see
	 * {@link ModelAccessRights#isDiscoverAllowed(Person, TLObject)}.
	 * </p>
	 *
	 * <p>
	 * A {@link ReflectiveDataObject} (e.g. a diagram assembled by a script) is kept for the same
	 * reason. It is a <em>structured value</em>, not a container of business objects: its properties
	 * form an object graph that may be cyclic, and a filtered copy would be a plain {@link Map} that
	 * cannot take the place of the original - filtering it would destroy the value instead of
	 * protecting data. A business object stored in such a value is therefore <em>not</em> filtered:
	 * whoever passes that object on to the user is responsible for checking the discover right.
	 * </p>
	 *
	 * @param user
	 *        The user whose discover right decides.
	 * @param value
	 *        The value to filter. May be <code>null</code>.
	 * @param onDropped
	 *        Receives every object removed from the value. May be <code>null</code>, then the
	 *        removals are not reported.
	 * @return A value containing only discoverable objects. When the value is (recursively)
	 *         discoverable, the given value itself is returned.
	 */
	public static Object filter(Person user, Object value, Consumer<? super TLObject> onDropped) {
		return new Filter(user, onDropped).filter(value);
	}

	/**
	 * Whether the current user may discover the given single value.
	 *
	 * @see #isDiscoverable(Person, Object)
	 */
	public static boolean isDiscoverable(Object value) {
		return isDiscoverable(TLContext.currentUser(), value);
	}

	/**
	 * Whether the given user may discover the given single value.
	 *
	 * <p>
	 * A value that is no {@link TLObject} (a string, a number, a node of a technical tree) is
	 * discoverable: only business objects are protected. A {@link TLModelPart model element} is
	 * discoverable for every user, see {@link ModelAccessRights#isDiscoverAllowed(Person, TLObject)}.
	 * The value is not inspected further: a collection is a single value here, use
	 * {@link #filter(Person, Object, Consumer)} to filter its elements.
	 * </p>
	 *
	 * @param user
	 *        The user whose discover right decides.
	 * @param value
	 *        The value to check. May be <code>null</code>.
	 */
	public static boolean isDiscoverable(Person user, Object value) {
		// The access rights are only needed for an object: a value without objects is accepted also
		// when the security service is not active.
		return !(value instanceof TLObject object) || ModelAccessRights.getInstance().isDiscoverAllowed(user, object);
	}

	/**
	 * Whether a form field shows the given value of a reference as placeholder
	 * {@link I18NConstants#MASKED_VALUE}, because the current user may not discover it.
	 *
	 * <p>
	 * A field holds the stored value of a reference, so it may contain such objects. Their identity
	 * - label, image, tooltip, link - is not shown: the field shows the placeholder instead, keeps
	 * the object at its place in the value, and writes it back unchanged unless the user removes it.
	 * </p>
	 *
	 * @param value
	 *        The value to check. May be <code>null</code>.
	 * @see #isDiscoverable(Object)
	 */
	public static boolean isMasked(Object value) {
		return !isDiscoverable(value);
	}

	/**
	 * Whether the current thread computes a value on behalf of the current user, see
	 * {@link #computeForUser(boolean, Supplier)}.
	 *
	 * <p>
	 * A value can contain objects in a form of its own, for example the object links of a formatted
	 * text. A presentation of such a value as text masks the objects that the current user may not
	 * discover, unless the computation is explicitly on behalf of the system. So every output to a
	 * user masks, also one that nobody thought of, and only a computation on behalf of the system
	 * gives the value as it is stored, so that a result that it stores does not depend on the user
	 * who started it.
	 * </p>
	 *
	 * @return <code>true</code> outside {@link #computeForUser(boolean, Supplier)}.
	 */
	@FrameworkInternal
	public static boolean isComputedForUser() {
		return COMPUTED_FOR_USER.get() != Boolean.FALSE;
	}

	/**
	 * Computes a value on behalf of the current user or on behalf of the system, see
	 * {@link #isComputedForUser()}.
	 *
	 * <p>
	 * A secured TL-Script evaluation computes on behalf of the user, an unsecured one on behalf of
	 * the system (R12 of the Discover specification). A computation inside the given one can change
	 * the setting for its own duration.
	 * </p>
	 *
	 * @param forUser
	 *        Whether the computation is on behalf of the current user.
	 * @param computation
	 *        The computation.
	 * @return The result of the computation.
	 */
	@FrameworkInternal
	public static <T> T computeForUser(boolean forUser, Supplier<T> computation) {
		Boolean before = COMPUTED_FOR_USER.get();
		COMPUTED_FOR_USER.set(Boolean.valueOf(forUser));
		try {
			return computation.get();
		} finally {
			if (before == null) {
				// A pooled thread keeps no state of a former computation.
				COMPUTED_FOR_USER.remove();
			} else {
				COMPUTED_FOR_USER.set(before);
			}
		}
	}

	/**
	 * Checks that the current user may discover every object the given new value adds to the given
	 * reference.
	 *
	 * <p>
	 * Objects already contained in the stored value are not checked: they may stay even if the user
	 * cannot discover them. A deleted object is not checked either: it is discoverable for nobody,
	 * but whether it may be stored is no question of the user's rights.
	 * </p>
	 *
	 * @param self
	 *        The object to modify.
	 * @param part
	 *        The attribute of the given object to modify. Only references are checked.
	 * @param newValue
	 *        The value that is going to be stored: a single object, a {@link Collection} or an array
	 *        of objects, or <code>null</code>.
	 * @throws TopLogicException
	 *         If the new value refers to an object that is not already referenced and that the
	 *         current user cannot discover.
	 */
	public static void checkNewTargets(TLObject self, TLStructuredTypePart part, Object newValue) {
		checkNewTargets(self, part, () -> self.tValue(part), newValue);
	}

	/**
	 * Checks that the current user may discover every object the given new value adds to the given
	 * stored value of a reference.
	 *
	 * <p>
	 * Used where the object holding the reference does not exist yet (the stored value of an object
	 * being created is empty) or where its stored value is not its current value.
	 * </p>
	 *
	 * @param self
	 *        The object to modify, used to describe the denial. May be a form overlay of an object
	 *        that is not yet created.
	 * @param part
	 *        The attribute of the given object to modify. Only references are checked.
	 * @param storedValue
	 *        The stored value of the reference. Objects contained in it may stay even if the user
	 *        cannot discover them.
	 * @param newValue
	 *        The value that is going to be stored: a single object, a {@link Collection} or an array
	 *        of objects, or <code>null</code>.
	 * @throws TopLogicException
	 *         If the new value refers to an object that is not contained in the stored value and that
	 *         the current user cannot discover.
	 *
	 * @see #checkNewTargets(TLObject, TLStructuredTypePart, Object)
	 */
	public static void checkNewTargets(TLObject self, TLStructuredTypePart part, Object storedValue,
			Object newValue) {
		checkNewTargets(self, part, () -> storedValue, newValue);
	}

	private static void checkNewTargets(TLObject self, TLStructuredTypePart part, Supplier<?> storedValue,
			Object newValue) {
		if (!(part instanceof TLReference)) {
			return;
		}
		Collection<?> newTargets = asCollection(newValue);
		if (newTargets.isEmpty()) {
			return;
		}
		Set<?> stored = asSet(storedValue.get());
		ModelAccessRights accessRights = ModelAccessRights.getInstance();
		for (Object target : newTargets) {
			if (target instanceof TLObject object && object.tValid() && !stored.contains(object)
				&& !accessRights.isDiscoverAllowed(object)) {
				throw new TopLogicException(I18NConstants.ERROR_TARGET_NOT_DISCOVERABLE__OBJECT_ATTRIBUTE.fill(self, part));
			}
		}
	}

	/**
	 * The given value as collection: a collection itself, the elements of an array, a single
	 * value, or nothing for <code>null</code>.
	 */
	static Collection<?> asCollection(Object value) {
		if (value instanceof Collection<?> collection) {
			return collection;
		} else if (value instanceof Object[] array) {
			return Arrays.asList(array);
		} else if (value == null) {
			return Collections.emptyList();
		} else {
			return Collections.singletonList(value);
		}
	}

	private static Set<?> asSet(Object value) {
		if (value instanceof Set<?> set) {
			return set;
		} else {
			return new HashSet<>(asCollection(value));
		}
	}

	/**
	 * The recursive filter of {@link DiscoverRight#filter(Person, Object, Consumer)} for one user.
	 */
	private static final class Filter {

		private final Person _user;

		private final Consumer<? super TLObject> _onDropped;

		Filter(Person user, Consumer<? super TLObject> onDropped) {
			_user = user;
			_onDropped = onDropped;
		}

		Object filter(Object value) {
			if (value == null) {
				return null;
			}
			if (value instanceof TLObject object) {
				return filterObject(object);
			}
			if (value instanceof ReflectiveDataObject) {
				// A structured value, not a container of business objects.
				return value;
			}
			if (value instanceof Collection<?> collection) {
				return filterCollection(collection);
			}
			if (value instanceof Map<?, ?> map) {
				return filterMap(map);
			}
			return value;
		}

		private Object filterObject(TLObject value) {
			if (isDiscoverable(_user, value)) {
				return value;
			}
			if (_onDropped != null) {
				_onDropped.accept(value);
			}
			return null;
		}

		private Object filterCollection(Collection<?> value) {
			if (value.isEmpty()) {
				return value;
			}
			Collection<Object> filtered;
			boolean anyChanges = false;
			if (value instanceof RandomAccess) {
				List<?> list = (List<?>) value;
				filtered = new ArrayList<>();
				for (int i = 0, size = list.size(); i < size; i++) {
					anyChanges |= addFiltered(filtered, list.get(i));
				}
			} else {
				if (value instanceof LinkedHashSet) {
					filtered = new LinkedHashSet<>();
				} else if (value instanceof Set) {
					filtered = new HashSet<>();
				} else {
					filtered = new ArrayList<>();
				}
				for (Object element : value) {
					anyChanges |= addFiltered(filtered, element);
				}
			}
			// The original value indicates that no element had to be filtered.
			return anyChanges ? filtered : value;
		}

		/**
		 * Adds the filtered form of the given element to the given collection.
		 *
		 * @return Whether the element was changed by filtering.
		 */
		private boolean addFiltered(Collection<Object> filtered, Object element) {
			Object filteredElement = filter(element);
			if (filteredElement == element) {
				filtered.add(element);
				return false;
			}
			if (filteredElement != null) {
				// Some content of the element is not discoverable.
				filtered.add(filteredElement);
			}
			return true;
		}

		private Object filterMap(Map<?, ?> value) {
			if (value.isEmpty()) {
				return value;
			}
			Map<Object, Object> filtered = value instanceof LinkedHashMap ? new LinkedHashMap<>() : new HashMap<>();
			boolean anyChanges = false;
			for (Entry<?, ?> entry : value.entrySet()) {
				Object key = entry.getKey();
				Object filteredKey = filter(key);
				Object element = entry.getValue();
				Object filteredElement = filter(element);
				if (filteredKey == key) {
					if (filteredElement == element) {
						filtered.put(key, element);
					} else {
						if (filteredElement != null) {
							// Some content of the element is not discoverable.
							filtered.put(key, filteredElement);
						}
						anyChanges = true;
					}
				} else {
					if (filteredKey != null && (element == null || filteredElement != null)) {
						// Some content of the key is not discoverable.
						filtered.put(filteredKey, filteredElement);
					}
					anyChanges = true;
				}
			}
			// The original value indicates that no entry had to be filtered.
			return anyChanges ? filtered : value;
		}
	}

}
