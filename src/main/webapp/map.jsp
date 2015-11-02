<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@page import="main.AdminTools"%>
<%
AdminTools obj=new AdminTools();
String result=obj.GenMap();

%>
<HTML>
<head>
    <title>Порталы на медаль</title>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
    <meta charset="utf-8"/>
    <link href='http://fonts.googleapis.com/css?family=Open+Sans+Condensed:300&subset=latin,cyrillic' rel='stylesheet' type='text/css'/>
    <script src="https://maps.googleapis.com/maps/api/js?v=3.exp"></script>
	<script>
	function create_marker(lat,lng,name,map)
	{
	    var Lat=lat/1E6;
        var Lng=lng/1E6;
        var latlng=new google.maps.LatLng(Lat/1E6,Lng/1E6);
        var vimg='images/city.png';
		var Marker=new google.maps.Marker({	Position:  latlng,map:map, icon:vimg, title:name});
	}
	function initialize() {
       lat=47.2584933;
       lng=39.7722394;
        var mapOptions = {zoom: 9, center: new google.maps.LatLng(lat, lng)}
        map = new google.maps.Map(document.getElementById('map-canvas'),mapOptions);
        <%=result%>
    };
	google.maps.event.addDomListener(window, 'load', initialize);
	</SCRIPT>
	<body>
	<div id="map-canvas" style='z-index:1'></div>
	</body>
</HTML>