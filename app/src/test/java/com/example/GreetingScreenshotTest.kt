package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.IslandEvent
import com.example.data.model.IslandSettings
import com.example.data.model.IslandState
import com.example.ui.island.DynamicIsland
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun island_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        DynamicIsland(
          event = IslandEvent.Music(
            title = "Blinding Lights",
            artist = "The Weeknd",
            isPlaying = true
          ),
          state = IslandState.Compact,
          settings = IslandSettings(),
          onExpand = {},
          onCollapse = {},
          onDismiss = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
