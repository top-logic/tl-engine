/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.chartjs.ChartElement;
import com.top_logic.layout.react.chartjs.ClickHandlerConfig;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.WriteChannelAction;
import com.top_logic.layout.view.element.CompositionTableElement;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.layout.view.element.FieldElement;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.element.FormLayoutOptions;
import com.top_logic.layout.view.element.GroupElement;
import com.top_logic.layout.view.element.HtmlDisplay;
import com.top_logic.layout.view.element.HtmlElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnsConfig;
import com.top_logic.layout.view.table.ComputedColumn;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.display.CollapsedDisplay;
import com.top_logic.tl.ai.core.tools.present.ChartData;
import com.top_logic.tl.ai.core.tools.present.PresentAs;
import com.top_logic.tl.ai.core.tools.present.Presentation;
import com.top_logic.util.Resources;

/**
 * The pane of a {@code <chat-rail input="…">}: shows whatever stands on the rail's input channel, for
 * any type, and remembers what it showed before.
 *
 * <p>
 * <b>What it shows.</b> One object as its form, several as a table with the columns of their type,
 * and a {@link Presentation} - what the agent's {@code present} tool writes - as the table, chart or
 * form it names. Nothing here knows a type: the form shows the attributes of the object's type, the
 * table the main properties of the rows' common type with the rest offered in its column selection
 * (see {@link TableElement}), and the chart the Chart.js configuration the presentation draws (see
 * {@link PaneCharts}). Data a query computed - a count per group, several series - is a chart over
 * the same configuration, or a table with a column for the labels and one per series. An
 * application no longer wires the pane to its types.
 * </p>
 *
 * <p>
 * <b>What the model says about a form.</b> Two annotations of the model change a form, and nothing
 * else does: a composition annotated {@code <collapsed/>} ({@link CollapsedDisplay}) is shown in a
 * group of its own, closed, and a binary attribute whose value is an HTML document is shown below its
 * field - read in place, in the pane's typography, and still downloadable from the field. A
 * research run is shown this way: its report, then its cited sources, and what it read and did not
 * cite folded away.
 * </p>
 *
 * <p>
 * <b>History.</b> Every value that reaches the channel becomes a step on top of the ones before, and
 * so does a row the user clicks in a table or a bar in a chart, which opens that object's form.
 * {@link #back()} returns to the step below, as it was - its control is kept, not rebuilt, so a
 * table comes back with its data, its sort, its filter and its scroll position - and without asking
 * the agent anything. Closing the pane empties the channel, which empties the history.
 * </p>
 *
 * <p>
 * <b>The channel follows the top step.</b> Going back writes the value of the step below onto the
 * channel, so whatever else on the page reads it - a context fact, a list scoped to "the open
 * object" - sees what the user sees. A value equal to the top step's is not a new step: that is this
 * pane's own write coming back, or a list re-selecting what is already shown.
 * </p>
 *
 * <p>
 * Every step is built from the engine's own elements - {@code <form>}, {@code <table>},
 * {@code <chart>} - with channels of its own, so two steps never share a selection.
 * </p>
 */
final class PresentationPane {

	/** How many steps the history keeps; the oldest goes first. */
	static final int MAX_DEPTH = 20;

	/** The step's own channel holding the rows a table or chart shows. */
	private static final String ROWS = "chatPaneRows";

	/** The step's own channel holding the Chart.js configuration a chart draws. */
	private static final String CHART = "chatPaneChart";

	/** The step's own channel a table row click or a chart click writes its object to. */
	private static final String OPEN = "chatPaneOpen";

	/** The step's own channel holding the object a form shows. */
	private static final String OBJECT = "chatPaneObject";

	/** The prefix of the step's own channels holding an HTML document a form shows. */
	private static final String DOCUMENT = "chatPaneDocument";

	/** The content type of a binary value the pane shows as a document. */
	private static final String HTML_TYPE = "text/html";

