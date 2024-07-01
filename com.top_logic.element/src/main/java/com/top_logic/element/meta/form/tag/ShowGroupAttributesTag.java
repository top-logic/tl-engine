/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.layout.meta.search.SearchFilterSupport;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.component.EditAttributedComponent;
import com.top_logic.element.meta.gui.CreateAttributedComponent;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.boxes.reactive_tag.ColumnsLayoutTag;
import com.top_logic.layout.form.boxes.reactive_tag.DescriptionCellTag;
import com.top_logic.layout.form.boxes.reactive_tag.DescriptionTag;
import com.top_logic.layout.form.boxes.reactive_tag.GroupCellTag;
import com.top_logic.layout.form.tag.AbstractTag;
import com.top_logic.layout.form.tag.ControlBodyTag;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.LabelPosition;

/**
 * The {@link ShowGroupAttributesTag} creates input fields for MetaAttributes.
 *
 * @author <a href="mailto:mga@top-logic.com">mga</a>
 */
public class ShowGroupAttributesTag extends AbstractTag implements ControlBodyTag {

    /** I18N key for the legend. */
    private String legend;

	/** Set of attribute names to be excluded from display. */
    private Set exclude;

    /** Indicates if a field set is shown. */
    private boolean showFieldSet = true;

	private final DescriptionCellTag _cell = new DescriptionCellTag();

	private final DescriptionTag _description = new DescriptionTag();

	private ControlBodyTag _capture;

	private Integer _columns;

	private String _firstColumnWidth;

	private Boolean _labelAbove;

	private boolean _splitControls;

    /**
     * This method sets the legendName.
     *
     * @param    aLegendName    The legendName to set.
     */
    public void setLegend(String aLegendName) {
        this.legend = aLegendName;
    }

    /**
     * This method sets the exclude.
     *
     * @param    aExclude    The exclude to set.
     */
    public void setExclude(Set aExclude) {
        this.exclude = aExclude;
    }

	/**
	 * This method sets the maximal number of columns.
	 * 
	 * @param columns
	 *        Maximal number of columns.
	 */
	public void setColumns(int columns) {
		_columns = columns;
	}

	/**
	 * This method sets the width of the first column.
	 * 
	 * @param firstColumnWidth
	 *        The width of the first column.
	 */
	public void setFirstColumnWidth(String firstColumnWidth) {
		_firstColumnWidth = firstColumnWidth;
	}

	/**
	 * This method sets whether the label is rendered above the input.
	 * 
	 * @param labelAbove
	 *        If <code>true</code> the label is rendered above the input.
	 */
	public void setLabelAbove(boolean labelAbove) {
		_labelAbove = labelAbove;
	}

