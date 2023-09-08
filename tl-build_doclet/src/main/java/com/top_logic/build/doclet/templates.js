(function() {(window.nunjucksPrecompiled = window.nunjucksPrecompiled || {})["class-template.html"] = (function() {
function root(env, context, frame, runtime, cb) {
var lineno = 0;
var colno = 0;
var output = "";
try {
var parentTemplate = null;
output += "<div class=\"root\">\n";
env.getTemplate("common-template.html", false, "class-template.html", false, function(t_2,t_1) {
if(t_2) { cb(t_2); return; }
t_1.getExported(function(t_3,t_1) {
if(t_3) { cb(t_3); return; }
context.setVariable("common", t_1);
output += "\n\n<div class=\"javadoc\">\n";
var tasks = [];
tasks.push(
function(callback) {
env.getTemplate("search-template.html", false, "class-template.html", false, function(t_5,t_4) {
if(t_5) { cb(t_5); return; }
callback(null,t_4);});
});
tasks.push(
function(template, callback){
template.render(context.getVariables(), frame, function(t_7,t_6) {
if(t_7) { cb(t_7); return; }
callback(null,t_6);});
});
tasks.push(
function(result, callback){
output += result;
callback(null);
});
env.waterfall(tasks, function(){
output += "\n\n<div class=\"header\">\n<a href=\"#package-list\"><img alt=\"TopLogic\" src=\"toplogic.svg\" height=\"50px\"/></a>\nDocumentation\n</div>\n\n";
var macro_t_8 = runtime.makeMacro(
["method"], 
[], 
function (l_method, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("method", l_method);
var t_9 = "";t_9 += "\n  \t<p>";
t_9 += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((l_method),"title"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
t_9 += "</p>\n  \t";
t_9 += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((l_method),"doc"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
t_9 += "\n";
;
frame = callerFrame;
return new runtime.SafeString(t_9);
});
context.addExport("printDoc");
context.setVariable("printDoc", macro_t_8);
output += "\n\n";
var macro_t_10 = runtime.makeMacro(
["element"], 
[], 
function (l_element, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("element", l_element);
var t_11 = "";t_11 += "\n\t<a class=\"link\" title=\"Link to this member.\" href=\"#";
t_11 += runtime.suppressValue(runtime.contextOrFrameLookup(context, frame, "name"), env.opts.autoescape);
t_11 += "#";
t_11 += runtime.suppressValue(runtime.memberLookup((l_element),"id"), env.opts.autoescape);
t_11 += "\"><i class=\"fas fa-link\"></i></a>\n\t";
if(runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "settings")),"showSrcLink") && runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "pkgInfo")),"srcLink")) {
t_11 += "\n\t<a class=\"link\" title=\"Link to the source code.\" href=\"";
t_11 += runtime.suppressValue(env.getFilter("srcUrl").call(context, runtime.contextOrFrameLookup(context, frame, "name"),runtime.contextOrFrameLookup(context, frame, "settings"),runtime.contextOrFrameLookup(context, frame, "pkgInfo"),runtime.memberLookup((l_element),"line")), env.opts.autoescape);
t_11 += "\" target=\"_blank\"><i class=\"fas fa-code-branch\"></i></a>\n\t";
;
}
t_11 += "\n";
;
frame = callerFrame;
return new runtime.SafeString(t_11);
});
context.addExport("srcDisplay");
context.setVariable("srcDisplay", macro_t_10);
output += "\n\n";
var macro_t_12 = runtime.makeMacro(
["method"], 
[], 
function (l_method, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("method", l_method);
var t_13 = "";frame = frame.push();
var t_16 = env.getFilter("annotationFilter").call(context, runtime.memberLookup((l_method),"annotations"));
if(t_16) {t_16 = runtime.fromIterator(t_16);
var t_15 = t_16.length;
for(var t_14=0; t_14 < t_16.length; t_14++) {
var t_17 = t_16[t_14];
frame.set("annotation", t_17);
frame.set("loop.index", t_14 + 1);
frame.set("loop.index0", t_14);
frame.set("loop.revindex", t_15 - t_14);
frame.set("loop.revindex0", t_15 - t_14 - 1);
frame.set("loop.first", t_14 === 0);
frame.set("loop.last", t_14 === t_15 - 1);
frame.set("loop.length", t_15);
t_13 += "\n\t\t<div class=\"annotation\">\n\t\t\t";
t_13 += runtime.suppressValue((lineno = 26, colno = 21, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printAnnotation"), "printAnnotation", context, [t_17])), env.opts.autoescape);
t_13 += "\n\t\t</div>\n \t";
;
}
}
frame = frame.pop();
t_13 += "\n";
;
frame = callerFrame;
return new runtime.SafeString(t_13);
});
context.addExport("printAnnotations");
context.setVariable("printAnnotations", macro_t_12);
output += "\n\n";
var macro_t_18 = runtime.makeMacro(
["annotation"], 
[], 
function (l_annotation, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("annotation", l_annotation);
var t_19 = "";t_19 += runtime.suppressValue((lineno = 32, colno = 13, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printType"), "printType", context, [l_annotation,runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
if(runtime.memberLookup((runtime.memberLookup((l_annotation),"params")),"length") > 0) {
t_19 += "(";
frame = frame.push();
var t_22 = runtime.memberLookup((l_annotation),"params");
if(t_22) {t_22 = runtime.fromIterator(t_22);
var t_21 = t_22.length;
for(var t_20=0; t_20 < t_22.length; t_20++) {
var t_23 = t_22[t_20];
frame.set("param", t_23);
frame.set("loop.index", t_20 + 1);
frame.set("loop.index0", t_20);
frame.set("loop.revindex", t_21 - t_20);
frame.set("loop.revindex0", t_21 - t_20 - 1);
frame.set("loop.first", t_20 === 0);
frame.set("loop.last", t_20 === t_21 - 1);
frame.set("loop.length", t_21);
if(runtime.memberLookup((t_23),"name") != "value") {
t_19 += runtime.suppressValue(runtime.memberLookup((t_23),"name"), env.opts.autoescape);
t_19 += "=";
;
}
t_19 += runtime.suppressValue((lineno = 39, colno = 16, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printValue"), "printValue", context, [runtime.memberLookup((t_23),"value")])), env.opts.autoescape);
if(!runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"last")) {
t_19 += ", ";
;
}
;
}
}
frame = frame.pop();
t_19 += ")";
;
}
;
frame = callerFrame;
return new runtime.SafeString(t_19);
});
context.addExport("printAnnotation");
context.setVariable("printAnnotation", macro_t_18);
output += "\n\n";
var macro_t_24 = runtime.makeMacro(
["value"], 
[], 
function (l_value, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("value", l_value);
var t_25 = "";if(runtime.memberLookup((l_value),"kind") == "string") {
t_25 += "\"";
t_25 += runtime.suppressValue(runtime.memberLookup((l_value),"label"), env.opts.autoescape);
t_25 += "\"";
;
}
else {
if(runtime.memberLookup((l_value),"kind") == "enum") {
t_25 += runtime.suppressValue(env.getFilter("enumValueRef").call(context, l_value,runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
;
}
else {
if(runtime.memberLookup((l_value),"kind") == "type") {
t_25 += runtime.suppressValue((lineno = 52, colno = 14, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printType"), "printType", context, [runtime.memberLookup((l_value),"type"),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
;
}
else {
if(runtime.memberLookup((l_value),"kind") == "annotation") {
t_25 += runtime.suppressValue((lineno = 54, colno = 20, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printAnnotation"), "printAnnotation", context, [l_value])), env.opts.autoescape);
;
}
else {
if(runtime.memberLookup((l_value),"kind") == "array") {
if(runtime.memberLookup((runtime.memberLookup((l_value),"values")),"length") == 1) {
t_25 += runtime.suppressValue((lineno = 57, colno = 16, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printValue"), "printValue", context, [runtime.memberLookup((runtime.memberLookup((l_value),"values")),0)])), env.opts.autoescape);
;
}
else {
t_25 += "[";
frame = frame.push();
var t_28 = runtime.memberLookup((l_value),"values");
if(t_28) {t_28 = runtime.fromIterator(t_28);
var t_27 = t_28.length;
for(var t_26=0; t_26 < t_28.length; t_26++) {
var t_29 = t_28[t_26];
frame.set("elem", t_29);
frame.set("loop.index", t_26 + 1);
frame.set("loop.index0", t_26);
frame.set("loop.revindex", t_27 - t_26);
frame.set("loop.revindex0", t_27 - t_26 - 1);
frame.set("loop.first", t_26 === 0);
frame.set("loop.last", t_26 === t_27 - 1);
frame.set("loop.length", t_27);
t_25 += runtime.suppressValue((lineno = 61, colno = 17, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printValue"), "printValue", context, [t_29])), env.opts.autoescape);
if(!runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"last")) {
t_25 += ", ";
;
}
;
}
}
frame = frame.pop();
t_25 += "]";
;
}
;
}
else {
t_25 += runtime.suppressValue(runtime.memberLookup((l_value),"label"), env.opts.autoescape);
;
}
;
}
;
}
;
}
;
}
;
frame = callerFrame;
return new runtime.SafeString(t_25);
});
context.addExport("printValue");
context.setVariable("printValue", macro_t_24);
output += "\n\n";
var macro_t_30 = runtime.makeMacro(
["method"], 
[], 
function (l_method, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("method", l_method);
var t_31 = "";t_31 += "\n\t";
if(runtime.memberLookup((l_method),"abstract") == "true") {
t_31 += "<code class=\"modifier\">abstract </code>";
;
}
t_31 += "<code class=\"method-name\">";
t_31 += runtime.suppressValue(env.getFilter("methodName").call(context, l_method), env.opts.autoescape);
t_31 += "</code>";
if(runtime.contextOrFrameLookup(context, frame, "kind") != "annotation") {
t_31 += "<code>(";
frame = frame.push();
var t_34 = runtime.memberLookup((l_method),"params");
if(t_34) {t_34 = runtime.fromIterator(t_34);
var t_33 = t_34.length;
for(var t_32=0; t_32 < t_34.length; t_32++) {
var t_35 = t_34[t_32];
frame.set("param", t_35);
frame.set("loop.index", t_32 + 1);
frame.set("loop.index0", t_32);
frame.set("loop.revindex", t_33 - t_32);
frame.set("loop.revindex0", t_33 - t_32 - 1);
frame.set("loop.first", t_32 === 0);
frame.set("loop.last", t_32 === t_33 - 1);
frame.set("loop.length", t_33);
if(!runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"first")) {
t_31 += ", ";
;
}
t_31 += runtime.suppressValue((lineno = 78, colno = 89, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printType"), "printType", context, [runtime.memberLookup((t_35),"type"),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
t_31 += " <span class=\"param\" title=\"";
t_31 += runtime.suppressValue(env.getFilter("docPlain").call(context, runtime.memberLookup((t_35),"title")), env.opts.autoescape);
t_31 += "\">";
t_31 += runtime.suppressValue(runtime.memberLookup((t_35),"name"), env.opts.autoescape);
t_31 += "</span>";
;
}
}
frame = frame.pop();
t_31 += ")</code>";
;
}
if(runtime.memberLookup((l_method),"return") && (runtime.memberLookup((runtime.memberLookup((runtime.memberLookup((l_method),"return")),"type")),"id") != "void")) {
t_31 += "<code>: </code><code class=\"return>\" title=\"";
t_31 += runtime.suppressValue(env.getFilter("docPlain").call(context, runtime.memberLookup((runtime.memberLookup((l_method),"return")),"title")), env.opts.autoescape);
t_31 += "\">";
t_31 += runtime.suppressValue((lineno = 82, colno = 96, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printType"), "printType", context, [runtime.memberLookup((runtime.memberLookup((l_method),"return")),"type"),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
t_31 += "</code>";
;
}
t_31 += "\n  \n\t";
t_31 += runtime.suppressValue((lineno = 85, colno = 14, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "srcDisplay"), "srcDisplay", context, [l_method])), env.opts.autoescape);
t_31 += "\n";
;
frame = callerFrame;
return new runtime.SafeString(t_31);
});
context.addExport("methodDisplay");
context.setVariable("methodDisplay", macro_t_30);
output += "\n\n";
var macro_t_36 = runtime.makeMacro(
["type", "current"], 
[], 
function (l_type, l_current, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("type", l_type);
frame.set("current", l_current);
var t_37 = "";if(runtime.memberLookup((l_type),"kind") == "primitive" || runtime.memberLookup((l_type),"kind") == "typevar" || runtime.memberLookup((l_type),"kind") == "wildcard") {
t_37 += runtime.suppressValue(runtime.memberLookup((l_type),"id"), env.opts.autoescape);
;
}
else {
if(env.getFilter("outerType").call(context, l_type) && runtime.memberLookup(((env.getFilter("outerType").call(context, l_type))),"id") != l_current) {
t_37 += runtime.suppressValue((lineno = 93, colno = 15, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printType"), "printType", context, [env.getFilter("outerType").call(context, l_type),l_current])), env.opts.autoescape);
t_37 += ".";
;
}
t_37 += runtime.suppressValue((lineno = 95, colno = 24, runtime.callWrap(runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "common")),"printTypeRef"), "common[\"printTypeRef\"]", context, [l_type,l_current])), env.opts.autoescape);
;
}
if(runtime.memberLookup((runtime.memberLookup((l_type),"args")),"length") > 0) {
t_37 += "&lt;";
frame = frame.push();
var t_40 = runtime.memberLookup((l_type),"args");
if(t_40) {t_40 = runtime.fromIterator(t_40);
var t_39 = t_40.length;
for(var t_38=0; t_38 < t_40.length; t_38++) {
var t_41 = t_40[t_38];
frame.set("arg", t_41);
frame.set("loop.index", t_38 + 1);
frame.set("loop.index0", t_38);
frame.set("loop.revindex", t_39 - t_38);
frame.set("loop.revindex0", t_39 - t_38 - 1);
frame.set("loop.first", t_38 === 0);
frame.set("loop.last", t_38 === t_39 - 1);
frame.set("loop.length", t_39);
t_37 += runtime.suppressValue((lineno = 101, colno = 15, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printType"), "printType", context, [t_41,l_current])), env.opts.autoescape);
if(!runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"last")) {
t_37 += ",";
;
}
;
}
}
frame = frame.pop();
t_37 += "&gt;";
;
}
if(runtime.memberLookup((runtime.memberLookup((l_type),"extendsBounds")),"length") > 0) {
t_37 += "\n\t\textends\n\t\t";
frame = frame.push();
var t_44 = runtime.memberLookup((l_type),"extendsBounds");
if(t_44) {t_44 = runtime.fromIterator(t_44);
var t_43 = t_44.length;
for(var t_42=0; t_42 < t_44.length; t_42++) {
var t_45 = t_44[t_42];
frame.set("bound", t_45);
frame.set("loop.index", t_42 + 1);
frame.set("loop.index0", t_42);
frame.set("loop.revindex", t_43 - t_42);
frame.set("loop.revindex0", t_43 - t_42 - 1);
frame.set("loop.first", t_42 === 0);
frame.set("loop.last", t_42 === t_43 - 1);
frame.set("loop.length", t_43);
t_37 += runtime.suppressValue((lineno = 110, colno = 15, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printType"), "printType", context, [t_45,l_current])), env.opts.autoescape);
if(!runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"last")) {
t_37 += ", ";
;
}
;
}
}
frame = frame.pop();
;
}
if(runtime.memberLookup((runtime.memberLookup((l_type),"superBounds")),"length") > 0) {
t_37 += "\n\t\tsuper\n\t\t";
frame = frame.push();
var t_48 = runtime.memberLookup((l_type),"superBounds");
if(t_48) {t_48 = runtime.fromIterator(t_48);
var t_47 = t_48.length;
for(var t_46=0; t_46 < t_48.length; t_46++) {
var t_49 = t_48[t_46];
frame.set("bound", t_49);
frame.set("loop.index", t_46 + 1);
frame.set("loop.index0", t_46);
frame.set("loop.revindex", t_47 - t_46);
frame.set("loop.revindex0", t_47 - t_46 - 1);
frame.set("loop.first", t_46 === 0);
frame.set("loop.last", t_46 === t_47 - 1);
frame.set("loop.length", t_47);
t_37 += "\n\t\t\t";
t_37 += runtime.suppressValue((lineno = 118, colno = 15, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printType"), "printType", context, [t_49,l_current])), env.opts.autoescape);
if(!runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"last")) {
t_37 += ",";
;
}
;
}
}
frame = frame.pop();
;
}
t_37 += runtime.suppressValue((lineno = 123, colno = 14, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printArray"), "printArray", context, [runtime.memberLookup((l_type),"dim")])), env.opts.autoescape);
;
frame = callerFrame;
return new runtime.SafeString(t_37);
});
context.addExport("printType");
context.setVariable("printType", macro_t_36);
output += "\n\n";
var macro_t_50 = runtime.makeMacro(
["dim"], 
[], 
function (l_dim, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("dim", l_dim);
var t_51 = "";if(l_dim > 0) {
t_51 += "[]";
;
}
;
frame = callerFrame;
return new runtime.SafeString(t_51);
});
context.addExport("printArray");
context.setVariable("printArray", macro_t_50);
output += "\n\n";
var macro_t_52 = runtime.makeMacro(
["method"], 
[], 
function (l_method, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("method", l_method);
var t_53 = "";t_53 += "\n  <dt id=\"";
t_53 += runtime.suppressValue(runtime.memberLookup((l_method),"id"), env.opts.autoescape);
t_53 += "\">\n\t";
t_53 += runtime.suppressValue((lineno = 134, colno = 17, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "methodDisplay"), "methodDisplay", context, [l_method])), env.opts.autoescape);
t_53 += "\n  </dt>\n  <dd>\n\t";
t_53 += runtime.suppressValue((lineno = 137, colno = 20, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printAnnotations"), "printAnnotations", context, [l_method])), env.opts.autoescape);
t_53 += "\n\t\n\t";
if(runtime.memberLookup((l_method),"defaultValue")) {
t_53 += "\n\t\t<div class=\"annotation\">\n\t\t\tDefault value: ";
t_53 += runtime.suppressValue((lineno = 141, colno = 31, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printValue"), "printValue", context, [runtime.memberLookup((runtime.memberLookup((l_method),"defaultValue")),"value")])), env.opts.autoescape);
t_53 += "\n\t\t</div>\n\t";
;
}
t_53 += "\n\t\n\t";
t_53 += runtime.suppressValue((lineno = 145, colno = 12, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printDoc"), "printDoc", context, [l_method])), env.opts.autoescape);
t_53 += "\n\t\n  \t";
if(runtime.memberLookup(((env.getFilter("withDoc").call(context, runtime.memberLookup((l_method),"params")))),"length") > 0) {
t_53 += "\n  \t<div class=\"parameters\">\n  \t\t<b>Parameters</b>\n  \t<table>\n\t  \t";
frame = frame.push();
var t_56 = env.getFilter("withDoc").call(context, runtime.memberLookup((l_method),"params"));
if(t_56) {t_56 = runtime.fromIterator(t_56);
var t_55 = t_56.length;
for(var t_54=0; t_54 < t_56.length; t_54++) {
var t_57 = t_56[t_54];
frame.set("param", t_57);
frame.set("loop.index", t_54 + 1);
frame.set("loop.index0", t_54);
frame.set("loop.revindex", t_55 - t_54);
frame.set("loop.revindex0", t_55 - t_54 - 1);
frame.set("loop.first", t_54 === 0);
frame.set("loop.last", t_54 === t_55 - 1);
frame.set("loop.length", t_55);
t_53 += "\n\t  \t<tr>\n\t  \t<td><code>";
t_53 += runtime.suppressValue(runtime.memberLookup((t_57),"name"), env.opts.autoescape);
t_53 += "</code></td>\n\t  \t<td> - </td>\n\t  \t<td class=\"doc\">\n\t\t  \t";
t_53 += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((t_57),"title"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
t_53 += "\n\t\t  \t";
t_53 += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((t_57),"doc"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
t_53 += "\n\t  \t</td>\n\t  \t</tr>\n\t  \t";
;
}
}
frame = frame.pop();
t_53 += "\n  \t</table>\n  \t</div>\n  \t";
;
}
t_53 += "\n  \t\n\t";
if(runtime.memberLookup((runtime.memberLookup((l_method),"return")),"doc")) {
t_53 += "\n\t<p>\n\t<b>Returns</b>: ";
t_53 += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((runtime.memberLookup((l_method),"return")),"title"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
t_53 += " ";
t_53 += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((runtime.memberLookup((l_method),"return")),"doc"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
t_53 += "\n\t</p>  \t\n  \t";
;
}
t_53 += "\n\n\t";
t_53 += runtime.suppressValue((lineno = 171, colno = 20, runtime.callWrap(runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "common")),"printSees"), "common[\"printSees\"]", context, [runtime.memberLookup((l_method),"sees"),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
t_53 += "\n  </dd>\n";
;
frame = callerFrame;
return new runtime.SafeString(t_53);
});
context.addExport("printMethod");
context.setVariable("printMethod", macro_t_52);
output += "\n\n<div class=\"package\">\nPackage <code>";
output += runtime.suppressValue(env.getFilter("packageRef").call(context, env.getFilter("packageName").call(context, runtime.contextOrFrameLookup(context, frame, "name"))), env.opts.autoescape);
output += "</code>\n</div>\n\n<h1>\n";
if(runtime.contextOrFrameLookup(context, frame, "abstract")) {
output += "\nAbstract\n";
;
}
output += "\n\n";
output += runtime.suppressValue(env.getFilter("capitalize").call(context, runtime.contextOrFrameLookup(context, frame, "kind")), env.opts.autoescape);
output += " <code>";
output += runtime.suppressValue(env.getFilter("typeName").call(context, runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
output += "</code>";
if(runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "args")),"length") > 0) {
output += "<code>&lt;</code>";
frame = frame.push();
var t_60 = runtime.contextOrFrameLookup(context, frame, "args");
if(t_60) {t_60 = runtime.fromIterator(t_60);
var t_59 = t_60.length;
for(var t_58=0; t_58 < t_60.length; t_58++) {
var t_61 = t_60[t_58];
frame.set("arg", t_61);
frame.set("loop.index", t_58 + 1);
frame.set("loop.index0", t_58);
frame.set("loop.revindex", t_59 - t_58);
frame.set("loop.revindex0", t_59 - t_58 - 1);
frame.set("loop.first", t_58 === 0);
frame.set("loop.last", t_58 === t_59 - 1);
frame.set("loop.length", t_59);
output += "<code>";
output += runtime.suppressValue((lineno = 189, colno = 20, runtime.callWrap(macro_t_36, "printType", context, [t_61,runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += "</code>";
if(!runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"last")) {
output += ", ";
;
}
;
}
}
frame = frame.pop();
output += "<code>&gt;</code>";
;
}
output += "\n\n";
if(env.getFilter("containingTypeName").call(context, runtime.contextOrFrameLookup(context, frame, "name"))) {
output += "\n\t(<code>";
output += runtime.suppressValue(env.getFilter("typeNameRef").call(context, env.getFilter("containingTypeName").call(context, runtime.contextOrFrameLookup(context, frame, "name"))), env.opts.autoescape);
output += "</code>)\n";
;
}
output += "\n\n<a class=\"link\" title=\"Link to this type.\" href=\"#";
output += runtime.suppressValue(runtime.contextOrFrameLookup(context, frame, "name"), env.opts.autoescape);
output += "\"><i class=\"fas fa-link\"></i></a>\n";
if(runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "settings")),"showSrcLink") && runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "pkgInfo")),"srcLink")) {
output += "\n<a class=\"link\" title=\"Link to the source code.\" href=\"";
output += runtime.suppressValue(env.getFilter("srcUrl").call(context, runtime.contextOrFrameLookup(context, frame, "name"),runtime.contextOrFrameLookup(context, frame, "settings"),runtime.contextOrFrameLookup(context, frame, "pkgInfo"),runtime.contextOrFrameLookup(context, frame, "line")), env.opts.autoescape);
output += "\" target=\"_blank\"><i class=\"fas fa-code-branch\"></i></a>\n";
;
}
output += "\n</h1>\n\n<div class=\"section\">\n\n";
frame = frame.push();
var t_64 = runtime.contextOrFrameLookup(context, frame, "annotations");
if(t_64) {t_64 = runtime.fromIterator(t_64);
var t_63 = t_64.length;
for(var t_62=0; t_62 < t_64.length; t_62++) {
var t_65 = t_64[t_62];
frame.set("annotation", t_65);
frame.set("loop.index", t_62 + 1);
frame.set("loop.index0", t_62);
frame.set("loop.revindex", t_63 - t_62);
frame.set("loop.revindex0", t_63 - t_62 - 1);
frame.set("loop.first", t_62 === 0);
frame.set("loop.last", t_62 === t_63 - 1);
frame.set("loop.length", t_63);
output += "\n\t<div class=\"annotation\">\n\t\t";
output += runtime.suppressValue((lineno = 209, colno = 20, runtime.callWrap(macro_t_18, "printAnnotation", context, [t_65])), env.opts.autoescape);
output += "\n\t</div>\n";
;
}
}
frame = frame.pop();
output += "\n\n<p class=\"title\">";
output += runtime.suppressValue(env.getFilter("doc").call(context, runtime.contextOrFrameLookup(context, frame, "title"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
output += "</p>\n";
output += runtime.suppressValue(env.getFilter("doc").call(context, runtime.contextOrFrameLookup(context, frame, "doc"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
output += "\n\n";
if(runtime.contextOrFrameLookup(context, frame, "extends") && (runtime.contextOrFrameLookup(context, frame, "kind") != "enum")) {
output += "\n<p class=\"extends\">\nExtends <code>";
output += runtime.suppressValue((lineno = 218, colno = 26, runtime.callWrap(macro_t_36, "printType", context, [runtime.contextOrFrameLookup(context, frame, "extends"),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += "</code>\n</p>\n";
;
}
output += "\n\n";
if(runtime.contextOrFrameLookup(context, frame, "implements") && (runtime.contextOrFrameLookup(context, frame, "kind") != "annotation")) {
output += "\n<p class=\"implements\">\n\tImplements ";
frame = frame.push();
var t_68 = runtime.contextOrFrameLookup(context, frame, "implements");
if(t_68) {t_68 = runtime.fromIterator(t_68);
var t_67 = t_68.length;
for(var t_66=0; t_66 < t_68.length; t_66++) {
var t_69 = t_68[t_66];
frame.set("type", t_69);
frame.set("loop.index", t_66 + 1);
frame.set("loop.index0", t_66);
frame.set("loop.revindex", t_67 - t_66);
frame.set("loop.revindex0", t_67 - t_66 - 1);
frame.set("loop.first", t_66 === 0);
frame.set("loop.last", t_66 === t_67 - 1);
frame.set("loop.length", t_67);
output += "<code>";
output += runtime.suppressValue((lineno = 224, colno = 58, runtime.callWrap(macro_t_36, "printType", context, [t_69,runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += "</code>";
if(!runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"last")) {
output += ", ";
;
}
;
}
}
frame = frame.pop();
output += ".\n</p>\n";
;
}
output += "\n\n";
if(env.getFilter("getConfigInterface").call(context, runtime.contextOrFrameLookup(context, frame, "constructors"))) {
output += "\n<p class=\"configuration\">\n\tConfiguration options: <code>";
output += runtime.suppressValue((lineno = 230, colno = 42, runtime.callWrap(macro_t_36, "printType", context, [env.getFilter("getConfigInterface").call(context, runtime.contextOrFrameLookup(context, frame, "constructors"),runtime.contextOrFrameLookup(context, frame, "name")),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += "</code>\n</p>\n";
;
}
output += "\n\n";
if(runtime.memberLookup((runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "xref")),"specializations")),"length") > 0) {
output += "\n<p class=\"specializations\">\nDirect specializations:\n";
frame = frame.push();
var t_72 = runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "xref")),"specializations");
if(t_72) {t_72 = runtime.fromIterator(t_72);
var t_71 = t_72.length;
for(var t_70=0; t_70 < t_72.length; t_70++) {
var t_73 = t_72[t_70];
frame.set("usage", t_73);
frame.set("loop.index", t_70 + 1);
frame.set("loop.index0", t_70);
frame.set("loop.revindex", t_71 - t_70);
frame.set("loop.revindex0", t_71 - t_70 - 1);
frame.set("loop.first", t_70 === 0);
frame.set("loop.last", t_70 === t_71 - 1);
frame.set("loop.length", t_71);
output += "\n\n";
if(runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"index") == 6) {
output += "\n<span class=\"further-reading collapsed\">\n<span class=\"when-expanded\">\n";
;
}
output += "\n<code>";
output += runtime.suppressValue((lineno = 243, colno = 18, runtime.callWrap(macro_t_36, "printType", context, [{"id": t_73},runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += "</code>";
if(!runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"last")) {
output += ", ";
;
}
output += "\n\n";
if(runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"last") && runtime.memberLookup((runtime.contextOrFrameLookup(context, frame, "loop")),"length") >= 6) {
output += "\n</span>\n<button onclick=\"$(this.parentNode).toggleClass('expanded collapsed');\">\n<i class=\"when-collapsed fas fa-ellipsis-h\"></i>\n<i class=\"when-expanded fas fa-angle-up\"></i>\n</button>\n</span>\n";
;
}
output += "\n";
;
}
}
frame = frame.pop();
output += "\n</p>\n";
;
}
output += "\n\n";
output += runtime.suppressValue((lineno = 257, colno = 19, runtime.callWrap(runtime.memberLookup((t_1),"printSees"), "common[\"printSees\"]", context, [runtime.contextOrFrameLookup(context, frame, "sees"),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += "\n</div>\n\n";
if(runtime.contextOrFrameLookup(context, frame, "kind") != "config") {
output += "\n";
if(runtime.memberLookup(((env.getFilter("onlyStatic").call(context, runtime.contextOrFrameLookup(context, frame, "fields")))),"length") > 0) {
output += "\n<h2>Constants</h2>\n\n<div class=\"section\">\n<dl>\n";
frame = frame.push();
var t_76 = env.getFilter("onlyStatic").call(context, runtime.contextOrFrameLookup(context, frame, "fields"));
if(t_76) {t_76 = runtime.fromIterator(t_76);
var t_75 = t_76.length;
for(var t_74=0; t_74 < t_76.length; t_74++) {
var t_77 = t_76[t_74];
frame.set("field", t_77);
frame.set("loop.index", t_74 + 1);
frame.set("loop.index0", t_74);
frame.set("loop.revindex", t_75 - t_74);
frame.set("loop.revindex0", t_75 - t_74 - 1);
frame.set("loop.first", t_74 === 0);
frame.set("loop.last", t_74 === t_75 - 1);
frame.set("loop.length", t_75);
output += "\n  <dt id=\"";
output += runtime.suppressValue(runtime.memberLookup((t_77),"id"), env.opts.autoescape);
output += "\">\n  \t";
if((runtime.contextOrFrameLookup(context, frame, "kind") != "enum")) {
output += "<code class=\"field-type\">";
output += runtime.suppressValue((lineno = 269, colno = 40, runtime.callWrap(macro_t_36, "printType", context, [runtime.memberLookup((t_77),"type"),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += " </code>";
;
}
output += "<code class=\"field-name\">";
output += runtime.suppressValue(runtime.memberLookup((t_77),"id"), env.opts.autoescape);
output += "</code>";
if(runtime.memberLookup((t_77),"value")) {
output += "<code> = </code><code class=\"field-value\">";
output += runtime.suppressValue(runtime.memberLookup((t_77),"value"), env.opts.autoescape);
output += "</code>";
;
}
output += "\n\n\t";
output += runtime.suppressValue((lineno = 278, colno = 14, runtime.callWrap(macro_t_10, "srcDisplay", context, [t_77])), env.opts.autoescape);
output += "\n  </dt>\n  <dd>\n\t";
output += runtime.suppressValue((lineno = 281, colno = 12, runtime.callWrap(macro_t_8, "printDoc", context, [t_77])), env.opts.autoescape);
output += "\n  </dd>\n";
;
}
}
frame = frame.pop();
output += "\n</dl>\n</div>\n";
;
}
output += "\n";
;
}
output += "\n\n";
if(runtime.memberLookup(((env.getFilter("nonStatic").call(context, runtime.contextOrFrameLookup(context, frame, "fields")))),"length") > 0) {
output += "\n<h2>Fields</h2>\n<div class=\"section\">\n<dl>\n";
frame = frame.push();
var t_80 = env.getFilter("nonStatic").call(context, runtime.contextOrFrameLookup(context, frame, "fields"));
if(t_80) {t_80 = runtime.fromIterator(t_80);
var t_79 = t_80.length;
for(var t_78=0; t_78 < t_80.length; t_78++) {
var t_81 = t_80[t_78];
frame.set("field", t_81);
frame.set("loop.index", t_78 + 1);
frame.set("loop.index0", t_78);
frame.set("loop.revindex", t_79 - t_78);
frame.set("loop.revindex0", t_79 - t_78 - 1);
frame.set("loop.first", t_78 === 0);
frame.set("loop.last", t_78 === t_79 - 1);
frame.set("loop.length", t_79);
output += "\n  <dt id=\"";
output += runtime.suppressValue(runtime.memberLookup((t_81),"id"), env.opts.autoescape);
output += "\">\n  \t<code>";
output += runtime.suppressValue((lineno = 295, colno = 21, runtime.callWrap(macro_t_36, "printType", context, [runtime.memberLookup((t_81),"type"),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += " </code><code class=\"field-name\">";
output += runtime.suppressValue(runtime.memberLookup((t_81),"id"), env.opts.autoescape);
output += "</code>\n  \n  \t";
output += runtime.suppressValue((lineno = 297, colno = 16, runtime.callWrap(macro_t_10, "srcDisplay", context, [t_81])), env.opts.autoescape);
output += "\n  </dt>\n  <dd>\n\t";
output += runtime.suppressValue((lineno = 300, colno = 12, runtime.callWrap(macro_t_8, "printDoc", context, [t_81])), env.opts.autoescape);
output += "\n  </dd>\n";
;
}
}
frame = frame.pop();
output += "\n</dl>\n</div>\n";
;
}
output += "\n\n";
if(runtime.memberLookup(((env.getFilter("onlyPublic").call(context, env.getFilter("withType").call(context, env.getFilter("onlyStatic").call(context, runtime.contextOrFrameLookup(context, frame, "methods")),runtime.contextOrFrameLookup(context, frame, "name"))))),"length") > 0) {
output += "\n<h2>Factory Methods</h2>\n<div class=\"section\">\n<dl>\n\t";
frame = frame.push();
var t_84 = env.getFilter("sortMethods").call(context, env.getFilter("onlyPublic").call(context, env.getFilter("withType").call(context, env.getFilter("onlyStatic").call(context, runtime.contextOrFrameLookup(context, frame, "methods")),runtime.contextOrFrameLookup(context, frame, "name"))));
if(t_84) {t_84 = runtime.fromIterator(t_84);
var t_83 = t_84.length;
for(var t_82=0; t_82 < t_84.length; t_82++) {
var t_85 = t_84[t_82];
frame.set("method", t_85);
frame.set("loop.index", t_82 + 1);
frame.set("loop.index0", t_82);
frame.set("loop.revindex", t_83 - t_82);
frame.set("loop.revindex0", t_83 - t_82 - 1);
frame.set("loop.first", t_82 === 0);
frame.set("loop.last", t_82 === t_83 - 1);
frame.set("loop.length", t_83);
output += "\n\t\t";
output += runtime.suppressValue((lineno = 312, colno = 16, runtime.callWrap(macro_t_52, "printMethod", context, [t_85])), env.opts.autoescape);
output += "\n\t";
;
}
}
frame = frame.pop();
output += "\n</dl>\n</div>\n";
;
}
output += "\n\n";
if(runtime.contextOrFrameLookup(context, frame, "constructors")) {
output += "\n<h2>Constructors</h2>\n<div class=\"section\">\n<dl>\n";
frame = frame.push();
var t_88 = runtime.contextOrFrameLookup(context, frame, "constructors");
if(t_88) {t_88 = runtime.fromIterator(t_88);
var t_87 = t_88.length;
for(var t_86=0; t_86 < t_88.length; t_86++) {
var t_89 = t_88[t_86];
frame.set("method", t_89);
frame.set("loop.index", t_86 + 1);
frame.set("loop.index0", t_86);
frame.set("loop.revindex", t_87 - t_86);
frame.set("loop.revindex0", t_87 - t_86 - 1);
frame.set("loop.first", t_86 === 0);
frame.set("loop.last", t_86 === t_87 - 1);
frame.set("loop.length", t_87);
output += "\n  <dt id=\"";
output += runtime.suppressValue(runtime.memberLookup((t_89),"id"), env.opts.autoescape);
output += "\">\n\t";
output += runtime.suppressValue((lineno = 324, colno = 17, runtime.callWrap(macro_t_30, "methodDisplay", context, [t_89])), env.opts.autoescape);
output += "\n  </dt>\n  <dd>\n  \t<p>";
output += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((t_89),"title"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
output += "</p>\n  \t\n  \t";
output += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((t_89),"doc"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
output += "\n  </dd>\n";
;
}
}
frame = frame.pop();
output += "\n</dl>\n</div>\n";
;
}
output += "\n\n";
if(runtime.contextOrFrameLookup(context, frame, "kind") == "config") {
output += "\n\t";
if(runtime.memberLookup(((env.getFilter("nonSetters").call(context, env.getFilter("nonDefaults").call(context, env.getFilter("onlyPublic").call(context, env.getFilter("nonStatic").call(context, env.getFilter("nonOverrides").call(context, runtime.contextOrFrameLookup(context, frame, "allMethods")))))))),"length") > 0) {
output += "\n\t<h2>Configuration Options</h2>\n\t<div class=\"section\">\n\t<dl>\n\t\t";
frame = frame.push();
var t_92 = env.getFilter("nonSetters").call(context, env.getFilter("nonDefaults").call(context, env.getFilter("onlyPublic").call(context, env.getFilter("nonStatic").call(context, env.getFilter("nonOverrides").call(context, runtime.contextOrFrameLookup(context, frame, "allMethods"))))));
if(t_92) {t_92 = runtime.fromIterator(t_92);
var t_91 = t_92.length;
for(var t_90=0; t_90 < t_92.length; t_90++) {
var t_93 = t_92[t_90];
frame.set("method", t_93);
frame.set("loop.index", t_90 + 1);
frame.set("loop.index0", t_90);
frame.set("loop.revindex", t_91 - t_90);
frame.set("loop.revindex0", t_91 - t_90 - 1);
frame.set("loop.first", t_90 === 0);
frame.set("loop.last", t_90 === t_91 - 1);
frame.set("loop.length", t_91);
output += "\n\t\t\t<dt id=\"";
output += runtime.suppressValue(runtime.memberLookup((t_93),"id"), env.opts.autoescape);
output += "\">\n\t\t\t\t<code class=\"option-name\">";
output += runtime.suppressValue(env.getFilter("configName").call(context, t_93), env.opts.autoescape);
output += ": </code><code class=\"return\">";
output += runtime.suppressValue((lineno = 343, colno = 97, runtime.callWrap(macro_t_36, "printType", context, [runtime.memberLookup((runtime.memberLookup((t_93),"return")),"type"),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += "</code>\n\t\t\t\t";
output += runtime.suppressValue((lineno = 344, colno = 17, runtime.callWrap(macro_t_10, "srcDisplay", context, [t_93])), env.opts.autoescape);
output += "\n\t\t\t</dt>\n\t\t\t<dd>\n\t\t\t\t";
output += runtime.suppressValue((lineno = 347, colno = 23, runtime.callWrap(macro_t_12, "printAnnotations", context, [t_93])), env.opts.autoescape);
output += "\n\t\t\t\t\n\t\t\t\t";
output += runtime.suppressValue((lineno = 349, colno = 15, runtime.callWrap(macro_t_8, "printDoc", context, [t_93])), env.opts.autoescape);
output += "\n\t\t\t\t\n\t\t\t\t";
if(runtime.memberLookup((runtime.memberLookup((t_93),"return")),"doc")) {
output += "\n\t\t\t\t\t";
output += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((runtime.memberLookup((t_93),"return")),"title"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
output += " ";
output += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((runtime.memberLookup((t_93),"return")),"doc"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
output += "\n\t\t\t  \t";
;
}
output += "\n\t\t\t\n\t\t\t\t";
output += runtime.suppressValue((lineno = 355, colno = 23, runtime.callWrap(runtime.memberLookup((t_1),"printSees"), "common[\"printSees\"]", context, [runtime.memberLookup((t_93),"sees"),runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += "\n\t\t\t</dd>\n\t\t";
;
}
}
frame = frame.pop();
output += "\n\t</dl>\n\t</div>\n\t";
;
}
output += "\n";
;
}
else {
output += "\n\t";
if(runtime.memberLookup(((env.getFilter("nonDefaults").call(context, env.getFilter("onlyPublic").call(context, env.getFilter("nonStatic").call(context, env.getFilter("nonOverrides").call(context, runtime.contextOrFrameLookup(context, frame, "methods"))))))),"length") > 0) {
output += "\n\t";
if(runtime.contextOrFrameLookup(context, frame, "kind") == "annotation") {
output += "\n\t<h2>Arguments</h2>\n\t";
;
}
else {
output += "\n\t<h2>Methods</h2>\n\t";
;
}
output += "\n\t<div class=\"section\">\n\t<dl>\n\t\t";
frame = frame.push();
var t_96 = env.getFilter("sortMethods").call(context, env.getFilter("nonDefaults").call(context, env.getFilter("onlyPublic").call(context, env.getFilter("nonStatic").call(context, env.getFilter("nonOverrides").call(context, runtime.contextOrFrameLookup(context, frame, "methods"))))));
if(t_96) {t_96 = runtime.fromIterator(t_96);
var t_95 = t_96.length;
for(var t_94=0; t_94 < t_96.length; t_94++) {
var t_97 = t_96[t_94];
frame.set("method", t_97);
frame.set("loop.index", t_94 + 1);
frame.set("loop.index0", t_94);
frame.set("loop.revindex", t_95 - t_94);
frame.set("loop.revindex0", t_95 - t_94 - 1);
frame.set("loop.first", t_94 === 0);
frame.set("loop.last", t_94 === t_95 - 1);
frame.set("loop.length", t_95);
output += "\n\t\t\t";
output += runtime.suppressValue((lineno = 371, colno = 17, runtime.callWrap(macro_t_52, "printMethod", context, [t_97])), env.opts.autoescape);
output += "\n\t\t";
;
}
}
frame = frame.pop();
output += "\n\t</dl>\n\t</div>\n\t";
;
}
output += "\n";
;
}
output += "\n\n";
if(runtime.memberLookup(((env.getFilter("onlyDefaults").call(context, env.getFilter("onlyPublic").call(context, env.getFilter("nonStatic").call(context, env.getFilter("nonOverrides").call(context, runtime.contextOrFrameLookup(context, frame, "methods"))))))),"length") > 0) {
output += "\n<h2>Default Methods</h2>\n<div class=\"section\">\n<dl>\n\t";
frame = frame.push();
var t_100 = env.getFilter("sortMethods").call(context, env.getFilter("onlyDefaults").call(context, env.getFilter("onlyPublic").call(context, env.getFilter("nonStatic").call(context, env.getFilter("nonOverrides").call(context, runtime.contextOrFrameLookup(context, frame, "methods"))))));
if(t_100) {t_100 = runtime.fromIterator(t_100);
var t_99 = t_100.length;
for(var t_98=0; t_98 < t_100.length; t_98++) {
var t_101 = t_100[t_98];
frame.set("method", t_101);
frame.set("loop.index", t_98 + 1);
frame.set("loop.index0", t_98);
frame.set("loop.revindex", t_99 - t_98);
frame.set("loop.revindex0", t_99 - t_98 - 1);
frame.set("loop.first", t_98 === 0);
frame.set("loop.last", t_98 === t_99 - 1);
frame.set("loop.length", t_99);
output += "\n\t\t";
output += runtime.suppressValue((lineno = 383, colno = 16, runtime.callWrap(macro_t_52, "printMethod", context, [t_101])), env.opts.autoescape);
output += "\n\t";
;
}
}
frame = frame.pop();
output += "\n</dl>\n</div>\n";
;
}
output += "\n\n";
if(runtime.memberLookup(((env.getFilter("onlyPublic").call(context, env.getFilter("withoutType").call(context, env.getFilter("onlyStatic").call(context, runtime.contextOrFrameLookup(context, frame, "methods")),runtime.contextOrFrameLookup(context, frame, "name"))))),"length") > 0) {
output += "\n<h2>Static Methods</h2>\n<div class=\"section\">\n<dl>\n\t";
frame = frame.push();
var t_104 = env.getFilter("sortMethods").call(context, env.getFilter("onlyPublic").call(context, env.getFilter("withoutType").call(context, env.getFilter("onlyStatic").call(context, runtime.contextOrFrameLookup(context, frame, "methods")),runtime.contextOrFrameLookup(context, frame, "name"))));
if(t_104) {t_104 = runtime.fromIterator(t_104);
var t_103 = t_104.length;
for(var t_102=0; t_102 < t_104.length; t_102++) {
var t_105 = t_104[t_102];
frame.set("method", t_105);
frame.set("loop.index", t_102 + 1);
frame.set("loop.index0", t_102);
frame.set("loop.revindex", t_103 - t_102);
frame.set("loop.revindex0", t_103 - t_102 - 1);
frame.set("loop.first", t_102 === 0);
frame.set("loop.last", t_102 === t_103 - 1);
frame.set("loop.length", t_103);
output += "\n\t\t";
output += runtime.suppressValue((lineno = 394, colno = 16, runtime.callWrap(macro_t_52, "printMethod", context, [t_105])), env.opts.autoescape);
output += "\n\t";
;
}
}
frame = frame.pop();
output += "\n</dl>\n</div>\n";
;
}
output += "\n\n";
if(runtime.memberLookup(((env.getFilter("nonPublic").call(context, env.getFilter("nonStatic").call(context, env.getFilter("nonOverrides").call(context, runtime.contextOrFrameLookup(context, frame, "methods")))))),"length") > 0) {
output += "\n<h2>Protected Methods</h2>\n<div class=\"section\">\n<dl>\n\t";
frame = frame.push();
var t_108 = env.getFilter("sortMethods").call(context, env.getFilter("nonPublic").call(context, env.getFilter("nonStatic").call(context, env.getFilter("nonOverrides").call(context, runtime.contextOrFrameLookup(context, frame, "methods")))));
if(t_108) {t_108 = runtime.fromIterator(t_108);
var t_107 = t_108.length;
for(var t_106=0; t_106 < t_108.length; t_106++) {
var t_109 = t_108[t_106];
frame.set("method", t_109);
frame.set("loop.index", t_106 + 1);
frame.set("loop.index0", t_106);
frame.set("loop.revindex", t_107 - t_106);
frame.set("loop.revindex0", t_107 - t_106 - 1);
frame.set("loop.first", t_106 === 0);
frame.set("loop.last", t_106 === t_107 - 1);
frame.set("loop.length", t_107);
output += "\n\t\t";
output += runtime.suppressValue((lineno = 405, colno = 16, runtime.callWrap(macro_t_52, "printMethod", context, [t_109])), env.opts.autoescape);
output += "\n\t";
;
}
}
frame = frame.pop();
output += "\n</dl>\n</div>\n";
;
}
output += "\n\n";
if(runtime.memberLookup(((env.getFilter("nonPublic").call(context, env.getFilter("onlyStatic").call(context, runtime.contextOrFrameLookup(context, frame, "methods"))))),"length") > 0) {
output += "\n<h2>Protected Static Methods</h2>\n<div class=\"section\">\n<dl>\n\t";
frame = frame.push();
var t_112 = env.getFilter("sortMethods").call(context, env.getFilter("nonPublic").call(context, env.getFilter("onlyStatic").call(context, runtime.contextOrFrameLookup(context, frame, "methods"))));
if(t_112) {t_112 = runtime.fromIterator(t_112);
var t_111 = t_112.length;
for(var t_110=0; t_110 < t_112.length; t_110++) {
var t_113 = t_112[t_110];
frame.set("method", t_113);
frame.set("loop.index", t_110 + 1);
frame.set("loop.index0", t_110);
frame.set("loop.revindex", t_111 - t_110);
frame.set("loop.revindex0", t_111 - t_110 - 1);
frame.set("loop.first", t_110 === 0);
frame.set("loop.last", t_110 === t_111 - 1);
frame.set("loop.length", t_111);
output += "\n\t\t";
output += runtime.suppressValue((lineno = 416, colno = 16, runtime.callWrap(macro_t_52, "printMethod", context, [t_113])), env.opts.autoescape);
output += "\n\t";
;
}
}
frame = frame.pop();
output += "\n</dl>\n</div>\n";
;
}
output += "\n\n";
if(runtime.contextOrFrameLookup(context, frame, "contents")) {
output += "\n<h2>Inner types</h2>\n<div class=\"section\">\n<ul>\n";
frame = frame.push();
var t_116 = runtime.contextOrFrameLookup(context, frame, "contents");
if(t_116) {t_116 = runtime.fromIterator(t_116);
var t_115 = t_116.length;
for(var t_114=0; t_114 < t_116.length; t_114++) {
var t_117 = t_116[t_114];
frame.set("type", t_117);
frame.set("loop.index", t_114 + 1);
frame.set("loop.index0", t_114);
frame.set("loop.revindex", t_115 - t_114);
frame.set("loop.revindex0", t_115 - t_114 - 1);
frame.set("loop.first", t_114 === 0);
frame.set("loop.last", t_114 === t_115 - 1);
frame.set("loop.length", t_115);
output += "\n  <li><code>";
output += runtime.suppressValue((lineno = 427, colno = 24, runtime.callWrap(macro_t_36, "printType", context, [t_117,runtime.contextOrFrameLookup(context, frame, "name")])), env.opts.autoescape);
output += "</code></li>\n";
;
}
}
frame = frame.pop();
output += "\n</ul>\n</div>\n";
;
}
output += "\n</div>\n\n<div class=\"footer\">\nTopLogic API and implementations © Copyright by Business Operation Systems GmbH\n</div>\n</div>";
if(parentTemplate) {
parentTemplate.rootRenderFunc(env, context, frame, runtime, cb);
} else {
cb(null, output);
}
})})});
} catch (e) {
  cb(runtime.handleError(e, lineno, colno));
}
}
return {
root: root
};

})();
})();
(function() {(window.nunjucksPrecompiled = window.nunjucksPrecompiled || {})["common-template.html"] = (function() {
function root(env, context, frame, runtime, cb) {
var lineno = 0;
var colno = 0;
var output = "";
try {
var parentTemplate = null;
output += "<div>\n";
var macro_t_1 = runtime.makeMacro(
["type", "current"], 
[], 
function (l_type, l_current, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("type", l_type);
frame.set("current", l_current);
var t_2 = "";if(runtime.memberLookup((l_type),"id") == l_current) {
t_2 += "<span class=\"self\" title=\"This type.\">";
t_2 += runtime.suppressValue(env.getFilter("typeName").call(context, runtime.memberLookup((l_type),"id")), env.opts.autoescape);
t_2 += "</span>";
;
}
else {
if(env.getFilter("isExternal").call(context, runtime.memberLookup((l_type),"id"))) {
t_2 += "<span class=\"external\" title=\"";
t_2 += runtime.suppressValue(env.getFilter("qualifiedName").call(context, runtime.memberLookup((l_type),"id")), env.opts.autoescape);
t_2 += "\">";
t_2 += runtime.suppressValue(env.getFilter("typeName").call(context, runtime.memberLookup((l_type),"id")), env.opts.autoescape);
t_2 += "</span>";
;
}
else {
t_2 += "<a href=\"#";
t_2 += runtime.suppressValue(runtime.memberLookup((l_type),"id"), env.opts.autoescape);
t_2 += "\" title=\"";
t_2 += runtime.suppressValue(env.getFilter("qualifiedName").call(context, runtime.memberLookup((l_type),"id")), env.opts.autoescape);
t_2 += "\">";
t_2 += runtime.suppressValue(env.getFilter("typeName").call(context, runtime.memberLookup((l_type),"id")), env.opts.autoescape);
t_2 += "</a>";
;
}
;
}
;
frame = callerFrame;
return new runtime.SafeString(t_2);
});
context.addExport("printTypeRef");
context.setVariable("printTypeRef", macro_t_1);
output += "\n\n";
var macro_t_3 = runtime.makeMacro(
["ref", "current"], 
[], 
function (l_ref, l_current, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("ref", l_ref);
frame.set("current", l_current);
var t_4 = "";if(runtime.memberLookup((l_ref),"class")) {
if(runtime.memberLookup((l_ref),"member")) {
if(runtime.memberLookup((l_ref),"class") != l_current) {
t_4 += runtime.suppressValue((lineno = 15, colno = 19, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printTypeRef"), "printTypeRef", context, [env.getFilter("asType").call(context, runtime.memberLookup((l_ref),"class"))])), env.opts.autoescape);
t_4 += ".";
;
}
if(env.getFilter("isExternal").call(context, runtime.memberLookup((l_ref),"class"))) {
t_4 += "<span class=\"external\" title=\"";
t_4 += runtime.suppressValue(env.getFilter("qualifiedName").call(context, runtime.memberLookup((l_ref),"class")), env.opts.autoescape);
t_4 += ".";
t_4 += runtime.suppressValue(runtime.memberLookup((l_ref),"member"), env.opts.autoescape);
t_4 += "\">";
t_4 += runtime.suppressValue(env.getFilter("simpleMemberName").call(context, runtime.memberLookup((l_ref),"member")), env.opts.autoescape);
t_4 += "</span>";
;
}
else {
t_4 += "<a href=\"#";
t_4 += runtime.suppressValue(runtime.memberLookup((l_ref),"class"), env.opts.autoescape);
t_4 += "#";
t_4 += runtime.suppressValue(runtime.memberLookup((l_ref),"member"), env.opts.autoescape);
t_4 += "\" title=\"";
if(runtime.memberLookup((l_ref),"class") == l_current) {
t_4 += runtime.suppressValue(env.getFilter("typeName").call(context, runtime.memberLookup((l_ref),"class")), env.opts.autoescape);
t_4 += ".";
;
}
else {
t_4 += runtime.suppressValue(env.getFilter("qualifiedName").call(context, runtime.memberLookup((l_ref),"class")), env.opts.autoescape);
t_4 += ".";
;
}
t_4 += runtime.suppressValue(runtime.memberLookup((l_ref),"member"), env.opts.autoescape);
t_4 += "\">";
t_4 += runtime.suppressValue(env.getFilter("simpleMemberName").call(context, runtime.memberLookup((l_ref),"member")), env.opts.autoescape);
t_4 += "</a>";
;
}
;
}
else {
t_4 += runtime.suppressValue((lineno = 23, colno = 18, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printTypeRef"), "printTypeRef", context, [env.getFilter("asType").call(context, runtime.memberLookup((l_ref),"class"))])), env.opts.autoescape);
;
}
;
}
else {
if(runtime.memberLookup((l_ref),"package")) {
t_4 += "<a href=\"#";
t_4 += runtime.suppressValue(runtime.memberLookup((l_ref),"package"), env.opts.autoescape);
t_4 += ".package-info\" title=\"";
t_4 += runtime.suppressValue(runtime.memberLookup((l_ref),"package"), env.opts.autoescape);
t_4 += "\">";
t_4 += runtime.suppressValue(env.getFilter("typeName").call(context, runtime.memberLookup((l_ref),"package")), env.opts.autoescape);
t_4 += "</a>";
;
}
;
}
;
frame = callerFrame;
return new runtime.SafeString(t_4);
});
context.addExport("printRef");
context.setVariable("printRef", macro_t_3);
output += "\n\n";
var macro_t_5 = runtime.makeMacro(
["sees", "current"], 
[], 
function (l_sees, l_current, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("sees", l_sees);
frame.set("current", l_current);
var t_6 = "";t_6 += "\n\t";
if(l_sees) {
t_6 += "\n\t\t<div class=\"see\">\n\t  \t<p>\n\t  \t<b>See also</b>:\n\t  \t</p>\n\t\t\t<ul>\n\t\t\t\t";
frame = frame.push();
var t_9 = l_sees;
if(t_9) {t_9 = runtime.fromIterator(t_9);
var t_8 = t_9.length;
for(var t_7=0; t_7 < t_9.length; t_7++) {
var t_10 = t_9[t_7];
frame.set("see", t_10);
frame.set("loop.index", t_7 + 1);
frame.set("loop.index0", t_7);
frame.set("loop.revindex", t_8 - t_7);
frame.set("loop.revindex0", t_8 - t_7 - 1);
frame.set("loop.first", t_7 === 0);
frame.set("loop.last", t_7 === t_8 - 1);
frame.set("loop.length", t_8);
t_6 += "\n\t\t\t\t\t<li>\n\t\t\t\t\t";
if(runtime.memberLookup((t_10),"class") || runtime.memberLookup((t_10),"package")) {
t_6 += "\n\t\t\t\t\t\t<code>";
t_6 += runtime.suppressValue((lineno = 40, colno = 23, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printRef"), "printRef", context, [t_10,l_current])), env.opts.autoescape);
t_6 += "</code>";
if(runtime.memberLookup((t_10),"title")) {
t_6 += ": ";
;
}
t_6 += "\n\t\t\t\t\t";
;
}
t_6 += "\n\t\t\t\t\t";
if(runtime.memberLookup((t_10),"title")) {
t_6 += "\n\t\t\t\t\t\t";
t_6 += runtime.suppressValue(env.getFilter("doc").call(context, runtime.memberLookup((t_10),"title"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
t_6 += "\n\t\t\t\t\t";
;
}
t_6 += "\n\t\t\t\t\t</li>\n\t\t\t\t";
;
}
}
frame = frame.pop();
t_6 += "\n\t\t\t</ul>\n\t\t</div>\n  \t";
;
}
t_6 += "\n";
;
frame = callerFrame;
return new runtime.SafeString(t_6);
});
context.addExport("printSees");
context.setVariable("printSees", macro_t_5);
output += "\n</div>";
if(parentTemplate) {
parentTemplate.rootRenderFunc(env, context, frame, runtime, cb);
} else {
cb(null, output);
}
;
} catch (e) {
  cb(runtime.handleError(e, lineno, colno));
}
}
return {
root: root
};

})();
})();
(function() {(window.nunjucksPrecompiled = window.nunjucksPrecompiled || {})["index-template.html"] = (function() {
function root(env, context, frame, runtime, cb) {
var lineno = 0;
var colno = 0;
var output = "";
try {
var parentTemplate = null;
output += "<div class=\"root\">\n<div class=\"javadoc\">\n";
var tasks = [];
tasks.push(
function(callback) {
env.getTemplate("search-template.html", false, "index-template.html", false, function(t_2,t_1) {
if(t_2) { cb(t_2); return; }
callback(null,t_1);});
});
tasks.push(
function(template, callback){
template.render(context.getVariables(), frame, function(t_4,t_3) {
if(t_4) { cb(t_4); return; }
callback(null,t_3);});
});
tasks.push(
function(result, callback){
output += result;
callback(null);
});
env.waterfall(tasks, function(){
output += "\n\n<div class=\"header\">\n<a href=\"#package-list\"><img alt=\"TopLogic\" src=\"toplogic.svg\" height=\"50px\"/></a>\nDocumentation\n</div>\n\n<h1>Package overview</h1>\n\n";
var macro_t_5 = runtime.makeMacro(
["packages", "n"], 
[], 
function (l_packages, l_n, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("packages", l_packages);
frame.set("n", l_n);
var t_6 = "";t_6 += "\n<ul>\n\t";
frame = frame.push();
var t_9 = l_packages;
if(t_9) {t_9 = runtime.fromIterator(t_9);
var t_8 = t_9.length;
for(var t_7=0; t_7 < t_9.length; t_7++) {
var t_10 = t_9[t_7];
frame.set("package", t_10);
frame.set("loop.index", t_7 + 1);
frame.set("loop.index0", t_7);
frame.set("loop.revindex", t_8 - t_7);
frame.set("loop.revindex0", t_8 - t_7 - 1);
frame.set("loop.first", t_7 === 0);
frame.set("loop.last", t_7 === t_8 - 1);
frame.set("loop.length", t_8);
t_6 += "\n\t<li class=\"";
if(env.getFilter("isExpanded").call(context, t_10,l_n)) {
t_6 += "expanded";
;
}
else {
t_6 += "collapsed";
;
}
t_6 += "\">\n\t<span class=\"package-node\" onclick=\"return togglePackage(this);\">\n\t\t";
t_6 += runtime.suppressValue(env.getFilter("packageRef").call(context, runtime.memberLookup((t_10),"name")), env.opts.autoescape);
t_6 += "\n\t\t";
if(runtime.memberLookup((runtime.memberLookup((t_10),"packages")),"length") > 0) {
t_6 += "\n\t\t<i class=\"far fa-plus-square expand\"></i>\n\t\t<i class=\"far fa-minus-square collapse\"></i>\n\t\t";
;
}
t_6 += "\n\t</span>\n\t";
if(runtime.memberLookup((runtime.memberLookup((t_10),"packages")),"length") > 0) {
t_6 += "\n\t\t";
t_6 += runtime.suppressValue((lineno = 23, colno = 18, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printPackages"), "printPackages", context, [runtime.memberLookup((t_10),"packages"),l_n + 1])), env.opts.autoescape);
t_6 += "\n\t";
;
}
t_6 += "\n\t</li>\n\t";
;
}
}
frame = frame.pop();
t_6 += "\n</ul>\n";
;
frame = callerFrame;
return new runtime.SafeString(t_6);
});
context.addExport("printPackages");
context.setVariable("printPackages", macro_t_5);
output += "\n\n<div>\n";
output += runtime.suppressValue((lineno = 31, colno = 16, runtime.callWrap(macro_t_5, "printPackages", context, [runtime.contextOrFrameLookup(context, frame, "packages"),0])), env.opts.autoescape);
output += "\n</div>\n\n</div>\n\n<div class=\"footer\">\nTopLogic API and implementations © Copyright by Business Operation Systems GmbH\n</div>\n</div>\n";
if(parentTemplate) {
parentTemplate.rootRenderFunc(env, context, frame, runtime, cb);
} else {
cb(null, output);
}
});
} catch (e) {
  cb(runtime.handleError(e, lineno, colno));
}
}
return {
root: root
};

})();
})();
(function() {(window.nunjucksPrecompiled = window.nunjucksPrecompiled || {})["package-template.html"] = (function() {
function root(env, context, frame, runtime, cb) {
var lineno = 0;
var colno = 0;
var output = "";
try {
var parentTemplate = null;
output += "<div class=\"root\">\n";
env.getTemplate("common-template.html", false, "package-template.html", false, function(t_2,t_1) {
if(t_2) { cb(t_2); return; }
t_1.getExported(function(t_3,t_1) {
if(t_3) { cb(t_3); return; }
context.setVariable("common", t_1);
output += "\n\n<div class=\"javadoc\">\n";
var tasks = [];
tasks.push(
function(callback) {
env.getTemplate("search-template.html", false, "package-template.html", false, function(t_5,t_4) {
if(t_5) { cb(t_5); return; }
callback(null,t_4);});
});
tasks.push(
function(template, callback){
template.render(context.getVariables(), frame, function(t_7,t_6) {
if(t_7) { cb(t_7); return; }
callback(null,t_6);});
});
tasks.push(
function(result, callback){
output += result;
callback(null);
});
env.waterfall(tasks, function(){
output += "\n\n<div class=\"header\">\n<a href=\"#package-list\"><img alt=\"TopLogic\" src=\"toplogic.svg\" height=\"50px\"/></a>\nDocumentation\n</div>\n\n";
var macro_t_8 = runtime.makeMacro(
["pkg"], 
[], 
function (l_pkg, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("pkg", l_pkg);
var t_9 = "";if(env.getFilter("basePackage").call(context, l_pkg)) {
t_9 += runtime.suppressValue((lineno = 13, colno = 17, runtime.callWrap(runtime.contextOrFrameLookup(context, frame, "printPackage"), "printPackage", context, [env.getFilter("basePackage").call(context, l_pkg)])), env.opts.autoescape);
t_9 += ".";
;
}
t_9 += runtime.suppressValue(env.getFilter("pkgRef").call(context, l_pkg,runtime.contextOrFrameLookup(context, frame, "index")), env.opts.autoescape);
;
frame = callerFrame;
return new runtime.SafeString(t_9);
});
context.addExport("printPackage");
context.setVariable("printPackage", macro_t_8);
output += "\n\n<h1>Package ";
output += runtime.suppressValue((lineno = 18, colno = 27, runtime.callWrap(macro_t_8, "printPackage", context, [env.getFilter("basePackage").call(context, runtime.contextOrFrameLookup(context, frame, "name"))])), env.opts.autoescape);
output += ".";
output += runtime.suppressValue(env.getFilter("typeName").call(context, runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
output += "</h1>\n\n<p>";
output += runtime.suppressValue(env.getFilter("doc").call(context, runtime.contextOrFrameLookup(context, frame, "title"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
output += "</p>\n";
output += runtime.suppressValue(env.getFilter("doc").call(context, runtime.contextOrFrameLookup(context, frame, "doc"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
output += "\n\n";
output += runtime.suppressValue((lineno = 23, colno = 19, runtime.callWrap(runtime.memberLookup((t_1),"printSees"), "common[\"printSees\"]", context, [runtime.contextOrFrameLookup(context, frame, "sees"),null])), env.opts.autoescape);
output += "\n\n";
if(runtime.memberLookup(((env.getFilter("getPackageNode").call(context, runtime.contextOrFrameLookup(context, frame, "name"),runtime.contextOrFrameLookup(context, frame, "index")))),"packages")) {
output += "\n\t<h2>Packages</h2>\n\t<ul>\n\t";
frame = frame.push();
var t_12 = runtime.memberLookup(((env.getFilter("getPackageNode").call(context, runtime.contextOrFrameLookup(context, frame, "name"),runtime.contextOrFrameLookup(context, frame, "index")))),"packages");
if(t_12) {t_12 = runtime.fromIterator(t_12);
var t_11 = t_12.length;
for(var t_10=0; t_10 < t_12.length; t_10++) {
var t_13 = t_12[t_10];
frame.set("sub", t_13);
frame.set("loop.index", t_10 + 1);
frame.set("loop.index0", t_10);
frame.set("loop.revindex", t_11 - t_10);
frame.set("loop.revindex0", t_11 - t_10 - 1);
frame.set("loop.first", t_10 === 0);
frame.set("loop.last", t_10 === t_11 - 1);
frame.set("loop.length", t_11);
output += "\n\t\t<li>\n\t\t\t";
output += runtime.suppressValue(env.getFilter("pkgRef").call(context, runtime.memberLookup((t_13),"name"),runtime.contextOrFrameLookup(context, frame, "index")), env.opts.autoescape);
output += "\n\t\t</li>\n\t";
;
}
}
frame = frame.pop();
output += "\n\t</ul>\n";
;
}
output += "\n\n";
var macro_t_14 = runtime.makeMacro(
["title", "list"], 
[], 
function (l_title, l_list, kwargs) {
var callerFrame = frame;
frame = new runtime.Frame();
kwargs = kwargs || {};
if (Object.prototype.hasOwnProperty.call(kwargs, "caller")) {
frame.set("caller", kwargs.caller); }
frame.set("title", l_title);
frame.set("list", l_list);
var t_15 = "";t_15 += "\n\t";
if(runtime.memberLookup((l_list),"length") > 0) {
t_15 += "\n\t<h2>";
t_15 += runtime.suppressValue(l_title, env.opts.autoescape);
t_15 += "</h2>\n\t<ul>\n\t\t";
frame = frame.push();
var t_18 = l_list;
if(t_18) {t_18 = runtime.fromIterator(t_18);
var t_17 = t_18.length;
for(var t_16=0; t_16 < t_18.length; t_16++) {
var t_19 = t_18[t_16];
frame.set("member", t_19);
frame.set("loop.index", t_16 + 1);
frame.set("loop.index0", t_16);
frame.set("loop.revindex", t_17 - t_16);
frame.set("loop.revindex0", t_17 - t_16 - 1);
frame.set("loop.first", t_16 === 0);
frame.set("loop.last", t_16 === t_17 - 1);
frame.set("loop.length", t_17);
t_15 += "\n\t\t<li>\n\t\t";
if(runtime.memberLookup((t_19),"abstract")) {
t_15 += "\n\t\t\t<i>\n\t\t\t\t";
t_15 += runtime.suppressValue(env.getFilter("localTypeRef").call(context, runtime.memberLookup((t_19),"id"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
t_15 += "\n\t\t\t</i>\n\t\t";
;
}
else {
t_15 += "\n\t\t\t";
t_15 += runtime.suppressValue(env.getFilter("localTypeRef").call(context, runtime.memberLookup((t_19),"id"),runtime.contextOrFrameLookup(context, frame, "name")), env.opts.autoescape);
t_15 += "\n\t\t";
;
}
t_15 += "\n\t\t</li>\n\t\t";
;
}
}
frame = frame.pop();
t_15 += "\n\t</ul>\n\t";
;
}
t_15 += "\n";
;
frame = callerFrame;
return new runtime.SafeString(t_15);
});
context.addExport("section");
context.setVariable("section", macro_t_14);
output += "\n\n";
output += runtime.suppressValue((lineno = 55, colno = 10, runtime.callWrap(macro_t_14, "section", context, ["Enums",runtime.contextOrFrameLookup(context, frame, "enums")])), env.opts.autoescape);
output += "\n";
output += runtime.suppressValue((lineno = 56, colno = 10, runtime.callWrap(macro_t_14, "section", context, ["Annotations",runtime.contextOrFrameLookup(context, frame, "annotations")])), env.opts.autoescape);
output += "\n";
output += runtime.suppressValue((lineno = 57, colno = 10, runtime.callWrap(macro_t_14, "section", context, ["Configs",runtime.contextOrFrameLookup(context, frame, "configs")])), env.opts.autoescape);
output += "\n";
output += runtime.suppressValue((lineno = 58, colno = 10, runtime.callWrap(macro_t_14, "section", context, ["Instances",runtime.contextOrFrameLookup(context, frame, "instances")])), env.opts.autoescape);
output += "\n";
output += runtime.suppressValue((lineno = 59, colno = 10, runtime.callWrap(macro_t_14, "section", context, ["Interfaces",runtime.contextOrFrameLookup(context, frame, "interfaces")])), env.opts.autoescape);
output += "\n";
output += runtime.suppressValue((lineno = 60, colno = 10, runtime.callWrap(macro_t_14, "section", context, ["Abstract Classes",env.getFilter("onlyAbstract").call(context, runtime.contextOrFrameLookup(context, frame, "classes"))])), env.opts.autoescape);
output += "\n";
output += runtime.suppressValue((lineno = 61, colno = 10, runtime.callWrap(macro_t_14, "section", context, ["Classes",env.getFilter("nonAbstract").call(context, runtime.contextOrFrameLookup(context, frame, "classes"))])), env.opts.autoescape);
output += "\n";
output += runtime.suppressValue((lineno = 62, colno = 10, runtime.callWrap(macro_t_14, "section", context, ["Exceptions",runtime.contextOrFrameLookup(context, frame, "exceptions")])), env.opts.autoescape);
output += "\n";
output += runtime.suppressValue((lineno = 63, colno = 10, runtime.callWrap(macro_t_14, "section", context, ["Errors",runtime.contextOrFrameLookup(context, frame, "errors")])), env.opts.autoescape);
output += "\n</div>\n\n<div class=\"footer\">\nTopLogic API and implementations © Copyright by Business Operation Systems GmbH\n</div>\n</div>";
if(parentTemplate) {
parentTemplate.rootRenderFunc(env, context, frame, runtime, cb);
} else {
cb(null, output);
}
})})});
} catch (e) {
  cb(runtime.handleError(e, lineno, colno));
}
}
return {
root: root
};

})();
})();
(function() {(window.nunjucksPrecompiled = window.nunjucksPrecompiled || {})["ref-template.html"] = (function() {
function root(env, context, frame, runtime, cb) {
var lineno = 0;
var colno = 0;
var output = "";
try {
var parentTemplate = null;
output += "<code>";
env.getTemplate("common-template.html", false, "ref-template.html", false, function(t_2,t_1) {
if(t_2) { cb(t_2); return; }
t_1.getExported(function(t_3,t_1) {
if(t_3) { cb(t_3); return; }
context.setVariable("common", t_1);
output += runtime.suppressValue((lineno = 3, colno = 18, runtime.callWrap(runtime.memberLookup((t_1),"printRef"), "common[\"printRef\"]", context, [runtime.contextOrFrameLookup(context, frame, "ref"),runtime.contextOrFrameLookup(context, frame, "current")])), env.opts.autoescape);
output += "</code>";
if(parentTemplate) {
parentTemplate.rootRenderFunc(env, context, frame, runtime, cb);
} else {
cb(null, output);
}
})});
} catch (e) {
  cb(runtime.handleError(e, lineno, colno));
}
}
return {
root: root
};

})();
})();
(function() {(window.nunjucksPrecompiled = window.nunjucksPrecompiled || {})["search-template.html"] = (function() {
function root(env, context, frame, runtime, cb) {
var lineno = 0;
var colno = 0;
var output = "";
try {
var parentTemplate = null;
output += "<div class=\"toolbar\">\n\n<div id=\"search\" class=\"search\">\n<input id=\"seach-input\" type=\"search\" size=\"20\" placeholder=\"Search...\"/>\n<i class=\"fas fa-search\"></i>\n</div>\n\n</div>";
if(parentTemplate) {
parentTemplate.rootRenderFunc(env, context, frame, runtime, cb);
} else {
cb(null, output);
}
;
} catch (e) {
  cb(runtime.handleError(e, lineno, colno));
}
}
return {
root: root
};

})();
})();

