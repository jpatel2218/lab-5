package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });

        // Set up long click listener for delete functionality
        cityListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            deleteCity(city);
            return true;
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null){
                Log.e("Firestore", error.toString());
                return;
            }

            if (value != null && !value.isEmpty()) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");
                    String documentId = snapshot.getId();

                    City city = new City(name, province);
                    city.setDocumentId(documentId);
                    cityArrayList.add(city);
                }

                cityArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateCity(City city, String title, String year) {
        // Get the old document ID before updating
        String oldDocumentId = city.getDocumentId();

        // Update local object
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Update Firestore: delete old document and create new one
        if (oldDocumentId != null && !oldDocumentId.isEmpty()) {
            // Delete old document
            citiesRef.document(oldDocumentId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Old document deleted successfully");
                        // Add new document with updated data
                        Map<String, Object> cityData = new HashMap<>();
                        cityData.put("name", city.getName());
                        cityData.put("province", city.getProvince());

                        DocumentReference newDocRef = citiesRef.document(city.getName());
                        newDocRef.set(cityData)
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d("Firestore", "City updated successfully");
                                    city.setDocumentId(city.getName());
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Error updating city", e));
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error deleting old document", e));
        }
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        Map<String, Object> cityData = new HashMap<>();
        cityData.put("name", city.getName());
        cityData.put("province", city.getProvince());

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(cityData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "City added successfully");
                    city.setDocumentId(city.getName());
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding city", e));
    }

    @Override
    public void deleteCity(City city) {
        // Remove from local list
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();

        // Delete from Firestore
        String documentId = city.getDocumentId();
        if (documentId != null && !documentId.isEmpty()) {
            citiesRef.document(documentId).delete()
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "City deleted successfully"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error deleting city", e));
        }
    }

    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}