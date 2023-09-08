/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utility for managing reflexive symmetric transitive relations between objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransitiveRelations<T> {
	
	private final Map<String, TransitiveRelation<T>> _relations = new HashMap<>();

	/**
	 * Retrieve the (read-only) {@link TransitiveRelation} with the given name.
	 */
	public TransitiveRelation<T> getRelation(String relationName) {
		TransitiveRelation<T> result = _relations.get(relationName);
		if (result == null) {
			@SuppressWarnings("unchecked")
			TransitiveRelation<T> empty = (TransitiveRelation<T>) TransitiveRelation.EMPTY;
			return empty;
		}
		return result;
	}

	/**
	 * Retrieve a modifiable {@link TransitiveRelation} with the given name.
	 * 
	 * <p>
	 * If the relation does not yet exist, it is created.
	 * </p>
	 */
	public TransitiveRelation<T> makeRelation(String relationName) {
		TransitiveRelation<T> relation = _relations.get(relationName);
		if (relation == null) {
			relation = new TransitiveRelation<>();
			_relations.put(relationName, relation);
		}
		return relation;
	}
	
	/**
	 * Removes all known relations.
	 * 
	 */
	public void clear() {
		_relations.clear();
	}

	/**
	 * A single relation between named objects.
	 */
	public static class TransitiveRelation<T> {

		static final TransitiveRelation<Object> EMPTY = new TransitiveRelation<>() {
			@Override
			public void assignToGroup(String groupName, Object member) {
				throw new UnsupportedOperationException("Unmodifiable.");
			}

			@Override
			public void link(Object comp1, Object comp2) {
				throw new UnsupportedOperationException("Unmodifiable.");
			}
		};

		private boolean _valid;

		/**
		 * A representative object for a group with a certain name.
		 */
		private Map<String, T> _representative = new HashMap<>();

		private Map<T, Set<T>> _related = new HashMap<>();

		TransitiveRelation() {
			super();
		}

		/**
		 * Whether the given objects are related according to this
		 * {@link TransitiveRelations.TransitiveRelation}.
		 */
		public boolean isRelated(T obj1, T obj2) {
			validate();
			Set<T> relatedObjects = relatedOrNull(obj1);
			if (relatedObjects == null) {
				return false;
			} else {
				return relatedObjects.contains(obj2);
			}
		}

		private void validate() {
			if (_valid) {
				return;
			}
			_valid = true;

			transitiveClosure(_related);
		}

		/**
		 * Makes the given object member of the given group.
		 * 
		 * <p>
		 * Objects are related, if they are member of the same group, or explicitly
		 * {@link #link(Object, Object) linked} to each other.
		 * </p>
		 */
		public void assignToGroup(String groupName, T obj) {
			invalidate();
			T other = _representative.put(groupName, obj);
			if (other != null) {
				link(other, obj);
			}
		}

		/**
		 * Links the given objects.
		 * 
		 * <p>
		 * Objects are related, if they are member of the same group, or explicitly
		 * {@link #link(Object, Object) linked} to each other.
		 * </p>
		 */
		public void link(T obj1, T obj2) {
			invalidate();

			// Ensure that the relation is symmetric by construction. This is a precondition for the
			// algorithm ensuring transitivity, see below.
			related(obj1).add(obj2);
			related(obj2).add(obj1);
		}

		private void invalidate() {
			_valid = false;
		}

		private Set<T> related(T obj) {
			Set<T> relatedObjects = relatedOrNull(obj);
			if (relatedObjects == null) {
				relatedObjects = new HashSet<>();

				// Ensure that the relation is reflexive by construction.
				relatedObjects.add(obj);

				_related.put(obj, relatedObjects);
			}
			return relatedObjects;
		}

		/**
		 * All objects related to the given object.
		 */
		public Set<T> getRelated(T obj) {
			validate();
			Set<T> result = relatedOrNull(obj);
			if (result == null) {
				return Collections.emptySet();
			}
			return result;
		}

		private Set<T> relatedOrNull(T obj) {
			return _related.get(obj);
		}

		private static <T> void transitiveClosure(Map<T, Set<T>> reflexiveSymmetricRelation) {
			boolean change;
			do {
				change = false;
				for (Entry<T, Set<T>> entry : reflexiveSymmetricRelation.entrySet()) {
					Set<T> targets = entry.getValue();

					for (T target : targets) {
						// Note: By construction, the relation is symmetric. Therefore, for each
						// target, a target set already exists in the map. Since the relation is
						// reflexive by construction, it is not necessary to add the key/source
						// of the current entry to the targets of targets explicitly.
						change |= reflexiveSymmetricRelation.get(target).addAll(targets);
					}
				}
			} while (change);
		}
	}

}