<?xml version="1.0" encoding="utf-8" ?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0"
>
	<xsl:template match="/config//module[@name='tl.core']">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<annotations>
				<package-binding
					implementation-package="com.top_logic.model.core.generated"
					interface-package="com.top_logic.model.core"
				/>
				<factory value="com.top_logic.element.model.DefaultModelFactory"/>
			</annotations>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="/config//module[@name='tl.core']//datatype[@name='Icon']">
		<xsl:copy-of select="."/>

		<interface name="Author">
			<attributes>
				<property name="name"
					mandatory="true"
					type="tl.core:String"
				>
					<annotations>
						<delete-protected/>
					</annotations>
				</property>
			</attributes>
		</interface>
		<class name="Revision">
			<annotations>
				<implementation-binding interface-name="com.top_logic.knowledge.service.Revision"/>
				<instance-presentation icon="theme:mime.Revision"/>
				<main-properties properties="revision, date, author"/>
				<table name="Revision"/>
				<storage-algorithm>
					<primitive-storage storage-mapping="com.top_logic.element.meta.kbbased.storage.mappings.RevisionAsLong"/>
				</storage-algorithm>
			</annotations>
			<attributes>
				<property name="revision"
					type="tl.core:Long"
				>
					<annotations>
						<storage-algorithm>
							<derived-storage>
								<expression-evaluation>
									<method-call method="com.top_logic.knowledge.service.Revision#getCommitNumber()"/>
								</expression-evaluation>
							</derived-storage>
						</storage-algorithm>
						<delete-protected/>
					</annotations>
				</property>
				<property name="date"
					type="tl.core:DateTime"
				>
					<annotations>
						<storage-algorithm>
							<derived-storage>
								<expression-evaluation>
									<method-call method="com.top_logic.knowledge.service.Revision#resolveDate()"/>
								</expression-evaluation>
							</derived-storage>
						</storage-algorithm>
						<delete-protected/>
						<format format-ref="date-time"/>
					</annotations>
				</property>
				<property name="author"
					type="tl.core:Author"
				>
					<annotations>
						<storage-algorithm>
							<derived-storage>
								<expression-evaluation>
									<method-call method="com.top_logic.knowledge.service.Revision#resolveAuthor()"/>
								</expression-evaluation>
							</derived-storage>
						</storage-algorithm>
						<delete-protected/>
					</annotations>
				</property>
				<property name="log"
					type="tl.core:String"
				>
					<annotations>
						<storage-algorithm>
							<derived-storage>
								<expression-evaluation>
									<method-call method="com.top_logic.knowledge.service.Revision#getLog()"/>
								</expression-evaluation>
							</derived-storage>
						</storage-algorithm>
						<delete-protected/>
					</annotations>
				</property>
			</attributes>
		</class>
	</xsl:template>
	<xsl:template match="/config//module[@name='tl.accounts']//class[@name='Person']/generalizations">
		<generalizations>
			<generalization type="tl.core:Author"/>
		</generalizations>
	</xsl:template>

	<xsl:template match="/config//module[@name='tl.accounts']//class[@name='Person']//property[@name='name']">
		 <xsl:copy>
			<xsl:attribute name="override">
				<xsl:value-of select="'true'"/>
			</xsl:attribute>
    		<xsl:apply-templates select="@* | node()"/>
  		</xsl:copy>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>