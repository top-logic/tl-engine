/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.util.Resources;

/**
 * {@link Renderer} of edit button of {@link GalleryControl}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GalleryButtonRenderer extends AbstractButtonRenderer<GalleryButtonRenderer.Config> {

	private static final String ACTIVE_GALLERY_MANAGEMENT_BUTTON_CLASS = "activeGalleryManagementButton";

	private static final String INACTIVE_GALLERY_MANAGEMENT_BUTTON_CLASS = "inactiveGalleryManagementButton";

	public static GalleryButtonRenderer newGalleryButtonRenderer(DisplayDimension width) {
		Config config = TypedConfiguration.newConfigItem(GalleryButtonRenderer.Config.class);
		config.setButtonWidth(width);
		return TypedConfigUtil.createInstance(config);
	}

	/**
	 * Typed configuration interface definition for {@link GalleryButtonRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractButtonRenderer.Config<GalleryButtonRenderer> {

		DisplayDimension getButtonWidth();

		void setButtonWidth(DisplayDimension width);
	}

	/**
	 * Create a {@link GalleryButtonRenderer}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public GalleryButtonRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}
	
	@Override
	protected void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button)
			throws IOException {
		out.beginBeginTag(DIV);
		writeControlTagAttributes(context, out, button);
		writeStyleAttribute(out);
		if (!button.isDisabled()) {
			writeOnClickAttribute(context, out, button);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, button.getTooltip(),
				null);
		} else {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, button.getDisabledReason(),
				button.getDisabledReasonTitle());
		}
		out.endBeginTag();

		out.writeText(Resources.getInstance().getString(I18NConstants.OPEN_IMAGE_MANAGEMENT_DIALOG_BUTTON));

		out.endTag(DIV);
	}

	private void writeStyleAttribute(TagWriter out) throws IOException {
		out.beginAttribute(STYLE_ATTR);
		out.append("width: ");
		out.append(String.valueOf(getConfig().getButtonWidth()));
		out.endAttribute();
	}

	@Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		return button.isDisabled() ? INACTIVE_GALLERY_MANAGEMENT_BUTTON_CLASS : ACTIVE_GALLERY_MANAGEMENT_BUTTON_CLASS;
	}
}