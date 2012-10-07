<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/publish") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/dictionarypublishing/publish.form"><spring:message
				code="dictionarypublishing.publish" /></a>
	</li>
	
	<!-- Add further links here -->
</ul>
<h2>
	<spring:message code="dictionarypublishing.title" />
</h2>
