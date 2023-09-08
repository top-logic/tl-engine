/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.knowledge.test.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.demo.layout.demo.DummyEditComponent;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.component.model.NoSingleSelectionModel;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractStringConstraint;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.control.OnVisibleControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TreeField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbControl;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbDataOwner.NoBreadcrumbDataOwner;
import com.top_logic.layout.tree.breadcrumb.DefaultBreadcrumbRenderer;
import com.top_logic.layout.tree.breadcrumb.MutableBreadcrumbData;
import com.top_logic.layout.tree.model.AbstractTLTreeNodeBuilder;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.DefaultColumnDeclaration;
import com.top_logic.layout.tree.renderer.DefaultTableDeclaration;
import com.top_logic.layout.tree.renderer.DefaultTreeImageProvider;
import com.top_logic.layout.tree.renderer.DefaultTreeRenderer;
import com.top_logic.layout.tree.renderer.TreeTableRenderer;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Example how an editable Tree can be used.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class EditTreeDemo extends DummyEditComponent {

    public static final String NODE_NAME_PREFIX = "Node_";


	static final List<String> SELECTIONS = Arrays.asList(
        new String[] {
                "A", "B", "C", "D" , "E", "F", "G"});


	public static final String NODE_NAME_FIELD = "nodeName";


	public static final TreeBuilder INFINITE_TREE = new AbstractTLTreeNodeBuilder() {

		@Override
		public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node) {
			int cnt;
			if (node.getParent() == null) {
				cnt = 5;
			} else {
				cnt = (int) (Math.random() * 10);
			}
			
			List<DefaultMutableTLTreeNode> result = new ArrayList<>();
			for (int n = 0; n < cnt; n++) {
				result.add(createNode(node.getModel(), node, node.getBusinessObject() + "." + n));
			}
			return result;
		}

		@Override
		public boolean isFinite() {
			return false;
		};

	};

    /**
     * Create a new EditTreeComponent from XML.
     */
    public EditTreeDemo(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }

    /**
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        FormContext  result = new FormContext("treeForm", getResPrefix());

        // Setup model
		final TLTreeModel treeModel = (TLTreeModel) getModel();
        TreeUIModel uiModel   = new DefaultStructureTreeUIModel(treeModel);
        final TreeUIModel rootInvisibleModel = new DefaultStructureTreeUIModel(treeModel, false);

		DefaultSingleSelectionModel selectionModel1 = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		TreeField tree1 =
			FormFactory.newTreeField("tree1", uiModel, selectionModel1, DefaultTreeRenderer.INSTANCE);
		result.addMember(tree1);
        // using default Renderer

        FormTree tree2 = createTreeTable("tree2", uiModel);
        result.addMember(tree2);

		final FormTree tree1RootInvisible = new FormTree("tree1RootInvisible",getResPrefix(), rootInvisibleModel, NodeGroupInitializer.EMPTY_GROUP_INITIALIZER);
		result.addMember(tree1RootInvisible);

        /*
         * The selection model of tree2, tree3 and tree1RootInvisible must be the same to receive
         * selection events when selecting the other tree.
         */
		final DefaultSingleSelectionModel singleSelectionModel =
			new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
        tree2.setSelectionModel(singleSelectionModel);
        tree1RootInvisible.setSelectionModel(singleSelectionModel);
        
        FormTree tree3 = new FormTree("tree3", getResPrefix(), new DefaultStructureTreeUIModel(treeModel), NodeGroupInitializer.EMPTY_GROUP_INITIALIZER);
		tree3.setControlProvider(new ControlProvider() {
        	
        	/**
        	 * Creates a breadcrumb control whose last node is always also selected
        	 */
        	@Override
			public Control createControl(Object model, String style) {
        		FormTree member = (FormTree) model;
				final OnVisibleControl control = DefaultFormFieldControlProvider.createOnVisibleControl(member);
				control.setRenderer(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
				final DefaultBreadcrumbRenderer renderer = DefaultBreadcrumbRenderer.defaultRenderer();
        		final BreadcrumbControl breadcrumb =
					new BreadcrumbControl(renderer,
						new MutableBreadcrumbData(treeModel, singleSelectionModel, singleSelectionModel,
						NoBreadcrumbDataOwner.INSTANCE));
				control.addChild(breadcrumb);
        		return control;
        	}
        });
        tree3.setSelectionModel(singleSelectionModel);
        result.addMember(tree3);
        
        FormTree tree4 = new FormTree("tree4", getResPrefix(), new DefaultStructureTreeUIModel(treeModel), NodeGroupInitializer.EMPTY_GROUP_INITIALIZER);
		tree4.setControlProvider(new ControlProvider() {
        	
        	/**
        	 * Creates a breadcrumb control whose last node is independent of the selection 
        	 */
        	@Override
			public Control createControl(Object model, String style) {
        		FormTree member = (FormTree) model;
				final OnVisibleControl control = DefaultFormFieldControlProvider.createOnVisibleControl(member);
				control.setRenderer(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
				final DefaultBreadcrumbRenderer renderer = DefaultBreadcrumbRenderer.defaultRenderer();
				final DefaultSingleSelectionModel lastNodeModel =
					new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
				final BreadcrumbControl breadcrumb =
					new BreadcrumbControl(renderer,
						new MutableBreadcrumbData(treeModel, singleSelectionModel, lastNodeModel,
							NoBreadcrumbDataOwner.INSTANCE));
        		control.addChild(breadcrumb);
        		return control;
        	}
        });
        tree4.setSelectionModel(singleSelectionModel);
        result.addMember(tree4);
        
        FormTree tree5 = new FormTree("tree5", getResPrefix(), new DefaultStructureTreeUIModel(treeModel), NodeGroupInitializer.EMPTY_GROUP_INITIALIZER);
		tree5.setControlProvider(new ControlProvider() {
        	
        	/**
        	 * Creates a breadcrumb control without selection
        	 */
        	@Override
			public Control createControl(Object model, String style) {
        		FormTree member = (FormTree) model;
				final OnVisibleControl control = DefaultFormFieldControlProvider.createOnVisibleControl(member);
				control.setRenderer(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
				final DefaultBreadcrumbRenderer renderer = DefaultBreadcrumbRenderer.defaultRenderer();
        		final DefaultSingleSelectionModel lastNodeModel = singleSelectionModel;
				final BreadcrumbControl breadcrumb =
					new BreadcrumbControl(renderer,
						new MutableBreadcrumbData(treeModel, NoSingleSelectionModel.SINGLE_SELECTION_INSTANCE,
							lastNodeModel, NoBreadcrumbDataOwner.INSTANCE));
        		control.addChild(breadcrumb);
        		return control;
        	}
        });
        tree5.setSelectionModel(singleSelectionModel);
        result.addMember(tree5);
        
		final CommandField moveLevelUpCommand = FormFactory.newCommandField("moveLevelUp", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				final DefaultMutableTLTreeNode selectedNode = getNamedOrSelected(treeModel, singleSelectionModel);
				Object root = treeModel.getRoot();
				if (!root.equals(selectedNode) && !root.equals(selectedNode.getParent())) {
					selectedNode.moveTo(selectedNode.getParent().getParent());
				}
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		moveLevelUpCommand.setLabel("Moves selected Node a level up");
		result.addMember(moveLevelUpCommand);

		final CommandField moveLevelDownCommand = FormFactory.newCommandField("moveLevelDown", new Command() {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				final DefaultMutableTLTreeNode selectedNode = getNamedOrSelected(treeModel, singleSelectionModel);
				if (!treeModel.getRoot().equals(selectedNode)) {
					List<? extends DefaultMutableTLTreeNode> children = selectedNode.getParent().getChildren();
					if (children.size() > 1) {
						final int indexOf = children.indexOf(selectedNode);
						if (indexOf < children.size() - 1) {
							selectedNode.moveTo(children.get(indexOf + 1));
						} else {
							selectedNode.moveTo(children.get(0));
						}
					}
				}
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		moveLevelDownCommand.setLabel("Moves selected Node a level down");
		result.addMember(moveLevelDownCommand);
		
        final CommandField moveUpCommand = FormFactory.newCommandField("moveUp", new Command() {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				final DefaultMutableTLTreeNode selectedNode = getNamedOrSelected(treeModel, singleSelectionModel);
				if (!treeModel.getRoot().equals(selectedNode)) {
					final int indexOf = selectedNode.getParent().getChildren().indexOf(selectedNode);
					if (indexOf > 0) {
						selectedNode.moveTo(selectedNode.getParent(), indexOf -1);
					} else {
						selectedNode.moveTo(selectedNode.getParent());
					}
				}
				return HandlerResult.DEFAULT_RESULT; 
			}
		});
        moveUpCommand.setLabel("Moves selected Node up");
		result.addMember(moveUpCommand);

		final CommandField removeNode = FormFactory.newCommandField("remove", new Command() {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				DefaultMutableTLTreeNode selectedNode = getNamedOrSelected(treeModel, singleSelectionModel);
				if (selectedNode != null && !treeModel.getRoot().equals(selectedNode)) {
					DefaultMutableTLTreeNode parent = selectedNode.getParent();
					parent.removeChild(parent.getIndex(selectedNode));
				}
				return HandlerResult.DEFAULT_RESULT; 
			}
		});
		removeNode.setLabel("Removes node.");
		result.addMember(removeNode);
		final StringField nodeName = FormFactory.newStringField(NODE_NAME_FIELD, null, false);
		nodeName.setLabel("Select node to change");
		nodeName.addConstraint(new AbstractStringConstraint() {
			
			@Override
			protected boolean checkString(String value) throws CheckException {
				if (findNode(treeModel, value) == null) {
					throw new CheckException("Not a valid node");
				}
				return false;
			}
		});
		result.addMember(nodeName);
		
		final CommandField createChild = FormFactory.newCommandField("createChild", new Command() {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				final DefaultMutableTLTreeNode selectedNode = getNamedOrSelected(treeModel, singleSelectionModel);
				
				if (selectedNode != null && treeModel.containsNode(selectedNode)) {
					DefaultMutableTLTreeNode root = (DefaultMutableTLTreeNode) treeModel.getRoot();
					String newName;
					List<? extends DefaultMutableTLTreeNode> children = selectedNode.getChildren();
					if (children.isEmpty()) {
						final String userObject = (String) selectedNode.getBusinessObject();
						if (root.equals(selectedNode)) {
							newName = NODE_NAME_PREFIX + "1";
						} else {
							newName = userObject + ".1";
						}
					} else {
						final DefaultMutableTLTreeNode last = CollectionUtil.getLast(children);
						final String userObject = (String) last.getBusinessObject();
						if (root.equals(selectedNode)) {
							int childNumber = Integer.parseInt(userObject.substring(NODE_NAME_PREFIX.length()));
							newName = NODE_NAME_PREFIX + String.valueOf(childNumber + 1);
						} else {
							int lastIndexOf = userObject.lastIndexOf('.');
							int childNumber = Integer.parseInt(userObject.substring(lastIndexOf + 1));
							newName = userObject.substring(0, lastIndexOf + 1) + String.valueOf(childNumber + 1);
						}
					}
					selectedNode.createChild(newName);
				}
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		createChild.setLabel("Creates a child of the selected node.");
		result.addMember(createChild);
		
		final CommandField reset = FormFactory.newCommandField("reset", new Command() {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				invalidate();
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		reset.setLabel("Resets tree");
		result.addMember(reset);
        
        return result;
    }

	TLTreeNode getNamedNode(TLTreeModel treeModel) {
		StringField nodeNameField = (StringField) getFormContext().getFirstMemberRecursively(NODE_NAME_FIELD);
		TLTreeNode findNode = findNode(treeModel, nodeNameField.getAsString());
		nodeNameField.reset();
		return findNode;
    }

	DefaultMutableTLTreeNode getNamedOrSelected(TLTreeModel treeModel, SingleSelectionModel selectionModel) {
		TLTreeNode namedNode = getNamedNode(treeModel);
		if (namedNode != null) {
			return (DefaultMutableTLTreeNode) namedNode;
		} else {
			return (DefaultMutableTLTreeNode) selectionModel.getSingleSelection();
		}
	}

	protected static TLTreeNode findNode(TLTreeModel treeModel, String value) {
		return findNode(value, (DefaultMutableTLTreeNode) treeModel.getRoot());
	}

	public static DefaultMutableTLTreeNode findNode(String value, DefaultMutableTLTreeNode root) {
		if (root.getBusinessObject().equals(value)) {
			return root;
		}
		if (!root.isInitialized()) {
			// The tree is infinite so it must be checked whether the node was expanded before
			return null;
		}
		List<? extends DefaultMutableTLTreeNode> children = root.getChildren();
		for (DefaultMutableTLTreeNode child : children) {
			DefaultMutableTLTreeNode sub = findNode(value, child);
			if (sub != null) {
				return sub;
			}
		}
		return null;
	}

	private FormTree createTreeTable(String name, TreeUIModel uiModel) {
		Renderer<Object> r = new Renderer<>() {
            @Override
			public void write(DisplayContext context, TagWriter out, Object node) throws IOException {
                out.writeContent(StringServices.getRandomString(32));
            }
        };
        String fieldNames[] = new String[] { "checked", "string" ,"select" };

        DefaultTableDeclaration tree2Table = new DefaultTableDeclaration(
                getResPrefix(), fieldNames);
        tree2Table.setHasHeader(true);

        // Example of using a Renderer
        tree2Table.addColumnDeclaration("static" , new DefaultColumnDeclaration(r));

        FormTree tree2 = new FormTree(name, getResPrefix(),uiModel, new SimpleGroupInitializer());
        tree2.setTreeRenderer(new TreeTableRenderer(DefaultTreeImageProvider.INSTANCE, tree2Table));

		return tree2;
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(createDemoRoot());
		}
		return super.validateModel(context);
	}

    /**
     * Setup a NodeGroup with a Checkbox, StringField and SelectField.
     */
    public final class SimpleGroupInitializer implements NodeGroupInitializer {

        @Override
		public void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node)
        {
            nodeGroup.addMember(FormFactory.newBooleanField("checked"));
            nodeGroup.addMember(FormFactory.newStringField("string"));
            nodeGroup.addMember(FormFactory.newSelectField("select", SELECTIONS));
        }
    }

    /** Create a Demo Model  */
    public static DefaultMutableTLTreeModel createDemoRoot() {
		return new DefaultMutableTLTreeModel(INFINITE_TREE, "root");
    }

}

