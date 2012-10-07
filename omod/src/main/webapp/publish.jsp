<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<c:choose>
<c:when test="${!hasEverBeenPublished}">
<p>You are not publishing your Concept Dictionary.</p>

<p>This one-time setup may take a long time to run depending on the size of your dictionary, and will:
<ol>
<li>Create a metadata package with all ${conceptCount} of your concepts created or modified after
<openmrs:formatDate date="${nextPublishDate}"/>.</li>
<li>Ensure each concept has a mapping to the ${localSource.name} concept source.</li>
<li>Publish your dictionary at ${publishedUrl}</li>
</ol>

<form action="publishNewVersion.form" method="post">
<input type="submit" value="Start publishing" />
</form>
</c:when>
<c:otherwise>

<c:choose>
<c:when test="${isPublished}">
<p>Your dictionary is <b>published</b>.</p>
<table>
<tr>
<td>
&nbsp;&nbsp; URL: <a href="${publishedUrl}">${publishedUrl}</a><br />
&nbsp;&nbsp; Version: ${lastPublishedVersion}
</td>
<td>
<form action="disablePublishing.form" method="post">
&nbsp;&nbsp;<input type="submit" value="Disable" />
</form>
</td>
</tr>
</table>
</c:when>
<c:otherwise>
<p>Your dictionary is ready to publish, but sharing is temporarily <b>disabled</b>.</p>
<table>
<tr>
<td>
<p style="color: gray;">
&nbsp;&nbsp; URL: <a href="${publishedUrl}">${publishedUrl}</a><br />
&nbsp;&nbsp; Version: ${lastPublishedVersion}
</p>
</td>
<td>
<form action="enablePublishing.form" method="post">
&nbsp;&nbsp;<input type="submit" value="Enable" />
</form>
</td>
</tr>
</table>
</c:otherwise>
</c:choose>
</p>
<p>
<c:if test="${conceptCount > 0}">
<form action="publishNewVersion.form" method="post">
<input type="submit" value="Publish New Version" /> (${conceptCount} concepts modified since current version)
</form>
</c:if>
</p>
</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp" %>