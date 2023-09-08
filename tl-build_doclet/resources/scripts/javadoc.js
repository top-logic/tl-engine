var INDEX_PATH = "package-list.xml";

var LIST_ELEMENTS;
var MIXED_ELEMENTS;
var INLINE_ELEMENTS;

var resources;
var packageExpanded;

function init() {
	// Delay to prevent early errors in unsupported browsers.
	
	LIST_ELEMENTS = new Map();
	LIST_ELEMENTS.set("implements", true);
	LIST_ELEMENTS.set("contents", true);
	LIST_ELEMENTS.set("fields", true);
	LIST_ELEMENTS.set("constructors", true);
	LIST_ELEMENTS.set("methods", true);
	LIST_ELEMENTS.set("params", true);
	LIST_ELEMENTS.set("annotations", true);
	LIST_ELEMENTS.set("args", true);
	LIST_ELEMENTS.set("bounds", true);
	LIST_ELEMENTS.set("extendsBounds", true);
	LIST_ELEMENTS.set("superBounds", true);
	LIST_ELEMENTS.set("values", true);
	LIST_ELEMENTS.set("sees", true);
	LIST_ELEMENTS.set("packages", true);
	LIST_ELEMENTS.set("subtypes", true);
	LIST_ELEMENTS.set("classes", true);
	LIST_ELEMENTS.set("interfaces", true);
	LIST_ELEMENTS.set("enums", true);
	LIST_ELEMENTS.set("configs", true);
	LIST_ELEMENTS.set("instances", true);
	LIST_ELEMENTS.set("exceptions", true);
	LIST_ELEMENTS.set("errors", true);
	
	MIXED_ELEMENTS = new Map();
	MIXED_ELEMENTS.set("title", true);
	MIXED_ELEMENTS.set("doc", true);
	
	INLINE_ELEMENTS = new Map();
	
	resources = new Map();
	packageExpanded = new Map();
}

function showPage(type, member) {
	var xref = getXrefPath(type);
	var path = getPath(type);
	
	var pkgName = packageName(type);
	var pkgPath = packageInfoPath(pkgName);
	
	new Loader().load("settings.json", "json").load(xref, "json").load(pkgPath).load(path).onLoad(function(map) {
		var self = map.get(path);
		if (self == null) {
			searchType(type, member);
			return;
		}
		self.settings = map.get("settings.json");
		self.xref = map.get(xref);
		self.pkgInfo = map.get(pkgPath);
		
		if (self.kind == "config") {
			loadImplements([self], function() {
				computeAllMethods(self);
				displayData(self, member);
			});
		} else {
			displayData(self, member);
		}
	});
}

function searchType(type, member) {
	if (type.indexOf(".") >= 0) {
		// Maybe an inner class with simple name is searched.
		type = type.replace(/\./g, "$");
	}
	loadIndex(function(data) {
		// Search single type.
		var searchNames = data.searchNames;
		var names = data.names;
		var term = toSearchTerm(type.toLowerCase());
		var start = searchStart(searchNames, term);
		
		while (true) {
			if (start < 0 || start >= searchNames.length || searchNames[start] != term) {
				showIndex();
				return;
			}
			
			if (names[start] == type) {
				// Exact match.
				break;
			}
			
			start++;
		}
		
		var list = data.types;
		var n = searchStart(list, start, getName);
		if (n < 0 || n >= list.length || list[n][0] != start) {
			showIndex();
			return;
		}
		
		var typeInfo = list[n];
		var typeName = names[typeInfo[0]];
		var pkgInfo = typeInfo[1];
		var pkgName = expandPackageName(data, pkgInfo);
		
		showPage(pkgName + "." + typeName, member);
	});
}

/**
 * Resolves monomorphic "pointers" in encoded JSON index and expand delta-encoded name indices.
 */
function expandIndex(data) {
	if (data.expanded) {
		return;
	}
	data.expanded = true;
	
	var compressed = data.compressed;
	
	var names = data.names;
	var searchNames = [];
	var lastName = "";
	for (var n = 0, cnt = names.length; n < cnt; n++) {
		var name = names[n];
		if (compressed) {
			var prefixLength = name.charCodeAt(0) - 48;
			
			var prefix;
			if (prefixLength > 44) {
				prefixLength -= 44;
				if (prefixLength > lastName.length) {
					throw new Error("Invalid index.");
				}
				
				var first = lastName.substring(0, 1);
				if (first.toLowerCase() == first) {
					first = first.toUpperCase();
				} else {
					first = first.toLowerCase();
				}
				prefix = first + lastName.substring(1, prefixLength);
			} else {
				if (prefixLength > lastName.length) {
					throw new Error("Invalid index.");
				}
				prefix = lastName.substring(0, prefixLength);
			}
			
			name = prefix + name.substring(1);
			names[n] = name;
			lastName = name;
		}
		searchNames.push(toSearchName(name));
	}
	data.searchNames = searchNames;
	
	data.packageId = 0;
	
	var packages = data.packages;
	{
		var nameId = 0;
		var outerId = 0;
		for (var n = 0, cnt = packages.length; n < cnt; n++) {
			var pkg = packages[n];
			if (compressed) {
				nameId += pkg[0];
				if (nameId < 0) {
					throw new Error("Invalid index.");
				}
				pkg[0] = nameId;
			}
			var parent = pkg[1];
			if (parent != undefined) {
				if (compressed) {
					outerId += pkg[1];
					if (outerId < 0) {
						throw new Error("Invalid index.");
					}
					pkg[1] = outerId;
				}
				pkg[1] = packages[pkg[1]];
			}
		}
	}

	data.typeId = data.packageId + packages.length;
	
	var types = data.types;
	{
		var nameId = 0, superId = 0;
		for (var n = 0, cnt = types.length; n < cnt; n++) {
			var type = types[n];
			if (compressed) {
				nameId += type[0];
				if (nameId < 0) {
					throw new Error("Invalid index.");
				}
				type[0] = nameId;
				
				superId += type[1];
				if (superId < 0) {
					throw new Error("Invalid index.");
				}
				type[1] = superId;
			}
			type[1] = packages[type[1]];
		}
	}
	
	data.methodId = data.typeId + types.length;
	expandMemberTable(data, data.methods);
	
	data.fieldId = data.methodId + data.methods.length;
	expandMemberTable(data, data.fields);
	
	if (compressed) {
		expandIndexTable(data.praefixes);
		expandIndexTable(data.suffixes);
	}
}

