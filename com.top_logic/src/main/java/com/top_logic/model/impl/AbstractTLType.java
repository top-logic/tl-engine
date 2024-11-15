/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.util.AttributeSettings;

/**
 * Default base implementation of {@link TLType}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTLType extends AbstractTLModelPart implements TLType {

	private TLModule module;
	private TLScope scope;
	private String name;

	/* package protected */ AbstractTLType(TLModel model, String name) {
		super(model);
		this.name = name;
	}
	
	@Override
	public TLModule getModule() {
		return module;
	}

	@Override
	public void setModule(TLModule module) {
		this.module = module;
	}

	@Override
	public TLScope getScope() {
		return this.scope;
	}
	
	/*package protected*/ void internalSetScope(TLScope value) {
		this.scope = value;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(String value) {
		TLModule owner = module;
		if (owner != null) {
			// Note: The owner of this type keeps an index of all of its parts by name. When simply
			// changing the name, this index gets corrupted. Since changing a type name is only
			// possible, if the type is not owned, the type is temporarily removed from its owner.
			owner.getTypes().remove(this);
			this.name = value;
			owner.getTypes().add(this);
		} else {
			this.name = value;
		}
	}
	
	protected static <T extends TLTypePart> Map<String, T> initAllParts(List<? extends T> ...references) {
		int expectedSize = 0;
		for (List<? extends T> parts : references) {
			expectedSize += parts.size();
		}
		HashMap<String, T> result = MapUtil.newMap(expectedSize);
		for (List<? extends T> parts : references) {
			for (T part : parts) {
				String partName = part.getName();
				if (partName == null) {
					// Anonymous parts are not indexed.
					continue;
				}
				T clash = result.put(partName, part);
				if (clash != null) {
					throw new IllegalArgumentException("Duplicate name '" + partName + "'.");
				}
			}
		}
		return result;
	}
	
	/**
	 * The default {@link TLAnnotation} for the type.
	 */
	@FrameworkInternal
	public static <T extends TLAnnotation> T getDefaultsFromType(Class<T> annotationInterface, TLType type) {
		AttributeSettings attributeSettings = AttributeSettings.getInstance();
		return attributeSettings.getConfiguredTypeAnnotation(annotationInterface, type);
	}

}
