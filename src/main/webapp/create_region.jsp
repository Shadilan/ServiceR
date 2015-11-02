<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.AdminTools"%>
<%
String result;
try
{
AdminTools obj=new AdminTools();

String countS=request.getParameter("COUNT");
int count=Integer.parseInt(countS);
String Lat1S=request.getParameter("Lat1");
String Lat2S=request.getParameter("Lat2");
String Lng1S=request.getParameter("Lng1");
String Lng2S=request.getParameter("Lng2");
int Lat1=Integer.parseInt(Lat1S);
int Lng1=Integer.parseInt(Lng1S);
int Lat2=Integer.parseInt(Lat2S);
int Lng1=Integer.parseInt(Lng2S);
result=obj.GenCity(Lat1,Lng1,Lat2,Lng2,count);
} catch (Exception e)
{
result=e.toString();
}

%>
<%=result%>