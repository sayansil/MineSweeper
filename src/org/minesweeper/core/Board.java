package org.minesweeper.core;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
/**
 * Encapsulates a MineSweeper game board.
 */
public class Board implements Serializable, Cloneable {
    private final int width, height;
    private final Cell[][] board;
    private final Cell[] mines;
    private boolean firstTime;
    private int callsToUpdate, difficulty;
    public Board(int width, int height, float mineFraction) {
        if (mineFraction < 0 || mineFraction > 1) {
            throw new IllegalArgumentException("Fraction of mines can only be between 0 & 1 : " + mineFraction);
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Illegal board dimensions : " + width + " , " + height);
        }
        board = new Cell[height][width];
        this.height = board.length;
        this.width = board[0].length;
        mines = new Cell[Math.round(mineFraction * getHeight() * getWidth())];
        firstTime = true;
        callsToUpdate = 0;
        difficulty = -1;
        difficulty = get3BValue();
        initBoard();
    }
    public void initBoard() {
        for (int i = 0; i < getHeight(); ++i) {
            for (int j = 0; j < getWidth(); ++i) {
                board[i][j] = new Cell(j, i);
            }
        }
    }
    public Cell[][] getBoard() {
        return board;
    }
    public float getMineFraction() {
        return ((float) getMinesCount()) / (getHeight() * getWidth());
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public int getMinesCount() {
        return getMines().length;
    }
    public Cell[] getMines() {
        return mines;
    }
    public Cell get(Cell cell) {
        return get(board, cell);
    }
    public Cell get(Cell[][] board, Cell cell) {
        return get(board, cell.getX(), cell.getY());
    }
    public void set(int x, int y, int neighbouringMineCount) {
        set(board, x, y, neighbouringMineCount);
    }
    public void set(Cell[][] board, int x, int y, int neighbouringMineCount) {
        get(x, y).setNeighbouringMineCount(neighbouringMineCount);
    }
    public Cell get(int x, int y) {
        return get(board, x, y);
    }
    public Cell get(Cell[][] board, int x, int y) {
        y = bounded(y, getHeight());
        return board[y][bounded(x, getWidth())];
    }
    private static int bounded(int ptr, int size) {
        return (ptr < 0) ? Math.abs(size + ptr) % size : ((ptr >= size) ? (ptr % size) : ptr);
    }
    public void set(Cell cell, int neighbouringMineCount) {
        set(board, cell, neighbouringMineCount);
    }
    public void set(Cell[][] board, Cell cell, int neighbouringMineCount) {
        set(board, cell.getX(), cell.getY(), neighbouringMineCount);
    }
    public boolean isClear(Cell cell) {
        return isClear(board, cell);
    }
    public boolean isClear(Cell[][] board, Cell cell) {
        return isClear(board, cell.getX(), cell.getY());
    }
    public boolean isClear(Cell[][] board, int x, int y) {
        return get(board, x, y).isClear();
    }
    public boolean isClear(int x, int y) {
        return isClear(board, x, y);
    }
    public boolean hasMine(int x, int y) {
        return hasMine(board, x, y);
    }
    public boolean hasMine(Cell[][] board, int x, int y) {
        return get(board, x, y).hasMine();
    }
    public boolean hasMine(Cell cell) {
        return hasMine(board, cell);
    }
    public boolean hasMine(Cell[][] board, Cell cell) {
        return hasMine(board, cell.getX(), cell.getY());
    }
    public ArrayList<Cell> getMarkedCells() {
        ArrayList<Cell> marked = new ArrayList<>(getHeight() * board[0].length);
        for (Cell[] row : board) {
            for (Cell cell : row) {
                if (isMarked(cell)) {
                    marked.add(new Cell(cell, true));
                }
            }
        }
        return marked;
    }
    public Cell[] getNeighbours(Cell cell) {
        return getNeighbours(board, cell);
    }
    public Cell[] getNeighbours(Cell[][] board, Cell cell) {
        cell = get(board, cell);
        Cell[] neighbours = new Cell[Cell.MAX_NEIGHBOURS];
        for (int y = cell.getY() - 1, count = 0; y <= cell.getY() + 1; ++y) {
            for (int x = cell.getX() - 1; x <= cell.getX() + 1 && count < neighbours.length; ++x) {
                if (cell.getX() == x && cell.getY() == y) {
                    continue;
                }
                neighbours[count++] = get(board, x, y);
            }
        }
        return neighbours;
    }
    public boolean isLost(Cell selected) {
        boolean lost = hasMine(selected);
        if (lost) {
            reveal(selected);
            //reveal all the mines,which hadn't been revealed
            revealAll();
        }
        return lost;
    }
    public void revealAll() {
        for (Cell[] row : board) {
            for (Cell cell : row) {
                if (!hasBeenRevealed(cell)) {
                    reveal(get(cell));
                }
            }
        }
    }
    private void updateCellWithMineCount(Cell cell) {
        updateCellWithMineCount(board, cell);
    }
    private void updateCellWithMineCount(Cell[][] board, Cell cell) {
        //maintain the suspense
        if (hasMine(board, cell) || isMarked(board, cell)) {
            return;
        }
        for (Cell neighbour : getNeighbours(board, cell)) {
            if (cell.getNumberOfNeighboursWithMines() < 0 || cell.getNumberOfNeighboursWithMines() > Cell.MAX_NEIGHBOURS) {
                break;
            }
            if (hasMine(board, neighbour)) {
                //update mine count
                set(board, cell, cell.getNumberOfNeighboursWithMines() + 1);
            } else if (isClear(board, neighbour) && (!(hasBeenRevealed(board, neighbour) || isMarked(board, neighbour) || hasMine(board, neighbour)))) {
                //recursively update mine counts of all clear neighbours
                updateCellWithMineCount(board, neighbour);
            }
        }
    }
    /**
     * Use for calculating scores:
     * <ol>
     * <li>Divide by total number of clicks (implemented)</li>
     * <li>Divide by time taken (implemented)</li>
     * </ol>
     *
     * @return an objective measure of the difficulty of this minesweeper board
     */
    public int get3BValue() {
        //status check
        if (difficulty >= 0) {
            return difficulty;
        }
        //initialization
        Cell[][] backup = new Cell[getHeight()][getWidth()];
        for (int i = 0; i < getHeight(); ++i) {
            for (int j = 0; j < getWidth(); ++j) {
                backup[i][j] = new Cell(get(j, i), true);
            }
        }
        //processing
        for (Cell[] row : backup) {
            for (Cell cell : row) {
                updateCellWithMineCount(backup, cell);
            }
        }
        //calculation
        int value = 0;
        for (Cell[] row : backup) {
            for (Cell cell : row) {
                if (isClear(backup, cell)) {
                    if (!isMarked(backup, cell)) {
                        mark(backup, cell);
                        ++value;
                        floodFillMark(backup, cell);
                    }
                }
                if (!(isMarked(backup, cell) || hasMine(backup, cell))) {
                    ++value;
                }
            }
        }
        difficulty = value;
        return value;
    }
    private void floodFillMark(Cell[][] backup, Cell cell) {
        Cell[] neighbours = getNeighbours(backup, cell);
        for (Cell neighbour : neighbours) {
            if (!isMarked(backup, neighbour)) {
                mark(backup, neighbour);
                if (isClear(backup, neighbour)) {
                    floodFillMark(backup, neighbour);
                }
            }
        }
    }
    public boolean isWon() {
        boolean win = true;
        for (Cell[] row : board) {
            for (Cell cell : row) {
                if (!(hasMine(cell) || hasBeenRevealed(cell))) {
                    win = false;
                    break;
                }
            }
        }
        win |= getMarkedCells().containsAll(Arrays.asList(mines));
        if (win) {
            revealAll();
        }
        return win;
    }
    public int getClicks() {
        return callsToUpdate;
    }
    /**
     * The click-based score, depending on number of calls to {@link #update(Cell)}.
     * <br>
     * Lower is better. Usually value is more than 1 at the end of the game if it has been won,
     * and less than 1 if it has been lost.
     * <br>
     * Multiply this with time taken for completion and round to get presentable score.
     * <br>
     * For presentable score involving time, lower is better.
     *
     * @return the click-based score
     */
    public double getClickBasedScore() {
        return ((double) getClicks()) / get3BValue();
    }
    /**
     * Guaranteed to be non-negative.
     *
     * @param timeTaken the time taken till now
     * @return the presentable score.
     */
    public int getPresentableScore(int timeTaken) {
        return Math.round((float) (getClickBasedScore() * timeTaken));
    }
    public void reveal(Cell cell) {
        reveal(cell.getX(), cell.getY());
    }
    public void reveal(int x, int y) {
        get(x, y).reveal();
    }
    public boolean hasBeenRevealed(Cell cell) {
        return hasBeenRevealed(board, cell);
    }
    public boolean hasBeenRevealed(Cell[][] board, Cell cell) {
        return hasBeenRevealed(board, cell.getX(), cell.getY());
    }
    public boolean hasBeenRevealed(int x, int y) {
        return hasBeenRevealed(board, x, y);
    }
    public boolean hasBeenRevealed(Cell[][] board, int x, int y) {
        return get(board, x, y).hasBeenRevealed();
    }
    public void mark(Cell cell) {
        mark(board, cell);
    }
    public void mark(Cell[][] board, Cell cell) {
        mark(board, cell.getX(), cell.getY());
    }
    public void mark(Cell[][] board, int x, int y) {
        get(board, x, y).mark();
    }
    public void mark(int x, int y) {
        mark(board, x, y);
    }
    public boolean isMarked(Cell cell) {
        return isMarked(board, cell);
    }
    public boolean isMarked(Cell[][] board, Cell cell) {
        return isMarked(board, cell.getX(), cell.getY());
    }
    public boolean isMarked(int x, int y) {
        return isMarked(board, x, y);
    }
    public boolean isMarked(Cell[][] board, int x, int y) {
        return get(board, x, y).isMarked();
    }
    public GameStatus update(Cell selected) {
        if (firstTime) {
            plantMines(selected);
            firstTime = false;
        }
        ++callsToUpdate;
        if (isLost(selected)) {
            return GameStatus.LOST;
        } else if (isWon()) {
            reveal(selected);
            return GameStatus.WON;
        } else {
            reveal(selected);
            updateCellWithMineCount(selected);
            return GameStatus.CONTINUING;
        }
    }
    /**
     * Call this before updating neighbouring mines count for the first time.
     *
     * @param initialCell the initial cell selected, which may not have a mine.
     */
    public void plantMines(Cell initialCell) {
        for (int counter = 0; counter < getMinesCount(); ) {
            //get a random cell
            Cell randomCell = get(randInt(getHeight()), randInt(getWidth()));
            //protect a (valid) initial cell, and don't make it repeat itself (mine locations should be unique)
            if ((initialCell == null || (!randomCell.equals(initialCell))) && (!hasMine(randomCell))) {
                //add the random mine-containing cell to the mine list
                mines[counter] = new Cell(randomCell, true);
                mines[counter].setMine();
                //update the counter
                counter += randomCell.setMine();
            }
        }
    }
    private int randInt(int max) {
        return randInt(0, max - 1);
    }
    private int randInt(int min, int max) {
        return (int) (Math.random() * (Math.abs(max - min) + 1)) + (min <= max ? min : max);
    }
}