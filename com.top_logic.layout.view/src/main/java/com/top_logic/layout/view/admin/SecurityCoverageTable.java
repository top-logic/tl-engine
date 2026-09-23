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
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.CoverageStatus;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.element.boundsec.manager.rule.NavigationRule;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.PathNavigation;
import com.top_logic.element.boundsec.manager.rule.RoleProvider;
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
 * {@link Config#getSelection() selection channel} (so a command can act on it); the parts of it a
 * detail display shows go to the {@link Config#getSelectedFindings() findings} and the
 * {@link Config#getSelectedRule() rule channel}, both cleared to {@code null} when the selection is
 * empty.
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

	/**
	 * Configuration for {@link SecurityCoverageTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getSelectedFindings()}. */
		String SELECTED_FINDINGS = "selected-findings";

		/** Configuration name for {@link #getSelectedRule()}. */
		String SELECTED_RULE = "selected-rule";

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
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _selectionRef;

	private final ChannelRef _selectedFindingsRef;

	private final ChannelRef _selectedRuleRef;

	/**
	 * Creates a new {@link SecurityCoverageTable} from configuration.
	 */
	@CalledByReflection
	public SecurityCoverageTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_selectionRef = config.getSelection();
		_selectedFindingsRef = config.getSelectedFindings();
		_selectedRuleRef = config.getSelectedRule();
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

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> {
				List<Object> newRows = rows(newValue);
				rowByKey.clear();
				rowByKey.putAll(index(newRows));
				source.setElements(newRows);
				control.refreshData();
			};
			dataChannel.addListener(listener);
			control.addCleanupAction(() -> dataChannel.removeListener(listener));
		}

		ViewChannel selection = _selectionRef != null ? context.resolveChannel(_selectionRef) : null;
		ViewChannel selectedFindings =
			_selectedFindingsRef != null ? context.resolveChannel(_selectedFindingsRef) : null;
		ViewChannel selectedRule = _selectedRuleRef != null ? context.resolveChannel(_selectedRuleRef) : null;
		if (selection != null || selectedFindings != null || selectedRule != null) {
			control.addSelectionListener(keys -> {
				TypeCoverage row = keys.size() == 1 ? rowByKey.get(keys.iterator().next()) : null;
				if (selection != null) {
					selection.set(row);
				}
				if (selectedFindings != null) {
					selectedFindings.set(row == null ? null : findings(row));
				}
				if (selectedRule != null) {
					String rule = row == null ? null : SecurityCoverageAction.ruleHtml(row);
					selectedRule.set(rule == null || rule.isEmpty() ? null : rule);
				}
			});
		}
		return control;
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
	 */
	private static String roleRules(Object row) {
		return coverage(row).roleRules().stream()
			.map(RoleProvider::getId)
			.collect(Collectors.joining(VALUE_SEPARATOR));
	}

	/**
	 * The paths of the security parent rules applying to the type.
	 */
	private static String securityParents(Object row) {
		return coverage(row).securityParentRules().stream()
			.map(SecurityCoverageTable::path)
			.collect(Collectors.joining(RULE_SEPARATOR));
	}

	/**
	 * The navigation path of the given rule, one entry per step.
	 */
	private static String path(NavigationRule rule) {
		return rule.getPath().stream()
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
				(CellControlFactory) context -> MetaResourceControlProvider.INSTANCE.createControl(context, status)))
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
