/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.FragmentControl;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.CheckboxControl;
import com.top_logic.layout.form.control.DefaultButtonRenderer;
import com.top_logic.layout.form.control.LabelControl;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.tag.TableTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.model.FieldSetBoxTemplate;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.AllToolNames;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BackgroundChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BaselineChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CellState;
import com.top_logic.tl.ai.core.security.layout.PolicyMatrix.RowState;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/*
 * Issue #404, variant C of the wireframes: one matrix per sub-tab, the role inspector next to it.
 *
 * The class comment below is exported to META-INF/messages_*.properties by the build's doclet and shown
 * as this component's tooltip in the layout editor, so it is written for an administrator and stays
 * short - TestConfigPresentation enforces a length limit on everything under this package prefix.
 * The design notes live here, where the doclet does not export them.
 *
 * Why the cells are controls and not form fields: fifty roles against thirty names are fifteen
 * hundred cells per sub-tab, and a form field per cell would sit in the form context whether its page
 * is shown or not. A cell is a question to the draft and a click on it is an edit of the draft, so it
 * needs no value of its own. What remains a form field is what is chosen from a list - the row state
 * and the section default - and those are one per row and one per matrix.
 *
 * Why a click cycles only between empty and granted: the third state is a prohibition that beats every
 * permission of every other role, and a click cycle makes it one accidental click away from a
 * permission. It is set and cleared through the cell's context menu and the bulk dialog, both of which
 * say what it does.
 *
 * Why the four matrices share one edit session rather than one component: only the matrix on the
 * visible sub-tab is built, and an apply, a discard and the undo history are still about the one policy
 * all four edit.
 *
 * On rebuilding: a change of *structure* - a column that appears or disappears, an apply, a discard -
 * drops the form context and invalidates the component. A change of a *value* keeps the table, its page
 * and its selection: the edited cell repaints itself, and a change that decides the look of several
 * cells - a row state, the section default, an undo - repaints the table.
 */

/**
 * Configures which security roles may use which agents, models, MCP servers or capabilities.
 *
 * <p>
 * One matrix of roles against names. Writes the policy of the tool access service; what is saved takes
 * effect when that service is restarted, and this view offers the restart.
 * </p>
 *
 * @see ToolCatalogModelBuilder
 * @see EffectiveToolAccessComponent
 */
public class ToolAccessPolicyComponent extends FormComponent {

	/** Name of the table holding the matrix. */
	public static final String FIELD_MATRIX = "matrix";

	/** Name of the field searching the roles by name. */
	public static final String FIELD_SEARCH = "search";

	/** Name of the field choosing which rows to show by their state. */
	public static final String FIELD_ROW_FILTER = "rowFilter";

	/** Name of the switch showing what a run of each role would actually do. */
	public static final String FIELD_OVERLAY = "overlay";

	/** Name of the switch asking about unattended runs instead of conversations. */
	public static final String FIELD_UNATTENDED = "unattended";

	/** Name of the command opening the bulk edit over the selected roles. */
	public static final String FIELD_EDIT_SELECTION = "editSelection";

	/** Name of the field holding what a section admits from a role it does not name. */
	public static final String FIELD_SECTION_DEFAULT = "sectionDefault";

	/** Name of the field in each row choosing whether the section names the role. */
	public static final String FIELD_ROW_STATE = "rowState";

	/** Name of the group holding which tools belong to which capability group. */
	public static final String GROUP_ASSIGNMENTS = "assignments";

	/** Name of the field choosing capability groups. */
	public static final String FIELD_GROUPS = "groups";

	/** Name of the field choosing single tools. */
	public static final String FIELD_TOOLS = "tools";

	/** Name of the command adding something to the list it sits in. */
	public static final String FIELD_ADD = "add";

	/** Name of the command removing the thing it sits in. */
	public static final String FIELD_REMOVE = "remove";

	/** Name of the command restarting the service, next to the banner that asks for it. */
	public static final String FIELD_RESTART = "restart";

	/** Column of the role names. */
	static final String COLUMN_ROLE = "role";

	/** Column of the row states, whose header carries the section default. */
	static final String COLUMN_STATE = "state";

	/** Column counting the entries of a role that name single tools. */
	static final String COLUMN_TOOLS = "tools";

	/** Prefix of the columns of the names, followed by their position. */
	static final String COLUMN_NAME_PREFIX = "n";

	/** How many rows a page of the matrix shows. */
	private static final int PAGE_SIZE = 25;

	/**
	 * Which rows the matrix shows, by whether the section names them.
	 */
	public enum RowFilter {

		/** Every role. */
		ALL,

		/** The roles the section names. */
		NAMED,

		/** The roles the section does not name. */
		NOT_NAMED;
	}

	/**
	 * One row of the matrix.
	 *
	 * @param kind
	 *        What the row is about.
	 * @param roleName
	 *        The role, or <code>null</code> for a row no role owns.
	 */
	public record MatrixRow(Kind kind, String roleName) {

		/**
		 * What a row of the matrix is about.
		 */
		public enum Kind {

			/** What the basic permissions grant every account. Only in the capability matrix. */
			BASELINE,

			/** What an unattended run may reach at most. Only in the capability matrix. */
			BACKGROUND,

			/** What one role may use. */
			ROLE;
		}

		/** The row of the given role. */
		public static MatrixRow role(String roleName) {
			return new MatrixRow(Kind.ROLE, roleName);
		}

		/** Whether a role owns the row. */
		public boolean isRole() {
			return kind == Kind.ROLE;
		}
	}

	/**
	 * Configuration options for {@link ToolAccessPolicyComponent}.
	 */
	public interface Config extends FormComponent.Config {

		/** @see #getDimension() */
		String DIMENSION = "dimension";

		@Override
		@ClassDefault(ToolAccessPolicyComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

		/**
		 * Which part of the policy this matrix edits.
		 */
		@Name(DIMENSION)
		@Mandatory
		MatrixDimension getDimension();
	}

	private final MatrixDimension _dimension;

	/** The revision of the edit session the form was built or last brought in line with. */
	private int _shownRevision = -1;

	/** The columns the form was built with; a change of them needs a new table. */
	private List<String> _shownColumns = List.of();

	/** The row the inspector describes, or <code>null</code>. Survives a rebuild. */
	private MatrixRow _inspected;

	/** What the search field says. Survives a rebuild. */
	private String _search = "";

	/** Which rows are shown. Survives a rebuild. */
	private RowFilter _rowFilter = RowFilter.ALL;

	/** Whether the overlay is on. Survives a rebuild. */
	private boolean _overlay;

	/** Whether the capability overlay asks about unattended runs. Survives a rebuild. */
	private boolean _unattended;

	/** Every block of the view that is recomputed when a value changes. */
	private final Map<String, DynamicFragment> _dynamic = new LinkedHashMap<>();

	/** The row-state field of each row that has one. */
	private final Map<MatrixRow, SelectField> _rowStateFields = new HashMap<>();

	/** The capability pickers of the group assignments, so that they can be kept apart. */
	private final List<SelectField> _assignmentGroups = new ArrayList<>();

	private TableControl _table;

	private FormTableModel _tableModel;

	private TableField _matrixField;

	private SelectField _defaultField;

	/** The column names of the table, and the name each stands for. */
	private final Map<String, String> _columnNames = new LinkedHashMap<>();

	/** The roles the application has, read once per build. */
	private Set<String> _existingRoles = Set.of();

	/** The names of this dimension the application has, read once per build. */
	private Set<String> _knownNames = Set.of();

