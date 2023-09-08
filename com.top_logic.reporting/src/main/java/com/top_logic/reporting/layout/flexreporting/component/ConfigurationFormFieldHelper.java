/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.component;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationAccess;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationResolver;
import com.top_logic.basic.config.DefaultConfigurationResolver;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.AbstractDispatchingResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public class ConfigurationFormFieldHelper {

	public static final Property<ConfigurationDescriptor> CONFIG_DESCRIPTOR =
		TypedAnnotatable.property(ConfigurationDescriptor.class, "configDescriptior");

	public static final Property<PropertyDescriptor> PROPERTY_DESCRIPTOR =
		TypedAnnotatable.property(PropertyDescriptor.class, "propertyDescriptior");

    private ConfigurationResolver resolver = new DefaultConfigurationResolver();

    public static final String TABLE_PREFIX     = "table_";
    public static final String TABLE_ROW_PREFIX = "row_";
    public static final String REMOVE_TABLE_ROW = "removeRow";
    public static final String GROUP_CONTAINER  = "groupContainer";
    public static final String SELECT_FIELD     = "select";

	private int idCount;

    /**
	 * Returns the idCount.
	 */
	protected final int getIdCount() {
		return idCount;
	}

	public ConfigurationFormFieldHelper() {
    }

    /**
	 * Add a {@link FormGroup} for a {@link ConfigurationDescriptor} filled with form fields for
	 * each {@link PropertyDescriptor}.
	 *
	 * <p>
	 * For {@link PropertyDescriptor}s of {@link PropertyKind#ITEM} or {@link PropertyKind#LIST} a
	 * SelectField and a {@link FormGroup} will be embedded.
	 * </p>
	 *
	 * <p>
	 * The original {@link ConfigurationDescriptor} or {@link PropertyDescriptor} can be obtained
	 * through {@link FormMember#get(Property)}, using {@link #CONFIG_DESCRIPTOR} or
	 * {@link #PROPERTY_DESCRIPTOR} as key
	 * </p>
	 */
    public FormGroup addFormGroup(FormContainer aContext, ConfigurationItem anItem, ConfigurationDescriptor aConfigDesc, String aFormGroupName) {
        FormGroup theGroup = this.createFormGroup(anItem, aConfigDesc, aFormGroupName, aContext.getResources());

        aContext.addMember(theGroup);
        return theGroup;
    }

    public FormGroup addFormGroup(FormContainer aContext, ConfigurationItem anItem, ConfigurationDescriptor aConfigDesc) {
        String    theName  = extractName(aConfigDesc);

        FormGroup theGroup = this.createFormGroup(anItem, aConfigDesc, theName, aContext.getResources());

        aContext.addMember(theGroup);
        return theGroup;
    }

    /**
     * Return the last part of a "."-separated string
     */
	public static final String extractName(ConfigurationDescriptor aConfigDesc) {
        return extractName(aConfigDesc.getConfigurationInterface());
    }

	public static final String extractName(Class aClass) {
        String theString = aClass.getName();
        int idx = theString.lastIndexOf('.');
        if (idx > -1) {
			return theString.substring(idx + 1);
        }
		return theString;
    }

	public static final ResKey extractKey(Class aClass) {
		return ResKey.legacy(extractName(aClass));
	}

	public static final ResKey extractKey(ConfigurationDescriptor type) {
		return ResKey.legacy(extractName(type));
	}

    /**
	 * Create a {@link FormGroup} for a {@link ConfigurationDescriptor}. Each {@link FormMember}
	 * represents a {@link PropertyDescriptor} which will be created through
	 * {@link #createFormMember(ResourceView, ConfigurationItem, PropertyDescriptor, String)}.
	 *
	 * For customizing reasons {@link #postProcessFormGroup(FormGroup, ConfigurationDescriptor)} is
	 * called after adding all {@link FormMember}s to the group.
	 *
	 * The {@link ConfigurationDescriptor} is added as Property {@link #CONFIG_DESCRIPTOR} to the
	 * {@link FormGroup}
	 */
    public FormGroup createFormGroup(ConfigurationItem anItem, ConfigurationDescriptor aConfigDesc, String aFormGroupName, ResourceView someResources) {
        FormGroup theGroup = new FormGroup(aFormGroupName, someResources);
		ResourceProvider provider = new ConfigurationDescriptorResourceProvider();
		theGroup.setLabel(provider.getLabel(theGroup));
		theGroup.setTooltip(provider.getTooltip(aConfigDesc));

        for (PropertyDescriptor property : aConfigDesc.getProperties()) {
            String thePropName = property.getPropertyName();

            List someMembers = this.createFormMember(someResources, anItem, property, thePropName);
            this.addFormMembers(theGroup, someMembers, property);
        }

		theGroup.set(CONFIG_DESCRIPTOR, aConfigDesc);
        this.postProcessFormGroup(theGroup, aConfigDesc);
        return theGroup;
    }

    /**
     * Create a {@link FormMember} for a {@link PropertyDescriptor} and name it like aFieldname.
     */
    public List createFormMember(ResourceView aResource, ConfigurationItem anItem, PropertyDescriptor aProperty, String aFieldname) {

        FormMember theMember = null;

        switch (aProperty.kind()) {
        case PLAIN:
            theMember = this.createPlainValueField(anItem, aProperty, aFieldname);
            break;
        case ITEM:

				ConfigurationItem theConfig = null;
        	if (anItem != null) {
					theConfig = aProperty.getConfigurationAccess().getConfig(anItem.value(aProperty));
        	}
        	theMember = this.createItemGroup(theConfig, aResource, aProperty, aFieldname);

            break;
        case LIST:
            theMember = this.createListGroup(anItem, aResource, aProperty, aFieldname);
            break;
        case MAP:
            // not yet supported
            break;
        case COMPLEX:
            // not yet supported
            break;
        default:
            // not yet supported
            break;
        }

        // add the PropertyDescriptor as property
        if (theMember != null) {
			theMember.set(PROPERTY_DESCRIPTOR, aProperty);
        }

        return theMember != null ? Collections.singletonList(theMember) : null;
    }

    protected FormField createPlainValueField(ConfigurationItem anItem, PropertyDescriptor aProperty, String aFieldname) {
        Class thePropType = aProperty.getType();
        Object theValue = null;
        if (anItem != null) {
        	theValue = anItem.value(aProperty);
        }
        else {
        	theValue = aProperty.getDefaultValue();
        }
        if (String.class.isAssignableFrom(thePropType)) {
            return FormFactory.newStringField(aFieldname, theValue, false);
        }
        else if (boolean.class.isAssignableFrom(thePropType) || Boolean.class.isAssignableFrom(thePropType)) {
            if (Utils.isTrue((Boolean) theValue)) {
                theValue = Boolean.TRUE;
            }
            else {
                theValue = Boolean.FALSE;
            }
            return FormFactory.newBooleanField(aFieldname, theValue, false);
        }
        else if (Date.class.isAssignableFrom(thePropType)) {
            return FormFactory.newDateField(aFieldname, theValue, false);
        }
        else if (int.class.isAssignableFrom(thePropType) || Number.class.isAssignableFrom(thePropType)) {
            return FormFactory.newComplexField(aFieldname, new DecimalFormat("0"), theValue, false);
        }
        else if (Enum.class.isAssignableFrom(thePropType)) {
            List   theSelection = Collections.EMPTY_LIST;
            if (theValue != null) {
                theSelection = Collections.singletonList(theValue);
            }

            final List options = Arrays.asList(thePropType.getEnumConstants());
			SelectField theField = FormFactory.newSelectField(aFieldname, options, false, theSelection, false);
            theField.setOptionLabelProvider(I18NResourceProvider.INSTANCE);
            return theField;
        }
        else if (Color.class.isAssignableFrom(thePropType)) {
            return FormFactory.newComplexField(aFieldname, ColorFormat.INSTANCE, theValue, false);
        }
        else {
            return null;
        }
    }

    /**
	 * Create a {@link FormGroup} containing a {@link SelectField} with all possible Class given by
	 * {@link PropertyDescriptorImpl#getImplementationClasses(PropertyDescriptor)} as options and a
	 * {@link FormGroup} containing additional {@link FormGroup}s, one for each
	 * {@link ConfigurationDescriptor} that is implementing a Class of the options of the
	 * {@link SelectField}
	 */
    protected FormGroup createItemGroup(ConfigurationItem anItem, ResourceView aResource, PropertyDescriptor aProperty, String aGroupname) {

        // group containing select and groupContainer
        FormGroup theSelectionGroup = new FormGroup(aGroupname, aResource);
		theSelectionGroup.set(PROPERTY_DESCRIPTOR, aProperty);

		PolymorphicConfiguration<?> theConcreteValue = null;
        Class                    theConcreteClass = null;
        if (anItem != null) {
			theConcreteValue = (PolymorphicConfiguration<?>) anItem;
        	theConcreteClass = theConcreteValue.getImplementationClass();
        }

		List theOptions = new ArrayList(PropertyDescriptorImpl.getImplementationClasses(aProperty));
        int   theSize    = theOptions.size();
        List  theMembers = new ArrayList(theSize + 2);

        SelectField theField = FormFactory.newSelectField(SELECT_FIELD, theOptions, false,
                Collections.EMPTY_LIST,
                false);
		theField.setOptionLabelProvider(new ConfigurationDescriptorResourceProvider());
        // if only one option is possible, this field must not be shown
        theField.setVisible(theSize != 1);
        // if a value is given, then select this value
        if (theConcreteClass != null) {
        	theField.setAsSelection(Collections.singletonList(theConcreteClass));
        }
        // if only one option is possible, then preselect it
        else if (theSize == 1) {
        	theField.setAsSelection(theOptions);
        }
        theMembers.add(theField);

        Set theItems = new HashSet();
        for (int i = 0; i < theOptions.size(); i++) {
            Class theImplClass = (Class) theOptions.get(i);
            // if a concrete value is given, then use its descriptior
            if (theImplClass == theConcreteClass) {
            	theItems.add(theConcreteValue.descriptor());
            }
            else {
            	theItems.add(this.getConfigurationDescriptor(theImplClass));
            }
        }

        FormGroup theGroupContainer = new FormGroup(GROUP_CONTAINER, aResource);

        // the visiblity of the groups depends of the selection
        theMembers.add(theGroupContainer);
        final Map /* <ConfigurationDescriptor, FormGroup> */groupsByDescription = new HashMap();
        for (Iterator theIter = theItems.iterator(); theIter.hasNext(); ) {
            ConfigurationDescriptor theConfDesc = (ConfigurationDescriptor) theIter.next();
            String       theResPrefix = extractName(theConfDesc);
			ResourceView theResources = ResPrefix.legacyString(theResPrefix);
            ConfigurationItem theItem = null;

            // if a concrete value is given, then init the formgroup with its values
            if (theConcreteValue != null && theConcreteValue.descriptor() == theConfDesc) {
            	theItem = theConcreteValue;
            }
            FormGroup theGroup = this.createFormGroup(theItem, theConfDesc, theResPrefix, theResources);
            if (theGroup != null) {
                theGroup.setVisible(false);
                theGroupContainer.addMember(theGroup);
                groupsByDescription.put(theConfDesc, theGroup);
            }
        }

        ValueListener theListener = new ValueListener() {
            @Override
			public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                Iterator theIter = groupsByDescription.values().iterator();
                while (theIter.hasNext()) {
                    ((FormGroup) theIter.next()).setVisible(false);
                }
                Class theClass = (Class) CollectionUtil.getFirst(aNewValue);
                if (theClass != null) {
                    FormGroup theGroup = (FormGroup) groupsByDescription.get(getConfigurationDescriptor(theClass));
                    if (theGroup != null) {
                        theGroup.setVisible(true);
                    }
                }
            }
        };
        FormFieldHelper.initDependency(theField, theListener);

        theSelectionGroup.addMembers(theMembers);
        return theSelectionGroup;
    }

    /**
     * Create a CommandField which will remove aRowGroup from aTableGroup if executed
     */
    protected CommandField createRemoveRowCommand(final FormGroup aTableGroup, final FormGroup aRowGroup) {
        Command theExe = new Command() {
            @Override
			public HandlerResult executeCommand(DisplayContext aContext) {
                aTableGroup.removeMember(aRowGroup);
                return HandlerResult.DEFAULT_RESULT;
            }
        };

        CommandField theCommand = FormFactory.newCommandField(REMOVE_TABLE_ROW, theExe);
		theCommand.setImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON);
		theCommand.setNotExecutableImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED);
        return theCommand;
    }

    /**
     * Create a custom set of {@link FormGroup}s which behave like a table.
     * The name of the group is prefixed with {@link #TABLE_PREFIX}
     */
    protected FormGroup createListGroup(ConfigurationItem anItem, final ResourceView aResource, final PropertyDescriptor aProperty, String aFieldname) {

        final FormGroup theTableGroup = new FormGroup(TABLE_PREFIX + aFieldname, aResource);

        if (anItem != null) {
        	List theConcreteValues = (List) anItem.value(aProperty);
			ConfigurationAccess configurationAccess = aProperty.getConfigurationAccess();
        	for (int i=0; i<theConcreteValues.size(); i++) {
				PolymorphicConfiguration<?> theConcreteConf =
					(PolymorphicConfiguration<?>) configurationAccess.getConfig(theConcreteValues.get(i));
        		this.addListRow(theConcreteConf, theTableGroup, aProperty);
        	}
        }
//        else {
        	this.addListRow(null, theTableGroup, aProperty);
//        }


        return theTableGroup;

    }

    /**
	 * Create a {@link FormGroup} representing on row in the table returned by
	 * {@link #createListGroup(ConfigurationItem, ResourceView, PropertyDescriptor, String)}
	 *
	 * The row contains three members. One {@link CommandField} for removing the row, a
	 * {@link SelectField} for selection the displayed {@link ConfigurationDescriptor} and a
	 * {@link FormGroup} containing more {@link FormGroup}, one for each possible
	 * {@link ConfigurationDescriptor}.
	 */
    protected final void addListRow(ConfigurationItem anItem, final FormGroup aTableGroup, final PropertyDescriptor aProperty) {

        final ResourceView theResources = aTableGroup.getResources();

        FormGroup theRowGroup = createItemGroup(anItem, theResources, aProperty, TABLE_ROW_PREFIX + idCount++);

        final SelectField theSelect = (SelectField) theRowGroup.getMember(SELECT_FIELD);

        // add a command to remove the group again
        final CommandField theCommand = this.createRemoveRowCommand(aTableGroup, theRowGroup);
        theCommand.setDisabled(anItem == null);
        theRowGroup.addMember(theCommand);

        // we must add the row to the table before triggering the listener
        // to guarantee the order of the rows
        aTableGroup.addMember(theRowGroup);

        // if a selection is done, add a new empty line and make the previos selection mandatory
        ValueListener theListener = new ValueListener() {
            @Override
			public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                if (CollectionUtil.isEmptyOrNull((Collection) aOldValue) && ! CollectionUtil.isEmptyOrNull((Collection) aNewValue)) {
                    theSelect.setMandatory(true);
                    theCommand.setDisabled(false);
                    addListRow(null, aTableGroup, aProperty);
                }
            }
        };
		FormFieldHelper.initDependency(theSelect, theListener);

		// if the item has a value, we trigger the listener to add a new empty row at the end of the table
