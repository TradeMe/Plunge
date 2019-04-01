# Plunge

### _Simple, easily testable deep link handling – the way it should be._


## Installation

Add the dependencies to your module-level `build.gradle` file:

```groovy
buildscript {
  dependencies {
    classpath "nz.co.trademe.plunge:plunge-gradle-plugin:current_version"
  }
}

apply plugin: 'nz.co.trademe.plunge'

dependencies {
  implementation 'nz.co.trademe.plunge:plunge:current_version'

  testImplementation 'nz.co.trademe.plunge:plunge-test:current_version'
  testImplementation 'nz.co.trademe.plunge:plunge-parsing:current_version'
}
```

The `current_version` is: [ ![Download](https://api.bintray.com/packages/trademe/Plunge/plunge/images/download.svg) ](https://bintray.com/trademe/Plunge/plunge/_latestVersion)


## Better deep linking in 7 easy steps!

1. Add link-handling support to the `AndroidManifest.xml` file, as per usual:

```xml
<intent-filter>
  ...
  <data android:scheme="https"
        android:host="plunge.example.com"
        android:pathPattern="/submarines/.*/buy"
  ...
</intent-filter>
```

2. Implement a `UrlSchemeHandler`:

```kotlin
class PlungeExampleSchemeHandler(private val router: DeepLinkRouter): UrlSchemeHandler() {
    override fun hostMatches(host: String): Boolean = host.contains("plunge.example.com")

    override val matchers by patterns {
        pattern("/submarines/{d|id}/buy") { result -> router.launchBuyPage(result.params["id"]) }
    }
}

interface DeepLinkRouter {
  fun launchBuyPage(id: String) // Note: parameters are always Strings
}
```

3. Forward your links from your intent filter `Activity`:

```kotlin
class PlungeActivity : AppCompatActivity(), DeepLinkRouter {
  val linkHandler = DeepLinkHandler.withSchemeHandlers(
    PlungeExampleSchemeHandler(this)
  )

  fun onDeepLinkCaught() {
    val link = Uri.parse(getIntent().getData())

    val handled = linkHandler.processUri(link)
    if (!handled) {
        // ... Some kind of default fallback
    }
  }

  fun launchBuyPage(id: String) {
    // ...
  }
}
```

4. Add the path to your test cases to your module-level `build.gradle` file:

```groovy
plunge {
    testDirectory = file("$projectDir/src/test/test-cases")
}
```

5. Write some test cases to ensure the links you want to handle will be handled:

```json
{
  "url": "https://plunge.example.com/submarines/12345/buy",
  "description": "The page for buying a submarine",
  "params": [
    {
      "name": "id",
      "value": "12345"
    }
  ]
}
```

6. Write some more test cases to ensure the links you _don't_ want to handle won't launch your app:

```json
{
  "url": "https://plunge.example.com/submarines/sell",
  "description": "The page for selling a submarine. We don't support this in the app yet, so users will have to use the desktop site.",
  "handled": false
}
```

7. Finally, write a JUnit test case for executing Plunge tests:

```kotlin
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PlungeExampleTests {
  companion object {

    @JvmStatic
    private val pathToTests = System.getProperty("user.dir") + "/src/test/test-cases"

    @JvmStatic
    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    fun parameters() = PlungeTestRunner.testCases(pathToTests)

  }

  val linkHandler = DeepLinkHandler.withSchemeHandlers(
    PlungeExampleSchemeHandler(Mockito.mock(DeepLinkRouter::class.java))
  )

  @ParameterizedRobolectricTestRunner.Parameter(0)
  lateinit var testCase: PlungeTestCase

  @Test
  fun runTest() = PlungeTestRunner.assertPlungeTest(testCase, linkHandler)
}
```

## Running tests

Plunge unit tests can be easily run in the same way you'd run any other unit tests; however, a Gradle plugin is included to execute the verification tests. To run these tests, look for the appropriate `plungeTest` Gradle task for your build variant in the `verification` group. This task will build and install the app on the device or emulator currently connected to your machine, and execute your tests against it. The task will fail if any links are not correctly handled by the app (or links **are** handled by the app when they shouldn't be).

Integrate the task into your CI pipeline for an even better time!

## More info

See [the wiki](https://github.com/TradeMe/Plunge/wiki) for more in-depth information about how to do better deep linking with Plunge.

## License

Plunge is made available under the MIT licence.

## Contributing

We love contributions, but make sure to check out [CONTRIBUTING.MD](CONTRIBUTING.MD) first!
