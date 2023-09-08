/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.ConfigurationDescriptorBuilder.VisitMethod;
import com.top_logic.basic.config.internal.ItemFactory;
import com.top_logic.basic.config.internal.gen.ConfigurationDescriptorSPI;

/**
 * Reflective type description of a {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class ConfigurationDescriptorImpl extends AbstractConfigurationDescriptor
		implements ConfigurationDescriptorSPI {
	
	private static final String INTERNAL_PROPERTY_NAME_IMPLEMENTATION_CLASS = "ImplementationClass";

	private static final MethodBasedPropertyDescriptor[] NO_PROPERTIES = {};

	private final HashMap<String, MethodBasedPropertyDescriptor> propertiesByInternalName = new LinkedHashMap<>();

	private final Map<Method, MethodImplementation> implementationByMethod = new HashMap<>();

	private final Map<Method, PropertyDescriptorImpl> propertiesByMethod = new HashMap<>();
	
	private final Map<String, PropertyDescriptorImpl> propertiesByConfigName = new HashMap<>();

	private Map<Class<?>, Map<Method, VisitMethod>> visitImplementationsByType = new HashMap<>();

	private final ConfigurationDescriptorImpl[] superDescriptors;

	private Set<Class<?>> concreteTypes;

	private volatile ItemFactory _factory;

	/**
	 * Creates a {@link ConfigurationDescriptorImpl}.
	 * <p>
	 * TODO What is the state this object when this call returns? In particular: Is it fully
	 * initialized?
	 * </p>
	 */
	ConfigurationDescriptorImpl(ConfigurationDescriptorBuilder builder) {
		super(builder.getConfigInterface());
		this.superDescriptors = builder.getSuperDescriptors();
		
		Map<String, Set<PropertyDescriptorImpl>> inheritedPropertiesByInternalName =
			new LinkedHashMap<>();
		PropertyDescriptorImpl defaultContainer = null;
		PropertyDescriptorImpl id = null;
		for (int n = 0, cnt = superDescriptors.length; n < cnt; n++) {
			ConfigurationDescriptorImpl superDescriptor = superDescriptors[n];
			
			MultiMaps.add(inheritedPropertiesByInternalName, superDescriptor.propertiesByInternalName);

			PropertyDescriptorImpl container = superDescriptor.getDefaultContainer();
			if (container != null) {
				if (defaultContainer != null && !defaultContainer.getPropertyName().equals(container.getPropertyName())) {
					builder.getProtocol().error(
						"Ambiguous default container in '" + toString() + "': " + defaultContainer + " vs. "
							+ container);
				} else {
					defaultContainer = container;
				}
			}
			PropertyDescriptorImpl superId = superDescriptor.getIdProperty();
			if (superId != null) {
				if (id != null && !id.getPropertyName().equals(superId.getPropertyName())) {
					builder.getProtocol().error(
						"Ambiguous default container in '" + toString() + "': " + id + " vs. " + superId);
				} else {
					id = superId;
				}
			}
		}
		
		for (Iterator<Entry<String, Set<PropertyDescriptorImpl>>> it =
			inheritedPropertiesByInternalName.entrySet().iterator(); it.hasNext();) {
			Entry<String, Set<PropertyDescriptorImpl>> entry = it.next();
			
			String propertyName = entry.getKey();
			Set<PropertyDescriptorImpl> superProperties = entry.getValue();
			MethodBasedPropertyDescriptor inheritedProperty = new MethodBasedPropertyDescriptor(builder.getProtocol(), this,
				propertyName, superProperties.toArray(NO_PROPERTIES));
			
			propertiesByInternalName.put(propertyName, inheritedProperty);
		}
		
		if (defaultContainer != null) {
			// Adjust to local property.
			initDefaultContainer(propertiesByInternalName.get(defaultContainer.internalName()));
		}

		if (id != null) {
			// Adjust to local property.
			initIdProperty(id.getDescriptor().getIdScope(), propertiesByInternalName.get(id.internalName()));
		}

		for (int n = 0, cnt = superDescriptors.length; n < cnt; n++) {
			ConfigurationDescriptorImpl superDescriptor = superDescriptors[n];
			implementationByMethod.putAll(superDescriptor.getMethodImplementations());

			// Map inherited implementations to own properties.
			for (Iterator<Entry<Method, PropertyDescriptorImpl>> it =
				superDescriptor.propertiesByMethod.entrySet().iterator(); it.hasNext();) {
				Entry<Method, PropertyDescriptorImpl> entry = it.next();
				
				addMethod(entry.getKey(), internalGetProperty(entry.getValue().internalName()));
			}
		}
		
		// Override inherited visit methods with implementations for the own
		// configuration interface.
		installVisitImplementations(getConfigurationInterface(), superDescriptors);
	}

	private void installVisitImplementations(Class<?> configurationInterface,
			ConfigurationDescriptorImpl[] superDescriptors) {
		for (int n = 0, cnt = superDescriptors.length; n < cnt; n++) {
			ConfigurationDescriptorImpl superDescriptor = superDescriptors[n];
			
			// Recursion.
			installVisitImplementations(configurationInterface, superDescriptor.superDescriptors);
			
			// All visit method implementations.
			implementationByMethod.putAll(superDescriptor.getVisitImplementations(configurationInterface));
		}
	}

	@Override
	public Collection<Method> getDeclaredVisitMethods() {
		if (visitImplementationsByType.isEmpty()) {
			return Collections.emptyList();
		}

		HashSet<Method> result = new HashSet<>();
		for (Map<Method, VisitMethod> map : visitImplementationsByType.values()) {
			result.addAll(map.keySet());
		}
		return result;
	}

	@Override
	public Map<Method, VisitMethod> getVisitImplementations(Class<?> type) {
		Map<Method, VisitMethod> result = visitImplementationsByType.get(type);
		if (result == null) {
			return Collections.emptyMap();
		}
		return result;
	}

	@Override
	public ConfigurationDescriptorImpl[] getSuperDescriptors() {
		return superDescriptors;
	}
	
	/*package protected*/ void addImplementation(Method method, MethodImplementation methodImplementation) {
		checkNotFrozen();
		implementationByMethod.put(method, methodImplementation);
	}

	@Override
	public Collection<? extends MethodBasedPropertyDescriptor> getProperties() {
		return propertiesByInternalName.values();
	}

	@Override
	public MethodBasedPropertyDescriptor[] getPropertiesOrdered() {
		return sortByName(getProperties(), MethodBasedPropertyDescriptor.class);
	}

	/*package protected*/ void addMethod(Method method, PropertyDescriptorImpl property) {
		checkNotFrozen();
		propertiesByMethod.put(method, property);
	}

	@Override
	protected Map<Method, MethodImplementation> getMethodImplementations() {
		return implementationByMethod;
	}
	
	@Override
	protected Map<Method, PropertyDescriptorImpl> getPropertiesByMethod() {
		return propertiesByMethod;
	}

	@Override
	public PropertyDescriptorImpl getProperty(String name) {
		return internalGetPropertyByConfigurationName(name);
	}
	
	private PropertyDescriptorImpl internalGetPropertyByConfigurationName(String configurationName) {
		return propertiesByConfigName.get(configurationName);
	}
	
	/* package protected */ MethodBasedPropertyDescriptor initProperty(Protocol protocol, String propertyName,
			Class<?> propertyType, AnnotatedElement annotations) {
		MethodBasedPropertyDescriptor result = internalGetProperty(propertyName);
		if (result == null) {
			checkNotFrozen();
			
			result = new MethodBasedPropertyDescriptor(protocol, this, propertyName, NO_PROPERTIES);
			propertiesByInternalName.put(propertyName, result);
		}
		initPropertyAnnotations(protocol, result, propertyType, annotations);
		return result;
	}

	private MethodBasedPropertyDescriptor internalGetProperty(String propertyName) {
		return propertiesByInternalName.get(propertyName);
	}

	void fillImplementationClassDefaultFromType(Protocol protocol) {
		if (PolymorphicConfiguration.class.isAssignableFrom(getConfigurationInterface())
			&& getConfigurationInterface() != PolymorphicConfiguration.class) {

			Class<?> defaultImplementationClass = lookupImplementationClassDefault();
			if (defaultImplementationClass != null) {
				PropertyDescriptorImpl implementationClassProperty =
					internalGetProperty(INTERNAL_PROPERTY_NAME_IMPLEMENTATION_CLASS);

				DefaultSpec inheritedDefault = implementationClassProperty.getDefaultSpec();
				if (inheritedDefault == null || 
					(!(inheritedDefault instanceof LiteralDefault)) ||
					!defaultImplementationClass.isAssignableFrom(
						(Class<?>) ((LiteralDefault) inheritedDefault).getValue())) {
					implementationClassProperty.setImplicitDefault(new LiteralDefault(defaultImplementationClass));
				}
			}
		}
	}

	private Class<?> lookupImplementationClassDefault() {
		Type[] superInterfaces = getConfigurationInterface().getGenericInterfaces();
		Map<TypeVariable<?>, Type> bindings = new HashMap<>();
		addTypeBindings(bindings, superInterfaces);
		return resolveImplementationClass(bindings);
	}

	static Class<?> resolveImplementationClass(Map<TypeVariable<?>, Type> bindings) {
		return resolveBound(bindings, PolymorphicConfiguration.class.getTypeParameters()[0]);
	}

	static Class<?> resolveBound(Map<TypeVariable<?>, Type> bindings, TypeVariable<?> typeVariable) {
		List<Class<?>> bounds = resolveBounds(bindings, typeVariable);
		if (bounds.isEmpty()) {
			return Object.class;
		}
		return bounds.get(0);
	}

	private static void addTypeBindings(Map<TypeVariable<?>, Type> bindings, Type[] superInterfaces) {
		for (Type superInterface : superInterfaces) {
			addTypeBinding(bindings, superInterface);
		}
	}

	static void addTypeBinding(Map<TypeVariable<?>, Type> bindings, Type interfaceType) {
		if (interfaceType instanceof ParameterizedType) {
			ParameterizedType parameterizedSuperInterface = (ParameterizedType) interfaceType;
			computeBinding(bindings, parameterizedSuperInterface);
		}
		addTypeBindings(bindings, rawType(interfaceType).getGenericInterfaces());
	}

	static Class<?> rawType(Type type) {
		while (!(type instanceof Class<?>)) {
			if (type instanceof ParameterizedType) {
				type = ((ParameterizedType) type).getRawType();
			}
			else if (type instanceof WildcardType) {
				Type[] lowerBounds = ((WildcardType) type).getUpperBounds();
				if (lowerBounds.length == 0) {
					return Object.class;
				} else {
					type = lowerBounds[0];
				}
			}
			else if (type instanceof TypeVariable) {
				Type[] bounds = ((TypeVariable<?>) type).getBounds();
				if (bounds.length == 0) {
					return Object.class;
				} else {
					type = bounds[0];
				}
			}
			else if (type instanceof GenericArrayType) {
				return Array.newInstance(rawType(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
			}
			else {
				throw new IllegalArgumentException("Cannot determine raw type for: " + type);
			}
		}
		return (Class<?>) type;
	}

	private static List<Class<?>> resolveBounds(Map<TypeVariable<?>, Type> bindings, TypeVariable<?> typeVariable) {
		List<Class<?>> bounds = new ArrayList<>();
		for (Type bound : typeVariable.getBounds()) {
			bounds.add(rawType(bound));
		}

		while (true) {
			Type type = bindings.get(typeVariable);
			if (type instanceof TypeVariable<?>) {
				TypeVariable<?> boundVar = (TypeVariable<?>) type;
				join(bounds, boundVar.getBounds());
				typeVariable = boundVar;
			} else if (type instanceof WildcardType) {
				join(bounds, ((WildcardType) type).getUpperBounds());
				break;
			} else if (type == null) {
				break;
			} else {
				join(bounds, rawType(type));
				break;
			}
		}

		return bounds;
	}

	private static void join(List<Class<?>> bounds, Type[] specializedBounds) {
		for (Type specializedBound : specializedBounds) {
			join(bounds, rawType(specializedBound));
		}
	}

	private static void join(List<Class<?>> bounds, Class<?> specializedBound) {
		for (Iterator<Class<?>> it = bounds.iterator(); it.hasNext();) {
			Class<?> bound = it.next();
			if (specializedBound.isAssignableFrom(bound)) {
				// No new information. Note: This covers the case where the existing bound and the
				// specialized bound are equal. Therefore, this test must be the first one to
				// prevent removing both, the existing bound and the specialized bound.
				return;
			}
			if (bound.isAssignableFrom(specializedBound)) {
				it.remove();
			}
		}
		bounds.add(specializedBound);
	}

	private static void computeBinding(Map<TypeVariable<?>, Type> bindings, ParameterizedType parameterizedType) {
		Type rawType = parameterizedType.getRawType();
		Type[] typeArguments = parameterizedType.getActualTypeArguments();

		Class<?> baseType = rawType(rawType);
		TypeVariable<?>[] typeParameters = baseType.getTypeParameters();

		for (int n = 0, cnt = typeArguments.length; n < cnt; n++) {
			bindings.put(typeParameters[n], typeArguments[n]);
		}

		Type[] interfaces = baseType.getGenericInterfaces();
		for (Type genericInterface : interfaces) {
			if (genericInterface instanceof ParameterizedType) {
				computeBinding(bindings, (ParameterizedType) genericInterface);
			}
		}
	}

	@Override
	public ItemFactory factory() {
		ItemFactory result = _factory;
		if (result == null) {
			result = ItemFactory.createFactory(this);
			_factory = result;
		}
		return result;
	}

	void initialize(Protocol protocol) {
		for (PropertyDescriptorImpl property : propertiesByInternalName.values()) {
			property.resolveLocalProperties(protocol);
		}

		implementationByMethod.putAll(getVisitImplementations(getConfigurationInterface()));

		// Index properties by configuration name.
		for (PropertyDescriptorImpl property : propertiesByInternalName.values()) {
			this.propertiesByConfigName.put(property.getPropertyName(), property);
		}
	}

	VisitMethod addVisitImplementation(Class<?> visitedType, Method visitMethod,
			VisitMethod visitMethodImplementation) {
		return makeVisitImplementationMap(visitedType).put(visitMethod, visitMethodImplementation);
	}

	private Map<Method, VisitMethod> makeVisitImplementationMap(Class<?> visitedType) {
		Map<Method, VisitMethod> visitImplementationByMethod = this.visitImplementationsByType.get(visitedType);
		if (visitImplementationByMethod == null) {
			visitImplementationByMethod = new HashMap<>();
			this.visitImplementationsByType.put(visitedType, visitImplementationByMethod);
		}
		return visitImplementationByMethod;
	}

	boolean addConcreteType(Class<?> concreteType) {
		checkNotFrozen();
		if (this.concreteTypes == null) {
			this.concreteTypes = new HashSet<>();
		}
		return this.concreteTypes.add(concreteType);
	}

	@Override
	public Set<Class<?>> getConcreteTypes() {
		return concreteTypes;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + '(' + getConfigurationInterface().getName() + ')';
	}
}