# MovieApp

-   This is a application which connects to the Movies RESTFUL Service.

## How to enable JUnit5?

-   Please make the below changes to enable JUnit5 in your project.

### build.gradle

-   Add the below code to enable Junit5 as a test platform.

```youtrack
test {
    useJUnitPlatform() // enables Junit5
}
```
-   Add this dependency to use the Junit% 

```youtrack
dependencies {
//junit5-dependencies
	testImplementation('org.junit.jupiter:junit-jupiter:5.5.1')
}
```