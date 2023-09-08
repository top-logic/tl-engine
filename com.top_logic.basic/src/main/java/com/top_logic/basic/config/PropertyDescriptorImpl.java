/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.ImplClasses;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ByteDefault;
import com.top_logic.basic.config.annotation.defaults.CharDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.ShortDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.format.BuiltInFormats;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.basic.func.Identity;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.reflect.JavaTypeUtil;
import com.top_logic.basic.type.PrimitiveTypeUtil;
import com.top_logic.basic.util.Utils;

/**
 * Runtime representation of a property of a {@link ConfigurationDescriptor}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public abstract class PropertyDescriptorImpl
		extends AbstractPropertyDescriptor<PropertyDescriptorImpl, AbstractConfigurationDescriptor> {

	/** Empty {@link PropertyDescriptorImpl} array. */
	public static final PropertyDescriptorImpl[] NO_PROPERTY_DESCRIPTORS = {};

	private static final String DEFAULT_ELEMENT_NAME = "entry";

	private static transient final Object UNRESOLVED_DEFAULT = new Object();
	
	private String configurationName;

	private Object computedDefaultValue = UNRESOLVED_DEFAULT;

	private boolean _isDefaultShared;

	private Class<?> elementType;
	
	private DefaultSpec _defaultSpec;

	private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = new MapBuilder<Class<?>, Object>()
		.put(boolean.class, Boolean.FALSE)
		.put(char.class, Character.valueOf('\u0000'))
		.put(short.class, Short.valueOf((short) 0))
		.put(byte.class, Byte.valueOf((byte) 0))
		.put(int.class, Integer.valueOf(0))
		.put(long.class, Long.valueOf(0L))
		.put(float.class, Float.valueOf(0.0f))
		.put(double.class, Double.valueOf(0.0d))
		.toMap();

	/**
	 * Is there an <em>explicit</em> default value for this property or any super property?
	 * <p>
	 * Explicitly storing whether an <em>implicit</em> default exists is not necessary: If this
	 * property is not mandatory at least the Java default exists (e.g. 0 for int-property).
	 * </p>
	 */
	private boolean _explicitDefault = false;

	private static final Object MISMATCH = new NamedConstant("mismatch");

	private ConfigurationValueProvider<?> valueProvider;
	
	private ConfigurationValueBinding<?> valueBinding;
	
	
	private PropertyKind kind;
	private String elementName;
	private String keyPropertyConfigurationName;

	private PropertyDescriptor keyProperty;
	private boolean multiple;
	private boolean ordered;
	private boolean _hasDerivedAnnotation;

	private boolean _hasLocalDerivedAnnotation;

	private DerivedPropertyAlgorithm _algorithm;

	private ConfigurationValueProviderFuture valueProviderFuture;
	private ConfigurationValueBindingFuture valueBindingFuture;

	private SubtypesDef _subtypesDef;

	private Map<String, ConfigurationDescriptor> _descriptorByElementName;

	private Map<ConfigurationDescriptor, String> _elementNameByDescriptor;

	private ConfigurationDescriptorImpl _elementDescriptor;

	private ConfigurationDescriptorImpl _defaultDescriptor;

	private boolean ambiguousInheritedSubConfigurations;

	private NullableAspect _nullable;

	/**
	 * If the property is mandatory, a value has to be set explicitly.
	 * <p>
	 * That is, mandatory deletes all defaults, regardless of whether {@link #_explicitDefault
	 * explicit} or implicit.
	 * </p>
	 */
	private boolean _hasMandatoryAnnotation = false;

	private boolean _hasLocalMandatoryAnnotation;

	/**
	 * The implementation type of {@link PolymorphicConfiguration} or {@link ConfiguredInstance}
	 * properties inferred from the property return type.
	 * 
	 * <p>
	 * This is not necessarily the default for the implementation class, which is either annotated
	 * to the property or inferred from the configuration descriptor of the implementation type.
	 * </p>
	 */
	private Class<?> _instanceType;

	private boolean _instanceFormat;

	private boolean _instanceValued;

	private boolean _hasContainerAnnotation;

	private boolean _hasLocalContainerAnnotation;

	private Mapping<Object, Object> _keyMapping;

	private boolean _hasAbstractAnnotation;

	private boolean _hasLocalAbstractAnnotation;

	private AlgorithmSpec _algorithmSpec;

	private boolean _encrypted;

	private boolean _localExplicitDefault;

	private AlgorithmDependency _derivedDependency;

	/**
	 * Creates a {@link PropertyDescriptor}.
	 * <p>
	 * TODO What is the state this object when this call returns? In particular: Is it fully
	 * initialized?
	 * </p>
	 * 
	 * @param protocol
	 *        The {@link Protocol} to which errors will be reported. Is not allowed to be
	 *        <code>null</code>.
	 * @param descriptor
	 *        The {@link ConfigurationDescriptor} to which this {@link PropertyDescriptor} belongs.
	 *        Is not allowed to be <code>null</code>.
	 * @param propertyName
	 *        The name of this property.
	 * @param superProperties
	 *        The list of direct super {@link PropertyDescriptor}s. (If the property is declared
	 *        only in a super {@link ConfigurationDescriptor} of the super
	 *        {@link ConfigurationDescriptor}, that property <em>is</em> in this list, though, as it
	 *        is a property in the direct super {@link ConfigurationDescriptor}, too.) In no
	 *        guaranteed order. Is not allowed to be <code>null</code>. Is not allowed to contain
	 *        <code>null</code>.
	 */
	public PropertyDescriptorImpl(Protocol protocol, AbstractConfigurationDescriptor descriptor, String propertyName,
			PropertyDescriptorImpl[] superProperties) {
		super(descriptor, propertyName, superProperties);
		if (isInherited()) {
			init(protocol);
		}
	}

	protected void init(Protocol protocol) {
		checkCommonRoot(protocol);

		String inheritedElementName = null;
		Class<?> inheritedElementType = null;
		String inheritedConfigurationName = null;
		Boolean inheritedInstanceValued = null;
		Object inheritedInstanceType = null;
		DefaultSpec inheritedDefaultSpec = null;
		Object inheritedValueProvider = null;
		Object inheritedValueBinding = null;
		Object inheritedSubConfigurations = null;
		Object inheritedElementDescriptor = null;
		PropertyDescriptorImpl firstSuperProperty = firstSuperProperty();
		boolean inheritedEncryption = firstSuperProperty.isEncrypted();
		
		for (int n = 0, cnt = getSuperProperties().length; n < cnt; n++) {
			PropertyDescriptorImpl inheritedProperty = getSuperProperties()[n];
			
			if (n > 0) {
				if ((inheritedProperty.kind() != firstSuperProperty.kind()) && (inheritedProperty.kind() != PropertyKind.DERIVED) && (firstSuperProperty.kind() != PropertyKind.DERIVED)) {
					error(protocol,
						"Property kind mismatch of inherited property in '"
							+ firstSuperProperty.getDescriptor().getConfigurationInterface() + "' and '"
							+ inheritedProperty.getDescriptor().getConfigurationInterface() + "': '"
							+ firstSuperProperty.kind() + "' vs. '" + inheritedProperty.kind() + "'.");
				}
				if (inheritedProperty.isMultiple() != firstSuperProperty.isMultiple()) {
					error(protocol,
						"Property multiplicity mismatch of inherited property in '"
							+ firstSuperProperty.getDescriptor().getConfigurationInterface() + "' and '"
							+ inheritedProperty.getDescriptor().getConfigurationInterface() + "': '"
							+ firstSuperProperty.isMultiple() + "' vs. '" + inheritedProperty.isMultiple() + "'.");
				}
				if (inheritedProperty.isOrdered() != firstSuperProperty.isOrdered()) {
					error(protocol,
						"Property ordering attribute mismatch of inherited property in '"
							+ firstSuperProperty.getDescriptor().getConfigurationInterface() + "' and '"
							+ inheritedProperty.getDescriptor().getConfigurationInterface() + "': '"
							+ firstSuperProperty.isOrdered() + "' vs. '" + inheritedProperty.isOrdered() + "'.");
				}
				if (inheritedEncryption != inheritedProperty.isEncrypted()) {
					StringBuilder encryptionMismatch = new StringBuilder();
					encryptionMismatch.append("Encryption attribute mismatch of inherited property '");
					encryptionMismatch.append(firstSuperProperty);
					encryptionMismatch.append("' and '");
					encryptionMismatch.append(inheritedProperty);
					encryptionMismatch.append("': ");
					encryptionMismatch.append(firstSuperProperty.isEncrypted() ? "encypted" : "non-encrypted");
					encryptionMismatch.append(" vs. ");
					encryptionMismatch.append(inheritedProperty.isEncrypted() ? "encrypted" : "non-encrypted");
					encryptionMismatch.append(".");
					error(protocol, encryptionMismatch.toString());
				}
			}
			
			if (inheritedConfigurationName == null) {
				inheritedConfigurationName = inheritedProperty.getPropertyName();
			} else {
				if (! inheritedConfigurationName.equals(inheritedProperty.getPropertyName())) {
					error(protocol, "Configuration name mismatch of inherited property '" + inheritedProperty + "': '"
						+ inheritedConfigurationName + "' vs. '" + inheritedProperty.getPropertyName() + "'.");
				}
			}
			
			if (inheritedValueProvider == null) {
				inheritedValueProvider = inheritedProperty.getValueProvider();
			} else {
				if (! inheritedValueProvider.equals(inheritedProperty.getValueProvider())) {
					inheritedValueProvider = MISMATCH;
				}
			}
			
			if (inheritedValueBinding == null) {
				inheritedValueBinding = inheritedProperty.getValueBinding();
			} else {
				if (! inheritedValueBinding.equals(inheritedProperty.getValueBinding())) {
					inheritedValueBinding = MISMATCH;
				}
			}
			
			if (inheritedInstanceValued == null) {
				inheritedInstanceValued = Boolean.valueOf(inheritedProperty.isInstanceValued());
			} else {
				if (inheritedInstanceValued.booleanValue() != inheritedProperty.isInstanceValued()) {
					error(protocol, "Mismatch of value types (instance valued vs. configuration valued) in inherited property '" + inheritedProperty + "'.");
				}
			}

			if (inheritedInstanceType == null) {
				inheritedInstanceType = inheritedProperty._instanceType;
			} else {
				if (inheritedInstanceType != inheritedProperty._instanceType) {
					inheritedInstanceType = MISMATCH;
				}
			}

			if (inheritedElementName == null) {
				inheritedElementName = inheritedProperty.getElementName();
			} else {
				if (! inheritedElementName.equals(inheritedProperty.getElementName())) {
					error(protocol, "Mismatch of inherited property element names '" + inheritedElementName + "' vs. '"
						+ inheritedProperty.getElementName() + "'.");
				}
			}
			
			Class<?> otherElementType = inheritedProperty.getElementType();
			if (inheritedElementType == null) {
				inheritedElementType = initInheritedElementType(inheritedProperty);
			} else {
				// Choose the most specific type.
				if (!otherElementType.isAssignableFrom(inheritedElementType)) {
					if (inheritedElementType.isAssignableFrom(otherElementType)) {
						// Other type is more specific.
						inheritedElementType = otherElementType;
					} else {
						error(protocol, "Incompatible element types '" + inheritedElementType + "' and '"
							+ otherElementType + "'.");
					}
				}
			}
			
			if (inheritedProperty.getDefaultSpec() == null) {
				// Ignore.
			} else if (inheritedDefaultSpec == null && inheritedProperty.hasExplicitDefault()) {
				/* The default that is found first, wins. (In super-interface declaration order.)
				 * The other defaults are ignored. */
				inheritedDefaultSpec = inheritedProperty.getDefaultSpec();
			}
			
			if (inheritedSubConfigurations == null) {
				inheritedSubConfigurations = inheritedProperty._subtypesDef;
			} else {
				if (inheritedProperty._subtypesDef == null) {
					// Ignore.
				} else if (!inheritedSubConfigurations.equals(inheritedProperty._subtypesDef)) {
					inheritedSubConfigurations = MISMATCH;
				}
			}

			if (inheritedElementDescriptor == null) {
				inheritedElementDescriptor = inheritedProperty._elementDescriptor;
			} else {
				if (inheritedProperty._elementDescriptor == null) {
					// Ignore.
				} else if (!inheritedElementDescriptor.equals(inheritedProperty._elementDescriptor)) {
					inheritedElementDescriptor = MISMATCH;
				}
			}
			
		}

		setConfigAttributeName(protocol, inheritedConfigurationName);
		this._instanceValued = inheritedInstanceValued;
		this._instanceFormat = inheritedInstanceValued;
		if (!mismatch(inheritedInstanceType)) {
			_instanceType = (Class<?>) inheritedInstanceType;
		}
		this.elementName = inheritedElementName;
		this.elementType = inheritedElementType;
		
		if (mismatch(inheritedSubConfigurations)) {
			this.ambiguousInheritedSubConfigurations = true;
			inheritedSubConfigurations = null;
		}

		setDefaultSpec(inheritedDefaultSpec);
		setValueProvider((ConfigurationValueProvider<?>) inheritedValueProvider);
		setValueBinding((ConfigurationValueBinding<?>) inheritedValueBinding);

		_subtypesDef = (SubtypesDef) inheritedSubConfigurations;

		assert firstSuperProperty.kind() != null : PropertyDescriptorImpl.message(this, "Uninitialized super property: " + firstSuperProperty);
		setMultiple(protocol, getDescriptor().getConfigurationInterface(), firstSuperProperty.isMultiple());
		setOrdered(firstSuperProperty.isOrdered());
		setKeyPropertyConfigurationName(firstSuperProperty.getKeyPropertyConfigurationName());
		if (inheritedElementDescriptor != MISMATCH) {
			_elementDescriptor = (ConfigurationDescriptorImpl) inheritedElementDescriptor;
		}
		setEncryped(inheritedEncryption);
	}

	protected Class<?> initInheritedElementType(PropertyDescriptorImpl inheritedProperty) {
		return inheritedProperty.getElementType();
	}

	private void checkCommonRoot(Protocol protocol) {
		Map<PropertyDescriptor, Set<PropertyDescriptor>> roots = getRoots();
		if (roots.size() > 1) {
			error(protocol, "Inherited properties have no common root property. "
				+ "Roots and the corresponding parents: " + roots);
		}
	}

	private Map<PropertyDescriptor, Set<PropertyDescriptor>> getRoots() {
		Map<PropertyDescriptor, Set<PropertyDescriptor>> roots =
			new LinkedHashMap<>();

		for (PropertyDescriptorImpl parent : getSuperProperties()) {
			MultiMaps.add(roots, parent.getRoot(), parent);
		}
		return roots;
	}

	private PropertyDescriptor getRoot() {
		if (isLocalProperty()) {
			return this;
		}
		// All super properties have a common root. Therefore, it doesn't matter which super
		// property is asked for its root.
		return firstSuperProperty().getRoot();
	}

	private boolean mismatch(Object... values) {
		boolean hasValue = false;
		for (Object obj : values) {
			if (obj == MISMATCH) {
				return true;
			}
			else if (obj != null) {
				if (hasValue) {
					return true;
				} else {
					hasValue = true;
				}
			}
		}
		return false;
	}

	String internalName() {
		return identifier().asString();
	}

	/*package protected*/ void setConfigAttributeName(Protocol protocol, String name) {
		if (configurationName != null && !configurationName.equals(name)) {
			error(protocol, "Has already a configuration name '" + configurationName
				+ "', must not be redeclared as '" + name + "'.");
			return;
		}
		this.configurationName = name;
	}

	@Override
	public String getPropertyName() {
		if (configurationName == null) {
			return ConfigurationDescriptorBuilder.getDefaultConfigurationName(internalName());
		}
		return configurationName;
	}

	@Override
	public Object getDefaultValue() {
		if (isDefaultShared()) {
			if (computedDefaultValue == UNRESOLVED_DEFAULT) {
				// resolve default on demand.
				LogProtocol protocol = new LogProtocol(PropertyDescriptor.class);
				resolveDefault(protocol);
				protocol.checkErrors();
				assert computedDefaultValue != UNRESOLVED_DEFAULT : PropertyDescriptorImpl.message(this, "Shard default was not resolved.");
			}
			return computedDefaultValue;
		} else {
			LogProtocol protocol = new LogProtocol(PropertyDescriptor.class);
			Object result = newDefaultValue(protocol);
			protocol.checkErrors();
			return result;
		}
	}

	private Class<?> nonPrimitiveReturnType() {
		Class<?> type = getType();
		if (type.isPrimitive()) {
			return getPrimitiveWrapperClass(type);
		} else {
			return type; 
		}
	}

	@Override
	public Class<?> getElementType() {
		return elementType;
	}
	
	/*package protected*/ void setElementType(Class<?> elementType) {
		assert elementType != null : PropertyDescriptorImpl.message(this, "Element type not initialized.");
		this.elementType = elementType;
	}
	
	public abstract Class<?> getKeyType();

	abstract boolean hasSetter();

	/**
	 * Returns true, if and only if this property (not a super property) has that
	 * {@link Annotation}.
	 */
	boolean hasLocalAnnotation(Class<? extends Annotation> annotationClass) {
		return getLocalAnnotation(annotationClass) != null;
	}

	/**
	 * @see #hasMandatoryAnnotation()
	 */
	private void setHasMandatoryAnnotation(boolean hasMandatoryAnnotation) {
		_hasMandatoryAnnotation = hasMandatoryAnnotation;
		if (hasMandatoryAnnotation == false) {
			setHasLocalMandatoryAnnotation(false);
		}
	}

	/**
	 * @see #hasMandatoryAnnotation()
	 * @see #hasLocalMandatoryAnnotation()
	 */
	@Override
	public boolean isMandatory() {
		return hasMandatoryAnnotation();
	}

	/**
	 * Is {@link Mandatory} annotated on this this property or any super property?
	 * 
	 * @see #isMandatory()
	 * @see #hasLocalMandatoryAnnotation()
	 */
	public boolean hasMandatoryAnnotation() {
		return _hasMandatoryAnnotation;
	}

	/**
	 * Is {@link Mandatory} annotated on the local property? (i.e. not inherited)
	 * 
	 * @see #isMandatory()
	 * @see #hasMandatoryAnnotation()
	 */
	public boolean hasLocalMandatoryAnnotation() {
		return _hasLocalMandatoryAnnotation;
	}

	/**
	 * @see #hasLocalMandatoryAnnotation()
	 */
	protected void setHasLocalMandatoryAnnotation(boolean haslocalMandatoryAnnotation) {
		_hasLocalMandatoryAnnotation = haslocalMandatoryAnnotation;
		if (haslocalMandatoryAnnotation) {
			setHasMandatoryAnnotation(true);
		}
	}

	/** 
	 * Tries to normalize the given value to a legal value of this property. In case it can not be normalized the given value is returned.
	 * 
	 * @see ConfigurationValueCheck#normalize(Object)
	 */
	@FrameworkInternal
	Object normalize(Object value) {
		if ((value == null) && isNullable()) {
			return null;
		}
		switch (kind()) {
			case COMPLEX: {
				return valueBinding.normalize(value);
			}
			case PLAIN: {
				return valueProvider.normalize(value);
			}
			case MAP:
				if (value == null) {
					return Collections.emptyMap(); 
				}
				break;
			case ARRAY:
				if (value == null) {
					return newArrray(this, 0);
				}
				break;
			case LIST:
				if (value == null) {
					return Collections.emptyList(); 
				}
				break;
			default:
		}
		return value;
	}

	@Override
	public final boolean isNullable() {
		return getNullableAspect().isNullable();
	}

	@Override
	public final boolean isDefaultContainer() {
		return getDescriptor().getDefaultContainer() == this;
	}

	/**
	 * Checks that the given value conforms to type and nullability of this property.
	 *
	 * @param value
	 *        The value to check.
	 * @throws IllegalArgumentException
	 *         If the check fails.
	 */
	@FrameworkInternal
	public final void checkValue(Object value) throws IllegalArgumentException {
		checkValue(this, value);
	}

	/**
	 * Checks whether the given value is a legal value for the given property.
	 */
	// Called from generated code.
	@CalledByReflection
	public static void checkValue(PropertyDescriptor property, Object value) {
		Class<?> propertyType = property.getType();
		if (propertyType.isPrimitive()) {
			Class<?> expectedType = getPrimitiveWrapperClass(propertyType);
			checkExpectedType(property, expectedType, value);
		} else {
			if (value != null) {
				checkExpectedType(property, propertyType, value);
				checkNonNullValue(property, value);
			} else {
				checkNullValue(property);
			}
		}
	}

	private static void checkNonNullValue(PropertyDescriptor property, Object value) throws IllegalArgumentException {
		switch (property.kind()) {
			case ARRAY: {
				// Check contents.
				checkCorrectListValues(property, Collections.emptyList(), Collections.emptyList(),
					PropertyDescriptorImpl.arrayAsList(value));
				break;
			}

			case LIST: {
				// Check contents.
				checkCorrectListValues(property, Collections.emptyList(), Collections.emptyList(),
					(Collection<?>) value);
				break;
			}

			case MAP: {
				// Check contents.
				checkCorrectMapValues(property, (Map<?, ?>) value);
				break;
			}

			case PLAIN: {
				boolean legalValue = property.getValueProvider().isLegalValue(value);
				if (!legalValue) {
					throw new IllegalArgumentException(PropertyDescriptorImpl.message(property,
						"Value '" + value + "' is not legal for value provider '"
							+ property.getValueProvider() + "'."));
				}
				break;
			}

			case COMPLEX: {
				boolean legalValue = property.getValueBinding().isLegalValue(value);
				if (!legalValue) {
					throw new IllegalArgumentException(PropertyDescriptorImpl.message(property,
						"Value '" + value + "' is not legal for value binding '"
							+ property.getValueBinding() + "'."));
				}
				break;
			}
			case DERIVED:
			case ITEM:
			case REF:
				// No Check needed.
				break;
		}
	}

	/**
	 * Checks that the values to put into the value map can be put without violating constraints.
	 * 
	 * @param valuesToPut
	 *        Values to put into the current value map.
	 * 
	 * @throws IllegalArgumentException
	 *         iff the given values can not be added to the current values.
	 */
	public static void checkCorrectMapValues(PropertyDescriptor property, Map<?, ?> valuesToPut)
			throws IllegalArgumentException {
		Class<?> expectedElementType = property.getElementType();
		if (expectedElementType.isPrimitive()) {
			expectedElementType = getPrimitiveWrapperClass(expectedElementType);
		}
		Mapping<Object, Object> keyMapping = property.getKeyMapping();
		for (Entry<?, ?> entry : valuesToPut.entrySet()) {

			// Check item type.
			Object entryValue = entry.getValue();
			checkExpectedType(property, expectedElementType, entryValue);

			// Check correct indexing.
			Object expectedKeyPropertyValue = keyMapping.map(entryValue);
			Object entryKey = entry.getKey();
			if (!CollectionUtil.equals(expectedKeyPropertyValue, entryKey)) {
				throw new IllegalArgumentException(
					PropertyDescriptorImpl.message(property, "Key of value '" + entryValue + "' is expected to be '"
					+ expectedKeyPropertyValue + "' but was '" + entryKey + "'."));
			}
		}
	}

	/**
	 * Checks that the added values are appropriate to the current list values, i.e. the added
	 * values may be added to the current values without violating constraints after the values to
	 * remove are removed.
	 * 
	 * @param currentValues
	 *        Values currently contained in the value list.
	 * @param valuesToRemove
	 *        Values designated to be removed from the current list.
	 * @param valuesToAdd
	 *        Values designated to be added to the current list.
	 * 
	 * @throws IllegalArgumentException
	 *         iff the given changes can not be applied.
	 */
	public static void checkCorrectListValues(PropertyDescriptor property, Collection<?> currentValues,
			Collection<?> valuesToRemove,
			Collection<?> valuesToAdd) throws IllegalArgumentException {
		if (valuesToAdd.isEmpty()) {
			return;
		}

		Class<?> expectedElementType = property.getElementType();
		if (expectedElementType.isPrimitive()) {
			expectedElementType = getPrimitiveWrapperClass(expectedElementType);
		}
		final Mapping<Object, Object> keyMapping = property.getKeyMapping();
		if (keyMapping != null) {
			HashSet<Object> knownKeys =
				CollectionUtil.newSet(valuesToAdd.size() + currentValues.size() - valuesToRemove.size());
			for (Object currentContent : currentValues) {
				knownKeys.add(keyMapping.map(currentContent));
			}
			for (Object element : valuesToAdd) {
				checkExpectedType(property, expectedElementType, element);
				final Object keyValue = keyMapping.map(element);
				if (!knownKeys.add(keyValue)) {
					// some previous item has same key
					throw new IllegalArgumentException(
						PropertyDescriptorImpl.message(property, "Value to add '" + element + "' has the same key '"
						+ keyValue
						+ "' as some different value in."));
				}
			}
		} else {
			for (Object element : valuesToAdd) {
				checkExpectedType(property, expectedElementType, element);
			}
		}
	}

	private static void checkExpectedType(PropertyDescriptor property, Class<?> expectedType, Object value)
			throws IllegalArgumentException {
		if (value == null) {
			// Better error message for illegal null values.
			checkNullValue(property);
		}
		if (!expectedType.isInstance(value)) {
			throw new IllegalArgumentException(PropertyDescriptorImpl.message(property, "Value " + description(value)
				+ " is not of expected type '" + expectedType.getName() + "'."));
		}
	}

	private static String description(Object value) {
		if (value instanceof ConfigurationItem) {
			ConfigurationItem item = (ConfigurationItem) value;
			return item.toString() + " (" + item.location() + ")";
		}
		return StringServices.getObjectDescription(value);
	}
	
	static void checkNullValue(PropertyDescriptor property) throws IllegalArgumentException {
		if (property.getType().isPrimitive()) {
			// Better error message for primitive properties.
			throw errorPrimitiveIsNull(property);
		}
		if (!property.isNullable()) {
			String message = "Property is non-nullable, therefore 'null' is not allowed.";
			if (property.getValueProvider() != null) {
				message += " Value provider: '" + property.getValueProvider() + "'.";
			} else if (property.getValueBinding() != null) {
				message += " Value binding: '" + property.getValueBinding() + "'.";
			}
			throw new IllegalArgumentException(PropertyDescriptorImpl.message(property, message));
		}
	}

	static IllegalArgumentException errorPrimitiveIsNull(PropertyDescriptor property) {
		return new IllegalArgumentException(message(property, "A primitive property cannot be null."));
	}

	static Class<?> getPrimitiveWrapperClass(Class<?> type) {
		return PrimitiveTypeUtil.getWrapper(type);
	}

	public static Object getPrimitiveDefault(Class<?> type) {
		if (! type.isPrimitive()) throw new IllegalArgumentException("Not a primitive type.");

		return PRIMITIVE_DEFAULTS.get(type);
	}

	void setImplicitDefault(DefaultSpec defaultSpec) {
		setDefaultSpec(defaultSpec);
	}

	void setAnnotatedDefaultSpec(DefaultSpec defaultSpec) {
		setDefaultSpec(defaultSpec);
		setLocalExplicitDefault(true);
		setExplicitDefault(true);
	}

	/**
	 * @see #hasLocalExplicitDefault()
	 */
	@Override
	public boolean hasExplicitDefault() {
		return _explicitDefault;
	}

	/**
	 * @see #hasExplicitDefault()
	 */
	private void setExplicitDefault(boolean explicitDefault) {
		_explicitDefault = explicitDefault;
	}

	DefaultSpec getDefaultSpec() {
		return _defaultSpec;
	}

	private void setDefaultSpec(DefaultSpec defaultSpec) {
		_defaultSpec = defaultSpec;
	}

	/*package protected*/ void setValueProvider(ConfigurationValueProvider<?> valueProvider) {
		this.valueProvider = valueProvider;
	}
	
	/*package protected*/ void setConfigurationValueProviderFuture(ConfigurationValueProviderFuture valueProvider) {
		this.valueProviderFuture = valueProvider;
	}
	
	/*package protected*/ void setConfigurationValueBindingFuture(ConfigurationValueBindingFuture valueBinding) {
		this.valueBindingFuture = valueBinding;
	}
	
	@Override
	public ConfigurationValueProvider getValueProvider() {
		return valueProvider;
	}
	
	/*package protected*/ void setValueBinding(ConfigurationValueBinding<?> valueBinding) {
		this.valueBinding = valueBinding;
	}
	
	@Override
	public ConfigurationValueBinding getValueBinding() {
		return valueBinding;
	}

	/**
	 * Resolves all relations to other {@link PropertyDescriptor}. It is
	 * guaranteed that all local properties are resolved but the external
	 * references may not be resolved.
	 */
	/*package protected*/ void resolveExternalRelations(Protocol protocol) {
		switch (kind) {
			case DERIVED:
			case REF:
			case ITEM:
				initElementDescriptor(protocol, getType());
				break;
			case ARRAY:
			case LIST:
			case MAP:
				initElementDescriptor(protocol, getElementType());
				initKeyProperty(protocol);
				break;
			case COMPLEX:
			case PLAIN:
				// No external references
				break;
		}
		initSubConfigurationsByTag(protocol);
	}

	/* package protected */ void resolveLocalProperties(Protocol protocol) {
		initValueAnnotations(protocol);
		
		resolveFromDeclaredReturnType(protocol);
		checkAnnotationImplementationClassDefault(protocol);
		_isDefaultShared = computeIsDefaultShared(protocol);
		
		if (hasSetter()) {
			resolveSetter(protocol);
		}
		
		if (configurationName == null) {
			// No configuration name defined, use canonical one.
			setConfigAttributeName(protocol, ConfigurationDescriptorBuilder.getDefaultConfigurationName(internalName()));
		}
		
		if (getType() == null) {
			assert protocol.hasErrors() : PropertyDescriptorImpl.message(this, "Previous error must have been reported.");
			return;
		}
		
		if (valueProviderFuture != null) {
			setValueProvider(valueProviderFuture.resolveFor(protocol, this));
		}
		if (valueBindingFuture != null) {
			setValueBinding(valueBindingFuture.resolveFor(protocol, this));
		}

		initKind(protocol);
		if (isEncrypted()) {
			if (getValueProvider() != null) {
				setValueProvider(EncodingConfigurationValueProvider.ensureEncodedConfigurationValue(getValueProvider()));
			} else {
				error(protocol, "Encryption can only be used for properties with a value provider.");
			}
		}
		_nullable = new NullableAspect(this, protocol);
	}

	abstract void resolveFromDeclaredReturnType(Protocol protocol);

	abstract void resolveSetter(Protocol protocol);

	public final NullableAspect getNullableAspect() {
		return _nullable;
	}

	private void initValueAnnotations(Protocol protocol) {
		acceptValueAnnotationsLocal(protocol);
		checkValueAnnotationsConflictLocal(protocol);
		if (!hasLocalValueAnnotation()) {
			acceptValueAnnotationsInherited();
		} else if (hasLocalDerivedAnnotation() && !hasLocalExplicitDefault()) {
			acceptFromSuperPropertiesDefault();
		} else if (hasLocalExplicitDefault() && !hasLocalDerivedAnnotation()) {
			acceptFromSuperPropertiesDerived();
		}
	}

	private void checkAnnotationImplementationClassDefault(Protocol protocol) {
		Class<?> implClass = annotatedImplementationClassDefault();
		if (implClass == null) {
			return;
		}
		checkAnnotationImplementationClassDefault(protocol, implClass);
	}

	protected void checkAnnotationImplementationClassDefault(Protocol protocol, Class<?> implClass) {
		if (implClass.isPrimitive()) {
			error(protocol, "A primitive type cannot be used as implementation class: " + implClass.getName(), null);
		}
		if (implClass.isArray()) {
			error(protocol, "Array types cannot be used as implementation class: " + implClass.getName(), null);
		}
		if (_instanceType != null) {
			if (Modifier.isAbstract(implClass.getModifiers())) {
				error(protocol, "Abstract type cannot be used as implementation class: " + implClass.getName(), null);
			}
		}

		Class<?> type = _instanceType != null ? _instanceType : elementType;
		if (!type.isAssignableFrom(implClass)) {
			error(protocol, "The declared implementation class default '" + implClass.getName()
				+ "' must be compatible with the property type '" + type.getName() + "'.");
		}
	}

	private Class<?> annotatedImplementationClassDefault() {
		ImplementationClassDefault annotation = getAnnotation(ImplementationClassDefault.class);
		if (annotation == null) {
			return null;
		}
		return annotation.value();
	}

	private void acceptValueAnnotationsLocal(Protocol protocol) {
		acceptAnnotationDefault(protocol);
		acceptAnnotationMandatory();
		acceptAnnotationDerived(protocol);
		acceptAnnotationContainer(protocol);
		acceptAnnotationAbstract();
	}

	private void acceptAnnotationDefault(Protocol protocol) {
		MutableInteger numberDefaultAnnotations = new MutableInteger();
		setAnnotatedDefaults(numberDefaultAnnotations);
		if (numberDefaultAnnotations.intValue() > 1) {
			error(protocol, "Has more than one default value.");
		}
		if (_defaultSpec == null && getValueProvider() != null) {
			setImplicitDefault(DefaultFromFormat.INSTANCE);
		}
	}

	protected void setAnnotatedDefaults(MutableInteger numberDefaultAnnotations) {
		final BooleanDefault booleanAnnotation = getLocalAnnotation(BooleanDefault.class);
		if (booleanAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(booleanAnnotation.value()));
		}
		final ByteDefault byteAnnotation = getLocalAnnotation(ByteDefault.class);
		if (byteAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(byteAnnotation.value()));
		}
		final ShortDefault shortAnnotation = getLocalAnnotation(ShortDefault.class);
		if (shortAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(shortAnnotation.value()));
		}
		final CharDefault charAnnotation = getLocalAnnotation(CharDefault.class);
		if (charAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(charAnnotation.value()));
		}
		final FloatDefault floatAnnotation = getLocalAnnotation(FloatDefault.class);
		if (floatAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(floatAnnotation.value()));
		}
		final DoubleDefault doubleAnnotation = getLocalAnnotation(DoubleDefault.class);
		if (doubleAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(doubleAnnotation.value()));
		}
		final IntDefault intAnnotation = getLocalAnnotation(IntDefault.class);
		if (intAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(intAnnotation.value()));
		}
		final LongDefault longAnnotation = getLocalAnnotation(LongDefault.class);
		if (longAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(longAnnotation.value()));
		}
		final ClassDefault classAnnotation = getLocalAnnotation(ClassDefault.class);
		if (classAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(classAnnotation.value()));
		}
		final StringDefault stringAnnotation = getLocalAnnotation(StringDefault.class);
		if (stringAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(stringAnnotation.value()));
		}
		final InstanceDefault instanceAnnotation = getLocalAnnotation(InstanceDefault.class);
		if (instanceAnnotation != null) {
			numberDefaultAnnotations.inc();
			Class<?> defaultImplementationClass = instanceAnnotation.value();
			setAnnotatedDefaultSpec(new InstanceDefaultSpec(defaultImplementationClass));
		}
		final FormattedDefault formattedAnnotation = getLocalAnnotation(FormattedDefault.class);
		if (formattedAnnotation != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new FormattedDefaultSpec(formattedAnnotation.value()));
		}
		final ComplexDefault complexAnnotation = getLocalAnnotation(ComplexDefault.class);
		if (complexAnnotation != null) {
			numberDefaultAnnotations.inc();

			setAnnotatedDefaultSpec(new ComplexDefaultSpec(complexAnnotation.value()));
		}
		final ItemDefault itemDefault = getLocalAnnotation(ItemDefault.class);
		if (itemDefault != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new ItemDefaultSpec(itemDefault.value()));
		}
		final ListDefault listDefault = getLocalAnnotation(ListDefault.class);
		if (listDefault != null) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new ListDefaultSpec(listDefault.value()));
		}
		if (hasLocalAnnotation(NullDefault.class)) {
			numberDefaultAnnotations.inc();
			setAnnotatedDefaultSpec(new LiteralDefault(null));
		}
	}

	protected abstract void acceptAnnotationMandatory();

	private void acceptAnnotationAbstract() {
		if (hasLocalAnnotation(Abstract.class)) {
			setHasLocalAbstractAnnotation(true);
		}
	}

	private void acceptValueAnnotationsInherited() {
		boolean hasDefault = acceptFromSuperPropertiesDefault();
		if (hasDefault) {
			acceptFromSuperPropertiesDerived();
			return;
		}
		boolean hasContainerAnnotation = acceptFromSuperPropertiesContainer();
		if (hasContainerAnnotation) {
			return;
		}
		boolean isDerived = acceptFromSuperPropertiesDerived();
		if (isDerived) {
			return;
		}
		boolean isMandatory = acceptFromSuperPropertiesMandatory();
		if (isMandatory) {
			return;
		}
		acceptFromSuperPropertiesAbstract();
	}

	/**
	 * Has any super property an explicit default value?
	 */
	private boolean acceptFromSuperPropertiesDefault() {
		boolean isInheritingDefault = hasAnySuperExplicitDefault();
		setExplicitDefault(isInheritingDefault);
		return isInheritingDefault;
	}

	/**
	 * Is any super property mandatory?
	 */
	private boolean acceptFromSuperPropertiesMandatory() {
		boolean isInheritingMandatory = isAnySuperMandatory();
		setHasMandatoryAnnotation(isInheritingMandatory);
		return isInheritingMandatory;
	}

	protected void acceptFromSuperPropertiesAbstract() {
		setHasAbstractAnnotation(areAllSuperAbstract());
	}

	/**
	 * Has to be called after the local annotations and before the inherited annotations are
	 * accepted.
	 * <p>
	 * Reason: The fields for the value annotations are used for storing the end result of accepting
	 * the local and the inherited annotations. Therefore, the check for a <b>local</b> conflict has
	 * to be done before the inherited annotations are stored to those fields.
	 * </p>
	 */
	private void checkValueAnnotationsConflictLocal(Protocol protocol) {
		int valueAnnotations = 0;
		// "Default" and "derived" are not conflicting with each other
		if (hasLocalExplicitDefault() || hasLocalDerivedAnnotation()) {
			valueAnnotations += 1;
		}
		if (hasLocalMandatoryAnnotation()) {
			valueAnnotations += 1;
		}
		if (hasLocalContainerAnnotation()) {
			valueAnnotations += 1;
		}
		if (hasLocalAbstractAnnotation()) {
			valueAnnotations += 1;
		}
		if (valueAnnotations > 1) {
			error(protocol,
				"Only one of the settings (default value or derived), mandatory, container, or abstract"
					+ " is allowed.");
		}
	}

	/**
	 * "Value Annotations" are the features "Derived Properties" ("Container Property"),
	 * "Mandatory", "Default Values" and "Abstract", that use annotations that say something about
	 * the value of a property. They (inter-)act in a similar and systematic way and are therefore
	 * often handled together.
	 */
	private boolean hasLocalValueAnnotation() {
		return hasLocalExplicitDefault() || hasLocalMandatoryAnnotation()
			|| hasLocalDerivedAnnotation() || hasLocalContainerAnnotation()
			|| hasLocalAbstractAnnotation();
	}

	private boolean hasAnySuperExplicitDefault() {
		for (PropertyDescriptor superDescriptor : getSuperProperties()) {
			if (superDescriptor.hasExplicitDefault()) {
				return true;
			}
		}
		return false;
	}

	private boolean isAnySuperMandatory() {
		for (PropertyDescriptor superDescriptor : getSuperProperties()) {
			if (superDescriptor.isMandatory()) {
				return true;
			}
		}
		return false;
	}

	protected boolean areAllSuperAbstract() {
		if (isLocalProperty()) {
			return false;
		}
		for (PropertyDescriptor superDescriptor : getSuperProperties()) {
			if (!superDescriptor.isAbstract()) {
				return false;
			}
		}
		return true;
	}

	protected void initInstanceType(Class<?> contentType) {
		setElementType(contentType);

		boolean instanceValued = ConfiguredInstance.class.isAssignableFrom(contentType) || hasInstanceFormat();
		if (instanceValued) {
			setInstanceType(contentType);
		}
		_instanceValued = instanceValued;
	}

	void setInstanceFormat(boolean instanceFormat) {
		_instanceFormat = instanceFormat;
	}

	private boolean hasInstanceFormat() {
		return _instanceFormat;
	}

	@Override
	public boolean isInstanceValued() {
		return _instanceValued;
	}

	@Override
	public Class<?> getInstanceType() {
		return _instanceType;
	}

	private boolean isItemValued() {
		return ConfigurationItem.class.isAssignableFrom(getElementType());
	}

	Class<?> resolveConfigurationType(Type configuredInstanceType) {
		Map<TypeVariable<?>, Type> bindings = new HashMap<>();
		ConfigurationDescriptorImpl.addTypeBinding(bindings, configuredInstanceType);
		Class<?> configType =
			ConfigurationDescriptorImpl.resolveBound(bindings, ConfiguredInstance.class.getTypeParameters()[0]);
		return configType;
	}

	protected void setInstanceType(Class<?> defaultImplementationClass) {
		_instanceType = defaultImplementationClass;
	}

	void resolveDefault(Protocol protocol) {
		if (computedDefaultValue != UNRESOLVED_DEFAULT) {
			StringBuilder info = new StringBuilder();
			info.append("Default value for property '");
			info.append(getPropertyName());
			info.append("' already resolved.");
			// default already resolved
			protocol.info(info.toString(), Protocol.VERBOSE);
			return;
		}
		if (isDefaultShared()) {
			computedDefaultValue = newDefaultValue(protocol);
			return;
		} else {
			StringBuilder info = new StringBuilder();
			info.append("Default value for property '");
			info.append(getPropertyName());
			info.append("' is not shared and is therefore created at ConfigurationItem construction.");
			protocol.info(info.toString(), Protocol.VERBOSE);
		}
	}

	private void checkDefaultValue(Protocol protocol, Object newDefault) {
		try {
			checkValue(newDefault);
		} catch (RuntimeException ex) {
			StringBuilder error = new StringBuilder();
			error.append("Default value does not conform to property type '");
			error.append(getType().getName());
			error.append("'. Value: ");
			error.append(newDefault);
			error(protocol, error.toString(), ex);
		}
	}

	private Object resolveDefaultSpec(Protocol protocol) {
		try {
			Object result = getDefaultSpec().getDefaultValue(this);
			return result;
		} catch (ConfigurationException ex) {
			error(protocol, "Default value cannot be resolved.", ex);
			return null;
		}
	}

	private Object newDefaultValue(Protocol protocol) {
		Object newValue = getDefaultSpec() != null ? resolveDefaultSpec(protocol) : newDefaultFromType();
		if (!isMandatory()) {
			// The default value of a mandatory property is not required to conform to the
			// constraints of a property. The default value of a mandatory property is never
			// accessed in a loaded configuration. In a programatically constructed
			// configuration, constraints are not checked anyway, therefore, the default of such
			// property may violate e.g. the non-nullable constraint of the property.
			checkDefaultValue(protocol, newValue);
		}

		return newValue;
	}

	private Object newDefaultFromType() {
		switch (kind()) {
			case PLAIN: {
				assert valueProvider != null : PropertyDescriptorImpl.message(this, "Primitive property without a format.");
				return valueProvider.defaultValue();
			}
			case COMPLEX: {
				assert valueBinding != null : PropertyDescriptorImpl.message(this, "Complex property without a value binding.");
				return valueBinding.defaultValue();
			}
			case ARRAY: {
				return newArrray(this, 0);
			}
			case LIST: {
				return new ArrayList<>();
			}
			case MAP: {
				return new HashMap<>();
			}
			case DERIVED: {
				if (hasContainerAnnotation()) {
					return null;
				}
				if (valueProvider != null) {
					return valueProvider.defaultValue();
				}
				if (valueBinding != null) {
					return valueBinding.defaultValue();
				}
				if (getType().isPrimitive()) {
					return getPrimitiveDefault(getType());
				}
			}
			default: {
				// in general no default
				return null;
			}
		}
	}

	protected boolean computeIsDefaultShared(Protocol protocol) {
		DefaultSpec defaultSpec = getDefaultSpec();

		if (defaultSpec != null) {
			return defaultSpec.isShared(this);
		}

		if (isInherited()) {
			return firstSuperProperty().isDefaultShared();
		} else {
			if (getType() == null) {
				/* When only a setter without a corresponding getter is configured. Appears for
				 * example in test cases for invalid configurations
				 * TestInvalidConfigurationInterface#testSetterWithoutGetter */
				Logger.error("A configuration property getter for '" + getPropertyName()
					+ "' is missing.", PropertyDescriptorImpl.class);

				return false;
			}

			return BuiltInFormats.isPrimitive(getType());
		}
	}

	/** Is the default value shared between the {@link ConfigurationItem} instances? */
	public boolean isDefaultShared() {
		return _isDefaultShared;
	}

	protected void initKind(Protocol protocol) {
		if (isDerived() || hasContainerAnnotation()) {
			setKind(PropertyKind.DERIVED);
		} else if (getValueBinding() != null) {
			setKind(PropertyKind.COMPLEX);
		}
		else if (getValueProvider() != null && !isItemValued()) {
			setKindPlain(protocol);
		}
		else if (isMultiRefType(getType())) {
			if (isArrayType(getType())) {
				Class<?> componentType = getType().getComponentType();
				if (BuiltInFormats.isPrimitive(componentType)) {
					ensureValueProvider(protocol, getType());
					setKind(PropertyKind.PLAIN);
				} else {
					setKindCollection(protocol, getDescriptor().getConfigurationInterface());
				}
			} else {
				setKindCollection(protocol, getDescriptor().getConfigurationInterface());
			}
		}
		else if (isIndexedRefType(getType())) {
			setKindMap(protocol, getDescriptor().getConfigurationInterface());
		}
		else if (BuiltInFormats.isPrimitive(getType())) {
			ensureValueProvider(protocol, getType());
			setKind(PropertyKind.PLAIN);
		} else {
			// Structure property.
			setKind(PropertyKind.ITEM);
		}
	}

	private void setKindMap(Protocol protocol, Class<?> configurationInterface) {
		// Map property.
		ensureElementName(protocol, configurationInterface);
		setMultiple(protocol, configurationInterface, true);
		setOrdered(false);
		setKind(PropertyKind.MAP);
	}

	private void setKindCollection(Protocol protocol, Class<?> configurationInterface) {
		// List property.
		ensureElementName(protocol, configurationInterface);
		setMultiple(protocol, configurationInterface, true);
		setOrdered(isOrderedRefType(getType()));
		setKind(isArrayType(getType()) ? PropertyKind.ARRAY : PropertyKind.LIST);
	}

	private void setKindPlain(Protocol protocol) {
		Class<?> providedValueType = PrimitiveTypeUtil.asNonPrimitive(getValueProvider().getValueType());
		Class<?> expectedValueType = nonPrimitiveReturnType();
		if (providedValueType != expectedValueType && !providedValueType.isAssignableFrom(expectedValueType)) {
			String message = "Provider '" + getValueProvider() + "' expects values of type '" + providedValueType
				+ "' but property may contain values of '" + expectedValueType + "'.";
			error(protocol, message);
		}
		setKind(PropertyKind.PLAIN);
	}

	protected void setMultiple(Protocol protocol, Class<?> configurationInterface, boolean value) {
		this.multiple = value;
		if (value) {
			ensureElementType(protocol, configurationInterface);
		}
	}
	
	@Override
	public boolean isMultiple() {
		return multiple;
	}

	protected void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}
	
	@Override
	public boolean isOrdered() {
		return ordered;
	}

	/**
	 * @see #isAbstract()
	 * @see #setHasLocalAbstractAnnotation(boolean)
	 */
	protected void setHasAbstractAnnotation(boolean hasAbstractAnnotation) {
		_hasAbstractAnnotation = hasAbstractAnnotation;
		if (!hasAbstractAnnotation) {
			setHasLocalAbstractAnnotation(false);
		}
	}

	@Override
	public boolean isAbstract() {
		return _hasAbstractAnnotation;
	}

	/**
	 * @see #hasLocalAbstractAnnotation()
	 * @see #setHasAbstractAnnotation(boolean)
	 */
	private void setHasLocalAbstractAnnotation(boolean hasLocalAbstractAnnotation) {
		_hasLocalAbstractAnnotation = hasLocalAbstractAnnotation;
		if (hasLocalAbstractAnnotation) {
			setHasAbstractAnnotation(true);
		}
	}

	/**
	 * Has this property a local {@link Abstract} annotation? (i.e. not inherited)
	 * 
	 * @see #isAbstract()
	 */
	private boolean hasLocalAbstractAnnotation() {
		return _hasLocalAbstractAnnotation;
	}

	@Override
	public boolean isDerived() {
		return _hasDerivedAnnotation;
	}

	/**
	 * @see #isDerived()
	 * @see #setHasLocalDerivedAnnotation(boolean)
	 */
	private void setHasDerivedAnnotation(boolean hasDerivedAnnotation) {
		_hasDerivedAnnotation = hasDerivedAnnotation;
		if (!hasDerivedAnnotation) {
			setHasLocalDerivedAnnotation(false);
		}
	}

	/**
	 * Has this property a local {@link Derived} or {@link DerivedRef} annotation? (i.e. not
	 * inherited)
	 * 
	 * @see #isDerived()
	 * @see PropertyKind#DERIVED
	 */
	private boolean hasLocalDerivedAnnotation() {
		return _hasLocalDerivedAnnotation;
	}

	/**
	 * @see #hasLocalDerivedAnnotation()
	 * @see #setHasDerivedAnnotation(boolean)
	 */
	private void setHasLocalDerivedAnnotation(boolean hasLocalDerivedAnnotation) {
		_hasLocalDerivedAnnotation = hasLocalDerivedAnnotation;
		if (hasLocalDerivedAnnotation) {
			setHasDerivedAnnotation(true);
		}
	}

	@Override
	public DerivedPropertyAlgorithm getAlgorithm() {
		return _algorithm;
	}

	private void initDerived(AlgorithmSpec algorithmSpec) {
		setHasDerivedAnnotation(algorithmSpec != null);
		setAlgorithmSpec(algorithmSpec);
	}

	/**
	 * Finishes the initialization of the "derived" feature by creating the
	 * {@link DerivedPropertyAlgorithm}.
	 */
	void initAlgorithm(Protocol protocol) {
		_algorithm = getAlgorithmSpecDirectly().createAlgorithm(protocol, this,
			"derived property '" + this.toString() + "'");
		setAlgorithmSpec(null);
	}

	/**
	 * Is any super property derived?
	 */
	private boolean acceptFromSuperPropertiesDerived() {
		for (PropertyDescriptorImpl inheritedProperty : getSuperProperties()) {
			AlgorithmSpec inheritedAlgorithmSpec = inheritedProperty.getAlgorithmSpec();
			if (inheritedAlgorithmSpec != null) {
				initDerived(inheritedAlgorithmSpec);
				return true;
			}
		}
		initDerived(null);
		return false;
	}

	private AlgorithmSpec getAlgorithmSpec() {
		if (isDerived()) {
			DerivedPropertyAlgorithm propertyAlgorithm = getAlgorithm();
			if (propertyAlgorithm != null) {
				return propertyAlgorithm.getSpec();
			} else {
				return getAlgorithmSpecDirectly();
			}
		} else {
			return null;
		}
	}

	private AlgorithmSpec getAlgorithmSpecDirectly() {
		return _algorithmSpec;
	}

	private void setAlgorithmSpec(AlgorithmSpec algorithmSpec) {
		_algorithmSpec = algorithmSpec;
	}

	private void acceptAnnotationDerived(Protocol protocol) {
		Derived derivedAnnotation = getLocalAnnotation(Derived.class);
		DerivedRef derivedRefAnnotation = getLocalAnnotation(DerivedRef.class);
		if (derivedAnnotation != null && derivedRefAnnotation != null) {
			errorTwoDerivedAnnotations(protocol);
		} else if (derivedAnnotation != null) {
			setHasLocalDerivedAnnotation(true);
			initDerived(protocol, derivedAnnotation);
		} else if (derivedRefAnnotation != null) {
			setHasLocalDerivedAnnotation(true);
			initDerivedRef(protocol, derivedRefAnnotation);
		}
	}

	private void errorTwoDerivedAnnotations(Protocol protocol) {
		error(protocol, "Conflicting usage of derived specifications: Using both "
			+ Derived.class.getSimpleName() + " and " + DerivedRef.class.getSimpleName()
			+ " at the same property is invalid.");
	}

	private void initDerivedRef(Protocol protocol, DerivedRef annotation) {
		try {
			NamePath[] paths = new NamePath[] { NamePath.path(annotation) };
			initDerived(AlgorithmSpec.create(getDefaultFunction(), paths));
		} catch (IllegalArgumentException ex) {
			errorDerived(protocol, ex);
		}
	}

	private GenericFunction<?> getDefaultFunction() {
		return Identity.INSTANCE;
	}

	private void initDerived(Protocol protocol, Derived annotation) {
		try {
			Class<? extends GenericFunction<?>> functionClass = annotation.fun();
			GenericFunction<?> function = ConfigUtil.getInstance(functionClass);

			initDerived(AlgorithmSpec.create(function, annotation.args()));
		} catch (IllegalArgumentException ex) {
			errorDerived(protocol, ex);
		} catch (ConfigurationException ex) {
			errorDerived(protocol, ex);
		}
	}

	private void errorDerived(Protocol protocol, Exception cause) {
		error(protocol, "Unable to resolve algorithm for derived property.", cause);
	}

	protected static boolean isIndexedRefType(Class<?> type) {
		// Note: Only exactly these interfaces are allowed to simplify detection
		// of content type through analysis of type parameters (see callers).
		return type == Map.class;
	}

	protected static boolean isMultiRefType(Class<?> type) {
		// Note: Only exactly these interfaces are allowed to simplify detection
		// of content type through analysis of type parameters (see callers).
		return isOrderedRefType(type) || type == Collection.class || type == Set.class;
	}

	protected static boolean isOrderedRefType(Class<?> type) {
		return type == List.class || isArrayType(type);
	}

	protected static boolean isArrayType(Class<?> type) {
		return type.isArray();
	}
	
	private void ensureElementType(Protocol protocol, Class<?> configurationInterface) {
		if (getElementType() == null) {
			error(protocol, "Requires an element type declaration.");
		}
	}

	private void ensureValueProvider(Protocol protocol, Class<?> propertyType) {
		ConfigurationValueProvider<?> primitiveValueProvider =
			BuiltInFormats.getPrimitiveValueProvider(propertyType, hasLocalAnnotation(Nullable.class));
		setValueProvider(primitiveValueProvider);
	}

	boolean potentiallyHasKeyProperty() {
		switch (kind()) {
			case ARRAY:
			case LIST:
			case MAP:
				return true;
			default:
				// No key property needed
				break;
		}
		return false;
	}
	
	private void initKeyProperty(Protocol protocol) {
		if (potentiallyHasKeyProperty()) {
			ensureKeyProperty(protocol);
			_keyMapping = createKeyMapping();
		}
	}

	private void ensureKeyProperty(Protocol protocol) {
		if (isInherited()) {
			ensureConsistentKeyProperty(protocol);
		}

		String keyPropertyName = getKeyPropertyConfigurationName();
		if (keyPropertyName != null) {
			if (_elementDescriptor == null) {
				error(protocol, "A key property cannot be declared on a property that has no configuration type.");
				return;
			}
			PropertyDescriptorImpl newKeyProperty = _elementDescriptor.getProperty(keyPropertyName);
			if (newKeyProperty == null) {
				error(protocol, "Key property '" + keyPropertyName + "' does not exist in element type '"
					+ getElementType().getName() + "'.");
			} else {
				setKeyProperty(newKeyProperty);
			}
		} else if (kind() == PropertyKind.MAP) {
			error(protocol, "A property of kind '" + PropertyKind.MAP + "' needs a key property.");
		}
	}

	private void ensureConsistentKeyProperty(Protocol protocol) {
		PropertyDescriptorImpl[] superProperties = getSuperProperties();
		PropertyDescriptorImpl firstSuperProperty = superProperties[0];
		PropertyDescriptor firstSuperKeyProperty = firstSuperProperty.getKeyProperty();

		for (int n = 1, cnt = superProperties.length; n < cnt; n++) {
			PropertyDescriptorImpl inheritedProperty = getSuperProperties()[n];

			// check that different super property have the "same" key property
			PropertyDescriptor inheritedKeyProperty = inheritedProperty.getKeyProperty();
			if (firstSuperKeyProperty == null) {
				if (inheritedKeyProperty != null) {
					error(protocol, "Key property mismatch of inherited property '"
						+ inheritedProperty + "': No key property was defined in '" + firstSuperProperty + "'.");
				}
			} else {
				if (inheritedKeyProperty == null) {
					error(protocol, "Key property mismatch of inherited property '"
						+ inheritedProperty + "': A key property was defined in '" + firstSuperProperty + "'.");
				} else {
					// both given, check same identifier
					if (inheritedKeyProperty.identifier() != firstSuperKeyProperty.identifier()) {
						error(protocol, "Key property mismatch of inherited property '"
							+ inheritedProperty + "': Does not refer to the same key property as '"
							+ firstSuperProperty + "'.");
					}
				}
			}
		}

		String localKeyPropName = getKeyPropertyConfigurationName();
		if (!Utils.equals(localKeyPropName, firstSuperProperty.getKeyPropertyConfigurationName())) {
			error(protocol, "Key property mismatch of inherited property '"
					+ firstSuperProperty + "': A different key property was defined: '"
					+ firstSuperProperty.getKeyPropertyConfigurationName() + "' vs. '" + localKeyPropName + "'.");
		}
	}

	void initElementDescriptor(Protocol protocol, Class<?> valueType) {
		if (valueType == null) {
			error(protocol, "No element type can be determined.");
			return;
		}

		Class<?> declaredConfig;
		if (isInstanceValued()) {
			if (ConfiguredInstance.class.isAssignableFrom(_instanceType)) {
				declaredConfig = JavaTypeUtil.getTypeBound(_instanceType, ConfiguredInstance.class, 0);
			} else {
				declaredConfig = PolymorphicConfiguration.class;
			}
		} else {
			declaredConfig = valueType;
		}
		
		
		Class<?> defaultConfig = declaredConfig;

		Class<?> implClassDefault = annotatedImplementationClassDefault();
		if (implClassDefault == null) {
			implClassDefault = _instanceType;
		}
		if (implClassDefault != null && implClassDefault != Object.class && !implClassDefault.isInterface()) {
			try {
				Class<?> instanceConfig =
					DefaultConfigConstructorScheme.getFactory(implClassDefault).getConfigurationInterface();
				if (defaultConfig.isAssignableFrom(instanceConfig)) {
					// The configuration of the implementation class is a specialization of the
					// property-declared configuration.
					defaultConfig = instanceConfig;
				}
			} catch (ConfigurationException ex) {
				error(protocol, "Cannot resolve configuration interface for implementation class '"
					+ implClassDefault + "'.", ex);
			}
		}
		
		Class<?> declaredImlClass = _instanceType;
		if (declaredImlClass != null && declaredImlClass != Object.class && !declaredImlClass.isInterface()) {
			try {
				Class<?> instanceConfig =
					DefaultConfigConstructorScheme.getFactory(declaredImlClass).getConfigurationInterface();
				if (declaredConfig.isAssignableFrom(instanceConfig)) {
					// The configuration of the implementation class is a specialization of the
					// property-declared configuration.
					declaredConfig = instanceConfig;
				}
			} catch (ConfigurationException ex) {
				error(protocol, "Cannot resolve configuration interface for implementation class '"
					+ declaredImlClass + "'.", ex);
			}
		}

		if (isGuaranteedUnparsable(declaredConfig)) {
			String message = "Parsing the property will always fail, as it stores neither a primitive value,"
				+ "nor a ConfigItem, nor is it annotated with @InstanceFormat."
				+ " Possible causes: Missing '@InstanceFormat' annotation,"
				+ " missing 'extends ConfigurationItem', wrong property type, etc.";
			error(protocol, message, new RuntimeException(message));
			return;
		}
		if (!isConfigType(declaredConfig)) {
			return;
		}

		_elementDescriptor =
			(ConfigurationDescriptorImpl) TypedConfiguration.getConfigurationDescriptor(declaredConfig);

		ConfigurationDescriptorImpl defaultDescriptor =
			(ConfigurationDescriptorImpl) TypedConfiguration.getConfigurationDescriptor(defaultConfig);

		if (PolymorphicConfiguration.class.isAssignableFrom(defaultConfig)) {
			PropertyDescriptorImpl implClassProperty = implClassProperty(defaultDescriptor);
			try {
				DefaultSpec defaultSpec = implClassProperty.getDefaultSpec();
				Class<?> defaultImplementationClass = defaultSpec == null ? null :
					(Class<?>) defaultSpec.getDefaultValue(implClassProperty);

				if (implClassDefault != null
					&& (defaultImplementationClass == null || (defaultImplementationClass != implClassDefault
						&& defaultImplementationClass.isAssignableFrom(implClassDefault)))) {

					// The property-defined default implementation class is a true
					// specialization of the descriptor-defined one. A specialized descriptor is
					// required.

					defaultDescriptor = createCustomDescriptor(protocol, defaultDescriptor, implClassDefault);
				}
			} catch (ConfigurationException ex) {
				error(protocol, "Cannot resolve default implementation class from content descriptor of property '"
					+ getPropertyName() + "'.", ex);
			}
		} else {
			if (implClassDefault != null) {
				defaultDescriptor =
					(ConfigurationDescriptorImpl) TypedConfiguration.getConfigurationDescriptor(implClassDefault);
			}
		}

		_defaultDescriptor = defaultDescriptor;
	}

	private boolean isConfigType(Class<?> type) {
		return ConfigurationItem.class.isAssignableFrom(type) || Annotation.class.isAssignableFrom(type);
	}

	private boolean isGuaranteedUnparsable(Class<?> elementConfigType) {
		if (elementConfigType.isInterface()) {
			return false;
		}
		if (kind() == PropertyKind.DERIVED) {
			return false;
		}
		if (isAbstract()) {
			return false;
		}
		if (BuiltInFormats.isPrimitive(elementConfigType)) {
			return false;
		}
		return true;
	}

	/**
	 * Creates a {@link ConfigurationDescriptor} with the default for the "implementation class"
	 * property set to {@link #_instanceType}.
	 */
	private ConfigurationDescriptorImpl createCustomDescriptor(Protocol protocol,
			ConfigurationDescriptorImpl contentDescriptor, Class<?> implClassDefault) {
		ConfigurationDescriptorBuilder builder = createConfigDescriptorBuilder(protocol, contentDescriptor);
		builder.build();
		setImplementationClassDefault(builder, implClassDefault);
		ConfigurationDescriptorImpl descriptor = builder.getDescriptor();
		descriptor.resolve(protocol);
		descriptor.freeze(protocol);
		return descriptor;
	}

	private ConfigurationDescriptorBuilder createConfigDescriptorBuilder(Protocol protocol,
			ConfigurationDescriptorImpl contentDescriptor) {
		Class<?> configInterface = contentDescriptor.getConfigurationInterface();
		ConfigurationDescriptorImpl[] superDescriptors = new ConfigurationDescriptorImpl[] { contentDescriptor };
		return new ConfigurationDescriptorBuilder(protocol, configInterface, superDescriptors);
	}

	private void setImplementationClassDefault(ConfigurationDescriptorBuilder builder, Class<?> implClassDefault) {
		PropertyDescriptorImpl property = implClassProperty(builder.getDescriptor());
		property.setAnnotatedDefaultSpec(new LiteralDefault(implClassDefault));
	}

	/**
	 * @param implementationDescriptor
	 *        Must be descriptor of a subclass of {@link PolymorphicConfiguration}
	 */
	private PropertyDescriptorImpl implClassProperty(ConfigurationDescriptorImpl implementationDescriptor) {
		if (!PolymorphicConfiguration.class.isAssignableFrom(implementationDescriptor.getConfigurationInterface())) {
			throw new ConfigurationError(PropertyDescriptorImpl.message(this, "Descriptor '"
				+ implementationDescriptor.getConfigurationInterface().getName()
				+ "' is expected to be a PolymorphicConfiguration."));
		}
		return implementationDescriptor.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
	}

	private void ensureElementName(Protocol protocol, Class<?> configurationInterface) {
		if (getElementName() == null) {
			setElementName(getFallbackElementName());
		}
	}

	String getFallbackElementName() {
		return CodeUtil.singularName(getPropertyName(), PropertyDescriptorImpl.DEFAULT_ELEMENT_NAME);
	}

	@Override
	public PropertyKind kind() {
		return kind;
	}
	
	/**
	 * @see #kind()
	 */
	/*package protected*/ void setKind(PropertyKind kind) {
		this.kind = kind;
	}

	String getElementName() {
		return this.elementName;
	}

	/*package protected*/ void setElementName(String elementName) {
		this.elementName = elementName;
	}

	@Override
	public PropertyDescriptor getKeyProperty() {
		return keyProperty;
	}
	
	/**
	 * @see #getKeyProperty()
	 */
	private void setKeyProperty(PropertyDescriptor keyProperty) {
		this.keyProperty = keyProperty;
	}

	/*package protected*/ void setKeyPropertyConfigurationName(String keyPropertyName) {
		this.keyPropertyConfigurationName = keyPropertyName;
	}
	
	private String getKeyPropertyConfigurationName() {
		return keyPropertyConfigurationName;
	}

	/**
	 * @see #hasLocalContainerAnnotation()
	 * @see #setHasContainerAnnotation(boolean)
	 * @see com.top_logic.basic.config.PropertyDescriptor#hasContainerAnnotation()
	 */
	@Override
	public boolean hasContainerAnnotation() {
		return _hasContainerAnnotation;
	}

	private void setHasContainerAnnotation(boolean hasContainerAnnotation) {
		_hasContainerAnnotation = hasContainerAnnotation;
		if (!hasContainerAnnotation) {
			setHasLocalContainerAnnotation(false);
		}
	}

	/**
	 * Has this property a local {@link Container} annotation? (i.e. a non-inherited one)
	 * 
	 * @see #hasContainerAnnotation()
	 * @see #setHasLocalContainerAnnotation(boolean)
	 */
	public boolean hasLocalContainerAnnotation() {
		return _hasLocalContainerAnnotation;
	}

	private void setHasLocalContainerAnnotation(boolean hasLocalContainerAnnotation) {
		_hasLocalContainerAnnotation = hasLocalContainerAnnotation;
		if (hasLocalContainerAnnotation) {
			setHasContainerAnnotation(true);
		}
	}

	private void acceptAnnotationContainer(Protocol protocol) {
		if (!hasLocalAnnotation(Container.class)) {
			return;
		}
		if (ConfigPart.class.isAssignableFrom(getDescriptor().getConfigurationInterface())) {
			setHasLocalContainerAnnotation(true);
		} else {
			logContainerAnnotationOnNonConfigPart(protocol);
		}
	}

	private void logContainerAnnotationOnNonConfigPart(Protocol protocol) {
		error(protocol, "Annotating a property with @"
			+ Container.class.getSimpleName() + " is only allowed in subinterfaces of "
			+ ConfigPart.class.getSimpleName() + ".");
	}

	private boolean acceptFromSuperPropertiesContainer() {
		boolean result = hasAnySuperContainerAnnotation();
		setHasContainerAnnotation(result);
		return result;
	}

	private boolean hasAnySuperContainerAnnotation() {
		for (PropertyDescriptor superDescriptor : getSuperProperties()) {
			if (superDescriptor.hasContainerAnnotation()) {
				return true;
			}
		}
		return false;
	}

	Initializer buildInitializer(Protocol protocol) {
		if (isDerived()) {
			_derivedDependency = DerivedPropertyInitializer.createAlgorithm(protocol, this);
			return new DerivedPropertyInitializer(this, _derivedDependency);
		}
		if (canHaveDefault() && !isDefaultShared()) {
			return new DefaultValueInitializer(this);
		}
		return null;
	}

	/**
	 * The {@link AlgorithmDependency} to be used to install listeners for updating the value
	 * computed by {@link #computeDerived(ConfigurationItem)}.
	 * 
	 * @see #isDerived()
	 * @see PropertyKind#DERIVED
	 * @see #getAlgorithm()
	 */
	AlgorithmDependency getDerivedDependency() {
		if (_derivedDependency == null) {
			throw new UnsupportedOperationException("Not a derived property: " + this);
		}
		return _derivedDependency;
	}

	@Override
	public boolean canHaveDefault() {
		return canHaveSetter() && !isMandatory();
	}

	@Override
	public boolean canHaveSetter() {
		if (kind() == PropertyKind.DERIVED) {
			return false;
		}
		if (getPropertyName().equals(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)) {
			return false;
		}
		return true;
	}

	/**
	 * Use fully qualified name of the getter method in the configuration interface of the
	 * ConfigurationDescriptor.
	 * 
	 * <p>
	 * If the property is declared in a super interface, the getter name is printed as if the getter
	 * was redeclared in the concrete configuration interface of the {@link ConfigurationDescriptor}
	 * of this {@link PropertyDescriptor}. This is necessary to describe errors that occur e.g.
	 * because of inheriting from multiple sources with conflicting properties without redeclaring
	 * the property.
	 * </p>
	 */
    @Override
    public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getDescriptor().getConfigurationInterface().getName());
		result.append(".");
		appendName(result);
		return result.toString();
    }

    /**
     * Like {@link #toString()} but without the configuration interface name.
     */
    String toStringLocal() {
    	StringBuilder result = new StringBuilder();
    	appendName(result);
    	return result.toString();
    }

	protected abstract void appendName(StringBuilder result);

	void setSubtypeByTag(SubtypesDef subtypesDef) {
		_subtypesDef = subtypesDef;
	}

	private void initSubConfigurationsByTag(Protocol protocol) {
		if (kind == PropertyKind.ITEM || kind == PropertyKind.LIST || kind == PropertyKind.MAP || kind == PropertyKind.ARRAY) {
			_elementNameByDescriptor = new HashMap<>();
			_descriptorByElementName = new LinkedHashMap<>();
			if (_elementDescriptor == null) {
				// Happens e.g. for indexed primitive list properties.
				return;
			}
			SubtypesDef subtypesDef = _subtypesDef;
			if (subtypesDef == null) {
				// No local definition.
				if (ambiguousInheritedSubConfigurations) {
					// Note: The error must only be reported, if no local subtypes definition is
					// found.
					error(protocol, "Inherited ambiguous subtype definitions must be redefined.");
				}
				subtypesDef = SubtypesDef.NONE;
			}

			for (Entry<String, Class<? extends ConfigurationItem>> entry : subtypesDef.getTypeByTag().entrySet()) {
				final String tagName = entry.getKey();

				Class<? extends ConfigurationItem> subConfig = entry.getValue();
				ConfigurationDescriptor elementDescriptor = loadDescriptor(protocol, subConfig);
				if (elementDescriptor == null) {
					error(protocol,
						"Cannot resolve subtype descriptor '" + subConfig.getName() + "' for tag '" + tagName
							+ "'.");
					continue;
				}
				enterSpecialTag(tagName, elementDescriptor);
			}

			if (subtypesDef.isAdjust()) {
				Iterable<Entry<String, ConfigurationDescriptor>> descriptorByTag =
					subtypeByTag(protocol, getInstanceType(), _descriptorByElementName.keySet());
				for (Entry<String, ConfigurationDescriptor> entry : descriptorByTag) {
					String tagName = entry.getKey();
					ConfigurationDescriptor type = entry.getValue();
					enterSpecialTag(tagName, type);
				}
			}

			final String generalTag = getElementName();
			if (!_descriptorByElementName.containsKey(generalTag)) {
				_descriptorByElementName.put(generalTag, _defaultDescriptor);
			}

		}
	}

	private Iterable<Entry<String, ConfigurationDescriptor>> subtypeByTag(Protocol log, Class<?> instanceType,
			Set<String> ignoreTags) {
		TagNameMap subtypes;
		if (instanceType != null) {
			subtypes = ConfigDescriptionResolver.polymorphicSubtypes(log, instanceType);
		} else {
			subtypes = ConfigDescriptionResolver.subtypes(log, getValueDescriptor().getConfigurationInterface());
		}
		return subtypes.apply(log, this, ignoreTags);
	}

	private void enterSpecialTag(final String tagName, ConfigurationDescriptor elementDescriptor) {
		_descriptorByElementName.put(tagName, elementDescriptor);
		_elementNameByDescriptor.put(elementDescriptor, tagName);
	}

	private ConfigurationDescriptor loadDescriptor(Protocol protocol, Class<?> subConfig) {
		ConfigurationDescriptor elementDescriptor;
		try {
			elementDescriptor = ConfigDescriptionResolver.getDescriptor(protocol, subConfig);
		} catch (ConfigurationException ex) {
			error(protocol, "Error resolving configuration descriptor '" + subConfig.getName() + "'.", ex);
			elementDescriptor = null;
		}
		return elementDescriptor;
	}

	@Override
	public ConfigurationDescriptor getValueDescriptor() {
		return _elementDescriptor;
	}

	@Override
	public ConfigurationDescriptor getDefaultDescriptor() {
		return _defaultDescriptor;
	}

	@Override
	public ConfigurationDescriptor getElementDescriptor(String someElementName) {
		return _descriptorByElementName.get(someElementName);
	}

	@Override
	public String getElementName(ConfigurationDescriptor elementDescriptor) {
		final String specialElementName = _elementNameByDescriptor.get(elementDescriptor);
		if (specialElementName != null) {
			return specialElementName;
		}
		return elementName;
	}

	@Override
	public Set<String> getElementNames() {
		switch (kind) {
			case ITEM:
			case ARRAY:
			case LIST:
			case MAP:
				return Collections.unmodifiableSet(_descriptorByElementName.keySet());
			default:
				return Collections.emptySet();
		}
	}

	@Override
	public Mapping<Object, Object> getKeyMapping() {
		return _keyMapping;
	}

	private Mapping<Object, Object> createKeyMapping() {
		if (keyProperty == null) {
			return null;
		}

		if (isInstanceValued()) {
			ConfigurationDescriptorImpl polymorphicConfigDescriptor = (ConfigurationDescriptorImpl) TypedConfiguration
				.getConfigurationDescriptor(PolymorphicConfiguration.class);
			NamedConstant IMPLEMENTATION_CLASS_IDENTIFIER = implClassProperty(polymorphicConfigDescriptor).identifier();

			if (keyProperty.identifier() == IMPLEMENTATION_CLASS_IDENTIFIER) {
				// Instances cannot be cast to ConfiguredInstance.
				return new Mapping<>() {
					@Override
					public Object map(Object input) {
						return input.getClass();
					}
				};
			} else {
				return new Mapping<>() {
					@Override
					public Object map(Object input) {
						return ((ConfiguredInstance<?>) input).getConfig().value(keyProperty);
					}
				};
			}
		} else {
			return new Mapping<>() {
				@Override
				public Object map(Object input) {
					return ((ConfigurationItem) input).value(keyProperty);
				}
			};
		}
	}

	@Override
	public ConfigurationAccess getConfigurationAccess() {
		return isInstanceValued() ? InstanceAccess.INSTANCE : DirectConfigurationAccess.INSTANCE;
	}

	void setEncryped(boolean encryped) {
		_encrypted = encryped;
	}

	private boolean isEncrypted() {
		return _encrypted;
	}

	/**
	 * Whether this {@link PropertyDescriptor} has a local (i.e. non-inherited) explicit default
	 * value.
	 */
	boolean hasLocalExplicitDefault() {
		return _localExplicitDefault;
	}

	private void setLocalExplicitDefault(boolean localExplicitDefault) {
		_localExplicitDefault = localExplicitDefault;
	}

	/**
	 * Installs the given object as default value iff this {@link PropertyDescriptor} has a shared
	 * default.
	 * 
	 * @param inherited
	 *        Whether the default value is exactly for this {@link PropertyDescriptor} or whether it
	 *        is inherited.
	 */
	void setConfiguredDefault(Object defaultValue, boolean inherited) {
		LiteralDefault newDefaultSpec = new LiteralDefault(defaultValue);
		if (!inherited) {
			// handle default as it were annotated on this property.
			setAnnotatedDefaultSpec(newDefaultSpec);
		} else {
			setDefaultSpec(newDefaultSpec);
		}
		computedDefaultValue = defaultValue;
		setHasMandatoryAnnotation(false);
	}

	@Override
	public void checkMandatory(Log protocol, ConfigurationItem config) {
		if (!isMandatory()) {
			return;
		}
		switch (kind) {
			case LIST:
				if (((Collection<?>) config.value(this)).isEmpty()) {
					errorMandatoryNotSet(protocol, config);
				}
				break;
			case MAP:
				if (((Map<?, ?>) config.value(this)).isEmpty()) {
					errorMandatoryNotSet(protocol, config);
				}
				break;
			case ARRAY:
				// Handled as atomic value.
			default:
				checkMandatoryAtomic(protocol, config);
				break;
		}
	}

	private void checkMandatoryAtomic(Log protocol, ConfigurationItem config) {
		if (!config.valueSet(this)) {
			errorMandatoryNotSet(protocol, config);
		}
	}

	private void errorMandatoryNotSet(Log protocol, ConfigurationItem config) {
		String message = "Property is mandatory but not set. Location: " + config.location() + ". Item: " + config;
		error(protocol, message);
	}

	/**
	 * Log an error with added context information to the given {@link Protocol}.
	 */
	protected void error(Log protocol, String message, Throwable ex) {
		protocol.error(message(this, message), ex);
	}

	static String message(PropertyDescriptor property, String message) {
		return "Property '" + property.toString() + "': " + message;
	}

	/**
	 * Log an error with added context information to the given {@link Protocol}.
	 */
	protected void error(Log protocol, String message) {
		error(protocol, message, null);
	}

	/**
	 * For a derived property, compute the current value in the context of the given
	 * {@link ConfigurationItem}.
	 */
	Object computeDerived(ConfigurationItem item) {
		// "raw" means: Without taking care of implicit defaults.
		Object rawValue;
		try {
			rawValue = getAlgorithm().apply(item);
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException("Function of derived property '" + getPropertyName()
				+ "' in '" + getDescriptor().getConfigurationInterface().getName()
				+ "' is called with wrong argument types.", ex);
		}
	
		// Ensures that "at least" the implicit default value is used (instead of null).
		//
		// Example: If a property is of type "int", 0 is returned and not null.
		if (rawValue == null) {
			// Explicit defaults are not allowed on derived properties.
			// This call therefore returns either the implicit default or null.
			Object defaultValue = getDefaultValue();
			checkValue(defaultValue);
			return defaultValue;
		}

		checkValue(rawValue);
		return rawValue;
	}

	/**
	 * Creates a copy of the given array value that should be assigned to or returned from this
	 * property.
	 */
	public static Object copyArray(PropertyDescriptor property, Object arrayValue) {
		if (arrayValue == null) {
			return newArrray(property, 0);
		}
		int length = Array.getLength(arrayValue);
		Object copy = newArrray(property, length);
		System.arraycopy(arrayValue, 0, copy, 0, length);
		return copy;
	}

	static Object newArrray(PropertyDescriptor property, int length) {
		return Array.newInstance(property.getElementType(), length);
	}

	protected PropertyDescriptorImpl firstSuperProperty() {
		return getSuperProperties()[0];
	}

	public static List<?> arrayAsList(Object valueToAdopt) {
		if (valueToAdopt == null) {
			return Collections.emptyList();
		}
		return Arrays.asList((Object[]) valueToAdopt);
	}

	public static Object[] listAsArray(PropertyDescriptor property, Collection<?> values) {
		return values.toArray((Object[]) newArrray(property, 0));
	}

	/**
	 * Get the implementation classes derived by the annotated
	 * {@link ImplementationClassesProvider}.
	 * 
	 * @see ImplClasses
	 */
	public static Set getImplementationClasses(PropertyDescriptor property) {
		ImplClasses localAnnotation = property.getLocalAnnotation(ImplClasses.class);
		if (localAnnotation == null) {
			return null;
		}
		ImplementationClassesProvider implementationClassesProvider;
		try {
			implementationClassesProvider = ConfigUtil.getSingleton(localAnnotation.value());
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		switch (property.kind()) {
			case ITEM:
			case ARRAY:
			case LIST:
			case MAP:
				if (implementationClassesProvider == null) {
					return Collections.emptySet();
				}
				return implementationClassesProvider.getImplementationClasses(property.getValueDescriptor());
			default:
				String message = "No implementation classes for a property of kind '" + property.kind() + "'.";
				throw new AssertionError(message(property, message));
		}
	
	}

}