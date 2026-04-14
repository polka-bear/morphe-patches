package app.template.patches.ipoji

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.ipoji.Fingerprints.SignatureCheckFingerprint
import app.template.patches.ipoji.Fingerprints.StartupLauncherFingerprint
import app.template.patches.ipoji.Fingerprints.EntitlementIsActiveFingerprint
import app.template.patches.shared.Constants.COMPATIBILITY_IPOJI

@Suppress("unused")
val ipojiBypassIntegrityPatch = bytecodePatch(
    name = "Bypass Integrity Protection",
    description = "Disables PairIP signature verification and startup VM checks that crash re-signed APKs.",
    default = true
) {
    compatibleWith(COMPATIBILITY_IPOJI)

    execute {
        // 1. Bypass SignatureCheck.verifyIntegrity(Context) — make it return immediately.
        // After Morphe re-signs the APK, the SHA-256 of the signing cert no longer matches
        // the hardcoded hashes. Without this bypass, the app throws SignatureTamperedException.
        val sigCheckClass = classDefBy(SignatureCheckFingerprint.definingClass!!)
        SignatureCheckFingerprint.match(sigCheckClass).method
            .addInstructions(0, "return-void")

        // 2. Bypass StartupLauncher.launch() — skip the encrypted VM startup code.
        // The VM (libpairipcore.so) runs encrypted bytecode from assets/ that may perform
        // additional tamper detection. Skipping it prevents any secondary integrity failures.
        val launcherClass = classDefBy(StartupLauncherFingerprint.definingClass!!)
        StartupLauncherFingerprint.match(launcherClass).method
            .addInstructions(0, "return-void")
    }
}

@Suppress("unused")
val ipojiUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features by forcing all RevenueCat entitlements to active.",
    default = true
) {
    compatibleWith(COMPATIBILITY_IPOJI)

    execute {
        // Force EntitlementInfo.isActive() to return true.
        // Inject at index 0 — original code preserved but unreachable.
        //
        // This single patch covers the entire subscription pipeline:
        // 1. EntitlementInfos constructor filters on isActive() → all entitlements now in "active" map
        // 2. EntitlementInfoMapperKt.map() reads isActive() → Flutter receives isActive=true
        // 3. Flutter code checking any entitlement sees active status
        // 4. Ads gated by entitlement check in Flutter are also suppressed
        val entitlementClass = classDefBy(EntitlementIsActiveFingerprint.definingClass!!)
        EntitlementIsActiveFingerprint.match(entitlementClass).method
            .addInstructions(0, "const/4 v0, 0x1\nreturn v0")
    }
}