	/**
	 * The class of an element of an HTML document that is in the download only, not in the pane.
	 */
	public static final String DOWNLOAD_ONLY = "download-only";

	/** The class of a composition's table in a group that already names the composition. */
	private static final String GROUPED_TABLE = "tl-chat-pane__grouped-table";

	/** The chart handler that opens a clicked point's object. */
	private static final String OPEN_HANDLER = "open";

	/** The key of a data row its position is kept under, so that two equal rows stay two. */
	private static final String ROW_INDEX = "row";

	/** The prefix of the keys a data row holds its cells under, and of its table's column names. */
	private static final String CELL = "c";

	/** The qualified name of the type a column of whole numbers is declared with. */
	private static final String WHOLE_NUMBERS = "tl.core:Long";

	/** The qualified name of the type a column of numbers with a fraction is declared with. */
	private static final String NUMBERS = "tl.core:Double";

	/** The width of a number column whose header is short, in pixels: "Anzahl" with its filter. */
	private static final int MIN_NUMBER_WIDTH = 110;

	/** The widest a number column gets for its header, in pixels; a longer header is elided. */
	private static final int MAX_NUMBER_WIDTH = 140;

	/**
	 * One step of the history: what stood on the channel, and the control showing it.
	 */
	final class Step {

		private final int _id;

		private final Object _value;

		private final PresentAs _kind;

		private final String _title;

		private final ReactControl _control;

		private final ViewChannel _open;

		private final ViewChannel.ChannelListener _openListener;

		private Runnable _refresh = () -> {
			// Nothing to recompute: a form and a table follow their objects by themselves.
		};

		Step(int id, Object value, PresentAs kind, String title, ReactControl control, ViewChannel open) {
			_id = id;
			_value = value;
			_kind = kind;
			_title = title;
			_control = control;
			_open = open;
			if (open == null) {
				_openListener = null;
			} else {
				_openListener = (sender, oldValue, newValue) -> opened(this, newValue);
				open.addListener(_openListener);
			}
		}

		Object value() {
			return _value;
		}

		/**
		 * Recomputes what the step shows from its objects as they are now, for a step the user returns
		 * to.
		 */
		void refresh() {
			_refresh.run();
		}

		void retire() {
			if (_open != null) {
				_open.removeListener(_openListener);
			}
			ContentControls.retire(_control);
		}

		Map<String, Object> state() {
			Map<String, Object> state = new LinkedHashMap<>();
			state.put("id", Integer.valueOf(_id));
			state.put("kind", _kind.name().toLowerCase());
			state.put("title", _title);
			state.put("control", _control);
			return state;
		}
	}

	private final ViewContext _context;

	private final ViewChannel _input;

	private final ReactControl _owner;

	private final Runnable _changed;

	private final List<Step> _history = new ArrayList<>();

	private final ViewChannel.ChannelListener _inputListener;

	private int _nextId;

	/**
	 * Creates a {@link PresentationPane} and shows what already stands on the channel.
	 *
	 * @param context
	 *        The context the steps are built in.
	 * @param input
	 *        The channel whose values the pane shows, and which its close and back write.
	 * @param owner
	 *        The control the steps are children of: a step built while it is attached is attached as
	 *        well.
	 * @param changed
	 *        Called whenever the history changed, so the owner can tell the browser.
	 */
	PresentationPane(ViewContext context, ViewChannel input, ReactControl owner, Runnable changed) {
		_context = context;
		_input = input;
		_owner = owner;
		_changed = changed;
		_inputListener = (sender, oldValue, newValue) -> show(newValue);
		_input.addListener(_inputListener);
		show(_input.get());
	}

	/**
	 * The history, bottom first, as the browser renders it: every step stays mounted and the last
	 * one is shown.
	 */
	List<Map<String, Object>> state() {
		List<Map<String, Object>> result = new ArrayList<>(_history.size());
		for (Step step : _history) {
			result.add(step.state());
		}
		return result;
	}

