package map.net.apscanner.classes.acquisition_set;

/**
 * Created by adriano on 9/7/16.
 */
public class AcquisitionSet {

    private String normalization_algorithm;
    private float time_interval;
    private int measures_per_point;

    public AcquisitionSet(String normalization_algorithm, float time_interval, int measures_per_point) {
        this.normalization_algorithm = normalization_algorithm;
        this.time_interval = time_interval;
        this.measures_per_point = measures_per_point;
    }

    public String getNormalization_algorithm() {
        return normalization_algorithm;
    }

    public void setNormalization_algorithm(String normalization_algorithm) {
        this.normalization_algorithm = normalization_algorithm;
    }

    public float getTime_interval() {
        return time_interval;
    }

    public void setTime_interval(float time_interval) {
        this.time_interval = time_interval;
    }

    public int getMeasures_per_point() {
        return measures_per_point;
    }

    public void setMeasures_per_point(int measures_per_point) {
        this.measures_per_point = measures_per_point;
    }
}
