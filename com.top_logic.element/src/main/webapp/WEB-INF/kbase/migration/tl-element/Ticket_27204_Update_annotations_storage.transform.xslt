<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config//module[@name='tl.model']//datatype[@name='TLAnnotation']/storage-mapping">
		<storage-mapping class="com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping"
			application-type="com.top_logic.model.annotate.TLAnnotation"
		/>
	</xsl:template>

	<xsl:template match="/config//module[@name='tl.model']//interface[@name='TLModelPart']//property[@name='annotations']">
		<property name="annotations"
			bag="false"
			multiple="true"
			type="TLAnnotation"
		>
			<annotations>
				<storage-algorithm>
					<implementation class="com.top_logic.element.meta.kbbased.storage.AnnotationConfigsStorage"
						db-attribute="annotations"
					/>
				</storage-algorithm>
			</annotations>
		</property>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>