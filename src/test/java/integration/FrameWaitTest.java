package integration;

import com.automation.remarks.video.annotations.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.name;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static com.codeborne.selenide.WebDriverRunner.isHtmlUnit;
import static com.codeborne.selenide.WebDriverRunner.source;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class FrameWaitTest extends IntegrationTest {
  @BeforeEach
  void setUp() {
    openFile("page_with_frames_with_delays.html");
  }

  @Test
  @Video
  void waitsUntilFrameAppears_inner() {
    switchTo().innerFrame("parentFrame");
    $("frame").shouldHave(name("childFrame_1"));
  }

  @Test
  @Video
  void waitsUntilFrameAppears_byTitle() {
    switchTo().frame("leftFrame");
    $("h1").shouldHave(text("Page with dynamic select"));
  }

  @Test
  @Video
  void waitsUntilFrameAppears_byIndex() {
    assumeFalse(isHtmlUnit());

    switchTo().frame(2);

    $("h1").shouldHave(text("Page with JQuery"));
    assertThat(source()).contains("Test::jquery");
  }
}
