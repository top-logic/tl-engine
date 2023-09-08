/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Id;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.container.ConfigPartUtilInternal;

/**
 * Base class for {@link ConfigurationDescriptor} implementations.
 * 
 * <p>
 * The implementation assumes that {@link PropertyDescriptor} is implemented as
 * {@link PropertyDescriptorImpl}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class AbstractConfigurationDescriptor implements ConfigurationDescriptor {

	private static final Class<?>[] ITEM_CONSTRUCTOR_SIGNATURE = new Class<?>[] { InvocationHandler.class };

	/** @see #getConfigurationInterface() */
	private final Class<?> _configInterface;

	/** @see #isAbstract() */
	private boolean _abstract;

	/** Additional {@link ResolverPart}s. */
	private List<ResolverPart> _resolverParts = new ArrayList<>();

	/** @see #getDefaultContainer() */
	private PropertyDescriptorImpl _defaultContainer;

	/** @see #getIdProperty() */
	private PropertyDescriptorImpl _id;

	/** @see #getIdScope() */
	private Class<?> _idScope;

	/** @see #isFrozen() */
	private boolean frozen;

	/** An additional {@link Initializer} for created {@link ConfigurationItem}. */
	private Initializer _initializer;

	private Constructor<?> _itemConstructor;

	/**
	 * Creates a new {@link AbstractConfigurationDescriptor}.
	 * 
	 * @param configInterface
	 *        Value of {@link #getConfigurationInterface()}.
	 */
	public AbstractConfigurationDescriptor(Class<?> configInterface) {
		_configInterface = configInterface;
	}

	@Override
	public Class<?> getConfigurationInterface() {
		return _configInterface;
	}

	@Override
	public abstract Collection<? extends PropertyDescriptorImpl> getProperties();

	@Override
	public void check(Log protocol, ConfigurationItem item) {
		for (PropertyDescriptor property : getProperties()) {
			property.checkMandatory(protocol, item);
			for (ConfigurationItem childConfig : ConfigUtil.getChildConfigs(item, property)) {
				childConfig.check(protocol);
			}
		}
	}

	@Override
	public boolean isAbstract() {
		return _abstract;
	}

	/**
	 * Marks this {@link ConfigurationDescriptor} as {@link #isAbstract() abstract}.
	 */
	public void setAbstract() {
		_abstract = true;
	}

	/**
	 * Creates an array of the given component type containing the given properties sorted by
	 * {@link PropertyDescriptor#BY_NAME_COMPARATOR name}.
	 * 
	 * @param properties
	 *        The properties to sort.
	 * @param componentType
	 *        {@link Class#getComponentType() Component type} of the result array.
	 */
	protected static <T extends PropertyDescriptor> T[] sortByName(Collection<? extends T> properties,
			Class<T> componentType) {
		T[] tmp = ArrayUtil.toArrayTyped(properties, componentType);
		Arrays.sort(tmp, PropertyDescriptor.BY_NAME_COMPARATOR);
		return tmp;
	}

	/**
	 * Adds an additional {@link ResolverPart} to this {@link AbstractConfigurationDescriptor} to
	 * apply during {@link #resolve(Protocol)}.
	 */
	public void addResolverPart(ResolverPart part) {
		_resolverParts.add(part);
	}

	private void applyResolverParts(Protocol protocol) {
		for (ResolverPart part : _resolverParts) {
			part.resolve(protocol, this);
		}
		_resolverParts = null;
	}

	void resolve(Protocol protocol) {
		if (protocol.hasErrors()) {
			return;
		}
		for (PropertyDescriptorImpl property : getProperties()) {
			property.resolveExternalRelations(protocol);
		}

		applyResolverParts(protocol);

		for (PropertyDescriptorImpl property : getProperties()) {
			property.resolveDefault(protocol);
		}

		checkAbstract(protocol);

		checkDefaultContainer(protocol);
	}

	private void checkDefaultContainer(Protocol protocol) {
		PropertyDescriptor defaultContainer = getDefaultContainer();
		if (defaultContainer == null) {
			return;
		}
		switch (defaultContainer.kind()) {
			case LIST:
			case ARRAY:
			case MAP:
			case ITEM: {
				for (String name : defaultContainer.getElementNames()) {
					PropertyDescriptor clash = getProperty(name);
					if (clash != null) {
						StringBuilder errorClash = new StringBuilder();
						errorClash.append("Ambiguous content tag name '");
						errorClash.append(name);
						errorClash.append("': May either represent the property ");
						errorClash.append(clash);
						errorClash.append(", or a content element of the default container ");
						errorClash.append(defaultContainer);
						protocol.error(errorClash.toString());
					}
				}
				break;
			}
			default: {
				protocol.error("Only item, array, list or map properties may by used as default containers: "
					+ defaultContainer);
			}
		}
	}

	void checkAbstract(Protocol protocol) {
		if (isAbstract()) {
			return;
		}
		for (PropertyDescriptor property : getProperties()) {
			if (property.isAbstract()) {
				protocol.error("Configuration interface '" + getConfigurationInterface().getName()
					+ "' must be declared abstract, since it has an abstract property '"
					+ property.getPropertyName() + "'.");
				setAbstract();
			}
		}
	}

	@Override
	public PropertyDescriptorImpl getDefaultContainer() {
		return _defaultContainer;
	}

	/**
	 * Setter for {@link #getDefaultContainer()}.
	 */
	public void initDefaultContainer(PropertyDescriptorImpl defaultContainer) {
		_defaultContainer = defaultContainer;
	}

	@Override
	public PropertyDescriptorImpl getIdProperty() {
		return _id;
	}

	@Override
	public Class<?> getIdScope() {
		return _idScope;
	}

	/**
	 * Setter for {@link #getIdProperty()} and {@link #getIdScope()}.
	 * 
	 * <p>
	 * Must only be called once.
	 * </p>
	 * 
	 * @param idScope
	 *        See {@link #getIdScope()}.
	 * @param idProperty
	 *        See {@link #getIdProperty()}.
	 */
	public void initIdProperty(Class<?> idScope, PropertyDescriptorImpl idProperty) {
		assert _id == null : "Re-initialization of ID property.";
		_id = idProperty;
		_idScope = idScope;
	}

	void initPropertyAnnotations(Protocol protocol, PropertyDescriptorImpl property, Class<?> propertyType,
			AnnotatedElement annotations) {
		AbstractConfigurationDescriptor configDescriptor = property.getDescriptor();
		final Name nameAnnotation = annotations.getAnnotation(Name.class);
		if (nameAnnotation != null) {
			property.setConfigAttributeName(protocol, nameAnnotation.value());
		}
		final Key keyAnnotation = annotations.getAnnotation(Key.class);
		if (keyAnnotation != null) {
			property.setKeyPropertyConfigurationName(keyAnnotation.value());
		}
		final EntryTag elementAnnotation = annotations.getAnnotation(EntryTag.class);
		if (elementAnnotation != null) {
			property.setElementName(elementAnnotation.value());
		}
		final Subtypes subtypesAnnotation = annotations.getAnnotation(Subtypes.class);
		if (subtypesAnnotation != null) {
			property.setSubtypeByTag(buildSubtypeByTag(protocol, property, subtypesAnnotation));
		}
		final DefaultContainer defaultContainerAnnotation = annotations.getAnnotation(DefaultContainer.class);
		if (defaultContainerAnnotation != null) {
			PropertyDescriptor inheritedDefaultDescriptor = configDescriptor.getDefaultContainer();
			if (inheritedDefaultDescriptor != null
				&& !inheritedDefaultDescriptor.getPropertyName().equals(property.getPropertyName())) {
				protocol.error(
					"Ambiguous default container in '" + toString() + "': " + inheritedDefaultDescriptor
						+ " vs. "
						+ property);
			} else {
				configDescriptor.initDefaultContainer(property);
			}
		}
		final Id idAnnotation = annotations.getAnnotation(Id.class);
		if (idAnnotation != null) {
			PropertyDescriptor inheritedId = configDescriptor.getIdProperty();
			if (inheritedId != null && !inheritedId.getPropertyName().equals(property.getPropertyName())) {
				protocol.error(
					"Ambiguous ID in '" + toString() + "': " + inheritedId + " vs. " + property);
			} else {
				if (!(PolymorphicConfiguration.class.isAssignableFrom(configDescriptor.getConfigurationInterface()))) {
					protocol.error(
						"ID properties can only be defined in in '" + PolymorphicConfiguration.class.getSimpleName()
							+ "' interfaces: " + property);
				} else {
					Class<?> scope = idAnnotation.value();
					Class<?> implClass =
						MethodBasedPropertyDescriptor
							.resolveImplementationClass(configDescriptor.getConfigurationInterface());

					if (scope == Void.class) {
						scope = implClass;
					} else if (!scope.isAssignableFrom(implClass)) {
						protocol.error(
							"Invalid ID property '" + property + "': An ID in scope '" + scope.getName()
								+ "' must only be defined in a polymorphic configuration for a subclass of this scope. '"
								+ implClass.getName() + "' is no such subclass.");
					}

					configDescriptor.initIdProperty(scope, property);
				}
			}
		}
		final Encrypted encrypedAnnotation = annotations.getAnnotation(Encrypted.class);
		if (encrypedAnnotation != null) {
			property.setEncryped(true);
		}
		final InstanceFormat instantiationAnnotation = annotations.getAnnotation(InstanceFormat.class);
		if (instantiationAnnotation != null) {
			property.setInstanceFormat(true);
		}
		initFormat(protocol, property, propertyType, annotations);
	}

	private void initFormat(Protocol protocol, PropertyDescriptorImpl property, Class<?> propertyType,
			AnnotatedElement annotations) {
		final Format formatAnnotation = annotations.getAnnotation(Format.class);
		if (formatAnnotation != null) {
			setFormat(protocol, property, formatAnnotation);
		}
		
		if (property.getValueProvider() == null) {
			Format format = propertyType.getAnnotation(Format.class);
			if (format != null) {
				setFormat(protocol, property, format);
			}
		}

		int numBindings = 0;
		final Binding bindingAnnotation = annotations.getAnnotation(Binding.class);
		if (bindingAnnotation != null) {
			numBindings++;
			setBinding(protocol, property, bindingAnnotation);
		}
		final ListBinding listBindingAnnotation = annotations.getAnnotation(ListBinding.class);
		if (listBindingAnnotation != null) {
			numBindings++;
			property.setConfigurationValueBindingFuture(new SimpleListValueBindingFuture(listBindingAnnotation));
		}
		final MapBinding mapBindingAnnotation = annotations.getAnnotation(MapBinding.class);
		if (mapBindingAnnotation != null) {
			numBindings++;
			property.setConfigurationValueBindingFuture(new SimpleMapValueBindingFuture(mapBindingAnnotation));
		}
		if (numBindings > 1) {
			StringBuilder error = new StringBuilder();
			error.append("Property '" + property.toStringLocal()
				+ "': At most one of the value binding annotation is allowed: ");
			error.append(Binding.class.getSimpleName());
			error.append(", or ");
			error.append(ListBinding.class.getSimpleName());
			error.append(", or ");
			error.append(MapBinding.class.getSimpleName());
			protocol.error(error.toString());
			return;
		}
		if (numBindings == 0) {
			bindingFromType(protocol, propertyType, property);
		}
	}

	private void bindingFromType(Protocol protocol, Class<?> propertyType, PropertyDescriptorImpl property) {
		Binding binding = propertyType.getAnnotation(Binding.class);
		if (binding != null) {
			setBinding(protocol, property, binding);
		}
	}

	private void setBinding(Protocol protocol, PropertyDescriptorImpl property, final Binding bindingAnnotation) {
		final Class<? extends ConfigurationValueBinding<?>> bindingClass = bindingAnnotation.value();
		try {
			final ConfigurationValueBinding<?> binding = ConfigUtil.getInstance(bindingClass);
			property.setValueBinding(binding);
		} catch (ConfigurationException ex) {
			protocol.error("Unable to resolve @" + Binding.class.getSimpleName() + " annotation.", ex);
		}
	}

	private void setFormat(Protocol protocol, PropertyDescriptorImpl property, final Format formatAnnotation) {
		final Class<? extends ConfigurationValueProvider<?>> formatClass = formatAnnotation.value();
		try {
			final ConfigurationValueProvider<?> formatter = ConfigUtil.getInstance(formatClass);
			property.setValueProvider(formatter);
		} catch (ConfigurationException ex) {
			protocol.error("Unable to resolve @" + Format.class.getSimpleName() + " annotation.", ex);
		}
	}

	private SubtypesDef buildSubtypeByTag(Protocol protocol, PropertyDescriptor property, Subtypes subtypesAnnotation) {
		HashMap<String, Class<? extends ConfigurationItem>> result =
			new HashMap<>();

		Subtype[] subtypes = subtypesAnnotation.value();
		for (int n = 0, cnt = subtypes.length; n < cnt; n++) {
			Subtype subtype = subtypes[n];
			String tag = subtype.tag();
			Class<? extends ConfigurationItem> subConfig = subtype.type();

			Class<? extends ConfigurationItem> clash = result.put(tag, subConfig);
			if (clash != null) {
				protocol.error("Ambiguous tag name '" + tag + "' in subtypes definition of '" + property + "': "
					+ subConfig.getName() + " vs. " + clash.getName());
			}
		}

		return new SubtypesDef(result, subtypesAnnotation.adjust());
	}

	/**
	 * Prevent future modifications.
	 */
	public void freeze(Protocol protocol) {
		if (protocol.hasErrors()) {
			return;
		}
		internalFreeze(protocol);
		frozen = true;
	}

	/**
	 * Actual implementation of {@link #freeze(Protocol)}.
	 * 
	 * @param protocol
	 *        Protocol to write messages to.
	 */
	protected void internalFreeze(Protocol protocol) {
		_itemConstructor = createItemConstructor();
		initAlgorithms(protocol);
		setInitializer(buildInitializer(protocol));
	}

	private void initAlgorithms(Protocol protocol) {
		for (PropertyDescriptorImpl property : getProperties()) {
			if (property.isDerived()) {
				property.initAlgorithm(protocol);
			}
		}
	}

	private Initializer buildInitializer(Protocol protocol) {
		List<Initializer> initializers = buildInitializers(protocol);
		if (initializers.isEmpty()) {
			return EmptyInitializer.EMPTY_INITIALIZER;
		} else if (initializers.size() == 1) {
			return initializers.get(0);
		} else {
			return new CombinedInitializer(initializers);
		}
	}

	private List<Initializer> buildInitializers(Protocol protocol) {
		List<Initializer> initializers = new ArrayList<>();
		for (PropertyDescriptorImpl property : getProperties()) {
			Initializer initializer = property.buildInitializer(protocol);
			if (initializer != null) {
				initializers.add(initializer);
			}
		}
		return initializers;
	}

	/**
	 * Whether this {@link AbstractConfigurationDescriptor} must not longer be modified.
	 * 
	 * @see #freeze(Protocol)
	 */
	public boolean isFrozen() {
		return frozen;
	}

	void setInitializer(Initializer initializer) {
		_initializer = initializer;
	}

	/**
	 * Initialised the given {@link AbstractConfigItem configuration}.
	 */
	protected void initInstance(AbstractConfigItem item) {
		_initializer.init(item);
	}

	private Constructor<?> createItemConstructor() {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class<?> proxyClass = Proxy.getProxyClass(loader, getInterfacesToImplement());
	
			return proxyClass.getConstructor(ITEM_CONSTRUCTOR_SIGNATURE);
		} catch (SecurityException e) {
			throw new UnreachableAssertion(e);
		} catch (IllegalArgumentException e) {
			throw new UnreachableAssertion(e);
		} catch (NoSuchMethodException e) {
			throw new UnreachableAssertion(e);
		}
	}

	/**
	 * Determines the interfaces that a new instance of this configuration must be implement.
	 * 
	 * <p>
	 * The returned interfaces must not contain duplicates.
	 * </p>
	 * 
	 * @see Proxy#getProxyClass(ClassLoader, Class...)
	 */
	protected Class<?>[] getInterfacesToImplement() {
		Class<?> configInterface = getConfigurationInterface();
		if (ConfigPart.class.isAssignableFrom(configInterface)) {
			return new Class<?>[] { configInterface, ConfigPartUtilInternal.INTERNAL_IMPLEMENTATION_CLASS };
		} else if (ConfigurationItem.class.isAssignableFrom(configInterface)) {
			return new Class<?>[] { configInterface };
		} else {
			return new Class<?>[] { configInterface, ConfigurationItem.class };
		}
	}

	/**
	 * Creates an immutable view to the given {@link ConfigurationItem}.
	 * 
	 * @param configItem
	 *        The {@link ConfigurationItem} to create view for.
	 * 
	 * @see #newInstance(ReflectiveConfigItem)
	 */
	protected ConfigurationItem createView(ConfigurationItem configItem) {
		return newInstance(new ConfigItemView(configItem));
	}

	/**
	 * Creates a new {@link ConfigurationItem} based on the given {@link ReflectiveConfigItem}
	 * implementing all interfaces in {@link #getInterfacesToImplement()}.
	 * 
	 * @param impl
	 *        Actual value holder for the result item.
	 */
	protected ConfigurationItem newInstance(ReflectiveConfigItem impl) {
		if (isAbstract()) {
			throw new IllegalArgumentException(
				"Abstract configuration '" + this + "' in " + impl.location() + " cannot be instantiated.");
		}
		try {
			ConfigurationItem intf = ConfigPartUtilInternal.newInstance(_itemConstructor, impl);
			impl.initInterface(intf);
			return intf;
		} catch (IllegalArgumentException e) {
			throw new UnreachableAssertion("Invalid constructor argument creating instance of '" + this + "'.", e);
		} catch (InstantiationException e) {
			throw new UnreachableAssertion("Cannot create instance of '" + this + "'.", e);
		} catch (IllegalAccessException e) {
			throw new UnreachableAssertion("Illegal access while creating instance of '" + this + "'.", e);
		} catch (InvocationTargetException e) {
			throw new AssertionError("Error while creating instance of '" + this + "'.", e);
		}
	}

	/**
	 * The available {@link MethodImplementation}s by {@link Method} to handle the methods of the
	 * interfaces in {@link #getInterfacesToImplement()}.
	 */
	protected abstract Map<Method, MethodImplementation> getMethodImplementations();

	/**
	 * Mapping from the {@link Method}s in {@link #getInterfacesToImplement()} to the actual
	 * {@link PropertyDescriptor} that represents the "key" method.
	 */
	protected abstract Map<Method, ? extends PropertyDescriptor> getPropertiesByMethod();

	/**
	 * Asserts that this {@link ConfigurationDescriptor} is not frozen.
	 * 
	 * @see #isFrozen()
	 */
	protected void checkNotFrozen() {
		if (isFrozen()) {
			throw new IllegalStateException("Descriptor can no longer be modified.");
		}
	}

}
