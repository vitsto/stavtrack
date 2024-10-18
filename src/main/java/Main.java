import com.fasterxml.jackson.databind.ObjectMapper;
import model.Request;
import model.Response;
import service.RequestService;
import service.impl.RequestServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        RequestService requestService = new RequestServiceImpl();

        //read json file data to String
        byte[] jsonData = Files.readAllBytes(Paths.get("src/main/resources/coords.txt"));
        Request request = objectMapper.readValue(jsonData, Request.class);
        Response response = requestService.processRequest(request);
        System.out.println(request);
        System.out.println(response);

    }
}
