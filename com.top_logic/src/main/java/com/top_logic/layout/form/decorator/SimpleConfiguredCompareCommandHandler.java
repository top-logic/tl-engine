/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.compare.CompareAlgorithm;
import com.top_logic.layout.compare.CompareAlgorithmHolder;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link AbstractCompareCommandHandler} which delegates the
 * {@link #createCompareObject(DisplayContext, FormComponent, Map) compare object creation} to a
 * {@link Config#getAlgorithm() configured algorithm}.
 * 
 * @see Config#getAlgorithm()
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class SimpleConfiguredCompareCommandHandler<C extends FormComponent & CompareAlgorithmHolder>
		extends AbstractCompareCommandHandler<C> {

	/**
	 * Configuration of the {@link SimpleConfiguredCompareCommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCompareCommandHandler.Config {

		/**
		 * The algorithm that creates the compare object.
		 */
		@Mandatory
		PolymorphicConfiguration<CompareObjectCreator> getAlgorithm();

	}

	/**
	 * Algorithm creating the {@link CompareAlgorithm} for tue given component.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface CompareObjectCreator {

		/**
		 * Creates the {@link CompareAlgorithm} for the given component.
		 * 
		 * @param context
		 *        Current interaction.
		 * @param component
		 *        The component to create {@link CompareAlgorithm} for. It is also an instance of
		 *        {@link CompareAlgorithmHolder}.
		 * @param someArguments
		 *        Arguments of the command execution.
		 * 
		 * @return May be <code>null</code>, when no comparison is possible.
		 */
		CompareAlgorithm createCompareObject(DisplayContext context, FormComponent component,
				Map<String, Object> someArguments);

	}

	private final CompareObjectCreator _algorithm;

	/**
	 * Creates a new {@link SimpleConfiguredCompareCommandHandler}.
	 */
	public SimpleConfiguredCompareCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
		_algorithm = context.getInstance(config.getAlgorithm());
	}

	@Override
	protected void setCompareObject(C component, Object anotherModel) {
		component.setCompareAlgorithm((CompareAlgorithm) anotherModel);
	}

	@Override
	protected Object getCompareObject(C component) {
		return component.getCompareAlgorithm();
	}

	@Override
	protected void adaptCommandModel(DisplayContext context, CommandModel model, LayoutComponent component,
			boolean comparisonActive) {
		super.adaptCommandModel(context, model, component, comparisonActive);
		ThemeImage image;
		if (comparisonActive) {
			image = Icons.COMPARE_MODE_INACTIVE;
		} else {
			image = Icons.COMPARE_MODE_ACTIVE;
		}
		model.setImage(image);
	}

	@Override
	protected Object createCompareObject(DisplayContext context, C component, final Map<String, Object> someArguments) {
		return _algorithm.createCompareObject(context, component, someArguments);
	}

}
