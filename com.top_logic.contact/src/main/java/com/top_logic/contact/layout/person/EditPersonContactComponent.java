/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.LazyListUnmodifyable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.layout.AbstractEditContactComponent;
import com.top_logic.contact.mandatoraware.COSPersonContact;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.util.PersonComparator;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.treetable.component.ConstantFieldProvider;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Edit component to change PersonContacts, to create and delete PersonContacts.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class EditPersonContactComponent extends AbstractEditContactComponent {

	public static final String PARAM_REPRESENTATIVES = "paramRepresentatives";

    @Deprecated
	public static final String PARAM_CO_WORKER = "paramCoWorker";

    public static final String PARAM_REPRESENTATIVES_FOR = "paramRepresFor";

    public static final String FIELD_PERSON = "person";

	/**
	 * Configuration of the {@link EditPersonContactComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractEditContactComponent.Config {

		@Override
		@StringDefault(PersonContactApplyHandler.COMMAND_ID)
		String getApplyCommand();

		@Override
		@StringDefault(PersonContactDeleteCommandHandler.COMMAND_ID)
		String getDeleteCommand();

		@Override
		@BooleanDefault(true)
		boolean getShowNoModel();

	}

    /**
     * Creates a new instance of this class.
     */
    public EditPersonContactComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
    }

	@Override
	protected boolean supportsInternalModelNonNull(Object anObject) {
		if (anObject instanceof PersonContact) {
			return true;
		}
		return super.supportsInternalModelNonNull(anObject);
	}

    @Override
	protected String getMetaElementName() {
        return (PersonContact.META_ELEMENT);
    }

	@Override
	public List<String> getExcludeList() {
		List<String> excludeList = super.getExcludeList();
		excludeList.add(PersonContact.FULLNAME);
		return excludeList;
	}

    /**
     * Special handling when PersonContact refers to a Person.
     * 
	 * @see com.top_logic.element.meta.form.component.EditAttributedComponent#createFormContext()
     * 
	 * If the current PersonContact is related to a person, then disable all input fields for
	 * Attributes which values are supposed to be retrieved from the according person and therefore
	 * should not be editable
	 */
	@Override
	public synchronized FormContext createFormContext() {
		FormContext theContext = super.createFormContext();
		try{
    		PersonContact theContact = (PersonContact) this.getModel();

    		if (theContact != null && theContact.tValid()) {
    		    Person thePerson = theContact.getPerson();
    		    theContext.addMember(ConstantFieldProvider.INSTANCE.createField(FIELD_PERSON, thePerson));
                // Disable only when Person wasAlive when when last checked
				if (thePerson != null && thePerson.tValid()) {
					TLClass theMetaElement = theContact.tType();
    
                    disableAttribute(theContext, PersonContact.FIRST_NAME    , theContact, theMetaElement);
                    disableAttribute(theContext, PersonContact.NAME_ATTRIBUTE   , theContact, theMetaElement);
                    disableAttribute(theContext, PersonContact.EMAIL         , theContact, theMetaElement);
                    disableAttribute(theContext, PersonContact.TITLE        , theContact, theMetaElement);
                    disableAttribute(theContext, PersonContact.PHONE_MOBILE , theContact, theMetaElement);
                    disableAttribute(theContext, PersonContact.PHONE , theContact, theMetaElement);
                    disableAttribute(theContext, PersonContact.PHONE_PRIVATE, theContact, theMetaElement);
                    
                    Group theRepresentativeGroup = thePerson.getRepresentativeGroup();
                    if (theRepresentativeGroup != null) {
                    	List thePersons = new LazyListUnmodifyable() {
                            @Override
							protected List initInstance() {
								// No need to create sorted list, because field gets comparator.
								return PersonManager.getManager().getAllPersonsList();
                            }
                        };

                        SelectField theRepresentativeField = FormFactory.newSelectField(PARAM_REPRESENTATIVES, thePersons, true, false);
						theRepresentativeField.setOptionComparator(PersonComparator.getInstance());
                    	theRepresentativeField.setAsSelection(new ArrayList(theRepresentativeGroup.getMembers()));
                    	theContext.addMember(theRepresentativeField);
                    }

					TLClass metaElement = ContactFactory.getInstance().getMetaElement(PersonContact.META_ELEMENT);
					TableConfigurationProvider tableConfigProvider =
						GenericTableConfigurationProvider.getTableConfigurationProvider(metaElement);

					List theList = this.getCoWorkerList(theContact);
					SelectField coWorkerField =
						FormFactory.newSelectField(PARAM_CO_WORKER, theList, true, theList, true);
					coWorkerField.setTableConfigurationProvider(tableConfigProvider);
					theContext.addMember(coWorkerField);

                    theList = this.getPeopleThisOneIsRepresentativeFor(theContact);
					SelectField representativeForField =
						FormFactory.newSelectField(PARAM_REPRESENTATIVES_FOR, theList, true, theList, true);
					representativeForField.setTableConfigurationProvider(tableConfigProvider);
					theContext.addMember(representativeForField);
                }
    		}
		}
		catch(Exception e) {
			Logger.error("Unable to disable fields for attributes which values were retrieved from the respective person",e, this);
		}

		return theContext;
	}

    /** 
     * Disable a Constraint in aContext specified by anAttrName.
     */
    protected void disableAttribute(FormContext aContext, String anAttrName, Wrapper anAttributed, TLClass aMeta) throws NoSuchAttributeException {
		TLStructuredTypePart theAttr = MetaElementUtil.getMetaAttribute(aMeta, anAttrName);
		String theID = MetaAttributeGUIHelper.getAttributeID(theAttr, anAttributed);

        aContext.getField(theID).setImmutable(true);
    }

    /** 
     * Return the list of person contacts working for the given person contact.
     * 
     * @param    aModel    The person contact to get the workers for, may be <code>null</code>. 
     * @return   The list of workers ({@link PersonContact}), never <code>null</code>.
     * @deprecated method will be replaced by calculated attribute
     */
    @Deprecated
    public List getCoWorkerList(Object aModel) {
        List theResult = new ArrayList();

        if (aModel instanceof PersonContact) {
			try {
				TLClass   theME = MetaElementFactory.getInstance().getGlobalMetaElement(ContactFactory.STRUCTURE_NAME, this.getMetaElementName());
				TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theME, PersonContact.BOSS);

				theResult.addAll(AttributeOperations.getReferers((PersonContact) aModel, theMA));
			}
            catch (Exception ex) {
				Logger.error("Unable to retrieve employees from person contact " + aModel , ex, this);
			}
		}

        return theResult;
    }

    /** 
     * Return the list of person contacts the given person contact is a representative for.
     * 
     * @param    aModel    The person contact to get the representatives for, may be <code>null</code>. 
     * @return   The list of contacts the given one is representative for, never <code>null</code>.
     */
    public List getPeopleThisOneIsRepresentativeFor(Object aModel) {
        List theResult = new ArrayList();

        if (aModel instanceof PersonContact) {
            try {
                Person thePerson = ((PersonContact) aModel).getPerson();

                if (thePerson != null) {
                    theResult = COSPersonContact.getRepresentations(thePerson);
                }
            }
            catch (Exception ex) {
                Logger.error("Unable to retrieve person contacts for which the selected one is a representative " + aModel , ex, this);
            }
        }
        return theResult;
    }
}
