/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart;

import java.awt.Dimension;
import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.form.tag.AbstractTag;
import com.top_logic.layout.form.tag.ControlTagUtil;

/**
 * A simple tag to glue ImageComponent and JSP-Page togehter.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class LayoutImgTag extends AbstractTag {

    /** Constant for the image id */
    public static final String  IMAGE_ID  = "imgId";
    /** Constant for the image map */
    public static final String  IMAGE_MAP = "imageMap";

    /** Id of the image to write, may be null */
    protected String            imgId;
    /** width of the Image */
	protected int width = -1;
    /** height of the Image */
	protected int height = -1;
    
	private Boolean _noImageMap = null;

	private Boolean _ignoreHorizontalScroll = null;

	private Boolean _ignoreVerticalScroll = null;

	private Boolean _waitingSlider = null;

    /**
     * Overriden to access the surrounding FormComponent.
     */
    @Override
	public void setParent(Tag myParent) {
        super.setParent(myParent);

		if (!(getComponent() instanceof ImageComponent)) {
			throw new IllegalArgumentException("Need a ImageComponent. Got: " + getComponent());
		}

    }
    
    /**
     * Write out an Image tag refering to the LayoutImgServlet.
     */
    @Override
	protected int startElement() throws JspException, IOException {
		try {
			ControlComponent controlComponent = (ControlComponent) this.getComponent();
            
            /* Check if the form component already has a control object. */
            String theControlKey = getControlName(this.imgId);
			ImageControl theControl = (ImageControl)controlComponent.getControl(theControlKey);
			Dimension dimension = (width < 0 || height < 0) ? null : new Dimension(this.width, this.height);
            if (theControl == null) {
				theControl = createControl(dimension);
                controlComponent.addControl(theControlKey, theControl);
            } else {
				theControl.setDimension(dimension);
           }
            
			ControlTagUtil.writeControl(this, pageContext, theControl);

        }
        catch (Exception e) {
            Logger.error("Could not write image ('" + this.imgId+ "').", e, this);
        }
        
        return SKIP_BODY;
    }

	private ImageControl createControl(Dimension dimension) {
		ImageComponent imgComponent = (ImageComponent) getComponent();
		ImageControl control = new ImageControl(imgComponent, dimension, this.imgId, null);
		if (_noImageMap != null) {
			control.setUseImageMap(!_noImageMap.booleanValue());
		}
		if (_ignoreHorizontalScroll != null) {
			control.setRespectHorizontalScrollbar(!_ignoreHorizontalScroll.booleanValue());
		}
		if (_ignoreVerticalScroll != null) {
			control.setRespectVerticalScrollbar(!_ignoreVerticalScroll.booleanValue());
		}
		if (_waitingSlider != null) {
			control.setUseWaitingSlider(_waitingSlider.booleanValue());
		}
		return control;
	}

    private String getControlName(String anImageId) {
		String controlName = getComponent().getName().qualifiedName();
        
        if (!StringServices.isEmpty(anImageId)) {
            controlName += anImageId;
        }
        
        return controlName;
    }

    /**
	 * @see com.top_logic.layout.form.tag.AbstractTag#endElement()
	 */
	@Override
	protected int endElement() throws IOException, JspException {
		return EVAL_PAGE;
	}

	/**
	 * @param anId
	 *        the ID of the Image to write.
	 */
    public void setImgId(String anId) {
        this.imgId = anId;
    }
    
    /**
     * @param aWidth width of the image to write.
     */
    public void setWidth(int aWidth) {
        this.width = aWidth;
    }

    /**
     * @param aHeight height of the image to write.
     */
    public void setHeight(int aHeight) {
        this.height = aHeight;
    }

	/**
	 * @param noImageMap
	 *        Whether the image map must <b>not</b> be written.
	 */
	public void setNoImageMap(boolean noImageMap) {
		_noImageMap = Boolean.valueOf(noImageMap);
	}

	/**
	 * Sets value to set inverted to {@link ImageControl#setRespectHorizontalScrollbar(boolean)}.
	 */
	public void setIgnoreHorizontalScrollbar(boolean ignoreHorScroll) {
		_ignoreHorizontalScroll = ignoreHorScroll;
	}

	/**
	 * Sets value to set inverted to {@link ImageControl#setRespectVerticalScrollbar(boolean)}.
	 */
	public void setIgnoreVerticalScrollbar(boolean ignoreVertScroll) {
		_ignoreVerticalScroll = ignoreVertScroll;
	}

	/**
	 * Sets value to inverted to {@link ImageControl#setUseWaitingSlider(boolean)}.
	 */
	public void setUseWaitingSlider(boolean waitingSlider) {
		_waitingSlider = waitingSlider;
	}

	@Override
	protected void teardown() {
		_noImageMap = null;
		imgId = null;
		width = -1;
		height = -1;
		_ignoreHorizontalScroll = null;
		_ignoreVerticalScroll = null;
		_waitingSlider = null;
		super.teardown();
	}
}
