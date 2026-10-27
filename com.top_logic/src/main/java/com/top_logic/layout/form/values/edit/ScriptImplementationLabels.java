/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.MethodProperty;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.ScriptImplementations;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.util.Resources;

/**
 * Labels and tooltips of the method properties of a script implementation, as configuration
 * editors display them.
 *
 * <p>
 * A script implementation of a property (see
 * {@link ScriptImplementations#acceptsScript(PropertyDescriptor)}) is a configuration with one
 * property per implemented method: a script, or a plain value for a method without parameters
 * returning a value. What a method computes is documented at the method of the implemented
 * interface, and at the property that is implemented (the <i>owner</i>). The documentation of an
 * interface is available as resources, if the interface is exported for in-app display (see the
 * key scheme below). Therefore:
 * </p>
 *
 * <ul>
 * <li>If the implemented interface has a single abstract method (a functional interface), the
 * property of this method is labelled like the owner. Its tooltip is the documentation of the
 * owner followed by the documentation of the method.</li>
 * <li>Every other method property is labelled and documented like its method, falling back to
 * the label and documentation of the property itself.</li>
 * <li>The tooltip of a script ends with the signature of the implemented method: each argument
 * with its name, its type (with the type arguments of the owner resolved) and its documentation,
 * and the result type with its documentation. The tooltip of a value property ends with the
 * documentation of the result.</li>
 * </ul>
 *
 * <p>
 * The resources of a method <code>m</code> of an interface <code>F</code> are
 * <code>F.m</code> (label), <code>F.m.tooltip</code> (documentation),
 * <code>F.m.param.p.tooltip</code> (documentation of parameter <code>p</code>) and
 * <code>F.m.return</code> (documentation of the result). Each of them is optional.
 * </p>
 *
 * <p>
 * Used by the classic configuration editor ({@link EditorFactory}) and the React configuration
 * editor alike.
 * </p>
 */
public final class ScriptImplementationLabels {

	private static final String TOOLTIP = "tooltip";

	private static final String PARAM = "param";

	private static final String RETURN = "return";