	/**
	 * Whether the pane shows anything.
	 */
	boolean isOpen() {
		return !_history.isEmpty();
	}

	/**
	 * Returns to the step below the top one, or closes the pane when there is none.
	 */
	void back() {
		if (_history.size() < 2) {
			close();
			return;
		}
		_history.remove(_history.size() - 1).retire();
		Step top = top();
		top.refresh();
		if (top._open != null) {
			// A row the user came from stays selected otherwise, and clicking it again would change
			// nothing - so the step could not be opened a second time.
			top._open.set(null);
		}
		_changed.run();
		// The channel follows the top step; its listener sees the top step's own value and does
		// nothing.
		_input.set(top.value());
	}

	/**
	 * Closes the pane: empties the channel, which empties the history.
	 */
	void close() {
		if (_input.get() == null) {
			clear();
			return;
		}
		_input.set(null);
	}

	/**
	 * Drops every step, at the end of the owner's life.
	 */
	void dispose() {
		_input.removeListener(_inputListener);
		for (Step step : _history) {
			step.retire();
		}
		_history.clear();
	}

	private void show(Object value) {
		if (value == null) {
			clear();
			return;
		}
		Step top = top();
		if (top != null && same(top.value(), value)) {
			return;
		}
		Step step;
		try {
			step = build(value);
		} catch (RuntimeException ex) {
			// The pane must not take the page down with it. What it cannot show it does not show,
			// and the steps before stay where they are.
			Logger.error("The chat pane could not show " + value + ": " + ex.getMessage(), ex,
				PresentationPane.class);
			return;
		}
		if (step == null) {
			return;
		}
		_history.add(step);
		while (_history.size() > MAX_DEPTH) {
			_history.remove(0).retire();
		}
		if (_owner.isAttached()) {
			step._control.attach();
		}
		_changed.run();
	}

	private void clear() {
		if (_history.isEmpty()) {
			return;
		}
		for (Step step : _history) {
			step.retire();
		}
		_history.clear();
		_changed.run();
	}

	/** A row clicked in a table, or a point in a chart: opens that object's form on top. */
	private void opened(Step step, Object value) {
		if (value == null || step != top()) {
			return;
		}
		Object object = value instanceof Collection<?> many && many.size() == 1 ? many.iterator().next() : value;
		if (object instanceof TLObject) {
			_input.set(object);
		}
	}

	private Step top() {
		return _history.isEmpty() ? null : _history.get(_history.size() - 1);
	}

	private static boolean same(Object shown, Object value) {
		return shown == value || Objects.equals(shown, value);
	}

	/** The step showing a channel value, or {@code null} for a value nothing here can show. */
	private Step build(Object value) {
		if (value instanceof Presentation presentation) {
			switch (presentation.as()) {
				case FORM:
					return form(value, presentation.objects().get(0));
				case CHART:
					return chart(value, presentation);
				default:
					return presentation.isData()
						? dataTable(value, presentation)
						: table(value, presentation.objects(), presentation.title());
			}
		}
		if (value instanceof TLObject object) {
			return form(value, object);
		}
		if (value instanceof Collection<?> many) {
			List<TLObject> objects = objects(many);
			if (objects.isEmpty()) {
				return null;
			}
			return objects.size() == 1 ? form(value, objects.get(0)) : table(value, objects, null);
		}
		Logger.info("The chat pane shows model objects and presentations; ignoring " + value + ".",
			PresentationPane.class);
		return null;
	}

