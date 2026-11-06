/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.config.diff.filediff.FileDiff;
import com.top_logic.config.diff.filediff.FileDiffGenerator;
import com.top_logic.config.diff.filediff.Line;
import com.top_logic.config.diff.filediff.Region;
import com.top_logic.config.diff.zip.ZipReader.EntryResult;
import com.top_logic.config.diff.zip.ZipReader.EntryResult.State;

/**
 * This class holds informations about whether two versions of a files have changes or not,
 * and some statistics about the extend of those changes.
 * 
 * @author     <a href="mailto:aru@top-logic.com">aru</a>
 */
public class CompareStatistics {

	private EntryResult entryResult = null;
	private CompareStatistics parent = null;
	
	private Set<CompareStatistics> seenChildren = null;
	private Set<Integer> seenChangedRegions = null;

	private int     currentChangedRegion;
	
	private boolean hasChanges;
	private int     numOfRegions 				= 0;
	private int     numOfUnchangedRegions 		= 0; 
	private int     numOfChangedRegions 		= 0; 
	private int     numOfSourceLines 			= 0;
	private int     numOfDestLines 				= 0;
	private int     numOfChangedSourceLines 	= 0; 
	private int     numOfUnchangedSourceLines	= 0;
	private int     numOfChangedDestLines 		= 0; 
	private int     numOfUnchangedDestLines		= 0;
	
	private boolean initialized                 = false;
	private boolean allChangesSeen				= false;
	private boolean allChildrenSeen				= false;
	
	
	
	public CompareStatistics() {
		
		seenChangedRegions = new HashSet<>();
		currentChangedRegion = -1;
	}
	

	public CompareStatistics(EntryResult entryResult) {
		
		setEntryResult(entryResult);
		setChanged(!EntryResult.State.NO_CHANGE.equals(entryResult.getState()));
		seenChangedRegions = new HashSet<>();
		seenChildren = new HashSet<>();
		currentChangedRegion = -1;
		// files have no children
		if (!(entryResult.isDirectory())) {
			setAllChildrenSeen(true);
		}
		// for the propagation..
		if (getEntryResult().getParent() != null) {
			CompareStatistics parentsStatistics = getEntryResult().getParent().getStatistics();
			setParent(parentsStatistics);
		}
		if (getState().equals(EntryResult.State.NO_CHANGE) && (getParent() != null)) {
			getParent().allMyChangesAreSeen(this);
			setAllChangesSeen(true);
		}
	}
	
	/**
	 * Is called in the <tt>setEntryResult()</tt> method. Retrieves informations about
	 * {@link Region}s and {@link Line}s, if they are changed and the amount of them. 
	 */
	public void findChanges(FileDiff fileDiff) {
		boolean notFound = true;
		
		List<Region> regions = fileDiff.getRegions();
		
		setNumOfRegions(regions.size());
		
		for (Region region : regions) {

			int numOfSourceLinesInRegion = region.getSourceLines().size();
			numOfSourceLines += numOfSourceLinesInRegion;
			int numOfDestLinesInRegion = region.getDestLines().size();
			numOfDestLines += numOfDestLinesInRegion;
			/*
			 * If the region has changes...
			 */
			if (region.isChanged()) {
				numOfChangedRegions++;
				numOfChangedSourceLines += numOfSourceLinesInRegion;
				numOfChangedDestLines += numOfDestLinesInRegion;
				/*
				 *  if the region has changes, hasChanges must be set true
				 */
				if(notFound) {
					setChanged(true);
					notFound = false;
				}
			}
			/*
			 * if the region is unchanged... 
			 */
			else {
				numOfUnchangedRegions++;
				numOfUnchangedSourceLines += numOfSourceLinesInRegion;
				numOfUnchangedDestLines += numOfDestLinesInRegion;
			}
		}
	}

	/**
	 * This Method computes a {@link FileDiff}, representing the changes between two versions
	 * of a textfile. 
	 * 
	 * @return {@link FileDiff}
	 */
	public FileDiff getFileDiff() {
		String sourceText = getEntryResult().getSource();
		String destText = getEntryResult().getDest();
		FileDiff fileDiff = FileDiffGenerator.INSTANCE.generate(sourceText, destText);
		if (!initialized) {
			findChanges(fileDiff);
			initialized = true;
		}
		return fileDiff;
	}
	
