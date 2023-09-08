/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.security;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.boundsec.attribute.AttributeClassifierManager;
import com.top_logic.element.boundsec.manager.ElementAccessHelper;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.layout.security.handler.SecurityExportHandler;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.form.FormField;
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
import com.top_logic.layout.provider.StringOptionLabelProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLNamed;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/**
 * {@link LayoutComponent} assigning access rights for roles on attribute classifications.
 * 
 * @see AttributeSecurityEditComponent
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class AttributeClassifierRolesComponent extends EditComponent {

	/**
	 * Configuration options for {@link AttributeClassifierRolesComponent}.
	 */
	public interface Config extends EditComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@NullDefault
		String getLockOperation();

		@Override
		@StringDefault(AttributeClassifirRolesApplyHandler.COMMAND_ID)
		String getApplyCommand();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			registry.registerButton(SecurityExportHandler.COMMAND_ID);
			EditComponent.Config.super.modifyIntrinsicCommands(registry);
		}
	}

	/**
	 * Place holder for the global domain. This constant is used as model of the
	 * {@link AttributeClassifierRolesComponent} to configure the global attribute roles.
	 * 
	 * @see I18NConstants#GLOBAL_DOMAIN
	 */
	public static final Object GLOBAL_DOMAIN = GlobalDomain.INSTANCE;

    private Map generators;

    private Map rightOptions;

    public AttributeClassifierRolesComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);

        this.rightOptions = new HashMap();
        this.rightOptions.put("element.accessRight.option.none",
                Collections.EMPTY_LIST);
        this.rightOptions.put("element.accessRight.option.read",
        		Arrays.asList(new String[] {
        				"Read"
        		}));
        this.rightOptions.put("element.accessRight.option.readExport",
                Arrays.asList(new String[] {
                        "Read", "Export"
                }));
        this.rightOptions.put("element.accessRight.option.write",
        		Arrays.asList(new String[] {
        				"Read", "Write"
        		}));
        this.rightOptions.put("element.accessRight.option.writeExport",
                Arrays.asList(new String[] {
                        "Read", "Export", "Write"
                }));
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == GLOBAL_DOMAIN || anObject instanceof TLClass;
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(GLOBAL_DOMAIN);
			return true;
		}
		return super.validateModel(context);
    }

    public RowFormGroupGenerator getRowFormGroupGenerator(String aFieldName) {
        return (RowFormGroupGenerator) this.generators.get(aFieldName);
    }

	/**
	 * The {@link TLClass} model of this component or <code>null</code> when model is
	 *         {@link #GLOBAL_DOMAIN}.
	 */
	public TLClass getMetaElement() {
		Object model = getModel();
		if (model == GLOBAL_DOMAIN) {
			return null;
		}
		return (TLClass) model;
    }

    @Override
	public FormContext createFormContext() {

        this.generators = new HashMap();

        FormContext theFC = new FormContext("form", this.getResPrefix());

        TLClass theME = this.getMetaElement();

        Set  theSelected;
        List theAvailable;

        AttributeClassifierManager theACM   = AttributeClassifierManager.getInstance();
        List                       theCList = theACM.getDeclaredClassifiers(theME);
        theAvailable = ElementAccessHelper.getAvailableRoles(theME, (ElementAccessManager)AccessManager.getInstance());

        for (Iterator theCIt = theCList.iterator(); theCIt.hasNext();) {

            FastList theClassifierList = (FastList) theCIt.next();

            theSelected = new HashSet();

            for (Iterator theAIt = theAvailable.iterator(); theAIt.hasNext();) {
                BoundRole theBR = (BoundRole) theAIt.next();
				{
					for (Iterator<FastListElement> theFIt = theClassifierList.elements().iterator(); theFIt
						.hasNext();) {
						FastListElement theElement = theFIt.next();
                        if ( ! ElementAccessHelper.getAccessRights(theElement, theBR).isEmpty()) {
                            theSelected.add(theBR);
                            continue;
                        }
                    }
                }
            }

            AttClassRolesFormGroupGenerator theGenerator = new AttClassRolesFormGroupGenerator(theClassifierList);

			final Accessor<?> tableAccessor = new TableFieldAccessor(theGenerator, theFC);
			final String selectFieldName = getAttributeName(theClassifierList);
			SelectField theClassified = FormFactory.newSelectField(selectFieldName, theAvailable, true, new ArrayList(theSelected), false);
			theClassified.setOptionComparator(LabelComparator.newCachingInstance());
			theClassified.setTableConfigurationProvider(new TableConfigurationProvider() {
				@Override
				public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
					defaultColumn.setFilterProvider(null);
					defaultColumn.setAccessor(tableAccessor);
				}

				@Override
				public void adaptConfigurationTo(TableConfiguration table) {
					table.setResPrefix(AttributeClassifierRolesComponent.this.getResPrefix().append(selectFieldName));
					table.setDefaultFilterProvider(null);
					ColumnConfiguration nameColumn = table.declareColumn(TLNamed.NAME_ATTRIBUTE);
					nameColumn.setColumnLabelKey(I18NConstants.ROLE);
					nameColumn.setComparator(LabelComparator.newCachingInstance());
					table.setColumnCustomization(ColumnCustomization.ORDER);
				}
			});
            new RowFormGroupHolder(theFC, theClassified, theSelected, this, theGenerator);
            theFC.addMember(theClassified);
            this.generators.put(theClassified.getName(), theGenerator);
        }

        return theFC;
    }

    private static String getAttributeName(FastList aFastList) {
		return "list_" + convertRowFieldName(aFastList.getName());
    }

    private static String getAttributeName(FastListElement aFastListElement) {
		return "classifier_" + convertRowFieldName(aFastListElement.getName());
    }

    private String getRightOption(Collection someAccessRights) {
        for (Iterator theIt = this.rightOptions.entrySet().iterator(); theIt.hasNext();) {
            Map.Entry theEntry = (Map.Entry) theIt.next();
            if (((CollectionUtil.containsSame((Collection) theEntry.getValue(), someAccessRights)))) {
                return (String) theEntry.getKey();
            }
        }
        return null;
    }

    public Collection getClassificationAttributeNames() {
        return this.generators.keySet();
    }

    public String getTableColumnNamesDeclaration(String aCalssificationAttributeName) {
        return ((AttClassRolesFormGroupGenerator) this.generators.get(aCalssificationAttributeName)).getColumnNameDeclaration();
    }

    private Collection getAccessRights(String aRightOption) {
        return (Collection) this.rightOptions.get(aRightOption);
    }

    public static class AttributeClassifirRolesApplyHandler extends AbstractApplyCommandHandler {

        public static final String COMMAND_ID = "attributeClassifierRolesApply";

        

        public AttributeClassifirRolesApplyHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		protected boolean storeChanges(LayoutComponent aComponent, FormContext aContext, Object aModel) {
            AttributeClassifierRolesComponent theComponent = (AttributeClassifierRolesComponent) aComponent;

            // remove old access associations
            for (Iterator theIt = theComponent.generators.entrySet().iterator(); theIt.hasNext(); ) {
                Map.Entry theEntry     = (Map.Entry) theIt.next();
                AttClassRolesFormGroupGenerator theGenerator = (AttClassRolesFormGroupGenerator) theEntry.getValue();

                for (Iterator theEIt = theGenerator.fastList.elements().iterator(); theEIt.hasNext(); ) {
                    FastListElement theElement = (FastListElement) theEIt.next();
                    ElementAccessHelper.clearAccessRights(theElement);
                }

            }

            // add new access associations
            for (Iterator theIt = theComponent.generators.entrySet().iterator(); theIt.hasNext(); ) {
                Map.Entry             theEntry     = (Map.Entry)                       theIt.next();
                String                theFieldName = (String)                          theEntry.getKey();
                RowFormGroupGenerator theGenerator = (AttClassRolesFormGroupGenerator) theEntry.getValue();

                theGenerator.handleResult(theComponent, aContext, aContext.getField(theFieldName), aModel);

            }
            AttributeClassifierManager.getInstance().resetMetaAttributeClassificationAccess();

			return true;
        }
	}

	private static String convertRowFieldName(String name) {
		return name.replace('.', '_');
    }

    @Override
    public void removeFormContext() {
        super.removeFormContext();
        doRemoveFormContext();
    }

    protected void doRemoveFormContext() {
        this.generators = null;
    }

    public class AttClassRolesFormGroupGenerator implements RowFormGroupGenerator {

        FastList fastList;
        String   columnNameDeclaration;
        Map      fastListElements;

        public AttClassRolesFormGroupGenerator(FastList aFastList) {
            this.fastList = aFastList;
            this.fastListElements = new HashMap();
            StringBuffer theSB = new StringBuffer();
			theSB.append(FastList.NAME);
			{
                for (Iterator theIt = this.fastList.elements().iterator(); theIt.hasNext(); ) {

                    FastListElement theElement = (FastListElement) theIt.next();
                    String          theAttName = getAttributeName(theElement);
                    theSB.append(" field.");
                    theSB.append(theAttName);
                }
            }
            this.columnNameDeclaration = theSB.toString();
        }

        public String getColumnNameDeclaration() {
            return this.columnNameDeclaration;
        }

        @Override
		public void addFormFields(ControlComponent aComponent, FormGroup aFormGroup, Object aRowObject) {

            BoundRole theBR        = (BoundRole) aRowObject;

            String        theGroupName = this.getRowGroupName(aRowObject);
            FormGroup     theGroup     = new FormGroup(theGroupName, aComponent.getResPrefix());
			theGroup.setStableIdSpecialCaseMarker(aRowObject);
            aFormGroup.addMember(theGroup);

            List          thePossible = new ArrayList(AttributeClassifierRolesComponent.this.rightOptions.keySet());
			LabelProvider theLP = new StringOptionLabelProvider(Resources.getInstance(), ResPrefix.GLOBAL);
			{
                for (Iterator theIt = this.fastList.elements().iterator(); theIt.hasNext(); ) {

                    FastListElement theElement = (FastListElement) theIt.next();
                    String          theAttName = getAttributeName(theElement);
                    Collection      theARs     = ElementAccessHelper.getAccessRights(theElement, theBR);
                    String          theCurrent = AttributeClassifierRolesComponent.this.getRightOption(theARs);

                    SelectField theField    = FormFactory.newSelectField(theAttName, thePossible, false, Collections.singletonList(theCurrent), false);
                    theField.setMandatory(true);
                    theGroup.addMember(theField);
                    theField.setOptionLabelProvider(theLP);

                    this.fastListElements.put(theField, theElement);
                }
            }
        }

        @Override
		public String getRowGroupName(Object aRowObject) {
            BoundRole theBR = (BoundRole) aRowObject;
			String name = IdentifierUtil.toExternalForm(theBR.getID()).toString();
			return getAttributeName(this.fastList) + "_" + convertRowFieldName(name);
        }

        @Override
		public void handleResult(ControlComponent aComponent, FormGroup aFormGroup, FormField aTableAttribute, Object aModel) {

            for (Iterator theRIt = ((Collection) aTableAttribute.getValue()).iterator(); theRIt.hasNext(); ) {
                BoundedRole       theRole    = (BoundedRole) theRIt.next();
                FormGroup         theGroup   = (FormGroup)   aFormGroup.getMember(this.getRowGroupName(theRole));
                for (Iterator theGIt = theGroup.getFields(); theGIt.hasNext(); ) {
                    SelectField     theFF      = (SelectField) theGIt.next();
                    FastListElement theElement = (FastListElement) this.fastListElements.get(theFF);
                    String          theOption  = (String)          theFF.getSingleSelection();
                    Collection      theRights  = AttributeClassifierRolesComponent.this.getAccessRights(theOption);

                    for (Iterator theIt = theRights.iterator(); theIt.hasNext();) {
                        String theRight = (String) theIt.next();
                        ElementAccessHelper.addAccessRight(theElement, theRight, theRole);
                    }
                }
            }

        }

		@Override
		public ControlProvider getControlProvider() {
			return DefaultFormFieldControlProvider.INSTANCE;
		}

    }
}
