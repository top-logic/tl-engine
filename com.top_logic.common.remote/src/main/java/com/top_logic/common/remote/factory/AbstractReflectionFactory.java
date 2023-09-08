/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.factory;

import java.lang.reflect.InvocationTargetException;

import com.top_logic.common.remote.shared.ConstantData;
import com.top_logic.common.remote.shared.HandleFactory;
import com.top_logic.common.remote.shared.ObjectData;
import com.top_logic.common.remote.shared.ObjectScope;

/**
 * {@link HandleFactory} that uses Java reflection to directly instantiate network types that equal
 * qualified Java class names.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractReflectionFactory implements HandleFactory {

	/**
	 * Creates a {@link AbstractReflectionFactory}.
	 */
	protected AbstractReflectionFactory() {
		// Singleton constructor.
	}

	@Override
	public ObjectData createHandle(String typeName, ObjectScope scope) {
		try {
			int sepIdx = typeName.indexOf('#');
			if (sepIdx >= 0) {
				String className = typeName.substring(0, sepIdx);
				Class<?> enumClass = Class.forName(className);
				Object literalName = typeName.substring(sepIdx + 1);
				for (Object x : enumClass.getEnumConstants()) {
					if (((Enum<?>) x).name().equals(literalName)) {
						return new ConstantData(scope, x);
					}
				}
				throw new IllegalArgumentException("Invalid enum type '" + typeName + "'.");
			} else {
				Class<?> javaClass = Class.forName(typeName);
				Object handle = newInstance(javaClass, scope);
				return scope.data(handle);
			}
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Handle creation failed for type '" + typeName + "'.", ex);
		}
	}

	/**
	 * Actually instantiates the shared object of the given type in the given scope.
	 * 
	 * @param javaClass
	 *        The shared object class.
	 * @param scope
	 *        The {@link ObjectScope} to operate on.
	 * @return The newly created shared object.
	 */
	protected Object newInstance(Class<?> javaClass, ObjectScope scope)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return javaClass.getConstructor(ObjectScope.class).newInstance(scope);
	}

}
