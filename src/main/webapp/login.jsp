<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.SpiritProto"%>
<%
String Login=request.getParameter("Login");
String Password=request.getParameter("Password");

SpiritProto obj=new SpiritProto();
obj.ConnectDB();
String token=obj.GetToken(Login, Password);
%>    
<%=token%>