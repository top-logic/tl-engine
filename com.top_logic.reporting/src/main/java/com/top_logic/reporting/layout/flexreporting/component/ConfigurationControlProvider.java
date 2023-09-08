/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.component;

import org.w3c.dom.Document;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ButtonRenderer;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormListControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.template.SimpleListControlProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The ConfigurationControlProvider provides individual layout templates for each
 * {@link FormContainer} that habe been created by {@link ConfigurationFormFieldHelper}.
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public class ConfigurationControlProvider extends SimpleListControlProvider {

    protected static final Document TABLE_ROW_TEMPLATE_DEFAULT = DOMUtil.parseThreadSafe(
             "<fieldset>"
            +   "<table"
            +   " xmlns='" + HTMLConstants.XHTML_NS + "'"
            +   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
            +   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
            +   ">"
            +       "<tr>"
            +           "<td valign='top' class='content'><p:field name='select' /></td>"
            +           "<td class='content'><p:field name='removeRow' /></td>"
            +       "</tr>"
            +       "<tr>"
            +           "<td valign='top' class='content' colspan='2'><p:field name='groupContainer' /></td>"
            +       "</tr>"
            +   "</table>"
            +   "</fieldset>"
        );

    protected static final Document TABLE_TEMPLATE_DEFAULT = DOMUtil.parseThreadSafe(
                "<table"
            +   " xmlns='" + HTMLConstants.XHTML_NS + "'"
            +   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
            +   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
            +   " >"
            +       "<t:items>"
            +       "<tr>"
            +       "<td class='content'><p:self /></td>"
            +       "</tr>"
            +       "</t:items>"
            +   "</table>"
        );

    protected static final Document GROUP_CONTAINER_TEMPLATE_DEFAULT = DOMUtil.parseThreadSafe(
                "<table"
            +   " xmlns='" + HTMLConstants.XHTML_NS + "'"
            +   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
            +   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
            +   ">"
            +       "<t:items>"
            +       "<tr>"
            +       "<td class='content'><p:self /></td>"
            +       "</tr>"
            +       "</t:items>"
            +   "</table>"
        );

    protected static final Document FORM_GROUP_TEMPLATE_DEFAULT = DOMUtil.parseThreadSafe(
                "<table"
            +   " xmlns='" + HTMLConstants.XHTML_NS + "'"
            +   " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
            +   " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
            +   ">"
            +       "<t:items>"
            +       "<tr>"
            +       "<td class='label'><p:self style='label' /></td>"
            +       "<td class='content'><p:self /><p:self style='error' /></td>"
            +       "</tr>"
            +       "</t:items>"
            +   "</table>"
        );


	public ConfigurationControlProvider(ResPrefix aResPrefix) {
        super(aResPrefix);
    }

    @Override
	public Control visitFormContainer(FormContainer aMember, Void arg) {

        String theName = aMember.getName();

		ConfigurationDescriptor theConfDesc = aMember.get(ConfigurationFormFieldHelper.CONFIG_DESCRIPTOR);
		PropertyDescriptor thePropDesc = aMember.get(ConfigurationFormFieldHelper.PROPERTY_DESCRIPTOR);

        if (theName.startsWith(ConfigurationFormFieldHelper.TABLE_ROW_PREFIX)) {
            return this.getTableRowGroupControl(aMember, thePropDesc);
        }
        else if (theName.startsWith(ConfigurationFormFieldHelper.GROUP_CONTAINER)) {
            return this.getGroupContainerControl(aMember, thePropDesc);
        }
        else if (theName.startsWith(ConfigurationFormFieldHelper.TABLE_PREFIX)) {
            return this.getTableGroupControl(aMember, thePropDesc);
        }
        else if (theConfDesc != null) {
            return this.getFormGroupControl(aMember, theConfDesc);
        }

		return super.visitFormContainer(aMember, arg);
    }

    protected Control getTableRowGroupControl(FormContainer aGroup, PropertyDescriptor aPropDesc) {
        return new FormGroupControl((FormGroup) aGroup, this, this.getTableRowTemplate(aPropDesc), this.getResPrefix());
    }

    protected Control getFormGroupControl(FormContainer aGroup, ConfigurationDescriptor aConfDesc) {
        return new FormListControl(aGroup, this, this.getFormGroupTemplate(aConfDesc), this.getResPrefix());
    }

    protected Control getGroupContainerControl(FormContainer aGroup, PropertyDescriptor aPropDesc) {
        return new FormListControl(aGroup, this, this.getGroupContainerTemplate(aPropDesc), this.getResPrefix());
    }

    protected Control getTableGroupControl(FormContainer aGroup, PropertyDescriptor aPropDesc) {
        return new FormListControl(aGroup, this, this.getTableGroupTemplate(aPropDesc), this.getResPrefix());
    }
    
    @Override
	public Control visitCommandField(CommandField member, Void arg) {
    	return new ButtonControl(member, ButtonRenderer.newButtonRenderer(false));
    }


    protected Document getTableRowTemplate(PropertyDescriptor aDescriptor) {
        return TABLE_ROW_TEMPLATE_DEFAULT;
    }

    protected Document getFormGroupTemplate(ConfigurationDescriptor aDescriptor) {
        return FORM_GROUP_TEMPLATE_DEFAULT;
    }
    protected Document getGroupContainerTemplate(PropertyDescriptor aDescriptor) {
        return GROUP_CONTAINER_TEMPLATE_DEFAULT;
    }

    protected Document getTableGroupTemplate(PropertyDescriptor aDescriptor) {
        return TABLE_TEMPLATE_DEFAULT;
    }
}

