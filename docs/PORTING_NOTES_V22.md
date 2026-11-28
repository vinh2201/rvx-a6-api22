# rvx-a6 → ReVanced Patcher v22 (Giai đoạn 1)

Phạm vi: chỉ giữ patch **YouTube 17.34.36** + nhóm **`patches/all`** (+ `patches/shared` mà YouTube cần).
`patches/music`, `patches/reddit`, `generator/`, `resources/music` và Java extension của music/reddit đã được gỡ bỏ.

> Chưa biên dịch được trong môi trường làm việc (không có kotlinc/Gradle/mạng). Mọi kiểm tra là kiểm tra tĩnh:
> mọi `import app.revanced.patcher.*` / `com.android.tools.smali.*` đều trùng với import đã dùng trong
> `rv-NEW-patch22` hoặc `rvx-a5-NEW`; mọi import nội bộ đều trỏ tới khai báo có thật; ngoặc cân bằng.

## Build
- Gradle 9.3.1, plugin `app.revanced.patches` 1.0.0-dev.10, patcher 22.0.0, smali 3.0.8 (giống rvx-a5-NEW).
- `extensions/shared` → `extensions/rvxa6-shared` (file `.rve`: `extensions/rvxa6-shared.rve`)
- `extensions/spoof-wifi` → `extensions/rvxa6-spoof-wifi` (file `.rve`: `extensions/rvxa6-spoof-wifi.rve`)
- `desugarlib.rve` → `rvxa6-desugarlib.rve` (sinh từ `rvxa6-shared`, xem `patches/build.gradle.kts`).
- Lệnh build: `./gradlew clean :patches:buildAndroid` (xem `.github/workflows/release.yml`).

## Đổi tên patch
Tên hiển thị ở CLI/Manager có hậu tố ` (A6)`, ví dụ `Add missing resources (A6)`.
- 64 patch YouTube: thuộc tính mới `PatchList.patchName` (= `title + " (A6)"`).
  `PatchList.title` giữ nguyên vì dùng cho trang cài đặt/PatchStatus trong app.
- `GmsCore support (A6)`, `Change version code (A6)`, `Disable edge-to-edge display (A6)`, `Spoof Wi-Fi connection (A6)`.
- Patch không có tên (nội bộ/phụ thuộc) giữ nguyên không tên.

## Chuyển đổi API v21 → v22
| v21 | v22 |
|---|---|
| `execute {}` / `finalize {}` | `apply {}` / `afterDependents {}` |
| `InstructionExtensions.x`, `util.smali.ExternalLabel` | `patcher.extensions.x`, `patcher.extensions.ExternalLabel` |
| `proxy.mutableTypes.MutableMethod/MutableClass/MutableField` | `com.android.tools.smali.dexlib2.mutable.MutableMethod/MutableClassDef/MutableField` |
| `classes`, `proxy(c).mutableClass`, `classBy` | `classDefs`, `classDefs.getOrReplaceMutable(c)`, `classDefs[type]` |
| option `key = / title =` | `name =` (bỏ `key`) |
| `context(BytecodePatchContext)` | `context(context: BytecodePatchContext)` |
| `patch.options["key"]` | option hoist lên top-level (`appIconOption`, `changeSplashIconOption`) |
| `Fingerprint`/`Match` của patcher | `util/fingerprint/LegacyFingerprint.kt` (xem bên dưới) |

### `util/fingerprint/LegacyFingerprint.kt`
~620 fingerprint kiểu v21 được giữ nguyên chỗ gọi. File này cài lại ngữ nghĩa v21 trên `classDefs`:
accessFlags so khớp tuyệt đối, returnType/parameters so khớp tiền tố, `strings` khớp chuỗi con (thứ tự theo lệnh),
opcode có wildcard + fuzzy threshold, `endIndex` là chỉ số *bao gồm*, match đầu tiên theo thứ tự `classDefs` thắng.
Kết quả được cache theo `BytecodePatchContext` (chạy patch lần 2 trong cùng JVM không dùng lại match cũ).

## Sửa lỗi có sẵn trong a6
- `AddMissingResourcesPatch`: thiếu một dấu `)` ở `copyResources(...)` (lỗi cú pháp). Giai đoạn 2 sẽ cải thiện toàn bộ patch này.