	/**
	 * Renders the differences of the compared entry as HTML table.
	 *
	 * @param aSource
	 *         Heading of the column showing the source version.
	 * @param aDest
	 *         Heading of the column showing the destination version.
	 * @return The HTML representation of the {@link #getFileDiff() file difference}.
	 */
	public String getHTML(String aSource, String aDest) {		
		return FileDiffGenerator.INSTANCE.generateHtmlToString(getFileDiff(), aSource, aDest, this);
	}

	/**
	 * Setter for {@link #getEntryResult()}.
	 */
	public void setEntryResult(EntryResult entryResult) {
		this.entryResult = entryResult;
	}

	/**
	 * Whether the source and destination version of the compared entry differ.
	 */
	public boolean hasChanges() {
		boolean result = hasChanges;
		return result;
	}
	
	private void setChanged(boolean changed) {
		hasChanges = changed;
	}

	/**
	 * The compared entry these statistics are about.
	 */
	public EntryResult getEntryResult() {
		return (entryResult);
	}

	/**
	 * The plain name of the compared entry.
	 *
	 * @see EntryResult#getPlainName()
	 */
	public String getName() {
		return (getEntryResult().getPlainName());
	}

	/**
	 * The number of {@link Region}s of the {@link #getFileDiff() file difference}.
	 */
	public int getNumOfRegions() {
		return (numOfRegions);
	}

	private void setNumOfRegions(int numOfRegions) {
		this.numOfRegions = numOfRegions;
	}

	/**
	 * The number of {@link Region}s without changes.
	 */
	public int getNumOfUnchangedRegions() {
		return (numOfUnchangedRegions);
	}

	/**
	 * The number of lines of the source version.
	 */
	public int getNumOfSourceLines() {
		return (numOfSourceLines);
	}

	/**
	 * The number of lines of the destination version.
	 */
	public int getNumOfDestLines() {
		return (numOfDestLines);
	}

	/**
	 * The number of source lines in changed {@link Region}s.
	 */
	public int getNumOfChangedSourceLines() {
		return (numOfChangedSourceLines);
	}

	/**
	 * The comparison state of the compared entry.
	 */
	public State getState() {
		return (entryResult.getState());
	}

	/**
	 * Textual representation of {@link #getState()}.
	 */
	public String getStateAsString() {
		return (entryResult.getStateAsString());
	}

	/**
	 * The number of source lines in unchanged {@link Region}s.
	 */
	public int getNumOfUnchangedSourceLines() {
		return (numOfUnchangedSourceLines);
	}

	/**
	 * The number of destination lines in changed {@link Region}s.
	 */
	public int getNumOfChangedDestLines() {
		return (numOfChangedDestLines);
	}

	/**
	 * The number of destination lines in unchanged {@link Region}s.
	 */
	public int getNumOfUnchangedDestLines() {
		return (numOfUnchangedDestLines);
	}

	/**
	 * The number of {@link Region}s with changes.
	 */
	public int getNumOfChangedRegions() {
		return (numOfChangedRegions);
	}

	/**
	 * Setter for {@link #getNumOfChangedRegions()}.
	 */
	public void setNumOfChangedRegions(int numOfChangedRegions) {
		this.numOfChangedRegions = numOfChangedRegions;
	}

	/**
	 * Marks the changed {@link Region} with the given number as seen by the user.
	 *
	 * @param regionID
	 *         The 1-based number of the changed region, at most {@link #getNumOfChangedRegions()}.
	 */
	public void markChangedRegionAsEvaluated(int regionID) {
		if(regionID<1){
			throw new IllegalArgumentException("regionID="+regionID+", must be greater than zero");
		}
		if(regionID>numOfChangedRegions){
			throw new IllegalArgumentException("regionID="+regionID+", numOfChangedRegions="+numOfChangedRegions+", regionsID must be less or equal numOfChangedRegions");
		}
		
		seenChangedRegions.add(regionID);
		validateAllChangesSeen();
	}
	
	/**
	 * Whether the changed {@link Region} with the given number has been seen by the user.
	 *
	 * @see #markChangedRegionAsEvaluated(int)
	 */
	public boolean isEvaluatedChangedRegion(int regionID) {
		return seenChangedRegions.contains(regionID);
	}
	
