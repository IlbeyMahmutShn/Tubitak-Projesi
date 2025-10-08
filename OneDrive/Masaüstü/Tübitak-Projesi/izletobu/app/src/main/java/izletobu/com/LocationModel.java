package izletobu.com;

public class LocationModel {
    public double Lat;
    public double Lon;
    public int Tahmin;

    public LocationModel() {}

    public LocationModel(double Lat, double Lon, int Tahmin) {
        this.Lat = Lat;
        this.Lon = Lon;
        this.Tahmin = Tahmin;
    }
}
