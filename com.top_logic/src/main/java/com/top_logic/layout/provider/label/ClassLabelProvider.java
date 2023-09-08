/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.util.Resources;

/**
 * a {@link LabelProvider} for {@link Class Classes} that uses {@link Class#getName()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ClassLabelProvider implements LabelProvider {

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}

		Class<?> clazz = (Class<?>) object;
		String i18nName = Resources.getInstance().getString(ResKey.forClass(clazz), null);

		if (i18nName == null) {
			if (clazz.isArray()) {
				return simpleName(clazz.getComponentType()) + "[]";
			} else {
				return simpleName(clazz);
			}
		} else {
			return i18nName + " (" + simpleName(clazz) + ")";
		}
	}

	/**
	 * Computes something similar to {@link Class#getSimpleName()} that is safe against classes with
	 * missing dependencies.
	 * 
	 * <p>
	 * The regular {@link Class#getSimpleName()} cannot be computed for an inner {@link Class} of a
	 * class extending a class that is missing on the class path. In that situation, a
	 * {@link NoClassDefFoundError} occurs. Such situation can happen in an application in
	 * development mode, where test classes are on the class path, but the JUnit library is not.
	 * </p>
	 * 
	 * @param clazz
	 *        The class to compute some simple name for.
	 * @return The class name without the package or context name.
	 */
	public static String simpleName(Class<?> clazz) {
		String className = clazz.getName();
		int idx = className.lastIndexOf('.');
		if (idx < 0) {
			return className.replace('$', '.');
		} else {
			return className.substring(idx + 1).replace('$', '.');
		}
	}

}
