/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that lets the user write the text value of a channel.
 *
 * <p>
 * The input is bound to the {@link Config#getValue() value channel} in both directions: what the
 * user types becomes the value of the channel, and a value the channel receives from elsewhere
 * appears in the input. It is the counterpart of a {@code <field>} for a value that belongs to the
 * view rather than to a model object - the term a table filters by, the text a search runs on - and
 * needs no form and no object to hold it.
 * </p>
 *
 * <p>
 * The value is the text as written, and an emptied input clears the channel to the empty string.
 * </p>
 */
@InApp
public class TextInputElement implements UIElement {

	/**
	 * Configuration for {@link TextInputElement}.
	 */
	@TagName("text-input")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(TextInputElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/**
		 * The channel carrying the text the user writes.
		 */
		@Name(VALUE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getValue();

		/**
		 * The label shown beside the input.
		 *
		 * <p>
		 * Without one, the input stands alone, which is what an input in a toolbar or above a list
		 * does, where the surrounding display says what it is for.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();
	}

	private final ChannelRef _valueRef;

	private final ResKey _label;

	/**
	 * Creates a new {@link TextInputElement} from configuration.
	 */
	@CalledByReflection
	public TextInputElement(InstantiationContext context, Config config) {
		_valueRef = config.getValue();
		_label = config.getLabel();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel channel = context.resolveChannel(_valueRef);

		AbstractFieldModel field = new AbstractFieldModel(text(channel.get()));
		field.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				channel.set(text(newValue));
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// The input is the only writer of the field, so nothing else has to follow it.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Nothing validates the text: any text is a value.
			}
		});

		ReactTextInputControl input = new ReactTextInputControl(context, field);
		ChannelListener listener = (sender, oldValue, newValue) -> field.setValue(text(newValue));
		channel.addListener(listener);
		input.addCleanupAction(() -> channel.removeListener(listener));

		if (_label == null) {
			return input;
		}
		return new ReactFormFieldChromeControl(context, Resources.getInstance().getString(_label), input);
	}

	/**
	 * The given value as the text of the input.
	 */
	private static String text(Object value) {
		return value == null ? "" : value.toString();
	}

}
