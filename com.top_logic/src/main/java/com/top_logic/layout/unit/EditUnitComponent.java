/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.unit;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.constraints.SelectionSizeConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.NoImageResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.Resources;
import com.top_logic.util.resource.I18NConstants;

/**
 * {@link LayoutComponent} to edit a {@link Unit} instance. It has form fields for
 * the name, format, sort order, base unit and conversion factor.
 * 
 * @author     <a href="mailto:TEH@top-logic.com">TEH</a>
 */
public class EditUnitComponent extends EditComponent {

	/**
	 * Typed configuration interface definition for {@link EditUnitComponent}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends EditComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@StringDefault(ApplyUnitCommandHandler.COMMAND_ID)
		String getApplyCommand();

		@Override
		@StringDefault(DeleteUnitCommandHandler.COMMAND_ID)
		String getDeleteCommand();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			EditComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.addAll(SimpleBoundCommandGroup.READWRITECREATE_SET);
		}
	}

	/**
	 * Name of the property used to annotate the I18N prefix of the translations to the form group.
	 */
	private static final Property<String> I18N_PREFIX_PROPERTY = TypedAnnotatable.property(String.class, "I18N prefix");

	/**
	 * Name of the group created by {@link #createLanguageGroup(Wrapper, String, String...)}
	 */
	@CalledFromJSP
	public static final String TRANSLATIONS_GROUP_NAME = "_languageFieldGroupName";

	/**
	 * Create a {@link EditUnitComponent}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public EditUnitComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = new FormContext("UnitFormContext", getResPrefix());
		
		String theName = null;
		String theFormat = null;
		Integer theOrder = null;
		Unit theBaseUnit = null;
		Double theFactor = null;
		if (this.getModel() != null) {
			Unit theUnit = (Unit) this.getModel();
			
			theName = theUnit.getName();
			theFormat = theUnit.getFormatSpec();
			theOrder = theUnit.getSortOrder();
			theBaseUnit = theUnit.getBaseUnit();
			theFactor = Double.valueOf(theUnit.getConversionFactor());
		}
		
		List<Unit> theUnits = UnitWrapper.getAllUnits();
        
        StringField  theNameField      = FormFactory.newStringField(UnitWrapper.NAME_ATTRIBUTE, theName, MANDATORY, !IMMUTABLE, null);
		StringField theFormatField =
			FormFactory.newStringField(UnitWrapper.FORMAT, theFormat, MANDATORY, !IMMUTABLE, null);
        IntField     theSortOrderField = FormFactory.newIntField(UnitWrapper.SORT_ORDER, theOrder, MANDATORY, !IMMUTABLE, null);
		SelectField baseUnitField =
			FormFactory.newSelectField(UnitWrapper.BASE_FORMAT_REF, theUnits, !MULTI_SELECT,
				CollectionUtil.intoList(theBaseUnit), !MANDATORY, !IMMUTABLE, new SelectionSizeConstraint(0, 1));

		// Use a label provider that shows a name even for those units that do not display anything
		// in the context of an amount.
		baseUnitField.setOptionLabelProvider(NoImageResourceProvider.createNoImageResourceProvider(WrapperResourceProvider.INSTANCE));

        ComplexField theFactorField    = FormFactory.newComplexField(UnitWrapper.FACTOR, HTMLFormatter.getInstance().getNumberFormat(), theFactor, IGNORE_WHITESPACE, !MANDATORY, !IMMUTABLE, null);

        formContext.addMember(theNameField);
        formContext.addMember(theFormatField);
        formContext.addMember(theSortOrderField);
        formContext.addMember(baseUnitField);
        formContext.addMember(theFactorField);

		addLanguageGroup(formContext, (Wrapper) getModel(), Unit.DISPLAY_NAME_ATTRIBUTE_KEY);
            
		return formContext;
	}

	/**
	 * Adds a language group for the given model which is identified uniquely by the
	 * {@link AbstractWrapper#NAME_ATTRIBUTE name} attribute.
	 * 
	 * @see #addLanguageGroup(FormContext, String, Wrapper, String...)
	 */
	public static void addLanguageGroup(FormContext context, Wrapper model, String... i18nAttributeKeys) {
		addLanguageGroup(context, AbstractWrapper.NAME_ATTRIBUTE, model, i18nAttributeKeys);
	}

