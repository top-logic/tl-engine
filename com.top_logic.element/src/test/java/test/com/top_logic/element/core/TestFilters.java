/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.core;

import static com.top_logic.model.util.TLModelUtil.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.element.core.TLElementFilter;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.ElementTypeFilter;
import com.top_logic.element.core.util.FilteredVisitor;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;


/**
 * Test the different kind of standard filters.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestFilters extends BasicTestCase {

    /** name of structure as configured in xml */
    public static final String TEST_STRUCTURE_NAME = "projElement";

    public TestFilters() {
        super();
    }

    public TestFilters(String aName) {
        super(aName);
    }

    public void testTLElementFilter() throws Exception {
        StructuredElement theRoot   = this.getStructure();
        StructuredElement         theChild  = theRoot.getChildren(null).get(0);
        TLElementFilter   theFilter = new TLElementFilter(theChild);

        Assert.assertNotNull("Unable to get filter for test!", theFilter);

        Assert.assertFalse("null value has been accepted by TLElementFilter!", theFilter.accept(null));
        Assert.assertFalse("No value has been accepted by TLElementFilter!", theFilter.accept("HELLO"));
        Assert.assertFalse("Root element is accepted by TLElementFilter!", theFilter.accept(theRoot));
        Assert.assertTrue("Starting point for filter is not accepted by TLElementFilter!", theFilter.accept(theChild));

        Collection theList = theFilter.getElementList();

        Assert.assertNotNull("No list available from getElementList!", theList);

        try {
            theList.add(theFilter.toString());

            Assert.fail("GetElementList returned a modifyable list");
        }
        catch (Exception ex) {
            // expected
        }
    }

    @SuppressWarnings("unused")
	public void testElementTypeFilter() throws Exception {
        try {
			new ElementTypeFilter((Set<String>) null);
            Assert.fail("Can initialize ElementTypeFilter with null");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }

        StructuredElement theRoot   = this.getStructure();
        StructuredElement         theChild  = theRoot.getChildren(null).get(0);
		ElementTypeFilter theFilter = new ElementTypeFilter(localNames(theRoot.getChildrenTypes()));

        Assert.assertNotNull("Unable to get filter for test!", theFilter);

        Assert.assertFalse("null value has been accepted by TLElementFilter!", theFilter.accept(null));
        Assert.assertFalse("No value has been accepted by TLElementFilter!", theFilter.accept("HELLO"));
        Assert.assertFalse("Root element is accepted by TLElementFilter!", theFilter.accept(theRoot));
        Assert.assertTrue("Starting point for filter is not accepted by TLElementFilter!", theFilter.accept(theChild));
    }

    /**
     * Test the {@link FilteredVisitor}
     */
    public void testFilteredVisitor() throws Exception {
        try {
            new FilteredVisitor(null);
            fail("Can initialize FilteredVisitor with null");
        }
        catch (IllegalArgumentException ex) { /* expected */ }

        StructuredElement theRoot    = this.getStructure();
        StructuredElement         theChild   = theRoot.getChildren(null).get(0);
        TLElementFilter   theFilter  = new TLElementFilter(theChild);
        FilteredVisitor   theVisitor = new FilteredVisitor(theFilter);

        assertTrue("Failed to traverse " + theRoot, 
                    TraversalFactory.traverse(theRoot, theVisitor, TraversalFactory.DEPTH_FIRST));

        List theList = theVisitor.getResult();

        assertNotNull("Result of visitor is null!", theList);
        assertFalse("Result of visitor is empty!" , theList.isEmpty());

        assertFalse("Root element is in result list!", theList.contains(theRoot));

        for (Iterator theIt = theList.iterator(); theIt.hasNext();) {
            StructuredElement theElement = (StructuredElement) theIt.next();

            assertTrue("Element " + theElement + " is in list but will be rejected by filter!", 
                              theFilter.accept(theElement));
        }
    }

    /**
     * Test some variants of using the {@link FilteredVisitor}
     */
    public void testFilteredVisitor2() throws Exception {
        StructuredElement theRoot    = this.getStructure();
        Filter<Object>       theFilter  = FilterFactory.falseFilter();
        FilteredVisitor   theVisitor = new FilteredVisitor(theFilter, false, 12);

        assertFalse("Should not traverse anything", 
        		TraversalFactory.traverse(theRoot, theVisitor, TraversalFactory.DEPTH_FIRST, true));

        List theList = theVisitor.getResult();

        assertNotNull("Result of visitor is null!", theList);
        assertTrue   ("Result of visitor is not empty!" , theList.isEmpty());
    }

    /**
     * Create an example Structure for testing.
     */
    protected StructuredElement getStructure() throws Exception {
        StructuredElement theRoot = this.getRoot();

        if (theRoot.getChildren(null).size() == 0) {
            this.createChildren(theRoot, "Child ", 3, 3);
        }

        return (theRoot);
    }

    protected void createChildren(StructuredElement aParent, String aName, int aWidth, int aDepth) {
        if (aDepth < 0) {
            return;
        }
        else {
            StructuredElement theChild;
            String            theName;
            int               theDepth = aDepth - 1;
			List<String> theTypes = localNames(aParent.getChildrenTypes());
       
            for (int thePos = 0; thePos < aWidth; thePos++ ) {
                theName  = aName + thePos;
				theChild = aParent.createChild(theName, theTypes.get(thePos % theTypes.size()));

                this.createChildren(theChild, theName + '.', aWidth, theDepth);
            }
        }
    }

    protected StructuredElement getRoot() throws Exception {
        StructuredElementFactory theFactory = StructuredElementFactory.getInstanceForStructure(TEST_STRUCTURE_NAME);
    
        Assert.assertNotNull("Unable to get factory!", theFactory);
    
		return (theFactory.getRoot());
    }

    /** 
     * Return the suite of tests to perform. 
     */
    public static Test suite() {
        return (ElementWebTestSetup.createElementWebTestSetup(new TestSuite(TestFilters.class)));
    }

}
