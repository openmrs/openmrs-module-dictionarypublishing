<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<p>You are not publishing your Concept Dictionary.</p>

<p>
	You cannot publish your dictionary until you <a
		href="<c:url value="/module/metadatasharing/configure.form" />"><b>configure</b></a>
	the public URL prefix in the Metadata Sharing Module.
</p>

<%@ include file="/WEB-INF/template/footer.jsp"%>