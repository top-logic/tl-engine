/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.External;
import com.top_logic.basic.config.annotation.Factory;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.container.ConfigPartUtilInternal;

/**
 * Builder for {@link ConfigurationDescriptor}s from configuration interfaces.
 * 
 * @see #ConfigurationDescriptorBuilder(Protocol, Class, ConfigurationDescriptorImpl[])
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public final class ConfigurationDescriptorBuilder {

	/**
	 * {@link MethodImplementation} of a {@link ConfigurationItem} visit method.
	 */
	@FrameworkInternal
	public static final class VisitMethod implements MethodImplementation {
		private final Method visitorMethod;

		public VisitMethod(Method visitorMethod) {
			this.visitorMethod = visitorMethod;
		}
		
		public Method getVisitorMethod() {
			return visitorMethod;
		}

		@Override
		public Object invoke(ReflectiveConfigItem impl, Method method, Object[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			Object visitor = args[0];
			args[0] = impl.getInterface();
			return visitorMethod.invoke(visitor, args);
		}
	}

	private static final String VISIT_PREFIX = "visit";

	/**
	 * Method prefixes that signal boolean property getter methods. 
	 */
	private static final Set<String> BOOLEAN_GETTER_PREFIXES = 
		new HashSet<>(Arrays.asList(new String[] {"is", "should", "has", "can", "must"}));

	private static final String GET_PREFIX = "get";

	/**
	 * Method prefixes that signal property getter methods. 
	 */
	private static final Set<String> GETTER_PREFIXES = 
		CollectionUtil.union(BOOLEAN_GETTER_PREFIXES, new HashSet<>(Arrays.asList(new String[] { GET_PREFIX })));
	
	/**
	 * Method prefixes that signal property setter methods. 
	 */
	private static final Set<String> SETTER_PREFIXES = 
		new HashSet<>(Arrays.asList(new String[] {"set"}));

	/**
	 * Method prefixes that signal property access methods. 
	 */
	private static final HashSet<String> PROPERTY_PREFIXES = CollectionUtil.union(GETTER_PREFIXES, SETTER_PREFIXES);
	
	/**
	 * {@link Pattern} that must match property access methods.
	 */
	public static final Pattern METHOD_NAME_PATTERN;
	static {
		String prefixExpr = StringServices.join(new ArrayList<>(PROPERTY_PREFIXES), "|");
		
		METHOD_NAME_PATTERN =
			Pattern.compile("^(?:(" + prefixExpr + ")(\\p{Upper}.*))|(" + VISIT_PREFIX + "(?:\\p{Upper}.*)?)|(.*)$");
	}

	/**
	 * {@link Matcher#group(int) Matcher group index} that signals the access
	 * method kind (getter or setter) in a match result of
	 * {@link #METHOD_NAME_PATTERN}.
	 */
	static final int PREFIX_GROUP = 1;

	/**
	 * {@link Matcher#group(int) Matcher group index} that defines the property
	 * name in a match result of {@link #METHOD_NAME_PATTERN}.
	 */
	public static final int NAME_GROUP = 2;
	
	/**
	 * Matcher group active for a visit method.
	 */
	public static final int VISIT_GROUP = 3;

	/**
	 * Matcher group active for an unprefixed getter method.
	 */
	public static final int FULL_GROUP = 4;

	private static final Mapping<String, String> TO_LOWER_CASE = new Mapping<>() {
		@Override
		public String map(String input) {
			return input.toLowerCase();
		}
	};

	private static final Set<String> OBJECT_METHODS =
		new HashSet<>(Arrays.asList(new String[] {
			AbstractConfigItem.EQUALS_METHOD_NAME,
			AbstractConfigItem.HASH_CODE_METHOD_NAME,
			AbstractConfigItem.TO_STRING_METHOD_NAME
		}));

	private static final Set<String> INTERNAL_METHODS = 
		new HashSet<>(Arrays.asList(new String[] {
			AbstractConfigItem.DESCRIPTOR_METHOD_NAME, 
			AbstractConfigItem.VALUE_METHOD_NAME, 
			AbstractConfigItem.VALUE_SET_METHOD_NAME,
			AbstractConfigItem.LOCALTION_METHOD_NAME,
			AbstractConfigItem.UPDATE_METHOD_NAME,
			AbstractConfigItem.RESET_METHOD_NAME,
			AbstractConfigItem.ADD_CONFIGURATION_LISTENER_METHOD_NAME,
			AbstractConfigItem.REMOVE_CONFIGURATION_LISTENER_METHOD_NAME,
			AbstractConfigItem.CHECK_METHOD_NAME,
			AbstractConfigItem.UNIMPLEMENTABLE_METHOD_NAME,
			ConfigPartUtilInternal.UPDATE_CONTAINER_METHOD_NAME,
			ConfigPartUtilInternal.CONTAINER_METHOD_NAME
		}));

	private static final Set<String> CONTAINER_METHODS =
		new HashSet<>(Arrays.asList(new String[] {
			ConfigPartUtilInternal.CONTAINER_METHOD_NAME,
			ConfigPartUtilInternal.UPDATE_CONTAINER_METHOD_NAME
		}));

	private final Protocol protocol;

	private final Class<?> configurationInterface;

	private final ConfigurationDescriptorImpl[] superDescriptors;

	private ConfigurationDescriptorImpl descriptor;

	private static final String WORD_END = "(?<=\\p{Lower})(?=\\p{Upper})";

	private static final String ABBREVIATION_END = "(?<=\\p{Upper})(?=\\p{Upper}\\p{Lower})";

	private static final Pattern PROPERTY_NAME_SPLITTER =
		Pattern.compile("(?:" + WORD_END + ")|(?:" + ABBREVIATION_END + ")");

	/**
	 * @param protocol
	 *        The {@link Protocol} to which errors will be reported. Is not allowed to be
	 *        <code>null</code>.
	 * @param configInterface
	 *        The interface for which this is the {@link ConfigurationDescriptor}. Is not allowed to
	 *        be <code>null</code>.
	 * @param superDescriptors
	 *        The list of direct super {@link ConfigurationDescriptor}s. Ordered by their
	 *        declaration order: First declared, first in list. Is not allowed to be
	 *        <code>null</code>. Is not allowed to contain <code>null</code>.
	 */
	ConfigurationDescriptorBuilder(Protocol protocol, Class<?> configInterface,
			ConfigurationDescriptorImpl[] superDescriptors) {
		this.protocol = protocol;
		this.configurationInterface = configInterface;
		this.superDescriptors = superDescriptors;

	}

	/**
	 * The protocol to which errors have to be reported.
	 */
	public Protocol getProtocol() {
		return protocol;
	}

	/**
	 * The type for which the new {@link ConfigurationDescriptor} is responsible.
	 */
	public Class<?> getConfigInterface() {
		return configurationInterface;
	}

	/**
	 * The super-descriptors of the new {@link ConfigurationDescriptor}.
	 */
	public ConfigurationDescriptorImpl[] getSuperDescriptors() {
		return superDescriptors;
	}

	/**
	 * The {@link #build()} result.
	 */
	public ConfigurationDescriptorImpl getDescriptor() {
		return descriptor;
	}

	/**
	 * Builds the {@link #getDescriptor()} from the information given in the constructor.
	 */
	public void build() {
		if (! configurationInterface.isInterface()) {
			protocol.fatal("Configuration interface expected, found class '" + configurationInterface.getName() + "'.");
		}

		if ((configurationInterface.getModifiers() & Modifier.PUBLIC) == 0) {
			protocol.fatal("Configuration interface '" + configurationInterface.getName() + "' is not public.");
		}
		
		descriptor = new ConfigurationDescriptorImpl(this);
		descriptor.fillImplementationClassDefaultFromType(protocol);

		Abstract abstractInterface = configurationInterface.getAnnotation(Abstract.class);
		if (abstractInterface != null) {
			descriptor.setAbstract();
		}

		Method[] methods = configurationInterface.getDeclaredMethods();
		for (int n = 0, cnt = methods.length; n < cnt; n++) {
			Method method = methods[n];
			if (method.isSynthetic()) {
				// Since Java 8, Jacoco adds static methods even to interfaces.
				continue;
			}
			
			if (Modifier.isStatic(method.getModifiers())) {
				// Since Java 8, interfaces may contain static methods. These are not properties.
				continue;
			}
			
			if (method.isDefault()) {
				// Since Java 8, interfaces may contain default methods. These are not properties.
				continue;
			}
			
			if (method.getAnnotation(External.class) != null) {
				// TODO: Add a notation of external methods to the descriptor.
				continue;
			}
			Indexed indexedAnnotation = method.getAnnotation(Indexed.class);
			if (indexedAnnotation != null) {
				String collectionProperty = indexedAnnotation.collection();
				descriptor.addResolverPart(new IndexedGetterSpec(descriptor, method, collectionProperty));
				continue;
			}
			if (method.getAnnotation(Factory.class) != null) {
				// TODO: Add a notation of factory methods to the descriptor.
				// TODO: Make sure that factory interfaces are considered as well.
				continue;
			}
			
			Class<?> declaringClass = method.getDeclaringClass();
			if (declaringClass == Object.class) {
				continue;
			}
			String methodName = method.getName();
			if (declaringClass == Annotation.class && !"annotationType".equals(methodName)) {
				continue;
			}
			if (declaringClass == ConfigPart.class) {
				continue;
			}
			
			if (declaringClass == ConfigurationItem.class && INTERNAL_METHODS.contains(methodName)) {
				continue;
			}
			
			if (OBJECT_METHODS.contains(methodName)) {
				continue;
			}

			if (CONTAINER_METHODS.contains(methodName)) {
				error("Overriding method '" + methodName + "' is not allowed.");
			}
			
			Matcher matcher = METHOD_NAME_PATTERN.matcher(methodName);
			if (matcher.matches()) {
				if (matcher.group(VISIT_GROUP) != null) {
					handleVisitMethod(method);
				} else {
					String methodPrefix = matcher.group(PREFIX_GROUP);
					String propertyName;
					if (methodPrefix == null) {
						assert matcher.group(FULL_GROUP) != null;

						methodPrefix = GET_PREFIX;
						propertyName = matcher.group(FULL_GROUP);
					} else {
						propertyName = matcher.group(NAME_GROUP);
					}

					handlePropertyMethod(method, methodPrefix, propertyName);
				}
			} else {
				throw new UnreachableAssertion("Method '" + methodName + "' not covered by method name pattern.");
			}
		}

		descriptor.initialize(protocol);
	}

	private void handlePropertyMethod(Method method, String methodKind, String propertyName) {
		Class<?> returnType = method.getReturnType();
		MethodBasedPropertyDescriptor property = descriptor.initProperty(protocol, propertyName, returnType, method);

		descriptor.addMethod(method, property);
		if (SETTER_PREFIXES.contains(methodKind)) {
			property.setSetter(method);
		} else {
			property.setGetter(method);
		}
	}

	private void handleVisitMethod(Method method) {
		Class<?>[] visitParameterTypes = method.getParameterTypes();
		if (visitParameterTypes.length == 0) {
			error("Visit method '" + method + "' must at least take a visitor argument.");
			return;
		}

		Class<?> visitorInterface = visitParameterTypes[0];
		if (!visitorInterface.isInterface()) {
			error("Visitor '" + visitorInterface + "' must be an interface.");
			return;
		}

		Method[] visitorMethods = visitorInterface.getMethods();
		for (int i = 0; i < visitorMethods.length; i++) {
			final Method visitorMethod = visitorMethods[i];

			if (!visitorMethod.getName().startsWith(VISIT_PREFIX)) {
				error("Methods in visitor interface '" + visitorInterface + "' must start with '"
					+ VISIT_PREFIX + "' prefix.");
			}

			if (visitorMethod.getReturnType() != method.getReturnType()) {
				error("The return type of visitor method '" + visitorMethod + "' must match the return type '"
					+ method.getReturnType() + "' of visit method '" + method + "'.");
			}

			Class<?>[] visitorParameters = visitorMethod.getParameterTypes();
			if (visitorParameters.length != visitParameterTypes.length) {
				error("The number of arguments of visitor method '" + visitorMethod
					+ "' must match the number of arguments of visit method '" + method.getReturnType()
					+ "' of visit method '" + method + "'.");
				continue;
			}

			Class<?> visitedType = visitorParameters[0];
			if (!configurationInterface.isAssignableFrom(visitedType)) {
				error("The visited type '" + visitedType + "' in visitor method '" + visitorMethod
					+ "' must be a subtype of the type declaring the visit method '" + method + "'.");
			}

			for (int j = 1; j < visitorParameters.length; j++) {
				if (visitorParameters[j] != visitParameterTypes[j]) {
					error("Visit parameter type of visitor method '" + visitorMethod
						+ "' must match the parameter types of the visit method '" + method + "'.");
					break;
				}
			}

			// visitMethod in visitedType must dispatch to visitorMethod
			VisitMethod clash =
				descriptor.addVisitImplementation(visitedType, method, new VisitMethod(visitorMethod));
			if (clash != null) {
				error("Ambiguous visit method implementations ('" + visitorMethod + "', '"
					+ clash.getVisitorMethod() + "') for visit method '" + method + "' in visited type '"
					+ visitedType + "'.");
			}

			if (!descriptor.addConcreteType(visitedType)) {
				error("More than one visit method for '" + visitedType + "' in visitor '" + visitorInterface
					+ "'.");
			}
		}
	}

	@Override
	public String toString() {
		return ConfigurationDescriptorBuilder.class.getName() + "(" + configurationInterface.getName() + ")";
	}

	static String getDefaultConfigurationName(String propertyName) {
		List<String> camelCaseParts = Arrays.asList(PROPERTY_NAME_SPLITTER.split(propertyName));
		List<String> lowerCaseParts = Mappings.map(TO_LOWER_CASE, camelCaseParts);
		return StringServices.join(lowerCaseParts, "-");
	}

	private void error(String message) {
		error(message, null);
	}

	private void error(String message, Throwable ex) {
		protocol.error(getErrorPrefix(configurationInterface) + message, ex);
	}

	static String getErrorPrefix(Class<?> configurationInterface) {
		return "Invalid configuration interface '" + configurationInterface.getName() + "': ";
	}

}