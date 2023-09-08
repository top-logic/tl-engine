/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.annotations;

import java.util.Optional;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.demo.model.types.DemoTypesAll;
import com.top_logic.demo.model.types.PX;
import com.top_logic.model.TLObject;
import com.top_logic.model.access.DeleteConstraint;
import com.top_logic.model.search.constraint.DeleteConstraintByExpression;

/**
 * Example for a {@link DeleteConstraint} implemented in code.
 * 
 * <p>
 * Note: A better alternative is to use configured constraints:
 * {@link DeleteConstraintByExpression}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoDeleteConstraint implements DeleteConstraint {

	/**
	 * Configuration options for {@link DemoDeleteConstraint}.
	 */
	public interface Config<I extends DemoDeleteConstraint> extends PolymorphicConfiguration<I> {
		// Would allow adding custom properties. Here in the demo, it just ensures that the correct
		// configuration type is passed to the constructor.
	}

	/**
	 * Creates a {@link DemoDeleteConstraint} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DemoDeleteConstraint(InstantiationContext context, Config<?> config) {
		super();
	}

	@Override
	public Optional<ResKey> getDeleteVeto(TLObject obj) {
		if (Utils.isTrue((Boolean) obj.tValueByName(PX.ALTERNATIVE_PROTECTED_ATTR))) {
			return Optional.of(I18NConstants.ALTERNATIVE_PROTECTED__OBJ.fill(obj.tValueByName(DemoTypesAll.NAME_ATTR)));
		}
		return Optional.empty();
	}

}