	private DraftAccess _access;

	private int _accessRevision = -1;

	/**
	 * Whether a value listener is currently bringing dependent fields in line.
	 *
	 * <p>
	 * Setting the value of a field fires its own listener, and that listener would write the value back
	 * into the draft.
	 * </p>
	 */
	private boolean _syncing;

	/**
	 * Creates a {@link ToolAccessPolicyComponent} from configuration.
	 */
	@CalledByReflection
	public ToolAccessPolicyComponent(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);
		_dimension = config.getDimension();
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		// The view is about the application's policy; there is no business object it could be opened on.
		return object == null;
	}

	/** Which part of the policy this matrix edits. */
	public MatrixDimension dimension() {
		return _dimension;
	}

	/** The editing session this matrix shares with the other three. */
	public PolicyEditSession session() {
		return PolicyEditSession.of(this);
	}

	private PolicyDraft draft() {
		return session().draft();
	}

	/** This component's matrix over the current draft. */
	PolicyMatrix matrix() {
		return new PolicyMatrix(draft(), _dimension);
	}

	// ------------------------------------------------------------- building

	@Override
	public FormContext createFormContext() {
		_dynamic.clear();
		_rowStateFields.clear();
		_assignmentGroups.clear();
		_columnNames.clear();
		_table = null;
		FormContext context = new FormContext(this);
		if (!AiToolAccessService.Module.INSTANCE.isActive()) {
			return unreadable(context, I18NConstants.SERVICE_NOT_RUNNING);
		}
		PolicyEditSession session = session();
		if (session.draft() == null) {
			return unreadable(context, I18NConstants.POLICY_UNREADABLE);
		}
		_shownRevision = session.revision();
		_existingRoles = existingRoleNames();
		_knownNames = knownNames(_dimension);

		HTMLFragment bar = addBarFields(context);
		_matrixField = createMatrix(context);
		context.addMember(_matrixField);
		_table = TableTag.createTableControl(_matrixField);

		List<HTMLTemplateFragment> content = new ArrayList<>();
		content.add(htmlTemplate(messages(context)));
		content.add(htmlTemplate(bar));
		content.add(div(css("aiPolicyMatrixSplit"),
			div(css("aiPolicyMatrixTable"), htmlTemplate(_table)),
			div(css("aiPolicyInspector"), htmlTemplate(dynamic("inspector", this::inspector)))));
		if (_dimension == MatrixDimension.CAPABILITIES) {
			addAssignmentGroup(context, content);
		}
		template(context, div(css("aiSecurityView aiPolicyMatrixView"), fragment(content)));

		if (!mayWrite()) {
			// Reading the policy is what the two other tabs are for; writing it is an administrator's
			// job, and the fields of a form are not command handlers, so nothing would have checked.
			context.setImmutable(true);
		}
		return context;
	}

	private static FormContext unreadable(FormContext context, ResKey message) {
		template(context, div(css("aiSecurityView"), htmlTemplate(note(message))));
		return context;
	}

	/** Whether the acting account may change the policy rather than only look at it. */
	boolean mayWrite() {
		return BoundChecker.allowCommand(this, SimpleBoundCommandGroup.WRITE, getModel());
	}

