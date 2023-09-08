/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import static com.top_logic.basic.StringServices.*;
import static com.top_logic.basic.col.factory.CollectionFactory.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.iterator.AppendIterator;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.element.config.annotation.ScopeRef;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.ScopeConfig;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Utils;

/**
 * Transforms the fooChildren-attributes of the node types to a single children-attribute per type.
 * <p>
 * Uses only {@link Collection}s with stable order to produce the exact same output with every
 * invocation. This makes it possible to diff the generated files after changes to the code or the
 * transformed source file.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class ChildAttributeTransformer {

	/** The suffix of the old "FooChildren" attributes. */
	private static final String LEGACY_CHILDREN_ATTRIBUTE_SUFFIX = "Children";

	/** The instance of the {@link ChildAttributeTransformer}. */
	public static final ChildAttributeTransformer INSTANCE = new ChildAttributeTransformer();

	void transform(ModelConfig model) {
		Collection<ModuleConfig> modules = getStructureElementModules(model);
		for (ModuleConfig module : modules) {
			makeRootInterfacesToStructuredElements(module);
			/* Ignore interface types: Interfaces can have attributes, too. But the transformed
			 * StructureElementConfig.xml files have the children-attributes only in classes. It is
			 * therefore correct to transform only ClassConfigs and ignore InterfaceConfigs. */
			for (ClassConfig type : getAllClasses(module)) {
				addStructuredElementAsSuperType(type);
				if (type.getAttributes().isEmpty()) {
					/* If there are no FooChildren-attributes, there must not be a "children"
					 * attribute, as the type is not allowed to have children. */
					continue;
				}
				List<PartConfig> formerChildAttributes = mergeChildrenAttributes(type);
				addChildType(type, module, getTargetTypes(formerChildAttributes, module, model));
				moveScopeRefs(formerChildAttributes, module, model);
			}
		}
	}

	private Collection<ModuleConfig> getStructureElementModules(ModelConfig model) {
		List<ModuleConfig> result = list();
		for (ModuleConfig module : model.getModules()) {
			if (isStructuredElementModule(module)) {
				result.add(module);
			}
		}
		return result;
	}

	private boolean isStructuredElementModule(ModuleConfig module) {
		return !ElementConfigUtil.getSingletons(module).isEmpty();
	}

	/**
	 * The root interface types of every "StructuredElement" module are made subtypes of
	 * StructureElements to simplify searches a bit.
	 */
	private void makeRootInterfacesToStructuredElements(ModuleConfig module) {
		for (InterfaceConfig interfaceConfig : getRootInterfaces(getGlobalInterfaces(module), module.getName())) {
			addStructuredElementAsSuperType(interfaceConfig);
		}
	}

	private Set<InterfaceConfig> getGlobalInterfaces(ScopeConfig scope) {
		return FilterUtil.filterSet(InterfaceConfig.class, scope.getTypes());
	}

	/** Returns the types that have no super-type within their own module. */
	private Collection<InterfaceConfig> getRootInterfaces(Set<InterfaceConfig> types, String module) {
		Set<InterfaceConfig> rootTypes = set(types);
		Iterator<InterfaceConfig> iterator = rootTypes.iterator();
		while (iterator.hasNext()) {
			InterfaceConfig type = iterator.next();
			for (ExtendsConfig extend : type.getGeneralizations()) {
				if (isWithinSameModule(extend, module)) {
					iterator.remove();
					break;
				}
			}
		}
		return rootTypes;
	}

	private boolean isWithinSameModule(ExtendsConfig extend, String module) {
		if (isEmpty(extend.getModuleName())) {
			return true;
		}
		return Utils.equals(extend.getModuleName(), module);
	}

	private List<ObjectTypeConfig> getTargetTypes(List<PartConfig> attributes, ModuleConfig module,
			ModelConfig model) {
		List<ObjectTypeConfig> result = list();
		for (PartConfig attribute : attributes) {
			result.add(resolveClass(attribute.getTypeSpec(), module, model));
		}
		return result;
	}

	private ObjectTypeConfig resolveClass(String typeSpec, ModuleConfig module, ModelConfig model) {
		String[] nameParts = TLModelUtil.SPLIT_PATTERN.split(typeSpec);
		if (nameParts.length < 1) {
			throw errorNoNameParts(typeSpec);
		}
		if (nameParts.length == 1) {
			return (ObjectTypeConfig) module.getType(nameParts[0]);
		}
		if (nameParts.length == 2) {
			return (ObjectTypeConfig) model.getModule(nameParts[0]).getType(nameParts[1]);
		}
		throw errorTooManyNameParts(typeSpec);
	}

	private RuntimeException errorNoNameParts(String typeSpec) {
		throw new RuntimeException("Illegal type spec: '" + typeSpec + "'");
	}

	private RuntimeException errorTooManyNameParts(String typeSpec) {
		throw new RuntimeException(
			"Illegal type spec. Expected at most one ':' but found multiple: '" + typeSpec + "'");
	}

	private void addChildType(ClassConfig type, ModuleConfig module,
			List<ObjectTypeConfig> subTypes) {
		InterfaceConfig childType = createChildType(type);
		registerType(childType, module);
		addSpecializations(childType.getName(), module.getName(), subTypes);
	}

	private InterfaceConfig createChildType(ClassConfig classConfig) {
		InterfaceConfig childType = TypedConfiguration.newConfigItem(InterfaceConfig.class);
		childType.setName(createChildTypeName(classConfig));
		addStructuredElementAsSuperType(childType);
		return childType;
	}

	private void registerType(TypeConfig type, ModuleConfig module) {
		module.getTypes().add(type);
	}

	/**
	 * @param superType
	 *        The local name of the super type.
	 * @param superTypeModule
	 *        The name of the module.
	 */
	private void addSpecializations(String superType, String superTypeModule, List<ObjectTypeConfig> subTypes) {
		for (ObjectTypeConfig subType : subTypes) {
			subType.getGeneralizations().add(createExtend(superType, superTypeModule));
		}
	}

	private List<ClassConfig> getAllClasses(ScopeConfig scope) {
		return FilterUtil.filterList(ClassConfig.class, getAllScopes(scope));
	}

	private List<ScopeConfig> getAllScopes(ScopeConfig rootScope) {
		AppendIterator<ScopeConfig> scopes = new AppendIterator<>();
		scopes.append(rootScope);
		while (scopes.hasNext()) {
			scopes.appendAll(getInnerScopes(scopes.next()));
		}
		return scopes.copyUnderlyingCollection();
	}

	private List<ScopeConfig> getInnerScopes(ScopeConfig module) {
		Collection<TypeConfig> innerTypes = module.getTypes();
		return FilterUtil.filterList(ScopeConfig.class, innerTypes);
	}

	private List<PartConfig> mergeChildrenAttributes(ClassConfig classConfig) {
		checkHasOnlyChildrenAttributes(classConfig);
		List<PartConfig> formerChildAttributes = list(classConfig.getAttributes());
		removeAllAttributes(classConfig);
		addChildrenAttribute(classConfig);
		return formerChildAttributes;
	}

	private void checkHasOnlyChildrenAttributes(ClassConfig type) {
		for (PartConfig attribute : type.getAttributes()) {
			if (!isChildrenAttribute(attribute)) {
				throw errorUnexpectedNonChildrenAttribute(type, attribute);
			}
		}
	}

	private boolean isChildrenAttribute(PartConfig attribute) {
		return attribute.getName().endsWith(LEGACY_CHILDREN_ATTRIBUTE_SUFFIX);
	}

	/**
	 * Always throws a {@link RuntimeException}.
	 * 
	 * @return Never returns, but declares a {@link RuntimeException} for callers that need to throw
	 *         an {@link Exception} to make Java accept the code.
	 */
	private RuntimeException errorUnexpectedNonChildrenAttribute(ClassConfig type, PartConfig attribute) {
		throw new RuntimeException("Unexpected attribute declaration in non-abstract class."
			+ " Expect are only the 'FooChildren' attributes, as the other attributes are declared"
			+ " in abstract types. Attribute: '" + attribute.getName() + "' Class: '"
			+ type.getName() + "'");
	}

	private void removeAllAttributes(ClassConfig classConfig) {
		classConfig.getAttributes().clear();
	}

	private void addChildrenAttribute(ClassConfig classConfig) {
		ReferenceConfig childrenAttribute = createChildrenAttribute(classConfig);
		registerAttribute(childrenAttribute, classConfig);
	}

	private ReferenceConfig createChildrenAttribute(ClassConfig classConfig) {
		ReferenceConfig childrenAttribute = TypedConfiguration.newConfigItem(ReferenceConfig.class);
		childrenAttribute.setName(StructuredElement.CHILDREN_ATTR);
		childrenAttribute.setTypeSpec(createChildTypeName(classConfig));
		/* This is an override, as it is originally defined in StructuredElementContainer. */
		childrenAttribute.setOverride(true);
		return childrenAttribute;
	}

	private String createChildTypeName(ClassConfig classConfig) {
		return classConfig.getName() + StructuredElement.CHILDREN_TYPE_SUFFIX;
	}

	private void registerAttribute(PartConfig attribute, AttributedTypeConfig type) {
		type.getAttributes().add(attribute);
	}

	private void addStructuredElementAsSuperType(ObjectTypeConfig type) {
		type.getGeneralizations().add(createStructureElementExtend(type));
	}

	private ExtendsConfig createStructureElementExtend(ObjectTypeConfig type) {
		return createExtend(getStructuredElementSuperType(type), StructuredElement.MODULE_NAME);
	}

	private String getStructuredElementSuperType(ObjectTypeConfig type) {
		if (type instanceof InterfaceConfig) {
			/* Foo_Child-types and the root interface types of every "StructuredElement" module are
			 * made subtypes of StructureElements to simplify searches a bit. */
			return StructuredElement.TL_TYPE_NAME;
		}
		boolean hasChildrenAttributes = !type.getAttributes().isEmpty();
		if (hasChildrenAttributes) {
			return StructuredElement.CONTAINER_TL_TYPE_NAME;
		}
		return StructuredElement.TL_TYPE_NAME;
	}

	private ExtendsConfig createExtend(String superType, String superTypeModule) {
		ExtendsConfig extend = TypedConfiguration.newConfigItem(ExtendsConfig.class);
		extend.setQualifiedTypeName(qualifiedName(superTypeModule, superType));
		return extend;
	}

	private String qualifiedName(String module, String type) {
		return module + TLModelUtil.QUALIFIED_NAME_SEPARATOR + type;
	}

	private void moveScopeRefs(List<PartConfig> attributes, ModuleConfig module, ModelConfig model) {
		for (PartConfig attribute : attributes) {
			ScopeRef scopeRef = removeScopeRef(attribute);
			if (scopeRef == null) {
				continue;
			}
			TypeConfig targetType = resolveClass(attribute.getTypeSpec(), module, model);
			ScopeRef existingScopeRef = getScopeRef(targetType);
			if (isConflict(scopeRef, existingScopeRef)) {
				throw errorConflictingScopeRefs(targetType, scopeRef, existingScopeRef);
			}
			if (existingScopeRef == null) {
				setScopeRef(scopeRef, targetType);
			}
		}
	}

	private ScopeRef removeScopeRef(PartConfig attribute) {
		ScopeRef result = attribute.getAnnotation(ScopeRef.class);
		if (result != null) {
			attribute.getAnnotations().remove(result);
		}
		return result;
	}

	private ScopeRef getScopeRef(TypeConfig type) {
		return type.getAnnotation(ScopeRef.class);
	}

	private boolean isConflict(TLAnnotation left, TLAnnotation right) {
		if ((left == null) || (right == null)) {
			return false;
		}
		return !ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, right);
	}

	private RuntimeException errorConflictingScopeRefs(TypeConfig type, ScopeRef scopeRef,
			ScopeRef existingScopeRef) {
		throw new RuntimeException("Conflicting ScopeRefs for type '" + type.getName() + "'. First: " + scopeRef
			+ ". Second: " + existingScopeRef);
	}

	private void setScopeRef(ScopeRef scopeRef, TypeConfig type) {
		type.getAnnotations().add(scopeRef);
	}

}
