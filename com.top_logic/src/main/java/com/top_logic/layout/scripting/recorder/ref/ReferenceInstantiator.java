/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import static com.top_logic.knowledge.wrap.WrapperHistoryUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.component.ComponentBasedNamingScheme.ComponentName;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.action.FormInput;
import com.top_logic.layout.scripting.action.FormRawInput;
import com.top_logic.layout.scripting.recorder.gui.AssertionTreeNode;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.ref.field.AttributeFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.BusinessObjectFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.FieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.NamedFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.TableFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.TreeFieldRef;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeRef;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeValue;
import com.top_logic.layout.scripting.recorder.ref.misc.FieldValue;
import com.top_logic.layout.scripting.recorder.ref.misc.SelectionRef;
import com.top_logic.layout.scripting.recorder.ref.ui.button.ButtonAspectNaming.ButtonAspectName;
import com.top_logic.layout.scripting.recorder.ref.ui.button.LabeledButtonNaming.LabeledButtonName;
import com.top_logic.layout.scripting.recorder.ref.ui.form.FormMemberAspectNaming.FormMemberAspectName;
import com.top_logic.layout.scripting.recorder.ref.ui.table.TableAspectName;
import com.top_logic.layout.scripting.recorder.ref.ui.table.TableColumnAspectNaming.TableColumnAspectName;
import com.top_logic.layout.scripting.recorder.ref.value.AllTableRows;
import com.top_logic.layout.scripting.recorder.ref.value.BooleanNaming;
import com.top_logic.layout.scripting.recorder.ref.value.BooleanValue;
import com.top_logic.layout.scripting.recorder.ref.value.BranchName;
import com.top_logic.layout.scripting.recorder.ref.value.DateNaming;
import com.top_logic.layout.scripting.recorder.ref.value.DateValue;
import com.top_logic.layout.scripting.recorder.ref.value.DisplayedTableRows;
import com.top_logic.layout.scripting.recorder.ref.value.DoubleNaming;
import com.top_logic.layout.scripting.recorder.ref.value.DoubleValue;
import com.top_logic.layout.scripting.recorder.ref.value.EnumNaming;
import com.top_logic.layout.scripting.recorder.ref.value.EnumValue;
import com.top_logic.layout.scripting.recorder.ref.value.FieldAspect;
import com.top_logic.layout.scripting.recorder.ref.value.FloatNaming;
import com.top_logic.layout.scripting.recorder.ref.value.FloatValue;
import com.top_logic.layout.scripting.recorder.ref.value.IntNaming;
import com.top_logic.layout.scripting.recorder.ref.value.IntValue;
import com.top_logic.layout.scripting.recorder.ref.value.ListNaming;
import com.top_logic.layout.scripting.recorder.ref.value.ListValue;
import com.top_logic.layout.scripting.recorder.ref.value.LongNaming;
import com.top_logic.layout.scripting.recorder.ref.value.LongValue;
import com.top_logic.layout.scripting.recorder.ref.value.NullValue;
import com.top_logic.layout.scripting.recorder.ref.value.RevisionName;
import com.top_logic.layout.scripting.recorder.ref.value.RowOfObject;
import com.top_logic.layout.scripting.recorder.ref.value.RowTableValue;
import com.top_logic.layout.scripting.recorder.ref.value.StringNaming;
import com.top_logic.layout.scripting.recorder.ref.value.StringValue;
import com.top_logic.layout.scripting.recorder.ref.value.TableAspect;
import com.top_logic.layout.scripting.recorder.ref.value.TableColumnRef;
import com.top_logic.layout.scripting.recorder.ref.value.TableSelection;
import com.top_logic.layout.scripting.recorder.ref.value.TreeNodeRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.CompactLabelPath;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.IdRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.IndexRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.IndexedObjectNaming;
import com.top_logic.layout.scripting.recorder.ref.value.object.LabeledValue;
import com.top_logic.layout.scripting.recorder.ref.value.object.NodeRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.TypeRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.ValuePath;
import com.top_logic.layout.scripting.recorder.ref.value.object.VersionedObjectRef;
import com.top_logic.layout.scripting.recorder.ref.value.table.TableColumnLabel;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * Creates and fills {@link ModelName}s and {@link FieldRef}s. Should not contain any logic beyond
 * that. Put your convenience methods in the {@link ReferenceFactory}, please.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public class ReferenceInstantiator {

	/**
	 * Creates an {@link IdRef}.
	 */
	public static IdRef idRef(String typeName, TLID objectName, Wrapper versionContext) {
		return setVersionContext(createIdRef(typeName, objectName), versionContext);
	}

	/**
	 * Creates an {@link IdRef}.
	 */
	public static IdRef idRef(String typeName, TLID objectName, ModelName branchName, ModelName revisionName) {
		return setVersionContext(createIdRef(typeName, objectName), branchName, revisionName);
	}

	private static IdRef createIdRef(String typeName, TLID objectName) {
		IdRef item = TypedConfiguration.newConfigItem(IdRef.class);
		item.setTableName(typeName);
		item.setObjectName(objectName);
		return item;
	}

	/**
	 * Creates an {@link AttributeFieldRef} from the given arguments.
	 */
	public static AttributeFieldRef attributeFieldRef(ModelName selfRef, AttributeRef attributeRef) {
		AttributeFieldRef item = TypedConfiguration.newConfigItem(AttributeFieldRef.class);
		item.setSelfRef(selfRef);
		item.setAttributeRef(attributeRef);
		return item;
	}

	/**
	 * @deprecated Use {@link FormInput}, or {@link FormRawInput}.
	 */
	@Deprecated
	public static FieldValue fieldValue(List<? extends FieldRef> fieldPath, ModelName valueRef) {
		FieldValue item = TypedConfiguration.newConfigItem(FieldValue.class);
		item.setFieldPath(fieldPath);
		item.setValue(valueRef);
		return item;
	}

	public static <T extends FieldAspect> T fieldAspect(Class<T> fieldAspectClass, FormField field) {
		T item = TypedConfiguration.newConfigItem(fieldAspectClass);
		item.setField(ModelResolver.buildModelName(field));
		return item;
	}

	public static BusinessObjectFieldRef businessObjectFieldRef(ModelName businessObjectRef) {
		BusinessObjectFieldRef businessObject = TypedConfiguration.newConfigItem(BusinessObjectFieldRef.class);
		businessObject.setBusinessObject(businessObjectRef);
		return businessObject;
	}

	/**
	 * Creates a {@link NamedFieldRef} from the given arguments.
	 */
	public static NamedFieldRef namedFieldRef(String fieldName) {
		NamedFieldRef item = TypedConfiguration.newConfigItem(NamedFieldRef.class);
		item.setFieldName(fieldName);
		return item;
	}

	public static TreeFieldRef treeFieldRef(List<? extends ModelName> treePath) {
		TreeFieldRef item = TypedConfiguration.newConfigItem(TreeFieldRef.class);
		item.setPath(treePath);
		return item;
	}

	public static TableFieldRef tableFieldRef(String columnName, ModelName rowObject) {
		TableFieldRef item = TypedConfiguration.newConfigItem(TableFieldRef.class);
		item.setColumnName(columnName);
		item.setRowObject(rowObject);
		return item;
	}

	/**
	 * Creates a {@link NodeRef}.
	 * 
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}
	 * @see "StructuredElementNaming"
	 */
	@Deprecated
	public static NodeRef nodeRef(String structureName, List<String> structurePath, ModelName branchName,
			ModelName revisionName) {
		NodeRef item = createNodeRef(structureName, structurePath);
		setVersionContext(item, branchName, revisionName);
		return item;
	}

	/**
	 * Creates a {@link NodeRef}.
	 * 
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}
	 * @see "StructuredElementNaming"
	 */
	@Deprecated
	public static NodeRef nodeRef(String structureName, List<String> structurePath, Wrapper versionContext) {
		return setVersionContext(createNodeRef(structureName, structurePath), versionContext);
	}

	private static NodeRef createNodeRef(String structureName, List<String> structurePath) {
		NodeRef item = TypedConfiguration.newConfigItem(NodeRef.class);
		item.setStructureName(structureName);
		item.setStructurePath(structurePath);
		return item;
	}

	public static AttributeRef attributeRef(TypeRef typeRef, String attributeName) {
		AttributeRef item = TypedConfiguration.newConfigItem(AttributeRef.class);
		item.setType(typeRef);
		item.setAttributeName(attributeName);
		return item;
	}

	public static TypeRef typeRef(ModelName scopeRef, String typeName) {
		TypeRef item = TypedConfiguration.newConfigItem(TypeRef.class);
		item.setScopeRef(scopeRef);
		item.setTypeName(typeName);
		return item;
	}

	/**
	 * Creates an {@link IndexRef}.
	 */
	public static IndexRef indexRef(String typeName, Map<String, AttributeValue> keyValues, Wrapper versionContext) {
		return setVersionContext(createIndexRef(typeName, keyValues), versionContext);
	}

	/**
	 * Creates an {@link IndexRef}.
	 */
	public static IndexRef indexRef(String typeName, Map<String, AttributeValue> keyValues, ModelName branchName,
			ModelName revisionName) {
		return setVersionContext(createIndexRef(typeName, keyValues), branchName, revisionName);
	}

	/**
	 * Creates an {@link IndexRef}.
	 * 
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}
	 * @see IndexedObjectNaming
	 */
	@Deprecated
	public static IndexRef indexRef(String typeName, Map<String, AttributeValue> keyValues, Branch branch,
			Revision revision) {
		return setVersionContext(createIndexRef(typeName, keyValues), branch, revision);
	}

	private static IndexRef createIndexRef(String typeName, Map<String, AttributeValue> keyValues) {
		IndexRef item = TypedConfiguration.newConfigItem(IndexRef.class);
		item.setTableName(typeName);
		item.setKeyValues(keyValues);
		return item;
	}

	/**
	 * Creates a {@link BooleanValue} from the given arguments.
	 * 
	 * @see BooleanNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	public static BooleanValue booleanValue(boolean value) {
		BooleanValue item = TypedConfiguration.newConfigItem(BooleanValue.class);
		item.setBoolean(value);
		return item;
	}

	/**
	 * Creates a {@link StringValue} from the given arguments.
	 * 
	 * @see StringNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	public static StringValue stringValue(String value) {
		StringValue item = TypedConfiguration.newConfigItem(StringValue.class);
		item.setString(value);
		return item;
	}

	/**
	 * Creates an {@link IntValue} from the given arguments.
	 * 
	 * @see IntNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	public static IntValue intValue(int value) {
		IntValue item = TypedConfiguration.newConfigItem(IntValue.class);
		item.setInt(value);
		return item;
	}

	/**
	 * Creates an {@link LongValue} from the given arguments.
	 * 
	 * @see LongNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	public static LongValue longValue(long value) {
		LongValue item = TypedConfiguration.newConfigItem(LongValue.class);
		item.setLong(value);
		return item;
	}

	/**
	 * Creates a {@link FloatValue} from the given arguments.
	 * 
	 * @see FloatNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	public static FloatValue floatValue(float value) {
		FloatValue item = TypedConfiguration.newConfigItem(FloatValue.class);
		item.setFloat(value);
		return item;
	}

	/**
	 * Creates a {@link DoubleValue} from the given arguments.
	 * 
	 * @see DoubleNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	public static DoubleValue doubleValue(double value) {
		DoubleValue item = TypedConfiguration.newConfigItem(DoubleValue.class);
		item.setDouble(value);
		return item;
	}

	/**
	 * Creates an {@link DateValue} from the given arguments.
	 * 
	 * @see DateNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	public static DateValue dateValue(Date value) {
		DateValue item = TypedConfiguration.newConfigItem(DateValue.class);
		item.setDate(value);
		return item;
	}

	/**
	 * Creates an {@link EnumValue}.
	 * 
	 * @see EnumNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	public static EnumValue enumValue(Enum<?> value) {
		EnumValue item = TypedConfiguration.newConfigItem(EnumValue.class);
		item.setEnum(value);
		return item;
	}

	/**
	 * Creates an {@link ListValue} from the given arguments.
	 * 
	 * @see ListNaming
	 * @deprecated Use {@link ModelResolver#buildModelName(Object)}.
	 */
	@Deprecated
	public static ListValue listValue(List<? extends ModelName> value) {
		ListValue item = TypedConfiguration.newConfigItem(ListValue.class);
		item.setList(value);
		return item;
	}

	/**
	 * Creates an {@link AttributeValue}.
	 */
	public static AttributeValue attributeValue(String name, ModelName value) {
		AttributeValue attributeValue = TypedConfiguration.newConfigItem(AttributeValue.class);
		attributeValue.setName(name);
		attributeValue.setValue(value);
		return attributeValue;
	}

	/**
	 * Creates a {@link NullValue}.
	 */
	public static ModelName nullValue() {
		return TypedConfiguration.newConfigItem(NullValue.class);
	}

	/**
	 * Creates an {@link CompactLabelPath}.
	 */
	public static CompactLabelPath labelPath(List<String> labels) {
		CompactLabelPath treePath = TypedConfiguration.newConfigItem(CompactLabelPath.class);
		treePath.setLabelPath(labels);
		return treePath;
	}

	/**
	 * Creates a {@link ComponentName} with the given properties.
	 */
	public static ComponentName componentName(com.top_logic.mig.html.layout.ComponentName componentName) {
		ComponentName componentModelName = TypedConfiguration.newConfigItem(ComponentName.class);
		componentModelName.setComponentName(componentName);
		return componentModelName;
	}

	/**
	 * Creates a {@link BranchName} for the given {@link Branch}
	 */
	public static BranchName branchName(Branch branch) {
		BranchName branchName = TypedConfiguration.newConfigItem(BranchName.class);
		branchName.setBranchId(branch.getBranchId());
		return branchName;
	}

	/**
	 * Creates a {@link RevisionName} for the given {@link Revision}
	 */
	public static RevisionName revisionName(Revision rev) {
		RevisionName revName = TypedConfiguration.newConfigItem(RevisionName.class);
		revName.setCommitNumber(rev.getCommitNumber());
		return revName;
	}

	/**
	 * Creates a {@link SelectionRef}.
	 */
	public static SelectionRef selectionRef(ModelName selection, BooleanValue selectionState) {
		SelectionRef selectionRef = TypedConfiguration.newConfigItem(SelectionRef.class);
		selectionRef.setSelectee(selection);
		selectionRef.setSelectionState(selectionState);
		return selectionRef;
	}

	/**
	 * Creates a {@link LabeledValue}.
	 */
	public static LabeledValue labeledValue(String label) {
		LabeledValue value = TypedConfiguration.newConfigItem(LabeledValue.class);
		value.setLabel(label);
		return value;
	}

	/**
	 * Creates a {@link ValuePath}.
	 */
	public static ValuePath valuePath(List<ModelName> namePath) {
		ValuePath value = TypedConfiguration.newConfigItem(ValuePath.class);
		value.setNodes(namePath);
		return value;
	}

	/**
	 * Creates a {@link Base64Value}.
	 */
	public static Base64Value base64Data(String name, String contentType, String base64) {
		Base64Value result = TypedConfiguration.newConfigItem(Base64Value.class);
		result.setName(name);
		result.setContentType(contentType);
		result.setBase64Data(base64);
		return result;
	}

	/**
	 * Sets the {@link Branch} and {@link Revision} from the given object to the given reference.
	 * 
	 * @param <T>
	 *        The concrete type of the reference.
	 * @param ref
	 *        The refernece to modify.
	 * @param versionContext
	 *        The context to take the version information from.
	 * @return The given reference for call chaining.
	 */
	public static <T extends VersionedObjectRef> T setVersionContext(T ref, Wrapper versionContext) {
		return setVersionContext(ref, getBranch(versionContext), getRevision(versionContext));
	}

	public static <T extends VersionedObjectRef> T setVersionContext(T ref, Branch branch, Revision revision) {
		ModelName branchName;
		if (branch.getBranchId() != TLContext.TRUNK_ID) {
			branchName = ModelResolver.buildModelName(branch);
		} else {
			branchName = null;
		}

		ModelName revisionName;
		if (!revision.equals(Revision.CURRENT)) {
			revisionName = ModelResolver.buildModelName(revision);
		} else {
			revisionName = null;
		}

		return setVersionContext(ref, branchName, revisionName);
	}

	public static <T extends VersionedObjectRef> T setVersionContext(T ref, ModelName branchName,
			ModelName revisionName) {

		ref.setBranch(branchName);
		ref.setRevision(revisionName);
		return ref;
	}

	/**
	 * Creates a {@link Base64Value}.
	 */
	public static Base64Value base64Value(BinaryData value) {
		String name = value.getName();
		String contentType = value.getContentType();
	
		try {
			InputStream stream = value.getStream();
			byte[] content;
			try {
				content = StreamUtilities.readStreamContents(stream);
			} finally {
				stream.close();
			}
			String base64 = Base64.encodeBase64URLSafeString(content);
	
			return base64Data(name, contentType, base64);
		} catch (IOException ex) {
			throw ApplicationAssertions.fail(null, "Cannot read input data.", ex);
		}
	}

	/**
	 * Creates a {@link TableAspect} for the selection of the given {@link TableData}.
	 */
	public static TableAspect tableSelection(TableData tableData) {
		TableAspect selection = TypedConfiguration.newConfigItem(TableSelection.class);
		selection.setTable(tableData.getModelName());
		return selection;
	}

	/**
	 * Creates a {@link AllTableRows} or {@link DisplayedTableRows} to the given {@link TableData}.
	 */
	public static TableAspect rowCount(TableData tableData, boolean allRows) {
		TableAspect numberRows =
			TypedConfiguration.newConfigItem(allRows ? AllTableRows.class : DisplayedTableRows.class);
		numberRows.setTable(tableData.getModelName());
		return numberRows;
	}

	/**
	 * Creates a {@link RowTableValue} to the given {@link TableCell}.
	 */
	public static <T extends RowTableValue> T tableValue(Class<T> valueRefClass, TableCell tableCell) {
		TableData tableData = tableCell.getTableData();
		TableModel tableModel = tableData.getTableModel();
		Object rowObject = tableCell.getRowObject();
		String columnName = tableCell.getColumnName();
	
		T tableValue = TypedConfiguration.newConfigItem(valueRefClass);
		tableValue.setTable(tableData.getModelName());
		tableValue.setRowObject(ModelResolver.buildModelName(tableData, rowObject));
		tableValue.setColumnLabel(ScriptTableUtil.getColumnLabel(columnName, tableData));
		return tableValue;
	}

	/**
	 * Creates a {@link TableColumnLabel} representing the column label
	 */
	public static TableColumnLabel tableColumnLabel(TableCell tableCell) {
		TableData tableData = tableCell.getTableData();
		String columnName = tableCell.getColumnName();
		return ReferenceInstantiator.tableColumnLabel(tableData, columnName);
	}

	/**
	 * Creates a {@link TableColumnLabel} representing the column label.
	 */
	public static TableColumnLabel tableColumnLabel(TableData tableData, String columnName) {
		TableColumnLabel valueRef = TypedConfiguration.newConfigItem(TableColumnLabel.class);
		ModelName tableDataName = tableData.getModelName();
		valueRef.setTable(tableDataName);
		valueRef.setColumnName(columnName);
		return valueRef;
	}

	/**
	 * Creates a {@link TableColumnRef} identifying a column by its label.
	 */
	public static TableColumnRef tableColumnByLabel(String label) {
		TableColumnRef valueRef = tableColumnRef();
		valueRef.setColumnLabel(label);
		return valueRef;
	}

	/**
	 * Creates a {@link TableColumnRef} identifying a column by its name.
	 */
	public static TableColumnRef tableColumnByName(String name) {
		TableColumnRef valueRef = tableColumnRef();
		valueRef.setColumnName(name);
		return valueRef;
	}

	/**
	 * Creates an empty {@link TableColumnRef}.
	 */
	public static TableColumnRef tableColumnRef() {
		return TypedConfiguration.newConfigItem(TableColumnRef.class);
	}

	/**
	 * Creates a {@link SortConfig} with the given values.
	 */
	public static SortConfig sortConfig(String columnName, boolean ascending) {
		SortConfig sortConfig = TypedConfiguration.newConfigItem(SortConfig.class);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(SortConfig.class);
		PropertyDescriptor columnProperty = descriptor.getProperty(SortConfig.COLUMN_PROPERTY);
		PropertyDescriptor ascendingProperty = descriptor.getProperty(SortConfig.ASCENDING_PROPERTY);
		sortConfig.update(columnProperty, columnName);
		sortConfig.update(ascendingProperty, ascending);
		return sortConfig;
	}

	/**
	 * Convenience method for creating and filling an {@link GlobalVariableRef}.
	 * 
	 * @return Never <code>null</code>.
	 */
	public static GlobalVariableRef globalVariableRef(String variableName) {
		GlobalVariableRef variableRef = TypedConfiguration.newConfigItem(GlobalVariableRef.class);
		variableRef.setName(variableName);
		return variableRef;
	}

	public static ModelName rowInTable(RowTableValue cellValue) {
		RowOfObject valueRef = TypedConfiguration.newConfigItem(RowOfObject.class);
		valueRef.setTable(cellValue.getTable());
		valueRef.setRowObject(cellValue.getRowObject());
		return valueRef;
	}

	public static <N> ModelName treeNodeRef(AssertionTreeNode<N> assertionTreeNode,
			Class<? extends TreeNodeRef> nodeRefClass) {
		TreeNodeRef valueRef = TypedConfiguration.newConfigItem(nodeRefClass);
		initTreeNodeRef(valueRef, assertionTreeNode);
		return valueRef;
	}

	private static <N> void initTreeNodeRef(TreeNodeRef valueRef, AssertionTreeNode<N> guiTreeNode) {
		Object context = guiTreeNode.getContext();
		valueRef.setContext(ModelResolver.buildModelName(context));
		valueRef.setNode(ModelResolver.buildModelName(context, guiTreeNode.getNode()));
	}

	/**
	 * Creates a {@link LabeledButtonName}.
	 * 
	 * @param responsibleComponent
	 *        See: {@link LabeledButtonName#getComponent()}
	 * @param businessObject
	 *        See {@link LabeledButtonName#getBusinessObject()}
	 */
	public static LabeledButtonName labeledButtonName(String label, LayoutComponent responsibleComponent,
			Object businessObject) {
		LabeledButtonName buttonName = TypedConfiguration.newConfigItem(LabeledButtonName.class);
		buttonName.setLabel(label);
		if (responsibleComponent != null) {
			buttonName.setComponent(ModelResolver.buildModelName(responsibleComponent));
		}
		if (businessObject != null) {
			buttonName.setBusinessObject(ModelResolver.buildModelName(businessObject));
		}
		return buttonName;
	}

	/** Creates a {@link ButtonAspectName} of the given type. */
	public static <T extends ButtonAspectName> T buttonAspectName(Class<T> nameClass, ModelName buttonName) {
		T labelName = TypedConfiguration.newConfigItem(nameClass);
		labelName.setButton(buttonName);
		return labelName;
	}

	/** Creates a {@link FormMemberAspectName} of the given type. */
	public static <T extends FormMemberAspectName> T formMemberAspectName(Class<T> nameClass, ModelName formMemberRef) {
		T item = TypedConfiguration.newConfigItem(nameClass);
		item.setFormMember(formMemberRef);
		return item;
	}

	/** Creates a {@link TableColumnAspectName} of the given type. */
	public static <T extends TableColumnAspectName> T tableColumnAspectName(
			Class<T> nameClass, ModelName tableRef, String columnLabel) {
		T item = tableAspectName(nameClass, tableRef);
		item.setColumnLabel(columnLabel);
		return item;
	}

	/** Creates a {@link TableAspectName} of the given type. */
	public static <T extends TableAspectName> T tableAspectName(Class<T> nameClass, ModelName tableRef) {
		T item = TypedConfiguration.newConfigItem(nameClass);
		item.setTable(tableRef);
		return item;
	}

}
