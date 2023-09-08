/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * Static utilities to convert incompatible tree model implementations.
 *
 * <p>
 * {@link #isSupportedModel(Object) Supported} tree model implementations are
 * {@link TLTreeModel}, {@link TreeModel}, and {@link TreeNode}.
 * </p>
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class TreeModelCompatibility {

    public static boolean isSupportedModel(Object aModel) {
        return aModel instanceof TLTreeModel 
            || aModel instanceof TreeModel
            || aModel instanceof TreeNode;
    }
    
    /**
     * Construct a {@link TLTreeModel} from any supported tree model type.
     */
    public static TLTreeModel asTreeModel(Object aModel) {
        if (aModel == null) {
            return null;
        }
        else if (aModel instanceof TLTreeModel) {
            return (TLTreeModel)aModel;
        }
        else if (aModel instanceof TreeModel) {
            TreeModel swingTree = (TreeModel)aModel;
            Object rootNode = swingTree.getRoot();
            if (rootNode instanceof TreeNode) {
                return new SwingTreeNodeTreeModelAdapter(swingTree);
            } else {
                return new SwingTreeModelAdapter(swingTree);
            }
        }
        throw new IllegalArgumentException("Not a supported tree model type: " + aModel);
    }

}
