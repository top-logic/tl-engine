/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.progress;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.progress.AbstractProgressComponent.AbstractProgressCommandHandler;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.view.component.AbstractProgressFilterComponent;
import com.top_logic.reporting.view.component.table.FilterAwareComponent;
import com.top_logic.reporting.view.component.table.FilterAwareModelBuilder;

/**
 * TODO olb: Add a description...
 * 
 * @author     <a href="mailto:olb@top-logic.com">olb</a>
 */
public class ApplyFilterProgressCommandHandler extends AbstractProgressCommandHandler {

	/**
	 * Configuration options for {@link ApplyFilterProgressCommandHandler}.
	 */
	public interface Config extends AbstractProgressCommandHandler.Config {
		@Override
		@FormattedDefault(TARGET_NULL)
		ModelSpec getTarget();
	}

	/** The unique ID of this command. */
	public final static String COMMAND_ID = "applyFilterProgress";
	
	/** The parameter to provide filters to be used in the command handler. */
	public final static String PARAMETER_NAME_FILTER = "Filters";

	/**
	 * Creates a {@link ApplyFilterProgressCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ApplyFilterProgressCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ProgressInfo startThread(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		final DefaultProgressInfo progressInfo = new DefaultProgressInfo();
		final FilterVO filter = extractFilterFromParams(someArguments);
		
		final AbstractProgressFilterComponent<FilterVO> masterComponent = (AbstractProgressFilterComponent<FilterVO>) aComponent;
		final FilterAwareModelBuilder modelBuilder = getTableModelBuilderOfSlaveComponent(masterComponent);
		ApplyFilterProgressComponent progressComponent = (ApplyFilterProgressComponent) aComponent.getMainLayout().getComponentByName(getProgressComponent());

		progressComponent.setOpener(masterComponent);

		if (masterComponent.needProgressDialog(masterComponent.getSearchBaseCount())) {
			ThreadContext.inSystemContext(ApplyFilterProgressCommandHandler.class, new InContext() {
				@Override
				public void inContext() {
					try {
						modelBuilder.prepareModel(filter, progressInfo);
					}
					catch (Exception ex) {
						Logger.error("Failed to search.", ex, ApplyFilterProgressCommandHandler.class);
						throw new RuntimeException(ex);
					}
					finally {
						progressInfo.setFinished(true);
					}
				}
			});

			return progressInfo;
		}
		else {
			modelBuilder.prepareModel(filter, progressInfo);
			masterComponent.fireEventToSlave();

			return null;
		}
	}

	private final FilterAwareModelBuilder getTableModelBuilderOfSlaveComponent(AbstractProgressFilterComponent<FilterVO> masterComponent) {
		LayoutComponent slaveLayoutComponent = getSlaveComponent(masterComponent);
        
        return getFilterAwareModelBuilder(slaveLayoutComponent);
	}

	private FilterAwareModelBuilder getFilterAwareModelBuilder(LayoutComponent slaveLayoutComponent) {
		FilterAwareComponent tableForFilterComp = null;
        if (slaveLayoutComponent instanceof FilterAwareComponent) {
        	tableForFilterComp = (FilterAwareComponent) slaveLayoutComponent;
        }
        else {
        	throw new IllegalArgumentException("The slave component '" + slaveLayoutComponent.getClass().getName() + "' has to implements '" +
        				FilterAwareComponent.class.getName() + "'");
        }
        return tableForFilterComp.getModelBuilder();
	}

	private LayoutComponent getSlaveComponent(AbstractProgressFilterComponent<FilterVO> filterComponent) {
		return filterComponent.getSlaveComponent();
	}

	private FilterVO extractFilterFromParams(Map<String, Object> someArguments) {
		Object object = someArguments.get(PARAMETER_NAME_FILTER);
		if (object instanceof FilterVO) {
			return (FilterVO) object;
		}
		else {
			throw new IllegalArgumentException("The parameter '" + PARAMETER_NAME_FILTER + "' should be an instance of '" + FilterVO.class.getName() + "' but is '" + object.getClass().getName());
		}
	}

	@Override
	protected ComponentName getProgressComponent() {
		return ApplyFilterProgressComponent.COMPONENT_ID;
	}

}