	/**
	 * Rebuilds the form and repaints the whole view.
	 *
	 * <p>
	 * For every change that adds or removes a column, and for an apply, a discard or a restart, after
	 * which the view shows another draft.
	 * </p>
	 */
	private HandlerResult rebuild() {
		removeFormContext();
		invalidate();
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected void becomingVisible() {
		super.becomingVisible();
		if (hasFormContext() && _shownRevision != session().revision()) {
			// Another sub-tab changed the draft, undid a step or applied it.
			refreshAll();
		}
	}

	/**
	 * Brings everything in line with the draft after a change the edited cell cannot show on its own.
	 *
	 * <p>
	 * Keeps the table, its page and its selection where it can, and rebuilds only when the columns
	 * changed - an undo of the edit that removed the last mention of an unknown name, for example.
	 * </p>
	 */
	void refreshAll() {
		if (!hasFormContext() || _table == null || draft() == null) {
			rebuild();
			return;
		}
		if (!currentColumns().equals(_shownColumns)) {
			rebuild();
			return;
		}
		_shownRevision = session().revision();
		syncing(() -> {
			if (_defaultField != null) {
				_defaultField.setAsSingleSelection(draft().sectionDefault(_dimension.runDimension()));
			}
			for (Map.Entry<MatrixRow, SelectField> entry : _rowStateFields.entrySet()) {
				entry.getValue().setAsSingleSelection(rowStateValue(entry.getKey()));
			}
		});
		updateRows();
		_table.requestRepaint();
		refreshNotes();
	}

	/** After a click: the cell repaints itself; the notes, the warnings and the inspector follow. */
	void afterCellEdit() {
		_shownRevision = session().revision();
		refreshNotes();
	}

	/** Brings dependent fields in line with the draft, without letting them write it again. */
	private void syncing(Runnable update) {
		boolean outer = _syncing;
		_syncing = true;
		try {
			update.run();
		} finally {
			_syncing = outer;
		}
	}

	/** Repaints the blocks that depend on what the policy says, without rebuilding a field. */
	private void refreshNotes() {
		for (DynamicFragment note : _dynamic.values()) {
			note.requestRepaint();
		}
	}

	// ------------------------------------------------------------- the toolbar

	/**
	 * The search, the row filter and the overlay switch, in one line above the matrix.
	 *
	 * <p>
	 * They are transient: they choose what is shown and change nothing, so they must not make the form
	 * look changed.
	 * </p>
	 */
	private HTMLFragment addBarFields(FormContext context) {
		StringField search = FormFactory.newStringField(FIELD_SEARCH);
		search.setValue(_search);
		labelAndHelp(search, I18NConstants.FIELD_ROLE_SEARCH);
		search.setPlaceholder(resources().getString(I18NConstants.FIELD_ROLE_SEARCH));
		search.addValueListener((changed, oldValue, newValue) -> {
			_search = newValue == null ? "" : newValue.toString().trim();
			updateRows();
		});
		search.setInheritDeactivation(false);
		search.setTransient(true);
		context.addMember(search);

		SelectField filter = FormFactory.newSelectField(FIELD_ROW_FILTER, List.of(RowFilter.values()),
			false, List.of(_rowFilter), false, false, null);
		filter.setMandatory(true);
		filter.setOptionLabelProvider(this::rowFilterLabel);
		labelAndHelp(filter, I18NConstants.FIELD_ROW_FILTER);
		filter.addValueListener((changed, oldValue, newValue) -> {
			_rowFilter = single(newValue, RowFilter.ALL);
			updateRows();
		});
		filter.setInheritDeactivation(false);
		filter.setTransient(true);
		context.addMember(filter);

		BooleanField overlay = FormFactory.newBooleanField(FIELD_OVERLAY);
		overlay.initializeField(Boolean.valueOf(_overlay));
		labelAndHelp(overlay, I18NConstants.FIELD_OVERLAY);
		overlay.addValueListener((changed, oldValue, newValue) -> {
			_overlay = Boolean.TRUE.equals(newValue);
			if (_table != null) {
				_table.requestRepaint();
			}
		});
		overlay.setInheritDeactivation(false);
		overlay.setTransient(true);
		context.addMember(overlay);

		List<HTMLFragment> views = new ArrayList<>();
		views.add(new TextInputControl(search));
		views.add(new SelectControl(filter, true));
		if (_dimension == MatrixDimension.CAPABILITIES) {
			BooleanField unattended = FormFactory.newBooleanField(FIELD_UNATTENDED);
			unattended.initializeField(Boolean.valueOf(_unattended));
			labelAndHelp(unattended, I18NConstants.FIELD_UNATTENDED);
			unattended.addValueListener((changed, oldValue, newValue) -> {
				_unattended = Boolean.TRUE.equals(newValue);
				if (_table != null) {
					_table.requestRepaint();
				}
				refreshNotes();
			});
			unattended.setInheritDeactivation(false);
			unattended.setTransient(true);
			context.addMember(unattended);
			views.add(switchView(unattended));
		}
		views.add(switchView(overlay));
		if (mayWrite()) {
			CommandField bulk = FormFactory.newCommandField(FIELD_EDIT_SELECTION, this::openEditSelection);
			bulk.setLabel(resources().getString(I18NConstants.COMMAND_EDIT_SELECTION));
			bulk.setTooltip(resources().getString(I18NConstants.COMMAND_EDIT_SELECTION.tooltip()));
			bulk.setInheritDeactivation(false);
			context.addMember(bulk);
			views.add(Fragments.span("aiPolicyBulkButton",
				new ButtonControl(bulk, DefaultButtonRenderer.INSTANCE)));
		}

		return Fragments.div("aiPolicyBar", views.toArray(new HTMLFragment[views.size()]));
	}

	/** A checkbox followed by its label, as one piece of the toolbar. */
	private static HTMLFragment switchView(BooleanField field) {
		return Fragments.span("aiPolicySwitch", new CheckboxControl(field), new LabelControl(field));
	}

	private String rowFilterLabel(Object option) {
		RowFilter filter = option instanceof RowFilter value ? value : RowFilter.ALL;
		boolean run = _dimension.isRunSection();
		switch (filter) {
			case NAMED:
				return resources().getString(run ? I18NConstants.ROW_FILTER_RESTRICTED
					: I18NConstants.ROW_FILTER_WITH_ENTRY);
			case NOT_NAMED:
				return resources().getString(run ? I18NConstants.ROW_FILTER_NOT_NAMED
					: I18NConstants.ROW_FILTER_WITHOUT_ENTRY);
			default:
				return resources().getString(I18NConstants.ROW_FILTER_ALL);
		}
	}

	// -------------------------------------------------------------- the matrix

	/** The names that are columns now; a change of them needs a new table. */
	private List<String> currentColumns() {
		return matrix().columns(_knownNames);
	}

	private TableField createMatrix(FormContext context) {
		PolicyMatrix matrix = matrix();
		List<String> names = matrix.columns(_knownNames);
		_shownColumns = names;

		TableConfiguration config = TableConfigurationFactory.table();
		config.setResPrefix(getResPrefix().append(FIELD_MATRIX));
		config.setFilterProvider(null);
		config.setDefaultFilterProvider(null);
		config.setShowTitle(false);
		config.setPageSizeOptions(PAGE_SIZE, 50, 100);
		config.setRowClassProvider((view, rowOptions, displayedRow, row) -> rowClass(
			view.getViewModel().getRowObject(row)));
		config.getDefaultColumn().setSortable(false);
		config.getDefaultColumn().setFilterProvider(null);

		List<String> columns = new ArrayList<>();
		ColumnConfiguration role = config.declareColumn(COLUMN_ROLE);
		role.setColumnLabelKey(I18NConstants.COLUMN_ROLE);
		role.setDefaultColumnWidth("160px");
		role.setAccessor(ROW_ACCESSOR);
		role.setRenderer((displayContext, out, value) -> roleLabel((MatrixRow) value).write(displayContext, out));
		columns.add(COLUMN_ROLE);

		ColumnConfiguration state = config.declareColumn(COLUMN_STATE);
		state.setColumnLabelKey(_dimension.isRunSection() ? I18NConstants.COLUMN_ROW_STATE
			: I18NConstants.COLUMN_ENTRY);
		state.setDefaultColumnWidth("150px");
		state.setAccessor(ROW_ACCESSOR);
		state.setFieldProvider(new RowStateFieldProvider());
		state.setControlProvider(DefaultFormFieldControlProvider.INSTANCE);
		if (_dimension.isRunSection()) {
			state.setHeadControlProvider(this::sectionDefaultHeader);
		}
		columns.add(COLUMN_STATE);

		int position = 0;
		for (String name : names) {
			String column = COLUMN_NAME_PREFIX + position++;
			_columnNames.put(column, name);
			ColumnConfiguration nameColumn = config.declareColumn(column);
			nameColumn.setColumnLabel(columnLabel(name));
			nameColumn.setDefaultColumnWidth("84px");
			nameColumn.setAccessor(new CellAccessor(name));
			nameColumn.setControlProvider(CELLS);
			nameColumn.setHeadControlProvider((model, style) -> new FragmentControl(columnHeader(name)));
			if (isUnknownName(name)) {
				nameColumn.setCssHeaderClass("aiPolicyUnknown");
				nameColumn.setCssClass("aiPolicyUnknown");
			}
			columns.add(column);
		}
		if (_dimension == MatrixDimension.CAPABILITIES) {
			ColumnConfiguration tools = config.declareColumn(COLUMN_TOOLS);
			tools.setColumnLabelKey(I18NConstants.COLUMN_TOOLS);
			tools.setDefaultColumnWidth("110px");
			tools.setAccessor(new CellAccessor(null));
			tools.setControlProvider(CELLS);
			columns.add(COLUMN_TOOLS);
		}
		config.setDefaultColumns(columns);
		// The role and its row state stay in view; the selection column is not counted.
		config.setFixedColumnCount(2);

		ObjectTableModel inner = new ObjectTableModel(columns, config, rows());
		_tableModel = new FormTableModel(inner, context);
		TableField field = FormFactory.newTableField(FIELD_MATRIX, _tableModel, true);
		DefaultMultiSelectionModel<Object> selection = new DefaultMultiSelectionModel<>(
			row -> row instanceof MatrixRow matrixRow && matrixRow.isRole(), field);
		selection.addSelectionListener((model, event) -> inspectSelection(model));
		field.setSelectionModel(selection);
		if (_inspected != null && _inspected.isRole() && rows().contains(_inspected)) {
			selection.setSelection(Set.of(_inspected), _inspected);
		}
		field.setInheritDeactivation(false);
		field.setTransient(true);

		if (_dimension.isRunSection()) {
			_defaultField = FormFactory.newSelectField(FIELD_SECTION_DEFAULT,
				PolicyDraft.sectionDefaults(_dimension.runDimension()), false,
				List.of(draft().sectionDefault(_dimension.runDimension())), false, false, null);
			_defaultField.setMandatory(true);
			_defaultField.setOptionLabelProvider(MetaLabelProvider.INSTANCE);
			labelAndHelp(_defaultField, I18NConstants.FIELD_SECTION_DEFAULT);
			_defaultField.setTransient(true);
			_defaultField.addValueListener((changed, oldValue, newValue) -> {
				if (_syncing) {
					return;
				}
				session().edit(() -> draft().setSectionDefault(_dimension.runDimension(), single(newValue, null)));
				refreshAll();
			});
			_tableModel.getColumnGroup(COLUMN_STATE).addMember(_defaultField);
		} else {
			_defaultField = null;
		}
		return field;
	}

	/**
	 * The rows the search and the filter let through; the two rows no role owns always come first.
	 */
	private List<MatrixRow> rows() {
		List<MatrixRow> result = new ArrayList<>();
		if (_dimension == MatrixDimension.CAPABILITIES) {
			result.add(new MatrixRow(MatrixRow.Kind.BASELINE, null));
			result.add(new MatrixRow(MatrixRow.Kind.BACKGROUND, null));
		}
		PolicyMatrix matrix = matrix();
		String search = _search.toLowerCase(Locale.ROOT);
		for (String roleName : matrix.roles(_existingRoles)) {
			if (!search.isEmpty() && !roleName.toLowerCase(Locale.ROOT).contains(search)
				&& !roleLabelText(roleName).toLowerCase(Locale.ROOT).contains(search)) {
				continue;
			}
			RowState state = matrix.rowState(roleName);
			if (_rowFilter == RowFilter.NAMED && state != RowState.NAMED
				|| _rowFilter == RowFilter.NOT_NAMED && state != RowState.NOT_NAMED) {
				continue;
			}
			result.add(MatrixRow.role(roleName));
		}
		return result;
	}

	/** Shows the rows the search and the filter let through, keeping the selection of the ones left. */
	private void updateRows() {
		if (_tableModel == null) {
			return;
		}
		List<MatrixRow> rows = rows();
		_rowStateFields.keySet().retainAll(rows);
		_tableModel.setRowObjects(rows);
	}

	private String rowClass(Object row) {
		if (!(row instanceof MatrixRow matrixRow)) {
			return null;
		}
		List<String> classes = new ArrayList<>();
		if (!matrixRow.isRole()) {
			classes.add("aiPolicySpecialRow");
		} else {
			if (!roleKnown(matrixRow.roleName())) {
				classes.add("aiPolicyUnknown");
			}
			if (matrix().isLocked(matrixRow.roleName())) {
				classes.add("aiPolicyLockedRow");
			}
		}
		if (matrixRow.equals(_inspected)) {
			classes.add("aiPolicyInspectedRow");
		}
		return classes.isEmpty() ? null : String.join(" ", classes);
	}

	/** The role, as its label with the technical name under it, or one of the two special rows. */
	private HTMLFragment roleLabel(MatrixRow row) {
		switch (row.kind()) {
			case BASELINE:
				return Fragments.div("aiPolicyRole",
					Fragments.div("aiPolicyRoleLabel", Fragments.message(I18NConstants.ROW_BASELINE)),
					Fragments.div("aiPolicyRoleName", Fragments.text("<baseline>")));
			case BACKGROUND:
				return Fragments.div("aiPolicyRole",
					Fragments.div("aiPolicyRoleLabel", Fragments.message(I18NConstants.ROW_BACKGROUND)),
					Fragments.div("aiPolicyRoleName", Fragments.text("<background-allowlist>")));
			default:
				String roleName = row.roleName();
				String label = roleLabelText(roleName);
				HTMLFragment second;
				if (!roleKnown(roleName)) {
					second = Fragments.div("aiPolicyRoleName aiSecurityWarning",
						Fragments.message(I18NConstants.ROLE_UNKNOWN__ROLE.fill(roleName)));
				} else if (!label.equals(roleName)) {
					second = Fragments.div("aiPolicyRoleName", Fragments.text(roleName));
				} else {
					// The label is the name: saying it twice is noise.
					second = Fragments.empty();
				}
				return Fragments.div("aiPolicyRole", Fragments.div("aiPolicyRoleLabel", Fragments.text(label)),
					second);
		}
	}

	/** The header of the row-state column: the section default, directly above the rows it acts on. */
	private FragmentControl sectionDefaultHeader(Object model, String style) {
		FormContainer group = (FormContainer) model;
		SelectField field = (SelectField) group.getMember(FIELD_SECTION_DEFAULT);
		return new FragmentControl(Fragments.div("aiPolicyDefault",
			Fragments.div("aiPolicyDefaultLabel", Fragments.message(I18NConstants.FIELD_SECTION_DEFAULT)),
			new SelectControl(field, true)));
	}

	/** The header of a name column: the name, and what is special about it. */
	private HTMLFragment columnHeader(String name) {
		List<HTMLFragment> parts = new ArrayList<>();
		parts.add(Fragments.div("aiPolicyColumnName", Fragments.text(columnLabel(name))));
		String detail = columnDetail(name);
		if (!detail.isEmpty()) {
			parts.add(Fragments.div(isUnknownName(name) ? "aiPolicyColumnDetail aiSecurityWarning"
				: "aiPolicyColumnDetail", Fragments.text(detail)));
		}
		return Fragments.div("aiPolicyColumn", parts.toArray(new HTMLFragment[parts.size()]));
	}

	/** How a name is labelled in its column header. */
	private String columnLabel(String name) {
		if (_dimension == MatrixDimension.CAPABILITIES) {
			ResKey label = ToolGroups.labelKey(name);
			return label == null ? name : resources().getString(label);
		}
		return name;
	}

	/** The small line under a column label. */
	private String columnDetail(String name) {
		if (isUnknownName(name)) {
			return resources().getString(I18NConstants.NAME_UNKNOWN);
		}
		switch (_dimension) {
			case AGENTS: {
				AgentFacts facts = AgentFacts.live();
				if (facts.isInfrastructure(name)) {
					return resources().getString(I18NConstants.AGENT_INFRASTRUCTURE);
				}
				String model = facts.assignedModel(name);
				return model == null ? "" : resources().getString(I18NConstants.AGENT_MODEL__MODEL.fill(model));
			}
			case CAPABILITIES: {
				String risk = ToolAccessLabels.risk(name);
				return risk.isEmpty() ? name : name + " " + risk;
			}
			default:
				return "";
		}
	}

	/** Whether the policy names something the application does not have. */
	boolean isUnknownName(String name) {
		return _dimension.isRunSection() && !_knownNames.isEmpty() && !_knownNames.contains(name);
	}

	// ------------------------------------------------------------- row states

	/**
	 * The row-state field of one row: whether the section names the role, or which of their three
	 * states the two rows no role owns are in.
	 */
	private final class RowStateFieldProvider extends AbstractFieldProvider {

		@Override
		public FormMember createField(Object model, Accessor accessor, String property) {
			MatrixRow row = (MatrixRow) model;
			SelectField field = FormFactory.newSelectField(FIELD_ROW_STATE, rowStateOptions(row), false,
				List.of(rowStateValue(row)), false, false, null);
			field.setMandatory(true);
			field.setOptionLabelProvider(option -> rowStateLabel(option));
			field.setLabel(resources().getString(_dimension.isRunSection() ? I18NConstants.COLUMN_ROW_STATE
				: I18NConstants.COLUMN_ENTRY));
			field.setTooltip(resources().getString(rowStateTooltip(row)));
			field.setTransient(true);
			field.addValueListener((changed, oldValue, newValue) -> {
				if (_syncing) {
					return;
				}
				Object value = single(newValue, null);
				if (value != null) {
					session().edit(() -> setRowState(row, value));
					refreshAll();
				}
			});
			if (!mayWrite()) {
				field.setImmutable(true);
			}
			_rowStateFields.put(row, field);
			return field;
		}

		@Override
		public String getFieldName(Object model, Accessor accessor, String property) {
			return FIELD_ROW_STATE;
		}
	}

	private static List<?> rowStateOptions(MatrixRow row) {
		switch (row.kind()) {
			case BASELINE:
				return List.of(BaselineChoice.values());
			case BACKGROUND:
				return List.of(BackgroundChoice.values());
			default:
				return List.of(RowState.values());
		}
	}

	private Object rowStateValue(MatrixRow row) {
		switch (row.kind()) {
			case BASELINE:
				return draft().baselineChoice();
			case BACKGROUND:
				return draft().backgroundChoice();
			default:
				return matrix().rowState(row.roleName());
		}
	}

	private void setRowState(MatrixRow row, Object value) {
		if (value instanceof BaselineChoice choice) {
			draft().setBaselineChoice(choice);
		} else if (value instanceof BackgroundChoice choice) {
			draft().setBackgroundChoice(choice);
		} else if (value instanceof RowState state && row.isRole()) {
			matrix().setRowState(row.roleName(), state);
		}
	}

	private String rowStateLabel(Object option) {
		if (option instanceof BaselineChoice choice) {
			return resources().getString(ToolAccessLabels.baselineChoice(choice));
		}
		if (option instanceof BackgroundChoice choice) {
			return resources().getString(ToolAccessLabels.backgroundChoice(choice));
		}
		boolean named = option == RowState.NAMED;
		if (_dimension.isRunSection()) {
			return resources().getString(named ? I18NConstants.ROW_STATE_RESTRICTED
				: I18NConstants.ROW_STATE_NOT_NAMED);
		}
		return resources().getString(named ? I18NConstants.ROW_STATE_ENTRY : I18NConstants.ROW_STATE_NO_ENTRY);
	}

	private ResKey rowStateTooltip(MatrixRow row) {
		switch (row.kind()) {
			case BASELINE:
				return I18NConstants.ROW_BASELINE.tooltip();
			case BACKGROUND:
				return I18NConstants.ROW_BACKGROUND.tooltip();
			default:
				return _dimension.isRunSection() ? I18NConstants.COLUMN_ROW_STATE.tooltip()
					: I18NConstants.COLUMN_ENTRY.tooltip();
		}
	}

	// ------------------------------------------------------------------ cells

	/**
	 * What one cell of the matrix shows: its state, whether it can be clicked, and what the overlay adds.
	 */
	PolicyCellControl.Look look(PolicyCell cell) {
		MatrixRow row = cell.row();
		String name = cell.name();
		PolicyMatrix matrix = matrix();
		switch (row.kind()) {
			case BASELINE: {
				boolean custom = draft().baselineChoice() == BaselineChoice.CUSTOM;
				return PolicyCellControl.Look.of(matrix.baselineCell(name) ? CellState.GRANTED : CellState.ABSENT,
					!custom, DraftAccess.Mark.NONE, baselineTooltip(name));
			}
			case BACKGROUND: {
				boolean custom = draft().backgroundChoice() == BackgroundChoice.CUSTOM;
				return PolicyCellControl.Look.of(matrix.backgroundCell(name) ? CellState.GRANTED : CellState.ABSENT,
					!custom, DraftAccess.Mark.NONE, backgroundTooltip(name));
			}
			default:
				break;
		}
		String roleName = row.roleName();
		if (_dimension.isRunSection()) {
			Dimension dimension = _dimension.runDimension();
			DraftAccess.RunVerdict verdict = access().run(roleName, dimension, name);
			if (matrix.isLocked(roleName)) {
				// Without the overlay the row shows what the default means; with it, what it does here.
				PolicyCellControl.Ghost ghost;
				if (!_overlay) {
					ghost = ghostState();
				} else if (!verdict.allowed()) {
					ghost = PolicyCellControl.Ghost.NONE;
				} else {
					ghost = verdict.mark() == DraftAccess.Mark.AGENTS_OWN ? PolicyCellControl.Ghost.AGENTS_OWN
						: PolicyCellControl.Ghost.GRANTED;
				}
				DraftAccess.Mark mark = _overlay && verdict.mark() == DraftAccess.Mark.REFUSED
					? DraftAccess.Mark.REFUSED : DraftAccess.Mark.NONE;
				return PolicyCellControl.Look.ghost(ghost, mark, cellTooltip(roleName, name, verdict));
			}
			DraftAccess.Mark mark = _overlay ? verdict.mark() : DraftAccess.Mark.NONE;
			return PolicyCellControl.Look.of(matrix.cell(roleName, name), false, mark,
				cellTooltip(roleName, name, verdict));
		}
		DraftAccess.CapabilityVerdict verdict = access().capability(roleName, name);
		CellState state = matrix.cell(roleName, name);
		boolean refused = _overlay && state == CellState.GRANTED && !verdict.allowed(_unattended);
		return PolicyCellControl.Look.of(state, false, refused ? DraftAccess.Mark.REFUSED : DraftAccess.Mark.NONE,
			ToolAccessLabels.sentence(verdict.explanation())
				+ (_unattended && !verdict.background() ? " " + resources().getString(I18NConstants.CELL_NOT_IN_BACKGROUND)
					: ""));
	}

	/** What a locked row shows in every cell: what the section default gives a role it does not name. */
	private PolicyCellControl.Ghost ghostState() {
		switch (access().fallback(_dimension.runDimension())) {
			case ALL:
				return PolicyCellControl.Ghost.GRANTED;
			case NONE:
				return PolicyCellControl.Ghost.NONE;
			default:
				return PolicyCellControl.Ghost.AGENTS_OWN;
		}
	}

	private String cellTooltip(String roleName, String name, DraftAccess.RunVerdict verdict) {
		StringBuilder result = new StringBuilder();
		if (matrix().isLocked(roleName)) {
			result.append(resources().getString(I18NConstants.CELL_LOCKED)).append(' ');
		} else if (matrix().cell(roleName, name) == CellState.DENIED) {
			result.append(resources().getString(I18NConstants.CELL_DENIED)).append(' ');
		}
		result.append(resources().getString(I18NConstants.CELL_RUN_VERDICT__VERDICT
			.fill(ToolAccessLabels.sentence(verdict.decision()))));
		return result.toString();
	}

	private String baselineTooltip(String group) {
		if (draft().baselineChoice() != BaselineChoice.CUSTOM) {
			return resources().getString(I18NConstants.CELL_BASELINE_LOCKED);
		}
		return ToolAccessLabels.groupLabel(group);
	}

	private String backgroundTooltip(String group) {
		if (draft().backgroundChoice() != BackgroundChoice.CUSTOM) {
			return resources().getString(I18NConstants.CELL_BACKGROUND_LOCKED);
		}
		return ToolAccessLabels.groupLabel(group);
	}

	/**
	 * Toggles a cell between empty and granted, the one edit a click makes.
	 *
	 * <p>
	 * A prohibited cell does not react: clearing a prohibition is as deliberate a decision as setting
	 * one, and is taken through the context menu.
	 * </p>
	 *
	 * @return Whether anything changed.
	 */
	boolean click(PolicyCell cell) {
		if (!mayWrite() || cell.isTools()) {
			return false;
		}
		MatrixRow row = cell.row();
		String name = cell.name();
		PolicyEditSession session = session();
		int before = session.revision();
		switch (row.kind()) {
			case BASELINE:
				session.edit(() -> matrix().setBaselineCell(name, !matrix().baselineCell(name)));
				break;
			case BACKGROUND:
				session.edit(() -> matrix().setBackgroundCell(name, !matrix().backgroundCell(name)));
				break;
			default:
				PolicyMatrix matrix = matrix();
				String roleName = row.roleName();
				CellState state = matrix.cell(roleName, name);
				if (matrix.isLocked(roleName) || state == CellState.DENIED) {
					return false;
				}
				return set(cell, state == CellState.GRANTED ? CellState.ABSENT : CellState.GRANTED);
		}
		afterCellEdit();
		return session.revision() != before;
	}

	/**
	 * Puts a cell of a role into the given state, the edit of the context menu.
	 *
	 * @return Whether anything changed.
	 */
	boolean set(PolicyCell cell, CellState state) {
		if (!mayWrite() || !cell.row().isRole() || cell.isTools()) {
			return false;
		}
		String roleName = cell.row().roleName();
		PolicyEditSession session = session();
		int before = session.revision();
		boolean named = matrix().rowState(roleName) == RowState.NAMED;
		session.edit(() -> matrix().setCell(roleName, cell.name(), state));
		if (!named && matrix().rowState(roleName) == RowState.NAMED) {
			// The first mark of a role in the capability matrix gives it an entry; its row says so.
			SelectField field = _rowStateFields.get(cell.row());
			if (field != null) {
				syncing(() -> field.setAsSingleSelection(RowState.NAMED));
			}
		}
		afterCellEdit();
		return session.revision() != before;
	}

	/** Whether the context menu of the given cell has anything to offer. */
	boolean hasMenu(PolicyCell cell) {
		return mayWrite() && cell.row().isRole() && !cell.isTools()
			&& !matrix().isLocked(cell.row().roleName());
	}

	/** What the matrix says about one row and one name. */
	CellState state(PolicyCell cell) {
		return matrix().cell(cell.row().roleName(), cell.name());
	}

	/** How the single-tool cell of a row summarizes what the row lists. */
	String toolsSummary(MatrixRow row) {
		List<String> granted;
		List<String> denied;
		switch (row.kind()) {
			case BASELINE:
				granted = new ArrayList<>(draft().baselineTools());
				denied = List.of();
				break;
			case BACKGROUND:
				granted = new ArrayList<>(draft().backgroundTools());
				denied = List.of();
				break;
			default:
				granted = draft().roleTools(row.roleName(), false);
				denied = draft().roleTools(row.roleName(), true);
				break;
		}
		if (granted.isEmpty() && denied.isEmpty()) {
			return "–";
		}
		List<String> parts = new ArrayList<>();
		if (!granted.isEmpty()) {
			parts.add(granted.size() + " ✓");
		}
		if (!denied.isEmpty()) {
			parts.add(denied.size() + " ✕");
		}
		return String.join(" · ", parts);
	}

	/** Whether the single tools of the given row can be edited right now. */
	boolean mayEditTools(MatrixRow row) {
		switch (row.kind()) {
			case BASELINE:
				return mayWrite() && draft().baselineChoice() == BaselineChoice.CUSTOM;
			case BACKGROUND:
				return mayWrite() && draft().backgroundChoice() == BackgroundChoice.CUSTOM;
			default:
				return mayWrite();
		}
	}

	/** Opens the dialog listing the single tools of one row. */
	HandlerResult openTools(DisplayContext context, MatrixRow row) {
		return new ToolEntriesDialog(this, row).open(context);
	}

	/** Writes the single tools the dialog chose. */
	void setTools(MatrixRow row, List<String> granted, List<String> denied) {
		session().edit(() -> {
			switch (row.kind()) {
				case BASELINE:
					draft().setBaselineTools(granted);
					break;
				case BACKGROUND:
					draft().setBackgroundTools(granted);
					break;
				default:
					draft().setRoleTools(row.roleName(), false, granted);
					draft().setRoleTools(row.roleName(), true, denied);
					break;
			}
		});
		refreshAll();
	}

	/** The evaluation of the draft, recomputed once per change. */
	DraftAccess access() {
		int revision = session().revision();
		if (_access == null || _accessRevision != revision || _access.draft() != draft()) {
			_access = new DraftAccess(draft(), AgentFacts.live(), ToolAccessPolicyComponent::knownNameList);
			_accessRevision = revision;
		}
		return _access;
	}

	// ------------------------------------------------------------- inspector

	private void inspectSelection(SelectionModel<?> model) {
		Set<?> selection = model.getSelection();
		MatrixRow lead = null;
		for (Object row : selection) {
			if (row instanceof MatrixRow matrixRow) {
				if (matrixRow.equals(_inspected)) {
					lead = matrixRow;
					break;
				}
				lead = matrixRow;
			}
		}
		if (lead != null || selection.isEmpty()) {
			_inspected = lead;
		}
		DynamicFragment inspector = _dynamic.get("inspector");
		if (inspector != null) {
			inspector.requestRepaint();
		}
		if (_table != null) {
			_table.requestRepaint();
		}
	}

	private HTMLFragment inspector() {
		if (_inspected == null || !_inspected.isRole()) {
			return Fragments.div("aiPolicyInspectorEmpty", Fragments.message(I18NConstants.INSPECTOR_EMPTY));
		}
		return new RoleInspector(access(), _unattended)
			.view(_inspected.roleName(), roleLabelText(_inspected.roleName()));
	}

	/** The roles the current selection holds, in the order of the matrix. */
	List<String> selectedRoles() {
		if (_matrixField == null) {
			return List.of();
		}
		Set<?> selection = _matrixField.getSelectionModel().getSelection();
		List<String> result = new ArrayList<>();
		for (Object row : _tableModel.getAllRows()) {
			if (selection.contains(row) && row instanceof MatrixRow matrixRow && matrixRow.isRole()) {
				result.add(matrixRow.roleName());
			}
		}
		return result;
	}

	/** The names that are columns, in their order. */
	List<String> columnNames() {
		return new ArrayList<>(_columnNames.values());
	}

	/** How a name of this matrix is named in a picker. */
	String nameLabel(String name) {
		if (_dimension == MatrixDimension.CAPABILITIES) {
			return ToolAccessLabels.groupLabel(name);
		}
		return isUnknownName(name) ? name + " (" + resources().getString(I18NConstants.NAME_UNKNOWN) + ")" : name;
	}

	// --------------------------------------------------------------- commands

	/**
	 * Opens the bulk edit over the selected roles.
	 */
	HandlerResult openEditSelection(DisplayContext context) {
		List<String> roles = selectedRoles();
		if (roles.isEmpty()) {
			InfoService.showInfo(I18NConstants.SELECTION_EMPTY);
			return HandlerResult.DEFAULT_RESULT;
		}
		return new EditSelectionDialog(this, roles).open(context);
	}

	/** Applies what the bulk dialog chose. */
	void applyBulk(List<String> roles, List<String> names, CellState state, boolean restrictFirst) {
		session().edit(() -> matrix().apply(roles, names, state, restrictFirst));
		refreshAll();
	}

	/** Goes back one step of the editing session. */
	public HandlerResult undo() {
		session().undo();
		refreshAll();
		return HandlerResult.DEFAULT_RESULT;
	}

	/** Goes forward one undone step of the editing session. */
	public HandlerResult redo() {
		session().redo();
		refreshAll();
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Checks the draft and, if nothing would stop the next start, writes it.
	 *
	 * <p>
	 * The refusal is the whole point: the raw editor accepts a policy that makes the next application
	 * start fail, hours later and for somebody else.
	 * </p>
	 *
	 * @return Always successful; a refused policy leaves the reasons in the view.
	 */
	public HandlerResult applyPolicy() {
		PolicyEditSession session = session();
		List<ResKey> refusals =
			PolicyChecks.refusals(session.draft().config(), ToolAccessPolicyComponent::roleExists);
		if (!refusals.isEmpty()) {
			session.setRefusals(refusals);
			_shownRevision = session.revision();
			refreshNotes();
			return HandlerResult.DEFAULT_RESULT;
		}
		PolicyStore.save(session.draft().config());
		session.reset(PolicyDraft.ofSavedPolicy());
		InfoService.showInfo(I18NConstants.APPLY_SAVED);
		return rebuild();
	}

	/**
	 * Throws the unsaved changes away and shows the saved policy again.
	 */
	public HandlerResult discardChanges() {
		session().reset(PolicyDraft.ofSavedPolicy());
		InfoService.showInfo(I18NConstants.DISCARDED);
		return rebuild();
	}

	private HandlerResult restart() {
		PolicyStore.restartService();
		session().reset(PolicyDraft.ofSavedPolicy());
		InfoService.showInfo(I18NConstants.RESTART_DONE);
		return rebuild();
	}

	static boolean roleExists(String roleName) {
		return BoundedRole.getRoleByName(PersistencyLayer.getKnowledgeBase(), roleName) != null;
	}

	// ------------------------------------------------------- group assignments

	/**
	 * Which tools the policy puts into which capability group, below the capability matrix.
	 *
	 * <p>
	 * A section of its own, because a group assignment says what a tool <em>is</em> rather than what a
	 * role may do. This is the only place the policy states it, so it is edited here and nowhere else.
	 * </p>
	 */
	private void addAssignmentGroup(FormContext context, List<HTMLTemplateFragment> sections) {
		FormGroup group = new FormGroup(GROUP_ASSIGNMENTS, getResPrefix());
		group.setLabel(resources().getString(I18NConstants.POLICY_TOOL_GROUPS_HEADING));
		group.setTooltip(resources().getString(I18NConstants.POLICY_TOOL_GROUPS_HEADING.tooltip()));
		context.addMember(group);

		List<HTMLTemplateFragment> rows = new ArrayList<>();
		rows.add(contentBox(htmlTemplate(note(I18NConstants.POLICY_TOOL_GROUPS_NOTE))));
		List<String> assigned = draft().assignedGroups();
		if (assigned.isEmpty()) {
			rows.add(contentBox(htmlTemplate(note(I18NConstants.POLICY_TOOL_GROUPS_NONE))));
		}
		for (int a = 0; a < assigned.size(); a++) {
			rows.add(assignmentRow(group, assigned.get(a), a));
		}
		button(group, FIELD_ADD, I18NConstants.COMMAND_ADD_ASSIGNMENT, this::addAssignment);
		rows.add(contentBox(member(path(GROUP_ASSIGNMENTS, FIELD_ADD))));

		int count = assigned.size();
		sections.add(section(group, rows, "assignments", true, () -> countSummary(count)));
	}

	/**
	 * Starts an assignment for a capability group that has none yet.
	 *
	 * <p>
	 * With the first tool of the application already in it: an entry naming no tool assigns nothing and
	 * is refused as a policy that says nothing, so an empty row could not be applied.
	 * </p>
	 */
	private HandlerResult addAssignment() {
		String group = draft().unassignedGroup();
		String tool = firstToolName();
		if (group == null || tool == null) {
			InfoService.showInfo(I18NConstants.ASSIGNMENT_NOTHING_TO_ADD);
			return HandlerResult.DEFAULT_RESULT;
		}
		session().edit(() -> draft().setAssignedTools(group, List.of(tool)));
		return rebuild();
	}

	private HTMLTemplateFragment assignmentRow(FormGroup group, String assignedGroup, int index) {
		FormGroup block = new FormGroup("assignment_" + index, getResPrefix());
		group.addMember(block);

		SelectField target = FormFactory.newSelectField(FIELD_GROUPS,
			draft().assignableGroups(assignedGroup), false, false, false, null);
		target.setOptionLabelProvider(GROUP_LABELS);
		labelAndHelp(target, I18NConstants.FIELD_ASSIGNMENT_GROUP);
		target.setAsSingleSelection(assignedGroup);
		target.setTransient(true);
		block.addMember(target);
		_assignmentGroups.add(target);

		SelectField tools = FormFactory.newSelectField(FIELD_TOOLS, allToolNames(), true, false, false, null);
		labelAndHelp(tools, I18NConstants.FIELD_ASSIGNMENT_TOOLS);
		tools.setAsSelection(draft().assignedTools(assignedGroup));
		tools.setTransient(true);
		block.addMember(tools);

		// The capability group of an assignment is its key, so changing it moves the entry rather than
		// editing a value in place. The tools come from the field rather than from the draft, so that
		// an edit that has not been applied yet moves with it.
		target.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			String to = single(newValue, null);
			session().edit(() -> {
				draft().renameAssignment(single(oldValue, assignedGroup), to);
				draft().setAssignedTools(to, strings(tools.getValue()));
			});
			refreshAssignmentOptions();
			afterCellEdit();
		});
		tools.addValueListener((changed, oldValue, newValue) -> {
			if (_syncing) {
				return;
			}
			session().edit(() -> draft().setAssignedTools(single(target.getValue(), assignedGroup),
				strings(newValue)));
			afterCellEdit();
		});

		button(block, FIELD_REMOVE, I18NConstants.COMMAND_REMOVE_ASSIGNMENT, () -> {
			session().edit(() -> draft().removeAssignment(single(target.getValue(), assignedGroup)));
			return rebuild();
		});

		String prefix = path(GROUP_ASSIGNMENTS, block.getName());
		return div(css("aiSecurityRow"), contentBox(fragment(
			fieldBox(path(prefix, FIELD_GROUPS)),
			fieldBox(path(prefix, FIELD_TOOLS)),
			member(path(prefix, FIELD_REMOVE)))));
	}

