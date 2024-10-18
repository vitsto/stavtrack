package model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Response {
    private double[] startPos;
    private double[] endPos;
    private String startAddress;
    private String endAddress;
    private  double distance;
}
