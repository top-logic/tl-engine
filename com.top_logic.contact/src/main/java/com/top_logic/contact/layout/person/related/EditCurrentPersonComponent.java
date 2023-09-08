/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person.related;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.knowledge.wrap.person.Homepage;
import com.top_logic.knowledge.wrap.person.NoStartPageAutomatismExecutability;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;

/**
 * The EditCurrentPersonComponent is used to edit the current persons user. It's
 * supposed to open in a global dialog and only supports the current user as a
 * model.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class EditCurrentPersonComponent extends EditRelatedPersonComponent {

	public static final Set<BoundCommandGroup> READWRITE_SET = 
		CollectionFactory.set(SimpleBoundCommandGroup.READ, SimpleBoundCommandGroup.WRITE);

	/**
	 * {@link FormField} name for the {@link PersonalConfiguration#getAutoTranslate()} editor.
	 */
	public static final String PERSONAL_CONFIGURATION_AUTO_TRANSLATE = "autoTranslate";
	public static final String PERSONAL_CONFIGURATION_START_PAGE = "startPage";
	public static final String PERSONAL_CONFIGURATION_TABLE_GROUP        = "personalConfigurationGroup";
	public static final String PERSONAL_CONFIGURATION_TABLE              = "personalConfiguration";
	public static final String PERSONAL_CONFIGURATION_TABLE_COLUMN_KEY   = "key";
	public static final String PERSONAL_CONFIGURATION_TABLE_COLUMN_VALUE = "value";

	/**
	 * Configuration for the {@link EditCurrentPersonComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EditRelatedPersonComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			EditRelatedPersonComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(DispatchAction.COMMAND_NAME);
			registry.registerButton(ResetStartPageCommandHandler.COMMAND_ID);
			registry.unregisterHandler(getDeleteCommand());
		}

	}

	/**
	 * The name of the {@link FormField} representing whether the start page should be set
	 * automatically to the last visited site.
	 */
	public static final String FIELD_START_PAGE_AUTOMATISM = "startPageAutomatism";

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link EditCurrentPersonComponent}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public EditCurrentPersonComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	/**
	 * Only current person is allowed.
	 * 
	 * @return <code>true</code> iff given object is current {@link Person}.
	 */
	@Override
	protected boolean supportsInternalModel(Object aObject) {
		boolean isCurrentPerson = aObject == TLContextManager.getSubSession().getPerson();
		if (!isCurrentPerson) {
			return false;
		}
		return super.supportsInternalModel(aObject);
	}
	
	@Override
	public FormContext createFormContext() {
		FormContext                        theFC     = super.createFormContext();
		Person                             thePerson = (Person) this.getModel();
		final PersonalConfigurationWrapper theConf   = PersonalConfigurationWrapper.getPersonalConfiguration(thePerson);

		if (!getPersonalConfig().getStartPageAutomatism()) {
			LayoutComponent theStartComponent = getStartPage();
			String theStartPageName = startPageName(theStartComponent);
			StringField theStartPageField =
				FormFactory.newStringField(PERSONAL_CONFIGURATION_START_PAGE, theStartPageName, StringField.IMMUTABLE);
			theFC.addMember(theStartPageField);
		}
		{
			List<String> theAttributeNames = theConf == null ? Collections.<String>emptyList() : new ArrayList<>(theConf.getAllAttributeNames());
			for (Iterator<String> theIt = theAttributeNames.iterator(); theIt.hasNext();) {
				String theName = theIt.next();
				if (theName.startsWith("_")) {
					theIt.remove();
				}
			}
			TableConfiguration table = TableConfigurationFactory.table();
			table.getDefaultColumn().setAccessor(accessor(theConf));
			ColumnConfiguration keyColumn = table.declareColumn(PERSONAL_CONFIGURATION_TABLE_COLUMN_KEY);
			keyColumn.setAccessor(SimpleAccessor.INSTANCE);
			TableModel thePCTableModel =
				new ObjectTableModel(new String[] { PERSONAL_CONFIGURATION_TABLE_COLUMN_KEY,
					PERSONAL_CONFIGURATION_TABLE_COLUMN_VALUE }, table, theAttributeNames);
			
			TableField thePCField      = FormFactory.newTableField(PERSONAL_CONFIGURATION_TABLE, thePCTableModel);
			FormGroup  thePCTableGroup = new FormGroup(PERSONAL_CONFIGURATION_TABLE_GROUP, theFC.getResources());
			
			thePCTableGroup.addMember(thePCField);
			thePCTableGroup.setCollapsed(true);
			theFC.addMember(thePCTableGroup);
			theFC.getField(FIELD_NAME_RELATED_CONTACT).setImmutable(true);
			if (!PersonalConfiguration.getConfig().getHideOptionStartPageAutomatism()) {
				theFC.addMember(createFieldStartPageAutomatism());
			}
		}

		BooleanField autoTranslate = FormFactory.newBooleanField(PERSONAL_CONFIGURATION_AUTO_TRANSLATE,
			PersonalConfiguration.getPersonalConfiguration().getAutoTranslate(), false);
		theFC.addMember(autoTranslate);
		autoTranslate.setVisible(TranslationService.isActive());

		return theFC;
	}

	private String startPageName(LayoutComponent startComponent) {
		if (startComponent == null) {
			return null;
		}
		CompoundSecurityLayout nearestCompoundLayout = CompoundSecurityLayout.getNearestCompoundLayout(startComponent);
		if (nearestCompoundLayout == null) {
			return null;
		}
		return Resources.getInstance().getString(nearestCompoundLayout.getTitleKey());
	}

	private LayoutComponent getStartPage() {
		PersonalConfiguration thePConf = PersonalConfiguration.getPersonalConfiguration();
		Homepage homepage;
		try {
			homepage = thePConf.getHomepage(getMainLayout());
		} catch (ConfigurationException ex) {
			// Home page can not be resolved.
			return null;
		}
		if (homepage == null) {
			return null;
		}
		ComponentName startComponentName = homepage.getComponentName();
		if (startComponentName == null) {
			return null;
		}
		return this.getMainLayout().getComponentByName(startComponentName);
	}

	private Accessor<String> accessor(final PersonalConfigurationWrapper pc) {
		return new Accessor<>() {
			@Override
			public void setValue(String object, String property, Object value) {
				pc.setValue(object, value);
			}
			
			@Override
			public Object getValue(String object, String property) {
				return pc.getValue(object);
			}
		};
	}

	private BooleanField createFieldStartPageAutomatism() {
		BooleanField field = FormFactory.newBooleanField(FIELD_START_PAGE_AUTOMATISM);
		field.initializeField(getPersonalConfig().getStartPageAutomatism());
		return field;
	}

	private PersonalConfiguration getPersonalConfig() {
		return PersonalConfiguration.getPersonalConfiguration();
	}

	/** A {@link CommandHandler} for resetting the start page of the current user. */
	public static class ResetStartPageCommandHandler extends AbstractCommandHandler {

		/** {@link ConfigurationItem} of the {@link ResetStartPageCommandHandler}. */
		public interface Config extends AbstractCommandHandler.Config {

			@Override
			@ListDefault({ InViewModeExecutable.class, NoStartPageAutomatismExecutability.class })
			List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

			@Override
			@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
			CommandGroupReference getGroup();

		}

		/** The default {@link #getID()} of the command. */
		public static final String COMMAND_ID = "resetStartPage";

		/**
		 * Called by the {@link TypedConfiguration} for creating a
		 * {@link EditCurrentPersonComponent.ResetStartPageCommandHandler}.
		 * <p>
		 * <b>Don't call directly.</b> Use
		 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
		 * </p>
		 * 
		 * @param context
		 *        For error reporting and instantiation of dependent configured objects.
		 * @param config
		 *        The configuration for the new instance.
		 */
		@CalledByReflection
		public ResetStartPageCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			PersonalConfiguration thePConf = PersonalConfiguration.getPersonalConfiguration();
			thePConf.removeHomepage(aComponent.getMainLayout());
			PersonalConfiguration.storePersonalConfiguration();
			aComponent.invalidate();
			return DefaultHandlerResult.DEFAULT_RESULT;
		}

	}

	@Override
	public boolean allow(BoundCommandGroup aCmdGroup, BoundObject anObject) {
		if (READWRITE_SET.contains(aCmdGroup)) {
			return true;
		}
		return super.allow(aCmdGroup, anObject);
	}
}