## Cần chú ý khi test
1. `rvxa6-desugarlib.rve`: jar lấy `classes1000.dex` từ `extensions/rvxa6-shared/build/intermediates/desugar_lib_dex/release/`.
   Nếu thiếu file này lúc chạy trên Android 6–7 sẽ báo lỗi desugar → gửi mình log.
2. `Disable layout updates` (`LayoutUpdatesPatch.kt`) vẫn là patch không tên như bản gốc a6 (không chọn được). Giai đoạn 2 sẽ xử lý.
3. Nếu build báo lỗi biên dịch, gửi nguyên log: phần lớn sẽ là lỗi nhỏ ở chỗ gọi API v22 mà mình không có compiler để thử.

## Sửa sau lần build thử đầu tiên
- `LithoFilterPatch.kt`: thiếu `import app.revanced.util.fingerprint.methodOrNull`.
- `ShortcutPatch.kt`: option v22 không còn `key`; `key` cũ chính là id shortcut nên giờ ghi rõ theo cặp `"subscriptions" to subscriptions`, ...

## Sửa sau lần chạy CLI đầu tiên (fix2)
**1. Va chạm tên lớp khi nạp chung nhiều bundle** (`IncompatibleClassChangeError: ... cannot inherit from sealed class ...Hook`)
CLI nạp nhiều `-bp` qua cùng một class loader. a6 và bundle chính thức có 23 lớp trùng tên đầy đủ
(`app.revanced.util.BytecodeUtilsKt`, `...video.playerresponse.Hook`, ...), nên lớp của bundle nạp trước thắng.
- Toàn bộ package Kotlin của a6 chuyển sang vùng tên riêng:
  `app.revanced.patches` → `app.revanced.rvxa6.patches`, `app.revanced.util` → `app.revanced.rvxa6.util`.
- Tài nguyên đóng gói cũng nằm dưới gốc riêng: `resources/youtube/...` → `resources/rvxa6/youtube/...`
  (đọc qua `BUNDLED_RESOURCE_ROOT` trong `util/ResourceUtils.kt`). Các chuỗi `"youtube/..."` trong patch giữ nguyên.
- Extension Java (`app.revanced.extension.*`) không đổi: lớp của a6 nằm trong `rvxa6-*.rve`; xung đột chỉ xảy ra nếu bật
  patch chính thức dùng cùng tên lớp extension (các patch trong file bat không dùng `shared.rve`).

**2. `Extension "extensions/rvxa6-desugarlib.rve" not found`**
`jar` đang trỏ cứng tới `desugar_lib_dex/release/classes1000.dex` (đường dẫn của AGP cũ). Giờ `patches/build.gradle.kts`:
- tìm `**/desugar_lib_dex/**/*.dex` hoặc `**/classes1000.dex` trong `extensions/rvxa6-shared/build`;
- kích hoạt tác vụ `l8DexDesugarLib*` của module extension nếu có;
- nếu không tìm thấy thì **dừng build ngay** và in danh sách tệp `.dex` đang có, thay vì để lỗi lộ ra lúc chạy CLI;
- ghi đè thủ công: đặt `rvxa6-desugarlib.rve` (lấy từ `desugarlib.rve` của một bản a6 gốc) vào
  `patches/src/main/resources/extensions/` thì sẽ dùng tệp đó.
Các lỗi `lateinit property rvxSettingFile` / `has failed previously` trong log cũ đều chỉ là hệ quả của lỗi này.

## Sửa sau lần chạy CLI thứ hai (fix3)
Log chỉ còn 3 patch lỗi logic (`Spoof streaming data` lỗi vì `AudioTracksHook`, `Miniplayer`, `Remove background playback restrictions`).
- **Khớp chuỗi của fingerprint**: shim khớp theo *chuỗi con* nên `"raw"` và `"appName"` khớp nhầm phương thức khác
  (`Lcqv;->p()` thay vì `Lxvf;->O(...)`; `Lpdq;->toString` thay vì `Lwrv;->a`). Đã kiểm chứng trên smali 17.34.36
  bằng bộ mô phỏng fingerprint: khớp CHÍNH XÁC trước, chỉ khi không có kết quả chính xác mới khớp chuỗi con
  (chỉ `ConversionContext{container` và `playerControlsVisibilityModelFingerprint` cần nhánh dự phòng này).
- **`by navigate(...)`** (BackgroundPlaybackPatch): v22 dùng `navigate(method).to(index).stop()`.
