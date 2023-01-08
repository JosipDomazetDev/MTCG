Describes design

Describes lessons learned

Describes unit testing decisions


Contains tracked time

Contains link to GIT

# Unique feature


| Monster | Crit Chance |
|---------|-------------|
| dragon  | 1%          |
| goblin  | 40%         |
| knight  | 20%         |
| kraken  | 10%         |
| other   | 5%          |

"Critical Strikes" is a mechanic that only comes into effect in pure monster battles.
A critical strike increase the damage dealt by 50%.
The battle log uses a "!" character to indicate critical strikes.

> admin's "Ork" [83!] WINS against kienboec's "Dragon" [55]

The crit damage calculation is applied at the very end. Meaning a dragon with a strength of 70 that fights a WATER
monster might even reach a strength of 210 if he lands a critical strike.

| Monster | Dodge Chance |
|---------|--------------|
| dragon  | 10%          |
| goblin  | 10%          |
| elf     | 30%          |
| other   | 5%           |

"Dodge" is a mechanic that only comes into effect in pure monster battles.
If a monster is about to lose a round, they might be able to dodge in time if they are lucky.
In this case the round ends in a draw.
The battle log reflects whether a dodge occurred.

> admin's "WaterGoblin" [15] DRAWS against kienboec's "Ork" [55] by narrowly escaping the attack


docker exec cb0e090df0ca createdb -U josip mtcg

docker exec cb0e090df0ca createdb -U josip mtcgtest