/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.ScriptImplementations;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.util.Resources;

/**
 * Labels and tooltips of the script properties of a script implementation, as configuration
 * editors display them.
 *
 * <p>
 * A script implementation of a property (see
 * {@link ScriptImplementations#acceptsScript(PropertyDescriptor)}) is a configuration with one
 * script per implemented method. The generic documentation of these script properties says
 * nothing about what the script computes, this is documented at the property that is implemented
 * (the <i>owner</i>). Therefore:
 * </p>
 *
 * <ul>
 * <li>If the implemented interface has a single abstract method (a functional interface), its
 * script is labelled and documented like the owner. The scripts of its default methods keep
 * their own labels.</li>
 * <li>Otherwise, each script keeps the label and documentation of its method, the owner's
 * documentation is shown at the group of the implementation.</li>
 * <li>The tooltip of each script ends with the signature of the implemented method, with the type
 * arguments of the owner resolved, e.g. "Arguments: 1. TLObject, 2. ResKey – Result: Object".</li>
 * </ul>
 *
 * <p>
 * Used by the classic configuration editor ({@link EditorFactory}) and the React configuration
 * editor alike.
 * </p>
 */
public final class ScriptImplementationLabels {

	private static final String TOOLTIP = "tooltip";

	private ScriptImplementationLabels() {
		// Utility class.
	}

	/**
	 * The label of the given property of a script implementation.
	 *
	 * @param owner
	 *        The property the given item implements, <code>null</code> if unknown.
	 * @param item
	 *        The configuration item holding the given property.
	 * @param property
	 *        The property to label.
	 * @return The label of the owner, if the given property is the script of the functional method
	 *         of a script implementation, <code>null</code> if the default label of the given
	 *         property applies.
	 */
	public static String label(PropertyDescriptor owner, ConfigurationItem item, PropertyDescriptor property) {
		if (owner == null || !isSingleScript(item, property)) {
			return null;
		}
		return Labels.propertyLabel(owner, false);
	}

	/**
	 * The tooltip (HTML) of the given property of a script implementation.
	 *
	 * @param owner
	 *        The property the given item implements, <code>null</code> if unknown.
	 * @param item
	 *        The configuration item holding the given property.
	 * @param property
	 *        The property to document.
	 * @return The documentation of the owner (for the script of the functional method) or of the
	 *         given property (for one of several scripts), followed by the signature of the
	 *         implemented method; <code>null</code>, if the given property is no script of a script
	 *         implementation, and its default tooltip applies.
	 */
	public static String tooltip(PropertyDescriptor owner, ConfigurationItem item, PropertyDescriptor property) {
		Method method = ScriptImplementations.getImplementedMethod(item, property);
		if (method == null) {
			return null;
		}
		PropertyDescriptor documented = owner != null && isSingleScript(item, property) ? owner : property;
		String documentation = Labels.propertyLabel(documented, "." + TOOLTIP, true);
		String signature = TagUtil.encodeXML(signature(owner, method));
		if (documentation == null || documentation.isBlank()) {
			return signature;
		}
		return documentation + "<p>" + signature + "</p>";
	}

	/**
	 * The signature of the given method, as implemented for the given owner.
	 *
	 * @param owner
	 *        The property whose instances implement the given method, <code>null</code> if
	 *        unknown. The type arguments of its {@link ConfigUtil#getGenericInstanceType(PropertyDescriptor)
	 *        instance type} resolve the types of the method.
	 * @param method
	 *        The implemented method.
	 * @return The argument and result types, e.g. "Arguments: 1. TLObject, 2. ResKey – Result:
	 *         Object".
	 */
	public static String signature(PropertyDescriptor owner, Method method) {
		Type context = owner == null ? null
			: JavaTypeUtil.eraseTypeVariables(JavaTypeUtil.upperBound(ConfigUtil.getGenericInstanceType(owner)));
		String result = typeName(context, method.getGenericReturnType());
		Type[] parameters = method.getGenericParameterTypes();
		if (parameters.length == 0) {
			return Resources.getInstance().getString(I18NConstants.SCRIPT_SIGNATURE__RESULT.fill(result));
		}
		StringBuilder arguments = new StringBuilder();
		for (int n = 0; n < parameters.length; n++) {
			if (n > 0) {
				arguments.append(", ");
			}
			arguments.append(n + 1).append(". ").append(typeName(context, parameters[n]));
		}
		return Resources.getInstance()
			.getString(I18NConstants.SCRIPT_SIGNATURE__ARGUMENTS_RESULT.fill(arguments.toString(), result));
	}

	private static String typeName(Type context, Type type) {
		Type resolved = context == null ? type : JavaTypeUtil.resolve(context, type);
		return JavaTypeUtil.toSimpleTypeName(resolved);
	}

	/**
	 * Whether the given property is the script of the functional method of the script
	 * implementation holding it: it implements the only abstract method of the implemented
	 * interface. Scripts of default methods do not count, they only override a Java
	 * implementation.
	 */
	private static boolean isSingleScript(ConfigurationItem item, PropertyDescriptor property) {
		Method method = ScriptImplementations.getImplementedMethod(item, property);
		if (method == null || method.isDefault()) {
			return false;
		}
		for (PropertyDescriptor other : item.descriptor().getProperties()) {
			if (other.getPropertyName().equals(property.getPropertyName())) {
				continue;
			}
			Method otherMethod = ScriptImplementations.getImplementedMethod(item, other);
			if (otherMethod != null && !otherMethod.isDefault()) {
				return false;
			}
		}
		return true;
	}

}
