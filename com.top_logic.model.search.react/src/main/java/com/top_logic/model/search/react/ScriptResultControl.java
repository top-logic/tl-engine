/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Renders a script evaluation result as a table whose columns are derived from the result and
 * rebuilt on every evaluation.
 *
 * <p>
 * The result is split into elements (one per element of a collection/array, a single element for a
 * scalar, none for {@code null}). The columns depend on the element values:
 * </p>
 * <ul>
 * <li>When <em>all</em> elements are {@link TLObject model objects}, the table reuses the platform
 * column auto-configuration ({@link ColumnProviderService}): one column per attribute - the union
 * of the non-hidden attributes across all object types present - with type-appropriate sorting,
 * filtering and cell rendering. The objects are the rows directly.</li>
 * <li>Otherwise (a primitive value is present, or no object attributes exist) a {@code result}
 * column shows each element's label, followed by columns for any object attributes with their
 * type-derived display. These columns are sortable and text-filterable.</li>
 * </ul>
 *
 * <p>
 * A {@link DefaultTableView}'s columns are fixed at construction, so a changed result rebuilds the
 * whole {@link TableViewControl} and swaps it in. This control is the wrapper that performs the
 * swap: it reuses the {@code TLPanel} fill container (the same one {@code <full-page>} uses, so the
 * table bounds its own scroll viewport and keeps its header pinned) and replaces its single child -
 * the result table - on each evaluation.
 * </p>
 *
 * @implNote The replaced table is disposed synchronously: its cell controls do not listen to the
 *           result channel that triggers the rebuild, so there is no re-entrant teardown.
 */
public class ScriptResultControl extends ReactControl {

	/** {@code TLPanel} state key holding the panel content (the result table). */
	private static final String CHILD = "child";

	/** {@code TLPanel} state key making the content fill the panel's bounded height. */
	private static final String FILL = "fill";

	/** Column id of the label column shown for primitive (non-object) results. */
	private static final String RESULT_COLUMN = "result";

	/**
	 * One result row of the generic (mixed/primitive) table, wrapping a result element with its
	 * position so duplicate values keep distinct row keys.
	 *
	 * @param index
	 *        The zero-based position of the element in the result, used as the row key.
	 * @param value
	 *        The result element, may be {@code null}.
	 */
	public record ScriptResultRow(int index, Object value) {
		// Marker record; components documented above.
	}

	private final ViewContext _context;

	private ReactControl _currentTable;

