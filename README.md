# Lend Tracker 📦

**Never forget who borrowed your stuff.**

Lend Tracker is an Android app that helps students keep track of things they lend to friends
(calculators, books, chargers, notes, money…). Each record stores what was lent, to whom,
the friend's phone number, the date and a photo. You can call the friend or send a reminder
message straight from the app.

- **Course:** Mobile Application and Development Lab (0613-410), DIU
- **Student:** Kazi Mubtasim Tahmid Mahin
- **Student ID:** CS-D-85-22-128741-CT

## Features
1. 5 screens: Splash, Login, Register, Home (list), Add/Edit — Activities + Intents, data passed with `putExtra()`
2. Material Design 3 UI with ConstraintLayout, LinearLayout, TextView, ImageView, Button, MaterialCardView
3. Input validation with `TextInputLayout.setError()` (no crash on empty or wrong input)
4. Model class `Item` with getters/setters
5. Scrolling list with RecyclerView + Adapter + ViewHolder
6. Full CRUD — FAB to add, tap a card to edit, delete with AlertDialog confirmation
7. Firebase Authentication — register, login, logout, stay logged in
8. Firebase Realtime Database — list updates in real time (`ValueEventListener`)
9. SharedPreferences — "Remember me" and saved email
10. Take a photo (Camera) or pick one (Gallery) and show it
11. Accelerometer sensor — **shake the phone** on Home to add a new item
12. Implicit Intents — `ACTION_DIAL` (call friend), `ACTION_SEND` (share reminder)

Also: custom app name and icon, Material theme, Toast and Snackbar messages, DatePicker.

## Tech
Java, XML, Android Studio, Firebase Authentication, Firebase Realtime Database, Material Components.

## Firebase database structure
```
users
 └── {uid}
      ├── profile
      │    ├── name
      │    └── email
      └── items
           └── {itemId}
                ├── id
                ├── itemName
                ├── friendName
                ├── phone
                ├── lentDate
                ├── imageBase64
                └── createdAt
```

## How to run
1. Clone this repository and open it in Android Studio.
2. The project already contains `app/google-services.json` (Firebase config).
   To use your own Firebase project, replace it with yours and enable
   *Email/Password* sign-in and *Realtime Database*.
3. Click **Run ▶** on an emulator or a phone (Android 7.0+).
4. Or download the APK from the **Releases** section (v1.0) and install it.

## Screenshots
_Add screenshots here_
