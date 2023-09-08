/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Iterator;

import com.top_logic.basic.Logger;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Operations on {@link TLStructuredTypePart}s of type {@link WebFolder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebFolderAttributeOperations {

	/**
	 * Simple class that holds the Wrapper to which a WebFolder is attached as the value 
	 * of a WebFolderMetaAttribute and the WebFolder type
	 * of that attribute 
	 * 
	 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
	 */
	public static class ResponsibilityData {
	    /**
	     * CTor with all attributes
	     * @param aResp the responsible object
	     * @param aType the webfolder type of the attribute
	     */
		public ResponsibilityData(TLObject aResp, TLStructuredTypePart anMA, String aType) {
	        this.setResponsibleObject(aResp);
	        this.setMetaAttribute(anMA);
	        this.setWebFolderType(aType);
	    }

		public void setResponsibleObject(TLObject responsibleObject) {
			this.responsibleObject = responsibleObject;
		}

		public TLObject getResponsibleObject() {
			return responsibleObject;
		}
		public void setWebFolderType(String webFolderType) {
			this.webFolderType = webFolderType;
		}
		public String getWebFolderType() {
			return webFolderType;
		}
		public void setMetaAttribute(TLStructuredTypePart metaAttribute) {
			this.metaAttribute = metaAttribute;
		}
		public TLStructuredTypePart getMetaAttribute() {
			return metaAttribute;
		}
		/** the responsible object. */
		private TLObject responsibleObject;
	    /** the webfolder type of the attribute. */
	    private String  webFolderType;
	    /** the MetaAttributa via which the object is connected to the responsible object. */
	    private TLStructuredTypePart metaAttribute;
	}

	/**
	 * Get the responsibility data of the given folder,
	 * i.e. the Wrapper to which it is attached as the value 
	 * of a WebFolderMetaAttribute and the WebFolder type
	 * of that attribute 
	 * 
	 * @param aValue    the WebFolder. Must not be <code>null</code>.
	 * @return the responsibility data or <code>null</code> if exceptions
	 * occur or the folder does not belong to a WebFolderMetaAttribute.
	 */
	public static WebFolderAttributeOperations.ResponsibilityData getResponsibilityData(WebFolder aValue) {
	    // Get the top level WebFolder
	    WebFolder theParent = aValue;
	    while (theParent != null) {
			TLObject theParentW = aValue.getOwner();
	        if (theParentW instanceof WebFolder) {
	            theParent = (WebFolder) theParentW;
	            aValue = theParent;
	        }
	        else {
	            theParent = null;
	        }
	    }
	    
	    // Iterate the WRAPPER_ATTRIBUTE_ASSOCIATIONs
	    Iterator<KnowledgeAssociation> theKAs;
		theKAs = WrapperMetaAttributeUtil.getIncomingAssociations(aValue);
	
	    while (theKAs.hasNext()) {
	        KnowledgeAssociation ass = theKAs.next();
	        try {
				TLStructuredTypePart theMA = WrapperMetaAttributeUtil.getMetaAttribute(ass);
	
				KnowledgeObject contractKO = ass.getSourceObject();
				TLObject theObj = WrapperFactory.getWrapper(contractKO);
				String theType = AttributeOperations.getFolderType(theMA);

				return new WebFolderAttributeOperations.ResponsibilityData(theObj, theMA, theType);
	        }
	        catch (Exception ex) {
				Logger.info("Failed do get value", ex, WebFolderAttributeOperations.class);
	        }
	    }
	    
	    return null;
	}

	/**
	 * Get the responsibility data of the given folder,
	 * i.e. the Wrapper to which it is attached as the value 
	 * of a WebFolderMetaAttribute and the WebFolder type
	 * of that attribute 
	 * 
	 * @param aValue    the WebFolder. Must not be <code>null</code>.
	 * @return the responsibility data or <code>null</code> if exceptions
	 * occur or the folder does not belong to a WebFolderMetaAttribute.
	 */
	public static ResponsibilityData getResponsibilityData(Document aValue) {
		// Handling via WebFolder is the default
		ResponsibilityData theData = null; 
	
		{
			theData = getResponsibilityData(WebFolder.getWebFolder(aValue));
			
			// Special handling for DocumentMetaAttributes
			// The Document is stored in a WebFolder which might belong to a WebFolderMetaAttribute
			// but we have to find the DocumentMetaAttribute to which it belongs.
			if (theData != null) {
				Iterator<KnowledgeAssociation> theKAs = WrapperMetaAttributeUtil.getIncomingAssociations(aValue);
				if (theKAs.hasNext()) {
					KnowledgeAssociation theKA = theKAs.next();
					try {
	    	            TLStructuredTypePart theMA = WrapperMetaAttributeUtil.getMetaAttribute(theKA);
	    				if (theKAs.hasNext()) {
							Logger.warn("Document is attached to multiple MetaAttributes. Will use first one. ("
								+ aValue + ")", WebFolderAttributeOperations.class);
	    				}
	    				
						if (AttributeOperations.isDocumentAttribute(theMA)) {
	    					theData.setMetaAttribute(theMA);
	    				}
	    				else {
							Logger.warn("Document is attached to unexpected attribute type: " + theMA
								+ ". Will try WebFolder. (" + aValue + ")", WebFolderAttributeOperations.class);
	    				}
					} catch (NoSuchAttributeException e) {
						Logger.warn("Can't resolve DocumentMetaAttribute. Will try WebFolder. (" + aValue + ")",
							WebFolderAttributeOperations.class);
					}
				}
			}
		}
		
	    return theData;
	}

}
