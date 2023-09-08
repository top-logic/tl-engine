/**
 * Interfaces of bound security.
 * 
 * <p>
 * Bound Security is based on {@link com.top_logic.tool.boundsec.BoundRole},
 * {@link com.top_logic.tool.boundsec.BoundObject},
 * {@link com.top_logic.tool.boundsec.BoundChecker}, and
 * {@link com.top_logic.tool.boundsec.BoundCommandGroup}. A person has a role on a
 * {@link com.top_logic.tool.boundsec.BoundObject}. A
 * {@link com.top_logic.tool.boundsec.BoundCommandGroup} is associated to roles necessary to execute
 * it. If a command is to be performed on an object by a person, the
 * {@link com.top_logic.tool.boundsec.BoundChecker} asks the object if the person has one of the
 * roles needed for that command.
 * </p>
 */
package com.top_logic.tool.boundsec;
