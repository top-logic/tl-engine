/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.locking;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.locking.strategy.ConfiguredLockStrategy;
import com.top_logic.base.locking.strategy.LockStrategy;
import com.top_logic.base.locking.token.Token;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.structured.StructuredElement;

/**
 * {@link LockStrategy} that creates {@link Token}s for all elements on the path to root of a given
 * {@link StructuredElement}.
 * 
 * <p>
 * Note: This is a specialized {@link LockStrategy} for {@link StructuredElement}s that could easily
 * be formulated using TL-Script:
 * </p>
 * 
 * <code>
 * &lt;tokens objects="m -> recursion($m, n -> $n.get(`tl.element:StructuredElement#parent`), 0)"/>
 * </code>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class AncestorsOrSelfLockStrategy<C extends AncestorsOrSelfLockStrategy.Config<?>>
		extends ConfiguredLockStrategy<C, StructuredElement> {

	/**
	 * Configuration options for {@link AncestorsLockStrategy}.
	 */
	@TagName("ancestors-or-self")
	public interface Config<I extends AncestorsOrSelfLockStrategy<?>> extends ConfiguredLockStrategy.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link AncestorsOrSelfLockStrategy} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AncestorsOrSelfLockStrategy(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public List<Token> createTokens(StructuredElement model) {
		ArrayList<Token> result = new ArrayList<>();
		while (model != null) {
			result.add(Token.newToken(getConfig().getKind(), model, getConfig().getAspect()));
			model = model.getParent();
		}
		return result;
	}

}
