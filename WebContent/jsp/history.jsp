<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.io.*,java.util.*"%>
<html>
<head>
<title>历史</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>
<body>
    <h2 align="center">商品列表</h2>
    <div align="center">
        <form action="buy.jsp" method="post">
            <table border="0" cellspacing="30">
                <tr>
                    <th>商品名称</th>
                    <th>购买</th>
                    <th>价格</th>
                </tr>
                <tr>
                    <td>中国史</td>
                    <td><input type="checkbox" name="item" value="chineseHistory"></td>
                    <td>￥80</td>
                </tr>
                <tr>
                    <td>世界史</td>
                    <td><input type="checkbox" name="item" value="worldHistory"></td>
                    <td>￥70</td>
                </tr>
            </table>

            <input type="submit" value="购买">
        </form>
    </div>
</body>
</html>