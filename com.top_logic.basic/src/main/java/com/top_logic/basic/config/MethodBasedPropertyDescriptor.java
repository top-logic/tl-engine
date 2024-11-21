/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Reference;
import com.top_logic.basic.config.annotation.Root;
import com.top_logic.basic.config.internal.gen.PropertyDescriptorSPI;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.reflect.JavaTypeUtil;

/**
 * {@link PropertyDescriptorImpl} based on a {@link Method getter} and {@link Method setter}. All
 * main properties are derived from the getter.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class MethodBasedPropertyDescriptor extends PropertyDescriptorImpl implements PropertyDescriptorSPI {

	private boolean reference;

	private Method setter;

	private Method getter;

	private Class<?> keyType;

	private Class<?> minType;

	private Class<?> maxType;

	private Type _genericType;

	private List<Method> _indexedGetters = Collections.emptyList();

	private boolean indexed;

	/**
	 * Creates a new {@link MethodBasedPropertyDescriptor}.
	 */
	public MethodBasedPropertyDescriptor(Protocol protocol, AbstractConfigurationDescriptor descriptor,
			String propertyName, MethodBasedPropertyDescriptor[] superProperties) {
		super(protocol, descriptor, propertyName, superProperties);
	}

	@Override
	boolean hasSetter() {
		return setter != null;
	}

	@Override
	public Method getSetter() {
		return setter;
	}

	/* package protected */ void setSetter(Method method) {
		this.setter = method;
	}

	/**
	 * @see com.top_logic.basic.config.AbstractPropertyDescriptor#getDescriptor()
	 */
	@Override
	public ConfigurationDescriptorImpl getDescriptor() {
		return (ConfigurationDescriptorImpl) super.getDescriptor();
	}

	/**
	 * Cast the given {@link PropertyDescriptor} to a {@link MethodBasedPropertyDescriptor}.
	 */
	public static MethodBasedPropertyDescriptor cast(PropertyDescriptor property) {
		return MethodBasedPropertyDescriptor.class.cast(property);
	}

	@Override
	void resolveSetter(Protocol protocol) {
		Class<?>[] parameterTypes = setter.getParameterTypes();
		int parameterCount = parameterTypes.length;

		if (isIndexed()) {
			if (parameterCount != 2) {
				error(protocol, "Setter of indexed property must have exactly two arguments (int index, T value).");
			} else {
				if (parameterTypes[0] != int.class) {
					error(protocol, "First argument of setter of indexed property must have type 'int'.");
				} else {
					ensureSetterType(protocol, parameterTypes[1]);
					getDescriptor().addImplementation(setter, IndexedSetter.INSTANCE);
				}
			}
		} else {
			if (parameterCount != 1) {
				error(protocol, "Setter of simple property must have exactly one argument (T value).");
			} else {
				ensureSetterType(protocol, parameterTypes[0]);
				getDescriptor().addImplementation(setter, SimpleSetter.INSTANCE);
			}
		}

	}

	@Override
	public MethodBasedPropertyDescriptor[] getSuperProperties() {
		return (MethodBasedPropertyDescriptor[]) super.getSuperProperties();
	}

	@Override
	protected MethodBasedPropertyDescriptor firstSuperProperty() {
		return getSuperProperties()[0];
	}

	@Override
	protected Class<?> initInheritedElementType(PropertyDescriptorImpl inheritedProperty) {
		if (getter == null) {
			Method inheritedGetter = PropertyDescriptorSPI.cast(inheritedProperty).getSomeGetter();
			Type genericReturnType = inheritedGetter.getGenericReturnType();
			Class<?> rawReturnType = inheritedGetter.getReturnType();

			final boolean multiRef = isMultiRefType(rawReturnType);
			if (multiRef || isIndexedRefType(rawReturnType)) {
				Class<?> superConfigInterface = inheritedGetter.getDeclaringClass();
				TypeVariable<?>[] typeParameters = superConfigInterface.getTypeParameters();

				if (genericReturnType instanceof ParameterizedType) {
					ParameterizedType parametrizedReturnType = (ParameterizedType) genericReturnType;

					Type genericContentType;
					if (multiRef) {
						genericContentType = parametrizedReturnType.getActualTypeArguments()[0];
						if (genericContentType instanceof TypeVariable) {
							return getTypeBound(superConfigInterface, typeParameters, genericContentType);
						}
					} else {
						Type keyContentType = parametrizedReturnType.getActualTypeArguments()[0];
						if (keyContentType instanceof TypeVariable) {
							setKeyType(getTypeBound(superConfigInterface, typeParameters, keyContentType));
						} else {
							setKeyType(Object.class);
						}
						Type valueContentType = parametrizedReturnType.getActualTypeArguments()[1];
						if (valueContentType instanceof TypeVariable) {
							return getTypeBound(superConfigInterface, typeParameters, valueContentType);
						}
					}
				} else if (rawReturnType.isArray()) {
					if (genericReturnType instanceof GenericArrayType) {
						Type componentType = ((GenericArrayType) genericReturnType).getGenericComponentType();
						if (componentType instanceof TypeVariable) {
							return getTypeBound(superConfigInterface, typeParameters, componentType);
						}
					} else {
						return rawReturnType.getComponentType();
					}
				}
			} else {
				return rawReturnType;
			}
		}

		return super.initInheritedElementType(inheritedProperty);
	}

	private Class<?> getTypeBound(Class<?> superConfigInterface, TypeVariable<?>[] typeParameters, Type contentType) {
		int typeIndex = Arrays.asList(typeParameters).indexOf(contentType);
		if (typeIndex != -1) {
			Class<?> configurationInterface = getDescriptor().getConfigurationInterface();

			return JavaTypeUtil.getTypeBound(configurationInterface, superConfigInterface, typeIndex);
		} else {
			return null;
		}
	}

	@Override
	public boolean canHaveSetter() {
		boolean superCanHaveSetter = super.canHaveSetter();
		if (superCanHaveSetter) {
			if (isAnnotationProperty() && isAnnotationTypeProperty()) {
				return false;
			}
		}
		return superCanHaveSetter;
	}

	@Override
	protected void acceptAnnotationMandatory() {
		if (hasLocalAnnotation(Mandatory.class) || isMandatoryAnnotationProperty()) {
			setHasLocalMandatoryAnnotation(true);
		}
	}

	private boolean isMandatoryAnnotationProperty() {
		return isAnnotationProperty() && !getter.getReturnType().isArray() && !isAnnotationTypeProperty()
			&& getter.getDefaultValue() == null;
	}

	private boolean isAnnotationTypeProperty() {
		return ConfigurationItem.ANNOTATION_TYPE.equals(getPropertyName());
	}

	private boolean isAnnotationProperty() {
		return getter != null && Annotation.class.isAssignableFrom(getter.getDeclaringClass());
	}

	@Override
	String getFallbackElementName() {
		if (isAnnotationProperty()) {
			if (getter.getReturnType().isArray()) {
				return CodeUtil.toXMLTag(getter.getReturnType().getComponentType().getSimpleName());
			} else {
				return CodeUtil.toXMLTag(getter.getReturnType().getSimpleName());
			}
		}
		return super.getFallbackElementName();
	}

	/* package protected */ void setGetter(Method method) {
		this.getter = method;
	}

	@Override
	public Method getLocalGetter() {
		return getter;
	}

	@Override
	public Method getSomeGetter() {
		if (getter != null) {
			return getter;
		}
		if (isLocalProperty()) {
			return null;
		}
		return firstSuperProperty().getSomeGetter();
	}

	@Override
	public Annotation[] getLocalAnnotations() {
		Method localGetter = getLocalGetter();
		if (localGetter == null) {
			return ArrayUtil.EMPTY_ANNOTATION_ARRAY;
		}
		return localGetter.getAnnotations();
	}

	@Override
	public <T extends Annotation> T getLocalAnnotation(Class<T> annotationClass) {
		Method localGetter = getLocalGetter();
		if (localGetter == null) {
			return null;
		}
		return localGetter.getAnnotation(annotationClass);
	}

	@Override
	protected void setAnnotatedDefaults(MutableInteger numberDefaultAnnotations) {
		Method localGetter = getLocalGetter();
		if (localGetter != null) {
			Object annotationDefault = localGetter.getDefaultValue();
			if (annotationDefault != null) {
				numberDefaultAnnotations.inc();
				setAnnotatedDefaultSpec(new LiteralDefault(annotationDefault));
			}
		}

		super.setAnnotatedDefaults(numberDefaultAnnotations);
	}

	@Override
	protected void appendName(StringBuilder result) {
		Method someGetter = getSomeGetter();
		if (someGetter == null) {
			// May happen in invalid configuration items.
			result.append(getPropertyName());
		} else {
			String getterName = someGetter.getName();
			result.append(getterName);
			result.append("(");
			boolean first = true;
			for (Class<?> paramType : someGetter.getParameterTypes()) {
				if (first) {
					first = false;
				} else {
					result.append(",");
				}
				result.append(paramType.getName());
			}
			result.append(")");
		}
	}

	@Override
	protected void acceptFromSuperPropertiesAbstract() {
		boolean isInheritingAbstract = (getLocalGetter() == null) && areAllSuperAbstract();
		setHasAbstractAnnotation(isInheritingAbstract);
	}

	@Override
	void resolveFromDeclaredReturnType(Protocol protocol) {
		if (getter != null) {
			Class<?>[] parameterTypes = getter.getParameterTypes();
			int parameterCount = parameterTypes.length;
			if (parameterCount == 0) {
				if (hasContainerAnnotation()) {
					getDescriptor().addImplementation(getter, ContainerGetter.INSTANCE);
				} else {
					getDescriptor().addImplementation(getter, SimpleGetter.INSTANCE);
				}
			} else if (parameterCount == 1) {
				// Check indexed property.
				if (parameterTypes[0] != int.class) {
					error(protocol, "Has getter with illegal argument type '" + parameterTypes[0]
						+ "' (expecting int for an indexed getter).");
				} else {
					setIndexed(true);

					getDescriptor().addImplementation(getter, IndexedGetter.INSTANCE);
				}
			} else if (parameterCount > 1) {
				error(protocol,
					"Has getter with multiple parameters, expecting at least one for an indexed getter.");
			}

			// Check return type.
			Class<?> rawReturnType = getter.getReturnType();
			if (rawReturnType == Void.class) {
				error(protocol, "Has void getter.");
			} else {
				Type genericReturnType = getter.getGenericReturnType();
				ensureGetterType(protocol, rawReturnType, genericReturnType);

				// Use this for the generic case.
				final boolean multiRef = isMultiRefType(rawReturnType);
				if (multiRef || isIndexedRefType(rawReturnType)) {
					if (genericReturnType instanceof ParameterizedType) {
						ParameterizedType parametrizedReturnType = (ParameterizedType) genericReturnType;

						Type keyTypeParameter;
						Type genericContentType;
						if (multiRef) {
							genericContentType = parametrizedReturnType.getActualTypeArguments()[0];
							keyTypeParameter = null;
						} else {
							// Indexed ref (see above).
							keyTypeParameter = parametrizedReturnType.getActualTypeArguments()[0];
							genericContentType = parametrizedReturnType.getActualTypeArguments()[1];
						}

						Class<?> contentType = ConfigurationDescriptorImpl.rawType(genericContentType);
						if (contentType != null) {
							initInstanceType(contentType, genericContentType);
						}
						if (keyTypeParameter != null) {
							Class<?> newKeyType = ConfigurationDescriptorImpl.rawType(keyTypeParameter);
							if (newKeyType != null) {
								setKeyType(newKeyType);
							}
						}
					} else if (rawReturnType.isArray()) {
						Class<?> componentType = rawReturnType.getComponentType();
						initInstanceType(componentType, componentType);
					}
				} else {
					initInstanceType(rawReturnType, genericReturnType);
				}
			}
		}
	}

	@Override
	void resolveLocalProperties(Protocol protocol) {
		if (isLocalProperty()) {
			if (getter != null) {
				setReference(hasLocalAnnotation(Root.class) || hasLocalAnnotation(Reference.class));
			} else {
				errorNoGetter(protocol);
			}
		} else {
			setReference(firstSuperProperty().isReference());
		}
		super.resolveLocalProperties(protocol);
	}

	private void errorNoGetter(Protocol protocol) {
		error(protocol, "A configuration property must have a getter method.");
	}

	@Override
	protected void initKind(Protocol protocol) {
		if (isReference()) {
			setKindRef(protocol, getDescriptor().getConfigurationInterface());
		} else
			super.initKind(protocol);
	}

	private void setKindRef(Protocol protocol, Class<?> configurationInterface) {
		Class<?> propertyType = getType();
		setMultiple(protocol, configurationInterface, isMultiRefType(propertyType) || isIndexedRefType(propertyType));
		setOrdered(isOrderedRefType(propertyType));
		setKind(PropertyKind.REF);
	}

	private boolean isReference() {
		return reference;
	}

	private void setReference(boolean value) {
		this.reference = value;
	}

	@Override
	protected void init(Protocol protocol) {
		super.init(protocol);

		Boolean inheritedIndexed = null;
		MethodBasedPropertyDescriptor firstSuperProperty = firstSuperProperty();
		for (int n = 0, cnt = getSuperProperties().length; n < cnt; n++) {
			MethodBasedPropertyDescriptor inheritedProperty = getSuperProperties()[n];
			if (n > 0) {
				if (inheritedProperty.isReference() != firstSuperProperty.isReference()) {
					error(protocol,
						"Property reference attribute mismatch of inherited property in '"
							+ firstSuperProperty.getDescriptor().getConfigurationInterface() + "' and '"
							+ inheritedProperty.getDescriptor().getConfigurationInterface() + "': '"
							+ firstSuperProperty.isReference() + "' vs. '" + inheritedProperty.isReference() + "'.");
				}
			}

			if (inheritedIndexed == null) {
				inheritedIndexed = Boolean.valueOf(inheritedProperty.isIndexed());
			} else {
				if (inheritedIndexed.booleanValue() != inheritedProperty.isIndexed()) {
					error(protocol, "Mismatch of indexed attribute of inherited property '" + inheritedProperty + "'.");
				}
			}

			ensureGetterType(protocol, inheritedProperty.minType, inheritedProperty.getGenericType());

			if (inheritedProperty.maxType != null) {
				ensureSetterType(protocol, inheritedProperty.maxType);
			}
		}

		setIndexed(inheritedIndexed);

	}

	@Override
	public Class<?> getElementType() {
		if (isIndexed()) {
			return minType;
		} else {
			return super.getElementType();
		}
	}

	@Override
	public Class<?> getType() {
		if (isIndexed()) {
			return List.class;
		} else {
			return minType;
		}
	}

	private void ensureGetterType(Protocol protocol, Class<?> getterType, Type genericType) {
		_genericType = genericType;

		if (minType == null) {
			minType = getterType;
		} else {
			Class<?> propertyMinType = getterType;
			if (minType.isAssignableFrom(propertyMinType)) {
				// The inherited property requires a stronger type guarantee.
				minType = propertyMinType;
			} else if (propertyMinType.isAssignableFrom(minType)) {
				// Current guarantee fits property requirements.
			} else {
				// Incompatible types.
				error(protocol, "Mismatch of inherited getter types: '" + propertyMinType.getName()
					+ "' is not compatible with '" + minType.getName() + "'.");
			}
		}
	}

	private void ensureSetterType(Protocol protocol, Class<?> setterType) {
		if (maxType == null) {
			if (minType == null) {
				// Has been or will be reported at another location.
				// errorNoGetter(protocol);
			} else if (!setterType.isAssignableFrom(minType)) {
				error(protocol, "Setter type '" + setterType.getName()
					+ "' must not be more specific than any getter type ('"
					+ minType.getName() + "').");
			}
			maxType = setterType;
		} else {
			if (setterType == null) {
				// Property has no constraints.
			} else if (maxType.isAssignableFrom(setterType)) {
				// Property has no stronger requirements.
			} else if (setterType.isAssignableFrom(maxType)) {
				// Property has a setter with a lower type.
				maxType = setterType;
			} else {
				// Incompatible types.
				error(protocol, "Mismatch of inherited setter types: '" + setterType.getName()
					+ "' is not compatible with '" + maxType.getName() + "'.");
			}
		}
	}

	private void setKeyType(Class<?> keyType) {
		this.keyType = keyType;
	}

	@Override
	public Class<?> getKeyType() {
		return keyType;
	}

	@Override
	public Type getGenericType() {
		return _genericType;
	}

	@Override
	public List<Method> getIndexedGetters() {
		return _indexedGetters;
	}

	/**
	 * @see #getIndexedGetters()
	 */
	void addIndexedGetter(Method method) {
		if (_indexedGetters.isEmpty()) {
			_indexedGetters = new ArrayList<>();
		}
		_indexedGetters.add(method);
	}

	@Override
	boolean potentiallyHasKeyProperty() {
		switch (kind()) {
			case ARRAY:
			case LIST: {
				if (isIndexed()) {
					return false;
				}
			}
			//$FALL-THROUGH$
			default:
				return super.potentiallyHasKeyProperty();
		}
	}

	@Override
	public boolean isIndexed() {
		return indexed;
	}

	/**
	 * @see #isIndexed()
	 */
	void setIndexed(boolean value) {
		this.indexed = value;
	}

	protected void initInstanceType(Class<?> contentType, Type genericContentType) {
		initInstanceType(contentType);
		if (PolymorphicConfiguration.class.isAssignableFrom(contentType)) {
			setInstanceType(MethodBasedPropertyDescriptor.resolveImplementationClass(genericContentType));
		}

	}

	static Class<?> resolveImplementationClass(Type polymorphicConfigurationType) {
		Map<TypeVariable<?>, Type> bindings = new HashMap<>();
		ConfigurationDescriptorImpl.addTypeBinding(bindings, polymorphicConfigurationType);
		return ConfigurationDescriptorImpl.resolveImplementationClass(bindings);
	}

}

