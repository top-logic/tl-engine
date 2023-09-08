/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLClass;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.util.list.ListUtil;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TestClassification extends BasicTestCase {
    
    private static final String META_ELEMENT_PROJECT_ALL = "projElement.All";
    
    public void testClassification() throws Exception {
        
        TLClass theME = this.getMetaElement(META_ELEMENT_PROJECT_ALL);
        
		TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theME, "Verantwortlicher");
        
        assertFalse(AttributeOperations.isClassified(theMA));
        assertTrue (AttributeOperations.getClassifiers(theMA).isEmpty());
        
		FastListElement theClassifierOne = FastListElement.getElementByName(ListUtil.TLYESNO_YES);
		FastListElement theClassifierTwo = FastListElement.getElementByName(ListUtil.TLYESNO_NO);
        Collection theClassifiers = new ArrayList();
        theClassifiers.add(theClassifierOne);
        theClassifiers.add(theClassifierTwo);
        
        AttributeOperations.setClassifiers(theMA, theClassifiers);
        
        assertTrue(AttributeOperations.isClassified(theMA));
        assertTrue(CollectionUtil.containsSame(
                theClassifiers, AttributeOperations.getClassifiers(theMA)));
        
        AttributeOperations.setClassifiers(theMA, Collections.singletonList(theClassifierOne));
        
        assertTrue(AttributeOperations.isClassified(theMA));
        assertTrue(CollectionUtil.containsSame(
                Collections.singletonList(theClassifierOne), AttributeOperations.getClassifiers(theMA)));
        
        ((Wrapper) theMA).getKnowledgeBase().rollback();
        
        assertFalse(AttributeOperations.isClassified(theMA));
        assertTrue (AttributeOperations.getClassifiers(theMA).isEmpty());
    }
    
    private TLClass getMetaElement(String aName) {
        for (Iterator theIt = MetaElementFactory.getInstance().getAllMetaElements().iterator(); theIt.hasNext(); ) {
            TLClass theME = (TLClass) theIt.next();
            if (theME.getName().equals(aName)) {
                return theME;
            }
        }
        return null;
    }

    /**  Return the suite of tests to perform. */
    public static Test suite() {
        TestSuite suite = new TestSuite(TestClassification.class);

        return ElementWebTestSetup.createElementWebTestSetup(suite);
    }

    /**
     * Main function for direct testing.
     */
    public static void main(String[] args) throws IOException {
        Logger.configureStdout(); // Set to "INFO" to see failed commits
        junit.textui.TestRunner.run (suite ());
    } 

}

