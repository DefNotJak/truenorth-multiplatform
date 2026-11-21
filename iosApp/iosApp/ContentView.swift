import UIKit
import SwiftUI
import GoogleSignIn
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            // Google's flow leaves the app for Safari and returns through the
            // URL scheme in Info.plist. Without this the user signs in, comes
            // back, and nothing happens, because the SDK never sees the
            // callback.
            .onOpenURL { url in
                GIDSignIn.sharedInstance.handle(url)
            }
    }
}
