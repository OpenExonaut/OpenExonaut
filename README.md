# OpenExonaut

[//]: # (OpenATBP releases badge was here)
[//]: # (OpenATBP Trello badge was here)
[//]: # (OpenATBP Discord badge was here)
[![License Badge](https://img.shields.io/github/license/OpenExonaut/OpenExonaut)](https://github.com/OpenExonaut/OpenExonaut/blob/main/LICENSE.md)

An open-source lobby, service, and game server for Project Exonaut, built upon the foundation of [OpenATBP](https://github.com/OpenATBP/OpenATBP).

[//]: # (OpenATBP docs/screenshot2.png was here)

## Status
Currently, games can be played and rewards earned and spent. Position extrapolation and achievements still need to be worked on.

## Server Architecture
Originally, Project Exonaut required several server-side components in order to to function:
* Web server to serve static content/streaming assets
* Web server that provides service/API endpoints
* Socket policy server to satisfy the Unity Web Player [security sandbox](https://docs.unity3d.com/351/Documentation/Manual/SecuritySandbox.html)
* SmartFoxServer2X with custom zone and room extensions acting as the matchmaking server and actual game server respectively

To simplify development and deployment, all but the last component have been combined into one piece of software, which is available under the `ExonautServer` directory. The SmartFoxServer2X extensions live under the `ExonautExtension` directory.

~~More in-depth explanations of each component, how the client interacts with them, and how request/response packets are structured can be found in the `docs/` folder.~~
This is unfortunately not available yet, but work is slowly being done. For the time being, feel free to reference decompiled client code generated via ILSpy/dnSpy.

## Development

### Prerequisites 
*Ensure these are all installed before proceeding!*
* Git
* Java Development Kit 11
* SFS2X Community Edition
* NodeJS and NPM
* MongoDB Server 

### Setting up
1. Clone the repository: `git clone https://github.com/OpenExonaut/OpenExonaut`
1. Open a new terminal inside of the `ExonautServer` directory
1. In this new terminal window, run the following command to install dependencies and download required asset files - this may take a while! `npm install`
1. Copy the example config in the ExonautServer directory: `cp config.js.example config.js` - once copied, edit it to include the connection string URI for your MongoDB server
1. Run ExonautServer using the following command: `npm run start` - if done correctly you should see `Express server running on port 80!`
1. Start SmartFoxServer2X once so it can generate the correct files and folders, then close it
1. Open another terminal, this time in the root of the repository
1. Run the following commands to copy necessary files, then compile the game extensions: `.\gradlew ExonautExtension:copySFS2XLibs`, `.\gradlew ExonautExtension:allJars`
1. Provided there weren't any errors, deploy the SmartFox extensions and library: `.\gradlew ExonautExtension:copyDataFiles`, `.\gradlew ExonautExtension:deployAllJars`
1. Copy the example config in the SFS2X extension directory (SFS2X/extensions/Exonaut, should be right next to two jar files): `cp config.properties.example config.properties` - once copied, edit it to include the same URI string you did in step 4.
1. Start SmartFoxServer2X, you should see a log line indicating the zone extension is working: `Exonaut Zone Extension init finished`
1. Finally, connect to http://127.0.0.1:80 with an NPAPI-compatible browser such as Pale Moon to test the game!

Note that you can also run any Gradle task (`gradlew` commands) graphically through an IDE such as IntelliJ IDEA or Eclipse. 

These instructions are subject to change, if you run into any problems or have questions feel free to open an issue here on Github.

## License
MIT unless specified otherwise

![SFS2X Logo](docs/sfs2xlogo.png)
