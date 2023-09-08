
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.taglibs;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import com.top_logic.dob.DataObject;
import com.top_logic.util.Resources;

/**
 * TagLib generating the matching input field for the requested value.
 *
 * This tag lib checks, whether the user is allowed to change the given
 * knowledge object or not. If he is, it creates an input field for the
 * kind of defined attribute. If not, the attribute will be displayed
 * in the best manner.
 *
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class InputTag extends TagSupport {

    /** The object to be changed. */
    private DataObject object;

    /** Properties of the object. */
    private DataObject properties;

    /** The name of the attribute. */
    private String name;

    /** The size of the input field. */
    private int size;

    /**
     * Creates an instance of this class.
     */
    public InputTag () {
    }

    /**
     * Generates the tag for displaying a DataObject in the browser.
     *
     * @return    SKIP_BODY
     */
    @Override
	public int doStartTag () throws JspException {
        try {
            this.pageContext.getOut ().
                             print (this.getInputLabel ());
        }
        catch (Exception ex) {
            ex.printStackTrace ();

            throw new JspTagException ("InputTag: " + ex.getMessage ());
        }

        return (SKIP_BODY);
    }

    /**
     * Generates the end tag for displaying a DataObject in the browser.
     *
     * @return    EVAL_PAGE
     */
    @Override
	public int doEndTag () {
        return (EVAL_PAGE);
    }

    /**
     * Returns the object to be used.
     *
     * @return    The object to be used.
     */
    public DataObject getObject () {
        return (this.object);
    }

    /**
     * Set the object to be managed.
     *
     * @param    anObject    The object to be managed.
     */
    public void setObject (DataObject anObject) {
        this.object = anObject;
    }

    /**
     * Returns the object to be used.
     *
     * @return    The object to be used.
     */
    public DataObject getProperties () {
        return (this.properties);
    }

    /**
     * Set the object to be managed.
     *
     * @param    anObject    The object to be managed.
     */
    public void setProperties (DataObject anObject) {
        this.properties = anObject;
    }

    /**
     * Returns the size of this input field.
     *
     * @return    The size of the input field (may be 0, which will be ignored).
     */
    public int getSize () {
        return (this.size);
    }

    /**
     * Set the size of this input field.
     *
     * @param    aSize    The size of the input field (may be 0, which will be ignored)
     */
    public void setSize (int aSize) {
        this.size = aSize;
    }

    /**
     * Returns the name of the label.
     *
     * @return    The name of the label.
     */
    public String getName () {
        return (this.name);
    }

    /**
     * Set the name for the label.
     *
     * @param    aName    The name of the label.
     */
    public void setName (String aName) {
        this.name = aName;
    }

    /**
     * Return the input label for the defined object.
     *
     * Depending on the accessing rights this method returns just the value
     * or an input field for the value.
     *
     * @return    The requested HTML string.
     */
    protected Object getInputLabel () {
        String          theResult = "";
        DataObject theObject = this.getObject ();
        String          theName   = this.getName ();
        Object          theValue  = this.getValue (theObject, theName);

        if (theValue instanceof Boolean) {
            theResult = this.getBooleanInput (theName, 
                                              true, 
                                              (Boolean) theValue) +
                        "&nbsp;&nbsp;" +
                        this.getBooleanInput (theName, 
                                              false, 
                                              (Boolean) theValue);
        }
        else {
            int          theSize   = this.getSize ();
            StringBuffer theBuffer = new StringBuffer (128);

            theBuffer.append ("<input type=\"text\" name=\"").
                      append (theName).
                      append ("\" value=\"").
                      append (theValue);

            if (theSize > 0) {
                theBuffer.append ("\" size=\"").
                          append (theSize);
            }

            theResult = theBuffer.append ("\" />").toString ();
        }

        return (theResult);
    }

    /**
     * Returns the input field for a boolean value.
     *
     * The input field for the boolean value consists of two radio boxes,
     * where one is selected.
     *
     * @param    aName     The name of the attribute.
     * @param    aType     The type of box to be created.
     * @param    aValue    The value of the attribute.
     * @return   The string representation for the value.
     */
    protected String getBooleanInput (String aName, boolean aType, 
                                      Boolean aValue) {
        StringBuffer theResult = new StringBuffer (128);

        theResult.append ("<input type=\"radio\" name=\"").
                  append (aName).
                  append ("\" value=\"").
                  append (this.getString (aType)).
                  append ("\" ");

        if (aValue.booleanValue () == aType) {
            theResult.append ("checked=\"checked\" ");
        }

        theResult.append ("/>");

        return (theResult.toString ());
    }

    /**
     * Returns the matching string for the given boolean.
     *
     * @param    aValue    The value.
     * @return   The string representation for the value.
     */
    protected String getString (boolean aValue) {
		return (Resources.getInstance().getString(aValue ? I18NConstants.TRUE_LABEL : I18NConstants.FALSE_LABEL));
    }

    /**
     * Return the matching input type for the given attribute.
     *
     * @param    anObject    The requested value.
     * @return   The name of the input type for the given object.
     */
    protected String getInputType (Object anObject) {
        return ("text");
    }

    /**
     * Read the given attribute value from the given object.
     *
     * If the value is null, an empty string will be returned, otherwise
     * the value. If there is no such attribute, the error report will
     * be returned.
     *
     * @param    anObject    The object containing the attribute.
     * @param    anAttr      The name of the attribute to be read.
     * @return   The requested attribute value.
     */
    protected Object getValue (DataObject anObject, String anAttr) {
        Object theResult = null;

        try {
            theResult = anObject.getAttributeValue (anAttr);

            if (theResult == null) {
                theResult = "";
            }
        }
        catch (Exception ex) {
            DataObject theProps = this.getProperties ();

            if (theProps != null) {
                try {
                    theResult = theProps.getAttributeValue (anAttr);
    
                    if (theResult == null) {
                        theResult = "";
                    }
                }
                catch (Exception ex1) {
                    theResult = "com.top-logic.knowledge.taglibs.InputTag: Error reading '" + name + 
                                "', reason is: " + ex1;
                }
            }
            else {
                theResult = "com.top-logic.knowledge.taglibs.InputTag: Error reading attribute '" + name + "', reason: " + ex;
            }
        }

        return (theResult);        
    }

}
