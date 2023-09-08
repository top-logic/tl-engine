/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.iconBar;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.Adorner;

/**
 * @author    <a href=mailto:tsa@top-logic.com>Theo Sattler</a>
 */
public class JavaScriptAdorner extends Adorner {
    
	public interface Config extends PolymorphicConfiguration<Adorner> {
		@Name("text")
		@Mandatory
		String getText();

		@Name("onclick")
		String getOnclick();

		@Name("onmousedown")
		String getOnmousedown();

		@Name("onmouseover")
		String getOnmouseover();
	}

	private String text;
    private String onclick;
    private String onmousedown;
    private String onmouseover;
    
	public JavaScriptAdorner(InstantiationContext context, Config atts) throws ConfigurationException {
        super();
		this.text = atts.getText();
		this.onclick = StringServices.nonEmpty(atts.getOnclick());
		this.onmousedown = StringServices.nonEmpty(atts.getOnmousedown());
		this.onmouseover = StringServices.nonEmpty(atts.getOnmouseover());
    }

    @Override
	public void write(DisplayContext aContext, TagWriter anOut) throws IOException {
        anOut.beginBeginTag(HTMLConstants.ANCHOR);
        anOut.writeAttribute(HTMLConstants.ONCLICK_ATTR    , this.onclick);
        anOut.writeAttribute(HTMLConstants.ONMOUSEDOWN_ATTR, this.onmousedown);
        anOut.writeAttribute(HTMLConstants.ONMOUSEOVER_ATTR, this.onmouseover);
        anOut.writeAttribute(HTMLConstants.STYLE_ATTR,     "vertical-align:top"); // TODO TSA use css-class ?
        anOut.endBeginTag();
        anOut.writeContent(this.text);
        anOut.endTag(HTMLConstants.ANCHOR);
        

    }

}
