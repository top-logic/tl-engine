/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

import java.util.Set;

import com.top_logic.model.TLStructuredTypePart;

/**
 * The model.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ClassificationModel {

	private Set<Object> objects;
	private CollectionToNumberCalculator calculator;
    private final TLStructuredTypePart metaAttribute;

    /**
	 * Creates a new ClassificationModel.
	 * 
	 * @param    aCalculator    A calculator to calculate a number of each classification set. The number is displayed over each classification set.
	 */
    public ClassificationModel(Set<Object> someItems, CollectionToNumberCalculator aCalculator) {
        this(someItems, aCalculator, null);
    }

    /**
     * Creates a new ClassificationModel.
     * 
     * @param    aCalculator    A calculator to calculate a number of each classification set. The number is displayed over each classification set.
     * @param    aMA            Represented meta attribute, may be <code>null</code>.
     */
    public ClassificationModel(Set<Object> someItems, CollectionToNumberCalculator aCalculator, TLStructuredTypePart aMA) {
		this.objects     = someItems;
		this.calculator    = aCalculator;
		this.metaAttribute = aMA;
	}

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + " [" + this.toStringValues() + ']';
    }

    /**
	 * Returns the held objects.
	 */
	public Set<Object> getObjects() {
		return this.objects;
	}

	/**
	 * Returns the calculator.
	 */
	public CollectionToNumberCalculator getCalculator() {
		return this.calculator;
	}

    /**
     * This method returns the held meta attribute.
     * 
     * @return    Returns the meta attribute, may be <code>null</code>.
     */
    public TLStructuredTypePart getMetaAttribute() {
        return this.metaAttribute;
    }

	/**
	 * See get-method.
	 */
	public void setObjects(Set<Object> someObjects) {
		this.objects = someObjects;
	}

	/**
	 * See get-method.
	 */
	public void setCalculator(CollectionToNumberCalculator calculator) {
		this.calculator = calculator;
	}

	/** 
     * Return the significant attributes to be used in the {@link #toString()} method.
     * 
     * @return    The requested attributes.
     */
    protected String toStringValues() {
        return "calculator: " + this.calculator + ", metaAttribute: " + this.metaAttribute;
    }
}