	private Step form(Object value, TLObject object) {
		int id = _nextId++;
		ViewChannel shown = new DefaultViewChannel(OBJECT);
		shown.set(object);
		ViewContext context = stepContext(id).withLocalChannel(OBJECT, shown);
		int documents = 0;

		FormElement.Config form = TypedConfiguration.newConfigItem(FormElement.Config.class);
		set(form, FormElement.Config.INPUT, new ChannelRef(OBJECT));
		set(form, FormElement.Config.MODE_SWITCH, Boolean.FALSE);
		set(form, FormLayoutOptions.INSET, Boolean.TRUE);
		List<PolymorphicConfiguration<? extends UIElement>> fields = new ArrayList<>();
		List<PolymorphicConfiguration<? extends UIElement>> compositions = new ArrayList<>();
		for (TLStructuredTypePart part : PaneLayouts.ordered(object.tType())) {
			if (PaneLayouts.isComposition(part)) {
				boolean collapsed = CollapsedDisplay.isCollapsed(part);
				CompositionTableElement.Config table =
					TypedConfiguration.newConfigItem(CompositionTableElement.Config.class);
				if (collapsed) {
					// The group names the composition; the stylesheet hides the table's own title.
					set(table, UIElement.Config.CSS_CLASS, GROUPED_TABLE);
				}
				set(table, CompositionTableElement.Config.ATTRIBUTE, part.getName());
				if (part.getType() instanceof TLStructuredType partType) {
					List<String> columns = PaneLayouts.columns(partType);
					if (!columns.isEmpty()) {
						set(table, CompositionTableElement.Config.COLUMNS, columnsConfig(columns));
					}
				}
				if (collapsed) {
					// Parts the reader opens when they want them: in a group of their own, closed.
					GroupElement.Config group = TypedConfiguration.newConfigItem(GroupElement.Config.class);
					set(group, GroupElement.Config.LABEL, TLModelI18N.getI18NKey(part));
					set(group, GroupElement.Config.COLLAPSIBLE, Boolean.TRUE);
					set(group, GroupElement.Config.COLLAPSED, Boolean.TRUE);
					set(group, GroupElement.Config.CHILDREN, List.of(table));
					compositions.add(group);
				} else {
					compositions.add(table);
				}
			} else {
				FieldElement.Config field = TypedConfiguration.newConfigItem(FieldElement.Config.class);
				set(field, FieldElement.Config.ATTRIBUTE, part.getName());
				fields.add(field);
				String html = htmlDocument(object, part);
				if (html != null) {
					// A document is read, not downloaded first: its content is shown below its
					// field, which keeps the download, in the pane's own typography - so it
					// follows the theme and scrolls with the pane rather than in a frame of its own.
					String channel = DOCUMENT + documents++;
					ViewChannel document = new DefaultViewChannel(channel);
					document.set(html);
					context = context.withLocalChannel(channel, document);
					HtmlElement.Config view = TypedConfiguration.newConfigItem(HtmlElement.Config.class);
					set(view, HtmlElement.Config.INPUT, new ChannelRef(channel));
					set(view, HtmlElement.Config.DISPLAY, HtmlDisplay.INLINE);
					fields.add(view);
				}
			}
		}
		// The fields first, the parts below them: a composition is a table of its own.
		fields.addAll(compositions);
		set(form, FormElement.Config.CHILDREN, fields);

		ReactControl control = control(form, context);
		return new Step(id, value, PresentAs.FORM, MetaLabelProvider.INSTANCE.getLabel(object), control, null);
	}

