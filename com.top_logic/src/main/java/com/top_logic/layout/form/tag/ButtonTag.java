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

	/** The tab index of the button. */
	public final IntAttribute    tabindex      = new IntAttribute();
	/** The name of the command executed by the button. */
	public final StringAttribute command       = new StringAttribute();

	private ThemeImage _icon = null;

	private ThemeImage _disabledIcon = null;

	/** The resource key of the button label. */
	public ResKey reskey;

	private Map<String, Object> arguments = Collections.emptyMap();
    private boolean plain;
    private boolean execStateInTooltip = true;
	private boolean disabled;

	private AbstractButtonRenderer<?> renderer;

	private ResKey _disabledReason;

	/** Setter for {@link #command}. */
	public final void setCommand(String value) {
		this.command.set(value);
	}

	/** Setter for {@link #tabindex}. */
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

	/** Sets the renderer of the button. */
	public final void setRenderer(AbstractButtonRenderer<?> renderer) {
		this.renderer = renderer;
    }

    /** Sets whether the button is rendered plain. */
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

	/** The resource key of the button label. */
	public ResKey internalGetReskey() {
        return this.reskey;
    }

	/** Sets the resource key of the button label. */
	@CalledFromJSP
    public void setReskey(String aResKey) {
		setReskeyConst(ResKey.internalJsp(aResKey));
    }

	/** Sets the resource key of the button label. */
	@CalledFromJSP
	public void setReskeyConst(ResKey resKey) {
		this.reskey = resKey;
	}

	/** Sets the resource key of the reason why the button can not be executed. */
	@CalledFromJSP
	public void setDisabledReason(String disabledReason) {
		setDisabledReasonConst(ResKey.internalJsp(disabledReason));
	}

	/** Sets the reason why the button can not be executed. */
	@CalledFromJSP
	public void setDisabledReasonConst(ResKey reasonKey) {
		_disabledReason = reasonKey;
	}

    /** Sets the literal label of the button. */
    public void setLabel(String label) {
		this.reskey = ResKey.text(label);
    }

    /** Sets whether the reason why the button can not be executed is displayed in its tooltip. */
    public void setExecStateInTooltip(boolean execStateInTooltip) {
        this.execStateInTooltip = execStateInTooltip;
    }

    /** Sets whether the button is disabled. */
    public void setDisabled(boolean disabled) {
		this.disabled = disabled;
    }

    /** Sets the arguments passed to the executed command. */
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

		String labelOverride;
		String tooltipOverride;
		if (this.reskey != null) {
			Resources resources = Resources.getInstance();
			labelOverride = resources.getString(reskey);
			tooltipOverride = resources.getString(reskey.tooltipOptional());
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

	/** Logs the given error and writes the display message to the page. */
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
