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
  return isObject(value) && has(value, 'children') && !isConnection(value);
}

export * from 'diagram-js/lib/util/ModelUtil';