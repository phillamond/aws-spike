package awsspike

import org.junit.runner.RunWith
import cucumber.api.junit.Cucumber

@RunWith(classOf[Cucumber])
@Cucumber.Options(tags = Array("~@wip"), format = Array("pretty", "html:target/cucumber", "json:target/cucumber.json"))
class RunCukesTest