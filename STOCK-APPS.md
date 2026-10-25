# Stock apps

Binding keys in these apps asks for confirmation. `high` = checked on the device, `medium`/`low` = found by a static scan.

| Package | Risk | Notes |
|---|---|---|
| `com.android.contacts` | high | docomo phonebook claims Select, F2 ("Register"), F3, F4 and the F1 menu. |
| `com.android.deskclock` | high | Handles F1 (same as Menu) and F2 itself; Stopwatch claims F1/F2/Select, leaves F3/F4 free. |
| `com.android.dialer` | high | In-call UI handles F2-F4; Submenu dialog claims F1/F3/F4. |
| `com.android.email` | high | Handles F1/F2 in the photo viewer; also uses the softkey bar. |
| `com.android.mms` | high | Handles F2 in compose; claims F3 ("New msg.") and disables F2/F4. |
| `com.android.settings` | high | Claims F1 (blank on purpose); other slots unclaimed. |
| `com.android.systemui` | high | Notification shade claims F1 (Submenu), F2 (Delete) and Select (Enter); also draws the bar itself. |
| `com.kyocera.calculator2` | high | Claims F1 (AC), F2 (%), Select (=); handles F1/F2 itself. F3/F4 unclaimed. |
| `com.nttdocomo.android.schedulememo` | high | Claims all five slots (Submenu/Add/Previous/Next/Select). |
| `jp.kyocera.camera` | high | Handles F3/F4 itself (F3 is claimed blank on purpose, F4 = "Latest"); claims all slots. |
| `com.android.browser` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.android.documentsui` | medium | References the softkey API in its code and ships softkey XML resources; slot use not verified at runtime (static scan only). |
| `com.android.permissioncontroller` | medium | References the softkey API in its code and ships softkey XML resources; slot use not verified at runtime (static scan only). |
| `com.kyocera.apnsetting` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.nttdocomo.android.afphome` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.nttdocomo.android.anshinnumbercheckdfp` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.nttdocomo.android.areamail` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.nttdocomo.android.databackup` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.nttdocomo.android.idmanager` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.nttdocomo.android.messagedl` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.nttdocomo.android.msg` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.nttdocomo.android.remotelock` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.nttdocomo.android.sdcardbackup` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.co.nttdocomo.anshinmode` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.co.nttdocomo.carriermail` | medium | References the softkey API in its code and ships softkey XML resources; slot use not verified at runtime (static scan only). |
| `jp.co.nttdocomo.lcsapp` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.batterycare` | medium | References the softkey API in its code and ships softkey XML resources; slot use not verified at runtime (static scan only). |
| `jp.kyocera.browser.menu` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.charactercheck` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.customizekey` | medium | References the softkey API in its code and ships softkey XML resources; slot use not verified at runtime (static scan only). |
| `jp.kyocera.datafolder` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.datamigration` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.devicecontrol` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.docomo.initialsettings` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.functionrestriction` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.kc_fmradio` | medium | References the softkey API in its code and ships softkey XML resources; slot use not verified at runtime (static scan only). |
| `jp.kyocera.kc_soundrecorder` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.kcmushroom` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.kcsimapp` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.ky42c.manual` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.quicksettings` | medium | References the softkey API in its code and ships softkey XML resources; slot use not verified at runtime (static scan only). |
| `jp.kyocera.restrictdata` | medium | References the softkey API in its code and ships softkey XML resources; slot use not verified at runtime (static scan only). |
| `jp.kyocera.settings.nfp` | medium | References the softkey API in its code and ships softkey XML resources; slot use not verified at runtime (static scan only). |
| `jp.kyocera.start.setupwizard` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.sysupdater` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.vbookmark` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.wallpaperpicker` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `jp.kyocera.weather` | medium | References the softkey API in its code; slot use not verified at runtime (static scan only). |
| `com.android.bluetooth` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `com.android.phone` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `com.android.providers.contacts` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `com.android.providers.downloads` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `com.android.providers.downloads.ui` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `com.android.providers.media` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `com.android.server.telecom` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `com.android.stk` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `com.android.webview` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `jp.co.omronsoft.iwnnime.ml` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `jp.kyocera.corp.manager` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
| `jp.kyocera.kitting` | low | References the softkey API but is a provider/service/library with little or no UI of its own (static scan only). |
