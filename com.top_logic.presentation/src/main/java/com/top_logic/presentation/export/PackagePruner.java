/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;

/**
 * Removes the parts of an Office package that are no longer used.
 *
 * <p>
 * A part is used, if it can be reached from the package root by following internal
 * relationships. Removing e.g. a slide from a presentation leaves the parts only the slide refers
 * to (its images, notes, charts and embedded objects) in the package; they are removed here,
 * together with their relationships.
 * </p>
 */
public final class PackagePruner {

	private PackagePruner() {
		// Static utility.
	}

	/**
	 * Removes all parts of the given package that cannot be reached from the package root.
	 *
	 * @param pkg
	 *        The package to clean up, opened for writing.
	 * @return The names of the removed parts, without relationship parts.
	 */
	public static List<PackagePartName> prune(OPCPackage pkg) throws InvalidFormatException {
		Set<PackagePartName> reachable = reachableParts(pkg);

		List<PackagePart> unusedParts = new ArrayList<>();
		List<PackagePart> unusedRelations = new ArrayList<>();
		for (PackagePart part : pkg.getParts()) {
			PackagePartName name = part.getPartName();
			if (part.isRelationshipPart()) {
				URI source = PackagingURIHelper.getSourcePartUriFromRelationshipPartUri(name.getURI());
				if (!source.equals(PackagingURIHelper.PACKAGE_ROOT_URI)
					&& !reachable.contains(PackagingURIHelper.createPartName(source))) {
					unusedRelations.add(part);
				}
			} else if (!reachable.contains(name)) {
				unusedParts.add(part);
			}
		}

		for (PackagePart relations : unusedRelations) {
			pkg.removePart(relations.getPartName());
		}
		List<PackagePartName> result = new ArrayList<>(unusedParts.size());
		for (PackagePart part : unusedParts) {
			result.add(part.getPartName());
			pkg.removePart(part.getPartName());
		}
		return result;
	}

	/**
	 * Removes the relationship parts whose source part no longer exists.
	 *
	 * <p>
	 * Removing a part from a package leaves its relationship part behind. A part created later
	 * under the same name would take over these relationships, e.g. a chart created after removing
	 * a slide with a chart would refer to the embedded workbook of the removed chart.
	 * </p>
	 *
	 * @param pkg
	 *        The package to clean up, opened for writing.
	 */
	public static void removeDetachedRelationships(OPCPackage pkg) throws InvalidFormatException {
		List<PackagePart> detached = new ArrayList<>();
		for (PackagePart part : pkg.getParts()) {
			if (!part.isRelationshipPart()) {
				continue;
			}
			URI source = PackagingURIHelper.getSourcePartUriFromRelationshipPartUri(part.getPartName().getURI());
			if (!source.equals(PackagingURIHelper.PACKAGE_ROOT_URI)
				&& !pkg.containPart(PackagingURIHelper.createPartName(source))) {
				detached.add(part);
			}
		}
		for (PackagePart relations : detached) {
			pkg.removePart(relations.getPartName());
		}
	}

	/**
	 * The names of all parts that can be reached from the package root.
	 */
	public static Set<PackagePartName> reachableParts(OPCPackage pkg) throws InvalidFormatException {
		Set<PackagePartName> result = new HashSet<>();
		Deque<PackagePart> pending = new ArrayDeque<>();
		visit(pkg, pkg.getRelationships(), null, result, pending);
		while (!pending.isEmpty()) {
			PackagePart part = pending.pop();
			visit(pkg, part.getRelationships(), part, result, pending);
		}
		return result;
	}

	private static void visit(OPCPackage pkg, PackageRelationshipCollection relations, PackagePart source,
			Set<PackagePartName> reachable, Deque<PackagePart> pending) throws InvalidFormatException {
		for (PackageRelationship relation : relations) {
			if (relation.getTargetMode() == TargetMode.EXTERNAL) {
				continue;
			}
			PackagePart target = source == null ? pkg.getPart(relation) : related(source, relation);
			if (target != null && reachable.add(target.getPartName())) {
				pending.push(target);
			}
		}
	}

	private static PackagePart related(PackagePart source, PackageRelationship relation) {
		try {
			return source.getRelatedPart(relation);
		} catch (InvalidFormatException | IllegalArgumentException ex) {
			// A relationship to a missing part refers to nothing to keep.
			return null;
		}
	}

}
