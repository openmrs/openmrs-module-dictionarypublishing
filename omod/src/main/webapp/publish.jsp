<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<c:choose>
	<c:when test="${!hasEverBeenPublished}">
		<p>You are not publishing your Concept Dictionary.</p>

		<p>This one-time setup may take a long time to run depending on
			the size of your dictionary, and will:
		<ol>
			<li>Create a metadata package with all ${conceptsCount} of your
				concepts created or modified after <openmrs:formatDate
					date="${nextPublishDate}" />.
			</li>
			<li>Ensure each concept has a mapping to the ${localSource.name}
				concept source.</li>
			<li>Publish your dictionary at ${publishedUrl}</li>
		</ol>

		<form action="publishNewVersion.form" method="post">
			<input type="submit" value="Start publishing" />
		</form>
	</c:when>
	<c:otherwise>

		<c:choose>
			<c:when test="${isPublished}">
				<p>
					Your dictionary is <b>published</b>.
				</p>
				<table>
					<tr>
						<td>&nbsp;&nbsp; URL: <a href="${publishedUrl}">${publishedUrl}</a><br />
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
				<p>
					Your dictionary is ready to publish, but sharing is temporarily <b>disabled</b>.
				</p>
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
			<c:choose>
				<c:when test="${conceptsCount > 0}">
					<form action="publishNewVersion.form" method="post">
						<input type="submit" value="Publish New Version" />
						(${conceptsCount} concepts new or modified since current version)
					</form>
				</c:when>
				<c:otherwise>
					There are no new or modified concepts since current version.
				</c:otherwise>
			</c:choose>
		</p>
	</c:otherwise>
</c:choose>

<c:if test="${conceptsCount > 5000}">
	<p style="background-color: yellow;">It is not advised to export a
		version with more than 5000 concepts. You should consider setting the
		"dictionarypublishing.lastPublishDate" global property to
		some date (format: "yyyy-mm-dd hh:mm:ss") in the past to limit the
		number of concepts to be included in the new version.</p>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>