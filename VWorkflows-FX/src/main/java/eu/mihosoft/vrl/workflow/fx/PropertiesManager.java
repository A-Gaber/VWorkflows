package eu.mihosoft.vrl.workflow.fx;

/**
 * Created by dwigand on 24.04.2018.
 */
public class PropertiesManager {
    private static PropertiesManager instance = null;

    public static PropertiesManager getInstance() {
        if (instance == null) {
            instance = new PropertiesManager();
        }
        return instance;
    }

    private PropertiesManager() {

    }

    // Data Stored in here!
    private double snapSigma = 10;
    private double snapLineLength = 20;

    public double getSnapSigma() {
        return snapSigma;
    }

    public void setSnapSigma(double snapSigma) {
        this.snapSigma = snapSigma;
    }

    public double getSnapLineLength() {
        return snapLineLength;
    }

    public void setSnapLineLength(double snapLineLength) {
        this.snapLineLength = snapLineLength;
    }
}
