/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.awt.Dimension;

import com.top_logic.base.chart.ImageComponent;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.util.Utils;

/**
 * {@link FormMember} for displaying a dynamic image.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ImageField extends AbstractFormMember implements ImageModel{

	private ImageComponent imageComponent;
	private Dimension dimension;

    /**
	 * Creates an {@link ImageField}.
	 */
    public ImageField(String someName) {
        super(someName);
    }
    
    public ImageField(String someName, ImageComponent aComp, Dimension aDimension) {
    	super(someName);
    	this.imageComponent = aComp;
    	this.dimension = aDimension;
    }

    @Override
	public Object visit(FormMemberVisitor aVisitor, Object anObject) {
        return aVisitor.visitImageField(this, anObject);
    }

	@Override
	public ImageComponent getImageComponent() {
	    return this.imageComponent;
    }

	@Override
	public Dimension getDimension() {
		return this.dimension;
    }

	@Override
	public void setImageComponent(ImageComponent newValue) {
		ImageComponent oldValue = this.imageComponent;
		if (! Utils.equals(newValue, oldValue)) {
			this.imageComponent = newValue;
			firePropertyChanged(IMAGE_PROPERTY, self(), oldValue, newValue);
		}
	}
	
	@Override
	public void setDimension(Dimension newValue) {
		Dimension oldValue = this.dimension;
		if (! Utils.equals(newValue, oldValue)) {
			this.dimension = newValue;
			firePropertyChanged(DIMENSION_PROPERTY, self(), oldValue, newValue);
		}
    }

	@Override
	public void clearConstraints() {
    }

	@Override
	public void reset() {
    }
	
	@Override
	public boolean isChanged() {
		return false;
	}

	@Override
	public void setMandatory(boolean isMandatory) {
    }

	@Override
	public boolean focus() {
		// Ignore, this cannot have focus.
		return false;
	}

	@Override
	protected ImageField self() {
		return this;
	}

}

