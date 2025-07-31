# ToDo91 - A Modern Android Notes Application

ToDo91 is a feature-rich to-do list and note-taking application for Android, built entirely with modern technologies including Kotlin, Jetpack Compose, and Firebase. It is designed to be a fast, responsive, and intuitive tool for managing your daily tasks and ideas, taking inspiration from apps like Google Keep.

---

## ✨ Features

- **Modern UI**: Built with Jetpack Compose and Material 3, supporting Dynamic Theming (Material You) and a seamless edge-to-edge display.
- **Firebase Backend**:
    - **Authentication**: Secure sign-in with Email/Password and Google Sign-In.
    - **Realtime Database**: Notes are stored in Firestore and sync in real-time across devices.
- **Note Management**:
    - Create, edit, and delete notes.
    - **Pinning**: Keep important notes at the top of your list.
    - **Archiving**: Clean up your main screen without deleting notes.
    - **Color Coding**: Organize your notes with a palette of colors.
- **Flexible Note Types**: Switch between a simple text note and an interactive checklist.
- **Powerful Organization**:
    - **Search**: Quickly find notes by title or content.
    - **Filtering**: View all, completed, or incomplete notes.
    - **Sorting**: Arrange notes by date, title, or completion status.
- **Batch Operations**: A contextual action bar appears on long-press to pin, archive, or delete multiple notes at once.
- **Reminders**: Set time-based notifications for your notes.
- **Responsive Design**: The note grid adapts from 2 to 4 columns based on screen size.
- **Theme Persistence**: Your choice of Light, Dark, or System Default theme is saved and remembered when the app restarts.

---

## 🛠️ Tech Stack & Architecture

This project follows modern Android app development best practices.

- **Language**: **Kotlin**
- **UI Toolkit**: **Jetpack Compose**
- **Architecture**: **MVVM (Model-View-ViewModel)**
- **Asynchronous Programming**: **Kotlin Coroutines & Flows** for managing background tasks and reactive data streams.
- **Backend & Database**: **Firebase Authentication** & **Cloud Firestore**
- **Navigation**: **Jetpack Navigation for Compose**
- **Dependency Management**: **Gradle** with **Version Catalogs**
- **Theme Persistence**: **Jetpack DataStore**

### Project Structure

The project is organized by feature into the following packages:

-   `auth`: Contains the Login and Signup screens.
-   `home`: The main notes list screen, top bar, and filtering logic.
-   `archive`: The screen for viewing archived notes.
-   `taskdetail`: The screen for creating and editing individual notes.
-   `model`: Data classes for `Todo` and `ChecklistItem`.
-   `viewmodel`: The ViewModels that manage UI state and business logic.
-   `navigation`: The navigation graph (`AppNavHost`), `AppDrawer`, and `Screen` definitions.
-   `reminders`: The `AlarmScheduler` and `BroadcastReceiver` for handling notifications.
-   `common`: Reusable UI components like `TodoItem`, `EmptyScreen`, etc.
-   `ui/theme`: App-wide theming, colors, and typography.

---

## 🚀 Setup and Installation

To build and run this project yourself, follow these steps:

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/Vnj91/ToDo.git](https://github.com/Vnj91/ToDo.git)
    ```

2.  **Firebase Setup:**
    - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    - In the project settings, add a new Android app with the package name `com.example.todo91`.
    - Download the `google-services.json` file and place it in the `app/` directory of the project.

3.  **Enable Authentication Methods:**
    - In the Firebase Console, go to the "Authentication" section.
    - Under the "Sign-in method" tab, enable **Email/Password** and **Google**.

4.  **Add SHA-1 Fingerprint (for Google Sign-In):**
    - Open the project in Android Studio.
    - Open the Terminal (`View` > `Tool Windows` > `Terminal`).
    - Run the command `./gradlew signingReport` to get your debug SHA-1 key.
    - In your Firebase project settings, under the "General" tab, scroll down to your Android app and click "Add fingerprint".
    - Paste your SHA-1 key and save.

5.  **Build and Run:**
    - Sync the project with Gradle files.
    - Build and run the app on an emulator or a physical device.

---