	/** Offers every assignment the capability groups no other assignment holds, plus its own. */
	private void refreshAssignmentOptions() {
		for (SelectField picker : _assignmentGroups) {
			String own = single(picker.getValue(), null);
			picker.setOptions(draft().assignableGroups(own));
		}
	}

	// ------------------------------------------------- the messages at the top

	/** The banner, the refusals and the warnings, in the order they have to be read. */
	private HTMLFragment messages(FormContext context) {
		CommandField restart =
			button(context, FIELD_RESTART, I18NConstants.COMMAND_RESTART, this::restart);
		return Fragments.div("aiSecurityMessages",
			dynamic("banner", () -> banner(restart)),
			note(_dimension.isRunSection() ? I18NConstants.MATRIX_LEGEND_RUN
				: I18NConstants.MATRIX_LEGEND_CAPABILITIES),
			mayWrite() ? Fragments.empty() : note(I18NConstants.POLICY_READ_ONLY),
			dynamic("refusals", this::refusals),
			dynamic("warnings", this::warnings),
			dynamic("dirty", this::dirty));
	}

	/**
	 * The banner saying that what is saved is not what the application applies.
	 *
	 * <p>
	 * Together with the restart action next to it, this is the honest answer to "is what I am looking
	 * at live". The catalog and this tab report the saved policy; the effective-permissions view reports
	 * the running one, and until the service is restarted the two disagree.
	 * </p>
	 */
	private HTMLFragment banner(CommandField restart) {
		if (!PolicyDraft.differsFromRunningService(AiToolAccessService.savedPolicy())) {
			return Fragments.empty();
		}
		return Fragments.div("aiSecurityBanner",
			Fragments.span(Fragments.message(I18NConstants.BANNER_SAVED_NOT_EFFECTIVE)),
			new ButtonControl(restart, DefaultButtonRenderer.INSTANCE));
	}

