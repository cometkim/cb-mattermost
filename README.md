# cb-mattermost
See [codeBeamer](https://codebeamer.com/cb/project/CB) activity stream in your [Mattermost](https://www.mattermost.org/) channel

![](https://raw.githubusercontent.com/CometKim/cb-mattermost/master/screenshot.png)

## Installation
__WARNING__ : Backup your `my-applicationContext.xml` if you already have. 
```
cd [CB_INSTALL]/tomcat/webapps/cb/
wget https://raw.githubusercontent.com/CometKim/cb-mattermost/master/build.zip
upzip build.zip
```

## Configuration
See and Edit your `my-applicationContext.xml` to create MattermostNotifier bean.

* You have to set your mattermost incoming-url
* And you can specify username, icon and channel for mattermost-side
* Also you can specify tracker, event and message templates

Then __restart your codeBeamer server__  
That's it!


## Customize your message templates
Message templates are in `[CB_INSTALL]/tomcat/webapps/cb/config/templates/mattermost`

You can edit or make new template file in [Velocity](http://velocity.apache.org/engine/1.7/vtl-reference.html)