/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.editor.commands.DeleteComponentCommand;
import com.top_logic.layout.editor.commands.EditComponentCommand;
import com.top_logic.layout.editor.commands.EditableComponentExecutability;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.I18NConstants;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;

/**
 * {@link ComponentTile} for the combination of a {@link Selectable} and some selectable object. The
 * selectable object serves as {@link #getBusinessObject()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class BOComponentTile extends AbstractComponentTile {

	private Selectable _boOwner;

	/**
	 * Creates a new {@link BOComponentTile}.
	 * 
	 * @param tileComponent
	 *        Value of {@link #getTileComponent()}. May be null.
	 * @param bo
	 *        The represented business object.
	 */
	public BOComponentTile(LayoutComponent tileComponent, Selectable boOwner, Object bo) {
		super(tileComponent, bo);
		_boOwner = boOwner;
	}

	/**
	 * The {@link Selectable} for which {@link #getBusinessObject()} is a selectable object.
	 */
	protected final Selectable selectable() {
		return _boOwner;
	}

	/**
	 * Force {@link #selectable()} to select {@link #getBusinessObject()}.
	 */
	protected void selectBusinessObject() {
		selectable().setSelected(getBusinessObject());
	}

	@Override
	public boolean isAllowed() {
		return true;
	}

	@Override
	public ResKey getTileLabel() {
		return ResKey.text(MetaLabelProvider.INSTANCE.getLabel(getBusinessObject()));
	}

	/**
	 * Determines the {@link Menu} for editing and deleting the given target component.
	 * 
	 * @param invocationComponent
	 *        The component on which the commands are executed. It is expected that a
	 *        {@link EditComponentCommand} and a {@link DeleteComponentCommand} is registered at
	 *        this component.
	 * @param targetComponent
	 *        The component represented by the tile that gets the {@link Menu}.
	 * 
	 * @return A {@link Provider} delivering the {@link Menu}.
	 */
	public static Provider<Menu> getTileCommands(LayoutComponent invocationComponent,
			LayoutComponent targetComponent) {
		return () -> {
			ArrayList<CommandModel> commands = new ArrayList<>(2);
			addEditCommand(commands, invocationComponent, targetComponent);
			removeCommand(commands, invocationComponent, targetComponent);
			Menu menu = new Menu();
			commands.forEach(menu::add);
			return menu;
		};
	}

	private static void addEditCommand(Collection<CommandModel> commands, LayoutComponent invocationComponent,
			LayoutComponent toEdit) {
		CommandHandler editCommmand = invocationComponent.getCommandById(EditComponentCommand.DEFAULT_COMMAND_ID);
		if (editCommmand == null) {
			return;
		}
		String label = Resources.getInstance().getString(I18NConstants.EDIT_TILE_COMMAND_LABEL);
		Map<String, Object> args =
			Collections.singletonMap(EditableComponentExecutability.EDITED_COMPONENT_ARG, toEdit);
		commands.add(CommandModelFactory.commandModel(editCommmand, invocationComponent, args, label));
	}

	private static void removeCommand(Collection<CommandModel> commands, LayoutComponent invocationComponent,
			LayoutComponent toRemove) {
		CommandHandler deleteCommmand = invocationComponent.getCommandById(DeleteComponentCommand.DEFAULT_COMMAND_ID);
		if (deleteCommmand == null) {
			return;
		}
		String label = Resources.getInstance().getString(I18NConstants.DELETE_TILE_COMMAND_LABEL);
		Map<String, Object> args =
			Collections.singletonMap(EditableComponentExecutability.EDITED_COMPONENT_ARG, toRemove);
		commands.add(CommandModelFactory.commandModel(deleteCommmand, invocationComponent, args, label));
	}

}

