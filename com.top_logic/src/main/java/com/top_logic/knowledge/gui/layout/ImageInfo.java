/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.Adorner;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * Describes a complete img-tag to be used as lightwight component.
 *
 * @author  <a href="mailto:kha@top-logic.com">kha</a>
 */
public class ImageInfo extends Adorner  {

	public interface Config extends PolymorphicConfiguration<Adorner> {
		@Name("path")
		@Mandatory
		ThemeImage getPath();

		@Name("alt")
		@Mandatory
		@InstanceFormat
		ResKey getAlt();

		@Name("href")
		String getHref();

		@Name("target")
		String getTarget();
	}

	/** use this for call to CTor when overgiven href is absolute */ 
	public static final boolean ABSOLUTE_HREF = true;
	
	/** signals overgiven href is absolute */
	protected boolean isAbsoluteHref = false;
	
    /** Path to the Image */
	protected ThemeImage path;

    /** Key of Alttext to show for the image */
	protected ResKey alt;

    /** optional reference/anchor to write around the Image. */
    protected String href;
 
    /** optional target for reference */
    protected String target;

    /** Translated Alttext */
    protected transient String iAlt;
   
    // Nice to have: mouseOver, overtag ...

     /** return some reasonable debug info */
     @Override
	public String toString() {
         return "ImageInfo[" 
             + ':' + path 
             + ':' + alt 
             + ':' + href 
             + ':' + target + "]";
     }

    /**
     * Make clone() accessible.
     */
    @Override
	public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
 
    /**
     * Allow Construction via XML-Attributes.
     */
    public ImageInfo(InstantiationContext context, Config atts) throws ConfigurationException {
        path   = atts.getPath();
        alt    = atts.getAlt();
        href   = StringServices.nonEmpty(atts.getHref());
        target = StringServices.nonEmpty(atts.getTarget());
    }

    /** Translate alt Text on demand. */
    protected String translateAlt() {
        if (this.iAlt == null) {
            this.iAlt = (this.alt == null) ? "" : Resources.getInstance().getString(this.alt);
        }
        return this.iAlt;
    }
    
    /** Write out the image eventually surrounded by an anchor. */
    @Override
	public void write(
        ServletContext aContext,
        HttpServletRequest aRequest,  HttpServletResponse aResponse,
        TagWriter anOut,LayoutComponent aComponent)
        throws IOException, ServletException  {
        
        String contextPath = aRequest.getContextPath();
        if (this.href != null) {
            
            String theRef    = this.href;
			String theTarget = (this.target == null) ? HTMLConstants.BLANK_VALUE : this.target;

			if (StringServices.startsWithChar(theRef, '/') && !this.isAbsoluteHref) {
                theRef = contextPath + theRef;
            }

            anOut.beginBeginTag("a");
            anOut.writeContent("href=\"" + theRef + "\" target=\"" + theTarget + '\"');
            anOut.endBeginTag();
            this.writeInner(contextPath, anOut);
            HTMLUtil.endA(anOut);
        }
        else {
            this.writeInner(contextPath, anOut);
        }
    }
	/**
	 * Write the inner content of the ImageInfo which is included (or not) in the anchor.
	 * In this case the inner content is an image.
	 */
    protected void writeInner(String aPath, TagWriter anOut) throws IOException {
        Theme  theTheme = ThemeFactory.getTheme();
		ThemeImage image = this.getPath();

		if (image == null) {
			return;
        }

		image.writeWithPlainTooltip(DefaultDisplayContext.getDisplayContext(), anOut, this.translateAlt());
    }

    /**
     * Getter for the image path. Overwite if you need a more dynamic handliung of the image path
     */
	protected ThemeImage getPath() {
        return this.path;
    }
}
