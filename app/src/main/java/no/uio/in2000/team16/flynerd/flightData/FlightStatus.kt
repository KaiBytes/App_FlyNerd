package no.uio.in2000.team16.flynerd.flightData

class FlightStatus {
    var flightId: Long = 0
    var carrierFsCode: String? = null
    var flightNumber: String? = null
    var departureAirportFsCode: String? = null
    var arrivalAirportFsCode: String? = null
    var departureDate: Date? = null
    var arrivalDate: Date? = null
    var status: String? = null
    var delays: Delays? = null

    class Date {
        var dateUtc: String? = null
        var dateLocal: String? = null
    }

    class Delays {
        var departureRunwayDelayMinutes = 0
        var arrivalGateDelayMinutes = 0
    }
}