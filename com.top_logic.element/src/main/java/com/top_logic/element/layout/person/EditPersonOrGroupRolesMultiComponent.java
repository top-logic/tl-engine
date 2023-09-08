/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.structured.StructuredElementTreeModel;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.wrap.WrapperNameComparator;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.DefaultChannelSPI;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.table.renderer.DefaultRowClassProvider;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.model.ComposingTreeModel;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.layout.tree.renderer.DefaultColumnDeclaration;
import com.top_logic.layout.tree.renderer.DefaultTableDeclaration;
import com.top_logic.layout.tree.renderer.DefaultTreeImageProvider;
import com.top_logic.layout.tree.renderer.TreeTableRenderer;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Component supports setting multiple roles for a person
 * on the elements of a BoundObject-based tree
 * 
 * TODO KBU react upon creation/deletion of elements in the tree....
 * Deletion: tree containment
 * Creation: tree containment of parent
 * 
 * TODO KHA/TSA redesign based on MergeTreeComponenet.
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class EditPersonOrGroupRolesMultiComponent extends EditComponent {
	
	private static final String SECURITY_MODULE_CHANNEL = "securityModule";

	/**
	 * Configuration options for {@link EditPersonOrGroupRolesMultiComponent}.
	 */
	public interface Config extends EditComponent.Config {

		/**
		 * {@link ModelSpec} delivering the security module to configure.
		 */
		@Mandatory
		@Name(SECURITY_MODULE_CHANNEL)
		ModelSpec getSecurityModule();

		@Override
		@StringDefault(ApplyRolesHandler.COMMAND_ID)
		String getApplyCommand();
	}

	/**
	 * @see #channels()
	 */
	@SuppressWarnings("hiding")
	protected static final Map<String, ChannelSPI> CHANNELS =
		channels(EditComponent.CHANNELS, new DefaultChannelSPI(SECURITY_MODULE_CHANNEL, null));

	public static final String TREE_FIELD    = "tree";
	public static final String COL_CHECKBOX  = "inherit";
	public static final String COL_SELECTION = "role";
	
    /** XML layout definition attribute to set if we use 
     * role multi selection */
    protected static final String ROLE_MULTI_ATT = "multiselect";

	private static final ChannelListener SECURITY_MODULE_CHANGE_HANDLER = (sender, oldValue, newValue) -> {
		LayoutComponent component = sender.getComponent();
		((FormComponent) component).removeFormContext();
		component.invalidate();
	};

	private TLTreeModel treeModel;
	private Collection<?> oldExpansionModel;

	public EditPersonOrGroupRolesMultiComponent(InstantiationContext context, Config atts) throws ConfigurationException {
	    super(context, atts);
    }
	
	@Override
	protected Map<String, ChannelSPI> channels() {
		return CHANNELS;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		ComponentChannel securityModuleChannel = getChannel(SECURITY_MODULE_CHANNEL);
		ChannelLinking channelLinking = getChannelLinking(((Config) getConfig()).getSecurityModule());
		securityModuleChannel.linkChannel(log, this, channelLinking);
		securityModuleChannel.addListener(SECURITY_MODULE_CHANGE_HANDLER);
	}

	@Override
	public FormContext createFormContext() {
        FormContext theFC = new FormContext("treeForm", getResPrefix());
        
        // Setup model
		TreeUIModel uiModel = new DefaultStructureTreeUIModel(this.getTreeModel(), false);
        FormTree    theFormTree = new FormTree(TREE_FIELD, getResPrefix(), uiModel, new RolesNodeGroupInitializer());
        
        DefaultTableDeclaration theTD = new DefaultTableDeclaration(this.getResPrefix(), new String[] {});
		theTD.addColumnDeclaration(COL_SELECTION,
			new DefaultColumnDeclaration(SelectionControlProvider.SELECTION_INSTANCE));
        
        if (this.isInEditMode()) {
        	theTD.addColumnDeclaration(COL_CHECKBOX,  new DefaultColumnDeclaration(DefaultFormFieldControlProvider.INSTANCE));
        }
        
        theTD.setHasHeader(true);

        TreeTableRenderer theTreeTableRenderer = new TreeTableRenderer(DefaultTreeImageProvider.INSTANCE, theTD) {
        	@Override
			protected void writeNodeClassesContent(TagWriter out, NodeContext nodeContext) throws IOException {
        		Object node = nodeContext.currentNode();
        		TreeControl tree = nodeContext.getTree();
        		boolean isLeaf = tree.getModel().isLeaf(node);
        		if (isLeaf) {
					out.append(DefaultRowClassProvider.TR_EVEN_CSS_CLASS);
        		} else {
					out.append(DefaultTableRenderer.TABLE_ROW_CSS_CLASS);
        		}
			}
		};
        theFormTree.setTreeRenderer(theTreeTableRenderer);
        theFC.addMember(theFormTree);

        this.createInnerNodeGroups(theFormTree.getTreeModel(), theFormTree.getTreeModel().getRoot());
        
        if (this.oldExpansionModel != null) {
        	TreeUIModelUtil.setExpansionModel(this.oldExpansionModel, theFormTree.getTreeModel());
        }

        return theFC;
	}
	
	@Override
	public synchronized void removeFormContext() {
		if (this.hasFormContext()) {
            // in this case we have to preserve the tree expansion state.
            // in order to do so, we have to remember the old state, create a new form context, and set the expansion state
            FormContext theOldContext = this.getFormContext();
            
            if (theOldContext != null) {
                FormTree theTree = (FormTree) theOldContext.getFirstMemberRecursively(TREE_FIELD);
                
                if (theTree != null) {
                    this.oldExpansionModel = TreeUIModelUtil.getExpansionModel(theTree.getTreeModel());
                }
            }
            this.treeModel = null;
        }
		super.removeFormContext();
	}
	
	@Override
	protected boolean supportsInternalModel(Object anObject) {
		if (! super.supportsInternalModel(anObject)) {
			return false;
		}
		
		return anObject instanceof Person || anObject instanceof Group;
	}
	
    public TLTreeModel getTreeModel() {
        if (this.treeModel == null) {
            this.treeModel = this.createTreeModel();
        }
        return this.treeModel;
    }

    private TLTreeModel createTreeModel() {
		TLModule securityModule = getSecurityModule();

		List<TLTreeModel<?>> models = new ArrayList<>();
		for (TLModuleSingleton link : securityModule.getSingletons()) {
			models.add(new StructuredElementTreeModel((StructuredElement) link.getSingleton()));
		}

		return new ComposingTreeModel(models);
    }

	TLModule getSecurityModule() {
		return (TLModule) getChannel(SECURITY_MODULE_CHANNEL).get();
	}
    
    private void createInnerNodeGroups(TreeUIModel aTree, Object aNode) {
        if ( ! aTree.isLeaf(aNode) ) {
            for (Iterator theIt = aTree.getChildIterator(aNode); theIt.hasNext();) {
                Object theChild = theIt.next();
                this.createInnerNodeGroups(aTree, theChild);
            }
        }
    }
    
	public class RolesNodeGroupInitializer implements NodeGroupInitializer {

		private Group _group;

		private List<BoundedRole> _possibleRoles;

		/**
		 * Creates a {@link EditPersonOrGroupRolesMultiComponent.RolesNodeGroupInitializer}.
		 */
		public RolesNodeGroupInitializer() {
			Object model = getModel();
			if (model instanceof Person) {
				_group = ((Person) model).getRepresentativeGroup();
			} else {
				_group = (Group) model;
			}
			
			_possibleRoles = new ArrayList<>(BoundHelper.getInstance().getPossibleRoles(getSecurityModule()));
			Collections.sort(_possibleRoles, WrapperNameComparator.getInstance());
		}

		@Override
		public void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node) {
	        AbstractBoundWrapper theBO     = (AbstractBoundWrapper) node;
			Collection<? extends BoundRole> currRoles =
				_group == null ? Collections.emptyList() : theBO.getRoles(_group);
			SelectField theField =
				FormFactory.newSelectField(COL_SELECTION, _possibleRoles, true, false);
			
			theField.initSelection(CollectionUtil.toList(currRoles));
			theField.setLabel(theBO.getName());
			theField.setOptionComparator(LabelComparator.newCachingInstance());

			BooleanField theChkBox = FormFactory.newBooleanField(COL_CHECKBOX, Boolean.FALSE, false);
			theChkBox.setVisible(isInEditMode());
			
			nodeGroup.addMember(theField);
			nodeGroup.addMember(theChkBox);
        }
	}
}
