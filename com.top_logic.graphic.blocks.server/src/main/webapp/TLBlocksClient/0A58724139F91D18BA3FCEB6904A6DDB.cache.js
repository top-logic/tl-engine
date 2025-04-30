var $wnd = $wnd || window.parent;
var __gwtModuleFunction = $wnd.TLBlocksClient;
var $sendStats = __gwtModuleFunction.__sendStats;
$sendStats('moduleStartup', 'moduleEvalStart');
var $gwt_version = "2.10.0";
var $strongName = '0A58724139F91D18BA3FCEB6904A6DDB';
var $gwt = {};
var $doc = $wnd.document;
var $moduleName, $moduleBase;
function __gwtStartLoadingFragment(frag) {
var fragFile = 'deferredjs/' + $strongName + '/' + frag + '.cache.js';
return __gwtModuleFunction.__startLoadingFragment(fragFile);
}
function __gwtInstallCode(code) {return __gwtModuleFunction.__installRunAsyncCode(code);}
function __gwt_isKnownPropertyValue(propName, propValue) {
return __gwtModuleFunction.__gwt_isKnownPropertyValue(propName, propValue);
}
function __gwt_getMetaProperty(name) {
return __gwtModuleFunction.__gwt_getMetaProperty(name);
}
var $stats = $wnd.__gwtStatsEvent ? function(a) {
return $wnd.__gwtStatsEvent && $wnd.__gwtStatsEvent(a);
} : null;
var $sessionId = $wnd.__gwtStatsSessionId ? $wnd.__gwtStatsSessionId : null;
var $intern_0 = {3:1, 5:1}, $intern_1 = 2147483647, $intern_2 = {3:1, 16:1, 11:1}, $intern_3 = {3:1, 5:1, 12:1}, $intern_4 = {3:1}, $intern_5 = 65536, $intern_6 = 65535, $intern_7 = 4194303, $intern_8 = 1048575, $intern_9 = 524288, $intern_10 = 4194304, $intern_11 = 17592186044416, $intern_12 = -17592186044416, $intern_13 = {3:1, 82:1, 11:1}, $intern_14 = {l:3355444, m:838860, h:996147}, $intern_15 = {l:0, m:0, h:524288}, $intern_16 = {32:1, 3:1, 11:1}, $intern_17 = {24:1, 20:1}, $intern_18 = {55:1}, $intern_19 = {48:1}, $intern_20 = {123:1, 3:1, 5:1}, $intern_21 = 0.5522847498307935, $intern_22 = 0.44771525016920655, $intern_23 = {24:1}, $intern_24 = {27:1, 57:1, 138:1}, $intern_25 = {54:1}, $intern_26 = {27:1, 57:1, 106:1}, $intern_27 = {108:1}, $intern_28 = 1.7976931348623157E308, $intern_29 = {27:1, 8:1, 36:1, 19:1, 28:1, 30:1, 21:1}, $intern_30 = {24:1, 20:1, 13:1}, $intern_31 = {3:1, 24:1, 20:1, 13:1, 51:1}, $intern_32 = {72:1, 78:1}, $intern_33 = {158:1, 3:1, 5:1}, $intern_34 = {243:1, 188:1}, $intern_35 = {33:1, 83:1}, $intern_36 = {24:1, 20:1, 39:1}, $intern_37 = {22:1}, $intern_38 = {3:1, 24:1, 20:1, 17:1, 39:1}, $intern_39 = {3:1, 5:1, 459:1}, $intern_40 = {7:1, 4:1}, $intern_41 = {7:1, 4:1, 43:1};
var _, prototypesByTypeId_0, initFnList_0, permutationId = -1;
function setGwtProperty(propertyName, propertyValue){
  typeof window === 'object' && typeof window['$gwt'] === 'object' && (window['$gwt'][propertyName] = propertyValue);
}

function gwtOnLoad_0(errFn, modName, modBase, softPermutationId){
  ensureModuleInit();
  var initFnList = initFnList_0;
  $moduleName = modName;
  $moduleBase = modBase;
  permutationId = softPermutationId;
  function initializeModules(){
    for (var i = 0; i < initFnList.length; i++) {
      initFnList[i]();
    }
  }

  if (errFn) {
    try {
      $entry(initializeModules)();
    }
     catch (e) {
      errFn(modName, e);
    }
  }
   else {
    $entry(initializeModules)();
  }
}

function ensureModuleInit(){
  initFnList_0 == null && (initFnList_0 = []);
}

function addInitFunctions(){
  ensureModuleInit();
  var initFnList = initFnList_0;
  for (var i = 0; i < arguments.length; i++) {
    initFnList.push(arguments[i]);
  }
}

function typeMarkerFn(){
}

function toString_8(object){
  var number;
  if (Array.isArray(object) && object.typeMarker === typeMarkerFn) {
    return $getName(getClass__Ljava_lang_Class___devirtual$(object)) + '@' + (number = hashCode__I__devirtual$(object) >>> 0 , number.toString(16));
  }
  return object.toString();
}

function provide(namespace, optCtor){
  var cur = $wnd;
  if (namespace === '') {
    return cur;
  }
  var parts = namespace.split('.');
  !(parts[0] in cur) && cur.execScript && cur.execScript('var ' + parts[0]);
  if (optCtor) {
    var clazz = optCtor.prototype.___clazz;
    clazz.jsConstructor = optCtor;
  }
  for (var part; parts.length && (part = parts.shift());) {
    cur = cur[part] = cur[part] || !parts.length && optCtor || {};
  }
  return cur;
}

function portableObjCreate(obj){
  function F(){
  }

  ;
  F.prototype = obj || {};
  return new F;
}

function makeLambdaFunction(samMethod, ctor, ctorArguments){
  var lambda = function(){
    return samMethod.apply(lambda, arguments);
  }
  ;
  ctor.apply(lambda, ctorArguments);
  return lambda;
}

function emptyMethod(){
}

function defineClass(typeId, superTypeIdOrPrototype, castableTypeMap){
  var prototypesByTypeId = prototypesByTypeId_0, superPrototype;
  var prototype_0 = prototypesByTypeId[typeId];
  var clazz = prototype_0 instanceof Array?prototype_0[0]:null;
  if (prototype_0 && !clazz) {
    _ = prototype_0;
  }
   else {
    _ = (superPrototype = superTypeIdOrPrototype && superTypeIdOrPrototype.prototype , !superPrototype && (superPrototype = prototypesByTypeId_0[superTypeIdOrPrototype]) , portableObjCreate(superPrototype));
    _.castableTypeMap = castableTypeMap;
    !superTypeIdOrPrototype && (_.typeMarker = typeMarkerFn);
    prototypesByTypeId[typeId] = _;
  }
  for (var i = 3; i < arguments.length; ++i) {
    arguments[i].prototype = _;
  }
  clazz && (_.___clazz = clazz);
}

$wnd.goog = $wnd.goog || {};
$wnd.goog.global = $wnd.goog.global || $wnd;
prototypesByTypeId_0 = {};
function $equals(this$static, other){
  return maskUndefined(this$static) === maskUndefined(other);
}

function Object_0(){
}

function equals_Ljava_lang_Object__Z__devirtual$(this$static, other){
  return instanceOfString(this$static)?$equals_0(this$static, other):instanceOfDouble(this$static)?(checkCriticalNotNull(this$static) , maskUndefined(this$static) === maskUndefined(other)):instanceOfBoolean(this$static)?(checkCriticalNotNull(this$static) , maskUndefined(this$static) === maskUndefined(other)):hasJavaObjectVirtualDispatch(this$static)?this$static.equals_0(other):isJavaArray(this$static)?$equals(this$static, other):!!this$static && !!this$static.equals?this$static.equals(other):maskUndefined(this$static) === maskUndefined(other);
}

function getClass__Ljava_lang_Class___devirtual$(this$static){
  return instanceOfString(this$static)?Ljava_lang_String_2_classLit:instanceOfDouble(this$static)?Ljava_lang_Double_2_classLit:instanceOfBoolean(this$static)?Ljava_lang_Boolean_2_classLit:hasJavaObjectVirtualDispatch(this$static)?this$static.___clazz:isJavaArray(this$static)?this$static.___clazz:this$static.___clazz || Array.isArray(this$static) && getClassLiteralForArray(Lcom_google_gwt_core_client_JavaScriptObject_2_classLit, 1) || Lcom_google_gwt_core_client_JavaScriptObject_2_classLit;
}

function hashCode__I__devirtual$(this$static){
  return instanceOfString(this$static)?$hashCode_1(this$static):instanceOfDouble(this$static)?$hashCode_0(this$static):instanceOfBoolean(this$static)?$hashCode(this$static):hasJavaObjectVirtualDispatch(this$static)?this$static.hashCode_0():isJavaArray(this$static)?getObjectIdentityHashCode(this$static):!!this$static && !!this$static.hashCode?this$static.hashCode():getObjectIdentityHashCode(this$static);
}

defineClass(1, null, {}, Object_0);
_.equals_0 = function equals(other){
  return $equals(this, other);
}
;
_.getClass_0 = function getClass_0(){
  return this.___clazz;
}
;
_.hashCode_0 = function hashCode_0(){
  return getObjectIdentityHashCode(this);
}
;
_.toString_0 = function toString_1(){
  var number;
  return $getName(getClass__Ljava_lang_Class___devirtual$(this)) + '@' + (number = hashCode__I__devirtual$(this) >>> 0 , number.toString(16));
}
;
_.equals = function(other){
  return this.equals_0(other);
}
;
_.hashCode = function(){
  return this.hashCode_0();
}
;
_.toString = function(){
  return this.toString_0();
}
;
function toStringSimple(obj){
  return obj.toString?obj.toString():'[JavaScriptObject]';
}

function $clinit_StackTraceCreator(){
  $clinit_StackTraceCreator = emptyMethod;
  var c, enforceLegacy;
  enforceLegacy = !supportsErrorStack();
  c = new StackTraceCreator$CollectorModernNoSourceMap;
  collector_1 = enforceLegacy?new StackTraceCreator$CollectorLegacy:c;
}

function captureStackTrace(error){
  $clinit_StackTraceCreator();
  collector_1.collect(error);
}

function dropInternalFrames(stackTrace){
  var dropFrameUntilFnName, dropFrameUntilFnName2, i, numberOfFramesToSearch;
  dropFrameUntilFnName = 'captureStackTrace';
  dropFrameUntilFnName2 = 'initializeBackingError';
  numberOfFramesToSearch = $wnd.Math.min(stackTrace.length, 5);
  for (i = numberOfFramesToSearch - 1; i >= 0; i--) {
    if ($equals_0(stackTrace[i].methodName, dropFrameUntilFnName) || $equals_0(stackTrace[i].methodName, dropFrameUntilFnName2)) {
      stackTrace.length >= i + 1 && stackTrace.splice(0, i + 1);
      break;
    }
  }
  return stackTrace;
}

function extractFunctionName(fnName){
  var fnRE = /function(?:\s+([\w$]+))?\s*\(/;
  var match_0 = fnRE.exec(fnName);
  return match_0 && match_0[1] || 'anonymous';
}

function parseInt_0(number){
  $clinit_StackTraceCreator();
  return parseInt(number) || -1;
}

function split_0(t){
  $clinit_StackTraceCreator();
  var e = t.backingJsObject;
  if (e && e.stack) {
    var stack_0 = e.stack;
    var toString_0 = e + '\n';
    stack_0.substring(0, toString_0.length) == toString_0 && (stack_0 = stack_0.substring(toString_0.length));
    return stack_0.split('\n');
  }
  return [];
}

function supportsErrorStack(){
  if (Error.stackTraceLimit > 0) {
    $wnd.Error.stackTraceLimit = Error.stackTraceLimit = 64;
    return true;
  }
  return 'stack' in new Error;
}

var collector_1;
defineClass(482, 1, {});
function StackTraceCreator$CollectorLegacy(){
}

defineClass(257, 482, {}, StackTraceCreator$CollectorLegacy);
_.collect = function collect(error){
  var seen = {}, name_1;
  var fnStack = [];
  error['fnStack'] = fnStack;
  var callee = arguments.callee.caller;
  while (callee) {
    var name_0 = ($clinit_StackTraceCreator() , callee.name || (callee.name = extractFunctionName(callee.toString())));
    fnStack.push(name_0);
    var keyName = ':' + name_0;
    var withThisName = seen[keyName];
    if (withThisName) {
      var i, j;
      for (i = 0 , j = withThisName.length; i < j; i++) {
        if (withThisName[i] === callee) {
          return;
        }
      }
    }
    (withThisName || (seen[keyName] = [])).push(callee);
    callee = callee.caller;
  }
}
;
_.getStackTrace = function getStackTrace(t){
  var i, length_0, stack_0, stackTrace;
  stack_0 = ($clinit_StackTraceCreator() , t && t['fnStack']?t['fnStack']:[]);
  length_0 = stack_0.length;
  stackTrace = initUnidimensionalArray(Ljava_lang_StackTraceElement_2_classLit, $intern_0, 75, length_0, 0, 1);
  for (i = 0; i < length_0; i++) {
    stackTrace[i] = new StackTraceElement(stack_0[i], null, -1);
  }
  return stackTrace;
}
;
function $parse(this$static, stString){
  var closeParen, col, endFileUrlIndex, fileName, index_0, lastColonIndex, line, location_0, toReturn;
  location_0 = '';
  if (stString.length == 0) {
    return this$static.createSte('Unknown', 'anonymous', -1, -1);
  }
  toReturn = $trim(stString);
  $equals_0(toReturn.substr(0, 3), 'at ') && (toReturn = (checkCriticalStringElementIndex(3, toReturn.length + 1) , toReturn.substr(3)));
  toReturn = toReturn.replace(/\[.*?\]/g, '');
  index_0 = toReturn.indexOf('(');
  if (index_0 == -1) {
    index_0 = toReturn.indexOf('@');
    if (index_0 == -1) {
      location_0 = toReturn;
      toReturn = '';
    }
     else {
      location_0 = $trim((checkCriticalStringElementIndex(index_0 + 1, toReturn.length + 1) , toReturn.substr(index_0 + 1)));
      toReturn = $trim((checkCriticalStringBounds(0, index_0, toReturn.length) , toReturn.substr(0, index_0)));
    }
  }
   else {
    closeParen = toReturn.indexOf(')', index_0);
    location_0 = (checkCriticalStringBounds(index_0 + 1, closeParen, toReturn.length) , toReturn.substr(index_0 + 1, closeParen - (index_0 + 1)));
    toReturn = $trim((checkCriticalStringBounds(0, index_0, toReturn.length) , toReturn.substr(0, index_0)));
  }
  index_0 = $indexOf_1(toReturn, fromCodePoint(46));
  index_0 != -1 && (toReturn = (checkCriticalStringElementIndex(index_0 + 1, toReturn.length + 1) , toReturn.substr(index_0 + 1)));
  (toReturn.length == 0 || $equals_0(toReturn, 'Anonymous function')) && (toReturn = 'anonymous');
  lastColonIndex = $lastIndexOf_0(location_0, fromCodePoint(58));
  endFileUrlIndex = $lastIndexOf_1(location_0, fromCodePoint(58), lastColonIndex - 1);
  line = -1;
  col = -1;
  fileName = 'Unknown';
  if (lastColonIndex != -1 && endFileUrlIndex != -1) {
    fileName = (checkCriticalStringBounds(0, endFileUrlIndex, location_0.length) , location_0.substr(0, endFileUrlIndex));
    line = parseInt_0((checkCriticalStringBounds(endFileUrlIndex + 1, lastColonIndex, location_0.length) , location_0.substr(endFileUrlIndex + 1, lastColonIndex - (endFileUrlIndex + 1))));
    col = parseInt_0((checkCriticalStringElementIndex(lastColonIndex + 1, location_0.length + 1) , location_0.substr(lastColonIndex + 1)));
  }
  return this$static.createSte(fileName, toReturn, line, col);
}

defineClass(483, 482, {});
_.collect = function collect_0(error){
}
;
_.createSte = function createSte(fileName, method, line, col){
  return new StackTraceElement(method, fileName + '@' + col, line < 0?-1:line);
}
;
_.getStackTrace = function getStackTrace_0(t){
  var addIndex, i, length_0, stack_0, stackTrace, ste;
  stack_0 = split_0(t);
  stackTrace = initUnidimensionalArray(Ljava_lang_StackTraceElement_2_classLit, $intern_0, 75, 0, 0, 1);
  addIndex = 0;
  length_0 = stack_0.length;
  if (length_0 == 0) {
    return stackTrace;
  }
  ste = $parse(this, stack_0[0]);
  $equals_0(ste.methodName, 'anonymous') || (stackTrace[addIndex++] = ste);
  for (i = 1; i < length_0; i++) {
    stackTrace[addIndex++] = $parse(this, stack_0[i]);
  }
  return stackTrace;
}
;
function StackTraceCreator$CollectorModernNoSourceMap(){
}

defineClass(258, 483, {}, StackTraceCreator$CollectorModernNoSourceMap);
_.createSte = function createSte_0(fileName, method, line, col){
  return new StackTraceElement(method, fileName, -1);
}
;
function canSet(array, value_0){
  var elementTypeCategory;
  switch (getElementTypeCategory(array)) {
    case 6:
      return instanceOfString(value_0);
    case 7:
      return instanceOfDouble(value_0);
    case 8:
      return instanceOfBoolean(value_0);
    case 3:
      return Array.isArray(value_0) && (elementTypeCategory = getElementTypeCategory(value_0) , !(elementTypeCategory >= 14 && elementTypeCategory <= 16));
    case 11:
      return value_0 != null && typeof value_0 === 'function';
    case 12:
      return value_0 != null && (typeof value_0 === 'object' || typeof value_0 == 'function');
    case 0:
      return canCast(value_0, array.__elementTypeId$);
    case 2:
      return isJsObjectOrFunction(value_0) && !(value_0.typeMarker === typeMarkerFn);
    case 1:
      return isJsObjectOrFunction(value_0) && !(value_0.typeMarker === typeMarkerFn) || canCast(value_0, array.__elementTypeId$);
    default:return true;
  }
}

function getClassLiteralForArray(clazz, dimensions){
  return getClassLiteralForArray_0(clazz, dimensions);
}

function getElementTypeCategory(array){
  return array.__elementTypeCategory$ == null?10:array.__elementTypeCategory$;
}

function initUnidimensionalArray(leafClassLiteral, castableTypeMap, elementTypeId, length_0, elementTypeCategory, dimensions){
  var result;
  result = initializeArrayElementsWithDefaults(elementTypeCategory, length_0);
  elementTypeCategory != 10 && stampJavaTypeInfo(getClassLiteralForArray(leafClassLiteral, dimensions), castableTypeMap, elementTypeId, elementTypeCategory, result);
  return result;
}

function initializeArrayElementsWithDefaults(elementTypeCategory, length_0){
  var array = new Array(length_0);
  var initValue;
  switch (elementTypeCategory) {
    case 14:
    case 15:
      initValue = 0;
      break;
    case 16:
      initValue = false;
      break;
    default:return array;
  }
  for (var i = 0; i < length_0; ++i) {
    array[i] = initValue;
  }
  return array;
}

function isJavaArray(src_0){
  return Array.isArray(src_0) && src_0.typeMarker === typeMarkerFn;
}

function setCheck(array, index_0, value_0){
  checkCriticalArrayType(value_0 == null || canSet(array, value_0));
  return array[index_0] = value_0;
}

function stampJavaTypeInfo(arrayClass, castableTypeMap, elementTypeId, elementTypeCategory, array){
  array.___clazz = arrayClass;
  array.castableTypeMap = castableTypeMap;
  array.typeMarker = typeMarkerFn;
  array.__elementTypeId$ = elementTypeId;
  array.__elementTypeCategory$ = elementTypeCategory;
  return array;
}

function stampJavaTypeInfo_0(array, referenceType){
  getElementTypeCategory(referenceType) != 10 && stampJavaTypeInfo(getClass__Ljava_lang_Class___devirtual$(referenceType), referenceType.castableTypeMap, referenceType.__elementTypeId$, getElementTypeCategory(referenceType), array);
  return array;
}

function canCast(src_0, dstId){
  if (instanceOfString(src_0)) {
    return !!stringCastMap[dstId];
  }
   else if (src_0.castableTypeMap) {
    return !!src_0.castableTypeMap[dstId];
  }
   else if (instanceOfDouble(src_0)) {
    return !!doubleCastMap[dstId];
  }
   else if (instanceOfBoolean(src_0)) {
    return !!booleanCastMap[dstId];
  }
  return false;
}

function castTo(src_0, dstId){
  checkCriticalType(src_0 == null || canCast(src_0, dstId));
  return src_0;
}

function castToArray(src_0){
  var elementTypeCategory;
  checkCriticalType(src_0 == null || Array.isArray(src_0) && (elementTypeCategory = getElementTypeCategory(src_0) , !(elementTypeCategory >= 14 && elementTypeCategory <= 16)));
  return src_0;
}

function castToBoolean(src_0){
  checkCriticalType(src_0 == null || instanceOfBoolean(src_0));
  return src_0;
}

function castToDouble(src_0){
  checkCriticalType(src_0 == null || instanceOfDouble(src_0));
  return src_0;
}

function castToJso(src_0){
  checkCriticalType(src_0 == null || isJsObjectOrFunction(src_0) && !(src_0.typeMarker === typeMarkerFn));
  return src_0;
}

function castToJsoArray(src_0, dstId){
  checkCriticalType(src_0 == null || canCast(src_0, dstId) || !(src_0.typeMarker === typeMarkerFn) && Array.isArray(src_0));
  return src_0;
}

function castToNative(src_0, jsType){
  checkCriticalType(src_0 == null || jsinstanceOf(src_0, jsType));
  return src_0;
}

function castToString(src_0){
  checkCriticalType(src_0 == null || instanceOfString(src_0));
  return src_0;
}

function hasJavaObjectVirtualDispatch(src_0){
  return !Array.isArray(src_0) && src_0.typeMarker === typeMarkerFn;
}

function instanceOf(src_0, dstId){
  return src_0 != null && canCast(src_0, dstId);
}

function instanceOfBoolean(src_0){
  return typeof src_0 === 'boolean';
}

function instanceOfDouble(src_0){
  return typeof src_0 === 'number';
}

function instanceOfJso(src_0){
  return src_0 != null && isJsObjectOrFunction(src_0) && !(src_0.typeMarker === typeMarkerFn);
}

function instanceOfNative(src_0, jsType){
  return jsinstanceOf(src_0, jsType);
}

function instanceOfString(src_0){
  return typeof src_0 === 'string';
}

function isJsObjectOrFunction(src_0){
  return typeof src_0 === 'object' || typeof src_0 === 'function';
}

function jsinstanceOf(obj, jsType){
  return obj && jsType && obj instanceof jsType;
}

function maskUndefined(src_0){
  return src_0 == null?null:src_0;
}

function round_int(x_0){
  return Math.max(Math.min(x_0, $intern_1), -2147483648) | 0;
}

function throwClassCastExceptionUnlessNull(o){
  checkCriticalType(o == null);
  return o;
}

var booleanCastMap, doubleCastMap, stringCastMap;
function safeClose(resource, mainException){
  var e;
  if (!resource) {
    return mainException;
  }
  try {
    resource.close_0();
  }
   catch ($e0) {
    $e0 = toJava($e0);
    if (instanceOf($e0, 11)) {
      e = $e0;
      if (!mainException) {
        return e;
      }
      $addSuppressed(mainException, e);
    }
     else 
      throw toJs($e0);
  }
  return mainException;
}

function toJava(e){
  var javaException;
  if (instanceOf(e, 11)) {
    return e;
  }
  javaException = e && e.__java$exception;
  if (!javaException) {
    javaException = new JavaScriptException(e);
    captureStackTrace(javaException);
  }
  return javaException;
}

function toJs(t){
  return t.backingJsObject;
}

function $isInstance(instance){
  var type_0;
  if (instance == null) {
    return false;
  }
  type_0 = typeof(instance);
  return $equals_0(type_0, 'boolean') || $equals_0(type_0, 'number') || $equals_0(type_0, 'string') || instance.$implements__java_io_Serializable || Array.isArray(instance);
}

function $isInstance_3(instance){
  var type_0;
  type_0 = typeof(instance);
  if ($equals_0(type_0, 'boolean') || $equals_0(type_0, 'number') || $equals_0(type_0, 'string')) {
    return true;
  }
  return instance != null && instance.$implements__java_lang_Comparable;
}

function $clinit_Boolean(){
  $clinit_Boolean = emptyMethod;
}

function $booleanValue(this$static){
  return checkCriticalNotNull(this$static) , this$static;
}

function $compareTo_0(this$static, b){
  return compare_2((checkCriticalNotNull(this$static) , this$static), (checkCriticalNotNull(b) , b));
}

function $hashCode(this$static){
  return (checkCriticalNotNull(this$static) , this$static)?1231:1237;
}

function $isInstance_0(instance){
  $clinit_Boolean();
  return $equals_0('boolean', typeof(instance));
}

function compare_2(x_0, y_0){
  $clinit_Boolean();
  return x_0 == y_0?0:x_0?1:-1;
}

function compareTo_Ljava_lang_Object__I__devirtual$(this$static, other){
  $clinit_Boolean();
  return instanceOfString(this$static)?$compareTo_3(this$static, castToString(other)):instanceOfDouble(this$static)?$compareTo_1(this$static, castToDouble(other)):instanceOfBoolean(this$static)?$compareTo_0(this$static, castToBoolean(other)):this$static.compareTo_0(other);
}

booleanCastMap = {3:1, 253:1, 33:1};
function $isInstance_1(instance){
  if ($equals_0(typeof(instance), 'string')) {
    return true;
  }
  return instance != null && instance.$implements__java_lang_CharSequence;
}

function $ensureNamesAreInitialized(this$static){
  if (this$static.typeName != null) {
    return;
  }
  initializeNames(this$static);
}

function $getName(this$static){
  $ensureNamesAreInitialized(this$static);
  return this$static.typeName;
}

function Class(){
  ++nextSequentialId;
  this.typeName = null;
  this.simpleName = null;
  this.packageName = null;
  this.compoundName = null;
  this.canonicalName = null;
  this.typeId = null;
  this.arrayLiterals = null;
}

function createClassObject(packageName, compoundClassName){
  var clazz;
  clazz = new Class;
  clazz.packageName = packageName;
  clazz.compoundName = compoundClassName;
  return clazz;
}

function createForClass(packageName, compoundClassName, typeId){
  var clazz;
  clazz = createClassObject(packageName, compoundClassName);
  maybeSetClassLiteral(typeId, clazz);
  return clazz;
}

function createForEnum(packageName, compoundClassName, typeId, enumConstantsFunc){
  var clazz;
  clazz = createClassObject(packageName, compoundClassName);
  maybeSetClassLiteral(typeId, clazz);
  clazz.modifiers_0 = enumConstantsFunc?8:0;
  return clazz;
}

function createForInterface(packageName, compoundClassName){
  var clazz;
  clazz = createClassObject(packageName, compoundClassName);
  clazz.modifiers_0 = 2;
  return clazz;
}

function createForPrimitive(className, primitiveTypeId){
  var clazz;
  clazz = createClassObject('', className);
  clazz.typeId = primitiveTypeId;
  clazz.modifiers_0 = 1;
  return clazz;
}

function getClassLiteralForArray_0(leafClass, dimensions){
  var arrayLiterals = leafClass.arrayLiterals = leafClass.arrayLiterals || [];
  return arrayLiterals[dimensions] || (arrayLiterals[dimensions] = leafClass.createClassLiteralForArray(dimensions));
}

function getPrototypeForClass(clazz){
  if (clazz.isPrimitive()) {
    return null;
  }
  var typeId = clazz.typeId;
  return prototypesByTypeId_0[typeId];
}

function initializeNames(clazz){
  if (clazz.isArray_0()) {
    var componentType = clazz.componentType;
    componentType.isPrimitive()?(clazz.typeName = '[' + componentType.typeId):!componentType.isArray_0()?(clazz.typeName = '[L' + componentType.getName() + ';'):(clazz.typeName = '[' + componentType.getName());
    clazz.canonicalName = componentType.getCanonicalName() + '[]';
    clazz.simpleName = componentType.getSimpleName() + '[]';
    return;
  }
  var packageName = clazz.packageName;
  var compoundName = clazz.compoundName;
  compoundName = compoundName.split('/');
  clazz.typeName = join_0('.', [packageName, join_0('$', compoundName)]);
  clazz.canonicalName = join_0('.', [packageName, join_0('.', compoundName)]);
  clazz.simpleName = compoundName[compoundName.length - 1];
}

function join_0(separator, strings){
  var i = 0;
  while (!strings[i] || strings[i] == '') {
    i++;
  }
  var result = strings[i++];
  for (; i < strings.length; i++) {
    if (!strings[i] || strings[i] == '') {
      continue;
    }
    result += separator + strings[i];
  }
  return result;
}

function maybeSetClassLiteral(typeId, clazz){
  var proto;
  if (!typeId) {
    return;
  }
  clazz.typeId = typeId;
  var prototype_0 = getPrototypeForClass(clazz);
  if (!prototype_0) {
    prototypesByTypeId_0[typeId] = [clazz];
    return;
  }
  prototype_0.___clazz = clazz;
}

defineClass(187, 1, {}, Class);
_.createClassLiteralForArray = function createClassLiteralForArray(dimensions){
  var clazz;
  clazz = new Class;
  clazz.modifiers_0 = 4;
  dimensions > 1?(clazz.componentType = getClassLiteralForArray_0(this, dimensions - 1)):(clazz.componentType = this);
  return clazz;
}
;
_.getCanonicalName = function getCanonicalName(){
  $ensureNamesAreInitialized(this);
  return this.canonicalName;
}
;
_.getName = function getName(){
  return $getName(this);
}
;
_.getSimpleName = function getSimpleName(){
  return $ensureNamesAreInitialized(this) , this.simpleName;
}
;
_.isArray_0 = function isArray(){
  return (this.modifiers_0 & 4) != 0;
}
;
_.isPrimitive = function isPrimitive(){
  return (this.modifiers_0 & 1) != 0;
}
;
_.toString_0 = function toString_22(){
  return ((this.modifiers_0 & 2) != 0?'interface ':(this.modifiers_0 & 1) != 0?'':'class ') + ($ensureNamesAreInitialized(this) , this.typeName);
}
;
_.modifiers_0 = 0;
var nextSequentialId = 1;
function $isInstance_4(instance){
  return $equals_0('number', typeof(instance)) || instanceOfNative(instance, $wnd.java.lang.Number$impl);
}

function __parseAndValidateDouble(s){
  floatRegex == null && (floatRegex = new RegExp('^\\s*[+-]?(NaN|Infinity|((\\d+\\.?\\d*)|(\\.\\d+))([eE][+-]?\\d+)?[dDfF]?)\\s*$'));
  if (!floatRegex.test(s)) {
    throw toJs(new NumberFormatException('For input string: "' + s + '"'));
  }
  return parseFloat(s);
}

function __parseAndValidateInt(s){
  var i, isTooLow, length_0, startIndex, toReturn;
  if (s == null) {
    throw toJs(new NumberFormatException('null'));
  }
  length_0 = s.length;
  startIndex = length_0 > 0 && (checkCriticalStringElementIndex(0, s.length) , s.charCodeAt(0) == 45 || (checkCriticalStringElementIndex(0, s.length) , s.charCodeAt(0) == 43))?1:0;
  for (i = startIndex; i < length_0; i++) {
    if (digit((checkCriticalStringElementIndex(i, s.length) , s.charCodeAt(i))) == -1) {
      throw toJs(new NumberFormatException('For input string: "' + s + '"'));
    }
  }
  toReturn = parseInt(s, 10);
  isTooLow = toReturn < -2147483648;
  if (isNaN(toReturn)) {
    throw toJs(new NumberFormatException('For input string: "' + s + '"'));
  }
   else if (isTooLow || toReturn > $intern_1) {
    throw toJs(new NumberFormatException('For input string: "' + s + '"'));
  }
  return toReturn;
}

function doubleValue__D__devirtual$(this$static){
  return instanceOfDouble(this$static)?(checkCriticalNotNull(this$static) , this$static):this$static.doubleValue();
}

defineClass(63, 1, {3:1, 63:1});
var floatRegex;
function $compareTo_1(this$static, b){
  return compare_3((checkCriticalNotNull(this$static) , this$static), (checkCriticalNotNull(b) , b));
}

function $doubleValue(this$static){
  return checkCriticalNotNull(this$static) , this$static;
}

function $hashCode_0(this$static){
  return round_int((checkCriticalNotNull(this$static) , this$static));
}

function $isInstance_5(instance){
  return $equals_0('number', typeof(instance));
}

function compare_3(x_0, y_0){
  if (x_0 < y_0) {
    return -1;
  }
  if (x_0 > y_0) {
    return 1;
  }
  if (x_0 == y_0) {
    return x_0 == 0?compare_3(1 / x_0, 1 / y_0):0;
  }
  return isNaN(x_0)?isNaN(y_0)?0:1:-1;
}

doubleCastMap = {3:1, 33:1, 191:1, 63:1};
function $$init(this$static){
  this$static.stackTrace = initUnidimensionalArray(Ljava_lang_StackTraceElement_2_classLit, $intern_0, 75, 0, 0, 1);
}

function $addSuppressed(this$static, exception){
  checkCriticalNotNull_0(exception, 'Cannot suppress a null exception.');
  checkCriticalArgument(exception != this$static, 'Exception can not suppress itself.');
  if (this$static.disableSuppression) {
    return;
  }
  this$static.suppressedExceptions == null?(this$static.suppressedExceptions = stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_Throwable_2_classLit, 1), $intern_0, 11, 0, [exception])):(this$static.suppressedExceptions[this$static.suppressedExceptions.length] = exception);
}

function $fillInStackTrace(this$static){
  if (this$static.writableStackTrace) {
    this$static.backingJsObject !== '__noinit__' && this$static.initializeBackingError();
    this$static.stackTrace = null;
  }
  return this$static;
}

function $linkBack(this$static, error){
  if (error instanceof Object) {
    try {
      error.__java$exception = this$static;
      if (navigator.userAgent.toLowerCase().indexOf('msie') != -1 && $doc.documentMode < 9) {
        return;
      }
      var throwable = this$static;
      Object.defineProperties(error, {cause:{get:function(){
        var cause = throwable.getCause();
        return cause && cause.getBackingJsObject();
      }
      }, suppressed:{get:function(){
        return throwable.getBackingSuppressed();
      }
      }});
    }
     catch (ignored) {
    }
  }
}

function $printStackTraceImpl(this$static, out, prefix, ident){
  var t, t$array, t$index, t$max, theCause;
  out.println(ident + prefix + this$static);
  $printStackTraceItems(this$static, out, ident);
  for (t$array = (this$static.suppressedExceptions == null && (this$static.suppressedExceptions = initUnidimensionalArray(Ljava_lang_Throwable_2_classLit, $intern_0, 11, 0, 0, 1)) , this$static.suppressedExceptions) , t$index = 0 , t$max = t$array.length; t$index < t$max; ++t$index) {
    t = t$array[t$index];
    $printStackTraceImpl(t, out, 'Suppressed: ', '\t' + ident);
  }
  theCause = this$static.cause_0;
  !!theCause && $printStackTraceImpl(theCause, out, 'Caused by: ', ident);
}

function $printStackTraceItems(this$static, out, ident){
  var element, element$array, element$index, element$max, stackTrace;
  for (element$array = (this$static.stackTrace == null && (this$static.stackTrace = ($clinit_StackTraceCreator() , stackTrace = collector_1.getStackTrace(this$static) , dropInternalFrames(stackTrace))) , this$static.stackTrace) , element$index = 0 , element$max = element$array.length; element$index < element$max; ++element$index) {
    element = element$array[element$index];
    out.println(ident + '\tat ' + element);
  }
}

function $setBackingJsObject(this$static, backingJsObject){
  this$static.backingJsObject = backingJsObject;
  $linkBack(this$static, backingJsObject);
}

function $toString(this$static, message){
  var className;
  className = $getName(this$static.___clazz);
  return message == null?className:className + ': ' + message;
}

function Throwable(){
  $$init(this);
  $fillInStackTrace(this);
  this.initializeBackingError();
}

function Throwable_0(message, cause){
  $$init(this);
  this.cause_0 = cause;
  this.detailMessage = message;
  $fillInStackTrace(this);
  this.initializeBackingError();
}

function fixIE(e){
  if (!('stack' in e)) {
    try {
      throw e;
    }
     catch (ignored) {
    }
  }
  return e;
}

function of(e){
  var throwable;
  if (e != null) {
    throwable = e.__java$exception;
    if (throwable) {
      return throwable;
    }
  }
  return instanceOfNative(e, TypeError)?new NullPointerException_0(e):new JsException(e);
}

defineClass(11, 1, {3:1, 11:1});
_.createError = function createError(msg){
  return new Error(msg);
}
;
_.getBackingJsObject = function getBackingJsObject(){
  return this.backingJsObject;
}
;
_.getBackingSuppressed = function getBackingSuppressed(){
  var i, result, suppressed;
  suppressed = (this.suppressedExceptions == null && (this.suppressedExceptions = initUnidimensionalArray(Ljava_lang_Throwable_2_classLit, $intern_0, 11, 0, 0, 1)) , this.suppressedExceptions);
  result = initUnidimensionalArray(Ljava_lang_Object_2_classLit, $intern_0, 1, suppressed.length, 5, 1);
  for (i = 0; i < suppressed.length; i++) {
    result[i] = suppressed[i].backingJsObject;
  }
  return result;
}
;
_.getCause = function getCause(){
  return this.cause_0;
}
;
_.getMessage = function getMessage(){
  return this.detailMessage;
}
;
_.initializeBackingError = function initializeBackingError(){
  $setBackingJsObject(this, fixIE(this.createError($toString(this, this.detailMessage))));
  captureStackTrace(this);
}
;
_.toString_0 = function toString_2(){
  return $toString(this, this.getMessage());
}
;
_.backingJsObject = '__noinit__';
_.disableSuppression = false;
_.writableStackTrace = true;
function Exception(message){
  $$init(this);
  this.detailMessage = message;
  $fillInStackTrace(this);
  this.initializeBackingError();
}

function Exception_0(cause){
  $$init(this);
  this.detailMessage = !cause?null:$toString(cause, cause.getMessage());
  this.cause_0 = cause;
  $fillInStackTrace(this);
  this.initializeBackingError();
}

defineClass(93, 11, {3:1, 11:1});
function RuntimeException(){
  Throwable.call(this);
}

function RuntimeException_0(message){
  Exception.call(this, message);
}

function RuntimeException_1(message, cause){
  Throwable_0.call(this, message, cause);
}

function RuntimeException_2(cause){
  Exception_0.call(this, cause);
}

defineClass(16, 93, $intern_2, RuntimeException_1, RuntimeException_2);
function IndexOutOfBoundsException(){
  RuntimeException.call(this);
}

function IndexOutOfBoundsException_0(message){
  RuntimeException_0.call(this, message);
}

defineClass(97, 16, $intern_2, IndexOutOfBoundsException, IndexOutOfBoundsException_0);
function JsException(backingJsObject){
  $$init(this);
  $fillInStackTrace(this);
  this.backingJsObject = backingJsObject;
  $linkBack(this, backingJsObject);
  this.detailMessage = backingJsObject == null?'null':toString_8(backingJsObject);
}

defineClass(124, 16, $intern_2, JsException);
function NullPointerException(){
  RuntimeException.call(this);
}

function NullPointerException_0(typeError){
  JsException.call(this, typeError);
}

function NullPointerException_1(message){
  RuntimeException_0.call(this, message);
}

defineClass(62, 124, $intern_2, NullPointerException, NullPointerException_0, NullPointerException_1);
_.createError = function createError_0(msg){
  return new TypeError(msg);
}
;
function $charAt(this$static, index_0){
  checkCriticalStringElementIndex(index_0, this$static.length);
  return this$static.charCodeAt(index_0);
}

function $compareTo_3(this$static, other){
  var a, b;
  a = (checkCriticalNotNull(this$static) , this$static);
  b = (checkCriticalNotNull(other) , other);
  return a == b?0:a < b?-1:1;
}

function $compareToIgnoreCase(this$static, other){
  return $compareTo_3(this$static.toLowerCase(), other.toLowerCase());
}

function $equals_0(this$static, other){
  return checkCriticalNotNull(this$static) , maskUndefined(this$static) === maskUndefined(other);
}

function $equalsIgnoreCase(other){
  checkCriticalNotNull('true');
  if (other == null) {
    return false;
  }
  if ($equals_0('true', other)) {
    return true;
  }
  return 'true'.length == other.length && $equals_0('true'.toLowerCase(), other.toLowerCase());
}

function $getChars(this$static, srcBegin, srcEnd, dst, dstBegin){
  checkCriticalStringBounds(srcBegin, srcEnd, this$static.length);
  checkCriticalStringBounds(dstBegin, dstBegin + (srcEnd - srcBegin), dst.length);
  $getChars0(this$static, srcBegin, srcEnd, dst, dstBegin);
}

function $getChars0(this$static, srcBegin, srcEnd, dst, dstBegin){
  while (srcBegin < srcEnd) {
    dst[dstBegin++] = $charAt(this$static, srcBegin++);
  }
}

function $hashCode_1(this$static){
  var h, i;
  h = 0;
  for (i = 0; i < this$static.length; i++) {
    h = (h << 5) - h + (checkCriticalStringElementIndex(i, this$static.length) , this$static.charCodeAt(i)) | 0;
  }
  return h;
}

function $indexOf_1(this$static, str){
  return this$static.indexOf(str);
}

function $isInstance_6(instance){
  return $equals_0('string', typeof(instance));
}

function $lastIndexOf_0(this$static, str){
  return this$static.lastIndexOf(str);
}

function $lastIndexOf_1(this$static, str, start_0){
  return this$static.lastIndexOf(str, start_0);
}

function $split(this$static, regex, maxMatch){
  var compiled, count, lastNonEmpty, lastTrail, matchIndex, matchObj, out, trail;
  compiled = new RegExp(regex, 'g');
  out = initUnidimensionalArray(Ljava_lang_String_2_classLit, $intern_3, 2, 0, 6, 1);
  count = 0;
  trail = this$static;
  lastTrail = null;
  while (true) {
    matchObj = compiled.exec(trail);
    if (matchObj == null || trail == '' || count == maxMatch - 1 && maxMatch > 0) {
      out[count] = trail;
      break;
    }
     else {
      matchIndex = matchObj.index;
      out[count] = (checkCriticalStringBounds(0, matchIndex, trail.length) , trail.substr(0, matchIndex));
      trail = $substring_0(trail, matchIndex + matchObj[0].length, trail.length);
      compiled.lastIndex = 0;
      if (lastTrail == trail) {
        out[count] = (checkCriticalStringBounds(0, 1, trail.length) , trail.substr(0, 1));
        trail = (checkCriticalStringElementIndex(1, trail.length + 1) , trail.substr(1));
      }
      lastTrail = trail;
      ++count;
    }
  }
  if (maxMatch == 0 && this$static.length > 0) {
    lastNonEmpty = out.length;
    while (lastNonEmpty > 0 && out[lastNonEmpty - 1] == '') {
      --lastNonEmpty;
    }
    lastNonEmpty < out.length && (out.length = lastNonEmpty);
  }
  return out;
}

function $substring(this$static, beginIndex){
  checkCriticalStringElementIndex(beginIndex, this$static.length + 1);
  return this$static.substr(beginIndex);
}

function $substring_0(this$static, beginIndex, endIndex){
  checkCriticalStringBounds(beginIndex, endIndex, this$static.length);
  return this$static.substr(beginIndex, endIndex - beginIndex);
}

function $toCharArray(this$static){
  var charArr, n;
  n = this$static.length;
  charArr = initUnidimensionalArray(C_classLit, $intern_4, 46, n, 15, 1);
  $getChars0(this$static, 0, n, charArr, 0);
  return charArr;
}

function $toLowerCase(this$static, locale){
  return locale == ($clinit_Locale() , $clinit_Locale() , defaultLocale)?this$static.toLocaleLowerCase():this$static.toLowerCase();
}

function $toUpperCase(this$static, locale){
  return locale == ($clinit_Locale() , $clinit_Locale() , defaultLocale)?this$static.toLocaleUpperCase():this$static.toUpperCase();
}

function $trim(this$static){
  var end, length_0, start_0;
  length_0 = this$static.length;
  start_0 = 0;
  while (start_0 < length_0 && (checkCriticalStringElementIndex(start_0, this$static.length) , this$static.charCodeAt(start_0) <= 32)) {
    ++start_0;
  }
  end = length_0;
  while (end > start_0 && (checkCriticalStringElementIndex(end - 1, this$static.length) , this$static.charCodeAt(end - 1) <= 32)) {
    --end;
  }
  return start_0 > 0 || end < length_0?(checkCriticalStringBounds(start_0, end, this$static.length) , this$static.substr(start_0, end - start_0)):this$static;
}

function fromCharCode(array){
  return String.fromCharCode.apply(null, array);
}

function fromCodePoint(codePoint){
  var hiSurrogate, loSurrogate;
  if (codePoint >= $intern_5) {
    hiSurrogate = 55296 + (codePoint - $intern_5 >> 10 & 1023) & $intern_6;
    loSurrogate = 56320 + (codePoint - $intern_5 & 1023) & $intern_6;
    return String.fromCharCode(hiSurrogate) + ('' + String.fromCharCode(loSurrogate));
  }
   else {
    return String.fromCharCode(codePoint & $intern_6);
  }
}

function valueOf_0(x_0){
  return x_0 == null?'null':toString_8(x_0);
}

function valueOf_1(x_0, offset, count){
  var batchEnd, batchStart, end, s;
  end = offset + count;
  checkCriticalStringBounds(offset, end, x_0.length);
  s = '';
  for (batchStart = offset; batchStart < end;) {
    batchEnd = $wnd.Math.min(batchStart + 10000, end);
    s += fromCharCode(x_0.slice(batchStart, batchEnd));
    batchStart = batchEnd;
  }
  return s;
}

stringCastMap = {3:1, 188:1, 33:1, 2:1};
function StringIndexOutOfBoundsException(message){
  IndexOutOfBoundsException_0.call(this, message);
}

defineClass(194, 97, $intern_2, StringIndexOutOfBoundsException);
function clone_0(array){
  var result;
  result = array.slice();
  return stampJavaTypeInfo_0(result, array);
}

function copy_0(src_0, srcOfs, dest, destOfs, len, overwrite){
  var batchEnd, batchStart, destArray, end, spliceArgs;
  if (len == 0) {
    return;
  }
  if (maskUndefined(src_0) === maskUndefined(dest)) {
    src_0 = src_0.slice(srcOfs, srcOfs + len);
    srcOfs = 0;
  }
  destArray = dest;
  for (batchStart = srcOfs , end = srcOfs + len; batchStart < end;) {
    batchEnd = $wnd.Math.min(batchStart + 10000, end);
    len = batchEnd - batchStart;
    spliceArgs = src_0.slice(batchStart, batchEnd);
    spliceArgs.splice(0, 0, destOfs, overwrite?len:0);
    Array.prototype.splice.apply(destArray, spliceArgs);
    batchStart = batchEnd;
    destOfs += len;
  }
}

function insertTo(array, index_0, value_0){
  array.splice(index_0, 0, value_0);
}

function insertTo_0(array, index_0, values){
  copy_0(values, 0, array, index_0, values.length, false);
}

function push_1(array, o){
  array.push(o);
}

function removeFrom_0(array, index_0, deleteCount){
  array.splice(index_0, deleteCount);
}

defineClass(658, 1, {});
function HashCodes(){
}

function getIdentityHashCode(o){
  switch (typeof(o)) {
    case 'string':
      return $hashCode_1(o);
    case 'number':
      return $hashCode_0(o);
    case 'boolean':
      return $hashCode(o);
    default:return o == null?0:getObjectIdentityHashCode(o);
  }
}

function getNextHash(){
  return ++nextHash;
}

function getObjectIdentityHashCode(o){
  return o.$H || (o.$H = ++nextHash);
}

defineClass(480, 1, {}, HashCodes);
var nextHash = 0;
function checkCriticalArgument(expression, errorMessage){
  if (!expression) {
    throw toJs(new IllegalArgumentException(errorMessage));
  }
}

function checkCriticalArrayBounds(end, length_0){
  if (0 > end) {
    throw toJs(new IllegalArgumentException('fromIndex: 0 > toIndex: ' + end));
  }
  if (end > length_0) {
    throw toJs(new ArrayIndexOutOfBoundsException('fromIndex: 0, toIndex: ' + end + ', length: ' + length_0));
  }
}

function checkCriticalArraySize(size_0){
  if (size_0 < 0) {
    throw toJs(new NegativeArraySizeException('Negative array size: ' + size_0));
  }
}

function checkCriticalArrayType(expression){
  if (!expression) {
    throw toJs(new ArrayStoreException);
  }
}

function checkCriticalArrayType_0(expression, errorMessage){
  if (!expression) {
    throw toJs(new ArrayStoreException_0(errorMessage));
  }
}

function checkCriticalConcurrentModification(currentModCount, recordedModCount){
  if (currentModCount != recordedModCount) {
    throw toJs(new ConcurrentModificationException);
  }
}

function checkCriticalElement(expression){
  if (!expression) {
    throw toJs(new NoSuchElementException);
  }
}

function checkCriticalElementIndex(index_0, size_0){
  if (index_0 < 0 || index_0 >= size_0) {
    throw toJs(new IndexOutOfBoundsException_0('Index: ' + index_0 + ', Size: ' + size_0));
  }
}

function checkCriticalNotNull(reference){
  if (reference == null) {
    throw toJs(new NullPointerException);
  }
  return reference;
}

function checkCriticalNotNull_0(reference, errorMessage){
  if (reference == null) {
    throw toJs(new NullPointerException_1(errorMessage));
  }
}

function checkCriticalPositionIndex(index_0, size_0){
  if (index_0 < 0 || index_0 > size_0) {
    throw toJs(new IndexOutOfBoundsException_0('Index: ' + index_0 + ', Size: ' + size_0));
  }
}

function checkCriticalPositionIndexes(start_0, end, size_0){
  if (start_0 < 0 || end > size_0) {
    throw toJs(new IndexOutOfBoundsException_0('fromIndex: ' + start_0 + ', toIndex: ' + end + ', size: ' + size_0));
  }
  if (start_0 > end) {
    throw toJs(new IllegalArgumentException('fromIndex: ' + start_0 + ' > toIndex: ' + end));
  }
}

function checkCriticalState(expression){
  if (!expression) {
    throw toJs(new IllegalStateException);
  }
}

function checkCriticalStringBounds(start_0, end, length_0){
  if (start_0 < 0 || end > length_0 || end < start_0) {
    throw toJs(new StringIndexOutOfBoundsException('fromIndex: ' + start_0 + ', toIndex: ' + end + ', length: ' + length_0));
  }
}

function checkCriticalStringElementIndex(index_0, size_0){
  if (index_0 < 0 || index_0 >= size_0) {
    throw toJs(new StringIndexOutOfBoundsException('Index: ' + index_0 + ', Size: ' + size_0));
  }
}

function checkCriticalType(expression){
  if (!expression) {
    throw toJs(new ClassCastException);
  }
}

function toDoubleFromUnsignedInt(value_0){
  return value_0 >>> 0;
}

defineClass(481, 1, {});
var Ljava_lang_Object_2_classLit = createForClass('java.lang', 'Object', 1);
var Lcom_google_gwt_core_client_JavaScriptObject_2_classLit = createForClass('com.google.gwt.core.client', 'JavaScriptObject$', 0);
var Lcom_google_gwt_core_client_impl_StackTraceCreator$Collector_2_classLit = createForClass('com.google.gwt.core.client.impl', 'StackTraceCreator/Collector', 482);
var Lcom_google_gwt_core_client_impl_StackTraceCreator$CollectorLegacy_2_classLit = createForClass('com.google.gwt.core.client.impl', 'StackTraceCreator/CollectorLegacy', 257);
var Lcom_google_gwt_core_client_impl_StackTraceCreator$CollectorModern_2_classLit = createForClass('com.google.gwt.core.client.impl', 'StackTraceCreator/CollectorModern', 483);
var Lcom_google_gwt_core_client_impl_StackTraceCreator$CollectorModernNoSourceMap_2_classLit = createForClass('com.google.gwt.core.client.impl', 'StackTraceCreator/CollectorModernNoSourceMap', 258);
var Ljava_lang_Boolean_2_classLit = createForClass('java.lang', 'Boolean', 253);
var Ljava_lang_Class_2_classLit = createForClass('java.lang', 'Class', 187);
var Ljava_lang_Number_2_classLit = createForClass('java.lang', 'Number', 63);
var Ljava_lang_Double_2_classLit = createForClass('java.lang', 'Double', 191);
var Ljava_lang_Throwable_2_classLit = createForClass('java.lang', 'Throwable', 11);
var Ljava_lang_Exception_2_classLit = createForClass('java.lang', 'Exception', 93);
var Ljava_lang_RuntimeException_2_classLit = createForClass('java.lang', 'RuntimeException', 16);
var Ljava_lang_IndexOutOfBoundsException_2_classLit = createForClass('java.lang', 'IndexOutOfBoundsException', 97);
var Ljava_lang_JsException_2_classLit = createForClass('java.lang', 'JsException', 124);
var Ljava_lang_NullPointerException_2_classLit = createForClass('java.lang', 'NullPointerException', 62);
var Ljava_lang_String_2_classLit = createForClass('java.lang', 'String', 2);
var Ljava_lang_StringIndexOutOfBoundsException_2_classLit = createForClass('java.lang', 'StringIndexOutOfBoundsException', 194);
var Ljavaemul_internal_HashCodes_2_classLit = createForClass('javaemul.internal', 'HashCodes', 480);
var Ljavaemul_internal_JsUtils_2_classLit = createForClass('javaemul.internal', 'JsUtils', 481);
function $measureText(this$static, text_0){
  return this$static.measureText(text_0);
}

function setUncaughtExceptionHandler(handler){
  uncaughtExceptionHandler = handler;
  maybeInitializeWindowOnError();
}

var uncaughtExceptionHandler = null;
defineClass(260, 124, $intern_2);
var Lcom_google_gwt_core_client_impl_JavaScriptExceptionBase_2_classLit = createForClass('com.google.gwt.core.client.impl', 'JavaScriptExceptionBase', 260);
function $clinit_JavaScriptException(){
  $clinit_JavaScriptException = emptyMethod;
  NOT_SET = new Object_0;
}

function $ensureInit(this$static){
  var exception;
  if (this$static.message_0 == null) {
    exception = maskUndefined(this$static.e) === maskUndefined(NOT_SET)?null:this$static.e;
    this$static.name_0 = exception == null?'null':instanceOfJso(exception)?getExceptionName0(castToJso(exception)):instanceOfString(exception)?'String':$getName(getClass__Ljava_lang_Class___devirtual$(exception));
    this$static.description = this$static.description + ': ' + (instanceOfJso(exception)?getExceptionDescription0(castToJso(exception)):exception + '');
    this$static.message_0 = '(' + this$static.name_0 + ') ' + this$static.description;
  }
}

function JavaScriptException(e){
  $clinit_JavaScriptException();
  JsException.call(this, e);
  this.description = '';
  this.e = e;
  this.description = '';
}

function getExceptionDescription0(e){
  return e == null?null:e.message;
}

function getExceptionName0(e){
  return e == null?null:e.name;
}

defineClass(109, 260, {109:1, 3:1, 16:1, 11:1}, JavaScriptException);
_.getMessage = function getMessage_0(){
  $ensureInit(this);
  return this.message_0;
}
;
_.getThrown = function getThrown(){
  return maskUndefined(this.e) === maskUndefined(NOT_SET)?null:this.e;
}
;
var NOT_SET;
var Lcom_google_gwt_core_client_JavaScriptException_2_classLit = createForClass('com.google.gwt.core.client', 'JavaScriptException', 109);
defineClass(464, 1, {});
var Lcom_google_gwt_core_client_Scheduler_2_classLit = createForClass('com.google.gwt.core.client', 'Scheduler', 464);
function $clinit_Impl(){
  $clinit_Impl = emptyMethod;
  !!($clinit_StackTraceCreator() , collector_1);
}

function apply_0(jsFunction, thisObj, args){
  return jsFunction.apply(thisObj, args);
  var __0;
}

function enter(){
  var now_0;
  if (entryDepth != 0) {
    now_0 = Date.now();
    if (now_0 - watchdogEntryDepthLastScheduled > 2000) {
      watchdogEntryDepthLastScheduled = now_0;
      watchdogEntryDepthTimerId = $wnd.setTimeout(watchdogEntryDepthRun, 10);
    }
  }
  if (entryDepth++ == 0) {
    $flushEntryCommands(($clinit_SchedulerImpl() , INSTANCE));
    return true;
  }
  return false;
}

function entry_0(jsFunction){
  $clinit_Impl();
  return function(){
    return entry0_0(jsFunction, this, arguments);
    var __0;
  }
  ;
}

function entry0_0(jsFunction, thisObj, args){
  var initialEntry, t;
  initialEntry = enter();
  try {
    if (uncaughtExceptionHandler) {
      try {
        return apply_0(jsFunction, thisObj, args);
      }
       catch ($e0) {
        $e0 = toJava($e0);
        if (instanceOf($e0, 11)) {
          t = $e0;
          reportUncaughtException(t, true);
          return undefined;
        }
         else 
          throw toJs($e0);
      }
    }
     else {
      return apply_0(jsFunction, thisObj, args);
    }
  }
   finally {
    exit(initialEntry);
  }
}

function exit(initialEntry){
  initialEntry && $flushFinallyCommands(($clinit_SchedulerImpl() , INSTANCE));
  --entryDepth;
  if (initialEntry) {
    if (watchdogEntryDepthTimerId != -1) {
      watchdogEntryDepthCancel(watchdogEntryDepthTimerId);
      watchdogEntryDepthTimerId = -1;
    }
  }
}

function maybeInitializeWindowOnError(){
  $clinit_Impl();
  if (onErrorInitialized) {
    return;
  }
  onErrorInitialized = true;
  registerWindowOnError(false);
}

function registerWindowOnError(reportAlways){
  $clinit_Impl();
  function errorHandler(msg, url_0, line, column, error){
    var throwable = of(error);
    reportUncaughtException(throwable, false);
  }

  ;
  function addOnErrorHandler(windowRef){
    var origHandler = windowRef.onerror;
    if (origHandler && !reportAlways) {
      return;
    }
    windowRef.onerror = function(){
      errorHandler.apply(this, arguments);
      origHandler && origHandler.apply(this, arguments);
      return false;
    }
    ;
  }

  addOnErrorHandler($wnd);
  addOnErrorHandler(window);
}

function reportToBrowser(e){
  $wnd.setTimeout(function(){
    throw e;
  }
  , 0);
}

function reportUncaughtException(e, reportSwallowedExceptionToBrowser){
  $clinit_Impl();
  var handler;
  handler = uncaughtExceptionHandler;
  if (handler) {
    if (handler == uncaughtExceptionHandlerForTest) {
      return;
    }
    $log(handler.val$log2, ($clinit_Level() , e.getMessage()), e);
    return;
  }
  if (reportSwallowedExceptionToBrowser) {
    reportToBrowser(instanceOf(e, 109)?castTo(e, 109).getThrown():e);
  }
   else {
    $print(($clinit_System() , err), 'Uncaught exception ');
    $printStackTraceImpl(e, err, '', '');
  }
}

function watchdogEntryDepthCancel(timerId){
  $wnd.clearTimeout(timerId);
}

function watchdogEntryDepthRun(){
  entryDepth != 0 && (entryDepth = 0);
  watchdogEntryDepthTimerId = -1;
}

var entryDepth = 0, onErrorInitialized = false, uncaughtExceptionHandlerForTest, watchdogEntryDepthLastScheduled = 0, watchdogEntryDepthTimerId = -1;
function $clinit_SchedulerImpl(){
  $clinit_SchedulerImpl = emptyMethod;
  INSTANCE = new SchedulerImpl;
}

function $flushEntryCommands(this$static){
  var oldQueue, rescheduled;
  if (this$static.entryCommands) {
    rescheduled = null;
    do {
      oldQueue = this$static.entryCommands;
      this$static.entryCommands = null;
      rescheduled = runScheduledTasks(oldQueue, rescheduled);
    }
     while (this$static.entryCommands);
    this$static.entryCommands = rescheduled;
  }
}

function $flushFinallyCommands(this$static){
  var oldQueue, rescheduled;
  if (this$static.finallyCommands) {
    rescheduled = null;
    do {
      oldQueue = this$static.finallyCommands;
      this$static.finallyCommands = null;
      rescheduled = runScheduledTasks(oldQueue, rescheduled);
    }
     while (this$static.finallyCommands);
    this$static.finallyCommands = rescheduled;
  }
}

function SchedulerImpl(){
}

function push_0(queue, task){
  !queue && (queue = []);
  queue[queue.length] = task;
  return queue;
}

function runScheduledTasks(tasks, rescheduled){
  var e, i, j, t;
  for (i = 0 , j = tasks.length; i < j; i++) {
    t = tasks[i];
    try {
      t[1]?t[0].$_nullMethod() && (rescheduled = push_0(rescheduled, t)):t[0].$_nullMethod();
    }
     catch ($e0) {
      $e0 = toJava($e0);
      if (instanceOf($e0, 11)) {
        e = $e0;
        $clinit_Impl();
        reportUncaughtException(e, true);
      }
       else 
        throw toJs($e0);
    }
  }
  return rescheduled;
}

defineClass(274, 464, {}, SchedulerImpl);
var INSTANCE;
var Lcom_google_gwt_core_client_impl_SchedulerImpl_2_classLit = createForClass('com.google.gwt.core.client.impl', 'SchedulerImpl', 274);
function $appendChild(this$static, newChild){
  return this$static.appendChild(newChild);
}

function $removeChild(this$static, oldChild){
  return this$static.removeChild(oldChild);
}

function $setAttribute(this$static, name_0, value_0){
  this$static.setAttribute(name_0, value_0);
}

function $setId(this$static, id_0){
  this$static.id = id_0;
}

function $createScriptElement(doc, source){
  var elem;
  elem = doc.createElement('script');
  elem.text = source;
  return elem;
}

function $eventGetButton(evt){
  var button = evt.button;
  if (button == 1) {
    return 4;
  }
   else if (button == 2) {
    return 2;
  }
  return 1;
}

function $getDocumentScrollingElement(doc){
  if (doc.scrollingElement) {
    return doc.scrollingElement;
  }
  return $equals_0(doc.compatMode, 'CSS1Compat')?doc.documentElement:doc.body;
}

function $eventGetRelatedTarget(evt){
  var relatedTarget = evt.relatedTarget;
  if (!relatedTarget) {
    return null;
  }
  try {
    var nodeName = relatedTarget.nodeName;
    return relatedTarget;
  }
   catch (e) {
    return null;
  }
}

function $getAbsoluteLeftImpl(viewport, elem){
  if (Element.prototype.getBoundingClientRect) {
    return elem.getBoundingClientRect().left + viewport.scrollLeft | 0;
  }
   else {
    var doc = elem.ownerDocument;
    return doc.getBoxObjectFor(elem).screenX - doc.getBoxObjectFor(doc.documentElement).screenX;
  }
}

function $getAbsoluteTopImpl(viewport, elem){
  if (Element.prototype.getBoundingClientRect) {
    return elem.getBoundingClientRect().top + viewport.scrollTop | 0;
  }
   else {
    var doc = elem.ownerDocument;
    return doc.getBoxObjectFor(elem).screenY - doc.getBoxObjectFor(doc.documentElement).screenY;
  }
}

function $getScrollLeft(elem){
  var geckoVersion, style;
  geckoVersion = (cachedGeckoVersion == -2 && (cachedGeckoVersion = getNativeGeckoVersion()) , cachedGeckoVersion);
  if (!(geckoVersion != -1 && geckoVersion >= 1009000) && (style = elem.ownerDocument.defaultView.getComputedStyle(elem, null) , style.direction == 'rtl')) {
    return ((elem.scrollLeft || 0) | 0) - (((elem.scrollWidth || 0) | 0) - (elem.clientWidth | 0));
  }
  return (elem.scrollLeft || 0) | 0;
}

function $isOrHasChild(parent_0, child){
  return parent_0 === child || !!(parent_0.compareDocumentPosition(child) & 16);
}

function getNativeGeckoVersion(){
  var result = /rv:([0-9]+)\.([0-9]+)(\.([0-9]+))?.*?/.exec(navigator.userAgent.toLowerCase());
  if (result && result.length >= 3) {
    var version = parseInt(result[1]) * 1000000 + parseInt(result[2]) * 1000 + parseInt(result.length >= 5 && !isNaN(result[4])?result[4]:0);
    return version;
  }
  return -1;
}

var cachedGeckoVersion = -2;
function $setDropEffect(this$static, dropEffect){
  this$static.dropEffect && (this$static.dropEffect = dropEffect);
}

function $compareTo(this$static, other){
  return this$static.ordinal_0 - other.ordinal_0;
}

function $name(this$static){
  return this$static.name_0 != null?this$static.name_0:'' + this$static.ordinal_0;
}

function Enum(name_0, ordinal){
  this.name_0 = name_0;
  this.ordinal_0 = ordinal;
}

defineClass(29, 1, {3:1, 33:1, 29:1});
_.compareTo_0 = function compareTo_0(other){
  return $compareTo(this, castTo(other, 29));
}
;
_.compareTo = function compareTo(other){
  return this.ordinal_0 - other.ordinal_0;
}
;
_.equals = function equals_0(other){
  return this === other;
}
;
_.equals_0 = function(other){
  return this.equals(other);
}
;
_.hashCode = function hashCode_1(){
  return getObjectIdentityHashCode(this);
}
;
_.hashCode_0 = function(){
  return this.hashCode();
}
;
_.name = function name_2(){
  return $name(this);
}
;
_.ordinal = function ordinal_0(){
  return this.ordinal_0;
}
;
_.toString = function toString_3(){
  return this.name_0 != null?this.name_0:'' + this.ordinal_0;
}
;
_.toString_0 = function(){
  return this.toString();
}
;
_.ordinal_0 = 0;
var Ljava_lang_Enum_2_classLit = createForClass('java.lang', 'Enum', 29);
function $clinit_DataTransfer$DropEffect(){
  $clinit_DataTransfer$DropEffect = emptyMethod;
  COPY = new DataTransfer$DropEffect('COPY', 0);
  MOVE = new DataTransfer$DropEffect('MOVE', 1);
  LINK = new DataTransfer$DropEffect('LINK', 2);
  NONE = new DataTransfer$DropEffect('NONE', 3);
}

function DataTransfer$DropEffect(enum$name, enum$ordinal){
  Enum.call(this, enum$name, enum$ordinal);
}

function values_0(){
  $clinit_DataTransfer$DropEffect();
  return stampJavaTypeInfo(getClassLiteralForArray(Lcom_google_gwt_dom_client_DataTransfer$DropEffect_2_classLit, 1), $intern_0, 118, 0, [COPY, MOVE, LINK, NONE]);
}

defineClass(118, 29, {118:1, 3:1, 33:1, 29:1}, DataTransfer$DropEffect);
var COPY, LINK, MOVE, NONE;
var Lcom_google_gwt_dom_client_DataTransfer$DropEffect_2_classLit = createForEnum('com.google.gwt.dom.client', 'DataTransfer/DropEffect', 118, values_0);
function $createTextNode(this$static, data_0){
  return this$static.createTextNode(data_0);
}

function $getElementById(this$static, elementId){
  return this$static.getElementById(elementId);
}

function $getScrollLeft_0(this$static){
  var scrollingElement;
  return $getScrollLeft((scrollingElement = $getDocumentScrollingElement(this$static) , scrollingElement?scrollingElement:this$static.documentElement));
}

function $getScrollTop(this$static){
  var scrollingElement;
  return ((scrollingElement = $getDocumentScrollingElement(this$static) , scrollingElement?scrollingElement:this$static.documentElement).scrollTop || 0) | 0;
}

function $getViewportElement(this$static){
  return $equals_0(this$static.compatMode, 'CSS1Compat')?this$static.documentElement:this$static.body;
}

defineClass(494, 1, {});
_.toString_0 = function toString_4(){
  return 'An event type';
}
;
var Lcom_google_web_bindery_event_shared_Event_2_classLit = createForClass('com.google.web.bindery.event.shared', 'Event', 494);
defineClass(495, 494, {});
_.revive = function revive(){
}
;
var Lcom_google_gwt_event_shared_GwtEvent_2_classLit = createForClass('com.google.gwt.event.shared', 'GwtEvent', 495);
function $setNativeEvent(this$static, nativeEvent){
  this$static.nativeEvent = nativeEvent;
}

function $setRelativeElement(this$static, relativeElem){
  this$static.relativeElem = relativeElem;
}

function fireNativeEvent(nativeEvent, handlerSource, relativeElem){
  var currentNative, currentRelativeElem, type_0, type$iterator, types;
  if (registered) {
    types = castTo($unsafeGet(registered, nativeEvent.type), 13);
    if (types) {
      for (type$iterator = types.iterator(); type$iterator.hasNext_0();) {
        type_0 = castTo(type$iterator.next_1(), 87);
        currentNative = type_0.flyweight.nativeEvent;
        currentRelativeElem = type_0.flyweight.relativeElem;
        $setNativeEvent(type_0.flyweight, nativeEvent);
        $setRelativeElement(type_0.flyweight, relativeElem);
        handlerSource.fireEvent(type_0.flyweight);
        $setNativeEvent(type_0.flyweight, currentNative);
        $setRelativeElement(type_0.flyweight, currentRelativeElem);
      }
    }
  }
}

defineClass(496, 495, {});
_.getAssociatedType_0 = function getAssociatedType(){
  return this.getAssociatedType();
}
;
var registered;
var Lcom_google_gwt_event_dom_client_DomEvent_2_classLit = createForClass('com.google.gwt.event.dom.client', 'DomEvent', 496);
defineClass(498, 496, {});
var Lcom_google_gwt_event_dom_client_HumanInputEvent_2_classLit = createForClass('com.google.gwt.event.dom.client', 'HumanInputEvent', 498);
function $getRelativeX(this$static, target){
  var e;
  e = this$static.nativeEvent;
  return ((e.clientX || 0) | 0) - $getAbsoluteLeftImpl($getViewportElement(target.ownerDocument), target) + $getScrollLeft(target) + $getScrollLeft_0(target.ownerDocument);
}

function $getRelativeY(this$static, target){
  var e;
  e = this$static.nativeEvent;
  return ((e.clientY || 0) | 0) - $getAbsoluteTopImpl($getViewportElement(target.ownerDocument), target) + ((target.scrollTop || 0) | 0) + $getScrollTop(target.ownerDocument);
}

defineClass(499, 498, {});
var Lcom_google_gwt_event_dom_client_MouseEvent_2_classLit = createForClass('com.google.gwt.event.dom.client', 'MouseEvent', 499);
function $clinit_ClickEvent(){
  $clinit_ClickEvent = emptyMethod;
  TYPE = new DomEvent$Type('click', new ClickEvent);
}

function ClickEvent(){
}

defineClass(341, 499, {}, ClickEvent);
_.dispatch = function dispatch(handler){
  castTo(handler, 235).onClick(this);
}
;
_.getAssociatedType_0 = function getAssociatedType_1(){
  return TYPE;
}
;
_.getAssociatedType = function getAssociatedType_0(){
  return TYPE;
}
;
var TYPE;
var Lcom_google_gwt_event_dom_client_ClickEvent_2_classLit = createForClass('com.google.gwt.event.dom.client', 'ClickEvent', 341);
defineClass(294, 1, {});
_.hashCode_0 = function hashCode_2(){
  return this.index_0;
}
;
_.toString_0 = function toString_5(){
  return 'Event type';
}
;
_.index_0 = 0;
var nextHashCode = 0;
var Lcom_google_web_bindery_event_shared_Event$Type_2_classLit = createForClass('com.google.web.bindery.event.shared', 'Event/Type', 294);
defineClass(295, 294, {});
var Lcom_google_gwt_event_shared_GwtEvent$Type_2_classLit = createForClass('com.google.gwt.event.shared', 'GwtEvent/Type', 295);
function DomEvent$Type(eventName, flyweight){
  var types;
  this.index_0 = ++nextHashCode;
  this.flyweight = flyweight;
  !registered && (registered = new PrivateMap);
  types = castTo($unsafeGet(registered, eventName), 13);
  if (!types) {
    types = new ArrayList;
    $unsafePut(registered, eventName, types);
  }
  types.add(this);
  this.name_0 = eventName;
}

defineClass(87, 295, {87:1}, DomEvent$Type);
var Lcom_google_gwt_event_dom_client_DomEvent$Type_2_classLit = createForClass('com.google.gwt.event.dom.client', 'DomEvent/Type', 87);
defineClass(497, 496, {});
var Lcom_google_gwt_event_dom_client_DragDropEventBase_2_classLit = createForClass('com.google.gwt.event.dom.client', 'DragDropEventBase', 497);
function $clinit_DragOverEvent(){
  $clinit_DragOverEvent = emptyMethod;
  TYPE_0 = new DomEvent$Type('dragover', new DragOverEvent);
}

function DragOverEvent(){
}

defineClass(331, 497, {}, DragOverEvent);
_.dispatch = function dispatch_0(handler){
  throwClassCastExceptionUnlessNull(handler);
  null.$_nullMethod();
}
;
_.getAssociatedType_0 = function getAssociatedType_3(){
  return TYPE_0;
}
;
_.getAssociatedType = function getAssociatedType_2(){
  return TYPE_0;
}
;
var TYPE_0;
var Lcom_google_gwt_event_dom_client_DragOverEvent_2_classLit = createForClass('com.google.gwt.event.dom.client', 'DragOverEvent', 331);
function $clinit_DropEvent(){
  $clinit_DropEvent = emptyMethod;
  TYPE_1 = new DomEvent$Type('drop', new DropEvent);
}

function DropEvent(){
}

defineClass(332, 497, {}, DropEvent);
_.dispatch = function dispatch_1(handler){
  throwClassCastExceptionUnlessNull(handler);
  null.$_nullMethod();
}
;
_.getAssociatedType_0 = function getAssociatedType_5(){
  return TYPE_1;
}
;
_.getAssociatedType = function getAssociatedType_4(){
  return TYPE_1;
}
;
var TYPE_1;
var Lcom_google_gwt_event_dom_client_DropEvent_2_classLit = createForClass('com.google.gwt.event.dom.client', 'DropEvent', 332);
function $clinit_MouseDownEvent(){
  $clinit_MouseDownEvent = emptyMethod;
  TYPE_2 = new DomEvent$Type('mousedown', new MouseDownEvent);
}

function $dispatch(this$static, handler){
  $onMouseDown(handler, this$static);
}

function MouseDownEvent(){
}

defineClass(340, 499, {}, MouseDownEvent);
_.dispatch = function dispatch_2(handler){
  $dispatch(this, castTo(handler, 592));
}
;
_.getAssociatedType_0 = function getAssociatedType_7(){
  return TYPE_2;
}
;
_.getAssociatedType = function getAssociatedType_6(){
  return TYPE_2;
}
;
var TYPE_2;
var Lcom_google_gwt_event_dom_client_MouseDownEvent_2_classLit = createForClass('com.google.gwt.event.dom.client', 'MouseDownEvent', 340);
function $clinit_MouseMoveEvent(){
  $clinit_MouseMoveEvent = emptyMethod;
  TYPE_3 = new DomEvent$Type('mousemove', new MouseMoveEvent);
}

function $dispatch_0(this$static, handler){
  var x_0, y_0;
  x_0 = $getRelativeX(this$static, handler._svg.ot);
  y_0 = $getRelativeY(this$static, handler._svg.ot);
  !handler._dragElement && $wnd.Math.abs(x_0 - handler._dragStartX) + $wnd.Math.abs(y_0 - handler._dragStartY) > 10 && $startDrag(handler);
  !!handler._dragElement && $updateDrag(handler, x_0, y_0);
}

function MouseMoveEvent(){
}

defineClass(451, 499, {}, MouseMoveEvent);
_.dispatch = function dispatch_3(handler){
  $dispatch_0(this, castTo(handler, 593));
}
;
_.getAssociatedType_0 = function getAssociatedType_9(){
  return TYPE_3;
}
;
_.getAssociatedType = function getAssociatedType_8(){
  return TYPE_3;
}
;
var TYPE_3;
var Lcom_google_gwt_event_dom_client_MouseMoveEvent_2_classLit = createForClass('com.google.gwt.event.dom.client', 'MouseMoveEvent', 451);
function $clinit_MouseUpEvent(){
  $clinit_MouseUpEvent = emptyMethod;
  TYPE_4 = new DomEvent$Type('mouseup', new MouseUpEvent);
}

function MouseUpEvent(){
}

defineClass(452, 499, {}, MouseUpEvent);
_.dispatch = function dispatch_4(handler){
  $onMouseUp(castTo(handler, 594));
}
;
_.getAssociatedType_0 = function getAssociatedType_11(){
  return TYPE_4;
}
;
_.getAssociatedType = function getAssociatedType_10(){
  return TYPE_4;
}
;
var TYPE_4;
var Lcom_google_gwt_event_dom_client_MouseUpEvent_2_classLit = createForClass('com.google.gwt.event.dom.client', 'MouseUpEvent', 452);
function $unsafeGet(this$static, key){
  return this$static.map_0[key];
}

function $unsafePut(this$static, key, value_0){
  this$static.map_0[key] = value_0;
}

function PrivateMap(){
  this.map_0 = {};
}

defineClass(357, 1, {}, PrivateMap);
var Lcom_google_gwt_event_dom_client_PrivateMap_2_classLit = createForClass('com.google.gwt.event.dom.client', 'PrivateMap', 357);
defineClass(500, 1, {});
var Lcom_google_web_bindery_event_shared_EventBus_2_classLit = createForClass('com.google.web.bindery.event.shared', 'EventBus', 500);
defineClass(501, 500, {});
var Lcom_google_gwt_event_shared_EventBus_2_classLit = createForClass('com.google.gwt.event.shared', 'EventBus', 501);
function throwIfNull(value_0){
  if (null == value_0) {
    throw toJs(new NullPointerException_1('encodedURLComponent cannot be null'));
  }
}

function create_0(value_0){
  var a0, a1, a2;
  a0 = value_0 & $intern_7;
  a1 = value_0 >> 22 & $intern_7;
  a2 = value_0 < 0?$intern_8:0;
  return create0(a0, a1, a2);
}

function create_1(a){
  return create0(a.l, a.m, a.h);
}

function create0(l, m, h){
  return {l:l, m:m, h:h};
}

function divMod(a, b, computeRemainder){
  var aIsCopy, aIsMinValue, aIsNegative, bpower, c, negative;
  if (b.l == 0 && b.m == 0 && b.h == 0) {
    throw toJs(new ArithmeticException);
  }
  if (a.l == 0 && a.m == 0 && a.h == 0) {
    computeRemainder && (remainder = create0(0, 0, 0));
    return create0(0, 0, 0);
  }
  if (b.h == $intern_9 && b.m == 0 && b.l == 0) {
    return divModByMinValue(a, computeRemainder);
  }
  negative = false;
  if (b.h >> 19 != 0) {
    b = neg(b);
    negative = !negative;
  }
  bpower = powerOfTwo(b);
  aIsNegative = false;
  aIsMinValue = false;
  aIsCopy = false;
  if (a.h == $intern_9 && a.m == 0 && a.l == 0) {
    aIsMinValue = true;
    aIsNegative = true;
    if (bpower == -1) {
      a = create_1(($clinit_BigLongLib$Const() , MAX_VALUE));
      aIsCopy = true;
      negative = !negative;
    }
     else {
      c = shr(a, bpower);
      negative && negate(c);
      computeRemainder && (remainder = create0(0, 0, 0));
      return c;
    }
  }
   else if (a.h >> 19 != 0) {
    aIsNegative = true;
    a = neg(a);
    aIsCopy = true;
    negative = !negative;
  }
  if (bpower != -1) {
    return divModByShift(a, bpower, negative, aIsNegative, computeRemainder);
  }
  if (compare(a, b) < 0) {
    computeRemainder && (aIsNegative?(remainder = neg(a)):(remainder = create0(a.l, a.m, a.h)));
    return create0(0, 0, 0);
  }
  return divModHelper(aIsCopy?a:create0(a.l, a.m, a.h), b, negative, aIsNegative, aIsMinValue, computeRemainder);
}

function divModByMinValue(a, computeRemainder){
  if (a.h == $intern_9 && a.m == 0 && a.l == 0) {
    computeRemainder && (remainder = create0(0, 0, 0));
    return create_1(($clinit_BigLongLib$Const() , ONE));
  }
  computeRemainder && (remainder = create0(a.l, a.m, a.h));
  return create0(0, 0, 0);
}

function divModByShift(a, bpower, negative, aIsNegative, computeRemainder){
  var c;
  c = shr(a, bpower);
  negative && negate(c);
  if (computeRemainder) {
    a = maskRight(a, bpower);
    aIsNegative?(remainder = neg(a)):(remainder = create0(a.l, a.m, a.h));
  }
  return c;
}

function divModHelper(a, b, negative, aIsNegative, aIsMinValue, computeRemainder){
  var bshift, gte, quotient, shift_0, a1, a2, a0;
  shift_0 = numberOfLeadingZeros(b) - numberOfLeadingZeros(a);
  bshift = shl(b, shift_0);
  quotient = create0(0, 0, 0);
  while (shift_0 >= 0) {
    gte = trialSubtract(a, bshift);
    if (gte) {
      shift_0 < 22?(quotient.l |= 1 << shift_0 , undefined):shift_0 < 44?(quotient.m |= 1 << shift_0 - 22 , undefined):(quotient.h |= 1 << shift_0 - 44 , undefined);
      if (a.l == 0 && a.m == 0 && a.h == 0) {
        break;
      }
    }
    a1 = bshift.m;
    a2 = bshift.h;
    a0 = bshift.l;
    bshift.h = a2 >>> 1;
    bshift.m = a1 >>> 1 | (a2 & 1) << 21;
    bshift.l = a0 >>> 1 | (a1 & 1) << 21;
    --shift_0;
  }
  negative && negate(quotient);
  if (computeRemainder) {
    if (aIsNegative) {
      remainder = neg(a);
      aIsMinValue && (remainder = sub_0(remainder, ($clinit_BigLongLib$Const() , ONE)));
    }
     else {
      remainder = create0(a.l, a.m, a.h);
    }
  }
  return quotient;
}

function maskRight(a, bits){
  var b0, b1, b2;
  if (bits <= 22) {
    b0 = a.l & (1 << bits) - 1;
    b1 = b2 = 0;
  }
   else if (bits <= 44) {
    b0 = a.l;
    b1 = a.m & (1 << bits - 22) - 1;
    b2 = 0;
  }
   else {
    b0 = a.l;
    b1 = a.m;
    b2 = a.h & (1 << bits - 44) - 1;
  }
  return create0(b0, b1, b2);
}

function negate(a){
  var neg0, neg1, neg2;
  neg0 = ~a.l + 1 & $intern_7;
  neg1 = ~a.m + (neg0 == 0?1:0) & $intern_7;
  neg2 = ~a.h + (neg0 == 0 && neg1 == 0?1:0) & $intern_8;
  a.l = neg0;
  a.m = neg1;
  a.h = neg2;
}

function numberOfLeadingZeros(a){
  var b1, b2;
  b2 = numberOfLeadingZeros_0(a.h);
  if (b2 == 32) {
    b1 = numberOfLeadingZeros_0(a.m);
    return b1 == 32?numberOfLeadingZeros_0(a.l) + 32:b1 + 20 - 10;
  }
   else {
    return b2 - 12;
  }
}

function powerOfTwo(a){
  var h, l, m;
  l = a.l;
  if ((l & l - 1) != 0) {
    return -1;
  }
  m = a.m;
  if ((m & m - 1) != 0) {
    return -1;
  }
  h = a.h;
  if ((h & h - 1) != 0) {
    return -1;
  }
  if (h == 0 && m == 0 && l == 0) {
    return -1;
  }
  if (h == 0 && m == 0 && l != 0) {
    return numberOfTrailingZeros(l);
  }
  if (h == 0 && m != 0 && l == 0) {
    return numberOfTrailingZeros(m) + 22;
  }
  if (h != 0 && m == 0 && l == 0) {
    return numberOfTrailingZeros(h) + 44;
  }
  return -1;
}

function toDoubleHelper(a){
  return a.l + a.m * $intern_10 + a.h * $intern_11;
}

function trialSubtract(a, b){
  var sum0, sum1, sum2;
  sum2 = a.h - b.h;
  if (sum2 < 0) {
    return false;
  }
  sum0 = a.l - b.l;
  sum1 = a.m - b.m + (sum0 >> 22);
  sum2 += sum1 >> 22;
  if (sum2 < 0) {
    return false;
  }
  a.l = sum0 & $intern_7;
  a.m = sum1 & $intern_7;
  a.h = sum2 & $intern_8;
  return true;
}

var remainder;
function compare(a, b){
  var a0, a1, a2, b0, b1, b2, signA, signB;
  signA = a.h >> 19;
  signB = b.h >> 19;
  if (signA != signB) {
    return signB - signA;
  }
  a2 = a.h;
  b2 = b.h;
  if (a2 != b2) {
    return a2 - b2;
  }
  a1 = a.m;
  b1 = b.m;
  if (a1 != b1) {
    return a1 - b1;
  }
  a0 = a.l;
  b0 = b.l;
  return a0 - b0;
}

function fromDouble(value_0){
  var a0, a1, a2, negative, result;
  if (isNaN(value_0)) {
    return $clinit_BigLongLib$Const() , ZERO;
  }
  if (value_0 < -9223372036854775808) {
    return $clinit_BigLongLib$Const() , MIN_VALUE;
  }
  if (value_0 >= 9223372036854775807) {
    return $clinit_BigLongLib$Const() , MAX_VALUE;
  }
  negative = false;
  if (value_0 < 0) {
    negative = true;
    value_0 = -value_0;
  }
  a2 = 0;
  if (value_0 >= $intern_11) {
    a2 = round_int(value_0 / $intern_11);
    value_0 -= a2 * $intern_11;
  }
  a1 = 0;
  if (value_0 >= $intern_10) {
    a1 = round_int(value_0 / $intern_10);
    value_0 -= a1 * $intern_10;
  }
  a0 = round_int(value_0);
  result = create0(a0, a1, a2);
  negative && negate(result);
  return result;
}

function mul(a, b){
  var a0, a1, a2, a3, a4, b0, b1, b2, b3, b4, c0, c00, c01, c1, c10, c11, c12, c13, c2, c22, c23, c24, p0, p1, p2, p3, p4;
  a0 = a.l & 8191;
  a1 = a.l >> 13 | (a.m & 15) << 9;
  a2 = a.m >> 4 & 8191;
  a3 = a.m >> 17 | (a.h & 255) << 5;
  a4 = (a.h & 1048320) >> 8;
  b0 = b.l & 8191;
  b1 = b.l >> 13 | (b.m & 15) << 9;
  b2 = b.m >> 4 & 8191;
  b3 = b.m >> 17 | (b.h & 255) << 5;
  b4 = (b.h & 1048320) >> 8;
  p0 = a0 * b0;
  p1 = a1 * b0;
  p2 = a2 * b0;
  p3 = a3 * b0;
  p4 = a4 * b0;
  if (b1 != 0) {
    p1 += a0 * b1;
    p2 += a1 * b1;
    p3 += a2 * b1;
    p4 += a3 * b1;
  }
  if (b2 != 0) {
    p2 += a0 * b2;
    p3 += a1 * b2;
    p4 += a2 * b2;
  }
  if (b3 != 0) {
    p3 += a0 * b3;
    p4 += a1 * b3;
  }
  b4 != 0 && (p4 += a0 * b4);
  c00 = p0 & $intern_7;
  c01 = (p1 & 511) << 13;
  c0 = c00 + c01;
  c10 = p0 >> 22;
  c11 = p1 >> 9;
  c12 = (p2 & 262143) << 4;
  c13 = (p3 & 31) << 17;
  c1 = c10 + c11 + c12 + c13;
  c22 = p2 >> 18;
  c23 = p3 >> 5;
  c24 = (p4 & 4095) << 8;
  c2 = c22 + c23 + c24;
  c1 += c0 >> 22;
  c0 &= $intern_7;
  c2 += c1 >> 22;
  c1 &= $intern_7;
  c2 &= $intern_8;
  return create0(c0, c1, c2);
}

function neg(a){
  var neg0, neg1, neg2;
  neg0 = ~a.l + 1 & $intern_7;
  neg1 = ~a.m + (neg0 == 0?1:0) & $intern_7;
  neg2 = ~a.h + (neg0 == 0 && neg1 == 0?1:0) & $intern_8;
  return create0(neg0, neg1, neg2);
}

function shl(a, n){
  var res0, res1, res2;
  n &= 63;
  if (n < 22) {
    res0 = a.l << n;
    res1 = a.m << n | a.l >> 22 - n;
    res2 = a.h << n | a.m >> 22 - n;
  }
   else if (n < 44) {
    res0 = 0;
    res1 = a.l << n - 22;
    res2 = a.m << n - 22 | a.l >> 44 - n;
  }
   else {
    res0 = 0;
    res1 = 0;
    res2 = a.l << n - 44;
  }
  return create0(res0 & $intern_7, res1 & $intern_7, res2 & $intern_8);
}

function shr(a, n){
  var a2, negative, res0, res1, res2;
  n &= 63;
  a2 = a.h;
  negative = (a2 & $intern_9) != 0;
  negative && (a2 |= -1048576);
  if (n < 22) {
    res2 = a2 >> n;
    res1 = a.m >> n | a2 << 22 - n;
    res0 = a.l >> n | a.m << 22 - n;
  }
   else if (n < 44) {
    res2 = negative?$intern_8:0;
    res1 = a2 >> n - 22;
    res0 = a.m >> n - 22 | a2 << 44 - n;
  }
   else {
    res2 = negative?$intern_8:0;
    res1 = negative?$intern_7:0;
    res0 = a2 >> n - 44;
  }
  return create0(res0 & $intern_7, res1 & $intern_7, res2 & $intern_8);
}

function shru(a, n){
  var a2, res0, res1, res2;
  n &= 63;
  a2 = a.h & $intern_8;
  if (n < 22) {
    res2 = a2 >>> n;
    res1 = a.m >> n | a2 << 22 - n;
    res0 = a.l >> n | a.m << 22 - n;
  }
   else if (n < 44) {
    res2 = 0;
    res1 = a2 >>> n - 22;
    res0 = a.m >> n - 22 | a.h << 44 - n;
  }
   else {
    res2 = 0;
    res1 = 0;
    res0 = a2 >>> n - 44;
  }
  return create0(res0 & $intern_7, res1 & $intern_7, res2 & $intern_8);
}

function sub_0(a, b){
  var sum0, sum1, sum2;
  sum0 = a.l - b.l;
  sum1 = a.m - b.m + (sum0 >> 22);
  sum2 = a.h - b.h + (sum1 >> 22);
  return create0(sum0 & $intern_7, sum1 & $intern_7, sum2 & $intern_8);
}

function toDouble(a){
  if (compare(a, ($clinit_BigLongLib$Const() , ZERO)) < 0) {
    return -toDoubleHelper(neg(a));
  }
  return a.l + a.m * $intern_10 + a.h * $intern_11;
}

function toInt(a){
  return a.l | a.m << 22;
}

function toString_6(a){
  var digits, rem, res, tenPowerLong, zeroesNeeded;
  if (a.l == 0 && a.m == 0 && a.h == 0) {
    return '0';
  }
  if (a.h == $intern_9 && a.m == 0 && a.l == 0) {
    return '-9223372036854775808';
  }
  if (a.h >> 19 != 0) {
    return '-' + toString_6(neg(a));
  }
  rem = a;
  res = '';
  while (!(rem.l == 0 && rem.m == 0 && rem.h == 0)) {
    tenPowerLong = create_0(1000000000);
    rem = divMod(rem, tenPowerLong, true);
    digits = '' + toInt(remainder);
    if (!(rem.l == 0 && rem.m == 0 && rem.h == 0)) {
      zeroesNeeded = 9 - digits.length;
      for (; zeroesNeeded > 0; zeroesNeeded--) {
        digits = '0' + digits;
      }
    }
    res = digits + res;
  }
  return res;
}

function xor(a, b){
  return create0(a.l ^ b.l, a.m ^ b.m, a.h ^ b.h);
}

function $clinit_BigLongLib$Const(){
  $clinit_BigLongLib$Const = emptyMethod;
  MAX_VALUE = create0($intern_7, $intern_7, 524287);
  MIN_VALUE = create0(0, 0, $intern_9);
  ONE = create_0(1);
  create_0(2);
  ZERO = create_0(0);
}

var MAX_VALUE, MIN_VALUE, ONE, ZERO;
function compare_0(a, b){
  var result;
  if (isSmallLong0(a) && isSmallLong0(b)) {
    result = a - b;
    if (!isNaN(result)) {
      return result;
    }
  }
  return compare(isSmallLong0(a)?toBigLong(a):a, isSmallLong0(b)?toBigLong(b):b);
}

function createLongEmul(big_0){
  var a2;
  a2 = big_0.h;
  if (a2 == 0) {
    return big_0.l + big_0.m * $intern_10;
  }
  if (a2 == $intern_8) {
    return big_0.l + big_0.m * $intern_10 - $intern_11;
  }
  return big_0;
}

function eq(a, b){
  return compare_0(a, b) == 0;
}

function fromDouble_0(value_0){
  if ($intern_12 < value_0 && value_0 < $intern_11) {
    return value_0 < 0?$wnd.Math.ceil(value_0):$wnd.Math.floor(value_0);
  }
  return createLongEmul(fromDouble(value_0));
}

function gt(a, b){
  return compare_0(a, b) > 0;
}

function isSmallLong0(value_0){
  return typeof value_0 === 'number';
}

function mul_0(a, b){
  var result;
  if (isSmallLong0(a) && isSmallLong0(b)) {
    result = a * b;
    if ($intern_12 < result && result < $intern_11) {
      return result;
    }
  }
  return createLongEmul(mul(isSmallLong0(a)?toBigLong(a):a, isSmallLong0(b)?toBigLong(b):b));
}

function neg_0(a){
  var result;
  if (isSmallLong0(a)) {
    result = 0 - a;
    if (!isNaN(result)) {
      return result;
    }
  }
  return createLongEmul(neg(a));
}

function neq(a, b){
  return compare_0(a, b) != 0;
}

function sub_1(a, b){
  var result;
  if (isSmallLong0(a) && isSmallLong0(b)) {
    result = a - b;
    if ($intern_12 < result && result < $intern_11) {
      return result;
    }
  }
  return createLongEmul(sub_0(isSmallLong0(a)?toBigLong(a):a, isSmallLong0(b)?toBigLong(b):b));
}

function toBigLong(longValue){
  var a0, a1, a3, value_0;
  value_0 = longValue;
  a3 = 0;
  if (value_0 < 0) {
    value_0 += $intern_11;
    a3 = $intern_8;
  }
  a1 = round_int(value_0 / $intern_10);
  a0 = round_int(value_0 - a1 * $intern_10);
  return create0(a0, a1, a3);
}

function toDouble_0(a){
  var d;
  if (isSmallLong0(a)) {
    d = a;
    return d == -0.?0:d;
  }
  return toDouble(a);
}

function toInt_0(a){
  if (isSmallLong0(a)) {
    return a | 0;
  }
  return toInt(a);
}

function toString_7(a){
  if (isSmallLong0(a)) {
    return '' + a;
  }
  return toString_6(a);
}

function xor_0(a, b){
  return createLongEmul(xor(isSmallLong0(a)?toBigLong(a):a, isSmallLong0(b)?toBigLong(b):b));
}

function init(){
  $wnd.setTimeout($entry(assertCompileTimeUserAgent));
  $onModuleLoad_0();
  $clinit_LogConfiguration();
  $onModuleLoad();
  registerFactory('diagramJSgraph', new ModuleEntry$lambda$0$Type);
  registerFactory('blocksControl', new ModuleEntry$lambda$0$Type_0);
  registerFactory('diagramControl', new ModuleEntry$lambda$1$Type);
}

function $getLevel(this$static){
  if (this$static.level) {
    return this$static.level;
  }
  return $clinit_Level() , ALL;
}

function $setFormatter(this$static, newFormatter){
  this$static.formatter = newFormatter;
}

function $setLevel(this$static, newLevel){
  this$static.level = newLevel;
}

defineClass(126, 1, {126:1});
var Ljava_util_logging_Handler_2_classLit = createForClass('java.util.logging', 'Handler', 126);
function ConsoleLogHandler(){
  $setFormatter(this, new TextLogFormatter(true));
  $setLevel(this, ($clinit_Level() , ALL));
}

defineClass(248, 126, {126:1}, ConsoleLogHandler);
_.publish = function publish(record){
  var msg;
  if (!window.console || ($getLevel(this) , false)) {
    return;
  }
  msg = $format(this.formatter, record);
  $clinit_Level();
  window.console.error(msg);
}
;
var Lcom_google_gwt_logging_client_ConsoleLogHandler_2_classLit = createForClass('com.google.gwt.logging.client', 'ConsoleLogHandler', 248);
function DevelopmentModeLogHandler(){
  $setFormatter(this, new TextLogFormatter(false));
  $setLevel(this, ($clinit_Level() , ALL));
}

defineClass(249, 126, {126:1}, DevelopmentModeLogHandler);
_.publish = function publish_0(record){
  return;
}
;
var Lcom_google_gwt_logging_client_DevelopmentModeLogHandler_2_classLit = createForClass('com.google.gwt.logging.client', 'DevelopmentModeLogHandler', 249);
function $clinit_LogConfiguration(){
  $clinit_LogConfiguration = emptyMethod;
  impl_0 = new LogConfiguration$LogConfigurationImplSevere;
}

function $onModuleLoad(){
  var log_0;
  $configureClientSideLogging(impl_0);
  if (!uncaughtExceptionHandler) {
    log_0 = getLogger(($ensureNamesAreInitialized(Lcom_google_gwt_logging_client_LogConfiguration_2_classLit) , Lcom_google_gwt_logging_client_LogConfiguration_2_classLit.typeName));
    setUncaughtExceptionHandler(new LogConfiguration$1(log_0));
  }
}

var impl_0;
var Lcom_google_gwt_logging_client_LogConfiguration_2_classLit = createForClass('com.google.gwt.logging.client', 'LogConfiguration', null);
function LogConfiguration$1(val$log){
  this.val$log2 = val$log;
}

defineClass(247, 1, {}, LogConfiguration$1);
var Lcom_google_gwt_logging_client_LogConfiguration$1_2_classLit = createForClass('com.google.gwt.logging.client', 'LogConfiguration/1', 247);
function $configureClientSideLogging(this$static){
  this$static.root_0 = getLogger('');
  $setUseParentHandlers(this$static.root_0);
  $setLevels(this$static.root_0);
  $setDefaultHandlers(this$static.root_0);
}

function $setDefaultHandlers(l){
  var console_0, dev;
  console_0 = new ConsoleLogHandler;
  $addHandler(l, console_0);
  dev = new DevelopmentModeLogHandler;
  $addHandler(l, dev);
}

function $setLevels(l){
  var level, levelParam, paramsForName;
  levelParam = (ensureListParameterMap() , paramsForName = castTo(listParamMap.get('logLevel'), 13) , !paramsForName?null:castToString(paramsForName.getAtIndex(paramsForName.size() - 1)));
  level = levelParam == null?null:parse_0(levelParam);
  level?$setLevel_0(l, level):$setLevel_0(l, ($clinit_Level() , INFO));
}

defineClass(468, 1, {});
var Lcom_google_gwt_logging_client_LogConfiguration$LogConfigurationImplRegular_2_classLit = createForClass('com.google.gwt.logging.client', 'LogConfiguration/LogConfigurationImplRegular', 468);
function LogConfiguration$LogConfigurationImplSevere(){
}

defineClass(246, 468, {}, LogConfiguration$LogConfigurationImplSevere);
var Lcom_google_gwt_logging_client_LogConfiguration$LogConfigurationImplSevere_2_classLit = createForClass('com.google.gwt.logging.client', 'LogConfiguration/LogConfigurationImplSevere', 246);
defineClass(490, 1, {});
var Ljava_util_logging_Formatter_2_classLit = createForClass('java.util.logging', 'Formatter', 490);
defineClass(491, 490, {});
var Lcom_google_gwt_logging_impl_FormatterImpl_2_classLit = createForClass('com.google.gwt.logging.impl', 'FormatterImpl', 491);
function $format(this$static, event_0){
  var message, date, s;
  message = new StringBuilder;
  $append_7(message, (date = new Date_0(event_0.millis) , s = new StringBuilder , $append_7(s, $toString_1(date)) , s.string += ' ' , $append_7(s, event_0.loggerName) , s.string += '\n' , s.string += 'SEVERE' , s.string += ': ' , s.string));
  $append_7(message, event_0.msg);
  if (this$static.showStackTraces && !!event_0.thrown) {
    message.string += '\n';
    $printStackTraceImpl(event_0.thrown, new StackTracePrintStream(message), '', '');
  }
  return message.string;
}

function TextLogFormatter(showStackTraces){
  this.showStackTraces = showStackTraces;
}

defineClass(203, 491, {}, TextLogFormatter);
_.showStackTraces = false;
var Lcom_google_gwt_logging_client_TextLogFormatter_2_classLit = createForClass('com.google.gwt.logging.client', 'TextLogFormatter', 203);
function $write(this$static, buffer){
  checkCriticalNotNull(buffer);
  $write_2(this$static, buffer, buffer.length);
}

defineClass(467, 1, {});
_.close_0 = function close_0(){
}
;
var Ljava_io_OutputStream_2_classLit = createForClass('java.io', 'OutputStream', 467);
function $close(this$static){
  var e, thrown;
  thrown = null;
  try {
    this$static.flush_0();
  }
   catch ($e0) {
    $e0 = toJava($e0);
    if (instanceOf($e0, 11)) {
      e = $e0;
      thrown = e;
    }
     else 
      throw toJs($e0);
  }
  try {
    $close(this$static.out);
  }
   catch ($e1) {
    $e1 = toJava($e1);
    if (instanceOf($e1, 11)) {
      e = $e1;
      !thrown && (thrown = e);
    }
     else 
      throw toJs($e1);
  }
  if (thrown) {
    throw toJs(new IOException_0(thrown));
  }
}

function $flush(this$static){
  $flush(this$static.out);
}

function $write_0(this$static, oneByte){
  $write_0(this$static.out, oneByte);
}

function $write_1(this$static, buffer, length_0){
  var i;
  checkCriticalNotNull(buffer);
  checkOffsetAndCount(buffer.length, length_0);
  for (i = 0; i < length_0; i++) {
    $write_0(this$static, buffer[i]);
  }
}

function FilterOutputStream(out){
  this.out = out;
}

defineClass(189, 467, {}, FilterOutputStream);
_.close_0 = function close_1(){
  $close(this);
}
;
_.flush_0 = function flush(){
  $flush(this);
}
;
var Ljava_io_FilterOutputStream_2_classLit = createForClass('java.io', 'FilterOutputStream', 189);
function $flush_0(this$static){
  if (this$static.out) {
    try {
      $flush(this$static.out);
      return;
    }
     catch ($e0) {
      $e0 = toJava($e0);
      if (!instanceOf($e0, 32))
        throw toJs($e0);
    }
  }
}

function $print(this$static, s){
  var lastArg;
  if (!this$static.out) {
    return;
  }
  if (s == null) {
    this$static.print_0('null');
    return;
  }
  try {
    $write(this$static, $getBytes((lastArg = s , $clinit_EmulatedCharset() , lastArg)));
  }
   catch ($e0) {
    $e0 = toJava($e0);
    if (!instanceOf($e0, 32))
      throw toJs($e0);
  }
}

function $write_2(this$static, buffer, length_0){
  checkCriticalNotNull(buffer);
  checkOffsetAndCount(buffer.length, length_0);
  if (!this$static.out) {
    return;
  }
  try {
    $write_1(this$static.out, buffer, length_0);
  }
   catch ($e0) {
    $e0 = toJava($e0);
    if (!instanceOf($e0, 32))
      throw toJs($e0);
  }
}

function PrintStream(out){
  FilterOutputStream.call(this, out);
}

defineClass(162, 189, {}, PrintStream);
_.close_0 = function close_2(){
  $flush_0(this);
  if (this.out) {
    try {
      $close(this.out);
    }
     catch ($e0) {
      $e0 = toJava($e0);
      if (!instanceOf($e0, 32))
        throw toJs($e0);
    }
     finally {
      this.out = null;
    }
  }
}
;
_.flush_0 = function flush_0(){
  $flush_0(this);
}
;
_.print_0 = function print_0(s){
  $print(this, s);
}
;
_.println = function println(s){
  this.print_0(s);
  this.print_0(String.fromCharCode(10));
}
;
var Ljava_io_PrintStream_2_classLit = createForClass('java.io', 'PrintStream', 162);
function StackTracePrintStream(builder){
  PrintStream.call(this, new FilterOutputStream(null));
  this.builder = builder;
}

defineClass(309, 162, {}, StackTracePrintStream);
_.print_0 = function print_1(str){
  $append_7(this.builder, str);
}
;
_.println = function println_0(str){
  $append_7(this.builder, str);
  $append_7(this.builder, '\n');
}
;
var Lcom_google_gwt_logging_impl_StackTracePrintStream_2_classLit = createForClass('com.google.gwt.logging.impl', 'StackTracePrintStream', 309);
function $clinit_DOM(){
  $clinit_DOM = emptyMethod;
  $clinit_DOMImplMozilla();
}

function dispatchEvent_0(evt, elem, listener){
  $clinit_DOM();
  var prevCurrentEvent;
  prevCurrentEvent = currentEvent;
  currentEvent = evt;
  elem == sCaptureElem && $eventGetTypeInt(evt.type) == 8192 && (sCaptureElem = null);
  listener.onBrowserEvent(evt);
  currentEvent = prevCurrentEvent;
}

var currentEvent = null, sCaptureElem;
function $onModuleLoad_0(){
  var allowedModes, currentMode, i;
  currentMode = $doc.compatMode;
  allowedModes = stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['CSS1Compat']);
  for (i = 0; i < allowedModes.length; i++) {
    if ($equals_0(allowedModes[i], currentMode)) {
      return;
    }
  }
  allowedModes.length == 1 && $equals_0('CSS1Compat', allowedModes[0]) && $equals_0('BackCompat', currentMode)?"GWT no longer supports Quirks Mode (document.compatMode=' BackCompat').<br>Make sure your application's host HTML page has a Standards Mode (document.compatMode=' CSS1Compat') doctype,<br>e.g. by using &lt;!doctype html&gt; at the start of your application's HTML page.<br><br>To continue using this unsupported rendering mode and risk layout problems, suppress this message by adding<br>the following line to your*.gwt.xml module file:<br>&nbsp;&nbsp;&lt;extend-configuration-property name=\"document.compatMode\" value=\"" + currentMode + '"/&gt;':"Your *.gwt.xml module configuration prohibits the use of the current document rendering mode (document.compatMode=' " + currentMode + "').<br>Modify your application's host HTML page doctype, or update your custom " + "'document.compatMode' configuration property settings.";
}

function setEventListener(elem, listener){
  $clinit_DOM();
  elem.__listener = listener;
}

function $cancel(this$static){
  if (!this$static.timerId) {
    return;
  }
  ++this$static.cancelCounter;
  this$static.isRepeating?clearInterval_0(this$static.timerId.value_0):clearTimeout_0(this$static.timerId.value_0);
  this$static.timerId = null;
}

function $schedule(this$static, delayMillis){
  if (delayMillis < 0) {
    throw toJs(new IllegalArgumentException('must be non-negative'));
  }
  !!this$static.timerId && $cancel(this$static);
  this$static.isRepeating = false;
  this$static.timerId = valueOf(setTimeout_0(createCallback(this$static, this$static.cancelCounter), delayMillis));
}

function Timer(){
}

function clearInterval_0(timerId){
  $wnd.clearInterval(timerId);
}

function clearTimeout_0(timerId){
  $wnd.clearTimeout(timerId);
}

function createCallback(timer, cancelCounter){
  return $entry(function(){
    timer.fire(cancelCounter);
  }
  );
}

function setTimeout_0(func, time){
  return $wnd.setTimeout(func, time);
}

defineClass(204, 1, {});
_.fire = function fire(scheduleCancelCounter){
  if (scheduleCancelCounter != this.cancelCounter) {
    return;
  }
  this.isRepeating || (this.timerId = null);
  this.run();
}
;
_.cancelCounter = 0;
_.isRepeating = false;
_.timerId = null;
var Lcom_google_gwt_user_client_Timer_2_classLit = createForClass('com.google.gwt.user.client', 'Timer', 204);
function buildListParamMap(queryString){
  var entry, entry$iterator, key, kv, kvPair, kvPair$array, kvPair$index, kvPair$max, out, qs, val, values, regexp;
  out = new HashMap;
  if (queryString != null && queryString.length > 1) {
    qs = (checkCriticalStringElementIndex(1, queryString.length + 1) , queryString.substr(1));
    for (kvPair$array = $split(qs, '&', 0) , kvPair$index = 0 , kvPair$max = kvPair$array.length; kvPair$index < kvPair$max; ++kvPair$index) {
      kvPair = kvPair$array[kvPair$index];
      kv = $split(kvPair, '=', 2);
      key = kv[0];
      if (key.length == 0) {
        continue;
      }
      val = kv.length > 1?kv[1]:'';
      try {
        val = (throwIfNull(val) , regexp = /\+/g , decodeURIComponent(val.replace(regexp, '%20')));
      }
       catch ($e0) {
        $e0 = toJava($e0);
        if (!instanceOf($e0, 109))
          throw toJs($e0);
      }
      values = castTo(out.get(key), 13);
      if (!values) {
        values = new ArrayList;
        out.put(key, values);
      }
      values.add(val);
    }
  }
  for (entry$iterator = out.entrySet_0().iterator(); entry$iterator.hasNext_0();) {
    entry = castTo(entry$iterator.next_1(), 22);
    entry.setValue(unmodifiableList(castTo(entry.getValue(), 13)));
  }
  out = ($clinit_Collections() , new Collections$UnmodifiableMap(out));
  return out;
}

function ensureListParameterMap(){
  var currentQueryString;
  currentQueryString = $wnd.location.search;
  if (!listParamMap || !$equals_0(cachedQueryString, currentQueryString)) {
    listParamMap = buildListParamMap(currentQueryString);
    cachedQueryString = currentQueryString;
  }
}

var cachedQueryString = '', listParamMap;
function $eventGetTypeInt(eventType){
  switch (eventType) {
    case 'blur':
      return 4096;
    case 'change':
      return 1024;
    case 'click':
      return 1;
    case 'dblclick':
      return 2;
    case 'focus':
      return 2048;
    case 'keydown':
      return 128;
    case 'keypress':
      return 256;
    case 'keyup':
      return 512;
    case 'load':
      return 32768;
    case 'losecapture':
      return 8192;
    case 'mousedown':
      return 4;
    case 'mousemove':
      return 64;
    case 'mouseout':
      return 32;
    case 'mouseover':
      return 16;
    case 'mouseup':
      return 8;
    case 'scroll':
      return 16384;
    case 'error':
      return $intern_5;
    case 'DOMMouseScroll':
    case 'mousewheel':
      return 131072;
    case 'contextmenu':
      return 262144;
    case 'paste':
      return $intern_9;
    case 'touchstart':
      return 1048576;
    case 'touchmove':
      return 2097152;
    case 'touchend':
      return $intern_10;
    case 'touchcancel':
      return 8388608;
    case 'gesturestart':
      return 16777216;
    case 'gesturechange':
      return 33554432;
    case 'gestureend':
      return 67108864;
    default:return -1;
  }
}

function $maybeInitializeEventSystem(){
  if (!eventSystemIsInitialized) {
    $initEventSystem();
    $initSyntheticMouseUpEvents();
    eventSystemIsInitialized = true;
  }
}

function getEventListener(elem){
  var maybeListener = elem.__listener;
  return !instanceOfJso(maybeListener) && instanceOf(maybeListener, 105)?maybeListener:null;
}

var eventSystemIsInitialized = false;
function $clinit_DOMImplStandard(){
  $clinit_DOMImplStandard = emptyMethod;
  bitlessEventDispatchers = {_default_:dispatchEvent_2, dragenter:dispatchDragEvent, dragover:dispatchDragEvent};
  captureEventDispatchers = {click:dispatchCapturedMouseEvent, dblclick:dispatchCapturedMouseEvent, mousedown:dispatchCapturedMouseEvent, mouseup:dispatchCapturedMouseEvent, mousemove:dispatchCapturedMouseEvent, mouseover:dispatchCapturedMouseEvent, mouseout:dispatchCapturedMouseEvent, mousewheel:dispatchCapturedMouseEvent, keydown:dispatchCapturedEvent, keyup:dispatchCapturedEvent, keypress:dispatchCapturedEvent, touchstart:dispatchCapturedMouseEvent, touchend:dispatchCapturedMouseEvent, touchmove:dispatchCapturedMouseEvent, touchcancel:dispatchCapturedMouseEvent, gesturestart:dispatchCapturedMouseEvent, gestureend:dispatchCapturedMouseEvent, gesturechange:dispatchCapturedMouseEvent};
}

function $initEventSystem(){
  dispatchEvent_1 = $entry(dispatchEvent_2);
  dispatchUnhandledEvent = $entry(dispatchUnhandledEvent_0);
  var foreach = foreach_0;
  var bitlessEvents = bitlessEventDispatchers;
  foreach(bitlessEvents, function(e, fn){
    bitlessEvents[e] = $entry(fn);
  }
  );
  var captureEvents_0 = captureEventDispatchers;
  foreach(captureEvents_0, function(e, fn){
    captureEvents_0[e] = $entry(fn);
  }
  );
  foreach(captureEvents_0, function(e, fn){
    $wnd.addEventListener(e, fn, true);
  }
  );
}

function dispatchCapturedEvent(evt){
  $clinit_DOM();
}

function dispatchCapturedMouseEvent(evt){
  $clinit_DOMImplStandard();
  $clinit_DOM();
  return;
}

function dispatchDragEvent(evt){
  evt.preventDefault();
  dispatchEvent_2(evt);
}

function dispatchEvent_2(evt){
  var element;
  element = getFirstAncestorWithListener(evt);
  if (!element) {
    return;
  }
  dispatchEvent_0(evt, element.nodeType != 1?null:element, getEventListener(element));
}

function dispatchUnhandledEvent_0(evt){
  var element;
  element = evt.currentTarget;
  element['__gwtLastUnhandledEvent'] = evt.type;
  dispatchEvent_2(evt);
}

function getFirstAncestorWithListener(evt){
  var curElem;
  curElem = evt.currentTarget;
  while (!!curElem && !getEventListener(curElem)) {
    curElem = curElem.parentNode;
  }
  return curElem;
}

var bitlessEventDispatchers, captureElem, captureEventDispatchers, dispatchEvent_1, dispatchUnhandledEvent;
function $clinit_DOMImplMozilla(){
  $clinit_DOMImplMozilla = emptyMethod;
  $clinit_DOMImplStandard();
  captureEventDispatchers['DOMMouseScroll'] = dispatchCapturedMouseEvent;
}

function $initSyntheticMouseUpEvents(){
  $wnd.addEventListener('mouseout', $entry(function(evt){
    var cap = ($clinit_DOMImplStandard() , captureElem);
    if (cap && !evt.relatedTarget) {
      if ('html' == evt.target.tagName.toLowerCase()) {
        var muEvent = $doc.createEvent('MouseEvents');
        muEvent.initMouseEvent('mouseup', true, true, $wnd, 0, evt.screenX, evt.screenY, evt.clientX, evt.clientY, evt.ctrlKey, evt.altKey, evt.shiftKey, evt.metaKey, evt.button, null);
        cap.dispatchEvent(muEvent);
      }
    }
  }
  ), true);
}

function foreach_0(map_0, fn){
  for (var e in map_0) {
    map_0.hasOwnProperty(e) && fn(e, map_0[e]);
  }
}

function assertCompileTimeUserAgent(){
  var runtimeValue;
  runtimeValue = $getRuntimeValue();
  if (!$equals_0('gecko1_8', runtimeValue)) {
    throw toJs(new UserAgentAsserter$UserAgentAssertionError(runtimeValue));
  }
}

function Error_0(message, cause){
  Throwable_0.call(this, message, cause);
}

defineClass(82, 11, $intern_13);
var Ljava_lang_Error_2_classLit = createForClass('java.lang', 'Error', 82);
function AssertionError(){
  Throwable.call(this);
}

defineClass(40, 82, $intern_13, AssertionError);
var Ljava_lang_AssertionError_2_classLit = createForClass('java.lang', 'AssertionError', 40);
function UserAgentAsserter$UserAgentAssertionError(runtimeValue){
  Error_0.call(this, 'Possible problem with your *.gwt.xml module file.\nThe compile time user.agent value (gecko1_8) does not match the runtime user.agent value (' + runtimeValue + ').\n' + 'Expect more errors.' == null?'null':toString_8('Possible problem with your *.gwt.xml module file.\nThe compile time user.agent value (gecko1_8) does not match the runtime user.agent value (' + runtimeValue + ').\n' + 'Expect more errors.'), instanceOf('Possible problem with your *.gwt.xml module file.\nThe compile time user.agent value (gecko1_8) does not match the runtime user.agent value (' + runtimeValue + ').\n' + 'Expect more errors.', 11)?castTo('Possible problem with your *.gwt.xml module file.\nThe compile time user.agent value (gecko1_8) does not match the runtime user.agent value (' + runtimeValue + ').\n' + 'Expect more errors.', 11):null);
}

defineClass(240, 40, $intern_13, UserAgentAsserter$UserAgentAssertionError);
var Lcom_google_gwt_useragent_client_UserAgentAsserter$UserAgentAssertionError_2_classLit = createForClass('com.google.gwt.useragent.client', 'UserAgentAsserter/UserAgentAssertionError', 240);
function $getRuntimeValue(){
  var ua = navigator.userAgent.toLowerCase();
  var docMode = $doc.documentMode;
  if (function(){
    return ua.indexOf('webkit') != -1;
  }
  ())
    return 'safari';
  if (function(){
    return ua.indexOf('gecko') != -1 || docMode >= 11;
  }
  ())
    return 'gecko1_8';
  return 'unknown';
}

function UmbrellaException(causes){
  var cause, cause$iterator, entry, entry0, i, outerIter, outerIter0;
  RuntimeException_1.call(this, makeMessage(causes), causes.map_0.size() == 0?null:(outerIter0 = (new AbstractMap$1(causes.map_0)).this$01.entrySet_0().iterator() , entry0 = castTo((new AbstractMap$1$1(outerIter0)).val$outerIter2.next_1(), 22) , castTo(entry0.getKey(), 11)));
  i = 0;
  for (cause$iterator = (outerIter = (new AbstractMap$1(causes.map_0)).this$01.entrySet_0().iterator() , new AbstractMap$1$1(outerIter)); cause$iterator.val$outerIter2.hasNext_0();) {
    cause = (entry = castTo(cause$iterator.val$outerIter2.next_1(), 22) , castTo(entry.getKey(), 11));
    if (i++ == 0) {
      continue;
    }
    $addSuppressed(this, cause);
  }
}

function makeMessage(causes){
  var b, count, entry, first, outerIter, t, t$iterator;
  count = causes.map_0.size();
  if (count == 0) {
    return null;
  }
  b = new StringBuilder_1(count == 1?'Exception caught: ':count + ' exceptions caught: ');
  first = true;
  for (t$iterator = (outerIter = (new AbstractMap$1(causes.map_0)).this$01.entrySet_0().iterator() , new AbstractMap$1$1(outerIter)); t$iterator.val$outerIter2.hasNext_0();) {
    t = (entry = castTo(t$iterator.val$outerIter2.next_1(), 22) , castTo(entry.getKey(), 11));
    first?(first = false):(b.string += '; ' , b);
    $append_7(b, t.getMessage());
  }
  return b.string;
}

defineClass(419, 16, $intern_2, UmbrellaException);
var Lcom_google_web_bindery_event_shared_UmbrellaException_2_classLit = createForClass('com.google.web.bindery.event.shared', 'UmbrellaException', 419);
function $setControlID(this$static, controlID){
  return this$static.controlID = controlID;
}

function $invoke(command){
  throw toJs(new IllegalArgumentException("Command '" + command + "' not supported."));
}

function $sendCommand(this$static, command, arguments_0){
  $setControlID(arguments_0, this$static._id);
  arguments_0.controlCommand = command;
  $wnd.services.ajax.execute('dispatchControlCommand', arguments_0, true);
}

function getElement(id_0){
  return $getElementById($doc, id_0);
}

defineClass(169, 1, {105:1, 183:1});
_.invoke_0 = function invoke(command, args){
  $invoke(command);
}
;
_.fireEvent = function fireEvent(event_0){
}
;
_.init_0 = function init_0(args){
  $clinit_DOM();
  getElement(this._id).__listener = this , undefined;
}
;
_.onBrowserEvent = function onBrowserEvent(event_0){
  var related;
  switch ($clinit_DOM() , $eventGetTypeInt(event_0.type)) {
    case 16:
    case 32:
      related = $eventGetRelatedTarget(event_0);
      if (!!related && $isOrHasChild(getElement(this._id), related)) {
        return;
      }

  }
  fireNativeEvent(event_0, this, getElement(this._id));
}
;
var Lcom_top_1logic_ajax_client_control_AbstractJSControl_2_classLit = createForClass('com.top_logic.ajax.client.control', 'AbstractJSControl', 169);
function $clinit_UIService(){
  $clinit_UIService = emptyMethod;
  _factories = new HashMap;
}

function UIService(){
  $clinit_UIService();
}

function control_0(controlElement){
  var control;
  control = castTo(controlElement['tlControl'], 183);
  if (!control) {
    throw toJs(new IllegalArgumentException("No control on element '" + controlElement.id + "'."));
  }
  return control;
}

function create_2(type_0, controlId){
  var factory;
  factory = castTo($getStringValue(_factories, type_0), 190);
  if (!factory) {
    throw toJs(new IllegalArgumentException("Control type '" + type_0 + "' not supported."));
  }
  return factory.createControl(controlId);
}

function init_1(type_0, controlId){
  $clinit_UIService();
  var $i, args, control;
  args = initUnidimensionalArray(Ljava_lang_Object_2_classLit, $intern_0, 1, arguments.length - 2, 5, 1);
  for ($i = 0; $i < arguments.length - 2; $i++) {
    setCheck(args, $i, arguments[$i + 2]);
  }
  if ($equals_0('diagramJSgraph', type_0)) {
    if ($getElementById($doc, controlId + '-umljs-container')) {
      return;
    }
  }
  control = create_2(type_0, controlId);
  control.init_0(args);
  $getElementById($doc, control._id)['tlControl'] = control;
}

function invoke_0(controlElement, command){
  $clinit_UIService();
  var $i, args;
  args = initUnidimensionalArray(Ljava_lang_Object_2_classLit, $intern_0, 1, arguments.length - 2, 5, 1);
  for ($i = 0; $i < arguments.length - 2; $i++) {
    setCheck(args, $i, arguments[$i + 2]);
  }
  control_0(controlElement).invoke_0(command, args);
}

function registerFactory(type_0, factory){
  $clinit_UIService();
  $putStringValue(_factories, type_0, factory);
  $wnd.services.gwt.onLoad(type_0);
}

defineClass(478, 1, {}, UIService);
var _factories;
var Lcom_top_1logic_ajax_client_service_UIService_2_classLit = createForClass('com.top_logic.ajax.client.service', 'UIService', 478);
function $clinit_CollectionUtilShared(){
  var set_0;
  $clinit_CollectionUtilShared = emptyMethod;
  $clinit_IteratorUtilShared();
  $clinit_Collections();
  set_0 = new HashSet_0;
  set_0.map_0.put(null, set_0);
  new Collections$UnmodifiableSet(set_0);
}

function addInTopologicalOrder(dependencies, result, seen, pending, element, input_0, addDependencies){
  var cycle, dependency, dependency$iterator;
  if (seen.contains(element)) {
    if (pending.contains(element)) {
      cycle = new ArrayList_1(pending);
      push_1(cycle.array, element);
      throw toJs(new CyclicDependencyException(cycle));
    }
    return;
  }
  seen.add(element);
  pending.add(element);
  for (dependency$iterator = castTo(dependencies.apply_0(element), 24).iterator(); dependency$iterator.hasNext_0();) {
    dependency = dependency$iterator.next_1();
    addInTopologicalOrder(dependencies, result, seen, pending, dependency, input_0, addDependencies);
  }
  pending.remove(element);
  (addDependencies || input_0.contains(element)) && result.add(element);
}

function topsort(dependencies, input_0){
  $clinit_CollectionUtilShared();
  var element, element$iterator, inputSet, pending, result, seen;
  inputSet = new HashSet_1(input_0);
  result = new ArrayList;
  seen = new HashSet;
  pending = new LinkedHashSet;
  for (element$iterator = input_0.iterator(); element$iterator.hasNext_0();) {
    element = element$iterator.next_1();
    addInTopologicalOrder(dependencies, result, seen, pending, element, inputSet, false);
  }
  return result;
}

function IllegalArgumentException(message){
  RuntimeException_0.call(this, message);
}

function IllegalArgumentException_0(message, cause){
  RuntimeException_1.call(this, message, cause);
}

defineClass(15, 16, $intern_2, IllegalArgumentException, IllegalArgumentException_0);
var Ljava_lang_IllegalArgumentException_2_classLit = createForClass('java.lang', 'IllegalArgumentException', 15);
function CyclicDependencyException(cycle){
  IllegalArgumentException.call(this, 'Cyclic dependencies, cannot sort topologically, cycle: ' + castToString($collect($map_0(new StreamImpl(null, new Spliterators$IteratorSpliterator(cycle, 16)), new CyclicDependencyException$0methodref$toString$Type), of_0(new Collectors$lambda$15$Type, new Collectors$9methodref$add$Type, new Collectors$10methodref$merge$Type, new Collectors$11methodref$toString$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [])))));
}

defineClass(413, 15, $intern_2, CyclicDependencyException);
var Lcom_top_1logic_basic_shared_collection_CyclicDependencyException_2_classLit = createForClass('com.top_logic.basic.shared.collection', 'CyclicDependencyException', 413);
function CyclicDependencyException$0methodref$toString$Type(){
}

defineClass(414, 1, {}, CyclicDependencyException$0methodref$toString$Type);
_.apply_0 = function apply_1(arg0){
  return toString_8(arg0);
}
;
var Lcom_top_1logic_basic_shared_collection_CyclicDependencyException$0methodref$toString$Type_2_classLit = createForClass('com.top_logic.basic.shared.collection', 'CyclicDependencyException/0methodref$toString$Type', 414);
function $clinit_IteratorUtilShared(){
  $clinit_IteratorUtilShared = emptyMethod;
  $clinit_Collections();
  $clinit_Collections$EmptyListIterator();
}

function $read(this$static, cbuf, off, len){
  var avail, cnt;
  avail = this$static._data.length - this$static._pos;
  if (avail == 0) {
    return -1;
  }
  cnt = $wnd.Math.min(avail, len);
  arraycopy(this$static._data, this$static._pos, cbuf, off, cnt);
  this$static._pos += cnt;
  return cnt;
}

function StringR(data_0){
  this._data = $toCharArray(data_0);
}

defineClass(147, 1, {598:1}, StringR);
_._pos = 0;
var Lcom_top_1logic_basic_shared_io_StringR_2_classLit = createForClass('com.top_logic.basic.shared.io', 'StringR', 147);
function UmlJS(){
}

function createDiagram(parent_0, containerID){
  return new $wnd.UmlJS({parent:parent_0, containerID:containerID});
}

function createDiagramId(parentID, containerID){
  var parent_0;
  parent_0 = $getElementById($doc, parentID);
  return createDiagram(parent_0, containerID);
}

defineClass(479, 1, {}, UmlJS);
var Lcom_top_1logic_client_diagramjs_binding_UmlJS_2_classLit = createForClass('com.top_logic.client.diagramjs.binding', 'UmlJS', 479);
function $clinit_CommandExecutionPhase(){
  $clinit_CommandExecutionPhase = emptyMethod;
  CAN_EXECUTE = new CommandExecutionPhase('CAN_EXECUTE', 0, 'canExecute');
  PRE_EXECUTE = new CommandExecutionPhase('PRE_EXECUTE', 1, 'preExecute');
  PRE_EXECUTED = new CommandExecutionPhase('PRE_EXECUTED', 2, 'preExecuted');
  EXECUTE = new CommandExecutionPhase('EXECUTE', 3, 'execute');
  EXECUTED = new CommandExecutionPhase('EXECUTED', 4, 'executed');
  POST_EXECUTE = new CommandExecutionPhase('POST_EXECUTE', 5, 'postExecute');
  POST_EXECUTED = new CommandExecutionPhase('POST_EXECUTED', 6, 'postExecuted');
  REVERT = new CommandExecutionPhase('REVERT', 7, 'revert');
  REVERTED = new CommandExecutionPhase('REVERTED', 8, 'reverted');
}

function CommandExecutionPhase(enum$name, enum$ordinal, executionPhaseName){
  Enum.call(this, enum$name, enum$ordinal);
  this._executionPhaseName = executionPhaseName;
}

function values_1(){
  $clinit_CommandExecutionPhase();
  return stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_client_diagramjs_command_CommandExecutionPhase_2_classLit, 1), $intern_0, 60, 0, [CAN_EXECUTE, PRE_EXECUTE, PRE_EXECUTED, EXECUTE, EXECUTED, POST_EXECUTE, POST_EXECUTED, REVERT, REVERTED]);
}

defineClass(60, 29, {60:1, 3:1, 33:1, 29:1}, CommandExecutionPhase);
var CAN_EXECUTE, EXECUTE, EXECUTED, POST_EXECUTE, POST_EXECUTED, PRE_EXECUTE, PRE_EXECUTED, REVERT, REVERTED;
var Lcom_top_1logic_client_diagramjs_command_CommandExecutionPhase_2_classLit = createForEnum('com.top_logic.client.diagramjs.command', 'CommandExecutionPhase', 60, values_1);
function $addCommandInterceptor(this$static, command, clarifier, handler){
  $addEventHandler(this$static._eventBus, 'commandStack.' + command + '.' + clarifier._executionPhaseName, handler);
}

function $addCommandInterceptor_0(this$static, commands, clarifier, handler){
  var command, command$iterator, eventBus;
  eventBus = this$static._eventBus;
  for (command$iterator = new AbstractList$IteratorImpl(commands); command$iterator.i < command$iterator.this$01.size();) {
    command = (checkCriticalElement(command$iterator.i < command$iterator.this$01.size()) , castToString(command$iterator.this$01.getAtIndex(command$iterator.last = command$iterator.i++)));
    $addEventHandler(eventBus, 'commandStack.' + command + '.' + clarifier._executionPhaseName, handler);
  }
}

function $addShape(this$static, shape_0, parent_0){
  this$static.addShape(shape_0, parent_0);
}

function $inViewbox(this$static, element){
  var source, target;
  if (has(element, 'children') && !has(element, 'waypoints')) {
    return $inViewbox_0(this$static, element);
  }
   else if (has(element, 'waypoints')) {
    return source = element.source , target = element.target , $contains_0(this$static.viewbox(), $getPosition(source)) || $contains_0(this$static.viewbox(), $getPosition(target));
  }
   else if (has(element, 'labelTarget')) {
    return $inViewbox_0(this$static, element);
  }
  return false;
}

function $inViewbox_0(this$static, shape_0){
  return $contains_0(this$static.viewbox(), $getPosition(shape_0));
}

function $inViewbox_1(this$static, elements){
  var element, element$iterator;
  for (element$iterator = elements.iterator(); element$iterator.hasNext_0();) {
    element = castToJso(element$iterator.next_1());
    if (!$inViewbox(this$static, element)) {
      return false;
    }
  }
  return true;
}

function $setRootElement(this$static, root){
  this$static.setRootElement(root);
}

function $setViewbox(this$static, bounds){
  this$static.viewbox(bounds);
}

function $createClass(this$static, attributes){
  return this$static.createClass(attributes);
}

function $createConnection(this$static, attributes){
  return this$static.createConnection(attributes);
}

function $createLabel(this$static, attributes){
  return this$static.createUmlLabel(attributes);
}

function $layoutConnection(this$static, connection){
  return this$static.layoutConnection(connection);
}

function $setShowHiddenElements(this$static, showHiddenElements){
  this$static.showHiddenElements = showHiddenElements;
}

function $removeElementsInternal(this$static, elements){
  this$static.removeElements(elements);
}

function $resizeShape(this$static, shape_0, newBounds){
  this$static.resizeShape(shape_0, newBounds);
}

function $selectInternal(this$static, elements, addToSelected){
  this$static.select(elements, addToSelected);
}

function $getDimensions(this$static, text_0){
  return this$static.getDimensions(text_0);
}

function $addEventHandler(this$static, event_0, handler){
  this$static.on(event_0, handler);
}

function $setVisibility(this$static, isVisible){
  this$static.isVisible = isVisible;
}

function $setType(this$static, type_0){
  this$static.connectionType = type_0;
}

function $getBounds(this$static){
  return {x:this$static.x, y:this$static.y, width:this$static.width, height:this$static.height};
}

function $getPosition(this$static){
  return {x:this$static.x, y:this$static.y};
}

function $setX(this$static, x_0){
  this$static.x = x_0;
}

function $setY(this$static, y_0){
  this$static.y = y_0;
}

function $setImported(this$static, isImported){
  this$static.isImported = isImported;
}

function $setStereotypes(this$static, stereotypes){
  this$static.stereotypes = stereotypes;
}

function $contains(this$static, x_0, y_0){
  if (x_0 > this$static.x && x_0 < this$static.x + this$static.width) {
    return y_0 > this$static.y && y_0 < this$static.y + this$static.height;
  }
  return false;
}

function $contains_0(this$static, position){
  var x_0, y_0;
  x_0 = position.x;
  y_0 = position.y;
  return $contains(this$static, x_0, y_0);
}

function $getDimension(this$static){
  return {width:this$static.width, height:this$static.height};
}

function $getPosition_0(this$static){
  return {x:this$static.x, y:this$static.y};
}

function $setDimension(this$static, dimensions){
  $setWidth(this$static, dimensions.width);
  $setHeight(this$static, dimensions.height);
}

function $setHeight(this$static, height){
  this$static.height = height;
}

function $setPosition(this$static, position){
  $setX_0(this$static, position.x);
  $setY_0(this$static, position.y);
}

function $setWidth(this$static, width_0){
  this$static.width = width_0;
}

function $setX_0(this$static, x_0){
  this$static.x = x_0;
}

function $setY_0(this$static, y_0){
  this$static.y = y_0;
}

function getAbsoluteClosePoint(position1, position2, absoluteDistance){
  var euclidianDistance, xDifference, yDifference, relativeClosePoint;
  euclidianDistance = (xDifference = position1.x - position2.x , yDifference = position1.y - position2.y , $wnd.Math.sqrt(xDifference * xDifference + yDifference * yDifference));
  return relativeClosePoint = {} , $setX_1(relativeClosePoint, (1 - absoluteDistance / euclidianDistance) * position1.x + absoluteDistance / euclidianDistance * position2.x) , $setY_1(relativeClosePoint, (1 - absoluteDistance / euclidianDistance) * position1.y + absoluteDistance / euclidianDistance * position2.y) , relativeClosePoint;
}

function $move(this$static, deltaX, deltaY){
  $setX_1(this$static, this$static.x + deltaX);
  $setY_1(this$static, this$static.y + deltaY);
}

function $setX_1(this$static, x_0){
  this$static.x = x_0;
}

function $setY_1(this$static, y_0){
  this$static.y = y_0;
}

function $setX_2(this$static, x_0){
  this$static.x = x_0;
}

function $setY_2(this$static, y_0){
  this$static.y = y_0;
}

function createBounds(position, dimension){
  var bounds;
  bounds = {};
  $setX_0(bounds, position.x);
  $setY_0(bounds, position.y);
  $setWidth(bounds, dimension.width);
  $setHeight(bounds, dimension.height);
  return bounds;
}

function getAbsoluteElementPosition(canvas, position){
  var viewboxPosition;
  viewboxPosition = $getPosition_0(canvas.viewbox());
  $move(position, viewboxPosition.x, viewboxPosition.y);
  return position;
}

function getCenter(points){
  var center, point, point$iterator, x_0, y_0;
  center = {};
  x_0 = 0;
  y_0 = 0;
  for (point$iterator = points.iterator(); point$iterator.hasNext_0();) {
    point = castToJso(point$iterator.next_1());
    x_0 += point.x;
    y_0 += point.y;
  }
  $setX_1(center, x_0 / points.size());
  $setY_1(center, y_0 / points.size());
  return center;
}

function getCenter_0(elementsBounds, size_0){
  var center;
  center = getCenter(castTo($collect($map_0(elementsBounds.stream(), new DiagramJSObjectUtil$lambda$1$Type), of_0(new Collectors$23methodref$ctor$Type, new Collectors$24methodref$add$Type, new Collectors$lambda$50$Type, new Collectors$lambda$51$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , UNORDERED), IDENTITY_FINISH]))), 39));
  $move(center, -size_0.width / 2, -size_0.height / 2);
  return center;
}

function getNonLabelElement(element){
  return has(element, 'labelTarget')?element.parent:element;
}

function lambda$0(canvas_0, element_1){
  var absoluteBBox;
  absoluteBBox = canvas_0.getAbsoluteBBox(element_1);
  $setPosition(absoluteBBox, getAbsoluteElementPosition(canvas_0, $getPosition_0(absoluteBBox)));
  return absoluteBBox;
}

function setWaypoints(object, waypoints){
  var newWaypoints = [];
  for (var i = 0; i < waypoints.length; i++) {
    var waypoint = waypoints[i];
    newWaypoints.push({x:waypoint.x, y:waypoint.y});
  }
  object.waypoints = newWaypoints;
}

function DiagramJSObjectUtil$lambda$0$Type(canvas_0){
  this.canvas_0 = canvas_0;
}

defineClass(223, 1, {}, DiagramJSObjectUtil$lambda$0$Type);
_.apply_0 = function apply_2(arg0){
  return lambda$0(this.canvas_0, castToJso(arg0));
}
;
var Lcom_top_1logic_client_diagramjs_util_DiagramJSObjectUtil$lambda$0$Type_2_classLit = createForClass('com.top_logic.client.diagramjs.util', 'DiagramJSObjectUtil/lambda$0$Type', 223);
function DiagramJSObjectUtil$lambda$1$Type(){
}

defineClass(389, 1, {}, DiagramJSObjectUtil$lambda$1$Type);
_.apply_0 = function apply_3(arg0){
  return $getPosition_0(castToJso(arg0));
}
;
var Lcom_top_1logic_client_diagramjs_util_DiagramJSObjectUtil$lambda$1$Type_2_classLit = createForClass('com.top_logic.client.diagramjs.util', 'DiagramJSObjectUtil/lambda$1$Type', 389);
function lambda$0_0(waypoint_0){
  return new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_Double_2_classLit, 1), $intern_0, 191, 7, [waypoint_0.x, waypoint_0.y]));
}

function lambda$1(waypoint_0){
  var waypointObject;
  waypointObject = {};
  $setX_2(waypointObject, $doubleValue(castToDouble(waypoint_0.getAtIndex(0))));
  $setY_2(waypointObject, $doubleValue(castToDouble(waypoint_0.getAtIndex(1))));
  return waypointObject;
}

function DiagramUtil$lambda$0$Type(){
}

defineClass(411, 1, {}, DiagramUtil$lambda$0$Type);
_.apply_0 = function apply_4(arg0){
  return lambda$0_0(castToJso(arg0));
}
;
var Lcom_top_1logic_client_diagramjs_util_DiagramUtil$lambda$0$Type_2_classLit = createForClass('com.top_logic.client.diagramjs.util', 'DiagramUtil/lambda$0$Type', 411);
function DiagramUtil$lambda$1$Type(){
}

defineClass(412, 1, {}, DiagramUtil$lambda$1$Type);
_.apply_0 = function apply_5(arg0){
  return lambda$1(castTo(arg0, 13));
}
;
var Lcom_top_1logic_client_diagramjs_util_DiagramUtil$lambda$1$Type_2_classLit = createForClass('com.top_logic.client.diagramjs.util', 'DiagramUtil/lambda$1$Type', 412);
function assign_0(target_0, source){
  function assign(target, varArgs){
    if (target == null) {
      throw new TypeError('Cannot convert undefined or null to object');
    }
    var to = Object(target);
    for (var index_0 = 1; index_0 < arguments.length; index_0++) {
      var nextSource = arguments[index_0];
      if (nextSource != null) {
        for (var nextKey in nextSource) {
          Object.prototype.hasOwnProperty.call(nextSource, nextKey) && (to[nextKey] = nextSource[nextKey]);
        }
      }
    }
    return to;
  }

  typeof Object.assign != 'function' && Object.defineProperty(Object, 'assign', {value:assign, writable:true, configurable:true});
  Object.assign(target_0, source);
}

function getArray(items){
  var array;
  array = [];
  pushAll(array, items);
  return array;
}

function has(object, property){
  return Object.prototype.hasOwnProperty.call(object, property);
}

function pushAll(object, items){
  var item_0, item$iterator;
  for (item$iterator = items.iterator(); item$iterator.hasNext_0();) {
    item_0 = item$iterator.next_1();
    object.push(item_0);
  }
}

function $beginArray(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  if (p == 3) {
    $push(this$static, 1);
    this$static.pathIndices[this$static.stackSize - 1] = 0;
    this$static.peeked = 0;
  }
   else {
    throw toJs(new IllegalStateException_0('Expected BEGIN_ARRAY but was ' + $peek(this$static) + $locationString(this$static)));
  }
}

function $beginObject(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  if (p == 1) {
    $push(this$static, 3);
    this$static.peeked = 0;
  }
   else {
    throw toJs(new IllegalStateException_0('Expected BEGIN_OBJECT but was ' + $peek(this$static) + $locationString(this$static)));
  }
}

function $checkLenient(this$static){
  throw toJs($syntaxError(this$static, 'Use JsonReader.setLenient(true) to accept malformed JSON'));
}

function $doPeek(this$static){
  var c, c0, c1, peekStack, result;
  peekStack = this$static.stack_0[this$static.stackSize - 1];
  if (peekStack == 1) {
    this$static.stack_0[this$static.stackSize - 1] = 2;
  }
   else if (peekStack == 2) {
    c0 = $nextNonWhitespace(this$static, true);
    switch (c0) {
      case 93:
        return this$static.peeked = 4;
      case 59:
        $checkLenient(this$static);
      case 44:
        break;
      default:throw toJs($syntaxError(this$static, 'Unterminated array'));
    }
  }
   else if (peekStack == 3 || peekStack == 5) {
    this$static.stack_0[this$static.stackSize - 1] = 4;
    if (peekStack == 5) {
      c0 = $nextNonWhitespace(this$static, true);
      switch (c0) {
        case 125:
          return this$static.peeked = 2;
        case 59:
          $checkLenient(this$static);
        case 44:
          break;
        default:throw toJs($syntaxError(this$static, 'Unterminated object'));
      }
    }
    c1 = $nextNonWhitespace(this$static, true);
    switch (c1) {
      case 34:
        return this$static.peeked = 13;
      case 39:
        $checkLenient(this$static);
        return this$static.peeked = 12;
      case 125:
        if (peekStack != 5) {
          return this$static.peeked = 2;
        }
         else {
          throw toJs($syntaxError(this$static, 'Expected name'));
        }

      default:$checkLenient(this$static);
        --this$static.pos;
        if ($isLiteral(this$static, c1 & $intern_6)) {
          return this$static.peeked = 14;
        }
         else {
          throw toJs($syntaxError(this$static, 'Expected name'));
        }

    }
  }
   else if (peekStack == 4) {
    this$static.stack_0[this$static.stackSize - 1] = 5;
    c0 = $nextNonWhitespace(this$static, true);
    switch (c0) {
      case 58:
        break;
      case 61:
        $checkLenient(this$static);
        (this$static.pos < this$static.limit || $fillBuffer(this$static, 1)) && this$static.buffer[this$static.pos] == 62 && ++this$static.pos;
        break;
      default:throw toJs($syntaxError(this$static, "Expected ':'"));
    }
  }
   else if (peekStack == 6) {
    this$static.stack_0[this$static.stackSize - 1] = 7;
  }
   else if (peekStack == 7) {
    c0 = $nextNonWhitespace(this$static, false);
    if (c0 == -1) {
      return this$static.peeked = 17;
    }
     else {
      $checkLenient(this$static);
      --this$static.pos;
    }
  }
   else if (peekStack == 8) {
    throw toJs(new IllegalStateException_0('JsonReader is closed'));
  }
  c = $nextNonWhitespace(this$static, true);
  switch (c) {
    case 93:
      if (peekStack == 1) {
        return this$static.peeked = 4;
      }

    case 59:
    case 44:
      if (peekStack == 1 || peekStack == 2) {
        $checkLenient(this$static);
        --this$static.pos;
        return this$static.peeked = 7;
      }
       else {
        throw toJs($syntaxError(this$static, 'Unexpected value'));
      }

    case 39:
      $checkLenient(this$static);
      return this$static.peeked = 8;
    case 34:
      return this$static.peeked = 9;
    case 91:
      return this$static.peeked = 3;
    case 123:
      return this$static.peeked = 1;
    default:--this$static.pos;
  }
  result = $peekKeyword(this$static);
  if (result != 0) {
    return result;
  }
  result = $peekNumber(this$static);
  if (result != 0) {
    return result;
  }
  if (!$isLiteral(this$static, this$static.buffer[this$static.pos])) {
    throw toJs($syntaxError(this$static, 'Expected value'));
  }
  $checkLenient(this$static);
  return this$static.peeked = 10;
}

function $endArray(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  if (p == 4) {
    --this$static.stackSize;
    ++this$static.pathIndices[this$static.stackSize - 1];
    this$static.peeked = 0;
  }
   else {
    throw toJs(new IllegalStateException_0('Expected END_ARRAY but was ' + $peek(this$static) + $locationString(this$static)));
  }
}

function $endObject(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  if (p == 2) {
    --this$static.stackSize;
    this$static.pathNames[this$static.stackSize] = null;
    ++this$static.pathIndices[this$static.stackSize - 1];
    this$static.peeked = 0;
  }
   else {
    throw toJs(new IllegalStateException_0('Expected END_OBJECT but was ' + $peek(this$static) + $locationString(this$static)));
  }
}

function $fillBuffer(this$static, minimum){
  var buffer, total;
  buffer = this$static.buffer;
  this$static.lineStart -= this$static.pos;
  if (this$static.limit != this$static.pos) {
    this$static.limit -= this$static.pos;
    arraycopy(buffer, this$static.pos, buffer, 0, this$static.limit);
  }
   else {
    this$static.limit = 0;
  }
  this$static.pos = 0;
  while ((total = $read(this$static.in_0, buffer, this$static.limit, buffer.length - this$static.limit)) != -1) {
    this$static.limit += total;
    if (this$static.lineNumber == 0 && this$static.lineStart == 0 && this$static.limit > 0 && buffer[0] == 65279) {
      ++this$static.pos;
      ++this$static.lineStart;
      ++minimum;
    }
    if (this$static.limit >= minimum) {
      return true;
    }
  }
  return false;
}

function $getPath(this$static){
  var i, pathIndex, result;
  result = $append_1(new StringBuilder, 36);
  for (i = 0; i < this$static.stackSize; i++) {
    switch (this$static.stack_0[i]) {
      case 1:
      case 2:
        pathIndex = this$static.pathIndices[i];
        $append_1($append_2((result.string += '[' , result), pathIndex), 93);
        break;
      case 3:
      case 4:
      case 5:
        result.string += '.';
        this$static.pathNames[i] != null && $append_7(result, this$static.pathNames[i]);
    }
  }
  return result.string;
}

function $hasNext(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  return p != 2 && p != 4 && p != 17;
}

function $isLiteral(this$static, c){
  switch (c) {
    case 47:
    case 92:
    case 59:
    case 35:
    case 61:
      $checkLenient(this$static);
    case 123:
    case 125:
    case 91:
    case 93:
    case 58:
    case 44:
    case 32:
    case 9:
    case 12:
    case 13:
    case 10:
      return false;
    default:return true;
  }
}

function $locationString(this$static){
  var column, line;
  line = this$static.lineNumber + 1;
  column = this$static.pos - this$static.lineStart + 1;
  return ' at line ' + line + ' column ' + column + ' path ' + $getPath(this$static);
}

function $nextBoolean(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  if (p == 5) {
    this$static.peeked = 0;
    ++this$static.pathIndices[this$static.stackSize - 1];
    return true;
  }
   else if (p == 6) {
    this$static.peeked = 0;
    ++this$static.pathIndices[this$static.stackSize - 1];
    return false;
  }
  throw toJs(new IllegalStateException_0('Expected a boolean but was ' + $peek(this$static) + $locationString(this$static)));
}

function $nextDouble(this$static){
  var p, result;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  if (p == 15) {
    this$static.peeked = 0;
    ++this$static.pathIndices[this$static.stackSize - 1];
    return toDouble_0(this$static.peekedLong);
  }
  if (p == 16) {
    this$static.peekedString = valueOf_1(this$static.buffer, this$static.pos, this$static.peekedNumberLength);
    this$static.pos += this$static.peekedNumberLength;
  }
   else if (p == 8 || p == 9) {
    this$static.peekedString = $nextQuotedValue(this$static, p == 8?39:34);
  }
   else if (p == 10) {
    this$static.peekedString = $nextUnquotedValue(this$static);
  }
   else if (p != 11) {
    throw toJs(new IllegalStateException_0('Expected a double but was ' + $peek(this$static) + $locationString(this$static)));
  }
  this$static.peeked = 11;
  result = __parseAndValidateDouble(this$static.peekedString);
  if (isNaN(result) || !isNaN(result) && !isFinite(result)) {
    throw toJs(new MalformedJsonException('JSON forbids NaN and infinities: ' + result + $locationString(this$static)));
  }
  this$static.peekedString = null;
  this$static.peeked = 0;
  ++this$static.pathIndices[this$static.stackSize - 1];
  return result;
}

function $nextName(this$static){
  var p, result;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  if (p == 14) {
    result = $nextUnquotedValue(this$static);
  }
   else if (p == 12) {
    result = $nextQuotedValue(this$static, 39);
  }
   else if (p == 13) {
    result = $nextQuotedValue(this$static, 34);
  }
   else {
    throw toJs(new IllegalStateException_0('Expected a name but was ' + $peek(this$static) + $locationString(this$static)));
  }
  this$static.peeked = 0;
  this$static.pathNames[this$static.stackSize - 1] = result;
  return result;
}

function $nextNonWhitespace(this$static, throwOnEof){
  var buffer, c, charsLoaded, l, p, peek;
  buffer = this$static.buffer;
  p = this$static.pos;
  l = this$static.limit;
  while (true) {
    if (p == l) {
      this$static.pos = p;
      if (!$fillBuffer(this$static, 1)) {
        break;
      }
      p = this$static.pos;
      l = this$static.limit;
    }
    c = buffer[p++];
    if (c == 10) {
      ++this$static.lineNumber;
      this$static.lineStart = p;
      continue;
    }
     else if (c == 32 || c == 13 || c == 9) {
      continue;
    }
    if (c == 47) {
      this$static.pos = p;
      if (p == l) {
        --this$static.pos;
        charsLoaded = $fillBuffer(this$static, 2);
        ++this$static.pos;
        if (!charsLoaded) {
          return c;
        }
      }
      $checkLenient(this$static);
      peek = buffer[this$static.pos];
      switch (peek) {
        case 42:
          ++this$static.pos;
          if (!$skipTo(this$static)) {
            throw toJs($syntaxError(this$static, 'Unterminated comment'));
          }

          p = this$static.pos + 2;
          l = this$static.limit;
          continue;
        case 47:
          ++this$static.pos;
          $skipToEndOfLine(this$static);
          p = this$static.pos;
          l = this$static.limit;
          continue;
        default:return c;
      }
    }
     else if (c == 35) {
      this$static.pos = p;
      $checkLenient(this$static);
      $skipToEndOfLine(this$static);
      p = this$static.pos;
      l = this$static.limit;
    }
     else {
      this$static.pos = p;
      return c;
    }
  }
  if (throwOnEof) {
    throw toJs(new IOException('End of input' + $locationString(this$static)));
  }
   else {
    return -1;
  }
}

function $nextNull(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  if (p == 7) {
    this$static.peeked = 0;
    ++this$static.pathIndices[this$static.stackSize - 1];
  }
   else {
    throw toJs(new IllegalStateException_0('Expected null but was ' + $peek(this$static) + $locationString(this$static)));
  }
}

function $nextQuotedValue(this$static, quote_0){
  var buffer, builder, c, estimatedLength, l, len, p, start_0;
  buffer = this$static.buffer;
  builder = null;
  while (true) {
    p = this$static.pos;
    l = this$static.limit;
    start_0 = p;
    while (p < l) {
      c = buffer[p++];
      if (c == quote_0) {
        this$static.pos = p;
        len = p - start_0 - 1;
        if (!builder) {
          return valueOf_1(buffer, start_0, len);
        }
         else {
          builder.string += valueOf_1(buffer, start_0, len);
          return builder.string;
        }
      }
       else if (c == 92) {
        this$static.pos = p;
        len = p - start_0 - 1;
        if (!builder) {
          estimatedLength = (len + 1) * 2;
          builder = ($wnd.Math.max(estimatedLength, 16) , new StringBuilder_0);
        }
        builder.string += valueOf_1(buffer, start_0, len);
        $append_1(builder, $readEscapeCharacter(this$static));
        p = this$static.pos;
        l = this$static.limit;
        start_0 = p;
      }
       else if (c == 10) {
        ++this$static.lineNumber;
        this$static.lineStart = p;
      }
    }
    if (!builder) {
      estimatedLength = (p - start_0) * 2;
      builder = ($wnd.Math.max(estimatedLength, 16) , new StringBuilder_0);
    }
    builder.string += valueOf_1(buffer, start_0, p - start_0);
    this$static.pos = p;
    if (!$fillBuffer(this$static, 1)) {
      throw toJs($syntaxError(this$static, 'Unterminated string'));
    }
  }
}

function $nextString(this$static){
  var p, result;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  if (p == 10) {
    result = $nextUnquotedValue(this$static);
  }
   else if (p == 8) {
    result = $nextQuotedValue(this$static, 39);
  }
   else if (p == 9) {
    result = $nextQuotedValue(this$static, 34);
  }
   else if (p == 11) {
    result = this$static.peekedString;
    this$static.peekedString = null;
  }
   else if (p == 15) {
    result = '' + toString_7(this$static.peekedLong);
  }
   else if (p == 16) {
    result = valueOf_1(this$static.buffer, this$static.pos, this$static.peekedNumberLength);
    this$static.pos += this$static.peekedNumberLength;
  }
   else {
    throw toJs(new IllegalStateException_0('Expected a string but was ' + $peek(this$static) + $locationString(this$static)));
  }
  this$static.peeked = 0;
  ++this$static.pathIndices[this$static.stackSize - 1];
  return result;
}

function $nextUnquotedValue(this$static){
  var builder, i, result;
  builder = null;
  i = 0;
  findNonLiteralCharacter: while (true) {
    for (; this$static.pos + i < this$static.limit; i++) {
      switch (this$static.buffer[this$static.pos + i]) {
        case 47:
        case 92:
        case 59:
        case 35:
        case 61:
          $checkLenient(this$static);
        case 123:
        case 125:
        case 91:
        case 93:
        case 58:
        case 44:
        case 32:
        case 9:
        case 12:
        case 13:
        case 10:
          break findNonLiteralCharacter;
      }
    }
    if (i < this$static.buffer.length) {
      if ($fillBuffer(this$static, i + 1)) {
        continue;
      }
       else {
        break;
      }
    }
    !builder && (builder = ($wnd.Math.max(i, 16) , new StringBuilder_0));
    $append_8(builder, this$static.buffer, this$static.pos, i);
    this$static.pos += i;
    i = 0;
    if (!$fillBuffer(this$static, 1)) {
      break;
    }
  }
  result = !builder?valueOf_1(this$static.buffer, this$static.pos, i):$append_8(builder, this$static.buffer, this$static.pos, i).string;
  this$static.pos += i;
  return result;
}

function $peek(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek(this$static));
  switch (p) {
    case 1:
      return $clinit_JsonToken() , BEGIN_OBJECT;
    case 2:
      return $clinit_JsonToken() , END_OBJECT;
    case 3:
      return $clinit_JsonToken() , BEGIN_ARRAY;
    case 4:
      return $clinit_JsonToken() , END_ARRAY;
    case 12:
    case 13:
    case 14:
      return $clinit_JsonToken() , NAME;
    case 5:
    case 6:
      return $clinit_JsonToken() , BOOLEAN;
    case 7:
      return $clinit_JsonToken() , NULL;
    case 8:
    case 9:
    case 10:
    case 11:
      return $clinit_JsonToken() , STRING;
    case 15:
    case 16:
      return $clinit_JsonToken() , NUMBER;
    case 17:
      return $clinit_JsonToken() , END_DOCUMENT;
    default:throw toJs(new AssertionError);
  }
}

function $peekKeyword(this$static){
  var c, i, keyword, keywordUpper, length_0, peeking;
  c = this$static.buffer[this$static.pos];
  if (c == 116 || c == 84) {
    keyword = 'true';
    keywordUpper = 'TRUE';
    peeking = 5;
  }
   else if (c == 102 || c == 70) {
    keyword = 'false';
    keywordUpper = 'FALSE';
    peeking = 6;
  }
   else if (c == 110 || c == 78) {
    keyword = 'null';
    keywordUpper = 'NULL';
    peeking = 7;
  }
   else {
    return 0;
  }
  length_0 = keyword.length;
  for (i = 1; i < length_0; i++) {
    if (this$static.pos + i >= this$static.limit && !$fillBuffer(this$static, i + 1)) {
      return 0;
    }
    c = this$static.buffer[this$static.pos + i];
    if (c != (checkCriticalStringElementIndex(i, keyword.length) , keyword.charCodeAt(i)) && c != (checkCriticalStringElementIndex(i, keywordUpper.length) , keywordUpper.charCodeAt(i))) {
      return 0;
    }
  }
  if ((this$static.pos + length_0 < this$static.limit || $fillBuffer(this$static, length_0 + 1)) && $isLiteral(this$static, this$static.buffer[this$static.pos + length_0])) {
    return 0;
  }
  this$static.pos += length_0;
  return this$static.peeked = peeking;
}

function $peekNumber(this$static){
  var buffer, c, fitsInLong, i, l, last, negative, newValue, p, value_0;
  buffer = this$static.buffer;
  p = this$static.pos;
  l = this$static.limit;
  value_0 = 0;
  negative = false;
  fitsInLong = true;
  last = 0;
  i = 0;
  charactersOfNumber: for (; true; i++) {
    if (p + i == l) {
      if (i == buffer.length) {
        return 0;
      }
      if (!$fillBuffer(this$static, i + 1)) {
        break;
      }
      p = this$static.pos;
      l = this$static.limit;
    }
    c = buffer[p + i];
    switch (c) {
      case 45:
        if (last == 0) {
          negative = true;
          last = 1;
          continue;
        }
         else if (last == 5) {
          last = 6;
          continue;
        }

        return 0;
      case 43:
        if (last == 5) {
          last = 6;
          continue;
        }

        return 0;
      case 101:
      case 69:
        if (last == 2 || last == 4) {
          last = 5;
          continue;
        }

        return 0;
      case 46:
        if (last == 2) {
          last = 3;
          continue;
        }

        return 0;
      default:if (c < 48 || c > 57) {
          if (!$isLiteral(this$static, c)) {
            break charactersOfNumber;
          }
          return 0;
        }

        if (last == 1 || last == 0) {
          value_0 = -(c - 48);
          last = 2;
        }
         else if (last == 2) {
          if (compare_0(value_0, 0) == 0) {
            return 0;
          }
          newValue = sub_1(mul_0(value_0, 10), c - 48);
          fitsInLong = fitsInLong & (gt(value_0, $intern_14) || eq(value_0, $intern_14) && compare_0(newValue, value_0) < 0);
          value_0 = newValue;
        }
         else 
          last == 3?(last = 4):(last == 5 || last == 6) && (last = 7);
    }
  }
  if (last == 2 && fitsInLong && (neq(value_0, $intern_15) || negative) && (compare_0(value_0, 0) != 0 || !negative)) {
    this$static.peekedLong = negative?value_0:neg_0(value_0);
    this$static.pos += i;
    return this$static.peeked = 15;
  }
   else if (last == 2 || last == 4 || last == 7) {
    this$static.peekedNumberLength = i;
    return this$static.peeked = 16;
  }
   else {
    return 0;
  }
}

function $push(this$static, newTop){
  var newLength;
  if (this$static.stackSize == this$static.stack_0.length) {
    newLength = this$static.stackSize * 2;
    this$static.stack_0 = copyOf(this$static.stack_0, newLength);
    this$static.pathIndices = copyOf(this$static.pathIndices, newLength);
    this$static.pathNames = castTo(copyOf_0(this$static.pathNames, newLength), 12);
  }
  this$static.stack_0[this$static.stackSize++] = newTop;
}

function $readEscapeCharacter(this$static){
  var c, end, escaped, i, result;
  if (this$static.pos == this$static.limit && !$fillBuffer(this$static, 1)) {
    throw toJs($syntaxError(this$static, 'Unterminated escape sequence'));
  }
  escaped = this$static.buffer[this$static.pos++];
  switch (escaped) {
    case 117:
      if (this$static.pos + 4 > this$static.limit && !$fillBuffer(this$static, 4)) {
        throw toJs($syntaxError(this$static, 'Unterminated escape sequence'));
      }

      result = 0;
      for (i = this$static.pos , end = i + 4; i < end; i++) {
        c = this$static.buffer[i];
        result = result << 4 & $intern_6;
        if (c >= 48 && c <= 57) {
          result = result + (c - 48) & $intern_6;
        }
         else if (c >= 97 && c <= 102) {
          result = result + (c - 97 + 10) & $intern_6;
        }
         else if (c >= 65 && c <= 70) {
          result = result + (c - 65 + 10) & $intern_6;
        }
         else {
          throw toJs(new NumberFormatException('\\u' + valueOf_1(this$static.buffer, this$static.pos, 4)));
        }
      }

      this$static.pos += 4;
      return result;
    case 116:
      return 9;
    case 98:
      return 8;
    case 110:
      return 10;
    case 114:
      return 13;
    case 102:
      return 12;
    case 10:
      ++this$static.lineNumber;
      this$static.lineStart = this$static.pos;
    case 39:
    case 34:
    case 92:
    case 47:
      return escaped;
    default:throw toJs($syntaxError(this$static, 'Invalid escape sequence'));
  }
}

function $skipTo(this$static){
  var c, length_0;
  length_0 = '*/'.length;
  outer: for (; this$static.pos + length_0 <= this$static.limit || $fillBuffer(this$static, length_0); this$static.pos++) {
    if (this$static.buffer[this$static.pos] == 10) {
      ++this$static.lineNumber;
      this$static.lineStart = this$static.pos + 1;
      continue;
    }
    for (c = 0; c < length_0; c++) {
      if (this$static.buffer[this$static.pos + c] != (checkCriticalStringElementIndex(c, '*/'.length) , '*/'.charCodeAt(c))) {
        continue outer;
      }
    }
    return true;
  }
  return false;
}

function $skipToEndOfLine(this$static){
  var c;
  while (this$static.pos < this$static.limit || $fillBuffer(this$static, 1)) {
    c = this$static.buffer[this$static.pos++];
    if (c == 10) {
      ++this$static.lineNumber;
      this$static.lineStart = this$static.pos;
      break;
    }
     else if (c == 13) {
      break;
    }
  }
}

function $syntaxError(this$static, message){
  throw toJs(new MalformedJsonException(message + $locationString(this$static)));
}

function JsonReader(in_0){
  this.buffer = initUnidimensionalArray(C_classLit, $intern_4, 46, 1024, 15, 1);
  this.stack_0 = initUnidimensionalArray(I_classLit, $intern_4, 46, 32, 15, 1);
  this.stack_0[this.stackSize++] = 6;
  this.pathNames = initUnidimensionalArray(Ljava_lang_String_2_classLit, $intern_3, 2, 32, 6, 1);
  this.pathIndices = initUnidimensionalArray(I_classLit, $intern_4, 46, 32, 15, 1);
  this.in_0 = castTo(requireNonNull_0(in_0, 'in == null'), 598);
}

defineClass(146, 1, {}, JsonReader);
_.close_0 = function close_3(){
  this.peeked = 0;
  this.stack_0[0] = 8;
  this.stackSize = 1;
}
;
_.toString_0 = function toString_9(){
  return $ensureNamesAreInitialized(Lcom_top_1logic_common_json_gstream_JsonReader_2_classLit) , Lcom_top_1logic_common_json_gstream_JsonReader_2_classLit.simpleName + $locationString(this);
}
;
_.limit = 0;
_.lineNumber = 0;
_.lineStart = 0;
_.peeked = 0;
_.peekedLong = 0;
_.peekedNumberLength = 0;
_.pos = 0;
_.stackSize = 0;
var Lcom_top_1logic_common_json_gstream_JsonReader_2_classLit = createForClass('com.top_logic.common.json.gstream', 'JsonReader', 146);
function $clinit_JsonToken(){
  $clinit_JsonToken = emptyMethod;
  BEGIN_ARRAY = new JsonToken('BEGIN_ARRAY', 0);
  END_ARRAY = new JsonToken('END_ARRAY', 1);
  BEGIN_OBJECT = new JsonToken('BEGIN_OBJECT', 2);
  END_OBJECT = new JsonToken('END_OBJECT', 3);
  NAME = new JsonToken('NAME', 4);
  STRING = new JsonToken('STRING', 5);
  NUMBER = new JsonToken('NUMBER', 6);
  BOOLEAN = new JsonToken('BOOLEAN', 7);
  NULL = new JsonToken('NULL', 8);
  END_DOCUMENT = new JsonToken('END_DOCUMENT', 9);
}

function JsonToken(enum$name, enum$ordinal){
  Enum.call(this, enum$name, enum$ordinal);
}

function values_2(){
  $clinit_JsonToken();
  return stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_common_json_gstream_JsonToken_2_classLit, 1), $intern_0, 53, 0, [BEGIN_ARRAY, END_ARRAY, BEGIN_OBJECT, END_OBJECT, NAME, STRING, NUMBER, BOOLEAN, NULL, END_DOCUMENT]);
}

defineClass(53, 29, {53:1, 3:1, 33:1, 29:1}, JsonToken);
var BEGIN_ARRAY, BEGIN_OBJECT, BOOLEAN, END_ARRAY, END_DOCUMENT, END_OBJECT, NAME, NULL, NUMBER, STRING;
var Lcom_top_1logic_common_json_gstream_JsonToken_2_classLit = createForEnum('com.top_logic.common.json.gstream', 'JsonToken', 53, values_2);
function $clinit_JsonWriter(){
  $clinit_JsonWriter = emptyMethod;
  var i, number, result;
  REPLACEMENT_CHARS = initUnidimensionalArray(Ljava_lang_String_2_classLit, $intern_3, 2, 128, 6, 1);
  for (i = 0; i <= 31; i++) {
    REPLACEMENT_CHARS[i] = (result = (number = i >>> 0 , number.toString(16)) , '\\u' + ($substring_0('0000', result.length, 4) + ('' + result)));
  }
  REPLACEMENT_CHARS[34] = '\\"';
  REPLACEMENT_CHARS[92] = '\\\\';
  REPLACEMENT_CHARS[9] = '\\t';
  REPLACEMENT_CHARS[8] = '\\b';
  REPLACEMENT_CHARS[10] = '\\n';
  REPLACEMENT_CHARS[13] = '\\r';
  REPLACEMENT_CHARS[12] = '\\f';
  HTML_SAFE_REPLACEMENT_CHARS = initUnidimensionalArray(Ljava_lang_String_2_classLit, $intern_3, 2, 128, 6, 1);
  arraycopy(REPLACEMENT_CHARS, 0, HTML_SAFE_REPLACEMENT_CHARS, 0, HTML_SAFE_REPLACEMENT_CHARS.length);
  HTML_SAFE_REPLACEMENT_CHARS[60] = '\\u003c';
  HTML_SAFE_REPLACEMENT_CHARS[62] = '\\u003e';
  HTML_SAFE_REPLACEMENT_CHARS[38] = '\\u0026';
  HTML_SAFE_REPLACEMENT_CHARS[61] = '\\u003d';
  HTML_SAFE_REPLACEMENT_CHARS[39] = '\\u0027';
}

function $beforeName(this$static){
  var context;
  context = $peek_0(this$static);
  if (context == 5) {
    $append_0(this$static.out, 44);
  }
   else if (context != 3) {
    throw toJs(new IllegalStateException_0('Nesting problem.'));
  }
  this$static.stack_0[this$static.stackSize - 1] = 4;
}

function $beforeValue(this$static){
  switch ($peek_0(this$static)) {
    case 7:
      if (!this$static.lenient) {
        throw toJs(new IllegalStateException_0('JSON must have only one top-level value.'));
      }

    case 6:
      this$static.stack_0[this$static.stackSize - 1] = 7;
      break;
    case 1:
      this$static.stack_0[this$static.stackSize - 1] = 2;
      break;
    case 2:
      $append_0(this$static.out, 44);
      break;
    case 4:
      $append_3(this$static.out, this$static.separator);
      this$static.stack_0[this$static.stackSize - 1] = 5;
      break;
    default:throw toJs(new IllegalStateException_0('Nesting problem.'));
  }
}

function $close_0(this$static, empty, nonempty, closeBracket){
  var context;
  context = $peek_0(this$static);
  if (context != nonempty && context != empty) {
    throw toJs(new IllegalStateException_0('Nesting problem.'));
  }
  if (this$static.deferredName != null) {
    throw toJs(new IllegalStateException_0('Dangling name: ' + this$static.deferredName));
  }
  --this$static.stackSize;
  $append_0(this$static.out, closeBracket);
  return this$static;
}

function $name_0(this$static, name_0){
  requireNonNull_0(name_0, 'name == null');
  if (this$static.deferredName != null) {
    throw toJs(new IllegalStateException);
  }
  if (this$static.stackSize == 0) {
    throw toJs(new IllegalStateException_0('JsonWriter is closed.'));
  }
  this$static.deferredName = name_0;
  return this$static;
}

function $nullValue(this$static){
  if (this$static.deferredName != null) {
    if (this$static.serializeNulls) {
      $writeDeferredName(this$static);
    }
     else {
      this$static.deferredName = null;
      return this$static;
    }
  }
  $beforeValue(this$static);
  $append_3(this$static.out, 'null');
  return this$static;
}

function $open(this$static, empty, openBracket){
  $beforeValue(this$static);
  this$static.stackSize == this$static.stack_0.length && (this$static.stack_0 = copyOf(this$static.stack_0, this$static.stackSize * 2));
  this$static.stack_0[this$static.stackSize++] = empty;
  $append_0(this$static.out, openBracket);
  return this$static;
}

function $peek_0(this$static){
  if (this$static.stackSize == 0) {
    throw toJs(new IllegalStateException_0('JsonWriter is closed.'));
  }
  return this$static.stack_0[this$static.stackSize - 1];
}

function $string(this$static, value_0){
  var c, i, last, length_0, replacement, replacements;
  replacements = this$static.htmlSafe?HTML_SAFE_REPLACEMENT_CHARS:REPLACEMENT_CHARS;
  $append_0(this$static.out, 34);
  last = 0;
  length_0 = value_0.length;
  for (i = 0; i < length_0; i++) {
    c = (checkCriticalStringElementIndex(i, value_0.length) , value_0.charCodeAt(i));
    if (c < 128) {
      replacement = replacements[c];
      if (replacement == null) {
        continue;
      }
    }
     else if (c == 8232) {
      replacement = '\\u2028';
    }
     else if (c == 8233) {
      replacement = '\\u2029';
    }
     else {
      continue;
    }
    last < i && $append_5(this$static.out, value_0, last, i);
    $append_3(this$static.out, replacement);
    last = i + 1;
  }
  last < length_0 && $append_5(this$static.out, value_0, last, length_0);
  $append_0(this$static.out, 34);
}

function $value(this$static, value_0){
  if (value_0 == null) {
    return $nullValue(this$static);
  }
  $writeDeferredName(this$static);
  $beforeValue(this$static);
  $append_3(this$static.out, (checkCriticalNotNull(value_0) , value_0)?'true':'false');
  return this$static;
}

function $value_0(this$static, value_0){
  var string;
  if (value_0 == null) {
    return $nullValue(this$static);
  }
  $writeDeferredName(this$static);
  string = toString_8(value_0);
  if ($equals_0(string, '-Infinity') || $equals_0(string, 'Infinity') || $equals_0(string, 'NaN')) {
    if (!this$static.lenient) {
      throw toJs(new IllegalArgumentException('Numeric values must be finite, but was ' + string));
    }
  }
  $beforeValue(this$static);
  $append_3(this$static.out, string);
  return this$static;
}

function $value_1(this$static, value_0){
  if (value_0 == null) {
    return $nullValue(this$static);
  }
  $writeDeferredName(this$static);
  $beforeValue(this$static);
  $string(this$static, value_0);
  return this$static;
}

function $writeDeferredName(this$static){
  if (this$static.deferredName != null) {
    $beforeName(this$static);
    $string(this$static, this$static.deferredName);
    this$static.deferredName = null;
  }
}

function JsonWriter(out){
  $clinit_JsonWriter();
  this.stack_0 = initUnidimensionalArray(I_classLit, $intern_4, 46, 32, 15, 1);
  this.stackSize == this.stack_0.length && (this.stack_0 = copyOf(this.stack_0, this.stackSize * 2));
  this.stack_0[this.stackSize++] = 6;
  this.out = castTo(requireNonNull_0(out, 'out == null'), 243);
}

defineClass(392, 1, {}, JsonWriter);
_.close_0 = function close_4(){
  var size_0;
  size_0 = this.stackSize;
  if (size_0 > 1 || size_0 == 1 && this.stack_0[size_0 - 1] != 7) {
    throw toJs(new IOException('Incomplete document'));
  }
  this.stackSize = 0;
}
;
_.htmlSafe = false;
_.lenient = false;
_.separator = ':';
_.serializeNulls = true;
_.stackSize = 0;
var HTML_SAFE_REPLACEMENT_CHARS, REPLACEMENT_CHARS;
var Lcom_top_1logic_common_json_gstream_JsonWriter_2_classLit = createForClass('com.top_logic.common.json.gstream', 'JsonWriter', 392);
function IOException(message){
  Exception.call(this, message);
}

function IOException_0(throwable){
  Exception_0.call(this, throwable);
}

defineClass(32, 93, $intern_16, IOException, IOException_0);
var Ljava_io_IOException_2_classLit = createForClass('java.io', 'IOException', 32);
function MalformedJsonException(msg){
  IOException.call(this, msg);
}

defineClass(220, 32, $intern_16, MalformedJsonException);
var Lcom_top_1logic_common_json_gstream_MalformedJsonException_2_classLit = createForClass('com.top_logic.common.json.gstream', 'MalformedJsonException', 220);
function $add(this$static, listener){
  $copyWhenIterating(this$static);
  $add_0(this$static._listeners, listener);
}

function $copyWhenIterating(this$static){
  if (this$static._iterating) {
    this$static._listeners = new ArrayList_1(this$static._listeners);
    this$static._iterating = false;
  }
}

function $notifyListeners(this$static, operation, event_0){
  var before, listener, listener$iterator, before_0;
  before = (before_0 = this$static._iterating , this$static._iterating = true , before_0);
  try {
    for (listener$iterator = new ArrayList$1(this$static._listeners); listener$iterator.i < listener$iterator.this$01.array.length;) {
      listener = $next_1(listener$iterator);
      operation.notifyListener(listener, event_0);
    }
  }
   finally {
    this$static._iterating && (this$static._iterating = before);
  }
}

function ListenerContainer(){
  this._listeners = new ArrayList;
}

defineClass(215, 1, {});
_._iterating = false;
var Lcom_top_1logic_common_remote_event_ListenerContainer_2_classLit = createForClass('com.top_logic.common.remote.event', 'ListenerContainer', 215);
function readMap(reader, values){
  var key, value_0;
  $beginObject(reader);
  while ($hasNext(reader)) {
    key = $nextName(reader);
    value_0 = readValue(reader);
    key == null?$put_2(values.hashCodeMap, null, value_0):$put_3(values.stringMap, key, value_0);
  }
  $endObject(reader);
}

function readValue(reader){
  var list, token;
  token = $peek(reader);
  switch (token.ordinal_0) {
    case 0:
      list = new ArrayList;
      $beginArray(reader);
      while ($hasNext(reader)) {
        $add_0(list, readValue(reader));
      }

      $endArray(reader);
      return list;
    case 2:
      return readRef(reader);
    case 7:
      return $clinit_Boolean() , $nextBoolean(reader)?true:false;
    case 8:
      $nextNull(reader);
      return null;
    case 6:
      return $nextDouble(reader);
    case 5:
      return $nextString(reader);
    default:throw toJs(new IllegalArgumentException('Unexpected token: ' + token));
  }
}

function writeList(writer, collection){
  var entry, entry$iterator;
  $writeDeferredName(writer);
  $open(writer, 1, 91);
  for (entry$iterator = collection.iterator(); entry$iterator.hasNext_0();) {
    entry = entry$iterator.next_1();
    writeValue(writer, entry);
  }
  $close_0(writer, 1, 2, 93);
}

function writeValue(writer, value_0){
  var r;
  if (instanceOfString(value_0)) {
    $value_1(writer, castToString(value_0));
  }
   else if (instanceOf(value_0, 63)) {
    $value_0(writer, castTo(value_0, 63));
  }
   else if (instanceOfBoolean(value_0)) {
    $value(writer, castToBoolean(value_0));
  }
   else if (value_0 == null) {
    $nullValue(writer);
  }
   else if (instanceOf(value_0, 20)) {
    writeList(writer, castTo(value_0, 20));
  }
   else if (instanceOf(value_0, 6)) {
    r = castTo(value_0, 6);
    $writeTo(new Ref(r._id), writer);
  }
   else if (instanceOf(value_0, 80)) {
    castTo(value_0, 80).writeTo(writer);
  }
   else {
    throw toJs(new UnsupportedOperationException_0('Cannot serializes value: ' + value_0));
  }
}

function $writeTo(this$static, writer){
  $writeDeferredName(writer);
  $open(writer, 3, 123);
  $name_0(writer, 'id');
  $value_1(writer, this$static._id);
  $close_0(writer, 3, 5, 125);
}

function Ref(id_0){
  this._id = id_0;
}

function readRef(reader){
  var id_0, property;
  $beginObject(reader);
  id_0 = null;
  while ($hasNext(reader)) {
    property = $nextName(reader);
    switch (property) {
      case 'id':
        id_0 = $nextString(reader);
        break;
      default:throw toJs(new IllegalArgumentException('Unexpected property: ' + property));
    }
  }
  $endObject(reader);
  return new Ref(id_0);
}

defineClass(77, 1, {80:1, 77:1}, Ref);
_.writeTo = function writeTo(writer){
  $writeTo(this, writer);
}
;
var Lcom_top_1logic_common_remote_json_Ref_2_classLit = createForClass('com.top_logic.common.remote.json', 'Ref', 77);
defineClass(323, 1, {58:1});
var Lcom_top_1logic_common_remote_listener_AbstractAttributeObservable_2_classLit = createForClass('com.top_logic.common.remote.listener', 'AbstractAttributeObservable', 323);
function $addDependencies(this$static, ref, dependencies){
  var id_0, old;
  id_0 = ref._id;
  $hasStringValue(this$static._createsById, id_0) && (old = dependencies.map_0.put(id_0, dependencies) , old == null);
}

function $addDependencies_0(this$static, values, dependencies){
  var innerValue, innerValue$iterator;
  for (innerValue$iterator = values.iterator(); innerValue$iterator.hasNext_0();) {
    innerValue = innerValue$iterator.next_1();
    instanceOf(innerValue, 77) && $addDependencies(this$static, castTo(innerValue, 77), dependencies);
  }
}

function $calcDirectDependencies(this$static, changes){
  var directDependenciesById, update, update$iterator;
  directDependenciesById = new HashMap;
  for (update$iterator = new ArrayList$1(changes._updates); update$iterator.i < update$iterator.this$01.array.length;) {
    update = castTo($next_1(update$iterator), 70);
    $putStringValue(directDependenciesById, update._id, $extractDirectDependencies(this$static, update));
  }
  return directDependenciesById;
}

function $extractDirectDependencies(this$static, update){
  var dependencies, entry, outerIter, value_0, value$iterator;
  dependencies = new HashSet;
  for (value$iterator = (outerIter = (new AbstractMap$2(update._values)).this$01.entrySet_0().iterator() , new AbstractMap$2$1(outerIter)); value$iterator.val$outerIter2.hasNext_0();) {
    value_0 = (entry = castTo(value$iterator.val$outerIter2.next_1(), 22) , entry.getValue());
    instanceOf(value_0, 77)?$addDependencies(this$static, castTo(value_0, 77), dependencies):instanceOf(value_0, 20) && $addDependencies_0(this$static, castTo(value_0, 20), dependencies);
  }
  return dependencies;
}

function $getChangesById(changes){
  var update, update$iterator, updatesById;
  updatesById = new HashMap;
  for (update$iterator = new ArrayList$1(changes); update$iterator.i < update$iterator.this$01.array.length;) {
    update = castTo($next_1(update$iterator), 49);
    $putStringValue(updatesById, update._id, update);
  }
  return updatesById;
}

function $getDependencies(this$static, id_0){
  return castTo($getOrDefault(this$static._directDependencies, id_0, ($clinit_Collections() , $clinit_Collections() , EMPTY_SET)), 39);
}

function $lambda$0(this$static, id_0){
  return castTo($getStringValue(this$static._updatesById, id_0), 70);
}

function $lambda$2(this$static, id_0){
  return new StreamImpl(null, new Spliterators$IteratorSpliterator(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_common_remote_update_Change_2_classLit, 1), $intern_0, 49, 0, [castTo($getStringValue(this$static._createsById, id_0), 49), castTo($getStringValue(this$static._updatesById, id_0), 49)])), 16));
}

function $sort(this$static){
  var result, updateIds;
  result = new ArrayList;
  $addAll_1(result, castTo($collect($filter($flatMap(new StreamImpl(null, new Spliterators$IteratorSpliterator(topsort(new ChangeDependencySorter$1methodref$getDependencies$Type(this$static), new AbstractMap$1(this$static._createsById)), 16)), new ChangeDependencySorter$lambda$2$Type(this$static)), new ChangeDependencySorter$lambda$3$Type), of_1(new Collectors$21methodref$ctor$Type, new Collectors$20methodref$add$Type, new Collectors$lambda$42$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , IDENTITY_FINISH)]))), 13));
  $addAll_1(result, castTo($collect($filter($map_0(new StreamImpl(null, new Spliterators$IteratorSpliterator(topsort(new ChangeDependencySorter$0methodref$getDependencies$Type(this$static), (updateIds = new HashSet_1(new AbstractMap$1(this$static._updatesById)) , $removeAll(updateIds, new AbstractMap$1(this$static._createsById)) , updateIds)), 16)), new ChangeDependencySorter$lambda$0$Type(this$static)), new ChangeDependencySorter$lambda$1$Type), of_1(new Collectors$21methodref$ctor$Type, new Collectors$20methodref$add$Type, new Collectors$lambda$42$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [IDENTITY_FINISH]))), 13));
  $addAll_1(result, this$static._deletes);
  return result;
}

function ChangeDependencySorter(changes){
  this._createsById = $getChangesById(changes._creates);
  this._updatesById = $getChangesById(changes._updates);
  this._deletes = changes._deletes;
  this._directDependencies = $calcDirectDependencies(this, changes);
}

defineClass(367, 1, {}, ChangeDependencySorter);
var Lcom_top_1logic_common_remote_shared_ChangeDependencySorter_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ChangeDependencySorter', 367);
function ChangeDependencySorter$0methodref$getDependencies$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(372, 1, {}, ChangeDependencySorter$0methodref$getDependencies$Type);
_.apply_0 = function apply_6(arg0){
  return $getDependencies(this.$$outer_0, castToString(arg0));
}
;
var Lcom_top_1logic_common_remote_shared_ChangeDependencySorter$0methodref$getDependencies$Type_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ChangeDependencySorter/0methodref$getDependencies$Type', 372);
function ChangeDependencySorter$1methodref$getDependencies$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(373, 1, {}, ChangeDependencySorter$1methodref$getDependencies$Type);
_.apply_0 = function apply_7(arg0){
  return $getDependencies(this.$$outer_0, castToString(arg0));
}
;
var Lcom_top_1logic_common_remote_shared_ChangeDependencySorter$1methodref$getDependencies$Type_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ChangeDependencySorter/1methodref$getDependencies$Type', 373);
function ChangeDependencySorter$lambda$0$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(368, 1, {}, ChangeDependencySorter$lambda$0$Type);
_.apply_0 = function apply_8(arg0){
  return $lambda$0(this.$$outer_0, castToString(arg0));
}
;
var Lcom_top_1logic_common_remote_shared_ChangeDependencySorter$lambda$0$Type_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ChangeDependencySorter/lambda$0$Type', 368);
function ChangeDependencySorter$lambda$1$Type(){
}

defineClass(369, 1, {}, ChangeDependencySorter$lambda$1$Type);
_.test_0 = function test_1(arg0){
  return !!castTo(arg0, 70);
}
;
var Lcom_top_1logic_common_remote_shared_ChangeDependencySorter$lambda$1$Type_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ChangeDependencySorter/lambda$1$Type', 369);
function ChangeDependencySorter$lambda$2$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(370, 1, {}, ChangeDependencySorter$lambda$2$Type);
_.apply_0 = function apply_9(arg0){
  return $lambda$2(this.$$outer_0, castToString(arg0));
}
;
var Lcom_top_1logic_common_remote_shared_ChangeDependencySorter$lambda$2$Type_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ChangeDependencySorter/lambda$2$Type', 370);
function ChangeDependencySorter$lambda$3$Type(){
}

defineClass(371, 1, {}, ChangeDependencySorter$lambda$3$Type);
_.test_0 = function test_2(arg0){
  return !!castTo(arg0, 49);
}
;
var Lcom_top_1logic_common_remote_shared_ChangeDependencySorter$lambda$3$Type_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ChangeDependencySorter/lambda$3$Type', 371);
function $setData(this$static, property, value_0){
  var oldValue, scope_0;
  scope_0 = this$static._scope;
  oldValue = this$static.setDataRaw(property, $toData(value_0));
  (value_0 == null?oldValue == null:equals_Ljava_lang_Object__Z__devirtual$(value_0, oldValue)) || $notifyUpdate(scope_0, this$static, property);
}

function $updateProperties(this$static, values){
  var entry, entry$iterator;
  for (entry$iterator = values.entrySet_0().iterator(); entry$iterator.hasNext_0();) {
    entry = castTo(entry$iterator.next_1(), 22);
    this$static.setDataRaw(castToString(entry.getKey()), entry.getValue());
  }
}

defineClass(6, 323, {58:1, 6:1});
_.handleAttributeUpdate = function handleAttributeUpdate(property){
  throwClassCastExceptionUnlessNull($getStringValue(this._listeners_0, property));
  throwClassCastExceptionUnlessNull($get_6(this._listeners_0, null));
}
;
_.isTransient = function isTransient(property){
  return false;
}
;
var Lcom_top_1logic_common_remote_shared_ObjectData_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ObjectData', 6);
function $addReferrer(this$static, referrer, property){
  $mkReferrers(this$static, property).add(referrer);
}

function $getDataRaw(this$static, property){
  return $getStringValue(this$static._properties, property);
}

function $mkReferrers(this$static, property){
  var referrers;
  referrers = castTo($getStringValue(this$static._referrersByProperty, property), 39);
  if (!referrers) {
    referrers = new HashSet;
    $putStringValue(this$static._referrersByProperty, property, referrers);
  }
  return referrers;
}

function $removeReferrer(this$static, referrer, property){
  var referrers;
  referrers = castTo($getStringValue(this$static._referrersByProperty, property), 39);
  !!referrers && referrers.remove(referrer);
}

defineClass(41, 6, {58:1, 6:1, 41:1});
_.getDataRaw = function getDataRaw(property){
  return $getDataRaw(this, property);
}
;
_.properties = function properties_0(){
  return this._properties;
}
;
_.setDataRaw = function setDataRaw(property, rawValue){
  var oldValue;
  oldValue = $putStringValue(this._properties, property, rawValue);
  instanceOf(oldValue, 41) && $removeReferrer(castTo(oldValue, 41), this, property);
  instanceOf(rawValue, 41) && $addReferrer(castTo(rawValue, 41), this, property);
  return oldValue;
}
;
var Lcom_top_1logic_common_remote_shared_SharedObjectData_2_classLit = createForClass('com.top_logic.common.remote.shared', 'SharedObjectData', 41);
function DefaultSharedObject(scope_0){
  this._listeners_0 = new HashMap;
  this._scope = scope_0;
  $notifyCreate(this._scope, this);
  this._properties = new HashMap;
  this._referrersByProperty = new HashMap;
}

defineClass(42, 41, {58:1, 42:1, 6:1, 45:1, 41:1});
_.data_0 = function data_1(){
  return this;
}
;
_.handle = function handle(){
  return this;
}
;
var Lcom_top_1logic_common_remote_shared_DefaultSharedObject_2_classLit = createForClass('com.top_logic.common.remote.shared', 'DefaultSharedObject', 42);
function $addAll(this$static, c){
  var changed, e, e$iterator;
  checkCriticalNotNull(c);
  changed = false;
  for (e$iterator = c.iterator(); e$iterator.hasNext_0();) {
    e = e$iterator.next_1();
    changed = changed | this$static.add(e);
  }
  return changed;
}

function $advanceToFind(this$static, o, remove){
  var e, iter;
  for (iter = this$static.iterator(); iter.hasNext_0();) {
    e = iter.next_1();
    if (maskUndefined(o) === maskUndefined(e) || o != null && equals_Ljava_lang_Object__Z__devirtual$(o, e)) {
      remove && iter.remove_0();
      return true;
    }
  }
  return false;
}

function $containsAll(this$static, c){
  var e, e$iterator;
  checkCriticalNotNull(c);
  for (e$iterator = c.iterator(); e$iterator.hasNext_0();) {
    e = e$iterator.next_1();
    if (!this$static.contains(e)) {
      return false;
    }
  }
  return true;
}

defineClass(485, 1, $intern_17);
_.spliterator_0 = function spliterator_0(){
  return new Spliterators$IteratorSpliterator(this, 0);
}
;
_.stream = function stream(){
  return new StreamImpl(null, this.spliterator_0());
}
;
_.add = function add_0(o){
  throw toJs(new UnsupportedOperationException_0('Add not supported on this collection'));
}
;
_.addAll = function addAll(c){
  return $addAll(this, c);
}
;
_.clear = function clear_0(){
  var iter;
  for (iter = this.iterator(); iter.hasNext_0();) {
    iter.next_1();
    iter.remove_0();
  }
}
;
_.contains = function contains(o){
  return $advanceToFind(this, o, false);
}
;
_.containsAll = function containsAll(c){
  return $containsAll(this, c);
}
;
_.isEmpty = function isEmpty(){
  return this.size() == 0;
}
;
_.remove = function remove_0(o){
  return $advanceToFind(this, o, true);
}
;
_.removeAll = function removeAll(c){
  var changed, iter, o;
  checkCriticalNotNull(c);
  changed = false;
  for (iter = this.iterator(); iter.hasNext_0();) {
    o = iter.next_1();
    if (c.contains(o)) {
      iter.remove_0();
      changed = true;
    }
  }
  return changed;
}
;
_.retainAll = function retainAll(c){
  var changed, iter, o;
  checkCriticalNotNull(c);
  changed = false;
  for (iter = this.iterator(); iter.hasNext_0();) {
    o = iter.next_1();
    if (!c.contains(o)) {
      iter.remove_0();
      changed = true;
    }
  }
  return changed;
}
;
_.toArray = function toArray(){
  return this.toArray_0(initUnidimensionalArray(Ljava_lang_Object_2_classLit, $intern_0, 1, this.size(), 5, 1));
}
;
_.toArray_0 = function toArray_0(a){
  var i, it, result, size_0;
  size_0 = this.size();
  a.length < size_0 && (a = stampJavaTypeInfo_1(new Array(size_0), a));
  result = a;
  it = this.iterator();
  for (i = 0; i < size_0; ++i) {
    setCheck(result, i, it.next_1());
  }
  a.length > size_0 && setCheck(a, size_0, null);
  return a;
}
;
_.toString_0 = function toString_10(){
  var e, e$iterator, joiner;
  joiner = new StringJoiner(', ', '[', ']');
  for (e$iterator = this.iterator(); e$iterator.hasNext_0();) {
    e = e$iterator.next_1();
    $add_3(joiner, e === this?'(this Collection)':e == null?'null':toString_8(e));
  }
  return !joiner.builder?joiner.emptyValue:joiner.suffix.length == 0?joiner.builder.string:joiner.builder.string + ('' + joiner.suffix);
}
;
var Ljava_util_AbstractCollection_2_classLit = createForClass('java.util', 'AbstractCollection', 485);
function HandleCollectionWrapper(objs){
  this._objs = objs;
}

defineClass(116, 485, $intern_17, HandleCollectionWrapper);
_.iterator = function iterator(){
  var base;
  base = this._objs.iterator();
  return new HandleCollectionWrapper$1(base);
}
;
_.size = function size_1(){
  return this._objs.size();
}
;
var Lcom_top_1logic_common_remote_shared_HandleCollectionWrapper_2_classLit = createForClass('com.top_logic.common.remote.shared', 'HandleCollectionWrapper', 116);
function $forEachRemaining(this$static, consumer){
  checkCriticalNotNull(consumer);
  while (this$static.hasNext_0()) {
    $accept_0(consumer, this$static.next_1());
  }
}

function $remove_6(){
  throw toJs(new UnsupportedOperationException);
}

function HandleCollectionWrapper$1(val$base){
  this.val$base2 = val$base;
}

defineClass(390, 1, {}, HandleCollectionWrapper$1);
_.forEachRemaining = function forEachRemaining(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_0(){
  return castTo(this.val$base2.next_1(), 6).handle();
}
;
_.hasNext_0 = function hasNext_0(){
  return this.val$base2.hasNext_0();
}
;
_.remove_0 = function remove_1(){
  this.val$base2.remove_0();
}
;
var Lcom_top_1logic_common_remote_shared_HandleCollectionWrapper$1_2_classLit = createForClass('com.top_logic.common.remote.shared', 'HandleCollectionWrapper/1', 390);
function $setLastId(this$static, lastId){
  var lastInt;
  lastInt = __parseAndValidateInt(lastId);
  this$static._lastId = $wnd.Math.max(this$static._lastId, lastInt);
}

function IntIdSource(){
}

defineClass(354, 1, {}, IntIdSource);
_._lastId = 0;
var Lcom_top_1logic_common_remote_shared_IntIdSource_2_classLit = createForClass('com.top_logic.common.remote.shared', 'IntIdSource', 354);
function $addListener(this$static, listener){
  $add(this$static._listeners, listener);
}

function $checkCreate(this$static, obj){
  if (!$containsKey_0(this$static._operations, obj) && !$containsKey(this$static._changes, obj)) {
    return;
  }
  if (maskUndefined($get_8(this$static._operations, obj)) === maskUndefined(($clinit_Operation() , DELETE))) {
    throw toJs(new UnsupportedOperationException_0('Re-creating a deleted object is not supported.'));
  }
  throw toJs(new IllegalStateException_0('The object has already been created.'));
}

function $checkDelete(obj){
  if (obj._id == null) {
    throw toJs(new IllegalArgumentException('Object is not part of this scope.'));
  }
}

function $delete(this$static, id_0){
  var obj;
  obj = castTo($get_8(this$static._objById, id_0), 6);
  if (!obj) {
    throw toJs(new IllegalArgumentException('Dropping undefined ID: ' + id_0));
  }
  obj.onDelete();
  $remove_7(this$static._objById, id_0);
}

function $fireCreateEvents(this$static, creates){
  var create, create$iterator;
  for (create$iterator = new ArrayList$1(creates); create$iterator.i < create$iterator.this$01.array.length;) {
    create = castTo($next_1(create$iterator), 113);
    $notifyCreate_0(this$static._listeners, $obj(this$static, create._id).handle());
  }
}

function $fireDeleteEvents(this$static, deletes){
  var delete_0, delete$iterator;
  for (delete$iterator = new ArrayList$1(deletes); delete$iterator.i < delete$iterator.this$01.array.length;) {
    delete_0 = castTo($next_1(delete$iterator), 114);
    $notifyDelete_0(this$static._listeners, $obj(this$static, delete_0._id).handle());
  }
}

function $fireEvents(this$static){
  var entry, object, object$iterator, outerIter;
  $notifyPrepare(this$static._listeners);
  for (object$iterator = (outerIter = (new AbstractMap$2(this$static._objById)).this$01.entrySet_0().iterator() , new AbstractMap$2$1(outerIter)); object$iterator.val$outerIter2.hasNext_0();) {
    object = (entry = castTo(object$iterator.val$outerIter2.next_1(), 22) , castTo(entry.getValue(), 6));
    $notifyCreate_0(this$static._listeners, object.handle());
  }
  $notifyPostProcess(this$static._listeners);
}

function $fireUpdateEvents(this$static, updates){
  var entry, obj, outerIter, property, property$iterator, update, update$iterator;
  for (update$iterator = new ArrayList$1(updates); update$iterator.i < update$iterator.this$01.array.length;) {
    update = castTo($next_1(update$iterator), 70);
    for (property$iterator = (outerIter = (new AbstractMap$1(update._values)).this$01.entrySet_0().iterator() , new AbstractMap$1$1(outerIter)); property$iterator.val$outerIter2.hasNext_0();) {
      property = (entry = castTo(property$iterator.val$outerIter2.next_1(), 22) , castToString(entry.getKey()));
      obj = $obj(this$static, update._id);
      $notifyUpdate_0(this$static._listeners, obj.handle(), property);
      obj.handleAttributeUpdate(property);
    }
  }
}

function $index(this$static, obj, id_0){
  var clashObj, existingId;
  existingId = obj._id;
  if (existingId != null) {
    throw toJs(new IllegalArgumentException("Object '" + obj + "' is already part of a scope."));
  }
  obj._id = id_0;
  clashObj = castTo($put_4(this$static._objById, id_0, obj), 6);
  if (clashObj) {
    $put_4(this$static._objById, id_0, clashObj);
    throw toJs(new IllegalArgumentException("ID '" + id_0 + "' is already used in this scope."));
  }
}

function $inspect(values){
  var element, element$iterator, entry, outerIter, value_0, value$iterator;
  for (value$iterator = (outerIter = (new AbstractMap$1(values)).this$01.entrySet_0().iterator() , new AbstractMap$1$1(outerIter)); value$iterator.val$outerIter2.hasNext_0();) {
    value_0 = (entry = castTo(value$iterator.val$outerIter2.next_1(), 22) , entry.getKey());
    if (instanceOf(value_0, 20)) {
      for (element$iterator = castTo(value_0, 20).iterator(); element$iterator.hasNext_0();) {
        element = element$iterator.next_1();
        instanceOf(element, 45)?castTo(element, 45).data_0():null;
      }
    }
     else {
      instanceOf(value_0, 45)?castTo(value_0, 45).data_0():null;
    }
  }
  return values;
}

function $notifyCreate(this$static, obj){
  var id_0;
  if (this$static._replay) {
    return;
  }
  $checkCreate(this$static, obj);
  id_0 = '' + ++this$static._idSource._lastId;
  $index(this$static, obj, id_0);
  $put_4(this$static._operations, obj, ($clinit_Operation() , CREATE));
  $notifyCreate_0(this$static._listeners, obj.handle());
}

function $notifyDelete(this$static, obj){
  if (this$static._replay) {
    return;
  }
  $checkDelete(obj);
  $recordDelete(this$static, obj);
  $notifyDelete_0(this$static._listeners, obj);
  $delete(this$static, obj._id);
}

function $notifyUpdate(this$static, obj, property){
  if (this$static._replay) {
    return;
  }
  obj.isTransient(property) || $recordUpdate(this$static, obj, property);
  $notifyUpdate_0(this$static._listeners, obj.handle(), property);
  obj.handleAttributeUpdate(property);
}

function $obj(this$static, id_0){
  return castTo($get_8(this$static._objById, id_0), 6);
}

function $popChanges(this$static){
  var entry, entry$iterator, entry$iterator0, obj, properties, result, result_0;
  result = new Changes;
  do {
    for (entry$iterator0 = new ArrayList$1((result_0 = new ArrayList_1(new LinkedHashMap$EntrySet(this$static._operations)) , $clear_1(this$static._operations) , result_0)); entry$iterator0.i < entry$iterator0.this$01.array.length;) {
      entry = castTo($next_1(entry$iterator0), 22);
      obj = castTo(entry.getKey(), 6);
      switch (castTo(entry.getValue(), 132).ordinal_0) {
        case 0:
          {
            $add_0(result._creates, new Create(obj._id, $getName(obj.handle().___clazz)));
            properties = obj.properties();
            properties.hashCodeMap.size_0 + properties.stringMap.size_0 == 0 || $add_0(result._updates, new Update(obj._id, $inspect(properties)));
            break;
          }

        case 1:
          {
            $add_0(result._deletes, new Delete(obj._id));
            break;
          }

      }
    }
    for (entry$iterator = new AbstractHashMap$EntrySetIterator((new AbstractHashMap$EntrySet(this$static._changes)).this$01); entry$iterator.hasNext;) {
      entry = $next_0(entry$iterator);
      obj = castTo(entry.getKey(), 6);
      $add_0(result._updates, new Update(obj._id, $inspect(extract(obj, castTo(entry.getValue(), 39)))));
    }
    $reset(this$static._changes);
  }
   while ($size(this$static._operations.map_0) != 0);
  return result;
}

function $readFrom(this$static, reader){
  $startReplay(this$static);
  try {
    $readObjectData(this$static, reader);
  }
   finally {
    this$static._replay = false;
  }
  $fireEvents(this$static);
}

function $readObjectData(this$static, reader){
  var data_0, data$iterator, entry, id_0, name_0, obj, objData, objData$iterator, objData$iterator0, objectProperties, outerIter, property, type_0, value_0, value$iterator, values;
  objectProperties = new HashMap;
  $beginObject(reader);
  while ($hasNext(reader)) {
    property = $nextName(reader);
    switch (property) {
      case 'objects':
        {
          $beginArray(reader);
          while ($hasNext(reader)) {
            id_0 = null;
            type_0 = null;
            values = new HashMap;
            $beginObject(reader);
            while ($hasNext(reader)) {
              name_0 = $nextName(reader);
              switch (name_0) {
                case 'id':
                  {
                    id_0 = $nextString(reader);
                    break;
                  }

                case 'type':
                  {
                    type_0 = $nextString(reader);
                    break;
                  }

                case 'properties':
                  {
                    readMap(reader, values);
                    break;
                  }

                default:{
                    throw toJs(new IllegalArgumentException('Unexpected property: ' + name_0));
                  }

              }
            }
            $endObject(reader);
            obj = castTo($get_8(this$static._objById, id_0), 6);
            if (!obj) {
              obj = $createHandle(type_0, this$static);
              $index(this$static, obj, id_0);
              $setLastId(this$static._idSource, id_0);
            }
            $putStringValue(objectProperties, obj._id, values);
          }
          $endArray(reader);
          break;
        }

      default:{
          throw toJs(new IllegalArgumentException('Unexpected property: ' + property));
        }

    }
  }
  $endObject(reader);
  for (data$iterator = (outerIter = (new AbstractMap$2(objectProperties)).this$01.entrySet_0().iterator() , new AbstractMap$2$1(outerIter)); data$iterator.val$outerIter2.hasNext_0();) {
    data_0 = (entry = castTo(data$iterator.val$outerIter2.next_1(), 22) , castTo(entry.getValue(), 21));
    for (value$iterator = data_0.entrySet_0().iterator(); value$iterator.hasNext_0();) {
      value_0 = castTo(value$iterator.next_1(), 22);
      value_0.setValue($resolve(this$static, value_0.getValue()));
    }
  }
  for (objData$iterator0 = new AbstractHashMap$EntrySetIterator((new AbstractHashMap$EntrySet(objectProperties)).this$01); objData$iterator0.hasNext;) {
    objData = $next_0(objData$iterator0);
    $obj(this$static, castToString(objData.getKey()));
    castTo(objData.getValue(), 21);
  }
  for (objData$iterator = new AbstractHashMap$EntrySetIterator((new AbstractHashMap$EntrySet(objectProperties)).this$01); objData$iterator.hasNext;) {
    objData = $next_0(objData$iterator);
    $updateProperties($obj(this$static, castToString(objData.getKey())), castTo(objData.getValue(), 21));
  }
}

function $recordDelete(this$static, obj){
  if (maskUndefined($get_8(this$static._operations, obj)) === maskUndefined(($clinit_Operation() , CREATE))) {
    $remove_7(this$static._operations, obj);
    return;
  }
  $put_4(this$static._operations, obj, DELETE);
  $remove_1(this$static._changes, obj);
}

function $recordUpdate(this$static, obj, property){
  var changedProperties;
  if ($containsKey_0(this$static._operations, obj)) {
    return;
  }
  changedProperties = castTo($get_6(this$static._changes, obj), 39);
  if (!changedProperties) {
    changedProperties = new HashSet;
    $put_0(this$static._changes, obj, changedProperties);
  }
  changedProperties.add(property);
}

function $resolve(this$static, value_0){
  var id_0, result;
  if (instanceOf(value_0, 77)) {
    id_0 = castTo(value_0, 77)._id;
    result = castTo($get_8(this$static._objById, id_0), 6);
    if (!result) {
      throw toJs(new IllegalArgumentException('Unresolvable object ID: ' + id_0));
    }
    return result;
  }
  if (instanceOf(value_0, 13)) {
    return $resolveList(this$static, castTo(value_0, 13));
  }
  return value_0;
}

function $resolveList(this$static, list){
  var entry, entry$iterator, result;
  result = new ArrayList_0(list.size());
  for (entry$iterator = list.iterator(); entry$iterator.hasNext_0();) {
    entry = entry$iterator.next_1();
    $add_0(result, $resolve(this$static, entry));
  }
  return result;
}

function $startReplay(this$static){
  if (this$static._replay) {
    throw toJs(new IllegalStateException_0('Already updating.'));
  }
  this$static._replay = true;
}

function $toData(value_0){
  var data_0;
  data_0 = instanceOf(value_0, 45)?castTo(value_0, 45).data_0():null;
  if (data_0) {
    return data_0;
  }
  return value_0;
}

function $update(this$static, changes){
  $notifyPrepare(this$static._listeners);
  $fireDeleteEvents(this$static, changes._deletes);
  $startReplay(this$static);
  try {
    $forEach($sort(new ChangeDependencySorter(changes)), new ObjectScope$0methodref$updateObjectDataUnsorted$Type(this$static));
  }
   finally {
    this$static._replay = false;
  }
  $fireCreateEvents(this$static, changes._creates);
  $fireUpdateEvents(this$static, changes._updates);
  $notifyPostProcess(this$static._listeners);
}

function $updateObjectData(this$static, create){
  var id_0, obj;
  obj = $createHandle(create._networkType, this$static);
  id_0 = create._id;
  $index(this$static, obj, id_0);
  $setLastId(this$static._idSource, id_0);
}

function $updateObjectData_0(this$static, update){
  var entry, entry$iterator, id_0, obj;
  id_0 = update._id;
  obj = castTo($get_8(this$static._objById, id_0), 6);
  if (!obj) {
    throw toJs(new IllegalArgumentException('Updated object not found: ' + id_0));
  }
  for (entry$iterator = new AbstractHashMap$EntrySetIterator((new AbstractHashMap$EntrySet(update._values)).this$01); entry$iterator.hasNext;) {
    entry = $next_0(entry$iterator);
    $setData(obj, castToString(entry.getKey()), $resolve(this$static, entry.getValue()));
  }
}

function $updateObjectDataUnsorted(this$static, change){
  instanceOf(change, 113) && $updateObjectData(this$static, castTo(change, 113));
  instanceOf(change, 70) && $updateObjectData_0(this$static, castTo(change, 70));
  instanceOf(change, 114) && $delete(this$static, castTo(change, 114)._id);
}

function $writeObjects(writer, objects){
  var id_0, obj, obj$iterator, value_0, value$iterator;
  for (obj$iterator = new ArrayList$1(objects); obj$iterator.i < obj$iterator.this$01.array.length;) {
    obj = castTo($next_1(obj$iterator), 6);
    id_0 = obj._id;
    $writeDeferredName(writer);
    $open(writer, 3, 123);
    $name_0(writer, 'id');
    $value_1(writer, id_0);
    $name_0(writer, 'type');
    $value_1(writer, $getName(obj.handle().___clazz));
    $name_0(writer, 'properties');
    $writeDeferredName(writer);
    $open(writer, 3, 123);
    for (value$iterator = new AbstractHashMap$EntrySetIterator((new AbstractHashMap$EntrySet(obj.properties())).this$01); value$iterator.hasNext;) {
      value_0 = $next_0(value$iterator);
      $name_0(writer, castToString(value_0.getKey()));
      writeValue(writer, value_0.getValue());
    }
    $close_0(writer, 3, 5, 125);
    $close_0(writer, 3, 5, 125);
  }
}

function extract(obj, properties){
  var property, property$iterator, values;
  values = new HashMap;
  for (property$iterator = properties.iterator(); property$iterator.hasNext_0();) {
    property = castToString(property$iterator.next_1());
    $putStringValue(values, property, obj.getDataRaw(property));
  }
  return values;
}

defineClass(320, 1, {80:1});
_.writeTo = function writeTo_0(writer){
  var created, entry, entry$iterator, objects;
  $writeDeferredName(writer);
  $open(writer, 3, 123);
  $name_0(writer, 'objects');
  $writeDeferredName(writer);
  $open(writer, 1, 91);
  objects = new ArrayList_1(new AbstractMap$2(this._objById));
  $writeObjects(writer, objects);
  while ($size(this._operations.map_0) != 0) {
    created = new ArrayList;
    for (entry$iterator = new LinkedHashMap$EntrySet$EntryIterator(new LinkedHashMap$EntrySet(this._operations)); entry$iterator.next_0 != entry$iterator.this$11.this$01.head;) {
      entry = $next_2(entry$iterator);
      maskUndefined(entry.value_0) === maskUndefined(($clinit_Operation() , CREATE)) && $add_0(created, castTo(entry.key, 6));
    }
    $clear_1(this._operations);
    $writeObjects(writer, created);
  }
  $reset(this._changes);
  $close_0(writer, 1, 2, 93);
  $close_0(writer, 3, 5, 125);
}
;
_._replay = false;
var Lcom_top_1logic_common_remote_shared_ObjectScope_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ObjectScope', 320);
function ObjectScope$0methodref$updateObjectDataUnsorted$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(322, 1, {}, ObjectScope$0methodref$updateObjectDataUnsorted$Type);
_.accept = function accept(arg0){
  $updateObjectDataUnsorted(this.$$outer_0, castTo(arg0, 49));
}
;
var Lcom_top_1logic_common_remote_shared_ObjectScope$0methodref$updateObjectDataUnsorted$Type_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ObjectScope/0methodref$updateObjectDataUnsorted$Type', 322);
function $clinit_Operation(){
  $clinit_Operation = emptyMethod;
  CREATE = new Operation('CREATE', 0);
  DELETE = new Operation('DELETE', 1);
}

function Operation(enum$name, enum$ordinal){
  Enum.call(this, enum$name, enum$ordinal);
}

function values_3(){
  $clinit_Operation();
  return stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_common_remote_shared_Operation_2_classLit, 1), $intern_0, 132, 0, [CREATE, DELETE]);
}

defineClass(132, 29, {132:1, 3:1, 33:1, 29:1}, Operation);
var CREATE, DELETE;
var Lcom_top_1logic_common_remote_shared_Operation_2_classLit = createForEnum('com.top_logic.common.remote.shared', 'Operation', 132, values_3);
function ScopeEvent(obj){
  this._obj = obj;
}

defineClass(55, 1, $intern_18);
_.toString_0 = function toString_11(){
  return $name(this.getKind()) + '(' + this._obj + ')';
}
;
var Lcom_top_1logic_common_remote_shared_ScopeEvent_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ScopeEvent', 55);
function ScopeEvent$Create(obj){
  ScopeEvent.call(this, obj);
}

defineClass(365, 55, $intern_18, ScopeEvent$Create);
_.getKind = function getKind(){
  return $clinit_ScopeEvent$Kind() , CREATE_0;
}
;
var Lcom_top_1logic_common_remote_shared_ScopeEvent$Create_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ScopeEvent/Create', 365);
function ScopeEvent$Delete(obj){
  ScopeEvent.call(this, obj);
}

defineClass(366, 55, $intern_18, ScopeEvent$Delete);
_.getKind = function getKind_0(){
  return $clinit_ScopeEvent$Kind() , DELETE_0;
}
;
var Lcom_top_1logic_common_remote_shared_ScopeEvent$Delete_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ScopeEvent/Delete', 366);
function $clinit_ScopeEvent$Kind(){
  $clinit_ScopeEvent$Kind = emptyMethod;
  PREPARE = new ScopeEvent$Kind('PREPARE', 0);
  POST_PROCESS = new ScopeEvent$Kind('POST_PROCESS', 1);
  CREATE_0 = new ScopeEvent$Kind('CREATE', 2);
  DELETE_0 = new ScopeEvent$Kind('DELETE', 3);
  UPDATE = new ScopeEvent$Kind('UPDATE', 4);
}

function ScopeEvent$Kind(enum$name, enum$ordinal){
  Enum.call(this, enum$name, enum$ordinal);
}

function values_4(){
  $clinit_ScopeEvent$Kind();
  return stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_common_remote_shared_ScopeEvent$Kind_2_classLit, 1), $intern_0, 101, 0, [PREPARE, POST_PROCESS, CREATE_0, DELETE_0, UPDATE]);
}

defineClass(101, 29, {101:1, 3:1, 33:1, 29:1}, ScopeEvent$Kind);
var CREATE_0, DELETE_0, POST_PROCESS, PREPARE, UPDATE;
var Lcom_top_1logic_common_remote_shared_ScopeEvent$Kind_2_classLit = createForEnum('com.top_logic.common.remote.shared', 'ScopeEvent/Kind', 101, values_4);
function ScopeEvent$PostProcess(){
  ScopeEvent.call(this, null);
}

defineClass(364, 55, $intern_18, ScopeEvent$PostProcess);
_.getKind = function getKind_1(){
  return $clinit_ScopeEvent$Kind() , POST_PROCESS;
}
;
var Lcom_top_1logic_common_remote_shared_ScopeEvent$PostProcess_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ScopeEvent/PostProcess', 364);
function ScopeEvent$Prepare(){
  ScopeEvent.call(this, null);
}

defineClass(363, 55, $intern_18, ScopeEvent$Prepare);
_.getKind = function getKind_2(){
  return $clinit_ScopeEvent$Kind() , PREPARE;
}
;
var Lcom_top_1logic_common_remote_shared_ScopeEvent$Prepare_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ScopeEvent/Prepare', 363);
function ScopeEvent$Update(obj, property){
  ScopeEvent.call(this, obj);
  this._property = property;
}

defineClass(149, 55, {55:1, 149:1}, ScopeEvent$Update);
_.getKind = function getKind_3(){
  return $clinit_ScopeEvent$Kind() , UPDATE;
}
;
_.toString_0 = function toString_12(){
  return $name(($clinit_ScopeEvent$Kind() , UPDATE)) + '(' + this._obj + '.' + this._property + ')';
}
;
var Lcom_top_1logic_common_remote_shared_ScopeEvent$Update_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ScopeEvent/Update', 149);
function $notifyCreate_0(this$static, obj){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $sendScopeEvent(this$static, new ScopeEvent$Create(obj));
}

function $notifyDelete_0(this$static, obj){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $sendScopeEvent(this$static, new ScopeEvent$Delete(obj));
}

function $notifyPostProcess(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $sendScopeEvent(this$static, new ScopeEvent$PostProcess);
}

function $notifyPrepare(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $sendScopeEvent(this$static, new ScopeEvent$Prepare);
}

function $notifyUpdate_0(this$static, obj, property){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $sendScopeEvent(this$static, new ScopeEvent$Update(obj, property));
}

function $sendScopeEvent(this$static, event_0){
  $notifyListeners(this$static, this$static, event_0);
}

function ScopeListeners(){
  ListenerContainer.call(this);
}

defineClass(355, 215, {159:1}, ScopeListeners);
_.notifyListener = function notifyListener(listener, event_0){
  castTo(listener, 159).handleObjectScopeEvent(castTo(event_0, 55));
}
;
_.handleObjectScopeEvent = function handleObjectScopeEvent(event_0){
  $notifyListeners(this, this, event_0);
}
;
var Lcom_top_1logic_common_remote_shared_ScopeListeners_2_classLit = createForClass('com.top_logic.common.remote.shared', 'ScopeListeners', 355);
function Change(id_0){
  this._id = castToString(requireNonNull(id_0));
}

defineClass(49, 1, {80:1, 49:1});
_.serializeContent = function serializeContent(writer){
  $name_0(writer, 'id');
  $value_1(writer, this._id);
}
;
_.writeTo = function writeTo_1(writer){
  $writeDeferredName(writer);
  $open(writer, 3, 123);
  this.serializeContent(writer);
  $close_0(writer, 3, 5, 125);
}
;
var Lcom_top_1logic_common_remote_update_Change_2_classLit = createForClass('com.top_logic.common.remote.update', 'Change', 49);
function readChanges(changes){
  var $caught_ex_4, $primary_ex_3, ex, reader;
  try {
    $primary_ex_3 = null;
    try {
      reader = new JsonReader(new StringR(changes));
      return loadChanges(reader);
    }
     catch ($e0) {
      $e0 = toJava($e0);
      if (instanceOf($e0, 11)) {
        $caught_ex_4 = $e0;
        $primary_ex_3 = $caught_ex_4;
        throw toJs($primary_ex_3);
      }
       else 
        throw toJs($e0);
    }
     finally {
      $primary_ex_3 = safeClose(reader, $primary_ex_3);
      if ($primary_ex_3)
        throw toJs($primary_ex_3);
    }
  }
   catch ($e1) {
    $e1 = toJava($e1);
    if (instanceOf($e1, 32)) {
      ex = $e1;
      throw toJs(new RuntimeException_1('Failed to read update. Cause: ' + ex.detailMessage, ex));
    }
     else 
      throw toJs($e1);
  }
}

function writeChanges(changes){
  var $caught_ex_5, $primary_ex_4, buffer, ex, jsonWriter;
  buffer = new StringBuilder;
  try {
    $primary_ex_4 = null;
    try {
      jsonWriter = new JsonWriter(buffer);
      $writeTo_0(changes, jsonWriter);
    }
     catch ($e0) {
      $e0 = toJava($e0);
      if (instanceOf($e0, 11)) {
        $caught_ex_5 = $e0;
        $primary_ex_4 = $caught_ex_5;
        throw toJs($primary_ex_4);
      }
       else 
        throw toJs($e0);
    }
     finally {
      $primary_ex_4 = safeClose(jsonWriter, $primary_ex_4);
      if ($primary_ex_4)
        throw toJs($primary_ex_4);
    }
  }
   catch ($e1) {
    $e1 = toJava($e1);
    if (instanceOf($e1, 32)) {
      ex = $e1;
      throw toJs(new RuntimeException_1('Failed to write update. Cause: ' + ex.detailMessage, ex));
    }
     else 
      throw toJs($e1);
  }
  return buffer.string;
}

function $writeTo_0(this$static, writer){
  $writeDeferredName(writer);
  $open(writer, 3, 123);
  serializeList(writer, 'creates', this$static._creates);
  serializeList(writer, 'updates', this$static._updates);
  serializeList(writer, 'deletes', this$static._deletes);
  $close_0(writer, 3, 5, 125);
}

function Changes(){
  this._creates = new ArrayList;
  this._deletes = new ArrayList;
  this._updates = new ArrayList;
}

function loadChanges(reader){
  var changes, property;
  changes = new Changes;
  $beginObject(reader);
  while ($hasNext(reader)) {
    property = $nextName(reader);
    switch (property) {
      case 'creates':
        $beginArray(reader);
        while ($hasNext(reader)) {
          $add_0(changes._creates, readCreate(reader));
        }

        $endArray(reader);
        break;
      case 'deletes':
        $beginArray(reader);
        while ($hasNext(reader)) {
          $add_0(changes._deletes, readDelete(reader));
        }

        $endArray(reader);
        break;
      case 'updates':
        $beginArray(reader);
        while ($hasNext(reader)) {
          $add_0(changes._updates, readUpdate(reader));
        }

        $endArray(reader);
        break;
      default:throw toJs(new IllegalArgumentException('Unexpected property: ' + property));
    }
  }
  $endObject(reader);
  return changes;
}

function serializeList(writer, property, creates){
  var create, create$iterator;
  $name_0(writer, property);
  $writeDeferredName(writer);
  $open(writer, 1, 91);
  for (create$iterator = new ArrayList$1(creates); create$iterator.i < create$iterator.this$01.array.length;) {
    create = castTo($next_1(create$iterator), 80);
    create.writeTo(writer);
  }
  $close_0(writer, 1, 2, 93);
}

defineClass(212, 1, {80:1}, Changes);
_.toString_0 = function toString_13(){
  return 'Changes(creates = ' + this._creates + '; ' + 'updates = ' + this._updates + '; ' + 'deletes = ' + this._deletes + ')';
}
;
_.writeTo = function writeTo_2(writer){
  $writeTo_0(this, writer);
}
;
var Lcom_top_1logic_common_remote_update_Changes_2_classLit = createForClass('com.top_logic.common.remote.update', 'Changes', 212);
function Create(id_0, networkType){
  Change.call(this, id_0);
  this._networkType = networkType;
}

function readCreate(reader){
  var id_0, property, type_0;
  id_0 = null;
  type_0 = null;
  $beginObject(reader);
  while ($hasNext(reader)) {
    property = $nextName(reader);
    switch (property) {
      case 'id':
        id_0 = $nextString(reader);
        break;
      case 'type':
        type_0 = $nextString(reader);
        break;
      default:throw toJs(new IllegalArgumentException('Unexpected property: ' + property));
    }
  }
  $endObject(reader);
  return new Create(id_0, type_0);
}

defineClass(113, 49, {80:1, 49:1, 113:1}, Create);
_.serializeContent = function serializeContent_0(writer){
  $name_0(writer, 'id');
  $value_1(writer, this._id);
  $name_0(writer, 'type');
  $value_1(writer, this._networkType);
}
;
_.toString_0 = function toString_14(){
  return 'Create(id = ' + this._id + '; type = ' + this._networkType + ')';
}
;
var Lcom_top_1logic_common_remote_update_Create_2_classLit = createForClass('com.top_logic.common.remote.update', 'Create', 113);
function Delete(id_0){
  Change.call(this, id_0);
}

function readDelete(reader){
  var id_0, property;
  id_0 = null;
  $beginObject(reader);
  while ($hasNext(reader)) {
    property = $nextName(reader);
    switch (property) {
      case 'id':
        id_0 = $nextString(reader);
        break;
      default:throw toJs(new IllegalArgumentException('Unexpected property: ' + property));
    }
  }
  $endObject(reader);
  return new Delete(id_0);
}

defineClass(114, 49, {80:1, 49:1, 114:1}, Delete);
_.toString_0 = function toString_15(){
  return 'Delete(id = ' + this._id + ')';
}
;
var Lcom_top_1logic_common_remote_update_Delete_2_classLit = createForClass('com.top_logic.common.remote.update', 'Delete', 114);
function Update(id_0, values){
  Change.call(this, id_0);
  this._values = values;
}

function readUpdate(reader){
  var id_0, property, values;
  id_0 = null;
  values = new HashMap;
  $beginObject(reader);
  while ($hasNext(reader)) {
    property = $nextName(reader);
    switch (property) {
      case 'id':
        id_0 = $nextString(reader);
        break;
      case 'values':
        readMap(reader, values);
        break;
      default:throw toJs(new IllegalArgumentException('Unexpected property: ' + property));
    }
  }
  $endObject(reader);
  return new Update(id_0, values);
}

defineClass(70, 49, {80:1, 49:1, 70:1}, Update);
_.serializeContent = function serializeContent_1(writer){
  var entry, entry$iterator, key;
  $name_0(writer, 'id');
  $value_1(writer, this._id);
  $name_0(writer, 'values');
  $writeDeferredName(writer);
  $open(writer, 3, 123);
  for (entry$iterator = new AbstractHashMap$EntrySetIterator((new AbstractHashMap$EntrySet(this._values)).this$01); entry$iterator.hasNext;) {
    entry = $next_0(entry$iterator);
    key = castToString(entry.getKey());
    $name_0(writer, key);
    writeValue(writer, entry.getValue());
  }
  $close_0(writer, 3, 5, 125);
}
;
_.toString_0 = function toString_16(){
  return 'Update(id = ' + this._id + '; values = ' + this._values + ')';
}
;
var Lcom_top_1logic_common_remote_update_Update_2_classLit = createForClass('com.top_logic.common.remote.update', 'Update', 70);
var Lcom_top_1logic_graph_common_model_GraphPart_2_classLit = createForInterface('com.top_logic.graph.common.model', 'GraphPart');
defineClass(48, 1, $intern_19);
var Lcom_top_1logic_graph_common_model_GraphEvent_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent', 48);
defineClass(582, 48, $intern_19);
var Lcom_top_1logic_graph_common_model_GraphEvent$EdgeEvent_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/EdgeEvent', 582);
function GraphEvent$EdgeChanged(){
}

defineClass(401, 582, $intern_19, GraphEvent$EdgeChanged);
var Lcom_top_1logic_graph_common_model_GraphEvent$EdgeChanged_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/EdgeChanged', 401);
function GraphEvent$EdgeCreated(){
}

defineClass(399, 582, $intern_19, GraphEvent$EdgeCreated);
var Lcom_top_1logic_graph_common_model_GraphEvent$EdgeCreated_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/EdgeCreated', 399);
function GraphEvent$EdgeDeleted(){
}

defineClass(400, 582, $intern_19, GraphEvent$EdgeDeleted);
var Lcom_top_1logic_graph_common_model_GraphEvent$EdgeDeleted_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/EdgeDeleted', 400);
defineClass(583, 48, $intern_19);
var Lcom_top_1logic_graph_common_model_GraphEvent$LabelEvent_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/LabelEvent', 583);
function GraphEvent$LabelChanged(){
}

defineClass(403, 583, $intern_19, GraphEvent$LabelChanged);
var Lcom_top_1logic_graph_common_model_GraphEvent$LabelChanged_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/LabelChanged', 403);
function GraphEvent$LabelCreated(){
}

defineClass(404, 583, $intern_19, GraphEvent$LabelCreated);
var Lcom_top_1logic_graph_common_model_GraphEvent$LabelCreated_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/LabelCreated', 404);
function GraphEvent$LabelDeleted(){
}

defineClass(402, 583, $intern_19, GraphEvent$LabelDeleted);
var Lcom_top_1logic_graph_common_model_GraphEvent$LabelDeleted_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/LabelDeleted', 402);
defineClass(581, 48, $intern_19);
var Lcom_top_1logic_graph_common_model_GraphEvent$NodeEvent_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/NodeEvent', 581);
function GraphEvent$NodeChanged(){
}

defineClass(398, 581, $intern_19, GraphEvent$NodeChanged);
var Lcom_top_1logic_graph_common_model_GraphEvent$NodeChanged_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/NodeChanged', 398);
function GraphEvent$NodeCreated(){
}

defineClass(396, 581, $intern_19, GraphEvent$NodeCreated);
var Lcom_top_1logic_graph_common_model_GraphEvent$NodeCreated_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/NodeCreated', 396);
function GraphEvent$NodeDeleted(){
}

defineClass(397, 581, $intern_19, GraphEvent$NodeDeleted);
var Lcom_top_1logic_graph_common_model_GraphEvent$NodeDeleted_2_classLit = createForClass('com.top_logic.graph.common.model', 'GraphEvent/NodeDeleted', 397);
function $delete_0(this$static){
  this$static.onDelete();
  $notifyDelete(this$static._scope, this$static);
  $setData(this$static, 'graph', null);
}

function $setTag(this$static, newValue){
  var oldValue, result;
  this$static._tag = newValue;
  oldValue = this$static._tag;
  $updateBusinessObject((result = $getStringValue(this$static._properties, 'graph') , castTo(instanceOf(result, 6)?castTo(result, 6).handle():result, 99)), this$static, oldValue, newValue);
  $notifyUpdate(this$static._scope, this$static, 'tag');
}

function DefaultGraphPart(scope_0){
  DefaultSharedObject.call(this, scope_0);
}

defineClass(207, 42, {58:1, 42:1, 6:1, 45:1, 41:1, 44:1, 91:1});
_.delete_0 = function delete_1(){
  $delete_0(this);
}
;
_.getTag = function getTag(){
  return this._tag;
}
;
_.isTransient = function isTransient_0(property){
  if ($equals_0('tag', property)) {
    return true;
  }
  return false;
}
;
var Lcom_top_1logic_graph_common_model_impl_DefaultGraphPart_2_classLit = createForClass('com.top_logic.graph.common.model.impl', 'DefaultGraphPart', 207);
function $onDelete(this$static){
  var label_0, label$iterator;
  for (label$iterator = new ArrayList$1(new ArrayList_1(this$static.getLabels())); label$iterator.i < label$iterator.this$01.array.length;) {
    label_0 = castTo($next_1(label$iterator), 107);
    label_0.delete_0();
  }
}

function DefaultLabelOwner(scope_0){
  DefaultGraphPart.call(this, scope_0);
}

defineClass(208, 207, {58:1, 42:1, 6:1, 45:1, 41:1, 44:1, 122:1, 91:1});
_.getLabels = function getLabels(){
  return new HandleCollectionWrapper($mkReferrers(this, 'owner'));
}
;
_.onDelete = function onDelete(){
  $onDelete(this);
}
;
var Lcom_top_1logic_graph_common_model_impl_DefaultLabelOwner_2_classLit = createForClass('com.top_logic.graph.common.model.impl', 'DefaultLabelOwner', 208);
defineClass(145, 208, {58:1, 42:1, 6:1, 45:1, 41:1, 121:1, 44:1, 122:1, 91:1, 145:1});
_.onDelete = function onDelete_0(){
  var result;
  $onDelete(this);
  $setData(this, 'src', null);
  $setData(this, 'dst', null);
  $removeInternal((result = $getStringValue(this._properties, 'graph') , castTo(instanceOf(result, 6)?castTo(result, 6).handle():result, 99)), this);
}
;
var Lcom_top_1logic_graph_common_model_impl_DefaultEdge_2_classLit = createForClass('com.top_logic.graph.common.model.impl', 'DefaultEdge', 145);
function $getSelectedGraphParts(this$static){
  var result, selectedGraphParts;
  selectedGraphParts = (result = $getStringValue(this$static._properties, 'selectedGraphParts') , castTo(instanceOf(result, 6)?castTo(result, 6).handle():result, 20));
  if (!selectedGraphParts) {
    return $clinit_Collections() , $clinit_Collections() , EMPTY_SET;
  }
  return selectedGraphParts;
}

function $removeInternal(this$static, part){
  var tag;
  tag = part._tag;
  !!tag && $remove_1(this$static._graphPartByTag, tag);
}

function $setSelectedGraphParts(this$static, selectedGraphParts){
  $setData(this$static, 'selectedGraphParts', ($clinit_Collections() , new Collections$UnmodifiableCollection(new HashSet_1(selectedGraphParts))));
}

function $updateBusinessObject(this$static, graphPart, oldValue, newValue){
  !!oldValue && $remove_1(this$static._graphPartByTag, oldValue);
  !!newValue && $put_0(this$static._graphPartByTag, newValue, graphPart);
}

defineClass(99, 42, {58:1, 42:1, 6:1, 159:1, 45:1, 41:1, 91:1, 99:1});
_.getTag = function getTag_0(){
  return this._tag;
}
;
_.handleObjectScopeEvent = function handleObjectScopeEvent_0(event_0){
  var lastArg, lastArg0, lastArg1, target;
  if (this._internalOperation) {
    return;
  }
  switch (event_0.getKind().ordinal_0) {
    case 2:
      {
        target = event_0._obj;
        instanceOf(target, 120)?$nodeCreated((lastArg0 = this , castTo(target, 120) , lastArg0)._listeners):instanceOf(target, 121)?$edgeCreated((lastArg1 = this , castTo(target, 121) , lastArg1)._listeners):instanceOf(target, 107) && $labelCreated((lastArg = this , castTo(target, 107) , lastArg)._listeners);
        break;
      }

    case 3:
      {
        target = event_0._obj;
        instanceOf(target, 120)?$nodeDeleted((lastArg0 = this._listeners , castTo(target, 120) , lastArg0)):instanceOf(target, 121)?$edgeDeleted((lastArg1 = this._listeners , castTo(target, 121) , lastArg1)):instanceOf(target, 107) && $labelDeleted((lastArg = this._listeners , castTo(target, 107) , lastArg));
        break;
      }

    case 4:
      {
        target = event_0._obj;
        castTo(event_0, 149);
        instanceOf(target, 120)?$nodeChanged((lastArg0 = this._listeners , castTo(target, 120) , lastArg0)):instanceOf(target, 121)?$edgeChanged((lastArg1 = this._listeners , castTo(target, 121) , lastArg1)):instanceOf(target, 107) && $labelChanged((lastArg = this._listeners , castTo(target, 107) , lastArg));
        break;
      }

  }
}
;
_.onDelete = function onDelete_1(){
  throw toJs(new UnsupportedOperationException_0('The graph itself cannot be deleted, as it is the root of the object tree.'));
}
;
_._internalOperation = false;
var Lcom_top_1logic_graph_common_model_impl_DefaultGraphModel_2_classLit = createForClass('com.top_logic.graph.common.model.impl', 'DefaultGraphModel', 99);
defineClass(324, 207, {58:1, 42:1, 6:1, 45:1, 41:1, 44:1, 107:1, 91:1});
_.onDelete = function onDelete_2(){
  var result;
  $setData(this, 'owner', null);
  $removeInternal((result = $getStringValue(this._properties, 'graph') , castTo(instanceOf(result, 6)?castTo(result, 6).handle():result, 99)), this);
}
;
var Lcom_top_1logic_graph_common_model_impl_DefaultLabel_2_classLit = createForClass('com.top_logic.graph.common.model.impl', 'DefaultLabel', 324);
function $clearEdges(edgesView){
  var edge, edge$iterator;
  for (edge$iterator = new ArrayList$1(new ArrayList_1(edgesView)); edge$iterator.i < edge$iterator.this$01.array.length;) {
    edge = castTo($next_1(edge$iterator), 145);
    $delete_0(edge);
  }
}

defineClass(86, 208, {58:1, 42:1, 6:1, 45:1, 41:1, 44:1, 122:1, 120:1, 91:1, 86:1});
_.handleAttributeUpdate = function handleAttributeUpdate_0(property){
  var result;
  throwClassCastExceptionUnlessNull($getStringValue(this._listeners_0, property));
  throwClassCastExceptionUnlessNull($get_6(this._listeners_0, null));
  $equals_0(property, 'nodeStyle') && (result = $getStringValue(this._properties, 'nodeStyle') , instanceOf(result, 6)?castTo(result, 6).handle():result , $setData(this, 'collapsible', ($clinit_Boolean() , false)));
}
;
_.onDelete = function onDelete_3(){
  var result;
  $onDelete(this);
  $clearEdges(new HandleCollectionWrapper($mkReferrers(this, 'dst')));
  $clearEdges(new HandleCollectionWrapper($mkReferrers(this, 'src')));
  $removeInternal((result = $getStringValue(this._properties, 'graph') , castTo(instanceOf(result, 6)?castTo(result, 6).handle():result, 99)), this);
}
;
var Lcom_top_1logic_graph_common_model_impl_DefaultNode_2_classLit = createForClass('com.top_logic.graph.common.model.impl', 'DefaultNode', 86);
function $edgeChanged(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $handleGraphEvent(this$static, new GraphEvent$EdgeChanged);
}

function $edgeCreated(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $handleGraphEvent(this$static, new GraphEvent$EdgeCreated);
}

function $edgeDeleted(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $handleGraphEvent(this$static, new GraphEvent$EdgeDeleted);
}

function $handleGraphEvent(this$static, graphEvent){
  $notifyListeners(this$static, this$static, graphEvent);
}

function $labelChanged(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $handleGraphEvent(this$static, new GraphEvent$LabelChanged);
}

function $labelCreated(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $handleGraphEvent(this$static, new GraphEvent$LabelCreated);
}

function $labelDeleted(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $handleGraphEvent(this$static, new GraphEvent$LabelDeleted);
}

function $nodeChanged(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $handleGraphEvent(this$static, new GraphEvent$NodeChanged);
}

function $nodeCreated(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $handleGraphEvent(this$static, new GraphEvent$NodeCreated);
}

function $nodeDeleted(this$static){
  if (this$static._listeners.array.length == 0) {
    return;
  }
  $handleGraphEvent(this$static, new GraphEvent$NodeDeleted);
}

function $notifyListener(listener, event_0){
  $notifyListeners(listener, listener, event_0);
}

function GraphListeners(){
  ListenerContainer.call(this);
}

defineClass(356, 215, {599:1}, GraphListeners);
_.notifyListener = function notifyListener_0(listener, event_0){
  $notifyListener(castTo(listener, 599), castTo(event_0, 48));
}
;
var Lcom_top_1logic_graph_common_model_impl_GraphListeners_2_classLit = createForClass('com.top_logic.graph.common.model.impl', 'GraphListeners', 356);
function $setType_0(this$static, type_0){
  this$static.type = type_0;
}

function $setHeight_0(this$static, height){
  this$static.height = height;
}

function $setWidth_0(this$static, width_0){
  this$static.width = width_0;
}

function $setX_3(this$static, x_0){
  this$static.x = x_0;
}

function $setY_3(this$static, y_0){
  this$static.y = y_0;
}

function $setData_0(this$static, data_0){
  this$static.data = data_0;
}

function $setX_4(this$static, x_0){
  this$static.x = x_0;
}

function $setY_4(this$static, y_0){
  this$static.y = y_0;
}

function $setID(this$static, id_0){
  this$static.id = id_0;
}

function $setGraphUpdate(this$static, graphUpdate){
  return this$static.graphUpdate = graphUpdate;
}

function $setIDs(this$static, ids){
  this$static.ids = ids;
}

function $setVisibility_0(this$static, isVisible){
  this$static.visibility = isVisible;
}

function ModuleEntry$lambda$0$Type(){
}

defineClass(237, 1, {190:1}, ModuleEntry$lambda$0$Type);
_.createControl = function createControl(arg0){
  return new DiagramJSGraphControl(arg0);
}
;
var Lcom_top_1logic_graph_diagramjs_client_boot_ModuleEntry$lambda$0$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.boot', 'ModuleEntry/lambda$0$Type', 237);
function $clinit_DiagramJSGraphControl(){
  $clinit_DiagramJSGraphControl = emptyMethod;
  DOM_DRAG_OVER_EVENT_NAME = ($clinit_DragOverEvent() , $clinit_DragOverEvent() , TYPE_0).name_0;
  DOM_DROP_EVENT_NAME = ($clinit_DropEvent() , $clinit_DropEvent() , TYPE_1).name_0;
}

function $addEventListeners(this$static){
  var listeners;
  enableEvents(this$static._rootElement, stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [DOM_DROP_EVENT_NAME, DOM_DRAG_OVER_EVENT_NAME]));
  listeners = new HashMap;
  $putStringValue(listeners, DOM_DROP_EVENT_NAME, new DropEventListener(this$static));
  $putStringValue(listeners, DOM_DRAG_OVER_EVENT_NAME, ($clinit_DragOverEventListener() , INSTANCE_0));
  setEventListener(this$static._rootElement, new DispatchingEventListener(listeners));
}

function $initSharedGraphModel(this$static, jsonGraphModel){
  var $caught_ex_4, $primary_ex_3, ex, reader;
  try {
    $primary_ex_3 = null;
    try {
      reader = new JsonReader(new StringR(jsonGraphModel));
      $readFrom(this$static._scope, reader);
    }
     catch ($e0) {
      $e0 = toJava($e0);
      if (instanceOf($e0, 11)) {
        $caught_ex_4 = $e0;
        $primary_ex_3 = $caught_ex_4;
        throw toJs($primary_ex_3);
      }
       else 
        throw toJs($e0);
    }
     finally {
      $primary_ex_3 = safeClose(reader, $primary_ex_3);
      if ($primary_ex_3)
        throw toJs($primary_ex_3);
    }
  }
   catch ($e1) {
    $e1 = toJava($e1);
    if (instanceOf($e1, 32)) {
      ex = $e1;
      throw toJs(new IllegalArgumentException_0('Failed parsing graph data: ' + ex.detailMessage, ex));
    }
     else 
      throw toJs($e1);
  }
}

function $onDrop(this$static, event_0){
  var arguments_0, eventPosition, viewbox, coordinates, position;
  eventPosition = (viewbox = this$static._diagram.get('canvas').viewbox() , coordinates = $wnd.BAL.relativeMouseCoordinates(event_0, this$static._rootElement) , position = {} , $setX_1(position, coordinates.x + viewbox.x) , $setY_1(position, coordinates.y + viewbox.y) , position);
  arguments_0 = {};
  $setX_4(arguments_0, eventPosition.x);
  $setY_4(arguments_0, eventPosition.y);
  $setData_0(arguments_0, (event_0.dataTransfer || null).getData('text'));
  $sendCommand(this$static, 'graphDrop', arguments_0);
  event_0.stopPropagation();
  event_0.preventDefault();
}

function $registerDisplayGraphEventHandlers(this$static, eventBus){
  var id_0, commandStack;
  id_0 = this$static._id;
  $addEventHandler(eventBus, 'element.click', makeLambdaFunction(ClickEventHandler.prototype.call_0, ClickEventHandler, [this$static._scope._graphModel]));
  $addEventHandler(eventBus, 'connect.end', makeLambdaFunction(CreateConnectionEventHandler.prototype.call_0, CreateConnectionEventHandler, [id_0]));
  $addEventHandler(eventBus, 'create.class.property', makeLambdaFunction(CreateClassPropertyEventHandler.prototype.call_0, CreateClassPropertyEventHandler, [id_0]));
  $addEventHandler(eventBus, 'create.class', makeLambdaFunction(CreateClassEventHandler.prototype.call_0, CreateClassEventHandler, [id_0]));
  $addEventHandler(eventBus, 'create.enumeration', makeLambdaFunction(CreateEnumerationEventHandler.prototype.call_0, CreateEnumerationEventHandler, [id_0]));
  $addEventHandler(eventBus, 'delete.element', makeLambdaFunction(DeleteGraphPartEventHandler.prototype.call_0, DeleteGraphPartEventHandler, [id_0]));
  $addEventHandler(eventBus, 'element.goto', makeLambdaFunction(GoToDefinitionEventHandler.prototype.call_0, GoToDefinitionEventHandler, [id_0]));
  $addEventHandler(eventBus, 'elements.visibility', makeLambdaFunction(ElementVisibilityEventHandler.prototype.call_0, ElementVisibilityEventHandler, [id_0]));
  commandStack = this$static._diagram.get('commandStack');
  $addCommandInterceptor_0(commandStack, new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['connection.updateWaypoints', 'connection.layout'])), ($clinit_CommandExecutionPhase() , POST_EXECUTED), makeLambdaFunction(UpdatedWaypointsEventHandler.prototype.call_0, UpdatedWaypointsEventHandler, []));
  $addCommandInterceptor(commandStack, 'shape.resize', POST_EXECUTED, makeLambdaFunction(ShapeResizeEventHandler.prototype.call_0, ShapeResizeEventHandler, []));
  $addCommandInterceptor(commandStack, 'elements.move', POST_EXECUTED, makeLambdaFunction(ElementsMoveEventHandler.prototype.call_0, ElementsMoveEventHandler, []));
}

function $sendChangesToServer(this$static){
  var arguments_0;
  arguments_0 = {};
  $setGraphUpdate(arguments_0, writeChanges($popChanges(this$static._scope)));
  $sendCommand(this$static, 'updateGraph', arguments_0);
}

function $setShowHiddenElements_0(this$static, showHiddenElements){
  $setShowHiddenElements(this$static._diagram.get('layouter'), showHiddenElements);
}

function $setViewbox_0(this$static){
  var canvas, center, selectedDisplayGraphParts, size_0, canvas_0, viewbox;
  selectedDisplayGraphParts = castTo($collect($map_0($getSelectedGraphParts(this$static._scope._graphModel).stream(), new DiagramJSGraphControl$lambda$0$Type), of_1(new Collectors$21methodref$ctor$Type, new Collectors$20methodref$add$Type, new Collectors$lambda$42$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , IDENTITY_FINISH)]))), 13);
  if (selectedDisplayGraphParts.isEmpty()) {
    canvas_0 = this$static._diagram.get('canvas');
    viewbox = canvas_0.viewbox();
    viewbox.x = -90;
    viewbox.y = -20;
    canvas_0.viewbox(viewbox);
  }
   else {
    $selectInternal(this$static._diagram.get('selection'), getArray(selectedDisplayGraphParts), false);
    canvas = this$static._diagram.get('canvas');
    size_0 = canvas.getSize();
    center = getCenter_0(castTo($collect($map_0(selectedDisplayGraphParts.stream(), new DiagramJSObjectUtil$lambda$0$Type(canvas)), of_0(new Collectors$23methodref$ctor$Type, new Collectors$24methodref$add$Type, new Collectors$lambda$50$Type, new Collectors$lambda$51$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [UNORDERED, IDENTITY_FINISH]))), 20), canvas.getSize());
    $setViewbox(canvas, createBounds(center, size_0));
  }
}

function DiagramJSGraphControl(id_0){
  var root_0;
  $clinit_DiagramJSGraphControl();
  var root;
  this._id = id_0;
  this._rootElement = $getElementById($doc, id_0 + '-graph');
  this._diagram = createDiagram(this._rootElement, id_0 + '-umljs-container');
  this._scope = new DiagramJSGraphScope;
  this._updateTimer = new DiagramJSGraphControl$2(this);
  root = (root_0 = this._diagram.get('elementFactory').createRoot() , root_0.isVisible = true , root_0);
  $setRootElement(this._diagram.get('canvas'), root);
}

function enableEvents(element, eventNames){
  var eventName, eventName$array, eventName$index, eventName$max, dispatchMap, dispatcher;
  requireNonNull(element);
  for (eventName$array = eventNames , eventName$index = 0 , eventName$max = eventName$array.length; eventName$index < eventName$max; ++eventName$index) {
    eventName = eventName$array[eventName$index];
    $clinit_DOM();
    $maybeInitializeEventSystem();
    dispatchMap = bitlessEventDispatchers;
    dispatcher = dispatchMap[eventName] || dispatchMap['_default_'];
    element.addEventListener(eventName, dispatcher, false);
  }
}

defineClass(250, 169, {105:1, 183:1, 159:1}, DiagramJSGraphControl);
_.handleObjectScopeEvent = function handleObjectScopeEvent_1(event_0){
  $schedule(this._updateTimer, 150);
}
;
_.init_0 = function init_2(args){
  var scopeListener;
  scopeListener = this;
  $schedule(new DiagramJSGraphControl$1(this, args, scopeListener), 0);
}
;
_.invoke_0 = function invoke_1(command, args){
  var changes, updateJSON;
  switch (command) {
    case 'update':
      updateJSON = castToString(args[0]);
      changes = readChanges(updateJSON);
      $update(this._scope, changes);
      break;
    case 'showHiddenElements':
      $setShowHiddenElements_0(this, ($clinit_Boolean() , $equalsIgnoreCase(castToString(args[0]))));
      break;
    default:$invoke(command);
  }
}
;
var DOM_DRAG_OVER_EVENT_NAME, DOM_DROP_EVENT_NAME;
var Lcom_top_1logic_graph_diagramjs_client_service_DiagramJSGraphControl_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service', 'DiagramJSGraphControl', 250);
function DiagramJSGraphControl$1(this$0, val$args, val$scopeListener){
  this.this$01 = this$0;
  this.val$args2 = val$args;
  this.val$scopeListener3 = val$scopeListener;
  Timer.call(this);
}

defineClass(310, 204, {}, DiagramJSGraphControl$1);
_.run = function run(){
  $addListener(this.this$01._scope, new DefaultGraphScopeListener(this.this$01._diagram));
  $initSharedGraphModel(this.this$01, castToString(this.val$args2[0]));
  $setShowHiddenElements_0(this.this$01, $booleanValue(castToBoolean(this.val$args2[1])));
  $addListener(this.this$01._scope, this.val$scopeListener3);
  $registerDisplayGraphEventHandlers(this.this$01, this.this$01._diagram.get('eventBus'));
  $addEventListeners(this.this$01);
  $setViewbox_0(this.this$01);
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_DiagramJSGraphControl$1_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service', 'DiagramJSGraphControl/1', 310);
function DiagramJSGraphControl$2(this$0){
  this.this$01 = this$0;
  Timer.call(this);
}

defineClass(312, 204, {}, DiagramJSGraphControl$2);
_.run = function run_0(){
  $sendChangesToServer(this.this$01);
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_DiagramJSGraphControl$2_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service', 'DiagramJSGraphControl/2', 312);
function DiagramJSGraphControl$lambda$0$Type(){
}

defineClass(311, 1, {}, DiagramJSGraphControl$lambda$0$Type);
_.apply_0 = function apply_10(arg0){
  return $clinit_DiagramJSGraphControl() , castTo(arg0, 44).getTag();
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_DiagramJSGraphControl$lambda$0$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service', 'DiagramJSGraphControl/lambda$0$Type', 311);
function $createHandle(typeName, scope_0){
  if ($equals_0(($ensureNamesAreInitialized(Lcom_top_1logic_graph_diagramjs_model_impl_DefaultDiagramJSEdge_2_classLit) , Lcom_top_1logic_graph_diagramjs_model_impl_DefaultDiagramJSEdge_2_classLit.typeName), typeName)) {
    return new DefaultDiagramJSEdge(scope_0);
  }
   else if ($equals_0(($ensureNamesAreInitialized(Lcom_top_1logic_graph_diagramjs_model_impl_DefaultDiagramJSClassNode_2_classLit) , Lcom_top_1logic_graph_diagramjs_model_impl_DefaultDiagramJSClassNode_2_classLit.typeName), typeName)) {
    return new DefaultDiagramJSClassNode(scope_0);
  }
   else if ($equals_0(($ensureNamesAreInitialized(Lcom_top_1logic_graph_diagramjs_model_impl_DefaultDiagramJSLabel_2_classLit) , Lcom_top_1logic_graph_diagramjs_model_impl_DefaultDiagramJSLabel_2_classLit.typeName), typeName)) {
    return new DefaultDiagramJSLabel(scope_0);
  }
   else {
    throw toJs(new UnsupportedOperationException_0('No factory for: ' + typeName));
  }
}

function $getListener(this$static, eventName){
  var listener;
  listener = castTo($getStringValue(this$static._listeners, eventName), 105);
  if (listener) {
    return listener;
  }
  return null;
}

function DispatchingEventListener(listener){
  this._listeners = new HashMap;
  $putAll_0(this._listeners, listener);
}

defineClass(227, 1, {105:1}, DispatchingEventListener);
_.onBrowserEvent = function onBrowserEvent_0(event_0){
  var listener;
  listener = $getListener(this, event_0.type);
  !!listener && listener.onBrowserEvent(event_0);
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_DispatchingEventListener_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service', 'DispatchingEventListener', 227);
function ClickEventHandler(graphModel){
  this._graphModel = graphModel;
}

defineClass(619, $wnd.Function, {}, ClickEventHandler);
_.call_0 = function call_0(event_0){
  var element, elementFromEvent, sharedGraphPart;
  elementFromEvent = event_0.element || {};
  element = elementFromEvent;
  sharedGraphPart = castTo(element.sharedGraphPart, 44);
  if (sharedGraphPart) {
    $setSelectedGraphParts(this._graphModel, new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_graph_common_model_GraphPart_2_classLit, 1), $intern_0, 44, 0, [sharedGraphPart])));
  }
   else {
    if (has(element, 'labelTarget')) {
      sharedGraphPart = castTo(element.parent.sharedGraphPart, 44);
      $setSelectedGraphParts(this._graphModel, new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_graph_common_model_GraphPart_2_classLit, 1), $intern_0, 44, 0, [sharedGraphPart])));
    }
     else {
      $setSelectedGraphParts(this._graphModel, ($clinit_Collections() , $clinit_Collections() , EMPTY_LIST));
    }
  }
}
;
function CreateClassEventHandler(controlID){
  this._controlID = controlID;
}

defineClass(622, $wnd.Function, {}, CreateClassEventHandler);
_.call_0 = function call_1(event_0){
  var arguments_0, bounds, dimension, position;
  arguments_0 = {};
  bounds = event_0.context.bounds;
  position = $getPosition_0(bounds);
  dimension = $getDimension(bounds);
  $setX_3(arguments_0, position.x);
  $setY_3(arguments_0, position.y);
  $setWidth_0(arguments_0, dimension.width);
  $setHeight_0(arguments_0, dimension.height);
  $setControlID(arguments_0, this._controlID);
  arguments_0.controlCommand = 'createClass';
  $wnd.services.ajax.execute('dispatchControlCommand', arguments_0);
}
;
function CreateClassPropertyEventHandler(controlID){
  this._controlID = controlID;
}

defineClass(621, $wnd.Function, {}, CreateClassPropertyEventHandler);
_.call_0 = function call_2(event_0){
  var arguments_0;
  arguments_0 = {};
  $setID(arguments_0, castTo(event_0.context.shape.sharedGraphPart, 86)._id);
  $setControlID(arguments_0, this._controlID);
  arguments_0.controlCommand = 'createClassProperty';
  $wnd.services.ajax.execute('dispatchControlCommand', arguments_0);
}
;
function CreateConnectionEventHandler(controlID){
  this._controlID = controlID;
}

defineClass(620, $wnd.Function, {}, CreateConnectionEventHandler);
_.call_0 = function call_3(event_0){
  var arguments_0, connectionContext, sourceID, targetID;
  arguments_0 = {};
  connectionContext = event_0.context;
  sourceID = castTo(connectionContext.source.sharedGraphPart, 86)._id;
  targetID = castTo(getNonLabelElement(connectionContext.target).sharedGraphPart, 86)._id;
  $setType_0(arguments_0, connectionContext.type);
  arguments_0.sourceID = sourceID;
  arguments_0.targetID = targetID;
  $setControlID(arguments_0, this._controlID);
  arguments_0.controlCommand = 'createConnection';
  $wnd.services.ajax.execute('dispatchControlCommand', arguments_0);
}
;
function CreateEnumerationEventHandler(controlID){
  this._controlID = controlID;
}

defineClass(623, $wnd.Function, {}, CreateEnumerationEventHandler);
_.call_0 = function call_4(event_0){
  var arguments_0, bounds, dimension, position;
  arguments_0 = {};
  bounds = event_0.context.bounds;
  position = $getPosition_0(bounds);
  dimension = $getDimension(bounds);
  $setX_3(arguments_0, position.x);
  $setY_3(arguments_0, position.y);
  $setWidth_0(arguments_0, dimension.width);
  $setHeight_0(arguments_0, dimension.height);
  $setControlID(arguments_0, this._controlID);
  arguments_0.controlCommand = 'createEnumeration';
  $wnd.services.ajax.execute('dispatchControlCommand', arguments_0);
}
;
function DeleteGraphPartEventHandler(controlID){
  this._controlID = controlID;
}

defineClass(624, $wnd.Function, {}, DeleteGraphPartEventHandler);
_.call_0 = function call_5(event_0){
  var arguments_0, element;
  element = event_0.context.element;
  arguments_0 = {};
  $setID(arguments_0, castTo(element.sharedGraphPart, 42)._id);
  $setControlID(arguments_0, this._controlID);
  arguments_0.controlCommand = 'deleteGraphPart';
  $wnd.services.ajax.execute('dispatchControlCommand', arguments_0);
}
;
function $getGraphPartIDs(elements){
  var i, ids;
  ids = new ArrayList;
  for (i = 0; i < elements.length; i++) {
    $add_0(ids, castTo(elements[i].sharedGraphPart, 42)._id);
  }
  return castTo($toArray(ids, initUnidimensionalArray(Ljava_lang_String_2_classLit, $intern_3, 2, elements.length, 6, 1)), 12);
}

function ElementVisibilityEventHandler(controlID){
  this._controlID = controlID;
}

defineClass(626, $wnd.Function, {}, ElementVisibilityEventHandler);
_.call_0 = function call_6(event_0){
  var arguments_0;
  arguments_0 = {};
  $setVisibility_0(arguments_0, event_0.visibility);
  $setIDs(arguments_0, $getGraphPartIDs(event_0.elements));
  $setControlID(arguments_0, this._controlID);
  arguments_0.controlCommand = 'handleElementsVisibility';
  $wnd.services.ajax.execute('dispatchControlCommand', arguments_0);
}
;
function ElementsMoveEventHandler(){
}

defineClass(618, $wnd.Function, {}, ElementsMoveEventHandler);
_.call_0 = function call_7(event_0){
  var i, node, shape_0, shapes;
  shapes = event_0.context.shapes;
  for (i = 0; i < shapes.length; i++) {
    shape_0 = shapes[i];
    node = castTo(shape_0.sharedGraphPart, 68);
    $setData(node, 'x', shape_0.x);
    $setData(node, 'y', shape_0.y);
  }
}
;
function GoToDefinitionEventHandler(controlID){
  this._controlID = controlID;
}

defineClass(625, $wnd.Function, {}, GoToDefinitionEventHandler);
_.call_0 = function call_8(event_0){
  var arguments_0, element;
  element = event_0.context.element;
  arguments_0 = {};
  $setID(arguments_0, castTo(element.sharedGraphPart, 42)._id);
  $setControlID(arguments_0, this._controlID);
  arguments_0.controlCommand = 'gotoDefinition';
  $wnd.services.ajax.execute('dispatchControlCommand', arguments_0);
}
;
function ShapeResizeEventHandler(){
}

defineClass(617, $wnd.Function, {}, ShapeResizeEventHandler);
_.call_0 = function call_9(event_0){
  var bounds, dimension, node, position, shape_0;
  shape_0 = event_0.context.shape;
  bounds = event_0.context.newBounds;
  position = $getPosition_0(bounds);
  dimension = $getDimension(bounds);
  node = castTo(shape_0.sharedGraphPart, 68);
  $setData(node, 'height', dimension.height);
  $setData(node, 'width', dimension.width);
  $setData(node, 'x', position.x);
  $setData(node, 'y', position.y);
}
;
function UpdatedWaypointsEventHandler(){
}

defineClass(616, $wnd.Function, {}, UpdatedWaypointsEventHandler);
_.call_0 = function call_10(event_0){
  var connection, sharedGraphPart;
  connection = event_0.context.connection;
  sharedGraphPart = castTo(connection.sharedGraphPart, 76);
  $setData(sharedGraphPart, 'waypoints', castTo($collect($map_0(new StreamImpl(null, new Spliterators$IteratorSpliterator(new Arrays$ArrayList(event_0.context.connection.waypoints), 16)), new DiagramUtil$lambda$0$Type), of_1(new Collectors$21methodref$ctor$Type, new Collectors$20methodref$add$Type, new Collectors$lambda$42$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , IDENTITY_FINISH)]))), 13));
}
;
function $clinit_DragOverEventListener(){
  $clinit_DragOverEventListener = emptyMethod;
  INSTANCE_0 = new DragOverEventListener;
}

function DragOverEventListener(){
}

defineClass(394, 1, {105:1}, DragOverEventListener);
_.onBrowserEvent = function onBrowserEvent_1(event_0){
  $setDropEffect(event_0.dataTransfer || null, $toLowerCase($name(($clinit_DataTransfer$DropEffect() , COPY)), ($clinit_Locale() , ROOT)));
  event_0.stopPropagation();
  event_0.preventDefault();
}
;
var INSTANCE_0;
var Lcom_top_1logic_graph_diagramjs_client_service_event_listener_DragOverEventListener_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.event.listener', 'DragOverEventListener', 394);
function DropEventListener(graphControl){
  this._graphControl = graphControl;
}

defineClass(393, 1, {105:1}, DropEventListener);
_.onBrowserEvent = function onBrowserEvent_2(event_0){
  $onDrop(this._graphControl, event_0);
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_event_listener_DropEventListener_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.event.listener', 'DropEventListener', 393);
function DiagramJSGraphScope(){
  this._objById = new LinkedHashMap;
  this._operations = new LinkedHashMap;
  this._changes = new HashMap;
  this._idSource = new IntIdSource;
  this._listeners = new ScopeListeners;
  this._graphModel = new DefaultDiagramJSGraphModel(this);
  $popChanges(this);
}

defineClass(321, 320, {80:1}, DiagramJSGraphScope);
var Lcom_top_1logic_graph_diagramjs_client_service_scope_DiagramJSGraphScope_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope', 'DiagramJSGraphScope', 321);
function $createDisplayGraphPart(this$static, graphPart){
  var displayGraphPart;
  displayGraphPart = null;
  instanceOf(graphPart, 69)?(displayGraphPart = $createLabel_0(this$static._graphPartCreator, castTo(graphPart, 69))):instanceOf(graphPart, 68)?(displayGraphPart = $createNode(this$static._graphPartCreator, castTo(graphPart, 68))):instanceOf(graphPart, 76) && (displayGraphPart = $createEdge(this$static._graphPartCreator, castTo(graphPart, 76)));
  !!displayGraphPart && $add_2(this$static._drawingNewGraphParts, displayGraphPart);
}

function $handleResizedShapes(this$static){
  var element, elementRegistry, elements, i, shape_0;
  elementRegistry = this$static._diagram.get('elementRegistry');
  elements = elementRegistry.getAll();
  for (i = 0; i < elements.length; i++) {
    element = elements[i];
    if (has(element, 'children') && !has(element, 'waypoints')) {
      shape_0 = element;
      shape_0.isResized && $resizeShape(this$static._diagram.get('modeling'), shape_0, $getBounds(shape_0));
    }
  }
}

function $isClassifierLabel(element){
  var result;
  if (instanceOf(element, 69)) {
    return $equals_0('classifier', (result = $getDataRaw(castTo(element, 69), 'labelType') , castToString(instanceOf(result, 6)?castTo(result, 6).handle():result)));
  }
  return false;
}

function $updateDisplayGraphPart(this$static, event_0){
  var label_0, node, object, owner, parent_0, property, result, shape_0;
  object = event_0._obj;
  property = castTo(event_0, 149)._property;
  if (property == 'owner') {
    if (instanceOf(object, 69)) {
      owner = (result = $getDataRaw(castTo(object, 69), 'owner') , castTo(instanceOf(result, 6)?castTo(result, 6).handle():result, 122));
      if (owner) {
        if (instanceOf(owner, 68)) {
          node = castTo(owner, 68);
          shape_0 = node._tag;
          if (!$contains_3(this$static._drawingNewGraphParts, shape_0)) {
            label_0 = castTo(object, 69)._tag;
            $update_0(this$static._graphPartUpdater, label_0);
          }
        }
         else if (instanceOf(owner, 76)) {
          parent_0 = owner.getTag();
          if (!$contains_3(this$static._drawingNewGraphParts, parent_0)) {
            label_0 = castTo(object, 69)._tag;
            $addShape(this$static._diagram.get('canvas'), label_0, parent_0);
          }
        }
      }
    }
  }
   else 
    $equals_0('selectedGraphParts', property) && (this$static._selectedGraphParts = $getSelectedGraphParts(castTo(event_0._obj, 171)));
}

function DefaultGraphScopeListener(diagram){
  this._diagram = diagram;
  this._graphPartCreator = new DisplayGraphPartCreator(diagram);
  this._postProcesser = new DisplayGraphUpdatePostProcesser(diagram);
  this._graphPartUpdater = new DisplayGraphPartUpdater(diagram);
  this._drawingNewGraphParts = new HashSet;
  this._selectedGraphParts = new HashSet;
}

defineClass(362, 1, {159:1}, DefaultGraphScopeListener);
_.handleObjectScopeEvent = function handleObjectScopeEvent_2(event_0){
  var object, element, displayElement;
  switch (event_0.getKind().ordinal_0) {
    case 2:
      object = event_0._obj;
      instanceOf(object, 44) && $createDisplayGraphPart(this, castTo(object, 44));
      break;
    case 4:
      $updateDisplayGraphPart(this, event_0);
      break;
    case 3:
      element = castTo(event_0._obj, 44);
      displayElement = element.getTag();
      !!displayElement && ($isClassifierLabel(element) || $removeElementsInternal(this._diagram.get('modeling'), getArray(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Lcom_google_gwt_core_client_JavaScriptObject_2_classLit, 1), $intern_20, 0, 2, [displayElement])))));
      break;
    case 1:
      $finish(this._postProcesser, this._drawingNewGraphParts, this._selectedGraphParts);
      $handleResizedShapes(this);
      this._drawingNewGraphParts = new HashSet;
  }
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DefaultGraphScopeListener_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DefaultGraphScopeListener', 362);
function $createDisplayEdge(this$static, source, target, options){
  var attributes;
  attributes = {};
  attributes['source'] = source;
  attributes['target'] = target;
  $ifPresent(options, new DisplayGraphPartCreator$lambda$3$Type(attributes));
  return $createConnection(this$static._diagram.get('elementFactory'), attributes);
}

function $createDisplayEdgeSourceLabel(this$static, text_0, owner){
  var result, result0, sharedEdge;
  return sharedEdge = castTo(owner.sharedGraphPart, 76) , $createDisplayLabel(this$static, $getBounds_0(this$static, text_0), text_0, $createLabelOptions('source', null, (result0 = (result = $getStringValue(sharedEdge._properties, 'visible') , castToBoolean(instanceOf(result, 6)?castTo(result, 6).handle():result)) , result0 == null || (checkCriticalNotNull(result0) , result0))), owner);
}

function $createDisplayEdgeTargetLabel(this$static, text_0, owner){
  var result, result0, sharedEdge;
  return sharedEdge = castTo(owner.sharedGraphPart, 76) , $createDisplayLabel(this$static, $getBounds_0(this$static, text_0), text_0, $createLabelOptions('target', null, (result0 = (result = $getStringValue(sharedEdge._properties, 'visible') , castToBoolean(instanceOf(result, 6)?castTo(result, 6).handle():result)) , result0 == null || (checkCriticalNotNull(result0) , result0))), owner);
}

function $createDisplayLabel(this$static, bounds, text_0, options, owner){
  var attributes;
  attributes = {};
  assign_0(attributes, bounds);
  attributes['businessObject'] = text_0;
  $ifPresent(options, new DisplayGraphPartCreator$lambda$3$Type(attributes));
  attributes['labelTarget'] = owner;
  return $createLabel(this$static._diagram.get('elementFactory'), attributes);
}

function $createDisplayNode(this$static, bounds, name_0, options){
  var attributes;
  attributes = {};
  assign_0(attributes, bounds);
  attributes['name'] = name_0;
  $ifPresent(options, new DisplayGraphPartCreator$lambda$3$Type(attributes));
  return $createClass(this$static._diagram.get('elementFactory'), attributes);
}

function $createEdge(this$static, edge){
  var displayEdge, newWaypoints, result, result0, result1, result2, result3, result4, result5, result6, source, result0_0, target, result_0, options, result0_1, result1_0, result2_0, waypoints, result_1;
  displayEdge = (source = (result0_0 = $getStringValue(edge._properties, 'src') , castTo(instanceOf(result0_0, 6)?castTo(result0_0, 6).handle():result0_0, 86))._tag , target = (result_0 = $getStringValue(edge._properties, 'dst') , castTo(instanceOf(result_0, 6)?castTo(result_0, 6).handle():result_0, 86))._tag , $createDisplayEdge(this$static, source, target, ($clinit_Optional() , new Optional(checkCriticalNotNull((options = {} , options.sharedGraphPart = edge , $setType(options, (result0_1 = $getStringValue(edge._properties, 'edgeType') , castToString(instanceOf(result0_1, 6)?castTo(result0_1, 6).handle():result0_1))) , $setVisibility(options, (result1_0 = (result2_0 = $getStringValue(edge._properties, 'visible') , castToBoolean(instanceOf(result2_0, 6)?castTo(result2_0, 6).handle():result2_0)) , result1_0 == null || (checkCriticalNotNull(result1_0) , result1_0))) , $ifPresent((waypoints = ofNullable((result_1 = $getStringValue(edge._properties, 'waypoints') , castTo(instanceOf(result_1, 6)?castTo(result_1, 6).handle():result_1, 13))) , $map(waypoints, new DisplayGraphPartCreator$lambda$7$Type)), new DisplayGraphPartCreator$lambda$6$Type(options)) , options))))));
  if (displayEdge.waypoints == null) {
    newWaypoints = $layoutConnection(this$static._diagram.get('layouter'), displayEdge);
    setWaypoints(displayEdge, newWaypoints);
  }
  result0 = $getStringValue(edge._properties, 'sourceName');
  castToString(instanceOf(result0, 6)?castTo(result0, 6).handle():result0) != null && $createDisplayEdgeSourceLabel(this$static, (result1 = $getStringValue(edge._properties, 'sourceName') , castToString(instanceOf(result1, 6)?castTo(result1, 6).handle():result1)), displayEdge);
  result2 = $getStringValue(edge._properties, 'sourceCardinality');
  castToString(instanceOf(result2, 6)?castTo(result2, 6).handle():result2) != null && $createDisplayEdgeSourceLabel(this$static, (result3 = $getStringValue(edge._properties, 'sourceCardinality') , castToString(instanceOf(result3, 6)?castTo(result3, 6).handle():result3)), displayEdge);
  result4 = $getStringValue(edge._properties, 'targetName');
  castToString(instanceOf(result4, 6)?castTo(result4, 6).handle():result4) != null && $createDisplayEdgeTargetLabel(this$static, (result5 = $getStringValue(edge._properties, 'targetName') , castToString(instanceOf(result5, 6)?castTo(result5, 6).handle():result5)), displayEdge);
  result6 = $getStringValue(edge._properties, 'targetCardinality');
  castToString(instanceOf(result6, 6)?castTo(result6, 6).handle():result6) != null && $createDisplayEdgeTargetLabel(this$static, (result = $getStringValue(edge._properties, 'targetCardinality') , castToString(instanceOf(result, 6)?castTo(result, 6).handle():result)), displayEdge);
  $setTag(edge, displayEdge);
  return displayEdge;
}

function $createLabel_0(this$static, label_0){
  var displayLabel, owner, result, labelOptions, result0, result1, result2, labelOwner, result3, text_0, result_0;
  displayLabel = (labelOptions = $createLabelOptions((result0 = $getStringValue(label_0._properties, 'labelType') , castToString(instanceOf(result0, 6)?castTo(result0, 6).handle():result0)), label_0, (result1 = (result2 = $getStringValue(label_0._properties, 'visible') , castToBoolean(instanceOf(result2, 6)?castTo(result2, 6).handle():result2)) , result1 == null || (checkCriticalNotNull(result1) , result1))) , labelOwner = (result3 = $getStringValue(label_0._properties, 'owner') , castTo(instanceOf(result3, 6)?castTo(result3, 6).handle():result3, 122)).getTag() , text_0 = (result_0 = $getStringValue(label_0._properties, 'text') , castToString(instanceOf(result_0, 6)?castTo(result_0, 6).handle():result_0)) , $createDisplayLabel(this$static, $getBounds_0(this$static, text_0), text_0, labelOptions, labelOwner));
  owner = (result = $getStringValue(label_0._properties, 'owner') , castTo(instanceOf(result, 6)?castTo(result, 6).handle():result, 122));
  !!owner && (instanceOf(owner, 68)?$setClassPropertyLabelPosition(this$static, displayLabel, castTo(owner, 68)):instanceOf(owner, 76) && $setEdgleLabelPosition(displayLabel, castTo(owner, 600)));
  $setTag(label_0, displayLabel);
  if (owner) {
    return null;
  }
  return displayLabel;
}

function $createLabelOptions(type_0, sharedLabel, isVisible){
  var options;
  options = {};
  type_0 != null && type_0.length != 0 && (options.labelType = type_0 , undefined);
  !!sharedLabel && (options.sharedGraphPart = sharedLabel , undefined);
  options.isVisible = isVisible;
  return $clinit_Optional() , new Optional(checkCriticalNotNull(options));
}

function $createNode(this$static, node){
  var displayNode, options, result, options_0, result0, result1, result2, classModifiers, result3, stereotypes, result_0, bounds, result0_0, result1_0, result2_0, result3_0, width_0, result4, result5, textRenderer, result6, result7, result8, result9, result10, result_1;
  displayNode = (options = ($clinit_Optional() , new Optional(checkCriticalNotNull((options_0 = {} , options_0.sharedGraphPart = node , undefined , $setImported(options_0, $booleanValue((result0 = $getStringValue(node._properties, 'imported') , castToBoolean(instanceOf(result0, 6)?castTo(result0, 6).handle():result0)))) , $setVisibility(options_0, (result1 = (result2 = $getStringValue(node._properties, 'visible') , castToBoolean(instanceOf(result2, 6)?castTo(result2, 6).handle():result2)) , result1 == null || (checkCriticalNotNull(result1) , result1))) , classModifiers = (result3 = $getStringValue(node._properties, 'modifiers') , castTo(instanceOf(result3, 6)?castTo(result3, 6).handle():result3, 13)) , !!classModifiers && (options_0.modifiers = classModifiers , undefined) , stereotypes = (result_0 = $getStringValue(node._properties, 'stereotypes') , castTo(instanceOf(result_0, 6)?castTo(result_0, 6).handle():result_0, 13)) , !!stereotypes && $setStereotypes(options_0, castTo(stereotypes.toArray_0(initUnidimensionalArray(Ljava_lang_String_2_classLit, $intern_3, 2, stereotypes.size(), 6, 1)), 12)) , options_0)))) , $createDisplayNode(this$static, (bounds = {} , $setX_0(bounds, (result0_0 = (result1_0 = $getStringValue(node._properties, 'x') , castTo(instanceOf(result1_0, 6)?castTo(result1_0, 6).handle():result1_0, 63)) , result0_0 == null?0:doubleValue__D__devirtual$(result0_0))) , $setY_0(bounds, (result2_0 = (result3_0 = $getStringValue(node._properties, 'y') , castTo(instanceOf(result3_0, 6)?castTo(result3_0, 6).handle():result3_0, 63)) , result2_0 == null?0:doubleValue__D__devirtual$(result2_0))) , $setWidth(bounds, (width_0 = (result4 = (result5 = $getStringValue(node._properties, 'width') , castTo(instanceOf(result5, 6)?castTo(result5, 6).handle():result5, 63)) , result4 == null?0:doubleValue__D__devirtual$(result4)) , width_0 == 0?$wnd.Math.max((textRenderer = this$static._diagram.get('textRenderer') , $getDimensions(textRenderer, (result6 = $getStringValue(node._properties, 'name') , castToString(instanceOf(result6, 6)?castTo(result6, 6).handle():result6))).width), $doubleValue(castToDouble($orElse($getClassNodeStereotypesWidth(this$static, node), 0)))) + 5:width_0)) , $setHeight(bounds, (result7 = (result8 = $getStringValue(node._properties, 'height') , castTo(instanceOf(result8, 6)?castTo(result8, 6).handle():result8, 63)) , (result7 == null?0:doubleValue__D__devirtual$(result7)) == 0?$getDimensions(this$static._diagram.get('textRenderer'), (result9 = $getStringValue(node._properties, 'name') , castToString(instanceOf(result9, 6)?castTo(result9, 6).handle():result9))).height + 5:(result10 = (result_1 = $getStringValue(node._properties, 'height') , castTo(instanceOf(result_1, 6)?castTo(result_1, 6).handle():result_1, 63)) , result10 == null?0:doubleValue__D__devirtual$(result10)))) , bounds), (result = $getStringValue(node._properties, 'name') , castToString(instanceOf(result, 6)?castTo(result, 6).handle():result)), options));
  $setTag(node, displayNode);
  return displayNode;
}

function $getBounds_0(this$static, text_0){
  var bounds;
  bounds = {};
  bounds.x = 0;
  bounds.y = 0;
  $setDimension(bounds, $getDimensions(this$static._diagram.get('textRenderer'), text_0));
  return bounds;
}

function $getCardinalityDistance(existMarker){
  if (existMarker) {
    return 15;
  }
  return 5;
}

function $getClassNodeStereotypesWidth(this$static, node){
  var result, stereotypes, textRenderer;
  textRenderer = this$static._diagram.get('textRenderer');
  stereotypes = (result = $getStringValue(node._properties, 'stereotypes') , castTo(instanceOf(result, 6)?castTo(result, 6).handle():result, 13));
  if (stereotypes) {
    return $reduce_0($map_0(stereotypes.stream(), new DisplayGraphPartCreator$lambda$4$Type(textRenderer)), (checkCriticalNotNull(new DisplayGraphPartCreator$1methodref$compare$Type) , new BinaryOperator$lambda$0$Type));
  }
  return $clinit_Optional() , $clinit_Optional() , EMPTY;
}

function $getPropertyLabelsHeight(clazz){
  var displayLabels, i, y_0;
  displayLabels = clazz.labels;
  y_0 = 0;
  for (i = 0; i < displayLabels.length - 1; i++) {
    y_0 += displayLabels[i].height;
  }
  return y_0;
}

function $getReferencePoint(connectionSource, connectionTarget, direction, existMarker){
  var referencePoint, source, target;
  source = $transform(connectionSource);
  target = $transform(connectionTarget);
  referencePoint = getAbsoluteClosePoint(source, target, $getCardinalityDistance(existMarker));
  direction == 3 || direction == 2?($setX_1(referencePoint, referencePoint.x) , $setY_1(referencePoint, referencePoint.y + 5)):($setX_1(referencePoint, referencePoint.x + 5) , $setY_1(referencePoint, referencePoint.y));
  return referencePoint;
}

function $getStereotypesHeight(this$static, classNode){
  var result, stereotypes;
  stereotypes = (result = $getStringValue(classNode._properties, 'stereotypes') , castTo(instanceOf(result, 6)?castTo(result, 6).handle():result, 13));
  if (stereotypes) {
    return $doubleValue(castToDouble($reduce($map_0(stereotypes.stream(), new DisplayGraphPartCreator$lambda$1$Type(this$static)), 0, new DisplayGraphPartCreator$0methodref$sum$Type)));
  }
  return 0;
}

function $lambda$1(this$static, stereotype_0){
  return $getDimensions(this$static._diagram.get('textRenderer'), stereotype_0).height;
}

function $moveDisplayLabel(displayLabel, deltaPosition){
  var labelPosition;
  labelPosition = $getPosition(displayLabel);
  $move(labelPosition, deltaPosition.x, deltaPosition.y);
  $setX(displayLabel, labelPosition.x);
  $setY(displayLabel, labelPosition.y);
}

function $setClassPropertyLabelPosition(this$static, label_0, classNode){
  var clazz, newHeight, newWidth, y_0, clazz_0, y_1, result;
  clazz = classNode._tag;
  y_0 = (clazz_0 = classNode._tag , y_1 = clazz_0.y + 5 , y_1 += $getDimensions(this$static._diagram.get('textRenderer'), (result = $getStringValue(classNode._properties, 'name') , castToString(instanceOf(result, 6)?castTo(result, 6).handle():result))).height + 5 , y_1 += $getStereotypesHeight(this$static, classNode) , y_1 += $getPropertyLabelsHeight(clazz_0) , y_1);
  $setX(label_0, clazz.x + 5);
  label_0.y = y_0;
  newHeight = label_0.y - clazz.y + label_0.height;
  if (newHeight > clazz.height) {
    clazz.height = newHeight;
    clazz.isResized = true;
  }
  newWidth = 5 + label_0.width;
  if (newWidth > clazz.width) {
    clazz.width = newWidth;
    clazz.isResized = true;
  }
}

function $setEdgleLabelPosition(displayLabel, owner){
  var connection, type_0, existMarker, connectionType, waypoints, connectionSource, connectionTarget, direction, referencePosition, existMarker_0, connectionType_0, waypoints_0, connectionSource_0, connectionTarget_0, direction_0, referencePosition_0, existAnotherEdgeLabel, labelTypes, moveFactor, existMarker_1, connectionType_1, waypoints_1, connectionSource_1, connectionTarget_1, direction_1, referencePosition_1, existMarker_2, connectionType_2, waypoints_2, connectionSource_2, connectionTarget_2, direction_2, referencePosition_2, existAnotherEdgeLabel_0, labelTypes_0, moveFactor_0;
  connection = owner._tag;
  type_0 = displayLabel.labelType;
  $equals_0('sourceCard', type_0)?(existMarker = (connectionType = connection.connectionType , $equals_0('aggregation', connectionType) || $equals_0('composition', connectionType)) , waypoints = connection.waypoints , connectionSource = waypoints[0] , connectionTarget = waypoints[1] , direction = connectionSource.y == connectionTarget.y?connectionSource.x < connectionTarget.x?2:3:connectionSource.y < connectionTarget.y?1:0 , referencePosition = $getReferencePoint(connectionSource, connectionTarget, direction, existMarker) , direction == 3?$move(referencePosition, -displayLabel.width, 0):direction == 0 && $move(referencePosition, 0, -displayLabel.height) , $move(referencePosition, -displayLabel.x, -displayLabel.y) , $moveDisplayLabel(displayLabel, referencePosition) , undefined):$equals_0('sourceName', type_0)?(existMarker_0 = (connectionType_0 = connection.connectionType , $equals_0('aggregation', connectionType_0) || $equals_0('composition', connectionType_0)) , waypoints_0 = connection.waypoints , connectionSource_0 = waypoints_0[0] , connectionTarget_0 = waypoints_0[1] , direction_0 = connectionSource_0.y == connectionTarget_0.y?connectionSource_0.x < connectionTarget_0.x?2:3:connectionSource_0.y < connectionTarget_0.y?1:0 , referencePosition_0 = $getReferencePoint(connectionSource_0, connectionTarget_0, direction_0, existMarker_0) , existAnotherEdgeLabel = (labelTypes = castTo($collect($map_0(new StreamImpl(null, new Spliterators$IteratorSpliterator(new HandleCollectionWrapper($mkReferrers(owner, 'owner')), 0)), new DisplayGraphPartCreator$lambda$0$Type), of_0(new Collectors$23methodref$ctor$Type, new Collectors$24methodref$add$Type, new Collectors$lambda$50$Type, new Collectors$lambda$51$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , UNORDERED), IDENTITY_FINISH]))), 39) , labelTypes.contains('sourceCard')) , moveFactor = existAnotherEdgeLabel?2:1 , direction_0 == 3?$move(referencePosition_0, moveFactor * -displayLabel.width, 0):direction_0 == 0 && $move(referencePosition_0, 0, moveFactor * -displayLabel.height) , $move(referencePosition_0, -displayLabel.x, -displayLabel.y) , existAnotherEdgeLabel && (direction_0 == 3?($setX_1(referencePosition_0, referencePosition_0.x + -5) , $setY_1(referencePosition_0, referencePosition_0.y)):direction_0 == 2?$move(referencePosition_0, 5 + displayLabel.width, 0):direction_0 == 0?($setX_1(referencePosition_0, referencePosition_0.x) , $setY_1(referencePosition_0, referencePosition_0.y + -5)):direction_0 == 1 && $move(referencePosition_0, 0, 5 + displayLabel.height)) , $moveDisplayLabel(displayLabel, referencePosition_0) , undefined):$equals_0('targetCard', type_0)?(existMarker_1 = (connectionType_1 = connection.connectionType , $equals_0('association', connectionType_1) || $equals_0('inheritance', connectionType_1)) , waypoints_1 = connection.waypoints , connectionSource_1 = waypoints_1[waypoints_1.length - 1] , connectionTarget_1 = waypoints_1[waypoints_1.length - 2] , direction_1 = connectionSource_1.y == connectionTarget_1.y?connectionSource_1.x < connectionTarget_1.x?2:3:connectionSource_1.y < connectionTarget_1.y?1:0 , referencePosition_1 = $getReferencePoint(connectionSource_1, connectionTarget_1, direction_1, existMarker_1) , direction_1 == 3?$move(referencePosition_1, -displayLabel.width, 0):direction_1 == 0 && $move(referencePosition_1, 0, -displayLabel.height) , $move(referencePosition_1, -displayLabel.x, -displayLabel.y) , $move($getPosition(displayLabel), referencePosition_1.x, referencePosition_1.y) , $moveDisplayLabel(displayLabel, referencePosition_1) , undefined):$equals_0('targetName', type_0) && (existMarker_2 = (connectionType_2 = connection.connectionType , $equals_0('association', connectionType_2) || $equals_0('inheritance', connectionType_2)) , waypoints_2 = connection.waypoints , connectionSource_2 = waypoints_2[waypoints_2.length - 1] , connectionTarget_2 = waypoints_2[waypoints_2.length - 2] , direction_2 = connectionSource_2.y == connectionTarget_2.y?connectionSource_2.x < connectionTarget_2.x?2:3:connectionSource_2.y < connectionTarget_2.y?1:0 , referencePosition_2 = $getReferencePoint(connectionSource_2, connectionTarget_2, direction_2, existMarker_2) , existAnotherEdgeLabel_0 = (labelTypes_0 = castTo($collect($map_0(new StreamImpl(null, new Spliterators$IteratorSpliterator(new HandleCollectionWrapper($mkReferrers(owner, 'owner')), 0)), new DisplayGraphPartCreator$lambda$0$Type), of_0(new Collectors$23methodref$ctor$Type, new Collectors$24methodref$add$Type, new Collectors$lambda$50$Type, new Collectors$lambda$51$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , UNORDERED), IDENTITY_FINISH]))), 39) , labelTypes_0.contains('targetCard')) , moveFactor_0 = existAnotherEdgeLabel_0?2:1 , direction_2 == 3?$move(referencePosition_2, moveFactor_0 * -displayLabel.width, 0):direction_2 == 0 && $move(referencePosition_2, 0, moveFactor_0 * -displayLabel.height) , $move(referencePosition_2, -displayLabel.x, -displayLabel.y) , existAnotherEdgeLabel_0 && (direction_2 == 3?($setX_1(referencePosition_2, referencePosition_2.x + -5) , $setY_1(referencePosition_2, referencePosition_2.y)):direction_2 == 2?$move(referencePosition_2, 5 + displayLabel.width, 0):direction_2 == 0?($setX_1(referencePosition_2, referencePosition_2.x) , $setY_1(referencePosition_2, referencePosition_2.y + -5)):direction_2 == 1 && $move(referencePosition_2, 0, 5 + displayLabel.height)) , $move($getPosition(displayLabel), referencePosition_2.x, referencePosition_2.y) , $moveDisplayLabel(displayLabel, referencePosition_2) , undefined);
}

function $transform(waypoint){
  var position;
  position = {};
  $setX_1(position, waypoint.x);
  $setY_1(position, waypoint.y);
  return position;
}

function DisplayGraphPartCreator(diagram){
  this._diagram = diagram;
}

defineClass(377, 1, {}, DisplayGraphPartCreator);
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphPartCreator_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphPartCreator', 377);
function DisplayGraphPartCreator$0methodref$sum$Type(){
}

defineClass(379, 1, {}, DisplayGraphPartCreator$0methodref$sum$Type);
_.apply_1 = function apply_11(arg0, arg1){
  return $doubleValue(castToDouble(arg0)) + $doubleValue(castToDouble(arg1));
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphPartCreator$0methodref$sum$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphPartCreator/0methodref$sum$Type', 379);
function DisplayGraphPartCreator$1methodref$compare$Type(){
}

defineClass(381, 1, {}, DisplayGraphPartCreator$1methodref$compare$Type);
_.compare = function compare_1(arg0, arg1){
  return compare_3($doubleValue(castToDouble(arg0)), $doubleValue(castToDouble(arg1)));
}
;
_.equals_0 = function equals_1(other){
  return this === other;
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphPartCreator$1methodref$compare$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphPartCreator/1methodref$compare$Type', 381);
function DisplayGraphPartCreator$lambda$0$Type(){
}

defineClass(222, 1, {}, DisplayGraphPartCreator$lambda$0$Type);
_.apply_0 = function apply_12(arg0){
  var result;
  return result = $getDataRaw(castTo(arg0, 601), 'labelType') , castToString(instanceOf(result, 6)?castTo(result, 6).handle():result);
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphPartCreator$lambda$0$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphPartCreator/lambda$0$Type', 222);
function DisplayGraphPartCreator$lambda$1$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(378, 1, {}, DisplayGraphPartCreator$lambda$1$Type);
_.apply_0 = function apply_13(arg0){
  return $lambda$1(this.$$outer_0, castToString(arg0));
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphPartCreator$lambda$1$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphPartCreator/lambda$1$Type', 378);
function DisplayGraphPartCreator$lambda$3$Type(attributes_0){
  this.attributes_0 = attributes_0;
}

defineClass(176, 1, {}, DisplayGraphPartCreator$lambda$3$Type);
_.accept = function accept_0(arg0){
  assign_0(this.attributes_0, castToJso(arg0));
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphPartCreator$lambda$3$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphPartCreator/lambda$3$Type', 176);
function DisplayGraphPartCreator$lambda$4$Type(textRenderer_0){
  this.textRenderer_0 = textRenderer_0;
}

defineClass(380, 1, {}, DisplayGraphPartCreator$lambda$4$Type);
_.apply_0 = function apply_14(arg0){
  return $getDimensions(this.textRenderer_0, '<<' + castToString(arg0) + '>>').width;
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphPartCreator$lambda$4$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphPartCreator/lambda$4$Type', 380);
function DisplayGraphPartCreator$lambda$6$Type(options_0){
  this.options_0 = options_0;
}

defineClass(382, 1, {}, DisplayGraphPartCreator$lambda$6$Type);
_.accept = function accept_1(arg0){
  setWaypoints(this.options_0, castToJsoArray(arg0, 123));
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphPartCreator$lambda$6$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphPartCreator/lambda$6$Type', 382);
function $apply(arg0){
  return castToJsoArray(castTo($collect($map_0(castTo(arg0, 13).stream(), new DiagramUtil$lambda$1$Type), of_1(new Collectors$21methodref$ctor$Type, new Collectors$20methodref$add$Type, new Collectors$lambda$42$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , IDENTITY_FINISH)]))), 13).toArray_0(stampJavaTypeInfo(getClassLiteralForArray(Lcom_google_gwt_core_client_JavaScriptObject_2_classLit, 1), $intern_20, 0, 2, [])), 123);
}

function DisplayGraphPartCreator$lambda$7$Type(){
}

defineClass(383, 1, {}, DisplayGraphPartCreator$lambda$7$Type);
_.apply_0 = function apply_15(arg0){
  return $apply(arg0);
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphPartCreator$lambda$7$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphPartCreator/lambda$7$Type', 383);
function $update_0(this$static, displayPart){
  var label_0, owner;
  if (has(displayPart, 'labelTarget')) {
    label_0 = displayPart;
    owner = label_0.labelTarget;
    $addShape(this$static._diagram.get('canvas'), label_0, owner);
    $resizeShape(this$static._diagram.get('modeling'), owner, $getBounds(owner));
  }
}

function DisplayGraphPartUpdater(diagram){
  this._diagram = diagram;
}

defineClass(388, 1, {}, DisplayGraphPartUpdater);
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphPartUpdater_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphPartUpdater', 388);
function $addGraphPartsToCanvas(canvas, drawingGraphParts){
  var connection, displayObject, displayObject$iterator, entry, labels, outerIter, root, shape_0;
  root = (canvas.getRootElement() , undefined);
  for (displayObject$iterator = (outerIter = (new AbstractMap$1(drawingGraphParts.map_0)).this$01.entrySet_0().iterator() , new AbstractMap$1$1(outerIter)); displayObject$iterator.val$outerIter2.hasNext_0();) {
    displayObject = (entry = castTo(displayObject$iterator.val$outerIter2.next_1(), 22) , castToJso(entry.getKey()));
    if (has(displayObject, 'children') && !has(displayObject, 'waypoints')) {
      shape_0 = displayObject;
      canvas.addShape(shape_0, root);
      labels = new Arrays$ArrayList(shape_0.labels);
      $forEach_0(labels, new DisplayGraphUpdatePostProcesser$lambda$0$Type(canvas, shape_0));
    }
     else if (has(displayObject, 'waypoints')) {
      connection = displayObject;
      canvas.addConnection(connection, root);
      labels = new Arrays$ArrayList(connection.labels);
      $forEach_0(labels, new DisplayGraphUpdatePostProcesser$lambda$1$Type(canvas, connection));
    }
     else 
      has(displayObject, 'labelTarget') && (canvas.addShape(displayObject, root) , undefined);
  }
}

function $finish(this$static, drawingGraphParts, selectedGraphParts){
  var canvas;
  canvas = this$static._diagram.get('canvas');
  $addGraphPartsToCanvas(canvas, drawingGraphParts);
  $selectGraphParts(this$static, canvas, selectedGraphParts);
}

function $selectGraphParts(this$static, canvas, selectedGraphParts){
  var center, isVisible, selectedDiagrammParts, selectedElement, selectedElement$iterator, selectedElements, selection, size_0;
  selection = this$static._diagram.get('selection');
  if (selectedGraphParts.isEmpty()) {
    selectedElements = new Arrays$ArrayList(selection.get());
    for (selectedElement$iterator = new AbstractList$IteratorImpl(selectedElements); selectedElement$iterator.i < selectedElement$iterator.this$01.size();) {
      selectedElement = (checkCriticalElement(selectedElement$iterator.i < selectedElement$iterator.this$01.size()) , castToJso(selectedElement$iterator.this$01.getAtIndex(selectedElement$iterator.last = selectedElement$iterator.i++)));
      selection.deselect(selectedElement);
    }
  }
   else {
    selectedDiagrammParts = castTo($collect($map_0(selectedGraphParts.stream(), new DisplayGraphUpdatePostProcesser$lambda$2$Type), of_1(new Collectors$21methodref$ctor$Type, new Collectors$20methodref$add$Type, new Collectors$lambda$42$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , IDENTITY_FINISH)]))), 13);
    isVisible = $inViewbox_1(canvas, selectedDiagrammParts);
    if (!isVisible) {
      size_0 = canvas.getSize();
      center = getCenter_0(castTo($collect($map_0(selectedDiagrammParts.stream(), new DiagramJSObjectUtil$lambda$0$Type(canvas)), of_0(new Collectors$23methodref$ctor$Type, new Collectors$24methodref$add$Type, new Collectors$lambda$50$Type, new Collectors$lambda$51$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [UNORDERED, IDENTITY_FINISH]))), 20), canvas.getSize());
      $setViewbox(canvas, createBounds(center, size_0));
    }
    $selectInternal(selection, getArray(selectedDiagrammParts), false);
  }
}

function DisplayGraphUpdatePostProcesser(diagram){
  this._diagram = diagram;
}

function lambda$0_1(canvas_0, shape_1, label_2){
  canvas_0.addShape(label_2, shape_1);
}

function lambda$1_0(canvas_0, connection_1, label_2){
  canvas_0.addShape(label_2, connection_1);
}

defineClass(384, 1, {}, DisplayGraphUpdatePostProcesser);
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphUpdatePostProcesser_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphUpdatePostProcesser', 384);
function DisplayGraphUpdatePostProcesser$lambda$0$Type(canvas_0, shape_1){
  this.canvas_0 = canvas_0;
  this.shape_1 = shape_1;
}

defineClass(385, 1, {}, DisplayGraphUpdatePostProcesser$lambda$0$Type);
_.accept = function accept_2(arg0){
  lambda$0_1(this.canvas_0, this.shape_1, castToJso(arg0));
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphUpdatePostProcesser$lambda$0$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphUpdatePostProcesser/lambda$0$Type', 385);
function DisplayGraphUpdatePostProcesser$lambda$1$Type(canvas_0, connection_1){
  this.canvas_0 = canvas_0;
  this.connection_1 = connection_1;
}

defineClass(386, 1, {}, DisplayGraphUpdatePostProcesser$lambda$1$Type);
_.accept = function accept_3(arg0){
  lambda$1_0(this.canvas_0, this.connection_1, castToJso(arg0));
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphUpdatePostProcesser$lambda$1$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphUpdatePostProcesser/lambda$1$Type', 386);
function DisplayGraphUpdatePostProcesser$lambda$2$Type(){
}

defineClass(387, 1, {}, DisplayGraphUpdatePostProcesser$lambda$2$Type);
_.apply_0 = function apply_16(arg0){
  return castTo(arg0, 44).getTag();
}
;
var Lcom_top_1logic_graph_diagramjs_client_service_scope_listener_DisplayGraphUpdatePostProcesser$lambda$2$Type_2_classLit = createForClass('com.top_logic.graph.diagramjs.client.service.scope.listener', 'DisplayGraphUpdatePostProcesser/lambda$2$Type', 387);
function DefaultDiagramJSClassNode(scope_0){
  DefaultLabelOwner.call(this, scope_0);
}

defineClass(68, 86, {58:1, 42:1, 6:1, 45:1, 41:1, 44:1, 122:1, 120:1, 91:1, 86:1, 68:1}, DefaultDiagramJSClassNode);
var Lcom_top_1logic_graph_diagramjs_model_impl_DefaultDiagramJSClassNode_2_classLit = createForClass('com.top_logic.graph.diagramjs.model.impl', 'DefaultDiagramJSClassNode', 68);
function DefaultDiagramJSEdge(scope_0){
  DefaultLabelOwner.call(this, scope_0);
}

defineClass(76, 145, {58:1, 42:1, 6:1, 45:1, 41:1, 121:1, 44:1, 122:1, 91:1, 145:1, 600:1, 76:1}, DefaultDiagramJSEdge);
_.getLabels = function getLabels_0(){
  return new HandleCollectionWrapper($mkReferrers(this, 'owner'));
}
;
var Lcom_top_1logic_graph_diagramjs_model_impl_DefaultDiagramJSEdge_2_classLit = createForClass('com.top_logic.graph.diagramjs.model.impl', 'DefaultDiagramJSEdge', 76);
function DefaultDiagramJSGraphModel(scope_0){
  DefaultSharedObject.call(this, scope_0);
  this._graphPartByTag = new HashMap;
  this._listeners = new GraphListeners;
  $add(scope_0._listeners, this);
}

defineClass(171, 99, {58:1, 42:1, 6:1, 159:1, 45:1, 41:1, 91:1, 99:1, 171:1}, DefaultDiagramJSGraphModel);
var Lcom_top_1logic_graph_diagramjs_model_impl_DefaultDiagramJSGraphModel_2_classLit = createForClass('com.top_logic.graph.diagramjs.model.impl', 'DefaultDiagramJSGraphModel', 171);
function DefaultDiagramJSLabel(scope_0){
  DefaultGraphPart.call(this, scope_0);
}

defineClass(69, 324, {58:1, 42:1, 6:1, 45:1, 41:1, 44:1, 107:1, 91:1, 601:1, 69:1}, DefaultDiagramJSLabel);
var Lcom_top_1logic_graph_diagramjs_model_impl_DefaultDiagramJSLabel_2_classLit = createForClass('com.top_logic.graph.diagramjs.model.impl', 'DefaultDiagramJSLabel', 69);
function ModuleEntry$lambda$0$Type_0(){
}

defineClass(238, 1, {190:1}, ModuleEntry$lambda$0$Type_0);
_.createControl = function createControl_0(arg0){
  return new JSBlocksControl(arg0);
}
;
var Lcom_top_1logic_graphic_blocks_client_boot_ModuleEntry$lambda$0$Type_2_classLit = createForClass('com.top_logic.graphic.blocks.client.boot', 'ModuleEntry/lambda$0$Type', 238);
function ModuleEntry$lambda$1$Type(){
}

defineClass(239, 1, {190:1}, ModuleEntry$lambda$1$Type);
_.createControl = function createControl_1(arg0){
  return new JSDiagramControl(arg0);
}
;
var Lcom_top_1logic_graphic_blocks_client_boot_ModuleEntry$lambda$1$Type_2_classLit = createForClass('com.top_logic.graphic.blocks.client.boot', 'ModuleEntry/lambda$1$Type', 239);
function $visit(this$static, model, arg){
  var part, part$iterator;
  for (part$iterator = new ArrayList$1(model._parts); part$iterator.i < part$iterator.this$01.array.length;) {
    part = castTo($next_1(part$iterator), 119);
    part.visit(this$static, arg);
  }
  return null;
}

function $visit_0(this$static, model, arg){
  var part, part$iterator;
  for (part$iterator = new AbstractBlockConcatenation$1$1(model._contents); part$iterator._current;) {
    part = $next(part$iterator);
    $visit_3(this$static, part, throwClassCastExceptionUnlessNull(arg));
  }
  return throwClassCastExceptionUnlessNull(arg) , null;
}

function $visit_1(this$static, model, arg){
  var part, part$iterator;
  for (part$iterator = new AbstractBlockConcatenation$1$1(model._contents); part$iterator._current;) {
    part = $next(part$iterator);
    $visit_3(this$static, part, throwClassCastExceptionUnlessNull(arg));
  }
  return throwClassCastExceptionUnlessNull(arg) , null;
}

function $visit_2(this$static, model, arg){
  var part, part$iterator;
  for (part$iterator = model._contents.iterator(); part$iterator.hasNext_0();) {
    part = castTo(part$iterator.next_1(), 106);
    part.visit(this$static, arg);
  }
  return throwClassCastExceptionUnlessNull(arg) , null;
}

defineClass(492, 1, {});
var Lcom_top_1logic_graphic_blocks_model_visit_DescendingBlockVisitor_2_classLit = createForClass('com.top_logic.graphic.blocks.model.visit', 'DescendingBlockVisitor', 492);
function $getModel(this$static, id_0){
  return castTo($getStringValue(this$static._index, id_0), 88);
}

function $visit_3(this$static, model, arg){
  $putStringValue(this$static._index, model._id, model);
  return $visit(this$static, model, arg);
}

function BlockIndex(){
  this._index = new HashMap;
}

defineClass(209, 492, {}, BlockIndex);
var Lcom_top_1logic_graphic_blocks_client_control_BlockIndex_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'BlockIndex', 209);
function $findMatch(this$static, sensitives){
  var active, active$iterator, activeBlock, connectionBottom, connectionTop, match_0, sensitive, sensitive$array, sensitive$index, sensitive$max, sensitiveList;
  match_0 = ($clinit_ConnectorMatch() , NONE_0);
  for (sensitive$array = sensitives , sensitive$index = 0 , sensitive$max = sensitive$array.length; sensitive$index < sensitive$max; ++sensitive$index) {
    sensitive = sensitive$array[sensitive$index];
    for (active$iterator = new ArrayList$1(this$static._activeConnectors); active$iterator.i < active$iterator.this$01.array.length;) {
      active = castTo($next_1(active$iterator), 138);
      if (!$canConnectTo(sensitive.getKind_0(), active.getKind_0())) {
        continue;
      }
      if (sensitive.getKind_0() == 2) {
        if (active.getKind_0() != 0) {
          continue;
        }
        activeBlock = castTo(active.getOwner(), 88);
        if (activeBlock._previous) {
          continue;
        }
        if (!instanceOf(activeBlock._outer, 111)) {
          continue;
        }
      }
      sensitiveList = castTo(sensitive.getOwner(), 88)._outer;
      connectionTop = active.connectionTop();
      if (connectionTop) {
        if (connectionTop.getType_0() != sensitiveList.getFirst()._topConnector.this$01._type._topType) {
          continue;
        }
      }
      connectionBottom = active.connectionBottom();
      if (connectionBottom) {
        if (sensitiveList.getLast()._bottomConnector.this$01._type._bottomType != connectionBottom.getType_0()) {
          continue;
        }
      }
      match_0 = match_0.best(active.match_0(sensitive));
    }
  }
  return match_0;
}

function $onMouseDown(this$static, event_0){
  var target;
  target = event_0.nativeEvent.target;
  if ($select(this$static, target)) {
    this$static._dragStartX = $getRelativeX(event_0, this$static._svg.ot);
    this$static._dragStartY = $getRelativeY(event_0, this$static._svg.ot);
    this$static._moveHandler = $addDomHandler(this$static._svg, this$static, ($clinit_MouseMoveEvent() , $clinit_MouseMoveEvent() , TYPE_3));
    this$static._upHandler = $addDomHandler(this$static._svg, this$static, ($clinit_MouseUpEvent() , $clinit_MouseUpEvent() , TYPE_4));
  }
}

function $onMouseUp(this$static){
  var appendTo, inserted, out, parentNode, placeBefore, prependTo, result, top_0;
  if (!!this$static._dragElement && !!this$static._activeConnector) {
    inserted = this$static._dragElement._first;
    $removeFrom(this$static._dragElement, inserted);
    if (this$static._activeConnector.getKind_0() == 2) {
      appendTo = castTo(this$static._activeConnector.getOwner(), 88);
      appendTo._outer.insertBefore_0(appendTo._next, inserted);
      top_0 = appendTo._outer.top_0();
    }
     else {
      placeBefore = this$static._activeConnector.getKind_0() == 0;
      prependTo = placeBefore?castTo(this$static._activeConnector.getOwner(), 88)._outer:castTo(this$static._activeConnector.getOwner(), 234);
      prependTo.insertBefore_0(prependTo.getFirst(), inserted);
      top_0 = prependTo.top_0();
      placeBefore && $setY_5(top_0, top_0._y - this$static._dragElement._height - 1);
    }
    $removeDisplay(this$static, this$static._dragElement);
    $removeActiveConnector(this$static);
    result = castTo($getElementById_0(this$static._svgDoc, top_0._id), 4);
    $removeChild_0((parentNode = result.ot.parentNode , parentNode?convert(parentNode):null), result);
    $updateDimensions(top_0, this$static._renderContext, 0, 0);
    out = new SVGBuilder(this$static._svgDoc, this$static._svg);
    $draw_1(top_0, out);
  }
   else {
    $removeActiveConnector(this$static);
  }
  $resetDrag(this$static);
}

function $removeActiveConnector(this$static){
  if (this$static._activeConnector) {
    $removeDisplay(this$static, this$static._activeConnector);
    this$static._activeConnector = null;
  }
}

function $removeDisplay(this$static, model){
  var parentNode, result;
  result = castTo($getElementById_0(this$static._svgDoc, model.getId()), 4);
  $removeChild_0((parentNode = result.ot.parentNode , parentNode?convert(parentNode):null), result);
  return result;
}

function $resetDrag(this$static){
  if (this$static._moveHandler) {
    $removeHandler(this$static._moveHandler);
    this$static._moveHandler = null;
  }
  if (this$static._upHandler) {
    $removeHandler(this$static._upHandler);
    this$static._upHandler = null;
  }
  this$static._dragElement = null;
  this$static._dragStartX = 0;
  this$static._dragStartY = 0;
}

function $select(this$static, element){
  var id_0, model, parent_0;
  while (element) {
    id_0 = element.id;
    model = $getModel(this$static._blockById, id_0);
    if (model) {
      $setSelected(this$static, model);
      return true;
    }
    if (element == getElement(this$static._id)) {
      $setSelected(this$static, null);
      break;
    }
    element = (parent_0 = element.parentNode , (!parent_0 || parent_0.nodeType != 1) && (parent_0 = null) , parent_0);
  }
  return false;
}

function $setSelected(this$static, newSelection){
  var newElement, oldElement, ownerDocument;
  if (newSelection == this$static._selected) {
    return;
  }
  ownerDocument = getElement(this$static._id).ownerDocument;
  if (this$static._selected) {
    oldElement = $getElementById(ownerDocument, this$static._selected._id);
    !!oldElement && removeClassName(oldElement, 'tlbSelected');
    this$static._selected = null;
  }
  if (newSelection) {
    newElement = $getElementById(ownerDocument, newSelection._id);
    if (newElement) {
      this$static._selected = newSelection;
      addClassName(newElement, 'tlbSelected');
    }
  }
}

function $startDrag(this$static){
  var block, block$iterator, display, dragged, dragged0, entry, newList, out, outer, outerIter, top_0;
  this$static._dragOffsetX = $getX(this$static._selected) - this$static._dragStartX;
  this$static._dragOffsetY = $getY(this$static._selected) - this$static._dragStartY;
  out = new SVGBuilder(this$static._svgDoc, this$static._svg);
  outer = this$static._selected._outer;
  if (instanceOf(outer, 111) && !this$static._selected._previous) {
    this$static._dragElement = castTo(outer, 111);
    display = $removeDisplay(this$static, this$static._dragElement);
    $appendChild_1(this$static._svg, display);
  }
   else {
    dragged0 = outer.removeFrom(this$static._selected);
    newList = new BlockList;
    $setId_0(newList, this$static._id + '-' + this$static._nextId++);
    $insertBefore(newList, null, dragged0);
    this$static._dragElement = newList;
    top_0 = outer.top_0();
    $removeDisplay(this$static, top_0);
    $updateDimensions(top_0, this$static._renderContext, 0, 0);
    $updateDimensions(this$static._dragElement, this$static._renderContext, 0, 0);
    $draw_1(top_0, out);
    $draw_1(this$static._dragElement, out);
  }
  dragged = new BlockIndex;
  $visit_0(dragged, this$static._dragElement, null);
  this$static._activeConnectors = new ArrayList;
  for (block$iterator = (outerIter = (new AbstractMap$2(this$static._blockById._index)).this$01.entrySet_0().iterator() , new AbstractMap$2$1(outerIter)); block$iterator.val$outerIter2.hasNext_0();) {
    block = (entry = castTo(block$iterator.val$outerIter2.next_1(), 22) , castTo(entry.getValue(), 88));
    if ($getModel(dragged, block._id)) {
      continue;
    }
    $forEachConnector(block, new JSBlocksControl$0methodref$add$Type(this$static._activeConnectors));
  }
}

function $updateDrag(this$static, x_0, y_0){
  var active, connectableId, match_0, outerElement;
  $setX_5(this$static._dragElement, x_0 + this$static._dragOffsetX);
  $setY_5(this$static._dragElement, y_0 + this$static._dragOffsetY);
  $updatePosition(this$static, this$static._dragElement);
  match_0 = $findMatch(this$static, stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_graphic_blocks_model_block_Connector_2_classLit, 1), $intern_0, 138, 0, [this$static._dragElement._first._topConnector, this$static._dragElement._last._bottomConnector]));
  if (match_0.hasMatch()) {
    active = match_0.getActive();
    if (active != this$static._activeConnector) {
      $removeActiveConnector(this$static);
      this$static._activeConnector = active;
      connectableId = this$static._activeConnector.getOwner()._id;
      outerElement = castTo($getElementById_0(this$static._svgDoc, connectableId), 174);
      this$static._activeConnector.draw(new SVGBuilder_0(this$static._svgDoc, this$static._svg, outerElement));
    }
  }
   else {
    $removeActiveConnector(this$static);
  }
}

function $updatePosition(this$static, model){
  var node, tx, txList;
  node = castTo($getElementById_0(this$static._svgDoc, model._id), 43);
  txList = $getBaseVal_0(node.getTransform());
  if (txList.ot.numberOfItems != 1) {
    tx = this$static._svg.ot.createSVGTransform();
    txList.ot.clear();
    txList.ot.appendItem(tx);
  }
   else {
    tx = txList.ot.getItem(0);
  }
  $setTranslate(tx, model._x, model._y);
}

function JSBlocksControl(id_0){
  this._id = id_0;
  this._blockById = new BlockIndex;
  this._renderContext = new JSRenderContext;
}

defineClass(251, 169, {235:1, 592:1, 593:1, 594:1, 233:1, 105:1, 183:1}, JSBlocksControl);
_.init_0 = function init_3(args){
  var blocks, ex;
  $clinit_DOM();
  getElement(this._id).__listener = this;
  try {
    this._schema = read(new JsonReader(new StringR(getElement(this._id).getAttribute('data-blockschema') || '')));
  }
   catch ($e0) {
    $e0 = toJava($e0);
    if (instanceOf($e0, 32)) {
      ex = $e0;
      throw toJs(new RuntimeException_2(ex));
    }
     else 
      throw toJs($e0);
  }
  try {
    blocks = read_0(this._schema, new JsonReader(new StringR(getElement(this._id).getAttribute('data-blockdata') || '')));
  }
   catch ($e1) {
    $e1 = toJava($e1);
    if (instanceOf($e1, 32)) {
      ex = $e1;
      throw toJs(new RuntimeException_2(ex));
    }
     else 
      throw toJs($e1);
  }
  this._svgDoc = ($clinit_OMSVGParser() , castTo(convert(($clinit_DOMHelper() , $doc)), 170));
  this._svg = castTo($getElementById_0(this._svgDoc, getElement(this._id).id), 131);
  $visit_0(this._blockById, blocks, null);
  $updateDimensions(blocks, this._renderContext, 0, 0);
  $draw_1(blocks, new SVGBuilder(this._svgDoc, this._svg));
  $addDomHandler(this._svg, this, ($clinit_MouseDownEvent() , $clinit_MouseDownEvent() , TYPE_2));
  $addDomHandler(this._svg, this, ($clinit_ClickEvent() , $clinit_ClickEvent() , TYPE));
}
;
_.onClick = function onClick(event_0){
}
;
_._dragOffsetX = 0;
_._dragOffsetY = 0;
_._dragStartX = 0;
_._dragStartY = 0;
_._nextId = 1;
var Lcom_top_1logic_graphic_blocks_client_control_JSBlocksControl_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'JSBlocksControl', 251);
function JSBlocksControl$0methodref$add$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(296, 1, {}, JSBlocksControl$0methodref$add$Type);
_.accept = function accept_4(arg0){
  $add_0(this.$$outer_0, castTo(arg0, 138));
}
;
var Lcom_top_1logic_graphic_blocks_client_control_JSBlocksControl$0methodref$add$Type_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'JSBlocksControl/0methodref$add$Type', 296);
function $lambda$1_0(this$static, json_0){
  var diagram, ex;
  if (json_0.length == 0) {
    return null;
  }
  try {
    this$static._scope = new JSDiagramControl$3(this$static);
    diagram = readDiagram(this$static._scope, new JsonReader_0(new StringR_0(json_0)));
    $layout(diagram, this$static._renderContext);
    $draw_3(diagram, new JSDiagramControl$5(this$static, this$static._svgDoc, this$static._svg));
  }
   catch ($e0) {
    $e0 = toJava($e0);
    if (instanceOf($e0, 32)) {
      ex = $e0;
      $wnd.goog.global.console.error('Failed to fetch diagram data: ', ex.detailMessage);
    }
     else 
      throw toJs($e0);
  }
  return null;
}

function $onChange(this$static){
  var buffer, dirty, dirty$iterator, ex, patch, updateWriter, widget;
  this$static._changeTimeout = 0;
  if ($getDirty(this$static._scope).coll.isEmpty()) {
    return;
  }
  updateWriter = new JSDiagramControl$4(this$static._svgDoc, this$static._svg);
  for (dirty$iterator = new Collections$UnmodifiableCollectionIterator($getDirty(this$static._scope).coll.iterator()); dirty$iterator.it.hasNext_0();) {
    dirty = castTo(dirty$iterator.it.next_1(), 30);
    widget = castTo(dirty, 36);
    widget.getClientId() != null && widget.draw(updateWriter);
  }
  try {
    buffer = new StringW;
    $createPatch(this$static._scope, new JsonWriter_0(buffer));
    patch = buffer._buffer.string;
    $wnd.goog.global.console.info('Sending updates: ', patch);
    $sendUpdate(this$static._id, patch, true);
  }
   catch ($e0) {
    $e0 = toJava($e0);
    if (instanceOf($e0, 32)) {
      ex = $e0;
      $wnd.goog.global.console.error('Faild to write updates.', ex);
    }
     else 
      throw toJs($e0);
  }
}

function $sendUpdate(id_0, patch, showWait){
  $wnd.services.ajax.execute('dispatchControlCommand', {controlCommand:'update', controlID:id_0, patch:patch}, showWait);
}

function JSDiagramControl(id_0){
  this._id = id_0;
  this._renderContext = new JSRenderContext;
  this._nextId = new SubIdGenerator(id_0);
}

defineClass(252, 169, {235:1, 233:1, 105:1, 183:1}, JSDiagramControl);
_.init_0 = function init_4(args){
  var contentUrl, controlElement, response;
  $clinit_DOM();
  getElement(this._id).__listener = this;
  controlElement = $getElementById($doc, this._id);
  contentUrl = controlElement.getAttribute('data-content') || '';
  this._svgDoc = ($clinit_OMSVGParser() , castTo(convert(($clinit_DOMHelper() , $doc)), 170));
  this._svg = castTo($getElementById_0(this._svgDoc, getElement(this._id).id), 131);
  response = $wnd.goog.global.window.fetch(contentUrl);
  response.then(makeLambdaFunction(JSDiagramControl$lambda$0$Type.prototype.onInvoke_0, JSDiagramControl$lambda$0$Type, [])).then(makeLambdaFunction(JSDiagramControl$lambda$1$Type.prototype.onInvoke_0, JSDiagramControl$lambda$1$Type, [this]));
  $addDomHandler(this._svg, this, ($clinit_ClickEvent() , $clinit_ClickEvent() , TYPE));
}
;
_.onClick = function onClick_0(event_0){
  var element;
  element = event_0.nativeEvent.target;
}
;
_._changeTimeout = 0;
var Lcom_top_1logic_graphic_blocks_client_control_JSDiagramControl_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'JSDiagramControl', 252);
function $clinit_Listener(){
  $clinit_Listener = emptyMethod;
  NONE_4 = new Listener$1;
}

function register(current, added){
  $clinit_Listener();
  if (current == NONE_4 || current == added) {
    return added;
  }
  if (instanceOf(current, 144)) {
    return $register(castTo(current, 144), added);
  }
  return new Listener$MultiplexListener(current, added);
}

var NONE_4;
var Lde_haumacher_msgbuf_observer_Listener_2_classLit = createForInterface('de.haumacher.msgbuf.observer', 'Listener');
function $resolveOrFail(this$static, id_0){
  var result;
  result = castTo($get_6(this$static.index_1(), valueOf(id_0)), 30);
  if (!result) {
    throw toJs(new IllegalArgumentException("No object with ID '" + id_0 + "'."));
  }
  return result;
}

function $writeRefOrData(this$static, out, node){
  var id_0;
  id_0 = this$static.id_0(node);
  if (id_0 == 0) {
    id_0 = this$static.newId();
    this$static.enter(node, id_0);
    $writeData(node, this$static, out, id_0);
  }
   else {
    $value_3(out, id_0);
  }
}

function $clinit_DefaultScope(){
  $clinit_DefaultScope = emptyMethod;
  $clinit_Listener();
  NEW_MAP = new DefaultScope$lambda$0$Type;
}

function $createPatch(this$static, json){
  $writeDeferredName_0(json);
  $open_0(json, 1, '[');
  $foreachCommand(this$static, new DefaultScope$lambda$1$Type(this$static, json));
  $close_1(json, 1, 2, ']');
  $clear_1(this$static._changes);
}

function $foreachCommand(this$static, fun){
  var command, entry, first, first$iterator, next, outerIter, perObject, perObject$iterator;
  for (perObject$iterator = (outerIter = (new AbstractMap$2(this$static._changes)).this$01.entrySet_0().iterator() , new AbstractMap$2$1(outerIter)); perObject$iterator.val$outerIter2.hasNext_0();) {
    perObject = (entry = castTo(perObject$iterator.val$outerIter2.next_1(), 22) , castTo(entry.getValue(), 21));
    for (first$iterator = perObject.values().iterator(); first$iterator.hasNext_0();) {
      first = castTo(first$iterator.next_1(), 72);
      command = first;
      $lambda$1_1(fun.$$outer_0, fun.json_1, command);
      if (instanceOf(command, 78)) {
        next = castTo(command, 78)._next;
        while (next) {
          $lambda$1_1(fun.$$outer_0, fun.json_1, next);
          next = next._next;
        }
      }
    }
  }
}

function $getDirty(this$static){
  return $clinit_Collections() , new Collections$UnmodifiableSet(new AbstractMap$1(this$static._changes));
}

function $lambda$1_1(this$static, json_1, command_1){
  $writeDeferredName_0(json_1);
  $open_0(json_1, 1, '[');
  $writeTo_7(command_1, json_1);
  command_1.visit_0(this$static._extractor, json_1);
  $close_1(json_1, 1, 2, ']');
}

function $readData(this$static, node, id_0, in_0){
  var before;
  before = this$static._applying;
  this$static._applying = true;
  try {
    this$static.enter(node, id_0);
    $beginObject_0(in_0);
    $readFields(node, this$static, in_0);
    $endObject_0(in_0);
  }
   finally {
    this$static._applying = before;
  }
}

function append(clash, update){
  var cnt, current, next;
  current = clash;
  cnt = 1;
  while (true) {
    next = current._next;
    if (!next) {
      break;
    }
    ++cnt;
    if (cnt > 10) {
      return $setProperty_1($setNode_1($setId_4(new SetProperty, update._id), update._node), update._property);
    }
    current = next;
  }
  current.setNext(update);
  return clash;
}

defineClass(297, 1, {139:1});
_.resolveOrFail = function resolveOrFail(id_0){
  return $resolveOrFail(this, id_0);
}
;
_.writeRefOrData = function writeRefOrData(out, node){
  $writeRefOrData(this, out, node);
}
;
_.afterRemove = function afterRemove(obj, property, index_0, element){
  var changes, clash, node, remove;
  if (this._applying || obj.transientProperties().contains(property)) {
    return;
  }
  node = castTo(obj, 28);
  changes = ($size(this._changes.map_0) == 0 || this.beforeChange() , castTo($computeIfAbsent(this._changes, node, NEW_MAP), 21));
  remove = new RemoveElement;
  $setProperty_0($setId_3($setNode_0((remove._index = index_0 , remove), node), node._id), property);
  clash = castTo(changes.put(property, remove), 72);
  !!clash && (instanceOf(clash, 135)?changes.put(property, clash):changes.put(property, append(castTo(clash, 78), remove)));
}
;
_.beforeAdd = function beforeAdd(obj, property, index_0, element){
  var changes, clash, insert, node;
  if (this._applying || obj.transientProperties().contains(property)) {
    return;
  }
  node = castTo(obj, 28);
  changes = ($size(this._changes.map_0) == 0 || this.beforeChange() , castTo($computeIfAbsent(this._changes, node, NEW_MAP), 21));
  insert = new InsertElement;
  $setProperty($setId_2($setNode($setIndex((insert._element = element , insert), index_0), node), node._id), property);
  clash = castTo(changes.put(property, insert), 72);
  !!clash && (instanceOf(clash, 135)?changes.put(property, clash):changes.put(property, append(castTo(clash, 78), insert)));
}
;
_.beforeChange = function beforeChange(){
}
;
_.beforeSet = function beforeSet(obj, property, value_0){
  var node;
  if (this._applying || obj.transientProperties().contains(property)) {
    return;
  }
  node = castTo(obj, 28);
  ($size(this._changes.map_0) == 0 || this.beforeChange() , castTo($computeIfAbsent(this._changes, node, NEW_MAP), 21)).put(property, $setProperty_1($setId_4($setNode_1(new SetProperty, node), node._id), property));
}
;
_.enter = function enter_0(node, id_0){
  $initId(node, id_0);
  castTo($put_0(this._index, valueOf(id_0), node), 30);
  node._listener = register(node._listener, this);
}
;
_.id_0 = function id_1(node){
  return node._id;
}
;
_.index_1 = function index_1(){
  return this._index;
}
;
_.initId = function initId(node, id_0){
  $initId(node, id_0);
}
;
_.newId = function newId(){
  var result;
  result = this._nextId;
  this._nextId += this._totalParticipants;
  return result;
}
;
_.readData = function readData(node, id_0, in_0){
  $readData(this, node, id_0, in_0);
}
;
_._applying = false;
_._nextId = 0;
_._totalParticipants = 0;
var NEW_MAP;
var Lde_haumacher_msgbuf_graph_DefaultScope_2_classLit = createForClass('de.haumacher.msgbuf.graph', 'DefaultScope', 297);
function JSDiagramControl$3(this$0){
  $clinit_DefaultScope();
  this.this$01 = this$0;
  this._changes = new LinkedHashMap;
  this._index = new HashMap;
  this._extractor = new DefaultScope$ChangeExtractor(this);
  this._totalParticipants = 2;
  this._nextId = 2;
}

defineClass(298, 297, {139:1}, JSDiagramControl$3);
_.beforeChange = function beforeChange_0(){
  this.this$01._changeTimeout != 0 && $wnd.goog.global.clearTimeout(this.this$01._changeTimeout);
  this.this$01._changeTimeout = $wnd.goog.global.setTimeout(makeLambdaFunction(JSDiagramControl$3$0methodref$onChange$Type.prototype.onInvoke, JSDiagramControl$3$0methodref$onChange$Type, [this.this$01]), 10);
}
;
var Lcom_top_1logic_graphic_blocks_client_control_JSDiagramControl$3_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'JSDiagramControl/3', 298);
function JSDiagramControl$3$0methodref$onChange$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(606, $wnd.Function, {}, JSDiagramControl$3$0methodref$onChange$Type);
_.onInvoke = function onInvoke(arg0){
  $onChange(this.$$outer_0);
}
;
function $appendChild_0(this$static, next){
  $appendChild_1(this$static._parent, next);
  this$static._current = next;
}

function $beginRect(this$static, x_0, y_0, w, h, rx, ry){
  var rect;
  rect = new OMSVGRectElement(x_0, y_0, w, h, rx, ry);
  $appendChild_1(this$static._parent, rect);
  this$static._current = rect;
  this$static._current = rect;
}

function $moveToAbs(this$static, x_0, y_0){
  var path;
  path = castTo(this$static._current, 47);
  $appendItem_0($getPathSegList(path.ot), $createSVGPathSegMovetoAbs(path.ot, x_0, y_0));
}

function $setParent(this$static, next){
  this$static._parent = next;
  this$static._current = next;
}

function SVGBuilder(doc, root){
  SVGBuilder_0.call(this, doc, root, root);
}

function SVGBuilder_0(doc, root, parent_0){
  this._doc = doc;
  this._root = root;
  this._parent = parent_0;
  this._current = parent_0;
}

defineClass(98, 1, {}, SVGBuilder, SVGBuilder_0);
_.beginGroup = function beginGroup(){
  this.beginGroup_0(null);
}
;
_.beginPath = function beginPath(){
  this.beginPath_0(null);
}
;
_.beginRect = function beginRect(x_0, y_0, w, h){
  $beginRect(this, x_0, y_0, w, h, 0, 0);
}
;
_.moveToAbs_0 = function moveToAbs_0(v){
  $moveToAbs(this, v._x, v._y);
}
;
_.rect = function rect_0(x_0, y_0, w, h, radius){
  $beginRect(this, x_0, y_0, w, h, radius, radius);
  this._current = null;
}
;
_.rect_0 = function rect_1(x_0, y_0, w, h, rx, ry){
  $beginRect(this, x_0, y_0, w, h, rx, ry);
  this._current = null;
}
;
_.roundedCorner = function roundedCorner(ccw, rx, ry){
  var path, path0;
  rx != 0 && ry != 0 && (rx > 0 ^ ry > 0 ^ ccw?(path0 = castTo(this._current, 47) , $appendItem_0($getPathSegList(path0.ot), $createSVGPathSegCurvetoCubicRel(path0.ot, rx, ry, 0, $intern_21 * ry, $intern_22 * rx, ry))):(path = castTo(this._current, 47) , $appendItem_0($getPathSegList(path.ot), $createSVGPathSegCurvetoCubicRel(path.ot, rx, ry, $intern_21 * rx, 0, rx, $intern_22 * ry))));
}
;
_.text_0 = function text_1(x_0, y_0, text_0){
  $appendChild_0(this, new OMSVGTextElement(x_0, y_0, text_0));
  this._current = null;
}
;
_.write_0 = function write_0(element){
  element.draw(this);
}
;
_.attachOnClick = function attachOnClick(handler, sender){
  var registration;
  registration = $addDomHandler(this._current, new SVGBuilder$1(handler), ($clinit_ClickEvent() , $clinit_ClickEvent() , TYPE));
  return new SVGBuilder$lambda$0$Type(registration);
}
;
_.beginData = function beginData(){
}
;
_.beginGroup_0 = function beginGroup_0(model){
  var next;
  next = castTo(convert(createElementNS(this._doc.ot, 'http://www.w3.org/2000/svg', 'g')), 174);
  $appendChild_1(this._parent, next);
  this._parent = next;
  this._current = next;
  !!model && this.linkModel(next, model);
}
;
_.beginPath_0 = function beginPath_0(model){
  var path;
  path = castTo(convert(createElementNS(this._doc.ot, 'http://www.w3.org/2000/svg', 'path')), 47);
  $appendChild_1(this._parent, path);
  this._current = path;
  !!model && this.linkModel(path, model);
}
;
_.beginRect_0 = function beginRect_0(x_0, y_0, w, h, rx, ry){
  $beginRect(this, x_0, y_0, w, h, rx, ry);
}
;
_.beginSvg = function beginSvg(){
  this._parent != this._root && this.beginGroup_0(null);
}
;
_.beginText = function beginText(x_0, y_0, text_0){
  $appendChild_0(this, new OMSVGTextElement(x_0, y_0, text_0));
}
;
_.close_0 = function close_5(){
}
;
_.closePath = function closePath(){
  var path;
  path = castTo(this._current, 47);
  $appendItem_0($getPathSegList(path.ot), path.ot.createSVGPathSegClosePath());
}
;
_.curveToRel = function curveToRel(dx1, dy1, dx2, dy2, dx, dy){
  var path;
  path = castTo(this._current, 47);
  $appendItem_0($getPathSegList(path.ot), $createSVGPathSegCurvetoCubicRel(path.ot, dx, dy, dx1, dy1, dx2, dy2));
}
;
_.dimensions = function dimensions_0(width_0, height, x1, y1, x2, y2){
  if (this._parent == this._root) {
    $setAttribute_0(this._root, 'width', width_0);
    $setAttribute_0(this._root, 'height', height);
    $setAttribute_0(this._root, 'viewBox', x1 + ' ' + y1 + ' ' + x2 + ' ' + y2);
  }
}
;
_.endData = function endData(){
}
;
_.endGroup = function endGroup(){
  var parentNode;
  $setParent(this, (parentNode = this._parent.ot.parentNode , castTo(parentNode?convert(parentNode):null, 4)));
}
;
_.endPath = function endPath(){
}
;
_.endRect = function endRect(){
  this._current = null;
}
;
_.endSvg = function endSvg(){
  var parentNode;
  this._parent != this._root && $setParent(this, (parentNode = this._parent.ot.parentNode , castTo(parentNode?convert(parentNode):null, 4)));
}
;
_.endText = function endText(){
  this._current = null;
}
;
_.image_0 = function image(x_0, y_0, width_0, height, href_0, align_0, scale){
  var img;
  img = new OMSVGImageElement(x_0, y_0, width_0, height, href_0);
  $setAttribute(img.ot, 'preserveAspectRatio', align_0 + ' ' + scale);
  $appendChild_1(this._parent, img);
  this._current = img;
}
;
_.lineToAbs = function lineToAbs(x_0, y_0){
  var path;
  path = castTo(this._current, 47);
  $appendItem_0($getPathSegList(path.ot), $createSVGPathSegLinetoAbs(path.ot, x_0, y_0));
}
;
_.lineToHorizontalAbs = function lineToHorizontalAbs(x_0){
  var path;
  path = castTo(this._current, 47);
  $appendItem_0($getPathSegList(path.ot), $createSVGPathSegLinetoHorizontalAbs(path.ot, x_0));
}
;
_.lineToHorizontalRel = function lineToHorizontalRel(dx){
  var path;
  path = castTo(this._current, 47);
  $appendItem_0($getPathSegList(path.ot), $createSVGPathSegLinetoHorizontalRel(path.ot, dx));
}
;
_.lineToRel = function lineToRel(dx, dy){
  var path;
  path = castTo(this._current, 47);
  $appendItem_0($getPathSegList(path.ot), $createSVGPathSegLinetoRel(path.ot, dx, dy));
}
;
_.lineToVerticalAbs = function lineToVerticalAbs(y_0){
  var path;
  path = castTo(this._current, 47);
  $appendItem_0($getPathSegList(path.ot), $createSVGPathSegLinetoVerticalAbs(path.ot, y_0));
}
;
_.lineToVerticalRel = function lineToVerticalRel(dy){
  var path;
  path = castTo(this._current, 47);
  $appendItem_0($getPathSegList(path.ot), $createSVGPathSegLinetoVerticalRel(path.ot, dy));
}
;
_.linkModel = function linkModel(svgElement, model){
}
;
_.moveToAbs = function moveToAbs(x_0, y_0){
  $moveToAbs(this, x_0, y_0);
}
;
_.moveToRel = function moveToRel(dx, dy){
  var path;
  path = castTo(this._current, 47);
  $appendItem_0($getPathSegList(path.ot), $createSVGPathSegMovetoRel(path.ot, dx, dy));
}
;
_.setFill = function setFill(style){
  style != null && $setAttribute_0(this._current, 'fill', style);
}
;
_.setStroke = function setStroke(style){
  style != null && $setAttribute_0(this._current, 'stroke', style);
}
;
_.setStrokeDasharray = function setStrokeDasharray(dashes){
  $setAttribute_0(this._current, 'stroke-dasharray', valueList(dashes));
}
;
_.setStrokeWidth = function setStrokeWidth(value_0){
  $setAttribute_0(this._current, 'stroke-width', '' + value_0);
}
;
_.setTextStyle = function setTextStyle(fontFamily, fontSize, fontWeight){
  fontFamily != null && $setAttribute_0(this._current, 'font-family', fontFamily);
  fontSize != null && $setAttribute_0(this._current, 'font-size', fontSize);
  fontWeight != null && $setAttribute_0(this._current, 'font-weight', fontWeight);
}
;
_.translate = function translate(dx, dy){
  var list, transform;
  transform = this._root.ot.createSVGTransform();
  transform.setTranslate(dx, dy);
  list = $getBaseVal_0(castTo(this._current, 43).getTransform());
  list.ot.clear();
  list.ot.appendItem(transform);
}
;
_.writeAttribute = function writeAttribute(name_0, value_0){
  $setAttribute_0(this._current, name_0, value_0);
}
;
_.writeCssClass = function writeCssClass(cssClass){
  cssClass != null && $setAttribute_0(this._current, 'class', cssClass);
}
;
_.writeId = function writeId(id_0){
  id_0 != null && $setAttribute_0(this._current, 'id', id_0);
}
;
var Lcom_top_1logic_graphic_blocks_client_control_SVGBuilder_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'SVGBuilder', 98);
function $lookupUpdated(this$static, model){
  var id_0, updated;
  id_0 = model._clientId;
  updated = castTo($getElementById_0(this$static._doc, id_0), 4);
  this$static._parent = updated;
  this$static._current = updated;
}

function JSDiagramControl$4($anonymous0, $anonymous1){
  SVGBuilder.call(this, $anonymous0, $anonymous1);
}

defineClass(299, 98, {}, JSDiagramControl$4);
_.beginGroup_0 = function beginGroup_1(model){
  $lookupUpdated(this, model);
}
;
_.beginPath_0 = function beginPath_1(model){
  $lookupUpdated(this, model);
}
;
_.write_0 = function write_1(element){
}
;
var Lcom_top_1logic_graphic_blocks_client_control_JSDiagramControl$4_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'JSDiagramControl/4', 299);
function JSDiagramControl$5(this$0, $anonymous0, $anonymous1){
  this.this$01 = this$0;
  SVGBuilder.call(this, $anonymous0, $anonymous1);
}

defineClass(300, 98, {}, JSDiagramControl$5);
_.linkModel = function linkModel_0(svgElement, model){
  var id_0;
  id_0 = $createId(this.this$01._nextId);
  $setId(svgElement.ot, id_0);
  model.setClientId_0(id_0);
}
;
var Lcom_top_1logic_graphic_blocks_client_control_JSDiagramControl$5_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'JSDiagramControl/5', 300);
function JSDiagramControl$lambda$0$Type(){
}

defineClass(604, $wnd.Function, {}, JSDiagramControl$lambda$0$Type);
_.onInvoke_0 = function onInvoke_0(arg0){
  return castToNative(arg0, $wnd.Response).text();
}
;
function JSDiagramControl$lambda$1$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(605, $wnd.Function, {}, JSDiagramControl$lambda$1$Type);
_.onInvoke_0 = function onInvoke_1(arg0){
  return $lambda$1_0(this.$$outer_0, castToString(arg0));
}
;
function $measure(this$static, text_0){
  var metrics;
  metrics = $measureText(this$static._context2d, text_0);
  return new JSTextMetrics(metrics);
}

function JSRenderContext(){
  var canvas;
  canvas = $doc.createElement('canvas');
  this._context2d = canvas.getContext('2d');
  this._context2d.font = '14px Arial';
}

defineClass(210, 1, {}, JSRenderContext);
var Lcom_top_1logic_graphic_blocks_client_control_JSRenderContext_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'JSRenderContext', 210);
function $padding(this$static, top_0, left, bottom, right){
  return new TextMetricsImpl(this$static._metrics.width + left + right, this$static._metrics.actualBoundingBoxAscent + this$static._metrics.actualBoundingBoxDescent + top_0 + bottom, this$static._metrics.actualBoundingBoxAscent + top_0);
}

function JSTextMetrics(metrics){
  this._metrics = metrics;
}

defineClass(445, 1, {}, JSTextMetrics);
_.getBaseLine = function getBaseLine(){
  return this._metrics.actualBoundingBoxAscent;
}
;
_.getHeight = function getHeight(){
  return this._metrics.actualBoundingBoxAscent + this._metrics.actualBoundingBoxDescent;
}
;
_.getWidth = function getWidth(){
  return this._metrics.width;
}
;
var Lcom_top_1logic_graphic_blocks_client_control_JSTextMetrics_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'JSTextMetrics', 445);
function SVGBuilder$1(val$handler){
  this.val$handler2 = val$handler;
}

defineClass(305, 1, {235:1, 233:1}, SVGBuilder$1);
_.onClick = function onClick_1(event_0){
  this.val$handler2.onClick_0(new SVGBuilder$1$1(event_0));
}
;
var Lcom_top_1logic_graphic_blocks_client_control_SVGBuilder$1_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'SVGBuilder/1', 305);
function SVGBuilder$1$1(val$event){
  this.val$event3 = val$event;
}

defineClass(306, 1, {}, SVGBuilder$1$1);
var Lcom_top_1logic_graphic_blocks_client_control_SVGBuilder$1$1_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'SVGBuilder/1/1', 306);
function $clinit_Registration(){
  $clinit_Registration = emptyMethod;
  NONE_1 = new Registration$lambda$0$Type;
}

var NONE_1;
function SVGBuilder$lambda$0$Type(registration_0){
  this.registration_0 = registration_0;
}

defineClass(307, 1, {236:1}, SVGBuilder$lambda$0$Type);
_.cancel_0 = function cancel(){
  $removeHandler(this.registration_0);
}
;
var Lcom_top_1logic_graphic_blocks_client_control_SVGBuilder$lambda$0$Type_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'SVGBuilder/lambda$0$Type', 307);
function $createId(this$static){
  return this$static._baseId + '-' + this$static._next++;
}

function SubIdGenerator(baseId){
  this._baseId = baseId;
}

defineClass(313, 1, {}, SubIdGenerator);
_._next = 1;
var Lcom_top_1logic_graphic_blocks_client_control_SubIdGenerator_2_classLit = createForClass('com.top_logic.graphic.blocks.client.control', 'SubIdGenerator', 313);
function addClassName(element, className){
  var idx, oldClassName;
  className = trimClassName(className);
  oldClassName = getClassName(element);
  idx = indexOfName(oldClassName, className);
  if (idx == -1) {
    oldClassName.length > 0?setClassName(element, oldClassName + ' ' + className):setClassName(element, className);
    return true;
  }
  return false;
}

function getClassName(element){
  var result = element.className;
  if (!result) {
    return '';
  }
  return result.baseVal || result;
}

function indexOfName(nameList, name_0){
  var idx, last, lastPos;
  idx = nameList.indexOf(name_0);
  while (idx != -1) {
    if (idx == 0 || (checkCriticalStringElementIndex(idx - 1, nameList.length) , nameList.charCodeAt(idx - 1) == 32)) {
      last = idx + name_0.length;
      lastPos = nameList.length;
      if (last == lastPos || last < lastPos && (checkCriticalStringElementIndex(last, nameList.length) , nameList.charCodeAt(last) == 32)) {
        break;
      }
    }
    idx = nameList.indexOf(name_0, idx + 1);
  }
  return idx;
}

function removeClassName(element, className){
  var begin, end, idx, newClassName, oldStyle;
  className = trimClassName(className);
  oldStyle = getClassName(element);
  idx = indexOfName(oldStyle, className);
  if (idx != -1) {
    begin = $trim((checkCriticalStringBounds(0, idx, oldStyle.length) , oldStyle.substr(0, idx)));
    end = $trim($substring(oldStyle, idx + className.length));
    begin.length == 0?(newClassName = end):end.length == 0?(newClassName = begin):(newClassName = begin + ' ' + end);
    setClassName(element, newClassName);
    return true;
  }
  return false;
}

function setClassName(element, className){
  var newValue = className || '';
  var oldValue = element.className;
  !oldValue || !oldValue.baseVal?(element.className = newValue):(oldValue.baseVal = newValue);
}

function trimClassName(className){
  className = $trim(className);
  return className;
}

function $readFrom_0(this$static, context, json){
  $beginObject(json);
  while ($hasNext(json)) {
    this$static.readPropertyFrom(context, json, $nextName(json));
  }
  $endObject(json);
}

function $readPropertyFrom(this$static, name_0){
  throw toJs(new IllegalArgumentException("No such property '" + name_0 + "' in '" + $getName(getClass__Ljava_lang_Class___devirtual$(this$static)) + "'."));
}

function $intersects(this$static, other){
  return $wnd.Math.max(this$static._x1, other._x1) <= $wnd.Math.min(this$static._x2, other._x2) && $wnd.Math.max(this$static._y1, other._y1) <= $wnd.Math.min(this$static._y2, other._y2);
}

function Rect_0(x1, y1, x2, y2){
  this._x1 = x1;
  this._y1 = y1;
  this._x2 = x2;
  this._y2 = y2;
}

defineClass(157, 1, {}, Rect_0);
_._x1 = 0;
_._x2 = 0;
_._y1 = 0;
_._y2 = 0;
var Lcom_top_1logic_graphic_blocks_math_Rect_2_classLit = createForClass('com.top_logic.graphic.blocks.math', 'Rect', 157);
function $length(this$static){
  return $wnd.Math.sqrt(this$static._x * this$static._x + this$static._y * this$static._y);
}

function $minus(this$static, x_0, y_0){
  return new Vec(this$static._x - x_0, this$static._y - y_0);
}

function $minus_0(this$static, other){
  return $minus(this$static, other._x, other._y);
}

function $plus(this$static){
  return new Vec(this$static._x + 20, this$static._y);
}

function Vec(x_0, y_0){
  this._x = x_0;
  this._y = y_0;
}

defineClass(73, 1, {}, Vec);
_._x = 0;
_._y = 0;
var Lcom_top_1logic_graphic_blocks_math_Vec_2_classLit = createForClass('com.top_logic.graphic.blocks.math', 'Vec', 73);
function $readPropertyFrom_0(this$static, context, json, name_0){
  switch (name_0) {
    case 'id':
      $setId_0(this$static, $nextString(json));
      break;
    default:$readPropertyFrom(this$static, name_0);
  }
}

function $setId_0(this$static, value_0){
  this$static._id = value_0;
}

defineClass(493, 1, {57:1});
_.readFrom = function readFrom(context, json){
  $readFrom_0(this, context, json);
}
;
_.readPropertyFrom = function readPropertyFrom_0(context, json, name_0){
  this.readPropertyFrom_0(context, json, name_0);
}
;
_.top_0 = function top_1(){
  return this.getOwner().top_0();
}
;
_.getId = function getId(){
  return this._id;
}
;
_.readPropertyFrom_0 = function readPropertyFrom(context, json, name_0){
  $readPropertyFrom_0(this, context, json, name_0);
}
;
var Lcom_top_1logic_graphic_blocks_model_AbstractBlockModel_2_classLit = createForClass('com.top_logic.graphic.blocks.model', 'AbstractBlockModel', 493);
function $defineBlockType(this$static, type_0){
  $putStringValue(this$static._blockTypes, type_0._id, type_0);
}

function $defineConnectorType(this$static, type_0){
  $putStringValue(this$static._connectorTypes, type_0._name, type_0);
  return this$static;
}

function $defineRowPartType(this$static, kind, type_0){
  $putStringValue(this$static._contentTypes, kind, type_0);
  return this$static;
}

function $getBlockType(this$static, typeId){
  var result;
  result = castTo($getStringValue(this$static._blockTypes, typeId), 173);
  if (!result) {
    throw toJs(new IllegalArgumentException("No such block type '" + typeId + "'."));
  }
  return result;
}

function $getConnectorType(this$static, name_0){
  var result;
  result = castTo($getStringValue(this$static._connectorTypes, name_0), 54);
  if (!result) {
    throw toJs(new IllegalArgumentException("No such connector type '" + name_0 + "'."));
  }
  return result;
}

function $getRowPartType(this$static, kind){
  var result;
  result = castTo($getStringValue(this$static._contentTypes, kind), 108);
  if (!result) {
    throw toJs(new IllegalArgumentException("No such content type '" + kind + "'."));
  }
  return result;
}

function $readPropertyFrom_1(this$static, context, json, name_0){
  var result;
  switch (name_0) {
    case 'types':
      $beginArray(json);
      while ($hasNext(json)) {
        $defineBlockType(this$static, (result = new BlockType , $readFrom_0(result, this$static, json) , result));
      }

      $endArray(json);
      break;
    default:$readPropertyFrom(this$static, name_0);
  }
}

function BlockSchema(){
  this._connectorTypes = new HashMap;
  this._contentTypes = new HashMap;
  this._blockTypes = new HashMap;
}

function read(json){
  var schemaCopy, schema;
  schemaCopy = (schema = new BlockSchema , $defineRowPartType(schema, 'label', new BlockFactory$0methodref$ctor$Type) , $defineRowPartType(schema, 'text', new BlockFactory$1methodref$ctor$Type) , $defineRowPartType(schema, 'select', new BlockFactory$2methodref$ctor$Type) , $defineConnectorType(schema, ($clinit_VoidConnector() , INSTANCE_5)) , $defineConnectorType(schema, ($clinit_BooleanConnector() , INSTANCE_1)) , $defineConnectorType(schema, ($clinit_ValueConnector() , INSTANCE_4)) , $defineConnectorType(schema, ($clinit_SequenceConnector() , INSTANCE_3)) , $defineConnectorType(schema, ($clinit_NumberConnector() , INSTANCE_2)) , schema);
  $readFrom_0(schemaCopy, null, json);
  return schemaCopy;
}

defineClass(326, 1, {}, BlockSchema);
_.readFrom = function readFrom_0(context, json){
  $readFrom_0(this, context, json);
}
;
_.readPropertyFrom = function readPropertyFrom_1(context, json, name_0){
  $readPropertyFrom_1(this, throwClassCastExceptionUnlessNull(context), json, name_0);
}
;
var Lcom_top_1logic_graphic_blocks_model_BlockSchema_2_classLit = createForClass('com.top_logic.graphic.blocks.model', 'BlockSchema', 326);
function $addPartType(this$static, type_0){
  $add_0(this$static._parts, type_0);
}

function $readPropertyFrom_2(this$static, context, json, name_0){
  switch (name_0) {
    case 'id':
      $setId_1(this$static, $nextString(json));
      break;
    case 'cssClass':
      $setCssClass(this$static, $nextString(json));
      break;
    case 'topType':
      $setTopType(this$static, $getConnectorType(context, $nextString(json)));
      break;
    case 'bottomType':
      $setBottomType(this$static, $getConnectorType(context, $nextString(json)));
      break;
    case 'parts':
      $beginArray(json);
      while ($hasNext(json)) {
        $addPartType(this$static, read_1(context, json));
      }

      $endArray(json);
      break;
    default:$readPropertyFrom(this$static, name_0);
  }
}

function $setBottomType(this$static, bottomType){
  this$static._bottomType = bottomType;
}

function $setCssClass(this$static, cssClass){
  this$static._cssClass = cssClass;
}

function $setId_1(this$static, value_0){
  this$static._id = value_0;
}

function $setTopType(this$static, topType){
  this$static._topType = topType;
}

function BlockType(){
  this._parts = new ArrayList;
}

defineClass(173, 1, {173:1}, BlockType);
_.readFrom = function readFrom_1(context, json){
  $readFrom_0(this, context, json);
}
;
_.readPropertyFrom = function readPropertyFrom_2(context, json, name_0){
  $readPropertyFrom_2(this, context, json, name_0);
}
;
var Lcom_top_1logic_graphic_blocks_model_BlockType_2_classLit = createForClass('com.top_logic.graphic.blocks.model', 'BlockType', 173);
function $draw(this$static, out){
  var current, current$iterator;
  for (current$iterator = new AbstractBlockConcatenation$1$1(this$static._contents); current$iterator._current;) {
    current = $next(current$iterator);
    $draw_0(current, out);
  }
}

function $insertBefore(this$static, before, newBlock){
  var after, last, tail;
  if (!newBlock) {
    return;
  }
  last = $last(newBlock);
  if (!before) {
    if (!this$static._last) {
      this$static._first = newBlock;
      this$static._last = last;
      $checkInitOuter(newBlock);
      $doInitOuter(newBlock, this$static);
      return;
    }
    after = this$static._last;
  }
   else {
    after = before._previous;
  }
  $checkInitOuter(newBlock);
  $doInitOuter(newBlock, this$static);
  if (!after) {
    tail = this$static._first;
    this$static._first = newBlock;
  }
   else {
    tail = $unlinkNext(after);
    after._next = newBlock;
    newBlock._previous = after;
  }
  !tail?(this$static._last = last):(last._next = tail , tail._previous = last);
}

function $readPropertyFrom_3(this$static, context, json, name_0){
  var typeId, result;
  switch (name_0) {
    case 'contents':
      $beginArray(json);
      while ($hasNext(json)) {
        $insertBefore(this$static, null, ($beginArray(json) , typeId = $nextString(json) , result = new Block($getBlockType(context, typeId)) , $readFrom_0(result, context, json) , $endArray(json) , result));
      }

      $endArray(json);
      break;
    default:$readPropertyFrom_0(this$static, context, json, name_0);
  }
}

function $removeFrom(this$static, start_0){
  var previous;
  previous = start_0._previous;
  if (!previous) {
    this$static._first = this$static._last = null;
  }
   else {
    this$static._last = previous;
    $unlinkNext(this$static._last);
  }
  $unlinkOuter(start_0);
  return start_0;
}

function $updateDimensions(this$static, context, offsetX, offsetY){
  var content_0, content$iterator, contentHeight, first, localHeight;
  contentHeight = 0;
  first = true;
  for (content$iterator = new AbstractBlockConcatenation$1$1(this$static._contents); content$iterator._current;) {
    content_0 = $next(content$iterator);
    first?(first = false):(contentHeight += 1);
    $updateDimensions_0(content_0, context, offsetX, offsetY);
    localHeight = content_0._height;
    contentHeight += localHeight;
    offsetY += localHeight + 1;
  }
  this$static._height = contentHeight;
}

function AbstractBlockConcatenation(){
  this._contents = new AbstractBlockConcatenation$1(this);
}

defineClass(211, 493, {27:1, 57:1, 234:1});
_.draw = function draw(out){
  $draw(this, out);
}
;
_.getFirst = function getFirst(){
  return this._first;
}
;
_.getHeight = function getHeight_0(){
  return this._height;
}
;
_.getLast = function getLast(){
  return this._last;
}
;
_.insertBefore_0 = function insertBefore(before, newBlock){
  $insertBefore(this, before, newBlock);
}
;
_.readPropertyFrom_0 = function readPropertyFrom_3(context, json, name_0){
  $readPropertyFrom_3(this, context, json, name_0);
}
;
_.removeFrom = function removeFrom(start_0){
  return $removeFrom(this, start_0);
}
;
_.updateDimensions = function updateDimensions(context, offsetX, offsetY){
  $updateDimensions(this, context, offsetX, offsetY);
}
;
_._height = 0;
var Lcom_top_1logic_graphic_blocks_model_block_AbstractBlockConcatenation_2_classLit = createForClass('com.top_logic.graphic.blocks.model.block', 'AbstractBlockConcatenation', 211);
function AbstractBlockConcatenation$1(this$0){
  this.this$01 = this$0;
}

defineClass(325, 1, $intern_23, AbstractBlockConcatenation$1);
_.iterator = function iterator_0(){
  return new AbstractBlockConcatenation$1$1(this);
}
;
var Lcom_top_1logic_graphic_blocks_model_block_AbstractBlockConcatenation$1_2_classLit = createForClass('com.top_logic.graphic.blocks.model.block', 'AbstractBlockConcatenation/1', 325);
function $next(this$static){
  var result;
  result = this$static._current;
  this$static._current = this$static._current._next;
  return result;
}

function AbstractBlockConcatenation$1$1(this$1){
  this.this$11 = this$1;
  this._current = this.this$11.this$01._first;
}

defineClass(112, 1, {}, AbstractBlockConcatenation$1$1);
_.forEachRemaining = function forEachRemaining_0(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_1(){
  return $next(this);
}
;
_.remove_0 = function remove_2(){
  $remove_6();
}
;
_.hasNext_0 = function hasNext_1(){
  return !!this._current;
}
;
var Lcom_top_1logic_graphic_blocks_model_block_AbstractBlockConcatenation$1$1_2_classLit = createForClass('com.top_logic.graphic.blocks.model.block', 'AbstractBlockConcatenation/1/1', 112);
function $checkInitOuter(this$static){
  !!this$static._next && $checkInitOuter(this$static._next);
}

function $doInitOuter(this$static, outer){
  this$static._outer = outer;
  !!this$static._next && $doInitOuter(this$static._next, outer);
}

function $draw_0(this$static, out){
  var part, part$iterator;
  out.beginGroup();
  out.writeId(this$static._id);
  out.writeCssClass(this$static._type._cssClass);
  out.translate(this$static._offsetX, this$static._offsetY);
  out.beginPath();
  out.writeCssClass('tlbShape');
  out.beginData();
  $outline(this$static, out);
  out.endData();
  out.endPath();
  out.beginPath();
  out.writeCssClass('tlbBorder');
  out.beginData();
  $outline(this$static, new BevelBorderWriter(out));
  out.endData();
  out.endPath();
  for (part$iterator = new ArrayList$1(this$static._parts); part$iterator.i < part$iterator.this$01.array.length;) {
    part = castTo($next_1(part$iterator), 119);
    part.draw(out);
  }
  out.endGroup();
}

function $forEachConnector(this$static, consumer){
  var part, part$iterator;
  consumer.accept(this$static._topConnector);
  consumer.accept(this$static._bottomConnector);
  for (part$iterator = new ArrayList$1(this$static._parts); part$iterator.i < part$iterator.this$01.array.length;) {
    part = castTo($next_1(part$iterator), 119);
    part.forEachConnector(consumer);
  }
}

function $getBottomLeft(this$static){
  return new Vec(this$static._outer.getX() + this$static._offsetX, this$static._outer.getY() + this$static._offsetY + this$static._height);
}

function $getTopLeft(this$static){
  return new Vec(this$static._outer.getX() + this$static._offsetX, this$static._outer.getY() + this$static._offsetY);
}

function $getX(this$static){
  return this$static._outer.getX() + this$static._offsetX;
}

function $getY(this$static){
  return this$static._outer.getY() + this$static._offsetY;
}

function $last(this$static){
  if (this$static._next) {
    return $last(this$static._next);
  }
  return this$static;
}

function $outline(this$static, out){
  var hasNext, hasPrevious, part, part$iterator;
  out.moveToAbs(0, 2);
  hasPrevious = !!this$static._previous;
  if (hasPrevious) {
    out.lineToVerticalRel(-2);
    out.lineToHorizontalRel(2);
  }
   else {
    out.roundedCorner(false, 2, -2);
  }
  out.lineToHorizontalAbs(10);
  $outline_0(this$static._topConnector, out);
  out.lineToHorizontalAbs(this$static._width - 2);
  out.roundedCorner(false, 2, 2);
  out.lineToVerticalAbs($wnd.Math.max(($clinit_BlockDimensions() , BLOCK_MIN_PADDING_TOP), this$static._type._topType.getHeight() + 1));
  for (part$iterator = new ArrayList$1(this$static._parts); part$iterator.i < part$iterator.this$01.array.length;) {
    part = castTo($next_1(part$iterator), 119);
    part.outlineRight(out);
  }
  out.lineToVerticalAbs(this$static._height - 2);
  hasNext = !!this$static._next;
  out.roundedCorner(false, -2, 2);
  out.lineToHorizontalAbs(30);
  $outline_1(this$static._bottomConnector, out);
  out.lineToHorizontalAbs(2);
  if (hasNext) {
    out.lineToHorizontalRel(-2);
    out.lineToVerticalRel(-2);
  }
   else {
    out.roundedCorner(false, -2, -2);
  }
  out.lineToVerticalAbs(2);
  out.closePath();
}

function $unlinkNext(this$static){
  var result;
  result = this$static._next;
  this$static._next = null;
  !!result && (result._previous = null);
  return result;
}

function $unlinkOuter(this$static){
  this$static._outer = null;
  !!this$static._next && $unlinkOuter(this$static._next);
}

function $updateDimensions_0(this$static, context, offsetX, offsetY){
  var height, part, part$iterator, partHeight, width_0, y_0;
  this$static._offsetX = offsetX;
  this$static._offsetY = offsetY;
  height = $wnd.Math.max(($clinit_BlockDimensions() , BLOCK_MIN_PADDING_TOP), this$static._type._topType.getHeight() + 1) + BLOCK_PADDING_BOTTOM;
  width_0 = 50;
  y_0 = $wnd.Math.max(BLOCK_MIN_PADDING_TOP, this$static._type._topType.getHeight() + 1);
  for (part$iterator = new ArrayList$1(this$static._parts); part$iterator.i < part$iterator.this$01.array.length;) {
    part = castTo($next_1(part$iterator), 119);
    part.updateDimensions(context, 0, y_0);
    partHeight = part.getHeight();
    y_0 += partHeight;
    height += partHeight;
    width_0 = $wnd.Math.max(width_0, part.getWidth());
  }
  this$static._height = height;
  this$static._width = 8 + width_0;
}

function Block(type_0){
  var partType, partType$iterator;
  this._parts = new ArrayList;
  this._topConnector = new Block$1(this);
  this._bottomConnector = new Block$2(this);
  this._type = type_0;
  for (partType$iterator = new ArrayList$1(this._type._parts); partType$iterator.i < partType$iterator.this$01.array.length;) {
    partType = castTo($next_1(partType$iterator), 155);
    $add_0(this._parts, partType.newInstance(this));
  }
}

defineClass(88, 493, {27:1, 57:1, 88:1}, Block);
_.getOwner = function getOwner(){
  return this._outer;
}
;
_.draw = function draw_0(out){
  $draw_0(this, out);
}
;
_.forEachConnector = function forEachConnector(consumer){
  $forEachConnector(this, consumer);
}
;
_.getHeight = function getHeight_1(){
  return this._height;
}
;
_.getX = function getX(){
  return $getX(this);
}
;
_.getY = function getY(){
  return $getY(this);
}
;
_.readPropertyFrom_0 = function readPropertyFrom_4(context, json, name_0){
  var part, part$iterator;
  switch (name_0) {
    case 'parts':
      $beginArray(json);
      for (part$iterator = new ArrayList$1(this._parts); part$iterator.i < part$iterator.this$01.array.length;) {
        part = castTo($next_1(part$iterator), 119);
        part.readFrom(context, json);
      }

      $endArray(json);
      break;
    default:$readPropertyFrom_0(this, context, json, name_0);
  }
}
;
_.top_0 = function top_2(){
  return this._outer.top_0();
}
;
_.updateDimensions = function updateDimensions_0(context, offsetX, offsetY){
  $updateDimensions_0(this, context, offsetX, offsetY);
}
;
_.visit = function visit(v, arg){
  return $visit_3(v, this, throwClassCastExceptionUnlessNull(arg));
}
;
_._height = 0;
_._offsetX = 0;
_._offsetY = 0;
_._width = 0;
var Lcom_top_1logic_graphic_blocks_model_block_Block_2_classLit = createForClass('com.top_logic.graphic.blocks.model.block', 'Block', 88);
function $connectionBottom(this$static){
  switch (this$static.getKind_0()) {
    case 2:
    case 1:
      return this$static.connected();
    default:return this$static;
  }
}

function $connectionTop(this$static){
  switch (this$static.getKind_0()) {
    case 0:
    case 3:
      return this$static.connected();
    default:return this$static;
  }
}

function $draw_2(this$static, out){
  out.beginPath();
  out.writeId(this$static.getId());
  out.writeCssClass('tlbConnector');
  out.beginData();
  out.moveToAbs_0(this$static.getStart());
  this$static.outline(out);
  out.endData();
}

function $match(this$static, sensitive){
  if (!$intersects(this$static.getSensitiveArea(), sensitive.getSensitiveArea())) {
    return $clinit_ConnectorMatch() , NONE_0;
  }
  return $clinit_ConnectorMatch() , new ConnectorMatch$Impl($length($minus_0(this$static.getCenter(), sensitive.getCenter())), this$static);
}

var Lcom_top_1logic_graphic_blocks_model_block_Connector_2_classLit = createForInterface('com.top_logic.graphic.blocks.model.block', 'Connector');
function $outline_0(this$static, out){
  this$static.this$01._type._topType.outline_0(out, 0);
}

function Block$1(this$0){
  this.this$01 = this$0;
}

defineClass(338, 1, $intern_24, Block$1);
_.connectionBottom = function connectionBottom_0(){
  return $connectionBottom(this);
}
;
_.connectionTop = function connectionTop_0(){
  return $connectionTop(this);
}
;
_.draw = function draw_1(out){
  $draw_2(this, out);
}
;
_.match_0 = function match_1(sensitive){
  return $match(this, sensitive);
}
;
_.outline = function outline(out){
  $outline_0(this, out);
}
;
_.connected = function connected(){
  var outer, previous;
  previous = this.this$01._previous;
  if (previous) {
    return previous._bottomConnector;
  }
  outer = this.this$01._outer;
  return instanceOf(outer, 156)?castTo(outer, 156)._topConnector:null;
}
;
_.getCenter = function getCenter_1(){
  return $plus($getTopLeft(this.this$01));
}
;
_.getId = function getId_0(){
  return this.this$01._id + '-top';
}
;
_.getKind_0 = function getKind_4(){
  return 0;
}
;
_.getOwner = function getOwner_0(){
  return this.this$01;
}
;
_.getSensitiveArea = function getSensitiveArea(){
  var x_0, y_0;
  x_0 = $getX(this.this$01) + 10;
  y_0 = $getY(this.this$01);
  return new Rect_0(x_0 - 10, y_0 - 10, x_0 + 20 + 10, y_0);
}
;
_.getStart = function getStart(){
  return new Vec(10, 1);
}
;
_.getType_0 = function getType(){
  return this.this$01._type._topType;
}
;
var Lcom_top_1logic_graphic_blocks_model_block_Block$1_2_classLit = createForClass('com.top_logic.graphic.blocks.model.block', 'Block/1', 338);
function $outline_1(this$static, out){
  this$static.this$01._type._bottomType.outline_0(out, 2);
}

function Block$2(this$0){
  this.this$01 = this$0;
}

defineClass(339, 1, $intern_24, Block$2);
_.connectionBottom = function connectionBottom_1(){
  return $connectionBottom(this);
}
;
_.connectionTop = function connectionTop_1(){
  return $connectionTop(this);
}
;
_.draw = function draw_2(out){
  $draw_2(this, out);
}
;
_.match_0 = function match_2(sensitive){
  return $match(this, sensitive);
}
;
_.outline = function outline_0(out){
  $outline_1(this, out);
}
;
_.connected = function connected_0(){
  var next;
  next = this.this$01._next;
  if (next) {
    return next._topConnector;
  }
  return null;
}
;
_.getCenter = function getCenter_2(){
  return $plus($getBottomLeft(this.this$01));
}
;
_.getId = function getId_1(){
  return this.this$01._id + '-bottom';
}
;
_.getKind_0 = function getKind_5(){
  return 2;
}
;
_.getOwner = function getOwner_1(){
  return this.this$01;
}
;
_.getSensitiveArea = function getSensitiveArea_0(){
  var x_0, y_0;
  y_0 = $getY(this.this$01) + this.this$01._height;
  x_0 = $getX(this.this$01) + 10;
  return new Rect_0(x_0 - 10, y_0, x_0 + 20 + 10, y_0 + 10);
}
;
_.getStart = function getStart_0(){
  return new Vec(30, this.this$01._height - 1);
}
;
_.getType_0 = function getType_0(){
  return this.this$01._type._bottomType;
}
;
var Lcom_top_1logic_graphic_blocks_model_block_Block$2_2_classLit = createForClass('com.top_logic.graphic.blocks.model.block', 'Block/2', 339);
function $clinit_BlockDimensions(){
  $clinit_BlockDimensions = emptyMethod;
  BLOCK_MIN_PADDING_TOP = $wnd.Math.max(2, 2);
  BLOCK_PADDING_BOTTOM = $wnd.Math.max(2, 2) + 1;
  MOUTH_PADDING_TOP = $wnd.Math.max(3, 2) + 1;
  MOUTH_PADDING_BOTTOM = $wnd.Math.max(3, 2) + 3;
}

var BLOCK_MIN_PADDING_TOP = 0, BLOCK_PADDING_BOTTOM = 0, MOUTH_PADDING_BOTTOM = 0, MOUTH_PADDING_TOP = 0;
function $draw_1(this$static, out){
  out.beginGroup();
  out.translate(this$static._x, this$static._y);
  out.writeId(this$static._id);
  $draw(this$static, out);
  out.endGroup();
}

function $setX_5(this$static, x_0){
  this$static._x = x_0;
}

function $setY_5(this$static, y_0){
  this$static._y = y_0;
}

function BlockList(){
  AbstractBlockConcatenation.call(this);
}

function read_0(blockSchema, json){
  var result;
  result = new BlockList;
  $readFrom_0(result, blockSchema, json);
  return result;
}

defineClass(111, 211, {27:1, 57:1, 234:1, 111:1}, BlockList);
_.draw = function draw_3(out){
  $draw_1(this, out);
}
;
_.getOwner = function getOwner_2(){
  return null;
}
;
_.getX = function getX_0(){
  return this._x;
}
;
_.getY = function getY_0(){
  return this._y;
}
;
_.readPropertyFrom_0 = function readPropertyFrom_5(context, json, name_0){
  switch (name_0) {
    case 'x':
      $setX_5(this, $nextDouble(json));
      break;
    case 'y':
      $setY_5(this, $nextDouble(json));
      break;
    default:$readPropertyFrom_3(this, context, json, name_0);
  }
}
;
_.top_0 = function top_3(){
  return this;
}
;
_.visit = function visit_0(v, arg){
  return $visit_0(v, this, arg);
}
;
_._x = 0;
_._y = 0;
var Lcom_top_1logic_graphic_blocks_model_block_BlockList_2_classLit = createForClass('com.top_logic.graphic.blocks.model.block', 'BlockList', 111);
function $clinit_ConnectorMatch(){
  $clinit_ConnectorMatch = emptyMethod;
  NONE_0 = new ConnectorMatch$1;
}

defineClass(590, 1, {});
var NONE_0;
var Lcom_top_1logic_graphic_blocks_model_block_ConnectorMatch_2_classLit = createForClass('com.top_logic.graphic.blocks.model.block', 'ConnectorMatch', 590);
function ConnectorMatch$1(){
}

defineClass(457, 590, {}, ConnectorMatch$1);
_.best = function best(other){
  return other;
}
;
_.getActive = function getActive(){
  throw toJs(new UnsupportedOperationException);
}
;
_.hasMatch = function hasMatch(){
  return false;
}
;
var Lcom_top_1logic_graphic_blocks_model_block_ConnectorMatch$1_2_classLit = createForClass('com.top_logic.graphic.blocks.model.block', 'ConnectorMatch/1', 457);
function ConnectorMatch$Impl(distance, a){
  $clinit_ConnectorMatch();
  this._distance = distance;
  this._a = a;
}

defineClass(182, 590, {182:1}, ConnectorMatch$Impl);
_.best = function best_0(other){
  if (other == NONE_0) {
    return this;
  }
  return this._distance < castTo(other, 182)._distance?this:other;
}
;
_.getActive = function getActive_0(){
  return this._a;
}
;
_.hasMatch = function hasMatch_0(){
  return true;
}
;
_._distance = 0;
var Lcom_top_1logic_graphic_blocks_model_block_ConnectorMatch$Impl_2_classLit = createForClass('com.top_logic.graphic.blocks.model.block', 'ConnectorMatch/Impl', 182);
function ConnectorType(name_0){
  this._name = name_0;
}

defineClass(54, 1, $intern_25);
var Lcom_top_1logic_graphic_blocks_model_connector_ConnectorType_2_classLit = createForClass('com.top_logic.graphic.blocks.model.connector', 'ConnectorType', 54);
function $clinit_BooleanConnector(){
  $clinit_BooleanConnector = emptyMethod;
  INSTANCE_1 = new BooleanConnector;
}

function BooleanConnector(){
  ConnectorType.call(this, 'boolean');
}

defineClass(343, 54, $intern_25, BooleanConnector);
_.getHeight = function getHeight_2(){
  return 5;
}
;
_.outline_0 = function outline_1(out, kind){
  var rtl;
  rtl = $isRightToLeft(kind);
  out.lineToHorizontalRel(rtl?-5:5);
  out.lineToRel(rtl?-5:5, 5);
  out.lineToRel(rtl?-5:5, -5);
  out.lineToHorizontalRel(rtl?-5:5);
}
;
var INSTANCE_1;
var Lcom_top_1logic_graphic_blocks_model_connector_BooleanConnector_2_classLit = createForClass('com.top_logic.graphic.blocks.model.connector', 'BooleanConnector', 343);
function $canConnectTo(this$static, other){
  switch (this$static) {
    case 0:
      return other == 2 || other == 1;
    case 1:
      return other == 0;
    case 3:
      return other == 2;
    case 2:
      return other == 0 || other == 3;
  }
  throw toJs(new UnsupportedOperationException);
}

function $isFemale(this$static){
  switch (this$static) {
    case 0:
    case 3:
      return true;
    case 1:
    case 2:
      return false;
  }
  throw toJs(new UnsupportedOperationException);
}

function $isRightToLeft(this$static){
  switch (this$static) {
    case 1:
    case 2:
      return true;
    case 0:
    case 3:
      return false;
  }
  throw toJs(new UnsupportedOperationException);
}

function $clinit_NumberConnector(){
  $clinit_NumberConnector = emptyMethod;
  INSTANCE_2 = new NumberConnector;
}

function NumberConnector(){
  ConnectorType.call(this, 'number');
}

defineClass(346, 54, $intern_25, NumberConnector);
_.getHeight = function getHeight_3(){
  return 5;
}
;
_.outline_0 = function outline_2(out, kind){
  var deltaW, rtl;
  rtl = $isRightToLeft(kind);
  deltaW = $isFemale(kind)?1:0;
  out.lineToHorizontalRel(rtl?-(5 - deltaW):5 - deltaW);
  out.lineToVerticalRel(5);
  out.lineToHorizontalRel(rtl?-(10 + 2 * deltaW):10 + 2 * deltaW);
  out.lineToVerticalRel(-5);
  out.lineToHorizontalRel(rtl?-(5 - deltaW):5 - deltaW);
}
;
var INSTANCE_2;
var Lcom_top_1logic_graphic_blocks_model_connector_NumberConnector_2_classLit = createForClass('com.top_logic.graphic.blocks.model.connector', 'NumberConnector', 346);
function $clinit_SequenceConnector(){
  $clinit_SequenceConnector = emptyMethod;
  INSTANCE_3 = new SequenceConnector;
}

function SequenceConnector(){
  ConnectorType.call(this, 'sequence');
}

defineClass(345, 54, $intern_25, SequenceConnector);
_.getHeight = function getHeight_4(){
  return 5;
}
;
_.outline_0 = function outline_3(out, kind){
  var rtl;
  rtl = $isRightToLeft(kind);
  out.lineToRel(rtl?-5:5, 5);
  out.lineToHorizontalRel(rtl?-10:10);
  out.lineToRel(rtl?-5:5, -5);
}
;
var INSTANCE_3;
var Lcom_top_1logic_graphic_blocks_model_connector_SequenceConnector_2_classLit = createForClass('com.top_logic.graphic.blocks.model.connector', 'SequenceConnector', 345);
function $clinit_ValueConnector(){
  $clinit_ValueConnector = emptyMethod;
  INSTANCE_4 = new ValueConnector;
}

function ValueConnector(){
  ConnectorType.call(this, 'value');
}

defineClass(344, 54, $intern_25, ValueConnector);
_.getHeight = function getHeight_5(){
  return 5;
}
;
_.outline_0 = function outline_4(out, kind){
  var deltaW, rtl;
  rtl = $isRightToLeft(kind);
  deltaW = $isFemale(kind)?1:0;
  out.lineToHorizontalRel(rtl?-(5 - deltaW):5 - deltaW);
  out.roundedCorner(!rtl, rtl?-(5 + deltaW):5 + deltaW, 5);
  out.roundedCorner(!rtl, rtl?-(5 + deltaW):5 + deltaW, -5);
  out.lineToHorizontalRel(rtl?-(5 - deltaW):5 - deltaW);
}
;
var INSTANCE_4;
var Lcom_top_1logic_graphic_blocks_model_connector_ValueConnector_2_classLit = createForClass('com.top_logic.graphic.blocks.model.connector', 'ValueConnector', 344);
function $clinit_VoidConnector(){
  $clinit_VoidConnector = emptyMethod;
  INSTANCE_5 = new VoidConnector;
}

function VoidConnector(){
  ConnectorType.call(this, 'void');
}

defineClass(342, 54, $intern_25, VoidConnector);
_.getHeight = function getHeight_6(){
  return 0;
}
;
_.outline_0 = function outline_5(out, kind){
  var rtl;
  rtl = $isRightToLeft(kind);
  out.lineToHorizontalRel(rtl?-20:20);
}
;
var INSTANCE_5;
var Lcom_top_1logic_graphic_blocks_model_connector_VoidConnector_2_classLit = createForClass('com.top_logic.graphic.blocks.model.connector', 'VoidConnector', 342);
function read_1(context, json){
  var kind, result;
  $beginArray(json);
  kind = $nextString(json);
  switch (kind) {
    case 'row':
      result = new BlockRowType;
      break;
    case 'mouth':
      result = new MouthType;
      break;
    default:throw toJs(new IllegalArgumentException("No such part type '" + kind + "'."));
  }
  $readFrom_0(result, context, json);
  $endArray(json);
  return result;
}

defineClass(155, 1, {155:1});
_.readFrom = function readFrom_2(context, json){
  $readFrom_0(this, context, json);
}
;
_.readPropertyFrom = function readPropertyFrom_6(context, json, name_0){
  $readPropertyFrom(this, name_0);
}
;
var Lcom_top_1logic_graphic_blocks_model_content_BlockContentType_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content', 'BlockContentType', 155);
function $getHeight(this$static){
  return ($clinit_BlockDimensions() , MOUTH_PADDING_TOP) + MOUTH_PADDING_BOTTOM + this$static._contentHeight;
}

function $getX_0(this$static){
  return $getX(this$static._owner) + this$static._offsetX;
}

function $getY_0(this$static){
  return $getY(this$static._owner) + this$static._offsetY;
}

function Mouth(owner, type_0){
  AbstractBlockConcatenation.call(this);
  this._topConnector = new Mouth$1(this);
  this._bottomConnector = new Mouth$2(this);
  this._owner = owner;
  this._type = type_0;
}

defineClass(156, 211, {27:1, 57:1, 234:1, 119:1, 156:1}, Mouth);
_.getOwner = function getOwner_3(){
  return this._owner;
}
;
_.draw = function draw_4(out){
  out.beginGroup();
  out.writeId(this._id);
  out.translate(this._offsetX, this._offsetY);
  $draw(this, out);
  out.endGroup();
}
;
_.forEachConnector = function forEachConnector_0(consumer){
  consumer.accept(this._topConnector);
  consumer.accept(this._bottomConnector);
}
;
_.getHeight = function getHeight_7(){
  return $getHeight(this);
}
;
_.getWidth = function getWidth_0(){
  return 0;
}
;
_.getX = function getX_1(){
  return $getX_0(this);
}
;
_.getY = function getY_1(){
  return $getY_0(this);
}
;
_.outlineRight = function outlineRight(out){
  var leftInnerWidth;
  leftInnerWidth = this._owner._width - 20 - 11 - 20 - 2;
  out.lineToVerticalRel(($clinit_BlockDimensions() , MOUTH_PADDING_TOP) - 2);
  out.roundedCorner(false, -2, 2);
  out.lineToHorizontalRel(-leftInnerWidth);
  $outline_2(this._topConnector, out);
  out.lineToHorizontalRel(-8);
  out.roundedCorner(true, -3, 3);
  out.lineToVerticalRel(this._contentHeight - 6);
  out.roundedCorner(true, 3, 3);
  out.lineToHorizontalRel(8);
  $outline_3(this._bottomConnector, out);
  out.lineToHorizontalRel(leftInnerWidth);
  out.roundedCorner(false, 2, 2);
  out.lineToVerticalRel(MOUTH_PADDING_BOTTOM - 2);
}
;
_.top_0 = function top_4(){
  return this._owner._outer.top_0();
}
;
_.updateDimensions = function updateDimensions_1(context, offsetX, offsetY){
  var bottomType, content_0, content$iterator, contentHeight, dy, y_0;
  this._offsetX = offsetX + 20;
  this._offsetY = offsetY + ($clinit_BlockDimensions() , MOUTH_PADDING_TOP);
  if (!this._first) {
    contentHeight = 15;
  }
   else {
    y_0 = 1;
    contentHeight = 1;
    for (content$iterator = new AbstractBlockConcatenation$1$1(this._contents); content$iterator._current;) {
      content_0 = $next(content$iterator);
      $updateDimensions_0(content_0, context, 1, y_0);
      dy = content_0._height + 1;
      contentHeight += dy;
      y_0 += dy;
    }
    bottomType = this._last._type._bottomType;
    this._type._bottomType == bottomType || (contentHeight += bottomType.getHeight());
  }
  this._contentHeight = contentHeight;
}
;
_.visit = function visit_1(v, arg){
  return $visit_1(v, this, arg);
}
;
_._contentHeight = 0;
_._offsetX = 0;
_._offsetY = 0;
var Lcom_top_1logic_graphic_blocks_model_content_mouth_Mouth_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.mouth', 'Mouth', 156);
function $outline_2(this$static, out){
  this$static.this$01._type._topType.outline_0(out, 1);
}

function Mouth$1(this$0){
  this.this$01 = this$0;
}

defineClass(441, 1, $intern_24, Mouth$1);
_.connectionBottom = function connectionBottom_2(){
  return $connectionBottom(this);
}
;
_.connectionTop = function connectionTop_2(){
  return $connectionTop(this);
}
;
_.draw = function draw_5(out){
  $draw_2(this, out);
}
;
_.match_0 = function match_3(sensitive){
  return $match(this, sensitive);
}
;
_.outline = function outline_6(out){
  $outline_2(this, out);
}
;
_.connected = function connected_1(){
  var first;
  first = this.this$01._first;
  return !first?null:first._topConnector;
}
;
_.getCenter = function getCenter_3(){
  return new Vec($getX_0(this.this$01) + 10 + 10, $getY_0(this.this$01));
}
;
_.getId = function getId_2(){
  return this.this$01._id + '-top';
}
;
_.getKind_0 = function getKind_6(){
  return 1;
}
;
_.getOwner = function getOwner_4(){
  return this.this$01;
}
;
_.getSensitiveArea = function getSensitiveArea_1(){
  return new Rect_0($getX_0(this.this$01) + 10 - 10, $getY_0(this.this$01), $getX_0(this.this$01) + 10 + 20 + 10, $getY_0(this.this$01) + 10);
}
;
_.getStart = function getStart_1(){
  return new Vec(30, 1);
}
;
_.getType_0 = function getType_1(){
  return this.this$01._type._topType;
}
;
var Lcom_top_1logic_graphic_blocks_model_content_mouth_Mouth$1_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.mouth', 'Mouth/1', 441);
function $outline_3(this$static, out){
  this$static.this$01._type._bottomType.outline_0(out, 3);
}

function Mouth$2(this$0){
  this.this$01 = this$0;
}

defineClass(442, 1, $intern_24, Mouth$2);
_.connectionBottom = function connectionBottom_3(){
  return $connectionBottom(this);
}
;
_.connectionTop = function connectionTop_3(){
  return $connectionTop(this);
}
;
_.draw = function draw_6(out){
  $draw_2(this, out);
}
;
_.match_0 = function match_4(sensitive){
  return $match(this, sensitive);
}
;
_.outline = function outline_7(out){
  $outline_3(this, out);
}
;
_.connected = function connected_2(){
  return null;
}
;
_.getCenter = function getCenter_4(){
  return new Vec($getX_0(this.this$01) + 10 + 10, $getY_0(this.this$01));
}
;
_.getId = function getId_3(){
  return this.this$01._id + '-bottom';
}
;
_.getKind_0 = function getKind_7(){
  return 3;
}
;
_.getOwner = function getOwner_5(){
  return this.this$01;
}
;
_.getSensitiveArea = function getSensitiveArea_2(){
  return new Rect_0($getX_0(this.this$01) + 10 - 10, $getY_0(this.this$01) - 10, $getX_0(this.this$01) + 10 + 20 + 10, $getY_0(this.this$01));
}
;
_.getStart = function getStart_2(){
  return new Vec(10, $getHeight(this.this$01) - 1);
}
;
_.getType_0 = function getType_2(){
  return this.this$01._type._bottomType;
}
;
var Lcom_top_1logic_graphic_blocks_model_content_mouth_Mouth$2_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.mouth', 'Mouth/2', 442);
function $readPropertyFrom_4(this$static, context, json, name_0){
  switch (name_0) {
    case 'topType':
      $setTopType_0(this$static, $getConnectorType(context, $nextString(json)));
      break;
    case 'bottomType':
      $setBottomType_0(this$static, $getConnectorType(context, $nextString(json)));
      break;
    default:$readPropertyFrom(this$static, name_0);
  }
}

function $setBottomType_0(this$static, bottomType){
  this$static._bottomType = bottomType;
}

function $setTopType_0(this$static, topType){
  this$static._topType = topType;
}

function MouthType(){
}

defineClass(422, 155, {155:1}, MouthType);
_.readPropertyFrom = function readPropertyFrom_7(context, json, name_0){
  $readPropertyFrom_4(this, context, json, name_0);
}
;
_.newInstance = function newInstance(owner){
  return new Mouth(owner, this);
}
;
var Lcom_top_1logic_graphic_blocks_model_content_mouth_MouthType_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.mouth', 'MouthType', 422);
function BlockRow(owner, type_0){
  this._owner = owner;
  this._contents = castTo($collect($map_0(new StreamImpl(null, new Spliterators$IteratorSpliterator(type_0._contentTypes, 16)), new BlockRowType$lambda$0$Type(this)), of_1(new Collectors$21methodref$ctor$Type, new Collectors$20methodref$add$Type, new Collectors$lambda$42$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , IDENTITY_FINISH)]))), 13);
}

defineClass(440, 493, {27:1, 57:1, 119:1}, BlockRow);
_.getOwner = function getOwner_6(){
  return this._owner;
}
;
_.draw = function draw_7(out){
  var content_0, content$iterator;
  out.beginGroup();
  out.writeId(this._id);
  for (content$iterator = this._contents.iterator(); content$iterator.hasNext_0();) {
    content_0 = castTo(content$iterator.next_1(), 106);
    content_0.draw(out);
  }
  out.endGroup();
}
;
_.forEachConnector = function forEachConnector_1(consumer){
}
;
_.getHeight = function getHeight_8(){
  return this._height;
}
;
_.getWidth = function getWidth_1(){
  return this._width;
}
;
_.outlineRight = function outlineRight_0(out){
  out.lineToVerticalRel(this._height);
}
;
_.readPropertyFrom_0 = function readPropertyFrom_8(context, json, name_0){
  var part, part$iterator;
  switch (name_0) {
    case 'contents':
      $beginArray(json);
      for (part$iterator = this._contents.iterator(); part$iterator.hasNext_0();) {
        part = castTo(part$iterator.next_1(), 106);
        part.readFrom(context, json);
      }

      $endArray(json);
      break;
    default:$readPropertyFrom_0(this, context, json, name_0);
  }
}
;
_.updateDimensions = function updateDimensions_2(context, offsetX, offsetY){
  var baseLine, content_0, content$iterator, content$iterator0, contentWidth, height, metrics, width_0, xPos;
  width_0 = 5;
  height = 0;
  baseLine = 0;
  xPos = offsetX + 5;
  for (content$iterator0 = this._contents.iterator(); content$iterator0.hasNext_0();) {
    content_0 = castTo(content$iterator0.next_1(), 106);
    content_0.updateDimensions(context, xPos, offsetY);
    metrics = content_0.getMetrics();
    contentWidth = metrics.getWidth();
    width_0 += contentWidth;
    height = $wnd.Math.max(height, metrics.getHeight());
    baseLine = $wnd.Math.max(baseLine, metrics.getBaseLine());
    xPos += contentWidth;
  }
  for (content$iterator = this._contents.iterator(); content$iterator.hasNext_0();) {
    content_0 = castTo(content$iterator.next_1(), 106);
    content_0.setTargetBaseLine(baseLine);
  }
  this._width = width_0;
  this._height = height;
}
;
_.visit = function visit_2(v, arg){
  return $visit_2(v, this, arg);
}
;
_._height = 0;
_._width = 0;
var Lcom_top_1logic_graphic_blocks_model_content_row_BlockRow_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row', 'BlockRow', 440);
function $append(this$static, contentType){
  $add_0(this$static._contentTypes, contentType);
}

function $readPropertyFrom_5(this$static, context, json, name_0){
  var kind, result;
  switch (name_0) {
    case 'contentTypes':
      $beginArray(json);
      while ($hasNext(json)) {
        $append(this$static, ($beginArray(json) , kind = $nextString(json) , result = castTo($getRowPartType(context, kind).get_0(), 102) , $readFrom_0(result, context, json) , $endArray(json) , result));
      }

      $endArray(json);
      break;
    default:$readPropertyFrom(this$static, name_0);
  }
}

function BlockRowType(){
  this._contentTypes = new ArrayList;
}

function lambda$0_2(rowPart_0, t_1){
  return t_1.newInstance_0(rowPart_0);
}

defineClass(420, 155, {155:1}, BlockRowType);
_.readPropertyFrom = function readPropertyFrom_9(context, json, name_0){
  $readPropertyFrom_5(this, context, json, name_0);
}
;
_.newInstance = function newInstance_0(owner){
  return new BlockRow(owner, this);
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_BlockRowType_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row', 'BlockRowType', 420);
function BlockRowType$lambda$0$Type(rowPart_0){
  this.rowPart_0 = rowPart_0;
}

defineClass(421, 1, {}, BlockRowType$lambda$0$Type);
_.apply_0 = function apply_17(arg0){
  return lambda$0_2(this.rowPart_0, castTo(arg0, 102));
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_BlockRowType$lambda$0$Type_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row', 'BlockRowType/lambda$0$Type', 421);
function AbstractRowPart(owner, type_0){
  this._owner = owner;
  this._type = type_0;
}

defineClass(181, 493, $intern_26);
_.getHeight = function getHeight_9(){
  return this.getMetrics().getHeight();
}
;
_.getOwner = function getOwner_7(){
  return this._owner;
}
;
_.setTargetBaseLine = function setTargetBaseLine(baseLine){
  this._baseLine = baseLine;
}
;
_.updateDimensions = function updateDimensions_3(context, offsetX, offsetY){
  this._offsetX = offsetX;
  this._offsetY = offsetY;
}
;
_._baseLine = 0;
_._offsetX = 0;
_._offsetY = 0;
var Lcom_top_1logic_graphic_blocks_model_content_row_part_AbstractRowPart_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row.part', 'AbstractRowPart', 181);
defineClass(102, 1, {102:1});
_.readFrom = function readFrom_3(context, json){
  $readFrom_0(this, context, json);
}
;
_.readPropertyFrom = function readPropertyFrom_10(context, json, name_0){
  $readPropertyFrom(this, name_0);
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_part_RowPartType_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row.part', 'RowPartType', 102);
function $readPropertyFrom_6(this$static, context, json, name_0){
  switch (name_0) {
    case 'id':
      $nextString(json);
      break;
    default:$readPropertyFrom(this$static, name_0);
  }
}

defineClass(504, 102, {102:1});
_.readPropertyFrom = function readPropertyFrom_12(context, json, name_0){
  this.readPropertyFrom_0(context, json, name_0);
}
;
_.readPropertyFrom_0 = function readPropertyFrom_11(context, json, name_0){
  $readPropertyFrom_6(this, context, json, name_0);
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_part_InputType_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row.part', 'InputType', 504);
function LabelDisplay(owner, type_0){
  AbstractRowPart.call(this, owner, type_0);
}

defineClass(453, 181, $intern_26, LabelDisplay);
_.draw = function draw_8(out){
  out.beginGroup();
  out.writeId(this._id);
  out.writeCssClass('tlbLabel');
  out.text_0(this._offsetX, this._offsetY + this._baseLine, castTo(this._type, 150)._text);
  out.endGroup();
}
;
_.getMetrics = function getMetrics(){
  return this._metrics;
}
;
_.updateDimensions = function updateDimensions_4(context, offsetX, offsetY){
  this._offsetX = offsetX;
  this._offsetY = offsetY;
  this._metrics = $measure(context, castTo(this._type, 150)._text);
}
;
_.visit = function visit_3(v, arg){
  return throwClassCastExceptionUnlessNull(arg) , null;
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_part_LabelDisplay_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row.part', 'LabelDisplay', 453);
function $readPropertyFrom_7(this$static, context, json, name_0){
  switch (name_0) {
    case 'text':
      $setText(this$static, $nextString(json));
      break;
    default:$readPropertyFrom(this$static, name_0);
  }
}

function $setText(this$static, text_0){
  this$static._text = text_0;
}

function LabelType(){
}

defineClass(150, 102, {150:1, 102:1}, LabelType);
_.readPropertyFrom = function readPropertyFrom_13(context, json, name_0){
  $readPropertyFrom_7(this, context, json, name_0);
}
;
_.newInstance_0 = function newInstance_1(owner){
  return new LabelDisplay(owner, this);
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_part_LabelType_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row.part', 'LabelType', 150);
function $readPropertyFrom_8(this$static, context, json, name_0){
  switch (name_0) {
    case 'name':
      $setName(this$static, $nextString(json));
      break;
    case 'label':
      $setLabel(this$static, $nextString(json));
      break;
    default:$readPropertyFrom(this$static, name_0);
  }
}

function $setLabel(this$static, label_0){
  this$static._label = label_0;
}

function $setName(this$static, name_0){
  this$static._name = name_0;
}

function Option_0(){
  this._name = null;
  this._label = null;
}

defineClass(117, 1, {117:1}, Option_0);
_.readFrom = function readFrom_4(context, json){
  $readFrom_0(this, context, json);
}
;
_.readPropertyFrom = function readPropertyFrom_14(context, json, name_0){
  $readPropertyFrom_8(this, context, json, name_0);
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_part_Option_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row.part', 'Option', 117);
function $setSelected_0(this$static, selected){
  this$static._selected = selected;
}

function SelectInput(owner, type_0){
  AbstractRowPart.call(this, owner, type_0);
  this._selected = castTo($get_2(type_0._options, 0), 117);
}

defineClass(455, 181, $intern_26, SelectInput);
_.draw = function draw_9(out){
  var baseLine, metrics, x_0;
  x_0 = this._offsetX;
  baseLine = this._offsetY + this._baseLine;
  metrics = this._metrics;
  out.beginGroup();
  out.writeId(this._id);
  out.writeCssClass('tlbSelect');
  out.rect(x_0, baseLine - metrics._baseLine, metrics._width, metrics._height, 1);
  out.text_0(x_0 + 3, baseLine, this._selected._label);
  out.beginPath();
  out.beginData();
  out.moveToRel(x_0 + metrics._width - 3 - 6, baseLine - metrics._baseLine + metrics._height / 2 - 3);
  out.lineToHorizontalRel(6);
  out.lineToRel(-3, 3);
  out.closePath();
  out.endData();
  out.endPath();
  out.endGroup();
}
;
_.getMetrics = function getMetrics_0(){
  return this._metrics;
}
;
_.readPropertyFrom_0 = function readPropertyFrom_15(context, json, name_0){
  switch (name_0) {
    case 'selected':
      $setSelected_0(this, $getOption(castTo(this._type, 175), $nextString(json)));
      break;
    default:$readPropertyFrom_0(this, context, json, name_0);
  }
}
;
_.updateDimensions = function updateDimensions_5(context, offsetX, offsetY){
  this._offsetX = offsetX;
  this._offsetY = offsetY;
  this._metrics = $padding($measure(context, this._selected._label), 3, 3, 3, 15);
}
;
_.visit = function visit_4(v, arg){
  return throwClassCastExceptionUnlessNull(arg) , null;
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_part_SelectInput_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row.part', 'SelectInput', 455);
function $addOption(this$static, option){
  $add_0(this$static._options, option);
}

function $getOption(this$static, name_0){
  var option, option$iterator;
  for (option$iterator = new ArrayList$1(this$static._options); option$iterator.i < option$iterator.this$01.array.length;) {
    option = castTo($next_1(option$iterator), 117);
    if ($equals_0(option._name, name_0)) {
      return option;
    }
  }
  return null;
}

function SelectInputType(){
  this._options = new ArrayList;
}

defineClass(175, 504, {102:1, 175:1}, SelectInputType);
_.newInstance_0 = function newInstance_2(owner){
  return new SelectInput(owner, this);
}
;
_.readPropertyFrom_0 = function readPropertyFrom_16(context, json, name_0){
  var result;
  switch (name_0) {
    case 'options':
      $beginArray(json);
      while ($hasNext(json)) {
        $addOption(this, (result = new Option_0 , $readFrom_0(result, context, json) , result));
      }

      $endArray(json);
      break;
    default:$readPropertyFrom_6(this, context, json, name_0);
  }
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_part_SelectInputType_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row.part', 'SelectInputType', 175);
function $setValue(this$static, value_0){
  this$static._value = value_0;
}

function TextInput(owner, type_0){
  AbstractRowPart.call(this, owner, type_0);
  this._value = type_0._defaultValue;
}

defineClass(454, 181, $intern_26, TextInput);
_.draw = function draw_10(out){
  var baseLine, metrics, x_0;
  x_0 = this._offsetX;
  baseLine = this._offsetY + this._baseLine;
  out.beginGroup();
  out.writeId(this._id);
  out.writeCssClass('tlbInput');
  metrics = this._measure;
  out.rect(x_0, baseLine - metrics._baseLine, metrics._width, metrics._height, 1);
  out.text_0(x_0 + 2, baseLine, this._value);
  out.endGroup();
}
;
_.getMetrics = function getMetrics_1(){
  return this._measure;
}
;
_.readPropertyFrom_0 = function readPropertyFrom_17(context, json, name_0){
  switch (name_0) {
    case 'value':
      $setValue(this, $nextString(json));
      break;
    default:$readPropertyFrom_0(this, context, json, name_0);
  }
}
;
_.updateDimensions = function updateDimensions_6(context, offsetX, offsetY){
  this._offsetX = offsetX;
  this._offsetY = offsetY;
  this._measure = $padding($measure(context, this._value), 0, 2, 0, 2);
}
;
_.visit = function visit_5(v, arg){
  return throwClassCastExceptionUnlessNull(arg) , null;
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_part_TextInput_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row.part', 'TextInput', 454);
function $setDefaultValue(this$static, value_0){
  this$static._defaultValue = value_0;
}

function TextInputType(){
}

defineClass(374, 504, {102:1}, TextInputType);
_.newInstance_0 = function newInstance_3(owner){
  return new TextInput(owner, this);
}
;
_.readPropertyFrom_0 = function readPropertyFrom_18(context, json, name_0){
  switch (name_0) {
    case 'defaultValue':
      $setDefaultValue(this, $nextString(json));
      break;
    default:$readPropertyFrom_6(this, context, json, name_0);
  }
}
;
var Lcom_top_1logic_graphic_blocks_model_content_row_part_TextInputType_2_classLit = createForClass('com.top_logic.graphic.blocks.model.content.row.part', 'TextInputType', 374);
function BlockFactory$0methodref$ctor$Type(){
}

defineClass(333, 1, $intern_27, BlockFactory$0methodref$ctor$Type);
_.get_0 = function get_0(){
  return new LabelType;
}
;
var Lcom_top_1logic_graphic_blocks_model_factory_BlockFactory$0methodref$ctor$Type_2_classLit = createForClass('com.top_logic.graphic.blocks.model.factory', 'BlockFactory/0methodref$ctor$Type', 333);
function BlockFactory$1methodref$ctor$Type(){
}

defineClass(334, 1, $intern_27, BlockFactory$1methodref$ctor$Type);
_.get_0 = function get_1(){
  return new TextInputType;
}
;
var Lcom_top_1logic_graphic_blocks_model_factory_BlockFactory$1methodref$ctor$Type_2_classLit = createForClass('com.top_logic.graphic.blocks.model.factory', 'BlockFactory/1methodref$ctor$Type', 334);
function BlockFactory$2methodref$ctor$Type(){
}

defineClass(335, 1, $intern_27, BlockFactory$2methodref$ctor$Type);
_.get_0 = function get_2(){
  return new SelectInputType;
}
;
var Lcom_top_1logic_graphic_blocks_model_factory_BlockFactory$2methodref$ctor$Type_2_classLit = createForClass('com.top_logic.graphic.blocks.model.factory', 'BlockFactory/2methodref$ctor$Type', 335);
function $moveToAbs_0(this$static, dx, dy){
  this$static._impl.moveToAbs(dx, dy);
}

defineClass(358, 1, {});
_.attachOnClick = function attachOnClick_0(handler, sender){
  return $clinit_Registration() , NONE_1;
}
;
_.beginGroup = function beginGroup_2(){
  this._impl.beginGroup();
}
;
_.beginPath = function beginPath_2(){
  this._impl.beginPath();
}
;
_.beginRect = function beginRect_1(x_0, y_0, w, h){
  this._impl.beginRect(x_0, y_0, w, h);
}
;
_.moveToAbs_0 = function moveToAbs_2(v){
  $moveToAbs_0(this, v._x, v._y);
}
;
_.rect = function rect_2(x_0, y_0, w, h, radius){
  this.rect_0(x_0, y_0, w, h, radius, radius);
}
;
_.rect_0 = function rect_3(x_0, y_0, w, h, rx, ry){
  this._impl.beginRect(x_0, y_0, w, h);
  this._impl.endRect();
}
;
_.roundedCorner = function roundedCorner_0(ccw, rx, ry){
  rx != 0 && ry != 0 && (rx > 0 ^ ry > 0 ^ ccw?this.curveToRel(0, $intern_21 * ry, $intern_22 * rx, ry, rx, ry):this.curveToRel($intern_21 * rx, 0, rx, $intern_22 * ry, rx, ry));
}
;
_.text_0 = function text_2(x_0, y_0, text_0){
  this._impl.beginText(x_0, y_0, text_0);
  this._impl.endText();
}
;
_.write_0 = function write_2(element){
  element.draw(this);
}
;
_.beginData = function beginData_0(){
  this._impl.beginData();
}
;
_.beginGroup_0 = function beginGroup_3(model){
  this._impl.beginGroup();
}
;
_.beginPath_0 = function beginPath_3(model){
  this._impl.beginPath();
}
;
_.beginRect_0 = function beginRect_2(x_0, y_0, w, h, rx, ry){
  this._impl.beginRect(x_0, y_0, w, h);
}
;
_.beginSvg = function beginSvg_0(){
  this._impl.beginSvg();
}
;
_.beginText = function beginText_0(x_0, y_0, text_0){
  this._impl.beginText(x_0, y_0, text_0);
}
;
_.close_0 = function close_6(){
}
;
_.closePath = function closePath_0(){
  this._impl.closePath();
}
;
_.curveToRel = function curveToRel_0(dx1, dy1, dx2, dy2, dx, dy){
  this._impl.curveToRel(dx1, dy1, dx2, dy2, dx, dy);
}
;
_.dimensions = function dimensions_1(width_0, height, x1, y1, x2, y2){
  this._impl.dimensions(width_0, height, x1, y1, x2, y2);
}
;
_.endData = function endData_0(){
  this._impl.endData();
}
;
_.endGroup = function endGroup_0(){
  this._impl.endGroup();
}
;
_.endPath = function endPath_0(){
  this._impl.endPath();
}
;
_.endRect = function endRect_0(){
  this._impl.endRect();
}
;
_.endSvg = function endSvg_0(){
  this._impl.endSvg();
}
;
_.endText = function endText_0(){
  this._impl.endText();
}
;
_.image_0 = function image_0(x_0, y_0, width_0, height, href_0, align_0, scale){
  this._impl.image_0(x_0, y_0, width_0, height, href_0, align_0, scale);
}
;
_.lineToAbs = function lineToAbs_0(dx, dy){
  this._impl.lineToAbs(dx, dy);
}
;
_.lineToHorizontalAbs = function lineToHorizontalAbs_0(dx){
  this._impl.lineToHorizontalAbs(dx);
}
;
_.lineToHorizontalRel = function lineToHorizontalRel_0(dx){
  this._impl.lineToHorizontalRel(dx);
}
;
_.lineToRel = function lineToRel_0(dx, dy){
  this._impl.lineToRel(dx, dy);
}
;
_.lineToVerticalAbs = function lineToVerticalAbs_0(dy){
  this._impl.lineToVerticalAbs(dy);
}
;
_.lineToVerticalRel = function lineToVerticalRel_0(dy){
  this._impl.lineToVerticalRel(dy);
}
;
_.moveToAbs = function moveToAbs_1(dx, dy){
  $moveToAbs_0(this, dx, dy);
}
;
_.moveToRel = function moveToRel_0(dx, dy){
  this._impl.moveToRel(dx, dy);
}
;
_.setFill = function setFill_0(style){
  this._impl.setFill(style);
}
;
_.setStroke = function setStroke_0(style){
  this._impl.setStroke(style);
}
;
_.setStrokeDasharray = function setStrokeDasharray_0(dashes){
  this._impl.setStrokeDasharray(dashes);
}
;
_.setStrokeWidth = function setStrokeWidth_0(value_0){
  this._impl.setStrokeWidth(value_0);
}
;
_.setTextStyle = function setTextStyle_0(fontFamily, fontSize, fontWeight){
  this._impl.setTextStyle(fontFamily, fontSize, fontWeight);
}
;
_.translate = function translate_0(dx, dy){
  this._impl.translate(dx, dy);
}
;
_.writeAttribute = function writeAttribute_0(name_0, value_0){
  this._impl.writeAttribute(name_0, value_0);
}
;
_.writeCssClass = function writeCssClass_0(cssClass){
  this._impl.writeCssClass(cssClass);
}
;
_.writeId = function writeId_0(id_0){
  this._impl.writeId(id_0);
}
;
var Lcom_top_1logic_graphic_blocks_svg_SvgWriterAdapter_2_classLit = createForClass('com.top_logic.graphic.blocks.svg', 'SvgWriterAdapter', 358);
function $rect(this$static, x_0, y_0, w, h, rx, ry){
  if (w < 0) {
    $rect(this$static, x_0 + w, y_0, -w, h, rx, ry);
  }
   else if (h < 0) {
    $rect(this$static, x_0, y_0 + h, w, -h, rx, ry);
  }
   else {
    this$static._impl.moveToRel(x_0, y_0 + h);
    this$static._impl.lineToVerticalRel(-(h + 2 * ry));
    rx != 0 && ry != 0 && (rx > 0 ^ ry > 0?rx >= 0 && ry <= 0?this$static._impl.curveToRel(0, $intern_21 * ry, $intern_22 * rx, ry, rx, ry):this$static._impl.moveToRel(rx, ry):rx >= 0 && ry <= 0?this$static._impl.curveToRel($intern_21 * rx, 0, rx, $intern_22 * ry, rx, ry):this$static._impl.moveToRel(rx, ry));
    this$static._impl.lineToHorizontalRel(w - 2 * rx);
  }
}

function BevelBorderWriter(impl){
  this._impl = impl;
}

defineClass(359, 358, {}, BevelBorderWriter);
_.curveToRel = function curveToRel_1(dx1, dy1, dx2, dy2, dx, dy){
  dx >= 0 && dy <= 0?this._impl.curveToRel(dx1, dy1, dx2, dy2, dx, dy):this._impl.moveToRel(dx, dy);
}
;
_.lineToHorizontalRel = function lineToHorizontalRel_1(dx){
  dx > 0?this._impl.lineToHorizontalRel(dx):this._impl.moveToRel(dx, 0);
}
;
_.lineToRel = function lineToRel_1(dx, dy){
  dx >= 0 && dy <= 0?this._impl.lineToRel(dx, dy):this._impl.moveToRel(dx, dy);
}
;
_.lineToVerticalRel = function lineToVerticalRel_1(dy){
  dy < 0?this._impl.lineToVerticalRel(dy):this._impl.moveToRel(0, dy);
}
;
_.rect_0 = function rect_4(x_0, y_0, w, h, rx, ry){
  $rect(this, x_0, y_0, w, h, rx, ry);
}
;
var Lcom_top_1logic_graphic_blocks_svg_BevelBorderWriter_2_classLit = createForClass('com.top_logic.graphic.blocks.svg', 'BevelBorderWriter', 359);
function valueList(values){
  var buffer, first, x_0, x$array, x$index, x$max;
  buffer = new StringBuilder;
  first = true;
  for (x$array = values , x$index = 0 , x$max = x$array.length; x$index < x$max; ++x$index) {
    x_0 = x$array[x$index];
    first?(first = false):(buffer.string += ' ' , buffer);
    buffer.string += x_0;
  }
  return buffer.string;
}

function TextMetricsImpl(width_0, height, baseLine){
  this._width = width_0;
  this._height = height;
  this._baseLine = baseLine;
}

defineClass(456, 1, {}, TextMetricsImpl);
_.getBaseLine = function getBaseLine_0(){
  return this._baseLine;
}
;
_.getHeight = function getHeight_10(){
  return this._height;
}
;
_.getWidth = function getWidth_2(){
  return this._width;
}
;
_._baseLine = 0;
_._height = 0;
_._width = 0;
var Lcom_top_1logic_graphic_blocks_svg_TextMetricsImpl_2_classLit = createForClass('com.top_logic.graphic.blocks.svg', 'TextMetricsImpl', 456);
function Registration$lambda$0$Type(){
}

defineClass(308, 1, {236:1}, Registration$lambda$0$Type);
_.cancel_0 = function cancel_0(){
  $clinit_Registration();
}
;
var Lcom_top_1logic_graphic_blocks_svg_event_Registration$lambda$0$Type_2_classLit = createForClass('com.top_logic.graphic.blocks.svg.event', 'Registration/lambda$0$Type', 308);
function $computeIfAbsent(this$static, key, remappingFunction){
  var value_0;
  checkCriticalNotNull(remappingFunction);
  value_0 = $get_8(this$static, key);
  if (value_0 == null) {
    value_0 = ($clinit_DefaultScope() , new LinkedHashMap);
    value_0 != null && $put_4(this$static, key, value_0);
  }
  return value_0;
}

function $merge(this$static, key, value_0, remappingFunction){
  var currentValue, newValue;
  checkCriticalNotNull(remappingFunction);
  checkCriticalNotNull(value_0);
  currentValue = this$static.get(key);
  newValue = currentValue == null?value_0:lambda$44();
  newValue == null?this$static.remove(key):this$static.put(key, newValue);
  return newValue;
}

function $clear_0(this$static){
  var prop, prop$iterator;
  for (prop$iterator = this$static.self_3().properties_0().iterator(); prop$iterator.hasNext_0();) {
    prop = castToString(prop$iterator.next_1());
    this$static.self_3().set_0(prop, null);
  }
}

function $lambda$0_0(this$static, k_0){
  return this$static.self_3().get_1(k_0);
}

function $lambda$2_0(this$static, k_0){
  return this$static.self_3().get_1(k_0);
}

function $lambda$4(this$static, k_0){
  return this$static.self_3().get_1(k_0);
}

function $put(this$static, key, value_0){
  var before;
  before = this$static.self_3().get_1(key);
  this$static.self_3().set_0(key, value_0);
  return before;
}

function $putAll(this$static, m){
  var entry, entry$iterator;
  for (entry$iterator = m.entrySet_0().iterator(); entry$iterator.hasNext_0();) {
    entry = castTo(entry$iterator.next_1(), 22);
    this$static.put_0(castToString(entry.getKey()), entry.getValue());
  }
}

function lambda$1_1(value_0, v_1){
  return maskUndefined(v_1) === maskUndefined(value_0) || v_1 != null && equals_Ljava_lang_Object__Z__devirtual$(v_1, value_0);
}

function $getDiagram(this$static){
  var ancestor, box;
  ancestor = this$static._parent;
  while (instanceOf(ancestor, 8)) {
    box = castTo(ancestor, 8);
    ancestor = box.getParent();
  }
  return castTo(ancestor, 463);
}

function readBox(scope_0, in_0){
  var id_0, result, type_0;
  if ($peek_1(in_0) == ($clinit_JsonToken_0() , NUMBER_0)) {
    return castTo(scope_0.resolveOrFail($nextInt(in_0)), 8);
  }
  $beginArray_0(in_0);
  type_0 = $nextString_0(in_0);
  id_0 = $nextInt(in_0);
  switch (type_0) {
    case 'FloatingLayout':
      result = new FloatingLayout_Impl;
      break;
    case 'Text':
      result = new Text_Impl;
      break;
    case 'Image':
      result = new Image_Impl;
      break;
    case 'Empty':
      result = new Empty_Impl;
      break;
    case 'CompassLayout':
      result = new CompassLayout_Impl;
      break;
    case 'TreeLayout':
      result = new TreeLayout_Impl;
      break;
    case 'SelectableBox':
      result = new SelectableBox_Impl;
      break;
    case 'Align':
      result = new Align_Impl;
      break;
    case 'Border':
      result = new Border_Impl;
      break;
    case 'Fill':
      result = new Fill_Impl;
      break;
    case 'Padding':
      result = new Padding_Impl;
      break;
    case 'GridLayout':
      result = new GridLayout_Impl;
      break;
    case 'HorizontalLayout':
      result = new HorizontalLayout_Impl;
      break;
    case 'VerticalLayout':
      result = new VerticalLayout_Impl;
      break;
    default:$skipValue(in_0);
      result = null;
  }
  !!result && scope_0.readData(result, id_0, in_0);
  $endArray_0(in_0);
  return result;
}

var Lcom_top_1logic_graphic_flow_data_Box_2_classLit = createForInterface('com.top_logic.graphic.flow.data', 'Box');
function $computeIntrinsicSize(this$static, context, offsetX, offsetY){
  var content_0;
  content_0 = this$static.self_1().getContent();
  content_0.computeIntrinsicSize(context, offsetX, offsetY);
  this$static.self_1().setWidth(content_0.getWidth());
  this$static.self_1().setHeight(content_0.getHeight());
}

function $distributeSize(this$static, context, offsetX, offsetY, width_0, height){
  var additionalHeight, additionalWidth, bottom, left, right, top_0;
  additionalWidth = width_0 - this$static._width;
  additionalHeight = height - this$static._height;
  switch (this$static._xAlign.ordinal_0) {
    case 0:
      {
        left = 0;
        right = additionalWidth;
        break;
      }

    case 1:
      {
        left = additionalWidth / 2;
        right = additionalWidth / 2;
        break;
      }

    case 2:
      {
        left = additionalWidth;
        right = 0;
        break;
      }

    case 3:
      {
        left = 0;
        right = 0;
        break;
      }

    default:throw toJs(new IllegalArgumentException('Unexpected value: ' + this$static._xAlign));
  }
  switch (this$static._yAlign.ordinal_0) {
    case 0:
      {
        top_0 = 0;
        bottom = additionalHeight;
        break;
      }

    case 1:
      {
        top_0 = additionalHeight / 2;
        bottom = additionalHeight / 2;
        break;
      }

    case 2:
      {
        top_0 = additionalHeight;
        bottom = 0;
        break;
      }

    case 3:
      {
        top_0 = 0;
        bottom = 0;
        break;
      }

    default:throw toJs(new IllegalArgumentException('Unexpected value: ' + this$static._yAlign));
  }
  this$static._content.distributeSize(context, offsetX + left, offsetY + top_0, width_0 - left - right, height - top_0 - bottom);
  this$static._listener.beforeSet(this$static, 'x', offsetX);
  this$static._x = offsetX;
  this$static._listener.beforeSet(this$static, 'y', offsetY);
  this$static._y = offsetY;
  this$static._listener.beforeSet(this$static, 'width', width_0);
  this$static._width = width_0;
  this$static._listener.beforeSet(this$static, 'height', height);
  this$static._height = height;
}

function $clinit_Alignment(){
  $clinit_Alignment = emptyMethod;
  START = new Alignment('START', 0, 'start');
  MIDDLE = new Alignment('MIDDLE', 1, 'middle');
  STOP = new Alignment('STOP', 2, 'stop');
  STRECH = new Alignment('STRECH', 3, 'strech');
}

function $writeTo_1(this$static, out){
  $value_4(out, this$static._protocolName);
}

function Alignment(enum$name, enum$ordinal, protocolName){
  Enum.call(this, enum$name, enum$ordinal);
  this._protocolName = protocolName;
}

function valueOfProtocol(protocolName){
  $clinit_Alignment();
  if (protocolName == null) {
    return null;
  }
  switch (protocolName) {
    case 'start':
      return START;
    case 'middle':
      return MIDDLE;
    case 'stop':
      return STOP;
    case 'strech':
      return STRECH;
  }
  return START;
}

function values_5(){
  $clinit_Alignment();
  return stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_graphic_flow_data_Alignment_2_classLit, 1), $intern_0, 90, 0, [START, MIDDLE, STOP, STRECH]);
}

defineClass(90, 29, {90:1, 186:1, 3:1, 33:1, 29:1}, Alignment);
var MIDDLE, START, STOP, STRECH;
var Lcom_top_1logic_graphic_flow_data_Alignment_2_classLit = createForEnum('com.top_logic.graphic.flow.data', 'Alignment', 90, values_5);
function $distributeSize_0(this$static, context, offsetX, offsetY, width_0, height){
  var contentHeight, contentWidth, contentX, contentY;
  contentX = offsetX;
  contentY = offsetY;
  contentWidth = width_0;
  contentHeight = height;
  if (this$static._top) {
    contentY += this$static._thickness;
    contentHeight -= this$static._thickness;
  }
  if (this$static._left) {
    contentX += this$static._thickness;
    contentWidth -= this$static._thickness;
  }
  this$static._bottom && (contentHeight -= this$static._thickness);
  this$static._right && (contentWidth -= this$static._thickness);
  this$static._content.distributeSize(context, contentX, contentY, contentWidth, contentHeight);
  this$static._listener.beforeSet(this$static, 'x', offsetX);
  this$static._x = offsetX;
  this$static._listener.beforeSet(this$static, 'y', offsetY);
  this$static._y = offsetY;
  this$static._listener.beforeSet(this$static, 'width', width_0);
  this$static._width = width_0;
  this$static._listener.beforeSet(this$static, 'height', height);
  this$static._height = height;
}

function $draw_3(this$static, out){
  var clickHandler;
  clickHandler = this$static._clickHandler;
  !!clickHandler && clickHandler.cancel_0();
  out.beginSvg();
  out.writeCssClass(this$static._cssClass);
  out.dimensions('' + this$static._root.getWidth(), '' + this$static._root.getHeight(), 0, 0, this$static._root.getWidth(), this$static._root.getHeight());
  out.write_0(this$static._root);
  $setClickHandler(this$static, out.attachOnClick(this$static, this$static));
  out.endSvg();
}

function $onClick(this$static, event_0){
  var nativeButton, selected, selected$iterator;
  nativeButton = $eventGetButton(event_0.val$event3.nativeEvent);
  if ((nativeButton & 1) == 0) {
    return;
  }
  if (!!event_0.val$event3.nativeEvent.shiftKey || !!event_0.val$event3.nativeEvent.ctrlKey)
  ;
  else {
    for (selected$iterator = new ArrayList$1(this$static._selection); selected$iterator.i < selected$iterator.this$01.array.length;) {
      selected = castTo($next_1(selected$iterator), 92);
      selected._listener.beforeSet(selected, 'selected', ($clinit_Boolean() , false));
      selected._selected = false;
    }
    $setSelection(this$static, ($clinit_Collections() , $clinit_Collections() , EMPTY_LIST));
  }
  event_0.val$event3.nativeEvent.stopPropagation();
}

function readDiagram(scope_0, in_0){
  var id_0, result;
  if ($peek_1(in_0) == ($clinit_JsonToken_0() , NUMBER_0)) {
    return castTo($resolveOrFail(scope_0, $nextInt(in_0)), 463);
  }
  $beginArray_0(in_0);
  $nextString_0(in_0);
  id_0 = $nextInt(in_0);
  result = new Diagram_Impl;
  $readData(scope_0, result, id_0, in_0);
  $endArray_0(in_0);
  return result;
}

function $clinit_DiagramDirection(){
  $clinit_DiagramDirection = emptyMethod;
  LTR = new DiagramDirection('LTR', 0, 'ltr');
  RTL = new DiagramDirection('RTL', 1, 'rtl');
  TOPDOWN = new DiagramDirection('TOPDOWN', 2, 'topdown');
  BOTTOMUP = new DiagramDirection('BOTTOMUP', 3, 'bottomup');
}

function $writeTo_2(this$static, out){
  $value_4(out, this$static._protocolName);
}

function DiagramDirection(enum$name, enum$ordinal, protocolName){
  Enum.call(this, enum$name, enum$ordinal);
  this._protocolName = protocolName;
}

function valueOfProtocol_0(protocolName){
  $clinit_DiagramDirection();
  if (protocolName == null) {
    return null;
  }
  switch (protocolName) {
    case 'ltr':
      return LTR;
    case 'rtl':
      return RTL;
    case 'topdown':
      return TOPDOWN;
    case 'bottomup':
      return BOTTOMUP;
  }
  return LTR;
}

function values_6(){
  $clinit_DiagramDirection();
  return stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_graphic_flow_data_DiagramDirection_2_classLit, 1), $intern_0, 103, 0, [LTR, RTL, TOPDOWN, BOTTOMUP]);
}

defineClass(103, 29, {103:1, 186:1, 3:1, 33:1, 29:1}, DiagramDirection);
var BOTTOMUP, LTR, RTL, TOPDOWN;
var Lcom_top_1logic_graphic_flow_data_DiagramDirection_2_classLit = createForEnum('com.top_logic.graphic.flow.data', 'DiagramDirection', 103, values_6);
function $computeIntrinsicSize_0(this$static, context, offsetX, offsetY){
  var height, node, node$iterator, width_0;
  this$static.self_4().setX_1(offsetX);
  this$static.self_4().setY_1(offsetY);
  width_0 = 0;
  height = 0;
  for (node$iterator = new ArrayList$1(this$static.self_4().getNodes()); node$iterator.i < node$iterator.this$01.array.length;) {
    node = castTo($next_1(node$iterator), 8);
    node.computeIntrinsicSize(context, node.getX(), node.getY());
    width_0 = $wnd.Math.max(width_0, node.getX() + node.getWidth());
    height = $wnd.Math.max(height, node.getY() + node.getHeight());
  }
  this$static.self_4().setWidth_0(width_0);
  this$static.self_4().setHeight_0(height);
}

function $distributeSize_1(this$static, context, offsetX, offsetY, width_0, height){
  var node, node$iterator, nodeHeight, nodeWidth, nodeX, nodeY;
  for (node$iterator = new ArrayList$1(this$static.self_4().getNodes()); node$iterator.i < node$iterator.this$01.array.length;) {
    node = castTo($next_1(node$iterator), 8);
    nodeWidth = $wnd.Math.min(width_0, node.getWidth());
    nodeHeight = $wnd.Math.min(height, node.getHeight());
    nodeX = $wnd.Math.min(width_0 - nodeWidth, node.getX());
    nodeY = $wnd.Math.min(height - nodeHeight, node.getY());
    node.distributeSize(context, nodeX, nodeY, nodeWidth, nodeHeight);
  }
  this$static.self_4().setX_1(offsetX);
  this$static.self_4().setY_1(offsetY);
  this$static.self_4().setWidth_0(width_0);
  this$static.self_4().setHeight_0(height);
}

function $drawContents(this$static, out){
  var node, node$iterator;
  for (node$iterator = new ArrayList$1(this$static.self_4().getNodes()); node$iterator.i < node$iterator.this$01.array.length;) {
    node = castTo($next_1(node$iterator), 8);
    out.write_0(node);
  }
}

function $computeIntrinsicSize_1(this$static, context, offsetX, offsetY){
  var c, element, h, h$iterator, height, r, w, w$iterator, width_0;
  this$static._listener.beforeSet(this$static, 'x', offsetX);
  this$static._x = offsetX;
  this$static._listener.beforeSet(this$static, 'y', offsetY);
  this$static._y = offsetY;
  $setRowHeight(this$static, doubleList(this$static._rows));
  $setColWidth(this$static, doubleList(this$static._cols));
  for (r = 0; r < this$static._rows; r++) {
    for (c = 0; c < this$static._cols; c++) {
      element = $get_5(this$static, c, r);
      element.computeIntrinsicSize(context, offsetX, offsetY);
      $set_3(this$static._colWidth, c, $wnd.Math.max($doubleValue(castToDouble($get_2(this$static._colWidth, c))), element.getWidth()));
      $set_3(this$static._rowHeight, r, $wnd.Math.max($doubleValue(castToDouble($get_2(this$static._rowHeight, r))), element.getHeight()));
    }
  }
  height = 0;
  for (h$iterator = new ArrayList$1(this$static._rowHeight); h$iterator.i < h$iterator.this$01.array.length;) {
    h = $doubleValue(castToDouble($next_1(h$iterator)));
    height += h;
  }
  this$static._rows > 0 && (height += this$static._gapY * (this$static._rows - 1));
  width_0 = 0;
  for (w$iterator = new ArrayList$1(this$static._colWidth); w$iterator.i < w$iterator.this$01.array.length;) {
    w = $doubleValue(castToDouble($next_1(w$iterator)));
    width_0 += w;
  }
  this$static._cols > 0 && (width_0 += this$static._gapX * (this$static._cols - 1));
  this$static._listener.beforeSet(this$static, 'width', width_0);
  this$static._width = width_0;
  this$static._listener.beforeSet(this$static, 'height', height);
  this$static._height = height;
}

function $distributeSize_2(this$static, context, offsetX, offsetY, width_0, height){
  var additionalHeight, additionalWidth, c, c0, element, r, r0, x_0, y_0;
  this$static._listener.beforeSet(this$static, 'x', offsetX);
  this$static._x = offsetX;
  this$static._listener.beforeSet(this$static, 'y', offsetY);
  this$static._y = offsetY;
  additionalWidth = (width_0 - this$static._width) / this$static._cols;
  additionalHeight = (height - this$static._height) / this$static._rows;
  for (c0 = 0; c0 < this$static._cols; c0++) {
    $set_3(this$static._colWidth, c0, $doubleValue(castToDouble($get_2(this$static._colWidth, c0))) + additionalWidth);
  }
  for (r0 = 0; r0 < this$static._rows; r0++) {
    $set_3(this$static._rowHeight, r0, $doubleValue(castToDouble($get_2(this$static._rowHeight, r0))) + additionalHeight);
  }
  y_0 = offsetY;
  for (r = 0; r < this$static._rows; r++) {
    x_0 = offsetX;
    for (c = 0; c < this$static._cols; c++) {
      element = $get_5(this$static, c, r);
      element.distributeSize(context, x_0, y_0, $doubleValue(castToDouble($get_2(this$static._colWidth, c))), $doubleValue(castToDouble($get_2(this$static._rowHeight, r))));
      x_0 += $doubleValue(castToDouble($get_2(this$static._colWidth, c))) + this$static._gapX;
    }
    y_0 += $doubleValue(castToDouble($get_2(this$static._rowHeight, r))) + this$static._gapY;
  }
  this$static._listener.beforeSet(this$static, 'width', width_0);
  this$static._width = width_0;
  this$static._listener.beforeSet(this$static, 'height', height);
  this$static._height = height;
}

function $draw_4(this$static, out){
  var c, element, r;
  for (r = 0; r < this$static._rows; r++) {
    for (c = 0; c < this$static._cols; c++) {
      element = $get_5(this$static, c, r);
      out.write_0(element);
    }
  }
}

function $get_5(this$static, col, row){
  var contents, index_0;
  index_0 = $index_0(this$static, col, row);
  contents = this$static._contents;
  while (contents.array.length < index_0 + 1) {
    $add_1(contents, new Empty_Impl);
  }
  return checkCriticalElementIndex(index_0, contents.array.length) , castTo(contents.array[index_0], 8);
}

function $index_0(this$static, col, row){
  if (col < 0 || col >= this$static._cols || row < 0 || row >= this$static._rows) {
    throw toJs(new IllegalArgumentException('No such element: ' + col + ', ' + row));
  }
  return col + row * this$static._cols;
}

function $computeIntrinsicSize_2(this$static, context, offsetX, offsetY){
  var col, gap, maxHeight, n, width_0, x_0, y_0;
  width_0 = 0;
  maxHeight = 0;
  this$static._listener.beforeSet(this$static, 'x', offsetX);
  this$static._x = offsetX;
  this$static._listener.beforeSet(this$static, 'y', offsetY);
  this$static._y = offsetY;
  x_0 = offsetX;
  y_0 = offsetY;
  gap = this$static._gap;
  for (n = 0; n < this$static._contents.array.length; n++) {
    col = castTo($get_2(this$static._contents, n), 8);
    n > 0 && (width_0 += gap);
    x_0 = offsetX + width_0;
    col.computeIntrinsicSize(context, x_0, y_0);
    width_0 += col.getWidth();
    maxHeight = $wnd.Math.max(maxHeight, col.getHeight());
  }
  this$static._listener.beforeSet(this$static, 'width', width_0);
  this$static._width = width_0;
  this$static._listener.beforeSet(this$static, 'height', maxHeight);
  this$static._height = maxHeight;
}

function $distributeSize_3(this$static, context, offsetX, offsetY, width_0, height){
  var additionalSpace, additionalWidth, cnt, col, elementWidth, elementX, gap, n;
  this$static._listener.beforeSet(this$static, 'x', offsetX);
  this$static._x = offsetX;
  this$static._listener.beforeSet(this$static, 'y', offsetY);
  this$static._y = offsetY;
  additionalSpace = width_0 - this$static._width;
  cnt = this$static._contents.array.length;
  additionalWidth = this$static._fill == ($clinit_SpaceDistribution() , STRETCH_CONTENT) && cnt > 0?additionalSpace / cnt:0;
  gap = this$static._gap + (this$static._fill == STRETCH_GAP && cnt > 1?additionalSpace / (cnt - 1):0);
  elementX = offsetX;
  for (n = 0; n < cnt; n++) {
    col = castTo($get_2(this$static._contents, n), 8);
    elementWidth = col.getWidth() + additionalWidth;
    col.distributeSize(context, elementX, offsetY, elementWidth, height);
    elementX += elementWidth;
    elementX += gap;
  }
  this$static._listener.beforeSet(this$static, 'width', width_0);
  this$static._width = width_0;
  this$static._listener.beforeSet(this$static, 'height', height);
  this$static._height = height;
}

function $draw_5(this$static, out){
  var e, e$iterator;
  for (e$iterator = new ArrayList$1(this$static._contents); e$iterator.i < e$iterator.this$01.array.length;) {
    e = castTo($next_1(e$iterator), 8);
    out.write_0(e);
  }
}

function $clinit_ImageAlign(){
  $clinit_ImageAlign = emptyMethod;
  X_MID_YMID = new ImageAlign('X_MID_YMID', 0, 'xMidYMid');
  X_MIN_YMIN = new ImageAlign('X_MIN_YMIN', 1, 'xMinYMin');
  X_MID_YMIN = new ImageAlign('X_MID_YMIN', 2, 'xMidYMin');
  X_MAX_YMIN = new ImageAlign('X_MAX_YMIN', 3, 'xMaxYMin');
  X_MIN_YMID = new ImageAlign('X_MIN_YMID', 4, 'xMinYMid');
  X_MAX_YMID = new ImageAlign('X_MAX_YMID', 5, 'xMaxYMid');
  X_MIN_YMAX = new ImageAlign('X_MIN_YMAX', 6, 'xMinYMax');
  X_MID_YMAX = new ImageAlign('X_MID_YMAX', 7, 'xMidYMax');
  X_MAX_YMAX = new ImageAlign('X_MAX_YMAX', 8, 'xMaxYMax');
  NONE_2 = new ImageAlign('NONE', 9, 'none');
}

function $writeTo_3(this$static, out){
  $value_4(out, this$static._protocolName);
}

function ImageAlign(enum$name, enum$ordinal, protocolName){
  Enum.call(this, enum$name, enum$ordinal);
  this._protocolName = protocolName;
}

function valueOfProtocol_1(protocolName){
  $clinit_ImageAlign();
  if (protocolName == null) {
    return null;
  }
  switch (protocolName) {
    case 'xMidYMid':
      return X_MID_YMID;
    case 'xMinYMin':
      return X_MIN_YMIN;
    case 'xMidYMin':
      return X_MID_YMIN;
    case 'xMaxYMin':
      return X_MAX_YMIN;
    case 'xMinYMid':
      return X_MIN_YMID;
    case 'xMaxYMid':
      return X_MAX_YMID;
    case 'xMinYMax':
      return X_MIN_YMAX;
    case 'xMidYMax':
      return X_MID_YMAX;
    case 'xMaxYMax':
      return X_MAX_YMAX;
    case 'none':
      return NONE_2;
  }
  return X_MID_YMID;
}

function values_7(){
  $clinit_ImageAlign();
  return stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_graphic_flow_data_ImageAlign_2_classLit, 1), $intern_0, 50, 0, [X_MID_YMID, X_MIN_YMIN, X_MID_YMIN, X_MAX_YMIN, X_MIN_YMID, X_MAX_YMID, X_MIN_YMAX, X_MID_YMAX, X_MAX_YMAX, NONE_2]);
}

defineClass(50, 29, {50:1, 186:1, 3:1, 33:1, 29:1}, ImageAlign);
var NONE_2, X_MAX_YMAX, X_MAX_YMID, X_MAX_YMIN, X_MID_YMAX, X_MID_YMID, X_MID_YMIN, X_MIN_YMAX, X_MIN_YMID, X_MIN_YMIN;
var Lcom_top_1logic_graphic_flow_data_ImageAlign_2_classLit = createForEnum('com.top_logic.graphic.flow.data', 'ImageAlign', 50, values_7);
function $clinit_ImageScale(){
  $clinit_ImageScale = emptyMethod;
  MEET = new ImageScale('MEET', 0, 'meet');
  SLICE = new ImageScale('SLICE', 1, 'slice');
}

function $writeTo_4(this$static, out){
  $value_4(out, this$static._protocolName);
}

function ImageScale(enum$name, enum$ordinal, protocolName){
  Enum.call(this, enum$name, enum$ordinal);
  this._protocolName = protocolName;
}

function valueOfProtocol_2(protocolName){
  $clinit_ImageScale();
  if (protocolName == null) {
    return null;
  }
  switch (protocolName) {
    case 'meet':
      return MEET;
    case 'slice':
      return SLICE;
  }
  return MEET;
}

function values_8(){
  $clinit_ImageScale();
  return stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_graphic_flow_data_ImageScale_2_classLit, 1), $intern_0, 137, 0, [MEET, SLICE]);
}

defineClass(137, 29, {137:1, 186:1, 3:1, 33:1, 29:1}, ImageScale);
var MEET, SLICE;
var Lcom_top_1logic_graphic_flow_data_ImageScale_2_classLit = createForEnum('com.top_logic.graphic.flow.data', 'ImageScale', 137, values_8);
function $onClick_0(this$static, event_0){
  var diagram, nativeButton, selected, selected$iterator;
  nativeButton = $eventGetButton(event_0.val$event3.nativeEvent);
  if ((nativeButton & 1) == 0) {
    return;
  }
  diagram = $getDiagram(this$static);
  if (this$static._selected) {
    if (event_0.val$event3.nativeEvent.ctrlKey) {
      $remove_0(diagram._selection, this$static);
      this$static._listener.beforeSet(this$static, 'selected', ($clinit_Boolean() , false));
      this$static._selected = false;
    }
     else if (event_0.val$event3.nativeEvent.shiftKey)
    ;
    else {
      for (selected$iterator = new ArrayList$1(diagram._selection); selected$iterator.i < selected$iterator.this$01.array.length;) {
        selected = castTo($next_1(selected$iterator), 92);
        if (selected == this$static) {
          continue;
        }
        selected._listener.beforeSet(selected, 'selected', ($clinit_Boolean() , false));
        selected._selected = false;
      }
      $setSelection(diagram, ($clinit_Collections() , new Collections$SingletonList(this$static)));
    }
  }
   else {
    if (!!event_0.val$event3.nativeEvent.shiftKey || !!event_0.val$event3.nativeEvent.ctrlKey) {
      $add_1(diagram._selection, this$static);
    }
     else {
      for (selected$iterator = new ArrayList$1(diagram._selection); selected$iterator.i < selected$iterator.this$01.array.length;) {
        selected = castTo($next_1(selected$iterator), 92);
        if (selected == this$static) {
          continue;
        }
        selected._listener.beforeSet(selected, 'selected', ($clinit_Boolean() , false));
        selected._selected = false;
      }
      $setSelection(diagram, ($clinit_Collections() , new Collections$SingletonList(this$static)));
    }
    this$static._listener.beforeSet(this$static, 'selected', ($clinit_Boolean() , true));
    this$static._selected = true;
  }
  event_0.val$event3.nativeEvent.stopPropagation();
}

function readSelectableBox(scope_0, in_0){
  var id_0, result;
  if ($peek_1(in_0) == ($clinit_JsonToken_0() , NUMBER_0)) {
    return castTo(scope_0.resolveOrFail($nextInt(in_0)), 92);
  }
  $beginArray_0(in_0);
  $nextString_0(in_0);
  id_0 = $nextInt(in_0);
  result = new SelectableBox_Impl;
  scope_0.readData(result, id_0, in_0);
  $endArray_0(in_0);
  return result;
}

var Lcom_top_1logic_graphic_flow_data_SelectableBox_2_classLit = createForInterface('com.top_logic.graphic.flow.data', 'SelectableBox');
function $clinit_SpaceDistribution(){
  $clinit_SpaceDistribution = emptyMethod;
  NONE_3 = new SpaceDistribution('NONE', 0, 'NONE');
  STRETCH_CONTENT = new SpaceDistribution('STRETCH_CONTENT', 1, 'STRETCH_CONTENT');
  STRETCH_GAP = new SpaceDistribution('STRETCH_GAP', 2, 'STRETCH_GAP');
  STRETCH_ALL = new SpaceDistribution('STRETCH_ALL', 3, 'STRETCH_ALL');
}

function $writeTo_5(this$static, out){
  $value_4(out, this$static._protocolName);
}

function SpaceDistribution(enum$name, enum$ordinal, protocolName){
  Enum.call(this, enum$name, enum$ordinal);
  this._protocolName = protocolName;
}

function valueOfProtocol_3(protocolName){
  $clinit_SpaceDistribution();
  if (protocolName == null) {
    return null;
  }
  switch (protocolName) {
    case 'NONE':
      return NONE_3;
    case 'STRETCH_CONTENT':
      return STRETCH_CONTENT;
    case 'STRETCH_GAP':
      return STRETCH_GAP;
    case 'STRETCH_ALL':
      return STRETCH_ALL;
  }
  return NONE_3;
}

function values_9(){
  $clinit_SpaceDistribution();
  return stampJavaTypeInfo(getClassLiteralForArray(Lcom_top_1logic_graphic_flow_data_SpaceDistribution_2_classLit, 1), $intern_0, 104, 0, [NONE_3, STRETCH_CONTENT, STRETCH_GAP, STRETCH_ALL]);
}

defineClass(104, 29, {104:1, 186:1, 3:1, 33:1, 29:1}, SpaceDistribution);
var NONE_3, STRETCH_ALL, STRETCH_CONTENT, STRETCH_GAP;
var Lcom_top_1logic_graphic_flow_data_SpaceDistribution_2_classLit = createForEnum('com.top_logic.graphic.flow.data', 'SpaceDistribution', 104, values_9);
function $draw_7(this$static, out){
  var barX, child, child$iterator, childX, childY, fromX, fromY, maxY, minY, parent_0;
  parent_0 = this$static._parent;
  fromX = parent_0._connection._parent == parent_0?parent_0._anchor.getRightX():parent_0._anchor.getX();
  fromY = parent_0._anchor.getY() + parent_0._connectPosition * parent_0._anchor.getHeight();
  barX = this$static._barPosition;
  out.beginPath();
  out.setStroke(this$static._owner._strokeStyle);
  out.setStrokeWidth(this$static._owner._thickness);
  out.setFill('none');
  out.beginData();
  out.moveToAbs(fromX, fromY);
  out.lineToHorizontalAbs(barX);
  minY = $intern_28;
  maxY = 4.9E-324;
  for (child$iterator = new ArrayList$1(this$static._children); child$iterator.i < child$iterator.this$01.array.length;) {
    child = castTo($next_1(child$iterator), 74);
    childX = child._connection._parent == child?child._anchor.getRightX():child._anchor.getX();
    childY = child._anchor.getY() + child._connectPosition * child._anchor.getHeight();
    out.moveToAbs(childX, childY);
    out.lineToAbs(barX, childY);
    out.lineToAbs(barX, fromY);
    minY = $wnd.Math.min(minY, childY);
    maxY = $wnd.Math.max(maxY, childY);
  }
  out.endData();
  out.endPath();
}

function readTreeConnection(scope_0, in_0){
  var id_0, result;
  if ($peek_1(in_0) == ($clinit_JsonToken_0() , NUMBER_0)) {
    return castTo(scope_0.resolveOrFail($nextInt(in_0)), 81);
  }
  $beginArray_0(in_0);
  $nextString_0(in_0);
  id_0 = $nextInt(in_0);
  result = new TreeConnection_Impl;
  scope_0.readData(result, id_0, in_0);
  $endArray_0(in_0);
  return result;
}

var Lcom_top_1logic_graphic_flow_data_TreeConnection_2_classLit = createForInterface('com.top_logic.graphic.flow.data', 'TreeConnection');
function readTreeConnector(scope_0, in_0){
  var id_0, result;
  if ($peek_1(in_0) == ($clinit_JsonToken_0() , NUMBER_0)) {
    return castTo(scope_0.resolveOrFail($nextInt(in_0)), 74);
  }
  $beginArray_0(in_0);
  $nextString_0(in_0);
  id_0 = $nextInt(in_0);
  result = new TreeConnector_Impl;
  scope_0.readData(result, id_0, in_0);
  $endArray_0(in_0);
  return result;
}

var Lcom_top_1logic_graphic_flow_data_TreeConnector_2_classLit = createForInterface('com.top_logic.graphic.flow.data', 'TreeConnector');
function $computeIntrinsicSize_4(this$static, context, offsetX, offsetY){
  var bottomY, child, child$iterator, childNode, childNode$iterator, childNodes, childrenForParentNode, column, column$iterator, columns, connection, connection$iterator, connection$iterator0, maxWidth, minX, node, node$iterator, node$iterator0, nodeForAnchor, nodeSet, parentForChildNode, parentNode, root, root$iterator, roots;
  $computeIntrinsicSize_0(this$static, context, offsetX, offsetY);
  nodeSet = new HashSet_1(this$static._nodes);
  nodeForAnchor = new HashMap;
  for (connection$iterator0 = new ArrayList$1(this$static._connections); connection$iterator0.i < connection$iterator0.this$01.array.length;) {
    connection = castTo($next_1(connection$iterator0), 81);
    $enterAnchor(nodeSet, nodeForAnchor, connection._parent);
    for (child$iterator = new ArrayList$1(connection._children); child$iterator.i < child$iterator.this$01.array.length;) {
      child = castTo($next_1(child$iterator), 74);
      $enterAnchor(nodeSet, nodeForAnchor, child);
    }
  }
  parentForChildNode = new HashMap;
  childrenForParentNode = new HashMap;
  for (connection$iterator = new ArrayList$1(this$static._connections); connection$iterator.i < connection$iterator.this$01.array.length;) {
    connection = castTo($next_1(connection$iterator), 81);
    parentNode = castTo($get_6(nodeForAnchor, connection._parent._anchor), 8);
    childNodes = castTo($collect($map_0($map_0(new StreamImpl(null, new Spliterators$IteratorSpliterator(connection._children, 16)), new TreeLayoutOperations$0methodref$getAnchor$Type), new TreeLayoutOperations$1methodref$get$Type(nodeForAnchor)), of_1(new Collectors$21methodref$ctor$Type, new Collectors$20methodref$add$Type, new Collectors$lambda$42$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , IDENTITY_FINISH)]))), 13);
    for (childNode$iterator = childNodes.iterator(); childNode$iterator.hasNext_0();) {
      childNode = castTo(childNode$iterator.next_1(), 8);
      $put_2(parentForChildNode.hashCodeMap, childNode, parentNode);
    }
    $put_2(childrenForParentNode.hashCodeMap, parentNode, childNodes);
  }
  roots = new ArrayList;
  for (node$iterator0 = new ArrayList$1(this$static._nodes); node$iterator0.i < node$iterator0.this$01.array.length;) {
    node = castTo($next_1(node$iterator0), 8);
    getEntryValueOrNull($getEntry(parentForChildNode.hashCodeMap, node)) == null && (push_1(roots.array, node) , true);
  }
  $sort_0(roots, (checkCriticalNotNull(new TreeLayoutOperations$lambda$2$Type) , new Comparator$lambda$2$Type));
  columns = new ArrayList;
  bottomY = -this$static._gapY;
  for (root$iterator = new ArrayList$1(roots); root$iterator.i < root$iterator.this$01.array.length;) {
    root = castTo($next_1(root$iterator), 8);
    bottomY = $layoutTree(this$static, columns, childrenForParentNode, 0, root, bottomY);
  }
  minX = 0;
  for (column$iterator = new ArrayList$1(columns); column$iterator.i < column$iterator.this$01.array.length;) {
    column = castTo($next_1(column$iterator), 13);
    maxWidth = 0;
    for (node$iterator = column.iterator(); node$iterator.hasNext_0();) {
      node = castTo(node$iterator.next_1(), 8);
      node.setX(minX);
      maxWidth = $wnd.Math.max(maxWidth, node.getWidth());
    }
    minX += maxWidth;
    minX += this$static._gapX;
  }
  $setWidth_7(this$static, minX - (columns.array.length == 0?0:this$static._gapX));
  this$static._listener.beforeSet(this$static, 'height', bottomY);
  this$static._height = bottomY;
}

function $distributeSize_5(this$static, context, offsetX, offsetY, width_0, height){
  var barX, child, child$iterator, connection, connection$iterator, fromX, toX;
  $distributeSize_1(this$static, context, offsetX, offsetY, width_0, height);
  for (connection$iterator = new ArrayList$1(this$static._connections); connection$iterator.i < connection$iterator.this$01.array.length;) {
    connection = castTo($next_1(connection$iterator), 81);
    fromX = connection._parent._anchor.getRightX();
    toX = $intern_28;
    for (child$iterator = new ArrayList$1(connection._children); child$iterator.i < child$iterator.this$01.array.length;) {
      child = castTo($next_1(child$iterator), 74);
      toX = $wnd.Math.min(toX, child._anchor.getX());
    }
    barX = this$static._compact?toX - this$static._gapX / 2:(fromX + toX) / 2;
    connection._listener.beforeSet(connection, 'barPosition', barX);
    connection._barPosition = barX;
  }
}

function $drawContents_0(this$static, out){
  var connection, connection$iterator;
  $drawContents(this$static, out);
  for (connection$iterator = new ArrayList$1(this$static._connections); connection$iterator.i < connection$iterator.this$01.array.length;) {
    connection = castTo($next_1(connection$iterator), 81);
    out.write_0(connection);
  }
}

function $enterAnchor(nodes, nodeForAnchor, connector){
  var ancestor, anchor;
  anchor = connector._anchor;
  ancestor = anchor;
  while (ancestor) {
    if (nodes.map_0.containsKey(ancestor)) {
      $put_2(nodeForAnchor.hashCodeMap, anchor, ancestor);
      break;
    }
  }
}

function $layoutTree(this$static, columns, children, level, node, parentBottomY){
  var bottom, bottomY, centerY, child, child$iterator, child$iterator0, childBottomY, column, down, first, firstY, l, last, lastY, minY, nextLevel, shiftY;
  while (columns.size() <= level) {
    columns.add(new ArrayList);
  }
  column = castTo(columns.getAtIndex(level), 13);
  if (column.size() > 0) {
    last = castTo(column.getAtIndex(column.size() - 1), 8);
    minY = last.getBottomY() + this$static._gapY;
  }
   else {
    minY = 0;
  }
  this$static._compact || (minY = $wnd.Math.max(parentBottomY, minY));
  column.add(node);
  nextLevel = castTo(children.getOrDefault(node, ($clinit_Collections() , $clinit_Collections() , EMPTY_LIST)), 13);
  if (nextLevel.isEmpty()) {
    if (!this$static._compact) {
      for (l = level + 1; l < columns.size(); l++) {
        down = castTo(columns.getAtIndex(l), 13);
        if (down.size() > 0) {
          bottom = castTo(down.getAtIndex(down.size() - 1), 8);
          minY = $wnd.Math.max(minY, bottom.getBottomY() + this$static._gapY);
        }
      }
    }
    node.setY(minY);
    bottomY = minY + this$static._height;
  }
   else {
    childBottomY = minY;
    for (child$iterator0 = nextLevel.iterator(); child$iterator0.hasNext_0();) {
      child = castTo(child$iterator0.next_1(), 8);
      childBottomY = $layoutTree(this$static, columns, children, level + 1, child, childBottomY);
    }
    first = castTo(nextLevel.getAtIndex(0), 8);
    last = castTo(nextLevel.getAtIndex(nextLevel.size() - 1), 8);
    firstY = first.getY();
    lastY = last.getBottomY();
    centerY = (firstY + lastY - node.getHeight()) / 2;
    if (minY <= centerY) {
      minY = centerY;
    }
     else {
      shiftY = minY - centerY;
      for (child$iterator = nextLevel.iterator(); child$iterator.hasNext_0();) {
        child = castTo(child$iterator.next_1(), 8);
        $shiftY(this$static, children, child, shiftY);
      }
      childBottomY += shiftY;
    }
    node.setY(minY);
    bottomY = $wnd.Math.max(minY + this$static._height, childBottomY);
  }
  return bottomY;
}

function $shiftY(this$static, children, node, shiftY){
  var child, child$iterator;
  node.setY(node.getY() + shiftY);
  for (child$iterator = castTo(children.getOrDefault(node, ($clinit_Collections() , $clinit_Collections() , EMPTY_LIST)), 13).iterator(); child$iterator.hasNext_0();) {
    child = castTo(child$iterator.next_1(), 8);
    $shiftY(this$static, children, child, shiftY);
  }
}

function $computeIntrinsicSize_3(this$static, context, offsetX, offsetY){
  var height, maxWidth, n, row, x_0, y_0;
  height = 0;
  maxWidth = 0;
  this$static._listener.beforeSet(this$static, 'x', offsetX);
  this$static._x = offsetX;
  this$static._listener.beforeSet(this$static, 'y', offsetY);
  this$static._y = offsetY;
  x_0 = offsetX;
  y_0 = offsetY;
  for (n = 0; n < this$static._contents.array.length; n++) {
    row = castTo($get_2(this$static._contents, n), 8);
    n > 0 && (height += this$static._gap);
    y_0 = offsetY + height;
    row.computeIntrinsicSize(context, x_0, y_0);
    height += row.getHeight();
    maxWidth = $wnd.Math.max(maxWidth, row.getWidth());
  }
  this$static._listener.beforeSet(this$static, 'width', maxWidth);
  this$static._width = maxWidth;
  this$static._listener.beforeSet(this$static, 'height', height);
  this$static._height = height;
}

function $distributeSize_4(this$static, context, offsetX, offsetY, width_0, height){
  var additionalHeight, additionalSpace, cnt, elementHeight, elementY, gap, n, row;
  this$static._listener.beforeSet(this$static, 'x', offsetX);
  this$static._x = offsetX;
  this$static._listener.beforeSet(this$static, 'y', offsetY);
  this$static._y = offsetY;
  cnt = this$static._contents.array.length;
  additionalSpace = height - this$static._height;
  additionalHeight = this$static._fill == ($clinit_SpaceDistribution() , STRETCH_CONTENT) && cnt > 0?additionalSpace / cnt:0;
  gap = this$static._gap + (this$static._fill == STRETCH_GAP && cnt > 1?additionalSpace / (cnt - 1):0);
  elementY = offsetY;
  for (n = 0; n < cnt; n++) {
    row = castTo($get_2(this$static._contents, n), 8);
    elementHeight = row.getHeight() + additionalHeight;
    row.distributeSize(context, offsetX, elementY, width_0, elementHeight);
    elementY += elementHeight;
    elementY += gap;
  }
  this$static._listener.beforeSet(this$static, 'width', width_0);
  this$static._width = width_0;
  this$static._listener.beforeSet(this$static, 'height', height);
  this$static._height = height;
}

function $draw_6(this$static, out){
  var e, e$iterator;
  for (e$iterator = new ArrayList$1(this$static._contents); e$iterator.i < e$iterator.this$01.array.length;) {
    e = castTo($next_1(e$iterator), 8);
    out.write_0(e);
  }
}

function $initId(this$static, id_0){
  if (id_0 <= 0) {
    throw toJs(new IllegalArgumentException('Invalid ID: ' + id_0));
  }
  this$static._id = id_0;
}

function $readFields(this$static, scope_0, in_0){
  var field;
  while ($hasNext_0(in_0)) {
    field = $nextName_0(in_0);
    this$static.readField(scope_0, in_0, field);
  }
}

function $writeData(this$static, scope_0, out, id_0){
  $writeDeferredName_0(out);
  $open_0(out, 1, '[');
  $value_4(out, this$static.jsonType());
  $value_3(out, id_0);
  $writeDeferredName_0(out);
  $open_0(out, 3, '{');
  this$static.writeFields(scope_0, out);
  $close_1(out, 3, 5, '}');
  $close_1(out, 1, 2, ']');
}

function $writeTo_6(this$static, scope_0, out){
  scope_0.writeRefOrData(out, this$static);
}

defineClass(28, 1, {28:1, 30:1});
_.get_1 = function get_3(field){
  return null;
}
;
_.properties_0 = function properties_1(){
  return $clinit_Collections() , $clinit_Collections() , EMPTY_LIST;
}
;
_.set_0 = function set_1(field, value_0){
}
;
_.transientProperties = function transientProperties(){
  return $clinit_Collections() , $clinit_Collections() , EMPTY_SET;
}
;
_.readField = function readField(scope_0, in_0, field){
  $skipValue(in_0);
}
;
_.toString_0 = function toString_17(){
  var ex, out;
  out = new StringW;
  try {
    $writeTo_6(this, new DummyScope, new JsonWriter_0(out));
  }
   catch ($e0) {
    $e0 = toJava($e0);
    if (instanceOf($e0, 32)) {
      ex = $e0;
      throw toJs(new RuntimeException_2(ex));
    }
     else 
      throw toJs($e0);
  }
  return out._buffer.string;
}
;
_.writeElement = function writeElement(scope_0, out, field, element){
  $nullValue_0(out);
}
;
_.writeFieldValue = function writeFieldValue(scope_0, out, field){
  $nullValue_0(out);
}
;
_.writeFields = function writeFields(scope_0, out){
}
;
_.writeTo_0 = function writeTo_3(scope_0, out){
  $writeTo_6(this, scope_0, out);
}
;
_._id = 0;
var Lde_haumacher_msgbuf_graph_AbstractSharedGraphNode_2_classLit = createForClass('de.haumacher.msgbuf.graph', 'AbstractSharedGraphNode', 28);
function $clinit_Widget_Impl(){
  $clinit_Widget_Impl = emptyMethod;
  PROPERTIES = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['cssClass', 'userObject', 'clientId'])));
  TRANSIENT_PROPERTIES = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['userObject', 'clientId'])))));
}

function $get(this$static, field){
  switch (field) {
    case 'cssClass':
      return this$static._cssClass;
    case 'userObject':
      return this$static._userObject;
    case 'clientId':
      return this$static._clientId;
    default:return null;
  }
}

function $internalSetClientId(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'clientId', value_0);
  this$static._clientId = value_0;
}

function $internalSetCssClass(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'cssClass', value_0);
  this$static._cssClass = value_0;
}

function $readField(this$static, in_0, field){
  switch (field) {
    case 'cssClass':
      this$static.setCssClass(nextStringOptional(in_0));
      break;
    default:$skipValue(in_0);
  }
}

function $set(this$static, field, value_0){
  switch (field) {
    case 'cssClass':
      $internalSetCssClass(this$static, castToString(value_0));
      break;
    case 'userObject':
      this$static._listener.beforeSet(this$static, 'userObject', value_0);
      this$static._userObject = value_0;
      break;
    case 'clientId':
      $internalSetClientId(this$static, castToString(value_0));
  }
}

function $writeFieldValue(this$static, out, field){
  switch (field) {
    case 'cssClass':
      {
        this$static._cssClass != null?$value_4(out, this$static._cssClass):$nullValue_0(out);
        break;
      }

    case 'userObject':
      {
        if (this$static._userObject != null)
        ;
        else {
          $nullValue_0(out);
        }
        break;
      }

    case 'clientId':
      {
        this$static._clientId != null?$value_4(out, this$static._clientId):$nullValue_0(out);
        break;
      }

    default:$nullValue_0(out);
  }
}

function $writeFields(this$static, out){
  if (this$static._cssClass != null) {
    $name_1(out, 'cssClass');
    $value_4(out, this$static._cssClass);
  }
}

function Widget_Impl(){
  $clinit_Widget_Impl();
  this._listener = ($clinit_Listener() , NONE_4);
}

defineClass(151, 28, {27:1, 36:1, 28:1, 30:1, 21:1});
_.clear = function clear_1(){
  $clear_0(this);
}
;
_.containsKey = function containsKey(key){
  return $contains_2(this.self_3().properties_0(), key);
}
;
_.containsValue = function containsValue(value_0){
  return $findFirst($filter($map_0(new StreamImpl(null, new Spliterators$IteratorSpliterator(this.self_3().properties_0(), 16)), new MapLike$lambda$0$Type(this)), new MapLike$lambda$1$Type(value_0))).ref != null;
}
;
_.entrySet_0 = function entrySet(){
  return castTo($collect(new StreamImpl(null, new Spliterators$IteratorSpliterator(this.self_3().properties_0(), 16)), toMap(new MapLike$lambda$4$Type(this), new Collectors$lambda$44$Type, new Collectors$22methodref$ctor$Type)), 21).entrySet_0();
}
;
_.get = function get_4(key){
  return this.self_3().get_1(toString_8(key));
}
;
_.getOrDefault = function getOrDefault(key, defaultValue){
  var currentValue;
  return currentValue = this.self_3().get_1(toString_8(key)) , currentValue == null && !$contains_2(this.self_3().properties_0(), key)?defaultValue:currentValue;
}
;
_.isEmpty = function isEmpty_0(){
  return this.self_3().properties_0().list.isEmpty();
}
;
_.keySet = function keySet(){
  return new HashSet_1(this.self_3().properties_0());
}
;
_.merge = function merge(key, value_0, remappingFunction){
  return $merge(this, key, value_0, remappingFunction);
}
;
_.put = function put(arg0, arg1){
  return $put(this, castToString(arg0), arg1);
}
;
_.put_0 = function put_0(key, value_0){
  return $put(this, key, value_0);
}
;
_.putAll = function putAll(m){
  $putAll(this, m);
}
;
_.putIfAbsent = function putIfAbsent(key, value_0){
  var currentValue;
  return currentValue = this.self_3().get_1(toString_8(key)) , currentValue != null?currentValue:$put(this, castToString(key), value_0);
}
;
_.remove = function remove_3(key){
  var before;
  return before = this.self_3().get_1(toString_8(key)) , this.self_3().set_0(toString_8(key), null) , before;
}
;
_.replace = function replace(key, value_0){
  return $contains_2(this.self_3().properties_0(), key)?$put(this, castToString(key), value_0):null;
}
;
_.self_2 = function self_0(){
  return this;
}
;
_.self_3 = function self_1(){
  return this.self_2();
}
;
_.size = function size_2(){
  return this.self_3().properties_0().coll.size();
}
;
_.values = function values_10(){
  return castTo($collect($map_0(new StreamImpl(null, new Spliterators$IteratorSpliterator(this.self_3().properties_0(), 16)), new MapLike$lambda$2$Type(this)), of_1(new Collectors$21methodref$ctor$Type, new Collectors$20methodref$add$Type, new Collectors$lambda$42$Type, stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , IDENTITY_FINISH)]))), 20);
}
;
_.get_1 = function get_5(field){
  return $get(this, field);
}
;
_.getClientId = function getClientId(){
  return this._clientId;
}
;
_.properties_0 = function properties_2(){
  return PROPERTIES;
}
;
_.readField = function readField_0(scope_0, in_0, field){
  $readField(this, in_0, field);
}
;
_.set_0 = function set_2(field, value_0){
  $set(this, field, value_0);
}
;
_.setCssClass = function setCssClass(value_0){
  this._listener.beforeSet(this, 'cssClass', value_0);
  this._cssClass = value_0;
  return this;
}
;
_.transientProperties = function transientProperties_0(){
  return TRANSIENT_PROPERTIES;
}
;
_.writeFieldValue = function writeFieldValue_0(scope_0, out, field){
  $writeFieldValue(this, out, field);
}
;
_.writeFields = function writeFields_0(scope_0, out){
  $writeFields(this, out);
}
;
_._clientId = null;
_._cssClass = null;
_._userObject = null;
var PROPERTIES, TRANSIENT_PROPERTIES;
var Lcom_top_1logic_graphic_flow_data_impl_Widget_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Widget_Impl', 151);
function $clinit_Box_Impl(){
  $clinit_Box_Impl = emptyMethod;
  $clinit_Widget_Impl();
  PROPERTIES_0 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['parent', 'x', 'y', 'width', 'height'])));
  TRANSIENT_PROPERTIES_0 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $get_0(this$static, field){
  switch (field) {
    case 'parent':
      return this$static._parent;
    case 'x':
      return this$static._x;
    case 'y':
      return this$static._y;
    case 'width':
      return this$static._width;
    case 'height':
      return this$static._height;
    default:return $get(this$static, field);
  }
}

function $internalSetHeight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'height', value_0);
  this$static._height = value_0;
}

function $internalSetParent(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'parent', value_0);
  if (!!value_0 && !!this$static._parent) {
    throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
  }
  this$static._parent = value_0;
}

function $internalSetWidth(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'width', value_0);
  this$static._width = value_0;
}

function $internalSetX(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'x', value_0);
  this$static._x = value_0;
}

function $internalSetY(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'y', value_0);
  this$static._y = value_0;
}

function $readField_0(this$static, scope_0, in_0, field){
  switch (field) {
    case 'x':
      this$static.setX($nextDouble_0(in_0));
      break;
    case 'y':
      this$static.setY($nextDouble_0(in_0));
      break;
    case 'width':
      this$static.setWidth_1($nextDouble_0(in_0));
      break;
    case 'height':
      this$static.setHeight_1($nextDouble_0(in_0));
      break;
    default:$readField(this$static, in_0, field);
  }
}

function $set_0(this$static, field, value_0){
  switch (field) {
    case 'x':
      $internalSetX(this$static, $doubleValue(castToDouble(value_0)));
      break;
    case 'y':
      $internalSetY(this$static, $doubleValue(castToDouble(value_0)));
      break;
    case 'width':
      $internalSetWidth(this$static, $doubleValue(castToDouble(value_0)));
      break;
    case 'height':
      $internalSetHeight(this$static, $doubleValue(castToDouble(value_0)));
      break;
    default:$set(this$static, field, value_0);
  }
}

function $writeFieldValue_0(this$static, scope_0, out, field){
  switch (field) {
    case 'parent':
      {
        this$static._parent?$writeTo_6(this$static._parent, scope_0, out):$nullValue_0(out);
        break;
      }

    case 'x':
      {
        $value_2(out, this$static._x);
        break;
      }

    case 'y':
      {
        $value_2(out, this$static._y);
        break;
      }

    case 'width':
      {
        $value_2(out, this$static._width);
        break;
      }

    case 'height':
      {
        $value_2(out, this$static._height);
        break;
      }

    default:$writeFieldValue(this$static, out, field);
  }
}

function $writeFields_0(this$static, scope_0, out){
  $writeFields(this$static, out);
  $name_1(out, 'x');
  $value_2(out, this$static._x);
  $name_1(out, 'y');
  $value_2(out, this$static._y);
  $name_1(out, 'width');
  $value_2(out, this$static._width);
  $name_1(out, 'height');
  $value_2(out, this$static._height);
}

function Box_Impl(){
  $clinit_Box_Impl();
  Widget_Impl.call(this);
}

defineClass(19, 151, $intern_29);
_.getBottomY = function getBottomY(){
  return this.self_0()._y + this.self_0()._height;
}
;
_.getRightX = function getRightX(){
  return this.self_0()._x + this.self_0()._width;
}
;
_.self_0 = function self_2(){
  return this;
}
;
_.self_2 = function self_3(){
  return this.self_0();
}
;
_.self_3 = function self_4(){
  return this.self_2();
}
;
_.setCssClass = function setCssClass_1(value_0){
  return this.setCssClass_0(value_0);
}
;
_.get_1 = function get_6(field){
  return $get_0(this, field);
}
;
_.getHeight = function getHeight_11(){
  return this._height;
}
;
_.getParent = function getParent(){
  return this._parent;
}
;
_.getWidth = function getWidth_3(){
  return this._width;
}
;
_.getX = function getX_2(){
  return this._x;
}
;
_.getY = function getY_2(){
  return this._y;
}
;
_.properties_0 = function properties_3(){
  return PROPERTIES_0;
}
;
_.readField = function readField_1(scope_0, in_0, field){
  $readField_0(this, scope_0, in_0, field);
}
;
_.set_0 = function set_3(field, value_0){
  $set_0(this, field, value_0);
}
;
_.setCssClass_0 = function setCssClass_0(value_0){
  this._listener.beforeSet(this, 'cssClass', value_0);
  this._cssClass = value_0;
  return this;
}
;
_.setHeight_1 = function setHeight(value_0){
  this._listener.beforeSet(this, 'height', value_0);
  this._height = value_0;
  return this;
}
;
_.setWidth_1 = function setWidth(value_0){
  this._listener.beforeSet(this, 'width', value_0);
  this._width = value_0;
  return this;
}
;
_.setX = function setX(value_0){
  this._listener.beforeSet(this, 'x', value_0);
  this._x = value_0;
  return this;
}
;
_.setY = function setY(value_0){
  this._listener.beforeSet(this, 'y', value_0);
  this._y = value_0;
  return this;
}
;
_.transientProperties = function transientProperties_1(){
  return TRANSIENT_PROPERTIES_0;
}
;
_.writeFieldValue = function writeFieldValue_1(scope_0, out, field){
  $writeFieldValue_0(this, scope_0, out, field);
}
;
_.writeFields = function writeFields_1(scope_0, out){
  $writeFields_0(this, scope_0, out);
}
;
_._height = 0;
_._parent = null;
_._width = 0;
_._x = 0;
_._y = 0;
var PROPERTIES_0, TRANSIENT_PROPERTIES_0;
var Lcom_top_1logic_graphic_flow_data_impl_Box_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Box_Impl', 19);
function $clinit_Decoration_Impl(){
  $clinit_Decoration_Impl = emptyMethod;
  $clinit_Box_Impl();
  PROPERTIES_1 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['content'])));
  TRANSIENT_PROPERTIES_1 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $get_1(this$static, field){
  switch (field) {
    case 'content':
      return this$static._content;
    default:return $get_0(this$static, field);
  }
}

function $internalSetContent(this$static, value_0){
  var after, before, oldContainer;
  before = castTo(this$static._content, 19);
  after = castTo(value_0, 19);
  if (after) {
    oldContainer = after._parent;
    if (!!oldContainer && oldContainer != this$static) {
      throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
    }
  }
  this$static._listener.beforeSet(this$static, 'content', value_0);
  !!before && $internalSetParent(before, null);
  this$static._content = value_0;
  !!after && $internalSetParent(after, this$static);
}

function $readField_1(this$static, scope_0, in_0, field){
  switch (field) {
    case 'content':
      this$static.setContent(readBox(scope_0, in_0));
      break;
    default:$readField_0(this$static, scope_0, in_0, field);
  }
}

function $set_1(this$static, field, value_0){
  switch (field) {
    case 'content':
      $internalSetContent(this$static, castTo(value_0, 8));
      break;
    default:$set_0(this$static, field, value_0);
  }
}

function $writeFieldValue_1(this$static, scope_0, out, field){
  switch (field) {
    case 'content':
      {
        this$static._content?this$static._content.writeTo_0(scope_0, out):$nullValue_0(out);
        break;
      }

    default:$writeFieldValue_0(this$static, scope_0, out, field);
  }
}

function $writeFields_1(this$static, scope_0, out){
  $writeFields_0(this$static, scope_0, out);
  if (this$static._content) {
    $name_1(out, 'content');
    this$static._content.writeTo_0(scope_0, out);
  }
}

function Decoration_Impl(){
  $clinit_Decoration_Impl();
  Box_Impl.call(this);
}

defineClass(136, 19, $intern_29);
_.computeIntrinsicSize = function computeIntrinsicSize(context, offsetX, offsetY){
  $computeIntrinsicSize(this, context, offsetX, offsetY);
}
;
_.draw = function draw_11(out){
  out.write_0(this.self_1()._content);
}
;
_.drawContent = function drawContent(out){
  out.write_0(this.self_1()._content);
}
;
_.self_0 = function self_5(){
  return this.self_1();
}
;
_.self_1 = function self_6(){
  return this;
}
;
_.self_2 = function self_7(){
  return this.self_1();
}
;
_.self_3 = function self_8(){
  return this.self_2();
}
;
_.setClientId_0 = function setClientId_0(value_0){
  return this.setClientId(value_0);
}
;
_.setCssClass_0 = function setCssClass_2(value_0){
  return this.setCssClass_1(value_0);
}
;
_.setCssClass = function setCssClass_4(value_0){
  return this.setCssClass_1(value_0);
}
;
_.setHeight_1 = function setHeight_0(value_0){
  return this.setHeight(value_0);
}
;
_.setWidth_1 = function setWidth_0(value_0){
  return this.setWidth(value_0);
}
;
_.setX = function setX_0(value_0){
  return this.setX_0(value_0);
}
;
_.setY = function setY_0(value_0){
  return this.setY_0(value_0);
}
;
_.get_1 = function get_7(field){
  return $get_1(this, field);
}
;
_.getContent = function getContent(){
  return this._content;
}
;
_.properties_0 = function properties_4(){
  return PROPERTIES_1;
}
;
_.readField = function readField_2(scope_0, in_0, field){
  $readField_1(this, scope_0, in_0, field);
}
;
_.set_0 = function set_4(field, value_0){
  $set_1(this, field, value_0);
}
;
_.setClientId = function setClientId(value_0){
  this._listener.beforeSet(this, 'clientId', value_0);
  this._clientId = value_0;
  return this;
}
;
_.setContent = function setContent(value_0){
  $internalSetContent(this, value_0);
  return this;
}
;
_.setCssClass_1 = function setCssClass_3(value_0){
  this._listener.beforeSet(this, 'cssClass', value_0);
  this._cssClass = value_0;
  return this;
}
;
_.setHeight = function setHeight_1(value_0){
  this._listener.beforeSet(this, 'height', value_0);
  this._height = value_0;
  return this;
}
;
_.setWidth = function setWidth_1(value_0){
  this._listener.beforeSet(this, 'width', value_0);
  this._width = value_0;
  return this;
}
;
_.setX_0 = function setX_1(value_0){
  this._listener.beforeSet(this, 'x', value_0);
  this._x = value_0;
  return this;
}
;
_.setY_0 = function setY_1(value_0){
  this._listener.beforeSet(this, 'y', value_0);
  this._y = value_0;
  return this;
}
;
_.transientProperties = function transientProperties_2(){
  return TRANSIENT_PROPERTIES_1;
}
;
_.writeFieldValue = function writeFieldValue_2(scope_0, out, field){
  $writeFieldValue_1(this, scope_0, out, field);
}
;
_.writeFields = function writeFields_2(scope_0, out){
  $writeFields_1(this, scope_0, out);
}
;
_._content = null;
var PROPERTIES_1, TRANSIENT_PROPERTIES_1;
var Lcom_top_1logic_graphic_flow_data_impl_Decoration_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Decoration_Impl', 136);
function $clinit_Align_Impl(){
  $clinit_Align_Impl = emptyMethod;
  $clinit_Decoration_Impl();
  PROPERTIES_2 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['xAlign', 'yAlign'])));
  TRANSIENT_PROPERTIES_2 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetXAlign(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'xAlign' cannot be null."));
  this$static._listener.beforeSet(this$static, 'xAlign', value_0);
  this$static._xAlign = value_0;
}

function $internalSetYAlign(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'yAlign' cannot be null."));
  this$static._listener.beforeSet(this$static, 'yAlign', value_0);
  this$static._yAlign = value_0;
}

function $setHeight_1(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'height', value_0);
  this$static._height = value_0;
  return this$static;
}

function $setWidth_1(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'width', value_0);
  this$static._width = value_0;
  return this$static;
}

function $setXAlign(this$static, value_0){
  $internalSetXAlign(this$static, value_0);
  return this$static;
}

function $setYAlign(this$static, value_0){
  $internalSetYAlign(this$static, value_0);
  return this$static;
}

function Align_Impl(){
  $clinit_Align_Impl();
  Decoration_Impl.call(this);
  this._xAlign = ($clinit_Alignment() , START);
  this._yAlign = START;
}

defineClass(429, 136, $intern_29, Align_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_0(context, offsetX, offsetY){
  var content_0;
  this._listener.beforeSet(this, 'x', offsetX);
  this._x = offsetX;
  this._listener.beforeSet(this, 'y', offsetY);
  this._y = offsetY;
  $computeIntrinsicSize(this, context, offsetX, offsetY);
  content_0 = this._content;
  $setWidth_1(this, content_0.self_0().getWidth());
  $setHeight_1(this, content_0.self_0().getHeight());
}
;
_.distributeSize = function distributeSize(context, offsetX, offsetY, width_0, height){
  $distributeSize(this, context, offsetX, offsetY, width_0, height);
}
;
_.self_0 = function self_9(){
  return this;
}
;
_.self_1 = function self_10(){
  return this;
}
;
_.self_2 = function self_11(){
  return this;
}
;
_.self_3 = function self_12(){
  return this;
}
;
_.setClientId = function setClientId_1(value_0){
  return this._listener.beforeSet(this, 'clientId', value_0) , this._clientId = value_0 , this;
}
;
_.setClientId_0 = function setClientId_2(value_0){
  return this._listener.beforeSet(this, 'clientId', value_0) , this._clientId = value_0 , this;
}
;
_.setContent = function setContent_0(value_0){
  return $internalSetContent(this, value_0) , this;
}
;
_.setCssClass_0 = function setCssClass_5(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_1 = function setCssClass_6(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_7(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_2(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight = function setHeight_3(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_2(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth = function setWidth_3(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_2(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_0 = function setX_3(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_2(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_0 = function setY_3(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_8(field){
  switch (field) {
    case 'xAlign':
      return this._xAlign;
    case 'yAlign':
      return this._yAlign;
    default:return $get_1(this, field);
  }
}
;
_.jsonType = function jsonType(){
  return 'Align';
}
;
_.properties_0 = function properties_5(){
  return PROPERTIES_2;
}
;
_.readField = function readField_3(scope_0, in_0, field){
  switch (field) {
    case 'xAlign':
      $setXAlign(this, ($clinit_Alignment() , valueOfProtocol($nextString_0(in_0))));
      break;
    case 'yAlign':
      $setYAlign(this, ($clinit_Alignment() , valueOfProtocol($nextString_0(in_0))));
      break;
    default:$readField_1(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_5(field, value_0){
  switch (field) {
    case 'xAlign':
      $internalSetXAlign(this, castTo(value_0, 90));
      break;
    case 'yAlign':
      $internalSetYAlign(this, castTo(value_0, 90));
      break;
    default:$set_1(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_3(){
  return TRANSIENT_PROPERTIES_2;
}
;
_.writeFieldValue = function writeFieldValue_3(scope_0, out, field){
  switch (field) {
    case 'xAlign':
      {
        $writeTo_1(this._xAlign, out);
        break;
      }

    case 'yAlign':
      {
        $writeTo_1(this._yAlign, out);
        break;
      }

    default:$writeFieldValue_1(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_3(scope_0, out){
  $writeFields_1(this, scope_0, out);
  $name_1(out, 'xAlign');
  $writeTo_1(this._xAlign, out);
  $name_1(out, 'yAlign');
  $writeTo_1(this._yAlign, out);
}
;
var PROPERTIES_2, TRANSIENT_PROPERTIES_2;
var Lcom_top_1logic_graphic_flow_data_impl_Align_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Align_Impl', 429);
function $clinit_Border_Impl(){
  $clinit_Border_Impl = emptyMethod;
  $clinit_Decoration_Impl();
  PROPERTIES_3 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['strokeStyle', 'thickness', 'top', 'left', 'bottom', 'right', 'dashes'])));
  TRANSIENT_PROPERTIES_3 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetBottom(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'bottom', ($clinit_Boolean() , value_0?true:false));
  this$static._bottom = value_0;
}

function $internalSetDashes(this$static, value_0){
  $clear(this$static._dashes);
  $addAll_2(this$static._dashes, value_0);
}

function $internalSetLeft(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'left', ($clinit_Boolean() , value_0?true:false));
  this$static._left = value_0;
}

function $internalSetRight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'right', ($clinit_Boolean() , value_0?true:false));
  this$static._right = value_0;
}

function $internalSetStrokeStyle(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'strokeStyle', value_0);
  this$static._strokeStyle = value_0;
}

function $internalSetThickness(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'thickness', value_0);
  this$static._thickness = value_0;
}

function $internalSetTop(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'top', ($clinit_Boolean() , value_0?true:false));
  this$static._top = value_0;
}

function $setBottom(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'bottom', ($clinit_Boolean() , value_0?true:false));
  this$static._bottom = value_0;
  return this$static;
}

function $setLeft(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'left', ($clinit_Boolean() , value_0?true:false));
  this$static._left = value_0;
  return this$static;
}

function $setRight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'right', ($clinit_Boolean() , value_0?true:false));
  this$static._right = value_0;
  return this$static;
}

function $setStrokeStyle(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'strokeStyle', value_0);
  this$static._strokeStyle = value_0;
  return this$static;
}

function $setThickness(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'thickness', value_0);
  this$static._thickness = value_0;
  return this$static;
}

function $setTop(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'top', ($clinit_Boolean() , value_0?true:false));
  this$static._top = value_0;
  return this$static;
}

function Border_Impl(){
  $clinit_Border_Impl();
  Decoration_Impl.call(this);
  this._dashes = new Border_Impl$1(this);
}

defineClass(430, 136, $intern_29, Border_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_1(context, offsetX, offsetY){
  var width_0, height;
  $computeIntrinsicSize(this, context, offsetX, offsetY);
  width_0 = this._content.self_0().getWidth();
  height = this._content.self_0().getHeight();
  this._top && (height += this._thickness);
  this._left && (width_0 += this._thickness);
  this._bottom && (height += this._thickness);
  this._right && (width_0 += this._thickness);
  this._listener.beforeSet(this, 'width', width_0);
  this._width = width_0;
  this._listener.beforeSet(this, 'height', height);
  this._height = height;
}
;
_.distributeSize = function distributeSize_0(context, offsetX, offsetY, width_0, height){
  $distributeSize_0(this, context, offsetX, offsetY, width_0, height);
}
;
_.draw = function draw_12(out){
  var radius;
  out.write_0(this._content);
  radius = this._thickness / 2;
  out.beginPath_0(this);
  out.writeCssClass(this._cssClass);
  out.setStrokeWidth(this._thickness);
  out.setStroke(this._strokeStyle);
  this._dashes.array.length == 0 || out.setStrokeDasharray(doubleArray(this._dashes));
  out.setFill('none');
  out.beginData();
  out.moveToAbs(this._x + (this._left?radius:0), this._y + radius);
  this._top?out.lineToAbs(this._x + this._width - (this._right?radius:0), this._y + radius):out.moveToAbs(this._x + this._width - radius, this._y);
  this._right?out.lineToAbs(this._x + this._width - radius, this._y + this._height - (this._bottom?radius:0)):out.moveToAbs(this._x + this._width, this._y + this._height - radius);
  this._bottom?out.lineToAbs(this._x + (this._left?radius:0), this._y + this._height - radius):out.moveToAbs(this._x + radius, this._y + this._height);
  this._left && (this._top && this._right && this._bottom?out.closePath():out.lineToAbs(this._x + radius, this._y));
  out.endData();
  out.endPath();
}
;
_.self_0 = function self_13(){
  return this;
}
;
_.self_1 = function self_14(){
  return this;
}
;
_.self_2 = function self_15(){
  return this;
}
;
_.self_3 = function self_16(){
  return this;
}
;
_.setClientId = function setClientId_3(value_0){
  return this._listener.beforeSet(this, 'clientId', value_0) , this._clientId = value_0 , this;
}
;
_.setClientId_0 = function setClientId_4(value_0){
  return this._listener.beforeSet(this, 'clientId', value_0) , this._clientId = value_0 , this;
}
;
_.setContent = function setContent_1(value_0){
  return $internalSetContent(this, value_0) , this;
}
;
_.setCssClass_0 = function setCssClass_8(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_1 = function setCssClass_9(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_10(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_4(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight = function setHeight_5(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_4(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth = function setWidth_5(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_4(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_0 = function setX_5(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_4(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_0 = function setY_5(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_9(field){
  switch (field) {
    case 'strokeStyle':
      return this._strokeStyle;
    case 'thickness':
      return this._thickness;
    case 'top':
      return $clinit_Boolean() , this._top?true:false;
    case 'left':
      return $clinit_Boolean() , this._left?true:false;
    case 'bottom':
      return $clinit_Boolean() , this._bottom?true:false;
    case 'right':
      return $clinit_Boolean() , this._right?true:false;
    case 'dashes':
      return this._dashes;
    default:return $get_1(this, field);
  }
}
;
_.jsonType = function jsonType_0(){
  return 'Border';
}
;
_.properties_0 = function properties_6(){
  return PROPERTIES_3;
}
;
_.readField = function readField_4(scope_0, in_0, field){
  var newValue;
  switch (field) {
    case 'strokeStyle':
      $setStrokeStyle(this, nextStringOptional(in_0));
      break;
    case 'thickness':
      $setThickness(this, $nextDouble_0(in_0));
      break;
    case 'top':
      $setTop(this, $nextBoolean_0(in_0));
      break;
    case 'left':
      $setLeft(this, $nextBoolean_0(in_0));
      break;
    case 'bottom':
      $setBottom(this, $nextBoolean_0(in_0));
      break;
    case 'right':
      $setRight(this, $nextBoolean_0(in_0));
      break;
    case 'dashes':
      {
        newValue = new ArrayList;
        $beginArray_0(in_0);
        while ($hasNext_0(in_0)) {
          $add_0(newValue, $nextDouble_0(in_0));
        }
        $endArray_0(in_0);
        $clear(this._dashes);
        $addAll_2(this._dashes, newValue);
      }

      break;
    default:$readField_1(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_6(field, value_0){
  var result;
  switch (field) {
    case 'strokeStyle':
      $internalSetStrokeStyle(this, castToString(value_0));
      break;
    case 'thickness':
      $internalSetThickness(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'top':
      $internalSetTop(this, $booleanValue(castToBoolean(value_0)));
      break;
    case 'left':
      $internalSetLeft(this, $booleanValue(castToBoolean(value_0)));
      break;
    case 'bottom':
      $internalSetBottom(this, $booleanValue(castToBoolean(value_0)));
      break;
    case 'right':
      $internalSetRight(this, $booleanValue(castToBoolean(value_0)));
      break;
    case 'dashes':
      $internalSetDashes(this, (result = castTo(value_0, 13) , result));
      break;
    default:$set_1(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_4(){
  return TRANSIENT_PROPERTIES_3;
}
;
_.writeElement = function writeElement_0(scope_0, out, field, element){
  switch (field) {
    case 'dashes':
      {
        $value_2(out, $doubleValue(castToDouble(element)));
        break;
      }

    default:$nullValue_0(out);
  }
}
;
_.writeFieldValue = function writeFieldValue_4(scope_0, out, field){
  var x_0, x$iterator;
  switch (field) {
    case 'strokeStyle':
      {
        $value_4(out, this._strokeStyle);
        break;
      }

    case 'thickness':
      {
        $value_2(out, this._thickness);
        break;
      }

    case 'top':
      {
        $value_5(out, this._top);
        break;
      }

    case 'left':
      {
        $value_5(out, this._left);
        break;
      }

    case 'bottom':
      {
        $value_5(out, this._bottom);
        break;
      }

    case 'right':
      {
        $value_5(out, this._right);
        break;
      }

    case 'dashes':
      {
        $writeDeferredName_0(out);
        $open_0(out, 1, '[');
        for (x$iterator = new ArrayList$1(this._dashes); x$iterator.i < x$iterator.this$01.array.length;) {
          x_0 = $doubleValue(castToDouble($next_1(x$iterator)));
          $value_2(out, x_0);
        }
        $close_1(out, 1, 2, ']');
        break;
      }

    default:$writeFieldValue_1(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_4(scope_0, out){
  var x_0, x$iterator;
  $writeFields_1(this, scope_0, out);
  $name_1(out, 'strokeStyle');
  $value_4(out, this._strokeStyle);
  $name_1(out, 'thickness');
  $value_2(out, this._thickness);
  $name_1(out, 'top');
  $value_5(out, this._top);
  $name_1(out, 'left');
  $value_5(out, this._left);
  $name_1(out, 'bottom');
  $value_5(out, this._bottom);
  $name_1(out, 'right');
  $value_5(out, this._right);
  $name_1(out, 'dashes');
  $writeDeferredName_0(out);
  $open_0(out, 1, '[');
  for (x$iterator = new ArrayList$1(this._dashes); x$iterator.i < x$iterator.this$01.array.length;) {
    x_0 = $doubleValue(castToDouble($next_1(x$iterator)));
    $value_2(out, x_0);
  }
  $close_1(out, 1, 2, ']');
}
;
_._bottom = true;
_._left = true;
_._right = true;
_._strokeStyle = 'black';
_._thickness = 1;
_._top = true;
var PROPERTIES_3, TRANSIENT_PROPERTIES_3;
var Lcom_top_1logic_graphic_flow_data_impl_Border_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Border_Impl', 430);
function $indexOf(this$static, toFind){
  var i, n;
  for (i = 0 , n = this$static.size(); i < n; ++i) {
    if (equals_18(toFind, this$static.getAtIndex(i))) {
      return i;
    }
  }
  return -1;
}

defineClass(487, 485, $intern_30);
_.spliterator_0 = function spliterator_1(){
  return new Spliterators$IteratorSpliterator(this, 16);
}
;
_.addAtIndex = function add_1(index_0, element){
  throw toJs(new UnsupportedOperationException_0('Add not supported on this list'));
}
;
_.add = function add_2(obj){
  this.addAtIndex(this.size(), obj);
  return true;
}
;
_.addAllAtIndex = function addAll_0(index_0, c){
  var changed, e, e$iterator;
  checkCriticalNotNull(c);
  changed = false;
  for (e$iterator = c.iterator(); e$iterator.hasNext_0();) {
    e = e$iterator.next_1();
    this.addAtIndex(index_0++, e);
    changed = true;
  }
  return changed;
}
;
_.clear = function clear_2(){
  this.removeRange_0(0, this.size());
}
;
_.equals_0 = function equals_2(o){
  var elem, elem$iterator, elemOther, iterOther, other;
  if (o === this) {
    return true;
  }
  if (!instanceOf(o, 13)) {
    return false;
  }
  other = castTo(o, 13);
  if (this.size() != other.size()) {
    return false;
  }
  iterOther = other.iterator();
  for (elem$iterator = this.iterator(); elem$iterator.hasNext_0();) {
    elem = elem$iterator.next_1();
    elemOther = iterOther.next_1();
    if (!(maskUndefined(elem) === maskUndefined(elemOther) || elem != null && equals_Ljava_lang_Object__Z__devirtual$(elem, elemOther))) {
      return false;
    }
  }
  return true;
}
;
_.hashCode_0 = function hashCode_3(){
  return hashCode_13(this);
}
;
_.indexOf = function indexOf(toFind){
  return $indexOf(this, toFind);
}
;
_.iterator = function iterator_1(){
  return new AbstractList$IteratorImpl(this);
}
;
_.lastIndexOf = function lastIndexOf(toFind){
  var i;
  for (i = this.size() - 1; i > -1; --i) {
    if (equals_18(toFind, this.getAtIndex(i))) {
      return i;
    }
  }
  return -1;
}
;
_.listIterator = function listIterator(){
  return new AbstractList$ListIteratorImpl(this, 0);
}
;
_.listIterator_0 = function listIterator_0(from){
  return new AbstractList$ListIteratorImpl(this, from);
}
;
_.removeAtIndex = function remove_4(index_0){
  throw toJs(new UnsupportedOperationException_0('Remove not supported on this list'));
}
;
_.removeRange_0 = function removeRange(fromIndex, endIndex){
  var i, iter;
  iter = new AbstractList$ListIteratorImpl(this, fromIndex);
  for (i = fromIndex; i < endIndex; ++i) {
    checkCriticalElement(iter.i < iter.this$01.size());
    iter.this$01.getAtIndex(iter.last = iter.i++);
    $remove_2(iter);
  }
}
;
_.setAtIndex = function set_7(index_0, o){
  throw toJs(new UnsupportedOperationException_0('Set not supported on this list'));
}
;
_.subList = function subList(fromIndex, toIndex){
  return new AbstractList$SubList(this, fromIndex, toIndex);
}
;
var Ljava_util_AbstractList_2_classLit = createForClass('java.util', 'AbstractList', 487);
function $isInstance_2(instance){
  if (instance == null) {
    return false;
  }
  return instance.$implements__java_lang_Cloneable || Array.isArray(instance);
}

function $$init_0(this$static){
  this$static.array = initUnidimensionalArray(Ljava_lang_Object_2_classLit, $intern_0, 1, 0, 5, 1);
}

function $add_0(this$static, o){
  push_1(this$static.array, o);
  return true;
}

function $addAll_0(this$static, index_0, c){
  var cArray, len;
  checkCriticalPositionIndex(index_0, this$static.array.length);
  cArray = c.toArray();
  len = cArray.length;
  if (len == 0) {
    return false;
  }
  insertTo_0(this$static.array, index_0, cArray);
  return true;
}

function $addAll_1(this$static, c){
  var cArray, len;
  cArray = c.toArray();
  len = cArray.length;
  if (len == 0) {
    return false;
  }
  insertTo_0(this$static.array, this$static.array.length, cArray);
  return true;
}

function $forEach(this$static, consumer){
  var e, e$array, e$index, e$max;
  checkCriticalNotNull(consumer);
  for (e$array = this$static.array , e$index = 0 , e$max = e$array.length; e$index < e$max; ++e$index) {
    e = e$array[e$index];
    consumer.accept(e);
  }
}

function $get_2(this$static, index_0){
  checkCriticalElementIndex(index_0, this$static.array.length);
  return this$static.array[index_0];
}

function $indexOf_0(this$static, o, index_0){
  for (; index_0 < this$static.array.length; ++index_0) {
    if (equals_18(o, this$static.array[index_0])) {
      return index_0;
    }
  }
  return -1;
}

function $lastIndexOf(this$static, o, index_0){
  for (; index_0 >= 0; --index_0) {
    if (equals_18(o, this$static.array[index_0])) {
      return index_0;
    }
  }
  return -1;
}

function $remove(this$static, index_0){
  var previous;
  previous = (checkCriticalElementIndex(index_0, this$static.array.length) , this$static.array[index_0]);
  removeFrom_0(this$static.array, index_0, 1);
  return previous;
}

function $set_2(this$static, index_0, o){
  var previous;
  previous = (checkCriticalElementIndex(index_0, this$static.array.length) , this$static.array[index_0]);
  this$static.array[index_0] = o;
  return previous;
}

function $sort_0(this$static, c){
  sort_0(this$static.array, this$static.array.length, c);
}

function $toArray(this$static, out){
  var i, size_0;
  size_0 = this$static.array.length;
  out.length < size_0 && (out = stampJavaTypeInfo_1(new Array(size_0), out));
  for (i = 0; i < size_0; ++i) {
    setCheck(out, i, this$static.array[i]);
  }
  out.length > size_0 && setCheck(out, size_0, null);
  return out;
}

function ArrayList(){
  $$init_0(this);
}

function ArrayList_0(initialCapacity){
  $$init_0(this);
  checkCriticalArgument(initialCapacity >= 0, 'Initial capacity must not be negative');
}

function ArrayList_1(c){
  $$init_0(this);
  insertTo_0(this.array, 0, c.toArray());
}

defineClass(18, 487, $intern_31, ArrayList, ArrayList_0, ArrayList_1);
_.addAtIndex = function add_3(index_0, o){
  checkCriticalPositionIndex(index_0, this.array.length);
  insertTo(this.array, index_0, o);
}
;
_.add = function add_4(o){
  return $add_0(this, o);
}
;
_.addAllAtIndex = function addAll_1(index_0, c){
  return $addAll_0(this, index_0, c);
}
;
_.addAll = function addAll_2(c){
  return $addAll_1(this, c);
}
;
_.clear = function clear_3(){
  this.array.length = 0;
}
;
_.contains = function contains_0(o){
  return $indexOf_0(this, o, 0) != -1;
}
;
_.getAtIndex = function get_10(index_0){
  return $get_2(this, index_0);
}
;
_.indexOf = function indexOf_0(o){
  return $indexOf_0(this, o, 0);
}
;
_.isEmpty = function isEmpty_1(){
  return this.array.length == 0;
}
;
_.iterator = function iterator_2(){
  return new ArrayList$1(this);
}
;
_.lastIndexOf = function lastIndexOf_0(o){
  return $lastIndexOf(this, o, this.array.length - 1);
}
;
_.removeAtIndex = function remove_5(index_0){
  return $remove(this, index_0);
}
;
_.remove = function remove_6(o){
  var i;
  i = $indexOf_0(this, o, 0);
  if (i == -1) {
    return false;
  }
  this.removeAtIndex(i);
  return true;
}
;
_.removeRange_0 = function removeRange_0(fromIndex, endIndex){
  var count;
  checkCriticalPositionIndexes(fromIndex, endIndex, this.array.length);
  count = endIndex - fromIndex;
  removeFrom_0(this.array, fromIndex, count);
}
;
_.setAtIndex = function set_8(index_0, o){
  return $set_2(this, index_0, o);
}
;
_.size = function size_3(){
  return this.array.length;
}
;
_.toArray = function toArray_1(){
  return clone_0(this.array);
}
;
_.toArray_0 = function toArray_2(out){
  return $toArray(this, out);
}
;
var Ljava_util_ArrayList_2_classLit = createForClass('java.util', 'ArrayList', 18);
function $add_1(this$static, element){
  this$static.beforeAdd_0(this$static.array.length, element);
  return push_1(this$static.array, element) , true;
}

function $addAll_2(this$static, collection){
  $beforeAddAll(this$static, collection);
  return $addAll_1(this$static, collection);
}

function $beforeAddAll(this$static, collection){
  var element, element$iterator, index_0;
  index_0 = this$static.array.length;
  for (element$iterator = collection.iterator(); element$iterator.hasNext_0();) {
    element = element$iterator.next_1();
    this$static.beforeAdd_0(index_0++, element);
  }
}

function $clear(this$static){
  var index_0, removed;
  for (index_0 = this$static.array.length - 1; index_0 >= 0; index_0--) {
    removed = $remove(this$static, index_0);
    this$static.afterRemove_0(index_0, removed);
  }
}

function $doRemoveAll(this$static, c, removePresent){
  var changed, element, index_0, removed, test_0;
  changed = false;
  test_0 = instanceOf(c, 39) || c.size() < 10?c:new HashSet_1(c);
  for (index_0 = this$static.array.length - 1; index_0 >= 0; index_0--) {
    element = (checkCriticalElementIndex(index_0, this$static.array.length) , this$static.array[index_0]);
    if (test_0.contains(element) == removePresent) {
      removed = $remove(this$static, index_0);
      this$static.afterRemove_0(index_0, removed);
      changed = true;
    }
  }
  return changed;
}

function $remove_0(this$static, element){
  var index_0, removed, success;
  index_0 = $indexOf_0(this$static, element, 0);
  success = index_0 >= 0;
  if (success) {
    removed = $remove(this$static, index_0);
    this$static.afterRemove_0(index_0, removed);
  }
  return success;
}

function $set_3(this$static, index_0, element){
  var oldValue;
  this$static.beforeAdd_0(index_0, element);
  oldValue = $set_2(this$static, index_0, element);
  this$static.afterRemove_0(index_0 + 1, oldValue);
  return oldValue;
}

function ReferenceList(){
  ArrayList.call(this);
}

defineClass(89, 18, $intern_31);
_.addAtIndex = function add_5(index_0, element){
  this.beforeAdd_0(index_0, element);
  checkCriticalPositionIndex(index_0, this.array.length);
  insertTo(this.array, index_0, element);
}
;
_.add = function add_6(element){
  return $add_1(this, element);
}
;
_.addAllAtIndex = function addAll_3(index_0, collection){
  $beforeAddAll(this, collection);
  return $addAll_0(this, index_0, collection);
}
;
_.addAll = function addAll_4(collection){
  return $addAll_2(this, collection);
}
;
_.clear = function clear_4(){
  $clear(this);
}
;
_.removeAtIndex = function remove_7(index_0){
  var removed;
  return removed = $remove(this, index_0) , this.afterRemove_0(index_0, removed) , removed;
}
;
_.remove = function remove_8(element){
  return $remove_0(this, element);
}
;
_.removeAll = function removeAll_0(c){
  return $doRemoveAll(this, c, true);
}
;
_.retainAll = function retainAll_0(c){
  return $doRemoveAll(this, c, false);
}
;
_.setAtIndex = function set_9(index_0, element){
  return $set_3(this, index_0, element);
}
;
var Lde_haumacher_msgbuf_util_ReferenceList_2_classLit = createForClass('de.haumacher.msgbuf.util', 'ReferenceList', 89);
function $afterRemove(this$static, index_0, element){
  this$static.this$01._listener.afterRemove(this$static.this$01, 'dashes', index_0, element);
}

function $beforeAdd(this$static, index_0, element){
  this$static.this$01._listener.beforeAdd(this$static.this$01, 'dashes', index_0, element);
}

function Border_Impl$1(this$0){
  this.this$01 = this$0;
  ReferenceList.call(this);
}

defineClass(431, 89, $intern_31, Border_Impl$1);
_.afterRemove_0 = function afterRemove_0(index_0, element){
  $afterRemove(this, index_0, castToDouble(element));
}
;
_.beforeAdd_0 = function beforeAdd_0(index_0, element){
  $beforeAdd(this, index_0, castToDouble(element));
}
;
var Lcom_top_1logic_graphic_flow_data_impl_Border_1Impl$1_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Border_Impl/1', 431);
function $clinit_CompassLayout_Impl(){
  $clinit_CompassLayout_Impl = emptyMethod;
  $clinit_Box_Impl();
  PROPERTIES_4 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['north', 'west', 'east', 'south', 'center', 'centerHeight', 'hPadding', 'vPadding'])));
  TRANSIENT_PROPERTIES_4 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetCenter(this$static, value_0){
  var after, before, oldContainer;
  before = castTo(this$static._center, 19);
  after = castTo(value_0, 19);
  if (after) {
    oldContainer = after._parent;
    if (!!oldContainer && oldContainer != this$static) {
      throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
    }
  }
  this$static._listener.beforeSet(this$static, 'center', value_0);
  !!before && $internalSetParent(before, null);
  this$static._center = value_0;
  !!after && $internalSetParent(after, this$static);
}

function $internalSetCenterHeight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'centerHeight', value_0);
  this$static._centerHeight = value_0;
}

function $internalSetEast(this$static, value_0){
  var after, before, oldContainer;
  before = castTo(this$static._east, 19);
  after = castTo(value_0, 19);
  if (after) {
    oldContainer = after._parent;
    if (!!oldContainer && oldContainer != this$static) {
      throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
    }
  }
  this$static._listener.beforeSet(this$static, 'east', value_0);
  !!before && $internalSetParent(before, null);
  this$static._east = value_0;
  !!after && $internalSetParent(after, this$static);
}

function $internalSetHPadding(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'hPadding', value_0);
  this$static._hPadding = value_0;
}

function $internalSetNorth(this$static, value_0){
  var after, before, oldContainer;
  before = castTo(this$static._north, 19);
  after = castTo(value_0, 19);
  if (after) {
    oldContainer = after._parent;
    if (!!oldContainer && oldContainer != this$static) {
      throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
    }
  }
  this$static._listener.beforeSet(this$static, 'north', value_0);
  !!before && $internalSetParent(before, null);
  this$static._north = value_0;
  !!after && $internalSetParent(after, this$static);
}

function $internalSetSouth(this$static, value_0){
  var after, before, oldContainer;
  before = castTo(this$static._south, 19);
  after = castTo(value_0, 19);
  if (after) {
    oldContainer = after._parent;
    if (!!oldContainer && oldContainer != this$static) {
      throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
    }
  }
  this$static._listener.beforeSet(this$static, 'south', value_0);
  !!before && $internalSetParent(before, null);
  this$static._south = value_0;
  !!after && $internalSetParent(after, this$static);
}

function $internalSetVPadding(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'vPadding', value_0);
  this$static._vPadding = value_0;
}

function $internalSetWest(this$static, value_0){
  var after, before, oldContainer;
  before = castTo(this$static._west, 19);
  after = castTo(value_0, 19);
  if (after) {
    oldContainer = after._parent;
    if (!!oldContainer && oldContainer != this$static) {
      throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
    }
  }
  this$static._listener.beforeSet(this$static, 'west', value_0);
  !!before && $internalSetParent(before, null);
  this$static._west = value_0;
  !!after && $internalSetParent(after, this$static);
}

function $setCenter(this$static, value_0){
  $internalSetCenter(this$static, value_0);
  return this$static;
}

function $setCenterHeight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'centerHeight', value_0);
  this$static._centerHeight = value_0;
  return this$static;
}

function $setEast(this$static, value_0){
  $internalSetEast(this$static, value_0);
  return this$static;
}

function $setHPadding(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'hPadding', value_0);
  this$static._hPadding = value_0;
  return this$static;
}

function $setNorth(this$static, value_0){
  $internalSetNorth(this$static, value_0);
  return this$static;
}

function $setSouth(this$static, value_0){
  $internalSetSouth(this$static, value_0);
  return this$static;
}

function $setVPadding(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'vPadding', value_0);
  this$static._vPadding = value_0;
  return this$static;
}

function $setWest(this$static, value_0){
  $internalSetWest(this$static, value_0);
  return this$static;
}

function CompassLayout_Impl(){
  $clinit_CompassLayout_Impl();
  Box_Impl.call(this);
}

defineClass(426, 19, $intern_29, CompassLayout_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_2(context, offsetX, offsetY){
  var height, width_0;
  this._north.computeIntrinsicSize(context, offsetX, offsetY);
  this._south.computeIntrinsicSize(context, offsetX, offsetY);
  this._west.computeIntrinsicSize(context, offsetX, offsetY);
  this._east.computeIntrinsicSize(context, offsetX, offsetY);
  this._center.computeIntrinsicSize(context, offsetX, offsetY);
  $setHPadding(this, $wnd.Math.max(this._west.getWidth(), this._east.getWidth()));
  $setVPadding(this, $wnd.Math.max(this._north.getHeight(), this._south.getHeight()));
  $setCenterHeight(this, $wnd.Math.max($wnd.Math.max(this._west.getHeight(), this._east.getHeight()), this._center.getHeight()));
  height = 2 * this._vPadding + this._centerHeight;
  width_0 = $wnd.Math.max($wnd.Math.max(this._north.getWidth(), this._south.getWidth()), 2 * this._hPadding + this._center.getWidth());
  this._listener.beforeSet(this, 'width', width_0);
  this._width = width_0;
  this._listener.beforeSet(this, 'height', height);
  this._height = height;
}
;
_.distributeSize = function distributeSize_1(context, offsetX, offsetY, width_0, height){
  var centerWidth, centerX, centerY;
  this._listener.beforeSet(this, 'x', offsetX);
  this._x = offsetX;
  this._listener.beforeSet(this, 'y', offsetY);
  this._y = offsetY;
  centerWidth = width_0 - 2 * this._hPadding;
  $setCenterHeight(this, height - 2 * this._vPadding);
  centerX = offsetX + this._hPadding;
  centerY = offsetY + this._vPadding;
  this._north.distributeSize(context, offsetX, offsetY, width_0, this._vPadding);
  this._west.distributeSize(context, offsetX, centerY, this._hPadding, this._centerHeight);
  this._center.distributeSize(context, centerX, centerY, centerWidth, this._centerHeight);
  this._east.distributeSize(context, centerX + centerWidth, centerY, this._hPadding, this._centerHeight);
  this._south.distributeSize(context, offsetX, centerY + this._centerHeight, width_0, this._vPadding);
  this._listener.beforeSet(this, 'width', width_0);
  this._width = width_0;
  this._listener.beforeSet(this, 'height', height);
  this._height = height;
}
;
_.draw = function draw_13(out){
  out.write_0(this._north);
  out.write_0(this._west);
  out.write_0(this._east);
  out.write_0(this._south);
  out.write_0(this._center);
}
;
_.self_0 = function self_17(){
  return this;
}
;
_.self_2 = function self_18(){
  return this;
}
;
_.self_3 = function self_19(){
  return this;
}
;
_.setCssClass_0 = function setCssClass_11(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_12(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_6(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_6(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_6(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_6(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_11(field){
  switch (field) {
    case 'north':
      return this._north;
    case 'west':
      return this._west;
    case 'east':
      return this._east;
    case 'south':
      return this._south;
    case 'center':
      return this._center;
    case 'centerHeight':
      return this._centerHeight;
    case 'hPadding':
      return this._hPadding;
    case 'vPadding':
      return this._vPadding;
    default:return $get_0(this, field);
  }
}
;
_.jsonType = function jsonType_1(){
  return 'CompassLayout';
}
;
_.properties_0 = function properties_7(){
  return PROPERTIES_4;
}
;
_.readField = function readField_5(scope_0, in_0, field){
  switch (field) {
    case 'north':
      $setNorth(this, readBox(scope_0, in_0));
      break;
    case 'west':
      $setWest(this, readBox(scope_0, in_0));
      break;
    case 'east':
      $setEast(this, readBox(scope_0, in_0));
      break;
    case 'south':
      $setSouth(this, readBox(scope_0, in_0));
      break;
    case 'center':
      $setCenter(this, readBox(scope_0, in_0));
      break;
    case 'centerHeight':
      $setCenterHeight(this, $nextDouble_0(in_0));
      break;
    case 'hPadding':
      $setHPadding(this, $nextDouble_0(in_0));
      break;
    case 'vPadding':
      $setVPadding(this, $nextDouble_0(in_0));
      break;
    default:$readField_0(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_10(field, value_0){
  switch (field) {
    case 'north':
      $internalSetNorth(this, castTo(value_0, 8));
      break;
    case 'west':
      $internalSetWest(this, castTo(value_0, 8));
      break;
    case 'east':
      $internalSetEast(this, castTo(value_0, 8));
      break;
    case 'south':
      $internalSetSouth(this, castTo(value_0, 8));
      break;
    case 'center':
      $internalSetCenter(this, castTo(value_0, 8));
      break;
    case 'centerHeight':
      $internalSetCenterHeight(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'hPadding':
      $internalSetHPadding(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'vPadding':
      $internalSetVPadding(this, $doubleValue(castToDouble(value_0)));
      break;
    default:$set_0(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_5(){
  return TRANSIENT_PROPERTIES_4;
}
;
_.writeFieldValue = function writeFieldValue_5(scope_0, out, field){
  switch (field) {
    case 'north':
      {
        this._north?this._north.writeTo_0(scope_0, out):$nullValue_0(out);
        break;
      }

    case 'west':
      {
        this._west?this._west.writeTo_0(scope_0, out):$nullValue_0(out);
        break;
      }

    case 'east':
      {
        this._east?this._east.writeTo_0(scope_0, out):$nullValue_0(out);
        break;
      }

    case 'south':
      {
        this._south?this._south.writeTo_0(scope_0, out):$nullValue_0(out);
        break;
      }

    case 'center':
      {
        this._center?this._center.writeTo_0(scope_0, out):$nullValue_0(out);
        break;
      }

    case 'centerHeight':
      {
        $value_2(out, this._centerHeight);
        break;
      }

    case 'hPadding':
      {
        $value_2(out, this._hPadding);
        break;
      }

    case 'vPadding':
      {
        $value_2(out, this._vPadding);
        break;
      }

    default:$writeFieldValue_0(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_5(scope_0, out){
  $writeFields_0(this, scope_0, out);
  if (this._north) {
    $name_1(out, 'north');
    this._north.writeTo_0(scope_0, out);
  }
  if (this._west) {
    $name_1(out, 'west');
    this._west.writeTo_0(scope_0, out);
  }
  if (this._east) {
    $name_1(out, 'east');
    this._east.writeTo_0(scope_0, out);
  }
  if (this._south) {
    $name_1(out, 'south');
    this._south.writeTo_0(scope_0, out);
  }
  if (this._center) {
    $name_1(out, 'center');
    this._center.writeTo_0(scope_0, out);
  }
  $name_1(out, 'centerHeight');
  $value_2(out, this._centerHeight);
  $name_1(out, 'hPadding');
  $value_2(out, this._hPadding);
  $name_1(out, 'vPadding');
  $value_2(out, this._vPadding);
}
;
_._center = null;
_._centerHeight = 0;
_._east = null;
_._hPadding = 0;
_._north = null;
_._south = null;
_._vPadding = 0;
_._west = null;
var PROPERTIES_4, TRANSIENT_PROPERTIES_4;
var Lcom_top_1logic_graphic_flow_data_impl_CompassLayout_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'CompassLayout_Impl', 426);
function $clinit_Diagram_Impl(){
  $clinit_Diagram_Impl = emptyMethod;
  $clinit_Widget_Impl();
  PROPERTIES_5 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['root', 'selection', 'clickHandler'])));
  TRANSIENT_PROPERTIES_5 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['clickHandler'])))));
}

function $internalSetClickHandler(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'clickHandler', value_0);
  this$static._clickHandler = value_0;
}

function $internalSetRoot(this$static, value_0){
  var after, before, oldContainer;
  before = castTo(this$static._root, 19);
  after = castTo(value_0, 19);
  if (after) {
    oldContainer = after._parent;
    if (!!oldContainer && oldContainer != this$static) {
      throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
    }
  }
  this$static._listener.beforeSet(this$static, 'root', value_0);
  !!before && $internalSetParent(before, null);
  this$static._root = value_0;
  !!after && $internalSetParent(after, this$static);
}

function $internalSetSelection(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'selection' cannot be null."));
  $clear(this$static._selection);
  $addAll_2(this$static._selection, value_0);
}

function $layout(this$static, context){
  this$static._root.computeIntrinsicSize(context, 0, 0);
  this$static._root.distributeSize(context, 0, 0, this$static._root.getWidth(), this$static._root.getHeight());
}

function $setClickHandler(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'clickHandler', value_0);
  this$static._clickHandler = value_0;
  return this$static;
}

function $setRoot(this$static, value_0){
  $internalSetRoot(this$static, value_0);
  return this$static;
}

function $setSelection(this$static, value_0){
  $internalSetSelection(this$static, value_0);
  return this$static;
}

function Diagram_Impl(){
  $clinit_Diagram_Impl();
  Widget_Impl.call(this);
  this._selection = new Diagram_Impl$1(this);
}

defineClass(375, 151, {27:1, 463:1, 36:1, 28:1, 30:1, 21:1}, Diagram_Impl);
_.draw = function draw_14(out){
  $draw_3(this, out);
}
;
_.onClick_0 = function onClick_2(event_0){
  $onClick(this, event_0);
}
;
_.self_2 = function self_20(){
  return this;
}
;
_.self_3 = function self_21(){
  return this;
}
;
_.setCssClass = function setCssClass_13(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.get_1 = function get_12(field){
  switch (field) {
    case 'root':
      return this._root;
    case 'selection':
      return this._selection;
    case 'clickHandler':
      return this._clickHandler;
    default:return $get(this, field);
  }
}
;
_.jsonType = function jsonType_2(){
  return 'Diagram';
}
;
_.properties_0 = function properties_8(){
  return PROPERTIES_5;
}
;
_.readField = function readField_6(scope_0, in_0, field){
  var newValue;
  switch (field) {
    case 'root':
      $setRoot(this, readBox(scope_0, in_0));
      break;
    case 'selection':
      {
        newValue = new ArrayList;
        $beginArray_0(in_0);
        while ($hasNext_0(in_0)) {
          $add_0(newValue, readSelectableBox(scope_0, in_0));
        }
        $endArray_0(in_0);
        $internalSetSelection(this, newValue);
      }

      break;
    default:$readField(this, in_0, field);
  }
}
;
_.set_0 = function set_11(field, value_0){
  var result;
  switch (field) {
    case 'root':
      $internalSetRoot(this, castTo(value_0, 8));
      break;
    case 'selection':
      $internalSetSelection(this, (result = castTo(value_0, 13) , result));
      break;
    case 'clickHandler':
      $internalSetClickHandler(this, castTo(value_0, 236));
      break;
    default:$set(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_6(){
  return TRANSIENT_PROPERTIES_5;
}
;
_.writeElement = function writeElement_1(scope_0, out, field, element){
  switch (field) {
    case 'selection':
      {
        castTo(element, 92).writeTo_0(scope_0, out);
        break;
      }

    default:$nullValue_0(out);
  }
}
;
_.writeFieldValue = function writeFieldValue_6(scope_0, out, field){
  var x_0, x$iterator;
  switch (field) {
    case 'root':
      {
        this._root?this._root.writeTo_0(scope_0, out):$nullValue_0(out);
        break;
      }

    case 'selection':
      {
        $writeDeferredName_0(out);
        $open_0(out, 1, '[');
        for (x$iterator = new ArrayList$1(this._selection); x$iterator.i < x$iterator.this$01.array.length;) {
          x_0 = castTo($next_1(x$iterator), 92);
          $writeRefOrData(scope_0, out, x_0);
        }
        $close_1(out, 1, 2, ']');
        break;
      }

    case 'clickHandler':
      {
        if (this._clickHandler)
        ;
        else {
          $nullValue_0(out);
        }
        break;
      }

    default:$writeFieldValue(this, out, field);
  }
}
;
_.writeFields = function writeFields_6(scope_0, out){
  var x_0, x$iterator;
  $writeFields(this, out);
  if (this._root) {
    $name_1(out, 'root');
    this._root.writeTo_0(scope_0, out);
  }
  $name_1(out, 'selection');
  $writeDeferredName_0(out);
  $open_0(out, 1, '[');
  for (x$iterator = new ArrayList$1(this._selection); x$iterator.i < x$iterator.this$01.array.length;) {
    x_0 = castTo($next_1(x$iterator), 92);
    scope_0.writeRefOrData(out, x_0);
  }
  $close_1(out, 1, 2, ']');
}
;
_._clickHandler = null;
_._root = null;
var PROPERTIES_5, TRANSIENT_PROPERTIES_5;
var Lcom_top_1logic_graphic_flow_data_impl_Diagram_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Diagram_Impl', 375);
function $afterRemove_0(this$static, index_0, element){
  this$static.this$01._listener.afterRemove(this$static.this$01, 'selection', index_0, element);
}

function $beforeAdd_0(this$static, index_0, element){
  this$static.this$01._listener.beforeAdd(this$static.this$01, 'selection', index_0, element);
}

function Diagram_Impl$1(this$0){
  this.this$01 = this$0;
  ReferenceList.call(this);
}

defineClass(376, 89, $intern_31, Diagram_Impl$1);
_.afterRemove_0 = function afterRemove_1(index_0, element){
  $afterRemove_0(this, index_0, castTo(element, 92));
}
;
_.beforeAdd_0 = function beforeAdd_1(index_0, element){
  $beforeAdd_0(this, index_0, castTo(element, 92));
}
;
var Lcom_top_1logic_graphic_flow_data_impl_Diagram_1Impl$1_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Diagram_Impl/1', 376);
function $clinit_Empty_Impl(){
  $clinit_Empty_Impl = emptyMethod;
  $clinit_Box_Impl();
  PROPERTIES_6 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['minWidth', 'minHeight'])));
  TRANSIENT_PROPERTIES_6 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetMinHeight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'minHeight', value_0);
  this$static._minHeight = value_0;
}

function $internalSetMinWidth(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'minWidth', value_0);
  this$static._minWidth = value_0;
}

function $setHeight_2(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'height', value_0);
  this$static._height = value_0;
  return this$static;
}

function $setMinHeight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'minHeight', value_0);
  this$static._minHeight = value_0;
  return this$static;
}

function $setMinWidth(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'minWidth', value_0);
  this$static._minWidth = value_0;
  return this$static;
}

function $setWidth_2(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'width', value_0);
  this$static._width = value_0;
  return this$static;
}

function Empty_Impl(){
  $clinit_Empty_Impl();
  Box_Impl.call(this);
}

defineClass(230, 19, $intern_29, Empty_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_3(context, offsetX, offsetY){
  $setWidth_2(this, this._minWidth);
  $setHeight_2(this, this._minHeight);
}
;
_.distributeSize = function distributeSize_2(context, offsetX, offsetY, width_0, height){
  this._listener.beforeSet(this, 'x', offsetX);
  this._x = offsetX;
  this._listener.beforeSet(this, 'y', offsetY);
  this._y = offsetY;
  this._listener.beforeSet(this, 'width', width_0);
  this._width = width_0;
  this._listener.beforeSet(this, 'height', height);
  this._height = height;
}
;
_.draw = function draw_15(out){
}
;
_.self_0 = function self_22(){
  return this;
}
;
_.self_2 = function self_23(){
  return this;
}
;
_.self_3 = function self_24(){
  return this;
}
;
_.setCssClass_0 = function setCssClass_14(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_15(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_7(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_7(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_7(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_7(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_13(field){
  switch (field) {
    case 'minWidth':
      return this._minWidth;
    case 'minHeight':
      return this._minHeight;
    default:return $get_0(this, field);
  }
}
;
_.jsonType = function jsonType_3(){
  return 'Empty';
}
;
_.properties_0 = function properties_9(){
  return PROPERTIES_6;
}
;
_.readField = function readField_7(scope_0, in_0, field){
  switch (field) {
    case 'minWidth':
      $setMinWidth(this, $nextDouble_0(in_0));
      break;
    case 'minHeight':
      $setMinHeight(this, $nextDouble_0(in_0));
      break;
    default:$readField_0(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_12(field, value_0){
  switch (field) {
    case 'minWidth':
      $internalSetMinWidth(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'minHeight':
      $internalSetMinHeight(this, $doubleValue(castToDouble(value_0)));
      break;
    default:$set_0(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_7(){
  return TRANSIENT_PROPERTIES_6;
}
;
_.writeFieldValue = function writeFieldValue_7(scope_0, out, field){
  switch (field) {
    case 'minWidth':
      {
        $value_2(out, this._minWidth);
        break;
      }

    case 'minHeight':
      {
        $value_2(out, this._minHeight);
        break;
      }

    default:$writeFieldValue_0(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_7(scope_0, out){
  $writeFields_0(this, scope_0, out);
  $name_1(out, 'minWidth');
  $value_2(out, this._minWidth);
  $name_1(out, 'minHeight');
  $value_2(out, this._minHeight);
}
;
_._minHeight = 0;
_._minWidth = 0;
var PROPERTIES_6, TRANSIENT_PROPERTIES_6;
var Lcom_top_1logic_graphic_flow_data_impl_Empty_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Empty_Impl', 230);
function $clinit_Fill_Impl(){
  $clinit_Fill_Impl = emptyMethod;
  $clinit_Decoration_Impl();
  PROPERTIES_7 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['fillStyle'])));
  TRANSIENT_PROPERTIES_7 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetFillStyle(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'fillStyle', value_0);
  this$static._fillStyle = value_0;
}

function $setFillStyle(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'fillStyle', value_0);
  this$static._fillStyle = value_0;
  return this$static;
}

function $setHeight_3(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'height', value_0);
  this$static._height = value_0;
  return this$static;
}

function $setWidth_3(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'width', value_0);
  this$static._width = value_0;
  return this$static;
}

function $setY_6(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'y', value_0);
  this$static._y = value_0;
  return this$static;
}

function Fill_Impl(){
  $clinit_Fill_Impl();
  Decoration_Impl.call(this);
}

defineClass(432, 136, $intern_29, Fill_Impl);
_.distributeSize = function distributeSize_3(context, offsetX, offsetY, width_0, height){
  this._content.distributeSize(context, offsetX, offsetY, width_0, height);
  $setHeight_3($setWidth_3($setY_6((this._listener.beforeSet(this, 'x', offsetX) , this._x = offsetX , this), offsetY), width_0), height);
}
;
_.draw = function draw_16(out){
  out.beginPath_0(this);
  out.writeCssClass(this._cssClass);
  out.setStroke('none');
  out.setFill(this._fillStyle);
  out.beginData();
  out.moveToAbs(this._x, this._y);
  out.lineToHorizontalAbs(this._x + this._width);
  out.lineToVerticalAbs(this._y + this._height);
  out.lineToHorizontalAbs(this._x);
  out.closePath();
  out.endData();
  out.endPath();
  out.write_0(this._content);
}
;
_.self_0 = function self_25(){
  return this;
}
;
_.self_1 = function self_26(){
  return this;
}
;
_.self_2 = function self_27(){
  return this;
}
;
_.self_3 = function self_28(){
  return this;
}
;
_.setClientId = function setClientId_5(value_0){
  return this._listener.beforeSet(this, 'clientId', value_0) , this._clientId = value_0 , this;
}
;
_.setClientId_0 = function setClientId_6(value_0){
  return this._listener.beforeSet(this, 'clientId', value_0) , this._clientId = value_0 , this;
}
;
_.setContent = function setContent_2(value_0){
  return $internalSetContent(this, value_0) , this;
}
;
_.setCssClass_0 = function setCssClass_16(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_1 = function setCssClass_17(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_18(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_8(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight = function setHeight_9(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_8(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth = function setWidth_9(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_8(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_0 = function setX_9(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_8(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_0 = function setY_9(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_14(field){
  switch (field) {
    case 'fillStyle':
      return this._fillStyle;
    default:return $get_1(this, field);
  }
}
;
_.jsonType = function jsonType_4(){
  return 'Fill';
}
;
_.properties_0 = function properties_10(){
  return PROPERTIES_7;
}
;
_.readField = function readField_8(scope_0, in_0, field){
  switch (field) {
    case 'fillStyle':
      $setFillStyle(this, nextStringOptional(in_0));
      break;
    default:$readField_1(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_13(field, value_0){
  switch (field) {
    case 'fillStyle':
      $internalSetFillStyle(this, castToString(value_0));
      break;
    default:$set_1(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_8(){
  return TRANSIENT_PROPERTIES_7;
}
;
_.writeFieldValue = function writeFieldValue_8(scope_0, out, field){
  switch (field) {
    case 'fillStyle':
      {
        $value_4(out, this._fillStyle);
        break;
      }

    default:$writeFieldValue_1(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_8(scope_0, out){
  $writeFields_1(this, scope_0, out);
  $name_1(out, 'fillStyle');
  $value_4(out, this._fillStyle);
}
;
_._fillStyle = 'black';
var PROPERTIES_7, TRANSIENT_PROPERTIES_7;
var Lcom_top_1logic_graphic_flow_data_impl_Fill_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Fill_Impl', 432);
function $clinit_FloatingLayout_Impl(){
  $clinit_FloatingLayout_Impl = emptyMethod;
  $clinit_Box_Impl();
  PROPERTIES_8 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['nodes'])));
  TRANSIENT_PROPERTIES_8 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $get_3(this$static, field){
  switch (field) {
    case 'nodes':
      return this$static._nodes;
    default:return $get_0(this$static, field);
  }
}

function $internalSetNodes(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'nodes' cannot be null."));
  $clear(this$static._nodes);
  $addAll_2(this$static._nodes, value_0);
}

function $readField_2(this$static, scope_0, in_0, field){
  var newValue;
  switch (field) {
    case 'nodes':
      {
        newValue = new ArrayList;
        $beginArray_0(in_0);
        while ($hasNext_0(in_0)) {
          $add_0(newValue, readBox(scope_0, in_0));
        }
        $endArray_0(in_0);
        this$static.setNodes(newValue);
      }

      break;
    default:$readField_0(this$static, scope_0, in_0, field);
  }
}

function $set_4(this$static, field, value_0){
  var result;
  switch (field) {
    case 'nodes':
      $internalSetNodes(this$static, (result = castTo(value_0, 13) , result));
      break;
    default:$set_0(this$static, field, value_0);
  }
}

function $writeElement(scope_0, out, field, element){
  switch (field) {
    case 'nodes':
      {
        castTo(element, 8).writeTo_0(scope_0, out);
        break;
      }

    default:$nullValue_0(out);
  }
}

function $writeFieldValue_2(this$static, scope_0, out, field){
  var x_0, x$iterator;
  switch (field) {
    case 'nodes':
      {
        $writeDeferredName_0(out);
        $open_0(out, 1, '[');
        for (x$iterator = new ArrayList$1(this$static._nodes); x$iterator.i < x$iterator.this$01.array.length;) {
          x_0 = castTo($next_1(x$iterator), 8);
          x_0.writeTo_0(scope_0, out);
        }
        $close_1(out, 1, 2, ']');
        break;
      }

    default:$writeFieldValue_0(this$static, scope_0, out, field);
  }
}

function $writeFields_2(this$static, scope_0, out){
  var x_0, x$iterator;
  $writeFields_0(this$static, scope_0, out);
  $name_1(out, 'nodes');
  $writeDeferredName_0(out);
  $open_0(out, 1, '[');
  for (x$iterator = new ArrayList$1(this$static._nodes); x$iterator.i < x$iterator.this$01.array.length;) {
    x_0 = castTo($next_1(x$iterator), 8);
    x_0.writeTo_0(scope_0, out);
  }
  $close_1(out, 1, 2, ']');
}

function FloatingLayout_Impl(){
  $clinit_FloatingLayout_Impl();
  Box_Impl.call(this);
  this._nodes = new FloatingLayout_Impl$1(this);
}

defineClass(229, 19, $intern_29, FloatingLayout_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_4(context, offsetX, offsetY){
  $computeIntrinsicSize_0(this, context, offsetX, offsetY);
}
;
_.distributeSize = function distributeSize_4(context, offsetX, offsetY, width_0, height){
  $distributeSize_1(this, context, offsetX, offsetY, width_0, height);
}
;
_.draw = function draw_17(out){
  out.beginGroup();
  out.translate(this.self_4().getX(), this.self_4().getY());
  this.drawContents(out);
  out.endGroup();
}
;
_.drawContents = function drawContents(out){
  $drawContents(this, out);
}
;
_.self_0 = function self_29(){
  return this.self_4();
}
;
_.self_4 = function self_30(){
  return this;
}
;
_.self_2 = function self_31(){
  return this.self_4();
}
;
_.self_3 = function self_32(){
  return this.self_2();
}
;
_.setCssClass_0 = function setCssClass_19(value_0){
  return this.setCssClass_2(value_0);
}
;
_.setCssClass = function setCssClass_21(value_0){
  return this.setCssClass_2(value_0);
}
;
_.setHeight_1 = function setHeight_10(value_0){
  return this.setHeight_0(value_0);
}
;
_.setWidth_1 = function setWidth_10(value_0){
  return this.setWidth_0(value_0);
}
;
_.setX = function setX_10(value_0){
  return this.setX_1(value_0);
}
;
_.setY = function setY_10(value_0){
  return this.setY_1(value_0);
}
;
_.get_1 = function get_15(field){
  return $get_3(this, field);
}
;
_.getNodes = function getNodes(){
  return this._nodes;
}
;
_.jsonType = function jsonType_5(){
  return 'FloatingLayout';
}
;
_.properties_0 = function properties_11(){
  return PROPERTIES_8;
}
;
_.readField = function readField_9(scope_0, in_0, field){
  $readField_2(this, scope_0, in_0, field);
}
;
_.set_0 = function set_14(field, value_0){
  $set_4(this, field, value_0);
}
;
_.setCssClass_2 = function setCssClass_20(value_0){
  this._listener.beforeSet(this, 'cssClass', value_0);
  this._cssClass = value_0;
  return this;
}
;
_.setHeight_0 = function setHeight_11(value_0){
  this._listener.beforeSet(this, 'height', value_0);
  this._height = value_0;
  return this;
}
;
_.setNodes = function setNodes(value_0){
  $internalSetNodes(this, value_0);
  return this;
}
;
_.setWidth_0 = function setWidth_11(value_0){
  this._listener.beforeSet(this, 'width', value_0);
  this._width = value_0;
  return this;
}
;
_.setX_1 = function setX_11(value_0){
  this._listener.beforeSet(this, 'x', value_0);
  this._x = value_0;
  return this;
}
;
_.setY_1 = function setY_11(value_0){
  this._listener.beforeSet(this, 'y', value_0);
  this._y = value_0;
  return this;
}
;
_.transientProperties = function transientProperties_9(){
  return TRANSIENT_PROPERTIES_8;
}
;
_.writeElement = function writeElement_2(scope_0, out, field, element){
  $writeElement(scope_0, out, field, element);
}
;
_.writeFieldValue = function writeFieldValue_9(scope_0, out, field){
  $writeFieldValue_2(this, scope_0, out, field);
}
;
_.writeFields = function writeFields_9(scope_0, out){
  $writeFields_2(this, scope_0, out);
}
;
var PROPERTIES_8, TRANSIENT_PROPERTIES_8;
var Lcom_top_1logic_graphic_flow_data_impl_FloatingLayout_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'FloatingLayout_Impl', 229);
function $afterRemove_1(this$static, index_0, element){
  var removed;
  removed = castTo(element, 19);
  $internalSetParent(removed, null);
  this$static.this$01._listener.afterRemove(this$static.this$01, 'nodes', index_0, element);
}

function $beforeAdd_1(this$static, index_0, element){
  var added, oldContainer;
  added = castTo(element, 19);
  oldContainer = added._parent;
  if (!!oldContainer && oldContainer != this$static.this$01) {
    throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
  }
  this$static.this$01._listener.beforeAdd(this$static.this$01, 'nodes', index_0, element);
  $internalSetParent(added, this$static.this$01);
}

function FloatingLayout_Impl$1(this$0){
  this.this$01 = this$0;
  ReferenceList.call(this);
}

defineClass(423, 89, $intern_31, FloatingLayout_Impl$1);
_.afterRemove_0 = function afterRemove_2(index_0, element){
  $afterRemove_1(this, index_0, castTo(element, 8));
}
;
_.beforeAdd_0 = function beforeAdd_2(index_0, element){
  $beforeAdd_1(this, index_0, castTo(element, 8));
}
;
var Lcom_top_1logic_graphic_flow_data_impl_FloatingLayout_1Impl$1_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'FloatingLayout_Impl/1', 423);
function $clinit_Layout_Impl(){
  $clinit_Layout_Impl = emptyMethod;
  $clinit_Box_Impl();
  PROPERTIES_9 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['contents'])));
  TRANSIENT_PROPERTIES_9 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $get_4(this$static, field){
  switch (field) {
    case 'contents':
      return this$static._contents;
    default:return $get_0(this$static, field);
  }
}

function $internalSetContents(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'contents' cannot be null."));
  $clear(this$static._contents);
  $addAll_2(this$static._contents, value_0);
}

function $readField_3(this$static, scope_0, in_0, field){
  var newValue;
  switch (field) {
    case 'contents':
      {
        newValue = new ArrayList;
        $beginArray_0(in_0);
        while ($hasNext_0(in_0)) {
          $add_0(newValue, readBox(scope_0, in_0));
        }
        $endArray_0(in_0);
        this$static.setContents(newValue);
      }

      break;
    default:$readField_0(this$static, scope_0, in_0, field);
  }
}

function $set_5(this$static, field, value_0){
  var result;
  switch (field) {
    case 'contents':
      $internalSetContents(this$static, (result = castTo(value_0, 13) , result));
      break;
    default:$set_0(this$static, field, value_0);
  }
}

function $writeElement_0(scope_0, out, field, element){
  switch (field) {
    case 'contents':
      {
        castTo(element, 8).writeTo_0(scope_0, out);
        break;
      }

    default:$nullValue_0(out);
  }
}

function $writeFieldValue_3(this$static, scope_0, out, field){
  var x_0, x$iterator;
  switch (field) {
    case 'contents':
      {
        $writeDeferredName_0(out);
        $open_0(out, 1, '[');
        for (x$iterator = new ArrayList$1(this$static._contents); x$iterator.i < x$iterator.this$01.array.length;) {
          x_0 = castTo($next_1(x$iterator), 8);
          x_0.writeTo_0(scope_0, out);
        }
        $close_1(out, 1, 2, ']');
        break;
      }

    default:$writeFieldValue_0(this$static, scope_0, out, field);
  }
}

function $writeFields_3(this$static, scope_0, out){
  var x_0, x$iterator;
  $writeFields_0(this$static, scope_0, out);
  $name_1(out, 'contents');
  $writeDeferredName_0(out);
  $open_0(out, 1, '[');
  for (x$iterator = new ArrayList$1(this$static._contents); x$iterator.i < x$iterator.this$01.array.length;) {
    x_0 = castTo($next_1(x$iterator), 8);
    x_0.writeTo_0(scope_0, out);
  }
  $close_1(out, 1, 2, ']');
}

function Layout_Impl(){
  $clinit_Layout_Impl();
  Box_Impl.call(this);
  this._contents = new Layout_Impl$1(this);
}

defineClass(231, 19, $intern_29);
_.setCssClass_0 = function setCssClass_22(value_0){
  return this.setCssClass_3(value_0);
}
;
_.setCssClass = function setCssClass_24(value_0){
  return this.setCssClass_3(value_0);
}
;
_.setHeight_1 = function setHeight_12(value_0){
  return this.setHeight_2(value_0);
}
;
_.setWidth_1 = function setWidth_12(value_0){
  return this.setWidth_2(value_0);
}
;
_.setX = function setX_12(value_0){
  return this.setX_2(value_0);
}
;
_.setY = function setY_12(value_0){
  return this.setY_2(value_0);
}
;
_.get_1 = function get_16(field){
  return $get_4(this, field);
}
;
_.properties_0 = function properties_12(){
  return PROPERTIES_9;
}
;
_.readField = function readField_10(scope_0, in_0, field){
  $readField_3(this, scope_0, in_0, field);
}
;
_.set_0 = function set_15(field, value_0){
  $set_5(this, field, value_0);
}
;
_.setContents = function setContents(value_0){
  $internalSetContents(this, value_0);
  return this;
}
;
_.setCssClass_3 = function setCssClass_23(value_0){
  this._listener.beforeSet(this, 'cssClass', value_0);
  this._cssClass = value_0;
  return this;
}
;
_.setHeight_2 = function setHeight_13(value_0){
  this._listener.beforeSet(this, 'height', value_0);
  this._height = value_0;
  return this;
}
;
_.setWidth_2 = function setWidth_13(value_0){
  this._listener.beforeSet(this, 'width', value_0);
  this._width = value_0;
  return this;
}
;
_.setX_2 = function setX_13(value_0){
  this._listener.beforeSet(this, 'x', value_0);
  this._x = value_0;
  return this;
}
;
_.setY_2 = function setY_13(value_0){
  this._listener.beforeSet(this, 'y', value_0);
  this._y = value_0;
  return this;
}
;
_.transientProperties = function transientProperties_10(){
  return TRANSIENT_PROPERTIES_9;
}
;
_.writeElement = function writeElement_3(scope_0, out, field, element){
  $writeElement_0(scope_0, out, field, element);
}
;
_.writeFieldValue = function writeFieldValue_10(scope_0, out, field){
  $writeFieldValue_3(this, scope_0, out, field);
}
;
_.writeFields = function writeFields_10(scope_0, out){
  $writeFields_3(this, scope_0, out);
}
;
var PROPERTIES_9, TRANSIENT_PROPERTIES_9;
var Lcom_top_1logic_graphic_flow_data_impl_Layout_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Layout_Impl', 231);
function $clinit_GridLayout_Impl(){
  $clinit_GridLayout_Impl = emptyMethod;
  $clinit_Layout_Impl();
  PROPERTIES_10 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['rows', 'cols', 'gapX', 'gapY', 'rowHeight', 'colWidth'])));
  TRANSIENT_PROPERTIES_10 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetColWidth(this$static, value_0){
  $clear(this$static._colWidth);
  $addAll_2(this$static._colWidth, value_0);
}

function $internalSetCols(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'cols', valueOf(value_0));
  this$static._cols = value_0;
}

function $internalSetGapX(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'gapX', value_0);
  this$static._gapX = value_0;
}

function $internalSetGapY(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'gapY', value_0);
  this$static._gapY = value_0;
}

function $internalSetRowHeight(this$static, value_0){
  $clear(this$static._rowHeight);
  $addAll_2(this$static._rowHeight, value_0);
}

function $internalSetRows(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'rows', valueOf(value_0));
  this$static._rows = value_0;
}

function $setColWidth(this$static, value_0){
  $clear(this$static._colWidth);
  $addAll_2(this$static._colWidth, value_0);
  return this$static;
}

function $setCols(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'cols', valueOf(value_0));
  this$static._cols = value_0;
  return this$static;
}

function $setGapX(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'gapX', value_0);
  this$static._gapX = value_0;
  return this$static;
}

function $setGapY(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'gapY', value_0);
  this$static._gapY = value_0;
  return this$static;
}

function $setRowHeight(this$static, value_0){
  $clear(this$static._rowHeight);
  $addAll_2(this$static._rowHeight, value_0);
  return this$static;
}

function $setRows(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'rows', valueOf(value_0));
  this$static._rows = value_0;
  return this$static;
}

function GridLayout_Impl(){
  $clinit_GridLayout_Impl();
  Layout_Impl.call(this);
  this._rowHeight = new GridLayout_Impl$1(this);
  this._colWidth = new GridLayout_Impl$2(this);
}

defineClass(434, 231, $intern_29, GridLayout_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_5(context, offsetX, offsetY){
  $computeIntrinsicSize_1(this, context, offsetX, offsetY);
}
;
_.distributeSize = function distributeSize_5(context, offsetX, offsetY, width_0, height){
  $distributeSize_2(this, context, offsetX, offsetY, width_0, height);
}
;
_.draw = function draw_18(out){
  $draw_4(this, out);
}
;
_.self_0 = function self_33(){
  return this;
}
;
_.self_2 = function self_34(){
  return this;
}
;
_.self_3 = function self_35(){
  return this;
}
;
_.setContents = function setContents_0(value_0){
  return $internalSetContents(this, value_0) , this;
}
;
_.setCssClass_0 = function setCssClass_25(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_3 = function setCssClass_26(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_27(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_14(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight_2 = function setHeight_15(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_14(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth_2 = function setWidth_15(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_14(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_2 = function setX_15(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_14(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_2 = function setY_15(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_17(field){
  switch (field) {
    case 'rows':
      return valueOf(this._rows);
    case 'cols':
      return valueOf(this._cols);
    case 'gapX':
      return this._gapX;
    case 'gapY':
      return this._gapY;
    case 'rowHeight':
      return this._rowHeight;
    case 'colWidth':
      return this._colWidth;
    default:return $get_4(this, field);
  }
}
;
_.jsonType = function jsonType_6(){
  return 'GridLayout';
}
;
_.properties_0 = function properties_13(){
  return PROPERTIES_10;
}
;
_.readField = function readField_11(scope_0, in_0, field){
  var newValue;
  switch (field) {
    case 'rows':
      $setRows(this, $nextInt(in_0));
      break;
    case 'cols':
      $setCols(this, $nextInt(in_0));
      break;
    case 'gapX':
      $setGapX(this, $nextDouble_0(in_0));
      break;
    case 'gapY':
      $setGapY(this, $nextDouble_0(in_0));
      break;
    case 'rowHeight':
      {
        newValue = new ArrayList;
        $beginArray_0(in_0);
        while ($hasNext_0(in_0)) {
          $add_0(newValue, $nextDouble_0(in_0));
        }
        $endArray_0(in_0);
        $clear(this._rowHeight);
        $addAll_2(this._rowHeight, newValue);
      }

      break;
    case 'colWidth':
      {
        newValue = new ArrayList;
        $beginArray_0(in_0);
        while ($hasNext_0(in_0)) {
          $add_0(newValue, $nextDouble_0(in_0));
        }
        $endArray_0(in_0);
        $clear(this._colWidth);
        $addAll_2(this._colWidth, newValue);
      }

      break;
    default:$readField_3(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_16(field, value_0){
  var result, result0;
  switch (field) {
    case 'rows':
      $internalSetRows(this, castTo(value_0, 64).value_0);
      break;
    case 'cols':
      $internalSetCols(this, castTo(value_0, 64).value_0);
      break;
    case 'gapX':
      $internalSetGapX(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'gapY':
      $internalSetGapY(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'rowHeight':
      $internalSetRowHeight(this, (result0 = castTo(value_0, 13) , result0));
      break;
    case 'colWidth':
      $internalSetColWidth(this, (result = castTo(value_0, 13) , result));
      break;
    default:$set_5(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_11(){
  return TRANSIENT_PROPERTIES_10;
}
;
_.writeElement = function writeElement_4(scope_0, out, field, element){
  switch (field) {
    case 'colWidth':
    case 'rowHeight':
      {
        $value_2(out, $doubleValue(castToDouble(element)));
        break;
      }

    default:$writeElement_0(scope_0, out, field, element);
  }
}
;
_.writeFieldValue = function writeFieldValue_11(scope_0, out, field){
  var x_0, x$iterator;
  switch (field) {
    case 'rows':
      {
        $value_3(out, this._rows);
        break;
      }

    case 'cols':
      {
        $value_3(out, this._cols);
        break;
      }

    case 'gapX':
      {
        $value_2(out, this._gapX);
        break;
      }

    case 'gapY':
      {
        $value_2(out, this._gapY);
        break;
      }

    case 'rowHeight':
      {
        $writeDeferredName_0(out);
        $open_0(out, 1, '[');
        for (x$iterator = new ArrayList$1(this._rowHeight); x$iterator.i < x$iterator.this$01.array.length;) {
          x_0 = $doubleValue(castToDouble($next_1(x$iterator)));
          $value_2(out, x_0);
        }
        $close_1(out, 1, 2, ']');
        break;
      }

    case 'colWidth':
      {
        $writeDeferredName_0(out);
        $open_0(out, 1, '[');
        for (x$iterator = new ArrayList$1(this._colWidth); x$iterator.i < x$iterator.this$01.array.length;) {
          x_0 = $doubleValue(castToDouble($next_1(x$iterator)));
          $value_2(out, x_0);
        }
        $close_1(out, 1, 2, ']');
        break;
      }

    default:$writeFieldValue_3(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_11(scope_0, out){
  var x_0, x$iterator, x$iterator0;
  $writeFields_3(this, scope_0, out);
  $name_1(out, 'rows');
  $value_3(out, this._rows);
  $name_1(out, 'cols');
  $value_3(out, this._cols);
  $name_1(out, 'gapX');
  $value_2(out, this._gapX);
  $name_1(out, 'gapY');
  $value_2(out, this._gapY);
  $name_1(out, 'rowHeight');
  $writeDeferredName_0(out);
  $open_0(out, 1, '[');
  for (x$iterator0 = new ArrayList$1(this._rowHeight); x$iterator0.i < x$iterator0.this$01.array.length;) {
    x_0 = $doubleValue(castToDouble($next_1(x$iterator0)));
    $value_2(out, x_0);
  }
  $close_1(out, 1, 2, ']');
  $name_1(out, 'colWidth');
  $writeDeferredName_0(out);
  $open_0(out, 1, '[');
  for (x$iterator = new ArrayList$1(this._colWidth); x$iterator.i < x$iterator.this$01.array.length;) {
    x_0 = $doubleValue(castToDouble($next_1(x$iterator)));
    $value_2(out, x_0);
  }
  $close_1(out, 1, 2, ']');
}
;
_._cols = 0;
_._gapX = 0;
_._gapY = 0;
_._rows = 0;
var PROPERTIES_10, TRANSIENT_PROPERTIES_10;
var Lcom_top_1logic_graphic_flow_data_impl_GridLayout_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'GridLayout_Impl', 434);
function $afterRemove_2(this$static, index_0, element){
  this$static.this$01._listener.afterRemove(this$static.this$01, 'rowHeight', index_0, element);
}

function $beforeAdd_2(this$static, index_0, element){
  this$static.this$01._listener.beforeAdd(this$static.this$01, 'rowHeight', index_0, element);
}

function GridLayout_Impl$1(this$0){
  this.this$01 = this$0;
  ReferenceList.call(this);
}

defineClass(435, 89, $intern_31, GridLayout_Impl$1);
_.afterRemove_0 = function afterRemove_3(index_0, element){
  $afterRemove_2(this, index_0, castToDouble(element));
}
;
_.beforeAdd_0 = function beforeAdd_3(index_0, element){
  $beforeAdd_2(this, index_0, castToDouble(element));
}
;
var Lcom_top_1logic_graphic_flow_data_impl_GridLayout_1Impl$1_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'GridLayout_Impl/1', 435);
function $afterRemove_3(this$static, index_0, element){
  this$static.this$01._listener.afterRemove(this$static.this$01, 'colWidth', index_0, element);
}

function $beforeAdd_3(this$static, index_0, element){
  this$static.this$01._listener.beforeAdd(this$static.this$01, 'colWidth', index_0, element);
}

function GridLayout_Impl$2(this$0){
  this.this$01 = this$0;
  ReferenceList.call(this);
}

defineClass(436, 89, $intern_31, GridLayout_Impl$2);
_.afterRemove_0 = function afterRemove_4(index_0, element){
  $afterRemove_3(this, index_0, castToDouble(element));
}
;
_.beforeAdd_0 = function beforeAdd_4(index_0, element){
  $beforeAdd_3(this, index_0, castToDouble(element));
}
;
var Lcom_top_1logic_graphic_flow_data_impl_GridLayout_1Impl$2_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'GridLayout_Impl/2', 436);
function $clinit_RowLayout_Impl(){
  $clinit_RowLayout_Impl = emptyMethod;
  $clinit_Layout_Impl();
  PROPERTIES_11 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['gap', 'fill'])));
  TRANSIENT_PROPERTIES_11 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetFill(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'fill' cannot be null."));
  this$static._listener.beforeSet(this$static, 'fill', value_0);
  this$static._fill = value_0;
}

function $internalSetGap(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'gap', value_0);
  this$static._gap = value_0;
}

function RowLayout_Impl(){
  Layout_Impl.call(this);
  this._fill = ($clinit_SpaceDistribution() , NONE_3);
}

defineClass(232, 231, $intern_29);
_.setContents = function setContents_1(value_0){
  return this.setContents_0(value_0);
}
;
_.setCssClass_0 = function setCssClass_28(value_0){
  return this.setCssClass_4(value_0);
}
;
_.setCssClass_3 = function setCssClass_29(value_0){
  return this.setCssClass_4(value_0);
}
;
_.setCssClass = function setCssClass_31(value_0){
  return this.setCssClass_4(value_0);
}
;
_.setHeight_1 = function setHeight_16(value_0){
  return this.setHeight_3(value_0);
}
;
_.setHeight_2 = function setHeight_17(value_0){
  return this.setHeight_3(value_0);
}
;
_.setWidth_1 = function setWidth_16(value_0){
  return this.setWidth_3(value_0);
}
;
_.setWidth_2 = function setWidth_17(value_0){
  return this.setWidth_3(value_0);
}
;
_.setX = function setX_16(value_0){
  return this.setX_3(value_0);
}
;
_.setX_2 = function setX_17(value_0){
  return this.setX_3(value_0);
}
;
_.setY = function setY_16(value_0){
  return this.setY_3(value_0);
}
;
_.setY_2 = function setY_17(value_0){
  return this.setY_3(value_0);
}
;
_.get_1 = function get_18(field){
  switch (field) {
    case 'gap':
      return this._gap;
    case 'fill':
      return this._fill;
    default:return $get_4(this, field);
  }
}
;
_.properties_0 = function properties_14(){
  return PROPERTIES_11;
}
;
_.readField = function readField_12(scope_0, in_0, field){
  switch (field) {
    case 'gap':
      this.setGap($nextDouble_0(in_0));
      break;
    case 'fill':
      this.setFill_0(($clinit_SpaceDistribution() , valueOfProtocol_3($nextString_0(in_0))));
      break;
    default:$readField_3(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_17(field, value_0){
  switch (field) {
    case 'gap':
      $internalSetGap(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'fill':
      $internalSetFill(this, castTo(value_0, 104));
      break;
    default:$set_5(this, field, value_0);
  }
}
;
_.setContents_0 = function setContents_2(value_0){
  $internalSetContents(this, value_0);
  return this;
}
;
_.setCssClass_4 = function setCssClass_30(value_0){
  this._listener.beforeSet(this, 'cssClass', value_0);
  this._cssClass = value_0;
  return this;
}
;
_.setFill_0 = function setFill_1(value_0){
  $internalSetFill(this, value_0);
  return this;
}
;
_.setGap = function setGap(value_0){
  this._listener.beforeSet(this, 'gap', value_0);
  this._gap = value_0;
  return this;
}
;
_.setHeight_3 = function setHeight_18(value_0){
  this._listener.beforeSet(this, 'height', value_0);
  this._height = value_0;
  return this;
}
;
_.setWidth_3 = function setWidth_18(value_0){
  this._listener.beforeSet(this, 'width', value_0);
  this._width = value_0;
  return this;
}
;
_.setX_3 = function setX_18(value_0){
  this._listener.beforeSet(this, 'x', value_0);
  this._x = value_0;
  return this;
}
;
_.setY_3 = function setY_18(value_0){
  this._listener.beforeSet(this, 'y', value_0);
  this._y = value_0;
  return this;
}
;
_.transientProperties = function transientProperties_12(){
  return TRANSIENT_PROPERTIES_11;
}
;
_.writeFieldValue = function writeFieldValue_12(scope_0, out, field){
  switch (field) {
    case 'gap':
      {
        $value_2(out, this._gap);
        break;
      }

    case 'fill':
      {
        $writeTo_5(this._fill, out);
        break;
      }

    default:$writeFieldValue_3(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_12(scope_0, out){
  $writeFields_3(this, scope_0, out);
  $name_1(out, 'gap');
  $value_2(out, this._gap);
  $name_1(out, 'fill');
  $writeTo_5(this._fill, out);
}
;
_._gap = 0;
var PROPERTIES_11, TRANSIENT_PROPERTIES_11;
var Lcom_top_1logic_graphic_flow_data_impl_RowLayout_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'RowLayout_Impl', 232);
function HorizontalLayout_Impl(){
  $clinit_RowLayout_Impl();
  RowLayout_Impl.call(this);
}

defineClass(438, 232, $intern_29, HorizontalLayout_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_6(context, offsetX, offsetY){
  $computeIntrinsicSize_2(this, context, offsetX, offsetY);
}
;
_.distributeSize = function distributeSize_6(context, offsetX, offsetY, width_0, height){
  $distributeSize_3(this, context, offsetX, offsetY, width_0, height);
}
;
_.draw = function draw_19(out){
  $draw_5(this, out);
}
;
_.self_0 = function self_36(){
  return this;
}
;
_.self_2 = function self_37(){
  return this;
}
;
_.self_3 = function self_38(){
  return this;
}
;
_.setContents = function setContents_3(value_0){
  return $internalSetContents(this, value_0) , this;
}
;
_.setContents_0 = function setContents_4(value_0){
  return $internalSetContents(this, value_0) , this;
}
;
_.setCssClass_0 = function setCssClass_32(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_3 = function setCssClass_33(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_4 = function setCssClass_34(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_35(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setFill_0 = function setFill_2(value_0){
  return $internalSetFill(this, value_0) , this;
}
;
_.setGap = function setGap_0(value_0){
  return this._listener.beforeSet(this, 'gap', value_0) , this._gap = value_0 , this;
}
;
_.setHeight_1 = function setHeight_19(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight_2 = function setHeight_20(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight_3 = function setHeight_21(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_19(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth_2 = function setWidth_20(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth_3 = function setWidth_21(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_19(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_2 = function setX_20(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_3 = function setX_21(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_19(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_2 = function setY_20(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_3 = function setY_21(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.jsonType = function jsonType_7(){
  return 'HorizontalLayout';
}
;
var Lcom_top_1logic_graphic_flow_data_impl_HorizontalLayout_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'HorizontalLayout_Impl', 438);
function $clinit_Image_Impl(){
  $clinit_Image_Impl = emptyMethod;
  $clinit_Box_Impl();
  PROPERTIES_12 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['href', 'imgWidth', 'imgHeight', 'align', 'scale'])));
  TRANSIENT_PROPERTIES_12 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetAlign(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'align' cannot be null."));
  this$static._listener.beforeSet(this$static, 'align', value_0);
  this$static._align = value_0;
}

function $internalSetHref(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'href', value_0);
  this$static._href = value_0;
}

function $internalSetImgHeight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'imgHeight', value_0);
  this$static._imgHeight = value_0;
}

function $internalSetImgWidth(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'imgWidth', value_0);
  this$static._imgWidth = value_0;
}

function $internalSetScale(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'scale' cannot be null."));
  this$static._listener.beforeSet(this$static, 'scale', value_0);
  this$static._scale = value_0;
}

function $setAlign(this$static, value_0){
  $internalSetAlign(this$static, value_0);
  return this$static;
}

function $setHeight_4(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'height', value_0);
  this$static._height = value_0;
  return this$static;
}

function $setHref(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'href', value_0);
  this$static._href = value_0;
  return this$static;
}

function $setImgHeight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'imgHeight', value_0);
  this$static._imgHeight = value_0;
  return this$static;
}

function $setImgWidth(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'imgWidth', value_0);
  this$static._imgWidth = value_0;
  return this$static;
}

function $setScale(this$static, value_0){
  $internalSetScale(this$static, value_0);
  return this$static;
}

function $setWidth_4(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'width', value_0);
  this$static._width = value_0;
  return this$static;
}

function Image_Impl(){
  $clinit_Image_Impl();
  Box_Impl.call(this);
  this._align = ($clinit_ImageAlign() , X_MID_YMID);
  this._scale = ($clinit_ImageScale() , MEET);
}

defineClass(425, 19, $intern_29, Image_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_7(context, offsetX, offsetY){
  $setWidth_4(this, this._imgWidth);
  $setHeight_4(this, this._imgHeight);
}
;
_.distributeSize = function distributeSize_7(context, offsetX, offsetY, width_0, height){
  this._listener.beforeSet(this, 'x', offsetX);
  this._x = offsetX;
  this._listener.beforeSet(this, 'y', offsetY);
  this._y = offsetY;
  this._listener.beforeSet(this, 'width', width_0);
  this._width = width_0;
  this._listener.beforeSet(this, 'height', height);
  this._height = height;
}
;
_.draw = function draw_20(out){
  out.image_0(this._x, this._y, this._width, this._height, this._href, this._align, this._scale);
}
;
_.self_0 = function self_39(){
  return this;
}
;
_.self_2 = function self_40(){
  return this;
}
;
_.self_3 = function self_41(){
  return this;
}
;
_.setCssClass_0 = function setCssClass_36(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_37(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_22(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_22(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_22(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_22(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_19(field){
  switch (field) {
    case 'href':
      return this._href;
    case 'imgWidth':
      return this._imgWidth;
    case 'imgHeight':
      return this._imgHeight;
    case 'align':
      return this._align;
    case 'scale':
      return this._scale;
    default:return $get_0(this, field);
  }
}
;
_.jsonType = function jsonType_8(){
  return 'Image';
}
;
_.properties_0 = function properties_15(){
  return PROPERTIES_12;
}
;
_.readField = function readField_13(scope_0, in_0, field){
  switch (field) {
    case 'href':
      $setHref(this, nextStringOptional(in_0));
      break;
    case 'imgWidth':
      $setImgWidth(this, $nextDouble_0(in_0));
      break;
    case 'imgHeight':
      $setImgHeight(this, $nextDouble_0(in_0));
      break;
    case 'align':
      $setAlign(this, ($clinit_ImageAlign() , valueOfProtocol_1($nextString_0(in_0))));
      break;
    case 'scale':
      $setScale(this, ($clinit_ImageScale() , valueOfProtocol_2($nextString_0(in_0))));
      break;
    default:$readField_0(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_18(field, value_0){
  switch (field) {
    case 'href':
      $internalSetHref(this, castToString(value_0));
      break;
    case 'imgWidth':
      $internalSetImgWidth(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'imgHeight':
      $internalSetImgHeight(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'align':
      $internalSetAlign(this, castTo(value_0, 50));
      break;
    case 'scale':
      $internalSetScale(this, castTo(value_0, 137));
      break;
    default:$set_0(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_13(){
  return TRANSIENT_PROPERTIES_12;
}
;
_.writeFieldValue = function writeFieldValue_13(scope_0, out, field){
  switch (field) {
    case 'href':
      {
        $value_4(out, this._href);
        break;
      }

    case 'imgWidth':
      {
        $value_2(out, this._imgWidth);
        break;
      }

    case 'imgHeight':
      {
        $value_2(out, this._imgHeight);
        break;
      }

    case 'align':
      {
        $writeTo_3(this._align, out);
        break;
      }

    case 'scale':
      {
        $writeTo_4(this._scale, out);
        break;
      }

    default:$writeFieldValue_0(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_13(scope_0, out){
  $writeFields_0(this, scope_0, out);
  $name_1(out, 'href');
  $value_4(out, this._href);
  $name_1(out, 'imgWidth');
  $value_2(out, this._imgWidth);
  $name_1(out, 'imgHeight');
  $value_2(out, this._imgHeight);
  $name_1(out, 'align');
  $writeTo_3(this._align, out);
  $name_1(out, 'scale');
  $writeTo_4(this._scale, out);
}
;
_._href = '';
_._imgHeight = 0;
_._imgWidth = 0;
var PROPERTIES_12, TRANSIENT_PROPERTIES_12;
var Lcom_top_1logic_graphic_flow_data_impl_Image_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Image_Impl', 425);
function $afterRemove_4(this$static, index_0, element){
  var removed;
  removed = castTo(element, 19);
  $internalSetParent(removed, null);
  this$static.this$01._listener.afterRemove(this$static.this$01, 'contents', index_0, element);
}

function $beforeAdd_4(this$static, index_0, element){
  var added, oldContainer;
  added = castTo(element, 19);
  oldContainer = added._parent;
  if (!!oldContainer && oldContainer != this$static.this$01) {
    throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
  }
  this$static.this$01._listener.beforeAdd(this$static.this$01, 'contents', index_0, element);
  $internalSetParent(added, this$static.this$01);
}

function Layout_Impl$1(this$0){
  this.this$01 = this$0;
  ReferenceList.call(this);
}

defineClass(437, 89, $intern_31, Layout_Impl$1);
_.afterRemove_0 = function afterRemove_5(index_0, element){
  $afterRemove_4(this, index_0, castTo(element, 8));
}
;
_.beforeAdd_0 = function beforeAdd_5(index_0, element){
  $beforeAdd_4(this, index_0, castTo(element, 8));
}
;
var Lcom_top_1logic_graphic_flow_data_impl_Layout_1Impl$1_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Layout_Impl/1', 437);
function $clinit_Padding_Impl(){
  $clinit_Padding_Impl = emptyMethod;
  $clinit_Decoration_Impl();
  PROPERTIES_13 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['top', 'left', 'bottom', 'right'])));
  TRANSIENT_PROPERTIES_13 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetBottom_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'bottom', value_0);
  this$static._bottom = value_0;
}

function $internalSetLeft_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'left', value_0);
  this$static._left = value_0;
}

function $internalSetRight_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'right', value_0);
  this$static._right = value_0;
}

function $internalSetTop_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'top', value_0);
  this$static._top = value_0;
}

function $setBottom_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'bottom', value_0);
  this$static._bottom = value_0;
  return this$static;
}

function $setHeight_5(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'height', value_0);
  this$static._height = value_0;
  return this$static;
}

function $setLeft_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'left', value_0);
  this$static._left = value_0;
  return this$static;
}

function $setRight_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'right', value_0);
  this$static._right = value_0;
  return this$static;
}

function $setTop_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'top', value_0);
  this$static._top = value_0;
  return this$static;
}

function $setWidth_5(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'width', value_0);
  this$static._width = value_0;
  return this$static;
}

function Padding_Impl(){
  $clinit_Padding_Impl();
  Decoration_Impl.call(this);
}

defineClass(433, 136, $intern_29, Padding_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_8(context, offsetX, offsetY){
  var x_0, y_0, content_0;
  this._listener.beforeSet(this, 'x', offsetX);
  this._x = offsetX;
  this._listener.beforeSet(this, 'y', offsetY);
  this._y = offsetY;
  x_0 = offsetX + this._left;
  y_0 = offsetY + this._top;
  content_0 = this._content;
  content_0.computeIntrinsicSize(context, x_0, y_0);
  $setWidth_5(this, this._left + content_0.getWidth() + this._right);
  $setHeight_5(this, this._top + content_0.getHeight() + this._bottom);
}
;
_.distributeSize = function distributeSize_8(context, offsetX, offsetY, width_0, height){
  this._listener.beforeSet(this, 'x', offsetX);
  this._x = offsetX;
  this._listener.beforeSet(this, 'y', offsetY);
  this._y = offsetY;
  this._content.distributeSize(context, offsetX + this._left, offsetY + this._top, width_0 - this._left - this._right, height - this._top - this._bottom);
  this._listener.beforeSet(this, 'width', width_0);
  this._width = width_0;
  this._listener.beforeSet(this, 'height', height);
  this._height = height;
}
;
_.self_0 = function self_42(){
  return this;
}
;
_.self_1 = function self_43(){
  return this;
}
;
_.self_2 = function self_44(){
  return this;
}
;
_.self_3 = function self_45(){
  return this;
}
;
_.setClientId = function setClientId_7(value_0){
  return this._listener.beforeSet(this, 'clientId', value_0) , this._clientId = value_0 , this;
}
;
_.setClientId_0 = function setClientId_8(value_0){
  return this._listener.beforeSet(this, 'clientId', value_0) , this._clientId = value_0 , this;
}
;
_.setContent = function setContent_3(value_0){
  return $internalSetContent(this, value_0) , this;
}
;
_.setCssClass_0 = function setCssClass_38(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_1 = function setCssClass_39(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_40(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_23(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight = function setHeight_24(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_23(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth = function setWidth_24(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_23(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_0 = function setX_24(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_23(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_0 = function setY_24(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_20(field){
  switch (field) {
    case 'top':
      return this._top;
    case 'left':
      return this._left;
    case 'bottom':
      return this._bottom;
    case 'right':
      return this._right;
    default:return $get_1(this, field);
  }
}
;
_.jsonType = function jsonType_9(){
  return 'Padding';
}
;
_.properties_0 = function properties_16(){
  return PROPERTIES_13;
}
;
_.readField = function readField_14(scope_0, in_0, field){
  switch (field) {
    case 'top':
      $setTop_0(this, $nextDouble_0(in_0));
      break;
    case 'left':
      $setLeft_0(this, $nextDouble_0(in_0));
      break;
    case 'bottom':
      $setBottom_0(this, $nextDouble_0(in_0));
      break;
    case 'right':
      $setRight_0(this, $nextDouble_0(in_0));
      break;
    default:$readField_1(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_19(field, value_0){
  switch (field) {
    case 'top':
      $internalSetTop_0(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'left':
      $internalSetLeft_0(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'bottom':
      $internalSetBottom_0(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'right':
      $internalSetRight_0(this, $doubleValue(castToDouble(value_0)));
      break;
    default:$set_1(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_14(){
  return TRANSIENT_PROPERTIES_13;
}
;
_.writeFieldValue = function writeFieldValue_14(scope_0, out, field){
  switch (field) {
    case 'top':
      {
        $value_2(out, this._top);
        break;
      }

    case 'left':
      {
        $value_2(out, this._left);
        break;
      }

    case 'bottom':
      {
        $value_2(out, this._bottom);
        break;
      }

    case 'right':
      {
        $value_2(out, this._right);
        break;
      }

    default:$writeFieldValue_1(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_14(scope_0, out){
  $writeFields_1(this, scope_0, out);
  $name_1(out, 'top');
  $value_2(out, this._top);
  $name_1(out, 'left');
  $value_2(out, this._left);
  $name_1(out, 'bottom');
  $value_2(out, this._bottom);
  $name_1(out, 'right');
  $value_2(out, this._right);
}
;
_._bottom = 0;
_._left = 0;
_._right = 0;
_._top = 0;
var PROPERTIES_13, TRANSIENT_PROPERTIES_13;
var Lcom_top_1logic_graphic_flow_data_impl_Padding_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Padding_Impl', 433);
function $clinit_SelectableBox_Impl(){
  $clinit_SelectableBox_Impl = emptyMethod;
  $clinit_Decoration_Impl();
  PROPERTIES_14 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['selected', 'clickHandler'])));
  TRANSIENT_PROPERTIES_14 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['clickHandler'])))));
}

function $internalSetClickHandler_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'clickHandler', value_0);
  this$static._clickHandler = value_0;
}

function $internalSetSelected(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'selected', ($clinit_Boolean() , value_0?true:false));
  this$static._selected = value_0;
}

function $setClickHandler_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'clickHandler', value_0);
  this$static._clickHandler = value_0;
  return this$static;
}

function $setSelected_1(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'selected', ($clinit_Boolean() , value_0?true:false));
  this$static._selected = value_0;
  return this$static;
}

function SelectableBox_Impl(){
  $clinit_SelectableBox_Impl();
  Decoration_Impl.call(this);
}

defineClass(228, 136, {27:1, 8:1, 92:1, 36:1, 19:1, 28:1, 30:1, 21:1}, SelectableBox_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_9(context, offsetX, offsetY){
  $computeIntrinsicSize(this, context, 0, 0);
}
;
_.distributeSize = function distributeSize_9(context, offsetX, offsetY, width_0, height){
  this._content.distributeSize(context, 0, 0, width_0, height);
  this._listener.beforeSet(this, 'x', offsetX);
  this._x = offsetX;
  this._listener.beforeSet(this, 'y', offsetY);
  this._y = offsetY;
  this._listener.beforeSet(this, 'width', width_0);
  this._width = width_0;
  this._listener.beforeSet(this, 'height', height);
  this._height = height;
}
;
_.draw = function draw_21(out){
  var clickHandler, id_0;
  clickHandler = this._clickHandler;
  !!clickHandler && clickHandler.cancel_0();
  out.beginGroup_0(this);
  out.translate(this._x, this._y);
  out.writeCssClass(this._selected?'tlSelected':'tlCanSelect');
  id_0 = this._id;
  id_0 != 0 && out.writeAttribute('data-context-menu', '' + id_0);
  $setClickHandler_0(this, out.attachOnClick(this, this));
  out.write_0(this._content);
  out.endGroup();
}
;
_.onClick_0 = function onClick_3(event_0){
  $onClick_0(this, event_0);
}
;
_.self_0 = function self_46(){
  return this;
}
;
_.self_1 = function self_47(){
  return this;
}
;
_.self_2 = function self_48(){
  return this;
}
;
_.self_3 = function self_49(){
  return this;
}
;
_.setClientId = function setClientId_9(value_0){
  return this._listener.beforeSet(this, 'clientId', value_0) , this._clientId = value_0 , this;
}
;
_.setClientId_0 = function setClientId_10(value_0){
  return this._listener.beforeSet(this, 'clientId', value_0) , this._clientId = value_0 , this;
}
;
_.setContent = function setContent_4(value_0){
  return $internalSetContent(this, value_0) , this;
}
;
_.setCssClass_0 = function setCssClass_41(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_1 = function setCssClass_42(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_43(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_25(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight = function setHeight_26(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_25(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth = function setWidth_26(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_25(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_0 = function setX_26(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_25(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_0 = function setY_26(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_21(field){
  switch (field) {
    case 'selected':
      return $clinit_Boolean() , this._selected?true:false;
    case 'clickHandler':
      return this._clickHandler;
    default:return $get_1(this, field);
  }
}
;
_.jsonType = function jsonType_10(){
  return 'SelectableBox';
}
;
_.properties_0 = function properties_17(){
  return PROPERTIES_14;
}
;
_.readField = function readField_15(scope_0, in_0, field){
  switch (field) {
    case 'selected':
      $setSelected_1(this, $nextBoolean_0(in_0));
      break;
    default:$readField_1(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_20(field, value_0){
  switch (field) {
    case 'selected':
      $internalSetSelected(this, $booleanValue(castToBoolean(value_0)));
      break;
    case 'clickHandler':
      $internalSetClickHandler_0(this, castTo(value_0, 236));
      break;
    default:$set_1(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_15(){
  return TRANSIENT_PROPERTIES_14;
}
;
_.writeFieldValue = function writeFieldValue_15(scope_0, out, field){
  switch (field) {
    case 'selected':
      {
        $value_5(out, this._selected);
        break;
      }

    case 'clickHandler':
      {
        if (this._clickHandler)
        ;
        else {
          $nullValue_0(out);
        }
        break;
      }

    default:$writeFieldValue_1(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_15(scope_0, out){
  $writeFields_1(this, scope_0, out);
  $name_1(out, 'selected');
  $value_5(out, this._selected);
}
;
_._clickHandler = null;
_._selected = false;
var PROPERTIES_14, TRANSIENT_PROPERTIES_14;
var Lcom_top_1logic_graphic_flow_data_impl_SelectableBox_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'SelectableBox_Impl', 228);
function $clinit_Text_Impl(){
  $clinit_Text_Impl = emptyMethod;
  $clinit_Box_Impl();
  PROPERTIES_15 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['value', 'fontWeight', 'fontSize', 'fontFamily', 'strokeStyle', 'fillStyle', 'baseLine'])));
  TRANSIENT_PROPERTIES_15 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetBaseLine(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'baseLine', value_0);
  this$static._baseLine = value_0;
}

function $internalSetFillStyle_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'fillStyle', value_0);
  this$static._fillStyle = value_0;
}

function $internalSetFontFamily(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'fontFamily', value_0);
  this$static._fontFamily = value_0;
}

function $internalSetFontSize(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'fontSize', value_0);
  this$static._fontSize = value_0;
}

function $internalSetFontWeight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'fontWeight', value_0);
  this$static._fontWeight = value_0;
}

function $internalSetStrokeStyle_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'strokeStyle', value_0);
  this$static._strokeStyle = value_0;
}

function $internalSetValue(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'value', value_0);
  this$static._value = value_0;
}

function $setBaseLine(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'baseLine', value_0);
  this$static._baseLine = value_0;
  return this$static;
}

function $setFillStyle_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'fillStyle', value_0);
  this$static._fillStyle = value_0;
  return this$static;
}

function $setFontFamily(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'fontFamily', value_0);
  this$static._fontFamily = value_0;
  return this$static;
}

function $setFontSize(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'fontSize', value_0);
  this$static._fontSize = value_0;
  return this$static;
}

function $setFontWeight(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'fontWeight', value_0);
  this$static._fontWeight = value_0;
  return this$static;
}

function $setHeight_6(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'height', value_0);
  this$static._height = value_0;
  return this$static;
}

function $setStrokeStyle_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'strokeStyle', value_0);
  this$static._strokeStyle = value_0;
  return this$static;
}

function $setValue_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'value', value_0);
  this$static._value = value_0;
  return this$static;
}

function $setWidth_6(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'width', value_0);
  this$static._width = value_0;
  return this$static;
}

function Text_Impl(){
  $clinit_Text_Impl();
  Box_Impl.call(this);
}

defineClass(424, 19, $intern_29, Text_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_10(context, offsetX, offsetY){
  var metrics;
  metrics = $measure(context, this._value);
  $setBaseLine(this, metrics._metrics.actualBoundingBoxAscent);
  this._listener.beforeSet(this, 'x', offsetX);
  this._x = offsetX;
  this._listener.beforeSet(this, 'y', offsetY);
  this._y = offsetY;
  $setWidth_6(this, metrics._metrics.width);
  $setHeight_6(this, metrics._metrics.actualBoundingBoxAscent + metrics._metrics.actualBoundingBoxDescent);
}
;
_.distributeSize = function distributeSize_10(context, offsetX, offsetY, width_0, height){
  this._listener.beforeSet(this, 'x', offsetX);
  this._x = offsetX;
  this._listener.beforeSet(this, 'y', offsetY);
  this._y = offsetY;
  this._listener.beforeSet(this, 'width', width_0);
  this._width = width_0;
  this._listener.beforeSet(this, 'height', height);
  this._height = height;
}
;
_.draw = function draw_22(out){
  out.beginText(this._x, this._y + this._baseLine, this._value);
  out.writeCssClass(this._cssClass);
  out.setStroke(this._strokeStyle);
  out.setFill(this._fillStyle);
  out.setTextStyle(this._fontFamily, this._fontSize, this._fontWeight);
  out.endText();
}
;
_.self_0 = function self_50(){
  return this;
}
;
_.self_2 = function self_51(){
  return this;
}
;
_.self_3 = function self_52(){
  return this;
}
;
_.setCssClass_0 = function setCssClass_44(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_45(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_27(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_27(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_27(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_27(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_22(field){
  switch (field) {
    case 'value':
      return this._value;
    case 'fontWeight':
      return this._fontWeight;
    case 'fontSize':
      return this._fontSize;
    case 'fontFamily':
      return this._fontFamily;
    case 'strokeStyle':
      return this._strokeStyle;
    case 'fillStyle':
      return this._fillStyle;
    case 'baseLine':
      return this._baseLine;
    default:return $get_0(this, field);
  }
}
;
_.jsonType = function jsonType_11(){
  return 'Text';
}
;
_.properties_0 = function properties_18(){
  return PROPERTIES_15;
}
;
_.readField = function readField_16(scope_0, in_0, field){
  switch (field) {
    case 'value':
      $setValue_0(this, nextStringOptional(in_0));
      break;
    case 'fontWeight':
      $setFontWeight(this, nextStringOptional(in_0));
      break;
    case 'fontSize':
      $setFontSize(this, nextStringOptional(in_0));
      break;
    case 'fontFamily':
      $setFontFamily(this, nextStringOptional(in_0));
      break;
    case 'strokeStyle':
      $setStrokeStyle_0(this, nextStringOptional(in_0));
      break;
    case 'fillStyle':
      $setFillStyle_0(this, nextStringOptional(in_0));
      break;
    case 'baseLine':
      $setBaseLine(this, $nextDouble_0(in_0));
      break;
    default:$readField_0(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_21(field, value_0){
  switch (field) {
    case 'value':
      $internalSetValue(this, castToString(value_0));
      break;
    case 'fontWeight':
      $internalSetFontWeight(this, castToString(value_0));
      break;
    case 'fontSize':
      $internalSetFontSize(this, castToString(value_0));
      break;
    case 'fontFamily':
      $internalSetFontFamily(this, castToString(value_0));
      break;
    case 'strokeStyle':
      $internalSetStrokeStyle_0(this, castToString(value_0));
      break;
    case 'fillStyle':
      $internalSetFillStyle_0(this, castToString(value_0));
      break;
    case 'baseLine':
      $internalSetBaseLine(this, $doubleValue(castToDouble(value_0)));
      break;
    default:$set_0(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_16(){
  return TRANSIENT_PROPERTIES_15;
}
;
_.writeFieldValue = function writeFieldValue_16(scope_0, out, field){
  switch (field) {
    case 'value':
      {
        $value_4(out, this._value);
        break;
      }

    case 'fontWeight':
      {
        this._fontWeight != null?$value_4(out, this._fontWeight):$nullValue_0(out);
        break;
      }

    case 'fontSize':
      {
        this._fontSize != null?$value_4(out, this._fontSize):$nullValue_0(out);
        break;
      }

    case 'fontFamily':
      {
        this._fontFamily != null?$value_4(out, this._fontFamily):$nullValue_0(out);
        break;
      }

    case 'strokeStyle':
      {
        this._strokeStyle != null?$value_4(out, this._strokeStyle):$nullValue_0(out);
        break;
      }

    case 'fillStyle':
      {
        this._fillStyle != null?$value_4(out, this._fillStyle):$nullValue_0(out);
        break;
      }

    case 'baseLine':
      {
        $value_2(out, this._baseLine);
        break;
      }

    default:$writeFieldValue_0(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_16(scope_0, out){
  $writeFields_0(this, scope_0, out);
  $name_1(out, 'value');
  $value_4(out, this._value);
  if (this._fontWeight != null) {
    $name_1(out, 'fontWeight');
    $value_4(out, this._fontWeight);
  }
  if (this._fontSize != null) {
    $name_1(out, 'fontSize');
    $value_4(out, this._fontSize);
  }
  if (this._fontFamily != null) {
    $name_1(out, 'fontFamily');
    $value_4(out, this._fontFamily);
  }
  if (this._strokeStyle != null) {
    $name_1(out, 'strokeStyle');
    $value_4(out, this._strokeStyle);
  }
  if (this._fillStyle != null) {
    $name_1(out, 'fillStyle');
    $value_4(out, this._fillStyle);
  }
  $name_1(out, 'baseLine');
  $value_2(out, this._baseLine);
}
;
_._baseLine = 0;
_._fillStyle = null;
_._fontFamily = null;
_._fontSize = null;
_._fontWeight = null;
_._strokeStyle = null;
_._value = '';
var PROPERTIES_15, TRANSIENT_PROPERTIES_15;
var Lcom_top_1logic_graphic_flow_data_impl_Text_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'Text_Impl', 424);
function $clinit_TreeConnection_Impl(){
  $clinit_TreeConnection_Impl = emptyMethod;
  $clinit_Widget_Impl();
  PROPERTIES_16 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['owner', 'parent', 'children', 'barPosition'])));
  TRANSIENT_PROPERTIES_16 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetBarPosition(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'barPosition', value_0);
  this$static._barPosition = value_0;
}

function $internalSetChildren(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'children' cannot be null."));
  $clear(this$static._children);
  $addAll_2(this$static._children, value_0);
}

function $internalSetOwner(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'owner', value_0);
  if (!!value_0 && !!this$static._owner) {
    throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
  }
  this$static._owner = value_0;
}

function $internalSetParent_0(this$static, value_0){
  var after, before, oldContainer;
  before = this$static._parent;
  after = value_0;
  if (after) {
    oldContainer = after._connection;
    if (!!oldContainer && oldContainer != this$static) {
      throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
    }
  }
  this$static._listener.beforeSet(this$static, 'parent', value_0);
  !!before && $internalSetConnection(before, null);
  this$static._parent = value_0;
  !!after && $internalSetConnection(after, this$static);
}

function $setBarPosition(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'barPosition', value_0);
  this$static._barPosition = value_0;
  return this$static;
}

function $setParent_0(this$static, value_0){
  $internalSetParent_0(this$static, value_0);
  return this$static;
}

function TreeConnection_Impl(){
  $clinit_TreeConnection_Impl();
  Widget_Impl.call(this);
  this._children = new TreeConnection_Impl$1(this);
}

defineClass(446, 151, {27:1, 81:1, 36:1, 28:1, 30:1, 21:1}, TreeConnection_Impl);
_.draw = function draw_23(out){
  $draw_7(this, out);
}
;
_.self_2 = function self_53(){
  return this;
}
;
_.self_3 = function self_54(){
  return this;
}
;
_.setCssClass = function setCssClass_46(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.get_1 = function get_23(field){
  switch (field) {
    case 'owner':
      return this._owner;
    case 'parent':
      return this._parent;
    case 'children':
      return this._children;
    case 'barPosition':
      return this._barPosition;
    default:return $get(this, field);
  }
}
;
_.jsonType = function jsonType_12(){
  return 'TreeConnection';
}
;
_.properties_0 = function properties_19(){
  return PROPERTIES_16;
}
;
_.readField = function readField_17(scope_0, in_0, field){
  var newValue;
  switch (field) {
    case 'parent':
      $setParent_0(this, readTreeConnector(scope_0, in_0));
      break;
    case 'children':
      {
        newValue = new ArrayList;
        $beginArray_0(in_0);
        while ($hasNext_0(in_0)) {
          $add_0(newValue, readTreeConnector(scope_0, in_0));
        }
        $endArray_0(in_0);
        $internalSetChildren(this, newValue);
      }

      break;
    case 'barPosition':
      $setBarPosition(this, $nextDouble_0(in_0));
      break;
    default:$readField(this, in_0, field);
  }
}
;
_.set_0 = function set_22(field, value_0){
  var result;
  switch (field) {
    case 'parent':
      $internalSetParent_0(this, castTo(value_0, 74));
      break;
    case 'children':
      $internalSetChildren(this, (result = castTo(value_0, 13) , result));
      break;
    case 'barPosition':
      $internalSetBarPosition(this, $doubleValue(castToDouble(value_0)));
      break;
    default:$set(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_17(){
  return TRANSIENT_PROPERTIES_16;
}
;
_.writeElement = function writeElement_5(scope_0, out, field, element){
  switch (field) {
    case 'children':
      {
        castTo(element, 74).writeTo_0(scope_0, out);
        break;
      }

    default:$nullValue_0(out);
  }
}
;
_.writeFieldValue = function writeFieldValue_17(scope_0, out, field){
  var x_0, x$iterator;
  switch (field) {
    case 'owner':
      {
        this._owner?$writeTo_6(this._owner, scope_0, out):$nullValue_0(out);
        break;
      }

    case 'parent':
      {
        this._parent?$writeTo_6(this._parent, scope_0, out):$nullValue_0(out);
        break;
      }

    case 'children':
      {
        $writeDeferredName_0(out);
        $open_0(out, 1, '[');
        for (x$iterator = new ArrayList$1(this._children); x$iterator.i < x$iterator.this$01.array.length;) {
          x_0 = castTo($next_1(x$iterator), 74);
          $writeRefOrData(scope_0, out, x_0);
        }
        $close_1(out, 1, 2, ']');
        break;
      }

    case 'barPosition':
      {
        $value_2(out, this._barPosition);
        break;
      }

    default:$writeFieldValue(this, out, field);
  }
}
;
_.writeFields = function writeFields_17(scope_0, out){
  var x_0, x$iterator;
  $writeFields(this, out);
  if (this._parent) {
    $name_1(out, 'parent');
    $writeTo_6(this._parent, scope_0, out);
  }
  $name_1(out, 'children');
  $writeDeferredName_0(out);
  $open_0(out, 1, '[');
  for (x$iterator = new ArrayList$1(this._children); x$iterator.i < x$iterator.this$01.array.length;) {
    x_0 = castTo($next_1(x$iterator), 74);
    scope_0.writeRefOrData(out, x_0);
  }
  $close_1(out, 1, 2, ']');
  $name_1(out, 'barPosition');
  $value_2(out, this._barPosition);
}
;
_._barPosition = 0;
_._owner = null;
_._parent = null;
var PROPERTIES_16, TRANSIENT_PROPERTIES_16;
var Lcom_top_1logic_graphic_flow_data_impl_TreeConnection_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'TreeConnection_Impl', 446);
function $afterRemove_5(this$static, index_0, element){
  var removed;
  removed = element;
  $internalSetConnection(removed, null);
  this$static.this$01._listener.afterRemove(this$static.this$01, 'children', index_0, element);
}

function $beforeAdd_5(this$static, index_0, element){
  var added, oldContainer;
  added = element;
  oldContainer = added._connection;
  if (!!oldContainer && oldContainer != this$static.this$01) {
    throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
  }
  this$static.this$01._listener.beforeAdd(this$static.this$01, 'children', index_0, element);
  $internalSetConnection(added, this$static.this$01);
}

function TreeConnection_Impl$1(this$0){
  this.this$01 = this$0;
  ReferenceList.call(this);
}

defineClass(447, 89, $intern_31, TreeConnection_Impl$1);
_.afterRemove_0 = function afterRemove_6(index_0, element){
  $afterRemove_5(this, index_0, castTo(element, 74));
}
;
_.beforeAdd_0 = function beforeAdd_6(index_0, element){
  $beforeAdd_5(this, index_0, castTo(element, 74));
}
;
var Lcom_top_1logic_graphic_flow_data_impl_TreeConnection_1Impl$1_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'TreeConnection_Impl/1', 447);
function $clinit_TreeConnector_Impl(){
  $clinit_TreeConnector_Impl = emptyMethod;
  $clinit_Widget_Impl();
  PROPERTIES_17 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['connection', 'anchor', 'connectPosition'])));
  TRANSIENT_PROPERTIES_17 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetAnchor(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'anchor', value_0);
  this$static._anchor = value_0;
}

function $internalSetConnectPosition(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'connectPosition', value_0);
  this$static._connectPosition = value_0;
}

function $internalSetConnection(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'connection', value_0);
  if (!!value_0 && !!this$static._connection) {
    throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
  }
  this$static._connection = value_0;
}

function $setAnchor(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'anchor', value_0);
  this$static._anchor = value_0;
  return this$static;
}

function $setConnectPosition(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'connectPosition', value_0);
  this$static._connectPosition = value_0;
  return this$static;
}

function TreeConnector_Impl(){
  $clinit_TreeConnector_Impl();
  Widget_Impl.call(this);
}

defineClass(450, 151, {27:1, 74:1, 36:1, 28:1, 30:1, 21:1}, TreeConnector_Impl);
_.draw = function draw_24(out){
  var x_0, y_0;
  x_0 = this._connection._parent == this?this._anchor.getRightX():this._anchor.getX();
  y_0 = this._anchor.getY() + this._connectPosition * this._anchor.getHeight();
  out.beginRect(x_0 - 4.5, y_0 - 4.5, 9, 9);
  out.setStroke('black');
  out.setFill('none');
  out.setStrokeWidth(1);
  out.endRect();
}
;
_.self_2 = function self_55(){
  return this;
}
;
_.self_3 = function self_56(){
  return this;
}
;
_.setCssClass = function setCssClass_47(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.get_1 = function get_24(field){
  switch (field) {
    case 'connection':
      return this._connection;
    case 'anchor':
      return this._anchor;
    case 'connectPosition':
      return this._connectPosition;
    default:return $get(this, field);
  }
}
;
_.jsonType = function jsonType_13(){
  return 'TreeConnector';
}
;
_.properties_0 = function properties_20(){
  return PROPERTIES_17;
}
;
_.readField = function readField_18(scope_0, in_0, field){
  switch (field) {
    case 'anchor':
      $setAnchor(this, readBox(scope_0, in_0));
      break;
    case 'connectPosition':
      $setConnectPosition(this, $nextDouble_0(in_0));
      break;
    default:$readField(this, in_0, field);
  }
}
;
_.set_0 = function set_23(field, value_0){
  switch (field) {
    case 'anchor':
      $internalSetAnchor(this, castTo(value_0, 8));
      break;
    case 'connectPosition':
      $internalSetConnectPosition(this, $doubleValue(castToDouble(value_0)));
      break;
    default:$set(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_18(){
  return TRANSIENT_PROPERTIES_17;
}
;
_.writeFieldValue = function writeFieldValue_18(scope_0, out, field){
  switch (field) {
    case 'connection':
      {
        this._connection?$writeTo_6(this._connection, scope_0, out):$nullValue_0(out);
        break;
      }

    case 'anchor':
      {
        this._anchor?this._anchor.writeTo_0(scope_0, out):$nullValue_0(out);
        break;
      }

    case 'connectPosition':
      {
        $value_2(out, this._connectPosition);
        break;
      }

    default:$writeFieldValue(this, out, field);
  }
}
;
_.writeFields = function writeFields_18(scope_0, out){
  $writeFields(this, out);
  if (this._anchor) {
    $name_1(out, 'anchor');
    this._anchor.writeTo_0(scope_0, out);
  }
  $name_1(out, 'connectPosition');
  $value_2(out, this._connectPosition);
}
;
_._anchor = null;
_._connectPosition = 0.5;
_._connection = null;
var PROPERTIES_17, TRANSIENT_PROPERTIES_17;
var Lcom_top_1logic_graphic_flow_data_impl_TreeConnector_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'TreeConnector_Impl', 450);
function $clinit_TreeLayout_Impl(){
  $clinit_TreeLayout_Impl = emptyMethod;
  $clinit_FloatingLayout_Impl();
  PROPERTIES_18 = unmodifiableList(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['compact', 'direction', 'gapX', 'gapY', 'strokeStyle', 'thickness', 'connections'])));
  TRANSIENT_PROPERTIES_18 = ($clinit_Collections() , new Collections$UnmodifiableSet(new HashSet_1(new Arrays$ArrayList(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, [])))));
}

function $internalSetCompact(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'compact', ($clinit_Boolean() , value_0?true:false));
  this$static._compact = value_0;
}

function $internalSetConnections(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'connections' cannot be null."));
  $clear(this$static._connections);
  $addAll_2(this$static._connections, value_0);
}

function $internalSetDirection(this$static, value_0){
  if (!value_0)
    throw toJs(new IllegalArgumentException("Property 'direction' cannot be null."));
  this$static._listener.beforeSet(this$static, 'direction', value_0);
  this$static._direction = value_0;
}

function $internalSetGapX_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'gapX', value_0);
  this$static._gapX = value_0;
}

function $internalSetGapY_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'gapY', value_0);
  this$static._gapY = value_0;
}

function $internalSetStrokeStyle_1(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'strokeStyle', value_0);
  this$static._strokeStyle = value_0;
}

function $internalSetThickness_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'thickness', value_0);
  this$static._thickness = value_0;
}

function $setCompact(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'compact', ($clinit_Boolean() , value_0?true:false));
  this$static._compact = value_0;
  return this$static;
}

function $setDirection(this$static, value_0){
  $internalSetDirection(this$static, value_0);
  return this$static;
}

function $setGapX_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'gapX', value_0);
  this$static._gapX = value_0;
  return this$static;
}

function $setGapY_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'gapY', value_0);
  this$static._gapY = value_0;
  return this$static;
}

function $setStrokeStyle_1(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'strokeStyle', value_0);
  this$static._strokeStyle = value_0;
  return this$static;
}

function $setThickness_0(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'thickness', value_0);
  this$static._thickness = value_0;
  return this$static;
}

function $setWidth_7(this$static, value_0){
  this$static._listener.beforeSet(this$static, 'width', value_0);
  this$static._width = value_0;
  return this$static;
}

function TreeLayout_Impl(){
  $clinit_TreeLayout_Impl();
  FloatingLayout_Impl.call(this);
  this._direction = ($clinit_DiagramDirection() , LTR);
  this._connections = new TreeLayout_Impl$1(this);
}

defineClass(427, 229, $intern_29, TreeLayout_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_11(context, offsetX, offsetY){
  $computeIntrinsicSize_4(this, context, offsetX, offsetY);
}
;
_.distributeSize = function distributeSize_11(context, offsetX, offsetY, width_0, height){
  $distributeSize_5(this, context, offsetX, offsetY, width_0, height);
}
;
_.drawContents = function drawContents_0(out){
  $drawContents_0(this, out);
}
;
_.self_0 = function self_57(){
  return this;
}
;
_.self_4 = function self_58(){
  return this;
}
;
_.self_2 = function self_59(){
  return this;
}
;
_.self_3 = function self_60(){
  return this;
}
;
_.setCssClass_0 = function setCssClass_48(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_2 = function setCssClass_49(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_50(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setHeight_1 = function setHeight_28(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight_0 = function setHeight_29(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setNodes = function setNodes_0(value_0){
  return $internalSetNodes(this, value_0) , this;
}
;
_.setWidth_1 = function setWidth_28(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth_0 = function setWidth_29(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_28(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_1 = function setX_29(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_28(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_1 = function setY_29(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.get_1 = function get_25(field){
  switch (field) {
    case 'compact':
      return $clinit_Boolean() , this._compact?true:false;
    case 'direction':
      return this._direction;
    case 'gapX':
      return this._gapX;
    case 'gapY':
      return this._gapY;
    case 'strokeStyle':
      return this._strokeStyle;
    case 'thickness':
      return this._thickness;
    case 'connections':
      return this._connections;
    default:return $get_3(this, field);
  }
}
;
_.jsonType = function jsonType_14(){
  return 'TreeLayout';
}
;
_.properties_0 = function properties_21(){
  return PROPERTIES_18;
}
;
_.readField = function readField_19(scope_0, in_0, field){
  var newValue;
  switch (field) {
    case 'compact':
      $setCompact(this, $nextBoolean_0(in_0));
      break;
    case 'direction':
      $setDirection(this, ($clinit_DiagramDirection() , valueOfProtocol_0($nextString_0(in_0))));
      break;
    case 'gapX':
      $setGapX_0(this, $nextDouble_0(in_0));
      break;
    case 'gapY':
      $setGapY_0(this, $nextDouble_0(in_0));
      break;
    case 'strokeStyle':
      $setStrokeStyle_1(this, nextStringOptional(in_0));
      break;
    case 'thickness':
      $setThickness_0(this, $nextDouble_0(in_0));
      break;
    case 'connections':
      {
        newValue = new ArrayList;
        $beginArray_0(in_0);
        while ($hasNext_0(in_0)) {
          $add_0(newValue, readTreeConnection(scope_0, in_0));
        }
        $endArray_0(in_0);
        $internalSetConnections(this, newValue);
      }

      break;
    default:$readField_2(this, scope_0, in_0, field);
  }
}
;
_.set_0 = function set_24(field, value_0){
  var result;
  switch (field) {
    case 'compact':
      $internalSetCompact(this, $booleanValue(castToBoolean(value_0)));
      break;
    case 'direction':
      $internalSetDirection(this, castTo(value_0, 103));
      break;
    case 'gapX':
      $internalSetGapX_0(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'gapY':
      $internalSetGapY_0(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'strokeStyle':
      $internalSetStrokeStyle_1(this, castToString(value_0));
      break;
    case 'thickness':
      $internalSetThickness_0(this, $doubleValue(castToDouble(value_0)));
      break;
    case 'connections':
      $internalSetConnections(this, (result = castTo(value_0, 13) , result));
      break;
    default:$set_4(this, field, value_0);
  }
}
;
_.transientProperties = function transientProperties_19(){
  return TRANSIENT_PROPERTIES_18;
}
;
_.writeElement = function writeElement_6(scope_0, out, field, element){
  switch (field) {
    case 'connections':
      {
        castTo(element, 81).writeTo_0(scope_0, out);
        break;
      }

    default:$writeElement(scope_0, out, field, element);
  }
}
;
_.writeFieldValue = function writeFieldValue_19(scope_0, out, field){
  var x_0, x$iterator;
  switch (field) {
    case 'compact':
      {
        $value_5(out, this._compact);
        break;
      }

    case 'direction':
      {
        $writeTo_2(this._direction, out);
        break;
      }

    case 'gapX':
      {
        $value_2(out, this._gapX);
        break;
      }

    case 'gapY':
      {
        $value_2(out, this._gapY);
        break;
      }

    case 'strokeStyle':
      {
        $value_4(out, this._strokeStyle);
        break;
      }

    case 'thickness':
      {
        $value_2(out, this._thickness);
        break;
      }

    case 'connections':
      {
        $writeDeferredName_0(out);
        $open_0(out, 1, '[');
        for (x$iterator = new ArrayList$1(this._connections); x$iterator.i < x$iterator.this$01.array.length;) {
          x_0 = castTo($next_1(x$iterator), 81);
          $writeRefOrData(scope_0, out, x_0);
        }
        $close_1(out, 1, 2, ']');
        break;
      }

    default:$writeFieldValue_2(this, scope_0, out, field);
  }
}
;
_.writeFields = function writeFields_19(scope_0, out){
  var x_0, x$iterator;
  $writeFields_2(this, scope_0, out);
  $name_1(out, 'compact');
  $value_5(out, this._compact);
  $name_1(out, 'direction');
  $writeTo_2(this._direction, out);
  $name_1(out, 'gapX');
  $value_2(out, this._gapX);
  $name_1(out, 'gapY');
  $value_2(out, this._gapY);
  $name_1(out, 'strokeStyle');
  $value_4(out, this._strokeStyle);
  $name_1(out, 'thickness');
  $value_2(out, this._thickness);
  $name_1(out, 'connections');
  $writeDeferredName_0(out);
  $open_0(out, 1, '[');
  for (x$iterator = new ArrayList$1(this._connections); x$iterator.i < x$iterator.this$01.array.length;) {
    x_0 = castTo($next_1(x$iterator), 81);
    scope_0.writeRefOrData(out, x_0);
  }
  $close_1(out, 1, 2, ']');
}
;
_._compact = false;
_._gapX = 30;
_._gapY = 30;
_._strokeStyle = 'black';
_._thickness = 1;
var PROPERTIES_18, TRANSIENT_PROPERTIES_18;
var Lcom_top_1logic_graphic_flow_data_impl_TreeLayout_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'TreeLayout_Impl', 427);
function $afterRemove_6(this$static, index_0, element){
  var removed;
  removed = element;
  $internalSetOwner(removed, null);
  this$static.this$01._listener.afterRemove(this$static.this$01, 'connections', index_0, element);
}

function $beforeAdd_6(this$static, index_0, element){
  var added, oldContainer;
  added = element;
  oldContainer = added._owner;
  if (!!oldContainer && oldContainer != this$static.this$01) {
    throw toJs(new IllegalStateException_0('Object may not be part of two different containers.'));
  }
  this$static.this$01._listener.beforeAdd(this$static.this$01, 'connections', index_0, element);
  $internalSetOwner(added, this$static.this$01);
}

function TreeLayout_Impl$1(this$0){
  this.this$01 = this$0;
  ReferenceList.call(this);
}

defineClass(428, 89, $intern_31, TreeLayout_Impl$1);
_.afterRemove_0 = function afterRemove_7(index_0, element){
  $afterRemove_6(this, index_0, castTo(element, 81));
}
;
_.beforeAdd_0 = function beforeAdd_7(index_0, element){
  $beforeAdd_6(this, index_0, castTo(element, 81));
}
;
var Lcom_top_1logic_graphic_flow_data_impl_TreeLayout_1Impl$1_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'TreeLayout_Impl/1', 428);
function VerticalLayout_Impl(){
  $clinit_RowLayout_Impl();
  RowLayout_Impl.call(this);
}

defineClass(439, 232, $intern_29, VerticalLayout_Impl);
_.computeIntrinsicSize = function computeIntrinsicSize_12(context, offsetX, offsetY){
  $computeIntrinsicSize_3(this, context, offsetX, offsetY);
}
;
_.distributeSize = function distributeSize_12(context, offsetX, offsetY, width_0, height){
  $distributeSize_4(this, context, offsetX, offsetY, width_0, height);
}
;
_.draw = function draw_25(out){
  $draw_6(this, out);
}
;
_.self_0 = function self_61(){
  return this;
}
;
_.self_2 = function self_62(){
  return this;
}
;
_.self_3 = function self_63(){
  return this;
}
;
_.setContents = function setContents_5(value_0){
  return $internalSetContents(this, value_0) , this;
}
;
_.setContents_0 = function setContents_6(value_0){
  return $internalSetContents(this, value_0) , this;
}
;
_.setCssClass_0 = function setCssClass_51(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_3 = function setCssClass_52(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass_4 = function setCssClass_53(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setCssClass = function setCssClass_54(value_0){
  return this._listener.beforeSet(this, 'cssClass', value_0) , this._cssClass = value_0 , this;
}
;
_.setFill_0 = function setFill_3(value_0){
  return $internalSetFill(this, value_0) , this;
}
;
_.setGap = function setGap_1(value_0){
  return this._listener.beforeSet(this, 'gap', value_0) , this._gap = value_0 , this;
}
;
_.setHeight_1 = function setHeight_30(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight_2 = function setHeight_31(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setHeight_3 = function setHeight_32(value_0){
  return this._listener.beforeSet(this, 'height', value_0) , this._height = value_0 , this;
}
;
_.setWidth_1 = function setWidth_30(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth_2 = function setWidth_31(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setWidth_3 = function setWidth_32(value_0){
  return this._listener.beforeSet(this, 'width', value_0) , this._width = value_0 , this;
}
;
_.setX = function setX_30(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_2 = function setX_31(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setX_3 = function setX_32(value_0){
  return this._listener.beforeSet(this, 'x', value_0) , this._x = value_0 , this;
}
;
_.setY = function setY_30(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_2 = function setY_31(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.setY_3 = function setY_32(value_0){
  return this._listener.beforeSet(this, 'y', value_0) , this._y = value_0 , this;
}
;
_.jsonType = function jsonType_15(){
  return 'VerticalLayout';
}
;
var Lcom_top_1logic_graphic_flow_data_impl_VerticalLayout_1Impl_2_classLit = createForClass('com.top_logic.graphic.flow.data.impl', 'VerticalLayout_Impl', 439);
function MapLike$lambda$0$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(216, 1, {}, MapLike$lambda$0$Type);
_.apply_0 = function apply_18(arg0){
  return $lambda$0_0(this.$$outer_0, castToString(arg0));
}
;
var Lcom_top_1logic_graphic_flow_operations_MapLike$lambda$0$Type_2_classLit = createForClass('com.top_logic.graphic.flow.operations', 'MapLike/lambda$0$Type', 216);
function MapLike$lambda$1$Type(value_0){
  this.value_0 = value_0;
}

defineClass(217, 1, {}, MapLike$lambda$1$Type);
_.test_0 = function test_3(arg0){
  return lambda$1_1(this.value_0, arg0);
}
;
var Lcom_top_1logic_graphic_flow_operations_MapLike$lambda$1$Type_2_classLit = createForClass('com.top_logic.graphic.flow.operations', 'MapLike/lambda$1$Type', 217);
function MapLike$lambda$2$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(218, 1, {}, MapLike$lambda$2$Type);
_.apply_0 = function apply_19(arg0){
  return $lambda$2_0(this.$$outer_0, castToString(arg0));
}
;
var Lcom_top_1logic_graphic_flow_operations_MapLike$lambda$2$Type_2_classLit = createForClass('com.top_logic.graphic.flow.operations', 'MapLike/lambda$2$Type', 218);
function MapLike$lambda$4$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(219, 1, {}, MapLike$lambda$4$Type);
_.apply_0 = function apply_20(arg0){
  return $lambda$4(this.$$outer_0, castToString(arg0));
}
;
var Lcom_top_1logic_graphic_flow_operations_MapLike$lambda$4$Type_2_classLit = createForClass('com.top_logic.graphic.flow.operations', 'MapLike/lambda$4$Type', 219);
function TreeLayoutOperations$0methodref$getAnchor$Type(){
}

defineClass(415, 1, {}, TreeLayoutOperations$0methodref$getAnchor$Type);
_.apply_0 = function apply_21(arg0){
  return castTo(arg0, 74)._anchor;
}
;
var Lcom_top_1logic_graphic_flow_operations_tree_TreeLayoutOperations$0methodref$getAnchor$Type_2_classLit = createForClass('com.top_logic.graphic.flow.operations.tree', 'TreeLayoutOperations/0methodref$getAnchor$Type', 415);
function TreeLayoutOperations$1methodref$get$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(416, 1, {}, TreeLayoutOperations$1methodref$get$Type);
_.apply_0 = function apply_22(arg0){
  return $get_6(this.$$outer_0, arg0);
}
;
var Lcom_top_1logic_graphic_flow_operations_tree_TreeLayoutOperations$1methodref$get$Type_2_classLit = createForClass('com.top_logic.graphic.flow.operations.tree', 'TreeLayoutOperations/1methodref$get$Type', 416);
function TreeLayoutOperations$lambda$2$Type(){
}

defineClass(417, 1, {}, TreeLayoutOperations$lambda$2$Type);
var Lcom_top_1logic_graphic_flow_operations_tree_TreeLayoutOperations$lambda$2$Type_2_classLit = createForClass('com.top_logic.graphic.flow.operations.tree', 'TreeLayoutOperations/lambda$2$Type', 417);
function doubleArray(dashes){
  var cnt, n, result;
  result = initUnidimensionalArray(D_classLit, $intern_4, 46, dashes.array.length, 15, 1);
  for (n = 0 , cnt = dashes.array.length; n < cnt; n++) {
    result[n] = $doubleValue((checkCriticalElementIndex(n, dashes.array.length) , castToDouble(dashes.array[n])));
  }
  return result;
}

function doubleList(size_0){
  var n, result;
  result = new ArrayList_0(size_0);
  for (n = 0; n < size_0; n++) {
    result.array.push(0);
  }
  return result;
}

defineClass(589, 1, {});
_.toString_0 = function toString_18(){
  var ex, out;
  out = new StringW;
  try {
    this.writeTo_1(new JsonWriter_0(out));
  }
   catch ($e0) {
    $e0 = toJava($e0);
    if (instanceOf($e0, 32)) {
      ex = $e0;
      throw toJs(new RuntimeException_2(ex));
    }
     else 
      throw toJs($e0);
  }
  return out._buffer.string;
}
;
var Lde_haumacher_msgbuf_data_AbstractDataObject_2_classLit = createForClass('de.haumacher.msgbuf.data', 'AbstractDataObject', 589);
function DefaultScope$ChangeExtractor(this$0){
  this.this$01 = this$0;
}

defineClass(301, 1, {}, DefaultScope$ChangeExtractor);
var Lde_haumacher_msgbuf_graph_DefaultScope$ChangeExtractor_2_classLit = createForClass('de.haumacher.msgbuf.graph', 'DefaultScope/ChangeExtractor', 301);
function DefaultScope$lambda$0$Type(){
}

defineClass(302, 1, {}, DefaultScope$lambda$0$Type);
_.apply_0 = function apply_23(arg0){
  return castTo(arg0, 30) , $clinit_DefaultScope() , new LinkedHashMap;
}
;
var Lde_haumacher_msgbuf_graph_DefaultScope$lambda$0$Type_2_classLit = createForClass('de.haumacher.msgbuf.graph', 'DefaultScope/lambda$0$Type', 302);
function DefaultScope$lambda$1$Type($$outer_0, json_1){
  this.$$outer_0 = $$outer_0;
  this.json_1 = json_1;
}

defineClass(303, 1, {}, DefaultScope$lambda$1$Type);
var Lde_haumacher_msgbuf_graph_DefaultScope$lambda$1$Type_2_classLit = createForClass('de.haumacher.msgbuf.graph', 'DefaultScope/lambda$1$Type', 303);
function DummyScope(){
  this._ids = new HashMap;
}

defineClass(395, 1, {}, DummyScope);
_.readData = function readData_0(node, id_0, in_0){
  throw toJs(new UnsupportedOperationException);
}
;
_.resolveOrFail = function resolveOrFail_0(id_0){
  throw toJs(new UnsupportedOperationException);
}
;
_.writeRefOrData = function writeRefOrData_0(out, node){
  var id_0;
  id_0 = castTo($get_6(this._ids, node), 64);
  if (!id_0) {
    id_0 = valueOf(this._nextId++);
    $put_0(this._ids, node, id_0);
    $writeData(node, this, out, id_0.value_0);
  }
   else {
    $value_3(out, id_0.value_0);
  }
}
;
_._nextId = 1;
var Lde_haumacher_msgbuf_graph_DummyScope_2_classLit = createForClass('de.haumacher.msgbuf.graph', 'DummyScope', 395);
function $writeFields_4(this$static, out){
  $name_1(out, 'id');
  $value_3(out, this$static._id);
  $name_1(out, 'p');
  $value_4(out, this$static._property);
}

function $writeTo_7(this$static, out){
  $writeDeferredName_0(out);
  $open_0(out, 1, '[');
  $value_4(out, this$static.jsonType());
  $writeDeferredName_0(out);
  $open_0(out, 3, '{');
  this$static.writeFields_0(out);
  $close_1(out, 3, 5, '}');
  $close_1(out, 1, 2, ']');
}

defineClass(72, 589, {72:1});
_.writeFields_0 = function writeFields_20(out){
  $writeFields_4(this, out);
}
;
_.writeTo_1 = function writeTo_4(out){
  $writeTo_7(this, out);
}
;
_._id = 0;
_._node = null;
_._property = '';
var Lde_haumacher_msgbuf_graph_cmd_Command_2_classLit = createForClass('de.haumacher.msgbuf.graph.cmd', 'Command', 72);
function $writeFields_5(this$static, out){
  $writeFields_4(this$static, out);
  $name_1(out, 'i');
  $value_3(out, this$static._index);
}

defineClass(78, 72, $intern_32);
_.setNext = function setNext(value_0){
  this._next = value_0;
  return this;
}
;
_.visit_0 = function visit_6(v, arg){
  return this.visit_1(v, arg);
}
;
_.writeFields_0 = function writeFields_21(out){
  $writeFields_5(this, out);
}
;
_._index = 0;
_._next = null;
var Lde_haumacher_msgbuf_graph_cmd_ListUpdate_2_classLit = createForClass('de.haumacher.msgbuf.graph.cmd', 'ListUpdate', 78);
function $setId_2(this$static, value_0){
  this$static._id = value_0;
  return this$static;
}

function $setIndex(this$static, value_0){
  this$static._index = value_0;
  return this$static;
}

function $setNode(this$static, value_0){
  this$static._node = value_0;
  return this$static;
}

function $setProperty(this$static, value_0){
  this$static._property = value_0;
  return this$static;
}

function InsertElement(){
}

defineClass(443, 78, $intern_32, InsertElement);
_.setNext = function setNext_0(value_0){
  return this._next = value_0 , this;
}
;
_.jsonType = function jsonType_16(){
  return 'I';
}
;
_.visit_1 = function visit_7(v, arg){
  return this._node.writeElement(v.this$01, arg, this._property, this._element) , null;
}
;
_.writeFields_0 = function writeFields_22(out){
  $writeFields_5(this, out);
}
;
_._element = null;
var Lde_haumacher_msgbuf_graph_cmd_InsertElement_2_classLit = createForClass('de.haumacher.msgbuf.graph.cmd', 'InsertElement', 443);
function $setId_3(this$static, value_0){
  this$static._id = value_0;
  return this$static;
}

function $setNode_0(this$static, value_0){
  this$static._node = value_0;
  return this$static;
}

function $setProperty_0(this$static, value_0){
  this$static._property = value_0;
  return this$static;
}

function RemoveElement(){
}

defineClass(444, 78, $intern_32, RemoveElement);
_.setNext = function setNext_1(value_0){
  return this._next = value_0 , this;
}
;
_.jsonType = function jsonType_17(){
  return 'R';
}
;
_.visit_1 = function visit_8(v, arg){
  return null;
}
;
var Lde_haumacher_msgbuf_graph_cmd_RemoveElement_2_classLit = createForClass('de.haumacher.msgbuf.graph.cmd', 'RemoveElement', 444);
function $setId_4(this$static, value_0){
  this$static._id = value_0;
  return this$static;
}

function $setNode_1(this$static, value_0){
  this$static._node = value_0;
  return this$static;
}

function $setProperty_1(this$static, value_0){
  this$static._property = value_0;
  return this$static;
}

function SetProperty(){
}

defineClass(135, 72, {72:1, 135:1}, SetProperty);
_.jsonType = function jsonType_18(){
  return 'S';
}
;
_.visit_0 = function visit_9(v, arg){
  var property, node;
  return property = this._property , node = this._node , node.writeFieldValue(v.this$01, arg, property) , null;
}
;
var Lde_haumacher_msgbuf_graph_cmd_SetProperty_2_classLit = createForClass('de.haumacher.msgbuf.graph.cmd', 'SetProperty', 135);
function $read_0(this$static, cbuf, off, len){
  var count, end;
  if (this$static._in.length == this$static._pos) {
    return -1;
  }
  count = $wnd.Math.min(len, this$static._in.length - this$static._pos);
  end = this$static._pos + count;
  $getChars(this$static._in, this$static._pos, end, cbuf, off);
  this$static._pos = end;
  return count;
}

function StringR_0(in_0){
  this._in = in_0;
}

defineClass(361, 1, {}, StringR_0);
_._pos = 0;
var Lde_haumacher_msgbuf_io_StringR_2_classLit = createForClass('de.haumacher.msgbuf.io', 'StringR', 361);
function $write_3(this$static, str){
  $append_7(this$static._buffer, str);
}

function $write_4(this$static, str, start_0, length_0){
  $append_6(this$static._buffer, str, start_0, start_0 + length_0);
}

function StringW(){
  this._buffer = new StringBuilder;
}

defineClass(179, 1, {}, StringW);
_.toString_0 = function toString_19(){
  return this._buffer.string;
}
;
var Lde_haumacher_msgbuf_io_StringW_2_classLit = createForClass('de.haumacher.msgbuf.io', 'StringW', 179);
function $clinit_JsonReader(){
  $clinit_JsonReader = emptyMethod;
  $toCharArray(")]}'\n");
}

function $beginArray_0(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  if (p == 3) {
    $push_0(this$static, 1);
    this$static.pathIndices[this$static.stackSize - 1] = 0;
    this$static.peeked = 0;
  }
   else {
    throw toJs(new IllegalStateException_0('Expected BEGIN_ARRAY but was ' + $peek_1(this$static) + $locationString_0(this$static)));
  }
}

function $beginObject_0(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  if (p == 1) {
    $push_0(this$static, 3);
    this$static.peeked = 0;
  }
   else {
    throw toJs(new IllegalStateException_0('Expected BEGIN_OBJECT but was ' + $peek_1(this$static) + $locationString_0(this$static)));
  }
}

function $checkLenient_0(this$static){
  throw toJs($syntaxError_0(this$static, 'Use JsonReader.setLenient(true) to accept malformed JSON'));
}

function $doPeek_0(this$static){
  var c, c0, c1, peekStack, result;
  peekStack = this$static.stack_0[this$static.stackSize - 1];
  if (peekStack == 1) {
    this$static.stack_0[this$static.stackSize - 1] = 2;
  }
   else if (peekStack == 2) {
    c0 = $nextNonWhitespace_0(this$static, true);
    switch (c0) {
      case 93:
        return this$static.peeked = 4;
      case 59:
        $checkLenient_0(this$static);
      case 44:
        break;
      default:throw toJs($syntaxError_0(this$static, 'Unterminated array'));
    }
  }
   else if (peekStack == 3 || peekStack == 5) {
    this$static.stack_0[this$static.stackSize - 1] = 4;
    if (peekStack == 5) {
      c0 = $nextNonWhitespace_0(this$static, true);
      switch (c0) {
        case 125:
          return this$static.peeked = 2;
        case 59:
          $checkLenient_0(this$static);
        case 44:
          break;
        default:throw toJs($syntaxError_0(this$static, 'Unterminated object'));
      }
    }
    c1 = $nextNonWhitespace_0(this$static, true);
    switch (c1) {
      case 34:
        return this$static.peeked = 13;
      case 39:
        $checkLenient_0(this$static);
        return this$static.peeked = 12;
      case 125:
        if (peekStack != 5) {
          return this$static.peeked = 2;
        }
         else {
          throw toJs($syntaxError_0(this$static, 'Expected name'));
        }

      default:$checkLenient_0(this$static);
        --this$static.pos;
        if ($isLiteral_0(this$static, c1 & $intern_6)) {
          return this$static.peeked = 14;
        }
         else {
          throw toJs($syntaxError_0(this$static, 'Expected name'));
        }

    }
  }
   else if (peekStack == 4) {
    this$static.stack_0[this$static.stackSize - 1] = 5;
    c0 = $nextNonWhitespace_0(this$static, true);
    switch (c0) {
      case 58:
        break;
      case 61:
        $checkLenient_0(this$static);
        (this$static.pos < this$static.limit || $fillBuffer_0(this$static, 1)) && this$static.buffer[this$static.pos] == 62 && ++this$static.pos;
        break;
      default:throw toJs($syntaxError_0(this$static, "Expected ':'"));
    }
  }
   else if (peekStack == 6) {
    this$static.stack_0[this$static.stackSize - 1] = 7;
  }
   else if (peekStack == 7) {
    c0 = $nextNonWhitespace_0(this$static, false);
    if (c0 == -1) {
      return this$static.peeked = 17;
    }
     else {
      $checkLenient_0(this$static);
      --this$static.pos;
    }
  }
   else if (peekStack == 8) {
    throw toJs(new IllegalStateException_0('JsonReader is closed'));
  }
  c = $nextNonWhitespace_0(this$static, true);
  switch (c) {
    case 93:
      if (peekStack == 1) {
        return this$static.peeked = 4;
      }

    case 59:
    case 44:
      if (peekStack == 1 || peekStack == 2) {
        $checkLenient_0(this$static);
        --this$static.pos;
        return this$static.peeked = 7;
      }
       else {
        throw toJs($syntaxError_0(this$static, 'Unexpected value'));
      }

    case 39:
      $checkLenient_0(this$static);
      return this$static.peeked = 8;
    case 34:
      return this$static.peeked = 9;
    case 91:
      return this$static.peeked = 3;
    case 123:
      return this$static.peeked = 1;
    default:--this$static.pos;
  }
  result = $peekKeyword_0(this$static);
  if (result != 0) {
    return result;
  }
  result = $peekNumber_0(this$static);
  if (result != 0) {
    return result;
  }
  if (!$isLiteral_0(this$static, this$static.buffer[this$static.pos])) {
    throw toJs($syntaxError_0(this$static, 'Expected value'));
  }
  $checkLenient_0(this$static);
  return this$static.peeked = 10;
}

function $endArray_0(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  if (p == 4) {
    --this$static.stackSize;
    ++this$static.pathIndices[this$static.stackSize - 1];
    this$static.peeked = 0;
  }
   else {
    throw toJs(new IllegalStateException_0('Expected END_ARRAY but was ' + $peek_1(this$static) + $locationString_0(this$static)));
  }
}

function $endObject_0(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  if (p == 2) {
    --this$static.stackSize;
    this$static.pathNames[this$static.stackSize] = null;
    ++this$static.pathIndices[this$static.stackSize - 1];
    this$static.peeked = 0;
  }
   else {
    throw toJs(new IllegalStateException_0('Expected END_OBJECT but was ' + $peek_1(this$static) + $locationString_0(this$static)));
  }
}

function $fillBuffer_0(this$static, minimum){
  var buffer, total;
  buffer = this$static.buffer;
  this$static.lineStart -= this$static.pos;
  if (this$static.limit != this$static.pos) {
    this$static.limit -= this$static.pos;
    arraycopy(buffer, this$static.pos, buffer, 0, this$static.limit);
  }
   else {
    this$static.limit = 0;
  }
  this$static.pos = 0;
  while ((total = $read_0(this$static.in_0, buffer, this$static.limit, buffer.length - this$static.limit)) != -1) {
    this$static.limit += total;
    if (this$static.lineNumber == 0 && this$static.lineStart == 0 && this$static.limit > 0 && buffer[0] == 65279) {
      ++this$static.pos;
      ++this$static.lineStart;
      ++minimum;
    }
    if (this$static.limit >= minimum) {
      return true;
    }
  }
  return false;
}

function $getPath_0(this$static){
  var i, result, size_0;
  result = $append_1(new StringBuilder, 36);
  for (i = 0 , size_0 = this$static.stackSize; i < size_0; i++) {
    switch (this$static.stack_0[i]) {
      case 1:
      case 2:
        $append_1($append_2((result.string += '[' , result), this$static.pathIndices[i]), 93);
        break;
      case 3:
      case 4:
      case 5:
        result.string += '.';
        this$static.pathNames[i] != null && $append_7(result, this$static.pathNames[i]);
    }
  }
  return result.string;
}

function $hasNext_0(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  return p != 2 && p != 4;
}

function $isLiteral_0(this$static, c){
  switch (c) {
    case 47:
    case 92:
    case 59:
    case 35:
    case 61:
      $checkLenient_0(this$static);
    case 123:
    case 125:
    case 91:
    case 93:
    case 58:
    case 44:
    case 32:
    case 9:
    case 12:
    case 13:
    case 10:
      return false;
    default:return true;
  }
}

function $locationString_0(this$static){
  var column, line;
  line = this$static.lineNumber + 1;
  column = this$static.pos - this$static.lineStart + 1;
  return ' at line ' + line + ' column ' + column + ' path ' + $getPath_0(this$static);
}

function $nextBoolean_0(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  if (p == 5) {
    this$static.peeked = 0;
    ++this$static.pathIndices[this$static.stackSize - 1];
    return true;
  }
   else if (p == 6) {
    this$static.peeked = 0;
    ++this$static.pathIndices[this$static.stackSize - 1];
    return false;
  }
  throw toJs(new IllegalStateException_0('Expected a boolean but was ' + $peek_1(this$static) + $locationString_0(this$static)));
}

function $nextDouble_0(this$static){
  var p, result;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  if (p == 15) {
    this$static.peeked = 0;
    ++this$static.pathIndices[this$static.stackSize - 1];
    return toDouble_0(this$static.peekedLong);
  }
  if (p == 16) {
    this$static.peekedString = valueOf_1(this$static.buffer, this$static.pos, this$static.peekedNumberLength);
    this$static.pos += this$static.peekedNumberLength;
  }
   else if (p == 8 || p == 9) {
    this$static.peekedString = $nextQuotedValue_0(this$static, p == 8?39:34);
  }
   else if (p == 10) {
    this$static.peekedString = $nextUnquotedValue_0(this$static);
  }
   else if (p != 11) {
    throw toJs(new IllegalStateException_0('Expected a double but was ' + $peek_1(this$static) + $locationString_0(this$static)));
  }
  this$static.peeked = 11;
  result = __parseAndValidateDouble(this$static.peekedString);
  if (isNaN(result) || !isNaN(result) && !isFinite(result)) {
    throw toJs(new MalformedJsonException_0('JSON forbids NaN and infinities: ' + result + $locationString_0(this$static)));
  }
  this$static.peekedString = null;
  this$static.peeked = 0;
  ++this$static.pathIndices[this$static.stackSize - 1];
  return result;
}

function $nextInt(this$static){
  var asDouble, p, result;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  if (p == 15) {
    result = toInt_0(this$static.peekedLong);
    if (neq(this$static.peekedLong, result)) {
      throw toJs(new NumberFormatException('Expected an int but was ' + toString_7(this$static.peekedLong) + $locationString_0(this$static)));
    }
    this$static.peeked = 0;
    ++this$static.pathIndices[this$static.stackSize - 1];
    return result;
  }
  if (p == 16) {
    this$static.peekedString = valueOf_1(this$static.buffer, this$static.pos, this$static.peekedNumberLength);
    this$static.pos += this$static.peekedNumberLength;
  }
   else if (p == 8 || p == 9 || p == 10) {
    p == 10?(this$static.peekedString = $nextUnquotedValue_0(this$static)):(this$static.peekedString = $nextQuotedValue_0(this$static, p == 8?39:34));
    try {
      result = __parseAndValidateInt(this$static.peekedString);
      this$static.peeked = 0;
      ++this$static.pathIndices[this$static.stackSize - 1];
      return result;
    }
     catch ($e0) {
      $e0 = toJava($e0);
      if (!instanceOf($e0, 59))
        throw toJs($e0);
    }
  }
   else {
    throw toJs(new IllegalStateException_0('Expected an int but was ' + $peek_1(this$static) + $locationString_0(this$static)));
  }
  this$static.peeked = 11;
  asDouble = __parseAndValidateDouble(this$static.peekedString);
  result = round_int(asDouble);
  if (result != asDouble) {
    throw toJs(new NumberFormatException('Expected an int but was ' + this$static.peekedString + $locationString_0(this$static)));
  }
  this$static.peekedString = null;
  this$static.peeked = 0;
  ++this$static.pathIndices[this$static.stackSize - 1];
  return result;
}

function $nextName_0(this$static){
  var p, result;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  if (p == 14) {
    result = $nextUnquotedValue_0(this$static);
  }
   else if (p == 12) {
    result = $nextQuotedValue_0(this$static, 39);
  }
   else if (p == 13) {
    result = $nextQuotedValue_0(this$static, 34);
  }
   else {
    throw toJs(new IllegalStateException_0('Expected a name but was ' + $peek_1(this$static) + $locationString_0(this$static)));
  }
  this$static.peeked = 0;
  this$static.pathNames[this$static.stackSize - 1] = result;
  return result;
}

function $nextNonWhitespace_0(this$static, throwOnEof){
  var buffer, c, charsLoaded, l, p, peek;
  buffer = this$static.buffer;
  p = this$static.pos;
  l = this$static.limit;
  while (true) {
    if (p == l) {
      this$static.pos = p;
      if (!$fillBuffer_0(this$static, 1)) {
        break;
      }
      p = this$static.pos;
      l = this$static.limit;
    }
    c = buffer[p++];
    if (c == 10) {
      ++this$static.lineNumber;
      this$static.lineStart = p;
      continue;
    }
     else if (c == 32 || c == 13 || c == 9) {
      continue;
    }
    if (c == 47) {
      this$static.pos = p;
      if (p == l) {
        --this$static.pos;
        charsLoaded = $fillBuffer_0(this$static, 2);
        ++this$static.pos;
        if (!charsLoaded) {
          return c;
        }
      }
      $checkLenient_0(this$static);
      peek = buffer[this$static.pos];
      switch (peek) {
        case 42:
          ++this$static.pos;
          if (!$skipTo_0(this$static)) {
            throw toJs($syntaxError_0(this$static, 'Unterminated comment'));
          }

          p = this$static.pos + 2;
          l = this$static.limit;
          continue;
        case 47:
          ++this$static.pos;
          $skipToEndOfLine_0(this$static);
          p = this$static.pos;
          l = this$static.limit;
          continue;
        default:return c;
      }
    }
     else if (c == 35) {
      this$static.pos = p;
      $checkLenient_0(this$static);
      $skipToEndOfLine_0(this$static);
      p = this$static.pos;
      l = this$static.limit;
    }
     else {
      this$static.pos = p;
      return c;
    }
  }
  if (throwOnEof) {
    throw toJs(new IOException('End of input' + $locationString_0(this$static)));
  }
   else {
    return -1;
  }
}

function $nextNull_0(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  if (p == 7) {
    this$static.peeked = 0;
    ++this$static.pathIndices[this$static.stackSize - 1];
  }
   else {
    throw toJs(new IllegalStateException_0('Expected null but was ' + $peek_1(this$static) + $locationString_0(this$static)));
  }
}

function $nextQuotedValue_0(this$static, quote_0){
  var buffer, builder, c, estimatedLength, l, len, p, start_0;
  buffer = this$static.buffer;
  builder = null;
  while (true) {
    p = this$static.pos;
    l = this$static.limit;
    start_0 = p;
    while (p < l) {
      c = buffer[p++];
      if (c == quote_0) {
        this$static.pos = p;
        len = p - start_0 - 1;
        if (!builder) {
          return valueOf_1(buffer, start_0, len);
        }
         else {
          builder.string += valueOf_1(buffer, start_0, len);
          return builder.string;
        }
      }
       else if (c == 92) {
        this$static.pos = p;
        len = p - start_0 - 1;
        if (!builder) {
          estimatedLength = (len + 1) * 2;
          builder = ($wnd.Math.max(estimatedLength, 16) , new StringBuilder_0);
        }
        builder.string += valueOf_1(buffer, start_0, len);
        $append_1(builder, $readEscapeCharacter_0(this$static));
        p = this$static.pos;
        l = this$static.limit;
        start_0 = p;
      }
       else if (c == 10) {
        ++this$static.lineNumber;
        this$static.lineStart = p;
      }
    }
    if (!builder) {
      estimatedLength = (p - start_0) * 2;
      builder = ($wnd.Math.max(estimatedLength, 16) , new StringBuilder_0);
    }
    builder.string += valueOf_1(buffer, start_0, p - start_0);
    this$static.pos = p;
    if (!$fillBuffer_0(this$static, 1)) {
      throw toJs($syntaxError_0(this$static, 'Unterminated string'));
    }
  }
}

function $nextString_0(this$static){
  var p, result;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  if (p == 10) {
    result = $nextUnquotedValue_0(this$static);
  }
   else if (p == 8) {
    result = $nextQuotedValue_0(this$static, 39);
  }
   else if (p == 9) {
    result = $nextQuotedValue_0(this$static, 34);
  }
   else if (p == 11) {
    result = this$static.peekedString;
    this$static.peekedString = null;
  }
   else if (p == 15) {
    result = '' + toString_7(this$static.peekedLong);
  }
   else if (p == 16) {
    result = valueOf_1(this$static.buffer, this$static.pos, this$static.peekedNumberLength);
    this$static.pos += this$static.peekedNumberLength;
  }
   else {
    throw toJs(new IllegalStateException_0('Expected a string but was ' + $peek_1(this$static) + $locationString_0(this$static)));
  }
  this$static.peeked = 0;
  ++this$static.pathIndices[this$static.stackSize - 1];
  return result;
}

function $nextUnquotedValue_0(this$static){
  var builder, i, result;
  builder = null;
  i = 0;
  findNonLiteralCharacter: while (true) {
    for (; this$static.pos + i < this$static.limit; i++) {
      switch (this$static.buffer[this$static.pos + i]) {
        case 47:
        case 92:
        case 59:
        case 35:
        case 61:
          $checkLenient_0(this$static);
        case 123:
        case 125:
        case 91:
        case 93:
        case 58:
        case 44:
        case 32:
        case 9:
        case 12:
        case 13:
        case 10:
          break findNonLiteralCharacter;
      }
    }
    if (i < this$static.buffer.length) {
      if ($fillBuffer_0(this$static, i + 1)) {
        continue;
      }
       else {
        break;
      }
    }
    !builder && (builder = ($wnd.Math.max(i, 16) , new StringBuilder_0));
    $append_8(builder, this$static.buffer, this$static.pos, i);
    this$static.pos += i;
    i = 0;
    if (!$fillBuffer_0(this$static, 1)) {
      break;
    }
  }
  result = !builder?valueOf_1(this$static.buffer, this$static.pos, i):$append_8(builder, this$static.buffer, this$static.pos, i).string;
  this$static.pos += i;
  return result;
}

function $peek_1(this$static){
  var p;
  p = this$static.peeked;
  p == 0 && (p = $doPeek_0(this$static));
  switch (p) {
    case 1:
      return $clinit_JsonToken_0() , BEGIN_OBJECT_0;
    case 2:
      return $clinit_JsonToken_0() , END_OBJECT_0;
    case 3:
      return $clinit_JsonToken_0() , BEGIN_ARRAY_0;
    case 4:
      return $clinit_JsonToken_0() , END_ARRAY_0;
    case 12:
    case 13:
    case 14:
      return $clinit_JsonToken_0() , NAME_0;
    case 5:
    case 6:
      return $clinit_JsonToken_0() , BOOLEAN_0;
    case 7:
      return $clinit_JsonToken_0() , NULL_0;
    case 8:
    case 9:
    case 10:
    case 11:
      return $clinit_JsonToken_0() , STRING_0;
    case 15:
    case 16:
      return $clinit_JsonToken_0() , NUMBER_0;
    case 17:
      return $clinit_JsonToken_0() , END_DOCUMENT_0;
    default:throw toJs(new AssertionError);
  }
}

function $peekKeyword_0(this$static){
  var c, i, keyword, keywordUpper, length_0, peeking;
  c = this$static.buffer[this$static.pos];
  if (c == 116 || c == 84) {
    keyword = 'true';
    keywordUpper = 'TRUE';
    peeking = 5;
  }
   else if (c == 102 || c == 70) {
    keyword = 'false';
    keywordUpper = 'FALSE';
    peeking = 6;
  }
   else if (c == 110 || c == 78) {
    keyword = 'null';
    keywordUpper = 'NULL';
    peeking = 7;
  }
   else {
    return 0;
  }
  length_0 = keyword.length;
  for (i = 1; i < length_0; i++) {
    if (this$static.pos + i >= this$static.limit && !$fillBuffer_0(this$static, i + 1)) {
      return 0;
    }
    c = this$static.buffer[this$static.pos + i];
    if (c != (checkCriticalStringElementIndex(i, keyword.length) , keyword.charCodeAt(i)) && c != (checkCriticalStringElementIndex(i, keywordUpper.length) , keywordUpper.charCodeAt(i))) {
      return 0;
    }
  }
  if ((this$static.pos + length_0 < this$static.limit || $fillBuffer_0(this$static, length_0 + 1)) && $isLiteral_0(this$static, this$static.buffer[this$static.pos + length_0])) {
    return 0;
  }
  this$static.pos += length_0;
  return this$static.peeked = peeking;
}

function $peekNumber_0(this$static){
  var buffer, c, fitsInLong, i, l, last, negative, newValue, p, value_0;
  buffer = this$static.buffer;
  p = this$static.pos;
  l = this$static.limit;
  value_0 = 0;
  negative = false;
  fitsInLong = true;
  last = 0;
  i = 0;
  charactersOfNumber: for (; true; i++) {
    if (p + i == l) {
      if (i == buffer.length) {
        return 0;
      }
      if (!$fillBuffer_0(this$static, i + 1)) {
        break;
      }
      p = this$static.pos;
      l = this$static.limit;
    }
    c = buffer[p + i];
    switch (c) {
      case 45:
        if (last == 0) {
          negative = true;
          last = 1;
          continue;
        }
         else if (last == 5) {
          last = 6;
          continue;
        }

        return 0;
      case 43:
        if (last == 5) {
          last = 6;
          continue;
        }

        return 0;
      case 101:
      case 69:
        if (last == 2 || last == 4) {
          last = 5;
          continue;
        }

        return 0;
      case 46:
        if (last == 2) {
          last = 3;
          continue;
        }

        return 0;
      default:if (c < 48 || c > 57) {
          if (!$isLiteral_0(this$static, c)) {
            break charactersOfNumber;
          }
          return 0;
        }

        if (last == 1 || last == 0) {
          value_0 = -(c - 48);
          last = 2;
        }
         else if (last == 2) {
          if (compare_0(value_0, 0) == 0) {
            return 0;
          }
          newValue = sub_1(mul_0(value_0, 10), c - 48);
          fitsInLong = fitsInLong & (gt(value_0, $intern_14) || eq(value_0, $intern_14) && compare_0(newValue, value_0) < 0);
          value_0 = newValue;
        }
         else 
          last == 3?(last = 4):(last == 5 || last == 6) && (last = 7);
    }
  }
  if (last == 2 && fitsInLong && (neq(value_0, $intern_15) || negative) && (compare_0(value_0, 0) != 0 || !negative)) {
    this$static.peekedLong = negative?value_0:neg_0(value_0);
    this$static.pos += i;
    return this$static.peeked = 15;
  }
   else if (last == 2 || last == 4 || last == 7) {
    this$static.peekedNumberLength = i;
    return this$static.peeked = 16;
  }
   else {
    return 0;
  }
}

function $push_0(this$static, newTop){
  var newPathIndices, newPathNames, newStack;
  if (this$static.stackSize == this$static.stack_0.length) {
    newStack = initUnidimensionalArray(I_classLit, $intern_4, 46, this$static.stackSize * 2, 15, 1);
    newPathIndices = initUnidimensionalArray(I_classLit, $intern_4, 46, this$static.stackSize * 2, 15, 1);
    newPathNames = initUnidimensionalArray(Ljava_lang_String_2_classLit, $intern_3, 2, this$static.stackSize * 2, 6, 1);
    arraycopy(this$static.stack_0, 0, newStack, 0, this$static.stackSize);
    arraycopy(this$static.pathIndices, 0, newPathIndices, 0, this$static.stackSize);
    arraycopy(this$static.pathNames, 0, newPathNames, 0, this$static.stackSize);
    this$static.stack_0 = newStack;
    this$static.pathIndices = newPathIndices;
    this$static.pathNames = newPathNames;
  }
  this$static.stack_0[this$static.stackSize++] = newTop;
}

function $readEscapeCharacter_0(this$static){
  var c, end, escaped, i, result;
  if (this$static.pos == this$static.limit && !$fillBuffer_0(this$static, 1)) {
    throw toJs($syntaxError_0(this$static, 'Unterminated escape sequence'));
  }
  escaped = this$static.buffer[this$static.pos++];
  switch (escaped) {
    case 117:
      if (this$static.pos + 4 > this$static.limit && !$fillBuffer_0(this$static, 4)) {
        throw toJs($syntaxError_0(this$static, 'Unterminated escape sequence'));
      }

      result = 0;
      for (i = this$static.pos , end = i + 4; i < end; i++) {
        c = this$static.buffer[i];
        result = result << 4 & $intern_6;
        if (c >= 48 && c <= 57) {
          result = result + (c - 48) & $intern_6;
        }
         else if (c >= 97 && c <= 102) {
          result = result + (c - 97 + 10) & $intern_6;
        }
         else if (c >= 65 && c <= 70) {
          result = result + (c - 65 + 10) & $intern_6;
        }
         else {
          throw toJs(new NumberFormatException('\\u' + valueOf_1(this$static.buffer, this$static.pos, 4)));
        }
      }

      this$static.pos += 4;
      return result;
    case 116:
      return 9;
    case 98:
      return 8;
    case 110:
      return 10;
    case 114:
      return 13;
    case 102:
      return 12;
    case 10:
      ++this$static.lineNumber;
      this$static.lineStart = this$static.pos;
    case 39:
    case 34:
    case 92:
    case 47:
      return escaped;
    default:throw toJs($syntaxError_0(this$static, 'Invalid escape sequence'));
  }
}

function $skipQuotedValue(this$static, quote_0){
  var buffer, c, l, p;
  buffer = this$static.buffer;
  do {
    p = this$static.pos;
    l = this$static.limit;
    while (p < l) {
      c = buffer[p++];
      if (c == quote_0) {
        this$static.pos = p;
        return;
      }
       else if (c == 92) {
        this$static.pos = p;
        $readEscapeCharacter_0(this$static);
        p = this$static.pos;
        l = this$static.limit;
      }
       else if (c == 10) {
        ++this$static.lineNumber;
        this$static.lineStart = p;
      }
    }
    this$static.pos = p;
  }
   while ($fillBuffer_0(this$static, 1));
  throw toJs($syntaxError_0(this$static, 'Unterminated string'));
}

function $skipTo_0(this$static){
  var c, length_0;
  length_0 = '*/'.length;
  outer: for (; this$static.pos + length_0 <= this$static.limit || $fillBuffer_0(this$static, length_0); this$static.pos++) {
    if (this$static.buffer[this$static.pos] == 10) {
      ++this$static.lineNumber;
      this$static.lineStart = this$static.pos + 1;
      continue;
    }
    for (c = 0; c < length_0; c++) {
      if (this$static.buffer[this$static.pos + c] != (checkCriticalStringElementIndex(c, '*/'.length) , '*/'.charCodeAt(c))) {
        continue outer;
      }
    }
    return true;
  }
  return false;
}

function $skipToEndOfLine_0(this$static){
  var c;
  while (this$static.pos < this$static.limit || $fillBuffer_0(this$static, 1)) {
    c = this$static.buffer[this$static.pos++];
    if (c == 10) {
      ++this$static.lineNumber;
      this$static.lineStart = this$static.pos;
      break;
    }
     else if (c == 13) {
      break;
    }
  }
}

function $skipUnquotedValue(this$static){
  var i;
  do {
    i = 0;
    for (; this$static.pos + i < this$static.limit; i++) {
      switch (this$static.buffer[this$static.pos + i]) {
        case 47:
        case 92:
        case 59:
        case 35:
        case 61:
          $checkLenient_0(this$static);
        case 123:
        case 125:
        case 91:
        case 93:
        case 58:
        case 44:
        case 32:
        case 9:
        case 12:
        case 13:
        case 10:
          this$static.pos += i;
          return;
      }
    }
    this$static.pos += i;
  }
   while ($fillBuffer_0(this$static, 1));
}

function $skipValue(this$static){
  var count, p;
  count = 0;
  do {
    p = this$static.peeked;
    p == 0 && (p = $doPeek_0(this$static));
    if (p == 3) {
      $push_0(this$static, 1);
      ++count;
    }
     else if (p == 1) {
      $push_0(this$static, 3);
      ++count;
    }
     else if (p == 4) {
      --this$static.stackSize;
      --count;
    }
     else if (p == 2) {
      --this$static.stackSize;
      --count;
    }
     else 
      p == 14 || p == 10?$skipUnquotedValue(this$static):p == 8 || p == 12?$skipQuotedValue(this$static, 39):p == 9 || p == 13?$skipQuotedValue(this$static, 34):p == 16 && (this$static.pos += this$static.peekedNumberLength);
    this$static.peeked = 0;
  }
   while (count != 0);
  ++this$static.pathIndices[this$static.stackSize - 1];
  this$static.pathNames[this$static.stackSize - 1] = 'null';
}

function $syntaxError_0(this$static, message){
  throw toJs(new MalformedJsonException_0(message + $locationString_0(this$static)));
}

function JsonReader_0(in_0){
  $clinit_JsonReader();
  this.buffer = initUnidimensionalArray(C_classLit, $intern_4, 46, 1024, 15, 1);
  this.stack_0 = initUnidimensionalArray(I_classLit, $intern_4, 46, 32, 15, 1);
  this.stack_0[this.stackSize++] = 6;
  this.pathNames = initUnidimensionalArray(Ljava_lang_String_2_classLit, $intern_3, 2, 32, 6, 1);
  this.pathIndices = initUnidimensionalArray(I_classLit, $intern_4, 46, 32, 15, 1);
  this.in_0 = in_0;
}

defineClass(360, 1, {}, JsonReader_0);
_.close_0 = function close_7(){
  this.peeked = 0;
  this.stack_0[0] = 8;
  this.stackSize = 1;
}
;
_.toString_0 = function toString_20(){
  return $ensureNamesAreInitialized(Lde_haumacher_msgbuf_json_JsonReader_2_classLit) , Lde_haumacher_msgbuf_json_JsonReader_2_classLit.simpleName + $locationString_0(this);
}
;
_.limit = 0;
_.lineNumber = 0;
_.lineStart = 0;
_.peeked = 0;
_.peekedLong = 0;
_.peekedNumberLength = 0;
_.pos = 0;
_.stackSize = 0;
var Lde_haumacher_msgbuf_json_JsonReader_2_classLit = createForClass('de.haumacher.msgbuf.json', 'JsonReader', 360);
function $clinit_JsonToken_0(){
  $clinit_JsonToken_0 = emptyMethod;
  BEGIN_ARRAY_0 = new JsonToken_0('BEGIN_ARRAY', 0);
  END_ARRAY_0 = new JsonToken_0('END_ARRAY', 1);
  BEGIN_OBJECT_0 = new JsonToken_0('BEGIN_OBJECT', 2);
  END_OBJECT_0 = new JsonToken_0('END_OBJECT', 3);
  NAME_0 = new JsonToken_0('NAME', 4);
  STRING_0 = new JsonToken_0('STRING', 5);
  NUMBER_0 = new JsonToken_0('NUMBER', 6);
  BOOLEAN_0 = new JsonToken_0('BOOLEAN', 7);
  NULL_0 = new JsonToken_0('NULL', 8);
  END_DOCUMENT_0 = new JsonToken_0('END_DOCUMENT', 9);
}

function JsonToken_0(enum$name, enum$ordinal){
  Enum.call(this, enum$name, enum$ordinal);
}

function values_11(){
  $clinit_JsonToken_0();
  return stampJavaTypeInfo(getClassLiteralForArray(Lde_haumacher_msgbuf_json_JsonToken_2_classLit, 1), $intern_0, 56, 0, [BEGIN_ARRAY_0, END_ARRAY_0, BEGIN_OBJECT_0, END_OBJECT_0, NAME_0, STRING_0, NUMBER_0, BOOLEAN_0, NULL_0, END_DOCUMENT_0]);
}

defineClass(56, 29, {56:1, 3:1, 33:1, 29:1}, JsonToken_0);
var BEGIN_ARRAY_0, BEGIN_OBJECT_0, BOOLEAN_0, END_ARRAY_0, END_DOCUMENT_0, END_OBJECT_0, NAME_0, NULL_0, NUMBER_0, STRING_0;
var Lde_haumacher_msgbuf_json_JsonToken_2_classLit = createForEnum('de.haumacher.msgbuf.json', 'JsonToken', 56, values_11);
function nextStringOptional(in_0){
  if ($peek_1(in_0) == ($clinit_JsonToken_0() , NULL_0)) {
    $nextNull_0(in_0);
    return null;
  }
  return $nextString_0(in_0);
}

function $clinit_JsonWriter_0(){
  $clinit_JsonWriter_0 = emptyMethod;
  var hex, i, number;
  REPLACEMENT_CHARS_0 = initUnidimensionalArray(Ljava_lang_String_2_classLit, $intern_3, 2, 128, 6, 1);
  for (i = 0; i <= 31; i++) {
    hex = (number = i >>> 0 , number.toString(16));
    hex = $substring_0('0000', 0, 4 - hex.length) + ('' + hex);
    REPLACEMENT_CHARS_0[i] = '\\u' + hex;
  }
  REPLACEMENT_CHARS_0[34] = '\\"';
  REPLACEMENT_CHARS_0[92] = '\\\\';
  REPLACEMENT_CHARS_0[9] = '\\t';
  REPLACEMENT_CHARS_0[8] = '\\b';
  REPLACEMENT_CHARS_0[10] = '\\n';
  REPLACEMENT_CHARS_0[13] = '\\r';
  REPLACEMENT_CHARS_0[12] = '\\f';
  HTML_SAFE_REPLACEMENT_CHARS_0 = clone(REPLACEMENT_CHARS_0);
  HTML_SAFE_REPLACEMENT_CHARS_0[60] = '\\u003c';
  HTML_SAFE_REPLACEMENT_CHARS_0[62] = '\\u003e';
  HTML_SAFE_REPLACEMENT_CHARS_0[38] = '\\u0026';
  HTML_SAFE_REPLACEMENT_CHARS_0[61] = '\\u003d';
  HTML_SAFE_REPLACEMENT_CHARS_0[39] = '\\u0027';
}

function $beforeName_0(this$static){
  var context;
  context = $peek_2(this$static);
  if (context == 5) {
    $append_1(this$static.out._buffer, 44);
  }
   else if (context != 3) {
    throw toJs(new IllegalStateException_0('Nesting problem.'));
  }
  this$static.stack_0[this$static.stackSize - 1] = 4;
}

function $beforeValue_0(this$static){
  switch ($peek_2(this$static)) {
    case 7:
      if (!this$static.lenient) {
        throw toJs(new IllegalStateException_0('JSON must have only one top-level value.'));
      }

    case 6:
      this$static.stack_0[this$static.stackSize - 1] = 7;
      break;
    case 1:
      this$static.stack_0[this$static.stackSize - 1] = 2;
      break;
    case 2:
      $append_1(this$static.out._buffer, 44);
      break;
    case 4:
      $write_3(this$static.out, this$static.separator);
      this$static.stack_0[this$static.stackSize - 1] = 5;
      break;
    default:throw toJs(new IllegalStateException_0('Nesting problem.'));
  }
}

function $close_1(this$static, empty, nonempty, closeBracket){
  var context;
  context = $peek_2(this$static);
  if (context != nonempty && context != empty) {
    throw toJs(new IllegalStateException_0('Nesting problem.'));
  }
  if (this$static.deferredName != null) {
    throw toJs(new IllegalStateException_0('Dangling name: ' + this$static.deferredName));
  }
  --this$static.stackSize;
  $write_3(this$static.out, closeBracket);
  return this$static;
}

function $name_1(this$static, name_0){
  if (this$static.deferredName != null) {
    throw toJs(new IllegalStateException);
  }
  if (this$static.stackSize == 0) {
    throw toJs(new IllegalStateException_0('JsonWriter is closed.'));
  }
  this$static.deferredName = name_0;
  return this$static;
}

function $nullValue_0(this$static){
  if (this$static.deferredName != null) {
    if (this$static.serializeNulls) {
      $writeDeferredName_0(this$static);
    }
     else {
      this$static.deferredName = null;
      return this$static;
    }
  }
  $beforeValue_0(this$static);
  $write_3(this$static.out, 'null');
  return this$static;
}

function $open_0(this$static, empty, openBracket){
  $beforeValue_0(this$static);
  $push_1(this$static, empty);
  $write_3(this$static.out, openBracket);
  return this$static;
}

function $peek_2(this$static){
  if (this$static.stackSize == 0) {
    throw toJs(new IllegalStateException_0('JsonWriter is closed.'));
  }
  return this$static.stack_0[this$static.stackSize - 1];
}

function $push_1(this$static, newTop){
  var newStack;
  if (this$static.stackSize == this$static.stack_0.length) {
    newStack = initUnidimensionalArray(I_classLit, $intern_4, 46, this$static.stackSize * 2, 15, 1);
    arraycopy(this$static.stack_0, 0, newStack, 0, this$static.stackSize);
    this$static.stack_0 = newStack;
  }
  this$static.stack_0[this$static.stackSize++] = newTop;
}

function $string_0(this$static, value_0){
  var c, i, last, length_0, replacement, replacements;
  replacements = this$static.htmlSafe?HTML_SAFE_REPLACEMENT_CHARS_0:REPLACEMENT_CHARS_0;
  $write_3(this$static.out, '"');
  last = 0;
  length_0 = value_0.length;
  for (i = 0; i < length_0; i++) {
    c = (checkCriticalStringElementIndex(i, value_0.length) , value_0.charCodeAt(i));
    if (c < 128) {
      replacement = replacements[c];
      if (replacement == null) {
        continue;
      }
    }
     else if (c == 8232) {
      replacement = '\\u2028';
    }
     else if (c == 8233) {
      replacement = '\\u2029';
    }
     else {
      continue;
    }
    last < i && $write_4(this$static.out, value_0, last, i - last);
    $write_3(this$static.out, replacement);
    last = i + 1;
  }
  last < length_0 && $write_4(this$static.out, value_0, last, length_0 - last);
  $write_3(this$static.out, '"');
}

function $value_2(this$static, value_0){
  $writeDeferredName_0(this$static);
  if (!this$static.lenient && (isNaN(value_0) || !isNaN(value_0) && !isFinite(value_0))) {
    throw toJs(new IllegalArgumentException('Numeric values must be finite, but was ' + value_0));
  }
  $beforeValue_0(this$static);
  $write_3(this$static.out, '' + value_0);
  return this$static;
}

function $value_3(this$static, value_0){
  $writeDeferredName_0(this$static);
  $beforeValue_0(this$static);
  $write_3(this$static.out, '' + toString_7(value_0));
  return this$static;
}

function $value_4(this$static, value_0){
  if (value_0 == null) {
    return $nullValue_0(this$static);
  }
  $writeDeferredName_0(this$static);
  $beforeValue_0(this$static);
  $string_0(this$static, value_0);
  return this$static;
}

function $value_5(this$static, value_0){
  $writeDeferredName_0(this$static);
  $beforeValue_0(this$static);
  $write_3(this$static.out, value_0?'true':'false');
  return this$static;
}

function $writeDeferredName_0(this$static){
  if (this$static.deferredName != null) {
    $beforeName_0(this$static);
    $string_0(this$static, this$static.deferredName);
    this$static.deferredName = null;
  }
}

function JsonWriter_0(out){
  $clinit_JsonWriter_0();
  this.stack_0 = initUnidimensionalArray(I_classLit, $intern_4, 46, 32, 15, 1);
  $push_1(this, 6);
  this.out = out;
}

function clone(s){
  var result;
  result = initUnidimensionalArray(Ljava_lang_String_2_classLit, $intern_3, 2, s.length, 6, 1);
  arraycopy(s, 0, result, 0, s.length);
  return result;
}

defineClass(180, 1, {}, JsonWriter_0);
_.close_0 = function close_8(){
  var size_0;
  size_0 = this.stackSize;
  if (size_0 > 1 || size_0 == 1 && this.stack_0[size_0 - 1] != 7) {
    throw toJs(new IOException('Incomplete document'));
  }
  this.stackSize = 0;
}
;
_.htmlSafe = false;
_.lenient = false;
_.separator = ':';
_.serializeNulls = true;
_.stackSize = 0;
var HTML_SAFE_REPLACEMENT_CHARS_0, REPLACEMENT_CHARS_0;
var Lde_haumacher_msgbuf_json_JsonWriter_2_classLit = createForClass('de.haumacher.msgbuf.json', 'JsonWriter', 180);
function MalformedJsonException_0(msg){
  IOException.call(this, msg);
}

defineClass(226, 32, $intern_16, MalformedJsonException_0);
var Lde_haumacher_msgbuf_json_MalformedJsonException_2_classLit = createForClass('de.haumacher.msgbuf.json', 'MalformedJsonException', 226);
function $clinit_Listener$1(){
  $clinit_Listener$1 = emptyMethod;
  $clinit_Listener();
}

function Listener$1(){
  $clinit_Listener$1();
}

defineClass(304, 1, {139:1}, Listener$1);
_.afterRemove = function afterRemove_8(obj, property, index_0, element){
}
;
_.beforeAdd = function beforeAdd_8(obj, property, index_0, element){
}
;
_.beforeSet = function beforeSet_0(obj, property, value_0){
}
;
var Lde_haumacher_msgbuf_observer_Listener$1_2_classLit = createForClass('de.haumacher.msgbuf.observer', 'Listener/1', 304);
function $clinit_Listener$MultiplexListener(){
  $clinit_Listener$MultiplexListener = emptyMethod;
  $clinit_Listener();
}

function $register(this$static, l){
  $indexOf_0(this$static, l, 0) != -1 || (push_1(this$static.array, l) , true);
  return this$static;
}

function Listener$MultiplexListener(a, b){
  $clinit_Listener$MultiplexListener();
  ArrayList_0.call(this, 2);
  push_1(this.array, a);
  push_1(this.array, b);
}

defineClass(144, 18, {139:1, 144:1, 3:1, 24:1, 20:1, 13:1, 51:1}, Listener$MultiplexListener);
_.afterRemove = function afterRemove_9(obj, property, index_0, element){
  var l, l$array, l$index, l$max;
  for (l$array = castTo($toArray(this, initUnidimensionalArray(Lde_haumacher_msgbuf_observer_Listener_2_classLit, $intern_33, 139, 0, 0, 1)), 158) , l$index = 0 , l$max = l$array.length; l$index < l$max; ++l$index) {
    l = l$array[l$index];
    l.afterRemove(obj, property, index_0, element);
  }
}
;
_.beforeAdd = function beforeAdd_9(obj, property, index_0, element){
  var l, l$array, l$index, l$max;
  for (l$array = castTo($toArray(this, initUnidimensionalArray(Lde_haumacher_msgbuf_observer_Listener_2_classLit, $intern_33, 139, 0, 0, 1)), 158) , l$index = 0 , l$max = l$array.length; l$index < l$max; ++l$index) {
    l = l$array[l$index];
    l.beforeAdd(obj, property, index_0, element);
  }
}
;
_.beforeSet = function beforeSet_1(obj, property, value_0){
  var l, l$array, l$index, l$max;
  for (l$array = castTo($toArray(this, initUnidimensionalArray(Lde_haumacher_msgbuf_observer_Listener_2_classLit, $intern_33, 139, 0, 0, 1)), 158) , l$index = 0 , l$max = l$array.length; l$index < l$max; ++l$index) {
    l = l$array[l$index];
    l.beforeSet(obj, property, value_0);
  }
}
;
var Lde_haumacher_msgbuf_observer_Listener$MultiplexListener_2_classLit = createForClass('de.haumacher.msgbuf.observer', 'Listener/MultiplexListener', 144);
function checkOffsetAndCount(length_0, count){
  if (count < 0 || count > length_0) {
    throw toJs(new IndexOutOfBoundsException);
  }
}

function AbstractStringBuilder(string){
  this.string = string;
}

defineClass(161, 1, $intern_34);
_.toString_0 = function toString_21(){
  return this.string;
}
;
var Ljava_lang_AbstractStringBuilder_2_classLit = createForClass('java.lang', 'AbstractStringBuilder', 161);
function ArithmeticException(){
  RuntimeException_0.call(this, 'divide by zero');
}

defineClass(272, 16, $intern_2, ArithmeticException);
var Ljava_lang_ArithmeticException_2_classLit = createForClass('java.lang', 'ArithmeticException', 272);
function ArrayIndexOutOfBoundsException(msg){
  IndexOutOfBoundsException_0.call(this, msg);
}

defineClass(448, 97, $intern_2, ArrayIndexOutOfBoundsException);
var Ljava_lang_ArrayIndexOutOfBoundsException_2_classLit = createForClass('java.lang', 'ArrayIndexOutOfBoundsException', 448);
function ArrayStoreException(){
  RuntimeException.call(this);
}

function ArrayStoreException_0(message){
  RuntimeException_0.call(this, message);
}

defineClass(200, 16, $intern_2, ArrayStoreException, ArrayStoreException_0);
var Ljava_lang_ArrayStoreException_2_classLit = createForClass('java.lang', 'ArrayStoreException', 200);
function codePointAt(cs, index_0, limit){
  var hiSurrogate, loSurrogate;
  hiSurrogate = $charAt(cs, index_0++);
  if (hiSurrogate >= 55296 && hiSurrogate <= 56319 && index_0 < limit && isLowSurrogate(loSurrogate = (checkCriticalStringElementIndex(index_0, cs.length) , cs.charCodeAt(index_0)))) {
    return $intern_5 + ((hiSurrogate & 1023) << 10) + (loSurrogate & 1023);
  }
  return hiSurrogate;
}

function digit(c){
  if (c >= 48 && c < 48 + $wnd.Math.min(10, 10)) {
    return c - 48;
  }
  if (c >= 97 && c < 97) {
    return c - 97 + 10;
  }
  if (c >= 65 && c < 65) {
    return c - 65 + 10;
  }
  return -1;
}

function isLowSurrogate(ch_0){
  return ch_0 >= 56320 && ch_0 <= 57343;
}

function ClassCastException(){
  RuntimeException_0.call(this, null);
}

defineClass(254, 16, $intern_2, ClassCastException);
var Ljava_lang_ClassCastException_2_classLit = createForClass('java.lang', 'ClassCastException', 254);
function IllegalStateException(){
  RuntimeException.call(this);
}

function IllegalStateException_0(s){
  RuntimeException_0.call(this, s);
}

defineClass(9, 16, $intern_2, IllegalStateException, IllegalStateException_0);
var Ljava_lang_IllegalStateException_2_classLit = createForClass('java.lang', 'IllegalStateException', 9);
function $compareTo_2(this$static, b){
  return compare_4(this$static.value_0, b.value_0);
}

function Integer(value_0){
  this.value_0 = value_0;
}

function compare_4(x_0, y_0){
  return x_0 < y_0?-1:x_0 > y_0?1:0;
}

function numberOfLeadingZeros_0(i){
  var m, n, y_0;
  if (i < 0) {
    return 0;
  }
   else if (i == 0) {
    return 32;
  }
   else {
    y_0 = -(i >> 16);
    m = y_0 >> 16 & 16;
    n = 16 - m;
    i = i >> m;
    y_0 = i - 256;
    m = y_0 >> 16 & 8;
    n += m;
    i <<= m;
    y_0 = i - 4096;
    m = y_0 >> 16 & 4;
    n += m;
    i <<= m;
    y_0 = i - 16384;
    m = y_0 >> 16 & 2;
    n += m;
    i <<= m;
    y_0 = i >> 14;
    m = y_0 & ~(y_0 >> 1);
    return n + 2 - m;
  }
}

function numberOfTrailingZeros(i){
  var r, rtn;
  if (i == 0) {
    return 32;
  }
   else {
    rtn = 0;
    for (r = 1; (r & i) == 0; r <<= 1) {
      ++rtn;
    }
    return rtn;
  }
}

function valueOf(i){
  var rebase, result;
  if (i > -129 && i < 128) {
    return $clinit_Integer$BoxedValues() , rebase = i + 128 , result = boxedValues[rebase] , !result && (result = boxedValues[rebase] = new Integer(i)) , result;
  }
  return new Integer(i);
}

defineClass(64, 63, {3:1, 33:1, 64:1, 63:1}, Integer);
_.compareTo_0 = function compareTo_1(b){
  return $compareTo_2(this, castTo(b, 64));
}
;
_.doubleValue = function doubleValue(){
  return this.value_0;
}
;
_.equals_0 = function equals_3(o){
  return instanceOf(o, 64) && castTo(o, 64).value_0 == this.value_0;
}
;
_.hashCode_0 = function hashCode_4(){
  return this.value_0;
}
;
_.toString_0 = function toString_23(){
  return '' + this.value_0;
}
;
_.value_0 = 0;
var Ljava_lang_Integer_2_classLit = createForClass('java.lang', 'Integer', 64);
function $clinit_Integer$BoxedValues(){
  $clinit_Integer$BoxedValues = emptyMethod;
  boxedValues = initUnidimensionalArray(Ljava_lang_Integer_2_classLit, $intern_0, 64, 256, 0, 1);
}

var boxedValues;
function compare_5(x_0, y_0){
  return compare_0(x_0, y_0) < 0?-1:compare_0(x_0, y_0) > 0?1:0;
}

defineClass(662, 1, {});
function NegativeArraySizeException(message){
  RuntimeException_0.call(this, message);
}

defineClass(405, 16, $intern_2, NegativeArraySizeException);
var Ljava_lang_NegativeArraySizeException_2_classLit = createForClass('java.lang', 'NegativeArraySizeException', 405);
function NumberFormatException(message){
  IllegalArgumentException.call(this, message);
}

defineClass(59, 15, {3:1, 59:1, 16:1, 11:1}, NumberFormatException);
var Ljava_lang_NumberFormatException_2_classLit = createForClass('java.lang', 'NumberFormatException', 59);
function StackTraceElement(methodName, fileName, lineNumber){
  this.className_0 = 'Unknown';
  this.methodName = methodName;
  this.fileName = fileName;
  this.lineNumber = lineNumber;
}

defineClass(75, 1, {3:1, 75:1}, StackTraceElement);
_.equals_0 = function equals_4(other){
  var st;
  if (instanceOf(other, 75)) {
    st = castTo(other, 75);
    return this.lineNumber == st.lineNumber && this.methodName == st.methodName && this.className_0 == st.className_0 && this.fileName == st.fileName;
  }
  return false;
}
;
_.hashCode_0 = function hashCode_5(){
  return hashCode_11(stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_Object_2_classLit, 1), $intern_0, 1, 5, [valueOf(this.lineNumber), this.className_0, this.methodName, this.fileName]));
}
;
_.toString_0 = function toString_24(){
  return this.className_0 + '.' + this.methodName + '(' + (this.fileName != null?this.fileName:'Unknown Source') + (this.lineNumber >= 0?':' + this.lineNumber:'') + ')';
}
;
_.lineNumber = 0;
var Ljava_lang_StackTraceElement_2_classLit = createForClass('java.lang', 'StackTraceElement', 75);
function $append_0(this$static, x_0){
  return this$static.string += String.fromCharCode(x_0) , this$static;
}

function $append_1(this$static, x_0){
  this$static.string += String.fromCharCode(x_0);
  return this$static;
}

function $append_2(this$static, x_0){
  this$static.string += x_0;
  return this$static;
}

function $append_3(this$static, x_0){
  return this$static.string += '' + x_0 , this$static;
}

function $append_4(this$static, x_0){
  this$static.string += '' + x_0;
  return this$static;
}

function $append_5(this$static, x_0, start_0, end){
  return this$static.string += '' + (checkCriticalStringBounds(start_0, end, (x_0 == null?'null':x_0).length) , (x_0 == null?'null':x_0).substr(start_0, end - start_0)) , this$static;
}

function $append_6(this$static, x_0, start_0, end){
  this$static.string += '' + $substring_0(x_0 == null?'null':toString_8(x_0), start_0, end);
  return this$static;
}

function $append_7(this$static, x_0){
  this$static.string += '' + x_0;
  return this$static;
}

function $append_8(this$static, x_0, start_0, len){
  this$static.string += valueOf_1(x_0, start_0, len);
  return this$static;
}

function StringBuilder(){
  AbstractStringBuilder.call(this, '');
}

function StringBuilder_0(){
  AbstractStringBuilder.call(this, '');
}

function StringBuilder_1(s){
  AbstractStringBuilder.call(this, (checkCriticalNotNull(s) , s));
}

defineClass(37, 161, $intern_34, StringBuilder, StringBuilder_0, StringBuilder_1);
var Ljava_lang_StringBuilder_2_classLit = createForClass('java.lang', 'StringBuilder', 37);
function $clinit_System(){
  $clinit_System = emptyMethod;
  err = new PrintStream(null);
  new PrintStream(null);
}

function arraycopy(src_0, srcOfs, dest, destOfs, len){
  $clinit_System();
  var destArray, destComp, destEnd, destType, srcArray, srcComp, srcType;
  checkCriticalNotNull_0(src_0, 'src');
  checkCriticalNotNull_0(dest, 'dest');
  srcType = getClass__Ljava_lang_Class___devirtual$(src_0);
  destType = getClass__Ljava_lang_Class___devirtual$(dest);
  checkCriticalArrayType_0((srcType.modifiers_0 & 4) != 0, 'srcType is not an array');
  checkCriticalArrayType_0((destType.modifiers_0 & 4) != 0, 'destType is not an array');
  srcComp = srcType.componentType;
  destComp = destType.componentType;
  checkCriticalArrayType_0((srcComp.modifiers_0 & 1) != 0?srcComp == destComp:(destComp.modifiers_0 & 1) == 0, "Array types don't match");
  checkArrayCopyIndicies(src_0, srcOfs, dest, destOfs, len);
  if ((srcComp.modifiers_0 & 1) == 0 && srcType != destType) {
    srcArray = castToArray(src_0);
    destArray = castToArray(dest);
    if (maskUndefined(src_0) === maskUndefined(dest) && srcOfs < destOfs) {
      srcOfs += len;
      for (destEnd = destOfs + len; destEnd-- > destOfs;) {
        setCheck(destArray, destEnd, srcArray[--srcOfs]);
      }
    }
     else {
      for (destEnd = destOfs + len; destOfs < destEnd;) {
        setCheck(destArray, destOfs++, srcArray[srcOfs++]);
      }
    }
  }
   else {
    copy_0(src_0, srcOfs, dest, destOfs, len, true);
  }
}

function checkArrayCopyIndicies(src_0, srcOfs, dest, destOfs, len){
  var destlen, srclen;
  srclen = src_0.length;
  destlen = dest.length;
  if (srcOfs < 0 || destOfs < 0 || len < 0 || srcOfs + len > srclen || destOfs + len > destlen) {
    throw toJs(new IndexOutOfBoundsException);
  }
}

defineClass(664, 1, {});
var err;
function UnsupportedOperationException(){
  RuntimeException.call(this);
}

function UnsupportedOperationException_0(message){
  RuntimeException_0.call(this, message);
}

defineClass(23, 16, $intern_2, UnsupportedOperationException, UnsupportedOperationException_0);
var Ljava_lang_UnsupportedOperationException_2_classLit = createForClass('java.lang', 'UnsupportedOperationException', 23);
function $compareTo_4(this$static, that){
  return $compareToIgnoreCase(this$static.name_0, that.name_0);
}

defineClass(83, 1, $intern_35);
_.compareTo_0 = function compareTo_2(that){
  return $compareTo_4(this, castTo(that, 83));
}
;
_.equals_0 = function equals_5(o){
  var that;
  if (o === this) {
    return true;
  }
  if (!instanceOf(o, 83)) {
    return false;
  }
  that = castTo(o, 83);
  return $equals_0(this.name_0, that.name_0);
}
;
_.hashCode_0 = function hashCode_6(){
  return $hashCode_1(this.name_0);
}
;
_.toString_0 = function toString_25(){
  return this.name_0;
}
;
var Ljava_nio_charset_Charset_2_classLit = createForClass('java.nio.charset', 'Charset', 83);
function $containsEntry(this$static, entry){
  var key, ourValue, value_0;
  key = entry.getKey();
  value_0 = entry.getValue();
  ourValue = this$static.get(key);
  if (!(maskUndefined(value_0) === maskUndefined(ourValue) || value_0 != null && equals_Ljava_lang_Object__Z__devirtual$(value_0, ourValue))) {
    return false;
  }
  if (ourValue == null && !this$static.containsKey(key)) {
    return false;
  }
  return true;
}

function $getOrDefault(this$static, key, defaultValue){
  var currentValue;
  return currentValue = this$static.get(key) , currentValue == null && !this$static.containsKey(key)?defaultValue:currentValue;
}

function $implFindEntry(this$static, key, remove){
  var entry, iter, k;
  for (iter = this$static.entrySet_0().iterator(); iter.hasNext_0();) {
    entry = castTo(iter.next_1(), 22);
    k = entry.getKey();
    if (maskUndefined(key) === maskUndefined(k) || key != null && equals_Ljava_lang_Object__Z__devirtual$(key, k)) {
      if (remove) {
        entry = new AbstractMap$SimpleEntry(entry.getKey(), entry.getValue());
        iter.remove_0();
      }
      return entry;
    }
  }
  return null;
}

function $putAll_0(this$static, map_0){
  var e, e$iterator;
  checkCriticalNotNull(map_0);
  for (e$iterator = map_0.entrySet_0().iterator(); e$iterator.hasNext_0();) {
    e = castTo(e$iterator.next_1(), 22);
    this$static.put(e.getKey(), e.getValue());
  }
}

function $toString_0(this$static, o){
  return o === this$static?'(this Map)':o == null?'null':toString_8(o);
}

function getEntryValueOrNull(entry){
  return !entry?null:entry.getValue();
}

defineClass(484, 1, {21:1});
_.getOrDefault = function getOrDefault_0(key, defaultValue){
  return $getOrDefault(this, key, defaultValue);
}
;
_.merge = function merge_0(key, value_0, remappingFunction){
  return $merge(this, key, value_0, remappingFunction);
}
;
_.putIfAbsent = function putIfAbsent_0(key, value_0){
  var currentValue;
  return currentValue = this.get(key) , currentValue != null?currentValue:this.put(key, value_0);
}
;
_.replace = function replace_0(key, value_0){
  return this.containsKey(key)?this.put(key, value_0):null;
}
;
_.clear = function clear_5(){
  this.entrySet_0().clear();
}
;
_.containsKey = function containsKey_0(key){
  return !!$implFindEntry(this, key, false);
}
;
_.containsValue = function containsValue_0(value_0){
  var entry, entry$iterator, v;
  for (entry$iterator = this.entrySet_0().iterator(); entry$iterator.hasNext_0();) {
    entry = castTo(entry$iterator.next_1(), 22);
    v = entry.getValue();
    if (maskUndefined(value_0) === maskUndefined(v) || value_0 != null && equals_Ljava_lang_Object__Z__devirtual$(value_0, v)) {
      return true;
    }
  }
  return false;
}
;
_.equals_0 = function equals_6(obj){
  var entry, entry$iterator, otherMap;
  if (obj === this) {
    return true;
  }
  if (!instanceOf(obj, 21)) {
    return false;
  }
  otherMap = castTo(obj, 21);
  if (this.size() != otherMap.size()) {
    return false;
  }
  for (entry$iterator = otherMap.entrySet_0().iterator(); entry$iterator.hasNext_0();) {
    entry = castTo(entry$iterator.next_1(), 22);
    if (!$containsEntry(this, entry)) {
      return false;
    }
  }
  return true;
}
;
_.get = function get_26(key){
  return getEntryValueOrNull($implFindEntry(this, key, false));
}
;
_.hashCode_0 = function hashCode_7(){
  return hashCode_12(this.entrySet_0());
}
;
_.isEmpty = function isEmpty_2(){
  return this.size() == 0;
}
;
_.keySet = function keySet_0(){
  return new AbstractMap$1(this);
}
;
_.put = function put_1(key, value_0){
  throw toJs(new UnsupportedOperationException_0('Put not supported on this map'));
}
;
_.putAll = function putAll_0(map_0){
  $putAll_0(this, map_0);
}
;
_.remove = function remove_9(key){
  return getEntryValueOrNull($implFindEntry(this, key, true));
}
;
_.size = function size_4(){
  return this.entrySet_0().size();
}
;
_.toString_0 = function toString_26(){
  var entry, entry$iterator, joiner;
  joiner = new StringJoiner(', ', '{', '}');
  for (entry$iterator = this.entrySet_0().iterator(); entry$iterator.hasNext_0();) {
    entry = castTo(entry$iterator.next_1(), 22);
    $add_3(joiner, $toString_0(this, entry.getKey()) + '=' + $toString_0(this, entry.getValue()));
  }
  return !joiner.builder?joiner.emptyValue:joiner.suffix.length == 0?joiner.builder.string:joiner.builder.string + ('' + joiner.suffix);
}
;
_.values = function values_12(){
  return new AbstractMap$2(this);
}
;
var Ljava_util_AbstractMap_2_classLit = createForClass('java.util', 'AbstractMap', 484);
function $containsKey(this$static, key){
  return instanceOfString(key)?$hasStringValue(this$static, key):!!$getEntry(this$static.hashCodeMap, key);
}

function $containsValue(this$static, value_0, entries){
  var entry, entry$iterator;
  for (entry$iterator = entries.iterator(); entry$iterator.hasNext_0();) {
    entry = castTo(entry$iterator.next_1(), 22);
    if (this$static.equals_1(value_0, entry.getValue())) {
      return true;
    }
  }
  return false;
}

function $get_6(this$static, key){
  return instanceOfString(key)?$getStringValue(this$static, key):getEntryValueOrNull($getEntry(this$static.hashCodeMap, key));
}

function $getStringValue(this$static, key){
  return key == null?getEntryValueOrNull($getEntry(this$static.hashCodeMap, null)):$get_7(this$static.stringMap, key);
}

function $hasStringValue(this$static, key){
  return key == null?!!$getEntry(this$static.hashCodeMap, null):$contains_4(this$static.stringMap, key);
}

function $put_0(this$static, key, value_0){
  return instanceOfString(key)?$putStringValue(this$static, key, value_0):$put_2(this$static.hashCodeMap, key, value_0);
}

function $putStringValue(this$static, key, value_0){
  return key == null?$put_2(this$static.hashCodeMap, null, value_0):$put_3(this$static.stringMap, key, value_0);
}

function $remove_1(this$static, key){
  return instanceOfString(key)?key == null?$remove_4(this$static.hashCodeMap, null):$remove_5(this$static.stringMap, key):$remove_4(this$static.hashCodeMap, key);
}

function $reset(this$static){
  this$static.hashCodeMap = new InternalHashCodeMap(this$static);
  this$static.stringMap = new InternalStringMap(this$static);
  ++this$static.modCount;
}

function $size(this$static){
  return this$static.hashCodeMap.size_0 + this$static.stringMap.size_0;
}

defineClass(163, 484, {21:1});
_.clear = function clear_6(){
  $reset(this);
}
;
_.containsKey = function containsKey_1(key){
  return $containsKey(this, key);
}
;
_.containsValue = function containsValue_1(value_0){
  return $containsValue(this, value_0, this.stringMap) || $containsValue(this, value_0, this.hashCodeMap);
}
;
_.entrySet_0 = function entrySet_0(){
  return new AbstractHashMap$EntrySet(this);
}
;
_.get = function get_27(key){
  return $get_6(this, key);
}
;
_.put = function put_2(key, value_0){
  return $put_0(this, key, value_0);
}
;
_.remove = function remove_10(key){
  return $remove_1(this, key);
}
;
_.size = function size_5(){
  return $size(this);
}
;
_.modCount = 0;
var Ljava_util_AbstractHashMap_2_classLit = createForClass('java.util', 'AbstractHashMap', 163);
function $removeAll(this$static, c){
  var iter, o, o$iterator, size_0;
  checkCriticalNotNull(c);
  size_0 = this$static.size();
  if (size_0 < c.size()) {
    for (iter = this$static.iterator(); iter.hasNext_0();) {
      o = iter.next_1();
      c.contains(o) && iter.remove_0();
    }
  }
   else {
    for (o$iterator = c.iterator(); o$iterator.hasNext_0();) {
      o = o$iterator.next_1();
      this$static.remove(o);
    }
  }
  return size_0 != this$static.size();
}

defineClass(486, 485, $intern_36);
_.spliterator_0 = function spliterator_2(){
  return new Spliterators$IteratorSpliterator(this, 1);
}
;
_.equals_0 = function equals_7(o){
  var other;
  if (o === this) {
    return true;
  }
  if (!instanceOf(o, 39)) {
    return false;
  }
  other = castTo(o, 39);
  if (other.size() != this.size()) {
    return false;
  }
  return $containsAll(this, other);
}
;
_.hashCode_0 = function hashCode_8(){
  return hashCode_12(this);
}
;
_.removeAll = function removeAll_1(c){
  return $removeAll(this, c);
}
;
var Ljava_util_AbstractSet_2_classLit = createForClass('java.util', 'AbstractSet', 486);
function $contains_1(this$static, o){
  if (instanceOf(o, 22)) {
    return $containsEntry(this$static.this$01, castTo(o, 22));
  }
  return false;
}

function AbstractHashMap$EntrySet(this$0){
  this.this$01 = this$0;
}

defineClass(94, 486, $intern_36, AbstractHashMap$EntrySet);
_.clear = function clear_7(){
  this.this$01.clear();
}
;
_.contains = function contains_1(o){
  return $contains_1(this, o);
}
;
_.iterator = function iterator_3(){
  return new AbstractHashMap$EntrySetIterator(this.this$01);
}
;
_.remove = function remove_11(entry){
  var key;
  if ($contains_1(this, entry)) {
    key = castTo(entry, 22).getKey();
    this.this$01.remove(key);
    return true;
  }
  return false;
}
;
_.size = function size_6(){
  return this.this$01.size();
}
;
var Ljava_util_AbstractHashMap$EntrySet_2_classLit = createForClass('java.util', 'AbstractHashMap/EntrySet', 94);
function $computeHasNext(this$static){
  if (this$static.current.hasNext_0()) {
    return true;
  }
  if (this$static.current != this$static.stringMapEntries) {
    return false;
  }
  this$static.current = new InternalHashCodeMap$1(this$static.this$01.hashCodeMap);
  return this$static.current.hasNext_0();
}

function $next_0(this$static){
  var rv;
  checkCriticalConcurrentModification(this$static.this$01.modCount, this$static.lastModCount);
  checkCriticalElement(this$static.hasNext);
  this$static.last = this$static.current;
  rv = castTo(this$static.current.next_1(), 22);
  this$static.hasNext = $computeHasNext(this$static);
  return rv;
}

function AbstractHashMap$EntrySetIterator(this$0){
  this.this$01 = this$0;
  this.stringMapEntries = new InternalStringMap$1(this.this$01.stringMap);
  this.current = this.stringMapEntries;
  this.hasNext = $computeHasNext(this);
  this.lastModCount = this.this$01.modCount;
}

defineClass(95, 1, {}, AbstractHashMap$EntrySetIterator);
_.forEachRemaining = function forEachRemaining_1(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_2(){
  return $next_0(this);
}
;
_.hasNext_0 = function hasNext_2(){
  return this.hasNext;
}
;
_.remove_0 = function remove_12(){
  checkCriticalState(!!this.last);
  checkCriticalConcurrentModification(this.this$01.modCount, this.lastModCount);
  this.last.remove_0();
  this.last = null;
  this.hasNext = $computeHasNext(this);
  this.lastModCount = this.this$01.modCount;
}
;
_.hasNext = false;
_.lastModCount = 0;
var Ljava_util_AbstractHashMap$EntrySetIterator_2_classLit = createForClass('java.util', 'AbstractHashMap/EntrySetIterator', 95);
function $remove_2(this$static){
  checkCriticalState(this$static.last != -1);
  this$static.this$01.removeAtIndex(this$static.last);
  this$static.i = this$static.last;
  this$static.last = -1;
}

function AbstractList$IteratorImpl(this$0){
  this.this$01 = this$0;
}

defineClass(143, 1, {}, AbstractList$IteratorImpl);
_.forEachRemaining = function forEachRemaining_2(consumer){
  $forEachRemaining(this, consumer);
}
;
_.hasNext_0 = function hasNext_3(){
  return this.i < this.this$01.size();
}
;
_.next_1 = function next_3(){
  return checkCriticalElement(this.i < this.this$01.size()) , this.this$01.getAtIndex(this.last = this.i++);
}
;
_.remove_0 = function remove_13(){
  $remove_2(this);
}
;
_.i = 0;
_.last = -1;
var Ljava_util_AbstractList$IteratorImpl_2_classLit = createForClass('java.util', 'AbstractList/IteratorImpl', 143);
function AbstractList$ListIteratorImpl(this$0, start_0){
  AbstractList$IteratorImpl.call(this, this$0);
  checkCriticalPositionIndex(start_0, this$0.size());
  this.i = start_0;
}

defineClass(167, 143, {}, AbstractList$ListIteratorImpl);
_.remove_0 = function remove_14(){
  $remove_2(this);
}
;
var Ljava_util_AbstractList$ListIteratorImpl_2_classLit = createForClass('java.util', 'AbstractList/ListIteratorImpl', 167);
function AbstractList$SubList(wrapped, fromIndex, toIndex){
  checkCriticalPositionIndexes(fromIndex, toIndex, wrapped.size());
  this.wrapped = wrapped;
  this.fromIndex = fromIndex;
  this.size_0 = toIndex - fromIndex;
}

defineClass(268, 487, $intern_30, AbstractList$SubList);
_.addAtIndex = function add_7(index_0, element){
  checkCriticalPositionIndex(index_0, this.size_0);
  this.wrapped.addAtIndex(this.fromIndex + index_0, element);
  ++this.size_0;
}
;
_.getAtIndex = function get_28(index_0){
  checkCriticalElementIndex(index_0, this.size_0);
  return this.wrapped.getAtIndex(this.fromIndex + index_0);
}
;
_.removeAtIndex = function remove_15(index_0){
  var result;
  checkCriticalElementIndex(index_0, this.size_0);
  result = this.wrapped.removeAtIndex(this.fromIndex + index_0);
  --this.size_0;
  return result;
}
;
_.setAtIndex = function set_25(index_0, element){
  checkCriticalElementIndex(index_0, this.size_0);
  return this.wrapped.setAtIndex(this.fromIndex + index_0, element);
}
;
_.size = function size_7(){
  return this.size_0;
}
;
_.fromIndex = 0;
_.size_0 = 0;
var Ljava_util_AbstractList$SubList_2_classLit = createForClass('java.util', 'AbstractList/SubList', 268);
function AbstractMap$1(this$0){
  this.this$01 = this$0;
}

defineClass(52, 486, $intern_36, AbstractMap$1);
_.clear = function clear_8(){
  this.this$01.clear();
}
;
_.contains = function contains_2(key){
  return this.this$01.containsKey(key);
}
;
_.iterator = function iterator_4(){
  var outerIter;
  return outerIter = this.this$01.entrySet_0().iterator() , new AbstractMap$1$1(outerIter);
}
;
_.remove = function remove_16(key){
  if (this.this$01.containsKey(key)) {
    this.this$01.remove(key);
    return true;
  }
  return false;
}
;
_.size = function size_8(){
  return this.this$01.size();
}
;
var Ljava_util_AbstractMap$1_2_classLit = createForClass('java.util', 'AbstractMap/1', 52);
function AbstractMap$1$1(val$outerIter){
  this.val$outerIter2 = val$outerIter;
}

defineClass(85, 1, {}, AbstractMap$1$1);
_.forEachRemaining = function forEachRemaining_3(consumer){
  $forEachRemaining(this, consumer);
}
;
_.hasNext_0 = function hasNext_4(){
  return this.val$outerIter2.hasNext_0();
}
;
_.next_1 = function next_4(){
  var entry;
  return entry = castTo(this.val$outerIter2.next_1(), 22) , entry.getKey();
}
;
_.remove_0 = function remove_17(){
  this.val$outerIter2.remove_0();
}
;
var Ljava_util_AbstractMap$1$1_2_classLit = createForClass('java.util', 'AbstractMap/1/1', 85);
function AbstractMap$2(this$0){
  this.this$01 = this$0;
}

defineClass(96, 485, $intern_17, AbstractMap$2);
_.clear = function clear_9(){
  this.this$01.clear();
}
;
_.contains = function contains_3(value_0){
  return this.this$01.containsValue(value_0);
}
;
_.iterator = function iterator_5(){
  var outerIter;
  return outerIter = this.this$01.entrySet_0().iterator() , new AbstractMap$2$1(outerIter);
}
;
_.size = function size_9(){
  return this.this$01.size();
}
;
var Ljava_util_AbstractMap$2_2_classLit = createForClass('java.util', 'AbstractMap/2', 96);
function AbstractMap$2$1(val$outerIter){
  this.val$outerIter2 = val$outerIter;
}

defineClass(110, 1, {}, AbstractMap$2$1);
_.forEachRemaining = function forEachRemaining_4(consumer){
  $forEachRemaining(this, consumer);
}
;
_.hasNext_0 = function hasNext_5(){
  return this.val$outerIter2.hasNext_0();
}
;
_.next_1 = function next_5(){
  var entry;
  return entry = castTo(this.val$outerIter2.next_1(), 22) , entry.getValue();
}
;
_.remove_0 = function remove_18(){
  this.val$outerIter2.remove_0();
}
;
var Ljava_util_AbstractMap$2$1_2_classLit = createForClass('java.util', 'AbstractMap/2/1', 110);
function $setValue_1(this$static, value_0){
  var oldValue;
  oldValue = this$static.value_0;
  this$static.value_0 = value_0;
  return oldValue;
}

defineClass(259, 1, $intern_37);
_.equals_0 = function equals_8(other){
  var entry;
  if (!instanceOf(other, 22)) {
    return false;
  }
  entry = castTo(other, 22);
  return equals_18(this.key, entry.getKey()) && equals_18(this.value_0, entry.getValue());
}
;
_.getKey = function getKey(){
  return this.key;
}
;
_.getValue = function getValue(){
  return this.value_0;
}
;
_.hashCode_0 = function hashCode_9(){
  return hashCode_19(this.key) ^ hashCode_19(this.value_0);
}
;
_.setValue = function setValue(value_0){
  return $setValue_1(this, value_0);
}
;
_.toString_0 = function toString_27(){
  return this.key + '=' + this.value_0;
}
;
var Ljava_util_AbstractMap$AbstractEntry_2_classLit = createForClass('java.util', 'AbstractMap/AbstractEntry', 259);
function AbstractMap$SimpleEntry(key, value_0){
  this.key = key;
  this.value_0 = value_0;
}

defineClass(164, 259, $intern_37, AbstractMap$SimpleEntry);
var Ljava_util_AbstractMap$SimpleEntry_2_classLit = createForClass('java.util', 'AbstractMap/SimpleEntry', 164);
defineClass(488, 1, $intern_37);
_.equals_0 = function equals_9(other){
  var entry;
  if (!instanceOf(other, 22)) {
    return false;
  }
  entry = castTo(other, 22);
  return equals_18(this.val$entry2.value[0], entry.getKey()) && equals_18($getValue(this), entry.getValue());
}
;
_.hashCode_0 = function hashCode_10(){
  return hashCode_19(this.val$entry2.value[0]) ^ hashCode_19($getValue(this));
}
;
_.toString_0 = function toString_28(){
  return this.val$entry2.value[0] + '=' + $getValue(this);
}
;
var Ljava_util_AbstractMapEntry_2_classLit = createForClass('java.util', 'AbstractMapEntry', 488);
function $next_1(this$static){
  checkCriticalElement(this$static.i < this$static.this$01.array.length);
  this$static.last = this$static.i++;
  return this$static.this$01.array[this$static.last];
}

function ArrayList$1(this$0){
  this.this$01 = this$0;
}

defineClass(10, 1, {}, ArrayList$1);
_.forEachRemaining = function forEachRemaining_5(consumer){
  $forEachRemaining(this, consumer);
}
;
_.hasNext_0 = function hasNext_6(){
  return this.i < this.this$01.array.length;
}
;
_.next_1 = function next_6(){
  return $next_1(this);
}
;
_.remove_0 = function remove_19(){
  checkCriticalState(this.last != -1);
  this.this$01.removeAtIndex(this.i = this.last);
  this.last = -1;
}
;
_.i = 0;
_.last = -1;
var Ljava_util_ArrayList$1_2_classLit = createForClass('java.util', 'ArrayList/1', 10);
function copyOf(original, newLength){
  checkCriticalArraySize(newLength);
  return copyPrimitiveArray(original, initUnidimensionalArray(I_classLit, $intern_4, 46, newLength, 15, 1), newLength);
}

function copyOf_0(original, newLength){
  var result;
  checkCriticalArraySize(newLength);
  return result = original.slice(0, newLength) , result.length = newLength , stampJavaTypeInfo_0(result, original);
}

function copyPrimitiveArray(original, copy, to){
  var copyLen, len;
  len = original.length;
  copyLen = $wnd.Math.min(to, len);
  copy_0(original, 0, copy, 0, copyLen, true);
  return copy;
}

function hashCode_11(a){
  var e, e$array, e$index, e$max, hashCode;
  hashCode = 1;
  for (e$array = a , e$index = 0 , e$max = e$array.length; e$index < e$max; ++e$index) {
    e = e$array[e$index];
    hashCode = 31 * hashCode + (e != null?hashCode__I__devirtual$(e):0);
    hashCode = hashCode | 0;
  }
  return hashCode;
}

function insertionSort(array, low, high, comp){
  var i, j, t;
  for (i = low + 1; i < high; ++i) {
    for (j = i; j > low && comp.compare(array[j - 1], array[j]) > 0; --j) {
      t = array[j];
      setCheck(array, j, array[j - 1]);
      setCheck(array, j - 1, t);
    }
  }
}

function merge_1(src_0, srcLow, srcMid, srcHigh, dest, destLow, destHigh, comp){
  var topIdx;
  topIdx = srcMid;
  while (destLow < destHigh) {
    topIdx >= srcHigh || srcLow < srcMid && comp.compare(src_0[srcLow], src_0[topIdx]) <= 0?setCheck(dest, destLow++, src_0[srcLow++]):setCheck(dest, destLow++, src_0[topIdx++]);
  }
}

function mergeSort(x_0, fromIndex, toIndex, comp){
  var temp;
  comp = ($clinit_Comparators() , !comp?INTERNAL_NATURAL_ORDER:comp);
  temp = x_0.slice(fromIndex, toIndex);
  mergeSort_0(temp, x_0, fromIndex, toIndex, -fromIndex, comp);
}

function mergeSort_0(temp, array, low, high, ofs, comp){
  var length_0, tempHigh, tempLow, tempMid;
  length_0 = high - low;
  if (length_0 < 7) {
    insertionSort(array, low, high, comp);
    return;
  }
  tempLow = low + ofs;
  tempHigh = high + ofs;
  tempMid = tempLow + (tempHigh - tempLow >> 1);
  mergeSort_0(array, temp, tempLow, tempMid, -ofs, comp);
  mergeSort_0(array, temp, tempMid, tempHigh, -ofs, comp);
  if (comp.compare(temp[tempMid - 1], temp[tempMid]) <= 0) {
    while (low < high) {
      setCheck(array, low++, temp[tempLow++]);
    }
    return;
  }
  merge_1(temp, tempLow, tempMid, tempHigh, array, low, high, comp);
}

function sort_0(x_0, toIndex, c){
  checkCriticalArrayBounds(toIndex, x_0.length);
  mergeSort(x_0, 0, toIndex, c);
}

function $forEach_0(this$static, consumer){
  var e, e$array, e$index, e$max;
  checkCriticalNotNull(consumer);
  for (e$array = this$static.array , e$index = 0 , e$max = e$array.length; e$index < e$max; ++e$index) {
    e = e$array[e$index];
    consumer.accept(e);
  }
}

function $toArray_0(this$static, out){
  var i, size_0;
  size_0 = this$static.array.length;
  out.length < size_0 && (out = stampJavaTypeInfo_1(new Array(size_0), out));
  for (i = 0; i < size_0; ++i) {
    setCheck(out, i, this$static.array[i]);
  }
  out.length > size_0 && setCheck(out, size_0, null);
  return out;
}

function Arrays$ArrayList(array){
  checkCriticalNotNull(array);
  this.array = array;
}

defineClass(14, 487, $intern_31, Arrays$ArrayList);
_.contains = function contains_4(o){
  return $indexOf(this, o) != -1;
}
;
_.getAtIndex = function get_29(index_0){
  return checkCriticalElementIndex(index_0, this.array.length) , this.array[index_0];
}
;
_.setAtIndex = function set_26(index_0, value_0){
  var was;
  was = (checkCriticalElementIndex(index_0, this.array.length) , this.array[index_0]);
  setCheck(this.array, index_0, value_0);
  return was;
}
;
_.size = function size_10(){
  return this.array.length;
}
;
_.toArray = function toArray_3(){
  return $toArray_0(this, initUnidimensionalArray(Ljava_lang_Object_2_classLit, $intern_0, 1, this.array.length, 5, 1));
}
;
_.toArray_0 = function toArray_4(out){
  return $toArray_0(this, out);
}
;
var Ljava_util_Arrays$ArrayList_2_classLit = createForClass('java.util', 'Arrays/ArrayList', 14);
function $clinit_Collections(){
  $clinit_Collections = emptyMethod;
  EMPTY_LIST = new Collections$EmptyList;
  EMPTY_SET = new Collections$EmptySet;
}

function hashCode_12(collection){
  $clinit_Collections();
  var e, e$iterator, hashCode;
  hashCode = 0;
  for (e$iterator = collection.iterator(); e$iterator.hasNext_0();) {
    e = e$iterator.next_1();
    hashCode = hashCode + (e != null?hashCode__I__devirtual$(e):0);
    hashCode = hashCode | 0;
  }
  return hashCode;
}

function hashCode_13(list){
  $clinit_Collections();
  var e, e$iterator, hashCode;
  hashCode = 1;
  for (e$iterator = list.iterator(); e$iterator.hasNext_0();) {
    e = e$iterator.next_1();
    hashCode = 31 * hashCode + (e != null?hashCode__I__devirtual$(e):0);
    hashCode = hashCode | 0;
  }
  return hashCode;
}

function unmodifiableList(list){
  $clinit_Collections();
  return instanceOf(list, 51)?new Collections$UnmodifiableRandomAccessList(list):new Collections$UnmodifiableList(list);
}

var EMPTY_LIST, EMPTY_SET;
function Collections$EmptyList(){
}

defineClass(261, 487, $intern_31, Collections$EmptyList);
_.contains = function contains_5(object){
  return false;
}
;
_.getAtIndex = function get_30(location_0){
  checkCriticalElementIndex(location_0, 0);
  return null;
}
;
_.iterator = function iterator_6(){
  return $clinit_Collections() , $clinit_Collections$EmptyListIterator() , INSTANCE_6;
}
;
_.listIterator = function listIterator_1(){
  return $clinit_Collections() , $clinit_Collections$EmptyListIterator() , INSTANCE_6;
}
;
_.size = function size_11(){
  return 0;
}
;
var Ljava_util_Collections$EmptyList_2_classLit = createForClass('java.util', 'Collections/EmptyList', 261);
function $clinit_Collections$EmptyListIterator(){
  $clinit_Collections$EmptyListIterator = emptyMethod;
  INSTANCE_6 = new Collections$EmptyListIterator;
}

function Collections$EmptyListIterator(){
}

defineClass(262, 1, {}, Collections$EmptyListIterator);
_.forEachRemaining = function forEachRemaining_6(consumer){
  $forEachRemaining(this, consumer);
}
;
_.hasNext_0 = function hasNext_7(){
  return false;
}
;
_.next_1 = function next_7(){
  throw toJs(new NoSuchElementException);
}
;
_.remove_0 = function remove_20(){
  throw toJs(new IllegalStateException);
}
;
var INSTANCE_6;
var Ljava_util_Collections$EmptyListIterator_2_classLit = createForClass('java.util', 'Collections/EmptyListIterator', 262);
function Collections$EmptySet(){
}

defineClass(263, 486, {3:1, 24:1, 20:1, 39:1}, Collections$EmptySet);
_.contains = function contains_6(object){
  return false;
}
;
_.iterator = function iterator_7(){
  return $clinit_Collections() , $clinit_Collections$EmptyListIterator() , INSTANCE_6;
}
;
_.size = function size_12(){
  return 0;
}
;
var Ljava_util_Collections$EmptySet_2_classLit = createForClass('java.util', 'Collections/EmptySet', 263);
function Collections$SingletonList(element){
  this.element_0 = element;
}

defineClass(195, 487, {3:1, 24:1, 20:1, 13:1}, Collections$SingletonList);
_.contains = function contains_7(item_0){
  return equals_18(this.element_0, item_0);
}
;
_.getAtIndex = function get_31(index_0){
  checkCriticalElementIndex(index_0, 1);
  return this.element_0;
}
;
_.size = function size_13(){
  return 1;
}
;
var Ljava_util_Collections$SingletonList_2_classLit = createForClass('java.util', 'Collections/SingletonList', 195);
function $contains_2(this$static, o){
  return this$static.coll.contains(o);
}

function Collections$UnmodifiableCollection(coll){
  this.coll = coll;
}

defineClass(142, 1, $intern_17, Collections$UnmodifiableCollection);
_.spliterator_0 = function spliterator_3(){
  return new Spliterators$IteratorSpliterator(this, 0);
}
;
_.stream = function stream_0(){
  return new StreamImpl(null, this.spliterator_0());
}
;
_.add = function add_8(o){
  throw toJs(new UnsupportedOperationException);
}
;
_.addAll = function addAll_5(c){
  throw toJs(new UnsupportedOperationException);
}
;
_.clear = function clear_10(){
  throw toJs(new UnsupportedOperationException);
}
;
_.contains = function contains_8(o){
  return $contains_2(this, o);
}
;
_.containsAll = function containsAll_0(c){
  return this.coll.containsAll(c);
}
;
_.isEmpty = function isEmpty_3(){
  return this.coll.isEmpty();
}
;
_.iterator = function iterator_8(){
  return new Collections$UnmodifiableCollectionIterator(this.coll.iterator());
}
;
_.remove = function remove_21(o){
  throw toJs(new UnsupportedOperationException);
}
;
_.removeAll = function removeAll_2(c){
  throw toJs(new UnsupportedOperationException);
}
;
_.retainAll = function retainAll_1(c){
  throw toJs(new UnsupportedOperationException);
}
;
_.size = function size_14(){
  return this.coll.size();
}
;
_.toArray = function toArray_5(){
  return this.coll.toArray();
}
;
_.toArray_0 = function toArray_6(a){
  return this.coll.toArray_0(a);
}
;
_.toString_0 = function toString_29(){
  return toString_8(this.coll);
}
;
var Ljava_util_Collections$UnmodifiableCollection_2_classLit = createForClass('java.util', 'Collections/UnmodifiableCollection', 142);
function $remove_3(){
  throw toJs(new UnsupportedOperationException);
}

function Collections$UnmodifiableCollectionIterator(it){
  this.it = it;
}

defineClass(166, 1, {}, Collections$UnmodifiableCollectionIterator);
_.forEachRemaining = function forEachRemaining_7(consumer){
  $forEachRemaining(this, consumer);
}
;
_.hasNext_0 = function hasNext_8(){
  return this.it.hasNext_0();
}
;
_.next_1 = function next_8(){
  return this.it.next_1();
}
;
_.remove_0 = function remove_22(){
  $remove_3();
}
;
var Ljava_util_Collections$UnmodifiableCollectionIterator_2_classLit = createForClass('java.util', 'Collections/UnmodifiableCollectionIterator', 166);
function Collections$UnmodifiableList(list){
  Collections$UnmodifiableCollection.call(this, list);
  this.list = list;
}

defineClass(165, 142, $intern_30, Collections$UnmodifiableList);
_.spliterator_0 = function spliterator_4(){
  return new Spliterators$IteratorSpliterator(this, 16);
}
;
_.addAtIndex = function add_9(index_0, element){
  throw toJs(new UnsupportedOperationException);
}
;
_.addAllAtIndex = function addAll_6(index_0, c){
  throw toJs(new UnsupportedOperationException);
}
;
_.equals_0 = function equals_10(o){
  return equals_Ljava_lang_Object__Z__devirtual$(this.list, o);
}
;
_.getAtIndex = function get_32(index_0){
  return this.list.getAtIndex(index_0);
}
;
_.hashCode_0 = function hashCode_14(){
  return hashCode__I__devirtual$(this.list);
}
;
_.indexOf = function indexOf_1(o){
  return this.list.indexOf(o);
}
;
_.isEmpty = function isEmpty_4(){
  return this.list.isEmpty();
}
;
_.lastIndexOf = function lastIndexOf_1(o){
  return this.list.lastIndexOf(o);
}
;
_.listIterator = function listIterator_2(){
  return new Collections$UnmodifiableListIterator(this.list.listIterator_0(0));
}
;
_.listIterator_0 = function listIterator_3(from){
  return new Collections$UnmodifiableListIterator(this.list.listIterator_0(from));
}
;
_.removeAtIndex = function remove_23(index_0){
  throw toJs(new UnsupportedOperationException);
}
;
_.setAtIndex = function set_27(index_0, element){
  throw toJs(new UnsupportedOperationException);
}
;
_.subList = function subList_0(fromIndex, toIndex){
  return new Collections$UnmodifiableList(this.list.subList(fromIndex, toIndex));
}
;
var Ljava_util_Collections$UnmodifiableList_2_classLit = createForClass('java.util', 'Collections/UnmodifiableList', 165);
function Collections$UnmodifiableListIterator(lit){
  Collections$UnmodifiableCollectionIterator.call(this, lit);
}

defineClass(197, 166, {}, Collections$UnmodifiableListIterator);
_.remove_0 = function remove_24(){
  $remove_3();
}
;
var Ljava_util_Collections$UnmodifiableListIterator_2_classLit = createForClass('java.util', 'Collections/UnmodifiableListIterator', 197);
function $put_1(){
  throw toJs(new UnsupportedOperationException);
}

function Collections$UnmodifiableMap(map_0){
  this.map_0 = map_0;
}

defineClass(264, 1, {21:1}, Collections$UnmodifiableMap);
_.getOrDefault = function getOrDefault_1(key, defaultValue){
  var currentValue;
  return currentValue = this.map_0.get(key) , currentValue == null && !this.map_0.containsKey(key)?defaultValue:currentValue;
}
;
_.merge = function merge_2(key, value_0, remappingFunction){
  return $merge(this, key, value_0, remappingFunction);
}
;
_.putIfAbsent = function putIfAbsent_1(key, value_0){
  var currentValue;
  return currentValue = this.map_0.get(key) , currentValue != null?currentValue:$put_1();
}
;
_.replace = function replace_1(key, value_0){
  return this.map_0.containsKey(key)?$put_1():null;
}
;
_.clear = function clear_11(){
  throw toJs(new UnsupportedOperationException);
}
;
_.containsKey = function containsKey_2(key){
  return this.map_0.containsKey(key);
}
;
_.containsValue = function containsValue_2(val){
  return this.map_0.containsValue(val);
}
;
_.entrySet_0 = function entrySet_1(){
  !this.entrySet && (this.entrySet = new Collections$UnmodifiableMap$UnmodifiableEntrySet(this.map_0.entrySet_0()));
  return this.entrySet;
}
;
_.equals_0 = function equals_11(o){
  return equals_Ljava_lang_Object__Z__devirtual$(this.map_0, o);
}
;
_.get = function get_33(key){
  return this.map_0.get(key);
}
;
_.hashCode_0 = function hashCode_15(){
  return hashCode__I__devirtual$(this.map_0);
}
;
_.isEmpty = function isEmpty_5(){
  return this.map_0.isEmpty();
}
;
_.keySet = function keySet_1(){
  !this.keySet_0 && (this.keySet_0 = new Collections$UnmodifiableSet(this.map_0.keySet()));
  return this.keySet_0;
}
;
_.put = function put_3(key, value_0){
  return $put_1();
}
;
_.putAll = function putAll_1(t){
  throw toJs(new UnsupportedOperationException);
}
;
_.remove = function remove_25(key){
  throw toJs(new UnsupportedOperationException);
}
;
_.size = function size_15(){
  return this.map_0.size();
}
;
_.toString_0 = function toString_30(){
  return toString_8(this.map_0);
}
;
_.values = function values_13(){
  !this.values_0 && (this.values_0 = new Collections$UnmodifiableCollection(this.map_0.values()));
  return this.values_0;
}
;
var Ljava_util_Collections$UnmodifiableMap_2_classLit = createForClass('java.util', 'Collections/UnmodifiableMap', 264);
function Collections$UnmodifiableSet(set_0){
  Collections$UnmodifiableCollection.call(this, set_0);
}

defineClass(31, 142, $intern_36, Collections$UnmodifiableSet);
_.spliterator_0 = function spliterator_5(){
  return new Spliterators$IteratorSpliterator(this, 1);
}
;
_.equals_0 = function equals_12(o){
  return equals_Ljava_lang_Object__Z__devirtual$(this.coll, o);
}
;
_.hashCode_0 = function hashCode_16(){
  return hashCode__I__devirtual$(this.coll);
}
;
var Ljava_util_Collections$UnmodifiableSet_2_classLit = createForClass('java.util', 'Collections/UnmodifiableSet', 31);
function $wrap(array, size_0){
  var i;
  for (i = 0; i < size_0; ++i) {
    setCheck(array, i, new Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntry(castTo(array[i], 22)));
  }
}

function Collections$UnmodifiableMap$UnmodifiableEntrySet(s){
  Collections$UnmodifiableSet.call(this, s);
}

defineClass(265, 31, $intern_36, Collections$UnmodifiableMap$UnmodifiableEntrySet);
_.contains = function contains_9(o){
  return this.coll.contains(o);
}
;
_.containsAll = function containsAll_1(o){
  return this.coll.containsAll(o);
}
;
_.iterator = function iterator_9(){
  var it;
  it = this.coll.iterator();
  return new Collections$UnmodifiableMap$UnmodifiableEntrySet$1(it);
}
;
_.toArray = function toArray_7(){
  var array;
  array = this.coll.toArray();
  $wrap(array, array.length);
  return array;
}
;
_.toArray_0 = function toArray_8(a){
  var result;
  result = this.coll.toArray_0(a);
  $wrap(result, this.coll.size());
  return result;
}
;
var Ljava_util_Collections$UnmodifiableMap$UnmodifiableEntrySet_2_classLit = createForClass('java.util', 'Collections/UnmodifiableMap/UnmodifiableEntrySet', 265);
function Collections$UnmodifiableMap$UnmodifiableEntrySet$1(val$it){
  this.val$it2 = val$it;
}

defineClass(267, 1, {}, Collections$UnmodifiableMap$UnmodifiableEntrySet$1);
_.forEachRemaining = function forEachRemaining_8(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_9(){
  return new Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntry(castTo(this.val$it2.next_1(), 22));
}
;
_.hasNext_0 = function hasNext_9(){
  return this.val$it2.hasNext_0();
}
;
_.remove_0 = function remove_26(){
  throw toJs(new UnsupportedOperationException);
}
;
var Ljava_util_Collections$UnmodifiableMap$UnmodifiableEntrySet$1_2_classLit = createForClass('java.util', 'Collections/UnmodifiableMap/UnmodifiableEntrySet/1', 267);
function Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntry(entry){
  this.entry = entry;
}

defineClass(196, 1, $intern_37, Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntry);
_.equals_0 = function equals_13(o){
  return this.entry.equals_0(o);
}
;
_.getKey = function getKey_0(){
  return this.entry.getKey();
}
;
_.getValue = function getValue_0(){
  return this.entry.getValue();
}
;
_.hashCode_0 = function hashCode_17(){
  return this.entry.hashCode_0();
}
;
_.setValue = function setValue_0(value_0){
  throw toJs(new UnsupportedOperationException);
}
;
_.toString_0 = function toString_31(){
  return toString_8(this.entry);
}
;
var Ljava_util_Collections$UnmodifiableMap$UnmodifiableEntrySet$UnmodifiableEntry_2_classLit = createForClass('java.util', 'Collections/UnmodifiableMap/UnmodifiableEntrySet/UnmodifiableEntry', 196);
function Collections$UnmodifiableRandomAccessList(list){
  Collections$UnmodifiableList.call(this, list);
}

defineClass(266, 165, {24:1, 20:1, 13:1, 51:1}, Collections$UnmodifiableRandomAccessList);
var Ljava_util_Collections$UnmodifiableRandomAccessList_2_classLit = createForClass('java.util', 'Collections/UnmodifiableRandomAccessList', 266);
function Comparator$lambda$2$Type(){
}

defineClass(242, 1, $intern_4, Comparator$lambda$2$Type);
_.compare = function compare_6(a, b){
  return compare_3(castTo(a, 8).getY(), castTo(b, 8).getY());
}
;
_.equals_0 = function equals_14(other){
  return this === other;
}
;
var Ljava_util_Comparator$lambda$2$Type_2_classLit = createForClass('java.util', 'Comparator/lambda$2$Type', 242);
function $clinit_Comparators(){
  $clinit_Comparators = emptyMethod;
  INTERNAL_NATURAL_ORDER = new Comparators$NaturalOrderComparator;
}

var INTERNAL_NATURAL_ORDER;
function $compare(a, b){
  return checkCriticalNotNull(a) , compareTo_Ljava_lang_Object__I__devirtual$(a, (checkCriticalNotNull(b) , b));
}

function Comparators$NaturalOrderComparator(){
}

defineClass(449, 1, $intern_4, Comparators$NaturalOrderComparator);
_.compare = function compare_7(a, b){
  return $compare(castTo(a, 33), castTo(b, 33));
}
;
_.equals_0 = function equals_15(other){
  return this === other;
}
;
var Ljava_util_Comparators$NaturalOrderComparator_2_classLit = createForClass('java.util', 'Comparators/NaturalOrderComparator', 449);
function ConcurrentModificationException(){
  RuntimeException.call(this);
}

defineClass(353, 16, $intern_2, ConcurrentModificationException);
var Ljava_util_ConcurrentModificationException_2_classLit = createForClass('java.util', 'ConcurrentModificationException', 353);
function $compareTo_5(this$static, other){
  return compare_5(fromDouble_0(this$static.jsdate.getTime()), fromDouble_0(other.jsdate.getTime()));
}

function $toString_1(this$static){
  var hourOffset, minuteOffset, offset;
  offset = -this$static.jsdate.getTimezoneOffset();
  hourOffset = (offset >= 0?'+':'') + (offset / 60 | 0);
  minuteOffset = padTwo($wnd.Math.abs(offset) % 60);
  return ($clinit_Date$StringData() , DAYS)[this$static.jsdate.getDay()] + ' ' + MONTHS[this$static.jsdate.getMonth()] + ' ' + padTwo(this$static.jsdate.getDate()) + ' ' + padTwo(this$static.jsdate.getHours()) + ':' + padTwo(this$static.jsdate.getMinutes()) + ':' + padTwo(this$static.jsdate.getSeconds()) + ' GMT' + hourOffset + minuteOffset + ' ' + this$static.jsdate.getFullYear();
}

function Date_0(date){
  this.jsdate = new $wnd.Date(toDouble_0(date));
}

function padTwo(number){
  return number < 10?'0' + number:'' + number;
}

defineClass(133, 1, {3:1, 33:1, 133:1}, Date_0);
_.compareTo_0 = function compareTo_3(other){
  return $compareTo_5(this, castTo(other, 133));
}
;
_.equals_0 = function equals_16(obj){
  return instanceOf(obj, 133) && eq(fromDouble_0(this.jsdate.getTime()), fromDouble_0(castTo(obj, 133).jsdate.getTime()));
}
;
_.hashCode_0 = function hashCode_18(){
  var time;
  time = fromDouble_0(this.jsdate.getTime());
  return toInt_0(xor_0(time, createLongEmul(shru(isSmallLong0(time)?toBigLong(time):time, 32))));
}
;
_.toString_0 = function toString_32(){
  return $toString_1(this);
}
;
var Ljava_util_Date_2_classLit = createForClass('java.util', 'Date', 133);
function $clinit_Date$StringData(){
  $clinit_Date$StringData = emptyMethod;
  DAYS = stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']);
  MONTHS = stampJavaTypeInfo(getClassLiteralForArray(Ljava_lang_String_2_classLit, 1), $intern_3, 2, 6, ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']);
}

var DAYS, MONTHS;
function HashMap(){
  $reset(this);
}

function HashMap_0(ignored){
  checkCriticalArgument(ignored >= 0, 'Negative initial capacity');
  checkCriticalArgument(true, 'Non-positive load factor');
  $reset(this);
}

defineClass(26, 163, {3:1, 21:1}, HashMap, HashMap_0);
_.equals_1 = function equals_17(value1, value2){
  return maskUndefined(value1) === maskUndefined(value2) || value1 != null && equals_Ljava_lang_Object__Z__devirtual$(value1, value2);
}
;
_.getHashCode = function getHashCode(key){
  var hashCode;
  if (key == null) {
    return 0;
  }
  hashCode = hashCode__I__devirtual$(key);
  return hashCode | 0;
}
;
var Ljava_util_HashMap_2_classLit = createForClass('java.util', 'HashMap', 26);
function $add_2(this$static, o){
  var old;
  old = this$static.map_0.put(o, this$static);
  return old == null;
}

function $contains_3(this$static, o){
  return this$static.map_0.containsKey(o);
}

function HashSet(){
  this.map_0 = new HashMap;
}

function HashSet_0(){
  this.map_0 = new HashMap_0(1);
}

function HashSet_1(c){
  this.map_0 = new HashMap_0(c.size());
  $addAll(this, c);
}

function HashSet_2(map_0){
  this.map_0 = map_0;
}

defineClass(17, 486, $intern_38, HashSet, HashSet_0, HashSet_1);
_.add = function add_10(o){
  return $add_2(this, o);
}
;
_.clear = function clear_12(){
  this.map_0.clear();
}
;
_.contains = function contains_10(o){
  return $contains_3(this, o);
}
;
_.isEmpty = function isEmpty_6(){
  return this.map_0.size() == 0;
}
;
_.iterator = function iterator_10(){
  var outerIter;
  return outerIter = (new AbstractMap$1(this.map_0)).this$01.entrySet_0().iterator() , new AbstractMap$1$1(outerIter);
}
;
_.remove = function remove_27(o){
  return this.map_0.remove(o) != null;
}
;
_.size = function size_16(){
  return this.map_0.size();
}
;
var Ljava_util_HashSet_2_classLit = createForClass('java.util', 'HashSet', 17);
function $findEntryInChain(this$static, key, chain){
  var entry, entry$array, entry$index, entry$max;
  for (entry$array = chain , entry$index = 0 , entry$max = entry$array.length; entry$index < entry$max; ++entry$index) {
    entry = entry$array[entry$index];
    if (this$static.host.equals_1(key, entry.getKey())) {
      return entry;
    }
  }
  return null;
}

function $getChainOrEmpty(this$static, hashCode){
  var chain;
  chain = this$static.backingMap.get(hashCode);
  return chain == null?initUnidimensionalArray(Ljava_lang_Object_2_classLit, $intern_0, 1, 0, 5, 1):chain;
}

function $getEntry(this$static, key){
  return $findEntryInChain(this$static, key, $getChainOrEmpty(this$static, this$static.host.getHashCode(key)));
}

function $put_2(this$static, key, value_0){
  var chain, chain0, entry, hashCode;
  hashCode = this$static.host.getHashCode(key);
  chain0 = (chain = this$static.backingMap.get(hashCode) , chain == null?initUnidimensionalArray(Ljava_lang_Object_2_classLit, $intern_0, 1, 0, 5, 1):chain);
  if (chain0.length == 0) {
    this$static.backingMap.set(hashCode, chain0);
  }
   else {
    entry = $findEntryInChain(this$static, key, chain0);
    if (entry) {
      return entry.setValue(value_0);
    }
  }
  setCheck(chain0, chain0.length, new AbstractMap$SimpleEntry(key, value_0));
  ++this$static.size_0;
  ++this$static.host.modCount;
  return null;
}

function $remove_4(this$static, key){
  var chain, chain0, entry, hashCode, i;
  hashCode = this$static.host.getHashCode(key);
  chain0 = (chain = this$static.backingMap.get(hashCode) , chain == null?initUnidimensionalArray(Ljava_lang_Object_2_classLit, $intern_0, 1, 0, 5, 1):chain);
  for (i = 0; i < chain0.length; i++) {
    entry = chain0[i];
    if (this$static.host.equals_1(key, entry.getKey())) {
      if (chain0.length == 1) {
        chain0.length = 0;
        $delete_1(this$static.backingMap, hashCode);
      }
       else {
        chain0.splice(i, 1);
      }
      --this$static.size_0;
      ++this$static.host.modCount;
      return entry.getValue();
    }
  }
  return null;
}

function InternalHashCodeMap(host){
  this.backingMap = newJsMap();
  this.host = host;
}

defineClass(271, 1, $intern_23, InternalHashCodeMap);
_.iterator = function iterator_11(){
  return new InternalHashCodeMap$1(this);
}
;
_.size_0 = 0;
var Ljava_util_InternalHashCodeMap_2_classLit = createForClass('java.util', 'InternalHashCodeMap', 271);
function InternalHashCodeMap$1(this$0){
  this.this$01 = this$0;
  this.chains = this.this$01.backingMap.entries();
  this.chain = initUnidimensionalArray(Ljava_lang_Object_2_classLit, $intern_0, 1, 0, 5, 1);
}

defineClass(199, 1, {}, InternalHashCodeMap$1);
_.forEachRemaining = function forEachRemaining_9(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_10(){
  return this.lastEntry = this.chain[this.itemIndex++] , this.lastEntry;
}
;
_.hasNext_0 = function hasNext_10(){
  var current;
  if (this.itemIndex < this.chain.length) {
    return true;
  }
  current = this.chains.next();
  if (!current.done) {
    this.chain = current.value[1];
    this.itemIndex = 0;
    return true;
  }
  return false;
}
;
_.remove_0 = function remove_28(){
  $remove_4(this.this$01, this.lastEntry.getKey());
  this.itemIndex != 0 && --this.itemIndex;
}
;
_.itemIndex = 0;
_.lastEntry = null;
var Ljava_util_InternalHashCodeMap$1_2_classLit = createForClass('java.util', 'InternalHashCodeMap/1', 199);
function $delete_1(this$static, key){
  var fn;
  fn = this$static['delete'];
  fn.call(this$static, key);
}

function $delete_2(this$static, key){
  var fn;
  fn = this$static['delete'];
  fn.call(this$static, key);
}

function $clinit_InternalJsMapFactory(){
  $clinit_InternalJsMapFactory = emptyMethod;
  jsMapCtor = getJsMapConstructor();
}

function canHandleObjectCreateAndProto(){
  if (!Object.create || !Object.getOwnPropertyNames) {
    return false;
  }
  var protoField = '__proto__';
  var map_0 = Object.create(null);
  if (map_0[protoField] !== undefined) {
    return false;
  }
  var keys_0 = Object.getOwnPropertyNames(map_0);
  if (keys_0.length != 0) {
    return false;
  }
  map_0[protoField] = 42;
  if (map_0[protoField] !== 42) {
    return false;
  }
  if (Object.getOwnPropertyNames(map_0).length == 0) {
    return false;
  }
  return true;
}

function getJsMapConstructor(){
  function isCorrectIterationProtocol(){
    try {
      return (new Map).entries().next().done;
    }
     catch (e) {
      return false;
    }
  }

  if (typeof Map === 'function' && Map.prototype.entries && isCorrectIterationProtocol()) {
    return Map;
  }
   else {
    return getJsMapPolyFill();
  }
}

function getJsMapPolyFill(){
  function Stringmap(){
    this.obj = this.createObject();
  }

  ;
  Stringmap.prototype.createObject = function(key){
    return Object.create(null);
  }
  ;
  Stringmap.prototype.get = function(key){
    return this.obj[key];
  }
  ;
  Stringmap.prototype.set = function(key, value_0){
    this.obj[key] = value_0;
  }
  ;
  Stringmap.prototype['delete'] = function(key){
    delete this.obj[key];
  }
  ;
  Stringmap.prototype.keys = function(){
    return Object.getOwnPropertyNames(this.obj);
  }
  ;
  Stringmap.prototype.entries = function(){
    var keys_0 = this.keys();
    var map_0 = this;
    var nextIndex = 0;
    return {next:function(){
      if (nextIndex >= keys_0.length)
        return {done:true};
      var key = keys_0[nextIndex++];
      return {value:[key, map_0.get(key)], done:false};
    }
    };
  }
  ;
  if (!canHandleObjectCreateAndProto()) {
    Stringmap.prototype.createObject = function(){
      return {};
    }
    ;
    Stringmap.prototype.get = function(key){
      return this.obj[':' + key];
    }
    ;
    Stringmap.prototype.set = function(key, value_0){
      this.obj[':' + key] = value_0;
    }
    ;
    Stringmap.prototype['delete'] = function(key){
      delete this.obj[':' + key];
    }
    ;
    Stringmap.prototype.keys = function(){
      var result = [];
      for (var key in this.obj) {
        key.charCodeAt(0) == 58 && result.push(key.substring(1));
      }
      return result;
    }
    ;
  }
  return Stringmap;
}

function newJsMap(){
  $clinit_InternalJsMapFactory();
  return new jsMapCtor;
}

var jsMapCtor;
function $contains_4(this$static, key){
  return !(this$static.backingMap.get(key) === undefined);
}

function $get_7(this$static, key){
  return this$static.backingMap.get(key);
}

function $put_3(this$static, key, value_0){
  var oldValue;
  oldValue = this$static.backingMap.get(key);
  this$static.backingMap.set(key, value_0 === undefined?null:value_0);
  if (oldValue === undefined) {
    ++this$static.size_0;
    ++this$static.host.modCount;
  }
   else {
    ++this$static.valueMod;
  }
  return oldValue;
}

function $remove_5(this$static, key){
  var value_0;
  value_0 = this$static.backingMap.get(key);
  if (value_0 === undefined) {
    ++this$static.valueMod;
  }
   else {
    $delete_2(this$static.backingMap, key);
    --this$static.size_0;
    ++this$static.host.modCount;
  }
  return value_0;
}

function InternalStringMap(host){
  this.backingMap = newJsMap();
  this.host = host;
}

defineClass(269, 1, $intern_23, InternalStringMap);
_.iterator = function iterator_12(){
  return new InternalStringMap$1(this);
}
;
_.size_0 = 0;
_.valueMod = 0;
var Ljava_util_InternalStringMap_2_classLit = createForClass('java.util', 'InternalStringMap', 269);
function InternalStringMap$1(this$0){
  this.this$01 = this$0;
  this.entries_0 = this.this$01.backingMap.entries();
  this.current = this.entries_0.next();
}

defineClass(198, 1, {}, InternalStringMap$1);
_.forEachRemaining = function forEachRemaining_10(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_11(){
  return this.last = this.current , this.current = this.entries_0.next() , new InternalStringMap$2(this.this$01, this.last, this.this$01.valueMod);
}
;
_.hasNext_0 = function hasNext_11(){
  return !this.current.done;
}
;
_.remove_0 = function remove_29(){
  $remove_5(this.this$01, this.last.value[0]);
}
;
var Ljava_util_InternalStringMap$1_2_classLit = createForClass('java.util', 'InternalStringMap/1', 198);
function $getValue(this$static){
  if (this$static.this$01.valueMod != this$static.val$lastValueMod3) {
    return $get_7(this$static.this$01, this$static.val$entry2.value[0]);
  }
  return this$static.val$entry2.value[1];
}

function InternalStringMap$2(this$0, val$entry, val$lastValueMod){
  this.this$01 = this$0;
  this.val$entry2 = val$entry;
  this.val$lastValueMod3 = val$lastValueMod;
}

defineClass(270, 488, $intern_37, InternalStringMap$2);
_.getKey = function getKey_1(){
  return this.val$entry2.value[0];
}
;
_.getValue = function getValue_1(){
  return $getValue(this);
}
;
_.setValue = function setValue_1(object){
  return $put_3(this.this$01, this.val$entry2.value[0], object);
}
;
_.val$lastValueMod3 = 0;
var Ljava_util_InternalStringMap$2_2_classLit = createForClass('java.util', 'InternalStringMap/2', 270);
function $clear_1(this$static){
  $reset(this$static.map_0);
  this$static.head.prev = this$static.head;
  this$static.head.next_0 = this$static.head;
}

function $containsKey_0(this$static, key){
  return $containsKey(this$static.map_0, key);
}

function $get_8(this$static, key){
  var entry;
  entry = castTo($get_6(this$static.map_0, key), 100);
  if (entry) {
    $recordAccess(this$static, entry);
    return entry.value_0;
  }
  return null;
}

function $put_4(this$static, key, value_0){
  var newEntry, old, oldValue;
  old = castTo($get_6(this$static.map_0, key), 100);
  if (!old) {
    newEntry = new LinkedHashMap$ChainEntry_0(this$static, key, value_0);
    $put_0(this$static.map_0, key, newEntry);
    $addToEnd(newEntry);
    return null;
  }
   else {
    oldValue = $setValue_1(old, value_0);
    $recordAccess(this$static, old);
    return oldValue;
  }
}

function $recordAccess(this$static, entry){
  if (this$static.accessOrder) {
    $remove_8(entry);
    $addToEnd(entry);
  }
}

function $remove_7(this$static, key){
  var entry;
  entry = castTo($remove_1(this$static.map_0, key), 100);
  if (entry) {
    $remove_8(entry);
    return entry.value_0;
  }
  return null;
}

function LinkedHashMap(){
  $reset(this);
  this.head = new LinkedHashMap$ChainEntry(this);
  this.map_0 = new HashMap;
  this.head.prev = this.head;
  this.head.next_0 = this.head;
}

defineClass(115, 26, {3:1, 21:1}, LinkedHashMap);
_.clear = function clear_13(){
  $clear_1(this);
}
;
_.containsKey = function containsKey_3(key){
  return $containsKey_0(this, key);
}
;
_.containsValue = function containsValue_3(value_0){
  var node;
  node = this.head.next_0;
  while (node != this.head) {
    if (equals_18(node.value_0, value_0)) {
      return true;
    }
    node = node.next_0;
  }
  return false;
}
;
_.entrySet_0 = function entrySet_2(){
  return new LinkedHashMap$EntrySet(this);
}
;
_.get = function get_34(key){
  return $get_8(this, key);
}
;
_.put = function put_4(key, value_0){
  return $put_4(this, key, value_0);
}
;
_.remove = function remove_30(key){
  return $remove_7(this, key);
}
;
_.size = function size_17(){
  return $size(this.map_0);
}
;
_.accessOrder = false;
var Ljava_util_LinkedHashMap_2_classLit = createForClass('java.util', 'LinkedHashMap', 115);
function $addToEnd(this$static){
  var tail;
  tail = this$static.this$01.head.prev;
  this$static.prev = tail;
  this$static.next_0 = this$static.this$01.head;
  tail.next_0 = this$static.this$01.head.prev = this$static;
}

function $remove_8(this$static){
  this$static.next_0.prev = this$static.prev;
  this$static.prev.next_0 = this$static.next_0;
  this$static.next_0 = this$static.prev = null;
}

function LinkedHashMap$ChainEntry(this$0){
  LinkedHashMap$ChainEntry_0.call(this, this$0, null, null);
}

function LinkedHashMap$ChainEntry_0(this$0, key, value_0){
  this.this$01 = this$0;
  AbstractMap$SimpleEntry.call(this, key, value_0);
}

defineClass(100, 164, {100:1, 22:1}, LinkedHashMap$ChainEntry, LinkedHashMap$ChainEntry_0);
var Ljava_util_LinkedHashMap$ChainEntry_2_classLit = createForClass('java.util', 'LinkedHashMap/ChainEntry', 100);
function $contains_5(this$static, o){
  if (instanceOf(o, 22)) {
    return $containsEntry(this$static.this$01, castTo(o, 22));
  }
  return false;
}

function LinkedHashMap$EntrySet(this$0){
  this.this$01 = this$0;
}

defineClass(172, 486, $intern_36, LinkedHashMap$EntrySet);
_.clear = function clear_14(){
  $clear_1(this.this$01);
}
;
_.contains = function contains_11(o){
  return $contains_5(this, o);
}
;
_.iterator = function iterator_13(){
  return new LinkedHashMap$EntrySet$EntryIterator(this);
}
;
_.remove = function remove_31(entry){
  var key;
  if ($contains_5(this, entry)) {
    key = castTo(entry, 22).getKey();
    $remove_7(this.this$01, key);
    return true;
  }
  return false;
}
;
_.size = function size_18(){
  return $size(this.this$01.map_0);
}
;
var Ljava_util_LinkedHashMap$EntrySet_2_classLit = createForClass('java.util', 'LinkedHashMap/EntrySet', 172);
function $next_2(this$static){
  checkCriticalConcurrentModification(this$static.this$11.this$01.map_0.modCount, this$static.lastModCount);
  checkCriticalElement(this$static.next_0 != this$static.this$11.this$01.head);
  this$static.last = this$static.next_0;
  this$static.next_0 = this$static.next_0.next_0;
  return this$static.last;
}

function LinkedHashMap$EntrySet$EntryIterator(this$1){
  this.this$11 = this$1;
  this.next_0 = this$1.this$01.head.next_0;
  this.lastModCount = this$1.this$01.map_0.modCount;
}

defineClass(214, 1, {}, LinkedHashMap$EntrySet$EntryIterator);
_.forEachRemaining = function forEachRemaining_11(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_12(){
  return $next_2(this);
}
;
_.hasNext_0 = function hasNext_12(){
  return this.next_0 != this.this$11.this$01.head;
}
;
_.remove_0 = function remove_32(){
  checkCriticalState(!!this.last);
  checkCriticalConcurrentModification(this.this$11.this$01.map_0.modCount, this.lastModCount);
  $remove_8(this.last);
  $remove_1(this.this$11.this$01.map_0, this.last.key);
  this.lastModCount = this.this$11.this$01.map_0.modCount;
  this.last = null;
}
;
_.lastModCount = 0;
var Ljava_util_LinkedHashMap$EntrySet$EntryIterator_2_classLit = createForClass('java.util', 'LinkedHashMap/EntrySet/EntryIterator', 214);
function LinkedHashSet(){
  HashSet_2.call(this, new LinkedHashMap);
}

defineClass(418, 17, $intern_38, LinkedHashSet);
var Ljava_util_LinkedHashSet_2_classLit = createForClass('java.util', 'LinkedHashSet', 418);
function $clinit_Locale(){
  $clinit_Locale = emptyMethod;
  ROOT = new Locale$1;
  defaultLocale = new Locale$4;
}

defineClass(466, 1, {});
var ROOT, defaultLocale;
var Ljava_util_Locale_2_classLit = createForClass('java.util', 'Locale', 466);
function Locale$1(){
}

defineClass(244, 466, {}, Locale$1);
_.toString_0 = function toString_33(){
  return '';
}
;
var Ljava_util_Locale$1_2_classLit = createForClass('java.util', 'Locale/1', 244);
function Locale$4(){
}

defineClass(245, 466, {}, Locale$4);
_.toString_0 = function toString_34(){
  return 'unknown';
}
;
var Ljava_util_Locale$4_2_classLit = createForClass('java.util', 'Locale/4', 245);
function NoSuchElementException(){
  RuntimeException.call(this);
}

defineClass(202, 16, $intern_2, NoSuchElementException);
var Ljava_util_NoSuchElementException_2_classLit = createForClass('java.util', 'NoSuchElementException', 202);
function equals_18(a, b){
  return maskUndefined(a) === maskUndefined(b) || a != null && equals_Ljava_lang_Object__Z__devirtual$(a, b);
}

function hashCode_19(o){
  return o != null?hashCode__I__devirtual$(o):0;
}

function requireNonNull(obj){
  if (obj == null) {
    throw toJs(new NullPointerException);
  }
  return obj;
}

function requireNonNull_0(obj, message){
  if (obj == null) {
    throw toJs(new NullPointerException_1(message));
  }
  return obj;
}

function $clinit_Optional(){
  $clinit_Optional = emptyMethod;
  EMPTY = new Optional(null);
}

function $ifPresent(this$static, consumer){
  this$static.ref != null && consumer.accept(this$static.ref);
}

function $map(this$static, mapper){
  checkCriticalNotNull(mapper);
  if (this$static.ref != null) {
    return ofNullable($apply(this$static.ref));
  }
  return EMPTY;
}

function $orElse(this$static, other){
  return this$static.ref != null?this$static.ref:other;
}

function Optional(ref){
  $clinit_Optional();
  this.ref = ref;
}

function ofNullable(value_0){
  $clinit_Optional();
  return value_0 == null?EMPTY:new Optional(checkCriticalNotNull(value_0));
}

defineClass(71, 1, {71:1}, Optional);
_.equals_0 = function equals_19(obj){
  var other;
  if (obj === this) {
    return true;
  }
  if (!instanceOf(obj, 71)) {
    return false;
  }
  other = castTo(obj, 71);
  return equals_18(this.ref, other.ref);
}
;
_.hashCode_0 = function hashCode_20(){
  return hashCode_19(this.ref);
}
;
_.toString_0 = function toString_35(){
  return this.ref != null?'Optional.of(' + valueOf_0(this.ref) + ')':'Optional.empty()';
}
;
var EMPTY;
var Ljava_util_Optional_2_classLit = createForClass('java.util', 'Optional', 71);
function $forEachRemaining_0(this$static, consumer){
  while (this$static.tryAdvance(consumer))
  ;
}

defineClass(241, 1, {});
_.forEachRemaining = function forEachRemaining_12(consumer){
  $forEachRemaining_0(this, consumer);
}
;
_.characteristics_0 = function characteristics_0(){
  return this.characteristics;
}
;
_.estimateSize_0 = function estimateSize(){
  return this.sizeEstimate;
}
;
_.characteristics = 0;
_.sizeEstimate = 0;
var Ljava_util_Spliterators$BaseSpliterator_2_classLit = createForClass('java.util', 'Spliterators/BaseSpliterator', 241);
function Spliterators$AbstractSpliterator(size_0, characteristics){
  this.sizeEstimate = size_0;
  this.characteristics = (characteristics & 64) != 0?characteristics | 16384:characteristics;
}

defineClass(160, 241, {});
var Ljava_util_Spliterators$AbstractSpliterator_2_classLit = createForClass('java.util', 'Spliterators/AbstractSpliterator', 160);
function $initIterator(this$static){
  if (!this$static.it) {
    this$static.it = this$static.collection.iterator();
    this$static.estimateSize = this$static.collection.size();
  }
}

function Spliterators$IteratorSpliterator(collection, characteristics){
  this.collection = (checkCriticalNotNull(collection) , collection);
  this.characteristics = (characteristics & 4096) == 0?characteristics | 64 | 16384:characteristics;
}

defineClass(35, 1, {}, Spliterators$IteratorSpliterator);
_.characteristics_0 = function characteristics_1(){
  return this.characteristics;
}
;
_.estimateSize_0 = function estimateSize_0(){
  $initIterator(this);
  return this.estimateSize;
}
;
_.forEachRemaining = function forEachRemaining_13(consumer){
  $initIterator(this);
  this.it.forEachRemaining(consumer);
}
;
_.tryAdvance = function tryAdvance(consumer){
  checkCriticalNotNull(consumer);
  $initIterator(this);
  if (this.it.hasNext_0()) {
    consumer.accept(this.it.next_1());
    return true;
  }
  return false;
}
;
_.characteristics = 0;
_.estimateSize = 0;
var Ljava_util_Spliterators$IteratorSpliterator_2_classLit = createForClass('java.util', 'Spliterators/IteratorSpliterator', 35);
function $add_3(this$static, newElement){
  !this$static.builder?(this$static.builder = new StringBuilder_1(this$static.prefix)):$append_7(this$static.builder, this$static.delimiter);
  $append_4(this$static.builder, newElement);
  return this$static;
}

function $merge_0(this$static, other){
  var otherLength;
  if (other.builder) {
    otherLength = other.builder.string.length;
    !this$static.builder?(this$static.builder = new StringBuilder_1(this$static.prefix)):$append_7(this$static.builder, this$static.delimiter);
    $append_6(this$static.builder, other.builder, other.prefix.length, otherLength);
  }
  return this$static;
}

function $toString_2(this$static){
  return !this$static.builder?this$static.emptyValue:this$static.suffix.length == 0?this$static.builder.string:this$static.builder.string + ('' + this$static.suffix);
}

function StringJoiner(delimiter, prefix, suffix){
  this.delimiter = (checkCriticalNotNull(delimiter) , delimiter);
  this.prefix = (checkCriticalNotNull(prefix) , prefix);
  this.suffix = (checkCriticalNotNull(suffix) , suffix);
  this.emptyValue = this.prefix + ('' + this.suffix);
}

defineClass(84, 1, {84:1}, StringJoiner);
_.toString_0 = function toString_36(){
  return $toString_2(this);
}
;
var Ljava_util_StringJoiner_2_classLit = createForClass('java.util', 'StringJoiner', 84);
function BinaryOperator$lambda$0$Type(){
}

defineClass(284, 1, {}, BinaryOperator$lambda$0$Type);
_.apply_1 = function apply_24(arg0, arg1){
  return compare_3($doubleValue(castToDouble(arg0)), $doubleValue(castToDouble(arg1))) <= 0?arg1:arg0;
}
;
var Ljava_util_function_BinaryOperator$lambda$0$Type_2_classLit = createForClass('java.util.function', 'BinaryOperator/lambda$0$Type', 284);
function Function$lambda$0$Type(){
}

defineClass(255, 1, {}, Function$lambda$0$Type);
_.apply_0 = function apply_25(t){
  return t;
}
;
var Ljava_util_function_Function$lambda$0$Type_2_classLit = createForClass('java.util.function', 'Function/lambda$0$Type', 255);
function $clinit_Level(){
  $clinit_Level = emptyMethod;
  ALL = new Level$LevelAll;
  CONFIG = new Level$LevelConfig;
  FINE = new Level$LevelFine;
  FINER = new Level$LevelFiner;
  FINEST = new Level$LevelFinest;
  INFO = new Level$LevelInfo;
  OFF = new Level$LevelOff;
  SEVERE = new Level$LevelSevere;
  WARNING = new Level$LevelWarning;
}

function parse_0(name_0){
  $clinit_Level();
  var value_0;
  value_0 = $toUpperCase(name_0, ($clinit_Locale() , ROOT));
  switch (value_0) {
    case 'ALL':
      return ALL;
    case 'CONFIG':
      return CONFIG;
    case 'FINE':
      return FINE;
    case 'FINER':
      return FINER;
    case 'FINEST':
      return FINEST;
    case 'INFO':
      return INFO;
    case 'OFF':
      return OFF;
    case 'SEVERE':
      return SEVERE;
    case 'WARNING':
      return WARNING;
    default:throw toJs(new IllegalArgumentException('Invalid level "' + name_0 + '"'));
  }
}

defineClass(489, 1, $intern_4);
_.getName = function getName_0(){
  return 'DUMMY';
}
;
_.intValue = function intValue(){
  return -1;
}
;
_.toString_0 = function toString_37(){
  return this.getName();
}
;
var ALL, CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING;
var Ljava_util_logging_Level_2_classLit = createForClass('java.util.logging', 'Level', 489);
function Level$LevelAll(){
}

defineClass(285, 489, $intern_4, Level$LevelAll);
_.getName = function getName_1(){
  return 'ALL';
}
;
_.intValue = function intValue_0(){
  return -2147483648;
}
;
var Ljava_util_logging_Level$LevelAll_2_classLit = createForClass('java.util.logging', 'Level/LevelAll', 285);
function Level$LevelConfig(){
}

defineClass(286, 489, $intern_4, Level$LevelConfig);
_.getName = function getName_2(){
  return 'CONFIG';
}
;
_.intValue = function intValue_1(){
  return 700;
}
;
var Ljava_util_logging_Level$LevelConfig_2_classLit = createForClass('java.util.logging', 'Level/LevelConfig', 286);
function Level$LevelFine(){
}

defineClass(287, 489, $intern_4, Level$LevelFine);
_.getName = function getName_3(){
  return 'FINE';
}
;
_.intValue = function intValue_2(){
  return 500;
}
;
var Ljava_util_logging_Level$LevelFine_2_classLit = createForClass('java.util.logging', 'Level/LevelFine', 287);
function Level$LevelFiner(){
}

defineClass(288, 489, $intern_4, Level$LevelFiner);
_.getName = function getName_4(){
  return 'FINER';
}
;
_.intValue = function intValue_3(){
  return 400;
}
;
var Ljava_util_logging_Level$LevelFiner_2_classLit = createForClass('java.util.logging', 'Level/LevelFiner', 288);
function Level$LevelFinest(){
}

defineClass(289, 489, $intern_4, Level$LevelFinest);
_.getName = function getName_5(){
  return 'FINEST';
}
;
_.intValue = function intValue_4(){
  return 300;
}
;
var Ljava_util_logging_Level$LevelFinest_2_classLit = createForClass('java.util.logging', 'Level/LevelFinest', 289);
function Level$LevelInfo(){
}

defineClass(290, 489, $intern_4, Level$LevelInfo);
_.getName = function getName_6(){
  return 'INFO';
}
;
_.intValue = function intValue_5(){
  return 800;
}
;
var Ljava_util_logging_Level$LevelInfo_2_classLit = createForClass('java.util.logging', 'Level/LevelInfo', 290);
function Level$LevelOff(){
}

defineClass(291, 489, $intern_4, Level$LevelOff);
_.getName = function getName_7(){
  return 'OFF';
}
;
_.intValue = function intValue_6(){
  return $intern_1;
}
;
var Ljava_util_logging_Level$LevelOff_2_classLit = createForClass('java.util.logging', 'Level/LevelOff', 291);
function Level$LevelSevere(){
}

defineClass(292, 489, $intern_4, Level$LevelSevere);
_.getName = function getName_8(){
  return 'SEVERE';
}
;
_.intValue = function intValue_7(){
  return 1000;
}
;
var Ljava_util_logging_Level$LevelSevere_2_classLit = createForClass('java.util.logging', 'Level/LevelSevere', 292);
function Level$LevelWarning(){
}

defineClass(293, 489, $intern_4, Level$LevelWarning);
_.getName = function getName_9(){
  return 'WARNING';
}
;
_.intValue = function intValue_8(){
  return 900;
}
;
var Ljava_util_logging_Level$LevelWarning_2_classLit = createForClass('java.util.logging', 'Level/LevelWarning', 293);
function $addLoggerImpl(this$static, logger){
  $putStringValue(this$static.loggerMap, ($clinit_Logger() , LOGGING_OFF)?null:logger.name_0, logger);
}

function $ensureLogger(this$static, name_0){
  var logger, newLogger, name_1, parentName;
  logger = castTo($getStringValue(this$static.loggerMap, name_0), 125);
  if (!logger) {
    newLogger = new Logger(name_0);
    name_1 = ($clinit_Logger() , LOGGING_OFF)?null:newLogger.name_0;
    parentName = $substring_0(name_1, 0, $wnd.Math.max(0, $lastIndexOf_0(name_1, fromCodePoint(46))));
    $setParent_1(newLogger, $ensureLogger(this$static, parentName));
    $putStringValue(this$static.loggerMap, LOGGING_OFF?null:newLogger.name_0, newLogger);
    return newLogger;
  }
  return logger;
}

function LogManager(){
  this.loggerMap = new HashMap;
}

function getLogManager(){
  var rootLogger;
  if (!singleton) {
    singleton = new LogManager;
    rootLogger = new Logger('');
    $setLevel_0(rootLogger, ($clinit_Level() , INFO));
    $addLoggerImpl(singleton, rootLogger);
  }
  return singleton;
}

defineClass(273, 1, {}, LogManager);
var singleton;
var Ljava_util_logging_LogManager_2_classLit = createForClass('java.util.logging', 'LogManager', 273);
function $setLoggerName(this$static, newName){
  this$static.loggerName = newName;
}

function LogRecord(msg){
  this.msg = msg;
  this.millis = ($clinit_System() , fromDouble_0(Date.now()));
}

defineClass(330, 1, $intern_4, LogRecord);
_.loggerName = '';
_.millis = 0;
_.thrown = null;
var Ljava_util_logging_LogRecord_2_classLit = createForClass('java.util.logging', 'LogRecord', 330);
function $clinit_Logger(){
  $clinit_Logger = emptyMethod;
  LOGGING_OFF = false;
  ALL_ENABLED = false;
  INFO_ENABLED = false;
  WARNING_ENABLED = false;
  SEVERE_ENABLED = true;
}

function $actuallyLog(this$static, record){
  var handler, handler$array, handler$array0, handler$index, handler$index0, handler$max, handler$max0, logger;
  for (handler$array0 = $getHandlers(this$static) , handler$index0 = 0 , handler$max0 = handler$array0.length; handler$index0 < handler$max0; ++handler$index0) {
    handler = handler$array0[handler$index0];
    handler.publish(record);
  }
  logger = !LOGGING_OFF && this$static.useParentHandlers?LOGGING_OFF?null:this$static.parent_0:null;
  while (logger) {
    for (handler$array = $getHandlers(logger) , handler$index = 0 , handler$max = handler$array.length; handler$index < handler$max; ++handler$index) {
      handler = handler$array[handler$index];
      handler.publish(record);
    }
    logger = !LOGGING_OFF && logger.useParentHandlers?LOGGING_OFF?null:logger.parent_0:null;
  }
}

function $addHandler(this$static, handler){
  if (LOGGING_OFF) {
    return;
  }
  $add_0(this$static.handlers, handler);
}

function $getEffectiveLevel(this$static){
  var effectiveLevel, logger;
  if (this$static.level) {
    return this$static.level;
  }
  logger = LOGGING_OFF?null:this$static.parent_0;
  while (logger) {
    effectiveLevel = LOGGING_OFF?null:logger.level;
    if (effectiveLevel) {
      return effectiveLevel;
    }
    logger = LOGGING_OFF?null:logger.parent_0;
  }
  return $clinit_Level() , INFO;
}

function $getHandlers(this$static){
  if (LOGGING_OFF) {
    return initUnidimensionalArray(Ljava_util_logging_Handler_2_classLit, $intern_39, 126, 0, 0, 1);
  }
  return castTo($toArray(this$static.handlers, initUnidimensionalArray(Ljava_util_logging_Handler_2_classLit, $intern_39, 126, this$static.handlers.array.length, 0, 1)), 459);
}

function $log(this$static, msg, thrown){
  var record;
  (ALL_ENABLED?1000 >= $getEffectiveLevel(this$static).intValue():INFO_ENABLED?($clinit_Level() , true):WARNING_ENABLED?($clinit_Level() , true):SEVERE_ENABLED && ($clinit_Level() , true)) && (record = new LogRecord(msg) , record.thrown = thrown , $setLoggerName(record, LOGGING_OFF?null:this$static.name_0) , $actuallyLog(this$static, record) , undefined);
}

function $setLevel_0(this$static, newLevel){
  if (LOGGING_OFF) {
    return;
  }
  this$static.level = newLevel;
}

function $setParent_1(this$static, newParent){
  if (LOGGING_OFF) {
    return;
  }
  !!newParent && (this$static.parent_0 = newParent);
}

function $setUseParentHandlers(this$static){
  if (LOGGING_OFF) {
    return;
  }
  this$static.useParentHandlers = false;
}

function Logger(name_0){
  $clinit_Logger();
  if (LOGGING_OFF) {
    return;
  }
  this.name_0 = name_0;
  this.useParentHandlers = true;
  this.handlers = new ArrayList;
}

function getLogger(name_0){
  $clinit_Logger();
  if (LOGGING_OFF) {
    return new Logger(null);
  }
  return $ensureLogger(getLogManager(), name_0);
}

defineClass(125, 1, {125:1}, Logger);
_.useParentHandlers = false;
var ALL_ENABLED = false, INFO_ENABLED = false, LOGGING_OFF = false, SEVERE_ENABLED = false, WARNING_ENABLED = false;
var Ljava_util_logging_Logger_2_classLit = createForClass('java.util.logging', 'Logger', 125);
function of_0(supplier, accumulator, combiner, finisher, characteristics){
  checkCriticalNotNull(supplier);
  checkCriticalNotNull(accumulator);
  checkCriticalNotNull(combiner);
  checkCriticalNotNull(finisher);
  checkCriticalNotNull(characteristics);
  return new CollectorImpl(supplier, accumulator, finisher);
}

function of_1(supplier, accumulator, combiner, characteristics){
  checkCriticalNotNull(supplier);
  checkCriticalNotNull(accumulator);
  checkCriticalNotNull(combiner);
  checkCriticalNotNull(characteristics);
  return new CollectorImpl(supplier, accumulator, new Function$lambda$0$Type);
}

function $clinit_Collector$Characteristics(){
  $clinit_Collector$Characteristics = emptyMethod;
  CONCURRENT = new Collector$Characteristics('CONCURRENT', 0);
  IDENTITY_FINISH = new Collector$Characteristics('IDENTITY_FINISH', 1);
  UNORDERED = new Collector$Characteristics('UNORDERED', 2);
}

function Collector$Characteristics(enum$name, enum$ordinal){
  Enum.call(this, enum$name, enum$ordinal);
}

function values_14(){
  $clinit_Collector$Characteristics();
  return stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [CONCURRENT, IDENTITY_FINISH, UNORDERED]);
}

defineClass(34, 29, {3:1, 33:1, 29:1, 34:1}, Collector$Characteristics);
var CONCURRENT, IDENTITY_FINISH, UNORDERED;
var Ljava_util_stream_Collector$Characteristics_2_classLit = createForEnum('java.util.stream', 'Collector/Characteristics', 34, values_14);
function CollectorImpl(supplier, accumulator, finisher){
  this.supplier = supplier;
  this.accumulator = accumulator;
  $clinit_Collections();
  this.finisher = finisher;
}

defineClass(213, 1, {}, CollectorImpl);
var Ljava_util_stream_CollectorImpl_2_classLit = createForClass('java.util.stream', 'CollectorImpl', 213);
function lambda$42(c1_0, c2_1){
  return c1_0.addAll(c2_1) , c1_0;
}

function lambda$44(){
  throw toJs(new IllegalStateException_0("Can't assign multiple values to the same key"));
}

function lambda$46(valueMapper_1, map_3, item_4){
  var key, newValue;
  key = castToString(item_4);
  newValue = $lambda$4(valueMapper_1.$$outer_0, castToString(item_4));
  map_3.containsKey(key)?map_3.put(key, (map_3.get(key) , lambda$44())):map_3.put(key, newValue);
}

function lambda$47(mergeFunction_0, m1_1, m2_2){
  return mergeAll(m1_1, m2_2, mergeFunction_0);
}

function lambda$50(c1_0, c2_1){
  return $addAll(c1_0, c2_1) , c1_0;
}

function mergeAll(m1, m2, mergeFunction){
  var entry, entry$iterator;
  for (entry$iterator = m2.entrySet_0().iterator(); entry$iterator.hasNext_0();) {
    entry = castTo(entry$iterator.next_1(), 22);
    m1.merge(entry.getKey(), entry.getValue(), mergeFunction);
  }
  return m1;
}

function toMap(valueMapper, mergeFunction, mapSupplier){
  return of_1(mapSupplier, new Collectors$lambda$46$Type(valueMapper), new Collectors$lambda$47$Type(mergeFunction), stampJavaTypeInfo(getClassLiteralForArray(Ljava_util_stream_Collector$Characteristics_2_classLit, 1), $intern_0, 34, 0, [($clinit_Collector$Characteristics() , IDENTITY_FINISH)]));
}

function Collectors$10methodref$merge$Type(){
}

defineClass(316, 1, {}, Collectors$10methodref$merge$Type);
_.apply_1 = function apply_26(arg0, arg1){
  return $merge_0(castTo(arg0, 84), castTo(arg1, 84));
}
;
var Ljava_util_stream_Collectors$10methodref$merge$Type_2_classLit = createForClass('java.util.stream', 'Collectors/10methodref$merge$Type', 316);
function Collectors$11methodref$toString$Type(){
}

defineClass(317, 1, {}, Collectors$11methodref$toString$Type);
_.apply_0 = function apply_27(arg0){
  return $toString_2(castTo(arg0, 84));
}
;
var Ljava_util_stream_Collectors$11methodref$toString$Type_2_classLit = createForClass('java.util.stream', 'Collectors/11methodref$toString$Type', 317);
function Collectors$20methodref$add$Type(){
}

defineClass(65, 1, {}, Collectors$20methodref$add$Type);
_.accept_0 = function accept_5(arg0, arg1){
  castTo(arg0, 20).add(arg1);
}
;
var Ljava_util_stream_Collectors$20methodref$add$Type_2_classLit = createForClass('java.util.stream', 'Collectors/20methodref$add$Type', 65);
function Collectors$21methodref$ctor$Type(){
}

defineClass(67, 1, $intern_27, Collectors$21methodref$ctor$Type);
_.get_0 = function get_35(){
  return new ArrayList;
}
;
var Ljava_util_stream_Collectors$21methodref$ctor$Type_2_classLit = createForClass('java.util.stream', 'Collectors/21methodref$ctor$Type', 67);
function Collectors$22methodref$ctor$Type(){
}

defineClass(206, 1, $intern_27, Collectors$22methodref$ctor$Type);
_.get_0 = function get_36(){
  return new HashMap;
}
;
var Ljava_util_stream_Collectors$22methodref$ctor$Type_2_classLit = createForClass('java.util.stream', 'Collectors/22methodref$ctor$Type', 206);
function Collectors$23methodref$ctor$Type(){
}

defineClass(127, 1, $intern_27, Collectors$23methodref$ctor$Type);
_.get_0 = function get_37(){
  return new HashSet;
}
;
var Ljava_util_stream_Collectors$23methodref$ctor$Type_2_classLit = createForClass('java.util.stream', 'Collectors/23methodref$ctor$Type', 127);
function Collectors$24methodref$add$Type(){
}

defineClass(128, 1, {}, Collectors$24methodref$add$Type);
_.accept_0 = function accept_6(arg0, arg1){
  $add_2(castTo(arg0, 17), arg1);
}
;
var Ljava_util_stream_Collectors$24methodref$add$Type_2_classLit = createForClass('java.util.stream', 'Collectors/24methodref$add$Type', 128);
function Collectors$9methodref$add$Type(){
}

defineClass(315, 1, {}, Collectors$9methodref$add$Type);
_.accept_0 = function accept_7(arg0, arg1){
  $add_3(castTo(arg0, 84), castTo(arg1, 188));
}
;
var Ljava_util_stream_Collectors$9methodref$add$Type_2_classLit = createForClass('java.util.stream', 'Collectors/9methodref$add$Type', 315);
function Collectors$lambda$15$Type(){
  this.delimiter_0 = ' -> ';
  this.prefix_1 = '';
  this.suffix_2 = '';
}

defineClass(314, 1, $intern_27, Collectors$lambda$15$Type);
_.get_0 = function get_38(){
  return new StringJoiner(this.delimiter_0, this.prefix_1, this.suffix_2);
}
;
var Ljava_util_stream_Collectors$lambda$15$Type_2_classLit = createForClass('java.util.stream', 'Collectors/lambda$15$Type', 314);
function Collectors$lambda$42$Type(){
}

defineClass(66, 1, {}, Collectors$lambda$42$Type);
_.apply_1 = function apply_28(arg0, arg1){
  return lambda$42(castTo(arg0, 20), castTo(arg1, 20));
}
;
var Ljava_util_stream_Collectors$lambda$42$Type_2_classLit = createForClass('java.util.stream', 'Collectors/lambda$42$Type', 66);
function Collectors$lambda$44$Type(){
}

defineClass(205, 1, {}, Collectors$lambda$44$Type);
_.apply_1 = function apply_29(arg0, arg1){
  return lambda$44();
}
;
var Ljava_util_stream_Collectors$lambda$44$Type_2_classLit = createForClass('java.util.stream', 'Collectors/lambda$44$Type', 205);
function Collectors$lambda$46$Type(valueMapper_1){
  this.valueMapper_1 = valueMapper_1;
}

defineClass(318, 1, {}, Collectors$lambda$46$Type);
_.accept_0 = function accept_8(arg0, arg1){
  lambda$46(this.valueMapper_1, castTo(arg0, 21), arg1);
}
;
var Ljava_util_stream_Collectors$lambda$46$Type_2_classLit = createForClass('java.util.stream', 'Collectors/lambda$46$Type', 318);
function Collectors$lambda$47$Type(mergeFunction_0){
  this.mergeFunction_0 = mergeFunction_0;
}

defineClass(319, 1, {}, Collectors$lambda$47$Type);
_.apply_1 = function apply_30(arg0, arg1){
  return lambda$47(this.mergeFunction_0, castTo(arg0, 21), castTo(arg1, 21));
}
;
var Ljava_util_stream_Collectors$lambda$47$Type_2_classLit = createForClass('java.util.stream', 'Collectors/lambda$47$Type', 319);
function Collectors$lambda$50$Type(){
}

defineClass(129, 1, {}, Collectors$lambda$50$Type);
_.apply_1 = function apply_31(arg0, arg1){
  return lambda$50(castTo(arg0, 17), castTo(arg1, 17));
}
;
var Ljava_util_stream_Collectors$lambda$50$Type_2_classLit = createForClass('java.util.stream', 'Collectors/lambda$50$Type', 129);
function Collectors$lambda$51$Type(){
}

defineClass(130, 1, {}, Collectors$lambda$51$Type);
_.apply_0 = function apply_32(arg0){
  return castTo(arg0, 17);
}
;
var Ljava_util_stream_Collectors$lambda$51$Type_2_classLit = createForClass('java.util.stream', 'Collectors/lambda$51$Type', 130);
function $close_2(this$static){
  if (!this$static.root_0) {
    this$static.terminated = true;
    $runClosers(this$static);
  }
   else {
    $close_2(this$static.root_0);
  }
}

function $runClosers(this$static){
  var e, i, size_0, suppressed, throwables;
  throwables = new ArrayList;
  $forEach(this$static.onClose, new TerminatableStream$lambda$0$Type(throwables));
  this$static.onClose.array.length = 0;
  if (throwables.array.length != 0) {
    e = (checkCriticalElementIndex(0, throwables.array.length) , castTo(throwables.array[0], 11));
    for (i = 1 , size_0 = throwables.array.length; i < size_0; ++i) {
      suppressed = (checkCriticalElementIndex(i, throwables.array.length) , castTo(throwables.array[i], 11));
      suppressed != e && $addSuppressed(e, suppressed);
    }
    if (instanceOf(e, 16)) {
      throw toJs(castTo(e, 16));
    }
    if (instanceOf(e, 82)) {
      throw toJs(castTo(e, 82));
    }
  }
}

function $terminate(this$static){
  if (!this$static.root_0) {
    $throwIfTerminated(this$static);
    this$static.terminated = true;
  }
   else {
    $terminate(this$static.root_0);
  }
}

function $throwIfTerminated(this$static){
  if (this$static.root_0) {
    $throwIfTerminated(this$static.root_0);
  }
   else if (this$static.terminated) {
    throw toJs(new IllegalStateException_0("Stream already terminated, can't be modified or used"));
  }
}

function TerminatableStream(previous){
  if (!previous) {
    this.root_0 = null;
    this.onClose = new ArrayList;
  }
   else {
    this.root_0 = previous;
    this.onClose = null;
  }
}

function lambda$0_3(throwables_0){
  var e;
  try {
    null.$_nullMethod();
  }
   catch ($e0) {
    $e0 = toJava($e0);
    if (instanceOf($e0, 11)) {
      e = $e0;
      push_1(throwables_0.array, e);
    }
     else 
      throw toJs($e0);
  }
}

defineClass(275, 1, {});
_.terminated = false;
var Ljava_util_stream_TerminatableStream_2_classLit = createForClass('java.util.stream', 'TerminatableStream', 275);
function $collect(this$static, collector){
  var lastArg;
  return collector.finisher.apply_0($reduce(this$static, collector.supplier.get_0(), (lastArg = new StreamImpl$lambda$4$Type(collector) , lastArg)));
}

function $filter(this$static, predicate){
  $throwIfTerminated(this$static);
  return new StreamImpl(this$static, new StreamImpl$FilterSpliterator(predicate, this$static.spliterator));
}

function $findFirst(this$static){
  var holder;
  $terminate(this$static);
  holder = new StreamImpl$ValueConsumer;
  if (this$static.spliterator.tryAdvance(holder)) {
    return $clinit_Optional() , new Optional(checkCriticalNotNull(holder.value_0));
  }
  return $clinit_Optional() , $clinit_Optional() , EMPTY;
}

function $flatMap(this$static, mapper){
  var flatMapSpliterator, spliteratorOfStreams;
  $throwIfTerminated(this$static);
  spliteratorOfStreams = new StreamImpl$MapToObjSpliterator(mapper, this$static.spliterator);
  flatMapSpliterator = new StreamImpl$1(spliteratorOfStreams);
  return new StreamImpl(this$static, flatMapSpliterator);
}

function $map_0(this$static, mapper){
  $throwIfTerminated(this$static);
  return new StreamImpl(this$static, new StreamImpl$MapToObjSpliterator(mapper, this$static.spliterator));
}

function $reduce(this$static, identity, accumulator){
  var consumer;
  $terminate(this$static);
  consumer = new StreamImpl$ValueConsumer;
  consumer.value_0 = identity;
  this$static.spliterator.forEachRemaining(new StreamImpl$lambda$5$Type(consumer, accumulator));
  return consumer.value_0;
}

function $reduce_0(this$static, accumulator){
  var consumer;
  consumer = new StreamImpl$ValueConsumer;
  if (!this$static.spliterator.tryAdvance(consumer)) {
    $terminate(this$static);
    return $clinit_Optional() , $clinit_Optional() , EMPTY;
  }
  return $clinit_Optional() , new Optional(checkCriticalNotNull($reduce(this$static, consumer.value_0, accumulator)));
}

function StreamImpl(prev, spliterator){
  TerminatableStream.call(this, prev);
  this.spliterator = spliterator;
}

function lambda$4(collector_0, a_1, t_2){
  collector_0.accumulator.accept_0(a_1, t_2);
  return a_1;
}

function lambda$5(consumer_0, accumulator_1, item_2){
  $accept(consumer_0, accumulator_1.apply_1(consumer_0.value_0, item_2));
}

defineClass(38, 275, {591:1}, StreamImpl);
_.close_0 = function close_9(){
  $close_2(this);
}
;
var Ljava_util_stream_StreamImpl_2_classLit = createForClass('java.util.stream', 'StreamImpl', 38);
function $advanceToNextSpliterator(this$static){
  while (!this$static.next_0) {
    if (!$tryAdvance(this$static.val$spliteratorOfStreams5, new StreamImpl$1$lambda$0$Type(this$static))) {
      return false;
    }
  }
  return true;
}

function $lambda$0_1(this$static, n_0){
  if (n_0) {
    this$static.nextStream = n_0;
    this$static.next_0 = ($terminate(n_0) , n_0.spliterator);
  }
}

function StreamImpl$1(val$spliteratorOfStreams){
  this.val$spliteratorOfStreams5 = val$spliteratorOfStreams;
  Spliterators$AbstractSpliterator.call(this, {l:$intern_7, m:$intern_7, h:524287}, 0);
}

defineClass(281, 160, {}, StreamImpl$1);
_.tryAdvance = function tryAdvance_0(action){
  while ($advanceToNextSpliterator(this)) {
    if (this.next_0.tryAdvance(action)) {
      return true;
    }
     else {
      $close_2(this.nextStream);
      this.nextStream = null;
      this.next_0 = null;
    }
  }
  return false;
}
;
var Ljava_util_stream_StreamImpl$1_2_classLit = createForClass('java.util.stream', 'StreamImpl/1', 281);
function StreamImpl$1$lambda$0$Type($$outer_0){
  this.$$outer_0 = $$outer_0;
}

defineClass(282, 1, {}, StreamImpl$1$lambda$0$Type);
_.accept = function accept_9(arg0){
  $lambda$0_1(this.$$outer_0, castTo(arg0, 591));
}
;
var Ljava_util_stream_StreamImpl$1$lambda$0$Type_2_classLit = createForClass('java.util.stream', 'StreamImpl/1/lambda$0$Type', 282);
function $lambda$0_2(this$static, action_1, item_1){
  if (this$static.filter_0.test_0(item_1)) {
    this$static.found = true;
    action_1.accept(item_1);
  }
}

function StreamImpl$FilterSpliterator(filter, original){
  Spliterators$AbstractSpliterator.call(this, original.estimateSize_0(), original.characteristics_0() & -16449);
  checkCriticalNotNull(filter);
  this.filter_0 = filter;
  this.original = original;
}

defineClass(276, 160, {}, StreamImpl$FilterSpliterator);
_.tryAdvance = function tryAdvance_1(action){
  this.found = false;
  while (!this.found && this.original.tryAdvance(new StreamImpl$FilterSpliterator$lambda$0$Type(this, action)))
  ;
  return this.found;
}
;
_.found = false;
var Ljava_util_stream_StreamImpl$FilterSpliterator_2_classLit = createForClass('java.util.stream', 'StreamImpl/FilterSpliterator', 276);
function StreamImpl$FilterSpliterator$lambda$0$Type($$outer_0, action_1){
  this.$$outer_0 = $$outer_0;
  this.action_1 = action_1;
}

defineClass(278, 1, {}, StreamImpl$FilterSpliterator$lambda$0$Type);
_.accept = function accept_10(arg0){
  $lambda$0_2(this.$$outer_0, this.action_1, arg0);
}
;
var Ljava_util_stream_StreamImpl$FilterSpliterator$lambda$0$Type_2_classLit = createForClass('java.util.stream', 'StreamImpl/FilterSpliterator/lambda$0$Type', 278);
function $lambda$0_3(this$static, action_1, u_1){
  action_1.accept(this$static.map_0.apply_0(u_1));
}

function $tryAdvance(this$static, action){
  return this$static.original.tryAdvance(new StreamImpl$MapToObjSpliterator$lambda$0$Type(this$static, action));
}

function StreamImpl$MapToObjSpliterator(map_0, original){
  Spliterators$AbstractSpliterator.call(this, original.estimateSize_0(), original.characteristics_0() & -6);
  checkCriticalNotNull(map_0);
  this.map_0 = map_0;
  this.original = original;
}

defineClass(201, 160, {}, StreamImpl$MapToObjSpliterator);
_.tryAdvance = function tryAdvance_2(action){
  return $tryAdvance(this, action);
}
;
var Ljava_util_stream_StreamImpl$MapToObjSpliterator_2_classLit = createForClass('java.util.stream', 'StreamImpl/MapToObjSpliterator', 201);
function StreamImpl$MapToObjSpliterator$lambda$0$Type($$outer_0, action_1){
  this.$$outer_0 = $$outer_0;
  this.action_1 = action_1;
}

defineClass(277, 1, {}, StreamImpl$MapToObjSpliterator$lambda$0$Type);
_.accept = function accept_11(arg0){
  $lambda$0_3(this.$$outer_0, this.action_1, arg0);
}
;
var Ljava_util_stream_StreamImpl$MapToObjSpliterator$lambda$0$Type_2_classLit = createForClass('java.util.stream', 'StreamImpl/MapToObjSpliterator/lambda$0$Type', 277);
function $accept(this$static, value_0){
  this$static.value_0 = value_0;
}

function StreamImpl$ValueConsumer(){
}

defineClass(168, 1, {}, StreamImpl$ValueConsumer);
_.accept = function accept_12(value_0){
  $accept(this, value_0);
}
;
var Ljava_util_stream_StreamImpl$ValueConsumer_2_classLit = createForClass('java.util.stream', 'StreamImpl/ValueConsumer', 168);
function StreamImpl$lambda$4$Type(collector_0){
  this.collector_0 = collector_0;
}

defineClass(279, 1, {}, StreamImpl$lambda$4$Type);
_.apply_1 = function apply_33(arg0, arg1){
  return lambda$4(this.collector_0, arg0, arg1);
}
;
var Ljava_util_stream_StreamImpl$lambda$4$Type_2_classLit = createForClass('java.util.stream', 'StreamImpl/lambda$4$Type', 279);
function $accept_0(this$static, arg0){
  lambda$5(this$static.consumer_0, this$static.accumulator_1, arg0);
}

function StreamImpl$lambda$5$Type(consumer_0, accumulator_1){
  this.consumer_0 = consumer_0;
  this.accumulator_1 = accumulator_1;
}

defineClass(280, 1, {}, StreamImpl$lambda$5$Type);
_.accept = function accept_13(arg0){
  $accept_0(this, arg0);
}
;
var Ljava_util_stream_StreamImpl$lambda$5$Type_2_classLit = createForClass('java.util.stream', 'StreamImpl/lambda$5$Type', 280);
function TerminatableStream$lambda$0$Type(throwables_0){
  this.throwables_0 = throwables_0;
}

defineClass(283, 1, {}, TerminatableStream$lambda$0$Type);
_.accept = function accept_14(arg0){
  var lastArg;
  lambda$0_3((lastArg = this.throwables_0 , throwClassCastExceptionUnlessNull(arg0) , lastArg));
}
;
var Ljava_util_stream_TerminatableStream$lambda$0$Type_2_classLit = createForClass('java.util.stream', 'TerminatableStream/lambda$0$Type', 283);
function stampJavaTypeInfo_1(array, referenceType){
  return stampJavaTypeInfo_0(array, referenceType);
}

defineClass(503, 1, {});
var Ljavaemul_internal_ConsoleLogger_2_classLit = createForClass('javaemul.internal', 'ConsoleLogger', 503);
function $clinit_EmulatedCharset(){
  $clinit_EmulatedCharset = emptyMethod;
  new EmulatedCharset$UtfCharset;
  new EmulatedCharset$LatinCharset('ISO-LATIN-1');
  new EmulatedCharset$LatinCharset('ISO-8859-1');
}

function EmulatedCharset(name_0){
  this.name_0 = name_0;
}

defineClass(192, 83, $intern_35);
var Ljavaemul_internal_EmulatedCharset_2_classLit = createForClass('javaemul.internal', 'EmulatedCharset', 192);
function EmulatedCharset$LatinCharset(name_0){
  EmulatedCharset.call(this, name_0);
}

defineClass(193, 192, $intern_35, EmulatedCharset$LatinCharset);
var Ljavaemul_internal_EmulatedCharset$LatinCharset_2_classLit = createForClass('javaemul.internal', 'EmulatedCharset/LatinCharset', 193);
function $encodeUtf8(bytes, codePoint){
  if (codePoint < 128) {
    bytes.push((codePoint & 127) << 24 >> 24);
  }
   else if (codePoint < 2048) {
    bytes.push((codePoint >> 6 & 31 | 192) << 24 >> 24);
    bytes.push((codePoint & 63 | 128) << 24 >> 24);
  }
   else if (codePoint < $intern_5) {
    bytes.push((codePoint >> 12 & 15 | 224) << 24 >> 24);
    bytes.push((codePoint >> 6 & 63 | 128) << 24 >> 24);
    bytes.push((codePoint & 63 | 128) << 24 >> 24);
  }
   else if (codePoint < 2097152) {
    bytes.push((codePoint >> 18 & 7 | 240) << 24 >> 24);
    bytes.push((codePoint >> 12 & 63 | 128) << 24 >> 24);
    bytes.push((codePoint >> 6 & 63 | 128) << 24 >> 24);
    bytes.push((codePoint & 63 | 128) << 24 >> 24);
  }
   else if (codePoint < 67108864) {
    bytes.push((codePoint >> 24 & 3 | 248) << 24 >> 24);
    bytes.push((codePoint >> 18 & 63 | 128) << 24 >> 24);
    bytes.push((codePoint >> 12 & 63 | 128) << 24 >> 24);
    bytes.push((codePoint >> 6 & 63 | 128) << 24 >> 24);
    bytes.push((codePoint & 63 | 128) << 24 >> 24);
  }
   else {
    throw toJs(new IllegalArgumentException('Character out of range: ' + codePoint));
  }
}

function $getBytes(str){
  var bytes, ch_0, i, n;
  n = str.length;
  bytes = initUnidimensionalArray(B_classLit, $intern_4, 46, 0, 15, 1);
  for (i = 0; i < n;) {
    ch_0 = codePointAt(str, i, str.length);
    i += ch_0 >= $intern_5?2:1;
    $encodeUtf8(bytes, ch_0);
  }
  return bytes;
}

function EmulatedCharset$UtfCharset(){
  EmulatedCharset.call(this, 'UTF-8');
}

defineClass(256, 192, $intern_35, EmulatedCharset$UtfCharset);
var Ljavaemul_internal_EmulatedCharset$UtfCharset_2_classLit = createForClass('javaemul.internal', 'EmulatedCharset/UtfCharset', 256);
defineClass(1360, 1, {});
function $clinit_OMNode(){
  $clinit_OMNode = emptyMethod;
  eventBus_0 = new DOMEventBus;
}

function $addDomHandler(this$static, handler, type_0){
  bindEventListener(this$static.ot, type_0.name_0);
  return $addHandlerToSource(eventBus_0, type_0, this$static, handler);
}

function $appendChild_1(this$static, newChild){
  $appendChild(this$static.ot, newChild.ot);
  return newChild;
}

function $removeChild_0(this$static, oldChild){
  $removeChild(this$static.ot, oldChild.ot);
  return oldChild;
}

function OMNode(node){
  $clinit_OMNode();
  node.__wrapper = this;
  this.ot = node;
}

function convert(obj){
  $clinit_OMNode();
  var wrapper;
  wrapper = obj.__wrapper;
  !wrapper && (wrapper = (new OMNode$Conversion(obj)).result);
  return wrapper;
}

function convertList(obj){
  $clinit_OMNode();
  return (new OMNode$ListConversion(obj)).result;
}

defineClass(148, 1, {}, OMNode);
_.fireEvent = function fireEvent_0(event_0){
  event_0.revive();
  $doFire(eventBus_0, event_0, this);
}
;
_.toString_0 = function toString_38(){
  return toStringSimple(this.ot);
}
;
var eventBus_0;
var Lorg_vectomatic_dom_svg_OMNode_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMNode', 148);
function OMAttr(ot){
  $clinit_OMNode();
  OMNode.call(this, ot);
}

defineClass(505, 148, {}, OMAttr);
var Lorg_vectomatic_dom_svg_OMAttr_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMAttr', 505);
function $getElementById_0(this$static, elementId){
  var elt;
  elt = $getElementById(this$static.ot, elementId);
  return elt?castTo(convert(elt), 7):null;
}

defineClass(327, 148, {});
var Lorg_vectomatic_dom_svg_OMDocument_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMDocument', 327);
function $setAttribute_0(this$static, name_0, value_0){
  $setAttribute(this$static.ot, name_0, value_0);
}

function OMElement(element){
  $clinit_OMNode();
  OMNode.call(this, element);
}

defineClass(7, 148, {7:1}, OMElement);
var Lorg_vectomatic_dom_svg_OMElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMElement', 7);
function $clinit_OMNode$Conversion(){
  $clinit_OMNode$Conversion = emptyMethod;
  initialize();
}

function $convert(this$static, node){
  var wrapper = null;
  if (node != null) {
    var type_0 = getType_3(node);
    if (type_0) {
      var ctor = $wnd.otToWrapper[type_0];
      ctor != null?(wrapper = ctor(node)):node.nodeType == 1?(wrapper = new OMElement(node)):node.nodeType == 2?(wrapper = new OMAttr(node)):node.nodeType == 3?(wrapper = new OMText(node)):node.nodeType == 9?(wrapper = new OMSVGDocument(node)):(wrapper = new OMNode(node));
    }
  }
  this$static.result = wrapper;
}

function OMNode$Conversion(node){
  $clinit_OMNode$Conversion();
  $convert(this, node);
}

function initialize(){
  $wnd.otToWrapper == null && ($wnd.otToWrapper = new Object);
  $wnd.otToWrapper['SVGAElement'] = function(elem){
    return new OMSVGAElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGAltGlyphDefElement'] = function(elem){
    return new OMSVGAltGlyphDefElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGAltGlyphElement'] = function(elem){
    return new OMSVGAltGlyphElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGAltGlyphItemElement'] = function(elem){
    return new OMSVGAltGlyphItemElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGAnimateColorElement'] = function(elem){
    return new OMSVGAnimateColorElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGAnimateElement'] = function(elem){
    return new OMSVGAnimateElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGAnimateMotionElement'] = function(elem){
    return new OMSVGAnimateMotionElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGAnimateTransformElement'] = function(elem){
    return new OMSVGAnimateTransformElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGCircleElement'] = function(elem){
    return new OMSVGCircleElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGClipPathElement'] = function(elem){
    return new OMSVGClipPathElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGColorProfileElement'] = function(elem){
    return new OMSVGColorProfileElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGCursorElement'] = function(elem){
    return new OMSVGCursorElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGDefsElement'] = function(elem){
    return new OMSVGDefsElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGDescElement'] = function(elem){
    return new OMSVGDescElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGDocument'] = function(elem){
    return new OMSVGDocument(elem);
  }
  ;
  $wnd.otToWrapper['SVGEllipseElement'] = function(elem){
    return new OMSVGEllipseElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEBlendElement'] = function(elem){
    return new OMSVGFEBlendElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEColorMatrixElement'] = function(elem){
    return new OMSVGFEColorMatrixElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEComponentTransferElement'] = function(elem){
    return new OMSVGFEComponentTransferElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFECompositeElement'] = function(elem){
    return new OMSVGFECompositeElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEConvolveMatrixElement'] = function(elem){
    return new OMSVGFEConvolveMatrixElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEDiffuseLightingElement'] = function(elem){
    return new OMSVGFEDiffuseLightingElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEDisplacementMapElement'] = function(elem){
    return new OMSVGFEDisplacementMapElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEDistantLightElement'] = function(elem){
    return new OMSVGFEDistantLightElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEFloodElement'] = function(elem){
    return new OMSVGFEFloodElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEFuncAElement'] = function(elem){
    return new OMSVGFEFuncAElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEFuncBElement'] = function(elem){
    return new OMSVGFEFuncBElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEFuncGElement'] = function(elem){
    return new OMSVGFEFuncGElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEFuncRElement'] = function(elem){
    return new OMSVGFEFuncRElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEGaussianBlurElement'] = function(elem){
    return new OMSVGFEGaussianBlurElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEImageElement'] = function(elem){
    return new OMSVGFEImageElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEMergeElement'] = function(elem){
    return new OMSVGFEMergeElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEMergeNodeElement'] = function(elem){
    return new OMSVGFEMergeNodeElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEMorphologyElement'] = function(elem){
    return new OMSVGFEMorphologyElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEOffsetElement'] = function(elem){
    return new OMSVGFEOffsetElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFEPointLightElement'] = function(elem){
    return new OMSVGFEPointLightElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFESpecularLightingElement'] = function(elem){
    return new OMSVGFESpecularLightingElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFESpotLightElement'] = function(elem){
    return new OMSVGFESpotLightElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFETileElement'] = function(elem){
    return new OMSVGFETileElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFETurbulenceElement'] = function(elem){
    return new OMSVGFETurbulenceElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFilterElement'] = function(elem){
    return new OMSVGFilterElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFontElement'] = function(elem){
    return new OMSVGFontElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFontFaceElement'] = function(elem){
    return new OMSVGFontFaceElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFontFaceFormatElement'] = function(elem){
    return new OMSVGFontFaceFormatElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFontFaceNameElement'] = function(elem){
    return new OMSVGFontFaceNameElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFontFaceSrcElement'] = function(elem){
    return new OMSVGFontFaceSrcElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGFontFaceUriElement'] = function(elem){
    return new OMSVGFontFaceUriElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGForeignObjectElement'] = function(elem){
    return new OMSVGForeignObjectElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGGElement'] = function(elem){
    return new OMSVGGElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGGlyphElement'] = function(elem){
    return new OMSVGGlyphElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGGlyphRefElement'] = function(elem){
    return new OMSVGGlyphRefElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGHKernElement'] = function(elem){
    return new OMSVGHKernElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGImageElement'] = function(elem){
    return new OMSVGImageElement_0(elem);
  }
  ;
  $wnd.otToWrapper['SVGLinearGradientElement'] = function(elem){
    return new OMSVGLinearGradientElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGLineElement'] = function(elem){
    return new OMSVGLineElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGMarkerElement'] = function(elem){
    return new OMSVGMarkerElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGMaskElement'] = function(elem){
    return new OMSVGMaskElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGMetadataElement'] = function(elem){
    return new OMSVGMetadataElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGMissingGlyphElement'] = function(elem){
    return new OMSVGMissingGlyphElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGMPathElement'] = function(elem){
    return new OMSVGMPathElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGPathElement'] = function(elem){
    return new OMSVGPathElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGPatternElement'] = function(elem){
    return new OMSVGPatternElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGPolygonElement'] = function(elem){
    return new OMSVGPolygonElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGPolylineElement'] = function(elem){
    return new OMSVGPolylineElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGRadialGradientElement'] = function(elem){
    return new OMSVGRadialGradientElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGRectElement'] = function(elem){
    return new OMSVGRectElement_0(elem);
  }
  ;
  $wnd.otToWrapper['SVGRectElement'] = function(elem){
    return new OMSVGRectElement_0(elem);
  }
  ;
  $wnd.otToWrapper['SVGScriptElement'] = function(elem){
    return new OMSVGScriptElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGSetElement'] = function(elem){
    return new OMSVGSetElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGStopElement'] = function(elem){
    return new OMSVGStopElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGStyleElement'] = function(elem){
    return new OMSVGStyleElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGSVGElement'] = function(elem){
    return new OMSVGSVGElement_0(elem);
  }
  ;
  $wnd.otToWrapper['SVGSwitchElement'] = function(elem){
    return new OMSVGSwitchElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGSymbolElement'] = function(elem){
    return new OMSVGSymbolElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGTextElement'] = function(elem){
    return new OMSVGTextElement_0(elem);
  }
  ;
  $wnd.otToWrapper['SVGTextPathElement'] = function(elem){
    return new OMSVGTextPathElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGTitleElement'] = function(elem){
    return new OMSVGTitleElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGTRefElement'] = function(elem){
    return new OMSVGTRefElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGTSpanElement'] = function(elem){
    return new OMSVGTSpanElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGUseElement'] = function(elem){
    return new OMSVGUseElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGViewElement'] = function(elem){
    return new OMSVGViewElement(elem);
  }
  ;
  $wnd.otToWrapper['SVGVKernElement'] = function(elem){
    return new OMSVGVKernElement(elem);
  }
  ;
}

defineClass(328, 1, {}, OMNode$Conversion);
var Lorg_vectomatic_dom_svg_OMNode$Conversion_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMNode/Conversion', 328);
function $clinit_OMNode$ListConversion(){
  $clinit_OMNode$ListConversion = emptyMethod;
  initialize_0();
}

function $convert_0(this$static, list){
  var wrapper = null;
  var type_0 = getType_3(list);
  if (type_0) {
    var ctor = $wnd.otToWrapper[type_0];
    ctor != null && (wrapper = ctor(list));
  }
  this$static.result = wrapper;
}

function OMNode$ListConversion(list){
  $clinit_OMNode$ListConversion();
  $convert_0(this, list);
}

function initialize_0(){
  $wnd.otToWrapper == null && ($wnd.otToWrapper = new Object);
  $wnd.otToWrapper['NodeList'] = function(elem){
    return new OMNodeList(elem);
  }
  ;
  $wnd.otToWrapper['SVGLengthList'] = function(elem){
    return new OMSVGLengthList(elem);
  }
  ;
  $wnd.otToWrapper['SVGNumberList'] = function(elem){
    return new OMSVGNumberList(elem);
  }
  ;
  $wnd.otToWrapper['SVGPathSegList'] = function(elem){
    return new OMSVGPathSegList(elem);
  }
  ;
  $wnd.otToWrapper['SVGPointList'] = function(elem){
    return new OMSVGPointList(elem);
  }
  ;
  $wnd.otToWrapper['SVGStringList'] = function(elem){
    return new OMSVGStringList(elem);
  }
  ;
  $wnd.otToWrapper['SVGTransformList'] = function(elem){
    return new OMSVGTransformList(elem);
  }
  ;
}

defineClass(329, 1, {}, OMNode$ListConversion);
var Lorg_vectomatic_dom_svg_OMNode$ListConversion_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMNode/ListConversion', 329);
function $getItem(this$static, index_0){
  var node;
  node = this$static.ot[index_0];
  return node?convert(node):null;
}

function OMNodeList(nodeList){
  this.ot = nodeList;
}

defineClass(584, 1, $intern_23, OMNodeList);
_.iterator = function iterator_14(){
  return new OMNodeList$1(this);
}
;
var Lorg_vectomatic_dom_svg_OMNodeList_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMNodeList', 584);
function OMNodeList$1(this$0){
  this.this$01 = this$0;
}

defineClass(406, 1, {}, OMNodeList$1);
_.forEachRemaining = function forEachRemaining_14(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_13(){
  return $getItem(this.this$01, this.index_0++);
}
;
_.hasNext_0 = function hasNext_13(){
  return this.index_0 < this.this$01.ot.length;
}
;
_.remove_0 = function remove_33(){
  throw toJs(new UnsupportedOperationException);
}
;
_.index_0 = 0;
var Lorg_vectomatic_dom_svg_OMNodeList$1_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMNodeList/1', 406);
function OMSVGElement(ot){
  OMElement.call(this, ot);
}

defineClass(4, 7, $intern_40);
var Lorg_vectomatic_dom_svg_OMSVGElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGElement', 4);
function OMSVGAElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(506, 4, $intern_41, OMSVGAElement);
_.getTransform = function getTransform(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGAElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGAElement', 506);
function OMSVGAltGlyphDefElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(507, 4, $intern_40, OMSVGAltGlyphDefElement);
var Lorg_vectomatic_dom_svg_OMSVGAltGlyphDefElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGAltGlyphDefElement', 507);
function OMSVGTextContentElement(ot){
  OMSVGElement.call(this, ot);
}

defineClass(224, 4, $intern_40);
var Lorg_vectomatic_dom_svg_OMSVGTextContentElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGTextContentElement', 224);
function OMSVGTextPositioningElement(ot){
  OMSVGTextContentElement.call(this, ot);
}

defineClass(152, 224, $intern_40);
var Lorg_vectomatic_dom_svg_OMSVGTextPositioningElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGTextPositioningElement', 152);
function OMSVGAltGlyphElement(ot){
  $clinit_OMNode();
  OMSVGTextPositioningElement.call(this, ot);
}

defineClass(508, 152, $intern_40, OMSVGAltGlyphElement);
var Lorg_vectomatic_dom_svg_OMSVGAltGlyphElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGAltGlyphElement', 508);
function OMSVGAltGlyphItemElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(509, 4, $intern_40, OMSVGAltGlyphItemElement);
var Lorg_vectomatic_dom_svg_OMSVGAltGlyphItemElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGAltGlyphItemElement', 509);
function OMSVGAnimationElement(ot){
  OMSVGElement.call(this, ot);
}

defineClass(134, 4, $intern_40);
var Lorg_vectomatic_dom_svg_OMSVGAnimationElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGAnimationElement', 134);
function OMSVGAnimateColorElement(ot){
  $clinit_OMNode();
  OMSVGAnimationElement.call(this, ot);
}

defineClass(510, 134, $intern_40, OMSVGAnimateColorElement);
var Lorg_vectomatic_dom_svg_OMSVGAnimateColorElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGAnimateColorElement', 510);
function OMSVGAnimateElement(ot){
  $clinit_OMNode();
  OMSVGAnimationElement.call(this, ot);
}

defineClass(511, 134, $intern_40, OMSVGAnimateElement);
var Lorg_vectomatic_dom_svg_OMSVGAnimateElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGAnimateElement', 511);
function OMSVGAnimateMotionElement(ot){
  $clinit_OMNode();
  OMSVGAnimationElement.call(this, ot);
}

defineClass(512, 134, $intern_40, OMSVGAnimateMotionElement);
var Lorg_vectomatic_dom_svg_OMSVGAnimateMotionElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGAnimateMotionElement', 512);
function OMSVGAnimateTransformElement(ot){
  $clinit_OMNode();
  OMSVGAnimationElement.call(this, ot);
}

defineClass(513, 134, $intern_40, OMSVGAnimateTransformElement);
var Lorg_vectomatic_dom_svg_OMSVGAnimateTransformElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGAnimateTransformElement', 513);
function $getBaseVal(this$static){
  return convertList(this$static.baseVal);
}

function $setBaseVal(this$static, value_0){
  this$static.baseVal = value_0;
}

function $getBaseVal_0(this$static){
  return convertList(this$static.baseVal);
}

function OMSVGCircleElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(514, 4, $intern_41, OMSVGCircleElement);
_.getTransform = function getTransform_0(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGCircleElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGCircleElement', 514);
function OMSVGClipPathElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(515, 4, $intern_41, OMSVGClipPathElement);
_.getTransform = function getTransform_1(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGClipPathElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGClipPathElement', 515);
function OMSVGColorProfileElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(516, 4, $intern_40, OMSVGColorProfileElement);
var Lorg_vectomatic_dom_svg_OMSVGColorProfileElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGColorProfileElement', 516);
function OMSVGComponentTransferFunctionElement(ot){
  OMSVGElement.call(this, ot);
}

defineClass(153, 4, $intern_40);
var Lorg_vectomatic_dom_svg_OMSVGComponentTransferFunctionElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGComponentTransferFunctionElement', 153);
function OMSVGCursorElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(517, 4, $intern_40, OMSVGCursorElement);
var Lorg_vectomatic_dom_svg_OMSVGCursorElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGCursorElement', 517);
function OMSVGDefsElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(518, 4, $intern_41, OMSVGDefsElement);
_.getTransform = function getTransform_2(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGDefsElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGDefsElement', 518);
function OMSVGDescElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(519, 4, $intern_40, OMSVGDescElement);
var Lorg_vectomatic_dom_svg_OMSVGDescElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGDescElement', 519);
function OMSVGDocument(ot){
  $clinit_OMNode();
  OMNode.call(this, ot);
}

defineClass(170, 327, {170:1}, OMSVGDocument);
var Lorg_vectomatic_dom_svg_OMSVGDocument_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGDocument', 170);
function OMSVGEllipseElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(520, 4, $intern_41, OMSVGEllipseElement);
_.getTransform = function getTransform_3(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGEllipseElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGEllipseElement', 520);
function OMSVGFEBlendElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(521, 4, $intern_40, OMSVGFEBlendElement);
var Lorg_vectomatic_dom_svg_OMSVGFEBlendElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEBlendElement', 521);
function OMSVGFEColorMatrixElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(522, 4, $intern_40, OMSVGFEColorMatrixElement);
var Lorg_vectomatic_dom_svg_OMSVGFEColorMatrixElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEColorMatrixElement', 522);
function OMSVGFEComponentTransferElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(523, 4, $intern_40, OMSVGFEComponentTransferElement);
var Lorg_vectomatic_dom_svg_OMSVGFEComponentTransferElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEComponentTransferElement', 523);
function OMSVGFECompositeElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(524, 4, $intern_40, OMSVGFECompositeElement);
var Lorg_vectomatic_dom_svg_OMSVGFECompositeElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFECompositeElement', 524);
function OMSVGFEConvolveMatrixElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(525, 4, $intern_40, OMSVGFEConvolveMatrixElement);
var Lorg_vectomatic_dom_svg_OMSVGFEConvolveMatrixElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEConvolveMatrixElement', 525);
function OMSVGFEDiffuseLightingElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(526, 4, $intern_40, OMSVGFEDiffuseLightingElement);
var Lorg_vectomatic_dom_svg_OMSVGFEDiffuseLightingElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEDiffuseLightingElement', 526);
function OMSVGFEDisplacementMapElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(527, 4, $intern_40, OMSVGFEDisplacementMapElement);
var Lorg_vectomatic_dom_svg_OMSVGFEDisplacementMapElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEDisplacementMapElement', 527);
function OMSVGFEDistantLightElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(528, 4, $intern_40, OMSVGFEDistantLightElement);
var Lorg_vectomatic_dom_svg_OMSVGFEDistantLightElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEDistantLightElement', 528);
function OMSVGFEFloodElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(529, 4, $intern_40, OMSVGFEFloodElement);
var Lorg_vectomatic_dom_svg_OMSVGFEFloodElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEFloodElement', 529);
function OMSVGFEFuncAElement(ot){
  $clinit_OMNode();
  OMSVGComponentTransferFunctionElement.call(this, ot);
}

defineClass(530, 153, $intern_40, OMSVGFEFuncAElement);
var Lorg_vectomatic_dom_svg_OMSVGFEFuncAElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEFuncAElement', 530);
function OMSVGFEFuncBElement(ot){
  $clinit_OMNode();
  OMSVGComponentTransferFunctionElement.call(this, ot);
}

defineClass(531, 153, $intern_40, OMSVGFEFuncBElement);
var Lorg_vectomatic_dom_svg_OMSVGFEFuncBElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEFuncBElement', 531);
function OMSVGFEFuncGElement(ot){
  $clinit_OMNode();
  OMSVGComponentTransferFunctionElement.call(this, ot);
}

defineClass(532, 153, $intern_40, OMSVGFEFuncGElement);
var Lorg_vectomatic_dom_svg_OMSVGFEFuncGElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEFuncGElement', 532);
function OMSVGFEFuncRElement(ot){
  $clinit_OMNode();
  OMSVGComponentTransferFunctionElement.call(this, ot);
}

defineClass(533, 153, $intern_40, OMSVGFEFuncRElement);
var Lorg_vectomatic_dom_svg_OMSVGFEFuncRElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEFuncRElement', 533);
function OMSVGFEGaussianBlurElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(534, 4, $intern_40, OMSVGFEGaussianBlurElement);
var Lorg_vectomatic_dom_svg_OMSVGFEGaussianBlurElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEGaussianBlurElement', 534);
function OMSVGFEImageElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(535, 4, $intern_40, OMSVGFEImageElement);
var Lorg_vectomatic_dom_svg_OMSVGFEImageElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEImageElement', 535);
function OMSVGFEMergeElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(536, 4, $intern_40, OMSVGFEMergeElement);
var Lorg_vectomatic_dom_svg_OMSVGFEMergeElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEMergeElement', 536);
function OMSVGFEMergeNodeElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(537, 4, $intern_40, OMSVGFEMergeNodeElement);
var Lorg_vectomatic_dom_svg_OMSVGFEMergeNodeElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEMergeNodeElement', 537);
function OMSVGFEMorphologyElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(538, 4, $intern_40, OMSVGFEMorphologyElement);
var Lorg_vectomatic_dom_svg_OMSVGFEMorphologyElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEMorphologyElement', 538);
function OMSVGFEOffsetElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(539, 4, $intern_40, OMSVGFEOffsetElement);
var Lorg_vectomatic_dom_svg_OMSVGFEOffsetElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEOffsetElement', 539);
function OMSVGFEPointLightElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(540, 4, $intern_40, OMSVGFEPointLightElement);
var Lorg_vectomatic_dom_svg_OMSVGFEPointLightElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFEPointLightElement', 540);
function OMSVGFESpecularLightingElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(541, 4, $intern_40, OMSVGFESpecularLightingElement);
var Lorg_vectomatic_dom_svg_OMSVGFESpecularLightingElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFESpecularLightingElement', 541);
function OMSVGFESpotLightElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(542, 4, $intern_40, OMSVGFESpotLightElement);
var Lorg_vectomatic_dom_svg_OMSVGFESpotLightElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFESpotLightElement', 542);
function OMSVGFETileElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(543, 4, $intern_40, OMSVGFETileElement);
var Lorg_vectomatic_dom_svg_OMSVGFETileElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFETileElement', 543);
function OMSVGFETurbulenceElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(544, 4, $intern_40, OMSVGFETurbulenceElement);
var Lorg_vectomatic_dom_svg_OMSVGFETurbulenceElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFETurbulenceElement', 544);
function OMSVGFilterElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(545, 4, $intern_40, OMSVGFilterElement);
var Lorg_vectomatic_dom_svg_OMSVGFilterElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFilterElement', 545);
function OMSVGFontElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(546, 4, $intern_40, OMSVGFontElement);
var Lorg_vectomatic_dom_svg_OMSVGFontElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFontElement', 546);
function OMSVGFontFaceElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(547, 4, $intern_40, OMSVGFontFaceElement);
var Lorg_vectomatic_dom_svg_OMSVGFontFaceElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFontFaceElement', 547);
function OMSVGFontFaceFormatElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(548, 4, $intern_40, OMSVGFontFaceFormatElement);
var Lorg_vectomatic_dom_svg_OMSVGFontFaceFormatElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFontFaceFormatElement', 548);
function OMSVGFontFaceNameElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(549, 4, $intern_40, OMSVGFontFaceNameElement);
var Lorg_vectomatic_dom_svg_OMSVGFontFaceNameElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFontFaceNameElement', 549);
function OMSVGFontFaceSrcElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(550, 4, $intern_40, OMSVGFontFaceSrcElement);
var Lorg_vectomatic_dom_svg_OMSVGFontFaceSrcElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFontFaceSrcElement', 550);
function OMSVGFontFaceUriElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(551, 4, $intern_40, OMSVGFontFaceUriElement);
var Lorg_vectomatic_dom_svg_OMSVGFontFaceUriElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGFontFaceUriElement', 551);
function OMSVGForeignObjectElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(552, 4, $intern_41, OMSVGForeignObjectElement);
_.getTransform = function getTransform_4(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGForeignObjectElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGForeignObjectElement', 552);
function OMSVGGElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(174, 4, {7:1, 4:1, 174:1, 43:1}, OMSVGGElement);
_.getTransform = function getTransform_5(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGGElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGGElement', 174);
function OMSVGGlyphElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(553, 4, $intern_40, OMSVGGlyphElement);
var Lorg_vectomatic_dom_svg_OMSVGGlyphElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGGlyphElement', 553);
function OMSVGGlyphRefElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(554, 4, $intern_40, OMSVGGlyphRefElement);
var Lorg_vectomatic_dom_svg_OMSVGGlyphRefElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGGlyphRefElement', 554);
function OMSVGGradientElement(ot){
  OMSVGElement.call(this, ot);
}

defineClass(225, 4, $intern_40);
var Lorg_vectomatic_dom_svg_OMSVGGradientElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGGradientElement', 225);
function OMSVGHKernElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(555, 4, $intern_40, OMSVGHKernElement);
var Lorg_vectomatic_dom_svg_OMSVGHKernElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGHKernElement', 555);
function OMSVGImageElement(x_0, y_0, width_0, height, href_0){
  $clinit_OMNode();
  OMSVGImageElement_0.call(this, createElementNS(($clinit_DOMHelper() , $doc), 'http://www.w3.org/2000/svg', 'image'));
  $setValue_2(this.ot.x.baseVal, x_0);
  $setValue_2(this.ot.y.baseVal, y_0);
  $setValue_2(this.ot.width.baseVal, width_0);
  $setValue_2(this.ot.height.baseVal, height);
  $setBaseVal(this.ot.href, href_0);
}

function OMSVGImageElement_0(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(177, 4, $intern_41, OMSVGImageElement, OMSVGImageElement_0);
_.getTransform = function getTransform_6(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGImageElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGImageElement', 177);
function $setValue_2(this$static, value_0){
  this$static.value = value_0;
}

function $appendItem(this$static, newItem){
  return this$static.ot.appendItem(newItem);
}

function $getItem_0(this$static, index_0){
  return this$static.ot.getItem(index_0);
}

function OMSVGLengthList(ot){
  this.ot = ot;
}

defineClass(585, 1, $intern_23, OMSVGLengthList);
_.iterator = function iterator_15(){
  return new OMSVGLengthList$1(this);
}
;
var Lorg_vectomatic_dom_svg_OMSVGLengthList_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGLengthList', 585);
function OMSVGLengthList$1(this$0){
  this.this$01 = this$0;
}

defineClass(407, 1, {}, OMSVGLengthList$1);
_.forEachRemaining = function forEachRemaining_15(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_14(){
  return $getItem_0(this.this$01, this.index_0++);
}
;
_.hasNext_0 = function hasNext_14(){
  return this.index_0 < this.this$01.ot.numberOfItems;
}
;
_.remove_0 = function remove_34(){
  throw toJs(new UnsupportedOperationException);
}
;
_.index_0 = 0;
var Lorg_vectomatic_dom_svg_OMSVGLengthList$1_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGLengthList/1', 407);
function OMSVGLineElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(557, 4, $intern_41, OMSVGLineElement);
_.getTransform = function getTransform_7(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGLineElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGLineElement', 557);
function OMSVGLinearGradientElement(ot){
  $clinit_OMNode();
  OMSVGGradientElement.call(this, ot);
}

defineClass(556, 225, $intern_40, OMSVGLinearGradientElement);
var Lorg_vectomatic_dom_svg_OMSVGLinearGradientElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGLinearGradientElement', 556);
function OMSVGMPathElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(562, 4, $intern_40, OMSVGMPathElement);
var Lorg_vectomatic_dom_svg_OMSVGMPathElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGMPathElement', 562);
function OMSVGMarkerElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(558, 4, $intern_40, OMSVGMarkerElement);
var Lorg_vectomatic_dom_svg_OMSVGMarkerElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGMarkerElement', 558);
function OMSVGMaskElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(559, 4, $intern_40, OMSVGMaskElement);
var Lorg_vectomatic_dom_svg_OMSVGMaskElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGMaskElement', 559);
function OMSVGMetadataElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(560, 4, $intern_40, OMSVGMetadataElement);
var Lorg_vectomatic_dom_svg_OMSVGMetadataElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGMetadataElement', 560);
function OMSVGMissingGlyphElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(561, 4, $intern_40, OMSVGMissingGlyphElement);
var Lorg_vectomatic_dom_svg_OMSVGMissingGlyphElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGMissingGlyphElement', 561);
function $getItem_1(this$static, index_0){
  return this$static.ot.getItem(index_0);
}

function OMSVGNumberList(ot){
  this.ot = ot;
}

defineClass(586, 1, $intern_23, OMSVGNumberList);
_.iterator = function iterator_16(){
  return new OMSVGNumberList$1(this);
}
;
var Lorg_vectomatic_dom_svg_OMSVGNumberList_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGNumberList', 586);
function OMSVGNumberList$1(this$0){
  this.this$01 = this$0;
}

defineClass(408, 1, {}, OMSVGNumberList$1);
_.forEachRemaining = function forEachRemaining_16(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_15(){
  return $getItem_1(this.this$01, this.index_0++);
}
;
_.hasNext_0 = function hasNext_15(){
  return this.index_0 < this.this$01.ot.numberOfItems;
}
;
_.remove_0 = function remove_35(){
  throw toJs(new UnsupportedOperationException);
}
;
_.index_0 = 0;
var Lorg_vectomatic_dom_svg_OMSVGNumberList$1_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGNumberList/1', 408);
function OMSVGPathElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(47, 4, {7:1, 4:1, 47:1, 43:1}, OMSVGPathElement);
_.getTransform = function getTransform_8(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGPathElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGPathElement', 47);
function $appendItem_0(this$static, newItem){
  return this$static.ot.appendItem(newItem);
}

function $getItem_2(this$static, index_0){
  return this$static.ot.getItem(index_0);
}

function OMSVGPathSegList(ot){
  this.ot = ot;
}

defineClass(580, 1, $intern_23, OMSVGPathSegList);
_.iterator = function iterator_17(){
  return new OMSVGPathSegList$1(this);
}
;
var Lorg_vectomatic_dom_svg_OMSVGPathSegList_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGPathSegList', 580);
function OMSVGPathSegList$1(this$0){
  this.this$01 = this$0;
}

defineClass(391, 1, {}, OMSVGPathSegList$1);
_.forEachRemaining = function forEachRemaining_17(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_16(){
  return $getItem_2(this.this$01, this.index_0++);
}
;
_.hasNext_0 = function hasNext_16(){
  return this.index_0 < this.this$01.ot.numberOfItems;
}
;
_.remove_0 = function remove_36(){
  throw toJs(new UnsupportedOperationException);
}
;
_.index_0 = 0;
var Lorg_vectomatic_dom_svg_OMSVGPathSegList$1_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGPathSegList/1', 391);
function OMSVGPatternElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(563, 4, $intern_40, OMSVGPatternElement);
var Lorg_vectomatic_dom_svg_OMSVGPatternElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGPatternElement', 563);
function $getItem_3(this$static, index_0){
  return this$static.ot.getItem(index_0);
}

function OMSVGPointList(ot){
  this.ot = ot;
}

defineClass(587, 1, $intern_23, OMSVGPointList);
_.iterator = function iterator_18(){
  return new OMSVGPointList$1(this);
}
;
var Lorg_vectomatic_dom_svg_OMSVGPointList_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGPointList', 587);
function OMSVGPointList$1(this$0){
  this.this$01 = this$0;
}

defineClass(409, 1, {}, OMSVGPointList$1);
_.forEachRemaining = function forEachRemaining_18(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_17(){
  return $getItem_3(this.this$01, this.index_0++);
}
;
_.hasNext_0 = function hasNext_17(){
  return this.index_0 < this.this$01.ot.numberOfItems;
}
;
_.remove_0 = function remove_37(){
  throw toJs(new UnsupportedOperationException);
}
;
_.index_0 = 0;
var Lorg_vectomatic_dom_svg_OMSVGPointList$1_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGPointList/1', 409);
function OMSVGPolygonElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(564, 4, $intern_41, OMSVGPolygonElement);
_.getTransform = function getTransform_9(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGPolygonElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGPolygonElement', 564);
function OMSVGPolylineElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(565, 4, $intern_41, OMSVGPolylineElement);
_.getTransform = function getTransform_10(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGPolylineElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGPolylineElement', 565);
function OMSVGRadialGradientElement(ot){
  $clinit_OMNode();
  OMSVGGradientElement.call(this, ot);
}

defineClass(566, 225, $intern_40, OMSVGRadialGradientElement);
var Lorg_vectomatic_dom_svg_OMSVGRadialGradientElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGRadialGradientElement', 566);
function OMSVGRectElement(x_0, y_0, width_0, height, rx, ry){
  $clinit_OMNode();
  OMSVGRectElement_0.call(this, createElementNS(($clinit_DOMHelper() , $doc), 'http://www.w3.org/2000/svg', 'rect'));
  $setValue_2(this.ot.x.baseVal, x_0);
  $setValue_2(this.ot.y.baseVal, y_0);
  $setValue_2(this.ot.width.baseVal, width_0);
  $setValue_2(this.ot.height.baseVal, height);
  rx != 0 && $setValue_2(this.ot.rx.baseVal, rx);
  ry != 0 && $setValue_2(this.ot.ry.baseVal, ry);
}

function OMSVGRectElement_0(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(178, 4, $intern_41, OMSVGRectElement, OMSVGRectElement_0);
_.getTransform = function getTransform_11(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGRectElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGRectElement', 178);
function $createSVGLength(this$static, valueInSpecifiedUnits){
  var length_0;
  length_0 = this$static.ot.createSVGLength();
  length_0.newValueSpecifiedUnits(5, valueInSpecifiedUnits);
  return length_0;
}

function OMSVGSVGElement(){
  OMSVGSVGElement_0.call(this, createElementNS(($clinit_DOMHelper() , $doc), 'http://www.w3.org/2000/svg', 'svg'));
}

function OMSVGSVGElement_0(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(131, 4, {7:1, 4:1, 131:1}, OMSVGSVGElement, OMSVGSVGElement_0);
var Lorg_vectomatic_dom_svg_OMSVGSVGElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGSVGElement', 131);
function OMSVGScriptElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(567, 4, $intern_40, OMSVGScriptElement);
var Lorg_vectomatic_dom_svg_OMSVGScriptElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGScriptElement', 567);
function OMSVGSetElement(ot){
  $clinit_OMNode();
  OMSVGAnimationElement.call(this, ot);
}

defineClass(568, 134, $intern_40, OMSVGSetElement);
var Lorg_vectomatic_dom_svg_OMSVGSetElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGSetElement', 568);
function OMSVGStopElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(569, 4, $intern_40, OMSVGStopElement);
var Lorg_vectomatic_dom_svg_OMSVGStopElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGStopElement', 569);
function $getItem_4(this$static, index_0){
  return this$static.ot.getItem(index_0);
}

function OMSVGStringList(ot){
  this.ot = ot;
}

defineClass(588, 1, $intern_23, OMSVGStringList);
_.iterator = function iterator_19(){
  return new OMSVGStringList$1(this);
}
;
var Lorg_vectomatic_dom_svg_OMSVGStringList_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGStringList', 588);
function OMSVGStringList$1(this$0){
  this.this$01 = this$0;
}

defineClass(410, 1, {}, OMSVGStringList$1);
_.forEachRemaining = function forEachRemaining_19(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_18(){
  return $getItem_4(this.this$01, this.index_0++);
}
;
_.hasNext_0 = function hasNext_18(){
  return this.index_0 < this.this$01.ot.numberOfItems;
}
;
_.remove_0 = function remove_38(){
  throw toJs(new UnsupportedOperationException);
}
;
_.index_0 = 0;
var Lorg_vectomatic_dom_svg_OMSVGStringList$1_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGStringList/1', 410);
function OMSVGStyleElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(570, 4, $intern_40, OMSVGStyleElement);
var Lorg_vectomatic_dom_svg_OMSVGStyleElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGStyleElement', 570);
function OMSVGSwitchElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(571, 4, $intern_41, OMSVGSwitchElement);
_.getTransform = function getTransform_12(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGSwitchElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGSwitchElement', 571);
function OMSVGSymbolElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(572, 4, $intern_40, OMSVGSymbolElement);
var Lorg_vectomatic_dom_svg_OMSVGSymbolElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGSymbolElement', 572);
function OMSVGTRefElement(ot){
  $clinit_OMNode();
  OMSVGTextPositioningElement.call(this, ot);
}

defineClass(575, 152, $intern_40, OMSVGTRefElement);
var Lorg_vectomatic_dom_svg_OMSVGTRefElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGTRefElement', 575);
function OMSVGTSpanElement(ot){
  $clinit_OMNode();
  OMSVGTextPositioningElement.call(this, ot);
}

defineClass(576, 152, $intern_40, OMSVGTSpanElement);
var Lorg_vectomatic_dom_svg_OMSVGTSpanElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGTSpanElement', 576);
function OMSVGTextElement(x_0, y_0, data_0){
  $clinit_OMNode();
  var svg, xCoord, yCoord;
  OMSVGTextElement_0.call(this, createElementNS(($clinit_DOMHelper() , $doc), 'http://www.w3.org/2000/svg', 'text'));
  svg = new OMSVGSVGElement;
  xCoord = $createSVGLength(svg, x_0);
  $appendItem($getBaseVal(this.ot.x), xCoord);
  yCoord = $createSVGLength(svg, y_0);
  $appendItem($getBaseVal(this.ot.y), yCoord);
  $appendChild_1(this, new OMText_0(data_0));
}

function OMSVGTextElement_0(ot){
  $clinit_OMNode();
  OMSVGTextPositioningElement.call(this, ot);
}

defineClass(154, 152, $intern_41, OMSVGTextElement, OMSVGTextElement_0);
_.getTransform = function getTransform_13(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGTextElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGTextElement', 154);
function OMSVGTextPathElement(ot){
  $clinit_OMNode();
  OMSVGTextContentElement.call(this, ot);
}

defineClass(573, 224, $intern_40, OMSVGTextPathElement);
var Lorg_vectomatic_dom_svg_OMSVGTextPathElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGTextPathElement', 573);
function OMSVGTitleElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(574, 4, $intern_40, OMSVGTitleElement);
var Lorg_vectomatic_dom_svg_OMSVGTitleElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGTitleElement', 574);
function $setTranslate(this$static, tx, ty){
  this$static.setTranslate(tx, ty);
}

function $getItem_5(this$static, index_0){
  return this$static.ot.getItem(index_0);
}

function OMSVGTransformList(ot){
  this.ot = ot;
}

defineClass(502, 1, $intern_23, OMSVGTransformList);
_.iterator = function iterator_20(){
  return new OMSVGTransformList$1(this);
}
;
var Lorg_vectomatic_dom_svg_OMSVGTransformList_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGTransformList', 502);
function OMSVGTransformList$1(this$0){
  this.this$01 = this$0;
}

defineClass(352, 1, {}, OMSVGTransformList$1);
_.forEachRemaining = function forEachRemaining_20(consumer){
  $forEachRemaining(this, consumer);
}
;
_.next_1 = function next_19(){
  return $getItem_5(this.this$01, this.index_0++);
}
;
_.hasNext_0 = function hasNext_19(){
  return this.index_0 < this.this$01.ot.numberOfItems;
}
;
_.remove_0 = function remove_39(){
  throw toJs(new UnsupportedOperationException);
}
;
_.index_0 = 0;
var Lorg_vectomatic_dom_svg_OMSVGTransformList$1_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGTransformList/1', 352);
function OMSVGUseElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(577, 4, $intern_41, OMSVGUseElement);
_.getTransform = function getTransform_14(){
  return this.ot.transform;
}
;
var Lorg_vectomatic_dom_svg_OMSVGUseElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGUseElement', 577);
function OMSVGVKernElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(579, 4, $intern_40, OMSVGVKernElement);
var Lorg_vectomatic_dom_svg_OMSVGVKernElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGVKernElement', 579);
function OMSVGViewElement(ot){
  $clinit_OMNode();
  OMSVGElement.call(this, ot);
}

defineClass(578, 4, $intern_40, OMSVGViewElement);
var Lorg_vectomatic_dom_svg_OMSVGViewElement_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMSVGViewElement', 578);
function OMText(ot){
  $clinit_OMNode();
  OMNode.call(this, ot);
}

function OMText_0(data_0){
  OMText.call(this, $createTextNode($doc, data_0));
}

defineClass(221, 148, {}, OMText, OMText_0);
var Lorg_vectomatic_dom_svg_OMText_2_classLit = createForClass('org.vectomatic.dom.svg', 'OMText', 221);
function $addHandlerToSource(this$static, type_0, source, handler){
  if (!source) {
    throw toJs(new NullPointerException_1('Cannot add a handler with a null source'));
  }
  return $doAdd(this$static, type_0, source, handler);
}

function $defer(this$static, command){
  !this$static.deferredDeltas && (this$static.deferredDeltas = new ArrayList);
  $add_0(this$static.deferredDeltas, command);
}

function $doAdd(this$static, type_0, source, handler){
  var l;
  if (!type_0) {
    throw toJs(new NullPointerException_1('Cannot add a handler with a null type'));
  }
  if (!handler) {
    throw toJs(new NullPointerException_1('Cannot add a null handler'));
  }
  this$static.firingDepth > 0?$defer(this$static, new DOMEventBus$2(this$static, type_0, source, handler)):(l = $ensureHandlerList(this$static, type_0, source) , l.add(handler));
  return new DOMEventBus$1(this$static, type_0, source, handler);
}

function $doAddNow(this$static, type_0, source, handler){
  var l;
  l = $ensureHandlerList(this$static, type_0, source);
  l.add(handler);
}

function $doFire(this$static, event_0, source){
  var causes, e, handler, handlers, it;
  if (!event_0) {
    throw toJs(new NullPointerException_1('Cannot fire null event'));
  }
  try {
    ++this$static.firingDepth;
    handlers = $getDispatchList(this$static, event_0.getAssociatedType_0(), source);
    causes = null;
    it = handlers.listIterator();
    while (it.hasNext_0()) {
      handler = it.next_1();
      try {
        event_0.dispatch(castTo(handler, 233));
      }
       catch ($e0) {
        $e0 = toJava($e0);
        if (instanceOf($e0, 11)) {
          e = $e0;
          !causes && (causes = new HashSet);
          causes.map_0.put(e, causes);
        }
         else 
          throw toJs($e0);
      }
    }
    if (causes) {
      throw toJs(new UmbrellaException(causes));
    }
  }
   finally {
    --this$static.firingDepth;
    this$static.firingDepth == 0 && $handleQueuedAddsAndRemoves(this$static);
  }
}

function $doRemove(this$static, type_0, source, handler){
  this$static.firingDepth > 0?$defer(this$static, new DOMEventBus$3(this$static, type_0, source, handler)):$doRemoveNow(this$static, type_0, source, handler);
}

function $doRemoveNow(this$static, type_0, source, handler){
  var elem, eventName, l, removed, sourceMap;
  l = $getHandlerList(this$static, type_0, source);
  removed = l.remove(handler);
  if (removed && l.isEmpty()) {
    if (!!type_0 && !!source) {
      elem = source.ot;
      eventName = type_0.name_0;
      $clinit_DOMHelper();
      $unbindEventListener(impl_1, elem, eventName);
    }
    sourceMap = castTo($get_6(this$static.map_0, type_0), 21);
    castTo(sourceMap.remove(source), 13);
    sourceMap.isEmpty() && $remove_1(this$static.map_0, type_0);
  }
}

function $ensureHandlerList(this$static, type_0, source){
  var handlers, sourceMap;
  sourceMap = castTo($get_6(this$static.map_0, type_0), 21);
  if (!sourceMap) {
    sourceMap = new HashMap;
    $put_0(this$static.map_0, type_0, sourceMap);
  }
  handlers = castTo(sourceMap.get(source), 13);
  if (!handlers) {
    handlers = new ArrayList;
    sourceMap.put(source, handlers);
  }
  return handlers;
}

function $getDispatchList(this$static, type_0, source){
  var directHandlers, globalHandlers, rtn;
  directHandlers = $getHandlerList(this$static, type_0, source);
  if (!source) {
    return directHandlers;
  }
  globalHandlers = $getHandlerList(this$static, type_0, null);
  rtn = new ArrayList_1(directHandlers);
  $addAll_1(rtn, globalHandlers);
  return rtn;
}

function $getHandlerList(this$static, type_0, source){
  var handlers, sourceMap;
  sourceMap = castTo($get_6(this$static.map_0, type_0), 21);
  if (!sourceMap) {
    return $clinit_Collections() , $clinit_Collections() , EMPTY_LIST;
  }
  handlers = castTo(sourceMap.get(source), 13);
  if (!handlers) {
    return $clinit_Collections() , $clinit_Collections() , EMPTY_LIST;
  }
  return handlers;
}

function $handleQueuedAddsAndRemoves(this$static){
  var c, c$iterator;
  if (this$static.deferredDeltas) {
    try {
      for (c$iterator = new ArrayList$1(this$static.deferredDeltas); c$iterator.i < c$iterator.this$01.array.length;) {
        c = castTo($next_1(c$iterator), 462);
        c.execute_0();
      }
    }
     finally {
      this$static.deferredDeltas = null;
    }
  }
}

function DOMEventBus(){
  this.map_0 = new HashMap;
}

defineClass(348, 501, {}, DOMEventBus);
_.fireEvent = function fireEvent_1(event_0){
  $doFire(this, event_0, null);
}
;
_.firingDepth = 0;
var Lorg_vectomatic_dom_svg_impl_DOMEventBus_2_classLit = createForClass('org.vectomatic.dom.svg.impl', 'DOMEventBus', 348);
function $removeHandler(this$static){
  $doRemove(this$static.this$01, this$static.val$type2, this$static.val$source3, this$static.val$handler4);
}

function DOMEventBus$1(this$0, val$type, val$source, val$handler){
  this.this$01 = this$0;
  this.val$type2 = val$type;
  this.val$source3 = val$source;
  this.val$handler4 = val$handler;
}

defineClass(349, 1, {}, DOMEventBus$1);
var Lorg_vectomatic_dom_svg_impl_DOMEventBus$1_2_classLit = createForClass('org.vectomatic.dom.svg.impl', 'DOMEventBus/1', 349);
function DOMEventBus$2(this$0, val$type, val$source, val$handler){
  this.this$01 = this$0;
  this.val$type2 = val$type;
  this.val$source3 = val$source;
  this.val$handler4 = val$handler;
}

defineClass(350, 1, {462:1}, DOMEventBus$2);
_.execute_0 = function execute(){
  $doAddNow(this.this$01, this.val$type2, this.val$source3, this.val$handler4);
}
;
var Lorg_vectomatic_dom_svg_impl_DOMEventBus$2_2_classLit = createForClass('org.vectomatic.dom.svg.impl', 'DOMEventBus/2', 350);
function DOMEventBus$3(this$0, val$type, val$source, val$handler){
  this.this$01 = this$0;
  this.val$type2 = val$type;
  this.val$source3 = val$source;
  this.val$handler4 = val$handler;
}

defineClass(351, 1, {462:1}, DOMEventBus$3);
_.execute_0 = function execute_0(){
  $doRemoveNow(this.this$01, this.val$type2, this.val$source3, this.val$handler4);
}
;
var Lorg_vectomatic_dom_svg_impl_DOMEventBus$3_2_classLit = createForClass('org.vectomatic.dom.svg.impl', 'DOMEventBus/3', 351);
function $bindEventListener(this$static, elem, eventName){
  $init(this$static);
  elem.addEventListener(eventName, $wnd.__svgDispatch, false);
}

function $dispatch_1(event_0, node){
  var eventName;
  $flushEntryCommands(($clinit_SchedulerImpl() , INSTANCE));
  eventName = event_0.type;
  if ($equals_0('mouseover', eventName) || $equals_0('mouseout', eventName)) {
    if ($isChildOf(event_0.currentTarget, $eventGetRelatedTarget(event_0))) {
      return;
    }
  }
  fireNativeEvent(event_0, node, event_0.currentTarget);
  $flushFinallyCommands(INSTANCE);
}

function $init(this$static){
  if (!eventsInitialized) {
    eventsInitialized = true;
    $initEventSystem_0(this$static);
  }
}

function $initEventSystem_0(this$static){
  $wnd.__helperImpl = this$static;
  $wnd.__svgDispatch = function(evt){
    $wnd.__helperImpl.dispatch_0(evt, evt.currentTarget.__wrapper, evt.currentTarget);
  }
  ;
  $wnd.__svgCapture = function(evt){
    $wnd.__helperImpl.dispatchCapturedEvent(evt, evt.currentTarget);
  }
  ;
  $wnd.addEventListener('mousedown', $wnd.__svgCapture, true);
  $wnd.addEventListener('mouseup', $wnd.__svgCapture, true);
  $wnd.addEventListener('mousemove', $wnd.__svgCapture, true);
  $wnd.addEventListener('mouseover', $wnd.__svgCapture, true);
  $wnd.addEventListener('mouseout', $wnd.__svgCapture, true);
  $wnd.addEventListener('mousewheel', $wnd.__svgCapture, true);
  $wnd.addEventListener('click', $wnd.__svgCapture, true);
  $wnd.addEventListener('focusin', $wnd.__svgCapture, true);
  $wnd.addEventListener('focusout', $wnd.__svgCapture, true);
  $wnd.addEventListener('activate', $wnd.__svgCapture, true);
  $wnd.addEventListener('touchstart', $wnd.__svgCapture, true);
  $wnd.addEventListener('touchend', $wnd.__svgCapture, true);
  $wnd.addEventListener('touchmove', $wnd.__svgCapture, true);
  $wnd.addEventListener('touchcancel', $wnd.__svgCapture, true);
  $wnd.addEventListener('gesturestart', $wnd.__svgCapture, true);
  $wnd.addEventListener('gesturechange', $wnd.__svgCapture, true);
  $wnd.addEventListener('gestureend', $wnd.__svgCapture, true);
  $wnd.addEventListener('dragstart', $wnd.__svgCapture, true);
  $wnd.addEventListener('drag', $wnd.__svgCapture, true);
  $wnd.addEventListener('dragenter', $wnd.__svgCapture, true);
  $wnd.addEventListener('dragleave', $wnd.__svgCapture, true);
  $wnd.addEventListener('dragover', $wnd.__svgCapture, true);
  $wnd.addEventListener('drop', $wnd.__svgCapture, true);
  $wnd.addEventListener('dragend', $wnd.__svgCapture, true);
}

function $initXPath(){
  $wnd.xpp = new XPathParser;
  SvgNamespaceResolver.prototype = new NamespaceResolver;
  SvgNamespaceResolver.prototype.constructor = SvgNamespaceResolver;
  SvgNamespaceResolver.superclass = NamespaceResolver.prototype;
  function SvgNamespaceResolver(){
    this.gwtresolver = null;
  }

  SvgNamespaceResolver.prototype.getNamespace = function(prefix, n){
    var ns = null;
    this.gwtresolver != null && (ns = this.gwtresolver(prefix));
    ns == null && (ns = n.namespaceURI);
    ns == null && (ns = SvgNamespaceResolver.superclass.getNamespace(prefix, n));
    return ns;
  }
  ;
  $wnd.xpr = new SvgNamespaceResolver;
}

function $isChildOf(root, node){
  while (node != null && node != root && node.nodeName != 'BODY') {
    node = node.parentNode;
  }
  if (node === root) {
    return true;
  }
  return false;
}

function $unbindEventListener(this$static, elem, eventName){
  $init(this$static);
  elem.removeEventListener(eventName, $wnd.__svgDispatch, false);
}

function DOMHelperImpl(){
  var doc, doc1, doc2, scriptElem, scriptElem1, scriptElem2, builder;
  if (!(typeof Document.prototype.evaluate === 'function')) {
    doc = document;
    scriptElem = $createScriptElement(doc, (builder = new StringBuilder , builder.string += '/*\n * xpath.js\n *\n * An XPath 1.0 library for JavaScript.\n *\n * Cameron McCormack <cam (at) mcc.id.au>\n *\n * This work is licensed under the Creative Commons Attribution-ShareAlike\n * License. To view a copy of this license, visit\n * \n *   http://creativecommons.org/licenses/by-sa/2.0/\n *\n * or send a letter to Creative Commons, 559 Nathan Abbott Way, Stanford,\n * California 94305, USA.\n *\n * Revision 20: April 26, 2011\n *   Fixed a typo resulting in FIRST_ORDERED_NODE_TYPE results being wrong,\n *   thanks to <shi_a009 (at) hotmail.com>.\n *\n * Revision 19: November 29, 2005\n *   Nodesets now store their nodes in a height balanced tree, increasing\n *   performance for the common case of selecting nodes in document order,\n *   thanks to S\uFFFDbastien Cramatte <contact (at) zeninteractif.com>.\n *   AVL tree code adapted from Raimund Neumann <rnova (at) gmx.net>.\n *\n * Revision 18: October 27, 2005\n *   DOM 3 XPath support.  Caveats:\n *     - namespace prefixes aren\'t resolved in XPathEvaluator.createExpression,\n *       but in XPathExpression.evaluate.\n *     - XPathResult.invalidIteratorState is not implemented.\n *\n * Revision 17: October 25, 2005\n *   Some core XPath function fixes and a patch to avoid crashing certain\n *   versions of MSXML in PathExpr.prototype.getOwnerElement, thanks to\n *   S\uFFFDbastien Cramatte <contact (at) zeninteractif.com>.\n *\n * Revision 16: September 22, 2005\n *   Workarounds for some IE 5.5 deficiencies.\n *   Fixed problem with prefix node tests on attribute nodes.\n *\n * Revision 15: May 21, 2005\n *   Fixed problem with QName node tests on elements with an xmlns="...".\n *\n * Revision 14: May 19, 2005\n *   Fixed QName node tests on attribute node regression.\n *\n * Revision 13: May 3, 2005\n *   Node tests are case insensitive now if working in an HTML DOM.\n *\n * Revision 12: April 26, 2005\n *   Updated licence.  Slight code changes to enable use of Dean\n *   Edwards\' script compression, http://dean.edwards.name/packer/ .\n *\n * Revision 11: April 23, 2005\n *   Fixed bug with \'and\' and \'or\' operators, fix thanks to\n *   Sandy McArthur <sandy (at) mcarthur.org>.\n *\n * Revision 10: April 15, 2005\n *   Added support for a virtual root node, supposedly helpful for\n *   implementing XForms.  Fixed problem with QName node tests and\n *   the parent axis.\n *\n * Revision 9: March 17, 2005\n *   Namespace resolver tweaked so using the document node as the context\n *   for namespace lookups is equivalent to using the document element.\n *\n * Revision 8: February 13, 2005\n *   Handle implicit declaration of \'xmlns\' namespace prefix.\n *   Fixed bug when comparing nodesets.\n *   Instance data can now be associated with a FunctionResolver, and\n *     workaround for MSXML not supporting \'localName\' and \'getElementById\',\n *     thanks to Grant Gongaware.\n *   Fix a few problems when the context node is the root node.\n *   \n * Revision 7: February 11, 2005\n *   Default namespace resolver fix from Grant Gongaware\n *   <grant (at) gongaware.com>.\n *\n * Revision 6: February 10, 2005\n *   Fixed bug in \'number\' function.\n *\n * Revision 5: February 9, 2005\n *   Fixed bug where text nodes not getting converted to string values.\n *\n * Revision 4: January 21, 2005\n *   Bug in \'name\' function, fix thanks to Bill Edney.\n *   Fixed incorrect processing of namespace nodes.\n *   Fixed NamespaceResolver to resolve \'xml\' namespace.\n *   Implemented union \'|\' operator.\n *\n * Revision 3: January 14, 2005\n *   Fixed bug with nodeset comparisons, bug lexing < and >.\n *\n * Revision 2: October 26, 2004\n *   QName node test namespace handling fixed.  Few other bug fixes.\n *   \n * Revision 1: August 13, 2004\n *   Bug fixes from William J. Edney <bedney (at) technicalpursuit.com>.\n *   Added minimal licence.\n *\n * Initial version: June 14, 2004\n */\n\n// XPathParser ///////////////////////////////////////////////////////////////\n\nXPathParser.prototype = new Object();\nXPathParser.prototype.constructor = XPathParser;\nXPathParser.superclass = Object.prototype;\n\nfunction XPathParser() {\n\tthis.init();\n}\n\nXPathParser.prototype.init = function() {\n\tthis.reduceActions = [];\n\n\tthis.reduceActions[3] = function(rhs) {\n\t\treturn new OrOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[5] = function(rhs) {\n\t\treturn new AndOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[7] = function(rhs) {\n\t\treturn new EqualsOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[8] = function(rhs) {\n\t\treturn new NotEqualOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[10] = function(rhs) {\n\t\treturn new LessThanOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[11] = function(rhs) {\n\t\treturn new GreaterThanOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[12] = function(rhs) {\n\t\treturn new LessThanOrEqualOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[13] = function(rhs) {\n\t\treturn new GreaterThanOrEqualOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[15] = function(rhs) {\n\t\treturn new PlusOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[16] = function(rhs) {\n\t\treturn new MinusOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[18] = function(rhs) {\n\t\treturn new MultiplyOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[19] = function(rhs) {\n\t\treturn new DivOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[20] = function(rhs) {\n\t\treturn new ModOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[22] = function(rhs) {\n\t\treturn new UnaryMinusOperation(rhs[1]);\n\t};\n\tthis.reduceActions[24] = function(rhs) {\n\t\treturn new BarOperation(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[25] = function(rhs) {\n\t\treturn new PathExpr(undefined, undefined, rhs[0]);\n\t};\n\tthis.reduceActions[27] = function(rhs) {\n\t\trhs[0].locationPath = rhs[2];\n\t\treturn rhs[0];\n\t};\n\tthis.reduceActions[28] = function(rhs) {\n\t\trhs[0].locationPath = rhs[2];\n\t\trhs[0].locationPath.steps.unshift(new Step(Step.DESCENDANTORSELF, new NodeTest(NodeTest.NODE, undefined), []));\n\t\treturn rhs[0];\n\t};\n\tthis.reduceActions[29] = function(rhs) {\n\t\treturn new PathExpr(rhs[0], [], undefined);\n\t};\n\tthis.reduceActions[30] = function(rhs) {\n\t\tif (Utilities.instance_of(rhs[0], PathExpr)) {\n\t\t\tif (rhs[0].filterPredicates == undefined) {\n\t\t\t\trhs[0].filterPredicates = [];\n\t\t\t}\n\t\t\trhs[0].filterPredicates.push(rhs[1]);\n\t\t\treturn rhs[0];\n\t\t} else {\n\t\t\treturn new PathExpr(rhs[0], [rhs[1]], undefined);\n\t\t}\n\t};\n\tthis.reduceActions[32] = function(rhs) {\n\t\treturn rhs[1];\n\t};\n\tthis.reduceActions[33] = function(rhs) {\n\t\treturn new XString(rhs[0]);\n\t};\n\tthis.reduceActions[34] = function(rhs) {\n\t\treturn new XNumber(rhs[0]);\n\t};\n\tthis.reduceActions[36] = function(rhs) {\n\t\treturn new FunctionCall(rhs[0], []);\n\t};\n\tthis.reduceActions[37] = function(rhs) {\n\t\treturn new FunctionCall(rhs[0], rhs[2]);\n\t};\n\tthis.reduceActions[38] = function(rhs) {\n\t\treturn [ rhs[0] ];\n\t};\n\tthis.reduceActions[39] = function(rhs) {\n\t\trhs[2].unshift(rhs[0]);\n\t\treturn rhs[2];\n\t};\n\tthis.reduceActions[43] = function(rhs) {\n\t\treturn new LocationPath(true, []);\n\t};\n\tthis.reduceActions[44] = function(rhs) {\n\t\trhs[1].absolute = true;\n\t\treturn rhs[1];\n\t};\n\tthis.reduceActions[46] = function(rhs) {\n\t\treturn new LocationPath(false, [ rhs[0] ]);\n\t};\n\tthis.reduceActions[47] = function(rhs) {\n\t\trhs[0].steps.push(rhs[2]);\n\t\treturn rhs[0];\n\t};\n\tthis.reduceActions[49] = function(rhs) {\n\t\treturn new Step(rhs[0], rhs[1], []);\n\t};\n\tthis.reduceActions[50] = function(rhs) {\n\t\treturn new Step(Step.CHILD, rhs[0], []);\n\t};\n\tthis.reduceActions[51] = function(rhs) {\n\t\treturn new Step(rhs[0], rhs[1], rhs[2]);\n\t};\n\tthis.reduceActions[52] = function(rhs) {\n\t\treturn new Step(Step.CHILD, rhs[0], rhs[1]);\n\t};\n\tthis.reduceActions[54] = function(rhs) {\n\t\treturn [ rhs[0] ];\n\t};\n\tthis.reduceActions[55] = function(rhs) {\n\t\trhs[1].unshift(rhs[0]);\n\t\treturn rhs[1];\n\t};\n\tthis.reduceActions[56] = function(rhs) {\n\t\tif (rhs[0] == "ancestor") {\n\t\t\treturn Step.ANCESTOR;\n\t\t} else if (rhs[0] == "ancestor-or-self") {\n\t\t\treturn Step.ANCESTORORSELF;\n\t\t} else if (rhs[0] == "attribute") {\n\t\t\treturn Step.ATTRIBUTE;\n\t\t} else if (rhs[0] == "child") {\n\t\t\treturn Step.CHILD;\n\t\t} else if (rhs[0] == "descendant") {\n\t\t\treturn Step.DESCENDANT;\n\t\t} else if (rhs[0] == "descendant-or-self") {\n\t\t\treturn Step.DESCENDANTORSELF;\n\t\t} else if (rhs[0] == "following") {\n\t\t\treturn Step.FOLLOWING;\n\t\t} else if (rhs[0] == "following-sibling") {\n\t\t\treturn Step.FOLLOWINGSIBLING;\n\t\t} else if (rhs[0] == "namespace") {\n\t\t\treturn Step.NAMESPACE;\n\t\t} else if (rhs[0] == "parent") {\n\t\t\treturn Step.PARENT;\n\t\t} else if (rhs[0] == "preceding") {\n\t\t\treturn Step.PRECEDING;\n\t\t} else if (rhs[0] == "preceding-sibling") {\n\t\t\treturn Step.PRECEDINGSIBLING;\n\t\t} else if (rhs[0] == "self") {\n\t\t\treturn Step.SELF;\n\t\t}\n\t\treturn -1;\n\t};\n\tthis.reduceActions[57] = function(rhs) {\n\t\treturn Step.ATTRIBUTE;\n\t};\n\tthis.reduceActions[59] = function(rhs) {\n\t\tif (rhs[0] == "comment") {\n\t\t\treturn new NodeTest(NodeTest.COMMENT, undefined);\n\t\t} else if (rhs[0] == "text") {\n\t\t\treturn new NodeTest(NodeTest.TEXT, undefined);\n\t\t} else if (rhs[0] == "processing-instruction") {\n\t\t\treturn new NodeTest(NodeTest.PI, undefined);\n\t\t} else if (rhs[0] == "node") {\n\t\t\treturn new NodeTest(NodeTest.NODE, undefined);\n\t\t}\n\t\treturn new NodeTest(-1, undefined);\n\t};\n\tthis.reduceActions[60] = function(rhs) {\n\t\treturn new NodeTest(NodeTest.PI, rhs[2]);\n\t};\n\tthis.reduceActions[61] = function(rhs) {\n\t\treturn rhs[1];\n\t};\n\tthis.reduceActions[63] = function(rhs) {\n\t\trhs[1].absolute = true;\n\t\trhs[1].steps.unshift(new Step(Step.DESCENDANTORSELF, new NodeTest(NodeTest.NODE, undefined), []));\n\t\treturn rhs[1];\n\t};\n\tthis.reduceActions[64] = function(rhs) {\n\t\trhs[0].steps.push(new Step(Step.DESCENDANTORSELF, new NodeTest(NodeTest.NODE, undefined), []));\n\t\trhs[0].steps.push(rhs[2]);\n\t\treturn rhs[0];\n\t};\n\tthis.reduceActions[65] = function(rhs) {\n\t\treturn new Step(Step.SELF, new NodeTest(NodeTest.NODE, undefined), []);\n\t};\n\tthis.reduceActions[66] = function(rhs) {\n\t\treturn new Step(Step.PARENT, new NodeTest(NodeTest.NODE, undefined), []);\n\t};\n\tthis.reduceActions[67] = function(rhs) {\n\t\treturn new VariableReference(rhs[1]);\n\t};\n\tthis.reduceActions[68] = function(rhs) {\n\t\treturn new NodeTest(NodeTest.NAMETESTANY, undefined);\n\t};\n\tthis.reduceActions[69] = function(rhs) {\n\t\tvar prefix = rhs[0].substring(0, rhs[0].indexOf(":"));\n\t\treturn new NodeTest(NodeTest.NAMETESTPREFIXANY, prefix);\n\t};\n\tthis.reduceActions[70] = function(rhs) {\n\t\treturn new NodeTest(NodeTest.NAMETESTQNAME, rhs[0]);\n\t};\n};\n\nXPathParser.actionTable = [\n\t" s s        sssssssss    s ss  s  ss",\n\t"                 s                  ",\n\t"r  rrrrrrrrr         rrrrrrr rr  r  ",\n\t"                rrrrr               ",\n\t" s s        sssssssss    s ss  s  ss",\n\t"rs  rrrrrrrr s  sssssrrrrrr  rrs rs ",\n\t" s s        sssssssss    s ss  s  ss",\n\t"                            s       ",\n\t"                            s       ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"  s                                 ",\n\t"                            s       ",\n\t" s           s  sssss          s  s ",\n\t"r  rrrrrrrrr         rrrrrrr rr  r  ",\n\t"a                                   ",\n\t"r       s                    rr  r  ",\n\t"r      sr                    rr  r  ",\n\t"r   s  rr            s       rr  r  ",\n\t"r   rssrr            rss     rr  r  ",\n\t"r   rrrrr            rrrss   rr  r  ",\n\t"r   rrrrrsss         rrrrr   rr  r  ",\n\t"r   rrrrrrrr         rrrrr   rr  r  ",\n\t"r   rrrrrrrr         rrrrrs  rr  r  ",\n\t"r   rrrrrrrr         rrrrrr  rr  r  ",\n\t"r   rrrrrrrr         rrrrrr  rr  r  ",\n\t"r  srrrrrrrr         rrrrrrs rr sr  ",\n\t"r  srrrrrrrr         rrrrrrs rr  r  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"r   rrrrrrrr         rrrrrr  rr  r  ",\n\t"r   rrrrrrrr         rrrrrr  rr  r  ",\n\t"r  rrrrrrrrr         rrrrrrr rr  r  ",\n\t"r  rrrrrrrrr         rrrrrrr rr  r  ",\n\t"                sssss               ",\n\t"r  rrrrrrrrr         rrrrrrr rr sr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr  r  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"                             s      ",\n\t"r  srrrrrrrr         rrrrrrs rr  r  ",\n\t"r   rrrrrrrr         rrrrr   rr  r  ",\n\t"              s                     ",\n\t"                             s      ",\n\t"                rrrrr               ",\n\t" s s        sssssssss    s sss s  ss",\n\t"r  srrrrrrrr         rrrrrrs rr  r  ",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s s        sssssssss      ss  s  ss",\n\t" s s        sssssssss    s ss  s  ss",\n\t" s           s  sssss          s  s ",\n\t" s           s  sssss          s  s ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t" s           s  sssss          s  s ",\n\t" s           s  sssss          s  s ",\n\t"r  rrrrrrrrr         rrrrrrr rr sr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr sr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr  r  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"                             s      ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"                             rr     ",\n\t"                             s      ",\n\t"                             rs     ",\n\t"r      sr                    rr  r  ",\n\t"r   s  rr            s       rr  r  ",\n\t"r   rssrr            rss     rr  r  ",\n\t"r   rssrr            rss     rr  r  ",\n\t"r   rrrrr            rrrss   rr  r  ",\n\t"r   rrrrr            rrrss   rr  r  ",\n\t"r   rrrrr            rrrss   rr  r  ",\n\t"r   rrrrr            rrrss   rr  r  ",\n\t"r   rrrrrsss         rrrrr   rr  r  ",\n\t"r   rrrrrsss         rrrrr   rr  r  ",\n\t"r   rrrrrrrr         rrrrr   rr  r  ",\n\t"r   rrrrrrrr         rrrrr   rr  r  ",\n\t"r   rrrrrrrr         rrrrr   rr  r  ",\n\t"r   rrrrrrrr         rrrrrr  rr  r  ",\n\t"                                 r  ",\n\t"                                 s  ",\n\t"r  srrrrrrrr         rrrrrrs rr  r  ",\n\t"r  srrrrrrrr         rrrrrrs rr  r  ",\n\t"r  rrrrrrrrr         rrrrrrr rr  r  ",\n\t"r  rrrrrrrrr         rrrrrrr rr  r  ",\n\t"r  rrrrrrrrr         rrrrrrr rr  r  ",\n\t"r  rrrrrrrrr         rrrrrrr rr  r  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t" s s        sssssssss    s ss  s  ss",\n\t"r  rrrrrrrrr         rrrrrrr rr rr  ",\n\t"                             r      "\n];\n\nXPathParser.actionTableNumber = [\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t"                 J                  ",\n\t"a  aaaaaaaaa         aaaaaaa aa  a  ",\n\t"                YYYYY               ",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t"K1  KKKKKKKK .  +*)(\'KKKKKK  KK# K\\" ",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t"                            N       ",\n\t"                            O       ",\n\t"e  eeeeeeeee         eeeeeee ee ee  ",\n\t"f  fffffffff         fffffff ff ff  ",\n\t"d  ddddddddd         ddddddd dd dd  ",\n\t"B  BBBBBBBBB         BBBBBBB BB BB  ",\n\t"A  AAAAAAAAA         AAAAAAA AA AA  ",\n\t"  P                                 ",\n\t"                            Q       ",\n\t" 1           .  +*)(\'          #  \\" ",\n\t"b  bbbbbbbbb         bbbbbbb bb  b  ",\n\t"                                    ",\n\t"!       S                    !!  !  ",\n\t"\\"      T\\"                    \\"\\"  \\"  ",\n\t"$   V  $$            U       $$  $  ",\n\t"&   &ZY&&            &XW     &&  &  ",\n\t")   )))))            )))\\\\[   ))  )  ",\n\t".   ....._^]         .....   ..  .  ",\n\t"1   11111111         11111   11  1  ",\n\t"5   55555555         55555`  55  5  ",\n\t"7   77777777         777777  77  7  ",\n\t"9   99999999         999999  99  9  ",\n\t":  c::::::::         ::::::b :: a:  ",\n\t"I  fIIIIIIII         IIIIIIe II  I  ",\n\t"=  =========         ======= == ==  ",\n\t"?  ?????????         ??????? ?? ??  ",\n\t"C  CCCCCCCCC         CCCCCCC CC CC  ",\n\t"J   JJJJJJJJ         JJJJJJ  JJ  J  ",\n\t"M   MMMMMMMM         MMMMMM  MM  M  ",\n\t"N  NNNNNNNNN         NNNNNNN' , builder.string += ' NN  N  ",\n\t"P  PPPPPPPPP         PPPPPPP PP  P  ",\n\t"                +*)(\'               ",\n\t"R  RRRRRRRRR         RRRRRRR RR aR  ",\n\t"U  UUUUUUUUU         UUUUUUU UU  U  ",\n\t"Z  ZZZZZZZZZ         ZZZZZZZ ZZ ZZ  ",\n\t"c  ccccccccc         ccccccc cc cc  ",\n\t"                             j      ",\n\t"L  fLLLLLLLL         LLLLLLe LL  L  ",\n\t"6   66666666         66666   66  6  ",\n\t"              k                     ",\n\t"                             l      ",\n\t"                XXXXX               ",\n\t" 1 0        /.-,+*)(\'    & %$m #  \\"!",\n\t"_  f________         ______e __  _  ",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'      %$  #  \\"!",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t" 1           .  +*)(\'          #  \\" ",\n\t" 1           .  +*)(\'          #  \\" ",\n\t">  >>>>>>>>>         >>>>>>> >> >>  ",\n\t" 1           .  +*)(\'          #  \\" ",\n\t" 1           .  +*)(\'          #  \\" ",\n\t"Q  QQQQQQQQQ         QQQQQQQ QQ aQ  ",\n\t"V  VVVVVVVVV         VVVVVVV VV aV  ",\n\t"T  TTTTTTTTT         TTTTTTT TT  T  ",\n\t"@  @@@@@@@@@         @@@@@@@ @@ @@  ",\n\t"                             \\x87      ",\n\t"[  [[[[[[[[[         [[[[[[[ [[ [[  ",\n\t"D  DDDDDDDDD         DDDDDDD DD DD  ",\n\t"                             HH     ",\n\t"                             \\x88      ",\n\t"                             F\\x89     ",\n\t"#      T#                    ##  #  ",\n\t"%   V  %%            U       %%  %  ",\n\t"\'   \'ZY\'\'            \'XW     \'\'  \'  ",\n\t"(   (ZY((            (XW     ((  (  ",\n\t"+   +++++            +++\\\\[   ++  +  ",\n\t"*   *****            ***\\\\[   **  *  ",\n\t"-   -----            ---\\\\[   --  -  ",\n\t",   ,,,,,            ,,,\\\\[   ,,  ,  ",\n\t"0   00000_^]         00000   00  0  ",\n\t"/   /////_^]         /////   //  /  ",\n\t"2   22222222         22222   22  2  ",\n\t"3   33333333         33333   33  3  ",\n\t"4   44444444         44444   44  4  ",\n\t"8   88888888         888888  88  8  ",\n\t"                                 ^  ",\n\t"                                 \\x8a  ",\n\t";  f;;;;;;;;         ;;;;;;e ;;  ;  ",\n\t"<  f<<<<<<<<         <<<<<<e <<  <  ",\n\t"O  OOOOOOOOO         OOOOOOO OO  O  ",\n\t"`  `````````         ``````` ``  `  ",\n\t"S  SSSSSSSSS         SSSSSSS SS  S  ",\n\t"W  WWWWWWWWW         WWWWWWW WW  W  ",\n\t"\\\\  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\         \\\\\\\\\\\\\\\\\\\\\\\\\\\\ \\\\\\\\ \\\\\\\\  ",\n\t"E  EEEEEEEEE         EEEEEEE EE EE  ",\n\t" 1 0        /.-,+*)(\'    & %$  #  \\"!",\n\t"]  ]]]]]]]]]         ]]]]]]] ]] ]]  ",\n\t"                             G      "\n];\n\nXPathParser.gotoTable = [\n\t"3456789:;<=>?@ AB  CDEFGH IJ ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"L456789:;<=>?@ AB  CDEFGH IJ ",\n\t"            M        EFGH IJ ",\n\t"       N;<=>?@ AB  CDEFGH IJ ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"            S        EFGH IJ ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"              e              ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                        h  J ",\n\t"              i          j   ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"o456789:;<=>?@ ABpqCDEFGH IJ ",\n\t"                             ",\n\t"  r6789:;<=>?@ AB  CDEFGH IJ ",\n\t"   s789:;<=>?@ AB  CDEFGH IJ ",\n\t"    t89:;<=>?@ AB  CDEFGH IJ ",\n\t"    u89:;<=>?@ AB  CDEFGH IJ ",\n\t"     v9:;<=>?@ AB  CDEFGH IJ ",\n\t"     w9:;<=>?@ AB  CDEFGH IJ ",\n\t"     x9:;<=>?@ AB  CDEFGH IJ ",\n\t"     y9:;<=>?@ AB  CDEFGH IJ ",\n\t"      z:;<=>?@ AB  CDEFGH IJ ",\n\t"      {:;<=>?@ AB  CDEFGH IJ ",\n\t"       |;<=>?@ AB  CDEFGH IJ ",\n\t"       };<=>?@ AB  CDEFGH IJ ",\n\t"       ~;<=>?@ AB  CDEFGH IJ ",\n\t"         \\x7f=>?@ AB  CDEFGH IJ ",\n\t"\\x80456789:;<=>?@ AB  CDEFGH IJ\\x81",\n\t"            \\x82        EFGH IJ ",\n\t"            \\x83        EFGH IJ ",\n\t"                             ",\n\t"                     \\x84 GH IJ ",\n\t"                     \\x85 GH IJ ",\n\t"              i          \\x86   ",\n\t"              i          \\x87   ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"                             ",\n\t"o456789:;<=>?@ AB\\x8cqCDEFGH IJ ",\n\t"                             ",\n\t"                             "\n];\n\nXPathParser.productions = [\n\t[1, 1, 2],\n\t[2, 1, 3],\n\t[3, 1, 4],\n\t[3, 3, 3, -9, 4],\n\t[4, 1, 5],\n\t[4, 3, 4, -8, 5],\n\t[5, 1, 6],\n\t[5, 3, 5, -22, 6],\n\t[5, 3, 5, -5, 6],\n\t[6, 1, 7],\n\t[6, 3, 6, -23, 7],\n\t[6, 3, 6, -24, 7],\n\t[6, 3, 6, -6, 7],\n\t[6, 3, 6, -7, 7],\n\t[7, 1, 8],\n\t[7, 3, 7, -25, 8],\n\t[7, 3, 7, -26, 8],\n\t[8, 1, 9],\n\t[8, 3, 8, -12, 9],\n\t[8, 3, 8, -11, 9],\n\t[8, 3, 8, -10, 9],\n\t[9, 1, 10],\n\t[9, 2, -26, 9],\n\t[10, 1, 11],\n\t[10, 3, 10, -27, 11],\n\t[11, 1, 12],\n\t[11, 1, 13],\n\t[11, 3, 13, -28, 14],\n\t[11, 3, 13, -4, 14],\n\t[13, 1, 15],\n\t[13, 2, 13, 16],\n\t[15, 1, 17],\n\t[15, 3, -29, 2, -30],\n\t[15, 1, -15],\n\t[15, 1, -16],\n\t[15, 1, 18],\n\t[18, 3, -13, -29, -30],\n\t[18, 4, -13, -29, 19, -30],\n\t[19, 1, 20],\n\t[19, 3, 20, -31, 19],\n\t[20, 1, 2],\n\t[12, 1, 14],\n\t[12, 1, 21],\n\t[21, 1, -28],\n\t[21, 2, -28, 14],\n\t[21, 1, 22],\n\t[14, 1, 23],\n\t[14, 3, 14, -28, 23],\n\t[14, 1, 24],\n\t[23, 2, 25, 26],\n\t[23, 1, 26],\n\t[23, 3, 25, 26, 27],\n\t[23, 2, 26, 27],\n\t[23, 1, 28],\n\t[27, 1, 16],\n\t[27, 2, 16, 27],\n\t[25, 2, -14, -3],\n\t[25, 1, -32],\n\t[26, 1, 29],\n\t[26, 3, -20, -29, -30],\n\t[26, 4, -21, -29, -15, -30],\n\t[16, 3, -33, 30, -34],\n\t[30, 1, 2],\n\t[22, 2, -4, 14],\n\t[24, 3, 14, -4, 23],\n\t[28, 1, -35],\n\t[28, 1, -2],\n\t[17, 2, -36, -18],\n\t[29, 1, -17],\n\t[29, 1, -19],\n\t[29, 1, -18]\n];\n\nXPathParser.DOUBLEDOT = 2;\nXPathParser.DOUBLECOLON = 3;\nXPathParser.DOUBLESLASH = 4;\nXPathParser.NOTEQUAL = 5;\nXPathParser.LESSTHANOREQUAL = 6;\nXPathParser.GREATERTHANOREQUAL = 7;\nXPathParser.AND = 8;\nXPathParser.OR = 9;\nXPathParser.MOD = 10;\nXPathParser.DIV = 11;\nXPathParser.MULTIPLYOPERATOR = 12;\nXPathParser.FUNCTIONNAME = 13;\nXPathParser.AXISNAME = 14;\nXPathParser.LITERAL = 15;\nXPathParser.NUMBER = 16;\nXPathParser.ASTERISKNAMETEST = 17;\nXPathParser.QNAME = 18;\nXPathParser.NCNAMECOLONASTERISK = 19;\nXPathParser.NODETYPE = 20;\nXPathParser.PROCESSINGINSTRUCTIONWITHLITERAL = 21;\nXPathParser.EQUALS = 22;\nXPathParser.LESSTHAN = 23;\nXPathParser.GREATERTHAN = 24;\nXPathParser.PLUS = 25;\nXPathParser.MINUS = 26;\nXPathParser.BAR = 27;\nXPathParser.SLASH = 28;\nXPathParser.LEFTPARENTHESIS = 29;\nXPathParser.RIGHTPARENTHESIS = 30;\nXPathParser.COMMA = 31;\nXPathParser.AT = 32;\nXPathParser.LEFTBRACKET = 33;\nXPathParser.RIGHTBRACKET = 34;\nXPathParser.DOT = 35;\nXPathParser.DOLLAR = 36;\n\nXPathParser.prototype.tokenize = function(s1) {\n\tvar types = [];\n\tvar values = [];\n\tvar s = s1 + \'\\0\';\n\n\tvar pos = 0;\n\tvar c = s.charAt(pos++);\n\twhile (1) {\n\t\twhile (c == \' \' || c == \'\\t\' || c == \'\\r\' || c == \'\\n\') {\n\t\t\tc = s.charAt(pos++);\n\t\t}\n\t\tif (c == \'\\0\' || pos >= s.length) {\n\t\t\tbreak;\n\t\t}\n\n\t\tif (c == \'(\') {\n\t\t\ttypes.push(XPathParser.LEFTPARENTHESIS);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\tif (c == \')\') {\n\t\t\ttypes.push(XPathParser.RIGHTPARENTHESIS);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\tif (c == \'[\') {\n\t\t\ttypes.push(XPathParser.LEFTBRACKET);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\tif (c == \']\') {\n\t\t\ttypes.push(XPathParser.RIGHTBRACKET);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\tif (c == \'@\') {\n\t\t\ttypes.push(XPathParser.AT);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\tif (c == \',\') {\n\t\t\ttypes.push(XPathParser.COMMA);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\tif (c == \'|\') {\n\t\t\ttypes.push(XPathParser.BAR);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\tif (c == \'+\') {\n\t\t\ttypes.push(XPathParser.PLUS);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\tif (c == \'-\') {\n\t\t\ttypes.push(XPathParser.MINUS);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\tif (c == \'=\') {\n\t\t\ttypes.push(XPathParser.EQUALS);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\tif (c == \'$\') {\n\t\t\ttypes.push(XPathParser.DOLLAR);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\t\t\n\t\tif (c == \'.\') {\n\t\t\tc = s.charAt(pos++);\n\t\t\tif (c == \'.\') {\n\t\t\t\ttypes.push(XPathParser.DOUBLEDOT);\n\t\t\t\tvalues.push("..");\n\t\t\t\tc = s.charAt(pos++);\n\t\t\t\tcontinue;\n\t\t\t}\n\t\t\tif (c >= \'0\' && c <= \'9\') {\n\t\t\t\tvar number = "." + c;\n\t\t\t\tc = s.charAt(pos++);\n\t\t\t\twhile (c >= \'0\' && c <= \'9\') {\n\t\t\t\t\tnumber += c;\n\t\t\t\t\tc = s.charAt(pos++);\n\t\t\t\t}\n\t\t\t\ttypes.push(XPathParser.NUMBER);\n\t\t\t\tvalues.push(number);\n\t\t\t\tcontinue;\n\t\t\t}\n\t\t\ttypes.push(XPathParser.DOT);\n\t\t\tvalues.push(\'.\');\n\t\t\tcontinue;\n\t\t}\n\n\t\tif (c == \'\\\'\' || c == \'"\') {\n\t\t\tvar delimiter = c;\n\t\t\tvar literal = "";\n\t\t\twhile ((c = s.charAt(pos++)) != delimiter) {\n\t\t\t\tliteral += c;\n\t\t\t}\n\t\t\ttypes.push(XPathParser.LITERAL);\n\t\t\tvalues.push(literal);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\n\t\tif (c >= \'0\' && c <= \'9\') {\n\t\t\tvar number = c;\n\t\t\tc = s.charAt(pos++);\n\t\t\twhile (c >= \'0\' && c <= \'9\') {\n\t\t\t\tnumber += c;\n\t\t\t\tc = s.charAt(pos++);\n\t\t\t}\n\t\t\tif (c == \'.\') {\n\t\t\t\tif (s.charAt(pos) >= \'0\' && s.charAt(pos) <= \'9\') {\n\t\t\t\t\tnumber += c;\n\t\t\t\t\tnumber += s.charAt(pos++);\n\t\t\t\t\tc = s.charAt(pos++);\n\t\t\t\t\twhile (c >= \'0\' && c <= \'9\') {\n\t\t\t\t\t\tnumber += c;\n\t\t\t\t\t\tc = s.charAt(pos++);\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t}\n\t\t\ttypes.push(XPathParser.NUMBER);\n\t\t\tvalues.push(number);\n\t\t\tcontinue;\n\t\t}\n\n\t\tif (c == \'*\') {\n\t\t\tif (types.length > 0) {\n\t\t\t\tvar last = types[types.length - 1];\n\t\t\t\tif (last != XPathParser.AT\n\t\t\t\t\t\t&& last != XPathParser.DOUBLECOLON\n\t\t\t\t\t\t&& last != XPathParser.LEFTPARENTHESIS\n\t\t\t\t\t\t&& last != XPathParser.LEFTBRACKET\n\t\t\t\t\t\t&& last != XPathParser.AND\n\t\t\t\t\t\t&& last != XPathParser.OR\n\t\t\t\t\t\t&& last != XPathParser.MOD\n\t\t\t\t\t\t&& last != XPathParser.DIV\n\t\t\t\t\t\t&& last != XPathParser.MULTIPLYOPERATOR\n\t\t\t\t\t\t&& last != XPathParser.SLASH\n\t\t\t\t\t\t&& last != XPathParser.DOUBLESLASH\n\t\t\t\t\t\t&& last != XPathParser.BAR\n\t\t\t\t\t\t&& last != XPathParser.PLUS\n\t\t\t\t\t\t&& last != XPathParser.MINUS\n\t\t\t\t\t\t&& last != XPathParser.EQUALS\n\t\t\t\t\t\t&& last != XPathParser.NOTEQUAL\n\t\t\t\t\t\t&& last != XPathParser.LESSTHAN\n\t\t\t\t\t\t&& last != XPathParser.LESSTHANOREQUAL\n\t\t\t\t\t\t&& last != XPathParser.GREATERTHAN\n\t\t\t\t\t\t&& last != XPathParser.GREATERTHANOREQUAL) {\n\t\t\t\t\ttypes.push(XPathParser.MULTIPLYOPERATOR);\n\t\t\t\t\tvalues.push(c);\n\t\t\t\t\tc = s.charAt(pos++);\n\t\t\t\t\tcontinue;\n\t\t\t\t}\n\t\t\t}\n\t\t\ttypes.push(XPathParser.ASTERISKNAMETEST);\n\t\t\tvalues.push(c);\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\n\t\tif (c == \':\') {\n\t\t\tif (s.charAt(pos) == \':\') {\n\t\t\t\ttypes.push(XPathParser.DOUBLECOLON);\n\t\t\t\tvalues.push("::");\n\t\t\t\tpos++;\n\t\t\t\tc = s.charAt(pos++);\n\t\t\t\tcontinue;\n\t\t\t}\n\t\t}\n\n\t\tif (c == \'/\') {\n\t\t\tc = s.charAt(pos++);\n\t\t\tif (c == \'/\') {\n\t\t\t\ttypes.push(XPathParser.DOUBLESLASH);\n\t\t\t\tvalues.push("//");\n\t\t\t\tc = s.charAt(pos++);\n\t\t\t\tcontinue;\n\t\t\t}\n\t\t\ttypes.push(XPathParser.SLASH);\n\t\t\tvalues.push(\'/\');\n\t\t\tcontinue;\n\t\t}\n\n\t\tif (c == \'!\') {\n\t\t\tif (s.charAt(pos) == \'=\') {\n\t\t\t\ttypes.push(XPathParser.NOTEQUAL);\n\t\t\t\tvalues.push("!=");\n\t\t\t\tpos++;\n\t\t\t\tc = s.charAt(pos++);\n\t\t\t\tcontinue;\n\t\t\t}\n\t\t}\n\n\t\tif (c == \'<\') {\n\t\t\tif (s.charAt(pos) == \'=\') {\n\t\t\t\ttypes.push(XPathParser.LESSTHANOREQUAL);\n\t\t\t\tvalues.push("<=");\n\t\t\t\tpos++;\n\t\t\t\tc = s.charAt(pos++);\n\t\t\t\tcontinue;\n\t\t\t}\n\t\t\ttypes.push(XPathParser.LESSTHAN);\n\t\t\tvalues.push(\'<\');\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\n\t\tif (c == \'>\') {\n\t\t\tif (s.charAt(pos) == \'=\') {\n\t\t\t\ttypes.push(XPathParser.GREATERTHANOREQUAL);\n\t\t\t\tvalues.push(">=");\n\t\t\t\tpos++;\n\t\t\t\tc = s.charAt(pos++);\n\t\t\t\tcontinue;\n\t\t\t}\n\t\t\ttypes.push(XPathParser.GREATERTHAN);\n\t\t\tvalues.push(\'>\');\n\t\t\tc = s.charAt(pos++);\n\t\t\tcontinue;\n\t\t}\n\n\t\tif (c == \'_\' || Utilities.isLetter(c.charCodeAt(0))) {\n\t\t\tvar name = c;\n\t\t\tc = s.charAt(pos++);\n\t\t\twhile (Utilities.isNCNameChar(c.charCodeAt(0))) {\n\t\t\t\tname += c;\n\t\t\t\tc = s.charAt(pos++);\n\t\t\t}\n\t\t\tif (types.length > 0) {\n\t\t\t\tvar last = types[types.length - 1];\n\t\t\t\tif (last != XPathParser.AT\n\t\t\t\t\t\t&& last != XPathParser.DOUBLECOLON\n\t\t\t\t\t\t&& last != XPathParser.LEFTPARENTHESIS\n\t\t\t\t\t\t&& last != XPathParser.LEFTBRACKET\n\t\t\t\t\t\t&& last != XPathParser.AND\n\t\t\t\t\t\t&& last != XPathParser.OR\n\t\t\t\t\t\t&& last != XPathParser.MOD\n\t\t\t\t\t\t&& last != XPathParser.DIV\n\t\t\t\t\t\t&& last != XPathParser.MULTIPLYOPERATOR\n\t\t\t\t\t\t&& last != XPathParser.SLASH\n\t\t\t\t\t\t&& last != XPathParser.DOUBLESLASH\n\t\t\t\t\t\t&& last != XPathParser.BAR\n\t\t\t\t\t\t&& last != XPathParser.PLUS\n\t\t\t\t\t\t&& last != XPathParser.MINUS\n\t\t\t\t\t\t&& last != XPathParser.EQUALS\n\t\t\t\t\t\t&& last != XPathParser.NOTEQUAL\n\t\t\t\t\t\t&& last != XPathParser.LESSTHAN\n\t\t\t\t\t\t&& last != XPathParser.LESSTHANOREQUAL\n\t\t\t\t\t\t&& last != XPathParser.GREATERTHAN\n\t\t\t\t\t\t&& last != XPathParser.GREATERTHANOREQUAL) {\n\t\t\t\t\tif (name == "and") {\n\t\t\t\t\t\ttypes.push(XPathParser.AND);\n\t\t\t\t\t\tvalues.push(name);\n\t\t\t\t\t\tcontinue;\n\t\t\t\t\t}\n\t\t\t\t\tif (name == "or") {\n\t\t\t\t\t\ttypes.push(XPathParser.OR);\n\t\t\t\t\t\tvalues.push(name);\n\t\t\t\t\t\tcontinue;\n\t\t\t\t\t}\n\t\t\t\t\tif (name == "mod") {\n\t\t\t\t\t\ttypes.push(XPathParser.MOD);\n\t\t\t\t\t\tvalues.push(name);\n\t\t\t\t\t\tcontinue;\n\t\t\t\t\t}\n\t\t\t\t\tif (name == "div") {\n\t\t\t\t\t\ttypes.push(XPathParser.DIV);\n\t\t\t\t\t\tvalues.push(name);\n\t\t\t\t\t\tcontinue;\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t}\n\t\t\tif (c == \':\') {\n\t\t\t\tif (s.charAt(pos) == \'*\') {\n\t\t\t\t\ttypes.push(XPathParser.NCNAMECOLONASTERISK);\n\t\t\t\t\tvalues.push(name + ":*");\n\t\t\t\t\tpos++;\n\t\t\t\t\tc = s.charAt(pos++);\n\t\t\t\t\tcontinue;\n\t\t\t\t}\n\t\t\t\tif (s.charAt(pos) == \'_\' || Utilities.isLetter(s.charCodeAt(pos))) {\n\t\t\t\t\tname += \':\';\n\t\t\t\t\tc = s.charAt(pos++);\n\t\t\t\t\twhile (Utilities.isNCNameChar(c.charCodeAt(0))) {\n\t\t\t\t\t\tname += c;\n\t\t\t\t\t\tc = s.charAt(pos++);\n\t\t\t\t\t}\n\t\t\t\t\tif (c == \'(\') {\n\t\t\t\t\t\ttypes.push(XPathParser.FUNCTIONNAME);\n\t\t\t\t\t\tvalues.push(name);\n\t\t\t\t\t\tcontinue;\n\t\t\t\t\t}\n\t\t\t\t\ttypes.push(XPathParser.QNAME);\n\t\t\t\t\tvalues.push(name);\n\t\t\t\t\tcontinue;\n\t\t\t\t}\n\t\t\t\tif (s.charAt(pos) == \':\') {\n\t\t\t\t\ttypes.push(XPathParser.AXISNAME);\n\t\t\t\t\tvalues.push(name);\n\t\t\t\t\tcontinue;\n\t\t\t\t}\n\t\t\t}\n\t\t\tif (c == \'(\') {\n\t\t\t\tif (name == "comment" || name == "text" || name == "node") {\n\t\t\t\t\ttypes.push(XPathParser.NODETYPE);\n\t\t\t\t\tvalues.push(name);\n\t\t\t\t\tcontinue;\n\t\t\t\t}\n\t\t\t\tif (name == "processing-instruction") {\n\t\t\t\t\tif (s.charAt(pos) == \')\') {' , builder.string += '\n\t\t\t\t\t\ttypes.push(XPathParser.NODETYPE);\n\t\t\t\t\t} else {\n\t\t\t\t\t\ttypes.push(XPathParser.PROCESSINGINSTRUCTIONWITHLITERAL);\n\t\t\t\t\t}\n\t\t\t\t\tvalues.push(name);\n\t\t\t\t\tcontinue;\n\t\t\t\t}\n\t\t\t\ttypes.push(XPathParser.FUNCTIONNAME);\n\t\t\t\tvalues.push(name);\n\t\t\t\tcontinue;\n\t\t\t}\n\t\t\ttypes.push(XPathParser.QNAME);\n\t\t\tvalues.push(name);\n\t\t\tcontinue;\n\t\t}\n\n\t\tthrow new Error("Unexpected character " + c);\n\t}\n\ttypes.push(1);\n\tvalues.push("[EOF]");\n\treturn [types, values];\n};\n\nXPathParser.SHIFT = \'s\';\nXPathParser.REDUCE = \'r\';\nXPathParser.ACCEPT = \'a\';\n\nXPathParser.prototype.parse = function(s) {\n\tvar types;\n\tvar values;\n\tvar res = this.tokenize(s);\n\tif (res == undefined) {\n\t\treturn undefined;\n\t}\n\ttypes = res[0];\n\tvalues = res[1];\n\tvar tokenPos = 0;\n\tvar state = [];\n\tvar tokenType = [];\n\tvar tokenValue = [];\n\tvar s;\n\tvar a;\n\tvar t;\n\n\tstate.push(0);\n\ttokenType.push(1);\n\ttokenValue.push("_S");\n\n\ta = types[tokenPos];\n\tt = values[tokenPos++];\n\twhile (1) {\n\t\ts = state[state.length - 1];\n\t\tswitch (XPathParser.actionTable[s].charAt(a - 1)) {\n\t\t\tcase XPathParser.SHIFT:\n\t\t\t\ttokenType.push(-a);\n\t\t\t\ttokenValue.push(t);\n\t\t\t\tstate.push(XPathParser.actionTableNumber[s].charCodeAt(a - 1) - 32);\n\t\t\t\ta = types[tokenPos];\n\t\t\t\tt = values[tokenPos++];\n\t\t\t\tbreak;\n\t\t\tcase XPathParser.REDUCE:\n\t\t\t\tvar num = XPathParser.productions[XPathParser.actionTableNumber[s].charCodeAt(a - 1) - 32][1];\n\t\t\t\tvar rhs = [];\n\t\t\t\tfor (var i = 0; i < num; i++) {\n\t\t\t\t\ttokenType.pop();\n\t\t\t\t\trhs.unshift(tokenValue.pop());\n\t\t\t\t\tstate.pop();\n\t\t\t\t}\n\t\t\t\tvar s_ = state[state.length - 1];\n\t\t\t\ttokenType.push(XPathParser.productions[XPathParser.actionTableNumber[s].charCodeAt(a - 1) - 32][0]);\n\t\t\t\tif (this.reduceActions[XPathParser.actionTableNumber[s].charCodeAt(a - 1) - 32] == undefined) {\n\t\t\t\t\ttokenValue.push(rhs[0]);\n\t\t\t\t} else {\n\t\t\t\t\ttokenValue.push(this.reduceActions[XPathParser.actionTableNumber[s].charCodeAt(a - 1) - 32](rhs));\n\t\t\t\t}\n\t\t\t\tstate.push(XPathParser.gotoTable[s_].charCodeAt(XPathParser.productions[XPathParser.actionTableNumber[s].charCodeAt(a - 1) - 32][0] - 2) - 33);\n\t\t\t\tbreak;\n\t\t\tcase XPathParser.ACCEPT:\n\t\t\t\treturn new XPath(tokenValue.pop());\n\t\t\tdefault:\n\t\t\t\tthrow new Error("XPath parse error");\n\t\t}\n\t}\n};\n\n// XPath /////////////////////////////////////////////////////////////////////\n\nXPath.prototype = new Object();\nXPath.prototype.constructor = XPath;\nXPath.superclass = Object.prototype;\n\nfunction XPath(e) {\n\tthis.expression = e;\n}\n\nXPath.prototype.toString = function() {\n\treturn this.expression.toString();\n};\n\nXPath.prototype.evaluate = function(c) {\n\tc.contextNode = c.expressionContextNode;\n\tc.contextSize = 1;\n\tc.contextPosition = 1;\n\tc.caseInsensitive = false;\n\tif (c.contextNode != null) {\n\t\tvar doc = c.contextNode;\n\t\tif (doc.nodeType != 9 /*Node.DOCUMENT_NODE*/) {\n\t\t\tdoc = doc.ownerDocument;\n\t\t}\n\t\ttry {\n\t\t\tc.caseInsensitive = doc.implementation.hasFeature("HTML", "2.0");\n\t\t} catch (e) {\n\t\t\tc.caseInsensitive = true;\n\t\t}\n\t}\n\treturn this.expression.evaluate(c);\n};\n\nXPath.XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";\nXPath.XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";\n\n// Expression ////////////////////////////////////////////////////////////////\n\nExpression.prototype = new Object();\nExpression.prototype.constructor = Expression;\nExpression.superclass = Object.prototype;\n\nfunction Expression() {\n}\n\nExpression.prototype.init = function() {\n};\n\nExpression.prototype.toString = function() {\n\treturn "<Expression>";\n};\n\nExpression.prototype.evaluate = function(c) {\n\tthrow new Error("Could not evaluate expression.");\n};\n\n// UnaryOperation ////////////////////////////////////////////////////////////\n\nUnaryOperation.prototype = new Expression();\nUnaryOperation.prototype.constructor = UnaryOperation;\nUnaryOperation.superclass = Expression.prototype;\n\nfunction UnaryOperation(rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(rhs);\n\t}\n}\n\nUnaryOperation.prototype.init = function(rhs) {\n\tthis.rhs = rhs;\n};\n\n// UnaryMinusOperation ///////////////////////////////////////////////////////\n\nUnaryMinusOperation.prototype = new UnaryOperation();\nUnaryMinusOperation.prototype.constructor = UnaryMinusOperation;\nUnaryMinusOperation.superclass = UnaryOperation.prototype;\n\nfunction UnaryMinusOperation(rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(rhs);\n\t}\n}\n\nUnaryMinusOperation.prototype.init = function(rhs) {\n\tUnaryMinusOperation.superclass.init.call(this, rhs);\n};\n\nUnaryMinusOperation.prototype.evaluate = function(c) {\n\treturn this.rhs.evaluate(c).number().negate();\n};\n\nUnaryMinusOperation.prototype.toString = function() {\n\treturn "-" + this.rhs.toString();\n};\n\n// BinaryOperation ///////////////////////////////////////////////////////////\n\nBinaryOperation.prototype = new Expression();\nBinaryOperation.prototype.constructor = BinaryOperation;\nBinaryOperation.superclass = Expression.prototype;\n\nfunction BinaryOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nBinaryOperation.prototype.init = function(lhs, rhs) {\n\tthis.lhs = lhs;\n\tthis.rhs = rhs;\n};\n\n// OrOperation ///////////////////////////////////////////////////////////////\n\nOrOperation.prototype = new BinaryOperation();\nOrOperation.prototype.constructor = OrOperation;\nOrOperation.superclass = BinaryOperation.prototype;\n\nfunction OrOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nOrOperation.prototype.init = function(lhs, rhs) {\n\tOrOperation.superclass.init.call(this, lhs, rhs);\n};\n\nOrOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " or " + this.rhs.toString() + ")";\n};\n\nOrOperation.prototype.evaluate = function(c) {\n\tvar b = this.lhs.evaluate(c).bool();\n\tif (b.booleanValue()) {\n\t\treturn b;\n\t}\n\treturn this.rhs.evaluate(c).bool();\n};\n\n// AndOperation //////////////////////////////////////////////////////////////\n\nAndOperation.prototype = new BinaryOperation();\nAndOperation.prototype.constructor = AndOperation;\nAndOperation.superclass = BinaryOperation.prototype;\n\nfunction AndOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nAndOperation.prototype.init = function(lhs, rhs) {\n\tAndOperation.superclass.init.call(this, lhs, rhs);\n};\n\nAndOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " and " + this.rhs.toString() + ")";\n};\n\nAndOperation.prototype.evaluate = function(c) {\n\tvar b = this.lhs.evaluate(c).bool();\n\tif (!b.booleanValue()) {\n\t\treturn b;\n\t}\n\treturn this.rhs.evaluate(c).bool();\n};\n\n// EqualsOperation ///////////////////////////////////////////////////////////\n\nEqualsOperation.prototype = new BinaryOperation();\nEqualsOperation.prototype.constructor = EqualsOperation;\nEqualsOperation.superclass = BinaryOperation.prototype;\n\nfunction EqualsOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nEqualsOperation.prototype.init = function(lhs, rhs) {\n\tEqualsOperation.superclass.init.call(this, lhs, rhs);\n};\n\nEqualsOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " = " + this.rhs.toString() + ")";\n};\n\nEqualsOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).equals(this.rhs.evaluate(c));\n};\n\n// NotEqualOperation /////////////////////////////////////////////////////////\n\nNotEqualOperation.prototype = new BinaryOperation();\nNotEqualOperation.prototype.constructor = NotEqualOperation;\nNotEqualOperation.superclass = BinaryOperation.prototype;\n\nfunction NotEqualOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nNotEqualOperation.prototype.init = function(lhs, rhs) {\n\tNotEqualOperation.superclass.init.call(this, lhs, rhs);\n};\n\nNotEqualOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " != " + this.rhs.toString() + ")";\n};\n\nNotEqualOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).notequal(this.rhs.evaluate(c));\n};\n\n// LessThanOperation /////////////////////////////////////////////////////////\n\nLessThanOperation.prototype = new BinaryOperation();\nLessThanOperation.prototype.constructor = LessThanOperation;\nLessThanOperation.superclass = BinaryOperation.prototype;\n\nfunction LessThanOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nLessThanOperation.prototype.init = function(lhs, rhs) {\n\tLessThanOperation.superclass.init.call(this, lhs, rhs);\n};\n\nLessThanOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).lessthan(this.rhs.evaluate(c));\n};\n\nLessThanOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " < " + this.rhs.toString() + ")";\n};\n\n// GreaterThanOperation //////////////////////////////////////////////////////\n\nGreaterThanOperation.prototype = new BinaryOperation();\nGreaterThanOperation.prototype.constructor = GreaterThanOperation;\nGreaterThanOperation.superclass = BinaryOperation.prototype;\n\nfunction GreaterThanOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nGreaterThanOperation.prototype.init = function(lhs, rhs) {\n\tGreaterThanOperation.superclass.init.call(this, lhs, rhs);\n};\n\nGreaterThanOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).greaterthan(this.rhs.evaluate(c));\n};\n\nGreaterThanOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " > " + this.rhs.toString() + ")";\n};\n\n// LessThanOrEqualOperation //////////////////////////////////////////////////\n\nLessThanOrEqualOperation.prototype = new BinaryOperation();\nLessThanOrEqualOperation.prototype.constructor = LessThanOrEqualOperation;\nLessThanOrEqualOperation.superclass = BinaryOperation.prototype;\n\nfunction LessThanOrEqualOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nLessThanOrEqualOperation.prototype.init = function(lhs, rhs) {\n\tLessThanOrEqualOperation.superclass.init.call(this, lhs, rhs);\n};\n\nLessThanOrEqualOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).lessthanorequal(this.rhs.evaluate(c));\n};\n\nLessThanOrEqualOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " <= " + this.rhs.toString() + ")";\n};\n\n// GreaterThanOrEqualOperation ///////////////////////////////////////////////\n\nGreaterThanOrEqualOperation.prototype = new BinaryOperation();\nGreaterThanOrEqualOperation.prototype.constructor = GreaterThanOrEqualOperation;\nGreaterThanOrEqualOperation.superclass = BinaryOperation.prototype;\n\nfunction GreaterThanOrEqualOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nGreaterThanOrEqualOperation.prototype.init = function(lhs, rhs) {\n\tGreaterThanOrEqualOperation.superclass.init.call(this, lhs, rhs);\n};\n\nGreaterThanOrEqualOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).greaterthanorequal(this.rhs.evaluate(c));\n};\n\nGreaterThanOrEqualOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " >= " + this.rhs.toString() + ")";\n};\n\n// PlusOperation /////////////////////////////////////////////////////////////\n\nPlusOperation.prototype = new BinaryOperation();\nPlusOperation.prototype.constructor = PlusOperation;\nPlusOperation.superclass = BinaryOperation.prototype;\n\nfunction PlusOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nPlusOperation.prototype.init = function(lhs, rhs) {\n\tPlusOperation.superclass.init.call(this, lhs, rhs);\n};\n\nPlusOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).number().plus(this.rhs.evaluate(c).number());\n};\n\nPlusOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " + " + this.rhs.toString() + ")";\n};\n\n// MinusOperation ////////////////////////////////////////////////////////////\n\nMinusOperation.prototype = new BinaryOperation();\nMinusOperation.prototype.constructor = MinusOperation;\nMinusOperation.superclass = BinaryOperation.prototype;\n\nfunction MinusOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nMinusOperation.prototype.init = function(lhs, rhs) {\n\tMinusOperation.superclass.init.call(this, lhs, rhs);\n};\n\nMinusOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).number().minus(this.rhs.evaluate(c).number());\n};\n\nMinusOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " - " + this.rhs.toString() + ")";\n};\n\n// MultiplyOperation /////////////////////////////////////////////////////////\n\nMultiplyOperation.prototype = new BinaryOperation();\nMultiplyOperation.prototype.constructor = MultiplyOperation;\nMultiplyOperation.superclass = BinaryOperation.prototype;\n\nfunction MultiplyOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nMultiplyOperation.prototype.init = function(lhs, rhs) {\n\tMultiplyOperation.superclass.init.call(this, lhs, rhs);\n};\n\nMultiplyOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).number().multiply(this.rhs.evaluate(c).number());\n};\n\nMultiplyOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " * " + this.rhs.toString() + ")";\n};\n\n// DivOperation //////////////////////////////////////////////////////////////\n\nDivOperation.prototype = new BinaryOperation();\nDivOperation.prototype.constructor = DivOperation;\nDivOperation.superclass = BinaryOperation.prototype;\n\nfunction DivOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nDivOperation.prototype.init = function(lhs, rhs) {\n\tDivOperation.superclass.init.call(this, lhs, rhs);\n};\n\nDivOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).number().div(this.rhs.evaluate(c).number());\n};\n\nDivOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " div " + this.rhs.toString() + ")";\n};\n\n// ModOperation //////////////////////////////////////////////////////////////\n\nModOperation.prototype = new BinaryOperation();\nModOperation.prototype.constructor = ModOperation;\nModOperation.superclass = BinaryOperation.prototype;\n\nfunction ModOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nModOperation.prototype.init = function(lhs, rhs) {\n\tModOperation.superclass.init.call(this, lhs, rhs);\n};\n\nModOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).number().mod(this.rhs.evaluate(c).number());\n};\n\nModOperation.prototype.toString = function() {\n\treturn "(" + this.lhs.toString() + " mod " + this.rhs.toString() + ")";\n};\n\n// BarOperation //////////////////////////////////////////////////////////////\n\nBarOperation.prototype = new BinaryOperation();\nBarOperation.prototype.constructor = BarOperation;\nBarOperation.superclass = BinaryOperation.prototype;\n\nfunction BarOperation(lhs, rhs) {\n\tif (arguments.length > 0) {\n\t\tthis.init(lhs, rhs);\n\t}\n}\n\nBarOperation.prototype.init = function(lhs, rhs) {\n\tBarOperation.superclass.init.call(this, lhs, rhs);\n};\n\nBarOperation.prototype.evaluate = function(c) {\n\treturn this.lhs.evaluate(c).nodeset().union(this.rhs.evaluate(c).nodeset());\n};\n\nBarOperation.prototype.toString = function() {\n\treturn this.lhs.toString() + " | " + this.rhs.toString();\n};\n\n// PathExpr //////////////////////////////////////////////////////////////////\n\nPathExpr.prototype = new Expression();\nPathExpr.prototype.constructor = PathExpr;\nPathExpr.superclass = Expression.prototype;\n\nfunction PathExpr(filter, filterPreds, locpath) {\n\tif (arguments.length > 0) {\n\t\tthis.init(filter, filterPreds, locpath);\n\t}\n}\n\nPathExpr.prototype.init = function(filter, filterPreds, locpath) {\n\tPathExpr.superclass.init.call(this);\n\tthis.filter = filter;\n\tthis.filterPredicates = filterPreds;\n\tthis.locationPath = locpath;\n};\n\nPathExpr.prototype.evaluate = function(c) {\n\tvar nodes;\n\tvar xpc = new XPathContext();\n\txpc.variableResolver = c.variableResolver;\n\txpc.functionResolver = c.functionResolver;\n\txpc.namespaceResolver = c.namespaceResolver;\n\txpc.expressionContextNode = c.expressionContextNode;\n\txpc.virtualRoot = c.virtualRoot;\n\txpc.caseInsensitive = c.caseInsensitive;\n\tif (this.filter == null) {\n\t\tnodes = [ c.contextNode ];\n\t} else {\n\t\tvar ns = this.filter.evaluate(c);\n\t\tif (!Utilities.instance_of(ns, XNodeSet)) {\n\t\t\tif (this.filterPredicates != null && this.filterPredicates.length > 0 || this.locationPath != null) {\n\t\t\t\tthrow new Error("Path expression filter must evaluate to a nodset if predicates or location path are used");\n\t\t\t}\n\t\t\treturn ns;\n\t\t}\n\t\tnodes = ns.toArray();\n\t\tif (this.filterPredicates != n' , builder.string += 'ull) {\n\t\t\t// apply each of the predicates in turn\n\t\t\tfor (var j = 0; j < this.filterPredicates.length; j++) {\n\t\t\t\tvar pred = this.filterPredicates[j];\n\t\t\t\tvar newNodes = [];\n\t\t\t\txpc.contextSize = nodes.length;\n\t\t\t\tfor (xpc.contextPosition = 1; xpc.contextPosition <= xpc.contextSize; xpc.contextPosition++) {\n\t\t\t\t\txpc.contextNode = nodes[xpc.contextPosition - 1];\n\t\t\t\t\tif (this.predicateMatches(pred, xpc)) {\n\t\t\t\t\t\tnewNodes.push(xpc.contextNode);\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t\tnodes = newNodes;\n\t\t\t}\n\t\t}\n\t}\n\tif (this.locationPath != null) {\n\t\tif (this.locationPath.absolute) {\n\t\t\tif (nodes[0].nodeType != 9 /*Node.DOCUMENT_NODE*/) {\n\t\t\t\tif (xpc.virtualRoot != null) {\n\t\t\t\t\tnodes = [ xpc.virtualRoot ];\n\t\t\t\t} else {\n\t\t\t\t\tif (nodes[0].ownerDocument == null) {\n\t\t\t\t\t\t// IE 5.5 doesn\'t have ownerDocument?\n\t\t\t\t\t\tvar n = nodes[0];\n\t\t\t\t\t\twhile (n.parentNode != null) {\n\t\t\t\t\t\t\tn = n.parentNode;\n\t\t\t\t\t\t}\n\t\t\t\t\t\tnodes = [ n ];\n\t\t\t\t\t} else {\n\t\t\t\t\t\tnodes = [ nodes[0].ownerDocument ];\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t} else {\n\t\t\t\tnodes = [ nodes[0] ];\n\t\t\t}\n\t\t}\n\t\tfor (var i = 0; i < this.locationPath.steps.length; i++) {\n\t\t\tvar step = this.locationPath.steps[i];\n\t\t\tvar newNodes = [];\n\t\t\tfor (var j = 0; j < nodes.length; j++) {\n\t\t\t\txpc.contextNode = nodes[j];\n\t\t\t\tswitch (step.axis) {\n\t\t\t\t\tcase Step.ANCESTOR:\n\t\t\t\t\t\t// look at all the ancestor nodes\n\t\t\t\t\t\tif (xpc.contextNode === xpc.virtualRoot) {\n\t\t\t\t\t\t\tbreak;\n\t\t\t\t\t\t}\n\t\t\t\t\t\tvar m;\n\t\t\t\t\t\tif (xpc.contextNode.nodeType == 2 /*Node.ATTRIBUTE_NODE*/) {\n\t\t\t\t\t\t\tm = this.getOwnerElement(xpc.contextNode);\n\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\tm = xpc.contextNode.parentNode;\n\t\t\t\t\t\t}\n\t\t\t\t\t\twhile (m != null) {\n\t\t\t\t\t\t\tif (step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\t\tnewNodes.push(m);\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\tif (m === xpc.virtualRoot) {\n\t\t\t\t\t\t\t\tbreak;\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\tm = m.parentNode;\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.ANCESTORORSELF:\n\t\t\t\t\t\t// look at all the ancestor nodes and the current node\n\t\t\t\t\t\tfor (var m = xpc.contextNode; m != null; m = m.nodeType == 2 /*Node.ATTRIBUTE_NODE*/ ? this.getOwnerElement(m) : m.parentNode) {\n\t\t\t\t\t\t\tif (step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\t\tnewNodes.push(m);\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\tif (m === xpc.virtualRoot) {\n\t\t\t\t\t\t\t\tbreak;\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.ATTRIBUTE:\n\t\t\t\t\t\t// look at the attributes\n\t\t\t\t\t\tvar nnm = xpc.contextNode.attributes;\n\t\t\t\t\t\tif (nnm != null) {\n\t\t\t\t\t\t\tfor (var k = 0; k < nnm.length; k++) {\n\t\t\t\t\t\t\t\tvar m = nnm.item(k);\n\t\t\t\t\t\t\t\tif (step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\t\t\tnewNodes.push(m);\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.CHILD:\n\t\t\t\t\t\t// look at all child elements\n\t\t\t\t\t\tfor (var m = xpc.contextNode.firstChild; m != null; m = m.nextSibling) {\n\t\t\t\t\t\t\tif (step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\t\tnewNodes.push(m);\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.DESCENDANT:\n\t\t\t\t\t\t// look at all descendant nodes\n\t\t\t\t\t\tvar st = [ xpc.contextNode.firstChild ];\n\t\t\t\t\t\twhile (st.length > 0) {\n\t\t\t\t\t\t\tfor (var m = st.pop(); m != null; ) {\n\t\t\t\t\t\t\t\tif (step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\t\t\tnewNodes.push(m);\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\tif (m.firstChild != null) {\n\t\t\t\t\t\t\t\t\tst.push(m.nextSibling);\n\t\t\t\t\t\t\t\t\tm = m.firstChild;\n\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\tm = m.nextSibling;\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.DESCENDANTORSELF:\n\t\t\t\t\t\t// look at self\n\t\t\t\t\t\tif (step.nodeTest.matches(xpc.contextNode, xpc)) {\n\t\t\t\t\t\t\tnewNodes.push(xpc.contextNode);\n\t\t\t\t\t\t}\n\t\t\t\t\t\t// look at all descendant nodes\n\t\t\t\t\t\tvar st = [ xpc.contextNode.firstChild ];\n\t\t\t\t\t\twhile (st.length > 0) {\n\t\t\t\t\t\t\tfor (var m = st.pop(); m != null; ) {\n\t\t\t\t\t\t\t\tif (step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\t\t\tnewNodes.push(m);\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\tif (m.firstChild != null) {\n\t\t\t\t\t\t\t\t\tst.push(m.nextSibling);\n\t\t\t\t\t\t\t\t\tm = m.firstChild;\n\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\tm = m.nextSibling;\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.FOLLOWING:\n\t\t\t\t\t\tif (xpc.contextNode === xpc.virtualRoot) {\n\t\t\t\t\t\t\tbreak;\n\t\t\t\t\t\t}\n\t\t\t\t\t\tvar st = [];\n\t\t\t\t\t\tif (xpc.contextNode.firstChild != null) {\n\t\t\t\t\t\t\tst.unshift(xpc.contextNode.firstChild);\n\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\tst.unshift(xpc.contextNode.nextSibling);\n\t\t\t\t\t\t}\n\t\t\t\t\t\tfor (var m = xpc.contextNode.parentNode; m != null && m.nodeType != 9 /*Node.DOCUMENT_NODE*/ && m !== xpc.virtualRoot; m = m.parentNode) {\n\t\t\t\t\t\t\tst.unshift(m.nextSibling);\n\t\t\t\t\t\t}\n\t\t\t\t\t\tdo {\n\t\t\t\t\t\t\tfor (var m = st.pop(); m != null; ) {\n\t\t\t\t\t\t\t\tif (step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\t\t\tnewNodes.push(m);\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\tif (m.firstChild != null) {\n\t\t\t\t\t\t\t\t\tst.push(m.nextSibling);\n\t\t\t\t\t\t\t\t\tm = m.firstChild;\n\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\tm = m.nextSibling;\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t} while (st.length > 0);\n\t\t\t\t\t\tbreak;\n\t\t\t\t\t\t\n\t\t\t\t\tcase Step.FOLLOWINGSIBLING:\n\t\t\t\t\t\tif (xpc.contextNode === xpc.virtualRoot) {\n\t\t\t\t\t\t\tbreak;\n\t\t\t\t\t\t}\n\t\t\t\t\t\tfor (var m = xpc.contextNode.nextSibling; m != null; m = m.nextSibling) {\n\t\t\t\t\t\t\tif (step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\t\tnewNodes.push(m);\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.NAMESPACE:\n\t\t\t\t\t\tvar n = {};\n\t\t\t\t\t\tif (xpc.contextNode.nodeType == 1 /*Node.ELEMENT_NODE*/) {\n\t\t\t\t\t\t\tn["xml"] = XPath.XML_NAMESPACE_URI;\n\t\t\t\t\t\t\tn["xmlns"] = XPath.XMLNS_NAMESPACE_URI;\n\t\t\t\t\t\t\tfor (var m = xpc.contextNode; m != null && m.nodeType == 1 /*Node.ELEMENT_NODE*/; m = m.parentNode) {\n\t\t\t\t\t\t\t\tfor (var k = 0; k < m.attributes.length; k++) {\n\t\t\t\t\t\t\t\t\tvar attr = m.attributes.item(k);\n\t\t\t\t\t\t\t\t\tvar nm = String(attr.name);\n\t\t\t\t\t\t\t\t\tif (nm == "xmlns") {\n\t\t\t\t\t\t\t\t\t\tif (n[""] == undefined) {\n\t\t\t\t\t\t\t\t\t\t\tn[""] = attr.value;\n\t\t\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\t\t} else if (nm.length > 6 && nm.substring(0, 6) == "xmlns:") {\n\t\t\t\t\t\t\t\t\t\tvar pre = nm.substring(6, nm.length);\n\t\t\t\t\t\t\t\t\t\tif (n[pre] == undefined) {\n\t\t\t\t\t\t\t\t\t\t\tn[pre] = attr.value;\n\t\t\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\tfor (var pre in n) {\n\t\t\t\t\t\t\t\tvar nsn = new NamespaceNode(pre, n[pre], xpc.contextNode);\n\t\t\t\t\t\t\t\tif (step.nodeTest.matches(nsn, xpc)) {\n\t\t\t\t\t\t\t\t\tnewNodes.push(nsn);\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.PARENT:\n\t\t\t\t\t\tm = null;\n\t\t\t\t\t\tif (xpc.contextNode !== xpc.virtualRoot) {\n\t\t\t\t\t\t\tif (xpc.contextNode.nodeType == 2 /*Node.ATTRIBUTE_NODE*/) {\n\t\t\t\t\t\t\t\tm = this.getOwnerElement(xpc.contextNode);\n\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\tm = xpc.contextNode.parentNode;\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t\tif (m != null && step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\tnewNodes.push(m);\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.PRECEDING:\n\t\t\t\t\t\tvar st;\n\t\t\t\t\t\tif (xpc.virtualRoot != null) {\n\t\t\t\t\t\t\tst = [ xpc.virtualRoot ];\n\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\tst = xpc.contextNode.nodeType == 9 /*Node.DOCUMENT_NODE*/\n\t\t\t\t\t\t\t\t? [ xpc.contextNode ]\n\t\t\t\t\t\t\t\t: [ xpc.contextNode.ownerDocument ];\n\t\t\t\t\t\t}\n\t\t\t\t\t\touter: while (st.length > 0) {\n\t\t\t\t\t\t\tfor (var m = st.pop(); m != null; ) {\n\t\t\t\t\t\t\t\tif (m == xpc.contextNode) {\n\t\t\t\t\t\t\t\t\tbreak outer;\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\tif (step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\t\t\tnewNodes.unshift(m);\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t\tif (m.firstChild != null) {\n\t\t\t\t\t\t\t\t\tst.push(m.nextSibling);\n\t\t\t\t\t\t\t\t\tm = m.firstChild;\n\t\t\t\t\t\t\t\t} else {\n\t\t\t\t\t\t\t\t\tm = m.nextSibling;\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.PRECEDINGSIBLING:\n\t\t\t\t\t\tif (xpc.contextNode === xpc.virtualRoot) {\n\t\t\t\t\t\t\tbreak;\n\t\t\t\t\t\t}\n\t\t\t\t\t\tfor (var m = xpc.contextNode.previousSibling; m != null; m = m.previousSibling) {\n\t\t\t\t\t\t\tif (step.nodeTest.matches(m, xpc)) {\n\t\t\t\t\t\t\t\tnewNodes.push(m);\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tcase Step.SELF:\n\t\t\t\t\t\tif (step.nodeTest.matches(xpc.contextNode, xpc)) {\n\t\t\t\t\t\t\tnewNodes.push(xpc.contextNode);\n\t\t\t\t\t\t}\n\t\t\t\t\t\tbreak;\n\n\t\t\t\t\tdefault:\n\t\t\t\t}\n\t\t\t}\n\t\t\tnodes = newNodes;\n\t\t\t// apply each of the predicates in turn\n\t\t\tfor (var j = 0; j < step.predicates.length; j++) {\n\t\t\t\tvar pred = step.predicates[j];\n\t\t\t\tvar newNodes = [];\n\t\t\t\txpc.contextSize = nodes.length;\n\t\t\t\tfor (xpc.contextPosition = 1; xpc.contextPosition <= xpc.contextSize; xpc.contextPosition++) {\n\t\t\t\t\txpc.contextNode = nodes[xpc.contextPosition - 1];\n\t\t\t\t\tif (this.predicateMatches(pred, xpc)) {\n\t\t\t\t\t\tnewNodes.push(xpc.contextNode);\n\t\t\t\t\t} else {\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t\tnodes = newNodes;\n\t\t\t}\n\t\t}\n\t}\n\tvar ns = new XNodeSet();\n\tns.addArray(nodes);\n\treturn ns;\n};\n\nPathExpr.prototype.predicateMatches = function(pred, c) {\n\tvar res = pred.evaluate(c);\n\tif (Utilities.instance_of(res, XNumber)) {\n\t\treturn c.contextPosition == res.numberValue();\n\t}\n\treturn res.booleanValue();\n};\n\nPathExpr.prototype.toString = function() {\n\tif (this.filter != undefined) {\n\t\tvar s = this.filter.toString();\n\t\tif (Utilities.instance_of(this.filter, XString)) {\n\t\t\ts = "\'" + s + "\'";\n\t\t}\n\t\tif (this.filterPredicates != undefined) {\n\t\t\tfor (var i = 0; i < this.filterPredicates.length; i++) {\n\t\t\t\ts = s + "[" + this.filterPredicates[i].toString() + "]";\n\t\t\t}\n\t\t}\n\t\tif (this.locationPath != undefined) {\n\t\t\tif (!this.locationPath.absolute) {\n\t\t\t\ts += "/";\n\t\t\t}\n\t\t\ts += this.locationPath.toString();\n\t\t}\n\t\treturn s;\n\t}\n\treturn this.locationPath.toString();\n};\n\nPathExpr.prototype.getOwnerElement = function(n) {\n\t// DOM 2 has ownerElement\n\tif (n.ownerElement) {\n\t\treturn n.ownerElement;\n\t}\n\t// DOM 1 Internet Explorer can use selectSingleNode (ironically)\n\ttry {\n\t\tif (n.selectSingleNode) {\n\t\t\treturn n.selectSingleNode("..");\n\t\t}\n\t} catch (e) {\n\t}\n\t// Other DOM 1 implementations must use this egregious search\n\tvar doc = n.nodeType == 9 /*Node.DOCUMENT_NODE*/\n\t\t\t? n\n\t\t\t: n.ownerDocument;\n\tvar elts = doc.getElementsByTagName("*");\n\tfor (var i = 0; i < elts.length; i++) {\n\t\tvar elt = elts.item(i);\n\t\tvar nnm = elt.attributes;\n\t\tfor (var j = 0; j < nnm.length; j++) {\n\t\t\tvar an = nnm.item(j);\n\t\t\tif (an === n) {\n\t\t\t\treturn elt;\n\t\t\t}\n\t\t}\n\t}\n\treturn null;\n};\n\n// LocationPath //////////////////////////////////////////////////////////////\n\nLocationPath.prototype = new Object();\nLocationPath.prototype.constructor = LocationPath;\nLocationPath.superclass = Object.prototype;\n\nfunction LocationPath(abs, steps) {\n\tif (arguments.length > 0) {\n\t\tthis.init(abs, steps);\n\t}\n}\n\nLocationPath.prototype.init = function(abs, steps) {\n\tthis.absolute = abs;\n\tthis.steps = steps;\n};\n\nLocationPath.prototype.toString = function() {\n\tvar s;\n\tif (this.absolute) {\n\t\ts = "/";\n\t} else {\n\t\ts = "";\n\t}\n\tfor (var i = 0; i < this.steps.length; i++) {\n\t\tif (i != 0) {\n\t\t\ts += "/";\n\t\t}\n\t\ts += this.steps[i].toString();\n\t}\n\treturn s;\n};\n\n// Step //////////////////////////////////////////////////////////////////////\n\nStep.prototype = new Object();\nStep.prototype.constructor = Step;\nStep.superclass = Object.prototype;\n\nfunction Step(axis, nodetest, preds) {\n\tif (arguments.length > 0) {\n\t\tthis.init(axis, nodetest, preds);\n\t}\n}\n\nStep.prototype.init = function(axis, nodetest, preds) {\n\tthis.axis = axis;\n\tthis.nodeTest = nodetest;\n\tthis.predicates = preds;\n};\n\nStep.prototype.toString = function() {\n\tvar s;\n\tswitch (this.axis) {\n\t\tcase Step.ANCESTOR:\n\t\t\ts = "ancestor";\n\t\t\tbreak;\n\t\tcase Step.ANCESTORORSELF:\n\t\t\ts = "ancestor-or-self";\n\t\t\tbreak;\n\t\tcase Step.ATTRIBUTE:\n\t\t\ts = "attribute";\n\t\t\tbreak;\n\t\tcase Step.CHILD:\n\t\t\ts = "child";\n\t\t\tbreak;\n\t\tcase Step.DESCENDANT:\n\t\t\ts = "descendant";\n\t\t\tbreak;\n\t\tcase Step.DESCENDANTORSELF:\n\t\t\ts = "descendant-or-self";\n\t\t\tbreak;\n\t\tcase Step.FOLLOWING:\n\t\t\ts = "following";\n\t\t\tbreak;\n\t\tcase Step.FOLLOWINGSIBLING:\n\t\t\ts = "following-sibling";\n\t\t\tbreak;\n\t\tcase Step.NAMESPACE:\n\t\t\ts = "namespace";\n\t\t\tbreak;\n\t\tcase Step.PARENT:\n\t\t\ts = "parent";\n\t\t\tbreak;\n\t\tcase Step.PRECEDING:\n\t\t\ts = "preceding";\n\t\t\tbreak;\n\t\tcase Step.PRECEDINGSIBLING:\n\t\t\ts = "preceding-sibling";\n\t\t\tbreak;\n\t\tcase Step.SELF:\n\t\t\ts = "self";\n\t\t\tbreak;\n\t}\n\ts += "::";\n\ts += this.nodeTest.toString();\n\tfor (var i = 0; i < this.predicates.length; i++) {\n\t\ts += "[" + this.predicates[i].toString() + "]";\n\t}\n\treturn s;\n};\n\nStep.ANCESTOR = 0;\nStep.ANCESTORORSELF = 1;\nStep.ATTRIBUTE = 2;\nStep.CHILD = 3;\nStep.DESCENDANT = 4;\nStep.DESCENDANTORSELF = 5;\nStep.FOLLOWING = 6;\nStep.FOLLOWINGSIBLING = 7;\nStep.NAMESPACE = 8;\nStep.PARENT = 9;\nStep.PRECEDING = 10;\nStep.PRECEDINGSIBLING = 11;\nStep.SELF = 12;\n\n// NodeTest //////////////////////////////////////////////////////////////////\n\nNodeTest.prototype = new Object();\nNodeTest.prototype.constructor = NodeTest;\nNodeTest.superclass = Object.prototype;\n\nfunction NodeTest(type, value) {\n\tif (arguments.length > 0) {\n\t\tthis.init(type, value);\n\t}\n}\n\nNodeTest.prototype.init = function(type, value) {\n\tthis.type = type;\n\tthis.value = value;\n};\n\nNodeTest.prototype.toString = function() {\n\tswitch (this.type) {\n\t\tcase NodeTest.NAMETESTANY:\n\t\t\treturn "*";\n\t\tcase NodeTest.NAMETESTPREFIXANY:\n\t\t\treturn this.value + ":*";\n\t\tcase NodeTest.NAMETESTRESOLVEDANY:\n\t\t\treturn "{" + this.value + "}*";\n\t\tcase NodeTest.NAMETESTQNAME:\n\t\t\treturn this.value;\n\t\tcase NodeTest.NAMETESTRESOLVEDNAME:\n\t\t\treturn "{" + this.namespaceURI + "}" + this.value;\n\t\tcase NodeTest.COMMENT:\n\t\t\treturn "comment()";\n\t\tcase NodeTest.TEXT:\n\t\t\treturn "text()";\n\t\tcase NodeTest.PI:\n\t\t\tif (this.value != undefined) {\n\t\t\t\treturn "processing-instruction(\\"" + this.value + "\\")";\n\t\t\t}\n\t\t\treturn "processing-instruction()";\n\t\tcase NodeTest.NODE:\n\t\t\treturn "node()";\n\t}\n\treturn "<unknown nodetest type>";\n};\n\nNodeTest.prototype.matches = function(n, xpc) {\n\tswitch (this.type) {\n\t\tcase NodeTest.NAMETESTANY:\n\t\t\tif (n.nodeType == 2 /*Node.ATTRIBUTE_NODE*/\n\t\t\t\t\t|| n.nodeType == 1 /*Node.ELEMENT_NODE*/\n\t\t\t\t\t|| n.nodeType == XPathNamespace.XPATH_NAMESPACE_NODE) {\n\t\t\t\treturn true;\n\t\t\t}\n\t\t\treturn false;\n\t\tcase NodeTest.NAMETESTPREFIXANY:\n\t\t\tif ((n.nodeType == 2 /*Node.ATTRIBUTE_NODE*/ || n.nodeType == 1 /*Node.ELEMENT_NODE*/)) {\n\t\t\t\tvar ns = xpc.namespaceResolver.getNamespace(this.value, xpc.expressionContextNode);\n\t\t\t\tif (ns == null) {\n\t\t\t\t\tthrow new Error("Cannot resolve QName " + this.value);\n\t\t\t\t}\n\t\t\t\treturn true;\t\n\t\t\t}\n\t\t\treturn false;\n\t\tcase NodeTest.NAMETESTQNAME:\n\t\t\tif (n.nodeType == 2 /*Node.ATTRIBUTE_NODE*/\n\t\t\t\t\t|| n.nodeType == 1 /*Node.ELEMENT_NODE*/\n\t\t\t\t\t|| n.nodeType == XPathNamespace.XPATH_NAMESPACE_NODE) {\n\t\t\t\tvar test = Utilities.resolveQName(this.value, xpc.namespaceResolver, xpc.expressionContextNode, false);\n\t\t\t\tif (test[0] == null) {\n\t\t\t\t\tthrow new Error("Cannot resolve QName " + this.value);\n\t\t\t\t}\n\t\t\t\ttest[0] = String(test[0]);\n\t\t\t\ttest[1] = String(test[1]);\n\t\t\t\tif (test[0] == "") {\n\t\t\t\t\ttest[0] = null;\n\t\t\t\t}\n\t\t\t\tvar node = Utilities.resolveQName(n.nodeName, xpc.namespaceResolver, n, n.nodeType == 1 /*Node.ELEMENT_NODE*/);\n\t\t\t\tnode[0] = String(node[0]);\n\t\t\t\tnode[1] = String(node[1]);\n\t\t\t\tif (node[0] == "") {\n\t\t\t\t\tnode[0] = null;\n\t\t\t\t}\n\t\t\t\tif (xpc.caseInsensitive) {\n\t\t\t\t\treturn test[0] == node[0] && String(test[1]).toLowerCase() == String(node[1]).toLowerCase();\n\t\t\t\t}\n\t\t\t\treturn test[0] == node[0] && test[1] == node[1];\n\t\t\t}\n\t\t\treturn false;\n\t\tcase NodeTest.COMMENT:\n\t\t\treturn n.nodeType == 8 /*Node.COMMENT_NODE*/;\n\t\tcase NodeTest.TEXT:\n\t\t\treturn n.nodeType == 3 /*Node.TEXT_NODE*/ || n.nodeType == 4 /*Node.CDATA_SECTION_NODE*/;\n\t\tcase NodeTest.PI:\n\t\t\treturn n.nodeType == 7 /*Node.PROCESSING_INSTRUCTION_NODE*/\n\t\t\t\t&& (this.value == null || n.nodeName == this.value);\n\t\tcase NodeTest.NODE:\n\t\t\treturn n.nodeType == 9 /*Node.DOCUMENT_NODE*/\n\t\t\t\t|| n.nodeType == 1 /*Node.ELEMENT_NODE*/\n\t\t\t\t|| n.nodeType == 2 /*Node.ATTRIBUTE_NODE*/\n\t\t\t\t|| n.nodeType == 3 /*Node.TEXT_NODE*/\n\t\t\t\t|| n.nodeType == 4 /*Node.CDATA_SECTION_NODE*/\n\t\t\t\t|| n.nodeType == 8 /*Node.COMMENT_NODE*/\n\t\t\t\t|| n.nodeType == 7 /*Node.PROCESSING_INSTRUCTION_NODE*/;\n\t}\n\treturn false;\n};\n\nNodeTest.NAMETESTANY = 0;\nNodeTest.NAMETESTPREFIXANY = 1;\nNodeTest.NAMETESTQNAME = 2;\nNodeTest.COMMENT = 3;\nNodeTest.TEXT = 4;\nNodeTest.PI = 5;\nNodeTest.NODE = 6;\n\n// VariableReference /////////////////////////////////////////////////////////\n\nVariableReference.prototype = new Expression();\nVariableReference.prototype.constructor = VariableReference;\nVariableReference.superclass = Expression.prototype;\n\nfunction VariableReference(v) {\n\tif (arguments.length > 0) {\n\t\tthis.init(v);\n\t}\n}\n\nVariableReference.prototype.init = function(v) {\n\tthis.variable = v;\n};\n\nVariableReference.prototype.toString = function() {\n\treturn "$" + this.variable;\n};\n\nVariableReference.prototype.evaluate = function(c) {\n\treturn c.variableResolver.getVariable(this.variable, c);\n};\n\n// FunctionCall //////////////////////////////////////////////////////////////\n\nFunctionCall.prototype = new Expression();\nFunctionCall.prototype.constructor = FunctionCall;\nFunctionCall.superclass = Expression.prototype;\n\nfunction FunctionCall(fn, args) {\n\tif (arguments.length > 0) {\n\t\tthis.init(fn, args);\n\t}\n}\n\nFunctionCall.prototype.init = function(fn, args) {\n\tthis.functionName = fn;\n\tthis.arguments = args;\n};\n\nFunctionCall.prototype.toString = function() {\n\tvar s ' , builder.string += '= this.functionName + "(";\n\tfor (var i = 0; i < this.arguments.length; i++) {\n\t\tif (i > 0) {\n\t\t\ts += ", ";\n\t\t}\n\t\ts += this.arguments[i].toString();\n\t}\n\treturn s + ")";\n};\n\nFunctionCall.prototype.evaluate = function(c) {\n\tvar f = c.functionResolver.getFunction(this.functionName, c);\n\tif (f == undefined) {\n\t\tthrow new Error("Unknown function " + this.functionName);\n\t}\n\tvar a = [c].concat(this.arguments);\n\treturn f.apply(c.functionResolver.thisArg, a);\n};\n\n// XString ///////////////////////////////////////////////////////////////////\n\nXString.prototype = new Expression();\nXString.prototype.constructor = XString;\nXString.superclass = Expression.prototype;\n\nfunction XString(s) {\n\tif (arguments.length > 0) {\n\t\tthis.init(s);\n\t}\n}\n\nXString.prototype.init = function(s) {\n\tthis.str = s;\n};\n\nXString.prototype.toString = function() {\n\treturn this.str;\n};\n\nXString.prototype.evaluate = function(c) {\n\treturn this;\n};\n\nXString.prototype.string = function() {\n\treturn this;\n};\n\nXString.prototype.number = function() {\n\treturn new XNumber(this.str);\n};\n\nXString.prototype.bool = function() {\n\treturn new XBoolean(this.str);\n};\n\nXString.prototype.nodeset = function() {\n\tthrow new Error("Cannot convert string to nodeset");\n};\n\nXString.prototype.stringValue = function() {\n\treturn this.str;\n};\n\nXString.prototype.numberValue = function() {\n\treturn this.number().numberValue();\n};\n\nXString.prototype.booleanValue = function() {\n\treturn this.bool().booleanValue();\n};\n\nXString.prototype.equals = function(r) {\n\tif (Utilities.instance_of(r, XBoolean)) {\n\t\treturn this.bool().equals(r);\n\t}\n\tif (Utilities.instance_of(r, XNumber)) {\n\t\treturn this.number().equals(r);\n\t}\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithString(this, Operators.equals);\n\t}\n\treturn new XBoolean(this.str == r.str);\n};\n\nXString.prototype.notequal = function(r) {\n\tif (Utilities.instance_of(r, XBoolean)) {\n\t\treturn this.bool().notequal(r);\n\t}\n\tif (Utilities.instance_of(r, XNumber)) {\n\t\treturn this.number().notequal(r);\n\t}\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithString(this, Operators.notequal);\n\t}\n\treturn new XBoolean(this.str != r.str);\n};\n\nXString.prototype.lessthan = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this.number(), Operators.greaterthanorequal);\n\t}\n\treturn this.number().lessthan(r.number());\n};\n\nXString.prototype.greaterthan = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this.number(), Operators.lessthanorequal);\n\t}\n\treturn this.number().greaterthan(r.number());\n};\n\nXString.prototype.lessthanorequal = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this.number(), Operators.greaterthan);\n\t}\n\treturn this.number().lessthanorequal(r.number());\n};\n\nXString.prototype.greaterthanorequal = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this.number(), Operators.lessthan);\n\t}\n\treturn this.number().greaterthanorequal(r.number());\n};\n\n// XNumber ///////////////////////////////////////////////////////////////////\n\nXNumber.prototype = new Expression();\nXNumber.prototype.constructor = XNumber;\nXNumber.superclass = Expression.prototype;\n\nfunction XNumber(n) {\n\tif (arguments.length > 0) {\n\t\tthis.init(n);\n\t}\n}\n\nXNumber.prototype.init = function(n) {\n\tthis.num = Number(n);\n};\n\nXNumber.prototype.toString = function() {\n\treturn this.num;\n};\n\nXNumber.prototype.evaluate = function(c) {\n\treturn this;\n};\n\nXNumber.prototype.string = function() {\n\treturn new XString(this.num);\n};\n\nXNumber.prototype.number = function() {\n\treturn this;\n};\n\nXNumber.prototype.bool = function() {\n\treturn new XBoolean(this.num);\n};\n\nXNumber.prototype.nodeset = function() {\n\tthrow new Error("Cannot convert number to nodeset");\n};\n\nXNumber.prototype.stringValue = function() {\n\treturn this.string().stringValue();\n};\n\nXNumber.prototype.numberValue = function() {\n\treturn this.num;\n};\n\nXNumber.prototype.booleanValue = function() {\n\treturn this.bool().booleanValue();\n};\n\nXNumber.prototype.negate = function() {\n\treturn new XNumber(-this.num);\n};\n\nXNumber.prototype.equals = function(r) {\n\tif (Utilities.instance_of(r, XBoolean)) {\n\t\treturn this.bool().equals(r);\n\t}\n\tif (Utilities.instance_of(r, XString)) {\n\t\treturn this.equals(r.number());\n\t}\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this, Operators.equals);\n\t}\n\treturn new XBoolean(this.num == r.num);\n};\n\nXNumber.prototype.notequal = function(r) {\n\tif (Utilities.instance_of(r, XBoolean)) {\n\t\treturn this.bool().notequal(r);\n\t}\n\tif (Utilities.instance_of(r, XString)) {\n\t\treturn this.notequal(r.number());\n\t}\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this, Operators.notequal);\n\t}\n\treturn new XBoolean(this.num != r.num);\n};\n\nXNumber.prototype.lessthan = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this, Operators.greaterthanorequal);\n\t}\n\tif (Utilities.instance_of(r, XBoolean) || Utilities.instance_of(r, XString)) {\n\t\treturn this.lessthan(r.number());\n\t}\n\treturn new XBoolean(this.num < r.num);\n};\n\nXNumber.prototype.greaterthan = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this, Operators.lessthanorequal);\n\t}\n\tif (Utilities.instance_of(r, XBoolean) || Utilities.instance_of(r, XString)) {\n\t\treturn this.greaterthan(r.number());\n\t}\n\treturn new XBoolean(this.num > r.num);\n};\n\nXNumber.prototype.lessthanorequal = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this, Operators.greaterthan);\n\t}\n\tif (Utilities.instance_of(r, XBoolean) || Utilities.instance_of(r, XString)) {\n\t\treturn this.lessthanorequal(r.number());\n\t}\n\treturn new XBoolean(this.num <= r.num);\n};\n\nXNumber.prototype.greaterthanorequal = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this, Operators.lessthan);\n\t}\n\tif (Utilities.instance_of(r, XBoolean) || Utilities.instance_of(r, XString)) {\n\t\treturn this.greaterthanorequal(r.number());\n\t}\n\treturn new XBoolean(this.num >= r.num);\n};\n\nXNumber.prototype.plus = function(r) {\n\treturn new XNumber(this.num + r.num);\n};\n\nXNumber.prototype.minus = function(r) {\n\treturn new XNumber(this.num - r.num);\n};\n\nXNumber.prototype.multiply = function(r) {\n\treturn new XNumber(this.num * r.num);\n};\n\nXNumber.prototype.div = function(r) {\n\treturn new XNumber(this.num / r.num);\n};\n\nXNumber.prototype.mod = function(r) {\n\treturn new XNumber(this.num % r.num);\n};\n\n// XBoolean //////////////////////////////////////////////////////////////////\n\nXBoolean.prototype = new Expression();\nXBoolean.prototype.constructor = XBoolean;\nXBoolean.superclass = Expression.prototype;\n\nfunction XBoolean(b) {\n\tif (arguments.length > 0) {\n\t\tthis.init(b);\n\t}\n}\n\nXBoolean.prototype.init = function(b) {\n\tthis.b = Boolean(b);\n};\n\nXBoolean.prototype.toString = function() {\n\treturn this.b.toString();\n};\n\nXBoolean.prototype.evaluate = function(c) {\n\treturn this;\n};\n\nXBoolean.prototype.string = function() {\n\treturn new XString(this.b);\n};\n\nXBoolean.prototype.number = function() {\n\treturn new XNumber(this.b);\n};\n\nXBoolean.prototype.bool = function() {\n\treturn this;\n};\n\nXBoolean.prototype.nodeset = function() {\n\tthrow new Error("Cannot convert boolean to nodeset");\n};\n\nXBoolean.prototype.stringValue = function() {\n\treturn this.string().stringValue();\n};\n\nXBoolean.prototype.numberValue = function() {\n\treturn this.num().numberValue();\n};\n\nXBoolean.prototype.booleanValue = function() {\n\treturn this.b;\n};\n\nXBoolean.prototype.not = function() {\n\treturn new XBoolean(!this.b);\n};\n\nXBoolean.prototype.equals = function(r) {\n\tif (Utilities.instance_of(r, XString) || Utilities.instance_of(r, XNumber)) {\n\t\treturn this.equals(r.bool());\n\t}\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithBoolean(this, Operators.equals);\n\t}\n\treturn new XBoolean(this.b == r.b);\n};\n\nXBoolean.prototype.notequal = function(r) {\n\tif (Utilities.instance_of(r, XString) || Utilities.instance_of(r, XNumber)) {\n\t\treturn this.notequal(r.bool());\n\t}\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithBoolean(this, Operators.notequal);\n\t}\n\treturn new XBoolean(this.b != r.b);\n};\n\nXBoolean.prototype.lessthan = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this.number(), Operators.greaterthanorequal);\n\t}\n\treturn this.number().lessthan(r.number());\n};\n\nXBoolean.prototype.greaterthan = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this.number(), Operators.lessthanorequal);\n\t}\n\treturn this.number().greaterthan(r.number());\n};\n\nXBoolean.prototype.lessthanorequal = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this.number(), Operators.greaterthan);\n\t}\n\treturn this.number().lessthanorequal(r.number());\n};\n\nXBoolean.prototype.greaterthanorequal = function(r) {\n\tif (Utilities.instance_of(r, XNodeSet)) {\n\t\treturn r.compareWithNumber(this.number(), Operators.lessthan);\n\t}\n\treturn this.number().greaterthanorequal(r.number());\n};\n\n// AVLTree ///////////////////////////////////////////////////////////////////\n\nAVLTree.prototype = new Object();\nAVLTree.prototype.constructor = AVLTree;\nAVLTree.superclass = Object.prototype;\n\nfunction AVLTree(n) {\n\tthis.init(n);\n}\n\nAVLTree.prototype.init = function(n) {\n\tthis.left = null;\n    this.right = null;\n\tthis.node = n;\n\tthis.depth = 1;\n};\n\nAVLTree.prototype.balance = function() {\n    var ldepth = this.left  == null ? 0 : this.left.depth;\n    var rdepth = this.right == null ? 0 : this.right.depth;\n\n\tif (ldepth > rdepth + 1) {\n        // LR or LL rotation\n        var lldepth = this.left.left  == null ? 0 : this.left.left.depth;\n        var lrdepth = this.left.right == null ? 0 : this.left.right.depth;\n\n        if (lldepth < lrdepth) {\n            // LR rotation consists of a RR rotation of the left child\n            this.left.rotateRR();\n            // plus a LL rotation of this node, which happens anyway \n        }\n        this.rotateLL();       \n    } else if (ldepth + 1 < rdepth) {\n        // RR or RL rorarion\n\t\tvar rrdepth = this.right.right == null ? 0 : this.right.right.depth;\n\t\tvar rldepth = this.right.left  == null ? 0 : this.right.left.depth;\n\t \n        if (rldepth > rrdepth) {\n            // RR rotation consists of a LL rotation of the right child\n            this.right.rotateLL();\n            // plus a RR rotation of this node, which happens anyway \n        }\n        this.rotateRR();\n    }\t     \n};\n\nAVLTree.prototype.rotateLL = function() {\n    // the left side is too long => rotate from the left (_not_ leftwards)\n    var nodeBefore = this.node;\n    var rightBefore = this.right;\n    this.node = this.left.node;\n    this.right = this.left;\n    this.left = this.left.left;\n    this.right.left = this.right.right;\n    this.right.right = rightBefore;\n    this.right.node = nodeBefore;\n    this.right.updateInNewLocation();\n    this.updateInNewLocation();\n};\n\nAVLTree.prototype.rotateRR = function() {\n    // the right side is too long => rotate from the right (_not_ rightwards)\n    var nodeBefore = this.node;\n    var leftBefore = this.left;\n    this.node = this.right.node;\n    this.left = this.right;\n    this.right = this.right.right;\n    this.left.right = this.left.left;\n    this.left.left = leftBefore;\n    this.left.node = nodeBefore;\n    this.left.updateInNewLocation();\n    this.updateInNewLocation();\n}; \n\t\nAVLTree.prototype.updateInNewLocation = function() {\n    this.getDepthFromChildren();\n};\n\nAVLTree.prototype.getDepthFromChildren = function() {\n    this.depth = this.node == null ? 0 : 1;\n    if (this.left != null) {\n        this.depth = this.left.depth + 1;\n    }\n    if (this.right != null && this.depth <= this.right.depth) {\n        this.depth = this.right.depth + 1;\n    }\n};\n\nAVLTree.prototype.order = function(n1, n2) {\n\tif (n1 === n2) {\n\t\treturn 0;\n\t}\n\tvar d1 = 0;\n\tvar d2 = 0;\n\tfor (var m1 = n1; m1 != null; m1 = m1.parentNode) {\n\t\td1++;\n\t}\n\tfor (var m2 = n2; m2 != null; m2 = m2.parentNode) {\n\t\td2++;\n\t}\n\tif (d1 > d2) {\n\t\twhile (d1 > d2) {\n\t\t\tn1 = n1.parentNode;\n\t\t\td1--;\n\t\t}\n\t\tif (n1 == n2) {\n\t\t\treturn 1;\n\t\t}\n\t} else if (d2 > d1) {\n\t\twhile (d2 > d1) {\n\t\t\tn2 = n2.parentNode;\n\t\t\td2--;\n\t\t}\n\t\tif (n1 == n2) {\n\t\t\treturn -1;\n\t\t}\n\t}\n\twhile (n1.parentNode != n2.parentNode) {\n\t\tn1 = n1.parentNode;\n\t\tn2 = n2.parentNode;\n\t}\n\twhile (n1.previousSibling != null && n2.previousSibling != null) {\n\t\tn1 = n1.previousSibling;\n\t\tn2 = n2.previousSibling;\n\t}\n\tif (n1.previousSibling == null) {\n\t\treturn -1;\n\t}\n\treturn 1;\n};\n\nAVLTree.prototype.add = function(n)  {\n\tif (n === this.node) {\n        return false;\n    }\n\t\n\tvar o = this.order(n, this.node);\n\t\n    var ret = false;\n    if (o == -1) {\n        if (this.left == null) {\n            this.left = new AVLTree(n);\n            ret = true;\n        } else {\n            ret = this.left.add(n);\n            if (ret) {\n                this.balance();\n            }\n        }\n    } else if (o == 1) {\n        if (this.right == null) {\n            this.right = new AVLTree(n);\n            ret = true;\n        } else {\n            ret = this.right.add(n);\n            if (ret) {\n                this.balance();\n            }\n        }\n    }\n\t\n    if (ret) {\n        this.getDepthFromChildren();\n    }\n    return ret;\n};\n\n// XNodeSet //////////////////////////////////////////////////////////////////\n\nXNodeSet.prototype = new Expression();\nXNodeSet.prototype.constructor = XNodeSet;\nXNodeSet.superclass = Expression.prototype;\n\nfunction XNodeSet() {\n\tthis.init();\n}\n\nXNodeSet.prototype.init = function() {\n\tthis.tree = null;\n\tthis.size = 0;\n};\n\nXNodeSet.prototype.toString = function() {\n\tvar p = this.first();\n\tif (p == null) {\n\t\treturn "";\n\t}\n\treturn this.stringForNode(p);\n};\n\nXNodeSet.prototype.evaluate = function(c) {\n\treturn this;\n};\n\nXNodeSet.prototype.string = function() {\n\treturn new XString(this.toString());\n};\n\nXNodeSet.prototype.stringValue = function() {\n\treturn this.toString();\n};\n\nXNodeSet.prototype.number = function() {\n\treturn new XNumber(this.string());\n};\n\nXNodeSet.prototype.numberValue = function() {\n\treturn Number(this.string());\n};\n\nXNodeSet.prototype.bool = function() {\n\treturn new XBoolean(this.tree != null);\n};\n\nXNodeSet.prototype.booleanValue = function() {\n\treturn this.tree != null;\n};\n\nXNodeSet.prototype.nodeset = function() {\n\treturn this;\n};\n\nXNodeSet.prototype.stringForNode = function(n) {\n\tif (n.nodeType == 9 /*Node.DOCUMENT_NODE*/) {\n\t\tn = n.documentElement;\n\t}\n\tif (n.nodeType == 1 /*Node.ELEMENT_NODE*/) {\n\t\treturn this.stringForNodeRec(n);\n\t}\n\tif (n.isNamespaceNode) {\n\t\treturn n.namespace;\n\t}\n\treturn n.nodeValue;\n};\n\nXNodeSet.prototype.stringForNodeRec = function(n) {\n\tvar s = "";\n\tfor (var n2 = n.firstChild; n2 != null; n2 = n2.nextSibling) {\n\t\tif (n2.nodeType == 3 /*Node.TEXT_NODE*/) {\n\t\t\ts += n2.nodeValue;\n\t\t} else if (n2.nodeType == 1 /*Node.ELEMENT_NODE*/) {\n\t\t\ts += this.stringForNodeRec(n2);\n\t\t}\n\t}\n\treturn s;\n};\n\nXNodeSet.prototype.first = function() {\n\tvar p = this.tree;\n\tif (p == null) {\n\t\treturn null;\n\t}\n\twhile (p.left != null) {\n\t\tp = p.left;\n\t}\n\treturn p.node;\n};\n\nXNodeSet.prototype.add = function(n) {\n    var added;\n    if (this.tree == null) {\n        this.tree = new AVLTree(n);\n        added = true;\n    } else {\n        added = this.tree.add(n);\n    }\n    if (added) {\n        this.size++;\n    }\n};\n\nXNodeSet.prototype.addArray = function(ns) {\n\tfor (var i = 0; i < ns.length; i++) {\n\t\tthis.add(ns[i]);\n\t}\n};\n\nXNodeSet.prototype.toArray = function() {\n\tvar a = [];\n\tthis.toArrayRec(this.tree, a);\n\treturn a;\n};\n\nXNodeSet.prototype.toArrayRec = function(t, a) {\n\tif (t != null) {\n\t\tthis.toArrayRec(t.left, a);\n\t\ta.push(t.node);\n\t\tthis.toArrayRec(t.right, a);\n\t}\n};\n\nXNodeSet.prototype.compareWithString = function(r, o) {\n\tvar a = this.toArray();\n\tfor (var i = 0; i < a.length; i++) {\n\t\tvar n = a[i];\n\t\tvar l = new XString(this.stringForNode(n));\n\t\tvar res = o(l, r);\n\t\tif (res.booleanValue()) {\n\t\t\treturn res;\n\t\t}\n\t}\n\treturn new XBoolean(false);\n};\n\nXNodeSet.prototype.compareWithNumber = function(r, o) {\n\tvar a = this.toArray();\n\tfor (var i = 0; i < a.length; i++) {\n\t\tvar n = a[i];\n\t\tvar l = new XNumber(this.stringForNode(n));\n\t\tvar res = o(l, r);\n\t\tif (res.booleanValue()) {\n\t\t\treturn res;\n\t\t}\n\t}\n\treturn new XBoolean(false);\n};\n\nXNodeSet.prototype.compareWithBoolean = function(r, o) {\n\treturn o(this.bool(), r);' , builder.string += '\n};\n\nXNodeSet.prototype.compareWithNodeSet = function(r, o) {\n\tvar a = this.toArray();\n\tfor (var i = 0; i < a.length; i++) {\n\t\tvar n = a[i];\n\t\tvar l = new XString(this.stringForNode(n));\n\t\tvar b = r.toArray();\n\t\tfor (var j = 0; j < b.length; j++) {\n\t\t\tvar n2 = b[j];\n\t\t\tvar r = new XString(this.stringForNode(n2));\n\t\t\tvar res = o(l, r);\n\t\t\tif (res.booleanValue()) {\n\t\t\t\treturn res;\n\t\t\t}\n\t\t}\n\t}\n\treturn new XBoolean(false);\n};\n\nXNodeSet.prototype.equals = function(r) {\n\tif (Utilities.instance_of(r, XString)) {\n\t\treturn this.compareWithString(r, Operators.equals);\n\t}\n\tif (Utilities.instance_of(r, XNumber)) {\n\t\treturn this.compareWithNumber(r, Operators.equals);\n\t}\n\tif (Utilities.instance_of(r, XBoolean)) {\n\t\treturn this.compareWithBoolean(r, Operators.equals);\n\t}\n\treturn this.compareWithNodeSet(r, Operators.equals);\n};\n\nXNodeSet.prototype.notequal = function(r) {\n\tif (Utilities.instance_of(r, XString)) {\n\t\treturn this.compareWithString(r, Operators.notequal);\n\t}\n\tif (Utilities.instance_of(r, XNumber)) {\n\t\treturn this.compareWithNumber(r, Operators.notequal);\n\t}\n\tif (Utilities.instance_of(r, XBoolean)) {\n\t\treturn this.compareWithBoolean(r, Operators.notequal);\n\t}\n\treturn this.compareWithNodeSet(r, Operators.notequal);\n};\n\nXNodeSet.prototype.lessthan = function(r) {\n\tif (Utilities.instance_of(r, XString)) {\n\t\treturn this.compareWithNumber(r.number(), Operators.lessthan);\n\t}\n\tif (Utilities.instance_of(r, XNumber)) {\n\t\treturn this.compareWithNumber(r, Operators.lessthan);\n\t}\n\tif (Utilities.instance_of(r, XBoolean)) {\n\t\treturn this.compareWithBoolean(r, Operators.lessthan);\n\t}\n\treturn this.compareWithNodeSet(r, Operators.lessthan);\n};\n\nXNodeSet.prototype.greaterthan = function(r) {\n\tif (Utilities.instance_of(r, XString)) {\n\t\treturn this.compareWithNumber(r.number(), Operators.greaterthan);\n\t}\n\tif (Utilities.instance_of(r, XNumber)) {\n\t\treturn this.compareWithNumber(r, Operators.greaterthan);\n\t}\n\tif (Utilities.instance_of(r, XBoolean)) {\n\t\treturn this.compareWithBoolean(r, Operators.greaterthan);\n\t}\n\treturn this.compareWithNodeSet(r, Operators.greaterthan);\n};\n\nXNodeSet.prototype.lessthanorequal = function(r) {\n\tif (Utilities.instance_of(r, XString)) {\n\t\treturn this.compareWithNumber(r.number(), Operators.lessthanorequal);\n\t}\n\tif (Utilities.instance_of(r, XNumber)) {\n\t\treturn this.compareWithNumber(r, Operators.lessthanorequal);\n\t}\n\tif (Utilities.instance_of(r, XBoolean)) {\n\t\treturn this.compareWithBoolean(r, Operators.lessthanorequal);\n\t}\n\treturn this.compareWithNodeSet(r, Operators.lessthanorequal);\n};\n\nXNodeSet.prototype.greaterthanorequal = function(r) {\n\tif (Utilities.instance_of(r, XString)) {\n\t\treturn this.compareWithNumber(r.number(), Operators.greaterthanorequal);\n\t}\n\tif (Utilities.instance_of(r, XNumber)) {\n\t\treturn this.compareWithNumber(r, Operators.greaterthanorequal);\n\t}\n\tif (Utilities.instance_of(r, XBoolean)) {\n\t\treturn this.compareWithBoolean(r, Operators.greaterthanorequal);\n\t}\n\treturn this.compareWithNodeSet(r, Operators.greaterthanorequal);\n};\n\nXNodeSet.prototype.union = function(r) {\n\tvar ns = new XNodeSet();\n\tns.tree = this.tree;\n\tns.size = this.size;\n\tns.addArray(r.toArray());\n\treturn ns;\n};\n\n// XPathNamespace ////////////////////////////////////////////////////////////\n\nXPathNamespace.prototype = new Object();\nXPathNamespace.prototype.constructor = XPathNamespace;\nXPathNamespace.superclass = Object.prototype;\n\nfunction XPathNamespace(pre, ns, p) {\n\tthis.isXPathNamespace = true;\n\tthis.ownerDocument = p.ownerDocument;\n\tthis.nodeName = "#namespace";\n\tthis.prefix = pre;\n\tthis.localName = pre;\n\tthis.namespaceURI = ns;\n\tthis.nodeValue = ns;\n\tthis.ownerElement = p;\n\tthis.nodeType = XPathNamespace.XPATH_NAMESPACE_NODE;\n}\n\nXPathNamespace.prototype.toString = function() {\n\treturn "{ \\"" + this.prefix + "\\", \\"" + this.namespaceURI + "\\" }";\n};\n\n// Operators /////////////////////////////////////////////////////////////////\n\nvar Operators = new Object();\n\nOperators.equals = function(l, r) {\n\treturn l.equals(r);\n};\n\nOperators.notequal = function(l, r) {\n\treturn l.notequal(r);\n};\n\nOperators.lessthan = function(l, r) {\n\treturn l.lessthan(r);\n};\n\nOperators.greaterthan = function(l, r) {\n\treturn l.greaterthan(r);\n};\n\nOperators.lessthanorequal = function(l, r) {\n\treturn l.lessthanorequal(r);\n};\n\nOperators.greaterthanorequal = function(l, r) {\n\treturn l.greaterthanorequal(r);\n};\n\n// XPathContext //////////////////////////////////////////////////////////////\n\nXPathContext.prototype = new Object();\nXPathContext.prototype.constructor = XPathContext;\nXPathContext.superclass = Object.prototype;\n\nfunction XPathContext(vr, nr, fr) {\n\tthis.variableResolver = vr != null ? vr : new VariableResolver();\n\tthis.namespaceResolver = nr != null ? nr : new NamespaceResolver();\n\tthis.functionResolver = fr != null ? fr : new FunctionResolver();\n}\n\n// VariableResolver //////////////////////////////////////////////////////////\n\nVariableResolver.prototype = new Object();\nVariableResolver.prototype.constructor = VariableResolver;\nVariableResolver.superclass = Object.prototype;\n\nfunction VariableResolver() {\n}\n\nVariableResolver.prototype.getVariable = function(vn, c) {\n\tvar parts = Utilities.splitQName(vn);\n\tif (parts[0] != null) {\n\t\tparts[0] = c.namespaceResolver.getNamespace(parts[0], c.expressionContextNode);\n        if (parts[0] == null) {\n            throw new Error("Cannot resolve QName " + fn);\n        }\n\t}\n\treturn this.getVariableWithName(parts[0], parts[1], c.expressionContextNode);\n};\n\nVariableResolver.prototype.getVariableWithName = function(ns, ln, c) {\n\treturn null;\n};\n\n// FunctionResolver //////////////////////////////////////////////////////////\n\nFunctionResolver.prototype = new Object();\nFunctionResolver.prototype.constructor = FunctionResolver;\nFunctionResolver.superclass = Object.prototype;\n\nfunction FunctionResolver(thisArg) {\n\tthis.thisArg = thisArg != null ? thisArg : Functions;\n\tthis.functions = new Object();\n\tthis.addStandardFunctions();\n}\n\nFunctionResolver.prototype.addStandardFunctions = function() {\n\tthis.functions["{}last"] = Functions.last;\n\tthis.functions["{}position"] = Functions.position;\n\tthis.functions["{}count"] = Functions.count;\n\tthis.functions["{}id"] = Functions.id;\n\tthis.functions["{}local-name"] = Functions.localName;\n\tthis.functions["{}namespace-uri"] = Functions.namespaceURI;\n\tthis.functions["{}name"] = Functions.name;\n\tthis.functions["{}string"] = Functions.string;\n\tthis.functions["{}concat"] = Functions.concat;\n\tthis.functions["{}starts-with"] = Functions.startsWith;\n\tthis.functions["{}contains"] = Functions.contains;\n\tthis.functions["{}substring-before"] = Functions.substringBefore;\n\tthis.functions["{}substring-after"] = Functions.substringAfter;\n\tthis.functions["{}substring"] = Functions.substring;\n\tthis.functions["{}string-length"] = Functions.stringLength;\n\tthis.functions["{}normalize-space"] = Functions.normalizeSpace;\n\tthis.functions["{}translate"] = Functions.translate;\n\tthis.functions["{}boolean"] = Functions.boolean_;\n\tthis.functions["{}not"] = Functions.not;\n\tthis.functions["{}true"] = Functions.true_;\n\tthis.functions["{}false"] = Functions.false_;\n\tthis.functions["{}lang"] = Functions.lang;\n\tthis.functions["{}number"] = Functions.number;\n\tthis.functions["{}sum"] = Functions.sum;\n\tthis.functions["{}floor"] = Functions.floor;\n\tthis.functions["{}ceiling"] = Functions.ceiling;\n\tthis.functions["{}round"] = Functions.round;\n};\n\nFunctionResolver.prototype.addFunction = function(ns, ln, f) {\n\tthis.functions["{" + ns + "}" + ln] = f;\n};\n\nFunctionResolver.prototype.getFunction = function(fn, c) {\n\tvar parts = Utilities.resolveQName(fn, c.namespaceResolver, c.contextNode, false);\n    if (parts[0] == null) {\n        throw new Error("Cannot resolve QName " + fn);\n    }\n\treturn this.getFunctionWithName(parts[0], parts[1], c.contextNode);\n};\n\nFunctionResolver.prototype.getFunctionWithName = function(ns, ln, c) {\n\treturn this.functions["{" + ns + "}" + ln];\n};\n\n// NamespaceResolver /////////////////////////////////////////////////////////\n\nNamespaceResolver.prototype = new Object();\nNamespaceResolver.prototype.constructor = NamespaceResolver;\nNamespaceResolver.superclass = Object.prototype;\n\nfunction NamespaceResolver() {\n}\n\nNamespaceResolver.prototype.getNamespace = function(prefix, n) {\n\tif (prefix == "xml") {\n\t\treturn XPath.XML_NAMESPACE_URI;\n\t} else if (prefix == "xmlns") {\n\t\treturn XPath.XMLNS_NAMESPACE_URI;\n\t}\n\tif (n.nodeType == 9 /*Node.DOCUMENT_NODE*/) {\n\t\tn = n.documentElement;\n\t} else if (n.nodeType == 2 /*Node.ATTRIBUTE_NODE*/) {\n\t\tn = PathExpr.prototype.getOwnerElement(n);\n\t} else if (n.nodeType != 1 /*Node.ELEMENT_NODE*/) {\n\t\tn = n.parentNode;\n\t}\n\twhile (n != null && n.nodeType == 1 /*Node.ELEMENT_NODE*/) {\n\t\tvar nnm = n.attributes;\n\t\tfor (var i = 0; i < nnm.length; i++) {\n\t\t\tvar a = nnm.item(i);\n\t\t\tvar aname = a.nodeName;\n\t\t\tif (aname == "xmlns" && prefix == ""\n\t\t\t\t\t|| aname == "xmlns:" + prefix) {\n\t\t\t\treturn String(a.nodeValue);\n\t\t\t}\n\t\t}\n\t\tn = n.parentNode;\n\t}\n\treturn null;\n};\n\n// Functions /////////////////////////////////////////////////////////////////\n\nFunctions = new Object();\n\nFunctions.last = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 1) {\n\t\tthrow new Error("Function last expects ()");\n\t}\n\treturn new XNumber(c.contextSize);\n};\n\nFunctions.position = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 1) {\n\t\tthrow new Error("Function position expects ()");\n\t}\n\treturn new XNumber(c.contextPosition);\n};\n\nFunctions.count = function() {\n\tvar c = arguments[0];\n\tvar ns;\n\tif (arguments.length != 2 || !Utilities.instance_of(ns = arguments[1].evaluate(c), XNodeSet)) {\n\t\tthrow new Error("Function count expects (node-set)");\n\t}\n\treturn new XNumber(ns.size);\n};\n\nFunctions.id = function() {\n\tvar c = arguments[0];\n\tvar id;\n\tif (arguments.length != 2) {\n\t\tthrow new Error("Function id expects (object)");\n\t}\n\tid = arguments[1].evaluate(c);\n\tif (Utilities.instance_of(id, XNodeSet)) {\n\t\tid = id.toArray().join(" ");\n\t} else {\n\t\tid = id.stringValue();\n\t}\n\tvar ids = id.split(/[\\x0d\\x0a\\x09\\x20]+/);\n\tvar count = 0;\n\tvar ns = new XNodeSet();\n\tvar doc = c.contextNode.nodeType == 9 /*Node.DOCUMENT_NODE*/\n\t\t\t? c.contextNode\n\t\t\t: c.contextNode.ownerDocument;\n\tfor (var i = 0; i < ids.length; i++) {\n\t\tvar n;\n\t\tif (doc.getElementById) {\n\t\t\tn = doc.getElementById(ids[i]);\n\t\t} else {\n\t\t\tn = Utilities.getElementById(doc, ids[i]);\n\t\t}\n\t\tif (n != null) {\n\t\t\tns.add(n);\n\t\t\tcount++;\n\t\t}\n\t}\n\treturn ns;\n};\n\nFunctions.localName = function() {\n\tvar c = arguments[0];\n\tvar n;\n\tif (arguments.length == 1) {\n\t\tn = c.contextNode;\n\t} else if (arguments.length == 2) {\n\t\tn = arguments[1].evaluate(c).first();\n\t} else {\n\t\tthrow new Error("Function local-name expects (node-set?)");\n\t}\n\tif (n == null) {\n\t\treturn new XString("");\n\t}\n\treturn new XString(n.localName ? n.localName : n.baseName);\n};\n\nFunctions.namespaceURI = function() {\n\tvar c = arguments[0];\n\tvar n;\n\tif (arguments.length == 1) {\n\t\tn = c.contextNode;\n\t} else if (arguments.length == 2) {\n\t\tn = arguments[1].evaluate(c).first();\n\t} else {\n\t\tthrow new Error("Function namespace-uri expects (node-set?)");\n\t}\n\tif (n == null) {\n\t\treturn new XString("");\n\t}\n\treturn new XString(n.namespaceURI);\n};\n\nFunctions.name = function() {\n\tvar c = arguments[0];\n\tvar n;\n\tif (arguments.length == 1) {\n\t\tn = c.contextNode;\n\t} else if (arguments.length == 2) {\n\t\tn = arguments[1].evaluate(c).first();\n\t} else {\n\t\tthrow new Error("Function name expects (node-set?)");\n\t}\n\tif (n == null) {\n\t\treturn new XString("");\n\t}\n\tif (n.nodeType == 1 /*Node.ELEMENT_NODE*/ || n.nodeType == 2 /*Node.ATTRIBUTE_NODE*/) {\n\t\treturn new XString(n.nodeName);\n\t} else if (n.localName == null) {\n\t\treturn new XString("");\n\t} else {\n\t\treturn new XString(n.localName);\n\t}\n};\n\nFunctions.string = function() {\n\tvar c = arguments[0];\n\tif (arguments.length == 1) {\n\t\treturn XNodeSet.prototype.stringForNode(c.contextNode);\n\t} else if (arguments.length == 2) {\n\t\treturn arguments[1].evaluate(c).string();\n\t}\n\tthrow new Error("Function string expects (object?)");\n};\n\nFunctions.concat = function() {\n\tvar c = arguments[0];\n\tif (arguments.length < 3) {\n\t\tthrow new Error("Function concat expects (string, string, string*)");\n\t}\n\tvar s = "";\n\tfor (var i = 1; i < arguments.length; i++) {\n\t\ts += arguments[i].evaluate(c).stringValue();\n\t}\n\treturn new XString(s);\n};\n\nFunctions.startsWith = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 3) {\n\t\tthrow new Error("Function startsWith expects (string, string)");\n\t}\n\tvar s1 = arguments[1].evaluate(c).stringValue();\n\tvar s2 = arguments[2].evaluate(c).stringValue();\n\treturn new XBoolean(s1.substring(0, s2.length) == s2);\n};\n\nFunctions.contains = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 3) {\n\t\tthrow new Error("Function contains expects (string, string)");\n\t}\n\tvar s1 = arguments[1].evaluate(c).stringValue();\n\tvar s2 = arguments[2].evaluate(c).stringValue();\n\treturn new XBoolean(s1.indexOf(s2) != -1);\n};\n\nFunctions.substringBefore = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 3) {\n\t\tthrow new Error("Function substring-before expects (string, string)");\n\t}\n\tvar s1 = arguments[1].evaluate(c).stringValue();\n\tvar s2 = arguments[2].evaluate(c).stringValue();\n\treturn new XString(s1.substring(0, s1.indexOf(s2)));\n};\n\nFunctions.substringAfter = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 3) {\n\t\tthrow new Error("Function substring-after expects (string, string)");\n\t}\n\tvar s1 = arguments[1].evaluate(c).stringValue();\n\tvar s2 = arguments[2].evaluate(c).stringValue();\n\tif (s2.length == 0) {\n\t\treturn new XString(s1);\n\t}\n\tvar i = s1.indexOf(s2);\n\tif (i == -1) {\n\t\treturn new XString("");\n\t}\n\treturn new XString(s1.substring(s1.indexOf(s2) + 1));\n};\n\nFunctions.substring = function() {\n\tvar c = arguments[0];\n\tif (!(arguments.length == 3 || arguments.length == 4)) {\n\t\tthrow new Error("Function substring expects (string, number, number?)");\n\t}\n\tvar s = arguments[1].evaluate(c).stringValue();\n\tvar n1 = Math.round(arguments[2].evaluate(c).numberValue()) - 1;\n\tvar n2 = arguments.length == 4 ? n1 + Math.round(arguments[3].evaluate(c).numberValue()) : undefined;\n\treturn new XString(s.substring(n1, n2));\n};\n\nFunctions.stringLength = function() {\n\tvar c = arguments[0];\n\tvar s;\n\tif (arguments.length == 1) {\n\t\ts = XNodeSet.prototype.stringForNode(c.contextNode);\n\t} else if (arguments.length == 2) {\n\t\ts = arguments[1].evaluate(c).stringValue();\n\t} else {\n\t\tthrow new Error("Function string-length expects (string?)");\n\t}\n\treturn new XNumber(s.length);\n};\n\nFunctions.normalizeSpace = function() {\n\tvar c = arguments[0];\n\tvar s;\n\tif (arguments.length == 1) {\n\t\ts = XNodeSet.prototype.stringForNode(c.contextNode);\n\t} else if (arguments.length == 2) {\n\t\ts = arguments[1].evaluate(c).stringValue();\n\t} else {\n\t\tthrow new Error("Function normalize-space expects (string?)");\n\t}\n\tvar i = 0;\n\tvar j = s.length - 1;\n\twhile (Utilities.isSpace(s.charCodeAt(j))) {\n\t\tj--;\n\t}\n\tvar t = "";\n\twhile (i <= j && Utilities.isSpace(s.charCodeAt(i))) {\n\t\ti++;\n\t}\n\twhile (i <= j) {\n\t\tif (Utilities.isSpace(s.charCodeAt(i))) {\n\t\t\tt += " ";\n\t\t\twhile (i <= j && Utilities.isSpace(s.charCodeAt(i))) {\n\t\t\t\ti++;\n\t\t\t}\n\t\t} else {\n\t\t\tt += s.charAt(i);\n\t\t\ti++;\n\t\t}\n\t}\n\treturn new XString(t);\n};\n\nFunctions.translate = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 4) {\n\t\tthrow new Error("Function translate expects (string, string, string)");\n\t}\n\tvar s1 = arguments[1].evaluate(c).stringValue();\n\tvar s2 = arguments[2].evaluate(c).stringValue();\n\tvar s3 = arguments[3].evaluate(c).stringValue();\n\tvar map = [];\n\tfor (var i = 0; i < s2.length; i++) {\n\t\tvar j = s2.charCodeAt(i);\n\t\tif (map[j] == undefined) {\n\t\t\tvar k = i > s3.length ? "" : s3.charAt(i);\n\t\t\tmap[j] = k;\n\t\t}\n\t}\n\tvar t = "";\n\tfor (var i = 0; i < s1.length; i++) {\n\t\tvar c = s1.charCodeAt(i);\n\t\tvar r = map[c];\n\t\tif (r == undefined) {\n\t\t\tt += s1.charAt(i);\n\t\t} else {\n\t\t\tt += r;\n\t\t}\n\t}\n\treturn new XString(t);\n};\n\nFunctions.boolean_ = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 2) {\n\t\tthrow new Error("Function boolean expects (object)");\n\t}\n\treturn arguments[1].evaluate(c).bool();\n};\n\nFunctions.not = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 2) {\n\t\tthrow new Error("Function not expects (object)");\n\t}\n\treturn arguments[1].evaluate(c).bool().not();\n};\n\nFunctions.true_ = function() {\n\tif (arguments.length != 1) {\n\t\tthrow new Error("Function true expects ()");\n' , builder.string += '\t}\n\treturn new XBoolean(true);\n};\n\nFunctions.false_ = function() {\n\tif (arguments.length != 1) {\n\t\tthrow new Error("Function false expects ()");\n\t}\n\treturn new XBoolean(false);\n};\n\nFunctions.lang = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 2) {\n\t\tthrow new Error("Function lang expects (string)");\n\t}\n\tvar lang;\n\tfor (var n = c.contextNode; n != null && n.nodeType != 9 /*Node.DOCUMENT_NODE*/; n = n.parentNode) {\n\t\tvar a = n.getAttributeNS(XPath.XML_NAMESPACE_URI, "lang");\n\t\tif (a != null) {\n\t\t\tlang = String(a);\n\t\t\tbreak;\n\t\t}\n\t}\n\tif (lang == null) {\n\t\treturn new XBoolean(false);\n\t}\n\tvar s = arguments[1].evaluate(c).stringValue();\n\treturn new XBoolean(lang.substring(0, s.length) == s\n\t\t\t\t&& (lang.length == s.length || lang.charAt(s.length) == \'-\'));\n};\n\nFunctions.number = function() {\n\tvar c = arguments[0];\n\tif (!(arguments.length == 1 || arguments.length == 2)) {\n\t\tthrow new Error("Function number expects (object?)");\n\t}\n\tif (arguments.length == 1) {\n\t\treturn new XNumber(XNodeSet.prototype.stringForNode(c.contextNode));\n\t}\n\treturn arguments[1].evaluate(c).number();\n};\n\nFunctions.sum = function() {\n\tvar c = arguments[0];\n\tvar ns;\n\tif (arguments.length != 2 || !Utilities.instance_of((ns = arguments[1].evaluate(c)), XNodeSet)) {\n\t\tthrow new Error("Function sum expects (node-set)");\n\t}\n\tns = ns.toArray();\n\tvar n = 0;\n\tfor (var i = 0; i < ns.length; i++) {\n\t\tn += new XNumber(XNodeSet.prototype.stringForNode(ns[i])).numberValue();\n\t}\n\treturn new XNumber(n);\n};\n\nFunctions.floor = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 2) {\n\t\tthrow new Error("Function floor expects (number)");\n\t}\n\treturn new XNumber(Math.floor(arguments[1].evaluate(c).numberValue()));\n};\n\nFunctions.ceiling = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 2) {\n\t\tthrow new Error("Function ceiling expects (number)");\n\t}\n\treturn new XNumber(Math.ceil(arguments[1].evaluate(c).numberValue()));\n};\n\nFunctions.round = function() {\n\tvar c = arguments[0];\n\tif (arguments.length != 2) {\n\t\tthrow new Error("Function round expects (number)");\n\t}\n\treturn new XNumber(Math.round(arguments[1].evaluate(c).numberValue()));\n};\n\n// Utilities /////////////////////////////////////////////////////////////////\n\nUtilities = new Object();\n\nUtilities.splitQName = function(qn) {\n\tvar i = qn.indexOf(":");\n\tif (i == -1) {\n\t\treturn [ null, qn ];\n\t}\n\treturn [ qn.substring(0, i), qn.substring(i + 1) ];\n};\n\nUtilities.resolveQName = function(qn, nr, n, useDefault) {\n\tvar parts = Utilities.splitQName(qn);\n\tif (parts[0] != null) {\n\t\tparts[0] = nr.getNamespace(parts[0], n);\n\t} else {\n\t\tif (useDefault) {\n\t\t\tparts[0] = nr.getNamespace("", n);\n\t\t\tif (parts[0] == null) {\n\t\t\t\tparts[0] = "";\n\t\t\t}\n\t\t} else {\n\t\t\tparts[0] = "";\n\t\t}\n\t}\n\treturn parts;\n};\n\nUtilities.isSpace = function(c) {\n\treturn c == 0x9 || c == 0xd || c == 0xa || c == 0x20;\n};\n\nUtilities.isLetter = function(c) {\n\treturn c >= 0x0041 && c <= 0x005A ||\n\t\tc >= 0x0061 && c <= 0x007A ||\n\t\tc >= 0x00C0 && c <= 0x00D6 ||\n\t\tc >= 0x00D8 && c <= 0x00F6 ||\n\t\tc >= 0x00F8 && c <= 0x00FF ||\n\t\tc >= 0x0100 && c <= 0x0131 ||\n\t\tc >= 0x0134 && c <= 0x013E ||\n\t\tc >= 0x0141 && c <= 0x0148 ||\n\t\tc >= 0x014A && c <= 0x017E ||\n\t\tc >= 0x0180 && c <= 0x01C3 ||\n\t\tc >= 0x01CD && c <= 0x01F0 ||\n\t\tc >= 0x01F4 && c <= 0x01F5 ||\n\t\tc >= 0x01FA && c <= 0x0217 ||\n\t\tc >= 0x0250 && c <= 0x02A8 ||\n\t\tc >= 0x02BB && c <= 0x02C1 ||\n\t\tc == 0x0386 ||\n\t\tc >= 0x0388 && c <= 0x038A ||\n\t\tc == 0x038C ||\n\t\tc >= 0x038E && c <= 0x03A1 ||\n\t\tc >= 0x03A3 && c <= 0x03CE ||\n\t\tc >= 0x03D0 && c <= 0x03D6 ||\n\t\tc == 0x03DA ||\n\t\tc == 0x03DC ||\n\t\tc == 0x03DE ||\n\t\tc == 0x03E0 ||\n\t\tc >= 0x03E2 && c <= 0x03F3 ||\n\t\tc >= 0x0401 && c <= 0x040C ||\n\t\tc >= 0x040E && c <= 0x044F ||\n\t\tc >= 0x0451 && c <= 0x045C ||\n\t\tc >= 0x045E && c <= 0x0481 ||\n\t\tc >= 0x0490 && c <= 0x04C4 ||\n\t\tc >= 0x04C7 && c <= 0x04C8 ||\n\t\tc >= 0x04CB && c <= 0x04CC ||\n\t\tc >= 0x04D0 && c <= 0x04EB ||\n\t\tc >= 0x04EE && c <= 0x04F5 ||\n\t\tc >= 0x04F8 && c <= 0x04F9 ||\n\t\tc >= 0x0531 && c <= 0x0556 ||\n\t\tc == 0x0559 ||\n\t\tc >= 0x0561 && c <= 0x0586 ||\n\t\tc >= 0x05D0 && c <= 0x05EA ||\n\t\tc >= 0x05F0 && c <= 0x05F2 ||\n\t\tc >= 0x0621 && c <= 0x063A ||\n\t\tc >= 0x0641 && c <= 0x064A ||\n\t\tc >= 0x0671 && c <= 0x06B7 ||\n\t\tc >= 0x06BA && c <= 0x06BE ||\n\t\tc >= 0x06C0 && c <= 0x06CE ||\n\t\tc >= 0x06D0 && c <= 0x06D3 ||\n\t\tc == 0x06D5 ||\n\t\tc >= 0x06E5 && c <= 0x06E6 ||\n\t\tc >= 0x0905 && c <= 0x0939 ||\n\t\tc == 0x093D ||\n\t\tc >= 0x0958 && c <= 0x0961 ||\n\t\tc >= 0x0985 && c <= 0x098C ||\n\t\tc >= 0x098F && c <= 0x0990 ||\n\t\tc >= 0x0993 && c <= 0x09A8 ||\n\t\tc >= 0x09AA && c <= 0x09B0 ||\n\t\tc == 0x09B2 ||\n\t\tc >= 0x09B6 && c <= 0x09B9 ||\n\t\tc >= 0x09DC && c <= 0x09DD ||\n\t\tc >= 0x09DF && c <= 0x09E1 ||\n\t\tc >= 0x09F0 && c <= 0x09F1 ||\n\t\tc >= 0x0A05 && c <= 0x0A0A ||\n\t\tc >= 0x0A0F && c <= 0x0A10 ||\n\t\tc >= 0x0A13 && c <= 0x0A28 ||\n\t\tc >= 0x0A2A && c <= 0x0A30 ||\n\t\tc >= 0x0A32 && c <= 0x0A33 ||\n\t\tc >= 0x0A35 && c <= 0x0A36 ||\n\t\tc >= 0x0A38 && c <= 0x0A39 ||\n\t\tc >= 0x0A59 && c <= 0x0A5C ||\n\t\tc == 0x0A5E ||\n\t\tc >= 0x0A72 && c <= 0x0A74 ||\n\t\tc >= 0x0A85 && c <= 0x0A8B ||\n\t\tc == 0x0A8D ||\n\t\tc >= 0x0A8F && c <= 0x0A91 ||\n\t\tc >= 0x0A93 && c <= 0x0AA8 ||\n\t\tc >= 0x0AAA && c <= 0x0AB0 ||\n\t\tc >= 0x0AB2 && c <= 0x0AB3 ||\n\t\tc >= 0x0AB5 && c <= 0x0AB9 ||\n\t\tc == 0x0ABD ||\n\t\tc == 0x0AE0 ||\n\t\tc >= 0x0B05 && c <= 0x0B0C ||\n\t\tc >= 0x0B0F && c <= 0x0B10 ||\n\t\tc >= 0x0B13 && c <= 0x0B28 ||\n\t\tc >= 0x0B2A && c <= 0x0B30 ||\n\t\tc >= 0x0B32 && c <= 0x0B33 ||\n\t\tc >= 0x0B36 && c <= 0x0B39 ||\n\t\tc == 0x0B3D ||\n\t\tc >= 0x0B5C && c <= 0x0B5D ||\n\t\tc >= 0x0B5F && c <= 0x0B61 ||\n\t\tc >= 0x0B85 && c <= 0x0B8A ||\n\t\tc >= 0x0B8E && c <= 0x0B90 ||\n\t\tc >= 0x0B92 && c <= 0x0B95 ||\n\t\tc >= 0x0B99 && c <= 0x0B9A ||\n\t\tc == 0x0B9C ||\n\t\tc >= 0x0B9E && c <= 0x0B9F ||\n\t\tc >= 0x0BA3 && c <= 0x0BA4 ||\n\t\tc >= 0x0BA8 && c <= 0x0BAA ||\n\t\tc >= 0x0BAE && c <= 0x0BB5 ||\n\t\tc >= 0x0BB7 && c <= 0x0BB9 ||\n\t\tc >= 0x0C05 && c <= 0x0C0C ||\n\t\tc >= 0x0C0E && c <= 0x0C10 ||\n\t\tc >= 0x0C12 && c <= 0x0C28 ||\n\t\tc >= 0x0C2A && c <= 0x0C33 ||\n\t\tc >= 0x0C35 && c <= 0x0C39 ||\n\t\tc >= 0x0C60 && c <= 0x0C61 ||\n\t\tc >= 0x0C85 && c <= 0x0C8C ||\n\t\tc >= 0x0C8E && c <= 0x0C90 ||\n\t\tc >= 0x0C92 && c <= 0x0CA8 ||\n\t\tc >= 0x0CAA && c <= 0x0CB3 ||\n\t\tc >= 0x0CB5 && c <= 0x0CB9 ||\n\t\tc == 0x0CDE ||\n\t\tc >= 0x0CE0 && c <= 0x0CE1 ||\n\t\tc >= 0x0D05 && c <= 0x0D0C ||\n\t\tc >= 0x0D0E && c <= 0x0D10 ||\n\t\tc >= 0x0D12 && c <= 0x0D28 ||\n\t\tc >= 0x0D2A && c <= 0x0D39 ||\n\t\tc >= 0x0D60 && c <= 0x0D61 ||\n\t\tc >= 0x0E01 && c <= 0x0E2E ||\n\t\tc == 0x0E30 ||\n\t\tc >= 0x0E32 && c <= 0x0E33 ||\n\t\tc >= 0x0E40 && c <= 0x0E45 ||\n\t\tc >= 0x0E81 && c <= 0x0E82 ||\n\t\tc == 0x0E84 ||\n\t\tc >= 0x0E87 && c <= 0x0E88 ||\n\t\tc == 0x0E8A ||\n\t\tc == 0x0E8D ||\n\t\tc >= 0x0E94 && c <= 0x0E97 ||\n\t\tc >= 0x0E99 && c <= 0x0E9F ||\n\t\tc >= 0x0EA1 && c <= 0x0EA3 ||\n\t\tc == 0x0EA5 ||\n\t\tc == 0x0EA7 ||\n\t\tc >= 0x0EAA && c <= 0x0EAB ||\n\t\tc >= 0x0EAD && c <= 0x0EAE ||\n\t\tc == 0x0EB0 ||\n\t\tc >= 0x0EB2 && c <= 0x0EB3 ||\n\t\tc == 0x0EBD ||\n\t\tc >= 0x0EC0 && c <= 0x0EC4 ||\n\t\tc >= 0x0F40 && c <= 0x0F47 ||\n\t\tc >= 0x0F49 && c <= 0x0F69 ||\n\t\tc >= 0x10A0 && c <= 0x10C5 ||\n\t\tc >= 0x10D0 && c <= 0x10F6 ||\n\t\tc == 0x1100 ||\n\t\tc >= 0x1102 && c <= 0x1103 ||\n\t\tc >= 0x1105 && c <= 0x1107 ||\n\t\tc == 0x1109 ||\n\t\tc >= 0x110B && c <= 0x110C ||\n\t\tc >= 0x110E && c <= 0x1112 ||\n\t\tc == 0x113C ||\n\t\tc == 0x113E ||\n\t\tc == 0x1140 ||\n\t\tc == 0x114C ||\n\t\tc == 0x114E ||\n\t\tc == 0x1150 ||\n\t\tc >= 0x1154 && c <= 0x1155 ||\n\t\tc == 0x1159 ||\n\t\tc >= 0x115F && c <= 0x1161 ||\n\t\tc == 0x1163 ||\n\t\tc == 0x1165 ||\n\t\tc == 0x1167 ||\n\t\tc == 0x1169 ||\n\t\tc >= 0x116D && c <= 0x116E ||\n\t\tc >= 0x1172 && c <= 0x1173 ||\n\t\tc == 0x1175 ||\n\t\tc == 0x119E ||\n\t\tc == 0x11A8 ||\n\t\tc == 0x11AB ||\n\t\tc >= 0x11AE && c <= 0x11AF ||\n\t\tc >= 0x11B7 && c <= 0x11B8 ||\n\t\tc == 0x11BA ||\n\t\tc >= 0x11BC && c <= 0x11C2 ||\n\t\tc == 0x11EB ||\n\t\tc == 0x11F0 ||\n\t\tc == 0x11F9 ||\n\t\tc >= 0x1E00 && c <= 0x1E9B ||\n\t\tc >= 0x1EA0 && c <= 0x1EF9 ||\n\t\tc >= 0x1F00 && c <= 0x1F15 ||\n\t\tc >= 0x1F18 && c <= 0x1F1D ||\n\t\tc >= 0x1F20 && c <= 0x1F45 ||\n\t\tc >= 0x1F48 && c <= 0x1F4D ||\n\t\tc >= 0x1F50 && c <= 0x1F57 ||\n\t\tc == 0x1F59 ||\n\t\tc == 0x1F5B ||\n\t\tc == 0x1F5D ||\n\t\tc >= 0x1F5F && c <= 0x1F7D ||\n\t\tc >= 0x1F80 && c <= 0x1FB4 ||\n\t\tc >= 0x1FB6 && c <= 0x1FBC ||\n\t\tc == 0x1FBE ||\n\t\tc >= 0x1FC2 && c <= 0x1FC4 ||\n\t\tc >= 0x1FC6 && c <= 0x1FCC ||\n\t\tc >= 0x1FD0 && c <= 0x1FD3 ||\n\t\tc >= 0x1FD6 && c <= 0x1FDB ||\n\t\tc >= 0x1FE0 && c <= 0x1FEC ||\n\t\tc >= 0x1FF2 && c <= 0x1FF4 ||\n\t\tc >= 0x1FF6 && c <= 0x1FFC ||\n\t\tc == 0x2126 ||\n\t\tc >= 0x212A && c <= 0x212B ||\n\t\tc == 0x212E ||\n\t\tc >= 0x2180 && c <= 0x2182 ||\n\t\tc >= 0x3041 && c <= 0x3094 ||\n\t\tc >= 0x30A1 && c <= 0x30FA ||\n\t\tc >= 0x3105 && c <= 0x312C ||\n\t\tc >= 0xAC00 && c <= 0xD7A3 ||\n\t\tc >= 0x4E00 && c <= 0x9FA5 ||\n\t\tc == 0x3007 ||\n\t\tc >= 0x3021 && c <= 0x3029;\n};\n\nUtilities.isNCNameChar = function(c) {\n\treturn c >= 0x0030 && c <= 0x0039 \n\t\t|| c >= 0x0660 && c <= 0x0669 \n\t\t|| c >= 0x06F0 && c <= 0x06F9 \n\t\t|| c >= 0x0966 && c <= 0x096F \n\t\t|| c >= 0x09E6 && c <= 0x09EF \n\t\t|| c >= 0x0A66 && c <= 0x0A6F \n\t\t|| c >= 0x0AE6 && c <= 0x0AEF \n\t\t|| c >= 0x0B66 && c <= 0x0B6F \n\t\t|| c >= 0x0BE7 && c <= 0x0BEF \n\t\t|| c >= 0x0C66 && c <= 0x0C6F \n\t\t|| c >= 0x0CE6 && c <= 0x0CEF \n\t\t|| c >= 0x0D66 && c <= 0x0D6F \n\t\t|| c >= 0x0E50 && c <= 0x0E59 \n\t\t|| c >= 0x0ED0 && c <= 0x0ED9 \n\t\t|| c >= 0x0F20 && c <= 0x0F29\n\t\t|| c == 0x002E\n\t\t|| c == 0x002D\n\t\t|| c == 0x005F\n\t\t|| Utilities.isLetter(c)\n\t\t|| c >= 0x0300 && c <= 0x0345 \n\t\t|| c >= 0x0360 && c <= 0x0361 \n\t\t|| c >= 0x0483 && c <= 0x0486 \n\t\t|| c >= 0x0591 && c <= 0x05A1 \n\t\t|| c >= 0x05A3 && c <= 0x05B9 \n\t\t|| c >= 0x05BB && c <= 0x05BD \n\t\t|| c == 0x05BF \n\t\t|| c >= 0x05C1 && c <= 0x05C2 \n\t\t|| c == 0x05C4 \n\t\t|| c >= 0x064B && c <= 0x0652 \n\t\t|| c == 0x0670 \n\t\t|| c >= 0x06D6 && c <= 0x06DC \n\t\t|| c >= 0x06DD && c <= 0x06DF \n\t\t|| c >= 0x06E0 && c <= 0x06E4 \n\t\t|| c >= 0x06E7 && c <= 0x06E8 \n\t\t|| c >= 0x06EA && c <= 0x06ED \n\t\t|| c >= 0x0901 && c <= 0x0903 \n\t\t|| c == 0x093C \n\t\t|| c >= 0x093E && c <= 0x094C \n\t\t|| c == 0x094D \n\t\t|| c >= 0x0951 && c <= 0x0954 \n\t\t|| c >= 0x0962 && c <= 0x0963 \n\t\t|| c >= 0x0981 && c <= 0x0983 \n\t\t|| c == 0x09BC \n\t\t|| c == 0x09BE \n\t\t|| c == 0x09BF \n\t\t|| c >= 0x09C0 && c <= 0x09C4 \n\t\t|| c >= 0x09C7 && c <= 0x09C8 \n\t\t|| c >= 0x09CB && c <= 0x09CD \n\t\t|| c == 0x09D7 \n\t\t|| c >= 0x09E2 && c <= 0x09E3 \n\t\t|| c == 0x0A02 \n\t\t|| c == 0x0A3C \n\t\t|| c == 0x0A3E \n\t\t|| c == 0x0A3F \n\t\t|| c >= 0x0A40 && c <= 0x0A42 \n\t\t|| c >= 0x0A47 && c <= 0x0A48 \n\t\t|| c >= 0x0A4B && c <= 0x0A4D \n\t\t|| c >= 0x0A70 && c <= 0x0A71 \n\t\t|| c >= 0x0A81 && c <= 0x0A83 \n\t\t|| c == 0x0ABC \n\t\t|| c >= 0x0ABE && c <= 0x0AC5 \n\t\t|| c >= 0x0AC7 && c <= 0x0AC9 \n\t\t|| c >= 0x0ACB && c <= 0x0ACD \n\t\t|| c >= 0x0B01 && c <= 0x0B03 \n\t\t|| c == 0x0B3C \n\t\t|| c >= 0x0B3E && c <= 0x0B43 \n\t\t|| c >= 0x0B47 && c <= 0x0B48 \n\t\t|| c >= 0x0B4B && c <= 0x0B4D \n\t\t|| c >= 0x0B56 && c <= 0x0B57 \n\t\t|| c >= 0x0B82 && c <= 0x0B83 \n\t\t|| c >= 0x0BBE && c <= 0x0BC2 \n\t\t|| c >= 0x0BC6 && c <= 0x0BC8 \n\t\t|| c >= 0x0BCA && c <= 0x0BCD \n\t\t|| c == 0x0BD7 \n\t\t|| c >= 0x0C01 && c <= 0x0C03 \n\t\t|| c >= 0x0C3E && c <= 0x0C44 \n\t\t|| c >= 0x0C46 && c <= 0x0C48 \n\t\t|| c >= 0x0C4A && c <= 0x0C4D \n\t\t|| c >= 0x0C55 && c <= 0x0C56 \n\t\t|| c >= 0x0C82 && c <= 0x0C83 \n\t\t|| c >= 0x0CBE && c <= 0x0CC4 \n\t\t|| c >= 0x0CC6 && c <= 0x0CC8 \n\t\t|| c >= 0x0CCA && c <= 0x0CCD \n\t\t|| c >= 0x0CD5 && c <= 0x0CD6 \n\t\t|| c >= 0x0D02 && c <= 0x0D03 \n\t\t|| c >= 0x0D3E && c <= 0x0D43 \n\t\t|| c >= 0x0D46 && c <= 0x0D48 \n\t\t|| c >= 0x0D4A && c <= 0x0D4D \n\t\t|| c == 0x0D57 \n\t\t|| c == 0x0E31 \n\t\t|| c >= 0x0E34 && c <= 0x0E3A \n\t\t|| c >= 0x0E47 && c <= 0x0E4E \n\t\t|| c == 0x0EB1 \n\t\t|| c >= 0x0EB4 && c <= 0x0EB9 \n\t\t|| c >= 0x0EBB && c <= 0x0EBC \n\t\t|| c >= 0x0EC8 && c <= 0x0ECD \n\t\t|| c >= 0x0F18 && c <= 0x0F19 \n\t\t|| c == 0x0F35 \n\t\t|| c == 0x0F37 \n\t\t|| c == 0x0F39 \n\t\t|| c == 0x0F3E \n\t\t|| c == 0x0F3F \n\t\t|| c >= 0x0F71 && c <= 0x0F84 \n\t\t|| c >= 0x0F86 && c <= 0x0F8B \n\t\t|| c >= 0x0F90 && c <= 0x0F95 \n\t\t|| c == 0x0F97 \n\t\t|| c >= 0x0F99 && c <= 0x0FAD \n\t\t|| c >= 0x0FB1 && c <= 0x0FB7 \n\t\t|| c == 0x0FB9 \n\t\t|| c >= 0x20D0 && c <= 0x20DC \n\t\t|| c == 0x20E1 \n\t\t|| c >= 0x302A && c <= 0x302F \n\t\t|| c == 0x3099 \n\t\t|| c == 0x309A\n\t\t|| c == 0x00B7 \n\t\t|| c == 0x02D0 \n\t\t|| c == 0x02D1 \n\t\t|| c == 0x0387 \n\t\t|| c == 0x0640 \n\t\t|| c == 0x0E46 \n\t\t|| c == 0x0EC6 \n\t\t|| c == 0x3005 \n\t\t|| c >= 0x3031 && c <= 0x3035 \n\t\t|| c >= 0x309D && c <= 0x309E \n\t\t|| c >= 0x30FC && c <= 0x30FE;\n};\n\nUtilities.coalesceText = function(n) {\n\tfor (var m = n.firstChild; m != null; m = m.nextSibling) {\n\t\tif (m.nodeType == 3 /*Node.TEXT_NODE*/ || m.nodeType == 4 /*Node.CDATA_SECTION_NODE*/) {\n\t\t\tvar s = m.nodeValue;\n\t\t\tvar first = m;\n\t\t\tm = m.nextSibling;\n\t\t\twhile (m != null && (m.nodeType == 3 /*Node.TEXT_NODE*/ || m.nodeType == 4 /*Node.CDATA_SECTION_NODE*/)) {\n\t\t\t\ts += m.nodeValue;\n\t\t\t\tvar del = m;\n\t\t\t\tm = m.nextSibling;\n\t\t\t\tdel.parentNode.removeChild(del);\n\t\t\t}\n\t\t\tif (first.nodeType == 4 /*Node.CDATA_SECTION_NODE*/) {\n\t\t\t\tvar p = first.parentNode;\n\t\t\t\tif (first.nextSibling == null) {\n\t\t\t\t\tp.removeChild(first);\n\t\t\t\t\tp.appendChild(p.ownerDocument.createTextNode(s));\n\t\t\t\t} else {\n\t\t\t\t\tvar next = first.nextSibling;\n\t\t\t\t\tp.removeChild(first);\n\t\t\t\t\tp.insertBefore(p.ownerDocument.createTextNode(s), next);\n\t\t\t\t}\n\t\t\t} else {\n\t\t\t\tfirst.nodeValue = s;\n\t\t\t}\n\t\t\tif (m == null) {\n\t\t\t\tbreak;\n\t\t\t}\n\t\t} else if (m.nodeType == 1 /*Node.ELEMENT_NODE*/) {\n\t\t\tUtilities.coalesceText(m);\n\t\t}\n\t}\n};\n\nUtilities.instance_of = function(o, c) {\n\twhile (o != null) {\n\t\tif (o.constructor === c) {\n\t\t\treturn true;\n\t\t}\n\t\tif (o === Object) {\n\t\t\treturn false;\n\t\t}\n\t\to = o.constructor.superclass;\n\t}\n\treturn false;\n};\n\nUtilities.getElementById = function(n, id) {\n\t// Note that this does not check the DTD to check for actual\n\t// attributes of type ID, so this may be a bit wrong.\n\tif (n.nodeType == 1 /*Node.ELEMENT_NODE*/) {\n\t\tif (n.getAttribute("id") == id\n\t\t\t\t|| n.getAttributeNS(null, "id") == id) {\n\t\t\treturn n;\n\t\t}\n\t}\n\tfor (var m = n.firstChild; m != null; m = m.nextSibling) {\n\t\tvar res = Utilities.getElementById(m, id);\n\t\tif (res != null) {\n\t\t\treturn res;\n\t\t}\n\t}\n\treturn null;\n};\n\n// XPathException ////////////////////////////////////////////////////////////\n\nXPathException.prototype = {};\nXPathException.prototype.constructor = XPathException;\nXPathException.superclass = Object.prototype;\n\nfunction XPathException(c, e) {\n\tthis.code = c;\n\tthis.exception = e;\n}\n\nXPathException.prototype.toString = function() {\n\tvar msg = this.exception ? ": " + this.exception.toString() : "";\n\tswitch (this.code) {\n\t\tcase XPathException.INVALID_EXPRESSION_ERR:\n\t\t\treturn "Invalid expression" + msg;\n\t\tcase XPathException.TYPE_ERR:\n\t\t\treturn "Type error" + msg;\n\t}\n};\n\nXPathException.INVALID_EXPRESSION_ERR = 51;\nXPathException.TYPE_ERR = 52;\n\n// XPathExpression ///////////////////////////////////////////////////////////\n\nXPathExpression.prototype = {};\nXPathExpression.prototype.constructor = XPathExpression;\nXPathExpression.superclass = Object.prototype;\n\nfunction XPathExpression(e, r, p) {\n\tthis.xpath = p.parse(e);\n\tthis.context = new XPathContext();\n\tthis.context.namespaceResolver = new XPathNSResolverWrapper(r);\n}\n\nXPathExpression.prototype.evaluate = function(n, t, res) {\n\tthis.context.expressionContextNode = n;\n\tvar result = this.xpath.evaluate(this.context);\n\treturn new XPathResult(result, t);\n}\n\n// XPathNSResolverWrapper ////////////////////////////////////////////////////\n\nXPathNSResolverWrapper.prototype = {};\nXPathNSResolverWrapper.prototype.constructor = XPathNSResolverWrapper;\nXPathNSResolverWrapper.superclass = Object.prototype;\n\nfunction XPathNSResolverWrapper(r) {\n\tthis.xpathNSResolver = r;\n}\n\nXPathNSResolverWrapper.prototype.getNamespace = function(prefix, n) {\n    if (this.xpathNSResolver == null) {\n        return null;\n    }\n\treturn this.xpathNSResolver.getNamespace(prefix, n);\n};\n\n// NodeXPathNSResolver ///////////////////////////////////////////////////////\n\nNodeXPathNSResolver.prototype = {};\nNodeXPathNSResolver.prototype.constructor = NodeXPathNSResolver;\nNodeXPathNSResolver.superclass = Object.prototype;\n\nfunction NodeXPathNSResolver(n) {\n\tthis.node = n;\n\tthis.namespaceResolver = new NamespaceResolver();\n}\n\nNodeXPathNSResolver.prototype.lookupNamespaceURI = function(prefix) {\n\treturn this.namespaceResolver.getNamespace(prefix, this.node);\n};\n\n// XPathResult ///////////////////////////////////////////////////////////////\n\nXPathResult.prototype = {};\nXPathResult.prototype.constructor = XPathResult;\nXPathResult.superclass = Object.prototype;\n\nfunction XPathResult(v, t) {\n\tif (t == XPathResult.ANY_TYPE) {\n\t\tif (v.constructor ' , builder.string += '=== XString) {\n\t\t\tt = XPathResult.STRING_TYPE;\n\t\t} else if (v.constructor === XNumber) {\n\t\t\tt = XPathResult.NUMBER_TYPE;\n\t\t} else if (v.constructor === XBoolean) {\n\t\t\tt = XPathResult.BOOLEAN_TYPE;\n\t\t} else if (v.constructor === XNodeSet) {\n\t\t\tt = XPathResult.UNORDERED_NODE_ITERATOR_TYPE;\n\t\t}\n\t}\n\tthis.resultType = t;\n\tswitch (t) {\n\t\tcase XPathResult.NUMBER_TYPE:\n\t\t\tthis.numberValue = v.numberValue();\n\t\t\treturn;\n\t\tcase XPathResult.STRING_TYPE:\n\t\t\tthis.stringValue = v.stringValue();\n\t\t\treturn;\n\t\tcase XPathResult.BOOLEAN_TYPE:\n\t\t\tthis.booleanValue = v.booleanValue();\n\t\t\treturn;\n\t\tcase XPathResult.ANY_UNORDERED_NODE_TYPE:\n\t\tcase XPathResult.FIRST_ORDERED_NODE_TYPE:\n\t\t\tif (v.constructor === XNodeSet) {\n\t\t\t\tthis.singleNodeValue = v.first();\n\t\t\t\treturn;\n\t\t\t}\n\t\t\tbreak;\n\t\tcase XPathResult.UNORDERED_NODE_ITERATOR_TYPE:\n\t\tcase XPathResult.ORDERED_NODE_ITERATOR_TYPE:\n\t\t\tif (v.constructor === XNodeSet) {\n\t\t\t\tthis.invalidIteratorState = false;\n\t\t\t\tthis.nodes = v.toArray();\n\t\t\t\tthis.iteratorIndex = 0;\n\t\t\t\treturn;\n\t\t\t}\n\t\t\tbreak;\n\t\tcase XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE:\n\t\tcase XPathResult.ORDERED_NODE_SNAPSHOT_TYPE:\n\t\t\tif (v.constructor === XNodeSet) {\n\t\t\t\tthis.nodes = v.toArray();\n\t\t\t\tthis.snapshotLength = this.nodes.length;\n\t\t\t\treturn;\n\t\t\t}\n\t\t\tbreak;\n\t}\n\tthrow new XPathException(XPathException.TYPE_ERR);\n};\n\nXPathResult.prototype.iterateNext = function() {\n\tif (this.resultType != XPathResult.UNORDERED_NODE_ITERATOR_TYPE\n\t\t\t&& this.resultType != XPathResult.ORDERED_NODE_ITERATOR_TYPE) {\n\t\tthrow new XPathException(XPathException.TYPE_ERR);\n\t}\n\treturn this.nodes[this.iteratorIndex++];\n};\n\nXPathResult.prototype.snapshotItem = function(i) {\n\tif (this.resultType != XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE\n\t\t\t&& this.resultType != XPathResult.ORDERED_NODE_SNAPSHOT_TYPE) {\n\t\tthrow new XPathException(XPathException.TYPE_ERR);\n\t}\n\treturn this.nodes[i];\n};\n\nXPathResult.ANY_TYPE = 0;\nXPathResult.NUMBER_TYPE = 1;\nXPathResult.STRING_TYPE = 2;\nXPathResult.BOOLEAN_TYPE = 3;\nXPathResult.UNORDERED_NODE_ITERATOR_TYPE = 4;\nXPathResult.ORDERED_NODE_ITERATOR_TYPE = 5;\nXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE = 6;\nXPathResult.ORDERED_NODE_SNAPSHOT_TYPE = 7;\nXPathResult.ANY_UNORDERED_NODE_TYPE = 8;\nXPathResult.FIRST_ORDERED_NODE_TYPE = 9;\n\n// DOM 3 XPath support ///////////////////////////////////////////////////////\n\nfunction installDOM3XPathSupport(doc, p) {\n\tdoc.createExpression = function(e, r) {\n\t\ttry {\n\t\t\treturn new XPathExpression(e, r, p);\n\t\t} catch (e) {\n\t\t\tthrow new XPathException(XPathException.INVALID_EXPRESSION_ERR, e);\n\t\t}\n\t};\n\tdoc.createNSResolver = function(n) {\n\t\treturn new NodeXPathNSResolver(n);\n\t};\n\tdoc.evaluate = function(e, cn, r, t, res) {\n\t\tif (t < 0 || t > 9) {\n\t\t\tthrow { code: 0, toString: function() { return "Request type not supported"; } };\n\t\t}\n        return doc.createExpression(e, r, p).evaluate(cn, t, res);\n\t};\n};\n\n// ---------------------------------------------------------------------------\n\n// Install DOM 3 XPath support for the current document.\ntry {\n\tvar shouldInstall = true;\n\ttry {\n\t\tif (document.implementation\n\t\t\t\t&& document.implementation.hasFeature\n\t\t\t\t&& document.implementation.hasFeature("XPath", null)) {\n\t\t\tshouldInstall = false;\n\t\t}\n\t} catch (e) {\n\t}\n\tif (shouldInstall) {\n\t\tinstallDOM3XPathSupport(document, new XPathParser());\n\t}\n} catch (e) {\n}\n' , builder.string));
    $appendChild(doc.body, scriptElem);
    $initXPath();
  }
  if (!(typeof SVGPathSeg === 'function')) {
    doc1 = $doc;
    scriptElem1 = $createScriptElement(doc1, $getText());
    $appendChild(doc1.body, scriptElem1);
    doc2 = document;
    scriptElem2 = $createScriptElement(doc2, $getText());
    $appendChild(doc2.body, scriptElem2);
  }
  if (!('getTransformToElement' in SVGSVGElement.prototype)) {
    doc1 = $doc;
    scriptElem1 = $createScriptElement(doc1, '// getTransformToElement() polyfill\n// courtesy of http://jointjs.com/blog/get-transform-to-element-polyfill.html\n\t\n(function() { "use strict";\nSVGElement.prototype.getTransformToElement = SVGElement.prototype.getTransformToElement || function(toElement) {\n    return toElement.getScreenCTM().inverse().multiply(this.getScreenCTM());\n};\n}());');
    $appendChild(doc1.body, scriptElem1);
    doc2 = document;
    scriptElem2 = $createScriptElement(doc2, '// getTransformToElement() polyfill\n// courtesy of http://jointjs.com/blog/get-transform-to-element-polyfill.html\n\t\n(function() { "use strict";\nSVGElement.prototype.getTransformToElement = SVGElement.prototype.getTransformToElement || function(toElement) {\n    return toElement.getScreenCTM().inverse().multiply(this.getScreenCTM());\n};\n}());');
    $appendChild(doc2.body, scriptElem2);
  }
}

defineClass(347, 1, {}, DOMHelperImpl);
_.dispatch_0 = function dispatch_5(event_0, node, elem){
  $dispatch_1(event_0, node);
}
;
_.dispatchCapturedEvent = function dispatchCapturedEvent_0(event_0, elem){
}
;
var eventsInitialized = false;
var Lorg_vectomatic_dom_svg_impl_DOMHelperImpl_2_classLit = createForClass('org.vectomatic.dom.svg.impl', 'DOMHelperImpl', 347);
function $getText(){
  var builder;
  builder = new StringBuilder;
  builder.string += '// SVGPathSeg API polyfill\n// https://github.com/progers/pathseg\n//\n// This is a drop-in replacement for the SVGPathSeg and SVGPathSegList APIs that were removed from\n// SVG2 (https://lists.w3.org/Archives/Public/www-svg/2015Jun/0044.html), including the latest spec\n// changes which were implemented in Firefox 43 and Chrome 46.\n\n(function() { "use strict";\n    if (!("SVGPathSeg" in window)) {\n        // Spec: http://www.w3.org/TR/SVG11/single-page.html#paths-InterfaceSVGPathSeg\n        window.SVGPathSeg = function(type, typeAsLetter, owningPathSegList) {\n            this.pathSegType = type;\n            this.pathSegTypeAsLetter = typeAsLetter;\n            this._owningPathSegList = owningPathSegList;\n        }\n\n        window.SVGPathSeg.prototype.classname = "SVGPathSeg";\n\n        window.SVGPathSeg.PATHSEG_UNKNOWN = 0;\n        window.SVGPathSeg.PATHSEG_CLOSEPATH = 1;\n        window.SVGPathSeg.PATHSEG_MOVETO_ABS = 2;\n        window.SVGPathSeg.PATHSEG_MOVETO_REL = 3;\n        window.SVGPathSeg.PATHSEG_LINETO_ABS = 4;\n        window.SVGPathSeg.PATHSEG_LINETO_REL = 5;\n        window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS = 6;\n        window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL = 7;\n        window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS = 8;\n        window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL = 9;\n        window.SVGPathSeg.PATHSEG_ARC_ABS = 10;\n        window.SVGPathSeg.PATHSEG_ARC_REL = 11;\n        window.SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS = 12;\n        window.SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL = 13;\n        window.SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS = 14;\n        window.SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL = 15;\n        window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS = 16;\n        window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL = 17;\n        window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS = 18;\n        window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL = 19;\n\n        // Notify owning PathSegList on any changes so they can be synchronized back to the path element.\n        window.SVGPathSeg.prototype._segmentChanged = function() {\n            if (this._owningPathSegList)\n                this._owningPathSegList.segmentChanged(this);\n        }\n\n        window.SVGPathSegClosePath = function(owningPathSegList) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_CLOSEPATH, "z", owningPathSegList);\n        }\n        window.SVGPathSegClosePath.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegClosePath.prototype.toString = function() { return "[object SVGPathSegClosePath]"; }\n        window.SVGPathSegClosePath.prototype._asPathString = function() { return this.pathSegTypeAsLetter; }\n        window.SVGPathSegClosePath.prototype.clone = function() { return new window.SVGPathSegClosePath(undefined); }\n\n        window.SVGPathSegMovetoAbs = function(owningPathSegList, x, y) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_MOVETO_ABS, "M", owningPathSegList);\n            this._x = x;\n            this._y = y;\n        }\n        window.SVGPathSegMovetoAbs.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegMovetoAbs.prototype.toString = function() { return "[object SVGPathSegMovetoAbs]"; }\n        window.SVGPathSegMovetoAbs.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x + " " + this._y; }\n        window.SVGPathSegMovetoAbs.prototype.clone = function() { return new window.SVGPathSegMovetoAbs(undefined, this._x, this._y); }\n        Object.defineProperty(window.SVGPathSegMovetoAbs.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegMovetoAbs.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegMovetoRel = function(owningPathSegList, x, y) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_MOVETO_REL, "m", owningPathSegList);\n            this._x = x;\n            this._y = y;\n        }\n        window.SVGPathSegMovetoRel.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegMovetoRel.prototype.toString = function() { return "[object SVGPathSegMovetoRel]"; }\n        window.SVGPathSegMovetoRel.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x + " " + this._y; }\n        window.SVGPathSegMovetoRel.prototype.clone = function() { return new window.SVGPathSegMovetoRel(undefined, this._x, this._y); }\n        Object.defineProperty(window.SVGPathSegMovetoRel.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegMovetoRel.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegLinetoAbs = function(owningPathSegList, x, y) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_LINETO_ABS, "L", owningPathSegList);\n            this._x = x;\n            this._y = y;\n        }\n        window.SVGPathSegLinetoAbs.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegLinetoAbs.prototype.toString = function() { return "[object SVGPathSegLinetoAbs]"; }\n        window.SVGPathSegLinetoAbs.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x + " " + this._y; }\n        window.SVGPathSegLinetoAbs.prototype.clone = function() { return new window.SVGPathSegLinetoAbs(undefined, this._x, this._y); }\n        Object.defineProperty(window.SVGPathSegLinetoAbs.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegLinetoAbs.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegLinetoRel = function(owningPathSegList, x, y) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_LINETO_REL, "l", owningPathSegList);\n            this._x = x;\n            this._y = y;\n        }\n        window.SVGPathSegLinetoRel.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegLinetoRel.prototype.toString = function() { return "[object SVGPathSegLinetoRel]"; }\n        window.SVGPathSegLinetoRel.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x + " " + this._y; }\n        window.SVGPathSegLinetoRel.prototype.clone = function() { return new window.SVGPathSegLinetoRel(undefined, this._x, this._y); }\n        Object.defineProperty(window.SVGPathSegLinetoRel.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegLinetoRel.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegCurvetoCubicAbs = function(owningPathSegList, x, y, x1, y1, x2, y2) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS, "C", owningPathSegList);\n            this._x = x;\n            this._y = y;\n            this._x1 = x1;\n            this._y1 = y1;\n            this._x2 = x2;\n            this._y2 = y2;\n        }\n        window.SVGPathSegCurvetoCubicAbs.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegCurvetoCubicAbs.prototype.toString = function() { return "[object SVGPathSegCurvetoCubicAbs]"; }\n        window.SVGPathSegCurvetoCubicAbs.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x1 + " " + this._y1 + " " + this._x2 + " " + this._y2 + " " + this._x + " " + this._y; }\n        window.SVGPathSegCurvetoCubicAbs.prototype.clone = function() { return new window.SVGPathSegCurvetoCubicAbs(undefined, this._x, this._y, this._x1, this._y1, this._x2, this._y2); }\n        Object.defineProperty(window.SVGPathSegCurvetoCubicAbs.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicAbs.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicAbs.prototype, "x1", { get: function() { return this._x1; }, set: function(x1) { this._x1 = x1; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicAbs.prototype, "y1", { get: function() { return this._y1; }, set: function(y1) { this._y1 = y1; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicAbs.prototype, "x2", { get: function() { return this._x2; }, set: function(x2) { this._x2 = x2; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicAbs.prototype, "y2", { get: function() { return this._y2; }, set: function(y2) { this._y2 = y2; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegCurvetoCubicRel = function(owningPathSegList, x, y, x1, y1, x2, y2) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL, "c", owningPathSegList);\n            this._x = x;\n            this._y = y;\n            this._x1 = x1;\n            this._y1 = y1;\n            this._x2 = x2;\n            this._y2 = y2;\n        }\n        window.SVGPathSegCurvetoCubicRel.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegCurvetoCubicRel.prototype.toString = function() { return "[object SVGPathSegCurvetoCubicRel]"; }\n        window.SVGPathSegCurvetoCubicRel.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x1 + " " + this._y1 + " " + this._x2 + " " + this._y2 + " " + this._x + " " + this._y; }\n        window.SVGPathSegCurvetoCubicRel.prototype.clone = function() { return new window.SVGPathSegCurvetoCubicRel(undefined, this._x, this._y, this._x1, this._y1, this._x2, this._y2); }\n        Object.defineProperty(window.SVGPathSegCurvetoCubicRel.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicRel.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicRel.prototype, "x1", { get: function() { return this._x1; }, set: function(x1) { this._x1 = x1; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicRel.prototype, "y1", { get: function() { return this._y1; }, set: function(y1) { this._y1 = y1; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicRel.prototype, "x2", { get: function() { return this._x2; }, set: function(x2) { this._x2 = x2; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicRel.prototype, "y2", { get: function() { return this._y2; }, set: function(y2) { this._y2 = y2; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegCurvetoQuadraticAbs = function(owningPathSegList, x, y, x1, y1) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS, "Q", owningPathSegList);\n            this._x = x;\n            this._y = y;\n            this._x1 = x1;\n            this._y1 = y1;\n        }\n        window.SVGPathSegCurvetoQuadraticAbs.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegCurvetoQuadraticAbs.prototype.toString = function() { return "[object SVGPathSegCurvetoQuadraticAbs]"; }\n        window.SVGPathSegCurvetoQuadraticAbs.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x1 + " " + this._y1 + " " + this._x + " " + this._y; }\n        window.SVGPathSegCurvetoQuadraticAbs.prototype.clone = function() { return new window.SVGPathSegCurvetoQuadraticAbs(undefined, this._x, this._y, this._x1, this._y1); }\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticAbs.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticAbs.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticAbs.prototype, "x1", { get: function() { return this._x1; }, set: function(x1) { this._x1 = x1; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticAbs.prototype, "y1", { get: function() { return this._y1; }, set: function(y1) { this._y1 = y1; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegCurvetoQuadraticRel = function(owningPathSegList, x, y, x1, y1) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL, "q", owningPathSegList);\n            this._x = x;\n            this._y = y;\n            this._x1 = x1;\n            this._y1 = y1;\n        }\n        window.SVGPathSegCurvetoQuadraticRel.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegCurvetoQuadraticRel.prototype.toString = function() { return "[object SVGPathSegCurvetoQuadraticRel]"; }\n        window.SVGPathSegCurvetoQuadraticRel.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x1 + " " + this._y1 + " " + this._x + " " + this._y; }\n        window.SVGPathSegCurvetoQuadraticRel.prototype.clone = function() { return new window.SVGPathSegCurvetoQuadraticRel(undefined, this._x, this._y, this._x1, this._y1); }\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticRel.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticRel.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticRel.prototype, "x1", { get: function() { return this._x1; }, set: function(x1) { this._x1 = x1; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticRel.prototype, "y1", { get: function() { return this._y1; }, set: function(y1) { this._y1 = y1; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegArcAbs = function(owningPathSegList, x, y, r1, r2, angle, largeArcFlag, sweepFlag) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_ARC_ABS, "A", owningPathSegList);\n            this._x = x;\n            this._y = y;\n            this._r1 = r1;\n            this._r2 = r2;\n            this._angle = angle;\n            this._largeArcFlag = largeArcFlag;\n            this._sweepFlag = sweepFlag;\n        }\n        window.SVGPathSegArcAbs.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegArcAbs.prototype.toString = function() { return "[object SVGPathSegArcAbs]"; }\n        window.SVGPathSegArcAbs.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._r1 + " " + this._r2 + " " + this._angle + " " + (this._largeArcFlag ? "1" : "0") + " " + (this._sweepFlag ? "1" : "0") + " " + this._x + " " + this._y; }\n        window.SVGPathSegArcAbs.prototype.clone = function() { return new window.SVGPathSegArcAbs(undefined, this._x, this._y, this._r1, this._r2, this._angle, this._largeArcFlag, this._sweepFlag); }\n        Object.defineProperty(window.SVGPathSegArcAbs.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: tru';
  builder.string += 'e });\n        Object.defineProperty(window.SVGPathSegArcAbs.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcAbs.prototype, "r1", { get: function() { return this._r1; }, set: function(r1) { this._r1 = r1; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcAbs.prototype, "r2", { get: function() { return this._r2; }, set: function(r2) { this._r2 = r2; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcAbs.prototype, "angle", { get: function() { return this._angle; }, set: function(angle) { this._angle = angle; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcAbs.prototype, "largeArcFlag", { get: function() { return this._largeArcFlag; }, set: function(largeArcFlag) { this._largeArcFlag = largeArcFlag; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcAbs.prototype, "sweepFlag", { get: function() { return this._sweepFlag; }, set: function(sweepFlag) { this._sweepFlag = sweepFlag; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegArcRel = function(owningPathSegList, x, y, r1, r2, angle, largeArcFlag, sweepFlag) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_ARC_REL, "a", owningPathSegList);\n            this._x = x;\n            this._y = y;\n            this._r1 = r1;\n            this._r2 = r2;\n            this._angle = angle;\n            this._largeArcFlag = largeArcFlag;\n            this._sweepFlag = sweepFlag;\n        }\n        window.SVGPathSegArcRel.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegArcRel.prototype.toString = function() { return "[object SVGPathSegArcRel]"; }\n        window.SVGPathSegArcRel.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._r1 + " " + this._r2 + " " + this._angle + " " + (this._largeArcFlag ? "1" : "0") + " " + (this._sweepFlag ? "1" : "0") + " " + this._x + " " + this._y; }\n        window.SVGPathSegArcRel.prototype.clone = function() { return new window.SVGPathSegArcRel(undefined, this._x, this._y, this._r1, this._r2, this._angle, this._largeArcFlag, this._sweepFlag); }\n        Object.defineProperty(window.SVGPathSegArcRel.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcRel.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcRel.prototype, "r1", { get: function() { return this._r1; }, set: function(r1) { this._r1 = r1; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcRel.prototype, "r2", { get: function() { return this._r2; }, set: function(r2) { this._r2 = r2; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcRel.prototype, "angle", { get: function() { return this._angle; }, set: function(angle) { this._angle = angle; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcRel.prototype, "largeArcFlag", { get: function() { return this._largeArcFlag; }, set: function(largeArcFlag) { this._largeArcFlag = largeArcFlag; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegArcRel.prototype, "sweepFlag", { get: function() { return this._sweepFlag; }, set: function(sweepFlag) { this._sweepFlag = sweepFlag; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegLinetoHorizontalAbs = function(owningPathSegList, x) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS, "H", owningPathSegList);\n            this._x = x;\n        }\n        window.SVGPathSegLinetoHorizontalAbs.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegLinetoHorizontalAbs.prototype.toString = function() { return "[object SVGPathSegLinetoHorizontalAbs]"; }\n        window.SVGPathSegLinetoHorizontalAbs.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x; }\n        window.SVGPathSegLinetoHorizontalAbs.prototype.clone = function() { return new window.SVGPathSegLinetoHorizontalAbs(undefined, this._x); }\n        Object.defineProperty(window.SVGPathSegLinetoHorizontalAbs.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegLinetoHorizontalRel = function(owningPathSegList, x) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL, "h", owningPathSegList);\n            this._x = x;\n        }\n        window.SVGPathSegLinetoHorizontalRel.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegLinetoHorizontalRel.prototype.toString = function() { return "[object SVGPathSegLinetoHorizontalRel]"; }\n        window.SVGPathSegLinetoHorizontalRel.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x; }\n        window.SVGPathSegLinetoHorizontalRel.prototype.clone = function() { return new window.SVGPathSegLinetoHorizontalRel(undefined, this._x); }\n        Object.defineProperty(window.SVGPathSegLinetoHorizontalRel.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegLinetoVerticalAbs = function(owningPathSegList, y) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS, "V", owningPathSegList);\n            this._y = y;\n        }\n        window.SVGPathSegLinetoVerticalAbs.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegLinetoVerticalAbs.prototype.toString = function() { return "[object SVGPathSegLinetoVerticalAbs]"; }\n        window.SVGPathSegLinetoVerticalAbs.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._y; }\n        window.SVGPathSegLinetoVerticalAbs.prototype.clone = function() { return new window.SVGPathSegLinetoVerticalAbs(undefined, this._y); }\n        Object.defineProperty(window.SVGPathSegLinetoVerticalAbs.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegLinetoVerticalRel = function(owningPathSegList, y) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL, "v", owningPathSegList);\n            this._y = y;\n        }\n        window.SVGPathSegLinetoVerticalRel.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegLinetoVerticalRel.prototype.toString = function() { return "[object SVGPathSegLinetoVerticalRel]"; }\n        window.SVGPathSegLinetoVerticalRel.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._y; }\n        window.SVGPathSegLinetoVerticalRel.prototype.clone = function() { return new window.SVGPathSegLinetoVerticalRel(undefined, this._y); }\n        Object.defineProperty(window.SVGPathSegLinetoVerticalRel.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegCurvetoCubicSmoothAbs = function(owningPathSegList, x, y, x2, y2) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS, "S", owningPathSegList);\n            this._x = x;\n            this._y = y;\n            this._x2 = x2;\n            this._y2 = y2;\n        }\n        window.SVGPathSegCurvetoCubicSmoothAbs.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegCurvetoCubicSmoothAbs.prototype.toString = function() { return "[object SVGPathSegCurvetoCubicSmoothAbs]"; }\n        window.SVGPathSegCurvetoCubicSmoothAbs.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x2 + " " + this._y2 + " " + this._x + " " + this._y; }\n        window.SVGPathSegCurvetoCubicSmoothAbs.prototype.clone = function() { return new window.SVGPathSegCurvetoCubicSmoothAbs(undefined, this._x, this._y, this._x2, this._y2); }\n        Object.defineProperty(window.SVGPathSegCurvetoCubicSmoothAbs.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicSmoothAbs.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicSmoothAbs.prototype, "x2", { get: function() { return this._x2; }, set: function(x2) { this._x2 = x2; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicSmoothAbs.prototype, "y2", { get: function() { return this._y2; }, set: function(y2) { this._y2 = y2; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegCurvetoCubicSmoothRel = function(owningPathSegList, x, y, x2, y2) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL, "s", owningPathSegList);\n            this._x = x;\n            this._y = y;\n            this._x2 = x2;\n            this._y2 = y2;\n        }\n        window.SVGPathSegCurvetoCubicSmoothRel.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegCurvetoCubicSmoothRel.prototype.toString = function() { return "[object SVGPathSegCurvetoCubicSmoothRel]"; }\n        window.SVGPathSegCurvetoCubicSmoothRel.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x2 + " " + this._y2 + " " + this._x + " " + this._y; }\n        window.SVGPathSegCurvetoCubicSmoothRel.prototype.clone = function() { return new window.SVGPathSegCurvetoCubicSmoothRel(undefined, this._x, this._y, this._x2, this._y2); }\n        Object.defineProperty(window.SVGPathSegCurvetoCubicSmoothRel.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicSmoothRel.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicSmoothRel.prototype, "x2", { get: function() { return this._x2; }, set: function(x2) { this._x2 = x2; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoCubicSmoothRel.prototype, "y2", { get: function() { return this._y2; }, set: function(y2) { this._y2 = y2; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegCurvetoQuadraticSmoothAbs = function(owningPathSegList, x, y) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS, "T", owningPathSegList);\n            this._x = x;\n            this._y = y;\n        }\n        window.SVGPathSegCurvetoQuadraticSmoothAbs.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegCurvetoQuadraticSmoothAbs.prototype.toString = function() { return "[object SVGPathSegCurvetoQuadraticSmoothAbs]"; }\n        window.SVGPathSegCurvetoQuadraticSmoothAbs.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x + " " + this._y; }\n        window.SVGPathSegCurvetoQuadraticSmoothAbs.prototype.clone = function() { return new window.SVGPathSegCurvetoQuadraticSmoothAbs(undefined, this._x, this._y); }\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticSmoothAbs.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticSmoothAbs.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n\n        window.SVGPathSegCurvetoQuadraticSmoothRel = function(owningPathSegList, x, y) {\n            window.SVGPathSeg.call(this, window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL, "t", owningPathSegList);\n            this._x = x;\n            this._y = y;\n        }\n        window.SVGPathSegCurvetoQuadraticSmoothRel.prototype = Object.create(window.SVGPathSeg.prototype);\n        window.SVGPathSegCurvetoQuadraticSmoothRel.prototype.toString = function() { return "[object SVGPathSegCurvetoQuadraticSmoothRel]"; }\n        window.SVGPathSegCurvetoQuadraticSmoothRel.prototype._asPathString = function() { return this.pathSegTypeAsLetter + " " + this._x + " " + this._y; }\n        window.SVGPathSegCurvetoQuadraticSmoothRel.prototype.clone = function() { return new window.SVGPathSegCurvetoQuadraticSmoothRel(undefined, this._x, this._y); }\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticSmoothRel.prototype, "x", { get: function() { return this._x; }, set: function(x) { this._x = x; this._segmentChanged(); }, enumerable: true });\n        Object.defineProperty(window.SVGPathSegCurvetoQuadraticSmoothRel.prototype, "y", { get: function() { return this._y; }, set: function(y) { this._y = y; this._segmentChanged(); }, enumerable: true });\n\n        // Add createSVGPathSeg* functions to window.SVGPathElement.\n        // Spec: http://www.w3.org/TR/SVG11/single-page.html#paths-Interfacewindow.SVGPathElement.\n        window.SVGPathElement.prototype.createSVGPathSegClosePath = function() { return new window.SVGPathSegClosePath(undefined); }\n        window.SVGPathElement.prototype.createSVGPathSegMovetoAbs = function(x, y) { return new window.SVGPathSegMovetoAbs(undefined, x, y); }\n        window.SVGPathElement.prototype.createSVGPathSegMovetoRel = function(x, y) { return new window.SVGPathSegMovetoRel(undefined, x, y); }\n        window.SVGPathElement.prototype.createSVGPathSegLinetoAbs = function(x, y) { return new window.SVGPathSegLinetoAbs(undefined, x, y); }\n        window.SVGPathElement.prototype.createSVGPathSegLinetoRel = function(x, y) { return new window.SVGPathSegLinetoRel(undefined, x, y); }\n        window.SVGPathElement.prototype.createSVGPathSegCurvetoCubicAbs = function(x, y, x1, y1, x2, y2) { return new window.SVGPathSegCurvetoCubicAbs(undefined, x, y, x1, y1, x2, y2); }\n        window.SVGPathElement.prototype.createSVGPathSegCurvetoCubicRel = function(x, y, x1, y1, x2, y2) { return new window.SVGPathSegCurvetoCubicRel(undefined, x, y, x1, y1, x2, y2); }\n        window.SVGPathElement.prototype.createSVGPathSegCurvetoQuadraticAbs = function(x, y, x1, y1) { return new window.SVGPathSegCurvetoQuadraticAbs(undefined, x, y, x1, y1); }\n        window.SVGPathElement.prototype.createSVGPathSegCurvetoQuadraticRel = function(x, y, x1, y1) { return new window.SVGPathSegCurvetoQuadraticRel(undefined, x, y, x1, y1); }\n        window.SVGPathElement.prototype.createSVGPathSegArcAbs = function(x, y, r1, r2, angle, largeArcFlag, sweepFlag) { return new window.SVGPathSegArcAbs(undefined, x, y, r1, r2, angle, largeArcFlag, sweepFlag); }\n        window.SVGPathElement.prototype.createSVGPathSegArcRel = function(x, y, r1, r2, angle, largeArcFlag, sweepFlag) { return new window.SVGPathSegArcRel(undefined, x, y, r1, r2, angle, largeArcFlag, sweepFlag); }\n        window.SVGPathElement.prototype.createSVGPathSegLinetoHorizontalAbs = function(x) { return new window.SVGPathSegLinetoHorizontalAbs(undefined, x); }\n        window.SVGPathElement.prototype.createSVGPathSegLinetoHorizontalRel = function(x) { return new window.SVGPathSegLinetoHorizontalRel(undefined, x); }\n        window.SVGPathElement.prototype.createSVGPathSegLinetoVerticalAbs = function(y) { return new window.SVGPathSegLinetoVerticalAbs(undefined, y); }\n        window.SVGPathElement.prototype.createSVGPathSegLinetoVerticalRel = function(y) { return new window.SVGPathSegLinetoVerticalRel(undefined, y); }\n        window.SVGPathElement.prototype.createSVGPathSegCurvetoCubicSmoothAbs = function(x, y, x2, y2) { return new ';
  builder.string += 'window.SVGPathSegCurvetoCubicSmoothAbs(undefined, x, y, x2, y2); }\n        window.SVGPathElement.prototype.createSVGPathSegCurvetoCubicSmoothRel = function(x, y, x2, y2) { return new window.SVGPathSegCurvetoCubicSmoothRel(undefined, x, y, x2, y2); }\n        window.SVGPathElement.prototype.createSVGPathSegCurvetoQuadraticSmoothAbs = function(x, y) { return new window.SVGPathSegCurvetoQuadraticSmoothAbs(undefined, x, y); }\n        window.SVGPathElement.prototype.createSVGPathSegCurvetoQuadraticSmoothRel = function(x, y) { return new window.SVGPathSegCurvetoQuadraticSmoothRel(undefined, x, y); }\n\n        if (!("getPathSegAtLength" in window.SVGPathElement.prototype)) {\n            // Add getPathSegAtLength to SVGPathElement.\n            // Spec: https://www.w3.org/TR/SVG11/single-page.html#paths-__svg__SVGPathElement__getPathSegAtLength\n            // This polyfill requires SVGPathElement.getTotalLength to implement the distance-along-a-path algorithm.\n            window.SVGPathElement.prototype.getPathSegAtLength = function(distance) {\n                if (distance === undefined || !isFinite(distance))\n                    throw "Invalid arguments.";\n\n                var measurementElement = document.createElementNS("http://www.w3.org/2000/svg", "path");\n                measurementElement.setAttribute("d", this.getAttribute("d"));\n                var lastPathSegment = measurementElement.pathSegList.numberOfItems - 1;\n\n                // If the path is empty, return 0.\n                if (lastPathSegment <= 0)\n                    return 0;\n\n                do {\n                    measurementElement.pathSegList.removeItem(lastPathSegment);\n                    if (distance > measurementElement.getTotalLength())\n                        break;\n                    lastPathSegment--;\n                } while (lastPathSegment > 0);\n                return lastPathSegment;\n            }\n        }\n    }\n\n    // Checking for SVGPathSegList in window checks for the case of an implementation without the\n    // SVGPathSegList API.\n    // The second check for appendItem is specific to Firefox 59+ which removed only parts of the\n    // SVGPathSegList API (e.g., appendItem). In this case we need to re-implement the entire API\n    // so the polyfill data (i.e., _list) is used throughout.\n    if (!("SVGPathSegList" in window) || !("appendItem" in window.SVGPathSegList.prototype)) {\n        // Spec: http://www.w3.org/TR/SVG11/single-page.html#paths-InterfaceSVGPathSegList\n        window.SVGPathSegList = function(pathElement) {\n            this._pathElement = pathElement;\n            this._list = this._parsePath(this._pathElement.getAttribute("d"));\n\n            // Use a MutationObserver to catch changes to the path\'s "d" attribute.\n            this._mutationObserverConfig = { "attributes": true, "attributeFilter": ["d"] };\n            this._pathElementMutationObserver = new MutationObserver(this._updateListFromPathMutations.bind(this));\n            this._pathElementMutationObserver.observe(this._pathElement, this._mutationObserverConfig);\n        }\n\n        window.SVGPathSegList.prototype.classname = "SVGPathSegList";\n\n        Object.defineProperty(window.SVGPathSegList.prototype, "numberOfItems", {\n            get: function() {\n                this._checkPathSynchronizedToList();\n                return this._list.length;\n            },\n            enumerable: true\n        });\n\n        // Add the pathSegList accessors to window.SVGPathElement.\n        // Spec: http://www.w3.org/TR/SVG11/single-page.html#paths-InterfaceSVGAnimatedPathData\n        Object.defineProperty(window.SVGPathElement.prototype, "pathSegList", {\n            get: function() {\n                if (!this._pathSegList)\n                    this._pathSegList = new window.SVGPathSegList(this);\n                return this._pathSegList;\n            },\n            enumerable: true\n        });\n        // FIXME: The following are not implemented and simply return window.SVGPathElement.pathSegList.\n        Object.defineProperty(window.SVGPathElement.prototype, "normalizedPathSegList", { get: function() { return this.pathSegList; }, enumerable: true });\n        Object.defineProperty(window.SVGPathElement.prototype, "animatedPathSegList", { get: function() { return this.pathSegList; }, enumerable: true });\n        Object.defineProperty(window.SVGPathElement.prototype, "animatedNormalizedPathSegList", { get: function() { return this.pathSegList; }, enumerable: true });\n\n        // Process any pending mutations to the path element and update the list as needed.\n        // This should be the first call of all public functions and is needed because\n        // MutationObservers are not synchronous so we can have pending asynchronous mutations.\n        window.SVGPathSegList.prototype._checkPathSynchronizedToList = function() {\n            this._updateListFromPathMutations(this._pathElementMutationObserver.takeRecords());\n        }\n\n        window.SVGPathSegList.prototype._updateListFromPathMutations = function(mutationRecords) {\n            if (!this._pathElement)\n                return;\n            var hasPathMutations = false;\n            mutationRecords.forEach(function(record) {\n                if (record.attributeName == "d")\n                    hasPathMutations = true;\n            });\n            if (hasPathMutations)\n                this._list = this._parsePath(this._pathElement.getAttribute("d"));\n        }\n\n        // Serialize the list and update the path\'s \'d\' attribute.\n        window.SVGPathSegList.prototype._writeListToPath = function() {\n            this._pathElementMutationObserver.disconnect();\n            this._pathElement.setAttribute("d", window.SVGPathSegList._pathSegArrayAsString(this._list));\n            this._pathElementMutationObserver.observe(this._pathElement, this._mutationObserverConfig);\n        }\n\n        // When a path segment changes the list needs to be synchronized back to the path element.\n        window.SVGPathSegList.prototype.segmentChanged = function(pathSeg) {\n            this._writeListToPath();\n        }\n\n        window.SVGPathSegList.prototype.clear = function() {\n            this._checkPathSynchronizedToList();\n\n            this._list.forEach(function(pathSeg) {\n                pathSeg._owningPathSegList = null;\n            });\n            this._list = [];\n            this._writeListToPath();\n        }\n\n        window.SVGPathSegList.prototype.initialize = function(newItem) {\n            this._checkPathSynchronizedToList();\n\n            this._list = [newItem];\n            newItem._owningPathSegList = this;\n            this._writeListToPath();\n            return newItem;\n        }\n\n        window.SVGPathSegList.prototype._checkValidIndex = function(index) {\n            if (isNaN(index) || index < 0 || index >= this.numberOfItems)\n                throw "INDEX_SIZE_ERR";\n        }\n\n        window.SVGPathSegList.prototype.getItem = function(index) {\n            this._checkPathSynchronizedToList();\n\n            this._checkValidIndex(index);\n            return this._list[index];\n        }\n\n        window.SVGPathSegList.prototype.insertItemBefore = function(newItem, index) {\n            this._checkPathSynchronizedToList();\n\n            // Spec: If the index is greater than or equal to numberOfItems, then the new item is appended to the end of the list.\n            if (index > this.numberOfItems)\n                index = this.numberOfItems;\n            if (newItem._owningPathSegList) {\n                // SVG2 spec says to make a copy.\n                newItem = newItem.clone();\n            }\n            this._list.splice(index, 0, newItem);\n            newItem._owningPathSegList = this;\n            this._writeListToPath();\n            return newItem;\n        }\n\n        window.SVGPathSegList.prototype.replaceItem = function(newItem, index) {\n            this._checkPathSynchronizedToList();\n\n            if (newItem._owningPathSegList) {\n                // SVG2 spec says to make a copy.\n                newItem = newItem.clone();\n            }\n            this._checkValidIndex(index);\n            this._list[index] = newItem;\n            newItem._owningPathSegList = this;\n            this._writeListToPath();\n            return newItem;\n        }\n\n        window.SVGPathSegList.prototype.removeItem = function(index) {\n            this._checkPathSynchronizedToList();\n\n            this._checkValidIndex(index);\n            var item = this._list[index];\n            this._list.splice(index, 1);\n            this._writeListToPath();\n            return item;\n        }\n\n        window.SVGPathSegList.prototype.appendItem = function(newItem) {\n            this._checkPathSynchronizedToList();\n\n            if (newItem._owningPathSegList) {\n                // SVG2 spec says to make a copy.\n                newItem = newItem.clone();\n            }\n            this._list.push(newItem);\n            newItem._owningPathSegList = this;\n            // TODO: Optimize this to just append to the existing attribute.\n            this._writeListToPath();\n            return newItem;\n        }\n\n        window.SVGPathSegList._pathSegArrayAsString = function(pathSegArray) {\n            var string = "";\n            var first = true;\n            pathSegArray.forEach(function(pathSeg) {\n                if (first) {\n                    first = false;\n                    string += pathSeg._asPathString();\n                } else {\n                    string += " " + pathSeg._asPathString();\n                }\n            });\n            return string;\n        }\n\n        // This closely follows SVGPathParser::parsePath from Source/core/svg/SVGPathParser.cpp.\n        window.SVGPathSegList.prototype._parsePath = function(string) {\n            if (!string || string.length == 0)\n                return [];\n\n            var owningPathSegList = this;\n\n            var Builder = function() {\n                this.pathSegList = [];\n            }\n\n            Builder.prototype.appendSegment = function(pathSeg) {\n                this.pathSegList.push(pathSeg);\n            }\n\n            var Source = function(string) {\n                this._string = string;\n                this._currentIndex = 0;\n                this._endIndex = this._string.length;\n                this._previousCommand = window.SVGPathSeg.PATHSEG_UNKNOWN;\n\n                this._skipOptionalSpaces();\n            }\n\n            Source.prototype._isCurrentSpace = function() {\n                var character = this._string[this._currentIndex];\n                return character <= " " && (character == " " || character == "\\n" || character == "\\t" || character == "\\r" || character == "\\f");\n            }\n\n            Source.prototype._skipOptionalSpaces = function() {\n                while (this._currentIndex < this._endIndex && this._isCurrentSpace())\n                    this._currentIndex++;\n                return this._currentIndex < this._endIndex;\n            }\n\n            Source.prototype._skipOptionalSpacesOrDelimiter = function() {\n                if (this._currentIndex < this._endIndex && !this._isCurrentSpace() && this._string.charAt(this._currentIndex) != ",")\n                    return false;\n                if (this._skipOptionalSpaces()) {\n                    if (this._currentIndex < this._endIndex && this._string.charAt(this._currentIndex) == ",") {\n                        this._currentIndex++;\n                        this._skipOptionalSpaces();\n                    }\n                }\n                return this._currentIndex < this._endIndex;\n            }\n\n            Source.prototype.hasMoreData = function() {\n                return this._currentIndex < this._endIndex;\n            }\n\n            Source.prototype.peekSegmentType = function() {\n                var lookahead = this._string[this._currentIndex];\n                return this._pathSegTypeFromChar(lookahead);\n            }\n\n            Source.prototype._pathSegTypeFromChar = function(lookahead) {\n                switch (lookahead) {\n                case "Z":\n                case "z":\n                    return window.SVGPathSeg.PATHSEG_CLOSEPATH;\n                case "M":\n                    return window.SVGPathSeg.PATHSEG_MOVETO_ABS;\n                case "m":\n                    return window.SVGPathSeg.PATHSEG_MOVETO_REL;\n                case "L":\n                    return window.SVGPathSeg.PATHSEG_LINETO_ABS;\n                case "l":\n                    return window.SVGPathSeg.PATHSEG_LINETO_REL;\n                case "C":\n                    return window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS;\n                case "c":\n                    return window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL;\n                case "Q":\n                    return window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS;\n                case "q":\n                    return window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL;\n                case "A":\n                    return window.SVGPathSeg.PATHSEG_ARC_ABS;\n                case "a":\n                    return window.SVGPathSeg.PATHSEG_ARC_REL;\n                case "H":\n                    return window.SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS;\n                case "h":\n                    return window.SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL;\n                case "V":\n                    return window.SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS;\n                case "v":\n                    return window.SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL;\n                case "S":\n                    return window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS;\n                case "s":\n                    return window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL;\n                case "T":\n                    return window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS;\n                case "t":\n                    return window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL;\n                default:\n                    return window.SVGPathSeg.PATHSEG_UNKNOWN;\n                }\n            }\n\n            Source.prototype._nextCommandHelper = function(lookahead, previousCommand) {\n                // Check for remaining coordinates in the current command.\n                if ((lookahead == "+" || lookahead == "-" || lookahead == "." || (lookahead >= "0" && lookahead <= "9")) && previousCommand != window.SVGPathSeg.PATHSEG_CLOSEPATH) {\n                    if (previousCommand == window.SVGPathSeg.PATHSEG_MOVETO_ABS)\n                        return window.SVGPathSeg.PATHSEG_LINETO_ABS;\n                    if (previousCommand == window.SVGPathSeg.PATHSEG_MOVETO_REL)\n                        return window.SVGPathSeg.PATHSEG_LINETO_REL;\n                    return previousCommand;\n                }\n                return window.SVGPathSeg.PATHSEG_UNKNOWN;\n            }\n\n            Source.prototype.initialCommandIsMoveTo = function() {\n                // If the path is empty it is still valid, so return true.\n                if (!this.hasMoreData())\n                    return true;\n                var command = this.peekSegmentType();\n                // Path must start with moveTo.\n                return command == window.SVGPathSeg.PATHSEG_MOVETO_ABS || command == window.SVGPathSeg.PATHSEG_MOVETO_REL;\n            }\n\n            // Parse a number from an SVG path. This very closely follows genericParseNumber(...) from Source/core/svg/SVGParserUtilities.cpp.\n            // Spec: http://www.w3.org/TR/SVG11/single-page.html#paths-PathDataBNF\n            Source.prototype._parseNumber = function() {\n                var exponent = 0;\n                var integer = 0;\n                var frac = 1;\n                var decimal = 0;\n                var sign = 1;\n                var expsign = 1;\n\n                var startIndex = this._currentIndex;\n\n                this._skipOptionalSpaces();\n\n                // Read the sign.\n                if (this._currentIndex < this._endIndex && this._string.charAt(this._currentIndex) == "+")\n                    this._currentIndex++;\n                else if (this._currentIndex < this._endIndex && this._string.charAt(this._currentIndex) == "-") {\n                    this._currentIndex++;\n                    sign = -1;\n                }\n\n                if (this._currentIndex == this._endIndex || ((this._string.charAt(this._currentIndex) < "0" || this._string.charAt(this._currentIndex) > "9") && this._string.ch';
  builder.string += 'arAt(this._currentIndex) != "."))\n                    // The first character of a number must be one of [0-9+-.].\n                    return undefined;\n\n                // Read the integer part, build right-to-left.\n                var startIntPartIndex = this._currentIndex;\n                while (this._currentIndex < this._endIndex && this._string.charAt(this._currentIndex) >= "0" && this._string.charAt(this._currentIndex) <= "9")\n                    this._currentIndex++; // Advance to first non-digit.\n\n                if (this._currentIndex != startIntPartIndex) {\n                    var scanIntPartIndex = this._currentIndex - 1;\n                    var multiplier = 1;\n                    while (scanIntPartIndex >= startIntPartIndex) {\n                        integer += multiplier * (this._string.charAt(scanIntPartIndex--) - "0");\n                        multiplier *= 10;\n                    }\n                }\n\n                // Read the decimals.\n                if (this._currentIndex < this._endIndex && this._string.charAt(this._currentIndex) == ".") {\n                    this._currentIndex++;\n\n                    // There must be a least one digit following the .\n                    if (this._currentIndex >= this._endIndex || this._string.charAt(this._currentIndex) < "0" || this._string.charAt(this._currentIndex) > "9")\n                        return undefined;\n                    while (this._currentIndex < this._endIndex && this._string.charAt(this._currentIndex) >= "0" && this._string.charAt(this._currentIndex) <= "9") {\n                        frac *= 10;\n                        decimal += (this._string.charAt(this._currentIndex) - "0") / frac;\n                        this._currentIndex += 1;\n                    }\n                }\n\n                // Read the exponent part.\n                if (this._currentIndex != startIndex && this._currentIndex + 1 < this._endIndex && (this._string.charAt(this._currentIndex) == "e" || this._string.charAt(this._currentIndex) == "E") && (this._string.charAt(this._currentIndex + 1) != "x" && this._string.charAt(this._currentIndex + 1) != "m")) {\n                    this._currentIndex++;\n\n                    // Read the sign of the exponent.\n                    if (this._string.charAt(this._currentIndex) == "+") {\n                        this._currentIndex++;\n                    } else if (this._string.charAt(this._currentIndex) == "-") {\n                        this._currentIndex++;\n                        expsign = -1;\n                    }\n\n                    // There must be an exponent.\n                    if (this._currentIndex >= this._endIndex || this._string.charAt(this._currentIndex) < "0" || this._string.charAt(this._currentIndex) > "9")\n                        return undefined;\n\n                    while (this._currentIndex < this._endIndex && this._string.charAt(this._currentIndex) >= "0" && this._string.charAt(this._currentIndex) <= "9") {\n                        exponent *= 10;\n                        exponent += (this._string.charAt(this._currentIndex) - "0");\n                        this._currentIndex++;\n                    }\n                }\n\n                var number = integer + decimal;\n                number *= sign;\n\n                if (exponent)\n                    number *= Math.pow(10, expsign * exponent);\n\n                if (startIndex == this._currentIndex)\n                    return undefined;\n\n                this._skipOptionalSpacesOrDelimiter();\n\n                return number;\n            }\n\n            Source.prototype._parseArcFlag = function() {\n                if (this._currentIndex >= this._endIndex)\n                    return undefined;\n                var flag = false;\n                var flagChar = this._string.charAt(this._currentIndex++);\n                if (flagChar == "0")\n                    flag = false;\n                else if (flagChar == "1")\n                    flag = true;\n                else\n                    return undefined;\n\n                this._skipOptionalSpacesOrDelimiter();\n                return flag;\n            }\n\n            Source.prototype.parseSegment = function() {\n                var lookahead = this._string[this._currentIndex];\n                var command = this._pathSegTypeFromChar(lookahead);\n                if (command == window.SVGPathSeg.PATHSEG_UNKNOWN) {\n                    // Possibly an implicit command. Not allowed if this is the first command.\n                    if (this._previousCommand == window.SVGPathSeg.PATHSEG_UNKNOWN)\n                        return null;\n                    command = this._nextCommandHelper(lookahead, this._previousCommand);\n                    if (command == window.SVGPathSeg.PATHSEG_UNKNOWN)\n                        return null;\n                } else {\n                    this._currentIndex++;\n                }\n\n                this._previousCommand = command;\n\n                switch (command) {\n                case window.SVGPathSeg.PATHSEG_MOVETO_REL:\n                    return new window.SVGPathSegMovetoRel(owningPathSegList, this._parseNumber(), this._parseNumber());\n                case window.SVGPathSeg.PATHSEG_MOVETO_ABS:\n                    return new window.SVGPathSegMovetoAbs(owningPathSegList, this._parseNumber(), this._parseNumber());\n                case window.SVGPathSeg.PATHSEG_LINETO_REL:\n                    return new window.SVGPathSegLinetoRel(owningPathSegList, this._parseNumber(), this._parseNumber());\n                case window.SVGPathSeg.PATHSEG_LINETO_ABS:\n                    return new window.SVGPathSegLinetoAbs(owningPathSegList, this._parseNumber(), this._parseNumber());\n                case window.SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_REL:\n                    return new window.SVGPathSegLinetoHorizontalRel(owningPathSegList, this._parseNumber());\n                case window.SVGPathSeg.PATHSEG_LINETO_HORIZONTAL_ABS:\n                    return new window.SVGPathSegLinetoHorizontalAbs(owningPathSegList, this._parseNumber());\n                case window.SVGPathSeg.PATHSEG_LINETO_VERTICAL_REL:\n                    return new window.SVGPathSegLinetoVerticalRel(owningPathSegList, this._parseNumber());\n                case window.SVGPathSeg.PATHSEG_LINETO_VERTICAL_ABS:\n                    return new window.SVGPathSegLinetoVerticalAbs(owningPathSegList, this._parseNumber());\n                case window.SVGPathSeg.PATHSEG_CLOSEPATH:\n                    this._skipOptionalSpaces();\n                    return new window.SVGPathSegClosePath(owningPathSegList);\n                case window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_REL:\n                    var points = {x1: this._parseNumber(), y1: this._parseNumber(), x2: this._parseNumber(), y2: this._parseNumber(), x: this._parseNumber(), y: this._parseNumber()};\n                    return new window.SVGPathSegCurvetoCubicRel(owningPathSegList, points.x, points.y, points.x1, points.y1, points.x2, points.y2);\n                case window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS:\n                    var points = {x1: this._parseNumber(), y1: this._parseNumber(), x2: this._parseNumber(), y2: this._parseNumber(), x: this._parseNumber(), y: this._parseNumber()};\n                    return new window.SVGPathSegCurvetoCubicAbs(owningPathSegList, points.x, points.y, points.x1, points.y1, points.x2, points.y2);\n                case window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_REL:\n                    var points = {x2: this._parseNumber(), y2: this._parseNumber(), x: this._parseNumber(), y: this._parseNumber()};\n                    return new window.SVGPathSegCurvetoCubicSmoothRel(owningPathSegList, points.x, points.y, points.x2, points.y2);\n                case window.SVGPathSeg.PATHSEG_CURVETO_CUBIC_SMOOTH_ABS:\n                    var points = {x2: this._parseNumber(), y2: this._parseNumber(), x: this._parseNumber(), y: this._parseNumber()};\n                    return new window.SVGPathSegCurvetoCubicSmoothAbs(owningPathSegList, points.x, points.y, points.x2, points.y2);\n                case window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_REL:\n                    var points = {x1: this._parseNumber(), y1: this._parseNumber(), x: this._parseNumber(), y: this._parseNumber()};\n                    return new window.SVGPathSegCurvetoQuadraticRel(owningPathSegList, points.x, points.y, points.x1, points.y1);\n                case window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_ABS:\n                    var points = {x1: this._parseNumber(), y1: this._parseNumber(), x: this._parseNumber(), y: this._parseNumber()};\n                    return new window.SVGPathSegCurvetoQuadraticAbs(owningPathSegList, points.x, points.y, points.x1, points.y1);\n                case window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_REL:\n                    return new window.SVGPathSegCurvetoQuadraticSmoothRel(owningPathSegList, this._parseNumber(), this._parseNumber());\n                case window.SVGPathSeg.PATHSEG_CURVETO_QUADRATIC_SMOOTH_ABS:\n                    return new window.SVGPathSegCurvetoQuadraticSmoothAbs(owningPathSegList, this._parseNumber(), this._parseNumber());\n                case window.SVGPathSeg.PATHSEG_ARC_REL:\n                    var points = {x1: this._parseNumber(), y1: this._parseNumber(), arcAngle: this._parseNumber(), arcLarge: this._parseArcFlag(), arcSweep: this._parseArcFlag(), x: this._parseNumber(), y: this._parseNumber()};\n                    return new window.SVGPathSegArcRel(owningPathSegList, points.x, points.y, points.x1, points.y1, points.arcAngle, points.arcLarge, points.arcSweep);\n                case window.SVGPathSeg.PATHSEG_ARC_ABS:\n                    var points = {x1: this._parseNumber(), y1: this._parseNumber(), arcAngle: this._parseNumber(), arcLarge: this._parseArcFlag(), arcSweep: this._parseArcFlag(), x: this._parseNumber(), y: this._parseNumber()};\n                    return new window.SVGPathSegArcAbs(owningPathSegList, points.x, points.y, points.x1, points.y1, points.arcAngle, points.arcLarge, points.arcSweep);\n                default:\n                    throw "Unknown path seg type."\n                }\n            }\n\n            var builder = new Builder();\n            var source = new Source(string);\n\n            if (!source.initialCommandIsMoveTo())\n                return [];\n            while (source.hasMoreData()) {\n                var pathSeg = source.parseSegment();\n                if (!pathSeg)\n                    return [];\n                builder.appendSegment(pathSeg);\n            }\n\n            return builder.pathSegList;\n        }\n    }\n}());\n';
  return builder.string;
}

defineClass(336, 1, {});
var Lorg_vectomatic_dom_svg_impl_SVGParserImpl_2_classLit = createForClass('org.vectomatic.dom.svg.impl', 'SVGParserImpl', 336);
function SVGParserImplMozilla(){
  new DOMParser;
}

defineClass(337, 336, {}, SVGParserImplMozilla);
var Lorg_vectomatic_dom_svg_impl_SVGParserImplMozilla_2_classLit = createForClass('org.vectomatic.dom.svg.impl', 'SVGParserImplMozilla', 337);
function $createSVGPathSegCurvetoCubicRel(this$static, x_0, y_0, x1, y1, x2, y2){
  return this$static.createSVGPathSegCurvetoCubicRel(x_0, y_0, x1, y1, x2, y2);
}

function $createSVGPathSegLinetoAbs(this$static, x_0, y_0){
  return this$static.createSVGPathSegLinetoAbs(x_0, y_0);
}

function $createSVGPathSegLinetoHorizontalAbs(this$static, x_0){
  return this$static.createSVGPathSegLinetoHorizontalAbs(x_0);
}

function $createSVGPathSegLinetoHorizontalRel(this$static, x_0){
  return this$static.createSVGPathSegLinetoHorizontalRel(x_0);
}

function $createSVGPathSegLinetoRel(this$static, x_0, y_0){
  return this$static.createSVGPathSegLinetoRel(x_0, y_0);
}

function $createSVGPathSegLinetoVerticalAbs(this$static, y_0){
  return this$static.createSVGPathSegLinetoVerticalAbs(y_0);
}

function $createSVGPathSegLinetoVerticalRel(this$static, y_0){
  return this$static.createSVGPathSegLinetoVerticalRel(y_0);
}

function $createSVGPathSegMovetoAbs(this$static, x_0, y_0){
  return this$static.createSVGPathSegMovetoAbs(x_0, y_0);
}

function $createSVGPathSegMovetoRel(this$static, x_0, y_0){
  return this$static.createSVGPathSegMovetoRel(x_0, y_0);
}

function $getPathSegList(this$static){
  return convertList(this$static.pathSegList);
}

function $clinit_DOMHelper(){
  $clinit_DOMHelper = emptyMethod;
  impl_1 = new DOMHelperImpl;
}

function bindEventListener(elem, eventName){
  $clinit_DOMHelper();
  $bindEventListener(impl_1, elem, eventName);
}

function createElementNS(document_0, namespaceURI, qualifiedName){
  $clinit_DOMHelper();
  return document_0.createElementNS(namespaceURI, qualifiedName);
}

function getType_3(x_0){
  $clinit_DOMHelper();
  if (x_0 == null) {
    return 'null';
  }
  var t = typeof x_0;
  if (t != 'object') {
    return t;
  }
  if ('__IsSVGPathSegList__' in x_0) {
    return 'SVGPathSegList';
  }
  var c = Object.prototype.toString.apply(x_0);
  c = c.substring(8, c.length - 1);
  if (c != 'Object') {
    return c;
  }
  if (x_0.constructor == Object) {
    return c;
  }
  if ('classname' in x_0.constructor.prototype && typeof x_0.constructor.prototype.classname == 'string')
    return x_0.constructor.prototype.classname;
  return '<unknown type>';
}

var impl_1;
function $clinit_OMSVGParser(){
  $clinit_OMSVGParser = emptyMethod;
  new SVGParserImplMozilla;
}

var C_classLit = createForPrimitive('char', 'C');
var I_classLit = createForPrimitive('int', 'I');
var B_classLit = createForPrimitive('byte', 'B');
var D_classLit = createForPrimitive('double', 'D');
_ = provide('UmlJS', UmlJS);
_.createDiagram = createDiagram;
_.createDiagramId = createDiagramId;
_ = provide('java.io.Serializable');
_.$isInstance = $isInstance;
$clinit_Boolean();
_ = provide('java.lang.Boolean');
_.$isInstance = $isInstance_0;
_ = provide('java.lang.CharSequence');
_.$isInstance = $isInstance_1;
_ = provide('java.lang.Cloneable');
_.$isInstance = $isInstance_2;
_ = provide('java.lang.Comparable');
_.$isInstance = $isInstance_3;
_ = provide('java.lang.Double');
_.$isInstance = $isInstance_5;
_ = provide('java.lang.Number');
_.$isInstance = $isInstance_4;
_ = provide('java.lang.String');
_.$isInstance = $isInstance_6;
_ = provide('java.lang.Throwable');
_.of = of;
_ = provide('javaemul.internal.HashCodes', HashCodes);
_.getIdentityHashCode = getIdentityHashCode;
_.getNextHash = getNextHash;
_.getObjectIdentityHashCode = getObjectIdentityHashCode;
_ = provide('javaemul.internal.JsUtils');
_.toDoubleFromUnsignedInt = toDoubleFromUnsignedInt;
$clinit_UIService();
_ = provide('tl.service.UIService', UIService);
_.init = init_1;
_.invoke = invoke_0;
_.registerFactory = registerFactory;
var $entry = ($clinit_Impl() , entry_0);
var gwtOnLoad = gwtOnLoad = gwtOnLoad_0;
addInitFunctions(init);
setGwtProperty('permProps', [[['locale', 'default'], ['user.agent', 'gecko1_8']]]);
$sendStats('moduleStartup', 'moduleEvalEnd');
gwtOnLoad(__gwtModuleFunction.__errFn, __gwtModuleFunction.__moduleName, __gwtModuleFunction.__moduleBase, __gwtModuleFunction.__softPermutationId,__gwtModuleFunction.__computePropValue);
$sendStats('moduleStartup', 'end');
$gwt && $gwt.permProps && __gwtModuleFunction.__moduleStartupDone($gwt.permProps);
//# sourceURL=TLBlocksClient-0.js

