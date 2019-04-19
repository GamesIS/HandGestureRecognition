package application;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Properties {
    @JsonProperty
    private double blur;
    @JsonProperty
    private double hueStart;
    @JsonProperty
    private double hueStop;
    @JsonProperty
    private double saturationStart;
    @JsonProperty
    private double saturationStop;
    @JsonProperty
    private double valueStart;
    @JsonProperty
    private double valueStop;

    public double getBlur() {
        return blur;
    }

    public void setBlur(double blur) {
        this.blur = blur;
    }

    public double getHueStart() {
        return hueStart;
    }

    public void setHueStart(double hueStart) {
        this.hueStart = hueStart;
    }

    public double getHueStop() {
        return hueStop;
    }

    public void setHueStop(double hueStop) {
        this.hueStop = hueStop;
    }

    public double getSaturationStart() {
        return saturationStart;
    }

    public void setSaturationStart(double saturationStart) {
        this.saturationStart = saturationStart;
    }

    public double getSaturationStop() {
        return saturationStop;
    }

    public void setSaturationStop(double saturationStop) {
        this.saturationStop = saturationStop;
    }

    public double getValueStart() {
        return valueStart;
    }

    public void setValueStart(double valueStart) {
        this.valueStart = valueStart;
    }

    public double getValueStop() {
        return valueStop;
    }

    public void setValueStop(double valueStop) {
        this.valueStop = valueStop;
    }
}
