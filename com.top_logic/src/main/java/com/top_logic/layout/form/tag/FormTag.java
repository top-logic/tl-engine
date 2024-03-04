/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.util.Resources;

/**
 * {@link Tag} for rendering the view of a form.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormTag extends AbstractTag implements FormContainerTag, FormTagProperties {

	/**
	 * The {@link ResKey} suffix to the component's key for creating a custom "no model" text.
	 */
	public static final String DEFAULT_NO_MODEL_KEY_SUFFIX = "noModel";

	/**
	 * CSS class for the "no model" view.
	 */
	public static final String NO_MODEL_CSS_CLASS = "frmNoModel";

	private static final boolean SELECT_FIRST_DEFAULT = true;

	private static final Object NOT_INITIALIZED = new NamedConstant("not initialized");

	private Boolean _displayCondition;

	private Boolean _displayWithoutModel;

	private ResKey _noModelKey;

	private Boolean _ignoreModel;

	private Object _model = NOT_INITIALIZED;

    /** The FormContext held by this tag. */
    protected FormContext formContext;
    
    /** The Name of the generated form. */
    protected String name;

    /** The target of the generated form. */
    protected String target;
    
    /** The action of the generated form. */
    protected String action;

	private String _cssClass;

    /**
     * Whether to select the first input element upon display.
     */
	private boolean selectFirst = SELECT_FIRST_DEFAULT;

	private Boolean labelAbove;

	@Override
	@CalledFromJSP
	public void setDisplayWithoutModel(boolean value) {
		_displayWithoutModel = Boolean.valueOf(value);
	}

	/**
	 * @see #setDisplayWithoutModel(boolean)
	 */
	protected boolean displayWithoutModel() {
		if (_displayWithoutModel != null) {
			return _displayWithoutModel.booleanValue();
		}
		return defaultDisplayWithoutModel();
	}

	/**
	 * Default value for the property set in {@link #displayWithoutModel()} when
	 * {@link #setDisplayWithoutModel(boolean)} is not set.
	 */
	protected boolean defaultDisplayWithoutModel() {
		if (isCreateComponent()) {
			return true;
		}
		return getComponent().getConfig().getDisplayWithoutModel();
	}

	@Override
	@CalledFromJSP
	public void setIgnoreModel(boolean ignoreModel) {
		_ignoreModel = Boolean.valueOf(ignoreModel);
		if (_ignoreModel) {
			// Otherwise, the form would never be displayed.
			_displayWithoutModel = Boolean.TRUE;
		}
	}

	private boolean ignoreModel() {
		if (_ignoreModel != null) {
			return _ignoreModel.booleanValue();
		}
		return defaultIgnoreModel();
	}

	/**
	 * Default value for the property set in {@link #ignoreModel()} when
	 * {@link #setIgnoreModel(boolean)} is not set.
	 */
	protected boolean defaultIgnoreModel() {
		if (isCreateComponent()) {
			return true;
		}
		return false;
	}

	private boolean isCreateComponent() {
		return getComponent() instanceof AbstractCreateComponent || isInAssistant();
	}

	private boolean isInAssistant() {
		return AssistentComponent.getEnclosingAssistentComponent(getComponent()) != null;
	}

	private boolean openedInDialog() {
		LayoutComponent component = getComponent();
		return component.getDialogParent() != null;
	}

	private boolean noMasterComponent() {
		return !getComponent().hasMaster();
	}

	@Override
	@CalledFromJSP
	public void setNoModelKeySuffix(String resourceKeySuffix) {
		setNoModelKeyConst(toResourceKey(resourceKeySuffix));
	}

	@Override
	@CalledFromJSP
	public void setNoModelKey(String resourceKey) {
		setNoModelKeyConst(ResKey.internalJsp(resourceKey));
	}

	@Override
	@CalledFromJSP
	public void setNoModelKeyConst(ResKey key) {
		_noModelKey = key;
	}

	/**
	 * The model of the underlying form.
	 */
	protected Object getModel() {
		if (_model == NOT_INITIALIZED) {
			_model = lookupModel();
		}
		return _model;
	}

	/**
	 * Algorithm computing the value of {@link #getModel()}.
	 */
	protected Object lookupModel() {
		if (ignoreModel()) {
			// Prevent "absurd" component models being used in this form.
			return null;
		} else {
			Object componentModel = getComponent().getModel();
			if (!ComponentUtil.isValid(componentModel)) {
				return null;
			}
			return componentModel;
		}
	}

	@Override
	@CalledFromJSP
	public void setDisplayCondition(boolean value) {
		_displayCondition = Boolean.valueOf(value);
	}

	/**
	 * Whether the form should be displayed.
	 * 
	 * @see #displayWithoutModel()
	 * @see #startForm()
	 * @see #writeNoModel()
	 */
	protected boolean shouldDisplay() {
		// Note: Only access the form context, if either displayWithoutModel() is true, or the model
		// is not null.
		if (_displayCondition != null) {
			if (!_displayCondition.booleanValue()) {
				return false;
			} else {
				// Fall through to also check the existence of the form context.
			}
		} else {
			if (!displayWithoutModel()) {
				if (getModel() == null) {
					return false;
				}
			}
		}

		return getFormContext() != null;
	}
    
    /**
     * Acessor to the context.
     * 
     * @return the FormContext held by this tag 
     */
    public FormContext getFormContext() {
		if (formContext == null) {
			FormComponent component = getFormComponent();
			if (component != null) {
				formContext = getFormContext(component);
			}
		}
        return formContext;
    }
    
    public FormComponent getFormComponent() {
    	return (FormComponent) getComponent();
    }
    
	/**
	 * Return the form context from the given component.
	 * 
	 * @param aComponent
	 *        The component to get the form context from, must not be <code>null</code>.
	 * @return The requested form context, may be <code>null</code>.
	 */
	protected FormContext getFormContext(FormComponent aComponent) {
		return aComponent.getFormContext();
	}

    public void setSelectFirst(boolean selectFirst) {
		this.selectFirst = selectFirst;
	}
    
	/**
	 * The CSS class string.
	 */
	public String getCssClass() {
		return _cssClass;
	}

	/**
	 * @see #getCssClass()
	 */
	@CalledFromJSP
	public void setCssClass(String cssClass) {
		_cssClass = cssClass;
	}

	@Override
	protected void setup() throws JspException {
		super.setup();
		
		if (shouldDisplay()) {
			action = getComponent().getComponentURL(getDisplayContext()).getURL();
		}
	}

	@Override
	protected void teardown() {
		_model = NOT_INITIALIZED;
		_noModelKey = null;

		this.formContext = null;
		this.action = null;
		
		this.name = null;
		this.target = null;
		
		this.selectFirst = SELECT_FIRST_DEFAULT;
		
		super.teardown();
	}

    /**
     * @see jakarta.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
	public int startElement() throws JspException {
        try {
			if (shouldDisplay()) {
				startForm();
				return EVAL_BODY_INCLUDE;
			} else {
				writeNoModel();
				return SKIP_BODY;
            }
		} catch (IOException ex) {
			throw new JspException(ex);
		}
    }

    /**
     * Displays an alternative text, if the form should not be displayed.
     * 
     * @see #shouldDisplay()
     */
	protected void writeNoModel() throws IOException {
		beginBeginTag(DIV);
		writeAttribute(CLASS_ATTR, NO_MODEL_CSS_CLASS);
		endBeginTag();
		{
			beginBeginTag(TABLE);
			endBeginTag();
			{
				beginBeginTag(TR);
				endBeginTag();
				{
					beginBeginTag(TD);
					endBeginTag();
					{
						writeNoModelText();
					}
					endTag(TD);
				}
				endTag(TR);
			}
			endTag(TABLE);
		}
		endTag(DIV);
	}

	/**
	 * Writes the replacement text, if {@link #shouldDisplay()} is <code>false</code>.
	 */
	protected void writeNoModelText() throws IOException {
		writeText(Resources.getInstance().getString(noModelKey()));
	}

	private ResKey noModelKey() {
		if (_noModelKey != null) {
			// Explicitly specified, no further default.
			return _noModelKey;
		} else {
			return getComponent().noModelKey();
		}
	}

	/**
	 * Complete a resource key suffix to a resource key by prepending the {@link #getComponent()}'s
	 * resource prefix.
	 */
	protected final ResKey toResourceKey(String keySuffix) {
		return getComponent().getResPrefix().key(keySuffix);
	}

	/**
	 * Starts rendering the form, in case it should be displayed.
	 * 
	 * @see #shouldDisplay()
	 * @see #endForm()
	 */
	protected void startForm() throws IOException {
		FormContext theContext = getFormContext();
		writeFormStartTag(out(), null, theContext.getQualifiedName(), action, target, getCssClass());
		writeHiddenFields();
	}

	/**
	 * Writes a {@link HTMLConstants#FORM form} tag to the given
	 * {@link TagWriter} with the given parameters.
	 */
	public static void writeFormStartTag(TagWriter out, String id, String name, String action, String target) throws IOException {
    	writeFormStartTag(out, id, name, action, target, null);
    }

    /**
	 * Writes a {@link HTMLConstants#FORM form} tag to the given
	 * {@link TagWriter} with the given parameters.
	 */
	public static void writeFormStartTag(TagWriter out, String id, String name, String action, String target, String cssClass) throws IOException {
		out.beginBeginTag(FORM);
		out.writeAttribute(ID_ATTR, id);
		out.writeAttribute(NAME_ATTR, name);
		out.writeAttribute(CLASS_ATTR, cssClass);
		out.writeAttribute(METHOD_ATTR, POST_VALUE);
		out.writeAttribute(ENCTYPE_ATTR, MULTIPART_FORM_DATA_VALUE);
		out.writeAttribute(ACTION_ATTR, action);
		out.writeAttribute(TARGET_ATTR, target);
		
		// Prevent regular submission of the form.
		out.writeAttribute(ONSUBMIT_ATTR, "return false;");
		
		// Mark complex widget
		out.writeAttribute(TL_COMPLEX_WIDGET_ATTR, true);

		out.endBeginTag();
	}

	/** Write all the hidden fields we may need */
    protected void writeHiddenFields() throws IOException {
    	TagWriter out = out();
    	out.beginBeginTag(INPUT);
    	out.writeAttribute(TYPE_ATTR, HIDDEN_TYPE_VALUE);
    	out.writeAttribute(NAME_ATTR, LayoutConstants.PARAM_LAYOUT);
		out.writeAttribute(VALUE_ATTR, getFormComponent().getName().qualifiedName());
    	out.endEmptyTag();
    	
    	// For uploads using form.submit() E.G. UploadCommandScriptWriter
    	out.beginBeginTag(INPUT);
    	out.writeAttribute(TYPE_ATTR, HIDDEN_TYPE_VALUE);
    	out.writeAttribute(NAME_ATTR, LayoutComponent.COMMAND);
    	out.endEmptyTag();
    	
    	out.beginBeginTag(INPUT);
    	out.writeAttribute(TYPE_ATTR, HIDDEN_TYPE_VALUE);
    	out.writeAttribute(NAME_ATTR, LayoutComponent.PARAM_SEQUENCE_NUMBER);
    	out.endEmptyTag();
    	
    }

    /**
     * Overriden to close the tag and reset some members (in case of reuse).
     * 
     * @see jakarta.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
	public int endElement() throws JspException {
        try {
			if (shouldDisplay()) {
				endForm();
            }
        } catch (Exception ex) {
            throw new JspException(ex);
        } finally {
            reset();
        }
        return 0;
    }

    /**
     * Renders the end of the form.
     * 
     * @see #startForm()
     */
    protected void endForm() throws IOException {
		TagWriter out = out();
		FormContext theContext = this.formContext;
		if (selectFirst && theContext.isActive()) {
			out.beginScript();
			{
				out.writeContent("focusFirst();");
			}
			out.endScript();
		}
		out.endTag(FORM);
	}

	private void reset() {
        name = null;
		formContext = null;
	}

    /**
     * @param aName the Name of the generated form
     */
    public void setName(String aName) {
        name = aName;
    }
    
    /**
     * Get the name of the form
     * 
     * @return the name of the form
     */
    public String getName() {
        return name;
    }

    /**
     * the Target name of the window to open
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * @param aString value for the target parameter.
     */
    public void setTarget(String aString) {
        this.target = aString;
    }

    /**
     * the action to POS/GET the Formula to.
     */
    public String getAction() {
        return this.action;
    }

    /**
     * @param aString value for the target parameter.
     */
    public void setAction(String aString) {
        this.action = aString;
    }

	/**
	 * Sets the value for whether the label is rendered above the content.
	 * 
	 * @param labelAbove
	 *        If <code>true</code> label is rendered above, else it will be rendered before.
	 */
	public void setLabelAbove(Boolean labelAbove) {
		this.labelAbove = labelAbove;
	}

	/**
	 * Returns whether the label is rendered above the content.
	 * 
	 * @return If <code>true</code> label is rendered above, else it will be rendered before.
	 */
	public Boolean getLabelAbove() {
		return this.labelAbove;
	}

	@Override
	public FormMember getMember() {
		return getFormContext();
	}
    
	@Override
	public FormContainer getFormContainer() {
		return getFormContext();
	}

	@Override
	public FormTag getFormTag() {
		return this;
	}

}
