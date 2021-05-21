package no.uio.in2000.team16.flynerd.airportweatherdata

import android.content.Context
import no.uio.in2000.team16.flynerd.Airport
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito

class AirportsListTest {
    //mockup context and airportList
    private val mockAirportsList: MutableList<Airport> = mutableListOf()
    private val mockContext: Context = Mockito.mock(Context::class.java)
    private val testAirportsList: AirportsList = AirportsList(mockContext, 0, mockAirportsList)

    //check if the it creates a right airport
    @Test
    fun testCreateAirport() {
        val expectedAirport = Airport("ICAO1", "City1", "Name1", "Country1", 0.0, 0.0)
        val createdAirport =
            testAirportsList.createAirport("ICAO1", "City1", "Name1", "Country1", 0.0, 0.0)
        assertEquals(expectedAirport.ICAO, createdAirport.ICAO)
        assertEquals(expectedAirport.city, createdAirport.city)
        assertEquals(expectedAirport.country, createdAirport.country)
        assertEquals(expectedAirport.name, createdAirport.name)
        assertEquals(expectedAirport.latitude, createdAirport.latitude, 0.0)
        assertEquals(expectedAirport.longtitude, createdAirport.longtitude, 0.0)
    }

    //check if append airport is working
    @Test
    fun testAppendToList() {
        val oldListCount = mockAirportsList.count()
        val newAirport = Airport("ICAO1", "City1", "Name1", "Country1", 0.0, 0.0)
        testAirportsList.appendToList(newAirport)
        assertEquals(oldListCount + 1, mockAirportsList.count())
    }
}