//		if (! CollectionUtil.isEmptyOrNull(theSelect.getSelection())) {
//			theListener.valueChanged(theSelect, null, theSelect.getSelection());
//		}
    }

    protected List addFormMembers(FormContainer aContext, List someMembers, PropertyDescriptor aProperty) {
        if (someMembers != null) {
            for (int i=0; i<someMembers.size(); i++) {
                FormMember theMember = (FormMember) someMembers.get(i);
                aContext.addMember(theMember);
                this.postProcessFormMember(theMember, aProperty, theMember.getName());
            }
        }
        return someMembers;
    }

    /**
	 * Hook for subclasses to modify form members after
	 * {@link #addFormMembers(FormContainer, List, PropertyDescriptor)}
	 */
    protected void postProcessFormMember(FormMember aMember, PropertyDescriptor aProperty, String aPropertyName) {
    }

    /**
	 * Hook for subclasses to modify {@link FormGroup} after the were filled with {@link FormMember}
	 * in {@link #createFormGroup(ConfigurationItem, ConfigurationDescriptor, String, ResourceView)}
	 */
    protected void postProcessFormGroup(FormGroup aGroup, ConfigurationDescriptor aConfigDesc) {
    }


    /**
     * Parse a {@link FormContainer} and build a concrete {@link ConfigurationItem}
     */
    public ConfigurationItem createConfigurationItem(FormContainer aFormGroup, Class anImplementationClassDefault) {

		ConfigurationDescriptor theItemDesc = aFormGroup.get(CONFIG_DESCRIPTOR);

        if (theItemDesc != null) {
            ConfigBuilder theBuilder = TypedConfiguration.createConfigBuilder(theItemDesc);

            Iterator<? extends FormMember> theIter = aFormGroup.getMembers();
            while (theIter.hasNext()) {
                FormMember theMember = theIter.next();

				PropertyDescriptor thePropDesc = theMember.get(PROPERTY_DESCRIPTOR);
                if (thePropDesc != null) {
                    switch (thePropDesc.kind()) {
                        case PLAIN:
                            this.initPlainValue((FormField) theMember, thePropDesc, theBuilder);
                            break;
                        case ITEM:
                            this.initItemValue((FormGroup) theMember, thePropDesc, theBuilder);
                            break;
                        case LIST:
                            this.initListValue((FormGroup) theMember, thePropDesc, theBuilder);
                            break;
                        case MAP:
                            // not yet supported
                            break;
                        case COMPLEX:
                            // not yet supported
                            break;
                        default:
                            // not yet supported
                            break;
                    }
                }
            }

            if (anImplementationClassDefault != null) {
                PropertyDescriptor theImplDesc = theItemDesc.getProperty(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
                if (theImplDesc != null) {
                    theBuilder.initValue(theImplDesc, anImplementationClassDefault);
                }
            }

			return theBuilder.createConfig(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
        }

        return null;
    }

    public final ConfigurationDescriptor getConfigurationDescriptor(Class anImplementationClass) {
        try {
            return this.resolver.getConfigurationDescriptor(anImplementationClass);
        } catch (ConfigurationException cfex) {
            throw new ConfigurationError("Misconfigured class " + anImplementationClass, cfex);
        }
    }

    protected void initPlainValue(FormField aField, PropertyDescriptor aProperty, ConfigBuilder aConfigBuilder) {
        Object theValue = null;
        if (aField instanceof SelectField) {
            theValue = ((SelectField) aField).getSingleSelection();
        }
        else {
            theValue = aField.getValue();

            if (theValue instanceof Number) {
                Class theExpected = aProperty.getType();

                if (! theExpected.isAssignableFrom(theValue.getClass())) {
                    try {
                        Constructor theConst = theExpected.getConstructor(new Class[] { String.class } );
                        theValue = theConst.newInstance(new Object[] { theValue.toString() });
                    } catch (Exception ex) {
                        Logger.error("Unable to convert " + theValue + " into " + theExpected, ex, ConfigurationFormFieldHelper.class);
                    }

                }
            }

            if (theValue != null && ! aProperty.getType().isAssignableFrom(theValue.getClass())) {
//                Logger.warn("Expected: " + aProperty.getType() + ", got: " + theValue.getClass() + " from field: " + aField, ConfigurationFormFieldHelper.class);
            }

        }
        aConfigBuilder.initValue(aProperty, theValue);
    }

    /**
	 * Init a property value of type {@link PropertyKind#ITEM} represented by a {@link FormGroup}
	 * created through
	 * {@link #createItemGroup(ConfigurationItem, ResourceView, PropertyDescriptor, String)}
	 */
    protected void initItemValue(FormGroup aSelectionGroup, PropertyDescriptor aDesc, ConfigBuilder aConfigBuilder) {
        ConfigurationItem theItem = this.extractSelectedConfigurationItem(aSelectionGroup, aDesc, aConfigBuilder);
        if (theItem != null) {
            aConfigBuilder.initValue(aDesc, theItem);
        }
    }

    protected ConfigurationItem extractSelectedConfigurationItem(FormGroup aSelectionGroup, PropertyDescriptor aDesc, ConfigBuilder aConfigBuilder) {
        SelectField theSelect = (SelectField) aSelectionGroup.getField(SELECT_FIELD);
        Class theClass = (Class) theSelect.getSingleSelection();

        if (theClass == null) {
            return null;
        }

        ConfigurationDescriptor theDesc = this.getConfigurationDescriptor(theClass);

        FormGroup   theGroupContainer = (FormGroup) aSelectionGroup.getMember(GROUP_CONTAINER);
        Iterator theIter = theGroupContainer.getMembers();
        while (theIter.hasNext()) {
            FormMember theMember = (FormMember) theIter.next();
			if (theMember.get(CONFIG_DESCRIPTOR) == theDesc && theMember instanceof FormContainer) {
                ConfigurationItem theItem = this.createConfigurationItem((FormContainer) theMember, theClass);
                return theItem;
            }
        }
        return null;
    }

    /**
	 * Init a property value of type {@link PropertyKind#LIST}, represented by a {@link FormGroup}
	 * created through
	 * {@link #createListGroup(ConfigurationItem, ResourceView, PropertyDescriptor, String)}
	 */
    protected void initListValue(FormGroup aTableGroup, PropertyDescriptor aDesc, ConfigBuilder aConfigBuilder) {
        Iterator theRowGroups = aTableGroup.getMembers();

        List theValues = new ArrayList();
        while (theRowGroups.hasNext()) {
            FormGroup theRowGroup = (FormGroup) theRowGroups.next();

            ConfigurationItem theItem = this.extractSelectedConfigurationItem(theRowGroup, aDesc, aConfigBuilder);
            if (theItem != null) {
                theValues.add(theItem);
            }
        }
        aConfigBuilder.initValue(aDesc, theValues);
    }

	public static class ConfigurationDescriptorResourceProvider extends AbstractDispatchingResourceProvider {

		private final Resources resources = Resources.getInstance();

		@Override
		public String getLabel(Object aObject) {
            if (aObject instanceof ConfigurationDescriptor) {
                Class theIF = ((ConfigurationDescriptor) aObject).getConfigurationInterface();
				return resources.getString(extractKey(theIF));
            }
            else if (aObject instanceof Class) {
				return resources.getString(extractKey((Class) aObject));
            }

            return super.getLabel(aObject);
        }

        @Override
		public String getTooltip(Object aObject) {
            if (aObject instanceof ConfigurationDescriptor) {
                Class theIF = ((ConfigurationDescriptor) aObject).getConfigurationInterface();
				return resources.getString(extractKey(theIF).tooltip());
            }
            else if (aObject instanceof Class) {
				return resources.getString(extractKey((Class) aObject));
            }

            return super.getTooltip(aObject);
        }

		@Override
		protected ResourceProvider getProviderImpl(Object anObject) {
			return DefaultResourceProvider.INSTANCE;
		}
    }
}
