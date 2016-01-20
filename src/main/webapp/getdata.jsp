<%@ page language="java" contentType="text/plain; charset=UTF8"
    pageEncoding="UTF8"%>
    <%@page import="main.SpiritProto"%>
<%
String token=request.getParameter("Token");
String ReqName=request.getParameter("ReqName");
String LatS=request.getParameter("Lat");
String LngS=request.getParameter("Lng");
String PLatS=request.getParameter("PLat");
String PLngS=request.getParameter("PLng");
String TGUID=request.getParameter("TGUID");
double PLAT=Double.parseDouble(PLatS);
double PLNG=Double.parseDouble(PLngS);
double LAT=Double.parseDouble(LatS);
double LNG=Double.parseDouble(LngS);
Client client=new Client();
Player player=new Player(client.getCon(), token);
String PGUID=player.getGUID();
String result = Client.SendData(nvl(ReqName,""),nvl(PGUID,""),nvl(TGUID,""),nvl(PLAT,100),nvl(PLNG,200),nvl(LAT,100),nvl(LNG,200));
%>
<%=result%>