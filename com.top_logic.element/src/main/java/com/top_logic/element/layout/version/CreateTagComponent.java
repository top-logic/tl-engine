/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.version;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.gui.AbstractCreateAttributedCommandHandler;
import com.top_logic.element.meta.gui.CreateAttributedComponent;
import com.top_logic.element.version.intf.Tag;
import com.top_logic.element.version.intf.TagAll;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.util.error.TopLogicException;

/**
 * General create component for a {@link Tag}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CreateTagComponent extends CreateAttributedComponent {

    public interface Config extends CreateAttributedComponent.Config {
		@Name(CreateTagComponent.XML_ATTRIBUTE_META_ELEMENT)
		@StringDefault(TagAll.TAG_ALL_TYPE)
		String getMetaElement();

		@StringDefault(CreateTagCommandHandler.COMMAND_ID)
		@Override
		String getCreateHandler();
	}

	/** The attribute name in the configuration file. */
    private static final String XML_ATTRIBUTE_META_ELEMENT = "metaElement";

    public static Set IGNORE_SET;

    static {
        CreateTagComponent.IGNORE_SET = new HashSet();

		CreateTagComponent.IGNORE_SET.add(Tag.DATE_ATTR);
    }

    /** The meta element name of the tag. */
    private String meName;

    /** 
     * Creates a {@link CreateTagComponent}.
     */
    public CreateTagComponent(InstantiationContext context, Config someAttr) throws ConfigurationException {
        super(context, someAttr);

        this.meName = someAttr.getMetaElement();
    }

    @Override
	public TLClass getMetaElement() {
        return MetaElementFactory.getInstance().getGlobalMetaElement(this.meName);
    }

    /**
     * General create command handler for the {@link Tag}.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class CreateTagCommandHandler extends AbstractCreateAttributedCommandHandler {

        // Constants

        /** The command ID. */
        public static final String COMMAND_ID = "createTag";

        // Constructors
        
        /** 
         * Creates a {@link CreateTagCommandHandler}.
         */
        public CreateTagCommandHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        // Overridden methods from AbstractCreateAttributedCommandHandler

        @Override
		public Wrapper createNewObject(Map<String, Object> someValues, Wrapper aModel) {
			String theName = (String) someValues.get(Tag.NAME_ATTR);

            try {
				return (Wrapper) Tag.createTag(theName);
            }
            catch (Exception ex) {
                throw new TopLogicException(CreateTagCommandHandler.class, "create.object", new String[] {theName}, ex);
            }
        }
    }
}

