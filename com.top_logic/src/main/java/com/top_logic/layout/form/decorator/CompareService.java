/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.CustomStartNodeDFSIterator;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.ReverseCustomStartNodeDFSIterator;
import com.top_logic.basic.col.ReverseDescendantDFSIterator;
import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.decorator.DetailDecorator.Context;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.tree.compare.TreeCompareConfiguration;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.TreeTable;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.IndexedTreeTableModel;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.model.TLObject;
import com.top_logic.tool.export.ExcelExportSupport;

/**
 * Compare a form field with another one from an additional form context attached to the form
 * context, the form field lives in.
 * 
 * <p>
 * This service can be accessed by {@link AbstractCompareCommandHandler}.
 * </p>
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class CompareService<CI extends CompareInfo> extends DecorateService<CI> {

	/** Key for the {@link CompareAlgorithm} to get a {@link DetailDecorator}. */
	public static final Property<DetailDecorator> DETAIL_DECORATOR =
		TypedAnnotatable.property(DetailDecorator.class, "detailDecorator", DefaultCompareInfoDecorator.INSTANCE);

	/** Key for the {@link CompareAlgorithm} to get a {@link FieldResolver}. */
	public static final Property<FieldResolver> FIELD_RESOLVER =
		TypedAnnotatable.property(FieldResolver.class, "fieldResolver", DefaultFieldResolver.INSTANCE);

	private final FormContext _compareContext;

	private final ExcelExportSupport _excelExportSupport = ExcelExportSupport.newInstance();

	private final CompareAlgorithm _algorithm;

	private final FieldResolver _fieldResolver;

	private final DetailDecorator _detailDecorator;

	/**
	 * Creates a {@link CompareService}.
	 */
	public CompareService(FormContext anotherContext, CompareAlgorithm algorithm) {
		_compareContext = anotherContext;
		_algorithm = algorithm;
		_fieldResolver = getPlugin(FIELD_RESOLVER);
		_detailDecorator = getPlugin(DETAIL_DECORATOR);
	}

	/**
	 * The algorithm for this {@link CompareInfo}.
	 */
	public CompareAlgorithm getAlgorithm() {
		return _algorithm;
	}

	private <T> T getPlugin(Property<T> key) {
		if (_algorithm == null) {
			return key.getDefaultValue(null);
		}
		return _algorithm.getPlugin(key);
	}

	@Override
	protected CI createDecorateInfo(FormField field, LabelProvider labels) {
		FormField baseValueField = findBaseValueField(field);
		if (baseValueField == null) {
			return null;
		}
		CompareInfo compareInfo = createCompareInfo(baseValueField, field, labels);
		if (compareInfo.getChangeInfo() == ChangeInfo.NO_CHANGE) {
			// No decoration for unchanged fields.
			return null;
		}
		return cast(compareInfo);
	}

	/**
	 * @param field
	 *        The field, containing the actual ("changed") value
	 * @return the comparison base field, to which the given field shall be compared to, or
	 *         <code>null</code>
	 */
	public FormField findBaseValueField(FormField field) {
		return _fieldResolver.findCompareField(field, _compareContext);
	}

	@Override
	protected void start(DisplayContext context, TagWriter out, CI info) throws IOException {
		_detailDecorator.start(context, out, info, Context.DEFAULT);
	}

	@Override
	protected void end(DisplayContext context, TagWriter out, CI info) throws IOException {
		_detailDecorator.end(context, out, info, Context.DEFAULT);
	}

	/**
	 * Create the info object for the given fields.
	 * 
	 * @param baseValueField
	 *        The comparison base version of the field.
	 * @param changeValueField
	 *        The changed version of the field.
	 * @param labels
	 *        A label provider for getting the name (or optional tool tip).
	 * 
	 * @return The requested info object.
	 */
	public CompareInfo createCompareInfo(FormField baseValueField, FormField changeValueField, LabelProvider labels) {
		if (changeValueField instanceof SelectField) {
			return handleSelectField((SelectField) baseValueField, (SelectField) changeValueField, labels);
		} else if (changeValueField instanceof BooleanField) {
			return handleBooleanField((BooleanField) baseValueField, (BooleanField) changeValueField);
		} else if (changeValueField instanceof TableField) {
			return handleTableField((TableField) baseValueField, (TableField) changeValueField, labels);
		} else {
			return handleDefaultField(baseValueField, changeValueField);
		}
	}

	private CompareInfo handleTableField(TableField baseValueField, TableField changeValueField, LabelProvider labels) {
		List<?> baseValueList = getRowsAsList(baseValueField);
		List<?> changeValueList = getRowsAsList(changeValueField);
		return getCollectionCompareInfo(labels, baseValueList, changeValueList);
	}

	@SuppressWarnings("unchecked")
	private List<?> getRowsAsList(TableField field) {
		return CollectionUtil.toList(field.getTableModel().getAllRows());
	}

	/**
	 * Provide an historic info for boolean fields.
	 * 
	 * @param baseValueField
	 *        The first field, must not be <code>null</code>.
	 * @param changeValueField
	 *        The second field, must not be <code>null</code>.
	 * 
	 * @return The requested info object, never <code>null</code>.
	 * @see #createCompareInfo(FormField, FormField, LabelProvider)
	 */
	protected CompareInfo handleBooleanField(BooleanField baseValueField, BooleanField changeValueField) {
		// Must not use raw value here, because it is the string "true" or "false".
		return newCompareInfo(baseValueField.getValue(), changeValueField.getValue());
	}

	/**
	 * Provide an historic info for normal fields.
	 * 
	 * @param baseValueField
	 *        The first field, must not be <code>null</code>.
	 * @param changeValueField
	 *        The second field, must not be <code>null</code>.
	 * 
	 * @return The requested info object, never <code>null</code>.
	 * @see #createCompareInfo(FormField, FormField, LabelProvider)
	 */
	protected CompareInfo handleDefaultField(FormField baseValueField, FormField changeValueField) {
		return newCompareInfo(baseValueField.getRawValue(), changeValueField.getRawValue());
	}

	/**
	 * Provide an historic info for select fields.
	 * 
	 * @param baseValueField
	 *        The first field, must not be <code>null</code>.
	 * @param changeValueField
	 *        The second field, must not be <code>null</code>.
	 * @param labels
	 *        Label provider to get strings from the values.
	 * 
	 * @return The requested info object, never <code>null</code>.
	 * @see #createCompareInfo(FormField, FormField, LabelProvider)
	 */
	protected CompareInfo handleSelectField(SelectField baseValueField, SelectField changeValueField, LabelProvider labels) {
		List<?> baseValues = baseValueField.getSelection();
		List<?> changeValues = changeValueField.getSelection();

		return getCollectionCompareInfo(labels, baseValues, changeValues);
	}

	private CompareInfo getCollectionCompareInfo(LabelProvider labels, List<?> baseValues, List<?> changeValues) {
		ResourceProvider resourceProvider;
		if (labels instanceof ResourceProvider) {
			resourceProvider = (ResourceProvider) labels;
		} else {
			resourceProvider = MetaResourceProvider.INSTANCE;
		}
		return newCollectionCompareInfo(baseValues, changeValues, resourceProvider);
	}

	/**
	 * Creates a new {@link CompareInfo} with default labels.
	 * 
	 * @see #newCompareInfo(Object, Object, LabelProvider)
	 */
	public final CompareInfo newCompareInfo(Object baseValue, Object changeValue) {
		return newCompareInfo(baseValue, changeValue, defaultLabelProvider(baseValue, changeValue));
	}

	/**
	 * Creates a new {@link CompareInfo} used by this {@link CompareService}.
	 * 
	 * @see CompareInfo
	 */
	public CompareInfo newCompareInfo(Object baseValue, Object changeValue, LabelProvider labels) {
		return new CompareInfo(baseValue, changeValue, labels);
	}

	/**
	 * Creates a new {@link AttributeCompareInfo} with default labels.
	 * 
	 * @see #newCompareInfo(Object, Object, LabelProvider)
	 */
	public final AttributeCompareInfo newAttributeCompareInfo(Object baseValue, Object changeValue) {
		return newAttributeCompareInfo(baseValue, changeValue, defaultLabelProvider(baseValue, changeValue));
	}

	/**
	 * Creates a new {@link AttributeCompareInfo} used by this {@link CompareService}.
	 * 
	 * @see AttributeCompareInfo
	 */
	public AttributeCompareInfo newAttributeCompareInfo(Object baseValue, Object changeValue, LabelProvider labels) {
		return new AttributeCompareInfo(baseValue, changeValue, labels);
	}

	/**
	 * Default {@link LabelProvider} used in {@link #newCompareInfo(Object, Object)}.
	 */
	protected LabelProvider defaultLabelProvider(Object baseValue, Object changeValue) {
		if (isBusinessObject(baseValue) || isBusinessObject(changeValue)) {
			// Can not always use MetaResourceProvider, because e.g. for Boolean label is empty
			return MetaResourceProvider.INSTANCE;
		}
		return MetaLabelProvider.INSTANCE;
	}

	private static boolean isBusinessObject(Object value) {
		return value instanceof TLObject || value instanceof KnowledgeItem;
	}

	/**
	 * Creates a new {@link CollectionCompareInfo} used by this {@link CompareService}.
	 */
	public CollectionCompareInfo newCollectionCompareInfo(List<?> baseValues, List<?> changeValues,
			ResourceProvider resourceProvider) {
		return new CollectionCompareInfo(baseValues, changeValues, resourceProvider);
	}

	@SuppressWarnings("unchecked")
	private CI cast(CompareInfo info) {
		return (CI) info;
	}

	@Override
	protected TableData doPrepare(SelectField changeValueField) {
		SelectField baseValueField = (SelectField) findBaseValueField(changeValueField);
		if (baseValueField == null) {
			return changeValueField.getTableData();
		}

		String[] columnNames = null;
		TableConfigurationProvider tableConfig = getCompareTableProvider(changeValueField.getTableConfigurationProvider(), false);
		List<?> rows = getCompareRows(baseValueField, changeValueField);
		ConfigKey derivedKey = ConfigKey.derived(changeValueField.getConfigKey(), "_compare");
		return createCompareTable(changeValueField, columnNames, rows, tableConfig, derivedKey);
	}

	private TableData createCompareTable(FormMember field, String[] columnNames, List<?> rows,
			TableConfigurationProvider tableConfig, ConfigKey configKey) {
		TableConfiguration table = TableConfigurationFactory.build(tableConfig);
		table.setResPrefix(field.getResources());
		ObjectTableModel tableModel = new ObjectTableModel(columnNames, table, rows);
		CompareTableOwner owner = new CompareTableOwner();
		TableData compareTable = DefaultTableData.createTableData(owner, tableModel, configKey);
		owner.init(compareTable, field);
		attachTableModelToCompareColumn(tableModel, table);
		return compareTable;
	}

	@Override
	protected TableData doPrepare(TableField changeValueField) {
		TableField baseValueField = (TableField) findBaseValueField(changeValueField);
		if (baseValueField == null) {
			return changeValueField;
		}

		List<String> theColumns = new ArrayList<>(changeValueField.getTableModel().getColumnNames());
		theColumns.add(0, DecorateService.DECORATION_COLUMN);

		String[] columnNames = ArrayUtil.toStringArray(theColumns);
		TableConfigurationProvider tableConfig =
			getCompareTableProvider(changeValueField.getTableModel().getTableConfiguration(), false);
		List<?> rows = getCompareRows(getRowsAsList(baseValueField), getRowsAsList(changeValueField));
		ConfigKey derivedKey = ConfigKey.derived(changeValueField.getConfigKey(), "_compare");
		return createCompareTable(changeValueField, columnNames, rows, tableConfig, derivedKey);
	}

	/**
	 * Creates a {@link TableConfigurationProvider} that wraps the {@link TableConfiguration}
	 * created by the given {@link TableConfigurationProvider} to display {@link CompareRowObject}
	 * containing the actual business row objects.
	 * 
	 * @param tableConfigProvider
	 *        The table configuration provider for the original row objects.
	 * @param isTreeTable
	 *        Whether the table is a tree table.
	 */
	public TableConfigurationProvider getCompareTableProvider(TableConfigurationProvider tableConfigProvider,
			boolean isTreeTable) {
		return getCompareTableProvider(TableConfigurationFactory.build(tableConfigProvider), isTreeTable);
	}

	/**
	 * Creates a {@link TableConfigurationProvider} that wraps the given {@link TableConfiguration}
	 * to display {@link CompareRowObject} containing the actual business row objects.
	 * 
	 * @param tableConfig
	 *        The table configuration for the original row objects.
	 * @param isTreeTable
	 *        Whether the table is a tree table.
	 */
	public TableConfigurationProvider getCompareTableProvider(TableConfiguration tableConfig, boolean isTreeTable) {
		TableConfigurationProvider decorationColumn = getCompareColumnProvider(isTreeTable);
		return new OverlayCompareTableProvider<>(this, tableConfig, decorationColumn, _excelExportSupport,
			isTreeTable);
	}

	/**
	 * Creates a {@link TableConfigurationProvider} that wraps the given {@link TableConfiguration}s
	 * to display {@link CompareRowObject}, containing the actual business row objects, side by
	 * side.
	 * 
	 * @param isTreeTable
	 *        Whether the table is a tree table.
	 */
	public TableConfigurationProvider getCompareTableProvider(TableConfiguration firstTableConfig,
			TableConfiguration secondTableConfig, boolean isTreeTable) {
		TableConfigurationProvider decorationColumn = getCompareColumnProvider(isTreeTable);
		return new SideBySideCompareTableProvider<>(this, firstTableConfig, secondTableConfig, decorationColumn,
			_excelExportSupport,
			isTreeTable);
	}

	private TableConfigurationProvider getCompareColumnProvider(boolean isTreeTable) {
		Accessor<?> accessor = new CompareInfoAccessor(this, isTreeTable);
		TableConfigurationProvider decorationColumn =
			createDecorationInfoColumn(accessor,
				new CompareInfoRenderer(_detailDecorator, Context.TABLE),
				I18NConstants.CHANGE_INFO_COLUMN_LABEL,
				new CompareInfoExcelRenderer(_excelExportSupport.defaultExcelCellRenderer()));
		return decorationColumn;
	}

	private TableConfigurationProvider createJoinedTableConfigurationProvider(
			final TreeCompareConfiguration treeCompareConfig) {
		return new TableConfigurationProvider() {

			@Override
			public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
				treeCompareConfig.getFirstTableConfigurationProvider().adaptDefaultColumn(defaultColumn);
			}

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				// Settings of first table configuration provider shall dominate
				treeCompareConfig.getSecondTableConfigurationProvider().adaptConfigurationTo(table);
				treeCompareConfig.getFirstTableConfigurationProvider().adaptConfigurationTo(table);
			}
		};
	}

	/**
	 * Returns the row objects of the compare table, when comparing the selection of given two
	 * {@link SelectField}s.
	 * 
	 * @see #getCompareRows(List, List)
	 */
	protected List<?> getCompareRows(SelectField baseValueField, SelectField changeValueField) {
		return getCompareRows(baseValueField.getSelection(), changeValueField.getSelection());
	}

	/**
	 * Returns the row objects of the compare table, when comparing the given two {@link List}s.
	 */
	public static List<CompareRowObject> getCompareRows(List<?> baseValues, List<?> changeValues) {
		return getCompareRows(CompareInfo.identifierMapping(), baseValues, changeValues);
	}

	/**
	 * Creates the rows for a compare table.
	 * 
	 * @param identifier
	 *        Mapping the maps the business object to an identity that is used to identify objects
	 *        in both lists.
	 * @param baseValues
	 *        The comparison base list of row objects.
	 * @param changeValues
	 *        The list of row objects to compare to base values.
	 */
	public static List<CompareRowObject> getCompareRows(Mapping<Object, ?> identifier, List<?> baseValues,
			List<?> changeValues) {

		int numberBaseValues = baseValues.size();
		int numberChangeValues = changeValues.size();

		List<CompareRowObject> rows;
		switch (numberChangeValues) {
			case 0: {
				if (baseValues.isEmpty()) {
					rows = Collections.emptyList();
					break;
				}
				rows = new ArrayList<>(numberBaseValues);
				for (Object oldValue : baseValues) {
					rows.add(new CompareRowObject(oldValue, null));
				}
				break;
			}
			case 1: {
				Object newSelectedObject = changeValues.get(0);
				switch (numberBaseValues) {
					case 0: {
						rows = new ArrayList<>(1);
						rows.add(new CompareRowObject(null, newSelectedObject));
						break;
					}
					default: {
						rows = new ArrayList<>(numberBaseValues + 1);
						Object newIdentifier = identifier.map(newSelectedObject);
						boolean oldValueFound = false;
						for (Object oldValue : baseValues) {
							Object oldIdentifier = identifier.map(oldValue);
							if (Utils.equals(oldIdentifier, newIdentifier)) {
								oldValueFound = true;
								rows.add(new CompareRowObject(oldValue, newSelectedObject));
							} else {
								rows.add(new CompareRowObject(oldValue, null));
							}
						}
						if (!oldValueFound) {
							rows.add(new CompareRowObject(null, newSelectedObject));
						}

					}
				}
				break;
			}
			default: {
				switch (numberBaseValues) {
					case 0: {
						rows = new ArrayList<>(numberChangeValues);
						for (Object newValue : changeValues) {
							rows.add(new CompareRowObject(null, newValue));
						}
						break;
					}
					case 1: {
						rows = new ArrayList<>(numberChangeValues + 1);
						Object oldSelectedObject = baseValues.get(0);
						Object oldIdentifier = identifier.map(oldSelectedObject);
						boolean oldValueFound = false;
						for (Object newValue : changeValues) {
							Object newIdentifier = identifier.map(newValue);
							if (Utils.equals(newIdentifier, oldIdentifier)) {
								oldValueFound = true;
								rows.add(new CompareRowObject(oldSelectedObject, newValue));
							} else {
								rows.add(new CompareRowObject(null, newValue));
							}
						}
						if (!oldValueFound) {
							rows.add(new CompareRowObject(oldSelectedObject, null));
						}

						break;
					}
					default: {
						Map<Object, Object> oldValuesById = new HashMap<>();
						for (Object oldValue : baseValues) {
							oldValuesById.put(identifier.map(oldValue), oldValue);
						}

						rows = new ArrayList<>();
						for (Object newValue : changeValues) {
							Object oldValue = oldValuesById.remove(identifier.map(newValue));
							rows.add(new CompareRowObject(oldValue, newValue));
						}
						for (Object oldValue : oldValuesById.values()) {
							rows.add(new CompareRowObject(oldValue, null));
						}

					}
				}
				break;

			}
		}

		return rows;
	}

	/**
	 * Creates a {@link DefaultTreeTableModel} that displays the comparison of two trees.
	 * 
	 * @param treeCompareConfig
	 *        - the configuration for tree comparison
	 * 
	 * @throws IllegalArgumentException
	 *         iff the root elements does not have the same identifier.
	 */
	public AbstractTreeTableModel<?> getCompareTreeTableModel(TreeCompareConfiguration treeCompareConfig) {
		return getCompareTreeTableModel(treeCompareConfig.getFirstTree(), treeCompareConfig.getFirstTreeRoot(),
			treeCompareConfig.getSecondTree(), treeCompareConfig.getSecondTreeRoot(),
			TableConfigurationFactory.build(treeCompareConfig.getFirstTableConfigurationProvider())
				.getDefaultColumns(),
			createJoinedTableConfigurationProvider(treeCompareConfig),
			treeCompareConfig.getIdentifierMapping());
	}

	/**
	 * Creates a {@link DefaultTreeTableModel} that displays the comparison of two trees, using
	 * separate table part for each tree.
	 * 
	 * @param treeCompareConfig
	 *        - the configuration for tree comparison
	 * 
	 * @throws IllegalArgumentException
	 *         iff the root elements does not have the same identifier.
	 */
	public AbstractTreeTableModel<?> getSideBySideCompareTreeTableModel(TreeCompareConfiguration treeCompareConfig) {
		return getSideBySideCompareTreeTableModel(treeCompareConfig.getFirstTree(),
			treeCompareConfig.getFirstTreeRoot(),
			treeCompareConfig.getSecondTree(), treeCompareConfig.getSecondTreeRoot(),
			treeCompareConfig.getFirstTableConfigurationProvider(),
			treeCompareConfig.getSecondTableConfigurationProvider(),
			treeCompareConfig.getIdentifierMapping());
	}

	/**
	 * Creates a {@link DefaultTreeTableModel} that displays the comparison of two trees.
	 * 
	 * @param baseValueTree
	 *        The original tree.
	 * @param baseValueRoot
	 *        The root of the original tree. It must have the same identifier as
	 *        <code>changeValueRoot</code>.
	 * @param changeValueTree
	 *        The tree to compare with base value tree.
	 * @param changeValueRoot
	 *        The root object of the compare tree. It must have the same identifier as
	 *        <code>baseValueRoot</code>.
	 * @param columnNames
	 *        The displayed columns in the result table.
	 * @param tableConfigProvider
	 *        Definition of the table (treating original row objects).
	 * @param identifier
	 *        Mapping to get identifier to identify objects in the different trees.
	 * 
	 * @throws IllegalArgumentException
	 *         iff the root elements does not have the same identifier.
	 */
	public <N> AbstractTreeTableModel<?> getCompareTreeTableModel(TreeView<N> baseValueTree,
			N baseValueRoot, TreeView<N> changeValueTree,
			N changeValueRoot, List<String> columnNames, TableConfigurationProvider tableConfigProvider,
			Mapping<Object, ?> identifier) {
		return getCompareTreeModelInternal(baseValueTree, baseValueRoot, changeValueTree, changeValueRoot, columnNames,
			tableConfigProvider, null, identifier);
	}

	/**
	 * Creates a {@link DefaultTreeTableModel} that displays the comparison of two trees side by
	 * side.
	 * 
	 * @param baseValueTree
	 *        The left tree.
	 * @param baseValueRoot
	 *        The root of the left tree. It must have the same identifier as
	 *        <code>changeValueRoot</code>.
	 * @param changeValueTree
	 *        The right tree.
	 * @param changeValueRoot
	 *        The root object of the right tree. It must have the same identifier as
	 *        <code>baseValueRoot</code>.
	 * @param baseValueTableConfigProvider
	 *        Definition of the first tree table part (treating original row objects).
	 * @param identifier
	 *        Mapping to get identifier to identify objects in the different trees.
	 * 
	 * @throws IllegalArgumentException
	 *         iff the root elements does not have the same identifier.
	 */
	public <N> AbstractTreeTableModel<?> getSideBySideCompareTreeTableModel(TreeView<N> baseValueTree,
			N baseValueRoot,
			TreeView<N> changeValueTree,
			N changeValueRoot, TableConfigurationProvider baseValueTableConfigProvider,
			TableConfigurationProvider changeValueTableConfigProvider, Mapping<Object, ?> identifier) {
		return getCompareTreeModelInternal(baseValueTree, baseValueRoot, changeValueTree, changeValueRoot,
			Collections.<String> emptyList(),
			baseValueTableConfigProvider, changeValueTableConfigProvider, identifier);
	}

	private <N> AbstractTreeTableModel<?> getCompareTreeModelInternal(TreeView<N> baseValueTree,
			N baseValueRoot,
			TreeView<N> changeValueTree, N changeValueRoot, List<String> columnNames,
			TableConfigurationProvider baseValueTableConfigProvider, TableConfigurationProvider changeValueTableConfigProvider,
			Mapping<Object, ?> identifier) {
		TreeBuilder<DefaultTreeTableNode> builder = newCompareTreeTableBuilder(identifier, baseValueTree, changeValueTree);
		CompareRowObject compareTreeNode;
		if (identifier.map(baseValueRoot).equals(identifier.map(changeValueRoot))) {
			compareTreeNode = new CompareRowObject(baseValueRoot, changeValueRoot);
		} else {
			throw new IllegalArgumentException(baseValueRoot + " and " + changeValueRoot + " are not equal. "
				+ identifier.map(baseValueRoot) + " vs. " + identifier.map(changeValueRoot));
		}

		TableConfigurationProvider tableConfigurationProvider;
		if (changeValueTableConfigProvider == null) {
			tableConfigurationProvider = getCompareTableProvider(baseValueTableConfigProvider, true);
		} else {
			tableConfigurationProvider = getCompareTableProvider(TableConfigurationFactory.build(baseValueTableConfigProvider),
				TableConfigurationFactory.build(changeValueTableConfigProvider), true);
		}
		TableConfiguration tableConfig = TableConfigurationFactory.build(tableConfigurationProvider);
		IndexedTreeTableModel<DefaultTreeTableNode> treeTableModel =
			new IndexedTreeTableModel<>(builder, compareTreeNode, columnNames, tableConfig);
		attachTableModelToCompareColumn(treeTableModel.getTable(), tableConfig);
		return treeTableModel;
	}

	private void attachTableModelToCompareColumn(TableModel tableModel, TableConfiguration tableConfig) {
		ColumnConfiguration compareColumn = tableConfig.getDeclaredColumn(DECORATION_COLUMN);
		if (compareColumn != null) {
			CompareInfoAccessor compareAccessor = (CompareInfoAccessor) compareColumn.getAccessor();
			compareAccessor.setTableModel(tableModel);
		}
	}

	/**
	 * Displays a message, using {@link InfoService}, that no change after current position can be
	 * determined.
	 */
	public static void showNoNextChangeMessage() {
		showNoChangeMessage(I18NConstants.COMPARE_NO_NEXT_CHANGE);
	}

	/**
	 * Displays a message, using {@link InfoService}, that no change before current position can be
	 * determined.
	 */
	public static void showNoPreviousChangeMessage() {
		showNoChangeMessage(I18NConstants.COMPARE_NO_PREVIOUS_CHANGE);
	}

	private static void showNoChangeMessage(ResKey noChangeMessage) {
		InfoService.showInfo(noChangeMessage);
	}

	/**
	 * Sets the row selection of the compare table to the next change, starting at given row,
	 * respecting currently active filters. If start row is <code>null</code>, then the first row,
	 * that displays a change, will be selected.
	 */
	public static void selectNextChange(TableData tableData, Object startRow) {
		Iterator<?> tableRowIterator = getForwardIterator(tableData.getViewModel(), startRow);
		selectNextChangeInternal(tableData, tableRowIterator);
	}

	/**
	 * Sets the row selection of the compare table to the very next change after the given row,
	 * respecting currently active filters. If start row is <code>null</code>, then the first row,
	 * that displays a change, will be selected.
	 */
	public static void selectNextChangeAfter(TableData tableData, Object startRow) {
		Iterator<?> tableRowIterator = getForwardIterator(tableData.getViewModel(), startRow);
		if (tableRowIterator.hasNext()) {
			tableRowIterator.next();
		}
		selectNextChangeInternal(tableData, tableRowIterator);
	}

	/**
	 * Sets the row selection of the compare table to the previous change, starting at given row,
	 * respecting currently active filters. If start row is <code>null</code>, then the very last
	 * row, that displays a change, will be selected.
	 */
	public static void selectPreviousChange(TableData tableData, Object startRow) {
		Iterator<?> tableRowIterator = getReverseIterator(tableData.getViewModel(), startRow);
		selectNextChangeInternal(tableData, tableRowIterator);
	}

	/**
	 * Sets the row selection of the compare table to the very previous change before the given row,
	 * respecting currently active filters. If start row is <code>null</code>, then the very last
	 * row, that displays a change, will be selected.
	 */
	public static void selectPreviousChangeBefore(TableData tableData, Object startRow) {
		Iterator<?> tableRowIterator = getReverseIterator(tableData.getViewModel(), startRow);
		if (tableRowIterator.hasNext()) {
			tableRowIterator.next();
		}
		selectNextChangeInternal(tableData, tableRowIterator);
	}

	private static Iterator<?> getForwardIterator(TableViewModel compareTableModel, Object startRow) {
		Iterator<?> tableRowIterator;
		if (isTreeTable(compareTableModel)) {
			tableRowIterator = getForwardTreeTableIterator(compareTableModel, startRow);
		} else {
			tableRowIterator = getForwardFlatTableIterator(compareTableModel, startRow);
		}
		return tableRowIterator;
	}

	private static Iterator<?> getReverseIterator(TableViewModel compareTableModel, Object startRow) {
		Iterator<?> tableRowIterator;
		if (isTreeTable(compareTableModel)) {
			tableRowIterator = getReverseTreeTableIterator(compareTableModel, startRow);
		} else {
			tableRowIterator = getReverseFlatTableIterator(compareTableModel, startRow);
		}
		return tableRowIterator;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Iterator<DefaultTreeTableNode> getForwardTreeTableIterator(TableViewModel compareTableModel, Object startRow) {
		AbstractTreeTableModel<DefaultTreeTableNode> treeTable = getTreeTable(compareTableModel);
		DefaultTreeTableNode rootNode = treeTable.getRoot();
		Iterator<DefaultTreeTableNode> treeTableIterator;
		if (startRow != null) {
			treeTableIterator = new CustomStartNodeDFSIterator<DefaultTreeTableNode>(treeTable, rootNode, (DefaultTreeTableNode) startRow, (Comparator) compareTableModel.getRowComparator());
		} else {
			treeTableIterator = new DescendantDFSIterator<DefaultTreeTableModel.DefaultTreeTableNode>(treeTable,
				rootNode, false, (Comparator) compareTableModel.getRowComparator());
		}
		return treeTableIterator;
	}

	private static Iterator<?> getForwardFlatTableIterator(TableViewModel compareTableModel, Object startRow) {
		return getIterableList(compareTableModel, startRow).iterator();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Iterator<DefaultTreeTableNode> getReverseTreeTableIterator(TableViewModel compareTableModel,
			Object startRow) {
		AbstractTreeTableModel<DefaultTreeTableNode> treeTable = getTreeTable(compareTableModel);
		DefaultTreeTableNode rootNode = treeTable.getRoot();
		Iterator<DefaultTreeTableNode> treeTableIterator;
		if (startRow != null) {
			treeTableIterator =
				new ReverseCustomStartNodeDFSIterator<DefaultTreeTableNode>(treeTable, rootNode,
					(DefaultTreeTableNode) startRow, false,
					(Comparator) compareTableModel.getRowComparator());
		} else {
			treeTableIterator = new ReverseDescendantDFSIterator<DefaultTreeTableModel.DefaultTreeTableNode>(
				treeTable, rootNode, false, (Comparator) compareTableModel.getRowComparator());
		}

		return treeTableIterator;
	}

	private static Iterator<?> getReverseFlatTableIterator(TableViewModel compareTableModel, Object startRow) {
		List<?> subList = getIterableList(compareTableModel, startRow);
		Collections.reverse(subList);
		return subList.iterator();
	}

	private static List<?> getIterableList(TableViewModel compareTableModel, Object startRow) {
		List<?> displayedRows = compareTableModel.getDisplayedRows();
		if (startRow != null) {
			int startRowIndex = displayedRows.indexOf(startRow);
			List<?> subList = new ArrayList<>(displayedRows.subList(startRowIndex, displayedRows.size()));
			return subList;
		} else {
			return new ArrayList<>(displayedRows);
		}
	}

	@SuppressWarnings("unchecked")
	static AbstractTreeTableModel<DefaultTreeTableNode> getTreeTable(
			TableViewModel compareTableModel) {
		return (AbstractTreeTableModel<DefaultTreeTableNode>) ((AbstractTreeTableModel<DefaultTreeTableNode>.TreeTable) compareTableModel
			.getApplicationModel())
			.getTreeModel();
	}

	private static void selectNextChangeInternal(TableData tableData, Iterator<?> tableRowIterator) {
		TableViewModel viewModel = tableData.getViewModel();
		Object changeRow = getNextChangeRow(viewModel, tableRowIterator);

		if (changeRow != null) {
			expandTreeTableParents(viewModel, changeRow);
			tableData.getSelectionModel().setSelection(Collections.singleton(changeRow));
		}
	}

	private static boolean isTreeTable(TableViewModel compareTableModel) {
		return compareTableModel.getApplicationModel() instanceof TreeTable;
	}

	@SuppressWarnings("unchecked")
	private static void expandTreeTableParents(TableViewModel compareTableModel, Object changeRow) {
		if (isTreeTable(compareTableModel)) {
			TreeUIModel<Object> treeTable =
				(TreeUIModel<Object>) ((TreeTableModel) compareTableModel.getApplicationModel()).getTreeModel();
			TreeUIModelUtil.expandParents(treeTable, changeRow);
		}
	}

	private static Object getNextChangeRow(TableViewModel compareTableModel, Iterator<?> tableRowIterator) {
		Object changeRow;
		if (isTreeTable(compareTableModel)) {
			changeRow = getNextTableChange(tableRowIterator, new TreeTableChangeFilter(compareTableModel));
		} else {
			changeRow = getNextTableChange(tableRowIterator, new FlatTableChangeFilter(compareTableModel));
		}
		return changeRow;
	}

	private static Object getNextTableChange(Iterator<?> tableRowIterator, Filter<Object> changeFilter) {
		Object changeRow = null;
		while (tableRowIterator.hasNext()) {
			Object row = tableRowIterator.next();
			if (changeFilter.accept(row)) {
				changeRow = row;
				break;
			}
		}
		return changeRow;
	}

	static boolean isChangeRow(TableViewModel compareTableModel, Object row) {
		CompareInfo compareInfo = (CompareInfo) compareTableModel.getValueAt(row, DECORATION_COLUMN);
		return compareInfo.getChangeInfo() != ChangeInfo.NO_CHANGE;
	}

	static <N> TreeBuilder<DefaultTreeTableNode> newCompareTreeTableBuilder(final Mapping<Object, ?> identifier,
			final TreeView<N> baseValue, final TreeView<N> changeValue) {
		TreeBuilder<DefaultTreeTableNode> builder = new TreeBuilder<>() {

			@Override
			public boolean isFinite() {
				return changeValue.isFinite() && baseValue.isFinite();
			}

			@Override
			public DefaultTreeTableModel.DefaultTreeTableNode createNode(
					AbstractMutableTLTreeModel<DefaultTreeTableModel.DefaultTreeTableNode> model,
					DefaultTreeTableModel.DefaultTreeTableNode parent, Object userObject) {
				return new DefaultTreeTableNode(model, parent, userObject);
			}

			@SuppressWarnings("unchecked")
			@Override
			public List<DefaultTreeTableModel.DefaultTreeTableNode> createChildList(
					DefaultTreeTableModel.DefaultTreeTableNode node) {
				CompareRowObject compareTreeNode = (CompareRowObject) node.getBusinessObject();
				List<N> changeValueChildren;
				if (compareTreeNode.changeValue() == null) {
					changeValueChildren = Collections.emptyList();
				} else {
					changeValueChildren =
						CollectionUtil.toList(changeValue.getChildIterator((N) compareTreeNode.changeValue()));
				}
				List<N> baseValueChildren;
				if (compareTreeNode.baseValue() == null) {
					baseValueChildren = Collections.emptyList();
				} else {
					baseValueChildren =
						CollectionUtil.toList(baseValue.getChildIterator((N) compareTreeNode.baseValue()));
				}
				List<DefaultTreeTableNode> children = new ArrayList<>();
				AbstractMutableTLTreeModel<DefaultTreeTableNode> model = node.getModel();
				for (Object businessObject : getCompareRows(identifier, baseValueChildren, changeValueChildren)) {
					children.add(createNode(model, node, businessObject));
				}
				return children;
			}

		};
		return builder;
	}

	private static final class TreeTableChangeFilter implements Filter<Object> {

		private TableViewModel _compareTableModel;
		private AbstractTreeTableModel<DefaultTreeTableNode> _treeTable;

		TreeTableChangeFilter(TableViewModel compareTableModel) {
			_compareTableModel = compareTableModel;
			_treeTable = CompareService.getTreeTable(compareTableModel);
		}

		@Override
		public boolean accept(Object rowObject) {
			DefaultTreeTableNode currentNode = (DefaultTreeTableNode) rowObject;
			return CompareService.isChangeRow(_compareTableModel, currentNode) && _treeTable.shouldDisplay(currentNode);
		}
	}

	private static final class FlatTableChangeFilter implements Filter<Object> {

		private TableViewModel _compareTableModel;

		FlatTableChangeFilter(TableViewModel compareTableModel) {
			_compareTableModel = compareTableModel;
		}

		@Override
		public boolean accept(Object rowObject) {
			return CompareService.isChangeRow(_compareTableModel, rowObject);
		}
	}

}
