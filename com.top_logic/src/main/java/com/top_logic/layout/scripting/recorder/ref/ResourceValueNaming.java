/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.util.Map;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.Resource;

/**
 * {@link ValueNamingScheme} for {@link Resource}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResourceValueNaming extends ValueNamingScheme<Resource> {

	@Override
	public Class<Resource> getModelClass() {
		return Resource.class;
	}

	@Override
	public Map<String, Object> getName(Resource model) {
		return new MapBuilder<String, Object>()
			.put("userObject", model.getUserObject())
			.put("label", model.getLabel())
			.put("image", model.getImage())
			.put("tooltip", model.getTooltip())
			.put("link", model.getLink())
			.put("type", model.getType())
			.put("cssClass", model.getCssClass())
			.toMap();
	}

	@Override
	public boolean matches(Map<String, Object> name, Resource model) {
		return Utils.equals(name.get("userObject"), model.getUserObject()) &&
				Utils.equals(name.get("label"), model.getLabel()) &&
				Utils.equals(name.get("image"), model.getImage()) &&
				Utils.equals(name.get("tooltip"), model.getTooltip()) &&
				Utils.equals(name.get("link"), model.getLink()) &&
				Utils.equals(name.get("type"), model.getType()) &&
				Utils.equals(name.get("cssClass"), model.getCssClass())
		;
	}

}

