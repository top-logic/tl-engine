/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateFolder;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateLocation;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.layout.tree.model.StructureTreeModel.Node;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.tool.dataImport.ImportZipDataCommand;
import com.top_logic.tool.dataImport.UploadDataDialog;
import com.top_logic.tool.dataImport.ZipImporter;

/**
 * Command executes the import of ScriptRecorder templates.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ImportScriptRecorderTemplateCommand extends ImportZipDataCommand {

	/**
	 * Content type of files compressed to a ZIP file.
	 */
	protected static final String ZIP_COMPRESSED_CONTENT_TYPE = "application/x-zip-compressed";

	private final TemplateTreeComponent _templateComponent;

	/**
	 * Creates a {@link ImportScriptRecorderTemplateCommand} for the given upload dialog.
	 */
	public ImportScriptRecorderTemplateCommand(UploadDataDialog uploadDialog, TemplateTreeComponent templateComponent) {
		super(uploadDialog);

		_templateComponent = Objects.requireNonNull(templateComponent);
	}

	@Override
	protected void uploadPostProcess(BinaryData data) {
		if (ZIP_COMPRESSED_CONTENT_TYPE.equals(data.getContentType())) {
			super.uploadPostProcess(data);
			_templateComponent.resetTreeModel();
		} else {
			TLTreeNode<?> child = createTemplateNode(data);

			_templateComponent.resetTreeModel();
			_templateComponent.setSelected(child);
		}
	}

	private TLTreeNode<?> createTemplateNode(BinaryData data) throws IOError {
		TemplateResource importSingleTemplateFile = importSingleTemplateFile(data);

		return createTemplateNode(importSingleTemplateFile, ScriptTemplateUtil.getSelectedNode(_templateComponent));
	}

	private TLTreeNode<?> createTemplateNode(TemplateResource newTemplate, Node rootNodeToImportTo) {
		TLTreeNode<?> newTemplateResourceNode;

		if (rootNodeToImportTo.isLeaf()) {
			newTemplateResourceNode = rootNodeToImportTo.getParent().createChild(newTemplate);
		} else {
			newTemplateResourceNode = rootNodeToImportTo.createChild(newTemplate);
		}

		newTemplate.initLocation(newTemplateResourceNode);

		return newTemplateResourceNode;
	}

	private TemplateResource importSingleTemplateFile(BinaryData data) throws IOError {
		try {
			String pathSuffixToTemplateRoot = createResourceSuffixToTemplateRoot(data.getName());

			File newTemplateFile = new File(Workspace.topLevelWebapp(), getResourcePath(pathSuffixToTemplateRoot));

			newTemplateFile.getParentFile().mkdirs();
			if (newTemplateFile.createNewFile()) {
				writeSingleTemplateFile(data, newTemplateFile);
			}

			return new TemplateResource(pathSuffixToTemplateRoot);
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	String getResourcePath(String pathSuffixToTemplateRoot) {
		return ScriptTemplateUtil.getTemplateRootPath(_templateComponent) + pathSuffixToTemplateRoot;
	}

	String createResourceSuffixToTemplateRoot(String name) {
		return getSelectedContainingTemplateFolderResourceSuffix() + name;
	}

	private void writeSingleTemplateFile(BinaryData data, File templateFile) throws IOException {
		try (InputStream stream = data.getStream()) {
			FileUtilities.copyToFile(stream, templateFile);
		}
	}

	private String getSelectedContainingTemplateFolderResourceSuffix() {
		return getSelectedContainingTemplateFolder().getResourceSuffix();
	}

	private TemplateFolder getSelectedContainingTemplateFolder() {
		TemplateLocation selectedLocation = ScriptTemplateUtil.getSelectedTemplateLocation(_templateComponent);

		return isTemplateResource(selectedLocation) ? selectedLocation.getParent() : (TemplateFolder) selectedLocation;
	}

	private boolean isTemplateResource(TemplateLocation selectedLocation) {
		return selectedLocation instanceof TemplateResource;
	}

	@Override
	protected ZipImporter createImporter(BinaryData data) {
		return new ZipImporter(data) {
			@Override
			protected void importZipEntry(ZipInputStream zipStream, ZipEntry zipEntry) {
				try {
					createZipEntryFile(zipStream, zipEntry);
				} catch (IOException exception) {
					throw new IOError(exception);
				}
			}

			private File createZipEntryFile(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
				File file = getFile(zipEntry);

				if (zipEntry.isDirectory()) {
					file.mkdirs();
				} else {
					createZipEntryFile(zipInputStream, file);
				}

				return file;
			}

			private void createZipEntryFile(ZipInputStream zipInputStream, File file) throws IOException {
				file.getParentFile().mkdirs();

				if (file.createNewFile()) {
					FileUtilities.copyToFile(zipInputStream, file);
				}
			}

			private File getFile(ZipEntry zipEntry) {
				return getTemplateRootFile(zipEntry.getName());
			}

			private File getTemplateRootFile(String entryName) {
				String resourceSuffixToTemplateRoot = createResourceSuffixToTemplateRoot(entryName);

				return new File(Workspace.topLevelWebapp(), getResourcePath(resourceSuffixToTemplateRoot));
			}
		};
	}

}
