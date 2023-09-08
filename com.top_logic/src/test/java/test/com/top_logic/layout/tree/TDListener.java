/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree;

import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.TreeDataEvent;
import com.top_logic.layout.tree.TreeDataEventVisitor;
import com.top_logic.layout.tree.TreeDataListener;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * Testing implementation of TreeDataListener.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TDListener implements TreeDataEventVisitor, TreeDataListener {

    /** Data harvested from notify methods */
	public TreeData treeData;
    
	public Object newObj, oldObj;

	public void clear() {
         oldObj = newObj = treeData = null;
    }
    
    @Override
	public void notifyRendererChange(TreeData aSource, TreeRenderer aNewRenderer,
            TreeRenderer aOldRenderer) {
        treeData = aSource;
        newObj = aNewRenderer;
        oldObj = aOldRenderer;
    }

    @Override
	public void notifySelectionModelChange(TreeData aSource,
            SelectionModel aNewSelection, SelectionModel aOldSelection) {
        treeData = aSource;
        newObj = aNewSelection;
        oldObj = aOldSelection;
    }

    @Override
	public void notifyTreeModelChange(TreeData aSource, TreeUIModel aNewModel,
            TreeUIModel aOldModel) {
        treeData = aSource;
        newObj = aNewModel;
        oldObj = aOldModel;
    }
    
	@Override
	public void handleTreeDataChange(TreeDataEvent event) {
		event.visit(this);
	}

}
