<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.SpiritProto"%>
<%
 int x=request.getParameter("X");
 int y=request.getParameter("Y");
 int count=request.getParameter("COUNT");

SpiritProto obj=new SpiritProto();
String result=obj.GenCity(x,y,count);

%>
<%=result%>