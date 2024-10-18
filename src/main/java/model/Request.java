package model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Request {
    private double[] startPos;
    private double[] endPos;
}
