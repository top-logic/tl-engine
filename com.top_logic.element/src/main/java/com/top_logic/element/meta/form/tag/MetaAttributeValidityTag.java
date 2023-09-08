/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.ValidityCheck;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.component.TouchMetaAttributeCommandHandler;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.tag.ButtonTag;
import com.top_logic.layout.form.tag.js.JSString;
import com.top_logic.layout.table.renderer.ThemeBasedResourceImageRenderer;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Resources;

/**
 * Renders the validity indicator for a meta attribute.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class MetaAttributeValidityTag extends ButtonTag {

	/** The attribute. */
    private TLStructuredTypePart metaAttributeObj;

	/** The object. */
    private Wrapper attributedObj;

    /** Indicates whether valid attributes are allowed to get touched. */
    private boolean allowTouchValid = true;


    public MetaAttributeValidityTag() {
        super();
        setExecStateInTooltip(false);
    }


    @Override
	protected void teardown() {
        super.teardown();
        setExecStateInTooltip(false);
        this.attributedObj = null;
        this.metaAttributeObj = null;
        this.allowTouchValid = true;
    }


    // Getter and setter methods

    public void setAttributed (Wrapper anAttributed) {
        if (anAttributed == null) {
			throw new IllegalArgumentException("Object must not be null");
        }
        this.attributedObj = anAttributed;
    }

    public Wrapper getAttributed () {
        return this.attributedObj;
    }

    public void setMetaAttribute(TLStructuredTypePart aMetaAttribute) {
        if (aMetaAttribute == null) {
			throw new IllegalArgumentException("Attribute must not be null");
        }
        this.metaAttributeObj = aMetaAttribute;
    }

    public TLStructuredTypePart getMetaAttribute() {
        return this.metaAttributeObj;
    }

    public void setAllowTouchValid(boolean allowTouchValid) {
        this.allowTouchValid = allowTouchValid;
    }

    @Override
	protected int startFormMember() throws IOException, JspException {
    	TLStructuredTypePart theMA = this.getMetaAttribute();
    	
    	try {
    		Wrapper           theAttributed    = this.getAttributed();
    		String               theState          = WrapperMetaAttributeUtil.getValidityState(theAttributed, theMA);
    		AttributeFormContext theFC            = (AttributeFormContext) this.getFormContext();
    		ValidityCheck        theValidityCheck = AttributeOperations.getValidityCheck(theMA);
    		
    		if (theFC.isImmutable() || theValidityCheck.isReadOnly() ||
    				(!allowTouchValid && MetaElementUtil.STATE_GREEN.equals(theState)) ||
    				MetaElementUtil.STATE_WHITE.equals(theState)) {
    			this.setDisabled(true);
    		}
    		Date theNextTouch = WrapperMetaAttributeUtil.getNextTimeout(theAttributed, theMA);
    		this.setLabel(computeLabel(theState, theValidityCheck, theNextTouch));
    		
			setIcon(
				ThemeImage.i18n(ResPrefix.legacyString(theState).key(ThemeBasedResourceImageRenderer.IMAGE_SUFFIX)));
    		this.command.set(TouchMetaAttributeCommandHandler.COMMAND_ID);
    		
    		Map theArguments = new HashMap();
    		theArguments.put(TouchMetaAttributeCommandHandler.PARAMETER_FIELD, new JSString(MetaAttributeGUIHelper.getAttributeID(theMA, theAttributed)));
    		this.setArguments(theArguments);
    	}
    	catch (Exception ex) {
    		throw new JspException(ex);
    	}
    	return super.startFormMember();
    }

    /**
     * Computes the label (title) for a meta attribute validity bubble.
     *
     * @param aState
     *        the state of the attribute
     * @param aValidityCheck
     *        the validity check of the attribute
     * @param aExpireDate
     *        the expiration date of the attribute
     * @return the i18N-ed label for a meta attribute validity bubble
     */
    public static String computeLabel(String aState, ValidityCheck aValidityCheck, Date aExpireDate) {
		ResKey theLabel = I18NConstants.VALIDITY_DEFAULT;
        if (!aValidityCheck.isActive()) {
			theLabel = I18NConstants.NO_VALIDITY_CHECK;
        }
        else if (aValidityCheck.isReadOnly()) {
			theLabel = I18NConstants.VALIDITY_READ_ONLY;
        }
        else if (MetaElementUtil.STATE_WHITE.equals(aState)) {
			theLabel = I18NConstants.NO_VALIDITY_CHECK;
        }
        else if (MetaElementUtil.STATE_RED.equals(aState)) {
			theLabel = I18NConstants.VALIDITY_EXPIRED;
            aExpireDate = DateUtil.nextDay(aExpireDate);
        }
        else if (MetaElementUtil.STATE_YELLOW.equals(aState)) {
			theLabel = I18NConstants.VALIDITY_DEFAULT;
        }
        else if (MetaElementUtil.STATE_GREEN.equals(aState)) {
			theLabel = I18NConstants.VALIDITY_UP_TO_DATE;
        }
		return (Resources.getInstance().getMessage(theLabel, aExpireDate));
    }

}
