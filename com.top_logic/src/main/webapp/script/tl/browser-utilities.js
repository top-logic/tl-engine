/* -------------------------------------------------------------------------- *
 * Extends Array
 * -------------------------------------------------------------------------- */
 
function arrayCopy(source) {
	var copy = new Array();
	
	for (var i = 0; i < source.length; i++) {
		copy[i] = source[i];
 	}

	return copy;
}

function arrayRemove(target, element) {
	for (var i = target.length - 1; i >= 0; i--) {
		if (target[i] === element) {
			target.splice(i, 1);
		}
 	}
}

function arrayContains(array, element) {
	for (var i = 0; i < array.length; i++) {
		if (array[i] == element) {
			return true;
		}
 	}

	return false;
}

function arrayIndexOf(array, element, startIndex) {
	for (var i = (startIndex == undefined) ? 0 : startIndex; i < array.length; i++) {
		if (array[i] == element) {
			return i;
		}
 	}

	return -1;
}

/* -------------------------------------------------------------------------- *
 * Functional Utilities
 * -------------------------------------------------------------------------- */
	 
/**
 * Iterates over the given array and applies the given function to each array element. 
 * Returns the first non-null result of a function application. The iteration is aborted
 * after the first function application returns a non-null result. If none of the function
 * applications returns a non-null value, the given default value is returned.
 */
function foreach(array, method, defaultValue) {
	for (var i = 0; i < array.length; i++) {
		var result = method(array[i]);
			
		if (result === undefined) continue;
		
		return result;
	}
		
	return defaultValue;
}

/**
 * Returns an array of results of applications of the given function to the elements 
 * of the given array.
 */
function map(array, method) {
	var result = [];
	for (var i = 0; i < array.length; i++) {
		result.push(method(array[i]));
	}
	return result;	
}

/**
 * Return all elements of the given array, for which the given method 
 * returns true.
 */
function filter(array, method) {
	var result = [];
	for (var i = 0; i < array.length; i++) {
		var element = array[i];

		if (method(element)) {
			result.push(element);
		}
	}
	return result;	
}

/**
 * Returns the first element of the array for witch the test returns true
 * (more precise: a value coerced to true in an if-condition),
 * or undefined if there is no such element.
 */
function findFirst(array, test) {
	for (var i = 0; i < array.length; i++) {
		if (test(array[i])) {
			return array[i];
		}
	}
	return undefined;
}

/**
 * Creates a list starting with the given start value.
 * The second element is the result of applying the successor function on the start element.
 * The third element is the result of applying the successor function on the second element.
 * The element (n+1) is the result of applying the successor function on the n-th element.
 * The list ends when the successor function returns undefined or null.
 * If the start value is undefined or null, the list will be empty.
 * This means, you cannot create a list containing undefined or null with this function.
 * The successor Function has one parameter: The element for which it should return the successor.
 */
function createList(start, successorFunction) {
	var resultList = [];
	var currentObject = start;
	while (currentObject != null) {
		resultList.push(currentObject);
		currentObject = successorFunction(currentObject);
	}
	return resultList;
}
