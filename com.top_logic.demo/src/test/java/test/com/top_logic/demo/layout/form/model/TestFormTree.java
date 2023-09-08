/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.form.model;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.layout.tree.TDListener;
import test.com.top_logic.layout.tree.model.AbstractTLTreeModelTest;
import test.com.top_logic.layout.tree.model.DummyTreeUIModel;

import com.top_logic.demo.knowledge.test.layout.EditTreeDemo;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormMemberVisitor;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.tree.TreeDataListener;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * Test the {@link FormTree}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestFormTree extends AbstractTLTreeModelTest<DefaultMutableTLTreeNode> {

	private ArrayList<Object> requestedNodes;
	private FormTree formTree;
	private TreeUIModel treeUIModel;

    /** 
     * Creates a TestFormTree for functionName.
     */
    public TestFormTree(String functionName) {
        super(functionName);
    }

	@Override
	protected AbstractMutableTLTreeModel<DefaultMutableTLTreeNode> createTreeModel() {
		return new DefaultMutableTLTreeModel();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		requestedNodes = new ArrayList<>();
		formTree = new FormTree("tree", ResPrefix.forTest("tree."), new DefaultStructureTreeUIModel(treemodel), new NodeGroupInitializer() {
			@Override
			public void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node) {
				 requestedNodes.add(node);
			}
		});
		treeUIModel = formTree.getTreeModel();
	}

	@Override
	protected void tearDown() throws Exception {
		requestedNodes = null;
		formTree = null;
		treeUIModel = null;
		super.tearDown();
	}

	protected void simulateRendering() {
		requestVisibleNodes(treeUIModel, treeUIModel.getRoot());
		requestedNodes.clear();
	}
	
    private void requestVisibleNodes(TreeUIModel model, Object node) {
    	model.getUserObject(node);
    	
    	if (model.isExpanded(node)) {
    		for (Object child : model.getChildren(node)) {
    			requestVisibleNodes(model, child);
    		}
    	}
	}

	public void testMoveSubStructure() {
		DefaultMutableTLTreeNode child_1_1_1 = child_1_1.createChild("1_1_1");
		DefaultMutableTLTreeNode child_1_1_2 = child_1_1.createChild("1_1_2");
		DefaultMutableTLTreeNode child_1_1_2_1 = child_1_1_2.createChild("1_1_2_1");
		DefaultMutableTLTreeNode child_1_1_2_2 = child_1_1_2.createChild("1_1_2_2");
        
        child_2.createChild("2_1");
        child_2.createChild("2_2");
        
        assertEquals(1, child_1.getChildCount());
        assertEquals(2, child_1_1.getChildCount());
        assertEquals(2, child_2.getChildCount());

        Object g_1_1_2_1 = treeUIModel.getUserObject(child_1_1_2_1);
        Object g_1_1_2_2 = treeUIModel.getUserObject(child_1_1_2_2);
        simulateRendering();
        child_1_1_2.clearChildren();
        
        Object g_1_1 = treeUIModel.getUserObject(child_1_1);
        Object g_1_1_1 = treeUIModel.getUserObject(child_1_1_1);
        Object g_1_1_2 = treeUIModel.getUserObject(child_1_1_2);
        simulateRendering();
        child_1_1.moveTo(child_2, 1);
        
        // Group for child_2 has been created during the move.
        assertEquals(1, requestedNodes.size());
        requestedNodes.clear();
        
        assertSame(child_1_1, treeUIModel.getChildren(child_2).get(1));
        assertSame(child_1_1_1, treeUIModel.getChildren(child_1_1).get(0));
        assertSame(child_1_1_2, treeUIModel.getChildren(child_1_1).get(1));
        assertSame(0, treeUIModel.getChildren(child_1_1_2).size());
        assertSame(g_1_1, treeUIModel.getUserObject(child_1_1));
        assertSame(g_1_1_1, treeUIModel.getUserObject(child_1_1_1));
        assertSame(g_1_1_2, treeUIModel.getUserObject(child_1_1_2));
        
		DefaultMutableTLTreeNode child_1_1_2_1_n = child_1_1_2.createChild("1_1_2_1");
		DefaultMutableTLTreeNode child_1_1_2_2_n = child_1_1_2.createChild("1_1_2_2");
        
		assertNull(treeUIModel.getUserObject(child_1_1_2_1));
		assertNull(treeUIModel.getUserObject(child_1_1_2_2));
        assertNotSame(g_1_1_2_1, treeUIModel.getUserObject(child_1_1_2_1_n));
        assertNotSame(g_1_1_2_2, treeUIModel.getUserObject(child_1_1_2_2_n));
        // Again:
        assertEquals(2, requestedNodes.size());
        
        assertEquals(0, child_1.getChildCount());
        assertEquals(3, child_2.getChildCount());
        assertEquals(child_1_1, child_2.getChildAt(1));
        assertEquals(2, child_1_1.getChildCount());
    }
    
    static TLTreeModel createTestModel() {
        return EditTreeDemo.createDemoRoot(); 
     }
    
    /**
     * Test minimal usage of FormTree.
     */
    public void testMinimalTree() {
        TLTreeModel          testModel  = createTestModel();
        TreeUIModel          uiModel    = new DefaultStructureTreeUIModel(testModel);
        FormTree             formTree   = new FormTree("TestFormTree",ResPrefix.forTest("test.from.tree."), uiModel, NodeGroupInitializer.EMPTY_GROUP_INITIALIZER);
        
        assertSame(NodeGroupInitializer.EMPTY_GROUP_INITIALIZER, formTree.getNodeGroupProvider());
        
        assertFalse(formTree.getMembers().hasNext());
        TreeDataListener tdl = new TDListener();
        assertTrue(formTree.addTreeDataListener(tdl));
        assertTrue(formTree.removeTreeDataListener(tdl));
        assertTrue(formTree.addTreeDataListener(tdl));
        
        assertNull(formTree.findNodeGroup(null));
        assertNull(formTree.findNodeGroup(testModel.getRoot())); // no expanded, yet

    }

    /**
     * Test functions concerning the TreeUIModel.
     */
    public void testUIModel() {
        TLTreeModel          testModel  = createTestModel();
        TreeUIModel          uiModel    = new DefaultStructureTreeUIModel(testModel);
        FormTree             formTree   = new FormTree("TestFormTree",ResPrefix.forTest("test.from.tree."), uiModel,NodeGroupInitializer.EMPTY_GROUP_INITIALIZER);
        
        TDListener tdl = new TDListener();
        formTree.addTreeDataListener(tdl);
        
        formTree.setTreeModel(uiModel);
        assertNull("Setting same Model should be noop", tdl.newObj);
        
        uiModel    = new DefaultStructureTreeUIModel(testModel);
        formTree.setTreeModel(uiModel);
        assertSame("Setting new model does not trigger (correct) event", formTree.getTreeModel(), tdl.newObj);
        tdl.clear();
    }

    /**
     * Test unsupported or exceptional behavior here.
     */
    public void testExceptions() {
        TreeUIModel uiModel = new DefaultStructureTreeUIModel(createTestModel());
        FormTree formTree = new FormTree("TestFormTree",ResPrefix.forTest("test.from.tree."), uiModel, NodeGroupInitializer.EMPTY_GROUP_INITIALIZER);
        try {
            formTree.addMember(formTree);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) { /*expected */ }
    }
    
    /**
     * Test method for {@link com.top_logic.layout.form.model.FormTree#visit(com.top_logic.layout.form.FormMemberVisitor, java.lang.Object)}.
     */
    public void testVisit() {
        final FormTree fTree = new FormTree("testVisit", ResPrefix.GLOBAL, new DummyTreeUIModel() , null);
		AbstractFormMemberVisitor afmv = new AbstractFormMemberVisitor<Boolean, Void>() {
            /**
             * @see com.top_logic.layout.form.FormMemberVisitor#visitFormMember(com.top_logic.layout.form.FormMember, java.lang.Object)
             */
            @Override
			public Boolean visitFormMember(FormMember aMember, Void aArg) {
                return Boolean.valueOf(fTree==  aMember);
            }
        };
        assertSame(Boolean.TRUE, fTree.visit(afmv, null));
    }

    /**
     * Get coverage for assertions.
     */
    public void testAsserts() {
        try {
            new FormTree(null, ResPrefix.GLOBAL, null, null);
        } catch (AssertionError expected) { /* expected */ }
        try {
            new FormTree("", ResPrefix.GLOBAL, null, null);
        } catch (AssertionError expected) { /* expected */ }
    }

    public static Test suite() {
		return suite(new TestSuite(TestFormTree.class));
    }

}

