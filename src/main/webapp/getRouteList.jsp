<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.SpiritProto"%>
<%
String result="";
String token="";
token=request.getParameter("Token");
String city="";
if (request.getParameterMap().containsKey("City"))
 {
     city = request.getParameter("City");
 }


SpiritProto obj=new SpiritProto();
result = obj.getRouteList(token,city);
%>
<%=result%>