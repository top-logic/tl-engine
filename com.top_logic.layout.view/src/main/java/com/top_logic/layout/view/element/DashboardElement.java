/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.listener.Registration;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.Tile;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopeDefinition;
import com.top_logic.layout.view.command.ActionScopes;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.list.InputValues;
import com.top_logic.layout.view.security.AccessCheck;
import com.top_logic.layout.view.security.CommandAccess;
import com.top_logic.layout.view.security.UnitHiding;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.util.TLContext;

/**
 * A UI element that renders a responsive dashboard grid of tiles.
 *
 * <p>
 * The tiles are contributed by the {@link Config#getEntries() entries} of the dashboard: a
 * {@link TileElement &lt;tile&gt;} contributes one fixed tile, a {@link TileListElement
 * &lt;tiles&gt;} one tile per object of a computed list. All tiles share one order, and the tiles of
 * a {@code <tiles>} entry follow its objects while the dashboard is displayed.
 * </p>
 *
 * <p>
 * Tile order is personalized per user via {@link PersonalConfiguration}, keyed
 * by {@link Config#getId() this element's id}. Unknown ids in the persisted
 * order are ignored; tiles not mentioned are appended in their configured
 * order.
 * </p>
 *
 * <p>
 * Which tiles are shown is personalized as well. A user hides a tile while editing the layout and
 * adds it again through a menu of the surrounding toolbar that offers one command per hidden tile,
 * named after the tile. Only the tiles a user shows or hides deviating from their default are stored: a
 * tile is shown by default unless it is {@link TileElement.Config#isOptional() optional}. A tile
 * added to the configuration therefore appears for everyone who has not hidden it. A tile the user
 * may not access is neither shown nor offered.
 * </p>
 *
 * <p>
 * With {@link Config#getPersonalizeBy() personalize-by}, a user has an arrangement of his own per
 * displayed object. With an {@link Config#getArrangement() arrangement} and a
 * {@link Config#getStoreArrangement() store function}, the application stores the arrangement
 * instead, e.g. at the displayed object for all users; the
 * {@link Config#getArrangementAccess() arrangement access} decides who may change it, and a user
 * who may not is offered no editing of the layout. When an {@link Config#getInputs() input}
 * changes, the dashboard reads its arrangement anew.
 * </p>
 *
 * <p>
 * An anonymous session is offered neither the layout edit commands nor tile
 * reordering nor hiding: all anonymous visitors share one account, so there is no personal
 * layout to store for any of them.
 * </p>
 *
 * @implNote The commands adding a tile again form the clique
 *           {@link com.top_logic.layout.view.command.CommandCliques#DASHBOARD_TILES}, which the
 *           {@link com.top_logic.layout.view.command.CommandCliqueService} displays as menu.
 */
@InApp
public class DashboardElement implements UIElement {

	/**
	 * Prefix of the {@link PersonalConfiguration} key, followed by the dashboard id, holding the
	 * ids of all tiles in the order the user arranged them.
	 */
	private static final String PC_KEY_PREFIX = "dashboard.tileOrder.";

	/**
	 * Prefix of the {@link PersonalConfiguration} key, followed by the dashboard id, holding a JSON
	 * object that maps the id of a tile to whether it is shown.
	 *
	 * <p>
	 * Only tiles shown or hidden deviating from their default are mentioned.
	 * </p>
	 */
	public static final String PC_VISIBILITY_KEY_PREFIX = "dashboard.tileVisibility.";

	/**
	 * Separator between the dashboard id and the addition computed by
	 * {@link Config#getPersonalizeBy()} in a {@link PersonalConfiguration} key.
	 */
	public static final String PC_KEY_ADDITION_SEPARATOR = "#";

