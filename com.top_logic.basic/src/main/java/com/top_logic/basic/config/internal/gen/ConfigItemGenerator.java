/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal.gen;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationDescriptorBuilder.VisitMethod;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.container.ConfigPartInternal;
import com.top_logic.basic.config.equal.EqualityByValue;
import com.top_logic.basic.config.internal.gen.PropertyWriter.PropertyInheritance;
import com.top_logic.basic.generate.JavaGenerator;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.type.PrimitiveTypeUtil;

/**
 * Implementation generator for {@link ConfigurationItem} implementations.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigItemGenerator extends JavaGenerator {

	/**
	 * Argument of the generated copy constructor.
	 */
	static final String OTHER_ARG = "other";

	/** For these classes no generation is needed. */
	private static final Set<Class<? extends ConfigurationItem>> NO_GENERATION_ITEMS;
	static {
		HashSet<Class<? extends ConfigurationItem>> ignoreItems = new HashSet<>();
		ignoreItems.add(ConfigBuilder.class);
		ignoreItems.add(ConfigurationItem.class);
		ignoreItems.add(ConfigPart.class);
		ignoreItems.add(ConfigPartInternal.class);
		NO_GENERATION_ITEMS = ignoreItems;
	}

	private static final Class<?>[] NEEDED_CLASSES = new Class[] {
		NamedConstant.class,
		ConfigurationDescriptor.class,
		ConfigurationDescriptorSPI.class,
		TypedConfiguration.class,
	};

	private static final String PROPERTY_INDEX_NAME = "_INDEX";

	private static final String VISITOR_ARG = "v";

	private static final String VISIT_ARGUMENT_ARG = "arg";

	private static final String[] VISIT_PARAMS = { VISITOR_ARG, VISIT_ARGUMENT_ARG };

	private static final String[] VISITOR_ARGS = { "this", VISIT_ARGUMENT_ARG };

	final Class<?> _itemType;

	final private Type _leadingSuperInterface;

	private final Class<?> _leadingSuperInterfaceRaw;

	final private Set<Class<?>> _implementedInterfaces;

	private final Map<TypeVariable<?>, Type> _bindings;

	private String _className;

	private List<PropertyWriter> _properties = new ArrayList<>();

	private final List<String> _imports = new ArrayList<>();

	private boolean _fullSwitches;

	private boolean _noOwnProperties;

	private boolean _needEqualsMethod;

	/**
	 * Creates a {@link ConfigItemGenerator}.
	 *
	 * @param descriptor
	 *        The {@link ConfigurationDescriptor} of the item to implement.
	 */
	public ConfigItemGenerator(ConfigurationDescriptorSPI descriptor) {
		super(implClassPackage(descriptor.getConfigurationInterface()));

		_itemType = descriptor.getConfigurationInterface();
		_className = implClassName(_itemType);
		_bindings = analyzeTypeParameters();
		_leadingSuperInterface = findSuperImplementationInterface(_itemType);
		_leadingSuperInterfaceRaw = rawType(_leadingSuperInterface);
		_implementedInterfaces = findImplementedInterfaces(_itemType, _leadingSuperInterfaceRaw);

		analyze(descriptor);

		for (Class<?> importClass : NEEDED_CLASSES) {
			addImport(importClass);
		}
	}

	void addImport(Class<?> clazz) {
		String importPackage = clazz.getPackage().getName();
		if (packageName().equals(importPackage)) {
			return;
		}
		if (packageName().equals("java.lang")) {
			// No import needed for language classes.
			return;
		}
		_imports.add(toString(clazz));
	}

	private Map<TypeVariable<?>, Type> analyzeTypeParameters() {
		Map<TypeVariable<?>, Type> binding = new HashMap<>();

		TypeVariable<?>[] typeParameters = _itemType.getTypeParameters();
		for (TypeVariable<?> var : typeParameters) {
			// Variables of current type are not bound.
			binding.put(var, var);
		}
		
		analyzeSuperTypes(binding, _itemType);

		return binding;
	}

	private void analyzeSuperTypes(Map<TypeVariable<?>, Type> binding, Class<?> interfaceType) {
		Type[] superInterfaces = interfaceType.getGenericInterfaces();
		for (Type superType : superInterfaces) {
			if (superType instanceof Class<?>) {
				Class<?> rawSuperType = (Class<?>) superType;

				TypeVariable<?>[] superParameters = rawSuperType.getTypeParameters();
				for (int n = 0, cnt = superParameters.length; n < cnt; n++) {
					// Unbound type variables are mapped to "raw type" represented by null.
					binding.put(superParameters[n], null);
				}

				analyzeSuperTypes(binding, rawSuperType);
			} else if (superType instanceof ParameterizedType) {
				ParameterizedType genericSuperType = (ParameterizedType) superType;

				Class<?> rawSuperType = (Class<?>) genericSuperType.getRawType();
				TypeVariable<?>[] superParameters = rawSuperType.getTypeParameters();
				Type[] superBindings = genericSuperType.getActualTypeArguments();
				
				for (int n = 0, cnt = superParameters.length; n < cnt; n++) {
					binding.put(superParameters[n], superBindings[n]);
				}

				analyzeSuperTypes(binding, rawSuperType);
			} else {
				throw new UnsupportedOperationException(
					"Do not expect a super type of a configuation interface to be of type '"
						+ superType.getClass().getName() + "'.");
			}
		}
	}

	private void analyze(ConfigurationDescriptorSPI descriptor) {
		Map<NamedConstant, PropertyDescriptorSPI> inheritedProperties = indexProperties(_leadingSuperInterfaceRaw);

		int index = TypedConfiguration.getConfigurationDescriptor(_leadingSuperInterfaceRaw).getProperties().size();

		int declaredCnt = 0;
		for (PropertyDescriptorSPI property : descriptor.getPropertiesOrdered()) {
			PropertyInheritance inheritance;

			int propertyIndex;
			PropertyDescriptorSPI inheritedProperty = inheritedProperties.get(property.identifier());
			if (inheritedProperty != null) {
				if (getDeclaration(_implementedInterfaces, property) != null) {
					inheritance = PropertyInheritance.REDECLARED;
				} else {
					inheritance = PropertyInheritance.INHERITED;
				}
				propertyIndex = Integer.MIN_VALUE;
			} else {
				inheritance = PropertyInheritance.DECLARED;
				propertyIndex = index++;
				declaredCnt++;
			}

			_properties.add(PropertyWriter.newPropertyWriter(this, property, inheritedProperty, propertyIndex, inheritance));
		}

		int inheritedCnt = _properties.size() - declaredCnt;
		_fullSwitches = declaredCnt >= inheritedCnt / 2 || inheritedCnt <= 5;
		_noOwnProperties = declaredCnt == 0;

		if (EqualityByValue.class.isAssignableFrom(_itemType)
			&& !EqualityByValue.class.isAssignableFrom(_leadingSuperInterfaceRaw)) {
			_needEqualsMethod = true;
		}
	}

	/**
	 * Find the {@link PropertyDescriptor} that has a local declaration (setter, or getter) that is
	 * defined in in the given implemented interfaces.
	 */
	private PropertyDescriptorSPI getDeclaration(Set<Class<?>> implementedInterfaces,
			PropertyDescriptorSPI property) {
		if ((property.getLocalGetter() != null || property.getSetter() != null)
			&& implementedInterfaces.contains(property.getDescriptor().getConfigurationInterface())) {
			return property;
		}

		for (PropertyDescriptorSPI superProperty : property.getSuperProperties()) {
			PropertyDescriptorSPI declaration = getDeclaration(implementedInterfaces, superProperty);
			if (declaration != null) {
				return declaration;
			}
		}
		return null;
	}

	/**
	 * Constructs the set of interfaces, whose local properties must be implemented by this
	 * implementation class, if the implementation class of the given leading super interface is
	 * chosen as super class of the current implementation class.
	 */
	private Set<Class<?>> findImplementedInterfaces(Class<?> itemType, Class<?> leadingSuperInterface) {
		Set<Class<?>> result = new HashSet<>();
		addImplementedInterfaces(result, leadingSuperInterface, itemType);
		return result;
	}

	private void addImplementedInterfaces(Set<Class<?>> result, Class<?> leadingSuperInterface, Class<?> type) {
		if (!result.add(type)) {
			// Already seen.
			return;
		}

		// New interface found.
		for (Class<?> superInterface : type.getInterfaces()) {
			if (superInterface.isAssignableFrom(leadingSuperInterface)) {
				// Already implemented.
				continue;
			}

			addImplementedInterfaces(result, leadingSuperInterface, superInterface);
		}
	}

	/**
	 * Creates a {@link Map} of {@link PropertyDescriptor#identifier()} to the
	 * {@link PropertyDescriptor} for all properties of the given type.
	 */
	private Map<NamedConstant, PropertyDescriptorSPI> indexProperties(Class<?> type) {
		Map<NamedConstant, PropertyDescriptorSPI> result = new HashMap<>();
		ConfigurationDescriptorSPI descriptor =
			(ConfigurationDescriptorSPI) TypedConfiguration.getConfigurationDescriptor(type);
		for (PropertyDescriptorSPI property : descriptor.getProperties()) {
			result.put(property.identifier(), property);
		}
		return result;
	}

	/**
	 * Find the super interface of {@link #_itemType} whose implementation class can be used as
	 * super class for this implementation class.
	 */
	private Type findSuperImplementationInterface(Class<?> itemType) {
		for (Type candidate : itemType.getGenericInterfaces()) {
			Class<?> rawCandidate = rawType(candidate);
			if (ConfigurationItem.class.isAssignableFrom(rawCandidate)) {
				if (candidate == ConfigurationItem.class) {
					// Even if no implementation class is generated, ConfigurationItem has an
					// implementation class that can be used as super class.
					return candidate;
				}

				if (doNotGenerateImplementationClass(rawCandidate)) {
					// No super class exists, choose another interface.
					continue;
				}

				// An implementation class should have been generated for the super interface,
				// use this as super class of this implementation class.
				return candidate;
			}
		}

		// No suitable super type found, use ConfigurationItem as super interface
		// to ensure that the resulting class is compatible with ConfigurationItem.
		return ConfigurationItem.class;
	}

	private String toImplString(Type type) {
		StringBuilder result = new StringBuilder();
		appendImplType(result, type);
		return result.toString();
	}

	private void appendImplType(StringBuilder out, Type type) {
		if (type instanceof Class<?>) {
			out.append(qualifiedImplClassName((Class<?>) type));
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parametrizedType = (ParameterizedType) type;
			appendImplType(out, parametrizedType.getRawType());
			appendTypeArguments(out, parametrizedType);
		} else {
			appendType(out, type);
		}
	}

	String toString(Type type) {
		StringBuilder result = new StringBuilder();
		appendType(result, type);
		return result.toString();
	}

	void appendType(Type type) {
		appendType(lineBuffer(), type);
	}

	private boolean appendType(StringBuilder out, Type type) {
		if (type instanceof Class<?>) {
			appendClassType(out, (Class<?>) type);
			return true;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parametrizedType = (ParameterizedType) type;
			appendType(out, parametrizedType.getRawType());
			appendTypeArguments(out, parametrizedType);
			return true;
		} else if (type instanceof TypeVariable<?>) {
			TypeVariable<?> var = (TypeVariable<?>) type;
			Type boundType = _bindings.get(var);
			if (boundType == null && !_bindings.containsKey(var)) {
				// Unknown var, may happen if generating signatures of methods with local type
				// parameters.
				boundType = var;
			}
			if (boundType == null) {
				return false;
			} else if (boundType == var) {
				out.append(var.getName());
				return true;
			} else {
				return appendType(out, boundType);
			}
		} else if (type instanceof WildcardType) {
			WildcardType wildcard = (WildcardType) type;
			out.append('?');
			Type[] lowerBounds = wildcard.getLowerBounds();
			{
				int beforeSuper = out.length();
				int cnt = 0;
				out.append(" super ");
				boolean first = true;
				for (Type bound : lowerBounds) {
					int beforeType = out.length();
					if (first) {
						first = false;
					} else {
						out.append(" & ");
					}
					boolean ok = appendType(out, bound);
					if (ok) {
						cnt++;
					} else {
						out.setLength(beforeType);
					}
				}
				if (cnt == 0) {
					out.setLength(beforeSuper);
				}
			}

			Type[] upperBounds = wildcard.getUpperBounds();
			{
				int beforeExtends = out.length();
				int cnt = 0;
				out.append(" extends ");
				boolean first = true;
				for (Type bound : upperBounds) {
					if (bound == Object.class) {
						continue;
					}
					int beforeType = out.length();
					if (first) {
						first = false;
					} else {
						out.append(" & ");
					}
					boolean ok = appendType(out, bound);
					if (ok) {
						cnt++;
					} else {
						out.setLength(beforeType);
					}
				}
				if (cnt == 0) {
					out.setLength(beforeExtends);
				}
			}
			return true;
		} else if (type instanceof GenericArrayType) {
			GenericArrayType arrayType = (GenericArrayType) type;
			appendType(out, arrayType.getGenericComponentType());
			out.append("[]");
			return true;
		} else {
			throw new UnsupportedOperationException("Cannot handle type '" + type + "'.");
		}
	}

	void appendTypeArguments(Type type) {
		if (type instanceof ParameterizedType) {
			appendTypeArguments(lineBuffer(), (ParameterizedType) type);
		}
	}

	private void appendTypeArguments(StringBuilder out, ParameterizedType type) {
		out.append('<');
		boolean first = true;
		for (Type arg : type.getActualTypeArguments()) {
			if (first) {
				first = false;
			} else {
				out.append(',');
			}
			appendType(out, arg);
		}
		out.append('>');
	}

	private void appendClassType(StringBuilder out, Class<?> rawType) {
		out.append(toString(rawType));
	}

	private String toString(Class<?> type) {
		return type.getCanonicalName();
	}

	private Class<?> rawType(Type type) {
		if (type instanceof Class<?>) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parametrizedType = (ParameterizedType) type;
			return rawType(parametrizedType.getRawType());
		} else if (type instanceof TypeVariable<?>) {
			TypeVariable<?> var = (TypeVariable<?>) type;
			Type boundType = _bindings.get(var);
			if (boundType == var) {
				Type[] bounds = var.getBounds();
				if (bounds.length == 0) {
					return Object.class;
				} else {
					return rawType(bounds[0]);
				}
			} else if (boundType == null) {
				return Object.class;
			} else {
				return rawType(boundType);
			}
		} else if (type instanceof WildcardType) {
			WildcardType wildcard = (WildcardType) type;
			Type[] upperBounds = wildcard.getUpperBounds();
			if (upperBounds.length == 0) {
				return Object.class;
			} else {
				return rawType(upperBounds[0]);
			}
		} else if (type instanceof GenericArrayType) {
			GenericArrayType arrayType = (GenericArrayType) type;
			return Array.newInstance(rawType(arrayType.getGenericComponentType()), 0).getClass();
		} else {
			throw new UnsupportedOperationException("Cannot handle type '" + type + "'.");
		}
	}

	static boolean doNotGenerateImplementationClass(Class<?> configInterface) {
		if (NO_GENERATION_ITEMS.contains(configInterface)) {
			return true;
		}
		if (configInterface.getAnnotation(NoImplementationClassGeneration.class) != null) {
			return true;
		}
		return false;
	}

	@Override
	protected void writeBody() {
		writeImports();
		writeClassComment();
		writeClass();
	}

	private void writeClass() {
		writeDeclarationLine();
		{
			nl();
			writeClassContent();
		}
		line("}");
	}

	private void writeDeclarationLine() {
		append("public ");
		if (isAbstract()) {
			append("abstract ");
		}
		append("class ");
		append(className());
		appendTypes(true);
		append(" extends ");
		append(toImplString(_leadingSuperInterface));
		append(" implements ");
		append(itemClassDotSeparated());
		appendTypes(false);
		if (isConfigPart() && !isConfigPartInherited()) {
			append(", ");
			append(ConfigPartInternal.class.getCanonicalName());
		}
		append(" {");
		flushLine();
	}

	private boolean isAbstract() {
		return _itemType.getAnnotation(Abstract.class) != null;
	}

	private void writeClassContent() {
		writeStatics();
		writeVariables();
		writeEmptyConstructor();
		writeCopyConstructor();
		writeMethods();
	}

	private void writeStatics() {
		for (PropertyWriter property : _properties) {
			property.writeIndexConstant();
		}
		for (PropertyWriter property : _properties) {
			property.writePropertyConstant();
		}
		writePropertyIndexing();
	}

	private void writePropertyIndexing() {
		String Map = toString(Map.class);
		String HashMap = toString(HashMap.class);
		String descriptor = "descriptor";
		String index = "index";

		line("protected static final " + Map + "<NamedConstant, Integer> " + PROPERTY_INDEX_NAME + ";");
		nl();
		line("static {");
		appendType(ConfigurationDescriptorSPI.class);
		append(" " + descriptor + " = (");
		appendType(ConfigurationDescriptorSPI.class);
		append(") TypedConfiguration.getConfigurationDescriptor(" + itemClassDotSeparated() + ".class);");
		flushLine();
		for (PropertyWriter property : _properties) {
			property.writePropertyLookup(descriptor);
		}
		nl();
		append(Map);
		append("<NamedConstant, Integer> ");
		append(index);
		append(" = new ");
		append(HashMap);
		append("<>(");
		append(toImplString(_leadingSuperInterfaceRaw));
		append(".");
		append(PROPERTY_INDEX_NAME);
		append(");");
		flushLine();
		for (PropertyWriter property : _properties) {
			property.writePropertyIndexing(index);
		}
		line(PROPERTY_INDEX_NAME + " = " + index + ";");
		line("}");
		nl();
	}

	private void writeEmptyConstructor() {
		javadocStart();
		commentLine("Creates a new empty {@link " + itemClassDotSeparated() + "}.");
		javadocStop();
		append("public ");
		append(className());
		appendEmptyConstructorArgs(true);
		append(" {");
		flushLine();

		append("super");
		appendEmptyConstructorArgs(false);
		append(";");
		flushLine();

		for (PropertyWriter property : _properties) {
			property.writeInitValueCall();
		}
		line("}");
		nl();
	}

	private void appendEmptyConstructorArgs(boolean withParameterDef) {
		append('(');
		if (withParameterDef) {
			append(ConfigurationDescriptor.class.getName());
			append(' ');
		}
		append("descriptor, ");
		if (withParameterDef) {
			append(Location.class.getName());
			append(' ');
		}
		append("location");
		append(')');
	}

	private void writeCopyConstructor() {
		javadocStart();
		commentLine("Copies a {@link " + itemClassDotSeparated() + "}.");
		javadocStop();
		append("public ");
		append(className());
		appendCopyConstructorArgs(true);
		append(" {");
		flushLine();

		append("super");
		appendCopyConstructorArgs(false);
		append(";");
		flushLine();

		for (PropertyWriter property : _properties) {
			property.writeInitCopyCall(OTHER_ARG);
		}
		line("}");
		nl();
	}

	private void appendCopyConstructorArgs(boolean withParameterDef) {
		append('(');
		if (withParameterDef) {
			appendType(ConfigurationDescriptor.class);
			append(' ');
		}
		append("descriptor, ");
		if (withParameterDef) {
			appendType(ConfigurationItem.class);
			append(' ');
		}
		append(OTHER_ARG);
		append(')');
	}

	private void writeClassComment() {
		javadocStart();
		commentLine("Implementation of class {@link " + itemClassDotSeparated() + "}.");
		commentLine("");
		writeCvsTags();
		javadocStop();
	}

	private boolean isConfigPart() {
		return ConfigPart.class.isAssignableFrom(_itemType);
	}

	/**
	 * Whether the {@link ConfigPart} interface (and with that {@link ConfigPartInternal}) is
	 * inherited from the main super descriptor.
	 */
	private boolean isConfigPartInherited() {
		return ConfigPart.class.isAssignableFrom(_leadingSuperInterfaceRaw);
	}

	private void writeVariables() {
		for (PropertyWriter property : _properties) {
			property.writeVariables();
		}
	}

	private void writeImports() {
		if (_imports.isEmpty()) {
			return;
		}
		for (String importClass : _imports) {
			line("import " + importClass + ";");
		}
		nl();
	}

	private String itemClassDotSeparated() {
		return _itemType.getCanonicalName();
	}

	private void appendTypes(boolean withBounds) {
		appendTypeParameters(_itemType.getTypeParameters(), withBounds);
	}

	void appendTypeParameters(TypeVariable<?>[] typeParameters, boolean withBounds) {
		if (typeParameters.length == 0) {
			return;
		}
		append("<");
		for (int i = 0; i < typeParameters.length; i++) {
			if (i > 0) {
				append(", ");
			}
			TypeVariable<?> type = typeParameters[i];
			append(type.getName());
			Type[] bounds = type.getBounds();
			if (withBounds) {
				int beforeExtends = mark();
				append(" extends ");

				int boundCnt = 0;
				boolean first = true;
				for (int n = 0, cnt = bounds.length; n < cnt; n++) {
					Type bound = bounds[n];
					if (bound == Object.class) {
						continue;
					}

					int beforeType = mark();
					if (first) {
						first = false;
					} else {
						append(" & ");
					}
					boolean ok = appendType(lineBuffer(), bound);
					if (ok) {
						boundCnt++;
					} else {
						revert(beforeType);
					}
				}

				if (boundCnt == 0) {
					revert(beforeExtends);
				}
			}

		}
		append(">");
	}

	private void writeMethods() {
		for (PropertyWriter property : _properties) {
			property.writeMethods();
		}
		writeVisitMethods();
		writeGeneralMethods();
	}

	private void writeVisitMethods() {
		Set<Signature> implemented = new HashSet<>();
		for (Class<?> implementedInterface : _implementedInterfaces) {
			ConfigurationDescriptorSPI descriptor =
				(ConfigurationDescriptorSPI) TypedConfiguration.getConfigurationDescriptor(implementedInterface);
			writeVisitMethods(implemented, descriptor);
		}
	}

	private void writeVisitMethods(Set<Signature> implemented, ConfigurationDescriptorSPI descriptor) {
		writeVisitMethods(descriptor, implemented);
		Collection<Method> declaredVisitMethods = descriptor.getDeclaredVisitMethods();
		for (Method visitMethod : declaredVisitMethods) {
			if (implemented.add(Signature.signature(visitMethod))) {
				writeVisitStub(visitMethod);
			}
		}
	}

	private void writeVisitMethods(ConfigurationDescriptorSPI descriptor, Set<Signature> implemented) {
		writeLocalVisitMethods(descriptor, implemented);
		for (ConfigurationDescriptorSPI superDescriptor : descriptor.getSuperDescriptors()) {
			writeVisitMethods(superDescriptor, implemented);
		}
	}

	private void writeLocalVisitMethods(ConfigurationDescriptorSPI descriptor, Set<Signature> implemented) {
		Map<Method, VisitMethod> visitMethods = descriptor.getVisitImplementations(_itemType);
		for (Entry<Method, VisitMethod> entry : visitMethods.entrySet()) {
			Method visitMethod = entry.getKey();
			Method visitorImpl = entry.getValue().getVisitorMethod();

			if (implemented.add(Signature.signature(visitMethod))) {
				writeVisitMethod(visitMethod, visitorImpl);
			}
		}
	}

	private void writeVisitStub(Method visitMethod) {
		writeVisitMethod(visitMethod, null);
	}

	private void writeVisitMethod(Method visitMethod, Method visitorImpl) {
		line("@Override");
		append("public ");
		TypeVariable<Method>[] typeParameters = visitMethod.getTypeParameters();
		int beforeTypeParams = mark();
		appendTypeParameters(typeParameters, true);
		if (mark() > beforeTypeParams) {
			append(' ');
		}
		append(toString(visitMethod.getGenericReturnType()));
		append(" ");
		appendMethodWithArgs(visitMethod, VISIT_PARAMS);
		append(" {");
		flushLine();

		if (visitorImpl == null) {
			// For backwards compatibility: There are non-@Abstract configuration types that are not
			// covered in some visit cases.
			line("throw __errorUnimplementedVisitCase(\"" + visitMethod.getName() + "\");");
		} else {
			if (visitMethod.getReturnType() != void.class) {
				append("return ");
			}
			append(VISITOR_ARG);
			append(".");
			appendMethodCall(visitorImpl, VISITOR_ARGS);
			append(";");
			flushLine();
		}

		line("}");
		nl();
	}

	private void writeGeneralMethods() {
		writeIndexAccess();

		if (!_noOwnProperties) {
			writeValueMethod();
			writeUpdateMethod();
			writeResetMethod();
			writeValueSet();
		}
		if (_needEqualsMethod) {
			writeEqualsByValue();
			writeHashCodeByValue();
		}

	}

	private void writeIndexAccess() {
		override();
		line("protected " + toString(Map.class) + "<NamedConstant, Integer> _index() {");
		line("return " + PROPERTY_INDEX_NAME + ";");
		line("}");
	}

	static abstract class PropertyAction {

		void appendSignature() {
			// Ignore.
		}

		void appendArgs() {
			// Ignore.
		}

		abstract void appendCase(String propertyVar, String indexVar, PropertyWriter property);

	}

	private void writeValueMethod() {
		writeSwitchMethod("Object", "_value", new PropertyAction() {
			@Override
			void appendCase(String propertyVar, String indexVar, PropertyWriter property) {
				property.writeValueCase();
			}
		});
	}

	private void writeUpdateMethod() {
		final String newValueVar = "newValue";
		final String isSetVar = "isSet";

		writeSwitchMethod("Object", "_update", new PropertyAction() {
			@Override
			void appendSignature() {
				super.appendSignature();
				append(", Object ");
				append(newValueVar);
				append(", boolean ");
				append(isSetVar);
			}

			@Override
			void appendArgs() {
				super.appendArgs();
				append(", ");
				append(newValueVar);
				append(", ");
				append(isSetVar);
			}

			@Override
			void appendCase(String propertyVar, String indexVar, PropertyWriter property) {
				property.writeUpdateCase(propertyVar, newValueVar, isSetVar);
			}
		});
	}

	private void writeResetMethod() {
		writeSwitchMethod("void", "_reset", new PropertyAction() {
			@Override
			void appendCase(String propertyVar, String indexVar, PropertyWriter property) {
				property.writeResetCase();
			}
		});
	}

	private void writeValueSet() {
		writeSwitchMethod("boolean", "_valueSet", new PropertyAction() {
			@Override
			void appendCase(String propertyVar, String indexVar, PropertyWriter property) {
				property.writeValueSetCase();
			}
		});
	}

	private void writeSwitchMethod(String returnType, String methodName, PropertyAction inner) {
		String indexVar = "index";
		String propertyVar = "property";
	
		override();
		append("protected " + returnType + " ");
		append(methodName + (_fullSwitches ? "" : "Fallback"));
		append("(");
		appendType(PropertyDescriptor.class);
		append(" ");
		append(propertyVar);
		append(", int ");
		append(indexVar);
		inner.appendSignature();
		append(") {");
		flushLine();
	
		{
			writeSwitchStart(indexVar);
			{
				if (_fullSwitches) {
					line("case __NO_SUCH_PROPERTY: {");
					{
						if ("_valueSet".equals(methodName)) {
							line("return false;");
						} else {
							line("throw __unknown(" + propertyVar + ");");
						}
					}
					line("}");
				}
				for (PropertyWriter property : _properties) {
					if (!_fullSwitches) {
						if (!property.initialDeclaration()) {
							continue;
						}
					}
					inner.appendCase(propertyVar, indexVar, property);
				}
				line("default: {");
				{
					if (_fullSwitches) {
						fallbackSwitchCall(returnType, methodName, inner, indexVar, propertyVar);
					} else {
						superSwitchCall(returnType, methodName, inner, indexVar, propertyVar);
					}
				}
				line("}");
			}
			line("}");
		}
		line("}");
		nl();

		if (_fullSwitches) {
			// Generate new empty fallback method.
			override();
			append("protected " + returnType + " ");
			append(methodName + "Fallback");
			append("(");
			appendType(PropertyDescriptor.class);
			append(" ");
			append(propertyVar);
			append(", int ");
			append(indexVar);
			inner.appendSignature();
			append(") {");
			flushLine();
			{
				line("throw __missing(" + propertyVar + ", " + indexVar + ");");
			}
			line("}");
			nl();
		}
	}

	private void fallbackSwitchCall(String returnType, String methodName, PropertyAction inner, String indexVar,
			String propertyVar) {
		appendReturn(returnType);
		append(methodName + "Fallback");
		append("(");
		append(propertyVar);
		append(", ");
		append(indexVar);
		inner.appendArgs();
		append(");");
		flushLine();
	}

	private void superSwitchCall(String returnType, String methodName, PropertyAction inner, String indexVar,
			String propertyVar) {
		appendReturn(returnType);
		append("super.");
		append(methodName + "Fallback");
		append("(");
		append(propertyVar);
		append(", ");
		append(indexVar);
		inner.appendArgs();
		append(");");
		flushLine();
	}

	private void appendReturn(String returnType) {
		if (!"void".equals(returnType)) {
			append("return ");
		}
	}

	private void override() {
		line("@Override");
	}

	private void writeNoSuchProperty(String propertyVar, String indexVar) {
		line("throw __missing(" + propertyVar + ", " + indexVar + ");");
	}

	private void writeSwitchStart(String indexVar) {
		line("switch (" + indexVar + ") {");
	}

	private void writeHashCodeByValue() {
		override();
		line("public int hashCode() {");
		line("return __hashCodeByValue();");
		line("}");
		nl();
	}

	private void writeEqualsByValue() {
		override();
		line("public boolean equals(Object o) {");
		line("return __equalsByValue(o);");
		line("}");
		nl();
	}

	@Override
	public String className() {
		return _className;
	}

	/**
	 * Generates implementation classes for the given configItem and all super config items.
	 * 
	 * @see ConfigItemGenerator#generate(Log, File, ConfigItemGenerator)
	 */
	public static void generateRecursive(Log log, File applicationRoot, Class<? extends ConfigurationItem> item) {
		Set<Class<? extends ConfigurationItem>> allItems = new HashSet<>();
		collectItems(allItems, item);
		for (Class<? extends ConfigurationItem> configItem : allItems) {
			generate(log, applicationRoot, configItem);
		}
	}

	private static void collectItems(Set<Class<? extends ConfigurationItem>> allItems,
			Class<? extends ConfigurationItem> item) {
		boolean newlyAdded = allItems.add(item);
		if (!newlyAdded) {
			// Item and therefore all super items already added.
			return;
		}
		for (Class<?> superInterface : item.getInterfaces()) {
			if (ConfigurationItem.class.isAssignableFrom(superInterface)) {
				@SuppressWarnings("unchecked")
				Class<? extends ConfigurationItem> superConfigInterface = (Class<? extends ConfigurationItem>) superInterface;
				collectItems(allItems, superConfigInterface);
			}
		}
	}

	public static boolean generate(Log log, File applicationRoot, Class<? extends ConfigurationItem> item) {
		if (!item.isInterface()) {
			log.error("Implementation classes can only be build for interfaces: " + item.getName());
			return false;
		}
		log.info("Creating implementation class for " + item.getName());
		ConfigurationDescriptorSPI descriptor;
		try {
			descriptor = (ConfigurationDescriptorSPI) TypedConfiguration.getConfigurationDescriptor(item);
		} catch (Exception ex) {
			log.error("Unable to create config descriptor for class " + item.getName(), ex);
			return false;
		}
		ConfigItemGenerator generator = new ConfigItemGenerator(descriptor);
		boolean generated = generate(log, applicationRoot, generator);
		if (generated) {
			log.info("Generated implemention class for " + item.getName());
		}
		return generated;
	}

	public static boolean generate(Log log, File applicationRoot, ConfigItemGenerator generator) {
		Class<?> item = generator._itemType;
		File source = new File(applicationRoot, ModuleLayoutConstants.SRC_MAIN_DIR);
		File packageFile = new File(source, generator.packageName().replace('.', '/'));
		if (!packageFile.exists()) {
			log.error("Folder to create implementation for config item '" + item.getName() + "' does not exist: "
				+ packageFile.getAbsolutePath());
			return false;
		}
		if (!packageFile.isDirectory()) {
			log.error("Folder to create implementation for config item '" + item.getName() + "' is not a directory: "
				+ packageFile.getAbsolutePath());
			return false;
		}
		File targetFile = new File(packageFile, generator.className() + ".java");
		try {
			generator.generate(targetFile);
			return true;
		} catch (IOException ex) {
			log.error("Failure creating implementation class for '" + item.getName() + "'.", ex);
			return false;
		}
	}

	/**
	 * Make visible to {@link PropertyWriter}.
	 * 
	 * @see com.top_logic.basic.generate.JavaGenerator#line(java.lang.String)
	 */
	@Override
	protected void line(String line) {
		super.line(line);
	}

	/**
	 * Make visible to {@link PropertyWriter}.
	 * 
	 * @see com.top_logic.basic.generate.JavaGenerator#line(java.lang.String)
	 */
	@Override
	protected void nl() {
		super.nl();
	}

	/**
	 * The local name of the implementation class of the given config interface.
	 */
	public static String implClassName(Class<?> configInterface) {
		if (configInterface.isLocalClass()) {
			// Named class defined in a method
			throw new IllegalArgumentException("No generation for local class.");
		}
		if (configInterface.isAnonymousClass()) {
			throw new IllegalArgumentException("No generation for anonymous class.");
		}
		String packageName = configInterface.getPackage().getName();
		String className;
		if (packageName.isEmpty()) {
			className = configInterface.getName();
		} else {
			className = configInterface.getName().substring(packageName.length() + 1);
		}
		return "_" + className.replace("$", "__");
	}

	static String implClassPackage(Class<?> configInterface) {
		return configInterface.getPackage().getName();
	}

	/**
	 * The name of a generated implementation class for the given configuration type.
	 */
	public static String qualifiedImplClassName(Class<?> configInterface) {
		return implClassPackage(configInterface) + "." + implClassName(configInterface);
	}

	void appendMethodWithArgs(Method method, String[] paramNames) {
		appendMethodString(method.getName(), method.getGenericParameterTypes(), paramNames);
	}

	void appendMethodString(String methodName, Type[] paramTypes, String[] paramNames) {
		append(methodName);
		append("(");
		int paramCnt = paramTypes.length;
		for (int i = 0; i < paramCnt; i++) {
			if (i > 0) {
				append(", ");
			}
			append(toString(paramTypes[i], false));
			if (paramNames != null) {
				append(" ");
				append(paramNames[paramNames.length - paramCnt + i]);
			}
		}
		append(")");
	}

	void appendMethodCall(Method method, String[] argNames) {
		appendMethodCall(method.getName(), method.getGenericParameterTypes(), argNames);
	}

	void appendMethodCall(String methodName, Type[] paramTypes, String[] argNames) {
		append(methodName);
		append("(");
		int paramCnt = paramTypes.length;
		for (int i = 0; i < paramCnt; i++) {
			if (i > 0) {
				append(", ");
			}
			append(argNames[argNames.length - paramCnt + i]);
		}
		append(")");
	}

	String toString(Type type, boolean usePrimitiveWrapper) {
		return toString(wrappedPrimitive(type, usePrimitiveWrapper));
	}

	Type wrappedPrimitive(Type type, boolean usePrimitiveWrapper) {
		if (usePrimitiveWrapper) {
			type = wrappedPrimitive(type);
		}
		return type;
	}

	Type wrappedPrimitive(Type type) {
		type = PrimitiveTypeUtil.asNonPrimitive(type);
		return type;
	}

}
