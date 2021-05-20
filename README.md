# Flynerd APP ✈️
### What is Flynerd? 🛫
Flynerd is an app which can assist you with your flights. It can give you information about flights, flight status, airport weather and more. 🔥 For example, you can use Flynerd to check all the airplanes currently in the air in Norway or you may also check the status of a specific flight when you want to go to the airport to pick up a friend. FLynerd is an app written in Kotlin.

### Requirements 🛩️
* Android Studio
* Git
* Android build tools 
* (An Android phone)

### Common setup 🛫
* Clone the repo and install the dependencies.
* git clone + project link
* Open Android Studio and create a new project from the just downloaded source.

### Others  🛩️

###  API and how to use keys 🛩️

NB. Current api key for flightstatus valid from May 20-2021 to June 20-2021

in this project we used three API for different data fetching purposes

1.  MET-Api for location and weather forecast (Used in airport info & weather forecast)
   https://api.met.no/weatherapi/tafmetar/1.0/documentation

2 OpenSky api for displaying airplane in air over Norway at moment  
    https://opensky-network.org/
	
3. FlightStatus api from Flightstatus by  Cirium for flight status checking 
    https://www.flightstats.com/v2  
    
We fetch flight status data and  used FlightStatus API from provider and 
created developer account on
https://developer.flightstats.com/ 

The current flight Status API account is created as evaluation plan 
for free and valid for one month , the evaluation period:  valid (20th May -2021 to 20th June 2021)

After this evaluation period if one try to run the apps flight status may not work
so in this case you may need to use your account by signing app at
 https://developer.flightstats.com/ 
 and request new api ID and key according to your plan

