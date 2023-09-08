export function getEuclidianDistance(point1, point2) {
  var xDifference = point1.x - point2.x;
  var yDifference = point1.y - point2.y;

  return Math.sqrt(xDifference*xDifference + yDifference*yDifference);
};

export function getRelativeClosePoint(point1, point2, t) {
  return {
    x: (1-t)*point1.x + t*point2.x,
    y: (1-t)*point1.y + t*point2.y
  }
};

export function getAbsoluteClosePoint(point1, point2, absoluteDistance) {
  var distance = getEuclidianDistance(point1, point2);

  return getRelativeClosePoint(point1, point2, absoluteDistance / distance);
};
