package utils.webdriver

fun main(){
    val driver = Browser.chrome
    driver.get("http://google.com")
    print(driver.title)
}


fun main2(){
    val firefox = Browser.firefox
    firefox.get("http://google.com")
    print(firefox.title)
}