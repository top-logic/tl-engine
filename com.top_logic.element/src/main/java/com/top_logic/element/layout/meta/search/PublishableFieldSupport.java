/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.List;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.Expandable;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class PublishableFieldSupport {
	
	public static FormContext createFormContext(Expandable aComp, FormContext aCtx, ResPrefix aResPrefix) {
		FormContext theCtx = aCtx;
		FormGroup theGroup = new FormGroup(QueryUtils.FORM_GROUP, aResPrefix);
		
		BooleanField publishQueryField = FormFactory.newBooleanField(QueryUtils.PUBLISH_QUERY_FIELD);
		publishQueryField.setAsBoolean(false);
		theGroup.addMember(publishQueryField);

		List visibleGroups;
		visibleGroups = Group.getAll();
		SelectField visibleGroupsField = FormFactory.newSelectField(QueryUtils.VISIBLE_GROUPS_FIELD, visibleGroups, true, false);
		visibleGroupsField.setDisabled(true);
	    theGroup.addMember(visibleGroupsField);
		
	    if(!aComp.isExpanded()) {
			theGroup.setVisible(false);
		}
	    
	    theCtx.addMember(theGroup);
	    
	    publishQueryField.addValueListener(new VisisbleGroupsFieldDisableListener());
	    
	    return theCtx;
	}
	
	/**
	 * A {@link ValueListener} to enable/disable the groups field in the "save as" dialogs.
	 */
	public static class VisisbleGroupsFieldDisableListener implements ValueListener {

		/**
		 * @see com.top_logic.layout.form.ValueListener#valueChanged(com.top_logic.layout.form.FormField,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
			FormContext theCtx = aField.getFormContext();
			if (theCtx.hasMember(QueryUtils.FORM_GROUP)) {
				FormGroup theGrp = (FormGroup) theCtx.getMember(QueryUtils.FORM_GROUP);
				FormField groupsField = theGrp.getField(QueryUtils.VISIBLE_GROUPS_FIELD);
				if (((BooleanField) aField).getAsBoolean()) {
					groupsField.setDisabled(false);
					groupsField.setMandatory(true);
				}
				else {
					groupsField.setDisabled(true);
					groupsField.setMandatory(false);
				}
			}
		}
	}
}
