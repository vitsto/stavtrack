package service;

import model.Request;
import model.Response;

public interface RequestService {
    Response processRequest(Request request);
}