	/**
	 * Creates a new {@link ScriptResultControl}.
	 *
	 * @param context
	 *        The view context used to build the result table.
	 * @param dataChannel
	 *        Channel holding the raw evaluation result; a written result rebuilds the table. May be
	 *        {@code null} for a static empty table.
	 */
	public ScriptResultControl(ViewContext context, ViewChannel dataChannel) {
		super(context, null, "TLPanel");
		_context = context;
		putState(FILL, Boolean.TRUE);

		_currentTable = buildTable(dataChannel == null ? null : dataChannel.get());
		putState(CHILD, _currentTable);

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> rebuild(newValue);
			dataChannel.addListener(listener);
			addCleanupAction(() -> dataChannel.removeListener(listener));
		}
	}

	private void rebuild(Object value) {
		ReactControl old = _currentTable;
		ReactControl built = buildTable(value);
		_currentTable = built;

		Object token = beginUpdate();
		putState(CHILD, built);
		commitUpdate(token);

		if (old != null && old != built) {
			old.cleanupTree();
		}
	}

	private ReactControl buildTable(Object value) {
		List<Object> elements = elements(value);
		if (!elements.isEmpty() && elements.stream().allMatch(ScriptResultControl::isModelObject)) {
			ReactControl objectTable = buildObjectTable(elements);
			if (objectTable != null) {
				return objectTable;
			}
		}
		return buildGenericTable(elements);
	}

	/**
	 * Builds a table for an all-object result, reusing {@link ColumnProviderService} for type-aware
	 * sortable/filterable columns (the union of non-hidden attributes across all types present).
	 * Returns {@code null} when no columns could be derived, so the caller falls back to the generic
	 * table.
	 */
	private ReactControl buildObjectTable(List<Object> elements) {
		Map<String, TLStructuredTypePart> parts = attributeUnion(elements);
		if (parts.isEmpty()) {
			return null;
		}
		ColumnProviderService columnService = ColumnProviderService.getInstance();
		List<Column<Object, ?>> columns = new ArrayList<>(parts.size());
		for (TLStructuredTypePart part : parts.values()) {
			columns.add(columnService.createColumn(part.getName(), TLModelNamingConvention.resourceKey(part), part));
		}
		ListRowSource<Object> source = new ListRowSource<>(elements, columns);
		DefaultTableView<Object> view = DefaultTableView.create(columns, source);
		return new TableViewControl<>(_context, view, false);
	}

	/**
	 * Builds the fallback table for a result containing primitive values: a label {@code result}
	 * column followed by columns for any object attributes with their type-derived display, all
	 * sortable and text-filterable by display label.
	 */
	private ReactControl buildGenericTable(List<Object> elements) {
		List<ScriptResultRow> rows = new ArrayList<>(elements.size());
		for (int i = 0; i < elements.size(); i++) {
			rows.add(new ScriptResultRow(i, elements.get(i)));
		}

		List<Column<ScriptResultRow, ?>> columns = new ArrayList<>();
		columns.add(textColumn(RESULT_COLUMN, I18NConstants.SCRIPT_RESULT_COLUMN, row -> label(row.value())));
		for (TLStructuredTypePart part : attributeUnion(elements).values()) {
			columns.add(attributeColumn(part));
		}

		ListRowSource<ScriptResultRow> source =
			new ListRowSource<>(rows, columns, row -> Integer.valueOf(row.index()));
		DefaultTableView<ScriptResultRow> view = DefaultTableView.create(columns, source);
		return new TableViewControl<>(_context, view, false);
	}

	/**
	 * The union of the non-hidden attributes across the types of all object elements, in
	 * first-seen order and deduplicated by attribute name.
	 */
	private static Map<String, TLStructuredTypePart> attributeUnion(List<Object> elements) {
		Map<String, TLStructuredTypePart> parts = new LinkedHashMap<>();
		for (Object element : elements) {
			if (isModelObject(element)) {
				for (TLStructuredTypePart part : ((TLObject) element).tType().getAllParts()) {
					if (!DisplayAnnotations.isHidden(part)) {
						parts.putIfAbsent(part.getName(), part);
					}
				}
			}
		}
		return parts;
	}

	/**
	 * The result value flattened into elements: the members of a {@link Collection} or array, the
	 * single value for any other non-{@code null} value, and none for {@code null}.
	 */
	private static List<Object> elements(Object value) {
		List<Object> elements = new ArrayList<>();
		if (value == null) {
			return elements;
		}
		if (value instanceof Collection<?> collection) {
			elements.addAll(collection);
		} else if (value instanceof Object[] array) {
			elements.addAll(Arrays.asList(array));
		} else {
			elements.add(value);
		}
		return elements;
	}

	private static boolean isModelObject(Object value) {
		return value instanceof TLObject object && object.tType() != null;
	}

	/** A sortable, text-filterable column rendering a {@link String} value of a row. */
	private static Column<ScriptResultRow, String> textColumn(String id, ResKey label,
			Function<? super ScriptResultRow, String> value) {
		return DefaultColumn.<ScriptResultRow, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.build();
	}

	/**
	 * A column showing an object attribute with its type-derived display (see
	 * {@link ColumnProviderService#displayContent}), sorted and text-filtered by the value's
	 * display label. The value is empty for elements that are not {@link TLObject}s or whose type
	 * lacks the attribute.
	 */
	private static Column<ScriptResultRow, Object> attributeColumn(TLStructuredTypePart part) {
		String name = part.getName();
		return DefaultColumn
			.<ScriptResultRow, Object> builder(name, row -> ColumnProviderService.attributeValue(row.value(), name))
			.label(TLModelNamingConvention.resourceKey(part))
			.renderer(value -> ColumnProviderService.displayContent(part, value))
			.sort(() -> Comparator.comparing(ColumnProviderService::label))
			.filter(new TextColumnFilter<>(ColumnProviderService::label))
			.build();
	}

	/**
	 * The label of the given value, or the empty string for {@code null}.
	 */
	private static String label(Object value) {
		return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
	}

	@Override
	protected void cleanupChildren() {
		if (_currentTable != null) {
			_currentTable.cleanupTree();
			_currentTable = null;
		}
	}
}