	/** Why the last apply was refused, or nothing while none was. */
	private HTMLFragment refusals() {
		List<ResKey> refusals = session().refusals();
		if (refusals.isEmpty()) {
			return Fragments.empty();
		}
		return Fragments.div("aiSecurityRefusals",
			Fragments.p(Fragments.message(I18NConstants.REFUSALS_HEADING)),
			messageList(refusals));
	}

	/**
	 * What the draft does that its author probably did not mean.
	 *
	 * <p>
	 * Coalesced, and computed from the whole policy: every one of these is about two settings at once,
	 * so none of them can be shown next to a single control.
	 * </p>
	 */
	private HTMLFragment warnings() {
		List<ResKey> messages =
			PolicyChecks.warnings(draft().config(), rolesWithModelRights(),
				AiToolAccessService.partsWithoutSecurity());
		if (messages.isEmpty()) {
			return Fragments.empty();
		}
		return Fragments.div("aiSecurityWarning",
			Fragments.p(Fragments.message(I18NConstants.WARNINGS_HEADING)),
			messageList(messages));
	}

	private HTMLFragment dirty() {
		return note(draft().isModified() ? I18NConstants.DIRTY_UNSAVED : I18NConstants.DIRTY_NONE);
	}

	private static HTMLFragment messageList(List<ResKey> messages) {
		List<HTMLFragment> items = new ArrayList<>();
		for (ResKey message : messages) {
			items.add(Fragments.li(Fragments.message(message)));
		}
		return Fragments.ul(items.toArray(new HTMLFragment[items.size()]));
	}

