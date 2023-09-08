/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import junit.framework.Assert;

import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.sql.DBIndex;

/**
 * This class represents the Objects found in <code>KBTestMeta.xml</code>.
 * 
 * I depends on the KnowlegdBase currently found in
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class KBTestMeta extends Assert {
    
    public static final String TEST_DB_TYPES    = "TestDBTypes";

    /**
     * Base class for type hierarchy test.
     */
    public static final String TEST_A           = "TestA";
    public static final String TEST_A_NAME      = "nameA";
    public static final String TEST_A_INT       = "intA";

    /**
     * Extends {@link #TEST_A}.
     */
    public static final String TEST_B           = "TestB";
    public static final String TEST_B_NAME      = "nameB";
    public static final String TEST_B_INT       = "intB";

    /**
     * Extends {@link #TEST_A}.
     */
    public static final String TEST_C           = "TestC";
    public static final String TEST_C_NAME      = "nameC";
    public static final String TEST_C_INT       = "intC";

    /**
     * Extends {@link #TEST_B}.
     */
    public static final String TEST_D           = "TestD";
    public static final String TEST_D_NAME      = "nameD";
    public static final String TEST_D_STRING    = "stringD";
    public static final String TEST_D_INT       = "intD";

    /**
     * Base association for type hierarchy tests.
     */
    public static final String ASSOC_A          = "hasTestAssocA";
    
    /**
     * Test association with attributes.
     */
    public static final String ASSOC_B          = "hasTestAssocB";
    public static final String ASSOC_B_STRING   = "stringAttr";
    public static final String ASSOC_B_INT      = "intAttr";

    /**
     * Extends {@link #ASSOC_A}.
     */
    public static final String ASSOC_C = "hasTestAssocC";
    public static final String ASSOC_C_BOOLEAN = "booleanAttr";

    /**
     * Test class for testing indices. 
     */
    public static final String IDX_SUPER         = "IDXSuper";
    
    /**
     * Attribute of {@link #IDX_SUPER} with unique constraint ({@link #IDX_SUPER_IATTR3}).
     */
	public static final String IDX_SUPER_IATTR3 = "iattr3";
	
    /**
     * Attribute of {@link #IDX_SUPER} with unique constraint ({@link #IDX_SUPER_SATTR3}).
     */
	public static final String IDX_SUPER_SATTR3 = "sattr3";


	/**
	 * Test class for testing indices. Extends {@link #IDX_SUPER}.
	 */
    public static final String IDX_TEST          = "IDXTest";
    
    /**
     * Attribute of {@link #IDX_TEST} with unique constraint ({@link #IDX_TEST_IATTR1}, {@link #IDX_TEST_SATTR1}).
     */
    public static final String IDX_TEST_IATTR1 = "iattr1";

    /**
     * Attribute of {@link #IDX_TEST} with unique constraint ({@link #IDX_TEST_IATTR1}, {@link #IDX_TEST_SATTR1}).
     */
    public static final String IDX_TEST_SATTR1 = "sattr1";
    
    /**
     * Attribute of {@link #IDX_TEST} with unique constraint ({@link #IDX_TEST_IATTR2}).
     */
	public static final String IDX_TEST_IATTR2 = "iattr2";

    /**
     * Attribute of {@link #IDX_TEST} with unique constraint ({@link #IDX_TEST_SATTR2}).
     */
	public static final String IDX_TEST_SATTR2 = "sattr2";

	/**
     * Test class for testing indices with null values.
     */
    public static final String IDX_NULL_TEST   = "IDXNullTest";

    /**
     * Test for (mis-)usage of empty MOs
     */
    public static final String EMPTY   = "empty";

	/** Type having custom indexes */
	public static final String IDX_CUSTOM = "IDXCustom";

	/** {@link MOAttribute#isMandatory() Mandatory} string attribute of {@link #IDX_CUSTOM}.*/
	public static final String IDX_CUSTOM_SATTR = "sattr";
	/** {@link MOAttribute#isMandatory() Mandatory} integer attribute of {@link #IDX_CUSTOM}.*/
	public static final String IDX_CUSTOM_IATTR = "iattr";

	/** {@link DBIndex#isUnique() Unique} index on {@link #IDX_CUSTOM}.*/
	public static final String IDX_CUSTOM_UNIQUE = "IDXCustomUnique";

    /**
     * Fetch the MetaObject with given Name form the current KnowledgeBase.
     * 
     * @throws UnknownTypeException when name is not know in KnowledgeBase 
     */
    public static MOClass getMetaObject(String aName) throws UnknownTypeException {
        return (MOClass) KBSetup.getKnowledgeBase()
            .getMORepository().getMetaObject(aName);
    }

}