function expandMemberTable(data, methods) {
	var compressed = data.compressed;
	if (compressed) {
		var last = null;
		for (var n = 0, cnt = methods.length; n < cnt; n++) {
			var method = methods[n];
			if (n > 0) {
				for (var i = 0, length = Math.min(method.length, last.length); i < length; i++) {
					method[i] += last[i];
					if (method[i] < 0) {
						throw new Error("Invalid index.");
					}
				}
			}
			last = method;
		}
	}

	var types = data.types;
	for (var n = 0, cnt = methods.length; n < cnt; n++) {
		var method = methods[n];
		for (var i = 1; i < method.length; i++) {
			method[i] = types[method[i]];
		}
	}
}


function expandIndexTable(index) {
	var nameId = 0, refId = 0;
	for (var n = 0, cnt = index.length; n < cnt; n++) {
		var entry = index[n];
		nameId += entry[0];
		if (nameId < 0) {
			throw new Error("Invalid index.");
		}
		entry[0] = nameId;
		
		var lastRef = refId;
		for (var i = 1, length = entry.length; i < length; i++) {
			lastRef += entry[i];
			if (lastRef < 0) {
				throw new Error("Invalid index.");
			}
			entry[i] = lastRef;
		}
		refId = entry[1];
	}
}


function toSearchName(name) {
	return name.toLowerCase().replace(/[-_\$\.]/g, "");
}

function computeAllMethods(type) {
	if (type.kind != "config") {
		return [];
	}
	if (type.allMethods == undefined) {
		type.allMethods = [];
		type.allMethods = type.allMethods.concat(nonOverrides(type.methods));
		
		if (type.implementsRef != undefined) {
			for (var i = 0; i < type.implementsRef.length; i++) {
				var ref = type.implementsRef[i];
				type.allMethods = type.allMethods.concat(computeAllMethods(ref));
			}
		}
		
		type.allMethods = sortConfigProperties(type.allMethods);
	}
	
	return type.allMethods;
}

function sortConfigProperties(list) {
	list.sort(function(m1, m2) { 
		var name1 = configName(m1);
		var name2 = configName(m2);
		return name1 < name2 ? -1 : (name1 > name2 ? 1 : 0); 
	});
	
	return list;
}

function loadImplements(types, cont) {
	var loader = new Loader();
	for (var i = 0; i < types.length; i++) {
		var type = types[i];
		
		var impls = type["implements"];
		if (impls != undefined) {
			for (var n = 0, cnt = impls.length; n < cnt; n++) {
				loader.load(getPath(impls[n].id));
			}
		}
	}
	loader.onLoad(function(map) {
		var next = [];
		
		for (var i = 0; i < types.length; i++) {
			var type = types[i];
			
			var impls = type["implements"];
			if (impls != undefined) {
				type.implementsRef = [];
				for (var n = 0, cnt = impls.length; n < cnt; n++) {
					var implRef = map.get(getPath(impls[n].id));
					type.implementsRef.push(implRef);
					next.push(implRef);
				}
			}
		}
		
		if (next.length == 0) {
			cont(self);
		} else {
			loadImplements(next, cont);
		}
	});
}

function showPackage(pkg) {
	var pkgPath = packageInfoPath(pkg);
	new Loader().load(INDEX_PATH).load(pkgPath).onLoad(function(map) {
		var index = map.get(INDEX_PATH);
		var data = map.get(pkgPath);
		if (data == null) {
			showIndex();
			return;
		}
		data.index = index;
		
		displayPackage(data);
	});
}

function packageInfoPath(pkg) {
	return basePath(pkg) + "/package-info.xml";
}

function Loader() {
	this.requests = 0;
	this.responses = 0;
}
Loader.prototype.load = function(resource, dataType) {
	this.requests++;
	
	var cached = resources.get(resource);
	if (cached != undefined) {
		this.responses++;
		this.checkLoaded();
		return this;
	}
	
	var self = this;
	var jqxhr = $.get(resource, null, function(file) {
		var data;
		if (dataType == "json") {
			data = file;
		} else {
			data = toJSON(file.documentElement);
		}
		resources.set(resource, data);
		self.responses++;
		self.checkLoaded();
	}, dataType);
	
	jqxhr.fail(function() {
		self.responses++;
		self.checkLoaded();
	});
	
	return this;
}
Loader.prototype.onLoad = function(onload) {
	this.onload = onload;
	this.checkLoaded();
}
Loader.prototype.checkLoaded = function() {
	if (this.onload != undefined && this.requests == this.responses) {
		this.onload(resources);
	}
}

function showIndex() {
	new Loader().load(INDEX_PATH).onLoad(function(map) {
		displayIndex(map.get(INDEX_PATH));
	});
}

function displayUnsupported() {
	var content = "<div class='message'>Your browser is not supported. Please try a current version of FireFox, Chrome, or Edge.</div>";
	document.getElementById("javadoc-content").innerHTML = content;
}

