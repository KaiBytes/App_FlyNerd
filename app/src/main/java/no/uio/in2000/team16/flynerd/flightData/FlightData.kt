package no.uio.in2000.team16.flynerd.flightData

/**
 * flifht data class holding list of flight stustus and apendix from internal classes
 */
class FlightData(
    var appendix: Appendix?,
    var flightStatuses: List<FlightStatus>?
)