	/**
	 * Whether the controls which belong to the descriptionCell are splitted instead of rendered one
	 * after each other.
	 * 
	 * @param splitControls
	 *        If <code>true</code> the controls are splitted.
	 */
	public void setSplitControls(boolean splitControls) {
		_splitControls = splitControls;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		if (_capture != null) {
			return _capture.addControl(childControl);
		}
		try {
			childControl.write(DefaultDisplayContext.getDisplayContext(pageContext), getOut());
			return null;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected int startElement() throws JspException, IOException {
        // The context containing the values.
        AttributeFormContext theContext = this.getFormContext();
        Collection           theColl    = theContext.getAllUpdatesWithFormConstraints();
        boolean              isEmpty    = true;
        Set                  theExclude = (this.exclude == null) ? new HashSet() : new HashSet(this.exclude);
        LayoutComponent      theComp    = this.getComponent();
        TLStructuredTypePart        theMA;

        if (theComp instanceof CreateAttributedComponent) {
            theExclude.addAll(((CreateAttributedComponent) theComp).getExcludeForUI());
        }
        else if (theComp instanceof EditAttributedComponent) {
            theExclude.addAll(((EditAttributedComponent) theComp).getExcludeListForUI());
        }

        for (Iterator theIt = theColl.iterator(); isEmpty && theIt.hasNext(); ) {
			AttributeUpdate theUpdate = (AttributeUpdate) theIt.next();
            if (theUpdate.getDomain() != null) {
				// Note: An attribute with domain (a deep search attribute from
				// another object) must not be displayed in the
				// "other attributes" section, because it's not clear at the UI,
				// what this attribute means.
            	continue;
            }
            theMA     = theUpdate.getAttribute();
            isEmpty   = MetaTagUtil.wasDisplayed(pageContext, theUpdate.getObject(), theMA) || theExclude.contains(theMA.getName());
        }

		if (!isEmpty) {
			GroupedMetaLabelTag theLabel = new GroupedMetaLabelTag();
			GroupedMetaErrorTag theError = new GroupedMetaErrorTag();
			BodyTagSupport containerTag;
			if (this.showFieldSet) {
				GroupCellTag groupTag = new GroupCellTag();

				groupTag.setParent(this);
				groupTag.setPageContext(pageContext);
				groupTag.setPersonalizationName("additionalAttributes");
				if (this.legend == null) {
					groupTag.setTitleKeyConst(I18NConstants.GROUP_ADDITIONAL_ATTRIBUTES);
				} else {
					groupTag.setTitleKeySuffix(this.legend, I18NConstants.GROUP_ADDITIONAL_ATTRIBUTES);
				}
			if (_labelAbove != null) {
				groupTag.setLabelAbove(_labelAbove);
			}
			if (_columns != null) {
				groupTag.setColumns(_columns);
			}

				containerTag = groupTag;
			} else {
				containerTag = new ColumnsLayoutTag();
				((ColumnsLayoutTag) containerTag).setCount(2);
			}
			containerTag.setPageContext(pageContext);
			containerTag.setParent(getParent());
			containerTag.doStartTag();
			containerTag.setBodyContent(pageContext.pushBody());
			containerTag.doInitBody();

            for (java.util.Iterator theIt  = theColl.iterator(); theIt.hasNext(); ) {
				AttributeUpdate theUpdate = (AttributeUpdate) theIt.next();
                if (theUpdate.getDomain() != null) {
    				// Note: An attribute with domain (a deep search attribute from
    				// another object) must not be displayed in the
    				// "other attributes" section, because it's not clear at the UI,
    				// what this attribute means.
                	continue;
                }
                theMA     = theUpdate.getAttribute();

				TLObject attributed = theUpdate.getObject();
                if (!(MetaTagUtil.wasDisplayed(pageContext, attributed, theMA) || theExclude.contains(theMA.getName()))) {
                    String theStyle = null;
                    if (theComp instanceof AttributedSearchComponent) {
                        theStyle = ((AttributedSearchComponent)this.getComponent()).getStyleInformation(theMA, theUpdate.getDomain());
                    } 

					LabelPosition labelPosition = AttributeOperations.labelPosition(theMA, theUpdate);

					{
						_cell.setParent(containerTag);
						_cell.setWholeLine(AttributeOperations.renderWholeLine(theMA, theUpdate));
						_cell.setLabelPosition(labelPosition);
						if (_firstColumnWidth != null) {
							_cell.setFirstColumnWidth(_firstColumnWidth);
						}
						_cell.setSplitControls(_splitControls);
						setupTag(_cell);

						_capture = _cell;

						_description.setParent(_cell);
						setupTag(_description);

						_capture = _description;
		
						theLabel.setParent(_description);
						theLabel.setAttributeUpdate(theUpdate);
						theLabel.setColon(labelPosition == LabelPosition.DEFAULT);
						theLabel.setPageContext(pageContext);
						theLabel.doStartTag();
						theLabel.doEndTag();

						theError.setParent(_description);
						theError.setAttributeUpdate(theUpdate);
						theError.setPageContext(pageContext);
						theError.doStartTag();
						theError.doEndTag();
					}

					{
						_description.doAfterBody();
						pageContext.popBody();
						_description.doEndTag();
						_capture = _cell;
					}

					if (theUpdate.isSearchUpdate()) {
						String theName =
							SearchFilterSupport.getRelevantAndNegateMemberName(theMA, theUpdate.getDomain());

						this.writeSearchInput(theUpdate, theContext.hasMember(theName) ? theName : null, theStyle);
					} else {
						writeDefaultInput(theUpdate);
					}

					{
						_cell.doAfterBody();
						pageContext.popBody();
						_cell.doEndTag();
					}

					_capture = null;

                }
            }

			containerTag.doAfterBody();
			pageContext.popBody();
			containerTag.doEndTag();
        }
		return SKIP_BODY;
	}

    /**
     * @see com.top_logic.layout.form.tag.AbstractTag#endElement()
     */
    @Override
	protected int endElement() throws IOException, JspException {
        return TagSupport.EVAL_PAGE;
    }

    /**
	 * Find the instance of a given {@link MetaGroupTag} class type that is closest to this
	 * instance.
	 * 
	 * @see TagSupport#getParent()
	 *
	 * @return The closest {@link MetaGroupTag}.
	 */
    protected MetaGroupTag getMetaGroupTag() {
        return ((MetaGroupTag) TagSupport.findAncestorWithClass(this, MetaGroupTag.class));
    }

    /**
	 * Find the {@link AttributeFormContext} for MetaAttributes and their values.
	 *
	 * @return The {@link AttributeFormContext}.
	 */
    protected AttributeFormContext getFormContext() {
        FormTag theFormTag = (FormTag) TagSupport.findAncestorWithClass(this, FormTag.class);

        return ((AttributeFormContext) theFormTag.getFormContext());
    }

	/**
	 * Writes a {@link MetaInputTag} and a {@link MetaErrorTag} for the given
	 * {@link AttributeUpdate}
	 * 
	 * @param update
	 *        the update containing information about the representing {@link TLStructuredTypePart}
	 *        and the {@link Wrapper value holder}
	 * @throws JspException
	 *         if some tag throws some
	 */
	protected void writeDefaultInput(AttributeUpdate update) throws JspException {
		MetaInputTag inputTag = new MetaInputTag();
		inputTag.setAttributeUpdate(update);
		inputTag.setParent(this);
		inputTag.setPageContext(this.pageContext);
		inputTag.doStartTag();
		inputTag.doEndTag();
    }

    /**
	 * Write a search input representation for the given parameters.
	 *
	 * @param anUpdate
	 *        The attribute update containing the data.
	 * @param aString
	 *        The name of the the search check box (positive, negative, neutral).
	 * @param aStyle
	 *        The displaying style of the field to be written.
	 */
    protected void writeSearchInput(AttributeUpdate anUpdate, String aString, String aStyle) throws JspException, IOException {
        TagWriter out = out();
		GroupedMetaInputTag.writeSearchInput(this, this.pageContext, out, anUpdate, aString, aStyle);
    }

	/**
	 * Look at {@link #showFieldSet} for the comment.
	 */
	public void setShowFieldSet(boolean aShowFieldSet) {
		this.showFieldSet = aShowFieldSet;
	}

	/**
	 * @see com.top_logic.layout.form.tag.AbstractTag#teardown()
	 */
	@Override
	protected void teardown() {
		super.teardown();

		this.showFieldSet = true;
	}

	private void setupTag(BodyTagSupport tag) throws JspException {
		tag.setPageContext(pageContext);
		tag.doStartTag();
		tag.setBodyContent(pageContext.pushBody());
		tag.doInitBody();
	}
}

