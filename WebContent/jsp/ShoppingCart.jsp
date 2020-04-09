<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.io.*,java.util.*"%>
<html>
<head>
<%
    Map<String, Integer> shoppingMap = (Map<String, Integer>) session.getAttribute("shoppingMap");
    Set<String> key1 = shoppingMap.keySet();
%>
<title>show</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script type="text/javascript">
	function del(idx) {
		document.getElementById('item').value = idx;
		var form = document.getElementById('shoppingCartForm');
		form.submit();
	}
</script>
</head>
<body>
	<div align="center">
		<h2>购物车</h2>
		<form id="shoppingCartForm" action="notbuy.jsp" method="post">
			<table border="0" cellspacing="30">
				<tr>
					<th>商品名称</th>
					<th>数量</th>
					<th>总价</th>
				</tr>
				<% if (shoppingMap.get("笔记本") != 0) { %>
				<tr>
					<td>笔记本</td>
					<td><%=shoppingMap.get("笔记本")%></td>
					<td><%=shoppingMap.get("笔记本")%>*5000</td>
					<td><input type="button" onclick="del('笔记本')" value="删除" /></td>
				</tr>
				<% } %>
				<% if (shoppingMap.get("台式机") != 0) { %>
				<tr>
					<td>台式机</td>
					<td><%=shoppingMap.get("台式机")%></td>
					<td><%=shoppingMap.get("台式机")%>*200000</td>
					<td><input type="button" onclick="del('台式机')" value="删除" /></td>
				</tr>
				<% } %>
				<% if (shoppingMap.get("中国史") != 0) { %>
				<tr>
					<td>中国史</td>
					<td><%=shoppingMap.get("中国史")%></td>
					<td><%=shoppingMap.get("中国史")%>*500</td>
					<td><input type="button" onclick="del('中国史')" value="删除" /></td>
				</tr>
				<% } %>
				<% if (shoppingMap.get("世界史") != 0) { %>
				<tr>
					<td>世界史</td>
					<td><%=shoppingMap.get("世界史")%></td>
					<td><%=shoppingMap.get("世界史")%>*20</td>
					<td><input type="button" onclick="del('世界史')" value="删除" /></td>
				</tr>
				<% } %>
			</table>
			<input type="hidden" id="item" name="item" value="" />
		</form>
		<a href="computer.jsp">计算机</a> <a href="history.jsp">历史</a>
		</p>
	</div>
</body>
</html>