	/**
	 * The number of the changed {@link Region} currently shown, -1 if none.
	 */
	public int getCurrentChangedRegion() {
		return (currentChangedRegion);
	}

	/**
	 * Setter for {@link #getCurrentChangedRegion()}.
	 */
	public void setCurrentChangedRegion(int currentChangedRegion) {
		this.currentChangedRegion = currentChangedRegion;
	}
	
	/**
	 * Checks whether all changes of this entry (all changed regions of a file, all children of a
	 * directory) have been seen and, if so, marks this entry as seen and propagates this to the
	 * parent.
	 */
	protected void validateAllChangesSeen() {
		if (getEntryResult().isDirectory() ){
			if (getNumOfChildren() == seenChildren.size()) {
				setAllChangesSeen(true);
				setAllChildrenSeen(true);
				// propagate to parent
				CompareStatistics parent2 = getParent();
				if(parent2!= null){
					parent2.allMyChangesAreSeen(this);
				}
			}
		} else { // entry is a file
			if (seenChangedRegions.size() == getNumOfChangedRegions()) {
				setAllChangesSeen(true);
				// setAllChildrenSeen(true);
				// propagate to parent
				CompareStatistics parent2 = getParent();
				if(parent2!= null){
					parent2.allMyChangesAreSeen(this);
				}
			}
		}
	}

	/**
	 * Whether all changes of the compared entry have been seen by the user.
	 */
	public boolean areAllChangesSeen() {
//		assert(!isEntryDirectory());
		return (allChangesSeen);
	}

	/**
	 * Setter for {@link #areAllChangesSeen()}.
	 */
	public void setAllChangesSeen(boolean allChangesSeen) {
		this.allChangesSeen = allChangesSeen;
	}

	/**
	 * Whether the compared entry is a directory.
	 */
	public boolean isEntryDirectory() {
		return (getEntryResult().isDirectory());
	}

	/**
	 * Whether all children of the compared directory have been seen by the user.
	 */
	public boolean areAllChildrenSeen() {
		assert(getEntryResult().isDirectory());
		return (allChildrenSeen);
	}
	
	/**
	 * the childs statistics object calls this method and identifies itself
	 * propagates the seen status ...
	 */
	public void allMyChangesAreSeen(CompareStatistics statistics) {
		seenChildren.add(statistics);
		validateAllChangesSeen();
	}

	/**
	 * Setter for {@link #areAllChildrenSeen()}.
	 */
	public void setAllChildrenSeen(boolean allChildrenSeen) {
		this.allChildrenSeen = allChildrenSeen;
	}

	/**
	 * The statistics of the parent directory, {@code null} for the root.
	 */
	public CompareStatistics getParent() {
		return (parent);
	}

	/**
	 * Setter for {@link #getParent()}.
	 */
	public void setParent(CompareStatistics parent) {
		this.parent = parent;
	}

	/**
	 * The number of children of the compared entry.
	 */
	public int getNumOfChildren() {
		return (entryResult.getChildren().size());
	}


//	public int[] getSeenChangedRegionsArray() {
//		int result[] = new int[seenChangedRegions.size()];
//		Iterator<Integer> it = seenChangedRegions.iterator();
//		int i = 0;
//		while (it.hasNext()) {
//			result[i] = it.next().intValue();
//			i++;
//		}
//		return (result);
//	}

	/**
	 * The numbers of the changed {@link Region}s that have been seen by the user.
	 */
	public Set<Integer> getSeenChangedRegions() {
		return (seenChangedRegions);
	}

	/**
	 * Whether the change counts have been computed by {@link #getFileDiff()}.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * The statistics of all children whose changes have been seen completely.
	 */
	public Set<CompareStatistics> getSeenChildren() {
		return (seenChildren);
	}

	/**
	 * Makes the given changed {@link Region} the current one and marks it as seen.
	 *
	 * @see #setCurrentChangedRegion(int)
	 * @see #markChangedRegionAsEvaluated(int)
	 */
	public void setAndMarkCurrentChangedRegion(int numOfRegion) {
		setCurrentChangedRegion(numOfRegion);
		markChangedRegionAsEvaluated(numOfRegion);
	}
	
	/**
	 * Setter for {@link #getSeenChangedRegions()}.
	 */
	public void setSeenChangedRegions(Set<Integer> seenChangedRegions) {
		this.seenChangedRegions = seenChangedRegions;
	}

}
