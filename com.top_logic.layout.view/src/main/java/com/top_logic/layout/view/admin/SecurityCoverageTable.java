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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.CoverageStatus;
import com.top_logic.element.boundsec.manager.coverage.OperationUse;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageAnalysis;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.SecurityDefinitionEditor;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.PathNavigation;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.SingletonRuleConfig;
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
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.rules.PathByExpression;
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.ContainerRelation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.DelegatingColumn;
import com.top_logic.table.impl.TreeRowSource;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/**
 * Table of the model based access definition, one row per analyzed type with the roles that may
 * read it, the rules that deliver a role on it, the access parent it delegates to and the gaps
 * found in its definition. The table is a tree: the modules of the analyzed types are its nodes,
 * opened when the table is opened, the types are their children. The findings column, whose text a
 * detail display shows, starts hidden; the user shows it from the column selection.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * The table shows a fresh analysis when opened and rebuilds from the configured
 * {@link Config#getInput() input channel} after a command. The selected row is written to the
 * {@link Config#getSelection() selection channel} (so a command can act on it); the type of it goes
 * to the {@link Config#getSelectedType() type channel} (so a command names it), its marks to the
 * {@link Config#getSelectedInternal() internal} and {@link Config#getSelectedWithoutSecurity()
 * without-security channels} (so a command offers the mark the type does not carry yet), and the
 * parts of it a detail display shows go to the {@link Config#getSelectedFindings() findings} and
 * the {@link Config#getSelectedRules() rules in effect}, all cleared when the selection is empty. The channels are pushed again once the rows
 * were replaced, so the detail of the row that stays selected describes the analysis the table now
 * shows.
 * </p>
 *
 * <p>
 * A module can be selected as well: it is written to the {@link Config#getSelectedModule() module
 * channel} for the commands acting on a whole module, while the channels describing a type are
 * empty.
 * </p>
 *
 * @implNote The rows come from {@link #analysisRows()}; a row is keyed by its
 *           module or type, since every analysis yields fresh {@link TypeCoverage} instances and
 *           the selection and the opened modules are to survive a refresh. The type and role
 *           columns are built by the {@link ColumnProviderService} from the model types of their
 *           values, so they show, sort and filter those objects exactly as a table over model
 *           attributes does.
 */
public class SecurityCoverageTable implements UIElement {

	/**
	 * The node grouping the findings that belong to no analyzed type, see
	 * {@link SecurityCoverageAnalysis#getUnattachedFindings()}.
	 */
	public static final Object UNATTACHED = new NamedConstant("unattachedFindings");

	/** Id of the column showing the analyzed type. */
	public static final String COLUMN_TYPE = "type";

	/** Id of the column showing the {@link CoverageStatus} of the analyzed type. */
	public static final String COLUMN_STATUS = "status";

	/** Id of the column showing the roles that may read the analyzed type. */
	public static final String COLUMN_READ_ROLES = "readRoles";

	/** Id of the column showing the role rules applying to the analyzed type. */
	public static final String COLUMN_ROLE_RULES = "roleRules";

	/** Id of the column showing the role parent rules applying to the analyzed type. */
	public static final String COLUMN_ROLE_PARENTS = "roleParents";

	/** Id of the column showing the access parent the analyzed type delegates to. */
	public static final String COLUMN_ACCESS_PARENT = "accessParent";

	/** Id of the column showing the findings reported for the analyzed type. */
	public static final String COLUMN_FINDINGS = "findings";

