# ProjectGameOOP — Zombie Survival 2D (continued from original project)

This is an expanded version of the user's existing Java Swing/Java2D game. The original character artwork in `images/` is preserved. Source files remain in the original `scr/` directory.

## Requirements
- JDK 17 or newer
- Windows: double click `run_windows.bat` (or run it from Command Prompt)
- Linux/macOS: run `sh run_linux_mac.sh`
- Manually (from project root): `javac -encoding UTF-8 -d out scr/*.java` then `java -cp out Main`

## How to play
A/D: move. SPACE: jump. J or left mouse button: fire. 1–5: weapons. R: reload. H: heal for $100. B: armor for $100. ESC: return to select stage. ENTER: stage selection after winning or losing.

## Game design
- 10 stages, each with its own palette, world length, increased zombie quota and hazard pattern.
- Defeat regular zombies, then Mini Boss (stages 1–9) or Final Boss (stage 10). Walk to the EXIT to unlock the next stage.
- 5 weapons: Pistol, SMG, Shotgun, Assault Rifle, Sniper.
- Zombie variants: Walker, Runner, Brute; MiniBoss and FinalBoss extend Zombie.
- Stage progress saved with Java Preferences (on this computer).
- Java Swing/Java2D only, NO SFML/JSFML dependency.

## OOP learning points
- Encapsulation: state and getters in Player, Zombie, Weapon, Bullet.
- Inheritance: MiniBoss and FinalBoss extend Zombie.
- Polymorphism: a List<Zombie> holds different enemy subclasses.
- Composition: GamePanel owns Player, Weapon[], List<Zombie> and List<Bullet>.

## Important notes
- This is a runnable coursework prototype with procedural stage graphics, not a finished commercial game.
- Stage variants are primarily palette/hazard/progression differences. Unique boss abilities, sprite animation, sounds and detailed custom stage art can be developed further.
- Original assets are included exactly as uploaded.


## Pixel-art graphic update
Original artwork is preserved. Transparent versions `images/clean_player_idle.png` and `images/clean_Zombie.png` are used in-game. Stage 1 also has city props (ruined cars, barrels, street lamps and barricades) drawn procedurally. Launch from the project folder using `run_windows.bat` or `javac -encoding UTF-8 -d out scr\*.java` then `java -cp out Main`.
