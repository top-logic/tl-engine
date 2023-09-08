/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.table.component.SelectableBuilderComponent;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.ComponentResolver;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;

/**
 * {@link SelectableBuilderComponent} with additional configuration properties and pre-defined
 * property values.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TileListComponent extends SelectableBuilderComponent {

	/**
	 * Configuration of the buttons to display in the burger menu of the tiles.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface ContextMenuButtons extends ConfigurationItem {

		/** Configuration name of the value of {@link #getContextMenuButtons()}. */
		String CONTEXT_MENU_BUTTONS = "contextMenuButtons";

		/**
		 * All buttons on this component.
		 */
		@Options(fun = InAppImplementations.class)
		@Name(CONTEXT_MENU_BUTTONS)
		List<CommandHandler.ConfigBase<? extends CommandHandler>> getContextMenuButtons();

	}

	/**
	 * Configuration of {@link TileListComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends SelectableBuilderComponent.Config, ContextMenuButtons {


		/** Configuration name of the value of {@link #getTilePreview()}. */
		String TILE_PREVIEW = "tilePreview";

		/** Configuration name of the value of {@link #getNoCardKey()}. */
		String NO_CARD_KEY = "noCardKey";

		@Override
		@ItemDefault(TileListControlProvider.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
		
		/**
		 * The {@link TilePreview} that is used to {@link HTMLFragment} as preview for the displayed
		 * elements.
		 */
		@Name(TILE_PREVIEW)
		@ItemDefault(TLObjectTilePreview.Config.class)
		PolymorphicConfiguration<TilePreview> getTilePreview();

		/**
		 * The {@link ResKey} of the message, when there are no elements to be displayed as tiles.
		 */
		@Name(NO_CARD_KEY)
		ResKey getNoCardKey();

		@Override
		@ListDefault(SelectableContextTileResolver.class)
		List<PolymorphicConfiguration<ComponentResolver>> getComponentResolvers();

	}

	private final List<CommandHandler> _contextMenuButtons;
	
	private Collection<?> _guiModel;

	/**
	 * Creates a new {@link TileListComponent}.
	 */
	public TileListComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_contextMenuButtons = config.getContextMenuButtons()
			.stream()
			.map(buttonConf -> AbstractCommandHandler.getInstance(context, buttonConf))
			.collect(Collectors.toList());
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * Creates a new instance of {@link Config#getTilePreview()}. Fails immediately on problems.
	 */
	public TilePreview createPreview() {
		return createPreview(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
	}

	/**
	 * Creates a new instance of {@link Config#getTilePreview()}.
	 */
	public TilePreview createPreview(InstantiationContext context) {
		return context.getInstance(getConfig().getTilePreview());
	}

	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		if (_guiModel != null && !Collections.disjoint(_guiModel, models)) {
			invalidate();
			super.receiveModelDeletedEvent(models, changedBy);
			return true;
		} else {
			return super.receiveModelDeletedEvent(models, changedBy);
		}
	}

	/**
	 * Creates {@link ComponentTile} for the gui model of this {@link TileListComponent}.
	 */
	public List<ComponentTile> createAllTiles() {
		TilePreview preview = createPreview();
		initGUIModel();
		if (_guiModel == null) {
			return Collections.emptyList();
		} else {
			return ((Collection<?>) _guiModel)
				.stream()
				.map(entry -> newComponentTile(entry, preview, _contextMenuButtons))
				.collect(Collectors.toList());
		}
	}

	private void initGUIModel() {
		ModelBuilder builder = getBuilder();
		Object guiModel = builder.getModel(getModel(), this);
		if (guiModel instanceof Collection<?>) {
			_guiModel = (Collection<?>) guiModel;
		} else if (guiModel == null) {
			_guiModel = null;
		} else {
			_guiModel = Collections.singletonList(guiModel);
		}
	}

	private BOComponentTile newComponentTile(Object bo, TilePreview preview, List<CommandHandler> buttons) {
		return new BOComponentTile(null, this, bo) {

			/**
			 * Selects {@link #getBusinessObject()}. The {@link TileListComponent} has listener which steps
			 * into the tile.
			 * 
			 * @see UpdateTileLayoutSelectionListener
			 */
			@Override
			public void displayTile() {
				selectBusinessObject();
				if (ScriptingRecorder.isRecordingActive()) {
					ScriptingRecorder.recordSelection(tileList(), getBusinessObject(), true,
						SelectionChangeKind.ABSOLUTE);
				}
			}
			
			private TileListComponent tileList() {
				return (TileListComponent) selectable();
			}

			@Override
			public TilePreview getPreview() {
				return preview;
			}
			
			@Override
			public Provider<Menu> getBurgerMenu() {
				if (buttons.isEmpty()) {
					return super.getBurgerMenu();
				}
				Resources resources = Resources.getInstance();
				Object targetObject = getBusinessObject();
				Map<String, Object> args = ContextMenuUtil.createArguments(targetObject);
				return () -> {
					Menu menu = new Menu();
					for (CommandHandler command : buttons) {
						menu.add(tileList().createCommandModel(resources, command, args));
					}
					return menu;
				};
			}

		};
	}

}

