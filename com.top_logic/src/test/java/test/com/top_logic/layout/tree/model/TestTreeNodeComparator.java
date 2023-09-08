/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import junit.framework.Test;

import org.apache.commons.collections4.comparators.ComparableComparator;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TreeNodeComparator;
import com.top_logic.layout.tree.model.UserObjectNodeComparator;

/**
 * Test case for {@link TreeNodeComparator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTreeNodeComparator extends BasicTestCase {
	
	private DefaultMutableTLTreeModel model;
	private ArrayList dfsNodes;
	private ArrayList allNodes;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.model = new DefaultMutableTLTreeModel();
		
		int nr = 0;
		DefaultMutableTLTreeNode r = model.getRoot();
		r.setBusinessObject(Integer.valueOf(nr++));
		
		DefaultMutableTLTreeNode r1 = r.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r11 = r1.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r111 = r11.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r12 = r1.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r2 = r.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r3 = r.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r31 = r3.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r311 = r31.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r312 = r31.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r313 = r31.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r32 = r3.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r321 = r32.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r322 = r32.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r33 = r3.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r331 = r33.createChild(Integer.valueOf(nr++));
		DefaultMutableTLTreeNode r3311 = r331.createChild(Integer.valueOf(nr++));
		
		// Prevent unused local variable warning.
		assertNotNull(r111);
		assertNotNull(r12);
		assertNotNull(r2);
		assertNotNull(r311);
		assertNotNull(r312);
		assertNotNull(r313);
		assertNotNull(r321);
		assertNotNull(r322);
		assertNotNull(r3311);
		
		
		dfsNodes = CollectionUtil.toList(new DescendantDFSIterator(model, r));
		dfsNodes.add(0, r);
		
		allNodes = new ArrayList(dfsNodes);
		Collections.shuffle(allNodes, new Random(42));
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		this.model = null;
		this.dfsNodes = null;
		this.allNodes = null;
	}
	
	protected TreeNodeComparator createNodeComparator() {
		return new TreeNodeComparator(model, new UserObjectNodeComparator(ComparableComparator.INSTANCE));
	}

	public void testSort() {
		Collections.sort(allNodes, createNodeComparator());
	
		assertEquals(dfsNodes, allNodes);
	}

	public void testOrder() {
		TreeNodeComparator comparator = createNodeComparator();
		
		int cnt = dfsNodes.size();
		for (int i = 0; i < cnt; i++) {
			for (int j = 0; j < cnt; j++) {
				assertEquals(sign(i - j), sign(comparator.compare(dfsNodes.get(i), dfsNodes.get(j))));	
			}
		}
	}

	private int sign(int n) {
		return n > 0 ? 1 : (n < 0 ? -1 : 0);
	}
	
    static public Test suite () {   
        return TLTestSetup.createTLTestSetup(TestTreeNodeComparator.class);
    }
}
