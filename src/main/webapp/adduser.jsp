<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.SpiritProto"%>
<%
String Login=request.getParameter("Login");
String Password=request.getParameter("Password");
String email=request.getParameter("email");
String inviteCode=request.getParameter("InviteCode");

SpiritProto obj=new SpiritProto();
String result=obj.NewPlayer(Login, Password,email,inviteCode);
%>
<%=result%>