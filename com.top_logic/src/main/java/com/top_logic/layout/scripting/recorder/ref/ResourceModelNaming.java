/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Resource;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link AbstractModelNamingScheme} for {@link ResourceModelNaming.Name}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResourceModelNaming extends AbstractModelNamingScheme<Resource, ResourceModelNaming.Name> {

	/**
	 * {@link ModelName} for {@link Resource}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Name extends ModelName {

		/**
		 * Name for the user object of the resource.
		 * 
		 * @see Resource#getUserObject()
		 */
		ModelName getUserObject();

		/**
		 * Setter for {@link #getUserObject()}.
		 */
		void setUserObject(ModelName value);

		/**
		 * Image of the resource.
		 * 
		 * @see Resource#getImage()
		 */
		ThemeImage getImage();

		/**
		 * Setter for {@link #getImage()}.
		 */
		void setImage(ThemeImage value);

		/**
		 * Label of the resource.
		 * 
		 * @see Resource#getLabel()
		 */
		@Nullable
		ResKey getLabel();

		/**
		 * Setter for {@link #getLabel()}.
		 */
		void setLabel(ResKey value);

		/**
		 * Tooltip of the resource.
		 * 
		 * @see Resource#getTooltip()
		 */
		@Nullable
		ResKey getTooltip();

		/**
		 * Setter for {@link #getTooltip()}.
		 */
		void setTooltip(ResKey value);

		/**
		 * Link of the resource.
		 * 
		 * @see Resource#getLink()
		 */
		@Nullable
		String getLink();

		/**
		 * Setter for {@link #getLink()}.
		 */
		void setLink(String value);

		/**
		 * Type of the resource.
		 * 
		 * @see Resource#getType()
		 */
		@Nullable
		String getType();

		/**
		 * Setter for {@link #getType()}.
		 */
		void setType(String value);

		/**
		 * CSS class of the resource.
		 * 
		 * @see Resource#getCssClass()
		 */
		@Nullable
		String getCSSClass();

		/**
		 * Setter for {@link #getCSSClass()}.
		 */
		void setCSSClass(String value);

	}

	/**
	 * Creates a new {@link ResourceModelNaming}.
	 */
	public ResourceModelNaming() {
		super(Resource.class, ResourceModelNaming.Name.class);
	}

	@Override
	protected void initName(Name name, Resource model) {
		name.setUserObject(ModelResolver.buildModelName(model.getUserObject()));
		name.setImage(model.getImage());
		name.setLabel(model.getLabel());
		name.setTooltip(model.getTooltip());
		name.setLink(model.getLink());
		name.setType(model.getType());
		name.setCSSClass(model.getCssClass());
	}

	@Override
	public Resource locateModel(ActionContext context, Name name) {
		return Resource.resourceFor(
			ModelResolver.locateModel(context, name.getUserObject()),
			name.getLabel(),
			name.getImage(),
			name.getTooltip(),
			name.getLink(),
			name.getType(),
			name.getCSSClass());
	}

}

