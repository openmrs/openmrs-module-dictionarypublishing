<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<p>You are not publishing your Concept Dictionary.</p>

<p>
	You cannot publish your dictionary until you <a
		href="<c:url value="/module/conceptpubsub/configure.form" />"><b>configure</b></a>
	the local concept source in the ConceptPubSub module.
</p>

<%@ include file="/WEB-INF/template/footer.jsp"%>