function displayData(self, member) {
	if (member != undefined && member.endsWith(")")) {
		var method = findMethod(self, member);
		if (method != null) {
			var overrides = method.overrides;
			if (overrides != undefined) {
				// Jump to original member.
				showPage(overrides, member);
				return;
			}
		}
	}

	renderTemplate('class-template.html', self);
	
	if (member != undefined) {
		var targetElement = document.getElementById(member);
		if (targetElement != null) {
			targetElement.scrollIntoView(); 
			targetElement.setAttribute("class", "highlight");
		}
	} else {
		window.scrollTo({ top: 0, left: 0 });
	}
	
	var name;
	var type = self.name;
	var sep = type.lastIndexOf(".");
	if (sep >= 0) {
		var mediumTypeName = type.substring(sep + 1).replace(/\$/g, ".");
		var packageName = type.substring(0, sep);
		name = mediumTypeName + " (" + packageName + ")"
	} else {
		name = type;
	}
	document.title = name;
	
	enableSearch();
}

function renderTemplate(template, self) {
	var out = nunjucks.render(template, self);
	document.getElementById("javadoc-content").innerHTML = out;
}

function findMethod(type, id) {
	var methods = type.methods;
	if (methods == undefined) {
		return null;
	}
	for (var n = 0, cnt = methods.length; n < cnt; n++) {
		var method = methods[n];
		if (method.id == id) {
			return method;
		}
	}
	return null;
}

function displayPackage(data) {
	renderTemplate('package-template.html', data);
	
	document.title = data.name;

	enableSearch();
}

function displayIndex(data) {
	renderTemplate('index-template.html', data);

	document.title = "TopLogic Documentation";
	enableSearch();
}

function enableSearch() {
	var searchInput = document.getElementById("seach-input");
	if (searchInput != null) {
		searchInput.addEventListener('focus', openSearch);
		searchInput.addEventListener('blur', closeSearch);
		searchInput.addEventListener('keyup', onSearchInput);
		searchInput.addEventListener('keydown', onSearchNavigation);
	}
}

function onSearchNavigation(evt) {
	var searchResults = document.getElementById("search-results");
	var searchCursor = document.getElementById("search-cursor");
	if (evt.keyCode == 13) {
		// enter
		if (searchCursor != null) {
			navigateTo(searchCursor);
			closeSearch();
		}
		evt.preventDefault();
		return;
	}
	else if (evt.keyCode == 38) {
		// up arrow
		if (searchCursor != null) {
			searchCursor.removeAttribute("id");
			searchCursor = searchCursor.previousSibling;
		}
		if (searchCursor == null) {
			searchCursor = searchResults.lastChild;
		}
		while (searchCursor != null && searchCursor.nodeType != Node.ELEMENT_NODE) {
			searchCursor = searchResults.previousSibling;
		}
		if (searchCursor != null) {
			searchCursor.setAttribute("id", "search-cursor");
		}
		evt.preventDefault();
		return;
    }
    else if (evt.keyCode == 40) {
        // down arrow
		if (searchCursor != null) {
			searchCursor.removeAttribute("id");
			searchCursor = searchCursor.nextSibling;
		}
		if (searchCursor == null) {
			searchCursor = searchResults.firstChild;
		}
		while (searchCursor != null && searchCursor.nodeType != Node.ELEMENT_NODE) {
			searchCursor = searchResults.nextSibling;
		}
		if (searchCursor != null) {
			searchCursor.setAttribute("id", "search-cursor");
		}
		evt.preventDefault();
		return;
    }	
}

function Search(data) {
	this.data = data;
	this.searchText = "";
	this.queries = new Map();
	this.result = new Set();
}
Search.prototype.update = function(searchText) {
	if (this.searchText == searchText) {
		return false;
	}
	this.searchText = searchText;
	
	var terms = searchText.split(/\s+/);
	
	var queries = new Map();
	for (var n = 0, cnt = terms.length; n < cnt; n++) {
		if (this.queries.has(terms[n])) {
			queries.set(terms[n], this.queries.get(terms[n]));
		} else {
			queries.set(terms[n], new Query(this.data, terms[n]));
		}
	}
	this.queries = queries;
	
	var overflow = false;
	var result = null;
	for (var n = 0, cnt = terms.length; n < cnt; n++) {
		var query = queries.get(terms[n]);
		if (query.overflow) {
			overflow = true;
			continue;
		}
		
		if (result == null) {
			result = query.result;
			continue;
		}
		
		result = intersection(result, query.result);
	}
	
	this.overflow = overflow && result == null;
	this.result = result == null ? new Set() : result;
	return true;
}

function intersection(setA, setB) {
    let _intersection = new Set()
    for (let elem of setB) {
        if (setA.has(elem)) {
            _intersection.add(elem)
        }
    }
    return _intersection
}

function getName(list, index) {
	return list[index][0];
}

function Query(data, term) {
	var matchPraefix = term.startsWith('"');
	var matchSuffix = term.endsWith('"');
	
	term = toSearchTerm(term);
	
	var items = new Set();
	this.result = items;
	this.overflow = false;
	
	if (term.length == 0) {
		this.overflow = true;
		return;
	}

	var names = data.searchNames;
	var start = searchStart(names, term);
	var stop = start;
	while (stop < names.length) {
		if (matchSuffix) {
			if (names[stop] == term) {
				stop++;
			} else {
				break;
			}
		} else if (names[stop].startsWith(term)) {
			stop++;
		} else {
			break;
		}
	}
	
	var nothingFound = start == stop;
	if (nothingFound) {
		return;
	}
	
	if (stop - start > 500) {
		this.overflow = true;
		return;
	}

	{
		// Search types.
		var list = data.types;
		var idStart = data.typeId;
		var n = searchStart(list, start, getName);
		while (n < list.length && getName(list, n) < stop) {
			items.add(idStart + n);
			n++;
		}
	}
	
	{
		// Search methods.
		var list = data.methods;
		var idStart = data.methodId;
		var n = searchStart(list, start, getName);
		while (n < list.length && getName(list, n) < stop) {
			items.add(idStart + n);
			n++;
		}
	}
	
	{
		// Search fields.
		var list = data.fields;
		var idStart = data.fieldId;
		var n = searchStart(list, start, getName);
		while (n < list.length && getName(list, n) < stop) {
			items.add(idStart + n);
			n++;
		}
	}
	
	// Search indexed items.
	this.addIndexEntries(items, getName, data.praefixes, start, stop);
	
	if (!matchPraefix) {
		this.addIndexEntries(items, getName, data.suffixes, start, stop);
	}
}
Query.prototype.addIndexEntries = function(items, getName, list, start, stop) {
	var n = searchStart(list, start, getName);
	while (n < list.length && getName(list, n) < stop) {
		var refs = list[n];
		for (var i = 1; i < refs.length; i++) {
			items.add(refs[i]);
		}
		
		n++;
	}
}

