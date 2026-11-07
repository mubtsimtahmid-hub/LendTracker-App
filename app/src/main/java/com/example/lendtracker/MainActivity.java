package com.example.lendtracker;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Screen 4: Home. Shows all lent items in a RecyclerView.
 * The list updates in real time from Firebase Realtime Database.
 */
public class MainActivity extends AppCompatActivity implements ItemAdapter.OnItemActionListener {

    private final List<Item> items = new ArrayList<>();
    private ItemAdapter adapter;
    private TextView tvEmpty;
    private View rootView;

    private DatabaseReference itemsRef;
    private Query itemsQuery;
    private ValueEventListener itemsListener;

    // Sensor (shake to add)
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {          // safety: not logged in
            goToLogin();
            return;
        }

        rootView = findViewById(R.id.main);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Toolbar with Logout menu
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle(user.getEmail());
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(this::onMenuClick);

        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ItemAdapter(items, this);
        recyclerView.setAdapter(adapter);

        // FAB -> Add screen
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> openAddScreen());

        // Firebase: users/{uid}/items
        itemsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid()).child("items");
        listenForItems();

        // Sensor: accelerometer for shake
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        shakeDetector = new ShakeDetector(() -> {
            Toast.makeText(this, "Shake detected! Add a new item", Toast.LENGTH_SHORT).show();
            openAddScreen();
        });
    }

    /** Real-time listener: runs every time data changes in Firebase */
    private void listenForItems() {
        itemsQuery = itemsRef.orderByChild("createdAt");
        itemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Item item = child.getValue(Item.class);
                    if (item != null) {
                        item.setId(child.getKey());
                        items.add(item);
                    }
                }
                Collections.reverse(items); // newest first
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        itemsQuery.addValueEventListener(itemsListener);
    }

    private void openAddScreen() {
        startActivity(new Intent(this, AddEditActivity.class));
    }

    // ---------- Card button actions ----------

    @Override
    public void onEdit(Item item) {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra(AddEditActivity.EXTRA_ITEM_ID, item.getId()); // pass data with putExtra
        startActivity(intent);
    }

    @Override
    public void onDelete(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete item")
                .setMessage("Delete \"" + item.getItemName() + "\"? Did " + item.getFriendName() + " return it?")
                .setPositiveButton("Delete", (dialog, which) ->
                        itemsRef.child(item.getId()).removeValue()
                                .addOnSuccessListener(unused ->
                                        Snackbar.make(rootView, "Item deleted", Snackbar.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Snackbar.make(rootView, "Delete failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Implicit Intent: open the phone dialer */
    @Override
    public void onCall(Item item) {
        if (item.getPhone() == null || item.getPhone().isEmpty()) {
            Toast.makeText(this, "No phone number saved", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.getPhone()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No dialer app found", Toast.LENGTH_SHORT).show();
        }
    }

    /** Implicit Intent: share a reminder message (WhatsApp, Messenger, SMS...) */
    @Override
    public void onShare(Item item) {
        String message = "Hi " + item.getFriendName() + ", you borrowed my "
                + item.getItemName() + " on " + item.getLentDate()
                + ". Please return it when you can. Thanks!";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(intent, "Send reminder via"));
    }

    // ---------- Menu ----------

    private boolean onMenuClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Do you want to logout?")
                    .setPositiveButton("Logout", (d, w) -> {
                        FirebaseAuth.getInstance().signOut();
                        Prefs.clearRememberMe(this);
                        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                        goToLogin();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return false;
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ---------- Sensor on/off with the screen ----------

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (itemsQuery != null && itemsListener != null) {
            itemsQuery.removeEventListener(itemsListener);
        }
    }
}
