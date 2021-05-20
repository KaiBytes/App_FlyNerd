package no.uio.in2000.team16.flynerd.flightData


/**
 * an internal class holding information list of airline & airport as obtained from flightstatus API
 */

class Appendix(var airlines: List<Airline>?, var airports: List<Airport>?) {


    /**
     * an internal class reprasentation of Airline and its standeard flight number designation iata & icao
     */
    class Airline (
        var fs: String? ,
        var iata: String? ,
        var icao: String? ,
        var name: String? ,
        var active: Boolean
        )


    /**
     * an internal class reprasentation of Airport and its info as obtained from flight staus json API

     */

    class Airport (
        var fs: String? ,
        var iata: String? ,
        var icao: String? ,
        var faa: String? ,
        var name: String? ,
        var city: String?,
        var cityCode: String? ,
        var stateCode: String? ,
        var countryCode: String? ,
        var countryName: String? ,
        var regionName: String? ,
        var timeZoneRegionName: String? ,
        var weatherZone: String? ,
        var localTime: String? ,
        var utcOffsetHours: Double?,
        var latitude: Double? ,
        var longitude: Double? ,
        var elevationFeet: Int? ,
        var classification: Int? ,
        var active: Boolean?,
        var weatherUrl: String?,
        var delayIndexUrl: String?
    )
}
