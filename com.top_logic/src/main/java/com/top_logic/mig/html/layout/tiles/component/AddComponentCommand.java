/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;


import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.LabeledConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.messagebox.SimpleSelectDialog;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileBuilder;
import com.top_logic.mig.html.layout.tiles.TileUtils;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.util.Resources;

/**
 * {@link CommandHandler} to add a new view from the possible {@link TileBuilder}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AddComponentCommand extends PreconditionCommandHandler {

	/**
	 * Default {@link CommandHandler#getID() id} of this {@link CommandHandler} as registered in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "addComponent";

	/**
	 * Creates a new {@link AddComponentCommand}.
	 */
	public AddComponentCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		TileContainerComponent tileContainer = (TileContainerComponent) component;
		List<TileBuilder> allBuilders = TileUtils.allBuildersByName(tileContainer);
		switch (allBuilders.size()) {
			case 0: {
				return new Hide();
			}
			case 1: {
				return new Success() {

					@Override
					protected void doExecute(DisplayContext context) {
						new ConfigureNewTileComponent(tileContainer, allBuilders.get(0)).open(context);
					}
				};
			}
			default: {
				return new Success() {

					@Override
					protected void doExecute(DisplayContext context) {
						SimpleSelectDialog<TileBuilder> selectDialog =
							new SimpleSelectDialog<>(I18NConstants.TILE_BUILDER_SELECT_DIALOG,
								SimpleSelectDialog.DEFAULT_WIDTH, SimpleSelectDialog.DEFAULT_HEIGHT, allBuilders);
						selectDialog.setSelectionHandler(
							(confirmContext, selected) -> new ConfigureNewTileComponent(tileContainer, selected)
								.open(confirmContext));
						selectDialog.setLabels(new LabelProvider() {

							Resources res = Resources.getInstance();

							@Override
							public String getLabel(Object object) {
								return res.getString(((LabeledConfiguration) object).getLabel());
							}

						});
						selectDialog.open(context);
					}
				};
			}
		}
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(HasCompositeTileLayout.INSTANCE, super.intrinsicExecutability());
	}

}