	/** The roles named in a Read, Write or Create grant of the model access rights. */
	private static Set<String> rolesWithModelRights() {
		if (!SecurityConfigurationService.Module.INSTANCE.isActive()) {
			return Set.of();
		}
		return AiToolAccessService.rolesWithModelRights(
			SecurityConfigurationService.Module.INSTANCE.getImplementationInstance().getConfig());
	}

	// --------------------------------------------------------- small helpers

	/**
	 * One collapsible section, the framework's own group cell.
	 *
	 * @param group
	 *        The form group whose label and tooltip name and explain the section.
	 * @param content
	 *        What the section contains.
	 * @param key
	 *        Distinguishes the section in the personal configuration that remembers whether it is open.
	 * @param collapsed
	 *        Whether the section starts collapsed for a reader who has not touched it.
	 * @param summary
	 *        What the title says about the content, so that a collapsed section still answers something.
	 */
	private FieldSetBoxTemplate section(FormMember group, List<HTMLTemplateFragment> content,
			String key, boolean collapsed, Supplier<String> summary) {
		return fieldsetBoxDirect(
			htmlTemplate(Fragments.concat(
				new LabelControl(group),
				dynamic("summary." + key, () -> summaryFragment(summary)))),
			fragment(content),
			ConfigKey.part(this, key))
				.setInitiallyCollapsed(collapsed);
	}

