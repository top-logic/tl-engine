/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ImageProvider;

/**
 * Properties to customize templates for {@link FormEditorElementControl}. Values of properties must
 * <b>not</b> be <code>null</code>.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorElementProperties {

	/** The id of the form editor. */
	public static final Property<String> FORM_EDITOR_CONTROL =
		TypedAnnotatable.property(String.class, "formEditorControl");

	/** The id to identify the form editor element at the GUI. */
	public static final Property<String> FORM_EDITOR_DATA_ID =
		TypedAnnotatable.property(String.class, "dataId");

	/** Whether the form editor element is draggable. */
	public static final Property<Boolean> FORM_EDITOR_DRAGGABLE =
		TypedAnnotatable.property(Boolean.class, "draggable");

	/** Whether the icon+label mode is hidden and it is displayed as label+input. */
	public static final Property<Boolean> FORM_EDITOR_FIRST_ELEMENT_HIDDEN =
		TypedAnnotatable.property(Boolean.class, "firstElementHidden");

	/** The {@link ImageProvider} for the icon in icon+label mode. */
	public static final Property<ImageProvider> FORM_EDITOR_IMAGE_PROVIDER =
		TypedAnnotatable.property(ImageProvider.class, "imageProvider");

	/** Whether the form editor element is a tool. */
	public static final Property<Boolean> FORM_EDITOR_IS_TOOL =
		TypedAnnotatable.property(Boolean.class, "isTool");

	/** The name for the label of the form editor element. */
	public static final Property<String> FORM_EDITOR_NAME =
		TypedAnnotatable.property(String.class, "name");

	/** The label text for the element inside the toolbar of the form editor. */
	public static final Property<ResKey> FORM_EDITOR_LABEL =
		TypedAnnotatable.property(ResKey.class, "label");

	/** Whether the form editor element is rendered over the whole line. */
	public static final Property<Boolean> FORM_EDITOR_WHOLE_LINE =
		TypedAnnotatable.property(Boolean.class, "wholeLine");
}
