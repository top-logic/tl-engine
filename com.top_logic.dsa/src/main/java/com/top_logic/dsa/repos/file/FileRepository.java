/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos.file;

import java.io.File;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dsa.repos.AbstractRepository;
import com.top_logic.dsa.repos.BaseEntryInfo;
import com.top_logic.dsa.repos.RepositoryException;
import com.top_logic.dsa.repos.RepositoryInfo;

/**
 * File based implementation of a repository.
 * 
 * @author    <a href="mailto:Michael Gänsler@top-logic.com">Michael Gänsler</a>
 */
public class FileRepository<C extends FileContainerObject, L extends FileLeafObject> extends AbstractRepository<C, L> {

	/**
	 * Configuration of {@link FileRepository}.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config<I extends FileRepository<?, ?>> extends AbstractRepository.Config<I> {
		// Nothing in here...
	}

	/**
	 * Creates a new {@link FileRepository} from the given configuration.
	 * 
	 * @param aContext
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param aConfig
	 *        Configuration for this {@link FileRepository}.
	 */
	public FileRepository(InstantiationContext aContext, Config aConfig) {
		try {
			this.init(this.getRepositoryContainer(aConfig.getPath()),
					  this.getWorkareaContainer(aConfig.getWorkarea()), 
					  this.getAtticContainer(aConfig.getAttic()));
		}
		catch (RepositoryException ex) {
			aContext.error("Unable to create FileBasedRepository.", ex);
		}
	}

	@Override
	protected C createContainerReference(C aParent, String aName) {
		return this.toC(new FileContainerObject(aParent, aName));
	}

	@Override
	protected C createContainerReference(String aName) {
		return this.toC(new FileContainerObject(aName));
	}

	@Override
	protected RepositoryInfo createRepositoryInfo(String aPath, C aContainer) throws RepositoryException {
		File theFile = new File(aPath);

		return new BaseEntryInfo(new FileContainerObject(theFile), aContainer);
	}

	@Override
	protected String normalizePath(String aPath, String aFileName) {
		return aPath + File.separator + aFileName;
	}

	@Override
	protected C escapeDirNamesRecursivly(C anObject) throws RepositoryException {
		if ((anObject != null) && anObject.getFile() != null) { 
			return super.escapeDirNamesRecursivly(anObject);
		}
		else {
			return anObject;
		}
	}

	private C getRepositoryContainer(String aFolderName) throws RepositoryException {
		C theFolder = this.getBaseFile(aFolderName);

		if (theFolder == null) {
			throw new RepositoryException("Repository folder '" + aFolderName + "' not found!");
		}

		Logger.info("<init> Repository = " + theFolder.getAbsolutePath(), FileRepository.class);

		if (!theFolder.exists()) {
			theFolder.mkdirs();
			if (!theFolder.exists()) {
				Logger.warn("Folder " + theFolder.getAbsolutePath() + " could not be created.", FileRepository.class);
			}
		} else {
			if (!theFolder.isDirectory()) {
				throw new RepositoryException("Repository root ('" + theFolder + "') must be a directory");
			}
		}
		return theFolder;
	}

	private C getAtticContainer(String aFolderName) {
		C theFolder = null;

		if (!aFolderName.isEmpty()) {
			theFolder = this.getBaseFile(aFolderName);

			Logger.info("<init> Attic root = " + (theFolder == null ? "null" : theFolder.getAbsolutePath()), FileRepository.class);
		}

		return theFolder;
	}

	private C getWorkareaContainer(String aFolderName) throws RepositoryException {
		C theFolder = null;

		if (!aFolderName.isEmpty()) {
			theFolder = this.getBaseFile(aFolderName);

			if (theFolder != null) {
				String thePath = theFolder.getAbsolutePath();

				if (!theFolder.exists()) {
					theFolder.mkdirs();
				} 
				else if (!theFolder.isDirectory()) {
					throw new RepositoryException("Work area root ('" + thePath + "') must be a Directory");
				}

				Logger.info("<init> work area path = " + thePath, FileRepository.class);
			}
			else {
				Logger.warn("<init> Failed to initialize work area root ('" + aFolderName + "') no matching folder found", FileRepository.class);
			}
		}

		return theFolder;
	}

	@SuppressWarnings("unchecked")
	private C getBaseFile(String aFileName) {
        try {
			File theFile = new File(aFileName);
			return (C) new FileContainerObject(theFile);
		}
		catch (Exception ex) {	
			Logger.error("Failed to getBaseFile('" + aFileName + "')!", ex, FileRepository.class);

			return null;
		}
	}

	@Override
	public String toString() {
		// Note: This value is printed in the application monitor to identify the repository.
		return getRoot().getAbsolutePath();
	}

}

