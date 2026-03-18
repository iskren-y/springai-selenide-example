package com.example

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.ex.ElementNotFound
import com.codeborne.selenide.ex.ElementShould
import com.codeborne.selenide.ex.ElementShouldNot
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component
import static com.codeborne.selenide.Condition.hidden
import static com.codeborne.selenide.Condition.enabled
import static com.codeborne.selenide.Condition.visible
import static com.codeborne.selenide.Selectors.byText
import static com.codeborne.selenide.WebDriverRunner.getWebDriver
import static com.codeborne.selenide.WebDriverRunner.source

@Component  // Spring bean for tool registration
class BrowserTools {

    static {
        Configuration.browser = 'chrome'
        Configuration.headless = true
        Configuration.browserBinary = System.getenv("CHROME_BIN")
        Configuration.screenshots = true
        Configuration.savePageSource = false
        Configuration.timeout = 10000

        def chromeOptions = new ChromeOptions()
        chromeOptions.addArguments("--headless=new")
        chromeOptions.addArguments("--no-sandbox")
        chromeOptions.addArguments("--disable-dev-shm-usage")
        chromeOptions.addArguments("--disable-gpu")
        chromeOptions.addArguments("--disable-extensions")
        chromeOptions.addArguments("--disable-software-rasterizer")
        chromeOptions.addArguments("--remote-debugging-port=9222")
        Configuration.browserCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions)
    }

    @Tool(description = "Navigates the current browser to the specified URL. Use it you need to visit an URL.")
    String goToUrl(
            @ToolParam(description = "Full URL to visit (e.g., https://the-internet.herokuapp.com/dynamic_controls)") String url) {

        println("→ Navigating to: $url")
        selenideAction("navigating to ${url}") {
            Selenide.open(url)
            return "✅ Navigated to ${url}."
        }
    }

    @Tool(description = "Use it to locate page element by text and make sure that element is visible.")
    String elementIsVisible(@ToolParam(description = "Text value of the element") String textVal) {

        println("→ Checking visibility: \"$textVal\"")

        selenideAction("checking visibility of element with text='${textVal}'") {
            Selenide.$(byText(textVal)).shouldBe(visible)
            return "✅ Element with text '${textVal}' is visible"
        }
    }

    @Tool(description = "Clicks the first visible element matching the exact text (case-sensitive).")
    String clickElementByText(
            @ToolParam(description = "Visible text of the element to click (e.g., 'Remove')") String textVal) {

        println("→ Clicking: \"$textVal\"")

        selenideAction("clicking element with text='${textVal}'") {
            Selenide.$(byText(textVal)).shouldBe(visible).click()
            return "✅ Clicked element with text '${textVal}'"
        }
    }

    // ============ Navigation ============

    @Tool(description = "Get the current page HTML source. Use it when you need to determine element selector yourself.")
    String getPageSource() {
        println("→ Fetching page source")
        selenideAction("getting page source") {
            return source()
        }
    }

    @Tool(description = "Refresh the current page.")
    String refreshPage() {
        println("→ Refreshing page")
        selenideAction("refreshing page") {
            Selenide.refresh()
            return "✅ Page refreshed"
        }
    }

    @Tool(description = "Get the current page URL.")
    String getCurrentUrl() {
        println("→ Getting current URL")
        selenideAction("getting current URL") {
            return getWebDriver().getCurrentUrl()
        }
    }

    @Tool(description = "Get the current page title.")
    String getPageTitle() {
        println("→ Getting page title")
        selenideAction("getting page title") {
            return Selenide.title()
        }
    }

    // ============ Input Actions ============

    @Tool(description = "Type text into an input field identified by CSS selector.")
    String typeText(
            @ToolParam(description = "CSS selector for the input element") String cssSelector,
            @ToolParam(description = "Text to type") String text) {

        println("→ Typing into: $cssSelector")
        selenideAction("typing into cssSelector='${cssSelector}'") {
            Selenide.$(cssSelector).shouldBe(visible).setValue(text)
            return "✅ Typed '${text}' into '${cssSelector}'"
        }
    }

    @Tool(description = "Clear an input field.")
    String clearInput(@ToolParam(description = "CSS selector for the input element") String cssSelector) {

        println("→ Clearing: $cssSelector")
        selenideAction("clearing input cssSelector='${cssSelector}'") {
            Selenide.$(cssSelector).shouldBe(visible).clear()
            return "✅ Cleared input '${cssSelector}'"
        }
    }

    @Tool(description = "Press Enter key on an element.")
    String pressEnter(@ToolParam(description = "CSS selector for the element") String cssSelector) {

        println("→ Pressing Enter: $cssSelector")
        selenideAction("pressing Enter on cssSelector='${cssSelector}'") {
            Selenide.$(cssSelector).shouldBe(visible).pressEnter()
            return "✅ Pressed Enter on '${cssSelector}'"
        }
    }

    // ============ Element Interaction ============

    @Tool(description = "Click an element by CSS selector.")
    String clickElement(@ToolParam(description = "CSS selector for the element") String cssSelector) {

        println("→ Clicking: $cssSelector")
        selenideAction("clicking cssSelector='${cssSelector}'") {
            Selenide.$(cssSelector).shouldBe(visible).click()
            return "✅ Clicked element '${cssSelector}'"
        }
    }

    @Tool(description = "Double-click an element by CSS selector.")
    String doubleClickElement(@ToolParam(description = "CSS selector for the element") String cssSelector) {

        println("→ Double-clicking: $cssSelector")
        selenideAction("double-clicking cssSelector='${cssSelector}'") {
            Selenide.$(cssSelector).shouldBe(visible).doubleClick()
            return "✅ Double-clicked element '${cssSelector}'"
        }
    }

    @Tool(description = "Right-click (context-click) an element by CSS selector.")
    String rightClickElement(@ToolParam(description = "CSS selector for the element") String cssSelector) {

        println("→ Right-clicking: $cssSelector")
        selenideAction("right-clicking cssSelector='${cssSelector}'") {
            Selenide.$(cssSelector).shouldBe(visible).contextClick()
            return "✅ Right-clicked element '${cssSelector}'"
        }
    }

    @Tool(description = "Hover over an element by CSS selector.")
    String hoverOverElement(@ToolParam(description = "CSS selector for the element") String cssSelector) {

        println("→ Hovering: $cssSelector")
        selenideAction("hovering cssSelector='${cssSelector}'") {
            Selenide.$(cssSelector).shouldBe(visible).hover()
            return "✅ Hovered over element '${cssSelector}'"
        }
    }

    // ============ Element Assertions ============

    @Tool(description = "Check if an element exists in the DOM.")
    String elementExists(@ToolParam(description = "CSS selector for the element") String cssSelector) {

        println("→ Checking existence: $cssSelector")
        selenideAction("checking existence of cssSelector='${cssSelector}'") {
            def exists = Selenide.$(cssSelector).exists()
            return exists ? "✅ Element '${cssSelector}' exists" : "❌ Element '${cssSelector}' does not exist"
        }
    }

    @Tool(description = "Check if an element is hidden.")
    String elementIsHidden(@ToolParam(description = "CSS selector for the element") String cssSelector) {

        println("→ Checking hidden: $cssSelector")
        selenideAction("checking hidden cssSelector='${cssSelector}'") {
            Selenide.$(cssSelector).shouldBe(hidden)
            return "✅ Element '${cssSelector}' is hidden"
        }
    }

    @Tool(description = "Check if element contains specific text.")
    String elementHasText(
            @ToolParam(description = "CSS selector for the element") String cssSelector,
            @ToolParam(description = "Text to search for") String text) {

        println("→ Checking text in: $cssSelector")
        selenideAction("checking text in cssSelector='${cssSelector}'") {
            Selenide.$(cssSelector).shouldBe(visible).shouldHave(Condition.text(text))
            return "✅ Element '${cssSelector}' contains text '${text}'"
        }
    }

    @Tool(description = "Get visible text from an element.")
    String getElementText(@ToolParam(description = "CSS selector for the element") String cssSelector) {

        println("→ Getting text: $cssSelector")
        selenideAction("getting text from cssSelector='${cssSelector}'") {
            def text = Selenide.$(cssSelector).shouldBe(visible).text()
            return text
        }
    }

    @Tool(description = "Get value from an input field.")
    String getInputValue(@ToolParam(description = "CSS selector for the input element") String cssSelector) {

        println("→ Getting value: $cssSelector")
        selenideAction("getting value from cssSelector='${cssSelector}'") {
            def value = Selenide.$(cssSelector).shouldBe(visible).getValue()
            return value
        }
    }

    @Tool(description = "Check if an element is enabled.")
    String elementIsEnabled(@ToolParam(description = "CSS selector for the element") String cssSelector) {

        println("→ Checking enabled: $cssSelector")
        selenideAction("checking enabled cssSelector='${cssSelector}'") {
            Selenide.$(cssSelector).shouldBe(enabled)
            return "✅ Element '${cssSelector}' is enabled"
        }
    }

    // ============ Collections ============

    @Tool(description = "Count elements matching a CSS selector.")
    String getElementCount(@ToolParam(description = "CSS selector for elements") String cssSelector) {

        println("→ Counting: $cssSelector")
        selenideAction("counting elements cssSelector='${cssSelector}'") {
            def count = Selenide.$$(cssSelector).size()
            return count.toString()
        }
    }

    // ============ JavaScript ============

    @Tool(description = "Execute arbitrary JavaScript code.")
    String executeScript(@ToolParam(description = "JavaScript code to execute") String script) {

        println("→ Executing JS: ${script.take(40)}...")
        selenideAction("executing JavaScript") {
            def result = Selenide.executeJavaScript(script)
            return result?.toString() ?: "✅ Script executed"
        }
    }

    private String selenideAction(Closure<String> supplier) {
        selenideAction(null, supplier)
    }

    private String selenideAction(String context, Closure<String> supplier) {
        try {
            supplier
        } catch (ElementNotFound e) {
            def msg = context ? "Element not found. ${context}: ${e.message}" : "Element not found: ${e.message}"
            return "❌ ${msg}"
        } catch (ElementShould e) {
            def msg = context ? "Assertion failed. ${context}: ${e.message}" : "Assertion failed: ${e.message}"
            return "❌ ${msg}"
        } catch (ElementShouldNot e) {
            def msg = context ? "Assertion failed. ${context}: ${e.message}" : "Assertion failed: ${e.message}"
            return "❌ ${msg}"
        } catch (NoSuchElementException e) {
            def msg = context ? "No such element. ${context}: ${e.message}" : "No such element: ${e.message}"
            return "❌ ${msg}"
        } catch (StaleElementReferenceException e) {
            def msg = context ? "Stale element. ${context}: element no longer in DOM" : "Stale element: element no longer in DOM"
            return "❌ ${msg}"
        } catch (TimeoutException e) {
            def msg = context ? "Timeout. ${context}: ${e.message}" : "Timeout: ${e.message}"
            return "❌ ${msg}"
        } catch (Exception e) {
            def msg = context ? "Error. ${context}: ${e.message}" : "Error: ${e.message}"
            return "❌ ${msg}"
        }
    }
}
