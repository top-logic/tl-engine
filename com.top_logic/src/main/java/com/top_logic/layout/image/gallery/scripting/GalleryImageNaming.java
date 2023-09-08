/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery.scripting;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.image.gallery.GalleryImage;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.util.Utils;

/**
 * {@link ValueNamingScheme} for {@link GalleryImage}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GalleryImageNaming extends ValueNamingScheme<GalleryImage> {

	private static final String NAME_KEY = "name";

	@Override
	public Class<GalleryImage> getModelClass() {
		return GalleryImage.class;
	}

	@Override
	public Map<String, Object> getName(GalleryImage model) {
		return Collections.<String, Object> singletonMap(NAME_KEY, model.getName());
	}

	@Override
	public boolean matches(Map<String, Object> name, GalleryImage model) {
		return Utils.equals(name.get(NAME_KEY), model.getName());
	}

}