	private static HTMLFragment summaryFragment(Supplier<String> summary) {
		String text = summary.get();
		return text.isEmpty() ? Fragments.empty()
			: Fragments.span("aiSecuritySummary", Fragments.text(text));
	}

	/** How many things a collapsed section holds. */
	private static String countSummary(int count) {
		return resources().getString(I18NConstants.SECTION_COUNT__COUNT.fill(Integer.valueOf(count)));
	}

	/** A block of the view that is recomputed and repainted when a value below it changes. */
	private DynamicFragment dynamic(String key, Supplier<HTMLFragment> content) {
		DynamicFragment fragment = new DynamicFragment(content);
		_dynamic.put(key, fragment);
		return fragment;
	}

	private static HTMLFragment note(ResKey message) {
		return Fragments.p("aiSecurityNote", Fragments.message(message));
	}

	private CommandField button(FormContainer parent, String name, ResKey label,
			Supplier<HandlerResult> action) {
		CommandField field = FormFactory.newCommandField(name, (Command) context -> action.get());
		field.setControlProvider(BUTTONS);
		field.setLabel(resources().getString(label));
		String tooltip = resources().getString(label.tooltipOptional());
		if (tooltip != null) {
			field.setTooltip(tooltip);
		}
		parent.addMember(field);
		return field;
	}

