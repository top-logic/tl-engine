/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Extracts relevant information from a {@link ResultSet}.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public interface ResultExtractor<T> {

    /**
     * Extracts relevant information from a {@link ResultSet}. The {@link ResultSet} is not closed
     * afterwards, so the caller of this method must do this afterwards.
     *
     * @param result
     *        the {@link ResultSet} to process
     * @return the relevant information
     * @throws SQLException
     *         if some error occurs while requesting the database
     */
    public T extractResult(ResultSet result) throws SQLException;

}
