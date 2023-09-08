/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge.layout;

import java.io.IOException;

import com.top_logic.base.merge.MergeMessage;
import com.top_logic.base.merge.MergeTreeModel;
import com.top_logic.base.merge.MergeTreeNode;
import com.top_logic.base.merge.RootMergeNode;
import com.top_logic.base.merge.Validator;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeModelCompatibility;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.ConfigurableTreeContentRenderer;
import com.top_logic.layout.tree.renderer.ConfigurableTreeRenderer;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * A Tree Componenent based on merge TreeNodes.
 * 
 * TODO KHA/BHU redesign using recursive FormComponents, while dropping the HTMLTree,
 * but not the MergeNode.
 * 
 * Such a Componenent is usuallay found as part of some complex
 * import or creation wizard. It shows the user the mergeMessages
 * and allows him/her to aprove them to perform them finally.  
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class MergeTreeComponenet extends FormComponent implements ControlRepresentable {
	
	/**
	 * Configuration options for {@link MergeTreeComponenet}.
	 */
	public interface Config extends FormComponent.Config {
		@Override
		@ItemDefault(ControlRepresentableCP.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
	}

	/**
	 * Comment for <code>TREE_FIELD</code>
	 */
	public static final String TREE_FIELD = "tree";

	public static final String APPROVE_FIELD = "approve";

	private NodeGroupInitializer nodeGroupInitializer = new NodeGroupInitializer() {

		@Override
		public void createNodeGroup(FormTree aTree, FormGroup aNodeGroup, Object aNode) {
			boolean isImmutable = false;

			if (aNode instanceof MergeMessage) {
				isImmutable = !((MergeMessage) aNode).isApproveable();
			}

			aNodeGroup.addMember(FormFactory.newBooleanField(APPROVE_FIELD, true, isImmutable));
		}
	};

	/**
	 * Create a new MergeTreeComponenet from XML.
	 */
    public MergeTreeComponenet(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

	@Override
	public Control getRenderingControl() {
		return new TreeControl((TreeData) getFormContext().getMember(TREE_FIELD));
	}

	@Override
	public FormContext createFormContext() {
		FormContext theContext = new FormContext(this);
		TLTreeModel theModel   = (TLTreeModel) this.getModel();
		FormTree    theTree    = new FormTree(TREE_FIELD, getResPrefix(), new DefaultStructureTreeUIModel(theModel), this.nodeGroupInitializer);

		theTree.setTreeRenderer(new ConfigurableTreeRenderer(HTMLConstants.SPAN, HTMLConstants.DIV, new MergeTreeContentRenderer()));
		theContext.addMember(theTree);

		return theContext;
	}

	/**
	 * @author    <a href=mailto:jes@top-logic.com>Jens Schäfer</a>
	 */
	public static class MergeTreeContentRenderer extends ConfigurableTreeContentRenderer {

		@Override
		public ResourceProvider getResourceProvider() {
			return MergeTreeResourceProvider.INSTANCE;
		}

		@Override
		public void writeNodeContent(DisplayContext aContext, TagWriter aWriter, NodeContext aNodeContext) throws IOException {
			this.writeNodeDecoration(aContext, aWriter, aNodeContext);
			this.writeTypeImage(aContext, aWriter, aNodeContext);
			this.writeTextSeparator(aWriter);

			FormTree    theTree      = (FormTree) aNodeContext.getTree().getData();
			TreeUIModel theTreeModel = theTree.getTreeModel();
			TLTreeNode  theNode      = (TLTreeNode) aNodeContext.currentNode();
			FormGroup   theGroup     = (FormGroup) theTreeModel.getUserObject(theNode);
			FormMember  theField     = (theGroup != null) ? theGroup.getMember(APPROVE_FIELD) : FormFactory.newBooleanField(APPROVE_FIELD, true, true);

			DefaultFormFieldControlProvider.INSTANCE.createControl(theField).write(aContext, aWriter);
			aWriter.writeContent(this.getResourceProvider().getLabel(theNode));
			theTree.setCollapsed(false);
		}
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(TreeModelCompatibility.asTreeModel(this.getBuilder().getModel(null, this)));
		}
		return super.validateModel(context);
	}

	protected Class<? extends ResourceProvider> getDefaultResourceProviderClass() {
		return MergeTreeResourceProvider.class;
    }
    
    /** We only support MergeTreeNodes ! */ 
	public boolean supportsTreeNode(Object aNode) {
        return aNode instanceof MergeTreeNode;
    }
    
    
    /**
     * Need this to make BHU happy.
     */
    public static class MergeTreeResourceProvider extends DefaultResourceProvider {

        /**
         * The final singleton instance.
         */
        public static final MergeTreeResourceProvider INSTANCE = new MergeTreeResourceProvider();
        
        /**
         * For now I just return the class name.
         */
        @Override
		public String getType(Object aNode) {
            Object theSource = ((TLTreeNode) aNode).getBusinessObject();

            if (theSource != null) {
                return MetaResourceProvider.INSTANCE.getType(theSource);
            }
            else { 
            	return null;
            }
        }
        
        /**
         * Translate the given model object into a textual description. 
         * 
         * @param aNode The model object
         * @return a textual description that represents the given model object.
         */
        @Override
		public String getLabel(Object aNode) {
            TLTreeNode theNode = (TLTreeNode) aNode;

            if (theNode instanceof MergeTreeNode) {
            	Object theObject = theNode.getBusinessObject();
	
	            if (theObject == null) {
            		theObject = ((MergeTreeNode) theNode).getDest();
            	}

	            if (theObject != null) {
	                return MetaResourceProvider.INSTANCE.getLabel(theObject);
	            }
            }
        	else if (theNode instanceof MergeMessage) {
				return Resources.getInstance().decodeMessageFromKeyWithEncodedArguments(
					((MergeMessage) theNode).getMessage());
        	}

            return "";  // Root often has empty source ...
        }
    }
    
    /**
     * Override this inner class to set up your MergeTreeModel.
     * 
     * 
     * @author    <a href=mailto:kha@top-logic.com>kha</a>
     */
    public static abstract class MergeTreeBuilder implements ModelBuilder {
        
        /**
         * Override to create the needed RootNode and all other needed nodes. 
         * 
         * @return an empty RootMergeNode here.
         */
        public RootMergeNode createRootNode(LayoutComponent baseComponenet)  {
            return new RootMergeNode();
        }
        
        /** 
         * Create a TreeModel based on the RootMergeNode.
         */
        @Override
		public Object getModel(Object businessModel, LayoutComponent baseComponenet) {
            RootMergeNode root   = createRootNode(baseComponenet);
            Validator     valido = createValidator(baseComponenet);
            if (valido != null) {
                valido.validate(root);
            }
            return new MergeTreeModel(root);
            
        }
        
        /**
         * Create a Validator matching the node created in {@link #createRootNode(LayoutComponent)}.
         */
        public abstract Validator createValidator(LayoutComponent baseComponenet);
    }
    
}
