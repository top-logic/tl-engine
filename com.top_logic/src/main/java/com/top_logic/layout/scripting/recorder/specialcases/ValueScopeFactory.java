/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.specialcases;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.folder.FolderData;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.GalleryField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.treetable.component.StructureEditComponent;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.ScriptingUtil;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.value.object.ContextRef;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbData;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNodeResourceProvider;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * Factory of {@link ValueScope} for various contexts.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ValueScopeFactory {

	/**
	 * Creates a reference to the given value in the given value context.
	 * 
	 * @param valueContext
	 *        The context in which the value is referenced. Referencing in the global context (
	 *        <code>null</code>) always fails.
	 * @param value
	 *        The value that should be referenced local to the given context.
	 * @return A context local reference for the given value.
	 */
	public static Maybe<? extends ModelName> referenceContextLocalValue(Object valueContext, Object value) {
		Maybe<? extends ValueScope> scope = buildValueScope(valueContext);
		if (!scope.hasValue()) {
			return Maybe.none();
		}
		return scope.get().buildReference(value);
	}

	/**
	 * Resolves a given value reference in a given context.
	 * 
	 * @see #referenceContextLocalValue(Object, Object)
	 */
	public static Maybe<Object> resolveContextLocalValue(ActionContext actionContext, Object valueContext, ContextRef valueRef) {
		Maybe<? extends ValueScope> scope = buildValueScope(valueContext);
		if (!scope.hasValue()) {
			return Maybe.none();
		}
		return Maybe.toMaybe(scope.get().resolveReference(actionContext, valueRef));
	}

	/**
	 * Creates a {@link ValueScope} for the given context.
	 * 
	 * @param valueContext
	 *        The context in which values are resolved.
	 * @return The {@link ValueScope} for the given context, or <code>null</code> if no matching one
	 *         is found.
	 */
	private static Maybe<? extends ValueScope> buildValueScope(Object valueContext) {
		if (valueContext instanceof SelectField) {
			return Maybe.toMaybe(getSelectFieldScope((SelectField) valueContext));
		} else if (valueContext instanceof GalleryField) {
			return Maybe.toMaybe(getGalleryFieldScope((GalleryField) valueContext));
		} else if (valueContext instanceof TreeComponent) {
			TreeComponent treeComponent = (TreeComponent) valueContext;
			TreeUIModel<?> treeModel = treeComponent.getTreeData().getTreeModel();
			ResourceProvider plainResourceProvider = TypedConfigUtil
				.createInstance((((TreeComponent.Config) treeComponent.getConfig()).getResourceProvider()));
			TLTreeNodeResourceProvider treeNodeProvider = TLTreeNodeResourceProvider.newTLTreeNodeResourceProvider(
				plainResourceProvider);
			return Maybe.some(getTreeModelScope(treeNodeProvider, treeModel, true));
		} else if (valueContext instanceof TreeData) {
			return Maybe.toMaybe(getTreeScope((TreeData) valueContext));
		} else if (valueContext instanceof TableData) {
			return Maybe.toMaybe(getTableScope((TableData) valueContext));
		} else if (valueContext instanceof TableModel) {
			return Maybe.toMaybe(getTableModelScope(null, (TableModel) valueContext));
		} else if (valueContext instanceof TableComponent) {
			return Maybe.toMaybe(getTableModelScope(null, ((TableComponent) valueContext).getTableModel()));
		} else if (valueContext instanceof FormContainer) {
			return Maybe.toMaybe(getFormContainerScope(null, (FormContainer) valueContext));
		} else if (valueContext instanceof LabelScope) {
			return Maybe.toMaybe((LabelScope) valueContext);
		} else if (valueContext instanceof Iterable) {
			return Maybe.toMaybe(getCollectionScope(null, CollectionUtil.toListIterable((Iterable<?>) valueContext)));
		} else if (valueContext instanceof FolderData) {
			return Maybe.toMaybe(getFolderDataScope((FolderData) valueContext));
		} else if (valueContext instanceof BreadcrumbData) {
			return Maybe.toMaybe(getBreadcrumbScope((BreadcrumbData) valueContext));
		} else if (valueContext instanceof SelectionModel) {
			return buildValueScope(((SelectionModel) valueContext).getOwner());
		} else if (valueContext instanceof StructureEditComponent<?>) {
			return buildValueScope(((StructureEditComponent<?>) valueContext).getFormTree());
		} else {
			return Maybe.toMaybe(NoScope.INSTANCE);
		}
	}

	private static ValueScope getSelectFieldScope(SelectField field) {
		LabelProvider labelProvider = field.getOptionLabelProvider();

		if (field.isOptionsList()) {
			List<?> options = field.getOptions();
			if (!options.containsAll(field.getSelection())) {
				options = new ArrayList<>(field.getOptions());
				options.addAll(field.getSelection());
			}
			return new LabelScope(labelProvider, options);
		} else if (field.isOptionsTree()) {
			TLTreeModel treeModel = field.getOptionsAsTree().getBaseModel();
			return new TreeValueScope(treeModel, labelProvider, false);
		} else {
			throw new RuntimeException(
				"The SelectFields options are neither organized as a List, nor as a tree. SelectField: "
					+ StringServices.getObjectDescription(field));
		}
	}

	private static ValueScope getGalleryFieldScope(GalleryField field) {
		return new CollectionValueScope(field.getImages(), null);
	}

	private static ValueScope getTreeScope(TreeData treeData) {
		return getTreeModelScope(ScriptingUtil.getResourceProvider(treeData), treeData.getTreeModel());
	}

	private static ValueScope getTreeModelScope(LabelProvider labelProvider, TLTreeModel treeModel) {
		return getTreeModelScope(labelProvider, treeModel, false);
	}

	private static ValueScope getTreeModelScope(LabelProvider labelProvider, TLTreeModel treeModel,
			boolean asBusinessObject) {
		return new TreeValueScope(treeModel, labelProvider, asBusinessObject);
	}

	private static ValueScope getTableScope(TableData tableData) {
		return getTableModelScope(ScriptingRecorder.getNameProvider(tableData), tableData.getTableModel());
	}

	private static ValueScope getTableModelScope(ValueNamingScheme<?> customNameProvider, TableModel tableModel) {
		if (tableModel instanceof TreeTableModel) {
			return getTreeModelScope(getLabelProvider(tableModel), ((TreeTableModel) tableModel).getTreeModel());
		} else {
			return getCollectionScope(customNameProvider, tableModel.getAllRows());
		}
	}

	private static ValueScope getFormContainerScope(ValueNamingScheme<?> customNameProvider, FormContainer formContainer) {
		List<Object> objects = new ArrayList<>(formContainer.size());
		for (Iterator<? extends FormMember> it = formContainer.getMembers(); it.hasNext();) {
			objects.add(it.next().getStableIdSpecialCaseMarker());
		}
		return getCollectionScope(customNameProvider, objects);
	}

	private static ValueScope getCollectionScope(ValueNamingScheme<?> customNameProvider, Collection<?> objects) {
		return new CollectionValueScope(objects, customNameProvider);
	}

	private static ValueScope getFolderDataScope(FolderData folderData) {
		LabelProvider labelProvider = getLabelProvider(folderData.getTableData().getTableModel());
		return getTreeModelScope(labelProvider, folderData.getTreeModel());
	}

	private static ValueScope getBreadcrumbScope(BreadcrumbData breadcrumbData) {
		return getTreeModelScope(null, breadcrumbData.getTree());
	}

	/**
	 * {@link LabelProvider} for identifying rows in a {@link TableModel}.
	 */
	public static LabelProvider getLabelProvider(final TableModel tableModel) {
		ColumnConfiguration keyColumnConfig = getKeyColumnConfig(tableModel);
		if (keyColumnConfig == null) {
			LabelProvider rowObjectLabelProvider = tableModel.getTableConfiguration().getRowObjectResourceProvider();
			if (rowObjectLabelProvider != null) {
				return rowObjectLabelProvider;
			}

			return MetaLabelProvider.INSTANCE;
		}

		return new LabelProvider() {
			@SuppressWarnings("unchecked")
			final Accessor<Object> _accessor = keyColumnConfig.getAccessor();

			final LabelProvider _fullTextProvider = keyColumnConfig.getFullTextProvider();

			@Override
			public String getLabel(Object object) {
				Object columnValue = _accessor.getValue(object, keyColumnConfig.getName());
				return _fullTextProvider.getLabel(columnValue);
			}
		};
	}

	/** The "key" column is the one whose label can represent the whole row. */
	private static ColumnConfiguration getKeyColumnConfig(TableModel tableModel) {
		String idColumn = tableModel.getTableConfiguration().getIDColumn();

		if (StringServices.isEmpty(idColumn)) {
			return null;
		}

		return getColumnConfig(tableModel, idColumn);
	}

	private static ColumnConfiguration getColumnConfig(TableModel tableModel, String columnName) {
		return tableModel.getColumnDescription(columnName);
	}

}
