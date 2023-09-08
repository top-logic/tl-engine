/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util.scripting;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * {@link ModelNamingScheme} for {@link TLModelPart} of the application model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLModelPartNaming extends GlobalModelNamingScheme<TLModelPart, TLModelPartNaming.Name> {

	/**
	 * {@link ModelName} referencing a part of the application model.
	 */
	public interface Name extends ModelName {
		/**
		 * The name of the referenced {@link TLModelPart}.
		 */
		String getRef();

		/**
		 * @see #getRef()
		 */
		void setRef(String value);
	}

	/**
	 * Creates a {@link TLModelPartNaming}.
	 */
	public TLModelPartNaming() {
		super(TLModelPart.class, Name.class);
	}

	@Override
	public Maybe<Name> buildName(TLModelPart model) {
		if (ModelService.getApplicationModel() != model.getModel()) {
			// Such part would not be globally resolvable.
			return Maybe.none();
		}

		String ref = TLModelUtil.qualifiedName(model);
		Name name = TypedConfiguration.newConfigItem(Name.class);
		name.setRef(ref);

		return Maybe.some(name);
	}

	@Override
	public TLModelPart locateModel(ActionContext context, Name name) {
		return (TLModelPart) TLModelUtil.resolveQualifiedName(name.getRef());
	}

}