function toSearchTerm(term) {
	return term.replace(/[^A-Za-z0-9]+/g, "");
}

function onSearchInput(evt) {
	var display = document.getElementById("search-display");
	var data = display.data;

	var searchResults = document.getElementById("search-results");
	var searchInput = document.getElementById("seach-input");
	var searchText = toSearchName(searchInput.value.trim());

	var searchInfo = document.getElementById("search-info");
	if (searchText.length == 0) {
		searchResults.innerHTML = "";
		searchInfo.innerHTML = "";
		return;
	}

	var search = searchResults.dataLastSearch;
	if (search == null) {
		search = new Search(data);
		searchResults.dataLastSearch = search;
	}
	
	if (!search.update(searchText)) {
		// Cursor movement?
		return;
	}
	
	if (search.overflow) {
		searchInfo.innerHTML = "Provide more info!";
		searchResults.innerHTML = "";
		return;
	}
	
	if (search.result.size == 0) {
		searchInfo.innerHTML = "Nothing found!";
		searchResults.innerHTML = "";
		return;
	}
	
	searchInfo.innerHTML = "";

	var items = Array.from(search.result);
	items.sort(function(a, b) {return a-b});
	
	var content = "";
	for (var n = 0, cnt = Math.min(500, items.length); n < cnt; n++) {
		var item = items[n];
		if (item < data.typeId) {
			// A package.
			var pkgItem = data.packages[item];
			var pkgName = expandPackageName(data, pkgItem);
			content += "<div data-package='" + pkgName + "' class='search-result'>" + pkgName + "</div>";
		}
		else if (item < data.methodId) {
			// A type.
			var typeItem = data.types[item - data.typeId];
			var packageItem = typeItem[1];
			var packageName = expandPackageName(data, packageItem);
			var typeName = 	data.names[typeItem[0]];
			var qTypeName = packageName + "." + typeName;

			content += 
				"<div data-type='" + qTypeName + "' class='search-result'>" + 
					"<span class='search-hit'>" + expandFullTypeName(data, typeItem) + "</span>" + " (" + packageName + ")" +
				"</div>";
		}
		else if (item < data.fieldId) {
			// A method.
			var methodItem = data.methods[item - data.methodId];
			var typeItem = methodItem[1];
			var packageItem = typeItem[1];
			var packageName = expandPackageName(data, packageItem);
			var typeName = 	data.names[typeItem[0]];
			var qTypeName = packageName + "." + typeName;
			var qMethodName = data.names[methodItem[0]] + "(";
			for (var p = 2; p < methodItem.length; p++) {
				if (p > 2) {
					qMethodName += ",";
				}
				var paramType = methodItem[p];
				var paramPkg = expandPackageName(data, paramType[1]);
				if (paramPkg.length > 0) {
					qMethodName += paramPkg;
					qMethodName += ".";
				}
				qMethodName += data.names[paramType[0]];
			}
			qMethodName += ")";
			
			var methodName = expandMethodName(data, methodItem);
			content += "<div data-type='" + qTypeName + "' data-member='" + qMethodName + "' class='search-result'>" + methodName + "</div>";
		}
		else {
			// A field.
			var fieldItem = data.fields[item - data.fieldId];
			var typeItem = fieldItem[1];
			var packageItem = typeItem[1];
			var packageName = expandPackageName(data, packageItem);
			var typeName = 	data.names[typeItem[0]];
			var qTypeName = packageName + "." + typeName;
			var qFieldName = data.names[fieldItem[0]];

			var fieldName = expandFieldName(data, fieldItem);
			content += "<div data-type='" + qTypeName + "' data-member='" + qFieldName + "' class='search-result'>" + fieldName + "</div>";
		}
	}
	
	searchResults.innerHTML = content;
}

function expandPackageName(data, item) {
	var name = data.names[item[0]];
	if (item[1] != undefined) {
		return expandPackageName(data, item[1]) + "." + name;
	} else {
		return name;
	}
}

function expandFullTypeName(data, item) {
	var name = data.names[item[0]];
	return name.replace(/\$/g, ".");
}

function expandLocalTypeName(data, item) {
	var name = data.names[item[0]];
	return typeName(name);
}

function expandMethodName(data, item) {
	var name = data.names[item[0]];
	var result = expandFullTypeName(data, item[1]) + "." + "<span class='search-hit'>" + name + "</span>" + "(";
	for (var n = 2, cnt = item.length; n < cnt; n++) {
		result += expandLocalTypeName(data, item[n]);
		if (n < cnt - 1) {
			result += ", ";
		}
	}
	result += ")";
	return result;
}

function expandFieldName(data, item) {
	var name = data.names[item[0]];
	return expandFullTypeName(data, item[1]) + "." + "<span class='search-hit'>" + name + "</span>";
}

/**
 * Performs a binary search for the index in the given list that contains the smallest value greater or equal to the given value.
 * 
 * @param list The list to search in.
 * @param value The value to compare with.
 * @param getter An access function taking the list and an index.
 * @returns Index in the list containing the smallest element not smaller than the given value.
 */
