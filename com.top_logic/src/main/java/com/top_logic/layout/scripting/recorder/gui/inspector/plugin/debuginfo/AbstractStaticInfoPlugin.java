/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.tag.Icons;
import com.top_logic.layout.inspector.OpenSeparateInspectorWindowCommandHandler;
import com.top_logic.layout.provider.ImageButtonControlProvider;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;

/**
 * {@link DebugInfoPlugin} that simply displays its {@link #getModel()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractStaticInfoPlugin<M> extends DebugInfoPlugin<M> {

	private static final String INSPECTOR_WINDOW = "openInspector";

	private static final String INSPECT_FIELD = "inspect";

	private static final String VALUE_FIELD = "value";

	private Object _inspectModel;

	/**
	 * Constructor creates a new {@link AbstractStaticInfoPlugin}.
	 */
	public AbstractStaticInfoPlugin(M model, ResPrefix resPrefix, String internalName) {
		this(model, resPrefix, internalName, isTrivial(model) ? null : model);
	}

	/**
	 * Constructor creates a new {@link AbstractStaticInfoPlugin}.
	 */
	public AbstractStaticInfoPlugin(M model, ResPrefix resPrefix, String internalName, Object inspectModel) {
		super(model, resPrefix, internalName);

		_inspectModel = inspectModel;
	}

	@Override
	protected FormMember createInformationField(String name) {
		M model = getModel();
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		FormGroup content = new FormGroup(name, getI18nPrefix());
		// Make label of group explicit to be able to set same label for actual content field
		String groupLabel = displayContext.getResources().getString(getI18nPrefix().key(name));
		content.setLabel(groupLabel);

		FormMember valueField;
		if (model == null) {
			valueField = createNullField(VALUE_FIELD);
		} else {
			valueField = createValueField(model, VALUE_FIELD);
		}
		if (!valueField.hasLabel()) {
			valueField.setLabel(content.getLabel());
		}
		content.addMember(valueField);

		MainLayout ml = displayContext.getLayoutContext().getMainLayout();
		CommandField inspectButton = createInspectButton(getI18nPrefix(), ml, _inspectModel);
		if (inspectButton != null) {
			content.addMember(inspectButton);
		}

		return content;
	}

	/**
	 * Creates a {@link CommandField} to inspect the given model.
	 * 
	 * @param buttonPrefix
	 *        Prefix to compute a specialised label for the button.
	 * @param inspectModel
	 *        The model to inspect.
	 * 
	 * @return A {@link CommandField} to inspect the given model, or <code>null</code> when the
	 *         given model can not be inspected.
	 */
	public static CommandField createInspectButton(ResPrefix buttonPrefix, MainLayout ml, Object inspectModel) {
		if (inspectModel == null) {
			return null;
		}
		CommandField inspectButton = createInspectButton(ml, INSPECT_FIELD, inspectModel);
		if (inspectButton == null) {
			return null;
		}
		inspectButton.setImage(Icons.OPEN_CHOOSER);
		inspectButton.setControlProvider(ImageButtonControlProvider.INSTANCE);
		inspectButton.setCssClasses("appended");

		ResKey detailLabelKey =
			ResKey.fallback(buttonPrefix.key(inspectButton.getName()), I18NConstants.SHOW_DETAIL_COMMAND);
		inspectButton.setLabel(Resources.getInstance().getString(detailLabelKey));
		return inspectButton;
	}

	/**
	 * Creates a button that opens the given model in the object inspector.
	 * 
	 * @param layout
	 *        The layout root.
	 * @param fieldName
	 *        The name of the button to create.
	 * @param model
	 *        The model to inspect.
	 * @return The button that opens the inspector.
	 */
	public static CommandField createInspectButton(MainLayout layout, String fieldName, Object model) {
		CommandHandler handler = layout.getCommandById(INSPECTOR_WINDOW);
		Map<String, Object> arguments =
			Collections.singletonMap(OpenSeparateInspectorWindowCommandHandler.PARAM_OBJECT, model);
		return FormFactory.newCommandField(fieldName, handler, layout, arguments);
	}

	/**
	 * {@link FormMember} for <code>null</code> model.
	 * 
	 * @see #createValueField(Object, String)
	 */
	protected FormMember createNullField(String fieldName) {
		return FormFactory.newStringField(fieldName, "-", FormFactory.IMMUTABLE);
	}

	/**
	 * {@link FormMember} displaying non <code>null</code> model.
	 * 
	 * @see #createNullField(String)
	 */
	protected abstract FormMember createValueField(M model, String fieldName);

	private static boolean isTrivial(Object model) {
		if (model == null) {
			return true;
		}
		Class<? extends Object> type = model.getClass();
		if (type == ThemeImage.class) {
			return true;
		}
		String className = type.getName();
		return className.startsWith("java.");
	}


}

