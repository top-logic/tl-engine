/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.component;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.knowledge.gui.layout.form.AbstractWrapperApplyCommandHandler;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;

/**
 * Extended handler for apply (and save) commands with {@link com.top_logic.knowledge.wrap.Wrapper}.
 * 
 * <p>
 * Additionally to the {@link AbstractWrapperApplyCommandHandler}, this class allows the automatic
 * storage of values from the {@link com.top_logic.knowledge.wrap.Wrapper} objects via
 * {@link #saveMetaAttributes(FormContext)}. This will only store the values defined in the
 * {@link com.top_logic.model.TLClass}.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractApplyAttributedCommandHandler extends AbstractApplyCommandHandler {

	public AbstractApplyAttributedCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * Store the changes done to the {@link com.top_logic.knowledge.wrap.Wrapper attributed} values.
     * 
     * @param    aContext    The context holding all needed information for update, may be <code>null</code>.
     * @return   Flag, if updating has taken place (will not happen, if given context is not instance of
     *           {@link AttributeFormContext}).
     */
    public boolean saveMetaAttributes(FormContext aContext) {
        if (aContext instanceof AttributeFormContext) {
            ((AttributeFormContext) aContext).store();
			return true;
        }
        return false;
    }

	/** 
     * Extract the value from the given form field.
     * 
     * @param    aField          The field to get the value from, must not be <code>null</code>.
     * @param    anAttributed    The attributed to be filled with data, must not be <code>null</code>.
     * @param    aName           The name of the attribute currently processed, must not be <code>null</code>.
     * @return   The requested value.
     */
    protected Object getValueFromField(TableField aTable, FormField aField, String aName, Wrapper anAttributed) {
        return aField.getValue();
    }

    /** 
     * Extract the value from the given select field.
     * 
     * @param    aTable          The table field the processed field is coming from, must not be <code>null</code>.
     * @param    aField          The field to get the value from, must not be <code>null</code>.
     * @param    anAttributed    The attributed to be filled with data, must not be <code>null</code>.
     * @param    aName           The name of the attribute currently processed, must not be <code>null</code>.
     * @return   The requested value.
     */
    protected Object getValueFromField(TableField aTable, SelectField aField, String aName, Wrapper anAttributed) {
        List theSelection = aField.getSelection();
    
        return (!aField.isMultiple()) ? CollectionUtil.getSingleValueFrom(theSelection) : theSelection;
    }

}
