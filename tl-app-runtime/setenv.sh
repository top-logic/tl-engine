# SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
#
# The JVM options of a TopLogic application in its container, read by catalina.sh. Options given in
# CATALINA_OPTS when the container is started come last and so take precedence.
#
# - The heap takes a share of the memory of the container (tl_max_ram_percentage, default 75).
# - After an OutOfMemoryError the JVM writes a heap dump into the data volume and stops: the
#   restart policy of the container starts the application again, instead of a JVM that goes on
#   in an undefined state.
# - The scheduler of the engine names the node it runs on, and a task left running when the node
#   stopped is resumed by the node of the same name. The default name is the host name, which is
#   new for each container; tl_node_name gives the name that stays (default toplogic).
# - Lucene maps its index files without the memory segments of the JVM, whose mappings are freed
#   only when the index reader is closed.
# - tl_config: the configuration of the installation, see toplogic.config.xml.

CATALINA_OPTS="-XX:MaxRAMPercentage=${tl_max_ram_percentage:-75} \
-XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${tl_storage_path:-/var/lib/toplogic} \
-Djava.awt.headless=true \
-Dcom.top_logic.util.sched.task.SystemPropertyTaskClusterNodeName=${tl_node_name:-toplogic} \
-Dorg.apache.lucene.store.MMapDirectory.enableMemorySegments=false \
-Dtl_config=${CATALINA_HOME}/conf/toplogic.config.xml \
${CATALINA_OPTS:-}"
