/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.filt;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.filt.DOAttributeComparator;
import com.top_logic.dob.filt.DOStringAttributeComparator;
import com.top_logic.dob.simple.ExampleDataObject;

/**
 * TestCase for {@link com.top_logic.dob.filt.DOStringAttributeComparator}.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestDOStringAttributeComparator extends TestCase {

    /**
     * Default Constructor.
     *
     * @param name  name of testFunction to perform. 
     */
    public TestDOStringAttributeComparator(String name) {
        super(name);
    }

    /** 
     * Checks that the Objects are equal according to given coperator.
     */
    public void checkEquality(Comparator c , Object o1, Object o2) {
    
        assertTrue(c.compare(o1,o2) == 0);
        assertTrue(c.compare(o2,o1) == 0);
    }
    
    /** 
     * Checks that the Objects less according to given comparator.
     */
    public void checkLess(Comparator c , Object o1, Object o2, Object o3) {
    
        assertTrue(c.compare(o1,o2) < 0);
        assertTrue(c.compare(o2,o3) < 0);
        assertTrue(c.compare(o1,o3) < 0);

        assertTrue(c.compare(o2,o1) > 0);
        assertTrue(c.compare(o3,o2) > 0);
        assertTrue(c.compare(o3,o1) > 0);
    }
    
    /** 
     * Checks that the Objects less according to given coperator.
     */
    public void checkLess(Comparator c , Object o1, Object o2) {
    
        assertTrue(c.compare(o1,o2) < 0);
        assertTrue(c.compare(o2,o1) > 0);
    }
    
    /** Test Equality with some german collating Strings. */
    public void testEquality() throws ParseException {

        HashMap ex = new HashMap();
        ex.put("name"  , "Muller");
        ex.put("street", "Baumwech");
        ex.put("city"  , "OstrichWinkel");
        ExampleDataObject edo1 = new ExampleDataObject(ex);
        
        ex = new HashMap();
        ex.put("name"  , "Müller");
        ex.put("street", "Baumwech");
        ex.put("city"  , "ÖstrichWinkel");
        ExampleDataObject edo2 = new ExampleDataObject(ex);
        
        ex = new HashMap();
        ex.put("name"  , "Mueller");
        ex.put("street", "Baumwech");
        ex.put("city"  , "OestrichWinkel");
        ExampleDataObject edo3 = new ExampleDataObject(ex);
        
        DOStringAttributeComparator comp = 
			new DOStringAttributeComparator("street", DOAttributeComparator.ASCENDING);
        checkEquality(comp, edo1, edo1); 
        checkEquality(comp, edo1, edo2); 
        checkEquality(comp, edo2, edo3); 

        comp = new DOStringAttributeComparator("name", getPhoneBook());
        checkEquality(comp, edo1, edo1); 
        checkEquality(comp, edo1, edo2); 
        checkEquality(comp, edo2, edo3); 

        comp = new DOStringAttributeComparator(
            new String[] { "name", "city" }, getPhoneBook());
        checkEquality(comp, edo1, edo1); 
        checkEquality(comp, edo1, edo2); 
        checkEquality(comp, edo2, edo3); 
    }

    /** 
     * Simple tests for Less repective Greater values.
     */
    public void testCompare() {
    
        HashMap ex = new HashMap();
        ex.put("name"  , "Mueller");
        ex.put("street", "Anfangstraße");
        ex.put("city"  , "OestrichWinkel");
        ExampleDataObject edo1 = new ExampleDataObject(ex);
        
        ex = new HashMap();
        ex.put("name"  , "Muller");
        ex.put("street", "Mittelstraße");
        ex.put("city"  , "OstrichWinkel");
        ExampleDataObject edo2 = new ExampleDataObject(ex);
        
        ex = new HashMap();
        ex.put("name"  , "Müller");
        ex.put("street", "Zuendestraße");
        ex.put("city"  , "ÖstrichWinkel");
        ExampleDataObject edo3 = new ExampleDataObject(ex);
    
        DOStringAttributeComparator comp = 
            new DOStringAttributeComparator("street");
        checkLess(comp, edo1, edo2, edo3);

        comp = new DOStringAttributeComparator("city", Locale.GERMANY);
        checkLess(comp, edo1, edo2, edo3);
        
        comp = new DOStringAttributeComparator(
            new String[] { "name", "city" } , Locale.GERMANY);
        checkLess(comp, edo1, edo2, edo3);

        comp = new DOStringAttributeComparator(
            new String[] { "name", "city", "street" } , getPhoneBook());
        checkLess(comp, edo1, edo2, edo3);

    }
    

    /** Achive German phoneBooks Sorting (Muller == Müller == Mueller) 
     * 
     * this is actually INCORRECT but fine for these examples.
     */
    private static Collator getPhoneBook() {
          try {
              // This is not correct but fine for this test       
        	  Collator phoneBook = new RuleBasedCollator(
                "<0<1<2<3<4<5<6<7<8<9<"
              + "a;ä=ae=A=Ä=AE=Ae<b=B<c=C<d=D<e=E<f=F<g=G<h=H<i=I<j=J<k=K<l=L<m=M<"
              + "n=N<o=ö=oe=O=Ö=OE=Oe<p=P<q=Q<r=R<s=S & SS=ß<t=T<u=ü=ue=U=Ü=UE=Ue<v=V<"
              + "w=W<x=X<y=Y<z=Z");
              phoneBook.setStrength(Collator.PRIMARY);
              return phoneBook;
        } catch (ParseException e) {
            throw (AssertionError) new AssertionError("ParseException").initCause(e);
        }
        
    }
    

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        return new TestSuite (TestDOStringAttributeComparator.class);
        // return new TestDOStringAttributeComparator("testCompare");
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