	/**
	 * Configuration for {@link DashboardElement}.
	 */
	@TagName("dashboard")
	@TypeIcon("css:bi bi-grid-1x2")
	public interface Config extends UIElement.Config, Inputs, ActionScopeDefinition {

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Config property name for {@link #getMinColWidth()}. */
		String MIN_COL_WIDTH = "min-col-width";

		/** Config property name for {@link #getRowHeight()}. */
		String ROW_HEIGHT = "row-height";

		/** Config property name for {@link #getEntries()}. */
		String ENTRIES = "entries";

		/** Config property name for {@link #getArrangement()}. */
		String ARRANGEMENT = "arrangement";

		/** Config property name for {@link #getStoreArrangement()}. */
		String STORE_ARRANGEMENT = "store-arrangement";

		/** Config property name for {@link #getArrangementAccess()}. */
		String ARRANGEMENT_ACCESS = "arrangement-access";

		/** Config property name for {@link #getPersonalizeBy()}. */
		String PERSONALIZE_BY = "personalize-by";

		/**
		 * Key of the tile ids in their order in the map the {@link #getArrangement() arrangement}
		 * yields, and name of the variable holding them in the {@link #getStoreArrangement() store
		 * function}.
		 */
		String ORDER = "order";

		/**
		 * Key of the ids of the hidden tiles in the map the {@link #getArrangement() arrangement}
		 * yields, and name of the variable holding them in the {@link #getStoreArrangement() store
		 * function}.
		 */
		String HIDDEN = "hidden";

		@Override
		@ClassDefault(DashboardElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Stable id used as persistence key for personal tile ordering.
		 */
		@Override
		@Name(ID)
		@Mandatory
		String getId();

		/**
		 * CSS length hint used by the client to decide the column count.
		 * Defaults to {@code 16rem}.
		 */
		@Name(MIN_COL_WIDTH)
		@StringDefault("16rem")
		String getMinColWidth();

		/**
		 * The height of one grid row, as a CSS length.
		 *
		 * <p>
		 * A tile is as many rows tall as the row span of its
		 * {@link TileElement}, plus the gaps between those rows. Content that
		 * fills the available height, a panel configured to fill or a table,
		 * resolves its height against the tile and scrolls inside it; content
		 * that does not fill and is taller than the tile scrolls inside the
		 * tile as well.
		 * </p>
		 */
		@Name(ROW_HEIGHT)
		@StringDefault("16rem")
		String getRowHeight();

		/**
		 * The entries contributing the tiles: fixed {@code <tile>}s and {@code <tiles>} computed
		 * from a list of objects.
		 *
		 * <p>
		 * Without an arrangement of the user, the tiles appear in the order of their entries, the
		 * tiles of a {@code <tiles>} entry in the order of its objects.
		 * </p>
		 *
		 * @implNote Written directly as children of the {@code <dashboard>}. Keyed by
		 *           {@link TileDeclaration.Config#ID} so that a same-path overlay can address an
		 *           entry.
		 */
		@Name(ENTRIES)
		@Subtypes({
			@Subtype(tag = TileElement.Config.TAG_NAME, type = TileElement.Config.class),
			@Subtype(tag = TileListElement.Config.TAG_NAME, type = TileListElement.Config.class) })
		@com.top_logic.basic.config.annotation.Key(TileDeclaration.Config.ID)
		@com.top_logic.basic.config.annotation.DefaultContainer
		@TreeProperty
		List<TileDeclaration.Config> getEntries();

		/**
		 * TL-Script expression yielding the stored arrangement of the tiles, instead of the
		 * arrangement each user stores in the personal configuration.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names. It yields a map with the ids of the tiles in
		 * their order under {@value #ORDER} and the ids of the hidden tiles under {@value #HIDDEN},
		 * or nothing while no arrangement is stored:
		 * </p>
		 *
		 * <pre>
		 * if($project == null, null, {
		 *   "order": $project.get(`my.module:Project#tileOrder`).split(","),
		 *   "hidden": $project.get(`my.module:Project#hiddenTiles`).split(",")
		 * })
		 * </pre>
		 *
		 * <p>
		 * Stored ids without a tile are skipped; tiles without a stored position follow the stored
		 * ones, in the order of their declaration. A tile that is mentioned in neither list has its
		 * default visibility. So an arrangement can be kept at the displayed object, the same for
		 * every user. The dashboard reads it again when an input changes.
		 * </p>
		 *
		 * <p>
		 * The arrangement can be changed only with a {@link #getStoreArrangement() store function}
		 * and by a user the {@link #getArrangementAccess() arrangement access} allows it.
		 * </p>
		 */
		@Name(ARRANGEMENT)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getArrangement();

		/**
		 * TL-Script function storing a changed arrangement, the counterpart of the
		 * {@link #getArrangement() arrangement}.
		 *
		 * <p>
		 * Runs in a transaction after the user rearranged or hid a tile. It sees the values of the
		 * {@link #getInputs() inputs}, the ids of all tiles in their order as {@value #ORDER} and the
		 * ids of the hidden tiles as {@value #HIDDEN}:
		 * </p>
		 *
		 * <pre>
		 * {
		 *   $project.set(`my.module:Project#tileOrder`, $order.join(","));
		 *   $project.set(`my.module:Project#hiddenTiles`, $hidden.join(","));
		 * }
		 * </pre>
		 */
		@Name(STORE_ARRANGEMENT)
		@Supplies(ORDER)
		@Supplies(HIDDEN)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getStoreArrangement();

		/**
		 * What storing the {@link #getArrangement() arrangement} does in the model, checked in the
		 * model access rights of the current user.
		 *
		 * <p>
		 * The same checks a command declares in its {@link ViewCommand.Config#getAccess() access}:
		 * {@code <operation>} and {@code <create>}. Their scripts see the values of the
		 * {@link #getInputs() inputs}, the first input being the checked object where a check names
		 * none. A user the checks refuse is offered no editing of the layout. Without a check,
		 * storing is not restricted here.
		 * </p>
		 *
		 * <pre>
		 * &lt;arrangement-access&gt;
		 *   &lt;operation name="Write" object="$project" type="my.module:Project"/&gt;
		 * &lt;/arrangement-access&gt;
		 * </pre>
		 */
		@Name(ARRANGEMENT_ACCESS)
		List<PolymorphicConfiguration<? extends AccessCheck>> getArrangementAccess();

		/**
		 * TL-Script expression computing an addition to the key the arrangement of a user is stored
		 * under in the personal configuration, so that the dashboard has an arrangement of its own
		 * per displayed object.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs}. A persistent object is
		 * added by its id, any other value by its text; without a value, the arrangement is stored
		 * under the key of the dashboard alone. Not used with an {@link #getArrangement()
		 * arrangement} stored by the application.
		 * </p>
		 */
		@Name(PERSONALIZE_BY)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getPersonalizeBy();

		/**
		 * The checks of the {@link #getArrangementAccess() arrangement access} see the values of the
		 * inputs.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			if (ActionScopes.contains(getArrangementAccess(), part)) {
				return accessScope(this);
			}
			return outer;
		}
	}

	/**
	 * The arguments the {@link Config#getArrangementAccess() arrangement access} decides over: the
	 * values of the inputs, the first one being the default argument.
	 */
	static ActionScope accessScope(Config config) {
		List<String> names = InputRef.names(config.getInputs());
		return ActionScope.of(names, names.isEmpty() ? null : names.get(0));
	}

	private final String _id;

	private final String _minColWidth;

	private final String _rowHeight;

	private final List<TileDeclaration> _declarations;

	private final String _cssClass;

	private final Config _config;

	/** The {@link Config#getArrangement() arrangement}, {@code null} for the personal one. */
	private final QueryExecutor _arrangement;

	/** The {@link Config#getStoreArrangement() store function}, {@code null} for none. */
	private final QueryExecutor _storeArrangement;

	/** The {@link Config#getPersonalizeBy() key addition}, {@code null} for none. */
	private final QueryExecutor _personalizeBy;

	/**
	 * Creates a new {@link DashboardElement} from configuration.
	 */
	@CalledByReflection
	public DashboardElement(InstantiationContext context, Config config) {
		_config = config;
		_id = config.getId();
		_minColWidth = config.getMinColWidth();
		_rowHeight = config.getRowHeight();
		_declarations = new ArrayList<>();
		for (TileDeclaration.Config entry : config.getEntries()) {
			TileDeclaration declaration = (TileDeclaration) context.getInstance(entry);
			if (declaration != null) {
				_declarations.add(declaration);
			}
		}
		_cssClass = config.getCssClass();
		_arrangement = InputScript.compile(config, Config.ARRANGEMENT);
		_storeArrangement = InputScript.compile(config, Config.STORE_ARRANGEMENT);
		_personalizeBy = InputScript.compile(config, Config.PERSONALIZE_BY);
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(List.<UIElement> copyOf(_declarations)));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new Display(context).control();
	}

	/**
	 * One display of the dashboard: its control, the tiles of its entries and their arrangement.
	 */
	private final class Display implements DashboardTiles.Host {

		/**
		 * Whether the arrangement may ever be changed in this display.
		 *
		 * <p>
		 * An arrangement stored by the application can be changed with a store function only. The
		 * shared anonymous account has no personal layout of its own, so an anonymous session gets a
		 * dashboard with a personal arrangement that offers no rearranging and no hiding at all.
		 * </p>
		 */
		private final boolean _changeable;

		private final List<ViewChannel> _inputs;

		/** Who may store the arrangement, see {@link Config#getArrangementAccess()}. */
		private final ViewExecutabilityRule _access;

		private final List<String> _inputNames;

		private final List<DashboardTiles> _sources = new ArrayList<>();

		/** The values of the inputs the arrangement was read for. */
		private Object[] _inputValues;

		/** The addition to the personal configuration keys, {@code null} for none. */
		private String _personalKey;

		/** The stored order of the tile ids, empty while no order is stored. */
		private List<String> _order = List.of();

		/** The stored visibilities of tiles, tile id to whether it is shown. */
		private Map<String, Boolean> _visibility = new HashMap<>();

		/** The current tiles by id, in the order of their declaration. */
		private Map<String, DashboardTiles.Entry> _entries;

		private final ReactDashboardControl _control;

		/** The command scope the edit commands are offered in, {@code null} for none. */
		private final CommandScope _scope;

		/** The commands switching the edit mode on and off. */
		private final List<DashboardCommandModel> _editCommands = new ArrayList<>();

		/** The commands adding a hidden tile again, by tile id. */
		private final Map<String, DashboardCommandModel> _tileCommands = new LinkedHashMap<>();

		/** Whether the current user may change the arrangement for the current inputs. */
		private boolean _editable;

		/** Whether the edit commands are currently offered in the {@link #_scope}. */
		private boolean _offered;

		/** Ends the observation of the inputs and of the access while the dashboard is displayed. */
		private final List<Runnable> _observations = new ArrayList<>();

		Display(ViewContext context) {
			_changeable = _arrangement != null ? _storeArrangement != null : !TLContext.isAnonymous();
			_inputs = ChannelInputs.resolve(context, _config.getInputs());
			_inputNames = InputRef.names(_config.getInputs());
			_access = CommandAccess.create(_config.getArrangementAccess(), accessScope(_config), context,
				"the arrangement access of the dashboard at " + _config.location());
			for (TileDeclaration declaration : _declarations) {
				_sources.add(declaration.createTiles(context, this));
			}
			_entries = collectEntries();
			loadArrangement();

			_control = new ReactDashboardControl(context, _minColWidth, _rowHeight, arrangeTiles(),
				_changeable ? this::handleReorder : null,
				_changeable ? this::handleVisibilityChange : null);
			_control.setCssClass(_cssClass);

			for (DashboardTiles.Entry entry : _entries.values()) {
				UnitHiding hiding = entry.hiding();
				if (hiding != null) {
					String id = entry.id();
					hiding.bind(_control, hidden -> _control.setTileWithheld(id, hidden));
				}
			}

			_scope = _changeable ? context.getScope(CommandScope.class) : null;
			if (_scope != null) {
				_editCommands.add(DashboardCommandModel.editCommand(_control));
				_editCommands.add(DashboardCommandModel.doneCommand(_control));
				updateTileCommands();
			}
			_editable = mayStore();
			_control.setRearrangeable(_editable);

			_control.addAttachListener(() -> attach(context));
			_control.addDetachListener(this::detach);
			_control.addCleanupAction(() -> {
				for (DashboardTiles source : _sources) {
					source.dispose();
				}
			});
		}

		ReactDashboardControl control() {
			return _control;
		}

		private void attach(ViewContext context) {
			for (DashboardTiles source : _sources) {
				source.attach(context.getModelScope());
			}
			Registration inputs = ChannelInputs.observe(_inputs, this::handleInputsChanged);
			_observations.add(inputs::dispose);
			if (_access instanceof ObservableRule observable) {
				_observations.add(observable.observe(this::updateEditable));
			}
			if (!Arrays.equals(_inputValues, inputValues())) {
				handleInputsChanged();
			} else {
				updateEditable();
			}
			offerCommands();
		}

		private void detach() {
			withdrawCommands();
			_observations.forEach(Runnable::run);
			_observations.clear();
			for (DashboardTiles source : _sources) {
				source.detach();
			}
		}

		@Override
		public void tilesChanged() {
			_entries = collectEntries();
			_control.setTiles(arrangeTiles());
			if (_scope != null) {
				updateTileCommands();
			}
		}

		@Override
		public void activationChanged() {
			_control.updateActivation();
		}

		/**
		 * Another object is displayed: the arrangement stored for it is read, and whether the user
		 * may change it is decided again.
		 */
		private void handleInputsChanged() {
			loadArrangement();
			tilesChanged();
			updateEditable();
		}

		/**
		 * The current values of the inputs, a deleted object being no value.
		 */
		private Object[] inputValues() {
			return InputValues.alive(ChannelInputs.arguments(_inputs));
		}

		/**
		 * Reads the stored arrangement for the current values of the inputs.
		 */
		private void loadArrangement() {
			_inputValues = inputValues();
			_order = List.of();
			_visibility = new HashMap<>();
			if (_arrangement != null) {
				if (_arrangement.execute(_inputValues) instanceof Map<?, ?> stored) {
					_order = ids(stored.get(Config.ORDER));
					for (String id : _order) {
						_visibility.put(id, Boolean.TRUE);
					}
					for (String id : ids(stored.get(Config.HIDDEN))) {
						_visibility.put(id, Boolean.FALSE);
					}
				}
			} else if (_changeable) {
				_personalKey = personalKey();
				_order = readPersonalOrder(_personalKey);
				_visibility = readPersonalVisibility(_personalKey);
			}
		}

		/**
		 * The addition to the personal configuration keys for the current values of the inputs.
		 */
		private String personalKey() {
			if (_personalizeBy == null) {
				return null;
			}
			Object value = _personalizeBy.execute(_inputValues);
			return value == null ? null : TileListElement.objectKey(value);
		}

		/**
		 * Whether the current user may change the arrangement for the current values of the inputs.
		 */
		private boolean mayStore() {
			if (!_changeable) {
				return false;
			}
			ActionArguments arguments = ActionArguments.of(_inputNames, Arrays.asList(inputValues()),
				_inputNames.isEmpty() ? null : _inputNames.get(0));
			return _access.isExecutableFor(arguments).isExecutable();
		}

		/**
		 * Decides again whether the user may change the arrangement, offering or withdrawing the
		 * edit commands and leaving the edit mode accordingly.
		 */
		private void updateEditable() {
			boolean editable = mayStore();
			if (editable == _editable) {
				return;
			}
			_editable = editable;
			_control.setRearrangeable(editable);
			if (editable) {
				offerCommands();
			} else {
				withdrawCommands();
				_control.exitEditMode();
			}
		}

		/**
		 * The tiles of all entries by id, in the order of their declaration.
		 */
		private Map<String, DashboardTiles.Entry> collectEntries() {
			Map<String, DashboardTiles.Entry> result = new LinkedHashMap<>();
			for (DashboardTiles source : _sources) {
				for (DashboardTiles.Entry entry : source.tiles()) {
					result.putIfAbsent(entry.id(), entry);
				}
			}
			return result;
		}

		/**
		 * The current tiles in their arrangement: the tiles of the stored order first, skipping
		 * stored ids without a tile, then the others in the order of their declaration.
		 */
		private List<DashboardTiles.Entry> arrangedEntries() {
			Map<String, DashboardTiles.Entry> remaining = new LinkedHashMap<>(_entries);
			List<DashboardTiles.Entry> result = new ArrayList<>(remaining.size());
			for (String id : _order) {
				DashboardTiles.Entry entry = remaining.remove(id);
				if (entry != null) {
					result.add(entry);
				}
			}
			result.addAll(remaining.values());
			return result;
		}

		/**
		 * The tiles of the control in their arrangement.
		 */
		private List<Tile> arrangeTiles() {
			List<DashboardTiles.Entry> ordered = arrangedEntries();
			List<Tile> result = new ArrayList<>(ordered.size());
			for (DashboardTiles.Entry entry : ordered) {
				Tile tile = new Tile(entry.id(), entry.width(), entry.rowSpan(), isVisible(entry), entry.content())
					.withLabel(entry.label());
				UnitHiding hiding = entry.hiding();
				if (hiding != null) {
					tile.withWithheld(hiding.isHidden());
				}
				if (entry.activation() != null) {
					tile.withActivation(entry.activation(), entry.item());
				}
				result.add(tile);
			}
			return result;
		}

		/**
		 * Whether the given tile is shown: as stored, else by its default.
		 */
		private boolean isVisible(DashboardTiles.Entry entry) {
			Boolean stored = _visibility.get(entry.id());
			return stored != null ? stored.booleanValue() : !entry.optional();
		}

		private void handleReorder(List<String> order) {
			_order = new ArrayList<>(order);
			if (_arrangement != null) {
				storeArrangement();
			} else {
				storePersonalOrder(_personalKey, _order);
			}
		}

		/**
		 * Remembers that the given tile is shown or hidden and stores the arrangement.
		 *
		 * <p>
		 * The personal configuration keeps only the tiles shown or hidden deviating from their
		 * default, so that a tile added to the configuration appears for everyone who has not
		 * hidden it.
		 * </p>
		 */
		private void handleVisibilityChange(String tileId, Boolean visible) {
			if (_arrangement != null) {
				_visibility.put(tileId, visible);
				storeArrangement();
				return;
			}
			DashboardTiles.Entry entry = _entries.get(tileId);
			if (entry != null && visible.booleanValue() == !entry.optional()) {
				_visibility.remove(tileId);
			} else {
				_visibility.put(tileId, visible);
			}
			storePersonalVisibility(_personalKey, _visibility);
		}

		/**
		 * Runs the {@link Config#getStoreArrangement() store function} with the current
		 * arrangement, in a transaction.
		 */
		private void storeArrangement() {
			List<String> order = new ArrayList<>();
			List<String> hidden = new ArrayList<>();
			for (DashboardTiles.Entry entry : arrangedEntries()) {
				order.add(entry.id());
				if (!isVisible(entry)) {
					hidden.add(entry.id());
				}
			}
			Object[] arguments = InputValues.alive(ChannelInputs.arguments(_inputs, order, hidden));
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			Transaction tx = kb.beginTransaction(I18NConstants.STORED_DASHBOARD_ARRANGEMENT);
			try {
				_storeArrangement.execute(arguments);
				tx.commit();
			} finally {
				tx.rollback();
			}
		}

		/**
		 * Offers the edit commands, while the dashboard is displayed and the user may change the
		 * arrangement.
		 */
		private void offerCommands() {
			if (_scope == null || _offered || !_editable || !_control.isAttached()) {
				return;
			}
			_offered = true;
			for (DashboardCommandModel command : _editCommands) {
				offer(command);
			}
			for (DashboardCommandModel command : _tileCommands.values()) {
				offer(command);
			}
		}

		private void withdrawCommands() {
			if (!_offered) {
				return;
			}
			_offered = false;
			for (DashboardCommandModel command : _editCommands) {
				withdraw(command);
			}
			for (DashboardCommandModel command : _tileCommands.values()) {
				withdraw(command);
			}
		}

		/**
		 * Brings the commands adding a hidden tile in line with the current tiles.
		 */
		private void updateTileCommands() {
			for (Iterator<Map.Entry<String, DashboardCommandModel>> it = _tileCommands.entrySet().iterator(); it
				.hasNext();) {
				Map.Entry<String, DashboardCommandModel> command = it.next();
				if (!_entries.containsKey(command.getKey())) {
					it.remove();
					if (_offered) {
						withdraw(command.getValue());
					}
				}
			}
			for (DashboardTiles.Entry entry : _entries.values()) {
				if (!_tileCommands.containsKey(entry.id())) {
					DashboardCommandModel command =
						DashboardCommandModel.showTileCommand(_control, entry.id(), entry.title());
					_tileCommands.put(entry.id(), command);
					if (_offered) {
						offer(command);
					}
				}
			}
		}

		private void offer(DashboardCommandModel command) {
			command.attach();
			_scope.addCommand(command);
		}

		private void withdraw(DashboardCommandModel command) {
			_scope.removeCommand(command);
			command.detach();
		}
	}

	/**
	 * The given value as a list of tile ids.
	 */
	private static List<String> ids(Object value) {
		if (!(value instanceof Collection<?> raw)) {
			return List.of();
		}
		List<String> result = new ArrayList<>(raw.size());
		for (Object o : raw) {
			if (o instanceof String id) {
				result.add(id);
			}
		}
		return result;
	}

	/**
	 * The {@link PersonalConfiguration} key with the given prefix for this dashboard and the given
	 * addition.
	 */
	private String personalKey(String prefix, String addition) {
		return addition == null ? prefix + _id : prefix + _id + PC_KEY_ADDITION_SEPARATOR + addition;
	}

	private List<String> readPersonalOrder(String addition) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return List.of();
		}
		return ids(pc.getJSONValue(personalKey(PC_KEY_PREFIX, addition)));
	}

