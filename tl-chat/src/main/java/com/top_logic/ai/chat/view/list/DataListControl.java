/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.ai.chat.view.list.DataListElement.Config;
import com.top_logic.ai.chat.view.list.DataListElement.LineConfig;
import com.top_logic.ai.chat.view.list.DataListElement.LinesConfig;
import com.top_logic.ai.chat.view.list.DataListElement.RowActions;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.button.KeyStroke;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewExecutabilityRules;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * React control for {@code <data-list>}.
 *
 * <p>
 * Runs the {@code rows} TL-Script expression, serialises each row to
 * {@code {id, lines[], group?, cssClass?}} and {@code putState}s the array to the {@code TLDataList}
 * React component; re-runs and re-pushes when observed model objects change. A row click arrives as
 * the {@code select} command carrying the row id; the control resolves it to the row object and
 * writes it to the two-way {@code selection} channel (and reflects an incoming channel value as the
 * selected id). The channel holds the {@link TLObject} so a consumer like {@code <chat-conversation>}
 * reads the object directly. The optional {@code count} channel receives the number of rows with
 * every serialisation, so a rail section can show it without asking the browser.
 * </p>
 *
 * <p>
 * Configured {@code row-commands} are offered per row. The row a menu was opened on reaches its
 * commands through a local {@code row-channel} this control registers - the same set-then-execute
 * idiom {@code <context-menu>} uses - so an {@code <executability>} rule sees the row and
 * re-evaluates before the menu is composed. The menu itself is the application's: the {@code rowMenu}
 * command hands a {@link ContextMenuContribution} to the enclosing frame's {@link ContextMenuOpener},
 * which renders, positions and keyboard-operates it. Where there is no opener - a list outside an
 * {@code <app-shell>} - no trigger is rendered.
 * </p>
 */
public class DataListControl extends ReactControl {

	private final QueryExecutor _rowsExecutor;

	private final List<LineSpec> _lineSpecs = new ArrayList<>();

	private final QueryExecutor _groupExecutor;

	private final QueryExecutor _cssClassExecutor;

	private final List<ViewChannel> _inputChannels = new ArrayList<>();

	private final Set<TLStructuredType> _observedTypes;

	private final ViewChannel _selectionChannel;

	private final ViewChannel _countChannel;

	/** Header command buttons, in config order, parallel to the {@code buttons} state array. */
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
		super(context, null, blankToDefault(config.getReactElement(), "TLDataList"));

		_rowsExecutor = QueryExecutor.compile(config.getRows());
		_groupExecutor = config.getGroupBy() == null ? null : QueryExecutor.compile(config.getGroupBy());
		_cssClassExecutor = config.getRowCssClass() == null ? null : QueryExecutor.compile(config.getRowCssClass());

		LinesConfig linesConfig = config.getLines();
		if (linesConfig != null) {
			for (LineConfig line : linesConfig.getLines()) {
				QueryExecutor value = line.getValue() == null ? null : QueryExecutor.compile(line.getValue());
				String emphasis = line.getEmphasis() == null ? null : line.getEmphasis().getExternalName();
				_lineSpecs.add(new LineSpec(line.getAttribute(), value, emphasis));
			}
		}
		_observedTypes = resolveObservedTypes(config.getObservedTypes());

		for (ChannelRef ref : config.getInputs()) {
			_inputChannels.add(context.resolveChannel(ref));
		}

		setCssClass(config.getCssClass());
		putState("search", Boolean.valueOf(config.getSearch()));
		putState("collapsible", Boolean.valueOf(config.getCollapsible()));
		putState("collapsed", Boolean.valueOf(config.getCollapsed()));
		putState("drawerBelow", Integer.valueOf(config.getDrawerBelow()));
		putState("title", labelOf(config.getTitle()));
		putState("emptyText", labelOf(config.getEmptyText()));
		putState("language", currentLanguage());

