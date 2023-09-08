/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.layout.meta.search.SearchFilterSupport;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.layout.form.tag.FormTagUtil;
import com.top_logic.layout.form.tag.TristateCheckboxTag;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.InputSize;
import com.top_logic.util.error.TopLogicException;

/**
 * This tag lifes in a meta element group.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class GroupedMetaInputTag extends MetaInputTag {

    private String name;

    private boolean hideErrorIcon;

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return this.getClass().getName() + " ["
            + "name: '" + this.name +
            "']";
    }

    /**
     * @see com.top_logic.layout.form.tag.AbstractProxyTag#doStartTag()
     */
    @Override
	public int doStartTag() throws JspException {
        AttributeUpdate theUpdate      = this.getAttributeUpdate();
        PageContext     thePageContext = this.getPageContext();
        int             theResult;

        if (theUpdate.isSearchUpdate()) {
            String    theName = SearchFilterSupport.getRelevantAndNegateMemberName(theUpdate.getAttribute(), theUpdate.getDomain());
            TagWriter theOut  = MainLayout.getTagWriter(thePageContext);

            if (!GroupedMetaInputTag.getFormContext(this).hasMember(theName)) {
                theName = null;
            }

            try {
            	TLStructuredTypePart theMA = theUpdate.getAttribute();
            	MetaGroupTag ancestorGroupTag = (MetaGroupTag) TagSupport.findAncestorWithClass(this, MetaGroupTag.class);
            	LayoutComponent theComp = ancestorGroupTag.getComponent();
            	String style = null;
            	if(theComp instanceof AttributedSearchComponent) {
            		style = ((AttributedSearchComponent)theComp).getStyleInformation(theMA, theUpdate.getDomain());
            	}
                theResult = GroupedMetaInputTag.writeSearchInput(this, this.getParent(), thePageContext, theOut, theUpdate, theName, style);

				theOut.flushBuffer();
            }
            catch (JspException ex) {
                throw ex;
            }
            catch (Exception ex) {
                throw new JspException("Unable to write search input for " + theUpdate.getAttribute(), ex);
            }
        }
        else {
            theResult = super.doStartTag();
            if (!hideErrorIcon) {
            	FormContainer containigFormContainer = FormTagUtil.findParentFormContainer(this);
            	if (containigFormContainer.getMember(getFieldName()) instanceof FormField) {
            		MetaErrorTag theError = new MetaErrorTag();
            		
            		theError.setParent(this.getParent());
            		theError.setAttributeUpdate(theUpdate);
            		theError.setPageContext(thePageContext);
            		
            		theError.doStartTag();
            	}
            }
        }

        return theResult;
    }

    /**
     * @see com.top_logic.layout.form.tag.AbstractProxyTag#doEndTag()
     */
    @Override
	public int doEndTag() throws JspException {
        if (this.getImplTag() != null) {
            return super.doEndTag();
        }
        else {
            return EVAL_PAGE;
        }
    }

    /**
     * This method sets the hideErrorIcon.
     *
     * @param    aHideErrorIcon    The hideErrorIcon to set.
     */
    public void setHideErrorIcon(boolean aHideErrorIcon) {
        this.hideErrorIcon = aHideErrorIcon;
    }

    /**
     * Return the attribute name represented by this tag.
     *
     * @return    The requested attribute name.
     */
    public String getName() {
        return (this.name);
    }

    /**
     * Set the attribute name represented by this tag.
     *
     * @param    aName    The attribute name.
     */
    public void setName(String aName) {
        this.name = aName;

        GroupedMetaInputTag.initAttributeUpdate(this, aName);
    }

    /**
     * Initialize the tag and the update by getting information form the surrounding group tag.
     *
     * @param    aMetaTag    The calling tag, must not be <code>null</code>.
     * @param    aName       The name of the accessed attributed field, must not be <code>null</code>.
     */
    public static void initAttributeUpdate(AbstractMetaTag aMetaTag, String aName) {
        AttributeFormContext theContext = GroupedMetaInputTag.getFormContext(aMetaTag);

        try {
            MetaGroupTag ancestorGroupTag = (MetaGroupTag) TagSupport.findAncestorWithClass(aMetaTag, MetaGroupTag.class);

            if (ancestorGroupTag == null) {
                throw new RuntimeException("Input tag '" + aName + "' is not located within an meta group tag (meta:group)!");
            }

            Wrapper      theAttributed = ancestorGroupTag.getAttributedObject();
            TLClass     theME;
            TLStructuredTypePart   theMA;
            AttributeUpdate theUpdate;
            if (aName.indexOf('.') > 0) {
                int theSeparatorIndex = aName.lastIndexOf('.');
                String thePath          = aName.substring(0, theSeparatorIndex);
                String theAttributeName = aName.substring(theSeparatorIndex + 1);
                theME     = ancestorGroupTag.getDerivedME(thePath);
				theMA = MetaElementUtil.getMetaAttribute(theME, theAttributeName);
                theUpdate = theContext.getAttributeUpdateContainer().getAttributeUpdate(theMA, theAttributed, thePath);
                aMetaTag.setDomain(thePath);
            } else {
				theME =
					(TLClass) ((theAttributed == null) ? ancestorGroupTag.getMetaElement() : theAttributed.tType());
				theMA = MetaElementUtil.getMetaAttribute(theME, aName);
                theUpdate = theContext.getAttributeUpdateContainer().getAttributeUpdate(theMA, theAttributed);
                aMetaTag.setDomain(null);
                
				// Note: An attribute with domain (a deep search attribut of
				// another object) must not prevent the the same own attribute
				// from being displayed in the "other attributes" section.
				// Therefore, no attribute update with domain must be marked as
				// displayed.
				MetaTagUtil.addDisplayedAttribute(aMetaTag.getPageContext(), theAttributed, theMA);
            }

            if (theUpdate != null) {
                aMetaTag.setAttributeUpdate(theUpdate);
            }
            else {
                throw new TopLogicException(GroupedMetaInputTag.class, "init.update.null", new Object[] {aName});
            }
        }
        catch (NoSuchAttributeException ex) {
            throw new RuntimeException("Unable to get attribute update for '" + aName + "'!", ex);
        }
    }

    /**
	 * Check if object for given tag contains an AttribueUpdate for aName.
	 */
    public static boolean hasAttributeUpdate(Tag metaTag, String aName) {
        AttributeFormContext theContext = GroupedMetaInputTag.getFormContext(metaTag);

		MetaGroupTag ancestorGroupTag = (MetaGroupTag) TagSupport.findAncestorWithClass(metaTag, MetaGroupTag.class);

		if (ancestorGroupTag == null) {
			throw new RuntimeException(
				"Input tag '" + aName + "' is not located within an meta group tag (meta:group)!");
        }

		Wrapper theAttributed = ancestorGroupTag.getAttributedObject();
		TLStructuredType theME = (theAttributed == null) ? ancestorGroupTag.getMetaElement() : theAttributed.tType();
		TLStructuredTypePart part = theME.getPart(aName);
		if (part == null) {
			return false;
        }
		AttributeUpdate theUpdate =
			theContext.getAttributeUpdateContainer().getAttributeUpdate(part, theAttributed);

		return (theUpdate != null);
    }

    /**
     * Return the form context the given tag lives in.
     *
     * @param    aTag    The tag requesting the form context.
     * @return   The ontext the tag lives in.
     */
    protected static AttributeFormContext getFormContext(Tag aTag) {
        FormTag theFormTag = (FormTag) TagSupport.findAncestorWithClass(aTag, FormTag.class);

        return (AttributeFormContext) theFormTag.getFormContext();
    }

    /**
     * Write a search input representation for the given parameters.
     *
     * @param    aParent          The tag context the calling will be performed in.
     * @param    aContext         The page context the writer is working in.
     * @param    anOut            The writer to be used.
     * @param    anUpdate         The attribute update containing the data.
     * @param    aCheckboxName    The name of the the search check box (positive, negative, neutral).
     * @param    aStyle           The displaying style of the field to be written.
     * @return   Information how to perform further.
     */
    public static int writeSearchInput(Tag aParent, PageContext aContext, TagWriter anOut, AttributeUpdate anUpdate, String aCheckboxName, String aStyle) throws JspException, IOException {
        return writeSearchInput(null, aParent, aContext, anOut, anUpdate, aCheckboxName, aStyle);
    }

    /**
     * Write a search input representation for the given parameters.
     *
     * @param    anOrig           The calling original tag.
     * @param    aParent          The tag context the calling will be performed in.
     * @param    aContext         The page context the writer is working in.
     * @param    anOut            The writer to be used.
     * @param    anUpdate         The attribute update containing the data.
     * @param    aCheckboxName    The name of the the search check box (positive, negative, neutral).
     * @param    aStyle           The displaying style of the field to be written.
     * @return   Information how to perform further.
     */
    public static int writeSearchInput(MetaInputTag anOrig, Tag aParent, PageContext aContext, TagWriter anOut, AttributeUpdate anUpdate, String aCheckboxName, String aStyle) throws JspException, IOException {
        MetaInputTag theInput = new MetaInputTag();
        MetaErrorTag theError = new MetaErrorTag();

        GroupedMetaInputTag.writeSearchCheckbox(aParent, aContext, anOut, aCheckboxName);

        int          theResult;

        if (!AttributeOperations.allowsSearchRange(anUpdate.getAttribute())) {
            theResult = GroupedMetaInputTag.writeSearchInputTag(anOrig, theInput, theError, aParent, aContext, anUpdate, aStyle);
        }
        else {
            theInput.setPart(AttributeFormFactory.SEARCH_FROM_FIELDNAME);
            theError.setPart(AttributeFormFactory.SEARCH_FROM_FIELDNAME);
            GroupedMetaInputTag.writeSearchInputTag(anOrig, theInput, theError, aParent, aContext, anUpdate, aStyle);
            anOut.writeText(HTMLConstants.NBSP + "-" + HTMLConstants.NBSP + HTMLConstants.NBSP + HTMLConstants.NBSP + HTMLConstants.NBSP);

            theInput.setPart(AttributeFormFactory.SEARCH_TO_FIELDNAME);
            theError.setPart(AttributeFormFactory.SEARCH_TO_FIELDNAME);

            theResult = GroupedMetaInputTag.writeSearchInputTag(anOrig, theInput, theError, aParent, aContext, anUpdate, aStyle);
        }

        return theResult;
    }

    /**
     * Write a search check box for the given parameters.
     *
     * @param    aParent          The tag context the calling will be performed in.
     * @param    aContext         The page context the writer is working in.
     * @param    anOut            The writer to be used.
     * @param    aCheckboxName    The name of the the search check box (positive, negative, neutral).
     */
    private static void writeSearchCheckbox(Tag aParent, PageContext aContext, TagWriter anOut, String aCheckboxName) throws JspException, IOException {
        if (!StringServices.isEmpty(aCheckboxName)) {
            TristateCheckboxTag theCheckbox = new TristateCheckboxTag();

            theCheckbox.setName(aCheckboxName);
            theCheckbox.setParent(aParent);
            theCheckbox.setPageContext(aContext);

            theCheckbox.doStartTag();
            theCheckbox.doEndTag();

            anOut.writeText(HTMLConstants.NBSP);
        }
    }

    /**
     * Write a search input representation for the given parameters.
     *
     * @param    anOrig           The calling original tag.
     * @param    input            ???
     * @param    error            ???
     * @param    aParent          The tag context the calling will be performed in.
     * @param    aContext         The page context the writer is working in.
     * @param    anUpdate         The attribute update containing the data.
     * @param    aStyle           The displaying style of the field to be written.
     * @return   Information how to perform further.
     */
    protected static int writeSearchInputTag(MetaInputTag anOrig, MetaInputTag input, MetaErrorTag error, Tag aParent, PageContext aContext, AttributeUpdate anUpdate, String aStyle) throws JspException {
        input.setAttributeUpdate(anUpdate);
        input.setParent(aParent);
        input.setPageContext(aContext);

        error.setAttributeUpdate(anUpdate);
        error.setParent(aParent);
        error.setPageContext(aContext);
        if(!StringServices.isEmpty(aStyle)) {
        	input.setStyle(aStyle);
        }

        if (anOrig != null) {
			input.setInputSize(DisplayAnnotations.inputSize(anOrig, MetaInputTag.NO_INPUT_SIZE));

			InputSize annotation = anOrig.getLocalAnnotation(InputSize.class);
			if (annotation != null) {
				input.setLocalAnnotation(annotation);
			}
        }
        input.setDomain(anUpdate.getDomain());
        error.setDomain(anUpdate.getDomain());

        int theResult = input.doStartTag();
        input.doEndTag();

        boolean showError = (anOrig == null); // Ticket #2249 When called from ShowGroupAttributes, anOrig is null!

        if(anOrig instanceof GroupedMetaInputTag){
        	showError = !((GroupedMetaInputTag)anOrig).hideErrorIcon;
        }

        if (showError) {
        	error.doStartTag();
        	error.doEndTag();
        }

        return theResult;
    }
}

