<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config//module[@name='tl.model']//datatype[@name='TLAnnotation']">
		<datatype name="HistoryType"
			db_type="string"
			kind="Custom"
		>
			<annotations>
				<config-type value="ENUM"/>
			</annotations>
			<storage-mapping class="com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping"
				application-type="com.top_logic.dob.meta.MOReference$HistoryType"
			/>
		</datatype>
		<xsl:copy-of select="."/>
	</xsl:template>

	<xsl:template match="/config//module[@name='tl.model']//class[@name='TLAssociationEnd']//property[@name='navigate']">
		<xsl:copy-of select="."/>
		<property name="historyType"
			type="tl.model:HistoryType"
		/>
	</xsl:template>

	<xsl:template match="/config//module[@name='tl.model']//class[@name='TLReference']//property[@name='bag']">
		<xsl:copy-of select="."/>
		<property name="historyType"
			type="tl.model:HistoryType"
		>
			<annotations>
				<storage-algorithm>
					<derived-storage>
						<expression-evaluation>
							<chain>
								<get-value attribute="end"/>
								<get-value attribute="historyType"/>
							</chain>
						</expression-evaluation>
					</derived-storage>
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