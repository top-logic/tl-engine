/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Top-level {@link FormGroup} in a {@link FormComponent}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormContext extends FormGroup {

	private static final Property<FormHandler> PROPERTY_CONTEXT_HANDLER =
		TypedAnnotatable.property(FormHandler.class, "contextHandler");

	private static final Property<List<FormContextStoreAlgorithm>> PROPERTY_STORE_ALGORITHMS =
		TypedAnnotatable.propertyList("storeAlgorithms");

	/**
	 * Constant for the convenience constructor
	 * {@link #FormContext(String, ResPrefix, FormMember[])}.
	 */
    public static final FormMember[] NO_FORM_MEMBERS = new FormMember[] {};

    /**
	 * Create a FormContext from an array of predefined members.
	 * 
	 * @param resPrefix
	 *        Prefix to translate error String and labels.
	 */
	public FormContext(String name, ResPrefix resPrefix, FormMember[] members) {
		super(name, resPrefix, members);
	}

    /**
	 * Create a FormContext from an array of predefined members.
	 * 
	 * @param aI18nPrefix
	 *     Prefix to translate error String and labels.
	 */
	public FormContext(String name, ResPrefix aI18nPrefix, Collection members) {
		super(name, aI18nPrefix, members);
	}

    /**
     * Create a FormContext for the given layout component without any members.
     */
    public FormContext(LayoutComponent aComponent) {
		this(aComponent.getName().localName() + "_FormContext", aComponent.getResPrefix(), NO_FORM_MEMBERS);
    }

    public FormContext(String name, ResourceView resourceView) {
		super(name, resourceView);
	}
    
    /**
     * Create a FormContext without any members.
     * 
     * Is useful for graceful exception handling and such.
     * 
     * @param aI18nPrefix
     *            Prefix to translate error String and labels.
     */
	public FormContext(String name, ResPrefix aI18nPrefix) {
        this(name, aI18nPrefix, NO_FORM_MEMBERS);
    }

	/**
     * Return this unless this has a parent, too.
     */
	@Override
	public FormContext getFormContext() {
		if (getParent() == null) {
			return this;
		} else {
			return super.getFormContext();
		}
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitFormContext(this, arg);
	}
	/**
	 * Returns the {@link FormHandler} containing this {@link FormContext}.
	 */
	public FormHandler getOwningModel() {
		return get(PROPERTY_CONTEXT_HANDLER);
	}

	/**
	 * Stores {@link FormHandler} containing this {@link FormContext} for later access via
	 * {@link FormContext#getOwningModel()}.
	 */
	public FormHandler setOwningModel(FormHandler model) {
		return set(PROPERTY_CONTEXT_HANDLER, model);
	}

	/**
	 * Registers the given {@link FormContextStoreAlgorithm} to perform during {@link #store() store
	 * operation}.
	 */
	public void addStoreAlgorithm(FormContextStoreAlgorithm algorithm) {
		Objects.requireNonNull(algorithm, "Store algorithm must not be null.");
		List<FormContextStoreAlgorithm> algorithms;
		if (isSet(PROPERTY_STORE_ALGORITHMS)) {
			algorithms = get(PROPERTY_STORE_ALGORITHMS);
		} else {
			algorithms = new ArrayList<>();
			set(PROPERTY_STORE_ALGORITHMS, algorithms);
		}
		algorithms.add(algorithm);
	}

	/**
	 * Removes a formerly added {@link FormContextStoreAlgorithm}.
	 * 
	 * @return Whether the given algorithm was registered before.
	 * 
	 * @see #addStoreAlgorithm(FormContextStoreAlgorithm)
	 */
	public boolean removeStoreAlgorithm(FormContextStoreAlgorithm algorithm) {
		if (isSet(PROPERTY_STORE_ALGORITHMS)) {
			return get(PROPERTY_STORE_ALGORITHMS).remove(algorithm);
		}
		return false;
	}

	/**
	 * Transfers all values in this {@link FormContext} to the underlying model.
	 * 
	 * <p>
	 * Note: Before storing values, {@link #checkAll()} has to be called to verify consistency of
	 * user inputs.
	 * </p>
	 */
	public void store() {
		get(PROPERTY_STORE_ALGORITHMS).forEach(this::runStoreAlgorithm);
	}

	private void runStoreAlgorithm(FormContextStoreAlgorithm algorithm) {
		algorithm.store(this);
	}

}
