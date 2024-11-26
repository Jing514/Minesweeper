import java.util.Random;
import java.util.Scanner;

public class Minesweeper {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Minesweeper!");
        System.out.println("Choose difficulty:");
        System.out.println("1. Easy (8x8 grid, 10 mines)");
        System.out.println("2. Medium (12x12 grid, 20 mines)");
        System.out.println("3. Hard (16x16 grid, 40 mines)");

        int difficulty = 0;
        while (difficulty < 1 || difficulty > 3) {
            System.out.print("Enter your choice (1-3): ");
            difficulty = scanner.nextInt();
        }

        int size = 0;
        int numMines = 0;

        // Set grid size and mine count based on difficulty
        switch (difficulty) {
            case 1: // Easy
                size = 8;
                numMines = 10;
                break;
            case 2: // Medium
                size = 12;
                numMines = 20;
                break;
            case 3: // Hard
                size = 16;
                numMines = 40;
                break;
        }

        char[][] grid = new char[size][size];
        boolean[][] revealed = new boolean[size][size];
        boolean[][] flagged = new boolean[size][size];

        initializeGrid(grid);
        placeMines(grid, numMines);

        playGame(grid, revealed, flagged);
    }

    static void initializeGrid(char[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = '.';
            }
        }
    }

    static void placeMines(char[][] grid, int numMines) {
        Random rand = new Random();
        int placedMines = 0;

        while (placedMines < numMines) {
            int row = rand.nextInt(grid.length);
            int col = rand.nextInt(grid[0].length);

            if (grid[row][col] != '*') {
                grid[row][col] = '*';
                placedMines++;
            }
        }
    }

    static void calculateNumbers(char[][] grid) {
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == '*') continue;

                int count = 0;
                for (int i = 0; i < 8; i++) {
                    int newRow = row + dx[i];
                    int newCol = col + dy[i];

                    if (newRow >= 0 && newRow < grid.length &&
                        newCol >= 0 && newCol < grid[0].length &&
                        grid[newRow][newCol] == '*') {
                        count++;
                    }
                }
                grid[row][col] = (char) (count + '0');
            }
        }
    }

    static void displayGrid(char[][] grid, boolean[][] revealed, boolean[][] flagged) {
        System.out.print("   ");
        for (int col = 0; col < grid[0].length; col++) {
            System.out.print((col + 1) + " ");
        }
        System.out.println();

        for (int row = 0; row < grid.length; row++) {
            System.out.print((row + 1) + " ");
            for (int col = 0; col < grid[row].length; col++) {
                if (flagged[row][col]) {
                    System.out.print("F "); // Show flagged cells
                } else if (revealed[row][col]) {
                    System.out.print(grid[row][col] + " "); // Show revealed cells
                } else {
                    System.out.print(". "); // Show hidden cells
                }
            }
            System.out.println();
        }
    }

    static void playGame(char[][] grid, boolean[][] revealed, boolean[][] flagged) {
        Scanner scanner = new Scanner(System.in);
        boolean gameOver = false;
        boolean firstMove = true;
        int revealedCells = 0;
        int totalCells = grid.length * grid[0].length;
        int totalNonMineCells = totalCells - countMines(grid);

        while (!gameOver) {
            displayGrid(grid, revealed, flagged);

            System.out.print("Enter action (R to reveal, F to flag/unflag), row and column (e.g., R 1 2): ");
            char action = scanner.next().toUpperCase().charAt(0);
            int row = scanner.nextInt() - 1;
            int col = scanner.nextInt() - 1;

            if (row < 0 || row >= grid.length || col < 0 || col >= grid[0].length) {
                System.out.println("Invalid input. Try again.");
                continue;
            }

            if (action == 'F') { // Toggle flag
                if (revealed[row][col]) {
                    System.out.println("You cannot flag a revealed cell.");
                } else {
                    flagged[row][col] = !flagged[row][col];
                    System.out.println(flagged[row][col] ? "Cell flagged!" : "Flag removed!");
                }
            } else if (action == 'R') { // Reveal a cell
                if (flagged[row][col]) {
                    System.out.println("Cannot reveal a flagged cell. Remove the flag first.");
                } else if (firstMove) {
                    grid = initializeGridForFirstMove(grid, row, col, totalCells - totalNonMineCells);
                    calculateNumbers(grid);
                    firstMove = false;
                }

                if (grid[row][col] == '*') {
                    System.out.println("Game Over! You hit a mine.");
                    gameOver = true;
                } else {
                    revealed[row][col] = true;
                    revealedCells++;
                    if (grid[row][col] == '0') {
                        revealEmptyCells(grid, revealed, row, col);
                    }

                    if (revealedCells == totalNonMineCells) {
                        System.out.println("Congratulations! You cleared the board!");
                        gameOver = true;
                    }
                }
            } else {
                System.out.println("Invalid action. Use R to reveal or F to flag.");
            }
        }

        System.out.println("Final Grid:");
        for (int i = 0; i < revealed.length; i++) {
            for (int j = 0; j < revealed[i].length; j++) {
                revealed[i][j] = true;
            }
        }
        displayGrid(grid, revealed, flagged);
    }

    static char[][] initializeGridForFirstMove(char[][] grid, int safeRow, int safeCol, int numMines) {
        char[][] newGrid = new char[grid.length][grid[0].length];
        initializeGrid(newGrid);
        placeMinesWithSafeArea(newGrid, numMines, safeRow, safeCol);
        return newGrid;
    }

    static void placeMinesWithSafeArea(char[][] grid, int numMines, int safeRow, int safeCol) {
        Random rand = new Random();
        int placedMines = 0;

        while (placedMines < numMines) {
            int row = rand.nextInt(grid.length);
            int col = rand.nextInt(grid[0].length);

            if ((Math.abs(row - safeRow) <= 1 && Math.abs(col - safeCol) <= 1) || grid[row][col] == '*') {
                continue;
            }

            grid[row][col] = '*';
            placedMines++;
        }
    }

    static int countMines(char[][] grid) {
        int count = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == '*') {
                    count++;
                }
            }
        }
        return count;
    }

    static void revealEmptyCells(char[][] grid, boolean[][] revealed, int row, int col) {
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int newRow = row + dx[i];
            int newCol = col + dy[i];

            if (newRow >= 0 && newRow < grid.length &&
                newCol >= 0 && newCol < grid[0].length &&
                !revealed[newRow][newCol] &&
                grid[newRow][newCol] != '*') {
                revealed[newRow][newCol] = true;

                if (grid[newRow][newCol] == '0') {
                    revealEmptyCells(grid, revealed, newRow, newCol);
                }
            }
        }
    }
}