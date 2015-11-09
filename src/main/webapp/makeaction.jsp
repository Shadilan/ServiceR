<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@page import="main.SpiritProto"%>
<%
//Убрать это все в объект
String Token=request.getParameter("Token");
String LatS=request.getParameter("Lat");
String LatL=request.getParameter("Lng");
String Action=request.getParameter("Action");
String Target=request.getParameter("Target");

SpiritProto obj=new SpiritProto();
obj.ConnectDB();
obj.action(Token,LatS,LatL,Target,Action);
%>