/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.chart.chartjs.tile;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.chart.chartjs.component.ChartJsComponent;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileComponentFinder;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.LabelBasedPreview;

/**
 * {@link LabelBasedPreview} that displays the chart if the {@link ComponentTile#getTileComponent()
 * chart component} in the preview.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChartJSPreview<C extends ChartJSPreview.Config<?>> extends LabelBasedPreview<C> {

	/**
	 * Configuration for a {@link ChartJSPreview}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends ChartJSPreview<?>> extends LabelBasedPreview.Config<I> {

		/**
		 * The name of the {@link ChartJsComponent} whose chart is displayed.
		 * 
		 * <p>
		 * If the value is not set, the first {@link ChartJsComponent} in the displayed tile is used
		 * to create the preview chart.
		 * </p>
		 */
		ComponentName getChartName();

		/**
		 * Description of the chart.
		 */
		ResKey getChartDescription();
	}

	/**
	 * Creates a new {@link ChartJSPreview}.
	 */
	public ChartJSPreview(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected HTMLFragment image(ComponentTile tile) {
		LayoutComponent tileComponent = tile.getTileComponent();
		ChartJsComponent chartComponent;
		ComponentName configuredChartName = getConfig().getChartName();
		if (configuredChartName != null) {
			LayoutComponent configuredComponent =
				tileComponent.getMainLayout().getComponentByName(configuredChartName);
			if (!(configuredComponent instanceof ChartJsComponent)) {
				InfoService.showError(I18NConstants.ERROR_NO_CHART_COMPONENT_FOUND,
					I18NConstants.ERROR_NOT_CHART_COMPONENT__COMPONENT.fill(configuredChartName));
				return empty();
			}
			chartComponent = (ChartJsComponent) configuredComponent;
		} else {
			chartComponent = TileComponentFinder.getFirstOfType(ChartJsComponent.class, tileComponent);
			if (chartComponent == null) {
				InfoService.showError(I18NConstants.ERROR_NO_CHART_COMPONENT_FOUND,
					I18NConstants.ERROR_NO_CHART_COMPONENT_DESCENDENT__COMPONENT.fill(tileComponent));
				return empty();
			}
		}
		return chartComponent.createChartControl();
	}

	@Override
	protected HTMLFragment descriptionContent(ComponentTile tile) {
		ResKey description = getConfig().getChartDescription();
		if (description != null) {
			return message(description);
		} else {
			return empty();
		}
	}

}

