/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.boundsec.attribute.AttributeClassifierManager;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElementCollectionComparator;
import com.top_logic.layout.FormFieldControlComparator;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.tableForm.RowFormGroupGenerator;
import com.top_logic.layout.form.component.tableForm.RowFormGroupHolder;
import com.top_logic.layout.form.component.tableForm.TableFieldAccessor;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLCollator;

/**
 * {@link LayoutComponent} for classifying attributes.
 * 
 * @see AttributeClassifierRolesComponent
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class AttributeSecurityEditComponent extends EditComponent {

	/**
	 * Configuration options for {@link AttributeSecurityEditComponent}.
	 */
	public interface Config extends EditComponent.Config {
		@Override
		@NullDefault
		String getLockOperation();

		@Override
		@StringDefault(AttributeClassificationApplyHandler.COMMAND_ID)
		String getApplyCommand();

	}
    
    public static final String FORM_FIELD_CLASSIFIED = "classified";
    private RowFormGroupGenerator generator;  
    private Map classifiers;

    public AttributeSecurityEditComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof TLClass;
    }
    
    public RowFormGroupGenerator getRowFormGroupGenerator() {
        return this.generator;
    }

    @Override
	public FormContext createFormContext() {
        
        FormContext theFC = new FormContext("form", this.getResPrefix());
        
        TLClass theME = (TLClass) this.getModel();
        
        List theSelected;
        List theAvailable;
        
        if (theME != null) {
            this.classifiers = getClassifiers(theME);
            this.generator = new AttSecRowFormGroupGenerator(theME, this.classifiers);
			Collection<TLStructuredTypePart> theMetaAttributes = TLModelUtil.getLocalMetaAttributes(theME);
            theAvailable = new ArrayList(theMetaAttributes);
            theSelected = new ArrayList();
            for (Iterator theIt = theMetaAttributes.iterator(); theIt.hasNext();) {
                TLStructuredTypePart theMA          = (TLStructuredTypePart) theIt.next();
                Set           theClassifiers = AttributeOperations.getClassifiers(theMA);
                if ( ! theClassifiers.isEmpty()) {
                    theSelected.add(theMA);
                }
            }
        } else {
            theSelected  = Collections.EMPTY_LIST;
            theAvailable = Collections.EMPTY_LIST;
        }
        SelectField theClassified = FormFactory.newSelectField(FORM_FIELD_CLASSIFIED, theAvailable, true, theSelected, false);
		theClassified.setOptionComparator(LabelComparator.newCachingInstance());
		theClassified.setTableConfigurationProvider(new TableConfigurationProvider() {
			@Override
			public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
				AttributeSecurityEditComponent component = AttributeSecurityEditComponent.this;
				TableFieldAccessor accessor =
					new TableFieldAccessor(component.getRowFormGroupGenerator(), component.getFormContext());
				defaultColumn.setFilterProvider(null);
				defaultColumn.setAccessor(accessor);
			}

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.setResPrefix(AttributeSecurityEditComponent.this.getResPrefix().append(FORM_FIELD_CLASSIFIED));
				table.setDefaultFilterProvider(null);
				table.setColumnCustomization(ColumnCustomization.ORDER);
				setColumnComparators(table);
			}
		});
        new RowFormGroupHolder(theFC, theClassified, theSelected, this, this.generator);
        theFC.addMember(theClassified);
        
        return theFC;
    }
    
    public static class AttributeClassificationApplyHandler extends AbstractApplyCommandHandler {
        
        public static final String COMMAND_ID = "attributeClassificationApply";

        

        public AttributeClassificationApplyHandler(InstantiationContext context, Config config) {
			super(context, config);
        }
        
        @Override
		protected boolean storeChanges(LayoutComponent aComponent, FormContext aContext, Object aModel) {
            ((AttributeSecurityEditComponent) aComponent).generator.handleResult((ControlComponent) aComponent, aContext, aContext.getField(FORM_FIELD_CLASSIFIED), aModel);
            AttributeClassifierManager.getInstance().resetMetaAttributeClassificationAccess();
			return true;
        }

    }
    
    private static String convertRowFieldName(String aName) {
        return aName.replace('.', '_');
    }
    
    public String getColumnDeclaration() {
        StringBuffer theSB = new StringBuffer();
		theSB.append(TLNamed.NAME_ATTRIBUTE);
        if (this.classifiers != null) {
            for (Iterator theIt = this.classifiers.keySet().iterator(); theIt.hasNext();) {
                String theListName = (String) theIt.next();
                theSB.append(" field.");
                theSB.append(convertRowFieldName(theListName));
            }
        }
        return theSB.toString();
    }
    
	/**
	 * Sets the column comparators. Default column must not be adapted.
	 */
	protected void setColumnComparators(TableConfiguration config) {
		String[] columnNames = getColumnDeclaration().trim().split("\\s+");
		config.declareColumn(columnNames[0]).setComparator(LabelComparator.newCachingInstance());
		TLCollator tlCollator = new TLCollator();
		for (int i = 1; i < columnNames.length; i++) {
			config.declareColumn(columnNames[i]).setComparator(new SelectFieldFastListComparator(tlCollator));
		}
	}

    private Map getClassifiers(TLClass aME) {
        Map theResult = new HashMap();
        
        AttributeClassifierManager theACM = AttributeClassifierManager.getInstance();
        Collection theClassifiers = theACM.getAvailableClassifiers(aME);
        for (Iterator theIt = theClassifiers.iterator(); theIt.hasNext();) {
            FastList theElement = (FastList) theIt.next();
            theResult.put(theElement.getName(), theElement);
        }
        return theResult;
    }
    
    public class AttSecRowFormGroupGenerator implements RowFormGroupGenerator {
        
        TLClass metaElement;
        Map         classifiers;        

        public AttSecRowFormGroupGenerator(TLClass aME, Map someClassifiers) {
            this.metaElement = aME;
            this.classifiers = someClassifiers;
        }
        
        @Override
		public void addFormFields(ControlComponent aComponent, FormGroup aFormGroup, Object aRowObject) {
            
            TLStructuredTypePart theMA        = (TLStructuredTypePart) aRowObject;
            Set           theSelected  = AttributeOperations.getClassifiers(theMA);
            
            String        theGroupName = this.getRowGroupName(aRowObject); 
            FormGroup     theGroup     = new FormGroup(theGroupName, aComponent.getResPrefix());
            aFormGroup.addMember(theGroup);
            
            for (Iterator theIt = this.classifiers.entrySet().iterator(); theIt.hasNext();) {
                Map.Entry   theElement  = (Map.Entry) theIt.next();
                String      theKey      = (String)   theElement.getKey();
                FastList    theFastList = (FastList) theElement.getValue();
                List thePossible;
				thePossible = theFastList.elements();
                List        theCurrent  = new ArrayList(theSelected);
                theCurrent.retainAll(thePossible);
                
                SelectField theField    = FormFactory.newSelectField(convertRowFieldName(theKey), thePossible, false, theCurrent, false);
                theGroup.addMember(theField);
                theField.setOptionLabelProvider(MetaLabelProvider.INSTANCE);
                
            }
        }
        
        @Override
		public String getRowGroupName(Object aRowObject) {
            TLStructuredTypePart theMA = (TLStructuredTypePart) aRowObject;
            return theMA.getName();
        }

        @Override
		public void handleResult(ControlComponent aComponent, FormGroup aFormGroup, FormField aTableAttribute, Object aModel) {
            
            TLClass theME = (TLClass) aModel;
			for (Iterator theIt = TLModelUtil.getLocalMetaAttributes(theME).iterator(); theIt.hasNext();) {
                TLStructuredTypePart theMA = (TLStructuredTypePart) theIt.next();
                AttributeOperations.setClassifiers(theMA, Collections.EMPTY_LIST);
            }
            
            Collection theAtts = (Collection) aTableAttribute.getValue();
            if (theAtts != null) {
                for (Iterator theIt = theAtts.iterator(); theIt.hasNext();) {
                    TLStructuredTypePart theMA          = (TLStructuredTypePart) theIt.next();
                    List          theClassifiers = new ArrayList();
                    FormGroup     theGroup       = (FormGroup) aFormGroup.getMember(this.getRowGroupName(theMA));
                    for(Iterator theInnerIt = theGroup.getMembers(); theInnerIt.hasNext(); ) {
                        FormMember theMember = (FormMember) theInnerIt.next();
                        if (theMember instanceof SelectField) {
                            theClassifiers.addAll(((SelectField) theMember).getSelection());
                        }
                    }
                    AttributeOperations.setClassifiers(theMA, theClassifiers);
                }
            }
            
        }
        
		@Override
		public ControlProvider getControlProvider() {
			return DefaultFormFieldControlProvider.INSTANCE;
		}

    }
    
    public static class SelectFieldFastListComparator implements Comparator {

		private final FormFieldControlComparator _inner;

		public SelectFieldFastListComparator(TLCollator collator) {
			_inner = new FormFieldControlComparator(new FastListElementCollectionComparator(collator));
		}
    	
		@Override
		public int compare(Object o1, Object o2) {
			return _inner.compare(o1, o2);
		}
    	
    }
    
}
