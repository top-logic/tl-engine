/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;


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
     * @param baseObject the object to use for evaluation the data for calculation.
     * @return any value, even <code>null</code>
     * @throws AttributeException when the performing of the algorithm is unsuccessful
     */
    public abstract Object calculate(TLObject baseObject) throws AttributeException;
    
}

