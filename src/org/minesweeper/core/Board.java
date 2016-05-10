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
        return get(cell.getX(), cell.getY());
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
        cell = get(cell);
        Cell[] neighbours = new Cell[Cell.MAX_NEIGHBOURS];
        for (int y = cell.getY() - 1, count = 0; y <= cell.getY() + 1; ++y) {
            for (int x = cell.getX() - 1; x <= cell.getX() + 1 && count < neighbours.length; ++x) {
                if (cell.getX() == x && cell.getY() == y) {
                    continue;
                }
                neighbours[count++] = get(x, y);
            }
        }
        return neighbours;
    }
    public boolean isLost(Cell selected) {
        boolean lost = hasMine(selected);
        if (lost) {
            reveal(selected);
            //reveal all the mines,which hadn't been revealed
            revealAllMines();
        }
        return lost;
    }
    public void revealAllMines() {
        for (Cell cell : mines) {
            reveal(get(cell));
        }
    }
    private void updateCellWithMineCount(Cell cell) {
        //maintain the suspense
        if (hasMine(cell) || isMarked(cell)) {
            return;
        }
        for (Cell neighbour : getNeighbours(cell)) {
            //@formatter:off
            if (cell.getNumberOfNeighboursWithMines() < 0 ||
                        cell.getNumberOfNeighboursWithMines() > Cell.MAX_NEIGHBOURS) {
                break;
            }
            //@formatter:on
            if (hasMine(neighbour)) {
                //update mine count
                set(cell, cell.getNumberOfNeighboursWithMines() + 1);
            } else if (isClear(neighbour) && (!(isMarked(neighbour) || hasMine(neighbour)))) {
                //recursively update mine counts of all clear neighbours
                updateCellWithMineCount(neighbour);
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
            revealAllMines();
        }
        return win;
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