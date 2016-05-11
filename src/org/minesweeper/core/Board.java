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
    private int callsToUpdate;
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
        initBoard();
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
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
        get(x, y).setNeighbouringMineCount(neighbouringMineCount);
    }
    public void set(Cell cell, int neighbouringMineCount) {
        set(cell.getX(), cell.getY(), neighbouringMineCount);
    }
    public boolean isClear(Cell cell) {
        return isClear(cell.getX(), cell.getY());
    }
    public boolean isClear(int x, int y) {
        return get(x, y).isClear();
    }
    public boolean hasMine(int x, int y) {
        return get(x, y).hasMine();
    }
    public boolean hasMine(Cell cell) {
        return hasMine(cell.getX(), cell.getY());
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
        //maintain the suspense
        if (hasMine(cell) || isMarked(cell)) {
            return;
        }
        for (Cell neighbour : getNeighbours(cell)) {
            if (cell.getNumberOfNeighboursWithMines() < 0 || cell.getNumberOfNeighboursWithMines() > Cell.MAX_NEIGHBOURS) {
                break;
            }
            if (hasMine(neighbour)) {
                //update mine count
                set(cell, cell.getNumberOfNeighboursWithMines() + 1);
            } else if (isClear(neighbour) && (!(hasBeenRevealed(neighbour) || isMarked(neighbour) || hasMine(neighbour)))) {
                //recursively update mine counts of all clear neighbours
                updateCellWithMineCount(neighbour);
            }
        }
    }
    /**
     * Call this after the game has been completed, as it works on processed data.
     * <br>
     * Use for calculating scores:
     * <ol>
     * <li>Divide by total number of clicks (implemented)</li>
     * <li>Divide by time taken</li>
     * </ol>
     *
     * @return an objective measure of the difficulty of this minesweeper board
     */
    public int get3BValue() {
        Cell[][] backup = new Cell[getHeight()][getWidth()];
        for (int i = 0; i < getHeight(); ++i) {
            for (int j = 0; j < getWidth(); ++j) {
                backup[i][j] = new Cell(get(j, i), true);
            }
        }
        int value = 0;
        for (Cell[] row : backup) {
            for (Cell cell : row) {
                if (cell.isClear()) {
                    if (!cell.isMarked()) {
                        cell.mark();
                        ++value;
                        floodFillMark(backup, cell);
                    }
                }
                if (!(cell.isMarked() || cell.hasMine())) {
                    ++value;
                }
            }
        }
        return value;
    }
    private void floodFillMark(Cell[][] backup, Cell cell) {
        Cell[] neighbours = getNeighbours(backup, cell);
        for (Cell neighbour : neighbours) {
            if (!neighbour.isMarked()) {
                neighbour.mark();
                if (neighbour.isClear()) {
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
    /**
     * The click-based score, depending on number of calls to {@link #update(Cell)}.
     * <br>
     * Higher is better. Usually value is less than 1,
     * multiply with time taken for completion and round to get presentable score.
     * <br>
     * For presentable score involving time, higher is not necessarily better.
     * Usually, lower will be better (factoring in time taken to complete).
     * <br>
     * Call this after the game has ended.
     * @return the click-based score
     */
    public double getClickBasedScore() {
        return ((double) get3BValue()) / callsToUpdate;
    }
    public void reveal(Cell cell) {
        reveal(cell.getX(), cell.getY());
    }
    public void reveal(int x, int y) {
        get(x, y).reveal();
    }
    public boolean hasBeenRevealed(Cell cell) {
        return hasBeenRevealed(cell.getX(), cell.getY());
    }
    public boolean hasBeenRevealed(int x, int y) {
        return get(x, y).hasBeenRevealed();
    }
    public void mark(Cell cell) {
        mark(cell.getX(), cell.getY());
    }
    public void mark(int x, int y) {
        get(x, y).mark();
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
    public boolean isMarked(Cell cell) {
        return isMarked(cell.getX(), cell.getY());
    }
    public boolean isMarked(int x, int y) {
        return get(x, y).isMarked();
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