# Best known solutions

The file HIGHSCORES in this directory contains the output of BreakoutUtilities program with "highscore" command. It queries all the solutions in BreakoutSolutions table in DynamoDB, orders them by the gametime required for each solution, and then lists the solution in best-last order.

The solutions can be replayed with ReplayAI by copy-pasting the chain as input. The following configuration can be used (for example):
* main program: karski.breakout.simple.Breakout
* program arguments: karski.breakout.simple.ReplayAI
* environment variable PRG_LOCATION=<path-to-docker/breakout/breakout.prg>
* environment variable VICE_COMMAND=<path-to-vice/x64.exe>
* stdin input when the program starts: [1:54 1 25][2:120 3 23][3:190 5 19][4:264 7 38][5:409 18 202][6:480 19 143][7:862 33 157][8:1297 47 184][9:1399 48 118][10:1504 51 99][11:1575 52 32][12:1637 54 0][13:2009 83 99][14:2084 85 40][15:2154 87 40][16:2263 90 95][17:2352 91 60][18:2423 93 234][19:2489 94 215][20:2596 96 221][21:2685 98 170][22:2734 100 fi]

