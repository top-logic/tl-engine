/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.TLModelUtil;

/**
 * Summary of a change in terms of the registrations of a {@link ModelScope} it reaches: the keys of
 * the objects whose object listeners are notified, and the types whose type listeners are notified.
 *
 * <p>
 * A summary is independent of any registered {@link ModelListener}, so that a single summary of an
 * {@link UpdateEvent} can be checked against many scopes, see
 * {@link DefaultModelScope#isAffectedBy(ModelChangeSummary)}. It is built with the same rules a
 * {@link ModelChangeEvent} is built with, see {@link EventBuilder#summary()} and
 * {@link EventBuilder#summarize(ModelEventSettings, UpdateEvent)}.
 * </p>
 *
 * <p>
 * A summary is immutable and can be shared between threads.
 * </p>
 */
public final class ModelChangeSummary {

	private final Set<ObjectKey> _changedObjects;

	private final Set<TLStructuredType> _affectedTypes;

	/**
	 * Creates a {@link ModelChangeSummary}.
	 *
	 * @param changedObjects
	 *        The keys of the updated and deleted objects.
	 * @param changedTypes
	 *        The types of the created, updated and deleted objects. Their generalizations are added
	 *        to {@link #getAffectedTypes()}, see {@link #typesObserving(TLStructuredType)}.
	 */
	public ModelChangeSummary(Collection<ObjectKey> changedObjects,
			Collection<? extends TLStructuredType> changedTypes) {
		_changedObjects = Collections.unmodifiableSet(new HashSet<>(changedObjects));

		Set<TLStructuredType> affectedTypes = new HashSet<>();
		for (TLStructuredType changedType : changedTypes) {
			affectedTypes.addAll(typesObserving(changedType));
		}
		_affectedTypes = Collections.unmodifiableSet(affectedTypes);
	}

	/**
	 * The keys of the objects whose listeners
	 * ({@link ModelScope#addModelListener(com.top_logic.model.TLObject, ModelListener)}) are
	 * notified about the change: the updated and the deleted objects.
	 *
	 * <p>
	 * An object is updated also when it is the end of a created, updated or deleted link that is
	 * relevant according to the {@link ModelEventSettings}.
	 * </p>
	 */
	public Set<ObjectKey> getChangedObjects() {
		return _changedObjects;
	}

	/**
	 * The types whose listeners ({@link ModelScope#addModelListener(TLStructuredType, ModelListener)})
	 * are notified about the change: the types of the created, updated and deleted objects together
	 * with all their generalizations.
	 */
	public Set<TLStructuredType> getAffectedTypes() {
		return _affectedTypes;
	}

	/**
	 * Whether the change reaches no object and no type listener.
	 *
	 * <p>
	 * A listener for all changes ({@link ModelScope#addModelListener(ModelListener)}) is notified
	 * nevertheless.
	 * </p>
	 */
	public boolean isEmpty() {
		return _changedObjects.isEmpty() && _affectedTypes.isEmpty();
	}

	/**
	 * The types whose type listeners are notified about a change of an object of the given type.
	 *
	 * @return For a {@link TLClass} its reflexive transitive generalizations, otherwise the given
	 *         type alone.
	 */
	public static Collection<? extends TLStructuredType> typesObserving(TLStructuredType changedType) {
		if (changedType.getModelKind() == ModelKind.CLASS) {
			return TLModelUtil.getReflexiveTransitiveGeneralizations((TLClass) changedType);
		} else {
			return Collections.singleton(changedType);
		}
	}

	@Override
	public String toString() {
		return "ModelChangeSummary(objects: " + _changedObjects.size() + ", types: " + _affectedTypes + ")";
	}

}
