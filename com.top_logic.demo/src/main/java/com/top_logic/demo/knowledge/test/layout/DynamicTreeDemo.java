/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.knowledge.test.layout;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.MutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.DefaultTableDeclaration;
import com.top_logic.layout.tree.renderer.DefaultTreeImageProvider;
import com.top_logic.layout.tree.renderer.TreeTableRenderer;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Example of a dynamically modifiable Tree.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class DynamicTreeDemo extends FormComponent implements ModelProvider {
   
    /** Common parameters for the Add- and Delete-Commands*/ 
    static final String[] ATTRS = new String[] { "g" };

    /** 
     * Create a new DynamicTreeDemo from XML.
     */
    public DynamicTreeDemo(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }

    /**
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        FormContext  result = new FormContext("treeForm", getResPrefix());
        
        // Setup model
		TLTreeModel<?> treeModel = (TLTreeModel<?>) getModel();
		TreeUIModel<?> uiModel = new DefaultStructureTreeUIModel(treeModel);
        
        String fieldNames[] = new String[] { "input" , "plus" , "minus" };

        DefaultTableDeclaration treeTable = new DefaultTableDeclaration(
                getResPrefix(), fieldNames);
        treeTable.setHasHeader(true);
        
        FormTree tree = new FormTree("tree", this.getResPrefix(), uiModel, new MutableNodeGroupInitializer());
        tree.setTreeRenderer(new TreeTableRenderer(DefaultTreeImageProvider.INSTANCE, treeTable));
        result.addMember(tree);
        return result;
    }

    @Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return EditTreeDemo.createDemoRoot();
    }

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object instanceof TLTreeModel;
	}

	@Override
	protected boolean isChangeHandlingDefault() {
		return false;
	}
	
    /**
     * Setup a NodeGroup with a Checkbox, StringField and SelectField.
     */
    public class MutableNodeGroupInitializer implements NodeGroupInitializer {
        
        @Override
		public void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node) 
        { 
            String      baseName = nodeGroup.getName();
            StringField field = FormFactory.newStringField("input");
			MutableTLTreeNode<?> dtmNode = (MutableTLTreeNode<?>) node;
			Object uObject = dtmNode.getBusinessObject();
			field.initializeField(uObject);
            field.setDefaultValue(uObject);
            nodeGroup.addMember(field);

			final Map<String, Object> args = Collections.<String, Object> singletonMap("g", baseName);
			nodeGroup
				.addMember(FormFactory.newCommandField("plus", getCommandById("addNode"), DynamicTreeDemo.this, args));
			if (!dtmNode.getModel().getRoot().equals(dtmNode)) { // Do not allow removal of root.
				nodeGroup.addMember(
					FormFactory.newCommandField("minus", getCommandById("delNode"), DynamicTreeDemo.this, args));
            }
        }
    }
    
    /**
     * A Private command to store the value from the {@link FormTree} back to the model. 
     * 
     * In a more realistic case this could be an {@link AbstractApplyCommandHandler}.
     */
	public static class SetUserObjectCommand extends AbstractCommandHandler {

		/**
		 * Configuration for {@link SetUserObjectCommand}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

        /** 
         * Create a new SetUserObjectCommand with Id "setUserObject"
         */
		public SetUserObjectCommand(InstantiationContext context, Config config) {
			super(context, config);
        }

        /**
         * Store the changed values from the {@link FormTree} back to its nodes. 
         */
        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
            FormTree theTree = (FormTree) ((FormComponent) aComponent).getFormContext().getMember("tree");
            
			Iterator<? extends FormMember> members = theTree.getMembers();
			while (members.hasNext()) {
			    FormGroup theGroup = (FormGroup) members.next();
			    if (theGroup.getField("input").isChanged()) {
			        FormField input = theGroup.getField("input");
					Object    value = input.getValue();
					MutableTLTreeNode<?> node = (MutableTLTreeNode<?>) theTree.getNode(theGroup);
					node.setBusinessObject(value);
					// So isChanged works correct the next time ...
					input.setDefaultValue(value);
			    }
			}

            return HandlerResult.DEFAULT_RESULT;
        }
        
        @Override
		@Deprecated
		public ResKey getDefaultI18NKey() {
			return I18NConstants.SAVE;
        }
        
    }
    
    public static class AddNodeCommand extends AbstractCommandHandler {
        
        /** Need the current node as a Parameter */ 
        static final String[] ATTRS = new String[] { "g" };

		/**
		 * Configuration for {@link AddNodeCommand}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

        /** 
         * Create a new AddNodeCommand.
         */
		public AddNodeCommand(InstantiationContext context, Config config) {
			super(context, config);
        }
        
        /**
         * @see com.top_logic.tool.boundsec.AbstractCommandHandler#getAttributeNames()
         */
        @Override
		public String[] getAttributeNames() {
            return ATTRS;
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> args) {
            String g = (String) args.get("g");
            FormTree theTree = (FormTree) ((FormComponent) aComponent).getFormContext().getMember("tree");
            FormGroup fg     = (FormGroup) theTree.getMember(g);
			MutableTLTreeNode<?> node = (MutableTLTreeNode<?>) theTree.getNode(fg);
            if (node != null) {
				node.createChild("New child");
                // Expand parent node so child is visible ...
                // Must expand later as model is not aware of children before addChild()
                theTree.getTreeModel().setExpanded(node, true);
            } else {
                Logger.error("No Tree Node for '" + g + "' found, ignored", this);
            }
            return HandlerResult.DEFAULT_RESULT;
        }
        
    }

    public static class DelNodeCommand extends AbstractCommandHandler {
        
		/**
		 * Configuration for {@link DelNodeCommand}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.DELETE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

		/**
		 * Create a new AddNodeCommand.
		 */
		public DelNodeCommand(InstantiationContext context, Config config) {
			super(context, config);
        }
        
        /**
         * @see com.top_logic.tool.boundsec.AbstractCommandHandler#getAttributeNames()
         */
        @Override
		public String[] getAttributeNames() {
            return ATTRS;
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> args) {
            String g = (String) args.get("g");
            FormTree theTree = (FormTree) ((FormComponent) aComponent).getFormContext().getMember("tree");
            FormGroup fg     = (FormGroup) theTree.getMember(g);
			MutableTLTreeNode<?> node = (MutableTLTreeNode<?>) theTree.getNode(fg);
            if (node != null) {
				MutableTLTreeNode<?> parent = node.getParent();
				parent.removeChild(parent.getIndex(node));
            } else {
                Logger.error("No Tree Node for '" + g + "' found, ignored", this);
            }
            
            return HandlerResult.DEFAULT_RESULT;
        }
        
    }
    

}

