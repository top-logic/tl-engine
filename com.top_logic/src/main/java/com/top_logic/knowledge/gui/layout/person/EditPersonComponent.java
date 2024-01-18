/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.top_logic.base.bus.UserEvent;
import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.Sender;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.knowledge.wrap.person.infouser.InfoUserConstraint;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.admin.component.ThemeLabelProvider;
import com.top_logic.layout.form.ChangeStateListener;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.constraints.AtMostOneFilledFieldDependency;
import com.top_logic.layout.form.constraints.SelectionSizeConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.gui.GroupComparator;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Component to edit persons.
 *
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class EditPersonComponent extends EditComponent {

	/** Name of the {@link FormField} containing the {@link Person}'s assigned groups. */
	public static final String GROUPS_FIELD_NAME = "groups";

	/** {@link Person}'s language parameter */
    public static final String LANGUAGE = "language";

	/** {@link Person}'s country parameter */
    public static final String COUNTRY = "country";

	/** {@link Person}'s time zone parameter */
	public static final String TIME_ZONE = "timeZone";

	/** {@link Person}'s super user flag. */
	public static final String SUPER_USER_FIELD = "superuser";

    protected static final Sender USER_EVENT_SENDER =
        new Sender(Bus.CHANGES, Bus.USER);

	/**
	 * ID of the {@link SelectField}, which holds all selectable user {@link Theme themes}
	 */
	public static final String THEME_SELECTOR = "themeSelector";

    /**
     * The set of groups which are allowed to change person names.<br/>
     * Empty list means all users are allowed to change person names.
     */
    protected static Set allowedGroups;
    
	private final MOStructureImpl _userMO;

	/**
	 * Configuration options for {@link EditPersonComponent}.
	 */
	public interface Config extends EditComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@StringDefault(ApplyPersonCommandHandler.COMMAND_ID)
		String getApplyCommand();

		@Override
		@StringDefault(DeletePersonCommandHandler.COMMAND_ID)
		String getDeleteCommand();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			registry.registerButton(ResetPersonalConfiguration.COMMAND_ID);
			EditComponent.Config.super.modifyIntrinsicCommands(registry);
		}

	}

    /**
	 * Creates a new {@link EditPersonComponent} from the given configuration.
	 */
	public EditPersonComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		MOStructureImpl userMetaObject;
		try {
			userMetaObject = (MOStructureImpl) PersistencyLayer.getKnowledgeBase().getMORepository()
				.getMetaObject(Person.OBJECT_NAME);
		} catch (UnknownTypeException ex) {
			context.error("Unable to get meta object for user data.", ex);
			userMetaObject = null;
		}
		_userMO = userMetaObject;
    }

    /**
     * Creates the set of groups which are allowed to change person names.
     */
    private static final Set createAllowedGroups() {
        HashSet theGroups = new HashSet();
		{
            IterableConfiguration theConf = Configuration.getConfiguration(PersonManager.class);
            String theGroupsString = theConf.getValue("editPersonGroups");
            String[] theGroupList = (theGroupsString == null ? null : StringServices.toArray(theGroupsString));

            if (theGroupList != null)
            for (int i = 0; i < theGroupList.length; i++) {
                Group theGroup = Group.getGroupByName(theGroupList[i]);
                if (theGroup == null) {
                    Logger.error("EditPersonComponent: Failed to get group with name '" + theGroupList[i] + "'.", EditPersonComponent.class);
                }
                else {
                    theGroups.add(theGroup);
                }
            }
        }
        return Collections.unmodifiableSet(theGroups);
    }

    /**
     * Gets the set of groups which are allowed to change person names.
     */
    public Set getAllowedGroups() {
        if (allowedGroups == null) {
            allowedGroups = createAllowedGroups();
        }
        return allowedGroups;
    }

	/**
	 * Only alive persons are supported.
	 */
	@Override
	protected boolean supportsInternalModel(Object anObject) {
		if (anObject == null) {
			return true;
		}
		if (!(anObject instanceof Person)) {
			return false;
		}
		Person person = (Person) anObject;
		if (!person.isAlive()) {
			return person.isAlive();
		}
		return super.supportsInternalModel(anObject);
	}

    /**
     * Overridden to disable GoTo-links to non-alive persons.
     *
     * @see com.top_logic.tool.boundsec.BoundComponent#allow(com.top_logic.tool.boundsec.BoundObject)
     */
    @Override
	public boolean allow(BoundObject aObject) {
        if (!supportsInternalModel(aObject)) {
            return false;
        }
        return super.allow(aObject);
    }

    @Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(TLContext.getContext().getCurrentPersonWrapper());
		}
		return super.validateModel(context);
    }

    @Override
	public FormContext createFormContext() {
        FormContext formContext = new FormContext("PersonFormContext", getResPrefix());
		Locale currentUserLocale = TLContext.getLocale();

        // Create the necessary constraints
        SelectionSizeConstraint constraintSelectOne = new SelectionSizeConstraint(1, 1);

		Person account = (Person) this.getModel();
		Locale theLocale = getLocale(account);

		List<String> languages = Arrays.asList(ResourcesModule.getInstance().getSupportedLocaleNames());
        SelectField languageField = FormFactory.newSelectField(EditPersonComponent.LANGUAGE, languages, false, true, false, constraintSelectOne);
		languageField.setOptionComparator(new LanguageComparator(currentUserLocale));
		languageField.setOptionLabelProvider(new LanguageLabelProvider(currentUserLocale));
		languageField.enableOptionsBasedSelectionConstraint();
		languageField.setAsSingleSelection(theLocale.getLanguage());
		formContext.addMember(languageField);

		List<String> countries = Arrays.asList(Locale.getISOCountries());
        SelectField countryField = FormFactory.newSelectField(EditPersonComponent.COUNTRY, countries, false, true, false, constraintSelectOne);
		countryField.setOptionComparator(new CountryComparator(currentUserLocale));
		countryField.setOptionLabelProvider(new CountryLabelProvider(currentUserLocale));
		countryField.enableOptionsBasedSelectionConstraint();
		countryField.setAsSingleSelection(theLocale.getCountry());
		formContext.addMember(countryField);

		formContext.addMember(createTimezoneField(account, constraintSelectOne));
		// Note: Only super users can change the super user flag. A super-user cannot remove itself
		// the super user flag to prevent to delete the last super user.
		boolean cannotChangeSuperUserFlag =
			!TLContext.isAdmin() || account == TLContext.getContext().getCurrentPersonWrapper()
				|| noDataStorage(account);

		// TODO BHU, KHA, KBU: this code might never be used at all, the
		// creation of new users is done in a dialog
        
        // compare to PersonAttributes.
		BooleanField restrictedUser;
		if (account == null) {
            //this is for create new user, null-selection in list is not expected

			int theSize = getSizeForMOAttribute(_userMO, Person.NAME, false);
            formContext.addMember(FormFactory.newStringField(UserInterface.USER_NAME, true, false, new StringLengthConstraint(1, theSize)));

			theSize = getSizeForMOAttribute(_userMO, UserInterface.NAME, false);
            formContext.addMember(FormFactory.newStringField(UserInterface.NAME, false, false, new StringLengthConstraint(1, theSize)));

			theSize = getSizeForMOAttribute(_userMO, UserInterface.TITLE, false);
            formContext.addMember(FormFactory.newStringField(UserInterface.TITLE, false, false, new StringLengthConstraint(0, theSize)));

			theSize = getSizeForMOAttribute(_userMO, UserInterface.FIRST_NAME, false);
            formContext.addMember(FormFactory.newStringField(UserInterface.FIRST_NAME, false, false, new StringLengthConstraint(0, theSize)));

			theSize = getSizeForMOAttribute(_userMO, UserInterface.PHONE, false);
            formContext.addMember(FormFactory.newStringField(UserInterface.PHONE, false, false, new StringLengthConstraint(0, theSize)));

			theSize = getSizeForMOAttribute(_userMO, UserInterface.EMAIL, false);
            formContext.addMember(FormFactory.newStringField(UserInterface.EMAIL, false, false, new StringLengthConstraint(0, theSize)));

			restrictedUser = FormFactory.newBooleanField(Person.RESTRICTED_USER, Boolean.FALSE, false);
			formContext.addMember(restrictedUser);
        }
        else { // model != null
            boolean allowedToEdit = false;
			allowedToEdit = getAllowedGroups().isEmpty()
				|| Group.isMemberOfAnyGroup(TLContext.getContext().getCurrentPersonWrapper(), getAllowedGroups());

			{
				String authenticationDeviceID = account.getAuthenticationDeviceID();

				Set<String> allDataDevices = TLSecurityDeviceManager.getInstance().getConfiguredDataAccessDeviceIDs();
                ArrayList<String> options = new ArrayList<>(allDataDevices);

                Set<String> allAuthDevices   = TLSecurityDeviceManager.getInstance().getConfiguredAuthenticationDeviceIDs();
				options = new ArrayList<>(allAuthDevices);
				if (!StringServices.isEmpty(authenticationDeviceID) && !allAuthDevices.contains(authenticationDeviceID)) {
					options.add(0, authenticationDeviceID);
				}
				SelectField authDevicesField = FormFactory.newSelectField(Person.AUTHENTICATION_DEVICE_ID, options, false, false, false, null);
				if (!StringServices.isEmpty(authenticationDeviceID))
					authDevicesField.initSingleSelection(authenticationDeviceID);

				UserInterface user = account.getUser();
				if (user != null) {
					boolean deviceReadonly = noDataStorage(account);
					// if the device is read only no constraints need to be set, because the
					// following
					// fields will be disabled anyway.
					if (deviceReadonly) {
						formContext.addMember(givenNameField(user, deviceReadonly));
						formContext.addMember(titleField(user, deviceReadonly));
						formContext.addMember(surnameField(user, deviceReadonly));
						formContext.addMember(newPersonField(UserInterface.PHONE, user.getPhone(),
							deviceReadonly));
						formContext.addMember(newPersonField(UserInterface.EMAIL, user.getEMail(),
							deviceReadonly));
					} else {
						int givenNameSize = getSizeForMOAttribute(_userMO, UserInterface.NAME, deviceReadonly);
						StringField givenNameField =
							newSizedFormConstraint(UserInterface.NAME, !allowedToEdit || deviceReadonly, 0,
								givenNameSize, user.getFirstName());
						givenNameField.setMandatory(true);
						formContext.addMember(givenNameField);
						int tileSize = getSizeForMOAttribute(_userMO, UserInterface.TITLE, deviceReadonly);
						formContext.addMember(newSizedFormConstraint(UserInterface.TITLE,
							!allowedToEdit || deviceReadonly, 0, tileSize, user.getTitle()));
						StringField surNameField =
							newSizedFormConstraint(UserInterface.NAME,
								!allowedToEdit || deviceReadonly, 1, getSizeForMOAttribute(_userMO,
									UserInterface.NAME, deviceReadonly),
								user.getName());
						surNameField.setMandatory(true);
						formContext.addMember(surNameField);
						formContext.addMember(newSizedFormConstraint(UserInterface.PHONE, deviceReadonly, 0,
							getSizeForMOAttribute(_userMO, UserInterface.PHONE, deviceReadonly),
							user.getPhone()));
						formContext.addMember(newSizedFormConstraint(UserInterface.EMAIL, deviceReadonly, 0,
							getSizeForMOAttribute(_userMO, UserInterface.EMAIL, deviceReadonly),
							user.getEMail()));
					}
				}
                formContext.addMember(authDevicesField);

				restrictedUser = createRestrictedUserField(account);
				formContext.addMember(restrictedUser);

                if(ThreadContext.isAdmin()){
					authDevicesField.setDisabled(!BoundHelper.getInstance().isAllowChangeDeleteProtection());
                } else {
                    authDevicesField.setVisible(false);
                }

				// Retrieve all available themes
				ThemeFactory themeFactory = ThemeFactory.getInstance();
				List<Theme> themes = new ArrayList<>(themeFactory.getChoosableThemes());

				String currentThemeId;
				Person currentPerson = TLContext.getContext().getCurrentPersonWrapper();
				if (Utils.equals(account, currentPerson)) {
					PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
					currentThemeId = MultiThemeFactory.getPersonalThemeId(pc);
				} else {
					PersonalConfigurationWrapper pcw = PersonalConfigurationWrapper.getPersonalConfiguration(account);
					currentThemeId = pcw == null ? null : MultiThemeFactory.getPersonalThemeId(pcw);
				}
				Theme currentTheme;
				if (currentThemeId == null) {
					currentTheme = themeFactory.getDefaultTheme();
				} else {
					currentTheme = themeFactory.getTheme(currentThemeId);
					if (currentTheme == null) {
						// Theme no longer available.
						currentTheme = themeFactory.getDefaultTheme();
					}
				}

				Collections.sort(themes, LabelComparator.newCachingInstance(ThemeLabelProvider.INSTANCE));

				SelectField themeField = FormFactory.newSelectField(THEME_SELECTOR, themes, !MULTI_SELECT,
					Collections.singletonList(currentTheme), MANDATORY, !IMMUTABLE, null);

				themeField.setLabel(getResString("themeSelectorLabel"));

				formContext.addMember(themeField);
				formContext.addMember(createGroupsField(account));
            }
        }

		BooleanField superUser =
			FormFactory.newBooleanField(SUPER_USER_FIELD, Person.isAdmin(account), cannotChangeSuperUserFlag);
		formContext.addMember(superUser);

		AtMostOneFilledFieldDependency dependency = new AtMostOneFilledFieldDependency(restrictedUser, superUser);
		dependency.attach();

        return formContext;
    }

	private static boolean noDataStorage(Person account) {
		AuthenticationDevice device = account.getAuthenticationDevice();
		if (device == null) {
			return true;
		}
		return !device.allowPwdChange();
	}

	private SelectField createGroupsField(Person person) {
		ArrayList<Group> groups = new ArrayList<>(Group.getGroups(person));
		SelectField field =
			FormFactory.newSelectField(GROUPS_FIELD_NAME, groups, FormFactory.MULTIPLE, groups, FormFactory.IMMUTABLE);
		SelectFieldUtils.setOptionComparator(field, new GroupComparator(true));
		return field;
	}

	private FormField createTimezoneField(Person editedPerson, SelectionSizeConstraint constraintSelectOne) {
		if (TimeZones.Module.INSTANCE.isActive()) {
			TimeZone currentTimeZone = getTimeZone(editedPerson);
			SelectField timeZoneField =
				FormFactory.newSelectField(EditPersonComponent.TIME_ZONE, TimeZones.getInstance().getTimeZones(),
					!FormFactory.MULTIPLE, FormFactory.MANDATORY, !FormFactory.IMMUTABLE, constraintSelectOne);

			timeZoneField.enableOptionsBasedSelectionConstraint();
			timeZoneField.setAsSingleSelection(currentTimeZone);
			return timeZoneField;
		} else {
			String userTimeZone = MetaLabelProvider.INSTANCE.getLabel(TimeZones.defaultUserTimeZone());
			return FormFactory.newStringField(EditPersonComponent.TIME_ZONE, userTimeZone, FormFactory.IMMUTABLE);
		}
	}

	private BooleanField createRestrictedUserField(Person person) {
		Boolean isRestrictedUser = person.isRestrictedUser();
		boolean restrictedFieldImmutable =
			!hasDeleteAccess(person) || person.equals(TLContext.currentUser())
				|| person.equals(PersonManager.getManager().getRoot());
		BooleanField restrictedUserField =
			FormFactory.newBooleanField(Person.RESTRICTED_USER, isRestrictedUser, restrictedFieldImmutable);
		addMaxFullUserCheck(restrictedUserField);
		return restrictedUserField;
	}

	private void addMaxFullUserCheck(BooleanField restrictedUserField) {
		restrictedUserField.addListener(FormField.IS_CHANGED_PROPERTY, new ChangeStateListener() {
			@Override
			public Bubble handleChangeStateChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				FormField field = (FormField) sender;
				String userName = (String) field.getParent().getField(UserInterface.USER_NAME).getValue();
				Boolean restrictedUser = Person.byName(userName).isRestrictedUser();
				if (restrictedUser.equals(field.getValue())) {
					field.removeConstraint(InfoUserConstraint.INSTANCE);
				} else {
					field.addConstraint(InfoUserConstraint.INSTANCE);
				}
				return Bubble.BUBBLE;
			}
		});
	}

	private static StringField newSizedFormConstraint(String aName, boolean isDisabled, int aMinSize, int aMaxSize,
			String aRawValue) {
		StringLengthConstraint constraint = new StringLengthConstraint(aMinSize, aMaxSize);
		StringField result = FormFactory.newStringField(aName, aRawValue, false, false, constraint);
		result.setDisabled(isDisabled);
		return result;
	}

	private StringField newPersonField(String fieldName, String initialValue, boolean disabled) {
		StringField field = FormFactory.newStringField(fieldName, initialValue, false);
		field.setDisabled(disabled);
		return field;
	}

	private StringField surnameField(UserInterface user, boolean deviceReadonly) {
		StringField field = newPersonField(UserInterface.NAME, user.getName(), deviceReadonly);
		field.setMandatory(true);
		return field;
	}

	private StringField userNameField(Person person, MOStructureImpl type, boolean deviceReadonly) {
		int maximumSize = getSizeForMOAttribute(type, UserInterface.USER_NAME, deviceReadonly);
		StringField userNameField =
			FormFactory.newStringField(UserInterface.USER_NAME, person.getName(), FormFactory.MANDATORY,
				FormFactory.IMMUTABLE, new StringLengthConstraint(1, maximumSize));
		return userNameField;
	}

	private StringField titleField(UserInterface user, boolean deviceReadonly) {
		StringField field = FormFactory.newStringField(UserInterface.TITLE, user.getTitle(), false);
		field.setDisabled(deviceReadonly);
		return field;
	}

	private StringField givenNameField(UserInterface user, boolean deviceReadonly) {
		StringField field =
			FormFactory.newStringField(UserInterface.FIRST_NAME, user.getFirstName(), false);
		field.setDisabled(deviceReadonly);
		field.setMandatory(true);
		return field;
	}

	private Locale getLocale(Person person) {
		Locale locale;
		if (person != null) {
			locale = person.getLocale();
			if (locale == null) {
				locale = ResourcesModule.getInstance().getDefaultLocale();
			}
		} else {
			locale = ResourcesModule.getInstance().getDefaultLocale();
		}
		return locale;
	}

	private TimeZone getTimeZone(Person person) {
		TimeZone timeZone;
		if (person != null) {
			timeZone = person.getTimeZone();
			if (timeZone == null) {
				timeZone = TimeZones.defaultUserTimeZone();
			}
		} else {
			timeZone = TimeZones.defaultUserTimeZone();
		}
		return timeZone;
	}

    /** 
     * the database size for the given attribute or -1 if something goes wrong
	 */
	public static int getSizeForMOAttribute(MOStructureImpl aMO, String anAttributeName, boolean useMaximum) {
		if (useMaximum) {
			return 4096;
		}
		try {
			MOAttributeImpl theAttribute = (MOAttributeImpl) aMO.getAttribute(anAttributeName);
			return theAttribute.getSQLSize();
		} catch (NoSuchAttributeException e) {
			Logger.error("Failed to get attribute '" + anAttributeName + "' from MOStructure '" + aMO.getName() +"'.", e, EditPersonComponent.class);
		}
		return -1;
	}

    /**
	 * Send information to the application bus, that something in the given user has changed.
	 *
	 * The type describes the kind of change, whereas the given data object describes the changed
	 * object.
	 *
	 * @param aType
	 *        The kind of change fired as {@link UserEvent}
	 */
    protected void notifyBus(String aType) {
        Date                  now = new Date();
		UserEvent theEvent =
			new UserEvent(USER_EVENT_SENDER, (Person) this.getModel(), TLContext.currentUser(), now, aType);

        USER_EVENT_SENDER.send(theEvent);
    }

	/**
	 * Determines whether the current user has the right to delete the given {@link Person}.
	 * 
	 * @param aPerson
	 *            the person in question
	 * @return whether the user can delete <code>aPerson</code>
	 */
	boolean hasDeleteAccess(Person aPerson) {
		String theDelString = this.getDeleteCommandHandlerName();
		CommandHandler theDelete = (theDelString != null) ? CommandHandlerFactory.getInstance().getHandler(theDelString) : null;
		boolean hasChangeAccess = (theDelete != null) && this.allow(theDelete.getCommandGroup(), aPerson);
		return hasChangeAccess;
	}

	private static final class LanguageLabelProvider implements LabelProvider {

		private final Locale _locale;

		public LanguageLabelProvider(Locale locale) {
			_locale = locale;
		}

		@Override
		public String getLabel(Object aObject) {
			String language = (String) aObject;
			Locale languageLocale = new Locale(language);
			return languageLocale.getDisplayLanguage(_locale);
		}
	}

	private static final class CountryLabelProvider implements LabelProvider {

		private final Locale _locale;

		public CountryLabelProvider(Locale locale) {
			_locale = locale;
		}

		@Override
		public String getLabel(Object aObject) {
			String country = (String) aObject;
			Locale countryLocal = new Locale(country, country);
			return countryLocal.getDisplayCountry(_locale);
		}
	}

	/** Helper class to sortobjects by local */
	public abstract static class TextBasedComparator<T> implements Comparator<T> {

		/** The {@link Locale} to show text for */
		protected final Locale _locale;
        /** The collator for the final comparison */
        protected final Collator base;

		public TextBasedComparator(Locale locale) {
			this._locale = locale;
            this.base   = Collator.getInstance(this._locale);
        }

        /**
		 * Compare to objects according to their translated names.
		 */
        @Override
		public int compare(T arg0, T arg1) {
            String txt0 = getText(arg0, _locale);
            String txt1 = getText(arg1, _locale);

            return base.compare(txt0, txt1);
        }

        /**
		 * Returns the text to compare. The text must be according to the given {@link Locale}.
		 * 
		 * @param locale
		 *        The locale to create text for.
		 */
		protected abstract String getText(T aCurrent, Locale locale);
	}

	/** Helper class to sort the locales by Language */
	public static class LanguageComparator extends TextBasedComparator<String> {

		/**
		 * Creates a new {@link LanguageComparator}.
		 * 
		 * @param locale
		 *        The {@link Locale} to use for comparison.
		 */
		public LanguageComparator(Locale locale) {
			super(locale);
			// singleton instance
		}

		@Override
		protected String getText(String aCurrent, Locale locale) {
			Locale languageLocale = new Locale(aCurrent);
            return languageLocale.getDisplayLanguage(locale);
		}

	}

    /** Helper class to sort the Locales by Language */
	public static class CountryComparator extends TextBasedComparator<String> {

		/**
		 * Creates a new {@link CountryComparator}.
		 * 
		 * @param locale
		 *        The {@link Locale} to use for comparison.
		 */
		public CountryComparator(Locale locale) {
			super(locale);
			// singleton instance
		}

        /**
         * This is name of the languages for the Current User
         */
        @Override
		protected String getText(String aCurrent, Locale locale) {
			String theCountry = aCurrent;
            Locale countryLocal = new Locale(theCountry, theCountry);
            return countryLocal.getDisplayCountry(locale);
        }
    }

    private class EqualsConstraint implements Constraint {

        FormField relatedField;

        public EqualsConstraint(FormField aFormField) {
            this.relatedField = aFormField;
        }

        @Override
		public boolean check(Object aValue) throws CheckException {
            if (this.relatedField.getValue().equals(aValue)) {
                return true;
            }

			throw new CheckException(Resources.getInstance().getString(
				EditPersonComponent.this.getResPrefix().key("pwdNotEqual")));
        }

        @Override
		public Collection<FormField> reportDependencies() {
            return Collections.singletonList(this.relatedField);
        }

    }

	public static final class WriteableSecurityDeviceRule implements ExecutabilityRule {

		/** Singleton {@link WriteableSecurityDeviceRule} instance. */
		public static final WriteableSecurityDeviceRule INSTANCE = new WriteableSecurityDeviceRule();

		private WriteableSecurityDeviceRule() {
			// singleton instance
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			Set<?> writeableDecives = TLSecurityDeviceManager.getInstance().getWritableSecurityDeviceIDs();
			if (writeableDecives.isEmpty()) {
				return ExecutableState.createDisabledState(I18NConstants.NO_WRITEABLE_SECURITY_DEVICE);
			}
			return ExecutableState.EXECUTABLE;
		}

	}

}
