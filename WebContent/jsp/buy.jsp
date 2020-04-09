<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="java.io.*,java.util.*"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + path + "/";

    request.setCharacterEncoding("utf-8");

    Map<String, Integer> shoppingMap = (Map<String, Integer>) session.getAttribute("shoppingMap");
    if (shoppingMap == null) {
        shoppingMap = new HashMap<String, Integer>();
        shoppingMap.put("笔记本", 0);
        shoppingMap.put("台式机", 0);
        shoppingMap.put("中国史", 0);
        shoppingMap.put("世界史", 0);
    }

    String[] buys = request.getParameterValues("item");

    for (String item : buys) {
        if (item.equals("laptop")) {
            int num1 = shoppingMap.get("笔记本").intValue();
            shoppingMap.put("笔记本", 1);
        } else if (item.equals("computer")) {
            int num2 = shoppingMap.get("台式机").intValue();
            shoppingMap.put("台式机", 1);
        } else if (item.equals("chineseHistory")) {
            int num2 = shoppingMap.get("中国史").intValue();
            shoppingMap.put("中国史", 1);
        } else if (item.equals("worldHistory")) {
            int num2 = shoppingMap.get("世界史").intValue();
            shoppingMap.put("世界史", 1);
        }
    }

    session.setAttribute("shoppingMap", shoppingMap);
    request.getRequestDispatcher("ShoppingCart.jsp").forward(request, response);
%>