		// Header buttons are standard view commands (instantiated by the element): build a model per
		// command (resolving input and executability) and push its label + icon for the list to render.
		// Clicks run the command via executeCommand.
		List<Map<String, Object>> buttons = new ArrayList<>();
		for (int i = 0; i < commands.size() && i < commandConfigs.size(); i++) {
			ViewCommand.Config cmdConfig = commandConfigs.get(i);
			ViewCommandModel model = ViewCommandModel.forCommand(context, commands.get(i), cmdConfig);
			_commandModels.add(model);

			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("label", model.getLabel() == null ? "" : model.getLabel());
			ThemeImage image = cmdConfig.getImage();
			entry.put("icon", image == null ? "" : image.toEncodedForm());
			buttons.add(entry);
		}
		putState("buttons", buttons);
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
			String rowChannelName = blankToDefault(config.getRowChannel(), "row");
			_rowChannel = new DefaultViewChannel(rowChannelName);
			context.registerChannel(rowChannelName, _rowChannel);
			for (int i = 0; i < rowCommands.size() && i < rowCommandConfigs.size(); i++) {
				ViewCommand.Config cmdConfig = rowCommandConfigs.get(i);
				ViewChannel commandInput =
					cmdConfig.getInput() == null ? _rowChannel : context.resolveChannel(cmdConfig.getInput());
				ViewExecutabilityRule rule = ViewExecutabilityRules.build(cmdConfig.getExecutability(), context);
				_rowCommandModels.add(
					ViewCommandModel.create(context, rowCommands.get(i), cmdConfig, commandInput, rule));
			}
			// Wrapped, so each command runs in *this* list's context. The opener dispatches with the
			// frame's own ReactContext, which is not a ViewContext and does not hold this list's row
			// channel - under it an <execute-script> with <inputs> throws, a <write-channel> silently
			// does nothing, and the row channel would be unreachable. The context a command was
			// configured in is the one it belongs to.
			List<CommandModel> menuCommands = new ArrayList<>();
			for (ViewCommandModel model : _rowCommandModels) {
				menuCommands.add(new InListContext(model, context));
			}
			_rowMenu = new ContextMenuContribution(row -> _rowChannel.set(row), menuCommands);
			addAttachListener(() -> _rowCommandModels.forEach(model -> model.attach(context.getModelScope())));
			addDetachListener(() -> _rowCommandModels.forEach(ViewCommandModel::detach));
		}
		putState("hasRowMenu", Boolean.valueOf(_rowMenu != null));
		putState("rowActions", rowActions.getExternalName());

		ChannelRef countRef = config.getCount();
		// Resolved before the first serialisation below, which is what writes the initial count.
		_countChannel = countRef == null ? null : context.resolveChannel(countRef);

		ChannelRef selectionRef = config.getSelection();
		if (selectionRef == null) {
			_selectionChannel = null;
			putState("selectedId", "");
		} else {
			_selectionChannel = context.resolveChannel(selectionRef);
			ViewChannel.ChannelListener listener = (sender, oldValue, newValue) -> applyFromChannel(newValue);
			_selectionChannel.addListener(listener);
			addCleanupAction(() -> _selectionChannel.removeListener(listener));
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

	@Override
	protected void onAttach() {
		super.onAttach();
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
	@ReactCommandHandler(value = "select", params = {
		@ReactParam(name = "id", type = "string", description = "Row id, or empty to clear.")
	})
	public void select(Map<String, Object> args) {
		if (_applyingFromChannel[0]) {
			return;
		}
		String id = stringArg(args, "id");
		if (_selectionChannel == null) {
			putState("selectedId", id);
			return;
		}
		TLObject object = id.isEmpty() ? null : _rowsById.get(id);
		_selectionChannel.set(object);
		putState("selectedId", object == null ? "" : idOf(object));
	}

	/**
	 * Runs a configured header command.
	 *
	 * @param args
	 *        {@code index} is the command's position in the configured list.
	 */
	@ReactCommandHandler(value = "button", params = {
		@ReactParam(name = "index", type = "number", description = "Index of the clicked header button.")
	})
	public void button(Map<String, Object> args) {
		int index = intArg(args, "index");
		if (index < 0 || index >= _commandModels.size()) {
			return;
		}
		_commandModels.get(index).executeCommand(getReactContext());
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
	@ReactCommandHandler(value = "rowMenu", params = {
		@ReactParam(name = "id", type = "string", description = "Row id the menu belongs to."),
		@ReactParam(name = "x", type = "number", description = "Client x coordinate to open at."),
		@ReactParam(name = "y", type = "number", description = "Client y coordinate to open at.")
	})
	public void rowMenu(Map<String, Object> args) {
		if (_rowMenu == null || _menuOpener == null) {
			return;
		}
		TLObject row = _rowsById.get(stringArg(args, "id"));
		if (row == null) {
			return;
		}
		// Publish the row and re-evaluate before the opener composes the menu. The opener publishes
		// it too, but a channel that already holds this row notifies nobody - so opening the same
		// row's menu twice would show what its rules decided the first time, however much the row
		// has changed since (a run of it has finished, say).
		_rowChannel.set(row);
		_rowCommandModels.forEach(ViewCommandModel::revalidate);
		_menuOpener.open(intArg(args, "x"), intArg(args, "y"),
			List.of(new ContextMenuOpener.Targeted(_rowMenu, row)));
	}

	private void applyFromChannel(Object value) {
		_applyingFromChannel[0] = true;
		try {
			TLObject object = firstObject(value);
			putState("selectedId", object == null ? "" : idOf(object));
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
			entry.put("id", id);
			entry.put("lines", lines(object));
			if (_groupExecutor != null) {
				entry.put("group", label(_groupExecutor.execute(object)));
			}
			if (_cssClassExecutor != null) {
				entry.put("cssClass", text(_cssClassExecutor.execute(object)));
			}
			out.add(entry);
		}
		_rowsById = byId;
		putState("rows", out);
		if (_countChannel != null) {
			_countChannel.set(Integer.valueOf(out.size()));
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

	/** The display lines for a row, honouring the configured attributes / values and emphasis. */
	private List<Map<String, Object>> lines(TLObject object) {
		List<Map<String, Object>> lines = new ArrayList<>();
		for (LineSpec spec : _lineSpecs) {
			Object value = spec.executor() != null
				? spec.executor().execute(object)
				: attributeValue(object, spec.attribute());
			Map<String, Object> line = new LinkedHashMap<>();
			line.put("text", label(value));
			if (spec.emphasis() != null) {
				line.put("emphasis", spec.emphasis());
			}
			lines.add(line);
		}
		if (lines.isEmpty()) {
			Map<String, Object> line = new LinkedHashMap<>();
			line.put("text", label(object));
			lines.add(line);
		}
		return lines;
	}

	private static Object attributeValue(TLObject object, String attribute) {
		if (attribute == null || attribute.isEmpty()) {
			return null;
		}
		TLStructuredType type = object.tType();
		TLStructuredTypePart part = type == null ? null : type.getPart(attribute);
		return part == null ? null : object.tValue(part);
	}

	/** A display label for an expression result (formats dates, objects and text uniformly). */
	private static String label(Object value) {
		return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
	}

	private static String labelOf(ResKey key) {
		return key == null ? "" : Resources.getInstance().getString(key);
	}

	private static String currentLanguage() {
		TLContext tlContext = TLContext.getContext();
		Locale locale = tlContext == null ? null : tlContext.getCurrentLocale();
		return locale == null ? Locale.ENGLISH.getLanguage() : locale.getLanguage();
	}

	private static Set<TLStructuredType> resolveObservedTypes(List<TLModelPartRef> refs) {
		if (refs == null || refs.isEmpty()) {
			return Set.of();
		}
		Set<TLStructuredType> types = new HashSet<>();
		for (TLModelPartRef ref : refs) {
			TLStructuredType type = (TLStructuredType) ref.resolveType();
			if (type == null) {
				throw new RuntimeException("Failed to resolve observed type: " + ref.qualifiedName());
			}
			types.add(type);
		}
		return types;
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

	/**
	 * A row command as the menu sees it: everything the wrapped model says, but executed in the
	 * list's context instead of the one the menu offers.
	 *
	 * @param model
	 *        The configured command.
	 * @param context
	 *        The list's own context, which holds its row channel.
	 */
	private record InListContext(ViewCommandModel model, ViewContext context) implements CommandModel {

		@Override
		public HandlerResult executeCommand(ReactContext ignored) {
			return model.executeCommand(context);
		}

		@Override
		public HandlerResult perform(ReactContext ignored) {
			return model.perform(context);
		}

		@Override
		public ExecutableState getExecutableState() {
			return model.getExecutableState();
		}

		@Override
		public boolean isActive() {
			return model.isActive();
		}

		@Override
		public String getCssClasses() {
			return model.getCssClasses();
		}

		@Override
		public String getName() {
			return model.getName();
		}

		@Override
		public String getLabel() {
			return model.getLabel();
		}

		@Override
		public ThemeImage getImage() {
			return model.getImage();
		}

		@Override
		public String getTooltip() {
			return model.getTooltip();
		}

		@Override
		public boolean isExecutable() {
			return model.isExecutable();
		}

		@Override
		public boolean isVisible() {
			return model.isVisible();
		}

		@Override
		public CommandPlacement getPlacement() {
			return model.getPlacement();
		}

		@Override
		public String getClique() {
			return model.getClique();
		}

		@Override
		public ButtonDisplayMode getDisplayMode() {
			return model.getDisplayMode();
		}

		@Override
		public KeyStroke getKeyGesture() {
			return model.getKeyGesture();
		}

		@Override
		public void addStateChangeListener(Runnable listener) {
			model.addStateChangeListener(listener);
		}

		@Override
		public void removeStateChangeListener(Runnable listener) {
			model.removeStateChangeListener(listener);
		}
	}
}