	/**
	 * Adds a {@link FormGroup} to the given {@link FormContext} that contains fields for
	 * translations for the given model.
	 * 
	 * <p>
	 * The given I18N attribute keys are the I18N keys of the attributes that are internationalized.
	 * The values for the different language for the given model are finally stored under the I18N
	 * key which is the concatenation of the I18N key of the attribute, '.', and the value of the
	 * given unique attribute of the given model.
	 * </p>
	 * 
	 * <p>
	 * The values are made persistent by using {@link #storeI18N(Wrapper, FormContext)}.
	 * </p>
	 * 
	 * <p>
	 * Note: To display the added fields just include the JSP snipplet "jsp/util/changeI18N.inc"
	 * </p>
	 * 
	 * @param context
	 *        The {@link FormContext} to add translation fields group to. It must not already
	 *        contain a member with name {@value #TRANSLATIONS_GROUP_NAME}.
	 * @param uniqueAttribute
	 *        The attribute of the given model that identifies the given object unique under all
	 *        objects of the same type. If the value is changed the translations will be loast,
	 *        therefore it should be immutable.
	 * @param model
	 *        The object to translate. May be <code>null</code> in case the object is currently
	 *        created. In such case the language fields are empty.
	 * @param i18nAttributeKeys
	 *        The I18N keys of the attributes to store internationalized.
	 * @see #storeI18N(Wrapper, FormContext)
	 */
	public static void addLanguageGroup(FormContext context, String uniqueAttribute, Wrapper model,
			String... i18nAttributeKeys) {
		context.addMember(createLanguageGroup(model, uniqueAttribute, i18nAttributeKeys));
	}

	private static FormGroup createLanguageGroup(Wrapper model, String uniqueAttribute, String... i18nAttributeKeys) {
		String[] supportedLanguages = ResourcesModule.getInstance().getSupportedLocaleNames();
		FormGroup languageGroup = new FormGroup(TRANSLATIONS_GROUP_NAME, I18NConstants.I18N_FORM_GROUP_PREFIX);
		Resources resources = Resources.getInstance();
		String groupLabel = resources.getString(I18NConstants.I18N_FORM_GROUP_TITLE);
		languageGroup.setLabel(groupLabel);
		int i = 0;
		for (String i18nAttributeKey : i18nAttributeKeys) {
			FormGroup g = new FormGroup("group_" + i++, I18NConstants.I18N_FORM_GROUP_PREFIX);
			String attribute = resources.getString(ResKey.legacy(i18nAttributeKey));
			String resourcePrefix = i18nAttributeKey + '.';
			for (String supportedLanguage : supportedLanguages) {
				StringField languageField =
					createLanguageField(resources, supportedLanguage, attribute, resourcePrefix, model,
						uniqueAttribute);
				g.addMember(languageField);
			}
			languageGroup.addMember(g);
		}
		return languageGroup;
	}

	private static StringField createLanguageField(Resources resources, String language, String i18nAttribute,
			String resourcePrefix, Wrapper model, String uniqueAttribute) {
		String currentI18N = currentI18N(language, resourcePrefix, model, uniqueAttribute);
		boolean isMandatory = !StringServices.isEmpty(currentI18N);
		StringLengthConstraint constraint = new StringLengthConstraint((isMandatory ? 1 : 0), 50);
		String fieldName = language;
		StringField newField =
			FormFactory.newStringField(fieldName, currentI18N, isMandatory, !FormFactory.IMMUTABLE, constraint);
		String fieldLabel = resources.getMessage(I18NConstants.I18N_FORM_GROUP_PREFIX.key(fieldName), i18nAttribute);
		newField.setLabel(fieldLabel);
		newField.set(I18N_PREFIX_PROPERTY, resourcePrefix);
		return newField;
	}

	private static String currentI18N(String language, String resourcePrefix, Wrapper model, String uniqueAttribute) {
		String currentI18N;
		if (model != null) {
			ResKey resourceKey = getResourceKey(resourcePrefix, model, uniqueAttribute);
			currentI18N = Resources.getInstance(language).getString(resourceKey, StringServices.EMPTY_STRING);
		} else {
			currentI18N = StringServices.EMPTY_STRING;
		}
		return currentI18N;
	}

	/**
	 * Stores the translations for the given wrapper changed in the given {@link FormContext}.
	 * 
	 * @param model
	 *        The model to update I18N for.
	 * @param context
	 *        The {@link FormContext} formerly enhanced by
	 *        {@link #addLanguageGroup(FormContext, Wrapper, String...)}
	 * 
	 * @see #addLanguageGroup(FormContext, String, Wrapper, String...)
	 */
	public static void storeI18N(Wrapper model, FormContext context) {
		FormGroup group = (FormGroup) context.getMember(TRANSLATIONS_GROUP_NAME);

		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			for (FormMember changedMember : group.getChangedMembers()) {
				String resourcePrefix = changedMember.get(I18N_PREFIX_PROPERTY);
				if (resourcePrefix == null) {
					throw new IllegalArgumentException("Given field was not build with service method.");
				}
				String translation = ((StringField) changedMember).getAsString();
				String language = changedMember.getName();
				tx.saveI18N(language, getResourceKey(resourcePrefix, model, AbstractWrapper.NAME_ATTRIBUTE),
					translation);
			}

			tx.commit();
		}
	}

	private static ResKey getResourceKey(String resourcePrefix, Wrapper model, String uniqueAttribute) {
		return ResKey.legacy(resourcePrefix + getObjectIdentifier(model, uniqueAttribute));
	}

	private static String getObjectIdentifier(Wrapper model, String uniqueAttribute) {
		return (String) model.getValue(uniqueAttribute);
	}

	@Override
	public boolean allow(BoundObject aObject) {
        if (!supportsInternalModel(aObject)) {
            return false;
        }
        return super.allow(aObject);
    }

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof Unit;
	}

}
