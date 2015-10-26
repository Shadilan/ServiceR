<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.SpiritProto"%>
<%
SpiritProto obj=new SpiritProto();
String result=obj.StartTask();

%>    
<%=result%>