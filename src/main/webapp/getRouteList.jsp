<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.SpiritProto"%>
<%
String token=null;
String token=request.getParameter("Token");
String city=null;

SpiritProto obj=new SpiritProto();
String result = obj.getRouteList(token,city);

%>
<%=result%>