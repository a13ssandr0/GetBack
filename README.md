# GetBack Minecraft Plugin
#### Easily come back to your death place in Minecraft

This plugin is a drop-in replacement and update for [BackAgain](https://www.curseforge.com/minecraft/bukkit-plugins/backagain), since it seems to be no longer updated.

Like the original plugin allows you to teleport back to the place where you died.

### Breaking changes
Updating from `1.1.0` to `1.2.0` requires deleting `config.yml` or editing it to something like this:
```yaml
deathmessage: You died. Pathetic... Use /back to teleport back to your death location.
errormessage: You're not dead, please DIE!!
deaths:
- world: world
  x: 153.2966423555146
  y: 64.0
  z: -415.11122677089094
  pitch: 19.650005
  yaw: -59.099823
  player: joe
- world: world
  x: 1723.199434152375
  y: 66.0
  z: -529.2948763248208
  pitch: 57.29997
  yaw: 81.57434
  player: ligma
- world: world
  x: 1750.1511871672117
  y: -43.0
  z: -368.7057311581982
  pitch: 15.900025
  yaw: 4.729004
  player: pro_gamer_69_420
- world: world
  x: 156.89701091756524
  y: 64.0
  z: -389.19700897704195
  pitch: 17.550077
  yaw: -152.6969
  player: sugon
```

### Commands
`/back` Teleport yourself

`/back <Player>` Teleport someone (useful for server commands)

`/home` Teleport yourself

`/home <Player>` Teleport someone (useful for server commands)

### Dependencies
None
