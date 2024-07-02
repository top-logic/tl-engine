/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.algorithm;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.config.algorithm.MethodAlgorithmConfig;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.MetaAttributeAlgorithm;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.TLObject;

/**
 * {@link MetaAttributeAlgorithm} that calls a method on the object and returns the method value.
 * 
 * Note this algorithm works only with methods without parameters!!!
 * 
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class MethodInvocationAlgorithm extends MetaAttributeAlgorithm {

	/**
	 * The method name that is called at the attributed.
	 */
	public String methodName;
	
	/** The method derive from the members above */
	private transient Map<Class<?>, Method> methodMap;
	
	/**
	 * Creates a {@link MethodInvocationAlgorithm} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MethodInvocationAlgorithm(InstantiationContext context, MethodAlgorithmConfig config) {
		super(context, config);

		methodName = config.getMethodName();
	}
	
	/** 
	 * This method returns the object that is returned of the configured method.
	 * See {@link #methodName}.
	 * 
	 * @see com.top_logic.element.meta.MetaAttributeAlgorithm#calculate(TLObject)
	 */
    @Override
	public Object calculate(TLObject anAttributed) throws AttributeException {
		try {
		    Class<?> theClass = anAttributed.getClass();
		    
		    if (this.methodMap == null) {
		        this.methodMap = new HashMap<>();
		    }

		    Method theMethod = this.methodMap.get(theClass);
		    
		    if (theMethod == null) {
                theMethod = theClass.getMethod(this.methodName);

                this.methodMap.put(theClass, theMethod);
		    }

		    return theMethod.invoke(anAttributed);
		}
		catch (Exception ex) {
			throw new AttributeException(
				"The method ('" + this.methodName + "') could not be found or called for the attributed ('"
					+ anAttributed + "[" + KBUtils.getWrappedObjectKeyString(anAttributed) + "]" + "')",
				ex);
		} 
	}

}
