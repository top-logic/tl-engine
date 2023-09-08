/* 
 * JavaScript functions used for compressed trees.
 * 
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 *
 * Author    Bernhard Haumacher
 */
var references;
var prefix;

var nodeURL;
var expandURL;
var collapseURL;

function init() {
	references = new Array();
	prefix = new Array();
}

function setup(aNodeURL, aExpandURL, aCollapseURL) {
	nodeURL = aNodeURL;
	expandURL = aExpandURL;
	collapseURL = aCollapseURL;
}

function d(string) {
	references[references.length] = string;
	return string;
}

function r(index) {
	return references[index];
}

function getExpandLink(isExpanded, id) {
	return isExpanded ? (collapseURL + id) : (expandURL + id);
}

function getNodeURL(id) {
	return nodeURL + id;
}

function write(string) {
	document.write(string);
}

function node(plain, id, position, selections, prefixImage, expandImage, nodeImage, type, name) {
	prefix.length = plain.length - 1;
	if (prefixImage) {
		prefix[prefix.length - 1] = prefixImage;
	}
	for (n = 0; n < prefix.length; n++) {
		write('<img class="m" src="' + prefix[n] + '"/>');
	}
	isLeaf = plain.charAt(plain.length - 1) == '-';
	
	if (! isLeaf) {
		isExpanded = plain.charAt(plain.length - 1) == '+';
		write('<a class="t" href="' + getExpandLink(isExpanded, id) + '"/>');
	}
	write('<img class="m" src="' + expandImage + '"/>');
	if (! isLeaf) {
		write('</a>');
	}
	write('<img class="m" src="' + nodeImage + '" alt="' + type + '"/>');
	write('&nbsp;');
	write('<a class="s' + (selections & 0x03) + '" href="' + getNodeURL(id) + '">');
	write(name);
	write('</a>');
	
	write('<br/>');
}
