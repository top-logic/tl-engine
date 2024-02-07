/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.MemberChangedListener;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.model.AbstractFormContainer;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.form.template.model.internal.TemplateControl;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.Header;
import com.top_logic.layout.table.renderer.ColumnLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Light-weight dialog for choosing multiple sort columns for {@link TableControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SortConfigDialog extends AbstractFormPageDialog {

	/**
	 * Configuration for {@link SortConfigDialog}.
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * See {@link Config#getWidth}.
		 */
		String DIALOG_WIDTH = "width";

		/**
		 * See {@link Config#getHeight}.
		 */
		String DIALOG_HEIGHT = "height";

		/**
		 * The width of the dialog to sort columns.
		 */
		@Name(DIALOG_WIDTH)
		@FormattedDefault("400px")
		DisplayDimension getWidth();

		/**
		 * The height of the dialog to sort columns.
		 */
		@Name(DIALOG_HEIGHT)
		@FormattedDefault("300px")
		DisplayDimension getHeight();

	}

	private static final ResPrefix RES_PREFIX = ResPrefix.legacyClassLocal(SortConfigDialog.class);

	private static final ResourceText TITLE = new ResourceText(SortConfigDialog.RES_PREFIX.key("title"));

	private static final ResourceView RESOURCES = SortConfigDialog.RES_PREFIX;

	private static final TagTemplate TEMPLATE = div(style("margin: 5px; padding-left: 10px;"),
		Templates.member(SortConfigContext.COLUMNS_GROUP,
			self(
				table(
					thead(
						tr(
							th(colspan(2), resource(SortConfigDialog.RES_PREFIX.key("column"))),
							th(resource(SortConfigDialog.RES_PREFIX.key("direction"))))),
					tbody(
						items(
							self(
								tr(
									td(style("padding-right: 3px;"), Templates.label(SortConfigGroup.COLUMN_INPUT)),
									td(Templates.member(SortConfigGroup.COLUMN_INPUT)),
									td(Templates.member(SortConfigGroup.DIRECTION_INPUT))))))))));

	private static final ControlProvider CONTROL_PROVIDER = new DefaultFormFieldControlProvider() {
		@Override
		public Control visitCommandField(CommandField member, Void arg) {
			ButtonControl result = new ButtonControl(member, ButtonComponentButtonRenderer.INSTANCE);
			return result;
		}
		
		@Override
		public Control visitSelectField(SelectField member, Void arg) {
			if (member.getName().equals(SortConfigGroup.DIRECTION_INPUT)) {
				SelectControl selectControl = new SelectControl(member, true);
				return selectControl;
			} else {
				return super.visitSelectField(member, arg);
			}
		}
	};

	static final NamedConstant DIALOG_MODEL = new NamedConstant("dialogControl");

	private final TableData _table;

	/**
	 * Creates a {@link SortConfigDialog} for the given {@link TableData} and opens it.
	 */
	public static void openDialog(DisplayContext context, TableData table) {
		SortConfigDialog dialog = createDialog(table);
		dialog.open(context);
	}
	
	/**
	 * Creates a new {@link SortConfigDialog}.
	 */
	protected SortConfigDialog(DialogModel dialogModel, TableData table) {
		super(dialogModel, RES_PREFIX.key(HEADER_RESOURCE_SUFFIX), RES_PREFIX.key(MESSAGE_RESOURCE_SUFFIX));

		_table = table;
	}

	/**
	 * Creates a {@link SortConfigDialog} for the given {@link TableData}.
	 */
	public static SortConfigDialog createDialog(TableData table) {
		Config sortDialogConfig = ApplicationConfig.getInstance().getConfig(Config.class);

		DefaultDialogModel dialogModel = new DefaultDialogModel(
			new DefaultLayoutData(sortDialogConfig.getWidth(), 100, sortDialogConfig.getHeight(), 100, Scrolling.AUTO),
			TITLE,
			true,
			true, null);

		return new SortConfigDialog(dialogModel, table);
	}
	
	private static class SortConfigContext extends FormContext {

		final class OkAction implements Command {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				Iterable<SortConfigGroup> configGroups =
					AbstractFormContainer.getMembersTyped(getColumnsGroup(), SortConfigGroup.class);
				ArrayList<SortConfig> configs = new ArrayList<>();
				for (SortConfigGroup configGroup : configGroups) {
					String columnName = configGroup.getColumnName();
					if (columnName == null) {
						// Last input for adding column.
						break;
					}
					boolean ascending = configGroup.getAscending();
					configs.add(SortConfigFactory.sortConfig(columnName, ascending));
				}
				TableControl.sortTable(_table, configs);

				return getCancel().executeCommand(context);
			}
		}

		public static final String COLUMNS_GROUP = "columns";

		static final ValueListener REMOVE_ON_CLEAR = new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (CollectionUtil.isEmptyOrNull((Collection<?>) newValue)) {
					SortConfigGroup myConfig = (SortConfigGroup) field.getParent();
					myConfig.getParent().removeMember(myConfig);
				}
			}
		};

		static final ValueListener ENSURE_UNIQUE_VALUE = new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (!CollectionUtil.isEmptyOrNull((Collection<?>) newValue)) {
					SortConfigGroup myConfig = (SortConfigGroup) field.getParent();
					Object myConfigSelection = myConfig.getColumnInput().getSelection();
					Iterator<? extends FormMember> allConfigsIterator = myConfig.getParent().getMembers();
					ArrayList<FormMember> sortConfigList = IteratorUtil.toList(allConfigsIterator);
					for (FormMember formMember : sortConfigList) {
						SortConfigGroup nextConfig = (SortConfigGroup) formMember;
						if (!nextConfig.equals(myConfig)) {
							Object nextConfigSelection = nextConfig.getColumnInput().getSelection();
							if (CollectionUtil.equals(myConfigSelection, nextConfigSelection)) {
								nextConfig.getColumnInput().setAsSelection(Collections.emptyList());
							}
						}
					}
				}
			}
		};

		static final ValueListener EXTEND_ON_CHOOSE = new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (!CollectionUtil.isEmptyOrNull((Collection<?>) newValue)) {
					field.removeValueListener(EXTEND_ON_CHOOSE);
					field.addValueListener(REMOVE_ON_CLEAR);
					
					SortConfigGroup myConfig = (SortConfigGroup) field.getParent();
					
					SortConfigGroup emptySortGroup = SortConfigGroup.cloneConfig(myConfig, myConfig.getIndex() + 1);
					emptySortGroup.getColumnInput().addValueListener(EXTEND_ON_CHOOSE);
					emptySortGroup.getColumnInput().addValueListener(ENSURE_UNIQUE_VALUE);
					
					myConfig.getParent().addMember(emptySortGroup);
				}
			}

		};
		
		private static final MemberChangedListener COLUMN_CONFIG_LABEL_UPDATER = new MemberChangedListener() {

			@Override
			public Bubble memberAdded(FormContainer parent, FormMember member) {
				initColumnLabels(parent);
				return Bubble.BUBBLE;
			}

			@Override
			public Bubble memberRemoved(FormContainer parent, FormMember member) {
				initColumnLabels(parent);
				return Bubble.BUBBLE;
			}
		};

		TableData _table;
		
		private final FormGroup columns;

		private final Command _cancel;

		private final Command _ok;

		public SortConfigContext(ResourceView resources, String name, TableData table, Command cancel) {
			super(name, resources);
			_cancel = cancel;
			_ok = new OkAction();

			_table = table;
			TableViewModel tableModel = _table.getViewModel();
			columns = new FormContext(COLUMNS_GROUP, resources);
			this.addMember(columns);
			
			Header header = tableModel.getHeader();
			List<Column> sortableColumns = getSortableColumns(tableModel);
			final LabelProvider directLabel = ColumnLabelProvider.newInstance(_table);
			LabelProvider allColumnLabels = new StructuredColumnLabels(directLabel);
			
			int n = 0;
			for (SortConfig config : tableModel.getSortOrder()) {
				SortConfigGroup sortGroup = new SortConfigGroup(resources, n++, sortableColumns, allColumnLabels);
				columns.addMember(sortGroup);
				
				sortGroup.setColumn(header.getColumn(config.getColumnName()));
				sortGroup.setAscending(config.getAscending());
				
				sortGroup.getColumnInput().addValueListener(REMOVE_ON_CLEAR);
				sortGroup.getColumnInput().addValueListener(ENSURE_UNIQUE_VALUE);
			}
			SortConfigGroup emptySortGroup = new SortConfigGroup(resources, n, sortableColumns, allColumnLabels);
				
			columns.addMember(emptySortGroup);
			emptySortGroup.getColumnInput().addValueListener(EXTEND_ON_CHOOSE);
			emptySortGroup.getColumnInput().addValueListener(ENSURE_UNIQUE_VALUE);
			
			initColumnLabels(columns);
			columns.addListener(FormContainer.MEMBER_ADDED_PROPERTY, COLUMN_CONFIG_LABEL_UPDATER);
			columns.addListener(FormContainer.MEMBER_REMOVED_PROPERTY, COLUMN_CONFIG_LABEL_UPDATER);
		}

		private List<Column> getSortableColumns(TableViewModel viewModel) {
			Header header = viewModel.getHeader();
			List<Column> sortableColumns = new ArrayList<>();
			for (int i = 0; i < viewModel.getColumnCount(); i++) {
				if (viewModel.isSortable(i)) {
					Column column = header.getColumn(viewModel.getColumnName(i));
					sortableColumns.add(column);
				}
			}
			return sortableColumns;
		}

		static void initColumnLabels(FormContainer configContext) {
			int number = 1;
			for (Iterator<?> it = configContext.getMembers(); it.hasNext();) {
				SortConfigGroup sortConfig = (SortConfigGroup) it.next();
				sortConfig.getColumnInput().setLabel(number + ".");
				number++;
			}
			
		}

		public FormGroup getColumnsGroup() {
			return columns;
		}

		public Command getCancel() {
			return _cancel;
		}

		public Command getOk() {
			return _ok;
		}
		
	}

	/**
	 * A {@link LabelProvider} for {@link NamedConstant}s.
	 */
	public static final class NamedConstantResourceLabelProvider implements LabelProvider {
		private final ResourceView resources;
		
		/**
		 * Creates a new {@link NamedConstantResourceLabelProvider}.
		 */
		public NamedConstantResourceLabelProvider(ResourceView resources) {
			this.resources = resources;
		}

		@Override
		public String getLabel(Object object) {
			return resources.getStringResource(((NamedConstant) object).asString());
		}
	}

	static class SortConfigGroup extends FormGroup {

		public static final String DIRECTION_INPUT = "ascending";
		public static final String COLUMN_INPUT = "column";
		
		private static final Object ASCENDING = new NamedConstant("ascending");
		private static final Object DESCENDING = new NamedConstant("descending");
		
		private static final List<?> DIRECTIONS = Arrays.asList(new Object[] {
			ASCENDING, DESCENDING
		});
		
		
		private final SelectField columnInput;
		private final SelectField directionInput;
		private final int index;

		private List<Column> allColumns;

		private LabelProvider columnLabels;

		public SortConfigGroup(final ResourceView resources, int index, List<Column> allColumns,
				final LabelProvider columnLabels) {
			super("sort" + index, resources);
			
			this.index = index;
			this.allColumns = allColumns;
			this.columnLabels = columnLabels;
			
			this.columnInput = FormFactory.newSelectField(SortConfigGroup.COLUMN_INPUT, allColumns, false, false);
			addMember(this.columnInput);
			
			this.directionInput = FormFactory.newSelectField(SortConfigGroup.DIRECTION_INPUT, DIRECTIONS, false, false);
			addMember(this.directionInput);
			
			this.columnInput.setOptionLabelProvider(columnLabels);
			this.columnInput.setOptionComparator(LabelComparator.newCachingInstance(columnLabels));
			
			// Set explicit default value to prevent tristate behavior.
			this.directionInput.setAsSingleSelection(ASCENDING);
			this.directionInput.setOptionLabelProvider(new NamedConstantResourceLabelProvider(resources));
		}

		public void setColumn(Column name) {
			columnInput.setAsSingleSelection(name);
		}
		
		public boolean hasColumn() {
			return columnInput.getSingleSelection() != null;
		}
		
		String getColumnName() {
			Column column = (Column) columnInput.getSingleSelection();
			if (column == null) {
				return null;
			}
			return column.getName();
		}
		
		public void setAscending(boolean ascending) {
			directionInput.setAsSingleSelection(ascending ? ASCENDING : DESCENDING);
		}
		
		boolean getAscending() {
			return directionInput.getSingleSelection() == ASCENDING;
		}

		public static SortConfigGroup cloneConfig(SortConfigGroup orig, int index) {
			return new SortConfigGroup(orig.getResources(), index, orig.allColumns, orig.columnLabels);
		}

		public SelectField getColumnInput() {
			return columnInput;
		}

		public SelectField getDirectionInput() {
			return directionInput;
		}

		public int getIndex() {
			return index;
		}
	}

	/**
	 * External command to open the {@link SortConfigDialog} for a
	 * {@link ControlRepresentable} component that is rendered through a
	 * {@link TableControl}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class OpenForControlRepresentable extends AbstractCommandHandler {

		/** Command name. */
		public static final String COMMAND_NAME = "sortConfigOpenForControlRepresentable";

		/**
		 * Creates a new {@link OpenForControlRepresentable}.
		 */
		public OpenForControlRepresentable(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
			TableControl tableControl = (TableControl) ((ControlRepresentable) component).getRenderingControl();
			
			openDialog(context, tableControl.getTableData());
			
			return HandlerResult.DEFAULT_RESULT;
		}
		
	}
	
	/**
	 * External command to open the {@link SortConfigDialog} for a
	 * {@link TableComponent}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class OpenForTableComponent extends AbstractCommandHandler {
		
		/** Command name. */
		public static final String COMMAND_NAME = "sortConfigOpenForTableComponent";

		/**
		 * Creates a new {@link OpenForTableComponent}.
		 */
		public OpenForTableComponent(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
			TableControl tableControl = (TableControl) ((TableComponent) component).getRenderingControl();
			
			openDialog(context, tableControl.getTableData());
			
			return HandlerResult.DEFAULT_RESULT;
		}
		
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		SortConfigContext fc = (SortConfigContext) getFormContext();

		CommandModel okButton = MessageBox.button(ButtonType.OK, fc.getOk());
		buttons.add(okButton);
		getDialogModel().setDefaultCommand(okButton);
		buttons.add(MessageBox.button(ButtonType.CANCEL, fc.getCancel()));
	}

	@Override
	protected FormContext createFormContext() {
		return new SortConfigContext(RESOURCES, getFormId(), _table, getDialogModel().getCloseAction());
	}

	@Override
	protected void fillFormContext(FormContext context) {
		// Custom context fills itself upon creation.
	}

	@Override
	protected ControlProvider getControlProvider() {
		return CONTROL_PROVIDER;
	}
	
	@Override
	protected IconControl createTitleIcon() {
		return IconControl.icon(Icons.SORT_CONFIG_60);
	}

	@Override
	protected HTMLFragment createBodyContent() {
		return new TemplateControl(getFormContext(), getControlProvider(), TEMPLATE);
	}

}
