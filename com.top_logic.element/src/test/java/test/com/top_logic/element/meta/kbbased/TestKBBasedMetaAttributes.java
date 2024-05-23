/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.meta.OrderedListHelper;
import test.com.top_logic.element.meta.TestMetaElementFactory;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.element.config.PartConfig;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.config.annotation.TLValidityCheck;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.element.meta.ValidityCheck;
import com.top_logic.element.meta.kbbased.PersistentObjectImpl;
import com.top_logic.element.meta.kbbased.storage.PrimitiveStorage;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.config.CreateStructuredTypePart;
import com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.Visibility;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;


/**
 * Test the attribute implementation based on the KnowledgeBase.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class TestKBBasedMetaAttributes extends BasicTestCase {

	/** attribute names. */
    private static final String COLLECTION_NAME     = "Collection";
    private static final String LIST_NAME           = "List";
    private static final String CLASSIFICATION_NAME = "Classification";
    private static final String SUB_STRING_NAME = "SubString";
    private static final String SUPER_LONG_NAME = "SuperLong";
    private static final String TYPED_SET_NAME = "TypedSet";

	private static final String FAST_LIST_NAME = "fastList";

	private static final String WRAPPER_NAME = "wrapper";
    private static final String STRING_NAME = "String";
    private static final String LONG_NAME = "Long";
    private static final String FLOAT_NAME = "Float";
    private static final String DATE_NAME = "Date";
    private static final String BINARY_NAME = "Binary";
    
    public static final String BOOLEAN_NAME = "Boolean";
    
    /** ID of created example list. */
    public static final String EXAMPLE_LIST = "exampleList";
    /** Value used for Float attribute. */
	public static Float VAL_FLOAT = Float.valueOf(1234.56F);
    /** Value used for String attribute. */
	public static String  VAL_STRING      = "a_string";
    /** Value used for Date attribute. */
	public static Date    VAL_DATE        = new Date();
    /** Value used for Long attribute. */
	public static Long VAL_LONG = Long.valueOf(4567);
	/** Value used for Boolean attribute. */
	public static Boolean VAL_BOOL        = Boolean.TRUE;
    /** Value used for Boolean attribute. */
	public static byte[]  VAL_BINARY      = new byte[] {0, 23, 112, 34, -19, -100};

    /**
	 * CTor with a test method name
	 * 
	 * @param name the test method name
	 */
	public TestKBBasedMetaAttributes(String name) {
		super(name);
	}

	public void testChangeConfiguration() throws DuplicateAttributeException, IllegalArgumentException,
			KnowledgeBaseException, NoSuchAttributeException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		Transaction tx1 = kb.beginTransaction();
		TLModel tlModel = ModelService.getApplicationModel();
		TLModule module =
			TLModelUtil.makeModule(tlModel, TestKBBasedMetaAttributes.class.getName() + ".testChangeConfiguration");
		TLClass thisME = TestMetaElementFactory.addME(module, "Foo.Super");
		thisME.setAbstract(true);
		tx1.commit();

		Transaction tx2 = kb.beginTransaction();

		AttributeConfig config = TypedConfiguration.newConfigItem(AttributeConfig.class);
		config.setName("s1");
		config.setTypeSpec(TypeSpec.STRING_TYPE);
		DisplayAnnotations.setSortOrder(config, 6.0);
		DisplayAnnotations.setDeleteProtected(config);
		config.getAnnotations().add(DisplayAnnotations.hidden());
		createAttribute(kb, thisME, config);
		tx2.commit();
		
		Transaction tx3 = kb.beginTransaction();
		TLStructuredTypePart metaAttribute = MetaElementUtil.getLocalMetaAttribute(thisME, "s1");
		assertTrue(DisplayAnnotations.isHidden(metaAttribute));
		metaAttribute.setAnnotation(DisplayAnnotations.readOnly());
		tx3.commit();

		assertFalse(DisplayAnnotations.isEditable(metaAttribute));
		assertFalse(DisplayAnnotations.isHidden(metaAttribute));

		Transaction tx4 = kb.beginTransaction();
		metaAttribute.setAnnotation(DisplayAnnotations.newVisibility(Visibility.EDITABLE));
		metaAttribute.setAnnotation(DisplayAnnotations.newCreateVisibility(Visibility.READ_ONLY));
		tx4.commit();

		assertTrue(DisplayAnnotations.isEditable(metaAttribute));
		assertFalse(DisplayAnnotations.isEditableInCreate(metaAttribute));
	}

	public void testRevert() throws DuplicateAttributeException, KnowledgeBaseException, NoSuchAttributeException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		Transaction tx1 = kb.beginTransaction();
		TLModel tlModel = ModelService.getApplicationModel();
		TLModule module = TLModelUtil.makeModule(tlModel, TestKBBasedMetaAttributes.class.getName() + ".testRevert");
		TLClass superME = TestMetaElementFactory.addME(module, "Foo.Super");
		superME.setAbstract(true);
		TLClass thisME = TestMetaElementFactory.addME(module, "Foo.This");
		thisME.setAbstract(true);
		MetaElementUtil.setSuperMetaElement(thisME, superME);
		tx1.commit();

		Transaction tx2 = kb.beginTransaction();
		createAttribute(kb, superME, "s1", TypeSpec.STRING_TYPE, 6.0, false);
		tx2.commit();

		Transaction tx3 = kb.beginTransaction();
		createAttribute(kb, thisME, "s2", TypeSpec.STRING_TYPE, 6.0, false);
		tx3.commit();

		// Check that attributes are there.
		assertNotNull(MetaElementUtil.getMetaAttributeOrNull(thisME, "s1"));
		assertNotNull(MetaElementUtil.getMetaAttributeOrNull(thisME, "s2"));

		// Revert creation of attribute in thisME
		Transaction tx4 = kb.beginTransaction();
		KBUtils.revert(kb, tx2.getCommitRevision(), kb.getHistoryManager().getTrunk());
		tx4.commit();

		assertNotNull(MetaElementUtil.getMetaAttributeOrNull(thisME, "s1"));
		assertNull(MetaElementUtil.getMetaAttributeOrNull(thisME, "s2"));

		// Revert creation of attribute in superME
		Transaction tx5 = kb.beginTransaction();
		KBUtils.revert(kb, tx1.getCommitRevision(), kb.getHistoryManager().getTrunk());
		tx5.commit();

		assertNull(MetaElementUtil.getMetaAttributeOrNull(thisME, "s1"));
		assertNull(MetaElementUtil.getMetaAttributeOrNull(thisME, "s2"));

		// Revert deletions of attributes
		Transaction tx6 = kb.beginTransaction();
		KBUtils.revert(kb, tx1.getCommitRevision(), kb.getHistoryManager().getTrunk(), tx3.getCommitRevision(), kb
			.getHistoryManager().getTrunk());
		tx6.commit();

		assertNotNull(MetaElementUtil.getMetaAttributeOrNull(thisME, "s1"));
		assertNotNull(MetaElementUtil.getMetaAttributeOrNull(thisME, "s2"));
	}

    /**
     * Create MetaElements and MetaAttributes.
     * 
     * Tests creation and setting/resetting values and attribute inheritance.
     */
    public void testCompleteScenario() throws Exception {
        // INIT STUFF
        KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
        assertNotNull("KB is null!", theKB);
        
        
		AttributedStructuredElementWrapper theSE = OrderedListHelper.getAttributedWrapper();
        
        startTime();
        
        // SETUP
        
        Transaction tx = theKB.beginTransaction();
        
        // Create a three step hierarchy MetaElements and add them to the list (holder)
		TLModel tlModel = ModelService.getApplicationModel();

		// Get the TLEnumeration that is used as holder, attributes, and classification list ;-)
		TLEnumeration theList = OrderedListHelper.createTestEnumeration(tlModel, EXAMPLE_LIST, false);
		assertNotNull("FastList is null!", theList);
		TLModule module = TLModelUtil.makeModule(tlModel, TestKBBasedMetaAttributes.class.getName());
		TLClass theSuperME = TestMetaElementFactory.addME(module, theSE, OrderedListHelper.SUPER_META_ELEMENT_TYPE);
		TLClass theME = TestMetaElementFactory.addME(module, theSE, OrderedListHelper.META_ELEMENT_TYPE);
		MetaElementUtil.setSuperMetaElement(theME, theSuperME);
		TLClass theSubME = TestMetaElementFactory.addME(module, theSE, OrderedListHelper.SUB_META_ELEMENT_TYPE);
		MetaElementUtil.setSuperMetaElement(theSubME, theME);

        checkMetaHierarchy(theSE, theSuperME, theME, theSubME);
        // Add some MetaAttributes and test their existence
        
        AttributeConfig booleanConfig = propertyConfig(BOOLEAN_NAME, TypeSpec.BOOLEAN_TYPE, 0.0, false);
		setValidity(booleanConfig, "validity:1d;duration:500s");
        TLStructuredTypePart theBoolMA = createAttribute(theKB, theME, booleanConfig);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, BOOLEAN_NAME));
        
		createAttribute(theKB, theME, DATE_NAME, TypeSpec.DATE_TYPE, 3.0, false);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, DATE_NAME));
        
		// The Logged ERROR is correct and can be ignored, here
		String illegalValidityPattern = "-100s";
		AttributeConfig floatConfig = propertyConfig(FLOAT_NAME, TypeSpec.FLOAT_TYPE, 4.0, false);
		setValidity(floatConfig, illegalValidityPattern);
		createAttribute(theKB, theME, floatConfig);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, FLOAT_NAME));
        
		TLStructuredTypePart theLongMA = createAttribute(theKB, theME, LONG_NAME, TypeSpec.LONG_TYPE, 5.0, false);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, LONG_NAME));
        
		createAttribute(theKB, theME, STRING_NAME, TypeSpec.STRING_TYPE, 6.0, false);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, STRING_NAME));
        
		createAttribute(theKB, theME, BINARY_NAME, TypeSpec.BINARY_TYPE, 6.5, false);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, BINARY_NAME));
        
		createReference(theKB, theME, WRAPPER_NAME,
			TLModelUtil.qualifiedName(TlModelFactory.TL_MODEL_STRUCTURE, TLObject.TL_OBJECT_TYPE), 6.8,
			false);
		assertTrue(MetaElementUtil.hasLocalMetaAttribute(theME, WRAPPER_NAME));

		createReference(theKB, theME, FAST_LIST_NAME,
			TLModelUtil.qualifiedName(TlModelFactory.TL_MODEL_STRUCTURE, TLEnumeration.TL_ENUMERATION_TYPE), 7.0,
			false);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, FAST_LIST_NAME));

		createReference(theKB, theME, CLASSIFICATION_NAME, TypeSpec.ENUM_PROTOCOL + ":" + theList.getName(), 1.0, true);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, CLASSIFICATION_NAME));

		createReference(theKB, theME, COLLECTION_NAME, TypeSpec.ENUM_PROTOCOL + ":" + theList.getName(), 2.0, true);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, COLLECTION_NAME));
        
		createReference(theKB, theME, TYPED_SET_NAME,
			TLModelUtil.qualifiedName(TlModelFactory.TL_MODEL_STRUCTURE, TLEnumeration.TL_ENUMERATION_TYPE), 2.0,
			true);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, TYPED_SET_NAME));

		AttributeConfig countryConfig = propertyConfig("Country", TypeSpec.COUNTRY_TYPE, 6.0, false);
		PrimitiveStorage.Config countryStorage = TypedConfiguration.newConfigItem(PrimitiveStorage.Config.class);
		countryStorage.setImplementationClass(PrimitiveStorage.class);
		TLStorage storageAnnotation = TypedConfiguration.newConfigItem(TLStorage.class);
		storageAnnotation.setImplementation(countryStorage);
		countryConfig.getAnnotations().add(storageAnnotation);
        createAttribute(theKB, theME, countryConfig);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, "Country"));
            
        
        //      Super ME MAs
		createAttribute(theKB, theSuperME, SUPER_LONG_NAME, TypeSpec.LONG_TYPE, 5.0, false);
        
        //      Sub ME MAs
		createAttribute(theKB, theSubME, SUB_STRING_NAME, TypeSpec.STRING_TYPE, 5.0, false);

        //      Sub ME MAs
		TLStructuredTypePart theToDeleteMA =
			createAttribute(theKB, theSubME, "SubStringToDel", TypeSpec.STRING_TYPE, 5.0, false);

        // Test inheritance with hasMetaAttribute and getMetaAttributes
        assertFalse (MetaElementUtil.hasLocalMetaAttribute(theME, SUPER_LONG_NAME));
        assertFalse (MetaElementUtil.hasLocalMetaAttribute(theME, SUB_STRING_NAME));
        
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theSuperME, SUPER_LONG_NAME));
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theSubME, SUB_STRING_NAME));   
        
		assertFalse(hasMetaAttributeInHierarchyExcludingInherited(theME, SUPER_LONG_NAME));
        assertFalse (MetaElementUtil.hasMetaAttribute(theME, SUB_STRING_NAME));

        assertTrue (MetaElementUtil.hasMetaAttribute(theME, SUPER_LONG_NAME));
		assertTrue(hasMetaAttributeInHierarchyExcludingInherited(theME, SUB_STRING_NAME));

        assertTrue (MetaElementUtil.hasMetaAttributeInHierarchy(theME, SUPER_LONG_NAME));
        assertTrue (MetaElementUtil.hasMetaAttributeInHierarchy(theME, SUB_STRING_NAME));

        assertFalse (MetaElementUtil.hasLocalMetaAttribute(theME, "non_existing_att_name"));
        
        // Test MA delete
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theSubME, "SubStringToDel"));
        MetaElementUtil.removeMetaAttribute(theSubME, theToDeleteMA);
        assertFalse (MetaElementUtil.hasLocalMetaAttribute(theSubME, "SubStringToDel"));
        
        // Test inheritance with getMetaAttributes
		Collection<TLStructuredTypePart> theMAs = TLModelUtil.getMetaAttributesInHierarchy(theME);
        assertNotNull (theMAs);
        assertTrue (theMAs.contains(theBoolMA));
        
		Collection<TLStructuredTypePart> theLongMAs = MetaElementUtil.getMetaAttributes(theME, LegacyTypeCodes.TYPE_LONG, true, true);
        assertNotNull (theMAs);
        assertTrue (theLongMAs.contains(theLongMA));
        assertFalse (theLongMAs.contains(theBoolMA));
        
		Collection<TLStructuredTypePart> theSuperStringMAs =
			MetaElementUtil.getMetaAttributes(theSuperME, LegacyTypeCodes.TYPE_STRING, false, false);
        assertNotNull (theSuperStringMAs);
        assertTrue (theSuperStringMAs.isEmpty());

        // SET VALUES
        // Set some values - test getMetaAttribute, setAtributeValue (correct/incorrect value type), getAttributeValue
        //      This MAs
		TLStructuredTypePart theSimpleAtt = getAttribute(theSE, BOOLEAN_NAME);
        AttributeOperations.setAttributeValue(theSE, theSimpleAtt, VAL_BOOL);
		assertEquals(VAL_BOOL, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        //          Test incorrect value
        try {
            AttributeOperations.setAttributeValue(theSE, theSimpleAtt, "not_a_correct_value");
            
            fail ("Attribute permitted setting value of wrong type!");
        }
        catch (IllegalArgumentException iax) {
            // Expected!!
        }
        
		theSimpleAtt = getAttribute(theSE, DATE_NAME);
        AttributeOperations.setAttributeValue(theSE, theSimpleAtt, VAL_DATE);
		assertEquals(VAL_DATE, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        
		theSimpleAtt = getAttribute(theSE, FLOAT_NAME);
        AttributeOperations.setAttributeValue(theSE, theSimpleAtt, VAL_FLOAT);
		assertEquals(VAL_FLOAT, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        
		theSimpleAtt = getAttribute(theSE, LONG_NAME);
        AttributeOperations.setAttributeValue(theSE, theSimpleAtt, VAL_LONG);
		assertEquals(VAL_LONG, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        
		theSimpleAtt = getAttribute(theSE, STRING_NAME);
        AttributeOperations.setAttributeValue(theSE, theSimpleAtt, VAL_STRING);
		assertEquals(VAL_STRING, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));

		theSimpleAtt = getAttribute(theSE, BINARY_NAME);
        AttributeOperations.setAttributeValue(theSE, theSimpleAtt, BinaryDataFactory.createFileBasedBinaryData(new ByteArrayInputStream(VAL_BINARY)));
		assertEquals(BinaryDataFactory.createFileBasedBinaryData(new ByteArrayInputStream(VAL_BINARY)), AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        
		theSimpleAtt = getAttribute(theSE, WRAPPER_NAME);
		// list can be set because the list has table type FAST_LIST which is an OBJECT table type
		AttributeOperations.setAttributeValue(theSE, theSimpleAtt, theList);
		assertEquals(theList, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
		// Classifier can be set althoug expected type is not 'ENUM:<listName> the classifier is
		// stored in some table which is an OBJECT table type
		AttributeOperations.setAttributeValue(theSE, theSimpleAtt, theList.getClassifiers().get(0));
		assertEquals(theList.getClassifiers().get(0), AttributeOperations.getAttributeValue(theSE, theSimpleAtt));

		// Test incorrect value type
		try {
			AttributeOperations.setAttributeValue(theSE, theSimpleAtt, "Not a wrapper value");

			fail("Attribute permitted setting value of not Wrapper type!");
		} catch (IllegalArgumentException iax) {
			// Expected!!
		}

		theSimpleAtt = getAttribute(theSE, FAST_LIST_NAME);
		AttributeOperations.setAttributeValue(theSE, theSimpleAtt, theList);
		assertEquals(theList, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        //          Test incorrect Wrapper type
        try {
			AttributeOperations.setAttributeValue(theSE, theSimpleAtt, theSE);
            
            fail ("Attribute permitted setting value of wrong Wrapper type!");
        }
        catch (IllegalArgumentException iax) {
            // Expected!!
        }
        
        // Now the CollectionMetaAttributes...
		TLClassifier theValue = theList.getClassifiers().get(0);
        TLStructuredTypePart theCollAtt = getAttribute(theSE, CLASSIFICATION_NAME);
        AttributeOperations.addAttributeValue(theSE, theCollAtt, theValue);
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theValue));

		TLClassifier theAddress = theList.getClassifiers().get(0);
		theCollAtt = getAttribute(theSE, COLLECTION_NAME);
        AttributeOperations.addAttributeValue(theSE, theCollAtt, theAddress);
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theAddress));
        
        Object theAddress2 = theList.getClassifiers().get(1);
        AttributeOperations.addAttributeValue(theSE, theCollAtt, theAddress2);
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theAddress));
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theAddress2));

        AttributeOperations.removeAttributeValue(theSE, theCollAtt, theAddress2);
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theAddress));
        assertFalse(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theAddress2));
        
        AttributeOperations.removeAttributeValue(theSE, theCollAtt, theAddress);
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).isEmpty());
        
        AttributeOperations.setAttributeValue(theSE, theCollAtt, theList.getClassifiers());
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theList.getClassifiers().get(0)));
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theList.getClassifiers().get(1)));
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theList.getClassifiers().get(2)));
        
        //TypedWrapperSetMA
        //add valid wrapper to set
		TLStructuredTypePart typedSetMA = getAttribute(theSE, TYPED_SET_NAME);
		AttributeOperations.addAttributeValue(theSE, typedSetMA, theList);
        //assert existence
		assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, typedSetMA)).contains(theList));
        
		// add invalid (a FastListElement-Instance), expect exception
        try {
			AttributeOperations.addAttributeValue(theSE, typedSetMA, theValue);
            
            fail ("Attribute permitted setting value of wrong Wrapper type!");
        }
        catch (IllegalArgumentException iax) {
            // Expected!!
        }
        
        
        //      Super MAs
		theSimpleAtt = getAttribute(theSE, SUPER_LONG_NAME);
        AttributeOperations.setAttributeValue(theSE, theSimpleAtt, VAL_LONG);
		assertEquals(VAL_LONG, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        

        // Test setting of value of sub ME
        try {
			theSimpleAtt = MetaElementUtil.getMetaAttribute(((TLClass) theSE.tType()), SUB_STRING_NAME);
            AttributeOperations.setAttributeValue(theSE, theSimpleAtt, VAL_STRING);
			assertEquals(VAL_STRING, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
            
            fail ("Attribute setting succeeded fpr attribute of sub meta element");
        }
        catch (NoSuchAttributeException nsax) {
            // Expected!!
        }

        // Test setting of value on non-existing attribute for an ME
        try {
            AttributeOperations.setAttributeValue(theSE, theToDeleteMA, "a_correct_value");
            
			fail("Attribute setting succeeded on object that doesn't have the attribute!");
        }
 catch (AttributeException ex) {
		assertContains("Access to undefined attribute", ex.getMessage());
            // Expected!!
        }
		theToDeleteMA.tDelete();
        
		tx.commit();
        
        // Refetch simple MAs
		theSimpleAtt = getAttribute(theSE, BOOLEAN_NAME);
		assertEquals(VAL_BOOL, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        
		theSimpleAtt = getAttribute(theSE, DATE_NAME);
		assertEquals(VAL_DATE, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        
		theSimpleAtt = getAttribute(theSE, FLOAT_NAME);
		assertEquals(VAL_FLOAT, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        
		theSimpleAtt = getAttribute(theSE, LONG_NAME);
		assertEquals(VAL_LONG, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        
		theSimpleAtt = getAttribute(theSE, STRING_NAME);
		assertEquals(VAL_STRING, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        
		theSimpleAtt = getAttribute(theSE, BINARY_NAME);
		assertEquals(BinaryDataFactory.createFileBasedBinaryData(new ByteArrayInputStream(VAL_BINARY)), AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
        
		theSimpleAtt = getAttribute(theSE, FAST_LIST_NAME);
		assertEquals(theList, AttributeOperations.getAttributeValue(theSE, theSimpleAtt));
    
        // Refetch CollectionMetaAttributes...
        theValue = theList.getClassifiers().get(0);
        theCollAtt = getAttribute(theSE, CLASSIFICATION_NAME);
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theValue));

		theCollAtt = getAttribute(theSE, COLLECTION_NAME);
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theList.getClassifiers().get(0)));
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theList.getClassifiers().get(1)));
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theSE, theCollAtt)).contains(theList.getClassifiers().get(2)));
        
        // test validity time stuff
        checkValidity(theSE);
        
        logTime("testCompleteScenario");
        
        assertTrue(theKB.commit());
    }

	private void setValidity(AttributeConfig booleanConfig, String specification) {
		booleanConfig.getAnnotations().add(validityCheck(specification));
	}

	private TLValidityCheck validityCheck(String specification) {
		TLValidityCheck result = TypedConfiguration.newConfigItem(TLValidityCheck.class);
		result.setValue(specification);
		return result;
	}

	private TLStructuredTypePart getAttribute(AttributedStructuredElementWrapper wrapper, String attributeName)
			throws NoSuchAttributeException {
		return (TLStructuredTypePart) wrapper.tType().getPart(attributeName);
	}
    
    /**
     * Test using small medium and large Binary Attributes.
     */
    public void testBinaryAttribute() throws Exception {
        // INIT STUFF
        KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
        assertNotNull("KB is null!", theKB);
        
		AttributedStructuredElementWrapper theSE = OrderedListHelper.getAttributedWrapper();
        
        // SETUP
		TLClass theME = (TLClass) theSE.tType();
        
		File ThisFile = new File(ModuleLayoutConstants.SRC_TEST_DIR
			+ "/test/com/top_logic/element/meta/kbbased/TestKBBasedMetaAttributes.java");
        byte[] VAL_BINARY_MEDIUM = new byte[4444];
        new Random(44444444L).nextBytes(VAL_BINARY_MEDIUM);

		createAttribute(theKB, theME, "B-Small", TypeSpec.BINARY_TYPE, 17, false);
		createAttribute(theKB, theME, "B-Medium", TypeSpec.BINARY_TYPE, 177, true);
		createAttribute(theKB, theME, "B-Large", TypeSpec.BINARY_TYPE, 1777, false);

        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, "B-Small"));
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, "B-Medium"));
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, "B-Large"));
        
        // SET VALUES

        final int REPEAT = 10;
        startTime();
        for (int i =0; i <REPEAT; i++) {
			TLStructuredTypePart binaryAtt = getAttribute(theSE, "B-Small");
            AttributeOperations.setAttributeValue(theSE, binaryAtt, BinaryDataFactory.createFileBasedBinaryData(new ByteArrayInputStream(VAL_BINARY)));
            BinaryData bData = (BinaryData) AttributeOperations.getAttributeValue(theSE, binaryAtt);
			assertEquals(VAL_BINARY.length, bData.getSize());
			InputStream in1 = bData.getStream();
			try {
				InputStream in2 = new ByteArrayInputStream(VAL_BINARY);
				try {
					assertTrue(StreamUtilities.equalsStreamContents(in1, in2));
				} finally {
					in2.close();
				}
			} finally {
				in1.close();
			}
    
			binaryAtt = getAttribute(theSE, "B-Medium");
            AttributeOperations.setAttributeValue(theSE, binaryAtt, BinaryDataFactory.createFileBasedBinaryData(new ByteArrayInputStream(VAL_BINARY_MEDIUM)));
            bData = (BinaryData) AttributeOperations.getAttributeValue(theSE, binaryAtt);
			assertEquals(VAL_BINARY_MEDIUM.length, bData.getSize());
			InputStream in11 = bData.getStream();
			InputStream in2 = new ByteArrayInputStream(VAL_BINARY_MEDIUM);
			try {
				try {
					assertTrue(StreamUtilities.equalsStreamContents(in11, in2));
				} finally {
					in2.close();
				}
			} finally {
				in11.close();
			}
            
    
			binaryAtt = getAttribute(theSE, "B-Large");
			AttributeOperations.setAttributeValue(theSE, binaryAtt, BinaryDataFactory.createBinaryData(ThisFile));
            bData = (BinaryData) AttributeOperations.getAttributeValue(theSE, binaryAtt);
			assertEquals(ThisFile.length(), bData.getSize());
			InputStream in12 = bData.getStream();
			try {
				InputStream in21 = new FileInputStream(ThisFile);
				try {
					assertTrue(StreamUtilities.equalsStreamContents(in12, in21));
				} finally {
					in21.close();
				}
			} finally {
				in12.close();
			}
    
            assertTrue(theKB.commit());
            
            // Test after commit
    
			binaryAtt = getAttribute(theSE, "B-Small");
            bData = (BinaryData) AttributeOperations.getAttributeValue(theSE, binaryAtt);
			assertEquals(VAL_BINARY.length, bData.getSize());
			InputStream in13 = bData.getStream();
			try {
				InputStream in21 = new ByteArrayInputStream(VAL_BINARY);
				try {
					assertTrue(StreamUtilities.equalsStreamContents(in13, in21));
				} finally {
					in21.close();
				}
			} finally {
				in13.close();
			}
            
			binaryAtt = getAttribute(theSE, "B-Medium");
            bData = (BinaryData) AttributeOperations.getAttributeValue(theSE, binaryAtt);
			assertEquals(VAL_BINARY_MEDIUM.length, bData.getSize());
			InputStream in14 = bData.getStream();
			try {
				InputStream in21 = new ByteArrayInputStream(VAL_BINARY_MEDIUM);
				try {
					assertTrue(StreamUtilities.equalsStreamContents(in14, in21));
				} finally {
					in21.close();
				}
			} finally {
				in14.close();
			}
            
			binaryAtt = getAttribute(theSE, "B-Large");
            bData = (BinaryData) AttributeOperations.getAttributeValue(theSE, binaryAtt);
			assertEquals(ThisFile.length(), bData.getSize());
			InputStream in15 = bData.getStream();
			try {
				InputStream in21 = new FileInputStream(ThisFile);
				try {
					assertTrue(StreamUtilities.equalsStreamContents(in15, in21));
				} finally {
					in21.close();
				}
			} finally {
				in15.close();
			}
        }
        logTime("testBinaryAttribute x " + REPEAT + " ");
        
        assertTrue(theKB.commit());
    }

	public static TLStructuredTypePart createAttribute(KnowledgeBase kb, TLClass me, String name, String dataTypeName,
			double sortOder, boolean mandatory) throws DuplicateAttributeException {
		AttributeConfig config = propertyConfig(name, dataTypeName, sortOder, mandatory);
		return createAttribute(kb, me, config);
	}

	static TLStructuredTypePart createAttribute(KnowledgeBase kb, TLClass me, AttributeConfig config)
			throws DuplicateAttributeException {
		return createStructuredTypePart(me, config);
	}

	public static AttributeConfig propertyConfig(String name, String dataTypeName, double sortOder, boolean mandatory) {
		AttributeConfig config = TypedConfiguration.newConfigItem(AttributeConfig.class);
		config.setName(name);
		config.setTypeSpec(dataTypeName);
		config.setMandatory(mandatory);
		DisplayAnnotations.setSortOrder(config, sortOder);
		DisplayAnnotations.setDeleteProtected(config);
		return config;
	}
    
	public static TLStructuredTypePart createReference(KnowledgeBase kb, TLClass me, String name, String typeSpec,
			double sortOder, boolean multiple) throws DuplicateAttributeException, ConfigurationException {
		ReferenceConfig config = referenceConfig(name, typeSpec, sortOder, multiple);
		return (TLStructuredTypePart) createReference(me, config);
	}

	public static ReferenceConfig referenceConfig(String name, String typeSpec, double sortOder, boolean multiple) {
		assertNotNull(typeSpec);
		ReferenceConfig config = TypedConfiguration.newConfigItem(ReferenceConfig.class);
		config.setName(name);
		config.setTypeSpec(typeSpec);
		config.setMultiple(multiple);
		DisplayAnnotations.setSortOrder(config, sortOder);
		DisplayAnnotations.setDeleteProtected(config);
		return config;
	}

    /**
	 * Check MetaHierary of given parameters
	 * 
	 * @param aw
	 *        will become Holder of me
	 * @param superMe
	 *        Root type
	 * @param me
	 *        Middle type
	 * @param subME
	 *        Leaf type.
	 */
	private void checkMetaHierarchy(AttributedStructuredElementWrapper aw, TLClass superMe,
            TLClass me, TLClass subME) {
		assertNotNull("Super type is null", superMe);
		assertNotNull("Type is null", me);
		assertNotNull("Sub type is null", subME);
        
		PersistentObjectImpl.setMetaElement(aw, me);
        
		// Test TLObject interface
		assertEquals(me, aw.tType());

        // Test MetaElementHolder interface
		assertEquals(superMe, aw.getMetaElement(OrderedListHelper.SUPER_META_ELEMENT_TYPE));
		assertEquals(me, aw.getMetaElement(OrderedListHelper.META_ELEMENT_TYPE));
		assertEquals(subME, aw.getMetaElement(OrderedListHelper.SUB_META_ELEMENT_TYPE));
        
		Set<TLClass> theMetaElements = aw.getMetaElements();
        assertNotNull(theMetaElements);
        assertEquals(3, theMetaElements.size());
        assertTrue(theMetaElements.contains(me));
        assertTrue(theMetaElements.contains(superMe));
        assertTrue(theMetaElements.contains(subME));
        
        // Remove and re-add Sub Meta element
        aw.removeMetaElement(subME);
        assertNull (aw.getMetaElement(OrderedListHelper.SUB_META_ELEMENT_TYPE));
        assertFalse(aw.getMetaElements().contains(subME));
        aw.addMetaElement(subME);

    }

    /**
	 * Check using Attributes with {@link ValidityCheck}.
	 * 
	 * @param aWrapper
	 *        the object used for testing
	 */
	private void checkValidity(AttributedStructuredElementWrapper aWrapper) throws NoSuchAttributeException,
            IllegalArgumentException, AttributeException, InterruptedException {

		TLStructuredTypePart theNoValidityAtt = getAttribute(aWrapper, DATE_NAME);
		TLStructuredTypePart theNegativeValidityAtt = getAttribute(aWrapper, FLOAT_NAME);
		TLStructuredTypePart theValidityAtt = getAttribute(aWrapper, BOOLEAN_NAME);
        
        ValidityCheck theCheck = AttributeOperations.getValidityCheck(theNoValidityAtt);
        assertNotNull(theCheck);
        assertEquals(ValidityCheck.CHECK_DURATION_NOT_SET, theCheck.getCheckDuration());
		assertNull(null, AttributeOperations.getLastChangeDate(aWrapper, theNoValidityAtt));

        theCheck = AttributeOperations.getValidityCheck(theNegativeValidityAtt);
        assertNotNull(theCheck);
        assertEquals(ValidityCheck.CHECK_DURATION_NOT_SET, theCheck.getCheckDuration());
		assertEquals(null, AttributeOperations.getLastChangeDate(aWrapper, theNegativeValidityAtt));

        theCheck = AttributeOperations.getValidityCheck(theValidityAtt);
        assertEquals(500000, theCheck.getCheckDuration());
        
		Date lastChangeDate = AttributeOperations.getLastChangeDate(aWrapper, theValidityAtt);
        
        assertNotSame(ValidityCheck.INVALID_DATE, lastChangeDate);
        Date nextTimeout = theCheck.getNextTimeout(lastChangeDate);

        assertTrue("Last Change in the future ?" , lastChangeDate.getTime() <= System.currentTimeMillis());
        assertTrue("Next timeout not in the future?", nextTimeout.getTime() > System.currentTimeMillis());
        
        AttributeOperations.setAttributeValue(aWrapper, theValidityAtt, Boolean.FALSE);

        Thread.sleep(100);

		Date lastChangeDate2 = AttributeOperations.getLastChangeDate(aWrapper, theValidityAtt);
        assertNotSame(ValidityCheck.INVALID_DATE, lastChangeDate2);
        Date nextTimeout2 = theCheck.getNextTimeout(lastChangeDate2);

        assertTrue(lastChangeDate.before(lastChangeDate2));
        assertTrue(!nextTimeout2.after(nextTimeout2));
        assertTrue("Last Change in the future ?", lastChangeDate2.getTime() <= System.currentTimeMillis());
        assertTrue("Next timeout no in the future?", nextTimeout2.getTime() > System.currentTimeMillis());

        Thread.sleep(100);

		AttributeOperations.touch(aWrapper, theValidityAtt);
		lastChangeDate = AttributeOperations.getLastChangeDate(aWrapper, theValidityAtt);
        nextTimeout = theCheck.getNextTimeout(lastChangeDate);
        
        assertNotSame(ValidityCheck.INVALID_DATE, lastChangeDate);
        assertTrue(lastChangeDate2.before(lastChangeDate));
        assertTrue(!nextTimeout.after(nextTimeout2));
        assertTrue("Last Change in the future ?" , lastChangeDate.getTime() <= System.currentTimeMillis());
        assertTrue("Next timeout not in the future?", nextTimeout.getTime() > System.currentTimeMillis());
        
        // Reset value as TestStoredQuery depends on it :-(
        AttributeOperations.setAttributeValue(aWrapper, theValidityAtt, VAL_BOOL);

    }

    /**
     * Test MetaAttributes of type TYPE_COLLECTION.
     */
    public void testSimpleCollection() throws Exception {
        // INIT STUFF
        KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
        assertNotNull(theKB);
        
		AttributedStructuredElementWrapper theAttributed = OrderedListHelper.getAttributedWrapper();
        
        // SETUP
		// Assume MetaElements where created before MetaElements and add them to the list (holder)
        
        // Get the FastList that is used as holder, attributes, and classification list ;-)
		TLEnumeration theList =
			OrderedListHelper.createTestEnumeration(ModelService.getApplicationModel(), EXAMPLE_LIST, false);
        assertNotNull("FastList is null!", theList);

        assertTrue(theKB.commit());
        
		TLStructuredTypePart theCollAtt = getAttribute(theAttributed, COLLECTION_NAME);
        AttributeOperations.setAttributeValue(theAttributed, theCollAtt, null); // 

		List elements = theList.getClassifiers();
        Object theAddress0 = elements.get(0);
        Object theAddress1 = elements.get(1);
        Object theAddress2 = elements.get(2);
        AttributeOperations.addAttributeValue(theAttributed, theCollAtt, theAddress0);
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theAttributed, theCollAtt)).contains(theAddress0));
        
        AttributeOperations.addAttributeValue(theAttributed, theCollAtt, theAddress1);
        Collection theCol = (Collection) AttributeOperations.getAttributeValue(theAttributed, theCollAtt);
        assertTrue(theCol.contains(theAddress0));
        assertTrue(theCol.contains(theAddress1));

        try {
            AttributeOperations.addAttributeValue(theAttributed, theCollAtt, theAddress1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }

        try {
            AttributeOperations.addAttributeValue(theAttributed, theCollAtt, "Not a Wrapper, you see?");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }

        AttributeOperations.removeAttributeValue(theAttributed, theCollAtt, theAddress1);
        theCol = (Collection) AttributeOperations.getAttributeValue(theAttributed, theCollAtt);
        assertTrue (theCol.contains(theAddress0));
        assertFalse(theCol.contains(theAddress1));
        
        try {
            AttributeOperations.removeAttributeValue(theAttributed, theCollAtt, theAddress1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }

        try {
            AttributeOperations.removeAttributeValue(theAttributed, theCollAtt, "Not a Wrapper, you see?");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }

        AttributeOperations.removeAttributeValue(theAttributed, theCollAtt, theAddress0);
        theCol = (Collection) AttributeOperations.getAttributeValue(theAttributed, theCollAtt);
        assertTrue(theCol.isEmpty());
        
        AttributeOperations.setAttributeValue(theAttributed, theCollAtt, elements);
        theCol = (Collection) AttributeOperations.getAttributeValue(theAttributed, theCollAtt);
        assertTrue(theCol.containsAll(elements));

        AttributeOperations.setAttributeValue(theAttributed, theCollAtt, Collections.singleton(theAddress1));
        theCol = (Collection) AttributeOperations.getAttributeValue(theAttributed, theCollAtt);
        assertEquals(1, theCol.size());
        assertTrue (theCol.contains(theAddress1));

        AttributeOperations.setAttributeValue(theAttributed, theCollAtt, null);
        theCol = (Collection) AttributeOperations.getAttributeValue(theAttributed, theCollAtt);
        assertTrue(theCol.isEmpty());

        List list2 = new ArrayList(2);
        list2.add(theAddress0); list2.add(theAddress2);
        AttributeOperations.setAttributeValue(theAttributed, theCollAtt, list2);
        theCol = (Collection) AttributeOperations.getAttributeValue(theAttributed, theCollAtt);
        assertEquals(2, theCol.size());
        assertTrue (theCol.contains(theAddress0));
        assertTrue (theCol.contains(theAddress2));

        // NOOP should be ignored
        // theCollAtt.checkUpdate(aContext, anUpdate)
        AttributeOperations.setAttributeValue(theAttributed, theCollAtt, list2);
        theCol = (Collection) AttributeOperations.getAttributeValue(theAttributed, theCollAtt);
        assertEquals(2, theCol.size());
        assertTrue (theCol.contains(theAddress0));
        assertTrue (theCol.contains(theAddress2));

        AttributeOperations.setAttributeValue(theAttributed, theCollAtt, Collections.EMPTY_SET);
        theCol = (Collection) AttributeOperations.getAttributeValue(theAttributed, theCollAtt);
        assertTrue(theCol.isEmpty());
        
        assertFalse(AttributeOperations.allowsSearchRange(theCollAtt));
    }
    
    /**
     * Test MetaAttributes of type TYPE_LIST
     */
    public void testSimpleList() throws Exception {
        // INIT STUFF
        KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
        assertNotNull(theKB);
        
		AttributedStructuredElementWrapper theAttributed = OrderedListHelper.getAttributedWrapper();
        TLClass theME = theAttributed.getMetaElement(OrderedListHelper.META_ELEMENT_TYPE);

        // Get the FastList that is used as holder, attributes, and classification list ;-)
		TLEnumeration theList =
			OrderedListHelper.createTestEnumeration(ModelService.getApplicationModel(), EXAMPLE_LIST, false);
        assertNotNull("FastList is null!", theList);

        assertTrue(theKB.commit());

		ReferenceConfig listConfig =
			referenceConfig(LIST_NAME, TypeSpec.ENUM_PROTOCOL + ":" + theList.getName(), 2.14, true);
		listConfig.setOrdered(true);
		listConfig.setBag(true);
		TLStructuredTypePart createReference = (TLStructuredTypePart) createReference(theME, listConfig);
        assertTrue (MetaElementUtil.hasLocalMetaAttribute(theME, LIST_NAME));
		TLStructuredTypePart wlma = getAttribute(theAttributed, LIST_NAME);
        AttributeOperations.setAttributeValue(theAttributed, wlma, null); // 

		List<TLClassifier> elements = theList.getClassifiers();
		TLClassifier theAddress0 = elements.get(0);
		TLClassifier theAddress1 = elements.get(1);
		TLClassifier theAddress2 = elements.get(2);
        AttributeOperations.addAttributeValue(theAttributed, wlma, theAddress0);
        assertTrue(((Collection) AttributeOperations.getAttributeValue(theAttributed, wlma)).contains(theAddress0));
        
        AttributeOperations.addAttributeValue(theAttributed, wlma, theAddress1);
        List<FastListElement> theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);
        assertTrue(theAList.contains(theAddress0));
        assertTrue(theAList.contains(theAddress1));

        AttributeOperations.addAttributeValue(theAttributed, wlma, theAddress1);

        theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);

        assertEquals(3, theAList.size());
        assertTrue(theAList.contains(theAddress1));
        assertTrue(theAList.indexOf(theAddress1) == 1 && theAList.lastIndexOf(theAddress1) == 2);
        
        try {
            AttributeOperations.addAttributeValue(theAttributed, wlma, "Not a Wrapper, you see?");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }

        // remove only once !
        AttributeOperations.removeAttributeValue(theAttributed, wlma, theAddress1);
        theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);
        assertEquals(2, theAList.size());
        assertTrue (theAList.contains(theAddress0));
        assertTrue (theAList.contains(theAddress1));

        AttributeOperations.removeAttributeValue(theAttributed, wlma, theAddress1);
        theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);
        assertTrue (theAList.contains(theAddress0));
        assertFalse(theAList.contains(theAddress1));
        
        try {
            AttributeOperations.removeAttributeValue(theAttributed, wlma, theAddress1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }

        try {
            AttributeOperations.removeAttributeValue(theAttributed, wlma, "Not a Wrapper, you see?");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) { /* expected */ }

        AttributeOperations.removeAttributeValue(theAttributed, wlma, theAddress0);
        theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);
        assertTrue(theAList.isEmpty());
        
        AttributeOperations.setAttributeValue(theAttributed, wlma, elements);
        theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);
        assertTrue(theAList.containsAll(elements));

        AttributeOperations.setAttributeValue(theAttributed, wlma, Collections.singleton(theAddress1));
        theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);
        assertEquals(1, theAList.size());
        assertTrue (theAList.contains(theAddress1));

        AttributeOperations.setAttributeValue(theAttributed, wlma, null);
        theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);
        assertTrue(theAList.isEmpty());

        List list2 = new ArrayList(2);
        list2.add(theAddress2); list2.add(theAddress0); list2.add(theAddress2);
        AttributeOperations.setAttributeValue(theAttributed, wlma, list2);
        theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);
        assertEquals(3, theAList.size());
        assertTrue (theAList.contains(theAddress0));
        assertTrue (theAList.contains(theAddress2));
        assertTrue(theAList.indexOf(theAddress2) == 0 && theAList.lastIndexOf(theAddress2) == 2);

        // NOOP should be ignored
        // theCollAtt.checkUpdate(aContext, anUpdate)
        AttributeOperations.setAttributeValue(theAttributed, wlma, list2);
        theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);
        assertEquals(3, theAList.size());
        assertTrue (theAList.contains(theAddress0));
        assertTrue (theAList.contains(theAddress2));

        AttributeOperations.setAttributeValue(theAttributed, wlma, Collections.EMPTY_SET);
        theAList = (List<FastListElement>) AttributeOperations.getAttributeValue(theAttributed, wlma);
        assertTrue(theAList.isEmpty());
        
        assertFalse(AttributeOperations.allowsSearchRange(wlma));
        
        
    }

	private static boolean hasMetaAttributeInHierarchyExcludingInherited(TLClass metaElement,
			String name) {
		TLStructuredTypePart ma = MetaElementUtil.getMetaAttributeInHierarchyOrNull(metaElement, name);
		if (ma == null) {
			return false;
		}
		if (MetaElementUtil.hasMetaAttribute(metaElement, name)) {
			return false;
		}
		return true;
	}

	public static TLReference createReference(TLClass tlClass, ReferenceConfig referenceConfig) {
		return (TLReference) createStructuredTypePart(tlClass, referenceConfig);
	}

	public static TLStructuredTypePart createStructuredTypePart(TLClass owner, PartConfig partConfig) {
		CreateStructuredTypePart createPartConfig = TypedConfiguration.newConfigItem(CreateStructuredTypePart.class);
		createPartConfig.setType(TLModelUtil.qualifiedName(owner));
		createPartConfig.setPart(partConfig);

		ApplyModelPatch.applyPatch(new AssertProtocol(), owner.getModel(), DynamicModelService.getInstance(),
			Arrays.asList(createPartConfig));

		TLStructuredTypePart newPart = owner.getPartOrFail(partConfig.getName());
		return newPart;
	}

    /** 
     * Suite of tests.
     */
    public static Test suite () {
		TestSuite test = new TestSuite();
		test.addTest(new TestKBBasedMetaAttributes("testRevert"));
		test.addTest(new TestKBBasedMetaAttributes("testChangeConfiguration"));
		test.addTest(new TestKBBasedMetaAttributes("testCompleteScenario"));
		test.addTest(new TestKBBasedMetaAttributes("testBinaryAttribute"));
		test.addTest(new TestKBBasedMetaAttributes("testSimpleCollection"));
		test.addTest(new TestKBBasedMetaAttributes("testSimpleList"));
//		TestSetup revertChangesSetup = new TestSetup(test) {
//
//			private Revision _preTestRevision;
//
//			@Override
//			protected void setUp() throws Exception {
//				KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
//				HistoryManager historyManager = kb.getHistoryManager();
//				_preTestRevision = historyManager.getRevision(historyManager.getLastRevision());
//			}
//
//			@Override
//			protected void tearDown() throws Exception {
//				KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
//				Branch contextBranch = kb.getHistoryManager().getContextBranch();
//				KBUtils.revert(kb, _preTestRevision, contextBranch);
//			}
//
//		};
		return ElementWebTestSetup.createElementWebTestSetup(test);
    }

}