function searchStart(list, value, getter) {
	if (getter == undefined) {
		getter = function(list, index) {
			return list[index];
		}
	}
	var start = 0;
	var stop = list.length;
	while (start + 1 < stop) {
		var mid = Math.floor((start + stop) / 2);
		var midItem = getter(list, mid);
		if (value < midItem) {
			stop = mid;
		}
		else if (value > midItem) {
			start = mid;
		}
		else {
			// Note: There could be more than one equal item and we must find the first one.
			while (mid > 0 && getter(list, mid - 1) == value) {
				mid--;
			}
			return mid;
		}
	}
	return stop;
}

function openSearch(evt) {
	var display = document.createElement("div");
	display.setAttribute("id", "search-display");
	document.getElementById("search").appendChild(display);
	display.innerHTML= "<div id='search-loader'>Loading index...</div><div id='search-info'></div><div id='search-results'></div>";

	loadIndex(function(data) {
		display.data = data;
		removeElement("search-loader");
	});
}

function loadIndex(onLoad) {
	new Loader().load("index.json", "json").onLoad(function(map) {
		var data = map.get("index.json");
		expandIndex(data);
		onLoad(data);
	});
}

function closeSearch(evt) {
	removeElement("search-display");
}

function removeElement(id) {
	var display = document.getElementById(id);
	if (display != null) {
		display.parentNode.removeChild(display);
	}
}

function toJSON(element) {
	var result = {};
	
	if (element.hasAttributes()) {
		var attributes = element.attributes;
		for (var n = 0, cnt = attributes.length; n < cnt; n++) {
			var attr = attributes.item(n);
			
			result[attr.name] = attr.value;
		}
	}
	
	for (var child = element.firstChild; child != null; child = child.nextSibling) {
		if (child.nodeType == Node.ELEMENT_NODE) {
			var name = child.localName;
			
			if (LIST_ELEMENTS.has(name)) {
				value = toJSONList(child);
			} else {
				value = toJSONValue(child);
			}
			
			result[name] = value;
		}
	}
	
	return result;
}

function toJSONValue(element) {
	var name = element.localName;

	var attrName = INLINE_ELEMENTS.get(name);
	if (attrName != undefined) {
		return element.getAttribute(attrName);
	} else {
		if (MIXED_ELEMENTS.has(name)) {
			return element;
		} else {
			return toJSON(element);
		}
	}
}

function toJSONList(element) {
	var result = [];
	for (var child = element.firstChild; child != null; child = child.nextSibling) {
		if (child.nodeType == Node.ELEMENT_NODE) {
			result.push(toJSONValue(child));
		}
	}
	return result;
}

function getPath(type) {
	return basePath(type) + ".xml";
}

function getXrefPath(type) {
	return basePath(type) + "-xref.json";
}

function basePath(type) {
	return type.replace(/\./g, "/");
}

function Buffer() {
	this.codeOutput = false;
	this.result = "";
}
Buffer.prototype.append = function(value) {
	this.result += value;
	this.codeOutput = false;
}

function docOutput(element, current) {
	if (element == undefined) {
		return ""
	}
	return nunjucks.runtime.markSafe(docAppendContents(new Buffer(), element, current).result);
}

/**
 * Documentation without links for display in a tooltip.
 */
function docPlain(element) {
	if (element == undefined) {
		return ""
	}
	return nunjucks.runtime.markSafe(docAppendContentsPlain("", element));
}

function docAppendContents(result, element, current) {
	for (var child = element.firstChild; child != null; child = child.nextSibling) {
		docAppendNode(result, child, current);
	}
	return result;
}

function docAppendContentsPlain(result, element) {
	for (var child = element.firstChild; child != null; child = child.nextSibling) {
		result = docAppendNodePlain(result, child);
	}
	return result;
}

function docAppendNode(result, node, current) {
	if (node.nodeType == Node.ELEMENT_NODE) {
		var localName = node.localName;
		if (localName == "ref") {
			var targetClass = node.getAttribute("class");
			var targetMember = node.getAttribute("member");
			var targetPackage = node.getAttribute("package");
			
			if (node.firstChild == null || node.textContent.trim().length == 0) {
				var ref = {};
				ref["class"] = targetClass;
				ref["member"] = targetMember;
				ref["package"] = targetPackage;
				result.append(nunjucks.render("ref-template.html", {ref: ref, current: current}));
				result.codeOutput = true;
			} else {
				if (targetClass != null) {
					result.append(typeRefTag(asType(targetClass), targetMember, current, 
						function() { 
							return docAppendContents(new Buffer(), node, current).result; 
						}
					));
				} else {
					if (targetPackage != null) {
						if (isExternalPackage(targetPackage)) {
							result.append(
								"<span title='" + targetPackage + "'>" + 
								docAppendContents(new Buffer(), node, current).result + 
								"</span>");
						} else {
							result.append(
								"<span title='" + targetPackage + "'>" + 
								openRefTag(targetClass, targetMember, targetPackage) + 
								docAppendContents(new Buffer(), node, current).result + 
								closeRefTag() + 
								"</span>");
						}
					} else {
						result.append(
							"<span>" + 
							docAppendContents(new Buffer(), node, current).result + 
							"</span>");
					}
				}
			}
		} else {
			result.append("<");
			result.append(node.localName);
			
			if (node.hasAttributes()) {
				var attributes = node.attributes;
				for (var n = 0, cnt = attributes.length; n < cnt; n++) {
					var attr = attributes.item(n);
					
					result.append(" ");
					result.append(attr.name);
					result.append("='");
					result.append(attr.value);
					result.append("'");
				}
			}
			
			result.append(">");
			
			docAppendContents(result, node, current);
			
			result.append("</");
			result.append(node.localName);
			result.append(">");
		}
	} else if (node.nodeType == Node.TEXT_NODE || node.nodeType == Node.CDATA_SECTION_NODE) {
		var text = node.textContent;
		if (result.codeOutput) {
			var suffix = text.match(/^[A-Za-z]+/);
			if (suffix != null) {
				result.append("<code>" + escape(suffix[0]) + "</code>");
				text = text.substring(suffix[0].length);
			}
		}
		result.append(escape(text));
	}
	return result;
}

