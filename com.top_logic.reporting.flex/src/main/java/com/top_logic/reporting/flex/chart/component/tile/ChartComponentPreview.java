/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.tile;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.chart.ImageControl;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.LabelBasedPreview;
import com.top_logic.mig.html.layout.tiles.component.StaticPreview;
import com.top_logic.reporting.flex.chart.component.AbstractChartComponent;


/**
 * Tile preview to display the chart image of a chart component in the tile 
 * 
 * @author     <a href="mailto:tri@top-logic.com">tri</a>
 */
public class ChartComponentPreview extends StaticPreview<ChartComponentPreview.Config> {

	private ComponentName _chartCompName;

	private String _cssClass;

	/**
	 * Configuration of a {@link ChartComponentPreview}.
	 */
	public interface Config extends StaticPreview.Config {
		/** Name of the css class property */
		public static final String PREVIEW_CSS = "cssClass";

		/**
		 * Name of the chart component to get the icon image from
		 * 
		 * @return May be <code>null</code>.
		 */
		ComponentName getChartComponentName();

		/**
		 * The css-class to use for the {@link ImageControl} that is used to create the preview.
		 */
		@Name(PREVIEW_CSS)
		@StringDefault("previewContainer")
		String getPreviewCss();

		// maybe (hopefully) this is not even required and can be removed
		/**
		 * the width in px for the generated preview chart image
		 */
//		@IntDefault(200)
//		Integer getPreviewChartWidth();

		/**
		 * the height in px for the generated preview chart image
		 */
//		@IntDefault(200)
//		Integer getPreviewChartHeight();

	}

	/**
	 * Creates a new {@link LabelBasedPreview}.
	 */
	public ChartComponentPreview(InstantiationContext context, Config config) {
		super(context, config);
		_chartCompName = config.getChartComponentName();
		_cssClass = config.getPreviewCss();
	}

	@Override
	protected HTMLFragment image(ComponentTile tile) {
		HTMLFragment content = getChartPreviewFragment(tile);
		// we *could* introduce a new css class and use it in a surrounding div, but maybe this is not even
		// necessary
		// return div("card-content", content);
		return content;
	}

	/**
	 * the Fragment to be used as tile-preview content
	 */
	protected HTMLFragment getChartPreviewFragment(ComponentTile tile) {
		if (_chartCompName != null) {
//			int width = Utils.getintValue(getConfig().getPreviewChartWidth());
//			int height = Utils.getintValue(getConfig().getPreviewChartHeight());
			AbstractChartComponent comp =
				(AbstractChartComponent) tile.getTileComponent().getMainLayout().getComponentByName(_chartCompName);

			String imageId = comp.getName() + "_chartPreview";
			ImageControl imageControl =
				new ImageControl(comp, null/* new Dimension(width, height) */, imageId, _cssClass);
			imageControl.setUseImageMap(false);
			return imageControl;
		}
		return Fragments.empty();
	}

	@Override
	protected HTMLFragment previewContent(ComponentTile tile) {
		return div(
				attributes(css(CSS_PREVIEW_BOX_OUTER)),
				div(
					attributes(style("position: relative; width: 100%; height: 100%;")),
					image(tile)
				)
			);
	}
	
}
