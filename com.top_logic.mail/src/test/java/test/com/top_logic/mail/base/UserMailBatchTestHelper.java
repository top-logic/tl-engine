/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import java.util.Collection;

import com.top_logic.base.mail.UserMailBatch;
import com.top_logic.base.user.UserInterface;

/** 
 * This class is an helper-class for testing {@link com.top_logic.base.mail.UserMailBatch}.
 * 
 * It extends this class and overwrite its method createRecipientSet().
 * Use this class only for test-purpose.
 *
 * @author    hbo
 */
public class UserMailBatchTestHelper extends UserMailBatch {
    
    /**
     * Invoke the constructor of the super-class.
     */
    public UserMailBatchTestHelper(String aUserList, String aSubject, 
                                     String aBody){
          super(aUserList, aSubject, aBody);                               
    }
    
    /**
     * Invoke the overwritten method of super-class.
     */    
    @Override
	public Collection<UserInterface> createRecipientSet(){
        return super.createRecipientSet();
    }
}
