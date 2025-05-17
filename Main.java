import model.*;
import system.LibrarySystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LibrarySystem lib = new LibrarySystem();
        boolean running=true;
        while (running) {
            System.out.println("\n---- Library Menu ----");
            System.out.println("1. Add Book");
            System.out.println("2. Register User");
            System.out.println("3. Borrow Book");
            System.out.println("4. Return Book");
            System.out.println("5. View All Books");
            System.out.println("6. Search Book");
            System.out.println("7. View My Borrowed Books & Due Dates");
            System.out.println("8. Exit");

            System.out.print("Choose: ");

            int choice = sc.nextInt();
            try {
                switch (choice) {
                    case 1 -> {
                        System.out.println("Enter ID: ");
                        int searchID = sc.nextInt();
                        lib.authorized(searchID);
                        System.out.print("Title: ");
                        sc.nextLine(); // consume newline
                        String title = sc.nextLine();
                        System.out.print("Author: ");
                        String author = sc.nextLine();
                        System.out.print("Total Copies: ");
                        int total = sc.nextInt();
                        lib.addBook(new Book(0, title, author, total, total));
                    }
                    case 2 -> {
                        System.out.print("Name: ");
                        sc.nextLine();
                        String name = sc.nextLine();
                        System.out.print("Is student? (true/false): ");
                        boolean isStudent = sc.nextBoolean();
                        System.out.print("Mobile: ");
                        long mobile = sc.nextLong();
                        User u = isStudent ? new Student(0, name, mobile) : new Librarian(0, name, mobile);
                        lib.registerUser(u);
                    }
                    case 3 -> {
                        System.out.print("User ID: ");
                        int uid = sc.nextInt();
                        System.out.print("Book ID: ");
                        int bid = sc.nextInt();
                        lib.borrowBook(uid, bid);
                    }
                    case 4 -> {
                        System.out.print("User ID: ");
                        int uid = sc.nextInt();
                        System.out.print("Book ID: ");
                        int bid = sc.nextInt();
                        lib.returnBook(uid, bid);
                    }
                    case 5 -> {
                        System.out.println("Enter ID: ");
                        int search_ID= sc.nextInt();
                        lib.authorized(search_ID);
                        lib.listAllBooks();
                    }
                    case 6 -> {
                        System.out.println("Search By: 1) Title  2) Author  3) Book ID");
                        int opt = sc.nextInt();
                        sc.nextLine(); // consume newline

                        String key;
                        if (opt == 3) {
                            System.out.print("Enter Book ID: ");
                            key = String.valueOf(sc.nextInt());
                        } else {
                            System.out.print("Enter search keyword: ");
                            key = sc.nextLine();
                        }

                        lib.searchBooksByOption(opt, key);
                    }
                    case 7 -> {
                        System.out.print("Enter your User ID: ");
                        int uid = sc.nextInt();
                        lib.checkDueAndFine(uid);
                    }
                    case 8 -> {
                        System.out.println("Exiting Menu");
                        running=false;
                    }
                    default -> System.out.println("Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        }
        sc.close();

    }
}
