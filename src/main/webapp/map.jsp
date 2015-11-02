<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@page import="main.AdminTools"%>
    <@AdminTools obj=new AdminTools();
    String result=obj.GenMap();%>
<!DOCTYPE html>
<html>
  <head>
    <style type="text/css">
      html, body { height: 100%; margin: 0; padding: 0; }
      #map { height: 100%; }
    </style>
  </head>
  <body>
    <div id="map"></div>
    <script type="text/javascript">
function create_marker(lat,lng,name,mapa)
	{
	    var Lat=lat/1E6;
        var Lng=lng/1E6;
        var latlng=new google.maps.LatLng(Lat/1E6,Lng/1E6);
        var vimg='images/city.png';
		var Marker=new google.maps.Marker({	Position:  latlng,map:mapa, icon:vimg, title:name});
	}
var map;
function initMap() {
  map = new google.maps.Map(document.getElementById('map'), {
    center: {lat: 47.2584933, lng: 39.7722394},
    zoom: 8
  });

}

    </script>
    <script async defer
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDx1sHtgx3dBVdiU0zaaxq3AaEe7MaHVz8&callback=initMap">
    </script>
  </body>
</html>