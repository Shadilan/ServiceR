<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.AdminTools"%>
<%
String result;
try
{
AdminTools obj=new AdminTools();

result="Test";
} catch (Exception e)
{
result=e.toString();
}

%>
<%=result%>