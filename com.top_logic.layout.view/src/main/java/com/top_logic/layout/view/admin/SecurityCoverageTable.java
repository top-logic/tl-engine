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
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/**
 * Table of the model based access definition, one row per analyzed type with the roles that may
 * read it, the rules that deliver a role on it and the gaps found in its definition.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * The table shows a fresh analysis when opened and rebuilds from the configured
 * {@link Config#getInput() input channel} after a command. The selected row is written to the
 * {@link Config#getSelection() selection channel} (so a command can act on it); the parts of it a
 * detail display shows go to the {@link Config#getSelectedType() type}, the
 * {@link Config#getSelectedFindings() findings} and the {@link Config#getSelectedRule() rule
 * channel}, all cleared to {@code null} when the selection is empty.
 * </p>
 *
 * @implNote The rows come from {@link SecurityCoverageCheck#analyze()}; a row is keyed by the
 *           qualified name of its type, since every analysis yields fresh {@link TypeCoverage}
 *           instances and the selection is to survive a refresh.
 */
public class SecurityCoverageTable implements UIElement {

	/** Separator between the entries of a list rendered into a cell. */
	private static final String VALUE_SEPARATOR = ", ";

	/** Separator between the rules rendered into a cell. */
	private static final String RULE_SEPARATOR = "; ";

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

		/** Configuration name for {@link #getSelectedType()}. */
		String SELECTED_TYPE = "selected-type";

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
		 * Channel the qualified name of the selected type is written to.
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
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _selectionRef;

	private final ChannelRef _selectedTypeRef;

	private final ChannelRef _selectedFindingsRef;

	private final ChannelRef _selectedRuleRef;

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
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		Resources resources = Resources.getInstance();
		String covered = resources.getString(I18NConstants.COVERAGE_STATUS_COVERED);
		String incomplete = resources.getString(I18NConstants.COVERAGE_STATUS_INCOMPLETE);

		List<Column<TypeCoverage, ?>> columns = new ArrayList<>();
		columns.add(textColumn("type", I18NConstants.COVERAGE_COLUMN_TYPE, SecurityCoverageTable::typeName, 320));
		columns.add(textColumn("status", I18NConstants.COVERAGE_COLUMN_STATUS,
			coverage -> coverage.status() == CoverageStatus.COVERED ? covered : incomplete, 120));
		columns.add(textColumn("readRoles", I18NConstants.COVERAGE_COLUMN_READ_ROLES,
			SecurityCoverageTable::readRoles, 200));
		columns.add(textColumn("roleRules", I18NConstants.COVERAGE_COLUMN_ROLE_RULES,
			SecurityCoverageTable::roleRules, 220));
		columns.add(textColumn("securityParents", I18NConstants.COVERAGE_COLUMN_SECURITY_PARENTS,
			SecurityCoverageTable::securityParents, 300));
		columns.add(textColumn("findings", I18NConstants.COVERAGE_COLUMN_FINDINGS,
			SecurityCoverageTable::findings, 460));

		ViewChannel dataChannel = _inputRef != null ? context.resolveChannel(_inputRef) : null;

		List<TypeCoverage> initialRows = rows(dataChannel == null ? null : dataChannel.get());
		Map<Object, TypeCoverage> rowByKey = new HashMap<>(index(initialRows));

		ListRowSource<TypeCoverage> source =
			new ListRowSource<>(initialRows, columns, SecurityCoverageTable::typeName);
		DefaultTableView<TypeCoverage> view = DefaultTableView.create(columns, source);
		TableViewControl<TypeCoverage> control = new TableViewControl<>(context, view, false);

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> {
				List<TypeCoverage> newRows = rows(newValue);
				rowByKey.clear();
				rowByKey.putAll(index(newRows));
				source.setElements(newRows);
				control.refreshData();
			};
			dataChannel.addListener(listener);
			control.addCleanupAction(() -> dataChannel.removeListener(listener));
		}

		ViewChannel selection = _selectionRef != null ? context.resolveChannel(_selectionRef) : null;
		ViewChannel selectedType = _selectedTypeRef != null ? context.resolveChannel(_selectedTypeRef) : null;
		ViewChannel selectedFindings =
			_selectedFindingsRef != null ? context.resolveChannel(_selectedFindingsRef) : null;
		ViewChannel selectedRule = _selectedRuleRef != null ? context.resolveChannel(_selectedRuleRef) : null;
		if (selection != null || selectedType != null || selectedFindings != null || selectedRule != null) {
			control.addSelectionListener(keys -> {
				TypeCoverage row = keys.size() == 1 ? rowByKey.get(keys.iterator().next()) : null;
				if (selection != null) {
					selection.set(row);
				}
				if (selectedType != null) {
					selectedType.set(row == null ? null : typeName(row));
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
	@SuppressWarnings("unchecked")
	private static List<TypeCoverage> rows(Object value) {
		if (value instanceof List<?> list) {
			return (List<TypeCoverage>) list;
		}
		return SecurityCoverageCheck.getInstance().analyze();
	}

	/**
	 * The given rows by their row key.
	 */
	private static Map<Object, TypeCoverage> index(List<TypeCoverage> rows) {
		Map<Object, TypeCoverage> result = new LinkedHashMap<>();
		for (TypeCoverage row : rows) {
			result.put(typeName(row), row);
		}
		return result;
	}

	/**
	 * The qualified name of the analyzed type, which is also the row key.
	 */
	private static String typeName(TypeCoverage coverage) {
		return TLModelUtil.qualifiedName(coverage.type());
	}

	/**
	 * The names of the roles that may read the type.
	 */
	private static String readRoles(TypeCoverage coverage) {
		return coverage.readRoles().stream()
			.map(BoundedRole::getName)
			.sorted()
			.collect(Collectors.joining(VALUE_SEPARATOR));
	}

	/**
	 * The ids of the rules delivering a role on the type.
	 */
	private static String roleRules(TypeCoverage coverage) {
		return coverage.roleRules().stream()
			.map(RoleProvider::getId)
			.collect(Collectors.joining(VALUE_SEPARATOR));
	}

	/**
	 * The paths of the security parent rules applying to the type.
	 */
	private static String securityParents(TypeCoverage coverage) {
		return coverage.securityParentRules().stream()
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
	private static String findings(TypeCoverage coverage) {
		Resources resources = Resources.getInstance();
		return coverage.findings().stream()
			.map(CoverageFinding::getMessage)
			.map(resources::getString)
			.collect(Collectors.joining(RULE_SEPARATOR));
	}

	/**
	 * A sortable, text-filterable column reading one {@link String} property of an analyzed type.
	 */
	private static Column<TypeCoverage, String> textColumn(String id, ResKey label,
			Function<? super TypeCoverage, String> value, int width) {
		return DefaultColumn.<TypeCoverage, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(width)
			.build();
	}
}
