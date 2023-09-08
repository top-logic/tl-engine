/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.URLBinaryData;
import com.top_logic.basic.util.Computation;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.decorator.ChangeInfo;
import com.top_logic.layout.form.decorator.CompareInfo;
import com.top_logic.layout.form.decorator.CompareService;
import com.top_logic.layout.form.decorator.DecorateInfo;
import com.top_logic.layout.form.decorator.DecorateService;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.action.CurrentFieldOptions;
import com.top_logic.layout.scripting.action.CurrentFieldValue;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.TableCellFullTextAssertionPlugin;
import com.top_logic.layout.scripting.recorder.ref.calc.NotRef;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeValue;
import com.top_logic.layout.scripting.recorder.ref.value.AllTableRows;
import com.top_logic.layout.scripting.recorder.ref.value.BooleanValue;
import com.top_logic.layout.scripting.recorder.ref.value.CellChangeInfo;
import com.top_logic.layout.scripting.recorder.ref.value.CellCompareValue;
import com.top_logic.layout.scripting.recorder.ref.value.DateValue;
import com.top_logic.layout.scripting.recorder.ref.value.DisplayedTableRows;
import com.top_logic.layout.scripting.recorder.ref.value.DoubleValue;
import com.top_logic.layout.scripting.recorder.ref.value.EnumValue;
import com.top_logic.layout.scripting.recorder.ref.value.FieldAspect;
import com.top_logic.layout.scripting.recorder.ref.value.FieldChangeInfo;
import com.top_logic.layout.scripting.recorder.ref.value.FieldCompareValue;
import com.top_logic.layout.scripting.recorder.ref.value.FieldValueAccess;
import com.top_logic.layout.scripting.recorder.ref.value.FloatValue;
import com.top_logic.layout.scripting.recorder.ref.value.IndexedTableValue;
import com.top_logic.layout.scripting.recorder.ref.value.IntValue;
import com.top_logic.layout.scripting.recorder.ref.value.ListValue;
import com.top_logic.layout.scripting.recorder.ref.value.LongValue;
import com.top_logic.layout.scripting.recorder.ref.value.MapValue;
import com.top_logic.layout.scripting.recorder.ref.value.NullValue;
import com.top_logic.layout.scripting.recorder.ref.value.RowOfObject;
import com.top_logic.layout.scripting.recorder.ref.value.RowTableValue;
import com.top_logic.layout.scripting.recorder.ref.value.StringValue;
import com.top_logic.layout.scripting.recorder.ref.value.TableAspect;
import com.top_logic.layout.scripting.recorder.ref.value.TableDisplayValue;
import com.top_logic.layout.scripting.recorder.ref.value.TableSelection;
import com.top_logic.layout.scripting.recorder.ref.value.TreeNodeChildCountRef;
import com.top_logic.layout.scripting.recorder.ref.value.TreeNodeExpansionRef;
import com.top_logic.layout.scripting.recorder.ref.value.TreeNodeLeafRef;
import com.top_logic.layout.scripting.recorder.ref.value.TreeNodeSelectionRef;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.recorder.ref.value.expressions.SingletonValue;
import com.top_logic.layout.scripting.recorder.ref.value.object.ContextRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.FormattedValue;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.IdRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.IndexRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.LabeledValue;
import com.top_logic.layout.scripting.recorder.ref.value.object.NamedModelRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.NodeRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.ObjectAttribute;
import com.top_logic.layout.scripting.recorder.ref.value.object.ObjectAttributeByLabel;
import com.top_logic.layout.scripting.recorder.ref.value.object.SelectedModelRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.TypeRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.VersionedObjectRef;
import com.top_logic.layout.scripting.recorder.ref.value.table.TableCellFullText;
import com.top_logic.layout.scripting.recorder.ref.value.table.TableColumnLabel;
import com.top_logic.layout.scripting.recorder.specialcases.ValueScope;
import com.top_logic.layout.scripting.recorder.specialcases.ValueScopeFactory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.structure.BrowserWindowControl;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Utils;
import com.top_logic.util.model.CompatibilityService;

