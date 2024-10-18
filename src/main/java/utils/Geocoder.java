package utils;


import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Geocoder {

    private static final String GEOCODING_RESOURCE = "https://geocode-maps.yandex.ru/1.x";
    private static final String API_KEY = "5839ca78-7bcb-4829-ba03-2d91fa1422f8";

    public String geocodeSync(String query) throws IOException, InterruptedException {

        HttpClient httpClient = HttpClient.newHttpClient();

//        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String requestUri = GEOCODING_RESOURCE + "?apikey=" + API_KEY + "&geocode=" + query +
                "&lang=ru&sco=latlong&results=1&format=json";

        HttpRequest geocodingRequest = HttpRequest.newBuilder().GET().uri(URI.create(requestUri))
                .timeout(Duration.ofMillis(2000)).build();

        HttpResponse geocodingResponse = httpClient.send(geocodingRequest,
                HttpResponse.BodyHandlers.ofString());

        return geocodingResponse.body().toString();
    }
}
