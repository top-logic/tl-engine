/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.TileContent;
import com.top_logic.layout.react.control.layout.TileWidth;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopeDefinition;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.view.list.ElementAsConfig;
import com.top_logic.layout.view.list.InputValues;
import com.top_logic.layout.view.list.TemplateInstances;
import com.top_logic.layout.view.model.CommandItemActivation;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Tiles of a {@link DashboardElement dashboard} computed from a list of objects (the
 * {@code <tiles>} tag): one tile per object.
 *
 * <p>
 * The {@link Config#getItems() items} script computes the objects from the values of the
 * {@link Config#getInputs() inputs}. Each tile displays the {@link Config#getContent() content}
 * instantiated with its object published on the {@link Config#getElementChannel() element
 * channel}, like an item of an {@code <object-list>}. Its {@link Config#getWidth() width},
 * {@link Config#getRowSpan() row span} and {@link Config#getLabel() name} are scripts over the
 * object, and activating the tile runs the {@link Config#getAction() action} with the object.
 * </p>
 *
 * <pre>
 * &lt;dashboard id="overview"&gt;
 *   &lt;tile id="summary" width="full"&gt;...&lt;/tile&gt;
 *   &lt;tiles id="products"
 *     inputs="catalogue"
 *     items="$catalogue.get(`demo:Catalogue#products`)"
 *     width="$element.get(`demo:Product#tileWidth`)"
 *     label="$element.get(`demo:Product#name`)"
 *   &gt;
 *     &lt;view-ref view="catalogue/product-card.view.xml"&gt;
 *       &lt;bind channel="product" to="element"/&gt;
 *     &lt;/view-ref&gt;
 *     &lt;action class="com.top_logic.layout.view.command.GenericViewCommand"&gt;
 *       &lt;show-object/&gt;
 *     &lt;/action&gt;
 *   &lt;/tiles&gt;
 * &lt;/dashboard&gt;
 * </pre>
 *
 * <p>
 * The tiles take the place of the declaration in the order of the dashboard, in the order of the
 * objects. The id of a tile is the {@link Config#getId() id} of the declaration, a colon and the
 * key of its object, so that a tile keeps its place in a rearranged dashboard. The tiles follow the
 * model: a change of an input, of a displayed object or of an object of the
 * {@link Config#getObservedTypes() observed types} computes the objects again; the tile of an object
 * that stays keeps its controls, a new object gets a new tile, and the tile of an object that is
 * gone disappears.
 * </p>
 */
@InApp
public class TileListElement implements TileDeclaration {

	/** Separator between the id of the declaration and the key of an object in a tile id. */
	public static final String TILE_ID_SEPARATOR = ":";

	/**
	 * Configuration for {@link TileListElement}.
	 */
	@TagName(Config.TAG_NAME)
	@TypeIcon("css:bi bi-grid-3x2-gap")
	public interface Config extends TileDeclaration.Config, Inputs, ElementAsConfig, ActionScopeDefinition {

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Configuration tag of a {@link TileListElement}. */
		String TAG_NAME = "tiles";

		/** Configuration name for {@link #getItems()}. */
		String ITEMS = "items";

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/** Configuration name for {@link #getElementChannel()}. */
		String ELEMENT_CHANNEL = "element-channel";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/** Configuration name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Configuration name for {@link #getRowSpan()}. */
		String ROW_SPAN = "row-span";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getAction()}. */
		String ACTION = "action";

		@Override
		@ClassDefault(TileListElement.class)
		Class<? extends TileListElement> getImplementationClass();

		/**
		 * TL-Script expression computing the objects to show a tile for from the values of the
		 * inputs.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named
		 * after their channels, or by their {@code as} names. It is evaluated again whenever an
		 * input changes, so the tiles follow what the inputs hold.
		 * </p>
		 */
		@Name(ITEMS)
		@Mandatory
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getItems();

		/**
		 * Types whose object changes (create / update / delete) compute the
		 * {@link #getItems() objects} again, so that a tile appears for a new object.
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();

		/**
		 * Name of the channel publishing the object of a tile to its {@link #getContent()
		 * content}.
		 */
		@Name(ELEMENT_CHANNEL)
		@StringDefault(ElementAsConfig.DEFAULT_ELEMENT_AS)
		String getElementChannel();

		/**
		 * The content instantiated once per object, with the object published on the
		 * {@link #getElementChannel() element channel}.
		 */
		@Name(CONTENT)
		@DefaultContainer
		@Mandatory
		@Options(fun = AllInAppImplementations.class)
		@TreeProperty
		PolymorphicConfiguration<? extends UIElement> getContent();

		/**
		 * TL-Script expression computing the relative width of the tile of an object.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} and the object, as
		 * the variable {@value ElementAsConfig#DEFAULT_ELEMENT_AS} or by the name given with
		 * {@value ElementAsConfig#ELEMENT_AS}. It yields the name of a width -
		 * <code>quarter</code>, <code>third</code>, <code>half</code>, <code>two-thirds</code>
		 * or <code>full</code> - as text or as a classifier of that name.
		 * </p>
		 */
		@Name(WIDTH)
		@Mandatory
		@Supplies(value = ElementAsConfig.DEFAULT_ELEMENT_AS, renamedBy = ElementAsConfig.ELEMENT_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getWidth();

		/**
		 * TL-Script expression computing the number of grid rows the tile of an object spans.
		 *
		 * <p>
		 * Sees the same variables as the {@link #getWidth() width}. Without an expression, a tile
		 * spans one row.
		 * </p>
		 */
		@Name(ROW_SPAN)
		@Supplies(value = ElementAsConfig.DEFAULT_ELEMENT_AS, renamedBy = ElementAsConfig.ELEMENT_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getRowSpan();

		/**
		 * TL-Script expression computing the name of the tile of an object.
		 *
		 * <p>
		 * Sees the same variables as the {@link #getWidth() width}. A hidden tile is offered to be
		 * added again under this name, and a tile is announced by it while it is moved. Without an
		 * expression, a tile is named after its object.
		 * </p>
		 */
		@Name(LABEL)
		@Supplies(value = ElementAsConfig.DEFAULT_ELEMENT_AS, renamedBy = ElementAsConfig.ELEMENT_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getLabel();

		/**
		 * The command an activation of a tile runs, with the object of the tile as its main
		 * argument, named by {@link #getElementAs() element-as}.
		 *
		 * <p>
		 * The tile is activated like a fixed tile of the dashboard: by a click or {@code Enter}.
		 * The command's own executability rules decide over each object: a tile whose object the
		 * rules hide the command for only displays its content, one they disable it for shows that.
		 * </p>
		 */
		@Name(ACTION)
		@Nullable
		@Options(fun = AllInAppImplementations.class)
		@TreeProperty
		PolymorphicConfiguration<? extends ViewCommand> getAction();

		/**
		 * The {@link #getAction() action} runs with the object of the activated tile as its main
		 * argument, named like the object.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			if (part != null && part == getAction()) {
				return ActionScope.single(elementName(this));
			}
			return outer;
		}
	}

	private final Config _config;

	private final UIElement _content;

	private final QueryExecutor _itemsExecutor;

	private final QueryExecutor _widthExecutor;

	private final QueryExecutor _rowSpanExecutor;

	private final QueryExecutor _labelExecutor;

	/** The instantiated {@link Config#getAction() action}, {@code null} without one. */
	private final ViewCommand _action;

	/** The configuration {@link #_action} was instantiated from, {@code null} without one. */
	private final ViewCommand.Config _actionConfig;

	/**
	 * Creates a {@link TileListElement} from configuration.
	 */
	@CalledByReflection
	public TileListElement(InstantiationContext context, Config config) {
		_config = config;
		_content = context.getInstance(config.getContent());
		_itemsExecutor = InputScript.compile(config, Config.ITEMS);
		_widthExecutor = InputScript.compile(config, Config.WIDTH);
		_rowSpanExecutor = InputScript.compile(config, Config.ROW_SPAN);
		_labelExecutor = InputScript.compile(config, Config.LABEL);
		PolymorphicConfiguration<? extends ViewCommand> action = config.getAction();
		_actionConfig = action instanceof ViewCommand.Config actionConfig ? actionConfig : null;
		_action = ViewCommands.instantiate(context, action, elementName(config));
	}

	/**
	 * The name the scripts and the action of the given declaration see the object under.
	 */
	static String elementName(Config config) {
		String name = config.getElementAs();
		return name == null || name.isBlank() ? ElementAsConfig.DEFAULT_ELEMENT_AS : name.trim();
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(_content));
	}

	@Override
	public DashboardTiles createTiles(ViewContext context, DashboardTiles.Host host) {
		return new ComputedTiles(context, host);
	}

	/**
	 * The id of the tile of the given object.
	 */
	String tileId(Object item) {
		return _config.getId() + TILE_ID_SEPARATOR + objectKey(item);
	}

	/**
	 * The key identifying the given object: the id of a persistent object, the text of any other
	 * value.
	 */
	static String objectKey(Object item) {
		if (item instanceof TLObject object && object.tHandle() != null) {
			return IdentifierUtil.toExternalForm(object.tIdLocal());
		}
		return String.valueOf(item);
	}

	/**
	 * The tiles of one display of the dashboard, following the objects they are computed from.
	 */
	private final class ComputedTiles implements DashboardTiles {

		private final ViewContext _context;

		private final List<ViewChannel> _inputs;

		private final TemplateInstances _instances;

		private final RowSourceObserver<Object> _observer;

		/** The model of the action, {@code null} without one. */
		private final ViewCommandModel _actionModel;

		/** What activating a tile runs, {@code null} without an action. */
		private final CommandItemActivation _activation;

		private final Runnable _activationListener;

		private List<Object> _items;

		ComputedTiles(ViewContext context, Host host) {
			_context = context;
			_inputs = ChannelInputs.resolve(context, _config.getInputs());
			_instances = new TemplateInstances(List.of(_content), _config.getElementChannel(), _config.getId());
			if (_action != null && _actionConfig != null) {
				_actionModel = ViewCommandModel.forCommand(context, _action, _actionConfig, elementName(_config));
				_activation = new CommandItemActivation(_actionModel);
				_activationListener = host::activationChanged;
				_actionModel.addStateChangeListener(_activationListener);
			} else {
				_actionModel = null;
				_activation = null;
				_activationListener = null;
			}

			_items = computeItems(ChannelInputs.arguments(_inputs));
			_instances.update(_context, _items);
			_observer = new RowSourceObserver<>(_items, this::computeItems,
				ObservedTypes.resolve(_config.getObservedTypes()), _inputs,
				items -> {
					_items = items;
					_instances.update(_context, items);
					host.tilesChanged();
				});
		}

		/**
		 * The distinct objects the items script yields for the given input values.
		 */
		private List<Object> computeItems(Object[] inputValues) {
			Object result = _itemsExecutor.execute(InputValues.alive(inputValues));
			if (result instanceof Collection<?> collection) {
				return new ArrayList<>(new LinkedHashSet<>(collection));
			}
			return result == null ? new ArrayList<>() : new ArrayList<>(List.of(result));
		}

		@Override
		public List<Entry> tiles() {
			List<Entry> result = new ArrayList<>(_items.size());
			for (Object item : _items) {
				Object[] arguments = InputValues.alive(ChannelInputs.arguments(_inputs, item));
				String label = label(item, arguments);
				ReactControl control = _instances.get(item);
				result.add(new Entry(tileId(item), width(arguments), rowSpan(arguments), label,
					ResKey.text(label), false, null, () -> new TileContent(control, null), _activation, item));
			}
			return result;
		}

		private TileWidth width(Object[] arguments) {
			Object value = _widthExecutor.execute(arguments);
			String name = value instanceof TLClassifier classifier ? classifier.getName()
				: value instanceof TileWidth width ? width.getExternalName()
				: value == null ? null : value.toString();
			try {
				return ConfigUtil.getEnumValueMandatory(Config.WIDTH, name, TileWidth.class);
			} catch (ConfigurationException ex) {
				Logger.warn("Invalid tile width '" + value + "' at " + _config.location() + ", using '"
					+ TileWidth.QUARTER.getExternalName() + "'.", ex, TileListElement.class);
				return TileWidth.QUARTER;
			}
		}

		private int rowSpan(Object[] arguments) {
			if (_rowSpanExecutor == null) {
				return 1;
			}
			Object value = _rowSpanExecutor.execute(arguments);
			return value instanceof Number number ? Math.max(1, number.intValue()) : 1;
		}

		private String label(Object item, Object[] arguments) {
			Object value = _labelExecutor == null ? item : _labelExecutor.execute(arguments);
			if (value instanceof String text) {
				return text;
			}
			return MetaLabelProvider.INSTANCE.getLabel(value);
		}

		@Override
		public void attach(ModelScope scope) {
			_observer.attach(scope);
			if (_actionModel != null) {
				_actionModel.attach(scope);
			}
		}

		@Override
		public void detach() {
			_observer.detach();
			if (_actionModel != null) {
				_actionModel.detach();
			}
		}

		@Override
		public void dispose() {
			detach();
			if (_actionModel != null) {
				_actionModel.removeStateChangeListener(_activationListener);
			}
			_instances.update(_context, List.of());
		}
	}

}
