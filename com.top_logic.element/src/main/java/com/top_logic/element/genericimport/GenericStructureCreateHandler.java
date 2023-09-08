/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Properties;

import com.top_logic.basic.StringServices;
import com.top_logic.element.genericimport.interfaces.GenericCreateHandler;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.knowledge.objects.CreateException;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 * 
 * @deprecated Used spurious attribute "parentId", which is not available in general.
 */
@Deprecated
public class GenericStructureCreateHandler extends MetaElementBasedUpdateHandler implements GenericCreateHandler {

    private StructuredElement structureRoot;

	public GenericStructureCreateHandler(Properties someProps) {
		super(someProps);
		String theStructureName = someProps.getProperty("structureName");
		if (StringServices.isEmpty(theStructureName)) {
		    throw new IllegalArgumentException("Structure name must not be null");
		}
		structureRoot = StructuredElementFactory.getInstanceForStructure(theStructureName).getRoot();
	}

	@Override
	public Object createBusinessObject(GenericValueMap ado, String aFKeyAttr) throws CreateException {
		{
		    String theType     = ado.getType();
			String parentId    = (String) ado.getAttributeValue("parentId");
			String elementType = (String) ado.getAttributeValue("elementType");
			StructuredElement theParent = structureRoot;
			
			StructuredElement theElement;
			if (! StringServices.isEmpty(parentId)) {
			    
			    if (StringServices.isEmpty(elementType)) {
			        throw new CreateException("elementType must not be null if a parentId is specified!");
			    }
			    
			    theParent = (StructuredElement) this.getImportConfiguration().getCache().get(theType, parentId);
			    
			    if (theParent == null) {
			        throw new CreateException("No parent found for parentId '" + parentId + "'. Please make sure that structred elements are imported in order!");
			    }
			    
			    Object theName  = ado.getAttributeValue("name");
                theElement = theParent.createChild((String) theName, elementType);
			}
			else {
			    theElement = theParent;
			}
			
			super.updateBusinessObject(theElement, ado, aFKeyAttr);

			return theElement;
		}
	}
}
