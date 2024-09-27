/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.demo.layout.form.demo.model.DemoAddress;
import com.top_logic.demo.layout.form.demo.model.DemoAddressContext;
import com.top_logic.demo.layout.form.demo.model.DemoOrder;
import com.top_logic.demo.layout.form.demo.model.DemoOrderContext;
import com.top_logic.demo.layout.form.demo.model.DemoPerson;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.component.BasicAJAXSupport;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.AlwaysExecutable;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * Demo of AJAX-enabled forms.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoOrderForm extends EditComponent implements ModelProvider {

	/**
	 * Configuration options for {@link DemoOrderForm}.
	 */
	public interface Config extends EditComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@NullDefault
		String getLockOperation();

		@Override
		@StringDefault(ApplyCommand.COMMAND)
		String getApplyCommand();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			EditComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(FillInDefaultValuesAction.COMMAND_ID);
			registry.registerCommand(NewAddressAction.COMMAND_ID);
			registry.registerCommand(ToggleNameVisibility.COMMAND_ID);
			registry.registerButton(LegacyCommand.COMMAND_ID);
			registry.registerButton(AjaxApply.COMMAND_ID);
			registry.registerButton(ExternalCheck.COMMAND_ID);
			registry.registerButton(ExternalLegacyCheck.COMMAND_ID);
		}
	}

	public static final String ORDER_CONTEXT_NAME = "order";

    BasicAJAXSupport basicAJAXSupport = new BasicAJAXSupport();
    
	public DemoOrderForm(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public FormContext createFormContext() {
        // The fields of this demo are grouped in a custom form context created here.
		DemoOrderContext demoOrderContext = new DemoOrderContext(ORDER_CONTEXT_NAME);
		return new FormContext("test", I18NConstants.TEST, new FormMember[] {
			demoOrderContext,
			createReflectionContext(demoOrderContext)
		});
	}
	
	private static FormGroup createReflectionContext(FormGroup group) {
		FormGroup result = new FormGroup("reflection", group.getResources());

		for (Iterator it = group.getDescendants(); it.hasNext(); ) {
			FormMember member = (FormMember) it.next();
			
			FormGroup properties = new FormGroup(
				member.getQualifiedName().replace('.', '_'), 
				ReflectiveProperties.SINGLETON);
			result.addMember(properties);
			properties.setLabel(ResKey.text("field '" + member.getLabel() + "'"));
			
			{
				FormField reflectiveField = 
					FormFactory.newBooleanField("visible", Boolean.valueOf(member.isLocallyVisible()), false);
				reflectiveField.addValueListener(new VisibilityManager(member));
				properties.addMember(reflectiveField);
			}

			{
				FormField reflectiveField = 
					FormFactory.newBooleanField("immutable", Boolean.valueOf(member.isLocallyImmutable()), false);
				reflectiveField.addValueListener(new WriteProtectionManager(member));
				properties.addMember(reflectiveField);
			}

			{
				FormField reflectiveField = 
					FormFactory.newBooleanField("disabled", Boolean.valueOf(member.isLocallyDisabled()), false);
				reflectiveField.addValueListener(new DisableManager(member));
				properties.addMember(reflectiveField);
			}

			if (member instanceof FormField) {
				FormField field = (FormField) member;
				
				{
					FormField reflectiveField = 
						FormFactory.newBooleanField("frozen", Boolean.valueOf(field.isFrozen()), false);
					reflectiveField.addValueListener(new FieldFreezeManager(field));
					properties.addMember(reflectiveField);
				}
				
				{
					FormField reflectiveField = 
						FormFactory.newBooleanField("blocked", Boolean.valueOf(field.isBlocked()), false);
					reflectiveField.addValueListener(new FieldBlockManager(field));
					properties.addMember(reflectiveField);
				}
				
				{
					FormField reflectiveField = 
						FormFactory.newBooleanField("mandatory", Boolean.valueOf(field.isMandatory()), false);
					reflectiveField.addValueListener(new MandatoryManager(field));
					properties.addMember(reflectiveField);
				}

				{
					StringField reflectiveField = 
						FormFactory.newStringField("value", rawToString(field.getRawValue()), false);
					reflectiveField.addValueListener(new ValueObserver(reflectiveField));
					properties.addMember(reflectiveField);
				}
			}
		}
		
		return result;
	}
	
	private static String rawToString(Object rawValue) {
		if (rawValue == AbstractFormField.NO_RAW_VALUE) return "nil";
		if (rawValue instanceof List) {
			List rawList = (List) rawValue;
			StringServices.toString(rawList, ", ");
		}
		if (rawValue instanceof String) {
			return '"' + ((String) rawValue) + '"';
		}
		return rawValue.toString();
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return new DemoOrder(new DemoPerson(null, null, null, null, null, null, null, null, null), new DemoAddress(),
			null);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof DemoOrder && super.supportsInternalModel(anObject);
	}

	private boolean checkForHeinz() {
		StringField givenName = 
			((DemoOrderContext) getFormContext().getContainer(ORDER_CONTEXT_NAME)).purchaser.givenName;

		if (givenName.isValid()) {
			if (! "Heinz".equals(givenName.getValue())) {
				basicAJAXSupport.add(JSSnipplet.createAlert("The given name is not 'Heinz'!"));
				givenName.setError("The given name is not 'Heinz'!");
				
				return false;
			}
		}
		
		return true;
	}
	
	public static final class AjaxApply extends AJAXCommandHandler {
		public static final String COMMAND_ID = "ajaxApply";

		public AjaxApply(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			Logger.info("Received AJAX apply.", this);
			aComponent.markAsValid();
			return HandlerResult.DEFAULT_RESULT;
		}
	}


	public static final class LegacyCommand extends AJAXCommandHandler {
		public static final String COMMAND_ID = "legacyCommand";

		public LegacyCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			aComponent.markAsValid();
			return HandlerResult.DEFAULT_RESULT;
		}
	}


	public static final class ExternalCheck extends AJAXCommandHandler {
		public static final String COMMAND_ID = "externalCheck";

		public ExternalCheck(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			((DemoOrderForm) aComponent).checkForHeinz();
			return HandlerResult.DEFAULT_RESULT;
		}
	}


	public static final class ExternalLegacyCheck extends AJAXCommandHandler {

		public static final String COMMAND_ID = "externalLegacyCheck";

		public ExternalLegacyCheck(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			DemoOrderForm form = (DemoOrderForm) aComponent;
			if (!form.checkForHeinz()) {
				return addFieldErrors(new HandlerResult(), form.getFormContext());
			}
			form.markAsValid();
			return HandlerResult.DEFAULT_RESULT;
		}
	}


	public static final class ToggleNameVisibility extends AJAXCommandHandler {
		public static final String COMMAND_ID = "toggleNameVisibility";

		public ToggleNameVisibility(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext context,
				LayoutComponent component, Object model, Map<String, Object> someArguments) {
			BlockControl control = (BlockControl) ((ControlComponent) component).getControl("nameRows");
			control.setVisible(! control.isVisible());
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return AlwaysExecutable.INSTANCE;
		}
	}


	public static final class ApplyCommand extends AbstractApplyCommandHandler {
		
		public static final String COMMAND = "demoOrderApply";

		public ApplyCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
			return true;
		}
		
	}
	
	
	private static final class ValueObserver implements ValueListener {
		StringField observerField;
		
		public ValueObserver(StringField observerField) {
			this.observerField = observerField;
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			observerField.setAsString(String.valueOf(field.getValue()));
		}
	}

	private static abstract class PropertyManager implements ValueListener {
		FormMember controledMember;

		public PropertyManager(FormMember controledMember) {
			this.controledMember = controledMember;
		}
	}

	private static final class DisableManager  extends PropertyManager {
		public DisableManager(FormMember controledMember) {
			super(controledMember);
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			controledMember.setDisabled((((Boolean) newValue).booleanValue()));
		}
	}

	private static final class MandatoryManager extends PropertyManager {
		public MandatoryManager(FormMember controledMember) {
			super(controledMember);
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			controledMember.setMandatory((((Boolean) newValue).booleanValue()));
		}
	}
	
	private static final class FieldBlockManager extends PropertyManager {
		public FieldBlockManager(FormField controledMember) {
			super(controledMember);
		}
		
		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			((FormField) controledMember).setBlocked((((Boolean) newValue).booleanValue()));
		}
	}

	private static final class FieldFreezeManager extends PropertyManager {
		public FieldFreezeManager(FormField controledMember) {
			super(controledMember);
		}
		
		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			((FormField) controledMember).setFrozen((((Boolean) newValue).booleanValue()));
		}
	}
	
	private static final class WriteProtectionManager extends PropertyManager {
		public WriteProtectionManager(FormMember controledMember) {
			super(controledMember);
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			controledMember.setImmutable((((Boolean) newValue).booleanValue()));
		}
	}

	private static final class VisibilityManager  extends PropertyManager {
		public VisibilityManager(FormMember controledMember) {
			super(controledMember);
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			controledMember.setVisible((((Boolean) newValue).booleanValue()));
		}
	}

	private static final class ReflectiveProperties implements ResourceView {
		public static final ResourceView SINGLETON = new ReflectiveProperties();

		@Override
		public boolean hasStringResource(String resourceKey) {
			return true;
		}
		
		@Override
		public ResKey getStringResource(String i18nKey) {
			return ResKey.text("property '" + i18nKey + "'");
		}

		@Override
		public ResKey getStringResource(String i18nKey, ResKey defaultValue) {
			// There is a resource for every key, no default is ever required.
			return getStringResource(i18nKey);
		}
	}

	public static final class NewAddressAction extends AJAXCommandHandler {
		public static final String COMMAND_ID = "newAddress";
		
		/**
		 * Configuration for {@link NewAddressAction}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AJAXCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

		/**
		 * Singleton constructor.
		 */
		public NewAddressAction(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			DemoOrderContext orderContext = 
				((DemoOrderContext) ((FormComponent) aComponent).getFormContext().getContainer(ORDER_CONTEXT_NAME));

			// Create a new address object.
			DemoAddress newAddress = new DemoAddress();
			DemoAddressContext deliveryContext = orderContext.deliveryAddress;
			deliveryContext.saveIn(newAddress);
			
			SelectField lastAddresses = orderContext.lastAddresses;

			// Get all options and the currently selected options. Saving the
			// selection before changing the options list is necessary, because
			// the select field performs a reset() after setting the options
			// list.
			List updatedAddresses = new ArrayList(lastAddresses.getOptions());
			List updatedSelection = new ArrayList(lastAddresses.getSelection());
			
			// Add the new address to the options of the last addresses field.
			updatedAddresses.add(newAddress);
			
			// Add the new option to the selected options list.
			updatedSelection.add(newAddress);

			lastAddresses.setOptions(updatedAddresses);
			lastAddresses.setAsSelection(updatedSelection);
			
			return HandlerResult.DEFAULT_RESULT;
		}
		
	}

	public static final class FillInDefaultValuesAction extends AJAXCommandHandler {

		public static final String COMMAND_ID = "fillInDefault";

		public FillInDefaultValuesAction(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			// Set some of the fields to some predefined values. 
			DemoOrderContext theContext = 
				((DemoOrderContext) ((FormComponent) aComponent).getFormContext().getContainer(ORDER_CONTEXT_NAME));

			theContext.purchaser.givenName.setAsString("Hans");
			theContext.purchaser.surname.setAsString("Tester");
			
			return HandlerResult.DEFAULT_RESULT;
		}
	}
	
}
