/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import com.top_logic.layout.progress.ProgressInfo;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface LoggerProgressInfo extends ProgressInfo {

    public void info(String aMessage);

    public void info(String aMessage, Class aCaller);

    public void info(String aMessage, Throwable anException);

    public void info(String aMessage, Throwable anException, Class aCaller);

    public void warn(String aMessage);

    public void warn(String aMessage, Class aCaller);

    public void warn(String aMessage, Throwable anException);

    public void warn(String aMessage, Throwable anException, Class aCaller);

    public void error(String aMessage);

    public void error(String aMessage, Class aCaller);

    public void error(String aMessage, Throwable anException);

    public void error(String aMessage, Throwable anException, Class aCaller);

    public void debug(String aMessage);

    public void debug(String aMessage, Class aCaller);

    public void debug(String aMessage, Throwable anException);

    public void debug(String aMessage, Throwable anException, Class aCaller);

    public void setFinished(String aMessage);

    public void setFinished(String aMessage, Class aCaller);

    public void setFinished(String aMessage, Throwable anException);

    public void setFinished(String aMessage, Throwable anException, Class aCaller);

    public void reset(long expected);

    public void increaseProgress();

}
