package application.db;

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
    @JsonProperty
    private double Y_MIN;
    @JsonProperty
    private double Y_MAX;
    @JsonProperty
    private double Cr_MIN;
    @JsonProperty
    private double Cr_MAX;
    @JsonProperty
    private double Cb_MIN;
    @JsonProperty
    private double Cb_MAX;
    @JsonProperty
    private double kernel;
    @JsonProperty
    private double sigma;

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    public double getKernel() {
        return kernel;
    }

    public void setKernel(double kernel) {
        this.kernel = kernel;
    }

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

    public double getY_MIN() {
        return Y_MIN;
    }

    public void setY_MIN(double y_MIN) {
        Y_MIN = y_MIN;
    }

    public double getY_MAX() {
        return Y_MAX;
    }

    public void setY_MAX(double y_MAX) {
        Y_MAX = y_MAX;
    }

    public double getCr_MIN() {
        return Cr_MIN;
    }

    public void setCr_MIN(double cr_MIN) {
        Cr_MIN = cr_MIN;
    }

    public double getCr_MAX() {
        return Cr_MAX;
    }

    public void setCr_MAX(double cr_MAX) {
        Cr_MAX = cr_MAX;
    }

    public double getCb_MIN() {
        return Cb_MIN;
    }

    public void setCb_MIN(double cb_MIN) {
        Cb_MIN = cb_MIN;
    }

    public double getCb_MAX() {
        return Cb_MAX;
    }

    public void setCb_MAX(double cb_MAX) {
        Cb_MAX = cb_MAX;
    }
}
