package izletobu.com;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference tahminlerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Harita fragment'ını başlat
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Firebase "tahminler" referansını al
        tahminlerRef = FirebaseDatabase.getInstance().getReference("tahminler");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Başlangıçta haritayı Türkiye'ye odakla
        LatLng merkez = new LatLng(39.92, 32.85); // Ankara
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(merkez, 6));

        // Firebase'den tahmin verilerini dinle
        tahminlerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear(); // Önceki marker'ları temizle

                for (DataSnapshot zamanSnap : snapshot.getChildren()) {
                    Double lat = zamanSnap.child("Lat").getValue(Double.class);
                    Double lon = zamanSnap.child("Lon").getValue(Double.class);
                    Integer tahmin = zamanSnap.child("Tahmin").getValue(Integer.class);
                    String zaman = zamanSnap.child("Zaman").getValue(String.class);

                    if (lat != null && lon != null && tahmin != null) {
                        LatLng konum = new LatLng(lat, lon);

                        float markerColor = (tahmin == 0)
                                ? BitmapDescriptorFactory.HUE_GREEN
                                : BitmapDescriptorFactory.HUE_RED;

                        mMap.addMarker(new MarkerOptions()
                                .position(konum)
                                .title("Zaman: " + zaman)
                                .snippet("Tahmin: " + tahmin)
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Hata loglama yapılabilir
            }
        });
    }
}
