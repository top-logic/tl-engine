/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.handler.structured;

import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObject;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper;
import com.top_logic.importer.base.ImportValueProvider;
import com.top_logic.importer.base.StructuredDataImportPerformer.StructureImportResult;

/**
 * Import a work process element.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class StructuredElementDOImportHandler<C extends StructuredElementDOImportHandler.Config, O extends AttributedStructuredElementWrapper> extends AbstractAttributedDOImportHandler<C, O> {

    public interface Config extends AbstractAttributedDOImportHandler.Config {

        /** Attribute name for setting the unique ID of an structured element. */
        @StringDefault("id")
        @Name("id")
        String getID();

        /** Optional name of the structured element to be created. */
        String getElementType();
    }

	/** 
	 * Creates a {@link StructuredElementDOImportHandler}.
	 */
	public StructuredElementDOImportHandler(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);
	}

	/**
	 * Set a unique element ID (for a reason look at
	 * {@link #getElementID(AttributedStructuredElementWrapper)}).
	 * 
	 * @param anElement
	 *        The element to set the ID for.
	 * @param anID
	 *        The unique ID to be set.
	 */
	protected void setElementID(O anElement, String anID) {
        anElement.setString(this.getConfig().getID(), anID);
    }

    /** 
     * Return the unique ID to identify this element within the children of its parent.
     * 
     * <p>During import this ID will be used to identify an element as child from its parent.
     * When there is no child with that ID, a new structured element will be created as child
     * of the currently visited parent.</p>
     * 
     * @param    anElement    The element to get the ID from.
     * @return   The requested ID.
     */
    protected String getElementID(O anElement) {
        return anElement.getString(this.getConfig().getID());
    }

	@Override
    public ResKey execute(Map<String, Object> someObjects, ImportValueProvider valueProvider, StructureImportResult aResult) {
	    ResKey     theResult;
	    DataObject theDO    = valueProvider.getDO();
	    String     theType  = this.getElementType(theDO);
		String     theID    = theDO.getIdentifier().toString();
        O          theBase  = this.getCurrentObject(someObjects);
        O          theChild = this.findChild(theBase, theType, theID);
        boolean    created  = (theChild == null);

        if (created) {
            theChild  = this.createChild(theBase, theType);
			theResult = I18NConstants.OBJECT_CREATED.fill(theChild.getName());
        }
        else { 
			theResult = I18NConstants.OBJECT_UPDATED.fill(theChild.getName());
        }

        someObjects.put(this.getConfig().getName(), theChild);
        someObjects.put(AbstractDOImportHandler.CURRENT_OBJECT_KEY, theChild);

        this.excecuteUpdate(theChild, valueProvider, aResult, created);
        this.postProcess(theChild, valueProvider, theBase, created);

        if (!StringServices.isEmpty(theID)) {
            this.setElementID(theChild, theID);
        }

        return theResult;
	}

    protected O createChild(O aBase, String aType) {
	    StructuredElement theChild = aBase.createChild("_CREATED_", aType);

        return this.toObject(theChild);
    }

	protected O findChild(O aBase, String aType, String anID) {
        O theChild = null;

		if (!StringServices.isEmpty(anID)) {
			for (StructuredElement theElt : aBase.getChildren()) {
                if (aType.equals(theElt.getElementType())) {
                    O theO = this.toObject(theElt);

                    if (anID.equals(this.getElementID(theO))) {
                        theChild = theO;
                    }
                }
            }
        }

        return theChild;
    }

    private String getElementType(DataObject theDO) {
        String theType = this.getConfig().getElementType();

        return StringServices.isEmpty(theType) ? theDO.tTable().getName() : theType;
    }

    @SuppressWarnings("unchecked")
    private O toObject(StructuredElement anElement) {
        return (O) anElement;
    }
}
