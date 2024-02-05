<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<!-- replace implementation class dependent of the drop type -->
	<xsl:template match="arguments/dropTargets/dropTarget/@class">
		<xsl:attribute name="class">
			<xsl:choose>
				<xsl:when test="parent::dropTarget/@dropType = 'ROW'">
					<xsl:value-of select="'com.top_logic.model.search.providers.OntoTreeDropByExpression'"/>
				</xsl:when>
				
				<!-- otherwise the droptype is ORDERED -->
				<xsl:otherwise>
					<xsl:value-of select="'com.top_logic.model.search.providers.OrderedTreeDropByExpression'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
	
	<!-- Transform signature of tables canDrop to the more general tree canDrop -->
	<xsl:template match="arguments/dropTargets/dropTarget/@canDrop">
		<xsl:attribute name="canDrop">
			<xsl:choose>
				<xsl:when test="parent::dropTarget/@dropType = 'ROW'">
					<xsl:value-of select="."/>
				</xsl:when>
				
				<!-- otherwise the droptype is ORDERED -->
				<xsl:otherwise>
					<xsl:text>newDraggedObjects -> newParent -> newReference -> { oldCanDrop = </xsl:text>
					<xsl:value-of select="."/>
					<xsl:text>$newDraggedObjects.foreach(newDraggedObject -> $oldCanDrop($newDraggedObject, $newReference));}</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
	
	<!-- Transform signature of tables canDrop to the more general tree canDrop -->
	<xsl:template match="arguments/dropTargets/dropTarget/canDrop">
		<xsl:element name="canDrop">
			<xsl:choose>
				<xsl:when test="parent::dropTarget/@dropType = 'ROW'">
					<xsl:value-of select="."/>
				</xsl:when>
				
				<!-- otherwise the droptype is ORDERED -->
				<xsl:otherwise>
					<xsl:text>newDraggedObjects -> newParent -> newReference -> { oldCanDrop = </xsl:text>
					<xsl:value-of select="."/>
					<xsl:text>$newDraggedObjects.foreach(newDraggedObject -> $oldCanDrop($newDraggedObject, $newReference));}</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
	
	<!-- Transform signature of tables handleDrop to the more general tree handleDrop -->
	<xsl:template match="arguments/dropTargets/dropTarget/@handleDrop">
		<xsl:attribute name="handleDrop">
			<xsl:choose>
				<xsl:when test="parent::dropTarget/@dropType = 'ROW'">
					<xsl:value-of select="."/>
				</xsl:when>
				
				<!-- otherwise the droptype is ORDERED -->
				<xsl:otherwise>
					<xsl:text>newDraggedObjects -> newParent -> newReference -> { oldHandleDrop = </xsl:text>
					<xsl:value-of select="."/>
					<xsl:text>$newDraggedObjects.foreach(newDraggedObject -> $oldHandleDrop($newDraggedObject, $newReference));}</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
	
	<!-- Transform signature of tables handleDrop to the more general tree handleDrop -->
	<xsl:template match="arguments/dropTargets/dropTarget/handleDrop">
		<xsl:element name="handleDrop">
			<xsl:choose>
				<xsl:when test="parent::dropTarget/@dropType = 'ROW'">
					<xsl:value-of select="."/>
				</xsl:when>
				
				<!-- otherwise the droptype is ORDERED -->
				<xsl:otherwise>
					<xsl:text>newDraggedObjects -> newParent -> newReference -> { oldHandleDrop = </xsl:text>
					<xsl:value-of select="."/>
					<xsl:text>$newDraggedObjects.foreach(newDraggedObject -> $oldHandleDrop($newDraggedObject, $newReference));}</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
	
	<!-- remove droptype attribute -->
	<xsl:template match="arguments/dropTargets/dropTarget/@dropType"/>
	
	<!-- Standard copy template. -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>