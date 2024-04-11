/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.themes.modern.layout;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControlBase;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.LayoutControlProvider;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ModernStatusLineComponent extends ControlComponent implements ControlRepresentable {

    public interface Config extends ControlComponent.Config {
		@Override
		@ItemDefault(ControlRepresentableCP.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Name(XML_VERSION_RENDERER_CLASS)
		@InstanceDefault(DefaultVersionRenderer.class)
		@InstanceFormat
		ControlRenderer<? super AbstractConstantControlBase> getVersionRendererClass();
	}

	private static final String XML_VERSION_RENDERER_CLASS = "versionRendererClass";
    
	private Control renderingControl;

	private final ControlRenderer<? super AbstractConstantControlBase> versionRenderer;
	
    public ModernStatusLineComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
        
        this.versionRenderer = aAtts.getVersionRendererClass();
    }

    /**
     * Write the body of html-file. Overwrites method of superclass.
     */
    @Override
	public final void writeBody(ServletContext context, HttpServletRequest req, HttpServletResponse resp, TagWriter anOut)
                        throws IOException, ServletException {

        DisplayContext theDisplayContext = DefaultDisplayContext.getDisplayContext(req);
        getRenderingControl().write(theDisplayContext, anOut);
    }

    @Override
	public Control getRenderingControl() {
        if (renderingControl == null) {
            initControls();
        }
        return renderingControl;
    }

    private void initControls() {
		this.renderingControl = new AbstractConstantControlBase() {

			@Override
			public Object getModel() {
				return null;
			}

            @Override
			protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
                versionRenderer.write(context, out, this);
            }

			@Override
			protected void writeControlClassesContent(Appendable out) throws IOException {
				super.writeControlClassesContent(out);
				versionRenderer.appendControlCSSClasses(out, this);
			}

            @Override
			public boolean isVisible() {
                return ModernStatusLineComponent.this.isVisible();
            }

        };
    }

	@Override
	protected Map<String, ChannelSPI> channels() {
		return Collections.emptyMap();
	}

    @Override
	protected boolean supportsInternalModel(Object object) {
		return false;
	}

	@Override
	protected void becomingInvisible() {
        super.becomingInvisible();
        if (renderingControl != null) {
            renderingControl.detach();
        }
    }

}

