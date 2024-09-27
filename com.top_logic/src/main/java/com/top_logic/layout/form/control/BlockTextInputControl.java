/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.ExpandableStringField;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.util.Resources;

/**
 * The BlockTextInputControl renders text in block format.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class BlockTextInputControl extends ExpandableTextInputControl {

    private int blockColsExpanded = -1;
    private int blockRowsExpanded = -1;
    
    private int blockColsCollapsed = -1;
    private int blockRowsCollapsed = -1;
    
	private static final ResKey MORE = I18NConstants.EXPAND_TEXT;

	private static final ResKey LESS = I18NConstants.COLLAPSE_TEXT;

    /**
	 * Creates a {@link BlockTextInputControl}.
	 */
    public BlockTextInputControl(FormField aModel) {
        this(aModel, 60, 5);
    }

	/**
	 * Creates a {@link BlockTextInputControl}.
	 * 
	 * @param aModel
	 *        the FormField this {@link TextInputControl} renders.
	 */
    public BlockTextInputControl(FormField aModel, int colsCollaped, int rowsCollaped) {
        super(aModel, -1);
        
        this.setBlockColsCollapsed(colsCollaped);
        this.setBlockColsExpanded(colsCollaped);
        this.setBlockRowsCollapsed(rowsCollaped);
        
        this.setMultiLine(!((ExpandableStringField) aModel).isCollapsed());
    }

    
    public void setBlockColsCollapsed(int aBlockColsCollapsed) {
        this.blockColsCollapsed = aBlockColsCollapsed;
    }
    
    public void setBlockColsExpanded(int aBlockColsExpanded) {
        this.blockColsExpanded = aBlockColsExpanded;
    }
    
    public void setBlockRowsCollapsed(int aBlockRowsCollapsed) {
        this.blockRowsCollapsed = aBlockRowsCollapsed;
    }
    
    public void setBlockRowsExpanded(int aBlockRowsExpanded) {
        this.blockRowsExpanded = aBlockRowsExpanded;
    }
    
	@Override
	protected String getTypeCssClass() {
		return "cBlockTextInput";
	}

    @Override
	protected void writeImmutable(DisplayContext context, TagWriter out)
            throws IOException {
        ExpandableStringField theField   = (ExpandableStringField) getModel();
        String                theValue   = theField.getRawString();

		out.beginBeginTag(DIV);
        // Functional attributes
		writeControlAttributes(context, out);
		out.writeAttribute(STYLE_ATTR, getInputStyle());
		out.writeAttribute(SIZE_ATTR, this.getColumns());
        
		ResKey tooltip = theField.getTooltip();
		if (tooltip != null) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
				ResKey.text(TagUtil.encodeXML(theValue)),
				ResKey.text(TagUtil.encodeXML(context.getResources().getString(theField.getTooltipCaption()))));
        }
        
        StringBuffer theFormatted = new StringBuffer();
        boolean shortened = this.formatString(theFormatted, theValue);
        
		out.endBeginTag();
        {
			out.writeContent(theFormatted.toString());
            boolean expanded = this.isMultiLine();
            
            // write toggle link only if its needed
            if (expanded || shortened) {
				this.writeToggleLink(context, out, expanded);
            }
        }
		out.endTag(DIV);
    }
    
    private void writeToggleLink(DisplayContext aContext, TagWriter anOut, boolean isExpanded) throws IOException {
        
        String         theId      = this.getOpenButtonID();
        ControlCommand theCommand = this.getToggleFieldModeCommand();
        
        if (isExpanded) {
            anOut.emptyTag(BR);
        }
        
        anOut.beginBeginTag(ANCHOR);
        anOut.writeAttribute(ID_ATTR, theId);
        anOut.writeAttribute(HREF_ATTR, "#");
        
        writeOnClick(anOut, theCommand);
        anOut.endBeginTag();
        
        Resources theRes = Resources.getInstance();
        if (this.isMultiLine()) {
			anOut.writeContent(theRes.getString(LESS));
        }
        else {
			anOut.writeContent(theRes.getString(MORE));
        }
        anOut.endTag(ANCHOR);
    }

	private void writeOnClick(TagWriter out, ControlCommand theCommand) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("return ");
		theCommand.writeInvokeExpression(out, this);
		out.append(';');
		out.endAttribute();
	}
    
    private boolean formatString(StringBuffer aResult, String aValue) {
        
        if (StringServices.isEmpty(aValue)) {
            return false;
        }
        
        boolean expanded = this.isMultiLine();
        int theMax = aValue.length();
        
        if (expanded) {
            if (this.blockColsExpanded > 0) {
                if (this.blockRowsExpanded > 0) {
                    theMax = this.blockColsExpanded * this.blockRowsExpanded;
                }
           }
        }
        else {
            if (this.blockColsCollapsed > 0) {
                 if (this.blockRowsCollapsed > 0) {
                     theMax = this.blockColsCollapsed * this.blockRowsCollapsed;
                 }
            }
        }

        if (this.getMaxLengthShown() > 0) {
            theMax = Math.min(theMax, this.getMaxLengthShown());
        }
        
        Resources    theRes    = Resources.getInstance(); 

        if (expanded) {
			return blockString(aResult, aValue, this.blockColsExpanded, this.blockRowsExpanded, "<br/>", theRes
				.getString(LESS).length());
        }
        else {
            if (aValue.length() < this.blockColsCollapsed * this.blockRowsCollapsed) {
                return blockString(aResult, aValue, this.blockColsCollapsed, this.blockRowsCollapsed, "<br/>", 0);
            }
            else {
				return blockString(aResult, aValue, this.blockColsCollapsed, this.blockRowsCollapsed, "<br/>", theRes
					.getString(MORE).length());
            }
        }
    }
    
    /**
     * Formats a string into an encoded HTML text block.
     * 
     * @param aResult   the result of the applied formatting
     * @param aString   the string the format
     * @param blockSize number of characters per line
     * @param maxRows   maximum number of rows shown in the block.
     *                  if maxRows < 0, all text will be shown,
     *                  otherwise the text is shorten and "..." will be 
     *                  appended at the end.
     * @param aLineSeparator a separator to split the lines
     * @param moreSpace if the text must be shorten, a space of this length will 
     *                  be left after "...".
     *                  
     * @return true, if the text was shortened.                 
     */
    public static boolean blockString(StringBuffer aResult, String aString, int blockSize, int maxRows, String aLineSeparator, int moreSpace) {
        
        // block only if you should and don't care about empty things
        if (blockSize < 0 || StringServices.isEmpty(aString)) {
            aResult.append(aString);
            return false;
        }
       
        int theLength = aString.length();
        
        // don't care, if the string is short enough and do not contain any line break
        if (theLength < blockSize && aString.indexOf('\n') < 0) {
			TagUtil.writeText(aResult, aString);
            return false;
        }
        
        StringBuffer theBlock  = aResult;
        boolean      shortened = false;
        
        int lineStart   = 0;
        int lineEnd     = 0;
        int countBreaks = 0;
        for (int thePos=0; thePos < theLength && (maxRows < 0 || countBreaks < maxRows); countBreaks++) {

            lineStart  = thePos;
            lineEnd    = thePos + blockSize;
            lineEnd    = lineEnd < theLength ? lineEnd : theLength; 
            
            String theLine = aString.substring(lineStart, lineEnd);
            
            // always break at linebreaks 
            int theNextBreak = theLine.indexOf('\n');
            if (theNextBreak > -1) {
                
                theLine = theLine.substring(0, theNextBreak+1);
                thePos += theNextBreak+1;

                // break after maxRows have been inserted
                // append "..."+morespace in the current line iff the rest won't fit
                if (countBreaks+1 == maxRows && thePos < theLength) {
                    int needed   = blockSize-3-moreSpace;
                    
                    // strip the line, if theres not enough empty space
                    if (theLine.length() - needed > 0) {
						TagUtil.writeText(theBlock, theLine.substring(0, needed));
                    }
                    else {
						TagUtil.writeText(theBlock, theLine);
                    }
                    
                    theBlock.append("...");
                    shortened = true;
                    break;
                }
                else {
					TagUtil.writeText(theBlock, theLine);
                    theBlock.append(aLineSeparator);
                }
            }
            else {
                // find possible position to insert a linebreak
                theNextBreak = findLastIndexOfSeparator(theLine);

                // possible separator found, 
                // begin the next block line at that position
                if (theNextBreak > -1) {
                    theLine = theLine.substring(0, theNextBreak+1);
                    thePos += theNextBreak+1;
                }
                // no possible separator found
                // append the whole line
                else {
                    thePos = lineEnd;
                }
                
                // break after maxRows have been inserted
                // append "..."+morespace in the current line iff the rest won't fit
                if (countBreaks+1 == maxRows && thePos < theLength) {
                    int needed   = blockSize-3-moreSpace;
                    
                    // strip the line, if theres not enough empty space
                    if (theLine.length() - needed > 0) {
						TagUtil.writeText(theBlock, theLine.substring(0, needed));
                    }
                    else {
						TagUtil.writeText(theBlock, theLine);
                    }
                    
                    theBlock.append("...");
                    shortened = true;
                    break;
                }
                else {
					TagUtil.writeText(theBlock, theLine);
                    // append line separator except at the last line
                    if (theLength != lineEnd) {
                        theBlock.append(aLineSeparator);
                    }
                }
            }
        }
        
        return shortened;
    }
    
    private static int findLastIndexOfSeparator(String aString) {
        for (int thePos = aString.length()-1; thePos > 0; thePos--) {
            char theChar = aString.charAt(thePos);
            switch (theChar) {
              case ' ' : return thePos;
              case '\t': return thePos;
              default:
            }
        }
        
        return -1;
    }
}