function escape(text) {
	return text.replace(/&/g, "&amp;").replace(/</g, "&lt;");
}

function docAppendNodePlain(result, node) {
	if (node.nodeType == Node.ELEMENT_NODE) {
		result = docAppendContentsPlain(result, node);
	} else if (node.nodeType == Node.TEXT_NODE || node.nodeType == Node.CDATA_SECTION_NODE) {
		result += escape(node.textContent);
	}
	return result;
}

function localTypeRef(id, pkg) {
	return typeRef(asType(pkg + "." + id), null);
}

function asType(typeName) {
	return {id: typeName};
}

function typeRef(type, current) {
	if (type == undefined) {
		return "";
	}
	try {
		return typeRefTag(type, null, current, function() {return label(type, current); });
	} catch (ex) {
		return nunjucks.runtime.markSafe("<span class='error'>[typeRef(" + type + ", " + current + "): " + ex + "]</span>");
	}
}

function label(type, current) {
	var result = rawLabel(type, current);
	
	if (type.args && type.args.length > 0) {
		result += "&lt;";
		for (var n = 0, cnt = type.args.length; n < cnt; n++) {
			if (n > 0) {
				result += ", ";
			}
			result += label(type.args[n]);
		}
		result += ">";
	}
	
	if (type.extendsBounds && type.extendsBounds.length > 0) {
		result += " extends ";
		for (var n = 0, cnt = type.extendsBounds.length; n < cnt; n++) {
			if (n > 0) {
				result += " & ";
			}
			result += label(type.extendsBounds[n]);
		}
	}
	
	if (type.superBounds && type.superBounds.length > 0) {
		result += " super ";
		for (var n = 0, cnt = type.superBounds.length; n < cnt; n++) {
			if (n > 0) {
				result += " | ";
			}
			result += label(type.superBounds[n]);
		}
	}
	
	if (type.dim) {
		for (var n = type.dim; n > 0; n--) {
			result += "[]";
		}
	}
	
	return result;
}

function rawLabel(type, current) {
	var id = type.id;
	
	if (isPrimitive(type) || isTypeVar(type) || isWildcard(type)) {
		return id;
	}
	
	if (current != undefined) {
		if (id.startsWith(current)) {
			// A local type within the current type.
			var suffix = id.substring(current.length);
			if (suffix.startsWith("$")) {
				return suffix.substring(1).replace(/\$/g, ".");
			}
		}
	}
	
	return mediumTypeName(id);
}

function isPrimitive(type) {
	return type.kind == "primitive";
}

function isTypeVar(type) {
	return type.kind == "typevar";
}

function isWildcard(type) {
	return type.kind == "wildcard";
}

function typeRefTag(type, member, current, content) {
	if (isPrimitive(type)) {
		return nunjucks.runtime.markSafe("<code class='primitive'>" + content() + "</code>");
	} else if (isExternal(type.id)) {
		return nunjucks.runtime.markSafe("<code class='external' title='" + qualifiedName(type.id) + (member != null ? "." + member : "") + "'>" + content() + "</code>");
	} else if (type.hidden) {
		return nunjucks.runtime.markSafe("<code class='hidden'>" + content() + "</code>");
	} else if (member == null && type.id == current) {
		return nunjucks.runtime.markSafe("<code class='self' title='This type.'>" + content() + "</code>");
	} else {
		return nunjucks.runtime.markSafe(openRefTag(type.id, member) + content() + closeRefTag());
	}
}

function startRef(obj, current) {
	var type = obj["class"];
	var member = obj["member"];
	var pkg = obj["package"];
	if (type == current && member == null && pkg == null) {
		return nunjucks.runtime.markSafe("<code>");
	} else {
		return nunjucks.runtime.markSafe(openRefTag(type, member, pkg));
	}
}

function endRef(obj, current) {
	var type = obj["class"];
	var member = obj["member"];
	var pkg = obj["package"];
	if (type == current && member == null && pkg == null) {
		return nunjucks.runtime.markSafe("</code>");
	} else {
		return nunjucks.runtime.markSafe(closeRefTag(type, member));
	}
}

function openRefTag(type, member, pkg) {
	var link = 
		type != null ? 
			type + (member != null ? "#" + member : "") : 
			(pkg != null ? packagePath(pkg) : null);
	return "<a" + 
		(type != null ? " data-type='" + type + "'" : "") + 
		(member != null ? " data-member='" + member + "'" : "") + 
		(pkg != null ? " data-package='" + pkg + "'" : "") + 
		(link != null ? " href='#" + link + "'" : "") + 
		" onclick='navigateTo(this)' title='" + shortTooltip(type, member, pkg) + "'" + 
		">";
}

function shortTooltip(type, member, pkg) {
	if (type != null) {
		if (member != null) {
			return typeName(type) + "." + member;
		} else {
			return qualifiedName(type);
		}
	} else {
		return pkg;
	}
}

function packageRef(name) {
	return nunjucks.runtime.markSafe("<a data-package='" + name + "' href='#" + packagePath(name) + "' onclick='navigateTo(this)'>" + name + "</a>");
}

function packagePath(name) {
	return name + ".package-info";
}

function enumValueRef(value, current) {
	return typeRefTag(value, value.member, current, function() {return value.member});
}

function typeNameRef(type, current) {
	return typeRefTag({id : type}, null, current, function() {return typeName(type) });
}

function closeRefTag() {
	return "</a>";
}

function navigateTo(element) {
	var pkg = element.getAttribute("data-package");
	if (pkg != null) {
		window.location.hash = packagePath(pkg);
		// showPackage(pkg);
	} else {
		var type = element.getAttribute("data-type");
		var member = element.getAttribute("data-member");
		// showPage(type, member);
		window.location.hash = type + (member != null ? "#" + member : "");
	}
}