	/**
	 * The tile visibilities the current user stored, tile id to whether the tile is shown.
	 */
	private Map<String, Boolean> readPersonalVisibility(String addition) {
		Map<String, Boolean> result = new HashMap<>();
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return result;
		}
		if (pc.getJSONValue(personalKey(PC_VISIBILITY_KEY_PREFIX, addition)) instanceof Map<?, ?> raw) {
			for (Map.Entry<?, ?> entry : raw.entrySet()) {
				if (entry.getKey() instanceof String tileId && entry.getValue() instanceof Boolean visible) {
					result.put(tileId, visible);
				}
			}
		}
		return result;
	}

	/**
	 * Stores the given tile visibilities deviating from their defaults.
	 */
	private void storePersonalVisibility(String addition, Map<String, Boolean> visibility) {
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc == null) {
				return;
			}
			pc.setJSONValue(personalKey(PC_VISIBILITY_KEY_PREFIX, addition),
				visibility.isEmpty() ? null : new LinkedHashMap<>(visibility));
			PersonalConfiguration.storePersonalConfiguration();
		} catch (RuntimeException ex) {
			Logger.warn("Failed to persist dashboard tile visibility for '" + _id + "'.", ex,
				DashboardElement.class);
		}
	}

	private void storePersonalOrder(String addition, List<String> order) {
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc == null) {
				return;
			}
			pc.setJSONValue(personalKey(PC_KEY_PREFIX, addition), order);
			PersonalConfiguration.storePersonalConfiguration();
		} catch (RuntimeException ex) {
			Logger.warn("Failed to persist dashboard tile order for '" + _id + "'.", ex, DashboardElement.class);
		}
	}
}