	/**
	 * The content of a binary attribute's value when it is an HTML document - the inside of its
	 * {@code <body>}, which the pane shows in place; {@code null} for every other attribute and value.
	 *
	 * <p>
	 * Only the body: the document's own styles would apply to the whole page, and the pane gives the
	 * content its own typography. What the content may contain is decided where it is shown, by the
	 * application's HTML safety check.
	 * </p>
	 *
	 * <p>
	 * An element of class {@value #DOWNLOAD_ONLY} is left out: it is in the document for the reader of
	 * the download, and the form already shows what it says as fields - the title and the date of a
	 * report, for example.
	 * </p>
	 */
	static String htmlDocument(TLObject object, TLStructuredTypePart part) {
		Object value = object.tValue(part);
		if (!(value instanceof BinaryDataSource binary)) {
			return null;
		}
		String type = binary.getContentType();
		if (type == null || !type.toLowerCase(Locale.ROOT).startsWith(HTML_TYPE)) {
			return null;
		}
		try (InputStream in = binary.toData().getStream()) {
			return inPlace(new String(in.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException ex) {
			Logger.error("Cannot read the HTML document of " + part.getName() + ".", ex, PresentationPane.class);
			return null;
		}
	}

	/** What the pane shows of an HTML document: its body, without what is for the download only. */
	static String inPlace(String document) {
		Element body = Jsoup.parse(document).body();
		body.select("." + DOWNLOAD_ONLY).remove();
		return body.html();
	}

	private Step table(Object value, List<TLObject> rows, String title) {
		int id = _nextId++;
		ViewChannel rowChannel = new DefaultViewChannel(ROWS);
		rowChannel.set(List.copyOf(rows));
		ViewChannel open = new DefaultViewChannel(OPEN);
		ViewContext context = stepContext(id).withLocalChannel(ROWS, rowChannel).withLocalChannel(OPEN, open);

		TLStructuredType type = commonType(rows);
		TableElement.Config table = TypedConfiguration.newConfigItem(TableElement.Config.class);
		set(table, Inputs.INPUTS, List.of(new ChannelRef(ROWS)));
		set(table, TableElement.Config.ROWS, expr(TableElement.Config.ROWS, "rows -> $rows"));
		set(table, TableElement.Config.SELECTION, new ChannelRef(OPEN));
		if (type != null) {
			set(table, TableElement.Config.TYPES, List.of(TLModelPartRef.ref(type)));
			List<String> chosen = PaneLayouts.chosenColumns(type);
			if (!chosen.isEmpty()) {
				set(table, TableElement.Config.COLUMNS, columnsConfig(chosen));
			}
			// One personalization per type, whichever conversation presented it: the columns a user
			// picked for events are the columns of the next table of events, too.
			set(table, UIElement.Config.PERSONALIZATION_KEY, "chat-pane:" + TLModelUtil.qualifiedName(type));
		}

		ReactControl control = control(table, context);
		return new Step(id, value, PresentAs.TABLE, title(title, type, rows.size()), control, open);
	}

	private Step chart(Object value, Presentation presentation) {
		int id = _nextId++;
		ViewChannel open = new DefaultViewChannel(OPEN);
		ViewChannel spec = new DefaultViewChannel(CHART);
		spec.set(PaneCharts.config(presentation.chartData(), OPEN_HANDLER));
		ViewContext context = stepContext(id).withLocalChannel(CHART, spec).withLocalChannel(OPEN, open);

		WriteChannelAction.Config write = TypedConfiguration.newConfigItem(WriteChannelAction.Config.class);
		set(write, "name", OPEN);
		GenericViewCommand.Config command = TypedConfiguration.newConfigItem(GenericViewCommand.Config.class);
		set(command, "actions", List.of(write));
		ClickHandlerConfig handler = TypedConfiguration.newConfigItem(ClickHandlerConfig.class);
		set(handler, ClickHandlerConfig.NAME, OPEN_HANDLER);
		set(handler, ClickHandlerConfig.ACTION, command);

		ChartElement.Config chart = TypedConfiguration.newConfigItem(ChartElement.Config.class);
		set(chart, Inputs.INPUTS, List.of(new ChannelRef(CHART)));
		set(chart, ChartElement.Config.DATA, expr(ChartElement.Config.DATA, "spec -> $spec"));
		set(chart, ChartElement.Config.HANDLERS, List.of(handler));

		ReactControl control = control(chart, context);
		List<TLObject> rows = presentation.objects();
		String title = presentation.isData()
			? dataTitle(presentation.title())
			: title(presentation.title(), commonType(rows), rows.size());
		Step step = new Step(id, value, PresentAs.CHART, title, control, open);
		// A chart's values are computed, not bound: a chart the user returns to is computed again, so
		// it shows what its objects say now rather than what they said when the agent presented it.
		// Data a query computed is shown as it was computed - there is nothing to compute it from.
		step._refresh = () -> {
			try {
				spec.set(PaneCharts.config(presentation.chartData(), OPEN_HANDLER));
			} catch (RuntimeException ex) {
				Logger.warn("The chat pane could not recompute a chart: " + ex.getMessage(), ex,
					PresentationPane.class);
			}
		};
		return step;
	}

	/**
	 * A table of data a query computed: a column for the labels, and one per series.
	 *
	 * <p>
	 * The rows are no model objects, so the columns are computed ones reading a row's cells - and a
	 * row click opens nothing.
	 * </p>
	 */
	private Step dataTable(Object value, Presentation presentation) {
		int id = _nextId++;
		ChartData data = presentation.chartData();
		List<String> labels = data.labels();
		List<ChartData.Series> series = data.series();
		List<Object> rows = new ArrayList<>(labels.size());
		for (int n = 0; n < labels.size(); n++) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put(ROW_INDEX, Integer.valueOf(n));
			row.put(CELL + 0, labels.get(n));
			for (int s = 0; s < series.size(); s++) {
				List<?> values = series.get(s).values();
				row.put(CELL + (s + 1), n < values.size() ? values.get(n) : null);
			}
			rows.add(row);
		}
		Resources resources = Resources.getInstance();
		List<PolymorphicConfiguration<? extends ColumnDeclaration>> columns = new ArrayList<>();
		columns.add(dataColumn(0, resources.getString(I18NConstants.PANE_COLUMN_LABEL), null));
		for (int s = 0; s < series.size(); s++) {
			String name = series.get(s).name();
			String header = name.isEmpty() ? resources.getString(I18NConstants.PANE_COLUMN_VALUE) : name;
			columns.add(dataColumn(s + 1, header, numberType(series.get(s).values(), rows, s + 1)));
		}
		ColumnsConfig columnsConfig = TypedConfiguration.newConfigItem(ColumnsConfig.class);
		set(columnsConfig, ColumnsConfig.COLUMNS, columns);
		// Only now: the column types above made the cells of each number column one Java type.
		ViewChannel rowChannel = new DefaultViewChannel(ROWS);
		rowChannel.set(rows);
		ViewContext context = stepContext(id).withLocalChannel(ROWS, rowChannel);

		TableElement.Config table = TypedConfiguration.newConfigItem(TableElement.Config.class);
		set(table, Inputs.INPUTS, List.of(new ChannelRef(ROWS)));
		set(table, TableElement.Config.ROWS, expr(TableElement.Config.ROWS, "rows -> $rows"));
		set(table, TableElement.Config.COLUMNS, columnsConfig);

		ReactControl control = control(table, context);
		return new Step(id, value, PresentAs.TABLE, dataTitle(presentation.title()), control, null);
	}

	/** The column showing a data row's cell of the given index. */
	private static ComputedColumn.Config dataColumn(int index, String header, TLModelPartRef type) {
		ComputedColumn.Config column = TypedConfiguration.newConfigItem(ComputedColumn.Config.class);
		set(column, ComputedColumn.Config.NAME, CELL + index);
		set(column, ComputedColumn.Config.LABEL, ResKey.text(header));
		set(column, ComputedColumn.Config.VALUE, expr(ComputedColumn.Config.VALUE, "r -> $r['" + CELL + index + "']"));
		if (type != null) {
			set(column, ComputedColumn.Config.TYPE, type);
			// A number column starts as narrow as its numbers, which cuts even a header such as
			// "Anzahl" short. It is as wide as its header asks for - up to the width at which a label
			// column and two series still fit the pane's default 420 pixels.
			set(column, ComputedColumn.Config.WIDTH,
				Integer.valueOf(Math.min(MAX_NUMBER_WIDTH, Math.max(MIN_NUMBER_WIDTH, 8 * header.length() + 62))));
		}
		return column;
	}

	/**
	 * The type a column of the given numbers is declared with, so that it sorts and aligns as
	 * numbers: whole numbers as such, so that a count does not read "7.00". The row cells are made
	 * one Java type to match, since a column compares its values with each other.
	 */
	private static TLModelPartRef numberType(List<?> values, List<Object> rows, int cell) {
		boolean whole = values.stream()
			.allMatch(v -> !(v instanceof Number number) || number.doubleValue() == Math.rint(number.doubleValue()));
		for (Object row : rows) {
			@SuppressWarnings("unchecked")
			Map<String, Object> cells = (Map<String, Object>) row;
			if (cells.get(CELL + cell) instanceof Number number) {
				cells.put(CELL + cell, whole ? (Object) Long.valueOf(number.longValue())
					: (Object) Double.valueOf(number.doubleValue()));
			}
		}
		return TLModelPartRef.ref(whole ? WHOLE_NUMBERS : NUMBERS);
	}

	private static ColumnsConfig columnsConfig(List<String> attributes) {
		ColumnsConfig columns = TypedConfiguration.newConfigItem(ColumnsConfig.class);
		List<PolymorphicConfiguration<? extends ColumnDeclaration>> declared = new ArrayList<>();
		for (String name : attributes) {
			AttributeColumn.Config column = TypedConfiguration.newConfigItem(AttributeColumn.Config.class);
			set(column, AttributeColumn.Config.ATTRIBUTE, name);
			declared.add(column);
		}
		set(columns, ColumnsConfig.COLUMNS, declared);
		return columns;
	}

	private ViewContext stepContext(int id) {
		return _context.childContext("pane").withChildSlotPath("pane-" + id);
	}

	private static ReactControl control(PolymorphicConfiguration<? extends UIElement> config, ViewContext context) {
		UIElement element = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		return ContentControls.toControl(List.of(element), context);
	}

	/** The title of a table or chart of data: the agent's, otherwise a generic one. */
	private static String dataTitle(String title) {
		if (title != null && !title.isBlank()) {
			return title;
		}
		return Resources.getInstance().getString(I18NConstants.PANE_TITLE_DATA);
	}

	/** The title of a table or chart: the agent's, otherwise the type and the count. */
	private static String title(String title, TLStructuredType type, int count) {
		if (title != null && !title.isBlank()) {
			return title;
		}
		Resources resources = Resources.getInstance();
		if (type == null) {
			return resources.getString(I18NConstants.PANE_TITLE_OBJECTS__COUNT.fill(Integer.valueOf(count)));
		}
		return resources.getString(I18NConstants.PANE_TITLE_TYPE__TYPE_COUNT
			.fill(MetaLabelProvider.INSTANCE.getLabel(type), Integer.valueOf(count)));
	}

	/**
	 * The most specific type every row is an instance of, or {@code null} when they share none
	 * beyond the model's root.
	 */
	static TLStructuredType commonType(List<TLObject> rows) {
		Set<TLClass> types = new LinkedHashSet<>();
		for (TLObject row : rows) {
			if (row.tType() instanceof TLClass type) {
				types.add(type);
			}
		}
		if (types.isEmpty()) {
			return null;
		}
		if (types.size() == 1) {
			return types.iterator().next();
		}
		Set<TLClass> common = TLModelUtil.getCommonGeneralizations(types);
		for (TLClass candidate : common) {
			boolean mostSpecific = common.stream()
				.noneMatch(other -> other != candidate && TLModelUtil.isGeneralization(candidate, other));
			if (mostSpecific && !candidate.getAllParts().isEmpty()) {
				return candidate;
			}
		}
		return null;
	}

	private static List<TLObject> objects(Collection<?> many) {
		List<TLObject> result = new ArrayList<>(many.size());
		for (Object element : many) {
			if (element instanceof TLObject object) {
				result.add(object);
			}
		}
		return result;
	}

	private static Expr expr(String property, String source) {
		try {
			return ExprFormat.INSTANCE.getValue(property, source);
		} catch (ConfigurationException ex) {
			throw new IllegalStateException("The pane's own expression does not parse: " + source, ex);
		}
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}
}
