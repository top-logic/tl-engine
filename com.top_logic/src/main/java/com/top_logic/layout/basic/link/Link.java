/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.link;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Common properties for UI elements that can be displayed as clickable elements.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Link extends WithProperties {

	/**
	 * Name of the {@link #getID()} {@link #getPropertyValue(String) property}.
	 */
	String ID = "id";

	/**
	 * Name of the {@link #isVisible()} {@link #getPropertyValue(String) property}.
	 */
	String VISIBLE = "visible";

	/**
	 * Name of the {@link #isDisabled()} {@link #getPropertyValue(String) property}.
	 */
	String DISABLED = "disabled";

	/**
	 * Name of the {@link #getImage()} {@link #getPropertyValue(String) property}.
	 */
	String IMAGE = "image";

	/**
	 * Name of the {@link #getDisabledImage()} {@link #getPropertyValue(String) property}.
	 */
	String DISABLED_IMAGE = "disabled-image";

	/**
	 * Name of the {@link #getActiveImage()} {@link #getPropertyValue(String) property}.
	 */
	String ACTIVE_IMAGE = "active-image";

	/**
	 * Name of the {@link #getAltText()} {@link #getPropertyValue(String) property}.
	 */
	String ALT_TEXT = "alt";

	/**
	 * Name of the {@link #getAccessKey()} {@link #getPropertyValue(String) property}.
	 */
	String ACCESS_KEY = "access-key";

	/**
	 * Name of the {@link #getLabel()} {@link #getPropertyValue(String) property}.
	 */
	String LABEL = "label";

	/**
	 * Name of the {@link #getTooltip()} {@link #getPropertyValue(String) property}.
	 */
	String TOOLTIP = "tooltip";

	/**
	 * Name of the {@link #getTooltipCaption()} {@link #getPropertyValue(String) property}.
	 */
	String TOOLTIP_CAPTION = "tooltip-caption";

	/**
	 * Name of the {@link #writeCssClassesContent(Appendable)} {@link #getPropertyValue(String) property}.
	 */
	String CSS_CLASSES = "css-classes";

	/**
	 * Name of the {@link #getOnclick()} {@link #getPropertyValue(String) property}.
	 */
	String ONCLICK = "onclick";

	/**
	 * The {@link HTMLConstants#ID_ATTR ID} of the top-level element of the visual representation.
	 */
	String getID();

	/**
	 * Whether the button is visible.
	 */
	boolean isVisible();

	/**
	 * Whether the button is disabled.
	 */
	boolean isDisabled();

	/**
	 * Whether the button is active.
	 * 
	 * @return Per default it is inactive.
	 */
	default boolean isActive() {
		return false;
	}

	/**
	 * The icon of the button.
	 */
	ThemeImage getImage();

	/**
	 * Optional disabled variant of {@link #getImage()}.
	 * 
	 * @return The icon to display, when the button {@link #isDisabled() is disabled}, or
	 *         <code>null</code> if {@link #getImage()} should be used instead.
	 */
	default ThemeImage getDisabledImage() {
		return null;
	}

	/**
	 * Optional active variant of {@link #getImage()}.
	 * 
	 * @return The icon to display, when the button {@link #isActive() is active}, or
	 *         <code>null</code> if {@link #getImage()} should be used instead.
	 */
	default ThemeImage getActiveImage() {
		return null;
	}

	/**
	 * The {@link HTMLConstants#ALT_ATTR alternative text} for the button {@link #getImage() icon}.
	 */
	ResKey getAltText();

	/**
	 * The access key to use for directly invoking this button.
	 */
	default char getAccessKey() {
		return '\0';
	}

	/**
	 * The button label.
	 */
	ResKey getLabel();

	/**
	 * The tooltip text displayed over the button.
	 */
	ResKey getTooltip();

	/**
	 * The caption of the {@link #getTooltip() tooltip}.
	 */
	ResKey getTooltipCaption();

	/**
	 * Writes the CSS classes to add to the top-level button element.
	 */
	void writeCssClassesContent(Appendable out) throws IOException;

	/**
	 * The {@link HTMLConstants#ONCLICK_ATTR} contents of the active element of the button.
	 */
	String getOnclick();

	@Override
	default Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		switch (propertyName) {
			case ID:
				return getID();
			case VISIBLE:
				return isVisible();
			case DISABLED:
				return isDisabled();
			case IMAGE:
				return getImage();
			case DISABLED_IMAGE:
				return getDisabledImage();
			case ACCESS_KEY:
				return getAccessKey() == '\0' ? null : Character.valueOf(getAccessKey());
			case ALT_TEXT:
				return getAltText();
			case LABEL:
				return getLabel();
			case TOOLTIP:
				return getTooltip();
			case TOOLTIP_CAPTION:
				return getTooltipCaption();
			case CSS_CLASSES:
				TagWriter buffer = new TagWriter();
				buffer.setState(TagWriter.State.CLASS_ATTRIBUTE);
				try {
					writeCssClassesContent(buffer);
				} catch (IOException ex) {
					throw new IOError(ex);
				}
				return buffer.toString();
			case ONCLICK:
				return getOnclick();
		}
		return WithProperties.super.getPropertyValue(propertyName);
	}

}
