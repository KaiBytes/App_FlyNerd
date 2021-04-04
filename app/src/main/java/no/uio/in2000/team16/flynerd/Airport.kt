package no.uio.in2000.team16.flynerd

class Airport(val ICAO : String,
              val name : String,
              val country : String,
              val latitude : Double,
              val longtitude : Double,
              val currentMETAR : String? = null,
              var forecastString: String?)
{

    override fun toString(): String {
        return  "ICAO" + ":".padStart(10) + "$ICAO\n" +
                "name" + ":".padStart(10) + "$name\n" +
                "country" + ":".padStart(7) + "$country\n" +
                "latitude" + ":".padStart(6) + "$latitude\n" +
                "longtitude" + ":".padStart(4) + "$longtitude\n" +
                "forecast" + ":".padStart(6) + "$forecastString";
    }


}