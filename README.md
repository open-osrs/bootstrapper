# bootstrapper
OpenOSRS bootstrapper

Yes it's kotlin, yes it ugly, and yes it sometimes kind of strap. Got GitHub Actions so this
quickly changed from multi-platform tornadofx gui app to what it is now for automatic nightly deployments.
will be updated when tested/finished etc.


## How to use

* First: run `gradle clean build` then  `gradle :client:processResources :client:dependencyReportFile` in the main OpenOSRS project. It should generate a file called dependencies.txt.
* Open the bootstrapper project and run `gradle clean build`
* Once the bootstrapper has finished building, run the application and you should see a screen similar to this, showing the old bootstrap: https://cdn.discordapp.com/attachments/569741527254040586/632230025160294414/unknown.png
* Next select the mode from the dropdown (we will use staging for our purposes) and press New Bootstrap.
* A file selector will pop-up. Navigate to the root project directory of the client (normally this would be called runelite or OpenOSRS but it depends on whateve you named it) and select that folder. https://cdn.discordapp.com/attachments/569741527254040586/632230664254652475/unknown.png
* The bootstrapper should then do its thing and proccess all the dependencies. https://cdn.discordapp.com/attachments/569741527254040586/632231091352371233/unknown.png
* Once its done, you will see a popup, as well as a new bootstrap tab https://cdn.discordapp.com/attachments/569741527254040586/632231791100690472/unknown.png
* feel free to close the old bootstrap tab. Now you can sort the new one by name and go through and right click any duplicates that didn't get caught automaticlally and delete the oldest versions. https://cdn.discordapp.com/attachments/569741527254040586/632232597543583775/unknown.png
* When you're finished with that hit validate and let it double check all the hashes (yeah i know the count is off, will be fixed soon) https://cdn.discordapp.com/attachments/569741527254040586/632233484047351828/unknown.png
* Then you should be able to click the Export button and your bootstrap along with the artifacts to be uploaded will be exported to the Bootstrapper's Out dir: https://cdn.discordapp.com/attachments/569741527254040586/632234117567873025/unknown.png
