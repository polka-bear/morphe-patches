package app.template.patches.excel

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.excel.Fingerprints.PremiumLicensingDisabledFingerprint
import app.template.patches.excel.Fingerprints.OHubUtilFingerprint
import app.template.patches.excel.Fingerprints.IntegrityCheckFingerprint
import app.template.patches.excel.Fingerprints.WatchdogTimerFingerprint
import app.template.patches.excel.Fingerprints.CanEditPermissionsFingerprint
import app.template.patches.shared.Constants.COMPATIBILITY_EXCEL
import app.morphe.patches.shared.SmaliTemplates.returnBoolean
import app.morphe.patches.shared.SmaliTemplates.returnStaticField

@Suppress("unused")
val excelUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Microsoft 365 premium features and hides upsell prompts.",
    default = true
) {
    compatibleWith(COMPATIBILITY_EXCEL)

    execute {
        // 0. Skip Integrity Check
        // Force com.microsoft.office.android.transparencyverification.a.n() to return true
        // This prevents the "trusted source" error message.
        val integrityClass = classDefBy(IntegrityCheckFingerprint.definingClass!!)
        IntegrityCheckFingerprint.match(integrityClass).method
            .addInstructions(0, returnBoolean(true))

        // 0b. Disable Native Watchdog
        // Force com.microsoft.office.mso.async.UnderlyingTimer$a.run() to do nothing.
        // This prevents the native crash in nativeTimerExpiryHandlerInternal.
        val watchdogClass = classDefBy(WatchdogTimerFingerprint.definingClass!!)
        WatchdogTimerFingerprint.match(watchdogClass).method
            .addInstructions(0, "return-void")

        // 0c. Force Edit Permissions
        // Force com.microsoft.office.mso.docs.model.sharingfm.SharedDocumentUI.getCanEditPermissions() to return true.
        // This should remove the "Activate Microsoft 365 to create or edit" block.
        val editPermsClass = classDefBy(CanEditPermissionsFingerprint.definingClass!!)
        CanEditPermissionsFingerprint.match(editPermsClass).method
            .addInstructions(0, returnBoolean(true))

        // All patches inject at index 0 — original code is preserved but unreachable.
        // This avoids corrupting exception handler tables (GetLicensingState has try-finally).

        // All patches inject at index 0 — original code is preserved but unreachable.
        // This avoids corrupting exception handler tables (GetLicensingState has try-finally).

        // 1. Force LicensingController.IsPremiumFeatureLicensingDisabled() to return true
        // Master override — when true, the app skips all premium feature licensing checks.
        val licensingControllerClass = classDefBy(PremiumLicensingDisabledFingerprint.definingClass!!)
        PremiumLicensingDisabledFingerprint.match(licensingControllerClass).method
            .addInstructions(0, returnBoolean(true))

        // 2. Patch OHubUtil methods — the public API surface used throughout the app
        val ohubClass = classDefBy(OHubUtilFingerprint.definingClass!!)
        val ohubMutableClass = OHubUtilFingerprint.match(ohubClass).classDef

        for (method in ohubMutableClass.methods) {
            if (method.implementation == null) continue
            when (method.name) {
                // Force GetLicensingState() to return ConsumerPremium
                // This method has a try-finally block — inject-at-top keeps it valid.
                "GetLicensingState" -> {
                    if (method.returnType == "Lcom/microsoft/office/licensing/LicensingState;") {
                        method.addInstructions(
                            0,
                            returnStaticField("Lcom/microsoft/office/licensing/LicensingState;", "ConsumerPremium")
                        )
                    }
                }
                // Force CanPerformPremiumEdit() to return true
                "CanPerformPremiumEdit" -> {
                    if (method.returnType == "Z") {
                        method.addInstructions(0, returnBoolean(true))
                    }
                }
                // Force isConsumerPremium() to return true
                "isConsumerPremium" -> {
                    if (method.returnType == "Z") {
                        method.addInstructions(0, returnBoolean(true))
                    }
                }
                // Force isEnterprisePremium() to return true
                "isEnterprisePremium" -> {
                    if (method.returnType == "Z") {
                        method.addInstructions(0, returnBoolean(true))
                    }
                }
                // Force isUpsellEligibleBasedOnLicensingState() to return false — hides all upsell UI
                "isUpsellEligibleBasedOnLicensingState" -> {
                    if (method.returnType == "Z") {
                        method.addInstructions(0, returnBoolean(false))
                    }
                }
            }
        }
    }
}
