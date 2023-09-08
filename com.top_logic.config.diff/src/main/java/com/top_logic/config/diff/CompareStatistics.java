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
	
	public String getHTML(String aSource, String aDest) {		
		return FileDiffGenerator.INSTANCE.generateHtmlToString(getFileDiff(), aSource, aDest, this);
	}

	public void setEntryResult(EntryResult entryResult) {
		this.entryResult = entryResult;
	}

	public boolean hasChanges() {
		boolean result = hasChanges;
		return result;
	}
	
	private void setChanged(boolean changed) {
		hasChanges = changed;
	}

	public EntryResult getEntryResult() {
		return (entryResult);
	}

	public String getName() {
		return (getEntryResult().getPlainName());
	}

	public int getNumOfRegions() {
		return (numOfRegions);
	}

	private void setNumOfRegions(int numOfRegions) {
		this.numOfRegions = numOfRegions;
	}

	public int getNumOfUnchangedRegions() {
		return (numOfUnchangedRegions);
	}

	public int getNumOfSourceLines() {
		return (numOfSourceLines);
	}

	public int getNumOfDestLines() {
		return (numOfDestLines);
	}

	public int getNumOfChangedSourceLines() {
		return (numOfChangedSourceLines);
	}

	public State getState() {
		return (entryResult.getState());
	}

	public String getStateAsString() {
		return (entryResult.getStateAsString());
	}

	public int getNumOfUnchangedSourceLines() {
		return (numOfUnchangedSourceLines);
	}

	public int getNumOfChangedDestLines() {
		return (numOfChangedDestLines);
	}

	public int getNumOfUnchangedDestLines() {
		return (numOfUnchangedDestLines);
	}

	public int getNumOfChangedRegions() {
		return (numOfChangedRegions);
	}

	public void setNumOfChangedRegions(int numOfChangedRegions) {
		this.numOfChangedRegions = numOfChangedRegions;
	}

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
	
	public boolean isEvaluatedChangedRegion(int regionID) {
		return seenChangedRegions.contains(regionID);
	}
	
	public int getCurrentChangedRegion() {
		return (currentChangedRegion);
	}

	public void setCurrentChangedRegion(int currentChangedRegion) {
		this.currentChangedRegion = currentChangedRegion;
	}
	
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

	public boolean areAllChangesSeen() {
//		assert(!isEntryDirectory());
		return (allChangesSeen);
	}

	public void setAllChangesSeen(boolean allChangesSeen) {
		this.allChangesSeen = allChangesSeen;
	}

	public boolean isEntryDirectory() {
		return (getEntryResult().isDirectory());
	}

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

	public void setAllChildrenSeen(boolean allChildrenSeen) {
		this.allChildrenSeen = allChildrenSeen;
	}

	public CompareStatistics getParent() {
		return (parent);
	}

	public void setParent(CompareStatistics parent) {
		this.parent = parent;
	}

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

	public Set<Integer> getSeenChangedRegions() {
		return (seenChangedRegions);
	}

	public boolean isInitialized() {
		return initialized;
	}

	public Set<CompareStatistics> getSeenChildren() {
		return (seenChildren);
	}

	public void setAndMarkCurrentChangedRegion(int numOfRegion) {
		setCurrentChangedRegion(numOfRegion);
		markChangedRegionAsEvaluated(numOfRegion);
	}
	
	public void setSeenChangedRegions(Set<Integer> seenChangedRegions) {
		this.seenChangedRegions = seenChangedRegions;
	}

}
