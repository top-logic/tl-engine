/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import static com.top_logic.layout.form.model.FormFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.util.PersonComparator;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.component.AbstractDeleteCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.tool.boundsec.wrap.GroupResourceHelper;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Component to view and edit groups.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class EditGroupComponent extends EditComponent {

    public interface Config extends EditComponent.Config {
		@Name(XML_KEY_PERMIT_SYSTEM_FLAG)
		@Mandatory
		boolean getPermitEditSystemFlag();

		@Override
		@StringDefault(ApplyGroupCommandHandler.COMMAND_ID)
		String getApplyCommand();

		@Override
		@StringDefault(DeleteGroupCommandHandler.COMMAND_ID)
		String getDeleteCommand();

	}

	/** i18n error message for invalid group IDs, used in {@link UniqueGroupNameConstraint} */
	public static final ResKey i18N_INVALID_GROUP_NAME = I18NConstants.ERROR_INVALID_GROUP_NAME;
    
    /** table form field for person contacts associated with this group */
    public static final String FORM_FIELD_MEMBERS = "members";
    
    public static final String FORM_FIELD_SYSTEM  = Group.GROUP_SYSTEM;
    public static final String FORM_FIELD_NAME    = Group.NAME_ATTRIBUTE;
    
    /** prefix for fields for i18n display names */
    public static final String PREFIX_FORM_FIELD_LANGUAGE    = "name_";
    
    /** prefix for fields for i18n display descriptions */
    public static final String PREFIX_FORM_FIELD_DESCRIPTION = "description_";

    private static final String XML_KEY_PERMIT_SYSTEM_FLAG = "permitEditSystemFlag";

    private boolean permitSystemFlag;
    
    /**
     * Default C'Tor
     */
    public EditGroupComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
        this.permitSystemFlag = someAttrs.getPermitEditSystemFlag();
    }
        
    /**
     * @see com.top_logic.layout.form.component.EditComponent#supportsInternalModel(java.lang.Object)
     */
    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof Group;
    }
    
    /**
     * Returns the input field name for a i18n display name for
     * the given language (e.g. "de", "en")
     * 
     * @return field name
     */
	public static String getLanguageFieldName(Locale locale) {
		return PREFIX_FORM_FIELD_LANGUAGE + locale;
    }
    
    /**
     * Returns the input field name for a i18n description for
     * the given language (e.g. "de", "en")
     */
	public static String getDescriptionFieldName(Locale locale) {
		return PREFIX_FORM_FIELD_DESCRIPTION + locale;
    }
    
    /**
     * @see com.top_logic.layout.form.component.EditComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        
        Group theGroup = (Group) this.getModel();
        
        String  theName;
        Boolean isSystem;
		List<Person> members;
        
        if (theGroup != null) {
            theName     = theGroup.getName();
            isSystem    = Boolean.valueOf(theGroup.isSystem());
			members = new ArrayList<>(theGroup.getMembers(false));
        } else {
            theName     = "";
            isSystem    = Boolean.FALSE; 
			members = Collections.emptyList();
        }
        
        FormContext theFC = new FormContext(this);
        
        StringField theNameField = FormFactory.newStringField(FORM_FIELD_NAME, theName, true);
        theNameField.setMandatory(true);
        theFC.addMember(theNameField);

        BooleanField theSystemField = FormFactory.newBooleanField(FORM_FIELD_SYSTEM, isSystem, !this.permitSystemFlag && !BoundHelper.getInstance().isAllowChangeDeleteProtection());
        theSystemField.setMandatory(true);
        theFC.addMember(theSystemField);

		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			StringField theField = FormFactory.newStringField(getLanguageFieldName(locale),
				GroupResourceHelper.findI18NName(theGroup, locale), false);
            theField.setMandatory(true);
            theFC.addMember(theField);
			theField = FormFactory.newStringField(getDescriptionFieldName(locale),
				GroupResourceHelper.findI18NDescription(theGroup, locale), false);
            theFC.addMember(theField);
        }
        
		List<Person> possibleMembers = Person.all();
		SelectField memberField = newSelectField(FORM_FIELD_MEMBERS, possibleMembers, MULTIPLE, members, !IMMUTABLE);
		memberField.setOptionComparator(PersonComparator.getInstance());
		makeConfigurable(memberField);
		theFC.addMember(memberField);
        
        return theFC;
    }

    @Override
	protected void becomingInvisible() {
		super.becomingInvisible();
		removeFormContext();
    }
    
    /**
     * @see com.top_logic.mig.html.layout.LayoutComponent#receiveModelCreatedEvent(java.lang.Object, java.lang.Object)
     */
    @Override
	protected boolean receiveModelCreatedEvent(Object aModel, Object aChangedBy) {
        if (aChangedBy instanceof NewGroupCommandHandler) {
            this.invalidate();
        }
        return super.receiveModelCreatedEvent(aModel, aChangedBy);
    }
    
    /**
     * null.
     */
    protected BoundObject getBindingObject() {
        return (null);
    }

    /**
     * This method stores a groups name in the dynamic resource file of the given language 
     * @return <code>true</code> if storing was successful, <code>false</code> otherwise
     */
	public static boolean saveI18N(ResourceTransaction tx, Group aGroup, String aNewName, String aNewDescription,
			Locale locale) {
        if (StringServices.isEmpty(aNewName)) {
            Logger.warn("the name must not be empty", EditGroupComponent.class);
            return false;
        }
        if (aNewDescription == null) {
            aNewDescription = "";
        }
        
        boolean theResult = false;
        
		ResKey theResKey = GroupResourceHelper.getResKey(aGroup);
		String theOldName = Resources.getInstance(locale).getString(theResKey);
		if (!theOldName.equals(aNewName)) {
			tx.saveI18N(locale, theResKey, aNewName);
			theResult = true;
		}

		theResKey = GroupResourceHelper.getDescriptionResKey(aGroup);
		theOldName = Resources.getInstance(locale).getString(theResKey);
		if (!theOldName.equals(aNewDescription)) {
			tx.saveI18N(locale, theResKey, aNewDescription);
			theResult = true;
        }

        return theResult;
    }

    
    public static class ApplyGroupCommandHandler extends AbstractApplyCommandHandler {

        public static final String COMMAND_ID = "applyDGroup";

        public ApplyGroupCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }
        
        @Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
            
            Group   theGroup   = (Group) model;
            String  theName    = ((StringField)  formContext.getField(FORM_FIELD_NAME))   .getAsString();
            boolean isSystem   = ((BooleanField) formContext.getField(FORM_FIELD_SYSTEM)) .getAsBoolean(); 
            List    theMembers = ((SelectField)  formContext.getField(FORM_FIELD_MEMBERS)).getSelection();
            
            theGroup.setName(theName);
            theGroup.setIsSystem(isSystem);
            
			boolean i18nSaved = false;
			try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
				for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
					String theI18NName =
						((StringField) formContext.getField(getLanguageFieldName(locale))).getAsString();
					String theI18NDesc =
						((StringField) formContext.getField(getDescriptionFieldName(locale))).getAsString();
					i18nSaved |= saveI18N(tx, theGroup, theI18NName, theI18NDesc, locale);
				}

				tx.commit();
            }

            if (i18nSaved) {
				theGroup.touch();
            }
            
            Collection theOldMembers = theGroup.getMembers(false); 
            
            Collection theRemoved = new ArrayList(theOldMembers);
            theRemoved.removeAll(theMembers);

            for (Iterator theIt = theRemoved.iterator(); theIt.hasNext();) {
                Person thePerson = (Person) theIt.next();
                theGroup.removeMember(thePerson);
            }
            
            Collection theAdded = new ArrayList(theMembers);
            theAdded.removeAll(theOldMembers);
            
            for (Iterator theIt = theAdded.iterator(); theIt.hasNext();) {
                Person thePerson = (Person) theIt.next();
                theGroup.addMember(thePerson);
            }
            
            return true;
        }
    }

    public static class DeleteGroupCommandHandler extends AbstractDeleteCommandHandler {
        
        public static final String COMMAND_ID = "deleteDGroup";
        
		public DeleteGroupCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

		@Override
		protected void deleteObject(LayoutComponent component, Object model, Map<String, Object> arguments) {
			checkGroup(model);
			((Group) model).tDelete();
        }

		@Override
		protected void deleteObjects(LayoutComponent component, Iterable<?> elements, Map<String, Object> arguments) {
			elements.forEach(this::checkGroup);
			@SuppressWarnings("unchecked")
			Iterable<? extends TLObject> groups = (Iterable<? extends TLObject>) elements;
			KBUtils.deleteAll(groups);
		}

		private void checkGroup(Object model) {
			if (!(model instanceof Group)) {
				throw new TopLogicException(I18NConstants.ERROR_NOT_A_GROUP__ELEMENT.fill(model));
            }
		}

        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
            return CombinedExecutabilityRule.combine(super.createExecutabilityRule(), GroupDeleteRule.INSTANCE);
        }
    }

    public static class GroupDeleteRule implements ExecutabilityRule {
        
        public static final ExecutabilityRule INSTANCE = new GroupDeleteRule();
        
        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
			Group theGroup = (Group) model;

			if (theGroup == null || theGroup.isSystem()) {
                return ExecutableState.createDisabledState(I18NConstants.ERROR_SYSTEM_GROUP_CANNOT_BE_DELETED);
            }

            return ExecutableState.EXECUTABLE;
        }
    }
    
    public static class NewGroupCommandHandler extends AbstractCreateCommandHandler {

        // Constants
    	
        /** The ID of this command. */
        public static final String COMMAND_ID = "newDGroup";

        
        
		public NewGroupCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
		}

        @Override
		public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
				Map<String, Object> arguments) {
            String      theName   = (String)      formContainer.getField(FORM_FIELD_NAME).getValue();
            Boolean     isSystem  = (Boolean)     formContainer.getField(FORM_FIELD_SYSTEM).getValue();
            
            Group theGroup = this.createGroup((BoundObject) createContext, theName, isSystem.booleanValue());
            
			try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
				for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
					String theI18NName =
						((StringField) formContainer.getField(getLanguageFieldName(locale))).getAsString();
					String theI18NDesc =
						((StringField) formContainer.getField(getDescriptionFieldName(locale))).getAsString();
					saveI18N(tx, theGroup, theI18NName, theI18NDesc, locale);
				}

				tx.commit();
			}
            
            return theGroup;
        }

        /**
         * @param    aMaster     The master to bind the created group to, may be <code>null</code>.
         * @param    aName       The name of the new group, must not be empty or <code>null</code>.
         * @param    isSystem    Flag, if the group is a system group.
         * @return   The new created group.
         * @throws   IllegalArgumentException    If given name is empty or <code>null</code>.
         */
		public Group createGroup(BoundObject aMaster, String aName, boolean isSystem) {
            if (StringServices.isEmpty(aName)) {
                throw new IllegalArgumentException("No name defined for new group!");
            }

            Group theGroup = Group.createGroup(aName);

            theGroup.setIsSystem(isSystem);
            
            if (aMaster != null) {
                theGroup.bind(aMaster);
            }

            return (theGroup);
        }
    }

    
    public static class NewGroupComponent extends AbstractCreateComponent {
        
        public static final String GROUP_MASTER = "groupMaster";
        
		public interface Config extends AbstractCreateComponent.Config {

			@Override
			@FormattedDefault("null()")
			public ModelSpec getModelSpec();

			@StringDefault(NewGroupCommandHandler.COMMAND_ID)
			@Override
			String getCreateHandler();

		}

		public NewGroupComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
            super(context, someAttrs);
        }
        
        public BoundObject getBOMaster() {
			return (BoundObject) getModel();
        }
        
        @Override
		protected boolean supportsInternalModel(Object anObject) {
			return anObject == null || anObject instanceof BoundObject;
		}

		@Override
		protected void becomingVisible() {
			super.becomingVisible();
			LayoutComponent theParent = this.getDialogParent();
			BoundObject boMaster;
			if (theParent instanceof EditGroupComponent) {
				boMaster = ((EditGroupComponent) theParent).getBindingObject();
			}
			else {
				boMaster = null;
			}
			setModel(boMaster);
        }

        @Override
		public FormContext createFormContext() {
            LayoutComponent theParent = this.getDialogParent();
            
			FormContext theFC = new FormContext("form", getResPrefix());
            StringField theNameField = FormFactory.newStringField(FORM_FIELD_NAME, "", true, false, new UniqueGroupNameConstraint());
            theFC.addMember(theNameField);
            
            BooleanField theSystemField = FormFactory.newBooleanField(FORM_FIELD_SYSTEM, Boolean.TRUE, false);
            theSystemField.setMandatory(true);
            theFC.addMember(theSystemField);
			EditGroupComponent r = ((EditGroupComponent)theParent);

			for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
				StringField theField = FormFactory.newStringField(getLanguageFieldName(locale), "", false);
                theFC.addMember(theField);
                
				theField = FormFactory.newStringField(getDescriptionFieldName(locale), "", false);
                theFC.addMember(theField);
            }

            return theFC;
        }
    }
    
    public static class UniqueGroupNameConstraint implements Constraint {
    
        private Pattern pattern = Pattern.compile("^\\w+$");
        /**
         * @see com.top_logic.layout.form.Constraint#check(java.lang.Object)
         */
        @Override
		public boolean check(Object aValue) throws CheckException {
			{
				// explicit check all Group names including representative groups
				List theGroups = Group.getAll(PersistencyLayer.getKnowledgeBase(), FilterFactory.trueFilter());
                Set  theNames  = new HashSet(theGroups.size());
                for (int i=0; i<theGroups.size(); i++) {
                    theNames.add(((Group) theGroups.get(i)).getName());
                }
                
                if (!theNames.contains(aValue) && pattern.matcher((String) aValue).matches()) {
                    return true;
                }
                else {
					throw new CheckException(Resources.getInstance().getString(i18N_INVALID_GROUP_NAME));
                }
            }
        }
    }
}
