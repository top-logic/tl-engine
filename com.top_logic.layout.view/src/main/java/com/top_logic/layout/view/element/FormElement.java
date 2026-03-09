/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.form.FormCommandModel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLObject;
import com.top_logic.util.Resources;

/**
 * Declarative {@link UIElement} that wraps a {@link FormControl}.
 *
 * <p>
 * Establishes a form scope for child elements. Resolves an input channel to obtain the object to
 * display, creates a {@link FormControl}, and makes it available to nested {@link FieldElement}s
 * via the {@link ViewContext}.
 * </p>
 */
public class FormElement extends ContainerElement {

	/**
	 * Configuration for {@link FormElement}.
	 */
	@TagName("form")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getEditMode()}. */
		String EDIT_MODE = "editMode";

		/** Configuration name for {@link #getDirty()}. */
		String DIRTY = "dirty";

		/** Configuration name for {@link #getNoModelMessage()}. */
		String NO_MODEL_MESSAGE = "noModelMessage";

		/** Configuration name for {@link #getWithEditMode()}. */
		String WITH_EDIT_MODE = "withEditMode";

		@Override
		@ClassDefault(FormElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Reference to the channel providing the object to display in the form.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Optional reference to a channel for publishing edit mode state.
		 */
		@Name(EDIT_MODE)
		@Format(ChannelRefFormat.class)
		ChannelRef getEditMode();

		/**
		 * Optional reference to a channel for publishing the dirty state.
		 */
		@Name(DIRTY)
		@Format(ChannelRefFormat.class)
		ChannelRef getDirty();

		/**
		 * Message to display when no object is available.
		 *
		 * <p>
		 * Defaults to "No object selected." if not specified.
		 * </p>
		 */
		@Name(NO_MODEL_MESSAGE)
		ResKey getNoModelMessage();

		/**
		 * Whether to create edit/save/cancel command models for this form.
		 *
		 * <p>
		 * When {@code true}, command models for edit, save, and cancel are created and contributed
		 * to the enclosing {@link CommandScope}. This allows a parent panel to render toolbar
		 * buttons for switching between view and edit mode without the {@link FormControl} knowing
		 * about UI elements.
		 * </p>
		 */
		@Name(WITH_EDIT_MODE)
		@BooleanDefault(false)
		boolean getWithEditMode();
	}

	private final Config _config;

	/**
	 * Creates a new {@link FormElement} from configuration.
	 */
	@CalledByReflection
	public FormElement(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Resolve input channel.
		ViewChannel inputChannel = context.resolveChannel(_config.getInput());

		// 2. Get initial object from input channel.
		TLObject initialObject = (TLObject) inputChannel.get();

		// 3. Resolve no-model message.
		ResKey messageKey = _config.getNoModelMessage();
		if (messageKey == null) {
			messageKey = I18NConstants.FORM_NO_MODEL;
		}
		String noModelMessage = Resources.getInstance().getString(messageKey);

		// 4. Create FormControl with initial object.
		FormControl formControl = new FormControl(context, initialObject, noModelMessage);

		// 5. Wire channels.
		formControl.setInputChannel(inputChannel);

		ChannelRef editModeRef = _config.getEditMode();
		if (editModeRef != null) {
			formControl.setEditModeChannel(context.resolveChannel(editModeRef));
		}

		ChannelRef dirtyRef = _config.getDirty();
		if (dirtyRef != null) {
			formControl.setDirtyChannel(context.resolveChannel(dirtyRef));
		}

		// 6. Create edit mode command models if configured.
		if (_config.getWithEditMode()) {
			contributeEditCommands(context, formControl);
		}

		// 7. Create a child context with the form control set, so that
		// nested FieldElements can access it without polluting the parent context.
		ViewContext formContext = context.childContext("form");
		formContext.setFormControl(formControl);

		// 8. Create child controls in the form-scoped context.
		List<IReactControl> childControls = createChildControls(formContext);

		// 9. Convert to ReactControl list and put as state.
		List<ReactControl> reactChildren = childControls.stream()
			.map(c -> (ReactControl) c)
			.collect(Collectors.toList());
		formControl.setChildren(reactChildren);

		return formControl;
	}

	private void contributeEditCommands(ViewContext context, FormControl formControl) {
		CommandScope scope = context.getCommandScope();
		if (scope == null) {
			return;
		}

		List<FormCommandModel> models = new ArrayList<>();
		models.add(FormCommandModel.editCommand(formControl));
		models.add(FormCommandModel.saveCommand(formControl));
		models.add(FormCommandModel.cancelCommand(formControl));

		for (FormCommandModel model : models) {
			model.attach();
			scope.addCommand(model);
		}

		formControl.addCleanupAction(() -> {
			for (FormCommandModel model : models) {
				scope.removeCommand(model);
				model.detach();
			}
		});
	}
}
