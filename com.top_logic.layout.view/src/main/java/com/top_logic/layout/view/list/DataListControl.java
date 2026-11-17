/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.state.DataListState;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.list.DataListElement.Config;
import com.top_logic.layout.view.list.DataListElement.LeadingConfig;
import com.top_logic.layout.view.list.DataListElement.LineConfig;
import com.top_logic.layout.view.list.DataListElement.LinesConfig;
import com.top_logic.layout.view.list.DataListElement.RowActions;
import com.top_logic.layout.view.list.DataListElement.TrailingConfig;
import com.top_logic.layout.view.list.DataListElement.TrailingDisplay;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.Resources;

/**
 * React control for {@code <data-list>}.
 *
 * <p>
 * Runs the {@code rows} TL-Script expression, serialises each row as a {@link DataListState.Row}
 * and publishes them to the {@link #REACT_MODULE} component (state {@link DataListState}); re-runs
 * and re-publishes when observed model objects change. A row click arrives as the
 * {@link #CMD_SELECT} command carrying the row id; the control resolves it to the row object and
 * writes it to the two-way {@code selection} channel (and reflects an incoming channel value as the
 * selected id). The channel holds the {@link TLObject}, so a detail view reads the object directly.
 * The optional {@code count} channel receives the number of rows with every serialisation, so a
 * section around the list can show it without asking the browser.
 * </p>
 *
 * <p>
 * Configured header {@code commands} are the engine's command buttons ({@link ReactButtonControl}),
 * rendered as children of the list: each follows its {@link ViewCommandModel}, so it is hidden while
 * the command's rules hide it, disabled with their reason as tooltip while they disable it, and
 * updated whenever the command's state changes - an input channel taking another value, say.
 * </p>
 *
 * <p>
 * Configured {@code row-commands} are offered per row. The row a menu was opened on reaches its
 * commands through a local {@code row-channel} this control registers - the same set-then-execute
 * idiom {@code <context-menu>} uses - so an {@code <executability>} rule sees the row and
 * re-evaluates before the menu is composed. The menu itself is the application's: the {@link #CMD_ROW_MENU}
 * command hands a {@link ContextMenuContribution} to the enclosing frame's {@link ContextMenuOpener},
 * which renders, positions and keyboard-operates it. Where there is no opener - a list outside an
 * {@code <app-shell>} - no trigger is rendered.
 * </p>
 */
public class DataListControl extends ReactControl {

	/** The client component rendering the list. */
	public static final String REACT_MODULE = "TLDataList";

	/** The command selecting a row, see {@link #select(Map)}. */
	public static final String CMD_SELECT = "select";

	/** The command opening the commands of a row, see {@link #rowMenu(Map)}. */
	public static final String CMD_ROW_MENU = "rowMenu";

	/** Argument of {@link #CMD_SELECT} and {@link #CMD_ROW_MENU}: the {@link DataListState.Row#getId() row ID}. */
	public static final String ARG_ID = "id";

	/** Argument of {@link #CMD_ROW_MENU}: the client x coordinate to open the menu at. */
	public static final String ARG_X = "x";

	/** Argument of {@link #CMD_ROW_MENU}: the client y coordinate to open the menu at. */
	public static final String ARG_Y = "y";

	/** The encoded form of the invisible image, which the client renders as nothing. */
	private static final String NO_IMAGE = "none";

	/** The prefix of an image embedded as data URI. */
	private static final String DATA_URI_PREFIX = "data:";

	/** The count a badge shows no badge for. */
	private static final String NO_COUNT = "0";

	/** The {@link Config#getRowChannel() row channel} name used when none is configured. */
	private static final String DEFAULT_ROW_CHANNEL = "row";

	private final QueryExecutor _rowsExecutor;

	private final List<LineSpec> _lineSpecs = new ArrayList<>();

	private final QueryExecutor _groupExecutor;

	private final QueryExecutor _cssClassExecutor;

	/** The name an avatar in front of a row stands for, or {@code null} for none. */
	private final QueryExecutor _leadingNameExecutor;

	/** The picture or icon in front of a row, or {@code null} for none. */
	private final QueryExecutor _leadingImageExecutor;

