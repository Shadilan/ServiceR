<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@page import="main.AdminTools"%>
<%
AdminTools obj=new AdminTools();


%>
<!DOCTYPE html>
<HTML>
<head>
    <title>Порталы на медаль</title>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
    <meta charset="utf-8"/>
    <style type="text/css">
          html, body { height: 100%; margin: 0; padding: 0; }
          #map { height: 100%; }
    </style>
    <link href='http://fonts.googleapis.com/css?family=Open+Sans+Condensed:300&subset=latin,cyrillic' rel='stylesheet' type='text/css'/>
	<script type="text/javascript">
	function create_marker(lat,lng,name,map)
	{
	    var Lat=lat/1E6;
        var Lng=lng/1E6;
        var latlng=new google.maps.LatLng(Lat/1E6,Lng/1E6);
        var vimg='images/city.png';
		var Marker=new google.maps.Marker({	Position:  latlng,map:map, icon:vimg, title:name});
	}
	var map;
    function initMap() {
      map = new google.maps.Map(document.getElementById('map-canvas'), {
        center: {lat: -34.397, lng: 150.644},
        zoom: 8
      });
      alert("STRANGE");
    }
	</SCRIPT>
	<script async defer
          src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDx1sHtgx3dBVdiU0zaaxq3AaEe7MaHVz8&callback=initMap">
    </script>
	<body>
	<div id="map-canvas" style='z-index:1'>MAP NOT LOAD</div>
	</body>
</HTML>