/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.util;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.util.RegularExpressionLabelFilter;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TestRegularExpressionLabelFilter extends BasicTestCase {
    
    public void testAccept() throws Exception {
        Filter theFilter;
        FormMember theMember = FormFactory.newStringField("dummy");
        
        theFilter = new RegularExpressionLabelFilter("[0-9]*");
        theMember.setLabel("nixZahl");
        assertFalse(theFilter.accept(theMember));
        theMember.setLabel("0815");
        assertTrue(theFilter.accept(theMember));
    }
    

    public static Test suite () {
        return TLTestSetup.createTLTestSetup(TestRegularExpressionLabelFilter.class);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
