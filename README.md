# HotSwan + Metro assisted ViewModel signature mismatch repro

Minimal Android project for checking a HotSwan `2.0.0-beta03` issue with Metro assisted ViewModels.

## Environment

- Kotlin: `2.4.0`
- AGP: `9.0.1`
- Compose BOM: `2026.06.00`
- HotSwan compiler plugin: `2.0.0-beta03`
- Metro / metrox-viewmodel / metrox-viewmodel-compose: `1.2.1`
- Android Studio Live Edit: disabled

## Project shape

- `:app` owns the Metro `AppGraph` and `ActivityRetainedGraph`.
- `:feature` owns `SampleScreen` and an `@AssistedInject` `SampleViewModel`.
- `SampleViewModel` is created from Compose with `assistedMetroViewModel(...)`.
- The ViewModel has one assisted argument and two non-assisted injected dependencies:
  - `FirstRepository`
  - `SecondRepository`

This mirrors the original failure shape where a feature ViewModel constructor signature changes and Metro regenerates the ViewModel factory signature.

## Reproduction steps

1. Open this project in Android Studio.
2. Make sure Android Studio Live Edit is disabled.
3. Build and install the app once with the current source.
4. Run the app and confirm the screen renders.
5. In `feature/src/main/kotlin/com/easyhooon/hotswanmetroconflict/ui/SampleViewModel.kt`, remove the unused non-assisted injected dependency:

```kotlin
import com.easyhooon.hotswanmetroconflict.data.SecondRepository
```

```kotlin
private val secondRepository: SecondRepository,
```

6. Change the message back to only use `FirstRepository`:

```kotlin
val message: String = "$screenName ${firstRepository.label()}"
```

7. Apply the change through Android Studio / HotSwan deploy.

## Expected failure

HotSwan should force a rebuild/reinstall or otherwise invalidate stale Metro-generated callsites when the assisted ViewModel factory signature changes.

## Observed failure

In the original project, the app process stayed alive but Compose rendered a blank white screen because composition captured a `NoSuchMethodError`.

The mismatch looked like this:

- `SampleViewModel$MetroFactory$Companion` exposes the new signature:

```text
create(Provider<FirstRepository>)
```

- `AppGraph$Impl$ActivityRetainedGraphImpl` still invokes the old signature:

```text
create(Provider<FirstRepository>, Provider<SecondRepository>)
```

That produces:

```text
Error was captured in composition while live edit was enabled.
java.lang.NoSuchMethodError: No virtual method create(Ldev/zacsweers/metro/Provider;Ldev/zacsweers/metro/Provider;)...
```

## Bytecode inspection

After attempting the HotSwan deploy, the two sides can be inspected with:

```bash
javap -classpath feature/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes \
  -s -p 'com.easyhooon.hotswanmetroconflict.ui.SampleViewModel$MetroFactory$Companion' \
  | rg -n "create|Provider"
```

```bash
javap -classpath app/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes:feature/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes \
  -c -p 'com.easyhooon.hotswanmetroconflict.di.AppGraph$Impl$ActivityRetainedGraphImpl' \
  | rg -n "SampleViewModel|MetroFactory\\$Companion.create" -C 4
```

If the issue reproduces, the first command shows the new `create(...)` descriptor while the second command shows `AppGraph` still calling the old descriptor.
