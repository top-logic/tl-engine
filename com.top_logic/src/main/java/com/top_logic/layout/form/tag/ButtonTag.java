/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import jakarta.servlet.jsp.JspException;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DefaultButtonRenderer;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.control.LinkButtonRenderer;
import com.top_logic.layout.form.tag.util.IntAttribute;
import com.top_logic.layout.form.tag.util.StringAttribute;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.Resources;

/**
 * View of a command within a form.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ButtonTag extends AbstractFormTag {

	public final IntAttribute    tabindex      = new IntAttribute();
	public final StringAttribute command       = new StringAttribute();

	private ThemeImage _icon = null;

	private ThemeImage _disabledIcon = null;

	public ResKey reskey;

	private Map<String, Object> arguments = Collections.emptyMap();
    private boolean plain;
    private boolean execStateInTooltip = true;
	private boolean disabled;

	private AbstractButtonRenderer<?> renderer;

	private ResKey _disabledReason;

	public final void setCommand(String value) {
		this.command.set(value);
	}

	public final void setTabindex(String value) {
		this.tabindex.set(value);
	}

	/**
	 * Use {@link #setDisabledIcon(ThemeImage)}.
	 * 
	 * @param value
	 *        The key for the concrete image.
	 */
	@Deprecated
    public final void setDisabledImage(String value) {
		setDisabledIcon(ThemeImage.icon(value));
    }

	/**
	 * Button icon to render if disabled.
	 */
	public final void setDisabledIcon(ThemeImage value) {
		_disabledIcon = value;
	}

	public final void setRenderer(AbstractButtonRenderer<?> renderer) {
		this.renderer = renderer;
    }

    public final void setPlain(boolean plain) {
    	this.plain = plain;
    }

	/**
	 * Use {@link #setIcon(ThemeImage)}.
	 * 
	 * @param value
	 *        The key for the concrete image.
	 */
	@Deprecated
 	public final void setImage(String value) {
		setIcon(ThemeImage.icon(value));
	}

	/**
	 * Button icon to render.
	 */
	@CalledFromJSP
	public void setIcon(ThemeImage icon) {
		_icon = icon;
	}

	public ResKey internalGetReskey() {
        return this.reskey;
    }

	@CalledFromJSP
    public void setReskey(String aResKey) {
		setReskeyConst(ResKey.internalJsp(aResKey));
    }

	@CalledFromJSP
	public void setReskeyConst(ResKey resKey) {
		this.reskey = resKey;
	}

	@CalledFromJSP
	public void setDisabledReason(String disabledReason) {
		setDisabledReasonConst(ResKey.internalJsp(disabledReason));
	}

	@CalledFromJSP
	public void setDisabledReasonConst(ResKey reasonKey) {
		_disabledReason = reasonKey;
	}

    public void setLabel(String label) {
		this.reskey = ResKey.text(label);
    }

    public void setExecStateInTooltip(boolean execStateInTooltip) {
        this.execStateInTooltip = execStateInTooltip;
    }

    public void setDisabled(boolean disabled) {
		this.disabled = disabled;
    }

    public void setArguments(Map arguments) {
		this.arguments = arguments;
    }

    @Override
	protected int startFormMember() throws IOException, JspException {
		String  commandName = command.getAsString();

		LayoutComponent component = getComponent();

        if (component == null) {
			error("[ERROR: No component]", "No component found  (button is: '" + commandName + "')!");
        	return SKIP_BODY;
        }

		CommandHandler theCommand = component.getCommandById(commandName);

        if (theCommand == null) {
        	error("[ERROR: Missing command '" + commandName + "']", "Cannot find command '" + commandName + "' for button in component '" + component.getName() + "'.");
        	return SKIP_BODY;
        }

		ResKey labelOverride;
		ResKey tooltipOverride;
		if (this.reskey != null) {
			labelOverride = reskey;
			tooltipOverride = reskey.tooltipOptional();
        } else {
			labelOverride = null;
			tooltipOverride = null;
        }

        // Decide about the best-matching button renderer to use.
		AbstractButtonRenderer<?> view;
        if (renderer != null) {
        	view = renderer;
        } else if (plain) {
        	view = execStateInTooltip ? LinkButtonRenderer.INSTANCE : LinkButtonRenderer.NO_REASON_INSTANCE;
		} else if (_icon != null) {
        	view = execStateInTooltip ? ImageButtonRenderer.INSTANCE : ImageButtonRenderer.NO_REASON_INSTANCE;
        } else {
        	view = execStateInTooltip ? DefaultButtonRenderer.INSTANCE : DefaultButtonRenderer.NO_REASON_INSTANCE;
        }

		CommandModel commandModel = component.createCommandModel(Resources.getInstance(), theCommand, arguments);
		if (labelOverride != null) {
			commandModel.setLabel(labelOverride);
		}
		if (tooltipOverride != null) {
			commandModel.setTooltip(tooltipOverride);
		}
		ButtonControl buttonControl = new ButtonControl(commandModel, view);
		if (tabindex.isSet()) {
			buttonControl.setTabindex(tabindex.get());
		}
		if (_icon != null) {
			buttonControl.setImage(_icon);
		}
		if (_disabledIcon != null) {
			buttonControl.setDisabledImage(_disabledIcon);
        }
		if (disabled) {
			buttonControl.disable(_disabledReason);
		}

		ControlTagUtil.writeControl(this, pageContext, buttonControl);

		return SKIP_BODY;
	}

	protected void error(String displayMessage, String logMessage) throws IOException {
		Logger.error(logMessage, this);

		TagWriter out = out();
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "error");
		out.endBeginTag();
		out.writeText(displayMessage);
		out.endTag(SPAN);
	}

	@Override
	protected int endFormMember() {
		return EVAL_PAGE;
	}

	@Override
	protected void teardown() {
		super.teardown();
		command.reset();
		tabindex.reset();
		_icon = null;
		_disabledIcon = null;
		arguments = Collections.emptyMap();
		reskey = null;
		execStateInTooltip = true;
		plain = false;
		disabled = false;
		renderer = null;
	}
}
