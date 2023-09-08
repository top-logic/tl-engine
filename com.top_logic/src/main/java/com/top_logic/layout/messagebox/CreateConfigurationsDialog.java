/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.mig.html.layout.tiles.component.SimpleFormBuilder;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCreateConfigurationDialog} for creating a list of {@link ConfigurationItem}.
 * 
 * <p>
 * The {@link CreateConfigurationsDialog} gets a {@link List} of {@link ModelPartDefinition}. Each
 * {@link ModelPartDefinition} represents a part of the resulting form to create a
 * {@link ConfigurationItem} finally contained in {@link #getModel()}. The index of each
 * {@link ModelPartDefinition} in the given list of definitions is the same as the
 * {@link ConfigurationItem} for that definition in {@link #getModel()} and in the list delivered to
 * {@link #getOkHandle() OK handle}.
 * </p>
 * 
 * @see CreateConfigurationDialog Creating exactly one item.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateConfigurationsDialog extends AbstractCreateConfigurationDialog<List<? extends ConfigurationItem>> {

	/**
	 * Definition for one of the edited {@link ConfigurationItem}s.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ModelPartDefinition<T extends ConfigurationItem> {

		private final T _model;

		private DialogFormBuilder<? super T> _formBuilder = SimpleFormBuilder.getInstance();

		private ResKey _legendKey;

		/**
		 * Creates a new {@link ModelPartDefinition}.
		 * 
		 * @param model
		 *        See {@link #model()}.
		 */
		public ModelPartDefinition(T model) {
			_model = model;
		}
		
		/**
		 * Creates a new {@link ModelPartDefinition}.
		 * 
		 * @param type
		 *        The concrete {@link ConfigurationItem} type to create.
		 */
		public ModelPartDefinition(Class<T> type) {
			this(TypedConfiguration.newConfigItem(type));
		}

		/**
		 * The {@link ConfigurationItem} to edit.
		 */
		public T model() {
			return _model;
		}

		/**
		 * The {@link DialogFormBuilder} which creates the part of the form to edit the actual
		 * {@link ConfigurationItem}.
		 */
		public DialogFormBuilder<? super T> formBuilder() {
			return _formBuilder;
		}

		/**
		 * Setter for {@link #formBuilder()}.
		 */
		public void setFormBuilder(DialogFormBuilder<? super T> formBuilder) {
			_formBuilder = Objects.requireNonNull(formBuilder, "Form builder must not be null.");
		}

		/**
		 * The {@link ResKey} which is used for the legend of created form part to edit the actual
		 * form model.
		 */
		public ResKey legendKey() {
			return _legendKey;
		}

		/**
		 * Setter for {@link #legendKey()}.
		 */
		public void setLegendKey(ResKey legendKey) {
			_legendKey = legendKey;
		}

	}

	private final List<? extends ConfigurationItem> _models;

	private final List<ModelPartDefinition<? extends ConfigurationItem>> _partDefinitions;

	@SuppressWarnings("rawtypes")
	private Consumer[] _consumers;

	private boolean _isEmpty;

	/**
	 * Creates a new {@link CreateConfigurationDialog} without {@link #getOkHandle()}.
	 * 
	 * <p>
	 * It is strongly recommend to call {@link #setOkHandle(Function)}, as otherwise the whole
	 * dialog is useless.
	 * </p>
	 * 
	 * @param partDefinitions
	 *        The definitions of the {@link ConfigurationItem} to instantiate.
	 * @param title
	 *        The dialog title.
	 * @param width
	 *        The width of the dialog.
	 * @param height
	 *        The height of the dialog.
	 */
	public CreateConfigurationsDialog(List<ModelPartDefinition<? extends ConfigurationItem>> partDefinitions,
			ResKey title, DisplayDimension width, DisplayDimension height) {
		super(DefaultDialogModel.dialogModel(title, width, height));
		_partDefinitions = partDefinitions;
		_models = partDefinitions
			.stream()
			.map(ModelPartDefinition::model)
			.collect(Collectors.toList());
		_consumers = new Consumer[partDefinitions.size()];
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setOkHandle(Function<? super List<? extends ConfigurationItem>, HandlerResult> okHandle) {
		Function<? super List<? extends ConfigurationItem>, HandlerResult> enhancedHandle;
		if (okHandle != null) {
			enhancedHandle = models -> {
				for (int i = 0; i < models.size(); i++) {
					_consumers[i].accept(models.get(i));
				}
				return okHandle.apply(models);
			};
		} else {
			enhancedHandle = null;
		}
		super.setOkHandle(enhancedHandle);
	}

	@Override
	protected void fillFormContext(FormContext context) {
		HTMLTemplateFragment[] partTemplates = new HTMLTemplateFragment[modelPartDefinitions().size()];
		boolean emptyForm = true;
		for (int i = 0, cnt = modelPartDefinitions().size(); i < cnt; i++) {
			HTMLTemplateFragment partTemplate = buildPartMember(context, i);
			if (partTemplate == null) {
				// hide empty member
				partTemplate = empty();
			} else {
				emptyForm = false;
			}
			partTemplates[i] = partTemplate;
		}
		template(context, div(partTemplates));
		_isEmpty = emptyForm;
	}

	/**
	 * Whether the displayed {@link FormContext} will not display any visible field.
	 * 
	 * <p>
	 * For determination, {@link #getFormContext()} is called.
	 * </p>
	 */
	public boolean hasEmptyForm() {
		if (!hasFormContext()) {
			// Calling #getFormContext initialises the local variable.
			getFormContext();
		}
		return _isEmpty;
	}

	/**
	 * Creates the {@link FormMember} to edit the {@link ConfigurationItem} created by the
	 * {@link ModelPartDefinition} at the given index.
	 * 
	 * @param parent
	 *        The parent of the created {@link FormMember}.
	 * @param index
	 *        The index of the {@link ModelPartDefinition} in {@link #modelPartDefinitions()} to
	 *        create a {@link FormMember} for.
	 * 
	 * @return A {@link HTMLTemplateFragment} to to display the {@link ModelPartDefinition} at given
	 *         index, or <code>null</code> when no visible content was created.
	 */
	protected <T extends ConfigurationItem> HTMLTemplateFragment buildPartMember(FormContainer parent, int index) {
		@SuppressWarnings("unchecked")
		ModelPartDefinition<T> partDefinition = (ModelPartDefinition<T>) modelPartDefinitions().get(index);
		FormGroup modelBox = Fields.group(parent, "modelBox" + index);
		FormGroup modelContent = Fields.group(modelBox, "modelContent" + index);
		@SuppressWarnings("unchecked")
		T model = (T) getModel().get(index);
		DialogFormBuilder<? super T> formBuilder = partDefinition.formBuilder();
		Consumer<? super T> callback = formBuilder.initForm(modelContent, model);
		_consumers[index] = callback;

		if (isEmpty(modelContent)) {
			// No parameters to fill by user.
			return null;
		}
		ResKey legendKey = partDefinition.legendKey();
		if (legendKey != null) {
			template(modelBox, div(
				fieldsetBoxWrap(
					resource(legendKey),
					member(modelContent.getName()),
					ConfigKey.field(modelBox))));
		} else {
			template(modelBox, div(member(modelContent.getName())));
		}
		return member(modelBox.getName());
	}

	private boolean isEmpty(FormContainer container) {
		return !container.getMembers().hasNext();
	}

	@Override
	public List<? extends ConfigurationItem> getModel() {
		return _models;
	}

	@Override
	protected HandlerResult beforeSave(DisplayContext context) {
		for (ConfigurationItem model : getModel()) {
			ConfigurationTranslator.INSTANCE.translateIfAutoTranslateEnabled(model);
		}

		return super.beforeSave(context);
	}

	/**
	 * Returns the {@link ModelPartDefinition}s given in the constructor.
	 */
	public List<ModelPartDefinition<? extends ConfigurationItem>> modelPartDefinitions() {
		return _partDefinitions;
	}

	/**
	 * Service method to either open the dialog when the user can update or check informations or to
	 * directly call {@link #getApplyClosure()}, if the displayed dialog is empty.
	 */
	public HandlerResult openIfNecessaryOrConfirm(DisplayContext context) {
		if (hasEmptyForm()) {
			return getApplyClosure().executeCommand(context);
		} else {
			return open(context);
		}
	}

}

