/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.base.merge.AbstractMergeNode;
import com.top_logic.base.merge.MergeMessage;
import com.top_logic.base.merge.MergeTreeModel;
import com.top_logic.base.merge.MergeTreeNode;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link CommandHandler} to {@link MergeMessage#setApproved(boolean) approve}
 * {@link MergeMessage}s.
 * 
 * @author <a href=mailto:kha@top-logic.com>kha</a>
 */
public class MergePerformHandler extends AbstractCommandHandler {

	public static final String COMMAND_ID = "performMergeTree";

	public interface Config extends AbstractCommandHandler.Config {
		/**
		 * @see #getPerform()
		 */
		public static final String PERFORM_PROPERTY = "perform";

		@Name(PERFORM_PROPERTY)
		@BooleanDefault(true)
		boolean getPerform();

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();
	}

    /** perform the Tree ? */
	final boolean perform;
    
	/**
	 * Creates a {@link MergePerformHandler}.
	 */
	public MergePerformHandler(InstantiationContext context, Config config) {
		super(context, config);

		perform = config.getPerform();
    }

    /** 
     * Fetch the Root MergeTreeNode and call performMerge().
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
    	FormComponent  theComponent = (FormComponent) aComponent;
		MergeTreeModel theModel = (MergeTreeModel) model;
    	FormTree       theTree      = (FormTree) theComponent.getFormContext().getMember(MergeTreeComponenet.TREE_FIELD);

    	List<MergeMessage> messagesToPerform = new ArrayList<>();
		this.performMerge(theTree.getTreeModel(), (AbstractMergeNode) theModel.getRoot(), messagesToPerform);

    	return HandlerResult.DEFAULT_RESULT;
    }

    /**
     * Run through the childs of the {@link TreeUIModel} and set those to "approved" that
     * have their checkbox checked.
     */
	protected void performMerge(TreeUIModel<AbstractMergeNode> aTreeModel, AbstractMergeNode aNode,
			List<MergeMessage> messagesToPerform) {
    	Object  theObject    = aTreeModel.getUserObject(aNode);
    	boolean needsPerform = true;

    	if (theObject instanceof FormGroup) {
    		FormGroup theGroup = (FormGroup) theObject;

    		if (theGroup.hasMember(MergeTreeComponenet.APPROVE_FIELD)) {
	    		needsPerform = ((BooleanField) theGroup.getMember(MergeTreeComponenet.APPROVE_FIELD)).getAsBoolean();
	    	}
    	}

		if (needsPerform && (aNode instanceof MergeMessage)) {
			((MergeMessage) aNode).setApproved(true);
    	}

		for (AbstractMergeNode theNode : aNode.getChildren()) {
			this.performMerge(aTreeModel, theNode, messagesToPerform);
		}
	}

    /** 
     * Override this method to perform the node (and do any extra cleanup yu may need).
     * 
     * @return HandlerResult.DEFAULT_RESULT except for errors.
     */
    protected HandlerResult performNode(MergeTreeNode rootNode)  {
        try {
            rootNode.perform();
        } catch (Exception ex) {
            Logger.error("Failed to performNode", ex, this);
            HandlerResult result = new HandlerResult();
			result.addErrorText("performNode failed");
			result.addErrorText(ex.toString());
            return result;
        }
        return HandlerResult.DEFAULT_RESULT;
    }

	public static <C extends Config> C updatePerform(C config, boolean value) {
		return update(config, Config.PERFORM_PROPERTY, value);
	}

}
