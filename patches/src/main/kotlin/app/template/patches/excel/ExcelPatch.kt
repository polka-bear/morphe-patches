package app.template.patches.excel

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.excel.Fingerprints.PremiumLicensingDisabledFingerprint
import app.template.patches.excel.Fingerprints.OHubUtilFingerprint
import app.template.patches.shared.Constants.COMPATIBILITY_EXCEL

@Suppress("unused")
val excelUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Microsoft 365 premium features and hides upsell prompts.",
    default = true
) {
    compatibleWith(COMPATIBILITY_EXCEL)

    execute {
        // 1. Force LicensingController.IsPremiumFeatureLicensingDisabled() to return true
        // This is the master override — when true, the app skips all premium feature licensing checks
        val licensingControllerClass = classDefBy(PremiumLicensingDisabledFingerprint.definingClass!!)
        val disabledMatch = PremiumLicensingDisabledFingerprint.match(licensingControllerClass)
        disabledMatch.method.apply {
            implementation?.let { impl ->
                removeInstructions(0, impl.instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // 2. Patch OHubUtil methods — the public API surface used throughout the app
        val ohubClass = classDefBy(OHubUtilFingerprint.definingClass!!)
        val ohubMutableClass = OHubUtilFingerprint.match(ohubClass).classDef

        for (method in ohubMutableClass.methods) {
            when (method.name) {
                // Force GetLicensingState() to return ConsumerPremium
                // This short-circuits the obfuscated licensing/e.c() -> NativeProxy.Gs() chain
                "GetLicensingState" -> {
                    if (method.returnType == "Lcom/microsoft/office/licensing/LicensingState;" && method.implementation != null) {
                        method.removeInstructions(0, method.instructions.count())
                        method.addInstructions(
                            0,
                            """
                            sget-object v0, Lcom/microsoft/office/licensing/LicensingState;->ConsumerPremium:Lcom/microsoft/office/licensing/LicensingState;
                            return-object v0
                            """.trimIndent()
                        )
                    }
                }
                // Force CanPerformPremiumEdit() to return true
                // This short-circuits the obfuscated licensing/f.a() -> NativeProxy.Cppe() chain
                "CanPerformPremiumEdit" -> {
                    if (method.returnType == "Z" && method.implementation != null) {
                        method.removeInstructions(0, method.instructions.count())
                        method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                    }
                }
                // Force isConsumerPremium() to return true
                "isConsumerPremium" -> {
                    if (method.returnType == "Z" && method.implementation != null) {
                        method.removeInstructions(0, method.instructions.count())
                        method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                    }
                }
                // Force isEnterprisePremium() to return true
                "isEnterprisePremium" -> {
                    if (method.returnType == "Z" && method.implementation != null) {
                        method.removeInstructions(0, method.instructions.count())
                        method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                    }
                }
                // Force isUpsellEligibleBasedOnLicensingState() to return false — hides all upsell UI
                "isUpsellEligibleBasedOnLicensingState" -> {
                    if (method.returnType == "Z" && method.implementation != null) {
                        method.removeInstructions(0, method.instructions.count())
                        method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                    }
                }
            }
        }
    }
}
