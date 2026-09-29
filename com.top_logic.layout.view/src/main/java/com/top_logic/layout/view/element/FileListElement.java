/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FileListControl;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.model.annotate.LabelPosition;

/**
 * Declarative {@link UIElement} that creates a {@link FileListControl} for a composition reference
 * whose composed objects carry a file.
 *
 * <p>
 * Must be nested inside a {@link FormElement}. Displays the composed objects as file chips (name,
 * size, download); in edit mode, files can be added by upload and removed per chip.
 * </p>
 *
 * <p>
 * Example configuration:
 * </p>
 *
 * <pre>
 * &lt;file-list attribute="attachments"/&gt;
 * </pre>
 */
public class FileListElement implements UIElement {

	/**
	 * Configuration for {@link FileListElement}.
	 */
	@TagName("file-list")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(FileListElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name for {@link #getDataAttribute()}. */
		String DATA_ATTRIBUTE = "data-attribute";

		/** Configuration name for {@link #getLabelPosition()}. */
		String LABEL_POSITION = "label-position";

		/**
		 * The name of the composition reference attribute on the parent object.
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();

		/**
		 * The name of the binary attribute on the composed type that stores the file.
		 *
		 * <p>
		 * If not set, the single binary-typed property of the composition target type is used.
		 * </p>
		 */
		@Name(DATA_ATTRIBUTE)
		@Nullable
		String getDataAttribute();

		/**
		 * Where the file list renders its label relative to the chips, e.g. {@code hide-label} for
		 * a label-less file list.
		 */
		@Name(LABEL_POSITION)
		@Nullable
		LabelPosition getLabelPosition();
	}

	private final Config _config;

	/**
	 * Creates a new {@link FileListElement} from configuration.
	 */
	@CalledByReflection
	public FileListElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		FormModel formModel = context.getFormModel();
		if (formModel == null) {
			throw new IllegalStateException(
				"FileListElement must be nested inside a FormElement. No FormModel found in ViewContext.");
		}

		// FormElement always sets a FormControl as the FormModel.
		FormControl formControl = (FormControl) formModel;

		FileListControl control = new FileListControl(
			context, formControl, _config.getAttribute(), _config.getDataAttribute());

		ReactFormFieldChromeControl chrome = new ReactFormFieldChromeControl(context,
			_config.getAttribute(), false, false, null, null,
			AttributeFieldControl.wirePosition(_config.getLabelPosition(), false), true, true, control);
		chrome.setAgentName(_config.getAttribute());
		control.setChrome(chrome);

		control.init();
		return chrome;
	}
}
