/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.List;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.KeyEventListener;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;

/**
 * Base class for custom {@link FormField}s that are composed of several standard ones.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CompositeField extends FormGroup implements FormField {

	/**
	 * Creates a {@link CompositeField}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param aLabelRessource
	 *        The {@link ResourceView} used to label all contents.
	 */
	public CompositeField(String name, ResourceView aLabelRessource) {
		super(name, aLabelRessource);
	}

	@Override
	public boolean isMandatory() {
		return getProxy().isMandatory();
	}

	@Override
	public boolean isFrozen() {
		return getProxy().isFrozen();
	}

	@Override
	public void setFrozen(boolean frozen) {
		getProxy().setFrozen(frozen);
	}

	@Override
	public void setMandatory(boolean mandatory) {
		getProxy().setMandatory(mandatory);
	}

	@Override
	public boolean isBlocked() {
		return getProxy().isBlocked();
	}

	@Override
	public void setBlocked(boolean blocked) {
		getProxy().setBlocked(blocked);
	}

	@Override
	public void update(Object newRawValue) throws VetoException {
		getProxy().update(newRawValue);
	}

	@Override
	public boolean listensForUpdate() {
		return getProxy().listensForUpdate();
	}

	@Override
	public boolean isCheckRequired(boolean isFinalSubmit) {
		return getProxy().isCheckRequired(isFinalSubmit);
	}

	@Override
	public void check() {
		getProxy().check();
	}

	@Override
	public boolean checkConstraints() {
		return getProxy().checkConstraints();
	}

	@Override
	public boolean checkConstraints(Object value) {
		return getProxy().checkConstraints(value);
	}

	@Override
	public String getError() {
		return getProxy().getError();
	}

	@Override
	public void setError(String message) {
		getProxy().setError(message);
	}

	@Override
	public void clearError() {
		getProxy().clearError();
	}

	@Override
	public List<String> getWarnings() {
		return getProxy().getWarnings();
	}

	@Override
	public void setWarnings(List<String> messages) {
		getProxy().setWarnings(messages);
	}

	@Override
	public boolean isValid() {
		return getProxy().isValid();
	}

	@Override
	public boolean hasValue() {
		return getProxy().hasValue();
	}

	@Override
	public Object getValue() {
		return getProxy().getValue();
	}

	@Override
	public void setValue(Object value) {
		getProxy().setValue(value);
	}

	@Override
	public void setDefaultValue(Object defaultValue) {
		getProxy().setDefaultValue(defaultValue);
	}

	@Override
	public Object getDefaultValue() {
		return getProxy().getDefaultValue();
	}

	@Override
	public void initializeField(Object defaultValue) {
		getProxy().initializeField(defaultValue);
	}

	@Override
	public Object getRawValue() {
		return getProxy().getRawValue();
	}

	@Override
	public FormField addConstraint(Constraint constraint) {
		getProxy().addConstraint(constraint);
		return this;
	}

	@Override
	public boolean removeConstraint(Constraint constraint) {
		return getProxy().removeConstraint(constraint);
	}

	@Override
	public List<Constraint> getConstraints() {
		return getProxy().getConstraints();
	}

	@Override
	public List<Constraint> getWarningConstraints() {
		return getProxy().getWarningConstraints();
	}

	@Override
	public FormField addWarningConstraint(Constraint constraint) {
		getProxy().addWarningConstraint(constraint);
		return this;
	}

	@Override
	public boolean removeWarningConstraint(Constraint constraint) {
		return getProxy().removeWarningConstraint(constraint);
	}

	@Override
	public void setExampleValue(Object exampleValue) {
		getProxy().setExampleValue(exampleValue);
	}

	@Override
	public Object getExampleValue() {
		return getProxy().getExampleValue();
	}

	@Override
	public boolean addValueListener(ValueListener listener) {
		return getProxy().addValueListener(listener);
	}

	@Override
	public boolean removeValueListener(ValueListener listener) {
		return getProxy().removeValueListener(listener);
	}

	@Override
	public boolean hasValueListeners() {
		return getProxy().hasValueListeners();
	}

	@Override
	public List<ValueListener> getValueListeners() {
		return getProxy().getValueListeners();
	}

	@Override
	public boolean addKeyListener(KeyEventListener listener) {
		return getProxy().addKeyListener(listener);
	}

	@Override
	public boolean removeKeyListener(KeyEventListener listener) {
		return getProxy().removeKeyListener(listener);
	}

	@Override
	public boolean addValueVetoListener(ValueVetoListener listener) {
		return getProxy().addValueVetoListener(listener);
	}

	@Override
	public boolean removeValueVetoListener(ValueVetoListener listener) {
		return getProxy().removeValueVetoListener(listener);
	}

	@Override
	public void checkWithAllDependencies() {
		getProxy().checkWithAllDependencies();
	}

	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		if (FormMember.FORM_MEMBER_EVENT_TYPES.contains(type)) {
			return super.addListener(type, listener);
		}
		return getProxy().addListener(type, listener);
	}

	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
		if (FormMember.FORM_MEMBER_EVENT_TYPES.contains(type)) {
			return super.removeListener(type, listener);
		}
		return getProxy().removeListener(type, listener);
	}

	@Override
	protected <L extends PropertyListener, S, V> EventType.Bubble notifyListeners(EventType<L, ? super S, V> type,
			S sender, V oldValue, V newValue) {
		return super.notifyListeners(type, sender, oldValue, newValue);
	}

	/**
	 * The proxy {@link FormField} that receives all {@link FormField} methods of this
	 * {@link CompositeField}.
	 */
	protected abstract FormField getProxy();

}
