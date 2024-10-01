/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.View;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.util.Resources;

/**
 * Internationalizable image {@link View}.
 * 
 * @see ThemeImageView View of a thee image.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResourceImageView extends DefaultView {

	/**
	 * The prefix to the given resource key that is used to look up the
	 * alternative text for the image.
	 */
	public static final String RESOURCE_SUFFIX_ALT = ".alt";
	
	/**
	 * The prefix to the given resource key that is used to look up the
	 * tooltip caption for the image.
	 */
	public static final String RESOURCE_SUFFIX_TOOLTIP_CAPTION = ".tooltip.caption";
	
	/**
	 * The prefix to the given resource key that is used to look up the
	 * tooltip for the image.
	 */
	public static final String RESOURCE_SUFFIX_TOOLTIP = ".tooltip";

	/**
	 * Suffix to build the image resource key.
	 */
	private static final String RESOURCE_SUFFIX_IMAGE = ".image";
	
	private final ResPrefix _imageResourceKey;

	private final ThemeImage _image;

	/**
	 * Creates a {@link ResourceImageView}.
	 * 
	 * @param imagePrefix
	 *        A resource prefix that allows deriving the resources {@link #RESOURCE_SUFFIX_IMAGE},
	 *        {@link #RESOURCE_SUFFIX_ALT}, {@link #RESOURCE_SUFFIX_TOOLTIP}, and
	 *        {@link #RESOURCE_SUFFIX_TOOLTIP_CAPTION}.
	 */
	public ResourceImageView(ResPrefix imagePrefix) {
		this(imagePrefix, ThemeImage.i18n(imagePrefix.key(RESOURCE_SUFFIX_IMAGE)));
	}

	/**
	 * Creates a {@link ResourceImageView}.
	 * 
	 * @param imagePrefix
	 *        A resource prefix that allows deriving the resources {@link #RESOURCE_SUFFIX_ALT},
	 *        {@link #RESOURCE_SUFFIX_TOOLTIP}, and {@link #RESOURCE_SUFFIX_TOOLTIP_CAPTION}.
	 * @param image
	 *        The icon to use.
	 */
	public ResourceImageView(ResPrefix imagePrefix, ThemeImage image) {
		_imageResourceKey = imagePrefix;
		_image = image;
	}
	
	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		Resources resources = context.getResources();

		String alt = StringServices.nonNull(resources.getStringOptional(_imageResourceKey.key(RESOURCE_SUFFIX_ALT)));
		ResKey tooltip = _imageResourceKey.key(RESOURCE_SUFFIX_TOOLTIP);
		
		XMLTag tag = _image.toIcon();
		tag.beginBeginTag(context, out);
		out.writeAttribute(ALT_ATTR, alt);
		if (tooltip != null) {
			ResKey tooltipCaption = _imageResourceKey.key(RESOURCE_SUFFIX_TOOLTIP_CAPTION);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip, tooltipCaption);
		}
		tag.endEmptyTag(context, out);
	}

}