	private static void labelAndHelp(FormMember field, ResKey label) {
		field.setLabel(resources().getString(label));
		String tooltip = resources().getString(label.tooltipOptional());
		if (tooltip != null) {
			field.setTooltip(tooltip);
		}
	}

	private static Set<String> existingRoleNames() {
		Set<String> result = new HashSet<>();
		if (PersistencyLayer.Module.INSTANCE.isActive()) {
			for (BoundedRole role : BoundedRole.getAll()) {
				if (role.getName() != null && !role.getName().isBlank()) {
					result.add(role.getName());
				}
			}
		}
		return result;
	}

	/** Whether the given role is one the application has; checked against the roles read at build. */
	private boolean roleKnown(String roleName) {
		return _existingRoles.contains(roleName);
	}

	/** How a role is shown: the label an administrator gave it, or its name. */
	private static String roleLabelText(String roleName) {
		if (!PersistencyLayer.Module.INSTANCE.isActive()) {
			return roleName;
		}
		BoundedRole role = BoundedRole.getRoleByName(PersistencyLayer.getKnowledgeBase(), roleName);
		if (role == null) {
			return roleName;
		}
		String label = MetaLabelProvider.INSTANCE.getLabel(role);
		return label == null || label.isBlank() ? roleName : label;
	}

	/** The names of one dimension the application has. */
	private static Set<String> knownNames(MatrixDimension dimension) {
		if (!dimension.isRunSection()) {
			return Set.of();
		}
		Set<String> known = AiToolAccessService.knownNames(dimension.runDimension());
		return known == null ? Set.of() : known;
	}

	private static List<String> knownNameList(Dimension dimension) {
		Set<String> known = AiToolAccessService.knownNames(dimension);
		return known == null ? List.of() : new ArrayList<>(known);
	}

	private static String firstToolName() {
		List<String> tools = allToolNames();
		return tools.isEmpty() ? null : tools.get(0);
	}

	static List<String> allToolNames() {
		return new AllToolNames().apply();
	}

	@SuppressWarnings("unchecked")
	static <T> T single(Object value, T fallback) {
		if (value instanceof List<?> list && !list.isEmpty()) {
			return (T) list.get(0);
		}
		return fallback;
	}

	@SuppressWarnings("unchecked")
	static List<String> strings(Object value) {
		return value instanceof List<?> list ? (List<String>) list : List.of();
	}

	private static Resources resources() {
		return Resources.getInstance();
	}

	/** Renders a command as a button carrying its label, not as a link nobody sees. */
	private static final ControlProvider BUTTONS =
		(model, style) -> new ButtonControl((CommandField) model, DefaultButtonRenderer.INSTANCE);

	/** How a capability group is named: display name and technical name, as everywhere else. */
	private static final LabelProvider GROUP_LABELS = model -> model instanceof String group
		? ToolAccessLabels.groupLabel(group) : String.valueOf(model);

	/** Hands the row itself to the columns that render it. */
	private static final Accessor<Object> ROW_ACCESSOR = new ReadOnlyAccessor<>() {
		@Override
		public Object getValue(Object object, String property) {
			return object;
		}
	};

	/** Renders a cell of the matrix as the control that edits it. */
	private final ControlProvider CELLS = (model, style) -> new PolicyCellControl(this, (PolicyCell) model);

	/** Hands a column the cell of its name in the row. */
	private static final class CellAccessor extends ReadOnlyAccessor<Object> {

		private final String _name;

		CellAccessor(String name) {
			_name = name;
		}

		@Override
		public Object getValue(Object object, String property) {
			return new PolicyCell((MatrixRow) object, _name);
		}
	}

}
