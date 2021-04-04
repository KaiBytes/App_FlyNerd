package no.uio.in2000.team16.flynerd

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

private val ns: String? = null

class ForecastParser {
    @Throws(XmlPullParserException::class, IOException::class)

    fun parse(inputStream: InputStream): List<*> {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): List<Forecast> {
        val entries = mutableListOf<Forecast>()

        parser.require(XmlPullParser.START_TAG, ns, "metno:aviationProducts")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag
            if (parser.name == "metno:terminalAerodromeForecast") {
                entries.add(readEntry(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    private fun readEntry(parser: XmlPullParser): Forecast {
        parser.require(XmlPullParser.START_TAG, ns, "metno:terminalAerodromeForecast")
        var forecastString: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "metno:tafText" -> forecastString = readForecastString(parser)
                else -> skip(parser)
            }
        }
        return Forecast(forecastString)
    }

    private fun readForecastString(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "metno:tafText")
        val forecastString = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "metno:tafText")
        return forecastString
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

}

data class Forecast(val forecastString: String?) {
    override fun toString(): String {
        return forecastString.toString()
    }
}