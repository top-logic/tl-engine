/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import static com.top_logic.basic.shared.string.StringServicesShared.*;
import static com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.LabelMatcher;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.editor.scripting.CreateComponentButtonAction;
import com.top_logic.layout.editor.scripting.Identifiers;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.layout.scripting.FieldMode;
import com.top_logic.layout.scripting.FieldValidity;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.action.assertion.CheckAction;
import com.top_logic.layout.scripting.action.assertion.FieldErrorAssertion;
import com.top_logic.layout.scripting.action.assertion.FieldLabelAssertion;
import com.top_logic.layout.scripting.action.assertion.FieldModeAssertion;
import com.top_logic.layout.scripting.action.assertion.FieldValidityAssertion;
import com.top_logic.layout.scripting.action.assertion.GlobalVariableExistenceAssertion;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.action.assertion.ModelNotExistsAssertion;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion.Comparision;
import com.top_logic.layout.scripting.check.CheckInstantiator;
import com.top_logic.layout.scripting.check.EqualsCheck;
import com.top_logic.layout.scripting.check.EqualsCheck.EqualsCheckConfig;
import com.top_logic.layout.scripting.check.ValueCheck.ValueCheckConfig;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.ref.ContextDependent;
import com.top_logic.layout.scripting.recorder.ref.DownloadValue;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.scripting.recorder.ref.ReferenceFactory;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.ValueInContextNamingScheme.ValueInContextName;
import com.top_logic.layout.scripting.recorder.ref.misc.FieldValue;
import com.top_logic.layout.scripting.recorder.ref.misc.SelectionRef;
import com.top_logic.layout.scripting.recorder.ref.ui.button.ButtonAspectNaming.ButtonAspectName;
import com.top_logic.layout.scripting.recorder.ref.ui.form.FormMemberAspectNaming.FormMemberAspectName;
import com.top_logic.layout.scripting.recorder.ref.ui.table.TableColumnAspectNaming.TableColumnAspectName;
import com.top_logic.layout.scripting.recorder.ref.ui.table.TableColumnsNaming.TableColumnsName;
import com.top_logic.layout.scripting.recorder.ref.value.AspectNaming;
import com.top_logic.layout.scripting.recorder.ref.value.AspectNaming.Name;
import com.top_logic.layout.scripting.recorder.ref.value.ListNaming;
import com.top_logic.layout.scripting.recorder.ref.value.ListValue;
import com.top_logic.layout.scripting.recorder.ref.value.MapNaming;
import com.top_logic.layout.scripting.recorder.ref.value.MapNaming.Name.EntryName;
import com.top_logic.layout.scripting.recorder.ref.value.MapValue;
import com.top_logic.layout.scripting.recorder.ref.value.MapValue.MapEntryValue;
import com.top_logic.layout.scripting.recorder.ref.value.RowTableValue;
import com.top_logic.layout.scripting.recorder.ref.value.TableValue;
import com.top_logic.layout.scripting.recorder.ref.value.form.FieldPlaceholderNaming;
import com.top_logic.layout.scripting.recorder.ref.value.form.FieldRawValueNaming;
import com.top_logic.layout.scripting.recorder.ref.value.form.FieldValueNaming;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ActionChainOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.AwaitProgressActionOp;
import com.top_logic.layout.scripting.runtime.action.CheckSelectionOp;
import com.top_logic.layout.scripting.runtime.action.CheckVisibilityOp;
import com.top_logic.layout.scripting.runtime.action.ClearDownloadsOp;
import com.top_logic.layout.scripting.runtime.action.ClearGlobalVariablesOp;
import com.top_logic.layout.scripting.runtime.action.CloseDialogActionOp;
import com.top_logic.layout.scripting.runtime.action.CollapseToolbarActionOp;
import com.top_logic.layout.scripting.runtime.action.CollapseToolbarActionOp.CollapseToolbarAction;
import com.top_logic.layout.scripting.runtime.action.CommandActionOp;
import com.top_logic.layout.scripting.runtime.action.CommandExecutionOp;
import com.top_logic.layout.scripting.runtime.action.DelGlobalVariableOp;
import com.top_logic.layout.scripting.runtime.action.EditActionOp;
import com.top_logic.layout.scripting.runtime.action.ExpandActionOp;
import com.top_logic.layout.scripting.runtime.action.FormInputOp;
import com.top_logic.layout.scripting.runtime.action.FormRawInputOp;
import com.top_logic.layout.scripting.runtime.action.FuzzyGotoActionOp;
import com.top_logic.layout.scripting.runtime.action.FuzzyGotoActionOp.FuzzyGoto;
import com.top_logic.layout.scripting.runtime.action.GotoActionOp;
import com.top_logic.layout.scripting.runtime.action.LabeledButtonActionOp;
import com.top_logic.layout.scripting.runtime.action.LogMessageActionOp;
import com.top_logic.layout.scripting.runtime.action.MakeVisibleOp;
import com.top_logic.layout.scripting.runtime.action.NamedTabSwitchOp;
import com.top_logic.layout.scripting.runtime.action.OpenDialogOp;
import com.top_logic.layout.scripting.runtime.action.OpenTableFilterOp;
import com.top_logic.layout.scripting.runtime.action.OpenTreeFilterOp;
import com.top_logic.layout.scripting.runtime.action.RecordingFailedActionOp;
import com.top_logic.layout.scripting.runtime.action.SelectActionOp;
import com.top_logic.layout.scripting.runtime.action.SelectObjectOp;
import com.top_logic.layout.scripting.runtime.action.SelectSelectableActionOp;
import com.top_logic.layout.scripting.runtime.action.SetTableColumns;
import com.top_logic.layout.scripting.runtime.action.SetTableColumnsOp;
import com.top_logic.layout.scripting.runtime.action.SimpleActionOp;
import com.top_logic.layout.scripting.runtime.action.SimpleActionWrapperOp;
import com.top_logic.layout.scripting.runtime.action.SortTableColumn;
import com.top_logic.layout.scripting.runtime.action.SortTableColumnOp;
import com.top_logic.layout.scripting.runtime.action.TabSwitchOp;
import com.top_logic.layout.scripting.runtime.action.assertion.CheckActionOp;
import com.top_logic.layout.scripting.runtime.action.assertion.FieldErrorAssertionOp;
import com.top_logic.layout.scripting.runtime.action.assertion.FieldLabelAssertionOp;
import com.top_logic.layout.scripting.runtime.action.assertion.FieldModeAssertionOp;
import com.top_logic.layout.scripting.runtime.action.assertion.FieldValidityAssertionOp;
import com.top_logic.layout.scripting.runtime.action.assertion.ModelNotExistsAssertionOp;
import com.top_logic.layout.scripting.runtime.action.assertion.NamedTabAssertOp;
import com.top_logic.layout.scripting.runtime.action.assertion.ValueAssertionOp;
import com.top_logic.layout.scripting.util.ScriptTableUtil;
import com.top_logic.layout.structure.Expandable.ExpansionState;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.TableData;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * Factory for {@link ApplicationActionOp}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ActionFactory {

	private static final Class<?>[] NO_ARGUMENTS = {};
	
	/**
	 * Create an {@link ApplicationAction} that selects the object with the
	 * given label.
	 */
	public static SelectObject selectObject(ComponentName selectableComponentName, String objectName,
			Class<? extends LabelProvider> labelProviderClass) {
		final PolymorphicConfiguration<LabelProvider> labelProviderConfig = labelProvider(labelProviderClass);
		return selectObject(selectableComponentName, objectName, labelProviderConfig);
	}

	public static SelectObject selectObject(ComponentName selectableComponentName, String objectName,
			final PolymorphicConfiguration<? extends LabelProvider> labelProviderConfig) {
		SelectObject result = selectObject(selectableComponentName, labelMatcher(objectName, labelProviderConfig));
		result.setImplementationClass(SelectObjectOp.class);
		return result;
	}

	/**
	 * Create an {@link ApplicationAction} that checks, whether an object with
	 * the the given label is selected.
	 */
	public static SelectObject checkSelection(ComponentName selectableComponentName, String objectName,
			Class<? extends LabelProvider> labelProviderClass) {
		final PolymorphicConfiguration<LabelProvider> labelProviderConfig = labelProvider(labelProviderClass);
		return checkSelection(selectableComponentName, objectName, labelProviderConfig);
	}

	public static SelectObject checkSelection(ComponentName selectableComponentName, String objectName,
			final PolymorphicConfiguration<? extends LabelProvider> labelProviderConfig) {
		SelectObject result = selectObject(selectableComponentName, labelMatcher(objectName, labelProviderConfig));
		result.setImplementationClass(CheckSelectionOp.class);
		return result;
	}
	
	private static SelectObject selectObject(ComponentName selectableComponentName,
			LabelMatcher.Config labelMatcherConfig) {
		SelectObject config = ActionFactory.newApplicationAction(SelectObject.class);
		config.setComponentName(selectableComponentName);
		config.setMatcherConfig(labelMatcherConfig);
		return config;
	}

	/**
	 * Create an {@link ApplicationActionOp} that checks whether the given component is visible.
	 */
	public static ComponentAction checkVisibility(ComponentName componentName, boolean visible) {
		CheckVisibilityOp.Config config =
			ActionFactory.newApplicationAction(CheckVisibilityOp.Config.class, CheckVisibilityOp.class);
		config.setComponentName(componentName);
		config.setVisible(visible);
		return config;
	}
	
	/**
	 * Create an {@link ApplicationAction} that makes the given component
	 * visible.
	 */
	public static ComponentAction makeVisible(ComponentName componentName) {
		return componentAction(ComponentAction.class, componentName, MakeVisibleOp.class);
	}
	
	/**
	 * @deprecated Use {@link FormInput}
	 */
	@Deprecated
	public static FormAction formInput(ModelName modelName, List<? extends FieldValue> fieldInputs) {
		return formAction(FormInputOp.class, modelName, fieldInputs);
	}

	/**
	 * Is not working for TabBars. Use {@link #formInput(ModelName, List)} instead if possible.
	 */
	@Deprecated
	public static FormAction formInput(ComponentName componentName, List<? extends FieldValue> fieldInputs) {
		com.top_logic.layout.component.ComponentBasedNamingScheme.ComponentName componentModelName = TypedConfiguration
			.newConfigItem(com.top_logic.layout.component.ComponentBasedNamingScheme.ComponentName.class);
		componentModelName.setComponentName(componentName);
		return formAction(FormInputOp.class, componentModelName, fieldInputs);
	}
	
	/**
	 * @deprecated Use regular command for switching edit mode.
	 */
	@Deprecated
	public static FormAction editAction(ModelName modelName, List<? extends FieldValue> fieldInputs) {
		return formAction(EditActionOp.class, modelName, fieldInputs);
	}

	private static PolymorphicConfiguration<LabelProvider> labelProvider(Class<? extends LabelProvider> labelProviderClass) {
		Factory implementationFactory;
		try {
			implementationFactory = DefaultConfigConstructorScheme.getFactory(labelProviderClass);
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException("Invalid implementation class.", ex);
		}
		PolymorphicConfiguration<LabelProvider> config =
			(PolymorphicConfiguration) TypedConfiguration.newConfigItem((Class) implementationFactory.getConfigurationInterface());
		config.setImplementationClass(labelProviderClass);
		return config;
	}

	private static LabelMatcher.Config labelMatcher(String objectName,
			PolymorphicConfiguration<? extends LabelProvider> labelProviderConfig) {
		LabelMatcher.Config config = TypedConfiguration.newConfigItem(LabelMatcher.Config.class);
		config.setExpectedName(objectName);
		config.setLabelProvider(labelProviderConfig);
		return config;
	}

	private static <T extends ComponentAction> T componentAction(Class<T> actionClass, ComponentName componentName,
			Class<? extends ApplicationActionOp<?>> implementationClass) {
		T config = ActionFactory.newApplicationAction(actionClass, implementationClass);
		config.setComponentName(componentName);
		return config;
	}
	
	/**
	 * Create an {@link ApplicationAction} that executes the given command.
	 */
	public static CommandAction commandAction(ComponentName componentName, String commandName, Map<String, ?> arguments) {
		CommandAction config = ActionFactory.newApplicationAction(CommandAction.class, CommandActionOp.class);
		setCommandParameters(config, componentName, commandName, arguments);
		return config;
	}

	/**
	 * Creates a {@link LabeledButtonAction} that create a new dynamic component using the given
	 * stable component identifiers.
	 */
	public static CreateComponentButtonAction createComponentButtonAction(Identifiers identifiers, ComponentName name,
			ResKey label) {
		return createComponentButtonAction(identifiers, name, Resources.getInstance().getString(label));
	}

	/**
	 * Creates a {@link LabeledButtonAction} that create a new dynamic component using the given
	 * stable component identifiers.
	 * 
	 * @param label
	 *        Internationalised label of the button.
	 */
	public static CreateComponentButtonAction createComponentButtonAction(Identifiers identifiers, ComponentName name,
			String label) {
		CreateComponentButtonAction buttonAction = TypedConfiguration.newConfigItem(CreateComponentButtonAction.class);

		buttonAction.setIdentifiers(identifiers);
		buttonAction.setComponentName(name);
		buttonAction.setLabel(label);

		return buttonAction;
	}

	/**
	 * Creates a {@link GotoAction} for the specified jump.
	 */
	public static GotoAction gotoAction(ComponentName componentName, String commandName, Map<String, ?> arguments) {
		GotoAction config = ActionFactory.newApplicationAction(GotoAction.class, GotoActionOp.class);
		setCommandParameters(config, componentName, commandName, arguments);
		return config;
	}

	/**
	 * Fills given {@link CommandAction} with informations from given arguments.
	 */
	public static void setCommandParameters(CommandAction config, ComponentName componentName, String commandName,
			Map<String, ?> arguments) {
		config.setComponentName(componentName);
		if (!UUIDInitializer.ID_PATTERN.matcher(commandName).matches()) {
			config.setCommandName(commandName);
		}
		config.setArguments(ReferenceFactory.attributeValues(arguments));
	}
	
	/**
	 * Creates a {@link CommandExecution}.
	 */
	public static CommandExecution commandExecution(ModelName modelName) {
		return commandExecution(modelName, false);
	}

	/**
	 * Creates a {@link CommandExecution}.
	 */
	public static CommandExecution commandExecution(ModelName modelName, boolean failureExpected) {
		CommandExecution config = ActionFactory.newApplicationAction(CommandExecution.class, CommandExecutionOp.class);
		config.setModelName(modelName);
		config.setFailureExpected(failureExpected);
		return config;
	}

	/**
	 * Create an {@link ApplicationAction} that opens the dialog with the given
	 * name at the given component.
	 */
	public static OpenDialog openDialog(ComponentName componentName, ComponentName dialogName) {
		OpenDialog config = ActionFactory.newApplicationAction(OpenDialog.class, OpenDialogOp.class);
		config.setComponentName(componentName);
		config.setDialogName(dialogName);
		return config;
	}

	/**
	 * Create an {@link ApplicationAction} that switches to the given tab index.
	 */
	public static TabSwitch tabSwitch(ComponentName componentName, int index) {
		TabSwitch config = ActionFactory.newApplicationAction(TabSwitch.class, TabSwitchOp.class);
		config.setComponentName(componentName);
		config.setIndex(index);
		return config;
	}

	/**
	 * Create an {@link ApplicationAction} that switches to the given tab name.
	 */
	public static NamedTabSwitch tabSwitch(ComponentName componentName, String cardName) {
		NamedTabSwitch config = ActionFactory.newApplicationAction(NamedTabSwitch.class, NamedTabSwitchOp.class);
		config.setComponentName(componentName);
		config.setCardName(cardName);
		return config;
	}

	/**
	 * Create an {@link NamedTabSwitch} that switches to the tab with the given index. The
	 * {@link NamedTabSwitch} is preferable to the index based {@link TabSwitch}, as the name helps
	 * reading, understanding and debugging the action.
	 */
	public static NamedTabSwitch tabSwitchNamed(TabComponent tabBar, int tabIndex) {
		return tabSwitch(tabBar.getName(), tabBar.getTabName(tabIndex));
	}

	/**
	 * Create an {@link ApplicationAction} that asserts that the given tab is active.
	 */
	public static NamedTabSwitch tabAssert(ComponentName componentName, String cardName) {
		NamedTabSwitch config = ActionFactory.newApplicationAction(NamedTabSwitch.class, NamedTabAssertOp.class);
		config.setComponentName(componentName);
		config.setCardName(cardName);
		return config;
	}
	
	/**
	 * @deprecated Use {@link FormRawInput}, or {@link FormInput}.
	 */
	@Deprecated
	public static FormAction formAction(Class<? extends ApplicationActionOp<?>> op, ModelName modelName,
			List<? extends FieldValue> fieldInputs) {
		FormAction config = ActionFactory.newApplicationAction(FormAction.class, op);
		config.setModelName(modelName);
		config.setFieldValues(fieldInputs);
		return config;
	}

	/**
	 * Create a {@link ActionChain} of the given {@link ApplicationAction}s.
	 */
	public static ActionChain actionChain(ApplicationAction... configs) {
		return actionChain(CollectionUtil.toList(configs));
	}

	/**
	 * Create a {@link ActionChain} of the given {@link ApplicationAction}s.
	 */
	public static ActionChain actionChain(List<ApplicationAction> configs) {
		ActionChain config = ActionFactory.newApplicationAction(ActionChain.class, ActionChainOp.class);
		config.setActions(configs);
		return config;
	}

	/**
	 * Create a {@link ConditionalAction} of the given {@link ApplicationAction}s.
	 */
	public static ConditionalAction conditionalAction(ApplicationAction condition, List<ApplicationAction> configs) {
		ConditionalAction config = ActionFactory.newApplicationAction(ConditionalAction.class, ActionChainOp.class);
		config.setCondition(condition);
		config.setActions(configs);
		return config;
	}

	/**
	 * @deprecated Use {@link FormInput}
	 */
	@Deprecated
	public static FormAction formUpdate(ModelName modelName, List<FieldValue> fieldInputs) {
		FormAction item = ActionFactory.newApplicationAction(FormAction.class, FormInputOp.class);
		item.setModelName(modelName);
		item.setFieldValues(fieldInputs);
		return item;
	}

	/**
	 * @deprecated Use {@link FormRawInput}
	 */
	@Deprecated
	public static FormAction formRawUpdate(ModelName modelName, List<FieldValue> fieldInputs) {
		FormAction item = ActionFactory.newApplicationAction(FormAction.class, FormRawInputOp.class);
		item.setModelName(modelName);
		item.setFieldValues(fieldInputs);
		return item;
	}

	/**
	 * Use {@link #selectObject(ModelName, SelectionRef, SelectionChangeKind)} instead.
	 */
	@Deprecated
	public static ApplicationAction selectObject(ComponentName componentName, ModelName selectionRef) {
		SelectSelectableAction item =
			ActionFactory.newApplicationAction(SelectSelectableAction.class, SelectSelectableActionOp.class);
		item.setComponentName(componentName);
		item.setSelection(selectionRef);
		return item;
	}
	
	public static ApplicationAction selectObject(ModelName modelName, SelectionRef selectionRef,
			SelectionChangeKind changeKind) {
		SelectAction selectAction = ActionFactory.newApplicationAction(SelectAction.class, SelectActionOp.class);
		selectAction.setSelectionModelName(modelName);
		selectAction.setSelection(selectionRef);
		selectAction.setChangeKind(changeKind);
		return selectAction;
	}

	public static ApplicationAction selectObject(NamedModel namedModel, Object target, boolean selectionState,
			SelectionChangeKind changeKind) {
		ModelName modelName = namedModel.getModelName();
		SelectionRef selectionRef = ReferenceFactory.selectionRef(target, selectionState, namedModel);
		return selectObject(modelName, selectionRef, changeKind);
	}

	public static ApplicationAction logout() {
		LogoutAction item = ActionFactory.newApplicationAction(LogoutAction.class);
		return item;
	}

	public static ApplicationAction customAction(Class<? extends AbstractApplicationActionOp<?>> opClass) {
		assert (opClass.getModifiers() & Modifier.PUBLIC) != 0 : "Action implementation is not public: " + opClass;
		
		ApplicationAction item = ActionFactory.newApplicationAction(ApplicationAction.class, opClass);
		return item;
	}

	public static SimpleActionWrapperOp.Config simpleAction(Class<? extends SimpleActionOp> implClass) {
		assert (implClass.getModifiers() & Modifier.PUBLIC) != 0 : "Action implementation is not public: " + implClass;
		
		Constructor<? extends SimpleActionOp> constructor;
		try {
			constructor = implClass.getConstructor(NO_ARGUMENTS);
		} catch (SecurityException ex) {
			throw (AssertionError) new AssertionError("Cannot access action implementation: " + implClass).initCause(ex);
		} catch (NoSuchMethodException ex) {
			throw (AssertionError) new AssertionError("No default constuctor, or not a static inner class: " + implClass).initCause(ex);
		}
		
		assert (constructor.getModifiers() & Modifier.PUBLIC) != 0 : "Action implementation constructor is not public: " + implClass;
		
		SimpleActionWrapperOp.Config config =
			ActionFactory.newApplicationAction(SimpleActionWrapperOp.Config.class, SimpleActionWrapperOp.class);
		config.setActionClass(implClass);
		return config;
	}

	/**
	 * Creates a {@link LabeledButtonAction} that "klicks" the button with the given label.
	 */
	public static LabeledButtonAction buttonAction(ComponentName componentName, Object businessObject, ResKey label) {
		return buttonAction(componentName, businessObject, Resources.getInstance().getString(label));
	}

	/**
	 * Creates a {@link LabeledButtonAction} that "klicks" the button with the given label.
	 * 
	 * @param componentName
	 *        Name of the component where the button can be found.
	 * @param label
	 *        Label of the button.
	 */
	public static LabeledButtonAction buttonAction(ComponentName componentName, Object businessObject, String label) {
		LabeledButtonAction config =
			ActionFactory.newApplicationAction(LabeledButtonAction.class, LabeledButtonActionOp.class);
		config.setComponentName(componentName);
		if (businessObject != null) {
			config.setBusinessObject(ModelResolver.buildModelName(businessObject));
		}
		config.setLabel(label);
		return config;
	}

	/**
	 * Creates a new {@link ValueAssertion} that asserts the value of a form field.
	 */
	public static ValueAssertion fieldValueAssertion(ModelName formMemberName, ModelName expectedValue,
			boolean rawValue, String comment) {
		return fieldAssertion(formMemberName, expectedValue, currentFieldValue(formMemberName, rawValue), comment);
	}

	/**
	 * Creates a new {@link ValueAssertion} that asserts the placeholder of a form field.
	 */
	public static ValueAssertion fieldPlaceholderAssertion(ModelName formMemberName, ModelName expectedValue,
			String comment) {
		return fieldAssertion(formMemberName, expectedValue, fieldPlaceholderValue(formMemberName), comment);
	}

	private static ValueAssertion fieldAssertion(ModelName formMemberName, ModelName expectedValue, Name actualValue,
			String comment) {
		if (needsContext(expectedValue)) {
			ValueInContextName valueInContext = createValueInContext(formMemberName, expectedValue);

			return valueAssertion(valueInContext, actualValue, comment);
		}
		return valueAssertion(expectedValue, actualValue, comment);
	}

	/**
	 * Creates a {@link ModelName} ValueContext for the given {@link FormMember} name.
	 */
	public static ValueInContextName createValueInContext(ModelName formMemberName, ModelName expectedValue) {
		ValueInContextName valueInContext = TypedConfiguration.newConfigItem(ValueInContextName.class);

		valueInContext.setLocalName(expectedValue);
		valueInContext.setContextName(formMemberName);

		return valueInContext;
	}

	private static boolean needsContext(ModelName expectedValue) {
		if (expectedValue instanceof ListValue) {
			for (ModelName entry : ((ListValue) expectedValue).getList()) {
				if (needsContext(entry)) {
					return true;
				}
			}
		}
		if (expectedValue instanceof ListNaming.Name) {
			for (ModelName entry : ((ListNaming.Name) expectedValue).getValues()) {
				if (needsContext(entry)) {
					return true;
				}
			}
		}
		if (expectedValue instanceof MapValue) {
			for (MapEntryValue entry : ((MapValue) expectedValue).getEntries()) {
				if (needsContext(entry.getValue()) || needsContext(entry.getKey())) {
					return true;
				}
			}
		}
		if (expectedValue instanceof MapNaming.Name) {
			for (EntryName entry : ((MapNaming.Name) expectedValue).getValues()) {
				if (needsContext(entry.getValue()) || needsContext(entry.getKey())) {
					return true;
				}
			}
		}
		return expectedValue instanceof ContextDependent;
	}

	/**
	 * Creates a new {@link ValueAssertion}.
	 */
	public static ValueAssertion valueAssertion(ModelName expectedValue, ModelName actualValue, String comment) {
		return valueAssertion(expectedValue, actualValue, Comparision.EQUALS, false, comment);
	}

	/**
	 * Creates a new {@link ValueAssertion}.
	 */
	public static ValueAssertion valueAssertion(ModelName expectedValue, ModelName actualValue,
			Comparision comparision, boolean inverted, String comment) {
		ValueAssertion config = ActionFactory.newApplicationAction(ValueAssertion.class, ValueAssertionOp.class);
		config.setExpectedValue(expectedValue);
		config.setActualValue(actualValue);
		config.setComparision(comparision);
		config.setInverted(inverted);
		setComment(config, comment);
		return config;
	}

	/**
	 * Either the {@link ModelName} for a {@link FieldRawValueNaming} or {@link FieldValueNaming}.
	 */
	public static Name currentFieldValue(ModelName fieldName, boolean rawValue) {
		AspectNaming.Name currentFieldValue;
		if (rawValue) {
			currentFieldValue = TypedConfiguration.newConfigItem(FieldRawValueNaming.Name.class);
		} else {
			currentFieldValue = TypedConfiguration.newConfigItem(FieldValueNaming.Name.class);
		}
		currentFieldValue.setModel(fieldName);
		return currentFieldValue;
	}

	/**
	 * The {@link ModelName} for a {@link FieldPlaceholderNaming}.
	 */
	public static Name fieldPlaceholderValue(ModelName fieldName) {
		AspectNaming.Name currentFieldValue = TypedConfiguration.newConfigItem(FieldPlaceholderNaming.Name.class);
		currentFieldValue.setModel(fieldName);
		return currentFieldValue;
	}

	/**
	 * Creates a new {@link FieldLabelAssertion} with {@link FieldLabelAssertionOp} as
	 * implementation class.
	 */
	public static FieldLabelAssertion fieldLabelAssertion(ModelName formMemberName, String label, String comment) {
		FieldLabelAssertion config = newApplicationAction(FieldLabelAssertion.class);
		config.setImplementationClass(FieldLabelAssertionOp.class);
		config.setModelName(formMemberName);
		config.setLabel(label);
		setComment(config, comment);
		return config;
	}

	/** Creates an assertion about the specified aspect of the given field. */
	public static CheckAction formMemberAspectAssertion(Class<? extends FormMemberAspectName> nameClass,
			String expectedValue, ModelName fieldName, String comment) {
		ModelName actualValueRef = formMemberAspectName(nameClass, fieldName);
		return equalsCheck(nonNull(expectedValue), actualValueRef, comment);
	}

	/**
	 * Creates a new assertion about the specified aspect of the given button.
	 */
	public static CheckAction buttonAspectAssertion(Class<? extends ButtonAspectName> nameClass,
			ModelName buttonName, Object expectedValue, String comment) {
		ModelName actualValueRef = buttonAspectName(nameClass, buttonName);
		return equalsCheck(expectedValue, actualValueRef, comment);
	}

	/**
	 * Creates a new assertion about the displayed column the given table.
	 */
	public static CheckAction tableColumnsAssertion(Class<? extends TableColumnsName> nameClass,
			TableCell tableCell, boolean allColumns, List<String> expectedValue, String comment) {
		ModelName tableRef = ModelResolver.buildModelName(tableCell.getTableData());
		tableAspectName(nameClass,tableRef).setAllColumns(allColumns);
		ModelName actualValueRef = tableAspectName(nameClass,tableRef);
		return equalsCheck(expectedValue, actualValueRef, comment);
	}

	/**
	 * Creates a new assertion about the specified aspect of the given table column.
	 */
	public static CheckAction tableColumnAspectAssertion(Class<? extends TableColumnAspectName> nameClass,
			TableCell tableCell, boolean expectedValue, String comment) {
		ModelName tableRef = ModelResolver.buildModelName(tableCell.getTableData());
		String columnLabel = ScriptTableUtil.getColumnLabel(tableCell.getColumnName(), tableCell.getTableData());
		ModelName actualValueRef = tableColumnAspectName(nameClass, tableRef, columnLabel);
		return equalsCheck(expectedValue, actualValueRef, comment);
	}

	/**
	 * Creates a {@link CheckAction} with an {@link EqualsCheck}.
	 */
	public static CheckAction equalsCheck(Object expectedValue, ModelName actualValueRef, String comment) {
		EqualsCheckConfig equalsCheck = CheckInstantiator.equalsCheck(ModelResolver.buildModelName(expectedValue));
		CheckAction config = checkAction(actualValueRef, Collections.<ValueCheckConfig> singletonList(equalsCheck));
		config.setComment(comment);
		return config;
	}

	/**
	 * Creates a new {@link FieldModeAssertion} with {@link FieldModeAssertionOp} as implementation
	 * class.
	 */
	public static FieldModeAssertion fieldModeAssertion(ModelName formMemberName, FieldMode mode, String comment) {
		FieldModeAssertion config = newApplicationAction(FieldModeAssertion.class);
		config.setImplementationClass(FieldModeAssertionOp.class);
		config.setModelName(formMemberName);
		config.setMode(mode);
		setComment(config, comment);
		return config;
	}

	/**
	 * Creates a new {@link FieldErrorAssertion} with {@link FieldErrorAssertionOp} as
	 * implementation class.
	 */
	public static FieldErrorAssertion fieldErrorAssertion(ModelName formMemberName, String error, Pattern errorPattern,
			String comment) {
		FieldErrorAssertion config =
			ActionFactory.newApplicationAction(FieldErrorAssertion.class, FieldErrorAssertionOp.class);
		config.setModelName(formMemberName);
		config.setError(error);
		config.setErrorPattern(errorPattern);
		setComment(config, comment);
		return config;
	}

	/**
	 * Creates a new {@link FieldValidityAssertion} with {@link FieldValidityAssertionOp} as
	 * implementation class.
	 */
	public static FieldValidityAssertion fieldValidityAssertion(ModelName formMemberName, FieldValidity validity,
			String comment) {
		FieldValidityAssertion config =
			ActionFactory.newApplicationAction(FieldValidityAssertion.class, FieldValidityAssertionOp.class);
		config.setModelName(formMemberName);
		config.setValidity(validity);
		setComment(config, comment);
		return config;
	}

	/**
	 * Creates a new {@link ModelNotExistsAssertion} with {@link ModelNotExistsAssertionOp} as
	 * implementation class.
	 */
	public static ModelNotExistsAssertion modelNotExistsAssertion(ModelName modelName, String failurePattern,
			String comment) {
		ModelNotExistsAssertion config =
			ActionFactory.newApplicationAction(ModelNotExistsAssertion.class, ModelNotExistsAssertionOp.class);
		config.setModelName(modelName);
		config.setFailurePattern(failurePattern);
		setComment(config, comment);
		return config;
	}

	private static void setComment(GuiAssertion config, String comment) {
		// Make sure not to include empty attributes into the serialization.
		if (!StringServices.isEmpty(comment)) {
			config.setComment(comment);
		}
	}

	/**
	 * Creates a new {@link ValueAssertion} with {@link ValueAssertionOp} as
	 * implementation class.
	 */
	public static ValueAssertion tableCellValueAssertion(TableCell tableCell, String comment) {
		ModelName expectedValue = ModelResolver.buildModelName(tableCell.getValue());
		TableValue actualValue = ReferenceInstantiator.tableValue(RowTableValue.class, tableCell);
		return valueAssertion(expectedValue, actualValue, comment);
	}

	/**
	 * Creates a {@link OpenTableFilter} action.
	 */
	public static OpenTableFilter openTableFilter(ModelName tableDataName, String columnLabel) {
		OpenTableFilter config = ActionFactory.newApplicationAction(OpenTableFilter.class, OpenTableFilterOp.class);
		config.setModelName(tableDataName);
		config.setColumnLabel(columnLabel);
		return config;
	}

	/**
	 * Creates a {@link OpenTreeFilter} action.
	 */
	public static OpenTreeFilter openTreeFilter(ModelName tableDataName) {
		OpenTreeFilter config = ActionFactory.newApplicationAction(OpenTreeFilter.class, OpenTreeFilterOp.class);
		config.setModelName(tableDataName);
		return config;
	}

	/**
	 * Creates a {@link SortTableColumn} action.
	 */
	public static SortTableColumn sortTableColumn(TableData table, List<SortConfig> sortOrder) {
		SortTableColumn config = ActionFactory.newApplicationAction(SortTableColumn.class, SortTableColumnOp.class);

		config.setModelName(table.getModelName());
		config.setSortOrders(createLabelSortOrder(sortOrder, table));
		config.setLabel(true);
		return config;
	}

	private static List<SortConfig> createLabelSortOrder(List<SortConfig> sortConfigs, TableData tableData) {
		List<SortConfig> result = new ArrayList<>();
		for (SortConfig sortConfig : sortConfigs) {
			String columnName = sortConfig.getColumnName();
			String columnLabel = ScriptTableUtil.getColumnLabel(columnName, tableData);
			boolean ascending = sortConfig.getAscending();
			result.add(ReferenceInstantiator.sortConfig(columnLabel, ascending));
		}
		return result;
	}

	/**
	 * Creates a {@link SetTableColumns} action.
	 */
	public static SetTableColumns setTableColumns(TableData table, List<String> columnNames) {
		SetTableColumns config = ActionFactory.newApplicationAction(SetTableColumns.class, SetTableColumnsOp.class);

		config.setModelName(table.getModelName());
		config.setLabel(true);
		config.setColumns(ScriptTableUtil.toColumnLabels(columnNames, table));
		return config;
	}

	/**
	 * Creates a {@link CheckAction} for an actual download.
	 */
	public static ApplicationAction downloadCheck(BinaryDataSource data) {
		DownloadValue actualValue = downloadValue(data.getName());
		ValueCheckConfig binaryFileEqualityCheck =
			CheckInstantiator.binaryFileEqualityCheck(ReferenceInstantiator.base64Value(data.toData()));
		List<ValueCheckConfig> checks = Arrays.asList(binaryFileEqualityCheck);
		ApplicationAction downloadCheck = checkAction(actualValue, checks);

		ApplicationAction clearDownloads =
			ActionFactory.newApplicationAction(ApplicationAction.class, ClearDownloadsOp.class);

		ActionChain actionChain = actionChain(downloadCheck, clearDownloads);
		actionChain.setComment("Check and clear download.");

		return actionChain;
	}

	/**
	 * Creates a {@link CheckAction}.
	 */
	public static CheckAction checkAction(ModelName actualValue, List<ValueCheckConfig> checks) {
		CheckAction checkAction = ActionFactory.newApplicationAction(CheckAction.class, CheckActionOp.class);

		checkAction.setActualValue(actualValue);
		checkAction.setConstraints(checks);

		return checkAction;
	}

	/**
	 * Creates a {@link DownloadValue}.
	 */
	public static DownloadValue downloadValue(String downloadName) {
		DownloadValue downloadValue = TypedConfiguration.newConfigItem(DownloadValue.class);
		downloadValue.setFileName(downloadName);
		return downloadValue;
	}

	/**
	 * Convenience method for creating and filling an {@link AwaitProgressAction}.
	 * 
	 * @param maxWait
	 *        In milliseconds
	 */
	public static AwaitProgressAction awaitProgressAction(long maxWait, ComponentName componentName) {
		AwaitProgressAction action =
			ActionFactory.newApplicationAction(AwaitProgressAction.class, AwaitProgressActionOp.class);
		action.setMaxSleep(maxWait);
		action.setComponentName(componentName);
		return action;
	}

	/**
	 * Convenience method for creating and filling an {@link CollapseToolbarAction}.
	 * 
	 * @param toolbarOwner
	 *        Value of {@link CollapseToolbarAction#getExpandable()}.
	 * @param state
	 *        Value of {@link CollapseToolbarAction#getExpansionState()}.
	 */
	public static CollapseToolbarAction collapseToolbar(ModelName toolbarOwner, ExpansionState state) {
		CollapseToolbarAction action =
			ActionFactory.newApplicationAction(CollapseToolbarAction.class, CollapseToolbarActionOp.class);
		action.setExpandable(toolbarOwner);
		action.setExpansionState(state);
		return action;
	}

	/**
	 * Convenience method for creating an filling a {@link SetGlobalVariableAction}.
	 * 
	 * @see GlobalVariableStore
	 * 
	 * @param variableName
	 *        Must not be <code>null</code>.
	 * @param newValue
	 *        Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static SetGlobalVariableAction setGlobalVariableAction(String variableName, ModelName newValue) {
		checkGlobalVariableName(variableName);
		SetGlobalVariableAction action = newApplicationAction(SetGlobalVariableAction.class);
		action.setName(variableName);
		action.setValue(newValue);
		return action;
	}

	/**
	 * Convenience method for creating an filling a {@link GlobalVariableAction} for an
	 * {@link DelGlobalVariableOp}.
	 * 
	 * @see GlobalVariableStore
	 * 
	 * @param variableName
	 *        Must not be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static GlobalVariableAction delGlobalVariableAction(String variableName) {
		checkGlobalVariableName(variableName);
		GlobalVariableAction action = newApplicationAction(GlobalVariableAction.class, DelGlobalVariableOp.class);
		action.setName(variableName);
		return action;
	}

	/**
	 * Convenience method for creating an {@link ApplicationAction} for a
	 * {@link ClearGlobalVariablesOp}.
	 * 
	 * @see GlobalVariableStore
	 * 
	 * @return Never <code>null</code>.
	 */
	public static ApplicationAction clearGlobalVariablesAction() {
		return newApplicationAction(ApplicationAction.class, ClearGlobalVariablesOp.class);
	}

	/**
	 * Convenience method for creating an filling an {@link GlobalVariableExistenceAssertion}.
	 * 
	 * @see GlobalVariableStore
	 * 
	 * @param variableName
	 *        Must not be <code>null</code>.
	 * @param existing
	 *        Has the global variable to exist, or to not exist?
	 * @return Never <code>null</code>.
	 */
	public static GlobalVariableExistenceAssertion assertGlobalVariableExistence(String variableName, boolean existing) {
		checkGlobalVariableName(variableName);
		GlobalVariableExistenceAssertion action = newApplicationAction(GlobalVariableExistenceAssertion.class);
		action.setName(variableName);
		action.setExisting(existing);
		return action;
	}

	private static void checkGlobalVariableName(String variableName) {
		if (variableName == null) {
			throw new NullPointerException("The name of a global variable must not be null");
		}
	}

	/**
	 * Convenience method for creating an filling an {@link FuzzyGoto}.
	 * 
	 * @param componentName
	 *        Must not be <code>null</code>.
	 * @return Never <code>null</code>.
	 */
	public static FuzzyGoto fuzzyGoto(ModelName componentName) {
		FuzzyGoto action = newApplicationAction(FuzzyGotoActionOp.FuzzyGoto.class);
		action.setComponent(componentName);
		return action;
	}

	/**
	 * Convenience method for creating an filling a {@link ExpandAction} for an
	 * {@link ExpandActionOp}.
	 * 
	 * @see ExpandAction
	 * 
	 * @return Never <code>null</code>.
	 */
	public static ExpandAction expandAction(ModelName treeData, ModelName path, boolean expand) {
		ExpandAction action = newApplicationAction(ExpandAction.class, ExpandActionOp.class);
		action.setModelName(treeData);
		action.setPath(path);
		action.setExpand(expand);
		return action;
	}

	/**
	 * @see LogMessageAction
	 */
	public static LogMessageAction logMessageAction(String message, String level) {
		LogMessageAction action = newApplicationAction(LogMessageAction.class, LogMessageActionOp.class);
		action.setMessage(message);
		action.setLevel(level);
		return action;
	}

	/** @see RecordingFailedAction */
	public static RecordingFailedAction recordingFailedAction(String message, String printedCause) {
		RecordingFailedAction action = newApplicationAction(RecordingFailedAction.class, RecordingFailedActionOp.class);
		action.setMessage(message);
		action.setPrintedCause(printedCause);
		return action;
	}

	/**
	 * Creates an {@link ComponentAction} that closes the dialog in which the given component is
	 * shown.
	 * 
	 * @see CloseDialogActionOp
	 */
	public static ComponentAction closeDialogAction(LayoutComponent component) {
		return newComponentAction(ComponentAction.class, CloseDialogActionOp.class, component);
	}

	/**
	 * Creates a new instance of {@link ApplicationAction} of the given type and sets the given
	 * implementation class.
	 * 
	 * @param implClass
	 *        the actual implementation class
	 * @return A new application action instance of the given type
	 * 
	 * @since 5.7.2
	 * 
	 * @see #newApplicationAction(Class)
	 */
	public static <T extends ApplicationAction> T newApplicationAction(Class<? extends T> actionType,
			Class<? extends ApplicationActionOp<?>> implClass) {
		T applicationAction = newApplicationAction(actionType);
		applicationAction.setImplementationClass(implClass);
		return applicationAction;
	}

	/**
	 * Creates a new instance of {@link ApplicationAction} of the given type.
	 * 
	 * @param actionType
	 *        The type of action to create instance for
	 * @return A new application action instance of the given type
	 * 
	 * @since 5.7.2
	 */
	public static <T extends ApplicationAction> T newApplicationAction(Class<T> actionType) {
		return TypedConfiguration.newConfigItem(actionType);
	}

	/**
	 * Creates a new instance of {@link ComponentAction} for the given component.
	 * 
	 * @param implClass
	 *        the actual implementation class
	 * @return A new application action instance of the given type
	 * 
	 * @see #newApplicationAction(Class, Class)
	 */
	public static <T extends ComponentAction> T newComponentAction(Class<? extends T> actionType,
			Class<? extends ApplicationActionOp<?>> implClass, LayoutComponent component) {
		T action = newApplicationAction(actionType, implClass);
		action.setComponentName(component.getName());
		action.setComponentImplementationComment(component.getClass().getName());
		return action;
	}

}
