/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.TileAction;
import com.top_logic.layout.react.control.layout.TileWidth;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.security.AccessChecks;
import com.top_logic.layout.view.security.UnitAccess;
import com.top_logic.layout.view.security.UnitHiding;
import com.top_logic.layout.view.security.WithAccessControl;
import com.top_logic.util.Resources;
import java.util.List;

/**
 * A single tile in a {@link DashboardElement dashboard}.
 *
 * <p>
 * Exactly one inner {@link UIElement} is rendered inside the tile. The tile
 * carries layout metadata: a stable {@link Config#getId() id} (used as
 * persistence key for reordering), a relative {@link Config#getWidth() width}
 * fraction and an optional {@link Config#getRowSpan() row span}.
 * </p>
 *
 * <p>
 * A tile with an {@link Config#getAction() action} is an entry point: the whole tile is the
 * surface the user activates, and the tile is announced by the action's label, or by the title of
 * its content where the action carries no label of its own. The tile is marked as an entry point
 * by how it answers the user - the pointer, the raise under it, the focus ring around it - and by
 * its name.
 * </p>
 *
 * <p>
 * The user can hide a tile and add it again later. An {@link Config#isOptional() optional} tile
 * starts hidden. A hidden tile is offered to be added under its {@link Config#getLabel() name}.
 * </p>
 */
@InApp
public class TileElement implements UIElement {

	/**
	 * Configuration for {@link TileElement}.
	 */
	@TagName(Config.TAG_NAME)
	@TypeIcon("css:bi bi-postcard")
	public interface Config extends UIElement.Config, WithAccessControl {

		/** Configuration tag of a {@link TileElement}. */
		String TAG_NAME = "tile";

		/** Config property name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Config property name for {@link #getRowSpan()}. */
		String ROW_SPAN = "row-span";

		/** Config property name for {@link #getContent()}. */
		String CONTENT = "content";

		/** Config property name for {@link #getAction()}. */
		String ACTION = "action";

		/** Config property name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Config property name for {@link #isOptional()}. */
		String OPTIONAL = "optional";

		@Override
		@ClassDefault(TileElement.class)
		Class<? extends TileElement> getImplementationClass();

		/**
		 * Stable id identifying this tile. Used as the persistence key when
		 * users reorder tiles via drag-and-drop.
		 */
		@Override
		@Name(ID)
		@Mandatory
		String getId();

		/**
		 * The tile's relative width as a fraction of available columns.
		 */
		@Name(WIDTH)
		@Mandatory
		TileWidth getWidth();

		/**
		 * The number of grid rows this tile spans. Defaults to 1.
		 */
		@Name(ROW_SPAN)
		@IntDefault(1)
		int getRowSpan();

		/**
		 * The {@link UIElement} rendered inside this tile.
		 */
		@Name(CONTENT)
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		@TreeProperty
		PolymorphicConfiguration<? extends UIElement> getContent();

		/**
		 * The command that an activation of the tile runs.
		 *
		 * <p>
		 * With a command configured, the whole tile is the surface the user activates - the tile
		 * is what a dashboard offers as the entry point to what it shows. The command's own
		 * executability rules decide, so a tile whose command is refused says so instead of
		 * leading nowhere, and one whose command is hidden is a tile that only displays its
		 * content.
		 * </p>
		 *
		 * <p>
		 * A visible mark is placed by the tile's content, e.g. in the header of a card.
		 * </p>
		 *
		 * <p>
		 * Configured as {@code <action class="..." .../>} inside the {@code <tile>} element.
		 * </p>
		 */
		@Name(ACTION)
		@Nullable
		@Options(fun = AllInAppImplementations.class)
		@TreeProperty
		PolymorphicConfiguration<? extends ViewCommand> getAction();

		/**
		 * The name of the tile.
		 *
		 * <p>
		 * A hidden tile is offered to be added to the dashboard again under this name, and a tile
		 * with an {@link #getAction() action} is announced by it, where the action carries no label
		 * of its own. Without a name, the tile is named after the title of its content, or, where
		 * the content has no title, by its {@link #getId() id}.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * Whether the tile is hidden until the user adds it to the dashboard.
		 *
		 * <p>
		 * An optional tile is not shown by default but offered to be added while the dashboard is
		 * edited. A tile that is not optional is shown by default and can be hidden by the user.
		 * </p>
		 */
		@Name(OPTIONAL)
		@BooleanDefault(false)
		boolean isOptional();
	}

	private final String _id;

	private final TileWidth _width;

	private final int _rowSpan;

	private final UnitAccess _access;

	private final UIElement _content;

	private final String _cssClass;

	private final ViewCommand _action;

