import java.util.Random;

public class Minesweeper {

    static int revealedCells = 0;
    static int numMines; 

    public static void main(String[] args) {
        System.out.println("Choose difficulty:");
        System.out.println("1 - Easy");
        System.out.println("2 - Medium");
        System.out.println("3 - Hard");

        int difficulty = 0;
        while (difficulty < 1 || difficulty > 3) {
            System.out.print("Enter your choice (1-3): ");
            difficulty = readIntegerFromInput();
            if (difficulty < 1 || difficulty > 3) {
                System.out.println("Please enter a valid number between 1 and 3.");
            }
        }


        revealedCells = 0;

        int size = 0;

        if (difficulty == 1) {
            size = 8;
            numMines = 10;
        } else if (difficulty == 2) {
            size = 12;
            numMines = 20;
        } else if (difficulty == 3) {
            size = 16;
            numMines = 40;
        }

        char[][] grid = new char[size][size];
        boolean[][] revealed = new boolean[size][size];
        boolean[][] flagged = new boolean[size][size];

        initializeGrid(grid);


        long startTime = System.currentTimeMillis();

        playGame(grid, revealed, flagged);


        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000; 
        //The time is in miliseconds so need to divide by 1000
        System.out.println("Time: " + duration + "seconds");
    }

    static void initializeGrid(char[][] grid) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                grid[row][col] = '.';
            }
        }
    }

    static void playGame(char[][] grid, boolean[][] revealed, boolean[][] flagged) {
        boolean gameOver = false;
        boolean firstMove = true;
        boolean isWin = false; 
        // isWin is used in order for the last message to be different for win and loss

        int totalCells = grid.length * grid[0].length;
        int totalNonMineCells = totalCells - numMines;

        while (!gameOver) {
            displayGrid(grid, revealed, flagged);

            char action = ' ';
            int row = -1, col = -1;

            // while loop so the user is required to input the required format
            while (true) {
                System.out.print("Enter: (R to reveal, F to flag/unflag), row and column (e.g., R 1 2): ");
                String input = readLineFromInput().trim();

                if (input.length() < 3) {
                    System.out.println("Invalid input. Try again.");
                    continue;
                }

                action = Character.toUpperCase(input.charAt(0));
                String[] tokens = input.substring(1).trim().split("\\s+");
                if (tokens.length != 2) {
                    System.out.println("Invalid input format. Try again.");
                    continue;
                }
                //this record the "action", which is F or R and splits the number into two "tokens"

                row = stringToInt(tokens[0]) - 1;
                col = stringToInt(tokens[1]) - 1;
                // tis converts the tokens into integers

                if (row < 0 || col < 0) {
                    System.out.println("Invalid row or column number. Please enter positive integers.");
                    continue;
                }

                if (row >= grid.length || col >= grid[0].length) {
                    System.out.println("Coordinates out of bounds. Try again.");
                    continue;
                }

                if (action == 'F') {
                    if (revealed[row][col]) {
                        System.out.println("Cannot flag a revealed cell. Try again.");
                        continue;
                    }
                    break;
                    // when it works, it breaks and jumps out of the while loop
                } else if (action == 'R') {
                    if (flagged[row][col]) {
                        System.out.println("Cannot reveal a flagged cell. Unflag it first. Try again.");
                        continue;
                    }
                    if (revealed[row][col]) {
                        System.out.println("Cell already revealed. Try a different cell.");
                        continue;
                    }
                    break;
                } else {
                    System.out.println("Invalid action. Use R to reveal or F to flag/unflag.");
                }
            }

            if (action == 'F') { 
                flagged[row][col] = !flagged[row][col];
                System.out.println(flagged[row][col] ? "Cell flagged!" : "Flag removed!");
            } else if (action == 'R') { 
                if (firstMove) {
                    grid = initializeGridForFirstMove(grid, row, col);
                    calculateNumbers(grid);
                    firstMove = false;

                    totalNonMineCells = totalCells - numMines;
                }

                if (grid[row][col] == '*') {
                    revealed[row][col] = true;
                    gameOver = true;
                    isWin = false;
                } else {
                    revealCell(grid, revealed, row, col);

                    if (revealedCells == totalNonMineCells) {
                        gameOver = true;
                        isWin = true;
                    }
                }
            }
        }


        if (isWin) {
            System.out.println("You won the game!");
        } else {
            System.out.println("Game Over! You hit a mine.");
        }


        for (int i = 0; i < revealed.length; i++) {
            for (int j = 0; j < revealed[i].length; j++) {
                revealed[i][j] = true;
            }
        }
        //reveals the entire grid

        System.out.println("Final Grid:");
        displayGrid(grid, revealed, flagged);
    }

    static String readLineFromInput() {
        StringBuilder inputBuilder = new StringBuilder();
        int ch;
        while (true) {
            ch = readChar();
            if (ch == '\n' || ch == -1) {
                break;
            }
            if (ch != '\r') { // Ignore carriage returns
                inputBuilder.append((char) ch);
            }
        }
        return inputBuilder.toString();
    }
    // gets the input from the user and returns it into a string

    static int readChar() {
        try {
            return System.in.read();
        } catch (Exception e) {
            return -1;
        }
    }
    //find errors and read the character R or F

    static int stringToInt(String s) {
        int number = 0;
        if (s == null || s.length() == 0) {
            return -1; 
            // Invalid input
        }
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= '0' && ch <= '9') {
                number = number * 10 + (ch - '0');
            } else {
                return -1; 
                // Invalid character
            }
        }
        return number;
    }

    static int readIntegerFromInput() {
        String line = readLineFromInput().trim();
        return stringToInt(line);
    }

    static char[][] initializeGridForFirstMove(char[][] grid, int safeRow, int safeCol) {
        char[][] newGrid = new char[grid.length][grid[0].length];
        initializeGrid(newGrid);
        numMines = placeMinesWithSafeArea(newGrid, numMines, safeRow, safeCol); // Update numMines
        return newGrid;
    }

    static int placeMinesWithSafeArea(char[][] grid, int numMines, int safeRow, int safeCol) {
        int totalCells = grid.length * grid[0].length;
        int safeAreaSize = getSafeAreaSize(safeRow, safeCol, grid.length, grid[0].length);
        int maxPossibleMines = totalCells - safeAreaSize;

        if (numMines > maxPossibleMines) {
            numMines = maxPossibleMines;
            System.out.println("Adjusted number of mines to " + numMines + " due to safe area constraints.");
        }

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

        return placedMines; // Return actual number of mines placed
    }

    static int getSafeAreaSize(int safeRow, int safeCol, int numRows, int numCols) {
        int safeAreaSize = 0;
        for (int row = safeRow - 1; row <= safeRow + 1; row++) {
            for (int col = safeCol - 1; col <= safeCol + 1; col++) {
                if (row >= 0 && row < numRows && col >= 0 && col < numCols) {
                    safeAreaSize++;
                }
            }
        }
        return safeAreaSize;
    }

    static void revealCell(char[][] grid, boolean[][] revealed, int row, int col) {
        if (revealed[row][col]) {
            return;
        }

        revealed[row][col] = true;
        revealedCells++;

        if (grid[row][col] == '0') {
            int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
            int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

            for (int i = 0; i < 8; i++) {
                int newRow = row + dx[i];
                int newCol = col + dy[i];

                if (newRow >= 0 && newRow < grid.length &&
                    newCol >= 0 && newCol < grid[0].length &&
                    grid[newRow][newCol] != '*' &&
                    !revealed[newRow][newCol]) {
                    revealCell(grid, revealed, newRow, newCol);
                }
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
        System.out.print("   "); // Top row header
        for (int col = 0; col < grid[0].length; col++) {
            System.out.printf("%3d", col + 1); // Print column numbers with alignment
        }
        System.out.println();

        for (int row = 0; row < grid.length; row++) {
            System.out.printf("%3d", row + 1); // Print row numbers with alignment
            for (int col = 0; col < grid[row].length; col++) {
                if (flagged[row][col]) {
                    System.out.print("  F");
                } else if (revealed[row][col]) {
                    System.out.printf("%3c", grid[row][col]);
                } else {
                    System.out.print("  .");
                }
            }
            System.out.println();
        }
    }
}
