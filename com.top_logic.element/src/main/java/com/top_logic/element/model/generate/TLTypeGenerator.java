/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link TLModelGenerator} creating java classes for a given {@link TLType}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TLTypeGenerator extends TLModelGenerator {

	private final TLType _type;

	/**
	 * Creates a new {@link TLTypeGenerator}.
	 * 
	 * @param packageName
	 *        see {@link TLModelGenerator#TLModelGenerator(String)}.
	 * @param type
	 *        {@link TLType} to create class for.
	 */
	public TLTypeGenerator(String packageName, TLType type) {
		super(packageName);
		_type = type;
	}

	/**
	 * {@link TLType} to generate java class for.
	 */
	protected TLType type() {
		return _type;
	}

	/**
	 * Returns the {@link TLType#getName() name} of the {@link #type() type}.
	 */
	protected String typeName() {
		return type().getName();
	}

	/**
	 * Returns the {@link TLType#getModule() module} of the {@link #type() type}.
	 */
	protected TLModule module() {
		return type().getModule();
	}

	/**
	 * Returns a sorted list of {@link TLClass} that are {@link TLClass#getGeneralizations()
	 * generalizations} of the given {@link TLType}.
	 * 
	 * @param type
	 *        The type to get generalizations for.
	 */
	protected List<TLClass> generalizations(TLType type) {
		if (!(type instanceof TLClass)) {
			return Collections.emptyList();
		}

		List<TLClass> generalizations = ((TLClass) type).getGeneralizations();
		List<TLClass> generalizationCopy = new ArrayList<>(generalizations.size());
		for (int i = 0, size = generalizations.size(); i < size; i++) {
			TLClass generalization = generalizations.get(i);
			generalizationCopy.add(generalization);
		}
		return generalizationCopy;
	}

	/**
	 * Whether the given type is read only, i.e. whether a setter must be generated for the given
	 * type.
	 */
	protected boolean isReadOnly(TLStructuredTypePart part) {
		return TLModelUtil.isDerived(part);
	}

}
