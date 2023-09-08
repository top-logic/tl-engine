/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal.gen;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptorBuilder;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.internal.ItemList;
import com.top_logic.basic.config.internal.ItemMap;
import com.top_logic.basic.generate.CodeUtil;

/**
 * Algorithm generating code for a certain {@link PropertyDescriptor}.
 * 
 * @see ConfigItemGenerator
 * 
 * @since 5.8.0
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class PropertyWriter {

	enum PropertyInheritance {

		/**
		 * Property is defined in the configuration type but not defined by the selected super
		 * implementation class.
		 * 
		 * <p>
		 * For a declared property, no implementation aspects are inherited from the chosen super
		 * implementation class. The property is not necessarily defined directly by the implemented
		 * configuration interface, but may be defined anywhere in a non-primary super interface of
		 * the implemented configuration type.
		 * </p>
		 */
		DECLARED,

		/**
		 * Property is already implemented by the chosen super implementation class but is not
		 * {@link #REDECLARED}.
		 */
		INHERITED,

		/**
		 * Property is already implemented by the chosen super implementation class, but the
		 * property is redeclared either in the implemented configuration type or in a non-primary
		 * super interface thereof.
		 */
		REDECLARED,

		;
	}

	private static final String VARIABLE_VISIBILITY = "private";

	/**
	 * Name of the index parameter of an indexed setter.
	 */
	private static final String INDEX_PARAM = "index";

	/**
	 * Name of the setter parameter providing the new value.
	 */
	private static final String VALUE_PARAM = "value";

	/**
	 * Parameters of getter methods.
	 */
	private static final String[] GETTER_PARAMS = { INDEX_PARAM };

	/**
	 * Parameters of setter methods.
	 */
	private static final String[] SETTER_PARAMS = { INDEX_PARAM, VALUE_PARAM };

	public static PropertyWriter newPropertyWriter(ConfigItemGenerator generator, PropertyDescriptorSPI property,
			PropertyDescriptorSPI inheritedProperty, int index, PropertyInheritance inheritance) {
		return new PropertyWriter(generator, property, inheritedProperty, index, inheritance);
	}

	private final ConfigItemGenerator _generator;

	private Method _setter;

	private String _getterName;

	private Method _getter;

	private Type _type;

	private String _varValue;

	private String _varIsSet;

	private String _indexConstant;

	private String _propertyConstant;

	int _index;

	private final PropertyDescriptorSPI _descriptor;

	private final PropertyDescriptorSPI _inheritedDescriptor;

	private String _internalPropertyName;

	private String _internalGetMethod;

	private String _internalUpdateMethod;

	private String _internalSetMethod;

	private String _internalValueSetMethod;

	private String _propertyAccessMethod;

	private String _initMethod;

	private String _normalizeMethod;

	private PropertyInheritance _inheritance;

	private PropertyWriter(ConfigItemGenerator generator, PropertyDescriptorSPI descriptor,
			PropertyDescriptorSPI inheritedDescriptor, int index,
			PropertyInheritance inheritance) {
		_generator = generator;
		_descriptor = descriptor;
		_inheritedDescriptor = inheritedDescriptor;
		_inheritance = inheritance;
		initGetterSetter();
		_varValue = "_" + _internalPropertyName;
		initConstants(index, _internalPropertyName);
		String methodSuffix = CodeUtil.toUpperCaseStart(_internalPropertyName);
		_internalGetMethod = "_get" + methodSuffix;
		_internalUpdateMethod = "_update" + methodSuffix;
		_internalSetMethod = "_set" + methodSuffix;
		_internalValueSetMethod = "_isSet" + methodSuffix;
		_varIsSet = _varValue + "__set";
		_initMethod = "_init" + methodSuffix;
		_normalizeMethod = "_normalize" + methodSuffix;
		_propertyAccessMethod = "_property" + methodSuffix;
	}

	private void initConstants(int index, String variableName) {
		_index = index;
		String prefix = "_" + CodeUtil.toAllUpperCase(variableName);
		_indexConstant = prefix + "__IDX";
		_propertyConstant = prefix + "__PRP";
	}

	private void initGetterSetter() {
		switch (_inheritance) {
			case INHERITED: {
				_getter = _descriptor.getSomeGetter();
				break;
			}
			case DECLARED: {
				findAccessors(_descriptor);
				break;
			}
			case REDECLARED: {
				_setter = _descriptor.getSetter();
				_getter = _descriptor.getSomeGetter();
				break;
			}
		}
		if (_getter == null) {
			throw propertyWithoutGetter();
		}
		setInternalPropertyName();
		_type = _descriptor.getGenericType();
	}

	private void findAccessors(PropertyDescriptorSPI property) {
		if (_getter == null) {
			_getter = property.getLocalGetter();
		}
		if (_setter == null) {
			_setter = property.getSetter();
		}
		for (PropertyDescriptorSPI superProperty : property.getSuperProperties()) {
			if (_setter != null && _getter != null) {
				break;
			}
			findAccessors(superProperty);
		}
	}

	private IllegalArgumentException propertyWithoutGetter() {
		return new IllegalArgumentException("Property without getter.");
	}

	private void setInternalPropertyName() {
		_getterName = _getter.getName();
		Matcher matcher = ConfigurationDescriptorBuilder.METHOD_NAME_PATTERN.matcher(_getterName);
		if (!matcher.matches()) {
			throw new ConfigurationError("Property with incorrect getter name '" + _getterName + "'.");
		}
		_internalPropertyName = CodeUtil.toLowerCaseStart(matcher.group(ConfigurationDescriptorBuilder.NAME_GROUP));
	}

	public void writeInitValueCall() {
		if (_inheritedDescriptor != null) {
			return;
		}
		writeInitCall();
	}

	public void writeInitCopyCall(String otherVar) {
		if (_inheritedDescriptor != null) {
			return;
		}
		line(_initMethod + "(" + otherVar + ");");
	}

	public void writeMethods() {
		String propKind = kind().name().toLowerCase();
		String kind;
		switch (_inheritance) {
			case INHERITED:
				kind = "Inherited " + propKind;
				break;
			case REDECLARED:
				kind = "Re-declared " + propKind;
				break;
			default:
				kind = CodeUtil.toUpperCaseStart(propKind);
				break;
		}
		comment(kind + " property '" + _descriptor.getPropertyName() + "'");
		nl();
		writePropertyAccess();
		writeGetter();
		writeIndexedGetters();
		writeSetter();
		writeInternalGetter();
		writeInternalValueSet();
		writeInitValue();
		writeInitCopy();
		writeInternalUpdate();
		writeInternalSet();
		writeNormalizeValue();
	}

	/**
	 * Writes the hook for accessing a reference to the concrete {@link PropertyDescriptor} for a
	 * (potentially overridden) property.
	 */
	private void writePropertyAccess() {
		if (isSimplyInherited()) {
			return;
		}

		if (_inheritedDescriptor != null) {
			line("@Override");
		}

		append("protected ");
		appendType(PropertyDescriptor.class);
		append(" ");
		append(_propertyAccessMethod);
		append("() {");
		flushLine();

		append("return ");
		append(_propertyConstant);
		append(";");
		flushLine();

		line("}");
		nl();
	}

	public void writeNormalizeValue() {
		if (_inheritedDescriptor != null) {
			if (sameKind()) {
				// No need to re-declare.
				return;
			}
			line("@Override");
		}

		append("protected ");
		appendPropertyType(false);
		append(" ");
		append(_normalizeMethod);
		append("(");
		appendType(PropertyDescriptor.class);
		append(" property, ");
		append("Object value");
		append(") {");
		flushLine();
		switch (kind()) {
			case PLAIN: {
				boolean nullable = !primitive();
				if (nullable) {
					line("if (value != null || !property.isNullable()) {");
				}
				{
					append("value = ");
					append("property.getValueProvider().normalize(value);");
					flushLine();
				}
				if (nullable) {
					line("}");
				}
				break;
			}
			case COMPLEX: {
				boolean nullable = !primitive();
				if (nullable) {
					line("if (value != null || !property.isNullable()) {");
				}
				{
					append("value = ");
					append("property.getValueBinding().normalize(value);");
					flushLine();
				}
				if (nullable) {
					line("}");
				}
				break;
			}
			case ARRAY: {
				line("if (value == null) {");
				{
					append("value = ");
					append("new ");
					appendType(_descriptor.getElementType());
					append("[0];");
					flushLine();
				}
				line("}");
				break;
			}
			case LIST: {
				line("if (value == null) {");
				{
					append("value = ");
					appendType(Collections.class);
					append(".emptyList();");
					flushLine();
				}
				line("}");
				break;
			}
			case MAP: {
				line("if (value == null) {");
				{
					append("value = ");
					appendType(Collections.class);
					append(".emptyMap();");
					flushLine();
				}
				line("}");
				break;
			}
			case ITEM:
			case REF:
			case DERIVED: {
				// No normalization.
			}
		}
		appendType(PropertyDescriptorImpl.class);
		append(".checkValue(property, value);");
		flushLine();

		append("return ");
		append("(");
		appendPropertyType(false);
		append(")");
		append("value");
		append(";");
		flushLine();
		line("}");
		nl();
	}

	private boolean sameKind() {
		return _inheritedDescriptor.kind() == _descriptor.kind();
	}

	private boolean primitive() {
		return _type instanceof Class<?> && ((Class<?>) _type).isPrimitive();
	}

	public void writeInitValue() {
		if (_inheritedDescriptor != null) {
			if (sameInitializer()) {
				// No need to re-declare.
				return;
			}
			line("@Override");
		}

		line("protected void " + _initMethod + "() {");
		{
			appendType(PropertyDescriptor.class);
			append(" property = ");
			append(_propertyAccessMethod);
			append("();");
			flushLine();

			writeInitBody("property", "property.getDefaultValue()", "false");
		}
		line("}");
		nl();
	}

	private void writeInitCopy() {
		if (_inheritedDescriptor != null) {
			if (sameInitializer()) {
				// No need to re-declare.
				return;
			}
			line("@Override");
		}

		append("protected void ");
		append(_initMethod);
		append("(");
		appendType(ConfigurationItem.class);
		append(" other) {");
		flushLine();

		appendType(PropertyDescriptor.class);
		append(" property = ");
		append(_propertyAccessMethod);
		append("();");
		flushLine();

		writeInitBody("property", "other.value(property)", "other.valueSet(property)");
		line("}");
		nl();
	}

	private boolean sameInitializer() {
		return sameKind() && _descriptor.hasContainerAnnotation() == _inheritedDescriptor.hasContainerAnnotation();
	}

	private void writeInitBody(String propertyExpr, String valueExpr, String isSetExpr) {
		switch (_descriptor.kind()) {
			case MAP:
			case ARRAY:
			case LIST:
			case COMPLEX:
			case ITEM:
			case PLAIN:
			case REF:
			{
				append(_internalUpdateMethod);
				append("(");
				append(propertyExpr);
				append(", ");
				append(valueExpr);
				append(", ");
				append(isSetExpr);
				append(");");
				flushLine();
				break;
			}
			case DERIVED: {
				if (_descriptor.hasContainerAnnotation()) {
					comment("No initialization for container property '" + _descriptor.getPropertyName() + "'.");
				} else {
					append("__initDerived((");
					appendType(PropertyDescriptorImpl.class);
					append(")");
					append(propertyExpr);
					append(");");
					flushLine();
				}
				break;
			}
		}
	}

	private void writeSetter() {
		if (_setter == null) {
			return;
		}
		line("@Override");
		append("public void ");
		_generator.appendMethodWithArgs(_setter, SETTER_PARAMS);
		append(" {");
		flushLine();
		String valueExpr = VALUE_PARAM;

		if (_descriptor.isIndexed()) {
			valueExpr = castedValue(_setter.getGenericParameterTypes()[1], valueExpr);

			line("__setIndexed(" + _varValue + ", " + INDEX_PARAM + ", " + getNullReturnValue() + ", " + valueExpr
				+ ");");
		} else {
			valueExpr = castedValue(_setter.getGenericParameterTypes()[0], valueExpr);

			append(_internalSetMethod);
			append("(");
			append(valueExpr);
			append(", true");
			append(")");
			append(';');
			flushLine();
		}
		line("}");
		nl();
	}

	private String castedValue(Type valueType, String valueExpr) {
		if (!_type.equals(valueType)) {
			valueExpr = "(" + _generator.toString(_type) + ")" + valueExpr;
		}
		return valueExpr;
	}

	private void appendGetterCall() {
		append(_getterName);
		append("()");
	}

	private void appendInternalGetterCall() {
		if (_descriptor.hasContainerAnnotation()) {
			append('(');
			append('(');
			appendType(_type);
			append(')');
			append("container()");
			append(')');
		} else {
			append(_internalGetMethod);
			append("()");
		}
	}

	private void writeInternalGetter() {
		if (!initialDeclaration()) {
			return;
		}
		append("protected final ");
		appendPropertyType(false);
		append(" ");
		append(_internalGetMethod);
		append("() {");
		flushLine();
		switch (_descriptor.kind()) {
			case ARRAY: {
				append("return ");
				append("(");
				appendPropertyType(false);
				append(")");
				appendType(PropertyDescriptorImpl.class);
				append(".copyArray(");
				append(_propertyAccessMethod);
				append("()");
				append(",");
				append(_varValue);
				append(");");
				flushLine();
				break;
			}
			default: {
				line("return " + _varValue + ";");
				break;
			}
		}
		line("}");
		nl();
	}

	private void writeInternalValueSet() {
		if (!initialDeclaration()) {
			return;
		}
		append("protected final boolean ");
		append(_internalValueSetMethod);
		append("() {");
		flushLine();

		switch (kind()) {
			case LIST:
			case MAP:
				line("return " + _varValue + ".isModified();");
				break;
			case ARRAY:
				// Arrays have a flag variable.
			default:
				line("return " + _varIsSet + ";");
				break;
		}

		line("}");
		nl();
	}

	private void writeInternalUpdate() {
		if (!initialDeclaration()) {
			return;
		}
		append("protected final ");
		appendPropertyType(false);
		append(" ");
		append(_internalUpdateMethod);
		append("(");
		appendType(PropertyDescriptor.class);
		append(" property, ");
		append("Object newValue, boolean isSet");
		append(") {");
		flushLine();
		{
			if (primitive()) {
				line("__checkPrimitive(property, newValue);");
			}
			append("return ");
			append(_internalSetMethod);
			append("(");
			append("(");
			appendPropertyType(false);
			append(") ");
			append("newValue");
			append(", isSet");
			append(")");
			append(";");
			flushLine();
		}
		line("}");
		nl();
	}

	private void writeInternalSet() {
		if (!initialDeclaration()) {
			return;
		}
		append("protected final ");
		appendPropertyType(false);
		append(" ");
		append(_internalSetMethod);
		append("(");
		appendPropertyType(false);
		append(" newValue, boolean isSet) {");
		flushLine();

		switch (kind()) {
			case LIST: {
				appendPropertyType(false);
				append(" oldValue = ");
				append("new ");
				appendType(ArrayList.class);
				append("(");
				append(_varValue);
				append(")");
				append(";");
				flushLine();
				break;
			}
			case MAP: {
				appendPropertyType(false);
				append(" oldValue = ");
				append("new ");
				appendType(HashMap.class);
				append("(");
				append(_varValue);
				append(")");
				append(";");
				flushLine();
				break;
			}
			case ARRAY:
				// Handle as plain value.
			default: {
				appendPropertyType(false);
				append(" oldValue = ");
				append(_varValue);
				append(";");
				flushLine();
				break;
			}
		}

		appendType(PropertyDescriptor.class);
		append(" property = ");
		append(_propertyAccessMethod);
		append("();");
		flushLine();

		line("__checkSet(property, isSet);");

		line("newValue = " + _normalizeMethod + "(property, newValue);");

		switch (kind()) {
			case LIST:
			case MAP: {
				line(_varValue + ".set(newValue, isSet);");
				break;
			}
			case ARRAY: {
				append(_varValue);
				append(" = ");
				append("(");
				appendPropertyType(false);
				append(")");
				appendType(PropertyDescriptorImpl.class);
				append(".copyArray(property,newValue);");
				flushLine();
				line(_varIsSet + " = isSet;");
				break;
			}
			default:
				line(_varValue + " = newValue;");
				line(_varIsSet + " = isSet;");
		}

		line("__notifyUpdate(property, oldValue, newValue);");

		line("return oldValue;");

		line("}");
		nl();
	}

	private void writeGetter() {
		switch (_inheritance) {
			case REDECLARED:
				if (_type.equals(_inheritedDescriptor.getType())
					&& (_descriptor.hasContainerAnnotation() == _inheritedDescriptor.hasContainerAnnotation())) {
					break;
				}
				//$FALL-THROUGH$
			case DECLARED: {
				line("@Override");
				append("public ");
				appendType(_type);
				append(" ");
				_generator.appendMethodWithArgs(_getter, GETTER_PARAMS);
				append(" {");
				flushLine();

				if (_descriptor.isIndexed()) {
					append("return ");
					appendCastIfRedeclared();
					appendInternalGetterCall();
					append(".get(");
					append(INDEX_PARAM);
					append(");");
					flushLine();
				} else {
					append("return ");
					appendCastIfRedeclared();
					appendInternalGetterCall();
					append(";");
					flushLine();
				}
				line("}");
				nl();
				break;
			}
			case INHERITED: {
				break;
			}
		}
	}

	private void appendCastIfRedeclared() {
		if (_inheritance == PropertyInheritance.REDECLARED && !_type.equals(_inheritedDescriptor.getType())) {
			appendCast();
		}
	}

	private void appendCast() {
		append("(");
		appendType(_type);
		append(")");
	}

	private void writeIndexedGetters() {
		writeIndexedGetters(_descriptor);
	}

	private void writeIndexedGetters(PropertyDescriptorSPI descriptor) {
		for (Method method : descriptor.getIndexedGetters()) {
			writeIndexedGetter(method);
		}
		for (PropertyDescriptorSPI superDescriptor : descriptor.getSuperProperties()) {
			writeIndexedGetters(superDescriptor);
		}
	}

	private void writeIndexedGetter(Method method) {
		Type genericReturnType = method.getGenericReturnType();
		String returnType = _generator.toString(genericReturnType);

		line("@Override");

		append("public ");
		_generator.appendTypeParameters(method.getTypeParameters(), true);
		append(returnType);
		append(" ");
		_generator.appendMethodWithArgs(method, GETTER_PARAMS);
		append(" {");
		flushLine();

		if (_descriptor.kind() == PropertyKind.MAP) {
			append("return (");
			appendType(_generator.wrappedPrimitive(genericReturnType));
			append(") ");
			appendInternalGetterCall();
			append(".get(");
			append(INDEX_PARAM);
			append(");");
			flushLine();
		} else {
			PropertyDescriptorSPI keyProperty =
				PropertyDescriptorSPI.cast(_descriptor.getKeyProperty());
			String elementConfigType =
				_generator.toString(_descriptor.getValueDescriptor().getConfigurationInterface());
			String keyType = _generator.toString(keyProperty.getType());

			append("for (Object entry : ");
			appendInternalGetterCall();
			append(") {");
			flushLine();
			{
				String entryTyped = "entryTyped";
				line(returnType + " " + entryTyped + " = (" + returnType + ")entry;");

				String element = "element";
				if (_descriptor.isInstanceValued()) {
					line(elementConfigType + " " + element + " = " + entryTyped + ".getConfig();");
				} else {
					element = entryTyped;
				}

				line(keyType + " key = " + element + "." + keyProperty.getSomeGetter().getName() + "();");
				if (keyProperty.getType().isPrimitive()) {
					line("if (key == " + INDEX_PARAM + ") {");
				} else {
					line(
						"if ((key != null && key.equals(" + INDEX_PARAM + ")) || (key == null && " + INDEX_PARAM
							+ " == null)) {");
				}
				{
					line("return " + entryTyped + ";");
				}
				line("}");
			}
			line("}");

			line("return " + getNullReturnValue(genericReturnType) + ";");
		}
		line("}");
		nl();
	}

	private PropertyKind kind() {
		return _descriptor.kind();
	}

	void writeIndexConstant() {
		if (!initialDeclaration()) {
			return;
		}
		line("protected static final int " + _indexConstant + " = " + _index + ";");
		nl();
	}

	void writePropertyConstant() {
		if (isSimplyInherited()) {
			return;
		}
		append("protected static final ");
		appendType(PropertyDescriptor.class);
		append(" ");
		append(_propertyConstant);
		append(";");
		flushLine();
		nl();
	}

	void writePropertyLookup(String descriptorVar) {
		if (isSimplyInherited()) {
			return;
		}
		line(
			_propertyConstant + " = " + descriptorVar + ".getProperty(\"" + _descriptor.getPropertyName() + "\");");
	}

	private boolean isSimplyInherited() {
		// Note: A specialized property constant is required for each property, since each property
		// of every concrete interface may have a specialized configured default. Applying this
		// default is only possible, if a specialized property constant exits.
		return false && _inheritance == PropertyInheritance.INHERITED && !isImplementationClassProperty();
	}

	private boolean isImplementationClassProperty() {
		// Requires special case, since this property may change its default without a local getter
		// declaration.
		return PolymorphicConfiguration.class.isAssignableFrom(_descriptor.getDescriptor().getConfigurationInterface())
			&& _descriptor.getPropertyName().equals(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
	}

	void writePropertyIndexing(String indexVar) {
		if (!initialDeclaration()) {
			return;
		}
		line("__index(" + indexVar + ", " + _propertyConstant + ", " + _indexConstant + ");");
	}

	public void writeVariables() {
		if (!initialDeclaration()) {
			return;
		}
		writeValueVariable();
		writeVariableSet();
	}

	boolean initialDeclaration() {
		return _inheritance == PropertyInheritance.DECLARED;
	}

	private void writeVariableSet() {
		switch (_descriptor.kind()) {
			case LIST:
			case MAP:
				// handled by collection implementation
				break;
			case DERIVED:
				// Fall-through, since property might be overridden as not-derived.
			case ARRAY:
				// Can only be set as a whole, uses flag for determining whether a value is set.
			case COMPLEX:
			case ITEM:
			case PLAIN:
			case REF:
				javadocStart();
				commentLine(
					"Whether value of property " + _descriptor.getPropertyName() + " should be treated as set.");
				javadocStop();
				line(VARIABLE_VISIBILITY + " boolean " + _varIsSet + ";");
				nl();
				break;
			default:
				throw PropertyKind.noSuchPropertyKind(_descriptor.kind());
		}
	}

	private void writeValueVariable() {
		// Note: Arrays are handled as plain values.
		boolean isCollection = kind() == PropertyKind.LIST || kind() == PropertyKind.MAP;

		javadocStart();
		commentLine("@see #" + _getterName + "()");
		javadocStop();
		append(VARIABLE_VISIBILITY);
		append(" ");
		if (isCollection) {
			append("final ");
		}
		appendVariableDeclarationType(false);
		append(" ");
		append(_varValue);
		if (isCollection) {
			append(" = ");
			append("new ");
			appendVariableDeclarationType(false);
			append("(this, ");
			append(_propertyAccessMethod);
			append("()");
			append(")");
		}
		append(";");
		flushLine();
		nl();
	}

	public void writeValueCase() {
		caseBegin();

		append("return ");
		if (_descriptor.isIndexed()) {
			// An indexed property has no typed getter for the whole list.
			appendInternalGetterCall();
		} else {
			// Call the public getter, since this may be overridden with a container property
			// implementation.
			appendGetterCall();
		}
		append(";");
		flushLine();

		caseEnd();
	}

	private void caseEnd() {
		line("}");
	}

	private void caseBegin() {
		line("case " + _indexConstant + ": {");
	}

	public void writeUpdateCase(String propertyExpr, String newValueExpr, String isSetExpr) {
		caseBegin();

		append("return ");
		append(_internalUpdateMethod);
		append("(");
		append(propertyExpr);
		append(", ");
		append(newValueExpr);
		append(", ");
		append(isSetExpr);
		append(");");
		flushLine();

		caseEnd();
	}

	public void writeResetCase() {
		caseBegin();
		writeInitCall();
		line("return;");
		caseEnd();
	}

	private void writeInitCall() {
		line(_initMethod + "();");
	}

	public void writeValueSetCase() {
		caseBegin();
		line("return " + _internalValueSetMethod + "();");
		caseEnd();
	}

	private String getNullReturnValue() {
		return getNullReturnValue(_type);
	}

	private String getNullReturnValue(Type returnType) {
		if (returnType instanceof Class && ((Class<?>) returnType).isPrimitive()) {
			return PropertyDescriptorImpl.getPrimitiveDefault((Class<?>) returnType).toString();
		}
		return "null";
	}

	private void appendVariableDeclarationType(boolean nonPrimitive) {
		switch (kind()) {
			case MAP: {
				appendType(ItemMap.class);
				break;
			}
			case LIST: {
				appendType(ItemList.class);
				break;
			}
			case ARRAY:
				// Use declared type.
			default:
				appendPropertyType(nonPrimitive);
				break;
		}
	}

	private void appendPropertyType(boolean nonPrimitive) {
		if (_descriptor.isIndexed()) {
			appendType(List.class);
			append('<');
			appendType(_generator.wrappedPrimitive(_type));
			append('>');
		} else {
			appendType(_generator.wrappedPrimitive(_type, nonPrimitive));
		}
	}

	private void line(String line) {
		_generator.line(line);
	}

	private void comment(String line) {
		_generator.comment(line);
	}

	private void commentLine(String comment) {
		_generator.commentLine(comment);
	}

	private void nl() {
		_generator.nl();
	}

	private void append(char value) {
		_generator.append(value);
	}

	private void append(CharSequence csq) {
		_generator.append(csq);
	}

	private void appendType(Type type) {
		_generator.appendType(type);
	}

	private void flushLine() {
		_generator.flushLine();
	}

	private void javadocStart() {
		_generator.javadocStart();
	}

	private void javadocStop() {
		_generator.javadocStop();
	}

}
