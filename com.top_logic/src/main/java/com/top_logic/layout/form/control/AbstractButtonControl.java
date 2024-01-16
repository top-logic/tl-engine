/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.AltTextChangedListener;
import com.top_logic.layout.basic.ButtonImageListener;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ExecutableListener;
import com.top_logic.layout.basic.FocusHandling;
import com.top_logic.layout.basic.Focusable;
import com.top_logic.layout.basic.InvokeExpressionProvider;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.link.Link;
import com.top_logic.layout.form.CSSClassListener;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.LabelChangedListener;
import com.top_logic.layout.form.TooltipChangedListener;
import com.top_logic.layout.renderers.Icons;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.util.Resources;

/**
 * {@link com.top_logic.layout.Control} implementation that triggers the given
 * {@link ControlCommand}.
 * <p>
 * If an image is provided, this view renders itself as {@link HTMLConstants#INPUT} element of type
 * {@link HTMLConstants#IMAGE_TYPE_VALUE image}. Otherwise, it renders itself as
 * {@link HTMLConstants#BUTTON} element with the given label text.
 * </p>
 * *
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractButtonControl<M extends ButtonUIModel> extends AbstractControl implements HTMLConstants, Comparable,
		VisibilityListener, CSSClassListener, TooltipChangedListener, ExecutableListener, Focusable.FocusRequestedListener,
		ButtonImageListener, LabelChangedListener, AltTextChangedListener, Link {

	public static final int DISABLED_PROPERTY = 0;
	public static final int DISABLED_IMAGE_PROPERTY = 1;
	public static final int IMAGE_PROPERTY = 2;

	/**
	 * {@link Property} which must be used for adding an {@link InvokeExpressionProvider} to the
	 * command model to be used during rendering by a {@link ButtonControl}.
	 */
	public static final Property<InvokeExpressionProvider> INVOKE_EXPRESSION_PROVIDER =
		TypedAnnotatable.property(InvokeExpressionProvider.class, "invokeExpressionProvider");

	/**
	 * {@link Property} to use as key for a property of a {@link CommandModel}. The value for this
	 * key must be an {@link IButtonRenderer} which will be used as renderer for a ButtonControl if
	 * no renderer is given explicit.
	 */
	public static final Property<IButtonRenderer> BUTTON_RENDERER =
		TypedAnnotatable.property(IButtonRenderer.class, "buttonRenderer");

	/**
	 * Indicates whether a progress bar shall be shown while executing the command. If this property
	 * is set the value must be {@link Boolean#TRUE} or {@link Boolean#FALSE}.
	 */
	public static final Property<Boolean> SHOW_PROGRESS = TypedAnnotatable.property(Boolean.class, "showProgress", Boolean.FALSE);

	/**
	 * The ID of the progress DIV which shall be shown if the property
	 * {@link ButtonUIModel#showProgress()} is <code>true</code>.
	 */
	@FrameworkInternal
	public static final Property<DisplayValue> SHOW_PROGRESS_DIV_ID =
		TypedAnnotatable.property(DisplayValue.class, "showProgressDivID");

	private final IButtonRenderer view;

	protected M model;

	private boolean hasTabindex;
	private int tabindex;

	/**
	 * @param commandsByName
	 *            BHU please explain
	 */
	protected AbstractButtonControl(M model, IButtonRenderer view,
			Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
		assert model != null : "No button model.";
		assert view != null : "No button renderer.";
		this.model = model;
		this.view = view;
	}

	/**
	 * Creates a {@link ButtonControl} for the given {@link CommandModel model}.
	 * The renderer will be the renderer set as property of the key {@link #BUTTON_RENDERER}. If no
	 * such renderer was set a {@link ButtonRenderer} is used.
	 */
	public AbstractButtonControl(M model, Map<String, ControlCommand> commandsByName) {
		this(model, getRenderer(model), commandsByName);
	}

	private static final IButtonRenderer getRenderer(ButtonUIModel model) {
		IButtonRenderer renderer = model.get(BUTTON_RENDERER);
		return renderer == null ? ButtonRenderer.INSTANCE : renderer;
	}

	protected static Map<String, ControlCommand> addCommands(Map<String, ControlCommand> inheritedCommands,
			ControlCommand... commands) {
		Map<String, ControlCommand> result = new HashMap<>(inheritedCommands);
		for (ControlCommand command : commands) {
			Object clash = result.put(command.getID(), command);
			if (clash != null) {
				throw new IllegalArgumentException("The command name '" + command.getID() + "' already taken by '"
					+ clash + " '.");
			}
		}
		return result;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		model.addListener(FormMember.VISIBLE_PROPERTY, this);
		model.addListener(FormMember.LABEL_PROPERTY, this);
		model.addListener(FormMember.CLASS_PROPERTY, this);
		model.addListener(FormMember.TOOLTIP_PROPERTY, this);
		model.addListener(ButtonUIModel.NOT_EXECUTABLE_REASON_PROPERTY, this);
		model.addListener(ButtonUIModel.EXECUTABLE_PROPERTY, this);
		model.addListener(Focusable.FOCUS_PROPERTY, this);
		model.addListener(ButtonUIModel.IMAGE_PROPERTY, this);
		model.addListener(ButtonUIModel.NOT_EXECUTABLE_IMAGE_PROPERTY, this);
		model.addListener(ButtonUIModel.ALT_TEXT_PROPERTY, this);
	}

	@Override
	protected void internalDetach() {
		model.removeListener(ButtonUIModel.ALT_TEXT_PROPERTY, this);
		model.removeListener(ButtonUIModel.NOT_EXECUTABLE_IMAGE_PROPERTY, this);
		model.removeListener(ButtonUIModel.IMAGE_PROPERTY, this);
		model.removeListener(Focusable.FOCUS_PROPERTY, this);
		model.removeListener(ButtonUIModel.EXECUTABLE_PROPERTY, this);
		model.removeListener(ButtonUIModel.NOT_EXECUTABLE_REASON_PROPERTY, this);
		model.removeListener(FormMember.TOOLTIP_PROPERTY, this);
		model.removeListener(FormMember.CLASS_PROPERTY, this);
		model.removeListener(FormMember.LABEL_PROPERTY, this);
		model.removeListener(FormMember.VISIBLE_PROPERTY, this);
		super.internalDetach();
	}

	public boolean hasTabindex() {
		return hasTabindex;
	}

	public int getTabindex() {
		assert hasTabindex();

		return tabindex;
	}

	public void setTabindex(int tabindex) {
		this.hasTabindex = true;
		this.tabindex = tabindex;
	}

	public void removeTabindex() {
		this.hasTabindex = false;
	}

	/**
	 * Text that is written on the button.
	 */
	@Override
	@TemplateVariable("label")
	public String getLabel() {
		return this.model.getLabel();
	}

	@Override
	public String getTooltip() {
		return this.model.getTooltip();
	}

	@Override
	public String getTooltipCaption() {
		return this.model.getTooltipCaption();
	}

	@Override
	public String getAltText() {
		return this.model.getAltText();
	}

	public void setLabel(String label) {
		model.setLabel(label);
	}

	public void setVisible(boolean aLocallyVisible) {
		model.setVisible(aLocallyVisible);
	}

	public void enable() {
		model.setExecutable();
	}

	public void disable(ResKey disabledReason) {
		model.setNotExecutable(disabledReason);
	}

	public boolean hasImage() {
		return model.getImage() != null;
	}

	@Override
	public final ThemeImage getImage() {
		return this.model.getImage();
	}

	public void setImage(ThemeImage newValue) {
		model.setImage(newValue);
	}

	/**
	 * @see #getDisabledImage()
	 */
	public final boolean hasDisabledImage() {
		return this.model.getNotExecutableImage() != null;
	}

	@Override
	public final ThemeImage getDisabledImage() {
		return this.model.getNotExecutableImage();
	}

	/**
	 * @see #getActiveImage()
	 */
	public final boolean hasActiveImage() {
		return this.model.getActiveImage() != null;
	}

	@Override
	public final ThemeImage getActiveImage() {
		return this.model.getActiveImage();
	}

	@Override
	public char getAccessKey() {
		return model.getAccessKey();
	}

	/**
	 * @see #getDisabledImage()
	 */
	public void setDisabledImage(ThemeImage newValue) {
		model.setNotExecutableImage(newValue);
	}

	@Override
	public boolean isDisabled() {
		return !this.model.isExecutable();
	}

	@Override
	public boolean isActive() {
		return this.model.isLinkActive();
	}

	@Override
	public boolean isVisible() {
		return this.model.isVisible();
	}

	@Override
	public final void writeCssClassesContent(Appendable out) throws IOException {
		writeControlClassesContent(out);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		if (isVisible()) {
			view.write(context, out, this);
		} else {
			out.beginBeginTag(SPAN);
			writeIdAttribute(out);
			out.writeAttribute(STYLE_ATTR, "display: none;");
			out.endBeginTag();
			// Do not use an empty tag, because this would be interpreted as dangling start
			// tag by most browsers.
			out.endTag(SPAN);
		}

		if (FocusHandling.shouldFocus(context, getModel())) {
			FocusHandling.writeFocusRequest(out, getID());
		}
	}

	@Override
	protected String getTypeCssClass() {
		return "cButton";
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		out.append(this.model.getCssClasses());
		view.appendControlCSSClasses(out, this);
	}

	/**
	 * Writes the keyboard shortcut to execute this button.
	 * 
	 * @param out
	 *        Writes the access- attribute.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("accessKey")
	public void writeAccessKey(TagWriter out) throws IOException {
		char accessKey = model.getAccessKey();
		if (accessKey != 0) {
			out.append(accessKey);
		}
	}

	/**
	 * Determines which method should be executed when the button is pressed.
	 */
	@Override
	@TemplateVariable("onclick")
	public String getOnclick() {
		if (!isDisabled()) {
			StringBuilder out = new StringBuilder();
			try {
				writeOnClickContent(DefaultDisplayContext.getDisplayContext(), out);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
			return out.toString();
		} else {
			return null;
		}
	}

	public void writeOnClickContent(DisplayContext context, Appendable out) throws IOException {
		InvokeExpressionProvider provider = getModel().get(INVOKE_EXPRESSION_PROVIDER);
		if (provider != null) {
			provider.writeInvokeExpression(context, out, this);
		} else {
			HandleClickInvokeExpressionProvider.INSTANCE.writeInvokeExpression(context, out, this);
		}
	}

	/**
	 * Short explanation that is displayed when the mouse pointer is moved over the button.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        For writing the tooltip.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("tooltip")
	public void writeToolTip(DisplayContext context, TagWriter out) throws IOException {
		if (isDisabled()) {
			writeDisabledTooltip(context, out);
		} else {
			writeActiveTooltip(context, out);
		}
	}

	private void writeDisabledTooltip(DisplayContext context, TagWriter out) throws IOException {
		String title = getDisabledReasonTitle();
		String tooltip = getDisabledReason();
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltip(context, out, tooltip, title);
	}

	private void writeActiveTooltip(DisplayContext context, TagWriter out) throws IOException {
		String tooltip = getTooltip();
		if (!StringServices.isEmpty(tooltip)) {
			String title = getTooltipCaption();
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltip(context, out, tooltip, title);
		}
	}

	/**
	 * Renders an image next to the label if an image exists.
	 * 
	 * @param out
	 *        For writing the icon.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("commandImage")
	public void writeCommandImage(TagWriter out) throws IOException {
		ThemeImage img = getCommandImage();
		if (img != null) {
			img.writeWithCss(DefaultDisplayContext.getDisplayContext(), out, "cmdImg");
		}
	}

	private ThemeImage getCommandImage() {
		boolean disabled = isDisabled();
		ThemeImage img = null;
		Theme theme = ThemeFactory.getTheme();
		if (theme.getValue(Icons.BUTTONBAR_CUSTOM_ICONS)) {
			ThemeImage customIcon = getImage();
			if (customIcon != null) {
				img = disabled && hasDisabledImage() ? getDisabledImage() : customIcon;
			}
		} else if (theme.getValue(Icons.BUTTONBAR_ARROW_DECORATION)) {
			img = disabled ? Icons.BUTTON_ARROW_DISABLED : Icons.BUTTON_ARROW;
		}
		return img;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object anObject) {
		ButtonControl theButton = (ButtonControl) anObject;
		return this.getLabel().compareTo(theButton.getLabel());
	}

	@Override
	public M getModel() {
		return this.model;
	}

	/**
	 * Returns a text as a tooltip caption explaining why this button is disabled or
	 * <code>null</code>..
	 */
	public String getDisabledReason() {
		ResKey theKey = this.model.getNotExecutableReasonKey();
		if (theKey == null) {
			Logger.warn("Command '" + this.model.getLabel() + "' has no NotExecutableReasonKey (model is: "
				+ this.model + ")!", AbstractButtonControl.class);
			return null;
		}
		return Resources.getInstance().decodeMessageFromKeyWithEncodedArguments(theKey);
	}

	/**
	 * We return a title text if the no_executable_reason_key has a corresponding title key 
	 * or if a default title is defined.
	 * 
	 * @return the text to render as the title in the tooltip for disabled buttons or <code>null</code>
	 */
	public final String getDisabledReasonTitle() {
		Resources resources = Resources.getInstance();
		ResKey encodedMessage = this.model.getNotExecutableReasonKey();
		if (encodedMessage != null) {
			String nonExecTitle = resources.getString(Resources.derivedKey(encodedMessage, ".title"), null);
			if (!StringServices.isEmpty(nonExecTitle)) {
				return nonExecTitle;
			}
		}
		return resources.getString(I18NConstants.NON_EXECUTABLE_DEFAULT_TITLE, null);
	}
	
	@Override
	public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
		/* Prevent reacting on events of foreign models. */
		if (sender == getModel()) {
			requestRepaint();
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleCSSClassChange(Object sender, String oldValue, String newValue) {
		/* Prevent reacting on events of foreign models. */
		if (sender == getModel() && isVisible()) {
			view.handleClassPropertyChange(this, oldValue, newValue);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleTooltipChanged(Object sender, String oldValue, String newValue) {
		return repaintOnEvent(sender);
	}

	@Override
	public Bubble handleExecutableChange(ButtonUIModel sender, Boolean oldExecutability, Boolean newExecutability) {
		/* Prevent reacting on events of foreign models. */
		if (sender == getModel() && isVisible()) {
			if (newExecutability) {
				view.handleDisabledPropertyChange(this, true, false);
			} else {
				view.handleDisabledPropertyChange(this, false, true);
			}
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleNotExecutableReasonChange(ButtonUIModel sender, ResKey oldReason, ResKey newReason) {
		/* Prevent reacting on events of foreign models. */
		if (sender == getModel() && isVisible()) {
			view.handleDisabledReasonChanged(this, oldReason, newReason);
		}
		return Bubble.BUBBLE;
	}

	/**
	 * Pass the focus to this view.
	 */
	@Override
	public Bubble handleFocusRequested(Focusable sender) {
		Object fieldToFocus = sender;
		if (fieldToFocus == getModel()) {
			addUpdate(FocusHandling.focusRequest(getID()));
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleImageChanged(ButtonUIModel sender, ThemeImage oldValue, ThemeImage newValue) {
		/* Prevent reacting on events of foreign models. */
		if (sender == getModel() && isVisible()) {
			view.handleImagePropertyChange(this, IMAGE_PROPERTY, oldValue, newValue);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleDisabledImageChanged(ButtonUIModel sender, ThemeImage oldValue, ThemeImage newValue) {
		/* Prevent reacting on events of foreign models. */
		if (sender == getModel() && isVisible()) {
			view.handleImagePropertyChange(this, DISABLED_IMAGE_PROPERTY, oldValue, newValue);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleLabelChanged(Object sender, String oldLabel, String newLabel) {
		/* Prevent reacting on events of foreign models. */
		if (sender == getModel() && isVisible()) {
			view.handleLabelPropertyChange(this, newLabel);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleAltTextChanged(Object sender, String oldValue, String newValue) {
		return repaintOnEvent(sender);
	}

	private Bubble repaintOnEvent(Object sender) {
		/* Prevent reacting on events of foreign models. */
		if (sender == getModel() && isVisible()) {
			requestRepaint();
		}
		return Bubble.BUBBLE;
	}

	/**
	 * This method returns a provider for the ID of the progress div to show
	 * during command execution.
	 * 
	 * @return a provider for the ID of the progress div to show, or
	 *         <code>null</code> if no progress bar shall be shown.
	 */
	public DisplayValue showProgressDivID() {
		if (getModel().showProgress()) {
			return getModel().get(SHOW_PROGRESS_DIV_ID);
		}
		return null;
	}
	
	private static class HandleClickInvokeExpressionProvider implements InvokeExpressionProvider {

		private static final HandleClickInvokeExpressionProvider INSTANCE = new HandleClickInvokeExpressionProvider();

		@Override
		public void writeInvokeExpression(DisplayContext context, Appendable out, AbstractButtonControl<?> buttonControl)
				throws IOException {
			out.append("services.form.ButtonControl.handleClick(event,'");
			out.append(buttonControl.getID());
			out.append("', ");
			DisplayValue progressDivID = buttonControl.showProgressDivID();
			if (progressDivID != null) {
				out.append('\'');
				progressDivID.append(context, out);
				out.append('\'');
			} else {
				out.append("null");
			}
			out.append("); return false;");
		}
	}

	@Override
	public String toString() {
		return "ButtonControl('" + getLabel() + "')";
	}

}
