/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.list;

import java.io.IOException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.template.BeaconFormFieldControlProvider;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Resources;

/**
 * Renderer which renders images for the found beacon values.
 * 
 * The images will be taken from "[theme]/beacon/[key].png", where [theme]
 * points to the current theme and [key] is the I18N of the fast list element.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class BeaconRenderer implements Renderer<FastListElement> {

	private static final String EMPTY_VALUE = "empty";

	private static final String LEGACY_EMPTY_VALUE = "white";

	/** Reference to this instance. */ 
    public static final BeaconRenderer INSTANCE = new BeaconRenderer();

	private static final ResPrefix BEACON_NAME_PREFIX = ResPrefix.legacyString("");


    @Override
	public void write(DisplayContext aContext, TagWriter anOut, FastListElement aValue) throws IOException {
		FastListElement element = aValue;
        String theLink = ThemeFactory.getTheme().getFileLink(BeaconRenderer.getImageKey(element));
        if (!StringServices.isEmpty(theLink)) {
        	int index = -1;
        	if (element != null) try {
					index = element.getIndex();
        	} catch (Exception ex) {
        		Logger.warn("Cannot determine index of the given FastListElement (" + element + ") in List.", ex, BeaconRenderer.class);
        	}
		ResKey tooltip = getTooltip(aContext, index);
        	anOut.beginBeginTag(HTMLConstants.IMG);
        	anOut.writeAttribute(HTMLConstants.BORDER_ATTR, 0);
			anOut.writeAttribute(HTMLConstants.ALT_ATTR, aContext.getResources().getString(tooltip));
			anOut.writeAttribute(HTMLConstants.TITLE_ATTR, "");
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(aContext, anOut, tooltip);
        	anOut.writeAttribute(HTMLConstants.SRC_ATTR, aContext.getContextPath() + theLink);
        	anOut.endEmptyTag();
        }
    }

	private ResKey getTooltip(DisplayContext aContext, int index) {
		int tooltipIndex = Math.min(0, index);
		return ResKey.legacy("beacon_" + BeaconFormFieldControlProvider.VAL_TYPE_BEACON + '_' + tooltipIndex);
	}

    public static String getImageKey(FastListElement aBeaconValue){
		String theKey = EMPTY_VALUE;
    	if (aBeaconValue != null) {
    	   theKey = getBeaconKey(aBeaconValue);
    	}
    	return BeaconRenderer.getBeaconPath(theKey);
    }

	private static String getBeaconKey(FastListElement aBeaconValue) {
		String theKey = Resources.getInstance().getString(BEACON_NAME_PREFIX.key(aBeaconValue.getName()));
		if (theKey.equals(LEGACY_EMPTY_VALUE)) {
			return EMPTY_VALUE;
		} else {
			return theKey;
		}
	}

	/**
	 * Return the path to a beacon image defined by the given name.
	 * 
	 * @param aKey
	 *        The name of the requested beacon (e.g. "green").
	 * @return The path relative to the themes folder.
	 */
    public static String getBeaconPath(String aKey) {
        return "/beacon/" + aKey + ".png";
    }

}
