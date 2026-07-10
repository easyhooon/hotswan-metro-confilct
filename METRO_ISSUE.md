# Generated graph callsite can become stale after `@AssistedInject` ViewModel constructor signature changes

## Description

Changing a non-assisted dependency in an `@AssistedInject` ViewModel constructor can leave the generated Metro graph calling a stale assisted factory signature.

The mismatch appears to be between Metro-generated classes:

- the ViewModel `MetroFactory$Companion` exposes one `create(...)` descriptor
- the generated `AppGraph$Impl$ActivityRetainedGraphImpl` invokes another descriptor

In the real project this happened after adding/removing an unused non-assisted repository dependency from an assisted ViewModel constructor. I also reproduced the same descriptor mismatch in this minimal project with Metro `1.3.0`.

## Self-contained Reproducer

Minimal repro project:

https://github.com/easyhooon/hotswan-metro-confilct

The project shape is:

- `:app` contains `AppGraph` and `ActivityRetainedGraph`
- `:feature` contains `SampleScreen` and `@AssistedInject SampleViewModel`
- `SampleViewModel` is contributed with `@ContributesIntoMap(ActivityRetainedScope::class)`
- `SampleViewModel` is created from Compose with `assistedMetroViewModel(...)`

Baseline constructor:

```kotlin
@AssistedInject
class SampleViewModel(
    @param:Assisted private val screenName: String,
    firstRepository: FirstRepository,
    secondRepository: SecondRepository,
) : ViewModel() {
    val message: String = "$screenName ${firstRepository.label()} ${secondRepository.label()}"
}
```

Reproduction steps:

1. Clone the repro project.

```bash
git clone https://github.com/easyhooon/hotswan-metro-confilct
cd hotswan-metro-confilct
```

2. Compile the baseline source once.

```bash
./gradlew :app:compileDebugKotlin
```

3. In `feature/src/main/kotlin/com/easyhooon/hotswanmetroconflict/ui/SampleViewModel.kt`, remove the non-assisted `SecondRepository` dependency:

```kotlin
// Remove this constructor parameter:
secondRepository: SecondRepository,
```

4. Update the message so it no longer references `secondRepository`:

```kotlin
val message: String = "$screenName ${firstRepository.label()}"
```

5. Compile again.

```bash
./gradlew :app:compileDebugKotlin
```

6. Inspect the generated factory and graph callsite descriptors.

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

In the affected project, the mismatch looked like this:

```text
TripInviteNameSelectViewModel$MetroFactory$Companion exposes:
create(Provider<InvitationRepository>)
```

but:

```text
AppGraph$Impl$ActivityRetainedGraphImpl still invokes:
create(Provider<InvitationRepository>, Provider<TripRepository>)
```

Runtime failure:

```text
java.lang.NoSuchMethodError:
No virtual method create(Ldev/zacsweers/metro/Provider;Ldev/zacsweers/metro/Provider;)...
```

Stack trace points into the generated graph:

```text
at ...AppGraph$Impl$ActivityRetainedGraphImpl.getMapOfKClass_out_ManualViewModelAssistedFactory_ToManualViewModelAssistedFactoryProvider(...)
at ...AppGraph$Impl$ActivityRetainedGraphImpl.<init>(...)
```

Expected behavior:

When an `@AssistedInject` constructor signature changes and Metro regenerates the assisted factory signature, the generated graph callsite should also be invalidated/regenerated. The build should not produce bytecode where the factory exposes the new descriptor but `AppGraph$Impl...` invokes the old descriptor.

## Metro environment

Generated with:

```bash
./gradlew :app:metroEnv
```

Metro environment:

