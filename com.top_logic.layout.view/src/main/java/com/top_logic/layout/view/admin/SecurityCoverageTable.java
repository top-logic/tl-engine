/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.CoverageStatus;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.SecurityDefinitionEditor;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.PathNavigation;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.controlprovider.MetaResourceControlProvider;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.DelegatingColumn;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/**
 * Table of the model based access definition, one row per analyzed type with the roles that may
 * read it, the rules that deliver a role on it and the gaps found in its definition. The rows are
 * grouped by the module of the type when the table is opened, so the module column itself and the
 * findings column, whose text a detail display shows, start hidden; the user shows them from the
 * column selection and regroups or ungroups the table from a column header.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * The table shows a fresh analysis when opened and rebuilds from the configured
 * {@link Config#getInput() input channel} after a command. The selected row is written to the
 * {@link Config#getSelection() selection channel} (so a command can act on it); the type of it goes
 * to the {@link Config#getSelectedType() type channel} (so a command names it), and the parts of it
 * a detail display shows go to the {@link Config#getSelectedFindings() findings}, the
 * {@link Config#getSelectedRule() proposed rule} and the {@link Config#getSelectedRules() rules in
 * effect}, all cleared when the selection is empty. The channels are pushed again once the rows
 * were replaced, so the detail of the row that stays selected describes the analysis the table now
 * shows.
 * </p>
 *
 * @implNote The rows come from {@link SecurityCoverageCheck#analyze()}; a row is keyed by the
 *           qualified name of its type, since every analysis yields fresh {@link TypeCoverage}
 *           instances and the selection is to survive a refresh. The type, module and role columns
 *           are built by the {@link ColumnProviderService} from the model types of their values, so
 *           they show, sort and filter those objects exactly as a table over model attributes does.
 */
public class SecurityCoverageTable implements UIElement {

	/** Id of the column showing the analyzed type. */
	public static final String COLUMN_TYPE = "type";

	/** Id of the column showing the module of the analyzed type, the initial grouping column. */
	public static final String COLUMN_MODULE = "module";

	/** Id of the column showing the {@link CoverageStatus} of the analyzed type. */
	public static final String COLUMN_STATUS = "status";

	/** Id of the column showing the roles that may read the analyzed type. */
	public static final String COLUMN_READ_ROLES = "readRoles";

	/** Id of the column showing the role rules applying to the analyzed type. */
	public static final String COLUMN_ROLE_RULES = "roleRules";

	/** Id of the column showing the security parent rules applying to the analyzed type. */
	public static final String COLUMN_SECURITY_PARENTS = "securityParents";

	/** Id of the column showing the findings reported for the analyzed type. */
	public static final String COLUMN_FINDINGS = "findings";

	/** Key of the kind of a rule entry, one of {@link #KIND_SECURITY_PARENT} / {@link #KIND_ROLE_RULE}. */
	public static final String RULE_KIND = "kind";

	/** Key of the localized name of the {@link #RULE_KIND kind} of a rule entry. */
	public static final String RULE_KIND_LABEL = "kindLabel";

	/** Key of the id a rule entry is stored and edited under. */
	public static final String RULE_ID = "id";

	/** Key of the text describing what a rule entry does. */
	public static final String RULE_DESCRIPTION = "description";

	/** Key of the flag telling whether the application's configuration defines a rule entry. */
	public static final String RULE_STORED = "stored";

	/**
	 * Renders a {@link CoverageStatus} with its icon and label through the default
	 * {@link MetaResourceControlProvider} configuration.
	 */
	private static final MetaResourceControlProvider STATUS_DISPLAY =
		TypedConfigUtil.createInstance(MetaResourceControlProvider.Config.class);

	/** {@link #RULE_KIND} of an entry standing for a security parent rule. */
	public static final String KIND_SECURITY_PARENT = "securityParent";

	/** {@link #RULE_KIND} of an entry standing for a role rule. */
	public static final String KIND_ROLE_RULE = "roleRule";

	/** Qualified name of the model type of the values in the {@link #COLUMN_TYPE} column. */
	private static final String TL_CLASS_TYPE = "tl.model:TLClass";

	/** Qualified name of the model type of the values in the {@link #COLUMN_MODULE} column. */
	private static final String TL_MODULE_TYPE = "tl.model:TLModule";

	/** Separator between the rules rendered into a cell. */
	private static final String RULE_SEPARATOR = "; ";

