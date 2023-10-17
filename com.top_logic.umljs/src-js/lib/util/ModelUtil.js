import {
  has,
  isObject
} from 'min-dash';

/**
 * Checks whether a value is an instance of Shape.
 *
 * @param {any} value
 *
 * @return {boolean}
 */
export function isShape(value) {
  return isObject(value) && has(value, 'children');
}

export * from 'diagram-js/lib/util/ModelUtil';