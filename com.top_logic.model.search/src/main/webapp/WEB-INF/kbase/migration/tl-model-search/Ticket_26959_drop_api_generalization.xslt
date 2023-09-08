<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<xsl:template match="arguments/tableDrop/@canDrop | arguments/treeDrop[@class = 'com.top_logic.model.search.providers.OntoTreeDropByExpression']/@canDrop">
		<xsl:attribute name="canDrop">
			<xsl:text>draggedObjects->referenceRow->{canDrop=</xsl:text>
			<xsl:value-of select="."/>
			<xsl:text>;$draggedObjects.filter(draggedObject -> !$canDrop($draggedObject,$referenceRow)).isEmpty();}</xsl:text>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="arguments/tableDrop/canDrop | arguments/treeDrop[@class = 'com.top_logic.model.search.providers.OntoTreeDropByExpression']/canDrop">
		<canDrop>
		<xsl:text>draggedObjects->referenceRow->{canDrop=</xsl:text>
		<xsl:value-of select="."/>
		<xsl:text>;$draggedObjects.filter(draggedObject -> !$canDrop($draggedObject,$referenceRow)).isEmpty();}</xsl:text>
		</canDrop>
	</xsl:template>
	
	<xsl:template match="arguments/tableDrop/@handleDrop | arguments/treeDrop[@class = 'com.top_logic.model.search.providers.OntoTreeDropByExpression']/@handleDrop">
		<xsl:attribute name="handleDrop">
			<xsl:text>draggedObjects->referenceRow->{handleDrop=</xsl:text>
			<xsl:value-of select="."/>
			<xsl:text>;$draggedObjects.foreach(draggedObject -> $handleDrop($draggedObject,$referenceRow));}</xsl:text>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="arguments/tableDrop/handleDrop | arguments/treeDrop[@class = 'com.top_logic.model.search.providers.OntoTreeDropByExpression']/handleDrop">
		<handleDrop>
		<xsl:text>draggedObjects->referenceRow->{handleDrop=</xsl:text>
		<xsl:value-of select="."/>
		<xsl:text>;$draggedObjects.foreach(draggedObject -> $handleDrop($draggedObject,$referenceRow));}</xsl:text>
		</handleDrop>
	</xsl:template>
	
	<xsl:template match="arguments/treeDrop/@canDrop">
		<xsl:attribute name="canDrop">
			<xsl:text>draggedObjects->parent->reference->{canDrop=</xsl:text>
			<xsl:value-of select="."/>
			<xsl:text>;$draggedObjects.filter(draggedObject -> !$canDrop($draggedObject,$parent,$reference)).isEmpty();}</xsl:text>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="arguments/treeDrop/canDrop">
		<canDrop>
		<xsl:text>draggedObjects->parent->reference->{canDrop=</xsl:text>
		<xsl:value-of select="."/>
		<xsl:text>;$draggedObjects.filter(draggedObject -> !$canDrop($draggedObject,$parent,$reference)).isEmpty();}</xsl:text>
		</canDrop>
	</xsl:template>
	
	<xsl:template match="arguments/treeDrop/@handleDrop">
		<xsl:attribute name="handleDrop">
			<xsl:text>draggedObjects->parent->reference->{handleDrop=</xsl:text>
			<xsl:value-of select="."/>
			<xsl:text>;$draggedObjects.foreach(draggedObject -> $handleDrop($draggedObject,$parent,$reference));}</xsl:text>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="arguments/treeDrop/handleDrop">
		<handleDrop>
		<xsl:text>draggedObjects->parent->reference->{handleDrop=</xsl:text>
		<xsl:value-of select="."/>
		<xsl:text>;$draggedObjects.foreach(draggedObject -> $handleDrop($draggedObject,$parent,$reference));}</xsl:text>
		</handleDrop>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>