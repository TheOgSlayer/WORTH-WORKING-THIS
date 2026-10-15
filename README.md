# WorthLore

Adds a "Worth" lore line under item names on hover, plus a `/worth` GUI.

## Requirements
- Spigot / Paper 1.21+
- Java 21
- ProtocolLib 5.3.0+ (put it in /plugins too)

## Build
    mvn clean package
Jar will be at target/WorthLore.jar -> drop in /plugins.

## Config (plugins/WorthLore/)
- prices.yml  ->  MATERIAL_NAME: worth   (e.g. COBBLESTONE_SLAB: 20)
- config.yml  ->  lore format, GUI title, messages

## Commands
- /worth           open the worth GUI (permission: worth.use, default everyone)
- /worth reload    reload prices.yml & config.yml (permission: worth.reload, default op)

## Building on GitHub
Every push to main/master runs .github/workflows/build.yml.
Download the compiled jar from the repo's Actions tab -> latest run -> Artifacts -> WorthLore.
