/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.tile;

import java.util.List;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.func.Function0;
import com.top_logic.demo.model.types.A;
import com.top_logic.demo.model.types.DemoTypesFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.ValueEditor;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.LiveActionContext;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.tiles.component.ComponentParameters;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * {@link ComponentParameters} for defining a {@link ModelName} of a {@link A#A_TYPE}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CustomDemoATile extends ComponentParameters {

	/**
	 * Function returning all object of type {@link A#A_TYPE}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AllA extends Function0<List<TLObject>> {

		@Override
		public List<TLObject> apply() {
			TLClass aType = DemoTypesFactory.getAType();
			return MetaElementUtil.getAllInstancesOf(aType, TLObject.class);
		}

	}

	/**
	 * {@link OptionMapping} that maps an {@link Object} to its {@link ModelName}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ModelNameMapping implements OptionMapping {

		@Override
		public Object toSelection(Object option) {
			return ModelResolver.buildModelName(option);
		}

		@Override
		public Object asOption(Iterable<?> allOptions, Object selection) {
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
			ActionContext context = new LiveActionContext(displayContext, MainLayout.getComponent(displayContext));
			return ModelResolver.locateModel(context, (ModelName) selection);
		}

	}

	/**
	 * The {@link ModelName} of the displayed object.
	 */
	@Options(fun = AllA.class, mapping = ModelNameMapping.class)
	@PropertyEditor(ValueEditor.class)
	@OptionLabels(MetaResourceProvider.class)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Mandatory
	ModelName getModelName();

	/**
	 * Setter for {@link #getModelName()}.
	 */
	void setModelName(ModelName name);

}

