/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ModelSpec;

/**
 * Configuration options of {@link LayoutComponent} that are displayed in simplified templates for
 * component configuration.
 * 
 * @see LayoutComponent.Config
 */
public interface LayoutComponentUIOptions extends ConfigurationItem {

	/** @see #getTitleKey() */
	String TITLE_KEY = "titleKey";

	/** @see #getModelSpec() */
	String MODEL = "model";

	/**
	 * Internationalized title of this component.
	 * 
	 * <p>
	 * If the component has a tile bar, this title is shown there.
	 * </p>
	 */
	@Label("Title")
	@Name(TITLE_KEY)
	@InstanceFormat
	ResKey getTitleKey();

	/** @see #getTitleKey() */
	void setTitleKey(ResKey value);


	/**
	 * Source of the component's model.
	 * 
	 * <p>
	 * A component typically displays some content based on a model. The component's model is a
	 * single object, on which the component operates. Based on this model, a table component e.g.
	 * produces a list of row objects to be displayed. In most cases, the component's model is not
	 * displayed directly, but some aspects derived from this model. Another example is a form
	 * component. A form typically displays several fields for editing some properties of the
	 * component's model.
	 * </p>
	 * 
	 * <p>
	 * The component's model can be received from other components through channels. A common use
	 * case is to receive a model from another component's selection channel. In such a
	 * configuration, the model of the receiving component changes whenever a user selects a new
	 * value in the sending component. All components have a <code>model</code> channel. Several
	 * components such as tables and trees have also a <code>selection</code> channel. Besides these
	 * common channels, there may be components with more specialized channels.
	 * </p>
	 * 
	 * @implNote
	 *           <p>
	 *           Receiving the model from another component's channel is configured with either
	 *           <code>model([other])</code>) or the selection (<code>selection([other])</code>),
	 *           where <code>[other]</code> determines the other component to receive from. This
	 *           other component is either specified by name (<code>[component-name]</code>) or
	 *           through a relation, e.g. the dialog parent relation ( <code>dialogParent()</code>).
	 *           </p>
	 * 
	 *           <p>
	 *           Examples for valid values of the {@link #MODEL} configuration are the following:
	 *           </p>
	 * 
	 *           <dl>
	 *           <dt>selection(projectTable)</dt>
	 *           <dd>The selection of the table component named "projectTable".</dd>
	 * 
	 *           <dt>model(projectEdit)</dt>
	 *           <dd>The same model as in the editor named "projectEdit".</dd>
	 * 
	 *           <dt>model(dialogParent())</dt>
	 *           <dd>The same model as in the {@link LayoutComponent#getDialogParent() dialog parent
	 *           component}.</dd>
	 *           </dl>
	 * 
	 *           <p>
	 *           In rare cases, a component may receive its model from multiple sources. In that
	 *           case, the {@link #MODEL} value is a comma separated list of such model
	 *           specifications listed above.
	 *           </p>
	 */
	@Name(MODEL)
	ModelSpec getModelSpec();

	/**
	 * @see #getModelSpec()
	 */
	void setModelSpec(ModelSpec value);

}