<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.AdminTools"%>
<%
String result;
try
{
AdminTools obj=new AdminTools();

String countS=request.getParameter("COUNT");
String Lat1S=request.getParameter("Lat1");
String Lat2S=request.getParameter("Lat2");
String Lng1S=request.getParameter("Lng1");
String Lng2S=request.getParameter("Lng2");

result="Test";
} catch (Exception e)
{
result=e.toString();
}

%>
<%=countS%><br/>
<%=Lat1S%><br/>
<%=Lat2S%><br/>
<%=Lng1S%><br/>
<%=Lng2S%><br/>
<%=result%>