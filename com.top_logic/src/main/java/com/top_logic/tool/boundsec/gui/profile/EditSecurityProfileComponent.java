/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.Environment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CheckChangesValueVetoListener;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.tag.TableTag;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.DefaultTooltipControlProvider;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.ViewportState;
import com.top_logic.layout.table.display.VisiblePaneRequest;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.mig.html.layout.ComponentInstantiationContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConfigTree;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;
import com.top_logic.tool.export.ExportAware;
import com.top_logic.util.Resources;

/**
 * Component editing the role profiles using the configuration of the layout tree.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EditSecurityProfileComponent extends EditComponent
		implements ExportAware, ControlRepresentable, TableConfigurationProvider {

	/**
	 * Configuration interface for {@link EditSecurityProfileComponent}
	 */
	public interface Config extends EditComponent.Config {

		/** Configuration name for the value of {@link #getExcludedRoles}. */
		String EXCLUDED_ROLES_NAME = "excludedRoles";

		/**
		 * the names of the roles to exclude from the GUI
		 */
		@Format(CommaSeparatedStringSet.class)
		@Name(EXCLUDED_ROLES_NAME)
		Set<String> getExcludedRoles();

		@StringDefault(ApplyRolesCommandHandler.COMMAND_ID)
		@Override
		String getApplyCommand();

	}

	private static final String TREE = "tree";

	private static final String LAYOUT_SELECT_FIELD = "layoutSelect";

	private static final String LAYOUT_SELECT_TOOLBAR_GROUP = "main";

	static final Property<CommandNode> COMMAND_NODE = TypedAnnotatable.property(CommandNode.class, "commandNode");

	private Set<String> _excludedRoles;

	Map<String, Set<BoundedRole>> _rolesMap = Collections.emptyMap();

	private Collection<?> _oldExpansionModel;

	private ViewportState _oldViewportState;

	private TableControl _renderingControl;

	private HTMLFragment _selectLayoutView;

	private Map<String, LayoutConfigTree> _layoutTrees;

	String _displayedLayout;

	/**
	 * Create a new {@link EditSecurityProfileComponent}
	 */
	public EditSecurityProfileComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_excludedRoles = config.getExcludedRoles();
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public void removeFormContext() {
		TreeUIModel<?> treeModel = getTreeModel();
		if (treeModel != null) {
			_oldExpansionModel = TreeUIModelUtil.getExpansionModel(treeModel);
			_oldViewportState = getTableField().getViewModel().getClientDisplayData().getViewportState();
		}
		_renderingControl = null;
		ToolBar toolBar = getToolBar();
		if (toolBar != null) {
			TableUtil.removeTableButtons(toolBar, getTableField());
			if (_selectLayoutView != null) {
				toolBar.defineGroup(LAYOUT_SELECT_TOOLBAR_GROUP).removeView(_selectLayoutView);
			}
		}

		super.removeFormContext();
	}

	private void initLayoutTrees(InstantiationContext context) {
		_layoutTrees = LayoutUtils.getLayoutConfigTreesByName(context);
		try {
			_displayedLayout = LayoutConfig.getDefaultLayout();
		} catch (ConfigurationException ex) {
			context.error("No default layout found.", ex);
			_displayedLayout = null;
		}
	}

	@Override
	protected boolean doValidateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(SecurityRootObjectProvider.INSTANCE.getSecurityRoot());
		}
		initRolesMap();
		getFormContext();
		return super.doValidateModel(context);
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		resetRolesMap();
	}

	@Override
	protected void becomingInvisible() {
		/* Reset the roles. Otherwise the workflow: 1. change to role admin GUI, 2. create role,
		 * 3.set role profiles, would not work, as the new role is not in the roles map. */
		resetRolesMap();

		_layoutTrees = null;
		_displayedLayout = null;

		super.becomingInvisible();
	}

	@Override
	protected void becomingVisible() {
		super.becomingVisible();
		initLayoutTrees(new ComponentInstantiationContext(
			new DefaultInstantiationContext(EditSecurityProfileComponent.class), getMainLayout()));
	}

	private void initRolesMap() {
		if (_rolesMap == null) {
			_rolesMap = createRoleMap();
		}
	}

	private void resetRolesMap() {
		_rolesMap = null;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof TLObject && super.supportsInternalModel(anObject);
	}

	private TLModule model() {
		return (TLModule) getModel();
	}

	private Map<String, Set<BoundedRole>> createRoleMap() {
		TLModule model = model();
		Map<String, Set<BoundedRole>> map = new HashMap<>();
		Collection<?> roles = BoundHelper.getInstance().getPossibleRoles(model);
		for (Object obj : roles) {
			BoundedRole role = (BoundedRole) obj;
			String name = role.getName();
			if (_excludedRoles.contains(name)) {
				continue;
			}
			int index = name.lastIndexOf('.');
			String localName;
			if (index > 0) {
				localName = name.substring(index + 1);
			} else {
				localName = name;
			}
			Set<BoundedRole> set = map.get(localName);
			if (set == null) {
				set = new HashSet<>();
				map.put(localName, set);
			}
			set.add(role);
		}
		return map;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		table.setResPrefix(getResPrefix().append(TREE));
		table.setFilterProvider(null);
		table.setDefaultFilterProvider(null);
		declareRoleColumns(table);
	}

	@Override
	public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
		defaultColumn.setComparator(null);
		defaultColumn.setFilterProvider(null);
	}

	private void declareRoleColumns(TableConfiguration table) {
		FieldProvider commandGroupFieldProvider = new CommandGroupFieldProvider(table, _rolesMap);
		Accessor<?> accessor = new RowGroupColumnAccessor();
		for (String col : getRoles()) {
			ColumnConfiguration dc = table.declareColumn(col);
			dc.setFieldProvider(commandGroupFieldProvider);
			dc.setColumnLabel(createRoleName(col));
			dc.setAccessor(accessor);
			dc.setControlProvider(DefaultFormFieldControlProvider.INSTANCE);
		}
	}

	private static class CommandGroupFieldProvider extends AbstractFieldProvider {

		private final TableConfiguration _config;

		private final Map<String, Set<BoundedRole>> _rolesByLocalName;

		public CommandGroupFieldProvider(TableConfiguration config, Map<String, Set<BoundedRole>> map) {
			_config = config;
			_rolesByLocalName = map;
		}

		@Override
		public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
			String fieldName = getFieldName(aModel, anAccessor, aProperty);
			if (!(aModel instanceof CommandNode)) {
				return notRelevant(fieldName);
			}
			CommandNode node = (CommandNode) aModel;
			ConfigNode config = node.configNode();
			Set<BoundedRole> colRoles = _rolesByLocalName.get(aProperty);
			if (!config.needsCheckBox(colRoles)) {
				return notRelevant(fieldName);
			}
			Boolean hasRight = Boolean.valueOf(node.hasRight(colRoles));
			BooleanField field = FormFactory.newBooleanField(fieldName, hasRight, !FormFactory.IMMUTABLE);
			field.setLabel(fieldName + ": " + ExportNameLabels.INSTANCE.getLabel(node));
			field.setTooltipCaption(_config.getDeclaredColumn(aProperty).getColumnLabel());
			field.setTooltip(node.getRoleNamesAsTooltip(colRoles));
			field.setControlProvider(DefaultTooltipControlProvider.INSTANCE);
			field.set(COMMAND_NODE, node);
			return field;
		}

		private StringField notRelevant(String fieldName) {
			return FormFactory.newStringField(fieldName, StringServices.EMPTY_STRING, true);
		}

	}

	@Override
	public FormContext createFormContext() {

		FormContext context = new FormContext(this);

		addLayoutSelectField(context);

		TableConfiguration config = TableConfigurationFactory.build(
			this,
			lookupTableConfigurationBuilder(TREE));

		LayoutConfigTree displayedLayoutTree = _layoutTrees.get(_displayedLayout);
		AbstractTreeTableModel<?> treeModel =
			buildTreeTable(displayedLayoutTree, tableColumns(config), config, context);

		treeModel.setRootVisible(false);

		if (_oldExpansionModel != null) {
			TreeUIModelUtil.setExpansionModel(_oldExpansionModel, treeModel);
		}

		TableField field = FormFactory.newTreeTableField(TREE, treeModel);
		ToolBar toolBar = getToolBar();
		if (toolBar != null) {
			field.setToolBar(toolBar);
			if (_selectLayoutView != null) {
				toolBar.defineGroup(LAYOUT_SELECT_TOOLBAR_GROUP).addView(_selectLayoutView);
			}
		}

		context.addMember(field);

		return context;
	}

	private void addLayoutSelectField(FormContext context) {
		if (_layoutTrees.size() < 2) {
			return;
		}
		List<?> initialSelection = Collections.singletonList(_displayedLayout);
		List<String> options = Arrays.asList(_layoutTrees.keySet().toArray(ArrayUtil.EMPTY_STRING_ARRAY));
		SelectField selectField = FormFactory.newSelectField(LAYOUT_SELECT_FIELD, options, !FormFactory.MULTIPLE,
			initialSelection, !FormFactory.IMMUTABLE);

		selectField.setOptionLabelProvider(new LabelProvider() {

			Resources _resources = Resources.getInstance();

			@Override
			public String getLabel(Object object) {
				return EditSecurityProfileComponent.this.getLabel(_resources, (String) object);
			}
		});

		selectField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				List<?> newSelection = (List<?>) newValue;
				_displayedLayout = (String) newSelection.get(0);
				EditSecurityProfileComponent.this.removeFormContext();
				EditSecurityProfileComponent.this.invalidate();
			}
		});
		selectField.addValueVetoListener(new CheckChangesValueVetoListener(this));

		/* Do not deactivate selection in view mode. */
		selectField.setInheritDeactivation(false);
		/* Ignore changes in "Check changed" handling. */
		selectField.setTransient(true);

		context.addMember(selectField);
		_selectLayoutView = new SelectControl(selectField, true);
	}

	String getLabel(Resources resources, String layoutName) {
		LayoutComponent.Config config = _layoutTrees.get(layoutName).getRoot().getConfig();
		ResKey titleKey = config.getTitleKey();
		String label;
		if (titleKey == null) {
			label = layoutName;
		} else {
			label = resources.getString(titleKey, layoutName);
		}
		return label;
	}

	private List<String> tableColumns(TableConfiguration config) {
		List<String> defaultColumns = new ArrayList<>(config.getDefaultColumns());
		defaultColumns.addAll(getRoles());
		return defaultColumns;
	}

	private String securityDomain() {
		return model().getName();
	}

	@Override
	protected void installFormContext(FormContext newFormContext) {
		super.installFormContext(newFormContext);

		if (_oldViewportState != null) {
			TableField tableField = (TableField) newFormContext.getMember(TREE);
			TableViewModel viewModel = tableField.getViewModel();
			VisiblePaneRequest paneRequest = viewModel.getClientDisplayData().getVisiblePaneRequest();
			String columnName = _oldViewportState.getColumnAnchor().getColumnName();
			int columnIndex = viewModel.getColumnIndex(columnName);
			if (columnIndex > -1) {
				paneRequest.setColumnRange(IndexRange.singleIndex(columnIndex));
			}
			paneRequest.setRowRange(IndexRange.singleIndex(_oldViewportState.getRowAnchor().getIndex()));
		}
	}

	@Override
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		super.onSetToolBar(oldValue, newValue);

		// When in a dialog, the setter happens for each opening of the dialog. Ensure that new
		// buttons are built for each new toolbar.
		if (hasFormContext()) {
			tableField().setToolBar(newValue);
		}
	}

	@Override
	public Control getRenderingControl() {
		return getTableControl();
	}

	private TableControl getTableControl() {
		if (_renderingControl == null) {
			/* Cache control to avoid multiple Toolbar entries on recreation of Control */
			_renderingControl = TableTag.createTableControl(tableField());
		}
		return _renderingControl;
	}

	private TableField tableField() {
		return (TableField) getFormContext().getField(TREE);
	}

	private String createRoleName(String col) {
		Set<BoundedRole> set = _rolesMap.get(col);
		Iterator<BoundedRole> iter = set.iterator();
		BoundedRole role = iter.next();
		String label = MetaResourceProvider.INSTANCE.getLabel(role);
		if (!Environment.isDeployed()) {
			while (iter.hasNext()) {
				BoundedRole next = iter.next();
				if (!Utils.equals(MetaResourceProvider.INSTANCE.getLabel(next), label)) {
					Logger.error("Roles with same suffix are expected to have the same translation '" + label + "'. "
						+ set, this);
				}
			}
		}
		return label;
	}

	/**
	 * roles ordered by name
	 */
	private Collection<String> getRoles() {
		List<String> roles = new ArrayList<>(_rolesMap.keySet());
		Collections.sort(roles);
		return roles;
	}

	static final Filter<LayoutComponent> SECURITY_LAYOUT_FILTER = new Filter<>() {
		@Override
		public boolean accept(LayoutComponent anObject) {
			return anObject instanceof CompoundSecurityLayout;
		}
	};

	private AbstractTreeTableModel<?> buildTreeTable(LayoutConfigTree tree,
			List<String> columns, TableConfiguration config, FormContainer container) {
		SecurityTreeTableBuilder builder = new SecurityTreeTableBuilder(securityDomain());
		return new SecurityTreeTableModel(builder, tree.getRoot(), columns, config, new Consumer<SecurityNode>() {

			private final StringBuilder _nameBuffer;

			private final int _length;

			private int index;

			{
				String formGroupPrefix = "commandNodeGroup_";
				_nameBuffer = new StringBuilder(formGroupPrefix);
				_length = formGroupPrefix.length();
			}

			@Override
			public void accept(SecurityNode t) {
				if (!(t instanceof CommandNode)) {
					return;
				}
				FormGroup rowGroup = new FormGroup(createName(), container.getResources());
				rowGroup.setStableIdSpecialCaseMarker(ExportNameLabels.INSTANCE.getLabel(t));
				initNodeGroup(rowGroup, t);
				container.addMember(rowGroup);
				t.set(RowGroupColumnAccessor.ROW_GROUP, rowGroup);
			}

			private void initNodeGroup(FormGroup aGroup, SecurityNode node) {
				for (String theAttribute : columns) {
					ColumnConfiguration colConf = config.getCol(theAttribute);
					FieldProvider fieldProvider = colConf.getFieldProvider();
					Accessor<?> accessor = colConf.getAccessor();
					if (fieldProvider != null) {
						FormMember member = fieldProvider.createField(node, accessor, theAttribute);
						if (member != null) {
							aGroup.addMember(member);
						}
					}
				}
			}

			private String createName() {
				String newName = _nameBuffer.append(index++).toString();
				_nameBuffer.setLength(_length);
				return newName;
			}

		});
	}

	@Override
	public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map arguments) {
		return new OfficeExportValueHolder("defaultTemplate.xls", "export.xls", getTableControl(), false);
	}

	TreeUIModel<?> getTreeModel() {
		TableModel tableModel = getTableModel();
		return tableModel == null ? null : ((TreeTableModel) tableModel).getTreeModel();
	}

	private TableModel getTableModel() {
		TableField tree = getTableField();
		return tree == null ? null : tree.getTableModel();
	}

	TableField getTableField() {
		if (hasFormContext()) {
			return (TableField) getFormContext().getFirstMemberRecursively(TREE);
		}
		return null;
	}

	public Collection<LayoutConfigTree> getAvailableLayoutTrees() {
		return Collections.unmodifiableCollection(_layoutTrees.values());
	}

}