	/** Key of the kind of a rule entry, one of {@link #KIND_ROLE_PARENT} / {@link #KIND_ROLE_RULE}. */
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
	 * Key of the change of a rule entry that is stored but not yet applied, one of
	 * {@link #STATE_NEW}, {@link #STATE_CHANGED}, {@link #STATE_DELETED}, or the empty string for a
	 * rule in effect as stored.
	 */
	public static final String RULE_STATE = "state";

	/** Key of the localized name of the {@link #RULE_STATE state} of a rule entry. */
	public static final String RULE_STATE_LABEL = "stateLabel";

	/** {@link #RULE_STATE} of a stored rule that is not yet in effect. */
	public static final String STATE_NEW = "new";

	/** {@link #RULE_STATE} of a rule in effect whose stored form differs from the one in effect. */
	public static final String STATE_CHANGED = "changed";

	/** {@link #RULE_STATE} of a rule in effect that the stored configuration deletes. */
	public static final String STATE_DELETED = "deleted";

	/** {@link #RULE_STATE} of a rule in effect as stored. */
	public static final String STATE_APPLIED = "";

	/**
	 * Renders a {@link CoverageStatus} with its icon and label through the default
	 * {@link MetaResourceControlProvider} configuration.
	 */
	private static final MetaResourceControlProvider STATUS_DISPLAY =
		TypedConfigUtil.createInstance(MetaResourceControlProvider.Config.class);

	/** {@link #RULE_KIND} of an entry standing for a role parent rule. */
	public static final String KIND_ROLE_PARENT = "roleParent";

	/** {@link #RULE_KIND} of an entry standing for a role rule. */
	public static final String KIND_ROLE_RULE = "roleRule";

	/**
	 * Qualified name of the model type of the values in the {@link #COLUMN_TYPE} column: a module or
	 * a type.
	 */
	private static final String TL_MODEL_PART_TYPE = "tl.model:TLModelPart";

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

		/** Configuration name for {@link #getSelectedInternal()}. */
		String SELECTED_INTERNAL = "selected-internal";

		/** Configuration name for {@link #getSelectedWithoutSecurity()}. */
		String SELECTED_WITHOUT_SECURITY = "selected-without-security";

		/** Configuration name for {@link #getSelectedFindings()}. */
		String SELECTED_FINDINGS = "selected-findings";

		/** Configuration name for {@link #getSelectedRules()}. */
		String SELECTED_RULES = "selected-rules";

		/** Configuration name for {@link #getSelectedModule()}. */
		String SELECTED_MODULE = "selected-module";

		/** Configuration name for {@link #getPending()}. */
		String PENDING = "pending";

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
		 * Channel receiving whether the selected type is internal to the application code, as the
		 * analysis reports it; <code>null</code> while nothing is selected.
		 *
		 * <p>
		 * The value is <code>true</code> for a type carrying the mark of its own, which can be
		 * dropped for the type, <code>false</code> for a type not internal at all, and the
		 * generalization or module declaring the mark for a type inheriting it.
		 * </p>
		 */
		@Name(SELECTED_INTERNAL)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelectedInternal();

		/**
		 * Channel receiving whether the selected type is excluded from access control, as the
		 * analysis reports it; <code>null</code> while nothing is selected.
		 *
		 * <p>
		 * The value is <code>true</code> for a type carrying the mark of its own, which can be
		 * dropped for the type, <code>false</code> for a type access controlled, and the
		 * generalization or module declaring the mark for a type inheriting it.
		 * </p>
		 */
		@Name(SELECTED_WITHOUT_SECURITY)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelectedWithoutSecurity();

		/**
		 * Channel the findings of the selected type are written to, as the HTML a detail display
		 * shows: a list with one entry per problem, the ways to solve it nested underneath; for an
		 * exempt type, a paragraph naming the mark exempting it.
		 */
		@Name(SELECTED_FINDINGS)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelectedFindings();

		/**
		 * Channel the rules in effect for the selected type are written to, one entry per rule.
		 *
		 * <p>
		 * An entry is a dictionary keyed by {@link SecurityCoverageTable#RULE_KIND},
		 * {@link SecurityCoverageTable#RULE_KIND_LABEL}, {@link SecurityCoverageTable#RULE_ID},
		 * {@link SecurityCoverageTable#RULE_DESCRIPTION}, {@link SecurityCoverageTable#RULE_STORED},
		 * {@link SecurityCoverageTable#RULE_STATE} and {@link SecurityCoverageTable#RULE_STATE_LABEL},
		 * so a display shows the entries in a table of computed columns and a command works on the
		 * entry the user picks.
		 * </p>
		 *
		 * <p>
		 * Besides the rules in effect, the entries show the stored changes that are not yet applied:
		 * a stored rule that is not yet in effect is listed as new, a rule in effect is marked as
		 * changed or deleted by the stored configuration.
		 * </p>
		 */
		@Name(SELECTED_RULES)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelectedRules();

		/**
		 * Channel the selected module is written to, when the user selects a module rather than a
		 * type; <code>null</code> otherwise. The channels describing a selected type are empty
		 * while a module is selected.
		 */
		@Name(SELECTED_MODULE)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelectedModule();

		/**
		 * Channel marking the stored changes of the access definition that are not yet applied.
		 *
		 * <p>
		 * Whenever its value changes, the {@link #getSelectedRules() rules} of the selected type are
		 * written again, so that a stored change shows in them right away, see
		 * {@link SecurityCoverageTable#RULE_STATE}.
		 * </p>
		 */
		@Name(PENDING)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getPending();
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _selectionRef;

	private final ChannelRef _selectedTypeRef;

	private final ChannelRef _selectedInternalRef;

	private final ChannelRef _selectedWithoutSecurityRef;

	private final ChannelRef _selectedFindingsRef;

	private final ChannelRef _selectedRulesRef;

	private final ChannelRef _selectedModuleRef;

	private final ChannelRef _pendingRef;

	/**
	 * Creates a new {@link SecurityCoverageTable} from configuration.
	 */
	@CalledByReflection
	public SecurityCoverageTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_selectionRef = config.getSelection();
		_selectedTypeRef = config.getSelectedType();
		_selectedInternalRef = config.getSelectedInternal();
		_selectedWithoutSecurityRef = config.getSelectedWithoutSecurity();
		_selectedFindingsRef = config.getSelectedFindings();
		_selectedModuleRef = config.getSelectedModule();
		_selectedRulesRef = config.getSelectedRules();
		_pendingRef = config.getPending();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<Object, ?>> columns = new ArrayList<>();
		columns.add(withUnattachedLabels(objectColumn(COLUMN_TYPE, I18NConstants.COVERAGE_COLUMN_TYPE,
			TL_MODEL_PART_TYPE, false, row -> isModelRow(row) ? modelValue(row) : null, 300)));
		columns.add(statusColumn());
		columns.add(objectColumn(COLUMN_READ_ROLES, I18NConstants.COVERAGE_COLUMN_READ_ROLES, BoundedRole.ROLE_TYPE,
			true, ofType(SecurityCoverageTable::readRoles), 230));
		columns.add(textColumn(COLUMN_ROLE_RULES, I18NConstants.COVERAGE_COLUMN_ROLE_RULES,
			ofType(SecurityCoverageTable::roleRules), 200));
		columns.add(textColumn(COLUMN_ROLE_PARENTS, I18NConstants.COVERAGE_COLUMN_ROLE_PARENTS,
			ofType(SecurityCoverageTable::roleParents), 260));
		columns.add(textColumn(COLUMN_ACCESS_PARENT, I18NConstants.COVERAGE_COLUMN_ACCESS_PARENT,
			ofType(SecurityCoverageTable::accessParent), 260));
		columns.add(textColumn(COLUMN_FINDINGS, I18NConstants.COVERAGE_COLUMN_FINDINGS,
			SecurityCoverageTable::findingsText, 460));

		ViewChannel dataChannel = _inputRef != null ? context.resolveChannel(_inputRef) : null;
		CoverageTree tree = new CoverageTree();
		tree.update(rows(dataChannel == null ? null : dataChannel.get()));
		TreeRowSource<Object, Object> source = new TreeRowSource<>(tree, columns);
		TableViewState initialState =
			DefaultTableView.initialState(columns, SortSpec.NONE, Set.of(COLUMN_FINDINGS));
		DefaultTableView<Object> view = new DefaultTableView<>(columns, source, initialState);
		Set<Object> opened = new HashSet<>();
		openNew(view, tree, opened);
		TableViewControl<Object> control = new TableViewControl<>(context, view, true);

		Detail detail = new Detail(
			_selectionRef == null ? null : context.resolveChannel(_selectionRef),
			_selectedTypeRef == null ? null : context.resolveChannel(_selectedTypeRef),
			_selectedInternalRef == null ? null : context.resolveChannel(_selectedInternalRef),
			_selectedWithoutSecurityRef == null ? null : context.resolveChannel(_selectedWithoutSecurityRef),
			_selectedFindingsRef == null ? null : context.resolveChannel(_selectedFindingsRef),
			_selectedRulesRef == null ? null : context.resolveChannel(_selectedRulesRef),
			_selectedModuleRef == null ? null : context.resolveChannel(_selectedModuleRef));
		if (detail.isBound()) {
			control.addSelectionListener(keys -> {
				Object node = keys.size() == 1 ? keys.iterator().next() : null;
				detail.setNode(node);
				detail.show(node, tree::coverage);
			});
		}

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> {
				tree.update(rows(newValue));
				source.refresh();
				openNew(view, tree, opened);
				control.refreshData();
				if (detail.isBound()) {
					// The rows are fresh instances, so the display of the node that stays selected
					// would otherwise keep describing the analysis that was replaced.
					detail.show(detail.getNode(), tree::coverage);
				}
			};
			Registration registration = dataChannel.addListener(listener);
			control.addCleanupAction(registration::dispose);
		}
		if (_pendingRef != null && detail.isBound()) {
			// A stored change does not change the analysis, but the rules of the selected type show
			// the stored changes, see ruleEntries().
			ChannelListener listener = (sender, oldValue, newValue) -> detail.show(detail.getNode(), tree::coverage);
			Registration registration = context.resolveChannel(_pendingRef).addListener(listener);
			control.addCleanupAction(registration::dispose);
		}
		return control;
	}

	/**
	 * Opens the modules of the given tree that were not displayed before, so that every module shows
	 * its types when it appears.
	 */
	private static void openNew(DefaultTableView<Object> view, CoverageTree tree, Set<Object> opened) {
		for (TLModule module : tree.modules()) {
			if (opened.add(module)) {
				view.setExpanded(module, true);
			}
		}
		if (tree.roots().contains(UNATTACHED) && opened.add(UNATTACHED)) {
			view.setExpanded(UNATTACHED, true);
		}
	}

	/**
	 * The analyzed types as a tree: the modules, sorted by name, with the types of each module as
	 * their children.
	 *
	 * <p>
	 * A node is the {@link TLModule} or {@link TLClass} itself, which stays the same object across
	 * analyses; the row displayed for a type is its current {@link TypeCoverage}. The findings that
	 * belong to no analyzed type follow the modules as the children of the node
	 * {@link SecurityCoverageTable#UNATTACHED}; each of them is its own row.
	 * </p>
	 */
	public static final class CoverageTree implements TreeStructure<Object, Object> {

		private List<TLModule> _modules = List.of();

		private Map<TLModule, List<Object>> _types = Map.of();

		private Map<TLClass, TypeCoverage> _coverages = Map.of();

		private List<Object> _unattached = List.of();

		/**
		 * Replaces the tree by the given rows: analyzed types and unattached findings, see
		 * {@link SecurityCoverageTable#analysisRows()}.
		 */
		public void update(List<?> rows) {
			Map<TLModule, List<Object>> types = new HashMap<>();
			Map<TLClass, TypeCoverage> coverages = new HashMap<>();
			List<Object> unattached = new ArrayList<>();
			for (Object row : rows) {
				if (row instanceof CoverageFinding finding) {
					unattached.add(finding);
					continue;
				}
				TypeCoverage coverage = (TypeCoverage) row;
				TLClass type = coverage.type();
				coverages.put(type, coverage);
				types.computeIfAbsent(type.getModule(), module -> new ArrayList<>()).add(type);
			}
			List<TLModule> modules = new ArrayList<>(types.keySet());
			modules.sort(Comparator.comparing(TLModule::getName));
			_modules = modules;
			_types = types;
			_coverages = coverages;
			_unattached = unattached;
		}

		/**
		 * The modules of the analyzed types.
		 */
		List<TLModule> modules() {
			return _modules;
		}

		/**
		 * The analysis of the given node if it is a type, else <code>null</code>.
		 */
		public TypeCoverage coverage(Object node) {
			return node instanceof TLClass type ? _coverages.get(type) : null;
		}

		/**
		 * The modules, followed by the {@link SecurityCoverageTable#UNATTACHED group} of the
		 * unattached findings when there are any.
		 */
		@Override
		public List<Object> roots() {
			List<Object> result = new ArrayList<>(_modules);
			if (!_unattached.isEmpty()) {
				result.add(UNATTACHED);
			}
			return result;
		}

		@Override
		public List<Object> children(Object node) {
			if (node == UNATTACHED) {
				return _unattached;
			}
			return node instanceof TLModule module ? _types.getOrDefault(module, List.of()) : List.of();
		}

		@Override
		public boolean isLeaf(Object node) {
			return !(node instanceof TLModule) && node != UNATTACHED;
		}

		@Override
		public Object businessObject(Object node) {
			TypeCoverage coverage = coverage(node);
			return coverage != null ? coverage : node;
		}
	}

	/**
	 * The given value of an analyzed type as a column value, which is empty for a module.
	 */
	private static <T> Function<Object, T> ofType(Function<TypeCoverage, T> value) {
		return row -> row instanceof TypeCoverage coverage ? value.apply(coverage) : null;
	}

	/**
	 * The channels describing the selected node, and the node they describe.
	 *
	 * <p>
	 * The node is remembered because the rows are replaced as a whole whenever the analysis is
	 * repeated: the type that stays selected has a different {@link TypeCoverage} afterwards, and
	 * the channels are pushed again for it without the user having to select it anew.
	 * </p>
	 */
	public static final class Detail {

		private final ViewChannel _selection;

		private final ViewChannel _type;

		private final ViewChannel _internal;

		private final ViewChannel _withoutSecurity;

		private final ViewChannel _findings;

		private final ViewChannel _rules;

		private final ViewChannel _module;

		private Object _node;

		/**
		 * Creates a {@link Detail} over the channels the table is configured with, each of them
		 * <code>null</code> where the configuration names none.
		 */
		public Detail(ViewChannel selection, ViewChannel type, ViewChannel internal, ViewChannel withoutSecurity,
				ViewChannel findings, ViewChannel rules, ViewChannel module) {
			_selection = selection;
			_type = type;
			_internal = internal;
			_withoutSecurity = withoutSecurity;
			_findings = findings;
			_rules = rules;
			_module = module;
		}

		/**
		 * Whether any channel is bound at all.
		 */
		boolean isBound() {
			return _selection != null || _type != null || _internal != null || _withoutSecurity != null
				|| _findings != null || _rules != null || _module != null;
		}

		/**
		 * The node being described, <code>null</code> while nothing is selected.
		 */
		Object getNode() {
			return _node;
		}

		/**
		 * @see #getNode()
		 */
		void setNode(Object node) {
			_node = node;
		}

		/**
		 * Writes what the given node is described by to the bound channels: a type by its analysis,
		 * a module by itself; clearing them all for <code>null</code>.
		 *
		 * @param coverages
		 *        The analysis of a type node.
		 */
		public void show(Object node, Function<Object, TypeCoverage> coverages) {
			TLModule module = node instanceof TLModule selected ? selected : null;
			show(node == null || module != null ? null : coverages.apply(node));
			if (_module != null) {
				_module.set(module);
			}
			if (node instanceof CoverageFinding finding && _findings != null) {
				// A finding belonging to no type is described by itself.
				_findings.set(findingsHtml(List.of(finding)));
			}
		}

		/**
		 * Writes what the given row is described by to the bound channels, clearing them all for
		 * <code>null</code>.
		 */
		private void show(TypeCoverage row) {
			if (_selection != null) {
				_selection.set(row);
			}
			if (_type != null) {
				_type.set(row == null ? null : row.type());
			}
			if (_internal != null) {
				_internal.set(row == null ? null : mark(row.type(), row.internalOrigin()));
			}
			if (_withoutSecurity != null) {
				_withoutSecurity.set(row == null ? null : mark(row.type(), row.withoutSecurityOrigin()));
			}
			if (_findings != null) {
				_findings.set(row == null ? null : findingsHtml(row));
			}
			if (_rules != null) {
				_rules.set(row == null ? List.of() : ruleEntries(row));
			}
		}
	}

	/**
	 * The rules in effect for the given type, the role parent rules first, each as the
	 * dictionary a display shows and a command works on, followed by the stored rules for the type
	 * that are not yet in effect.
	 *
	 * @see Config#getSelectedRules()
	 */
	private static List<Map<String, Object>> ruleEntries(TypeCoverage coverage) {
		SecurityDefinitionEditor editor = new SecurityDefinitionEditor();
		List<? extends NavigationRuleConfig> storedParents = stored(editor::storedRoleParentRules);
		List<? extends RoleRuleConfig> storedRoleRules = stored(editor::storedRoleRules);
		Set<String> removedParents = removed(editor::removedRoleParentRules);
		Set<String> removedRoleRules = removed(editor::removedRoleRules);
		Resources resources = Resources.getInstance();
		String parentKind = resources.getString(I18NConstants.COVERAGE_RULE_KIND_ROLE_PARENT);
		String roleKind = resources.getString(I18NConstants.COVERAGE_RULE_KIND_ROLE_RULE);

		List<Map<String, Object>> result = new ArrayList<>();
		for (NavigationRule rule : coverage.roleParentRules()) {
			String id = rule.getId();
			NavigationRuleConfig stored = find(storedParents, id);
			result.add(ruleEntry(KIND_ROLE_PARENT, parentKind, id, path(rule.getPath()), stored != null,
				state(stored, editor.effectiveRoleParentRule(id), removedParents.contains(id))));
		}
		for (Map.Entry<String, List<RoleProvider>> group : roleRulesByConfigId(coverage).entrySet()) {
			String id = group.getKey();
			RoleRuleConfig stored = find(storedRoleRules, id);
			result.add(ruleEntry(KIND_ROLE_RULE, roleKind, id, roleRuleDescription(group.getValue()),
				stored != null, state(stored, editor.effectiveRoleRule(id), removedRoleRules.contains(id))));
		}
		for (NavigationRuleConfig rule : storedParents) {
			if (editor.effectiveRoleParentRule(rule.getId()) == null && appliesTo(rule, coverage.type())) {
				result.add(ruleEntry(KIND_ROLE_PARENT, parentKind, rule.getId(), "", true, STATE_NEW));
			}
		}
		for (RoleRuleConfig rule : storedRoleRules) {
			if (editor.effectiveRoleRule(rule.getId()) == null && appliesTo(rule, coverage.type())) {
				result.add(ruleEntry(KIND_ROLE_RULE, roleKind, rule.getId(),
					String.join(VALUE_SEPARATOR, rule.getRole()), true, STATE_NEW));
			}
		}
		return result;
	}

	/**
	 * The {@link #RULE_STATE} of a rule in effect.
	 *
	 * @param stored
	 *        The stored form of the rule, <code>null</code> when it is not stored.
	 * @param effective
	 *        The rule as it is in effect.
	 * @param removed
	 *        Whether the stored configuration removes the rule.
	 */
	private static String state(NavigationRuleConfig stored, NavigationRuleConfig effective, boolean removed) {
		if (removed) {
			return STATE_DELETED;
		}
		if (stored != null && effective != null && !ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(stored, effective)) {
			return STATE_CHANGED;
		}
		return STATE_APPLIED;
	}

	/**
	 * Whether the given stored rule applies to the given type: it is configured for the type, for a
	 * generalization of it including its sub types, or for a singleton of the type.
	 */
	private static boolean appliesTo(NavigationRuleConfig rule, TLClass type) {
		try {
			if (rule instanceof SingletonRuleConfig singletonRule) {
				return TLModelUtil.resolveQualifiedName(singletonRule.getTarget()).tType() == type;
			}
			String typeName = rule.getMetaElement();
			if (typeName == null || typeName.isBlank()) {
				return false;
			}
			TLType ruleType = TLModelUtil.findType(typeName);
			if (ruleType == type) {
				return true;
			}
			return rule.isInherit() && ruleType instanceof TLClass ruleClass
				&& TLModelUtil.isGeneralization(ruleClass, type);
		} catch (RuntimeException ex) {
			// A rule referring to a model element that does not exist applies nowhere.
			return false;
		}
	}

	/**
	 * The rule with the given id of the given rules, or <code>null</code>.
	 */
	private static <R extends NavigationRuleConfig> R find(List<? extends R> rules, String id) {
		for (R rule : rules) {
			if (id.equals(rule.getId())) {
				return rule;
			}
		}
		return null;
	}

	/**
	 * The rules the given query of the stored configuration answers, empty when that configuration
	 * cannot be read.
	 */
	private static <R extends NavigationRuleConfig> List<? extends R> stored(StoredRulesQuery<R> query) {
		try {
			return query.run();
		} catch (ConfigurationException ex) {
			Logger.error("Cannot read the stored access definition.", ex, SecurityCoverageTable.class);
			return List.of();
		}
	}

	/**
	 * The ids of the removed rules the given query of the stored configuration answers, empty when
	 * that configuration cannot be read.
	 */
	private static Set<String> removed(RemovedRulesQuery query) {
		try {
			return query.run();
		} catch (ConfigurationException ex) {
			Logger.error("Cannot read the stored access definition.", ex, SecurityCoverageTable.class);
			return Set.of();
		}
	}

	/**
	 * One entry of {@link Config#getSelectedRules()}.
	 */
	private static Map<String, Object> ruleEntry(String kind, String kindLabel, String id, String description,
			boolean stored, String state) {
		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put(RULE_KIND, kind);
		entry.put(RULE_KIND_LABEL, kindLabel);
		entry.put(RULE_ID, id);
		entry.put(RULE_DESCRIPTION, description);
		entry.put(RULE_STORED, Boolean.valueOf(stored));
		entry.put(RULE_STATE, state);
		entry.put(RULE_STATE_LABEL, stateLabel(state));
		return entry;
	}

	/**
	 * The localized name of the given {@link #RULE_STATE}.
	 */
	private static String stateLabel(String state) {
		ResKey label = switch (state) {
			case STATE_NEW -> I18NConstants.COVERAGE_RULE_STATE_NEW;
			case STATE_CHANGED -> I18NConstants.COVERAGE_RULE_STATE_CHANGED;
			case STATE_DELETED -> I18NConstants.COVERAGE_RULE_STATE_DELETED;
			default -> null;
		};
		return label == null ? "" : Resources.getInstance().getString(label);
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
	private interface StoredRulesQuery<R extends NavigationRuleConfig> {

		/**
		 * Answers the query.
		 */
		List<? extends R> run() throws ConfigurationException;
	}

	/**
	 * A query for the ids of the rules the stored configuration removes.
	 */
	private interface RemovedRulesQuery {

		/**
		 * Answers the query.
		 */
		Set<String> run() throws ConfigurationException;
	}

	/**
	 * The rows held by the channel value, or a fresh analysis when the channel is unset.
	 *
	 * @see #analysisRows()
	 */
	private static List<Object> rows(Object value) {
		return value instanceof List<?> list ? new ArrayList<>(list) : analysisRows();
	}

	/**
	 * A fresh analysis as the rows of this table: the {@link TypeCoverage} of every analyzed type,
	 * followed by the {@link CoverageFinding}s that belong to no analyzed type
	 * ({@link SecurityCoverageAnalysis#getUnattachedFindings()}), which the table shows in a group
	 * of their own.
	 *
	 * <p>
	 * This is the value a command refreshing the table writes to its {@link Config#getInput() input
	 * channel}.
	 * </p>
	 */
	public static List<Object> analysisRows() {
		SecurityCoverageAnalysis analysis = SecurityCoverageCheck.getInstance().newAnalysis();
		List<Object> result = new ArrayList<>(analysis.analyze());
		result.addAll(analysis.getUnattachedFindings());
		return result;
	}

	/**
	 * Whether the given row stands for a part of the model: an analyzed type or a module.
	 */
	private static boolean isModelRow(Object row) {
		return row instanceof TypeCoverage || row instanceof TLModule;
	}

	/**
	 * The model part the given {@link #isModelRow(Object) model row} stands for.
	 */
	private static Object modelValue(Object row) {
		return row instanceof TypeCoverage coverage ? coverage.type() : row;
	}

	/**
	 * The given column, showing the label of the group of the unattached findings and the places
	 * of each of these findings in the rows that stand for no part of the model.
	 */
	private static <V> Column<Object, V> withUnattachedLabels(Column<Object, V> column) {
		return new DelegatingColumn<>(column) {
			@Override
			public CellContent renderCell(Object row) {
				String label = unattachedLabel(row);
				return label != null ? CellContent.text(label) : super.renderCell(row);
			}

			@Override
			public String searchText(Object row) {
				String label = unattachedLabel(row);
				return label != null ? label : super.searchText(row);
			}
		};
	}

	/**
	 * The text the type column shows for a row standing for no part of the model: the label of the
	 * {@link #UNATTACHED group} of the unattached findings, or the places of such a finding.
	 *
	 * @return <code>null</code> for a {@link #isModelRow(Object) model row}.
	 */
	private static String unattachedLabel(Object row) {
		if (row == UNATTACHED) {
			return Resources.getInstance().getString(I18NConstants.COVERAGE_UNATTACHED_GROUP);
		}
		if (row instanceof CoverageFinding finding) {
			return users(finding);
		}
		return null;
	}

	/**
	 * The text of the findings column: the messages of the findings of a type, the message of an
	 * unattached finding, nothing for a module.
	 */
	private static String findingsText(Object row) {
		if (row instanceof TypeCoverage) {
			return findings(row);
		}
		if (row instanceof CoverageFinding finding) {
			return Resources.getInstance().getString(finding.getMessage());
		}
		return null;
	}

	/**
	 * The row as the analyzed type it holds.
	 */
	private static TypeCoverage coverage(Object row) {
		return (TypeCoverage) row;
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
	 * The paths of the role parent rules applying to the type.
	 */
	private static String roleParents(Object row) {
		return coverage(row).roleParentRules().stream()
			.map(rule -> path(rule.getPath()))
			.collect(Collectors.joining(RULE_SEPARATOR));
	}

	/**
	 * The access parent the type delegates to, described by its
	 * {@link AccessParentFunction#getLabel() label}.
	 */
	private static String accessParent(Object row) {
		AccessParentFunction parent = coverage(row).accessParent();
		if (parent == null) {
			return "";
		}
		return Resources.getInstance().getString(parent.getLabel());
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
	 * {@link #INVERSE_MARKER} when it is navigated backwards, or the script computing it, as it is
	 * written in the configuration and shown for a script access parent.
	 */
	private static String step(PathElement element) {
		if (element instanceof PathNavigation navigation) {
			String reference = TLModelUtil.qualifiedName(navigation.getReference());
			return navigation.isInverse() ? INVERSE_MARKER + reference : reference;
		}
		if (element instanceof PathByExpression script) {
			// The tooltip text is HTML and shows the compiled expression, not what was written.
			return ExprFormat.INSTANCE.getSpecification(script.getConfig().getExpression());
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
		TypeCoverage coverage = coverage(row);
		ResKey exemption = exemption(coverage);
		if (exemption != null) {
			return resources.getString(exemption);
		}
		return coverage.findings().stream()
			.map(CoverageFinding::getMessage)
			.map(resources::getString)
			.collect(Collectors.joining(RULE_SEPARATOR));
	}

	/**
	 * The value a channel receives for a mark of the given type.
	 *
	 * @param type
	 *        The marked type.
	 * @param origin
	 *        The part declaring the mark, <code>null</code> when the type is not marked.
	 * @return <code>true</code> for a mark of the type's own, <code>false</code> for no mark, the
	 *         origin for an inherited mark.
	 *
	 * @see Config#getSelectedInternal()
	 */
	static Object mark(TLClass type, TLModelPart origin) {
		if (origin == null) {
			return Boolean.FALSE;
		}
		return origin == type ? Boolean.TRUE : origin;
	}

	/**
	 * The mark exempting the given type from the check, <code>null</code> when it is checked.
	 */
	private static ResKey exemption(TypeCoverage coverage) {
		if (coverage.internal()) {
			return inherited(coverage, coverage.internalOrigin())
				? I18NConstants.COVERAGE_EXEMPT_INTERNAL_INHERITED__ORIGIN.fill(coverage.internalOrigin())
				: I18NConstants.COVERAGE_EXEMPT_INTERNAL;
		}
		if (coverage.withoutSecurity()) {
			return inherited(coverage, coverage.withoutSecurityOrigin())
				? I18NConstants.COVERAGE_EXEMPT_WITHOUT_SECURITY_INHERITED__ORIGIN
					.fill(coverage.withoutSecurityOrigin())
				: I18NConstants.COVERAGE_EXEMPT_WITHOUT_SECURITY;
		}
		return null;
	}

	/**
	 * Whether the given mark of the type is declared by a generalization or a module, not by the
	 * type itself.
	 */
	private static boolean inherited(TypeCoverage coverage, TLModelPart origin) {
		return origin != coverage.type();
	}

	/**
	 * The findings of the given type as the HTML a detail display shows: a list with one entry per
	 * problem, the ways to solve it nested underneath. A type with an access parent is introduced
	 * by a paragraph saying what it delegates to.
	 */
	private static String findingsHtml(TypeCoverage coverage) {
		Resources resources = Resources.getInstance();
		ResKey exemption = exemption(coverage);
		if (exemption != null) {
			return "<p>" + TagUtil.encodeXML(resources.getString(exemption)) + "</p>";
		}
		StringBuilder html = new StringBuilder();
		ResKey delegation = delegation(coverage);
		if (delegation != null) {
			html.append("<p>").append(TagUtil.encodeXML(resources.getString(delegation))).append("</p>");
		}
		html.append(findingsHtml(coverage.findings()));
		return html.toString();
	}

	/**
	 * The given findings as an HTML list with one entry per problem, the ways to solve it nested
	 * underneath.
	 */
	private static String findingsHtml(List<CoverageFinding> findings) {
		Resources resources = Resources.getInstance();
		StringBuilder html = new StringBuilder();
		html.append("<ul>");
		for (CoverageFinding finding : findings) {
			ResKey problem = problem(finding);
			if (problem == null) {
				continue;
			}
			html.append("<li>");
			html.append(TagUtil.encodeXML(resources.getString(problem)));
			html.append("<ul>");
			for (ResKey solution : solutions(finding)) {
				html.append("<li>").append(TagUtil.encodeXML(resources.getString(solution))).append("</li>");
			}
			html.append("</ul>");
			html.append("</li>");
		}
		html.append("</ul>");
		return html.toString();
	}

	/**
	 * The description of the access parent the given type delegates to, <code>null</code> for a
	 * type deciding for itself.
	 */
	private static ResKey delegation(TypeCoverage coverage) {
		AccessParentFunction parent = coverage.accessParent();
		if (parent == null) {
			return null;
		}
		if (parent instanceof ContainerRelation container && container.composition() == null) {
			String containers = coverage.containerReferences().stream()
				.map(TLModelUtil::qualifiedName)
				.collect(Collectors.joining(VALUE_SEPARATOR));
			return container.configured()
				? I18NConstants.COVERAGE_DELEGATED_CONTAINER__CONTAINERS.fill(containers)
				: I18NConstants.COVERAGE_DELEGATED_DEFAULT__CONTAINERS.fill(containers);
		}
		return I18NConstants.COVERAGE_DELEGATED__PARENT.fill(accessParent(coverage));
	}

	/**
	 * The short description of the problem the given finding reports.
	 */
	private static ResKey problem(CoverageFinding finding) {
		return switch (finding.getKind()) {
			case NO_ROLE_SOURCE -> I18NConstants.COVERAGE_PROBLEM_NO_ROLE_SOURCE;
			case NO_READ_GRANT -> I18NConstants.COVERAGE_PROBLEM_NO_READ_GRANT;
			case DEAD_GRANT -> I18NConstants.COVERAGE_PROBLEM_DEAD_GRANT__OPERATION_ROLES.fill(
				finding.getOperationLabel(),
				finding.getRoles().stream().map(BoundedRole::getName).sorted().collect(Collectors.joining(VALUE_SEPARATOR)));
			case SHADOWED_RULES -> I18NConstants.COVERAGE_PROBLEM_SHADOWED_RULES__RULES
				.fill(String.join(VALUE_SEPARATOR, finding.getRuleIds()));
			case UNGRANTED_OPERATION -> I18NConstants.COVERAGE_PROBLEM_UNGRANTED_OPERATION__OPERATION
				.fill(finding.getOperationLabel());
			case INAPPLICABLE_OPERATION -> I18NConstants.COVERAGE_PROBLEM_INAPPLICABLE_OPERATION__OPERATION_USERS
				.fill(finding.getOperationLabel(), users(finding));
			case UNKNOWN_OPERATION -> I18NConstants.COVERAGE_PROBLEM_UNKNOWN_OPERATION__OPERATION_USERS
				.fill(finding.getOperationLabel(), users(finding));
			case UNCHECKED_GRANT -> I18NConstants.COVERAGE_PROBLEM_UNCHECKED_GRANT__OPERATION_ROLES.fill(
				finding.getOperationLabel(),
				finding.getRoles().stream().map(BoundedRole::getName).sorted().collect(Collectors.joining(VALUE_SEPARATOR)));
		};
	}

	/**
	 * The places of the application a finding about an operation lists.
	 */
	private static String users(CoverageFinding finding) {
		return finding.getUses().stream().map(OperationUse::user).collect(Collectors.joining(VALUE_SEPARATOR));
	}

	/**
	 * The ways to solve the problem the given finding reports, the most specific one first.
	 */
	private static List<ResKey> solutions(CoverageFinding finding) {
		List<ResKey> solutions = new ArrayList<>();
		switch (finding.getKind()) {
			case NO_ROLE_SOURCE -> {
				solutions.add(I18NConstants.COVERAGE_SOLUTION_ACCESS_PARENT);
				solutions.add(I18NConstants.COVERAGE_SOLUTION_ROLE_PARENT_RULE);
				solutions.add(I18NConstants.COVERAGE_SOLUTION_ROLE_RULE);
				solutions.add(I18NConstants.COVERAGE_SOLUTION_MARK_INTERNAL);
			}
			case NO_READ_GRANT -> {
				solutions.add(I18NConstants.COVERAGE_SOLUTION_READ_GRANT);
				solutions.add(I18NConstants.COVERAGE_SOLUTION_MARK_INTERNAL);
			}
			case DEAD_GRANT -> {
				solutions.add(I18NConstants.COVERAGE_SOLUTION_DELIVER_ROLE);
				solutions.add(I18NConstants.COVERAGE_SOLUTION_CHANGE_GRANT);
			}
			case SHADOWED_RULES -> {
				solutions.add(I18NConstants.COVERAGE_SOLUTION_REMOVE_SHADOWED_RULES);
				solutions.add(I18NConstants.COVERAGE_SOLUTION_REMOVE_ACCESS_PARENT);
			}
			case UNGRANTED_OPERATION -> {
				solutions.add(I18NConstants.COVERAGE_SOLUTION_GRANT_OPERATION);
			}
			case INAPPLICABLE_OPERATION -> {
				solutions.add(I18NConstants.COVERAGE_SOLUTION_DECLARE_OPERATION);
				solutions.add(I18NConstants.COVERAGE_SOLUTION_CHECK_OTHER_OPERATION);
			}
			case UNKNOWN_OPERATION -> {
				solutions.add(I18NConstants.COVERAGE_SOLUTION_REGISTER_OPERATION);
				solutions.add(I18NConstants.COVERAGE_SOLUTION_CHECK_OTHER_OPERATION);
			}
			case UNCHECKED_GRANT -> {
				solutions.add(I18NConstants.COVERAGE_SOLUTION_REMOVE_UNCHECKED_GRANT);
				solutions.add(I18NConstants.COVERAGE_SOLUTION_ADD_CHECK);
			}
		}
		return solutions;
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
		// A module has no status, and matches no filter on it.
		Function<CoverageStatus, String> text =
			status -> status == null ? null : resources.getString(ResKey.forEnum(status));
		return DefaultColumn.<Object, CoverageStatus> builder(COLUMN_STATUS, ofType(TypeCoverage::status))
			.label(I18NConstants.COVERAGE_COLUMN_STATUS)
			.renderer(status -> status == null ? CellContent.text("") : new CellContent.Raw(
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