	/** Separator between the entries of a list rendered into a cell. */
	private static final String VALUE_SEPARATOR = ", ";

	/** Separator between the steps of a rendered navigation path. */
	private static final String STEP_SEPARATOR = " / ";

	/** Marker in front of a path step that navigates its reference backwards. */
	private static final String INVERSE_MARKER = "^";

	/** Separator between the parts a rule is described by. */
	private static final String DESCRIPTION_SEPARATOR = " \u2192 ";

	/**
	 * Configuration for {@link SecurityCoverageTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getSelectedType()}. */
		String SELECTED_TYPE = "selected-type";

		/** Configuration name for {@link #getSelectedFindings()}. */
		String SELECTED_FINDINGS = "selected-findings";

		/** Configuration name for {@link #getSelectedRule()}. */
		String SELECTED_RULE = "selected-rule";

		/** Configuration name for {@link #getSelectedRules()}. */
		String SELECTED_RULES = "selected-rules";

		@Override
		@ClassDefault(SecurityCoverageTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the analyzed types ({@code List<TypeCoverage>}) to display; when unset or
		 * empty, the table analyses the access definition itself when it is opened.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Channel the selected type coverage is written to (or {@code null} when the selection is
		 * cleared).
		 */
		@Name(SELECTION)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Channel the selected type itself is written to, so that a command names it in the text it
		 * shows to the user.
		 */
		@Name(SELECTED_TYPE)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelectedType();

		/**
		 * Channel the findings of the selected type are written to, as the text a detail display
		 * shows.
		 */
		@Name(SELECTED_FINDINGS)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelectedFindings();

		/**
		 * Channel the security parent rule proposed for the selected type is written to, as the
		 * HTML a detail display shows, or {@code null} when no rule is proposed for it.
		 */
		@Name(SELECTED_RULE)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelectedRule();

		/**
		 * Channel the rules in effect for the selected type are written to, one entry per rule.
		 *
		 * <p>
		 * An entry is a dictionary keyed by {@link SecurityCoverageTable#RULE_KIND},
		 * {@link SecurityCoverageTable#RULE_KIND_LABEL}, {@link SecurityCoverageTable#RULE_ID},
		 * {@link SecurityCoverageTable#RULE_DESCRIPTION} and
		 * {@link SecurityCoverageTable#RULE_STORED}, so a display shows the entries in a table of
		 * computed columns and a command works on the entry the user picks.
		 * </p>
		 */
		@Name(SELECTED_RULES)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelectedRules();
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _selectionRef;

	private final ChannelRef _selectedTypeRef;

	private final ChannelRef _selectedFindingsRef;

	private final ChannelRef _selectedRuleRef;

	private final ChannelRef _selectedRulesRef;

	/**
	 * Creates a new {@link SecurityCoverageTable} from configuration.
	 */
	@CalledByReflection
	public SecurityCoverageTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_selectionRef = config.getSelection();
		_selectedTypeRef = config.getSelectedType();
		_selectedFindingsRef = config.getSelectedFindings();
		_selectedRuleRef = config.getSelectedRule();
		_selectedRulesRef = config.getSelectedRules();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<Object, ?>> columns = new ArrayList<>();
		columns.add(objectColumn(COLUMN_TYPE, I18NConstants.COVERAGE_COLUMN_TYPE, TL_CLASS_TYPE, false,
			row -> coverage(row).type(), 240));
		columns.add(objectColumn(COLUMN_MODULE, I18NConstants.COVERAGE_COLUMN_MODULE, TL_MODULE_TYPE, false,
			row -> coverage(row).type().getModule(), 200));
		columns.add(statusColumn());
		columns.add(objectColumn(COLUMN_READ_ROLES, I18NConstants.COVERAGE_COLUMN_READ_ROLES, BoundedRole.ROLE_TYPE,
			true, row -> readRoles(coverage(row)), 230));
		columns.add(textColumn(COLUMN_ROLE_RULES, I18NConstants.COVERAGE_COLUMN_ROLE_RULES,
			SecurityCoverageTable::roleRules, 200));
		columns.add(textColumn(COLUMN_SECURITY_PARENTS, I18NConstants.COVERAGE_COLUMN_SECURITY_PARENTS,
			SecurityCoverageTable::securityParents, 260));
		columns.add(textColumn(COLUMN_FINDINGS, I18NConstants.COVERAGE_COLUMN_FINDINGS,
			SecurityCoverageTable::findings, 460));

		ViewChannel dataChannel = _inputRef != null ? context.resolveChannel(_inputRef) : null;
		List<Object> initialRows = rows(dataChannel == null ? null : dataChannel.get());
		Map<Object, TypeCoverage> rowByKey = new HashMap<>(index(initialRows));
		ListRowSource<Object> source = new ListRowSource<>(initialRows, columns, SecurityCoverageTable::rowKey);
		Set<String> hiddenByDefault = Set.of(COLUMN_MODULE, COLUMN_FINDINGS);
		TableViewState initialState = DefaultTableView.initialState(columns, SortSpec.NONE, hiddenByDefault);
		initialState.setGrouping(new GroupSpec(List.of(COLUMN_MODULE)));
		DefaultTableView<Object> view = new DefaultTableView<>(columns, source, initialState);
		TableViewControl<Object> control = new TableViewControl<>(context, view, false);

		Detail detail = new Detail(
			_selectionRef == null ? null : context.resolveChannel(_selectionRef),
			_selectedTypeRef == null ? null : context.resolveChannel(_selectedTypeRef),
			_selectedFindingsRef == null ? null : context.resolveChannel(_selectedFindingsRef),
			_selectedRuleRef == null ? null : context.resolveChannel(_selectedRuleRef),
			_selectedRulesRef == null ? null : context.resolveChannel(_selectedRulesRef));
		if (detail.isBound()) {
			control.addSelectionListener(keys -> {
				Object key = keys.size() == 1 ? keys.iterator().next() : null;
				detail.setKey(key);
				detail.show(key == null ? null : rowByKey.get(key));
			});
		}

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> {
				List<Object> newRows = rows(newValue);
				rowByKey.clear();
				rowByKey.putAll(index(newRows));
				source.setElements(newRows);
				control.refreshData();
				if (detail.isBound()) {
					// The rows are fresh instances, so the display of the row that stays selected
					// would otherwise keep describing the analysis that was replaced.
					detail.show(rowByKey.get(detail.getKey()));
				}
			};
			dataChannel.addListener(listener);
			control.addCleanupAction(() -> dataChannel.removeListener(listener));
		}
		return control;
	}

	/**
	 * The channels describing the selected row, and the key of the row they describe.
	 *
	 * <p>
	 * The key is remembered because the rows are replaced as a whole whenever the analysis is
	 * repeated: the row that stays selected is a different {@link TypeCoverage} instance afterwards,
	 * and the channels are pushed again for it without the user having to select it anew.
	 * </p>
	 */
	private static final class Detail {

		private final ViewChannel _selection;

		private final ViewChannel _type;

		private final ViewChannel _findings;

		private final ViewChannel _proposedRule;

		private final ViewChannel _rules;

		private Object _key;

		/**
		 * Creates a {@link Detail} over the channels the table is configured with, each of them
		 * <code>null</code> where the configuration names none.
		 */
		Detail(ViewChannel selection, ViewChannel type, ViewChannel findings, ViewChannel proposedRule,
				ViewChannel rules) {
			_selection = selection;
			_type = type;
			_findings = findings;
			_proposedRule = proposedRule;
			_rules = rules;
		}

		/**
		 * Whether any channel is bound at all.
		 */
		boolean isBound() {
			return _selection != null || _type != null || _findings != null || _proposedRule != null
				|| _rules != null;
		}

		/**
		 * The key of the row being described, <code>null</code> while nothing is selected.
		 */
		Object getKey() {
			return _key;
		}

		/**
		 * @see #getKey()
		 */
		void setKey(Object key) {
			_key = key;
		}

		/**
		 * Writes what the given row is described by to the bound channels, clearing them all for
		 * <code>null</code>.
		 */
		void show(TypeCoverage row) {
			if (_selection != null) {
				_selection.set(row);
			}
			if (_type != null) {
				_type.set(row == null ? null : row.type());
			}
			if (_findings != null) {
				_findings.set(row == null ? null : findings(row));
			}
			if (_proposedRule != null) {
				String rule = row == null ? null : SecurityCoverageAction.ruleHtml(row);
				_proposedRule.set(rule == null || rule.isEmpty() ? null : rule);
			}
			if (_rules != null) {
				_rules.set(row == null ? List.of() : ruleEntries(row));
			}
		}
	}

	/**
	 * The rules in effect for the given type, the security parent rules first, each as the
	 * dictionary a display shows and a command works on.
	 *
	 * @see Config#getSelectedRules()
	 */
	private static List<Map<String, Object>> ruleEntries(TypeCoverage coverage) {
		SecurityDefinitionEditor editor = new SecurityDefinitionEditor();
		Set<String> storedParents = storedIds(editor::storedSecurityParentRules);
		Set<String> storedRoleRules = storedIds(editor::storedRoleRules);
		Resources resources = Resources.getInstance();
		String parentKind = resources.getString(I18NConstants.COVERAGE_RULE_KIND_SECURITY_PARENT);
		String roleKind = resources.getString(I18NConstants.COVERAGE_RULE_KIND_ROLE_RULE);

		List<Map<String, Object>> result = new ArrayList<>();
		for (NavigationRule rule : coverage.securityParentRules()) {
			String id = rule.getId();
			result.add(ruleEntry(KIND_SECURITY_PARENT, parentKind, id, path(rule.getPath()),
				storedParents.contains(id)));
		}
		for (Map.Entry<String, List<RoleProvider>> group : roleRulesByConfigId(coverage).entrySet()) {
			String id = group.getKey();
			result.add(ruleEntry(KIND_ROLE_RULE, roleKind, id, roleRuleDescription(group.getValue()),
				storedRoleRules.contains(id)));
		}
		return result;
	}

	/**
	 * One entry of {@link Config#getSelectedRules()}.
	 */
	private static Map<String, Object> ruleEntry(String kind, String kindLabel, String id, String description,
			boolean stored) {
		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put(RULE_KIND, kind);
		entry.put(RULE_KIND_LABEL, kindLabel);
		entry.put(RULE_ID, id);
		entry.put(RULE_DESCRIPTION, description);
		entry.put(RULE_STORED, Boolean.valueOf(stored));
		return entry;
	}

	/**
	 * The ids of the rules the given query of the stored configuration answers, empty when that
	 * configuration cannot be read.
	 *
	 * <p>
	 * A configuration that cannot be read means that no rule can be shown as stored, hence that
	 * none is offered for deletion - which is the safe answer, the file being the only place a rule
	 * can be deleted from.
	 * </p>
	 */
	private static Set<String> storedIds(StoredRulesQuery query) {
		try {
			return query.run().stream().map(NavigationRuleConfig::getId).collect(Collectors.toSet());
		} catch (ConfigurationException ex) {
			Logger.error("Cannot read the stored access definition.", ex, SecurityCoverageTable.class);
			return Set.of();
		}
	}

	/**
	 * The role rules applying to the given type, grouped by the id of the configuration they were
	 * created from.
	 *
	 * <p>
	 * One configured rule grants each of the roles it names, so it appears as one
	 * {@link RoleProvider} per role - the rule the user edits and deletes is the configured one.
	 * </p>
	 */
	private static Map<String, List<RoleProvider>> roleRulesByConfigId(TypeCoverage coverage) {
		Map<String, List<RoleProvider>> result = new LinkedHashMap<>();
		for (RoleProvider rule : coverage.roleRules()) {
			result.computeIfAbsent(rule.getConfigId(), id -> new ArrayList<>()).add(rule);
		}
		return result;
	}

	/**
	 * The roles the given rules grant and the path leading to the objects granting them.
	 */
	private static String roleRuleDescription(List<RoleProvider> rules) {
		String roles = rules.stream()
			.map(RoleProvider::getRole)
			.map(BoundRole::getName)
			.distinct()
			.collect(Collectors.joining(VALUE_SEPARATOR));
		String path = path(rules.get(0).getPath());
		return path.isEmpty() ? roles : roles + DESCRIPTION_SEPARATOR + path;
	}

	/**
	 * A query for the rules the stored configuration defines.
	 */
	private interface StoredRulesQuery {

		/**
		 * Answers the query.
		 */
		List<? extends NavigationRuleConfig> run() throws ConfigurationException;
	}

	/**
	 * The analyzed types held by the channel value, or a fresh analysis when the channel is unset.
	 */
	private static List<Object> rows(Object value) {
		List<?> rows = value instanceof List<?> list ? list : SecurityCoverageCheck.getInstance().analyze();
		return new ArrayList<>(rows);
	}

	/**
	 * The given rows by their row key.
	 */
	private static Map<Object, TypeCoverage> index(List<Object> rows) {
		Map<Object, TypeCoverage> result = new LinkedHashMap<>();
		for (Object row : rows) {
			result.put(rowKey(row), coverage(row));
		}
		return result;
	}

	/**
	 * The row as the analyzed type it holds.
	 */
	private static TypeCoverage coverage(Object row) {
		return (TypeCoverage) row;
	}

	/**
	 * The qualified name of the analyzed type, which is the row key.
	 */
	private static String rowKey(Object row) {
		return TLModelUtil.qualifiedName(coverage(row).type());
	}

	/**
	 * The roles that may read the type, ordered by name.
	 */
	private static List<BoundedRole> readRoles(TypeCoverage coverage) {
		return coverage.readRoles().stream()
			.sorted(Comparator.comparing(BoundedRole::getName))
			.toList();
	}

	/**
	 * The ids of the rules delivering a role on the type.
	 *
	 * <p>
	 * The id of the configured rule, not of the one provider per role it is applied as, so that the
	 * cell names the rules the way the rule list and the editing commands do.
	 * </p>
	 */
	private static String roleRules(Object row) {
		return roleRulesByConfigId(coverage(row)).keySet().stream()
			.collect(Collectors.joining(VALUE_SEPARATOR));
	}

	/**
	 * The paths of the security parent rules applying to the type.
	 */
	private static String securityParents(Object row) {
		return coverage(row).securityParentRules().stream()
			.map(rule -> path(rule.getPath()))
			.collect(Collectors.joining(RULE_SEPARATOR));
	}

	/**
	 * The given navigation path, one entry per step.
	 */
	private static String path(List<PathElement> steps) {
		return steps.stream()
			.map(SecurityCoverageTable::step)
			.collect(Collectors.joining(STEP_SEPARATOR));
	}

	/**
	 * The given navigation step: the qualified name of the reference it navigates, marked with
	 * {@link #INVERSE_MARKER} when it is navigated backwards.
	 */
	private static String step(PathElement element) {
		if (element instanceof PathNavigation navigation) {
			String reference = TLModelUtil.qualifiedName(navigation.getReference());
			return navigation.isInverse() ? INVERSE_MARKER + reference : reference;
		}
		StringBuilder buffer = new StringBuilder();
		try {
			element.appendForTooltip(buffer);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return buffer.toString();
	}

	/**
	 * The messages of the findings reported for the type, in the language of the user.
	 */
	private static String findings(Object row) {
		Resources resources = Resources.getInstance();
		return coverage(row).findings().stream()
			.map(CoverageFinding::getMessage)
			.map(resources::getString)
			.collect(Collectors.joining(RULE_SEPARATOR));
	}

	/**
	 * A column over objects of the given model type, built the way a table over model attributes
	 * builds its columns: the objects are shown with their icon and label, sorted and filtered by
	 * that label.
	 *
	 * @param typeName
	 *        The qualified name of the model type of the column's values.
	 * @param multiple
	 *        Whether a cell holds a collection of values.
	 */
	private static Column<Object, ?> objectColumn(String id, ResKey label, String typeName, boolean multiple,
			Function<Object, Object> value, int width) {
		TLType type = TLModelUtil.findType(typeName);
		Column<Object, ?> column =
			ColumnProviderService.getInstance().createColumn(id, label, ColumnType.of(type, multiple), value);
		return DelegatingColumn.withDefaultWidth(column, width);
	}

	/**
	 * The column showing the {@link CoverageStatus} of a type with its icon and label, sorted by
	 * the order of the status constants and filtered by the label.
	 */
	private static Column<Object, CoverageStatus> statusColumn() {
		Resources resources = Resources.getInstance();
		Function<CoverageStatus, String> text = status -> resources.getString(ResKey.forEnum(status));
		return DefaultColumn.<Object, CoverageStatus> builder(COLUMN_STATUS, row -> coverage(row).status())
			.label(I18NConstants.COVERAGE_COLUMN_STATUS)
			.renderer(status -> new CellContent.Raw(
				(CellControlFactory) context -> STATUS_DISPLAY.createControl(context, status)))
			.sort(() -> Comparator.<CoverageStatus> naturalOrder())
			.filter(new TextColumnFilter<>(text))
			.width(150)
			.build();
	}

	/**
	 * A sortable, text-filterable column reading one {@link String} property of an analyzed type.
	 */
	private static Column<Object, String> textColumn(String id, ResKey label, Function<Object, String> value,
			int width) {
		return DefaultColumn.<Object, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(t -> t))
			.width(width)
			.build();
	}

}