	/**
	 * Prefix of the generic name of an argument whose name is not known.
	 */
	public static final String ARGUMENT_PREFIX = "arg";

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
	 * @return The label of the owner, if the given property implements the functional method of a
	 *         script implementation, the label of the implemented method otherwise;
	 *         <code>null</code> if the default label of the given property applies.
	 */
	public static String label(PropertyDescriptor owner, ConfigurationItem item, PropertyDescriptor property) {
		Method method = ScriptImplementations.getImplementedMethod(item, property);
		if (method == null) {
			return null;
		}
		if (owner != null && isSingleScript(item, property)) {
			return Labels.propertyLabel(owner, false);
		}
		return resolve(methodKey(method));
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
	 * @return The documentation as described in {@link ScriptImplementationLabels};
	 *         <code>null</code>, if the given property is no method property of a script
	 *         implementation, and its default tooltip applies.
	 */
	public static String tooltip(PropertyDescriptor owner, ConfigurationItem item, PropertyDescriptor property) {
		Method method = ScriptImplementations.getImplementedMethod(item, property);
		if (method == null) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		ResKey methodKey = methodKey(method);
		String methodDoc = resolve(methodKey.suffix(TOOLTIP));
		if (owner != null && isSingleScript(item, property)) {
			append(result, Labels.propertyLabel(owner, "." + TOOLTIP, true));
			append(result, methodDoc);
		} else {
			append(result, methodDoc != null ? methodDoc : Labels.propertyLabel(property, "." + TOOLTIP, true));
		}
		if (MethodProperty.isValueType(property.getType())) {
			String returnDoc = resolve(methodKey.suffix(RETURN));
			if (returnDoc != null) {
				result.append(section(I18NConstants.SCRIPT_RESULT)).append(' ').append(returnDoc).append("</p>");
			}
		} else {
			result.append(signature(owner, method));
		}
		return result.toString();
	}

	/**
	 * The signature of the given method as HTML, as implemented for the given owner.
	 *
	 * @param owner
	 *        The property whose instances implement the given method, <code>null</code> if
	 *        unknown. The type arguments of its {@link ConfigUtil#getGenericInstanceType(PropertyDescriptor)
	 *        instance type} resolve the types of the method.
	 * @param method
	 *        The implemented method.
	 * @return A list of the arguments with their {@link #argumentName(Method, int) names}, types and
	 *         documentation, and the result type with its documentation.
	 */
	public static String signature(PropertyDescriptor owner, Method method) {
		Type context = owner == null ? null
			: JavaTypeUtil.eraseTypeVariables(JavaTypeUtil.upperBound(ConfigUtil.getGenericInstanceType(owner)));
		ResKey methodKey = methodKey(method);
		StringBuilder out = new StringBuilder();
		Parameter[] parameters = method.getParameters();
		Type[] parameterTypes = method.getGenericParameterTypes();
		if (parameters.length > 0) {
			out.append(section(I18NConstants.SCRIPT_ARGUMENTS)).append("</p><ul>");
			for (int n = 0; n < parameters.length; n++) {
				out.append("<li><code>").append(TagUtil.encodeXML(argumentName(method, n))).append("</code>: ");
				out.append("<code>").append(TagUtil.encodeXML(typeName(context, parameterTypes[n]))).append("</code>");
				if (parameters[n].isNamePresent()) {
					appendDoc(out,
						resolve(methodKey.suffix(PARAM).suffix(parameters[n].getName()).suffix(TOOLTIP)));
				}
				out.append("</li>");
			}
			out.append("</ul>");
		}
		out.append(section(I18NConstants.SCRIPT_RESULT)).append(" <code>")
			.append(TagUtil.encodeXML(typeName(context, method.getGenericReturnType()))).append("</code>");
		appendDoc(out, resolve(methodKey.suffix(RETURN)));
		out.append("</p>");
		return out.toString();
	}

	/**
	 * The name of the argument with the given index of the given method.
	 *
	 * @return The name of the parameter, if it is known (the class was compiled with parameter
	 *         names) and consists of ASCII letters, digits and underscores only; otherwise
	 *         {@link #ARGUMENT_PREFIX} followed by the number of the argument, starting at 1.
	 */
	public static String argumentName(Method method, int index) {
		Parameter parameter = method.getParameters()[index];
		if (parameter.isNamePresent() && PLAIN_NAME.matcher(parameter.getName()).matches()) {
			return parameter.getName();
		}
		return ARGUMENT_PREFIX + (index + 1);
	}

	private static final Pattern PLAIN_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

	private static String section(ResKey title) {
		return "<p><b>" + TagUtil.encodeXML(Resources.getInstance().getString(title)) + ":</b>";
	}

	private static void appendDoc(StringBuilder out, String doc) {
		if (doc != null && !doc.isBlank()) {
			out.append(" \u2013 ").append(doc);
		}
	}

	/**
	 * Appends the given documentation as a block of its own, so that consecutive documentations
	 * do not run into each other.
	 */
	private static void append(StringBuilder out, String doc) {
		if (doc != null && !doc.isBlank()) {
			out.append("<div>").append(doc).append("</div>");
		}
	}

	/**
	 * The key of the resources of the given method, see {@link ScriptImplementationLabels}.
	 */
	private static ResKey methodKey(Method method) {
		return ResKey.forClass(method.getDeclaringClass()).suffix(method.getName());
	}

	/**
	 * The value of the given key, <code>null</code> without logging, if it is missing.
	 */
	private static String resolve(ResKey key) {
		String result = Resources.getInstance().getString(key.optional());
		return result == null || result.isBlank() ? null : result;
	}

	private static String typeName(Type context, Type type) {
		Type resolved = context == null ? type : JavaTypeUtil.resolve(context, type);
		return JavaTypeUtil.toSimpleTypeName(resolved);
	}

	/**
	 * Whether the given property implements the functional method of the script implementation
	 * holding it: the only abstract method of the implemented interface. Properties of default
	 * methods do not count, they only override a Java implementation.
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
