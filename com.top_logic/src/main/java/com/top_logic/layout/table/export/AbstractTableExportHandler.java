/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Base class for {@link TableData} exports.
 */
public abstract class AbstractTableExportHandler extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link AbstractTableExportHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config {
		@Override
		@ComplexDefault(DefaultLabel.class)
		ResKey getResourceKey();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.EXPORT_NAME)
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault(CommandHandlerFactory.EXPORT_BUTTONS_GROUP)
		String getClique();

		// The table data from the component is used. The model of the command is ignored.
		@Override
		@Hidden
		ModelSpec getTarget();

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_GRID")
		ThemeImage getImage();

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_GRID_DISABLED")
		ThemeImage getDisabledImage();

		/**
		 * {@link DefaultValueProvider} for the {@link Config#getResourceKey()} property.
		 */
		class DefaultLabel extends DefaultValueProviderShared {
			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return com.top_logic.layout.table.renderer.I18NConstants.EXPORT_EXCEL;
			}
		}

	}

	/**
	 * Creates a {@link AbstractTableExportHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractTableExportHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, final LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		return createProgressDialog(aComponent).open(displayContext);
	}

	private ProgressDialog createProgressDialog(final LayoutComponent aComponent) {
		return new ProgressDialog(I18NConstants.PERFORMING_EXPORT, DisplayDimension.px(500), DisplayDimension.px(350)) {

			private final int _rowCount = extractTableData(aComponent).getViewModel().getRowCount();

			private BinaryData _download;

			@Override
			protected void run(I18NLog log) throws AbortExecutionException {
				_download = createDownloadData(this::incProgress, log, aComponent);
			}

			@Override
			protected CommandModel addOk(List<CommandModel> buttons) {
				CommandModel result;
				ButtonType type = ButtonType.OK;
				buttons.add(result = MessageBox.button(I18NConstants.DOWNLOAD, type.getButtonImage(),
					type.getDisabledButtonImage(), this::deliverContent));
				return result;
			}

			private HandlerResult deliverContent(DisplayContext displayContext) {
				if (_download != null) {
					displayContext.getWindowScope().deliverContent(_download);
				}
				return getDiscardClosure().executeCommand(displayContext);
			}

			@Override
			protected int getStepCnt() {
				return _rowCount;
			}

			protected void incProgress() {
				incProgress(1);
			}

		};
	}

	/**
	 * Creates the binary download data to be send to the client.
	 * 
	 * @param log
	 *        The export log.
	 * 
	 * @param component
	 *        the model fetched from the underlying component.
	 * @return {@link BinaryData} to be streamed to the client.
	 */
	protected abstract BinaryData createDownloadData(Runnable progressIncrementer, I18NLog log, LayoutComponent component);

	/**
	 * Retrieves the {@link TableData} from the component.
	 */
	protected TableData extractTableData(LayoutComponent component) {
		return ((TableControl) ((ControlRepresentable) component).getRenderingControl()).getTableData();
	}

}
