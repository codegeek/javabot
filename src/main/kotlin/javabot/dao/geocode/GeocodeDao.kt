package javabot.dao.geocode

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.inject.Inject
import javabot.JavabotConfig
import javabot.dao.geocode.model.GeocodeResponse
import javabot.dao.util.CallLimiter
import org.apache.http.client.fluent.Request
import java.util.concurrent.TimeUnit

class GeocodeDao @Inject constructor(private val javabotConfig: JavabotConfig) {
    private val limiter = CallLimiter.create() // limit to 850 every day
    private val baseUrl = "https://maps.googleapis.com/maps/api/geocode/json?key=${javabotConfig.googleAPI()}&address="

    val cache: LoadingCache<String, GeoLocation> = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(
                    object : CacheLoader<String, GeoLocation>() {
                        override fun load(key: String): GeoLocation {
                            return if (javabotConfig.googleAPI().isNotEmpty() && key.isNotEmpty()) {
                                if (limiter.tryAcquire()) {
                                    val mapper = ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                    val location = key.replace(" ", "+")
                                    val geocodeResponse = Request
                                            .Get("$baseUrl$location")
                                            .execute()
                                            .returnContent().asString()
                                    val response = mapper.readValue(geocodeResponse, GeocodeResponse::class.java)
                                    val results = response.results ?: emptyList()
                                    val geoLocation = if (results.isNotEmpty()) {
                                        results.get(0).geometry?.location
                                    } else {
                                        null
                                    }
                                    if (geoLocation != null) {
                                        GeoLocation(
                                                geoLocation.lat ?: 0.0,
                                                geoLocation.lng ?: 0.0,
                                                response.results?.get(0)?.formatted_address ?: "")
                                    } else {
                                        throw Exception("Geocode service did not return a location reference for $key")
                                    }
                                } else {
                                    throw Exception("Too many requests: geocode service is limited to ${limiter.allowed} " +
                                            "for every ${limiter.span} ${limiter.period}")
                                }
                            } else {
                                throw Exception("Geocode service has no API key defined. See javabot.properties.")
                            }
                        }
                    }
            )

    fun getLatitudeAndLongitudeFor(place: String): GeoLocation? {
        return cache.get(place)
    }
}

data class GeoLocation(val latitude: Double, val longitude: Double, val address: String)