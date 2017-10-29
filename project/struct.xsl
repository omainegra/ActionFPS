<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        version="2.0">
    <xsl:strip-space elements="*"/>
    <xsl:output method="text"/>
    <xsl:variable name="root" select="'https://github.com/ScalaWilliam/ActionFPS/tree/master/'"/>
    <xsl:template match="structure">
        digraph { &#xa;
        graph [splines=ortho, nodesep=0.2];
        node [shape=record];
        rankdir = LR; &#xa;
        <xsl:apply-templates select="project"/>
        &#xa;
        }
    </xsl:template>

    <xsl:template match="project">
        <xsl:variable name="id" select="string(id)"/>
        <xsl:variable name="relative-path" select="substring-after(data(base), data((/structure/project/base)[1]))"/>
        <xsl:variable name="url" select="concat($root, $relative-path)"/>
        "<xsl:value-of select="$id"/>" [label="<xsl:value-of select="$id"/>", href="<xsl:value-of select="$url"/>"]; &#xa;
        <xsl:for-each select="dependencies/project">
            <xsl:variable name="link-id" select="text()"/>
            "<xsl:value-of select="$id"/>" -&gt; "<xsl:value-of select="$link-id"/>";
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>