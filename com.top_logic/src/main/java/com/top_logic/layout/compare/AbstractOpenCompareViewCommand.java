/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.SimpleTableDataExport;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.model.TableDataExport;
import com.top_logic.layout.table.model.TableDataExportProxy;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Abstract super class for {@link CommandHandler} opening a compare view in a dialog.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractOpenCompareViewCommand extends AbstractCommandHandler {

	/**
	 * Configuration for an {@link AbstractOpenCompareViewCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@FormattedDefault("theme:com.top_logic.layout.compare.Icons.OPEN_COMPARE_VIEW")
		ThemeImage getImage();

		@Override
		@ListDefault(InCompareModeExecutability.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

	}

	/**
	 * Name of the table in the layout configuration that contains special settings for the compare
	 * view.
	 */
	public static final String COMPARE_TABLE = "compareTable";


	/**
	 * Creates a new {@link AbstractOpenCompareViewCommand}.
	 */
	public AbstractOpenCompareViewCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ResKey getDefaultI18NKey() {
		return I18NConstants.OPEN_COMPARE_VIEW;
	}

	/**
	 * Service method to get a potential specialised {@link TableConfigurationProvider} in case the
	 * compare dialog displays a table.
	 * 
	 * @param component
	 *        The {@link LayoutComponent} to compare.
	 * @return A special {@link TableConfigurationProvider} for the compare view.
	 */
	protected final TableConfigurationProvider getCompareTableProviderFor(LayoutComponent component) {
		if (component instanceof FormComponent) {
			TableConfigurationProvider tableConfig =
				((FormComponent) component).lookupTableConfigurationBuilder(COMPARE_TABLE);
			if (tableConfig != null) {
				return tableConfig;
			}
		}
		return TableConfigurationFactory.emptyProvider();
	}

	/**
	 * Service method to get a potential specialised {@link TableConfigurationProvider} in case the
	 * compare dialog displays a table.
	 * 
	 * @param component
	 *        The {@link LayoutComponent} to compare.
	 * @param executabilityDelegate
	 *        A {@link CommandHandler} that is used to compute the executability of the export
	 *        button. Typically this is the export command of the component. May be
	 *        <code>null</code>, in which case the export is always possible.
	 * 
	 * @return A special {@link TableConfigurationProvider} for the compare view.
	 */
	protected final TableConfigurationProvider getExportTableProviderFor(final LayoutComponent component,
			final CommandHandler executabilityDelegate) {
		return new NoDefaultColumnAdaption() {
			
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				TableDataExport exporter = newSimpleTableDataExport();
				exporter = ensureCorrectExecutability(exporter);
				table.setExporter(exporter);
			}

			private TableDataExport newSimpleTableDataExport() {
				SimpleTableDataExport.Config exporterConfig =
					TypedConfiguration.newConfigItem(SimpleTableDataExport.Config.class);
				return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(exporterConfig);
			}

			private TableDataExport ensureCorrectExecutability(final TableDataExport exporter) {
				if (executabilityDelegate == null) {
					return exporter;
				}
				return new TableDataExportProxy() {

					@Override
					protected TableDataExport impl() {
						return exporter;
					}

					@Override
					public ExecutableState getExecutability(TableData table) {
						Map<String, Object> args = Collections.emptyMap();
						return CommandDispatcher.resolveExecutableState(executabilityDelegate, component, args);
					}
				};
			}
		};
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {
		CompareAlgorithm compareAlgorithm = ((CompareAlgorithmHolder) aComponent).getCompareAlgorithm();
		Object compareModel = compareAlgorithm.getCompareObject(aComponent, model);
		if (compareModel == null) {
			return HandlerResult.error(I18NConstants.NO_COMPARE_OBJECT);
		}
		return openCompareDialog(aContext, aComponent, model, compareModel, compareAlgorithm);
	}

	/**
	 * Opens the dialog that displays the comparison between the given models.
	 * 
	 * @param context
	 *        {@link DisplayContext} in which command execution occurs.
	 * @param component
	 *        The {@link LayoutComponent} on which this command is executed.
	 * @param businessModel
	 *        The {@link LayoutComponent#getModel() model} of the given component.
	 * @param compareModel
	 *        The model to compare the given business model.
	 * @return {@link HandlerResult} to return in
	 *         {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	protected abstract HandlerResult openCompareDialog(DisplayContext context, LayoutComponent component,
			Object businessModel, Object compareModel, CompareAlgorithm compareAlgorithm);

	/**
	 * Opens a dialog that states that it is not possible to find a comparison model for the given
	 * model.
	 * 
	 * @param context
	 *        The context in which command execution occurs.
	 * @param model
	 *        The model to open a compare view for.
	 */
	protected HandlerResult openNotComparableDialog(DisplayContext context, Object model) {
		Resources resources = context.getResources();
		String title = resources.getString(I18NConstants.NO_COMPARISON_TITLE);
		String message =
			resources.getMessage(I18NConstants.NO_COMPARISON_MESSAGE, MetaLabelProvider.INSTANCE.getLabel(model));
		return MessageBox.confirm(context.getWindowScope(), MessageType.INFO, title, message,
			MessageBox.button(ButtonType.CLOSE));
	}

	/**
	 * Links the given dialog and component, such that dialog is closed when the component becomes
	 * invisible.
	 */
	protected void ensureDialogCloseOnComponentInvisible(final DialogModel dialogModel, final LayoutComponent component) {
		final VisibilityListener visibilityListener = new VisibilityListener() {
			
			@Override
			public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
				if (!newVisibility.booleanValue()) {
					// component becomes invisible.
					dialogModel.getCloseAction().executeCommand(DefaultDisplayContext.getDisplayContext());
				}
				return Bubble.BUBBLE;
			}
		};
		component.addListener(LayoutComponent.VISIBILITY_EVENT, visibilityListener);
		dialogModel.addListener(DialogModel.CLOSED_PROPERTY, new DialogClosedListener() {
	
			@Override
			public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					// dialog is closed.
					component.removeListener(LayoutComponent.VISIBILITY_EVENT, visibilityListener);
				}
			}
		});
	}

}

