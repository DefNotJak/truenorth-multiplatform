import SwiftUI
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        // GitLive's firebase-kotlin-sdk is a thin wrapper over the native
        // Firebase iOS SDK, which throws on every API call until the default
        // app is configured. On Android the google-services Gradle plugin
        // wires this up during the build; iOS has no equivalent, so it has to
        // happen here, before any Kotlin code reaches Firebase.auth.
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
