package no.uio.in2000.team16.flynerd.flightData

class Appendix {
    var airlines: List<Airline>? = null
    var airports: List<Airport>? = null

    class Airline {
        var fs: String? = null
        var iata: String? = null
        var icao: String? = null
        var name: String? = null
        var active: Boolean = false
    }

    class Airport {
        var fs: String? = null
        var iata: String? = null
        var icao: String? = null
        var faa: String? = null
        var name: String? = null
        var city: String? = null
        var cityCode: String? = null
        var stateCode: String? = null
        var countryCode: String? = null
        var countryName: String? = null
        var regionName: String? = null
        var timeZoneRegionName: String? = null
        var weatherZone: String? = null
        var localTime: String? = null
        var utcOffsetHours: Double? = 0.0
        var latitude: Double? = 0.0
        var longitude: Double? = 0.0
        var elevationFeet: Int? = 0
        var classification: Int? = 0
        var active: Boolean? = false
        var weatherUrl: String? = null
        var delayIndexUrl: String? = null
    }
}
