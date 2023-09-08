/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Address;

/**
 * Tests for {@link com.top_logic.knowledge.wrap.Address}.
 * 
 * @author <a href="mailto:hbo@top-logic.com">hbo</a>
 */
public class TestAddress extends BasicTestCase {
	
	/** Street of address. */
	protected static final String STREET_1_V 		= "asdsa";
	/** Street of address. */
	protected static final String STREET_2_V 		= "dfds2";
	/** ZIP-Code of address. */
	protected static final String ZIP_V 			= "erew";
	/** City of address. */
	protected static final String CITY_V 			= "reter";
	/** Country of address. */
	protected static final String COUNTRY_V 		= "gfggf";
	/** State of address. */
	protected static final String STATE_V 			= "vcbv";
	/** Telephone-Nr. of address. */
	protected static final String TELEPHONE_1_V 	= "uzutut";
	/** Telephone-Nr. of address. */
	protected static final String TELEPHONE_2_V 	= "kjlj";
	/** Mobile-Telephone-Nr. of address. */
	protected static final String MOBILE_V 			= "oppop";
	/** Fax-Nr. of address. */
	protected static final String FAX_1_V 			= "dffsf";
	/** Fax-Nr. of address. */
	protected static final String FAX_2_V 			= "uiouo";
	protected static final String E_MAIL_V			="kllök";
	protected static final String ASS_TYPE          ="jkkjljoo";
	
	/**
	 * Test of set- and get-methods of class {@link com.top_logic.knowledge.wrap.Address}.
	 */
	public void testSetAndGetMethods() throws Exception{
		KnowledgeBase theBase = KBSetup.getKnowledgeBase();
		Address theAddress = Address.createAddress(theBase);
		setAddressAttributes(theAddress);
		theBase.commit();
		checkAddressAttributes(theAddress);
		theBase.delete(theAddress.tHandle());
		theBase.commit();
			
	}
	
	/**
	 * Check, whether the attributes of the given address have the expected values.
	 * @param theAddress the Address that attributes are checked
	 */
	private void checkAddressAttributes(Address theAddress) {
		assertEquals(CITY_V       , theAddress.getCity());
		assertEquals(COUNTRY_V    , theAddress.getCountry());
		assertEquals(FAX_1_V      , theAddress.getFax1());
		assertEquals(FAX_2_V      , theAddress.getFax2());
		assertEquals(MOBILE_V     , theAddress.getMobile());
		assertEquals(STATE_V      , theAddress.getState());
		assertEquals(STREET_1_V   , theAddress.getStreet1());
		assertEquals(STREET_2_V   , theAddress.getStreet2());
		assertEquals(TELEPHONE_1_V, theAddress.getTelephone1());
		assertEquals(TELEPHONE_2_V, theAddress.getTelephone2());
		assertEquals(E_MAIL_V     , theAddress.getEMail());
	}

	/**
	* Set all attributes of the given address.
	* @param theAddress the Address that attributes are set
	*/
	private void setAddressAttributes(Address theAddress) {
		theAddress.setCity(CITY_V);
		theAddress.setCountry(COUNTRY_V);
		theAddress.setFax1(FAX_1_V);
		theAddress.setFax2(FAX_2_V);
		theAddress.setMobile(MOBILE_V);
		theAddress.setState(STATE_V);
		theAddress.setStreet1(STREET_1_V);
		theAddress.setStreet2(STREET_2_V);
		theAddress.setTelephone1(TELEPHONE_1_V);
		theAddress.setTelephone2(TELEPHONE_2_V);
		theAddress.setZip(ZIP_V);
		theAddress.setEMail(E_MAIL_V);
	}
	
	/**
	* The method constructing a test suite for this class.
	*
	* @return    The test to be executed.
	*/
	public static Test suite () {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestAddress.class));
	}
	
	/**
	* Start this test.
	* @param args not used
	*/   
	public static void main(String[] args) {
		SHOW_TIME             = true;
		//KBSetup.CREATE_TABLES = false;
		Logger.configureStdout(); // "INFO"
		
		TestRunner.run (suite ());
	}
}
