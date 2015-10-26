<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.SpiritProto"%>
<%
String xS=request.getParameter("X");
String yS=request.getParameter("Y");
String countS=request.getParameter("COUNT");
int x=Integer.parseInt(xS);
int y=Integer.parseInt(yS);
int count=Integer.parseInt(countS);

SpiritProto obj=new SpiritProto();
String result=obj.GenCity(x,y,count);

%>
<%=result%>