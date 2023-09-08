/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.business;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.AddressHolder;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.filter.CompanyContactFilter;
import com.top_logic.contact.layout.ContactResourceProvider;
import com.top_logic.knowledge.journal.JournalEntry;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Test behaviour of a company contact.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class CompanyContactTest extends BasicTestCase {
    
    /** Number of Contracts to ceate in massive Test(s) */
    protected static final int NUM_CONTRACTS = 100;

    /**
     * Constructor for companyContactTest.
     */
    public CompanyContactTest(String name) {
        super(name);
    }

    public void testCreateContact () throws Exception {
        
        CompanyContact newCompany = null;
        KnowledgeBase theBase = null;
        try {
            ContactFactory theFac = ContactFactory.getInstance();

            assertEquals (0,theFac.getAllContacts(ContactFactory.COMPANY_TYPE).size());
            
            newCompany = theFac.createNewCompanyContact("BOS GmbH", createAnAddress());
            
            assertNotNull(newCompany);
            newCompany.setValue("state","Hessen");

            theBase = newCompany.getKnowledgeBase();
            
            JournalEntry jEntry = newCompany.getJournalEntry(null, null, null);
            
            assertNotNull(jEntry.getAttributes());

            // TODO JCO/KBU/TSA should this be "CompanyContact"  ?
            assertEquals ("Contact"       , jEntry.getType()); 
            assertEquals ("CompanyContact", newCompany.getJournalType()); 
            
            assertTrue(theBase.commit());

            CompanyContactFilter ccf = new CompanyContactFilter();
            assertTrue (ccf.accept(newCompany));
            assertFalse(ccf.accept(theFac)   );
     
            ContactResourceProvider clr = newContactResourceProvider();
            assertTrue(clr.getLabel(newCompany).startsWith("BOS GmbH"));
     
            AddressHolder theAddress = newCompany.getAddress();
            assertEquals ("Hessen", theAddress.getProperty(AddressHolder.STATE));
            assertEquals ("60326", newCompany.getValue("ZIPCode"));
            
            assertEquals(0, newCompany.getStaff().size());
        }
        finally {
            // get rid of the company wrapper and knowledgeObject!
            newCompany.tDelete();
            assertFalse (newCompany.tValid());
            theBase.commit();
        }
    }

	private ContactResourceProvider newContactResourceProvider() {
		return TypedConfigUtil.newConfiguredInstance(ContactResourceProvider.class);
	}
    
    protected static AddressHolder createAnAddress () {
        AddressHolder theAddress = new AddressHolder();
        theAddress.setProperty(AddressHolder.STREET,"Rüsselheimer Strasse 22");
        theAddress.setProperty(AddressHolder.ZIP_CODE,"60326");
        theAddress.setProperty(AddressHolder.CITY,"Frankfurt");
        theAddress.setProperty(AddressHolder.COUNTRY,"DE");
        theAddress.setProperty(AddressHolder.PHONE,"069-24779 0");
        return theAddress;
    }

    /**
     * Testcase for the Foreign Key access (and mass object creation).
     */
    public void testForeignKey()throws Exception {
        
        Random  rand  = new Random(8998899889997L);
        CompanyContact newCompanies[] = new CompanyContact[NUM_CONTRACTS];
        int            num2           = NUM_CONTRACTS >> 1;
        KnowledgeBase  theBase = CompanyContact.getDefaultKnowledgeBase();
        try {
            ContactFactory theFac    = ContactFactory.getInstance();
            AddressHolder  adrHolder = createAnAddress();
            
            startTime();
            for (int i=0; i < NUM_CONTRACTS; i++) {
                String name = "Name" + Long   .toString(rand.nextInt());
				String fKey = limit(9, Long.toString(rand.nextLong()));
				String fKey2 = limit(10, Integer.toString(rand.nextInt(num2)));
                CompanyContact newCompany = newCompanies[i] 
                            = theFac.createNewCompanyContact(name, adrHolder);
                assertNotNull(newCompany);
				newCompany.setFKey(fKey);
                newCompany.setForeignKey2(fKey2);
            }
            assertTrue(theBase.commit());
            logTime("Creating " + NUM_CONTRACTS + " CompanyContacts");
            
            assertEquals (NUM_CONTRACTS,theFac.getAllContacts(ContactFactory.COMPANY_TYPE).size());
            
            // reseed so Names/keys are the same again.
            rand.setSeed(8998899889997L);
            startTime();
            for (int i=0; i < NUM_CONTRACTS; i++) {
                /* String name = "Name" + Long.toString( */ rand.nextInt(); // );
                CompanyContact  cc      = newCompanies[i];
				String fKey = limit(9, Long.toString(rand.nextLong()));
				String fKey2 = limit(10, Integer.toString(rand.nextInt(num2)));
				assertEquals(fKey, cc.getFKey());
				assertTrue(fKey, AbstractContact.getListByFKey(theBase, fKey).contains(cc));
                assertEquals (fKey2, cc.getForeignKey2());
                assertTrue   (fKey2, AbstractContact.getListByForeignKey2(theBase, fKey2).contains(cc));
            }
            logTime("Fetching " + NUM_CONTRACTS + " by foreign keys");
            
            assertEquals (NUM_CONTRACTS,theFac.getAllContacts(ContactFactory.COMPANY_TYPE).size());
        }
        finally {
            // get rid of the company wrappers and knowledgeObjects!
            for (int i=0; i < NUM_CONTRACTS; i++) {
                CompanyContact newCompany = newCompanies[i];
                if (newCompany != null && newCompany.tValid())
                    newCompany.tDelete();
            }
            theBase.commit();
        }
        
    }

	private String limit(int maxLength, String s) {
		if (s.length() > maxLength) {
			return s.substring(0, maxLength);
		}
		return s;
	}

	/**
	 * Testcase for the Foreign Key access using null and such values.
	 */
    public void testStrangeForeignKey()throws Exception {
        KnowledgeBase  theBase = CompanyContact.getDefaultKnowledgeBase();
		assertEmpty(true, AbstractContact.getListByFKey(theBase, "IsNotThere"));
		assertEmpty(true, AbstractContact.getListByForeignKey2(theBase, "IsNotThere"));
    }

    /** 
     * Return the suite of tests to perform. 
     * 
     * @return the suite
     */
    public static Test suite () {
        TestSuite theSuite = new TestSuite(CompanyContactTest.class);
        // Test      theSuite = new CompanyContactTest("testForeignKey");
        return KBSetup.getSingleKBTest(new ContactCreateSetup(theSuite));
    }

}
