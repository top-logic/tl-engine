/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ComponentCommand;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.scripting.ScriptingUtil;
import com.top_logic.layout.scripting.action.FormInput;
import com.top_logic.layout.scripting.action.FormRawInput;
import com.top_logic.layout.scripting.recorder.DefaultScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.field.AttributeFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.FieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.TableFieldRef;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeRef;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeValue;
import com.top_logic.layout.scripting.recorder.ref.misc.FieldValue;
import com.top_logic.layout.scripting.recorder.ref.misc.SelectionRef;
import com.top_logic.layout.scripting.recorder.ref.value.BooleanValue;
import com.top_logic.layout.scripting.recorder.ref.value.DoubleNaming;
import com.top_logic.layout.scripting.recorder.ref.value.FloatNaming;
import com.top_logic.layout.scripting.recorder.ref.value.IntNaming;
import com.top_logic.layout.scripting.recorder.ref.value.LongNaming;
import com.top_logic.layout.scripting.recorder.ref.value.MapNaming;
import com.top_logic.layout.scripting.recorder.ref.value.MapValue;
import com.top_logic.layout.scripting.recorder.ref.value.NumberValue;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.CompactLabelPath;
import com.top_logic.layout.scripting.recorder.ref.value.object.ContextRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.IndexRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.IndexedObjectNaming;
import com.top_logic.layout.scripting.recorder.ref.value.object.LabeledValue;
import com.top_logic.layout.scripting.recorder.ref.value.object.NamedValue;
import com.top_logic.layout.scripting.recorder.ref.value.object.NodeRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.ObjectRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.ValuePath;
import com.top_logic.layout.scripting.recorder.specialcases.ValueScope;
import com.top_logic.layout.scripting.recorder.specialcases.ValueScopeFactory;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLModuleSingletons;
import com.top_logic.model.TLObject;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;
import com.top_logic.util.model.CompatibilityService;

