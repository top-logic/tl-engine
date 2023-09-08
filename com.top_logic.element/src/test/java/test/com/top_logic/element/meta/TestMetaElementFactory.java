/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.ReflectionUtils.ReflectionException;
import test.com.top_logic.element.meta.kbbased.TestKBBasedMetaAttributes;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.element.meta.kbbased.KBBasedMetaElementFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLScope;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test the {@link com.top_logic.element.meta.MetaElementFactory}.
 * 
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class TestMetaElementFactory extends BasicTestCase {
    
	private MetaElementFactory _meFac;

    /**
     * CTor with a test method name
     * 
     * @param name the test method name
     */
    public TestMetaElementFactory(String name) {
        super(name);
    }

	@Override
    protected void setUp() throws Exception {
    	super.setUp();
		_meFac = MetaElementFactory.getInstance();
		// Allocate factory with clean cache.
		clearCache((KBBasedMetaElementFactory) _meFac);
    }

	@Override
	protected void tearDown() throws Exception {
		_meFac = null;
		super.tearDown();
	}

    public void testMetaElementLookup() throws Exception {
		Transaction tx = kb().beginTransaction();
		TLModel tlModel = ModelService.getApplicationModel();
		TLModule module = TLModelUtil.makeModule(tlModel, "testMetaElementLookup");

		TLClass me1 = addME(module, "caseTest");
		me1.setAbstract(true);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TestKBBasedMetaAttributes.createAttribute(kb, me1, "a", TypeSpec.LONG_TYPE, 1, false);
		TestKBBasedMetaAttributes.createAttribute(kb, me1, "A", TypeSpec.STRING_TYPE, 1, false);
		TLClass me2 = addME(module, "CaseTest");
		me2.setAbstract(true);
		TestKBBasedMetaAttributes.createAttribute(kb, me2, "b", TypeSpec.LONG_TYPE, 1, false);
		TestKBBasedMetaAttributes.createAttribute(kb, me2, "B", TypeSpec.STRING_TYPE, 1, false);
		tx.commit();
    	
    	assertTrue(MetaElementUtil.hasLocalMetaAttribute(me1, "a"));
    	assertTrue(MetaElementUtil.hasLocalMetaAttribute(me1, "A"));
    	assertTrue(MetaElementUtil.hasLocalMetaAttribute(me2, "b"));
    	assertTrue(MetaElementUtil.hasLocalMetaAttribute(me2, "B"));

		// Clear cache to have a clean lookup.
		clearCache((KBBasedMetaElementFactory) _meFac);

		TLClass me1Lookup = _meFac.getGlobalMetaElement("caseTest");
		TLClass me2Lookup = _meFac.getGlobalMetaElement("CaseTest");
    	assertNotNull(me1Lookup);
    	assertNotNull(me2Lookup);
		assertNotEquals("Ticket #2296: Wrong type found (case insensitive search).", me1Lookup, me2Lookup);
    	
		assertNotNull(MetaElementUtil.getLocalMetaAttribute(me1Lookup, "a"));
		assertNotNull(MetaElementUtil.getLocalMetaAttribute(me1Lookup, "A"));
		assertNotEquals(MetaElementUtil.getLocalMetaAttribute(me1Lookup, "a"), MetaElementUtil.getLocalMetaAttribute(me1Lookup, "A"));
    	
		assertNotNull(MetaElementUtil.getLocalMetaAttribute(me2Lookup, "b"));
		assertNotNull(MetaElementUtil.getLocalMetaAttribute(me2Lookup, "B"));
		assertNotEquals(MetaElementUtil.getLocalMetaAttribute(me2Lookup, "b"), MetaElementUtil.getLocalMetaAttribute(me2Lookup, "B"));
	}

	public static TLClass addME(TLModule module, TLScope scope, String name) {
		TLClass newClass = TLModelUtil.addClass(module, scope, name);
		TLModelUtil.setGeneralizations(newClass, list(TLModelUtil.tlObjectType(module.getModel())));
		return newClass;
	}

	public static TLClass addME(TLModule module, String name) {
		TLClass newClass = TLModelUtil.addClass(module, name);
		TLModelUtil.setGeneralizations(newClass, list(TLModelUtil.tlObjectType(module.getModel())));
		return newClass;
	}

    public void testGlobalMetaElementCache() throws Exception {
		Transaction tx1 = kb().beginTransaction();

		TLModel tlModel = ModelService.getApplicationModel();
		TLModule module = TLModelUtil.makeModule(tlModel, "testGlobalMetaElementCache");

		// Test the creating of this type
		TLClass theME = addME(module, "globalCacheTest");

		assertNotNull("Failed to create type 'globalCacheTest'!", theME);

		theME = _meFac.getGlobalMetaElement("globalCacheTest");
		assertNotNull("Type 'globalCacheTest' not found via getGlobalMetaElement()!", theME);
    	
    	inThread(new Execution() {
            
            @Override
			public void run() throws Exception {
                try {
					_meFac.getGlobalMetaElement("globalCacheTest");

                    fail("getGlobalMetaElement('globalCacheTest') didn't throw an IllegalArgumentException in parallel thread!");
                }
                catch (IllegalArgumentException ex) {
                }
            }
        });
		tx1.commit();

		theME = _meFac.getGlobalMetaElement("globalCacheTest");

		assertNotNull("Type 'globalCacheTest' not found via getGlobalMetaElement()!", theME);

	    inThread(new Execution() {

            @Override
			public void run() throws Exception {
				assertNotNull("Type 'globalCacheTest' not found in parallel thread after commit!",
					_meFac.getGlobalMetaElement("globalCacheTest"));
            }
        });

		// Now test the deleting of this type
        if (theME instanceof Wrapper) {
			Transaction tx2 = kb().beginTransaction();

            Wrapper theWrapper = (Wrapper) theME;

            theWrapper.tDelete();

            try {
				theME = _meFac.getGlobalMetaElement("globalCacheTest");

				fail(
					"getGlobalMetaElement('globalCacheTest') didn't throw an IllegalArgumentException after deleting the type!");
            }
            catch (IllegalArgumentException ex) {
            }

            inThread(new Execution() {
                
                @Override
				public void run() throws Exception {
					assertNotNull(
						"Type 'globalCacheTest' not found in parallel thread before commiting the deleted type!",
						_meFac.getGlobalMetaElement("globalCacheTest"));
                }
            });
			tx2.commit();

            try {
				theME = _meFac.getGlobalMetaElement("globalCacheTest");

				fail(
					"getGlobalMetaElement('globalCacheTest') didn't throw an IllegalArgumentException after deleting the type and commiting!");
            }
            catch (IllegalArgumentException ex) {
            }

            inThread(new Execution() {
                
                @Override
				public void run() throws Exception {
                    try {
						_meFac.getGlobalMetaElement("globalCacheTest");

						fail(
							"getGlobalMetaElement('globalCacheTest') didn't throw an IllegalArgumentException in parallel thread after deleting type!");
                    }
                    catch (IllegalArgumentException ex) {
                    }
                }
            });
    	}
    }

	private static KnowledgeBase kb() {
		return KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
	}

	/**
	 * Clears the internal cache of the given {@link KBBasedMetaElementFactory}.
	 * 
	 * @param meFactory
	 *        The {@link KBBasedMetaElementFactory} to clean cache in.
	 */
	private static void clearCache(KBBasedMetaElementFactory meFactory) throws ReflectionException {
		ReflectionUtils.getValue(meFactory, "meCache", Map.class).clear();
	}
    
    /** 
     * Return the suite of tests to perform. 
     * 
     * @return the suite
     */
    public static Test suite () {
        TestSuite theSuite = new TestSuite(TestMetaElementFactory.class);
        return ElementWebTestSetup.createElementWebTestSetup(theSuite);
    }

}
