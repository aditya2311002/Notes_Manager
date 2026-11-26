import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Shared location of the notes folder. */
class NotesConfig {
    static final Path NOTES_DIR = Path.of("./notes/");

    static void ensureDir() throws IOException {
        if (!Files.exists(NOTES_DIR)) {
            Files.createDirectories(NOTES_DIR);
        }
    }

    /** Returns all PDFs in the notes folder, sorted by name. */
    static List<Path> listNotes() throws IOException {
        ensureDir();
        try (Stream<Path> stream = Files.list(NOTES_DIR)) {
            return stream
                    .filter(p -> Files.isRegularFile(p))
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".pdf"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }
}

class RetrieveNotes {
    /** Prints a numbered list of saved notes. Returns the list so callers can pick from it. */
    public static List<Path> showNotes() throws IOException {
        List<Path> notes = NotesConfig.listNotes();
        if (notes.isEmpty()) {
            System.out.println("No notes saved yet.");
        } else {
            System.out.println("\nSaved notes:");
            for (int i = 0; i < notes.size(); i++) {
                String name = notes.get(i).getFileName().toString();
                name = name.substring(0, name.length() - 4); // hide ".pdf"
                System.out.println("  " + (i + 1) + ". " + name);
            }
        }
        return notes;
    }

    public static void openFile(Path path) throws IOException {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            System.out.println("Opening files isn't supported here. File is at: " + path.toAbsolutePath());
            return;
        }
        Desktop.getDesktop().open(path.toFile());
    }
}

class StoreNotes {
    /** Cleans up a path that was drag-and-dropped into the terminal. */
    static String cleanDroppedPath(String raw) {
        String s = raw.trim();

        // PowerShell (e.g. VS Code terminal) prefixes dropped paths with "& "
        if (s.startsWith("&")) {
            s = s.substring(1).trim();
        }

        // Windows / some terminals wrap the path in quotes
        if (s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\""))
                || (s.startsWith("'") && s.endsWith("'")))) {
            boolean single = s.startsWith("'");
            s = s.substring(1, s.length() - 1);
            // PowerShell escapes a literal ' inside single quotes as ''
            if (single) {
                s = s.replace("''", "'");
            }
        }

        // macOS / Linux terminals escape spaces as "\ "
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            s = s.replace("\\ ", " ");
        }

        // Some terminals paste a file:// URI
        if (s.startsWith("file://")) {
            s = s.substring(7);
        }
        return s.trim();
    }

    /** Removes characters that are illegal in file names. */
    static String sanitizeTitle(String title) {
        return title.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    public static void savePdfWithTitle(String droppedPathStr, String customTitle, Scanner sc) throws IOException {
        Path sourcePath;
        try {
            sourcePath = Path.of(cleanDroppedPath(droppedPathStr));
        } catch (java.nio.file.InvalidPathException e) {
            System.out.println("Error: That doesn't look like a valid file path.");
            return;
        }

        if (!Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
            System.out.println("Error: The dropped file could not be found.");
            return;
        }
        if (!sourcePath.getFileName().toString().toLowerCase().endsWith(".pdf")) {
            System.out.println("Error: Only PDF files can be stored.");
            return;
        }

        String title = sanitizeTitle(customTitle);
        if (title.isEmpty()) {
            // Fall back to the original file name (without extension)
            String original = sourcePath.getFileName().toString();
            title = original.substring(0, original.length() - 4);
            System.out.println("No title entered, using: " + title);
        }

        NotesConfig.ensureDir();
        Path destinationPath = NotesConfig.NOTES_DIR.resolve(title + ".pdf");

        if (Files.exists(destinationPath)) {
            System.out.print("A note named \"" + title + "\" already exists. Overwrite? (y/n): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println("Cancelled.");
                return;
            }
        }

        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Note saved successfully as: " + destinationPath.toAbsolutePath());
    }
}

class DeleteNotes {
    public static void deleteNote(Path path, Scanner sc) throws IOException {
        System.out.print("Delete \"" + path.getFileName() + "\"? This can't be undone (y/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            Files.delete(path);
            System.out.println("Note deleted.");
        } else {
            System.out.println("Cancelled.");
        }
    }
}

class Main {
    /** Asks the user to pick a note by number. Returns null if invalid or cancelled. */
    static Path pickNote(Scanner sc, List<Path> notes) {
        if (notes.isEmpty()) return null;
        System.out.print("Enter note number (0 to cancel): ");
        String input = sc.nextLine().trim();
        try {
            int n = Integer.parseInt(input);
            if (n == 0) return null;
            if (n < 1 || n > notes.size()) {
                System.out.println("Invalid number.");
                return null;
            }
            return notes.get(n - 1);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a number.");
            return null;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== Notes Manager =====");
            System.out.println("1. Retrieve a note (open PDF)");
            System.out.println("2. Store a PDF (drag & drop)");
            System.out.println("3. Delete a note");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            String line = sc.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                continue;
            }

            try {
                switch (choice) {
                    case 1: {
                        List<Path> notes = RetrieveNotes.showNotes();
                        Path selected = pickNote(sc, notes);
                        if (selected != null) {
                            RetrieveNotes.openFile(selected);
                        }
                        break;
                    }
                    case 2: {
                        System.out.println("\n--- Store PDF Note ---");
                        System.out.print("1. Drag and drop your PDF file into this window and press Enter: ");
                        String droppedPath = sc.nextLine();

                        System.out.print("2. Enter a custom title/name for this note: ");
                        String customTitle = sc.nextLine();

                        StoreNotes.savePdfWithTitle(droppedPath, customTitle, sc);
                        break;
                    }
                    case 3: {
                        List<Path> notes = RetrieveNotes.showNotes();
                        Path selected = pickNote(sc, notes);
                        if (selected != null) {
                            DeleteNotes.deleteNote(selected, sc);
                        }
                        break;
                    }
                    case 4:
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice");
                }
            } catch (IOException e) {
                System.out.println("Something went wrong: " + e.getMessage());
            }
        }

        sc.close();
    }
}
