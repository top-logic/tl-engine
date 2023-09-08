/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tools.layout;

import java.util.Set;

/**
 * @author    <a href="mailto:TEH@top-logic.com">TEH</a>
 */
public class LayoutConstants {

    /** this type is only allowed on level 1, when set an icon is generated instead of a tab */
    public static final String TYPE_TAB_ICON           = "tabIcon";
    /** this type is only allowed on level 1, when set no tree component is generated for this tab*/
    public static final String TYPE_BLANK              = "blank";
    /** this type expects a template name in the template column */
    public static final String TYPE_TEMPLATE           = "template";
    /** this type is only allowed on LEVEL 2, when set a project tree is generated below the 2. tabs */
    public static final String TYPE_TREE               = "tree";

	/** this type expects a template name in the template column */
	public static final String TYPE_NO_GENERATION = "noGeneration";

    private static final String MAIN_TEMPLATE = "masterFrame.template";
    private static final String LEVEL_ONE_TEMPLATE = "levelOne.template";
    private static final String LEVEL_ONE_TEMPLATE_TREE = "levelOneTree.template";
    private static final String LEVEL_TWO_TEMPLATE_TABBED = "levelTwoTabbed.template";
    private static final String LEVEL_TWO_TEMPLATE_TREE = "levelTwoTree.template";
    private static final String LEVEL_TWO_TEMPLATE_TABBED_TREE = "levelTwoTabbedTree.template";
    private static final String POS_PROJECT_LAYOUT_BRACE = "projectLayoutBrace.template";
    
    /** 
     * This method returns one of the default templates for the given criteria of a node 
     * in the layout tree.
     * 
     * @param someTypes  The types of the node.
     * @param aLevel  The level of the node. 
     * @param hasChildren  Whether the node has children.
     * @return  Returns the default template for use with the node.
     */
    public static String getDefaultTemplate(Set someTypes, int aLevel, boolean hasChildren) {
        switch (aLevel) {
        case 0:
            return MAIN_TEMPLATE;
        case 1:
            if (hasChildren) {
                if (someTypes.contains(TYPE_BLANK)) {
                    return LEVEL_ONE_TEMPLATE;
                } else {
                    return LEVEL_ONE_TEMPLATE_TREE;
                }
            }  else {
                if (someTypes.contains(TYPE_BLANK)) {
						return POS_PROJECT_LAYOUT_BRACE;
                } 
                else {
                    return LEVEL_TWO_TEMPLATE_TREE;
                }
            }
        case 2:
            if (someTypes.contains(TYPE_TREE)) {
                if (!hasChildren) {
                    return LEVEL_TWO_TEMPLATE_TREE;
                } else {
                    return LEVEL_TWO_TEMPLATE_TABBED_TREE;
                }
            }
            else {
                if (!hasChildren) {
						return POS_PROJECT_LAYOUT_BRACE;
                } else {
                    return LEVEL_TWO_TEMPLATE_TABBED;
                }
            }

        default:
				return POS_PROJECT_LAYOUT_BRACE;
        }
    }

}

