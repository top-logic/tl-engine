/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.document;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.top_logic.base.roles.Role;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Named;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.webfolder.model.WebFolderTreeBuilder;
import com.top_logic.common.webfolder.ui.WebFolderComponent.WebFolderUploadExecutor;
import com.top_logic.common.webfolder.ui.commands.Icons;
import com.top_logic.common.webfolder.ui.commands.UploadDialog.UploadExecutor;
import com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.messagebox.DialogFormBuilder;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.tool.boundsec.wrap.PersBoundChecker;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link DialogFormBuilder} for the configuration of the {@link DocumentTileConfig document
 * tile}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DocumentTileFormBuilder extends AbstractConfiguredInstance<DocumentTileFormBuilder.Config>
		implements DialogFormBuilder<DocumentTileConfig> {

	/**
	 * Typed configuration interface definition for {@link DocumentTileFormBuilder}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<DocumentTileFormBuilder> {

		/** Configuration name of the value of {@link #getResPrefix()}. */
		String RES_PREFIX_NAME = "resPrefix";

		/** Configuration name of the value of {@link #getSelectComponentName()}. */
		String SELECT_COMPONENT_NAME = "selectComponent";

		/** Configuration name of the value of {@link #getUploadComponentName()}. */
		String UPLOAD_COMPONENT_NAME = "uploadComponent";

		/**
		 * Name of the {@link LayoutComponent} to check executablility of upload command.
		 */
		@Name(UPLOAD_COMPONENT_NAME)
		ComponentName getUploadComponentName();

		/**
		 * Name of the {@link Selectable} whose selection is used to find available documents.
		 */
		@Mandatory
		@Name(SELECT_COMPONENT_NAME)
		ComponentName getSelectComponentName();

		/**
		 * {@link ResPrefix} for the created {@link FormMember}.
		 */
		@Mandatory
		@Name(RES_PREFIX_NAME)
		ResPrefix getResPrefix();

	}

	/** Name of the field for uploading a new document */
	protected static final String UPLOAD_FIELD = "newDocument";

	/** Name of the field for selecting an existing pdf document */
	protected static final String PDF_SELECT_FIELD = "availableDocuments";

	private final Selectable _selectComponent;

	/**
	 * Create a {@link DocumentTileFormBuilder}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public DocumentTileFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
		_selectComponent = findSelectComponent(context);
	}

	private Selectable findSelectComponent(InstantiationContext context) {
		ComponentName componentName = getConfig().getSelectComponentName();
		LayoutComponent configuredComponent = configuredComponent(componentName);
		if (configuredComponent == null) {
			context.error("Component '" + componentName + "' not found.");
			return null;
		}
		if (!(configuredComponent instanceof Selectable)) {
			context.error("Component '" + componentName + "' is not selectable.");
			return null;
		}
		return (Selectable) configuredComponent;
	}

	private LayoutComponent configuredComponent(ComponentName componentName) {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		MainLayout mainLayout = displayContext.getLayoutContext().getMainLayout();
		LayoutComponent selectComponent = mainLayout.getComponentByName(componentName);
		return selectComponent;
	}

	@Override
	public Consumer<DocumentTileConfig> initForm(FormContainer form, DocumentTileConfig model) {
		AbstractBoundWrapper context = getSelectedContext();
		FolderDefinition folder = getFolder(context);

		SelectField select = getPDFSelectField(folder);

		CommandField upload = createPDFUploadField(context, folder, select);

		form.addMember(select);
		form.addMember(upload);

		createUI(form);

		return finishedModel -> {
			Object singleSelection = select.getSingleSelection();
			String documentName = ((Named) singleSelection).getName();
			finishedModel.setDocumentName(documentName);
		};
	}

	/**
	 * Creates the gui layout.
	 */
	protected void createUI(FormContainer form) {
		template(form, div(verticalBox(
			fieldBox(PDF_SELECT_FIELD),
			fieldBox(UPLOAD_FIELD))));
	}

	/**
	 * Determines the {@link FolderDefinition} for the given context.
	 */
	protected FolderDefinition getFolder(Object context) {
		if (context instanceof WebFolderAware) {
			return ((WebFolderAware) context).getWebFolder();
		}
		return null;
	}

	private CommandField createPDFUploadField(AbstractBoundWrapper context, FolderDefinition folder,
			SelectField select) {
		CommandField upload = FormFactory.newCommandField(UPLOAD_FIELD, (Command) (executionContext -> {
			UploadExecutor uploadExecutor = createUploadExecutor(context, folder, document -> {
				select.setOptions(getPDFs(folder));
				if (isPDFFile(document)) {
					select.setAsSingleSelection(document);
				}
			});
			return uploadExecutor
				.createUploadDialog(com.top_logic.common.webfolder.ui.commands.I18NConstants.UPLOAD_DIALOG, folder)
				.open(executionContext);
		}));
		upload.setImage(Icons.UPLOAD);
		upload.setNotExecutableImage(Icons.UPLOAD);
		ExecutableState executability = getExecutablility(context);
		if (executability.isHidden()) {
			upload.setVisible(false);
		} else {
			if (executability.isDisabled()) {
				upload.setNotExecutable(executability.getI18NReasonKey());
			}
		}
		setLabel(upload);
		return upload;
	}

	/**
	 * Creates the {@link UploadExecutor} which actually performs the upload of the new document.
	 * 
	 * @param context
	 *        The context object from which the folder rises.
	 * @param folder
	 *        The {@link FolderDefinition} to create document in.
	 * @param continuation
	 *        Must be called with the uploaded document.
	 */
	protected UploadExecutor createUploadExecutor(AbstractBoundWrapper context, FolderDefinition folder, 
			Consumer<? super Named> continuation) {
		SingleSelectionModel folderSelection = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		AbstractMutableTLTreeModel<FolderNode> model =
			new AbstractMutableTLTreeModel<>(WebFolderTreeBuilder.INSTANCE, folder);
		folderSelection.setSingleSelection(model.getRoot());
		return new WebFolderUploadExecutor(folderSelection, continuation, 0L);
	}

	private ExecutableState getExecutablility(BoundObject context) {
		Collection<Role> someRoles =
			getRolesForPersBoundComp(getConfig().getUploadComponentName(), SimpleBoundCommandGroup.CREATE);
		boolean hasRole = AccessManager.getInstance().hasRole(context, someRoles);
		if (hasRole) {
			return ExecutableState.EXECUTABLE;
		} else {
			return ExecutableState.NO_EXEC_PERMISSION;
		}
	}

	/**
	 * @param securityID
	 *        ID of the persBoundCmp
	 * @param cmdGroup
	 *        the requested permission
	 * @return all roles that grant the given permission on the given persBoundComp
	 */
	private Collection<Role> getRolesForPersBoundComp(ComponentName securityID, BoundCommandGroup cmdGroup) {
		try {
			PersBoundComp myPers = SecurityComponentCache.getSecurityComponent(securityID);
			if (myPers == null) {
				Logger.error("No PersBoundComp for '" + securityID + "' found.",
					PersBoundChecker.class);
			} else {
				return myPers.rolesForCommandGroup(cmdGroup);
			}
		} catch (Exception e) {
			Logger.error("failed to getRolesForPersBoundComp " + securityID, e, DocumentTileFormBuilder.class);
		}
		return null;
	}

	private SelectField getPDFSelectField(FolderDefinition folder) {
		SelectField select = FormFactory.newSelectField(PDF_SELECT_FIELD, getPDFs(folder));
		select.setOptionLabelProvider(object -> ((Named) object).getName());
		select.setMandatory(true);
		setLabel(select);
		return select;
	}

	List<Named> getPDFs(FolderDefinition folder) {
		Collection<Named> contents = folder.getContents();
		List<Named> pdfDocuments = contents.stream()
			.filter(named -> isPDFFile(named))
			.collect(Collectors.toList());
		return pdfDocuments;
	}

	boolean isPDFFile(Named named) {
		return named.getName().endsWith("pdf");
	}

	private void setLabel(FormMember field) {
		field.setLabel(getConfig().getResPrefix().key(field.getName()));
	}

	private AbstractBoundWrapper getSelectedContext() {
		Object selected = _selectComponent.getSelected();
		if (selected == null) {
			throw new TopLogicException(com.top_logic.tool.execution.I18NConstants.ERROR_NO_SELECTION);
		}
		if (!(selected instanceof AbstractBoundWrapper)) {
			String businessLabel = MetaLabelProvider.INSTANCE.getLabel(selected);
			throw new TopLogicException(I18NConstants.ERROR_NO_DOCUMENTS_FOR_MODEL__MODEL.fill(businessLabel));
		}
		return (AbstractBoundWrapper) selected;
	}

}
