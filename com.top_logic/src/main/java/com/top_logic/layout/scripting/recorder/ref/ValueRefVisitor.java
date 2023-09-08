/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.layout.scripting.action.CurrentFieldOptions;
import com.top_logic.layout.scripting.action.CurrentFieldValue;
import com.top_logic.layout.scripting.recorder.ref.calc.NotRef;
import com.top_logic.layout.scripting.recorder.ref.value.AllTableRows;
import com.top_logic.layout.scripting.recorder.ref.value.BooleanValue;
import com.top_logic.layout.scripting.recorder.ref.value.CellChangeInfo;
import com.top_logic.layout.scripting.recorder.ref.value.CellCompareValue;
import com.top_logic.layout.scripting.recorder.ref.value.DateValue;
import com.top_logic.layout.scripting.recorder.ref.value.DisplayedTableRows;
import com.top_logic.layout.scripting.recorder.ref.value.DoubleValue;
import com.top_logic.layout.scripting.recorder.ref.value.EnumValue;
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
import com.top_logic.layout.scripting.recorder.ref.value.object.NamedModelRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.NodeRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.ObjectAttribute;
import com.top_logic.layout.scripting.recorder.ref.value.object.ObjectAttributeByLabel;
import com.top_logic.layout.scripting.recorder.ref.value.object.SelectedModelRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.TypeRef;
import com.top_logic.layout.scripting.recorder.ref.value.table.TableCellFullText;
import com.top_logic.layout.scripting.recorder.ref.value.table.TableColumnLabel;

