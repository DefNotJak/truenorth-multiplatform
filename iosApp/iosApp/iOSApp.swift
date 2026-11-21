import SwiftUI
import FirebaseCore
import GoogleSignIn
import Shared

/// Bridges Google's iOS sign-in SDK to the Kotlin `GoogleSignInHandler`.
///
/// It lives in Swift because `GIDSignIn` has a Swift-only surface that
/// Kotlin/Native cannot reach through cinterop. Only the resulting ID token
/// crosses the boundary; everything Google-specific stays on this side.
///
/// Sign in with Apple needs no equivalent, because `AuthenticationServices`
/// is Objective-C and `AppleSignIn.ios.kt` calls it directly.
class GoogleSignInBridge: NSObject, GoogleSignInHandler {

    func signIn(
        onToken: @escaping (String) -> Void,
        onCancelled: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        guard let presenter = Self.topViewController() else {
            onError("Could not find a screen to present Google sign-in from")
            return
        }

        // The client ID comes from GoogleService-Info.plist by way of
        // FirebaseApp, rather than being repeated in Info.plist as GIDClientID.
        // One source, so it cannot drift after a config re-download.
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            onError("Google sign-in is not configured for this build")
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)

        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
            if let error = error as NSError? {
                if error.code == GIDSignInError.canceled.rawValue {
                    onCancelled()
                } else {
                    onError(error.localizedDescription)
                }
                return
            }
            guard let idToken = result?.user.idToken?.tokenString else {
                onError("Google returned no identity token")
                return
            }
            onToken(idToken)
        }
    }

    /// Compose Multiplatform puts everything inside one hosting controller, so
    /// the root is the right presenter. The loop only matters if a sheet is
    /// already up when the user taps.
    private static func topViewController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }
        var controller = scene?.keyWindow?.rootViewController
        while let presented = controller?.presentedViewController {
            controller = presented
        }
        return controller
    }
}

@main
struct iOSApp: App {
    init() {
        // GitLive's firebase-kotlin-sdk is a thin wrapper over the native
        // Firebase iOS SDK, which throws on every API call until the default
        // app is configured. On Android the google-services Gradle plugin
        // wires this up during the build; iOS has no equivalent, so it has to
        // happen here, before any Kotlin code reaches Firebase.auth.
        FirebaseApp.configure()

        // Registered before any Compose code runs, because GoogleSignIn's
        // isAvailable reads this and AuthScreen decides whether to draw the
        // button from it. Registering later would hide the button on first
        // launch and show it after a restart.
        IosGoogleSignIn.shared.handler = GoogleSignInBridge()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
