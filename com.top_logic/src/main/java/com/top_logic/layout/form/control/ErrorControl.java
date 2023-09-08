/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.util.error.ErrorRenderUtil.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.BlockedStateChangedListener;
import com.top_logic.layout.form.CSSClassListener;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.ErrorChangedListener;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.HasErrorChanged;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.RemovedListener;
import com.top_logic.layout.form.WarningsChangedListener;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * {@link Control} that displays the error state of a {@link FormField}.
 * 
 * <p>
 * The control renders either as icon with the error message as title, or as
 * plain text using the CSS class {@link FormConstants#ERROR_CSS_CLASS}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ErrorControl extends AbstractControl implements CSSClassListener, RemovedListener, VisibilityListener,
		HasErrorChanged, ErrorChangedListener, WarningsChangedListener, ImmutablePropertyListener,
		BlockedStateChangedListener, DisabledPropertyListener {

	/**
	 * @see #isIconDisplay()
	 */
	private boolean iconDisplay;

	private final Set<? extends FormMember> _models;

	public ErrorControl(FormMember model, boolean iconDisplay) {
		this(Collections.singleton(model), iconDisplay);
	}

	public ErrorControl(Set<? extends FormMember> models, boolean iconDisplay) {
		super(Collections.EMPTY_MAP);
		_models = models;
		this.iconDisplay = iconDisplay;
	}
	
	@Override
	public Object getModel() {
		return null;
	}

	/**
	 * The {@link FormMember}s this {@link ErrorControl} shows the error state of.
	 */
	public Set<? extends FormMember> getModels() {
		return _models;
	}

	/**
	 * @see #isIconDisplay()
	 */
	public void setIconDisplay(boolean iconDisplay) {
		this.iconDisplay = iconDisplay;
		requestRepaint();
	}
	
	/**
	 * Whether the error is rendered as icon or plain text.
	 */
	public boolean isIconDisplay() {
		return iconDisplay;
	}

	@Override
	protected String getTypeCssClass() {
		return "cError";
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		boolean hasError = false;
		boolean hasWarnings = false;
		boolean isActive = false;

		List<FormField> reportedFields = null;
		for (FormMember member : _models) {
			if (member.isVisible()) {
				isActive = member.isActive();

				if (member instanceof FormField) {
					FormField field = (FormField) member;
					boolean localWarnings = field.hasWarnings();
					boolean localErrors = field.hasError();

					hasWarnings |= localWarnings;
					hasError |= localErrors;

					if (localErrors || localWarnings) {
						if (reportedFields == null) {
							reportedFields = new ArrayList<>();
						}
						reportedFields.add(field);
					}
				} else if (member instanceof FormContainer) {
					Iterator<? extends FormField> fields = ((FormContainer) member).getDescendantFields();
					while (fields.hasNext()) {
						FormField inner = fields.next();

						boolean localWarnings = inner.hasWarnings();
						boolean localErrors = inner.hasError();

						hasWarnings |= localWarnings;
						hasError |= localErrors;

						if (localErrors || localWarnings) {
							if (reportedFields == null) {
								reportedFields = new ArrayList<>();
							}
							reportedFields.add(inner);
						}
					}
				}
			}
		}
		
		// An icon (error display or spacer icon) is rendered for all fields
		// that accept user input. This is necessary to prevent the form layout
		// from reorganizing after the error state of a field changes.
		boolean   needsIcon   = isActive || hasError || hasWarnings;
		
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);

		if (!needsIcon) {
		    out.writeAttribute(STYLE_ATTR, "display: none;");
		}

		out.endBeginTag();

        if (needsIcon) {
			if (iconDisplay) {
				XMLTag icon = this.getDisplayIcon(hasError, hasWarnings).toIcon();
				icon.beginBeginTag(context, out);

				out.writeAttribute(CLASS_ATTR, this.getCssClass(hasError, hasWarnings));

				// TODO: Enable theme to report image dimensions.
				out.writeAttribute(WIDTH_ATTR, 8);
				out.writeAttribute(HEIGHT_ATTR, 8);

				if (hasError || hasWarnings) {
					OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
						tooltipContent(reportedFields));
				}
	
				// Required attribute. Could mark the the image as error display
				// for the corresponding field for accessibility purposes.
				out.writeAttribute(ALT_ATTR, "");
				icon.endEmptyTag(context, out);
			} else {
				// Provide an initial value, if the corresponding field has an error.
				if (hasError || hasWarnings) {
					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, this.getCssClass(hasError, hasWarnings));
					out.endBeginTag();
					{
						writeProblems(out, reportedFields);
					}
					out.endTag(SPAN);
				}
			}
		}
		out.endTag(SPAN);
	}

	private String tooltipContent(List<FormField> reportedFields) {
		TagWriter out = new TagWriter();
		writeProblems(out, reportedFields);
		return out.toString();
	}

	private void writeProblems(TagWriter out, List<FormField> reportedFields) {
		boolean multipleModels = _models.size() > 1;

		boolean first = true;
		for (FormField field : reportedFields) {
			if (field.hasError()) {
				if (first) {
					first = false;
				} else {
					out.beginBeginTag(BR);
					out.endEmptyTag();
				}
				if (multipleModels) {
					out.writeText(field.getLabel());
					out.writeText(": ");
				}
				writeLines(out, field.getError());
			}
			if (field.hasWarnings()) {
				for (String warning : field.getWarnings()) {
					if (first) {
						first = false;
					} else {
						out.beginBeginTag(BR);
						out.endEmptyTag();
					}
					if (multipleModels) {
						out.writeText(field.getLabel());
						out.writeText(": ");
					}
					writeLines(out, warning);
				}
			}
		}
	}

	protected ThemeImage getDisplayIcon(boolean hasError, boolean hasWarnings) {
		if (hasError) {
	        return Icons.ALERT;
	    }
		else if (hasWarnings) {
	        return Icons.WARN;
	    }
	    else {
	        return Icons.ALERT_SPACER;
	    }
	}
	
	protected String getCssClass(boolean hasError, boolean hasWarnings) {
		if (hasError) {
            return FormConstants.ERROR_CSS_CLASS;
        }
		else if (hasWarnings) {
            return FormConstants.WARNING_CSS_CLASS;
        }
        else {
            return FormConstants.NO_ERROR_CSS_CLASS;
        }
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		out.append(FormConstants.IS_ERROR_CSS_CLASS);
	}

	@Override
	public boolean isVisible() {
		for (FormMember member : _models) {
			if (member.isActive()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();

		for (FormMember model : _models) {
			registerAsListener(model);
		}
	}

	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();

		for (FormMember model : _models) {
			deregisterAsListener(model);
		}
	}

	private void registerAsListener(FormMember model) {
		model.addListener(FormMember.REMOVED_FROM_PARENT, this);
		model.addListener(FormMember.CLASS_PROPERTY, this);
		model.addListener(FormMember.VISIBLE_PROPERTY, this);
		model.addListener(FormField.HAS_ERROR_PROPERTY, this);
		model.addListener(FormField.HAS_WARNINGS_PROPERTY, this);
		model.addListener(FormMember.IMMUTABLE_PROPERTY, this);
		model.addListener(FormMember.DISABLED_PROPERTY, this);
		model.addListener(FormField.BLOCKED_PROPERTY, this);
	}

	private void deregisterAsListener(FormMember model) {
		model.removeListener(FormField.BLOCKED_PROPERTY, this);
		model.removeListener(FormMember.DISABLED_PROPERTY, this);
		model.removeListener(FormMember.IMMUTABLE_PROPERTY, this);
		model.removeListener(FormField.HAS_WARNINGS_PROPERTY, this);
		model.removeListener(FormField.HAS_ERROR_PROPERTY, this);
		model.removeListener(FormMember.VISIBLE_PROPERTY, this);
		model.removeListener(FormMember.CLASS_PROPERTY, this);
		model.removeListener(FormMember.REMOVED_FROM_PARENT, this);
	}

	@Override
	public Bubble handleCSSClassChange(Object sender, String oldValue, String newValue) {
		// TODO: Nothing to to here?
		return Bubble.BUBBLE;
	}

	private Bubble repaintOnEvent() {
		if (!skipEvent()) {
			requestRepaint();
		}
		return Bubble.BUBBLE;
	}

	private boolean skipEvent() {
		// Workaround for events being fired during rendering form the observed form members. This
		// happens, if a `FormTableModel` is rendered within a form group showing an error control
		// in its title area. The form table model lazily initializes its `FormMember`s and
		// adds them to its group. This group is observed by this `ErrorControl`. The events
		// then invalidate the error display in the group title. By ignoring those events, initial
		// errors on fields in a form table model wont be displayed in a group header.
		if (!DefaultDisplayContext.getDisplayContext().getLayoutContext().isInCommandPhase()) {
			return true;
		}
		return false;
	}

	@Override
	public Bubble handleRemovedFromParent(FormMember sender, FormContainer formerParent) {
		if (skipEvent()) {
			detach();
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
		return repaintOnEvent();
	}

	@Override
	public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		return repaintOnEvent();
	}

	@Override
	public Bubble handleIsBlockedChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		return repaintOnEvent();
	}

	@Override
	public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
		return repaintOnEvent();
	}

	@Override
	public Bubble warningsChanged(FormField field, List<String> oldWarnings, List<String> newWarnings) {
		return repaintOnEvent();
	}

	@Override
	public Bubble hasWarningsChanged(FormField field, Boolean oldWarnings, Boolean newWarnings) {
		return repaintOnEvent();
	}

	@Override
	public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
		return repaintOnEvent();
	}

	@Override
	public Bubble handleErrorChanged(FormField sender, String oldError, String newError) {
		return repaintOnEvent();
	}

}
