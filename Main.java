import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

class RetrieveNotes {
    1
    public static void openFile(String filePath) throws IOException {
        File file = new File(filePath);
        Desktop.getDesktop().open(file);
    }
}

class Main {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("Notes Manager : ");
        System.out.println("1. Retrieve notes");
        System.out.println("2. Store notes");
        
        int choice = sc.nextInt();
        
        if (choice == 1) {
            
            String path = "enter path for a document here"; 
            RetrieveNotes.openFile(path);
        } 
        else if (choice == 2) {
            
            System.out.println("Store notes selected.");
        } 
        else {
            System.out.println("Invalid choice");
        }
        
        sc.close();
    }
}