	private final ResKey _label;

	private final boolean _optional;

	/** The configuration {@link #_action} was instantiated from, {@code null} without one. */
	private final ViewCommand.Config _actionConfig;

	/**
	 * Creates a new {@link TileElement} from configuration.
	 */
	@CalledByReflection
	public TileElement(InstantiationContext context, Config config) {
		_id = config.getId();
		_width = config.getWidth();
		_rowSpan = Math.max(1, config.getRowSpan());
		_access = UnitAccess.create(context, config.getAccessControl());
		_content = context.getInstance(config.getContent());
		_cssClass = config.getCssClass();
		PolymorphicConfiguration<? extends ViewCommand> actionConfig = config.getAction();
		_actionConfig = actionConfig instanceof ViewCommand.Config typed ? typed : null;
		_action = context.getInstance(actionConfig);
		_label = config.getLabel();
		_optional = config.isOptional();
	}

	/**
	 * Whether this tile is accessible to the current user.
	 *
	 * <p>
	 * A tile whose access control the user can never satisfy is omitted from the dashboard, see
	 * {@link AccessChecks#isOffered(UnitAccess)}.
	 * </p>
	 */
	public boolean isAccessible() {
		return AccessChecks.isOffered(_access);
	}

	/**
	 * Whether this tile is currently withheld from the dashboard, see
	 * {@link AccessChecks#hiding(UnitAccess, ViewContext)}.
	 *
	 * @param context
	 *        The context the dashboard is displayed in.
	 * @return {@code null} when this tile is never withheld this way.
	 */
	public UnitHiding hiding(ViewContext context) {
		return AccessChecks.hiding(_access, context);
	}

	/** The stable tile id. */
	public String getId() {
		return _id;
	}

	/** The tile's relative width. */
	public TileWidth getWidth() {
		return _width;
	}

	/** The tile's row span. */
	public int getRowSpan() {
		return _rowSpan;
	}

	/**
	 * Whether this tile is hidden until the user adds it.
	 *
	 * @see Config#isOptional()
	 */
	public boolean isOptional() {
		return _optional;
	}

	/**
	 * The name of this tile: its configured label, else the title of its content, else its id.
	 *
	 * @see Config#getLabel()
	 */
	public ResKey getTitle() {
		ResKey name = name();
		return name == null ? ResKey.text(_id) : name;
	}

	/**
	 * The configured label of this tile, else the title of its content, {@code null} if there is
	 * neither.
	 */
	private ResKey name() {
		if (_label != null) {
			return _label;
		}
		return _content instanceof TitledElement titled ? titled.getTitle() : null;
	}

	/**
	 * Creates the inner control of this tile.
	 */
	public ReactControl createContentControl(ViewContext context) {
		ViewContext contentContext = AccessChecks.contentContext(_access, context);
		ReactControl inner =
			AccessChecks.guard(_access, contentContext, ctx -> (ReactControl) _content.createControl(ctx));
		if (_cssClass != null) {
			// A tile displays itself through its content, so the class of the tile goes on the control
			// of that content. It is written only when the tile declares one, so that the content
			// element keeps the class it declares itself.
			inner.setCssClass(_cssClass);
		}
		return inner;
	}

	/**
	 * The model of the command the tile is activated by, {@code null} for a tile that only
	 * displays its content.
	 *
	 * @implNote The model must be {@link ViewCommandModel#attach(com.top_logic.model.listen.ModelScope)
	 *           attached} and detached with the control displaying the tile, so that it follows its
	 *           input for as long as the tile is on screen.
	 */
	public ViewCommandModel createActionModel(ViewContext context) {
		if (_action == null || _actionConfig == null) {
			return null;
		}
		return ViewCommandModel.forCommand(context, _action, _actionConfig);
	}

	/**
	 * What the given model offers as the tile's action, {@code null} for no model.
	 */
	public TileAction toAction(ViewCommandModel model) {
		return model == null ? null : new TileAction(model, actionName());
	}

	/**
	 * The name the tile's action is offered under: the command's own label, or - where the command
	 * carries none - the name of the tile, so that the tile is announced by what it shows.
	 */
	private String actionName() {
		ResKey label = _actionConfig.getLabel();
		if (label == null) {
			ResKey title = name();
			label = title == null ? I18NConstants.TABLE_ACTIVATE_ROW : I18NConstants.TILE_ACTIVATE__TITLE.fill(title);
		}
		return Resources.getInstance().getString(label);
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(_content));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// Tiles are rendered by DashboardElement; direct rendering is unsupported.
		throw new UnsupportedOperationException("<tile> must be a child of <dashboard>.");
	}
}
