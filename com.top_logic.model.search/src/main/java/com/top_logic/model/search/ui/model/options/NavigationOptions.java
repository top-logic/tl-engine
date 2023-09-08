/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function3;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.AbstractStep;
import com.top_logic.model.search.ui.model.AssociationStep;
import com.top_logic.model.search.ui.model.AttributeStep;
import com.top_logic.model.search.ui.model.IncomingReferenceStep;
import com.top_logic.model.search.ui.model.NavigationStep;
import com.top_logic.model.search.ui.model.TypeCheckStep;

/**
 * Option provider for {@link AbstractStep#getNext()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NavigationOptions extends Function3<List<NavigationStep>, TLType, Boolean, String> {

	/** The {@link NavigationOptions} instance. */
	public static final NavigationOptions INSTANCE = new NavigationOptions();

	@Override
	public List<NavigationStep> apply(TLType type, Boolean expectedMultiplicity, String searchName) {
		if (containsNull(type, expectedMultiplicity, searchName)) {
			return emptyList();
		}
		if (!(type instanceof TLClass)) {
			return emptyList();
		}
		return getOptions((TLClass) type, expectedMultiplicity, searchName);
	}

	private List<NavigationStep> getOptions(TLClass tlClass, boolean expectedMultiplicity, String searchName) {
		List<NavigationStep> result = new ArrayList<>();
		if (SearchTypeUtil.hasParts(tlClass, searchName)) {
			result.add(attributeStep());
		}
		/* Associations and incoming references can always produce multiple results and must
		 * therefore not be used when only a single value is expected. */
		if (expectedMultiplicity) {
			if (SearchTypeUtil.isReferenced(tlClass, searchName)) {
				result.add(incomingReferenceNavigation());
			}
			if (SearchTypeUtil.isAssociated(tlClass, searchName)) {
				result.add(associationStep());
			}
		}
		if (SearchTypeUtil.hasSubtypes(tlClass, searchName)) {
			result.add(typeCheckStep());
		}
		return result;
	}

	private static AttributeStep attributeStep() {
		return TypedConfiguration.newConfigItem(AttributeStep.class);
	}

	private static IncomingReferenceStep incomingReferenceNavigation() {
		return TypedConfiguration.newConfigItem(IncomingReferenceStep.class);
	}

	private static AssociationStep associationStep() {
		return TypedConfiguration.newConfigItem(AssociationStep.class);
	}

	private static TypeCheckStep typeCheckStep() {
		return TypedConfiguration.newConfigItem(TypeCheckStep.class);
	}

}
