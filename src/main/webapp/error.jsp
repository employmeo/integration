<%@ page isErrorPage="true" %>
<%@ include file="/WEB-INF/includes/inc_head.jsp" %>
<%
Exception e = pageContext.getException();
//ErrorData ed = pageContext.getErrorData();
int statusCode = 500;
String message = "An unexpected error has occurred";
//if (ed != null) {
//    statusCode = ed.getStatusCode();
//}
if (e != null) {
	message = e.getMessage();
}
%>
<div class="col-md-12">
	<div class="col-middle">
		<div class="text-center text-center">
			<h1 class="error-number"><%=statusCode%></h1>
			<h2><%=message%></h2>
			<p><a href="<%=response.encodeURL("/contact_us.jsp")%>">Report this?</a></p>
		</div>
	</div>
</div>
<%@ include file="/WEB-INF/includes/inc_header.jsp" %>
</html>