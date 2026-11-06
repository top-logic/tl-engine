/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.util;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Transition of an object of a certain type from one of a set of states to a target state.
 *
 * <p>
 * The state is the value of an attribute of {@link TLEnumeration} type. The transition is
 * applicable to an object of {@link #getModelType()}, whose current state is one of
 * {@link #getFromStates()}, and sets {@link #getToState()} as its new state.
 * </p>
 *
 * <p>
 * The transition does not open a transaction, its caller provides one.
 * </p>
 */
public class StateTransition {

	private final TLStructuredType _modelType;

	private final TLStructuredTypePart _stateAttribute;

	private final Set<TLClassifier> _fromStates;

	private final TLClassifier _toState;

	/**
	 * Creates a {@link StateTransition}.
	 *
	 * @param modelType
	 *        See {@link #getModelType()}.
	 * @param stateAttribute
	 *        See {@link #getStateAttribute()}.
	 * @param fromStates
	 *        See {@link #getFromStates()}.
	 * @param toState
	 *        See {@link #getToState()}.
	 */
	public StateTransition(TLStructuredType modelType, TLStructuredTypePart stateAttribute,
			Set<TLClassifier> fromStates, TLClassifier toState) {
		_modelType = modelType;
		_stateAttribute = stateAttribute;
		_fromStates = Set.copyOf(fromStates);
		_toState = toState;
	}

	/**
	 * The type of the objects this transition applies to.
	 */
	public TLStructuredType getModelType() {
		return _modelType;
	}

	/**
	 * The attribute of {@link #getModelType()} holding the state.
	 */
	public TLStructuredTypePart getStateAttribute() {
		return _stateAttribute;
	}

	/**
	 * The states from which the transition is allowed.
	 */
	public Set<TLClassifier> getFromStates() {
		return _fromStates;
	}

	/**
	 * The state the transition leads to.
	 */
	public TLClassifier getToState() {
		return _toState;
	}

	/**
	 * Whether the given model is an object of {@link #getModelType()} in one of the
	 * {@link #getFromStates()}.
	 */
	public boolean isApplicable(Object model) {
		if (!(model instanceof TLObject obj)) {
			return false;
		}
		if (!TLModelUtil.isCompatibleInstance(_modelType, obj)) {
			return false;
		}
		return _fromStates.contains(currentState(obj));
	}

	/**
	 * The current state of the given object.
	 */
	public Object currentState(TLObject obj) {
		Object state = obj.tValue(_stateAttribute);
		if (AttributeOperations.isCollectionValued(_stateAttribute)) {
			state = CollectionUtil.getSingleValueFrom(state);
		}
		return state;
	}

	/**
	 * The value to store in {@link #getStateAttribute()} for {@link #getToState()}.
	 */
	public Object targetValue() {
		return AttributeOperations.isCollectionValued(_stateAttribute) ? Collections.singleton(_toState) : _toState;
	}

	/**
	 * Performs the transition on the given object.
	 *
	 * @throws TopLogicException
	 *         If the transition is not {@link #isApplicable(Object) applicable} to the given
	 *         object.
	 */
	public void apply(TLObject obj) {
		if (!isApplicable(obj)) {
			Object state = TLModelUtil.isCompatibleInstance(_modelType, obj) ? currentState(obj) : null;
			throw new TopLogicException(I18NConstants.ERROR_STATE_TRANSITION_NOT_ALLOWED__OBJECT_STATE_TARGET
				.fill(obj, state, _toState));
		}
		obj.tUpdate(_stateAttribute, targetValue());
	}

}
