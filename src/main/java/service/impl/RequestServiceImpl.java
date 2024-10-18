package service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Request;
import model.Response;
import service.RequestService;
import utils.Geocoder;


import java.io.IOException;

public class RequestServiceImpl implements RequestService {
    private final Geocoder geocoder = new Geocoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Response processRequest(Request request) {
        Response response = new Response();
        response.setStartPos(request.getStartPos());
        response.setEndPos(request.getEndPos());
        try {
            response.setStartAddress(getGeocoderStringResponse(request.getStartPos()));
            response.setEndAddress(getGeocoderStringResponse(request.getEndPos()));
            response.setDistance(calculateDistance(request.getStartPos(), request.getEndPos()));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    double calculateDistance(double[] startPos, double[] endPos) {
        double SEMI_MAJOR_AXIS_MT = 6378137;
        double SEMI_MINOR_AXIS_MT = 6356752.314245;
        double FLATTENING = 1 / 298.257223563;
        double ERROR_TOLERANCE = 1e-12;

        double latitude1 = startPos[0];
        double longitude1 = startPos[1];
        double latitude2 =  endPos[0];
        double longitude2 = endPos[1];
        double U1 = Math.atan((1 - FLATTENING) * Math.tan(Math.toRadians(latitude1)));
        double U2 = Math.atan((1 - FLATTENING) * Math.tan(Math.toRadians(latitude2)));

        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);

        double longitudeDifference = Math.toRadians(longitude2 - longitude1);
        double previousLongitudeDifference;

        double sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;

        do {
            sinSigma = Math.sqrt(Math.pow(cosU2 * Math.sin(longitudeDifference), 2) +
                    Math.pow(cosU1 * sinU2 - sinU1 * cosU2 * Math.cos(longitudeDifference), 2));
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * Math.cos(longitudeDifference);
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * Math.sin(longitudeDifference) / sinSigma;
            cosSqAlpha = 1 - Math.pow(sinAlpha, 2);
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) {
                cos2SigmaM = 0;
            }
            previousLongitudeDifference = longitudeDifference;
            double C = FLATTENING / 16 * cosSqAlpha * (4 + FLATTENING * (4 - 3 * cosSqAlpha));
            longitudeDifference = Math.toRadians(longitude2 - longitude1) + (1 - C) * FLATTENING * sinAlpha *
                    (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * Math.pow(cos2SigmaM, 2))));
        } while (Math.abs(longitudeDifference - previousLongitudeDifference) > ERROR_TOLERANCE);

        double uSq = cosSqAlpha * (Math.pow(SEMI_MAJOR_AXIS_MT, 2) - Math.pow(SEMI_MINOR_AXIS_MT, 2)) / Math.pow(SEMI_MINOR_AXIS_MT, 2);

        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * Math.pow(cos2SigmaM, 2))
                - B / 6 * cos2SigmaM * (-3 + 4 * Math.pow(sinSigma, 2)) * (-3 + 4 * Math.pow(cos2SigmaM, 2))));

        double distanceMt = SEMI_MINOR_AXIS_MT * A * (sigma - deltaSigma);
        return distanceMt;
    }

    private String getGeocoderStringResponse(double[] coords) throws IOException, InterruptedException {
        String json = geocoder.geocodeSync(coords[0] + "," + coords[1]);
        JsonNode jsonNode = objectMapper.readTree(json);
        return jsonNode.get("response")
                .get("GeoObjectCollection")
                .get("featureMember")
                .get(0)
                .get("GeoObject")
                .get("metaDataProperty")
                .get("GeocoderMetaData")
                .get("text").asText();
    }

}
