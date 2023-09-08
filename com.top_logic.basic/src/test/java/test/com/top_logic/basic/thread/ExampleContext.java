/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.thread;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.thread.ThreadContext;

/**
 * Example how to override  {@link com.top_logic.basic.thread.ThreadContext}.
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class ExampleContext extends ThreadContext {

	/**
	 * Configuration for an {@link ExampleContext}
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		String getPropertyValue();
	}

    /** Example for a Property. */
    String propval;
    
	/**
	 * Creates a new {@link ExampleContext}.
	 */
	public ExampleContext(Config configuration) {
        super ();
		propval = configuration.getPropertyValue();
    }
        
}
