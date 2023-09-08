/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.merge;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.merge.MergeMessage;
import com.top_logic.base.merge.MergeTreeNode;
import com.top_logic.base.merge.Validator;
import com.top_logic.basic.util.ResKey;

/**
 * Example of a Validator, to be used by Testcases.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestingValidator implements Validator {

    /** 
     * Example of converting aList of Strings to a List of numbers.
     * 
     * This is the (almost) fool proof way to do it ...
     */
    @Override
	public boolean validate(MergeTreeNode aNode) {
        Object source = aNode.getSource();
        if (null == source) {
            aNode.addMessage(new MergeMessage(MergeMessage.FATAL,ResKey.text("source is null")));
            return false;
        }
        if (! (source instanceof List)) {
            aNode.addMessage(new MergeMessage(MergeMessage.ERROR,ResKey.text("source is not a List")));
            return false;
        }
        List theList = (List) source;
        if (theList.isEmpty()) {
            aNode.addMessage(new MergeMessage(MergeMessage.WARN,ResKey.text("Input was empty")));
            return true;
        }
        int  size      = theList.size();
        aNode.setDest(new ArrayList(size));
        for (int i=0; i < size; i++) {
            Object theEntry = theList.get(i);
            if (theEntry == null) {
                aNode.addMessage(new MergeMessage(MergeMessage.DEBUG, ResKey.text("Null value at " + i + " will be ignored")));
            }
            if (theEntry instanceof String) {
                aNode.addMessage(new ConvertStringMessage((String) theEntry));
            }
//            if (theEntry == null) {
//                aNode.addMessage(new MergeMessage(MergeMessage.WARN, "Value at " + i + " ignored " + theEntry.getClass()));
//            }
        }
        
        return true;
    }
    
    /** 
     * Inner class doing the actal conversion 
     */
    static class ConvertStringMessage extends MergeMessage {

        /** the value to convert */
        String convert;
        
        /** 
         * ConvertStringMessage
         */
        public ConvertStringMessage(String aString) {
			super(INFO, ResKey.text("Convert '" + aString + "' to int"));
            convert = aString;
        }
        
        /** 
         * Append the converted String to a List found in anOwner.
         * 
         * (But only when approved)
         */
        @Override
		public void perform(MergeTreeNode anOwner) throws Exception {
            if (approved) {
				((List) anOwner.getDest()).add(Integer.valueOf(convert));
            }
        }
    }

}
