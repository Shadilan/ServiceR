<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.SpiritProto"%>
<%
String result;
try
{
String token="";
String token=request.getParameter("Token");
String city="";
city = request.getParameter("City");

SpiritProto obj=new SpiritProto();
result = obj.getRouteList(token,city);
} catch (Exception e)
{
  result=e.toString();
}
%>
<%=result%>