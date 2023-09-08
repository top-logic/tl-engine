/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.AbstractFormMemberControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * This class is an simplified and user friendly edit list component to view and edit a
 * single fastlist or ordered list. ID and other technical details are handled
 * automatically and only one language is used for the names.
 *
 * @see EditListComponent
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class SimpleEditListComponent extends EditListComponent {

	/**
	 * Configuration of the {@link SimpleEditListComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EditListComponent.Config {

		@Override
		@StringDefault(SimpleListApplyCommandHandler.COMMAND)
		String getApplyCommand();

	}

	/**
	 * Creates a {@link SimpleEditListComponent}.
	 */
    public SimpleEditListComponent(InstantiationContext context, Config aSomeAttrs) throws ConfigurationException {
        super(context, aSomeAttrs);
    }

    @Override
	public FormContext createFormContext() {
        FormContext theContext = super.createFormContext();
        FormField theField = (FormField)theContext.getMember(FIELD_LIST_NAME);
		theField.setValue(Resources.getInstance(TLContext.getLocale())
			.getString(FastListElementLabelProvider.labelKey((FastList) getModel())));
        return theContext;
    }

    @Override
	protected List createRowList(FormContext aContext) {
        List theRowList = super.createRowList(aContext);
		Resources theResources = Resources.getInstance(TLContext.getLocale());
        Iterator it = theRowList.iterator();
        while (it.hasNext()) {
            Map theRow = (Map)it.next();
            FormField theField = (FormField)((AbstractFormMemberControl)CollectionUtil.getFirst((Collection)theRow.get(COLUMN_NAME))).getModel();
            FastListElement classifier = ((FastListElement)theRow.get(COLUMN_SELF));
			theField.setValue(theResources.getString(TLModelNamingConvention.classifierKey(classifier)));
        }
        return theRowList;
    }

    @Override
	protected void computeColumns() {
        if (columns == null) {
            columns = COLUMN_NAME + "," + COLUMN_DESCRIPTION + "," + COLUMN_IN_USE;
        }
        if (columnSizes == null) {
            columnSizes = "32, 72";
        }
        super.computeColumns();
    }

    @Override
	protected void addMoreToContext(FormContext aContext, FastList aModel) {
        // Nothing more to add in this component
    }

    @Override
	protected void addMoreToRow(Map aRow, FastListElement aElement, FormContainer aGroup) {
        // Nothing more to add in this component
    }

    @Override
	protected void addMoreToNewRow(Map aRow, FormContainer aGroup) {
        // Nothing more to add in this component
    }

    @Override
	protected void removeMoreFromRow(Map aRow) {
        // Nothing more to remove in this component
    }

    @Override
	protected String getNameFieldValue(Control aControl) {
        return null;
    }

    /**
     * Command handler for applying changes made in the FastList or FastList.
     * This class is intended to be used only from the SimpleEditListComponent.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class SimpleListApplyCommandHandler extends ListApplyCommandHandler {

        public static final String COMMAND = "ApplySimpleList";

		
        
		public SimpleListApplyCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
		}
        
        @Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
            FastList theFastList = (FastList)model;
            ObjectTableModel theTableModel = (ObjectTableModel)((TableField)formContext.getMember(FIELD_TABLELIST)).getTableModel();
			Accessor theAccessor = theTableModel.getTableConfiguration().getDefaultColumn().getAccessor();
            saveI18NList = new ArrayList();

            // Apply changes of the list
            FormField theField = (FormField)formContext.getMember(FIELD_LIST_NAME);
            String theListName = (String)theField.getValue();
            theField = (FormField)formContext.getMember(FIELD_LIST_DESCRIPTION);
            theFastList.setDescription((String)theField.getValue());

            // Save the I18N name to all languages
			ResKey enumKey = TLModelNamingConvention.enumKey(theFastList);
            String[] supportedLanguages = ResourcesModule.getInstance().getSupportedLocaleNames();
			for (int j = 0; j < supportedLanguages.length; j++) {
                String language = supportedLanguages[j];
				if (!StringServices.equalsEmpty(Resources.getInstance(language).getString(enumKey),
					theListName)) {
					saveI18NList.add(new Object[] { language, enumKey, theListName });
                }
            }

            // Create added elements
            HashSet theSet = new HashSet();
            for (int i = 0; i < theTableModel.getRowCount(); i++) {
                Map theRow = (Map)theTableModel.getRowObject(i);
                FastListElement theElement = (FastListElement)theAccessor.getValue(theRow, COLUMN_SELF);
                if (theElement == null) {
                    theElement = theFastList.addElement(null, getNewName(theFastList), null, 0);
                    theAccessor.setValue(theRow, COLUMN_SELF, theElement);
                }
                theSet.add(theElement);
            }

            // Delete removed elements
            Iterator it = new ArrayList(theFastList.elements()).iterator();
            while (it.hasNext()) {
                FastListElement theElement = (FastListElement)it.next();
                if (!theSet.contains(theElement)) {
					ResKey key = ResKey.legacy(theElement.getName());
                    for (int j = 0; j < supportedLanguages.length; j++) {
						saveI18NList.add(new Object[] { supportedLanguages[j], key, null });
                    }
                    theFastList.removeElement(theElement);
                }
            }

            // Apply changes of elements
            for (int i = 0; i < theTableModel.getRowCount(); i++) {
                Map theRow = (Map)theTableModel.getRowObject(i);
                FastListElement theElement = (FastListElement)theAccessor.getValue(theRow, COLUMN_SELF);

                String theName = FormFieldHelper.getStringValue(theRow, COLUMN_NAME, theAccessor);
                String theDescription = FormFieldHelper.getStringValue(theRow, COLUMN_DESCRIPTION, theAccessor);
                theElement.setDescription(theDescription);
                theElement.setOrder(i);

                // Save the I18N name to all languages
				ResKey classifierKey = TLModelNamingConvention.classifierKey(theElement);
                for (int j = 0; j < supportedLanguages.length; j++) {
					String language = supportedLanguages[j];
					if (!StringServices.equalsEmpty(Resources.getInstance(language).getString(classifierKey), theName)) {
						saveI18NList.add(new Object[] { language, classifierKey, theName });
                    }
                }

            }

			theField = formContext.getField(FIELD_LIST_ORDERED);
			if (theField.isChanged()) {
				theFastList.setOrdered(((BooleanField) theField).getAsBoolean());
			}

            return true;
        }


        protected String getNewName(FastList aList) {
            String prefix = aList.getName();
            HashSet theSet = new HashSet(aList.size());
            Iterator it = aList.elements().iterator();
            while (it.hasNext()) {
                theSet.add(((FastListElement)it.next()).getName());
            }
            String theName = prefix + "." + aList.size();
            if (!theSet.contains(theName)) return theName;

            for (int i = 0; i < aList.size(); i++) {
                theName = prefix + "." + i;
                if (!theSet.contains(theName)) return theName;
            }
            return "";
        }
    }

}
