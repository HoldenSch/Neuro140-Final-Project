import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.List;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;

import engine.core.MarioGame;
import engine.core.MarioResult;

public class PlayLevel {
    public static class LevelStats {
        String levelName;
        double completionPercentage;
        int coins;
        int kills;
        int jumps;
        double maxJumpLength;
        boolean completed;
        
        public LevelStats(String name, MarioResult result) {
            this.levelName = name;
            this.completionPercentage = result.getCompletionPercentage();
            this.coins = result.getCurrentCoins();
            this.kills = result.getKillsTotal();
            this.jumps = result.getNumJumps();
            this.maxJumpLength = result.getMaxXJump();
            this.completed = result.getGameStatus().toString().equals("WIN");
        }
        
        @Override
        public String toString() {
            return String.format("Level: %s\n  Completed: %s\n  Completion%%: %.2f%%\n  Coins: %d\n  Kills: %d\n  Jumps: %d\n  Max Jump Length: %.2f\n",
                levelName, completed ? "YES" : "NO", completionPercentage, coins, kills, jumps, maxJumpLength);
        }
    }

    public static void printResults(MarioResult result) {
        System.out.println("****************************************************************");
        System.out.println("Game Status: " + result.getGameStatus().toString() +
                " Percentage Completion: " + result.getCompletionPercentage());
        System.out.println("Lives: " + result.getCurrentLives() + " Coins: " + result.getCurrentCoins() +
                " Remaining Time: " + (int) Math.ceil(result.getRemainingTime() / 1000f));
        System.out.println("Mario State: " + result.getMarioMode() +
                " (Mushrooms: " + result.getNumCollectedMushrooms() + " Fire Flowers: " + result.getNumCollectedFireflower() + ")");
        System.out.println("Total Kills: " + result.getKillsTotal() + " (Stomps: " + result.getKillsByStomp() +
                " Fireballs: " + result.getKillsByFire() + " Shells: " + result.getKillsByShell() +
                " Falls: " + result.getKillsByFall() + ")");
        System.out.println("Bricks: " + result.getNumDestroyedBricks() + " Jumps: " + result.getNumJumps() +
                " Max X Jump: " + result.getMaxXJump() + " Max Air Time: " + result.getMaxJumpAirTime());
        System.out.println("****************************************************************");
    }

    public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
            System.err.println("Error reading level file: " + filepath);
            e.printStackTrace();
        }
        return content;
    }

    public static List<Path> getLevelFiles(String folderPath) {
        List<Path> levelFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".txt"))
                 .forEach(levelFiles::add);
        } catch (IOException e) {
            System.err.println("Error reading directory: " + folderPath);
            e.printStackTrace();
        }
        return levelFiles;
    }

    public static void printStatsSummary(List<LevelStats> allStats) {
        int totalLevels = allStats.size();
        long completedLevels = allStats.stream().filter(s -> s.completed).count();
        
        DoubleSummaryStatistics completionStats = allStats.stream()
            .mapToDouble(s -> s.completionPercentage)
            .summaryStatistics();
            
        System.out.println("\n=== OVERALL STATISTICS ===");
        System.out.printf("Total Levels Evaluated: %d\n", totalLevels);
        System.out.printf("Levels Completed: %d (%.2f%%)\n", completedLevels, (completedLevels * 100.0) / totalLevels);
        System.out.printf("Average Completion: %.2f%%\n", completionStats.getAverage());
        System.out.printf("Min Completion: %.2f%%\n", completionStats.getMin());
        System.out.printf("Max Completion: %.2f%%\n", completionStats.getMax());
    }

    public static void main(String[] args) {
        // Specify the folder containing your levels here
        String levelsFolder = "levels/holden_levels";  
        
        List<Path> levelFiles = getLevelFiles(levelsFolder);
        List<LevelStats> allStats = new ArrayList<>();
        MarioGame game = new MarioGame();
        
        System.out.println("Evaluating levels in folder: " + levelsFolder);
        System.out.println("Found " + levelFiles.size() + " level files\n");
        
        for (Path levelFile : levelFiles) {
            System.out.println("Testing level: " + levelFile.getFileName());
            MarioResult result = game.runGame(new agents.robinBaumgarten.Agent(), 
                                            getLevel(levelFile.toString()), 
                                            20, 0, true);
            
            LevelStats stats = new LevelStats(levelFile.getFileName().toString(), result);
            allStats.add(stats);
            System.out.println(stats);
        }
        
        printStatsSummary(allStats);
    }
}