/**
 * {@link ValueRefVisitor} that resolves a referenced value.
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class ValueResolver implements ValueRefVisitor<Object, ActionContext> {

	private static final Class<?>[] GET_INSTANCE_FOR_STRUCTURE_SIGNATURE = new Class[] { String.class };

	private static final Class<?>[] GET_ROOT_SIGNATURE = new Class[] { Revision.class, Branch.class };

	/**
	 * Is needed to be able to return two things at once: A list of keys and a list of values.
	 * 
	 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
	 */
	private static final class KeyValueTable {

		private final List<String> keys;
		private final List<Object> values;

		KeyValueTable(List<String> keys, List<Object> values) {
			this.keys = keys;
			this.values = values;
		}

		public List<String> getKeys() {
			return keys;
		}

		public List<Object> getValues() {
			return values;
		}
	}

	/**
	 * Has only a default {@link ValueScope}! If you can provide a {@link ValueScope}, don't use
	 * this {@link ValueResolver}, instead use that {@link ValueScope} to create a new
	 * {@link ValueResolver}.
	 */
	public static final ValueResolver DEFAULT = new ValueResolver();

	private final Object _valueContext;

	/**
	 * Creates a new {@link ValueResolver} with a default {@link ValueScope}. Use the
	 * {@link #DEFAULT} constant instead. If you can provide a {@link ValueScope}, use
	 * {@link #ValueResolver(ValueScope)} instead to create a new {@link ValueResolver}.
	 */
	public ValueResolver() {
		this(null);
	}

	/**
	 * Creates a new {@link ValueResolver} for the given context.
	 * 
	 * @param valueContext
	 *        The context in which the value is referenced. <code>null</code> means no (global)
	 *        context. For a {@link ValueResolver} for the global context, use the {@link #DEFAULT}
	 *        constant.
	 * 
	 * @see ValueScopeFactory#referenceContextLocalValue(Object, Object)
	 */
	public ValueResolver(Object valueContext) {
		_valueContext = valueContext;
	}

	@Override
	public Object visitBooleanValue(BooleanValue value, ActionContext arg) {
		return Boolean.valueOf(value.getBoolean());
	}

	@Override
	public Object visitDateValue(DateValue value, ActionContext arg) {
		return value.getDate();
	}

	@Override
	public Object visitEnumValue(EnumValue value, ActionContext arg) {
		return value.getEnum();
	}

	@Override
	public Object visitDoubleValue(DoubleValue value, ActionContext arg) {
		return Double.valueOf(value.getDouble());
	}

	@Override
	public Object visitFloatValue(FloatValue value, ActionContext arg) {
		return Float.valueOf(value.getFloat());
	}

	@Override
	public Object visitIntValue(IntValue value, ActionContext arg) {
		return Integer.valueOf(value.getInt());
	}

	@Override
	public Object visitListValue(ListValue value, ActionContext arg) {
		List<ModelName> references = value.getList();
		ArrayList<Object> result = new ArrayList<>(references.size());
		for (ModelName valueRef : references) {
			result.add(locate(arg, valueRef));
		}
		return result;
	}

	@Override
	public Object visitMapValue(MapValue value, ActionContext arg) {
		List<MapValue.MapEntryValue> entries = value.getEntries();
		Map<Object, Object> result = new LinkedHashMap<>();
		for (MapValue.MapEntryValue entry : entries) {
			Object key = locate(arg, entry.getKey());
			if (result.containsKey(key)) {
				throw ApplicationAssertions.fail(value, "Duplicate key: " + key);
			}
			result.put(key, locate(arg, entry.getValue()));
		}
		return result;
	}

	@Override
	public Object visitSingletonValue(SingletonValue value, ActionContext arg) {
		return CollectionUtil.getSingleValueFrom(locate(arg, value.getCollection()));
	}

	@Override
	public Object visitLongValue(LongValue value, ActionContext arg) {
		return Long.valueOf(value.getLong());
	}

	@Override
	public Object visitStringValue(StringValue value, ActionContext arg) {
		return value.getString();
	}
	
	@Override
	public Object visitNullValue(NullValue value, ActionContext arg) {
		return null;
	}

	@Override
	public Object visitIdRef(IdRef value, ActionContext arg) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		HistoryManager hm = HistoryUtils.getHistoryManager(kb);

		String typeName = value.getTableName();
		MetaObject type;
		try {
			type = kb.getMORepository().getMetaObject(typeName);
		} catch (UnknownTypeException ex) {
			throw ApplicationAssertions.fail(value, "Failed to resolve lookup type.", ex);
		}

		Branch branch = getBranch(arg, value);
		Revision revision = getRevision(arg, value);
		KnowledgeObject ko;
		ko = (KnowledgeObject) hm.getKnowledgeItem(branch, revision, type, value.getObjectName());
		
		return getWrapper(ko);
	}

	@Override
	public Object visitIndexRef(IndexRef value, ActionContext actionContext) {
		Map<String, AttributeValue> keyValues = value.getKeyValues();
		int keySize = keyValues.size();
		String[] attrNames = new String[keySize];
		Object[] attrValues = new Object[keySize];
		
		int n = 0;
		for (AttributeValue attValue : keyValues.values()) {
			attrNames[n] = attValue.getName();
			attrValues[n] = locate(actionContext, attValue.getValue());
			n++;
		}
		
		return findObject(value, actionContext, attrNames, attrValues);
	}

	private TLObject findObject(IndexRef value, ActionContext actionContext, String[] restoredNames,
			Object[] restoredValues) {
		List<KnowledgeItem> kandidates = getCandidates(value, actionContext, restoredNames, restoredValues);
		return findWrapper(kandidates, restoredNames, restoredValues, value);
	}

	private List<KnowledgeItem> getCandidates(IndexRef value, ActionContext actionContext, String[] restoredNames,
			Object[] restoredValues) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try {
			String typeName = value.getTableName();
			MOClass metaObject = (MOClass) kb.getMORepository().getMetaObject(typeName);

			List<String> availableNames = Arrays.asList(metaObject.getAttributeNames());
			KeyValueTable attributesKnownToKO =
				getAttributesKnownToKO(availableNames, Arrays.asList(restoredNames),
					Arrays.asList(restoredValues));
			List<String> attributeNamesKnownToKO = attributesKnownToKO.getKeys();
			List<Object> attributeValuesKnownToKO = attributesKnownToKO.getValues();

			String[] attributeNames = attributeNamesKnownToKO.toArray(new String[attributeNamesKnownToKO.size()]);
			Object[] attributeValues = attributeValuesKnownToKO.toArray();
			for (int i = 0; i < attributeValues.length; i++) {
				if (attributeValues[i] instanceof TLObject) {
					attributeValues[i] = getKnowledgeItem((TLObject) attributeValues[i]);
				}
			}
			return IteratorUtil.toList(kb.getObjectsByAttribute(typeName, attributeNames, attributeValues));
		}
		catch (UnknownTypeException e) {
			throw (AssertionError) new AssertionError("Type not found.").initCause(e);
		} catch (NoSuchAttributeException e) {
			throw (AssertionError) new AssertionError("Index attribute not found.").initCause(e);
		}
	}

	private KnowledgeItem getKnowledgeItem(TLObject wrapper) {
		return wrapper.tHandle();
	}

	private KeyValueTable getAttributesKnownToKO(List<String> attributeOfKO, List<String> attributeNamesKnownToWrapper,
			List<Object> attributeValuesKnownToWrapper) {
		assert(attributeNamesKnownToWrapper.size() == attributeValuesKnownToWrapper.size()) : "Number of names (" + attributeNamesKnownToWrapper.size() + ") and values (" + attributeValuesKnownToWrapper.size() + ") is not equal!";
		
		List<String> attributeNamesKnownToKO = new ArrayList<>();
		List<Object> attributeValuesKnownToKO = new ArrayList<>();
		for (int i = 0; i < attributeNamesKnownToWrapper.size();i++) {
			if (attributeOfKO.contains(attributeNamesKnownToWrapper.get(i))) {
				attributeNamesKnownToKO.add(attributeNamesKnownToWrapper.get(i));
				attributeValuesKnownToKO.add(attributeValuesKnownToWrapper.get(i));
			}
		}
		return new KeyValueTable(attributeNamesKnownToKO, attributeValuesKnownToKO);
	}

	private TLObject findWrapper(List<KnowledgeItem> knowledgeItems, String[] restoredAttributeNames,
			Object[] restoredValues, IndexRef value) {
		assert(restoredAttributeNames.length == restoredValues.length) : "Number of restored Names (" + restoredAttributeNames.length + ") and restored values (" + restoredValues.length + ") is not equal!";
	
		TLObject searchedObject = null;
		List<KnowledgeObject> knowledgeObjects = CollectionUtil.dynamicCastView(KnowledgeObject.class, knowledgeItems);
		for (KnowledgeObject candidate : knowledgeObjects) {
			TLObject wrappedKandidate = getWrapper(candidate);
			if (matchesAllAttributes(wrappedKandidate, restoredAttributeNames, restoredValues)) {
				if (searchedObject != null) {
					throw new AssertionError("More than one object matches the given criteria! First match: '" + searchedObject + "'; Second Match: '" + wrappedKandidate + "'; Criteria: " + value);
				}
				searchedObject = wrappedKandidate;
			}
		}
		if (searchedObject == null) {
			throw new AssertionError("Referenced object not found: " + value);
		}
		return searchedObject;
	}

	private <T extends TLObject> T getWrapper(KnowledgeObject knowledgeObject) {
		return WrapperFactory.getWrapper(knowledgeObject);
	}

	private boolean matchesAllAttributes(TLObject wrapperToCheck, String[] restoredAttributeNames,
			Object[] restoredValues) {
		assert(restoredAttributeNames.length == restoredValues.length) : "Number of restored Names (" + restoredAttributeNames.length + ") and restored values (" + restoredValues.length + ") is not equal!";
		for (int i = 0; i < restoredAttributeNames.length; i++) {
			Object actualValue = wrapperToCheck.tValueByName(restoredAttributeNames[i]);
			if (actualValue instanceof KnowledgeObject) {
				actualValue = ((KnowledgeObject) actualValue).getWrapper();
			}
			if (!fuzzyEquals(actualValue, restoredValues[i])) {
				return false;
			}
		}
		return true;
	}

	private boolean fuzzyEquals(Object actualValue, Object searchedValue) {
		if (Utils.equals(actualValue, searchedValue)) {
			return true;
		}
		/* The script recorder stores all collections as lists. If the actual value is a set, the
		 * searched value was probably a set, too. */
		if ((actualValue instanceof Set) && (searchedValue instanceof Collection)) {
			Set<Object> searchedValueSet = new HashSet<>((Collection<?>) searchedValue);
			return searchedValueSet.equals(actualValue);
		}
		return false;
	}

	/**
	 * @deprecated Use {@link ModelResolver#locateModel(ActionContext, Object, ModelName)} with the
	 *             context argument passed to {@link #ValueResolver(Object)}.
	 */
	@Deprecated
	public Object resolveValue(final ModelName ref, final ActionContext actionContext) {
		return locate(actionContext, ref);
	}

	/**
	 * @param ref
	 *        Can be <code>null</code> in which case null is returned.
	 * @return Can be <code>null</code>.
	 */
	public Object legacyResolveValueRef(final ValueRef ref, final ActionContext actionContext) {
		if (ref == null) {
			return null;
		}
		return ScriptingRecorder.withResourceInspection(new Computation<Object>() {
			@Override
			public Object run() {
				return ref.visit(ValueResolver.this, actionContext);
			}
		});
	}

	/**
	 * Resolves the {@link ModelName}s in the {@link Collection}. All resolved elements are
	 * guaranteed to have the type represented by the <code>expectedValueType</code>, or null.
	 * 
	 * @deprecated Use {@link ModelResolver#locateModels(ActionContext, Class, Object, Collection)}
	 *             with the context argument passed to {@link #ValueResolver(Object)}.
	 * 
	 * @throws ApplicationAssertion
	 *         if one of the resolved values is neither of the given type nor null.
	 */
	@Deprecated
	public <T> List<T> resolveCollection(Collection<ModelName> collection, ActionContext actionContext,
			Class<T> expectedValueType) {
		return ModelResolver.locateModels(actionContext, expectedValueType, _valueContext, collection);
	}

	@Override
	public Object visitNodeRef(NodeRef value, ActionContext arg) {
		try {
			// As the StructuredElementFactory is in the "Element" project, it is not available here
			// and we have to use reflection when using it.
			Class<?> structuredElementFactory =
				Class.forName("com.top_logic.element.structured.StructuredElementFactory");
			
			Method getInstanceForStructure =
				structuredElementFactory.getMethod("getInstanceForStructure", GET_INSTANCE_FOR_STRUCTURE_SIGNATURE);
			Object factory = getInstanceForStructure.invoke(null, value.getStructureName());
			Method getRoot = structuredElementFactory.getMethod("getRoot", GET_ROOT_SIGNATURE);

			Revision revision = getRevision(arg, value);
			Branch branch = getBranch(arg, value);
			TLObject node = (TLObject) getRoot.invoke(factory, revision, branch);
			
			List<String> nodeNames = value.getStructurePath();
			for (String name : nodeNames) {
				Maybe<TLObject> child = ApplicationObjectUtil.getChild(node, name);
				if (!child.hasValue()) {
					throw new RuntimeException("Resolving structure node failed. Searched for a child named '" + name
						+ "' in the node: " + StringServices.getObjectDescription(node));
				}
				node = child.get();
			}
			return node;
		} catch (Exception e) {
			throw (AssertionError) new AssertionError("Resolving structure node failed.").initCause(e);
		}
	}

	@Override
	public Object visitTypeRef(TypeRef value, ActionContext arg) {
		try {
			KnowledgeObject typeKo;
			
			ModelName scopeRef = value.getScopeRef();
			if ((scopeRef == null) || (scopeRef instanceof NullValue)) {
				typeKo = ApplicationObjectUtil.findGlobalType(PersistencyLayer.getKnowledgeBase(), value.getTypeName());
			} else {
				TLObject scope = (TLObject) locate(arg, scopeRef);
				KnowledgeObject ko = (KnowledgeObject) scope.tHandle();
				typeKo = ApplicationObjectUtil.findType(ko, value.getTypeName());
			}
			
			// IGNORE FindBugs(RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT): For better portability.
			return WrapperFactory.getWrapper(typeKo);
		} catch (UnknownTypeException e) {
			throw (AssertionError) new AssertionError("Type not found.").initCause(e);
		} catch (NoSuchAttributeException e) {
			throw (AssertionError) new AssertionError("Attribute not found.").initCause(e);
		} catch (InvalidLinkException e) {
			throw (AssertionError) new AssertionError("Invalid link.").initCause(e);
		}
	}

	@Override
	public Object visitObjectAttribute(ObjectAttribute value, ActionContext arg) {
		TLObject target = (TLObject) locate(arg, value.getTarget());
		if (target == null) {
			throw ApplicationAssertions.fail(value, "Object reference is null.");
		}
		String attributeName = value.getAttribute();
		return target.tValueByName(attributeName);
	}

	@Override
	public Object visitObjectAttributeByLabel(ObjectAttributeByLabel value, ActionContext arg) {
		TLObject target = (TLObject) locate(arg, value.getTarget());
		if (target == null) {
			throw ApplicationAssertions.fail(value, "Object reference is null.");
		}
		String attributeLabelRaw = value.getAttributeLabel();
		String attributeLabel = value.isFuzzy() ? fuzzyNormalize(attributeLabelRaw) : attributeLabelRaw;
		String attributeName = findAttributeByLabel(value, target, attributeLabel);
		return target.tValueByName(attributeName);
	}

	private String findAttributeByLabel(ObjectAttributeByLabel value, TLObject target, String label) {
		CompatibilityService compatibilityService = CompatibilityService.getInstance();
		TLClass type = (TLClass) target.tType();
		String attributeName = null;
		for (TLStructuredTypePart attribute : type.getAllParts()) {
			if (matches(label, attribute, value.isFuzzy())) {
				if (attributeName != null) {
					throw ApplicationAssertions.fail(value, "Ambiguous attribute match for label '" + label
						+ "': " + attributeName + " vs. " + attribute.getName());
				}
				attributeName = attribute.getName();
			}
		}

		if (attributeName == null) {
			throw ApplicationAssertions.fail(value, "No such attribute '" + label + "' in object '" + target
				+ "'.");
		}
		return attributeName;
	}

	private boolean matches(String searchedLabel, TLStructuredTypePart attribute, boolean fuzzy) {
		String currentLabelRaw = MetaLabelProvider.INSTANCE.getLabel(attribute);
		String currentLabel = fuzzy ? fuzzyNormalize(currentLabelRaw) : currentLabelRaw;
		return searchedLabel.equals(currentLabel);
	}

	private String fuzzyNormalize(String attributeLabel) {
		return attributeLabel.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue")
			.replace("Ä", "Ae").replace("Ö", "Oe").replace("Ü", "Ue")
			.replace("ß", "ss").replaceAll("[^A-Za-z0-9]+", "");
	}

	@Override
	public Object visitContextRef(ContextRef contextRef, ActionContext arg) {
		Maybe<Object> resolvedValue = ValueScopeFactory.resolveContextLocalValue(arg, _valueContext, contextRef);
		if (!resolvedValue.hasValue()) {
			String errorMessage =
				"Resolving context local value reference failed! Reference: "
					+ StringServices.getObjectDescription(contextRef) + "; Context: "
					+ StringServices.getObjectDescription(_valueContext);
			throw new RuntimeException(errorMessage);
		}
		return resolvedValue.get();
	}

	@Override
	public Object visitNamedModelRef(NamedModelRef namedModelRef, ActionContext arg) {
		ModelName modelName = namedModelRef.getModelName();
		return locate(arg, modelName);
	}

	@Override
	public Object visitRowTableValue(RowTableValue value, ActionContext actionContext) {
		Object cellValue = rawCellValue(value, actionContext);
		if (cellValue instanceof CompareInfo) {
			cellValue = ((CompareInfo) cellValue).getDisplayedObject();
		}
		return cellValue;
	}

	private Object rawCellValue(RowTableValue value, ActionContext actionContext) {
		TableData tableData = (TableData) locate(actionContext, value.getTable());
		Object rowObject = actionContext.resolve(value.getRowObject(), tableData);
		String columnName = ScriptTableUtil.getColumnName(value, tableData);
		return getCellValue(value, tableData, rowObject, columnName);
	}

	@Override
	public Object visitCellChangeInfo(CellChangeInfo value, ActionContext actionContext) {
		Object rawCellValue = rawCellValue(value, actionContext);
		if (!(rawCellValue instanceof CompareInfo)) {
			throw ApplicationAssertions.fail(value, "No Compare info as cell object.");
		}
		return ((CompareInfo) rawCellValue).getChangeInfo();
	}

	@Override
	public Object visitCellCompareValue(CellCompareValue value, ActionContext actionContext) {
		Object rawCellValue = rawCellValue(value, actionContext);
		if (!(rawCellValue instanceof CompareInfo)) {
			throw ApplicationAssertions.fail(value, "No Compare info as cell object.");
		}
		CompareInfo compareInfo = (CompareInfo) rawCellValue;
		if (compareInfo.getChangeInfo() == ChangeInfo.REMOVED) {
			return compareInfo.getChangeValue();
		} else {
			return compareInfo.getBaseValue();
		}
	}

	@Override
	public Object visitTableCellFullText(TableCellFullText value, ActionContext actionContext) {
		TableData tableData = (TableData) locate(actionContext, value.getTable());
		Object rowObject = actionContext.resolve(value.getRowObject(), tableData);
		String columnName = ScriptTableUtil.getColumnName(value, tableData);
		Object cellValue = getCellValue(value, tableData, rowObject, columnName);
		return TableCellFullTextAssertionPlugin.getFullText(tableData, columnName, cellValue);
	}

	@Override
	public String visitTableColumnLabel(TableColumnLabel value, ActionContext actionContext) {
		TableData tableData = (TableData) locate(actionContext, value.getTable());
		String columnName = ScriptTableUtil.getColumnName(value, tableData);
		return ScriptTableUtil.getColumnLabel(columnName, tableData);
	}

	private Object getCellValue(TableAspect value, TableData tableData, Object rowObject, String columnName) {
		checkRow(value, tableData, rowObject);
		TableModel tableModel = tableData.getTableModel();
		int columnIndex = tableModel.getColumnIndex(columnName);
		checkColumn(value, columnName, columnIndex);
		int rowIndex = tableModel.getRowOfObject(rowObject);
		return tableModel.getValueAt(rowIndex, columnIndex);
	}

	private void checkRow(TableAspect value, TableData table, Object row) {
		ApplicationAssertions.assertNotNull(value, "Table row object not found.", row);
		ApplicationAssertions.assertTrue(value, "Object is not in table: " + StringServices.getObjectDescription(row),
			table.getTableModel().containsRowObject(row));
		ApplicationAssertions.assertTrue(value,
			"Object is not displayed: " + StringServices.getObjectDescription(row),
			table.getViewModel().getRowOfObject(row) >= 0);
	}

	private void checkColumn(TableAspect value, String columnName, int columnIndex) {
		ApplicationAssertions.assertTrue(value, "No such column '" + columnName + "' in table.",
			columnIndex >= 0);
		// It's not checked whether the column is visible,
		// as the column configuration cannot be scripted, yet.
	}

	@Override
	public Object visitIndexedTableValue(IndexedTableValue value, ActionContext arg) {
		TableData table = (TableData) locate(arg, value.getTable());
		TableModel tableModel = table.getTableModel();

		int column = tableModel.getColumnIndex(ScriptTableUtil.getColumnName(value, table));
		ApplicationAssertions.assertTrue(value, "Table column not found.", column >= 0);
		return tableModel.getValueAt(value.getRowIndex(), column);
	}

	@Override
	public Object visitCurrentFieldValue(CurrentFieldValue value, ActionContext arg) {
		FormMember member = (FormMember) locate(arg, value.getFieldName());
		ApplicationAssertions.assertTrue(value, "Not a field.", member instanceof FormField);

		FormField field = (FormField) member;
		ApplicationAssertions.assertTrue(value, "Field has no value.", field.hasValue());

		if (value.isRawValue()) {
			return field.getRawValue();
		} else {
			return field.getValue();
		}
	}

	@Override
	public Object visitCurrentFieldOptions(CurrentFieldOptions value, ActionContext arg) {
		FormMember member = (FormMember) locate(arg, value.getFieldName());
		ApplicationAssertions.assertTrue(value, "Not a field.", member instanceof FormField);

		FormField field = (FormField) member;
		return SelectFieldUtils.getOptions(field);
	}

	@Override
	public Object visitDataItemValue(DataItemValue value, ActionContext arg) {
		BinaryData data = BinaryDataFactory.createBinaryData(ArrayUtil.EMPTY_BYTE_ARRAY);
		return newDataItem(value, data);
	}

	@Override
	public Object visitBase64Value(Base64Value value, ActionContext arg) {
		BinaryData data = BinaryDataFactory.createBinaryData(Base64.decodeBase64(value.getBase64Data()));
		return newDataItem(value, data);
	}

	@Override
	public Object visitExternalDataValue(ExternalDataValue value, ActionContext arg) {
		try {
			BinaryData data = new URLBinaryData(new URL(value.getUrl()));
			return newDataItem(value, data);
		} catch (MalformedURLException ex) {
			throw ApplicationAssertions.fail(value, "Resolution of URL '" + value.getUrl() + "' failed.", ex);
		}
	}

	private DefaultDataItem newDataItem(DataItemValue value, BinaryData data) {
		return new DefaultDataItem(value.getName(), data, value.getContentType());
	}

	@Override
	public Object visitFileManagerDataValue(FileManagerDataValue value, ActionContext arg) {
		String preAlias = value.getName();
		String postAlias = AliasManager.getInstance().replace(preAlias);
		{
			BinaryData file = FileManager.getInstance().getDataOrNull(postAlias);
			if (file == null) {
				throw ApplicationAssertions.fail(value, "File '" + postAlias + "' not found.");
			}
			String dataName = FileUtilities.getFilenameOfResource(postAlias);
			return new DefaultDataItem(dataName, file, value.getContentType());
		}
	}

	private String createErrorMessagePartFilename(String preAlias, String postAlias, String inTopLevelProject) {
		return createErrorMessagePartFilename(preAlias, postAlias)
			+ " Path to the file if it were in the top-most project: '" + inTopLevelProject + "'.";
	}

	private String createErrorMessagePartFilename(String preAlias, String postAlias) {
		return "File before alias resolution: '" + preAlias + "'. File after alias resolution: '" + postAlias + "'.";
	}

	@Override
	public Object visitDownloadValue(DownloadValue value, ActionContext arg) {
		String fileName = value.getFileName();
		BinaryDataSource result = BrowserWindowControl.Internal.getDownload(
			(BrowserWindowControl) arg.getDisplayContext().getWindowScope(), fileName);
		ApplicationAssertions.assertNotNull(value, "Download is not available.", result);
		return result;
	}

	@Override
	public Object visitDisplayedTableRows(DisplayedTableRows value, ActionContext arg) {
		TableData table = resolveTable(value, arg);
		return table.getTableModel().getDisplayedRows().size();
	}

	private TableData resolveTable(TableAspect value, ActionContext arg) {
		ModelName tableName = value.getTable();
		return to(TableData.class, tableName, locate(arg, tableName));
	}

	@Override
	public Object visitFieldValueAccess(FieldValueAccess value, ActionContext arg) {
		FormField field = resolveField(value, arg);
		return field.getValue();
	}

	@Override
	public Object visitFieldChangeInfo(FieldChangeInfo value, ActionContext arg) {
		FormField changeValueField = resolveField(value, arg);
		CompareService<?> compareService = getCompareService(value, changeValueField);
		FormField baseValueField = getCompareField(value, changeValueField, compareService);
		CompareInfo info =
			compareService.createCompareInfo(baseValueField, changeValueField, DecorateService.getDefaultLabels(changeValueField));
		return info.getChangeInfo();
	}

	private FormField getCompareField(ConfigurationItem value, FormField field, CompareService<?> compareService) {
		FormField compareField = compareService.findBaseValueField(field);
		ApplicationAssertions.assertNotNull(value, "No compare field for " + field + ".", compareField);
		return compareField;
	}

	@Override
	public Object visitFieldCompareValue(FieldCompareValue value, ActionContext arg) {
		FormField field = resolveField(value, arg);
		FormField compareField = getCompareField(value, field, getCompareService(value, field));
		return compareField.getValue();
	}

	private CompareService<?> getCompareService(ConfigurationItem config, FormField field) {
		FormContext formContext = field.getFormContext();
		if (!DecorateService.isDecorated(formContext)) {
			throw ApplicationAssertions.fail(config, "FormContext not decorated.");
		}
		DecorateService<DecorateInfo> decorator = DecorateService.getDecorator(formContext);
		ApplicationAssertions.assertInstanceOf(config, "Unexpected decorator.", CompareService.class, decorator);

		return (CompareService<?>) decorator;
	}

	private FormField resolveField(FieldAspect value, ActionContext arg) {
		ModelName fieldName = value.getField();
		return to(FormField.class, fieldName, locate(arg, fieldName));
	}

	private <T> T to(Class<T> type, ConfigurationItem config, Object value) {
		if (value == null) {
			throw ApplicationAssertions.fail(config, "Unexpected null value.");
		}
		if (!type.isInstance(value)) {
			throw ApplicationAssertions.fail(config, "Unexpected type: Expected '" + type.getName() + "', found '"
				+ value.getClass().getName() + "'.");
		}
		return type.cast(value);
	}

	@Override
	public Object visitAllTableRows(AllTableRows value, ActionContext arg) {
		TableData table = resolveTable(value, arg);
		return table.getTableModel().getAllRows().size();
	}

	@Override
	public Object visitTableSelection(TableSelection value, ActionContext arg) {
		TableData table = resolveTable(value, arg);
		return table.getSelectionModel().getSelection();
	}

	@Override
	public Object visitTableDisplayValue(TableDisplayValue value, ActionContext arg) {
		TableData table = resolveTable(value, arg);
		int rowNum = (Integer) locate(arg, value.getRowNumber());
		int columnIndex = table.getTableModel().getColumnIndex(ScriptTableUtil.getColumnName(value, table));
		return table.getTableModel().getValueAt(rowNum, columnIndex);
	}

	@Override
	public Object visitGlobalVariableRef(GlobalVariableRef variableRef, ActionContext actionContext) {
		String variableSpec = variableRef.getName();

		// Lenient parsing:
		String variableName = variableSpec.startsWith("$") ? variableSpec.substring(1) : variableSpec;

		return actionContext.getGlobalVariableStore().get(variableName);
	}

	/**
	 * Resolves the {@link Branch} encoded into the given {@link VersionedObjectRef}.
	 */
	public static Branch getBranch(ActionContext arg, VersionedObjectRef value) {
		ModelName branchName = value.getBranch();
		if (branchName == null) {
			// Compatibility and shortness.
			return HistoryUtils.getTrunk();
		} else {
			return (Branch) ModelResolver.locateModel(arg, branchName);
		}
	}

	/**
	 * Resolves the {@link Revision} encoded into the given {@link VersionedObjectRef}.
	 */
	public static Revision getRevision(ActionContext arg, VersionedObjectRef value) {
		ModelName revisionName = value.getRevision();
		if (revisionName == null) {
			// Compatibility and shortness.
			return Revision.CURRENT;
		} else {
			return (Revision) ModelResolver.locateModel(arg, revisionName);
		}
	}

	/**
	 * Resolves a {@link LabeledValue} in the given context.
	 * 
	 * @param options
	 *        The options to choose from.
	 * @param labelProvider
	 *        The {@link LabelProvider} that is used to identify values (by label).
	 * @param valueLabel
	 *        The reference to resolve.
	 * 
	 * @return The resolved value.
	 */
	public static Object resolveLabeledValue(ActionContext actionContext, List<?> options, LabelProvider labelProvider,
			LabeledValue valueLabel) {
		return FuzzyValueFormat.resolveLabeledValue(actionContext, valueLabel, options, labelProvider,
			valueLabel.getLabel());
	}

	@Override
	public Object visitFormattedValue(FormattedValue value, ActionContext arg) {
		try {
			return value.getFormat().parse(value.getFormattedValue());
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Object visitSelectedModelValue(SelectedModelRef value, ActionContext arg) {
		Object component = locate(arg, value.getComponent());
		return ((Selectable) component).getSelected();
	}

	@Override
	public Object visitRowOfObjectValue(RowOfObject value, ActionContext arg) {
		TableData table = (TableData) locate(arg, value.getTable());

		Object rowObject = ModelResolver.locateModel(arg, table, value.getRowObject());
		checkRow(value, table, rowObject);

		return table.getViewModel().getRowOfObject(rowObject);
	}

	@Override
	public Object visitTreeNodeExpansion(TreeNodeExpansionRef value, ActionContext actionContext) {
		Object treeNodeContext = actionContext.resolve(value.getContext());
		Object node = actionContext.resolve(value.getNode(), treeNodeContext);
		return toTreeModel(treeNodeContext).isExpanded(node);
	}

	@Override
	public Object visitTreeNodeChildCount(TreeNodeChildCountRef value, ActionContext actionContext) {
		Object treeNodeContext = actionContext.resolve(value.getContext());
		Object node = actionContext.resolve(value.getNode(), treeNodeContext);
		return toTreeModel(treeNodeContext).getChildren(node).size();
	}

	@Override
	public Object visitTreeNodeLeaf(TreeNodeLeafRef value, ActionContext actionContext) {
		Object treeNodeContext = actionContext.resolve(value.getContext());
		Object node = actionContext.resolve(value.getNode(), treeNodeContext);
		return toTreeModel(treeNodeContext).isLeaf(node);
	}

	private TreeUIModel toTreeModel(Object resolve) {
		TreeUIModel treeModel;
		if (resolve instanceof TreeData) {
			TreeData tree = (TreeData) resolve;
			treeModel = tree.getTreeModel();
		} else {
			TableData table = (TableData) resolve;
			treeModel = ((TreeTableModel) table.getTableModel()).getTreeModel();
		}
		return treeModel;
	}

	@Override
	public Object visitTreeNodeSelection(TreeNodeSelectionRef value, ActionContext actionContext) {
		Object treeNodeContext = actionContext.resolve(value.getContext());
		Object node = actionContext.resolve(value.getNode(), treeNodeContext);
		if (treeNodeContext instanceof TreeData) {
			TreeData tree = (TreeData) treeNodeContext;
			return tree.getSelectionModel().isSelected(node);
		} else {
			TableData table = (TableData) treeNodeContext;
			TableViewModel viewModel = table.getViewModel();
			Set<?> selection = table.getSelectionModel().getSelection();
			Object selectedRow = CollectionUtil.getSingleValueFromCollection(selection);
			if (selectedRow != null) {
				return Utils.equals(selectedRow, node);
			}

			return false;
		}
	}

	@Override
	public Object visitNot(NotRef value, ActionContext actionContext) {
		return ! (Boolean) actionContext.resolve(value.getValue());
	}

	private Object locate(ActionContext arg, ModelName valueRef) {
		return ModelResolver.locateModel(arg, _valueContext, valueRef);
	}

}
