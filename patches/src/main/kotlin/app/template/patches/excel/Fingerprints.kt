package app.template.patches.excel

import app.morphe.patcher.Fingerprint

object Fingerprints {
    // com.microsoft.office.docsui.common.LicensingController -> IsPremiumFeatureLicensingDisabled() : boolean
    // Master override for premium feature licensing. When true, ALL premium feature checks are bypassed.
    // Non-obfuscated (@KeepClassAndMembers) — stable across versions.
    val PremiumLicensingDisabledFingerprint = Fingerprint(
        definingClass = "Lcom/microsoft/office/docsui/common/LicensingController;",
        name = "IsPremiumFeatureLicensingDisabled"
    )

    // com.microsoft.office.officehub.util.OHubUtil — public API surface for licensing state queries.
    // Non-obfuscated (@KeepClassAndMembers) — stable across versions.
    val OHubUtilFingerprint = Fingerprint(
        definingClass = "Lcom/microsoft/office/officehub/util/OHubUtil;"
    )
}
