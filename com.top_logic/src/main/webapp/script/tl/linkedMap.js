/**
 * LinkedMap is a stub implementation of a map which returns the values in the
 * order in which they are inserted.
 */
services.LinkedMap = function() {
	this.key = 0;
	this.clear();
};

services.LinkedMap.prototype.clear = function() {
	this.storage = new Object();
	this.order = new Array();
};

services.LinkedMap.prototype.newKey = function() {
	return this.key++;
};

/**
 * 
 * @param key - any number or string
 * @param value - any object
 */
services.LinkedMap.prototype.put = function(key, value) {
	if (! this.storage.hasOwnProperty(key)) {
		this.order.push(key);
	}
	this.storage[key] = value;
};

services.LinkedMap.prototype.values = function() {
	if (this.order.length == 0) {
		return null;
	}
	var values = new Array();
	for (var index = 0; index < this.order.length; index++) {
		values[index] = this.storage[this.order[index]];
	}
	return values;
};

services.LinkedMap.prototype.keys = function() {
	if (this.order.length == 0) {
		return null;
	}
	var keys = new Array();
	for (var index = 0; index < this.order.length; index++) {
		keys.push(this.order[index]);
	}
	return keys;
};

services.LinkedMap.prototype.size = function() {
	return this.order.length;
};

services.LinkedMap.prototype.contains = function(key) {
	return this.storage[key] != null;
};
