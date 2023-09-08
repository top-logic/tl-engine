/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.util.Resources;

/**
 * Cell renderer for writing I18N text to a table cell.
 * 
 * This class will append the prefix and postfix to the given string, translate it via the
 * {@link Resources} and write it via the method {@link TagWriter#writeText(CharSequence)}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class I18NCellRenderer extends AbstractCellRenderer {

    /** Prefix to be used for translation. */ 
    private final String prefix;

    /** Postfix to be used for translation. */ 
    private final String postfix;

    /** 
     * Creates a {@link I18NCellRenderer}.
     * 
     * @param    aPrefix    Prefix for I18N, must not be <code>null</code>, may be empty.
     */
    public I18NCellRenderer(String aPrefix) {
        this(aPrefix, "");
    }

    /** 
     * Creates a {@link I18NCellRenderer}.
     * 
     * @param    aPrefix     Prefix for I18N, must not be <code>null</code>, may be empty.
     * @param    aPostfix    Postfix for I18N, must not be <code>null</code>, may be empty.
     */
    public I18NCellRenderer(String aPrefix, String aPostfix) {
        assert aPrefix != null : "Prefix must not be null";
        assert aPostfix != null : "Postfix must not be null";

        this.prefix  = aPrefix;
        this.postfix = aPostfix;
    }

    @Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		Object theValue = cell.getValue();

        if (theValue != null) {
			ResKey theString = ResPrefix.legacyString(this.prefix).key(theValue.toString()).suffix(this.postfix);

            out.writeText(context.getResources().getString(theString));
        }
    }
}
