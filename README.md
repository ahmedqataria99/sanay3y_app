# Sanay3y: Professional Service Marketplace

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02.00-green.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-33.1.0-orange.svg?style=flat&logo=firebase)](https://firebase.google.com)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-red.svg?style=flat)](https://developer.android.com/topic/libraries/architecture)

Sanay3y is a robust, production-ready Android application designed as a professional marketplace connecting skilled service providers with clients in Egypt. The platform leverages modern Android development practices to provide a seamless, real-time ecosystem for home maintenance and technical services.

---

## Executive Summary

The application addresses the critical challenge of service discovery and quality assurance in the informal labor market. By providing a verified, transparent, and trackable environment, Sanay3y establishes trust between independent technicians and homeowners.

* **Value Proposition:** Centralized service discovery, location-based expert matching, and end-to-end job lifecycle management.
* **Core Problem Solved:** Lack of standardized pricing, difficulty in verifying technician credentials, and absence of a structured communication channel.

---

## Technical Features

### Client-Side Module
* **Advanced Search and Discovery:** Multi-criteria filtering by service category, ratings, and price points.
* **Proximity-Based Matching:** Real-time distance calculation using GPS coordinates to find local experts.
* **Job Lifecycle Management:** Comprehensive tracking from initial request through quotation acceptance to completion.
* **Reputation System:** Immutable feedback loop for provider ratings and community trust.
* **Internationalization:** Native support for English and Arabic with full RTL (Right-to-Left) layout persistence.

### Provider-Side Module
* **Professional Onboarding:** Specialized workflow for profile identity and expertise configuration.
* **Digital Verification:** Secure KYC (Know Your Customer) process via National ID and Police Clearance document uploads.
* **Service Dashboard:** Centralized command center for managing incoming requests, active jobs, and performance metrics.
* **Quotation Engine:** Structured bidding system allowing labor and material cost breakdowns.
* **Availability Management:** Real-time status toggle for dynamic workload control.

---

## System Architecture

The application is built on the **MVVM (Model-View-ViewModel)** architectural pattern, adhering to the principles of Clean Architecture and Unidirectional Data Flow (UDF).

### Component Layers
1. **Presentation Layer:** Built with **Jetpack Compose**, utilizing declarative UI components that observe immutable state flows.
2. **Domain/ViewModel Layer:** Orchestrates business logic and manages UI state using `StateFlow` and `SharedFlow`.
3. **Repository Layer:** Serves as a mediator between remote Firebase data sources and local SharedPreferences.
4. **Data Source Layer:** Integrates Firebase (Firestore, Auth, Storage) for real-time synchronization and document hosting.

---

## Technology Stack

### Core Frameworks
* **Kotlin:** 1.9.22 (Coroutines, StateFlow)
* **Jetpack Compose:** Modern declarative UI framework.
* **Material Design 3:** Implementation of Google's latest design system.

### Infrastructure (Firebase Suite)
* **Cloud Firestore:** Real-time NoSQL database for structured data.
* **Firebase Authentication:** Secure identity management and session persistence.
* **Firebase Storage:** Binary object storage for high-resolution verification documents and profile assets.

### Utilities and Libraries
* **Coil:** Efficient, coroutine-based image loading.
* **Compose Navigation:** Type-safe routing and deep-link handling.
* **SharedPreferences:** Encrypted local storage for session and localization settings.

---

## Database Schema (Firestore)

### Users Collection
Stores unified user profiles with role-based attributes.
* **Attributes:** uid, name, email, role, location_coordinates.
* **Provider Specifics:** category, hourly_rate, experience_years, verification_urls, status_online.

### Requests Collection
Tracks the state of service transactions.
* **Attributes:** request_id, client_id, provider_id, current_status, cost_labor, cost_materials, service_timestamp.

### Reviews Collection
Maintains the integrity of the rating system.
* **Attributes:** review_id, provider_id, client_id, rating_value, comment_text.

---

## Localization Strategy
The application implements a robust localization framework:
* **LocaleHelper:** Custom implementation for dynamic context switching without application restart.
* **Resource Management:** Modular `strings.xml` and `values-ar/strings.xml` for complete UI internationalization.
* **State Persistence:** User language preference is cached locally and injected at the `attachBaseContext` level.

---

## Installation and Deployment

### 1. Repository Configuration
```bash
git clone https://github.com/your-username/sanay3y_app.git
```

### 2. Environment Setup
* Register the application in the [Firebase Console](https://console.firebase.google.com/).
* Configure `com.sanay3y.egy` as the application ID.
* Download and integrate `google-services.json` into the `/app` directory.

### 3. Service Activation
* Enable **Email/Password** provider in Firebase Authentication.
* Initialize **Cloud Firestore** in production or test mode.
* Configure **Firebase Storage** buckets and set appropriate security rules.

---

## Technical Requirements
* **IDE:** Android Studio Jellyfish (2023.3.1) or higher.
* **Minimum SDK:** API 24 (Android 7.0).
* **Target SDK:** API 34 (Android 14).
* **JDK:** Version 17.
* **Gradle:** Version 8.2+.

---

## Roadmap and Future Iterations
* **Real-time Communication:** Integrated XMPP or Firebase-based chat system.
* **Push Notifications:** FCM integration for critical job status updates.
* **Geofencing:** Advanced provider tracking via Google Maps SDK.
* **Financial Integration:** Support for digital wallets and credit card processing.

---

## Development Team
* **Ahmed Qataria** - Authentication & Translation.
* **Ali Mohamed** - Client (home page & my jobs) & Testing  & Search.
* **Zeyad Abdelnaser** - Request & Job tracking & Quotation .
* **Jana Mohamed** - Provider job board (Available requests & Active jobs) & Job details & Search.
* **Abdullah Yousry** - Provider profile setup.
* **Sohaila Radwan** - Rating.

---

## License
This project is licensed under the MIT License - see the LICENSE file for details.

---

## Contact and Professional Networking
* **GitHub:** [your-profile-link]
* **LinkedIn:** [your-linkedin-profile]
* **Professional Email:** [your-email@address.com]
