/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.search;

import java.util.Collections;
import java.util.List;

import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.element.layout.meta.search.SearchFieldSupport;
import com.top_logic.element.layout.meta.search.SearchFilterSupport;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.query.MetaAttributeFilter;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.layout.basic.LabelSorter;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class ContactSearchFieldSupport extends SearchFieldSupport {
	/**
	 * Replaces the mandator field created via the {@link AttributeUpdate}
	 * (which is a multi selection field) with a single select field. If the old
	 * field has a value it will be used for the new field as well, otherwise
	 * the given {@link Mandator} will be used.
	 * 
	 * @param context              the {@link AttributeFormContext}
	 * @param aSearchMeta          the {@link TLClass} used for the search
	 * @param anAttributeName      the name of the mandator attribute for the given {@link TLClass}
	 * @param aMandator            the {@link Mandator} that will be used as a default value if no value was set via the {@link AttributeUpdate}
	 */
	public static void updateMandatorField(AttributeFormContext context, TLClass aSearchMeta, String anAttributeName, Mandator aMandator, StoredQuery aQuery ) {
		{
			TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(aSearchMeta, anAttributeName);
			AttributeUpdate theUpdate = context.getAttributeUpdateContainer().getAttributeUpdate(theMA, null);
			theUpdate.setValue(aMandator);

			AllElementVisitor theVisitor  = new AllElementVisitor();
            TraversalFactory.traverse(Mandator.getRootMandator(), theVisitor, TraversalFactory.DEPTH_FIRST);
            List theOptions = theVisitor.getList();
//			AllElementVisitor theVisitor  = new AllElementVisitor();
//			Mandator.getRootMandator().traverse(theVisitor, Mandator.TRAVERSE_THIS_FIRST, Mandator.INFINITE_DEPTH, Mandator.INITIAL_DEPTH);
//			List theOptions = theVisitor.getList();
			LabelSorter.sortByLabelInline(theOptions, MetaLabelProvider.INSTANCE);

			SelectField theField = (SelectField) context.getField(anAttributeName, aSearchMeta);
			SelectField theNew   = FormFactory.newSelectField(theField.getName(), theOptions, false, theField.getSelection(), theField.isImmutable());

			// use the selection of the old field if present (was then set via
			// the StoredQuery), otherwise use the mandator of the current user.
			Object theSelection = theField.getSingleSelection();
			theSelection = theSelection != null ? theSelection : aMandator;
			
			boolean isRelevant = false;
	        boolean doNegate   = false;
			if (aQuery != null) {
				MetaAttributeFilter theMAF = StoredQuery.getFilterFor(aQuery.getFilters(), theMA , "");
				
				if (theMAF != null) {
	            	isRelevant = theMAF.isRelevant();
	            	doNegate = theMAF.getNegate();
				}
			}

			theNew.setDefaultValue(Collections.singletonList(theSelection));
			theNew.setOptionLabelProvider(MetaResourceProvider.INSTANCE);
			theNew.setLabel(theField.getLabel());
			theNew.setMandatory(true);

			String theChkBoxName =SearchFilterSupport.getRelevantAndNegateMemberName(theMA, null);
//			boolean doNegate = false;
//			if (context.hasMember(relevantAndNegateMemberName)) {
//				Boolean theVal = (Boolean) ((BooleanField)context.getField(relevantAndNegateMemberName)).getValue();
//				doNegate = theVal != null ? theVal.booleanValue() : false;
//			}
			BooleanField theCheckbox = SearchFieldSupport.addRelevantAndNegateMember(context, theChkBoxName, doNegate, isRelevant);

			theNew.setAsSingleSelection(theSelection);

			context.removeMember(theField);
			context.addMember(theNew);

			SearchFieldSupport.ChangeColorListener.addTo(theCheckbox);
			SearchFieldSupport.InputColorListener.addTo(theNew, false);
		}
	}
}
