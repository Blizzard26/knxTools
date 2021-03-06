# KNX Tools
This repository currently contains two things: a parser for KNX `.knxproj` Files exported from ETS5 (see [KNX Project Parser](#knx-project-parser)) and a KNX to OpenHAB converter (see [KNX to OpenHAB converter](#knx-to-openhab-converter)) that preprocesses the loaded KNX Project and hands it over to Apache Velocity Templates for generating Things-, Items-, Sitemap-Files etc. It may also be used for creating other files based on the KNX Project file.
Both have been hacked together for my specific purpose ;). That is, they may be buggy / do not consider all the cases, and are not really documented at all.

## Disclaimer
I have no association with the KNX Association whatsoever. The ETS Loader has been implemented by reverse engineering different `.knxproj`-Files and analyzing the XML Schema Definition which is part of ETS5. The repository does not contain any files etc. that are intellectual property of the KNX Association or others. For the same reason, I also do not and will not provide any ready to use builds. To use it you will have to extract the XML Schema Definition from a local ETS5 installation and build it yourself.

## KNX to OpenHAB converter
Loads a KNX .knxproj File, converts all functions to `KNXThing`s based on the provided mapping-configuration, and hands the `KNXThing`s over to [Velocity Templates](https://velocity.apache.org) for creating .things, .items, .sitemap ... files.

Main File: [org.openhab.support.knx2openhab.Main](src/main/java/org/openhab/support/knx2openhab/Main.java)

### How to use it
#### Step 1. Create your KNX Project in ETS5 ;)

As ETS5/KNX and OpenHAB are working fundamentally different, you need to follow a few rules here:
* For each Thing you want to have in OpenHAB, you need to create a function in your KNX Project and add the corresponding Group Addresses to it.
* Each Function needs to have a type key followed by a unique name set as Number. E.g. `L My Lamp` (See Step 3)
* Group Addresses need to follow a (more or less) fixed naming suffix schema per Thing/Function type. (See Step 3)

#### Step 2. Close your KNX Project in ETS and Export it to a .knxproj file.

#### Step 3. Create things.json-File
Edit the Example `conf/things.json` to match your ETS5/KNX Naming Schema or create your own.

The file looks like this:

```
[{
  "key" : "L",
  "functionTypes" : [ "FT-1" ],
  "name" : "Lamp",
  "priority" : 3,
  "items" : [ {
    "key" : "switch",
    "keywords" : [ "Switch" ]
  }, {
    "key" : "status",
    "keywords" : [ "Status" ]
  }, ... ]
}, {
  "key" : "LD",
  "functionTypes" : [ "FT-6" ],
  "name" : "Dimmable Lamp",
  "priority" : 4,
  "items" : [ {
    "key" : "switch",
    "keywords" : [ "Switing" ]
  }, {
    "key" : "status",
    "keywords" : [ "Status" ]
  }, {
    "key" : "dimming",
    "keywords" : [ "Dimming" ]
  }, {
    "key" : "dimmingValue",
    "keywords" : [ "Dimming Value" ]
  }, {
    "key" : "value",
    "keywords" : [ "Current Value" ]
  } ]
},
...]
```

The following figure shows the connection between the entries in things.json and Functions / Group Addresses in ETS.
![Mapping ETS / things.json](doc/mapping.png)

* "key" is the `TYPEKEY` used in the function Number to identify different Thing types. E.g. "L" for Lamp, "LD" for dimmable lamp, "R" for Rollershutter, etc.
* "functionTypes" are the functions types in ETS the `TYPEKEY` may be used with. Note that the same `TYPEKEY` may be used with different function types. Also different function types may be specified for one TYPEKEY. 
The following values may be used for "functionTypes":
   * FT-0: Function Type "custom"
   * FT-1: Function Type "switchable light"
   * FT-6: Function Type "Dimmable Lamp"
   * FT-7: Function Type "sun protection"
   * FT-8: Function Type "heating (switching variable)"
   * FT-9: Function Type "heating (continuous variable)"
  
  There are also some deprecated values which might be used depending on your setup:
   * FT-2: Function Type "dimmable light" (deprecated)
   * FT-3: Function Type "sun protection" (deprecated)
   * FT-4: Function Type "heating radiator" (deprecated)
   * FT-5: Function Type "heating floor" (deprecated)
* "name" a user-defined name for this Thing-Type. This will be used by error messages and may be used inside the Velocity Templates
* "priority" a user-defined priority for Things of this type which, e.g., may be used for sorting Things in sitemaps.
* "items" a list of items. Each item basically maps to a possible Group Address in the KNX Function. Items will be provided as `KNXItems` inside the `KNXThing` for processing them in the Velocity Templates. Each item has the following attributes:
  * "key" a user-defined key for this Item. This will be used for identifying the item in Velocity Templates.
  * "keywords" a list of keywords. Each keyword represents a possible suffix of a group address in the KNX Function. If a Group Address Name contains one of the keywords as suffix it will be assigned to the respective Item.
  * Optional: "label" a Label provided as Name of the item to the Velocity Templates. This overrides the name provided by the KNX Project.

#### Step 4. Create Velocity Templates
Edit the example templates in the `templates/`-Directory or create your own templates. See the [Velocity User Guide](https://velocity.apache.org/engine/2.2/user-guide.html) for how it works. Currently the following version are used:
* [Velocity 2.2](https://velocity.apache.org/engine/2.2/)
* [Velocity Tool 3.0](https://velocity.apache.org/tools/3.0/)

Templates must be placed in the `templates/` directory and have `.vm` as file extension.

The following inputs are provided to the template:
* `$things` a List of `KNXThing`s parsed from the ETS-File using the things.json-Mapping File
* `$env` a Map containing the env-Section from the conf.json-File. This may for example be used to provide general information (e.g., IP Address of KNX Gateway).
* `$knx` the raw KNX Object representing the Parsed .knxproj-File (hard to use; It's better to used the KNXThings-Mechanism)
* `$installation` the raw KNX Installation object that has been selected.
* `$modelUtil` Utility Class for some advanced stuff (see [Advanced Options](#advanced-options))
* `$tools` VelocityTools Utility Class
* `$templates` List of all `.vm`-Files in the `templates/`-Directory. This may be used for dynamically splitting templates into multiple files. (see [Advanced Options](#advanced-options))

`KNXThing`s have the following properties (see org.openhab.support.knx2openhab.model.KNXItem):
* `descriptor`: The `KNXThingDescriptor` read from the things.json-File
* `key`: function key from the KNX Project. Spaces, slashes and backslashed are replaced by underscore.
* `description`: description from the KNX Project
* `location`: Name of the parent location (Space) from the KNX Project
* `priority`: user-defined priority from the things.json-File
* `space`: KNXSpace object of the parent location (Space) from the KNX Project
* `items`: Map of Item-Key (things.json-File) to `KNXItem`s
* `context`: Map from String to String read from the comments section of the Function in the KNX Project (see [Advanced Options](#advanced-options) for details)

`KNXItem`s have the following properties (see org.openhab.support.knx2openhab.model.KNXItem):
* `key`: Item-Key from the things.json-File
* `itemDescriptor`: The `KNXItemDescriptor` read from the things.json-File
* `address`: Group Address of the item in the format `high.mid.low` (e.g., `1.2.3`)
* `description`: Description of the Group Address from the KNX Project
* `name`: Name of the Group Address from the KNX Project (may be overridden by things.json-File)
* `type`: Group Address Value Type from the KNX Project as String (e.g., "1.001", "1.009", "5.001", etc.)
* `readable`: Indicates whether or not the associated group address may be read (i.e., KNX Read-Flag is set on one of the Com-Objects associated with the group address)
* `writeable`: Indicates whether or not the associated group address may be written (i.e., KNX Write-Flag is set on one of the Com-Objects associated with the group address)
* `context`: Map from String to String read from the comments section of the Function in the KNX Project (see [Advanced Options](#advanced-options) for details)

#### Step 5. Run
Run 

```
knx2openhab[.bat] --project projectFile.knxproj [--password pass] [--projectId P53] [--installationId 0] [--configDir conf/] (--template template.vm --out outputfile)+
```

E.g., for the example project

```
knx2openhab[.bat] --project <MyProject.knxproj> --password <MySecretPassword> --configDir conf/ --template things.vm --out out\knx.things --template items.vm --out out\knx.items --template sitemap.vm --out out\knx.sitemap --template itemhtml.vm --out out\items.html
```

### Example
TODO

### Advanced Options

#### Context on KNXItems and KNXThings
Context are special comments in ETS that are available to the templates.

To create a context add comment to a ETS function or GA that looks like this:

```
#OPENHAB
key=value
#END
```

Anything between `#OPENHAB` and `#END` is part of the context. The lines inside the context need to be key / value pairs separated by `=`. They are available as `Map<String, String> context` on `KNXThing`s and `KNXItem`s in the velocity template. 

For example, a context

``` 
#OPENHAB
groups = group1, group2
#END
```

can be used like this in Velocity:

```
#if ( $item.context.groups )( $item.context.groups )#en
```

or alternatively

```
#if ( $item.context["groups"] )( $item.context.["groups"] )#end
```

For an example on how to use it see Velocity macro `groups` in [examples\items.vm](examples\items.vm).

#### Advanced Templating Stuff
The `$templates` variable available the Velocity Templates can be used for dynamic loading of additional templates.
For example the following can be used to dynamically construct a sitemap based on multiple partial sitemap-Templates:

```
#foreach ($template in $collection.sort($templates))
#if ($template.endsWith('.sitemap.vm'))

#parse($template)

#end
#end
```

### Common Error Messages / Warnings
* `Group address '{}' ({}) is not assigned to any function` <br/>
The given group address is not assigned to any function and thus cannot be used by the templates. You can ignore this message if you do not want to use the the group address. Otherwise assign the group address to a function in ETS.
* `Function '{}' @ '{}' has no number assigned` <br/>
The given function has no set and thus cannot be processed. If you do not want to use the function, you can ignore this message. Otherwise set the number field on the function in ETS.
* `Unsupported function type: {}` <br/>
There are no mapping defined for this function-type in `things.json`. Check your function-type in ETS or the `things.json` file.
* `Unkown Thing type {} (function type {}) for function {}` <br/>
The `TYPEKEY` prefix Function-Type combination of the function is not defined by the `things.json` file. Check your function number, function-type or the mapping file.
* `Group Address '{}' has no key` <br/>
The given group address has no key. Check the group address in ETS.
* `Unable to identify item type for '{}' on thing '{}'` <br/>
The given group address has known suffix for the thing type of the respective function. Check the group address or the mapping-file.


### Know limitations / Issues
* See [Know Limitations / Issues of KNX Project Parser](#know-limitations--issues-1)

## KNX Project Parser
Loads a KNX .knxproj File as Java Objects Tree.

Main File: [org.knx.ETSLoader](src/main/java/org/knx/ETSLoader.java)

### How to use it:
```
File knxProjectFile = new File(...);
ETSLoader loader = new ETSLoader();
KNX knx = loader.load(knxProjectFile, Optional.of("MySecretPassword"));

System.out.println("Projects: " +
                    knx.getProject().stream().map(KnxProjectT::getId).collect(Collectors.joining(", ")));

KnxProjectT knxProject = knx.getProject().get(0);

System.out.println("Installations on project " + knxProject.getId() + ": "
            + knxProject.getInstallations().getInstallation().stream().map(KnxInstallation::getInstallationId)
                    .map(String::valueOf).collect(Collectors.joining(", ")));
```

See [org.openhab.support.knx2openhab.Main](src/main/java/org/openhab/support/knx2openhab/Main.java) for more...

### Know Limitations / Issues
* A KNX .knxproj File is basically a zip file (containing a directory/zip file for each project) consisting of several XML files representing one big XML structure. The Parser tries to put the different XML-Files together into one big Object structure representing the XML Structure. However, ID handling within the XML Files sometimes is quite strange. For example sometimes ID-Refs contain the full ID, sometime they only contain the last part of the ID and the remainder needs to be derived from the ID-Ref of the parent element. I tried my best to handle all strange cases I came across (see [org.knx.LookupIdResolver#resolveObject(String, BaseClass)](src/main/java/org/knx/LookupIdResolver.java) for some details), but there may be others... If so please open an issue and provide an example `.knxproj` file so I can reproduce the issues.

# Build it
To start hacking:

* make sure you have a JDK 1.8 (Note that the build and in particular the used gradle JAXB-Plugin does not support a JDK > 1.8 at the moment; see https://github.com/IntershopCommunicationsAG/jaxb-gradle-plugin/issues/11)
* clone this repository
* add a current KNX XML Schema Definition `src\main\resources` (see [src\main\resources\Readme.md](src/main/resources/Readme.md))
* build it - call `gradlew build`

```
git clone https://github.com/Blizzard26/knxTools.git
cp knx.xsd knxTools/src/main/resources
cd knxTools
gradlew build
```

The created zip/tar can be found in build/distributions

# TODOs
* Type-Mapping in `knx.xjb` is not fully done yet.
* JUnit Test for other parts
* JavaDoc
* Documentation (extend this README)
* Provide more extensive example project

# License
[KNX Tools](https://github.com/Blizzard26/knxTools) by [Andreas Lanz](https://github.com/Blizzard26) is licensed under [CC BY 4.0 CC](https://creativecommons.org/licenses/by/4.0/) ![CC Icon](https://mirrors.creativecommons.org/presskit/icons/cc.svg) ![BY Icon](https://mirrors.creativecommons.org/presskit/icons/by.svg)