	/** What stands after the lines of a row, or {@code null} for nothing. */
	private final LineSpec _trailing;

	/** Whether {@link #_trailing} is shown as a badge. */
	private final boolean _trailingBadge;

	private final List<ViewChannel> _inputChannels = new ArrayList<>();

	private final Set<TLStructuredType> _observedTypes;

	private final ViewChannel _selectionChannel;

	private final ViewChannel _countChannel;

	/** The commands of the header buttons, in config order. */
	private final List<ViewCommandModel> _commandModels = new ArrayList<>();

	/** Row commands, in config order; they read their input from {@link #_rowChannel}. */
	private final List<ViewCommandModel> _rowCommandModels = new ArrayList<>();

	/** The local channel holding the row a menu was opened on, or {@code null} without row commands. */
	private final ViewChannel _rowChannel;

	/** What the row commands contribute to the frame's menu, or {@code null} when there are none. */
	private final ContextMenuContribution _rowMenu;

	/** The frame's menu opener, or {@code null} outside an {@code <app-shell>}. */
	private final ContextMenuOpener _menuOpener;

	private final boolean[] _applyingFromChannel = { false };

	/** The row objects of the last serialisation, by their id, for the {@code select} lookup. */
	private Map<String, TLObject> _rowsById = Map.of();

