
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.taglibs;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * I18N implementation, translates lables according to language of current user.
 *
 * This class converts given keys to an I18N version and returns them.
 * Depending on the used locale of the user, this class returns a value.
 *
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class LabelTag extends TagSupport {

    /** The key for the name to be generated via I18N. */
	private ResKey _key;

    /**
     * Generates the tag for displaying a KnowledgeObject in the browser.
     *
     * @return    SKIP_BODY
     */
    @Override
	public int doStartTag () throws JspException {
        try {
            JspWriter out = this.pageContext.getOut ();
            Resources res = Resources.getInstance ();
			out.write(res.getString(_key));
        }
        catch (Exception ex) {
            throw new JspException (ex);
        }

        return (SKIP_BODY);
    }

    /**
     * Generates the end tag for displaying a KnowledgeObject in the browser.
     *
     * @return    EVAL_PAGE
     */
    @Override
	public int doEndTag () {
        return (EVAL_PAGE);
    }

    /**
	 * Set resource key to display.
	 * 
	 * @param key
	 *        The key of the label.
	 */
	@CalledFromJSP
	public void setName(String key) {
		setNameConst(ResKey.internalJsp(key));
    }

	/**
	 * Set resource key to display.
	 * 
	 * @param key
	 *        The key of the label.
	 */
	@CalledFromJSP
	public void setNameConst(ResKey key) {
		_key = key;
	}
}
