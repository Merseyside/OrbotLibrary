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
### Nodes
```
val currentExitNode = orbotManager.getExitNode() //Get current exit node

orbotManager.setExitNode(country) //Sets new exit node
```

### Bridges
```
orbotManager.setBridge(OrbotConstants.BRIDGES.getByValue(value)) // Sets one of availbale bridges(Directly, Community or Cloud)

orbotManager.getBridge() // Get current bridge
```