/**
 * Creates stable {@link ModelName}s.
 * 
 * <p>
 * Stable means, they can still be resolved after resetting/deleting the database and replaying all
 * actions.
 * </p>
 * 
 * <p>
 * Methods that just create and fill {@link ModelName}s, can be found in the
 * {@link ReferenceInstantiator}.
 * </p>
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class ReferenceFactory {

	private static final Map<String, Set<String>> wrapperKeyAttributesProvider = new HashMap<>();

	/**
	 * Mechanism to prevent stack overflows when trying to wrap {@link ModelName} into
	 * {@link ModelName} and vice versa.
	 */
	private static final ThreadLocal<Boolean> BUILDING = new ThreadLocal<>();

	private static void registerWrapperKeyAttributes(String name, Set<String> keyAttributes) {
		wrapperKeyAttributesProvider.put(name, keyAttributes);
	}

	static {
		IterableConfiguration configuration = Configuration.getConfiguration(ReferenceFactory.class);
		for (Object entry : configuration.getNames()) {
			String name = (String) entry;
			String value = configuration.getValue(name);
			HashSet<String> keyAttributes = new HashSet<>(Arrays.asList(value.split("\\s*,\\s*")));
			registerWrapperKeyAttributes(name.trim(), keyAttributes);
		}
	}

	/**
	 * Creates a {@link FieldValue} for a given {@link FormField}.
	 * 
	 * @deprecated Use {@link FormInput}, or {@link FormRawInput}.
	 */
	@Deprecated
	public static FieldValue fieldValue(FormField field, Object value) {
		List<FieldRef> fieldPath = referenceField(field);
		ModelName valueRef = ModelResolver.buildModelName(field, value);
		return ReferenceInstantiator.fieldValue(fieldPath, valueRef);
	}

	/**
	 * Creates an {@link AttributeRef} from the given arguments.
	 */
	public static AttributeRef attributeRef(ModelName scopeRef, String typeName, String attributeName) {
		return ReferenceInstantiator.attributeRef(ReferenceInstantiator.typeRef(scopeRef, typeName), attributeName);
	}

	/**
	 * Creates an {@link IndexRef} to a single key-value-mapping.
	 * 
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}
	 * @see IndexedObjectNaming
	 */
	@Deprecated
	public static IndexRef indexRef(String typeName, String attributeName, Object value, Branch branch,
			Revision revision) {
		ModelName valueRef = ModelResolver.buildModelName(value);
		AttributeValue attrValue = ReferenceInstantiator.attributeValue(attributeName, valueRef);
		Map<String, AttributeValue> keyValueMapping = Collections.singletonMap(attributeName, attrValue);
		return ReferenceInstantiator.indexRef(typeName, keyValueMapping, branch, revision);
	}

	/**
	 * Creates a {@link NumberValue} from the given arguments.
	 * 
	 * @see IntNaming
	 * @see LongNaming
	 * @see FloatNaming
	 * @see DoubleNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	public static NumberValue numberValue(Number value) {
		if (value instanceof Integer) {
			return ReferenceInstantiator.intValue(value.intValue());
		}
		if (value instanceof Long) {
			return ReferenceInstantiator.longValue(value.longValue());
		}
		if (value instanceof Float) {
			return ReferenceInstantiator.floatValue(value.floatValue());
		}
		return ReferenceInstantiator.doubleValue(value.doubleValue());
	}

	/**
	 * @deprecated Use {@link ModelResolver#buildModelNames(Collection)}
	 */
	@Deprecated
	public static List<ModelName> referenceEachValue(Collection<?> values) {
		return ModelResolver.buildModelNames(values);
	}

	/**
	 * @deprecated Use {@link ModelResolver#buildModelNames(Object, Collection)}
	 */
	@Deprecated
	public static List<ModelName> referenceEachValue(Collection<?> values, Object valueContext) {
		return ModelResolver.buildModelNames(valueContext, values);
	}

	/**
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}
	 */
	@Deprecated
	public static ModelName referenceValue(Object value) {
		return ModelResolver.buildModelName(value);
	}

	/**
	 * @deprecated Use {@link ModelResolver#buildModelName(Object, Object)}
	 */
	@Deprecated
	public static ModelName referenceValue(Object value, Object valueContext) {
		return ModelResolver.buildModelName(valueContext, value);
	}

	/**
	 * @deprecated Use {@link ModelResolver#buildModelName(Object, Object)}
	 */
	@Deprecated
	public static Maybe<List<ModelName>> tryReferenceEachValue(Collection<?> values) {
		return ModelResolver.buildModelNamesIfAvailable(values);
	}

	/**
	 * @deprecated Use {@link ModelResolver#buildModelNamesIfAvailable(Object, Collection)}
	 */
	@Deprecated
	public static Maybe<List<ModelName>> tryReferenceEachValue(Collection<?> values, Object valueContext) {
		return ModelResolver.buildModelNamesIfAvailable(valueContext, values);
	}

	/**
	 * @deprecated Use {@link ModelResolver#buildModelNameIfAvailable(Object)}
	 */
	@Deprecated
	public static Maybe<? extends ModelName> tryReferenceValue(Object value) {
		return ModelResolver.buildModelNameIfAvailable(value);
	}

	/**
	 * @deprecated Use {@link ModelResolver#buildModelName(Object, Object)}.
	 */
	@Deprecated
	public static Maybe<? extends ModelName> tryReferenceValue(Object value, Object valueContext) {
		return ModelResolver.buildModelNameIfAvailable(valueContext, value);
	}

	/**
	 * Creates a reference to the <code>target</code>. It first tries to do it itself, if that
	 * fails, it uses the given {@link ValueScope}. If there is none or it fails too,
	 * {@link Maybe#none()} is returned.
	 * 
	 * @see #tryReferenceEachValue(Collection, Object)
	 * @see #referenceValue(Object, Object)
	 * 
	 * @param value
	 *        Is allowed to be <code>null</code>.
	 * @param valueContext
	 *        See {@link ValueResolver#ValueResolver(Object)}.
	 * @return Is never <code>null</code>.
	 */
	public static Maybe<? extends ModelName> legacyTryReferenceValue(Object value, Object valueContext) {
		Maybe<ModelName> simpleValueRef = referenceSimpleValue(value, valueContext);
		if (simpleValueRef.hasValue()) {
			return simpleValueRef;
		}
		/* Prefer global variables to labeled values, as the user can always just drop all the
		 * global variables if they should not be used: ClearGlobalVariablesOp */
		Maybe<String> key = referenceGlobalVariable(value);
		if (key.hasValue()) {
			return Maybe.some(ReferenceInstantiator.globalVariableRef(key.get()));
		}

		/* ModelNames and NamedValues have to take precedence over the generic Wrapper based
		 * identification in case that produces unusable references. */
		Maybe<? extends ModelName> localValue = referenceContextLocalValueSafe(value, valueContext);
		if (localValue.hasValue()) {
			return localValue;
		}

		Boolean before = ReferenceFactory.buildingModelName(Boolean.TRUE);
		try {
			if (before == null || !before.booleanValue()) {
				Maybe<? extends ModelName> modelName = ModelResolver.buildModelNameIfAvailable(value);
				if (modelName.hasValue()) {
					return modelName;
				}
			}
		} finally {
			ReferenceFactory.buildingModelName(before);
		}

		if (value instanceof KnowledgeItem) {
			KnowledgeItem knowledgeItem = (KnowledgeItem) value;
			return ModelResolver.buildModelNameIfAvailable(valueContext, knowledgeItem.getWrapper());
		}
		if (value instanceof Wrapper) {
			Maybe<? extends ModelName> objectRef = referenceWrapperSafe(value);
			if (objectRef.hasValue()) {
				return objectRef;
			}
		}
		if (value instanceof BinaryData) {
			return referenceBase64ValueSafe(value);
		}

		return localValue;
	}

	private static Maybe<? extends ModelName> referenceContextLocalValueSafe(Object value, Object valueContext) {
		try {
			return ValueScopeFactory.referenceContextLocalValue(valueContext, value);
		} catch (RuntimeException ex) {
			/* Unable to use this ModelNamingScheme. This can happen, as not every NamingScheme can
			 * be used in every situation. The caller will try the next one. */
			return Maybe.none();
		}
	}

	private static Maybe<? extends ModelName> referenceWrapperSafe(Object value) {
		try {
			return objectRef((Wrapper) value);
		} catch (RuntimeException ex) {
			/* Unable to use this ModelNamingScheme. This can happen, as not every NamingScheme can
			 * be used in every situation. The caller will try the next one. */
			return Maybe.none();
		}
	}

	private static Maybe<? extends ModelName> referenceBase64ValueSafe(Object value) {
		try {
			return Maybe.some(ReferenceInstantiator.base64Value(BinaryData.cast(value)));
		} catch (RuntimeException ex) {
			/* Unable to use this ModelNamingScheme. This can happen, as not every NamingScheme can
			 * be used in every situation. The caller will try the next one. */
			return Maybe.none();
		}
	}

	private static Maybe<String> referenceGlobalVariable(Object value) {
		GlobalVariableStore variables = GlobalVariableStore.getFromSession();
		if (variables == null) {
			return Maybe.none();
		}
		return findVariableName(value, variables.getMappings());
	}

	private static Maybe<String> findVariableName(Object value, Map<String, Object> variables) {
		for (Map.Entry<String, Object> variable : variables.entrySet()) {
			if (Utils.equals(variable.getValue(), value)) {
				// Don't care if there is more than one match. Just take the first.
				return Maybe.toMaybeButTreatNullAsValidValue(variable.getKey());
			}
		}
		return Maybe.none();
	}
	
	private static Maybe<ModelName> referenceSimpleValue(Object value, Object valueContext) {
		if (value == null) {
			return Maybe.<ModelName> toMaybe(ReferenceInstantiator.nullValue());
		} else if (value instanceof String) {
			return Maybe.<ModelName> toMaybe(ReferenceInstantiator.stringValue((String) value));
		} else if (value instanceof Boolean) {
			return Maybe.<ModelName> toMaybe(ReferenceInstantiator.booleanValue(((Boolean) value).booleanValue()));
		} else if (value instanceof Number) {
			return Maybe.<ModelName> toMaybe(numberValue((Number) value));
		} else if (value instanceof Date) {
			return Maybe.<ModelName> toMaybe(ReferenceInstantiator.dateValue((Date) value));
		} else if (value instanceof Enum<?>) {
			return Maybe.<ModelName> toMaybe(ReferenceInstantiator.enumValue((Enum<?>) value));
		} else if (value instanceof Collection) {
			List<ModelName> listOfValueRefs = ModelResolver.buildModelNames(valueContext, (Collection<?>) value);
			return Maybe.<ModelName> toMaybe(ReferenceInstantiator.listValue(listOfValueRefs));
		} else if (value instanceof Map) {
			return Maybe.<ModelName> toMaybe(referenceMapValue((Map<?, ?>) value, valueContext));
		} else {
			return Maybe.none();
		}
	}

	/**
	 * @see MapNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	private static ModelName referenceMapValue(Map<?, ?> map, Object valueContext) {
		MapValue mapValue = TypedConfiguration.newConfigItem(MapValue.class);
		List<MapValue.MapEntryValue> entries = new ArrayList<>(map.size());
		for (Entry<?, ?> entry : map.entrySet()) {
			entries.add(referenceMapEntry(entry, valueContext));
		}
		mapValue.setEntries(entries);
		return mapValue;
	}

	private static MapValue.MapEntryValue referenceMapEntry(Entry<?, ?> entry, Object valueContext) {
		MapValue.MapEntryValue entryValue = TypedConfiguration.newConfigItem(MapValue.MapEntryValue.class);
		entryValue.setKey(ModelResolver.buildModelName(valueContext, entry.getKey()));
		entryValue.setValue(ModelResolver.buildModelName(valueContext, entry.getValue()));
		return entryValue;
	}

	private static Maybe<ObjectRef> objectRef(Wrapper wrapper) {
		{
			if ((wrapper == null) || isFakeWrapper(wrapper)) {
				return Maybe.none();
			}

			try {
				Class<?> structureType = Class.forName("com.top_logic.element.structured.StructuredElement");
				if (structureType.isAssignableFrom(wrapper.getClass())) {
					KnowledgeObject knowledgeObject = wrapper.tHandle();
					String structureName = (String) knowledgeObject.getAttributeValue("structureName");
					Maybe<List<String>> structurePath = buildStructurePath(knowledgeObject);
					if (!structurePath.hasValue()) {
						return Maybe.none();
					}

					NodeRef nodeRef = ReferenceInstantiator.nodeRef(structureName, structurePath.get(), wrapper);
					return Maybe.<ObjectRef> toMaybe(nodeRef);
				}
			} catch (NoSuchAttributeException exception) {
				String message = "Reference building failed! Attribute not found." + exception.getMessage();
				throw new RuntimeException(message, exception);
			} catch (InvalidLinkException exception) {
				String message = "Reference building failed! Invalid link." + exception.getMessage();
				throw new RuntimeException(message, exception);
			} catch (ClassNotFoundException ex) {
				// No element module linked to the application, continue.
			}

			Maybe<ObjectRef> configuredIndexRef = createConfiguredIndexRef(wrapper);
			if (configuredIndexRef.hasValue()) {
				return configuredIndexRef;
			}
			
			MOClass objectType = (MOClass) wrapper.tHandle().tTable();
			for (MOIndex moIndex : objectType.getIndexes()) {
				Maybe<ObjectRef> indexRef = createIndexRef(wrapper, moIndex);
				if (indexRef.hasValue()) {
					return indexRef;
				}
			}
			
			return Maybe.none();
		}
	}

	private static Maybe<ObjectRef> createConfiguredIndexRef(Wrapper wrapper) {
		Set<String> keyAttributeNames = getConfiguredKeyAttributes(wrapper);
		if (CollectionUtil.isEmptyOrNull(keyAttributeNames)) {
			return Maybe.none();
		}
		return Maybe.<ObjectRef> toMaybe(createIndexRef(wrapper, keyAttributeNames));
	}

	private static Set<String> getConfiguredKeyAttributes(Wrapper wrapper) {
		Set<String> keyAttributeNames = getConfiguredKeyAttributesFromMetaObject(wrapper);
		if (!CollectionUtil.isEmptyOrNull(keyAttributeNames)) {
			return keyAttributeNames;
		}
		if (CompatibilityService.getInstance().isAttributed(wrapper)) {
			return getConfiguredKeyAttributesFromMetaElement(wrapper);
		}
		return null;
	}

	private static Set<String> getConfiguredKeyAttributesFromMetaObject(Wrapper wrapper) {
		MOClass objectType = (MOClass) wrapper.tHandle().tTable();
		return wrapperKeyAttributesProvider.get(objectType.getName());
	}

	private static Set<String> getConfiguredKeyAttributesFromMetaElement(Wrapper wrapper) {
		String metaElementTypeName = CompatibilityService.getInstance().getTypeName(wrapper);
		return wrapperKeyAttributesProvider.get(metaElementTypeName);
	}

	private static Maybe<ObjectRef> createIndexRef(Wrapper wrapper, MOIndex moIndex) {
		MOClass objectType = (MOClass) wrapper.tHandle().tTable();
		if (objectType.getPrimaryKey() == moIndex) {
			// The primary key could be generated.
			// But we search something stable and not generated.
			return Maybe.none();
		}

		if (!moIndex.isUnique()) {
			return Maybe.none();
		}

		Set<String> attributeNames = getAttributeNames(moIndex);
		return Maybe.<ObjectRef> toMaybe(createIndexRef(wrapper, attributeNames));
	}

	private static Set<String> getAttributeNames(MOIndex moIndex) {
		Set<String> attributeNames = new HashSet<>();
		for (MOAttribute attribute : moIndex.getAttributes()) {
			attributeNames.add(attribute.getName());
		}
		return attributeNames;
	}

	/**
	 * Some applications use fake wrapper: Transient objects that implement the {@link Wrapper}
	 * interface. Those objects cannot be referenced with a generic approach.
	 */
	private static boolean isFakeWrapper(Wrapper wrapper) {
		return wrapper.tHandle() == null;
	}

	private static IndexRef createIndexRef(Wrapper wrapper, Set<String> keyNames) {
		Map<String, AttributeValue> keyValues = new HashMap<>();
		for (String name : keyNames) {
			Object value = wrapper.getValue(name);
			ModelName valueRef = ModelResolver.buildModelName(value);

			AttributeValue attributeValueRef = ReferenceInstantiator.attributeValue(name, valueRef);
			keyValues.put(name, attributeValueRef);
		}
		String metaObjectName = wrapper.tTable().getName();

		IndexRef indexRef = ReferenceInstantiator.indexRef(metaObjectName, keyValues, wrapper);

		return indexRef;
	}

	/**
	 * The root node is not added to the path, since the root node is identified by the empty path.
	 */
	private static Maybe<List<String>> buildStructurePath(KnowledgeObject node)
			throws NoSuchAttributeException, InvalidLinkException {
		List<String> path = new ArrayList<>();
		KnowledgeObject currentNode = node;
		KnowledgeObject currentParent =
			ApplicationObjectUtil.navigateBackwards(currentNode, ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION);
		while (currentParent != null) {
			String nodeName = (String) currentNode.getAttributeValue("name");

			boolean resolvingSucceeded = checkNodeResolvability(currentNode, nodeName);
			if (!resolvingSucceeded) {
				return Maybe.none();
			}

			path.add(0, nodeName);
			currentNode = currentParent;
			currentParent =
				ApplicationObjectUtil.navigateBackwards(currentParent,
					ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION);
		}

		if (!isSingleton(currentNode.getWrapper())) {
			return Maybe.none();
		}

		return Maybe.some(path);
	}

	/**
	 * Whether the given object is a model singleton.
	 */
	public static boolean isSingleton(TLObject currentNode) {
		for (TLModuleSingleton link : currentNode.tType().getModel().getQuery(TLModuleSingletons.class).getAllSingletons()) {
			if (link.getSingleton() == currentNode) {
				return true;
			}
		}
		return false;
	}

	private static boolean checkNodeResolvability(KnowledgeObject node, String nodeName)
			throws InvalidLinkException {
		KnowledgeObject parent =
			ApplicationObjectUtil.navigateBackwards(node, ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION);
		Maybe<KnowledgeObject> resolvedNode = ApplicationObjectUtil.getChild(parent, nodeName);
		if (!resolvedNode.hasValue()) {
			return false;
		}
		if (!resolvedNode.get().equals(node)) {
			String errorMessage =
				"Failed to build a resolvable structure path. Resolving the path resulted in a different object.";
			Logger.error(errorMessage, ReferenceFactory.class);
			return false;
		}
		return true;
	}

	/**
	 * Creates a a path of {@link FieldRef}s to the {@link FormMember}.
	 * 
	 * @param field
	 *        Must not be <code>null</code>.
	 * @return Is never <code>null</code>.
	 */
	public static List<FieldRef> referenceField(FormMember field) {
		return createPathToField(field);
	}

	static class FieldPathBuilder {

		private final List<FieldRef> path = new ArrayList<>();

		public void add(final FormMember source, FieldRef fieldRef) {
			if (source.hasLabel()) {
				String label = Resources.getInstance().getString(source.getLabel());
				fieldRef.setFieldLabelComment(label);
			}
			path.add(fieldRef);
		}

		public List<FieldRef> getPath() {
			return path;
		}

	}

	/**
	 * The path to the field in descending order. (Outermost parent first)
	 */
	private static List<FieldRef> createPathToField(FormMember field) {
		FieldPathBuilder fieldPath = new FieldPathBuilder();
		List<FormMember> parentsOfField = getParents(field);
		Collections.reverse(parentsOfField);

		for (int index = 0; index < parentsOfField.size(); index++) {
			FormMember formMember = parentsOfField.get(index);
			if (isSpecialCaseTableCell(formMember)) {
				List<FormMember> children = parentsOfField.subList(index, parentsOfField.size());
				repairReferenceToTableCell(children, fieldPath);
			} else {
				fieldPath.add(formMember, createPathElement(formMember));
			}
		}
		return fieldPath.getPath();
	}

	/**
	 * Returns the list of parents including the field itself but without the root in ascending
	 * order. If the field is null, returns a (modifiable) empty list.
	 * 
	 * @param field
	 *        Is allowed to be <code>null</code>.
	 */
	private static List<FormMember> getParents(FormMember field) {
		if (field == null) {
			// Not the singleton empty list, as the caller may want to add elements later.
			return new ArrayList<>(0);
		}
		List<FormMember> parentsIncludingSelf = new ArrayList<>();
		parentsIncludingSelf.add(field);
		FormContainer parent = field.getParent();
		while (parent != null) {
			parentsIncludingSelf.add(parent);
			parent = parent.getParent();
		}
		parentsIncludingSelf.remove(parentsIncludingSelf.size() - 1);
		return parentsIncludingSelf;
	}

	private static boolean isSpecialCaseTableCell(FormMember formMember) {
		return formMember.hasStableIdSpecialCaseMarker()
			&& (formMember.getStableIdSpecialCaseMarker() instanceof TableModel);
	}

	/**
	 * Repairs the reference to a table cell by manipulating/repairing the list of
	 * pendingFormMembers and the fieldPath.
	 * 
	 * @param pendingFormMembers
	 *        <i>Out parameter!</i> The FormMembers that have to be processed next, starting with
	 *        the table. Must not be null.
	 * @param fieldPath
	 *        <i>Out parameter!</i> The fieldPath so far, ending with the parent of the table. Must
	 *        not be null.
	 */
	private static void repairReferenceToTableCell(List<? extends FormMember> pendingFormMembers,
			FieldPathBuilder fieldPath) {

		assert pendingFormMembers.size() >= 3 : "Expected at least three pendingFormMembers: The table, the row and the cell. But got only "
			+ pendingFormMembers.size() + " pendingFormMembers: '" + StringServices.toString(pendingFormMembers) + "'";

		createReferenceToTable(pendingFormMembers, fieldPath);
		repairReferenceToCell(pendingFormMembers, fieldPath);
	}

	private static void createReferenceToTable(List<? extends FormMember> pendingFormMembers, FieldPathBuilder fieldPath) {
		final int indexOfTable = 0;
		FormMember table = pendingFormMembers.get(indexOfTable);
		fieldPath.add(table, createNonBusinessObjectPathElement(table));
	}

	private static void repairReferenceToCell(List<? extends FormMember> pendingFormMembers, FieldPathBuilder fieldPath) {
		final int indexOfTableGroup = 0;
		final int indexOfRowGroup = 1;
		final int indexOfCell = 2;

		FormMember tableGroup = pendingFormMembers.get(indexOfTableGroup);
		FormMember rowGroup = pendingFormMembers.get(indexOfRowGroup);
		FormMember cell = pendingFormMembers.get(indexOfCell);

		TableFieldRef referenceToCell = createTableFieldReference(tableGroup, rowGroup, cell);
		fieldPath.add(tableGroup, referenceToCell);

		// Keep fieldPath and pendingFormMembers in sync.
		pendingFormMembers.remove(indexOfCell);
		pendingFormMembers.remove(indexOfRowGroup);
	}

	private static TableFieldRef createTableFieldReference(FormMember tableGroup, FormMember rowGroup, FormMember cell) {
		assert rowGroup.hasStableIdSpecialCaseMarker() : "The row has the stableIdSpecialCaseMarker not set!";
		assert cell.hasStableIdSpecialCaseMarker() : "The table entry has the stableIdSpecialCaseMarker not set!";

		String columnName = (String) cell.getStableIdSpecialCaseMarker();
		Object rowObject = rowGroup.getStableIdSpecialCaseMarker();
		Object valueContext = tableGroup.getStableIdSpecialCaseMarker();
		ModelName rowObjectRef = ModelResolver.buildModelName(valueContext, rowObject);

		return ReferenceInstantiator.tableFieldRef(columnName, rowObjectRef);
	}

	private static FieldRef createPathElement(FormMember field) {
		if (field.hasStableIdSpecialCaseMarker()) {
			Object substituteId = field.getStableIdSpecialCaseMarker();
			return ReferenceInstantiator.businessObjectFieldRef(ModelResolver.buildModelName(field.getParent(), substituteId));
		}
		return createNonBusinessObjectPathElement(field);
	}

	private static FieldRef createNonBusinessObjectPathElement(FormMember field) {
		if (field.getParent() instanceof FormTree) {
			Maybe<FieldRef> formTreePath = createFormTreePath(field);
			if (formTreePath.hasValue()) {
				return formTreePath.get();
			}
		}
		
		String fieldName = field.getName();
		
		AttributeIdParser attributeIdParser = AttributeIdParser.newAttributeIdParser(fieldName);
		maSearch:
		if (attributeIdParser.isAttributeIdParsable()) {
			try {
				TLID attributeId = IdentifierUtil.fromExternalForm(attributeIdParser.getAttributeId());
				KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
				KnowledgeObject attribute =
					kb.getKnowledgeObject(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE, attributeId);
				if (attribute == null) {
					break maSearch;
				}
				String attributeName = (String) attribute.getAttributeValue(AbstractWrapper.NAME_ATTRIBUTE);
				
				KnowledgeObject type =
					(KnowledgeObject) attribute.getAttributeValue(ApplicationObjectUtil.META_ELEMENT_ATTR);
				String typeName = (String) type.getAttributeValue(ApplicationObjectUtil.META_ELEMENT_ME_TYPE_ATTR);
				
				KnowledgeObject scope = ApplicationObjectUtil.getScope(type);
				ModelName scopeRef = knowledgeObjectRef(scope);
				
				ModelName selfRef;
				if (attributeIdParser.isObjectIdParsable()) {
					KnowledgeObject self = getAttributedKO(field);
					selfRef = knowledgeObjectRef(self);
				} else {
					selfRef = null;
				}
				
				AttributeRef attributeRef = attributeRef(scopeRef, typeName, attributeName);
				
				AttributeFieldRef attributeFieldRef = ReferenceInstantiator.attributeFieldRef(selfRef, attributeRef);
				
				return attributeFieldRef;
			} catch (NoSuchAttributeException ex) {
				Logger.error("Attribute not found.", ex, DefaultScriptingRecorder.class);
			}
		}
		
		return ReferenceInstantiator.namedFieldRef(fieldName);
	}

	/**
	 * Reflection variant of method in AttributeFormFactory.
	 */
	public static KnowledgeObject getAttributedKO(FormMember field) {
		try {
			Class<?> attributeFormFactory = Class.forName("com.top_logic.element.meta.form.AttributeFormFactory");
			Method getAttributeKO = attributeFormFactory.getMethod("getAttributedKO", new Class[] { FormMember.class });
			return (KnowledgeObject) getAttributeKO.invoke(null, field);
		} catch (Exception ex) {
			throw (AssertionError) new AssertionError("TLObject resolution for field '" + field.getQualifiedName()
				+ "' failed.").initCause(ex);
		}
	}

	private static ModelName knowledgeObjectRef(KnowledgeObject ko) {
		return ModelResolver.buildModelName(WrapperFactory.getWrapper(ko));
	}

	private static Maybe<FieldRef> createFormTreePath(FormMember field) {
		FormTree parentTree = (FormTree) field.getParent();
		Object currentNode = parentTree.getNode(field);
		Maybe<? extends ModelName> potentialValueRef =
			ValueScopeFactory.referenceContextLocalValue(parentTree, currentNode);
		if (potentialValueRef.hasValue()) {
			ModelName valueRef = potentialValueRef.get();
			List<ModelName> treeValuePath;
			if (valueRef instanceof CompactLabelPath) {
				List<String> labelPath = ((CompactLabelPath) valueRef).getLabelPath();
				treeValuePath = new ArrayList<>(labelPath.size());
				for (String label : labelPath) {
					LabeledValue labeledValue = TypedConfiguration.newConfigItem(LabeledValue.class);
					labeledValue.setLabel(label);
					treeValuePath.add(labeledValue);
				}
			} else {
				treeValuePath = ((ValuePath) valueRef).getNodes();
			}
			return Maybe.<FieldRef> some(ReferenceInstantiator.treeFieldRef(treeValuePath));
		} else {
			TLTreeModel treeModel = parentTree.getTreeApplicationModel();
			ResourceProvider resourceProvider = ScriptingUtil.getResourceProvider(parentTree);
			Maybe<List<String>> treePath = ScriptingUtil.createTreeLabelPath(currentNode, treeModel, resourceProvider);
			if (!treePath.hasValue()) {
				return Maybe.none();
			}
			List<ModelName> treePathRef = ModelResolver.buildModelNames(treePath.get());
			return Maybe.<FieldRef> some(ReferenceInstantiator.treeFieldRef(treePathRef));
		}
	}

	/**
	 * @param arguments
	 *        Must not be <code>null</code>.
	 * @return Is never <code>null</code>.
	 */
	public static Map<String, AttributeValue> attributeValues(Map<String, ?> arguments) {
		Map<String, AttributeValue> attributeValues = MapUtil.newMap(arguments.size());
		for (Map.Entry<String, ?> entry : arguments.entrySet()) {
			String name = entry.getKey();
			if (ignoreArgumentKey(name)) {
				continue;
			}
			Object value = entry.getValue();
			ModelName valueRef = ModelResolver.buildModelName(value);

			AttributeValue attributeValue = ReferenceInstantiator.attributeValue(name, valueRef);
			attributeValues.put(name, attributeValue);
		}
		return attributeValues;
	}

	private static boolean ignoreArgumentKey(String name) {
		if (ComponentCommand.COMMAND_MODEL_KEY.equals(name)) {
			// technical key to made command model accessible during CommandHandler execution.
			return true;
		}
		return false;
	}

	/**
	 * Creates a {@link SelectionRef}.
	 * 
	 * @param target
	 *        See {@link SelectionRef#getSelectee()}
	 * @param selectionState
	 *        See {@link SelectionRef#getSelectionState()}
	 * @param valueContext
	 *        See {@link ValueResolver#ValueResolver(Object)}.
	 */
	public static SelectionRef selectionRef(Object target, boolean selectionState, Object valueContext) {
		ModelName selectedObjectRef = ModelResolver.buildModelName(valueContext, target);
		BooleanValue selectionStateRef = ReferenceInstantiator.booleanValue(selectionState);
		SelectionRef selectionRef = ReferenceInstantiator.selectionRef(selectedObjectRef, selectionStateRef);
		return selectionRef;
	}

	/** Tries to find the {@link FormHandler} of the {@link FormField} */
	public static Maybe<ModelName> getFormHandlerName(FormMember field) {
		FormHandler formHandlerOfField = FormComponent.formHandlerForMember(field);
		if (formHandlerOfField == null) {
			return Maybe.none();
		}
		return Maybe.toMaybe(formHandlerOfField.getModelName());
	}

	/**
	 * A local value is one that can only be resolved by comparing its name or label against all
	 * values in its scope.
	 */
	public static Maybe<? extends ContextRef> localValue(Object value, LabelProvider labelProvider) {
		Maybe<? extends ContextRef> namedValue = ValueNamingScheme.namedValue(NamedValue.class, value);
		if (namedValue.hasValue()) {
			return namedValue;
		}
		String label = getLabel(value, labelProvider);
		if (label == null) {
			return Maybe.none();
		}
		return Maybe.some(ReferenceInstantiator.labeledValue(label));
	}

	private static String getLabel(Object value, LabelProvider labelProvider) {
		if (labelProvider != null) {
			String label = labelProvider.getLabel(value);
			if (label != null) {
				return label;
			}
		}
		return FuzzyValueFormat.toLabel(value);
	}

	/**
	 * Updates the {@link ThreadLocal} state that a {@link ModelName} or a corresponding
	 * {@link ModelName} is currently built.
	 * 
	 * <p>
	 * If building is currently active, no redirections from {@link ModelName} to {@link ModelName}
	 * or vice versa must be created to prevent endless recursion.
	 * </p>
	 * 
	 * @param state
	 *        The new state.
	 * @return The previous state.
	 */
	public static Boolean buildingModelName(Boolean state) {
		Boolean before = BUILDING.get();
		if (state == null) {
			BUILDING.remove();
		} else {
			BUILDING.set(state);
		}
		return before;
	}

}
