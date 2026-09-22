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
import com.top_logic.layout.view.security.AccessControl;
import com.top_logic.layout.view.security.SecurityScope;
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
 * its content where the action carries no label of its own.
 * </p>
 */
@InApp
public class TileElement implements UIElement {

	/**
	 * Configuration for {@link TileElement}.
	 */
	@TagName("tile")
	public interface Config extends UIElement.Config, WithAccessControl {

		/** Config property name for {@link #getId()}. */
		String ID = "id";

		/** Config property name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Config property name for {@link #getRowSpan()}. */
		String ROW_SPAN = "row-span";

		/** Config property name for {@link #getContent()}. */
		String CONTENT = "content";

		/** Config property name for {@link #getAction()}. */
		String ACTION = "action";

		@Override
		@ClassDefault(TileElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Stable id identifying this tile. Used as the persistence key when
		 * users reorder tiles via drag-and-drop.
		 */
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
		 * Configured as {@code <action class="..." .../>} inside the {@code <tile>} element.
		 * </p>
		 */
		@Name(ACTION)
		@Nullable
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends ViewCommand> getAction();
	}

	private final String _id;

	private final TileWidth _width;

	private final int _rowSpan;

	private final AccessControl _accessControl;

	private final UIElement _content;

	private final ViewCommand _action;

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
		_accessControl = config.getAccessControl();
		_content = context.getInstance(config.getContent());
		PolymorphicConfiguration<? extends ViewCommand> actionConfig = config.getAction();
		_actionConfig = actionConfig instanceof ViewCommand.Config typed ? typed : null;
		_action = context.getInstance(actionConfig);
	}

	/**
	 * Whether this tile is accessible to the current user.
	 *
	 * <p>
	 * A tile guarded by an inaccessible {@link AccessControl} is omitted from the dashboard.
	 * </p>
	 */
	public boolean isAccessible() {
		return AccessChecks.isAccessible(_accessControl);
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
	 * Creates the inner control of this tile.
	 */
	public ReactControl createContentControl(ViewContext context) {
		SecurityScope scope = AccessChecks.resolveScope(_accessControl);
		ViewContext contentContext = scope != null ? context.withScope(SecurityScope.class, scope) : context;
		IReactControl inner = _content.createControl(contentContext);
		return (ReactControl) inner;
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
		return model == null ? null : new TileAction(model, actionName(), Icons.TILE_ACTIVATE);
	}

	/**
	 * The name the tile's action is offered under: the command's own label, or - where the command
	 * carries none - the title of the tile's content, so that the tile is announced by what it
	 * shows.
	 */
	private String actionName() {
		ResKey label = _actionConfig.getLabel();
		if (label == null) {
			ResKey title = _content instanceof TitledElement titled ? titled.getTitle() : null;
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
