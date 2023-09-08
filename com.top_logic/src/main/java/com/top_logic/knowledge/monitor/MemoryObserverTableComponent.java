/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.monitor;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.InvalidateCommand;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.NegateRule;
import com.top_logic.util.sched.MemoryObserverThread;

/**
 * Control the {@link MemoryObserverThread}.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MemoryObserverTableComponent extends TableComponent {
    
	/**
	 * Configuration for the {@link MemoryObserverTableComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends TableComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			TableComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(RunGarbargeCollectionCommand.COMMAND_ID);
			registry.registerButton(StopMemoryObserverCommand.COMMAND_ID);
			registry.registerButton(StartMemoryObserverCommand.COMMAND_ID);
			registry.registerButton(InvalidateCommand.COMMAND);
		}

	}

	/**
	 * Create from XML.
	 */
    public MemoryObserverTableComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }

    @Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(MemoryObserverThread.getInstance());
		}
		return super.validateModel(context);
    }

	public static class RunGarbargeCollectionCommand extends InvalidateCommand {
        
        public static final String COMMAND_ID = "runGarbageCollection";
        
        public RunGarbargeCollectionCommand(InstantiationContext context, Config config) {
            super(context, config);
        }
        
        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
            System.gc();
            return super.handleCommand(aContext, aComponent, model, aSomeArguments);
        }
    }
    
    public static class StartMemoryObserverCommand extends AbstractCommandHandler {
        
        public static final String COMMAND_ID = "startMemoryObserver";
        
        public StartMemoryObserverCommand(InstantiationContext context, Config config) {
            super(context, config);
        }
        
        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
            
            MemoryObserverThread.getInstance().startLogging();
            
            aComponent.invalidate();
            aComponent.invalidateButtons();
            
            return HandlerResult.DEFAULT_RESULT;
        }
        
        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
            return IsMemoryObserverLoggingRule.INVERSE_INSTANCE;
        }
    }
    
    public static class StopMemoryObserverCommand extends AbstractCommandHandler {
        public static final String COMMAND_ID = "stopMemoryObserver";
        
        public StopMemoryObserverCommand(InstantiationContext context, Config config) {
            super(context, config);
        }
        
        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
            
            MemoryObserverThread.getInstance().stopLogging();
            
            aComponent.invalidate();
            aComponent.invalidateButtons();
            
            return HandlerResult.DEFAULT_RESULT;
        }
        
        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
            return IsMemoryObserverLoggingRule.INSTANCE;
        }
    }
    
    private static class IsMemoryObserverLoggingRule implements ExecutabilityRule {
        
        public static final ExecutabilityRule INSTANCE          = new IsMemoryObserverLoggingRule();
        
        public static final ExecutabilityRule INVERSE_INSTANCE  = new NegateRule(INSTANCE, true);
        
        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
            if (MemoryObserverThread.getInstance().isLogging()) {
                return ExecutableState.EXECUTABLE;
            }
			return ExecutableState.NOT_EXEC_HIDDEN;
        }
    }
}

