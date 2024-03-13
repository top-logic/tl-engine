/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person.infouser;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.license.LicenseTool;
import com.top_logic.util.monitor.ApplicationMonitorComponent;

/**
 * @deprecated since TL 5.8 this functionality is implemented in {@link ApplicationMonitorComponent}.
 * 
 * @author <a href="mailto:tri@top-logic.com">tri</a>
 */
@Deprecated
public class LicenceOverviewUtil implements HTMLConstants {

	public static final LicenceOverviewUtil INSTANCE = new LicenceOverviewUtil();

	private static final ResPrefix I18N_PREFIX = I18NConstants.LICENSE_RESOURCES;
    
	/**
	 * must be public to be accessible through reflection
	 */
	@CalledByReflection
	public LicenceOverviewUtil() {
    }

	@CalledFromJSP
	public void writeLicenceOverview(DisplayContext context, TagWriter out) throws Exception {
		writeStandardLicenceOverview(context, out);

    }

	private void writeStandardLicenceOverview(DisplayContext context, TagWriter out)
			throws Exception {
        out.beginTag(TABLE);
        {
            out.beginTag(TR);
            {
                out.beginBeginTag(TD);
                {
                    out.writeAttribute(COLSPAN_ATTR, 3);
                    out.writeAttribute(STYLE_ATTR, "font-weight:bold");
                }
                out.endBeginTag();
                {
					out.writeContent(context.getResources().getString(I18N_PREFIX.key("label.licenceoverview")));
                }
                out.endTag(TD);
            }
            out.endTag(TR);

            out.beginTag(TR);
            {
                out.beginBeginTag(TD);
                {
                    out.writeAttribute(COLSPAN_ATTR, 3);
                }
                out.endBeginTag();
                {

					out.writeText(HTMLFormatter.getInstance().formatInt(Person.all().size()));
                    out.writeText(NBSP);
                    out.writeText("/");
                    out.writeText(NBSP);
					out.writeText(HTMLFormatter.getInstance().formatInt(LicenseTool.getInstance().getLicense().getUsers()));
                }
                out.endTag(TD);
            }
            out.endTag(TR);
        }
        out.endTag(TABLE);    
    }
}