	/** Creates a {@link DataListControl}. */
	public DataListControl(ViewContext context, Config config, List<ViewCommand> commands,
			List<ViewCommand.Config> commandConfigs, List<ViewCommand> rowCommands,
			List<ViewCommand.Config> rowCommandConfigs) {
		super(context, null, REACT_MODULE);

		_rowsExecutor = QueryExecutor.compile(config.getRows());
		_groupExecutor = config.getGroupBy() == null ? null : QueryExecutor.compile(config.getGroupBy());
		_cssClassExecutor = config.getRowCssClass() == null ? null : QueryExecutor.compile(config.getRowCssClass());

		LeadingConfig leading = config.getLeading();
		_leadingNameExecutor = leading == null ? null : compile(leading.getName());
		_leadingImageExecutor = leading == null ? null : compile(leading.getImage());

		TrailingConfig trailing = config.getTrailing();
		if (trailing == null || (trailing.getValue() == null && blank(trailing.getAttribute()))) {
			_trailing = null;
			_trailingBadge = false;
		} else {
			_trailing = new LineSpec(trailing.getAttribute(), compile(trailing.getValue()), null);
			_trailingBadge = trailing.getDisplay() == TrailingDisplay.BADGE;
		}

		LinesConfig linesConfig = config.getLines();
		if (linesConfig != null) {
			for (LineConfig line : linesConfig.getLines()) {
				QueryExecutor value = line.getValue() == null ? null : QueryExecutor.compile(line.getValue());
				String emphasis = line.getEmphasis() == null ? null : line.getEmphasis().getExternalName();
				_lineSpecs.add(new LineSpec(line.getAttribute(), value, emphasis));
			}
		}
		_observedTypes = ObservedTypes.resolve(config.getObservedTypes());

		for (ChannelRef ref : config.getInputs()) {
			_inputChannels.add(context.resolveChannel(ref));
		}

		setCssClass(config.getCssClass());
		putState(DataListState.SEARCH__PROP, Boolean.valueOf(config.getSearch()));
		putState(DataListState.COLLAPSIBLE__PROP, Boolean.valueOf(config.getCollapsible()));
		putState(DataListState.COLLAPSED__PROP, Boolean.valueOf(config.getCollapsed()));
		putState(DataListState.DRAWER_BELOW__PROP, Integer.valueOf(config.getDrawerBelow()));
		putState(DataListState.TITLE__PROP, labelOf(config.getTitle()));
		putState(DataListState.EMPTY_TEXT__PROP, labelOf(config.getEmptyText()));

		// Header buttons are standard view commands (instantiated by the element), each rendered as
		// the engine's command button: it shows and follows the state of its model (label, image,
		// visibility, executability with its reason) and runs the command on a click it is offered for.
		List<ReactButtonControl> buttons = new ArrayList<>();
		for (int i = 0; i < commands.size() && i < commandConfigs.size(); i++) {
			ViewCommandModel model = ViewCommandModel.forCommand(context, commands.get(i), commandConfigs.get(i));
			_commandModels.add(model);

			buttons.add(model.createButton(context));
		}
		putState(DataListState.BUTTONS__PROP, buttons);
		if (!_commandModels.isEmpty()) {
			addAttachListener(() -> _commandModels.forEach(model -> model.attach(context.getModelScope())));
			addDetachListener(() -> _commandModels.forEach(ViewCommandModel::detach));
		}

		// Row commands. The row reaches them through a channel registered here and visible only
		// inside this list: a ViewCommandModel reads its input from a channel and takes none from
		// the caller, so the row is written there just before the menu is composed.
		RowActions rowActions = config.getRowActions() == null ? RowActions.HOVER : config.getRowActions();
		_menuOpener = context.getContextMenuOpener();
		boolean wanted = !rowCommands.isEmpty() && rowActions != RowActions.NONE;
		if (!wanted) {
			_rowChannel = null;
			_rowMenu = null;
		} else if (_menuOpener == null) {
			// No frame to open a menu in. Rendering a trigger that cannot do anything is worse than
			// rendering none, and throwing would take the whole list down over an optional feature.
			Logger.debug("No context-menu opener in this context; <data-list> renders no row actions."
				+ " Place the list inside an <app-shell> to use <row-commands>.", DataListControl.class);
			_rowChannel = null;
			_rowMenu = null;
		} else {
			String rowChannelName = blankToDefault(config.getRowChannel(), DEFAULT_ROW_CHANNEL);
			_rowChannel = new DefaultViewChannel(rowChannelName);
			context.registerChannel(rowChannelName, _rowChannel);
			// A row command names the row channel in its inputs, like a command of a <context-menu>
			// names its target channel.
			_rowCommandModels.addAll(ViewCommands.buildCommandModels(context, rowCommands, rowCommandConfigs));
			// Each command runs in this list's context, which holds the row channel, although the
			// opener dispatches it with the frame's own context.
			_rowMenu = new ContextMenuContribution(row -> _rowChannel.set(row), _rowCommandModels);
			addAttachListener(() -> _rowCommandModels.forEach(model -> model.attach(context.getModelScope())));
			addDetachListener(() -> _rowCommandModels.forEach(ViewCommandModel::detach));
		}
		putState(DataListState.HAS_ROW_MENU__PROP, Boolean.valueOf(_rowMenu != null));
		putState(DataListState.ROW_ACTIONS__PROP, rowActions.getExternalName());

		ChannelRef countRef = config.getCount();
		// Resolved before the first serialisation below, which is what writes the initial count.
		_countChannel = countRef == null ? null : context.resolveChannel(countRef);

		ChannelRef selectionRef = config.getSelection();
		if (selectionRef == null) {
			_selectionChannel = null;
			putState(DataListState.SELECTED_ID__PROP, "");
		} else {
			_selectionChannel = context.resolveChannel(selectionRef);
			ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> applyFromChannel(newValue);
			addCleanupAction(_selectionChannel.addListener(listener)::dispose);
		}

		List<Object> initial = new ArrayList<>(executeRowsQuery(_rowsExecutor, readChannelValues(_inputChannels)));
		serialize(initial);
		if (_selectionChannel != null) {
			applyFromChannel(_selectionChannel.get());
		}

		RowSourceObserver<Object> observer = new RowSourceObserver<>(
			initial,
			args -> new ArrayList<>(executeRowsQuery(_rowsExecutor, args)),
			_observedTypes,
			_inputChannels,
			this::serialize);
		addAttachListener(() -> observer.attach(context.getModelScope()));
		addDetachListener(observer::detach);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Runs before the attach listeners, so the channels are emptied of a deleted object before the
	 * commands begin to follow them again: a command rule evaluated over a deleted object fails, and
	 * the deletion may have happened while this list was not displayed, with nobody to see it.
	 * </p>
	 */
	@Override
	protected void onAttach() {
		super.onAttach();
		releaseDeleted(_rowChannel);
		releaseDeleted(_selectionChannel);
		if (_selectionChannel != null) {
			applyFromChannel(_selectionChannel.get());
		}
	}

	/**
	 * Selects the row with the given id: resolves it to its object and writes it to the selection
	 * channel.
	 *
	 * @param args
	 *        {@code id} is the row id previously pushed in the {@code rows} state; empty clears the
	 *        selection.
	 */
	@ReactCommandHandler(value = CMD_SELECT, params = {
		@ReactParam(name = ARG_ID, type = "string", description = "Row id, or empty to clear.")
	})
	public void select(Map<String, Object> args) {
		if (_applyingFromChannel[0]) {
			return;
		}
		String id = stringArg(args, ARG_ID);
		if (_selectionChannel == null) {
			putState(DataListState.SELECTED_ID__PROP, id);
			return;
		}
		TLObject object = id.isEmpty() ? null : _rowsById.get(id);
		_selectionChannel.set(object);
		putState(DataListState.SELECTED_ID__PROP, object == null ? "" : idOf(object));
	}

	/**
	 * Opens the row menu for one row, at the point the browser reports.
	 *
	 * <p>
	 * The opener publishes the row into the contribution's channel before it reads which commands
	 * are visible, so an {@code <executability>} rule decides on the row the user clicked. A menu
	 * with nothing visible in it does not open.
	 * </p>
	 *
	 * @param args
	 *        {@code id} is the row id previously pushed in the {@code rows} state; {@code x} and
	 *        {@code y} are the client coordinates to open at.
	 */
	@ReactCommandHandler(value = CMD_ROW_MENU, params = {
		@ReactParam(name = ARG_ID, type = "string", description = "Row id the menu belongs to."),
		@ReactParam(name = ARG_X, type = "number", description = "Client x coordinate to open at."),
		@ReactParam(name = ARG_Y, type = "number", description = "Client y coordinate to open at.")
	})
	public void rowMenu(Map<String, Object> args) {
		if (_rowMenu == null || _menuOpener == null) {
			return;
		}
		TLObject row = _rowsById.get(stringArg(args, ARG_ID));
		if (row == null) {
			return;
		}
		// Publish the row and re-evaluate before the opener composes the menu. The opener publishes
		// it too, but a channel that already holds this row notifies nobody - so opening the same
		// row's menu twice would show what its rules decided the first time, however much the row
		// has changed since (a run of it has finished, say).
		_rowChannel.set(row);
		_rowCommandModels.forEach(ViewCommandModel::revalidate);
		_menuOpener.open(intArg(args, ARG_X), intArg(args, ARG_Y),
			List.of(new ContextMenuOpener.Targeted(_rowMenu, row)));
	}

	private void applyFromChannel(Object value) {
		_applyingFromChannel[0] = true;
		try {
			TLObject object = firstObject(value);
			putState(DataListState.SELECTED_ID__PROP, object == null ? "" : idOf(object));
		} finally {
			_applyingFromChannel[0] = false;
		}
	}

	/** Serialises the rows to browser state and re-indexes them by id. */
	private void serialize(Collection<?> rows) {
		Map<String, TLObject> byId = new LinkedHashMap<>();
		List<Map<String, Object>> out = new ArrayList<>();
		for (Object row : rows) {
			if (!(row instanceof TLObject object)) {
				continue;
			}
			String id = idOf(object);
			if (id.isEmpty()) {
				continue;
			}
			byId.put(id, object);
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put(DataListState.Row.ID__PROP, id);
			entry.put(DataListState.Row.LINES__PROP, lines(object));
			if (_groupExecutor != null) {
				entry.put(DataListState.Row.GROUP__PROP, label(_groupExecutor.execute(object)));
			}
			if (_cssClassExecutor != null) {
				entry.put(DataListState.Row.CSS_CLASS__PROP, text(_cssClassExecutor.execute(object)));
			}
			Map<String, Object> leading = leading(object);
			if (leading != null) {
				entry.put(DataListState.Row.LEADING__PROP, leading);
			}
			Map<String, Object> trailing = trailing(object);
			if (trailing != null) {
				entry.put(DataListState.Row.TRAILING__PROP, trailing);
			}
			out.add(entry);
		}
		_rowsById = byId;
		releaseVanishedRow();
		releaseDeleted(_selectionChannel);
		putState(DataListState.ROWS__PROP, out);
		if (_countChannel != null) {
			_countChannel.set(Integer.valueOf(out.size()));
		}
	}

	/**
	 * Empties the {@link #_rowChannel row channel} when the row it holds is no longer one of this
	 * list's rows.
	 *
	 * <p>
	 * The channel holds the row a menu was last opened on, and nothing else writes it. A row that
	 * left the list - deleted by its own "Delete" command, say - would otherwise stay on it, and the
	 * row commands' rules would go on deciding over an object the list no longer shows .
	 * </p>
	 */
	private void releaseVanishedRow() {
		if (_rowChannel == null) {
			return;
		}
		TLObject row = firstObject(_rowChannel.get());
		if (row != null && (!row.tValid() || !row.equals(_rowsById.get(idOf(row))))) {
			_rowChannel.set(null);
		}
	}

	/**
	 * Empties the given channel when it holds a deleted object.
	 *
	 * <p>
	 * A deleted object cannot be asked anything - not even who refers to it - so every expression
	 * over it fails. Only a command that writes the channel after its deletion empties it; a
	 * deletion from elsewhere - another window, another view - leaves the object on the channel.
	 * </p>
	 *
	 * @param channel
	 *        The channel to check; {@code null} where this list has no such channel.
	 */
	private static void releaseDeleted(ViewChannel channel) {
		if (channel == null) {
			return;
		}
		for (TLObject object : ChannelObjectObserver.objects(channel.get())) {
			if (!object.tValid()) {
				channel.set(null);
				return;
			}
		}
	}

	/** An expression's result as a plain token - a CSS class is not a value to format for display. */
	private static String text(Object value) {
		return value == null ? "" : String.valueOf(value).trim();
	}

	/** The browser id of a row object: its stable knowledge-object name. */
	private static String idOf(TLObject object) {
		if (object == null || object.tHandle() == null) {
			return "";
		}
		return object.tHandle().getObjectName().toExternalForm();
	}

	/** The single {@link TLObject} a channel value names, or {@code null}. */
	private static TLObject firstObject(Object value) {
		if (value instanceof TLObject object) {
			return object;
		}
		if (value instanceof Collection<?> many && many.size() == 1) {
			Object only = many.iterator().next();
			return only instanceof TLObject object ? object : null;
		}
		return null;
	}

	/**
	 * What stands in front of the lines of the given row, see {@link DataListState.Leading}, or
	 * {@code null} for nothing.
	 *
	 * <p>
	 * An image of an icon font is an icon, any other image - image data, an image file - the
	 * picture of the avatar, see {@link ReactImages#encode(com.top_logic.layout.react.ReactContext, ThemeImage)}.
	 * </p>
	 */
	private Map<String, Object> leading(TLObject object) {
		String name = _leadingNameExecutor == null ? "" : label(_leadingNameExecutor.execute(object));
		ThemeImage image =
			_leadingImageExecutor == null ? null : ThemeImage.fromValue(_leadingImageExecutor.execute(object));
		String encoded = image == null ? null : ReactImages.encode(getReactContext(), image);
		if (NO_IMAGE.equals(encoded)) {
			encoded = null;
		}
		if (name.isEmpty() && encoded == null) {
			return null;
		}
		Map<String, Object> result = new LinkedHashMap<>();
		if (!name.isEmpty()) {
			result.put(DataListState.Leading.NAME__PROP, name);
		}
		if (encoded != null) {
			if (isIconFont(encoded)) {
				result.put(DataListState.Leading.ICON__PROP, encoded);
			} else {
				result.put(DataListState.Leading.IMAGE__PROP, encoded);
			}
		}
		return result;
	}

	/**
	 * Whether the given encoded image is drawn by an icon font, not loaded as a picture: the client
	 * loads only an encoded form starting with a slash or {@code data:} as picture, see
	 * {@link ReactImages}.
	 */
	private static boolean isIconFont(String encoded) {
		return !(encoded.startsWith("/") || encoded.startsWith(DATA_URI_PREFIX));
	}

	/**
	 * What stands after the lines of the given row, see {@link DataListState.Trailing}, or
	 * {@code null} for nothing: no value, or an empty value or {@code 0} as badge.
	 */
	private Map<String, Object> trailing(TLObject object) {
		if (_trailing == null) {
			return null;
		}
		String text = text(_trailing, object);
		if (text.isEmpty() || (_trailingBadge && NO_COUNT.equals(text))) {
			return null;
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(DataListState.Trailing.TEXT__PROP, text);
		if (_trailingBadge) {
			result.put(DataListState.Trailing.BADGE__PROP, Boolean.TRUE);
		}
		return result;
	}

	/** The text the given line or trailing value shows for the given row. */
	private static String text(LineSpec spec, TLObject object) {
		if (spec.executor() != null) {
			return label(spec.executor().execute(object));
		}
		TLStructuredTypePart part = attribute(object, spec.attribute());
		return attributeLabel(part, part == null ? null : object.tValue(part));
	}

	private static QueryExecutor compile(Expr expr) {
		return expr == null ? null : QueryExecutor.compile(expr);
	}

	private static boolean blank(String value) {
		return value == null || value.isBlank();
	}

	/** The display lines for a row, honouring the configured attributes / values and emphasis. */
	private List<Map<String, Object>> lines(TLObject object) {
		List<Map<String, Object>> lines = new ArrayList<>();
		for (LineSpec spec : _lineSpecs) {
			String text = text(spec, object);
			Map<String, Object> line = new LinkedHashMap<>();
			line.put(DataListState.Line.TEXT__PROP, text);
			if (spec.emphasis() != null) {
				line.put(DataListState.Line.EMPHASIS__PROP, spec.emphasis());
			}
			lines.add(line);
		}
		if (lines.isEmpty()) {
			Map<String, Object> line = new LinkedHashMap<>();
			line.put(DataListState.Line.TEXT__PROP, label(object));
			lines.add(line);
		}
		return lines;
	}

	private static TLStructuredTypePart attribute(TLObject object, String attribute) {
		if (attribute == null || attribute.isEmpty()) {
			return null;
		}
		TLStructuredType type = object.tType();
		return type == null ? null : type.getPart(attribute);
	}

	/**
	 * The display label of the value of an attribute: a point in time in the format the attribute
	 * is displayed in everywhere else (a date with its time of day for a {@code tl.core:DateTime}),
	 * every other value as {@link #label(Object)}.
	 */
	private static String attributeLabel(TLStructuredTypePart part, Object value) {
		if (value instanceof Date date) {
			DateFormat format = FieldControlService.dateFormat(part);
			if (format != null) {
				return format.format(date);
			}
		}
		return label(value);
	}

	/**
	 * A display label for an expression result: a number in the number format of the user (a count
	 * of TL-Script, a floating point number, without a fraction where it has none), dates, objects
	 * and text by their label.
	 */
	private static String label(Object value) {
		if (value instanceof Number number) {
			return HTMLFormatter.getInstance().getNumberFormat().format(number);
		}
		return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
	}

	private static String labelOf(ResKey key) {
		return key == null ? "" : Resources.getInstance().getString(key);
	}

	private static Object[] readChannelValues(List<ViewChannel> channels) {
		Object[] values = new Object[channels.size()];
		for (int n = 0; n < channels.size(); n++) {
			values[n] = channels.get(n).get();
		}
		return values;
	}

	private static Collection<?> executeRowsQuery(QueryExecutor rowsExecutor, Object[] channelValues) {
		Object result = rowsExecutor.execute(channelValues);
		if (result instanceof Collection<?> collection) {
			return collection;
		}
		return result == null ? Collections.emptyList() : Collections.singletonList(result);
	}

	private static String stringArg(Map<String, Object> args, String key) {
		if (args == null) {
			return "";
		}
		Object value = args.get(key);
		return value == null ? "" : String.valueOf(value).trim();
	}

	private static int intArg(Map<String, Object> args, String key) {
		Object value = args == null ? null : args.get(key);
		if (value instanceof Number number) {
			return number.intValue();
		}
		try {
			return value == null ? -1 : Integer.parseInt(String.valueOf(value).trim());
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	private static String blankToDefault(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? fallback : trimmed;
	}

	/** One configured display line: an attribute or a computed value, with an optional emphasis. */
	private record LineSpec(String attribute, QueryExecutor executor, String emphasis) {
		// A compact holder for a resolved <line> configuration.
	}
}
