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

    // com.microsoft.office.android.transparencyverification.a -> n(Context) : boolean
    // Core integrity check that triggers the "trusted source" error.
    val IntegrityCheckFingerprint = Fingerprint(
        definingClass = "Lcom/microsoft/office/android/transparencyverification/a;",
        name = "n"
    )

    // com.microsoft.office.mso.async.UnderlyingTimer$a -> run() : void
    // Native watchdog heartbeat timer. Forcing this to do nothing prevents the native crash.
    val WatchdogTimerFingerprint = Fingerprint(
        definingClass = "Lcom/microsoft/office/mso/async/UnderlyingTimer\$a;",
        name = "run"
    )

    // com.microsoft.office.mso.docs.model.sharingfm.SharedDocumentUI -> getCanEditPermissions() : boolean
    // Determines if the current document session has edit permissions.
    val CanEditPermissionsFingerprint = Fingerprint(
        definingClass = "Lcom/microsoft/office/mso/docs/model/sharingfm/SharedDocumentUI;",
        name = "getCanEditPermissions"
    )

    // com.microsoft.office.officehub.util.OHubUtil — public API surface for licensing state queries.
    // Non-obfuscated (@KeepClassAndMembers) — stable across versions.
    val OHubUtilFingerprint = Fingerprint(
        definingClass = "Lcom/microsoft/office/officehub/util/OHubUtil;"
    )
}
