/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal.gen;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * An identifier for a Java method declaration.
 * 
 * <p>
 * Two methods with the same {@link Signature} override each other.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Signature {

	private String _name;

	private Class<?>[] _parameterTypes;

	private Signature(String name, Class<?>[] parameterTypes) {
		_name = name;
		_parameterTypes = parameterTypes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		result = prime * result + Arrays.hashCode(_parameterTypes);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Signature other = (Signature) obj;
		if (_name == null) {
			if (other._name != null)
				return false;
		} else if (!_name.equals(other._name))
			return false;
		if (!Arrays.equals(_parameterTypes, other._parameterTypes))
			return false;
		return true;
	}

	/**
	 * Creates a {@link Signature} for the given {@link Method}.
	 */
	public static Signature signature(Method visitMethod) {
		return new Signature(visitMethod.getName(), visitMethod.getParameterTypes());
	}

}
