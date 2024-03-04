/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;


import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.EditContextProxy;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.tag.AbstractFormFieldControlTag;
import com.top_logic.layout.form.tag.CustomInputTag;
import com.top_logic.layout.form.tag.util.IntAttribute;
import com.top_logic.layout.form.tag.util.StringAttribute;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotationContainer;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.ui.BooleanDisplay;
import com.top_logic.model.annotate.ui.BooleanPresentation;
import com.top_logic.model.annotate.ui.ClassificationDisplay;
import com.top_logic.model.annotate.ui.ClassificationDisplay.ClassificationPresentation;
import com.top_logic.model.annotate.ui.InputSize;
import com.top_logic.model.annotate.ui.MultiLine;
import com.top_logic.model.annotate.ui.ReferenceDisplay;
import com.top_logic.model.annotate.ui.ReferencePresentation;
import com.top_logic.model.annotate.ui.TLDimensions;

/**
 * Creates and wraps a tag suitable for MetaAttributes.
 * 
 * @author    <a href="mailto:kbu@top-logic.com"></a>
 */
public class MetaInputTag extends AbstractMetaTag implements AnnotationLookup {

	/**
	 * Value for {@link #setInputSize(int)} that means that no explicit value has been set.
	 */
	public static final int NO_INPUT_SIZE = 0;

    /** onChange attribute */
	public final StringAttribute onchange = new StringAttribute();

    /** Tab index for the field. */
	public final IntAttribute tabindex = new IntAttribute();

	/** Style attribute for this field. */
	private String style;

	private ControlProvider _cp;

	private AnnotationContainer _localAnnotations = AnnotationContainer.EMPTY;

	/**
	 * Default Ctor
	 */
	public MetaInputTag() {
        initDefaultValues();
	}

	/**
	 * Sets {@link ControlProvider} to render the field.
	 */
	public void setControlProvider(ControlProvider cp) {
		_cp = cp;
	}

	/**
	 * Set width for select boxes (<code>style</code> attribute).
	 */
	@CalledFromJSP
	public void setSelectWidth(String width) {
		_localAnnotations = _localAnnotations.with(TLDimensions.width(DisplayDimension.parseDimension(width)));
	}
	
	/**
	 * Set height for select boxes (<code>size</code> attribute of select elements).
	 */
	@CalledFromJSP
	public void setMultiselectHeight(int size) {
		_localAnnotations = _localAnnotations.with(InputSize.size(size));
	}
	
	/**
	 * Set the number of rows for text areas.
	 * 
	 * @param aRows the number of rows
	 */
	public void setTextareaRows (int aRows) {
		_localAnnotations = _localAnnotations.with(MultiLine.rows(aRows));
	}
	
	/**
	 * Equivalent to {@link #setInputSize(int)}.
	 * 
	 * @deprecated Use {@link #setInputSize(int)}
	 */
	@Deprecated
	public void setTextareaCols (int aCols) {
		setInputSize(aCols);
	}

	/**
	 * Set the style attribute for this field.
	 * 
	 * @param style a String definition of the style
	 */
	public void setStyle(String style) {
    	this.style = style;
    }

	/**
	 * Equivalent to {@link #setInputSize(int)} with value 3.
	 * 
	 * @deprecated Use {@link #setInputSize(int)}
	 */
	@Deprecated
	public void setIsCurrency (boolean value) {
		setInputSize(value ? 3 : NO_INPUT_SIZE);
	}
	
	/**
	 * Set the onChange attribute.
	 * 
	 * @param anOnChange the onChange attribute
	 */
	public void setOnChange (String anOnChange) {
		this.onchange.set(anOnChange);
	}
    
    /**
	 * Set the Size attribute.
	 * 
	 * @param size
	 *        the Size attribute
	 */
	public void setInputSize(int size) {
		if (size == NO_INPUT_SIZE) {
			_localAnnotations = _localAnnotations.without(InputSize.class);
		} else {
			_localAnnotations = _localAnnotations.with(InputSize.size(size));
		}
    }
    
    /**
     * Set the tab index attribute.
     * 
     * @param aSize the tab index attribute
     */
    public void setTabindex(String aSize) {
        this.tabindex.set(aSize);
    }
    
	/**
	 * Set if text areas should be preferred
	 * (otherwise simple inputs are used).
	 * 
	 * @param aPreferTextArea if true text areas will be preferred
	 */
	public void setPreferTextArea(boolean aPreferTextArea) {
		MultiLine annotation = _localAnnotations.getAnnotation(MultiLine.class);
		if (annotation == null) {
			setTextareaRows(MultiLineText.DEFAULT_ROWS);
		}
	}

	/**
	 * Set height for image galleries.
	 */
	public void setGalleryHeight(String height) {
		DisplayDimension value = DisplayDimension.parseDimension(height);

		TLDimensions annotation = _localAnnotations.getAnnotation(TLDimensions.class);
		if (annotation != null) {
			annotation.setHeight(value);
		} else {
			_localAnnotations = _localAnnotations.with(TLDimensions.height(value));
		}
	}

