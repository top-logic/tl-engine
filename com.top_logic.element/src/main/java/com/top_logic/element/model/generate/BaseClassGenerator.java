/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypeVisitor;
import com.top_logic.model.config.JavaClass;
import com.top_logic.model.util.TLModelNamingConvention;

/**
 * Base class for {@link TLTypeGenerator} that generate classes containing the getter and setter.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class BaseClassGenerator extends TLTypeGenerator {

	private static final Pattern NO_IMPORT_TYPES = Pattern.compile("\\bjava\\.lang\\.");

	static final BidiMap<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;
	static {
		BidiMap<Class<?>, Class<?>> tmp = new BidiHashMap<>();
		tmp.put(Boolean.TYPE, java.lang.Boolean.class);
		tmp.put(Byte.TYPE, java.lang.Byte.class);
		tmp.put(Short.TYPE, java.lang.Short.class);
		tmp.put(Integer.TYPE, java.lang.Integer.class);
		tmp.put(Long.TYPE, java.lang.Long.class);
		tmp.put(Float.TYPE, java.lang.Float.class);
		tmp.put(Double.TYPE, java.lang.Double.class);
		tmp.put(Character.TYPE, java.lang.Character.class);
		PRIMITIVE_TO_WRAPPER = tmp;
	}

	private static final Map<String, Class<?>> PRIMITIVE_CLASS_NAME_TO_CLASS = new MapBuilder<String, Class<?>>()
		.put(Boolean.TYPE.getName(), Boolean.TYPE)
		.put(Byte.TYPE.getName(), Byte.TYPE)
		.put(Short.TYPE.getName(), Short.TYPE)
		.put(Integer.TYPE.getName(), Integer.TYPE)
		.put(Long.TYPE.getName(), Long.TYPE)
		.put(Float.TYPE.getName(), Float.TYPE)
		.put(Double.TYPE.getName(), Double.TYPE)
		.put(Character.TYPE.getName(), Character.TYPE)
		.toMap();

	/**
	 * Visitor determining the implementation class of the content type of a
	 * {@link TLStructuredType}.
	 */
	private TLTypeVisitor<String, TLStructuredTypePart> _contentTypeVisitor =
		new TLTypeVisitor<>() {

			@Override
			public String visitEnumeration(TLEnumeration model, TLStructuredTypePart arg) {
				return TLClassifier.class.getName();
			}

			@Override
			public String visitClass(TLClass model, TLStructuredTypePart arg) {
				String interfaceName = TLModelNamingConvention.interfaceName(model);
				if (interfaceName != null) {
					return interfaceName;
				}
				String implementationClass = TLModelNamingConvention.implementationName(model);
				if (implementationClass != null) {
					return implementationClass;
				}
				Logger.info("No interface or implementation found for class " + model + " as type of part "
					+ arg + ". Use TLObject instead.", TLTypeGenerator.class);
				return TLObject.class.getName();
			}

			@Override
			public String visitPrimitive(TLPrimitive model, TLStructuredTypePart arg) {
				boolean isMandatory = arg.isMandatory();
				JavaClass annotation = model.getAnnotation(JavaClass.class);
				if (annotation != null) {
					if (!annotation.getInterfaceName().isEmpty()) {
						return annotation.getInterfaceName();
					}
					return annotation.getClassName();
				}
				Class<?> applicationType = model.getStorageMapping().getApplicationType();
				if (isMandatory) {
					applicationType = toPrimitiveType(applicationType);
				}
				if (applicationType == Object.class) {
					applicationType = applicationTypeFromKind(isMandatory, model.getKind());
				}
				return applicationType.getCanonicalName();
			}

			private Class<?> applicationTypeFromKind(boolean isMandatory, Kind kind) {
				switch (kind) {
					case BINARY:
						return BinaryData.class;
					case BOOLEAN:
						if (isMandatory) {
							return boolean.class;
						} else {
							return Boolean.class;
						}
					case CUSTOM:
						return Object.class;
					case DATE:
						return Date.class;
					case FLOAT:
						if (isMandatory) {
							return float.class;
						} else {
							return Float.class;
						}
					case INT:
						if (isMandatory) {
							return int.class;
						} else {
							return Integer.class;
						}
					case STRING:
						return String.class;
					case TRISTATE:
						if (isMandatory) {
							return boolean.class;
						} else {
							return Boolean.class;
						}
					default:
						throw new UnreachableAssertion("Unexpected kind " + kind);
				}
			}

			private Class<?> toPrimitiveType(Class<?> applicationType) {
				Class<?> primitive = PRIMITIVE_TO_WRAPPER.getKey(applicationType);
				return primitive != null ? primitive : applicationType;
			}

			@Override
			public String visitAssociation(TLAssociation model, TLStructuredTypePart arg) {
				throw new UnreachableAssertion(arg + " does not have an association as type " + model);
			}

		};

	private String _getterPrefix = "get";

	private String _setterPrefix = "set";

	private boolean _noGetter;

	private boolean _noSetter;

	/**
	 * Creates a new {@link BaseClassGenerator}.
	 */
	public BaseClassGenerator(String packageName, TLType type) {
		super(packageName, type);
	}

	/**
	 * Returns a {@link String} used to cast the storage {@link Object} to correct method return
	 * type.
	 */
	protected String getJavaTypeCast(TLStructuredTypePart part) {
		return toObjectClass(getJavaReturnType(part));
	}

	/**
	 * Convenience variant of {@link #getJavaReturnType(TLStructuredTypePart)} that includes "?
	 * extends" in the type.
	 */
	protected String getJavaReturnType(TLStructuredTypePart part) {
		return getJavaReturnType(part, true);
	}

	/**
	 * Returns a {@link String} used as return type for a getter for the given part.
	 * 
	 * @param withExtends
	 *        Whether "? extends" should be added to the type parameter.
	 */
	protected String getJavaReturnType(TLStructuredTypePart part, boolean withExtends) {
		String contentType = getContentType(part);
		String extendsModifier = getExtendsClause(part.getType(), withExtends);
		if (part.isMultiple()) {
			// Wrap primitive class because Collection<boolean> is invalid.
			contentType = toObjectClass(contentType);
			String typeParameter = "<" + extendsModifier + contentType + ">";
			if (part.isOrdered()) {
				return List.class.getName() + typeParameter;
			} else if (!part.isBag()) {
				return Set.class.getName() + typeParameter;
			} else {
				return Collection.class.getName() + typeParameter;
			}
		} else {
			return contentType;
		}
	}

	/**
	 * The Java type of the given part (the content type for a multiple reference type).
	 */
	protected String getContentType(TLStructuredTypePart part) {
		String contentType = part.getType().visitType(_contentTypeVisitor, part);
		contentType = NO_IMPORT_TYPES.matcher(contentType).replaceAll("");
		contentType = dropPackage(contentType);
		return contentType;
	}

	private String getExtendsClause(TLType targetType, boolean withExtends) {
		if (withExtends && canHaveSubtypes(targetType)) {
			return "? extends ";
		}
		return "";
	}

	private boolean canHaveSubtypes(TLType type) {
		if (!(type instanceof TLClass)) {
			return false;
		}
		TLClass tlClass = (TLClass) type;
		return !tlClass.isFinal();
	}

	/**
	 * Ensures that the given class name represents a java {@link Object} class, i.e. primitive
	 * classes are replaced by their wrapper class.
	 */
	protected String toObjectClass(String classAsString) {
		Class<?> primitiveClass = PRIMITIVE_CLASS_NAME_TO_CLASS.get(classAsString);
		if (primitiveClass != null) {
			Class<?> wrapperType = PRIMITIVE_TO_WRAPPER.get(primitiveClass);
			// Use simple name because all have package java.lang which can be removed.
			classAsString = wrapperType.getSimpleName();
		}
		return classAsString;
	}

	boolean shouldGenerateModificationGetter(TLStructuredTypePart part) {
		return isCollection(part) && isSupportedByLiveCollections(part) && (!isReadOnly(part)) && (!part.isOverride());
	}

	private boolean isCollection(TLStructuredTypePart part) {
		return part.isMultiple();
	}

	private boolean isSupportedByLiveCollections(TLStructuredTypePart part) {
		return AttributeOperations.getStorageImplementation(part).supportsLiveCollections();
	}

	/**
	 * The name of the modification-getter.
	 */
	protected String getModificationGetter(String accessorSuffix) {
		return getterPrefix() + accessorSuffix + "Modifiable";
	}

	/**
	 * Returns the prefix for getter methods.
	 */
	public String getterPrefix() {
		return _getterPrefix;
	}

	/**
	 * Setter for {@link #getterPrefix()}.
	 */
	public void setGetterPrefix(String getterPrefix) {
		_getterPrefix = getterPrefix;

	}

	/**
	 * Returns the prefix for setter methods.
	 */
	public String setterPrefix() {
		return _setterPrefix;
	}

	/**
	 * Setter for {@link #setterPrefix()}.
	 */
	public void setSetterPrefix(String setterPrefix) {
		_setterPrefix = setterPrefix;

	}

	/**
	 * Whether no getter must be generated.
	 */
	public boolean noGetter() {
		return _noGetter;
	}

	/**
	 * Setter for {@link #noGetter()}.
	 */
	public void setNoGetter(boolean noGetter) {
		_noGetter = noGetter;
		
	}

	/**
	 * Whether no setter must be generated.
	 */
	public boolean noSetter() {
		return _noSetter;
	}

	/**
	 * Setter for {@link #noSetter()}.
	 */
	public void setNoSetter(boolean noSetter) {
		_noSetter = noSetter;
		
	}

}