function typeName(type) {
	if (type == undefined) {
		return "[typeName: undefined]";
	}
	var dot = type.lastIndexOf(".");
	var localName = (dot >= 0) ? type.substring(dot + 1): type;
	var dollar = localName.lastIndexOf("$");
	return (dollar >= 0) ? localName.substring(dollar + 1): localName;
}

function basePackage(pkg) {
	var dot = pkg.lastIndexOf(".");
	if (dot < 0) {
		return null;
	}
	return pkg.substring(0, dot);
}

function pkgRef(pkg, index) {
	var name = typeName(pkg);
	if (getPackageNode(pkg, index) != null) {
		return nunjucks.runtime.markSafe(
			"<a href='" + "#" + packagePath(pkg) + "'>" + name + "</a>"
		);
	} else {
		return name;
	}
}

function getPackageNode(pkg, node) {
	if (node.packages != undefined) {
		for (var n = 0, cnt = node.packages.length; n < cnt; n++) {
			var p = node.packages[n];
			if (p.name == pkg) {
				return p;
			} else if (pkg.startsWith(p.name) && pkg[p.name.length] == ".") {
				return getPackageNode(pkg, p);
			}
		}
	}
	return null;
}

function outerType(type) {
	var outerName = containingTypeName(type.id);
	if (outerName != null) {
		return {id : outerName};
	} else {
		return null;
	}
}

function containingTypeName(type) {
	var dollar = type.lastIndexOf("$");
	if (dollar < 0) {
		return null;
	}
	return type.substring(0, dollar);
}

function mediumTypeName(type) {
	var name;
	var sep = type.lastIndexOf(".");
	if (sep >= 0) {
		return type.substring(sep + 1).replace(/\$/g, ".");
	} else {
		return type;
	}
}

function fileName(id) {
	var dot = id.lastIndexOf(".");
	var localName = (dot >= 0) ? id.substring(dot + 1): id;
	var dollar = localName.lastIndexOf("$");
	return (dollar >= 0) ? localName.substring(0, dollar): localName;
}

function isExternalPackage(packageName) {
	return !packageName.startsWith("com.top_logic");
}

function isExternal(type) {
	return isExternalPackage(packageName(type));
}

function packageName(id) {
	if (id == undefined) {
		return "[packageName: undefined]";
	}
	var dot = id.lastIndexOf(".");
	if (dot >= 0) {
		return id.substring(0, dot);
	}
	return "";
}

function qualifiedName(id) {
	return id.replace(/\$/g, ".");
}

function onlyStatic(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["static"] == "true") {
				result.push(elem);
			}
		}
	}
	return result;
}

function onlyPublic(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["visibility"] == "public") {
				result.push(elem);
			}
		}
	}
	return result;
}

function nonPublic(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["visibility"] != "public") {
				result.push(elem);
			}
		}
	}
	return result;
}

function onlyAbstract(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["abstract"] == "true") {
				result.push(elem);
			}
		}
	}
	return result;
}

function nonAbstract(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["abstract"] != "true") {
				result.push(elem);
			}
		}
	}
	return result;
}

function onlyDefaults(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["default"] == "true") {
				result.push(elem);
			}
		}
	}
	return result;
}

function sortMethods(obj) {
	if (obj != undefined) {
		obj.sort(function(a, b) { return a.id < b.id ? -1 : (a.id > b.id ? 1 : 0); });
	}
	return obj;
}

function nonDefaults(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["default"] != "true") {
				result.push(elem);
			}
		}
	}
	return result;
}

function annotationFilter(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["id"] == "com.top_logic.basic.config.annotation.Name") {
				continue;
			}
			result.push(elem);
		}
	}
	return result;
}

function nonOverrides(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["overrides"] != undefined) {
				continue;
			}
			result.push(elem);
		}
	}
	return result;
}

function nonSetters(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["id"].startsWith("set")) {
				continue;
			}
			result.push(elem);
		}
	}
	return result;
}

function simpleMemberName(method) {
	var start = method.indexOf("(");
	if (start >= 0) {
		var name = method.substring(0, start);
		var end = method.indexOf(")", start + 1);
		if (end == start + 1) {
			return name + "()";
		} else {
			return name + "(\u2026)";
		}
	} else {
		return method;
	}
}

function methodName(method) {
	var id = method.id;
	var idx = id.indexOf("(");
	if (idx >= 0) {
		return id.substring(0, idx);
	} else {
		return id;
	}
}

function getConfigInterface(methods, name) {
	if (methods == undefined) {
		return null;
	}
	
	for (var n = 0, cnt = methods.length; n < cnt; n++) {
		var method = methods[n];
		
		var start = method.id.indexOf("(");
		var stop = method.id.indexOf(",");
		
		if (start < 0 || stop < 0) {
			continue;
		}
		
		var firstType = method.id.substring(start + 1, stop);
		if (firstType != "com.top_logic.basic.config.InstantiationContext") {
			continue;
		}
		
		var next = method.id.indexOf(",", stop + 1);
		if (next >= 0) {
			continue;
		}
		
		var end = method.id.indexOf(")", stop + 1);
		var secondType = method.id.substring(stop + 1, end);
		
		return asType(secondType);
	}
	
	return null;
}

function configName(method) {
	var nameAnnotation = getAnnotation(method, "com.top_logic.basic.config.annotation.Name");
	if (nameAnnotation != null) {
		return getAnnotationParam(nameAnnotation, "value").value.label;
	} else {
		var name = simpleMemberName(method.id);
		var baseName;
		if (name.startsWith("get")) {
			baseName = name.substring(3);
		}
		else if (name.startsWith("is")) {
			baseName = name.substring(2);
		}
		else if (name.startsWith("has")) {
			baseName = name.substring(3);
		}
		else {
			baseName = name;
		}
		
		return baseName.replace(/(.[a-z])([A-Z])/g, "$1-$2").toLowerCase();
	}
}

