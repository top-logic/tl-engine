/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import static java.util.Collections.*;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLType;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.tool.boundsec.commandhandlers.OpenViewForModelCommand;

/**
 * Handler to open a top-logic object link in a Popup Dialog.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class OpenTLObjectLink extends ControlCommand {


	/**
	 * Singleton {@link OpenTLObjectLink} instance.
	 */
	public static final OpenTLObjectLink INSTANCE = new OpenTLObjectLink();

	/** The default command id for opening the views. */
	public static final String DEFAULT_OPEN_VIEW_COMMAND_ID = OpenViewForModelCommand.Config.ID;

	/** The {@link Property} used in {@link #getAnnotatedCommandMapping(TypedAnnotatable)}. */
	@SuppressWarnings({ "unchecked" })
	public static final Property<Function<TLType, String>> COMMAND_MAPPING_ANNOTATION =
		TypedAnnotatable.propertyRaw(Function.class, "open-view-command-mapping");

	private static final String OBJECT_ARGUMENT = "object";

	private static final String SECTION_ARGUMENT = "section";

	/** Command ID of {@link OpenTLObjectLink} */
	public static final String COMMAND_ID = "openTLObjectLink";

	/**
	 * Creates a {@link OpenTLObjectLink} command with the given id.
	 */
	public OpenTLObjectLink() {
		super(COMMAND_ID);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.OPEN_TL_OBJECT_LINK;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		String objectArgument = (String) arguments.get(OBJECT_ARGUMENT);
		Wrapper object = TLObjectLinkUtil.getObject(objectArgument);
		if (object != null) {
			goToObject(commandContext, control, object);

			String gotoSectionArgument = (String) arguments.get(SECTION_ARGUMENT);
			if (gotoSectionArgument != null) {
				FrameScope frameScope = commandContext.getWindowScope().getTopLevelFrameScope();
				frameScope.addClientAction(new JSSnipplet(new AbstractDisplayValue() {

					@Override
					public void append(DisplayContext context, Appendable out) throws IOException {
						out.append("services.wysiwyg.StructuredText.goto('");
						out.append(gotoSectionArgument);
						out.append("');");
					}
				}));
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private void goToObject(DisplayContext commandContext, Control control, Wrapper object) {
		LayoutComponent departureComponent = getComponent(commandContext, control);
		CommandHandler command = getCommand(departureComponent, control, object.tType());
		command.handleCommand(commandContext, departureComponent, object, emptyMap());
	}

	/**
	 * Tries to retrieve the {@link LayoutComponent} in which this {@link Control} is displayed.
	 * 
	 * @return Null, if none is found.
	 */
	private LayoutComponent getComponent(DisplayContext commandContext, Control control) {
		LayoutComponent componentFromControl = getComponent(control);
		if (componentFromControl != null) {
			return componentFromControl;
		}
		LayoutComponent contextComponent = MainLayout.getComponent(commandContext);
		if (contextComponent != null) {
			return contextComponent;
		}
		return MainLayout.getMainLayout(commandContext);
	}

	private LayoutComponent getComponent(Control control) {
		FormField field = getField(control);
		if (field == null) {
			return null;
		}
		FormHandler formHandler = field.getFormContext().getOwningModel();
		if (formHandler instanceof LayoutComponent) {
			return (LayoutComponent) formHandler;
		}
		return null;
	}

	private CommandHandler getCommand(LayoutComponent component, Control control, TLType type) {
		CommandHandlerFactory handlerFactory = CommandHandlerFactory.getInstance();
		return handlerFactory.getHandler(getOpenCommandId(component, control, type));
	}

	/** The id of the {@link CommandHandler} that should open the view. */
	private String getOpenCommandId(LayoutComponent component, Control control, TLType type) {
		Function<TLType, String> commandMapping = getCommandMapping(component, control);
		if (commandMapping == null) {
			return DEFAULT_OPEN_VIEW_COMMAND_ID;
		}
		String commandId = commandMapping.apply(type);
		if (StringServices.isEmpty(commandId)) {
			return DEFAULT_OPEN_VIEW_COMMAND_ID;
		}
		return commandId;
	}

	private Function<TLType, String> getCommandMapping(LayoutComponent component, Control control) {
		FormField field = getField(control);
		Function<TLType, String> fromField = getAnnotatedCommandMapping(field);
		if (fromField != null) {
			return fromField;
		}
		Function<TLType, String> fromComponent = getAnnotatedCommandMapping(component);
		if (fromComponent != null) {
			return fromComponent;
		}
		return null;
	}

	/**
	 * Tries to retrieve the {@link FormField} that is displayed by the {@link Control}.
	 * 
	 * @return Null, if none is found.
	 */
	private FormField getField(Control control) {
		Object controlModel = control.getModel();
		if (!(controlModel instanceof FormField)) {
			return null;
		}
		return (FormField) controlModel;
	}

	/** @return Null, if there is no annotation. */
	private Function<TLType, String> getAnnotatedCommandMapping(TypedAnnotatable annotatable) {
		if (annotatable == null) {
			return null;
		}
		return annotatable.get(COMMAND_MAPPING_ANNOTATION);
	}

	/**
	 * Annotate the given {@link FormField} or {@link LayoutComponent} with a mapping for the ID of
	 * the {@link CommandHandler} that should be used for opening the view.
	 * <p>
	 * The annotated mapping is a {@link Function} from a {@link TLType} to the id of the
	 * {@link CommandHandler} that should display instances of that type.
	 * </p>
	 * <p>
	 * The mapping returns null, when no {@link CommandHandler} is mapped for the given
	 * {@link TLType}.
	 * </p>
	 * <p>
	 * The {@link CommandHandler} is called with the object as its target model. The
	 * {@link OpenViewForModelCommand} is an example for such a {@link CommandHandler}. The
	 * {@link GotoHandler} does NOT work: It expects the target model in a custom parameter, not in
	 * the default target model parameter.
	 * </p>
	 */
	public static void annotateCommandId(TypedAnnotatable annotatable, Function<TLType, String> commandMapping) {
		annotatable.set(COMMAND_MAPPING_ANNOTATION, commandMapping);
	}

}
