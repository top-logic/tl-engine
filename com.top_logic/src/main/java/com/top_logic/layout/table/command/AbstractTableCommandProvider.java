/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelRegistry;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.ExecutabilityPolling;
import com.top_logic.layout.table.TableData;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Base class for {@link TableCommandProvider}s that thake their visual aspects from their
 * configuration.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTableCommandProvider<C extends AbstractTableCommandProvider.Config<?>>
		extends AbstractConfiguredInstance<C>
		implements TableCommandProvider {

	/**
	 * Configuration options for {@link AbstractTableCommandProvider}.
	 */
	public interface Config<I extends AbstractTableCommandProvider<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The button image.
		 * 
		 * @see #getImageDisabled()
		 */
		ThemeImage getImage();

		/**
		 * The image to display, if the button gets disabled.
		 * 
		 * @see #getImage()
		 */
		ThemeImage getImageDisabled();

		/**
		 * Whether to lock the UI while executing.
		 */
		boolean getShowProgress();

		/**
		 * The label to to display.
		 */
		ResKey getLabel();

		/**
		 * The tooltip for the button.
		 */
		ResKey getTooltip();

		/**
		 * The caption displayed above the tooltip.
		 */
		ResKey getTooltipCaption();

		/**
		 * A custom CSS class to render.
		 */
		String getCSSClass();

	}

	/**
	 * Creates a {@link AbstractTableCommandProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractTableCommandProvider(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public CommandModel createCommand(TableData table) {
		TableCommand command = getTableCommand();

		class DynamicCommandModel extends AbstractCommandModel implements ExecutabilityPolling {
			@Override
			public void updateExecutabilityState() {
				DisplayContext context = DefaultDisplayContext.getDisplayContext();
				CommandStep step = command.prepareCommand(context, table, self());
				ExecutableState executability = step.getExecutability();
				if (executability.isExecutable()) {
					setVisible(true);
					setExecutable();
				} else {
					if (executability.isHidden()) {
						setVisible(false);
						setNotExecutable(ExecutableState.NOT_EXEC_HIDDEN_REASON);
					} else {
						setVisible(true);
						setNotExecutable(executability.getI18NReasonKey());
					}
				}
			}

			@Override
			protected void firstListenerAdded() {
				super.firstListenerAdded();

				updateExecutabilityState();
				CommandModelRegistry.getRegistry().registerCommandModel(this);
			}

			@Override
			protected void lastListenerRemoved() {
				CommandModelRegistry.getRegistry().deregisterCommandModel(this);

				super.lastListenerRemoved();
			}

			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				return command.prepareCommand(context, table, self()).execute(context);
			}

			/**
			 * Reference to the button properties.
			 */
			protected CommandModel self() {
				return this;
			}
		}

		AbstractCommandModel result = new DynamicCommandModel();

		C config = getConfig();

		ThemeImage image = config.getImage();
		if (image == null) {
			image = defaultImage();
		}
		result.setImage(image);
		ThemeImage imageDisabled = config.getImageDisabled();
		if (imageDisabled == null) {
			imageDisabled = config.getImage();
		}
		if (imageDisabled == null) {
			imageDisabled = defaultImageDisabled();
		}
		result.setNotExecutableImage(imageDisabled);
		ResKey labelKey = config.getLabel();
		if (labelKey == null) {
			labelKey = defaultLabelKey();
		}
		ResKey tooltipKey = config.getTooltip();
		if (labelKey == null) {
			labelKey = defaultTooltipKey();
		}
		if (tooltipKey == null && labelKey != null) {
			tooltipKey = labelKey.tooltipOptional();
		}
		Resources resources = Resources.getInstance();
		if (labelKey != null) {
			result.setLabel(resources.getString(labelKey));
		}
		if (tooltipKey != null) {
			result.setTooltip(resources.getString(tooltipKey));

			ResKey tooltipCaptionKey = config.getTooltipCaption();
			if (tooltipCaptionKey == null) {
				tooltipCaptionKey = defaultTooltipCaptionKey();
			}
			if (tooltipCaptionKey != null) {
				result.setTooltipCaption(resources.getString(tooltipCaptionKey));
			}
		}

		result.setShowProgress(config.getShowProgress());
		result.setCssClasses(config.getCSSClass());

		return result;
	}

	/**
	 * Hook for sub-classes to provide a default value for {@link Config#getLabel()}.
	 */
	protected ResKey defaultLabelKey() {
		return null;
	}

	/**
	 * Hook for sub-classes to provide a default value for {@link Config#getTooltip()}.
	 */
	protected ResKey defaultTooltipKey() {
		return null;
	}

	/**
	 * Hook for sub-classes to provide a default value for {@link Config#getTooltipCaption()}.
	 */
	protected ResKey defaultTooltipCaptionKey() {
		return null;
	}

	/**
	 * Hook for sub-classes to provide a default value for {@link Config#getImage()}.
	 */
	protected ThemeImage defaultImage() {
		return null;
	}

	/**
	 * Hook for sub-classes to provide a default value for {@link Config#getImageDisabled()}.
	 */
	protected ThemeImage defaultImageDisabled() {
		return null;
	}

	/**
	 * Creates the {@link TableCommand} being executed.
	 */
	protected abstract TableCommand getTableCommand();

}
