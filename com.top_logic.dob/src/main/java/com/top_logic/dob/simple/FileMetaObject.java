
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.simple;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.meta.MOStructureImpl;

/**
 * A MetaObject describing a File.
 *
 * @author  Klaus Halfmann
 */
public class FileMetaObject {

    /** This is a special way of a singleton */
    public static MOStructureImpl SINGLETON = createType();
    
    /** private Constructor, since we are a singleton */
    private static MOStructureImpl createType() {
        MOStructureImpl result = new MOStructureImpl("File");
        try {
			result.addAttribute(new MOAttributeImpl("isReadable", MOPrimitive.BOOLEAN, MOAttribute.MANDATORY));
			result.addAttribute(new MOAttributeImpl("isWriteable", MOPrimitive.BOOLEAN, MOAttribute.MANDATORY));
			result.addAttribute(new MOAttributeImpl("isEntry", MOPrimitive.BOOLEAN, MOAttribute.MANDATORY));
			result.addAttribute(new MOAttributeImpl("isContainer", MOPrimitive.BOOLEAN, MOAttribute.MANDATORY));
			result.addAttribute(new MOAttributeImpl("exists", MOPrimitive.BOOLEAN, MOAttribute.MANDATORY));
			result.addAttribute(new MOAttributeImpl("length", MOPrimitive.LONG, MOAttribute.MANDATORY));
			result.addAttribute(new MOAttributeImpl("lastModified", MOPrimitive.DATE, MOAttribute.MANDATORY));

			// name is just the file.name
			result.addAttribute(new MOAttributeImpl("name", MOPrimitive.STRING, MOAttribute.MANDATORY));
			// physicalResource is the absolute pathname
			result.addAttribute(new MOAttributeImpl("physicalResource", MOPrimitive.STRING, MOAttribute.MANDATORY));
		} catch (DuplicateAttributeException dax) {
			throw new UnreachableAssertion(dax);
		}
		
		return result;
	}
}
