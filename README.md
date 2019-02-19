# Tor library
This library helps you to connect to the Tor network in a simple way.

[![](https://jitpack.io/v/Merseyside/OrbotLibrary.svg)](https://jitpack.io/#Merseyside/OrbotLibrary)

## How to Gradle?
```
//build.gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

```
//app/build.gradle
dependencies {
    implementation 'com.github.Merseyside.OrbotLibrary:library:reliase-tag'
}
```
## How to use?
```
private val orbotManager by lazy {OrbotManager.getInstance(application)} 

override fun onCreate(savedInstanceState : Bundle?) {
    super.onCreate(savedInstanceState)
    
    orbotManager.setOrbotListener(
        object : OrbotManager.OrbotListener {...}
    )
  
    orbotManager.startTor()
}

override fun onDestroy() {
    super.onDestroy()
    
    orbotManager.stopTor()
}
```
