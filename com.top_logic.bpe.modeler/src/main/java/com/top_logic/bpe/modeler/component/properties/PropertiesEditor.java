/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.component.properties;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.UpdateFactory;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.MetaControlProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.boxes.reactive_tag.DescriptionCellControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tag.LayoutHtml;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * {@link ModelBuilder} creating a {@link FormContext} with all fields for a {@link TLObject}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PropertiesEditor implements ModelBuilder {

	/**
	 * {@link ControlProvider} creating {@link DescriptionCellControl}s.
	 *
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class CellControlProvider extends AbstractFormFieldControlProvider {
		private final ControlProvider _inputCP;

		/**
		 * Singleton {@link PropertiesEditor.CellControlProvider} instance.
		 */
		public static final CellControlProvider META_INSTANCE = new CellControlProvider(MetaControlProvider.INSTANCE);

		/**
		 * Creates a {@link PropertiesEditor.CellControlProvider}.
		 * 
		 * @param inputCP
		 *        The {@link ControlProvider} used for creating the actual input, label and error
		 *        controls.
		 */
		public CellControlProvider(ControlProvider inputCP) {
			_inputCP = inputCP;
		}

		@Override
		protected Control createInput(FormMember member) {
			DescriptionCellControl cell =
				DescriptionCellControl.createInputBox(member, _inputCP, FormTemplateConstants.STYLE_DIRECT_VALUE, true,
					false);
			cell.setLabelAbove(true);
			return cell;
		}
	}

	/**
	 * Singleton {@link PropertiesEditor} instance.
	 */
	public static final PropertiesEditor INSTANCE = new PropertiesEditor();

	private PropertiesEditor() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel == null) {
			return null;
		}
		AttributeFormContext formContext = new AttributeFormContext(aComponent.getResPrefix());

		// Note: What follows is a mega-hack to simulate a JSP generically displaying all members of
		// the given model with default settings.

		FormTag parentTag = new FormTag() {
			@Override
			public FormContainer getFormContainer() {
				return formContext;
			}
		};

		LayoutHtml pageTag = new LayoutHtml() {
			@Override
			public LayoutComponent getComponent() {
				return aComponent;
			}
		};

		parentTag.setParent(pageTag);

		Wrapper object = (Wrapper) businessModel;
		List<? extends TLStructuredTypePart> allParts = object.tType().getAllParts().stream()
			.filter(p -> !DisplayAnnotations.isHidden(p)).collect(Collectors.toList());
		Collections.sort(allParts,
			(p1, p2) -> Double.compare(DisplayAnnotations.getSortOrder(p1), DisplayAnnotations.getSortOrder(p2)));
		UpdateFactory overlay = formContext.editObject(object);
		for (TLStructuredTypePart part : allParts) {
			if (DisplayAnnotations.isHidden(part)) {
				continue;
			}
			boolean isDisabled = !DisplayAnnotations.isEditable(part);
			AttributeUpdate update = overlay.newEditUpdateDefault(part, isDisabled);
			if (update == null) {
				continue;
			}

			FormMember member = formContext.addFormConstraintForUpdate(update);
			if (member == null) {
				continue;
			}
			ControlProvider cp = member.getControlProvider();
			if (cp == null) {
				member.setControlProvider(CellControlProvider.META_INSTANCE);
			} else {
				member.setControlProvider(new CellControlProvider(new InputOnlyControlProvider(cp)));
			}
		}

		return formContext;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null || aModel instanceof TLObject;
	}

}
