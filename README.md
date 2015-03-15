# Open Share Location Plugin for Conversations

This is a location sharing plugin for the XMPP client
[Conversations][conversations]. Unlike the [official
plugin][conversations-loc], this one uses data from Open Street Maps and
doesn't require the Google Play Services to be installed. Consequentially, it
will probably not be as accurate as the official one (which uses the Google
API's) as it only uses basic AOSP geolocation services.

Services used:

 - [OSMDroid][osmdroid]
 - [Mapnik][mapnik]

[conversations]: https://github.com/siacs/Conversations
[conversations-loc]: https://github.com/siacs/ShareLocationPlugin
[osmdroid]: https://github.com/osmdroid/osmdroid
[mapnik]: http://mapnik.org/