```text
Metro environment report

Project
  path: :app
  target:
  compilation: debug
  platform: androidJvm
  compile task: compileDebugKotlin

Versions
  Metro Gradle plugin: 1.3.0
  Metro compiler artifact: dev.zacsweers.metro:compiler:1.3.0
  Kotlin Gradle plugin: 2.4.0
  Kotlin compiler: 2.4.0
  Gradle: 9.1.0
  Java: 21
  OS: Mac OS X 26.3.1 (aarch64)

Kotlin compiler options
  languageVersion: <default>
  apiVersion: <default>
  freeCompilerArgs:
    -Xcompiler-plugin-order=dev.zacsweers.metro.compiler>androidx.compose.compiler.plugins.kotlin
    -Xuse-inline-scopes-numbers
    -Xallow-unstable-dependencies
    -Xcompiler-plugin-order=com.github.skydoves.compose.hotswan.compiler.pre>androidx.compose.compiler.plugins.kotlin
    -Xcompiler-plugin-order=androidx.compose.compiler.plugins.kotlin>com.github.skydoves.compose.hotswan.compiler

Metro compiler plugin options
  options:
    enabled = true
    max-ir-errors-count = 20
    debug = false
    generate-assisted-factories = false
    generate-contribution-hints = true
    generate-contribution-hints-in-fir = true
    generate-classes-in-ir = false
    statements-per-init-fun = 25
    enable-graph-sharding = true
    keys-per-graph-shard = 2000
    enable-switching-providers = false
    optional-binding-behavior = DEFAULT
    diagnostics-render-mode = PLAIN
    public-scoped-provider-severity = NONE
    non-public-contribution-severity = NONE
    warn-on-inject-annotation-placement = true
    interop-annotations-named-arg-severity = NONE
    unused-graph-inputs-severity = WARN
    enable-top-level-function-injection = true
    contributes-as-inject = true
    enable-klib-params-check = false
    patch-klib-params = true
    force-enable-fir-in-ide = false
    compiler-version =
    compiler-version-aliases =
    enable-function-providers = true
    desugared-provider-severity = WARN
    generate-contribution-providers = false
    enable-circuit-codegen = false
    enable-runtime-tracing = false
    plugin-order-set = true
    enable-dagger-runtime-interop = false
    enable-kclass-to-class-interop = false
    interop-include-javax-annotations = false
    interop-include-jakarta-annotations = false
    interop-include-dagger-annotations = false
    interop-include-kotlin-inject-annotations = false
    interop-include-anvil-annotations = false
    interop-include-kotlin-inject-anvil-annotations = false
    enable-dagger-anvil-interop = false
    interop-include-guice-annotations = false
    enable-guice-runtime-interop = false
    interop-include-hilt-annotations = false
```

## Previous working version

N/A. I reproduced this with Metro `1.3.0`, which is the latest release I found while testing. I originally observed the same kind of mismatch on Metro `1.2.1`.

## Possibly related issues

I searched existing Metro issues/PRs and did not find an exact duplicate for this assisted ViewModel generated graph callsite mismatch.

Possibly related, but not the same issue:

- https://github.com/ZacSweers/metro/issues/586
  - Also reports a `NoSuchMethodError` involving assisted params and a generated `MetroFactory`.
  - It was on Metro `0.3.7`, involved `@Inject constructor` with assisted params, and was discussed as likely incremental compilation related.
- https://github.com/ZacSweers/metro/pull/703
  - Fixed an IC factory class lookup issue when adding a parameter to an injected type in another module.
  - The failure mode there was `NoSuchFieldError` for a generated factory `INSTANCE`, not this stale `create(...)` descriptor mismatch.
- https://github.com/ZacSweers/metro/issues/1393
  - Discusses stale contribution hints / another-module IC behavior.
  - It was closed because it could not be reproduced with the provided case.
- https://github.com/ZacSweers/metro/issues/1708
  - Open tracking issue for FIR incremental compilation support.

I did not find a changelog entry that clearly says this exact assisted factory callsite mismatch has been fixed.

## IDE version

Android Studio. Exact version not recorded yet.

## Platform

Android.

## Context

This is a runtime failure caused by generated bytecode mismatch, not a compiler error. The issue surfaced during Compose composition because the stale graph callsite was executed while creating the assisted ViewModel factory map.

My current suspicion is generated graph invalidation / incremental compilation / build cache behavior around assisted factory signature changes.

I used AI assistance to organize this report into the issue template. The repro project, stack trace, and bytecode observations are from local testing.
