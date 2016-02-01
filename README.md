# MineSweeper
This game consitsts of a square grid, whose dimensions depend on the level of difficulty you choose. Bigger the grid, more difficult it becomes.
The main objective of the game is to identify the mines spread out in specific boxes in the grid. This is done by selecting all other boxes, except those containing the mines.
There are two types of boxes in the grid:
1) which contains a mine ( denoted by "X" )
2) which does not contain the mine, or safe boxes ( denoted by a number from 0 to 8 )

Initially all boxes are marked as "*". This is the unflipped state of boxes. One does not know what is below it.
At the beginning of the game, some of the safe boxes are already flipped, for ease of gameplay.

How am i supposed to know the position of mines?
Ans. Well, that is quite simple. The number marked on the safe boxes is the EXACT number of mines around that particular box.
Hence, a box with a number 3, means that out of the 8 neighbouring boxes, any 3 of them contains a mine each. No more, no less.
So as you flip more and more boxes, you get an idea of the possible locations of mines.

And just in case if you were wondering, the mines do not randomly shift positions during the gameplay. They all are fixed for each game.

Thats all for the basics.

Happy mine-sweeping! :D
