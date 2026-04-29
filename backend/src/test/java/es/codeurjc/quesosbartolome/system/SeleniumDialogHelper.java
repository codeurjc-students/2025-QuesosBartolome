package es.codeurjc.quesosbartolome.system;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class SeleniumDialogHelper {

	private static final String DIALOG_CARD_SELECTOR = ".dialog-overlay .dialog-card";
	private static final String DIALOG_MESSAGE_SELECTOR = DIALOG_CARD_SELECTOR + " .dialog-message";
	private static final String PRIMARY_BUTTON_SELECTOR = DIALOG_CARD_SELECTOR + " .dialog-button-primary";
	private static final String SECONDARY_BUTTON_SELECTOR = DIALOG_CARD_SELECTOR + " .dialog-button-secondary";

	private SeleniumDialogHelper() {
	}

	public static Alert waitForDialog(WebDriverWait wait) {
		WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(DIALOG_MESSAGE_SELECTOR)));
		return new DomAlert(wait, message.getText());
	}

	private static final class DomAlert implements Alert {
		private final WebDriverWait wait;
		private final String text;

		private DomAlert(WebDriverWait wait, String text) {
			this.wait = wait;
			this.text = text;
		}

		@Override
		public void dismiss() {
			clickIfPresent(SECONDARY_BUTTON_SELECTOR);
		}

		@Override
		public void accept() {
			clickIfPresent(PRIMARY_BUTTON_SELECTOR);
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public void sendKeys(String keysToSend) {
			throw new UnsupportedOperationException("sendKeys is not supported for app dialogs");
		}

		private void clickIfPresent(String cssSelector) {
			try {
				wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector))).click();
				wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(DIALOG_CARD_SELECTOR)));
			} catch (TimeoutException ignored) {
				// Alerts auto-close after a few seconds in the app, so missing button/dialog is acceptable.
			}
		}
	}
}
