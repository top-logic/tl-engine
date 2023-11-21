import {
  has,
  isObject
} from 'min-dash';

import {
	isConnection
} from 'diagram-js/lib/util/ModelUtil';

/**
 * Checks whether a value is an instance of Shape.
 *
 * @param {any} value
 *
 * @return {boolean}
 */
export function isShape(value) {
	// TODO SFO.. use of !isConnection is not the cleanest way to implement this..
  return isObject(value) && has(value, 'children') && !isConnection(value);
}

export * from 'diagram-js/lib/util/ModelUtil';