	/**
	 * Set width for image galleries.
	 */
	public void setGalleryWidth(String width) {
		DisplayDimension value = DisplayDimension.parseDimension(width);

		TLDimensions annotation = _localAnnotations.getAnnotation(TLDimensions.class);
		if (annotation != null) {
			annotation.setWidth(value);
		} else {
			_localAnnotations = _localAnnotations.with(TLDimensions.width(value));
		}
	}

    @Override
	protected void teardown() {
        super.teardown();
        initDefaultValues();
	}
    
	private void initDefaultValues() {
		// This tag
		_localAnnotations = AnnotationContainer.EMPTY;
		this.onchange.reset();
		this.tabindex.reset();
		this.style				= null;
	}

    @Override
	protected Tag createImplementation() {
		AbstractFormFieldControlTag tag = createInputTag();
		initInputTag(tag);
    	return tag;
    }

    /**
	 * Get or create the wrapped tag.
	 * 
	 * @return the wrapped tag
	 */
	protected AbstractFormFieldControlTag createInputTag() {
		ControlProvider controlProvider;
		if (_cp != null) {
			controlProvider = _cp;
		} else {
			TLStructuredTypePart attribute = this.getMetaAttribute();
			TagProviderAnnotation annotation = attribute.getAnnotation(TagProviderAnnotation.class);
			if (annotation != null) {
				DisplayProvider displayProvider = TypedConfigUtil.createInstance(annotation.getImpl());
				controlProvider = new AbstractFormFieldControlProvider() {
					@Override
					protected Control createInput(FormMember member) {
						return displayProvider.createDisplay(editContext(), member);
					}
				};
			} else {
				throw new IllegalArgumentException("No tag configured for " + attribute);
			}
		}

		CustomInputTag customInputTag = new CustomInputTag();
		customInputTag.setControlProvider(controlProvider);
		return customInputTag;
	}

	private EditContext editContext() {
		return new EditContextProxy(getAttributeUpdate()) {
			@Override
			public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
				T annotation = getLocalAnnotation(annotationType);
				if (annotation != null) {
					return annotation;
				}
				return super.getAnnotation(annotationType);
			}
		};
	}

	/**
	 * Note: Does only deliver locally defined annotations, not those given at
	 * {@link #getAttributeUpdate()}, because this lookup is only used to copy annotations from one
	 * tag to another. See {@link #editContext()} for full annotation lookup.
	 */
	@Override
	public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
		return getLocalAnnotation(annotationType);
	}

	/**
	 * The local {@link TLAnnotation} override for the currently rendered attribute.
	 */
	public <T extends TLAnnotation> T getLocalAnnotation(Class<T> annotationType) {
		return _localAnnotations.getAnnotation(annotationType);
	}

	/**
	 * Adds a local annotation override for the currently rendered attribute.
	 */
	public void setLocalAnnotation(TLAnnotation annotation) {
		_localAnnotations = _localAnnotations.with(annotation);
	}

	/**
	 * Hook to initialize a created tag for rendering.
	 */
	protected void initInputTag(AbstractFormFieldControlTag tag) {
		if (tag != null) {
			tag.setName(getFieldName());
			tag.onchange.set(onchange);
			tag.tabindex.set(tabindex);
			tag.style.set(style);
		}
	}

	/**
	 * @deprecated Use {@link #setReferenceDisplay(ReferencePresentation)},
	 *             {@link #setClassificationDisplay(ClassificationPresentation)}, or
	 *             {@link #setBooleanDisplay(BooleanPresentation)}.
	 */
	@Deprecated
	public void setPreferRadio(boolean aPreferRadio) {
		setReferenceDisplay(aPreferRadio ? ReferencePresentation.RADIO : ReferencePresentation.POP_UP);
		setBooleanDisplay(aPreferRadio ? BooleanPresentation.RADIO : BooleanPresentation.CHECKBOX);
		setClassificationDisplay(
			aPreferRadio ? ClassificationPresentation.RADIO : ClassificationPresentation.DROP_DOWN);
	}

	/**
	 * Sets the presentation of reference attributes.
	 */
	public void setReferenceDisplay(ReferencePresentation presentation) {
		_localAnnotations = _localAnnotations.with(ReferenceDisplay.display(presentation));
	}
	
	/**
	 * Sets the presentation of classification attributes (of enum type).
	 */
	public void setClassificationDisplay(ClassificationPresentation presentation) {
		_localAnnotations = _localAnnotations.with(ClassificationDisplay.display(presentation));
	}

	/**
	 * The presentation of a boolean attribute.
	 */
	public void setBooleanDisplay(BooleanPresentation value) {
		_localAnnotations = _localAnnotations.with(BooleanDisplay.display(value));
	}

	/**
	 * @deprecated Use {@link #setReferenceDisplay(ReferencePresentation)},
	 *             {@link #setClassificationDisplay(ClassificationPresentation)}, or
	 *             {@link #setBooleanDisplay(BooleanPresentation)}.
	 */
	public void setRadioHorizontal(boolean value) {
		setReferenceDisplay(value ? ReferencePresentation.RADIO_INLINE : ReferencePresentation.POP_UP);
		setClassificationDisplay(
			value ? ClassificationPresentation.RADIO_INLINE : ClassificationPresentation.DROP_DOWN);
	}

}