function getAnnotation(method, id) {
	var annotations = method.annotations;
	if (annotations == undefined) {
		return null;
	}
	for (var n = 0, cnt = annotations.length; n < cnt; n++) {
		var annotation = annotations[n];
		if (annotation.id == id) {
			return annotation;
		}
	}
	return null;
}

function getAnnotationParam(annotation, name) {
	var params = annotation.params;
	if (params == undefined) {
		return null;
	}
	for (var n = 0, cnt = params.length; n < cnt; n++) {
		var param = params[n];
		if (param.name == name) {
			return param;
		}
	}
	return null;
}

function nonStatic(obj) {
	var result = [];
	if (obj != undefined) {
		for (var n = 0, cnt = obj.length; n < cnt; n++) {
			var elem = obj[n];
			if (elem["static"] != "true") {
				result.push(elem);
			}
		}
	}
	return result;
}

function withType(methods, type) {
	var result = [];
	if (methods != undefined) {
		for (var n = 0, cnt = methods.length; n < cnt; n++) {
			var method = methods[n];
			if (method["return"].type.id == type) {
				result.push(method);
			}
		}
	}
	return result;
}

function withoutType(methods, type) {
	var result = [];
	if (methods != undefined) {
		for (var n = 0, cnt = methods.length; n < cnt; n++) {
			var method = methods[n];
			if (method["return"].type.id != type) {
				result.push(method);
			}
		}
	}
	return result;
}

function withDoc(objs) {
	var result = [];
	if (objs != undefined) {
		for (var n = 0, cnt = objs.length; n < cnt; n++) {
			var obj = objs[n];
			if (obj["title"] != undefined || obj["doc"] != undefined) {
				result.push(obj);
			}
		}
	}
	return result;
}

function nonEmpty(list) {
	if (list == undefined) {
		return false;
	}
	
	return list.length > 0;
}

function showHashPage() {
	var hash = window.location.hash;
	if (hash != null && hash.startsWith("#")) {
		var name = hash.substring(1);
		
		var sep = name.indexOf("#");
		
		var type;
		var member;
		if (sep >= 0) {
			type = name.substring(0, sep);
			member = name.substring(sep + 1)
		} else {
			if (name == "package-list") {
				showIndex();
				return;
			}
			else if (name.endsWith(".package-info")) {
				var pkg = name.substring(0, name.length - ".package-info".length);
				showPackage(pkg);
				return;
			}
			type = name;
			member = null;
		}
		showPage(type, member);
	} else {
		showIndex();
	}
}

function srcUrl(id, settings, pkgInfo, line) {
	return settings.srcBasePath + pkgInfo.srcLink + packageName(id).replace(/\./g, "/") + "/" + fileName(id) + ".java" + ((line != undefined) ? "#L" + line: "");
}

function togglePackage(node) {
	$(node.parentNode).toggleClass('expanded collapsed');
	
	var expanded = $(node.parentNode).hasClass('expanded');
	var pkg = node.textContent.trim();
	
	packageExpanded.set(pkg, expanded);
}

function isExpanded(pkg, depth) {
	var explicit = packageExpanded.get(pkg.name);
	if (explicit != undefined) {
		return explicit;
	}
	return false;
}

$(document).ready(function() {
	if (! String.prototype.startsWith) {
		displayUnsupported();
		return;
	}
	
	init();
	var env = nunjucks.configure({
		autoescape: true,
		web: {
			useCache: true
		}
	});
	
	env.addFilter("doc", docOutput, false);
	env.addFilter("docPlain", docPlain, false);
	env.addFilter("typeName", typeName, false);
	env.addFilter("asType", asType, false);
	env.addFilter("qualifiedName", qualifiedName, false);
	env.addFilter("isExternal", isExternal, false);
	env.addFilter("rawLabel", rawLabel, false);
	env.addFilter("basePackage", basePackage, false);
	env.addFilter("pkgRef", pkgRef, false);
	env.addFilter("getPackageNode", getPackageNode, false);
	env.addFilter("outerType", outerType, false);
	env.addFilter("containingTypeName", containingTypeName, false);
	env.addFilter("mediumTypeName", mediumTypeName, false);
	env.addFilter("typeRef", typeRef, false);
	env.addFilter("typeNameRef", typeNameRef, false);
	env.addFilter("localTypeRef", localTypeRef, false);
	
	env.addFilter("onlyStatic", onlyStatic, false);
	env.addFilter("nonStatic", nonStatic, false);

	env.addFilter("onlyPublic", onlyPublic, false);
	env.addFilter("nonPublic", nonPublic, false);
	
	env.addFilter("onlyAbstract", onlyAbstract, false);
	env.addFilter("nonAbstract", nonAbstract, false);

	env.addFilter("onlyDefaults", onlyDefaults, false);
	env.addFilter("sortMethods", sortMethods, false);
	env.addFilter("nonDefaults", nonDefaults, false);
	env.addFilter("annotationFilter", annotationFilter, false);

	env.addFilter("nonOverrides", nonOverrides, false);
	env.addFilter("nonSetters", nonSetters, false);
	env.addFilter("configName", configName, false);
	env.addFilter("methodName", methodName, false);
	env.addFilter("simpleMemberName", simpleMemberName, false);
	env.addFilter("getConfigInterface", getConfigInterface, false);

	env.addFilter("nonEmpty", nonEmpty, false);
	env.addFilter("packageName", packageName, false);
	env.addFilter("startRef", startRef, false);
	env.addFilter("endRef", endRef, false);
	env.addFilter("withType", withType, false);
	env.addFilter("withoutType", withoutType, false);
	env.addFilter("withDoc", withDoc, false);
	env.addFilter("srcUrl", srcUrl, false);
	env.addFilter("packageRef", packageRef, false);

	env.addFilter("isExpanded", isExpanded, false);
	
	env.addFilter("enumValueRef", enumValueRef, false);

	showHashPage();
	
	window.onhashchange = function() {
		showHashPage();
	}	
});
