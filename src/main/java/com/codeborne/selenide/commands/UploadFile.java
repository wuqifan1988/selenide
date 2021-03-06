package com.codeborne.selenide.commands;

import com.codeborne.selenide.Command;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.Stopwatch;
import com.codeborne.selenide.impl.Describe;
import com.codeborne.selenide.impl.WebElementSource;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.sleep;

public class UploadFile implements Command<File> {
  @Override
  public File execute(SelenideElement proxy, WebElementSource locator, Object[] args) throws IOException {
    File[] file;
    if (args instanceof File[]) {
      file = (File[]) args;
    }
    else {
      file = (File[]) args[0];
    }
    if (file.length == 0) {
      throw new IllegalArgumentException("No files to upload");
    }

    WebElement inputField = locator.getWebElement();
    File uploadedFile = uploadFile(inputField, file[0]);

    if (file.length > 1) {
      SelenideElement form = proxy.closest("form");
      List<WebElement> newInputs = cloneInputField(form, inputField, file.length - 1);

      Stopwatch stopwatch = new Stopwatch(Configuration.timeout);

      for (int i = 1; i < file.length; i++) {
        WebElement newInput = newInputs.get(i - 1);
        uploadSingleFile(file[i], stopwatch, newInput);
      }
    }

    return uploadedFile;

  }

  private void uploadSingleFile(File file, Stopwatch stopwatch, WebElement newInput) throws IOException {
    do {
      try {
        newInput.sendKeys(file.getCanonicalPath());
        return;
      }
      catch (ElementNotInteractableException notInteractable) {
        if (stopwatch.isTimeoutReached()) {
          throw notInteractable;
        }
        sleep(Configuration.pollingInterval);
      }
    } while (!stopwatch.isTimeoutReached());
  }

  protected File uploadFile(WebElement inputField, File file) throws IOException {
    if (!"input".equalsIgnoreCase(inputField.getTagName())) {
      throw new IllegalArgumentException("Cannot upload file because " + Describe.describe(inputField) + " is not an INPUT");
    }

    if (!file.exists()) {
      throw new IllegalArgumentException("File not found: " + file.getAbsolutePath());
    }

    String canonicalPath = file.getCanonicalPath();
    inputField.sendKeys(canonicalPath);
    return new File(canonicalPath);
  }

  protected List<WebElement> cloneInputField(SelenideElement form, WebElement inputField, int count) {
    return executeJavaScript(String.format("" +
        "var newInputs = [];" +
        "for (var i = 1; i <= arguments[2]; i++) {" +
        "  var id = '___selenide___id___' + arguments[1].getAttribute('name') + '___' + i + '___%s';" +
        "  var fileInput = document.createElement('input');" +
        "  fileInput.setAttribute('type', arguments[1].getAttribute('type'));" +
        "  fileInput.setAttribute('name', arguments[1].getAttribute('name'));" +
        "  fileInput.setAttribute('id', id);" +
        "  fileInput.style.width = '1px';" +
        "  fileInput.style.height = '1px';" +
        "  arguments[0].appendChild(fileInput);" +
        "  newInputs.push(fileInput);" +
        "}" +
        "return newInputs;", System.nanoTime()),
      form, inputField, count);
  }
}
