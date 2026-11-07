package com.example.lendtracker;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Screen 5: Add a new item, or Edit an existing one (if an item id is passed).
 */
public class AddEditActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private TextInputLayout tilItemName, tilFriend, tilPhone, tilDate;
    private TextInputEditText etItemName, etFriend, etPhone, etDate;
    private ImageView ivPhoto;
    private MaterialButton btnSave;

    private DatabaseReference itemsRef;
    private String editId = null;      // null = Add mode, not null = Edit mode
    private long createdAt = 0;
    private String imageBase64 = "";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    // Camera Intent: returns a small photo (Bitmap). No CAMERA permission needed.
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) setPhoto(bitmap);
            });

    // Gallery Intent: returns the Uri of the chosen image
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        setPhoto(ImageUtil.uriToBitmap(this, uri));
                    } catch (Exception e) {
                        Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilItemName = findViewById(R.id.tilItemName);
        tilFriend = findViewById(R.id.tilFriend);
        tilPhone = findViewById(R.id.tilPhone);
        tilDate = findViewById(R.id.tilDate);
        etItemName = findViewById(R.id.etItemName);
        etFriend = findViewById(R.id.etFriend);
        etPhone = findViewById(R.id.etPhone);
        etDate = findViewById(R.id.etDate);
        ivPhoto = findViewById(R.id.ivPhoto);
        btnSave = findViewById(R.id.btnSave);
        MaterialButton btnPhoto = findViewById(R.id.btnPhoto);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        itemsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid()).child("items");

        etDate.setOnClickListener(v -> showDatePicker());
        btnPhoto.setOnClickListener(v -> choosePhotoSource());
        ivPhoto.setOnClickListener(v -> choosePhotoSource());
        btnSave.setOnClickListener(v -> saveItem());

        // Data passed from Home screen with putExtra()
        editId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        if (editId != null) {
            toolbar.setTitle(R.string.edit_item);
            btnSave.setText(R.string.update);
            loadItem();
        } else {
            toolbar.setTitle(R.string.add_item);
            etDate.setText(dateFormat.format(new Date())); // today
        }
    }

    /** Edit mode: read the item once from Firebase and fill the form */
    private void loadItem() {
        itemsRef.child(editId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Item item = snapshot.getValue(Item.class);
                if (item == null) {
                    Toast.makeText(AddEditActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                etItemName.setText(item.getItemName());
                etFriend.setText(item.getFriendName());
                etPhone.setText(item.getPhone());
                etDate.setText(item.getLentDate());
                createdAt = item.getCreatedAt();
                imageBase64 = item.getImageBase64() == null ? "" : item.getImageBase64();
                Bitmap photo = ImageUtil.base64ToBitmap(imageBase64);
                if (photo != null) ivPhoto.setImageBitmap(photo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddEditActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** AlertDialog: Camera or Gallery? */
    private void choosePhotoSource() {
        String[] options = {"Take photo (Camera)", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Add photo")
                .setItems(options, (dialog, which) -> {
                    try {
                        if (which == 0) cameraLauncher.launch(null);
                        else galleryLauncher.launch("image/*");
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "No app found for this", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void setPhoto(Bitmap bitmap) {
        imageBase64 = ImageUtil.bitmapToBase64(bitmap);
        ivPhoto.setImageBitmap(ImageUtil.base64ToBitmap(imageBase64));
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day);
            etDate.setText(dateFormat.format(picked.getTime()));
            tilDate.setError(null);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveItem() {
        String itemName = getText(etItemName);
        String friend = getText(etFriend);
        String phone = getText(etPhone).replaceAll("[\\s-]", "");
        String date = getText(etDate);

        // ---- Validation ----
        tilItemName.setError(null);
        tilFriend.setError(null);
        tilPhone.setError(null);
        tilDate.setError(null);
        boolean valid = true;

        if (itemName.isEmpty()) {
            tilItemName.setError("Item name is required");
            valid = false;
        }
        if (friend.isEmpty()) {
            tilFriend.setError("Friend's name is required");
            valid = false;
        }
        if (phone.isEmpty()) {
            tilPhone.setError("Phone number is required");
            valid = false;
        } else if (!phone.matches("^\\+?[0-9]{6,15}$")) {
            tilPhone.setError("Enter a valid phone number");
            valid = false;
        }
        if (date.isEmpty()) {
            tilDate.setError("Pick a date");
            valid = false;
        }
        if (!valid) return;

        boolean isEdit = editId != null;
        String id = isEdit ? editId : itemsRef.push().getKey();
        if (id == null) {
            Toast.makeText(this, "Could not create item id", Toast.LENGTH_SHORT).show();
            return;
        }
        if (createdAt == 0) createdAt = System.currentTimeMillis();

        Item item = new Item(id, itemName, friend, phone, date, imageBase64, createdAt);

        btnSave.setEnabled(false);
        itemsRef.child(id).setValue(item)   // same call works for Add (new id) and Edit (old id)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, isEdit ? "Item updated" : "Item added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Snackbar.make(btnSave, "Save failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
