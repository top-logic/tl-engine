/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.help;

import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Traverses the given component and finds the last visible component with a help ID.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HelpFinder extends DefaultDescendingLayoutVisitor {
	
	private String _helpId;
	/** Suffix to identify a help id. */
	public static final String HELP_ID = "helpID";
	
	/**
	 * Only visit visible components.
	 * 
	 * @param component
	 *        the {@link LayoutComponent} to check for having a help ID.
	 */
	@Override
	public boolean visitLayoutComponent(LayoutComponent component) {
		boolean visible = component.isVisible();
		if (!visible) {
			return false;
		}
		String helpID = HelpFinder.getHelpID(component);
		if (helpID != null) {
			_helpId = helpID;
		}
		return true;
	}

	/**
	 * The most specific help id.
	 */
	public String getHelpId() {
		return _helpId;
    }

	/**
	 * Checks whether the given component delivers a non <code>null</code> help id.
	 * 
	 * @see #getHelpID(LayoutComponent)
	 */
	public static boolean hasHelpId(LayoutComponent component) {
		return hasHelpID(component.getConfig());
	}

	/**
	 * Checks whether the given component configuration delivers a non <code>null</code> help id.
	 * 
	 * @see #getHelpID(LayoutComponent.Config)
	 */
	public static boolean hasHelpID(LayoutComponent.Config config) {
		String helpId = config.getHelpId();
		if (helpId != null) {
			return true;
		}
		String helpIdLegacy = config.getHelpIdLegacy();
		if (helpIdLegacy != null) {
			return true;
		}
		return false;
	}

	/**
	 * Determines the help id of the given component, if one is present.
	 * 
	 * @return May be <code>null</code> when the component has no help id.
	 */
	public static String getHelpID(LayoutComponent component) {
		return getHelpID(component.getConfig());
	}

	/**
	 * Determines the help id of the given component configuration, if one is present.
	 * 
	 * @return May be <code>null</code> when the configuration has no help id.
	 */
	public static String getHelpID(LayoutComponent.Config config) {
		String helpId = config.getHelpId();
		if (helpId != null) {
			return helpId;
		}
		String helpIdLegacy = config.getHelpIdLegacy();
		if (helpIdLegacy != null) {
			return toHelpId(helpIdLegacy);
		}
		return null;
	}

	/**
	 * Legacy quirks to transform a resource key into a help ID.
	 */
	public static String toHelpId(String source) {
		int dotIndex = source.lastIndexOf('.');
		String prefix = source;
		if (dotIndex > 0) {
			prefix = source.substring(0, dotIndex);
		}
		return prefix + "." + HelpFinder.HELP_ID;
	}

	/** Finds the most specific help id in the component subtree. */
	public static String findHelpId(LayoutComponent component) {
		HelpFinder helpFinder = new HelpFinder();
	
		component.acceptVisitorRecursively(helpFinder);
	
		return helpFinder.getHelpId();
	}
}
