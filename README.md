# DSpace @ UNB

Themes & other customizations for UNB's implementation of the DSpace repository.

## Set up development environment

Clone appropriate branch of the repo: `[dev]/unb-dspace`

Fetch DSpace release: `[dspace-release]`

Back up default config files:

`mv [dspace-release]/dspace/config [dspace-release]/dspace/config_default` 

Link repository-managed config files to DSpace release directory:

`ln -s [dev]/unb-dspace/config [dspace-release]/dspace/config`

Link themes, modules, etc. as needed:

Remove existing (empty) webapp directory for symlink:

`rmdir [dspace-release]/dspace/modules/xmlui/src/main/webapp`

Create new link:

`ln -s [dev]/unb-dspace/modules/xmlui/src/main/webapp [dspace-release]/dspace/modules/xmlui/src/main/webapp`

... etc.

### Override default configuration settings

Customize config values with the default Maven profile defined in `settings.xml.default`.  

`cp [dev]/unb-dspace/config/settings.xml.default $HOME/.m2/settings.xml`

Edit properties defined in `$HOME/.m2/settings.xml`.  To use the settings in the build:

`cd [dspace-release]/dspace`
`mvn package -Dconfigure-dspace=true`

### Override PostgreSQL connection information

** Maven 3 **

If you're not connecting to PostgreSQL with default user account and password: edit the connection settings in the PostgreSQL profile defined in `[dspace-release]/dspace/pom.xml`

Run Maven:

`mvn package -Dconfigure-dspace=true -P postgres`

** Maven 2 **

Copy `[dspace-release]/dspace/profiles-example.xml` to `[dspace-release]/dspace/profiles.xml`.  Edit connection details; Maven will pick up `profiles.xml` automatically.

