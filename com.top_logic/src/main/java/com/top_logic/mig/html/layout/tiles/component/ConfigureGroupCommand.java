/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.PersonalizedTile;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.TileUtils;
import com.top_logic.mig.html.layout.tiles.scripting.UpdateCompositeTileActionOp;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.util.Resources;

/**
 * {@link AbstractCommandHandler} configuring the contents of a {@link CompositeTile}.
 * 
 * <p>
 * Possible selections are the currently displayed groups and all {@link TileRef}'s which can be
 * displayed in the current context stack of {@link TileRef}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigureGroupCommand extends AbstractCommandHandler {

	/**
	 * {@link ValueListener} applying the selection to a given {@link TileContainerComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class UpdateTileLayouts implements ValueListener {

		private final TileContainerComponent _tileContainer;

		public UpdateTileLayouts(TileContainerComponent tileContainer) {
			_tileContainer = tileContainer;
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			@SuppressWarnings("unchecked")
			/* The options and current selection only contains TileLayout's. */
			List<TileLayout> selectedTiles = (List<TileLayout>) newValue;
			if (ScriptingRecorder.isRecordingActive()) {
				UpdateCompositeTileActionOp.recordUpdateCompositeTile(_tileContainer, selectedTiles);
			}
			Set<PersonalizedTile> selectedPersonalized = new HashSet<>();
			for (TileLayout selected : selectedTiles) {
				if (!(selected instanceof PersonalizedTile)) {
					continue;
				}
				PersonalizedTile personalizedTile = (PersonalizedTile) selected;
				personalizedTile.setHidden(false);
				selectedPersonalized.add(personalizedTile);
			}
			List<?> options = ((SelectField) field).getOptions();
			for (Object option : options) {
				if (!(option instanceof PersonalizedTile)) {
					continue;
				}
				if (selectedPersonalized.contains(option)) {
					continue;
				}
				PersonalizedTile notSelectedTile = (PersonalizedTile) option;
				notSelectedTile.setHidden(true);
				selectedTiles.add(notSelectedTile);
			}
			_tileContainer.updateDisplayedLayout(selectedTiles);
		}
	}

	/**
	 * {@link ResourceProvider} to display the selectable {@link TileLayout} in a select dialog
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class TileLayoutLabels extends AbstractResourceProvider {

		/**
		 * CSS class which is used when the tile is allowed.
		 * 
		 * @see #NOT_ALLOWED_TILE_CSS_CLASS
		 */
		private static final String ALLOWED_TILE_CSS_CLASS = "tileAllowed";

		/**
		 * CSS class which is used when the tile is not allowed.
		 * 
		 * @see #ALLOWED_TILE_CSS_CLASS
		 */
		private static final String NOT_ALLOWED_TILE_CSS_CLASS = "tileNotAllowed";

		/**
		 * CSS class which is used when the tile is allowed and a {@link PersonalizedTile}.
		 * 
		 * @see #NOT_ALLOWED_PERSONALIZED_TILE_CSS_CLASS
		 */
		private static final String ALLOWED_PERSONALIZED_TILE_CSS_CLASS = "tileAllowed personalizedTile";

		/**
		 * CSS class which is used when the tile is not allowed and a {@link PersonalizedTile}.
		 * 
		 * @see #ALLOWED_PERSONALIZED_TILE_CSS_CLASS
		 */
		private static final String NOT_ALLOWED_PERSONALIZED_TILE_CSS_CLASS = "tileNotAllowed personalizedTile";

		private final TileContainerComponent _tileContainer;

		private final Resources _resources = Resources.getInstance();

		public TileLayoutLabels(TileContainerComponent tileContainer) {
			_tileContainer = tileContainer;
		}

		@Override
		public String getLabel(Object object) {
			TileLayout layout = asLayout(object);
			ResKey label = _tileContainer.getLabel(layout);
			return _resources.getString(label);
		}

		@Override
		public String getCssClass(Object anObject) {
			TileLayout layout = asLayout(anObject);
			if (_tileContainer.isTileAllowed(layout)) {
				if (layout instanceof PersonalizedTile) {
					return ALLOWED_PERSONALIZED_TILE_CSS_CLASS;
				} else {
					return ALLOWED_TILE_CSS_CLASS;
				}
			} else {
				if (layout instanceof PersonalizedTile) {
					return NOT_ALLOWED_PERSONALIZED_TILE_CSS_CLASS;
				} else {
					return NOT_ALLOWED_TILE_CSS_CLASS;
				}
			}
		}

		@Override
		public String getTooltip(Object anObject) {
			TileLayout layout = asLayout(anObject);
			if (_tileContainer.isTileAllowed(layout)) {
				return super.getTooltip(anObject);
			} else {
				return _resources.getString(I18NConstants.CURRENTLY_NOT_VISIBLE);
			}
		}

		private TileLayout asLayout(Object object) {
			return (TileLayout) object;
		}
	}

	/**
	 * Id under which the {@link ConfigureGroupCommand} is registered in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "configureTileGroup";

	/**
	 * Creates a new {@link ConfigureGroupCommand}.
	 */
	public ConfigureGroupCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), HasCompositeTileLayout.INSTANCE);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		TileContainerComponent tileContainer = (TileContainerComponent) aComponent;
		List<TileLayout> selection = getSelection(tileContainer);
		List<TileLayout> options = getOptions(tileContainer, selection);
		SelectField selectField =
			FormFactory.newSelectField("selectTiles", options, FormFactory.MULTIPLE, selection, !FormFactory.IMMUTABLE);
		selectField.setLabel(I18NConstants.CONFIGURE_TILE_GROUP);

		/* Do not record setting the value of the field, because no owner can be found. Configuring
		 * the tiles is recorded explicit. */
		ScriptingRecorder.annotateAsDontRecord(selectField);

		/* Add SelectField to FormContext. This is needed, because the ConfigKey of the field
		 * accessed the FormContext of the field. */
		FormContext formContext = new FormContext(aComponent);
		formContext.addMember(selectField);

		SelectFieldUtils.setOptionLabelProvider(selectField, new TileLayoutLabels(tileContainer));

		selectField.setCustomOrder(true);
		selectField.addValueListener(new UpdateTileLayouts(tileContainer));
		return SelectionControl.openSelectorPopup(aContext, aContext.getWindowScope(), selectField);
	}

	private List<TileLayout> getOptions(TileContainerComponent tileContainer, List<TileLayout> selection) {
		Map<String, TileRef> allTiles = TileUtils.allTilesByName(tileContainer);
		/* Add TileRef from current selection to all tiles. Replace already existing by current
		 * selection, because the inner group may be filled which would be lost when the "new"
		 * TileRef is used. */
		selection.stream()
			.filter(TileRef.class::isInstance)
			.forEach(layout -> {
				TileRef tileRef = (TileRef) layout;
				allTiles.put(tileRef.getName(), tileRef);
			});
		List<TileLayout> options = new ArrayList<>(allTiles.values());

		/* Add elements from selection which are not TileRef's. These are already contained. */
		selectionBase(tileContainer).stream()
			.filter(((Predicate<TileLayout>) TileRef.class::isInstance).negate())
			.forEach(tile -> options.add(tile));

		return options;
	}

	private List<TileLayout> getSelection(TileContainerComponent tileContainer) {
		return selectionBase(tileContainer)
			.stream()
			.filter(((Predicate<TileLayout>) ConfigureGroupCommand::isHiddenPersonalized).negate())
			.collect(Collectors.toList());
	}

	private List<TileLayout> selectionBase(TileContainerComponent tileContainer) {
		return ((CompositeTile) tileContainer.displayedLayout()).getTiles();
	}

	/**
	 * Whether the given {@link TileLayout} is a {@link PersonalizedTile} which is currently
	 * {@link PersonalizedTile#isHidden() hidden}.
	 */
	private static boolean isHiddenPersonalized(TileLayout tile) {
		return tile instanceof PersonalizedTile && ((PersonalizedTile) tile).isHidden();
	}

	@Override
	protected boolean mustNotRecord(DisplayContext context, LayoutComponent component,
			Map<String, Object> someArguments) {
		/* Do not record this command. Configuring the tiles is recorded explicit. */
		return true;
	}

}