/**
 * Visitor interface for {@link ValueRef}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ValueRefVisitor<R, A> {

	/**
	 * Visit case for {@link BooleanValue}.
	 */
	R visitBooleanValue(BooleanValue value, A arg);

	/**
	 * Visit case for {@link IntValue}.
	 */
	R visitIntValue(IntValue value, A arg);

	/**
	 * Visit case for {@link LongValue}.
	 */
	R visitLongValue(LongValue value, A arg);

	/**
	 * Visit case for {@link FloatValue}.
	 */
	R visitFloatValue(FloatValue value, A arg);

	/**
	 * Visit case for {@link DoubleValue}.
	 */
	R visitDoubleValue(DoubleValue value, A arg);

	/**
	 * Visit case for {@link StringValue}.
	 */
	R visitStringValue(StringValue value, A arg);

	/**
	 * Visit case for {@link NullValue}.
	 */
	R visitNullValue(NullValue value, A arg);

	/**
	 * Visit case for {@link DateValue}.
	 */
	R visitDateValue(DateValue value, A arg);

	/**
	 * Visit case for {@link EnumValue}.
	 */
	R visitEnumValue(EnumValue value, A arg);

	/**
	 * Visit case for {@link ListValue}.
	 */
	R visitListValue(ListValue value, A arg);

	/**
	 * Visit case for {@link MapValue}.
	 */
	R visitMapValue(MapValue value, A arg);

	/**
	 * Visit case for {@link SingletonValue}.
	 */
	R visitSingletonValue(SingletonValue value, A arg);

	/**
	 * Visit case for {@link IdRef}.
	 */
	R visitIdRef(IdRef value, A arg);

	/**
	 * Visit case for {@link TypeRef}.
	 */
	R visitTypeRef(TypeRef value, A arg);

	/**
	 * Visit case for {@link ObjectAttributeByLabel}.
	 */
	R visitObjectAttributeByLabel(ObjectAttributeByLabel value, A arg);
	
	/**
	 * Visit case for {@link ObjectAttribute}.
	 */
	R visitObjectAttribute(ObjectAttribute value, A arg);

	/**
	 * Visit case for {@link IndexRef}.
	 */
	R visitIndexRef(IndexRef value, A arg);

	/**
	 * Visit case for {@link NodeRef}.
	 */
	R visitNodeRef(NodeRef value, A arg);

	/**
	 * Visit case for {@link ContextRef}.
	 */
	R visitContextRef(ContextRef value, A arg);

	/**
	 * Visit case for {@link NamedModelRef}.
	 */
	R visitNamedModelRef(NamedModelRef namedModelRef, A arg);

	/**
	 * Visit case for {@link RowTableValue}.
	 */
	R visitRowTableValue(RowTableValue value, A arg);

	/**
	 * Visit case for {@link CellCompareValue}.
	 */
	R visitCellCompareValue(CellCompareValue value, A arg);

	/**
	 * Visit case for {@link CellChangeInfo}.
	 */
	R visitCellChangeInfo(CellChangeInfo value, A arg);

	/**
	 * Visit case for {@link TableCellFullText}.
	 */
	R visitTableCellFullText(TableCellFullText value, A arg);

	/**
	 * Visit case for {@link TableColumnLabel}.
	 */
	R visitTableColumnLabel(TableColumnLabel value, A arg);

	/**
	 * Visit case for {@link IndexedTableValue}.
	 */
	R visitIndexedTableValue(IndexedTableValue value, A arg);

	/**
	 * Visit case for {@link CurrentFieldValue}.
	 */
	R visitCurrentFieldValue(CurrentFieldValue value, A arg);

	/**
	 * Visit case for {@link CurrentFieldOptions}.
	 */
	R visitCurrentFieldOptions(CurrentFieldOptions value, A arg);

	/**
	 * Visit case for {@link DataItemValue}.
	 */
	R visitDataItemValue(DataItemValue value, A arg);

	/**
	 * Visit case for {@link Base64Value}.
	 */
	R visitBase64Value(Base64Value value, A arg);

	/**
	 * Visit case for {@link ExternalDataValue}.
	 */
	R visitExternalDataValue(ExternalDataValue value, A arg);

	/**
	 * Visit case for {@link FileManagerDataValue}.
	 */
	R visitFileManagerDataValue(FileManagerDataValue value, A arg);

	/**
	 * Visit case for {@link DownloadValue}.
	 */
	R visitDownloadValue(DownloadValue value, A arg);

	/**
	 * Visit case for {@link DisplayedTableRows}.
	 */
	R visitDisplayedTableRows(DisplayedTableRows value, A arg);

	/**
	 * Visit case for {@link TableDisplayValue}.
	 */
	R visitTableDisplayValue(TableDisplayValue value, A arg);

	/**
	 * Visit case for {@link AllTableRows}.
	 */
	R visitAllTableRows(AllTableRows value, A arg);

	/**
	 * Visit case for {@link TableSelection}.
	 */
	R visitTableSelection(TableSelection value, A arg);

	/**
	 * Visit case for {@link FieldValueAccess}.
	 */
	R visitFieldValueAccess(FieldValueAccess value, A arg);

	/**
	 * Visit case for {@link FieldChangeInfo }.
	 */
	R visitFieldChangeInfo(FieldChangeInfo value, A arg);

	/**
	 * Visit case for {@link FieldCompareValue }.
	 */
	R visitFieldCompareValue(FieldCompareValue value, A arg);

	/**
	 * Visit case for {@link GlobalVariableRef}.
	 */
	R visitGlobalVariableRef(GlobalVariableRef value, A arg);

	/**
	 * Visit case for {@link FormattedValue}.
	 */
	R visitFormattedValue(FormattedValue value, A arg);

	/**
	 * Visit case for {@link SelectedModelRef}.
	 */
	R visitSelectedModelValue(SelectedModelRef value, A arg);

	/**
	 * Visit case for {@link RowOfObject}.
	 */
	R visitRowOfObjectValue(RowOfObject value, A arg);

	/**
	 * Visit case for {@link TreeNodeExpansionRef}.
	 */
	R visitTreeNodeExpansion(TreeNodeExpansionRef value, A arg);

	/**
	 * Visit case for {@link TreeNodeSelectionRef}.
	 */
	R visitTreeNodeSelection(TreeNodeSelectionRef value, A arg);

	/**
	 * Visit case for {@link TreeNodeLeafRef}.
	 */
	R visitTreeNodeLeaf(TreeNodeLeafRef value, A arg);

	/**
	 * Visit case for {@link TreeNodeChildCountRef}.
	 */
	R visitTreeNodeChildCount(TreeNodeChildCountRef value, A arg);

	/**
	 * Visit case for {@link NotRef}.
	 */
	R visitNot(NotRef value, A arg);

}
