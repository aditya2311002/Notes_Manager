import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

class RetrieveNotes {
    public static void openFile(String filePath) throws IOException {
        File file = new File(filePath);
        Desktop.getDesktop().open(file);
    }
}

class StoreNotes {
    public static void savePdfWithTitle(String droppedPathStr, String customTitle) throws IOException {
        // Clean up quotes if the terminal wraps the dragged path in quotation marks
        droppedPathStr = droppedPathStr.replace("\"", "").trim();
        
        Path sourcePath = Path.of(droppedPathStr);
        
        // Check if the file exists
        if (!Files.exists(sourcePath)) {
            System.out.println("Error: The dropped file could not be found.");
            return;
        }

        // Create the ./notes/ directory if it doesn't exist
        Path dirPath = Path.of("./notes/");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        // Save the file using the user's custom title with a .pdf extension
        Path destinationPath = dirPath.resolve(customTitle + ".pdf");
        
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Note saved successfully as: " + destinationPath.toAbsolutePath());
    }
}

class Main {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("Notes Manager : ");
        System.out.println("1. Retrieve notes (Open PDF)");
        System.out.println("2. Store a PDF (Drag & Drop)");
        System.out.print("Enter your choice: ");
        
        int choice = sc.nextInt();
        sc.nextLine(); // Consume the leftover newline character
        
        if (choice == 1) {
            String path = "C:/Users/Lenovo/Desktop/FREE LLM API ( 7B Tokens ).pdf";
            RetrieveNotes.openFile(path);
        } 
        else if (choice == 2) {
            System.out.println("\n--- Store PDF Note ---");
            System.out.print("1. Drag and drop your PDF file into this window and press Enter: ");
            String droppedPath = sc.nextLine();
            
            System.out.print("2. Enter a custom title/name for this note: ");
            String customTitle = sc.nextLine();
            
            StoreNotes.savePdfWithTitle(droppedPath, customTitle);
        } 
        else {
            System.out.println("Invalid choice");
        }
        
        sc.close();
    }
}