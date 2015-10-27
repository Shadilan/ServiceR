<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.AdminTools"%>
<%
AdminTools obj=new AdminTools();
String result=obj.StartTask();

%>    
<%=result%>