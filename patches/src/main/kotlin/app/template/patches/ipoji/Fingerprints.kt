package app.template.patches.ipoji

import app.morphe.patcher.Fingerprint

object Fingerprints {
    // com.pairip.SignatureCheck -> verifyIntegrity(Context) : void
    // Google Play Integrity Protection (PairIP) signature verification.
    // Computes SHA-256 of APK signing cert and compares against hardcoded hashes.
    // After Morphe re-signs the APK, this check fails and throws SignatureTamperedException.
    // Must be bypassed or the app crashes on launch.
    val SignatureCheckFingerprint = Fingerprint(
        definingClass = "Lcom/pairip/SignatureCheck;",
        name = "verifyIntegrity"
    )

    // com.pairip.StartupLauncher -> launch() : void
    // Runs encrypted VM bytecode at app startup via libpairipcore.so.
    // The VM code may perform additional integrity/tamper checks.
    val StartupLauncherFingerprint = Fingerprint(
        definingClass = "Lcom/pairip/StartupLauncher;",
        name = "launch"
    )

    // com.revenuecat.purchases.EntitlementInfo -> isActive() : boolean
    // The single gatekeeper for all entitlement/subscription status.
    // Returns true if the entitlement is currently active (not expired, not cancelled).
    // Non-obfuscated (Parcelable, public API) — stable across app versions.
    val EntitlementIsActiveFingerprint = Fingerprint(
        definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
        name = "isActive"
    )
}
