package no.uio.in2000.team16.flynerd.flightData

/**
 * a data class for represantation of flight status as obtained from api Json
 */
class FlightStatus(
    var flightId: Long,
    var carrierFsCode: String?,
    var flightNumber: String?,
    var departureAirportFsCode: String?,
    var arrivalAirportFsCode: String?,
    var departureDate: Date?,
    var arrivalDate: Date?,
    var status: String?,
    var delays: Delays?
) {

    /**
     * internal data class for date
     */
    class Date(
        var dateUtc: String?,
        var dateLocal: String?
    )

    /**
     * internal data class for delay information as obtained from api
     */
    class Delays(
        var departureRunwayDelayMinutes: Int = 0,
        var arrivalGateDelayMinutes: Int = 0
    )
}