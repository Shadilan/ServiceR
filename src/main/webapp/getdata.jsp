<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.SpiritProto"%>
<%
String token=request.getParameter("Token");
String LatS=request.getParameter("Lat");
String LngS=request.getParameter("Lng");
int Lat=Integer.parseInt(LatS);
int Lng=Integer.parseInt(LngS);
SpiritProto obj=new SpiritProto();
String result = obj.GetData(token, Lat, Lng);

%>    
<%=result%>