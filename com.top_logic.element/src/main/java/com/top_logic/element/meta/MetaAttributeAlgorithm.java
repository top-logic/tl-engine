/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;


import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.config.algorithm.GenericAlgorithmConfig;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;

/**
 * This interface defines how an algorithm has to be implemented
 * An Algorithm for calculation an attribute value.
 * the Algorithm only defines a method to calculate a value.
 * 
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public abstract class MetaAttributeAlgorithm {
    
	/**
	 * Creates a {@link MetaAttributeAlgorithm} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MetaAttributeAlgorithm(InstantiationContext context, GenericAlgorithmConfig config) {
		// Ensure configuration constructor.
	}

    /**
     * Starting with the given {@link Wrapper} perform a calculation.
     * Any possible mechanism is allowed here.
     * 
     * @param anAttributedObject the object to use for evaluation the data for calculation.
     * @return any value, even <code>null</code>
     * @throws AttributeException when the performing of the algorithm is unsuccessful
     */
    public abstract Object calculate(Wrapper anAttributedObject) throws AttributeException;
    
	/**
	 * Determines the {@link TLObject} that have the given {@link TLObject} as value in
	 * {@link #calculate(Wrapper)}.
	 * 
	 * <p>
	 * Sources for <code>null</code> can not be found.
	 * </p>
	 * 
	 * @param value
	 *        The value to get {@link #calculate(Wrapper) sources} for. Must not be
	 *        <code>null</code>.
	 * @return The {@link TLObject}s for which the given value would be calculated.
	 * @throws AttributeException
	 *         when the performing of the algorithm is unsuccessful
	 */
	public Set<? extends TLObject> getReferers(TLObject value) throws AttributeException {
		return Collections.emptySet();
	}

}

