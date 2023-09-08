/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.security.attributes.PersonAttributes;
import com.top_logic.base.user.UserFullNameComparator;
import com.top_logic.base.user.douser.DOUser;
import com.top_logic.dob.simple.ExampleDataObject;
import com.top_logic.util.TLContext;

/**
 * Testcase for the {@link UserFullNameComparator}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestUserFullNameComparator extends BasicTestCase {

    /**
     * Check if Equals method holds its promise.
     */
    public void testEquals() {
        UserFullNameComparator ufnUp   = new UserFullNameComparator(true);
        UserFullNameComparator ufnDown = new UserFullNameComparator(false);
        
        assertEquals(ufnUp  , ufnUp);
        assertEquals(ufnDown, ufnDown);
        assertFalse(ufnUp   .equals(ufnDown));
        assertFalse(ufnDown.equals(ufnUp));
    }

    /**
     * Check if Collating actually works
     */
    public void testCollating() {
        String[] attrNames = new String[] 
         { PersonAttributes.SUR_NAME, PersonAttributes.GIVEN_NAME, PersonAttributes.USER_NAME};
        
        DOUser alm = DOUser.getInstance(
            new ExampleDataObject( attrNames,
               new String[] { "Marksdorf", "Alfred", "alm" }));

        DOUser emy = DOUser.getInstance(
           new ExampleDataObject( attrNames,
              new String[] { "Myrdonk", "Etienne", "emy" }));

        DOUser wfm = DOUser.getInstance(
            new ExampleDataObject( attrNames,
				new String[] { "Möller", "Wilfried", "wfm" }));
                        
        DOUser aum = DOUser.getInstance(
            new ExampleDataObject( attrNames,
				new String[] { "Möller", "Auguste", "aum" }));
        
        DOUser anm = DOUser.getInstance(
             new ExampleDataObject( attrNames,
				new String[] { "Moller", "Shila", "anm" }));
        
        DOUser atm = DOUser.getInstance(
             new ExampleDataObject( attrNames,
				new String[] { "Moeller", "Antoine", "atm" }));
               
		TLContext tcxt = TLContext.getContext();

		tcxt.setCurrentLocale(Locale.GERMAN);
		testSort(list(alm, atm, anm, aum, wfm, emy));

		Locale swedish = new Locale("sv");
		if (!list(Locale.getAvailableLocales()).contains(swedish)) {
			fail("No test for usage of correct Locale available.");
		}
		/* In Sweden ö is an own letter at the end of the alphabet. */
		tcxt.setCurrentLocale(swedish);
		testSort(list(alm, atm, anm, emy, aum, wfm));
	}

	private void testSort(List<DOUser> expectedSortedList) {
		UserFullNameComparator ufnc = new UserFullNameComparator();
		List<DOUser> theList = new ArrayList<>(expectedSortedList);

		Random random = new Random(hashCode());
		int repitions = 20;
		while (repitions-- > 0) {
			Collections.shuffle(theList, random);
			Collections.sort(theList, ufnc);
			assertSorted(theList, ufnc);
			assertEquals(expectedSortedList, theList);
		}
	}
    public static Test suite () {
		return TLTestSetup.createTLTestSetup(TestUserFullNameComparator.class);
    }

}
